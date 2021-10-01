/** @file TglMeasure.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D measure result
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.res.Resources;

import java.util.Locale;

class TglMeasure
{
  private Resources mRes;

  Cave3DStation st1, st2;
  double d3; // 3D distance
  double de, dn, dz;  // axis distances
  double d2; // horiz. plane distance
  double azimuth, clino; // angles
  double dcave; // cave distance

  TglMeasure( Resources res, Cave3DStation s1, Cave3DStation s2, double dc )
  {
    st1 = s1;
    st2 = s2;
    mRes = res;
    dcave = dc;
    de = s2.x - s1.x;
    dn = s2.y - s1.y;
    dz = s2.z - s1.z;
    d3 = Math.sqrt( de*de + dn*dn + dz*dz );
    d2 = Math.sqrt( de*de + dn*dn );
    azimuth = ( Math.atan2( de, dn ) * 180.0f / Math.PI );
    if ( azimuth < 0 ) azimuth += 360.0f; 
    clino   = ( Math.atan2( dz, d2 ) * 180.0f / Math.PI );
  }

  String getString()
  {
    if ( dcave < Float.MAX_VALUE-1 ) {
      String format = mRes.getString( R.string.dist_path );
      return String.format(Locale.US, format, st1.getShortName(), st2.getShortName(), d3, de, dn, dz, dcave );
    } 
    String format = mRes.getString( R.string.dist_no_path );
    return String.format(Locale.US, format, st1.getShortName(), st2.getShortName(), d3, de, dn, dz );
  }
}

