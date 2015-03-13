/* @file CalibToggleTask.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calib mode toggle task
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

class CalibToggleTask extends AsyncTask<Void, Integer, Boolean>
{
  Activity   mActivity;
  TopoDroidApp mApp;
  IEnableButtons mEnableButtons;

  CalibToggleTask( Activity act, IEnableButtons eb, TopoDroidApp app )
  {
    mActivity = act;
    mApp      = app;
    mEnableButtons = eb;
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    return new Boolean( mApp.toggleCalibMode( ) );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( result ) {
      Toast.makeText( mActivity, R.string.toggle_ok, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( mActivity, R.string.toggle_failed, Toast.LENGTH_SHORT).show();
    }
    mEnableButtons.enableButtons( true );
  }
}
