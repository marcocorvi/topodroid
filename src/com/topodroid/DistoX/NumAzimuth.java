/** @file NumAzimuth.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction leg-azimuth at a station
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class NumAzimuth
{
  float mAzimuth;  // azimuth of the leg at the station [degrees]
  float mExtend;   // extend of the leg "at the station"

  NumAzimuth( float a, float e )
  {
    mAzimuth = a;
    mExtend  = e;
  }
}
