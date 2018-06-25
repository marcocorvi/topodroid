/* @file TriLeg.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid centerline computation: cluster of triangles
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class TriLeg
{
  TriShot shot;
  double d; // distance [m]
  double a; // angle [degrees]
  boolean used; // work flag
  TriPoint pi;
  TriPoint pj;
  
  TriLeg( TriShot sh, TriPoint p1, TriPoint p2 )
  {
    shot = sh;
    d = sh.length() * Math.cos( sh.clino() * Math.PI / 180.0 );
    a = sh.bearing();
    used = false;
    pi = p1;
    pj = p2;
  }
}
