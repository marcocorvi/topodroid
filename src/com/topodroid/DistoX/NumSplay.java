/** @file NumSplay.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction splay shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130108 created fron DistoXNum
 */
package com.topodroid.DistoX;

import android.util.FloatMath;

public class NumSplay extends NumSurveyPoint
{
  private static final float grad2rad = TopoDroidUtil.GRAD2RAD;

  public NumStation from;
  private DistoXDBlock block;

  DistoXDBlock getBlock() { return block; }

  NumSplay( NumStation f, float d, float b, float c, float extend, DistoXDBlock blk )
  {
    from = f;
    v = from.v - d * FloatMath.sin(c * grad2rad);
    float h0 = d * TopoDroidUtil.abs( FloatMath.cos(c * grad2rad) );
    h = from.h + extend * h0;
    s = from.s - h0 * FloatMath.cos( b * grad2rad );
    e = from.e + h0 * FloatMath.sin( b * grad2rad );
    block = blk;
  }
}
