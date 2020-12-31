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
package com.topodroid.dev;

import com.topodroid.prefs.TDSetting;
// import com.topodroid.dev.Device;
import com.topodroid.DistoX.ILister;
import com.topodroid.DistoX.ListerHandler;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.TDUtil;


import android.os.AsyncTask;
 
// import android.util.Log;

public class DeviceX310TakeShot extends AsyncTask<Integer, Integer, Integer >
{
  private final ILister mILister;      // lister with BT button
  private final ListerHandler mLister; // lister that manages downloaded shots (if null shots are not downloaded)
  private final TopoDroidApp  mApp;    // FIXME LEAK
  private final int mNr;               // number of shots to measure before download
  private int mDataType;
 
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
  @Override
  protected Integer doInBackground( Integer... ii )
  {
    int i = mNr;
    for ( ; i>1; --i ) {
      // Log.v("DistoX", "take shot " + i + " wait " + TDSetting.mWaitLaser + "/" + TDSetting.mWaitShot );
      mApp.setX310Laser( Device.LASER_ON, 0, null, mDataType );
      TDUtil.slowDown( TDSetting.mWaitLaser ); 
      mApp.setX310Laser( Device.MEASURE, 0, null, mDataType );   
      TDUtil.slowDown( TDSetting.mWaitShot );
    }
    mApp.setX310Laser( Device.LASER_ON, 0, null, mDataType );
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
    mApp.setX310Laser( Device.MEASURE, nr, mLister, mDataType ); // measure and download if nr > 0
    mILister.enableBluetoothButton(true);
  }
}
