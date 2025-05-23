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

import com.topodroid.utils.TDLog;
import com.topodroid.mag.Geodetic;

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
  public double a_ellip = 0.0; // NOTE ellipsoidic altitude - used to compute radii
  // private double a_geoid = 0.0; // geodetic altitude (not used)
  public boolean hasWGS84;
  public double mToUnits = 1.0;
  public double mToVUnits = 1.0;
  public double mConvergence;

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
    dos.writeDouble( a_ellip );
    dos.writeDouble( mToUnits );
    dos.writeDouble( mToVUnits );
    dos.writeDouble( mConvergence );
    // dos.writeDouble( a_geoid ); // 20221203 inserted this
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
    double lng   = dis.readDouble( );
    double lat   = dis.readDouble( );
    double h_ell = dis.readDouble( );
    double m_to_units  = dis.readDouble( ); // NOTE these break backward compatibility
    double m_to_vunits = dis.readDouble( );
    double conv = dis.readDouble( );
    // double h_geo = dis.readDouble( ); // 20221203 inserted this
    return new Cave3DFix( name, x, y, z, null, lng, lat, h_ell /*, h_geo */, m_to_units, m_to_vunits, conv );
  }
    

  // public boolean hasCS() { return cs != null && cs.hasName(); }

  // void log()
  // {
  //   // TDLog.v("origin " + name + " CS " + cs.name + " " + longitude + " " + latitude );
  // }

  /** cstr
   * @param nm   name
   * @param e0   east coord
   * @param n0   north coord
   * @param z0   vertical coord
   * @param cs0  coord reference system
   * @param lng  WGS84 longitude
   * @param lat  WGS84 latitude
   * @param h_ell WGS84 altitude (ellipsoid)
   * (param h_geo geoid altitude)
   * @param m_to_units cs h units
   * @param m_to_vunits cs v units
   * @param conv  meridian convergence
   */
  public Cave3DFix( String nm, double e0, double n0, double z0, Cave3DCS cs0, double lng, double lat, double h_ell /* , double h_geo */,
                    double m_to_units, double m_to_vunits, double conv )
  {
    super( e0, n0, z0 );
    name = nm;
    cs = cs0;
    longitude = lng;
    latitude  = lat;
    a_ellip   = h_ell;
    // a_geoid   = h_geo;
    hasWGS84  = true;
    mToUnits  = m_to_units;
    mToVUnits = m_to_vunits;
    mConvergence = conv;
  }

  /** cstr
   * @param nm   name
   * @param e0   east coord
   * @param n0   north coord
   * @param z0   vertical coord
   * @param cs0  coord reference system
   * @param lng  WGS84 longitude
   * @param lat  WGS84 latitude
   * @param h_ell WGS84 altitude (ellipsoid)
   * (param h_geo geoid altitude)
   * @param m_to_units cs h units
   * @param m_to_vunits cs v units
   */
  public Cave3DFix( String nm, double e0, double n0, double z0, Cave3DCS cs0, double m_to_units, double m_to_vunits, double conv )
  {
    // super( e0, n0, z0 );
    // name = nm;
    // cs = cs0;
    // longitude = 0;
    // latitude  = 0;
    // a_ellip  = 0;
    // // a_geoid  = 0;
    this( nm, e0, n0, z0, cs0, 0, 0, 0 /*, 0 */, m_to_units, m_to_vunits, conv );
    hasWGS84 = false;
  }

  /** @return true if the 3D fix has a name
   * @note the "name" for a fix is the full-name
   */
  public boolean hasName( String nm ) { return name != null && name.equals( nm ); }

  /** @return the 3D fix CS name
   */
  public String getCSName( ) { return cs.name; }

  /** @return the 3D fix fullname
   */
  public String getFullName( ) { return name; }

  /** @return true if the coord system is WGS84
   */
  public boolean isWGS84() { return cs.isWGS84(); }

  /** @return the south-north radius (multiplied by PI/180)
   * the radius is used for the DEM Y-unit
   */
  double getSNradius() 
  { 
    return isWGS84()? Geodetic.meridianRadiusExact( latitude, a_ellip ) : 1.0;
    // return isWGS84()? Geodetic.meridianRadiusEllipsoid( latitude, a_ellip ) : 1.0; // FIXME_ELLIPSOID : NO
  }

  /** @return the west-east radius (multiplied by PI/180)
   * the radius is used for the DEM X-unit
   */
  double getWEradius() 
  { 
    return isWGS84()? Geodetic.parallelRadiusExact( latitude, a_ellip ) : 1.0;
    // return isWGS84()? Geodetic.parallelRadiusEllipsoid( latitude, a_ellip ) : 1.0; // FIXME_ELLIPSOID : NO
  }

  /** @return true if the 3D fix has WGS84 coords
   */
  boolean hasWGS84() { return hasWGS84; }

  /** @return north (Y) coord of a point from its latitude and altitude
   * @param lat   WGS84 latitude
   * @param h_ell WGS84 ellipsoid altitude
   */
  public double latToNorth( double lat, double h_ell ) 
  {
    double s_radius = Geodetic.meridianRadiusExact( lat, h_ell ); // this is the radius * PI/180
    return hasWGS84()? y + (lat - latitude) * s_radius : 0.0;
// FIXME_ELLIPSOID
    // double s_radius = Geodetic.meridianRadiusEllipsoid( lat, h_ell ); 
    // TDLog.v(" lat to N s_radius " + s_radius );
    // return hasWGS84()? y + (lat - latitude) * s_radius : 0.0;
  }

  /** @return east (X) coord of a point from its longitude, latitude, altitude
   * @param lng   WGS84 point longitude
   * @param lat   WGS84 point latitude
   * @param h_ell WGS84 point ellipsoid altitude
   * @param north north differece between the point and this point
   *
   * ref. T. Soler, R.J. Fury PS alignment surveys and meridian convergence, J. Surveying Eng., Aug. 2000 69
   *    dt = ds sin(A) tan(phi) / N
   * where
   *    N = a/W principal radius of curvature W = sqrt( 1 - E2 sin^2(phi) )
   *    E2 = 2 F - F^2
   *    a  = 6378137 [m]
   *    1/F = 298,257222101
   *    A = azimuth of AB  
   *    ds distance AB
   *    phi = latitude of A
   *    dt = angle of convergence
   * here i use a further approximation ds sin(A) = West-East projected distance AB 
   */
  public double lngToEast( double lng, double lat, double h_ell, double north )
  {
    double conv = Geodetic.meridianConvergenceFactor( latitude );
    double e_radius = Geodetic.parallelRadiusExact( lat, h_ell ); // this is the radius * PI/180
    // double e0_radius = Geodetic.parallelRadiusExact( latitude, a_ellip ); 
    return hasWGS84()? x + (lng - longitude) * e_radius * (1 + north*conv) : 0.0;
    // return hasWGS84()? x + (lng * e_radius - longitude * e0_radius) * (1 + north*conv) : 0.0;
 // FIXME_ELLIPSOID
    // double e_radius = Geodetic.parallelRadiusEllipsoid( lat, h_ell ); 
    // double e0_radius = Geodetic.parallelRadiusEllipsoid( latitude, a_ellip );
    // TDLog.v(" lng to E radius = " + e_radius + " " + e0_radius + " north " + north + " conv " + conv );
    // return hasWGS84()? x + (lng * e_radius - longitude * e0_radius) * (1 + north*conv) : 0.0;
  }

}
