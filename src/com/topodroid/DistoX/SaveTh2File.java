/** @file SaveTh2File.java
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

import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
// import android.os.Bundle;
import android.os.Handler;

// import android.util.Log;

class SaveTh2File extends AsyncTask<Intent,Void,Boolean>
{
  final static int NR_BACKUP = 3;
  private Context mContext;
  private Handler mHandler;
  private TopoDroidApp mApp;
  private DrawingSurface mSurface;
  private String mFullName1;
  private int mType; // plot type
  private String mSuffix;

  public SaveTh2File( Context context, Handler handler,
                      TopoDroidApp app, DrawingSurface surface, 
                      String fullname1, long type, String suffix )
  {
     mContext  = context;
     mHandler  = handler;
     mApp      = app;
     mSurface  = surface;
     mFullName1 = fullname1;
     mType = (int)type;
     mSuffix = suffix;
     // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "SaveTh2File " + mFullName1 + " type " + mType );
  }

  private void rotateBackups( String filename )
  {
    // Log.v("DistoX", "rotate " + filename );
    File file2;
    File file1;
    for ( int i=NR_BACKUP-1; i>0; --i ) { 
      file2 = new File( filename + Integer.toString(i) );
      file1 = new File( filename + Integer.toString(i-1) );
      if ( file1.exists() ) {
        // Log.v("DistoX", "rename " + (i-1) + "->" + i + " size " + file1.length() + " " + file2.length() );
        file1.renameTo( file2 );
      }
    }
    file2 = new File( filename + "0" );
    file1 = new File( filename );
    if ( file1.exists() ) {
      // Log.v("DistoX", "rename .->0 size " + file1.length() + " " + file2.length() );
      file1.renameTo( file2 );
    }
  }

  @Override
  protected Boolean doInBackground(Intent... arg0)
  {
    boolean ret = false;
    synchronized( TopoDroidPath.mTherionLock ) {
      try {
        String filename = TopoDroidPath.getTh2FileWithExt( mFullName1 ) + ".bck";
        // Log.v("DistoX", "save th2 files " + mFullName1 + " suffix " + mSuffix );
        rotateBackups( filename );

        String filename1 = TopoDroidPath.getTh2FileWithExt( mFullName1 );
        String tempname1 = filename1 + Long.toString(System.currentTimeMillis()) + mSuffix;
        File file1 = new File( tempname1 );
        TopoDroidApp.checkPath( tempname1 );
        FileWriter writer1 = new FileWriter( file1 );
        BufferedWriter out1 = new BufferedWriter( writer1 );
        mSurface.exportTherion( mType, out1, mFullName1, PlotInfo.projName[mType] );
        out1.flush();
        out1.close();
        if ( isCancelled() ) {
          // Log.v("DistoX", "save cancelled");
          file1.delete();
        } else {
          // Log.v("DistoX", "save completed");
          String p1 = TopoDroidPath.getTh2FileWithExt( mFullName1 );
          File f1 = new File( p1 );
          File b1 = new File( p1 + ".bck" );
          f1.renameTo( b1 );
          file1.renameTo( new File( filename1 ) );
        }
        ret = true;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return ret;
  }

  @Override
  protected void onPostExecute(Boolean bool)
  {
    super.onPostExecute(bool);
    if ( mHandler != null ) {
      mHandler.sendEmptyMessage( bool? 661 : 660 );
    }
  }
}

