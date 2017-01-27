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

// import java.util.List;
// import java.util.Locale;
// import java.util.ArrayList;

import android.util.Log;


public class TRobotPoint
{
  int mNumber;  // TopoRobot point number
  String mName; // TopoDroid station name
  TRobotSeries mSeries;
  int mCnt;     // number of TopoDroid legs with this station (used building series)

  TRobotPoint( int nr, String nm, TRobotSeries s )
  {
    mNumber = nr;
    mName   = nm;
    mSeries = s;
    mCnt    = 1;
  }

  void dump()
  {
    Log.v("DistoX", "P " + mSeries.mNumber + "." + mNumber + " <" + mName + ">" );
  }
}
