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
package com.topodroid.inport;

import com.topodroid.types.ExtendType;

class ParserTherionState extends ParserState
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
  boolean in_grade;


  int mExtend;
  String mPrefix;
  String mSuffix;

  int min_value_len;

  ParserTherionState()
  { 
    super();
    mParent = null;

    mExtend = ExtendType.EXTEND_RIGHT;
    mPrefix = "";
    mSuffix = "";

    in_centerline = false;
    in_data = false;
    in_survey = false;
    in_map = false;
    in_surface = false;
    in_scrap = false;
    in_line  = false;
    in_area  = false;
    in_grade = false;
    
    min_value_len = 0;
  }

  ParserTherionState( ParserTherionState state )
  {
    super( state );
    mParent  = state;

    mExtend = state.mExtend;
    mPrefix = state.mPrefix;
    mSuffix = state.mSuffix;
    // mSurveyLevel = state.mSurveyLevel;

    in_centerline = state.in_centerline;
    in_data = state.in_data;
    in_survey = state.in_survey;
    in_map = state.in_map;
    in_surface = state.in_surface;
    in_scrap = state.in_scrap;
    in_line = state.in_line;
    in_area = state.in_area;
    min_value_len = state.min_value_len;
  }

}
