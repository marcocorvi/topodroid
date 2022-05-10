/* @file TRobotPoint.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey TopoRobot point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.trb;

import com.topodroid.TDX.DBlock;

// import com.topodroid.utils.TDLog;

public class TRobotPoint
{
  public int     mNumber;  // TopoRobot point number
  public String  mName;    // TopoDroid station name
  public TRobotSeries mSeries;
  public int     mCnt;     // number of TopoDroid legs with this station (used building series)
  public DBlock  mBlk;     // shot that leads to this point in the series
  public boolean mForward; // this point is block's TO station

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
    // TDLog.v( "P " + mSeries.mNumber + "." + mNumber + " <" + mName + ">" );
  // }
}
