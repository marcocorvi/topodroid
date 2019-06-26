/* @file DistoXComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid-DistoX BlueTooth communication 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

// import java.nio.ByteBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
// import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.List;
// import java.util.ArrayList;
// import java.util.concurrent.atomic.AtomicInteger;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

// import android.app.Activity;

// import android.os.Bundle;
import android.os.Handler;
// import android.os.Message;

// import android.os.Parcelable;
import android.os.ParcelUuid;
// import android.os.AsyncTask;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

// import android.database.DataSetObserver;

class DistoXComm extends TopoDroidComm
{
  protected BluetoothDevice mBTDevice;
  protected BluetoothSocket mBTSocket;
  // protected String mAddress; // FIXME_ADDRESS use TopoDroidComm.mAddress

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
        BluetoothDevice bt_device = data.getParcelableExtra( DeviceUtil.EXTRA_DEVICE );
        String device = ( bt_device != null )? bt_device.getAddress() : "undefined";
 
        // if ( DeviceUtil.ACTION_DISCOVERY_STARTED.equals( action ) ) {
        // } else if ( DeviceUtil.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
        // } else if ( DeviceUtil.ACTION_FOUND.equals( action ) ) {

       
        if ( device.equals(mAddress) ) {
          // Log.v("DistoXDOWN", "on receive");
          if ( DeviceUtil.ACTION_ACL_CONNECTED.equals( action ) ) {
            // TDLog.Log( TDLog.LOG_BT, "[C] ACL_CONNECTED " + device + " addr " + mAddress );
            mApp.mDataDownloader.updateConnected( true );
            mApp.notifyStatus();
          } else if ( DeviceUtil.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
            // TDLog.Log( TDLog.LOG_BT, "[C] ACL_DISCONNECT_REQUESTED " + device + " addr " + mAddress );
            mApp.mDataDownloader.updateConnected( false );
            mApp.notifyStatus();
            closeSocket( );
          } else if ( DeviceUtil.ACTION_ACL_DISCONNECTED.equals( action ) ) {
            // TDLog.Log( TDLog.LOG_BT, "[C] ACL_DISCONNECTED " + device + " addr " + mAddress );
            mApp.mDataDownloader.updateConnected( false );
            mApp.notifyStatus();
            closeSocket( );
            mApp.notifyDisconnected();
          }
        } else if ( DeviceUtil.ACTION_BOND_STATE_CHANGED.equals( action ) ) { // NOT USED
          final int state     = data.getIntExtra(DeviceUtil.EXTRA_BOND_STATE, DeviceUtil.ERROR);
          final int prevState = data.getIntExtra(DeviceUtil.EXTRA_PREVIOUS_BOND_STATE, DeviceUtil.ERROR);
          if (state == DeviceUtil.BOND_BONDED && prevState == DeviceUtil.BOND_BONDING) {
            TDLog.Log( TDLog.LOG_BT, "BOND STATE CHANGED paired (BONDING --> BONDED) " + device );
          } else if (state == DeviceUtil.BOND_NONE && prevState == DeviceUtil.BOND_BONDED){
            TDLog.Log( TDLog.LOG_BT, "BOND STATE CHANGED unpaired (BONDED --> NONE) " + device );
          } else if (state == DeviceUtil.BOND_BONDING && prevState == DeviceUtil.BOND_BONDED) {
            TDLog.Log( TDLog.LOG_BT, "BOND STATE CHANGED unpaired (BONDED --> BONDING) " + device );
            if ( mBTSocket != null ) {
              // TDLog.Error( "[*] socket is not null: close and retry connect ");
              mApp.mDataDownloader.setConnected( DataDownloader.STATUS_OFF );
              mApp.notifyStatus();
              closeSocket( );
              mApp.notifyDisconnected();
              connectSocket( mAddress ); // returns immediately if mAddress == null
            }
          } else {
            TDLog.Log( TDLog.LOG_BT, "BOND STATE CHANGED " + prevState + " --> " + state + " " + device );
          }

          // DeviceUtil.bind2Device( data );
        // } else if ( DeviceUtil.ACTION_PAIRING_REQUEST.equals(action) ) {
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


    // mApp.registerReceiver( mBTReceiver, new IntentFilter( DeviceUtil.ACTION_FOUND ) );
    // mApp.registerReceiver( mBTReceiver, new IntentFilter( DeviceUtil.ACTION_DISCOVERY_STARTED ) );
    // mApp.registerReceiver( mBTReceiver, new IntentFilter( DeviceUtil.ACTION_DISCOVERY_FINISHED ) );
    mApp.registerReceiver( mBTReceiver, new IntentFilter( DeviceUtil.ACTION_ACL_CONNECTED ) );
    mApp.registerReceiver( mBTReceiver, new IntentFilter( DeviceUtil.ACTION_ACL_DISCONNECT_REQUESTED ) );
    mApp.registerReceiver( mBTReceiver, new IntentFilter( DeviceUtil.ACTION_ACL_DISCONNECTED ) );
    // mApp.registerReceiver( mBTReceiver, uuidFilter  = new IntentFilter( myUUIDaction ) );
    mApp.registerReceiver( mBTReceiver, new IntentFilter( DeviceUtil.ACTION_BOND_STATE_CHANGED ) );
    // mApp.registerReceiver( mBTReceiver, new IntentFilter( DeviceUtil.ACTION_PAIRING_REQUEST ) );
  }

// --------------------------------------------------

  protected DistoXComm( TopoDroidApp app )
  {
    super( app );
    // mAddress   = null; done by TopoDroidComm
    mBTDevice  = null;
    mBTSocket  = null;
    // TDLog.Log( TDLog.LOG_COMM, "DistoX Comm cstr");
  }

  // public void resume()
  // {
  //   // if ( mCommThread != null ) { mCommThread.resume(); }
  // }

  // public void suspend()
  // {
  //   // if ( mCommThread != null ) { mCommThread.suspend(); }
  // }

  // -------------------------------------------------------- 
  // SOCKET

  /** close the socket (and the RFcomm thread) but don't delete it
   * alwasy called with wait_thread
   */
  protected synchronized void closeSocket( )
  {
    if ( mBTSocket == null ) {
      // TDLog.Log( TDLog.LOG_COMM, "close socket() already null" );
      return;
    }

    // TDLog.Log( TDLog.LOG_COMM, "close socket() address " + mAddress );
    for ( int k=0; k<1 && mBTSocket != null; ++k ) { 
      // TDLog.Error( "try close socket nr " + k );
      cancelCommThread();
      closeProtocol();
      try {
        mBTSocket.close();
        mBTSocket = null;
      } catch ( IOException e ) {
        TDLog.Error( "close socket IOexception " + e.getMessage() );
        // TDLog.LogStackTrace( e );
      // } finally {
      //   mBTConnected = false;
      }
    }
    mBTConnected = false;
  }

  /** close the socket and delete it
   * the connection becomes unusable
   * As a matter of fact this is alwyas called with wait_thread = true
   */
  protected void destroySocket( ) // boolean wait_thread )
  {
    if ( mBTSocket == null ) return;
    // TDLog.Log( TDLog.LOG_COMM, "destroy socket() address " + mAddress );
    // closeProtocol(); // already in closeSocket()
    closeSocket();
    // mBTSocket = null;
    resetBTReceiver();
  }

  /** must be overridden to call create proper protocol
   * new DistoX310Protocol( in, out, TDInstance.device, mApp ); // mApp = context
   * new DistoXA3Protocol( in, out, TDInstance.device, mApp ); // mApp = context
   */
  protected DistoXProtocol createProtocol( DataInputStream in, DataOutputStream out ) { return null; }

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

      mBTDevice = DeviceUtil.getRemoteDevice( address );
      // FIXME PAIRING
      // TDLog.Log( TDLog.LOG_BT, "[1] device state " + mBTDevice.getBondState() );
      if ( ! DeviceUtil.isPaired( mBTDevice ) ) {
        int ret = DeviceUtil.pairDevice( mBTDevice );
        // TDLog.Log( TDLog.LOG_BT, "[1b] pairing device " + ret );
      // }

        // TDLog.Log( TDLog.LOG_BT, "[2] device state " + mBTDevice.getBondState() );
      // // if ( mBTDevice.getBondState() == DeviceUtil.BOND_NONE ) 
      // if ( ! DeviceUtil.isPaired( mBTDevice ) ) 
      // {
      //   TDLog.Log( TDLog.LOG_BT, "bind device " );
        DeviceUtil.bindDevice( mBTDevice );
      }

      // wait "delay" seconds
      if ( ! DeviceUtil.isPaired( mBTDevice ) ) {
        for ( int n=0; n < TDSetting.mConnectSocketDelay; ++n ) {
          // TDLog.Log( TDLog.LOG_BT, "[4] pairing device: trial " + n );
	  TDUtil.yieldDown( 100 );
          if ( DeviceUtil.isPaired( mBTDevice ) ) {
            // TDLog.Log( TDLog.LOG_BT, "[4a] device paired at time " + n );
            break;
          }
        }
      }

      if ( DeviceUtil.isPaired( mBTDevice ) ) {
        try {
          Log.v("DistoXSOCKET", "create socket");
          Class[] classes1 = new Class[]{ int.class };
          Class[] classes2 = new Class[]{ UUID.class };
          if ( TDSetting.mSockType == TDSetting.TD_SOCK_DEFAULT ) {
            // TDLog.Log( TDLog.LOG_COMM, "[5a] createRfcommSocketToServiceRecord " );
            mBTSocket = mBTDevice.createRfcommSocketToServiceRecord( SERVICE_UUID );
          } else if ( TDSetting.mSockType == TDSetting.TD_SOCK_INSEC ) {
            // TDLog.Log( TDLog.LOG_COMM, "[5b] createInsecureRfcommSocketToServiceRecord " );
            Method m3 = mBTDevice.getClass().getMethod( "createInsecureRfcommSocketToServiceRecord", classes2 );
            mBTSocket = (BluetoothSocket) m3.invoke( mBTDevice, SERVICE_UUID );
          } else if ( TDSetting.mSockType == TDSetting.TD_SOCK_INSEC_PORT ) {
            // TDLog.Log( TDLog.LOG_COMM, "[5c] invoke createInsecureRfcommSocket " );
            Method m1 = mBTDevice.getClass().getMethod( "createInsecureRfcommSocket", classes1 );
            mBTSocket = (BluetoothSocket) m1.invoke( mBTDevice, port );
            // mBTSocket = mBTDevice.createInsecureRfcommSocket( port );
            // mBTSocket = (BluetoothSocket) m1.invoke( mBTDevice, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") );
          } else if ( TDSetting.mSockType == TDSetting.TD_SOCK_PORT ) {
            // TDLog.Log( TDLog.LOG_COMM, "[5d] invoke createRfcommSocket " );
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
      // } else {
      //   TDLog.Log( TDLog.LOG_BT, "device not paired. state " + mBTDevice.getBondState() );
      }

      mProtocol = null;
      if ( mBTSocket != null ) {
        // TDLog.Log( TDLog.LOG_COMM, "[6a] create Socket OK: create I/O streams");
        // mBTSocket.setSoTimeout( 200 ); // BlueToothSocket does not have timeout 
        if ( TDInstance.device == null ) {
          TDLog.Error( "[6b] create Socket on null device ");
          mAddress = null;
          try {
            mBTSocket.close();
          } catch ( IOException ee ) { 
            TDLog.Error( "[6c] close Socket IO " + ee.getMessage() );
          }
          mBTSocket = null;
        } else {
          try {
            DataInputStream in   = new DataInputStream( mBTSocket.getInputStream() );
            DataOutputStream out = new DataOutputStream( mBTSocket.getOutputStream() );
            mProtocol = createProtocol( in, out );
            mAddress = address;
          } catch ( IOException e ) {
            TDLog.Error( "[6d] create Socket stream error " + e.getMessage() );
            mAddress = null;
            try {
              mBTSocket.close();
            } catch ( IOException ee ) { 
              TDLog.Error( "[6e] close Socket IO " + ee.getMessage() );
            }
            mBTSocket = null;
          }
        }
      }
      if ( mBTSocket == null ) {
        TDLog.Error( "[7] create Socket failure");
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
   * @param address   remote devioce address
   */
  protected boolean connectSocket( String address )
  {
    if ( address == null ) return false;
    // TDLog.Log( TDLog.LOG_COMM, "connect socket(): " + address );
    createSocket( address, 1 ); // default port == 1

    // DEBUG
    getUuids();

    if ( mBTSocket != null ) {
      DeviceUtil.cancelDiscovery();
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
            // Toast must run on UI Thread
            // not sure this is good because it shows also when reconnection is interrupted
            // TopoDroidApp.mActivity.runOnUiThread( new Runnable() {
            //   public void run() {
            //     TDToast.makeBad( R.string.connection_error  );
            //   }
            // } );
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

  /** start the communication thread
   * @param to_read   number of packets to read
   * @param lister    packet calback handler
   * @return true on success
   */
  protected boolean startCommThread( int to_read, Handler /* ILister */ lister ) // FIXME_LISTER
  {
    // TDLog.Log( TDLog.LOG_COMM, "start RFcomm thread: to_read " + to_read );
    if ( mBTSocket != null ) {
      if ( mCommThread == null ) {
        mCommThread = new CommThread( TopoDroidComm.COMM_RFCOMM, mProtocol, to_read, lister );
        mCommThread.start();
        // TDLog.Log( TDLog.LOG_COMM, "startRFcommThread started");
      } else {
        TDLog.Error( "start Comm Thread already running");
      }
      return true;
    } else {
      mCommThread = null;
      TDLog.Error( "startRFcommThread: null socket");
      return false;
    }
  }

  // -------------------------------------------------------- 
  
  // public boolean connectRemoteDevice( String address, Handler /* ArrayList<ILister> */ lister ) // FIXME_LISTER
  // {
  //   // TDLog.Log( TDLog.LOG_COMM, "connect remote device: address " + address );
  //   if ( connectSocket( address ) ) {
  //     if ( mProtocol != null ) {
  //       return startCommThread( -1, lister );
  //     }
  //     TDLog.Error( "connect RemoteDevice null protocol");
  //   } else {
  //     TDLog.Error( "connect RemoteDevice failed on connectSocket()" );
  //   }
  //   destroySocket( );
  //   return false;
  // }

  void disconnectRemoteDevice( )
  {
    // TDLog.Log( TDLog.LOG_COMM, "disconnect remote device ");
    super.disconnectRemoteDevice();
    // cancelCommThread();
    // closeProtocol();

    destroySocket( );
  }

  /** send the set/unset calib-mode command
   * note called within connectSocket()
   * nothing to read (only write) --> no AsyncTask
   *
   * @param turn_on   whether to turn on or off the DistoX calibration mode
   * @return true if success
   */
  protected boolean setCalibMode( boolean turn_on )
  {
    return sendCommand( turn_on? 0x31 : 0x30 ); 
  }

  /** Toggle device calibration mode - must be overridden
   * @param address    device address
   * @param type       device type
   * @return true if success
   */
  boolean toggleCalibMode( String address, int type ) { return false; }

  /** write the calibration coeff to the remote DistoX device
   * @param address   remote device address
   * @param coeff     coeffs byte array 
   * @return true on success
   */
  boolean writeCoeff( String address, byte[] coeff )
  {
    if ( ! checkCommThreadNull() ) return false;
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

  /** read the calibration coeff from the remote DistoX device
   * @param address   remote device address
   * @param coeff     coeffs byte array for the result
   * @return true on success
   * 
   * called only by CalibReadTask
   */
  boolean readCoeff( String address, byte[] coeff )
  {
    if ( ! checkCommThreadNull() ) return false;
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

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  boolean connectDevice( String address, Handler /* ILister */ lister ) // FIXME_LISTER
  {
    if ( mCommThread != null ) {
      // TDLog.Log( TDLog.LOG_COMM, "DistoX Comm connect: already connected");
      return true;
    }
    if ( ! connectSocket( address ) ) {
      // TDLog.Log( TDLog.LOG_COMM, "DistoX Comm connect: failed");
      return false;
    }
    startCommThread( -2, lister );
    return true;
  }

  void disconnectDevice()
  {
    // TDLog.Log( TDLog.LOG_COMM, "disconnect device");
    cancelCommThread();
    destroySocket( );
  }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  int downloadData( String address, Handler /* ILister */ lister ) // FIXME_LISTER
  {
    if ( ! checkCommThreadNull() ) {
      TDLog.Error( "download data: RFcomm thread not null");
      return TopoDroidProtocol.DISTOX_ERR_CONNECTED;
    }
    
    int ret = -1; // failure
    if ( connectSocket( address ) ) {
      DistoXProtocol protocol = (DistoXProtocol)mProtocol;
      if ( TDSetting.mHeadTail ) {
        boolean a3 = ( TDInstance.deviceType() == Device.DISTO_A3 );
        byte[] command = ( a3 ? DeviceA3Details.HeadTail : DeviceX310Details.HeadTail );
        int prev_read = -1;
        int to_read = protocol.readToRead( command );
        // TDLog.Log( TDLog.LOG_COMM, "download data HT: A3 " + a3 + " to-read " + to_read );
        if ( to_read == 0 ) {
          ret = to_read;
	} else if ( to_read < 0 ) {
	  int error_code = (protocol == null)? TopoDroidProtocol.DISTOX_ERR_PROTOCOL
                         : protocol.getErrorCode();
	  if ( error_code < 0 ) {
            ret = error_code;
	  } else { // read with timeout
            startCommThread( -1, lister );
            while ( mCommThread != null ) {
              TDUtil.slowDown( 100 );
            }
            ret = getNrReadPackets();
	  }
        } else {
          // FIXME asyncTask ?
          // resetNtReadPackets(); // done in CommThread cstr 
	  int packets = getNrReadPackets();
          startCommThread( to_read, lister );
          while ( mCommThread != null ) {
	    packets = getNrReadPackets();
	    if ( packets >= to_read ) break;
            // if ( packets != prev_read ) {
            //   TDLog.Log( TDLog.LOG_COMM, "download data: read " + packets + " / " + to_read );
            // }
            prev_read = packets;
            TDUtil.slowDown( 100 );
          }
	  ret = getNrReadPackets();
	  // if ( ret > to_read ) {
          //   TDLog.Log( TDLog.LOG_COMM, "download done: read " + ret + " expected " + to_read );
	  // }
        }
      } else {
        startCommThread( -1, lister );
        while ( mCommThread != null ) {
          TDUtil.slowDown( 100 );
        }
        // TDLog.Log( TDLog.LOG_COMM, "download done: read " + getNrReadPacket() );
        // cancelCommThread(); // called by closeSocket() which is called by destroySocket()
        ret = getNrReadPackets();
      }
    } else {
      TDLog.Error( "download data: fail to connect socket");
    }
    destroySocket( );
    
    return ret;
  }

}
