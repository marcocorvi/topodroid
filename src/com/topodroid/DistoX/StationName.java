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

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFeedback;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.ExtendType;

import android.util.Log;

import java.util.List;
import java.util.Set;
// import java.util.ArrayList;

import android.content.Context;

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

  boolean assignStations( List< DBlock > list, Set<String> sts )
  {
    return false;
  }

  boolean assignStationsAfter( DBlock blk0, List< DBlock > list, Set<String> sts )
  {
    return false;
  }

  // ----------------------------------------------------------------
  protected void legFeedback( ) 
  {
    TDFeedback.legFeedback( mContext );
  }

  // ----------------------------------------------------------------
  // current station(s)
  protected static volatile String mCurrentStationName = null;

  static String getCurrentStationName() { return mCurrentStationName; }

  static boolean isCurrentStationName( String name ) { return name.equals(mCurrentStationName); }

  static void clearCurrentStation() { mCurrentStationName = null; }

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
  
  // used only to reset/getCurrentOrLastStation name
  static private String getLastStationName( DataHelper data, long sid )
  {
    DBlock last = data.selectLastNonBlankShot( sid );
    if ( last == null ) return TDSetting.mInitStation;
    if ( last.isDistoXBacksight() ) {
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

  // ------------------------------------------------------------------------------------------------
  // setting the leg extend automatically, sets also stretch to 0

  protected void setLegExtend( DBlock blk )
  {
    // FIXME_EXTEND what has "splay extend" to do with "leg extend" ???
    // if ( ! TDSetting.mSplayExtend ) 
    {
      long extend = TDAzimuth.computeLegExtend( blk.mBearing );
      TDLog.Log( TDLog.LOG_SHOT, blk.mId + " set extend " + extend );
      blk.setExtend( (int)extend, ExtendType.STRETCH_NONE ); 
      mData.updateShotExtend( blk.mId, mSid, extend, ExtendType.STRETCH_NONE );
    }
  }


  // used to set block extend "fixed"
  protected void setLegFixedExtend( DBlock blk, long extend )
  {
    TDLog.Log( TDLog.LOG_SHOT, blk.mId + " set fixed extend " + extend );
    blk.setExtend( (int)extend, ExtendType.STRETCH_NONE );
    mData.updateShotExtend( blk.mId, mSid, extend, ExtendType.STRETCH_NONE );
  }

  // ------------------------------------------------------------------------------------------------
  // station assignments

  protected void setBlockName( DBlock blk, String from, String to, boolean is_backleg ) 
  {
    TDLog.Log( TDLog.LOG_SHOT, blk.mId + " set name " + from + "-" + to + " bckleg " + is_backleg );
    blk.setBlockName( from, to, is_backleg );
    // if ( mData.checkSiblings( blk.mId, mSid, from, to, blk.mLength, blk.mBearing, blk.mClino ) ) { // bad sibling
    //   TDToast.makeWarn( R.string.bad_sibling );
    // }
    mData.updateShotName( blk.mId, mSid, from, to );
  }

  protected void setBlockName( DBlock blk, String from, String to )
  {
    // Log.v( "DistoX-BLOCK", "set block " + blk.mId + " name " + from + " " + to );
    TDLog.Log( TDLog.LOG_SHOT, blk.mId + " set name " + from + "-" + to );
    blk.setBlockName( from, to );
    // if ( mData.checkSiblings( blk.mId, mSid, from, to, blk.mLength, blk.mBearing, blk.mClino ) ) { // bad sibling
    //   TDToast.makeWarn( R.string.bad_sibling );
    // }
    mData.updateShotName( blk.mId, mSid, from, to );
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

  protected void setLegName( DBlock blk, String from, String to )
  {
    if ( blk.isDistoXBacksight() ) { // bs
      setBlockName( blk, to, from );
    } else {
      setBlockName( blk, from, to );
    }
  }

  protected void setLegName( DBlock blk, String from, String to, boolean is_backsight_shot )
  {
    if ( blk.isDistoXBacksight() ) { // bs
      setBlockName( blk, to, from, is_backsight_shot );
    } else {
      setBlockName( blk, from, to, is_backsight_shot );
    }
  }

  protected void setSplayName( DBlock splay, String name ) 
  {
    if ( splay.isDistoXBacksight() ) {
      setBlockName( splay, "", name );
    } else {
      setBlockName( splay, name, "" );
    }
  }


}
