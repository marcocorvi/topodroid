/* @file SelectionRange.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief a line-range, used in a selection point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

class SelectionRange
{
  static final int RANGE_POINT = 0; // point - no range
  static final int RANGE_SOFT  = 1; // smooth ends
  static final int RANGE_HARD  = 2; // hard-cut ends

  private int mRangeType; // range type
  private LinePoint mLP1 = null;  // range endpoints
  private LinePoint mLP2 = null;
  private float mD1;      // range endpoint distances
  private float mD2;
  
  SelectionRange( int type, LinePoint lp1, LinePoint lp2, float d1, float d2 )
  {
    mRangeType = type;
    mLP1 = lp1;
    mLP2 = lp2;
    mD1  = d1;
    mD2  = d2;
  }

  static int rotateType( int type ) { return (type+1)%3; }

  LinePoint start() { return mLP1; }
  LinePoint end()   { return mLP2; }

  void setStartDistance( float d ) { mD1 = d; }
  void setEndDistance( float d )   { mD2 = d; }
  float startDistance() { return mD1; }
  float endDistance()   { return mD2; }

  static boolean isPoint(int type) { return type == RANGE_POINT; }
  boolean isPoint() { return mRangeType == RANGE_POINT; }
  boolean isSoft() { return mRangeType == RANGE_SOFT; }
  // boolean isHard() { return mRangeType == RANGE_HARD; }

}
