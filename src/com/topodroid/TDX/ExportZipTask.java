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

import com.topodroid.utils.TDLog;

import com.topodroid.prefs.TDSetting;

// import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.content.Context;
import android.net.Uri;
   
class ExportZipTask extends AsyncTask< Void, Void, Boolean >
{
  private Archiver mArchiver;
  // private final String   mSaved;
  private final TopoDroidApp mApp;
  private final Uri mUri;
  private final boolean mToast;
  
  /** cstr
   * @param context   context (unused)
   * @param app       application
   * @param uri       zip output URI (or null to use default zipfile)
   * @note if uri is null the zip is exported in the default path (topodroid/zip/survey.zip)
   *       currently (v. 6.3.0) uri is always null
   */
  ExportZipTask( Context context, TopoDroidApp app, Uri uri, boolean toast )
  {
    mApp   = app;
    mUri   = uri;
    mToast = toast;
    // mSaved    = context.getResources().getString( R.string.zip_saved );
  }

  /** export survey ZIP in foreground - run on the calling thread
   */
  boolean doInForeground( )
  {
    mArchiver = new Archiver( );
    boolean ret = mArchiver.archiveSurvey( mApp, mUri );
    onPostExecute( ret );
    return ret;
  }
  
  /** execute the task in background
   * @param args  args (unused)
   */
  @Override
  protected Boolean doInBackground( Void... args )
  {
    // TopoDroidApp.doExportDataSync( TDSetting.mExportShotsFormat );
    TDLog.v("export zip task exec ...");
    mArchiver = new Archiver( );
    return mArchiver.archiveSurvey( mApp, mUri );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress) { }

  /** post-execution (on UI thread)
   * @param res   execution result
   */
  @Override
  protected void onPostExecute( Boolean res )
  {
    TDLog.v("Export zip task post-exec res " + res );
    if ( res ) {
      // TDToast.make( mSaved + " " + mArchiver.getZipname() );
      if ( mToast ) TDToast.make( R.string.zip_saved );
      if ( TDSetting.mZipShare ) {
        TDLog.v("share zip");
        mApp.shareZip( mUri );
      }
    } else {
      if ( mToast ) TDToast.makeBad( R.string.zip_failed );
    }
  }
}

