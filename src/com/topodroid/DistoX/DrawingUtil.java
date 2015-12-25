/* @file DrawingUtil.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing utilities
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Paint;
// import android.graphics.Paint.FontMetrics;
// import android.graphics.PointF;
import android.graphics.Path;
// import android.graphics.Path.Direction;


class DrawingUtil
{
  static final float SCALE_FIX = 20.0f; 
  public static final float CENTER_X = 100f;
  public static final float CENTER_Y = 120f;

  // private static final PointF mCenter = new PointF( CENTER_X, CENTER_Y );

  static float toSceneX( float x ) { return CENTER_X + x * SCALE_FIX; }
  static float toSceneY( float y ) { return CENTER_Y + y * SCALE_FIX; }

  static float sceneToWorldX( float x ) { return (x - CENTER_X)/SCALE_FIX; }
  static float sceneToWorldY( float y ) { return (y - CENTER_Y)/SCALE_FIX; }

    
  static void makePath( DrawingPath dpath, float x1, float y1, float x2, float y2, float xoff, float yoff )
  {
    dpath.mPath = new Path();
    x1 = toSceneX( x1 );
    y1 = toSceneY( y1 );
    x2 = toSceneX( x2 );
    y2 = toSceneY( y2 );
    dpath.setEndPoints( x1, y1, x2, y2 ); // this sets the midpoint only
    dpath.mPath.moveTo( x1 - xoff, y1 - yoff );
    dpath.mPath.lineTo( x2 - xoff, y2 - yoff );
  }

  private static void addGridLine( int z, float x1, float x2, float y1, float y2, DrawingSurface surface )
  { 
    DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
    int k = 1;
    Paint paint = DrawingBrushPaths.fixedGridPaint;
    if ( Math.abs( z % 100 ) == 0 ) {
      k = 100;
      paint = DrawingBrushPaths.fixedGrid100Paint;
    } else if ( Math.abs( z % 10 ) == 0 ) {
      k = 10;
      paint = DrawingBrushPaths.fixedGrid10Paint;
    }
    dpath.setPaint( paint );
    dpath.mPath  = new Path();
    dpath.mPath.moveTo( x1, y1 );
    dpath.mPath.lineTo( x2, y2 );
    dpath.setBBox( x1, x2, y1, y2 );
    surface.addGridPath( dpath, k );
  }

  static void addGrid( float xmin, float xmax, float ymin, float ymax, float xoff, float yoff, DrawingSurface surface )
  {
    xmin = (xmin - 10.0f) / TDSetting.mUnitGrid;
    xmax = (xmax + 10.0f) / TDSetting.mUnitGrid;
    ymin = (ymin - 10.0f) / TDSetting.mUnitGrid;
    ymax = (ymax + 10.0f) / TDSetting.mUnitGrid;
    float x1 = toSceneX( xmin ) - xoff;
    float x2 = toSceneX( xmax ) - xoff;
    float y1 = toSceneY( ymin ) - yoff;
    float y2 = toSceneY( ymax ) - yoff;
    // mDrawingSurface.setBounds( toSceneX( xmin ), toSceneX( xmax ), toSceneY( ymin ), toSceneY( ymax ) );

    DrawingPath dpath = null;
    for ( int x = Math.round(xmin); x <= xmax; x += 1 ) {
      float x0 = toSceneX( x * TDSetting.mUnitGrid ) - xoff;
      addGridLine( x, x0, x0, y1, y2, surface );
    }
    for ( int y = Math.round(ymin); y <= ymax; y += 1 ) {
      float y0 = toSceneY( y * TDSetting.mUnitGrid ) - yoff;
      addGridLine( y, x1, x2, y0, y0, surface );
    }
  }

}
