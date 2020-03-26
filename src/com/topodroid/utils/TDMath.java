/* @file TDMath.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief math utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import java.lang.Math;

// import android.util.Log;

public class TDMath
{
  static final public float M_PI  = (float)Math.PI;     // 3.1415926536f;
  static final public float M_2PI = (2*M_PI); // 6.283185307f; 
  static final public float M_PI2 = M_PI/2;        // Math.PI/2
  static final public float M_PI4 = M_PI/4;        // Math.PI/4
  static final public float M_PI8 = M_PI/8;        // Math.PI/8
  static final public float RAD2DEG = (180.0f/M_PI);
  static final public float DEG2RAD = (M_PI/180.0f);

  static public float abs( float x )   { return Math.abs(x); }
  static public float cos( float x )   { return (float)Math.cos( x ); }
  static public float cosd( float xd ) { return (float)Math.cos( xd * DEG2RAD ); }
  static public float sin( float x )   { return (float)Math.sin( x ); }
  static public float sind( float xd ) { return (float)Math.sin( xd * DEG2RAD ); }
  static public float atan2( float y, float x ) { return (float)( Math.atan2( y, x ) ); }
  static public float atan2d( float y, float x ) { return (float)( RAD2DEG * Math.atan2( y, x ) ); }
  static public float acos( float x )   { return (float)( Math.acos( x ) ); }
  static public float acosd( float x )  { return (float)( RAD2DEG * Math.acos( x ) ); }
  static public float asind( float x )  { return (float)( RAD2DEG * Math.asin( x ) ); }
  static public float sqrt( float x )   { return (float)Math.sqrt( x ); }

  static public int in360( int f )
  {
    while ( f >= 360 ) f -= 360;
    while ( f < 0 )    f += 360;
    return f;
  }

  static public float in360( float f )
  {
    while ( f >= 360 ) f -= 360;
    while ( f < 0 )    f += 360;
    return f;
  }

  static public double in360( double f )
  {
    while ( f >= 360 ) f -= 360;
    while ( f < 0 )    f += 360;
    return f;
  }

  static public float add90( float a )
  {
    a += 90;
    if ( a >= 360 ) a -= 360;
    return a;
  }

  static public int add180( int a )
  {
    a += 180;
    if ( a >= 360 ) a -= 360;
    return a;
  }

  static public float add180( float a )
  {
    a += 180;
    if ( a >= 360 ) a -= 360;
    return a;
  }

  static public double add180( double a )
  {
    a += 180;
    if ( a >= 360 ) a -= 360;
    return a;
  }

  static public float sub90( float a )
  {
    a -= 90;
    if ( a < 0 ) a += 360;
    return a;
  }


  static public float around( float f, float f0 ) 
  {
    if ( f - f0 > 180 ) return f - 360;
    if ( f0 - f > 180 ) return f + 360;
    return f;
  }

  static public float degree2slope( float deg ) { return (float)(100 * Math.tan( deg * DEG2RAD ) ); }
  static public float slope2degree( float slp ) { return (float)( Math.atan( slp/100 ) * RAD2DEG ); }

  // orthonormalize a 3x3 matrix which is almost orthonormal
  static public boolean orthonormalize3( float[] R, float eps, int max )
  {
    float delta = 0;
    for (int iter = 0; iter < max; ++iter ) {
      delta = 0;
      for (int i=0; i<3; ++i ) { // orthogonalize
        int ii = i * 3;
        int i13 = 3*( (i+1)%3 );
        int i23 = 3*( (i+2)%3 );
        for (int j=0; j<3; ++j ) {
          int j1 = (j+1)%3;
          int j2 = (j+2)%3;
  	  float v = R[i13+j1]*R[i23+j2] - R[i13+j2]*R[i23+j1];
  	  delta += abs( v - R[ii+j] );
  	  R[ii+j] = ( v + R[ii+j] )/2;
        }
      }
      for (int i=0; i<3; ++i ) { // normalize
        int ii = i * 3;
        float s = 0;
        for (int j=0; j<3; ++j ) s += R[ii+j] * R[ii+j];
        s = 1.0f / sqrt(s);
        delta += abs( 1 - s );
        for (int j=0; j<3; ++j ) R[ii+j] *= s;
      }
      if ( delta < eps ) return true;
    }
    return false;
  }

// -------------------------------------------------------
// orthonormalization and ellipsoid fitting are not used

/* ORTHONORMALIZATION
  // orthonormalize a matrix which is almost orthonormal
  // @param DIM matrix size
  static boolean orthonormalize( float[] R, int DIM, float eps, int max )
  {
    float delta = 0;
    float[] V  = new float[ DIM ];
    for (int iter = 0; iter < max; ++iter ) { 
      delta = 0;
      for (int k=0; k<DIM; ++k ) {
        int kk = k*DIM;
        for (int h=0; h<DIM; ++h ) V[h] = 0; // reset V[]
        for (int i=0; i<DIM; ++i ) { // projection of column k on the span of the others
	  if ( i == k ) continue;
          int ii = i * DIM;
	  float s = 0;
          for (int h=0; h<DIM; ++h ) s += R[kk+h] * R[ii+h];
	  for (int h=0; h<DIM; ++h ) V[h] += s * R[ii+h];
	  delta += abs(s);
	}
	for (int h=0; h<DIM; ++h ) { // remove half projection and normalize
	  R[kk+h] -= V[h] / 2;
	}

        float s = 0; // normalize columns
        for (int j=0; j<DIM; ++j ) s += R[kk+j] * R[kk+j];
        s = 1.0f / sqrt(s);
        delta += abs( 1 - s );
        for (int j=0; j<DIM; ++j ) R[kk+j] *= s;
      }

      if ( delta < eps ) return true;
    }
    return false;
  }
*/

/* ELLIPSOID FITTING
  static private float applyAB( float[] A, float[] B, float x, float y, float z )
  {
    float x1 = B[0] + A[0] * x + A[1] * y + A[2] * z;
    float y1 = B[1] + A[3] * x + A[4] * y + A[5] * z;
    float z1 = B[2] + A[6] * x + A[7] * y + A[8] * z;
    return ( x1*x1 + y1*y1 + z1*z1 );
  }

  static private float errorAB( float[] A, float[] B, float[] x, float N )
  {
    float ret = 0;
    for ( int n=0; n<N; ++n ) {
      ret += abs( applyAB( A, B, x[n*3+0], x[n*3+1], x[n*3+2] ) );
    }
    return ret;
  }

  // compute the transform that takes  an ellipsoid to a sphere
  //     X' = A * X + B
  // @param x   XYZ coords of N points (array of size 3*N): x[0] = X  of point 0, x[1] = Y of point 0, etc.
  // @param N   number of points
  // @param eps exit threshold
  // @param max max number of iterations
  // @param A   return A matrix
  // @param B   return B vector
  static boolean ellipsoid( float[] x, int N, float eps, int max, float[] A, float[] B )
  {
    for ( int i=0; i<3; ++i ) B[i] = 0;
    for ( int j=0; j<9; ++j ) A[j] = 0;
    A[0] = A[4] = A[8] = 1;

    float err0 = 0;
    float delta = 0.01f;
    for ( int iter = 0; iter < max; ++iter ) {
      float err;
      // normalize the A*X+B
      float s = 0;
      for ( int n=0; n<N; ++n) {
        s += applyAB( A, B, x[n*3+0], x[n*3+1], x[n*3+2] );
      }
      s = 1.0f / sqrt( s/N );
      for ( int i=0; i<3; ++i ) B[i] *= s;
      for ( int j=0; j<9; ++j ) A[j] *= s;
      err0 = errorAB( A, B, x, N );
      int change = 0;
      for ( int i=0; i<3; ++i ) {
        B[i] += delta;
        err = errorAB( A, B, x, N );
        if ( err > err0 ) {
          B[i] -= 2*delta;
          err = errorAB( A, B, x, N );
          if ( err > err0 ) {
            B[i] += delta;
          } else {
            err0 = err;
            ++ change;
          }
        } else {
          err0 = err;
          ++ change;
        }
      }
      for ( int i=0; i<9; ++i ) {
        A[i] += delta;
        err = errorAB( A, B, x, N );
        if ( err > err0 ) {
          A[i] -= 2*delta;
          err = errorAB( A, B, x, N );
          if ( err > err0 ) {
            A[i] += delta;
          } else {
            err0 = err;
            ++ change;
          }
        } else {
          err0 = err;
          ++ change;
        }
      }
      if ( change < 3 ) delta /= 2;
      if ( err0 < eps ) return true;
    }
    return false; // failed
  }
*/

}
