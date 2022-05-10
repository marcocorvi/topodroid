/* @file TDExporter.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief TopoDroid data for Polygon exports
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

class PolygonData
{
  boolean used;
  String from;
  String to;
  // AverageLeg leg;
  float length;
  float bearing;
  float clino;
  LRUD lrud;
  String comment;

  /** cstr
   * @param f   FROM station
   * @param t   TO station
   * @param lg  leg average data
   * @param lr  LRUD
   * @param cmt comment
   */
  PolygonData( String f, String t, AverageLeg lg, LRUD lr, String cmt )
  {
    used = false;
    from = f;
    to   = t;
    // leg  = new AverageLeg( lg );
    length  = lg.length();
    bearing = lg.bearing();
    clino   = lg.clino();
    lrud = new LRUD( lr );
    comment = cmt;
  }

  /** reverse the shot
   */
  void reverse()
  {
    String tmp = from;  from   = to;      to     = tmp;
    float  t = lrud.l;  lrud.l = lrud.r;  lrud.r = t;
    // leg.reverse();
    bearing = bearing + 180; if ( bearing >= 360 ) bearing -= 360;
    clino   = - clino;
  }

  // void init( String f, String t, AverageLeg lg, LRUD lr, String cmt )
  // {
  //   from = f;
  //   to   = t;
  //   length  = lg.length();
  //   bearing = lg.bearing();
  //   clino   = lg.clino();
  //   lrud = new LRUD( lr );
  //   comment = cmt;
  // }
    
}
