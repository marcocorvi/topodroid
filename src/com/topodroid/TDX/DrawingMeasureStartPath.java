/* @file DrawingPointPath.java
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
import com.topodroid.prefs.TDSetting;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Matrix;

public class DrawingMeasureStartPath extends DrawingPath
{
  // float mXpos;        // scene coords
  // float mYpos;
  float  mRod;    // circle radius

  /** cstr
   * @param x       X coord of the center
   * @param y       Y coord of the center
   * @param rod     circle radius
   */
  public DrawingMeasureStartPath( float x, float y, float rod )
  {
    super( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    cx = x;
    cy = y;
    mRod = rod;
    // mScale   = PointScale.SCALE_NONE;
    setPathPaint( BrushManager.highlightPaint );
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
      float rod = mRod / zoom;
      Path path = new Path();
      path.addCircle( 0, 0, rod, Path.Direction.CCW );
      path.moveTo( -rod,   0);
      path.lineTo(  rod,   0);
      path.moveTo(   0, -rod);
      path.lineTo(   0,  rod);
      path.offset( cx, cy );
      path.transform( matrix );
      drawPath( path, canvas );
    }
  }

}

