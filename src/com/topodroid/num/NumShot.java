/* @file NumShot.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDMath;
import com.topodroid.Cave3X.SurveyInfo;
import com.topodroid.Cave3X.DBlock;
import com.topodroid.Cave3X.AverageLeg;

import java.util.ArrayList;

public class NumShot
{
  public final NumStation from;
  public final NumStation to;

  final DBlock firstBlock;
  final ArrayList< DBlock > blocks;

  public int mBranchDir; // branch direction
  public int mDirection; // direction of the block (1 same, -1 opposite)
                  // this is used only to decide between barrier and hidden
  NumBranch branch;
  boolean mUsed;  // whether the shot has been used in the station coords recomputation after loop-closure
  public boolean mIgnoreExtend;
  // int mExtend;
  // float mLength;
  // float mBearing;
  // float mClino;
  private AverageLeg mAvgLeg;
  private float mAnomaly;  // local magnetic anomaly

  float length()  { return mAvgLeg.length(); }
  float bearing() { return mAvgLeg.bearing(); }
  float clino()   { return mAvgLeg.clino(); }

  // reset the average leg values
  void reset( float d, float b, float c ) { mAvgLeg.set( d, b, c ); }

  public DBlock getFirstBlock() { return firstBlock; /* blocks.get(0); */ }

  public float getReducedExtend() { return firstBlock.getReducedExtend(); }
  public int getReducedFlag()     { return firstBlock.getReducedFlag(); }
  public String getComment()      { return firstBlock.mComment; }

  NumShot( NumStation f, NumStation t, DBlock blk, int dir, float anomaly, float decl )
  {
    from = f;
    to   = t;
    // block = blk;
    // mIgnoreExtend = ( blk.getIntExtend() == DBlock.EXTEND_IGNORE);
    mIgnoreExtend = !( f.hasExtend() && t.hasExtend() );
    mUsed = false;
    mDirection = dir;
    mBranchDir = 0;
    branch = null;
    blocks = new ArrayList<>();
    blocks.add( blk );
    // mLength  = blk.mLength;
    // mBearing = blk.mBearing;
    // mClino   = blk.mClino;
    mAvgLeg  = new AverageLeg( decl );
    mAvgLeg.set( blk );
    mAnomaly = anomaly;
    // mExtend  = blk.getIntExtend();
    firstBlock = blk;
  }

  void addBlock( DBlock blk )
  {
    int n = blocks.size();
    blocks.add( blk );
    if ( n == 0 ) {
      // mLength  = blk.mLength;
      // mBearing = blk.mBearing;
      // mClino   = mClino;
      mAvgLeg.set( blk );
    } else { // this is not exactly the average vector, but is close enough
      // mLength = (mLength * n + blk.mLength) / (n+1);
      // mClino  = (mClino * n + blk.mClino) / (n+1);
      // float b = TDUtil.around( blk.mBearing, mBearing );
      // mBearing = (mBearing * n  + b ) / (n+1);
      mAvgLeg.add( blk );
    }
    // FIXME DIRECTION
    // if ( mDirection == -1 ) {
    //   compute( from, to ); // compute the coords of "from" from "to"
    // } else {
      compute( to, from ); // compute the coords of "to" from "from"
    // }
  }

  // compute the coords of "st" from those of "sf"
  // N.B. this uses the "reduced extend" which is 0 if the DBlock extend is not set (ie > 1)
  private void compute( NumStation st, NumStation sf )
  {
    float l = length();
    float b = bearing();
    float c = clino();
    // float dv = mLength * TDMath.sin( mClino * TDMath.M_PI / 180 );
    // float dh = mLength * TDMath.cos( mClino * TDMath.M_PI / 180 );
    double dv = l * TDMath.sinDd( c );
    double dh = l * TDMath.cosDd( c );
    st.v = sf.v - dv; // v is downward
    st.h = sf.h + firstBlock.getReducedExtend() * dh;
    // float dn = dh * TDMath.cos( (mBearing-mAnomaly) * TDMath.M_PI / 180 );
    // float de = dh * TDMath.sin( (mBearing-mAnomaly) * TDMath.M_PI / 180 );
    double dn = dh * TDMath.cosDd( b - mAnomaly );
    double de = dh * TDMath.sinDd( b - mAnomaly );
    st.e = sf.e + de;
    st.s = sf.s - dn;
  }

  // float length() { return block.mLength; }

  // boolean connectedTo( NumShot sh ) 
  // {
  //   return sh.from == from || sh.to == from || sh.from == to || sh.to == to;
  // }

}

