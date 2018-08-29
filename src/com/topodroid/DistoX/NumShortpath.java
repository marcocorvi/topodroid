/* @file NumShortpath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction shortest path (loop closure error)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

class NumShortpath
{
  float   mDist;  // loop closure distance (shortest-path algo)
  float   mDist2; // loop closure squared distance (shortest-path algo)

  NumShortpath()
  {
    mDist  = 0;
    mDist2 = 0;
  }

  NumShortpath( float d, float d2 )
  {
    mDist  = d;
    mDist2 = d2;
  }

  NumShortpath add( float len )
  {
    return new NumShortpath( mDist+len, mDist2+len*len );
  }

  void reset( float d, float d2 )
  {
    mDist  = d;
    mDist2 = d2;
  }
}
