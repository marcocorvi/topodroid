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

import android.os.AsyncTask;
import android.content.Context;
   
class ExportZipTask extends AsyncTask< Void, Void, Boolean >
{
  Context mContext;
  Archiver mArchiver;
  
  ExportZipTask( Context context, TopoDroidApp app )
  {
    mContext = context;
    mArchiver = new Archiver( app );
  }

  @Override
  protected Boolean doInBackground( Void... arg )
  {
    TopoDroidApp.doExportDataSync( mContext, TDSetting.mExportShotsFormat );
    return mArchiver.archive( );
  }

  // @Override
  // protected void onProgressUpdate(Integer... progress) { }

  @Override
  protected void onPostExecute( Boolean res )
  {
    if ( res ) {
      TDToast.make( mContext.getResources().getString( R.string.zip_saved ) + " " + mArchiver.zipname );
    } else {
      TDToast.make( R.string.zip_failed );
    }
  }
}

