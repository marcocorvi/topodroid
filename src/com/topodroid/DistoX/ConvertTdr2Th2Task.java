/** @file ConvertTdr2Th2Task.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid drawing: save drawing in therion format
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;;

import java.util.List;

import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
// import android.os.Bundle;
import android.os.Handler;

import android.util.Log;

class ConvertTdr2Th2Task extends AsyncTask<Intent,Void,Boolean>
{
  private Context mContext;
  private Handler mHandler;
  private TopoDroidApp mApp;
  private long mSid;
  private String mSurvey;

  public ConvertTdr2Th2Task( Context context, Handler handler, TopoDroidApp app )
  {
     mContext  = context;
     mHandler  = handler;
     mApp      = app;
     mSid      = app.mSID;
     mSurvey   = app.mySurvey;
  }

  @Override
  protected Boolean doInBackground(Intent... arg0)
  {
    List< PlotInfo > plots = mApp.mData.selectAllPlots( mSid );
    for ( PlotInfo plot : plots ) {
      String fullname = mSurvey + "-" + plot.name;
      // int type = plot.type;
      String tdr = TopoDroidPath.getTdrFileWithExt( fullname );
      String th2 = TopoDroidPath.getTh2FileWithExt( fullname );
      File tdrfile = new File( tdr );
      File th2file = new File( th2 );
      if ( tdrfile.exists() ) {
        try {
          FileWriter fw = new FileWriter( th2file );
          BufferedWriter bw = new BufferedWriter( fw );
          DrawingIO.dataStream2Therion( tdrfile, bw );
          bw.flush();
          bw.close();
          fw.close();
        } catch ( IOException e ) {
          TopoDroidLog.Error("I/O error " + th2 );
        }
      } else {
        TopoDroidLog.Error("Missing file " + tdr );
      }

    }
    return true;
  }

  @Override
  protected void onPostExecute(Boolean bool)
  {
    super.onPostExecute(bool);
    if ( mHandler != null ) {
      mHandler.sendEmptyMessage( 999 );
    }
  }
}

