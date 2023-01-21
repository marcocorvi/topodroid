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

import com.topodroid.TDX.MainWindow;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.DataHelper;
import com.topodroid.TDX.R;
import com.topodroid.TDX.TDToast;
import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import android.app.ProgressDialog;

abstract class ImportTask extends AsyncTask< String, Integer, Long >
{
  final WeakReference<MainWindow> mMain;  // FIXME LEAK
  final WeakReference<TopoDroidApp> mApp; // FIXME LEAK used by inheriting classes
  ProgressDialog mProgress = null;

  protected InputStream fis = null;
  protected InputStreamReader isr = null;
  protected ParcelFileDescriptor mPfd = null;

  ImportTask( MainWindow main )
  {
    super();
    // this.fis = null;
    // this.isr = null;
    mMain = new WeakReference<MainWindow>( main );
    mApp  = new WeakReference<TopoDroidApp>( main.getApp() );
    mProgress = ProgressDialog.show( main,
		    main.getResources().getString(R.string.pleasewait),
		    main.getResources().getString(R.string.processing),
		    true );
  }

  // ImportTask( MainWindow main, InputStream fis )
  // {
  //   super();
  //   this.fis = fis;
  //   this.isr = null;
  //   mMain = new WeakReference<MainWindow>( main );
  //   mApp  = new WeakReference<TopoDroidApp>( main.getApp() );
  //   mProgress = ProgressDialog.show( main,
  //       	    main.getResources().getString(R.string.pleasewait),
  //       	    main.getResources().getString(R.string.processing),
  //       	    true );
  // }

  // ImportTask( MainWindow main, InputStreamReader isr )
  // {
  //   super();
  //   // this.fis = null;
  //   this.isr = isr;
  //   mMain = new WeakReference<MainWindow>( main );
  //   mApp  = new WeakReference<TopoDroidApp>( main.getApp() );
  //   mProgress = ProgressDialog.show( main,
  //       	    main.getResources().getString(R.string.pleasewait),
  //       	    main.getResources().getString(R.string.processing),
  //       	    true );
  // }

  ImportTask( MainWindow main, ParcelFileDescriptor pfd )
  {
    super();
    // this.fis = null;
    try {
      this.mPfd = pfd.dup();
      this.isr = new InputStreamReader( TDsafUri.docFileInputStream( mPfd ) );
    } catch ( IOException e ) {
      TDLog.Error("IO error " + e.getMessage() );
    }

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
    try {
      if ( mPfd != null ) mPfd.close();
    } catch ( IOException e ) {
      TDLog.Error( e.getMessage() );
    }

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

  /** @return true if the survey (name) already exists
   * @param name   survey name
   */
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
