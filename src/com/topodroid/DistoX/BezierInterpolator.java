/* @file BezierInterpolator.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid cubic bezier interpolator
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
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
 * CHANGES
 * 20120725 TopoDroidApp log
 * 20121109 enforced alpha_left + alpha_right < 2/3 * distance( point_left, point_right )
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

public class BezierInterpolator
{
  private ArrayList< BezierCurve > curves;  // array of cubic splines
  private float[][] C;                      // Matrix C: 2x2
  private float[]   X;                      // Matrix X: 2x1

  public BezierInterpolator()
  {
    curves = new ArrayList< BezierCurve >();
    C = new float[2][2];
    X = new float[2];
  }

  /** computeLeftTangent, computeRightTangent, computeCenterTangent:
   * Approximate unit tangents at endpoints and "center" of digitized curve
   * d   Digitized points
   * end Index to "left" end of region 
   */
  private BezierPoint computeLeftTangent( ArrayList<BezierPoint> d, int end)
  {
    BezierPoint tHat1 = d.get(end+1).sub( d.get(end) );
    tHat1.normalize();
    return tHat1;
  }
  
  /** end  Index to "right" end of region */
  private BezierPoint computeRightTangent( ArrayList<BezierPoint> d, int end)
  {
    BezierPoint tHat2 = d.get(end-1).sub( d.get(end) );
    tHat2.normalize();
    return tHat2;
  }
  
  /** center  Index to point inside region */
  private BezierPoint computeCenterTangent( ArrayList<BezierPoint> d, int center)
  {
    BezierPoint V1 = d.get(center-1).sub( d.get(center) );
    BezierPoint V2 = d.get(center).sub( d.get(center+1) );
    BezierPoint tHatCenter = new BezierPoint( (V1.mX + V2.mX)/2.0f, (V1.mY + V2.mY)/2.0f );
    tHatCenter.normalize();
    return tHatCenter;
  }

  // -------------------------------------------------------------
  // BEZIER

  /*  B0, B1, B2, B3: Bezier multipliers */
  float B0(float t, float t1) { return t1*t1*t1;  }
  float B1(float t, float t1) { return 3*t*t1*t1; }
  float B2(float t, float t1) { return 3*t*t*t1;  }
  float B3(float t, float t1) { return t*t*t;     }

  /**  generateBezier: Use least-squares method to find Bezier CP's for region.
   * d             Array of digitized points
   * first, last   Indices defining region
   * u             Parameter values for region
   * tHat1, tHat2  Unit tangents at endpoints
   */
  private BezierCurve generateBezier( ArrayList<BezierPoint> d, int first, int last, 
                              float[] u,
                              BezierPoint tHat1, BezierPoint tHat2 )
  {
  
    int nPts = last - first + 1; // nr. of points in sub-curve
  
    /* Initialize the C and X matrices	*/
    C[0][0] = 0.0f;
    C[0][1] = 0.0f;
    C[1][0] = 0.0f;
    C[1][1] = 0.0f;
    X[0]    = 0.0f;
    X[1]    = 0.0f;
  
    /* Compute the A's	*/
    BezierPoint bf = d.get( first );
    BezierPoint bl = d.get( last );
    for (int i = 0; i < nPts; i++) {
      float t  = u[i];
      float t1 = 1.0f - t;
      float b1t = B1(t, t1);
      float b2t = B2(t, t1);
      BezierPoint b0 = tHat1.times( b1t );
      BezierPoint b1 = tHat2.times( b2t );
      C[0][0] += b0.dot( b0 );
      C[0][1] += b0.dot( b1 );
      C[1][0] = C[0][1]; /*  C[1][0] += b1.dot( b0 );*/	
      C[1][1] += b1.dot( b1 );
  
      BezierPoint tmp = d.get(first + i).sub(
        bf.times( B0(t,t1) ).add(
        bf.times( b1t ).add(
  	bl.times( b2t ).add(
        bl.times( B3(t,t1) ) ) ) ) );
  	
      X[0] += b0.dot( tmp );
      X[1] += b1.dot( tmp );
    }
  
    /* Compute the determinants of C and X	*/
    float det_C0_C1 = C[0][0] * C[1][1] - C[1][0] * C[0][1];
    float det_C0_X  = C[0][0] * X[1]    - C[0][1] * X[0];
    float det_X_C1  = X[0]    * C[1][1] - X[1]    * C[0][1];
  
    /* Finally, derive alpha values	*/
    if ( det_C0_C1 == 0.0f ) {
      det_C0_C1 = (C[0][0] * C[1][1]) * 0.00000001f ;
    }
    float alpha_l = det_X_C1 / det_C0_C1;
    float alpha_r = det_C0_X / det_C0_C1;

    if ( Float.isNaN( alpha_l ) || Float.isNaN( alpha_r ) ) {
      // TopoDroidLog.Log( TopoDroidLog.LOG_BEZIER, "Npts " + nPts + " alpha " + alpha_l + " " + alpha_r );
      for (int i = 0; i < nPts; i++) {
        BezierPoint p = d.get(first + i);
        // TopoDroidLog.Log( TopoDroidLog.LOG_BEZIER, "Pt " + i + ": " + p.mX + " " + p.mY );
      }
    }
  
    /* If alpha negative, use the Wu/Barsky heuristic (see text) 
     * (if alpha is 0, you get coincident control points that lead to
     * divide by zero in any subsequent NewtonRaphsonRootFind() call.
     */
    if ( alpha_l < 0.01f || alpha_r < 0.01f ) {
      float dist = d.get(last).distance( d.get(first) ) / 3.0f;
      return new BezierCurve( bf,
                              bf.add( tHat1.times(dist) ),
                              bl.add( tHat2.times(dist) ),
                              bl );
    }
  
    /*  First and last control points of the Bezier curve are 
     *  positioned exactly at the first and last data points 
     *  Control points 1 and 2 are positioned an alpha distance out 
     *  on the tangent vectors, left and right, respectively 
     */

    /* Heuristic to avoid spikes:
     * if the sum of the length of the two control vectors is more that 2/3 of the distance 
     * between the base points, reduce the length of the control vectors 
     */
    float dfl = bf.distance( bl ) * 0.66f;
    float alr = alpha_l + alpha_r;
    if ( alr > dfl ) {
      dfl /= alr;
      alpha_l *= dfl;
      alpha_r *= dfl;
    }
    return new BezierCurve( bf,
                            bf.add( tHat1.times( alpha_l ) ),
                            bl.add( tHat2.times( alpha_r ) ),
                            bl );
  }

  /**  chordLengthParameterize : Assign parameter values to digitized points 
   *	using relative distances between points.
   * @param d            Array of digitized points
   * @param first, last  Indices defining region
   */
  private float[]
  chordLengthParameterize( ArrayList<BezierPoint> d, int first, int last)
  {
    int	nPts = last - first + 1;	
    float[] u = new float[ nPts ];
  
    u[0] = 0.0f;
    for (int i = first+1; i <= last; i++) {
      u[i-first] = u[i-first-1] + d.get(i).distance( d.get(i-1) );
    }
    float den = u[last-first];
    if ( den > 0.0f ) {
      for (int i = first + 1; i <= last; i++) {
        u[i-first] = u[i-first] / den;
      }
    }
    return u;
  }
    
  // -------------------------------------------------------------------
  // FITTING 
  
  /**  fitCubic : Fit a Bezier curve to a (sub)set of digitized points
   * @param d;		Array of digitized points 
   * @param first, last;	Indices of first and last pts in region
   * @param tHat1, tHat2;	Unit tangent vectors at endpoints
   * @param error;		User-defined error squared
   */
  private float fitCubic( ArrayList<BezierPoint> d, int first, int last, 
                          BezierPoint tHat1, BezierPoint tHat2,
                          float error )
  {
    int	maxIterations = 4; /*  Max times to try iterating  */
    BezierCurve	bezCurve;       /* Control points of fitted Bezier curve*/
  
    // float iterationError = error * error; // error below which try iterating
    float iterationError = error * 4.0f; // error below which try iterating
    int nPts = last - first + 1;          // nr. of points in the subset
    BezierPoint bf = d.get( first );
    BezierPoint bl = d.get( last );

    if ( nPts < 2 ) {
      // TopoDroidLog.Log( TopoDroidLog.LOG_BEZIER, "fitCubic with " + nPts + " points");
      // bezCurve = new BezierCurve( bf, bf, bl, bl );
      // insertBezierCurve( bezCurve );
      return 0.0f; 
    }
  
    /*  Use heuristic if region only has two points in it */
    if (nPts == 2) {
      float dist = d.get(last).distance( d.get(first) ) / 3.0f;
      bezCurve = new BezierCurve( bf,
                                  bf.add( tHat1.times( dist ) ),
                                  bl.add( tHat2.times( dist ) ),
                                  bl );
      insertBezierCurve( bezCurve );
      return 0.0f; 
    }
  
    /*  Parameterize points, and attempt to fit curve */
    float[] u = chordLengthParameterize( d, first, last );
    bezCurve = generateBezier( d, first, last, u, tHat1, tHat2 );
  
    /*  Find max deviation (max fittin error) of points to fitted curve */
    float maxError = bezCurve.computeMaxError( d, first, last, u );
    if (maxError < error) {
      insertBezierCurve( bezCurve );
      return maxError;
    }
  
  
    /*  If error not too large, try some reparameterization and iteration */
    if (maxError < iterationError) {
      for (int i = 0; i < maxIterations; i++) {
        bezCurve.reparameterize(d, first, last, u);
        bezCurve = generateBezier(d, first, last, u, tHat1, tHat2);
        maxError = bezCurve.computeMaxError(d, first, last, u );
        if (maxError < error) {
          insertBezierCurve(bezCurve);
          return maxError;
        }
      }
    }
  
    /* Fitting failed -- split at max error point and fit recursively */
    int split  = bezCurve.getSplitPoint();
    BezierPoint tHatCenter = computeCenterTangent( d, split );
    float err1 = fitCubic( d, first, split, tHat1, tHatCenter, error );
    tHatCenter.negate();
    float err2 = fitCubic( d, split, last, tHatCenter, tHat2, error );
    return (err1 < err2)? err2 : err1;
  }

  private void insertBezierCurve( BezierCurve curve )
  {
    curves.add( curve );
  }

  private ArrayList<Integer> findCorners( ArrayList<BezierPoint> d, int nPts, float len_thr )
  {
    float len_thr_low = len_thr * 1.6f;
    float len_thr_hgh = len_thr * 2.0f;
    if ( nPts <= 1 ) return null;
    float[] dist = new float[nPts];
    int[] nghb = new int[nPts];
    dist[0] = 0.0f;
    for (int k=1; k<nPts; ++k ) {
      dist[k] = d.get(k).distance( d.get(k-1) );
    }
    float len = 0.0f;
    int k2 = 0;
    int k1 = 0;
    for ( ; k1 < nPts; ++k1 ) {
      len -= dist[k1]; // subtract distance(k1-1,k1)
      while ( len < len_thr ) {
        if ( (++ k2) == nPts) {
          for ( ; k1 < nPts; ++k1 ) nghb[k1] = nPts;
          break;
        }
        len += dist[k2]; // add distance(k2-1,k2)
      }
      if ( k2 == nPts ) break;
      nghb[k1] = k2;
    }

    ArrayList<Integer> corners = new ArrayList<Integer>();
    corners.add( new Integer(0) );
    int kc = 0;
    int kgap = 0;
    float dc = 0.0f;
    boolean in_corner = false;
    for (k1=0; k1<nPts; ++k1) {
      int k0 = nghb[k1];
      if ( k0 == nPts ) break;
      k2 = nghb[k0];
      if ( k2 == nPts ) break;
      BezierPoint p1 = d.get(k1); 
      BezierPoint p2 = d.get(k2);
      float d0 = p1.distance( p2 );
      if ( d0 < len_thr_low ) {
        if ( ! in_corner ) {
          in_corner = true;
          dc = d0;
          kc = k0;
        } else if ( d0 < dc ) {
          dc = d0;
          kc = k0;
        }
      } else if ( d0 > len_thr_hgh ) {
        in_corner = false;
        if ( kc > kgap ) {
          corners.add( new Integer(kc) );
          kgap = k0;
        }
      }
    }
    if ( in_corner ) {
      if ( kc > kgap ) corners.add( new Integer(kc) );
    }
    if ( kc != nPts-1 ) { // last point is a corner
      corners.add( new Integer(nPts-1) );
    }
    return corners;  
  }

  /**  fitCurve : Fit a Bezier curve to a set of digitized points 
   * d	Array of digitized points
   * nPts	Number of digitized points
   * error	User-defined error squared
   */
  public float fitCurve( ArrayList<BezierPoint> d, int nPts, float error, float len_thr )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_BEZIER, "fitCurve nr. pts " + nPts );
    if ( nPts <= 1 ) return 0.0f;
    curves.clear();
    ArrayList<Integer> corners = findCorners( d, nPts, len_thr );
    int i1 = corners.get(0).intValue();
    float err = 0.0f;
    for ( int k=1; k<corners.size(); ++k ) {
      int i2 = corners.get(k).intValue(); // nPts-1
      // TopoDroidLog.Log( TopoDroidLog.LOG_BEZIER, "fitting from " + i1 + " to " + i2 );
      /*  Unit tangent vectors at endpoints */
      BezierPoint tHat1 = computeLeftTangent( d, i1 );
      BezierPoint tHat2 = computeRightTangent( d, i2 );
      float e = fitCubic( d, i1, i2, tHat1, tHat2, error );
      if ( e > err ) err = e;
      i1 = i2;
    }
    return err;
  }

  public ArrayList< BezierCurve > getCurves() { return curves; }

  public int size() { return curves.size(); }

}
