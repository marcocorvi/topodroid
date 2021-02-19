/* @file NumSplay.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction splay shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDMath;
import com.topodroid.DistoX.DBlock;

/** a splay has the coordinates of the endpoint
 */
public class NumSplay extends NumSurveyPoint
{
  public final NumStation from;
  private DBlock mBlock;
  private float  mDecl;
  private float  mCosine;

  public DBlock getBlock() { return mBlock; }

  public float getCosine() { return mCosine; }
  public int getReducedFlag() { return mBlock.getReducedFlag(); }
  public String getComment()  { return mBlock.mComment; }

  NumSplay( NumStation f, float d, float b, float c, float cosine, DBlock blk, float decl )
  {
    from = f;
    v = from.v - d * TDMath.sind( c );
    double h0 = d * Math.abs( TDMath.cosDd( c ) );
    h = from.h + cosine * h0;
    s = from.s - h0 * TDMath.cosDd( b + decl );
    e = from.e + h0 * TDMath.sinDd( b + decl );
    mBlock  = blk;
    mDecl   = decl;
    mCosine = cosine;
  }

}
