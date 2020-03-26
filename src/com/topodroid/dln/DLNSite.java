/* @file DLNSite.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid Delaunay site (point of the triangulation)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dln;

import com.topodroid.math.Point2D;

public class DLNSite  extends Point2D
{
  private DLNPole mPole;

  public DLNSite( float x, float y )
  {
    super( x, y );
    mPole = new DLNPole( null, null, 0 );
  }

  // p = center of t
  void setPole( Point2D p, DLNTriangle t ) 
  {
    float d = distance( p );
    if ( mPole.mT == null || d > mPole.mDist ) {
      mPole.set( p, t, d );
    }
  }

  float poleDistance() { return mPole.mDist; }
  DLNTriangle poleTriangle() { return mPole.mT; }
  Point2D polePoint() { return mPole.mP; }
  
}
