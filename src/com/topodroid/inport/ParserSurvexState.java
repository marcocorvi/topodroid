/* @file ParserSurvexState.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid state for the Survex parser
 *
 * The state is scoped by *begin/*end: a nested survey starts with a copy of the
 * enclosing state, and *end restores the enclosing one.
 * Besides calibrations and units, the scoped state includes
 *   - the survey prefix (hierarchical station naming)
 *   - the station aliases
 *   - the data style and column ordering
 *   - the case mode, the decimal and separator characters
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.util.TDLog;
import java.util.HashMap;
import java.util.Locale;

class ParserSurvexState extends ParserState
{
  final static int CASE_LOWER    = 0; // Survex default
  final static int CASE_UPPER    = 1;
  final static int CASE_PRESERVE = 2;

  boolean mFileRoot = false;

  ParserSurvexState mParent;
  String mDirpath = null; // dirpath: A case-insensitive fallback (list mDirpath and equalsIgnoreCase) helps with Windows-authored surveys on Android’s case-sensitive filesystem.
  String mName;           // *begin name (null for an anonymous *begin)
  String mPrefix = null;  // full survey path, without trailing separator ("" at the root)
  String mSep    = ".";   // level separator (*set separator)
  int mCaseMode  = CASE_LOWER;
  String mDecimalSep = null; // decimal separator, null for "."
  boolean interleaved;   // interleaved data
  boolean ignoreData;    // *data ignore
  HashMap< String, String > mAliases; // station aliases (alias --> target), scoped

  // data ordering: column index of each reading, -1 if not present
  int jFrom, jTo, jStation, jDirection;
  int jLength, jBackLength, jCompass, jBackCompass, jClino, jBackClino;
  int jLeft, jRight, jUp, jDown;
  int mJmin;   // max column index used by the normal style
  int mJmin2;  // max column index used by the passage style

  /** root state
   * @param name  name of the root ("." by convention)
   */
  ParserSurvexState( String prefix, String name )
  {
    super();
    mParent = null;
    mName   = name;
    mPrefix = prefix;
    mAliases = new HashMap<>();
    setDataDefault();
  }

  /** nested state, a copy of the enclosing state
   * @param state  enclosing state
   * @param name   survey name, null or empty for an anonymous *begin
   */
  ParserSurvexState( ParserSurvexState state, String name )
  {
    super( state );
    mParent  = state;
    mDirpath = state.mDirpath;
    mName    = name;
    mSep     = state.mSep;
    if ( name == null || name.length() == 0 ) { // no chnage
      mPrefix = state.mPrefix;
    } else if ( state.mPrefix == null ) {       // null becomes empty
      mPrefix = "";
    } else if ( state.mPrefix.length() == 0 ) { // empty becomes name
      mPrefix = name;
    } else {
      mPrefix = state.join( name );       // append name
    }
    // mPrefix  = ( name == null || name.length() == 0 )? state.mPrefix : state.join( name );
    mCaseMode = state.mCaseMode;
    mDecimalSep = state.mDecimalSep;
    interleaved = state.interleaved;
    ignoreData  = state.ignoreData;
    mAliases = new HashMap<>( state.mAliases );
    jFrom = state.jFrom; jTo = state.jTo; jStation = state.jStation; jDirection = state.jDirection;
    jLength = state.jLength; jBackLength = state.jBackLength;
    jCompass = state.jCompass; jBackCompass = state.jBackCompass;
    jClino = state.jClino; jBackClino = state.jBackClino;
    jLeft = state.jLeft; jRight = state.jRight; jUp = state.jUp; jDown = state.jDown;
    mJmin = state.mJmin; mJmin2 = state.mJmin2;
  }

  /** *data default: normal style, ordering "from to tape compass clino"
   */
  void setDataDefault()
  {
    data_type   = ParserUtil.DATA_DEFAULT;
    interleaved = false;
    ignoreData  = false;
    clearIndices();
    jFrom    = 0;
    jTo      = 1;
    jLength  = 2;
    jCompass = 3;
    jClino   = 4;
    mJmin    = 4;
  }

  /** clear the data ordering
   */
  void clearIndices()
  {
    jFrom = jTo = jStation = jDirection = -1;
    jLength = jBackLength = jCompass = jBackCompass = jClino = jBackClino = -1;
    jLeft = jRight = jUp = jDown = -1;
    mJmin = mJmin2 = -1;
  }

  /** apply the case mode to a name
   */
  String fold( String name )
  {
    switch ( mCaseMode ) {
      case CASE_UPPER:    return name.toUpperCase( Locale.ROOT );
      case CASE_PRESERVE: return name;
      default:            return name.toLowerCase( Locale.ROOT );
    }
  }

  /** @return the prefix extended by a name
   */
  String join( String name )
  {
    return ( mPrefix == null || mPrefix.length() == 0 )? name : mPrefix + mSep + name;
  }

  /** @return the full (hierarchical) name of a station written in this survey
   */
  String fullName( String name )
  {
    return join( name );
  }

  /** *prefix: add a level to the current survey path
   */
  void addPrefix( String name )
  {
    if ( mPrefix == null || mPrefix.length() == 0 ) { 
      mPrefix = name;
    } else {
      mPrefix = join( name );
    }
  }
}
