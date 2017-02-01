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

    if ( TDSetting.mTRobotNames && TDSetting.mTRobotShot ) {
      populateSeries( blks );
    } else {
      buildSeries( blks );
    }
  }

  private void populateSeries( List<DistoXDBlock> blks )
  {
    TRobotSeries sf, st;
    TRobotPoint pf, pt;
    int nsf, npf, nst, npt;
    for ( DistoXDBlock blk : blks ) {
      if ( blk.mType != DistoXDBlock.BLOCK_MAIN_LEG ) continue;
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
      }   
    }
  }
    

  private void buildSeries( List<DistoXDBlock> blks )
  {
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

  TRobotPoint getPoint( TRobotSeries sr, int np )
  {
    if ( sr == null ) return null;
    for ( TRobotPoint pt : mPoints ) {
      if ( np == pt.mNumber && sr == pt.mSeries ) return pt;
    }
    return null;
  }

  TRobotSeries getSeries( int ns )
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

  void dump()
  {
    Log.v("DistoX", "TRobot S: " + mSeries.size() + " P: " + mPoints.size() );
    for ( TRobotSeries series : mSeries ) series.dump();
    Log.v("DistoX", "Points");
    for (TRobotPoint pt : mPoints ) pt.dump();
  }

}
