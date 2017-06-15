/** @file DLNTriangle.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid Delaunay triangle
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


class DLNTriangle
{
  BezierPoint mP0;
  BezierPoint mP1;
  BezierPoint mP2;
  DLNSide mS0; // opposite side of P0, ie, [P1,P2]
  DLNSide mS1;
  DLNSide mS2;
  BezierPoint mCenter;
  float mRadius; // square radius
  float x10, y10, x21, y21, x02, y02;

  DLNTriangle( BezierPoint p0, BezierPoint p1, BezierPoint p2 )
  {
    mP0 = p0;
    mP1 = p1;
    mP2 = p2;
    mS2 = new DLNSide( mP0, mP1, this );
    mS0 = new DLNSide( mP1, mP2, this );
    mS1 = new DLNSide( mP2, mP0, this );
    computeCenterAndRadius();
  }

  boolean hasPoint( BezierPoint p ) 
  {
    return ( p == mP0 ) || ( p == mP1 ) || ( p == mP2 );
  }

  DLNSide side( int k ) 
  {
    if ( k == 0 ) return mS0;
    if ( k == 1 ) return mS1;
    if ( k == 2 ) return mS2;
    return null;
  }

  DLNSide nextSide( DLNSide s )
  {
    if ( s == mS0 ) return mS1;
    if ( s == mS1 ) return mS2;
    if ( s == mS2 ) return mS0;
    return null;
  }

  BezierPoint nextPoint( BezierPoint p ) 
  {
    if ( p == mP0 ) return mP1;
    if ( p == mP1 ) return mP2;
    if ( p == mP2 ) return mP0;
    return null;
  }

  BezierPoint prevPoint( BezierPoint p ) 
  {
    if ( p == mP0 ) return mP2;
    if ( p == mP1 ) return mP0;
    if ( p == mP2 ) return mP1;
    return null;
  }

  DLNSide sideOf( BezierPoint p )
  {
    if ( p == mP0 ) return mS0;
    if ( p == mP1 ) return mS1;
    if ( p == mP2 ) return mS2;
    return null;
  }

  BezierPoint pointOf( DLNSide s ) 
  {
    if ( s == mS0 ) return mP0;
    if ( s == mS1 ) return mP1;
    if ( s == mS2 ) return mP2;
    return null;
  }

  BezierPoint point( int k ) 
  {
    if ( k == 0 ) return mP0;
    if ( k == 1 ) return mP1;
    if ( k == 2 ) return mP2;
    return null;
  }

  // axes are righthanded Y-X
  boolean contains( BezierPoint p )
  {
    if ( y10*(p.mX-mP0.mX) - x10*(p.mY-mP0.mY) < 0 ) return false;
    if ( y21*(p.mX-mP1.mX) - x21*(p.mY-mP1.mY) < 0 ) return false;
    if ( y02*(p.mX-mP2.mX) - x02*(p.mY-mP2.mY) < 0 ) return false;
    return true;
  }
    

  // (x0+x1) + s * (y1-y0) = (x0+x2) - t * (y2-y0)
  // (y0+y1) - s * (x1-x0) = (y0+y2) + t * (x2-x0)
  //
  // (y1-y0) * s + (y2-y0) * t =  (x2-x1)
  // (x1-x0) * s + (x2-x0) * t = -(y2-y1)
  //
  // y10 s + y20 t =   x21
  // x10 s + x20 t = - y21
  void computeCenterAndRadius()
  {
    x10 = mP1.mX - mP0.mX;
    y10 = mP1.mY - mP0.mY;
    float x20 = mP2.mX - mP0.mX;
    float y20 = mP2.mY - mP0.mY;
    x02 = -x20;
    y02 = -y20;
    x21 = mP2.mX - mP1.mX;
    y21 = mP2.mY - mP1.mY;

    float det = y10*x20 - y20*x10;
    float s =   (x20*x21 + y20*y21)/det;
    // float t = - (x10*x21 + y10*y21)/det;
    // assert( fabs(s*y10 + t*y20 - x21) < 0.01 );
    // assert( fabs(s*x10 + t*x20 + y21) < 0.01 );
    // assert( fabs(mP0.mX + mP1.mX + s * ( mP1.mY - mP0.mY ) - (mP0.mX + mP2.mX - t * ( mP2.mY - mP0.mY ))) < 0.01 );
    // assert( fabs(mP0.mY + mP1.mY - s * ( mP1.mX - mP0.mX ) - (mP0.mY + mP2.mY + t * ( mP2.mX - mP0.mX ))) < 0.01 );

    mCenter = new BezierPoint( (mP0.mX + mP1.mX + s * ( mP1.mY - mP0.mY ))/2,
                               (mP0.mY + mP1.mY - s * ( mP1.mX - mP0.mX ))/2 );
    mRadius = mCenter.distance( mP0 );
    // assert( fabs( mRadius - mCenter.distance( mP1 )) < 0.01 );
    // assert( fabs( mRadius - mCenter.distance( mP2 )) < 0.01 );
  }

}


