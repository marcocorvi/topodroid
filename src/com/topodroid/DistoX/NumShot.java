/** @file NumShot.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created fron DistoXNum
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

public class NumShot 
{
  NumStation from;
  NumStation to;
  // DistoXDBlock block;
  ArrayList<DistoXDBlock> blocks;
  int mBranchDir; // branch direction
  int mDirection; // direction of the block (1 same, -1 opposite)
  NumBranch branch;
  boolean mUsed;  // whether the shot has been used in the station coords recomputation after loop-closure
  boolean mIgnoreExtend;
  int mExtend;
  float mLength;
  float mBearing;
  float mClino;

  DistoXDBlock getFirstBlock() { return blocks.get(0); }

  NumShot( NumStation f, NumStation t, DistoXDBlock blk, int dir )
  {
    from = f;
    to   = t;
    // block = blk;
    mIgnoreExtend = ( blk.mExtend == DistoXDBlock.EXTEND_IGNORE);
    mUsed = false;
    mDirection = dir;
    mBranchDir = 0;
    branch = null;
    blocks = new ArrayList<DistoXDBlock>();
    blocks.add( blk );
    mLength  = blk.mLength;
    mBearing = blk.mBearing;
    mClino   = blk.mClino;
    mExtend  = (int)(blk.mExtend);
  }

  boolean reversed() { return (mDirection == -1); }

  // boolean isRecent( long id ) { return blocks.get(0) != null && blocks.get(0).isRecent( id ); }

  void addBlock( DistoXDBlock blk )
  {
    int n = blocks.size();
    blocks.add( blk );
    if ( n == 0 ) {
      mLength  = blk.mLength;
      mBearing = blk.mBearing;
      mClino   = mClino;
    } else {
      mLength = (mLength * n + blk.mLength) / (n+1);
      mClino  = (mClino * n + blk.mClino) / (n+1);
      float b = TopoDroidUtil.around( blk.mBearing, mBearing );
      mBearing = (mBearing * n  + b ) / (n+1);
    }
    if ( mDirection == -1 ) {
      compute( from, to ); // compute the coords of "from" from "to"
    } else {
      compute( to, from ); // compute the coords of "to" from "from"
    }
  }

  // compute the coords of "st" from those of "sf"
  void compute( NumStation st, NumStation sf )
  {
    float dv = mLength * (float)Math.sin( mClino * TopoDroidUtil.M_PI / 180 );
    float dh = mLength * (float)Math.cos( mClino * TopoDroidUtil.M_PI / 180 );
    st.v = sf.v - dv; // v is downward
    st.h = sf.h + mExtend * dh;
    float dn = dh * (float)Math.cos( mBearing * TopoDroidUtil.M_PI / 180 );
    float de = dh * (float)Math.sin( mBearing * TopoDroidUtil.M_PI / 180 );
    st.e = sf.e + de;
    st.s = sf.s - dn;
  }


  // float length() { return block.mLength; }

  // boolean connectedTo( NumShot sh ) 
  // {
  //   return sh.from == from || sh.to == from || sh.from == to || sh.to == to;
  // }

}

