/* @file ParserTherionState.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid state for the Therion parser
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.DistoX;

class ParserTherionState
{
  ParserTherionState mParent;

  boolean in_centerline;
  boolean in_data;
  boolean in_survey;
  boolean in_map;
  boolean in_surface;
  boolean in_scrap;
  boolean in_line;
  boolean in_area;

  float mUnitLen,   mScaleLen,   mZeroLen;
  float mUnitBer,   mScaleBer,   mZeroBer;
  float mUnitCln,   mScaleCln,   mZeroCln;
  // float mUnitLeft,  mScaleLeft,  mZeroLeft;
  // float mUnitRight, mScaleRight, mZeroRight;
  // float mUnitUp,    mScaleUp,    mZeroUp;
  // float mUnitDown,  mScaleDown,  mZeroDown;
  float mDeclination;
  boolean mDuplicate;
  boolean mSurface;
  int mExtend;
  String mPrefix;
  String mSuffix;
  int mSurveyLevel;

  int data_type;

  ParserTherionState()
  {
    mParent = null;
    mUnitLen   = mUnitBer    = mUnitCln   = 1.0f;
    mZeroLen   = mZeroBer    = mZeroCln   = 0.0f;
    mScaleLen  = mScaleBer   = mScaleCln  = 1.0f;
    // mUnitLeft  = mUnitRight  = mUnitUp  = mUnitDown  = 1.0f;
    // mScaleLeft = mScaleRight = mScaleUp = mScaleDown = 1.0f;
    // mZeroLeft  = mZeroRight  = mZeroUp  = mZeroDown  = 0.0f;
    mDeclination= 0.0f;
    mDuplicate = false;
    mSurface   = false;
    mExtend = DBlock.EXTEND_RIGHT;
    mPrefix = "";
    mSuffix = "";
    mSurveyLevel = 0;
    in_centerline = false;
    in_data = false;
    in_survey = false;
    in_map = false;
    in_surface = false;
    in_scrap = false;
    in_line = false;
    in_area = false;
    data_type = 0; // DATA_NONE
  }

  ParserTherionState( ParserTherionState state )
  {
    mParent  = state;

    mUnitLen = state.mUnitLen;
    mUnitBer = state.mUnitBer;
    mUnitCln = state.mUnitCln;
    mZeroLen = state.mZeroLen;
    mZeroBer = state.mZeroBer;
    mZeroCln = state.mZeroCln;
    mScaleLen = state.mScaleLen;
    mScaleBer = state.mScaleBer;
    mScaleCln = state.mScaleCln;

    // mUnitLeft   = state.mUnitLeft;
    // mUnitRight  = state.mUnitRight;
    // mUnitUp     = state.mUnitUp;
    // mUnitDown   = state.mUnitDown;
    // mScaleLeft  = state.mScaleLeft;
    // mScaleRight = state.mScaleRight;
    // mScaleUp    = state.mScaleUp;
    // mScaleDown  = state.mScaleDown;
    // mZeroLeft   = state.mZeroLeft;
    // mZeroRight  = state.mZeroRight;
    // mZeroUp     = state.mZeroUp;
    // mZeroDown   = state.mZeroDown;

    mDeclination= state.mDeclination;
    mDuplicate = state.mDuplicate;
    mSurface   = state.mSurface;
    mExtend = state.mExtend;
    mPrefix = state.mPrefix;
    mSuffix = state.mSuffix;
    mSurveyLevel = state.mSurveyLevel;
    in_centerline = state.in_centerline;
    in_data = state.in_data;
    in_survey = state.in_survey;
    in_map = state.in_map;
    in_surface = state.in_surface;
    in_scrap = state.in_scrap;
    in_line = state.in_line;
    in_area = state.in_area;
    data_type = state.data_type;
  }

}
