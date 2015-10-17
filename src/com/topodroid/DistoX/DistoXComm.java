/* @file DistoXComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid-DistoX BlueTooth communication 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.nio.ByteBuffer;

import java.io.IOException;
import java.io.EOFException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
// import java.Thread;


import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.ParcelUuid;
import android.os.AsyncTask;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import android.database.DataSetObserver;

// import android.widget.Toast;
import android.util.Log;

public class DistoXComm
{
  private TopoDroidApp mApp;

  private BluetoothDevice mBTDevice;
  private BluetoothSocket mBTSocket;
  private String mAddress;
  private DistoXProtocol mProtocol;
  public boolean mBTConnected;
  private boolean mCalibMode;   //!< whether the device is in calib-mode


  private static final byte CALIB_BIT = (byte)0x08;
  public byte[] mCoeff;

  // private int   mHead;
  // private int   mTail;
  // public int Head()      { return mHead; }
  // public int Tail()      { return mTail; }

// -----------------------------------------------------------
// Bluetooth receiver

  private BroadcastReceiver mBTReceiver = null;

  private void resetBTReceiver()
  {
    if ( mBTReceiver == null ) return;
    TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "reset BT receiver");
    mApp.unregisterReceiver( mBTReceiver );
    mBTReceiver = null;
  }

  private void setupBTReceiver()
  {
    resetBTReceiver();
    TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "setup BT receiver");
    mBTReceiver = new BroadcastReceiver() 
    {
      @Override
      public void onReceive( Context ctx, Intent data )
      {
        String action = data.getAction();
        // if ( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals( action ) ) {
        // } else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
        // } else if ( BluetoothDevice.ACTION_FOUND.equals( action ) ) {
        if ( BluetoothDevice.ACTION_ACL_CONNECTED.equals( action ) ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_BT, "ACL_CONNECTED");
          mApp.mDataDownloader.setConnected( true );
          mApp.notifyStatus();
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_BT, "ACL_DISCONNECT_REQUESTED");
          mApp.mDataDownloader.setConnected( false );
          mApp.notifyStatus();
          closeSocket( );
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECTED.equals( action ) ) {
          Bundle extra = data.getExtras();
          String device = (extra!=null)? extra.getString( BluetoothDevice.EXTRA_DEVICE ) : "undefined";
          TopoDroidLog.Log( TopoDroidLog.LOG_BT, "DistoXComm received ACL_DISCONNECTED from " + device );
          mApp.mDataDownloader.setConnected( false );
          mApp.notifyStatus();
          closeSocket( );
          mApp.notifyDisconnected();
        } else if ( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals( action ) ) { // NOT USED
          final int state     = data.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
          final int prevState = data.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
          if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
            TopoDroidLog.Log( TopoDroidLog.LOG_BT, "BOND STATE CHANGED paired" );
          } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
            TopoDroidLog.Log( TopoDroidLog.LOG_BT, "BOND STATE CHANGED unpaired" );
          } else {
            TopoDroidLog.Log( TopoDroidLog.LOG_BT, "BOND STATE CHANGED ");
          }

          // DeviceUtil.bind2Device( data );
        // } else if ( BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action) ) {
        //   Log.v("DistoX", "PAIRING REQUEST");
        //   // BluetoothDevice device = getDevice();
        //   // //To avoid the popup notification:
        //   // device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
        //   // device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device, true);
        //   // byte[] pin = ByteBuffer.allocate(4).putInt(0000).array();
        //   // //Entering pin programmatically:  
        //   // Method ms = device.getClass().getMethod("setPin", byte[].class);
        //   // //Method ms = device.getClass().getMethod("setPasskey", int.class);
        //   // ms.invoke(device, pin);
        //     
        //   //Bonding the device:
        //   // Method mm = device.getClass().getMethod("createBond", (Class[]) null);
        //   // mm.invoke(device, (Object[]) null);
        }
      }
    };


    // mApp.registerReceiver( mBTReceiver, new IntentFilter( BluetoothDevice.ACTION_FOUND ) );
    // mApp.registerReceiver( mBTReceiver, new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_STARTED ) );
    // mApp.registerReceiver( mBTReceiver, new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED ) );
    mApp.registerReceiver( mBTReceiver, new IntentFilter( BluetoothDevice.ACTION_ACL_CONNECTED ) );
    mApp.registerReceiver( mBTReceiver, new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED ) );
    mApp.registerReceiver( mBTReceiver, new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECTED ) );
    // mApp.registerReceiver( mBTReceiver, uuidFilter  = new IntentFilter( myUUIDaction ) );
    mApp.registerReceiver( mBTReceiver, new IntentFilter( BluetoothDevice.ACTION_BOND_STATE_CHANGED ) );
    // mApp.registerReceiver( mBTReceiver, new IntentFilter( BluetoothDevice.ACTION_PAIRING_REQUEST ) );
  }

// --------------------------------------------------

  int nReadPackets;   // number of received data-packet
  long mLastShotId;   // last shot id
  boolean doWork = true;

  private class RfcommThread extends Thread
  {
    private DistoXProtocol mProto;
    private int toRead; // number of packet to read
    private ILister mLister;

    void cancelWork()
    {
      if ( mProto != null ) mProto.mMaxTimeout = 0;
      doWork = false;
    }

    /** 
     * @param protocol    communication protocol
     * @param to_read     number of data to read (use -1 to read forever until timeout or an exception)
     */
    public RfcommThread( DistoXProtocol protocol, int to_read, ILister lister )
    {
      nReadPackets = 0; // reset nr of read packets
      toRead = to_read;
      mProto = protocol;
      mLister = lister;
      // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "RFcommThread cstr ToRead " + toRead );
    }

    public void run()
    {
      boolean hasG = false;
      doWork = true;

      // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "RFcomm thread running ... to_read " + toRead );
      while ( doWork && nReadPackets != toRead ) {
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "RFcomm loop: read " + nReadPackets + " to-read " + toRead );
        
        int res = mProto.readPacket( toRead >= 0 );
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "RFcomm readPacket returns " + res );
        if ( res == DistoXProtocol.DISTOX_PACKET_NONE ) {
          if ( toRead == -1 ) {
            doWork = false;
          } else {
            try {
              // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "RFcomm sleeping 2000 " );
              Thread.sleep( 2000 );
            } catch (InterruptedException e) {
              TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "RFcomm thread sleep interrupt");
            }
          }
        } else if ( res == DistoXProtocol.DISTOX_ERR_OFF ) {
          // tell the user !
          // Toast.makeText( mApp.getApplicationContext(), R.string.device_off, Toast.LENGTH_SHORT).show()
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "RFcomm readPacket returns ERR_OFF " );
          doWork = false;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_DATA ) {
          ++nReadPackets;
          double d = mProto.mDistance;
          double b = mProto.mBearing;
          double c = mProto.mClino;
          double r = mProto.mRoll;
          long extend = mApp.computeLegExtend( b ); // FIXME-EXTEND
          TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "DATA PACKET " + d + " " + b + " " + c );
          // NOTE type=0 shot is DistoX-type
          mLastShotId = mApp.mData.insertShot( mApp.mSID, -1L, d, b, c, r, extend, 0, true );
          if ( mLister != null ) {
            DistoXDBlock blk = new DistoXDBlock( );
            blk.setId( mLastShotId, mApp.mSID );
            blk.mLength  = (float)d;
            blk.mBearing = (float)b;
            blk.mClino   = (float)c;
            blk.mRoll    = (float)r;
            mLister.updateBlockList( blk );
          }
        } else if ( res == DistoXProtocol.DISTOX_PACKET_G ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "G PACKET" );
          ++nReadPackets;
          hasG = true;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_M ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "M PACKET" );
          ++nReadPackets;
          // get G and M from mProto and save them to store
          mApp.mDData.insertGM( mApp.mCID, mProto.mGX, mProto.mGY, mProto.mGZ, mProto.mMX, mProto.mMY, mProto.mMZ );
          hasG = false;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_REPLY ) {
          
          byte[] addr = mProto.getAddress();
          byte[] reply = mProto.getReply();
          StringWriter sw = new StringWriter();
          PrintWriter pw  = new PrintWriter(sw);
          pw.format("%02x %02x %02x %02x at %02x%02x", reply[0], reply[1], reply[2], reply[3], addr[1], addr[0] );
          String result = sw.getBuffer().toString();
          TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "REPLY PACKET: " + result ); 

          if ( addr[0] == (byte)0x00 && addr[1] == (byte)0x80 ) { // 0x8000
            // TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "toggle reply" );
            // if ( (reply[0] & CALIB_BIT) == 0 ) {
            //     mProto.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
            // } else {
            //     mProto.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
            // }
          } else if ( ( addr[1] & (byte)0x80) == (byte)0x80 ) { // REPLY TO READ/WRITE-CALIBs
            // TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "write reply" );
            // mProto.setWrittenCalib( true );
          } else if ( addr[0] == 0x20 && addr[1] == (byte)0xC0 ) { // C020 READ HEAD-TAIL
            // TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "read head-tail reply");
            // mHead = (int)( reply[0] | ( (int)(reply[1]) << 8 ) );
            // mTail = (int)( reply[2] | ( (int)(reply[3]) << 8 ) );
          }
        } else if ( res == DistoXProtocol.DISTOX_PACKET_VECTOR ) {
          // ++nReadPackets;  // vector packet do not count
          double acc  = mProto.mAcceleration;
          double mag  = mProto.mMagnetic;
          double dip  = mProto.mDip;
          double roll = mProto.mRoll;
          TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "VECTOR PACKET " + acc + " " + mag + " " + dip + " " + roll );
          // TODO X310
          if ( mApp.distoType() == Device.DISTO_X310 ) {
            mApp.mData.updateShotAMDR( mLastShotId, mApp.mSID, acc, mag, dip, roll, true );
          }
          try {
            Thread.sleep( 1000 ); // FIXME SLOWDOWN
          } catch ( InterruptedException e ) { }
        }
      }
      // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "RFcomm thread run() exiting");
      mRfcommThread = null;

      // FIXME_COMM
      // mApp.notifyConnState( );
    }
  };

  RfcommThread mRfcommThread;


  DistoXComm( TopoDroidApp app )
  {
    mApp       = app;
    mProtocol  = null;
    mAddress   = null;
    mRfcommThread = null;
    mBTConnected  = false;
    mBTSocket     = null;
    mCalibMode    = false;
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "DistoXComm cstr");
  }

  public void resume()
  {
    // if ( mRfcommThread != null ) { mRfcommThread.resume(); }
  }

  public void suspend()
  {
    // if ( mRfcommThread != null ) { mRfcommThread.suspend(); }
  }


  private void cancelRfcommThread()
  {
    if ( mRfcommThread != null ) {
      // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "cancel Rfcomm thread: thread is active");
      mRfcommThread.cancelWork();
      try {
        mRfcommThread.join();
      } catch ( InterruptedException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "cancel thread interrupt " + e.getMessage() );
      } finally {
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "cancel Rfcomm thread: nulling thread");
        mRfcommThread = null;
      }
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "cancel Rfcomm thread: no thread");
    }
  }

  // -------------------------------------------------------- 
  // SOCKET


  /** close the socket (and the RFcomm thread) but don't delete it
   * alwasy called with wait_thread
   */
  private synchronized void closeSocket( )
  {
    if ( mBTSocket == null ) return;

    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "close socket()" );
    for ( int k=0; k<1 && mBTSocket != null; ++k ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "try close socket nr " + k );
      cancelRfcommThread();
      closeProtocol();
      try {
        mBTSocket.close();
        mBTSocket = null;
      } catch ( IOException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "close socket IOexception " + e.getMessage() );
        TopoDroidLog.LogStackTrace( e );
      } finally {
        mBTConnected = false;
      }
    }
    mBTConnected = false;
  }

  private void closeProtocol()
  {
    // if ( mProtocol != null ) mProtocol.closeIOstreams();
    mProtocol = null;
  }

  /** close the socket and delete it
   * the connection becomes unusable
   * As a matter of fact tghis is alwyas called with wait_thread = true
   */
  private void destroySocket( ) // boolean wait_thread )
  {
    if ( mBTSocket == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "destroy socket()" );
    // closeProtocol(); // already in closeSocket()
    closeSocket();
    // mBTSocket = null;
    resetBTReceiver();
  }


  /** create a socket (not connected)
   *  and a connection protocol on it
   */
  private void createSocket( String address, int port )
  {
    if ( address == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket() addr " + address + " mAddress " + mAddress);
    if ( mProtocol == null || ! address.equals( mAddress ) ) {
      if ( mProtocol != null && ! address.equals( mAddress ) ) {
        disconnectRemoteDevice();
      }
      mBTDevice = mApp.mBTAdapter.getRemoteDevice( address );
 
      // FIXME PAIRING
      if ( ! DeviceUtil.isPaired( mBTDevice ) ) {
        int ret = DeviceUtil.pairDevice( mBTDevice );
        TopoDroidLog.Log( TopoDroidLog.LOG_BT, "pairing device " + ret );
      }

      // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket() device " + mBTDevice.getName() );
      try {
        if ( mBTSocket != null ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket() BTSocket not null ... closing");
          mBTSocket.close();
          mBTSocket = null;
        }
        if ( mBTDevice.getBondState() == BluetoothDevice.BOND_NONE ) {
          DeviceUtil.bindDevice( mBTDevice );
        }

        Class[] classes1 = new Class[]{ int.class };
        Class[] classes2 = new Class[]{ UUID.class };
        if ( TopoDroidSetting.mSockType == TopoDroidSetting.TOPODROID_SOCK_DEFAULT ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket() createRfcommSocketToServiceRecord " );
          mBTSocket = mBTDevice.createRfcommSocketToServiceRecord( UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        } else if ( TopoDroidSetting.mSockType == TopoDroidSetting.TOPODROID_SOCK_INSEC ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket() createInsecureRfcommSocketToServiceRecord " );
          Method m3 = mBTDevice.getClass().getMethod( "createInsecureRfcommSocketToServiceRecord", classes2 );
          mBTSocket = (BluetoothSocket) m3.invoke( mBTDevice, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        } else if ( TopoDroidSetting.mSockType == TopoDroidSetting.TOPODROID_SOCK_INSEC_PORT ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket() invoke createInsecureRfcommSocket " );
          Method m1 = mBTDevice.getClass().getMethod( "createInsecureRfcommSocket", classes1 );
          mBTSocket = (BluetoothSocket) m1.invoke( mBTDevice, port );
          // mBTSocket = mBTDevice.createInsecureRfcommSocket( port );
          // mBTSocket = (BluetoothSocket) m1.invoke( mBTDevice, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        } else if ( TopoDroidSetting.mSockType == TopoDroidSetting.TOPODROID_SOCK_PORT ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket() invoke createInsecureRfcommSocket " );
          Method m2 = mBTDevice.getClass().getMethod( "createRfcommSocket", classes1 );
          mBTSocket = (BluetoothSocket) m2.invoke( mBTDevice, port );
        }

      } catch ( InvocationTargetException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "create Socket invoke target " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( UnsupportedEncodingException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "create Socket encoding " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( NoSuchMethodException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "create Socket no method " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( IllegalAccessException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "create Socket access " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( IOException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "create Socket IO " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      }

      if ( mBTSocket != null ) {
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket OK");
        // mBTSocket.setSoTimeout( 200 ); // BlueToothSocket does not have timeout 
        mProtocol = new DistoXProtocol( mBTSocket, mApp.mDevice );
        mAddress = address;
      } else {
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "create Socket fail");
        if ( mProtocol != null ) mProtocol.closeIOstreams();
        mProtocol = null;
        mAddress = null;
      }
      mBTConnected = false; // socket is created but not connected
    }
  }

  /** get the list of UUIDs supported by the remote device (for the DistoX only SPP uuid)
   */
  private void getUuids()
  {
    if ( mBTDevice == null ) return;
    try {
      Class cl = Class.forName("android.bluetooth.BluetoothDevice");
      Class[] pars = {};
      Method m0 = cl.getMethod( "getUuids", pars );
      Object[] args = {};
      ParcelUuid[] uuids = (ParcelUuid[]) m0.invoke( mBTDevice, args );
      // if ( uuids != null ) {
      //   for ( ParcelUuid uid : uuids ) {
      //     TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "uuid " + uid.toString() );
      //   }
      // }
    } catch ( Exception e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "get uuids error " + e.getMessage() );
    }
  }

  /** connect the socket to the device
   */
  private boolean connectSocket( String address )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "connect socket(): " + address );
    if ( address == null ) return false;
    createSocket( address, 1 ); // default port == 1

    // DEBUG
    getUuids();

    if ( mBTSocket != null ) {
      mApp.mBTAdapter.cancelDiscovery();
      setupBTReceiver();

      int port = 0;
      while ( ! mBTConnected && port < TopoDroidSetting.mCommRetry ) {
        ++ port;
        if ( mBTSocket != null ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "connect socket() try port " + port );
          try {
            mBTSocket.connect();
            mBTConnected = true;
          } catch ( IOException e ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "connect socket() (port " + port + ") IO error " + e.getMessage() );
            TopoDroidLog.LogStackTrace( e );
            closeSocket();
            // mBTSocket = null;
          }
        }
        if ( mBTSocket == null && port < TopoDroidSetting.mCommRetry ) {
          createSocket( address, port );
        }
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "connect socket() port " + port + " connected " + mBTConnected );
      }
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "connect socket() null socket");
    }
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "connect socket() result " + mBTConnected );
    return mBTConnected;
  }

  private boolean startRfcommThread( int to_read, ILister lister )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "start RFcomm thread: to_read " + to_read );
    if ( mBTSocket != null ) {
      if ( mRfcommThread == null ) {
        mRfcommThread = new RfcommThread( mProtocol, to_read, lister );
        mRfcommThread.start();
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "startRFcommThread started");
      } else {
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "startRFcommThread already running");
      }
      return true;
    } else {
      mRfcommThread = null;
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "startRFcommThread: null socket");
      return false;
    }
  }

  // -------------------------------------------------------- 
  
  // public boolean connectRemoteDevice( String address, ArrayList<ILister> lister )
  // {
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "connect remote device: address " + address );
  //   if ( connectSocket( address ) ) {
  //     if ( mProtocol != null ) {
  //       return startRfcommThread( -1, lister );
  //     }
  //     TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "connect RemoteDevice null protocol");
  //   } else {
  //     TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "connect RemoteDevice failed on connectSocket()" );
  //   }
  //   destroySocket( );
  //   return false;
  // }

  public void disconnectRemoteDevice( )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "disconnect remote device ");
    cancelRfcommThread();
    closeProtocol();
    destroySocket( );
  }

  private boolean checkRfcommThreadNull( String msg )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, msg );
    return ( mRfcommThread == null );
  }

  private boolean sendCommand( int cmd )
  {
    boolean ret = false;
    if ( mProtocol != null ) {
      for (int k=0; k<3 && ! ret; ++k ) { // try three times
        ret |= mProtocol.sendCommand( (byte)cmd ); 
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "sendCommand " + cmd + " " + k + "-ret " + ret );
        try {
          Thread.sleep( 100 );
        } catch ( InterruptedException e ) {
        }
      }
    }
    return ret;
  }

  /**
   * nothing to read (only write) --> no AsyncTask
   */
  void setX310Laser( String address, int what, ILister lister )
  {
    if ( connectSocket( address ) ) {
      switch ( what ) {
        case 0: // LASER OFF
          sendCommand( 0x37 );
          break;
        case 1: // LASER ON
          sendCommand( 0x36 );
          break;
        case 2: // MEASURE
        case 3: // MEASURE and DOWNLAOD
          sendCommand( 0x38 );
          if ( what == 3 ) {
            if ( mRfcommThread == null ) {
              // Log.v("DistoX", "RF comm thread start ... ");
              startRfcommThread( -1, lister );
              while ( mRfcommThread != null ) {
                try { Thread.sleep( 100 ); } catch ( InterruptedException e ) { }
              }
            } else {
              // Log.v("DistoX", "RF comm thread not null ");
            }
          }
          break;
      }
    }
    destroySocket( );
  }

  /** send the set/unset calib-mode command
   * @note called within connectSocket()
   * nothing to read (only write) --> no AsyncTask
   */
  private boolean setX310CalibMode( boolean turn_on )
  {
    boolean ret = false;
    if ( turn_on ) {
      ret = sendCommand( 0x31 ); // TOGGLE CALIB ON 
    } else { 
      ret = sendCommand( 0x30 ); // TOGGLE CALIB OFF
    }
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "set X310 CalibMode ret " + ret );
    return ret;
  }

  /** Toggle device calibration mode
   * @param address    device address
   * @param type       device type
   * @return 
   */
  public boolean toggleCalibMode( String address, int type )
  {
    if ( ! checkRfcommThreadNull( "toggle CalibMode: address " + address + " type " + type ) ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "toggle CalibMode address " + address + " not null RFcomm thread" );
      return false;
    }
    boolean ret = false;
    if ( connectSocket( address ) ) {
      switch ( type ) {
        case Device.DISTO_A3:
          byte[] result = new byte[4];
          if ( ! mProtocol.read8000( result ) ) { // FIXME ASYNC
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "toggleCalibMode A3 failed read8000" );
            destroySocket( );
            return false;
          }
          TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "toggleCalibMode A3 result " + result[0] );
          if ( (result[0] & CALIB_BIT) == 0 ) {
            ret = mProtocol.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
          } else {
            ret = mProtocol.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
          }
          break;
        case Device.DISTO_X310:
          mCalibMode = ! mCalibMode;
          TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "toggleCalibMode X310 setX310CalibMode " + mCalibMode );
          ret = setX310CalibMode( mCalibMode );
          break;
      }
    }
    destroySocket();
    return ret;
  }

  public boolean writeCoeff( String address, byte[] coeff )
  {
    if ( ! checkRfcommThreadNull( "write coeff: address " + address ) ) return false;
    boolean ret = false;
    if ( coeff != null ) {
      mCoeff = coeff;
      if ( connectSocket( address ) ) {
        ret = mProtocol.writeCalibration( mCoeff );
        // FIXME ASYNC new CommandThread( mProtocol, WRITE_CALIBRATION, mCoeff );
      }
      destroySocket( );
    }
    return ret;
  }

  // called only by CalibReadTask
  public boolean readCoeff( String address, byte[] coeff )
  {
    if ( ! checkRfcommThreadNull( "read coeff: address " + address ) ) return false;
    boolean ret = false;
    if ( coeff != null ) {
      if ( connectSocket( address ) ) {
        ret = mProtocol.readCalibration( coeff );
        // FIXME ASYNC new CommandThread( mProtocol, READ_CALIBRATION, coeff );

        // int k;
        // for ( k=0; k<48; k+=8 ) {
        //   StringWriter sw = new StringWriter();
        //   PrintWriter pw = new PrintWriter( sw );
        //   pw.format( "%02x %02x %02x %02x %02x %02x %02x %02x",
        //     coeff[k+0], coeff[k+1], coeff[k+2], coeff[k+3], coeff[k+4], coeff[k+5], coeff[k+6], coeff[k+7] );
        //   Log.v( TopoDroidApp.TAG, sw.getBuffer().toString() );
        // }
      }
      destroySocket( );
    }
    return ret;
  }

  public String readHeadTail( String address, int[] head_tail )
  {
    String res = null;
    if ( mApp.distoType() == Device.DISTO_A3 ) {
      if ( ! checkRfcommThreadNull( "read HeadTail: address " + address ) ) return null;
      if ( connectSocket( address ) ) {
        res = mProtocol.readHeadTailA3( head_tail );
        // FIXME ASYNC new CommandThread( mProtocol, READ_HEAD_TAIL, haed_tail ); NOTE int[] instead of byte[]
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "readHeadTail() result " + res );
      }
      destroySocket( );
    }
    return res;
  }
  
  // X310 data memory is read-only
  // public int resetX310Memory( String address, int from, int to )
  // {
  //   if ( ! checkRfcommThreadNull( "reset X310 memory: address " + address ) ) return -1;
  //   int n = 0;
  //   if ( connectSocket( address ) ) {
  //     n = mProtocol.resetX310Memory( from, to );
  //   }
  //   destroySocket( );
  //   return n;
  // }

  public int readX310Memory( String address, int from, int to, List< MemoryOctet > memory )
  {
    if ( ! checkRfcommThreadNull( "read X310 memory: address " + address ) ) return -1;
    int n = 0;
    if ( connectSocket( address ) ) {
      n = mProtocol.readX310Memory( from, to, memory );
      // FIXME ASYNC new CommandThread( mProtocol, READ_X310_MEMORY, memory ) Note...
    }
    destroySocket( );
    return n;
  }

  public int readA3Memory( String address, int from, int to, List< MemoryOctet > memory )
  {
    if ( ! checkRfcommThreadNull( "read A3 memory: address " + address ) ) return -1;
    from &= 0x7ff8;
    to   &= 0xfff8;
    if ( from >= 0x8000 ) from = 0;
    if ( to >= 0x8000 ) to &= 0x8000;
    int n = 0;
    if ( from < to ) {
      if ( connectSocket( address ) ) {
        n = mProtocol.readMemory( from, to, memory );
        // FIXME ASYNC new CommandThread( mProtocol, READ_MEMORY, memory ) Note...
      }
      destroySocket( );
    }
    return n;
  }

  // low-level memory read
  // called by TopoDroidApp.readMemory
  byte[] readMemory( String address, int addr )
  {
    byte[] ret = null;
    if ( connectSocket( address ) ) {
      ret = mProtocol.readMemory( addr );
      // FIXME ASYNC new CommandThread( mProtocol, READ_MEMORY_LOWLEVEL, addr ) Note...
    }
    destroySocket( );
    return ret;
  }

  /** swap hot bit in the range [from, to) [only A3]
   */
  public int swapHotBit( String address, int from, int to )
  {
    if ( ! checkRfcommThreadNull( "swap hot bit: address " + address ) ) return -1;

    from &= 0x7ff8;
    to   &= 0xfff8;
    if ( from >= 0x8000 ) from = 0;
    if ( to >= 0x8000 ) to &= 0x8000;

    int n = 0;
    if ( from != to ) {
      if ( connectSocket( address ) ) {
        do {
          if ( to == 0 ) {
            to = 0x8000 - 8;
          } else {
            to -= 8;
          }
          // Log.v( TopoDroidApp.TAG, "comm swap hot bit at addr " + to/8 );
          if ( ! mProtocol.swapHotBit( to ) ) break;
          ++ n;
        } while ( to != from );
        // FIXME ASYNC new CommandThread( mProtocol, SWAP_HOT_BITS, from, to ) Note...
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "swapHotBit swapped " + n + "data" );
      }
      destroySocket( );
    }
    return n;
  }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  public boolean connectDevice( String address, ILister lister ) 
  {
    if ( mRfcommThread != null ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "DistoXComm connect: already connected");
      return true;
    }
    if ( ! connectSocket( address ) ) {
      return false;
    }
    startRfcommThread( -2, lister );
    return true;
  }

  public void disconnect()
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "disconnect");
    cancelRfcommThread();
    destroySocket( );
  }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  public int downloadData( String address, ILister lister )
  {
    if ( ! checkRfcommThreadNull( "download data: address " + address ) ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: RFcomm thread not null");
      return DistoXProtocol.DISTOX_ERR_CONNECTED;
    }
    
    int ret = -1; // failure
    if ( connectSocket( address ) ) {
      // if ( mApp.distoType() == Device.DISTO_A3 ) {
      //   int prev_read = -1;
      //   int to_read = mProtocol.readToReadA3();
      //   TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "download data: A3 to-read " + to_read );
      //   if ( to_read <= 0 ) {
      //     ret = to_read;
      //   } else {
      //     // FIXME asyncTask ?
      //     // nReadPackets = 0; // done in RfcommThread cstr
      //     startRfcommThread( to_read, lister );
      //     while ( mRfcommThread != null && nReadPackets < to_read ) {
      //       if ( nReadPackets != prev_read ) {
      //         TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "download data: A3 read " + nReadPackets + " / " + to_read );
      //         prev_read = nReadPackets;
      //       }
      //       try { Thread.sleep( 100 ); } catch ( InterruptedException e ) { }
      //     }
      //     TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "download done: A3 read " + nReadPackets );
      //   }
      // } else if ( mApp.distoType() == Device.DISTO_X310 ) {
        startRfcommThread( -1, lister );
        while ( mRfcommThread != null ) {
          try { Thread.sleep( 100 ); } catch ( InterruptedException e ) { }
        }
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "download done: read " + nReadPackets );
      // } else {
      //   TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: unknown DistoType " + mApp.distoType() );
      // }
      // cancelRfcommThread(); // called by closeSocket() which is called by destroySocket()
      ret = nReadPackets;
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: fail to connect socket");
    }
    destroySocket( );
    
    return ret;
  }

  // ====================================================================================
  // FIRMWARE

  // int readFirmwareHardware( String address )
  // {
  //   int ret = 0;
  //   if ( connectSocket( address ) ) {
  //     ret = mProtocol.readFirmwareAddress( );
  //   }
  //   destroySocket( );
  //   return ret;
  // }
    
  int dumpFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocket( address ) ) {
      ret = mProtocol.dumpFirmware( filepath );
    }
    destroySocket( );
    return ret;
  }

  int uploadFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocket( address ) ) {
      TopoDroidLog.LogFile( "Firmware upload: socket is ready " );
      ret = mProtocol.uploadFirmware( filepath );
    }
    destroySocket( );
    return ret;
  }

};
