/* @file ExportPlotToFile.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid export plot to file
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.io.dxf.DrawingDxf;
import com.topodroid.io.shp.DrawingShp;
import com.topodroid.io.svg.DrawingSvg;
import com.topodroid.io.svg.DrawingSvgWalls;
import com.topodroid.io.svg.DrawingTunnel;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;

import android.content.Context;

import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
// import android.os.Message;
import android.net.Uri;

class ExportPlotToFile extends AsyncTask<Void,Void,Boolean>
{
    private final DrawingCommandManager mCommand;
    private final SurveyInfo mInfo;
    private final TDNum mNum;
    private final long mType;
    private final String mFullName; // "survey-plotX" name ;
    private final String mExt; // extension
    // private String filename = null;
    private final boolean mToast;
    private final String mFormat;
    private final GeoReference mStation; // for shp
    private final FixedInfo mFixedInfo;  // for c3d
    private final PlotInfo  mPlotInfo;
    private Uri mUri = null;

    /** constructor
     * @param content context, for the resources
     * @param uri     export URI
     * @param info    survey info
     * @param plot    plot info
     * @param fixed   fixed info, for georeference
     * @param num     data reduction
     * @param command plot drawing items
     * @param type
     * @param name    full filename
     * @param ext     extension, used to decide how to export the plot data
     * @param toast   whether to toast
     * @param station georeference station
     */
    ExportPlotToFile( Context context, Uri uri, SurveyInfo info, PlotInfo plot, FixedInfo fixed,
                      TDNum num, DrawingCommandManager command,
                      long type, String name, String ext, boolean toast, GeoReference station )
    {
      TDLog.v("export plot to file cstr. Type " + type + " fullname " + name + " ext " + ext );
      // FIXME assert( ext != null );
      /* if ( TDSetting.mExportUri ) */ mUri = uri; // FIXME_URI
      mFormat    = context.getResources().getString(R.string.saved_file_1);
      mInfo      = info;
      mPlotInfo  = plot;
      mFixedInfo = fixed;
      mNum       = num;
      mCommand   = command;
      mType      = type;
      mFullName  = name;
      mExt       = ext;
      mToast     = toast;
      mStation   = station;
    }

    @Override
    protected Boolean doInBackground(Void... arg0)
    {
      // TDLog.v("export plot to file in bkgr. ext " + mExt );
      // String dirname = null;
      ParcelFileDescriptor pfd = TDsafUri.docWriteFileDescriptor( mUri );
      if ( pfd == null ) return false;
      try {
        // TDLog.v("export plot to file: <" + mFullName + "> <" + mExt + ">" );
        // TDLog.Log( TDLog.LOG_IO, "export plot to file " + filename );
        boolean ret = true;
        synchronized ( TDFile.mFilesLock ) {
          // final FileOutputStream out = TDFile.getFileOutputStream( filename );
          if ( mExt.equals("shp") ) { 
            // FileOutputStream fos = (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( TDPath.getShpFileWithExt( mFullName ) );
            FileOutputStream fos = TDsafUri.docFileOutputStream( pfd );
            String dirpath = TDPath.getShpTempRelativeDir();
	    DrawingShp.writeShp( fos, dirpath, mCommand, mType, mStation );
	    // DrawingShp.writeShp( fos, mFullName, mCommand, mType, mStation );
            TDFile.deleteDir( dirpath );
            fos.close();
	  } else {
            BufferedWriter bw = null;
            if ( mExt.equals("dxf") ) {
              // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getDxfFileWithExt( mFullName ) ) );
              bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
              DrawingDxf.writeDxf( bw, mNum, mCommand, mType );
            } else if ( mExt.equals("svg") ) {
              String name = mFullName + ".svg"; // file-name
              // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getSvgFileWithExt( mFullName ) ) );
              bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
              if ( TDSetting.mSvgRoundTrip ) {
                // List<String> segments = pfd.getPathSegments();
                (new DrawingSvgWalls()).writeSvg( name, bw, mNum, mCommand, mType );
              } else {
                (new DrawingSvg()).writeSvg( bw, mNum, mCommand, mType );
              }
            } else if ( mExt.equals("xvi") ) {
              // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getXviFileWithExt( mFullName ) ) );
              bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
              DrawingXvi.writeXvi( bw, mNum, mCommand, mType );
            } else if ( mExt.equals("xml") ) {
              // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getTnlFileWithExt( mFullName ) ) );
              bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
              (new DrawingTunnel()).writeXml( bw, mInfo, mNum, mCommand, mType );
            } else if ( mExt.equals("c3d") ) {
              // TDLog.v("C3D export to Cave3D: " + mFullName );
              // bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : new FileWriter( TDPath.getC3dFileWithExt( mFullName ) ) );
              bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
              ret = DrawingIO.exportCave3D( bw, mCommand, mNum, mPlotInfo, mFixedInfo, mFullName );
            }
            if ( bw != null ) {
              bw.flush();
              bw.close();
            }
	  }
        }
        return ret;
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        TDsafUri.closeFileDescriptor( pfd );
      }
      return false;
    }


    @Override
    protected void onPostExecute(Boolean bool) 
    {
      // TDLog.v( "export plot to file post exec");
      super.onPostExecute(bool);
      if ( mToast ) {
        if ( bool ) {
          TDToast.make( String.format( mFormat, mFullName ) ); // mExt ) );
        } else {
          TDToast.makeBad( R.string.saving_file_failed );
        }
      }
    }
}

