/* @file StationNameBacksight.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid station naming for backsight policy
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * N.B. backsight and tripod need the whole list to decide how to assign names
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.LegType;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;

class StationNameBacksight extends StationName
{
  StationNameBacksight( Context ctx, DataHelper data, long sid ) 
  {
    super( ctx, data, sid );
  }

  // @param list list of dblock to assign
  // called by TopoDroidApp
  @Override
  boolean assignStationsAfter( DBlock blk0, List< DBlock > list, Set<String> sts )
  { 
    boolean ret = false;
    ArrayList< DBlock > unassigned = new ArrayList<>();
    // TDLog.v( "assign stations after - backsight");
    // TDLog.v( "BACKSIGHT assign stations after " + blk0.mFrom + "-" + blk0.mTo + " Size " + list.size() );
    boolean increment = true;

    DBlock prev = blk0;
    String from = null;
    String to   = null;
    String next;
    String station;
    String oldFrom;
    float fore_length  = 0;
    float fore_bearing = 0;
    float fore_clino   = 0;

    if ( blk0.isDistoXBacksight() ) { // blk0 is DistoX backsight
      from = blk0.mTo;
      to   = blk0.mFrom;
      oldFrom = blk0.mTo;
    } else { // blk0 is DistoX normal
      from = blk0.mFrom;
      to   = blk0.mTo;
      oldFrom = blk0.mFrom;
    }

    if ( DistoXStationName.isLessOrEqual( from, to ) ) { // forward
      // TDLog.v( "blk0 " + blk0.mFrom + " " + blk0.mTo + " foresight" + blk0.isBackLeg() );
      // blk0 is foresight 1---2 
      //          from --- blk0 ---> to ----- next
      //                          station
      station = to;
      next = DistoXStationName.incrementName( station, sts );
      fore_length  = blk0.mLength;
      fore_bearing = blk0.mBearing;
      fore_clino   = blk0.mClino;
    } else { // backward: blk0.isBackLeg();
      // TDLog.v( "blk0 " + blk0.mFrom + " " + blk0.mTo + " backsight " + blk0.isBackLeg() );
      // blk0 is backsight 2---1
      //          (to) <--- blk0 ---- from ---- to ---- next
      //         old_from            station
      increment = false;
      station = from;
      to   = DistoXStationName.incrementName( from, sts );
      next = DistoXStationName.incrementName( to, sts );
    }
    // TDLog.v( "FROM " + from + " TO " + to + " NEXT " + next + " STATION " + station + " increment " + increment );

    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId ) continue;
      if ( blk.isSplay() ) {
        setSplayName( blk, station );
	sts.add( station );
      } else if ( blk.isLeg() ) {
	prev = blk;
        String p_to;
        boolean is_backsight_shot = checkBacksightShot( blk, fore_length, fore_bearing, fore_clino ); 
        // TDLog.v( blk.mFrom + " " + blk.mTo + " backsight? " + is_backsight_shot );
        if ( is_backsight_shot ) {
          p_to = oldFrom; 
          from = to;
          station = from;
	  mData.updateShotLegFlag( blk.mId, mSid, LegType.BACK, DBlock.FLAG_DUPLICATE );
        } else {  // forward
          if ( increment ) {
            from = to;
            to   = next;
            next = DistoXStationName.incrementName( to, sts );
          } else {
            increment = true;
          }
          p_to = to;
          oldFrom = from;
          station = to;
          fore_length  = blk.mLength;
          fore_bearing = blk.mBearing;
          fore_clino   = blk.mClino;
        }
        setLegName( blk, from, p_to, is_backsight_shot );
        ret = true;
	sts.add( from );
	sts.add( p_to );
      } else {
	if ( ! blk.isRelativeDistance( prev ) ) {
	  unassigned.add( blk );
	}
      } 
    }
    if ( unassigned.size() > 0 ) ret |= assignStations( unassigned, sts );
    return ret;
  }

  // DistoX backshot-mode is handled separately
  @Override
  boolean assignStations( List< DBlock > list, Set<String> sts )
  { 
    boolean ret = false;
    DBlock prev = null;
    String from = DistoXStationName.mInitialStation;
    String to   = DistoXStationName.mSecondStation;
    String oldFrom = "empty"; // FIXME
    float fore_length  = 0;
    float fore_bearing = 0;
    float fore_clino   = 0;

    String station = ( mCurrentStationName != null )? mCurrentStationName : from;
    // TDLog.v( "assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // TDLog.v( "Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );

    int nrLegShots = 0;
    // TDLog.v( "FROM " + from + " TO " + to + " STATION " + station );

    for ( DBlock blk : list ) {
      if ( blk.mFrom.length() == 0 ) {
        if ( blk.mTo.length() == 0 ) {
          if ( prev == null ) { // blk is (possibly) splay
            prev = blk;
            // blk.mFrom = station;
            setSplayName( blk, station );
          } else {
            if ( prev.isRelativeDistance( blk ) ) { // check if leg
              if ( nrLegShots == 0 ) {
                if ( mCurrentStationName != null ) { // checkCurrentStationName
                  from = mCurrentStationName;
                }
                nrLegShots = 2; // prev and this shot
              } else {
                nrLegShots ++;  // one more centerline shot
              }
              if ( nrLegShots == TDSetting.mMinNrLegShots ) {
                legFeedback( );
                mCurrentStationName = null;
                String prev_from = from;
                String prev_to   = to;
                boolean is_backsight_shot = checkBacksightShot( prev, fore_length, fore_bearing, fore_clino);
                if ( is_backsight_shot ) { 
                                       // 2 backsight backward shot from--old_from
                  prev_to = oldFrom;   // 1
                  station = from;
	          // TDLog.v( "set " + prev.mId + " back leg and dup ");
	          mData.updateShotLegFlag( prev.mId, mSid, LegType.BACK, DBlock.FLAG_DUPLICATE );
                } else {               // 2 backsight forward shot from--to
                  // prev_to = to;     // 3
                  oldFrom = from;      // 2
                  from    = to;        // 3
                  station = to;
                  to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from          
                  fore_length  = prev.mLength;
                  fore_bearing = prev.mBearing;
                  fore_clino   = prev.mClino;
                }
                setLegName( prev, prev_from, prev_to, is_backsight_shot );
                ret = true;
                setLegExtend( prev );
                // TDLog.v( "FROM " + from + " TO " + to + " STATION " + station + " P_FROM " + prev_from + " P_TO " + prev_to + " backshot " + is_backsight_shot );
              }
            } else { // no more leg: (possibly) a splay - distance from prev > "closeness" setting
              if ( nrLegShots > 0 ) {
                if ( mCurrentStationName == null ) {
                  station = from;
                } // otherwise station = mCurrentStationName;
              }
              nrLegShots = 0;
              setSplayName( blk, station );
              prev = blk;
            }
          }
        } else { // blk.mTo.length() > 0 : blk is already SPLAY
          nrLegShots = 0;
          prev = blk;
        }
      } else { // blk.mFrom.length > 0
        if ( blk.mTo.length() > 0 ) { // FROM non-empty, TO non-empty --> already LEG
          String leg_to = blk.isDistoXBacksight()? blk.mFrom : blk.mTo;
          if ( ! leg_to.equals( oldFrom ) ) { // this is a backshot
            if ( blk.isDistoXBacksight() ) {
              oldFrom = blk.mTo;
              from    = blk.mFrom;
            } else {
              oldFrom = blk.mFrom;
              from    = blk.mTo;
            }
            to      = DistoXStationName.incrementName( from, sts );
            if ( mCurrentStationName == null ) {
              if ( blk.isDistoXBacksight() ) {
                station = blk.mFrom;
              } else {
                station = blk.mTo;
              }
            } // otherwise station = mCurrentStationName
            fore_length  = blk.mLength;
            fore_bearing = blk.mBearing;
            fore_clino   = blk.mClino;
          // } else { // this is a foreshot
          }
          nrLegShots = TDSetting.mMinNrLegShots;
          // TDLog.v( "FROM " + from + " TO " + to + " STATION " + station + " OLD FROM " + oldFrom );
        } else { // FROM non-empty, TO empty --> already SPLAY
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
    return ret;
  }
  
}
