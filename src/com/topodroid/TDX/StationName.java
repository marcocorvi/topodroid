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

// import com.topodroid.utils.TDLog;
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

  /** compare two string token, first as integers, if that fails as strings
   * @param t1   first token
   * @param t2   second token
   * @return -1: first greater than second, 1: second greater than first; 0 equal
   */
  private static int compareToken( String t1, String t2 )
  {
    try {
      int i1 = Integer.parseInt( t1 );
      int i2 = Integer.parseInt( t2 );
      return ( i1 < i2 )? 1 : -1;
    } catch ( NumberFormatException e ) {
      // StringBuilde1 sb1 = new StringBuilder();
      // sb1.append( t1[k] );
      // for ( int k1=k+1; k1 < t1.length; ++k1 ) sb1.append( ch1 ).append(t1[k1]);
      // StringBuilde1 sb2 = new StringBuilder();
      // sb2.append( t2[k] );
      // for ( int k2=k+1; k2 < t2.length; ++k2 ) sb2.append( ch1 ).append(t2[k2]);
      // return compareWithSeparators( ch2, -1 );
    }
    return t1.compareTo( t2 );
  }

  /** compare two string names, parsing tokens on char '.'
   * @param n1   first name
   * @param n2   second name
   * @return -1: first greater than second, 1: second greater than first; 0 equal
   */
  static int compareNames( String n1, String n2 ) 
  {
    final String ch1 = ".";
    int p1 = n1.indexOf( ch1 );
    int p2 = n2.indexOf( ch1 );
    if ( p1 >= 0 ) {
      if ( p2 >= 0 ) {
        String[] t1 = n1.split( ch1 );
        String[] t2 = n2.split( ch1 );
        int k = 0;
        while ( k < t1.length && k < t2.length && t1[k].equals( t2[k] ) ) ++k;
        if ( k == t1.length ) {
          if ( k == t2.length ) { 
            return 0;
          } else { // k > t2.length
            return 1;
          }
        } else {
          if ( k == t2.length ) {
            return -1;
          } else { // compare t1[k] and t2[k]
            return compareToken( t1[k], t2[k] );
          }
        }
      } else { // p1 >= 0, p2 < 0
        return -1;
      }
    } else { // p1 < 0
      if ( p2 >= 0 ) {
        return 1;
      } else {
        return compareToken( n1, n2 );
      }
    }
  }
            
  /** assign station names to shots
   * @param list  list of dblock, including those to assign
   * @param sts   station names already in use
   * @return true if a leg has been assigned
   */
  boolean assignStations( List< DBlock > list, Set<String> sts )
  {
    return false;
  }

  /** assign station names to shots after a given shot
   * @param blk0  given shot
   * @param list  list of dblock, including those to assign
   * @param sts   station names already in use
   * @return true if a leg has been assigned
   */
  boolean assignStationsAfter( DBlock blk0, List< DBlock > list, Set<String> sts )
  {
    return false;
  }

  // ----------------------------------------------------------------

  /** generate a visual/audio feedback 
   */
  protected void legFeedback( ) 
  {
    TDFeedback.legFeedback( mContext );
  }

  // ----------------------------------------------------------------
  // current station(s)
  protected static volatile String mCurrentStationName = null;

  /** @return the name of the "current station", or null (if unset)
   */
  static String getCurrentStationName() { return mCurrentStationName; }

  /** @return true if the "current station" name is the specified name
   * @param name   specified name
   */
  static boolean isCurrentStationName( String name ) { return name.equals(mCurrentStationName); }

  // /** @return true if the current station name is set (ie, not null)
  //  */
  // static boolean hasCurrentStationName() { return mCurrentStationName != null; }

  /** unset the "current station"
   */
  static void clearCurrentStation() { mCurrentStationName = null; }

  /** set the "current station" (or clear it(
   * @param name   name of the "current station"
   * @return true if the "current station" is set
   * @note if the given name equals the "current station" this is unset
   */
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

  /** set the "current station" to the "last station" if the current station is unset
   * @param data   database helper
   * @param sid    survey ID
   */
  static void resetCurrentOrLastStation( DataHelper data, long sid )
  {
    if ( mCurrentStationName == null ) mCurrentStationName = getLastStationName( data, sid );
  }

  /** @return the "current station" or the "last station" (if the current station is unset)
   * @param data   database helper
   * @param sid    survey ID
   */
  static String getCurrentOrLastStation( DataHelper data, long sid )
  {
    return ( mCurrentStationName != null )? mCurrentStationName : getLastStationName( data, sid );
  }
  
  /** @return the "first station"
   * @param data   database helper
   * @param sid    survey ID
   */
  static String getFirstStation( DataHelper data, long sid ) { return data.getFirstStation( sid ); }
  // String getFirstStation( ) { return mData.getFirstStation( mSid ); }
  
  /** @return the "last station"
   * @param data   database helper
   * @param sid    survey ID
   * @note used only to reset/getCurrentOrLastStation name
   */
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

  /** set the leg extend automatically, sets also stretch to 0
   * @param blk   data block
   */
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

  /** set the leg extend to a given value, sets also stretch to 0
   * @param blk     data block
   * @param extend  extend value
   * @note used to set block extend "fixed"
   */
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
    //   // TDLog.v("station name detect bad sibling (1)");
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
    //   // TDLog.v("station name detect bad sibling (2)");
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

  /** clear the blunder status of the block record in the database
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
