/* @file TdmViewStationBucket.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager drawing surface (canvas)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */

package com.topodroid.tdm;

import com.topodroid.util.TDLog;

import android.graphics.RectF;

import java.util.ArrayList;

class TdmViewStationBucket
{
  ArrayList< TdmViewStation > mStations;
  float xmin, xmax;
  float ymin, ymax;

  TdmViewStationBucket( float x1, float x2, float y1, float y2 )
  {
    xmin = x1;
    xmax = x2;
    ymin = y1; 
    ymax = y2;
    mStations = new ArrayList< TdmViewStation >();
  }

  void shift( float dx, float dy )
  {
    xmin += dx;
    xmax += dx;
    ymin += dy;
    ymax += dy;
  }

  void insertStation( TdmViewStation st )
  { 
    // check st.x and st.y in bounds
    mStations.add( st );
  }


  /** @return true if this bucket contains a point
   * @param x  X coord
   * @param y  Y coord
   */
  boolean coverPoint( float x, float y )
  {
    return ( x > xmin && x < xmax && y > ymin && y < ymax );
  }

  boolean intersects( float x1, float x2, float y1, float y2 )
  {
    if ( x2 < xmin ) return false;
    if ( x1 > xmax ) return false;
    if ( y2 < ymin ) return false;
    if ( y1 > ymax ) return false;
    return true;
  }

  boolean intersects( RectF bbox )
  {
    if ( bbox.right  < xmin ) return false;
    if ( bbox.left   > xmax ) return false;
    if ( bbox.bottom < ymin ) return false;
    if ( bbox.top    > ymax ) return false;
    return true;
  }


}
