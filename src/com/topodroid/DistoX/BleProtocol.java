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

import android.util.Log;

// import android.os.Handler;
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
  BleComm mComm;
  
  BleProtocol( BleComm comm, Device device, Context context )
  {
    super( device, context );
    mComm = comm;
    // Log.v("DistoXBLE", "new proto");
  }

  public int handleWrite( BluetoothGattCharacteristic chrt )
  {
    Log.v("DistoXBLE", "proto write");
    return 0;
  }

  public int handleRead( BluetoothGattCharacteristic chrt )
  {
    byte[] bytes = chrt.getValue();
    for ( int k=0; k<8; ++k ) mBuffer[k] = bytes[k];
    return handlePacket();
    // Log.v("DistoXBLE", "proto read. ret " + ret );
  }

  public void handleNotify( BluetoothGattCharacteristic chrt )
  {
    Log.v("DistoXBLE", "proto notify");
  }

}
