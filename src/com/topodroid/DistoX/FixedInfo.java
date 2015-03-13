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
 * 20120516 cstr
 * 20120522 rename FixedInfo
 * 20120531 added toString 
 * 20120603 added toLocString
 * 20121205 location units
 * 20130520 altimetric altitude
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
  double asl;      // altimetric altitude [m]
  String comment;

  public FixedInfo( long _id, String n, double longitude, double latitude, double altitude, double altimetric, String cmt )
  {
    id = _id;
    name = n;
    lng = longitude;
    lat = latitude;
    alt = altitude;
    asl = altimetric;
    comment = cmt;
  }

  public FixedInfo( long _id, String n, double longitude, double latitude, double altitude, double altimetric )
  {
    id = _id;
    name = n;
    lng = longitude;
    lat = latitude;
    alt = altitude;
    asl = altimetric;
    comment = "";
  }

  public FixedInfo( long _id, String n, double longitude, double latitude, double altitude )
  {
    id = _id;
    name = n;
    lng = longitude;
    lat = latitude;
    alt = altitude;
    asl = -1.0;
    comment = "";
  }

  public String toLocString()
  {
    return ( ( TopoDroidSetting.mUnitLocation == TopoDroidConst.DDMMSS ) ?
               double2ddmmss( lng ) + " " + double2ddmmss( lat ) : 
               double2degree( lng ) + " " + double2degree( lat ) )
         + " " + Integer.toString( (int)(alt) )
         + " " + Integer.toString( (int)(asl) );
  }

  public String toString()
  {
    return name + " "
         + ( ( TopoDroidSetting.mUnitLocation == TopoDroidConst.DDMMSS ) ?
               double2ddmmss( lng ) + " " + double2ddmmss( lat ) :
               double2degree( lng ) + " " + double2degree( lat ) )
         + " " + ( (asl < 0 )? Integer.toString( (int)(alt) ) + " wgs84" : Integer.toString( (int)(asl) ) );
  }

  static String double2ddmmss( double x )
  {
    int dp = (int)x;
    x = 60*(x - dp);
    int mp = (int)x;
    x = 60*(x - mp);
    int sp = (int)x;
    int ds = (int)( 100 * (x-sp) );
    StringWriter swp = new StringWriter();
    PrintWriter pwp = new PrintWriter( swp );
    pwp.format( "%d:%02d:%02d.%02d", dp, mp, sp, ds );
    return swp.getBuffer().toString();
  }

  static String double2degree( double x )
  {
    StringWriter swp = new StringWriter();
    PrintWriter pwp = new PrintWriter( swp );
    pwp.format(Locale.ENGLISH, "%.6f", x );
    return swp.getBuffer().toString();
  }
}
