/** @file FixedInfo.java
 *
 * @author marco corvi
 * @date apr 2012
 *
 * @brief TopoDroid fixed stations (GPS-localized stations)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;

/** fixed (GPS) point
 * Note the order of data: LONGITUDE - LATITUDE - ALTITUDE
 */
class FixedInfo
{
  Long   id;       // fixed id
  String name;     // station name, or whatever
  double lng;      // longitude [decimal deg]
  double lat;      // latitude [decimal deg]
  double alt;      // wgs84 altitude [m]
  double asl;      // geoid altitude [m] 
  String comment;

  public FixedInfo( long _id, String n, double longitude, double latitude, double alt_wgs84, double alt_ortho, String cmt )
  {
    id = _id;
    name = n;
    lng = longitude;
    lat = latitude;
    alt = alt_wgs84;
    asl = alt_ortho;
    comment = cmt;
  }

  public FixedInfo( long _id, String n, double longitude, double latitude, double alt_wgs84, double alt_ortho )
  {
    id = _id;
    name = n;
    lng = longitude;
    lat = latitude;
    alt = alt_wgs84;
    asl = alt_ortho;
    comment = "";
  }

  public String toString()
  {
    return name + " " + double2string( lng ) + " " + double2string( lat ) + " " + (int)(asl) + " " + (int)(alt);
  }

  static String double2string( double x )
  {
    return ( TopoDroidSetting.mUnitLocation == TopoDroidConst.DDMMSS ) ? double2ddmmss( x ) : double2degree( x );
  }

  static private String double2ddmmss( double x )
  {
    int dp = (int)x;
    x = 60*(x - dp);
    int mp = (int)x;
    x = 60*(x - mp);
    int sp = (int)x;
    int ds = (int)( 100 * (x-sp) );
    StringWriter swp = new StringWriter();
    PrintWriter pwp = new PrintWriter( swp );
    pwp.format( "%d°%02d'%02d.%02d", dp, mp, sp, ds );
    return swp.getBuffer().toString();
  }

  static private String double2degree( double x )
  {
    StringWriter swp = new StringWriter();
    PrintWriter pwp = new PrintWriter( swp );
    pwp.format(Locale.ENGLISH, "%.6f", x );
    return swp.getBuffer().toString();
  }


  static double string2double( String str )
  {
    str = str.trim();                  // drop initial and final spaces
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
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "string2double parse error: " + str );
    }
    return -1111.0; // more neg than -1000
  }        

}
