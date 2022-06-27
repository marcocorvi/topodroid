/* @file GraphPaperScaleCommandManager.java
 *
 * @author marco corvi
 * @date mar 2016
 *
 * @brief TopoDroid graph-paper scale adjustment: commands manager
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.math.Point2D;
import com.topodroid.prefs.TDSetting;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Paint;

class GraphPaperScaleCommandManager
{
  private static final int BORDER = 20;

  // private RectF mBBox = null;

  private DrawingPath mPath;
  private GraphPaperScaleActivity mParent = null;

  private Matrix mMatrix;
  private float  mZoom; // current zoom: value of 1 pl in scene space
  private int  mDensity;
  private int  mY;

  void changeDensity( int change )
  {
    int density = mDensity + change;
    if ( TopoDroidApp.getDensity() - density >= 120 ) {
      mDensity = density;
      mZoom = 1600 * 2.54f / ( TopoDroidApp.getDensity() - density );
      TDLog.v("GRAPH_PAPER zoom " + mZoom + " density " + density + " dp1cm " + ( TopoDroidApp.getDensity() - density )/2.54f );
      setTransform( 0, 0, mZoom );
      if ( mParent != null ) mParent.setDensityTextView( density );
    }
  }

  void setGraphPaperScaleActivity( GraphPaperScaleActivity parent ) { mParent = parent; }

  void setY( int y )
  {
    // TDLog.v("GRAPH_PAPER set Y " + y );
    mY = y;
  }

  int getGraphPaperDensity() { return mDensity; }

  /** cstr
   */
  GraphPaperScaleCommandManager()
  {
    // TDLog.v("GRAPH_PAPER command manager cstr ");
    mPath   = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
    mPath.makePath( 0, 2*mY, 5*20, 2*mY ); // 20 = FIX_SCALE, 5 m
    Paint paint = new Paint();
    paint.setDither(true);
    paint.setColor( 0xffffffff );
    paint.setStyle( Paint.Style.FILL_AND_STROKE );
    paint.setStrokeJoin(Paint.Join.MITER );
    paint.setStrokeCap(Paint.Cap.BUTT );
    paint.setStrokeWidth( 10 * BrushManager.WIDTH_FIXED );
    mPath.setPathPaint( paint );
    mMatrix = new Matrix(); // identity
    mDensity = TDSetting.mGraphPaperScale;
    changeDensity( 0 );
  }

  void clearDrawing()
  {
  }

  /** set the transformation matrix
   * @param dx   X translation
   * @param dy   Y translation
   * @param s    zoom
   * 
   * x' = (x + dx) * s
   * y' = (y + dy) * s
   */
  void setTransform( float dx, float dy, float s )
  {
    // TDLog.v("GRAPH_PAPER set transform " + dx + " " + dy + " " + s );
    mMatrix = new Matrix();
    mMatrix.postTranslate( dx, dy );
    mMatrix.postScale( s, s );
    // mScale  = 1 / s;
  }

  // oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
  static int cnt = 0;

  void executeAll( Canvas canvas )
  {
    if ( canvas == null ) {
      TDLog.Error( "drawing executeAll null canvas");
      return;
    }

    synchronized( mPath ) {
      if ( cnt < 4 ) { TDLog.v("GRAPH_PAPER draw "); ++cnt; }
      mPath.draw( canvas, mMatrix, null ); // null mBBox
    }
  }

}
