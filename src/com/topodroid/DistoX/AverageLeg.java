/** @file AverageLeg.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief average of leg shots
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.FloatMath;

class AverageLeg
{
  Vector mAverage;
  int mCnt;

  AverageLeg()
  {
    mAverage = new Vector( 0, 0, 0 );
    mCnt = 0;
  }

  void reset() {
    mAverage.x = 0;
    mAverage.y = 0;
    mAverage.z = 0;
    mCnt = 0;
  }

  void set( float l, float b, float c ) 
  {
    float g2r = TopoDroidUtil.GRAD2RAD;
    float cc = FloatMath.cos( c*g2r );
    mAverage.x = (l * FloatMath.sin(b*g2r) * cc );
    mAverage.y = (l * FloatMath.cos(b*g2r) * cc );
    mAverage.z = (l * FloatMath.sin(c*g2r) );
    mCnt = 1;
  }

  // l length
  // b bearing
  // c clino
  void add( float l, float b, float c ) 
  {
    float g2r = TopoDroidUtil.GRAD2RAD;
    float cc = FloatMath.cos( c*g2r );
    mAverage.x += (l * FloatMath.sin(b*g2r) * cc );
    mAverage.y += (l * FloatMath.cos(b*g2r) * cc );
    mAverage.z += (l * FloatMath.sin(c*g2r) );
    mCnt ++;
  }

  float length() { return mAverage.Length() / mCnt; }

  float bearing() 
  {
    float a = TopoDroidUtil.RAD2GRAD * TopoDroidUtil.atan2( mAverage.x, mAverage.y );
    if ( a < 0 ) a += 360;
    return a;
  }

  float clino() 
  {
    float h = mAverage.x * mAverage.x + mAverage.y * mAverage.y;
    if ( h == 0 ) return ( mAverage.z > 0 )? 90 : -90;
    h = FloatMath.sqrt( h );
    return TopoDroidUtil.RAD2GRAD * TopoDroidUtil.atan2( mAverage.z, h );
  }

}
