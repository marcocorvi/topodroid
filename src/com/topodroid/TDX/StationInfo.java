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

import com.topodroid.types.StationType;

// import androidx.annotation.RecentlyNonNull;

import com.topodroid.util.TDString;

public class StationInfo
{
  String mName;          // name (ID)
  String mComment;       // comment / description
  StationType mType;     // type
  String mPresentation;  // presentation string
  String mGeoCode;       // geomorphology code
  long mPhotoId;         // photo ID

  /** @return true if the station info flag is "fixed"
   */
  boolean isTypeFixed()   { return mType.isFixed(); }

  /** @return true if the station info flag is "painted"
   */
  boolean isTypePainted() { return mType.isPainted(); }

  // /** @return true if the station info flag is "geo"
  //  */
  // private boolean isTypeGeo()     { return mType.isGeo(); }

  /**
   * @return the string presentation of the flags
   * @note used also by ShpNamez
   */
  public String getTypeCode() { return mType.getCode(); }

  public int getTypeValue() { return mType.getValue(); }

  /** cstr
   * @param name     name (presentation string)
   * @param comment  comments (can be null)
   * @param flag     flag
   * @param presentation ???
   */
  StationInfo( String name, String comment, long flag, String presentation, String code, long photoId )
  {
    mName    = name;
    mComment = (comment == null)? TDString.EMPTY : comment;
    mType    = new StationType( (int)flag );
    mPresentation = presentation;
    mGeoCode    = code;
    mPhotoId    = photoId;
  }

  /** @return string presentation of the station info
   * @note the name must be followed by a space because CurrentStationDialog tokenizes on space
   */
  // @RecentlyNonNull
  public String toString()
  { 
    StringBuilder sb = new StringBuilder();
    sb.append( mPresentation ).append(" [").append( getTypeCode() ).append("]");
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

  public long getPhotoId() { return mPhotoId; }

}
