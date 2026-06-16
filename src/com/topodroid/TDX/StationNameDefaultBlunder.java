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

import com.topodroid.util.TDLog;
// import com.topodroid.util.TDStatus;
// import com.topodroid.types.LegType;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;

import android.view.View;

class StationNameDefaultBlunder extends StationName
{
  private static final boolean LOG = false;

  private int survey_stations;
  private boolean forward_shots;
  private boolean shot_after_splay;

  private DBlock prev_prev;
  private DBlock prev;
  private DBlock leg;  // tentative leg / leg
  private DBlock blunder;
  private DBlock old_leg;
  // private int nrLegShots;
  private ArrayList< DBlock > sec_legs;
  private String from, to, station, current_station;
  private boolean mRet;

  StationNameDefaultBlunder( Context ctx, DataHelper data, long sid ) 
  {
    super( ctx, data, sid );
    sec_legs = new ArrayList< DBlock >();
  }

  // ------------------------------------------------------------------------------------------------

  /** set the prev/prev_prev shots
   * @param blk   new prev shot
   * @note prev_prev is set with the old prev
   */
  private void setPrev( DBlock blk ) 
  {
    if ( prev != null && blk != null && blk.isRelativeAngle( prev ) ) {
      prev_prev = prev;
    } else {
      prev_prev = null;
    }
    prev = blk;
  }

  /** set the leg main-shot 
   * @param blk   new leg main shot
   * @note clears prev_prev
   * @note if leg coincide with the tentative blunder this is cleared
   */
  private void setLeg( String msg, DBlock blk )
  {
    if ( LOG ) TDLog.v("set leg " + id(leg) + " " + msg + " -> " + id(blk) );
    if ( leg == null ) {
      leg = blk;
      if ( leg == blunder ) blunder = null;
    }
    // nrLegShots = nr_legs;
    // nrLegShots = 1 + sec_legs.size();
    prev_prev = null;
  }

  /** mark the leg main-shot 
   */
  private boolean markLeg( Set<String> sts )
  {
    old_leg = leg;
    if ( leg == null ) return true; // safety protection
    if ( leg.isLeg() ) {
      if ( LOG ) TDLog.v( "==> leg " + name(leg) + " is already leg" );
      setFTS( leg.mFrom, leg.mTo, sts, "[1]" );
      return true;
    }
    if ( LOG ) TDLog.v( "==> leg " + id(leg) + " mark: " + from + "-" + to );
    setLegName( leg, from, to );
    setLegExtend( leg );
    // leg = null;
    return false;
  }

  private void setFTS( String f, String t, Set< String > sts, String msg )
  {
    TDLog.v("set FTS: " + f + " " + t + " " + msg );
    if ( forward_shots ) {
      station = shot_after_splay  ? t : f;     // splay-station = this-shot-to if splays before shot
                                                   //                 this-shot-from if splays after shot
      from = t;                                   // next-shot-from = this-shot-to
      to   = DistoXStationName.incrementName( t, sts );  // next-shot-to   = increment next-shot-from
      // logJump( blk, from, to, sts ); // NO_LOGS
    } else { // backward_shots
      to   = f;                                     // next-shot-to   = this-shot-from
      from = DistoXStationName.incrementName( f, sts ); // next-shot-from = increment this-shot-from
      station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                       //                = this-shot-from if splay after shot
      // logJump( blk, to, from, sts );
    }
    TDLog.v("----- " + msg + " set FTS " + FTS() );
  }
 
  private void setFTS( DBlock blk, Set< String > sts, String msg )
  {
    TDLog.v("set FTS: blk " + blk.mFrom + " " + blk.mTo + " " + msg );
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
      TDLog.v("----- " + msg + " forward blk set: FTS " + FTS() );
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
  }

  /** mark a shot as splay
   * @param blk   splay shot
   */
  private void markSplay( DBlock blk ) 
  {
    if ( blk == null ) return; // safety protection
    if ( blk.isAnyLeg() ) return;
    if ( blk.mFrom.length() > 0 ) return; // skip splay already marked 20220907
    if ( LOG ) TDLog.v( "==> splay " + id(blk) + " : " + station );
    setSplayName( blk, station ); // saved to DB
  }

  /** mark shot as secondary leg
   * @param blk    secondary leg shot
   */
  private void markSecLeg( DBlock blk )
  {
    if ( blk == null ) return;
    if ( LOG ) TDLog.v( "==> secLeg " + id(blk) );
    setSecLegName( blk );
  }

  /** if the blunder reference is not null and it is not equal to leg, mark it as BLUNDER
   */
  private void markBlunder( String msg )
  {
    if ( blunder == null ) return; // safety protection
    if ( blunder != leg ) {
      if ( LOG ) TDLog.v( msg + "==> blunder " + id(blunder) );
      setBlunderName( blunder ); // saved to DB
      View view = blunder.getView();
      if ( view != null ) { 
        blunder.setView( null );
        // mParent.dropBlock( blunder );
      }
    }
    blunder = null;
  }

  /** commit the leg
   * @param blk  current data shot (only for log)
   * @param msg  log message
   */
  private void flushLeg( DBlock blk, Set<String> sts, String msg )
  {
    old_leg = null;
    if ( sec_legs.size() > 1 ) { // ( nrLegShots > 0 ) 
      if ( LOG ) {
        StringBuilder sb = new StringBuilder();
        sb.append( ": " + id(leg) + "." + id(prev) + "." + id(blunder) + " (" );
        for ( DBlock b : sec_legs ) sb.append( " " ).append( id(b) );
        TDLog.v( msg + " flush leg at " + id(blk) + " sec_legs " + sec_legs.size() + ": " + sb.toString() );
      }
      if ( sec_legs.size() + 1 < TDSetting.mMinNrLegShots ) { // ( nrLegShots < TDSetting.mMinNrLegShots )
        markSplay( blunder );
        markSplay( prev );
        markSplay( prev_prev );
        for ( DBlock b : sec_legs ) markSplay( b );
      } else {
        markLeg( sts );
        for ( DBlock b : sec_legs ) markSecLeg( b );
        // old_leg = leg; // done in markLeg
      }
    }
  }

  /** increase the number of leg shots
   * @param blk  current data to add to the leg
   * @param sts  list of station names, used to increase the station name
   * @param msg  log message
   */
  private void increaseNrLegShots( DBlock blk, Set< String > sts, String msg ) 
  {
    if ( LOG ) TDLog.v( msg + " increase leg shots at " + id(blk) + " " + LPB() + " FTS " + FTS() );
    sec_legs.add( blk );
    if ( sec_legs.size() == 1 ) { // first sec-leg
      // checkCurrentStationName
      if ( current_station != null ) {
        if ( forward_shots ) { 
          from = current_station;
        } else if ( survey_stations == 2 ) {
          to = current_station;
        }
        station = current_station;
        TDLog.v("----- set from current: " + FTS() + " " + current_station );
      }
      setLeg( "from increase", leg ); // nrLegShots = 2: leg and this shot
      // prev_prev = null; 
      // prev      = null;
      if ( LOG ) TDLog.v( msg + " started sec_legs " + sec_legs.size() + " at " + id(blk) + " " + LPB() );
    } else {
      // nrLegShots ++;  // one more centerline shot
      if ( LOG ) TDLog.v( msg + " increased sec_legs " + sec_legs.size() + " at " + id(blk) + " " + LPB() );
    }
    if ( sec_legs.size() + 1 == TDSetting.mMinNrLegShots ) { // ( nrLegShots == TDSetting.mMinNrLegShots )
      legFeedback( );
      current_station = null;
      markLeg( sts );
      prev      = null;
      prev_prev = null;
      // do not clear lef reference
      mRet = true;
      // setFTS( from, to, sts, "[3]" );
      TDLog.v("----- set at minNrLeg: FTS " + FTS() + " " + current_station + " leg " + name(leg) );
      for ( DBlock b : sec_legs ) markSecLeg( b );
      markBlunder( "[incr.nr.leg]" );
    } else if ( sec_legs.size() + 1 > TDSetting.mMinNrLegShots ) { // ( nrLegShots > TDSetting.mMinNrLegShots )
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
  //         TDLog.v( from + "-" + to + " blk " + blk.mId + " set " + sb.toString() );
  //       }
  //     } catch ( NumberFormatException e ) { }
  //   }
  // }

  /** assign station names to shots
   * @param list         list of dblock, including those to assign
   * @param sts          station names already in use
   * DistoX backshot-mode is handled separately
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
    boolean backsight_splay  = ( StationPolicy.mSurveyStations == 1 ) && StationPolicy.mShotAfterSplays && TDSetting.mBacksightSplay;

    mRet = false;
    blunder   = null;
    prev_prev = null;
    prev      = null;
    leg       = null;
    old_leg   = null;

    from = ( forward_shots )? DistoXStationName.mInitialStation  // next FROM station
                            : DistoXStationName.mSecondStation;
    to   = ( forward_shots )? DistoXStationName.mSecondStation   // next TO station
                            : DistoXStationName.mInitialStation;
    station = ( current_station != null )? current_station
            : (shot_after_splay ? from : "");  // splays station

    if ( LOG ) {
      StringBuilder sb = new StringBuilder();
      for ( DBlock b : list ) sb.append( name(b) ).append( " " );
      TDLog.v( "----- start at F " + from + " T " + to + " S " + station + " List " + sb.toString() );
    }

    for ( DBlock blk : list ) {
      if ( LOG ) TDLog.v("process " + id(blk) + " " + name(blk) + " LPB " + LPB() );
      if ( blk.isSecLeg() && prev != null && ! prev.isSplay() ) { // 20250719 new test
        if ( LOG ) TDLog.v("blk " + id(blk) + " secLeg and prev " + id(prev) + " not splay: continue" );
        continue;
      }
      if ( blk.mTo.length() == 0 ) {
        // if ( blk.mFrom.length() == 0 ) {
          markSplay( blk );
          if ( blk.isScan() ) {
            flushLeg( blk, sts, id(blk) + " scan splay" );
            old_leg = null;
            continue;
          }
          if ( blk.isRelativeDistance( prev_prev ) ) {  // prev_prev( leg ) ... blunder (prev) blk
            TDLog.v( id(blk) + "> rel dist prev_prev " + id(prev_prev) );
            blunder   = prev;
            prev      = prev_prev;
            prev_prev = null;
            leg       = prev;
            increaseNrLegShots( blk, sts, "close to " + id(leg) );
          } else if ( blk.isRelativeDistance( leg ) ) {
            TDLog.v( id(blk) + "> rel dist leg " + id(leg) );
            markBlunder(  "close to " + id(leg) );
            increaseNrLegShots( blk, sts, "close to " + id(leg) );
          } else if ( blk.isRelativeDistance( prev ) ) {
            TDLog.v( id(blk) + "> rel dist prev " + id(prev) );
            markBlunder(  "close to " + id(prev) );
            markSecLeg( blk );
            prev_prev = null;
            leg       = prev;
            increaseNrLegShots( blk, sts, "close to " + id(leg) );
          } else if ( blk.isRelativeAngle( leg ) ) {
            TDLog.v( id(blk) + "> rel angle leg " + id(leg) );
            markBlunder(  "angle to " + id(leg) );
            blunder   = blk;
          } else if ( blk.isRelativeAngle( prev ) ) {
            TDLog.v( id(blk) + "> rel angle prev " + id(prev) );
            prev_prev = prev;
            prev      = blk;
            leg       = null;
            blunder   = null;
            old_leg   = null;
          } else {
            if ( backsight_splay && old_leg != null ) {
              TDLog.v( id(blk) + " back splay ");
              blk.setBackSplay();
              mData.updateShotFlag( blk.mId, mSid, blk.getFlagFully() );
              blk.doBacksightSplayCheck( old_leg, false );
            }
            flushLeg( blk, sts, "not close" );
            sec_legs.clear();
            old_leg   = null; // important
            blunder   = null;
            prev_prev = null;
            leg       = null; // no tentative leg
            prev      = blk;
          }
        // } else { // FROM != null   TO == null
        //   if ( LOG ) TDLog.v( "already splay " + id(blk) + " " + blk.mFrom );
        //   if ( blk.isRelativeAngle( prev ) ) prev_prev = prev;
        //   prev = blk;
        // }
      } else { // blk.mTo.length > 0
        if ( blk.mFrom.length() > 0 ) { // FROM non-empty, TO non-empty --> LEG
          TDLog.v("existing leg " + id(blk) + " " + name(blk) );
          leg     = blk;
          old_leg = leg;
          // setFTS( blk, sts );
          setFTS( leg.mFrom, leg.mTo, sts, "[2]" );
          // nrLegShots = TDSetting.mMinNrLegShots;
        } else { // FROM empty, TO non-empty --> rev-SPLAY
          if ( LOG ) TDLog.v( id(blk) + " rev splay - clear nr_legs ");
          sec_legs.clear();
          // nrLegShots = 0;
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
//     // processing skipped shots ...
//     if ( sec_legs.size() > 0 ) {
//       mRet |= assignStations( sec_legs, sts );
//     } else {
//       mCurrentStationName = null;
//     }
//     return mRet;
//   }

     private String FTS() { return from + "/" + to + "/" + station; }

     private String LPB() { return id(leg) + "/" + id(old_leg) + "." + id(prev) + "/" + id(prev_prev) + "." + id(blunder); }
}
