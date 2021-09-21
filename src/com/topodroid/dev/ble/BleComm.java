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

import android.content.Context;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

public interface BleComm // extends BleChrtChanged
{
  void changedMtu( int mtu );
  void readedRemoteRssi( int rssi );

  // BleChrtChanged
  void changedChrt( BluetoothGattCharacteristic chrt );

  void readedChrt(  String uuid_str, byte[] bytes );
  void writtenChrt( String uuid_str, byte[] bytes );
  void readedDesc(  String uuid_str, String uuid_chrt_str, byte[] bytes );
  void writtenDesc( String uuid_str, String uuid_chrt_str, byte[] bytes );

  void completedReliableWrite();
  void disconnected();
  void connected();

  int servicesDiscovered( BluetoothGatt gatt );

  boolean readChrt(  UUID srv_uuid, UUID chrt_uuid );
  boolean writeChrt( UUID srv_uuid, UUID chrt_uuid, byte[] bytes );

  // boolean enablePNotify( UUID srcUuid, BluetoothGattCharacteristic chrt );
  boolean enablePIndicate( UUID src_uuid, UUID chrt_uuid );
  boolean enablePNotify( UUID src_uuid, UUID chrt_uuid );

  void connectGatt( Context ctx, BluetoothDevice device );
  void disconnectGatt();

  // recoverable error
  void error( int status, String extra );

  // unrecoverable error
  void failure( int status, String extra );

  // void addService( BluetoothGattService srv );
  // void addChrt( UUID srv_uuid, BluetoothGattCharacteristic chrt );
  // void addDesc( UUID srv_uuid, UUID chrt_uuid, BluetoothGattDescriptor desc );

  void notifyStatus( int status );
}
