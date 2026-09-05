/* @file BezierCurve.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid cubic bezier curve (spline)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.algo.bezier;

import com.topodroid.math.Point2D;

import java.util.ArrayList;

public class BezierCurve
{
  private Point2D[] c;      // control points of the cubic spline
  private Point2D[] v_temp;  // work vector of four points
  private int splitIndex;       // Point of split (criteria: maximum error)	

  // BezierCurve() // UNUSED
  // {
  //   c = new Point2D[4];
  //   v_temp = new Point2D[4];
  //   for (int i=0; i<4; ++i ) {
  //     c[i] = new Point2D();
  //     v_temp[i] = new Point2D();
  //   }
  //   splitIndex = -1;
  // }

  // create a Bezier segment from four 2D points
  public BezierCurve( Point2D c0, Point2D c1, Point2D c2, Point2D c3 )
  {
    c = new Point2D[4];
    v_temp = new Point2D[4];
    c[0] = new Point2D( c0 );
    c[1] = new Point2D( c1 );
    c[2] = new Point2D( c2 );
    c[3] = new Point2D( c3 );
    for (int i=0; i<4; ++i ) {
      v_temp[i] = new Point2D();
    }
    splitIndex = -1;
  }

  // create a Bezier segment from four 2D coord-pairs
  public BezierCurve( float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3 )
  {
    c = new Point2D[4];
    v_temp = new Point2D[4];
    c[0] = new Point2D( x0, y0 );
    c[1] = new Point2D( x1, y1 );
    c[2] = new Point2D( x2, y2 );
    c[3] = new Point2D( x3, y3 );
    for (int i=0; i<4; ++i ) {
      v_temp[i] = new Point2D();
    }
    splitIndex = -1;
  }

  // control points
  // void setPoint(int k, Point2D p ) { c[k].set(p); } // UNUSED
  public Point2D getPoint( int k ) { return c[k]; }

  // used by BezierInterpolator
  int getSplitIndex() { return splitIndex; }

  /**  ComputeMaxError: Find max squared distance of digitized points to fitted curve.
   * @param d           array of digitized points	
   * @param first, last indices defining region	
   * @param u           parameterization of points	
   */
  float computeMaxError( ArrayList< Point2D > d, int first, int last, float[] u )
  {
    splitIndex = (last - first + 1)/2;
    float maxDist = 0.0f;

    return maxDist;
  }

  /**  re-parametrize: Given set of points and their parameterization,
   * try to find a better parameterization.
   *
   * @param d           Array of digitized points
   * @param first, last	Indices defining region
   * @param u           Current parameter values
   */
  void reparametrize( ArrayList< Point2D > d, int first, int last, float[] u )
  {
 
  }

  /**  Bezier: Evaluate a Bezier curve at a particular parameter value
   * degree  The degree of the bezier curve
   * @param t       Parametric value to find point for
   * note used by cSurvey export
   */
  public Point2D evaluate( float t ) { return evaluate(3, c, t ); }

  private Point2D evaluate( int degree, Point2D[] V, float t )
  {
    for (int i = 0; i <= degree; i++) { // copy array
      v_temp[i].set( V[i] );
    }
    return v_temp[0];
  }


}
