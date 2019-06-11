/* @file BleProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid BLE devices protocol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.DistoX;

// import android.util.Log;

import android.content.Context;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothAdapter;
// import android.bluetooth.BluetoothGatt;
// import android.bluetooth.BluetoothGattService;
// import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCharacteristic;

// -----------------------------------------------------------------------------
class BleProtocol extends TopoDroidProtocol
{
  
  BleProtocol( Device device, Context context )
  {
    super( device, context );
  }

  public void handleWrite( BluetoothGattCharacteristic chrt )
  {
  }

  public void handleRead( BluetoothGattCharacteristic chrt )
  {
  }

  public void handleNotify( BluetoothGattCharacteristic chrt )
  {
  }

}
