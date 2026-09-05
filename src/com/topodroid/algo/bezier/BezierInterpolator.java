/* @file BezierInterpolator.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid cubic bezier interpolator
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * An Algorithm for Automatically Fitting Digitized Curves
 * by Philip J. Schneider
 * from "Graphics Gems", Academic Press, 1990
 * 
 * Adapted from fit_cubic.c Piecewise cubic fitting code
 * Modified to add corner detection previous to cubic fitting.
 *
 * --------------------------------------------------------
 */
package com.topodroid.algo.bezier;

import com.topodroid.util.TDLog;
import com.topodroid.math.Point2D;

import java.util.ArrayList;

public class BezierInterpolator
{
  private ArrayList< BezierCurve > curves;  // array of cubic splines

  public BezierInterpolator()
  {
    curves = new ArrayList<>();
  }

 
  /**  fitCurve : Fit a Bezier curve to a set of digitized points 
   * d	Array of digitized points
   * nPts	Number of digitized points
   * error	User-defined error squared
   */
  public float fitCurve( ArrayList< Point2D > d, int nPts, float error, float len_thr )
  {
    // TDLog.Log( TDLog.LOG_BEZIER, "fitCurve nr. pts " + nPts );
    if ( nPts <= 1 ) return 0.0f;
    float err = 0.0f;
    curves.clear();
    Point2D p1 = d.get( 0 );
    for ( int k = 1; k < nPts; ++k ) {
      Point2D p2 = d.get( k );
      Point2D cp1 = new Point2D( p1.x/3 + 2*p2.x/3, p1.y/3 + 2*p2.y/3 );
      Point2D cp2 = new Point2D( 2*p1.x/3 + p2.x/3, 2*p1.y/3 + p2.y/3 );
      curves.add( new BezierCurve( p1, cp1, cp2, p2 ) );
      p1 = p2;
    }
    return 0.0f;
  }

  public ArrayList< BezierCurve > getCurves() { return curves; }

  // public int size() { return curves.size(); }

}
