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
// import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TDInstance;
// import com.topodroid.TDX.DataDownloader;
import com.topodroid.TDX.TopoDroidApp;
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

import android.os.Handler;
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

  private ConcurrentLinkedQueue< BleOperation > mOps;
  private Context         mContext;
  private String          mRemoteAddress;
  private BluetoothDevice mRemoteBtDevice;
  private BleCallback mCallback;
  private SapProtocol mSapProto;
  // private int mDataType = DataType.DATA_SHOT;
  private int mDataType;

  BluetoothGattCharacteristic mReadChrt  = null;
  BluetoothGattCharacteristic mWriteChrt = null;
  private boolean mReadInitialized  = false;
  private boolean mWriteInitialized = false;

  BluetoothGattCharacteristic getReadChrt() { return mReadChrt; }
  BluetoothGattCharacteristic getWriteChrt() { return mWriteChrt; }


  public SapComm( TopoDroidApp app, String address, BluetoothDevice bt_device ) 
  {
    super( app );
    mRemoteAddress = address;
    mRemoteBtDevice  = bt_device;
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
  /** 
   * connection and data handling must run on a separate thread
   */

  // Device has mAddress, mModel, mName, mNickname, mType
  // the only thing that coincide with the remote_device is the address
  //
  private void connectSapDevice( Device device, Context context, int data_type ) // FIXME BLEX_DATA_TYPE
  {
    mContext = context;
    if ( mRemoteBtDevice == null ) {
      // TDToast.makeBad( R.string.ble_no_remote );
      TDLog.Error("SAP comm: error: null remote device");
    } else {
      // check that device.mAddress.equals( mRemoteBtDevice.getAddress() 
      // TDLog.v( "SAP comm: connect remote addr " + mRemoteBtDevice.getAddress() + " " + device.mAddress );
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

  private boolean mDisconnecting = false;

  // disconnect the GATT

  void doDisconnectGatt()
  {
    // TDLog.v( "SAP comm: do disconnect GATT - disconnecting " + mDisconnecting );
    if ( mDisconnecting ) return;
    mDisconnecting = true;
    enqueueOp( new BleOpDisconnect( mContext, this ) );
    doNextOp();
    // closeChrt();
    // mCallback.disconnectGatt();
    notifyStatus( ConnectionState.CONN_WAITING );
    // mDisconnecting = false;
  }

  void doConnectGatt()
  {
    // TDLog.v( "SAP comm: do connect GATT");
    notifyStatus( ConnectionState.CONN_WAITING );
    enqueueOp( new BleOpConnect( mContext, this, mRemoteBtDevice ) );
    doNextOp();
  }

  void connected( boolean is_connected )
  {
    // TDLog.v( "SAP comm: connected ...");
    mBTConnected = is_connected;
    if (is_connected ) {
      notifyStatus( ConnectionState.CONN_CONNECTED );
    } else {
      // TODO
    }
  }

  // BleComm interface
  public void connected() { connected( true ); }

  void reconnectDevice()
  {
    // TDLog.v( "SAP comm: reconnect ...");
    doDisconnectGatt();
    doConnectGatt();
    // mCallback.connectGatt( mContext, mRemoteBtDevice );
  }

  private boolean readSapPacket( )
  { 
    // TDLog.v( "SAP comm: reading packet");
    // BluetoothGattService srv = mGatt.getService( SapConst.SAP5_SERVICE_UUID );
    // BluetoothGattCharacteristic chrt = srv.getCharacteristic( SapConst.SAP5_CHRT_READ_UUID );
    // return mGatt.readCharacteristic( chrt );

    enqueueOp( new BleOpChrtRead( mContext, this, SapConst.SAP5_SERVICE_UUID, SapConst.SAP5_CHRT_READ_UUID ) );
    doNextOp();
    return true;
    // return mCallback.readCharacteristic( );
  }
    
  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD
  private int mConnectionMode = -1;

  @Override
  public boolean connectDevice( String address, Handler /* ILister */ lister, int data_type )
  {
    // TDLog.v( "SAP comm: connect device (continuous data download)");
    mLister = lister;
    mDataType = data_type;
    mConnectionMode = 1;
    mNrPacketsRead = 0;
    connectSapDevice( TDInstance.getDeviceA(), mApp, mDataType );
    return true;
  }

  @Override
  public boolean disconnectDevice() 
  {
    // TDLog.v( "SAP comm: disconnect device");
    if ( mDisconnecting ) return true;
    if ( ! mBTConnected ) return true;
    mDisconnecting = true;
    mConnectionMode = -1;
    mBTConnected = false;
    closeChrt();
    mCallback.disconnectCloseGatt();
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    mDisconnecting = false;
    return true;
  }

  private void closeChrt()
  {
    mWriteInitialized = false; 
    mReadInitialized  = false; 
  }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD
  private Handler mLister;

  /** download data
   * @param address    device address
   * @param lister     data lister
   * @param data_type  packet datatype
   * @return always 0
   */
  public int downloadData( String address, Handler /* ILister */ lister, int data_type )
  {
    // TDLog.v( "SAP comm: batch data download");
    mConnectionMode = 0;
    mLister = lister;
    mNrPacketsRead = 0;
    connectSapDevice( TDInstance.getDeviceA(), mApp, data_type );
    // start a thread that keeps track of read packets
    // when read done stop it and return
    return 0;
  }

  // protected boolean startCommThread( int to_read, Handler /* ILister */ lister, int data_type ) 
  // {
  //   if ( mCommThread != null ) {
  //     TDLog.Error( "SAP comm start Comm Thread already running");
  //   }
  //   // TDLog.v( "SAP comm start comm thread");
  //   mCommThread = new CommThread( TopoDroidComm.COMM_GATT, mProtocol, to_read, lister, data_type );
  //   mCommThread.start();
  //   return true;
  // }

  // ================================================================

  // public void addService( BluetoothGattService srv );
  // public void addChrt( UUID srv_uuid, BluetoothGattCharacteristic chrt );
  // public void addDesc( UUID srv_uuid, UUID chrt_uuid, BluetoothGattDescriptor desc );

  private void writeChrt( )
  {
    byte[] bytes = mSapProto.handleWrite( );
    if ( bytes != null ) {
      // mCallback.writeCharacteristic( mWriteChrt );
      mCallback.writeChrt( SapConst.SAP5_SERVICE_UUID, SapConst.SAP5_CHRT_WRITE_UUID, bytes );
    } // else // done with the buffer writing
  }

  // -------------------------------------------------------------------------------
  private BleOperation mPendingOp = null;

  private void clearPending() 
  { 
    mPendingOp = null; 
    if ( ! mOps.isEmpty() ) doNextOp();
  }

  // @return the length of the ops queue
  private int enqueueOp( BleOperation op ) 
  {
    mOps.add( op );
    return mOps.size();
  }

  private void doNextOp() 
  {
    if ( mPendingOp != null ) {
      // TDLog.v( "SAP comm: next op with pending not null, ops " + mOps.size() ); 
      return;
    }
    mPendingOp = mOps.poll();
    // TDLog.v( "SAP comm: polled, ops " + mOps.size() );
    if ( mPendingOp != null ) {
      mPendingOp.execute();
    } 
  }
  // -------------------------------------------------------------------------------
  // BleComm interface

  public void changedMtu( int mtu ) { }

  public void readedRemoteRssi( int rssi ) { }


  public void changedChrt( BluetoothGattCharacteristic chrt )
  {
    // TDLog.v( "SAP comm: changedChrt" );
    String uuid_str = chrt.getUuid().toString();
    if ( uuid_str.equals( SapConst.SAP5_CHRT_READ_UUID_STR ) ) {
      int res = mSapProto.handleReadNotify( chrt );
      if ( res == DataType.PACKET_DATA ) {
        // mSapProto.handleWrite( mWriteChrt ); // ACKNOWLEDGMENT
        handleRegularPacket( res, mLister, DataType.DATA_SHOT );
      }
      // readSapPacket();
    } else if ( uuid_str.equals( SapConst.SAP5_CHRT_WRITE_UUID_STR ) ) {
      byte[] bytes = mSapProto.handleWriteNotify( chrt );
      if ( bytes != null ) {
        mCallback.writeChrt( SapConst.SAP5_SERVICE_UUID, SapConst.SAP5_CHRT_WRITE_UUID, bytes );
      }
    }
  }

  // @param uuid_str short UUID string
  public void readedChrt( String uuid_str, byte[] bytes )
  {
    // TDLog.v( "SAP comm: readedChrt" );
    if ( ! mReadInitialized ) { error(-1, uuid_str); return; }
    if ( ! uuid_str.equals( SapConst.SAP5_CHRT_READ_UUID_STR ) ) { error(-2, uuid_str); return; }
    int res = mSapProto.handleRead( bytes ); 
    if ( res != DataType.PACKET_DATA ) { error(-3, uuid_str); return; }
    ++ mNrPacketsRead; // FIXME NON_ATOMIC_ON_VOLATILE
    handleRegularPacket( res, mLister, DataType.DATA_SHOT );
  }

  public void writtenChrt( String uuid_str, byte[] bytes )
  {
    // TDLog.v( "SAP comm: written chrt ...");
    if ( ! mWriteInitialized ) { error(-4, uuid_str); return; }
    writeChrt( ); // try to write again
  }

  public void readedDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    TDLog.Log( TDLog.LOG_BT, "SAP comm: readedDesc" );
  }
  public void writtenDesc( String uuid_str, String uuid_chrt_str, byte[] bytes )
  {
    // TDLog.v( "SAP comm: ====== written desc " + uuid_str + " " + uuid_chrt_str );
    connected( true );
  }

  public void completedReliableWrite()
  {
    TDLog.Log( TDLog.LOG_BT, "SAP comm: reliable write" );
  }

  public void disconnected()
  {
    // TDLog.v( "SAP comm: disconnected ...");
    // if ( mDisconnecting ) return;
    mDisconnecting = false;
    mConnectionMode = -1;
    mBTConnected = false;
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
  }

  public int servicesDiscovered( BluetoothGatt gatt )
  {
    // TDLog.v( "SAP comm: service discovered" );
    BluetoothGattService srv = gatt.getService( SapConst.SAP5_SERVICE_UUID );

    mReadChrt  = srv.getCharacteristic( SapConst.SAP5_CHRT_READ_UUID );
    mWriteChrt = srv.getCharacteristic( SapConst.SAP5_CHRT_WRITE_UUID );

    // boolean write_has_write = BleUtils.isChrtRWrite( mWriteChrt.getProperties() );
    // boolean write_has_write_no_response = BleUtils.isChrtRWriteNoResp( mWriteChrt.getProperties() );
    // TDLog.v( "SAP callback W-chrt has write " + write_has_write );

    mWriteChrt.setWriteType( BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT );
    try {
      mWriteInitialized = gatt.setCharacteristicNotification(mWriteChrt, true);
      mReadInitialized = gatt.setCharacteristicNotification(mReadChrt, true);
    } catch ( SecurityException e ) {
      TDLog.Error("SECURITY " + e.getMessage() );
    }
    BluetoothGattDescriptor readDesc = mReadChrt.getDescriptor( BleUtils.CCCD_UUID );
    if ( readDesc == null ) {
      TDLog.Error("SAP callback FAIL no R-desc CCCD ");
      return -1;
    }

    // boolean read_has_write  = BleUtils.isChrtPWrite( mReadChrt.getProperties() );
     // TDLog.v( "SAP callback R-chrt has write " + read_has_write );

    byte[] notify = BleUtils.getChrtPNotify( mReadChrt );
    if ( notify == null ) {
      TDLog.Error("SAP callback FAIL no indicate/notify R-property ");
      return -2;
    } else {
      readDesc.setValue( notify );
      try {
        if (!gatt.writeDescriptor(readDesc)) {
          TDLog.Error("SAP callback ERROR writing readDesc");
          return -3;
        }
      } catch ( SecurityException e ) {
        TDLog.Error("SECURITY " + e.getMessage() );
        return -3;
      }
    }
    return 0;
  } 

  public boolean writeChrt( UUID srv_uuid, UUID chrt_uuid, byte[] bytes )
  {
    // TDLog.v( "SAP comm: ##### writeChrt TODO ..." );
    // return mCallback.writeCharacteristic( mWriteChrt );
    return mCallback.writeChrt( srv_uuid, chrt_uuid, bytes );
  }

  public boolean readChrt( UUID srv_uuid, UUID chrt_uuid )
  {
    // TDLog.v( "SAP comm: ##### readChrt" );
    // return mCallback.readCharacteristic( mReadChrt );
    return mCallback.readChrt( srv_uuid, chrt_uuid );
  }

  public void error( int status, String extra )
  {
    TDLog.Error("SAP comm: error " + status + " " + extra );
  }

  public void failure( int status, String extra )
  {
    TDLog.Error("SAP comm: failure " + status + " " + extra );
    switch ( status ) {
      case -1:
        // TDLog.Error("SAP comm: FAIL no R-desc CCCD ");
        break;
      case -2:
        // TDLog.Error("SAP comm: FAIL no indicate/notify R-property ");
        break;
      case -3:
        // TDLog.Error("SAP comm: ERROR writing readDesc");
        break;
      default:
    }
  }

  public boolean enablePNotify( UUID srcUuid, UUID chrtUuid )
  {
    TDLog.Log( TDLog.LOG_BT, "SAP comm: enable P notify");
    return true;
  }

  public boolean enablePIndicate( UUID srcUuid, UUID chrtUuid )
  {
    TDLog.Log( TDLog.LOG_BT, "SAP comm: enable P indicate");
    return true;
  }

  public void connectGatt( Context ctx, BluetoothDevice device )
  {
    // TDLog.Log( TDLog.LOG_BT, "SAP connect Gatt" );
    closeChrt();
    mCallback.connectGatt( ctx, device );
  }

  public void disconnectGatt()
  {
    // TDLog.Log( TDLog.LOG_BT, "SAP comm: disconnect Gatt" );
    closeChrt();
    mCallback.disconnectGatt();
    notifyStatus( ConnectionState.CONN_WAITING );
    mDisconnecting = false;
  }

  public void notifyStatus( int status )
  {
    mApp.notifyStatus( status );
  }

}
