/** @file Geodetic.java
 *
 * @author marco corvi
 * @date june 2020
 *
 * @brief TopoDroid WGS84 geodetic data and functions
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * ref. T. Soler, L.D. Hothem
 *      Coordinate systems usd in geodesy: basic definitions and concepts, 1988
 */
package com.topodroid.mag;

// import android.util.Log;

public class Geodetic
{
  static public  final double EARTH_A  = 6378137.0;          // a - semimajor axis [meter]
  static public  final double EARTH_B  = 6356752;
  static public  final double EARTH_BA = EARTH_B / EARTH_A; // b/a
  static private final double EARTH_C  = Math.sqrt( EARTH_A * EARTH_A - EARTH_B * EARTH_B );
  static private final double EARTH_E  = EARTH_C / EARTH_A; // e - eccentricity
  static private final double EARTH_E2 = EARTH_E * EARTH_E; // e^2
  static private final double EARTH_1E2 = 1.0 - EARTH_E2;   // (1- e^2)
  static private final double FLATTENING = 298.257223563; // F = 1/( 1 - B/A ), 
  // 2 * F - F*F = ( 1 + B/A )*( 1 - B/A ) 
  //             = (1 - B^2/A^2) 
  //             = C^2 / A^2
  //             = E^2

  // double s = Math.sin( latitude * Math.PI/180 );
  // double W = Math.sqrt( 1 - EARTH_E2 * s * s );
  // RADIUS_WE = EARTH_A / W; // principal radius of curvature in the prime vertical plane
  // RADIUS_NS = EARTH_A * EARTH_1E2 / W;
    
  // -----------------------------------------------------------------------------------
  // approximation

  static private final double EARTH_RADIUS1 = (EARTH_A * Math.PI / 180.0); // semimajor axis [m]
  static private final double EARTH_RADIUS2 = (EARTH_B * Math.PI / 180.0);

  // get the meridian radius times PI/180
  static public double meridianRadiusApprox( double latitude )
  {
    double alat = Math.abs( latitude );
    return ((90 - alat) * EARTH_RADIUS1 + alat * EARTH_RADIUS2)/90;
  }

  // get the parallel radius times PI/180 - horizontal X-Y radius
  static public double parallelRadiusApprox( double latitude )
  {
    double alat = Math.abs( latitude );
    double s_radius = ((90 - alat) * EARTH_RADIUS1 + alat * EARTH_RADIUS2)/90;
    return s_radius * Math.cos( alat * Math.PI / 180 );
  }

   // -----------------------------------------------------------------------------------
   //E. J. KRAKIWSKY, D. B. THOMSON: GEODETIC POSITION COMPUTATIONS
   //https://www2.unb.ca/gge/Pubs/LN39.pdf
  // exact

  // get the meridian radius times PI/180
  static public double meridianRadiusExact( double latitude )
  {
    double alat = Math.abs( latitude );
	double s = Math.sin( alat * Math.PI / 180.0 );
    double W = Math.sqrt( 1 - EARTH_E2 * s * s );
    //RADIUS_WE = EARTH_A / W; // principal radius of curvature in the prime vertical plane (N)
    double RADIUS_NS = EARTH_A * EARTH_1E2 / ( W * W * W ); // ! W3 meridian radius of curvature (M)
    return (RADIUS_NS * Math.PI / 180.0);
  }

  // get the parallel radius times PI/180 - horizontal X-Y radius
  static public double parallelRadiusExact( double latitude )
  {
    double alat = Math.abs( latitude );
	double s = Math.sin( alat * Math.PI / 180.0 );
    double W = Math.sqrt( 1 - EARTH_E2 * s * s );
    double RADIUS_WE = EARTH_A / W; // principal radius of curvature in the prime vertical plane (N)
    //RADIUS_NS = EARTH_A * EARTH_1E2 / ( W * W * W );    
    return (RADIUS_WE * Math.cos( alat * Math.PI / 180 ) * Math.PI / 180.0);
  }
 
  
  // UNUSED -----------------------------------------------------------------------------
  /*
   * The following functions are from
   *   K.M. Borokowski
   *   "Accurate algoritms to transform geocentric to geodetic coordinates" 
   *   Bull. Geod. 63 (1989) 50-56
   * 
   * R = A cos(psi) + H cos(phi)
   * Z = B sin(psi) + H sin(phi)
   *
   * A tan(psi) = B tan(phi)
   * ==> A sin(psi) cos(phi) = B cos(psi) sin(phi)
   *
   * H = ( R - A cos(psi) ) cos(phi) + ( Z - B sin(psi) ) sin(phi) 
   * ==> ( R - A cos(psi) ) sin(phi) = ( Z - B sin(psi) ) cos(phi)
   * ==> ( R - A cos(psi) ) A sin(psi) =  ( Z - B sin(psi) ) B cos(psi) 
   * ==> R A sin(psi) - Z B cos(psi) = ( A^2 - B^2 ) sin(psi) cos(psi)
   *                                 = C^2 sin(psi) cos(psi)
   * let tan(w) = (B Z)/(A R)
   * ==> 2 sin*( psi - w ) = K sin(2 psi)
   * where K = C^2 /sqrt( (AR)^2 + (BZ)^2 )
   */
  /*
  double mLat; // phi latitude [deg]
  double mLng; // longitude [deg]
  double mH;   // height [m]
  double mPsi; // psi = atan( (b/a) tg( mLat ) )
  double mN;   // grand normal [m] : N = A / sqrt( 1 - E^2 sin(lat)^2 )

  private double mLatRad;
  private double mPsiRad;
  private double mSinLat;
  private double mCosLat;
  private double mR;
  private double mZ;
  */

  /** cstr
   * @param lat   latitude [deg]
   * @param lng   longitude [deg] 
   * @param h     height (over ellipsoid) [m] 
  public Geodetic( double lat, double lng, double h )
  {
    mLat = lat;
    mLng = lng;
    mH   = h;

    mLatRad = lat * Math.PI / 180.0;
    mSinLat = Math.sin( mLatRad );
    mCosLat = Math.cos( mLatRad );
    mPsiRad = Math.atan( EARTH_BA * Math.tan( mLatRad ) );
    mPsi    = mPsiRad * 180.0 / Math.PI;

    mN = EARTH_A / Math.sqrt( 1  - EARTH_E2 * mSinLat * mSinLat );
    mR = EARTH_A * Math.cos( mPsiRad ) + mH * mCosLat;
    mZ = EARTH_B * Math.sin( mPsiRad ) + mH * mSinLat;
  }
  */

  /** get geodetic cords of a nearby point
   * @param dn   north displacement [m]
   * @param de   east displacement [m]
   * @param dh   vertical displacement [m]
   * @return geodetic cords of the displaced point
  public Geodetic getGeodetic( double dn, double de, double dh )
  {
    double dz = dh * mSinLat + dn * mCosLat;
    double dx = dh * mCosLat - dn * mSinLat;
    double r1 = Math.sqrt( (mR+dx) * (mR+dx) + de * de );
    double z1 = mZ + dz;

    double lng1 = mLng + Math.atan( de / r1 ) * 180.0 / Math.PI;
    double lat1 = geodeticLat( r1, z1 );
    double h1   = geodeticHeight( r1, z1, lat1 );
    return new Geodetic( lat1, lng1, h1 );
  }
  */

  /** compute geocentric R value: R = ( N + h ) * cos( lat )
   * @param phi latitude [deg]
   * @param h   height [m]
  public static double geocentricR( double phi, double h ) 
  { 
    phi = phi * Math.PI/180.0;
    double psi = Math.atan( EARTH_BA * Math.tan( phi ) );
    return EARTH_A * Math.cos(psi) + h * Math.cos(phi); 
  }
  */

  /** compute geocentric Z value: Z = ( N (1-e^2) + h ) * sin( lat )
   * @param phi latitude [deg]
   * @param h   height [m]
  public static double geocentricZ( double phi, double h ) 
  { 
    phi = phi * Math.PI/180.0;
    double psi = Math.atan( EARTH_BA * Math.tan( phi ) );
    return EARTH_A * Math.sin(psi) + h * Math.sin(phi);
  }
  */

  /** compute the geodetic latitude [degrees]
   * @param r    horizontal (X-Y) radius [m]
   * @param z    vertical [m]
  public static double geodeticLat( double r, double z )
  {
    if ( r <= 0 ) return 0;
    // A * ( 1 - (1-B/A) ) = A * B/A = B
    double b = EARTH_B * ( ( z < 0 )? -1 : 1 );
    double e = ( ( z + b ) * b / EARTH_A - EARTH_A ) / r;
    double f = ( ( z - b ) * b / EARTH_A + EARTH_A ) / r;
    double p = ( 1 + e * f ) * 4.0 / 3.0;
    double q = 2 * ( e*e - f*f );
    double d = p*p*p  + q*q;
    double v = 0;
    if ( d > 0 ) {
      double s = Math.sqrt( d ) + q;
      if ( s > 0 ) {
        s = Math.exp( Math.log(s) / 3 );
      } else {
        s = - Math.exp( Math.log(-s) / 3 );
      }
      v = p / s - s;
      v = - ( 2 * q + v*v*v ) / ( 3 * p );
    } else {
      double p1 = Math.sqrt( -p );
      v = 2 * p1 * Math.cos( Math.acos( q/(p*p1) )/3 );
    }
    double g = 0.5 * ( e + Math.sqrt( e*e + v ) );
    double t = Math.sqrt( g*g + (f - v*g)/(2*g - e) ) - g;
    double phi = Math.atan( (1 - t*t)*EARTH_A / (2*b*t) );
    // double h = ( r - a * t) * Math.cos( phi ) + ( z - b ) * Math.sin( phi );
    return phi * 180.0 / Math.PI;
  }
  */
 
  /** compute the geodetic height [m]
   * @param r    horizontal (X-Y) radius [m]
   * @param z    vertical [m]
   * @param phi  latitude [deg] - must have been obtained using geodeticLat()
   * 
   * @note t^2 + 2 * b/a * tan(phi) * t - 1 = 0
  public static double geodeticHeight( double r, double z, double phi )
  {
    phi = phi * Math.PI / 180;
    double B = Math.tan( phi ) * EARTH_BA; // b/a * tan(phi)
    double t = - B + Math.sqrt( 1 + B * B );
    return ( r - EARTH_A * t) * Math.cos( phi ) + ( z - EARTH_B ) * Math.sin( phi );
  }
  */
}
