/* @file StationPolicy.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid station naming policy
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * N.B. backsight and tripod need the whole list to decide how to assign names
 */
package com.topodroid.DistoX;

import android.util.Log;

class StationPolicy
{
  // must agree with res/values/array.xml surveyStationsValue
  static int SURVEY_STATION_ZERO      = 0;
  static int SURVEY_STATION_FOREWARD  = 1;
  static int SURVEY_STATION_BACKWARD  = 2;
  static int SURVEY_STATION_FOREWARD2 = 3;
  static int SURVEY_STATION_BACKWARD2 = 4;
  static int SURVEY_STATION_BACKSIGHT = 5;
  static int SURVEY_STATION_TRIPOD    = 6;
  static int SURVEY_STATION_TOPOROBOT = 7;
  static int SURVEY_STATION_ANOMALY   = 8;

  static boolean mShotAfterSplays = true;;
  private static boolean mBacksightShot = false;    // backsight shooting policy
  private static boolean mTripodShot = false;    // tripod shooting policy
  private static boolean mTRobotShot = false;    // TopoRobot shooting policy
  private static boolean mMagAnomaly = false; // local magnetic anomaly survey, tested with doMagAnomaly

  static long    mTitleColor;
  static int     mSurveyStations  = SURVEY_STATION_FOREWARD;
  private static int mSavedPolicy = SURVEY_STATION_ZERO;

  static boolean isSurveyForward()  { return (mSurveyStations%2) == SURVEY_STATION_FOREWARD; }
  static boolean isSurveyBackward() { return mSurveyStations>0 && (mSurveyStations%2) == SURVEY_STATION_ZERO; }
  // the check on the level should not be neceessary
  static boolean doMagAnomaly() { return mMagAnomaly && TDLevel.overAdvanced; }
  static boolean doTopoRobot()  { return mTRobotShot && TDLevel.overExpert; }
  static boolean doTripod()     { return mTripodShot && TDLevel.overNormal; }
  static boolean doBacksight()  { return mBacksightShot; }

  static int savedPolicy() { return mSavedPolicy; }

  static boolean policyDowngrade( int level )
  {
    // these checks are done with the old level: if level is lowered commit default policy to DB
    return (    ( doMagAnomaly() && level < TDLevel.EXPERT )
             || ( doTopoRobot()  && level < TDLevel.TESTER )
             || ( doTripod()     && level < TDLevel.ADVANCED ) );
  }

  static int policyUpgrade( int level )
  {
    // after settin the level, check if it has been raised and the saved policy committed to DB
    switch ( level ) { // order by decreasing level
      case TDLevel.TESTER:
        if ( mSavedPolicy == SURVEY_STATION_TOPOROBOT ) return 7;
      case TDLevel.EXPERT:
        if ( mSavedPolicy == SURVEY_STATION_ANOMALY )   return 8;
      case TDLevel.ADVANCED:
        if ( mSavedPolicy == SURVEY_STATION_TRIPOD    ) return 6;
        if ( mSavedPolicy == SURVEY_STATION_FOREWARD2 ) return 3;
        if ( mSavedPolicy == SURVEY_STATION_BACKWARD2 ) return 4;
    }
    return 0;
  }

  static boolean setPolicy( int policy )
  {
    // Log.v("DistoX", "set survey stations " + mSurveyStations );
    if ( policy == SURVEY_STATION_TOPOROBOT ) {
      if ( TDLevel.overExpert ) {
        mTRobotShot      = true;
        mBacksightShot   = false;
        mTripodShot      = false;
        mShotAfterSplays = true;
        mMagAnomaly      = false;
        mSurveyStations  = SURVEY_STATION_FOREWARD;
        mTitleColor = TDColor.TITLE_TOPOROBOT;
	mSavedPolicy = policy;
      } else {
        return false;
      }
    } else if ( policy == SURVEY_STATION_TRIPOD ) {
      if ( TDLevel.overNormal ) {
        mTRobotShot      = false;
        mBacksightShot   = false;
        mTripodShot      = true;
        mShotAfterSplays = true;
        mMagAnomaly      = false;
        mSurveyStations  = SURVEY_STATION_FOREWARD;
        mTitleColor = TDColor.TITLE_TRIPOD;
	mSavedPolicy = policy;
      } else {
        return false;
      }
    } else if ( policy == SURVEY_STATION_BACKSIGHT ) {
      // if ( TDLevel.overNothing ) {
        mTRobotShot      = false;
        mBacksightShot   = true;
        mTripodShot      = false;
        mShotAfterSplays = true;
        mMagAnomaly      = false;
        mSurveyStations  = SURVEY_STATION_FOREWARD;
        mTitleColor = TDColor.TITLE_BACKSIGHT;
	// mSavedPolicy = policy; // not neceessary
      // } else {
      //   return false;
      // }
    } else if ( policy == SURVEY_STATION_ANOMALY ) {
      if ( TDLevel.overAdvanced ) {
        mTRobotShot      = false;
        mBacksightShot   = true;
        mTripodShot      = false;
        mShotAfterSplays = true;
        mMagAnomaly      = true;
        mSurveyStations  = SURVEY_STATION_FOREWARD;
        mTitleColor = TDColor.TITLE_ANOMALY;
	mSavedPolicy = policy;
      } else {
	return false;
      }
    } else {
      mSurveyStations = policy;
      mShotAfterSplays = ( mSurveyStations <= 2 );
      if ( mSurveyStations > 2 ) {
	mSurveyStations -= 2;
	mSavedPolicy = policy;
      }
      if ( mSurveyStations == SURVEY_STATION_FOREWARD ) mTitleColor = TDColor.TITLE_BACKSHOT;
    }
    // Log.v("DistoX", "set survey stations. policy " + policy );
    return true;
  }

  // called on changed preferences
  // static private void setMagAnomaly( SharedPreferences prefs, boolean val )
  // {
  //   mMagAnomaly = val;
  //   if ( mMagAnomaly ) {
  //     int policy = mSavedPolicy;
  //     if ( ! TDLevel.overExpert ) { 
  //       setPreference( prefs, "DISTOX_MAG_ANOMALY", false );
  //     } else if ( mSurveyStations > 0 ) {
  //       setPreference( prefs, "DISTOX_SURVEY_STATION", "5" ); // SURVEY_STATION_BACKSIGHT This change saved policy
  //       mBacksightShot   = true;
  //       mTripodShot      = false;
  //       mSurveyStations  = SURVEY_STATION_FOREWARD;
  //       mShotAfterSplays = true;
  //     }
  //     mSavedPolicy = policy;
  //   } else {
  //     setPreference( prefs, "DISTOX_SURVEY_STATION", Integer.toString( mSavedPolicy ) );
  //   }
  //   // FIXME this one should tell the mPrefActivitySurvey to reload preferences
  //   //       however this restarts the activity which is not good
  //   // if ( TopoDroidApp.mPrefActivitySurvey != null ) TopoDroidApp.mPrefActivitySurvey.reloadPreferences();
  //   //
  //   // Log.v("DistoX", "SET Policy " + mSurveyStations + " " + mSavedPolicy + " mag anomlay " + mMagAnomaly );
  // }

  // // called only by parseStationPolicy
  // static private void clearMagAnomaly( SharedPreferences prefs ) 
  // {
  //   if ( mMagAnomaly ) {
  //     mMagAnomaly = false;
  //     // setPreference( prefs, "DISTOX_MAG_ANOMALY", false );
  //   }
  //   // Log.v("DistoX", "CLEAR Policy " + mSurveyStations + " " + mSavedPolicy + " mag anomlay " + mMagAnomaly );
  // }

}

