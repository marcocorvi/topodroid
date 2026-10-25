/* @file ParserState.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid state for the parser
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

class ParserState
{
  int data_type;
  int mSurveyLevel;

  // zero_error includes scale and units
  float mUnitLen,   mScaleLen,   mZeroLen;
  float mUnitBer,   mScaleBer,   mZeroBer;
  float mUnitCln,   mScaleCln,   mZeroCln;
  float mUnitBackLen,   mScaleBackLen,   mZeroBackLen;
  float mUnitBackBer,   mScaleBackBer,   mZeroBackBer;
  float mUnitBackCln,   mScaleBackCln,   mZeroBackCln;
  // float mUnitLeft,  mScaleLeft,  mZeroLeft;
  // float mUnitRight, mScaleRight, mZeroRight;
  // float mUnitUp,    mScaleUp,    mZeroUp;
  // float mUnitDown,  mScaleDown,  mZeroDown;
  float mDeclination = 0.0f;
  boolean mDuplicate = false;
  boolean mSurface   = false;
  boolean mSplay     = false;

  ParserState()
  {
    setDefault();
  }

  ParserState( ParserState state )
  {
    data_type = state.data_type;
    mSurveyLevel = state.mSurveyLevel + 1; // TH does not have +1

    mUnitLen   = state.mUnitLen;
    mUnitBer   = state.mUnitBer;
    mUnitCln   = state.mUnitCln;
    mZeroLen   = state.mZeroLen;
    mZeroBer   = state.mZeroBer;
    mZeroCln   = state.mZeroCln;
    mScaleLen  = state.mScaleLen;
    mScaleBer  = state.mScaleBer;
    mScaleCln  = state.mScaleCln;

    mUnitBackLen  = state.mUnitBackLen;
    mUnitBackBer   = state.mUnitBackBer;
    mUnitBackCln   = state.mUnitBackCln;
    mZeroBackLen  = state.mZeroBackLen;
    mZeroBackBer   = state.mZeroBackBer;
    mZeroBackCln   = state.mZeroBackCln;
    mScaleBackLen = state.mScaleBackLen;
    mScaleBackBer  = state.mScaleBackBer;
    mScaleBackCln  = state.mScaleBackCln;

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
    mSplay     = state.mSplay;
  }

  protected void setDefault()
  {
    data_type = 0; // DATA_NONE as in TH
    mSurveyLevel = 0;
    setUnitsDefault();
    // mUnitLeft  = mUnitRight  = mUnitUp  = mUnitDown  = 1.0f;
    // mScaleLeft = mScaleRight = mScaleUp = mScaleDown = 1.0f;
    // mZeroLeft  = mZeroRight  = mZeroUp  = mZeroDown  = 0.0f;
    setCalibrateDefault();
    mDeclination= 0.0f;
    mDuplicate = false;
    mSurface   = false;
    mSplay     = false;
  }

  void setUnitsDefault()
  {
    mUnitLen = mUnitBer = mUnitCln = mUnitBackLen = mUnitBackBer = mUnitBackCln = 1.0f;
  }


  void setCalibrateDefault()
  {
    mZeroLen   = mZeroBer   = mZeroCln  = mZeroBackLen  = mZeroBackBer  = mZeroBackCln  = 0.0f;
    mScaleLen  = mScaleBer  = mScaleCln = mScaleBackLen = mScaleBackBer = mScaleBackCln = 1.0f;
    // mUnitLeft  = mUnitRight  = mUnitUp  = mUnitDown  = 1.0f;
    // mScaleLeft = mScaleRight = mScaleUp = mScaleDown = 1.0f;
    // mZeroLeft  = mZeroRight  = mZeroUp  = mZeroDown  = 0.0f;
  }
}
