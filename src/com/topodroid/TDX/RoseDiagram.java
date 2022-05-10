/** @file RoseDiagram.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief 2D rose direction diagram
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

class RoseDiagram
{
  int mPointNr;
  double[] mValue; // accumulator

  /** cstr
   * @param n   number of bins
   */
  RoseDiagram( int n )
  {
    mPointNr = n;
    mValue = new double[ mPointNr ];
    reset();
  }

  /** reset the accumulator bins to zero
   */
  void reset()
  {
    for ( int k = 0; k < mPointNr; ++k ) mValue[k] = 0.0;
  }

  /** add a shot
   * @param d   shot distance
   * @param b   shot bearing [radians]
   * @param c   shot clino [radians]
   * @param eps tolerance
   */
  void add( double d, double b, double c, double eps ) 
  {
    d *= Math.cos( c );
    double f = 2.0*Math.PI/mPointNr;
    for ( int k = 0; k < mPointNr; ++k ) {
      double a = k * f;
      while ( a < b ) a += 2*Math.PI;
      a -= b;
      if ( a < eps || a > 2*Math.PI - eps || ( a > Math.PI-eps && a < Math.PI+eps) ) {
        mValue[k] += d * Math.abs( Math.cos( a ) );
      }
    }
  }

  /** @return the maximum accumulator value
   */
  double maxValue()
  {
    double max = 0;
    for ( int k = 0; k < mPointNr; ++k ) if ( mValue[k] > max ) max = mValue[k];
    return max;
  }

}

