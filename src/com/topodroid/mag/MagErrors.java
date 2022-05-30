/* @file MagErrors.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid World Magnetic Model 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
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
package com.topodroid.mag;

class MagErrors
{
  private final double DeclErr;
  private final double InclErr;
  private final double FErr;

  MagErrors( )
  {
    DeclErr = 0;
    InclErr = 0;
    FErr    = 0;
  }

  MagErrors( double d, double i, double f )
  {
    DeclErr = d;
    InclErr = i;
    FErr    = f;
  }
}
