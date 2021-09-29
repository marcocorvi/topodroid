/** @file WireSegment.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D wire segment
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

public class WireSegment
{
  Cave3DStation wp1;
  Cave3DStation wp2;

  public WireSegment( Cave3DStation p1, Cave3DStation p2 )
  {
    wp1 = p1;
    wp2 = p2;
  }

  boolean coincide( WireSegment s, double eps )
  {
    if ( wp1.coincide( s.wp1, eps ) && wp2.coincide( s.wp2, eps ) ) return true;
    if ( wp1.coincide( s.wp2, eps ) && wp2.coincide( s.wp1, eps ) ) return true;
    return false;
  }

}

