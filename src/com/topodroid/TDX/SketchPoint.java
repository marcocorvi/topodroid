/* @file SketchPoint.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketching: point of a line
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDMath;
// import com.topodroid.utils.TDLog;
import com.topodroid.math.TDVector;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;


public class SketchPoint extends TDVector // world coords
{
  SketchLinePath mLine;

  /** cstr
   * @param v     3D point
   * @param line  sketch line to which this point belongs
   */
  public SketchPoint( TDVector v, SketchLinePath line )
  {
    super( v );
    mLine = line;
  }

  // @param r  dot radius
  void drawPoint( Canvas canvas, Matrix mm, TDVector C, TDVector X, TDVector Y, float zoom, float off_x, float off_y, float r )
  {
    Path path = new Path();
    TDVector v = this.minus( C );
    float x = zoom * X.dot( v );
    float y = zoom * Y.dot( v ); // downaward
    path.addCircle( x, y, r, Path.Direction.CCW );
    path.transform( mm );
    path.offset( off_x, off_y );
    canvas.drawPath( path, BrushManager.fixedOrangePaint );
  }

}

