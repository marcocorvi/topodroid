/* @file BleOpMtuRequest.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE MTU request operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.utils.TDLog;

import android.content.Context;

import android.util.Log;

class BleOpMtuRequest extends BleOperation 
{
  BleOpMtuRequest( Context ctx, BleComm pipe )
  {
    super( ctx, pipe );
  }

  @Override 
  void execute()
  {
    Log.v("BRIC", "exec MTU request" );
  }
}
