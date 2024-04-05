/** @file Geodetic.java
 *
 * @author marco corvi
 * @date june 2020
 *
 * @brief TopoDroid WGS84 geodetic data and functions
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * ref. T. Soler, L.D. Hothem
 *      Coordinate systems usd in geodesy: basic definitions and concepts, 1988
 */
package com.topodroid.mag;

public class Geodetic
{
  static public  final double EARTH_A  = 6378137.0;          // a - semi-major axis [meter]
  static public  final double EARTH_B  = 6356752.314245;
  static public  final double EARTH_BA = EARTH_B / EARTH_A; // b/a
  static private final double EARTH_C  = Math.sqrt( EARTH_A * EARTH_A - EARTH_B * EARTH_B );
  static private final double EARTH_E  = EARTH_C / EARTH_A; // e - eccentricity
  static private final double EARTH_E2 = EARTH_E * EARTH_E; // e^2
  static private final double EARTH_1E2 = 1.0 - EARTH_E2;   // (1- e^2)
  static private final double FLATTENING = 298.257222101; // F = 1/( 1 - B/A ), 
  static private final double F = 1/FLATTENING; // (A - B) / A
  static private final double EARTH_A2 = EARTH_A * EARTH_A;
  static private final double EARTH_B2 = EARTH_B * EARTH_B;
  // 2 * F - F*F = ( 1 + B/A )*( 1 - B/A ) 
  //             = (1 - B^2/A^2) 
  //             = C^2 / A^2
  //             = E^2

  // double s = Math.sin( latitude * Math.PI/180 );
  // double W = Math.sqrt( 1 - EARTH_E2 * s * s );
  // RADIUS_WE = EARTH_A / W; // principal radius of curvature in the prime vertical plane
  // RADIUS_NS = EARTH_A * EARTH_1E2 / W;
    
  // -----------------------------------------------------------------------------------
  /* approximation

  static private final double EARTH_RADIUS1 = (EARTH_A * Math.PI / 180.0); // semi-major axis [m]
  static private final double EARTH_RADIUS2 = (EARTH_B * Math.PI / 180.0);

  // get the meridian radius times PI/180
  static public double meridianRadiusApprox( double latitude )
  {
    double a_lat = Math.abs( latitude );
    return ((90 - a_lat) * EARTH_RADIUS1 + a_lat * EARTH_RADIUS2)/90;
  }

  // get the parallel radius times PI/180 - horizontal X-Y radius
  static public double parallelRadiusApprox( double latitude )
  {
    double a_lat = Math.abs( latitude );
    double s_radius = ((90 - a_lat) * EARTH_RADIUS1 + a_lat * EARTH_RADIUS2)/90;
    return s_radius * Math.cos( a_lat * Math.PI / 180 );
  }
  */

  // -----------------------------------------------------------------------------------
  // E. J. KRAKIWSKY, D. B. THOMSON: GEODETIC POSITION COMPUTATIONS
  // https://www2.unb.ca/gge/Pubs/LN39.pdf
  // exact

  /** @return the meridian radius times PI/180
   * @param latitude   latitude [deg]
   * @param h_ellip    ellipsoidic altitude [m]
   */
  static public double meridianRadiusExact( double latitude, double h_ellip )
  {
    double a_lat = Math.abs( latitude ) * Math.PI / 180.0; // radians
    double s = Math.sin( a_lat );
    double W = Math.sqrt( 1 - EARTH_E2 * s * s );
    // RADIUS_WE = EARTH_A / W; // principal radius of curvature in the prime vertical plane (N)
    //double RADIUS_NS = EARTH_A * EARTH_1E2 / ( W * W * W ); // ! W3 meridian radius of curvature (M)
    double RADIUS_NS = (EARTH_A * EARTH_1E2 / ( W * W * W )) + h_ellip; //ellipsoidic altitude
    return (RADIUS_NS * Math.PI / 180.0);
  }

  /** @return get the parallel radius times PI/180 - horizontal X-Y radius
   * @param latitude   latitude [deg]
   * @param h_ellip    ellipsoidic altitude [m]
   */
  static public double parallelRadiusExact( double latitude, double h_ellip )
  {
    double a_lat = Math.abs( latitude ) * Math.PI / 180.0; // radian
    double s = Math.sin( a_lat );
    double W = Math.sqrt( 1 - EARTH_E2 * s * s );
    //double RADIUS_WE = EARTH_A / W; // principal radius of curvature in the prime vertical plane (N) 
    double RADIUS_WE = (EARTH_A / W) + h_ellip; // ellipsoid altitude
    // RADIUS_NS = EARTH_A * EARTH_1E2 / ( W * W * W );    
    return (RADIUS_WE * Math.cos( a_lat ) * Math.PI / 180.0);
  }

  /** @return approximate WGS84 meridian convergence factor
   * @param latitude of the reference point [deg]
   * the approximate meridian convergence of a point at distance D from the reference point
   * and azimuth A is
   *     D sin(A) * meridianConvergence( ref_latitude )
   *
   * The correction to DE = D sin(A) approx., at DN is DN DE * tan(lat) / N 
   * where N is the principal radius.
   * Therefore the correction to DE is a multiplicative factor (1 + DN tan(lat) / N ).
   *     neg.correction       pos. correction
   *                     |
   *     DE<0 DN>0   *   |   *   DE>0 DN>0
   *     tan(lat)>0 /    |    \  tan(lat)>0
   *     ----------------+-----------------
   *     DE<0 DN<0  \    |    /  DE>0 DN<0
   *     tan(lat)<0  *   |   *   tan(lat)<0
   */
  static public double meridianConvergenceFactor( double latitude )
  {
    double s = Math.sin( latitude * Math.PI / 180.0 );
    double c = Math.cos( latitude * Math.PI / 180.0 );
    double W = Math.sqrt( 1 - EARTH_E2 * s * s );
    return s/c * (W / EARTH_A);
  }

  
  // UNUSED -----------------------------------------------------------------------------
  /*
   * The following functions are from
   *   K.M. Borokowski
   *   "Accurate algorithms to transform geocentric to geodetic coordinates"
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
  double mH;   // h_ellip [m]
  double mPsi; // psi = atan( (b/a) tg( mLat ) )
  double mN;   // grand normal [m] : N = A / sqrt( 1 - E^2 sin(lat)^2 )

  private double mLatRad;
  private double mPsiRad;
  private double mSinLat;
  private double mCosLat;
  private double mR;
  private double mZ;
  */

  /* cstr
   * @param lat   latitude [deg]
   * @param lng   longitude [deg] 
   * @param h     h_ellip (over ellipsoid) [m] 
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

  /* get geodetic cords of a nearby point
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

  /* compute geocentric R value: R = ( N + h ) * cos( lat )
   * @param phi latitude [deg]
   * @param h   h_ellip [m]
  public static double geocentricR( double phi, double h ) 
  { 
    phi = phi * Math.PI/180.0;
    double psi = Math.atan( EARTH_BA * Math.tan( phi ) );
    return EARTH_A * Math.cos(psi) + h * Math.cos(phi); 
  }
  */

  /* compute geocentric Z value: Z = ( N (1-e^2) + h ) * sin( lat )
   * @param phi latitude [deg]
   * @param h   h_ellip [m]
  public static double geocentricZ( double phi, double h ) 
  { 
    phi = phi * Math.PI/180.0;
    double psi = Math.atan( EARTH_BA * Math.tan( phi ) );
    return EARTH_A * Math.sin(psi) + h * Math.sin(phi);
  }
  */

  /* compute the geodetic latitude [degrees]
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
 
  /* compute the geodetic h_ellip [m]
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

  /* 1975 T. Vincenty Direct and inverse of geodesics on the ellipsoid with application of nested equations

     @see https://www.movable-type.co.uk/scripts/latlong-vincenty.html


  */

  public static double equatorialDistance( double lat ) // , double lng )
  {
    lat *= Math.PI / 180.0;
    // lng *= Math.PI / 180.0;
    double L0 = 0;   // difference in longitude ( lng1 == lng2 )
    double tU1 = 0; // equator lat1 = 0 // only for generalizations
    double cU1 = 1;
    double sU1 = 0;
    double tU2 = (1 - F) * Math.tan( lat ); // reduced latitude_2
    double cU2 = 1.0 / Math.sqrt( 1 + tU2 * tU2 );
    double sU2 = tU2 * cU2;

    double S;
    double sS, cS;
    double sL, cL;
    double sA, cA2; // Aeq = azimuth of the geodesic at the equator
    double c2Sm; // Sm angular distance on the sphere from the equator to the midpoint of the line

    double L1;
    double L = L0; // difference in longitude on an auxiliary sphere
    do {
      L1 = L;
      sL = Math.sin( L );
      cL = Math.cos( L );
      double x1 = cU2 * sL;
      double x2 = cU1 * sU2 - sU1 * cU2 * cL;
      sS  = Math.sqrt( x1 * x1 + x2 * x2 );
      cS  = sU1 * sU2 + cU1 * cU2 * cL;
      S   = Math.atan2( sS, cS );
      sA  = cU1 * cU2 * sL / sS;
      cA2 = 1 - sA * sA;
      c2Sm = cS - 2 * sU1 * sU2 / cA2;
      double C = F/16 * cA2 * ( 4 + F * ( 4 - 3 * cA2 ) );
      L  = L0 + (1-C) * F * sA * ( S + C * sS * ( c2Sm + C * cS * ( -1 + 2 * c2Sm * c2Sm ) ) );
    } while ( Math.abs( L - L1 ) > 1e-10 );
    double u2 = cA2 * ( EARTH_A2 - EARTH_B2 ) / EARTH_B2;
    double A  = 1 + u2/16384 * ( 4096 + u2 * ( -768 + u2 * (320 - 175 * u2) ) );
    double B  =     u2/1024  * (  256 + u2 * ( -128 + u2 * ( 74 -  47 * u2) ) );
    double DS = B * sS * (c2Sm + B/4 * ( cS * (-1 + 2*c2Sm*c2Sm) - B/6 * c2Sm * (-3 + 4*sS*sS)*(-3 + 4*c2Sm*c2Sm)));
    double s  = EARTH_B * A * ( S - DS ); // length of geodetic
    // double a1 = Math.atan2( cU2 * sL,   cU1 * sU2 - sU1 * cU2 * cL ); // initial azimuth
    // double a2 = Math.atan2( cU1 * sL, - sU1 * cU2 + cU1 * sU2 * cL ); // final azimuth
    return s;
  }

  /** @return the WGS84 latitude [degrees] from the equatorial distance {m}
   * @param dist  equatorial distance [m]
   * 
   * tan(U1) = (1-f) tan(phi1) // reduced latitude
   * sigma1 = atan( tan(U1) / cos(a1) )   // a1 first point bearing
   * sin(a) = cos(U1) sin(a1)
   * u^2 = cos^2(a) * (a^2-b^2)/b^2
   * ...
   *  phi2 = Math.atan( sin U1 · cos σ + cos U1 · sin σ · cos α1 / (1−f) · √sin² α + (sin U1 · sin σ − cos U1 · cos σ · cos α1)² )
   *                      0                1                0                           0                1                0
   */
  public static double equatorialLatitude( double dist )
  {

    // double phi1 = 0; // radians
    // double tU1 = 0; // (1-F)) * Math.tan( phi1 ); // U1 reduced latitude of P1
    double cU1 = 1; // cos(U1)
    double sU1 = 0; // sin(U1)
    double ca1 = 1; // Math.cos( a1 ); // a1 = azimuth at P1
    // double sa1 = 0;
    double sigma1 = 0; // Math.atan2( tU1, ca1 ); // angular distance on the sphere from equator to P1 

    double sa  = 0; // cU1 * sa1;   // sin(a), a = azimuth on the equator
    double c2a = 1; // 1 - sa * sa; // cos^2( a )
    double u2  = c2a * (EARTH_A2 - EARTH_B2 ) / EARTH_B2; // cos^2(a) * (a^2-b^2)/b^2
    double A = 1 + u2/16384 * (4096 + u2 * (-768 + u2 * (320 -175 * u2)));
    double B =     u2/ 1024 * ( 256 + u2 * (-128 + u2 * ( 74 - 47 * u2)));
    double sigma0 = dist/( EARTH_B * A );
    double sigma  = sigma0;
    double ss, cs;
    double sigma2;
    do {
      double c2sm = Math.cos( 2 * sigma1 + sigma );
      double c2sm2 = c2sm * c2sm;
      ss = Math.sin(sigma);
      cs = Math.cos(sigma);
      double dsigma = B * ss * ( c2sm + B/4 * ( cs * (-1 + 2 * c2sm2) - B/6 * c2sm*( -3 + 4 * ss * ss ) * (-3 + 4 * c2sm2) ) );
      sigma2 = sigma;
      sigma = sigma0 + dsigma;
      System.out.println("Dsigma " + dsigma );
    } while ( Math.abs( sigma - sigma2 ) > 1e-10 );
    double x = sU1 * ss - cU1 * cs * ca1;
    x = Math.sqrt( sa * sa + x * x );
    double phi2 = Math.atan2( sU1 * cs + cU1 * ss * ca1, (1-F) * x );
    // double lambda = Math.atan2( ss * sa1, cU1 * cs - sU1 * ss * ca1 );
    // double C = f/16 * c2a * ( 4 + f *( 4 - 3 * c2a ) );
    // double L = lambda - (1-C) * f * sa * (sigma + C * ss * (c2sm + C * cs * (-1+ 2 * c2sm2) ) );
    // double lambda2 = lambda1 + L;
    // double a2 = Math.atan2( ss, -x );
    return phi2 * 180 / Math.PI;
  }

  /** @return meridian radius on the ellipsoid (multiplied by pi/180)
   * @param lat   latitude [deg]
   * @param h_ell ellipsoidic altitude [m]
   */
  public static double meridianRadiusEllipsoid( double lat, double h_ell ) 
  {
    return meridianRadiusExact( lat, 0 );
  }

  /** @return parallel radius on the ellipsoid (multiplied by pi/180)
   * @param lat   latitude [deg]
   * @param h_ell ellipsoidic altitude [m]
   */
  public static double parallelRadiusEllipsoid( double lat, double h_ell )
  {
    // FIXME
    // return parallelRadiusExact( lat, 0 );
    double tl = Math.tan( lat * Math.PI / 180.0 ) * EARTH_B / EARTH_A;
    return EARTH_A / Math.sqrt( 1 + tl * tl ) * Math.PI / 180;
  }

  // TEST -----------------------
  // static public void main( String[] args )
  // {
  //   double lng = Double.parseDouble( args[0] );
  //   double lat = Double.parseDouble( args[1] );
  //   System.out.println("Longitude " + lng + " latitude " + lat );
  //   double d = Geodetic.equatorialDistance( lat );
  //   System.out.println("Distance " + d );
  //   double l = Geodetic.equatorialLatitude( d );
  //   System.out.println("Latitude " + l );
  // }

}
