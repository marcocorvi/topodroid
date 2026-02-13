/* @file BleComm.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE callback interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.dev.ConnectionState;
import com.topodroid.utils.TDLog;

import android.content.Context;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
// import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
// import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

import com.topodroid.dev.TopoDroidComm;

abstract public class BleComm extends TopoDroidComm
{
  protected Context     mContext  = null;
  protected BleOpsQueue mQps      = null;
  protected BleCallback mCallback = null;
  protected BleQueue    mQueue    = null;

  private boolean mUseMtu;
  protected UUID mServiceUuid;
  protected UUID mChrtReadUuid;
  protected UUID mChrtWriteUuid;

  protected String          mRemoteAddress  = null;
  protected BluetoothDevice mRemoteBtDevice = null;

  public BleComm( TopoDroidApp app, boolean use_mtu, UUID service_uuid, UUID chrt_read_uuid, UUID chrt_write_uuid )
  {
    super( app );
    // mQps = new BleOpsQueue;
    mUseMtu = use_mtu;
    mServiceUuid   = service_uuid;
    mChrtReadUuid  = chrt_read_uuid;
    mChrtWriteUuid = chrt_write_uuid;
    mQueue = new BleQueue();
    TDLog.v("BLE comm. use MTU " + use_mtu );
  }


  /** react to a change in the MTU (empty)
   * @param mtu   MTU
   * @note called from the BleCallback
   */
  public void changedMtu( int mtu ) 
  {
    TDLog.v("BLE comm: changed MTU");
    if ( mUseMtu ) {
      mQps.enqueueOp( new BleOpNotify( mContext, this, mServiceUuid, mChrtReadUuid, true ) );
    }
    mQps.clearPending();
  }

  /** notified that the remote RSSI has been read (Received Signal Strength Indicator)
   * @param rssi   remote rssi
   */
  public void readedRemoteRssi( int rssi )
  { 
    TDLog.v("BLE comm: readed remote RSSI");
    mQps.clearPending();
  }

  // BleChrtChanged
  abstract public void changedChrt( BluetoothGattCharacteristic chrt );

  abstract public void readedChrt(  String uuid_str, byte[] bytes );

  public void writtenChrt( String uuid_str, byte[] bytes ) { mQps.clearPending(); }

  public void readedDesc(  String uuid_str, String uuid_chrt_str, byte[] bytes )
  { 
    TDLog.v("BLE comm: readed desc");
    mQps.clearPending();
  }

  abstract public void writtenDesc( String uuid_str, String uuid_chrt_str, byte[] bytes );

  public void completedReliableWrite()
  {
    TDLog.v("BLE comm: completed reliable write");
    mQps.clearPending();
  }

  abstract public void disconnected();

  abstract public void connected();

  abstract public int servicesDiscovered( BluetoothGatt gatt );

  public boolean readChrt(  UUID srv_uuid, UUID chrt_uuid )
  {
    TDLog.v("BLE comm: read chrt");
    return mCallback.readChrt( srv_uuid, chrt_uuid ); 
  }

  public boolean writeChrt( UUID srv_uuid, UUID chrt_uuid, byte[] bytes )
  {
    TDLog.v("BLE comm: write chrt");
    return mCallback.writeChrt( srv_uuid, chrt_uuid, bytes );
  }

  // boolean enablePNotify( UUID srcUuid, BluetoothGattCharacteristic chrt );
  public boolean enablePNotify( UUID srvUuid, UUID chrtUuid )
  { 
    TDLog.v("BLE comm: enable notify");
    return mCallback.enablePNotify( srvUuid, chrtUuid );
  }

  public boolean enablePIndicate( UUID srvUuid, UUID chrtUuid )
  {
    TDLog.v("BLE comm: enable indicate");
    return mCallback.enablePIndicate( srvUuid, chrtUuid );
  }

  public void connectGatt( Context ctx, BluetoothDevice bt_device )
  {
    TDLog.v("BLE comm: connect GATT");
    mContext = ctx;
    mCallback.connectGatt( mContext, bt_device );
  }

  public void disconnectGatt()
  {
    TDLog.v("BLE comm: disconnect GATT");
    notifyStatus( ConnectionState.CONN_DISCONNECTED );
    mCallback.closeGatt();
  }


  // this is cavway distoxble implementtaion, BRIC and SAP return always false (not-implemented)
  public boolean requestMtu( int mtu )
  { 
    TDLog.v("BLE comm: request MTU " + mtu );
    return mCallback.requestMtu( mtu );
  }

  // recoverable error
  abstract public void error( int status, String extra, String what );

  // unrecoverable error
  abstract public void failure( int status, String extra, String what );

  // void addService( BluetoothGattService srv );
  // void addChrt( UUID srv_uuid, BluetoothGattCharacteristic chrt );
  // void addDesc( UUID srv_uuid, UUID chrt_uuid, BluetoothGattDescriptor desc );

  // defined in TopoDroidComm
  // public void notifyStatus( int status );
}
