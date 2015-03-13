/** @file SelectionSet.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief set of selected drawing items
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------
 * CHANGES
 * 20131120 created
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

// import android.util.Log;

class SelectionSet
{
  int mIndex;   // index of the "hot" item
  SelectionPoint mHotItem; 
  ArrayList< SelectionPoint > mPoints;

  SelectionSet()
  {
    mPoints = new ArrayList< SelectionPoint >();
    reset();
  }

  void reset()
  {
    mIndex = -1;
    mHotItem = null;
  }

  void shiftHotItem( float dx, float dy )
  {
    if ( mHotItem != null ) {
      mHotItem.shiftBy( dx, dy );
    }
  }

  SelectionPoint nextHotItem()
  {
    if ( mPoints.size() > 0 ) {
      mIndex = ( mIndex + 1 ) % mPoints.size();
      mHotItem = mPoints.get( mIndex );
    }
    return mHotItem;
  }

  SelectionPoint prevHotItem()
  {
    if ( mPoints.size() > 0 ) {
      mIndex = ( mIndex + mPoints.size() - 1 ) % mPoints.size();
      mHotItem = mPoints.get( mIndex );
    }
    return mHotItem;
  }

  void addPoint( SelectionPoint pt ) { mPoints.add( pt ); }

  int size() { return mPoints.size(); }

  void clear() 
  { 
    mPoints.clear(); 
    reset();
  }

  // sort the array by the distances and set the "hot" index
  void sort() 
  {
    int size = mPoints.size();
    if ( size > 0 ) {
      for ( int k1 = 0; k1 < size; ++k1 ) {
        for ( int k2 = k1+1; k2 < size; ++k2 ) {
          SelectionPoint p1 = mPoints.get(k1);
          SelectionPoint p2 = mPoints.get(k2);
          if ( p1.mDistance > p2.mDistance ) {
            mPoints.set( k1, p2 ); 
            mPoints.set( k2, p1 );
          }
        }
      }
      mIndex = 0;
      mHotItem = mPoints.get( mIndex );
    } else {
      mHotItem = null;
      mIndex = -1;
    }
  }

}
