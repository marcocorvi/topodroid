/* @file StationNameTripod.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid station naming for tripod policy
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * N.B. backsight and tripod need the whole list to decide how to assign names
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;

class StationNameTripod extends StationName
{
  StationNameTripod( Context ctx, DataHelper data, long sid ) 
  {
    super( ctx, data, sid );
  }

  // ----------------------------------------------------------------
  // @param list list of dblock to assign
  @Override
  boolean assignStationsAfter( DBlock blk0, List< DBlock > list, Set<String> sts )
  { 
    boolean ret = false;
    ArrayList< DBlock > unassigned = new ArrayList<>();

    // TDLog.v( "assign stations after - tripod");
    // TDLog.v( "assign stations after.  size " + list.size() );
    boolean increment = true;
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = blk0;
    String from = null;
    String to   = null;
    String back = null;
    if ( blk0.isDistoXBacksight() ) {
      from = blk0.mTo;
      to   = blk0.mFrom;
      back = blk0.mFrom;
    } else { // blk0 is DistoX normal
      from = blk0.mFrom;
      to   = blk0.mTo;
      back = blk0.mTo;
    }
    if ( DistoXStationName.isLessOrEqual( from, to ) ) { // forward
      flip = true;
      // move next
      // back = blk0.mTo;
      from = DistoXStationName.incrementName( to, sts );
    } else { // backward
      // increment = false;
      flip = false; // already assigned
    }

    String next = DistoXStationName.incrementName( from, sts );
    String station = from;
    // TDLog.v( "*    " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
    String main_from = null;
    String main_to   = null;

    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId ) continue;
      if ( blk.isSplay() ) {
        if ( flip ) flip = false;
        setSplayName( blk, station );
	sts.add( station );
        // TDLog.v( "S:"+ station + "   " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
      } else if ( blk.isMainLeg() ) { // tripod renumber includes only main legs
        prev = blk;
        String p_from = from;
        String p_to   = next;
        if ( flip ) { // backward
          flip = false;
          p_to = back; 
        } else {  // forward
          flip = true;
          if ( increment ) { // ALWAYS true
            // move for
            back = next;
            from = DistoXStationName.incrementName( next, sts ); 
            next = DistoXStationName.incrementName( from, sts );
            station = from;
          } else {
            increment = true;
          }
        }
	main_from = p_from;
	main_to   = p_to;
        setLegName( blk, p_from, p_to );
        ret = true;
	sts.add( p_from );
	sts.add( p_to );
        // TDLog.v( "L:"+from+"-"+ p_to + " " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
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

  @Override
  boolean assignStations( List< DBlock > list, Set<String> sts )
  { 
    boolean ret = false;
    DBlock prev = null;
    String from = DistoXStationName.mSecondStation;     // 1
    String back = DistoXStationName.mInitialStation;    // 0
    String next = DistoXStationName.incrementName( from, sts );  // 2
    boolean flip = true; // whether to swap leg-stations (backsight backward shot)
    boolean is_fixed_extend = TDAzimuth.isFixedExtend();
    long fore_extend = TDAzimuth.getFixedExtend();
    long back_extend = - fore_extend;

    String station = ( mCurrentStationName != null )? mCurrentStationName : from;
    int nrLegShots = 1;

    for ( DBlock blk : list ) {
      // TDLog.v( blk.mId + " <" + blk.mFrom + ">-<" + blk.mTo + "> F " + from + " T " + back + " N " + next );
      if ( blk.mFrom.length() == 0 ) {
        if ( blk.mTo.length() == 0 ) {
          if ( prev == null ) { // possible SPLAY
            prev = blk;
            // blk.mFrom = station;
            setSplayName( blk, station );
          } else {
            if ( prev.isRelativeDistance( blk ) ) {
              if ( nrLegShots == 0 ) {
                // checkCurrentStationName
                if ( mCurrentStationName != null ) {
                  // if ( forward_shots ) { 
                    from = mCurrentStationName;
                  // } else if ( survey_stations == 2 ) {
                  //   back = mCurrentStationName;
                  // }
                }
                nrLegShots = 2; // prev and this shot
              } else {
                nrLegShots ++;  // one more centerline shot
              }
              if ( nrLegShots == TDSetting.mMinNrLegShots ) {
                legFeedback( );
                mCurrentStationName = null;
                String prev_from = from;
                String prev_to   = back;
                if ( flip ) {  // measuring FROM ==> BACK
                  flip = false;
                } else {       // measuring FROM ==> NEXT
                  flip = true;
                  prev_to = next;
                  // move forward 
                  back   = next;
                  from = DistoXStationName.incrementName( next, sts );
                  next = DistoXStationName.incrementName( from, sts ); 
                }
                station = from;
                setLegName( prev, prev_from, prev_to );
                ret = true;
	        if ( is_fixed_extend ) {
                  setLegFixedExtend( prev, (flip? fore_extend : back_extend) ); // flip is set when FROM ==> NEXT
	        } else {
                  setLegExtend( prev );
	        }
              }
            } else { // distance from prev > "closeness" setting : possible SPLAY
              if ( nrLegShots == 0 ) {
                if ( flip ) {
                  flip = false;
                  if ( prev != null ) {
                    if ( prev.isDistoXBacksight() ) {
                      if ( prev.mFrom.length() == 0 && ! prev.mTo.equals( station ) ) {
                        setSplayName( prev, station );
                      }
                    } else {
                      if ( prev.mTo.length() == 0 && ! prev.mFrom.equals( station ) ) {
                        setSplayName( prev, station );
                      }
                    }
                  }
                }
              // } else { // only when coming from a LEG
              //   // if ( mCurrentStationName == null ) {
              //   //   station = from;
              //   // }
              }
              nrLegShots = 0;
              setSplayName( blk, station );
              // TDLog.v( "non-close: b " + blk.mId + " <" + blk.mFrom + "> " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
              prev = blk;
            }
          }
        } else { // blk already SPLAY
          nrLegShots = 0;
          prev = blk;
        }
      } else { // blk.mFrom.length > 0
        if ( blk.mTo.length() > 0 ) { // FROM non-empty, TO non-empty --> LEG
          // TDLog.v( blk.mId + " [" + blk.mFrom + "-" + blk.mTo + "] " + from + "-" + back + "-" + next + " " + station );
          String blk_from = blk.mFrom;
          String blk_to   = blk.mTo;
          if ( blk.isDistoXBacksight() ) {
            blk_from = blk.mTo;
            blk_to   = blk.mFrom;
          }
          if ( DistoXStationName.isLessOrEqual( blk_from, blk_to ) ) { // forward shot
            flip = true;
            back = blk_to;
            from = DistoXStationName.incrementName( back, sts );
            next = DistoXStationName.incrementName( from, sts );
          } else { // backward shot
            flip = false;
            from = blk_from;
            back = blk_to;
            next = DistoXStationName.incrementName( from, sts );
          }
          if ( mCurrentStationName == null ) station = from;
          nrLegShots = TDSetting.mMinNrLegShots;
        } else { // FROM non-empty, TO empty --> SPLAY
          if ( nrLegShots == 0 ) {
            flip = false;
          }
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
    return ret;
  }

}
