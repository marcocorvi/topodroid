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

import java.io.FileWriter;
import java.io.BufferedWriter;

import android.content.Context;

import android.os.AsyncTask;
// import android.os.Message;

// import android.widget.Toast;

// import android.util.Log;

class ExportPlotToFile extends AsyncTask<Void,Void,Boolean>
{
    private final Context mContext;  // FIXME LEAK used by Toast
    private final DrawingCommandManager mCommand;
    private final DistoXNum mNum;
    private long mType;
    private String mFullName;
    private String mExt; // extension
    private String filename = null;
    private boolean mToast;
    private final DrawingUtil mDrawingUtil;

    ExportPlotToFile( Context context, DrawingCommandManager command, DrawingUtil drawingUtil,
                         DistoXNum num, long type, String name, String ext, boolean toast )
    {
      // Log.v("DistoX", "export plot to file cstr. " + name );
      // FIXME assert( ext != null );
      mContext  = context;
      mCommand  = command;
      mDrawingUtil = drawingUtil;
      mNum      = num;
      mType     = type;
      mFullName = name;
      mExt      = ext;
      mToast    = toast;
    }

    @Override
    protected Boolean doInBackground(Void... arg0)
    {
      // Log.v("DistoX", "export plot to file in bkgr. ext " + mExt );
      try {
        if ( mExt.equals("dxf") ) {
          filename = TDPath.getDxfFileWithExt( mFullName );
        } else if ( mExt.equals("svg") ) {
          filename = TDPath.getSvgFileWithExt( mFullName );
        }
        // Log.v("DistoX", "Export to File: " + filename );
        if ( filename != null ) {
          // final FileOutputStream out = new FileOutputStream( filename );
          TDPath.checkPath( filename );
          final FileWriter fw = new FileWriter( filename );
          BufferedWriter bw = new BufferedWriter( fw );
          if ( mExt.equals("dxf") ) {
            DrawingDxf.write( bw, mNum, mCommand, mType, mDrawingUtil );
          } else if ( mExt.equals("svg") ) {
            DrawingSvg.write( bw, mNum, mCommand, mType, mDrawingUtil );
          }
          fw.flush();
          fw.close();
          return true;
        }
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
          TDToast.make( mContext, mContext.getResources().getString(R.string.saved_file_1) + " " + filename );
        } else {
          TDToast.make( mContext, R.string.saving_file_failed );
        }
      }
    }
}

