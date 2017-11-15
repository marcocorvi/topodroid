/** @file TRobotSeries.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey TopoRobot series 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.util.Log;


public class TRobotSeries
{
  int mNumber;       // TopoRobot series number
  private int mPtCnt;  // point counter
  TRobotPoint mBegin; // start point
  ArrayList< TRobotPoint > mPoints; // other points
  TRobotPoint mEnd;
  
  TRobotSeries( int nr, TRobotPoint pt )
  {
    mNumber = nr;
    mPtCnt  = 0;
    mPoints = new ArrayList<>();
    mBegin  = pt;
    mEnd    = pt;
  }

  int size() { return mPoints.size(); } // mPtCnt

  void append( TRobotPoint pt )
  {
    mPtCnt ++;
    pt.mNumber = mPtCnt;
    pt.mSeries = this;
    mEnd = pt;
    mPoints.add( pt );
  }

  void dump()
  {
    Log.v("DistoX", "Series " + mNumber + " (Pts " + mPtCnt + ") start " + mBegin.mSeries.mNumber + "." + mBegin.mNumber );
    for ( TRobotPoint pt : mPoints ) pt.dump();
  }
}
