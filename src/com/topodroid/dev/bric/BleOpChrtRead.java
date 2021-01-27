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

  @Override 
  void execute()
  {
    // Log.v("BLEX", "exec chrt read");
    if ( mPipe == null ) { Log.v("BLEX", "ERROR null pipe" ); return; }
    mPipe.readChrt( mSrvUuid, mChrtUuid );
  }
}
