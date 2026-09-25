/** @file LineIntersection.java
 *
 * @author marco corvi
 * @date sept 2026
 *
 * @brief TopoDroid drawing: intersection of a segment with a line
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.algo.clip;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDMath;
import com.topodroid.math.Point2D;
import com.topodroid.TDX.DrawingPointLinePath;
import com.topodroid.TDX.LinePoint;

public class LineIntersection
{
  // intersection flags
  public static final int INTERSECT = -1;
  public static final int OUTSIDE = 0;
  private static final int LEFT   = 1;
  private static final int TOP    = 2;
  private static final int RIGHT  = 4;
  private static final int BOTTOM = 8;

  public static float mT;    // intersect abscissa of the test segment
  public static Point2D mPt; // P1 + mT (P2 - P1)

  // private static final boolean skip = true;
  private static final boolean flaf = false;   // whether to use flag
  private static final boolean linear = false; // force using liner intersection

  /** @return the intersection flag of a line and a segment P1-P2
   * @param line  line
   * @param p1    segment first endpoint
   * @param p2    segment second endpoint
   * @param cubc  whether line is cubic
   * @note forward seach from the beginning of the line
   */
  public static int flag( DrawingPointLinePath line, LinePoint p1, LinePoint p2, boolean cubic )
  {
    int f1 = pointPosition( line, p1 );
    int f2 = pointPosition( line, p2 );
    int f = f1 & f2;
    if ( flaf && f != 0 ) { 
      TDLog.v("flag " + f + " ==> skip line " );
      return f;
    }
    // eiher both inside or on opposite sides
    LinePoint q1 = line.first();
    LinePoint q2 = line.next( q1 ); // q1.mNext;
    float dpx = p2.x-p1.x;
    float dpy = p2.y-p1.y;
    // TDLog.v("point " + p1.x + " " + p1.y + " dp " + dpx + " " + dpy );
    if ( intersect( q1, q2, q2.cp1(), q2.cp2(), p1, dpx, dpy, cubic ) ) return INTERSECT;
    q1 = q2; 
    for ( q2 = line.next( q1 ); q2 != null; q2 = line.next( q1 ) ) { // q1.mNext;
      if ( intersect( q1, q2, q2.cp1(), q2.cp2(), p1, dpx, dpy, cubic ) ) return INTERSECT;
      q1 = q2;
      if ( q1 == line.last() ) return OUTSIDE;
    }
    return OUTSIDE;
  }

  /** @return the intersection flag of a line and a segment P1-P2
   * @param line  line
   * @param first point of line from which to start forward search of the intersection
   * @param p1    segment first endpoint
   * @param p2    segment second endpoint
   * @param cubic whether line is cubic
   */
  public static int forwardFlag( DrawingPointLinePath line, LinePoint first, LinePoint p1, LinePoint p2, boolean cubic )
  {
    if ( first == null ) return OUTSIDE;
    int f1 = pointPosition( line, p1 );
    int f2 = pointPosition( line, p2 );
    int f = f1 & f2;
    if ( flaf && f != 0 ) {
      TDLog.v("forward flag " + f + " ==> skip line ");
      return f;
    }
    TDLog.v("forward flag from " + first.x + " " + first.y + " pos " + f );
    // eiher both inside or on opposite sides
    LinePoint q1 = first;
    LinePoint q2 = line.next( q1 ); // q1.mNext;
    if ( q2 == null ) return OUTSIDE;
    float dpx = p2.x-p1.x;
    float dpy = p2.y-p1.y;
    TDLog.v("point " + p1.x + " " + p1.y + " dp " + dpx + " " + dpy );
    if ( intersect( q1, q2, q2.cp1(), q2.cp2(), p1, dpx, dpy, cubic ) ) return INTERSECT;
    q1 = q2;
    for ( q2 = line.next( q1 ); q2 != null; q2 = line.next( q1 ) ) { // q1.mNext;
      if ( intersect( q1, q2, q2.cp1(), q2.cp2(), p1, dpx, dpy, cubic ) ) return INTERSECT;
      q1 = q2;
      if ( q1 == line.last() ) return OUTSIDE;
    }
    return OUTSIDE;
  }

  /** @return the intersection flag of a line and a segment P1-P2
   * @param line  line
   * @param first point of line from which to start backward search of the intersection
   * @param p1    segment first endpoint
   * @param p2    segment second endpoint
   * @param cubc  whether line is cubic
   */
  public static int backwardFlag( DrawingPointLinePath line, LinePoint first, LinePoint p1, LinePoint p2, boolean cubic )
  {
    if ( first == null ) return OUTSIDE;
    int f1 = pointPosition( line, p1 );
    int f2 = pointPosition( line, p2 );
    int f = f1 & f2;
    if ( flaf && f != 0 ) {
      TDLog.v("backward flag " + f + " ==> skip line ");
      return f;
    }
    TDLog.v("backward flag from " + first.x + " " + first.y + " pos " + f );
    // eiher both inside or on opposite sides
    LinePoint q1 = first;
    LinePoint q2 = line.prev( q1 ); // q1.mPrev;
    if ( q2 == null ) return OUTSIDE;
    float dpx = p2.x-p1.x;
    float dpy = p2.y-p1.y;
    TDLog.v("point " + p1.x + " " + p1.y + " dp " + dpx + " " + dpy );
    if ( intersect( q2, q1, q1.cp1(), q1.cp2(), p1, dpx, dpy, cubic ) ) return INTERSECT;
    q1 = q2;
    for ( q2 = line.next( q1 ); q2 != null; q2 = line.prev( q1 ) ) { // q1.mPrev;
      if ( intersect( q2, q1, q1.cp1(), q1.cp2(), p1, dpx, dpy, cubic ) ) return INTERSECT;
      q1 = q2;
      if ( q1 == line.first() ) return OUTSIDE;
    }
    return OUTSIDE;
  }

  /** intersect with a (cubic) segment
   * @param q1   segment first endpoint
   * @param q2   segment sceond endpoint
   * @param cp1  segment first control-point (near q1) or null
   * @param cp2  segment secong control-point (near q2) or null
   * @param p    intersecting segment base-point P1
   * @param dpx  intersecting segment (P2-P1).x
   * @param dpy  intersecting segment (P2-P1).y
   * @param cubc  whether line is cubic
   */
  private static boolean intersect( Point2D q1, Point2D q2, Point2D cp1, Point2D cp2, Point2D p, float dpx, float dpy, boolean cubic )
  {
    if ( cubic ) {
      return cubicIntersect( q1, q2, cp1, cp2, p, dpx, dpy );
    } else {
      return linearIntersect( q1, q2, p, dpx, dpy );
    }
  }

  /** @return true if the segment Q1-Q2 intersects P + t * dP (t in [0,1])
   * @param q1   first endpoint of first segment
   * @param q2   second endpoint of first segment
   * @param p    basepoint of second segment
   * @param dp   displacement at the second endpoint of the second segment
   *
   * Q1 - s DQ = P1 + t DP
   * where DP = P2 - P1
   *       DQ = Q1 - Q2
   *     
   * Q1 - P1 = t DP + s DQ = DP.x  DQ.x   t
                             DP.y  DQ.y   s
   *
   * t =  DQ.y  -DQ.x   (q1.x - p1.x) / det
   * s = -DP.y   DP.x   (q1.y - p1.y) / det
   */
  private static boolean linearIntersect( Point2D q1, Point2D q2, Point2D p, float dpx, float dpy )
  {
    mPt = null;
    // TDLog.v("intersect linear " + q1.x + " " + q1.y + "  " + q2.x + " " + q2.y );
    float dqx = q1.x-q2.x;
    float dqy = q1.y-q2.y;
    float det = dpx * dqy - dqx * dpy;
    if ( det == 0 ) return false; // FIXME check whether p1 or p2 are on the q1-q2 segment
    float rx = q1.x-p.x;
    float ry = q1.y-p.y;
    mT = (dqy * rx - dqx * ry)/det;
    float s = (dpx * ry - dpy * rx)/det;
    boolean ret = ( mT >= 0 && mT <= 1 && s >= 0 && s <= 1 );
    // if ( ret ) mPt = new Point2D( p.x + s * dpx, p.y + s * dpy );
    if ( ret ) mPt = new Point2D( p.x + mT * dpx, p.y + mT * dpy );
    return ret;
  }

  // this intersect skip bezier segments
  private static boolean intersect_3( Point2D q1, Point2D q2, Point2D cp1, Point2D cp2, Point2D p, float dpx, float dpy )
  { 
    if ( cp1 != null || cp2 != null ) return false;
    return linearIntersect( q1, q2, p, dpx, dpy );
  }

  /**
   * @param cp1  first control point of q2
   * @param cp2  second control point of q2
   */
  private static boolean cubicIntersect( Point2D q1, Point2D q2, Point2D cp1, Point2D cp2, Point2D p, float dpx, float dpy )
  { 
    if ( cp1 == null || cp2 == null ) return linearIntersect( q1, q2, p, dpx, dpy );
    mPt = null;
    boolean ret = false;
    // TDLog.v("intersect cubic " + q1.x + " " + q1.y + "  " + q2.x + " " + q2.y + " CP1 " + cp1.x + " " + cp1.y + " CP2 " + cp2.x + " " + cp2.y );
    // (1-s)^3 Q1 + (1-s)^2 s CP1 + (1-s) s^2 CP2 + s^3 Q2 = P + t DP
    // dpy * [...]x - dpx * [...]y = 0
    float ax = q2.x - cp2.x +     cp1.x -     q1.x ;
    float ay = q2.y - cp2.y +     cp1.y -     q1.y ;
    float bx =        cp2.x - 2 * cp1.x + 3 * q1.x ;
    float by =        cp2.y - 2 * cp1.y + 3 * q1.y ;
    float cx =                    cp1.x - 3 * q1.x ;
    float cy =                    cp1.y - 3 * q1.y ;
    float dx =                                q1.x - p.x ;
    float dy =                                q1.y - p.y ;
    float a  = dpy * ax - dpx * ay;
    float b  = dpy * bx - dpx * by;
    float c  = dpy * cy - dpx * cy;
    float d  = dpy * dy - dpx * dy;
    if ( a == 0 ) { // second order b s^2 + c s + d = 0
      if ( b == 0 ) { // linear
        if ( c == 0 ) return false;
        float s = -d / c;  // c s + d 
        if ( s < 0 || s > 1 ) return false;
        float tdpx = cx * s + dx;
        float tdpy = cy * s + dy;
        mT = (dpx != 0 )? tdpx / dpx : tdpy / dpy;
        ret = ( mT >= 0 && mT <= 1 );
        if ( ret ) mPt = new Point2D( p.x + tdpx, p.y + tdpy );
        return ret;
      } 
      // b t^2 + c t + d
      float d0 = c*c - 4*b*d; // determinant
      if (d0 < 0 ) return false;
      if ( d0 > 0 ) d0 = TDMath.sqrt( d0 );
      float s1 = ( -c + d0 ) / (2 * b);
      float s2 = ( -c - d0 ) / (2 * b);
      float s = 0;
      if ( s1 >= 0 && s1 <= 1 ) {
        s = s1;
        if ( s2 >= 0 /* && s2 <= 1 */ && s2 < s1 ) s = s2; // use first intersect
      } else if ( s2 >= 0 && s2 <= 1 ) {
        s = s2;
      } else {
        return false;
      }
      // here s in [0,1]
      float t1dpx = (bx * s + cx) * s + dx;
      float t1dpy = (by * s + cy) * s + dy;
      mT = (dpx != 0 )? t1dpx / dpx : t1dpy / dpy;
      ret = ( mT >= 0 && mT <= 1 );
      if ( ret ) mPt = new Point2D( p.x + t1dpx, p.y + t1dpy );
      return ret;
    } // third order
    float alpha = b / (3 * a);
    float C1 =     ( c         - b * 2 * alpha         + a * 3 * alpha * alpha ) / a;
    float D1 = ( d - c * alpha + b     * alpha * alpha - a     * alpha * alpha * alpha ) / a;
    // s^3 + C1 s + D1 = 0
    // let s = u + v with u*v = - C1/3
    //     x = u^3 and the eq. becomes x^2 + D1 x + (C1/3)^3 = 0
    float b1 = D1;
    float c1 = C1 * C1 * C1 / 27;
    float d1 = b1 * b1 - 4 * c1;
    // there are solutions if d1 >= 0
    if ( d1 < 0 ) return false;
    if ( d1 > 0 ) d1 = TDMath.sqrt( d1 );
    float x1 = ( - b1 + d1 ) / 2;
    float x2 = ( - b1 - d1 ) / 2;
    float u = TDMath.pow( TDMath.abs(x1), 1.0f/3.0f ); if ( x1 < 0 ) u = -u;
    float v = TDMath.pow( TDMath.abs(x2), 1.0f/3.0f ); if ( x2 < 0 ) v = -v;
    float s = u + v - alpha;
    if ( s < 0 || s > 1 ) return false;
    float t3dpx = ((ax * s + bx) * s + cx) * s + dx;
    float t3dpy = ((ay * s + by) * s + cy) * s + dy;
    mT = ( dpx != 0 )? t3dpx / dpx : t3dpy / dpy;
    ret = ( mT >= 0 && mT <= 1 );
    if ( ret ) {
      // mPt = new Point2D( p.x + t3dpx, p.y + t3dpy );
      float s1 = 1 - s;
      float s12 = s1 * s1;
      float s13 = s1 * s12;
      float s2  = s * s;
      float s3  = s * s2;
      mPt = new Point2D( s13 * q1.x + s12 * s * cp1.x + s1 * s2 * cp2.x + s3 * q2.x, s13 * q1.y + s12 * s * cp1.y + s1 * s2 * cp2.y + s3 * q2.y );
    }
    return ret;
  }
    

  /** @return the 2D point position flag with respect to the line bounding box
   * @param line  line
   * @param p     point
   */
  private static int pointPosition( DrawingPointLinePath line, LinePoint p )
  {
    int ret = 0;
    if ( p.x < line.left ) { 
      ret += LEFT;
    } else if ( p.x > line.right ) {
      ret += RIGHT;
    }
    if ( p.y < line.top ) {
      ret += TOP;
    } else if ( p.y > line.bottom ) {
      ret += BOTTOM;
    }
    return ret;
  }

}

