/* @file SurveyInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief survey info (name, date, comment etc) - as in TopoDroid database
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3db;

// import com.topodroid.utils.TDLog;

public class SurveyInfo
{
  public final static double DECLINATION_MAX = 720;    // twice 360
  // public final static double DECLINATION_UNSET = 1080; // three times 360 (unused)

  public long id;
  public String name;
  public String date;
  public String team;
  public double  declination;

  public boolean hasDeclination() { return declination < DECLINATION_MAX; }

  // get the declination or 0 if not-defined
  public double getDeclination()
  {
    if ( declination < DECLINATION_MAX ) return declination;
    return 0;
  }

}
