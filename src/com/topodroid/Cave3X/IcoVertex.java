/** @file IcoVertex.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief vertex of 3D rose direction diagram
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 */
package com.topodroid.Cave3X;

class IcoVertex extends IcoPoint
{
  int mIndex;

  IcoVertex( int k )
  {
    // super();
    mIndex = k;
    k = k%12;
    int a = k % 2;
    int b = (k/2) % 2;
    int g = k/4;
    x = 0;
    y = 2*a - 1;
    z = G*( 2*b - 1 );
    while ( g > 0 ) {
      double t = x; x = y; y = z; z = t;
      --g;
    }
  }


}
  
