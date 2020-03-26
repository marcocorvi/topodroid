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
package com.topodroid.math;

// import com.topodroid.math.Point2D;

import java.util.ArrayList;

public class BezierCurve
{
  private Point2D[] c;      // control points of the cubic spline
  private Point2D[] Vtemp;  // work vector of four points
  private int splitIndex;       // Point of split (criteria: maximum error)	

  // BezierCurve() // UNUSED
  // {
  //   c = new Point2D[4];
  //   Vtemp = new Point2D[4];
  //   for (int i=0; i<4; ++i ) {
  //     c[i] = new Point2D();
  //     Vtemp[i] = new Point2D();
  //   }
  //   splitIndex = -1;
  // }

  // create a Bezier segment from four 2D points
  public BezierCurve( Point2D c0, Point2D c1, Point2D c2, Point2D c3 )
  {
    c = new Point2D[4];
    Vtemp = new Point2D[4];
    c[0] = new Point2D( c0 );
    c[1] = new Point2D( c1 );
    c[2] = new Point2D( c2 );
    c[3] = new Point2D( c3 );
    for (int i=0; i<4; ++i ) {
      Vtemp[i] = new Point2D();
    }
    splitIndex = -1;
  }

  // create a Bezier segment from four 2D coord-pairs
  public BezierCurve( float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3 )
  {
    c = new Point2D[4];
    Vtemp = new Point2D[4];
    c[0] = new Point2D( x0, y0 );
    c[1] = new Point2D( x1, y1 );
    c[2] = new Point2D( x2, y2 );
    c[3] = new Point2D( x3, y3 );
    for (int i=0; i<4; ++i ) {
      Vtemp[i] = new Point2D();
    }
    splitIndex = -1;
  }

  // control points
  // void setPoint(int k, Point2D p ) { c[k].set(p); } // UNUSED
  public Point2D getPoint( int k ) { return c[k]; }

  // used by BezierInterpolator
  int getSplitIndex() { return splitIndex; }

  /**  ComputeMaxError: Find max squared distance of digitized points to fitted curve.
      d;		  Array of digitized points	
      first, last;  Indices defining region	
      u;		  Parameterization of points	
  */
  float computeMaxError( ArrayList<Point2D> d, int first, int last, float[] u )
  {
    splitIndex = (last - first + 1)/2;
    float maxDist = 0.0f;
    for (int i = first + 1; i < last; i++) {
      Point2D P = evaluate( u[i-first] );
      Point2D v = P.sub( d.get(i) ); // vector from point to curve
      float dist = v.squareLength();
      if ( dist >= maxDist ) {
        maxDist = dist;
        splitIndex = i; 
      }
    }
    return maxDist;
  }

  /**  Reparameterize: Given set of points and their parameterization,
   * try to find a better parameterization.
   *
   * @param d           Array of digitized points
   * @param first, last	Indices defining region
   * @param u           Current parameter values
   */
  void reparameterize( ArrayList<Point2D> d, int first, int last, float[] u )
  {
    // int nPts = last-first+1;
    // float[] uPrime = new float[ nPts ]; /*  New parameter values	*/
  
    for (int i = first; i <= last; i++) {
      // uPrime[i-first] = findRootNewtonRaphson( d[i], u[i-first] );
      u[i-first] = findRootNewtonRaphson( d.get(i), u[i-first] );
    }
    // return uPrime;
  }

  /**  Bezier: Evaluate a Bezier curve at a particular parameter value
   * degree  The degree of the bezier curve
   * @param t       Parametric value to find point for
   * note used by cSurevy export
   */
  public Point2D evaluate( float t ) { return evaluate(3, c, t ); }

  private Point2D evaluate( int degree, Point2D[] V, float t )
  {
    float t1 = 1.0f - t;
    for (int i = 0; i <= degree; i++) { // copy array
      Vtemp[i].set( V[i] );
    }
    for (int i = 1; i <= degree; i++) {	// triangle computation
      for (int j = 0; j <= degree-i; j++) {
        Vtemp[j].x = t1 * Vtemp[j].x + t * Vtemp[j+1].x;
        Vtemp[j].y = t1 * Vtemp[j].y + t * Vtemp[j+1].y;
      }
    }
    return Vtemp[0];
  }

  /**  findRootNewtonRaphson: Use Newton-Raphson iteration to find better root.
   * @param P	digitized point		
   * @param u	parameter value for "P"	
   * @return improved 'u'
   */
  private float findRootNewtonRaphson( Point2D P, float u)
  {
    Point2D[] Q1 = new Point2D[3]; // Q'
    Point2D[] Q2 = new Point2D[2]; // Q"
    
    /* Generate control vertices for Q'	*/
    for (int i = 0; i < 3; i++) {
      Q1[i] = c[i+1].sub( c[i] ).times( 3.0f );
    }
    
    /* Generate control vertices for Q'' */
    for (int i = 0; i < 2; i++) {
      Q2[i] = Q1[i+1].sub( Q1[i] ).times( 2.0f );
    }
    
    Point2D Q_u  = evaluate(u);        // Compute Q(u)
    Point2D Q1_u = evaluate(2, Q1, u); // Q'(u)
    Point2D Q2_u = evaluate(1, Q2, u); // Q"(u)
    
    /* Compute f(u)/f'(u) */
    float num = (Q_u.x - P.x) * (Q1_u.x) + (Q_u.y - P.y) * (Q1_u.y);
    float den = (Q1_u.x)      * (Q1_u.x) + (Q1_u.y)      * (Q1_u.y) 
              + (Q_u.x - P.x) * (Q2_u.x) + (Q_u.y - P.y) * (Q2_u.y);
    
    /* u = u - f(u)/f'(u) improved u */
    return u - num / den;
  }
}
