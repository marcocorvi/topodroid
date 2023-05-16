/* @file DeviceX310TakeShot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX310 shooting class
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox2;

import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
// import com.topodroid.dev.DataType;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.ILister;
import com.topodroid.TDX.ListerHandler;
import com.topodroid.TDX.TopoDroidApp;


import android.os.AsyncTask;
 
public class DeviceX310TakeShot extends AsyncTask<Integer, Integer, Integer >
{
  private final ILister mILister;      // lister with BT button
  private final ListerHandler mLister; // lister that manages downloaded shots (if null shots are not downloaded)
  private final TopoDroidApp  mApp;    // FIXME LEAK
  private final int mNr;               // number of shots to measure before download
  private final int mDataType;
 
  /** cstr
   * @param ilister    ???
   * @param lister     ???
   * @param app        application
   * @param nr         number of shots
   * @param data_type  expected data type
   */
  public DeviceX310TakeShot( ILister ilister, ListerHandler lister, TopoDroidApp app, int nr, int data_type )
  {
    super();
    mILister  = ilister; 
    mLister   = lister; 
    mApp      = app;
    mNr       = nr;
    mDataType = data_type;
  } 

  // 0 off
  // 1 on
  // 2 measure
  // 3 measure and download
  //
  // @note this calls app.setX310Laser() which in turn calls comm.setX310Laser()
  @Override
  protected Integer doInBackground( Integer... ii )
  {
    int i = mNr;
    int wait_laser = TDSetting.mWaitLaser;
    int wait_shot  = TDSetting.mWaitShot;
    // if ( TDInstance.isDeviceX310() ) { // NOTE this is not necessary
    //   if ( wait_laser < 500 + mNr*500 ) wait_laser = 500 + mNr*500;
    //   if ( wait_shot  < 1000 ) wait_shot  = 1000;
    // }
    // if ( mNr > 1 && mDataType == DataType.DATA_CALIB ) {
      TDUtil.slowDown( wait_shot );
    // }
    for ( ; i>1; --i ) {
      // TDLog.v( "X310 take shot " + i + " wait " + wait_laser + "/" + wait_shot );
      if ( ! mApp.setX310Laser( Device.LASER_ON, 0, null, mDataType, false ) ) return i;
      TDUtil.slowDown( wait_laser ); 
      if ( ! mApp.setX310Laser( Device.MEASURE, 0, null, mDataType, false ) ) return i;   
      TDUtil.slowDown( wait_shot );
    }
    if ( ! mApp.setX310Laser( Device.LASER_ON, 0, null, mDataType, false ) ) return i;
    TDUtil.slowDown( wait_laser ); 
    return 0;
  }

  @Override
  protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPreExecute( )
  {
    mILister.enableBluetoothButton(false);
  }

  @Override
  protected void onPostExecute( Integer result ) 
  {
    // TDLog.v("Take Shot post-exec: " + result );
    int nr = ( mLister == null )? 0 : mNr; // number of shots to download
    if ( result == 0 ) {
      mApp.setX310Laser( Device.MEASURE, nr, mLister, mDataType, true ); // measure and download if nr > 0
    }
    mILister.enableBluetoothButton(true);
  }
}
