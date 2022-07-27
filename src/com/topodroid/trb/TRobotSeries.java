/* @file TRobotSeries.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey TopoRobot series 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.trb;

// import com.topodroid.utils.TDLog;

import java.util.ArrayList;

public class TRobotSeries
{
  private int mPtCnt;        // point counter
  public int mNumber;        // TopoRobot series number
  public TRobotPoint mBegin; // start point
  public TRobotPoint mEnd;
  public ArrayList< TRobotPoint > mPoints; // other points
  
  TRobotSeries( int nr, TRobotPoint pt )
  {
    mPtCnt  = 0;
    mNumber = nr;
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

  // void dump( )
  // {
  //   tdlog.v( "Series " + mNumber + " (Pts " + mPtCnt + ") start " + mBegin.mSeries.mNumber + "." + mBegin.mNumber );
  //   for ( TRobotPoint pt : mPoints ) pt.dump();
  // }
}
