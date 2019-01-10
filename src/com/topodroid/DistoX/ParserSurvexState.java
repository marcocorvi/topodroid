/* @file ParserSurvexState.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid state for the Survex parser
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.DistoX;

class ParserSurvexState
{
  ParserSurvexState mParent;

  String mName; // *begin name

  // zero_error includes scale and units
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
  boolean mSplay;
  int mSurveyLevel;
  int mCase;
  boolean interleaved; // interleaved data

  int data_type;

  ParserSurvexState( String name )
  {
    mParent = null;
    mName   = name;
    setUnitsDefault();
    setCalibrateDefault();
    setDataDefault();

    mDeclination= 0.0f;
    mDuplicate = false;
    mSurface   = false;
    mSplay     = false;
    mSurveyLevel = 0;
    mCase = ParserUtil.CASE_LOWER;
  }

  void setDataDefault()
  {
    data_type = 0; // DATA_DEFAULT
    interleaved = false;
  }

  void setCalibrateDefault()
  {
    mZeroLen   = mZeroBer   = mZeroCln   = 0.0f;
    mScaleLen  = mScaleBer  = mScaleCln  = 1.0f;
    // mUnitLeft  = mUnitRight  = mUnitUp  = mUnitDown  = 1.0f;
    // mScaleLeft = mScaleRight = mScaleUp = mScaleDown = 1.0f;
    // mZeroLeft  = mZeroRight  = mZeroUp  = mZeroDown  = 0.0f;
  }

  void setUnitsDefault()
  {
    mUnitLen   = mUnitBer   = mUnitCln   = 1.0f;
  }

  ParserSurvexState( ParserSurvexState state, String name )
  {
    mParent  = state;
    mName    = name;

    mUnitLen   = state.mUnitLen;
    mUnitBer   = state.mUnitBer;
    mUnitCln   = state.mUnitCln;
    mZeroLen   = state.mZeroLen;
    mZeroBer   = state.mZeroBer;
    mZeroCln   = state.mZeroCln;
    mScaleLen  = state.mScaleLen;
    mScaleBer  = state.mScaleBer;
    mScaleCln  = state.mScaleCln;

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
    data_type = state.data_type;
    interleaved = state.interleaved;

    mCase = state.mCase;
    mSurveyLevel = state.mSurveyLevel + 1;
  }

}
