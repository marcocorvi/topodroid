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

public class GeoReference
{
  final float e;  // data-reduced East value
  final float s;  // data-reduced South value
  final float v;  // data-reduced Vertical value (upward ?)
  final double eradius; // NOTE R-radius is not used 
  final double sradius;
  final float  declination;

  public GeoReference( float e0, float s0, float v0, double er, double sr, float decl )
  {
    e = e0;
    s = s0;
    v = v0;
    eradius = er;
    sradius = sr;
    declination = decl;
  }

  // DEBUG method
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Geo: E " + e );
    sb.append( " S " + s );
    sb.append( " V " + v );
    sb.append( " d " + declination );
    return sb.toString();
  }
}
