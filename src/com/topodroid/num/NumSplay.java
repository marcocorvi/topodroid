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
import com.topodroid.TDX.DBlock;

/** a splay has the coordinates of the endpoint
 */
public class NumSplay extends NumSurveyPoint
{
  public final NumStation from;
  private DBlock mBlock;
  private float  mDecl;
  private float  mCosine;

  /** @return the data block
   */
  public DBlock getBlock() { return mBlock; }

  /** @return the cosine that the splay makes with the reference leg
   */
  public float getCosine() { return mCosine; }

  /** @return the flag of the data blcok
   */
  public int getReducedFlag() { return mBlock.getReducedFlag(); }

  /** @return the comment of the data blcok
   */
  public String getComment()  { return mBlock.mComment; }

  /** cstr
   * @param f    FROM station
   * @param d    splay length
   * @param b    splay azimuth
   * @param c    splay clino
   * @param cosine cosine with the reference leg (for the extended profile)
   * @param blk  splay data block
   * @param decl magnetic declination
   */
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
