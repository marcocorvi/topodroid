/* @file CalibReadTask.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calib coeff read task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;

import android.app.Activity;
// import android.app.ActivityManager;
// import android.app.ActivityManager.RunningTaskInfo;
import android.os.AsyncTask;
import android.content.Context;

import android.widget.Button;
import android.widget.Toast;

import android.util.Log;

class CalibReadTask extends AsyncTask<Void, Integer, Boolean>
{
  static final int PARENT_DEVICE = 1;
  static final int PARENT_GM     = 2;

  byte[]   coeff;
  Activity mActivity;
  IEnableButtons mEnableButtons;
  TopoDroidApp mApp;
  int mParentType;
  // String comp_name;

  CalibReadTask( Activity activity, IEnableButtons eb, TopoDroidApp app, int parent_type )
  {
    mActivity = activity;
    mApp      = app;
    mEnableButtons = eb;
    coeff = new byte[52]; // always read 52 bytes
    mParentType = parent_type;
    // comp_name = "ComponentInfo{com.topodroid.DistoX/com.topodroid.DistoX." + act_name + "}";
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    return mApp.readCalibCoeff( coeff );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    mEnableButtons.enableButtons( true );
    if ( result ) {
      String[] items = new String[8];
      Vector bg = new Vector();
      Matrix ag = new Matrix();
      Vector bm = new Vector();
      Matrix am = new Matrix();
      Vector nL = new Vector();
      Calibration.coeffToG( coeff, bg, ag );
      Calibration.coeffToM( coeff, bm, am );
      Calibration.coeffToNL( coeff, nL );

      switch ( mParentType ) {
        case PARENT_DEVICE:
          if ( mApp.mDeviceActivityVisible ) {
            (new CalibCoeffDialog( mActivity, mApp, bg, ag, bm, am, nL, null, 0.0f, 0.0f, 0.0f, 0, null ) ).show();
          }
          break;
        case PARENT_GM:
          if ( mApp.mGMActivityVisible ) {
            (new CalibCoeffDialog( mActivity, mApp, bg, ag, bm, am, nL, null, 0.0f, 0.0f, 0.0f, 0, null ) ).show();
          }
          break;
      }
    } else {
      Toast.makeText( mApp, R.string.read_failed, Toast.LENGTH_SHORT).show();
    }
  }
}
