/** @file TRobotPoint.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey TopoRobot point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

class TRobotPoint
{
  int mNumber;  // TopoRobot point number
  String mName; // TopoDroid station name
  TRobotSeries mSeries;
  int mCnt;     // number of TopoDroid legs with this station (used building series)
  DBlock mBlk; // shot that leads to this point in the series
  boolean mForward;  // this point is block's TO station

  TRobotPoint( int nr, String nm, TRobotSeries s )
  {
    mNumber = nr;
    mName   = nm;
    mSeries = s;
    mCnt    = 1;
    mBlk    = null;
    mForward = false;
  }

  // void dump()
  // {
    // Log.v("DistoX", "P " + mSeries.mNumber + "." + mNumber + " <" + mName + ">" );
  // }
}
