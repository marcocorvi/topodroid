/* @file RawDBlock.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid raw survey data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class RawDBlock
{
  long   mId;
  long   mTime;
  long   mSurveyId;
  // private String mName;
  String mFrom;    // N.B. mfrom and mTo must be not null
  String mTo;
  float mLength;   // meters
  float mBearing;  // degrees
  float mClino;    // degrees
  float mRoll;     // degrees
  float mAcceleration;
  float mMagnetic;
  float mDip;
  // public float mDepth;     // depth at from station
  String mComment;

  int  mExtend;
  long mFlag;     
  int  mLeg;       // ? mBlockType;   
  int  mShotType;  // 0: DistoX, 1: manual, -1: DistoX backshot
  int  mStatus;    // FIXME used only to export CSV raw data
  // boolean mWithPhoto;

  // public  boolean mMultiBad; // whether it disagree with siblings
  // private float mStretch;
  String mAddress; // DistoX address - used only in exports
  // boolean mWasRecent = false; // REVISE_RECENT
}
