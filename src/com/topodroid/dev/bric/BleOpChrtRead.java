/* @file BleOpChrtRead.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE characteristic read operation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.utils.TDLog; 

import android.content.Context;

import android.bluetooth.BluetoothDevice;

import android.util.Log;

import java.util.UUID;

class BleOpChrtRead extends BleOperation 
{
  UUID mSrvUuid;
  UUID mChrtUuid;

  BleOpChrtRead( Context ctx, BleComm pipe, UUID srv_uuid, UUID chrt_uuid )
  {
    super( ctx, pipe );
    mSrvUuid  = srv_uuid;
    mChrtUuid = chrt_uuid;
  }

  String name() { return "ChrtRead"; }

  @Override 
  void execute()
  {
    // Log.v("DistoX-BLE_B", "BleOp exec chrt read");
    if ( mPipe == null ) { 
      TDLog.Error("BleOp chrt read: ERROR null pipe" );
      return;
    }
    mPipe.readChrt( mSrvUuid, mChrtUuid );
  }
}
