/** @file ExportGeo.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief general GEO info for georeferenced exports
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Cave3DFix;
import com.topodroid.mag.Geodetic;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class ExportGeo
{
  private Cave3DFix origin = null;
  private double lat, lng, h_geo;
  private double s_radius, e_radius;
  private Cave3DStation zero;

  // double mConv    = 0; 
  double mCosConv = 1;
  double mSinConv = 0;

  public boolean hasGeo = false;

  /** get E coord 
   * @param st station
   */
  double getE( Vector3D st ) { return hasGeo? lng + (st.x - zero.x) * e_radius : st.x; }

  /** get E coord without convergence
   * @param st station
   *
   *    E=x+Dy*C
   *              x=0  
   *           :  /       Dy*C > 0
   *       x<0 : /  x>0
   *           :/
   *   --------0----------------
   *          /:   Dy*C < 0
   *
   * This is the inverse of Cave3DFix::lngToEast where
   *    East = x_0 + (lng - lng_0) * E_radius * ( 1 + north * conv )
   * 
   * Shots azimuth are added declination (true North) and subtracted convergence
   * Now they must be rotated by the convergence
   *     X => X * cos(conv) + Y * sin(conv)
   *     Y => Y * cos(conv) - X * sin(conv)
   */
  double getENC( Vector3D st )
  { 
    if ( ! hasGeo ) return st.x;
    double x = (st.x - zero.x); // east
    double y = (st.y - zero.y); // north
    return lng + ( x * mCosConv + y * mSinConv ) * e_radius;
  }

  /** get N coord 
   * @param st station
   */
  double getN( Vector3D st ) { return hasGeo? lat + (st.y - zero.y) * s_radius : st.y; }

  /** get N coord without convergence
   * @param st station
   * This is the inverse ov Cave3DFix::latToNorth which is
   *     North = y_0 + ( lat - lat_0 ) * S_radius
   */
  double getNNC( Vector3D st ) 
  { 
    if ( ! hasGeo ) return st.y;
    double x = (st.x - zero.x); // east
    double y = (st.y - zero.y); // north
    return lat + ( y * mCosConv - x * mSinConv ) * s_radius;
  }

  /** get Z coord 
   * @param st station
   */
  double getZ( Vector3D st ) { return hasGeo? h_geo + (st.z - zero.z) : st.z; }

  /** ???
   * @param data        data parser
   * @param decl        magnetic declination (unused)
   */
  protected boolean getGeolocalizedData( TglParser data, double decl )
  {
    // TDLog.v( "KML get geo-localized data. Declination " + decl );
    List< Cave3DFix > fixes = data.getFixes();
    if ( fixes.size() == 0 ) {
      // TDLog.v( "KML no geo-localization");
      return false;
    }

    origin = null;
    for ( Cave3DFix fix : fixes ) {
      if ( ! fix.hasWGS84 ) continue;
      // if ( fix.cs == null ) continue;
      // if ( ! fix.cs.name.equals("long-lat") ) continue;
      for ( Cave3DStation st : data.getStations() ) {
        if ( st.getFullName().equals( fix.getFullName() ) ) {
          origin = fix;
          zero   = st;
          break;
        }
      }
      if ( origin != null ) break;
    }
    if ( origin == null ) {
      // TDLog.v( "KML no geolocalized origin");
      return false;
    }

    // origin has coordinates ( e, n, z ) these are assumed lat-long
    // altitude is assumed wgs84
    lat = origin.latitude;
    lng = origin.longitude;
    // mConv = Geodetic.meridianConvergenceFactor( origin.latitude );
    mCosConv = TDMath.cosDd( origin.mConvergence );
    mSinConv = TDMath.sinDd( origin.mConvergence );
    double h_ell = origin.a_ellip;
    h_geo = origin.z; // KML uses Geoid altitude (unless altitudeMode is set)
    TDLog.v( "Geo origin " + lat + " N " + lng + " E " + h_geo + " conv " + origin.mConvergence );

    s_radius = 1.0 / Geodetic.meridianRadiusExact( lat, h_ell );
    e_radius = 1.0 / Geodetic.parallelRadiusExact( lat, h_ell );
// FIXME_ELLIPSOID
    // s_radius = 1.0 / Geodetic.meridianRadiusEllipsoid( lat, h_ell );
    // e_radius = 1.0 / Geodetic.parallelRadiusEllipsoid( lat, h_ell );
    hasGeo = true;
    return true;
  }

}
