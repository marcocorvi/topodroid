/* @file FixedInfo.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid fixed stations (GPS-localized stations)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import androidx.annotation.RecentlyNonNull;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.prefs.TDSetting;

import com.topodroid.mag.MagLatLong;

import java.util.Locale;

/** fixed (GPS) point
 * Note the order of data: LONGITUDE - LATITUDE - ALTITUDE
 */
public class FixedInfo extends MagLatLong
{
  public final static long SRC_UNKNOWN      = 0L;
  public final static long SRC_TOPODROID    = 1L;
  public final static long SRC_MANUAL       = 2L;
  public final static long SRC_MOBILE_TOP   = 3L;
  public final static long SRC_GPX_RECORDER = 4L;
  public final static long SRC_GPS_POSITION = 5L;
  public final static long SRC_GPS_TEST     = 6L;
  public final static long SRC_GPS_LOGGER   = 7L;
  public final static long SRC_GPS_POINT    = 8L;

  long   id;       // fixed id
  long   source;   // 0: unknown,  1: topodroid,  2: manual,   3: mobile-topographer, ...
  public String name;     // station name, or whatever
  // public double lat;      // wgs84 latitude [decimal deg] (from MagLatLong)
  // public double lng;      // wgs84 longitude [decimal deg]
  public double h_ell;      // wgs84 altitude [m] (only internal use)
  public double h_geo;      // geoid altitude [m] 
  public String comment;
  public String cs;       // coordinate system
  public double cs_lng;   // longitude / east
  public double cs_lat;   // latitude / north
  public double cs_h_geo;   // altitude (geoid)
  long   cs_n_dec; // number of decimals in lng/lat
  double convergence = 0.0; // cs meridian convergence [degree]
  double accuracy    = 0.0; // horizontal [m]
  double accuracy_v  = 0.0;
  double mToUnits    = 1.0; // meters to units
  double mToVUnits   = 1.0; // meters to vert units

  public FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid, String cmt, long src, double accur, double accur_v )
  {
    id = _id;
    name = n;
    lat = latitude;
    lng = longitude;
    h_ell = h_ellip;
    h_geo = h_geoid;
    comment = cmt;
    source  = src;
    accuracy   = accur;
    accuracy_v = accur_v;
    clearConverted();
  }

  /** cstr
   * @param _id    database ID
   * @param n      station
   * @param longitude longitude
   * @param latitude  latitude
   * @param h_ellip   ellipsoid altitude
   * @param h_geoid   geoid altitude
   * @param cmt       comment
   * @param src       source
   * @param name_cs   CS name
   * @param lng_cs    CS longitude - east [m]
   * @param lat_cs    CS latitude - north [m]
   * @param h_geo_cs  CS altitude (geoid) [m]
   * @param n_dec     number of decimals
   * @param conv      convergence
   */
  FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid,
             String cmt, long src, String name_cs, double lng_cs, double lat_cs, double h_geo_cs, long n_dec, double conv, double accur, double accur_v,
             double m_to_units, double m_to_vunits )
  {
    id = _id;
    name = n;
    lat = latitude;
    lng = longitude;
    h_ell = h_ellip;
    h_geo = h_geoid;
    comment = cmt;
    source  = src;
    cs      = name_cs;
    cs_lng  = lng_cs;
    cs_lat  = lat_cs;
    cs_h_geo  = h_geo_cs;
    cs_n_dec = (n_dec >= 0)? n_dec : 0;
    convergence = conv;
    accuracy   = accur;
    accuracy_v = accur_v;
    mToUnits   = m_to_units;
    mToVUnits  = m_to_vunits;
  }

  /** set converted coordinates
   * @param name_cs   coordinate system
   * @param lng_cs    longitude / east [m]
   * @param lat_cs    latitude / north [m]
   * @param h_geo_cs  altitude (geoid) [m]
   * @param n_dec     number of decimals in lng/lat
   * @param conv      convergence [degree]
   * @param m_to_units  meters to units
   * @param m_to_vunits meters to vert units
   */
  void setCSCoords( String name_cs, double lng_cs, double lat_cs, double h_geo_cs, long n_dec, double conv, double m_to_units, double m_to_vunits )
  {
    cs = name_cs;
    if ( cs != null && cs.length() > 0 ) {
      cs_lng = lng_cs;  // store values in meters FIXME M_TO_UNITS
      cs_lat = lat_cs;
      cs_h_geo = h_geo_cs;
      cs_n_dec = (n_dec >= 0)? n_dec : 0;
      convergence = conv;
      mToUnits  = m_to_units;
      mToVUnits = m_to_vunits;
    }
  }

  /** reset converted coordinates
   */
  void clearConverted()
  {
    cs = null;
    cs_lng = 0;
    cs_lat = 0;
    cs_h_geo = 0;
    cs_n_dec = 2L;
    convergence = 0.0;
    mToUnits  = 1.0;
    mToVUnits = 1.0;
  }

  /** test if this fixed has converted coordinates
   * @return true if converted coord system is not null
   */
  boolean hasCSCoords() { return ( cs != null && cs.length() > 0 ); }

  // public FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid )
  // {
  //   id = _id;
  //   name = n;
  //   lat = latitude;
  //   lng = longitude;
  //   h_ell = h_ellip;
  //   h_geo = h_geoid;
  //   comment = "";
  // }

  /** @return the string "station long lat h_geo" for the exports
   * if the fixed point has accuracies they are appended to the string
   */
  String toExportString()
  {
    if ( accuracy < 0 ) {
      return String.format(Locale.US, "%s %.6f %.6f %.0f", name, lng, lat, h_geo );
    } else if ( accuracy_v < 0 ) {
      return String.format(Locale.US, "%s %.6f %.6f %.0f # %.1f", name, lng, lat, h_geo, accuracy );
    } else {
      return String.format(Locale.US, "%s %.6f %.6f %.0f # %.1f %.1f", name, lng, lat, h_geo, accuracy, accuracy_v );
    }
  }

  // FIXME M_TO_UNITS
  /** @return the string "long lat h_ell" with CS coordinates for the display
   * @note display cs lng-lat in cs units
   */
  String toExportCSString()
  {
    StringBuilder fmt = new StringBuilder();
    fmt.append("%.").append( cs_n_dec ).append("f %.").append( cs_n_dec ).append("f %.0f");
    return String.format(Locale.US, fmt.toString(), cs_lng*mToUnits, cs_lat*mToUnits, cs_h_geo*mToVUnits );
    // return String.format(Locale.US, "%s %.2f %.2f %.0f", name, cs_lng, cs_lat, cs_h_geo );
  }

  /** @return the name of the custom CS
   */
  String csName() { return cs; }

  /** @return the comment
   */
  public String getComment() { return comment; }

  /** @return the name of the source
   */
  public String getSource() 
  {
    switch ( (int)source ) {
      case 1: return "TopoDroid";
      case 2: return "manual";
      case 3: return "Mobile-Topographer";
      case 4: return "GPX recorder";
      case 5: return "GPS position";
      case 6: return "GPS test";
      case 7: return "GPS logger";
    }
    return "unknown";
  }

  /** @return meridian convergence [degree]
   */
  public double getConvergence() { return convergence; }

  /** @return the fix point accuracy
   */
  public double getAccuracy() { return accuracy; }

  /** @return the fix point vertical accuracy
   */
  public double getAccuracyVert() { return accuracy_v; }

  /** @return the meters-units factor
   */
  public double getMToUnits() { return mToUnits; }

  /** @return the meters-vert.units factor
   */
  public double getMToVertUnits() { return mToVUnits; }

  // @RecentlyNonNull
  public String toString()
  {
    return name + " " + double2string( lng ) + " " + double2string( lat ) + " " + (int)(h_geo); // + " [wgs " + (int)(h_ell) + "]";
  }

  static String double2string( double x )
  {
    return ( TDSetting.mUnitLocation == TDUtil.DDMMSS ) ? double2ddmmss( x ) : double2degree( x );
  }

  /** @return dd.mm.ss with seconds at two decimal places (roughly 0.3 m)
   * @param x  decimal degrees
   */ 
  static String double2ddmmss( double x )
  {
    boolean negative = x < 0;
    x = Math.abs(x);
    int dp = (int)x;
    x = 60*(x - dp);
    int mp = (int)x;
    x = 60*(x - mp);
    int sp = (int)x;
    int ds = (int)( 100 * (x-sp) + 0.4999 );
    if ( ds == 100 ) { sp += 1; ds = 0; }
    if (negative) {
      dp = -dp;
    }
    return String.format(Locale.US, "%d°%02d'%02d.%02d\"", dp, mp, sp, ds );
  }

  /** @return degrees with six decimal places (roughly 0.1 m)
   * @param x  decimal degrees
   */
  static String double2degree( double x )
  {
    return String.format(Locale.US, "%.6f", x );
  }

  /** convert a dd.mm.ss char-sequence to double (degrees)
   * @param txt  DD.MM.SS
   * @return the decimal degrees
   * @note see the next method
   */
  static double string2double( CharSequence txt )
  {
    if ( txt == null ) return -1111;
    return string2double( txt.toString() );
  }

  /** convert a dd.mm.ss string to double (degrees)
   * @param str  DD.MM.SS
   * @return the decimal degrees
   * @note the D/M and M/S separators can be color, space, degree-sign, or apostrophe
   *       the decimals separator can be point, slash, or comma
   */
  static double string2double( String str )
  {
    if ( str == null ) return -1111.0;
    str = str.trim();                  // drop initial and final spaces
    if ( str.length() == 0 ) return -1111.0;

    str = str.replace( " ", ":" );     // replace separators
    str = str.replace( "°", ":" );     
    str = str.replace( "'", ":" );     
    str = str.replace( "/", "." );
    str = str.replace( ",", "." );
    str = str.replace( "\"", "" );  // remove final seconds identifier
    String[] token = str.split( ":" ); // tokenize str on ':'
    try {
      if ( token.length == 3 ) {
        int degrees = Integer.parseInt( token[0] );
        boolean isNegative = degrees < 0;
        degrees = Math.abs(degrees);
        double dd = degrees
                + Integer.parseInt( token[1] ) / 60.0
                + Double.parseDouble( token[2] ) / 3600.0;
        if (isNegative) {
          dd = -dd;
        }
        return dd;
      } else if ( token.length == 1 ) {
        return Double.parseDouble( str );
      }
    } catch (NumberFormatException e ) {
      TDLog.Error( "string2double parse error: " + str );
    }
    return -1111.0; // more neg than -1000
  }        

  /** convert a decimal degree char-sequence to double (degrees)
   * @param txt  DD.MM.SS
   * @return the decimal degrees
   * @note see the next method
   */
  static double string2real( CharSequence txt )
  {
    if ( txt == null ) return 0;
    return string2real( txt.toString() );
  }

  /** convert a decimal degree string to double (degrees)
   * @param str  DD.MM.SS
   * @return the decimal degrees
   * @note the decimals separator can be point, slash, or comma
   */
  static private double string2real( String str )
  {
    if ( str == null ) return 0;
    str = str.trim();  // drop initial and final spaces
    if ( str.length() == 0 ) return 0;
    str = str.replace( "/", "." );
    str = str.replace( ",", "." );
    try {
      return Double.parseDouble( str );
    } catch (NumberFormatException e ) {
      TDLog.Error( "string2real parse error: " + str );
    }
    return 0;
  }        

}
