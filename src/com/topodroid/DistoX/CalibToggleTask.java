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
 */
package com.topodroid.DistoX;

import android.app.Activity;
import android.os.AsyncTask;

// import android.widget.Button;
// import android.widget.Toast;

class CalibToggleTask extends AsyncTask<Void, Integer, Boolean>
{
  Activity   mActivity; // FIXME LEAK
  TopoDroidApp mApp;    // FIXME LEAK
  private IEnableButtons mEnableButtons;

  CalibToggleTask( Activity act, IEnableButtons eb, TopoDroidApp app )
  {
    mActivity = act;
    mApp      = app;
    mEnableButtons = eb;
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    return Boolean.valueOf( mApp.toggleCalibMode( ) );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( result ) {
      TDToast.make( mActivity, R.string.toggle_ok );
    } else {
      TDToast.make( mActivity, R.string.toggle_failed );
    }
    mEnableButtons.enableButtons( true );
  }
}
