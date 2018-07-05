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
package com.topodroid.DistoX;

import java.util.Locale;

/** fixed (GPS) point
 * Note the order of data: LONGITUDE - LATITUDE - ALTITUDE
 */
class FixedInfo extends MagLatLong
{
  final static long SRC_UNKNOWN    = 0L;
  final static long SRC_TOPODROID  = 1L;
  final static long SRC_MANUAL     = 2L;
  final static long SRC_MOBILE_TOP = 3L;
  long   id;       // fixed id
  long   source;   // 0: unknown,  1: topodroid,  2: manual,   3: mobile-topographer
  String name;     // station name, or whatever
  // double lat;      // latitude [decimal deg]
  // double lng;      // longitude [decimal deg]
  double alt;      // wgs84 altitude [m]
  double asl;      // geoid altitude [m] 
  String comment;
  String cs;
  double cs_lng;
  double cs_lat;
  double cs_alt;
  long   cs_n_dec;

  FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid,
                    String cmt, long src )
  {
    id = _id;
    name = n;
    lat = latitude;
    lng = longitude;
    alt = h_ellip;
    asl = h_geoid;
    comment = cmt;
    source  = src;
    cs = null;
    cs_lng = 0;
    cs_lat = 0;
    cs_alt = 0;
    cs_n_dec = 2L;
  }

  FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid,
                    String cmt, long src,
                    String name_cs, double lng_cs, double lat_cs, double alt_cs, long n_dec )
  {
    id = _id;
    name = n;
    lat = latitude;
    lng = longitude;
    alt = h_ellip;
    asl = h_geoid;
    comment = cmt;
    source  = src;
    cs      = name_cs;
    cs_lng  = lng_cs;
    cs_lat  = lat_cs;
    cs_alt  = alt_cs;
    cs_n_dec = n_dec;
  }

  void setCSCoords( String name_cs, double lng_cs, double lat_cs, double alt_cs, long n_dec )
  {
    cs = name_cs;
    if ( cs != null && cs.length() > 0 ) {
      cs_lng = lng_cs;
      cs_lat = lat_cs;
      cs_alt = alt_cs;
      cs_n_dec = n_dec;
    }
  }

  boolean hasCSCoords() { return ( cs != null && cs.length() > 0 ); }

  // public FixedInfo( long _id, String n, double longitude, double latitude, double h_ellip, double h_geoid )
  // {
  //   id = _id;
  //   name = n;
  //   lat = latitude;
  //   lng = longitude;
  //   alt = h_ellip;
  //   asl = h_geoid;
  //   comment = "";
  // }

  // get the string "name long lat alt" for the exports
  String toExportString()
  {
    return String.format(Locale.US, "%s %.6f %.6f %.0f", name, lng, lat, asl );
  }

  String toExportCSString()
  {
    StringBuilder fmt = new StringBuilder();
    fmt.append("%s %.").append( cs_n_dec ).append("f %.").append( cs_n_dec ).append("f %.0f");
    return String.format(Locale.US, fmt.toString(), name, cs_lng, cs_lat, cs_alt );
    // return String.format(Locale.US, "%s %.2f %.2f %.0f", name, cs_lng, cs_lat, cs_alt );
  }

  String csName() { return cs; }


  public String toString()
  {
    return name + " " + double2string( lng ) + " " + double2string( lat ) + " " + (int)(asl) + " [wgs " + (int)(alt) + "]";
  }

  static String double2string( double x )
  {
    return ( TDSetting.mUnitLocation == TDConst.DDMMSS ) ? double2ddmmss( x ) : double2degree( x );
  }

  static String double2ddmmss( double x )
  {
    int dp = (int)x;
    x = 60*(x - dp);
    int mp = (int)x;
    x = 60*(x - mp);
    int sp = (int)x;
    int ds = (int)( 100 * (x-sp) + 0.4999 );
    return String.format(Locale.US, "%d°%02d'%02d.%02d", dp, mp, sp, ds );
  }

  static String double2degree( double x )
  {
    return String.format(Locale.US, "%.6f", x );
  }

  static double string2double( CharSequence txt )
  {
    if ( txt == null ) return -1111;
    return string2double( txt.toString() );
  }

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
    String[] token = str.split( ":" ); // tokenize str on ':'
    try {
      if ( token.length == 3 ) {
        return Integer.parseInt( token[0] )
             + Integer.parseInt( token[1] ) / 60.0
             + Double.parseDouble( token[2] ) / 3600.0;
      } else if ( token.length == 1 ) {
        return Double.parseDouble( str );
      }
    } catch (NumberFormatException e ) {
      TDLog.Error( "string2double parse error: " + str );
    }
    return -1111.0; // more neg than -1000
  }        

  static double string2real( CharSequence txt )
  {
    if ( txt == null ) return 0;
    return string2real( txt.toString() );
  }

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
