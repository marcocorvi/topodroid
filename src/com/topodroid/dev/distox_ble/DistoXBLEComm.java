/* @file DistoXBLEComm.java
 *
 * @author siwei tian
 * @date aug 2022
 *
 * @brief TopoDroid DistoX BLE comm class
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox_ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
// import android.os.Handler;

import com.topodroid.TDX.R;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.ListerHandler;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TopoDroidAlertDialog;
import com.topodroid.dev.ConnectionState;
// import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.dev.ble.BleCallback;
import com.topodroid.dev.ble.BleComm;
import com.topodroid.dev.ble.BleOpChrtWrite;
import com.topodroid.dev.ble.BleOpConnect;
import com.topodroid.dev.ble.BleOpDisconnect;
import com.topodroid.dev.ble.BleOpNotify;
import com.topodroid.dev.ble.BleOperation;
import com.topodroid.dev.ble.BleBuffer;
import com.topodroid.dev.ble.BleQueue;
import com.topodroid.dev.distox.DistoX;
import com.topodroid.packetX.MemoryOctet;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.TDProgress;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.content.res.Resources;

public class DistoXBLEComm extends TopoDroidComm
        implements BleComm
{
  final static int DATA_PRIM = 1;   // same as Bric DATA_PRIM
  final static int DATA_QUIT = -1;  // same as Bric 

  private ConcurrentLinkedQueue< BleOperation > mOps;
  private Context mContext;
  BleCallback mCallback;
  // private String          mRemoteAddress;
  private BluetoothDevice mRemoteBtDevice;
  private ListerHandler mLister = null;

  BluetoothGattCharacteristic mReadChrt  = null;
  BluetoothGattCharacteristic mWriteChrt = null;
  //private boolean mReadInitialized  = false;
  //private boolean mWriteInitialized = false;
  private boolean mReconnect = false;
  private boolean mSkipNotify = false;

  private int mDataType;
  private int mPacketType;  // type of the last incoming packet that has been read

  // private int mPacketToRead = 0; // number of packet to read with laser-commands
  Thread mConsumer = null;

  final Object mNewDataFlag = new Object();
  private BleQueue mQueue;

  boolean mThreadConsumerWorking = false;

  /** cstr
   * @param ctx       context
   * @param app       application
   * @param address   device address (not used: TODO drop)
   * @param bt_device remote device
   */
  public DistoXBLEComm(Context ctx,TopoDroidApp app, String address, BluetoothDevice bt_device )
  {
    super( app );
    // TDLog.v( "XBLE cstr" );
    // mRemoteAddress = address;
    mRemoteBtDevice  = bt_device;
    mContext = ctx;
    // mNewDataFlag = new Object();
    mQueue = new BleQueue();
    mConsumer = new Thread() { // this is the thread that consumes data on the queue
      @Override public void run() {
        //mThreadConsumerWorking = true;
        while ( true ) {
          // TDLog.v( "XBLE comm: Queue size " + mQueue.size );
          BleBuffer buffer = mQueue.get();
          if ( buffer == null ) continue;
          if ( buffer.type == DATA_PRIM ) {
            if ( buffer.data == null) {
              TDLog.Error( "XBLE comm: buffer PRIM with null data");
              continue;
            }
            // ++mNrReadPackets; this is incremented once for DATA and once for VECTOR by TopoDroidComm
            // TDLog.v( "XBLE comm: buffer PRIM read " + mNrReadPackets );
            int res = ((DistoXBLEProtocol)mProtocol).packetProcess( buffer.data );
            if ( res == DistoXBLEProtocol.PACKET_FLASH_BYTES_1 ) continue;   // first-half of firmware block received
            synchronized (mNewDataFlag) {
              // if ( mPacketToRead > 0 ) mPacketToRead --; // only for shot data from laser task
              mPacketType = res;
              mNewDataFlag.notifyAll(); // wake sleeping threads
            }
          } else if ( buffer.type == DATA_QUIT ) {
            TDLog.v( "XBLE comm: buffer QUIT");
            break;
          }
        }
      }
    };
    mConsumer.start();
    // TDLog.v( "XBLE comm: cstr, addr " + address );
    // mOps = new ConcurrentLinkedQueue<BleOperation>();
    // clearPending();
  }

  /** @return true if it is downloading or in skip-notify mode
   */
  boolean isDownloading() 
  { 
    // TDLog.v("XBLE comm is downloading - skip " + mSkipNotify );
    return mApp.isDownloading() || mSkipNotify;
  }

  /* terminate the consumer thread - put a "quit" buffer on the queue
   * @note this method has still to be used
   */
  @Override
  public void terminate()
  {
    // TDLog.v("XBLE comm terminate");
    if ( mConsumer != null ) {
      // put a DATA_QUIT buffer on the queue
      mQueue.put( DATA_QUIT, new byte[0] );
    }
  }
  // -------------------------------------------------------------
  /**
   * connection and data handling must run on a separate thread
   */

  /** connect to the remote DistoXBLE device
   * @param device    device (info)
   * @param lister    data lister
   // * @param data_type expected type of data (unused)
   * @return true if success
   * @note Device has mAddress, mModel, mName, mNickname, mType
   * the only thing that coincide with the remote_device is the address
   */
  private boolean connectDistoXBLEDevice( Device device, ListerHandler lister /*, int data_type */ )
  {
    mLister = lister;
    if ( mRemoteBtDevice == null ) {
      TDToast.makeBad( R.string.ble_no_remote );
      // TDLog.Error("XBLE comm ERROR null remote device");
      // TDLog.v( "XBLE comm - connect Device: null = [3b] status DISCONNECTED" );
      notifyStatus( ConnectionState.CONN_DISCONNECTED );
      return false;
    }
    // TDLog.v("XBLE comm - connect device status WAITING");
    notifyStatus( ConnectionState.CONN_WAITING );
    mReconnect   = true;
    mOps         = new ConcurrentLinkedQueue< BleOperation >();
    mProtocol    = new DistoXBLEProtocol( mContext, mApp, lister, device, this );
    // mChrtChanged = new BricChrtChanged( this, mQueue );
    // mCallback    = new BleCallback( this, mChrtChanged, false ); // auto_connect false
    mCallback    = new BleCallback( this, false ); // auto_connect false

    // mPendingCommands = 0; // FIXME COMPOSITE_COMMANDS
    // clearPending();

    // TDLog.v( "XBLE comm connect device = [3a] status WAITING" );
    int ret = enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
    // TDLog.v( "XBLE connects ... " + ret);
    clearPending();
    return true;
  }

  /** open connection to the GATT
   * @param ctx       context
   * @param bt_device (remote) bluetooth device
   */
  public void connectGatt( Context ctx, BluetoothDevice bt_device ) // called from BleOpConnect
  {
    // TDLog.v( "XBLE comm ***** connect GATT");
    mContext = ctx;
    mCallback.connectGatt( mContext, bt_device );
    // setupNotifications(); // FIXME_XBLE
  }

  /** connect to the remote XBLE device
   * @param address   device address (unused)
   * @param lister    data lister
   * @param data_type expected type of data (unused)
   * @param timeout   ...
   * @return true if success
   */
  @Override
  public boolean connectDevice(String address, ListerHandler lister, int data_type, int timeout ) // FIXME XBLE_DATA_TYPE ?
  {
    // TDLog.v( "XBLE comm connect Device");
    mNrReadPackets = 0;
    mDataType      = data_type;
    return connectDistoXBLEDevice( TDInstance.getDeviceA(), lister /*, data_type */ );
  }

  // ----------------- DISCONNECT -------------------------------

  /** notified that the device has disconnected
   * @note from onConnectionStateChange STATE_DISCONNECTED
   */
  public void disconnected()
  {
    clearPending();
    mOps.clear();
    // mPendingCommands = 0; // FIXME COMPOSITE_COMMANDS
    mBTConnected = false;
    // TDLog.v( "XBLE comm disconnected status DISCONNECTED ");
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
  }

  public void connected()
  {
    // TDLog.v( "XBLE comm connected" );
    clearPending();
  }

  public void disconnectGatt()  // called from BleOpDisconnect
  {
    // TDLog.v( "XBLE comm disconnect GATT status DISCONNECTED ");
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    mCallback.closeGatt();
  }

  /** disconnect from the remote device
   */
  @Override
  public boolean disconnectDevice()
  {
    // TDLog.v( "XBLE comm ***** disconnect device = connected:" + mBTConnected );
    return closeDevice( false );
  }

  // this is called only on a GATT failure, or the user disconnects
  private boolean closeDevice( boolean force )
  {
    mReconnect = false;
    if ( mBTConnected || force ) {
      // TDLog.v( "XBLE close device - connected " + mBTConnected + " force " + force );
      //mThreadConsumerWorking = false;
      mBTConnected = false;
      notifyStatus( ConnectionState.CONN_DISCONNECTED ); // not necessary
      // TDLog.v( "XBLE comm ***** close device");
      int ret = enqueueOp( new BleOpDisconnect( mContext, this ) ); // exec disconnectGatt
      doNextOp();
      // TDLog.v( "XBLE comm: close Device - disconnect ... ops " + ret );
    }
    return true;
  }

  // --------------------------------------------------------------------------
  private BleOperation mPendingOp = null;

  /** clear the pending op and do the next if the queue is not empty
   */
  private void clearPending()
  {
    mPendingOp = null;
    // if ( ! mOps.isEmpty() || mPendingCommands > 0 ) doNextOp();
    if ( ! mOps.isEmpty() ) doNextOp();
  }

  /** add a BLE op to the queue
   * @param op   BLE op
   * @return the length of the ops queue
   */
  private int enqueueOp( BleOperation op )
  {
    mOps.add( op );
    // printOps(); // DEBUG
    return mOps.size();
  }

 /** do the next op on the queue
  * @note access by BricChrtChanged
  */
  private void doNextOp()
  {
    if ( mPendingOp != null ) {
      // TDLog.v( "XBLE comm: next op with pending not null, ops " + mOps.size() );
      return;
    }
    mPendingOp = mOps.poll();
    // TDLog.v( "XBLE comm: polled, ops " + mOps.size() );
    if ( mPendingOp != null ) {
      mPendingOp.execute();
    }
    // else if ( mPendingCommands > 0 ) {
    //   enqueueShot( this );
    //   -- mPendingCommands;
    // }
  }

  // BleComm interface

  /** notified that the MTU (max transmit unit) has changed
   * @param mtu    max transmit unit
   */
  public void changedMtu( int mtu )
  {
    // TDLog.v("XBLE on MTU changed");
    clearPending();
  }

  /** notified that the remote RSSI has been read
   * @param rssi   remote rssi
   */
  public void readedRemoteRssi( int rssi )
  {
    // TDLog.v("XBLE readed remote RSSI");
    clearPending();
  }

  /** notifies that a characteristics has changed
   * @param chrt    changed characteristics
   */
  public void changedChrt( BluetoothGattCharacteristic chrt )
  {
    String uuid_str = chrt.getUuid().toString();
    if ( uuid_str.equals( DistoXBLEConst.DISTOXBLE_CHRT_READ_UUID_STR ) ) {
      // TDLog.v( "XBLE comm: changed read chrt" );
      // TODO set buffer type according to the read value[]
      mQueue.put( DATA_PRIM, chrt.getValue() );
    } else if ( uuid_str.equals( DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID_STR ) ) {
      // TDLog.v( "XBLE comm: changed write chrt" );
    } else {
      // TDLog.v( "XBLE comm: changed unknown chrt" );
    }
  }

  /** notified that bytes have been read from the read characteristics
   * @param uuid_str  service UUID string
   * @param bytes    array of read bytes 
   */
  public void readedChrt( String uuid_str, byte[] bytes )
  {
    // TDLog.v( "XBLE comm: readed chrt " + bytes.length );
  }

  /** notified that bytes have been written to the write characteristics
   * @param uuid_str  service UUID string
   * @param bytes    array of written bytes 
   */
  public void writtenChrt( String uuid_str, byte[] bytes )
  {
    // TDLog.v( "XBLE comm: written chrt " + bytes.length );
    clearPending();
  }

  /** notified that bytes have been read
   * @param uuid_str  service UUID string
   * @param uuid_chrt_str characteristics UUID string
   * @param bytes    array of read bytes 
   */
  public void readedDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    // TDLog.v( "XBLE comm: readed desc - bytes " + bytes.length );
  }

  /** notified that bytes have been written
   * @param uuid_str  service UUID string
   * @param uuid_chrt_str characteristics UUID string
   * @param bytes    array of written bytes 
   */
  public void writtenDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    // TDLog.v( "XBLE comm: written desc - bytes " + bytes.length + " UUID " + uuid_str + " chrt " + uuid_chrt_str );
    clearPending();
  }

  /** notified that a reliable write was completed
   */
  public void completedReliableWrite()
  {
    // TDLog.v( "DistoXBLE comm: reliable write" );
  }

  /** read a characteristics
   * @param srvUuid  service UUID
   * @param chrtUuid characteristics UUID
   * @return true if successful
   * @note this is run by BleOpChrtRead
   */
  public boolean readChrt(UUID srvUuid, UUID chrtUuid )
  {
    // TDLog.v( "XBLE comm: read chrt " + chrtUuid.toString() );
    return mCallback.readChrt( srvUuid, chrtUuid );
  }

  /** write a characteristics
   * @param srvUuid  service UUID
   * @param chrtUuid characteristics UUID
   * @param bytes    array of bytes to write
   * @return true if successful
   * @note this is run by BleOpChrtWrite
   */
  public boolean writeChrt( UUID srvUuid, UUID chrtUuid, byte[] bytes )
  {
    // TDLog.v( "XBLE comm: write chrt " + chrtUuid.toString() );
    return mCallback.writeChrt( srvUuid, chrtUuid, bytes );
  }

  /** react to service discovery
   * @param gatt   bluetooth GATT
   * @note from onServicesDiscovered
   *
   * What actually takes time is the service discovery which is always done when the
   * device has connected (even if you don't call discoverServices). Any command is delayed
   * until that is complete. To speed it up, you should try to shrink the peripheral's GATT db.
   * You could also try to send an MTU request from the peripheral directly after connection
   * to make the service discovery use less packets.
   */
  public int servicesDiscovered( BluetoothGatt gatt )
  {
    TDLog.v( "XBLE comm discovered services");
    enqueueOp( new BleOpNotify( mContext, this, DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_READ_UUID, true ) );
    doNextOp();
    mBTConnected  = true;
    // mPacketToRead = 0;
    /*if(!mThreadConsumerWorking) {
        mThreadConsumerWorking = true;
        mConsumer.start();
    }*/
    // TDLog.v( "XBLE comm discovered services status CONNECTED" );
    notifyStatus( ConnectionState.CONN_CONNECTED );
    // TODO write a resend-interrupt to the DistoXBLE
    return 0;
  }

  /** enable P-notify
   * @param srvUuid  service UUID
   * @param chrtUuid characteristics UUID
   * @return ???
   */
  public boolean enablePNotify( UUID srvUuid, UUID chrtUuid )
  {
    // TDLog.v("XBLE enable P notify");
    return mCallback.enablePNotify( srvUuid, chrtUuid );
  }

  /** enable P-indicate
   * @param srvUuid  service UUID
   * @param chrtUuid characteristics UUID
   * @return ???
   */
  public boolean enablePIndicate( UUID srvUuid, UUID chrtUuid )
  {
    // TDLog.v("XBLE enable P indicate");
    return mCallback.enablePIndicate( srvUuid, chrtUuid );
  }

  /** react to an error
   * @param status   GATT error status
   * @param extra    error extra message
   */
  public void error( int status, String extra )
  {
    switch ( status ) {
      case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
        TDLog.Error("XBLE COMM: invalid attr length " + extra );
        break;
      case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
        TDLog.Error("XBLE COMM: write not permitted " + extra );
        break;
      case BluetoothGatt.GATT_READ_NOT_PERMITTED:
        TDLog.Error("XBLE COMM: read not permitted " + extra );
        break;
      case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
        TDLog.Error("XBLE COMM: insufficient encrypt " + extra );
        break;
      case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
        TDLog.Error("XBLE COMM: insufficient auth " + extra );
        break;
      case BleCallback.CONNECTION_TIMEOUT:
      case BleCallback.CONNECTION_133: // unfortunately this happens
        // TDLog.v( "XBLE comm: connection timeout or 133");
        // notifyStatus( ConnectionState.CONN_WAITING );
        reconnectDevice();
        break;
      default:
        TDLog.Error("XBLE comm ***** ERROR " + status + ": reconnecting ...");
        reconnectDevice();
    }
    clearPending();
  }

  /** try to recover from an error ... and reconnect
   */
  private void reconnectDevice()
  {
    mOps.clear();
    // mPendingCommands = 0; // FIXME COMPOSITE_COMMANDS
    clearPending();
    mCallback.closeGatt();
    if ( mReconnect ) {
      TDLog.v( "XBLE comm ***** reconnect yes Device = [4a] status WAITING" );
      notifyStatus( ConnectionState.CONN_WAITING );
      enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
      doNextOp();
      mBTConnected = true;
    } else {
      // TDLog.v( "XBLE comm ***** reconnect no Device = [4b] status DISCONNECTED" );
      notifyStatus( ConnectionState.CONN_DISCONNECTED );
    }
  }

  /** react to a failure (unrecoverable error): clear pending op and close the connection to the remote device
   * @param status   GATT error status (unused)
   * @param extra    failure extra message (unused)
   */
  public void failure( int status, String extra )
  {
    // notifyStatus( ConnectionState.CONN_DISCONNECTED ); // this will be called by disconnected
    clearPending();
    // TDLog.v( "XBLE comm Failure: disconnecting ...");
    closeDevice( false );
  }

  /** forward status notification to the application
   * @param status   new status
   * @note the notification is delivered to this class' lister
   */
  @Override
  public void notifyStatus( int status )
  {
    // TDLog.v("XBLE notify status " + status + " skip " + mSkipNotify );
    if ( mSkipNotify ) return;
    mApp.notifyListerStatus( mLister, status );
  }

  /** prepare a write op and put it on the queue - call the next op
   * @param srvUuid   service UUID
   * @param chrtUuid  chracteristic UUID
   * @param bytes     data array byte
   * @param addHeader whether to add a (6-byte) header "data:#" and 2-byte footer "\r\n"
   * @return ...
   */
  private boolean enlistWrite( UUID srvUuid, UUID chrtUuid, byte[] bytes, boolean addHeader )
  {
    BluetoothGattCharacteristic chrt = mCallback.getWriteChrt( srvUuid, chrtUuid );
    if ( chrt == null ) {
      TDLog.Error("XLE comm enlist write: null write chrt");
      return false;
    }
    //Chrt.getPermission() always returns 0, I don't know why. Siwei Tian deleted
    // if ( ! BleUtils.isChrtWrite( chrt ) ) {
    //   TDLog.Error("XLE comm enlist write: cannot write chrt");
    //   return false;
    // }
    // TDLog.v( "XBLE comm: enlist chrt write " + chrtUuid.toString() );
    if ( addHeader ) {
      byte[] framebytes = new byte[bytes.length + 8];
      framebytes[0] = 'd';
      framebytes[1] = 'a';
      framebytes[2] = 't';
      framebytes[3] = 'a';
      framebytes[4] = ':';
      framebytes[5] = (byte)(bytes.length);
      int i = 0;
      for ( i = 0;i < bytes.length; i++ ) {
        framebytes[i+6] = bytes[i];
      }
      framebytes[i+6] = '\r';
      framebytes[i+7] = '\n';
      enqueueOp( new BleOpChrtWrite( mContext, this, srvUuid, chrtUuid, framebytes ) );
    } else {
      enqueueOp( new BleOpChrtWrite( mContext, this, srvUuid, chrtUuid, bytes ) );
    }
    doNextOp();
    //wait 100ms to let the MCU to receive correct frame, Siwei Tian added
    return true;
  }

  /** get DistoX-BLE hw/fw info, and display that on the Info dialog
   * @param info     XBLE info dialog
   */
  public void getXBLEInfo( DistoXBLEInfoDialog info )
  {
    if ( info == null ) return;
    // TDLog.v("XBLE comm get XBLE info");
    if ( readMemory( DistoXBLEDetails.FIRMWARE_ADDRESS, 4 ) != null ) { // ?? there was not 4
      info.SetVal( mPacketType, ((DistoXBLEProtocol)mProtocol).mFirmVer);
    }
    if ( readMemory( DistoXBLEDetails.HARDWARE_ADDRESS, 4 ) != null ) {
      info.SetVal( mPacketType, ((DistoXBLEProtocol)mProtocol).mHardVer);
    }
  }

  // --------------------------------------------------------
  /**
   * nothing to read (only write) --> no AsyncTask
   * @param address   remote device address
   * @param what      command to send to the remote device
   * @param to_read   number of data to read (unused)
   * @param lister    callback handler
   * @param data_type packet datatype
   * @param closeBT   whether to close the connection at the end
   */
  public void setXBLELaser( String address, int what, int to_read, ListerHandler lister, int data_type, boolean closeBT )
  {
    mSkipNotify = true;
    Thread laserThread = new Thread() {
      @Override public void run() {
        if ( ! tryConnectDevice( address, lister, 0 ) ) {
          TDLog.Error("XBLE laser failed connect device");
          closeDevice( true );
          mSkipNotify = false;
          return; 
        }
        // mNrReadPackets = 0;
        // mPacketToRead = to_read; // set the number of packet to read
        // TDLog.v("XBLE set laser: " + what + " packet " + to_read + " close BT " + closeBT );
        switch ( what ) {
          case DistoX.DISTOX_OFF:
            sendCommand( (byte)DistoX.DISTOX_OFF );
            break;
          case Device.LASER_ON:
            sendCommand( (byte)DistoX.LASER_ON );
            break;
          case Device.LASER_OFF:
            sendCommand( (byte)DistoX.LASER_OFF );
            break;
          case Device.MEASURE:
            // sendCommand( (byte)DistoX.MEASURE );
            // break;
          case Device.MEASURE_DOWNLOAD:
            sendCommand( (byte)DistoX.MEASURE );
            break;
          case DistoX.CALIB_OFF:
            sendCommand( (byte)DistoX.CALIB_OFF );
            break;
          case DistoX.CALIB_ON:
            sendCommand( (byte)DistoX.CALIB_ON );
            break;
        }
        TDUtil.slowDown(600);
        if ( closeBT ) {
          // synchronized ( mNewDataFlag ) {
          //   try {
          //     TDLog.v("XBLE to read " + to_read + " read " + mNrReadPackets );
          //     while ( 2 * to_read > mNrReadPackets ) mNewDataFlag.wait( 500 ); // 0.5 seconds
          //   } catch ( InterruptedException e ) { 
          //     TDLog.v("XBLE interrupted setXBLELaser");
          //     // e.printStackTrace();
          //   }
          // }
          TDUtil.slowDown( 2000 );
          // syncWait(2000, "laser close device");
          // TDLog.v("XBLE laser close device");
          closeDevice( true );
          mSkipNotify = false;
        }
      }
    };
    laserThread.start();
  }

  // ----------------- SEND COMMAND -------------------------------

  /** send a command to the DistoXBLE
   * @param cmd    command code
   * @return true if the command was scheduled
   * @note used by the protocol to send ack
   */
  @Override
  public boolean sendCommand( int cmd )
  {
    if ( ! isConnected() ) return false;
    if ( cmd == 0 ) return false;
    // TDLog.v( String.format( "XBLE comm send cmd 0x%02x", cmd ) );
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, new byte[] {(byte)cmd}, true);
    return true;
  }

  @Override
  public byte[] readMemory( String address, int addr )
  {
    TDLog.Error("XBLE readMemory( String address, int addr ) not implemented");
    return null;
  }

  /** 0x38: read 4 bytes from memory synchronously
   * @param addr memory address
   * @return array of read bytes, or null on failure
   * @note the thread waits up to 2 seconds for the reply - reading from memory takes about 1 sec
   */
  public byte[] readMemory( int addr )
  {
    // TDLog.v( String.format("XBLE read memory 0x%08x", addr) );
    byte[] cmd = new byte[3];
    cmd[0] = MemoryOctet.BYTE_PACKET_REPLY; // 0x38;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, cmd, true );
    syncWait( 2000, "read memory" );
    // synchronized ( mNewDataFlag ) {
    //   try {
    //     long start = System.currentTimeMillis();
    //     mNewDataFlag.wait( 2000 ); // 2 seconds
    //     // here if the thread gets notified
    //     long millis = System.currentTimeMillis() - start;
    //     TDLog.v("XBLE read-memory waited " + millis + " msec, packet type " + mPacketType );
    //   } catch ( InterruptedException e ) { 
    //     e.printStackTrace();
    //   }
    // }
    if ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) == DistoXBLEProtocol.PACKET_REPLY ) {
      return ( (DistoXBLEProtocol) mProtocol).mRepliedData;
    } else {
      TDLog.Error("XBLE read memory: no reply");
    }
    return null;
  }

  /** read bytes from memory, synchronously
   * @param addr memory address
   * @param len  number of bytes to read (between 0 and 124)
   * @return array of read bytes, or null on failure
   */
  public byte[] readMemory( int addr, int len )
  {
    // TDLog.v("XBLE read memory " + addr + " " + len  );
    if ( len < 0 || len > 124 ) return null;
    byte[] cmd = new byte[4];
    cmd[0] = 0x3d;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = (byte)(len);
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, cmd, true );
    syncWait( 2000, "read memory" );
    // synchronized ( mNewDataFlag ) {
    //   try {
    //     long start = System.currentTimeMillis();
    //     mNewDataFlag.wait( 2000 );
    //     // here if the thread gets notified
    //     long millis = System.currentTimeMillis() - start;
    //     TDLog.v("XBLE read-memory (len " + len + ") waited " + millis + " msec, packet type " + mPacketType );
    //   } catch ( InterruptedException e ) {
    //     e.printStackTrace();
    //   }
    // }
    if ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) == DistoXBLEProtocol.PACKET_REPLY ) {
      byte[] replydata = ( (DistoXBLEProtocol) mProtocol).mRepliedData;
      if(replydata.length == len)
        return replydata;
      else return null;
    }
    return null;
  }

  /** write 4 bytes to memory and wait synchronously for the reply packet
   * @param addr   memory address
   * @param data   4-byte array
   * @return true if the bytes have been written to memory 
   */
  public boolean writeMemory( int addr, byte[] data )
  {
    // TDLog.v("XBLE write memory " + addr + " bytes " + data.length );
    if ( data.length < 4 ) return false;
    byte[] cmd = new byte[7];
    cmd[0] = MemoryOctet.BYTE_PACKET_REQST; // 0x39;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = data[0];
    cmd[4] = data[1];
    cmd[5] = data[2];
    cmd[6] = data[3];
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, cmd, true );
    syncWait( 2000, "write memory" );
    // synchronized ( mNewDataFlag ) {
    //   try {
    //     long start = System.currentTimeMillis();
    //     mNewDataFlag.wait(2000);
    //     long millis = System.currentTimeMillis() - start;
    //     TDLog.v("XBLE write-memory (len 4) waited " + millis + " msec, packet type " + mPacketType );
    //   } catch ( InterruptedException e ) {
    //     // TDLog.v( "XBLE interrupted" );
    //     // e.printStackTrace();
    //   }
    // }
    if ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) == DistoXBLEProtocol.PACKET_REPLY ) {
      byte[] repliedbytes = ((DistoXBLEProtocol) mProtocol).mRepliedData;
      return Arrays.equals(data,repliedbytes);
    }
    return false;
  }


  /** write an array of bytes to memory, synchronously
   * @param addr   memory address
   * @param data   byte array (length must be at least len)
   * @param len    number of bytes to write (between 0 and 124)
   * @return true if the bytes have been written to memory
   */
  public boolean writeMemory( int addr, byte[] data, int len)
  {
    // TDLog.v("XBLE write memory " + addr + " bytes " + data.length + " len " + len);
    if ( data.length < len ) return false;
    if ( len < 0 || len > 124 ) return false;
    byte[] cmd = new byte[len+4];
    cmd[0] = 0x3e;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = (byte)len;
    for(int i = 0;i < len;i++)
      cmd[i+4] = data[i];
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, cmd, true );
    syncWait( 2000, "write memory" );
    // synchronized ( mNewDataFlag ) {
    //   try {
    //     long start = System.currentTimeMillis();
    //     mNewDataFlag.wait(2000);
    //     long millis = System.currentTimeMillis() - start;
    //     TDLog.v("XBLE write-memory (len 4) waited " + millis + " msec, packet type " + mPacketType );
    //   } catch ( InterruptedException e ) {
    //     e.printStackTrace();
    //   }
    // }
    if ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) == DistoXBLEProtocol.PACKET_REPLY ) {
      byte[] repliedbytes = ((DistoXBLEProtocol) mProtocol).mRepliedData;
      return Arrays.equals(data,repliedbytes);
    }
    return false;
  }

  /** read the DistoX-BLE memory
   * @param address   device address
   * @param h0        from address (?)
   * @param h1        to address (?)
   * @param memory    array of octets to be filled by the memory-read
   * @return number of octets that have been read (-1 on error)
   */
  public int readXBLEMemory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  { 
    TDLog.Error("XBLE read XBLE memory TODO");
    return -1;
  }

  @Override
  public boolean toggleCalibMode( String address, int type )
  {
    // TDLog.v("XBLE toggle calib");
    boolean ret = false;
    if ( ! tryConnectDevice( address, null, 0 ) ) return false;
    byte[] result = readMemory( DistoXBLEDetails.STATUS_ADDRESS, 4);
    if ( result == null ) {
      closeDevice( false );
      return false;
    }
    ret = setCalibMode( DistoXBLEDetails.isNotCalibMode( result[0] ) );
    TDUtil.slowDown(700);
    closeDevice( false );
    return ret;
  }


  /** send the set/unset calib-mode command
   *
   * @param turn_on   whether to turn on or off the DistoX calibration mode
   * @return true if success
   * @note commands: 0x31 calin-ON 0x30 calib-OFF
   */
  private boolean setCalibMode( boolean turn_on )
  {
    // TDLog.v("XBLE set calib " + turn_on );
    return sendCommand( turn_on? DistoX.CALIB_ON : DistoX.CALIB_OFF );
  }

  /** batch data download
   * @param address
   * @param lister
   * @param data_type  expected type of data (not really used)
   * @param timeout    (unused)
   * @return number of downloaded data (neg on error)
   *   -1 failed connect
   */
  @Override
  public int downloadData( String address, ListerHandler lister, int data_type, int timeout ) // FIXME_LISTER
  {
    // TDLog.v("XBLE comm batch download " + address );
    // mConnectionMode = 0;
    mDataType = data_type;
    if ( ! tryConnectDevice( address, lister, 0 ) ) return -1; 
    mNrReadPackets = 0;

    //sendCommand( 0x40 );     // start send measure packet ???
    TDUtil.yieldDown( 500 );
    // ??? start a thread that keeps track of read packets - when read done stop it and return
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    synchronized ( mNewDataFlag ) {
      while( true ) {
        if ( syncWait( 2000, "data download" ) ) {
          if ( mPacketType == DistoXBLEProtocol.PACKET_MEASURE_DATA ) {
            mPacketType = DistoXBLEProtocol.PACKET_NONE; // reset
            // TDLog.v("XBLE got packet " + mNrReadPackets );
          } else {
            TDLog.v("XBLE no packet " );
            break;
          }
        }
        // long start = System.currentTimeMillis();
        // try {
        //   mNewDataFlag.wait(2000); // was 5000
        //   if ( mPacketType == DistoXBLEProtocol.PACKET_MEASURE_DATA ) {
        //     mPacketType = DistoXBLEProtocol.PACKET_NONE; // reset
        //     ret++; // increment counter
        //     TDLog.v("XBLE got packet " + ret );
        //   } else {
        //     TDLog.v("XBLE no packet " );
        //     break;
        //   }
        // } catch (InterruptedException e) {
        //   TDLog.v("XBLE interrupted");
        //   // e.printStackTrace();
        // }
        // long millis = System.currentTimeMillis() - start;
        // TDLog.v("XBLE download one data took " + millis + " msec" );
      }
    }
    disconnectDevice();
    return mNrReadPackets / 2; // each data has two packets: DATA and VECTOR
  }

  /** try to connect to the XBLE device
   * @param address   device address
   * @param lister    data lister
   * @param data_type expected type of data
   * @return ...
   */
  public boolean tryConnectDevice( String address, ListerHandler lister, int data_type )
  {
    // TDLog.v("XBLE comm try connect " + address );
    if ( ! mBTConnected ) {
      int timeout = 10;
      if ( ! connectDevice( address, lister, data_type, timeout ) ) {
        return false;
      }
    }
    int loopcnt = 50;
    while( ! mBTConnected ) {
      TDUtil.slowDown(100);
      if ( loopcnt-- == 0 ) {
        disconnectGatt();
        return false;
      }
    }
    return true;
  }

  /** read the calibration coeff from the device
   * @param address   device address
   * @param coeff     array of 52 calibration coeffs (filled by the read)
   * @return true if success
   */
  @Override
  public boolean readCoeff( String address, byte[] coeff )
  {
    // TDLog.v("XBLE comm read coeff " + address );
    if ( coeff == null ) return false;
    int  len  = coeff.length;
    if ( len > 52 ) len = 52; // FIXME force max length of calib coeffs
    if ( ! tryConnectDevice( address, null, 0 ) ) return false;
    int addr = 0x8010;
    byte[] buff = new byte[4];
    int k = 0;
    byte[] coefftmp = readMemory( addr, 52 );
    disconnectDevice();
    if ( coefftmp == null || coefftmp.length != 52 ) return false;
    //coeff = Arrays.copyOf( coefftmp, 52 );  //calling this functions cause a problem: all the params shown in dialog are zero.
    //calling the following is ok. I don't know why. both the 2 functions can copy the right value to coeff[]
    for(int i = 0;i < 52;i++) coeff[i] = coefftmp[i];
    return true;
  }

  /** write the calibration coeff to the device
   * @param address   device address
   * @param coeff     array of 52 calibration coeffs
   * @return true if success
   */
  @Override
  public boolean writeCoeff( String address, byte[] coeff )
  {
    // TDLog.v("XBLE comm write coeff " + address );
    if ( coeff == null ) return false;
    int  len  = coeff.length;
    if( ! tryConnectDevice( address, null, 0 )) return false;
    int k = 0;
    int addr = 0x8010;
    boolean ret = writeMemory(addr, coeff, 52);
    disconnectDevice();
    return ret;
  }

  /**
   * calculate CRC-16 (polynomial 1 + x^2 + x^15 + (x^16))
   *
   * @param bytes  array of bytes containing the data
   * @param length length of the data (in the array, starting at index 0)
   * @return crc of the byte array
   */
  private int calCRC16( byte[] bytes, int length )
  {
    int CRC = 0x0000ffff;
    int POLYNOMIAL = 0x0000a001;
    for ( int i = 0; i < length; i++) {
      CRC ^= ((int) bytes[i] & 0x000000ff);
      for ( int j = 0; j < 8; j++) {
        if ((CRC & 0x00000001) != 0) {
          CRC >>= 1;
          CRC ^= POLYNOMIAL;
        } else {
          CRC >>= 1;
        }
      }
    }
    return CRC;
  }

  /** upload a firmware to the device
   * @param address   device address
   * @param file      firmware file
   * @param progress  progress dialog
   */
  public void uploadFirmware( String address, File file, TDProgress progress )
  {
    final boolean DRY_RUN = false; // DEBUG

    TDLog.v( "XBLE upload firmware " + file.getPath() );
    boolean is_log_file = TDLog.isStreamFile();
    if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_FILE ); // set log to file if necessary
    // int ret = 0;

    long len        = file.length();
    String filename = file.getName();
    Resources res   = mContext.getResources();
    Handler handler = new Handler();

    new Thread( new Runnable() {
      public void run() {
        boolean ok = true;
        int cnt = 0;
        String msg;
        byte[] buf = new byte[259];
        buf[0] = MemoryOctet.BYTE_PACKET_FW_WRITE; // (byte)0x3b;
        buf[1] = (byte)0;
        buf[2] = (byte)0;

        try {
          // File fp = new File( filepath );
          if ( ! DRY_RUN ) {
            if ( ! tryConnectDevice( address, null, 0 ) ) ok = false; // return 0;
          }

          FileInputStream fis = new FileInputStream(file);
          DataInputStream dis = new DataInputStream(fis);
          try{
            for ( int addr = 0; ok /* && addr < end_addr */ ; /*++addr*/) {
              // TDLog.v("XBLE fw upload: addr " + addr + " count " + cnt);
              for (int k = 0; k < 256; ++k) buf[k] = (byte) 0xff;
              int nr = dis.read( buf, 0, 256 );
              int crc16 = calCRC16( buf, 256 );
              //for (int k = 0; k < 256; ++k) buf[k] = (byte) 0xff;  //for simulating the bug
              if (nr <= 0) { // EOF ?
                // TDLog.v("XBLE fw upload: file read failure. Result " + nr);
                break;
              }
              //if(addr < 8) continue;
              int flashaddr = addr + 8;
              cnt += nr;
              addr++;
              int repeat = 3; // THIS IS A repeat TEST
              for ( ; repeat > 0; -- repeat ) { // repeat-for: exit with repeat == 0 (error) or -1 (success)
                byte[] seperated_buf1 = new byte[131]; // 131 = 3 (cmd, addr, index) + 128 (payload) 
                seperated_buf1[0] = MemoryOctet.BYTE_PACKET_FW_WRITE; // (byte) 0x3b;
                seperated_buf1[1] = (byte) (flashaddr & 0xff);
                seperated_buf1[2] = 0; //packet index
                System.arraycopy(buf, 0, seperated_buf1, 3, 128);
                if ( DRY_RUN ) {
                  TDUtil.slowDown( 100 );
                } else {
                  enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, seperated_buf1, true);
                }

                byte[] seperated_buf2 = new byte[133];
                seperated_buf2[0] = MemoryOctet.BYTE_PACKET_FW_WRITE; // (byte) 0x3b;
                seperated_buf2[1] = (byte) (flashaddr & 0xff);
                seperated_buf2[2] = 1;
                System.arraycopy(buf, 128, seperated_buf2, 3, 128);
                seperated_buf2[131] = (byte) (crc16 & 0x00FF);
                seperated_buf2[132] = (byte) ((crc16 >> 8) & 0x00FF);
                if ( DRY_RUN ) {
                  TDUtil.slowDown( 100 );
                  mPacketType = DistoXBLEProtocol.PACKET_FLASH_CHECKSUM;
                  ((DistoXBLEProtocol) mProtocol).mCheckCRC = crc16;
                } else {
                  enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, seperated_buf2, true);
                  //TDUtil.yieldDown(1000);
                  mPacketType = DistoXBLEProtocol.PACKET_NONE;
                  syncWait( 5000, "write firmware block" );
                  // synchronized ( mNewDataFlag ) {
                  //   try {
                  //     long start = System.currentTimeMillis();
                  //     mNewDataFlag.wait(5000); // allow long wait for firmware
                  //     long millis = System.currentTimeMillis() - start;
                  //     TDLog.v("XBLE write firmware block waited " + millis + " msec" );
                  //   } catch (InterruptedException e) {
                  //     e.printStackTrace();
                  //   }
                  // }
                }
                if ( mPacketType == DistoXBLEProtocol.PACKET_FLASH_CHECKSUM ) {
                  //int checksum = 0;
                  //for (int i = 0; i < 256; i++) checksum += (buf[i] & 0xff);
                  int ret_crc16 = ((DistoXBLEProtocol) mProtocol).mCheckCRC;
                  int ret_code  = ((DistoXBLEProtocol) mProtocol).mFwOpReturnCode;
                  if ( ret_crc16 != crc16 || ret_code != 0 ) {
                    TDLog.v( "XBLE fw upload: fail at " + cnt + " buf[0]: " + buf[0] + " reply addr " + addr + " code " + ret_code + " CRC " + ret_crc16 );
                    // ok = false; // without repeat-for uncomment these
                    // break;
                  } else {
                    String msg1 = String.format( mContext.getResources().getString( R.string.firmware_uploaded ), "XBLE", cnt );
                    int cnt1 = cnt;
                    TDLog.v( msg1 );
                    if ( progress != null ) {
                      handler.post( new Runnable() {
                        public void run() {
                          progress.setProgress( cnt1 );
                          progress.setText( msg1 );
                        }
                      } );
                    }
                    repeat = 0; // then the for-loop breaks with repeat = -1
                  }
                } else {
                  TDLog.v( "XBLE fw upload: fail at " + cnt ); // repeat
                  // ok = false; // without repeat-for uncomment these
                  // break;
                }
              }
              if ( repeat == 0 ) {
                ok = false;
                break;
              }
            }
            fis.close();
          } catch ( EOFException e ) { // OK
            TDLog.v("XBLE fw update: EOF " + e.getMessage());
          } catch ( FileNotFoundException e ) {
            TDLog.v( "XBLE fw update: Not Found error " + e.getMessage() );
            ok = false;
          }
        } catch ( IOException e ) {
          TDLog.v( "XBLE fw update: IO error " + e.getMessage() );
          ok = false;
        }
        closeDevice( false );     //close ble here
        msg = "XBLE Firmware update: result is " + (ok? "OK" : "FAIL") + " count " + cnt;
        TDLog.v( msg );
        int ret = ( ok ? cnt : -cnt );
        TDLog.v( "Dialog Firmware upload result: written " + ret + " bytes of " + len );

        String msg2 = ( ret > 0 )? String.format( res.getString(R.string.firmware_file_uploaded), filename, ret, len )
                                 : res.getString(R.string.firmware_file_upload_fail);
        if ( progress != null ) {
          handler.post( new Runnable() {
            public void run() {
              progress.setDone( msg2  );
            }
          } );
        } else { // run on UI thread
          handler.post( new Runnable() { 
            public void run () { TDToast.makeLong( msg2 ); }
          } );
        }
      }
    } ).start();
    if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // reset log stream if necessary
  }

  /** read a 256-byte firmware block
   * @param addr   block address
   * @return 256-byte array, block of firmware
   */
  private byte[] readFirmwareBlock(int addr)
  {
    byte[] buf = new byte[3];
    buf[0] = MemoryOctet.BYTE_PACKET_FW_READ; // (byte)0x3a;
    buf[1] = (byte)( addr & 0xff );
    buf[2] = 0; // not necessary
    try {
      enlistWrite(DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, buf, true);
      mPacketType = DistoXBLEProtocol.PACKET_NONE;
      syncWait( 5000, "read firmware block" );
      // synchronized (mNewDataFlag) {
      //   try {
      //     long start = System.currentTimeMillis();
      //     mNewDataFlag.wait(5000); // allow long wait for firmware
      //     long millis = System.currentTimeMillis() - start;
      //     TDLog.v("XBLE read firmware block waited " + millis + " msec" );
      //   } catch (InterruptedException e) {
      //     e.printStackTrace();
      //   }
      // }
      if ( mPacketType == DistoXBLEProtocol.PACKET_FLASH_BYTES_2 ) {
        buf = ((DistoXBLEProtocol) mProtocol).mFlashBytes;
        int crc16 = calCRC16( buf, 256 );
        if ( crc16 != ((DistoXBLEProtocol) mProtocol).mCheckCRC ) {
          TDLog.Error("XBLE CRC-16 mismatch: got " + crc16 + " expected " + ((DistoXBLEProtocol) mProtocol).mCheckCRC );
          return null;
        }
        return buf;
      }
    } catch (Exception e) {
      TDLog.Error("XBLE error " + e.getMessage() );
      return null;
    }
    TDLog.v("XBLE packet not FLASH_BYTES_2");
    return null;
  }

  /** read the firmware from the device and save it to file
   * @param address   device address (passed to tryConnect)
   * @param file      output file
   * @return ???
   */
  public int dumpFirmware( String address, File file )
  {
    TDLog.v( "XBLE fw dump: output filepath " + file.getPath() );
    byte[] buf = new byte[256];

    boolean ok = true;
    int cnt = 0;
    try {
      // TDPath.checkPath( filepath );
      // File fp = new File( filepath );
      FileOutputStream fos = new FileOutputStream(file);
      DataOutputStream dos = new DataOutputStream(fos);
      if ( tryConnectDevice( address, null, 0 ) ) {
        try {
          for ( int addr = 8; ; addr++ ) {
            buf = readFirmwareBlock(addr);
            if ( buf == null || buf.length < 256 ) {
              ok = false;
              break;
            }
            dos.write( buf, 0, 256 );
            cnt += 256;
            int k = 0; // check if the block is fully 0xFF
            for ( ; k<256; ++k ) {
              if ( buf[k] != (byte)0xff ) break;
            }
            if ( k == 256 ) break;
          }
          fos.close();
        } catch (EOFException e) {
          //OK
        } catch (IOException e) {
          ok = false;
        } finally {
          closeDevice( false );
        }
      } else {
        ok = false;
      }
    }catch ( FileNotFoundException e ) {
      return 0;
    }
    return ( ok ? cnt : -cnt );
  }

  /** 0x3c: read the hardware code
   * @param address device address
   * @param hw      (unused)
   * @return 2-byte hw code
   */
  public byte[] readFirmwareSignature( String address, int hw ) 
  {
    if ( ! tryConnectDevice( address, null, 0 ) ) return null;
    byte[] buf = new byte[1];
    buf[0] = (byte)0x3c;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, buf, true);
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    syncWait( 5000, "read firmware signature" );
    // synchronized (mNewDataFlag) {
    //   try {
    //     long start = System.currentTimeMillis();
    //     mNewDataFlag.wait(5000); // allow long wait for firmware
    //     long millis = System.currentTimeMillis() - start;
    //     TDLog.v("XBLE read firmware signature waited " + millis + " msec" );
    //   } catch (InterruptedException e) {
    //     e.printStackTrace();
    //   }
    // }
    boolean bisSuccess = false;
    if ( mPacketType == DistoXBLEProtocol.PACKET_SIGNATURE ) {
      buf = ((DistoXBLEProtocol) mProtocol).mRepliedData;
      bisSuccess = true;
    }
    closeDevice( false );
    return (bisSuccess)?  buf : null;
  }

  /** synchronized wait
   * @param msec  wait timeout [msec]
   * @param msg   log messsage
   * @return true if ok, false if interrupted
   */
  private boolean syncWait( long msec, String msg )
  {
    // TDLog.v("XBLE sync wait " + msec );
    synchronized ( mNewDataFlag ) {
      try {
        long start = System.currentTimeMillis();
        mNewDataFlag.wait( msec );
        long millis = System.currentTimeMillis() - start;
        TDLog.v("XBLE " + msg + " msec " + millis );
        return true;
      } catch ( InterruptedException e ) {
        TDLog.v( "XBLE interrupted wait " + msg );
        // e.printStackTrace();
        return false;
      }
    }
  }
}


