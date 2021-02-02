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
import com.topodroid.dev.DataType;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BricProto extends TopoDroidProtocol
{
  private Device mDevice; // device of this communication
  private ConcurrentLinkedQueue< BleOperation > mOps;
  private BricComm mComm;
  private Handler mLister;
  private byte[] mLastTime;

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
  short mYear;
  char  mMonth, mDay, mHour, mMinute, mSecond, mCentisecond;


  public BricProto( Context ctx, TopoDroidApp app, Handler lister, Device device, BricComm comm )
  {
    super( device, ctx );
    mLister = lister;
    mComm   = comm;
    mIndex  = -1;
    mLastIndex = -1;
    mLastTime = null;
  }


  // -------------------------------------------------------
  // DATA
  void addMeasPrim( byte[] bytes ) 
  {
    // Log.v("DistoX-BLE-B", "BRIC proto: meas_prim " );
    mDistance = BricConst.getDistance( bytes );
    mBearing  = BricConst.getAzimuth( bytes );
    mClino    = BricConst.getClino( bytes );
  }

  void addMeasMeta( byte[] bytes ) 
  {
    // Log.v("DistoX-BLE-B", "BRIC proto: meas_meta " );
    mIndex = BricConst.getIndex( bytes );
    mRoll  = BricConst.getRoll( bytes );
    mDip   = BricConst.getDip( bytes );
  }

  void addMeasErr( byte[] bytes ) 
  {
    // Log.v("DistoX-BLE-B", "BRIC proto: meas_err " );
  }

  void setLastTime( byte[] bytes )
  {
    // Log.v("DistoX-BLE-B", "BRIC proto: set last time " + BleUtils.bytesToString( bytes ) );
    mLastTime = Arrays.copyOfRange( bytes, 0, bytes.length );
  }

  void clearLastTime() { mLastTime = null; }

  byte[] getLastTime() { return mLastTime; }
  
  void processData()
  {
    if ( mIndex == mLastIndex ) {
      // Log.v("DistoX-BLE-B", "BRIC proto: skip data index " + mIndex + " last " + mLastIndex );
      return;
    }
    // Log.v("DistoX-BLE-B", "BRIC proto: process data index " + mIndex + " last " + mLastIndex );
    if ( mIndex != mLastIndex ) {
      mLastIndex = mIndex;
      // Log.v("DistoX-BLE-B", "BRIC proto send data to the app thru comm");
      mComm.handleRegularPacket( DataType.PACKET_DATA, mLister, DataType.DATA_SHOT );
    }
  }

  byte[] mPrevBytes = new byte[4]; // from TopoDroidProtocol double ... 

  void addMeasPrimAndProcess( byte[] bytes )
  {
    boolean same_time = true;
    for ( int k=0; k<4; ++k ) { // check only hour, minute, second, and centisecond
      if ( mPrevBytes[k] != bytes[4+k] ) same_time = false;
      mPrevBytes[k] = bytes[4+k];
    }
    if ( ! same_time ) {
      mDistance = BricConst.getDistance( bytes );
      mBearing  = BricConst.getAzimuth( bytes );
      mClino    = BricConst.getClino( bytes );
      mComm.handleRegularPacket( DataType.PACKET_DATA, mLister, DataType.DATA_SHOT );
    }
  }

}

