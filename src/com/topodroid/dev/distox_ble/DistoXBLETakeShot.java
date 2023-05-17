/* @file DeviceXBLETakeShot.java
 *
 * @author siwei tian
 * @date aug 2022
 *
 * @brief TopoDroid DistoXBLE shooting class
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox_ble;

import android.os.AsyncTask;

import com.topodroid.TDX.ILister;
import com.topodroid.TDX.ListerHandler;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.prefs.TDSetting;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDLog;

public class DistoXBLETakeShot extends AsyncTask<Integer, Integer, Integer >
{
  private final ILister mILister;      // lister with BT button
  private final ListerHandler mLister; // lister that manages downloaded shots (if null shots are not downloaded)
  private final TopoDroidApp  mApp;    // FIXME LEAK
  private final int mNr;               // number of shots to measure before download
  private final int mDataType;

  /** cstr
   * @param ilister    lister that started the task
   * @param lister     shot data lister
   * @param app        application
   * @param nr         number of shots to take
   * @param data_type  expected shot type
   */
  public DistoXBLETakeShot(ILister ilister, ListerHandler lister, TopoDroidApp app, int nr, int data_type )
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
  @Override
  protected Integer doInBackground( Integer... ii )
  {
    int i = mNr;
    if ( mNr > 1 && mDataType == DataType.DATA_CALIB ) {
      TDUtil.slowDown( TDSetting.mWaitShot );
    }
    for ( ; i>1; --i ) {
      TDLog.f( "take shot " + i + " wait " + TDSetting.mWaitLaser + "/" + TDSetting.mWaitShot );
      if ( ! mApp.setXBLELaser( Device.LASER_ON, 0, mLister, mDataType, false, false ) ) return i;
      TDUtil.slowDown( TDSetting.mWaitLaser ); 
      if ( ! mApp.setXBLELaser( Device.MEASURE, 0, mLister, mDataType, false, false ) ) return i;
      TDUtil.slowDown( TDSetting.mWaitShot );
    }
    if ( ! mApp.setXBLELaser( Device.LASER_ON, 0, mLister, mDataType, false, false ) ) return i;
    TDUtil.slowDown( TDSetting.mWaitLaser );
    return 0;
  }

  @Override
  protected void onProgressUpdate(Integer... progress) { }

  /** before the execution: tell the lister that started the task to disable the BT button
   */
  @Override
  protected void onPreExecute( )
  {
    mILister.enableBluetoothButton(false);
  }

  /** after the execution: 
   *  measure and download if the number of shots is positive and there is a sot lister
   *  tell the lister that started the task to re-enable the BT button
   * @param result exec result (unused)
   * @note post-exec is run on the UI thread
   */
  @Override
  protected void onPostExecute( Integer result ) 
  {
    int nr = ( mLister == null )? 0 : mNr; // number of shots to download
    // FIXME what if this fails ?
    //       really need to run on thread ?
    mApp.setXBLELaser( Device.MEASURE, nr, mLister, mDataType, true, true ); // measure and download if nr > 
    mILister.enableBluetoothButton(true);
  }
}
