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
// import com.topodroid.TDX.TDToast;

import android.os.Build;
// import android.os.Looper;
// import android.os.Handler;
import android.content.Context;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;

// import java.util.List;
import java.util.UUID;

public class BleCallback extends BluetoothGattCallback
{
  public final static int CONNECTION_TIMEOUT =   8;
  public final static int CONNECTION_133     = 133;
  public final static int CONNECTION_19      =  19;

  private BleComm mComm;
  // private BleChrtChanged mChrtChanged;
  private BluetoothGatt mGatt = null;
  private boolean mAutoConnect = false;

  /** cstr
   * @param comm    communication object
   * @param auto_connect whether to auto (re)connect
   */
  public BleCallback( BleComm comm, boolean auto_connect )
  {
    mComm        = comm;
    // mChrtChanged = comm;
    mAutoConnect = auto_connect;
  }

  // public BleCallback( BleComm comm, BleChrtChanged chrt_changed, boolean auto_connect )
  // {
  //   mComm        = comm;
  //   mChrtChanged = chrt_changed;
  //   mAutoConnect = auto_connect;
  // }

  /** react to a characteristic change
   * @param gatt   GATT
   * @param chrt   charcateristic
   */ 
  @Override
  public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic chrt )
  {
    // if ( mChrtChanged != null ) { mChrtChanged.changedChrt( chrt ); } else { mComm.changedChrt( chrt ); }
    // TDLog.f("BLE on chrt changed");
    mComm.changedChrt( chrt );
  }

  /** react to a characteristic read
   * @param gatt   GATT
   * @param chrt   charcateristic
   * @param status status
   */ 
  @Override
  public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status)
  {
    // TDLog.f("BLE on chrt read: " + status );
    if ( isSuccess( status, "onCharacteristicRead" ) ) {
      String uuid_str = chrt.getUuid().toString();
      mComm.readedChrt( uuid_str, chrt.getValue() );
    } else if ( status == BluetoothGatt.GATT_READ_NOT_PERMITTED ) {
      TDLog.e("BLE callback on char read NOT PERMITTED - perms " + BleUtils.isChrtRead( chrt ) + " " + chrt.getPermissions() );
      mComm.error( status, chrt.getUuid().toString() );
    } else {
      TDLog.e("BLE callback on char read generic error");
      mComm.error( status, chrt.getUuid().toString() );
    }
  }

  /** react to a characteristic write
   * @param gatt   GATT
   * @param chrt   charcateristic
   * @param status status
   */ 
  @Override
  public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic chrt, int status)
  {
    // TDLog.f("BLE on chrt write: " + status );
    if ( isSuccess( status, "onCharacteristicWrite" ) ) {
      String uuid_str = chrt.getUuid().toString();
      mComm.writtenChrt( uuid_str, chrt.getValue() );
    } else 
    if ( status == BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH 
      || status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED ) {
      mComm.error( status, chrt.getUuid().toString() );
    } else {
      mComm.failure( status, chrt.getUuid().toString() );
    }
  }
  
  /** react to a connection state change
   * @param gatt   GATT
   * @param status status
   * @param state  new state
   */ 
  @Override
  public void onConnectionStateChange(BluetoothGatt gatt, int status, int state)
  {
    // TDLog.v("BLE on connection state change: " + status );
    if ( isSuccess( status, "onConnectionStateChange" ) ) {
      if ( state == BluetoothProfile.STATE_CONNECTED ) {
        // TO CHECK THIS
        // mGatt = gatt;
        // (new Handler( Looper.getMainLooper() )).post( new Runnable() {
        //   public void run() { gatt.discoverServices(); }
        // } );

        // if ( mGatt != null ) mGatt.close(); // FIXME_BRIC
        // mGatt = gatt;
        mComm.connected();
        try {
          gatt.discoverServices();
        } catch ( SecurityException e ) {
          TDLog.e("SECURITY discover services " + e.getMessage() );
          // TDToast.makeBad("Security error: discover services");
          // TODO closeGatt() ?
          return;
        }

      } else if ( state == BluetoothProfile.STATE_DISCONNECTED ) {
        closeGatt();
        mComm.disconnected(); // this calls notifyStatus( CONN_DISCONNECTED );
      // } else {
        // TDLog.f( "BLE callback: on Connection State Change new state " + state );
      }
    } else {
      TDLog.Error("BLE callack onConnectionStateChange error - status " + status );
      mComm.notifyStatus( ConnectionState.CONN_WAITING );
      if ( status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION 
        || status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION 
        || status == CONNECTION_TIMEOUT 
        || status == CONNECTION_133 
        || status == CONNECTION_19 ) {
        // TODO
        // device.createBond();
        // device.connectGatt();
        mComm.error( status, "onConnectionStateChange" );
        // mComm.reconnectDevice();
      } else { // status == BluetoothGatt.GATT_FAILURE
        mComm.failure( status, "onConnectionStateChange" );
        // mComm.notifyStatus( ConnectionState.CONN_DISCONNECTED );
        // mComm.disconnected();
      }
    }
  }

  /** react to the peripheral services discovered
   * @param gatt   GATT
   * @param status status
   */
  @Override
  public void onServicesDiscovered(BluetoothGatt gatt, int status)
  {
    // super.onServicesDiscovered( gatt, status );
    // TDLog.v( "BLE on services discovered " + status );
    if ( isSuccess( status, "onServicesDiscovered" ) ) {
      int ret = mComm.servicesDiscovered( gatt ); // calls notifyStatus( ... CONNECTED )
      if ( ret == 0 ) {
        mGatt = gatt;
      } else {
        closeGatt();
        mComm.failure( ret, "onServicesDiscovered" );
      }
    } else {
      // TDLog.v( "BLE callback: service discover failure");
      mComm.failure( status, "onServicesDiscovered" );
    }
  }

  /** react to a descriptor read
   * @param gatt   GATT
   * @param desc   descriptor
   * @param status status
   */
  @Override
  public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor desc, int status)
  {
    // TDLog.f( "BLE on desc read " + status );
    if ( isSuccess( status, "onDescriptorRead" ) ) {
      String uuid_str = desc.getUuid().toString();
      String uuid_chrt_str = desc.getCharacteristic().getUuid().toString();
      mComm.readedDesc( uuid_str, uuid_chrt_str, desc.getValue() );
    } else {
      // TDLog.v( "BLE callback: desc read error");
      mComm.error( status, desc.getUuid().toString() );
    }
  }

  /** react to a descriptor write
   * @param gatt   GATT
   * @param desc   descriptor
   * @param status status
   */
  @Override
  public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor desc, int status)
  {
    // TDLog.f( "BLE on desc write " + status );
    if ( isSuccess( status, "onDescriptorWrite" ) ) {
      String uuid_str = desc.getUuid().toString();
      String uuid_chrt_str = desc.getCharacteristic().getUuid().toString();
      mComm.writtenDesc( uuid_str, uuid_chrt_str, desc.getValue() );
    } else {
      TDLog.Error( "BLE callback: desc write error - status " + status );
      mComm.error( status, desc.getUuid().toString() );
    }
  }

  /** react to a MTU change
   * @param gatt   GATT
   * @param mtu    MTU value
   * @param status status
   */
  @Override
  public void onMtuChanged(BluetoothGatt gatt, int mtu, int status)
  { 
    // TDLog.f( "BLE on MTU change " + status );
    if ( isSuccess( status, "onMtuChanged" ) ) {
      mComm.changedMtu( mtu );
    } else {
      // TDLog.v( "BLE callback: MTU change error");
      mComm.error( status, "onMtuChange" );
    }
  }

  /** react to a remote RSSI read
   * @param gatt   GATT
   * @param rssi   RSSI value
   * @param status status
   */
  @Override
  public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
  { 
    // TDLog.f( "BLE on read RSSI " + status );
    if ( isSuccess( status, "onReadRemoteRssi" ) ) {
      mComm.readedRemoteRssi( rssi );
    } else {
      // TDLog.v( "BLE callback: read RSSI error");
      mComm.error( status, "onReadRemoteRssi" );
    }
  }

  /** react to a reliable write completion
   * @param gatt   GATT
   * @param status status
   */
  @Override
  public void onReliableWriteCompleted(BluetoothGatt gatt, int status)
  { 
    // TDLog.f( "BLE on reliable write " + status );
    if ( isSuccess( status, "onReliableWriteCompleted" ) ) {
      mComm.completedReliableWrite();
    } else {
      // TDLog.v( "BLE callback: reliable write error");
      mComm.error( status, "onReliableWriteCompleted" );
    }
  }

  /** close the GATT
   */
  public void closeGatt()
  { 
    if ( mGatt != null ) {
      try {
        TDLog.v("BLE close GATT");
        // mGatt.disconnect();
        mGatt.close();
      } catch ( SecurityException e ) {
        TDLog.e("SECURITY GATT close " + e.getMessage() );
        // TDToast.makeBad("Security error: GATT close");
      }
      mGatt = null;
    }
  }

  /** connect the GATT
   * @param ctx    context
   * @param device bluetooth device
   */
  public void connectGatt( Context ctx, BluetoothDevice device )
  {
    closeGatt();
    // TDLog.f( "BLE connect gatt");
    // device.connectGatt( ctx, mAutoConnect, this );
    try { 
      if ( Build.VERSION.SDK_INT < 23 ) {
        mGatt = device.connectGatt( ctx, mAutoConnect, this );
      } else {
        mGatt = device.connectGatt( ctx, mAutoConnect, this, BluetoothDevice.TRANSPORT_LE ); 
      }
    } catch ( SecurityException e ) { // FIXME ANDROID-12
      TDLog.e("SECURITY GATT connect " + e.getMessage() );
      // TDToast.makeBad("Security error: GATT connect");
    }
  }

  /** disconnect and close the GATT
   * @note FROM SapCallback
   */
  public void disconnectCloseGatt( )
  { 
    // TDLog.f( "BLE disconnect close GATT");
    // mWriteInitialized = false; 
    // mReadInitialized  = false; 
    if ( mGatt != null ) {
      try {
        mGatt.disconnect();
        mGatt.close();
      } catch ( SecurityException e ) {
        TDLog.e("SECURITY GATT disconnect and close " + e.getMessage() );
        // TDToast.makeBad("Security error: GATT disconnect and close");
      }
      mGatt = null;
    }
  }

  /** disconnect the GATT
   */
  public void disconnectGatt()
  {
    // TDLog.f( "BLE disconnect GATT");
    // mWriteInitialized = false; 
    // mReadInitialized  = false; 
    if ( mGatt != null ) {
      try {
        // TDLog.v( "BLE callback: disconnect gatt");
        mGatt.disconnect();
        // FIXME mGapp.close();
      } catch ( SecurityException e ) {
        TDLog.e("SECURITY GATT disconnect " + e.getMessage() );
        // TDToast.makeBad("Security error: GATT disconnect");
      }
      mGatt = null;
    }
  }
  // ---------------------------------------------------------------------

  /** set the notification
   * @param chrt   characteristic
   * @param value  ???
   * @return ???
   */
  private boolean setNotification( BluetoothGattCharacteristic chrt, byte [] value )
  {
    try {
      if ( ! mGatt.setCharacteristicNotification( chrt, true ) ) {
        TDLog.e("BLE callback: failed notify enable");
        // TODO closeGatt() ?
        return false;
      }
    } catch ( SecurityException e ) {
      TDLog.e("SECURITY iCHRT notification " + e.getMessage() );
      // TDToast.makeBad("Security error: CHRT notification");
      // TODO closeGatt() ?
      return false;
    }

    BluetoothGattDescriptor desc = chrt.getDescriptor( BleUtils.CCCD_UUID );
    if ( desc == null ) {
      TDLog.e("BLE callback: failed no CCCD descriptor" );
      return false;
    }
    if ( ! desc.setValue( value ) ) {
      TDLog.e("BLE callback: failed descriptor set value" );
      return false;
    }
    TDLog.v( "BLE callback: set notification: " + chrt.getUuid().toString() + " " + value );
    try {
      return mGatt.writeDescriptor( desc );
    } catch ( SecurityException e ) {
      TDLog.e("SECURITY write descriptor " + e.getMessage());
      // TDToast.makeBad("Security error: write descriptor");
      // TODO closeGatt() ?
    }
    return false;
  }

  /*
  boolean enableNotify( UUID srvUuid, UUID chrtUuid )
  {
    // TDLog.v( "BLE callback enable notify " + chrtUuid.toString() );
    BluetoothGattCharacteristic chrt = getNotifyChrt( srvUuid, chrtUuid );
    return ( chrt != null ) && enableNotify( chrt );
  }
  */

  /** enable P notify
   * @param srvUuid     service UUID 
   * @param chrtUuid    characteristics UUID
   * @return true on success
   */
  public boolean enablePNotify( UUID srvUuid, UUID chrtUuid ) 
  {
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) {
      TDLog.e("BLE callback enablePNotify null service " + srvUuid );
      return false;
    }
    return enablePNotify( srvUuid, srv.getCharacteristic( chrtUuid ) );
  }

  /** enable P notify
   * @param srvUuid service UUID (unused)
   * @param chrt    characteristics
   * @return true on success
   */
  public boolean enablePNotify( UUID srvUuid, BluetoothGattCharacteristic chrt )
  {
    TDLog.v( "BLE callback enable P notify " + srvUuid + " " + chrt.getUuid() );
    if ( chrt == null ) {
      TDLog.e("BLE callback: enable notify null chrt");
      return false;
    }
    // TDLog.v( "BLE callback: notify chrt " + chrt.getUuid().toString() + " notifiable " + BleUtils.canChrtPNotify( chrt ) );
    byte[] enable = BleUtils.getChrtPNotify( chrt );
    if ( enable == null ) {
      TDLog.e("BLE callback: enable notify null enable");
      return false;
    }
    return setNotification( chrt, enable );
  }

  // public boolean disablePNotify( UUID srvUuid, BluetoothGattCharacteristic chrt )
  // {
  //   if ( chrt != null ) return false;
  //   return setNotification( chrt, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE );
  // }

  /** enable P indicate
   * @param srvUuid     service UUID 
   * @param chrtUuid    characteristics UUID
   * @return true on success
   */
  public boolean enablePIndicate( UUID srvUuid, UUID chrtUuid ) 
  {
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) {
      TDLog.e("BLE callback enablePIndicate null service " + srvUuid );
      return false;
    }
    return enablePIndicate( srvUuid, srv.getCharacteristic( chrtUuid ) );
  }

  /** enable P indicate
   * @param srvUuid service UUID (unused)
   * @param chrt    characteristics
   * @return true on success
   */
  public boolean enablePIndicate( UUID srvUuid, BluetoothGattCharacteristic chrt )
  {
    // TDLog.v( "BLE callback enable P notify " + srvUuid + " " + chrt.getUuid() );
    if ( chrt == null ) {
      TDLog.e("BLE callback: enable indicate null chrt");
      return false;
    }
    // TDLog.v( "BLE callback: indicate chrt " + chrt.getUuid().toString() + " indicate-able " + BleUtils.canChrtPIndicate( chrt ) );
    byte[] enable = BleUtils.getChrtPIndicate( chrt );
    if ( enable == null ) {
      TDLog.e("BLE callback: enable indicate null enable");
      return false;
    }
    return setNotification( chrt, enable );
  }

  // ----------------------------------------------------------------

  public boolean readChrt( UUID srvUuid, UUID chrtUuid )
  {
    // TDLog.f( "BLE read chrt");
    BluetoothGattCharacteristic chrt = getReadChrt( srvUuid, chrtUuid );
    try {
      return chrt != null && mGatt.readCharacteristic( chrt );
    } catch ( SecurityException e ) {
      TDLog.e("SECURITY read characteristic " + e.getMessage());
      // TDToast.makeBad("Security error: read characteristic");
      // TODO closeGatt() ?
    }
    return false;
  }

  public boolean writeChrt(  UUID srvUuid, UUID chrtUuid, byte[] bytes )
  {
    // TDLog.f( "BLE write chrt");
    BluetoothGattCharacteristic chrt = getWriteChrt( srvUuid, chrtUuid );
    if ( chrt == null ) {
      // TDLog.v( "BLE callback writeChrt null chrt ");
      return false;
    }
    int write_type = BleUtils.getChrtWriteType( chrt );
    if ( write_type < 0 ) {
      // TDLog.v( "BLE callback writeChrt neg type " + write_type );
      return false;
    }
    chrt.setWriteType( write_type );
    chrt.setValue( bytes );
    try {
      return mGatt.writeCharacteristic( chrt );
    } catch ( SecurityException e ) {
      TDLog.e("SECURITY write characteristic " + e.getMessage());
      // TDToast.makeBad("Security error: write characteristic");
      // TODO closeGatt() ?
    }
    return false;
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
    TDLog.e("BLE callback: callback " + name + " failure - status " + status );
    return false;
  }

  // -------------------------------------------------------------------------
  // UNUSED
  // private BluetoothGattCharacteristic getNotifyChrt( UUID srvUuid, UUID chrtUuid )
  // {
  //   if ( mGatt == null ) {
  //     return null;
  //   }
  //   BluetoothGattService srv = mGatt.getService( srvUuid );
  //   if ( srv  == null ) {
  //     return null;
  //   }
  //   BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
  //   if ( chrt == null ) {
  //     return null;
  //   }
  //   if ( ! BleUtils.canChrtPNotify( chrt ) ) {
  //     return null;
  //   }
  //   return chrt;
  // }

  public BluetoothGattCharacteristic getReadChrt( UUID srvUuid, UUID chrtUuid )
  {
    // TDLog.f( "BLE get read chrt");
    if ( mGatt == null ) {
      // TDLog.v( "BLE callback: null gatt");
      return null;
    }
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) {
      // TDLog.v( "BLE callback: null service");
      return null;
    }
    BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
    if ( chrt == null ) {
      // TDLog.v( "BLE callback: null read chrt");
      return null;
    }
    if ( ! BleUtils.canChrtPRead( chrt ) ) {
      TDLog.e("BLE callback: chrt " + chrtUuid.toString() + " without read property");
      return null;
    }
    return chrt;
  }

  public BluetoothGattCharacteristic getWriteChrt( UUID srvUuid, UUID chrtUuid )
  {
    // TDLog.f( "BLE get write chrt");
    if ( mGatt == null ) return null;
    BluetoothGattService srv = mGatt.getService( srvUuid );
    if ( srv  == null ) return null;
    BluetoothGattCharacteristic chrt = srv.getCharacteristic( chrtUuid );
    if ( chrt == null ) {
      // TDLog.v( "BLE callback: null write chrt");
      return null;
    }
    if ( ! BleUtils.canChrtPWrite( chrt ) ) {
      TDLog.e("BLE callback: chrt " + chrtUuid.toString() + " without write property");
      return null;
    }
    return chrt;
  }

}
