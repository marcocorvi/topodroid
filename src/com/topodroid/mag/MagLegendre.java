/* @file MagLegendre.java
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

// import java.util.Locale;

// MAGtype_LegendreFunction;
class MagLegendre
{
  private int nTerms;
  double[] Pcup; /* Legendre Function */
  double[] dPcup; /* Derivative of Legendre fcn */

  MagLegendre( int nt ) 
  {
    nTerms = nt;
    Pcup  = new double[ nTerms + 1 ];
    dPcup = new double[ nTerms + 1 ];
  }
 
  // void debugLegendre()
  // {
  //   for ( int n=0; n<=12; ++n ) {
  //     StringBuilder sb = new StringBuilder();
  //     for ( int m=0; m<=n; ++m ) {
  //       int idx = (n * (n+1))/2 + m;
  //       sb.append( String.format(Locale.US, "%.8f ", Pcup[idx] ) );
  //     }
  //     TDLog.v( sb.toString() );
  //   }
  // }
 
  // void ddebugLegendre()
  // {
  //   for ( int n=0; n<=12; ++n ) {
  //     StringBuilder sb = new StringBuilder();
  //     for ( int m=0; m<=n; ++m ) {
  //       int idx = (n * (n+1))/2 + m;
  //       sb.append( String.format(Locale.US, "%.8f ", dPcup[idx] ) );
  //     }
  //     TDLog.v( sb.toString() );
  //   }
  // }

}     
