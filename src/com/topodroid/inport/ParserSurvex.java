/* @file ParserSurvex.java
 *
 * @author marco corvi
 * @date jav 2019
 *
 * @brief TopoDroid Survex parser
 *
 * Station names are hierarchical: a station written in a survey (*begin ... *end)
 * is named with the path of the survey, "survey.subsurvey.station".
 * The parser sets the "partial" flag whenever something in the file could not
 * be imported faithfully.
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.util.TDMath;
import com.topodroid.util.TDLog;
import com.topodroid.util.TDio;
import com.topodroid.util.TDString;
import com.topodroid.util.TDUtil;
import com.topodroid.util.TDAnalytics;
import com.topodroid.types.ExtendType;
import com.topodroid.types.LegType;
import com.topodroid.TDX.TopoDroidApp;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

// units, calibrate supported quantities:
//   length tape backlength backtape bearing compass backbearing backcompass gradient clino backgradient backclino
//   left right up ceiling down floor declination (calibrate only)
// count, counter, depth, easting, northing, altitude, dx, dy, dz are accepted and ignored

class ParserSurvex extends ImportParser
{
  private static final int MAX_INCLUDE_DEPTH = 16; // max nr of include depth
  private int mIncludeDepth = 0;
  private final HashSet< String > mOpenFiles = new HashSet<>();


  private final static String SPLAY = "-"; // marker for splay (anonymous) station
  private final static Pattern WS      = Pattern.compile( "\\s+" );
  private final static Pattern BLANKS  = Pattern.compile( "[\\s,]+" ); // Survex default blank characters
  private final static Pattern NUMBER  = Pattern.compile( "[+-]?(\\d+\\.?\\d*|\\.\\d+)" );

  // quantities of *units and *calibrate
  private final static int Q_NONE = 0;
  private final static int Q_LEN  = 1;
  private final static int Q_BLEN = 2;
  private final static int Q_BER  = 3;
  private final static int Q_BBER = 4;
  private final static int Q_CLN  = 5;
  private final static int Q_BCLN = 6;
  private final static int Q_LEFT = 7;
  private final static int Q_RIGHT = 8;
  private final static int Q_UP   = 9;
  private final static int Q_DOWN = 10;
  private final static int Q_DECL = 11;
  private final static int Q_OTHER = 12; // valid quantity that is not used by the import

  /** fix station:
   * fix stations are supposed to be referred to the same coord system
   */
  class Fix
  {
    // private CS cs;
    String name;  // station name
    float e, n, z; // north east, vertical (upwards)

    Fix( String nm, float e0, float n0, float z0 )
    {
      name = nm;
      e = e0;
      n = n0;
      z = z0;
    }
  }

  private ArrayList< Fix > fixes;
  // private LinkedHashSet< String > stations; // not used

  // parser status
  // private ParserSurvexState mState; // current state
  private boolean mWarn = false;    // partial flag
  private int mLineNr;              // debug line number
  // interleaved traverse
  private boolean mInStation;       // true if the next line is a station line
  private String  mFrom;            // last station
  private boolean mLegOk;           // true if the last reading line is valid
  private boolean mReversed;        // true if the leg is a backsight
  private float mLen, mBer, mCln;   // last reading

  ArrayList< ParserShot > getShots()    { return shots; }
  ArrayList< ParserShot > getSplays()   { return splays; }
  // ArrayList< String >     getStations() { return new ArrayList<String>( stations ); }
  ArrayList< Fix >        getFixes()    { return fixes; }
  private boolean mWithLrud;
  private String mExternSurvey;

  // same as in ImportParser.java
  String initStation()
  {
    for ( ParserShot sh : shots ) {
      if ( sh.from != null && sh.from.length() > 0 ) return sh.from;
    }
    return TDString.ZERO;
  }

  // /** insert a station name in the linked has set
  //  * @param station   station name
  //  * called on insert LegShot insert Fix insert Equate and insert Passage
  //  */
  // private void insertStation( String station )
  // {
  //   if ( station == null || station.isEmpty() || station.equals("-") ) return;
  //   stations.add( station ); // duplicate are ignored
  // }

  float surveyDeclination( ) { return mDeclination; }
  // ---------------------------------------------------------

  /**
   * @param isr           input stream reader (closed by the caller)
   * @param filename      filename of the input
   * @param apply_decl    whether to apply the magn. declination
   * @param with_lrud     whether to import LRUD (as splay)
   * @param extern_survey emclosing survey, if empty no enclosing survey, if null strip top survey from station names
   * @param filepath      input filepath (cannot be null nor empty)
   */
  ParserSurvex( InputStreamReader isr, String filename, boolean apply_decl, boolean with_lrud, String extern_survey, String filepath ) throws ParserException
  {
    super( apply_decl );
    TopoDroidApp.updateAnalytic( TDAnalytics.IMPORT_SVX );
    if ( mApplyDeclination ) mDeclination = 0.0f;
    // mName     = TDio.extractName( filename );
    mWithLrud = with_lrud;
    mExternSurvey = extern_survey;
    fixes    = new ArrayList<>();
    // stations = new LinkedHashSet<>();
    if ( filepath != null ) addFile( new File(filepath) );
    mPartial = readFile( isr, filename, null, filepath );
    checkValid();
    if ( mApplyDeclination ) mDeclination = 0.0f;
  }

  /** log a problem and set the partial flag
   */
  private void warn( String msg )
  {
    TDLog.v( "SVX " + mLineNr + " " + msg );
    mWarn = true;
  }

  /** end the current traverse (interleaved data)
   */
  private void resetTraverse()
  {
    mFrom      = null;
    mInStation = true;
    mLegOk     = false;
    mReversed  = false;
    mLen = mBer = mCln = Float.NaN;
  }

  private ParserSurvexState readLines( BufferedReader br, ParserSurvexState st ) throws IOException, ParserException
  {
    String line = nextLine( br );
    while ( line != null ) {
      ++mLineNr;
      line = line.trim();
      if ( line.length() == 0 ) { // a blank line ends the current traverse (interleaved data)
        resetTraverse();
      } else {
        int pos = line.indexOf( ';' );
        if ( pos >= 0 ) { // a comment-only line is not a blank line
          line = line.substring( 0, pos ).trim();
        }
        if ( line.length() > 0 ) {
          if ( line.charAt(0) == '*' ) {
            String[] vals = WS.split( line );
            st = command( vals, vals.length, line, st );
          } else if ( ! st.ignoreData ) {
            String[] vals;
            if ( line.indexOf(',') >= 0 && ! ",".equals( st.mDecimalSep ) ) { // comma is a blank
              line = line.replaceFirst( "^[\\s,]+", "" );
              vals = BLANKS.split( line );
            } else {
              vals = WS.split( line );
            }
            if ( line.length() > 0 ) dataLine( vals, vals.length, line, st );
          }
        }
      }
      line = nextLine( br );
    }
    return st;
  }

  /** read input file
   * @param filename name of the file to parse
   * @param state    root state of the parser
   * @return true if the import is partial
   */
  private boolean readFile( InputStreamReader isr, String filename, ParserSurvexState state, String filepath ) throws ParserException
  {
    ParserSurvexState st = ( state != null )? new ParserSurvexState( state, null ) // null survey-name = preserve prefix
                         : new ParserSurvexState( mExternSurvey, null );           // root of the linked list of states
    st.mFileRoot = true;
    st.mDirpath = "./";
    if ( filepath != null ) {
      int i = filepath.lastIndexOf('/');
      if ( i >= 0 ) st.mDirpath = filepath.substring(0, i+1);
    }
    mLineNr = 0;
    resetTraverse();
    BufferedReader br = null;
    try {
      br = TDio.getBufferedReader( isr, filename );
      ParserSurvexState last = readLines( br, st );
      if ( last != st ) {
        TDLog.e( "SVX " + filename + " unclosed *begin" );
        mWarn = true;
      }
    } catch ( IOException e ) {
      throw new ParserException();
    } finally {
      if ( br != null ) { try { br.close(); } catch ( IOException e ) { } }
    }
    if ( state == null ) {  // root file only
      if ( mDate == null )  mDate = TDUtil.currentDate();
      if ( mTitle == null ) mTitle = TDString.EMPTY;
    }
    return mWarn;
  }

  // ---------------------------------------------------------------------
  // COMMANDS

  /** handle a command
   * @param vals  line tokens (first is the command)
   * @param n     number of tokens (at least 1)
   * @param line  line
   */
  private ParserSurvexState command( String[] vals, int n, String line, ParserSurvexState st ) throws ParserException
  {
    String cmd = vals[0].substring(1).toLowerCase( Locale.ROOT );
    if ( cmd.equals("begin") ) { // *begin [<survey>]
      st = new ParserSurvexState( st, ( n > 1 )? st.fold( vals[1] ) : null );
      resetTraverse();
    } else if ( cmd.equals("end") ) { // *end [<survey>]
      if ( st.mParent == null || st.mFileRoot ) {
        warn( "*end without *begin" );
      } else {
        if ( n > 1 && st.mName != null && ! st.mName.equals( st.fold( vals[1] ) ) ) { warn( "*end " + vals[1] + " does not match *begin " + st.mName ); }
        st = st.mParent;
      }
      resetTraverse();
    } else if ( cmd.equals("prefix") ) { // deprecated: *prefix <survey>
      if ( n > 1 ) st.addPrefix( st.fold( vals[1] ) ); else warn( "*prefix without survey" );
    } else if ( cmd.equals("export") ) {
      // stations have always their full name: nothing to do
    } else if ( cmd.equals("solve") || cmd.equals("copyright") || cmd.equals("infer") || cmd.equals("sd") ) {
      // ignored
    } else if ( cmd.equals("include") ) {
      int save_line_nr = mLineNr;
      cmdInclude( vals, n, st );
      mLineNr = save_line_nr;
    } else if ( cmd.equals("entrance") || cmd.equals("instrument") || cmd.equals("ref") || cmd.equals("require") || cmd.equals("truncate") ) {
      warn( "ignore " + cmd );
    } else if ( cmd.equals("alias") ) {
      cmdAlias( vals, n, st );
    } else if ( cmd.equals("case") ) {
      cmdCase( vals, n, st );
    } else if ( cmd.equals("set") ) {
      cmdSet( vals, n, st );
    } else if ( cmd.equals("default") ) {
      cmdDefault( vals, n, st );
    } else if ( cmd.equals("title") ) {
      cmdTitle( vals, n, st );
    } else if ( cmd.equals("date") ) {
      cmdDate( vals, n, st );
    } else if ( cmd.equals("team") ) {
      cmdTeam( vals, n, st );
    } else if ( cmd.equals("calibrate") ) {
      cmdCalibrate( vals, n, st );
    } else if ( cmd.equals("units") ) {
      cmdUnits( vals, n, st );
    } else if ( cmd.equals("declination") ) {
      cmdDeclination( vals, n, st );
    } else if ( cmd.equals("flags") ) {
      cmdFlags( vals, n, st );
    } else if ( cmd.equals("cs") ) {
      cmdCs( vals, n, st );
    } else if ( cmd.equals("fix") ) {
      cmdFix( vals, n, st );
    } else if ( cmd.equals("equate") ) {
      cmdEquate( vals, n, st );
    } else if ( cmd.equals("data") ) {
      cmdData( vals, n, st );
    } else {
      warn( "unknown cmd " + cmd );
    }
    return st;
  }

  /** *alias station <alias> [<target>]
   * @param vals     command tokens - vals[0] is *alias
   * @param n        number of tokens
   * @param st       parser state
   * @note splay endpoint strings are not aliased because they are all mapped to "-"
   */
  private void cmdAlias( String[] vals, int n, ParserSurvexState st )
  {
    if ( n >= 3 && vals[1].equalsIgnoreCase("station") ) {
      if ( isAnonymous( st, vals[2] ) ) return;
      String alias = st.fold( vals[2] );
      if ( n == 3 ) {
        st.mAliases.remove( alias );
      } else {
        if ( isAnonymous( st, vals[3] ) ) return;
        st.mAliases.put( alias, st.fold( vals[3] ) );
      }
    } else {
      warn( "unsupported alias" );
    }
  }

  /** *case preserve|toupper|tolower
   */
  private void cmdCase( String[] vals, int n, ParserSurvexState st )
  {
    String what = ( n > 1 )? vals[1].toLowerCase( Locale.ROOT ) : "";
    if ( what.equals("preserve") ) {
      st.mCaseMode = ParserSurvexState.CASE_PRESERVE;
    } else if ( what.equals("toupper") ) {
      st.mCaseMode = ParserSurvexState.CASE_UPPER;
    } else if ( what.equals("tolower") ) {
      st.mCaseMode = ParserSurvexState.CASE_LOWER;
    } else {
      warn( "unsupported case " + what );
    }
  }

  /** *set <item> <characters>
   */
  private void cmdSet( String[] vals, int n, ParserSurvexState st )
  {
    if ( n < 3 ) { warn( "*set without value" ); return; }
    String item = vals[1].toLowerCase( Locale.ROOT );
    if ( item.equals("decimal") ) {
      st.mDecimalSep = vals[2].equals(".")? null : vals[2];
    } else if ( item.equals("separator") ) {
      st.mSep = vals[2];
    } else if ( item.equals("blank") || item.equals("names") ) {
      // fields are split on blanks, and names are not validated
    } else {
      warn( "unsupported *set " + item );
    }
  }

  /** *default calibrate|data|units|all
   */
  private void cmdDefault( String[] vals, int n, ParserSurvexState st )
  {
    String what = ( n > 1 )? vals[1].toLowerCase( Locale.ROOT ) : "";
    if ( what.equals("calibrate") ) {
      st.setCalibrateDefault();
    } else if ( what.equals("data") ) {
      st.setDataDefault();
      resetTraverse();
    } else if ( what.equals("units") ) {
      st.setUnitsDefault();
    } else if ( what.equals("all") ) {
      st.setCalibrateDefault();
      st.setUnitsDefault();
      st.setDataDefault();
      resetTraverse();
    } else {
      warn( "unsupported default " + what );
    }
  }

  private static String join( String[] vals, int from, int to )
  {
    StringBuilder sb = new StringBuilder();
    for ( int k = from; k < to; ++k ) {
      if ( vals[k].length() == 0 ) continue;
      if ( sb.length() > 0 ) sb.append(" ");
      sb.append( vals[k] );
    }
    return sb.toString();
  }

  private void cmdTitle( String[] vals, int n, ParserSurvexState st )
  {
    if ( mTitle != null || n < 2 ) return;
    String t = join( vals, 1, n );
    if ( t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"") ) t = t.substring( 1, t.length() - 1 );
    mTitle = t;
  }

  /** *date [explored] [surveyed] <date> ...
   * only the surveyed date is used (a date without type is a surveyed date)
   */
  private void cmdDate( String[] vals, int n, ParserSurvexState st )
  {
    if ( mDate != null ) return; // do not overwrite the date if this has been set
    boolean explored = false;
    boolean surveyed = false;
    for ( int k = 1; k < n; ++k ) {
      if ( vals[k].equalsIgnoreCase("explored") ) explored = true;
      else if ( vals[k].equalsIgnoreCase("surveyed") ) surveyed = true;
    }
    if ( explored && ! surveyed ) return;
    for ( int k = 1; k < n; ++k ) {
      if ( vals[k].length() > 0 && Character.isDigit( vals[k].charAt(0) ) ) {
        mDate = vals[k];
        TDLog.v("SVX " + mLineNr + " date " + mDate );
        return;
      }
    }
  }

  /** *team <person> [<role>...] : the person is a word or a double-quoted string, roles are ignored
   */
  private void cmdTeam( String[] vals, int n, ParserSurvexState st )
  {
    if ( n < 2 ) return;
    String rest = join( vals, 1, n );
    String name;
    if ( rest.startsWith("\"") ) {
      StringBuilder sb = new StringBuilder();
      int i = 1;
      while ( i < rest.length() ) {
        char ch = rest.charAt( i );
        if ( ch == '"' ) {
          if ( i + 1 < rest.length() && rest.charAt( i+1 ) == '"' ) { sb.append( '"' ); i += 2; continue; }
          break;
        }
        sb.append( ch );
        ++i;
      }
      name = sb.toString();
    } else {
      name = vals[1];
    }
    if ( name.length() == 0 ) return;
    mTeam = ( mTeam.length() > 0 )? mTeam + ", " + name : name;
  }

  // ---- quantities, units and calibrations

  private boolean isNumber( ParserSurvexState st, String s )
  {
    return NUMBER.matcher( replaceDecimalSep( st, s ) ).matches();
  }

  private static int quantity( String w )
  {
    w = w.toLowerCase( Locale.ROOT );
    if ( w.equals("length")  || w.equals("tape") )         return Q_LEN;
    if ( w.equals("backlength") || w.equals("backtape") )  return Q_BLEN;
    if ( w.equals("bearing") || w.equals("compass") )      return Q_BER;
    if ( w.equals("backbearing") || w.equals("backcompass") )  return Q_BBER;
    if ( w.equals("gradient") || w.equals("clino") )       return Q_CLN;
    if ( w.equals("backgradient") || w.equals("backclino") )   return Q_BCLN;
    if ( w.equals("left") )  return Q_LEFT;
    if ( w.equals("right") ) return Q_RIGHT;
    if ( w.equals("up")   || w.equals("ceiling") ) return Q_UP;
    if ( w.equals("down") || w.equals("floor") )   return Q_DOWN;
    if ( w.equals("declination") ) return Q_DECL;
    if ( w.equals("count") || w.equals("counter") || w.equals("depth")
      || w.equals("easting") || w.equals("northing") || w.equals("altitude")
      || w.equals("dx") || w.equals("dy") || w.equals("dz") ) return Q_OTHER;
    return Q_NONE;
  }

  private static boolean isLengthQ( int q ) { return q == Q_LEN || q == Q_BLEN || ( q >= Q_LEFT && q <= Q_DOWN ); }
  private static boolean isAngleQ( int q )  { return q >= Q_BER && q <= Q_BCLN; }

  private static float getUnit( ParserSurvexState st, int q )
  {
    switch ( q ) {
      case Q_LEN:   return st.mUnitLen;
      case Q_BLEN:  return st.mUnitBackLen;
      case Q_BER:   return st.mUnitBer;
      case Q_BBER:  return st.mUnitBackBer;
      case Q_CLN:   return st.mUnitCln;
      case Q_BCLN:  return st.mUnitBackCln;
      case Q_LEFT:  return st.mUnitLeft;
      case Q_RIGHT: return st.mUnitRight;
      case Q_UP:    return st.mUnitUp;
      case Q_DOWN:  return st.mUnitDown;
    }
    return 1.0f;
  }

  private static void setUnit( ParserSurvexState st, int q, float u )
  {
    switch ( q ) {
      case Q_LEN:   st.mUnitLen = u;     break;
      case Q_BLEN:  st.mUnitBackLen = u; break;
      case Q_BER:   st.mUnitBer = u;     break;
      case Q_BBER:  st.mUnitBackBer = u; break;
      case Q_CLN:   st.mUnitCln = u;     break;
      case Q_BCLN:  st.mUnitBackCln = u; break;
      case Q_LEFT:  st.mUnitLeft = u;    break;
      case Q_RIGHT: st.mUnitRight = u;   break;
      case Q_UP:    st.mUnitUp = u;      break;
      case Q_DOWN:  st.mUnitDown = u;    break;
    }
  }

  /** set the calibration: measure = reading * scale * unit - zero
   * @param zero  zero error already converted to the base unit
   */
  private static void setCalibration( ParserSurvexState st, int q, float zero, float scale )
  {
    switch ( q ) {
      case Q_LEN:   st.mZeroLen = zero;     st.mScaleLen = scale;     break;
      case Q_BLEN:  st.mZeroBackLen = zero; st.mScaleBackLen = scale; break;
      case Q_BER:   st.mZeroBer = zero;     st.mScaleBer = scale;     break;
      case Q_BBER:  st.mZeroBackBer = zero; st.mScaleBackBer = scale; break;
      case Q_CLN:   st.mZeroCln = zero;     st.mScaleCln = scale;     break;
      case Q_BCLN:  st.mZeroBackCln = zero; st.mScaleBackCln = scale; break;
      case Q_LEFT:  st.mZeroLeft = zero;    st.mScaleLeft = scale;    break;
      case Q_RIGHT: st.mZeroRight = zero;   st.mScaleRight = scale;   break;
      case Q_UP:    st.mZeroUp = zero;      st.mScaleUp = scale;      break;
      case Q_DOWN:  st.mZeroDown = zero;    st.mScaleDown = scale;    break;
    }
  }

  /** @return the conversion factor of a unit name, or 0 if the unit is not supported
   */
  private static float unitFactor( int q, String unit )
  {
    if ( isLengthQ( q ) ) {
      float u = ParserUtil.parseLengthUnit( unit );
      return ( u > 0 )? u : 0;
    }
    if ( isAngleQ( q ) || q == Q_DECL ) {
      String w = unit.toLowerCase( Locale.ROOT );
      if ( w.startsWith("percent") || w.equals("quadrants") ) return 0; // not a linear conversion
      float u = ParserUtil.parseAngleUnit( unit );
      return ( u > 0 )? u : 0;
    }
    return 1.0f;
  }

  /** *units <quantity list> [<factor>] <unit>
   *  *units default
   */
  private void cmdUnits( String[] vals, int n, ParserSurvexState st )
  {
    if ( n < 2 ) { warn( "*units without arguments" ); return; }
    if ( vals[1].equalsIgnoreCase("default") ) {
      st.setUnitsDefault();
      return;
    }
    if ( n < 3 ) { warn( "*units without unit" ); return; }
    String unit = vals[ n-1 ];
    float factor = 1.0f;
    int kEnd = n - 1; // end of the quantity list
    if ( n >= 4 && isNumber( st, vals[ n-2 ] ) ) {
      factor = ParserUtil.parseFloat( replaceDecimalSep( st, vals[ n-2 ] ), Float.NaN );
      kEnd = n - 2;
      if ( Float.isNaN( factor ) || factor == 0 ) { warn( "invalid units factor" ); return; }
    }
    for ( int k = 1; k < kEnd; ++k ) {
      int q = quantity( vals[k] );
      if ( q == Q_NONE ) {
        warn( "unknown quantity " + vals[k] );
      } else if ( q == Q_DECL ) {
        warn( "unsupported *units declination" );
      } else if ( q != Q_OTHER ) {
        float u = unitFactor( q, unit );
        if ( u > 0 ) {
          setUnit( st, q, u * factor );
        } else {
          warn( "unsupported unit " + unit );
        }
      }
    }
  }

  /** *calibrate <quantity list> <zero error> [<scale>]
   *  *calibrate <quantity list> <zero error> <units> [<scale>]
   *  *calibrate default
   *  first form:  measure = (reading - zero) * scale
   *  second form: measure = reading * scale - zero
   */
  private void cmdCalibrate( String[] vals, int n, ParserSurvexState st )
  {
    if ( n < 2 ) { warn( "*calibrate without arguments" ); return; }
    if ( vals[1].equalsIgnoreCase("default") ) {
      st.setCalibrateDefault();
      return;
    }
    int[] qs = new int[ n ];
    int nq = 0;
    int k = 1;
    for ( ; k < n && ! isNumber( st, vals[k] ); ++k ) {
      int q = quantity( vals[k] );
      if ( q == Q_NONE ) { warn( "unknown quantity " + vals[k] ); return; }
      qs[ nq ++ ] = q;
    }
    if ( nq == 0 || k >= n ) { warn( "invalid *calibrate" ); return; }
    float zero = ParserUtil.parseFloat( replaceDecimalSep( st, vals[k] ), Float.NaN );
    ++k;
    float scale = 1.0f;
    String zunit = null;
    int r = n - k; // number of remaining tokens
    if ( r == 1 ) {
      if ( isNumber( st, vals[k] ) ) {
        scale = ParserUtil.parseFloat( replaceDecimalSep( st, vals[k] ), Float.NaN );
      } else {
        zunit = vals[k];
      }
    } else if ( r == 2 ) {
      zunit = vals[k];
      scale = isNumber( st, vals[k+1] )? ParserUtil.parseFloat( replaceDecimalSep( st, vals[k+1] ), Float.NaN ) : Float.NaN;
    } else if ( r > 2 ) {
      warn( "invalid *calibrate" );
      return;
    }
    if ( Float.isNaN( zero ) || Float.isNaN( scale ) || scale == 0 ) { warn( "invalid *calibrate values" ); return; }

    for ( int j = 0; j < nq; ++j ) {
      int q = qs[j];
      if ( q == Q_OTHER ) continue;
      float zf = 1.0f; // conversion factor of the zero error units
      if ( zunit != null ) {
        zf = unitFactor( q, zunit );
        if ( zf <= 0 ) { warn( "unsupported unit " + zunit ); continue; }
      }
      if ( q == Q_DECL ) {
        // *calibrate declination has the opposite sign of *declination
        if ( zunit == null && scale != 1.0f ) { warn( "*calibrate declination with scale" ); continue; }
        st.mDeclination = - zero * zf;
        if ( ! mApplyDeclination ) mDeclination = st.mDeclination;
      } else if ( zunit == null ) {
        setCalibration( st, q, zero * scale * getUnit( st, q ), scale );
      } else {
        setCalibration( st, q, zero * zf, scale );
      }
    }
  }

  /** *declination auto [<x> <y> <z>]
   *  *declination <value> [<units>]
   */
  private void cmdDeclination( String[] vals, int n, ParserSurvexState st )
  {
    if ( n < 2 ) { warn( "*declination without value" ); return; }
    if ( vals[1].equalsIgnoreCase("auto") ) { // cannot be computed: the file is not imported faithfully
      warn( "declination auto" );
      st.mDeclination = ( st.mParent == null )? 0 : st.mParent.mDeclination;
    } else {
      float declination = ParserUtil.parseFloat( replaceDecimalSep( st, vals[1] ), Float.NaN );
      if ( Float.isNaN( declination ) ) { warn( "invalid declination" ); return; }
      if ( n > 2 ) {
        float u = unitFactor( Q_DECL, vals[2] );
        if ( u <= 0 ) { warn( "unsupported unit " + vals[2] ); return; }
        declination *= u;
      }
      st.mDeclination = declination;
    }
    if ( ! mApplyDeclination ) mDeclination = st.mDeclination;
  }

  /** *flags [not] duplicate|splay|surface ...
   */
  private void cmdFlags( String[] vals, int n, ParserSurvexState st )
  {
    for ( int k = 1; k < n; ++k ) {
      boolean val = true;
      if ( vals[k].equalsIgnoreCase("not") ) {
        val = false;
        ++k;
      }
      if ( k < n ) {
        String what = vals[k].toLowerCase( Locale.ROOT );
        if ( what.equals("duplicate") ) {
          st.mDuplicate = val;
        } else if ( what.equals("splay") ) {
          st.mSplay = val;
        } else if ( what.equals("surface") ) {
          st.mSurface = val;
        } else {
          warn( "unsupported flag " + what );
        }
      }
    }
  }

  /** *cs <coordinate system>  or  *cs out <coordinate system>
   * only the input coordinate system matters (it is the one of the *fix)
   */
  private void cmdCs( String[] vals, int n, ParserSurvexState st )
  {
    if ( n < 2 ) { warn( "*cs without coordinate system" ); return; }
    if ( vals[1].equalsIgnoreCase("out") ) return; // output coordinate system
    st.mCSorder = vals[1].equalsIgnoreCase("LONG-LAT")? ParserState.CS_LONG_LAT : ParserState.CS_NONE;
  }

  /** *fix <station> [reference] <x> <y> <z> [<errors>]
   * only fixes in LONG-LAT are imported, all others are discarded
   */
  private void cmdFix( String[] vals, int n, ParserSurvexState st )
  {
    if ( n < 2 ) { warn( "*fix without station" ); return; }
    if ( st.mCSorder != ParserState.CS_LONG_LAT ) { warn( "discarded fix, coordinate system is not LONG-LAT" ); return; }
    String name = stationName( st, vals[1] );
    if ( SPLAY.equals( name ) ) { warn( "fix of anonymous station" ); return; }
    // insertStation( name );
    int k = 2;
    if ( k < n && vals[k].equalsIgnoreCase("reference") ) ++k;
    if ( n - k < 3 ) { warn( "fix without coordinates" ); return; }
    // float x = ParserUtil.parseFloat( replaceDecimalSep( st, vals[k]   ), Float.NaN );
    // float y = ParserUtil.parseFloat( replaceDecimalSep( st, vals[k+1] ), Float.NaN );
    // float z = ParserUtil.parseFloat( replaceDecimalSep( st, vals[k+2] ), Float.NaN );
    float x = ParserUtil.parseDegrees( replaceDecimalSep( st, vals[k]   ), Float.NaN );   
    float y = ParserUtil.parseDegrees( replaceDecimalSep( st, vals[k+1] ), Float.NaN );
    float z = ParserUtil.parseFloat(   replaceDecimalSep( st, vals[k+2] ), Float.NaN ); 
    if ( Float.isNaN( x ) || Float.isNaN( y ) || Float.isNaN( z ) ) { warn( "invalid fix coordinates" ); return; }
    fixes.add( new Fix( name, x, y, z ) );
  }

  /** *equate <station> <station> ...
   */
  private void cmdEquate( String[] vals, int n, ParserSurvexState st )
  {
    if ( n < 3 ) { warn( "*equate with one station" ); return; }
    String f = stationName( st, vals[1] );
    for ( int j = 2; j < n; ++j ) {
      String t = stationName( st, vals[j] );
      if ( SPLAY.equals( f ) || SPLAY.equals( t ) ) { warn( "*equate of anonymous station" ); continue; }
      // insertStation( f );
      // insertStation( t );
      shots.add( new ParserShot( f, t, 0.0f, 0.0f, 0.0f, 0.0f, 0, LegType.NORMAL, true, false, false, "" ) );
    }
  }

  /** @return the file for a filepath
   * @param path filapath
   * Case-insensitive fallback: list mDirpath and compare with equalsIgnoreCase when the exact path isn’t found. (?)
   */
  private File resolveFile( String path )
  {
    File f = new File( path );
    if ( f.isFile() ) return f;
    f = new File( path + ".svx" );   // Survex adds the extension
    return f.isFile()? f : null;
  }
  
  /** @return the key, or null if the file is already open 
   * @param f     file
   */
  private String addFile( File f )
  {
    String key;
    try { key = f.getCanonicalPath(); } catch ( IOException e ) { key = f.getAbsolutePath(); }
    return mOpenFiles.add( key )? key : null;
  }

  /** *include <filename> 
   * @note 
   *   warn() prints only the line number, so a warning from an included file is hard to place. Adding a mFileName field, saved and restored the same way as mLineNr, fixes that.
   *   an include resets it at its start and doesn’t restore it. That only matters if an *include sits in the middle of an interleaved traverse, which is rare.
   */
  private void cmdInclude( String[] vals, int n, ParserSurvexState st ) throws ParserException
  { 
    if ( n < 2 ) { warn( "include with no filename" ); return; }
    String name = join( vals, 1, n );
    int len = name.length();
    if ( len > 1 ) {
      int start = name.startsWith("\"")? 1 : 0;
      int end   = name.endsWith("\"")? len -1 : len;
      name = name.substring( start, end );
    }
    name = name.replace( '\\', '/' );               // files written on Windows
    if ( name.length() == 0 ) { warn( "include with empty filename" ); return; }
    if ( mIncludeDepth >= MAX_INCLUDE_DEPTH ) { warn( "include nested too deep " + name ); return; }
    String path = name.startsWith("/")? name : st.mDirpath + name;
    File f = resolveFile( path );
    if ( f == null ) { warn( "include file not found " + path ); return; }
    String key = addFile( f );
    if ( key == null ) { warn( "recursive include " + name ); return; }
    ++mIncludeDepth;
    InputStreamReader isr1 = null;
    try {
      isr1 = new InputStreamReader( new FileInputStream( f ) );  // charset?
      readFile( isr1, name, st, f.getPath() );
    } catch ( IOException e ) {
      TDLog.e( "SVX parser cannot read input " + path + " " + e.getMessage() );
      mWarn = true;
    } finally {
      --mIncludeDepth;
      mOpenFiles.remove( key );
      if ( isr1 != null ) try { isr1.close(); } catch ( IOException ee ) { }
    }
  }

  /** *data <style> <ordering>
   *  *data            keep style and ordering, reset the traverse
   *  *data default
   *  *data ignore
   */
  private void cmdData( String[] vals, int n, ParserSurvexState st )
  {
    resetTraverse();
    st.ignoreData = false;
    if ( n < 2 ) return; // bare *data
    String what = vals[1].toLowerCase( Locale.ROOT );
    String[] o = new String[ n ]; // ordering, lower case
    for ( int k = 0; k < n; ++k ) o[k] = vals[k].toLowerCase( Locale.ROOT );

    if ( what.equals("normal") || what.equals("topofil") ) { // topofil is synonim of normal
      st.data_type = ParserUtil.DATA_NORMAL;
      st.interleaved = setJIndices( st, o, n );
      if ( ( ! st.interleaved && ( st.jFrom < 0 || st.jTo < 0 ) ) || ( st.jLength < 0 && st.jBackLength < 0 ) ) {
        warn( "unsupported ordering" );
      }
    } else if ( what.equals("passage") ) {
      st.data_type = ParserUtil.DATA_PASSAGE;
      st.interleaved = false;
      setJIndicesPassage( st, o, n );
      if ( st.jStation < 0 || st.jLeft >= 0 || st.jRight >= 0 ) warn( "passage left/right are not imported" );
    } else if ( what.equals("diving") ) {
      st.data_type = ParserUtil.DATA_DIVING;
      st.interleaved = false;
    } else if ( what.equals("cartesian") ) {
      st.data_type = ParserUtil.DATA_CARTESIAN;
      st.interleaved = false;
    } else if ( what.equals("cylpolar") ) {
      st.data_type = ParserUtil.DATA_CYLPOLAR;
      st.interleaved = false;
    } else if ( what.equals("nosurvey") ) {
      st.data_type = ParserUtil.DATA_NOSURVEY;
      st.interleaved = false;
    } else if ( what.equals("ignore") ) {
      st.ignoreData = true;
    } else if ( what.equals("default") ) {
      st.setDataDefault();
    } else {
      warn( "unknown data style " + what );
    }
  }

  /** set the data ordering of the normal style
   * @param o  lower case ordering words
   * @return true if the data are interleaved
   */
  private boolean setJIndices( ParserSurvexState st, String[] o, int n )
  {
    st.clearIndices();
    boolean interleaved = ( n > 2 && o[2].equals("station") );
    int j = 2;
    if ( interleaved ) { // station [noteall|ignoreall ...] newline <readings>
      while ( j < n && ! o[j].equals("newline") ) ++j;
      ++j;
    }
    int j0 = 0;
    for ( ; j < n; ++j, ++j0 ) {
      String w = o[j];
      if ( ! interleaved && w.equals("from") ) {
        st.jFrom = j0;
      } else if ( ! interleaved && w.equals("to") ) {
        st.jTo = j0;
      } else if ( interleaved && w.equals("direction") ) {
        st.jDirection = j0;
      } else if ( w.equals("length") || w.equals("tape") ) {
        st.jLength = j0;
      } else if ( w.equals("backlength") || w.equals("backtape") ) {
        st.jBackLength = j0;
      } else if ( w.equals("compass") || w.equals("bearing") ) {
        st.jCompass = j0;
      } else if ( w.equals("backcompass") || w.equals("backbearing") ) {
        st.jBackCompass = j0;
      } else if ( w.equals("clino") || w.equals("gradient") ) {
        st.jClino = j0;
      } else if ( w.equals("backclino") || w.equals("backgradient") ) {
        st.jBackClino = j0;
      } else if ( ! interleaved && w.equals("left") ) {
        st.jLeft = j0;
      } else if ( ! interleaved && w.equals("right") ) {
        st.jRight = j0;
      } else if ( ! interleaved && ( w.equals("up") || w.equals("ceiling") ) ) {
        st.jUp = j0;
      } else if ( ! interleaved && ( w.equals("down") || w.equals("floor") ) ) {
        st.jDown = j0;
      } else {
        continue; // unused column
      }
      st.mJmin = j0;
    }
    return interleaved;
  }

  /** set the data ordering of the passage style
   */
  private void setJIndicesPassage( ParserSurvexState st, String[] o, int n )
  {
    st.clearIndices();
    int j0 = 0;
    for ( int j = 2; j < n; ++j, ++j0 ) {
      String w = o[j];
      if ( w.equals("station") ) {
        st.jStation = j0;
      } else if ( w.equals("left") ) {
        st.jLeft = j0;
      } else if ( w.equals("right") ) {
        st.jRight = j0;
      } else if ( w.equals("up") || w.equals("ceiling") ) {
        st.jUp = j0;
      } else if ( w.equals("down") || w.equals("floor") ) {
        st.jDown = j0;
      } else {
        continue;
      }
      st.mJmin2 = j0;
    }
  }

  // ---------------------------------------------------------------------
  // DATA

  /** NONE and DEFAULT data styles are NOT treated as NORMAL
   */
  private static boolean isNormalData( int t )
  {
    return t == ParserUtil.DATA_NORMAL || t == ParserUtil.DATA_TOPOFIL;
    //  || t == ParserUtil.DATA_DEFAULT || t == ParserUtil.DATA_NONE;
  }

  private void dataLine( String[] vals, int n, String line, ParserSurvexState st )
  {
    if ( isNormalData( st.data_type ) ) {
      if ( st.interleaved ) {
        interleavedLine( st, vals, n, line );
      } else {
        normalLine( st, vals, n, line );
      }
    } else if ( st.data_type == ParserUtil.DATA_PASSAGE && mWithLrud ) {
      passageLine( st, vals, n, line );
    } else {
      warn( "unsupported data style" );
    }
  }

  /** interleaved data: station lines alternate with reading lines
   */
  private void interleavedLine( ParserSurvexState st, String[] vals, int n, String line )
  {
    if ( mInStation ) {
      String to = stationName( st, vals[0] );
      if ( mFrom != null && mLegOk ) {
        if ( mReversed ) {
          insertLegShot( to, mFrom, mLen, mBer, mCln, st );
        } else {
          insertLegShot( mFrom, to, mLen, mBer, mCln, st );
        }
      }
      mFrom      = to;
      mInStation = false;
      mLegOk     = false;
    } else {
      float[] leg = computeLeg( st, vals, n );
      mReversed = false;
      if ( leg == null ) {
        warn( "invalid data " + line );
        mLegOk = false;
      } else {
        mLen = leg[0];
        mBer = leg[1];
        mCln = leg[2];
        mLegOk = true;
        String dir = tok( vals, n, st.jDirection );
        if ( dir != null ) {
          if ( dir.equalsIgnoreCase("B") ) {
            mReversed = true;
          } else if ( ! dir.equalsIgnoreCase("F") ) {
            warn( "invalid direction " + dir );
            mLegOk = false;
          }
        }
      }
      mInStation = true;
    }
  }

  /** non-interleaved data: one line per leg
   */
  private void normalLine( ParserSurvexState st, String[] vals, int n, String line )
  {
    if ( n <= st.mJmin || st.jFrom < 0 || st.jTo < 0 ) {
      warn( "unexpected data " + line );
      return;
    }
    String from = stationName( st, vals[ st.jFrom ] );
    String to   = stationName( st, vals[ st.jTo ] );
    float[] leg = computeLeg( st, vals, n );
    if ( leg == null ) {
      warn( "invalid data " + line );
      return;
    }
    float len = leg[0];
    float ber = leg[1];
    float cln = leg[2];
    if ( mWithLrud && ! SPLAY.equals( from ) ) {
      float dist;
      dist = lrud( st, vals, n, st.jLeft,  st.mScaleLeft  * st.mUnitLeft,  st.mZeroLeft );
      addSplay( st, from, dist, TDMath.in360( ber - 90 ), 0 );
      dist = lrud( st, vals, n, st.jRight, st.mScaleRight * st.mUnitRight, st.mZeroRight );
      addSplay( st, from, dist, TDMath.add90( ber ), 0 );
      dist = lrud( st, vals, n, st.jUp,    st.mScaleUp    * st.mUnitUp,    st.mZeroUp );
      addSplay( st, from, dist, 0, 90 );
      dist = lrud( st, vals, n, st.jDown,  st.mScaleDown  * st.mUnitDown,  st.mZeroDown );
      addSplay( st, from, dist, 0, -90 );
    }
    insertLegShot( from, to, len, ber, cln, st );
  }

  /** passage data: station with up and down (left and right have no direction)
   */
  private void passageLine( ParserSurvexState st, String[] vals, int n, String line )
  {
    if ( n <= st.mJmin2 || st.jStation < 0 ) {
      warn( "unexpected data " + line );
      return;
    }
    String from = stationName( st, vals[ st.jStation ] );
    if ( SPLAY.equals( from ) ) return;
    // insertStation( from );
    float dist;
    dist = lrud( st, vals, n, st.jUp,   st.mScaleUp   * st.mUnitUp,   st.mZeroUp );
    addSplay( st, from, dist, 0, 90 );
    dist = lrud( st, vals, n, st.jDown, st.mScaleDown * st.mUnitDown, st.mZeroDown );
    addSplay( st, from, dist, 0, -90 );
  }

  private static String tok( String[] vals, int n, int j )
  {
    return ( j < 0 || j >= n )? null : vals[j];
  }

  /** @return the numeric value of a reading, NaN if the reading is absent, omitted or not a number
   */
  private float num( ParserSurvexState st, String s )
  {
    if ( s == null || s.equals("-") ) return Float.NaN;
    return ParserUtil.parseFloat( replaceDecimalSep( st, s ), Float.NaN );
  }

  /** @return true if a reading is present, not omitted, and its value is not valid
   */
  private static boolean bad( String s, float v )
  {
    return s != null && ! s.equals("-") && Float.isNaN( v );
  }

  /** parse a clino reading and apply the calibration (but not to UP, DOWN and LEVEL)
   * @param su    scale * units
   * @param zero  zero error
   * @return the clino in degrees, NaN if the reading is absent, omitted or not valid
   */
  private float clino( ParserSurvexState st, String s, float su, float zero )
  {
    if ( s == null ) return Float.NaN;
    String v = s.toUpperCase( Locale.ROOT );
    if ( v.equals("UP")   || v.equals("U") || v.equals("+V") ) return 90;
    if ( v.equals("DOWN") || v.equals("D") || v.equals("-V") ) return -90;
    if ( v.equals("LEVEL") ) return 0;
    float ret = num( st, s );
    if ( ! Float.isNaN( ret ) ) ret = ret * su - zero;
    return ret;
  }

  /** compute the calibrated reading of a leg, with back readings combined with the forward ones
   *  Calibration, units and zero error are applied to each reading before they are combined.
   * @return array { length, bearing, clino } (in meters and degrees), or null if the line is not valid
   */
  private float[] computeLeg( ParserSurvexState st, String[] vals, int n )
  {
    if ( n <= st.mJmin ) return null;
    String sT  = tok( vals, n, st.jLength );
    String sBT = tok( vals, n, st.jBackLength );
    String sB  = tok( vals, n, st.jCompass );
    String sBB = tok( vals, n, st.jBackCompass );
    String sC  = tok( vals, n, st.jClino );
    String sBC = tok( vals, n, st.jBackClino );

    float t  = num( st, sT );
    float bt = num( st, sBT );
    float b  = num( st, sB );
    float bb = num( st, sBB );
    float c  = clino( st, sC,  st.mScaleCln * st.mUnitCln,         st.mZeroCln );
    float bc = clino( st, sBC, st.mScaleBackCln * st.mUnitBackCln, st.mZeroBackCln );
    if ( bad( sT, t ) || bad( sBT, bt ) || bad( sB, b ) || bad( sBB, bb ) || bad( sC, c ) || bad( sBC, bc ) ) return null;

    // length
    if ( ! Float.isNaN( t ) )  t  = t  * st.mScaleLen * st.mUnitLen - st.mZeroLen;
    if ( ! Float.isNaN( bt ) ) bt = bt * st.mScaleBackLen * st.mUnitBackLen - st.mZeroBackLen;
    float len = Float.isNaN( t )? bt : Float.isNaN( bt )? t : ( t + bt ) / 2;
    if ( Float.isNaN( len ) ) return null;

    // bearing, in degrees
    if ( ! Float.isNaN( b ) )  b  = TDMath.in360( b * st.mScaleBer * st.mUnitBer - st.mZeroBer );
    if ( ! Float.isNaN( bb ) ) bb = TDMath.in360( bb * st.mScaleBackBer * st.mUnitBackBer - st.mZeroBackBer + 180 );
    float ber = Float.isNaN( b )? bb : Float.isNaN( bb )? b : TDMath.averageDegrees( b, bb );

    // clino, in degrees
    float cln = Float.isNaN( c )? ( Float.isNaN( bc )? Float.NaN : 0.0f - bc ) : ( Float.isNaN( bc )? c : ( c - bc ) / 2 );
    boolean plumb = ! Float.isNaN( cln ) && Math.abs( Math.abs( cln ) - 90 ) < 0.01f;
    if ( Float.isNaN( cln ) ) cln = 0; // omitted clino: horizontal leg

    if ( Float.isNaN( ber ) ) {
      if ( ! plumb ) return null; // only plumbs can have no bearing
      ber = 0;
    } else {
      ber = checkApplyDeclination( ber, st.mDeclination ); // returns in360
    }
    return new float[] { len, ber, cln };
  }

  /** @return the calibrated value of a LRUD reading, NaN if absent or omitted
   */
  private float lrud( ParserSurvexState st, String[] vals, int n, int j, float su, float zero )
  {
    float v = num( st, tok( vals, n, j ) );
    return Float.isNaN( v )? Float.NaN : v * su - zero;
  }

  private void addSplay( ParserSurvexState st, String from, float dist, float ber, float cln )
  {
    if ( Float.isNaN( dist ) || dist < 0 ) return;
    shots.add( new ParserShot( from, TDString.EMPTY, dist, ber, cln, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, st.mDuplicate, st.mSurface, false, "" ) );
  }

  /** insert a leg or splay shot
   * @param from  from station, or SPLAY
   * @param to    to station, or SPLAY
   */
  private void insertLegShot( String from, String to, float len, float ber, float cln, ParserSurvexState st )
  {
    boolean splay_from = SPLAY.equals( from );
    boolean splay_to   = SPLAY.equals( to );
    if ( splay_from && splay_to ) return;
    if ( splay_from ) { // reverse the leg
      from = to;
      ber  = TDMath.in360( ber + 180 );
      cln  = 0.0f - cln;
      splay_to = true;
      // insertStation( to );
    } 
    // insertStation( from );
    boolean splay = splay_to || st.mSplay;
    shots.add( new ParserShot( from, splay? TDString.EMPTY : to, len, ber, cln, 0.0f, splay? ExtendType.EXTEND_UNSET : ExtendType.EXTEND_RIGHT, LegType.NORMAL, st.mDuplicate, st.mSurface, false, "" ) );
  }

  // ---------------------------------------------------------------------
  // NAMES

  /** @return true if the name is an anonymous station: "-" or one, two, or three separators
   */
  private static boolean isAnonymous( ParserSurvexState st, String name )
  {
    if ( name.equals("-") || name.equals("_") ) return true;
    String s = st.mSep;
    return name.equals( s ) || name.equals( s + s ) || name.equals( s + s + s );
  }

  /** @return the full hierarchical name of a station written in the survey of a state, or SPLAY if it is anonymous
   * @param st   state
   * @param raw  station name as written in the file
   */
  private String stationName( ParserSurvexState st, String raw )
  {
    String name   = st.fold( raw );
    String target = st.mAliases.get( name );
    if ( target != null ) name = target;
    if ( isAnonymous( st, name ) ) return SPLAY;
    return st.fullName( name );
  }

  private static String replaceDecimalSep( ParserSurvexState st, String val )
  {
    return ( st.mDecimalSep == null )? val : val.replace( st.mDecimalSep, "." );
  }
}
