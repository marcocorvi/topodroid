/** @file TherionParserState.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid state for the Therion parser
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 * 20130104 created 
 */
package com.topodroid.DistoX;

public class TherionParserState
{
  boolean in_centerline;
  boolean in_data;
  boolean in_survey;
  boolean in_map;
  boolean in_surface;
  boolean in_scrap;
  boolean in_line;
  boolean in_area;

  float mUnitLen;
  float mUnitBer;
  float mUnitCln;
  float mZeroLen;
  float mZeroBer;
  float mZeroCln;
  float mScaleLen;
  float mScaleBer;
  float mScaleCln;
  float mDeclination;
  boolean mDuplicate;
  boolean mSurface;
  int mExtend;
  String mPrefix;
  String mSuffix;
  int mSurveyLevel;

  public TherionParserState()
  {
    mUnitLen = 1.0f;
    mUnitBer = 1.0f;
    mUnitCln = 1.0f;
    mZeroLen = 0.0f;
    mZeroBer = 0.0f;
    mZeroCln = 0.0f;
    mScaleLen = 1.0f;
    mScaleBer = 1.0f;
    mScaleCln = 1.0f;
    mDeclination= 0.0f;
    mDuplicate = false;
    mSurface   = false;
    mExtend = DistoXDBlock.EXTEND_RIGHT;
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
  }

  public TherionParserState( TherionParserState state )
  {
    mUnitLen = state.mUnitLen;
    mUnitBer = state.mUnitBer;
    mUnitCln = state.mUnitCln;
    mZeroLen = state.mZeroLen;
    mZeroBer = state.mZeroBer;
    mZeroCln = state.mZeroCln;
    mScaleLen = state.mScaleLen;
    mScaleBer = state.mScaleBer;
    mScaleCln = state.mScaleCln;
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
  }

}
