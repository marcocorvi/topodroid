/* @file BleOpDescRead.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE descriptor read operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.utils.TDLog; 

import android.content.Context;

import android.util.Log;

class BleOpDescRead extends BleOperation 
{
  BleOpDescRead( Context ctx, BleComm pipe )
  {
    super( ctx, pipe );
  }

  String name() { return "DescRead"; }

  @Override 
  void execute()
  {
    Log.v("DistoX-BLE-B", "BleOp exec desc read");
  }
}
