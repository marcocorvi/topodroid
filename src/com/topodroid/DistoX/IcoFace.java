/** @file IcoFace.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief face of 3D rose direction diagram
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 */
package com.topodroid.DistoX;

class IcoFace
{
  IcoPoint p1;
  IcoPoint p2;
  IcoPoint p3;

  IcoFace( IcoPoint q1, IcoPoint q2, IcoPoint q3 )
  {
    if ( orientation( q1, q2, q3 ) < 0 ) {
      p1 = q1;
      p2 = q3;
      p3 = q2;
    } else {  
      p1 = q1;
      p2 = q2;
      p3 = q3;
    }
  }

  /*  i 0   1   2           5
   * p1 +---+---+---+---+---+ p2
   *     `+' `+' `+' `+' `+'  j=0
   *     1 `+' `+' `+' `+'  j=1
   *   j   2 `+' `+' `+'  j=2
   *         3 `+' `+'  j=3
   *             `+'  j=4
   *             5  p3
   */
  IcoPoint interpolate( int i, int j, int n )
  {
    if ( i < 0 || j < 0 || i+j > n ) return null; 
    if ( i == 0 ) {
      if ( j == 0 ) return p1;
      if ( j == n ) return p3;
      return IcoPoint.interpolate( p1, p3, j, n );
    } 
    if ( i == n ) return p2;
    if ( i+j == n ) return IcoPoint.interpolate( p2, p3, j, n );
    IcoPoint qi = IcoPoint.interpolate( p1, p2, i+j, n );
    IcoPoint qj = IcoPoint.interpolate( p1, p3, i+j, n );
    return IcoPoint.interpolate( qi, qj, j, n-i );
  }  

  boolean same( IcoPoint q1, IcoPoint q2, IcoPoint q3 )
  { 
    if ( orientation( q1, q2, q3 ) < 0 ) {
      IcoPoint t = q2; q2 = q3; q3 = t;
    }
    return ( p1 == q2 && p2 == q3 && p3 == q1 )
        || ( p1 == q3 && p2 == q1 && p3 == q2 )
        || ( p1 == q1 && p2 == q2 && p3 == q3 );
  }

  private static int orientation( IcoPoint q1, IcoPoint q2, IcoPoint q3 )
  {
    double x0 = q1.y * q2.z - q1.z * q2.y;
    double y0 = q1.z * q2.x - q1.x * q2.z;
    double z0 = q1.x * q2.y - q1.y * q2.x;
    double c = x0 * q3.x + y0 * q3.y + z0 * q3.z;
    return( c > 0.0 )? 1 : ( c < 0.0 )? -1 : 0; 
  }

}


