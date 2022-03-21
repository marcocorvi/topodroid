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
package com.topodroid.DistoX;

import com.topodroid.common.StationFlag;

// import androidx.annotation.RecentlyNonNull;

import com.topodroid.utils.TDString;

public class StationInfo
{
  String mName;      // name (ID)
  String mComment;   // comment / description
  StationFlag mFlag; // flags
  String mPresentation;  // presentation string

  // flag tests
  private boolean isFlagFixed()   { return mFlag.isFixed(); }
  private boolean isFlagPainted() { return mFlag.isPainted(); }
  // private boolean isFlagGeo()     { return mFlag.isGeo(); }

  /**
   * @return the string presentation of the flags
   */
  public String getFlagCode() { return mFlag.getCode(); }

  /** cstr
   * @param id       station ID
   * @param name     name (presentation string)
   * @param comment  comments (can be null)
   * @param flag     flag
   */
  StationInfo( String name, String comment, long flag, String presentation )
  {
    mName    = name;
    mComment = (comment == null)? TDString.EMPTY : comment;
    mFlag    = new StationFlag( (int)flag );
    mPresentation = presentation;
  }

  // @RecentlyNonNull
  public String toString()
  { 
    StringBuilder sb = new StringBuilder();
    sb.append( mPresentation ).append("[").append( getFlagCode() ).append("]");
    if ( mComment != null ) sb.append( mComment );
    return sb.toString();
  }

  /** @return the presentation string
   */
  public String getPresentation() { return mPresentation; }

  /** get the current station comment
   * @return comment
   */
  public String getComment() { return mComment; }
}
