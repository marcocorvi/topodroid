/* @file TriShot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid centerline computation: temporary shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.util.List;

public class TriShot
{
  boolean used;
  public String from;
  public String to;
  public int extend;
  public int reversed;  // -1 reversed, +1 normal 
                        // NOTE splay temp-shot can be reversed - leg temp-shot are always normal
                        // this is checked only in makeShotFromTmp to detect errors
  public boolean duplicate;
  public boolean surface;
  public int     backshot; // 0 forward, +1 sibling forward, -1 sibling backshot
  public TriShot sibling;  // sibling shot with same stations
  public ArrayList<DistoXDBlock> blocks;
  AverageLeg mAvgLeg;
  TriCluster cluster;

  public TriShot( DistoXDBlock blk, String f, String t, int e, int r )
  { 
    used = false;
    from = f;
    to   = t;
    extend = e;
    reversed = r;
    duplicate = false;
    surface   = false;
    backshot  = 0;
    sibling = null;
    blocks = new ArrayList<DistoXDBlock>();
    blocks.add( blk );
    mAvgLeg = new AverageLeg( 0.0f ); // temporary shot do not consider declination
    mAvgLeg.set( blk );
    cluster = null;
  }

  double length()  { return mAvgLeg.length(); } 
  double bearing() { return mAvgLeg.bearing(); } 
  double clino()   { return mAvgLeg.clino(); } 

  void addBlock( DistoXDBlock blk )
  {
    blocks.add( blk );
    mAvgLeg.add( blk );
  }

  DistoXDBlock getFirstBlock( ) { return blocks.get(0); }

  ArrayList<DistoXDBlock> getBlocks() { return blocks; }

  /** get the temp-shot distance
   * @note if the temp-shot is reversed the distance is negative
   */
  float d()
  {
    // float ret = 0.0f;
    // for ( DistoXDBlock b : blocks ) ret += b.mLength; 
    // return ret / blocks.size();
    return mAvgLeg.length();
  }

  float b()
  {
    // DistoXDBlock blk = blocks.get(0);
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
    if ( reversed == -1 ) { ret += 180; if (ret >= 360) ret -= 360; }
    return ret;
  }

  float c()
  {
    // float ret = 0.0f;
    // if ( blocks.size() == 1 ) {
    //   return reversed * blocks.get(0).mClino;
    // }
    // for ( DistoXDBlock b : blocks ) ret += b.mClino;
    // return ret / blocks.size();

    return reversed * mAvgLeg.clino(); 
  }

  // void Dump()
  // {
  //   Log.v( TDLog.TAG, "Shot " + from + "-" + to + " " + d() + " " + b() + " " + c() );
  //   for ( DistoXDBlock b : blocks ) {
  //     Log.v( TDLog.TAG, b.mLength + " " + b.mBearing + " " + b.mClino );
  //   }
  // }
}
