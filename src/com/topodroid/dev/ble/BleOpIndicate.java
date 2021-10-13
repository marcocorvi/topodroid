/* @file BleOpIndicate.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE indicate operation 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog;
// import com.topodroid.DistoX.R;

import android.content.Context;

// import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public class BleOpIndicate extends BleOperation 
{
  boolean mEnable;
  UUID mSrvUuid;
  UUID mChrtUuid;
  // BluetoothGattCharacteristic mChrt = null;

  // BleOpIndicate( Context ctx, BleComm pipe, UUID srvUuid, BluetoothGattCharacteristic chrt, boolean enable )
  // {
  //   super( ctx, pipe );
  //   mSrvUuid  = srvUuid;
  //   mChrtUuid = chrt.getUuid();
  //   mChrt = chrt;
  //   mEnable   = enable;
  // }

  public BleOpIndicate( Context ctx, BleComm pipe, UUID srvUuid, UUID chrtUuid, boolean enable )
  {
    super( ctx, pipe );
    mSrvUuid  = srvUuid;
    mChrtUuid = chrtUuid;
    // mChrt     = null;
    mEnable   = enable;
  }

  // public String name() { return "Notify"; }

  @Override 
  public void execute()
  {
    // TDLog.v( "BleOp exec indicate " + mEnable + " " + mChrtUuid.toString() );
    if ( mPipe == null ) { 
      TDLog.Error("BleOp notify error: null pipe " + mChrtUuid.toString() );
      return;
    }
    if ( mEnable ) {
      mPipe.enablePIndicate( mSrvUuid, mChrtUuid );
    } else {
      // mPipe.disablePNotify( mSrvUuid, mChrtUuid );
    }
  }
}
