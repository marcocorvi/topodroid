/* @file DrawingUtil.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *  @note this is actually DrawingUtilPortrait.java as the Landscape is never used
 *        and it is made all static (state-less)
 */
package com.topodroid.DistoX;

import com.topodroid.prefs.TDSetting;
import com.topodroid.math.Point2D;

import android.graphics.Paint;
import android.graphics.Path;

import android.util.Log;

public class DrawingUtil
{
  public static final float SCALE_FIX = 20.0f; 
  public static final float CENTER_X = 100f;
  public static final float CENTER_Y = 120f;

  // private static final PointF mCenter = new PointF( CENTER_X, CENTER_Y );

  public static float toSceneX( Point2D p ) { return CENTER_X + p.x * SCALE_FIX; }
  public static float toSceneY( Point2D p ) { return CENTER_Y + p.y * SCALE_FIX; }

  public static float sceneToWorldX( Point2D p ) { return (p.x - CENTER_X) / SCALE_FIX; }
  public static float sceneToWorldY( Point2D p ) { return (p.y - CENTER_Y) / SCALE_FIX; }

  // float toSceneX( float x, float y ) { return x; } 
  // float toSceneY( float x, float y ) { return y; } 
  public static float toSceneX( float x, float y ) { return CENTER_X + x * SCALE_FIX; }
  public static float toSceneY( float x, float y ) { return CENTER_Y + y * SCALE_FIX; }

  // float sceneToWorldX( float x, float y ) { return x; } 
  // float sceneToWorldY( float x, float y ) { return y; }
  public static float sceneToWorldX( float x, float y ) { return (x - CENTER_X)/SCALE_FIX; }
  public static float sceneToWorldY( float x, float y ) { return (y - CENTER_Y)/SCALE_FIX; }
    
  // int toBoundX( float x, float y ) { return Math.round(x); } 
  // int toBoundY( float x, float y ) { return Math.round(y); }
  private static int toBoundX( float x, float y ) { return Math.round(x); }
  private static int toBoundY( float x, float y ) { return Math.round(y); }

  static
  void makeDrawingPath( DrawingPath dpath, float xx1, float yy1, float xx2, float yy2, boolean is_splay )
  {
    dpath.mPath = new Path();
    float x1 = toSceneX( xx1, yy1 );
    float y1 = toSceneY( xx1, yy1 );
    float x2 = toSceneX( xx2, yy2 );
    float y2 = toSceneY( xx2, yy2 );
    dpath.setEndPoints( x1, y1, x2, y2 ); // this sets the midpoint only
    if ( is_splay && TDSetting.mSplayAsDot ) {
      dpath.mPath.addCircle( x2, y2, TDSetting.mLineThickness/4, Path.Direction.CCW );
    } else {
      dpath.mPath.moveTo( x1, y1 );
      dpath.mPath.lineTo( x2, y2 );
    }
  }

  static
  void makeDrawingPath( DrawingPath dpath, float xx1, float yy1, float xx2, float yy2, float xoff, float yoff )
  {
    dpath.mPath = new Path();
    float x1 = toSceneX( xx1, yy1 );
    float y1 = toSceneY( xx1, yy1 );
    float x2 = toSceneX( xx2, yy2 );
    float y2 = toSceneY( xx2, yy2 );
    dpath.setEndPoints( x1, y1, x2, y2 ); // this sets the midpoint only
    dpath.mPath.moveTo( x1 - xoff, y1 - yoff );
    dpath.mPath.lineTo( x2 - xoff, y2 - yoff );
  }

  private static void addGridLine( int z, float x1, float x2, float y1, float y2, DrawingSurface surface )
  { 
    // Log.v("DistoX-GRID", "add grid-line Z " + z + " X " + x1 + " " + x2 + " Y " + y1 + " " + y2 );
    DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID, null, -1 );
    int k = 1;
    Paint paint = BrushManager.fixedGridPaint;
    if ( Math.abs( z % 100 ) == 0 ) {
      k = 100;
      paint = BrushManager.fixedGrid100Paint;
    } else if ( Math.abs( z % 10 ) == 0 ) {
      k = 10;
      paint = BrushManager.fixedGrid10Paint;
    }
    dpath.setPathPaint( paint );
    dpath.mPath  = new Path();
    dpath.mPath.moveTo( x1, y1 );
    dpath.mPath.lineTo( x2, y2 );
    dpath.setBBox( x1, x2, y1, y2 );
    dpath.x1 = x1; // endpoints
    dpath.y1 = y1; // endpoints
    dpath.x2 = x2; // endpoints
    dpath.y2 = y2; // endpoints
    surface.addGridPath( dpath, k );
  }

  // used for plan/profile
  static
  void addGrid( float xmin, float xmax, float ymin, float ymax, DrawingSurface surface )
  {
    // Log.v("DistoX-GRID", "add grid min/max X " + xmin + " " + xmax + " Y " + ymin + " " + ymax );
    if ( xmin > xmax ) { float x = xmin; xmin = xmax; xmax = x; }
    if ( ymin > ymax ) { float y = ymin; ymin = ymax; ymax = y; }
    xmin = (xmin - 100.0f) / TDSetting.mUnitGrid;
    xmax = (xmax + 100.0f) / TDSetting.mUnitGrid;
    ymin = (ymin - 100.0f) / TDSetting.mUnitGrid;
    ymax = (ymax + 100.0f) / TDSetting.mUnitGrid;
    float x1 = toSceneX( xmin, ymin );
    float y1 = toSceneY( xmin, ymin );
    float x2 = toSceneX( xmax, ymax );
    float y2 = toSceneY( xmax, ymax );
    if ( x1 > x2 ) { float x = x1; x1 = x2; x2 = x; } // important for the bbox culling
    if ( y1 > y2 ) { float y = y1; y1 = y2; y2 = y; }
    // mDrawingSurface.setBounds( toSceneX( xmin ), toSceneX( xmax ), toSceneY( ymin ), toSceneY( ymax ) );

    int xx1 = toBoundX( xmin, ymin );
    int yy1 = toBoundY( xmin, ymin );
    int xx2 = toBoundX( xmax, ymax );
    int yy2 = toBoundY( xmax, ymax );
    if ( xx1 > xx2 ) { int x = xx1; xx1 = xx2; xx2 = x; }
    if ( yy1 > yy2 ) { int y = yy1; yy1 = yy2; yy2 = y; }

    DrawingPath dpath = null;
    for ( int x = xx1; x <= xx2; x += 1 ) {
      float x0 = x * TDSetting.mUnitGrid;
      x0 = toSceneX( x0, x0 );
      addGridLine( x, x0, x0, y1, y2, surface );
    }
    for ( int y = yy1; y <= yy2; y += 1 ) {
      float y0 = y * TDSetting.mUnitGrid;
      y0 = toSceneY( y0, y0 );
      addGridLine( y, x1, x2, y0, y0, surface );
    }
  }

  // used for x-sections
  static
  void addGrid( float xmin, float xmax, float ymin, float ymax, float xoff, float yoff, DrawingSurface surface )
  {
    // Log.v("DistoX-GRID", "add grid min/max X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " off " + xoff + " " + yoff );
    if ( xmin > xmax ) { float x = xmin; xmin = xmax; xmax = x; }
    if ( ymin > ymax ) { float y = ymin; ymin = ymax; ymax = y; }
    xmin = (xmin - 100.0f) / TDSetting.mUnitGrid;
    xmax = (xmax + 100.0f) / TDSetting.mUnitGrid;
    ymin = (ymin - 100.0f) / TDSetting.mUnitGrid;
    ymax = (ymax + 100.0f) / TDSetting.mUnitGrid;
    float x1 = toSceneX( xmin, ymin ) - xoff;
    float y1 = toSceneY( xmin, ymin ) - yoff;
    float x2 = toSceneX( xmax, ymax ) - xoff;
    float y2 = toSceneY( xmax, ymax ) - yoff;
    if ( x1 > x2 ) { float x = x1; x1 = x2; x2 = x; } // important for the bbox culling
    if ( y1 > y2 ) { float y = y1; y1 = y2; y2 = y; }
    // mDrawingSurface.setBounds( toSceneX( xmin ), toSceneX( xmax ), toSceneY( ymin ), toSceneY( ymax ) );
    
    int xx1 = toBoundX( xmin, ymin );
    int yy1 = toBoundY( xmin, ymin );
    int xx2 = toBoundX( xmax, ymax );
    int yy2 = toBoundY( xmax, ymax );
    if ( xx1 > xx2 ) { int x = xx1; xx1 = xx2; xx2 = x; }
    if ( yy1 > yy2 ) { int y = yy1; yy1 = yy2; yy2 = y; }

    // Log.v("DistoX-GRID", "add grid Y-lines X " + xx1 + " " + xx2 + " Y " + y1 + " " + y2 );
    DrawingPath dpath = null;
    for ( int x = xx1; x <= xx2; x += 1 ) {
      float x0 = x * TDSetting.mUnitGrid;
      x0 = toSceneX( x0, x0) - xoff;
      addGridLine( x, x0, x0, y1, y2, surface );
    }
    // Log.v("DistoX-GRID", "add grid X-lines X " + x1 + " " + x2 + " Y " + yy1 + " " + yy2 );
    for ( int y = yy1; y <= yy2; y += 1 ) {
      float y0 = y * TDSetting.mUnitGrid;
      y0 = toSceneY( y0, y0 ) - yoff;
      addGridLine( y, x1, x2, y0, y0, surface );
    }
    // Log.v("DistoX-GRID", "grid sizes " + surface.getGrid1Size() + " " + surface.getGrid10Size() );
  }

}
