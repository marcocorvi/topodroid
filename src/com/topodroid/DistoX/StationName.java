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

import android.content.Context;
import android.util.Log;

class StationName
{
  protected Context mContext;
  protected DataHelper mData;
  protected long mSid;

  StationName( Context ctx, DataHelper data, long sid ) 
  {
    mContext = ctx;
    mData    = data;
    mSid     = sid;
  }

  private static final int TRIPLE_SHOT_BELL_TIME = 200; // ms
  private static final int TRIPLE_SHOT_VIBRATE_TIME = 200; // ms

  void assignStations( List<DBlock> list, Set<String> sts )
  {
  }

  void assignStationsAfter( DBlock blk0, List<DBlock> list, Set<String> sts )
  {
  }

  protected void assignStationsBackshot( List<DBlock> list, Set<String> sts )
  {
  }

  // ----------------------------------------------------------------
  protected void legFeedback( )
  {
    if ( TDSetting.mTripleShot == 1 ) {
      TDUtil.ringTheBell( TRIPLE_SHOT_BELL_TIME );
    } else if ( TDSetting.mTripleShot == 2 ) {
      TDUtil.vibrate( mContext, TRIPLE_SHOT_VIBRATE_TIME );
    }
  }

  // ----------------------------------------------------------------
  // current station(s)
  protected static String mCurrentStationName = null;

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

  static private String getLastStationName( DataHelper data, long sid )
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
    DBlock last = data.selectLastNonBlankShot( sid, TDStatus.NORMAL, TDSetting.mDistoXBackshot );
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

  static void resetCurrentOrLastStation( DataHelper data, long sid )
  {
    if ( mCurrentStationName == null ) mCurrentStationName = getLastStationName( data, sid );
  }

  static String getCurrentOrLastStation( DataHelper data, long sid )
  {
    return ( mCurrentStationName != null )? mCurrentStationName : getLastStationName( data, sid );
  }
  
  // String getFirstStation( ) { return mData.getFirstStation( mSid ); }
  static String getFirstStation( DataHelper data, long sid ) { return data.getFirstStation( sid ); }
  
  static void clearCurrentStation() { mCurrentStationName = null; }

  // ------------------------------------------------------------------------------------------------
  // setting the leg extend automatically, sets also stretch to 0

  protected void setLegExtend( DBlock blk )
  {
    // FIXME_EXTEND what has "splay extend" to do with "leg extend" ???
    // if ( ! TDSetting.mSplayExtend ) 
    {
      long extend = TDAzimuth.computeLegExtend( blk.mBearing );
      blk.setExtend( (int)extend, DBlock.STRETCH_NONE ); 
      mData.updateShotExtend( blk.mId, mSid, extend, DBlock.STRETCH_NONE, true );
    }
  }


  // used to set block extend "fixed"
  protected void setLegFixedExtend( DBlock blk, long extend )
  {
    blk.setExtend( (int)extend, DBlock.STRETCH_NONE );
    mData.updateShotExtend( blk.mId, mSid, extend, DBlock.STRETCH_NONE, true );
  }

  // ------------------------------------------------------------------------------------------------
  // station assignments

  protected void setBlockName( DBlock blk, String from, String to, boolean is_backleg ) 
  {
    blk.setBlockName( from, to, is_backleg );
    mData.updateShotName( blk.mId, mSid, from, to, true );
  }

  protected void setBlockName( DBlock blk, String from, String to )
  {
    blk.setBlockName( from, to );
    mData.updateShotName( blk.mId, mSid, from, to, true );
  }

  // ------------------------------------------------------------------------------------------------
  // called in assignStationsAfter_Backsight
  //           assignStations_BacksightBachshot
  //           assignStations_Backsight
  // note backsight-shot is a shot taken backsight (ie backward)
  //      backshot is a distox mode, in which direction data are stored reversed
  protected static boolean checkBacksightShot( DBlock blk, float length, float bearing, float clino )
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

}
