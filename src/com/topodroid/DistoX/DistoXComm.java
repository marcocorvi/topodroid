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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

// import android.os.Parcelable;
import android.os.ParcelUuid;
// import android.os.AsyncTask;

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

public class DistoXComm extends TopoDroidComm
{
  private BluetoothDevice mBTDevice;
  private BluetoothSocket mBTSocket;
  private String mAddress;

  // private static final byte CALIB_BIT = (byte)0x08;
  // public byte[] mCoeff;

// -----------------------------------------------------------
// Bluetooth receiver

  private BroadcastReceiver mBTReceiver = null;

  private void resetBTReceiver()
  {
    if ( mBTReceiver == null ) return;
    // TDLog.Log( TDLog.LOG_COMM, "reset BT receiver");
    try {
      mApp.unregisterReceiver( mBTReceiver );
    } catch ( IllegalArgumentException e ) {
      TDLog.Error( "unregister BT receiver error " + e.getMessage() );
    }
    mBTReceiver = null;
  }

  // called only by connectSocket
  private void setupBTReceiver()
  {
    resetBTReceiver();
    // TDLog.Log( TDLog.LOG_COMM, "setup BT receiver");
    mBTReceiver = new BroadcastReceiver() 
    {
      @Override
      public void onReceive( Context ctx, Intent data )
      {
        String action = data.getAction();
        BluetoothDevice bt_device = data.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
        String device = ( bt_device != null )? bt_device.getAddress() : "undefined";

        // if ( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals( action ) ) {
        // } else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
        // } else if ( BluetoothDevice.ACTION_FOUND.equals( action ) ) {
        if ( BluetoothDevice.ACTION_ACL_CONNECTED.equals( action ) ) {
          TDLog.Log( TDLog.LOG_BT, "[C] ACL_CONNECTED " + device + " addr " + mAddress );
          if ( device.equals(mAddress) ) // FIXME ACL_DISCONNECT
          {
            mApp.mDataDownloader.setConnected( true );
            mApp.notifyStatus();
          }
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
          TDLog.Log( TDLog.LOG_BT, "[C] ACL_DISCONNECT_REQUESTED " + device + " addr " + mAddress );
          if ( device.equals(mAddress) ) // FIXME ACL_DISCONNECT
          {
            mApp.mDataDownloader.setConnected( false );
            mApp.notifyStatus();
            closeSocket( );
          }
        } else if ( BluetoothDevice.ACTION_ACL_DISCONNECTED.equals( action ) ) {
          TDLog.Log( TDLog.LOG_BT, "[C] ACL_DISCONNECTED " + device + " addr " + mAddress );
          if ( device.equals(mAddress) ) // FIXME ACL_DISCONNECT
          {
            mApp.mDataDownloader.setConnected( false );
            mApp.notifyStatus();
            closeSocket( );
            mApp.notifyDisconnected();
          }
        } else if ( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals( action ) ) { // NOT USED
          final int state     = data.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
          final int prevState = data.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
          if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
            TDLog.Log( TDLog.LOG_BT, "BOND STATE CHANGED paired (BONDING --> BONDED) " + device );
          } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
            TDLog.Log( TDLog.LOG_BT, "BOND STATE CHANGED unpaired (BONDED --> NONE) " + device );
          } else if (state == BluetoothDevice.BOND_BONDING && prevState == BluetoothDevice.BOND_BONDED) {
            TDLog.Log( TDLog.LOG_BT, "BOND STATE CHANGED unpaired (BONDED --> BONDING) " + device );
            if ( mBTSocket != null ) {
              TDLog.Error( "[*] socket is not null: close and retry connect ");
              mApp.mDataDownloader.setConnected( false );
              mApp.notifyStatus();
              closeSocket( );
              mApp.notifyDisconnected();
              connectSocket( mAddress ); // returns immediately if mAddress == null
            }
          } else {
            TDLog.Log( TDLog.LOG_BT, "BOND STATE CHANGED " + prevState + " --> " + state + " " + device );
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

  DistoXComm( TopoDroidApp app )
  {
    super( app );
    mAddress   = null;
    mBTSocket     = null;
    // TDLog.Log( TDLog.LOG_COMM, "DistoX Comm cstr");
  }

  // public void resume()
  // {
  //   // if ( mRfcommThread != null ) { mRfcommThread.resume(); }
  // }

  // public void suspend()
  // {
  //   // if ( mRfcommThread != null ) { mRfcommThread.suspend(); }
  // }

  // -------------------------------------------------------- 
  // SOCKET

  /** close the socket (and the RFcomm thread) but don't delete it
   * alwasy called with wait_thread
   */
  private synchronized void closeSocket( )
  {
    if ( mBTSocket == null ) return;

    // TDLog.Log( TDLog.LOG_COMM, "close socket() address " + mAddress );
    for ( int k=0; k<1 && mBTSocket != null; ++k ) {
      // TDLog.Error( "try close socket nr " + k );
      cancelRfcommThread();
      closeProtocol();
      try {
        mBTSocket.close();
        mBTSocket = null;
      } catch ( IOException e ) {
        TDLog.Error( "close socket IOexception " + e.getMessage() );
        // TDLog.LogStackTrace( e );
      } finally {
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
    if ( mBTSocket == null ) return;
    // TDLog.Log( TDLog.LOG_COMM, "destroy socket() address " + mAddress );
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
    // TDLog.Log( TDLog.LOG_COMM, "create Socket() addr " + address + " mAddress " + mAddress);
    if ( mProtocol == null || ! address.equals( mAddress ) ) {
      if ( mProtocol != null && ! address.equals( mAddress ) ) {
        disconnectRemoteDevice();
      }

      if ( mBTSocket != null ) {
        // TDLog.Log( TDLog.LOG_COMM, "create Socket() BTSocket not null ... closing");
        try {
          mBTSocket.close();
        } catch ( IOException e ) { 
          TDLog.Error( "close Socket IO " + e.getMessage() );
        }
        mBTSocket = null;
      }

      mBTDevice = mApp.mBTAdapter.getRemoteDevice( address );
      // FIXME PAIRING
      // TDLog.Log( TDLog.LOG_BT, "[1] device state " + mBTDevice.getBondState() );
      if ( ! DeviceUtil.isPaired( mBTDevice ) ) {
        int ret = DeviceUtil.pairDevice( mBTDevice );
        // TDLog.Log( TDLog.LOG_BT, "[1] pairing device " + ret );
      // }

      // TDLog.Log( TDLog.LOG_BT, "[2] device state " + mBTDevice.getBondState() );
      // // if ( mBTDevice.getBondState() == BluetoothDevice.BOND_NONE ) 
      // if ( ! DeviceUtil.isPaired( mBTDevice ) ) 
      // {
      //   TDLog.Log( TDLog.LOG_BT, "bind device " );
        DeviceUtil.bindDevice( mBTDevice );
      }

      // wait "delay" seconds
      if ( ! DeviceUtil.isPaired( mBTDevice ) ) {
        for ( int n=0; n < TDSetting.mConnectSocketDelay; ++n ) {
          try {
            Thread.yield();
            Thread.sleep( 100 );
          } catch ( InterruptedException e ) { }
          if ( DeviceUtil.isPaired( mBTDevice ) ) {
            // TDLog.Log( TDLog.LOG_BT, "device paired at time " + n );
            break;
          }
        }
      }

      if ( DeviceUtil.isPaired( mBTDevice ) ) {
        try {
          Class[] classes1 = new Class[]{ int.class };
          Class[] classes2 = new Class[]{ UUID.class };
          if ( TDSetting.mSockType == TDSetting.TOPODROID_SOCK_DEFAULT ) {
            // TDLog.Log( TDLog.LOG_COMM, "create Socket() createRfcommSocketToServiceRecord " );
            mBTSocket = mBTDevice.createRfcommSocketToServiceRecord( UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
          } else if ( TDSetting.mSockType == TDSetting.TOPODROID_SOCK_INSEC ) {
            // TDLog.Log( TDLog.LOG_COMM, "create Socket() createInsecureRfcommSocketToServiceRecord " );
            Method m3 = mBTDevice.getClass().getMethod( "createInsecureRfcommSocketToServiceRecord", classes2 );
            mBTSocket = (BluetoothSocket) m3.invoke( mBTDevice, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
          } else if ( TDSetting.mSockType == TDSetting.TOPODROID_SOCK_INSEC_PORT ) {
            // TDLog.Log( TDLog.LOG_COMM, "create Socket() invoke createInsecureRfcommSocket " );
            Method m1 = mBTDevice.getClass().getMethod( "createInsecureRfcommSocket", classes1 );
            mBTSocket = (BluetoothSocket) m1.invoke( mBTDevice, port );
            // mBTSocket = mBTDevice.createInsecureRfcommSocket( port );
            // mBTSocket = (BluetoothSocket) m1.invoke( mBTDevice, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
          } else if ( TDSetting.mSockType == TDSetting.TOPODROID_SOCK_PORT ) {
            // TDLog.Log( TDLog.LOG_COMM, "create Socket() invoke createRfcommSocket " );
            Method m2 = mBTDevice.getClass().getMethod( "createRfcommSocket", classes1 );
            mBTSocket = (BluetoothSocket) m2.invoke( mBTDevice, port );
          }

        } catch ( InvocationTargetException e ) {
          TDLog.Error( "create Socket invoke target " + e.getMessage() );
          if ( mBTSocket != null ) { mBTSocket = null; }
        } catch ( UnsupportedEncodingException e ) {
          TDLog.Error( "create Socket encoding " + e.getMessage() );
          if ( mBTSocket != null ) { mBTSocket = null; }
        } catch ( NoSuchMethodException e ) {
          TDLog.Error( "create Socket no method " + e.getMessage() );
          if ( mBTSocket != null ) { mBTSocket = null; }
        } catch ( IllegalAccessException e ) {
          TDLog.Error( "create Socket access " + e.getMessage() );
          if ( mBTSocket != null ) { mBTSocket = null; }
        } catch ( IOException e ) {
          TDLog.Error( "create Socket IO " + e.getMessage() );
          if ( mBTSocket != null ) { mBTSocket = null; }
        }
      } else {
        // TDLog.Log( TDLog.LOG_BT, "device not paired. state " + mBTDevice.getBondState() );
      }

      mProtocol = null;
      if ( mBTSocket != null ) {
        // TDLog.Log( TDLog.LOG_COMM, "create Socket OK");
        // mBTSocket.setSoTimeout( 200 ); // BlueToothSocket does not have timeout 
        try {
          DataInputStream in   = new DataInputStream( mBTSocket.getInputStream() );
          DataOutputStream out = new DataOutputStream( mBTSocket.getOutputStream() );
          mProtocol = new DistoXProtocol( in, out, mApp.mDevice );
          mAddress = address;
        } catch ( IOException e ) {
          mAddress = null;
        }
      } else {
        TDLog.Error( "create Socket fail");
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
      //     TDLog.Log( TDLog.LOG_COMM, "uuid " + uid.toString() );
      //   }
      // }
    } catch ( Exception e ) {
      TDLog.Error( "get uuids error " + e.getMessage() );
    }
  }

  /** connect the socket to the device
   */
  private boolean connectSocket( String address )
  {
    if ( address == null ) return false;
    // TDLog.Log( TDLog.LOG_COMM, "connect socket(): " + address );
    createSocket( address, 1 ); // default port == 1

    // DEBUG
    getUuids();

    if ( mBTSocket != null ) {
      mApp.mBTAdapter.cancelDiscovery();
      setupBTReceiver();

      int port = 0;
      while ( ! mBTConnected && port < TDSetting.mCommRetry ) {
        ++ port;
        if ( mBTSocket != null ) {
          // TDLog.Log( TDLog.LOG_COMM, "connect socket() try port " + port );
          try {
            // TDLog.Log( TDLog.LOG_BT, "[3] device state " + mBTDevice.getBondState() );
            mBTSocket.connect();
            mBTConnected = true;
          } catch ( IOException e ) {
            TDLog.Error( "connect socket() (port " + port + ") IO error " + e.getMessage() );
            // TDLog.LogStackTrace( e );
            closeSocket();
            // mBTSocket = null;
          }
        }
        if ( mBTSocket == null && port < TDSetting.mCommRetry ) {
          createSocket( address, port );
        }
        // TDLog.Log( TDLog.LOG_COMM, "connect socket() port " + port + " connected " + mBTConnected );
      }
    } else {
      TDLog.Error( "connect socket() null socket");
    }
    // TDLog.Log( TDLog.LOG_COMM, "connect socket() result " + mBTConnected );
    return mBTConnected;
  }

  protected boolean startRfcommThread( int to_read, Handler /* ILister */ lister ) // FIXME LISTER
  {
    // TDLog.Log( TDLog.LOG_COMM, "start RFcomm thread: to_read " + to_read );
    if ( mBTSocket != null ) {
      if ( mRfcommThread == null ) {
        mRfcommThread = new RfcommThread( mProtocol, to_read, lister );
        mRfcommThread.start();
        // TDLog.Log( TDLog.LOG_COMM, "startRFcommThread started");
      } else {
        TDLog.Error( "startRFcommThread already running");
      }
      return true;
    } else {
      mRfcommThread = null;
      TDLog.Error( "startRFcommThread: null socket");
      return false;
    }
  }

  // -------------------------------------------------------- 
  
  // public boolean connectRemoteDevice( String address, Handler /* ArrayList<ILister> */ lister ) // FIXME LISTER
  // {
  //   // TDLog.Log( TDLog.LOG_COMM, "connect remote device: address " + address );
  //   if ( connectSocket( address ) ) {
  //     if ( mProtocol != null ) {
  //       return startRfcommThread( -1, lister );
  //     }
  //     TDLog.Error( "connect RemoteDevice null protocol");
  //   } else {
  //     TDLog.Error( "connect RemoteDevice failed on connectSocket()" );
  //   }
  //   destroySocket( );
  //   return false;
  // }

  public void disconnectRemoteDevice( )
  {
    // TDLog.Log( TDLog.LOG_COMM, "disconnect remote device ");
    super.disconnectRemoteDevice();
    // cancelRfcommThread();
    // closeProtocol();

    destroySocket( );
  }

  /**
   * nothing to read (only write) --> no AsyncTask
   */
  public void setX310Laser( String address, int what, Handler /* ILister */ lister ) // FIXME LISTER
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
    // TDLog.Log( TDLog.LOG_COMM, "set X310 CalibMode ret " + ret );
    return ret;
  }

  /** Toggle device calibration mode
   * @param address    device address
   * @param type       device type
   * @return 
   */
  public boolean toggleCalibMode( String address, int type )
  {
    if ( ! checkRfcommThreadNull( "toggle Calib Mode: address " + address + " type " + type ) ) {
      TDLog.Error( "toggle Calib Mode address " + address + " not null RFcomm thread" );
      return false;
    }
    boolean ret = false;
    if ( connectSocket( address ) ) {
      switch ( type ) {
        case Device.DISTO_A3:
          byte[] result = new byte[4];
          if ( ! mProtocol.read8000( result ) ) { // FIXME ASYNC
            TDLog.Error( "toggle Calib Mode A3 failed read8000" );
            destroySocket( );
            return false;
          }
          // TDLog.Log( TDLog.LOG_COMM, "toggle Calib Mode A3 result " + result[0] );
          if ( (result[0] & CALIB_BIT) == 0 ) {
            ret = mProtocol.sendCommand( (byte)0x31 );  // TOGGLE CALIB ON
          } else {
            ret = mProtocol.sendCommand( (byte)0x30 );  // TOGGLE CALIB OFF
          }
          break;
        case Device.DISTO_X310:
          mCalibMode = ! mCalibMode;
          // TDLog.Log( TDLog.LOG_COMM, "toggle Calib Mode X310 setX310CalibMode " + mCalibMode );
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
        //   Log.v( TopoDroidApp.TAG, 
        //   String.format( "%02x %02x %02x %02x %02x %02x %02x %02x",
        //     coeff[k+0], coeff[k+1], coeff[k+2], coeff[k+3], coeff[k+4], coeff[k+5], coeff[k+6], coeff[k+7] ) );
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
        // TDLog.Log( TDLog.LOG_COMM, "readHeadTail() result " + res );
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
    if ( mApp.distoType() != Device.DISTO_A3 ) return -2;

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
        // TDLog.Log( TDLog.LOG_COMM, "swap Hot Bit swapped " + n + "data" );
      }
      destroySocket( );
    }
    return n;
  }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  public boolean connectDevice( String address, Handler /* ILister */ lister ) // FIXME LISTER
  {
    if ( mRfcommThread != null ) {
      TDLog.Log( TDLog.LOG_COMM, "DistoX Comm connect: already connected");
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
    // TDLog.Log( TDLog.LOG_COMM, "disconnect");
    cancelRfcommThread();
    destroySocket( );
  }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  public int downloadData( String address, Handler /* ILister */ lister ) // FIXME LISTER
  {
    if ( ! checkRfcommThreadNull( "download data: address " + address ) ) {
      TDLog.Error( "download data: RFcomm thread not null");
      return DistoXProtocol.DISTOX_ERR_CONNECTED;
    }
    
    int ret = -1; // failure
    if ( connectSocket( address ) ) {
      // if ( mApp.distoType() == Device.DISTO_A3 ) {
      //   int prev_read = -1;
      //   int to_read = mProtocol.readToReadA3();
      //   TDLog.Log( TDLog.LOG_COMM, "download data: A3 to-read " + to_read );
      //   if ( to_read <= 0 ) {
      //     ret = to_read;
      //   } else {
      //     // FIXME asyncTask ?
      //     // nReadPackets = 0; // done in RfcommThread cstr
      //     startRfcommThread( to_read, lister );
      //     while ( mRfcommThread != null && nReadPackets < to_read ) {
      //       if ( nReadPackets != prev_read ) {
      //         TDLog.Log( TDLog.LOG_COMM, "download data: A3 read " + nReadPackets + " / " + to_read );
      //         prev_read = nReadPackets;
      //       }
      //       try { Thread.sleep( 100 ); } catch ( InterruptedException e ) { }
      //     }
      //     TDLog.Log( TDLog.LOG_COMM, "download done: A3 read " + nReadPackets );
      //   }
      // } else if ( mApp.distoType() == Device.DISTO_X310 ) {
        startRfcommThread( -1, lister );
        while ( mRfcommThread != null ) {
          try { Thread.sleep( 100 ); } catch ( InterruptedException e ) { }
        }
        // TDLog.Log( TDLog.LOG_COMM, "download done: read " + nReadPackets );
      // } else {
      //   TDLog.Error( "download data: unknown DistoType " + mApp.distoType() );
      // }
      // cancelRfcommThread(); // called by closeSocket() which is called by destroySocket()
      ret = nReadPackets;
    } else {
      TDLog.Error( "download data: fail to connect socket");
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
    
  public int dumpFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocket( address ) ) {
      ret = mProtocol.dumpFirmware( filepath );
    }
    destroySocket( );
    return ret;
  }

  public int uploadFirmware( String address, String filepath )
  {
    int ret = 0;
    if ( connectSocket( address ) ) {
      TDLog.LogFile( "Firmware upload: socket is ready " );
      ret = mProtocol.uploadFirmware( filepath );
    }
    destroySocket( );
    return ret;
  }

};
