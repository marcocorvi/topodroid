/* @file TriSplay.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid centerline computation: temporary splay shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDMath;
import com.topodroid.TDX.DBlock;

class TriSplay
{
  boolean used;
  String from;   // splay station (usually "from")
  int extend;
  int reversed;  // -1 reversed, +1 normal // PRIVATE
                        // NOTE splay temp-shot can be reversed - leg temp-shot are always normal
                        // this is checked only in makeShotFromTmp to detect errors
  DBlock block;

  TriSplay( DBlock blk, String f, int e, int r )
  { 
    used = false;
    from = f;
    extend = e;
    reversed = r;
    block = blk;
  }

  float d() { return block.mLength; }

  float b( float decl )
  {
    if ( reversed == 1 ) return block.mBearing + decl ;
    // float ret = block.mBearing + decl + 180; if ( ret >= 360 ) ret -= 360;
    return TDMath.add180( block.mBearing + decl );
  }

  float c() { return reversed * block.mClino; }
}
