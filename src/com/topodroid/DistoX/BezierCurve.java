/* @file BezierCurve.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid cubic bezier curve (spline)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

class BezierCurve
{
  private BezierPoint c[];      // control points of the cubic spline
  private BezierPoint Vtemp[];  // work vector of four points
  private int splitPoint;       // Point of maximum error	

  public BezierCurve()
  {
    c = new BezierPoint[4];
    Vtemp = new BezierPoint[4];
    for (int i=0; i<4; ++i ) {
      c[i] = new BezierPoint();
      Vtemp[i] = new BezierPoint();
    }
    splitPoint = -1;
  }

  public BezierCurve( BezierPoint c0, BezierPoint c1, BezierPoint c2, BezierPoint c3 )
  {
    c = new BezierPoint[4];
    Vtemp = new BezierPoint[4];
    c[0] = new BezierPoint( c0 );
    c[1] = new BezierPoint( c1 );
    c[2] = new BezierPoint( c2 );
    c[3] = new BezierPoint( c3 );
    for (int i=0; i<4; ++i ) {
      Vtemp[i] = new BezierPoint();
    }
    splitPoint = -1;
  }

  // control points
  public void setPoint(int k, BezierPoint p ) { c[k].set(p); }
  public BezierPoint getPoint( int k ) { return c[k]; }

  public int getSplitPoint() { return splitPoint; }

  /**  ComputeMaxError: Find max squared distance of digitized points to fitted curve.
      d;		  Array of digitized points	
      first, last;  Indices defining region	
      bezCurve;	  Fitted Bezier curve		
      u;		  Parameterization of points	
  */
  public float computeMaxError( ArrayList<BezierPoint> d, int first, int last, float[] u )
  {
    splitPoint = (last - first + 1)/2;
    float maxDist = 0.0f;
    for (int i = first + 1; i < last; i++) {
      BezierPoint P = evaluate( u[i-first] );
      BezierPoint v = P.sub( d.get(i) ); // vector from point to curve
      float dist = v.squareLength();
      if ( dist >= maxDist ) {
        maxDist = dist;
        splitPoint = i;
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
   * @param bezCurve    Current fitted curve
   */
  public void reparameterize( ArrayList<BezierPoint> d, int first, int last, float[] u )
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
   * t       Parametric value to find point for	
   */
  public BezierPoint evaluate( float t ) { return evaluate(3, c, t ); }

  private BezierPoint evaluate( int degree, BezierPoint[] V, float t )
  {
    float t1 = 1.0f - t;
    for (int i = 0; i <= degree; i++) { // copy array
      Vtemp[i].set( V[i] );
    }
    for (int i = 1; i <= degree; i++) {	// triangle computation
      for (int j = 0; j <= degree-i; j++) {
        Vtemp[j].mX = t1 * Vtemp[j].mX + t * Vtemp[j+1].mX;
        Vtemp[j].mY = t1 * Vtemp[j].mY + t * Vtemp[j+1].mY;
      }
    }
    return Vtemp[0];
  }

  /**  findRootNewtonRaphson: Use Newton-Raphson iteration to find better root.
      P	Digitized point		
      u	Parameter value for "P"	
   */
  private float findRootNewtonRaphson( BezierPoint P, float u)
  {
    BezierPoint Q1[] = new BezierPoint[3]; // Q'
    BezierPoint Q2[] = new BezierPoint[2]; // Q"
    
    /* Generate control vertices for Q'	*/
    for (int i = 0; i < 3; i++) {
      Q1[i] = c[i+1].sub( c[i] ).times( 3.0f );
    }
    
    /* Generate control vertices for Q'' */
    for (int i = 0; i < 2; i++) {
      Q2[i] = Q1[i+1].sub( Q1[i] ).times( 2.0f );
    }
    
    BezierPoint Q_u  = evaluate(u);        // Compute Q(u)
    BezierPoint Q1_u = evaluate(2, Q1, u); // Q'(u)
    BezierPoint Q2_u = evaluate(1, Q2, u); // Q"(u)
    
    /* Compute f(u)/f'(u) */
    float num = (Q_u.mX - P.mX) * (Q1_u.mX) + (Q_u.mY - P.mY) * (Q1_u.mY);
    float den = (Q1_u.mX)       * (Q1_u.mX) + (Q1_u.mY)       * (Q1_u.mY) 
              + (Q_u.mX - P.mX) * (Q2_u.mX) + (Q_u.mY - P.mY) * (Q2_u.mY);
    
    /* u = u - f(u)/f'(u) improved u */
    return u - num / den;
  }
}
