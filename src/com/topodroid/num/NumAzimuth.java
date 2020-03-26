/* @file NumAzimuth.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction leg-azimuth at a station
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

class NumAzimuth
{
  final float mAzimuth;  // azimuth of the leg at the station [degrees]
  final float mExtend;   // extend of the leg "at the station"

  NumAzimuth( float a, float e )
  {
    mAzimuth = a;
    mExtend  = e;
  }
}
