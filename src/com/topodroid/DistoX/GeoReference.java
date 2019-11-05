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
  float e;
  float s;
  float v;
  float eradius;
  float sradius;
  GeoReference( float e0, float s0, float v0, float er, float sr )
  {
    e = e0;
    s = s0;
    v = v0;
    eradius = er;
    sradius = sr;
  }
}
