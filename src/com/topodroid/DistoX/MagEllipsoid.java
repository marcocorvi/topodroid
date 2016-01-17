/* @file MagEllipsoid.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid World Magnetic Model 
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * Implemented after GeomagneticLibrary.c by
 *  National Geophysical Data Center
 *  NOAA EGC/2
 *  325 Broadway
 *  Boulder, CO 80303 USA
 *  Attn: Susan McLean
 *  Phone:  (303) 497-6478
 *  Email:  Susan.McLean@noaa.gov
 */
package com.topodroid.DistoX;

// MAGtype_Ellipsoid;
class MagEllipsoid
{
  double a; /*semi-major axis of the ellipsoid*/
  double b; /*semi-minor axis of the ellipsoid*/
  double fla; /* flattening */
  double epssq; /*first eccentricity squared */
  double eps; /* first eccentricity */
  double re; /* mean radius of  ellipsoid*/

  /* Sets WGS-84 parameters Eq. 8 p. 8 */
  MagEllipsoid()
  {
    a = 6378.137; /*semi-major axis of the ellipsoid [Km] */
    b = 6356.7523142; /*semi-minor axis of the ellipsoid [Km] */
    fla = 1 / 298.257223563; /* flattening */
    eps = Math.sqrt(1 - (b * b) / (a * a)); /*first eccentricity */
    epssq = (eps * eps); /*first eccentricity squared */
    re = 6371.2; /* Earth's radius */
  }

  /* This converts the Cartesian x, y, and z coordinates to Geodetic Coordinates
   *   x is defined as the direction pointing out of the core toward the point defined by 0 degrees latitude and longitude.
   *   y is defined as the direction from the core toward 90 degrees east longitude along the equator
   *   z is defined as the direction from the core out the geographic north pole
   */
  MagGeodetic cartesianToGeodetic( MagVector V )
  {
    MagGeodetic ret = new MagGeodetic();
  
    /* 1.0 compute semi-minor axis and set sign to that of z in order to get sign of Phi correct */
    double modified_b = (V.z < 0.0)? -b : b;
  
    /* 2.0 compute intermediate values for latitude */
    double r= Math.sqrt( V.x*V.x + V.y*V.y );
    double e= ( modified_b*V.z - (a*a - modified_b*modified_b) ) / ( a*r );
    double f= ( modified_b*V.z + (a*a - modified_b*modified_b) ) / ( a*r );
  
    /* 3.0 find solution to: t^4 + 2*E*t^3 + 2*F*t - 1 = 0 */
    double p= (4.0 / 3.0) * (e*f + 1.0);
    double q= 2.0 * (e*e - f*f);
    double d= p*p*p + q*q;
  
    double v = ( d >= 0.0 ) ?
        Math.pow( (Math.sqrt( d ) - q), (1.0 / 3.0) ) - Math.pow( (Math.sqrt( d ) + q), (1.0 / 3.0) )
      : 2.0 * Math.sqrt( -p ) * Math.cos( Math.acos( q/(p * Math.sqrt( -p )) ) / 3.0 );
  
    /* 4.0 improve v NOTE: not really necessary unless point is near pole */
    if ( v*v < Math.abs(p) ) {
      v= -(v*v*v + 2.0*q) / (3.0*p);
    }
    double g = (Math.sqrt( e*e + v ) + e) / 2.0;
    double t = Math.sqrt( g*g  + (f - v*g)/(2.0*g - e) ) - g;
  
    double rlat = Math.atan( (a*(1.0 - t*t)) / (2.0*modified_b*t) );
    ret.phi = MagUtil.RAD2DEG * rlat;
          
    /* 5.0 compute height above ellipsoid */
    ret.HeightAboveEllipsoid = (r - a*t) * Math.cos(rlat) + (V.z - modified_b) * Math.sin(rlat);
  
    /* 6.0 compute longitude east of Greenwich */
    double zlong = Math.atan2( V.y, V.x );
    if ( zlong < 0.0 ) zlong= zlong + 2 * MagUtil.M_PI;
  
    ret.lambda = MagUtil.RAD2DEG * zlong;
    while(ret.lambda > 180) ret.lambda-=360;
    return ret;
  }

  /* This converts spherical coordinates back to geodetic coordinates.  It is not used in the WMM but 
   * may be necessary for some applications, such as geomagnetic coordinates
   */
  MagGeodetic sphericalToGeodetic( MagSpherical spherical )
  {
    MagVector V = spherical.toCartesian( );
    return cartesianToGeodetic( V );
  }

  /** Eq. 7 p. 8 */
  MagSpherical geodeticToSpherical( MagGeodetic geodetic )
  {
    double CosLat = Math.cos( MagUtil.DEG2RAD * geodetic.phi );
    double SinLat = Math.sin( MagUtil.DEG2RAD * geodetic.phi );
    double rc = a / Math.sqrt(1.0 - epssq * SinLat * SinLat);

    double xp = (rc + geodetic.HeightAboveEllipsoid) * CosLat;
    double zp = (rc * (1.0 - epssq) + geodetic.HeightAboveEllipsoid) * SinLat;
    double r = Math.sqrt(xp * xp + zp * zp);
    return new MagSpherical( 
      geodetic.lambda,                        // lambda: longitude 
      MagUtil.RAD2DEG * ( Math.asin(zp / r)), // phig: geocentric latitude 
      r );
  }

}

