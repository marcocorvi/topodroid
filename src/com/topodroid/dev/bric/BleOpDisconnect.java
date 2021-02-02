/* @file BleOpDisconnect.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE disconnect operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.utils.TDLog; 

import android.content.Context;

import android.util.Log;

class BleOpDisconnect extends BleOperation 
{
  BleOpDisconnect( Context ctx, BleComm pipe )
  {
    super( ctx, pipe );
  }

  String name() { return "Disconnect"; }

  @Override 
  void execute()
  {
    // Log.v("DistoX-BLE_B", "BleOp exec disconnect");
    if ( mPipe == null ) { 
      TDLog.Error("BleOp disconnect error: null pipe" );
      return;
    }
    if ( mPipe != null ) mPipe.disconnectGatt();
  }
}
