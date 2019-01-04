/* @file StationName.java
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


// import android.util.Log;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

class StationName
{
  // ----------------------------------------------------------------
  // current station(s)
  private static String mCurrentStationName = null;

  static boolean setCurrentStationName( String name ) 
  { 
    if ( name == null || name.equals(mCurrentStationName) ) {
      mCurrentStationName = null; // clear
      return false;
    } 
    mCurrentStationName = name;
    return true;
  }

  // unused
  // static void resetCurrentStationName( String name ) { mCurrentStationName = name; }

  static String getCurrentStationName() { return mCurrentStationName; }

  static boolean isCurrentStationName( String name ) { return name.equals(mCurrentStationName); }

  static private String getLastStationName( DataHelper data_helper, long sid )
  {
    // FIXME not efficient: use a better select with reverse order and test on FROM
    // DBlock last = null;
    // List<DBlock> list = data_helper.selectAllShots( sid, TDStatus.NORMAL );
    // if ( TDSetting.mDistoXBackshot ) {
    //   for ( DBlock blk : list ) {
    //     if ( blk.mTo != null && blk.mTo.length() > 0 ) { last = blk; }
    //   }
    //   if ( last == null ) return TDString.ZERO;
    //   if ( last.mFrom == null || last.mFrom.length() == 0 ) return last.mTo;
    //   if ( StationPolicy.mSurveyStations == 1 ) return last.mFrom;  // forward-shot
    //   return last.mTo;
    // } else {
    //   for ( DBlock blk : list ) {
    //     if ( blk.mFrom != null && blk.mFrom.length() > 0 ) { last = blk; }
    //   }
    //   if ( last == null ) return TDString.ZERO;
    //   if ( last.mTo == null || last.mTo.length() == 0 ) return last.mFrom;
    //   if ( StationPolicy.mSurveyStations == 1 ) return last.mTo;  // forward-shot
    //   return last.mFrom;
    // }
    DBlock last = data_helper.selectLastNonBlankShot( sid, TDStatus.NORMAL, TDSetting.mDistoXBackshot );
    if ( last == null ) return TDSetting.mInitStation;
    if ( TDSetting.mDistoXBackshot ) {
      if ( last.mFrom == null || last.mFrom.length() == 0 ) return last.mTo;
      if ( StationPolicy.mSurveyStations == 1 ) return last.mFrom;  // forward-shot
      return last.mTo;
    } else {
      if ( last.mTo == null || last.mTo.length() == 0 ) return last.mFrom;
      if ( StationPolicy.mSurveyStations == 1 ) return last.mTo;  // forward-shot
      return last.mFrom;
    }
    // return TDString.ZERO;
  }

  static void resetCurrentOrLastStation( DataHelper data_helper, long sid )
  {
    if ( mCurrentStationName == null ) mCurrentStationName = getLastStationName( data_helper, sid );
  }

  static String getCurrentOrLastStation( DataHelper data_helper, long sid )
  {
    if ( mCurrentStationName != null ) return mCurrentStationName;
    return getLastStationName( data_helper, sid );
  }
  
  static void clearCurrentStation()
  {
    mCurrentStationName = null;
  }

  static String getFirstStation( DataHelper data_helper, long sid )
  {
    return data_helper.getFirstStation( sid );
  }
  
  // ------------------------------------------------------------------------------------------------
  // setting the leg extend automatically, sets also stretch to 0

  private static void setLegExtend( DataHelper data_helper, long sid, DBlock blk )
  {
    // FIXME_EXTEND what has "splay extend" to do with "leg extend" ???
    // if ( ! TDSetting.mSplayExtend ) 
    {
      long extend = TDAzimuth.computeLegExtend( blk.mBearing );
      blk.setExtend( (int)extend, DBlock.STRETCH_NONE ); 
      data_helper.updateShotExtend( blk.mId, sid, extend, DBlock.STRETCH_NONE, true );
    }
  }

  // used to set block extend "fixed"
  private static void setLegFixedExtend( DataHelper data_helper, long sid, DBlock blk, long extend )
  {
    blk.setExtend( (int)extend, DBlock.STRETCH_NONE );
    data_helper.updateShotExtend( blk.mId, sid, extend, DBlock.STRETCH_NONE, true );
  }

  // ------------------------------------------------------------------------------------------------
  // station assignments

  private static void setBlockName( DataHelper data_helper, long sid, DBlock blk, String from, String to, boolean is_backleg ) 
  {
    blk.setBlockName( from, to, is_backleg );
    data_helper.updateShotName( blk.mId, sid, from, to, true );
  }

  private static void setBlockName( DataHelper data_helper, long sid, DBlock blk, String from, String to )
  {
    blk.setBlockName( from, to );
    data_helper.updateShotName( blk.mId, sid, from, to, true );
  }


  // @param list list of dblock to assign
  static void assignStationsAfter_Tripod( DataHelper data_helper, long sid, DBlock blk0, List<DBlock> list, Set<String> sts )
  { 
    ArrayList<DBlock> unassigned = new ArrayList<DBlock>();
    // boolean started = false;

    // Log.v("DistoX-SN", "assign stations after - tripod");
    boolean bs = TDSetting.mDistoXBackshot;
    // Log.v("DistoX", "assign stations after.  size " + list.size() );
    boolean increment = true;
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = blk0;
    String from = null;
    String back = null;
    if ( bs ) {
      from = blk0.mTo;
      back = blk0.mFrom;
      if ( DistoXStationName.isLessOrEqual( blk0.mTo, blk0.mFrom ) ) { // forward
        flip = true;
        // move next
        // back = blk0.mFrom;
        from = DistoXStationName.incrementName( blk0.mFrom, sts );
      } else { // backward
        // increment = false;
        flip = false; // already assigned
      }
    } else {
      from = blk0.mFrom;
      back = blk0.mTo;
      if ( DistoXStationName.isLessOrEqual( blk0.mFrom, blk0.mTo ) ) { // forward
        flip = true;
        // move next
        // back = blk0.mTo;
        from = DistoXStationName.incrementName( blk0.mTo, sts );
      } else { // backward
        // increment = false;
        flip = false; // already assigned
      }
    }

    String next = DistoXStationName.incrementName( from, sts );
    String station = from;
    // Log.v("DistoX", "*    " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
    String main_from = null;
    String main_to   = null;

    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId ) continue;
      if ( blk.isSplay() ) {
        if ( flip ) { 
          flip = false;
        }
	if ( bs ) { // blk.mTo = station;
          setBlockName( data_helper, sid, blk, "", station );
	} else { // blk.mFrom = station;
          setBlockName( data_helper, sid, blk, station, "" );
	}
	sts.add( station );
        // Log.v("DistoX", "S:"+ station + "   " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
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
	if ( bs ) {
          setBlockName( data_helper, sid, blk, p_to, p_from );
	} else {
          setBlockName( data_helper, sid, blk, p_from, p_to );
	}
	sts.add( p_from );
	sts.add( p_to );
        // Log.v("DistoX", "L:"+from+"-"+ p_to + " " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
      } else if ( blk.isBackLeg() ) {
	if ( main_from != null /* && main_to != null */ ) {
          prev = blk;
	  if ( bs ) {
            setBlockName( data_helper, sid, blk, main_from, main_to );
	  } else {
            setBlockName( data_helper, sid, blk, main_to, main_from );
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
    if ( unassigned.size() > 0 ) assignStations_Tripod( data_helper, sid, unassigned, sts );
  }

  static void assignStations_Tripod( DataHelper data_helper, long sid, List<DBlock> list, Set<String> sts )
  { 
    if ( TDSetting.mDistoXBackshot ) {
      assignStations_TripodBackshot( data_helper, sid, list, sts );
      return;
    }
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
      // Log.v("DistoX", blk.mId + " <" + blk.mFrom + ">-<" + blk.mTo + "> F " + from + " T " + back + " N " + next );
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );
        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          setBlockName( data_helper, sid, blk, station, "" );
          // Log.v( "DistoX", blk.mId + " FROM " + blk.mFrom + " PREV null" );
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
              mCurrentStationName = null;
              // Log.v("DistoX", "P " + prev.mId + " " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
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
              // Log.v("DistoX", "P: (" + prev_from + "-" + prev_to + ") " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
              setBlockName( data_helper, sid, prev, prev_from, prev_to );
	      if ( is_fixed_extend ) {
                setLegFixedExtend( data_helper, sid, prev, (flip? fore_extend : back_extend) ); // flip is set when FROM ==> NEXT
	      } else {
                setLegExtend( data_helper, sid, prev );
	      }
            }
          } else { // distance from prev > "closeness" setting
            if ( nrLegShots == 0 ) {
              if ( flip ) {
                flip = false;
                if ( prev != null && prev.mTo.length() == 0 ) {
                  if ( ! prev.mFrom.equals( station ) ) {
                    setBlockName( data_helper, sid, prev, station, "" );
                  }
                }
              }
            // } else { // only when coming from a LEG
            //   // if ( mCurrentStationName == null ) {
            //   //   station = from;
            //   // }
            }
            nrLegShots = 0;
            setBlockName( data_helper, sid, blk, station, "" );
            // Log.v( "DistoX", "non-close: b " + blk.mId + " <" + blk.mFrom + "> " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
            prev = blk;
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mTo.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          // Log.v("DistoX", blk.mId + " [" + blk.mFrom + "-" + blk.mTo + "] " + from + "-" + back + "-" + next + " " + station );
          if ( DistoXStationName.isLessOrEqual( blk.mFrom, blk.mTo ) ) { // forward shot
            flip = true;
            back = blk.mTo;
            from = DistoXStationName.incrementName( back, sts );
            next = DistoXStationName.incrementName( from, sts );
          } else { // backward shot
            flip = false;
            from = blk.mFrom;
            back = blk.mTo;
            next = DistoXStationName.incrementName( from, sts );
          }
          if ( mCurrentStationName == null ) station = from;
          // Log.v("DistoX", "   " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip? "y":"n") );
          nrLegShots = TDSetting.mMinNrLegShots;
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          if ( nrLegShots == 0 ) {
            flip = false;
          }
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }


  // called in assignStationsAfter_Backsight
  //           assignStations_BacksightBachshot
  //           assignStations_Backsight
  // note backsight-shot is a shot taken backsight (ie backward)
  //      backshot is a distox mode, in which direction data are stored reversed
  private static boolean checkBacksightShot( DBlock blk, float length, float bearing, float clino )
  {
    float d_thr = TDSetting.mCloseDistance * (blk.mLength+length);
    if ( Math.abs( length - blk.mLength ) > d_thr ) {
      // Log.v("DistoXX", "backshot check fails on distance " + length + " " + blk.mLength + " thr " + d_thr );
      return false;
    }
    float a_thr = TDSetting.mCloseDistance * 112; // rad2deg * 2 
    if ( Math.abs( clino + blk.mClino ) > a_thr ) {
      // Log.v("DistoXX", "backshot check fails on clino " + clino + " " + blk.mClino + " thr " + a_thr );
      return false;
    }
    if ( ! StationPolicy.doMagAnomaly() ) {
      if ( Math.abs( ( bearing < blk.mBearing )? blk.mBearing - bearing - 180 : bearing - blk.mBearing - 180 ) > a_thr ) {
        // Log.v("DistoXX", "backshot check fails on bearing " + bearing + " " + blk.mBearing + " thr " + a_thr );
        return false;
      }
    }
    return true;
  }

  // @param list list of dblock to assign
  // called by TopoDroidApp
  static void assignStationsAfter_Backsight( DataHelper data_helper, long sid, DBlock blk0, List<DBlock> list, Set<String> sts )
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
          setBlockName( data_helper, sid, blk, "", station );
	} else { // blk.mFrom = station;
          setBlockName( data_helper, sid, blk, station, "" );
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
	  data_helper.updateShotLegFlag( blk.mId, sid, LegType.BACK, DBlock.FLAG_DUPLICATE, true ); // true = forward
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
          setBlockName( data_helper, sid, blk, p_to, from, is_backsight_shot );
	} else {
          setBlockName( data_helper, sid, blk, from, p_to, is_backsight_shot );
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
    if ( unassigned.size() > 0 ) assignStations_Backsight( data_helper, sid, unassigned, sts );
  }

  // DistoX backshot-mode is handled separately
  static void assignStations_Backsight( DataHelper data_helper, long sid, List<DBlock> list, Set<String> sts )
  { 
    if ( TDSetting.mDistoXBackshot ) { // if the distox is in backshot mode
      assignStations_BacksightBachshot( data_helper, sid, list, sts );
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
          setBlockName( data_helper, sid, blk, station, "" );
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
	        data_helper.updateShotLegFlag( prev.mId, sid, LegType.BACK, DBlock.FLAG_DUPLICATE, true ); // true = forward
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
              setBlockName( data_helper, sid, prev, prev_from, prev_to, is_backsight_shot );
              setLegExtend( data_helper, sid, prev );
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
            setBlockName( data_helper, sid, blk, station, "" );
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
  
  // @param data_helper  database
  // @param sid          survey id
  // @param blk0         reference dblock
  // @param list         list of dblock to assign
  // @param sts          station names already in use
  static void assignStationsAfter_Default( DataHelper data_helper, long sid, DBlock blk0, List<DBlock> list, Set<String> sts )
  {
    ArrayList<DBlock> unassigned = new ArrayList<DBlock>();
    // boolean started = false;

    boolean bs = TDSetting.mDistoXBackshot;

    // Log.v("DistoX", "assign stations after.  size " + list.size() );
    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splays = StationPolicy.mShotAfterSplays;
    // Log.v("DistoX-SN", "default assign stations after. blk0 " + blk0.mId + " bs " + bs + " survey_stations " + survey_stations + " shot_after_splay " + shot_after_splays );

    String main_from = null;
    String main_to   = null;

    // boolean increment = true;
    // boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = blk0;
    String from = bs ? blk0.mTo   : blk0.mFrom;
    String to   = bs ? blk0.mFrom : blk0.mTo;
    String next;
    String station;
    if ( forward_shots ) {
      next = DistoXStationName.incrementName( to, sts );
      station = shot_after_splays ? to : from;
    } else {
      next = DistoXStationName.incrementName( from, sts );
      station = shot_after_splays ? next : from;
    }
    // Log.v("DistoX-SN", "F " + from + " T " + to + " N " + next + " S " + station );

    // int nrLegShots = 0;
    for ( DBlock blk : list ) {
      if ( blk.mId == blk0.mId ) continue;
      if ( blk.isSplay() ) {
	if ( bs ) { // blk.mTo = station;
          setBlockName( data_helper, sid, blk, "", station );
	} else { // blk.mFrom = station;
          setBlockName( data_helper, sid, blk, station, "" );
	}
	sts.add( station );
      } else if ( blk.isMainLeg() ) {
	prev = blk;
        if ( forward_shots ) {
          from = to;
          to   = next;
          next = DistoXStationName.incrementName( to, sts );
          station = shot_after_splays ? to : from;
        } else {
          to   = from;
          from = next;
          next = DistoXStationName.incrementName( from, sts );
          station = shot_after_splays ? next : from;
        }
	main_from = from;
	main_to   = to;
	if ( bs ) {
          // blk.mTo   = from;
          // blk.mFrom = to;
          setBlockName( data_helper, sid, blk, to, from );
	} else {
          // blk.mFrom = from;
          // blk.mTo   = to;
          setBlockName( data_helper, sid, blk, from, to );
	}
	sts.add( from );
	sts.add( to );
      } else if ( blk.isBackLeg() ) {
	if ( main_from != null /* && main_to != null */ ) {
	  prev = blk;
	  if ( bs ) {
            setBlockName( data_helper, sid, blk, main_from, main_to );
	  } else {
            setBlockName( data_helper, sid, blk, main_to, main_from );
	  }
	}
	main_from = main_to = null;
      } else {
        // Log.v("DistoX-SN", "blk is skipped" + blk.mId );
	if ( /* started || */ ! blk.isRelativeDistance( prev ) ) {
	  unassigned.add( blk );
	  // started = true;
	}
      }
    }
   
    if ( unassigned.size() > 0 ) assignStations_Default( data_helper, sid, unassigned, sts );
  }

  // DistoX backshot-mode is handled separatedly
  // @param data_helper  database
  // @param sid          survey id
  // @param list         list of dblock to assign
  // @param sts          station names already in use
  static void assignStations_Default( DataHelper data_helper, long sid, List<DBlock> list, Set<String> sts )
  { 
    if ( TDSetting.mDistoXBackshot ) {
      assignStations_DefaultBackshot( data_helper, sid, list, sts );
      return;
    }
    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splay = StationPolicy.mShotAfterSplays;

    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = null;
    String from = ( forward_shots )? DistoXStationName.mInitialStation  // next FROM station
                                   : DistoXStationName.mSecondStation;
    String to   = ( forward_shots )? DistoXStationName.mSecondStation   // nect TO station
                                   : DistoXStationName.mInitialStation;
    String station = ( mCurrentStationName != null )? mCurrentStationName
                   : (shot_after_splay ? from : "");  // splays station
    // Log.v("DistoX", "assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // Log.v("DistoX", "Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );
    // Log.v("DistoXX", "assign stations default. size " + list.size() + " fwd " + forward_shots + " shot after splays " + shot_after_splay + " station " + station );

    int nrLegShots = 0;

    for ( DBlock blk : list ) {
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );

        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          setBlockName( data_helper, sid, blk, station, "" );
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
              mCurrentStationName = null;
              // Log.v( "DistoX", "PREV " + prev.mId + " nrLegShots " + nrLegShots + " set PREV " + from + "-" + to );
              setBlockName( data_helper, sid, prev, from, to );
              setLegExtend( data_helper, sid, prev );
              if ( forward_shots ) {
                station = shot_after_splay  ? to : from;     // splay-station = this-shot-to if splays before shot
                                                             //                 this-shot-from if splays after shot
                from = to;                                   // next-shot-from = this-shot-to
                to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                // Log.v("DistoX", "station [1] " + station + " FROM " + from + " TO " + to );
              } else { // backward_shots
                to   = from;                                     // next-shot-to   = this-shot-from
                from = DistoXStationName.incrementName( from, sts ); // next-shot-from = increment this-shot-from
                station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                                 //                = this-shot-from if splay after shot
                // Log.v("DistoX", "station [2] " + station + " FROM " + from + " TO " + to );
              }
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            setBlockName( data_helper, sid, blk, station, "" );
            prev = blk;
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mTo.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          if ( forward_shots ) {  // : ..., 0-1, 1-2 ==> from=(2) to=Next(2)=3 ie 2-3
            from = blk.mTo;
            to   = DistoXStationName.incrementName( from, sts );
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? blk.mTo    // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
                                         : blk.mFrom; // 1-2, 1, 1,   [ 2-3, 2, 2, ... ] ...
            } // otherwise station = mCurrentStationName
          } else { // backward shots: ..., 1-0, 2-1 ==> from=Next(2)=3 to=2 ie 3-2
            to      = blk.mFrom;
            from    = DistoXStationName.incrementName( to, sts ); // FIXME it was from
	    // station must be set even there is a "currentStation"
            station = shot_after_splay ? from       // 2,   2, 2, 2-1, [ 3, 3, ..., 3-2 ]  ...
                                       : blk.mFrom; // 2-1, 2, 2, 2,   [ 3-2, 3, 3, ... 3 ] ...
          }
          // Log.v("DistoXX", "IDX " + blk.mId + ": " + blk.mFrom + " - " + blk.mTo + " Next " + from + " - " + to + " station " + station );
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
  static void assignStationsAfter_TRobot( DataHelper data_helper, long sid, DBlock blk0, List<DBlock> list, Set<String> sts )
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
          setBlockName( data_helper, sid, blk, "", station );
	} else {
          setBlockName( data_helper, sid, blk, station, "" ); 
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
          setBlockName( data_helper, sid, blk, to, from );
        } else {
          setBlockName( data_helper, sid, blk, from, to );
	}
	main_from = from;
	main_to   = to;
	sts.add( from );
	sts.add( to );
      } else if ( blk.isBackLeg() ) {
	if ( main_from != null /* && main_to != null */ ) {
          prev = blk;
	  if ( bs ) {
            setBlockName( data_helper, sid, blk, main_from, main_to );
	  } else {
            setBlockName( data_helper, sid, blk, main_to, main_from );
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
    if ( unassigned.size() > 0 ) assignStations_TRobot( data_helper, sid, unassigned, sts );
  }

  static void assignStations_TRobot( DataHelper data_helper, long sid, List<DBlock> list, Set<String> sts )
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
          data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true );  // SPLAY
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
              mCurrentStationName = null;
              // Log.v( "DistoX", "PREV " + prev.mId + " nrLegShots " + nrLegShots + " set PREV " + from + "-" + to );
              prev.setBlockName( from, to );
              data_helper.updateShotName( prev.mId, sid, from, to, true ); // LEG
              setLegExtend( data_helper, sid, prev );
              station = to;
              from = to;                                   // next-shot-from = this-shot-to
              to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                // Log.v("DistoX", "station [1] " + station + " FROM " + from + " TO " + to );
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            blk.setBlockName( ((station!=null)? station : from), "" );
            data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true ); // SPLAY
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

  // ------------------------------------------------------------------------------------------------------
  // DistoX backshot-mode station assignments

  private static void assignStations_TripodBackshot( DataHelper data_helper, long sid, List<DBlock> list, Set<String> sts )
  { 
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
      // Log.v("DistoX", blk.mId + " <" + blk.mTo + ">-<" + blk.mFrom + "> F " + from + " T " + back + " N " + next );
      if ( blk.mTo.length() == 0 ) // this implies blk.mFrom.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );
        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          setBlockName( data_helper, sid, blk, "", station );
          // Log.v( "DistoX", blk.mId + " FROM " + blk.mFrom + " PREV null" );
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
              mCurrentStationName = null;
              // Log.v("DistoX", "P " + prev.mId + " " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
              String prev_from = back; // measuring FROM ==> BACK but DistoX reports BACK --> FROM
              String prev_to   = from;
              if ( flip ) { 
                flip = false;
              } else {          // measuring FROM ==> NEXT but DistoX reports NEXT --> FROM
                flip = true;
                prev_from = next;
                // move forward 
                back   = next;
                from = DistoXStationName.incrementName( next, sts );
                next = DistoXStationName.incrementName( from, sts ); 
              }
              station = from;
              // Log.v("DistoX", "P: (" + prev_to + "-" + prev_from + ") " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
              setBlockName( data_helper, sid, prev, prev_from, prev_to );
	      if ( is_fixed_extend ) {
                setLegFixedExtend( data_helper, sid, prev, (flip? back_extend : fore_extend) ); // flip is set after NEXT-->FROM
	      } else {
                setLegExtend( data_helper, sid, prev );
	      }
            }
          } else { // distance from prev > "closeness" setting
            if ( nrLegShots == 0 ) {
              if ( flip ) {
                flip = false;
                if ( prev != null && prev.mFrom.length() == 0 ) {
                  if ( ! prev.mTo.equals( station ) ) {
                    setBlockName( data_helper, sid, prev, "", station ); // splays are all backwards
                  }
                }
              }
            // } else { // only when coming from a LEG
            //   // if ( mCurrentStationName == null ) {
            //   //   station = from;
            //   // }
            }
            nrLegShots = 0;
            setBlockName( data_helper, sid, blk, "", station );
            // Log.v( "DistoX", "non-close: b " + blk.mId + " <" + blk.mTo + "> " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
            prev = blk;
          }
        }
      }
      else // blk.mTo.length > 0
      {
        if ( blk.mFrom.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          // Log.v("DistoX", blk.mId + " [" + blk.mTo + "-" + blk.mFrom + "] " + from + "-" + back + "-" + next + " " + station );
          if ( DistoXStationName.isLessOrEqual( blk.mTo, blk.mFrom ) ) { // forward shot
            flip = true;
            back = blk.mFrom;
            from = DistoXStationName.incrementName( back, sts );
            next = DistoXStationName.incrementName( from, sts );
          } else { // backward shot
            flip = false;
            from = blk.mTo;
            back = blk.mFrom;
            next = DistoXStationName.incrementName( from, sts );
          }
          if ( mCurrentStationName == null ) station = from;
          // Log.v("DistoX", "   " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip? "y":"n") );
          nrLegShots = TDSetting.mMinNrLegShots;
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          if ( nrLegShots == 0 ) {
            flip = false;
          }
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }

  // backsight station policy with the DistoX in backshot-mode
  //    that is the distox reverts azymuth and clino
  //    since topodroid stores the data as they arrive from the distox, the shots have inverted azimuth and clino 
  private static void assignStations_BacksightBachshot( DataHelper data_helper, long sid, List<DBlock> list, Set<String> sts )
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
          setBlockName( data_helper, sid, blk, "", station );
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
	        data_helper.updateShotLegFlag( prev.mId, sid, LegType.BACK, DBlock.FLAG_DUPLICATE, true ); // true = forward
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
              setBlockName( data_helper, sid, prev, prev_to, prev_from, is_backsight_shot );
              setLegExtend( data_helper, sid, prev );
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
            setBlockName( data_helper, sid, blk, "", station );
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
  
  // DistoX backshot-mode default station assignment(s)
  private static void assignStations_DefaultBackshot( DataHelper data_helper, long sid, List<DBlock> list, Set<String> sts )
  { 
    // Log.v("DistoX", "assign stations default. size " + list.size() );
    int survey_stations = StationPolicy.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splay = StationPolicy.mShotAfterSplays;

    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

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
          setBlockName( data_helper, sid, blk, "", station );
          // Log.v( "DistoX", blk.mId + " FROM " + blk.mTo + " PREV null" );
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
              mCurrentStationName = null;
              // Log.v( "DistoX", "PREV " + prev.mId + " nrLegShots " + nrLegShots + " set PREV " + from + "-" + to );
              setBlockName( data_helper, sid, prev, to, from );
              setLegExtend( data_helper, sid, prev );
              if ( forward_shots ) {
                station = shot_after_splay  ? to : from;     // splay-station = this-shot-to if splays before shot
                                                             //                 this-shot-from if splays after shot
                from = to;                                   // next-shot-from = this-shot-to
                to   = DistoXStationName.incrementName( to, sts );  // next-shot-to   = increment next-shot-from
                // Log.v("DistoX", "station [1] " + station + " FROM " + from + " TO " + to );
              } else { // backward_shots
                to   = from;                                     // next-shot-to   = this-shot-from
                from = DistoXStationName.incrementName( from, sts ); // next-shot-from = increment this-shot-from
                station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                                 //                = this-shot-from if splay after shot
                // Log.v("DistoX", "station [2] " + station + " FROM " + from + " TO " + to );
              }
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            setBlockName( data_helper, sid, blk, "", station );
            prev = blk;
          }
        }
      }
      else // blk.mTo.length > 0
      {
        if ( blk.mFrom.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          if ( forward_shots ) {  // : ..., 0-1, 1-2 ==> from=(2) to=Next(2)=3 ie 2-3
            from = blk.mFrom;
            to   = DistoXStationName.incrementName( from, sts );
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? blk.mFrom  // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
                                         : blk.mTo;   // 1-2, 1, 1,   [ 2-3, 2, 2, ... ] ...
            } // otherwise station = mCurrentStationName
          } else { // backward shots: ..., 1-0, 2-1 ==> from=Next(2)=3 to=2 ie 3-2
            to      = blk.mTo;
            from    = DistoXStationName.incrementName( to, sts ); // FIXME it was from
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? from       // 2,   2, 2, 2-1, [ 3, 3, ..., 3-2 ]  ...
                                         : blk.mTo;   // 2-1, 2, 2, 2,   [ 3-2, 3, 3, ... 3 ] ...
            } // otherwise station = mCurrentStationName
          }
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
