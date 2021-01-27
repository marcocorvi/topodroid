/* @file BleCallback.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth low-energy callback
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.utils.TDLog;

import android.os.Looper;
import android.os.Handler;
import android.content.Context;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

import android.util.Log;

class BleCallback extends BluetoothGattCallback
{
  BleComm mComm;
  BluetoothGatt mGatt = null;
  static final boolean mAutoConnect = false;

  BleCallback( BleComm comm )
  {
    mComm = comm;
  }

  public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic chrt )
  {
    String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
    // Log.v("BRIC", "onCharacteristicChanged " + uuid_str );
    mComm.changedChrt( chrt );
  }

  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status)
  {
    String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
    // Log.v("BRIC", "onCharacteristicRead " + uuid_str + " " + status );
    if ( status == BluetoothGatt.GATT_READ_NOT_PERMITTED ) {
      TDLog.Error("BRIC read not permitted");
      mComm.error( status );
    } else if ( isSuccess( status ) ) {
      mComm.readedChrt( uuid_str, chrt.getValue() );
    } else {
      mComm.failure( status );
    }
  }

  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status)
  {
    String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
    // Log.v("BRIC", "onCharacteristicWrite " + uuid_str + " " + status );
    if ( status == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH ) {
      TDLog.Error("BRIC write invalid length");
      mComm.error( status );
    } else if ( status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED ) {
      TDLog.Error("BRIC write not permitted");
      mComm.error( status );
    } else if ( isSuccess( status ) ) {
      mComm.writtenChrt( uuid_str, chrt.getValue() );
    } else {
      mComm.failure( status );
    }
  }
  
  public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
  {
    // Log.v("BRIC", "onConnectionStateChange " + status );
    if ( isSuccess( status ) ) {
      if ( newState == BluetoothProfile.STATE_CONNECTED ) {
        mGatt = gatt;
        (new Handler( Looper.getMainLooper() )).post( new Runnable() {
          public void run() { mGatt.discoverServices(); }
        } );
      } else if ( newState == BluetoothProfile.STATE_DISCONNECTED ) {
        if ( gatt != null ) gatt.close();
        mGatt = null;
        mComm.disconnected();
      }
    } else {
      if ( gatt != null ) gatt.close();
      mGatt = null;
      if ( status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION ||
           status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION ) {
        // TODO
        // device.createBond();
        // device.connectGatt();
        mComm.error( status );
      } else {
        mComm.failure( status );
      }
    }
  }

  public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor desc, int status)
  {
    String uuid_str = BleUtils.uuidToShortString( desc.getUuid() );
    // Log.v("BRIC", "onDescriptorRead " + uuid_str + " " + status );
    if ( isSuccess( status ) ) {
      mComm.readedDesc( uuid_str, desc.getValue() );
    } else {
      mComm.failure( status );
    }
  }

  public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor desc, int status)
  {
    String uuid_str = BleUtils.uuidToShortString( desc.getUuid() );
    // Log.v("BRIC", "onDescriptorWrite " + uuid_str + " " + status );
    if ( isSuccess( status ) ) {
      mComm.writtenDesc( uuid_str, desc.getValue() );
    } else {
      mComm.failure( status );
    }
  }

  public void onMtuChanged(BluetoothGatt gatt, int mtu, int status)
  { 
    // Log.v("BRIC", "onMtuChanged " + status );
    if ( isSuccess( status ) ) {
    } else {
      mComm.error( status );
    }
  }

  public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
  { 
    // Log.v("BRIC", "onReadRemoteRssi " + status );
    if ( isSuccess( status ) ) {
    } else {
      mComm.error( status );
    }
  }

  public void onReliableWriteCompleted(BluetoothGatt gatt, int status)
  { 
    // Log.v("BRIC", "onReliableWriteCompleted " + status );
    if ( isSuccess( status ) ) {
      mComm.completedReliableWrite();
    } else {
      mComm.error( status );
    }
  }

  public void onServicesDiscovered(BluetoothGatt gatt, int status)
  {
    // Log.v("BRIC", "on Services Discovered " + status );
    if ( isSuccess( status ) ) {
      mComm.servicesDiscovered( gatt );
    } else {
      mComm.failure( status );
    }
  }

  void disconnectGatt()
  {
    if ( mGatt != null ) {
      // Log.v("BRIC", "disconnect gatt");
      mGatt.disconnect();
      mGatt = null;
    }
  }

  void connectGatt( Context ctx, BluetoothDevice device )
  {
    disconnectGatt();
    // Log.v("BRIC", "connect gatt");
    device.connectGatt( ctx, mAutoConnect, this );
  }

  // ---------------------------------------------------------------------

  private boolean setNotification( BluetoothGattCharacteristic chrt, byte [] value )
  {
    if ( ! mGatt.setCharacteristicNotification( chrt, true ) ) {
      TDLog.Error("BRIC failed notify enable");
      return false;
    }
    BluetoothGattDescriptor desc = chrt.getDescriptor( BleUtils.CCCD_UUID );
    if ( desc == null ) {
      TDLog.Error("BRIC failed no CCCD descr" );
      return false;
    }
    // Log.v("BRIC", "set notification: " + BleUtils.uuidToString( chrt.getUuid() ) );
    if ( ! desc.setValue( value ) ) return false;
    return mGatt.writeDescriptor( desc );
  }

  boolean enableNotify( UUID srvUuid, UUID chrtUuid )
  {
    BluetoothGattCharacteristic chrt = getNotifyChrt( srvUuid, chrtUuid );
    return ( chrt != null ) && enableNotify( chrt );
  }

  boolean enableNotify( BluetoothGattCharacteristic chrt )
  {
    byte[] enable = BleUtils.getChrtPNotify( chrt );
    if ( enable == null ) return false;
    return setNotification( chrt, enable );
  }

  boolean disableNotify( UUID srvUuid, UUID chrtUuid )
  {
    BluetoothGattCharacteristic chrt = getNotifyChrt( srvUuid, chrtUuid );
    return ( chrt != null ) && disableNotify( chrt );
  }

  boolean disableNotify( BluetoothGattCharacteristic chrt )
  {
    return setNotification( chrt, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE );
  }

  boolean readChrt( UUID srvUuid, UUID chrtUuid )
  {
    BluetoothGattCharacteristic chrt = getReadChrt( srvUuid, chrtUuid );
    return chrt != null && mGatt.readCharacteristic( chrt );
  }

  boolean writeChrt(  UUID srvUuid, UUID chrtUuid, byte[] bytes )
  {
    BluetoothGattCharacteristic chrt = getWriteChrt( srvUuid, chrtUuid );
    if ( chrt == null ) return false;
    int write_type = BleUtils.getChrtWriteType( chrt );
    if ( write_type < 0 ) return false;
    chrt.setWriteType( write_type );
    chrt.setValue( bytes );
    return mGatt.writeCharacteristic( chrt );
  }

  int toInt4( byte[] b )
  {
    return toInt( b[0] ) + ( toInt( b[1] ) << 8 ) + ( toInt( b[2] ) << 16 ) + ( toInt( b[3] ) << 24 );
  }

  int toInt( byte b ) { return (( b<0 )? (int)b + 256 : (int)b) & 0xff; }

  // failure codes
  //   0 GATT_SUCCESS
  //   2 GATT_READ_NOT_PERMITTED
  //   3 GATT_WRITE_NOT_PERMITTED
  //   5 GATT_INSUFFICIENT_AUTHENTICATION
  //   6 GATT_REQUEST_NOT_SUPPORTED
  //   7 GATT_INVALID_OFFSET
  //   8 ???
  //  13 GATT_INVALID_ATTRIBUTE_LENGTH
  //  15 GATT_INSUFFICIENT_ENCRYPTION
  // 133 GATT_ERROR
  // 143 GATT_CONNECTION_CONGESTED
  // 257 GATT_FAILURE  
  private static boolean isSuccess( int status )
  {
    if ( status == BluetoothGatt.GATT_SUCCESS ) return true;
    TDLog.Error("BRIC callback failure with status " + status );
    return false;
  }

  private BluetoothGattCharacteristic getNotifyChrt( UUID srvUuid, UUID chrtUuid )
  {
    if ( mGatt == null ) return null;
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) return null;
    BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
    if ( chrt == null || ! BleUtils.canChrtPNotify( chrt ) ) return null;
    return chrt;
  }

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
    if ( mGatt == null ) return null;
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) return null;
    BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
    if ( chrt == null || ! BleUtils.canChrtPWrite( chrt ) ) return null;
    return chrt;
  }

}
