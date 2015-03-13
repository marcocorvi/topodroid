/* @file DrawingPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: paths (points, lines, and areas)
 * 
 * FixedPath path is a straight line between the two endpoints
 * GridPath paths are also straight lines
 * previewPath path is a line with "many" points
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130204 type DRAWING_PATH_NAME
 * 20140328 endpoints scene coords re-enabled
 * 20140328 check if this path intersects a segment
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

// import android.util.FloatMath;
import android.util.Log;

/**
 */
public class DrawingPath implements ICanvasCommand
{
  public static final int DRAWING_PATH_FIXED   = 0; // leg
  public static final int DRAWING_PATH_SPLAY   = 1; // splay
  public static final int DRAWING_PATH_GRID    = 2; // grid
  public static final int DRAWING_PATH_STATION = 3; // station point
  public static final int DRAWING_PATH_POINT   = 4; // drawing point
  public static final int DRAWING_PATH_LINE    = 5;
  public static final int DRAWING_PATH_AREA    = 6;
  public static final int DRAWING_PATH_NAME    = 7; // station name
  public static final int DRAWING_PATH_NORTH   = 8; // north line (5m long)

  Path mPath;
  Path mTransformedPath;
  Paint mPaint;
  int mType;
  float x1, y1, x2, y2; // endpoint scene coords  (not private just to write the scrap scale using mNorthLine )
  // private int dir; // 0 x1 < x2, 1 y1 < y2, 2 x2 < x1, 3 y2 < y1

  float cx, cy; // midpoint scene coords
                 
  DistoXDBlock mBlock;

  // get the path color (or white)
  int color() { return ( mPaint != null )? mPaint.getColor() : 0xffffffff; }

  // void log()
  // {
  //   Log.v("DistoX", "Path " + x1 + " " + y1 + "   " + x2 + " " + y2 );
  // }

  /** make the path copying from another path
   * @param path   the path to copy
   * @param m      transform matrix
   * @param off_x  offset X
   * @param off_y  offset Y
   */
  void makePath( Path path, Matrix m, float off_x, float off_y )
  {
    mPath = new Path( path );
    mPath.transform( m );
    mPath.offset( off_x, off_y );
  }

  void makeStraightPath( float x1, float y1, float x2, float y2, float off_x, float off_y )
  {
    mPath = new Path();
    mPath.moveTo( x1, y1 );
    mPath.lineTo( x2, y2 );
    mPath.offset( off_x, off_y );
  }

  DrawingPath( int type )
  {
    mType = type;
    mBlock = null;
    // dir = 4;
    // x1 = y1 = 0.0f;
    // x2 = y2 = 1.0f;
    // dx = dy = 1.0f;
  }

  DrawingPath( int type, DistoXDBlock blk )
  {
    mType = type;
    mBlock = blk; 
    // dir = 4;
    // x1 = y1 = 0.0f;
    // x2 = y2 = 1.0f;
    // dx = dy = 1.0f;
  }

  public void setPaint( Paint paint ) { mPaint = paint; }

  // x10, y10 first endpoint scene coords
  // x20, y20 second endpoint scene coords
  public void setEndPoints( float x10, float y10, float x20, float y20 )
  {
    x1 = x10;
    y1 = y10;
    x2 = x20;
    y2 = y20;
    // dir = ( Math.abs( x2-x1 ) >= Math.abs( y2-y1 ) )?
    //          ( (x2 > x1)? 0 : 2 ) : ( (y2>y1)? 1 : 3 );
    // d = FloatMath.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
    cx = (x20+x10)/2;
    cy = (y20+y10)/2;
  }

  // intersection of 
  //    x = x1 + t*(x2-x1)
  //    y = y1 + t*(y2-y1)
  // and
  //    x = x10 + s*(x20-x10)
  //    y = y10 + s*(y20-y10)
  //
  // t * (x2-x1) - s*(x20-x10) = x10 - x1;
  // t * (y2-y1) - s*(y20-y10) = y10 - y1;
  // inverse
  // t   | -(y20-y10)  +(x20-x10) | | x10 - x1 |
  // s   | -(y2-y1)    +(x2-x1)   | | y10 - y1 |
  //
  boolean intersect( float x10, float y10, float x20, float y20, Float result )
  {
    float det = -(x2-x1)*(y20-y10) + (x20-x10)*(y2-y1);
    float t = ( -(y20-y10)*(x10 - x1) + (x20-x10)*(y10 - y1) )/det;
    float s = ( -(y2-y1)*(x10 - x1) + (x2-x1)*(y10 - y1) )/det;
    if ( result != null ) result = t;
    return ( t > 0.0f && t < 1.0f && s > 0.0f && s < 1.0f );
  }


  // DrawingPath by deafult does not shift
  void shiftBy( float dx, float dy ) { }

  void shiftPathBy( float dx, float dy ) 
  {
    x1 += dx;
    y1 += dy;
    x2 += dx;
    y2 += dy;
    cx += dx;
    cy += dy;
    mPath.offset( dx, dy );
  }

  float distance( float x, float y )
  {
    if ( mBlock == null ) return 1000.0f; // a large number
    double dx = x - cx;
    double dy = y - cy;
    // Log.v("DistoX", "distance from block " + dx + " " + dy );
    return (float)( Math.sqrt( dx*dx + dy*dy ) );
  }

  // public int type() { return mType; }

  public void draw( Canvas canvas )
  {
    if ( mType == DRAWING_PATH_AREA ) {
      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "DrawingPath::draw area" );
      mPath.close();
      canvas.save();
      canvas.clipPath( mPath );
      canvas.drawPaint( mPaint );
      canvas.restore();
    } else {
      if ( mType == DRAWING_PATH_SPLAY && mBlock != null && mBlock.isRecent( TopoDroidApp.mSecondLastShotId ) ) {
        canvas.drawPath( mPath, DrawingBrushPaths.fixedBluePaint );
      } else {
        canvas.drawPath( mPath, mPaint );
      }
    }
  }

  public void draw( Canvas canvas, Matrix matrix )
  {
    // if ( mType == DRAWING_PATH_AREA ) {
    //   // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "DrawingPath::draw[matrix] area" );
    //   mPath.close();
    // }
    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "DrawingPath::draw[matrix] " + mPaint );
    mTransformedPath = new Path( mPath );
    mTransformedPath.transform( matrix );
    if ( mType == DRAWING_PATH_AREA ) {
      canvas.save();
      canvas.clipPath( mTransformedPath );
      canvas.drawPaint( mPaint );
      canvas.restore();
    } else {
      if ( mType == DRAWING_PATH_SPLAY && mBlock != null && mBlock.isRecent( TopoDroidApp.mSecondLastShotId ) ) {
        canvas.drawPath( mTransformedPath, DrawingBrushPaths.fixedBluePaint );
      } else {
        canvas.drawPath( mTransformedPath, mPaint );
      }
    }
  }

  public void setOrientation( double angle ) { }

  public String toTherion() { return new String("FIXME"); }

  public void toCsurvey( PrintWriter pw ) { }

  public void undo()
  {
    // TODO this would be changed later
  }

  // public void transform( Matrix matrix )
  // {
  //   mPath.transform( matrix );
  // }
}
