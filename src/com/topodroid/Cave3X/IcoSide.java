/** @file IcoSide.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief side of 3D rose direction diagram
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 */
package com.topodroid.Cave3X;

class IcoSide
{
  IcoPoint p1;
  IcoPoint p2;
  // double alpha;

  IcoSide( IcoPoint q1, IcoPoint q2 )
  {
    p1 = q1;
    p2 = q2;
    // alpha = Math.acos( p1.times(p2) / IcoPoint.R2 );
  }

  // boolean equals( IcoPoint q1, IcoPoint q2 )
  // { 
  //   return p1 == q1 && p2 == q2;
  // }

  // boolean reverse( IcoPoint q1, IcoPoint q2 )
  // { 
  //   return p1 == q2 && p2 == q1;
  // }

  // boolean same( IcoPoint q1, IcoPoint q2 )
  // { 
  //   return ( p1 == q2 && p2 == q1 ) || ( p1 == q1 && p2 == q2 );
  // }

  IcoPoint interpolate( int i, int n ) 
  {
    return IcoPoint.interpolate( p1, p2, i, n );
  }

}
