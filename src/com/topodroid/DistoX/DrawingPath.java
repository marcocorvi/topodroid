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
 */
package com.topodroid.DistoX;

import java.io.PrintWriter;
import java.io.DataOutputStream;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.RectF;

import android.util.Log;

/**
 * direct/indirect subclasses:
 *   - DrawingPointLinePath
 *      - DrawingLinePath
 *      - DrawingAreaPath
 *   - DrawingPointPath
 *   - DrawingStationPath
 */
public class DrawingPath extends RectF
                         implements ICanvasCommand
{
  public static final int DRAWING_PATH_FIXED   = 0; // leg
  public static final int DRAWING_PATH_SPLAY   = 1; // splay
  public static final int DRAWING_PATH_GRID    = 2; // grid
  public static final int DRAWING_PATH_STATION = 3; // station point (user inserted)
  public static final int DRAWING_PATH_POINT   = 4; // drawing point
  public static final int DRAWING_PATH_LINE    = 5;
  public static final int DRAWING_PATH_AREA    = 6;
  public static final int DRAWING_PATH_NAME    = 7; // station name (from survey data)
  public static final int DRAWING_PATH_NORTH   = 8; // north line (5m long)

  Path mPath;
  Path mTransformedPath;
  Paint mPaint;
  int mType;
  String mOptions;
  float x1, y1, x2, y2; // endpoint scene coords  (not private just to write the scrap scale using mNorthLine )
  // private int dir; // 0 x1 < x2, 1 y1 < y2, 2 x2 < x1, 3 y2 < y1
  DistoXDBlock mBlock;

  float cx, cy; // midpoint scene coords
  // RectF mBBox;   // path boundig box (scene coords)

  DrawingPath( int type, DistoXDBlock blk )
  {
    mType  = type;
    mOptions  = null;
    mBlock = blk; 
    // mBBox  = new RectF();
    mPaint = DrawingBrushPaths.errorPaint;
    // dir = 4;
    // x1 = y1 = 0.0f;
    // x2 = y2 = 1.0f;
    // dx = dy = 1.0f;
  }

  static boolean isReferenceType( int type ) 
  {
    return type < DrawingPath.DRAWING_PATH_STATION || type >= DrawingPath.DRAWING_PATH_NAME;
  }

  static boolean isDrawingType( int type ) 
  {
    return type >= DrawingPath.DRAWING_PATH_STATION && type < DrawingPath.DRAWING_PATH_NAME;
  }

  void setBBox( float x1, float x2, float y1, float y2 )
  {
    left   = x1;
    right  = x2;
    top    = y1;
    bottom = y2;
  }

  protected boolean intersects( RectF bbox )
  { 
    // return true;
    if ( bbox == null ) return true;
    if ( ( bbox.right  < left   ) 
      || ( bbox.left   > right  ) 
      || ( bbox.top    > bottom ) 
      || ( bbox.bottom < top    ) ) return false;
    return true;
  }
  
  public void flipXAxis()
  {
    float x0 = TopoDroidApp.mDisplayWidth / 2;
    float offx = 2 * ( x0 + cx );
    float offc = 2 * ( x0 - cx );
    float dx = 2 * x0;
    cx = dx - cx;
    x1 = dx - x1;
    x2 = dx - x2;
    float r1 = dx - left;
    left = dx - right;
    right = r1;
    boolean flip_path = false;
    if ( mType != DRAWING_PATH_POINT ) return;
    DrawingPointPath dpp = (DrawingPointPath)this;
    if ( dpp.mOrientation != 0 ) {
      dpp.mOrientation = 360 - dpp.mOrientation;
      flip_path = true;
      offx = 2 * x0;
    } else {
      offx = offc;
    }
    if ( flip_path && mPath != null ) {
      float m[] = new float[9]; // { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
      android.graphics.Matrix mat = new android.graphics.Matrix();
      mat.getValues( m );
      m[0] = -m[0];
      mat.setValues( m );
      mPath.transform( mat );
    }
    mPath.offset( offx, 0 );
  }

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

  void makeTrianglePath( float x, float y, float r, float off_x, float off_y )
  {
    float r2 = r * 1.732f;
    mPath = new Path();
    mPath.moveTo( x1-r, y1 );
    mPath.lineTo( x1+r, y1 );
    mPath.lineTo( x1, y1-r2 );
    mPath.lineTo( x1-r, y1 );
    mPath.offset( off_x, off_y );
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
    // d = Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
    cx = (x20+x10)/2;
    cy = (y20+y10)/2;
    if ( x1 < x2 ) {
      left  = x1;
      right = x2;
    } else {
      left  = x2;
      right = x1;
    }
    if ( y1 < y2 ) {
      top    = y1;
      bottom = y2;
    } else {
      top    = y2;
      bottom = y1;
    }
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

  public void shiftPathBy( float dx, float dy ) 
  {
    x1 += dx;
    y1 += dy;
    x2 += dx;
    y2 += dy;
    cx += dx;
    cy += dy;
    mPath.offset( dx, dy );
    left   += dx;
    right  += dx;
    top    += dy;
    bottom += dy;
  }

  // this is used only by the Selection 
  float distanceToPoint( float x, float y )
  {
    // if ( mBlock == null ) return 1000.0f; // a large number
    double dx = x - cx;
    double dy = y - cy;
    return (float)( Math.sqrt( dx*dx + dy*dy ) );
  }

  // public int type() { return mType; }

  public void draw( Canvas canvas, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      if ( mType == DRAWING_PATH_AREA ) {
        // TDLog.Log( TDLog.LOG_PLOT, "DrawingPath::draw area" );
        mPath.close();
      }
      drawPath( mPath, canvas );
    }
  }

  // N.B. canvas is guaranteed ! null
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( intersects( bbox ) ) 
    {
      mTransformedPath = new Path( mPath );
      mTransformedPath.transform( matrix );
      drawPath( mTransformedPath, canvas );
    }
  }

  protected void drawPath( Path path, Canvas canvas )
  {
    if ( mType == DRAWING_PATH_AREA ) {
      canvas.save();
      canvas.clipPath( path );
      canvas.drawPaint( mPaint );
      canvas.restore();
    } else {
      if ( mType == DRAWING_PATH_SPLAY && mBlock != null && mBlock.isRecent( TopoDroidApp.mSecondLastShotId ) ) {
        canvas.drawPath( path, DrawingBrushPaths.fixedBluePaint );
      } else {
        canvas.drawPath( path, mPaint );
      }
    }
  }


  public void setOrientation( double angle ) { }

  public String toTherion() { return new String("FIXME"); }

  void toDataStream( DataOutputStream dos ) { TDLog.Error( "ERROR DrawingPath toDataStream executed"); }

  public void toCsurvey( PrintWriter pw, String cave, String branch ) { }

  // ICanvasCommand interface
  //
  public int  commandType() { return 0; }

  // public void undoCommand() { // TODO this would be changed later }

  public void computeBounds( RectF bound, boolean b ) { mPath.computeBounds( bound, b ); }

  // public void transform( Matrix matrix ) { mPath.transform( matrix ); }

  // ------------------------------------------------------------------
  // Therion options

  void addOption( String option ) 
  {
    // Log.v("DistoX", "add option <" + option +">" );
    if ( mOptions == null ) {
      mOptions = option;
    } else {
      mOptions = mOptions + " " + option;
    }
  }

  // String[] getOptions() 
  // {
  //   if ( mOptions == null ) return new String[0];
  //   return mOptions.split(" ");
  // }

  // key must be not null and start with '-'
  String getOption( String key )
  {
    if ( mOptions == null ) return null;
    String vals[] = mOptions.split(" ");
    int len = vals.length;
    for ( int k = 0; k < len; ++k ) {
      if ( key.equals( vals[k] ) ) {
        while ( ++k < len ) if ( vals[k].length() > 0 ) return vals[k];
        break;
      }
    }
    return null;
  }

  String getOptionString() { return ( mOptions == null )? "" : mOptions; }

  void setOptions( String options ) { mOptions = options; }
}
