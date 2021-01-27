/* @file BleOperation.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE abstract operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import android.content.Context;

// import android.bluetooth.BluetoothDevice;

abstract class BleOperation 
{
  protected Context mContext;
  protected BleComm mPipe;

  BleOperation( Context ctx, BleComm pipe )
  {
    mContext = ctx;
    mPipe    = pipe;
  }

  abstract void execute();
}
