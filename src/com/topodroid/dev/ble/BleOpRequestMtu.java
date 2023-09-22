/* @file BleOpRequestMtu.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetooth request MTU operation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog; 

import android.content.Context;

// import android.bluetooth.BluetoothDevice;

// import java.util.UUID;

public class BleOpRequestMtu extends BleOperation 
{
  int mMtu; 

  public BleOpRequestMtu( Context ctx, BleComm pipe, int mtu )
  {
    super( ctx, pipe );
    mMtu = mtu;
  }

  public String name() { return "RequestMtu"; }

  @Override 
  public void execute()
  {
    // TDLog.v( "BleOp exec request MTU");
    if ( mPipe == null ) { 
      TDLog.e("BleOp request MTU: ERROR null pipe" );
      return;
    }
    mPipe.requestMtu( mMtu );
  }
}
