/** @file TRobot.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey TopoRobot series builder
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
// import java.util.Locale;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;


public class TRobot
{
  ArrayList< TRobotSeries > mSeries;
  ArrayList< TRobotPoint  > mPoints;

  TRobot( List< DistoXDBlock > blks )
  {
    mSeries = new ArrayList< TRobotSeries >();
    mPoints = new ArrayList< TRobotPoint  >();
    int mSrCnt = 0;

    ArrayList< DistoXDBlock > repeat = new ArrayList<DistoXDBlock>();
    for ( DistoXDBlock blk : blks ) {
      if ( blk.mType == DistoXDBlock.BLOCK_MAIN_LEG ) repeat.add( blk );
    }
    // find stations and number of occurrences
    for ( DistoXDBlock blk : repeat ) {
      TRobotPoint pt = getPoint( blk.mFrom );
      if ( pt != null ) { pt.mCnt ++; } else { mPoints.add( new TRobotPoint( 0, blk.mFrom, null ) ); }
      pt = getPoint( blk.mTo );
      if ( pt != null ) { pt.mCnt ++; } else { mPoints.add( new TRobotPoint( 0, blk.mTo, null ) ); }
    }

    while ( repeat.size() > 0 ) {
      TRobotSeries series = null;
      for ( TRobotPoint pt : mPoints ) {
        if ( pt.mCnt == 1 ) {
          ++ mSrCnt;
          series = new TRobotSeries( mSrCnt, pt );
          mSeries.add( series );
          if ( pt.mSeries == null ) {
            pt.mSeries = series;
          }
          pt.mCnt --;
          break;
        }
      }
      if ( series == null ) break;
      boolean added = true;
      while ( added ) { // now grow the series
        added = false;
        for ( DistoXDBlock blk : repeat ) {
          TRobotPoint pfr = getPoint( blk.mFrom );
          TRobotPoint pto = getPoint( blk.mTo );
          if ( series.mEnd == pfr ) {
            pfr.mCnt --;
            pto.mCnt --;
            series.append( pto );
            added = true;
            repeat.remove( blk );
            break;
          } else if ( series.mEnd == pto ) {
            pfr.mCnt --;
            pto.mCnt --;
            series.append( pfr );
            added = true;
            repeat.remove( blk );
            break;
          }
        }
      }
    }
  }
      

  TRobotPoint getPoint( String name ) 
  {
    for ( TRobotPoint pt : mPoints ) {
      if ( pt.mName.equals( name ) ) return pt;
    }
    return null;
  }

  void dump()
  {
    Log.v("DistoX", "TRobot S: " + mSeries.size() + " P: " + mPoints.size() );
    for ( TRobotSeries series : mSeries ) series.dump();
    Log.v("DistoX", "Points");
    for (TRobotPoint pt : mPoints ) pt.dump();
  }

}
