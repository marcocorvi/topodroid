/** @file StationName.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid station naming
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * N.B. backsight and tripod need the whole list to decide how to assign names
 */
package com.topodroid.DistoX;


import android.util.Log;

import java.util.List;
import java.util.ArrayList;

class StationName
{
  // ----------------------------------------------------------------
  // current station(s)
  private String mCurrentStationName = null;

  boolean setCurrentStationName( String name ) 
  { 
    if ( name == null || name.equals(mCurrentStationName) ) {
      mCurrentStationName = null; // clear
      return false;
    } 
    mCurrentStationName = name;
    return true;
  }

  String getCurrentStationName() { return mCurrentStationName; }

  boolean isCurrentStationName( String name ) { return name.equals(mCurrentStationName); }

  // FIXME 
  // not efficient: use a better select with reverse order and test on FROM
  private String getLastStationName( DataHelper data_helper, long sid )
  {
    DBlock last = null;
    List<DBlock> list = data_helper.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    for ( DBlock blk : list ) {
      if ( blk.mFrom != null && blk.mFrom.length() > 0 ) { last = blk; }
    }
    if ( last == null ) return "0";
    if ( last.mTo == null || last.mTo.length() == 0 ) return last.mFrom;
    if ( TDSetting.mSurveyStations == 1 ) return last.mTo;  // forward-shot
    return last.mFrom;
  }


  String getCurrentOrLastStation( DataHelper data_helper, long sid )
  {
    if ( mCurrentStationName != null ) return mCurrentStationName;
    return getLastStationName( data_helper, sid );
  }
  
  void clearCurrentStations()
  {
    mCurrentStationName = null;
  }


  private void setLegExtend( DataHelper data_helper, long sid, DBlock prev )
  {
    // FIXME what has "splay extend" to do with "leg extend" ???
    // if ( ! TDSetting.mSplayExtend ) 
    {
      long extend = TDAzimuth.computeLegExtend( prev.mBearing );
      data_helper.updateShotExtend( prev.mId, sid, extend, true );
    }
  }

  void assignStationsAfter_Tripod( DataHelper data_helper, long sid, DBlock blk0, List<DBlock> list, ArrayList<String> sts )
  { 
    // Log.v("DistoX", "assign stations after.  size " + list.size() );
    boolean increment = true;
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = null;
    String from = blk0.mFrom; 
    String back = blk0.mTo;
    if ( DistoXStationName.isLessOrEqual( blk0.mFrom, blk0.mTo ) ) { // forward
      flip = true;
      // move next
      // back = blk0.mTo;
      from = DistoXStationName.increment( blk0.mTo );
    } else { // backward
      // increment = false;
      flip = false;
    }
    String next = DistoXStationName.increment( from, sts );
    String station = from;
    // Log.v("DistoX", "*    " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );

    for ( DBlock blk : list ) {
      if ( blk.mType == DBlock.BLOCK_SPLAY ) {
        if ( flip ) { 
          flip = false;
        }
        // blk.mFrom = station;
        blk.setName( station, "" );
        data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true );  // SPLAY
        // Log.v("DistoX", "S:"+ station + "   " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
      } else if ( blk.mType == DBlock.BLOCK_MAIN_LEG ) {
        if ( blk.mId != blk0.mId ) {
          String p_from = from;
          String p_to   = next;
          if ( flip ) { // backward
            flip = false;
            p_to = back; 
          } else {  // forward
            flip = true;
            if ( increment ) {
              // move for
              back = next;
              from = DistoXStationName.increment( next ); 
              next = DistoXStationName.increment( from, sts );
              station = from;
            } else {
              increment = true;
            }
          }
          blk.setName( p_from, p_to );
          data_helper.updateShotName( blk.mId, sid, p_from, p_to, true ); // LEG
          // Log.v("DistoX", "L:"+from+"-"+ p_to + " " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
        }
      }
    }
  }

  void assignStations_Tripod( DataHelper data_helper, long sid, List<DBlock> list )
  { 
    DBlock prev = null;
    String from = DistoXStationName.mSecondStation;     // 1
    String back = DistoXStationName.mInitialStation;    // 0
    String next = DistoXStationName.increment( from );  // 2
    boolean flip = true; // whether to swap leg-stations (backsight backward shot)

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
          blk.setName( station, "" );
          data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true );  // SPLAY
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
              if ( flip ) { 
                flip = false;
              } else {         
                flip = true;
                prev_to = next;
                // move forward 
                back   = next;
                from = DistoXStationName.increment( next, list );
                next = DistoXStationName.increment( from ); 
              }
              station = from;
              // Log.v("DistoX", "P: (" + prev_from + "-" + prev_to + ") " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
              prev.setName( prev_from, prev_to );
              data_helper.updateShotName( prev.mId, sid, prev_from, prev_to, true ); // LEG
              setLegExtend( data_helper, sid, prev );
            }
          } else { // distance from prev > "closeness" setting
            if ( nrLegShots == 0 ) {
              if ( flip ) {
                flip = false;
                if ( prev != null && prev.mTo.length() == 0 ) {
                  if ( ! prev.mFrom.equals( station ) ) {
                    prev.setName( station, "" );
                    data_helper.updateShotName( prev.mId, sid, station, "", true ); // SPLAY
                  }
                }
              }
            } else { // only when coming from a LEG
              // if ( mCurrentStationName == null ) {
              //   station = from;
              // }
            }
            nrLegShots = 0;
            blk.setName( station, "" );
            data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true ); // SPLAY
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
            from = DistoXStationName.increment( back, list );
            next = DistoXStationName.increment( from );
          } else { // backward shot
            flip = false;
            from = blk.mFrom;
            back = blk.mTo;
            next = DistoXStationName.increment( from, list );
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


  private boolean checkBackshot( DBlock blk, float length, float bearing, float clino )
  {
    float d_thr = TDSetting.mCloseDistance * (blk.mLength+length);
    if ( Math.abs( length - blk.mLength ) > d_thr ) {
      // Log.v("DistoX", "backshot check fails on distance " + length + " " + blk.mLength + " thr " + d_thr );
      return false;
    }
    float a_thr = TDSetting.mCloseDistance * 112; // rad2deg * 2 
    if ( Math.abs( clino + blk.mClino ) > a_thr ) {
      // Log.v("DistoX", "backshot check fails on clino " + clino + " " + blk.mClino + " thr " + a_thr );
      return false;
    }
    if ( ! TDSetting.mMagAnomaly ) {
      if ( Math.abs( ( bearing < blk.mBearing )? blk.mBearing - bearing - 180 : bearing - blk.mBearing - 180 ) > a_thr ) {
        // Log.v("DistoX", "backshot check fails on bearing " + bearing + " " + blk.mBearing + " thr " + a_thr );
        return false;
      }
    }
    return true;
  }

  void assignStationsAfter_Backsight( DataHelper data_helper, long sid, DBlock blk0, List<DBlock> list, ArrayList<String> sts )
  { 
    // Log.v("DistoX", "Backsight assign stations after " + blk0.mFrom + "-" + blk0.mTo + " Size " + list.size() + " sid " + sid );
    boolean increment = true;
    // boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = null;
    String from = blk0.mFrom;
    String to   = blk0.mTo;
    String next;
    String station;
    float fore_length  = 0;
    float fore_bearing = 0;
    float fore_clino   = 0;
    if ( DistoXStationName.isLessOrEqual( blk0.mFrom, blk0.mTo ) ) { // forward
      // flip    = true;
      station = to;
      next = DistoXStationName.increment( station, sts );
      fore_length  = blk0.mLength;
      fore_bearing = blk0.mBearing;
      fore_clino   = blk0.mClino;
    } else { // backward
      increment = false;
      // flip    = false;
      station = from;
      to   = DistoXStationName.increment( from );
      next = DistoXStationName.increment( to, sts );
    }

    String oldFrom = blk0.mFrom;
    for ( DBlock blk : list ) {
      if ( blk.mType == DBlock.BLOCK_SPLAY ) {
        // if ( flip ) { 
        //   flip = false;
        // }
        // blk.mFrom = station;
        blk.setName( station, "" );
        data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true );  // SPLAY
      } else if ( blk.mType == DBlock.BLOCK_MAIN_LEG ) {
        if ( blk.mId != blk0.mId ) {
          String p_to;
          if ( /* flip && */ checkBackshot( blk, fore_length, fore_bearing, fore_clino ) ) { // backward
            // flip = false;
            p_to = oldFrom; 
            from = to;
            station = from;
          } else {  // forward
            // flip = true;
            if ( increment ) {
              from = to;
              to   = next;
              next = DistoXStationName.increment( to, sts );
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
          blk.setName( from, p_to );
          data_helper.updateShotName( blk.mId, sid, from, p_to, true ); // LEG
        }
      }
    }
  }

  void assignStations_Backsight( DataHelper data_helper, long sid, List<DBlock> list )
  { 
    // mSecondLastShotId = lastShotId(); // FIXME this probably not needed
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
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          blk.setName( station, "" );
          data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true );  // SPLAY
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
              if ( /* flip && */ checkBackshot( prev, fore_length, fore_bearing, fore_clino) ) { 
                                     // 2 backsight backward shot from--old_from
                prev_to = oldFrom;   // 1
                station = from;
                // flip = false;
              } else {               // 2 backsight forward shot from--to
                // prev_to = to;     // 3
                oldFrom = from;      // 2
                from    = to;        // 3
                station = to;
                to   = DistoXStationName.increment( to,list );  // next-shot-to   = increment next-shot-from          
                // flip = true;
                fore_length  = prev.mLength;
                fore_bearing = prev.mBearing;
                fore_clino   = prev.mClino;
              }
              prev.setName( prev_from, prev_to );
              data_helper.updateShotName( prev.mId, sid, prev_from, prev_to, true ); // LEG
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
            blk.setName( station, "" );
            data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true ); // SPLAY
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
            to      = DistoXStationName.increment( from, list );
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
  
  void assignStationsAfter_Default( DataHelper data_helper, long sid, DBlock blk0, List<DBlock> list, ArrayList<String> sts )
  {
    // Log.v("DistoX", "assign stations after.  size " + list.size() );
    int survey_stations = TDSetting.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splays = TDSetting.mShotAfterSplays;

    boolean increment = true;
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = null;
    String from = blk0.mFrom;
    String to   = blk0.mTo;
    String next;
    String station;
    if ( forward_shots ) {
      next = DistoXStationName.increment( to, sts );
      station = shot_after_splays ? to : from;
    } else {
      next = DistoXStationName.increment( from, sts );
      station = shot_after_splays ? next : from;
    }

    // int nrLegShots = 0;
    for ( DBlock blk : list ) {
      if ( blk.mType == DBlock.BLOCK_SPLAY ) {
        // blk.mFrom = station;
        blk.setName( station, "" );
        data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true );  // SPLAY
      } else if ( blk.mType == DBlock.BLOCK_MAIN_LEG ) {
        if ( blk.mId != blk0.mId ) {
          if ( forward_shots ) {
            from = to;
            to   = next;
            next = DistoXStationName.increment( to, sts );
            station = shot_after_splays ? to : from;
          } else {
            to   = from;
            from = next;
            next = DistoXStationName.increment( from, sts );
            station = shot_after_splays ? next : from;
          }
          // blk.mFrom = from;
          // blk.mTo   = to;
          blk.setName( from, to );
          data_helper.updateShotName( blk.mId, sid, from, to, true );  // SPLAY
        }
      }
    }
  }

  void assignStations_Default( DataHelper data_helper, long sid, List<DBlock> list )
  { 
    // mSecondLastShotId = lastShotId(); // FIXME this probably not needed
    // Log.v("DistoX", "assign stations. size " + list.size() );
    int survey_stations = TDSetting.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splay = TDSetting.mShotAfterSplays;

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
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );

        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          blk.setName( station, "" );
          data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true );  // SPLAY
          // Log.v( "DistoX", blk.mId + " FROM " + blk.mFrom + " PREV null" );
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
              prev.setName( from, to );
              data_helper.updateShotName( prev.mId, sid, from, to, true ); // LEG
              setLegExtend( data_helper, sid, prev );
              if ( forward_shots ) {
                station = shot_after_splay  ? to : from;     // splay-station = this-shot-to if splays before shot
                                                             //                 this-shot-from if splays after shot
                from = to;                                   // next-shot-from = this-shot-to
                to   = DistoXStationName.increment( to, list );  // next-shot-to   = increment next-shot-from
                // Log.v("DistoX", "station [1] " + station + " FROM " + from + " TO " + to );
              } else { // backward_shots
                to   = from;                                     // next-shot-to   = this-shot-from
                from = DistoXStationName.increment( from,list ); // next-shot-from = increment this-shot-from
                station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                                 //                = this-shot-from if splay after shot
                // Log.v("DistoX", "station [2] " + station + " FROM " + from + " TO " + to );
              }
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            blk.setName( station, "" );
            data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true ); // SPLAY
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
            to   = DistoXStationName.increment( from, list );
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? blk.mTo    // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
                                         : blk.mFrom; // 1-2, 1, 1,   [ 2-3, 2, 2, ... ] ...
            } // otherwise station = mCurrentStationName
          } else { // backward shots: ..., 1-0, 2-1 ==> from=Next(2)=3 to=2 ie 3-2
            to      = blk.mFrom;
            from    = DistoXStationName.increment( to, list ); // FIXME it was from
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? from       // 2,   2, 2, 2-1, [ 3, 3, ..., 3-2 ]  ...
                                         : blk.mFrom; // 2-1, 2, 2, 2,   [ 3-2, 3, 3, ... 3 ] ...
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

  String getTRobotStation( int sr, int pt )
  {
    return Integer.toString( sr ) + "." + Integer.toString( pt );
  }

  int getMaxTRobotSeries( List<DBlock> list )
  {
    int ret = 1;
    for ( DBlock blk : list ) {
      if ( blk.mType != DBlock.BLOCK_MAIN_LEG ) continue;
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
  void assignStationsAfter_TRobot( DataHelper data_helper, long sid, DBlock blk0, List<DBlock> list, ArrayList<String> sts )
  {
    // Log.v("DistoX", "TRobot assign stations after.  size " + list.size() );
    boolean increment = true;
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DBlock prev = null;
    String from = blk0.mFrom;
    String to   = blk0.mTo;
    String next = DistoXStationName.increment( to, sts );
    String station = to;

    // int nrLegShots = 0;
    for ( DBlock blk : list ) {
      if ( blk.mType == DBlock.BLOCK_SPLAY ) {
        // blk.mFrom = station;
        blk.setName( station, "" );
        data_helper.updateShotName( blk.mId, sid, blk.mFrom, "", true );  // SPLAY
      } else if ( blk.mType == DBlock.BLOCK_MAIN_LEG ) {
        if ( blk.mId != blk0.mId ) {
          from = to;
          to   = next;
          next = DistoXStationName.increment( to, sts );
          station = to;
          // blk.mFrom = from;
          // blk.mTo   = to;
          blk.setName( from, to );
          data_helper.updateShotName( blk.mId, sid, from, to, true );  // SPLAY
        }
      }
    }
  }

  void assignStations_TRobot( DataHelper data_helper, long sid, List<DBlock> list )
  { 
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

        if ( prev == null ) {
          prev = blk;
          blk.setName( ((station!=null)? station : from), "" );
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
              prev.setName( from, to );
              data_helper.updateShotName( prev.mId, sid, from, to, true ); // LEG
              setLegExtend( data_helper, sid, prev );
              station = to;
              from = to;                                   // next-shot-from = this-shot-to
              to   = DistoXStationName.increment( to, list );  // next-shot-to   = increment next-shot-from
                // Log.v("DistoX", "station [1] " + station + " FROM " + from + " TO " + to );
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            blk.setName( ((station!=null)? station : from), "" );
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
          to   = DistoXStationName.increment( from, list );
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

}
