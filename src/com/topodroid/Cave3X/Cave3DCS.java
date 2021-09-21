/** @file Cave3DCS.java
 *
 * @author marco corvi
 * @date mav 2020
 *
 * @brief Cave3D coordinate system
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.Cave3X;

public class Cave3DCS
{
  public final static String WGS84 = "WGS-84";

  public String name; // CS name
  // String proj4; // proj4 syntax CS description

  public Cave3DCS( ) { name = WGS84; }
  public Cave3DCS( String nm ) { name = nm; }

  public boolean hasName() { return ( name != null ) && ( name.length() > 0 ); }

  public boolean equals( Cave3DCS cs ) { return (cs != null) && equals( cs.name ); }
  public boolean equals( String cs_name ) { return (cs_name != null) && ( cs_name.length() > 0 ) && name.equals( cs_name ); }

  public boolean isWGS84() { return name.equals( WGS84 ); }

}

