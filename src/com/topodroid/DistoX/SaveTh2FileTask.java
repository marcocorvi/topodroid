/** @file SaveTh2FileTask.java
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

import android.util.Log;

class SaveTh2FileTask extends AsyncTask<Intent,Void,Boolean>
{
  final static int NR_BACKUP = 3;
  private Context mContext;
  private Handler mHandler;
  private TopoDroidApp mApp;
  private DrawingSurface mSurface;
  private String mFullName1;
  private int mType; // plot type
  private String mSuffix;
  private int mRotate;  // nr. backups to rotate

  public SaveTh2FileTask( Context context, Handler handler,
                      TopoDroidApp app, DrawingSurface surface, 
                      String fullname1, long type, String suffix, int rotate )
  {
     mContext  = context;
     mHandler  = handler;
     mApp      = app;
     mSurface  = surface;
     mFullName1 = fullname1;
     mType = (int)type;
     mSuffix = suffix;
     mRotate = rotate;
     if ( mRotate > NR_BACKUP ) mRotate = NR_BACKUP;
     // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "Save Th2 File Task " + mFullName1 + " type " + mType );
  }

  private void rotateBackups( String filename )
  {
    // Log.v("DistoX", "rotate " + filename );
    File file2;
    File file1;
    for ( int i=mRotate-1; i>0; --i ) { 
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
        long now  = System.currentTimeMillis();
        long time = now - 60000; // one minute before now
        // TopoDroidApp.checkPath( tempname1 );
        File tmpDir = TopoDroidPath.getTmpDir();
        File[] files = tmpDir.listFiles();
        for ( File f : files ) {
          if ( f.getName().endsWith("tmp") && f.lastModified() < time ) {
            // Log.v("DistoX", "delete temp file " + f.getAbsolutePath() );
            f.delete();
          }
        }
        String tempname1 = TopoDroidPath.getTmpFileWithExt( mSuffix + Long.toString(now) );
        File file1 = new File( tempname1 );
        // Log.v("DistoX", "create temp file " + file1.getAbsolutePath() );

        // Log.v("DistoX", "repeating save th2");
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
      } catch (Exception e) {
        e.printStackTrace();
      }
      ret = true;
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

