/* @file Geometry2D.java
 *
 * @author marco corvi
 * @date non 2014
 *
 * @brief TopoDroid  2d geometry
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.graphics.PointF;

class Geometry2D
{
  // intersection of two lines in the plane
  // x = p1.x + s ( p2.x - p1.x ) = q1.x + t ( q2.x - q1.x )
  // y = p1.y + s ( p2.y - p1.y ) = q1.y + t ( q2.y - q1.y )
  //
  //  a s + b t = e
  //  c s + d t = f
  // inverse:
  //   s =  d  -b  *  e  / det
  //   t   -c   a     f
  //
  // @param pt point of intersection [out]
  //
  static float intersect( LinePoint p1, LinePoint p2, PointF q1, PointF q2, PointF pt )
  {
     float a = p2.mX - p1.mX;
     float b = q1.x - q2.x;
     float c = p2.mY - p1.mY;
     float d = q1.y - q2.y;
     float e = q1.x - p1.mX;
     float f = q1.y - p1.mY;
     float det = a * d - b * c;
     float t = ( -c * e + a * f ) / det;
     if ( pt != null ) {
       pt.x = (1-t) * q1.x + t * q2.x;
       pt.y = (1-t) * q1.y + t * q2.y;
     }
     return t;
  }
}
