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
package com.topodroid.TDX;

import com.topodroid.prefs.TDSetting;
import com.topodroid.utils.TDLog;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;

class StationNameTRobot extends StationName
{
  /** cstr
   * @param ctx   context
   * @param data  database helper
   * @param sid   survey ID
   */
  StationNameTRobot( Context ctx, DataHelper data, long sid ) 
  {
    super( ctx, data, sid );
  }

  // ---------------------------------- TopoRobot policy -----------------------------------
  // TopoRobot policy is splay-first then forward leg 

  /** @return the TopoRobot station name
   * @param sr    series
   * @param pt    point
   */
  private static String getTRobotStation( int sr, int pt )
  {
    return sr + "." + pt; // Integer.toString( sr ) + "." + Integer.toString( pt );
  }

  // /** @return the series of a station 
  //  * @param station station name
  //  */
  // private static int getSeries( String station )
  // {
  //   int pos = station.indexOf( '.' );
  //   return Integer.parseInt( station.substring( 0, pos ) );
  // }

  // /** @return the point of a station 
  //  * @param station station name
  //  */
  // private static int getPoint( String station )
  // {
  //   int pos = station.indexOf( '.' );
  //   return Integer.parseInt( station.substring( pos+1 ) );
  // }

  /** @return the next station in the series
   * @param station station name
   */
  private static String getNextStation( String station )
  {
    int pos = station.indexOf( '.' );
    try {
      if ( pos < 0 ) {
        int next = 1 + Integer.parseInt( station );
        return Integer.toString( next );
      } else {
        int next = 1 + Integer.parseInt( station.substring(pos+1) );
        return station.substring(0, pos+1 ) + Integer.toString( next );
      }
    } catch ( NumberFormatException e ) {
      // TODO TDLog.e( "Station name exception " + station );
    }
    return null;
  }
    
        


  /** @return the maximum series (number) occurring in a list of shots
   * @param list  list of shots
   */
  private static int getMaxTRobotSeries( List< DBlock > list )
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
          } catch ( NumberFormatException e ) {
            TDLog.e("TopoROBOT parse error " + blk.mFrom );
          }
        }
      }
      if ( blk.mTo.length() > 0 ) {
        int pos = blk.mTo.indexOf('.');
        if ( pos > 0 ) {
          try {
            int r = Integer.parseInt( blk.mTo.substring( 0, pos ) );
            if ( r > ret ) ret = r;
          } catch ( NumberFormatException e ) {
            TDLog.e("TopoROBOT parse error " + blk.mTo );
          }
        }
      }
    }
    return ret;
  }

  /** WARNING TopoRobot renumbering consider all the shots in a single series
   * @param blk0  start block
   * @param list  list of dblock to assign
   * @param sts   set of station names (used in the survey)
   * @return ???
   */
  @Override
  boolean assignStationsAfter( DBlock blk0, List< DBlock > list, Set<String> sts )
  {
    boolean ret = false;
    ArrayList< DBlock > unassigned = new ArrayList<>();
    TDLog.v( "TROBOT assign stations after " + blk0.mId );

    DBlock prev = blk0;
    String from = blk0.mFrom;
    String to   = blk0.mTo;
    if ( blk0.isDistoXBacksight() ) {
      from = blk0.mTo;
      to   = blk0.mFrom;
    }
 
    String next = DistoXStationName.incrementName( to, sts );
    String station = to;
    String main_from = null;
    String main_to   = null;

    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId || blk.isSecLeg() ) continue; // 20250719 replaces
      // if ( blk.mId == blk0.mId ) continue;
      if ( blk.isSplay() ) {
        if ( TDSetting.mSplayStation || blk.mFrom.length() == 0 ) { // mSplayStation 
          // blk.mFrom = station;
          setSplayName( blk, station );
	  sts.add( from );
        }
      } else if ( blk.isMainLeg() ) { 
        prev = blk;
        from = to;
        to   = next;
        next = DistoXStationName.incrementName( to, sts );
        station = to;
        // blk.mFrom = from;
        // blk.mTo   = to;
        setLegName( blk, from, to );
        ret = true;
	main_from = from;
	main_to   = to;
	sts.add( from );
	sts.add( to );
      } else if ( blk.isBackLeg() ) {
	if ( main_from != null /* && main_to != null */ ) {
          prev = blk;
          setLegName( blk, main_to, main_from );
          ret = true;
	}
	main_from = main_to = null;
      } else {
	if ( /* started || */ ! blk.isRelativeDistance( prev ) ) {
	  unassigned.add( blk );
	  // started = true;
	}
      }
    }
    if ( unassigned.size() > 0 ) ret |= assignStations( unassigned, sts );
    return ret;
  }

  /** assign station names
   * @param list  list of dblock to assign
   * @param sts   set of station names (used in the survey)
   * @return true if a leg has been assigned
   */
  @Override
  boolean assignStations( List< DBlock > list, Set<String> sts )
  { 
    boolean ret = false;
    int series = getMaxTRobotSeries( list ); // current max series
    TDLog.v( "TROBOT assign stations. data " + list.size() + " stations " + sts.size() );
    DBlock prev = null;
    String from = getTRobotStation( 1, 0 ); // from = 1.0
    String to   = getTRobotStation( 1, 1 ); // to   = 1.1
    String station = mCurrentStationName;
    if ( station == null ) station = from;

    // TDLog.v( "TRobot assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // TDLog.v( "TRobot Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );

    int nrLegShots = 0;
    for ( DBlock blk : list ) {
      if ( blk.isSecLeg() && prev != null && ! prev.isSplay() ) { // 20250719 new test
        continue;
      }
      if ( blk.mFrom.length() == 0 ) {
        if ( blk.mTo.length() == 0 ) {
          // TDLog.v( blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );
          if ( prev == null ) {
            prev = blk;
            setSplayName( blk, ((station!=null)? station : from) ); // ALWAYS true
            mData.updateShotName( blk.mId, mSid, blk.mFrom, blk.mTo );  // SPLAY
            // TDLog.v( blk.mId + " FROM " + blk.mFrom + " PREV null" );
          } else {
            if ( prev.isRelativeDistance( blk ) ) {
              if ( nrLegShots == 0 ) {
                // checkCurrentStationName
                if ( mCurrentStationName != null ) {
                  // if the mCurrentStationName is the last of its series do not start a new series
                  String next = getNextStation( mCurrentStationName );
                  if ( sts.contains( next ) ) {
                    ++series;
                    from = mCurrentStationName;
                    to   = getTRobotStation( series, 1 ); // to = series.1
                  } else {
                    from = mCurrentStationName;
                    to   = next;
                  }
                }
                nrLegShots = 2; // prev and this shot
              } else {
                nrLegShots ++;  // one more centerline shot
              }
              if ( nrLegShots == TDSetting.mMinNrLegShots ) {
                legFeedback( );
                mCurrentStationName = null;
                // TDLog.v( "PREV " + prev.mId + " nrLegShots " + nrLegShots + " set PREV " + from + "-" + to );
                setLegName( prev, from, to );
                ret = true;
                setLegExtend( prev );
                station = to;
                from = to;                                   // next-shot-from = this-shot-to
                to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                  // TDLog.v( "station [1] " + station + " FROM " + from + " TO " + to );
              }
            } else { // distance from prev > "closeness" setting
              nrLegShots = 0;
              setSplayName( blk, ((station!=null)? station : from) );
              prev = blk;
            }
          }
        } else { // blk already SPLAY
          nrLegShots = 0;
          prev = blk;
        }
      } else { // blk.mFrom.length > 0
        if ( blk.mTo.length() > 0 ) { // FROM non-empty, TO non-empty --> LEG
          from = blk.isDistoXBacksight()? blk.mFrom : blk.mTo;
          to   = DistoXStationName.incrementName( from, sts );
          if ( mCurrentStationName == null ) {
            station = blk.isDistoXBacksight()? blk.mFrom : blk.mTo;   // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
          } // otherwise station = mCurrentStationName
          nrLegShots = TDSetting.mMinNrLegShots;
        } else { // FROM non-empty, TO empty --> SPLAY
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
    return ret;
  }

}
