/* @file TriShot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid centerline computation: temporary shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.AverageLeg;
import com.topodroid.TDX.DBlock;
import com.topodroid.TDX.SurveyInfo;

import java.util.ArrayList;
// import java.util.List;
import java.util.UUID;


class TriShot
{
  boolean used;
  String from;
  String to;
  int extend;
  float stretch;
  int reversed;  // -1 reversed, +1 normal
                        // NOTE splay temp-shot can be reversed - leg temp-shot are always normal
                        // this is checked only in makeShotFromTmp to detect errors
  boolean duplicate;
  boolean surface;
  boolean commented;
  int     backshot; // 0 forward, +1 sibling forward, -1 sibling backshot
  TriShot sibling;  // sibling shot with same stations
  ArrayList< DBlock > blocks;
  AverageLeg mAvgLeg;
  TriCluster cluster;
  UUID triangle;

  // void dump()
  // {
  //   DBlock b = blocks.get(0);
  //   
  //   TDLog.v( from + "-" + to + 
  //     String.format( " blk %6.2f %6.2f  leg %6.2f %6.2f  decl %6.2f", 
  //       b.mLength, b.mBearing, mAvgLeg.length(), mAvgLeg.bearing(), mAvgLeg.mDecl )
  //   );
  // }

  /** cstr
   * @param blk   DBlock
   * @param f     FROM station
   * @param t     TO station
   * @param e     extend
   * @param s     stretch
   * @param r     ???
   */
  TriShot( DBlock blk, String f, String t, int e, float s, int r )
  { 
    used = false;
    from = f;
    to   = t;
    extend  = e;
    stretch = s;
    reversed = r;
    duplicate = false;
    surface   = false;
    commented = false;
    backshot  = 0;
    sibling = null;
    blocks = new ArrayList<>();
    blocks.add( blk );
    mAvgLeg = new AverageLeg( 0.0f ); // temporary shot do not consider declination
    mAvgLeg.set( blk );
    cluster = null;
    triangle = null;
  }

  /** @return the shot length
   */
  double length()  { return mAvgLeg.length(); } 

  /** @return the shot azimuth
   */
  double bearing() { return mAvgLeg.bearing(); } 

  /** @return the shot inclination
   */
  double clino()   { return mAvgLeg.clino(); } 

  /** @return the (integer) shot extend
   */
  int   getIntExtend()   { return extend; }

  /** @return the (float) shot extend, ie, integer part plus fractional part
   */
  float getFloatExtend() { return extend + stretch; }

  /** add a data to this shot
   * @param blk data to add
   */
  void addBlock( DBlock blk )
  {
    blocks.add( blk );
    mAvgLeg.add( blk );
  }

  /** @return the fisrt data in this shot
   */
  DBlock getFirstBlock( ) { return blocks.get(0); }

  /** @return the array of data in this shot
   */
  ArrayList< DBlock > getBlocks() { return blocks; }

  /** @return the shot reduction type: 1 SURVEY, 2 SURFACE
   */
  int getReductionType() { return ( blocks.get(0).isSurface() )? NumStation.STATION_SURFACE : NumStation.STATION_SURVEY; }

  /** get the temp-shot distance
   * note if the temp-shot is reversed the distance is negative
   */
  float d()
  {
    // float ret = 0.0f;
    // for ( DBlock b : blocks ) ret += b.mLength; 
    // return ret / blocks.size();
    return mAvgLeg.length();
  }

  // horizontal length (good only for DATAMODE_NORMAL)
  double h()
  {
    double hh = mAvgLeg.length() * TDMath.cosDd( mAvgLeg.clino() );
    // if ( hh < 0 ) {
    //   TDLog.v( "TRI block " + blocks.get(0).mId + " neg H " + hh );
    // }
    return hh;
    // return mAvgLeg.length() * TDMath.cosd( mAvgLeg.clino() );
  }

  // vertical length - with sign (only for DATAMODE_NORMAL)
  // depth           for DATAMODE_DIVING
  double v()
  {
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_NORMAL ) {
      return mAvgLeg.length() * TDMath.sinDd( mAvgLeg.clino() );
    } else { // SurveyInfo.DATAMODE_DIVING
      return mAvgLeg.clino();
    }
  }


  float b()
  {
    // DBlock blk = blocks.get(0);
    // int size = blocks.size();
    // float b0 = blk.mBearing;
    // if ( size == 1 ) {
    //   return (reversed == -1)? TDMath.in360(b0+180) : b0;
    // }
    // float ret = b0;
    // for ( int k=1; k<size; ++k ) {
    //   blk = blocks.get(k);
    //   ret += TDMath.around( blk.mBearing , b0 );
    // }
    // return TDMath.in360( ret/size );

    float ret = mAvgLeg.bearing();
    if ( reversed == -1 ) { 
      // ret += 180; if (ret >= 360) ret -= 360;
      ret = TDMath.add180( ret );
    }
    return ret;
  }

  /** 
   * @return clino if DATAMODE_NORMAL
   *         depth if DATAMODE_DIVING
   */
  float c()
  {
    // float ret = 0.0f;
    // if ( blocks.size() == 1 ) {
    //   return reversed * blocks.get(0).mClino;
    // }
    // for ( DBlock b : blocks ) ret += b.mClino;
    // return ret / blocks.size();

    return ( reversed * mAvgLeg.clino() );
  }

  // void Dump()
  // {
  //   // TDLog.v( "TRI shot " + from + "-" + to + " " + d() + " " + b() + " " + c() );
  //   // for ( DBlock b : blocks ) {
  //   //   // TDLog.v( b.mLength + " " + b.mBearing + " " + b.mClino );
  //   // }
  // }

  String name() {
    return TriShot.name( from, to );
	}

  static String name( String from, String to ) {
    return from.compareTo(to) < 0 ? from + '|' + to : to + '|' + from;
  }
}
