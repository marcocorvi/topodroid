/* @file StationNameDefault.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid station naming
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * N.B. backsight and tripod need the whole list to decide how to assign names
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;

class StationNameDefault extends StationName
{
  StationNameDefault( Context ctx, DataHelper data, long sid ) 
  {
    super( ctx, data, sid );
  }

  // ------------------------------------------------------------------------------------------------

  /** assign station names to a number of shots
   * @note this is called to renumber a list of blocks - therefore the extend need not be updated
   * @param blk0   reference dblock
   * @param list   list of dblock to assign
   * @param sts    station names already in use
   * @return ???
   */
  @Override
  boolean assignStationsAfter( DBlock blk0, List< DBlock > list, Set<String> sts )
  {
    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return false; // assign always false with no policy

    boolean ret = false;
    TDLog.v("DATA " + "assign station after " + id(blk0) + " list " + list.size() + " sts " + ((sts!=null)?sts.size():"-") );
    ArrayList< DBlock > sec_legs = new ArrayList<>();

    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splays = StationPolicy.mShotAfterSplays;
    // String  current_station = mCurrentStationName;
    // TDLog.v( "default assign stations after. blk0 " + id(blk0) + " bs " + bs + " survey_stations " + survey_stations + " shot_after_splay " + shot_after_splays );

    String main_from = null;
    String main_to   = null;

    // boolean increment = true;

    DBlock prev = blk0;
    String from = blk0.mFrom;
    String to   = blk0.mTo;
    if ( blk0.isDistoXBacksight() ) {
      from = blk0.mTo;
      to   = blk0.mFrom;
    }

    String next;
    String station;
    if ( forward_shots ) {
      next = to;
      next = DistoXStationName.incrementName( next, sts );
      station = shot_after_splays ? to : from;
    } else {
      next = from;
      next = DistoXStationName.incrementName( next, sts );
      station = shot_after_splays ? next : from;
    }
    // TDLog.v( "F " + from + " T " + to + " N " + next + " S " + station );

    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId ) continue;
      if ( blk.isScan() ) {
        setSplayName( blk, station );
	sts.add( station );
      } else if ( blk.isSplay() ) {
        setSplayName( blk, station );
	sts.add( station );
        // TDLog.Log( TDLog.LOG_DATA, "splay " + blk.mId + " S<" + station + "> bs " + bs );
      } else if ( blk.isMainLeg() ) {
	prev = blk;
        if ( forward_shots ) {
          from = to;
          to   = next;
          next = DistoXStationName.incrementName( next, sts ); // to, sts
          station = shot_after_splays ? to : from;
        } else {
          to   = from;
          from = next;
          next = DistoXStationName.incrementName( next, sts ); // from, sts
          station = shot_after_splays ? next : from;
        }
	main_from = from;
	main_to   = to;
        // blk.mFrom = from;
        // blk.mTo   = to;
        setLegName( blk, from, to );
        ret = true;
	sts.add( from );
	sts.add( to );
        // TDLog.v( "main leg: " + id(blk) + " F<" + from + "> T<" + to + "> S<" + station + "> bs " + bs );
      } else if ( blk.isBackLeg() ) {
	if ( main_from != null /* && main_to != null */ ) {
	  prev = blk;
          setLegName( blk, main_to, main_from );
          ret = true;
          // TDLog.v( "back leg: " + id(blk) + " F<" + main_from + "> T<" + main_to + "> bs " + bs );
	}
	main_from = main_to = null;
      } else {
        // TDLog.v( "blk is skipped " + id(blk) + " prev " + id(prev) );
	if ( ! blk.isRelativeDistance( prev ) ) {
          sec_legs.add( blk );
        } else {
          setSecLegName( blk );
        }
      }
    }
   
    // processing skipped ahots ...
    if ( sec_legs.size() > 0 ) {
      ret |= assignStations( sec_legs, sts );
    } else {
      mCurrentStationName = null;
    }
    return ret;
  }

  // debug log  NO_LOGS
  // private void logJump( DBlock blk, String from, String to, Set<String> sts )
  // {
  //   if ( TDLog.LOG_SHOT ) {
  //     try {
  //       int i1 = Integer.parseInt( from );
  //       int i2 = Integer.parseInt( to );
  //       if ( Math.abs(i2-i1) != 1 ) {
  //         StringBuilder sb = new StringBuilder();
  //         for ( String st : sts ) sb.append(st).append("," );
  //         TDLog.Error( from + "-" + to + " blk " + id(blk) + " set " + sb.toString() );
  //       }
  //     } catch ( NumberFormatException e ) { }
  //   }
  // }

  /** assign station names to shots
   * @param list         list of dblock, including those to assign
   * @param sts          station names already in use
   * DistoX backshot-mode is handled separatedly
   * @return true if a leg has been assigned
   */
  @Override
  boolean assignStations( List< DBlock > list, Set<String> sts )
  { 
    TDLog.v("BLOCK DATA " + "assign stations: list " + list.size() + " sts " + (sts!=null? sts.size():"-") );

    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return false; // assign always false with no policy

    boolean ret = false;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splay = StationPolicy.mShotAfterSplays;
    String  current_station  = mCurrentStationName; // steal current station name
    mCurrentStationName = null;

    // // TDLog.v( "BLOCK assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = null;
    DBlock prev_prev = null; // prev of prev (for BLUNDER SHOT)
    String from = ( forward_shots )? DistoXStationName.mInitialStation  // next FROM station
                                   : DistoXStationName.mSecondStation;
    String to   = ( forward_shots )? DistoXStationName.mSecondStation   // next TO station
                                   : DistoXStationName.mInitialStation;
    String station = ( current_station != null )? current_station
                   : (shot_after_splay ? from : "");  // splays station

    int nrLegShots = 0;
    ArrayList< DBlock > sec_legs = new ArrayList<>();
    DBlock blunder = null;
    boolean with_blunder = false;

    for ( DBlock blk : list ) {
      // TDLog.v( "BLOCK " + id(blk) + " " + name(blk) + " F " + from + " T " + to + " S " + station );
      if ( blk.mFrom.length() == 0 ) {
        // TDLog.v( "BLOCK " + id(blk) + " F EMPTY: prev " + id(prev) + " " + id(prev_prev) );
        if ( blk.isScan() ) {
          nrLegShots = 0;
          setSplayName( blk, station );
          prev_prev = null;
          prev = null;
          // TDLog.v( "BLOCK " + id(blk) + " is scan: nulling prevs");
          continue;
        }
        if ( blk.mTo.length() == 0 ) {
          // TDLog.v( "BLOCK " + blk.mId + " T EMPTY");
          if ( prev == null ) {
            prev_prev = prev;
            prev = blk;
            // TDLog.v( "BLOCK Null prev: set prev [1] " + id(prev) + " nulling prev_prev");
            // blk.mFrom = station;
            setSplayName( blk, station );
            // TDLog.v( "set prev [1] " + blk.mId + " " + name(blk) );
          } else {
            boolean is_relative_distance = false;
            // BLUNDER SHOT SKIP
            if ( prev.isRelativeDistance( blk ) ) {
              is_relative_distance = true;
            } else if ( TDSetting.mBlunderShot && prev_prev != null && prev_prev.isRelativeDistance( blk ) ) {
              blunder = prev;
              prev = prev_prev;
              prev_prev = null;
              // TDLog.v( "BLOCK blunder shot skip reset prev " + id(prev) + " nulling prev_prev" );
              is_relative_distance = true;
            }
            if ( is_relative_distance ) {
              sec_legs.add( blk );
              if ( nrLegShots == 0 ) {
                // checkCurrentStationName
                if ( current_station != null ) {
                  if ( forward_shots ) { 
                    from = current_station;
                  } else if ( survey_stations == 2 ) {
                    to = current_station;
                  }
                }
                nrLegShots = 2; // prev and this shot
                // TDLog.v( "BLOCK set leg 2: F " + from + " T " + to + " S " + station + " prev " + id(prev) + " blk " + id(blk) );
              } else {
                nrLegShots ++;  // one more centerline shot
              }
              if ( nrLegShots == TDSetting.mMinNrLegShots ) {
                legFeedback( );
                current_station = null;
                // TDLog.v( "BLOCK leg " + nrLegShots + ": prev " + id(prev) + " set PREV " + from + "-" + to + " blk " + id(blk) + " blunder " + id(blunder) );
                setLegName( prev, from, to );
                if ( blunder != null ) {
                  with_blunder = true;
                  setBlunderName( blunder );
                  blunder = null;
                }
                ret = true;
                setLegExtend( prev );
                if ( forward_shots ) {
                  station = shot_after_splay  ? to : from;     // splay-station = this-shot-to if splays before shot
                                                               //                 this-shot-from if splays after shot
                  from = to;                                   // next-shot-from = this-shot-to
                  to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                  // logJump( blk, from, to, sts ); // NO_LOGS
                } else { // backward_shots
                  to   = from;                                     // next-shot-to   = this-shot-from
                  from = DistoXStationName.incrementName( from, sts ); // next-shot-from = increment this-shot-from
                  station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                                   //                = this-shot-from if splay after shot
                  // logJump( blk, to, from, sts );
                }
                // TDLog.Log( TDLog.LOG_DATA, "increment F " + from + " T " + to + " S " + station );
                for ( DBlock b : sec_legs ) {
                  // TDLog.v( "BLOCK secondary leg [1b] " + b.mId );
                  setSecLegNameAndType( b, with_blunder );
                }
                sec_legs.clear();
              } else {
                // TDLog.v( "BLOCK secondary leg [2] " + blk.mId );
                setSecLegNameAndType( blk, with_blunder );
              }
            } else { // distance from prev > "closeness" setting
              nrLegShots = 0;
              setSplayName( blk, station );
              prev_prev = prev;
              prev = blk;
              with_blunder = false;
              // TDLog.v( "BLOCK not close: set prev [2] " + id(prev) + " prev_prev " + id(prev_prev) );
            }
          }
        } else { // blk.mTo.length() > 0 : blk already SPLAY
          nrLegShots = 0;
          prev_prev = prev;
          prev = blk;
          with_blunder = false;
          // TDLog.v( "BLOCK T " + id(blk) + ": set prev [3] " + id(prev) + " prev_prev " + id(prev_prev) );
        }
      } else { // blk.mFrom.length > 0
        if ( blk.mTo.length() > 0 ) { // FROM non-empty, TO non-empty --> LEG
          if ( forward_shots ) {  // : ..., 0-1, 1-2 ==> from=(2) to=Next(2)=3 ie 2-3
            from = blk.isDistoXBacksight()? blk.mFrom : blk.mTo;
            to   = from;
            to   = DistoXStationName.incrementName( to, sts );
            // logJump( blk, from, to, sts ); // NO_LOGS
            if ( current_station == null ) {
              if ( blk.isDistoXBacksight() ) {
                station = shot_after_splay ? blk.mFrom
                                           : blk.mTo;
              } else {
                station = shot_after_splay ? blk.mTo    // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
                                           : blk.mFrom; // 1-2, 1, 1,   [ 2-3, 2, 2, ... ] ...
              }
            } // otherwise station = current_station
          } else { // backward shots: ..., 1-0, 2-1 ==> from=Next(2)=3 to=2 ie 3-2
            to = blk.isDistoXBacksight()? blk.mTo : blk.mFrom;
            from = to;
            from = DistoXStationName.incrementName( from, sts ); // FIXME it was old from
            // logJump( blk, to, from, sts );

	    // station must be set even if there is a "currentStation"
            if ( blk.isDistoXBacksight() ) {
              station = shot_after_splay ? from   
                                         : blk.mTo;
            } else {
              station = shot_after_splay ? from       // 2,   2, 2, 2-1, [ 3, 3, ..., 3-2 ]  ...
                                         : blk.mFrom; // 2-1, 2, 2, 2,   [ 3-2, 3, 3, ... 3 ] ...
            }
          }
          // TDLog.Log( TDLog.LOG_DATA, "ID " + id(blk) + ": " + name(blk) + " F " + from + " T " + to + " S " + station );

          nrLegShots = TDSetting.mMinNrLegShots;
        } else { // FROM non-empty, TO empty --> SPLAY
          nrLegShots = 0;
        }
        prev_prev = prev;
        prev = blk;
        // TDLog.v( "BLOCK " + id(blk) + " " + name(blk) + ": set prev [4] " + id(prev) + " prev_prev " + id(prev_prev) );
      }
    }
    mCurrentStationName = current_station; // reset current station name
    return ret;
  }

}
