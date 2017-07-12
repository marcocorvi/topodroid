/* @file MagUtil.java
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


// #define NOOFCOEFFICIENTS (7)

class MagUtil
{
  /*These error values come from the ISCWSA error model:
   *http://www.copsegrove.com/Pages/MWDGeomagneticModels.aspx
   *
  #define INCL_ERROR_BASE (0.20)
  #define DECL_ERROR_OFFSET_BASE (0.36)  
  #define F_ERROR_BASE (130)
  #define DECL_ERROR_SLOPE_BASE (5000)
  #define WMM_ERROR_MULTIPLIER 1.21
  #define IGRF_ERROR_MULTIPLIER 1.21
   */

  // These error values are the NGDC error model 
  static final double WMM_UNCERTAINTY_F = 152;
  static final double WMM_UNCERTAINTY_H = 133;
  static final double WMM_UNCERTAINTY_X = 138;
  static final double WMM_UNCERTAINTY_Y = 89;
  static final double WMM_UNCERTAINTY_Z = 165;
  static final double WMM_UNCERTAINTY_I = 0.22;
  static final double WMM_UNCERTAINTY_D_OFFSET = 0.24;
  static final double WMM_UNCERTAINTY_D_COEF = 5432;

  // static final double M_PI = Math.PI;
  // static final double RAD2DEG = 180.0/M_PI;
  // static final double DEG2RAD = M_PI/180.0;

  static final double MAG_PS_MIN_LAT_DEGREE  = -55; /* Minimum Latitude for  Polar Stereographic projection in degrees   */
  static final double MAG_PS_MAX_LAT_DEGREE  =  55; /* Maximum Latitude for Polar Stereographic projection in degrees     */
  static final double MAG_UTM_MIN_LAT_DEGREE = -80.5; /* Minimum Latitude for UTM projection in degrees   */
  static final double MAG_UTM_MAX_LAT_DEGREE =  84.5; /* Maximum Latitude for UTM projection in degrees     */
  static final double MAG_GEO_POLE_TOLERANCE = 1e-5;
  static final boolean MAG_USE_GEOID = true;   /* 1 Geoid - Ellipsoid difference should be corrected, 0 otherwise */

  static int CALCULATE_NUMTERMS( int N) { return (N * ( N + 1 )) / 2 + N+1; }
  static double ATanH( double x ) { return (0.5 * Math.log((1 + x) / (1 - x))); }

  /*New Error Functions*/
  static MagElement getWMMErrorCalc( double H )
  {
    MagElement ret = new MagElement();
    ret.F = WMM_UNCERTAINTY_F;
    ret.H = WMM_UNCERTAINTY_H;
    ret.X = WMM_UNCERTAINTY_X;
    ret.Z = WMM_UNCERTAINTY_Z;
    ret.Y = WMM_UNCERTAINTY_Y;
    ret.Incl = WMM_UNCERTAINTY_I;
    double decl_variable = WMM_UNCERTAINTY_D_COEF / H;
    double decl_constant = WMM_UNCERTAINTY_D_OFFSET;
    ret.Decl = Math.sqrt(decl_constant*decl_constant + decl_variable*decl_variable);
    if (ret.Decl > 180) ret.Decl = 180;
    return ret;
  }
}
