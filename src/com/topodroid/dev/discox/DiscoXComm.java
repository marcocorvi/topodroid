/* @file DiscoXComm.java
 *
 * @author marco corvi
 * @date dec 2025
 *
 * @brief TopoDroid DiscoX communication - STUB
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * WARNING: This is a stub class for DiscoX support
 */
package com.topodroid.dev.discox;

import com.topodroid.utils.TDLog;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.dev.TopoDroidComm;

import android.bluetooth.BluetoothDevice;

// Stub class for DiscoX communication - to be implemented
public class DiscoXComm extends TopoDroidComm
{
  /** constructor
   * @param app        application
   * @param address    device address
   * @param bt_device  bluetooth device
   */
  public DiscoXComm( TopoDroidApp app, String address, BluetoothDevice bt_device )
  {
    super( app );
    TDLog.v( "DiscoXComm: stub constructor - DiscoX support not yet implemented" );
  }

  public void disconnectGatt()
  {
    // stub
  }
}
