/* @file DiscoXProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DiscoX5 protocol REQUIRES API-18 - uses SAP
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.dev.discox;

// import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
// import com.topodroid.dev.DataType;
// import com.topodroid.dev.TopoDroidProtocol;

import com.topodroid.dev.sap.SapProtocol;

// // import android.os.Handler;
import android.content.Context;

// // import android.bluetooth.BluetoothDevice;
// // import android.bluetooth.BluetoothAdapter;
// // import android.bluetooth.BluetoothGatt;
// // import android.bluetooth.BluetoothGattService;
// // import android.bluetooth.BluetoothGattDescriptor;
// import android.bluetooth.BluetoothGattCharacteristic;

// import java.nio.ByteOrder;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Locale;
// import java.nio.ByteBuffer;
// import java.nio.FloatBuffer;

// -----------------------------------------------------------------------------
class DiscoXProtocol extends SapProtocol
{
  // private SapComm mComm; // UNUSED
  // ArrayList< byte[] > mWriteBuffer;  // write buffer
  
  /** cstr
   * @param comm    communication class
   * @param device  BT device
   * @param context context
   */
  DiscoXProtocol( DiscoXComm comm, Device device, Context context )
  {
    super( comm, device, context );
  }

}

