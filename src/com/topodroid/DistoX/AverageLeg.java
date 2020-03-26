/* @file AverageLeg.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief average of leg shots
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.math.TDVector;

public class AverageLeg
{
  private TDVector mAverage;
  public  int mCnt;
  public  float mDecl; // magnetic declination

  // AverageLeg( AverageLeg leg ) // deep copy cstr
  // {
  //   mAverage = new TDVector( leg.mAverage );
  //   mCnt  = leg.mCnt;
  //   mDecl = leg.mDecl;
  // }

  public AverageLeg( float decl )
  {
    mAverage = new TDVector( 0, 0, 0 );
    mCnt = 0;
    mDecl = decl;
  }

  public void reset() {
    mAverage.x = 0;
    mAverage.y = 0;
    mAverage.z = 0;
    mCnt = 0;
  }

  public void set( DBlock blk ) { set( blk.mLength, blk.mBearing, blk.mClino ); }
  public void add( DBlock blk ) { add( blk.mLength, blk.mBearing, blk.mClino ); }

  public void set( float l, float b, float c ) 
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
  public void add( float l, float b, float c ) 
  {
    float cc = TDMath.cosd( c );
    mAverage.x += (l * TDMath.sind(b) * cc );
    mAverage.y += (l * TDMath.cosd(b) * cc );
    mAverage.z += (l * TDMath.sind(c) );
    mCnt ++;
  }

  // void reverse()
  // { 
  //   mAverage.x = - mAverage.x;
  //   mAverage.y = - mAverage.y;
  //   mAverage.z = - mAverage.z;
  // }

  public float length() { return mAverage.Length() / mCnt; }

  public float bearing() { return TDMath.in360( TDMath.atan2d( mAverage.x, mAverage.y ) + mDecl ); }

  public float clino() 
  {
    float h = mAverage.x * mAverage.x + mAverage.y * mAverage.y;
    if ( h == 0 ) return ( mAverage.z > 0 )? 90 : -90;
    return TDMath.atan2d( mAverage.z, TDMath.sqrt( h ) );
  }

}
