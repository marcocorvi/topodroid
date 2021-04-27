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
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog; 

import android.content.Context;

import android.util.Log;

public class BleOpDescRead extends BleOperation 
{
  public BleOpDescRead( Context ctx, BleComm pipe )
  {
    super( ctx, pipe );
  }

  // public String name() { return "DescRead"; }

  @Override 
  public void execute()
  {
    TDLog.Log( TDLog.LOG_BT, "BleOp exec desc read");
  }
}
