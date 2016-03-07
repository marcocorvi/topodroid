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
  private Context mContext;
  private Handler mHandler;
  private TopoDroidApp mApp;
  private DrawingSurface mSurface;
  private String mFullName1;
  private int mType;    // plot type
  private int mProjDir;
  private int mSuffix;
  private int mRotate;  // nr. backups to rotate

  public SaveTh2FileTask( Context context, Handler handler,
                          TopoDroidApp app, DrawingSurface surface, 
                          String fullname1, long type, int proj_dir, int suffix, int rotate )
  {
     mContext  = context;
     mHandler  = handler;
     mApp      = app;
     mSurface  = surface;
     mFullName1 = fullname1;
     mType     = (int)type;
     mProjDir  = proj_dir;
     mSuffix   = suffix;
     mRotate   = rotate;
     if ( mRotate > TDPath.NR_BACKUP ) mRotate = TDPath.NR_BACKUP;
     // TDLog.Log( TDLog.LOG_PLOT, "Save Th2 File Task " + mFullName1 + " type " + mType );
  }

  @Override
  protected Boolean doInBackground(Intent... arg0)
  {
    boolean ret = false;
    boolean do_binary = (TDSetting.mBinaryTh2 && mSuffix != PlotSave.EXPORT );
    synchronized( TDPath.mTherionLock ) {
      // Log.v("DistoX", "save scrap files " + mFullName1 + " suffix " + mSuffix );
      
      String filename = do_binary ? TDPath.getTdrFileWithExt( mFullName1 ) + TDPath.BCK_SUFFIX
                                  : TDPath.getTh2FileWithExt( mFullName1 ) + TDPath.BCK_SUFFIX;
      if ( mSuffix != PlotSave.EXPORT ) {
        TDPath.rotateBackups( filename, mRotate );
      }

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

      String tempname1 = TDPath.getTmpFileWithExt( Integer.toString(mSuffix) + Long.toString(now) );
      File file1 = new File( tempname1 );
      if ( do_binary ) {
        DrawingIO.exportDataStream( mSurface, mType, file1, mFullName1, mProjDir );
      } else {
        DrawingIO.exportTherion( mSurface, mType, file1, mFullName1, PlotInfo.projName[mType], mProjDir );
      }

      if ( isCancelled() ) {
        // Log.v("DistoX", "save cancelled");
        file1.delete();
      } else {
        // Log.v("DistoX", "save completed");
        String filename1 = do_binary ? TDPath.getTdrFileWithExt( mFullName1 )
                                     : TDPath.getTh2FileWithExt( mFullName1 );
        (new File( filename1 )).renameTo( new File( filename1 + TDPath.BCK_SUFFIX ) );
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

