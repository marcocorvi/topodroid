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
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.dev.sap;

// import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.DataDownloader;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.dev.bric.BleUtils;

import android.util.Log;

import android.os.Handler;
import android.os.Looper;
import android.os.Build;
import android.content.Context;

import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattDescriptor;
// import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;

// -----------------------------------------------------------------------------
public class SapComm extends TopoDroidComm
{
  // -----------------------------------------------
  // BluetoothAdapter   mAdapter;
  // private BluetoothGatt mGatt = null;
  // BluetoothGattCharacteristic mWriteChrt;

  private String          mRemoteAddress;
  private BluetoothDevice mRemoteBtDevice;
  private SapCallback mCallback;
  private SapProtocol mSapProto;
  // private int mDataType = DataType.DATA_SHOT;

  public SapComm( TopoDroidApp app, String address, BluetoothDevice bt_device ) 
  {
    super( app );
    mRemoteAddress = address;
    mRemoteBtDevice  = bt_device;
    Log.v("DistoX-BLE-S", "SAP comm cstr, addr " + address );
  }

  // void setRemoteDevice( BluetoothDevice device ) 
  // { 
  //   Log.v("DistoX-BLE-S", "SAP comm set remote " + device.getAddress() );
  //   mRemoteBtDevice = device;
  // }

  // -------------------------------------------------------------
  /** 
   * connection and data handling must run on a separate thread
   */
  private void setConnected( boolean is_connected ) 
  { 
    mBTConnected = is_connected;
    // if ( ! is_connected ) {
    //   TDUtil.yieldDown( 500 );
    //   Log.v("DistoX-BLE-S", "SAP comm try re-connect: does not do anything");
    //   // connectDevice( TDInstance.deviceAddress(), mLister, DataType.DATA_SHOT );
    // } else {
    //   Log.v("DistoX-BLE-S", "SAP comm set connected: does not do anything " + is_connected );
    //   // notify ???
    // }
  }

  // Device has mAddress, mModel, mName, mNickname, mType
  // the only thing that coincide with the remote_device is the address
  //
  private void connectSapDevice( Device device, Context context, int data_type ) // FIXME BLEX_DATA_TYPE
  {
    if ( mRemoteBtDevice == null ) {
      // TDToast.makeBad( R.string.ble_no_remote );
      Log.v("DistoX-BLE-S", "SAP comm error: null remote device");
    } else {
      // check that device.mAddress.equals( mRemoteBtDevice.getAddress() 
      // Log.v("DistoX-BLE-S", "SAP comm connect remote addr " + mRemoteBtDevice.getAddress() + " " + device.mAddress );
      mSapProto = new SapProtocol( this, device, context );
      mCallback = new SapCallback( this );
      mProtocol = mSapProto;
      mCallback.connectGatt( context, mRemoteBtDevice );
    }
  }

  void changedChrt( BluetoothGattCharacteristic chrt )
  {
    String uuid_str = chrt.getUuid().toString();
    if ( uuid_str.equals( SapConst.SAP5_CHRT_READ_UUID_STR ) ) {
      int res = mSapProto.handleNotify( chrt, true ); // true = READ
      if ( res == DataType.PACKET_DATA ) {
        // mSapProto.handleWrite( mWriteChrt ); // ACKNOWLEDGMENT
        handleRegularPacket( res, mLister, DataType.DATA_SHOT );
      }
      // readSapPacket();
    } else if ( uuid_str.equals( SapConst.SAP5_CHRT_WRITE_UUID_STR ) ) {
      mSapProto.handleNotify( chrt, false ); // false = WRITE
    }
  }

  void readedChrt( String uuid_str, byte[] bytes )
  {
    int res = mSapProto.handleRead( bytes ); // FIXME bytes
    if ( res == DataType.PACKET_DATA ) {
      ++ mNrPacketsRead;
      handleRegularPacket( res, mLister, DataType.DATA_SHOT );
    }
  }

  void writtenChrt( String uuid_str, byte[] bytes )
  {
    Log.v("DistoX-BLE", "SAP comm written chrt ...");
    writeChrt( ); // try to write again
  }

  void writeChrt( )
  {
    BluetoothGattCharacteristic chrt = mCallback.getWriteChrt();
    if ( mSapProto.handleWrite( chrt ) > 0 ) {
      mCallback.writeCharacteristic( chrt );
    } else {
      // done with the buffer writing
    }
  }

  private boolean mDisconnecting = false;

  // disconnect the GATT
  void disconnected()
  {
    Log.v("DistoX-BLE-S", "SAP comm disconnected ...");
    if ( mDisconnecting ) return;
    mDisconnecting = true;
    mConnectionMode = -1;
    setConnected( false );
    mCallback.disconnectGatt();
    notifyStatus( ConnectionState.CONN_WAITING );
    mDisconnecting = false;
  }

  void connected( boolean is_connected )
  {
    Log.v("DistoX-BLE-S", "SAP comm connected ...");
    setConnected( is_connected );
    if (is_connected ) {
      notifyStatus( ConnectionState.CONN_CONNECTED );
    } else {
      // TODO
    }
  }

  private boolean readSapPacket( )
  { 
    Log.v("DistoX-BLE-S", "SAP comm reading packet");
    // BluetoothGattService srv = mGatt.getService( SapConst.SAP5_SERVICE_UUID );
    // BluetoothGattCharacteristic chrt = srv.getCharacteristic( SapConst.SAP5_CHAR_READ_UUID );
    // return mGatt.readCharacteristic( chrt );
    return mCallback.readCharacteristic( );
  }

  void error( int status )
  {
    Log.v("DistoX-BLE-S", "SAP comm error " + status );
  }

  void failure( int status )
  {
    Log.v("DistoX-BLE-S", "SAP comm failure " + status );
    switch ( status ) {
      case -1:
        // Log.v("DistoX-BLE-S", "SAP comm FAIL no R-desc CCCD ");
        break;
      case -2:
        // Log.v("DistoX-BLE-S", "SAP comm FAIL no indicate/notify R-property ");
        break;
      case -3:
        // Log.v("DistoX-BLE-S", "SAP comm ERROR writing readDesc");
        break;
      default:
    }
  }
    
  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD
  private int mConnectionMode = -1;

  @Override
  public boolean connectDevice( String address, Handler /* ILister */ lister, int data_type )
  {
    Log.v("DistoX-BLE-S", "SAP comm connect device (continuous data download)");
    mLister = lister;
    mConnectionMode = 1;
    mNrPacketsRead = 0;
    connectSapDevice( TDInstance.deviceA, mApp, data_type );
    return true;
  }

  @Override
  public void disconnectDevice() 
  {
    Log.v("DistoX-BLE-S", "SAP comm disconnect device");
    if ( mDisconnecting ) return;
    if ( ! mBTConnected ) return;
    mDisconnecting = true;
    mConnectionMode = -1;
    setConnected( false );
    mCallback.closeGatt();
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    mDisconnecting = false;
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
    Log.v("DistoX-BLE-S", "SAP comm batch data downlaod");
    mConnectionMode = 0;
    mLister = lister;
    mNrPacketsRead = 0;
    connectSapDevice( TDInstance.deviceA, mApp, data_type );
    // start a thread that keeps track of read packets
    // when read done stop it and return
    return 0;
  }

  // protected boolean startCommThread( int to_read, Handler /* ILister */ lister, int data_type ) 
  // {
  //   if ( mCommThread != null ) {
  //     TDLog.Error( "SAP comm start Comm Thread already running");
  //   }
  //   // Log.v("DistoX-BLE-S", "SAP comm start comm thread");
  //   mCommThread = new CommThread( TopoDroidComm.COMM_GATT, mProtocol, to_read, lister, data_type );
  //   mCommThread.start();
  //   return true;
  // }

  void notifyStatus( int status )
  {
    mApp.notifyStatus( status );
  }

}
