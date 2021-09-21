/** @file TdmShot.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager survey shot object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
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

  void setStations( TdmStation fs, TdmStation ts )
  {
    mFromStation = fs;
    mToStation   = ts;
  }

}

