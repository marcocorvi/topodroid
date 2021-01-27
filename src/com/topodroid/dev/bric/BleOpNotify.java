/* @file BleOpNotify.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE notify operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.utils.TDLog;
import com.topodroid.DistoX.R;

import android.content.Context;

import android.bluetooth.BluetoothGattCharacteristic;

import android.util.Log;

import java.util.UUID;

class BleOpNotify extends BleOperation 
{
  BluetoothGattCharacteristic mChrt;
  boolean mEnable;

  BleOpNotify( Context ctx, BleComm pipe, BluetoothGattCharacteristic chrt, boolean enable )
  {
    super( ctx, pipe );
    mChrt = chrt;
    mEnable = enable;
  }

  @Override 
  void execute()
  {
    // Log.v("BRIC", "exec notify " + mEnable );
    if ( mPipe == null ) { 
      TDLog.Error("BRIC error: notify null pipe" );
      return;
    }
    if ( mEnable ) {
      mPipe.enableNotify( mChrt );
    } else {
      // mPipe.disableNotify( mChrt );
    }
  }
}
