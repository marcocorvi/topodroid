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
  final static int CONNECTION_TIMEOUT =   8;
  final static int CONNECTION_133     = 133;

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
    // Log.v("DistoX-BLE-B", "BLE callback: onCharacteristicChanged " + uuid_str );
    mComm.changedChrt( chrt );
  }

  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status)
  {
    String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
    // Log.v("DistoX-BLE-B", "BLE callback: onCharacteristicRead " + uuid_str + " " + status );
    if ( status == BluetoothGatt.GATT_READ_NOT_PERMITTED ) {
      // Log.v("DistoX-BLE-B", "BLE callback: chrt read not permitted");
      mComm.error( status );
    } else if ( isSuccess( status ) ) {
      mComm.readedChrt( uuid_str, chrt.getValue() );
    } else {
      // Log.v("DistoX-BLE-B", "BLE callback: chrt read error");
      mComm.error( status );
    }
  }

  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status)
  {
    String uuid_str = BleUtils.uuidToShortString( chrt.getUuid() );
    // Log.v("DistoX-BLE-B", "BLE callback: onCharacteristicWrite " + uuid_str + " " + status );
    if ( status == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH ) {
      // TDLog.Error("BLE callback: chrt write invalid length");
      // Log.v("DistoX-BLE-B", "BLE callback: chrt write invalid length");
      mComm.error( status );
    } else if ( status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED ) {
      // TDLog.Error("BLE callback: chrt write not permitted");
      // Log.v("DistoX-BLE-B", "BLE callback: chrt write not permitted");
      mComm.error( status );
    } else if ( isSuccess( status ) ) {
      mComm.writtenChrt( uuid_str, chrt.getValue() );
    } else {
      // Log.v("DistoX-BLE-B", "BLE callback: chrt write faiure");
      mComm.failure( status );
    }
  }
  
  public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
  {
    if ( isSuccess( status ) ) {
      if ( newState == BluetoothProfile.STATE_CONNECTED ) {
        // Log.v("DistoX-BLE-B", "BLE callback: onConnectionStateChange CONNECTED ");
        mGatt = gatt;
        (new Handler( Looper.getMainLooper() )).post( new Runnable() {
          public void run() { mGatt.discoverServices(); }
        } );
      } else if ( newState == BluetoothProfile.STATE_DISCONNECTED ) {
        // Log.v("DistoX-BLE-B", "BLE callback: onConnectionStateChange DISCONNECTED ");
        if ( gatt != null ) gatt.close();
        mGatt = null;
        mComm.disconnected();
      } else {
        // Log.v("DistoX-BLE-B", "BLE callback: onConnectionStateChange new state " + newState );
      }
    } else {
      // if ( gatt != null ) gatt.close();
      // mGatt = null;
      if ( status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION 
        || status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION 
        || status == CONNECTION_TIMEOUT 
        || status == CONNECTION_133 ) {
        // TODO
        // device.createBond();
        // device.connectGatt();
        // Log.v("DistoX-BLE-B", "BLE callback: on conn state change error " + status );
        mComm.error( status );
      } else {
        // Log.v("DistoX-BLE-B", "BLE callback: on conn state change faiure " + status );
        mComm.failure( status );
      }
    }
  }

  public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor desc, int status)
  {
    String uuid_str = BleUtils.uuidToShortString( desc.getUuid() );
    // Log.v("DistoX-BLE-B", "BLE callback: onDescriptorRead " + uuid_str + " " + status );
    if ( isSuccess( status ) ) {
      mComm.readedDesc( uuid_str, desc.getValue() );
    } else {
      // Log.v("DistoX-BLE-B", "BLE callback: desc read error");
      mComm.error( status );
    }
  }

  public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor desc, int status)
  {
    String uuid_str = BleUtils.uuidToShortString( desc.getUuid() );
    // Log.v("DistoX-BLE-B", "BLE callback: onDescriptorWrite " + uuid_str + " " + status );
    if ( isSuccess( status ) ) {
      mComm.writtenDesc( uuid_str, desc.getValue() );
    } else {
      // Log.v("DistoX-BLE-B", "BLE callback: desc write error");
      mComm.error( status );
    }
  }

  public void onMtuChanged(BluetoothGatt gatt, int mtu, int status)
  { 
    // Log.v("DistoX-BLE-B", "BLE callback: onMtuChanged " + status );
    if ( isSuccess( status ) ) {
      mComm.changedMtu( mtu );
    } else {
      // Log.v("DistoX-BLE-B", "BLE callback: MTU change error");
      mComm.error( status );
    }
  }

  public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
  { 
    // Log.v("DistoX-BLE-B", "BLE callback: onReadRemoteRssi " + status );
    if ( isSuccess( status ) ) {
      mComm.readedRemoteRssi( rssi );
    } else {
      // Log.v("DistoX-BLE-B", "BLE callback: read RSSI error");
      mComm.error( status );
    }
  }

  public void onReliableWriteCompleted(BluetoothGatt gatt, int status)
  { 
    // Log.v("DistoX-BLE-B", "BLE callback: onReliableWriteCompleted " + status );
    if ( isSuccess( status ) ) {
      mComm.completedReliableWrite();
    } else {
      // Log.v("DistoX-BLE-B", "BLE callback: reliable write error");
      mComm.error( status );
    }
  }

  public void onServicesDiscovered(BluetoothGatt gatt, int status)
  {
    // Log.v("DistoX-BLE-B", "BLE callback: on Services Discovered " + status );
    if ( isSuccess( status ) ) {
      mComm.servicesDiscovered( gatt );
    } else {
      // Log.v("DistoX-BLE-B", "BLE callback: service discover faiure");
      mComm.failure( status );
    }
  }

  void closeGatt()
  { 
    if ( mGatt != null ) {
      mGatt.close();
      mGatt = null;
    }
  }

  void disconnectGatt()
  {
    if ( mGatt != null ) {
      // Log.v("DistoX-BLE-B", "BLE callback: disconnect gatt");
      mGatt.disconnect();
      // FIXME mGapp.close();
      mGatt = null;
    }
  }

  void connectGatt( Context ctx, BluetoothDevice device )
  {
    disconnectGatt();
    // Log.v("DistoX-BLE-B", "BLE callback: connect gatt");
    device.connectGatt( ctx, mAutoConnect, this );
  }

  // ---------------------------------------------------------------------

  private boolean setNotification( BluetoothGattCharacteristic chrt, byte [] value )
  {
    if ( ! mGatt.setCharacteristicNotification( chrt, true ) ) {
      TDLog.Error("BLE callback: failed notify enable");
      return false;
    }
    BluetoothGattDescriptor desc = chrt.getDescriptor( BleUtils.CCCD_UUID );
    if ( desc == null ) {
      TDLog.Error("BLE callback: failed no CCCD descr" );
      return false;
    }
    if ( ! desc.setValue( value ) ) {
      TDLog.Error("BLE callback: failed descr set value" );
      return false;
    }
    // Log.v("DistoX-BLE-B", "BLE callback: set notification: " + BleUtils.uuidToString( chrt.getUuid() ) );
    return mGatt.writeDescriptor( desc );
  }

  /*
  boolean enableNotify( UUID srvUuid, UUID chrtUuid )
  {
    Log.v("DistoX-BLE", "BLE callback enable notify " + BleUtils.uuidToShortString( chrtUuid ) );
    BluetoothGattCharacteristic chrt = getNotifyChrt( srvUuid, chrtUuid );
    return ( chrt != null ) && enableNotify( chrt );
  }
  */

  boolean enablePNotify( UUID srvUuid, BluetoothGattCharacteristic chrt )
  {
    // Log.v("DistoX-BLE", "BLE callback enable P notify " + srvUuid + " " + chrt.getUuid() );
    if ( chrt == null ) {
      TDLog.Error("BLE callback: enable notify null chrt");
      return false;
    }
    byte[] enable = BleUtils.getChrtPNotify( chrt );
    if ( enable == null ) {
      TDLog.Error("BLE callback: enable notify null enable");
      return false;
    }
    return setNotification( chrt, enable );
  }

  boolean disablePNotify( UUID srvUuid, BluetoothGattCharacteristic chrt )
  {
    if ( chrt != null ) return false;
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
    if ( chrt == null ) {
      // Log.v("DistoX-BLE-B", "BLE acllback writeChrt null chrt ");
      return false;
    }
    int write_type = BleUtils.getChrtWriteType( chrt );
    if ( write_type < 0 ) {
      // Log.v("DistoX-BLE-B", "BLE acllback writeChrt neg type " + write_type );
      return false;
    }
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
    TDLog.Error("BLE callback: callback failure with status " + status );
    return false;
  }

  private BluetoothGattCharacteristic getNotifyChrt( UUID srvUuid, UUID chrtUuid )
  {
    if ( mGatt == null ) {
      // Log.v("DistoX-BLE-B", "BLE callback getNotofyChrt null GATT");
      return null;
    }
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) {
      // Log.v("DistoX-BLE-B", "BLE callback getNotofyChrt null service " + srvUuid );
      return null;
    }
    BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
    if ( chrt == null ) {
      // Log.v("DistoX-BLE-B", "BLE callback getNotofyChrt null chrt " + chrtUuid );
      return null;
    }
    if ( ! BleUtils.canChrtPNotify( chrt ) ) {
      // Log.v("DistoX-BLE-B", "BLE callback getNotofyChrt chrt " + chrtUuid + " cannot P notify");
      return null;
    }
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
    if ( mGatt == null ) {
      // Log.v("DistoX-BLE-B", "BLE callback chrt write null GATT");
      return null;
    }
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) {
      // Log.v("DistoX-BLE-B", "BLE callback chrt write null service " + srvUuid );
      return null;
    }
    BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
    if ( chrt == null ) {
      // Log.v("DistoX-BLE-B", "BLE callback chrt write null chrt " + chrtUuid );
      return null;
    }
    if ( ! BleUtils.canChrtPWrite( chrt ) ) {
      // Log.v("DistoX-BLE-B", "BLE callback chrt write cannot P-write " + chrtUuid );
      return null;
    }
    return chrt;
  }

}
