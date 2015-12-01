/* @file WMMcoeff.java
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
class WMMcoeff
{
  int n;
  int m;
  float v0, v1, v2, v3;

  WMMcoeff( int nn, int mm, float vv0, float vv1, float vv2, float vv3 )
  {
    n = nn;
    m = mm;
    v0 = vv0;
    v1 = vv1;
    v2 = vv2;
    v3 = vv3;
  }

  int index() { return (n * (n+1))/2 + m; }

  static int index( int nn, int mm ) { return (nn * (nn+1))/2 + mm; }
}
