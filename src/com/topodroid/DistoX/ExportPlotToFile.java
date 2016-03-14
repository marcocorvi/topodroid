/* @file ExportPlotToFile.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid export plot to file
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.FileWriter;
import java.io.BufferedWriter;

import android.content.Context;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

class ExportPlotToFile extends AsyncTask<Void,Void,Boolean> 
{
    private Context mContext;
    private DrawingCommandManager mCommand;
    private DistoXNum mNum;
    private long mType;
    private Handler mHandler;
    private String mFullName;
    private String mExt; // extension

    public ExportPlotToFile( Context context, Handler handler, DrawingCommandManager command,
                         DistoXNum num, long type, String name, String ext )
    {
       // FIXME assert( ext != null );
       mContext  = context;
       mCommand  = command;
       mNum = num;
       mType = type;
       mHandler  = handler;
       mFullName = name;
       mExt = ext;
    }

    @Override
    protected Boolean doInBackground(Void... arg0)
    {
      try {
        String filename = null;
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
            DrawingDxf.write( bw, mNum, mCommand, mType );
          } else if ( mExt.equals("svg") ) {
            DrawingSvg.write( bw, mNum, mCommand, mType );
          }
          fw.flush();
          fw.close();
          return true;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      // if ( mHandler != null) mHandler.post(completeRunnable);
      return false;
    }


    @Override
    protected void onPostExecute(Boolean bool) 
    {
      super.onPostExecute(bool);
      if ( mHandler != null ) mHandler.sendEmptyMessage( bool? 1 : 0 );
    }
}

