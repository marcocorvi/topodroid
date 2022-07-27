/* @file SurveyFixed.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief survey fixed point - as in TopoDroid database
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3db;

import com.topodroid.utils.TDLog;

public class SurveyFixed
{
  public String mCsName = null;
  public String station;
  public double mLongitude;
  public double mLatitude;
  public double mAltitude;
  public double mAltimetric;
  public double mCsLongitude;
  public double mCsLatitude;
  public double mCsAltitude;

  // public void log()
  // { 
  //   // TDLog.v("fix " + station + " " + mLongitude + " " + mLatitude + " <" + ((mCsName != null)? mCsName : "null" ) + ">" );
  // }

  public SurveyFixed( String name )
  {
    station = name;
  }

  public boolean hasCS() { return ( mCsName != null ) && ( mCsName.length() > 0 ); }
}
