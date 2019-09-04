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
package com.topodroid.DistoX;


// import android.util.Log;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import android.content.Context;
// import android.util.Log;

class StationNameBacksight extends StationName
{
  StationNameBacksight( Context ctx, DataHelper data, long sid ) 
  {
    super( ctx, data, sid );
  }

  // @param list list of dblock to assign
  // called by TopoDroidApp
  @Override
  void assignStationsAfter( DBlock blk0, List<DBlock> list, Set<String> sts )
  { 
    ArrayList<DBlock> unassigned = new ArrayList<DBlock>();
    // boolean started = false;

    // Log.v("DistoX-SN", "assign stations after - backsight");
    boolean bs = TDSetting.mDistoXBackshot; // whether distox is in backshot mode

    // Log.v("DistoX", "BACKSIGHT assign stations after " + blk0.mFrom + "-" + blk0.mTo + " Size " + list.size() );
    boolean increment = true;
    // boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = blk0;
    String from = null;
    String to   = null;
    String next;
    String station;
    String oldFrom;
    float fore_length  = 0;
    float fore_bearing = 0;
    float fore_clino   = 0;
    if ( bs ) {
      from = blk0.mTo;
      to   = blk0.mFrom;
      if ( DistoXStationName.isLessOrEqual( blk0.mTo, blk0.mFrom ) ) {
        // blk0 is backsight 2--1 next is foresight 2--3:
	//      splay station is blk0.from (2)
	//      next station is the increment (3)
	//           from <--- blk0 --- to ---- next
	//                            station
        // flip    = true;
        station = to;
        next = DistoXStationName.incrementName( station, sts );
        fore_length  = blk0.mLength;
        fore_bearing = blk0.mBearing;
        fore_clino   = blk0.mClino;
      } else { // backward
        // blk0 is foresight 1--2 next is backsight 2--1:
	//      splay station is blk0.to (2) so that if backsight fails the shot is taken as splay
	//           (to) --- blk0 ---> from ---- to ---- next
	//         old_from            station
        increment = false;
        // flip    = false;
        station = from;
        to   = DistoXStationName.incrementName( from, sts );
        next = DistoXStationName.incrementName( to, sts );
      }
      oldFrom = blk0.mTo;
    } else { // DistoX normal
      from = blk0.mFrom;
      to   = blk0.mTo;
      if ( DistoXStationName.isLessOrEqual( blk0.mFrom, blk0.mTo ) ) { // forward
        // Log.v("DistoX-SN", "blk0 " + blk0.mFrom + " " + blk0.mTo + " foresight" + blk0.isBackLeg() );
	// blk0 is foresight 1---2 
	//          from --- blk0 ---> to ----- next
	//                          station
        // flip    = true;
        station = to;
        next = DistoXStationName.incrementName( station, sts );
        fore_length  = blk0.mLength;
        fore_bearing = blk0.mBearing;
        fore_clino   = blk0.mClino;
      } else { // backward: blk0.isBackLeg();
        // Log.v("DistoX-SN", "blk0 " + blk0.mFrom + " " + blk0.mTo + " backsight " + blk0.isBackLeg() );
	// blk0 is backsight 2---1
	//          (to) <--- blk0 ---- from ---- to ---- next
	//         old_from            station
        increment = false;
        // flip    = false;
        station = from;
        to   = DistoXStationName.incrementName( from, sts );
        next = DistoXStationName.incrementName( to, sts );
      }
      oldFrom = blk0.mFrom;
    }
    // Log.v("DistoX", "FROM " + from + " TO " + to + " NEXT " + next + " STATION " + station + " increment " + increment );

    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId ) continue;
      if ( blk.isSplay() ) {
        // if ( flip ) { 
        //   flip = false;
        // }
	if ( bs ) { // blk.mTo = station;
          setBlockName( blk, "", station );
	} else { // blk.mFrom = station;
          setBlockName( blk, station, "" );
	}
	sts.add( station );
      } else if ( blk.isLeg() ) {
	prev = blk;
        String p_to;
        boolean is_backsight_shot = checkBacksightShot( blk, fore_length, fore_bearing, fore_clino ); 
        // Log.v("DistoX-SN", blk.mFrom + " " + blk.mTo + " backsight? " + is_backsight_shot );
        if ( /* flip && */ is_backsight_shot ) {
          // flip = false;
          p_to = oldFrom; 
          from = to;
          station = from;
	  mData.updateShotLegFlag( blk.mId, mSid, LegType.BACK, DBlock.FLAG_DUPLICATE, true ); // true = forward
        } else {  // forward
          // flip = true;
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
	if ( bs ) {
          setBlockName( blk, p_to, from, is_backsight_shot );
	} else {
          setBlockName( blk, from, p_to, is_backsight_shot );
	}
	sts.add( from );
	sts.add( p_to );
      } else {
	if ( /* started || */ ! blk.isRelativeDistance( prev ) ) {
	  unassigned.add( blk );
	  // started = true;
	}
      } 
    }
    if ( unassigned.size() > 0 ) assignStations( unassigned, sts );
  }

  // DistoX backshot-mode is handled separately
  @Override
  void assignStations( List<DBlock> list, Set<String> sts )
  { 
    if ( TDSetting.mDistoXBackshot ) { // if the distox is in backshot mode
      assignStationsBackshot( list, sts );
      return;
    }

    DBlock prev = null;
    String from = DistoXStationName.mInitialStation;
    String to   = DistoXStationName.mSecondStation;
    String oldFrom = "empty"; // FIXME
    // boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    float fore_length  = 0;
    float fore_bearing = 0;
    float fore_clino   = 0;

    String station = ( mCurrentStationName != null )? mCurrentStationName : from;
    // Log.v("DistoX", "assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // Log.v("DistoX", "Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );

    int nrLegShots = 0;
    // Log.v("DistoX", "FROM " + from + " TO " + to + " STATION " + station );

    for ( DBlock blk : list ) {
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          setBlockName( blk, station, "" );
        } else {
          if ( prev.isRelativeDistance( blk ) ) {
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
              if ( /* flip && */ is_backsight_shot ) { 
                                     // 2 backsight backward shot from--old_from
                prev_to = oldFrom;   // 1
                station = from;
                // flip = false;
	        // Log.v("DistoXX", "set " + prev.mId + " back leg and dup ");
	        mData.updateShotLegFlag( prev.mId, mSid, LegType.BACK, DBlock.FLAG_DUPLICATE, true ); // true = forward
              } else {               // 2 backsight forward shot from--to
                // prev_to = to;     // 3
                oldFrom = from;      // 2
                from    = to;        // 3
                station = to;
                to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from          
                // flip = true;
                fore_length  = prev.mLength;
                fore_bearing = prev.mBearing;
                fore_clino   = prev.mClino;
              }
              setBlockName( prev, prev_from, prev_to, is_backsight_shot );
              setLegExtend( prev );
              // Log.v("DistoX", "FROM " + from + " TO " + to + " STATION " + station + " P_FROM " + prev_from + " P_TO " + prev_to + " backshot " + is_backsight_shot );
            }
          } else { // distance from prev > "closeness" setting
            if ( nrLegShots > 0 ) {
              if ( mCurrentStationName == null ) {
                station = from;
              } // otherwise station = mCurrentStationName;
            // } else { // only when coming from a LEG
            //   flip = false;
            }
            nrLegShots = 0;
            setBlockName( blk, station, "" );
            prev = blk;
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mTo.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          if ( ! blk.mTo.equals( oldFrom ) ) { // this is a backshot
            // flip = true;
            oldFrom = blk.mFrom;
            from    = blk.mTo;
            to      = DistoXStationName.incrementName( from, sts );
            if ( mCurrentStationName == null ) {
              station = blk.mTo;
            } // otherwise station = mCurrentStationName
            fore_length  = blk.mLength;
            fore_bearing = blk.mBearing;
            fore_clino   = blk.mClino;
          // } else { // this is a foreshot
          //   flip = false;
          }
          nrLegShots = TDSetting.mMinNrLegShots;
          // Log.v("DistoX", "FROM " + from + " TO " + to + " STATION " + station + " OLD FROM " + oldFrom );
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          // if ( nrLegShots == 0 ) {
          //   flip = false; // after a splay shot is fore, but first splay is tentative
          // }
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }
  
  // backsight station policy with the DistoX in backshot-mode
  //    that is the distox reverts azymuth and clino
  //    since topodroid stores the data as they arrive from the distox, the shots have inverted azimuth and clino 
  @Override
  protected void assignStationsBackshot( List<DBlock> list, Set<String> sts )
  { 
    // Log.v("DistoX", "Backsight assign stations. Size " + list.size() );
    DBlock prev = null;
    String from = DistoXStationName.mInitialStation;
    String to   = DistoXStationName.mSecondStation;
    String oldFrom = "empty"; // FIXME
    // boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    float fore_length  = 0;
    float fore_bearing = 0;
    float fore_clino   = 0;

    String station = ( mCurrentStationName != null )? mCurrentStationName : from;
    // Log.v("DistoX", "assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // Log.v("DistoX", "Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );

    int nrLegShots = 0;

    for ( DBlock blk : list ) {
      if ( blk.mTo.length() == 0 ) // this implies blk.mFrom.length() == 0
      {
        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          setBlockName( blk, "", station );
        } else {
          if ( prev.isRelativeDistance( blk ) ) {
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
              if ( /* flip && */ is_backsight_shot ) {
                                     // 2 backsight backward shot from--old_from
                prev_to = oldFrom;   // 1
                station = from;
                // flip = false;
	        // Log.v("DistoXX", "set " + prev.mId + " back leg and dup ");
	        mData.updateShotLegFlag( prev.mId, mSid, LegType.BACK, DBlock.FLAG_DUPLICATE, true ); // true = forward
              } else {               // 2 backsight forward shot from--to
                // prev_to = to;     // 3
                oldFrom = from;      // 2
                from    = to;        // 3
                station = to;
                to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from          
                // flip = true;
                fore_length  = prev.mLength;
                fore_bearing = prev.mBearing;
                fore_clino   = prev.mClino;
              }
              setBlockName( prev, prev_to, prev_from, is_backsight_shot );
              setLegExtend( prev );
            }
          } else { // distance from prev > "closeness" setting
            if ( nrLegShots > 0 ) {
              if ( mCurrentStationName == null ) {
                station = from;
              } // otherwise station = mCurrentStationName;
            // } else { // only when coming from a LEG
            //   flip = false;
            }
            nrLegShots = 0;
            setBlockName( blk, "", station );
            prev = blk;
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mFrom.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          if ( ! blk.mFrom.equals( oldFrom ) ) { // this is a backshot
            // flip = true;
            oldFrom = blk.mTo;
            from    = blk.mFrom;
            to      = DistoXStationName.incrementName( from, sts );
            if ( mCurrentStationName == null ) {
              station = blk.mFrom;
            } // otherwise station = mCurrentStationName
            fore_length  = blk.mLength;
            fore_bearing = blk.mBearing;
            fore_clino   = blk.mClino;
          // } else { // this is a foreshot
          //   flip = false;
          }
          nrLegShots = TDSetting.mMinNrLegShots;
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          // if ( nrLegShots == 0 ) {
          //   flip = false; // after a splay shot is fore, but first splay is tentative
          // }
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }
  
}
