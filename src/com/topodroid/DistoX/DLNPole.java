/** @file DLNPole.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid Delaunay site pole
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


class DLNPole 
{
  BezierPoint mP;
  DLNTriangle mT;
  float mDist;

  DLNPole( BezierPoint p, DLNTriangle t, float d )
  {
    mP = p;
    mT = t;
    mDist = d;
  }

  void set ( BezierPoint p, DLNTriangle t, float d )
  {
    mP = p;
    mT = t;
    mDist = d;
  }
}
