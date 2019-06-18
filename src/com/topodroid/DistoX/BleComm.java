/* @file BleComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid BLE devices client communication
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.DistoX;

import android.util.Log;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;

// -----------------------------------------------------------------------------
class BleComm extends TopoDroidComm
{
  // -----------------------------------------------
  private boolean mWriteInitialized = false;
  private boolean mReadInitialized = false;

  // BluetoothAdapter   mAdapter;
  BluetoothGatt mGatt = null;
  BluetoothGattCharacteristic mWriteChrt;
  BluetoothGattCharacteristic mReadChrt;

  private String          mRemoteAddress;
  private BluetoothDevice mRemoteDevice;


  BleComm( TopoDroidApp app, String address, BluetoothDevice bt_device ) 
  {
    super( app );
    mRemoteAddress = address;
    mRemoteDevice  = bt_device;
    // Log.v("DistoXBLE", "new BLE comm " + address );
  }

  // void setRemoteDevice( BluetoothDevice device ) 
  // { 
  //   Log.v("DistoXBLE", "BLE comm set remote " + device.getAddress() );
  //   mRemoteDevice = device;
  // }

  // -------------------------------------------------------------
  /** 
   * connection and data handling must run on a separate thread
   */
  void setConnected( boolean connected ) { mBTConnected = connected; }

  void connectBleDevice( Device device, Context context )
  {
    if ( mRemoteDevice == null ) {
      TDToast.makeBad( R.string.ble_no_remote );
    } else {
      // Log.v("DistoXBLE", "BLE comm connect remote " + mRemoteDevice.getAddress() );
      BleProtocol protocol  = new BleProtocol( this, device, context );
      BleGattCallback callback = new BleGattCallback( protocol );
      mProtocol = protocol;
      mGatt = mRemoteDevice.connectGatt( context, false, callback ); // true: autoconnect as soon as the device becomes available
    }
  }

  void disconnectBleGatt()
  {
    mConnectionMode = -1;
    setConnected( false );
    mWriteInitialized = false; 
    mReadInitialized  = false; 
    if ( mGatt != null ) {
      mGatt.disconnect();
      mGatt.close();
    }
    mGatt = null;
  }

  // -------------------------------------------------------------
  private class BleGattCallback extends BluetoothGattCallback
  {
    BleProtocol mProto;

    BleGattCallback( BleProtocol proto ) { mProto = proto; }

    @Override
    public void onConnectionStateChange( BluetoothGatt gatt, int status, int state )
    {
      super.onConnectionStateChange( gatt, status, state );
      if ( status == BluetoothGatt.GATT_FAILURE ) {
        TDToast.makeBad( R.string.ble_gatt_failure );
        disconnectBleGatt();
      } else if ( status != BluetoothGatt.GATT_SUCCESS ) {
        disconnectBleGatt();
      } else {
        if ( state == BluetoothProfile.STATE_CONNECTED ) {
          // Log.v("DistoXBLE", "connected GATT");
          gatt.discoverServices();
          setConnected( true );
        } else if ( state == BluetoothProfile.STATE_DISCONNECTED ) {
          // Log.v("DistoXBLE", "disconnected GATT");
          disconnectBleGatt();
        }
      }
    }

    @Override
    public void onServicesDiscovered( BluetoothGatt gatt, int status )
    {
      super.onServicesDiscovered( gatt, status );
      if ( status != BluetoothGatt.GATT_SUCCESS ) {
        // return;
      } else {
        // Log.v("DistoXBLE", "comm service discovered ok");
        BluetoothGattService srv = gatt.getService( BleConst.BLE_SERVICE_UUID );

        // mWriteChrt = srv.getCharacteristic( BleConst.BLE_CHAR_WRITE_UUID );
        // mWriteChrt.setWriteType( BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT );
        // mWriteInitialized = gatt.setCharacteristicNotification( mWriteChrt, true );

        mReadChrt = srv.getCharacteristic( BleConst.BLE_CHAR_READ_UUID );
        mReadInitialized = gatt.setCharacteristicNotification( mReadChrt, true );

        readPacket();
      }
    }

    // @Override
    // public void onCharacteristicWrite( BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status )
    // {
    //   super.onCharacteristicWrite( gatt, chrt, status );
    //   if ( mWriteInitialized ) {
    //     int res = mProto.handleWrite( chrt );
    //   }
    // }

    @Override
    public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status )
    {
      super.onCharacteristicRead( gatt, chrt, status );
      long wait = 0x40; // msec
      // Log.v("DistoXBLE", "comm chrt read. init " + mReadInitialized + " status " + status );
      if ( mReadInitialized ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
          int res = mProto.handleRead( chrt );
          if ( res == 1 ) {
            ++ nReadPackets;
            handleRegularPacket( res, mLister );
          }
          readPacket();
        } else { 
          if ( mConnectionMode != 1 ) {
            disconnectBleGatt();
          } else if ( isConnected()  ) {
            if ( wait < 0x0400 ) wait <<= 1; // max 1 sec
            Handler handler = new Handler( Looper.getMainLooper() );
            handler.postDelayed( new Runnable() { public void run() { readPacket(); } }, wait );
          }
        }
      }
    }

    @Override
    public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic chrt )
    {
      super.onCharacteristicChanged( gatt, chrt );
      mProto.handleNotify( chrt );
    }

  }

  boolean readPacket()
  { 
    // Log.v("DistoXBLE", "comm read packet");
    if ( ! mBTConnected || ! mReadInitialized ) return false;
    // BluetoothGattService srv = mGatt.getService( BleConst.BLE_SERVICE_UUID );
    // BluetoothGattCharacteristic chrt = srv.getCharacteristic( BleConst.BLE_CHAR_READ_UUID );
    // return mGatt.readCharacteristic( chrt );
    return mGatt.readCharacteristic( mReadChrt );
  }
    
  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD
  int mConnectionMode = -1;

  boolean connectDevice( String address, Handler /* ILister */ lister )
  {
    // Log.v("DistoXBLE", "comm connect device");
    mLister = lister;
    mConnectionMode = 1;
    nReadPackets = 0;
    connectBleDevice( TDInstance.device, mApp );
    return true;
  }

  void disconnectDevice() 
  {
    disconnectBleGatt();
  }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD
  Handler mLister;

  int downloadData( String address, Handler /* ILister */ lister )
  {
    // Log.v("DistoXBLE", "comm data downlaod");
    mConnectionMode = 0;
    mLister = lister;
    nReadPackets = 0;
    connectBleDevice( TDInstance.device, mApp );
    // nReadPackets = 0;
    // start a thread that keeps track of read packets
    // when read done stop it and return
    return 0;
  }

  // protected boolean startCommThread( int to_read, Handler /* ILister */ lister ) 
  // {
  //   if ( mCommThread != null ) {
  //     TDLog.Error( "start Comm Thread already running");
  //   }
  //   // Log.v("DistoXBLE", "comm start comm thread");
  //   mCommThread = new CommThread( TopoDroidComm.COMM_GATT, mProtocol, to_read, lister );
  //   mCommThread.start();
  //   return true;
  // }

}
