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
import com.topodroid.utils.TDsaf;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.io.dxf.DrawingDxf;
import com.topodroid.io.shp.DrawingShp;
import com.topodroid.io.svg.DrawingSvg;
import com.topodroid.io.svg.DrawingSvgWalls;
import com.topodroid.io.svg.DrawingTunnel;

import android.util.Log;

// import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;

import android.content.Context;

import android.os.AsyncTask;
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
    private final Uri mUri;

    ExportPlotToFile( Context context, Uri uri, SurveyInfo info, PlotInfo plot, FixedInfo fixed,
                      TDNum num, DrawingCommandManager command,
                      long type, String name, String ext, boolean toast, GeoReference station )
    {
      // Log.v("DistoX-C3D", "export plot to file cstr. " + type + " " + name + "  " + ((station == null)? "no geo" : station.toString() ) );
      // FIXME assert( ext != null );
      mUri       = uri;
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
      // Log.v("DistoX-EXPORT", "export plot to file in bkgr. ext " + mExt );
      // String dirname = null;
      try {
        // if ( mExt.equals("dxf") ) {
        //   filename = TDPath.getDxfFileWithExt( mFullName );
        //   // dirname  = TDPath.getDxfFile( "" );
        // } else if ( mExt.equals("svg") ) {
        //   filename = TDPath.getSvgFileWithExt( mFullName );
        //   // dirname  = TDPath.getSvgFile( "" );
        // } else if ( mExt.equals("shp") ) {
        //   filename = TDPath.getShpBasepath( mFullName );
        //   // dirname  = TDPath.getShzFile( "" );
        // } else if ( mExt.equals("xvi") ) {
        //   filename = TDPath.getXviFileWithExt( mFullName );
        //   // dirname  = TDPath.getXviFile( "" );
        // } else if ( mExt.equals("xml") ) {
        //   filename = TDPath.getTnlFileWithExt( mFullName );
        //   // dirname  = TDPath.getTnlFile( "" );
        // } else if ( mExt.equals("c3d") ) {
        //   filename = TDPath.getC3dFileWithExt( mFullName );
        //   // dirname  = TDPath.getC3dFile( "" );
	// } else { // unexpected extension
	//   return false;
        // }
        // Log.v("DistoX-EXPORT", "export plot to file " + filename );
        // TDLog.Log( TDLog.LOG_IO, "export plot to file " + filename );
        boolean ret = true;
        synchronized ( TDFile.mFilesLock ) {
          // final FileOutputStream out = TDFile.getFileOutputStream( filename );
          if ( mExt.equals("shp") ) { 
            FileOutputStream fos = TDsaf.docFileOutputStream( mUri );
	    DrawingShp.writeShp( fos, mFullName, mCommand, mType, mStation );
	  } else {
            // File temp = File.createTempFile( "tmp", null, TDFile.getFile( dirname ) );
            // final FileWriter fw = TDFile.getFileWriter( temp );
            // BufferedWriter bw = new BufferedWriter( fw );
            BufferedWriter bw = null;
            if ( mExt.equals("dxf") ) {
              // bw = TDFile.getMSwriter( "dxf", mFullName + ".dxf", "text/dxf" );
              bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
              DrawingDxf.writeDxf( bw, mNum, mCommand, mType );
            } else if ( mExt.equals("svg") ) {
              String name = mFullName + ".svg"; // file-name
              // bw = TDFile.getMSwriter( "svg", name, "text/svg" );
              bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
              if ( TDSetting.mSvgRoundTrip ) {
                // List<String> segments = mUri.getPathSegments();
                (new DrawingSvgWalls()).writeSvg( name, bw, mNum, mCommand, mType );
              } else {
                (new DrawingSvg()).writeSvg( bw, mNum, mCommand, mType );
              }
            } else if ( mExt.equals("xvi") ) {
              // bw = TDFile.getMSwriter( "xvi", mFullName + ".xvi", "text/xvi" );
              bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
              DrawingXvi.writeXvi( bw, mNum, mCommand, mType );
            } else if ( mExt.equals("xml") ) {
              // bw = TDFile.getMSwriter( "tnl", mFullName + ".xml", "text/xml" );
              bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
              (new DrawingTunnel()).writeXml( bw, mInfo, mNum, mCommand, mType );
            } else if ( mExt.equals("c3d") ) {
              // Log.v("DistoX-C3D", "Export to Cave3D: " + mFullName );
              // bw = TDFile.getMSwriter( "c3d", mFullName + ".c3d", "text/c3d" );
              bw = new BufferedWriter( TDsaf.docFileWriter( mUri ) );
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
      }
      return false;
    }


    @Override
    protected void onPostExecute(Boolean bool) 
    {
      // Log.v("DistoX", "export plot to file post exec");
      super.onPostExecute(bool);
      if ( mToast ) {
        if ( bool ) {
          // TDToast.make( String.format( mFormat, mFullName + "." + mExt ) );
          TDToast.make( String.format( mFormat, mExt ) );
        } else {
          TDToast.makeBad( R.string.saving_file_failed );
        }
      }
    }
}

