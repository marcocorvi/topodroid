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

import com.topodroid.util.TDLog;
import com.topodroid.types.ExtendType;

import java.util.ArrayList;

class ParserTherionState extends ParserState
{
  ParserTherionState mParent;

  String mSurveyPath; // survey pathname
  ArrayList<String> mTeam; // shared among the top parser and the children

  boolean in_centerline;
  boolean in_data;
  boolean in_survey;
  boolean in_map;
  boolean in_surface;
  boolean in_scrap;
  boolean in_line;
  boolean in_area;
  boolean in_grade;
  boolean in_comment;
  boolean in_scan;
  // boolean in_revise;

  int mExtend;
  String mPrefix;
  String mSuffix;
  int mMark = 0;

  int min_value_len;
  boolean mClinoPercent = false;

  int jFrom        =  0;
  int jTo          =  1;
  int jStation     =  1;
  int jLength      =  2;
  int jCompass     =  3;
  int jClino       =  4;
  int jBackLength  = -1;
  int jBackCompass = -1;
  int jBackClino   = -1;
  int jLeft        = -1;
  int jRight       = -1;
  int jUp          = -1;
  int jDown        = -1;
  int jDepth       = -1;
  int jFromdepth   = -1;
  int jTodepth     = -1;
  int jDepthChange = -1;
  int jCounter     = -1;
  int jFromCount   = -1;
  int jToCount     = -1;
  int jNorthing    = -1;
  int jEasting     = -1;
  int jAltitude    = -1;

  ParserTherionState( String survey_path )
  { 
    super();
    mParent = null;
    mSurveyPath = survey_path;
    mTeam = new ArrayList<>();

    mExtend = ExtendType.EXTEND_RIGHT;
    mPrefix = "";
    mSuffix = "";
    mMark   = 0;
    mClinoPercent = false;

    in_centerline = false;
    in_data = false;
    in_survey = false;
    in_map = false;
    in_surface = false;
    in_scrap = false;
    in_line  = false;
    in_area  = false;
    in_grade = false;
    in_comment = false;
    in_scan    = false;
    // in_revise  = false;
    
    min_value_len = 0;
  }

  ParserTherionState( ParserTherionState state, String survey_name )
  {
    super( state );
    mParent  = state;
    if ( mSurveyPath == null ) {
      mSurveyPath = "";
    } else {
      if ( survey_name == null ) {
        mSurveyPath = state.mSurveyPath;
      } else {
        mSurveyPath = state.mSurveyPath.isEmpty()? survey_name : survey_name + "." + state.mSurveyPath;
      }
    }

    mExtend = state.mExtend;
    mPrefix = state.mPrefix;
    mSuffix = state.mSuffix;
    mMark   = state.mMark;
    mClinoPercent = state.mClinoPercent;
    mTeam    = state.mTeam; // child share mteam with parent
    mCSorder = state.mCSorder;

    in_centerline = state.in_centerline;
    in_data    = state.in_data;
    in_survey  = state.in_survey;
    in_map     = state.in_map;
    in_surface = state.in_surface;
    in_scrap   = state.in_scrap;
    in_line    = state.in_line;
    in_area    = state.in_area;
    in_grade   = state.in_grade;
    in_comment = state.in_comment;
    in_scan    = state.in_scan;
    // in_revise  = state.in_revise;
    min_value_len = state.min_value_len;

    jFrom        = state.jFrom;
    jTo          = state.jTo;
    jStation     = state.jStation;
    jLength      = state.jLength;
    jCompass     = state.jCompass;
    jClino       = state.jClino;
    jBackLength  = state.jBackLength;
    jBackCompass = state.jBackCompass;
    jBackClino   = state.jBackClino;
    jLeft        = state.jLeft;
    jRight       = state.jRight;
    jUp          = state.jUp;
    jDown        = state.jDown;
    jDepth       = state.jDepth;
    jFromdepth   = state.jFromdepth;
    jTodepth     = state.jTodepth;
    jDepthChange = state.jDepthChange;
    jCounter     = state.jCounter;
    jFromCount   = state.jFromCount;
    jToCount     = state.jToCount;
    jNorthing    = state.jNorthing;
    jEasting     = state.jEasting;
    jAltitude    = state.jAltitude;
  }

  void resetJindices()
  {
    jFrom = jTo = jStation = jLength = jCompass = jClino = -1;
    jBackLength = jBackCompass = jBackClino = -1;
    jLeft = jUp = jRight  = jDown = -1;
    jDepth = jTodepth = jFromdepth = jDepthChange = -1;
    jCounter = jFromCount = jToCount = jNorthing = jEasting = jAltitude = -1;
  }

  boolean setJindex( String val, int j0 )
  {
    if ( val.equals("from") ) {
      jFrom = j0;
    } else if ( val.equals("to") ) {
      jTo = j0;
    } else if ( val.equals("station") ) {
      jStation = j0;
    } else if ( val.equals("length") || val.equals("tape") ) {
      jLength = j0;
    } else if ( val.equals("compass") || val.equals("bearing") ) {
      jCompass = j0;
    } else if ( val.equals("clino") || val.equals("gradient") ) {
      jClino = j0;
    } else if ( val.equals("backlength") || val.equals("backtape") ) {
      jBackLength = j0;
    } else if ( val.equals("backcompass") || val.equals("backbearing") ) {
      jBackCompass = j0;
    } else if ( val.equals("backclino") || val.equals("backgradient") ) {
      jBackClino = j0;
    } else if ( val.equals("left") ) {
      jLeft  = j0;
    } else if ( val.equals("right") ) {
      jRight = j0;
    } else if ( val.equals("up") || val.equals("ceiling") ) {
      jUp    = j0;
    } else if ( val.equals("down") || val.equals("floor") ) {
      jDown  = j0;
    } else if ( val.equals("depth") ) {
      jDepth = j0;
    } else if ( val.equals("todepth") ) {
      jTodepth = j0;
    } else if ( val.equals("fromdepth") ) {
      jFromdepth = j0;
    } else if ( val.equals("depthchange") ) {
      jDepthChange = j0;
    } else if ( val.equals("counter") ) {
      jCounter = j0;
    } else if ( val.equals("fromcount") ) {
      jFromCount = j0;
    } else if ( val.equals("tocount") ) {
      jToCount = j0;
    } else if ( val.equals("northing") ) {
      jNorthing = j0;
    } else if ( val.equals("easting") ) {
      jEasting = j0;
    } else if ( val.equals("altitude") ) {
      jAltitude = j0;
    } else if ( val.equals("ignore") ) {
      // nothings
    // } else if ( val.equals("ignoreall") ) { // handled by caller
    //   return true;
    } else {
      return false; // should set partial = true
    }
    return true;
  }

  void checkStateInData()
  {
    in_data = false;
    switch ( data_type ) {
      case ParserUtil.DATA_NORMAL:
        in_data = (jFrom >= 0) && (jTo >= 0) 
          && ( jLength >= 0 || jBackLength >= 0 )
          && ( jCompass >= 0 || jBackCompass >= 0 )
          && ( jClino >= 0 || jBackClino >= 0 );
        break;
      case ParserUtil.DATA_TOPOFIL:
        in_data = (jFrom >= 0) && (jTo >= 0) 
          && ( jLength >= 0 || jBackLength >= 0 )
          && ( jCompass >= 0 || jBackCompass >= 0 )
          && ( jClino >= 0 || jBackClino >= 0 );
        break;
      case ParserUtil.DATA_DIVING:
        in_data = (jFrom >= 0) && (jTo >= 0) 
          && ( jLength >= 0 || jBackLength >= 0 )
          && ( jCompass >= 0 || jBackCompass >= 0 )
          && ( jDepth >= 0 || jTodepth >= 0 || jFromdepth >= 0 || jDepthChange >= 0 );
        break;
      case ParserUtil.DATA_CARTESIAN:
        in_data = (jFrom >= 0) && (jTo >= 0) 
          && ( jNorthing >= 0 ) && ( jEasting >= 0 ) && ( jAltitude >= 0 );
        break;
      case ParserUtil.DATA_CYLPOLAR:
        in_data = (jFrom >= 0) && (jTo >= 0) 
          && ( jLength >= 0 ) // horizoltan length
          && ( jCompass >= 0 )
          && ( jDepth >= 0 ); // vertical
        break;
      case ParserUtil.DATA_DIMENSION:
        in_data = (jStation >= 0) && ( jLeft >= 0 || jRight >= 0 || jUp >= 0 || jDown >= 0 );
        break;
      case ParserUtil.DATA_NOSURVEY:
        in_data = true;
    }
    // TODO other style syntax
  }

  void setDataType( String val )
  {
    if ( val.equals("normal") ) {
      data_type = ParserUtil.DATA_NORMAL;
    } else if ( val.equals("topofil") ) {
      data_type = ParserUtil.DATA_TOPOFIL;
    } else if ( val.equals("diving") ) {
      data_type = ParserUtil.DATA_DIVING;
    } else if ( val.equals("cartesian") ) {
      data_type = ParserUtil.DATA_CARTESIAN;
    } else if ( val.equals("cylpolar") ) {
      data_type = ParserUtil.DATA_CYLPOLAR;
    } else if ( val.equals("dimensions") ) {
      data_type = ParserUtil.DATA_DIMENSION;
    } else if ( val.equals("nosurvey") ) {
      data_type = ParserUtil.DATA_NOSURVEY;
    } else {
      data_type = ParserUtil.DATA_NONE;
    }
  }

  /** @return final j index (negative on failure: partial parsing - 0 is failure)
   * @param j   initial index of input string array
   * @param vals input string array
   *
   */
  int handleDeclination( int j, String[] vals )
  {
    int n = vals.length;
    if ( j >= n ) return 0;
    if ( vals[j].equals("[]") || vals[j].equals("-") ) { // declination reset - is "-" old syntax ?
      mDeclination = ( mParent == null )? 0 : mParent.mDeclination;
      return j;
    }
    java.util.ArrayList<String> toks = new java.util.ArrayList<>();
    int k = j;
    if ( vals[j].startsWith("[") ) {          // [value units] or [date value ... units]
      for ( ; k < n; ++k ) {
        String t = vals[k].replace("[", "").replace("]", "");
        if ( t.length() > 0 ) toks.add( t );
        if ( vals[k].endsWith("]") ) break;
      }
      if ( k == n ) return -(n-1);            // missing "]"
    } else {                                  // plain: value [units]
      toks.add( vals[k] );
      if ( k+1 < n && ! vals[k+1].startsWith("-") ) { ++k; toks.add( vals[k] ); }
    }
    if ( toks.isEmpty() ) {                   // "[ ]" is a reset too
      mDeclination = ( mParent == null )? 0 : mParent.mDeclination;
      return k;
    }
    String units = null;
    int m = toks.size();
    if ( Float.isNaN( ParserUtil.parseFloat( toks.get(m-1), Float.NaN ) ) ) { units = toks.remove(m-1); --m; }
    if ( m == 0 ) return -k;
    try {
      float value = Float.parseFloat( toks.get( m == 1 ? 0 : 1 ) ); // dated form: first value
      mDeclination = ( units == null )? value : value * ParserUtil.parseAngleUnit( units );
    } catch ( NumberFormatException e ) {
      return -k;
    }
    return ( m == 1 )? k : -k;                // several dates are approximated: flag partial
  }

  /** add the team name to the mTeam array - team names can be single string or strings in double quotes
   * @param vals strings array
   * @param k    start index in the strings array
   */
  void concatTeam( String[] vals, int k )
  {
    int n = vals.length;
    while ( k < n && vals[k].isEmpty() ) ++k;
    if ( k >= n ) return;
    String name;
    if ( vals[k].startsWith("\"") ) {
      StringBuilder sb = new StringBuilder();
      for ( int j = k; j < n; ++j ) {
        if ( sb.length() > 0 ) sb.append(' ');
        sb.append( vals[j] );
        if ( vals[j].endsWith("\"") && ( j > k || vals[j].length() > 1 ) ) break;
      }
      String s = sb.toString();
      name = s.substring( 1, ( s.length() > 1 && s.endsWith("\"") )? s.length()-1 : s.length() );
    } else {
      name = vals[k];   // unquoted: first token only
    }
    name = name.replace('/', ' ').trim();
    if ( name.length() > 0 && ! mTeam.contains( name ) ) mTeam.add( name );
  }
    

}
