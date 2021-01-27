/* @file BricProto.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BRIC4 protocol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidProtocol;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.utils.TDLog;

import android.os.Looper;
import android.os.Handler;
import android.content.Context;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCallback;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BricProto extends TopoDroidProtocol
{
  private Device mDevice; // device of this communication
  private ConcurrentLinkedQueue< BleOperation > mOps;
  private BricComm mComm;
  private Handler mLister;

  private Context mContext;
  BleCallback mCallback;

  // data struct
  int   mIndex;
  // float mDistance; // from TopoDroidProtocol double ...
  // float mBearing;
  // float mClino;
  // float mRoll;
  // float mDip;
  int   mLastIndex;


  public BricProto( Context ctx, TopoDroidApp app, Handler lister, Device device, BricComm comm )
  {
    super( device, ctx );
    mLister = lister;
    mComm   = comm;
    mIndex  = -1;
    mLastIndex = -1;
  }

  public boolean sendCommand( byte cmd )
  {
    switch ( cmd ) {
      case BricConst.CMD_SCAN:  return mComm.sendCommand( BricConst.COMMAND_SCAN ); 
      case BricConst.CMD_SHOT:  return mComm.sendCommand( BricConst.COMMAND_SHOT ); 
      case BricConst.CMD_LASER: return mComm.sendCommand( BricConst.COMMAND_LASER );
      case BricConst.CMD_OFF:   return mComm.sendCommand( BricConst.COMMAND_OFF );  
    }
    return false;
  }


  // -------------------------------------------------------
  // DATA
  void addMeasPrim( byte[] bytes ) 
  {
    mDistance = BricConst.getDistance( bytes );
    mBearing  = BricConst.getAzimuth( bytes );
    mClino    = BricConst.getClino( bytes );
  }

  void addMeasMeta( byte[] bytes ) 
  {
    mIndex = BricConst.getIndex( bytes );
    mRoll  = BricConst.getRoll( bytes );
    mDip   = BricConst.getDip( bytes );
  }

  void addMeasErr( byte[] bytes ) 
  {
  }
  
  void processData()
  {
    if ( mIndex == mLastIndex ) return;
    Log.v("BRIC", "process data index " + mIndex + " last " + mLastIndex );
    mLastIndex = mIndex;
    // TODO send data to the app / mLister
  }

}

