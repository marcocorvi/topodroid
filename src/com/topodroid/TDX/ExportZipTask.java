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
package com.topodroid.TDX;

// import com.topodroid.prefs.TDSetting;

// import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.content.Context;
import android.net.Uri;
   
class ExportZipTask extends AsyncTask< Void, Void, Boolean >
{
  private Archiver mArchiver;
  // private final String   mSaved;
  private final TopoDroidApp mApp;
  private final Uri      mUri;
  
  /** cstr
   * @param context   context (unused)
   * @param app       application
   * @param uri       zip output URI (or null to use default zipfile)
   */
  ExportZipTask( Context context, TopoDroidApp app, Uri uri )
  {
    mApp   = app;
    mUri   = uri;
    // mSaved    = context.getResources().getString( R.string.zip_saved );
  }

  /** execute the task in background
   * @param args  args (unused)
   */
  @Override
  protected Boolean doInBackground( Void... args )
  {
    // TopoDroidApp.doExportDataSync( TDSetting.mExportShotsFormat );
    mArchiver = new Archiver( );
    return mArchiver.archive( mApp, mUri );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress) { }

  /** post-execution (on UI thread)
   * @param res   execution result
   */
  @Override
  protected void onPostExecute( Boolean res )
  {
    if ( res ) {
      // TDToast.make( mSaved + " " + mArchiver.getZipname() );
      TDToast.make( R.string.zip_saved );
    } else {
      TDToast.makeBad( R.string.zip_failed );
    }
  }
}

