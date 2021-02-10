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

import com.topodroid.dev.bric.BleCallback;
import com.topodroid.dev.bric.BleUtils;
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

class SapCallback extends BluetoothGattCallback
{
  SapComm mSapComm; // TO BECOME BleComm mComm inherited
  // BluetoothGatt mGatt; // inherited
  static final boolean mAutoConnect = true; // this is false in BleCallback
  private BluetoothGatt mGatt;

  // BluetoothGattCharacteristic mReadChrt  = null;
  // BluetoothGattCharacteristic mWriteChrt = null;
  // private boolean mReadInitialized  = false;
  // private boolean mWriteInitialized = false;

  SapCallback( SapComm comm ) 
  { 
    mSapComm  = comm;
  }

  @Override
  public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic chrt )
  {
    mSapComm.changedChrt( chrt );
    /*
    if ( chrt == mReadChrt ) {
      // Log.v("DistoX-BLE", "SAP callback: read chrt changed");
      mSapComm.changedChrt( chrt );
    } else if ( chrt == mWriteChrt ) {
      Log.v("DistoX-BLE", "SAP callback: write chrt changed");
      mSapComm.changedChrt( chrt );
    } else {
      super.onCharacteristicChanged( gatt, chrt );
    }
    */
  }

  @Override
  public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status )
  {
    // if ( chrt != mReadChrt ) {
    //   super.onCharacteristicRead( gatt, chrt, status );
    //   return;
    // } 
    if ( status != BluetoothGatt.GATT_SUCCESS ) {
      Log.v("DistoX-BLE", "SAP callback: FAIL on char read");
      mSapComm.error( status );
    } else {
      String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
      mSapComm.readedChrt( uuid_str, chrt.getValue() );
      /*
      if ( ! mReadInitialized ) {
        Log.v("DistoX-BLE", "SAP callback: ERROR read-uninitialized chrt");
        mSapComm.error( -1 );
      } else {
        // Log.v("DistoX-BLE", "SAP callback: on char read ok");
        String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
        mSapComm.readedChrt( uuid_str, chrt.getValue() );
      }
      */
    }
  }
      
  @Override
  public void onCharacteristicWrite( BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status )
  {
    // if ( chrt != mWriteChrt ) {
    //   super.onCharacteristicWrite( gatt, chrt, status );
    //   return;
    // } 
    if ( status != BluetoothGatt.GATT_SUCCESS ) {
      Log.v("DistoX-BLE", "SAP callback: FAIL on char write");
      mSapComm.error( status );
    } else {
      String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
      mSapComm.writtenChrt( uuid_str, chrt.getValue() );
      /*
      if ( ! mWriteInitialized ) {
        Log.v("DistoX-BLE", "SAP callback: ERROR write-uninitialized chrt" );
        return;
      } else {
        // Log.v("DistoX-BLE", "SAP callback: on char write ok");
        String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
        mSapComm.writtenChrt( uuid_str, chrt.getValue() );
      }
      */
    }
  }

  @Override
  public void onConnectionStateChange( BluetoothGatt gatt, int status, int state )
  {
    // super.onConnectionStateChange( gatt, status, state );
    if ( BleCallback.isSuccess( status, "onConnectionStateChange" ) ) {
      if ( state == BluetoothProfile.STATE_CONNECTED ) {
        // Log.v("DistoX-BLE", "SAP callback: conn state changed: connected");
        if ( ! gatt.discoverServices() ) {
          // Log.v("DistoX-BLE", "SAP callback FAIL service discovery");
          mSapComm.notifyStatus( ConnectionState.CONN_DISCONNECTED );
        } else {
          mSapComm.notifyStatus( ConnectionState.CONN_CONNECTED );
        }
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
      } else if ( status == BluetoothGatt.GATT_FAILURE ) {
        Log.v("DistoX-BLE", "SAP callback FAIL state changed - GATT failure  disconnect" );
        mSapComm.notifyStatus( ConnectionState.CONN_DISCONNECTED );
        mSapComm.disconnected();
      } else {
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
    if ( status != BluetoothGatt.GATT_SUCCESS ) {
      Log.v("DistoX-BLE", "SAP callback FAIL service discover");
      mSapComm.failure( status );
      return;
    }
    // Log.v("DistoX-BLE", "SAP callback service discovered ok" );
    int ret = mSapComm.servicesDiscovered( gatt );
    if ( ret == 0 ) {
      mGatt = gatt;
    } else {
      if ( gatt != null ) gatt.close();
      mGatt = null;
      mSapComm.failure( ret );
    }

    /*
    BluetoothGattService srv = gatt.getService( SapConst.SAP5_SERVICE_UUID );

    mReadChrt  = srv.getCharacteristic( SapConst.SAP5_CHRT_READ_UUID );
    mWriteChrt = srv.getCharacteristic( SapConst.SAP5_CHRT_WRITE_UUID );

    // boolean write_has_write = BleUtils.isChrtRWrite( mWriteChrt.getProperties() );
    // boolean write_has_write_no_response = BleUtils.isChrtRWriteNoResp( mWriteChrt.getProperties() );
    // Log.v("DistoX-BLE", "SAP callback W-chrt has write " + write_has_write );

    mWriteChrt.setWriteType( BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT );
    mWriteInitialized = gatt.setCharacteristicNotification( mWriteChrt, true );

    mReadInitialized = gatt.setCharacteristicNotification( mReadChrt, true );

    BluetoothGattDescriptor readDesc = mReadChrt.getDescriptor( BleUtils.CCCD_UUID );
    if ( readDesc == null ) {
      Log.v("DistoX-BLE", "SAP callback FAIL no R-desc CCCD ");
      mSapComm.failure( -1 );
      return;
    }

    // boolean read_has_write  = BleUtils.isChrtPWrite( mReadChrt.getProperties() );
     // Log.v("DistoX-BLE", "SAP callback R-chrt has write " + read_has_write );

    byte[] notify = BleUtils.getChrtPNotify( mReadChrt );
    if ( notify == null ) {
      Log.v("DistoX-BLE", "SAP callback FAIL no indicate/notify R-property ");
      mSapComm.failure( -2 );
    } else {
      readDesc.setValue( notify );
      if ( ! gatt.writeDescriptor( readDesc ) ) {
        Log.v("DistoX-BLE", "SAP callback ERROR writing readDesc");
        mSapComm.failure( -3 );
      }
    }
    */
  }

  @Override
  public void onDescriptorWrite( BluetoothGatt gatt, BluetoothGattDescriptor desc, int status ) 
  {
    if ( status != BluetoothGatt.GATT_SUCCESS ) {
      Log.v("DistoX-BLE", "SAP callback FAIL on descriptor write");
      mSapComm.error( status );
    } else {
      String uuid_str = BleUtils.uuidToShortString( desc.getUuid() );
      String uuid_chrt_str = BleUtils.uuidToShortString( desc.getCharacteristic().getUuid() );
      mSapComm.writtenDesc( uuid_str, uuid_chrt_str, desc.getValue() );
      /*
      if ( desc.getCharacteristic() == mSapComm.getReadChrt() ) { // everything is ok
        // tell the comm it is connected
        mSapComm.connected( true );
      } else if ( desc.getCharacteristic() == mSapComm.getWriteChrt() ) { // should not happen
        Log.v("DistoX-BLE", "SAP callback ERROR write-descriptor write: ?? should not happen");
      } else {
        Log.v("DistoX-BLE", "SAP callback ERROR unknown descriptor write " + desc.getUuid().toString() );
        super.onDescriptorWrite( gatt, desc, status );
      }
      */
    }
  }

  // FROM BleCallback
  void closeGatt()
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

  boolean writeCharacteristic( BluetoothGattCharacteristic chrt ) 
  {
    return mGatt != null && mGatt.writeCharacteristic( chrt ); 
  }

  boolean readCharacteristic( BluetoothGattCharacteristic chrt ) 
  {
    return mGatt != null && mGatt.readCharacteristic( chrt ); 
  }

  // FUDGE
  // BluetoothGattCharacteristic getWriteChrt( ) { return mWriteChrt; }

}
