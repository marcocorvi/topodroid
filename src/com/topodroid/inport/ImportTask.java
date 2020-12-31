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
package com.topodroid.inport;

import com.topodroid.DistoX.MainWindow;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.DataHelper;
import com.topodroid.DistoX.R;
import com.topodroid.DistoX.TDToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
		    main.getResources().getString(R.string.pleasewait),
		    main.getResources().getString(R.string.processing),
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
        if ( result == -1L ) {
          TDToast.makeBad( R.string.import_already );
        } else {
          TDToast.makeBad( R.string.import_bad );
        }
      }
    }
  }

  protected boolean hasSurveyName( String name )
  {
    return ((DataHelper)(TopoDroidApp.mData)).hasSurveyName( name );
  }

  protected long insertImportShots( long sid, long id, ArrayList<ParserShot> shots )
  {
    return ((DataHelper)(TopoDroidApp.mData)).insertImportShots( sid, id, shots );
  }
    
  protected long insertImportShotsDiving( long sid, long id, ArrayList<ParserShot> shots )
  {
    return ((DataHelper)(TopoDroidApp.mData)).insertImportShotsDiving( sid, id, shots );
  }
    
  protected void updateSurveyMetadata( long sid, ImportParser parser )
  {
    parser.updateSurveyMetadata( sid, (DataHelper)(TopoDroidApp.mData) );
  }
    
}
