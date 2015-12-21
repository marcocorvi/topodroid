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

public class NumAzimuth
{
  float mAzimuth;  // azimuth of the leg at the station [degrees]
  int   mExtend;   // extend of the leg "at the station"

  NumAzimuth( float a, int e )
  {
    mAzimuth = a;
    mExtend  = e;
  }
}
