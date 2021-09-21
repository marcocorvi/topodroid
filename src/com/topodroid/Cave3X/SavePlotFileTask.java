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
package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

import java.lang.ref.WeakReference;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.List;

import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
// import android.os.Bundle;
import android.os.Handler;
import android.net.Uri;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;

class SavePlotFileTask extends AsyncTask<Intent,Void,Boolean>
{
  private String mFormat; 
  private Handler mHandler;
  // private TopoDroidApp mApp;
  private final WeakReference<DrawingWindow> mParent;
  private final TDNum mNum;
  // private final DrawingUtil mUtil;
  private final DrawingCommandManager mManager;
  private List< DrawingPath > mPaths;
  private String mFullName; // file fullname, or shp basepath
  private int mType;        // plot type
  private PlotInfo mInfo;   // plot info (can be null for th2 export/overview)
  private int mProjDir;
  private int mSuffix;
  private int mRotate;  // nr. backups to rotate
  private String origin = null;
  private PlotSaveData psd1 = null;
  private PlotSaveData psd2 = null;
  private Uri mUri;

  SavePlotFileTask( Context context, Uri uri, DrawingWindow parent, Handler handler,
		    TDNum num,
		    // DrawingUtil util, 
		    DrawingCommandManager manager, PlotInfo info,
                    String fullname, long type, int proj_dir, int suffix, int rotate )
  {
     mUri      = uri;
     mFormat   = context.getResources().getString(R.string.saved_file_2);
     mParent   = new WeakReference<DrawingWindow>( parent );
     mHandler  = handler;
     // mApp      = app;
     mNum      = num;
     // mUtil     = util;
     mManager  = manager;
     mInfo     = info;
     mPaths    = null;
     mFullName = fullname;
     mType     = (int)type;
     mProjDir  = proj_dir;
     mSuffix   = suffix;    // plot save mode
     mRotate   = rotate;
     if ( mRotate > TDPath.NR_BACKUP ) mRotate = TDPath.NR_BACKUP;
     // TDLog.Log( TDLog.LOG_PLOT, "Save Plot File Task [1] " + mFullName + " type " + mType + " suffix " + suffix);
     TDLog.v( "save plot file task [1] " + mFullName + " type " + mType + " suffix " + suffix );

     // if ( mSuffix == PlotSave.SAVE && TDSetting.mExportPlotFormat == TDConst.SURVEY_FORMAT_CSX ) { // auto-export format cSurvey
     //   // TDLog.v( "auto export CSX");
     //   origin = parent.getOrigin();
     //   psd1 = parent.makePlotSaveData( 1, suffix, rotate );
     //   psd2 = parent.makePlotSaveData( 2, suffix, rotate );
     // }
  }

  SavePlotFileTask( Context context, Uri uri, DrawingWindow parent, Handler handler,
                    // TopoDroidApp app,
		    TDNum num,
		    List< DrawingPath > paths, PlotInfo info,
                    String fullname, long type, int proj_dir )
  {
     mUri      = uri;
     mFormat   = context.getResources().getString(R.string.saved_file_2);
     mParent   = new WeakReference<DrawingWindow>( parent );
     mHandler  = handler;
     // mApp      = app;
     mNum      = num;
     mManager  = null;
     mPaths    = paths;
     mInfo     = info;
     mFullName = fullname;
     mType     = (int)type;
     mProjDir  = proj_dir;
     mSuffix   = PlotSave.CREATE;
     mRotate   = 0;
     // TDLog.Log( TDLog.LOG_PLOT, "Save Plot File Task [2] " + mFullName + " type " + mType );
     TDLog.v( "save plot file task [2] " + mFullName + " type " + mType + " suffix CREATE");
  }

  @Override
  protected Boolean doInBackground(Intent... arg0)
  {
    boolean ret1 = true; // false = png failed
    boolean ret2 = true; // false = binary cancelled
    // boolean do_binary = (TDSetting.mBinaryTh2 && mSuffix != PlotSave.EXPORT ); // TDR BINARY

    // TDLog.v( "save plot file task bkgr start");
    // synchronized( TDPath.mTherionLock ) // FIXME-THREAD_SAFE
    {
      TDLog.v( "save scrap files " + mFullName + " suffix " + mSuffix );

      // first pass: export
      if ( mSuffix == PlotSave.EXPORT ) {
        TDLog.v( "save plot Therion file EXPORT " + mFullName );
        if ( mManager != null ) {
          // File file2 = TDFile.getFile( TDPath.getTh2FileWithExt( mFullName ) );
          // DrawingIO.exportTherion( mManager, mType, file2, mFullName, PlotType.projName( mType ), mProjDir, false ); // single sketch
          try {
            BufferedWriter bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getTh2FileWithExt( mFullName ) ) );
            DrawingIO.exportTherion( mManager, mType, bw, mFullName, PlotType.projName( mType ), mProjDir, false ); // single sketch
            bw.flush();
            bw.close();
          } catch ( IOException e ) { TDLog.v( e.getMessage() ); e.printStackTrace(); }
        }
      } else if ( mSuffix == PlotSave.SAVE ) {
        // // TDLog.v( "save plot Therion file SAVE " + mFullName );
        // switch ( TDSetting.mExportPlotFormat ) { // auto-export format
        //   case TDConst.SURVEY_FORMAT_TH2:
        //     if ( mManager != null ) {
        //       File file2 = TDFile.getFile( TDPath.getTh2FileWithExt( mFullName ) );
        //       DrawingIO.exportTherion( mManager, mType, file2, mFullName, PlotType.projName( mType ), mProjDir, false ); // single sketch
        //     }
        //     break;
        //   case TDConst.SURVEY_FORMAT_DXF:
	//     if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
        //       mParent.get().doSaveWithExt( mNum, mManager, mType, mFullName, "dxf", false );
	//     }
        //     break;
        //   case TDConst.SURVEY_FORMAT_SVG:
	//     if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
        //       mParent.get().doSaveWithExt( mNum, mManager, mType, mFullName, "svg", false );
	//     }
        //     break;
        //   case TDConst.SURVEY_FORMAT_SHP:
	//     if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
        //       mParent.get().doSaveWithExt( mNum, mManager, mType, mFullName, "shp", false );
	//     }
        //     break;
        //   case TDConst.SURVEY_FORMAT_XVI:
	//     if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
        //       mParent.get().doSaveWithExt( mNum, mManager, mType, mFullName, "xvi", false );
	//     }
        //     break;
        //   // case TDConst.SURVEY_FORMAT_C3D:
	//   //   if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
        //   //     mParent.get().doSaveWithExt( mNum, mManager, mType, mFullName, "c3d", false );
	//   //   }
        //   //   break;
        //   case TDConst.SURVEY_FORMAT_CSX: // IMPORTANT CSX must come before PNG
        //     if ( PlotType.isSketch2D( mType ) ) {
	//       if ( mParent.get() != null && ! mParent.get().isFinishing() ) {
        //         mParent.get().doSaveCsx( origin, psd1, psd2, false );
	//       }
        //       break;
        //     } else { // X-Section cSurvey are exported as PNG
        //       // fall-through
        //     }
        //   case TDConst.SURVEY_FORMAT_PNG:
        //     if ( mManager != null ) {
        //       Bitmap bitmap = mManager.getBitmap();
        //       if (bitmap == null) {
        //         TDLog.Error( "cannot save PNG: null bitmap" );
        //         ret1 = false;
        //       } else {
        //         float scale = mManager.getBitmapScale();
        //         if (scale > 0) {
        //           // FIXME execute must be called from the main thread, current thread is working thread
        //           (new ExportBitmapToFile( mFormat, bitmap, scale, mFullName, false )).exec();
        //         } else {
        //           TDLog.Error( "cannot save PNG: negative scale" );
        //           ret1 = false;
        //         }
        //       }
        //     }
        //     break;
        // }
      } else if ( mSuffix == PlotSave.OVERVIEW ) {
        TDLog.v( "save plot Therion file OVERVIEW " + mFullName );
        // File file = TDFile.getFile( TDPath.getTh2FileWithExt( mFullName ) );
        // DrawingIO.exportTherion( mManager, mType, file, mFullName, PlotType.projName( mType ), mProjDir, true ); // multi-sketch
        try {
          BufferedWriter bw = new BufferedWriter( (mUri != null)? TDsafUri.docFileWriter( mUri ) : new FileWriter( TDPath.getTh2FileWithExt( mFullName ) ) );
          DrawingIO.exportTherion( mManager, mType, bw, mFullName, PlotType.projName( mType ), mProjDir, true ); // multi-sketch
          // bw.flush(); // FIXME necessary ???
          bw.close();
        } catch ( IOException e ) { TDLog.v( e.getMessage() ); e.printStackTrace(); }
	return true;
      }
      
      // second pass: save
      if ( mSuffix != PlotSave.EXPORT ) {
        TDLog.v( "save plot not-EXPORT");
        assert( mInfo != null );

        String filename = TDPath.getTdrFileWithExt( mFullName ) + TDPath.BCK_SUFFIX;

        // TDLog.v( "rotate backups " + filename );
        TDPath.rotateBackups( filename, mRotate ); // does not do anything if mRotate <= 0

        long now  = System.currentTimeMillis();
        TDFile.clearExternalTempDir( 600000 ); // clean the cache ten minutes before now

        // String tempname1 = TDPath.getTmpFileWithExt( Integer.toString(mSuffix) + Long.toString(now) );
        // File file1 = TDFile.getFile( tempname1 );
        File file1 = TDFile.getExternalTempFile( Integer.toString(mSuffix) + Long.toString(now) );

        // TDLog.Log( TDLog.LOG_PLOT, "saving binary " + mFullName );
        // TDLog.v( "saving binary " + mFullName + " file " + file1.getPath() );
        if ( mSuffix == PlotSave.CREATE ) {
          // TDLog.v("Save Plot CREATE file " + file1 + " paths " + mPaths.size() );
          DrawingIO.exportDataStreamFile( mPaths, mType, mInfo, file1, mFullName, mProjDir, 0 ); // set path scrap to 0
        } else {
          if ( mManager != null ) {
            DrawingIO.exportDataStreamFile( mManager, mType, mInfo, file1, mFullName, mProjDir );
          }
        }

        if ( isCancelled() ) {
          TDLog.Error( "binary save cancelled " + mFullName );
          // if ( ! file1.delete() ) TDLog.Error("File delete error"); // no need to delete cache file
          ret2 = false;
        } else {
          // TDLog.Log( TDLog.LOG_PLOT, "save binary completed" + mFullName );
          // TDLog.v( "save binary completed" + mFullName );

          String filename1 = TDPath.getTdrFileWithExt( mFullName );
          File file0 = TDFile.getTopoDroidFile( filename1 );
          if ( file0.exists() ) {
            if ( ! TDFile.renameTempFile( file0, filename1 + TDPath.BCK_SUFFIX ) ) {
              TDLog.Error("failed rename " + filename1 + TDPath.BCK_SUFFIX );
            }
          }
          if ( ! TDFile.renameTempFile( file1, filename1 ) ) {
            TDLog.Error("failed rename " + filename1 );
          }
        }
      }
    }
    // TDLog.v( "save plot file task bkgr done");
    return ret1 && ret2;
  }

  @Override
  protected void onPostExecute(Boolean bool)
  {
    super.onPostExecute(bool);
    // TDLog.v( "save plot file task post exec");
    if ( mHandler != null ) {
      mHandler.sendEmptyMessage( bool? 661 : 660 );
    }
  }
}

