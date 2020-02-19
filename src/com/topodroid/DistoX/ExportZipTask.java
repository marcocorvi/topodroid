/* @file ExportZipTask.java
 *
 * @author marco corvi
 * @date aug 2018
 *
 * @brief TopoDroid ZIP export task
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.content.Context;
   
class ExportZipTask extends AsyncTask< Void, Void, Boolean >
{
  private Archiver mArchiver;
  private final String   mSaved;
  private final TopoDroidApp mApp;
  
  ExportZipTask( Context context, TopoDroidApp app )
  {
    mApp   = app;
    mArchiver = new Archiver( );
    mSaved    = context.getResources().getString( R.string.zip_saved );
  }

  @Override
  protected Boolean doInBackground( Void... arg )
  {
    TopoDroidApp.doExportDataSync( TDSetting.mExportShotsFormat );
    return mArchiver.archive( mApp );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPostExecute( Boolean res )
  {
    if ( res ) {
      TDToast.make( mSaved + " " + mArchiver.zipname );
    } else {
      TDToast.makeBad( R.string.zip_failed );
    }
  }
}

