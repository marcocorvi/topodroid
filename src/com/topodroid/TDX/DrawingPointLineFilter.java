/* @file DrawingPointLineFilter.java
 *
 * @author marco corvi
 * @date mar 2023
 *
 * @brief TopoDroid main drawing pointline transformer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.prefs.TDSetting;

import com.topodroid.math.Point2D;
import com.topodroid.math.BezierCurve;
import com.topodroid.math.BezierInterpolator;

import java.util.ArrayList;

class DrawingPointLineFilter
{
  private static BezierInterpolator mBezierInterpolator = new BezierInterpolator();

  /** transform a line/area path - the applied transform depends on the line-path style
   * @param first    first line point
   * @param last     last line point (can be null)
   * @param path     path where the points are copied
   * @param zoom     canvas zoom (used only for weeding)
   * @return true on success
   */
  static boolean transform( LinePoint first, LinePoint last, DrawingPointLinePath path, float zoom )
  {
    if ( TDSetting.isLineStyleBezier() ) {
      return bezier( first, last, path );
    } else if ( TDSetting.isLineStyleSimplified() ) {
      return weeding( first, last, path, zoom );
    } else {
      return decimation( first, last, path );
    }
  }

  /** decimation does not do anything
   * @param first   first line point
   * @param last    last line point
   * @param path    line/area path
   * @return always true
   */
  static boolean decimation(  LinePoint first, LinePoint last, DrawingPointLinePath path )
  {
    return true;
  }

  /** copy a string of line points and puth it inside a path
   * @param first    first line point
   * @param last     last line point (can be null)
   * @param path     path where the points are copied
   * @return true on success
   */
  static boolean copy(  LinePoint first, LinePoint last, DrawingPointLinePath path )
  {
    if ( first == last || first.mNext == null ) return false;
    LinePoint lp = first;
    path.addStartPoint( lp.x, lp.y );
    for ( lp = lp.mNext; lp != last; lp = lp.mNext) {
      path.addPoint( lp.x, lp.y );
    }
    if ( last != null ) path.addPoint( last.x, last.y );
    return true;
  }

  /** compute the weeding of a string of line points and puth it inside a path
   * @param first    first line point
   * @param last     last line point (can be null)
   * @param path     path where the weeded points are put
   * @param zoom     canvas zoom
   * @return true on success
   */
  static boolean weeding( LinePoint first, LinePoint last, DrawingPointLinePath path, float zoom )
  {
    Weeder weeder = new Weeder();
    int cnt = 0;
    for ( LinePoint lp = first; lp != last; lp = lp.mNext ) {
      weeder.addPoint( lp.x, lp.y );
      ++ cnt;
    }
    if ( last != null ) {
      weeder.addPoint( last.x, last.y );
      ++ cnt;
    }
    // get pixels from meters
    // float dist = mZoom*DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance;
    float dist = DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance; // N.B. no zoom
    float len  = zoom*DrawingUtil.SCALE_FIX*TDSetting.mWeedLength;
    // TDLog.v( "Weed dist " + dist + " len " + len );

    ArrayList< Point2D > points = weeder.simplify( dist, len );
    int k0 = points.size();
    // TDLog.v( " Weeding " + cnt + " -> " + k0 );
    if ( k0 <= 1 ) return false;
    Point2D p0 = points.get(0);
    path.addStartPoint( p0.x, p0.y );
    for (int k=1; k<k0; ++k) {
      p0 = points.get(k);
      path.addPoint(p0.x, p0.y );
    }
    return true;
  }

  /** compute the Bezier interpolation of a string of line points and puth it inside a path
   * @param first    first line point
   * @param last     last line point (can be null)
   * @param path     path where the interpolated points are put
   * @return true on success
   */
  static boolean bezier( LinePoint first, LinePoint last, DrawingPointLinePath path )
  {
    ArrayList< Point2D > pts = new ArrayList<>(); // [ nPts ];
    for ( LinePoint lp = first; lp != last; lp = lp.mNext ) {
      pts.add( new Point2D( lp.x, lp.y ) );
    }
    if ( last != null ) pts.add( new Point2D( last.x, last.y ) );
    mBezierInterpolator.fitCurve( pts, pts.size(), TDSetting.mLineAccuracy, TDSetting.mLineCorner );
    ArrayList< BezierCurve > curves = mBezierInterpolator.getCurves();
    int k0 = curves.size();
    // TDLog.v( " Bezier " +  pts.size() + " -> " + k0 );
    if ( k0 < 1 ) return false; // 20240129 with <= short bezier made of just one piece are discarded
    BezierCurve c = curves.get(0);
    Point2D p0 = c.getPoint(0);
    path.addStartPoint( p0.x, p0.y );
    for (int k=0; k<k0; ++k) {
      c = curves.get(k);
      Point2D p1 = c.getPoint(1);
      Point2D p2 = c.getPoint(2);
      Point2D p3 = c.getPoint(3);
      path.addPoint3(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y );
    }
    return true;
  }

}
