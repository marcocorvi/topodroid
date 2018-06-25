/* @file StationSearch.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid result of the search for a station name
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class StationSearch
{
  private String mName = null;
  private int[] mPos = null; // result position of station search
  private int   mIdx = 0;    // current index in the array

  void set( String name, int[] pos )
  {
    mName = name;
    mPos  = pos;
    mIdx  = 0;
  }

  void reset() { set(null, null); }

  int nextPos( )
  {
    if ( mPos == null || mPos.length == 0 ) return -1;
    int ret = mPos[ mIdx ];
    mIdx = ( mIdx + 1 ) % mPos.length; // move index forward
    return ret;
  }

  String getName() { return mName; }

  // int getSize() { return ( mPos == null )? 0 : mPos.length; }

}



