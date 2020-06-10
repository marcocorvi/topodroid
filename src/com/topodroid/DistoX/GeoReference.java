/* @file GeoReference.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @grief georeference info: coords and E-S scale factors
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

class GeoReference
{
  final float e;
  final float s;
  final float v;
  final double eradius;
  final double sradius;

  GeoReference( float e0, float s0, float v0, double er, double sr )
  {
    e = e0;
    s = s0;
    v = v0;
    eradius = er;
    sradius = sr;
  }
}
