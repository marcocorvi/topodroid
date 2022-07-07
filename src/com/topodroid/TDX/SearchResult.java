/* @file SearchResult.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid result of the search for a station name or a leg-flag
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;

import java.util.ArrayList;

class SearchResult
{
  private String mName; // = null;
  private int   mIdx;    // current result-index in the array
  private ArrayList< Integer > mPos;

  SearchResult()
  {
    mName = null;
    mIdx  = -1;
    mPos  = new ArrayList< Integer >();
  }

  ArrayList< Integer > getPositions() { return mPos; }

  void reset( String name )
  {
    mName = name;
    mPos.clear();
    mIdx  = -1;
  }

  void clearSearch()
  { 
    // TDLog.v( "clear");
    mPos.clear(); mIdx = -1;
  }

  void add( int pos ) { mPos.add( Integer.valueOf(pos) ); }

  int nextPos( )
  {
    // TDLog.v( "size " + mPos.size() + " current index " + mIdx );
    if ( TDUtil.isEmpty(mPos) ) return -1;
    mIdx = ( mIdx + 1 ) % mPos.size(); // move index forward
    return mPos.get( mIdx ).intValue();
  }

  String getName() { return mName; }

  int size() { return mPos.size(); }

  boolean contains( int pos ) 
  {
    // if ( TDUtil.isEmpty(mPos) ) return false; 
    for ( Integer i : mPos ) if ( i.equals( pos ) ) return true;
    return false;
  }

}



