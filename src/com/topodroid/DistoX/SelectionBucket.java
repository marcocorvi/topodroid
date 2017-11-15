/* @file Selection.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief SelectionBucket a rectangle of selection points
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.LinkedList;
// import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.RectF;

import android.util.Log;

class SelectionBucket
{
  float x0, y0, x1, y1; // bounds
  ArrayList< SelectionPoint > mPoints;

  SelectionBucket( float _x0, float _y0, float _x1, float _y1 )
  {
    x0 = _x0;
    y0 = _y0;
    x1 = _x1;
    y1 = _y1;
    mPoints = new ArrayList<>();
  }

  // check if this bucket intersect a rectangle ( _x0 < _x1 and _y0 < _y1 )
  // boolean intersect( float _x0, float _y0, float _x1, float _y1 )
  // {
  //   if ( _x1 < x0 || x1 < _x0 ) return false;
  //   if ( _y1 < y0 || y1 < _y0 ) return false;
  //   return true;
  // }
 
  boolean intersects( RectF bbox )
  { 
    if ( bbox == null ) return true;
    if ( ( bbox.right  < x0 ) 
      || ( bbox.left   > x1 ) 
      || ( bbox.top    > y1 ) 
      || ( bbox.bottom < y0 ) ) return false;
    return true;
  }

  void addPoint( SelectionPoint pt ) { mPoints.add( pt ); }

  void removePoint( SelectionPoint pt ) { mPoints.remove( pt ); }

  int size() { return mPoints.size(); }

  // strict contains (S) and extended contains (E+S)
  //
  //  ___|___|___|___|_
  //     | E | E | E |
  //  ___|___|___|___|_
  //     | E | S | E |
  //  ___|___|___|___|_
  //     | E | E | E |
  //  ___|___|___|___|_
  //     |   |   |   |
  //
  boolean contains( float x, float y ) { return x >= x0 && x < x1 && y >= y0 && y < y1; }

  boolean contains( float x, float y, float dx, float dy ) 
  { return x >= x0-dx && x < x1+dx && y >= y0-dy && y < y1+dy; }


  // void dump()
  // {
  //   Log.v("DistoX", "B " + size() + " " + x0 + ":" + x1 + " " + y0 + ":" + y1 );
  // }
}
