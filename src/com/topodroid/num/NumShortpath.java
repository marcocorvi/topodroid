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

class NumShortpath
{
  NumShortpath mFrom; // link to the previous shortpath
  NumStation mStation;
  int     mNr;    // number of segments in this short path
  float   mDist;  // loop closure distance (shortest-path algo)
  float   mDist2; // loop closure squared distance (shortest-path algo)

  // create a short path - when created the shortpath is unlinked
  NumShortpath( NumStation station, int n, float d, float d2 )
  {
    mFrom    = null;
    mStation = station;
    mNr    = n;
    mDist  = d;
    mDist2 = d2;
  }

  // get the name of the station
  String getName() { return (mStation == null)? "--" : mStation.name; }

  // reset the values of this short-path
  // @param from previous link
  // @param n   number of segments
  // @param d   length = sum of segment lengths
  // @param d2  sum of segment square lengths
  void resetShortpath( NumShortpath from, int n, float d, float d2 )
  {
    mFrom  = from;
    mNr    = n;
    mDist  = d;
    mDist2 = d2;
  }
}
