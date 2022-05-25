/** @file WireSegment.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D wire segment
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.TDX;

/** 3D segment between two 3D stations
 */
public class WireSegment
{
  Cave3DStation wp1;
  Cave3DStation wp2;

  /** cstr
   * @param p1   first 3D station
   * @param p2   second 3D station
   */
  public WireSegment( Cave3DStation p1, Cave3DStation p2 )
  {
    wp1 = p1;
    wp2 = p2;
  }

  /** @return true if this segment coincide with the given segment withing epsilon tolerance
   * @param s   given segment
   * @param eps epsilon tolerance
   */
  boolean coincide( WireSegment s, double eps )
  {
    // if ( wp1.coincide( s.wp1, eps ) && wp2.coincide( s.wp2, eps ) ) return true;
    // if ( wp1.coincide( s.wp2, eps ) && wp2.coincide( s.wp1, eps ) ) return true;
    // return false;
    return ( wp1.coincide( s.wp1, eps ) && wp2.coincide( s.wp2, eps ) ) || ( wp1.coincide( s.wp2, eps ) && wp2.coincide( s.wp1, eps ) );
  }

}

