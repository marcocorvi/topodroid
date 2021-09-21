/* @file HalfSide.java
 *
 * @author marco corvi
 * @date may 2021
 *
 * @brief bubble algo half-side
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.bubble;

// half-side of the border
//          new_point
//    ---> p1 ---> p2 --->
//          |  tr  |
//           `    '
// therefore tr has the points in order p2--p1
public class HalfSide
{
  Point3S p1;
  Point3S p2;
  Triangle3S tr;

  HalfSide( Point3S pp1, Point3S pp2, Triangle3S tri )
  {
    p1 = pp1;
    p2 = pp2;
    tr = tri;
    // assert( tr.getSide( p2, p1 ) > 0 );
  }

}
