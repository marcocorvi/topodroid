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
package com.topodroid.dev.bric;


import android.content.Context;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import android.util.Log;

import java.util.UUID;

public interface BleComm
{
  public void changedMtu( int mtu );
  public void readedRemoteRssi( int rssi );

  public void changedChrt( BluetoothGattCharacteristic chrt );

  public void readedChrt( String uuid_str, byte[] bytes );
  public void writtenChrt( String uuid_str, byte[] bytes );
  public void readedDesc( String uuid_str, byte[] bytes );
  public void writtenDesc( String uuid_str, byte[] bytes );

  public void completedReliableWrite();
  public void disconnected();

  public void servicesDiscovered( BluetoothGatt gatt );

  public boolean readChrt( UUID srvUuid, UUID chrtUuid );
  public boolean writeChrt( UUID srv_uuid, UUID chrt_uuid, byte[] bytes );

  public boolean enablePNotify( UUID srcUuid, BluetoothGattCharacteristic chrt );

  public void connectGatt( Context ctx, BluetoothDevice device );
  public void disconnectGatt();

  // recoverable error
  public void error( int status );

  // unrecoverable error
  public void failure( int status );

  // public void addService( BluetoothGattService srv );
  // public void addChrt( UUID srv_uuid, BluetoothGattCharacteristic chrt );
  // public void addDesc( UUID srv_uuid, UUID chrt_uuid, BluetoothGattDescriptor desc );
}
