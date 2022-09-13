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
import android.os.Handler;

import com.topodroid.TDX.R;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.DataType;
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
import com.topodroid.prefs.TDSetting;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;

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
  private DistoXBLEInfoDialog mDistoXBLEInfoDialog = null;

  BluetoothGattCharacteristic mReadChrt  = null;
  BluetoothGattCharacteristic mWriteChrt = null;
  //private boolean mReadInitialized  = false;
  //private boolean mWriteInitialized = false;
  private boolean mReconnect = false;

  private int mDataType;
  private int mPacketType;
  // private Handler mLister; // 2022-09-09 no need to store the lister

  private int mPatketToRead = 0;
  Thread mConsumer = null;

  Object mNewDataFlag;
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
    // mRemoteAddress = address;
    mRemoteBtDevice  = bt_device;
    mContext = ctx;
    mNewDataFlag = new Object();
    mQueue = new BleQueue();
    mConsumer = new Thread() {
      @Override public void run() {
        //mThreadConsumerWorking = true;
        while ( true ) {
          TDLog.v( "XBLE comm: Queue size " + mQueue.size );
          BleBuffer buffer = mQueue.get();
          if ( buffer == null ) continue;
          if ( buffer.type == DATA_PRIM ) {
            if ( buffer.data == null) continue;
            int res = ((DistoXBLEProtocol)mProtocol).packetProcess( buffer.data );
            if ( res == DistoXBLEProtocol.PACKET_FLASH_BYTES_1 ) continue;   // non-complete packet received
            synchronized (mNewDataFlag) {
              mPacketType = res;
              mNewDataFlag.notifyAll();
            }
          } else if ( buffer.type == DATA_QUIT ) {
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


  /* terminate the consumer thread - put a "quit" buffer on the queue
   * @note this method has still to be used
   */
  @Override
  public void terminate()
  {
    TDLog.v("BRIC comm terminate");
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
  private boolean connectDistoXBLEDevice( Device device, Handler lister /*, int data_type */ )
  {
    if ( mRemoteBtDevice == null ) {
      TDToast.makeBad( R.string.ble_no_remote );
      // TDLog.Error("XBLE comm ERROR null remote device");
      // TDLog.v( "XBLE comm ***** connect Device: null = [3b] status DISCONNECTED" );
      notifyStatus( ConnectionState.CONN_DISCONNECTED );
      return false;
    }
    notifyStatus( ConnectionState.CONN_WAITING );
    mReconnect   = true;
    mOps         = new ConcurrentLinkedQueue< BleOperation >();
    mProtocol    = new DistoXBLEProtocol( mContext, mApp, lister, device, this );
    // mChrtChanged = new BricChrtChanged( this, mQueue );
    // mCallback    = new BleCallback( this, mChrtChanged, false ); // auto_connect false
    mCallback    = new BleCallback( this, false ); // auto_connect false

    // mPendingCommands = 0; // FIXME COMPOSITE_COMMANDS
    // clearPending();

    TDLog.v( "XBLE comm ***** connect Device = [3a] status WAITING" );
    int ret = enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) ); // exec connectGatt()
    TDLog.v( "XBLE connects ... " + ret);
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

  /** connect to the remote device
   * @param address   device address (unused)
   * @param lister    data lister
   * @param data_type expected type of data (unused)
   * @return true if success
   */
  @Override
  public boolean connectDevice(String address, Handler /* ILister */ lister, int data_type ) // FIXME XBLE_DATA_TYPE ?
  {
    // TDLog.v( "XBLE comm ***** connect Device");
    mNrPacketsRead = 0;
    mDataType      = data_type;
    return connectDistoXBLEDevice( TDInstance.getDeviceA(), lister /*, data_type */ );
  }

  // ----------------- DISCONNECT -------------------------------

  /** notified that the device has disconnected
   * @note from onConnectionStateChange STATE_DISCONNECTED
   */
  public void disconnected()
  {
    TDLog.v( "XBLE comm disconnected" );
    clearPending();
    mOps.clear();
    // mPendingCommands = 0; // FIXME COMPOSITE_COMMANDS
    mBTConnected = false;
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
  }

  public void connected()
  {
    clearPending();
  }

  public void disconnectGatt()  // called from BleOpDisconnect
  {
    // TDLog.v( "XBLE comm ***** disconnect GATT" );
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    mCallback.closeGatt();
  }

  @Override
  public boolean disconnectDevice()
  {
    // TDLog.v( "XBLE comm ***** disconnect device = connected:" + mBTConnected );
    return closeDevice();
  }

  // this is called only on a GATT failure, or the user disconnects
  private boolean closeDevice()
  {
    mReconnect = false;
    if ( mBTConnected ) {
        //mThreadConsumerWorking = false;
        mBTConnected = false;
        notifyStatus( ConnectionState.CONN_DISCONNECTED ); // not necessary
      // TDLog.v( "XBLE comm ***** close device");
        int ret = enqueueOp( new BleOpDisconnect( mContext, this ) ); // exec disconnectGatt
        doNextOp();
        TDLog.v( "XBLE comm: close Device - disconnect ... ops " + ret );
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
    clearPending();
  }

  /** notified that the remote RSSI has been read
   * @param rssi   remote rssi
   */
  public void readedRemoteRssi( int rssi )
  {
    clearPending();
  }

  /** notifies that a characteristics has changed
   * @param chrt    changed characteristics
   */
  public void changedChrt( BluetoothGattCharacteristic chrt )
  {
    String uuid_str = chrt.getUuid().toString();
    if ( uuid_str.equals( DistoXBLEConst.DISTOXBLE_CHRT_READ_UUID_STR ) ) {
      TDLog.v( "XBLE comm: changed read chrt" );
      mQueue.put( DATA_PRIM, chrt.getValue() );
    } else if ( uuid_str.equals( DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID_STR ) ) {
      TDLog.v( "XBLE comm: changed write chrt" );
    }
  }

  /** notified that bytes have been read from the read characteristics
   * @param uuid_str  service UUID string
   * @param bytes    array of read bytes 
   */
  public void readedChrt( String uuid_str, byte[] bytes )
  {
    // TDLog.v( "XBLE comm: readedChrt" );
  }

  /** notified that bytes have been written to the write characteristics
   * @param uuid_str  service UUID string
   * @param bytes    array of written bytes 
   */
  public void writtenChrt( String uuid_str, byte[] bytes )
  {
    clearPending();
  }

  /** notified that bytes have been read
   * @param uuid_str  service UUID string
   * @param uuid_chrt_str characteristics UUID string
   * @param bytes    array of read bytes 
   */
  public void readedDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    TDLog.v( "XBLE comm: readedDesc" );
  }

  /** notified that bytes have been written
   * @param uuid_str  service UUID string
   * @param uuid_chrt_str characteristics UUID string
   * @param bytes    array of written bytes 
   */
  public void writtenDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    // TDLog.v( "XBLE comm: ====== written desc " + uuid_str + " " + uuid_chrt_str );
    clearPending();
  }

  /** notified that a reliable write was completed
   */
  public void completedReliableWrite()
  {
    TDLog.v( "DistoXBLE comm: reliable write" );
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
   */
  public int servicesDiscovered( BluetoothGatt gatt )
  {
    enqueueOp( new BleOpNotify( mContext, this, DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_READ_UUID, true ) );
    doNextOp();
    mBTConnected  = true;
    mPatketToRead = 0;
    /*if(!mThreadConsumerWorking) {
        mThreadConsumerWorking = true;
        mConsumer.start();
    }*/
    TDLog.v( "XBLE comm discovered services status CONNECTED" );
    notifyStatus( ConnectionState.CONN_CONNECTED );
    return 0;
  }

  /** enable P-notify
   * @param srvUuid  service UUID
   * @param chrtUuid characteristics UUID
   * @return ???
   */
  public boolean enablePNotify( UUID srvUuid, UUID chrtUuid ) { return mCallback.enablePNotify( srvUuid, chrtUuid ); }

  /** enable P-indicate
   * @param srvUuid  service UUID
   * @param chrtUuid characteristics UUID
   * @return ???
   */
  public boolean enablePIndicate( UUID srvUuid, UUID chrtUuid ) { return mCallback.enablePIndicate( srvUuid, chrtUuid ); }

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
      TDLog.v( "XBLE comm ***** reconnect no Device = [4b] status DISCONNECTED" );
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
    closeDevice();
  }

  /** forward status notification to the application
   * @param status   new status
   */
  public void notifyStatus( int status )
  {
    mApp.notifyStatus( status );
  }

  /** prepare a write op and put it on the queue - call the next op
   * @param srvUuid   service UUID
   * @param chrtUuid  chracteristic UUID
   * @param bytes     data array byte
   * @param addHeader whether to add a (6-byte) header "data:#"
   * @return ...
   */
  public boolean enlistWrite( UUID srvUuid, UUID chrtUuid, byte[] bytes, boolean addHeader )
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
   */
  public void GetXBLEInfo()
  {
    if ( mDistoXBLEInfoDialog == null ) return;
    if ( readMemory(DistoXBLEDetails.FIRMWARE_ADDRESS, 4) != null ) { // ?? there was not 4
      mDistoXBLEInfoDialog.SetVal(mPacketType,((DistoXBLEProtocol)mProtocol).mFirmVer);
    }
    if ( readMemory(DistoXBLEDetails.HARDWARE_ADDRESS, 4) != null ) {
      mDistoXBLEInfoDialog.SetVal(mPacketType,((DistoXBLEProtocol)mProtocol).mHardVer);
    }
  }

  // --------------------------------------------------------
  /**
   * nothing to read (only write) --> no AsyncTask
   * @param address   remote device address
   * @param what      command to send to the remote device
   * @param lister    callback handler
   * @param data_type packet datatype
   */
  public void setXBLELaser( String address, int what, int to_read, Handler /* ILister */ lister, int data_type, Boolean closeBT ) // FIXME_LISTER
  {
    if ( ! tryConnectDevice( address, lister, 0 ) ) return; // ??? lister was null
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
    mPatketToRead = to_read;
    // mLister = lister;
    TDUtil.slowDown(700);
    if ( closeBT ) {
      disconnectDevice();
    }
  }

  // ----------------- SEND COMMAND -------------------------------
  public boolean sendCommand( int cmd )
  {
    if ( ! isConnected() ) return false;
    if ( cmd != 0 ) {
      // TDLog.v( "XBLE comm send cmd " + cmd );
      enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, new byte[] {(byte)cmd}, true);
    }
    return true;
  }

  public void registerInfo( DistoXBLEInfoDialog info ) { mDistoXBLEInfoDialog = info; }

  /** read 4 bytes from memory
   * @param addr memory address
   * @return array of read bytes, or null on failure
   */
  public byte[] readMemory( int addr )
  {
    byte[] cmd = new byte[3];
    cmd[0] = 0x38;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, cmd, true );
    synchronized ( mNewDataFlag ) {
      try {
        mNewDataFlag.wait(2000);
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }
    // while ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) != DistoXBLEProtocol.PACKET_REPLY ) {
    //   TDUtil.yieldDown(100);
    // }
    if ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) == DistoXBLEProtocol.PACKET_REPLY ) {
      return ( (DistoXBLEProtocol) mProtocol).mRepliedData;
    }
    return null;
  }

  /** read bytes from memory
   * @param addr memory address
   * @param len  number of bytes to read (between 0 and 124)
   * @return array of read bytes, or null on failure
   */
  public byte[] readMemory( int addr, int len )
  {
    if ( len < 0 || len > 124 ) return null;
    byte[] cmd = new byte[4];
    cmd[0] = 0x3d;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = (byte)(len);
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, cmd, true );
    synchronized ( mNewDataFlag ) {
      try {
        mNewDataFlag.wait(2000);
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }
    /*while ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) != DistoXBLEProtocol.PACKET_REPLY ) {
       TDUtil.yieldDown(100);
    }*/
    if ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) == DistoXBLEProtocol.PACKET_REPLY ) {
      byte[] replydata = ( (DistoXBLEProtocol) mProtocol).mRepliedData;
      if(replydata.length == len)
        return replydata;
      else return null;
    }
    return null;
  }

  /** write 4 bytes to memory
   * @param addr   memory address
   * @param data   4-byte array
   * @return ...
   */
  public boolean writeMemory( int addr, byte[] data )
  {
    if ( data.length < 4 ) return false;
    byte[] cmd = new byte[7];
    cmd[0] = 0x39;
    cmd[1] = (byte)(addr & 0xFF);
    cmd[2] = (byte)((addr >> 8) & 0xFF);
    cmd[3] = data[0];
    cmd[4] = data[1];
    cmd[5] = data[2];
    cmd[6] = data[3];
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, cmd, true );
    synchronized ( mNewDataFlag ) {
      try {
        mNewDataFlag.wait(2000);
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }
    if ( (mPacketType & DistoXBLEProtocol.PACKET_REPLY) == DistoXBLEProtocol.PACKET_REPLY ) {
      byte[] repliedbytes = ((DistoXBLEProtocol) mProtocol).mRepliedData;
      return Arrays.equals(data,repliedbytes);
    }
    return false;
  }


  /** write an array of bytes to memory
   * @param addr   memory address
   * @param data   byte array (length must be at least len)
   * @param len    number of bytes to write (between 0 and 124)
   * @return ...
   */
  public boolean writeMemory( int addr, byte[] data, int len)
  {
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
    synchronized ( mNewDataFlag ) {
      try {
        mNewDataFlag.wait(2000);
      } catch ( InterruptedException e ) {
        e.printStackTrace();
      }
    }
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
    // TODO
    return -1;
  }

  public boolean toggleCalibMode( String address, int type )
  {
    boolean ret = false;
    if ( ! tryConnectDevice( address, null, 0 ) ) return false;
    byte[] result = readMemory( DistoXBLEDetails.STATUS_ADDRESS ,4);
    if ( result == null ) {
      closeDevice();
      return false;
    }
    ret = setCalibMode( DistoXBLEDetails.isNotCalibMode( result[0] ) );
    TDUtil.slowDown(700);
    closeDevice();
    return ret;
  }


  /** send the set/unset calib-mode command
   *
   * @param turn_on   whether to turn on or off the DistoX calibration mode
   * @return true if success
   */
  protected boolean setCalibMode( boolean turn_on )
  {
    // return sendCommand( turn_on? 0x31 : 0x30 );
    return sendCommand( turn_on? DistoX.CALIB_ON : DistoX.CALIB_OFF );
  }

  public int downloadData( String address, Handler /* ILister */ lister, int data_type ) // FIXME_LISTER
  {
    //mConnectionMode = 0;
    // mLister = lister;
    mDataType = data_type;
    int ret = 0;
    if ( ! tryConnectDevice( address, lister, 0 ) ) return -1;  // FIXME 2022-09-09 added the lister, was null

    //sendCommand( 0x40 );     // start send measure packet ???
    TDUtil.yieldDown( 500 );
    // start a thread that keeps track of read packets
    // when read done stop it and return
    int timeout = 50;
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    synchronized ( mNewDataFlag ) {
      while( true ) {
        try {
          mNewDataFlag.wait(5000);
          if (mPacketType == DistoXBLEProtocol.PACKET_MEASURE_DATA) {
            mPacketType = DistoXBLEProtocol.PACKET_NONE;
            ret++;
          } else {
            break;
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    disconnectDevice();
    return ret;
  }

  public boolean tryConnectDevice(String address, Handler lister, int data_type)
  {
    if ( ! mBTConnected ) {
      if ( ! connectDevice( address, lister, data_type ) ) {
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

  public boolean readCoeff( String address, byte[] coeff )
  {
    if ( coeff == null ) return false;
    int  len  = coeff.length;
    if ( len > 52 ) len = 52; // FIXME force max length of calib coeffs
    if ( ! tryConnectDevice( address, null, 0 ) ) return false;
    int addr = 0x8010;
    byte[] buff = new byte[4];
    int k = 0;
    byte[] coefftmp = readMemory(addr, 52);

    /*while ( k < len ) {
      buff = readMemory(addr,4);
      if ( buff == null ) return false;
      coeff[k] = buff[0]; ++k;
      coeff[k] = buff[1]; ++k;
      coeff[k] = buff[2]; ++k;
      coeff[k] = buff[3]; ++k;
      addr += 4;
    }*/
    //coeff = coefftmp;
    //TDUtil.yieldDown(1000);
    //writeCoeff(address,coefftmp);

    disconnectDevice();
    if ( coefftmp == null || coefftmp.length != 52 ) return false;
    for ( int i=0; i < 52; ++i ) coeff[i] = coefftmp[i];
    return true;
  }

  public boolean writeCoeff( String address, byte[] coeff )
  {
    if ( coeff == null ) return false;
    int  len  = coeff.length;
    if(!tryConnectDevice(address,null,0)) return false;
    boolean ret = false;
    int k = 0;
    int addr = 0x8010;
    writeMemory(addr, coeff, 52);
    /*byte[] buff = new byte[4];
    while ( k < len ) {
      buff[0] = coeff[k]; ++k;
      buff[1] = coeff[k]; ++k;
      buff[2] = coeff[k]; ++k;
      buff[3] = coeff[k]; ++k;
      TDUtil.yieldDown(100);
      if ( ! writeMemory( addr,buff ) ) return false;
      addr += 4;
    }*/
    disconnectDevice();
    return true;
  }

  public int uploadFirmware( String address, File file )
  {
    TDLog.v( "Comm upload firmware " + file.getPath() );
    boolean is_log_file = TDLog.isStreamFile();
    if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_FILE ); // set log to file if necessary
    int ret = 0;

    byte[] buf = new byte[259];
    buf[0] = (byte)0x3b;
    buf[1] = (byte)0;
    buf[2] = (byte)0;
    boolean ok = true;
    int cnt = 0;
    try {
      // File fp = new File( filepath );
      if (!tryConnectDevice(address, null, 0)) return 0;

      FileInputStream fis = new FileInputStream(file);
      DataInputStream dis = new DataInputStream(fis);
      try{
        for ( int addr = 0; /* addr < end_addr */ ; /*++addr*/) {
          TDLog.f("Firmware upload: addr " + addr + " count " + cnt);
          for (int k = 0; k < 256; ++k) buf[k] = (byte) 0xff;
          int nr = dis.read(buf, 0, 256);
          if (nr <= 0) {
            TDLog.f("Firmware upload: file read failure. Result " + nr);
            break;
          }
          cnt += nr;
          //if(addr < 8) continue;
          int flashaddr = addr + 8;
          addr++;
          byte[] seperated_buf = new byte[131]; // 131 = 3 (cmd, addr, index) + 128 (payload) 
          seperated_buf[0] = (byte) 0x3b;
          seperated_buf[1] = (byte) (flashaddr & 0xff);
          seperated_buf[2] = 0; //packet index
          System.arraycopy(buf, 0, seperated_buf, 3, 128);
          enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, seperated_buf, true);
          seperated_buf = new byte[131];
          seperated_buf[0] = (byte) 0x3b;
          seperated_buf[1] = (byte) (flashaddr & 0xff);
          seperated_buf[2] = 1;
          System.arraycopy(buf, 128, seperated_buf, 3, 128);
          enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, seperated_buf, true);
          //TDUtil.yieldDown(1000);
          mPacketType = DistoXBLEProtocol.PACKET_NONE;
          synchronized ( mNewDataFlag ) {
            try {
              mNewDataFlag.wait(5000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          if ( mPacketType == DistoXBLEProtocol.PACKET_FLASH_CHECKSUM ) {
            int checksum = 0;
            for (int i = 0; i < 256; i++) checksum += (buf[i] & 0xff);
            if ( ((DistoXBLEProtocol) mProtocol).mCheckSum != checksum ) {
              TDLog.f("Firmware upload: fail at " + cnt + " buffer[0]: " + buf[0] + " reply_addr " + addr);
              ok = false;
              break;
            } else {
              TDLog.f("Firmware upload: reply address ok");
            }
          } else {
            ok = false;
            break;
          }
        }
        fis.close();
      } catch ( EOFException e ) { // OK
        TDLog.f("Firmware update: EOF " + e.getMessage());
      } catch ( FileNotFoundException e ) {
        TDLog.f( "Firmware update: Not Found error " + e.getMessage() );
        return 0;
      }
    } catch ( IOException e ) {
      TDLog.f( "Firmware update: IO error " + e.getMessage() );
      ok = false;
    }
    closeDevice();     //close ble here
    TDLog.f( "Firmware update: result is " + (ok? "OK" : "FAIL") + " count " + cnt );
    if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // reset log stream if necessary
    return ( ok ? cnt : -cnt );
  }

  public byte[] readFirmwareBlock(int addr)
  {
    byte[] buf = new byte[3];
    buf[0] = (byte)0x3a;
    buf[1] = (byte)( addr & 0xff );
    buf[2] = 0; // not necessary
    try {
      enlistWrite(DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, buf, true);
      mPacketType = DistoXBLEProtocol.PACKET_NONE;
      synchronized (mNewDataFlag) {
        try {
          mNewDataFlag.wait(5000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      if (mPacketType == DistoXBLEProtocol.PACKET_FLASH_BYTES_2) {
        buf = ((DistoXBLEProtocol) mProtocol).mFlashBytes;
        return buf;
      }
    }catch (Exception e){
      return null;
    }
    return null;
  }

  public int dumpFirmware( String address, File file ){
    TDLog.v( "Proto Firmware dump: output filepath " + file.getPath() );
    byte[] buf = new byte[256];

    boolean ok = true;
    int cnt = 0;
    try {
      // TDPath.checkPath( filepath );
      // File fp = new File( filepath );
      FileOutputStream fos = new FileOutputStream(file);
      DataOutputStream dos = new DataOutputStream(fos);
      tryConnectDevice(address, null, 0);
      try {
        for ( int addr = 8; ; addr++ ) {
          buf = readFirmwareBlock(addr);
          if(buf == null)
          {
            ok = false;
            break;
          }
          else if(buf.length < 256)
          {
            ok = false;
            break;
          }
          dos.write(buf, 0, 256);
          cnt += 256;

          int k = 0; // check there is a byte that is not 0xFF
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
      }
      closeDevice();
    }catch (FileNotFoundException e ) {
      return 0;
    }
    return ( ok ? cnt : -cnt );
  }

  public byte[] readFirmwareSignature(String deviceAddress, int hw) {
    tryConnectDevice(deviceAddress,null,0);
    byte[] buf = new byte[1];
    buf[0] = (byte)0x3c;
    enlistWrite( DistoXBLEConst.DISTOXBLE_SERVICE_UUID, DistoXBLEConst.DISTOXBLE_CHRT_WRITE_UUID, buf, true);
    mPacketType = DistoXBLEProtocol.PACKET_NONE;
    synchronized (mNewDataFlag) {
      try {
        mNewDataFlag.wait(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    boolean bisSuccess = false;
    if (mPacketType == DistoXBLEProtocol.PACKET_SIGNATURE) {
      buf = ((DistoXBLEProtocol) mProtocol).mRepliedData;
      bisSuccess = true;
    }
    closeDevice();
    if(bisSuccess) return buf;
    else return null;
  }
}


