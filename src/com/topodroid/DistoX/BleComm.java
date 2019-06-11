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

// import android.util.Log;

import java.util.UUID;

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
  static final String BLE_CHAR_WRITE_STRING = "...";
  static final String BLE_DESC_WRITE_STRING = "...";
  static final String BLE_CHAR_READ_STRING  = "...";
  static final String BLE_DESC_READ_STRING  = "...";

  static UUID BLE_CHAR_WRITE_UUID = UUID.fromString( BLE_CHAR_WRITE_STRING );
  static UUID BLE_DESC_WRITE_UUID = UUID.fromString( BLE_DESC_WRITE_STRING );
  static UUID BLE_CHAR_READ_UUID  = UUID.fromString( BLE_CHAR_READ_STRING );
  static UUID BLE_DESC_READ_UUID  = UUID.fromString( BLE_DESC_READ_STRING );
  
  // -----------------------------------------------
  private boolean mWriteInitialized = false;
  private boolean mReadInitialized = false;

  // BluetoothAdapter   mAdapter;
  BluetoothGatt mGatt = null;
  BluetoothGattCharacteristic mWriteChrt;
  BluetoothGattCharacteristic mReadChrt;

  private String          mRemoteAddress;
  private BluetoothDevice mRemoteDevice;
  private DeviceActivity  mParent;
  private BleProtocol     mBleProtocol;


  BleComm( TopoDroidApp app, DeviceActivity parent, String address ) 
  {
    super( app );
    mParent = parent;
    mRemoteAddress = address;
    mRemoteDevice  = null;
    mBleProtocol   = null;
  }

  void setRemoteDevice( BluetoothDevice device ) { mRemoteDevice = device; }

  /**
   * scan should not run on UI thread ?
   */
  void scanBleDevices( )
  {
    BleScanner scanner = new BleScanner( this );
    scanner.startScan( mRemoteAddress );
  }

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
      mBleProtocol  = new BleProtocol( device, context );
      BleGattCallback callback = new BleGattCallback( mBleProtocol );
      mGatt = mRemoteDevice.connectGatt( context, true, callback ); // true: autoconnect as soon as the device becomes available
    }
  }

  void disconnectBleGatt()
  {
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
          gatt.discoverServices();
          setConnected( true );
        } else if ( state == BluetoothProfile.STATE_DISCONNECTED ) {
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
        BluetoothGattService srv = gatt.getService( SERVICE_UUID );
        mWriteChrt = srv.getCharacteristic( BLE_CHAR_WRITE_UUID );
        mWriteChrt.setWriteType( BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT );
        mWriteInitialized = gatt.setCharacteristicNotification( mWriteChrt, true );

        mReadChrt = srv.getCharacteristic( BLE_CHAR_READ_UUID );
        mReadInitialized = gatt.setCharacteristicNotification( mReadChrt, true );
      }
    }

    @Override
    public void onCharacteristicWrite( BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status )
    {
      super.onCharacteristicWrite( gatt, chrt, status );
      if ( mWriteInitialized ) {
        mProto.handleWrite( chrt );
      }
    }

    @Override
    public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status )
    {
      super.onCharacteristicRead( gatt, chrt, status );
      if ( mReadInitialized ) {
        mProto.handleRead( chrt );
      }
    }

    @Override
    public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic chrt )
    {
      super.onCharacteristicChanged( gatt, chrt );
      mProto.handleNotify( chrt );
    }

  }

}
