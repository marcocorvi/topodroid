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
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog; 

import android.content.Context;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

public class BleOpChrtRead extends BleOperation 
{
  UUID mSrvUuid;
  UUID mChrtUuid;

  public BleOpChrtRead( Context ctx, BleComm pipe, UUID srv_uuid, UUID chrt_uuid )
  {
    super( ctx, pipe );
    mSrvUuid  = srv_uuid;
    mChrtUuid = chrt_uuid;
  }

  // public String name() { return "ChrtRead"; }

  @Override 
  public void execute()
  {
    // TDLog.v( "BleOp exec read on chrt " + mChrtUuid.toString() );
    if ( mPipe == null ) { 
      TDLog.Error("BleOp chrt read: ERROR null pipe" );
      return;
    }
    mPipe.readChrt( mSrvUuid, mChrtUuid );
  }
}
