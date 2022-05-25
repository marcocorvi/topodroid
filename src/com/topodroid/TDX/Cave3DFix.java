/** @file Cave3DFix.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief 3D: fixed station
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * ref. T. Soler, L.D. Hothem
 *      Coordinate systems usd in geodesy: basic definitions and concepts, 1988
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Cave3DFix extends Vector3D
{
  /** fix station:
   * fix stations are supposed to be referred to the same coord system
   */
  Cave3DCS cs;
  String name;
  // double e, n, z; // north east, vertical (upwards)
  
  public double longitude; // WGS84
  public double latitude; 
  public double altitude = 0.0; // FIXME ellipsoidic altitude
  public boolean hasWGS84;

  /** serialize the 3D fix
   * @param dos    output stream
   */
  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.writeUTF( name );
    dos.writeDouble( x );
    dos.writeDouble( y );
    dos.writeDouble( z );
    dos.writeDouble( longitude );
    dos.writeDouble( latitude );
    dos.writeDouble( altitude );
  }

  /** deserialize a 3D fix
   * @param dis     input stream
   * @param version stream version (unused)
   * @return the deserialized 3D fix
   */
  static Cave3DFix deserialize( DataInputStream dis, int version ) throws IOException
  {
    String name = dis.readUTF( );
    double x = dis.readDouble( );
    double y = dis.readDouble( );
    double z = dis.readDouble( );
    double lng = dis.readDouble( );
    double lat = dis.readDouble( );
    double alt = dis.readDouble( );
    return new Cave3DFix( name, x, y, z, null, lng, lat, alt );
  }
    

  // public boolean hasCS() { return cs != null && cs.hasName(); }

  // void log()
  // {
  //   TDLog.v("origin " + name + " CS " + cs.name + " " + longitude + " " + latitude );
  // }

  /** cstr
   * @param nm   name
   * @param e0   east coord
   * @param n0   north coord
   * @param z0   vertical coord
   * @param cs0  coord reference system
   * @param lng  WGS84 longitude
   * @param lat  WGS84 latitude
   * @param alt  WGS84 altitude (ellissoidic)
   */
  public Cave3DFix( String nm, double e0, double n0, double z0, Cave3DCS cs0, double lng, double lat, double alt )
  {
    super( e0, n0, z0 );
    name = nm;
    cs = cs0;
    longitude = lng;
    latitude  = lat;
    altitude  = alt;
    hasWGS84  = true;
  }

  /** cstr
   * @param nm   name
   * @param e0   east coord
   * @param n0   north coord
   * @param z0   vertical coord
   * @param cs0  coord reference system
   */
  public Cave3DFix( String nm, double e0, double n0, double z0, Cave3DCS cs0 )
  {
    super( e0, n0, z0 );
    name = nm;
    cs = cs0;
    longitude = 0;
    latitude  = 0;
    altitude  = 0;
    hasWGS84  = false;
  }

  /** @return true if the 3D fix has a name
   * @note the "name" for a fix is the full-name
   */
  public boolean hasName( String nm ) { return name != null && name.equals( nm ); }

  /** @return the 3D fix fullname
   */
  public String getFullName( ) { return name; }

  /** @return true if the coord system is WGS84
   */
  public boolean isWGS84() { return cs.isWGS84(); }

  /** @return the south-north redius
   */
  public double getSNradius() 
  { 
    return isWGS84()? Geodetic.meridianRadiusExact( latitude, altitude ) : 1.0;
  }

  /** @return the west-east redius
   */
  public double getWEradius() 
  { 
    return isWGS84()? Geodetic.parallelRadiusExact( latitude, altitude ) : 1.0;
  }

  /** @return true if the 3D fix has WGS84 coords
   */
  boolean hasWGS84() { return hasWGS84; }

  /** @return north coord from the latitude
   * @param lat WGS84 latitude
   * @param alt WGS84 altitude
   */
  public double latToNorth( double lat, double alt ) 
  {
    double s_radius = Geodetic.meridianRadiusExact( lat, alt ); // this is the radius * PI/180
    return hasWGS84()? y + (lat - latitude) * s_radius : 0.0;
  }

  /** @return east coord from the longitude
   * @param lng WGS84 longitude
   * @param lat WGS84 latitude
   * @param alt WGS84 altitude
   */
  public double lngToEast( double lng, double lat, double alt )
  {
    double e_radius = Geodetic.parallelRadiusExact( lat, alt ); // this is the radius * PI/180
    return hasWGS84()? x + (lng - longitude) * e_radius : 0.0;
  }

}
