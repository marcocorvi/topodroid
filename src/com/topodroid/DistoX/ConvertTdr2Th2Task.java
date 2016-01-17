/** @file ConvertTdr2Th2Task.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid drawing: save drawing in therion format
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;;

import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.Context;

import android.os.AsyncTask;
// import android.os.Bundle;
import android.os.Handler;

import android.graphics.RectF;

import android.util.Log;

class ConvertTdr2Th2Task extends AsyncTask<Intent,Void,Boolean>
{
  private Context mContext;
  private Handler mHandler;
  private TopoDroidApp mApp;
  private long mSid;
  private String mSurvey;

  public ConvertTdr2Th2Task( Context context, Handler handler, TopoDroidApp app )
  {
     mContext  = context;
     mHandler  = handler;
     mApp      = app;
     mSid      = app.mSID;
     mSurvey   = app.mySurvey;
  }

  @Override
  protected Boolean doInBackground(Intent... arg0)
  {
    List<DistoXDBlock> list = null;
    DistoXNum num = null;
    float xoff = 0;
    float yoff = 0;
    long  type = PlotInfo.PLOT_PLAN;
    if ( TDSetting.mAutoStations ) {
      list = mApp.mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
    }

    List< PlotInfo > plots = mApp.mData.selectAllPlots( mSid );
    for ( PlotInfo plot : plots ) {
      String fullname = mSurvey + "-" + plot.name;
      String tdr = TDPath.getTdrFileWithExt( fullname );
      String th2 = TDPath.getTh2FileWithExt( fullname );
      File tdrfile = new File( tdr );
      File th2file = new File( th2 );
      if ( tdrfile.exists() ) {
        if ( TDSetting.mAutoStations ) {
          String start = plot.start;
          String view  = plot.view;
          String hide  = plot.hide;
          type = plot.type;
          // xoff = plot.xoffset;
          // yoff = plot.yoffset;
          num  = new DistoXNum( list, start, view, hide );
        }
        try {
          FileWriter fw = new FileWriter( th2file );
          BufferedWriter bw = new BufferedWriter( fw );
          RectF bbox = new RectF();
          DrawingIO.dataStream2Therion( tdrfile, bw, bbox, false ); // false == no endscrap
          if ( TDSetting.mAutoStations &&
               ( type == PlotInfo.PLOT_PLAN || type == PlotInfo.PLOT_EXTENDED ) ) {
            float toTherion = TopoDroidConst.TO_THERION;
            NumStationSet nss = num.mStations;
            List< NumStation > ns = nss.getStations();
            float x = 0;
            float y = 0;
            for ( NumStation st : ns ) {
              if ( type == PlotInfo.PLOT_PLAN ) {
                x = DrawingUtil.toSceneX( st.e ) - xoff;
                y = DrawingUtil.toSceneY( st.s ) - yoff;
              } else {
                x = DrawingUtil.toSceneX( st.h ) - xoff;
                y = DrawingUtil.toSceneY( st.v ) - yoff;
              }
              if ( bbox.left > x || bbox.right  < x ) continue;
              if ( bbox.top  > y || bbox.bottom < y ) continue;
              bw.write( String.format(Locale.ENGLISH,
                           "point %.2f %.2f station -name \"%s\"\n", x*toTherion, -y*toTherion, st.name ) );
            }
          }
          bw.write("\nendscrap\n");
          bw.flush();
          bw.close();
          fw.close();
        } catch ( IOException e ) {
          TDLog.Error("I/O error " + th2 );
        }
      } else {
        TDLog.Error("Missing file " + tdr );
      }

    }
    return true;
  }

  @Override
  protected void onPostExecute(Boolean bool)
  {
    super.onPostExecute(bool);
    if ( mHandler != null ) {
      mHandler.sendEmptyMessage( 999 );
    }
  }
}

