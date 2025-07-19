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
// import com.topodroid.utils.TDStatus;
// import com.topodroid.common.LegType;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;
// import android.view.View;

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
    // TDLog.v("DATA " + "assign station after " + blk0.mId + " list " + list.size() + " sts " + ((sts!=null)?sts.size():"-") );
    ArrayList< DBlock > sec_legs = new ArrayList<>();

    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splays = StationPolicy.mShotAfterSplays;

    // NOTE on renumber the backsight-splay is not checeked
    // boolean backsight_splay   = forward_shots && shot_after_splays && TDSetting.mBacksightSplay;
    // DBlock  first_splay       = null;

    // String  current_station = mCurrentStationName;
    // TDLog.v( "default assign stations after. blk0 " + blk0.mId + " bs " + bs + " survey_stations " + survey_stations + " shot_after_splay " + shot_after_splays );

    String main_from = null;
    String main_to   = null;

    NativeName mNativeName = null;
    try {
      mNativeName = new NativeName();
      // TDLog.v( "Using native name lib" );
    } catch ( java.lang.UnsatisfiedLinkError e ) {
      TDLog.e("Native link error " + e.getMessage() );
      mNativeName = null;
    }

    // boolean increment = true;
    // TDLog.v( "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    // [1] initialize prev (DBlock), and strings from, to, next, station
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
      next = ( mNativeName != null )? mNativeName.incrementName( next, sts ) : DistoXStationName.incrementName( next, sts );
      station = shot_after_splays ? to : from;
    } else {
      next = from;
      next = ( mNativeName != null )? mNativeName.incrementName( next, sts ) : DistoXStationName.incrementName( next, sts );
      station = shot_after_splays ? next : from;
    }

    TDLog.v( "assign stations after " + blk0.mId + " F " + from + " T " + to + " N " + next + " S " + station );
    // if ( TDLog.LOG_DATA ) {
    //   TDLog.v( "assign after: F<" + from + "> T<" + to + "> N<" + next + "> S<" + station + "> CS " + ( (current_station==null)? "null" : current_station ) );
    //   StringBuilder sb = new StringBuilder();
    //   for ( String st : sts ) sb.append(st + " " );
    //   TDLog.v( "set " + sb.toString() );
    // }

    // [2] scan the list of DBlock's

    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId || blk.isSecLeg() ) continue; // 20250719 replaces
      // if ( blk.mId == blk0.mId ) continue;
        
      if ( blk.isScan() ) {
        setSplayName( blk, station );
	sts.add( station );
      } else if ( blk.isSplay() ) {
        if ( TDSetting.mSplayStation || blk.mFrom.length() == 0 ) { // mSplayStation 
          if ( TDLog.isStreamFile() ) TDLog.e("  set splay " + name(blk) + " => " + station + " add to station-set" );
          setSplayName( blk, station );
	  sts.add( station );
        }
        // TDLog.v( "splay " + blk.mId + " S<" + station + "> bs " + bs );
      } else if ( blk.isMainLeg() ) {
	prev = blk;
        if ( forward_shots ) {
          from = to;
          to   = next;
          next = ( mNativeName != null )? mNativeName.incrementName( next, sts ) : DistoXStationName.incrementName( next, sts ); // to, sts
          station = shot_after_splays ? to : from;
        } else {
          to   = from;
          from = next;
          next = ( mNativeName != null )? mNativeName.incrementName( next, sts ) : DistoXStationName.incrementName( next, sts ); // from, sts
          station = shot_after_splays ? next : from;
        }
	main_from = from;
	main_to   = to;
        // blk.mFrom = from;
        // blk.mTo   = to;
        if ( TDLog.isStreamFile() ) TDLog.e("  set main leg " + name(blk) + " => " + from + " " + to + " {" + station + "}" );
        setLegName( blk, from, to );
        ret = true;
	sts.add( from );
	sts.add( to );
        // TDLog.v( "main leg: " + blk.mId + " F<" + from + "> T<" + to + "> S<" + station + "> bs " + bs );
      } else if ( blk.isBackLeg() ) {
	if ( main_from != null /* && main_to != null */ ) {
	  prev = blk;
          setLegName( blk, main_to, main_from );
          ret = true;
          // TDLog.v( "back leg: " + blk.mId + " F<" + main_from + "> T<" + main_to + "> bs " + bs );
	}
	main_from = main_to = null;
      } else {
        // TDLog.v( "blk is skipped " + blk.mId + " prev " + prev.mId );
	if ( ! blk.isRelativeDistance( prev ) ) {
          sec_legs.add( blk );
        } else {
          setSecLegName( blk );
        }
      }
    }
   
    // processing skipped shots ...
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
  //         TDLog.e( from + "-" + to + " blk " + blk.mId + " set " + sb.toString() );
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
    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return false; // assign always false with no policy

    if ( TDSetting.mBlunderShot ) {
      return (new StationNameDefaultBlunder( mContext, mData, mSid )).assignStations( list, sts );
    }

    TDLog.v("Station Name Default assign stations all: list " + list.size() + " stations " + sts.size() );

    NativeName mNativeName = new NativeName();

    boolean ret = false;
    boolean forward_shots    = ( survey_stations == 1 );
    boolean shot_after_splay = StationPolicy.mShotAfterSplays;
    boolean backsight_splay  = forward_shots && shot_after_splay && TDSetting.mBacksightSplay;
    DBlock  first_splay      = null; // forward leg

    String  current_station  = mCurrentStationName; // steal current station name
    mCurrentStationName = null;

    DBlock prev = null;
    String from = ( forward_shots )? DistoXStationName.mInitialStation  // next FROM station
                                   : DistoXStationName.mSecondStation;
    String to   = ( forward_shots )? DistoXStationName.mSecondStation   // next TO station
                                   : DistoXStationName.mInitialStation;
    String station = ( current_station != null )? current_station
                   : (shot_after_splay ? from : "");  // splays station

    // StringBuilder sb0 = new StringBuilder();
    // for ( DBlock b : list ) sb0.append( name(b) ).append(" ");
    // TDLog.v("ASSIGN stations " + list.size() + ": from " + from + " to " + to + " station " + station + " " + sb0.toString() );

    if ( TDLog.isStreamFile() ) {
      TDLog.e("ASSIGN " + TDLog.threadId() + " shots " + list.size() + " stations " + (sts!=null? sts.size():"-") );
      StringBuilder sb = new StringBuilder();
      for ( DBlock b : list ) sb.append( name(b) ).append(" ");
      if ( sts != null ) {
        sb.append("Sts ");
        for ( String st : sts ) {
          sb.append( st ).append(" ");
        }
      }
      TDLog.e( "  {" + from + " " + to + " " + station + "} Blks " + sb.toString() );
    }

    int nrLegShots = 0;
    ArrayList< DBlock > sec_legs = new ArrayList<>();

    for ( DBlock blk : list ) {
      if ( TDLog.isStreamFile() ) TDLog.e("  process " + name(blk) + " prev " + id(prev) );
      if ( blk.isSecLeg() && prev != null && ! prev.isSplay() ) { // 20250719 new test
        // TDLog.v("20250719 skip block marked sec-leg: " + blk.mId + " prev " + ( prev==null? "null" : prev.mId ) );
        continue; 
      }
      // TDLog.v("  process " + name(blk) + " prev " + id(prev) );
      if ( blk.mFrom.length() == 0 ) {
        if ( blk.isScan() ) {
          nrLegShots = 0;
          setSplayName( blk, station );
          // TDLog.v("20250719 (1) set " + blk.mId + " splay " + station );
          prev = null;
          continue;
        }
        if ( blk.mTo.length() == 0 ) { // unassigned splay
          // TDLog.v("  unassigned splay " + id(blk) );
          if ( prev == null ) {
            if ( TDLog.isStreamFile() ) TDLog.e("  null prev: splay " + id(blk) + " : " + station );
            prev = blk;
            // blk.mFrom = station;
            setSplayName( blk, station );
            // TDLog.v("20250719 (2) set " + blk.mId + " splay " + station );
            first_splay = null;
            // TDLog.v("  null prev: splay " + id(blk) + " : " + station );
          } else {
            if ( prev.isRelativeDistance( blk ) ) {
              if ( TDLog.isStreamFile() ) TDLog.e("  close to prev " + id(prev) + ": sec-leg " + id(blk) + " nr. legs " + nrLegShots );
              // TDLog.v("  close to prev " + id(prev) + ": sec-leg " + id(blk) + " nr. legs " + nrLegShots );
              sec_legs.add( blk );
              if ( nrLegShots == 0 ) {
                // checkCurrentStationName
                if ( current_station != null ) {
                  if ( forward_shots ) { 
                    from = current_station;
                  } else if ( survey_stations == 2 ) {
                    to = current_station;
                  }
                  if ( TDLog.isStreamFile() ) TDLog.e( "  update {" + from + " " + to + " " + station + "}" );
                  // TDLog.v( "  update {" + from + " " + to + " " + station + "}" );
                }
                nrLegShots = 2; // prev and this shot
              } else {
                nrLegShots ++;  // one more centerline shot
              }
              if ( nrLegShots == TDSetting.mMinNrLegShots ) {
                // if ( current_station == null ) { // DEBUG_NAMES
                //   try { // this is DistoXStationName.isJump( from, to )
                //     int f = Integer.parseInt( from );
                //     int t = Integer.parseInt( to );
                //     if ( t - f > 1 ) {
                //       TDLog.e("LOG to FILE bad increment " + f + " --> " + t );
                //       TDLog.setLogStream( TDLog.LOG_FILE );
                //       TDLog.e("LOG bad increment [1] " + from + " - " + to + " native " + (mNativeName != null) );
                //     } else {
                //       // TDLog.v("normal increment " + from + " --> " + to );
                //     }
                //   } catch ( NumberFormatException e ) {
                //     TDLog.e("non-numeric increment " + from + " --> " + to );
                //   }
                // } else {
                //   // TDLog.v("current-station increment " + from + " --> " + to );
                // }
                legFeedback( );
                current_station = null;
                if ( TDLog.isStreamFile() ) TDLog.e("  set leg " + name(prev) + " => " + from + " " + to + " {" + station + "}" );
                setLegName( prev, from, to );
                // TDLog.v("20250719 (3) set " + prev.mId + " leg " + from + " " + to );
                first_splay = prev;
                ret = true;
                setLegExtend( prev );
                if ( forward_shots ) {
                  station = shot_after_splay  ? to : from;     // splay-station = this-shot-to if splays before shot
                                                               //                 this-shot-from if splays after shot
                  from = to;                                   // next-shot-from = this-shot-to
                  to = ( mNativeName != null )? mNativeName.incrementName( to, sts ) : DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                  // logJump( blk, from, to, sts ); // NO_LOGS
                  // TDLog.v("native increment FWD set station * " + station );
                  if ( current_station == null ) { // DEBUG_NAMES
                    try { 
                      int f = Integer.parseInt( from );
                      int t = Integer.parseInt( to );
                      if ( t - f > 1 ) {
                        TDLog.e("LOG to FILE bad increment " + f + " --> " + t );
                        TDLog.setLogStream( TDLog.LOG_FILE );
                        TDLog.e("LOG bad increment [2] " + from + " - " + to + " sttaion " + station + " native " + (mNativeName != null) );
                      } else {
                        // TDLog.v("normal increment " + from + " --> " + to );
                      }
                    } catch ( NumberFormatException e ) {
                      TDLog.e("non-numeric increment " + from + " --> " + to );
                    }
                  }
                } else { // backward_shots
                  to   = from;                                     // next-shot-to   = this-shot-from
                  from = ( mNativeName != null )? mNativeName.incrementName( from, sts ) : DistoXStationName.incrementName( from, sts ); // next-shot-from = increment this-shot-from
                  station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                                   //                = this-shot-from if splay after shot
                  // logJump( blk, to, from, sts );
                  // TDLog.v("native increment BCK set station * " + station );
                }
                if ( TDLog.isStreamFile() ) TDLog.e("  set leg " + name(prev) + " Now {" + from + " " + to + " " + station + "}" );
                // TDLog.v("  set leg " + name(prev) + " Now {" + from + " " + to + " " + station + "}" );
                for ( DBlock b : sec_legs ) {
                  setSecLegName( b );
                  // TDLog.v("20250719 (3) set " + b.mId + " sec-leg " );
                }
                sec_legs.clear();
              } else {
                if ( TDLog.isStreamFile() ) TDLog.e("  set sec-leg " + id(blk) );
                setSecLegName( blk );
                // TDLog.v("20250719 (4) set " + blk.mId + " sec-leg " );
              }
            } else { // distance from prev > "closeness" setting
              if ( backsight_splay && first_splay != null ) {
                blk.doBacksightSplayCheck( first_splay );
                first_splay = null;
              }
              if ( TDLog.isStreamFile() ) TDLog.e("  set splay " + id(blk) + " : " + station + " / prev " + id(prev) + " => " + id(blk) );
              nrLegShots = 0;
              setSplayName( blk, station );
              // TDLog.v("20250719 (5) set " + blk.mId + " splay " + station );
              prev = blk;
              // TDLog.v("  set splay " + name(blk) + " set prev " + id(prev) + " station " + station );
            }
          }
        } else { // blk.mTo.length() > 0 : blk already SPLAY
          if ( TDLog.isStreamFile() ) TDLog.e("  already splay " + name(blk) + " / prev " + id(prev) + " => " + id(blk) );
          nrLegShots = 0;
          prev = blk;
          // TDLog.v("  already splay " + name(blk) + " set prev " + id(prev) );
        }
      } else { // blk.mFrom.length > 0
        if ( blk.mTo.length() > 0 ) { // FROM non-empty, TO non-empty --> LEG
          if ( forward_shots ) {  // : ..., 0-1, 1-2 ==> from=(2) to=Next(2)=3 ie 2-3
            from = blk.isDistoXBacksight()? blk.mFrom : blk.mTo;
            to   = from;
            to   = ( mNativeName != null )? mNativeName.incrementName( to, sts ) : DistoXStationName.incrementName( to, sts );
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
            // TDLog.v(" FWD set station " + station );
            if ( current_station == null ) { // DEBUG_NAMES
              try { 
                int f = Integer.parseInt( from );
                int t = Integer.parseInt( to );
                if ( t - f > 1 ) {
                  TDLog.e("LOG to FILE bad increment " + f + " --> " + t );
                  TDLog.setLogStream( TDLog.LOG_FILE );
                  TDLog.e("LOG bad increment [3] " + from + " - " + to + " station " + station + " native " + (mNativeName != null) );
                } else {
                  // TDLog.v("normal increment " + from + " --> " + to );
                }
              } catch ( NumberFormatException e ) {
                TDLog.e("non-numeric increment " + from + " --> " + to );
              }
            }
          } else { // backward shots: ..., 1-0, 2-1 ==> from=Next(2)=3 to=2 ie 3-2
            to = blk.isDistoXBacksight()? blk.mTo : blk.mFrom;
            from = to;
            from = ( mNativeName != null )? mNativeName.incrementName( from, sts ) : DistoXStationName.incrementName( from, sts ); // FIXME it was old from
            // logJump( blk, to, from, sts );

	    // station must be set even if there is a "currentStation" FIXME FIXME FIXME 20231231
            if ( current_station == null ) {
              if ( blk.isDistoXBacksight() ) {
                station = shot_after_splay ? from   
                                           : blk.mTo;
              } else {
                station = shot_after_splay ? from       // 2,   2, 2, 2-1, [ 3, 3, ..., 3-2 ]  ...
                                           : blk.mFrom; // 2-1, 2, 2, 2,   [ 3-2, 3, 3, ... 3 ] ...
              }
            }
            // TDLog.v(" BCK set station " + station );
          }
          nrLegShots = TDSetting.mMinNrLegShots;
          first_splay = blk;
          if ( TDLog.isStreamFile() ) TDLog.e("  already leg " + name(blk) + " {" + from + " " + to + " " + station + "}" );
          // TDLog.v("  already leg " + name(blk) + " {" + from + " " + to + " " + station + "}" );
        } else { // FROM non-empty, TO empty --> SPLAY
          first_splay = null;
          nrLegShots = 0;
          if ( TDLog.isStreamFile() ) TDLog.e("  already splay " + name(blk) + " will set prev : old " + id(prev) );
          // TDLog.v("  already splay " + name(blk) + " will set prev : old " + id(prev) );
        }
        prev = blk;
      }
    }
    mCurrentStationName = current_station; // reset current station name
    return ret;
  }
}
