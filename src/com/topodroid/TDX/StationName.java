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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDFeedback;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.ExtendType;
import com.topodroid.common.LegType;

import java.util.List;
import java.util.Set;
// import java.util.ArrayList;

import android.content.Context;

import android.view.View;

class StationName
{
  protected Context mContext;
  protected DataHelper mData;
  protected long mSid;

  /** cstr
   * @param ctx    context
   * @param data   database helper
   * @param sid    survey ID
   */
  StationName( Context ctx, DataHelper data, long sid ) 
  {
    mContext = ctx;
    mData    = data;
    mSid     = sid;
  }

  /** debug
   */
  protected String id( DBlock blk ) { return (blk==null)? "<->" : "<" +Long.toString(blk.mId) + ">"; }

  /** debug
   */
  protected String name( DBlock blk ) { return (blk==null)? "<->" : "<" + Long.toString(blk.mId) + ":" + blk.mFrom + "-" + blk.mTo + ">"; }


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
  
  // @note used only to reset/getCurrentOrLastStation name
  static private String getLastStationName( DataHelper data, long sid )
  {
    DBlock last = data.selectLastNonBlankShot( sid );
    if ( last == null ) return TDSetting.mInitStation;
    if ( last.isDistoXBacksight() ) {
      if ( TDString.isNullOrEmpty( last.mFrom ) ) return last.mTo;
      if ( StationPolicy.mSurveyStations == 1 ) return last.mFrom;  // forward-shot
      return last.mTo;
    } else {
      if ( TDString.isNullOrEmpty( last.mTo ) ) return last.mFrom;
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
      // TDLog.Log( TDLog.LOG_SHOT, blk.mId + " set extend " + extend );
      blk.setExtend( (int)extend, ExtendType.STRETCH_NONE ); 
      mData.updateShotExtend( blk.mId, mSid, extend, ExtendType.STRETCH_NONE );
    }
  }


  // used to set block extend "fixed"
  protected void setLegFixedExtend( DBlock blk, long extend )
  {
    // TDLog.Log( TDLog.LOG_SHOT, blk.mId + " set fixed extend " + extend );
    blk.setExtend( (int)extend, ExtendType.STRETCH_NONE );
    mData.updateShotExtend( blk.mId, mSid, extend, ExtendType.STRETCH_NONE );
  }

  // ------------------------------------------------------------------------------------------------
  // station assignments

  /** set the block stations (only for legs)
   * @param blk     block
   * @param from    FROM station
   * @param to      TO station
   * @param is_backleg whether the shot is a back-leg
   * @note the block name is updated into the database
   */
  protected void setBlockName( DBlock blk, String from, String to, boolean is_backleg ) 
  {
    // TDLog.Log( TDLog.LOG_SHOT, blk.mId + " set name " + from + "-" + to + " backleg " + is_backleg );
    blk.setBlockName( from, to, is_backleg );
    // if ( mData.checkSiblings( blk.mId, mSid, from, to, blk.mLength, blk.mBearing, blk.mClino ) ) { // bad sibling
    //   TDLog.v("station name detect bad sibling (1)");
    //   TDToast.makeWarn( R.string.bad_sibling );
    // }
    mData.updateShotName( blk.mId, mSid, from, to );
  }

  /** set the block stations
   * @param blk     block
   * @param from    FROM station
   * @param to      TO station
   * @note the block name is updated into the database
   */
  protected void setBlockName( DBlock blk, String from, String to )
  {
    // TDLog.v( "set block " + blk.mId + " name " + from + " " + to );
    // TDLog.Log( TDLog.LOG_SHOT, blk.mId + " set name " + from + "-" + to );
    blk.setBlockName( from, to );
    // if ( mData.checkSiblings( blk.mId, mSid, from, to, blk.mLength, blk.mBearing, blk.mClino ) ) { // bad sibling
    //   TDLog.v("station name detect bad sibling (2)");
    //   TDToast.makeWarn( R.string.bad_sibling );
    // }
    mData.updateShotName( blk.mId, mSid, from, to );
  }

  // ------------------------------------------------------------------------------------------------
  /** @return true if the block is a backsight
   * @param blk     block
   * @param length  reference length (block is backsight if its length is close to length)
   * @param bearing reference azimuth (block is backsight if its azimuth is 180 degrees from this)
   * @param clino   reference clino (block is backsight if its clino is the negative of this)
   *
   * called in assignStationsAfter_Backsight
   *           assignStations_BacksightBackshot
   *           assignStations_Backsight
   * note backsight-shot is a shot taken backsight (ie backward)
   *      backshot is a distox mode, in which direction data are stored reversed
   */
  protected static boolean checkBacksightShot( DBlock blk, float length, float bearing, float clino )
  {
    float d_thr = TDSetting.mCloseDistance * (blk.mLength+length);
    if ( Math.abs( length - blk.mLength ) > d_thr ) {
      // TDLog.v( "backshot check fails on distance " + length + " " + blk.mLength + " thr " + d_thr );
      return false;
    }
    float a_thr = TDSetting.mCloseDistance * 112; // rad2deg * 2 
    if ( Math.abs( clino + blk.mClino ) > a_thr ) {
      // TDLog.v( "backshot check fails on clino " + clino + " " + blk.mClino + " thr " + a_thr );
      return false;
    }
    if ( ! StationPolicy.doMagAnomaly() ) {
      if ( Math.abs( ( bearing < blk.mBearing )? blk.mBearing - bearing - 180 : bearing - blk.mBearing - 180 ) > a_thr ) {
        // TDLog.v( "backshot check fails on bearing " + bearing + " " + blk.mBearing + " thr " + a_thr );
        return false;
      }
    }
    return true;
  }

  /** set the stations of a leg
   * @param blk    leg block
   * @param from    FROM station
   * @param to      TO station
   * @note the block name is saved to the database
   */
  protected void setLegName( DBlock blk, String from, String to )
  {
    // TDLog.v( "set leg " + blk.mId + " " + from + " " + to );
    if ( blk.isDistoXBacksight() ) { // bs
      setBlockName( blk, to, from );
    } else {
      setBlockName( blk, from, to );
    }
  }

  /** set the stations of a leg
   * @param blk    leg block
   * @param from    FROM station
   * @param to      TO station
   * @param is_backsight_shot whether the leg is backsight
   * @note the block name is saved to the database
   */
  protected void setLegName( DBlock blk, String from, String to, boolean is_backsight_shot )
  {
    // TDLog.v( "set leg " + from + " " + to + " bs " + is_backsight_shot );
    if ( blk.isDistoXBacksight() ) { // bs
      setBlockName( blk, to, from, is_backsight_shot );
    } else {
      setBlockName( blk, from, to, is_backsight_shot );
    }
  }
 
  /** set the block type to "secondary leg"
   * @param blk   leg secondary-block
   * @note the block name is set and saved to the database
   */
  protected void setSecLegName( DBlock blk )
  {
    // TDLog.v( "set sec leg " + blk.mId );
    setBlockName( blk, "", "" ); // FIXME_BLUNDER This is important for blunder-shot
    blk.setTypeSecLeg();
  }
 
  /** set the block type to "secondary leg"
   * @param blk   leg secondary-block
   * @param update_db whether to update leg type in the database - done in case of BLUNDER
   * @note called when the block comes after a blunder shot
   */
  protected void setSecLegNameAndType( DBlock blk, boolean update_db )
  {
    // TDLog.v( "set sec leg " + blk.mId );
    // setBlockName( blk, "", "" );
    blk.setTypeSecLeg();
    if ( update_db ) mData.updateShotLeg( blk.mId, mSid, LegType.EXTRA ); // must be done only if previous block is BLUNDER
  }

  /** clear the block stations and set its type to "blank" and status to "blunder"
   * @param blk   blunder block
   * @note this should be called only if TDSetting.mBlunderShot is true
   * @note the block name and blunder status are saved to the database
   */
  protected void setBlunderName( DBlock blk ) // BLUNDER
  { 
    // if ( ! TDSetting.mBlunderShot ) return; // unnecessary
    setBlockName( blk, "", "" );
    blk.setTypeBlank();
    mData.deleteShot( blk.mId, mSid, TDStatus.BLUNDER );
    blk.setVisible( View.GONE );
  }

  /** clear the blunder status of the block record in the datebase
   */
  protected void clearBlunder( DBlock blk ) 
  {
    mData.undeleteShot( blk.mId, mSid );
    blk.setVisible( View.VISIBLE );
  }

  /** set the station of a splay
   * @param splay  splay block
   * @param name   station 
   * @note if the DistoX is in normal mode the station is FROM, if it is backsight the station is TO
   * @note the block name is saved to the database
   */
  protected void setSplayName( DBlock splay, String name ) 
  {
    // TDLog.v( "set splay " + splay.mId + " " + name );
    if ( splay.isDistoXBacksight() ) {
      setBlockName( splay, "", name );
    } else {
      setBlockName( splay, name, "" );
    }
  }

}
