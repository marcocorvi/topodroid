/** @file DLNSide.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid Delaunay Traingle side
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


class DLNSide
{
  DLNTriangle triangle;
  DLNSide other; // nearby triangle side

  Point2D mP1;
  Point2D mP2;

  DLNSide( Point2D p1, Point2D p2, DLNTriangle tri )
  {
    triangle = tri;
    other = null;
    mP1 = p1;
    mP2 = p2;
  }

  static void pairSides( DLNSide s1, DLNSide s2 )
  {
    if ( s1 == null ) return;
    if ( s2 == null ) return;
    // assert( s1.mP1 == s2.mP2 );
    // assert( s1.mP2 == s2.mP1 );
    s1.other = s2;
    s2.other = s1;
  }

  void setTriangle( DLNTriangle tri ) { triangle = tri; }

}


