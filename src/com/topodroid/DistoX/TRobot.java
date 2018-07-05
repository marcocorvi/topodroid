/* @file TRobot.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey TopoRobot series builder
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
// import java.util.Locale;
import java.util.ArrayList;
// import java.util.Iterator;

// import android.util.Log;

class TRobot
{
  ArrayList< TRobotSeries > mSeries;
  ArrayList< TRobotPoint  > mPoints;

  TRobot( List< DBlock > blks )
  {
    mSeries = new ArrayList<>();
    mPoints = new ArrayList<>();

    boolean done = false;
    if ( TDSetting.doTopoRobot() ) {
      done = populateSeries( blks );
    }
    if ( ! done ) {
      buildSeries( blks );
    }
  }

  private boolean populateSeries( List<DBlock> blks )
  {
    TRobotSeries sf, st;
    TRobotPoint pf, pt;
    int nsf, npf, nst, npt;
    for ( DBlock blk : blks ) {
      if ( ! blk.isLeg() ) continue;
      String[] valf = blk.mFrom.split(".");
      String[] valt = blk.mTo.split(".");
      if ( valf.length != 2 || valt.length != 2 ) continue;
      try {
        nsf = Integer.parseInt(valf[0]);
        npf = Integer.parseInt(valf[1]);
        nst = Integer.parseInt(valt[0]);
        npt = Integer.parseInt(valt[1]);
        sf = getSeries( nsf );
        st = getSeries( nst );
        pf = getPoint( sf, npf );
        pt = getPoint( st, npt );
        if ( st != null ) { 
          if ( sf == null ) {
            // assert( pt != null )
            sf = new TRobotSeries( nsf, pt );
            pf = new TRobotPoint( npf, blk.mFrom, sf );
            sf.append( pf );
            pf.mBlk = blk;
            pf.mForward = false;
          } else if ( sf == st ) { 
            if ( pt == null ) {
              pt = new TRobotPoint( npt, blk.mTo, st );
              st.append( pt );
              pt.mBlk = blk;
              pt.mForward = true;
            } else if ( pf == null ) {
              pf = new TRobotPoint( npf, blk.mFrom, sf );
              sf.append( pf );
              pf.mBlk = blk;
              pf.mForward = false;
            } else {
              TDLog.Error("TRobot " + blk.Name() + " closes series " + sf.mNumber );
            }
          } else { 
            TDLog.Error("TRobot " + blk.Name() + " joins " + sf.mNumber + " " + st.mNumber );
          }
        } else if ( sf != null ) { // st == null
          // assert ( pf != null )
          st = new TRobotSeries( nst, pf );
          pt = new TRobotPoint( npt, blk.mTo, st );
          st.append( pt );
          pt.mBlk = blk;
          pt.mForward = true;
        } else { // st == null and sf == null
          TDLog.Error("TRobot unattached block " + blk.Name() );
        }
      } catch ( NumberFormatException e ) {
        TDLog.Error("TRobot " + e.getMessage() );
        mSeries.clear();
        mPoints.clear();
        return false;
      }   
    }
    return true;
  }
    

  private void buildSeries( List<DBlock> blks )
  {
    int mSrCnt = 0;
    ArrayList< DBlock > repeat = new ArrayList<>();
    for ( DBlock blk : blks ) {
      if ( blk.isLeg() ) repeat.add( blk );
    }
    // find stations and number of occurrences
    for ( DBlock blk : repeat ) {
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
        for ( DBlock blk : repeat ) {
          TRobotPoint pfr = getPoint( blk.mFrom );
          TRobotPoint pto = getPoint( blk.mTo );
          if ( series.mEnd == pfr ) {
            pfr.mCnt --;
            pto.mCnt --;
            series.append( pto );
            pto.mBlk = blk;
            pto.mForward = true;
            added = true;
            repeat.remove( blk );
            break;
          } else if ( series.mEnd == pto ) {
            pfr.mCnt --;
            pto.mCnt --;
            series.append( pfr );
            pto.mBlk = blk;
            pto.mForward = false;
            added = true;
            repeat.remove( blk );
            break;
          }
        }
      }
    }
  }

  private TRobotPoint getPoint(TRobotSeries sr,int np)
  {
    if ( sr == null ) return null;
    for ( TRobotPoint pt : mPoints ) {
      if ( np == pt.mNumber && sr == pt.mSeries ) return pt;
    }
    return null;
  }

  private TRobotSeries getSeries(int ns)
  {
    for ( TRobotSeries sr : mSeries ) {
      if ( ns == sr.mNumber ) return sr;
    }
    return null;
  }
      

  TRobotPoint getPoint( String name ) 
  {
    for ( TRobotPoint pt : mPoints ) {
      if ( pt.mName.equals( name ) ) return pt;
    }
    return null;
  }

  // void dump()
  // {
    // Log.v("DistoX", "TRobot S: " + mSeries.size() + " P: " + mPoints.size() );
    // for ( TRobotSeries series : mSeries ) series.dump();
    // Log.v("DistoX", "Points");
    // for (TRobotPoint pt : mPoints ) pt.dump();
  // }

}
