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
package com.topodroid.inport;

import com.topodroid.util.TDLog;

class ParserSurvexState extends ParserState
{
  ParserSurvexState mParent;
  String mName; // *begin name
  int mSurveyLevel;
  int mCase;
  boolean interleaved; // interleaved data

  // void dump( String msg )
  // {
  //   TDLog.v( msg + " state: " + mName + " level " + mSurveyLevel + " units " + mUnitLen + " " + mUnitBer + "/" + mScaleBer + "/" + mZeroBer + " " + mUnitCln + "/" + mScaleCln + "/" + mZeroCln );
  //   TDLog.v( "     " + mUnitBackBer + " " + mUnitBackCln );

  // }

  ParserSurvexState( String name )
  {
    super();
    mParent = null;
    mName   = name;
    setDataDefault();
    mCase = ParserUtil.CASE_LOWER;
  }

  void setDataDefault()
  {
    // data_type = 0; // DATA_DEFAULT
    interleaved = false;
  }

  ParserSurvexState( ParserSurvexState state, String name )
  {
    super( state );
    mParent  = state;
    mName    = name;
    interleaved = state.interleaved;
    mCase = state.mCase;
    // mSurveyLevel = state.mSurveyLevel + 1;
  }

}
