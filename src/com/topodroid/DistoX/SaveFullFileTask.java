/* @file SaveFullFileTask.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid drawing: save drawing in therion format
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDFile;

// import java.lang.ref.WeakReference;

// import java.io.File;
// import java.io.IOException;


// import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
// import android.os.Bundle;
// import android.os.Handler;
import android.net.Uri;

class SaveFullFileTask extends AsyncTask<Void,Void,String>
{
  private long mSid;
  private DataHelper mData;
  private SurveyInfo mInfo;
  // private String mFilename;
  private String mFullname;
  // private String mDirname;
  private String mOrigin = null;
  private PlotSaveData mPsd1 = null;
  private PlotSaveData mPsd2 = null;
  private boolean mToast;
  private String mFormat;
  private Uri mUri;

  SaveFullFileTask( Context context, Uri uri, long sid, DataHelper data, SurveyInfo info, PlotSaveData psd1, PlotSaveData psd2, String origin, // String filename,
                    String fullname, /* String dirname, */ boolean toast )
  {
    mUri      = uri;
    mSid      = sid;
    mData     = data;
    mInfo     = info.copy();
    // mFilename = filename;
    mFullname = fullname;
    // mDirname  = dirname;
    mOrigin   = origin;
    mPsd1     = psd1;
    mPsd2     = psd2;
    mToast    = toast;
    mFormat   = context.getResources().getString(R.string.saved_file_1);
  }

  protected String doInBackground(Void... arg0)
  {
    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    // File temp = null;
    // try {
    //   temp = File.createTempFile( "tmp", null, TDFile.getFile( mDirname ) );
    // } catch ( IOException e ) { 
    //   // TDLog.v( "cannot create temp file with " + mFullname );
    //   TDLog.Error("cannot create temp file with " + mFullname );
    //   return null;
    // }
    // if ( temp == null ) return null;
    // int res = TDExporter.exportSurveyAsCsx( mSid, mData, mInfo, mPsd1, mPsd2, mOrigin, temp );
    // if ( res == 1 && TDFile.renameTempFile( temp, mFilename ) ) {
    //   return mFilename;
    // }
    // return null;
    TDLog.v( "save full file: " + mFullname );
    int res = TDExporter.exportSurveyAsCsx( mUri, mSid, mData, mInfo, mPsd1, mPsd2, mOrigin, mFullname );
    return (res == 1)? mFullname : null;
  }

  @Override
  protected void onPostExecute(String filename)
  {
    // TDLog.v( "save plot file task post exec");
    if ( filename == null ) {
      TDLog.Log( TDLog.LOG_IO, "failed export as CSX " + mFullname );
      if ( mToast ) TDToast.make( R.string.saving_file_failed );
    } else {
      TDLog.Log( TDLog.LOG_IO, "exported survey as CSX " + filename );
      if ( mToast ) TDToast.make( String.format( mFormat, filename ) );
    }
  }
}

