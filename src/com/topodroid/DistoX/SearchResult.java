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
package com.topodroid.DistoX;

import android.util.Log;

import java.util.ArrayList;

class SearchResult
{
  private String mName = null;
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
    // Log.v("DistoX-SEARCH", "clear");
    mPos.clear(); mIdx = -1;
  }

  void add( int pos ) { mPos.add( Integer.valueOf(pos) ); }

  int nextPos( )
  {
    // Log.v("DistoX-SEARCH", "size " + mPos.size() + " current index " + mIdx );
    if ( mPos.size() == 0 ) return -1;
    mIdx = ( mIdx + 1 ) % mPos.size(); // move index forward
    return mPos.get( mIdx ).intValue();
  }

  String getName() { return mName; }

  int size() { return mPos.size(); }

  boolean contains( int pos ) 
  {
    // if ( mPos.size() == 0 ) return false; 
    for ( Integer i : mPos ) if ( i.equals( pos ) ) return true;
    return false;
  }

}



