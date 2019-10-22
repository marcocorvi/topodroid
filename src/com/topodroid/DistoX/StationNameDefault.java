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
package com.topodroid.DistoX;

import android.util.Log;

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

  // this is called to renumber a list of blocks - therefore the extend need not be updated
  // @param blk0         reference dblock
  // @param list         list of dblock to assign
  // @param sts          station names already in use
  @Override
  void assignStationsAfter( DBlock blk0, List<DBlock> list, Set<String> sts )
  {
    // Log.v("DistoX-BLOCK", "assign station after");
    ArrayList<DBlock> unassigned = new ArrayList<DBlock>();
    // boolean started = false;

    boolean bs = TDSetting.mDistoXBackshot;

    // TDLog.Log( TDLog.LOG_DATA, "assign stations after " + list.size() + " " + (sts!=null? sts.size():0) );

    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splays = StationPolicy.mShotAfterSplays;
    // Log.v("DistoX-SN", "default assign stations after. blk0 " + blk0.mId + " bs " + bs + " survey_stations " + survey_stations + " shot_after_splay " + shot_after_splays );

    String main_from = null;
    String main_to   = null;

    // boolean increment = true;
    // boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = blk0;
    String from = bs ? blk0.mTo   : blk0.mFrom;
    String to   = bs ? blk0.mFrom : blk0.mTo;
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

    // Log.v("DistoX-BLOCK", "F " + from + " T " + to + " N " + next + " S " + station );

    // if ( TDLog.LOG_DATA ) {
    //   TDLog.Log( TDLog.LOG_DATA, "assign after: F<" + from + "> T<" + to + "> N<" + next + "> S<" + station + "> CS " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );
    //   StringBuilder sb = new StringBuilder();
    //   for ( String st : sts ) sb.append(st + " " );
    //   TDLog.Log(TDLog.LOG_DATA, "set " + sb.toString() );
    // }

    // int nrLegShots = 0;
    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId ) continue;
      if ( blk.isSplay() ) {
	if ( bs ) { // blk.mTo = station;
          setBlockName( blk, "", station );
	} else { // blk.mFrom = station;
          setBlockName( blk, station, "" );
	}
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
	if ( bs ) {
          // blk.mTo   = from;
          // blk.mFrom = to;
          setBlockName( blk, to, from );
	} else {
          // blk.mFrom = from;
          // blk.mTo   = to;
          setBlockName( blk, from, to );
	}
	sts.add( from );
	sts.add( to );
        // TDLog.Log( TDLog.LOG_DATA, "main leg: " + blk.mId + " F<" + from + "> T<" + to + "> S<" + station + "> bs " + bs );
        // Log.v( "DistoX-BLOCK", "main leg: " + blk.mId + " F<" + from + "> T<" + to + "> S<" + station + "> bs " + bs );
      } else if ( blk.isBackLeg() ) {
	if ( main_from != null /* && main_to != null */ ) {
	  prev = blk;
	  if ( bs ) {
            setBlockName( blk, main_from, main_to );
	  } else {
            setBlockName( blk, main_to, main_from );
	  }
          // TDLog.Log( TDLog.LOG_DATA, "back leg: " + blk.mId + " F<" + main_from + "> T<" + main_to + "> bs " + bs );
	}
	main_from = main_to = null;
      } else {
        // Log.v( "DistoX-BLOCK", "blk is skipped " + blk.mId + " prev " + prev.mId );
	if ( /* started || */ ! blk.isRelativeDistance( prev ) ) {
	  unassigned.add( blk );
	  // started = true;
	}
      }
    }
   
    // processing skipped ahots ...
    if ( unassigned.size() > 0 ) assignStations( unassigned, sts );
  }

  // debug log
  private void logJump( DBlock blk, String from, String to, Set<String> sts )
  {
    if ( TDLog.LOG_SHOT ) {
      try {
        int i1 = Integer.parseInt( from );
        int i2 = Integer.parseInt( to );
        if ( Math.abs(i2-i1) != 1 ) {
          StringBuilder sb = new StringBuilder();
          for ( String st : sts ) sb.append(st + "," );
          TDLog.Error( from + "-" + to + " blk " + blk.mId + " set " + sb.toString() );
        }
      } catch ( NumberFormatException e ) { }
    }
  }

  /** assign station names to shots
   * @param list         list of dblock, including those to assign
   * @param sts          station names already in use
   * DistoX backshot-mode is handled separatedly
   */
  @Override
  void assignStations( List<DBlock> list, Set<String> sts )
  { 
    // TDLog.Log( TDLog.LOG_DATA, "assign stations: list " + list.size() + " sts " + (sts!=null? sts.size():0) );
    // Log.v( "DistoX-BLOCK", "assign stations: list " + list.size() + " sts " + (sts!=null? sts.size():0) );
    // for ( DBlock blk : list ) Log.v("DistoX-BLOCK", "blk " + blk.mId );

    if ( TDSetting.mDistoXBackshot ) {
      assignStationsBackshot( list, sts );
      return;
    }
    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splay = StationPolicy.mShotAfterSplays;

    // // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = null;
    String from = ( forward_shots )? DistoXStationName.mInitialStation  // next FROM station
                                   : DistoXStationName.mSecondStation;
    String to   = ( forward_shots )? DistoXStationName.mSecondStation   // next TO station
                                   : DistoXStationName.mInitialStation;
    String station = ( mCurrentStationName != null )? mCurrentStationName
                   : (shot_after_splay ? from : "");  // splays station

    // TDLog.Log( TDLog.LOG_DATA, "F<" + from + "> T<" + to + "> S<" + station + "> CS " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );
    // if ( TDLog.LOG_DATA ) {
    //   StringBuilder sb = new StringBuilder();
    //   for ( String st : sts ) sb.append(st + " " );
    //   TDLog.Log(TDLog.LOG_DATA, "set " + sb.toString() );
    // }

    int nrLegShots = 0;

    for ( DBlock blk : list ) {
      TDLog.Log( TDLog.LOG_SHOT, blk.mId + " <" + blk.mFrom + "-" + blk.mTo + "> F " + from + " T " + to + " S " + station );
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        // TDLog.Log( TDLog.LOG_DATA, blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );

        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          setBlockName( blk, station, "" );
          // TDLog.Log( TDLog.LOG_DATA, "set prev [1] " + blk.mId + " F<" + blk.mFrom + ">" );
        } else {
          if ( prev.isRelativeDistance( blk ) ) {
            if ( nrLegShots == 0 ) {
              // checkCurrentStationName
              if ( mCurrentStationName != null ) {
                if ( forward_shots ) { 
                  from = mCurrentStationName;
                } else if ( survey_stations == 2 ) {
                  to = mCurrentStationName;
                }
              }
              nrLegShots = 2; // prev and this shot
              // TDLog.Log( TDLog.LOG_DATA, "leg-2 F " + from + " T " + to + " S " + station );
            } else {
              nrLegShots ++;  // one more centerline shot
            }
            if ( nrLegShots == TDSetting.mMinNrLegShots ) {
              legFeedback( );
              mCurrentStationName = null;
              // TDLog.Log( TDLog.LOG_DATA, "PREV " + prev.mId + " nrLegShots " + nrLegShots + " set PREV " + from + "-" + to );

              setBlockName( prev, from, to );
              setLegExtend( prev );
              if ( forward_shots ) {
                station = shot_after_splay  ? to : from;     // splay-station = this-shot-to if splays before shot
                                                             //                 this-shot-from if splays after shot
                from = to;                                   // next-shot-from = this-shot-to
                to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                logJump( blk, from, to, sts );
              } else { // backward_shots
                to   = from;                                     // next-shot-to   = this-shot-from
                from = DistoXStationName.incrementName( from, sts ); // next-shot-from = increment this-shot-from
                station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                                 //                = this-shot-from if splay after shot
                // logJump( blk, to, from, sts );
              }
              // TDLog.Log( TDLog.LOG_DATA, "increment F " + from + " T " + to + " S " + station );
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            setBlockName( blk, station, "" );
            prev = blk;
            // TDLog.Log( TDLog.LOG_DATA, "set prev [2] " + blk.mId + " F<" + blk.mFrom + ">" );
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mTo.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          if ( forward_shots ) {  // : ..., 0-1, 1-2 ==> from=(2) to=Next(2)=3 ie 2-3
            from = blk.mTo;
            to   = from;
            to   = DistoXStationName.incrementName( to, sts );
            logJump( blk, from, to, sts );
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? blk.mTo    // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
                                         : blk.mFrom; // 1-2, 1, 1,   [ 2-3, 2, 2, ... ] ...
            } // otherwise station = mCurrentStationName
          } else { // backward shots: ..., 1-0, 2-1 ==> from=Next(2)=3 to=2 ie 3-2
            to   = blk.mFrom;
            from = to;
            from = DistoXStationName.incrementName( from, sts ); // FIXME it was old from
            // logJump( blk, to, from, sts );

	    // station must be set even if there is a "currentStation"
            station = shot_after_splay ? from       // 2,   2, 2, 2-1, [ 3, 3, ..., 3-2 ]  ...
                                       : blk.mFrom; // 2-1, 2, 2, 2,   [ 3-2, 3, 3, ... 3 ] ...
          }
          // TDLog.Log( TDLog.LOG_DATA, "ID " + blk.mId + ": " + blk.mFrom + " - " + blk.mTo + " F " + from + " T " + to + " S " + station );

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

  // ---------------------------------- backshot -----------------------------------
  
  // DistoX backshot-mode default station assignment(s)
  @Override
  protected void assignStationsBackshot( List<DBlock> list, Set<String> sts )
  { 
    // Log.v("DistoX", "assign stations default. size " + list.size() );
    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splay = StationPolicy.mShotAfterSplays;

    // // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = null;
    String from = ( forward_shots )? DistoXStationName.mInitialStation  // next FROM station
                                   : DistoXStationName.mSecondStation;
    String to   = ( forward_shots )? DistoXStationName.mSecondStation   // nect TO station
                                   : DistoXStationName.mInitialStation;
    String station = ( mCurrentStationName != null )? mCurrentStationName
                   : (shot_after_splay ? from : "");  // splays station
    // Log.v("DistoX", "assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // Log.v("DistoX", "Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );

    int nrLegShots = 0;

    for ( DBlock blk : list ) {
      if ( blk.mTo.length() == 0 ) // this implies blk.mFrom.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );

        if ( prev == null ) {
          prev = blk;
          // blk.mTo = station;
          setBlockName( blk, "", station );
          // TDLog.Log( TDLog.LOG_DATA, "set prev [3] " + blk.mId + " T<" + blk.mTo + ">" );
        } else {
          if ( prev.isRelativeDistance( blk ) ) {
            if ( nrLegShots == 0 ) {
              // checkCurrentStationName
              if ( mCurrentStationName != null ) {
                if ( forward_shots ) { 
                  from = mCurrentStationName;
                } else if ( survey_stations == 2 ) {
                  to = mCurrentStationName;
                }
              }
              nrLegShots = 2; // prev and this shot
            } else {
              nrLegShots ++;  // one more centerline shot
            }
            if ( nrLegShots == TDSetting.mMinNrLegShots ) {
              legFeedback( );
              mCurrentStationName = null;
              // TDLog.Log( TDLog.LOG_DATA, "PREV " + prev.mId + " nrLegShots " + nrLegShots + " set PREV <" + to + "-" + from + ">" );
              setBlockName( prev, to, from );
              setLegExtend( prev );
              if ( forward_shots ) {
                station = shot_after_splay  ? to : from;     // splay-station = this-shot-to if splays before shot
                                                             //                 this-shot-from if splays after shot
                from = to;                                   // next-shot-from = this-shot-to
                to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                // TDLog.Log( TDLog.LOG_DATA, "station [3] " + station + " FROM " + from + " TO " + to );
              } else { // backward_shots
                to   = from;                                     // next-shot-to   = this-shot-from
                from = DistoXStationName.incrementName( from, sts ); // next-shot-from = increment this-shot-from
                station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                                 //                = this-shot-from if splay after shot
                // TDLog.Log( TDLog.LOG_DATA, "station [5] " + station + " FROM " + from + " TO " + to );
              }
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            setBlockName( blk, "", station );
            prev = blk;
            // TDLog.Log( TDLog.LOG_DATA, "set prev [4] " + blk.mId + " T<" + blk.mTo + ">" );
          }
        }
      }
      else // blk.mTo.length > 0
      {
        if ( blk.mFrom.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          if ( forward_shots ) {  // : ..., 0-1, 1-2 ==> from=(2) to=Next(2)=3 ie 2-3
            from = blk.mFrom;
            to   = from;
            to   = DistoXStationName.incrementName( to, sts );
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? blk.mFrom  // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
                                         : blk.mTo;   // 1-2, 1, 1,   [ 2-3, 2, 2, ... ] ...
            } // otherwise station = mCurrentStationName
          } else { // backward shots: ..., 1-0, 2-1 ==> from=Next(2)=3 to=2 ie 3-2
            to   = blk.mTo;
            from = to;
            from = DistoXStationName.incrementName( from, sts ); // FIXME it was old from
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? from       // 2,   2, 2, 2-1, [ 3, 3, ..., 3-2 ]  ...
                                         : blk.mTo;   // 2-1, 2, 2, 2,   [ 3-2, 3, 3, ... 3 ] ...
            } // otherwise station = mCurrentStationName
          }
          nrLegShots = TDSetting.mMinNrLegShots;
          // TDLog.Log( TDLog.LOG_DATA, "station [6] F<" + from + "> T<" + to + "> S<" + station + ">" );
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }


}
