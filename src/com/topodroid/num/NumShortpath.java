/* @file NumShortpath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction shortest path (loop closure error)
 *        stores the sum of the lengths
 *        and the sum of the squared lengths
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

// import android.util.Log;

class NumShortpath
{
  int     mNr;    // number of segments in this short path
  float   mDist;  // loop closure distance (shortest-path algo)
  float   mDist2; // loop closure squared distance (shortest-path algo)

  NumShortpath()
  {
    mNr    = 0;
    mDist  = 0;
    mDist2 = 0;
  }

  // start a short path 
  NumShortpath( int n, float d, float d2 )
  {
    mNr    = n;
    mDist  = d;
    mDist2 = d2;
  }

  // create a new short path adding a segmentto this 
  // @param length    segment length
  NumShortpath addSegment( float len )
  {
    return new NumShortpath( mNr+1, mDist+len, mDist2+len*len );
  }

  // reset the values of this short-path
  // @param n   number of segments
  // @param d   length = sum of segment lengths
  // @param d2  sum of segment square lengths
  void resetShortpath( int n, float d, float d2 )
  {
    mNr    = n;
    mDist  = d;
    mDist2 = d2;
  }
}
