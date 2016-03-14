/** @file SavePlotFileTask.java
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

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import android.util.Log;

class SavePlotFileTask extends AsyncTask<Intent,Void,Boolean>
{
  private Context mContext;
  private Handler mHandler;
  private TopoDroidApp mApp;
  private DrawingActivity mParent;
  private DrawingSurface mSurface;
  private String mFullName;
  private int mType;    // plot type
  private int mProjDir;
  private int mSuffix;
  private int mRotate;  // nr. backups to rotate

  public SavePlotFileTask( Context context, DrawingActivity parent, Handler handler,
                          TopoDroidApp app, DrawingSurface surface, 
                          String fullname, long type, int proj_dir, int suffix, int rotate )
  {
     mContext  = context;
     mParent   = parent;
     mHandler  = handler;
     mApp      = app;
     mSurface  = surface;
     mFullName = fullname;
     mType     = (int)type;
     mProjDir  = proj_dir;
     mSuffix   = suffix;
     mRotate   = rotate;
     if ( mRotate > TDPath.NR_BACKUP ) mRotate = TDPath.NR_BACKUP;
     // TDLog.Log( TDLog.LOG_PLOT, "Save Th2 File Task " + mFullName + " type " + mType );
  }

  @Override
  protected Boolean doInBackground(Intent... arg0)
  {
    boolean ret = false;
    // boolean do_binary = (TDSetting.mBinaryTh2 && mSuffix != PlotSave.EXPORT ); // TDR BINARY

    synchronized( TDPath.mTherionLock ) {
      // Log.v("DistoX", "save scrap files " + mFullName + " suffix " + mSuffix );

      if ( mSuffix == PlotSave.EXPORT ) {
        String filename2 = TDPath.getTdrFileWithExt( mFullName );
        File file2 = new File( mFullName );
        DrawingIO.exportTherion( mSurface, mType, file2, mFullName, PlotInfo.projName[mType], mProjDir );
      } else if ( mSuffix == PlotSave.SAVE ) {
        switch ( TDSetting.mExportPlotFormat ) {
          case TDConst.DISTOX_EXPORT_TH2:
            String filename2 = TDPath.getTdrFileWithExt( mFullName );
            File file2 = new File( mFullName );
            DrawingIO.exportTherion( mSurface, mType, file2, mFullName, PlotInfo.projName[mType], mProjDir );
            break;
          case TDConst.DISTOX_EXPORT_CSX:
            if ( PlotInfo.isSketch2D( mType ) ) mParent.saveCsx( false );
            break;
          case TDConst.DISTOX_EXPORT_DXF:
            mParent.doSaveWithExt( mType, mFullName, "dxf", false );
            break;
          case TDConst.DISTOX_EXPORT_SVG:
            mParent.doSaveWithExt( mType, mFullName, "svg", false );
            break;
          case TDConst.DISTOX_EXPORT_PNG:
            Bitmap bitmap = mSurface.getBitmap( mType );
            if ( bitmap == null ) {
              TDLog.Error( "cannot save PNG: null bitmap" );
            } else {
              new ExportBitmapToFile( mContext, null, bitmap, mFullName ).execute();
            }
            break;
        }
      }
      
      if ( mSuffix != PlotSave.EXPORT ) {

        String filename = TDPath.getTdrFileWithExt( mFullName ) + TDPath.BCK_SUFFIX;

        if ( mSuffix != PlotSave.EXPORT ) {
          // Log.v("DistoX", "rotate backups " + filename );
          TDPath.rotateBackups( filename, mRotate );
        }

        long now  = System.currentTimeMillis();
        long time = now - 600000; // ten minutes before now
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
        // Log.v("DistoX", "saving binary " + mFullName );
        DrawingIO.exportDataStream( mSurface, mType, file1, mFullName, mProjDir );

        if ( isCancelled() ) {
          // Log.v("DistoX", "save cancelled");
          file1.delete();
        } else {
          // Log.v("DistoX", "save completed");
          String filename1 = TDPath.getTdrFileWithExt( mFullName );

          (new File( filename1 )).renameTo( new File( filename1 + TDPath.BCK_SUFFIX ) );
          file1.renameTo( new File( filename1 ) );
        }
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

