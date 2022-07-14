/* @file StationNameDefaultBlunder.java
 *
 * @author marco corvi
 * @date jul 2022
 *
 * @brief TopoDroid station naming allowing blunder-shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * N.B. backsight and tripod need the whole list to decide how to assign names
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDStatus;
import com.topodroid.common.LegType;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;
import android.view.View;

class StationNameDefaultBlunder extends StationName
{
  private int survey_stations;
  private boolean forward_shots;
  private boolean shot_after_splay;

  private DBlock prev_prev;
  private DBlock prev;
  private DBlock leg;  // tentative leg / leg
  private DBlock blunder;
  private int nrLegShots;
  private ArrayList< DBlock > sec_legs;
  private String from, to, station, current_station;
  private boolean mRet;

  StationNameDefaultBlunder( Context ctx, DataHelper data, long sid ) 
  {
    super( ctx, data, sid );
    sec_legs = new ArrayList< DBlock >();
  }

  // ------------------------------------------------------------------------------------------------

  /** reset shot refereneces
   * @param reset_leg whether to reset the leg reference and leg number
   */
  private void resetRefs( boolean reset_leg, boolean reset_prev )
  {
    prev_prev  = null;
    if ( reset_prev ) {
      prev = null;
    }
    if ( reset_leg ) {
      blunder = null;
      leg = null;
      nrLegShots = 0;
      sec_legs.clear();
    }
  }

  /** set the prev/prev_prev shots
   * @param blk   new prev shot
   * @note prev_prev is set with the old prev
   */
  private void setPrev( DBlock blk ) 
  {
    prev_prev = prev;
    prev = blk;
  }

  /** set the leg main-shot 
   * @param blk   new leg main shot
   * @note clears prev_prev
   * @note if leg coincide with the tentative blunder this is cleared
   */
  private void setLeg( String msg, DBlock blk, int nr_legs ) 
  {
    TDLog.v("set leg " + id(leg) + " " + msg + " -> " + id(blk) + " nr legs " + nr_legs );
    if ( leg == null ) {
      leg = blk;
      if ( leg == blunder ) blunder = null;
    }
    nrLegShots = nr_legs;
    prev_prev = null;
  }

  /** mark the leg main-shot 
   */
  private void markLeg()
  {
    if ( leg == null ) return; // safety protection
    if ( leg.isLeg() ) {
      TDLog.v( "mark leg " + name(leg) + " is altready leg" );
      return;
    }
    TDLog.v( "mark leg " + id(leg) + " : " + from + "-" + to );
    setLegName( leg, from, to );
    setLegExtend( leg );
    // leg = null;
  }

  /** mark a shot as splay
   * @param blk   splay shot
   */
  private void markSplay( DBlock blk ) 
  {
    if ( blk == null ) return; // safety protection
    if ( blk.isAnyLeg() ) return;
    if ( ! station.equals( blk.mFrom ) ) {
      TDLog.v( "mark splay " + id(blk) + " : " + blk.mFrom + " -> " + station );
      setSplayName( blk, station ); // saved to DB
    }
  }

  /** mark shot as secondary leg
   * @param blk    secondary leg shot
   */
  private void markSecLeg( DBlock blk )
  {
    setSecLegName( blk );
  }

  /** mark shot as secondary leg or splay according to whether it is close to the leg
   * @param leg    leg
   * @param blk    shot
   */
  private void markSecLegOrSplay( DBlock leg, DBlock blk )
  {
    if ( blk.isRelativeDistance( leg ) ) {
      markSecLeg( blk );
    } else {
      markSplay( blk );
    }
  }

  /** if the blunder reference is not null mark it as BLUNDER
   */
  private void markBlunder(String msg)
  {
    if ( blunder == null ) return; // safety protection
    if ( blunder != leg ) {
      TDLog.v( msg + " mark blunder " + id(blunder) );
      setBlunderName( blunder ); // saved to DB
    }
    blunder = null;
  }

  private void flushLeg( DBlock blk, String msg, boolean reset_leg, boolean reset_prev )
  {
    if ( nrLegShots > 0 ) {
      StringBuilder sb = new StringBuilder();
      sb.append( ": " + id(leg) + "." + id(prev) + "." + id(blunder) + " (" );
      for ( DBlock b : sec_legs ) sb.append( " " + id(b) );
      TDLog.v( msg + " flush at " + id(blk) + " legs " + nrLegShots + "/" + sec_legs.size() + " " + sb.toString() + " ) reset legs " + reset_leg );
      if ( nrLegShots < TDSetting.mMinNrLegShots ) {
        // if ( prev_prev != null ) markSplay( prev_prev );
        if ( blunder != null ) markSplay( blunder );
        // if ( leg != null && leg.isLeg() ) { // FIXME_BLUNDER this is not necessary
        //   if ( prev    != null ) markSecLegOrSplay( leg, prev );
        //   for ( DBlock b : sec_legs ) markSecLegOrSplay( leg, b );
        // } else {
          if ( prev    != null ) markSplay( prev );
          for ( DBlock b : sec_legs ) markSplay( b );
        // }
      } else {
        markLeg();
        for ( DBlock b : sec_legs ) markSecLeg( b );
      }
      resetRefs( reset_leg, reset_prev );
    }
  }

  private void increaseNrLegShots( DBlock blk, Set< String > sts, String msg ) 
  {
    TDLog.v( msg + " increase leg shots at " + id(blk) + " leg " + id(leg) + " prev " + id(prev) );
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
      setLeg( "from increase", prev, 2 ); // nrLegShots = 2; prev and this shot
      // prev_prev = null; 
      // prev      = null;
      TDLog.v( msg + " started nr_leg " + nrLegShots + "/" + sec_legs.size() + " at " + id(blk) + " leg " + id(leg) );
      // TDLog.Log( TDLog.LOG_DATA, "leg-2 F " + from + " T " + to + " S " + station );
    } else {
      nrLegShots ++;  // one more centerline shot
      TDLog.v( msg + " increased nr_leg " + nrLegShots + "/" + sec_legs.size() + " at " + id(blk) );
    }

    if ( nrLegShots == TDSetting.mMinNrLegShots ) {
      legFeedback( );
      current_station = null;
      markLeg();
      // do not clear lef reference
      mRet = true;
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
      for ( DBlock b : sec_legs ) markSecLeg( b );
      sec_legs.clear();
      markBlunder("[incr.nr.leg]");
    } else if ( nrLegShots > TDSetting.mMinNrLegShots ) {
      markSecLeg( blk );
    }
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
  //         TDLog.Error( from + "-" + to + " blk " + blk.mId + " set " + sb.toString() );
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
    // TDLog.v("DATA " + "assign stations: list " + list.size() + " sts " + (sts!=null? sts.size():"-") );

    survey_stations = StationPolicy.mSurveyStations;
    // if ( survey_stations <= 0 ) return false; // assign always false with no policy
    forward_shots = ( survey_stations == 1 );
    shot_after_splay = StationPolicy.mShotAfterSplays;
    current_station  = mCurrentStationName; // steal current station name
    mCurrentStationName = null;

    mRet = false;
    resetRefs( true, true );

    from = ( forward_shots )? DistoXStationName.mInitialStation  // next FROM station
                            : DistoXStationName.mSecondStation;
    to   = ( forward_shots )? DistoXStationName.mSecondStation   // next TO station
                            : DistoXStationName.mInitialStation;
    station = ( current_station != null )? current_station
            : (shot_after_splay ? from : "");  // splays station

    StringBuilder sb = new StringBuilder();
    for ( DBlock b : list ) sb.append( name(b) + " " );
    TDLog.v( "F " + from + " T " + to + " S " + station + " List " + sb.toString() );

    for ( DBlock blk : list ) {
      TDLog.v("process " + name(blk) + " " + id(leg) + "." + id(prev) + "." + id(blunder) );
      if ( blk.mTo.length() == 0 ) {
        // if ( blk.mFrom.length() == 0 ) {
        //   if ( blk.isScan() ) {
        //     flushLeg(blk, "[scan splay]", true, true ); // true = reset leg & nr_legs
        //     markSplay( blk );
        //     continue;
        //   }
        // }
        if ( prev == null ) { // FIXME_BLUNDER this block came first among the if's, but it can be also second after the "leg"
          TDLog.v("null prev at " + id(blk) );
          setPrev( blk );
          // blk.mFrom = station;
          markSplay( blk );
        } else if ( leg != null && leg.isRelativeDistance( blk ) ) {
          markBlunder("[close to leg]");
          increaseNrLegShots( blk, sts, "[close to leg]" );
          // if ( leg.isLeg() ) markSecLeg( blk ); // FIXME_BLUNDER not necessary
        } else if ( /* prev != null && */ prev.isRelativeDistance( blk ) ) {
          flushLeg(blk, "[close to prev]", false, false ); // true = reset leg & nr_legs
          if ( leg != null && ! prev.isRelativeDistance( leg ) ) {
            TDLog.v("clear leg " + id(leg) );
            leg = null;
            nrLegShots = 0;
          }
          // if ( leg == null ) setLeg( "from pref", prev, 0 );
          increaseNrLegShots( blk, sts, "[close to prev]" );
        } else if ( prev_prev != null && prev_prev.isRelativeDistance( blk ) ) {
          setLeg( "fron prev_prev", prev_prev, 0 ); // nrLegShots = 0; it will be set in increaseNrLegShots
          blunder = prev;
          increaseNrLegShots(  blk, sts, "[close to prev_prev]" );
          prev = blk;
        } else {
          if ( blunder != null ) { // two splays in a row
            blunder = null;        // clear blunder before flush
            flushLeg(blk, "[new splay]", true, true );
          } else if ( leg != null ) { // first splay after a leg
            blunder = blk; // tentative blunder
            flushLeg(blk, "[new splay]", false, true );
          }
          markSplay( blk );
          setPrev( blk );
        }
      } else { // blk.mTo.length > 0
        if ( blk.mFrom.length() > 0 ) { // FROM non-empty, TO non-empty --> LEG
          leg = blk;
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
          nrLegShots = TDSetting.mMinNrLegShots;
        } else { // FROM empty, TO non-empty --> rev-SPLAY
          TDLog.v( id(blk) + " rev splay - clear nr_legs ");
          nrLegShots = 0;
          setPrev( blk );
        }
      }
    }
    mCurrentStationName = current_station; // reset current station name
    return mRet;
  }

  // ------------------------------------------------------------------------------------------------

  /** assign station names to a number of shots
   * @note this is called to renumber a list of blocks - therefore the extend need not be updated
   * @param blk0   reference dblock
   * @param list   list of dblock to assign
   * @param sts    station names already in use
   * @return true if a leg has been assigned
   *
   * NOT USED
   */
//   @Override
//   boolean assignStationsAfter( DBlock blk0, List< DBlock > list, Set<String> sts )
//   {
//     int survey_stations = StationPolicy.mSurveyStations;
//     if ( survey_stations <= 0 ) return false; // assign always false with no policy
// 
//     mRet = false;
//     // TDLog.v("DATA " + "assign station after " + blk0.mId + " list " + list.size() + " sts " + ((sts!=null)?sts.size():"-") );
//     ArrayList< DBlock > sec_legs = new ArrayList<>();
//     // TDLog.Log( TDLog.LOG_DATA, "assign stations after " + list.size() + " " + (sts!=null? sts.size():0) );
// 
//     boolean forward_shots = ( survey_stations == 1 );
//     boolean shot_after_splays = StationPolicy.mShotAfterSplays;
//     // String  current_station = mCurrentStationName;
//     // TDLog.v( "default assign stations after. blk0 " + blk0.mId + " bs " + bs + " survey_stations " + survey_stations + " shot_after_splay " + shot_after_splays );
// 
//     String main_from = null;
//     String main_to   = null;
// 
//     // boolean increment = true;
//     // // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );
// 
//     DBlock prev = blk0;
//     String from = blk0.mFrom;
//     String to   = blk0.mTo;
//     if ( blk0.isDistoXBacksight() ) {
//       from = blk0.mTo;
//       to   = blk0.mFrom;
//     }
// 
//     String next;
//     String station;
//     if ( forward_shots ) {
//       next = to;
//       next = DistoXStationName.incrementName( next, sts );
//       station = shot_after_splays ? to : from;
//     } else {
//       next = from;
//       next = DistoXStationName.incrementName( next, sts );
//       station = shot_after_splays ? next : from;
//     }
// 
//     // TDLog.v( "F " + from + " T " + to + " N " + next + " S " + station );
//     // if ( TDLog.LOG_DATA ) {
//     //   TDLog.Log( TDLog.LOG_DATA, "assign after: F<" + from + "> T<" + to + "> N<" + next + "> S<" + station + "> CS " + ( (current_station==null)? "null" : current_station ) );
//     //   StringBuilder sb = new StringBuilder();
//     //   for ( String st : sts ) sb.append(st + " " );
//     //   TDLog.Log(TDLog.LOG_DATA, "set " + sb.toString() );
//     // }
// 
//     for ( DBlock blk : list ) {
//       if ( blk.mId == blk0.mId ) continue;
//       if ( blk.isScan() ) {
//         setSplayName( blk, station );
// 	sts.add( station );
//       } else if ( blk.isSplay() ) {
//         setSplayName( blk, station );
// 	sts.add( station );
//         // TDLog.Log( TDLog.LOG_DATA, "splay " + blk.mId + " S<" + station + "> bs " + bs );
//       } else if ( blk.isMainLeg() ) {
// 	prev = blk;
//         if ( forward_shots ) {
//           from = to;
//           to   = next;
//           next = DistoXStationName.incrementName( next, sts ); // to, sts
//           station = shot_after_splays ? to : from;
//         } else {
//           to   = from;
//           from = next;
//           next = DistoXStationName.incrementName( next, sts ); // from, sts
//           station = shot_after_splays ? next : from;
//         }
// 	main_from = from;
// 	main_to   = to;
//         // blk.mFrom = from;
//         // blk.mTo   = to;
//         setLegName( blk, from, to );
//         mRet = true;
// 	sts.add( from );
// 	sts.add( to );
//         // TDLog.Log( TDLog.LOG_DATA, "main leg: " + blk.mId + " F<" + from + "> T<" + to + "> S<" + station + "> bs " + bs );
//         // TDLog.v( "main leg: " + blk.mId + " F<" + from + "> T<" + to + "> S<" + station + "> bs " + bs );
//       } else if ( blk.isBackLeg() ) {
// 	if ( main_from != null /* && main_to != null */ ) {
// 	  prev = blk;
//           setLegName( blk, main_to, main_from );
//           mRet = true;
//           // TDLog.Log( TDLog.LOG_DATA, "back leg: " + blk.mId + " F<" + main_from + "> T<" + main_to + "> bs " + bs );
// 	}
// 	main_from = main_to = null;
//       } else {
//         // TDLog.v( "blk is skipped " + blk.mId + " prev " + prev.mId );
// 	if ( ! blk.isRelativeDistance( prev ) ) {
//           sec_legs.add( blk );
//         } else {
//           setSecLegName( blk );
//         }
//       }
//     }
//    
//     // processing skipped ahots ...
//     if ( sec_legs.size() > 0 ) {
//       mRet |= assignStations( sec_legs, sts );
//     } else {
//       mCurrentStationName = null;
//     }
//     return mRet;
//   }

}
