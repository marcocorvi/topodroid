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

  void set( DistoXDBlock blk ) { set( blk.mLength, blk.mBearing, blk.mClino ); }
  void add( DistoXDBlock blk ) { add( blk.mLength, blk.mBearing, blk.mClino ); }

  void set( float l, float b, float c ) 
  {
    float cc = TDMath.cosd( c );
    mAverage.x = (l * TDMath.sind(b) * cc );
    mAverage.y = (l * TDMath.cosd(b) * cc );
    mAverage.z = (l * TDMath.sind(c) );
    mCnt = 1;
  }

  // l length
  // b bearing
  // c clino
  void add( float l, float b, float c ) 
  {
    float cc = TDMath.cosd( c );
    mAverage.x += (l * TDMath.sind(b) * cc );
    mAverage.y += (l * TDMath.cosd(b) * cc );
    mAverage.z += (l * TDMath.sind(c) );
    mCnt ++;
  }

  float length() { return mAverage.Length() / mCnt; }

  float bearing() 
  {
    float a = TDMath.atan2d( mAverage.x, mAverage.y );
    if ( a < 0 ) a += 360;
    return a;
  }

  float clino() 
  {
    float h = mAverage.x * mAverage.x + mAverage.y * mAverage.y;
    if ( h == 0 ) return ( mAverage.z > 0 )? 90 : -90;
    return TDMath.atan2d( mAverage.z, TDMath.sqrt( h ) );
  }

}
