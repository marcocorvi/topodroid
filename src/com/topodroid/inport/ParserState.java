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

import com.topodroid.TDX.DBlock; // FLAG_xxx

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
  float mUnitDepth, mScaleDepth, mZeroDepth;
  float mUnitLeft,  mScaleLeft,  mZeroLeft;
  float mUnitRight, mScaleRight, mZeroRight;
  float mUnitUp,    mScaleUp,    mZeroUp;
  float mUnitDown,  mScaleDown,  mZeroDown;

  float mDeclination = 0.0f;

  boolean mDuplicate   = false;
  boolean mSurface     = false;
  boolean mArtificial  = false;
  boolean mApproximate = false;
  boolean mSplay       = false;

  // long mShotFlag = 0;

  ParserState()
  {
    setDefault();
  }

  ParserState( ParserState state )
  {
    data_type = state.data_type;
    mSurveyLevel = state.mSurveyLevel + 1; 

    mUnitLen   = state.mUnitLen;
    mZeroLen   = state.mZeroLen;
    mScaleLen  = state.mScaleLen;

    mUnitBer   = state.mUnitBer;
    mZeroBer   = state.mZeroBer;
    mScaleBer  = state.mScaleBer;

    mUnitCln   = state.mUnitCln;
    mZeroCln   = state.mZeroCln;
    mScaleCln  = state.mScaleCln;

    mUnitBackLen  = state.mUnitBackLen;
    mZeroBackLen  = state.mZeroBackLen;
    mScaleBackLen = state.mScaleBackLen;

    mUnitBackBer   = state.mUnitBackBer;
    mZeroBackBer   = state.mZeroBackBer;
    mScaleBackBer  = state.mScaleBackBer;

    mUnitBackCln   = state.mUnitBackCln;
    mZeroBackCln   = state.mZeroBackCln;
    mScaleBackCln  = state.mScaleBackCln;

    mUnitDepth  = state.mUnitDepth;
    mScaleDepth = state.mScaleDepth;
    mZeroDepth  = state.mZeroDepth;

    mUnitLeft   = state.mUnitLeft;
    mScaleLeft  = state.mScaleLeft;
    mZeroLeft   = state.mZeroLeft;

    mUnitRight  = state.mUnitRight;
    mScaleRight = state.mScaleRight;
    mZeroRight  = state.mZeroRight;

    mUnitUp     = state.mUnitUp;
    mScaleUp    = state.mScaleUp;
    mZeroUp     = state.mZeroUp;

    mUnitDown   = state.mUnitDown;
    mScaleDown  = state.mScaleDown;
    mZeroDown   = state.mZeroDown;

    mDeclination = state.mDeclination;
    mDuplicate   = state.mDuplicate;
    mSurface     = state.mSurface;
    mArtificial  = state.mArtificial;
    mApproximate = state.mApproximate;
    mSplay       = state.mSplay;
    // mShotFlag    = state.mShotFlag;
  }

  protected void setDefault()
  {
    data_type = 0; // DATA_NONE as in TH
    mSurveyLevel = 0;
    // mShotFlag    = 0;
    setUnitsDefault();
    setCalibrateDefault();
    mDeclination= 0.0f;
    mDuplicate = false;
    mSurface   = false;
    mSplay     = false;
    mArtificial = false;
    mApproximate = false;
  }

  void setUnitsDefault()
  {
    mUnitLen = mUnitBer = mUnitCln = mUnitBackLen = mUnitBackBer = mUnitBackCln = mUnitDepth = 1.0f;
    mUnitLeft  = mUnitRight  = mUnitUp  = mUnitDown  = 1.0f;
  }


  void setCalibrateDefault()
  {
    mZeroLen   = mZeroBer   = mZeroCln  = mZeroBackLen  = mZeroBackBer  = mZeroBackCln  = mZeroDepth  = 0.0f;
    mScaleLen  = mScaleBer  = mScaleCln = mScaleBackLen = mScaleBackBer = mScaleBackCln = mScaleDepth = 1.0f;
    mScaleLeft = mScaleRight = mScaleUp = mScaleDown = 1.0f;
    mZeroLeft  = mZeroRight  = mZeroUp  = mZeroDown  = 0.0f;
  }

  // void setShotFlag()
  // {
  //   mShotFlag = 0;
  //   if ( mSurface )     mShotFlag != DBlock.FLAG_SURFACE;
  //   if ( mDuplicate )   mShotFlag != DBlock.FLAG_DUPLICATE;
  //   if ( mArtificial )  mShotFlag != DBlock.FLAG_ARTIFICIAL;
  //   if ( mApproximate ) mShotFlag |= DBlock.FLAG_APPROXIMATE;
  // }
}
