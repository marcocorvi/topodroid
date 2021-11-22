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
import com.topodroid.dev.ble.BleOperation;
import com.topodroid.dev.ble.BleCallback;
// import com.topodroid.dev.ble.BleUtils;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.TDToast;
import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

// import android.os.Looper;
import android.os.Handler;
import android.content.Context;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothGatt;
// import android.bluetooth.BluetoothGattService;
// import android.bluetooth.BluetoothGattCharacteristic;
// import android.bluetooth.BluetoothGattDescriptor;
// import android.bluetooth.BluetoothGattCallback;

// import java.util.ArrayList;
import java.util.Arrays;
// import java.util.List;
// import java.util.UUID;
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
  private int mErr1; // first error code
  private int mErr2; // second error code
  private float mErrVal1;
  private float mErrVal2;
  // private float mErrSecondVal1;
  // private float mErrSecondVal2;
  private String mComment = null;

  private Context mContext;
  BleCallback mCallback;

  // data struct
  private int   mLastIndex = 0x7ffffffe;
  private int   mIndex = -1;
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
    // mIndex  = -1;
    // mLastIndex = 7ffffffe;
    mLastTime = null;
    mLastPrim = null; // new byte[20];
  }


  // DATA -------------------------------------------------------

  /* check if the bytes coincide with the last Prim
   * @return true if the bytes are equal to the last Prim
   * @note the last Prim is always filled with the new bytes on exit
   *       and mThisTime is set to the new timestamp
   */ 
  private boolean checkPrim( byte[] bytes )
  {
    if ( Arrays.equals( mLastPrim, bytes ) ) {
      return false;
    }
    mThisTime = BricConst.getTimestamp( bytes ); // first 8 bytes
    mLastPrim = Arrays.copyOf( bytes, 20 );
    return true;
  }

  void addMeasPrim( byte[] bytes ) 
  {
    if ( checkPrim( bytes ) ) { // if Prim is new
      if ( mPrimToDo ) {        // and there is a previous Prim unprocessed
        processData();
      }
      mTime     = mThisTime;
      mDistance = BricConst.getDistance( bytes );
      mBearing  = BricConst.getAzimuth( bytes );
      mClino    = BricConst.getClino( bytes );
      mPrimToDo = true;
      mErr1 = 0;
      mErr2 = 0;
      // TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: added Prim " + mDistance + " " + mBearing + " " + mClino );
      TDLog.v( "BRIC proto: meas_prim " +  mDistance + " " + mBearing + " " + mClino );
    } else {
      // TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: add Prim - repeated primary" );
      TDLog.v( "BRIC proto: add Prim - repeated primary" );
    }
  }

  void addMeasMeta( byte[] bytes ) 
  {
    mIndex   = BricConst.getIndex( bytes );
    mRoll    = BricConst.getRoll( bytes );
    mDip     = BricConst.getDip( bytes );
    mType    = BricConst.getType( bytes ); // 0: regular shot, 1: scan shot
    mSamples = BricConst.getSamples( bytes );
    // TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: added Meta " + mIndex + " type " + mType );
    TDLog.v( "BRIC proto: added Meta " + mIndex + "/" + mLastIndex + " type " + mType );
    if ( mType == 0 ) { 
      if ( mIndex > mLastIndex+1 ) { // LOST SHOTS
        TDLog.Error("BRIC proto: missed data, last " + mLastIndex + " current " + mIndex );
        if ( TDSetting.mBricMode != BricMode.MODE_PRIM_ONLY && TopoDroidApp.mMainActivity != null ) {
          final String lost = "missed " + (mIndex - mLastIndex -1) + " data";
          TopoDroidApp.mMainActivity.runOnUiThread( new Runnable() { 
            public void run() { TDToast.makeBad( lost ); }
          } );
        }
      }
    }
  }

  void addMeasErr( byte[] bytes ) 
  {
    // TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: added Err " );
    TDLog.v( "BRIC proto: added Err " );
    mErr1 = BricConst.firstErrorCode( bytes );
    mErr2 = BricConst.secondErrorCode( bytes );
    mErrVal1 = BricConst.firstErrorValue( bytes, mErr1 );
    mErrVal2 = BricConst.secondErrorValue( bytes, mErr2 );
    // mErrSecondVal1 = BricConst.firstErrorSecondValue( bytes, mErr1 );
    // mErrSecondVal2 = BricConst.secondErrorSecondValue( bytes, mErr2 );
    mComment = ( mErr1 > 0 || mErr2 > 0 )? BricConst.errorString( bytes ) : null;
  }
  
  // TODO use mType
  void processData()
  {
    TDLog.v( "BRIC proto process data - prim todo " + mPrimToDo + " index " + mIndex + " type " + mType );
    if ( mPrimToDo ) {
      if ( TDSetting.mBricZeroLength || mDistance > 0.01 ) {
        TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: process - PrimToDo true: " + mIndex + " prev " + mLastIndex );
        // mComm.handleRegularPacket( DataType.PACKET_DATA, mLister, DataType.DATA_SHOT );
        int index = ( TDSetting.mBricMode == BricMode.MODE_NO_INDEX )? -1 : mIndex;
        float clino = 0;
        float azimuth = 0;
        if ( mErr1 >= 14 || mErr2 >= 14 ) { 
          clino   = ( mErr1 == 14 )? mErrVal1 : (mErr2 == 14)? mErrVal2 : 0;
          azimuth = ( mErr1 == 15 )? mErrVal1 : (mErr2 == 15)? mErrVal2 : 0;
        }
        int data_type = ( mType == 1 )? DataType.DATA_SCAN : DataType.DATA_SHOT;
        if ( ! mComm.handleBricPacket( index, mLister, data_type, clino, azimuth, mComment ) ) {
          TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: skipped existing index " + index );
        }
      } else {
        TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: skipping 0-length data");
      }
      mPrimToDo = false;
      mLastIndex = mIndex;
    } else if ( mIndex == mLastIndex ) {
      TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: process - PrimToDo false: ... repeated " + mIndex);
    } else {
      TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: process - PrimToDo false: ... skip " + mIndex + " prev " + mLastIndex );
      // if ( TDSetting.mBricMode == BricMode.MODE_ALL_ZERO || TDSetting.mBricMode == BricMode.MODE_ZERO_NO_INDEX ) {
      //   int index = ( TDSetting.mBricMode == BricCModeMODE_ZERO_NO_INDEX )? -1 : mIndex;
      //   mComm.handleZeroPacket( mIndex, mLister, DataType.DATA_SHOT );
      // }
      mLastIndex = mIndex;
    }
  }

  void addMeasPrimAndProcess( byte[] bytes )
  {
    if ( checkPrim( bytes ) ) { // if Prim is new
      TDLog.v( "BRIC proto: add Prim and process" );
      mTime     = mThisTime;
      mDistance = BricConst.getDistance( bytes );
      mBearing  = BricConst.getAzimuth( bytes );
      mClino    = BricConst.getClino( bytes );
      if ( TDSetting.mBricZeroLength || mDistance > 0.01 ) { 
        mComm.handleRegularPacket( DataType.PACKET_DATA, mLister, DataType.DATA_SHOT );
      } else { 
        TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: skipping 0-length data");
      }
    } else {
      TDLog.Log( TDLog.LOG_PROTO, "BRIC proto: add & process - repeated prim: ... skip");
    }
  }

  void setLastTime( byte[] bytes )
  {
    // TDLog.v( "BRIC proto: set last time " + BleUtils.bytesToString( bytes ) );
    mLastTime = Arrays.copyOfRange( bytes, 0, bytes.length );
  }

  void clearLastTime() { mLastTime = null; }

  byte[] getLastTime() { return mLastTime; }

  

}

