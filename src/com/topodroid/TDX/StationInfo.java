/* @file StationInfo.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid current station
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.common.StationFlag;

// import androidx.annotation.RecentlyNonNull;

import com.topodroid.utils.TDString;

public class StationInfo
{
  String mName;      // name (ID)
  String mComment;   // comment / description
  StationFlag mFlag; // flags
  String mPresentation;  // presentation string
  String mGeoCode;          // geomorphology code

  /** @return true if the station info flag is "fixed"
   */
  private boolean isFlagFixed()   { return mFlag.isFixed(); }

  /** @return true if the station info flag is "painted"
   */
  private boolean isFlagPainted() { return mFlag.isPainted(); }

  // /** @return true if the station info flag is "geo"
  //  */
  // private boolean isFlagGeo()     { return mFlag.isGeo(); }

  /**
   * @return the string presentation of the flags
   */
  public String getFlagCode() { return mFlag.getCode(); }

  /** cstr
   * @param name     name (presentation string)
   * @param comment  comments (can be null)
   * @param flag     flag
   * @param presentation ???
   */
  StationInfo( String name, String comment, long flag, String presentation, String code )
  {
    mName    = name;
    mComment = (comment == null)? TDString.EMPTY : comment;
    mFlag    = new StationFlag( (int)flag );
    mPresentation = presentation;
    mGeoCode    = code;
  }

  /** @return string presentation of the station info
   * @note the name must be followed by a space because CurrentStationDialog tokenizes on space
   */
  // @RecentlyNonNull
  public String toString()
  { 
    StringBuilder sb = new StringBuilder();
    sb.append( mPresentation ).append(" [").append( getFlagCode() ).append("]");
    if ( mComment != null ) sb.append(" ").append( mComment );
    return sb.toString();
  }

  /** @return the presentation string
   */
  public String getPresentation() { return mPresentation; }

  /** get the current station comment
   * @return comment
   */
  public String getComment() { return mComment; }

  /** @return geomorphology code
   */
  public String getGeoCode() { return mGeoCode; }

  /** set the geomorphology code
   * @param code   new code
   */
  public void setGeoCode( String geocode ) { mGeoCode = (geocode == null)? "" : geocode; }

}
