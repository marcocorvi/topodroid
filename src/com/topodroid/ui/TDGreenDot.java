/* @file TDGreenDot.java
 *
 * @author marco corvi
 * @date feb 2020
 *
 * @grief graphical utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.TDX.SelectionPoint;
import com.topodroid.TDX.BrushManager;
import com.topodroid.TDX.DrawingSplayPath;

import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class TDGreenDot
{
  /** draw a selection point, as a green dot
   * @param canvas     canvas
   * @param matrix     transform matrix
   * @param pt         selection point
   * @param dot_radius circle radius
   *
   * @note this function could be a forward to
   *       draw( canvas, matrix, pt.mPoint.x, pt.mPoint.y, dot_radius, BrushManager.highlightPaint2 );
   *       or pt.mItem.x etc.
   */
  public static void draw( Canvas canvas, Matrix matrix, SelectionPoint pt, float dot_radius )
  {
    Path path = new Path();
    if ( pt.mPoint != null ) { // line-point
      path.addCircle( pt.mPoint.x, pt.mPoint.y, dot_radius, Path.Direction.CCW );
    } else {  
      path.addCircle( pt.mItem.cx, pt.mItem.cy, dot_radius, Path.Direction.CCW );
    }
    path.transform( matrix );
    canvas.drawPath( path, BrushManager.highlightPaint2 );
  }

  /** draw a point, as a dot with the given paint
   * @param canvas     canvas
   * @param matrix     transform matrix
   * @param x          X coordinate
   * @param y          Y coordinate
   * @param dot_radius circle radius
   * @param paint      dot paint
   */
  public static void draw( Canvas canvas, Matrix matrix, float x, float y, float dot_radius, Paint paint )
  {
    Path path = new Path();
    path.addCircle( x, y, dot_radius, Path.Direction.CCW );
    path.transform( matrix );
    canvas.drawPath( path, paint );
  }

  // /** draw a point, as a dot with the given paint
  //  * @param canvas     canvas
  //  * @param x          X coordinate
  //  * @param y          Y coordinate
  //  * @param dot_radius circle radius
  //  * @param paint      dot paint
  //  */
  // public static void draw( Canvas canvas, float x, float y, float dot_radius, Paint paint )
  // {
  //   Path path = new Path();
  //   path.addCircle( x, y, dot_radius, Path.Direction.CCW );
  //   canvas.drawPath( path, paint );
  // }

}

