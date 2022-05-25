/* @file DrawingMeasureEndPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: points
 *        type DRAWING_PATH_POINT
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */

package com.topodroid.TDX;

// import com.topodroid.math.TDVector;
// import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
// import android.graphics.Matrix;

public class DrawingMeasureEndPath extends DrawingPath
{
  // float mXpos;        // scene coords
  // float mYpos;
  private float  mRod;    // cross radius
  private Path   mPath;
  private boolean hasCross;

  /** cstr
   * @param x1      X coord of the first point
   * @param y1      Y coord of the first point
   * @param x2      X coord of the second point
   * @param y2      Y coord of the second point
   * @param rod     cross radius
   */
  public DrawingMeasureEndPath( float x1, float y1, float x2, float y2, float rod )
  {
    super( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    this.x1 = x1;
    this.y1 = y1;
    this.cx = x2;
    this.cy = y2;
    mRod = rod;
    setPathPaint( BrushManager.fixedBluePaint );
    mPath = new Path();
    mPath.moveTo( x1, y1 );
    mPath.lineTo( cx, cy );
    hasCross = true;
  }

  /** cstr
   * @param x1      X coord of the first point
   * @param y1      Y coord of the first point
   * @param rod     cross radius
   */
  public DrawingMeasureEndPath( float x1, float y1, float rod )
  {
    super( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    this.x1 = x1;
    this.y1 = y1;
    mRod = rod;
    setPathPaint( BrushManager.fixedBluePaint );
    mPath = new Path();
    mPath.moveTo( x1, y1 );
    hasCross = false;
  }

  /** set the endpoint of the measure path
   * @param x      X coord of the first point
   * @param y      Y coord of the first point
   */
  void setEndPath( float x, float y )
  {
    cx = x;
    cy = y;
    hasCross = true;
    mPath.lineTo( x, y );
  }

  /** draw the point on the canvas
   * @param canvas    canvas
   * @param matrix    transform matrix
   * @param zoom      display zoom - must be applied before the (center) offset 
   * @param bbox      clipping rectangle
   * @note canvas is guaranteed not null
   */
  // @Override
  public void draw( Canvas canvas, Matrix matrix, float zoom, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      if ( hasCross ) {
        float rod = mRod / zoom;
        Path cross = new Path();
        cross.moveTo(  rod,  rod);
        cross.lineTo( -rod, -rod);
        cross.moveTo(  rod, -rod);
        cross.lineTo( -rod,  rod);
        cross.offset( cx, cy );
        cross.transform( matrix );
        drawPath( cross, canvas );
        Path path = new Path( mPath );
        path.transform( matrix );
        drawPath( path, canvas );
      }
    }
  }

}

