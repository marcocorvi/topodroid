/* @file MagHarmonic.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid World Magnetic Model 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * Implemented after GeomagneticLibrary.c by
 *  National Geophysical Data Center
 *  NOAA EGC/2
 *  325 Broadway
 *  Boulder, CO 80303 USA
 *  Attn: Susan McLean
 *  Phone:  (303) 497-6478
 *  Email:  Susan.McLean@noaa.gov
 */
package com.topodroid.DistoX;

// MAGtype_SphericalHarmonicVariables
class MagHarmonic
{
  private int nMax;
  double RelativeRadiusPower[];
  double cos_mlambda[];
  double sin_mlambda[]; 

  MagHarmonic( int nm )
  { 
    nMax = nm;
    RelativeRadiusPower =  new double[ nMax + 1 ];
    cos_mlambda =  new double[ nMax + 1 ];
    sin_mlambda =  new double[ nMax + 1 ];
  }
}
