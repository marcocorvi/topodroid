/* @file DrawingSpecialPath.java
 *
 * @author marco corvi
 * @date apr 2018
 *
 * @brief TopoDroid drawing: special points
 *        type DRAWING_PATH_NORTH
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */

package com.topodroid.DistoX;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

// import android.util.Log;

/**
 */
class DrawingSpecialPath extends DrawingPath
{
  // private static float toTherion = TDConst.TO_THERION;
  
  static final int SPECIAL_ANY = 0; // generic
  static final int SPECIAL_DOT = 1; // leg x-section dot reference

  int mType; // type of special path

  DrawingSpecialPath( int t, float x, float y )
  {
    super( DrawingPath.DRAWING_PATH_NORTH, null );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    mType = t;
    setCenter( x, y );
    resetPath();
  }

  static DrawingSpecialPath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
    try {
      int t = dis.readInt();
      float ccx = x + dis.readFloat();
      float ccy = y + dis.readFloat();
      DrawingSpecialPath ret = new DrawingSpecialPath( t, ccx, ccy );
      return ret;
    } catch ( IOException e ) {
      TDLog.Error( "SPECIAL in error " + e.getMessage() );
    }
    return null;
  }

  void setCenter( float x, float y )
  {
    cx = x;
    cy = y;
    left   = x; 
    right  = x+1;
    top    = y;
    bottom = y+1;
  }

  @Override
  void shiftBy( float dx, float dy )
  {
    cx += dx;
    cy += dy;
    mPath.offset( dx, dy );
    left   += dx;
    right  += dx;
    top    += dy;
    bottom += dy;
  }

  @Override
  void scaleBy( float z, Matrix m )
  {
    cx *= z;
    cy *= z;
    mPath.transform( m );
    left   *= z;
    right  *= z;
    top    *= z;
    bottom *= z;
  }

  // @Override
  // public void shiftPathBy( float dx, float dy ) { }

  // @Override
  // public void scalePathBy( float z, Matrix m ) { }

  // N.B. canvas is guaranteed ! null
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      mTransformedPath = new Path( mPath );
      if ( mLandscape ) {
	Matrix rot = new Matrix();
	rot.postRotate( 90, cx, cy );
	mTransformedPath.transform( rot );
      }
      mTransformedPath.transform( matrix );
      drawPath( mTransformedPath, canvas );
    }
  }

  public void shiftTo( float x, float y ) // x,y scene coords
  {
    mPath.offset( x-cx, y-cy );
    setCenter( x, y );
  }

  private void resetPath()
  {
    setPaint( BrushManager.labelPaint );
    Path p = new Path();
    p.addCircle( 0, 0, TDSetting.mLineThickness, Path.Direction.CCW );
    makePath( p, new Matrix(), cx, cy );
  }

  // no export to cSurevy
  // @Override
  public void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind, DrawingUtil mDrawingUtil ) { }

  // no export to Therion
  @Override
  public String toTherion( ) { return ""; }

  @Override
  void toDataStream( DataOutputStream dos )
  {
    try {
      dos.write( 'J' );
      dos.writeInt( mType );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      // TDLog.Log( TDLog.LOG_PLOT, "P " + name + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.Error( "POINT out error " + e.toString() );
    }
  }

}

