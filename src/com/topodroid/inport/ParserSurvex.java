/* @file ParserSurvex.java
 *
 * @author marco corvi
 * @date jav 2019
 *
 * @brief TopoDroid Survex parser
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
// import com.topodroid.prefs.TDSetting;
import com.topodroid.types.ExtendType;
import com.topodroid.types.LegType;
import com.topodroid.TDX.TopoDroidApp;

import java.io.IOException;
// import java.io.FileNotFoundException;
// import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.Locale;
// import java.util.Stack;

// TODO this class can be made extend ImportParser
//
// units, calibrate, sd supported quantities:
//   length tape bearing compass gradient clino counter depth x y z declination

class ParserSurvex extends ImportParser
{
  // final static private String EMPTY = "";

  // private Stack< ParserSurvexState > mStates = new Stack<>(); // states stack (LIFO) - replaced by linked list

  // /** push a state at the top of the stack
  //  */
  // private void pushState( ParserSurvexState state )
  // {
  //   mStates.push( state );
  // }

  // /** remove the state at the top
  //  * @return the new state currently at the top
  //  */
  // private ParserSurvexState popState()
  // {
  //   mStates.pop();
  //   return mStates.peek();
  // }


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
  private ArrayList< String > stations;
  private HashMap<String,String> aliases;  // station aliases

  // public int getShotNumber()    { return shots.size(); }
  // public int getSplayNumber()   { return splays.size(); }

  ArrayList< ParserShot > getShots()    { return shots; }
  ArrayList< ParserShot > getSplays()   { return splays; }
  ArrayList< String >     getStations() { return stations; }
  ArrayList< Fix >        getFixes()    { return fixes; }

  private int jFrom    = 0;
  private int jTo      = 1;
  private int jLength  = 2;
  private int jCompass = 3;
  private int jClino   = 4;
  private int jBackCompass = -1;
  private int jBackClino   = -1;
  private int jLeft    = -1;
  private int jRight   = -1;
  private int jUp      = -1;
  private int jDown    = -1;
  private int jStation = -1;
 
  private String mDecimalSep = null;
  private static boolean FF = true;

  // same as in ImportParser.java
  String initStation()
  {
    for ( ParserShot sh : shots ) {
      if ( sh.from != null && sh.from.length() > 0 ) return sh.from;
    }
    return TDString.ZERO;
  }

  float surveyDeclination( ) { return mDeclination; }
  // ---------------------------------------------------------

  ParserSurvex( InputStreamReader isr, String filename, boolean apply_declination ) throws ParserException
  {
    super( apply_declination );
    TopoDroidApp.updateAnalytic( TDAnalytics.IMPORT_SVX );
    FF = TDAnalytics.FF();
    if ( mApplyDeclination ) mDeclination = 0.0f;
    mName    = TDio.extractName( filename );
    fixes    = new ArrayList<>();
    stations = new ArrayList<>();
    aliases  = new HashMap<>();
    ParserSurvexState state = new ParserSurvexState("."); // root of the linked list of states
    // mStates.pushState( state );
    mPartial = readFile( isr, filename, state );
    checkValid();
    if ( mApplyDeclination ) mDeclination = 0.0f;
  }

  // private String nextLine( BufferedReader br ) throws IOException // from ImportParser
  // {
  //   StringBuilder ret = new StringBuilder();
  //   {
  //     String line = br.readLine();
  //     if ( line == null ) return null; // EOF
  //     while ( line != null && line.endsWith( "\\" ) ) {
  //       ret.append( line.replace( '\\', ' ' ) ); // FIXME
  //       line = br.readLine();
  //     }
  //     if ( line != null ) ret.append( line );
  //   }
  //   return ret.toString();
  // }

  // station fullname:  cave.survey.station
  // private String extractStationName( String fullname )
  // {
  //   int idx = fullname.lastIndexOf('.');
  //   if ( idx >= 0 ) {
  //      return fullname.substring(idx+1); 
  //   }
  //   return fullname;
  // }

  /** read input file
   * @param filename name of the file to parse
   * @param state    state of the parser
   */
  private boolean readFile( InputStreamReader isr, String filename, ParserSurvexState state ) throws ParserException
  {
    // state.dump("initial");
    boolean partial = false;
    boolean in_station = true;
    boolean ignore_data = false;
    String from = null;
    String to;
    int line_nr = 0; // debug line number

    float len=0, ber=0, cln=0;

    // String path = basepath;   // survey pathname(s)
    // int ks = 0;               // survey index
    // int ksmax = 20;
    // int[] survey_pos = new int[ksmax]; // current survey pos in the pathname

    Pattern pattern = Pattern.compile( "\\s+" );

    try {
      String dirname = "./";
      int i = filename.lastIndexOf('/');
      if ( i > 0 ) dirname = filename.substring(0, i+1);
      // System.out.println("Read File dir " + dirname + " filename " + filename );
      // TDLog.Log( TDLog.LOG_THERION, "reading file " + filename + " dir " + dirname );
      // TDLog.Log( TDLog.LOG_IO, "import read Survex file <" + filename + ">" );

      BufferedReader br = TDio.getBufferedReader( isr, filename );
      String line = nextLine( br );
      while ( line != null ) {
        ++line_nr; // debug
        // TDLog.v( "SVX " + state.mSurveyLevel + " : " + line );
        line = line.trim();
        int pos = line.indexOf( ';' );
        if ( pos >= 0 ) {
          line = line.substring( 0, pos ).trim();
        }
        if ( line.length() > 0 ) {
          String[] vals = pattern.split(line); // line.split( "\\s+" );
          int vals_len = vals.length;
          if ( vals_len == 1 ) {
	    if ( vals[0].startsWith("*") ) {
	      String cmd = vals[0].substring(1);
	      if ( cmd.equals("solve" ) ) {
                // TDLog.v("SVX ignore solve");
                // partial = true;
	      } else if ( cmd.equals("data" ) ) { // data default
                ignore_data = false;
                state.setDataDefault();
	        resetJIndices();
              } else if ( FF && cmd.equals("begin") ) { // parse "*begin" name
                state = new ParserSurvexState( state, ( (vals_len > 1)? vals[1] : "--" ) );
                // state.dump( line_nr + " new" );
	      } else if ( FF && cmd.equals("end") ) {
                ignore_data = false;
                // state.dump( line_nr + " old" );
                if ( state.mParent != null ) state = state.mParent;
                // state.dump( line_nr + " current" );
              } else {
                TDLog.v("SVX unknown cmd <" + cmd + ">" );
                partial = true;
              }
	    } else { 
              // interleaved station
	      if ( state.interleaved && in_station ) { // read station
                to = checkAlias( ParserUtil.applyCase( state.mCase, vals[0] ) );
		if ( from != null ) { // add shot
                  shots.add( new ParserShot( from, to, len, ber, cln, 0.0f, ExtendType.EXTEND_RIGHT, LegType.NORMAL, state.mDuplicate, state.mSurface, false, "" ) );
		}
		from = to;
		in_station = false; // in_data
	      }
	    }
	  } else if ( vals_len > 1 ) {
            // all commands have parameters, except "*solve" which is ignored
	    // and data line have at least two values
	    if ( vals[0].startsWith("*") ) {
              String cmd = vals[0].substring(1);
              if ( cmd.equals("alias" ) ) { 
                // alias station <alias> <target>
		if ( "station".equals( vals[1] ) ) {
                  if ( vals_len >= 4 ) {
		    String target = vals[vals_len - 1];
		    for ( int j=1; j<vals_len-1; ++j ) {
                      aliases.put( vals[j], target );
		    }
		  } else if ( vals_len == 3 ) {
		    aliases.remove( vals[1] );
		  }
		} else {
                  TDLog.v( "SVX: unsupported alias " + vals[1] );
                  partial = true;
		}
              } else if ( cmd.equals("include") ) {
                TDLog.v("SVX " + line_nr + " ignore include " );
                partial = true;
              } else if ( cmd.equals("copyright") ) {
                // TDLog.v("SVX " + line_nr + " ignore copyright " );
                // partial = true;
              } else if ( cmd.equals("entrance") ) {
                TDLog.v("SVX " + line_nr + " ignore entrance " );
                partial = true;
              } else if ( cmd.equals("export") ) {
                TDLog.v("SVX " + line_nr + " ignore export " );
                partial = true;
              } else if ( cmd.equals("infer") ) {
                // TDLog.v("SVX " + line_nr + " ignore infer " );
                // partial = true;
              } else if ( cmd.equals("instrument") ) {
                TDLog.v("SVX " + line_nr + " ignore instrument " );
                partial = true;
              } else if ( cmd.equals("prefix") ) {
                TDLog.v("SVX " + line_nr + " ignore prefix " );
                partial = true;
              } else if ( cmd.equals("ref") ) {
                TDLog.v("SVX " + line_nr + " ignore ref " );
                partial = true;
              } else if ( cmd.equals("require") ) {
                TDLog.v("SVX " + line_nr + " ignore require " );
                partial = true;
              } else if ( cmd.equals("sd") ) {
                // TDLog.v("SVX " + line_nr + " ignore sd " );
                // partial = true;
              } else if ( cmd.equals("truncate") ) {
                TDLog.v("SVX " + line_nr + " ignore truncate " );
                partial = true;
              } else if ( cmd.equals("case") ) {
                TDLog.v("SVX " + line_nr + " TODO case " );
                partial = true;
              } else if ( cmd.equals("set") ) { // *set can change the presentation of input values
                // set blank x09x20
		// set decimal ,
                if ( vals[1].equals("decimal") && vals[2] != null ) {
                  mDecimalSep = vals[2];
                } else {
                  TDLog.v("SVX " + line_nr + " TODO set " );
                  partial = true;
                }
              } else if ( cmd.equals("default") ) {
                // default calibrate|data|units|all
		if ( vals_len > 1 ) { // always true
		  String what = vals[1].toLowerCase( Locale.getDefault() );
		  if ( what.equals("calibrate") ) {
                    state.setCalibrateDefault();
		  } else if ( what.equals("data") ) {
                    ignore_data = false;
                    state.setDataDefault();
	            resetJIndices();
		  } else if ( what.equals("units") ) {
                    state.setUnitsDefault();
		  } else if ( what.equals("all") ) {
                    state.setCalibrateDefault();
                    state.setDataDefault();
                    state.setUnitsDefault();
		  } else {
                    TDLog.v("SVX unsupported default <" + what + ">" );
                    partial = true;
                  }
		}
              } else if ( cmd.equals("begin") ) { // parse "*begin" name
                state = new ParserSurvexState( state, ( (vals_len > 1)? vals[1] : null ) );
                // state.dump( line_nr + " new" );

              } else if ( cmd.equals("title") ) {
                if ( mTitle == null && vals_len > 1 ) {
                  for ( int j = 1; j<vals_len; ++j ) {
                    if ( vals[j].length() == 0 ) continue;
                    if ( vals[j].startsWith("\"") ) {
                      if ( vals[j].endsWith( "\"" ) ) { 
                        mTitle = vals[j].substring(1, vals[j].length()-1 );
                      } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append( vals[j].substring(1) );
                        for ( ++j; j<vals_len; ++j ) {
                          if ( vals[j].length() == 0 ) continue;
                          if ( vals[j].endsWith( "\"" ) ) { // skip final " and end loop
                            sb.append(" ").append(vals[j].substring(0, vals[j].length()-1));
                            break;
                          } else {
                            sb.append(" ").append(vals[j] );
                          }
                        }
                        mTitle = sb.toString();
                      }
                    } else {
                      mTitle = vals[j];
                    }
                    break;
                  }
                }
              } else if ( cmd.equals("date") ) {
                String date = vals[1];
                if ( mDate == null ) mDate = date; // save date
                TDLog.v("SVX " + line_nr + " date " + mDate );
              } else if ( cmd.equals("team") ) {
                if ( mTeam.length() > 0 ) {
                  mTeam = mTeam + " " + TDUtil.concat( vals, 1 );
                } else {
                  mTeam = mTeam + TDUtil.concat( vals, 1 );
                }
                TDLog.v("SVX " + line_nr + " team " + mTeam );
              } else if ( cmd.equals("calibrate") ) {
                TDLog.v("SVX " + line_nr + " calibrate ");
                // calibrate <quantities> <zero_error> [<scale>]
                // calibrate <quantities> <zero_error> <zero_units> [<scale>]
		// calibrate default
		if ( vals_len > 1 ) { // always true
		  if ( vals[1].toLowerCase( Locale.getDefault() ).equals("default") ) {
                    state.setCalibrateDefault();
		  } else {
		    boolean clen = false;
                    boolean cber = false;
                    boolean ccln = false;
                    // boolean bber = false;
                    // boolean bcln = false;
                    boolean cdecl = false;
	            int k = 1;
                    for ( ; k<vals_len - 1; ++k ) {
                      String what = vals[k].toLowerCase( Locale.getDefault() );
                      if ( what.equals("length")  || what.equals("tape") )  { clen = true; }
                      else if ( what.equals("compass") || what.equals("bearing") )  { cber = true; }
                      else if ( what.equals("clino")   || what.equals("gradient") ) { ccln = true; }
                      // else if ( FF && ( what.equals("backcompass") || what.equals("backbearing") ) )  { cber = true; bber = true; }
                      // else if ( FF && ( what.equals("backclino")   || what.equals("backgradient") ) ) { ccln = true; bcln = true; }
                      else if ( what.equals("declination") ) { cdecl = true; }
                      else { 
                        TDLog.v("SVX unexpected default <" + what + ">" );
                        partial = true;
                        break;
                      }
                    }
                    float zero  =  0.0f;
                    float units = -1.0f;
                    float scale =  1.0f;
                    if( k<vals_len ) {
	              try { // try to read the "scale" float (next val)
                        zero = ParserUtil.parseFloat( replaceDecimalSep( vals[k] ), 0 );
	                ++k;
		        if ( k + 2 == vals_len ) {
	                  try { // try to read the "zero" float (next val)
                            units  = Float.parseFloat( replaceDecimalSep( vals[k] ) );
	                    ++k;
	                    break;
                          } catch ( NumberFormatException e ) {
	                    TDLog.e("SVX " + line_nr + " Non-number units " + vals[k] );
                            partial = true;
                          }
		        }
		        if ( k + 1 == vals_len ) {
	                  try { // try to read the "zero" float (next val)
                            scale  = Float.parseFloat( replaceDecimalSep( vals[k] ) );
	                    break;
                          } catch ( NumberFormatException e ) {
                            TDLog.e("SVX " + line_nr + " Non-number scale " + vals[k]);
                            partial = true;
                          }
		        }
                      } catch ( NumberFormatException e ) {
                        TDLog.e("SVX " + line_nr + " Non-number zero " + vals[k]);
                        partial = true;
                      }
                    }

                    if ( clen ) {
                      if ( units < 0 ) units = state.mUnitLen;
                      state.mZeroLen  = zero * scale * units;
                      state.mScaleLen = scale;
                    }
                    if ( cber ) {
		      if ( units < 0 ) units = state.mUnitBer;
                      state.mZeroBer  = zero * scale * units;
                      state.mScaleBer = scale;
                    }
                    if ( ccln ) {
		      if ( units < 0 ) units = state.mUnitCln;
                      state.mZeroCln  = zero * scale * units;
                      state.mScaleCln = scale;
                    }
                    if ( cdecl ) {
                      state.mDeclination = zero; // * scale * units;
                      if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                    }
                  }
                }
              } else if ( cmd.equals("units") ) {
                // units quantities [factor] unit
		// units default
		if ( vals_len > 1 ) {
                  if ( vals[1].toLowerCase( Locale.getDefault() ).equals("default") ) {
                    state.setUnitsDefault();
		  } else {
                    boolean ulen = false;
                    boolean uber = false;
                    boolean ucln = false;
                    boolean ubber = false;
                    boolean ubcln = false;
                    boolean uleft  = false;
                    boolean uright = false;
                    boolean uup    = false;
                    boolean udown  = false;
		    int k = 1;
                    for ( ; k<vals_len - 1; ++k ) {
                      if ( vals[k].equals("length")  || vals[k].equals("tape") ) { ulen = true; }
		      else if ( vals[k].equals("compass") || vals[k].equals("bearing") ) { uber = true; }
		      else if ( vals[k].equals("clino")   || vals[k].equals("gradient") ) { ucln = true; }
		      else if ( FF && ( vals[k].equals("backcompass") || vals[k].equals("backbearing") ) )  { ubber = true; }
		      else if ( FF && ( vals[k].equals("backclino")   || vals[k].equals("backgradient") ) ) { ubcln = true; }
		      else {
                        TDLog.v( "SVX unexpected default <" + vals[k] + ">" );
                        partial = true;
                        break;
                      }
                    }
                    float factor = 1.0f;
		    if ( k + 2 == vals_len ) {
                      // try {
                        factor = ParserUtil.parseFloat( replaceDecimalSep( vals[k] ), 1 );
		        ++k;
                      // } catch ( NumberFormatException e ) {
                      //   TDLog.e( "SVX " + line_nr + " units without factor " + line ); // this is OK
                      // }
		    }
		    if ( k + 1 == vals_len ) {
                      if ( ulen || uleft || uright || uup || udown ) {
                        float l = factor * ParserUtil.parseLengthUnit( vals[k] );
                        if ( ulen ) state.mUnitLen = l;
                      } 
                      if ( uber || ucln || ubber || ubcln ) {
                        float a = factor * ParserUtil.parseAngleUnit( vals[k] );
                        if ( uber ) state.mUnitBer = a;
                        if ( ucln ) state.mUnitCln = a;
                        if ( ubber ) state.mUnitBackBer = a;
                        if ( ubcln ) state.mUnitBackCln = a;
                      }
		    }
		  }
		}
              } else if ( cmd.equals("declination") ) { 
                // declination auto [<x> <y> <z>]
		// declination <value> [<units>]
                if ( vals_len > 1 ) {
	          if ( vals[1].equals("auto") ) { // declination reset
                    state.mDeclination = ( state.mParent == null )? 0 : state.mParent.mDeclination;
                    if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
	          } else {
                    try {
                      float declination = Float.parseFloat( replaceDecimalSep( vals[1] ) );
                      if ( vals_len > 2 ) {
                        declination *= ParserUtil.parseAngleUnit( vals[2] );
                      }
                      state.mDeclination = declination;
                      if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                    } catch ( NumberFormatException e ) {
                      TDLog.e( "SVX " + line_nr + " declination " + line );
                      partial = true;
	            }
                  }      
                }      
              } else if ( cmd.equals("flags") ) {
                int k = 1;
                if ( k < vals_len ) {
	          boolean val = true;
                  String what = vals[k].toLowerCase( Locale.getDefault() );
                  if ( what.equals("not") ) {
                    val = false;
	            ++k;
	          }
	          if ( k < vals_len ) {
                    what = vals[k].toLowerCase( Locale.getDefault() );
                    if ( what.equals("duplicate") ) {
                      state.mDuplicate = val;
	            } else if ( what.equals("splay") ) {
                      state.mSplay = val;
                    } else if ( what.equals("surface") ) {
                      state.mSurface = val;
                    } else {
                      TDLog.v("SVX unsupported flag <" + what + ">" );
                      partial = true;
                    }
                  }
                }
              } else if ( cmd.equals("cs") ) { 
                TDLog.v("SVX " + line_nr + " TODO cs " + line );
              } else if ( cmd.equals("fix") ) { // ***** fix station east north Z (ignored std-dev's)
                if ( vals_len > 4 ) {
                  String name = checkAlias( ParserUtil.applyCase( state.mCase, vals[1] ) );
                  try {
	            fixes.add( new Fix( name,
                                        Float.parseFloat( replaceDecimalSep( vals[2] ) ),
                                        Float.parseFloat( replaceDecimalSep( vals[3] ) ),
                                        Float.parseFloat( replaceDecimalSep( vals[4] ) ) ) );
                  } catch ( NumberFormatException e ) {
                    TDLog.e( "SVX " + line_nr + " fix " + line );
                    partial = true;
                  }
                }
              } else if ( cmd.equals("equate") ) {
                // equate station station ...
                if ( vals_len > 2 ) {
                  from = checkAlias( ParserUtil.applyCase( state.mCase, vals[1] ) );
                  for ( int j=2; j<vals_len; ++j ) {
                    to = checkAlias( ParserUtil.applyCase( state.mCase, vals[j] ) ); 
                    shots.add( new ParserShot( from, to, 0.0f, 0.0f, 0.0f, 0.0f, 0, LegType.NORMAL, true, false, false, "" ) );
                  }
                }
              } else if ( cmd.equals("data") ) {
                // data normal from to length compass clino ...
	        // data default
                ignore_data = false;
	        String what = vals[1].toLowerCase( Locale.getDefault() );
                if ( what.equals("normal") || what.equals("topofil") ) { // topofil is synonim of normal
                  state.data_type = ParserUtil.DATA_NORMAL;
	          if ( setJIndices( vals, vals_len ) ) {
                    state.interleaved = true;
		    in_station = true;
		    from = null;
		    len = ber = cln = 0;
		  } else {
                    state.interleaved = false;
		  }
                // TODO other style syntax
                } else if ( what.equals("diving") ) {
                  state.data_type = ParserUtil.DATA_DIVING;
                } else if ( what.equals("cartesian") ) {
                  state.data_type = ParserUtil.DATA_CARTESIAN;
                } else if ( what.equals("cylpolar") ) {
                  state.data_type = ParserUtil.DATA_CYLPOLAR;
                } else if ( what.equals("passage") ) {
                  state.data_type = ParserUtil.DATA_PASSAGE;
	          setJIndicesPassage( vals, vals_len );
                  state.interleaved = false;
                } else if ( what.equals("nosurvey") ) {
                  state.data_type = ParserUtil.DATA_NOSURVEY;
                } else if ( what.equals("ignore") ) {
                  ignore_data = true;
                } else { // "default"
                  state.data_type = ParserUtil.DATA_DEFAULT;
	          resetJIndices();
                }
              } else if ( cmd.equals("end") ) { // end | end <survey>
                ignore_data = false;
                // state = popState();
                // state.dump( line_nr + " old" );
                if ( state.mParent != null ) state = state.mParent;
                // state.dump( line_nr + " current" );
                // path = path.substring(survey_pos[ks]); // return to previous survey_pos in path
	      } else {
                TDLog.e("SVX " + line_nr + " unknown cmd " + cmd );
                partial = true;
              }
            } else if ( ! ignore_data ) {
              // this is for DATA_NORMAL
	      if ( state.interleaved ) {
		if ( in_station ) {
                  to = checkAlias( ParserUtil.applyCase( state.mCase, vals[0] ) );
		  if ( from != null ) { // add shot
                    shots.add( new ParserShot( from, to, len, ber, cln, 0.0f, ExtendType.EXTEND_RIGHT, LegType.NORMAL, state.mDuplicate, state.mSurface, false, "" ) );
		  }
		  from = to;
                  in_station = false;
		} else {
                  len  = (jLength < 0 )? 0 : ParserUtil.parseFloat( replaceDecimalSep( vals[jLength]), 0 );
                  ber  = (jCompass < 0 )? Float.NaN : ParserUtil.parseFloat( replaceDecimalSep( vals[jCompass] ), Float.NaN );
                  cln  = (jClino < 0 )? Float.NaN : parseClino( vals[jClino], Float.NaN );
                  if ( FF ) {
                    float bber = (jBackCompass < 0 )? Float.NaN : ParserUtil.parseFloat( replaceDecimalSep( vals[jBackCompass] ), Float.NaN );
                    float bcln = (jBackClino < 0)? Float.NaN : parseClino( vals[jBackClino], Float.NaN );
                    if ( Float.isNaN( cln ) ) {
                      cln = Float.isNaN( bcln )? Float.NaN : -bcln;
                    } else if ( ! Float.isNaN( bcln ) ) {
                      cln = ( cln - bcln )/2;
                    }
                    if ( Float.isNaN( ber ) ) {
                      ber = Float.isNaN( bber )? Float.NaN : TDMath.in360(bber+180);
                    } else if ( ! Float.isNaN( bber ) ) {
                      ber = TDMath.averageDegrees( ber, TDMath.in360(bber+180) );
                    }
                  }

	          float zLen = state.mZeroLen;
	          float sLen = state.mScaleLen * state.mUnitLen;

                  if ( Float.isNaN( len ) || Float.isNaN( ber ) || Float.isNaN( cln ) ) {
                    TDLog.v("SVX data " + line );
                    partial = true;
                  } else {
                    len = len * sLen - zLen;
                    ber = ber * state.mScaleBer * state.mUnitBer - state.mZeroBer;
                    ber = checkApplyDeclination( ber, state.mDeclination );
                    cln = cln * state.mScaleCln * state.mUnitCln - state.mZeroCln;
                  }
                  in_station = true;
		}
	      } else { // not interleaved
	        if ( vals_len >= 5 ) {
                  if ( state.data_type == ParserUtil.DATA_NORMAL || state.data_type == ParserUtil.DATA_TOPOFIL ) {
                    // TDLog.v("SVX " + line_nr + " data " + line );
                    from = checkAlias( ParserUtil.applyCase( state.mCase, vals[jFrom] ) );
                    to   = checkAlias( ParserUtil.applyCase( state.mCase, vals[jTo] ) );
                    len  = (jLength < 0 )? 0 : ParserUtil.parseFloat( replaceDecimalSep( vals[jLength] ), 0 );
                    ber  = (jCompass < 0 )? Float.NaN : ParserUtil.parseFloat( replaceDecimalSep( vals[jCompass] ), Float.NaN );
                    cln  = (jClino < 0 )? Float.NaN : parseClino( vals[jClino], Float.NaN );
                    if ( FF ) {
                      float bber = (jBackCompass < 0 )? Float.NaN : ParserUtil.parseFloat( replaceDecimalSep( vals[jBackCompass] ), Float.NaN );
                      float bcln = (jBackClino < 0)? Float.NaN : parseClino( vals[jBackClino], Float.NaN );
                      if ( Float.isNaN( cln ) ) {
                        cln = Float.isNaN( bcln )? 0 : -bcln;
                      } else if ( ! Float.isNaN( bcln ) ) {
                        cln = ( cln - bcln )/2;
                      }
                      if ( Float.isNaN( ber ) ) {
                        ber = Float.isNaN( bber )? 0 : TDMath.in360(bber+180);
                      } else if ( ! Float.isNaN( bber ) ) {
                        ber = TDMath.averageDegrees( ber, TDMath.in360(bber+180) );
                      }
                    }

	            // measure = read * scale - zero;
	            float zLen = state.mZeroLen;
	            float sLen = state.mScaleLen * state.mUnitLen;

                    if ( Float.isNaN( len ) || Float.isNaN( ber ) || Float.isNaN( cln ) ) {
                      TDLog.v("SVX data " + line );
                      partial = true;
                    } else {
                      len = len * sLen - zLen;
                      ber = ber * state.mScaleBer * state.mUnitBer - state.mZeroBer;
                      ber = checkApplyDeclination( ber, state.mDeclination );
                      cln = cln * state.mScaleCln * state.mUnitCln - state.mZeroCln;
                      TDLog.v("SVX shot " + from + " " + to + " " + len + " " + ber + " " + cln + " zL " + zLen + " sL " + state.mScaleLen + " " + state.mUnitLen );

                      float dist, b;
                      if ( jLeft >= 0 && jLeft < vals_len) {
                        dist = ParserUtil.parseFloat( replaceDecimalSep( vals[jLeft] ), 0 ) * sLen - zLen;
                        if ( dist >= 0 ) {
                          // b = ber - 90; if ( b < 0 ) b += 360;
                          b = TDMath.in360( ber - 90 );
                          shots.add( new ParserShot( from, TDString.EMPTY, dist, b, 0, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                        }
                      }
                      if ( jRight >= 0 && jRight < vals_len) {
                        dist = ParserUtil.parseFloat( replaceDecimalSep( vals[jRight] ), 0 ) * sLen - zLen;
                        if ( dist >= 0 ) {
                          // b = ber + 90; if ( b >= 360 ) b -= 360;
                          b = TDMath.add90( ber );
                          shots.add( new ParserShot( from, TDString.EMPTY, dist, b, 0, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                        }
                      }
                      if ( jUp >= 0 && jUp < vals_len) {
                        dist = ParserUtil.parseFloat( replaceDecimalSep( vals[jUp] ), 0 ) * sLen - zLen;
                        if ( dist >= 0 ) {
                          shots.add( new ParserShot( from, TDString.EMPTY, dist, 0, 90, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                        }
                      }
                      if ( jDown >= 0 && jDown < vals_len) {
                        dist = ParserUtil.parseFloat( replaceDecimalSep( vals[jDown] ), 0 ) * sLen - zLen;
                        if ( dist >= 0 ) {
                          shots.add( new ParserShot( from, TDString.EMPTY, dist, 0, -90, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                        }
                      }

                      // TODO add shot
                      if ( to.equals("-") || to.equals(".") || to.equals("..") || to.equals("...") ) { // splay shot
                        // FIXME splays
                        shots.add( new ParserShot( from, TDString.EMPTY, len, ber, cln, 0.0f, ExtendType.EXTEND_UNSET, LegType.NORMAL, state.mDuplicate, state.mSurface, false, "" ) );
                      } else {
                        // TDLog.v( "Parser Survex add shot " + from + " -- " + to);
                        shots.add( new ParserShot( from, to, len, ber, cln, 0.0f, ExtendType.EXTEND_RIGHT, LegType.NORMAL, state.mDuplicate, state.mSurface, false, "" ) );
                      }
                    }
                  } else if ( state.data_type == ParserUtil.DATA_PASSAGE ) {
                    from = checkAlias( ParserUtil.applyCase( state.mCase, vals[jStation] ) );
	            float zLen = state.mZeroLen;
	            float sLen = state.mScaleLen * state.mUnitLen;
                    float dist;
                    // if ( jLeft >= 0 && jLeft < vals_len) {
                    //   dist = ParserUtil.parseFloat( replaceDecimalSep( vals[jLeft] ), 0 ) * sLen - zLen;
                    // }
                    // if ( jRight >= 0 && jRight < vals_len) {
                    //   dist = ParserUtil.parseFloat( replaceDecimalSep( vals[jRight] ), 0 ) * sLen - zLen;
                    // }
                    if ( jUp >= 0 && jUp < vals_len) {
                      dist = ParserUtil.parseFloat( replaceDecimalSep( vals[jUp] ), 0 ) * sLen - zLen;
                      if ( dist >= 0 ) {
                        shots.add( new ParserShot( from, TDString.EMPTY, dist, 0, 90, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                      }
                    }
                    if ( jDown >= 0 && jDown < vals_len) {
                      dist = ParserUtil.parseFloat( replaceDecimalSep( vals[jDown] ), 0 ) * sLen - zLen;
                      if ( dist >= 0 ) {
                        shots.add( new ParserShot( from, TDString.EMPTY, dist, 0, -90, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                      }
                    }
                  } else {
                    partial = true;
                    // if ( state.data_type == ParserUtil.DATA_DIVING ) { // 
                    // } else if ( state.data_type == ParserUtil.DATA_DIVING ) { // from to tape compass fromdepth todepth
                    // } else if ( state.data_type == ParserUtil.DATA_CARTESIAN ) { //  from to nothing easting altitude
                    // } else {
                    // }
                  }
                }
                // FIXME other data types
              }
            }
          } // empty vals
        } // empty line
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      throw new ParserException();
    }
    // state.dump(line_nr + " final");
    if ( state.mSurveyLevel > 0 ) {
      TDLog.e("SVX " + line_nr + " endsurvey out of survey " + state.mSurveyLevel );
      partial = true;
    }

    if ( mDate == null ) mDate = TDUtil.currentDate();
    if ( mTitle == null ) mTitle = TDString.EMPTY;
    // TDLog.Log( TDLog.LOG_THERION, "Parser Survex shots "+ shots.size() +" splays "+ splays.size() +" fixes "+  fixes.size() );
    // TDLog.v( "Parser Survex shots "+ shots.size() + " splays "+ splays.size() +" fixes "+  fixes.size() );
    return partial;
  }

  /** @return true if data are interleaved
   */
  private boolean setJIndices(String[] vals, int vals_len)
  {
    jFrom = jTo = jLength = jCompass = jClino = jBackCompass = jBackClino = -1;
    jLeft = jUp = jRight  = jDown = -1;
    jStation = -1;
    int j0 = 0;
    if ( vals[2].equals("station") ) { // station ignoreall newline
      int j=3;
      for ( ; j<vals_len; ++j ) if ( vals[j].equals("newline") ) break;
      j0 = 0;
      for ( ++j; j<vals_len; ++j ) {
        if ( vals[j].equals("length") || vals[j].equals("tape") ) {
          jLength = j0; ++j0;
        } else if ( vals[j].equals("compass") || vals[j].equals("bearing") ) {
          jCompass = j0; ++j0;
        } else if ( vals[j].equals("clino") || vals[j].equals("gradient") ) {
          jClino = j0; ++j0;
        } else if ( FF && ( vals[j].equals("backcompass") || vals[j].equals("backbearing") ) ) {
          jBackCompass = j0; ++j0;
        } else if ( FF && ( vals[j].equals("backclino") || vals[j].equals("backgradient") ) ) {
          jBackClino = j0; ++j0;
	} else {
	  ++j0;
	}
      } 
      return true; // interleaved
    } else {
      for ( int j=2; j < vals_len; ++j ) {
        if ( vals[j].equals("from") ) {
          jFrom = j0; ++j0;
        } else if ( vals[j].equals("to") ) {
          jTo = j0; ++j0;
        } else if ( vals[j].equals("length") || vals[j].equals("tape") ) {
          jLength = j0; ++j0;
        } else if ( vals[j].equals("compass") || vals[j].equals("bearing") ) {
          jCompass = j0; ++j0;
        } else if ( vals[j].equals("clino") || vals[j].equals("gradient") ) {
          jClino = j0; ++j0;
        } else if ( FF && ( vals[j].equals("backcompass") || vals[j].equals("backbearing") ) ) {
          jBackCompass = j0; ++j0;
        } else if ( FF && ( vals[j].equals("backclino") || vals[j].equals("backgradient") ) ) {
          jBackClino = j0; ++j0;
        } else if ( vals[j].equals("left") ) {
          jLeft  = j0; ++j0;
        } else if ( vals[j].equals("right") ) {
          jRight = j0; ++j0;
        } else if ( vals[j].equals("up") ) {
          jUp    = j0; ++j0;
        } else if ( vals[j].equals("down") ) {
          jDown  = j0; ++j0;
        } else {
          ++j0;
        }
      }
    }
    // TDLog.v("SVX J-indices: F " + jFrom + " T " + jTo + " L " + jLength + " B " + jCompass + " C " + jClino + " BB " + jBackCompass + " BC " + jBackClino );
    return false;
  }

  private void setJIndicesPassage(String[] vals, int vals_len)
  {
    jFrom = jTo = jLength = jCompass = jClino = -1;
    jLeft = jUp = jRight  = jDown = -1;
    jStation = -1;
    int j0 = 0;
    // TDLog.v("j " + 0 + " vals[0] " + vals[0] );
    // TDLog.v("j " + 1 + " vals[1] " + vals[1] );
    for ( int j=2; j < vals_len; ++j ) {
      // TDLog.v("j " + j + " vals[j] " + vals[j] );
      if ( vals[j].equals("station") ) {
        jStation = j0; ++j0;
      } else if ( vals[j].equals("left") ) {
        jLeft  = j0; ++j0;
      } else if ( vals[j].equals("right") ) {
        jRight = j0; ++j0;
      } else if ( vals[j].equals("up") ) {
        jUp    = j0; ++j0;
      } else if ( vals[j].equals("down") ) {
        jDown  = j0; ++j0;
      } else {
        ++j0;
      }
    }
  }


  // 2023-03-10 FIX
  private void resetJIndices()
  {
     jFrom    = 0; // 2;
     jTo      = 1; // 3;
     jLength  = 2; // 4;
     jCompass = 3; // 5;
     jClino   = 4; // 6;
     jBackCompass = jBackClino = -1;
     jLeft = jUp = jRight  = jDown = -1;
  }

  private String checkAlias( String name )
  {
    if ( name.equals(".") || name.equals("..") || name.equals("...") || name.equals("-") ) return "";
    if ( aliases.containsKey( name ) ) return aliases.get( name );
    return name;
  }

  private float parseClino( String val, float def ) 
  {
    if ( FF && val.equalsIgnoreCase("UP") ) return 90;
    if ( FF && val.equalsIgnoreCase("DOWN") ) return -90;
    if ( FF && val.equalsIgnoreCase("LEVEL") ) return 0;
    return ParserUtil.parseFloat( replaceDecimalSep( val ), def );
  }

  private String replaceDecimalSep( String val )
  {
    return (mDecimalSep == null)? val : val.replace( mDecimalSep, "." );
  }
}
