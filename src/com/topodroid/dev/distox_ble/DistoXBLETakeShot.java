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

public class DistoXBLETakeShot extends AsyncTask<Integer, Integer, Integer >
{
  private final ILister mILister;      // lister with BT button
  private final ListerHandler mLister; // lister that manages downloaded shots (if null shots are not downloaded)
  private final TopoDroidApp  mApp;    // FIXME LEAK
  private final int mNr;               // number of shots to measure before download
  private final int mDataType;

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
      // TDLog.v( "take shot " + i + " wait " + TDSetting.mWaitLaser + "/" + TDSetting.mWaitShot );
      mApp.setXBLELaser( Device.LASER_ON, 0, mLister, mDataType,false );
      TDUtil.slowDown( TDSetting.mWaitLaser ); 
      mApp.setXBLELaser( Device.MEASURE, 0, mLister, mDataType ,false);
      TDUtil.slowDown( TDSetting.mWaitShot );
    }
    mApp.setXBLELaser( Device.LASER_ON, 0, mLister, mDataType,false );
    TDUtil.slowDown( TDSetting.mWaitLaser );

    // TDUtil.slowDown( TDSetting.mWaitLaser ); 
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
    int nr = ( mLister == null )? 0 : mNr; // number of shots to download
    mApp.setXBLELaser( Device.MEASURE, nr, mLister, mDataType,true ); // measure and download if nr > 0
    mILister.enableBluetoothButton(true);
  }
}
