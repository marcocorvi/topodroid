/** @file TglMeasure.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D measure result
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

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

  // positive and negative distances are the sum of deltaZ weighted by
  //    0 if clino < 10 degrees
  //    (clino - 10)/20 if clino is between 10 and 30
  //    1 if clino is above 30 degrees
  private double dist_pos; // vertical positive distance
  private double dist_neg; // vertical negative distance

  /** cstr
   * @param res     resources
   * @param s1      start station
   * @param s2      end station
   * @param dc      ...
   * @param dpos    positive denivelation
   * @param dneg    negative denivelation
   */
  TglMeasure( Resources res, Cave3DStation s1, Cave3DStation s2, double dc, double dpos, double dneg )
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
    dist_pos = dpos;
    dist_neg = dneg;
  }

  /** @return positive denivelation
   */
  double getDistPos() { return dist_pos; }

  /** @return negative denivelation
   */
  double getDistNeg() { return dist_neg; }

  String getString()
  {
    if ( dcave < Float.MAX_VALUE-1 ) {
      String format = mRes.getString( R.string.dist_path );
      return String.format(Locale.US, format, st1.getShortName(), st2.getShortName(), d3, de, dn, dz, dist_pos, dist_neg, dcave );
    } 
    String format = mRes.getString( R.string.dist_no_path );
    return String.format(Locale.US, format, st1.getShortName(), st2.getShortName(), d3, de, dn, dz, dist_pos, dist_neg  );
  }
}

