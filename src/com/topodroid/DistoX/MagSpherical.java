/* @file MagSpherical.java
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
package com.topodroid.DistoX;

// MAGtype_CoordSpherical;
class MagSpherical
{
  double lambda; /* longitude*/
  double phig; /* geocentric latitude*/
  double r; /* distance from the center of the ellipsoid*/

  MagSpherical( double l, double p, double rr )
  {
    lambda = l;
    phig   = p;
    r      = rr;
  }
  
  MagVector toCartesian( )
  {
    double radphi    = phig   * TDMath.RAD2DEG;
    double radlambda = lambda * TDMath.RAD2DEG;
    return new MagVector(
      r * Math.cos(radphi) * Math.cos(radlambda),
      r * Math.cos(radphi) * Math.sin(radlambda),
      r * Math.sin(radphi) );
  }
}
