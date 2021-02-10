/* @file SapCallback.java
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

import com.topodroid.dev.ble.BleCallback;
import com.topodroid.dev.ble.BleUtils;
import com.topodroid.dev.ConnectionState;

import android.content.Context; 
import android.os.Build;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;

import android.util.Log;

import java.util.UUID;

class SapCallback extends BluetoothGattCallback
{
  SapComm mSapComm; // TO BECOME BleComm mComm inherited
  // BluetoothGatt mGatt; // inherited
  static final boolean mAutoConnect = true; // this is false in BleCallback
  private BluetoothGatt mGatt;

  SapCallback( SapComm comm ) 
  { 
    mSapComm  = comm;
  }

  @Override
  public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic chrt )
  {
    mSapComm.changedChrt( chrt );
  }

  @Override
  public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status )
  {
    if ( status != BluetoothGatt.GATT_SUCCESS ) {
      Log.v("DistoX-BLE", "SAP callback: FAIL on char read");
      mSapComm.error( status );
    } else {
      String uuid_str = chrt.getUuid().toString();
      mSapComm.readedChrt( uuid_str, chrt.getValue() );
    }
  }
      
  @Override
  public void onCharacteristicWrite( BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status )
  {
    if ( status != BluetoothGatt.GATT_SUCCESS ) {
      Log.v("DistoX-BLE", "SAP callback: FAIL on char write");
      mSapComm.error( status );
    } else {
      String uuid_str = chrt.getUuid().toString();
      mSapComm.writtenChrt( uuid_str, chrt.getValue() );
    }
  }

  @Override
  public void onConnectionStateChange( BluetoothGatt gatt, int status, int state )
  {
    // super.onConnectionStateChange( gatt, status, state );
    if ( BleCallback.isSuccess( status, "onConnectionStateChange" ) ) {
      if ( state == BluetoothProfile.STATE_CONNECTED ) {
        // Log.v("DistoX-BLE", "SAP callback: conn state changed: connected");
        // mGatt = gatt;
        gatt.discoverServices();
        // if ( ! gatt.discoverServices() ) {
        //   // Log.v("DistoX-BLE", "SAP callback FAIL service discovery");
        //   mSapComm.notifyStatus( ConnectionState.CONN_DISCONNECTED );
        // } else {
        //   mSapComm.notifyStatus( ConnectionState.CONN_CONNECTED );
        // }
      } else if ( state == BluetoothProfile.STATE_DISCONNECTED ) {
        // Log.v("DistoX-BLE", "SAP callback conn state changed: disconnected");
        mSapComm.disconnected();
      }
    } else {
      if ( status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION 
        || status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION 
        || status == BleCallback.CONNECTION_TIMEOUT 
        || status == BleCallback.CONNECTION_133 ) {
        Log.v("DistoX-BLE", "SAP callback FAIL state changed - status " + status + " reconnect ");
        // mSapComm.notifyStatus( ConnectionState.CONN_WAITING );
        mSapComm.reconnectDevice();
      } else { // status == BluetoothGatt.GATT_FAILURE or whatever
        Log.v("DistoX-BLE", "SAP callback FAIL state changed - status " + status + " disconnect " );
        mSapComm.notifyStatus( ConnectionState.CONN_DISCONNECTED );
        mSapComm.disconnected();
      }
    } 
  }

  @Override
  public void onServicesDiscovered( BluetoothGatt gatt, int status )
  {
    // super.onServicesDiscovered( gatt, status );
    if ( status == BluetoothGatt.GATT_SUCCESS ) {
      // Log.v("DistoX-BLE", "SAP callback service discovered ok" );
      int ret = mSapComm.servicesDiscovered( gatt );
      if ( ret == 0 ) {
        mGatt = gatt;
      } else {
        if ( gatt != null ) gatt.close();
        mGatt = null;
        mSapComm.failure( ret );
      }
    } else {
      Log.v("DistoX-BLE", "SAP callback FAIL service discover");
      mSapComm.failure( status );
    }
  }

  @Override
  public void onDescriptorRead( BluetoothGatt gatt, BluetoothGattDescriptor desc, int status ) 
  {
    Log.v("DistoX-BLE", "SAP callback: onDescriptorRead " + desc.getUuid() + " " + status );
  }

  @Override
  public void onDescriptorWrite( BluetoothGatt gatt, BluetoothGattDescriptor desc, int status ) 
  {
    if ( status != BluetoothGatt.GATT_SUCCESS ) {
      Log.v("DistoX-BLE", "SAP callback FAIL on descriptor write");
      mSapComm.error( status );
    } else {
      String uuid_str = desc.getUuid().toString();
      String uuid_chrt_str = desc.getCharacteristic().getUuid().toString();
      mSapComm.writtenDesc( uuid_str, uuid_chrt_str, desc.getValue() );
    }
  }

  @Override
  public void onMtuChanged(BluetoothGatt gatt, int mtu, int status)
  { 
    Log.v("DistoX-BLE", "SAP callback: onMtuChanged " + status );
  }

  @Override
  public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
  { 
    Log.v("DistoX-BLE", "SAP callback: onReadRemoteRssi " + status );
  }

  @Override
  public void onReliableWriteCompleted(BluetoothGatt gatt, int status)
  { 
    Log.v("DistoX-BLE", "SAP callback: onReliableWriteCompleted " + status );
  }

  // NOT USED
  void disconnectAndCloseGatt()
  { 
    if ( mGatt != null ) {
      // mGatt.disconnect();
      mGatt.close();
      mGatt = null;
    }
  }

  void connectGatt( Context ctx, BluetoothDevice device )
  {
    disconnectGatt();
    // Log.v("DistoX-BLE", "SAP callback: connect gatt");
    if ( Build.VERSION.SDK_INT < 23 ) {
      mGatt = device.connectGatt( ctx, mAutoConnect, this );
    } else {
      mGatt = device.connectGatt( ctx, mAutoConnect, this, BluetoothDevice.TRANSPORT_LE ); 
    }
  }

  void disconnectCloseGatt( )
  { 
    Log.v("DistoX-BLE", "SAP callback: close GATT");
    // mWriteInitialized = false; 
    // mReadInitialized  = false; 
    if ( mGatt != null ) {
      mGatt.disconnect();
      mGatt.close();
      mGatt = null;
    }
  }

  void disconnectGatt()
  {
    Log.v("DistoX-BLE", "SAP callback: disconnect GATT");
    // mWriteInitialized = false; 
    // mReadInitialized  = false; 
    if ( mGatt != null ) {
      // Log.v("DistoX-BLE", "SAP callback: disconnect gatt");
      mGatt.disconnect();
      // FIXME mGapp.close();
      mGatt = null;
    }
  }

  // boolean writeCharacteristic( BluetoothGattCharacteristic chrt ) 
  // {
  //   return mGatt != null && mGatt.writeCharacteristic( chrt ); 
  // }

  // boolean readCharacteristic( BluetoothGattCharacteristic chrt ) 
  // {
  //   return mGatt != null && mGatt.readCharacteristic( chrt ); 
  // }

  boolean readChrt( UUID srvUuid, UUID chrtUuid )
  {
    BluetoothGattCharacteristic chrt = getReadChrt( srvUuid, chrtUuid );
    return chrt != null && mGatt.readCharacteristic( chrt );
  }

  boolean writeChrt(  UUID srvUuid, UUID chrtUuid, byte[] bytes )
  {
    BluetoothGattCharacteristic chrt = getWriteChrt( srvUuid, chrtUuid );
    if ( chrt == null ) {
      // Log.v("DistoX-BLE", "BLE callback writeChrt null chrt ");
      return false;
    }
    int write_type = BleUtils.getChrtWriteType( chrt );
    if ( write_type < 0 ) {
      // Log.v("DistoX-BLE", "BLE callback writeChrt neg type " + write_type );
      return false;
    }
    chrt.setWriteType( write_type );
    chrt.setValue( bytes );
    return mGatt.writeCharacteristic( chrt );
  }

  // -------------------------------------------------------------------

  private BluetoothGattCharacteristic getReadChrt( UUID srvUuid, UUID chrtUuid )
  {
    if ( mGatt == null ) return null;
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) return null;
    BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
    if ( chrt == null || ! BleUtils.canChrtPRead( chrt ) ) return null;
    return chrt;
  }

  private BluetoothGattCharacteristic getWriteChrt( UUID srvUuid, UUID chrtUuid )
  {
    if ( mGatt == null ) {
      return null;
    }
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) {
      return null;
    }
    BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
    if ( chrt == null ) {
      return null;
    }
    if ( ! BleUtils.canChrtPWrite( chrt ) ) {
      return null;
    }
    return chrt;
  }


}
