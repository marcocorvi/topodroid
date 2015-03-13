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

  private BroadcastReceiver mBTReceiver;

  private void resetBTReceiver()
  {
    // TopoDroidLog.Log(  TopoDroidLog.LOG_BT, "reset BTReceiver");
    if ( mBTReceiver != null ) {
      mApp.unregisterReceiver( mBTReceiver );
      mBTReceiver = null;
    }
  }

  private void setupBTReceiver()
  {
    resetBTReceiver();
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
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_BT, "ACL_DISCONNECT_REQUESTED");
          closeSocket( );
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECTED.equals( action ) ) {
          Bundle extra = data.getExtras();
          String device = extra.getString( BluetoothDevice.EXTRA_DEVICE ).toString();
          // Log.v("DistoX", " DistoXComm ACL_DISCONNECTED from " + device );
          TopoDroidLog.Log( TopoDroidLog.LOG_BT, "ACL_DISCONNECTED");
          closeSocket( );
        }
      }
    };

    // IntentFilter foundFilter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
    // IntentFilter startFilter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_STARTED );
    // IntentFilter finishFilter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
    IntentFilter connectedFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_CONNECTED );
    IntentFilter disconnectRequestFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED );
    IntentFilter disconnectedFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECTED );
    // IntentFilter uuidFilter  = new IntentFilter( myUUIDaction );
    // IntentFilter bondFilter  = new IntentFilter( BluetoothDevice.ACTION_BOND_STATE_CHANGED );

    // mApp.registerReceiver( mBTReceiver, foundFilter );
    // mApp.registerReceiver( mBTReceiver, startFilter );
    // mApp.registerReceiver( mBTReceiver, finishFilter );
    mApp.registerReceiver( mBTReceiver, connectedFilter );
    mApp.registerReceiver( mBTReceiver, disconnectRequestFilter );
    mApp.registerReceiver( mBTReceiver, disconnectedFilter );
    // mApp.registerReceiver( mBTReceiver, uuidFilter );
    // mApp.registerReceiver( mBTReceiver, bondFilter );
  }

// --------------------------------------------------
  // DataHelper getData() { return mApp.mData; }

  int nReadPackets;   // number of received data-packet
  long mLastShotId;   // last shot id
  boolean doWork = true;

  private class RfcommThread extends Thread
  {
    private DistoXProtocol mProto;
    private int toRead; // number of packet to read
    private ArrayList<ILister> mLister;

    /** 
     * @param protocol    communication protocol
     * @param to_read     number of data to read (use -1 to read forever until timeout or an exception)
     */
    public RfcommThread( DistoXProtocol protocol, int to_read, ArrayList<ILister> lister )
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

      // Log.v( TopoDroidApp.TAG, "RFcomm thread running ... to_read " + toRead );
      while ( doWork && nReadPackets != toRead ) {
        
        int res = mProto.readPacket( toRead >= 0 );
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "RFcomm readPacket returns " + res );
        if ( res == DistoXProtocol.DISTOX_PACKET_NONE ) {
          if ( toRead == -1 ) {
            doWork = false;
          } else {
            try {
              Thread.sleep( 500 );
            } catch (InterruptedException e) {
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
          TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "DATA PACKET " + d + " " + b + " " + c );
          // NOTE type=0 shot is DistoX-type
          mLastShotId = mApp.mData.insertShot( mApp.mSID, -1L, d, b, c, r, 0, true );
          if ( mLister != null && mLister.size() > 0 ) {
            DistoXDBlock blk = new DistoXDBlock( );
            blk.setId( mLastShotId, mApp.mSID );
            blk.mLength  = (float)d;
            blk.mBearing = (float)b;
            blk.mClino   = (float)c;
            blk.mRoll    = (float)r;
            for ( ILister lister : mLister ) {
              lister.updateBlockList( blk );
            }
          }
        } else if ( res == DistoXProtocol.DISTOX_PACKET_G ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "G PACKET" );
          ++nReadPackets;
          hasG = true;
        } else if ( res == DistoXProtocol.DISTOX_PACKET_M ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "M PACKET" );
          ++nReadPackets;
          // get G and M from mProto and save them to store
          mApp.mData.insertGM( mApp.mCID, mProto.mGX, mProto.mGY, mProto.mGZ, mProto.mMX, mProto.mMY, mProto.mMZ );
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

  // -------------------------------------------------------- 
  // SOCKET


  /** close the socket (and the RFcomm thread) but don't delete it
   * alwasy called with wait_thread
   */
  private void closeSocket( )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "close socket()" );
    if ( mBTSocket != null ) {
      try {
        mBTSocket.close();
        if ( mRfcommThread != null ) {
          mRfcommThread.join();
        }
      } catch ( InterruptedException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "close socket interrupt " + e.getMessage() );
      } catch ( IOException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "close socket IOexception " + e.getMessage() );
      } finally {
        mRfcommThread = null;
        mBTConnected = false;
      }
    }
    mBTConnected = false;
  }

  /** close the socket and delete it
   * the connection becomes unusable
   * As a matter of fact tghis is alwyas called with wait_thread = true
   */
  private void destroySocket( ) // boolean wait_thread )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "destroy socket()" );
    closeSocket( );
    mBTSocket = null;
    mProtocol = null;
    resetBTReceiver();
  }


  /** create a socket (not connected)
   *  and a connection protocol on it
   */
  private void createSocket( String address )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket() addr " + address + " mAddress " + mAddress);
    if ( mProtocol == null || ! address.equals( mAddress ) ) {
      if ( mProtocol != null && ! address.equals( mAddress ) ) {
        disconnectRemoteDevice();
      }
      mBTDevice     = mApp.mBTAdapter.getRemoteDevice( address );
      // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket() device " + mBTDevice.getName() );
      try {
        if ( mBTSocket != null ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket() BTSocket not null ... closing");
          mBTSocket.close();
          mBTSocket = null;
        }
        if ( mBTDevice.getBondState() == BluetoothDevice.BOND_NONE ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket() binding device ..." );
          // byte[] pin = mBTDevice.convertPinToBytes( "0000" );
          // FIXME tried to replace the two following lines with the next one
          // String spin = "0000";
          // byte[] pin = spin.getBytes( "UTF8" );
          byte[] pin = new byte[] { 0, 0, 0, 0 };

          // mBTDevice.setPin( pin );
          Class[] classes3 = new Class[ 1 ];
          classes3[0] = byte[].class;
          Method m = mBTDevice.getClass().getMethod( "setPin", classes3 );
          m.invoke( mBTDevice, pin );

          // mBTDevice.createBond();
          Class[] classes2 = new Class[ 0 ];
          m = mBTDevice.getClass().getMethod( "createBond", classes2 );
          m.invoke( mBTDevice );
        }

        if ( TopoDroidSetting.mSockType == TopoDroidSetting.TOPODROID_SOCK_INSEC ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket() invoke createInsecureRfcommSocket " );
          Class[] classes1 = new Class[ 1 ];
          classes1[0] = int.class;
          Method m = mBTDevice.getClass().getMethod( "createInsecureRfcommSocket", classes1 );
          mBTSocket = (BluetoothSocket) m.invoke( mBTDevice, 1 );
          // mBTSocket = (BluetoothSocket) m.invoke( mBTDevice, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        // } else if ( TopoDroidApp.mSockType == TopoDroidApp.TOPODROID_SOCK_INSEC_RECORD ) {
        //   // FIXME FIXME FIXME
        //   TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket() createInsecureRfcommSocketToServiceRecord " );
        //   mBTSocket = mBTDevice.createInsecureRfcommSocketToServiceRecord( UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        // } else if ( TopoDroidApp.mSockType == TopoDroidApp.TOPODROID_SOCK_INSEC ) {
        //   TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket() createInsecureRfcommSocket(0) " );
        //   mBTSocket = mBTDevice.createInsecureRfcommSocket( 0 );
        } else { // TOPODROID_SOCK_DEFAULT
          TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket() createRfcommSocketToServiceRecord " );
          mBTSocket = mBTDevice.createRfcommSocketToServiceRecord( UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
        }

      } catch ( InvocationTargetException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "createSocket invoke target " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( UnsupportedEncodingException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "createSocket encoding " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( NoSuchMethodException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "createSocket no method " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( IllegalAccessException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "createSocket access " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      } catch ( IOException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "createSocket IO " + e.getMessage() );
        if ( mBTSocket != null ) { mBTSocket = null; }
      }

      if ( mBTSocket != null ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket OK");
        mProtocol = new DistoXProtocol( mBTSocket, mApp.mDevice );
        mAddress = address;
      } else {
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "createSocket fail");
        mProtocol = null;
        mAddress = null;
      }
      mBTConnected = false;
    }
  }

  /** connect the socket to the device
   */
  private boolean connectSocket( String address )
  {
    if ( address == null ) return false;
    TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "connect socket(): " + address );
    createSocket( address );
    if ( mBTSocket != null ) {
      int k = 0;
      while ( ! mBTConnected && k<TopoDroidSetting.mCommRetry && mBTSocket != null ) {
        try {
          mBTSocket.connect();
          mBTConnected = true;
        } catch ( IOException e ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "connect socket() IO " + e.getMessage() );
          if ( ++k < TopoDroidSetting.mCommRetry ) {
            destroySocket( );
            createSocket( address );
          }
        }
      }
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "connect socket() null socket");
    }
    // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "connect socket() result " + mBTConnected );
    return mBTConnected;
  }

  private boolean startRfcommThread( int to_read, ArrayList<ILister> lister )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "startRFcommThread to_read " + to_read );
    if ( mBTSocket != null ) {
      if ( mRfcommThread == null ) {
        mRfcommThread = new RfcommThread( mProtocol, to_read, lister );
        mRfcommThread.start();
        // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "startRFcommThread started");
      // } else {
      //   TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "startRFcommThread already running");
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
  //     destroySocket( );
  //   } else {
  //     TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "connect RemoteDevice failed on connectSocket()" );
  //   }
  //   return false;
  // }

  public void disconnectRemoteDevice( )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "disconnect remote device ");
    if ( mBTSocket != null ) {
      destroySocket( );
      if ( mRfcommThread != null ) {
        try {
          mRfcommThread.join();
        } catch ( InterruptedException e ) {
          // TODO
        }
      }
      mRfcommThread = null;
    }
    mProtocol = null;
  }

  private boolean checkRfcommThreadNull( String msg )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_COMM, msg );
    return ( mRfcommThread == null );
  }

  private boolean sendCommand( int cmd )
  {
    boolean ret = false;
    for (int k=0; k<3 && ! ret; ++k ) { // try three times
      ret |= mProtocol.sendCommand( (byte)cmd ); 
      TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "sendCommand " + cmd + " " + k + "-ret " + ret );
      try {
        Thread.sleep( 100 );
      } catch ( InterruptedException e ) {
      }
    }
    return ret;
  }

  /**
   * nothing to read (only write) --> no AsyncTask
   */
  void setX310Laser( String address, int what, ArrayList<ILister> lister )
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
                try {
                  Thread.sleep( 100 );
                } catch ( InterruptedException e ) { }
              }
            } else {
              // Log.v("DistoX", "RF comm thread not null ");
            }
          }
          break;
      }
      destroySocket( );
    }
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
      setupBTReceiver();
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
      destroySocket( );
    }
    return ret;
  }

  public boolean writeCoeff( String address, byte[] coeff )
  {
    if ( ! checkRfcommThreadNull( "write coeff: address " + address ) ) return false;
    boolean ret = false;
    if ( coeff != null ) {
      mCoeff = coeff;
      // createSocket( address );
      if ( connectSocket( address ) ) {
        setupBTReceiver();
        ret = mProtocol.writeCalibration( mCoeff );
        // FIXME ASYNC new CommandThread( mProtocol, WRITE_CALIBRATION, mCoeff );
        destroySocket( );
      }
    }
    return ret;
  }

  // called only by CalibReadTask
  public boolean readCoeff( String address, byte[] coeff )
  {
    if ( ! checkRfcommThreadNull( "read coeff: address " + address ) ) return false;
    boolean ret = false;
    if ( coeff != null ) {
      // createSocket( address );
      if ( connectSocket( address ) ) {
        setupBTReceiver();
        ret = mProtocol.readCalibration( coeff );
        // FIXME ASYNC new CommandThread( mProtocol, READ_CALIBRATION, coeff );
        destroySocket( );

        // int k;
        // for ( k=0; k<48; k+=8 ) {
        //   StringWriter sw = new StringWriter();
        //   PrintWriter pw = new PrintWriter( sw );
        //   pw.format( "%02x %02x %02x %02x %02x %02x %02x %02x",
        //     coeff[k+0], coeff[k+1], coeff[k+2], coeff[k+3], coeff[k+4], coeff[k+5], coeff[k+6], coeff[k+7] );
        //   Log.v( TopoDroidApp.TAG, sw.getBuffer().toString() );
        // }
      }
    }
    return ret;
  }

  public String readHeadTail( String address, int[] head_tail )
  {
    if ( mApp.distoType() == Device.DISTO_A3 ) {
      if ( ! checkRfcommThreadNull( "read HeadTail: address " + address ) ) return null;
      // createSocket( address );
      if ( connectSocket( address ) ) {
        setupBTReceiver();
        String result = mProtocol.readHeadTailA3( head_tail );
        // FIXME ASYNC new CommandThread( mProtocol, READ_HEAD_TAIL, haed_tail ); NOTE int[] instead of byte[]
        destroySocket( );
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "readHeadTail() result " + result );
        return result; 
      }
    }
    // default
    return null;
  }
  
  // X310 data memory is read-only
  // public int resetX310Memory( String address, int from, int to )
  // {
  //   if ( ! checkRfcommThreadNull( "reset X310 memory: address " + address ) ) return -1;
  //   int n = 0;
  //   if ( connectSocket( address ) ) {
  //     setupBTReceiver();
  //     n = mProtocol.resetX310Memory( from, to );
  //     destroySocket( );
  //   }
  //   return n;
  // }

  public int readX310Memory( String address, int from, int to, List< MemoryOctet > memory )
  {
    if ( ! checkRfcommThreadNull( "read X310 memory: address " + address ) ) return -1;
    int n = 0;
    if ( connectSocket( address ) ) {
      setupBTReceiver();
      n = mProtocol.readX310Memory( from, to, memory );
      // FIXME ASYNC new CommandThread( mProtocol, READ_X310_MEMORY, memory ) Note...
      destroySocket( );
    }
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
        setupBTReceiver();
        n = mProtocol.readMemory( from, to, memory );
        // FIXME ASYNC new CommandThread( mProtocol, READ_MEMORY, memory ) Note...
        destroySocket( );
      }
    }
    return n;
  }

  // low-level readMemory
  byte[] readMemory( String address, int addr )
  {
    byte[] ret = null;
    if ( connectSocket( address ) ) {
      setupBTReceiver();
      ret = mProtocol.readMemory( addr );
      // FIXME ASYNC new CommandThread( mProtocol, READ_MEMORY_LOWLEVEL, addr ) Note...
      destroySocket( );
    }
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
        setupBTReceiver();
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
        
        destroySocket( );
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "swapHotBit swapped " + n + "data" );
      }
    }
    return n;
  }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  public boolean connect( String address, ArrayList<ILister> lister ) 
  {
    if ( mRfcommThread != null ) return true;
    if ( ! connectSocket( address ) ) return false;
    startRfcommThread( -2, lister );
    return true;
  }

  public void disconnect()
  {
    if ( mRfcommThread != null ) {
      try {
        doWork = false;
        mRfcommThread.join();
      } catch ( InterruptedException e ) {
        // TODO
      }
    }
    destroySocket( );
  }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  public int downloadData( String address, ArrayList<ILister> lister )
  {
    if ( ! checkRfcommThreadNull( "download data: address " + address ) ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: RFcomm thread not null");
      return DistoXProtocol.DISTOX_ERR_CONNECTED;
    }
    
    if ( connectSocket( address ) ) {
      if ( mApp.distoType() == Device.DISTO_A3 ) {
        int prev_read = -1;
        int to_read = mProtocol.readToReadA3();
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "download data: A3 to-read " + to_read );
        if ( to_read <= 0 ) {
          destroySocket( );
          return to_read;
        }

        // FIXME asyncTask ?
        // nReadPackets = 0; // done in RfcommThread cstr
        startRfcommThread( to_read, lister );
        while ( mRfcommThread != null && nReadPackets < to_read ) {
          if ( nReadPackets != prev_read ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "download data: A3 read " + nReadPackets + " / " + to_read );
            prev_read = nReadPackets;
            // try {
            //   Thread.sleep( 500 );
            // } catch ( InterruptedException e ) { }
          }
        }
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "download data: A3 read " + nReadPackets );
        destroySocket( );
      } else if ( mApp.distoType() == Device.DISTO_X310 ) {
        startRfcommThread( -1, lister );
        while ( mRfcommThread != null ) {
          try {
            Thread.sleep( 500 );
          } catch ( InterruptedException e ) { }
        }
        TopoDroidLog.Log( TopoDroidLog.LOG_COMM, "download data: X310 read " + nReadPackets );
        destroySocket( );
      } else {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: unknown DistoType " + mApp.distoType() );
      }
      if ( mRfcommThread != null ) {
        try {
          mRfcommThread.join();
        } catch ( InterruptedException e ) {
          // TODO
        }
        mRfcommThread = null;
      }
      return nReadPackets;
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "download data: fail to connect socket");
    }
    
    return -1; // failure
  }

  // ====================================================================================
  // FIRMWARE

  // int readFirmwareHardware( String address )
  // {
  //   int ret = 0;
  //   if ( connectSocket( address ) ) {
  //     ret = mProtocol.readFirmwareAddress( );
  //     destroySocket( );
  //   }
  //   return ret;
  // }
    
  int dumpFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocket( address ) ) {
      ret = mProtocol.dumpFirmware( filepath );
      destroySocket( );
    }
    return ret;
  }

  int uploadFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocket( address ) ) {
      TopoDroidLog.LogFile( "Firmware upload: socket is ready " );
      ret = mProtocol.uploadFirmware( filepath );
      destroySocket( );
    }
    return ret;
  }

};
