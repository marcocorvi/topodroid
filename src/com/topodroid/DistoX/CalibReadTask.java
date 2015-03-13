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
 * CHANGES
 * 20140701 created
 */
package com.topodroid.DistoX;

import android.app.Activity;
import android.os.AsyncTask;

import android.widget.Button;
import android.widget.Toast;

class CalibReadTask extends AsyncTask<Void, Integer, Boolean>
{
  byte[]   coeff;
  Activity mActivity;
  IEnableButtons mEnableButtons;
  TopoDroidApp mApp;

  CalibReadTask( Activity activity, IEnableButtons eb, TopoDroidApp app )
  {
    mActivity = activity;
    mApp      = app;
    mEnableButtons = eb;
    coeff = new byte[52]; // always read 52 bytes
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    return new Boolean( mApp.readCalibCoeff( coeff ) );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
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
      (new CalibCoeffDialog( mActivity, mApp, bg, ag, bm, am, nL, 0.0f, 0.0f, 0, null ) ).show();
    } else {
      Toast.makeText( mActivity, R.string.read_failed, Toast.LENGTH_SHORT).show();
    }
    mEnableButtons.enableButtons( true );
  }
}
