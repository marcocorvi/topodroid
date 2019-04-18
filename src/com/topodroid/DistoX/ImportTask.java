/* @file ImportTask.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid VisualTopo import task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

import android.app.ProgressDialog;

abstract class ImportTask extends AsyncTask< String, Integer, Long >
{
  final WeakReference<MainWindow> mMain;  // FIXME LEAK
  final WeakReference<TopoDroidApp> mApp; // FIXME LEAK used by inheriting classes
  ProgressDialog mProgress = null;

  ImportTask( MainWindow main )
  {
    super();
    mMain = new WeakReference<MainWindow>( main );
    mApp  = new WeakReference<TopoDroidApp>( main.getApp() );
    mProgress = ProgressDialog.show( main,
		    mApp.get().getResources().getString(R.string.pleasewait),
		    mApp.get().getResources().getString(R.string.processing),
		    true );
  }

  @Override
  protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPostExecute(Long result)
  {
    mProgress.dismiss();
    MainWindow main = mMain.get();
    if ( main != null && ! main.isFinishing() ) {
      main.setTheTitle( );
      if ( result >= 0 ) {
        main.updateDisplay( );
      } else {
        TDToast.makeBad( R.string.import_already );
      }
    }
  }
}
