/* @file CurrentStation.java
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

// import androidx.annotation.RecentlyNonNull;

import com.topodroid.utils.TDString;

public class CurrentStation
{
  final String mName;
  final String mComment;
  final int mFlag;

  public static final int STATION_NONE    = 0;
  public static final int STATION_FIXED   = 1;
  public static final int STATION_PAINTED = 2;

  static final private String[] flag_str = { " ", " [F] ", " [P] " };

  CurrentStation( String name, String comment, long flag )
  {
    mName    = name;
    mComment = (comment == null)? TDString.EMPTY : comment;
    mFlag    = (int)flag;
  }

  // @RecentlyNonNull
  public String toString()
  { 
    return mName + flag_str[mFlag] + mComment;
  }

  public String getFlagCode() 
  {
    switch ( mFlag ) {
      case 0: return " ";
      case 1: return "F";
      case 2: return "P";
      default: return "?";
    }
  }

  public String getComment() { return mComment; }
}
