/** @file Cave3DCS.java
 *
 * @author marco corvi
 * @date mav 2020
 *
 * @brief 3D: coordinate system
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

public class Cave3DCS
{
  public final static String WGS84 = "WGS-84";

  public String name; // CS name
  // String proj4; // proj4 syntax CS description

  /** default cstr
   */
  public Cave3DCS( ) { name = WGS84; }

  /** cstr
   * @param nm   CS name
   */
  public Cave3DCS( String nm ) { name = nm; }

  /** check if the CS name is not null
   * @return true if the name of the CS is not null
   */
  public boolean hasName() { return ( name != null ) && ( name.length() > 0 ); }

  /** check if the CS name is equal to a given name
   * @param name   name
   * @return true if the name of this CS is not null, nor empty, and equal to the given name
   */
  // public boolean equals( Cave3DCS cs ) { return (cs != null) && equals( cs.name ); }
  public boolean equals( String cs_name ) { return (cs_name != null) && ( cs_name.length() > 0 ) && name.equals( cs_name ); }

  /** check if the name of the CS is WGS84
   * @return true if the name of the CS is WGS84
   */
  public boolean isWGS84() { return name.equals( WGS84 ); }

}

