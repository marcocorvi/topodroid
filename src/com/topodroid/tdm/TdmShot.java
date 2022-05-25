/** @file TdmShot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager survey shot object
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

public class TdmShot
{
  private static final float DEG2RAD = (float)(Math.PI/180);

  String mFrom;
  String mTo;
  TdmStation mFromStation;
  TdmStation mToStation; 
  float mLength, mBearing, mClino;  // radians
  int mExtend;
  TdmSurvey mSurvey;  // survey this shot belongs to

  /** cstr
   * @param f       FROM station name
   * @param t       TO station name
   * @param l       length
   * @param b       bearing (azimuth)
   * @param c       clino
   * @param e       extend
   * @param survey  the survey of this shot
   */
  public TdmShot( String f, String t, float l, float b, float c, int e, TdmSurvey survey )
  {
    mFrom = f;
    mTo   = t;
    mLength  = l;
    mBearing = b * DEG2RAD;
    mClino   = c * DEG2RAD;
    mExtend  = e;
    mFromStation = null;
    mToStation   = null;
    mSurvey = survey;
  }

  /** cstr (FROM and TO stations are set null)
   * @param l       length
   * @param b       bearing (azimuth)
   * @param c       clino
   * @param e       extend
   * @param survey  the survey of this shot
   */
  public TdmShot( float l, float b, float c, int e, TdmSurvey survey )
  {
    mFrom = null;
    mTo   = null;
    mLength  = l;
    mBearing = b * DEG2RAD;
    mClino   = c * DEG2RAD;
    mExtend  = e;
    mFromStation = null;
    mToStation   = null;
    mSurvey = survey;
  }

  /** set TDM stations
   * @param fs FROM station 
   * @param ts TO station
   * @note DO NOT change the FROM/TO strings
   */
  void setTdmStations( TdmStation fs, TdmStation ts )
  {
    mFromStation = fs;
    mToStation   = ts;
  }

}

