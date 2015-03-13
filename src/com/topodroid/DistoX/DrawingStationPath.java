/* @file DrawingStationPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid drawing: station point 
 *        type DRAWING_PATH_STATION
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created
 * 20140526 point scale bug fix
 */

package com.topodroid.DistoX;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

// import android.util.Log;

/**
 * station points do not shift (!)
 */
public class DrawingStationPath extends DrawingPath
{
  private static float toTherion = TopoDroidConst.TO_THERION;

  float mXpos;
  float mYpos;
  protected int mScale;       //! symbol scale
  String mName;               // station name


  public DrawingStationPath( String name, float x, float y, int scale )
  {
    super( DrawingPath.DRAWING_PATH_STATION );
    // TopoDroidLog.Log( TopoDroidLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    // mType = DRAWING_PATH_STATION;
    mXpos = x;
    mYpos = y;
    mName = name;

    mScale = DrawingPointPath.SCALE_NONE; // scale
    mPath = null;
    setScale( scale );
    mPaint = DrawingBrushPaths.mStationSymbol.mPaint;
    // Log.v( TopoDroidApp.TAG, "Point cstr " + type + " orientation " + mOrientation + " flip " + mFlip );
  }

  public DrawingStationPath( DrawingStationName st, int scale )
  {
    super( DrawingPath.DRAWING_PATH_STATION );
    // TopoDroidLog.Log( TopoDroidLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    // mType = DRAWING_PATH_STATION;
    mXpos = st.cx;
    mYpos = st.cy;
    mName = st.mName;

    mScale = DrawingPointPath.SCALE_NONE; // scale
    mPath = null;
    setScale( scale );
    mPaint = DrawingBrushPaths.mStationSymbol.mPaint;
    // Log.v( TopoDroidApp.TAG, "Point cstr " + type + " orientation " + mOrientation + " flip " + mFlip );
  }

  void setScale( int scale )
  {
    if ( scale != mScale ) {
      mScale = scale;
      // station point does not have text
      float f = 1.0f;
      switch ( mScale ) {
        case DrawingPointPath.SCALE_XS: f = 0.50f; break;
        case DrawingPointPath.SCALE_S:  f = 0.72f; break;
        case DrawingPointPath.SCALE_L:  f = 1.41f; break;
        case DrawingPointPath.SCALE_XL: f = 2.00f; break;
      }
      Matrix m = new Matrix();
      m.postScale(f,f);
      makePath( DrawingBrushPaths.mStationSymbol.mPath, m, mXpos, mYpos );
    }  
  }
      
  // int getScale() { return mScale; }

  // public void setPos( float x, float y ) 
  // {
  //   mXpos = x;
  //   mYpos = y;
  // }

  // public void setPointType( int t ) { mPointType = t; }
  // public int pointType() { return mPointType; }

  // public double xpos() { return mXpos; }
  // public double ypos() { return mYpos; }

  // public double orientation() { return mOrientation; }

  float distance( float x, float y )
  {
    double dx = x - mXpos;
    double dy = y - mYpos;
    return (float)( Math.sqrt( dx*dx + dy*dy ) );
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);

    // Log.v( TopoDroidApp.TAG, "toTherion() Point " + mPointType + " orientation " + mOrientation + " flip " +
    //                  mFlip + " flippable " +
    //                  DrawingBrushPaths.canFlip( mPointType ) );

    pw.format(Locale.ENGLISH, "point %.2f %.2f station -name %s\n", mXpos*toTherion, -mYpos*toTherion, mName );
    return sw.getBuffer().toString();
  }


}

