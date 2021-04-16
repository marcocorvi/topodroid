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
  public final double ge;  // data-reduced East value
  public final double gs;  // data-reduced South value
  public final double gv;  // data-reduced Vertical value (upward ?)
  public final double eradius; // NOTE R-radius is not used 
  public final double sradius;
  public final float  declination;

  public GeoReference( double e0, double s0, double v0, double er, double sr, float decl )
  {
    ge = e0;
    gs = s0;
    gv = v0;
    eradius = er;
    sradius = sr;
    declination = decl;
  }

  // DEBUG method
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Geo: E " + ge );
    sb.append( " S " + gs );
    sb.append( " V " + gv );
    sb.append( " d " + declination );
    return sb.toString();
  }
}
