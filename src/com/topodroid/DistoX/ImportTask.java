/* @file ImportTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid VisualTopo import task
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.AsyncTask;

// import android.widget.Toast;
import android.app.ProgressDialog;

abstract class ImportTask extends AsyncTask< String, Integer, Long >
{
  MainWindow mMain;  // FIXME LEAK
  TopoDroidApp mApp; // FIXME LEAK used by inheriting classes
  ProgressDialog mProgress = null;

  ImportTask( MainWindow main )
  {
    super();
    mMain = main;
    mApp  = main.getApp();
    mProgress = ProgressDialog.show( main, "Please wait", "processing ...", true );
  }

  @Override
  protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPostExecute(Long result) {
    mMain.setTheTitle( );
    mProgress.dismiss();
    if ( result >= 0 ) {
      mMain.updateDisplay( );
    } else {
      TDToast.make( mMain, R.string.import_already );
    }
  }
}
