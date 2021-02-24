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
 * TopoDroid implementation of BLE callback follows the guidelines of 
 *   Chee Yi Ong,
 *   "The ultimate guide to Android bluetooth low energy"
 *   May 15, 2020
 */
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog;
import com.topodroid.dev.ConnectionState;

import android.os.Build;
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

public class BleCallback extends BluetoothGattCallback
{
  public final static int CONNECTION_TIMEOUT =   8;
  public final static int CONNECTION_133     = 133;

  private BleComm mComm;
  private BluetoothGatt mGatt = null;
  private boolean mAutoConnect = false;

  public BleCallback( BleComm comm, boolean auto_connect )
  {
    mComm = comm;
    mAutoConnect = auto_connect;
  }

  @Override
  public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic chrt )
  {
    mComm.changedChrt( chrt );
  }

  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status)
  {
    if ( isSuccess( status, "onCharacteristicRead" ) ) {
      String uuid_str = chrt.getUuid().toString();
      mComm.readedChrt( uuid_str, chrt.getValue() );
    } else if ( status == BluetoothGatt.GATT_READ_NOT_PERMITTED ) {
      mComm.error( status );
    } else {
      mComm.error( status );
    }
  }

  @Override
  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status)
  {
    if ( isSuccess( status, "onCharacteristicWrite" ) ) {
      String uuid_str = chrt.getUuid().toString();
      mComm.writtenChrt( uuid_str, chrt.getValue() );
    } else 
    if ( status == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH 
      || status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED ) {
      mComm.error( status );
    } else {
      mComm.failure( status );
    }
  }
  
  @Override
  public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
  {
    if ( isSuccess( status, "onConnectionStateChange" ) ) {
      if ( newState == BluetoothProfile.STATE_CONNECTED ) {
        // TO CHECK THIS
        // mGatt = gatt;
        // (new Handler( Looper.getMainLooper() )).post( new Runnable() {
        //   public void run() { gatt.discoverServices(); }
        // } );
        gatt.discoverServices();
      } else if ( newState == BluetoothProfile.STATE_DISCONNECTED ) {
        if ( gatt != null ) gatt.close();
        mGatt = null;
        mComm.disconnected(); // this calls notifyStatus( CONN_DISCONNECTED );
      } else {
        // Log.v("DistoX-BLE", "BLE callback: on Connection State Change new state " + newState );
      }
    } else {
      mComm.notifyStatus( ConnectionState.CONN_WAITING );
      if ( status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION 
        || status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION 
        || status == CONNECTION_TIMEOUT 
        || status == CONNECTION_133 ) {
        // TODO
        // device.createBond();
        // device.connectGatt();
        mComm.error( status );
        // mComm.reconnectDevice();
      } else { // status == BluetoothGatt.GATT_FAILURE
        mComm.failure( status );
        // mComm.notifyStatus( ConnectionState.CONN_DISCONNECTED );
        // mComm.disconnected();
      }
    }
  }

  @Override
  public void onServicesDiscovered(BluetoothGatt gatt, int status)
  {
    // super.onServicesDiscovered( gatt, status );
    // Log.v("DistoX-BLE", "BLE callback: on Services Discovered " + status );
    if ( isSuccess( status, "onServicesDiscovered" ) ) {
      int ret = mComm.servicesDiscovered( gatt ); // calls notifyStatus( ... CONNECTED )
      if ( ret == 0 ) {
        mGatt = gatt;
      } else {
        if ( gatt != null ) gatt.close();
        mGatt = null;
        mComm.failure( ret );
      }
    } else {
      // Log.v("DistoX-BLE", "BLE callback: service discover faiure");
      mComm.failure( status );
    }
  }

  @Override
  public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor desc, int status)
  {
    // Log.v("DistoX-BLE", "BLE callback: onDescriptorRead " + desc.getUuuid() + " " + status );
    if ( isSuccess( status, "onDescriptorRead" ) ) {
      String uuid_str = desc.getUuid().toString();
      String uuid_chrt_str = desc.getCharacteristic().getUuid().toString();
      mComm.readedDesc( uuid_str, uuid_chrt_str, desc.getValue() );
    } else {
      // Log.v("DistoX-BLE", "BLE callback: desc read error");
      mComm.error( status );
    }
  }

  @Override
  public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor desc, int status)
  {
    // Log.v("DistoX-BLE", "BLE callback: onDescriptorWrite " + desc.getUuid() + " " + status );
    if ( isSuccess( status, "onDescriptorWrite" ) ) {
      String uuid_str = desc.getUuid().toString();
      String uuid_chrt_str = desc.getCharacteristic().getUuid().toString();
      mComm.writtenDesc( uuid_str, uuid_chrt_str, desc.getValue() );
    } else {
      // Log.v("DistoX-BLE", "BLE callback: desc write error");
      mComm.error( status );
    }
  }

  @Override
  public void onMtuChanged(BluetoothGatt gatt, int mtu, int status)
  { 
    // Log.v("DistoX-BLE", "BLE callback: onMtuChanged " + status );
    if ( isSuccess( status, "onMtuChanged" ) ) {
      mComm.changedMtu( mtu );
    } else {
      // Log.v("DistoX-BLE", "BLE callback: MTU change error");
      mComm.error( status );
    }
  }

  @Override
  public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
  { 
    // Log.v("DistoX-BLE", "BLE callback: onReadRemoteRssi " + status );
    if ( isSuccess( status, "onReadRemoteRssi" ) ) {
      mComm.readedRemoteRssi( rssi );
    } else {
      // Log.v("DistoX-BLE", "BLE callback: read RSSI error");
      mComm.error( status );
    }
  }

  @Override
  public void onReliableWriteCompleted(BluetoothGatt gatt, int status)
  { 
    // Log.v("DistoX-BLE", "BLE callback: onReliableWriteCompleted " + status );
    if ( isSuccess( status, "onReliableWriteCompleted" ) ) {
      mComm.completedReliableWrite();
    } else {
      // Log.v("DistoX-BLE", "BLE callback: reliable write error");
      mComm.error( status );
    }
  }

  public void closeGatt()
  { 
    if ( mGatt != null ) {
      // mGatt.disconnect();
      mGatt.close();
      mGatt = null;
    }
  }

  public void connectGatt( Context ctx, BluetoothDevice device )
  {
    closeGatt();
    // Log.v("DistoX-BLE", "BLE callback: connect gatt");
    // device.connectGatt( ctx, mAutoConnect, this );
    if ( Build.VERSION.SDK_INT < 23 ) {
      mGatt = device.connectGatt( ctx, mAutoConnect, this );
    } else {
      mGatt = device.connectGatt( ctx, mAutoConnect, this, BluetoothDevice.TRANSPORT_LE ); 
    }
  }

  // FROM SapCallback
  public void disconnectCloseGatt( )
  { 
    // Log.v("DistoX-BLE", "BLE callback: close GATT");
    // mWriteInitialized = false; 
    // mReadInitialized  = false; 
    if ( mGatt != null ) {
      mGatt.disconnect();
      mGatt.close();
      mGatt = null;
    }
  }

  public void disconnectGatt()
  {
    // Log.v("DistoX-BLE", "SAP callback: disconnect GATT");
    // mWriteInitialized = false; 
    // mReadInitialized  = false; 
    if ( mGatt != null ) {
      // Log.v("DistoX-BLE", "SAP callback: disconnect gatt");
      mGatt.disconnect();
      // FIXME mGapp.close();
      mGatt = null;
    }
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
    // Log.v("DistoX-BLE", "BLE callback: set notification: " + chrt.getUuid().toString() );
    return mGatt.writeDescriptor( desc );
  }

  /*
  boolean enableNotify( UUID srvUuid, UUID chrtUuid )
  {
    Log.v("DistoX-BLE", "BLE callback enable notify " + chrtUuid.toString() );
    BluetoothGattCharacteristic chrt = getNotifyChrt( srvUuid, chrtUuid );
    return ( chrt != null ) && enableNotify( chrt );
  }
  */

  public boolean enablePNotify( UUID srvUuid, UUID chrtUuid ) 
  {
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) {
      // Log.v("DistoX-BLE", "BLE callback enablePNotify null service " + srvUuid );
      return false;
    }
    return enablePNotify( srvUuid, srv.getCharacteristic( chrtUuid ) );
  }

  public boolean enablePNotify( UUID srvUuid, BluetoothGattCharacteristic chrt )
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

  // public boolean disablePNotify( UUID srvUuid, BluetoothGattCharacteristic chrt )
  // {
  //   if ( chrt != null ) return false;
  //   return setNotification( chrt, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE );
  // }

  // ----------------------------------------------------------------

  public boolean readChrt( UUID srvUuid, UUID chrtUuid )
  {
    BluetoothGattCharacteristic chrt = getReadChrt( srvUuid, chrtUuid );
    return chrt != null && mGatt.readCharacteristic( chrt );
  }

  public boolean writeChrt(  UUID srvUuid, UUID chrtUuid, byte[] bytes )
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

  // int toInt4( byte[] b )
  // {
  //   return toInt( b[0] ) + ( toInt( b[1] ) << 8 ) + ( toInt( b[2] ) << 16 ) + ( toInt( b[3] ) << 24 );
  // }

  // int toInt( byte b ) { return (( b<0 )? (int)b + 256 : (int)b) & 0xff; }

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
  public static boolean isSuccess( int status, String name )
  {
    if ( status == BluetoothGatt.GATT_SUCCESS ) return true;
    TDLog.Error("BLE callback: callback " + name + " failure - status " + status );
    return false;
  }

  // -------------------------------------------------------------------------
  private BluetoothGattCharacteristic getNotifyChrt( UUID srvUuid, UUID chrtUuid )
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
    if ( ! BleUtils.canChrtPNotify( chrt ) ) {
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
