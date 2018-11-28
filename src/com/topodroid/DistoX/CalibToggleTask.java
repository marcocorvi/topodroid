/* @file CalibToggleTask.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calib mode toggle task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.ref.WeakReference;

// import android.app.Activity;
import android.os.AsyncTask;

// import android.widget.Button;
// import android.widget.Toast;

class CalibToggleTask extends AsyncTask<Void, Integer, Boolean>
{
  private WeakReference<TopoDroidApp> mApp; // FIXME LEAK
  private final IEnableButtons mEnableButtons;

  CalibToggleTask( IEnableButtons eb, TopoDroidApp app )
  {
    mApp = new WeakReference<TopoDroidApp>( app );
    mEnableButtons = eb;
  }

  @Override
  protected Boolean doInBackground(Void... v)
  {
    return mApp.get() != null && mApp.get().toggleCalibMode( );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress)
  // {
  // }

  @Override
  protected void onPostExecute( Boolean result )
  {
    if ( result ) {
      TDToast.make( R.string.toggle_ok );
    } else {
      TDToast.make( R.string.toggle_failed );
    }
    mEnableButtons.enableButtons( true );
  }
}
