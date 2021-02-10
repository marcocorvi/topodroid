/* @file BleOpDescWrite.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE descriptor write operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog; 

import android.content.Context;

import android.util.Log;

import java.util.Arrays;

public class BleOpDescWrite extends BleOperation 
{
  byte[] bytes;

  public BleOpDescWrite( Context ctx, BleComm pipe, byte[] b )
  {
    super( ctx, pipe );
    bytes = Arrays.copyOf( b, b.length );
  }

  // public String name() { return "DescWrite"; }

  @Override 
  public void execute()
  {
    Log.v("DistoX-BLE", "BleOp exec desc write");
  }
}
