/** @file NumSplay.java
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
package com.topodroid.DistoX;

/** a splay has the coordinates of the endpoint
 */
class NumSplay extends NumSurveyPoint
{
  public NumStation from;
  private DBlock block;
  private float mDecl;
  float mExtend;

  DBlock getBlock() { return block; }

  NumSplay( NumStation f, float d, float b, float c, float extend, DBlock blk, float decl )
  {
    from = f;
    v = from.v - d * TDMath.sind( c );
    float h0 = d * TDMath.abs( TDMath.cosd( c ) );
    h = from.h + extend * h0;
    s = from.s - h0 * TDMath.cosd( b + decl );
    e = from.e + h0 * TDMath.sind( b + decl );
    block = blk;
    mDecl = decl;
    mExtend = extend;
  }
}
