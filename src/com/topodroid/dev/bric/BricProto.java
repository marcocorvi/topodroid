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
  private byte[] mLastTime;       // content of LastTime payload
  private byte[] mLastPrim;   // used to check if the coming Prim is new
  private boolean mPrimToDo = false;

  private Context mContext;
  BleCallback mCallback;

  // data struct
  private int   mIndex;
  private long  mThisTime; // data timestamp [msec]
  long mTime = 0;          // timestamp of data that must be processed
  // float mDistance; // from TopoDroidProtocol double ...
  // float mBearing;
  // float mClino;
  // float mRoll;
  // float mDip;

  // unused
  // short mYear;
  // char  mMonth, mDay, mHour, mMinute, mSecond, mCentisecond;


  public BricProto( Context ctx, TopoDroidApp app, Handler lister, Device device, BricComm comm )
  {
    super( device, ctx );
    mLister = lister;
    mComm   = comm;
    mIndex  = -1;
    mLastTime = null;
    mLastPrim = new byte[20];
  }


  // DATA -------------------------------------------------------

  /* check if the bytes coincide with the last Prim
   * @return true if the bytes are equal to the last Prim
   * @note the last Prim is always filled with the new bytes on exit
   */ 
  private boolean checkPrim( byte[] bytes )
  {
    mThisTime = BricConst.getTimestamp( bytes ); // first 8 bytes
    if ( mTime != mThisTime ) {
      for ( int h=0; h<20; ++h ) mLastPrim[h] = bytes[h];
      return true;
    }
    for ( int k=8; k<20; ++k ) { // data bytes
      if ( bytes[k] != mLastPrim[k] ) {
        for ( int h=k; h<20; ++h ) mLastPrim[h] = bytes[h];
        return true;
      }
    }
    return false;
  }

  void addMeasPrim( byte[] bytes ) 
  {
    if ( checkPrim( bytes ) ) { // if Prim is new
      if ( mPrimToDo ) {        // and there is a previous Prim unprocessed
        processData();
      }
      // Log.v("DistoX-BLE-B", "BRIC proto: meas_prim " );
      mTime     = mThisTime;
      mDistance = BricConst.getDistance( bytes );
      mBearing  = BricConst.getAzimuth( bytes );
      mClino    = BricConst.getClino( bytes );
      mPrimToDo = true;
    } else {
      Log.v("DistoX-BLE-B", "BRIC proto: add Prim - repeated primary" );
    }
  }

  void addMeasMeta( byte[] bytes ) 
  {
    // Log.v("DistoX-BLE-B", "BRIC proto: add Meta " );
    mIndex = BricConst.getIndex( bytes );
    mRoll  = BricConst.getRoll( bytes );
    mDip   = BricConst.getDip( bytes );
  }

  void addMeasErr( byte[] bytes ) 
  {
    // Log.v("DistoX-BLE-B", "BRIC proto: add Err " );
  }
  
  void processData()
  {
    if ( mPrimToDo ) {
      // Log.v("DistoX-BLE-B", "BRIC proto send data to the app thru comm");
      mComm.handleRegularPacket( DataType.PACKET_DATA, mLister, DataType.DATA_SHOT );
      mPrimToDo = false;
    } else {
      Log.v("DistoX-BLE-B", "BRIC proto: process - PrimToDo false: ... skip");
    }
  }

  void addMeasPrimAndProcess( byte[] bytes )
  {
    mTime = mThisTime;
    if ( checkPrim( bytes ) ) { // if Prim is new
      // Log.v("DistoX-BLE-B", "BRIC proto: add Prim " );
      mTime     = mThisTime;
      mDistance = BricConst.getDistance( bytes );
      mBearing  = BricConst.getAzimuth( bytes );
      mClino    = BricConst.getClino( bytes );
      mComm.handleRegularPacket( DataType.PACKET_DATA, mLister, DataType.DATA_SHOT );
    } else {
      Log.v("DistoX-BLE-B", "BRIC proto: add+process - Prim repeated: ... skip");
    }
  }

  void setLastTime( byte[] bytes )
  {
    // Log.v("DistoX-BLE-B", "BRIC proto: set last time " + BleUtils.bytesToString( bytes ) );
    mLastTime = Arrays.copyOfRange( bytes, 0, bytes.length );
  }

  void clearLastTime() { mLastTime = null; }

  byte[] getLastTime() { return mLastTime; }

}

