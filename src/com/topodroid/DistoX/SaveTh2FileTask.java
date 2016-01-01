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
     // TDLog.Log( TDLog.LOG_PLOT, "Save Th2 File Task " + mFullName1 + " type " + mType );
  }

  private void rotateBackups( String filename )
  {
    File file2;
    File file1;
    for ( int i=mRotate-1; i>0; --i ) { 
      file2 = new File( filename + Integer.toString(i) );
      file1 = new File( filename + Integer.toString(i-1) );
      if ( file1.exists() ) {
        file1.renameTo( file2 );
      }
    }
    file2 = new File( filename + "0" );
    file1 = new File( filename );
    if ( file1.exists() ) {
      file1.renameTo( file2 );
    }
  }

  @Override
  protected Boolean doInBackground(Intent... arg0)
  {
    boolean ret = false;
    synchronized( TDPath.mTherionLock ) {
      // Log.v("DistoX", "save scrap files " + mFullName1 + " suffix " + mSuffix );
      String filename = (TDSetting.mBinaryTh2)? TDPath.getTdrFileWithExt( mFullName1 ) + ".bck"
                                              : TDPath.getTh2FileWithExt( mFullName1 ) + ".bck";
      rotateBackups( filename );

      long now  = System.currentTimeMillis();
      long time = now - 60000; // one minute before now
      // TDPath.checkPath( tempname1 );
      File tmpDir = TDPath.getTmpDir();
      File[] files = tmpDir.listFiles();
      for ( File f : files ) {
        if ( f.getName().endsWith("tmp") && f.lastModified() < time ) {
          // Log.v("DistoX", "delete temp file " + f.getAbsolutePath() );
          f.delete();
        }
      }

      String tempname1 = TDPath.getTmpFileWithExt( mSuffix + Long.toString(now) );
      File file1 = new File( tempname1 );
      if ( TDSetting.mBinaryTh2 ) {
        DrawingIO.exportDataStream( mSurface, mType, file1, mFullName1 );
      } else {
        DrawingIO.exportTherion( mSurface, mType, file1, mFullName1, PlotInfo.projName[mType] );
      }

      if ( isCancelled() ) {
        // Log.v("DistoX", "save cancelled");
        file1.delete();
      } else {
        // Log.v("DistoX", "save completed");
        String filename1 = (TDSetting.mBinaryTh2)? TDPath.getTdrFileWithExt( mFullName1 )
                                                        : TDPath.getTh2FileWithExt( mFullName1 );
        (new File( filename1 )).renameTo( new File( filename1 + ".bck" ) );
        file1.renameTo( new File( filename1 ) );
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

