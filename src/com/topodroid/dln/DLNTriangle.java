/* @file DLNTriangle.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid Delaunay triangle
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dln;

import com.topodroid.math.Point2D;

public class DLNTriangle
{
  private Point2D mP0;
  private Point2D mP1;
  private Point2D mP2;
  private DLNSide mS0; // opposite side of P0, ie, [P1,P2]
  private DLNSide mS1;
  private DLNSide mS2;
  private float x10, y10, x21, y21, x02, y02;

  public Point2D mCenter;
  public float mRadius; // square radius

  DLNTriangle( Point2D p0, Point2D p1, Point2D p2 )
  {
    mP0 = p0;
    mP1 = p1;
    mP2 = p2;
    mS2 = new DLNSide( mP0, mP1, this );
    mS0 = new DLNSide( mP1, mP2, this );
    mS1 = new DLNSide( mP2, mP0, this );
    computeCenterAndRadius();
  }

  public boolean hasPoint( Point2D p ) 
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

  Point2D nextPoint( Point2D p ) 
  {
    if ( p == mP0 ) return mP1;
    if ( p == mP1 ) return mP2;
    if ( p == mP2 ) return mP0;
    return null;
  }

  Point2D prevPoint( Point2D p ) 
  {
    if ( p == mP0 ) return mP2;
    if ( p == mP1 ) return mP0;
    if ( p == mP2 ) return mP1;
    return null;
  }

  DLNSide sideOf( Point2D p )
  {
    if ( p == mP0 ) return mS0;
    if ( p == mP1 ) return mS1;
    if ( p == mP2 ) return mS2;
    return null;
  }

  Point2D pointOf( DLNSide s ) 
  {
    if ( s == mS0 ) return mP0;
    if ( s == mS1 ) return mP1;
    if ( s == mS2 ) return mP2;
    return null;
  }

  Point2D point( int k ) 
  {
    if ( k == 0 ) return mP0;
    if ( k == 1 ) return mP1;
    if ( k == 2 ) return mP2;
    return null;
  }

  // axes are righthanded Y-X
  boolean contains( Point2D p )
  {
    if ( y10*(p.x-mP0.x) - x10*(p.y-mP0.y) < 0 ) return false;
    if ( y21*(p.x-mP1.x) - x21*(p.y-mP1.y) < 0 ) return false;
    if ( y02*(p.x-mP2.x) - x02*(p.y-mP2.y) < 0 ) return false;
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
  private void computeCenterAndRadius()
  {
    x10 = mP1.x - mP0.x;
    y10 = mP1.y - mP0.y;
    float x20 = mP2.x - mP0.x;
    float y20 = mP2.y - mP0.y;
    x02 = -x20;
    y02 = -y20;
    x21 = mP2.x - mP1.x;
    y21 = mP2.y - mP1.y;

    float det = y10*x20 - y20*x10;
    float s =   (x20*x21 + y20*y21)/det;
    // float t = - (x10*x21 + y10*y21)/det;
    // assert( fabs(s*y10 + t*y20 - x21) < 0.01 );
    // assert( fabs(s*x10 + t*x20 + y21) < 0.01 );
    // assert( fabs(mP0.x + mP1.x + s * ( mP1.y - mP0.y ) - (mP0.x + mP2.x - t * ( mP2.y - mP0.y ))) < 0.01 );
    // assert( fabs(mP0.y + mP1.y - s * ( mP1.x - mP0.x ) - (mP0.y + mP2.y + t * ( mP2.x - mP0.x ))) < 0.01 );

    mCenter = new Point2D( (mP0.x + mP1.x + s * ( mP1.y - mP0.y ))/2,
                               (mP0.y + mP1.y - s * ( mP1.x - mP0.x ))/2 );
    mRadius = mCenter.distance( mP0 );
    // assert( fabs( mRadius - mCenter.distance( mP1 )) < 0.01 );
    // assert( fabs( mRadius - mCenter.distance( mP2 )) < 0.01 );
  }

}


