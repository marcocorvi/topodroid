/* @file BleOpChrtWrite.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth LE characteristic write operation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog; 

import android.content.Context;

// import android.bluetooth.BluetoothDevice;

import java.util.UUID;
import java.util.Arrays;

public class BleOpChrtWrite extends BleOperation 
{
  byte[] bytes;
  UUID   mSrvUuid;
  UUID   mChrtUuid;

  public BleOpChrtWrite( Context ctx, BleComm pipe, UUID srv_uuid, UUID chrt_uuid, byte[] b )
  {
    super( ctx, pipe );
    mSrvUuid  = srv_uuid;
    mChrtUuid = chrt_uuid;
    bytes = Arrays.copyOf( b, b.length );
  }

  // public String name() { return "ChrtWrite"; }

  @Override 
  public void execute()
  {
    if ( mPipe == null ) { 
      TDLog.Error( "BleOp chrt write error: null pipe" );
      return;
    }
    // boolean ret = 
      mPipe.writeChrt( mSrvUuid, mChrtUuid, bytes );
    // TDLog.v( "BleOp exec chrt write: ret " + ret );
  }
}
