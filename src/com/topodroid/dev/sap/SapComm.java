/* @file SapComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SAP5 communication REQUIRES API-18
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * TopoDroid implementation of BLE callback follows the guidelines of 
 *   Chee Yi Ong,
 *   "The ultimate guide to Android bluetooth low energy"
 *   May 15, 2020
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.dev.sap;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.ListerHandler;
// import com.topodroid.TDX.DataDownloader;
import com.topodroid.TDX.TopoDroidApp;
// import com.topodroid.TDX.TDToast;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.dev.ble.BleUtils;
import com.topodroid.dev.ble.BleComm;
// import com.topodroid.dev.ble.BleChrtChanged;
import com.topodroid.dev.ble.BleCallback;
import com.topodroid.dev.ble.BleOperation;
import com.topodroid.dev.ble.BleOpConnect;
import com.topodroid.dev.ble.BleOpDisconnect;
import com.topodroid.dev.ble.BleOpChrtRead;
import com.topodroid.dev.ble.BleOpChrtWrite;

// import android.os.Handler;
// import android.os.Looper;
// import android.os.Build;
import android.content.Context;

import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothAdapter;
// import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattDescriptor;
// import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

// -----------------------------------------------------------------------------
public class SapComm extends TopoDroidComm
                     implements BleComm // , BleChrtChanged
{
  // -----------------------------------------------
  // BluetoothAdapter   mAdapter;
  // private BluetoothGatt mGatt = null;
  // BluetoothGattCharacteristic mWriteChrt;

  private UUID mServiceUUID;
  private UUID mReadUUID;
  private UUID mWriteUUID;
  private UUID mNotifyUUID;

  private ConcurrentLinkedQueue< BleOperation > mOps;
  private Context         mContext;
  private String          mRemoteAddress;
  private BluetoothDevice mRemoteBtDevice;
  private BleCallback mCallback;
  private SapProtocol mSapProto;
  // private int mDataType = DataType.DATA_SHOT;
  private int mDataType;

  private ListerHandler mLister;
  private int mConnectionMode = -1;
  private boolean mDisconnecting = false;
  private BleOperation mPendingOp = null;

  BluetoothGattCharacteristic mReadChrt  = null;
  BluetoothGattCharacteristic mWriteChrt = null;
  private boolean mReadInitialized  = false;
  private boolean mWriteInitialized = false;

  BluetoothGattCharacteristic getReadChrt() { return mReadChrt; }
  BluetoothGattCharacteristic getWriteChrt() { return mWriteChrt; }

  /** cstr
   * @param app        application
   * @param address    SAP address
   * @param bt_device  BT (SAP) device
   */
  public SapComm( TopoDroidApp app, String address, BluetoothDevice bt_device ) 
  {
    super( app );
    mRemoteAddress  = address;
    mRemoteBtDevice = bt_device;
    // TDLog.v( "SAP comm: cstr, addr " + address );
    mOps = new ConcurrentLinkedQueue< BleOperation >();
    clearPending();
  }

  // void setRemoteDevice( BluetoothDevice device ) 
  // { 
  //   // TDLog.v( "SAP comm: set remote " + device.getAddress() );
  //   mRemoteBtDevice = device;
  // }

  // -------------------------------------------------------------
  // CONNECTION AND DATA HANDLING MUST RUN ON A SEPARATE THREAD
  // 

  /** connect the SAP device
   * @param device    SAP device
   * @param context   context
   * @param data_type expected type of data
   * Device has mAddress, mModel, mName, mNickname, mType
   * the only thing that coincide with the remote_device is the address
   */
  private void connectSapDevice( Device device, Context context, int data_type ) // FIXME BLEX_DATA_TYPE
  {
    mContext = context;
    if ( mRemoteBtDevice == null ) {
      // TDToast.makeBad( R.string.ble_no_remote );
      TDLog.e("SAP comm: error: null remote device");
    } else { // FIXME_SAP6
      if ( device.isSap5() ) {
        mServiceUUID = SapConst.SAP5_SERVICE_UUID;
        mReadUUID    = SapConst.SAP5_CHRT_READ_UUID;
        mWriteUUID   = SapConst.SAP5_CHRT_WRITE_UUID;
      } else if ( device.isSap6() ) {
        TDLog.v("SAP6 connect device");
        mServiceUUID = SapConst.SAP6_SERVICE_UUID;
        mReadUUID    = SapConst.SAP6_CHRT_READ_UUID;
        mWriteUUID   = SapConst.SAP6_CHRT_WRITE_UUID;
        mNotifyUUID  = SapConst.SAP6_DSCR_NOTIFICATION;
      } else {
        TDLog.e("SAP comm: error: not a SAP device");
        return;
      }
      // check that device.mAddress.equals( mRemoteBtDevice.getAddress() 
      TDLog.v( "SAP comm: connect remote addr " + mRemoteBtDevice.getAddress() );
      notifyStatus( ConnectionState.CONN_WAITING );
      mSapProto = new SapProtocol( this, device, context );
      mCallback = new BleCallback( this, true ); // auto_connect true
      mProtocol = mSapProto;
      enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) );
      doNextOp();
      // mCallback.connectGatt( mContext, mRemoteBtDevice );
    }
  }

  // -------------------------------------------------------------
  /** disconnect the GATT
   */
  void doDisconnectGatt()
  {
    TDLog.v( "SAP comm: do disconnect GATT - disconnecting " + mDisconnecting );
    if ( mDisconnecting ) return;
    mDisconnecting = true;
    enqueueOp( new BleOpDisconnect( mContext, this ) );
    doNextOp();
    // closeChrt();
    // mCallback.disconnectGatt();
    notifyStatus( ConnectionState.CONN_WAITING );
    // mDisconnecting = false;
  }

  /** connect the GATT
   */
  void doConnectGatt()
  {
    TDLog.v( "SAP comm: do connect GATT");
    notifyStatus( ConnectionState.CONN_WAITING );
    enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) );
    doNextOp();
  }

  /** set the "connected" status
   * @param is_connected   whether the status is "connected"
   */
  void connected( boolean is_connected )
  {
    TDLog.v( "SAP comm: connected ... " + is_connected );
    mBTConnected = is_connected;
    if (is_connected ) {
      notifyStatus( ConnectionState.CONN_CONNECTED );
    } else {
      // TODO
    }
  }

  // ---------------------------------------------------------------
  // BleComm interface

  /** react to notification of becoming connected 
   */
  public void connected() 
  { 
    TDLog.v( "SAP comm: connected ...");
    connected( true );
  }

  /** re-connect to the SAP
   */
  void reconnectDevice()
  {
    TDLog.v( "SAP comm: reconnect ...");
    doDisconnectGatt();
    doConnectGatt();
    // mCallback.connectGatt( mContext, mRemoteBtDevice );
  }

  /** read a packet from SAP
   * @note THIS IS NOT USED
   */
  private boolean readSapPacket( )
  { 
    TDLog.v( "SAP comm: reading packet");
    // BluetoothGattService srv = mGatt.getService( mServiceUUID );
    // BluetoothGattCharacteristic chrt = srv.getCharacteristic( mReadUUID );
    // return mGatt.readCharacteristic( chrt );
    enqueueOp( new BleOpChrtRead( mContext, this, mServiceUUID, mReadUUID ) );
    doNextOp();
    return true;
    // return mCallback.readCharacteristic( );
  }
    
  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  /** connect to the SAP device
   * @param address   SAP address
   * @param lister    data lister
   * @param data_type expected type of data
   * @param timeout   ...
   * @return ...
   */
  @Override
  public boolean connectDevice( String address, ListerHandler lister, int data_type, int timeout )
  {
    TDLog.v( "SAP comm: connect device (continuous data download)");
    mLister = lister;
    mDataType = data_type;
    mConnectionMode = 1;
    mNrReadPackets = 0;
    connectSapDevice( TDInstance.getDeviceA(), mApp, mDataType );
    return true;
  }

  /** disconnect from the SAP device
   */
  @Override
  public boolean disconnectDevice() 
  {
    TDLog.v( "SAP comm: disconnect device");
    if ( mDisconnecting ) return true;
    if ( ! mBTConnected ) return true;
    mDisconnecting = true;
    mConnectionMode = -1;
    mBTConnected = false;
    closeChrt();
    mCallback.disconnectCloseGatt();
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    mDisconnecting = false;
    mLister = null; // ADDED 20220914
    return true;
  }

  /** mark the characteristics un-initialized
   */
  private void closeChrt()
  {
    TDLog.v( "SAP comm: close chrts");
    mWriteInitialized = false; 
    mReadInitialized  = false; 
  }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  /** download data
   * @param address    device address
   * @param lister     data lister
   * @param data_type  packet datatype
   * @param timeout    (unused)
   * @return always 0
   */
  @Override
  public int downloadData( String address, ListerHandler lister, int data_type, int timeout )
  {
    TDLog.v( "SAP comm: batch data download");
    mConnectionMode = 0;
    mLister = lister;
    mNrReadPackets = 0;
    connectSapDevice( TDInstance.getDeviceA(), mApp, data_type );
    // start a thread that keeps track of read packets
    // when read done stop it and return
    return 0;
  }

  // protected boolean startCommThread( int to_read, ListerHandler lister, int data_type, int timeout ) 
  // {
  //   if ( mCommThread != null ) {
  //     TDLog.e( "SAP comm start Comm Thread already running");
  //   }
  //   // TDLog.v( "SAP comm start comm thread");
  //   mCommThread = new CommThread( TopoDroidComm.COMM_GATT, to_read, lister, data_type, timeout );
  //   mCommThread.start();
  //   return true;
  // }

  // ================================================================

  // public void addService( BluetoothGattService srv );
  // public void addChrt( UUID srv_uuid, BluetoothGattCharacteristic chrt );
  // public void addDesc( UUID srv_uuid, UUID chrt_uuid, BluetoothGattDescriptor desc );

  /** try to write: ask the protocol to write
   */
  private void writeChrt( )
  {
    byte[] bytes = mSapProto.handleWrite( );
    if ( bytes != null ) {
      TDLog.v( "SAP comm: write chrt - bytes " + bytes.length );
      // mCallback.writeCharacteristic( mWriteChrt );
      mCallback.writeChrt( mServiceUUID, mWriteUUID, bytes );
    } else { // done with the buffer writing
      TDLog.v( "SAP comm: write chrt - bytes null");
    }
  }

  // -------------------------------------------------------------------------------

  /** clear the pending operation and execute the next if the queue is not empty
   */
  private void clearPending() 
  { 
    mPendingOp = null; 
    if ( ! mOps.isEmpty() ) doNextOp();
  }

  /** add a BLE operation to the queue
   * @param op   BLE operation
   * @return the length of the ops queue
   */
  private int enqueueOp( BleOperation op ) 
  {
    TDLog.v("SAP enqueue op " + op.name() );
    mOps.add( op );
    return mOps.size();
  }

  /** execute the next operation
   * if no op is available it does nothing
   */
  private void doNextOp() 
  {
    if ( mPendingOp != null ) {
      TDLog.v( "SAP comm: next op with pending not null: " + mPendingOp.name() + ", ops " + mOps.size() ); 
      return;
    }
    mPendingOp = mOps.poll();
    if ( mPendingOp != null ) {
      TDLog.v( "SAP comm: polled, exec pending " + mPendingOp.name() + ", ops " + mOps.size() );
      mPendingOp.execute();
      mPendingOp = null; //Remove the pending op after we execute.  Otherwise, the ops just stack up in the queue
    } else {
      TDLog.v( "SAP comm: polled, no pending, ops " + mOps.size() );
    }
  }
  // -------------------------------------------------------------------------------
  // BleComm interface

  /** react to a change in the MTU (empty)
   * @param mtu   MTU
   * @note from the BleCallback
   */
  public void changedMtu( int mtu ) { }

  /** react to the remote RSSI being read (empty)
   * @param rssi remote RSSI
   * @note from the BleCallback
   */
  public void readedRemoteRssi( int rssi ) { }

  /** react to the notification that a characteristic has changed
   * @param chrt   changed characteristic (either the read or the write characteristic)
   * @note from the BleCallback
   */
  public void changedChrt( BluetoothGattCharacteristic chrt )
  {
    //TODO: Implement SAP 6 handling of data
    TDLog.v( "SAP comm: changedChrt" );
    String uuid_str = chrt.getUuid().toString();
    if ( uuid_str.equals( SapConst.SAP5_CHRT_READ_UUID_STR ) || uuid_str.equals( SapConst.SAP6_CHRT_READ_UUID_STR )) {
      int res = mSapProto.handleReadNotify( chrt );
      if ( res == DataType.PACKET_DATA ) {
        if(uuid_str.equals( SapConst.SAP6_CHRT_READ_UUID_STR )) {
          byte[] ackByte = mSapProto.handleWrite(); // ACKNOWLEDGMENT
          mCallback.writeChrt( mServiceUUID, mWriteUUID, ackByte );
        }
        handleRegularPacket( res, mLister, DataType.DATA_SHOT );
      }
      // readSapPacket();
    } else if ( uuid_str.equals( SapConst.SAP5_CHRT_WRITE_UUID_STR ) ) {
      byte[] bytes = mSapProto.handleWriteNotify( chrt );
      if ( bytes != null ) {
        mCallback.writeChrt( mServiceUUID, mWriteUUID, bytes );
      }
    }
  }

  /** react to notification that the read characteristics has been read
   * @param uuid_str   characteristic short UUID (?) (unused)
   * @param bytes      array of read bytes (unused)
   * this method calls the protocol with the read bytes (a packet), and handle the packet
   * @note from the BleCallback
   */
  public void readedChrt( String uuid_str, byte[] bytes )
  {
    TDLog.v( "SAP comm: readedChrt" );
    if ( ! mReadInitialized ) { error(-1, uuid_str, "readInit"); return; }
    if ( ! uuid_str.equals( SapConst.SAP5_CHRT_READ_UUID_STR ) ) { error(-2, uuid_str, "readUUID"); return; }
    int res = mSapProto.handleRead( bytes ); 
    if ( res != DataType.PACKET_DATA ) { error(-3, uuid_str, "dataType"); return; }
    ++mNrReadPackets; // FIXME NON_ATOMIC_ON_VOLATILE
    handleRegularPacket( res, mLister, DataType.DATA_SHOT );
  }

  /** react to notification that the write characteristics has been written
   * @param uuid_str   characteristic short UUID (?) (unused)
   * @param bytes      array of written bytes (unused)
   * @note from the BleCallback
   */
  public void writtenChrt( String uuid_str, byte[] bytes )
  {
    TDLog.v( "SAP comm: written chrt ...");
    if ( ! mWriteInitialized ) { error(-4, uuid_str, "writeInit"); return; }
    writeChrt( ); // try to write again
  }

  /** react to the descriptor has been read (empty)
   * @param uuid_str       ??? UUID
   * @param uuid_chrt_str  characteristic UUID
   * @param bytes          array of read bytes
   * @note from the BleCallback
   */
  public void readedDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    TDLog.v( "SAP comm: readedDesc" );
  }

  /** react to the descriptor has been written
   * @param uuid_str       descriptor UUID
   * @param uuid_chrt_str  characteristic UUID
   * @param bytes          array of written bytes
   * @note this method marks that the SAP has been connected
   * @note from the BleCallback
   */
  public void writtenDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    TDLog.v( "SAP comm: ====== written desc " + uuid_str + " " + uuid_chrt_str );
    connected( true );
  }

  /** react to the completion of a reliable write (empty)
   * @note from the BleCallback
   */
  public void completedReliableWrite()
  {
    TDLog.v( "SAP comm: reliable write" );
  }

  /** communication disconnected
   * @note from the BleCallback
   */
  public void disconnected()
  {
    TDLog.v( "SAP comm: disconnected ...");
    clearPending();
    // if ( mDisconnecting ) return;
    mDisconnecting = false;
    mConnectionMode = -1;
    mBTConnected = false;
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
  }

  /** react to the discovery of a service
   * @param gatt  service GATT
   * @return ...
   * @note from the BleCallback
   */
  public int servicesDiscovered( BluetoothGatt gatt )
  {
    TDLog.v( "SAP comm: service discovered" );
    BluetoothGattService srv = gatt.getService( mServiceUUID );

    mReadChrt  = srv.getCharacteristic( mReadUUID );
    mWriteChrt = srv.getCharacteristic( mWriteUUID );

    // boolean write_has_write = BleUtils.isChrtRWrite( mWriteChrt.getProperties() );
    // boolean write_has_write_no_response = BleUtils.isChrtRWriteNoResp( mWriteChrt.getProperties() );
    // TDLog.v( "SAP callback W-chrt has write " + write_has_write );

    mWriteChrt.setWriteType( BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT );
    try {
      mWriteInitialized = gatt.setCharacteristicNotification( mWriteChrt, true );
      mReadInitialized  = gatt.setCharacteristicNotification( mReadChrt,  true );
    } catch ( SecurityException e ) {
      TDLog.e("SECURITY CHRT notification " + e.getMessage() );
      // TDToast.makeBad("Security error: CHRT notification");
      // TODO closeGatt() ?
      return -1;
    }
    BluetoothGattDescriptor readDesc = mReadChrt.getDescriptor( BleUtils.CCCD_UUID );
    if ( readDesc == null ) {
      TDLog.e("SAP callback FAIL no R-desc CCCD ");
      return -1;
    }

    // boolean read_has_write  = BleUtils.isChrtPWrite( mReadChrt.getProperties() );
    // TDLog.v( "SAP callback R-chrt has write " + read_has_write );

    clearPending(); // clear "connect" op

    byte[] notify = BleUtils.getChrtPNotify( mReadChrt );
    if ( notify == null ) {
      TDLog.e("SAP callback FAIL no indicate/notify R-property ");
      return -2;
    } else {
      readDesc.setValue( notify );
      try {
        if ( ! gatt.writeDescriptor( readDesc ) ) {
          TDLog.e("SAP callback ERROR writing readDesc");
          return -3;
        }
      } catch ( SecurityException e ) {
        TDLog.e("SECURITY write descriptor " + e.getMessage() );
        // TDToast.makeBad("Security error: write descriptor");
        // TODO closeGatt() ?
        return -3;
      }
    }
    return 0;
  } 

  /** write to a characteristic
   * @param srv_uuid   service UUID
   * @param chrt_uuid  characteristic UUID
   * @param bytes      array of bytes to write
   * @return ...
   */
  public boolean writeChrt( UUID srv_uuid, UUID chrt_uuid, byte[] bytes )
  {
    TDLog.v( "SAP comm: ##### writeChrt TODO ..." );
    // return mCallback.writeCharacteristic( mWriteChrt );
    return mCallback.writeChrt( srv_uuid, chrt_uuid, bytes );
  }

  /** read from a characteristic
   * @param srv_uuid   service UUID
   * @param chrt_uuid  characteristic UUID
   * @return ...
   */
  public boolean readChrt( UUID srv_uuid, UUID chrt_uuid )
  {
    TDLog.v( "SAP comm: ##### readChrt" );
    // return mCallback.readCharacteristic( mReadChrt );
    return mCallback.readChrt( srv_uuid, chrt_uuid );
  }

  /** handle an error
   * @param status   error status code
   * @param extra    error message
   * @param what     error source
   */
  public void error( int status, String extra, String what )
  {
    TDLog.Error("SAP comm: error " + status + ": " + extra + " what: " + what );
    if ( status == 8 ) { // (timeout) is ok
      reconnectDevice();
    }
  }

  /** handle a failure error: close GATT
   * @param status   error status code
   * @param extra    failure message
   * @param what     failure source (unused)
   */
  public void failure( int status, String extra, String what )
  {
    TDLog.Error("SAP comm: failure " + status + " " + extra );
    switch ( status ) {
      case -1:
        // TDLog.e("SAP comm: FAIL no R-desc CCCD ");
        break;
      case -2:
        // TDLog.e("SAP comm: FAIL no indicate/notify R-property ");
        break;
      case -3:
        // TDLog.e("SAP comm: ERROR writing readDesc");
        break;
      default:
    }
    mCallback.closeGatt();
  }

  /** enable notify on a characteristic - it does nothing
   * @param srcUuid    service UUID (unused)
   * @param chrtUuid   characteristic UUID (unused)
   * @return always true
   */
  public boolean enablePNotify( UUID srcUuid, UUID chrtUuid )
  {
    TDLog.v( "SAP comm: enable P notify");
    return true;
  }

  /** enable indicate on a characteristic - it does nothing
   * @param srcUuid    service UUID (unused)
   * @param chrtUuid   characteristic UUID (unused)
   * @return always true
   */
  public boolean enablePIndicate( UUID srcUuid, UUID chrtUuid )
  {
    TDLog.v( "SAP comm: enable P indicate");
    return true;
  }

  /** connect to the GATT
   * @param ctx     context
   * @param device  BT (SAP) device
   */
  public void connectGatt( Context ctx, BluetoothDevice device )
  {
    TDLog.v( "SAP connect Gatt" );
    closeChrt();
    mCallback.connectGatt( ctx, device );
  }

  /** disconnect from the GATT
   */
  public void disconnectGatt()
  {
    TDLog.v( "SAP comm: disconnect Gatt" );
    closeChrt();
    mCallback.disconnectGatt();
    notifyStatus( ConnectionState.CONN_WAITING );
    mDisconnecting = false;
    TDUtil.slowDown(500);
    clearPending(); // FIXME 20221028
  }

  /** request a new MTU
   * @param mtu   new value
   * @return always false
   */
  public boolean requestMtu( int mtu )
  {
    TDLog.Error( "SAP requestMtu not implemented" );
    return false;
  }

  // ----------------- SEND COMMAND -------------------------------

  /** sand a 1-byte command - FIXME_SAP6 only
   * @param cmd command byte
   * @return ...
   */
  @Override
  public boolean sendCommand( int cmd )
  {
    if ( ! isConnected() ) return false;
    byte[] command = new byte[1];
    command[0] = (byte)cmd;
    TDLog.v( "SAP6 comm send command " + cmd );
    enqueueOp( new BleOpChrtWrite( mContext, this, mServiceUUID, mWriteUUID, command ) );
    doNextOp();
    return true;
  }

}
