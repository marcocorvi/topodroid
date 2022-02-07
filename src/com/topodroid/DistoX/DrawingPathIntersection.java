/* @file DrawingPathIntersection.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid a 2D point intersection on a drawing path
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * 2017-06-16
 * renamed from BezierPoint and inherited coords from android.graphics.PointF
 */
package com.topodroid.DistoX;

// import com.topodroid.math.Point2D;

class DrawingPathIntersection // extends PointF
{
  public DrawingPath path; // reference shot 
  float tt;         // intersection coordinate

  /** cstr
   * @param path   path of intersecting shot
   * @param t0     intersection abscissa
   */
  DrawingPathIntersection( DrawingPath path0, float t0 )
  {
    path = path0;
    tt   = t0;
  }

}
