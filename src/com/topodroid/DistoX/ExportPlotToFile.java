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

import android.util.Log;

import java.io.FileWriter;
import java.io.BufferedWriter;

import android.content.Context;

import android.os.AsyncTask;
// import android.os.Message;

class ExportPlotToFile extends AsyncTask<Void,Void,Boolean>
{
    private final DrawingCommandManager mCommand;
    private final SurveyInfo mInfo;
    private final TDNum mNum;
    private final long mType;
    private final String mFullName; // "survey-plotX" name ;
    private final String mExt; // extension
    private String filename = null;
    private final boolean mToast;
    // private final DrawingUtil mUtil;
    private final String mFormat;
    private final GeoReference mStation;

    ExportPlotToFile( Context context, SurveyInfo info,
                      TDNum num, /* DrawingUtil util, */ DrawingCommandManager command,
                      long type, String name, String ext, boolean toast, GeoReference station )
    {
      // Log.v("DistoX", "export plot to file cstr. " + name );
      // FIXME assert( ext != null );
      mFormat   = context.getResources().getString(R.string.saved_file_1);
      mInfo     = info;
      mCommand  = command;
      // mUtil     = util;
      mNum      = num;
      mType     = type;
      mFullName = name;
      mExt      = ext;
      mToast    = toast;
      mStation  = station;
    }

    @Override
    protected Boolean doInBackground(Void... arg0)
    {
      // Log.v("DistoX-EXPORT", "export plot to file in bkgr. ext " + mExt );
      try {
        if ( mExt.equals("dxf") ) {
          filename = TDPath.getDxfFileWithExt( mFullName );
        } else if ( mExt.equals("svg") ) {
          filename = TDPath.getSvgFileWithExt( mFullName );
        } else if ( mExt.equals("shp") ) {
          filename = TDPath.getShpBasepath( mFullName );
        } else if ( mExt.equals("xvi") ) {
          filename = TDPath.getXviFileWithExt( mFullName );
        } else if ( mExt.equals("xml") ) {
          filename = TDPath.getTnlFileWithExt( mFullName );
	} else { // unexpected extension
	  return false;
        }
        // Log.v("DistoX", "Export to File: " + filename );
        // if ( filename != null ) { // always true
          // final FileOutputStream out = new FileOutputStream( filename );
          TDLog.Log( TDLog.LOG_IO, "export plot to file " + filename );
          if ( mExt.equals("shp") ) { 
	    DrawingShp.write( filename, mCommand, mType, mStation );
	  } else {
            TDPath.checkPath( filename );
            final FileWriter fw = new FileWriter( filename );
            BufferedWriter bw = new BufferedWriter( fw );
            if ( mExt.equals("dxf") ) {
              DrawingDxf.write( bw, mNum, /* mUtil, */ mCommand, mType );
            } else if ( mExt.equals("svg") ) {
              if ( TDSetting.mSvgRoundTrip ) {
                (new DrawingSvgWalls()).write( filename, bw, mNum, /* mUtil, */ mCommand, mType );
              } else {
                (new DrawingSvg()).write( bw, mNum, /* mUtil, */ mCommand, mType );
              }
            } else if ( mExt.equals("xvi") ) {
              DrawingXvi.write( bw, mNum, /* mUtil, */ mCommand, mType );
            } else if ( mExt.equals("xml") ) {
              (new DrawingTunnel()).write( bw, mInfo, mNum, /* mUtil, */ mCommand, mType );
            }
            fw.flush();
            fw.close();
	  }
          return true;
        // }
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
          TDToast.make( String.format( mFormat, filename ) );
        } else {
          TDToast.makeBad( R.string.saving_file_failed );
        }
      }
    }
}

