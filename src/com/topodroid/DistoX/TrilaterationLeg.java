/* @file TrilaterationLeg.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid centerline computation: cluster of triangles
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class TrilaterationLeg
{
  TriShot shot;
  double d; // distance [m]
  double a; // angle [degrees]
  boolean used; // work flag
  TrilaterationPoint pi;
  TrilaterationPoint pj;
  
  TrilaterationLeg( TriShot sh, TrilaterationPoint p1, TrilaterationPoint p2 )
  {
    shot = sh;
    d = sh.length() * Math.cos( sh.clino() * Math.PI / 180.0 );
    a = sh.bearing();
    used = false;
    pi = p1;
    pj = p2;
  }
}
