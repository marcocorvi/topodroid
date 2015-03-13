/* @file DrawingStationName.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing station name (this is not a station point) 
*        type: DRAWING_PATH_NAME
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

// import android.util.Log;

/**
 */
public class DrawingStationName extends DrawingPointPath
{
  private static float toTherion = TopoDroidConst.TO_THERION;

  String mName; // station name
  NumStation mStation;
  // float mX;     // scene coordinates (cx, cy)
  // float mY;
  boolean mDuplicate; // whether this is a duplicated station

  public DrawingStationName( String name, float x, float y )
  {
    super( DrawingBrushPaths.mPointLib.mPointLabelIndex,
           x, // scene coordinate
           y, 
           DrawingPointPath.SCALE_M, null );
    mType = DRAWING_PATH_NAME; // override DrawingPath.mType
    mStation = null;

    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "DrawingStationName cstr " + num_st.name + " " + x + " " + y );
    mName = name;
    cx = x; // scene coordinate
    cy = y; 
    mDuplicate = false;
    makeStraightPath( 0, 0, 2*TopoDroidSetting.mStationSize*mName.length(), 0, cx, cy );
  }

  public DrawingStationName( NumStation num_st, float x, float y )
  {
    super( DrawingBrushPaths.mPointLib.mPointLabelIndex,
           x, // scene coordinate
           y, 
           DrawingPointPath.SCALE_M, null );
    mType = DRAWING_PATH_NAME; // override DrawingPath.mType
    mStation = num_st;

    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "DrawingStationName cstr " + num_st.name + " " + x + " " + y );
    if ( num_st.mDuplicate ) mPaint = DrawingBrushPaths.duplicateStationPaint;
    mName = num_st.name;
    cx = x; // scene coordinate
    cy = y; 
    mDuplicate = num_st.mDuplicate;
    
    makeStraightPath( 0, 0, 2*TopoDroidSetting.mStationSize*mName.length(), 0, cx, cy );
  }

  float distance( float x, float y )
  { 
    double dx = x - cx;
    double dy = y - cy;
    return (float)( Math.sqrt( dx*dx + dy*dy ) );
  }

  @Override
  public void draw( Canvas canvas )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PATH, "DrawingStationName::draw LABEL " + mName );
    canvas.drawTextOnPath( mName, mPath, 0f, 0f, mPaint );
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PATH, "DrawingStationName::draw[matrix] LABEL " + mName );
    mTransformedPath = new Path( mPath );
    mTransformedPath.transform( matrix );
    canvas.drawTextOnPath( mName, mTransformedPath, 0f, 0f, mPaint );
  }
  
  String getCoordsString()
  {
    if ( mStation == null ) return null;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    // east north vertical (downward)
    pw.format(Locale.ENGLISH, "E %.2f N %.2f V %.2f", mStation.e, -mStation.s, mStation.v );
    return sw.getBuffer().toString();
  }

  @Override
  public String toTherion()
  {
    if ( mStation == null ) return ""; // empty string
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "point %.2f %.2f station -name \"%s\"", cx*toTherion, -cy*toTherion, mName );
    return sw.getBuffer().toString();
  }
}
