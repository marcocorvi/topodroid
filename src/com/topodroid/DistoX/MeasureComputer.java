/** @file MeasureComputer.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D station distances computer
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import android.os.AsyncTask;
// import android.content.Context;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

class MeasureComputer extends AsyncTask< Void, Void, Integer >
{
  private final static int MEASURE_NO_PATH      = 0;
  private final static int MEASURE_OK           = 1;
  private final static int MEASURE_SAME_STATION = 2;
  private final static int MEASURE_NO_STATION   = 3;
  private final static int MEASURE_NO_START     = 4;
  private final static int MEASURE_NO_NAME      = 5;
  private final static int MEASURE_NO_MODEL     = 6;
  private final static int MEASURE_SKIP         = 7;

  float mX, mY;
  float[] mMVPMatrix;
  TglParser mParser;
  GlModel   mModel;
  TopoGL    mApp;
  ParserDEM mDEM;
  TglMeasure mMeasure;  // result of the measure
  String     mFullname; // station name

  MeasureComputer( TopoGL app, float x, float y, float[] MVPMatrix, TglParser parser, ParserDEM dem, GlModel model )
  {
    mX = x;
    mY = y;
    mMVPMatrix = MVPMatrix;
    mParser    = parser;
    mModel     = model;
    mApp       = app;
    mDEM       = dem;
  }

  @Override
  protected Integer doInBackground( Void ... v ) 
  {
    if ( mModel == null || mParser == null ) return MEASURE_NO_MODEL; // new Integer( MEASURE_NO_MODEL );

    mFullname = mModel.checkNames( mX, mY, mMVPMatrix, TopoGL.mSelectionRadius, (mParser.mStartStation == null) );
    if ( mFullname == null ) return MEASURE_NO_NAME; // new Integer( MEASURE_NO_NAME );

    // TDLog.v("Measure computer - station: " + mFullname + " start: " + ((mParser.mStartStation==null)?"null":"non-null") );
    if ( mParser.mStartStation == null ) {
      mModel.clearPath( );
      mParser.setStartStation( mFullname );
      return MEASURE_NO_START; // new Integer( MEASURE_NO_START );
    }

    // if ( ! mApp.mMeasureStation.isChecked() ) return MEASURE_SKIP; // new Integer( MEASURE_SKIP ); // do not measure
    if ( ! mApp.isMeasuring ) return MEASURE_SKIP; // new Integer( MEASURE_SKIP ); // do not measure

    Cave3DStation station = mParser.getStation( mFullname );
    if ( station == null ) return MEASURE_NO_STATION; new Integer( MEASURE_NO_STATION ); // null station

    if ( station == mParser.mStartStation ) return MEASURE_SAME_STATION; // new Integer( MEASURE_SAME_STATION );

    mMeasure = mParser.computeCavePathlength( station );
    if ( mMeasure.dcave > 0 && station.getPathPrevious() != null ) {
      ArrayList< Cave3DStation > path = new ArrayList< >();
      while ( station != null ) {
        path.add( station );
        station = station.getPathPrevious();
      }
      // TDLog.v("path size " + path.size() );
      // FIXME INCREMENTAL mModel.clearPath( );
      mModel.setPath( path, false ); // mModel.setPath( path, mApp.hasBluetoothName() );
	  
      return MEASURE_OK; // new Integer( MEASURE_OK );
    }
    return MEASURE_NO_PATH; // new Integer( MEASURE_NO_PATH );
  }

  @Override
  protected void onPostExecute( Integer result )
  {
    // String[] resstr = { "no path", "ok", "same staion", "no station", "no start", "no name", "no model", "skip" };
    // TDLog.v("measure result: " + resstr[result] );
  
    switch ( result.intValue() ) {
      case MEASURE_OK:
      case MEASURE_NO_PATH:
        if ( TopoGL.mMeasureToast ) {
          if ( mApp != null ) mApp.uiToast( mMeasure.getString(), false );
        } else {
          (new DialogMeasure( mApp, mMeasure )).show();
        }
        mApp.refresh();
        break;
      case MEASURE_SAME_STATION:
        mApp.closeCurrentStation();
        break;
      // case MEASURE_NO_STATION:
      //   TDLog.w("path: null station" );
      //   // mModel.clearPath( );
      //   break;
      case MEASURE_NO_START:
        if ( TopoGL.mStationDialog ) {
          (new DialogStation( mApp, mApp, mParser, mFullname, mDEM )).show();
        } else {
          Cave3DStation st = mParser.getStation( mFullname );
          if ( st != null ) {
            DEMsurface surface = (mDEM != null)? mDEM : mParser.getSurface();
            String msg = String.format(Locale.US, "%s: E %.1f N %.1f H %.1f", st.getShortName(), st.x, st.y, st.z );
            if (surface != null) {
              double zs = surface.computeZ( st.x, st.y );
              if ( zs > -1000 ) {
                zs -= st.z;
                msg = msg + String.format(Locale.US, "\nDepth %.1f", zs );
              }
            }
            mApp.showCurrentStation( msg );
            // if ( mApp != null ) mApp.uiToast( msg, false );
          }
        }
        break;
      // case MEASURE_NO_NAME:
      //   // mModel.clearPath( );
      //   // mParser.mStartStation = null;
      //   break;
      // case MEASURE_NO_START:
      // case MEASURE_NO_MODEL:
      // case MEASURE_SKIP:
      //   break;
    }
    GlRenderer.mMeasureCompute = false;
  }

}
