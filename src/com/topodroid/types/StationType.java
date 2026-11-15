/* @file StationType.java
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
package com.topodroid.types;

// import androidx.annotation.RecentlyNonNull;

public class StationType
{
  public static final int STATION_NONE    = 0;  // no flag = temporary (default)
  public static final int STATION_FIXED   = 1;  // fixed station
  public static final int STATION_PAINTED = 2;  // painted station
  private static final int STATION_MAX = 2; // or of all flags

  private int mValue;

  static final private String[] flag_str = { " ", " [F] ", " [P] ", " [T] " };

  /** cstr
   * @param flag   flag value
   */
  public StationType( int flag ) 
  {
    if ( flag < STATION_NONE || flag > STATION_MAX ) flag = STATION_NONE;
    mValue = flag;
  }

  /** @return the flag value
   */
  public int getValue() { return mValue; }

  // flag tests
  public static boolean isFixed( int flag )   { return (flag & STATION_FIXED)   == STATION_FIXED; }
  public static boolean isPainted( int flag ) { return (flag & STATION_PAINTED) == STATION_PAINTED; }
  public static boolean isTemporary( int flag ) { return flag == 0; }
  // public static boolean isGeo( int flag )     { return (flag & STATION_GEO)     == STATION_GEO; }

  public boolean isFixed( )   { return (mValue & STATION_FIXED)   == STATION_FIXED; }
  public boolean isPainted( ) { return (mValue & STATION_PAINTED) == STATION_PAINTED; }
  public boolean isTemporary( ) { return mValue == 0; }
  // public boolean isGeo( )     { return (mValue & STATION_GEO)     == STATION_GEO; }

  /**
   * @return the string presentation of the flags
   */
  public static String getCode( int flag ) 
  {
    if ( flag == 0 ) return " ";
    StringBuilder sb = new StringBuilder();
    if ( isFixed(flag) )     sb.append("F");
    if ( isPainted(flag) )   sb.append("P");
    // if ( isGeo(flag) )     sb.append("G");
    return sb.toString();
  }

  public String getCode() { return getCode( mValue ); }

}
