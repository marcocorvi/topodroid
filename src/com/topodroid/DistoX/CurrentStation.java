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
  final String mName;    // name
  final String mComment; // comment / description
  final int mFlag;       // flags

  public static final int STATION_NONE    = 0;  // no flag
  public static final int STATION_FIXED   = 1;  // fixed station
  public static final int STATION_PAINTED = 2;  // painted station
  // public static final int STATION_GEO     = 4;  // geolocalized station

  static final private String[] flag_str = { " ", " [F] ", " [P] " };

  // flag tests
  private boolean isFlagFixed()   { return (mFlag & STATION_FIXED)   == STATION_FIXED; }
  private boolean isFlagPainted() { return (mFlag & STATION_PAINTED) == STATION_PAINTED; }
  private boolean isFlagGeo()     { return (mFlag & STATION_GEO)     == STATION_GEO; }

  /**
   * @return the string presentation of the flags
   */
  public String getFlagCode() 
  {
    if ( mFlag == 0 ) return " ";
    StringBuilder sb = new StringBuilder();
    if ( isFlagFixed() )   sb.append("F");
    if ( isFlagPainted() ) sb.append("P");
    if ( isFlagGeo() )     sb.append("G");
    return sb.toString();
  }

  /** cstr
   * @param name     name
   * @param comment  comments (can be null)
   * @param flag     flag
   */
  CurrentStation( String name, String comment, long flag )
  {
    mName    = name;
    mComment = (comment == null)? TDString.EMPTY : comment;
    mFlag    = (int)flag;
  }

  // @RecentlyNonNull
  public String toString()
  { 
    StringBuilder sb = new StringBuilder();
    sb.append( mname ).append("[").append( getFlagCode() ).append("]");
    if ( mComment != null ) sb.append( mComment );
    return sb.toString();
  }

  /** get the current station comment
   * @return comment
   */
  public String getComment() { return mComment; }
}
