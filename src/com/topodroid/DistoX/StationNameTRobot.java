/* @file StationNameTRobot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid station naming as TopoRobot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * N.B. backsight and tripod need the whole list to decide how to assign names
 */
package com.topodroid.DistoX;


// import android.util.Log;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

class StationNameTRobot extends StationName
{
  StationNameTRobot( Context ctx, DataHelper data, long sid ) 
  {
    super( ctx, data, sid );
  }

  // ---------------------------------- TopoRobot policy -----------------------------------
  // TopoRobot policy is splay-first then forward leg 

  private static String getTRobotStation( int sr, int pt )
  {
    return Integer.toString( sr ) + "." + Integer.toString( pt );
  }

  private static int getMaxTRobotSeries( List<DBlock> list )
  {
    int ret = 1;
    for ( DBlock blk : list ) {
      if ( ! blk.isMainLeg() ) continue; // skip splays and backsight leg
      if ( blk.mFrom.length() > 0 ) {
        int pos = blk.mFrom.indexOf('.');
        if ( pos > 0 ) {
          try {
            int r = Integer.parseInt( blk.mFrom.substring( 0, pos ) );
            if ( r > ret ) ret = r;
          } catch ( NumberFormatException e ) { }
        }
      }
      if ( blk.mTo.length() > 0 ) {
        int pos = blk.mTo.indexOf('.');
        if ( pos > 0 ) {
          try {
            int r = Integer.parseInt( blk.mTo.substring( 0, pos ) );
            if ( r > ret ) ret = r;
          } catch ( NumberFormatException e ) { }
        }
      }
    }
    return ret;
  }

  // WARNING TopoRobot renumbering consider all the shots in a single series
  // @param list list of dblock to assign
  @Override
  void assignStationsAfter( DBlock blk0, List<DBlock> list, Set<String> sts )
  {
    ArrayList<DBlock> unassigned = new ArrayList<DBlock>();
    // boolean started = false;

    // Log.v("DistoX-SN", "assign stations after - TRobot");
    if ( TDSetting.mDistoXBackshot ) return;
    boolean bs = TDSetting.mDistoXBackshot;

    DBlock prev = blk0;
    String from = blk0.mFrom;
    String to   = blk0.mTo;
    String next = DistoXStationName.incrementName( to, sts );
    String station = to;
    String main_from = null;
    String main_to   = null;

    // int nrLegShots = 0;
    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId ) continue;
      if ( blk.isSplay() ) {
        // blk.mFrom = station;
	if ( bs ) {
          setBlockName( blk, "", station );
	} else {
          setBlockName( blk, station, "" ); 
	}
	sts.add( from );
      } else if ( blk.isMainLeg() ) { 
        prev = blk;
        from = to;
        to   = next;
        next = DistoXStationName.incrementName( to, sts );
        station = to;
        // blk.mFrom = from;
        // blk.mTo   = to;
	if ( bs ) {
          setBlockName( blk, to, from );
        } else {
          setBlockName( blk, from, to );
	}
	main_from = from;
	main_to   = to;
	sts.add( from );
	sts.add( to );
      } else if ( blk.isBackLeg() ) {
	if ( main_from != null /* && main_to != null */ ) {
          prev = blk;
	  if ( bs ) {
            setBlockName( blk, main_from, main_to );
	  } else {
            setBlockName( blk, main_to, main_from );
	  }
	}
	main_from = main_to = null;
      } else {
	if ( /* started || */ ! blk.isRelativeDistance( prev ) ) {
	  unassigned.add( blk );
	  // started = true;
	}
      }
    }
    if ( unassigned.size() > 0 ) assignStations( unassigned, sts );
  }

  @Override
  void assignStations( List<DBlock> list, Set<String> sts )
  { 
    if ( TDSetting.mDistoXBackshot ) return;

    int series = getMaxTRobotSeries( list );
    // Log.v("DistoX", "TRobot assign stations. size " + list.size() );
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );
    DBlock prev = null;
    String from = getTRobotStation( 1, 0 );
    String to   = getTRobotStation( 1, 1 );
    String station = mCurrentStationName;
    if ( station == null ) station = from;

    // Log.v("DistoX", "TRobot assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // Log.v("DistoX", "TRobot Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );

    int nrLegShots = 0;

    for ( DBlock blk : list ) {
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );
	
        // FIXME FIXME use setBlockName() helper method and DistoX.backshot mode

        if ( prev == null ) {
          prev = blk;
          blk.setBlockName( ((station!=null)? station : from), "" ); // ALWAYS true
          mData.updateShotName( blk.mId, mSid, blk.mFrom, "", true );  // SPLAY
          // Log.v( "DistoX", blk.mId + " FROM " + blk.mFrom + " PREV null" );
        } else {
          if ( prev.isRelativeDistance( blk ) ) {
            if ( nrLegShots == 0 ) {
              // checkCurrentStationName
              if ( mCurrentStationName != null ) {
                ++series;
                from = mCurrentStationName;
                to   = getTRobotStation( series, 1 );
              }
              nrLegShots = 2; // prev and this shot
            } else {
              nrLegShots ++;  // one more centerline shot
            }
            if ( nrLegShots == TDSetting.mMinNrLegShots ) {
              legFeedback( );
              mCurrentStationName = null;
              // Log.v( "DistoX", "PREV " + prev.mId + " nrLegShots " + nrLegShots + " set PREV " + from + "-" + to );
              prev.setBlockName( from, to );
              mData.updateShotName( prev.mId, mSid, from, to, true ); // LEG
              setLegExtend( prev );
              station = to;
              from = to;                                   // next-shot-from = this-shot-to
              to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                // Log.v("DistoX", "station [1] " + station + " FROM " + from + " TO " + to );
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            blk.setBlockName( ((station!=null)? station : from), "" );
            mData.updateShotName( blk.mId, mSid, blk.mFrom, "", true ); // SPLAY
            prev = blk;
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mTo.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          from = blk.mTo;
          to   = DistoXStationName.incrementName( from, sts );
          if ( mCurrentStationName == null ) {
            station = blk.mTo;   // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
          } // otherwise station = mCurrentStationName
          nrLegShots = TDSetting.mMinNrLegShots;
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }

  // ---------------------------------- TopoRobot policy -----------------------------------
  // TopoRobot policy is splay-first then forward leg 
  //
  // TopoRobot policy is incompatible with DistoXBackshot

}
