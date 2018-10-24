/* @file SavePlotFileTask.java
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

class SavePlotFileTask extends AsyncTask<Intent,Void,Boolean>
{
  private String mFormat; 
  private Handler mHandler;
  // private TopoDroidApp mApp;
  private final DrawingWindow mParent;
  private final DistoXNum mNum;
  // private final DrawingUtil mUtil;
  private final DrawingCommandManager mManager;
  private List<DrawingPath> mPaths;
  private String mFullName;
  private int mType;    // plot type
  private int mProjDir;
  private int mSuffix;
  private int mRotate;  // nr. backups to rotate
  private String origin = null;
  private PlotSaveData psd1 = null;
  private PlotSaveData psd2 = null;

  SavePlotFileTask( Context context, DrawingWindow parent, Handler handler,
		    DistoXNum num,
		    // DrawingUtil util, 
		    DrawingCommandManager manager, 
                    String fullname, long type, int proj_dir, int suffix, int rotate )
  {
     mFormat   = context.getResources().getString(R.string.saved_file_2);
     mParent   = parent;
     mHandler  = handler;
     // mApp      = app;
     mNum      = num;
     // mUtil     = util;
     mManager  = manager;
     mPaths    = null;
     mFullName = fullname;
     mType     = (int)type;
     mProjDir  = proj_dir;
     mSuffix   = suffix;
     mRotate   = rotate;
     if ( mRotate > TDPath.NR_BACKUP ) mRotate = TDPath.NR_BACKUP;
     // TDLog.Log( TDLog.LOG_PLOT, "Save Plot File Task " + mFullName + " type " + mType );
     // Log.v( "DistoX", "save plot file task [1] " + mFullName + " type " + mType );
     if ( mSuffix == PlotSave.SAVE && TDSetting.mExportPlotFormat == TDConst.DISTOX_EXPORT_CSX ) { // auto-export format cSurvey
       origin = parent.getOrigin();
       psd1 = parent.makePlotSaveData( 1, suffix, rotate );
       psd2 = parent.makePlotSaveData( 2, suffix, rotate );
     }
  }

  SavePlotFileTask( Context context, DrawingWindow parent, Handler handler,
                    // TopoDroidApp app,
		    DistoXNum num,
		    // DrawingUtil util,
		    List<DrawingPath> paths,
                    String fullname, long type, int proj_dir )
  {
     mFormat   = context.getResources().getString(R.string.saved_file_2);
     mParent   = parent;
     mHandler  = handler;
     // mApp      = app;
     mNum      = num;
     // mUtil     = util;
     mManager  = null;
     mPaths    = paths;
     mFullName = fullname;
     mType     = (int)type;
     mProjDir  = proj_dir;
     mSuffix   = PlotSave.CREATE;
     mRotate   = 0;
     // TDLog.Log( TDLog.LOG_PLOT, "Save Plot File Task " + mFullName + " type " + mType );
     // Log.v( "DistoX", "save plot file task [2] " + mFullName + " type " + mType );
  }

  @Override
  protected Boolean doInBackground(Intent... arg0)
  {
    boolean ret1 = true; // false = png failed
    boolean ret2 = true; // false = binary cancelled
    // boolean do_binary = (TDSetting.mBinaryTh2 && mSuffix != PlotSave.EXPORT ); // TDR BINARY

    // Log.v( "DistoX", "save plot file task bkgr start");
    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    {
      // Log.v("DistoX", "save scrap files " + mFullName + " suffix " + mSuffix );

      // first pass: export
      if ( mSuffix == PlotSave.EXPORT ) {
        if ( mManager != null ) {
          File file2 = new File( TDPath.getTh2FileWithExt( mFullName ) );
          DrawingIO.exportTherion( mManager, mType, file2, mFullName, PlotInfo.projName[ mType ], mProjDir );
        }
      } else if ( mSuffix == PlotSave.SAVE ) {
        switch ( TDSetting.mExportPlotFormat ) { // auto-export format
          case TDConst.DISTOX_EXPORT_TH2:
            if ( mManager != null ) {
              File file2 = new File( TDPath.getTh2FileWithExt( mFullName ) );
              DrawingIO.exportTherion( mManager, mType, file2, mFullName, PlotInfo.projName[ mType ], mProjDir );
            }
            break;
          case TDConst.DISTOX_EXPORT_DXF:
            mParent.doSaveWithExt( mNum, /* mUtil, */ mManager, mType, mFullName, "dxf", false );
            break;
          case TDConst.DISTOX_EXPORT_SVG:
            mParent.doSaveWithExt( mNum, /* mUtil, */ mManager, mType, mFullName, "svg", false );
            break;
          case TDConst.DISTOX_EXPORT_CSX: // IMPORTANT CSX must come before PNG
            if ( PlotInfo.isSketch2D( mType ) ) {
              mParent.doSaveCsx( origin, psd1, psd2, false );
              break;
            } else { // X-Section cSurvey are exported as PNG
              // fall-through
            }
          case TDConst.DISTOX_EXPORT_PNG:
            if ( mManager != null ) {
              Bitmap bitmap = mManager.getBitmap();
              if (bitmap == null) {
                TDLog.Error( "cannot save PNG: null bitmap" );
                ret1 = false;
              } else {
                float scale = mManager.getBitmapScale();
                if (scale > 0) {
                  new ExportBitmapToFile( mFormat, bitmap, scale, mFullName, false ).execute();
                } else {
                  TDLog.Error( "cannot save PNG: negative scale" );
                  ret1 = false;
                }
              }
            }
            break;
        }
      }
      
      // second pass: save
      if ( mSuffix != PlotSave.EXPORT ) {

        String filename = TDPath.getTdrFileWithExt( mFullName ) + TDPath.BCK_SUFFIX;

        // Log.v("DistoX", "rotate backups " + filename );
        TDPath.rotateBackups( filename, mRotate ); // does not do anything if mRotate <= 0

        long now  = System.currentTimeMillis();
        long time = now - 600000; // ten minutes before now
        // TDPath.checkPath( tempname1 );
        File tmpDir = TDPath.getTmpDir();
        File[] files = tmpDir.listFiles();
        for ( File f : files ) {
          if ( f.getName().endsWith("tmp") && f.lastModified() < time ) {
            // TDLog.Log( TDLog.LOG_PLOT, "delete temp file " + f.getAbsolutePath() );
            if ( ! f.delete() ) TDLog.Error("File delete error");
          }
        }

        String tempname1 = TDPath.getTmpFileWithExt( Integer.toString(mSuffix) + Long.toString(now) );
        File file1 = new File( tempname1 );
        // TDLog.Log( TDLog.LOG_PLOT, "saving binary " + mFullName );
        // Log.v( "DistoX", "saving binary " + mFullName );
        if ( mSuffix == PlotSave.CREATE ) {
          DrawingIO.exportDataStream( mPaths, mType, file1, mFullName, mProjDir );
        } else {
          if ( mManager != null ) {
            DrawingIO.exportDataStream( mManager, mType, file1, mFullName, mProjDir );
          }
        }

        if ( isCancelled() ) {
          TDLog.Error( "binary save cancelled " + mFullName );
          if ( ! file1.delete() ) TDLog.Error("File delete error");
          ret2 = false;
        } else {
          // TDLog.Log( TDLog.LOG_PLOT, "save binary completed" + mFullName );
          // Log.v( "DistoX", "save binary completed" + mFullName );
          String filename1 = TDPath.getTdrFileWithExt( mFullName );
          File file0 = new File( filename1 );
          if ( file0.exists() ) file0.renameTo( new File( filename1 + TDPath.BCK_SUFFIX ) );
          file1.renameTo( new File( filename1 ) );
        }
      }
    }
    // Log.v( "DistoX", "save plot file task bkgr done");
    return ret1 && ret2;
  }

  @Override
  protected void onPostExecute(Boolean bool)
  {
    super.onPostExecute(bool);
    // Log.v( "DistoX", "save plot file task post exec");
    if ( mHandler != null ) {
      mHandler.sendEmptyMessage( bool? 661 : 660 );
    }
  }
}

