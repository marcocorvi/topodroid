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

import java.io.File;

import java.util.List;

import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
// import android.os.Bundle;
import android.os.Handler;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;

// import android.util.Log;

class SaveFullFileTask extends AsyncTask<Void,Void,String>
{
  private final Context mContext; // FIXME LEAK
  
  private long mSid;
  private DataHelper mData;
  private SurveyInfo mInfo;
  private String mFilename;
  private String mOrigin = null;
  private PlotSaveData mPsd1 = null;
  private PlotSaveData mPsd2 = null;

  SaveFullFileTask( Context context, long sid, DataHelper data, SurveyInfo info, PlotSaveData psd1, PlotSaveData psd2, String origin, String filename )
  {
    mContext  = context;
     
    mSid      = sid;
    mData     = data;
    mInfo     = info;
    mFilename = filename;
    mOrigin   = origin;
    mPsd1     = psd1;
    mPsd2     = psd2;
  }

  protected String doInBackground(Void... arg0)
  {
    String ret = null;
    synchronized( TDPath.mTherionLock ) {
      ret = TDExporter.exportSurveyAsCsx( mSid, mData, mInfo, mPsd1, mPsd2, mOrigin, mFilename );
    }
    return ret;
  }

  @Override
  protected void onPostExecute(String filename)
  {
    // Log.v( "DistoX", "save plot file task post exec");
    if ( filename == null ) {
      TDLog.Log( TDLog.LOG_IO, "failed export as CSX " + mFilename );
      TDToast.make( mContext, mContext.getResources().getString(R.string.saving_file_failed) );
    } else {
      TDLog.Log( TDLog.LOG_IO, "exported survey as CSX " + filename );
      TDToast.make( mContext, String.format( mContext.getResources().getString(R.string.saved_file_1), filename ) );
    }
  }
}

