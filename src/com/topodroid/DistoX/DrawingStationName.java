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

import java.util.Locale;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
    mName = name;

    // TDLog.Log( TDLog.LOG_PLOT, "DrawingStationName cstr " + mName + " " + x + " " + y );

    setCenter( x, y ); // scene coords
    mDuplicate = false;
    makeStraightPath( 0, 0, 2*TDSetting.mStationSize*mName.length(), 0, cx, cy );
  }

  public DrawingStationName( NumStation num_st, float x, float y )
  {
    super( DrawingBrushPaths.mPointLib.mPointLabelIndex,
           x, // scene coordinate
           y, 
           DrawingPointPath.SCALE_M, null );
    mType = DRAWING_PATH_NAME; // override DrawingPath.mType
    mStation = num_st;
    mName = num_st.name;

    // TDLog.Log( TDLog.LOG_PLOT, "DrawingStationName cstr " + mName + " " + x + " " + y );
    if ( num_st.mDuplicate ) mPaint = DrawingBrushPaths.duplicateStationPaint;
    setCenter( x, y ); // scene coords
    mDuplicate = num_st.mDuplicate;
    
    makeStraightPath( 0, 0, 2*TDSetting.mStationSize*mName.length(), 0, cx, cy );
  }

  // defined in DrawingPointPath
  // float distance( float x, float y )
  // { 
  //   double dx = x - cx;
  //   double dy = y - cy;
  //   return FloatMath.sqrt( dx*dx + dy*dy );
  // }

  @Override
  public void draw( Canvas canvas, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "DrawingStationName::draw LABEL " + mName );
      canvas.drawTextOnPath( mName, mPath, 0f, 0f, mPaint );
    }
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "DrawingStationName::draw[matrix] LABEL " + mName );
      mTransformedPath = new Path( mPath );
      mTransformedPath.transform( matrix );
      canvas.drawTextOnPath( mName, mTransformedPath, 0f, 0f, mPaint );
    }
  }
  
  String getCoordsString()
  {
    if ( mStation == null ) return null;
    // east north vertical (downward)
    return String.format(Locale.ENGLISH, "E %.2f N %.2f V %.2f", mStation.e, -mStation.s, mStation.v );
  }

  @Override
  public String toTherion()
  {
    if ( mStation == null ) return ""; // empty string
    return String.format(Locale.ENGLISH, "point %.2f %.2f station -name \"%s\"", cx*toTherion, -cy*toTherion, mName );
  }

  @Override
  public void toDataStream( DataOutputStream dos )
  {
    try {
      dos.write('X');
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      dos.writeUTF( mName );
    } catch ( IOException e ) { }
  }

  // used to make therion file from binary file
  //
  static DrawingStationName loadDataStream( int version, DataInputStream dis )
  {
    float ccx, ccy;
    String name;
    try {
      ccx = dis.readFloat();
      ccy = dis.readFloat();
      name = dis.readUTF();
      return new DrawingStationName( name, ccx, ccy );
    } catch ( IOException e ) { }
    return null;
  }
}
