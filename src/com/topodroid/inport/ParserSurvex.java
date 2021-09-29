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

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.common.ExtendType;
import com.topodroid.common.LegType;
import com.topodroid.DistoX.TDUtil;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

// TODO this class can be made extend ImportParser
//
// units, calibrate, sd supported quantities:
//   length tape bearing compass gradient clino counter depth x y z declination

class ParserSurvex extends ImportParser
{
  // final static private String EMPTY = "";

  // private Stack< ParserSurvexState > mStates; // states stack (LIFO)
  // private void pushState( ParserSurvexState state )
  // {
  //   mStates.push( state );
  // }
  // private ParserSurvexState popState()
  // {
  //   return mStates.pop();
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
  private int jLeft  = -1;
  private int jRight = -1;
  private int jUp    = -1;
  private int jDown  = -1;

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
    mName = extractName( filename );
    fixes    = new ArrayList<>();
    stations = new ArrayList<>();
    aliases  = new HashMap<>();
    // mStates  = new Stack< ParserSurvexState >();
    ParserSurvexState state = new ParserSurvexState("."); // root of the linked list of states
    readFile( isr, filename, state );
    checkValid();
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
  private void readFile( InputStreamReader isr, String filename, ParserSurvexState state ) throws ParserException
  {
    boolean in_station = true;
    String from = null;
    String to;

    float len=0, ber=0, cln=0;

    // String path = basepath;   // survey pathname(s)
    int ks = 0;               // survey index
    int ksmax = 20;
    int[] survey_pos = new int[ksmax]; // current survey pos in the pathname

    Pattern pattern = Pattern.compile( "\\s+" );

    try {
      String dirname = "./";
      int i = filename.lastIndexOf('/');
      if ( i > 0 ) dirname = filename.substring(0, i+1);
      // System.out.println("readFile dir " + dirname + " filename " + filename );
      // TDLog.Log( TDLog.LOG_THERION, "reading file " + filename + " dir " + dirname );
      // TDLog.Log( TDLog.LOG_IO, "import read Survex file <" + filename + ">" );

      BufferedReader br = getBufferedReader( isr, filename );
      String line = nextLine( br );
      while ( line != null ) {
        // TDLog.v( "Parser Survex " + state.in_survey + " " + state.in_centerline + " " + state.in_data + " : " + line );
        line = line.trim();
        int pos = line.indexOf( ';' );
        if ( pos >= 0 ) {
          line = line.substring( 0, pos );
        }
        if ( line.length() > 0 ) {
          String[] vals = pattern.split(line); // line.split( "\\s+" );
          int vals_len = vals.length;
          if ( vals_len == 1 ) {
	    if ( vals[0].startsWith("*") ) {
	        String cmd = vals[0].substring(1);
	        if ( cmd.equals("solve" ) ) {
                // ignore
	      } else if ( cmd.equals("data" ) ) { // data default
                state.setDataDefault();
	        resetJIndices();
	      }
	    } else { 
              // interleaved station
	      if ( state.interleaved && in_station ) { // read station
                to = checkAlias( ParserUtil.applyCase( state.mCase, vals[0] ) );
		if ( from != null ) { // add shot
                  shots.add( new ParserShot( from, to, len, ber, cln, 0.0f,
                                         ExtendType.EXTEND_RIGHT, LegType.NORMAL, state.mDuplicate, state.mSurface, false, "" ) );
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
		// } else {
                //   TDLog.Log(  , "survex parser: unsupported alias " + vals[1] );
		}
              } else if ( cmd.equals("include") ) {
                // ignore
              } else if ( cmd.equals("copyright") ) {
                // ignore
              } else if ( cmd.equals("entrance") ) {
                // ignore
              } else if ( cmd.equals("export") ) {
                // ignore
              } else if ( cmd.equals("infer") ) {
                // ignore
              } else if ( cmd.equals("instrument") ) {
                // ignore
              } else if ( cmd.equals("prefix") ) {
                // ignore
              } else if ( cmd.equals("ref") ) {
                // ignore
              } else if ( cmd.equals("require") ) {
                // ignore
              } else if ( cmd.equals("sd") ) {
                // ignore
              } else if ( cmd.equals("truncate") ) {
                // ignore
              } else if ( cmd.equals("equate") ) {
                // TODO
              } else if ( cmd.equals("case") ) {
                // TODO
              } else if ( cmd.equals("set") ) {
                // set blank x09x20
		// set decimal ,
                // TODO
              } else if ( cmd.equals("default") ) {
                // deafult calibrate|data|units|all
		if ( vals_len > 1 ) {
		  String what = vals[1].toLowerCase();
		  if ( what.equals("calibrate") ) {
                    state.setCalibrateDefault();
		  } else if ( what.equals("data") ) {
                    state.setDataDefault();
		  } else if ( what.equals("units") ) {
                    state.setUnitsDefault();
		  } else if ( what.equals("all") ) {
                    state.setCalibrateDefault();
                    state.setDataDefault();
                    state.setUnitsDefault();
		  }
		}
              } else if ( cmd.equals("begin") ) {
                // parse "*begin" name
                state = new ParserSurvexState( state, ( (vals_len > 1)? vals[1] : null ) );

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
              } else if ( cmd.equals("team") ) {
                mTeam += TDUtil.concat( vals, 1 );
              } else if ( cmd.equals("calibrate") ) {
                // calibrate <quantities> <zero_error> [<scale>]
                // calibrate <quantities> <zero_error> <zero_units> [<scale>]
		// calibrate default
		if ( vals_len > 1 ) {
		  if ( vals[1].toLowerCase().equals("default") ) {
                    state.setCalibrateDefault();
		  } else {
		    boolean clen = false;
                    boolean cber = false;
                    boolean ccln = false;
	            int k = 1;
                    for ( ; k<vals_len - 1; ++k ) {
                      String what = vals[k].toLowerCase();
                      if ( what.equals("length")  || what.equals("tape") )  { clen = true; }
                      else if ( what.equals("compass") || what.equals("bearing") )  { cber = true; }
                      else if ( what.equals("clino")   || what.equals("gradient") ) { ccln = true; }
                      else break;
                    }
                    float zero  =  0.0f;
                    float units = -1.0f;
                    float scale =  1.0f;
                    if( k<vals_len ) {
	              try { // try to read the "scale" float (next val)
                        zero = Float.parseFloat( vals[k] );
	                ++k;
		        if ( k + 2 == vals_len ) {
	                  try { // try to read the "zero" float (next val)
                            units  = Float.parseFloat( vals[k] );
	                    ++k;
	                    break;
                          } catch ( NumberFormatException e ) { }
		        }
		        if ( k + 1 == vals_len ) {
	                  try { // try to read the "zero" float (next val)
                            scale  = Float.parseFloat( vals[k] );
	                    break;
                          } catch ( NumberFormatException e ) { }
		        }
                      } catch ( NumberFormatException e ) { }
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
                  }
                }
              } else if ( cmd.equals("units") ) {
                // units quantities [factor] unit
		// units default
		if ( vals_len > 1 ) {
                  if ( vals[1].toLowerCase().equals("default") ) {
                    state.setUnitsDefault();
		  } else {
                    boolean ulen = false;
                    boolean uber = false;
                    boolean ucln = false;
                    boolean uleft  = false;
                    boolean uright = false;
                    boolean uup    = false;
                    boolean udown  = false;
		    int k = 1;
                    for ( ; k<vals_len - 1; ++k ) {
                      if ( vals[k].equals("length")  || vals[k].equals("tape") ) { ulen = true; }
		      else if ( vals[k].equals("compass") || vals[k].equals("bearing") ) { uber = true; }
		      else if ( vals[k].equals("clino")   || vals[k].equals("gradient") ) { ucln = true; }
		      else break;
                    }
                    float factor = 1.0f;
		    if ( k + 2 == vals_len ) {
                      try {
                        factor = Float.parseFloat( vals[k] );
		        ++k;
                      } catch ( NumberFormatException e ) {
                        TDLog.Debug( "survex parser: units without factor " + line ); // this is OK
                      }
		    }
		    if ( k + 1 == vals_len ) {
                      if ( ulen || uleft || uright || uup || udown ) {
                        float l = factor * ParserUtil.parseLengthUnit( vals[k] );
                        if ( ulen ) state.mUnitLen = l;
                      } 
                      if ( uber || ucln ) {
                        float a = factor * ParserUtil.parseAngleUnit( vals[k] );
                        if ( uber ) state.mUnitBer = a;
                        if ( ucln ) state.mUnitCln = a;
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
	          } else {
                    try {
                      float declination = Float.parseFloat( vals[1] );
                      if ( vals_len > 2 ) {
                        declination *= ParserUtil.parseAngleUnit( vals[2] );
                      }
                      state.mDeclination = declination;
                      if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                    } catch ( NumberFormatException e ) {
                      TDLog.Error( "survex parser error: declination " + line );
	            }
                  }      
                }      
              } else if ( cmd.equals("flags") ) {
                int k = 1;
                if ( k < vals_len ) {
	          boolean val = true;
                  String what = vals[k].toLowerCase();
                  if ( what.equals("not") ) {
                    val = false;
	            ++k;
	          }
	          if ( k < vals_len ) {
                    what = vals[k].toLowerCase();
                    if ( what.equals("duplicate") ) {
                      state.mDuplicate = val;
	            } else if ( what.equals("splay") ) {
                      state.mSplay = val;
                    } else if ( what.equals("surface") ) {
                      state.mSurface = val;
                    }
                  }
                }
              } else if ( cmd.equals("cs") ) { 
                // TODO cs
              } else if ( cmd.equals("fix") ) { // ***** fix station east north Z (ignored std-dev's)
                if ( vals_len > 4 ) {
                  String name = checkAlias( ParserUtil.applyCase( state.mCase, vals[1] ) );
                  try {
	            fixes.add( new Fix( name,
                                        Float.parseFloat( vals[2] ),
                                        Float.parseFloat( vals[3] ),
                                        Float.parseFloat( vals[4] ) ) );
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "survex parser error: fix " + line );
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
	        String what = vals[1].toLowerCase();
                if ( what.equals("normal") ) {
                  state.data_type = ParserUtil.DATA_NORMAL;
	          if ( setJIndices( vals, vals_len ) ) {
                    state.interleaved = true;
		    in_station = true;
		    from = null;
		    len = ber = cln = 0;
		  } else {
                    state.interleaved = false;
		  }
                } else if ( what.equals("topofil") ) {
                  state.data_type = ParserUtil.DATA_TOPOFIL;
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
                } else if ( what.equals("nosurvey") ) {
                  state.data_type = ParserUtil.DATA_NOSURVEY;
                } else { // "default"
                  state.data_type = ParserUtil.DATA_DEFAULT;
	          resetJIndices();
                }
              } else if ( cmd.equals("end") ) {
                // end
	        // end <survey>
	        //
                // state = popState();
                if ( state.mParent != null ) state = state.mParent;
	        if ( ks > 0 ) {
                  --ks;
                } else {
                  TDLog.Error("Parser Survex: endsurvey out of survey");
	        }
                // path = path.substring(survey_pos[ks]); // return to previous survey_pos in path
	      }
            } else {
	      if ( state.interleaved ) {
		if ( in_station ) {
                  to = checkAlias( ParserUtil.applyCase( state.mCase, vals[0] ) );
		  if ( from != null ) { // add shot
                    shots.add( new ParserShot( from, to, len, ber, cln, 0.0f,
                                           ExtendType.EXTEND_RIGHT, LegType.NORMAL, state.mDuplicate, state.mSurface, false, "" ) );
		  }
		  from = to;
                  in_station = false;
		} else {
                  try {
                    len  = Float.parseFloat( vals[jLength] );
                    ber  = Float.parseFloat( vals[jCompass] );
                    cln  = Float.parseFloat( vals[jClino] );

	            float zLen = state.mZeroLen;
	            float sLen = state.mScaleLen * state.mUnitLen;

                    len = len * sLen - zLen;
                    ber = ber * state.mScaleBer * state.mUnitBer - state.mZeroBer;
                    if ( mApplyDeclination ) ber += state.mDeclination;
	            // if ( ber < 0 ) { ber += 360; } else if ( ber >= 360 ) { ber -= 360; }
                    ber = TDMath.in360( ber );
                    cln = cln * state.mScaleCln * state.mUnitCln - state.mZeroCln;

                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "survex parser error: data " + line );
                  }

                  in_station = true;
		}
	      } else {
	        if ( vals_len >= 5 ) {
                  // data line
                  try {
                      from = checkAlias( ParserUtil.applyCase( state.mCase, vals[jFrom] ) );
                    to   = checkAlias( ParserUtil.applyCase( state.mCase, vals[jTo] ) );
                    len  = Float.parseFloat( vals[jLength] );
                    ber  = Float.parseFloat( vals[jCompass] );
                    cln  = Float.parseFloat( vals[jClino] );

	            // measure = read * scale - zero;
	            float zLen = state.mZeroLen;
	            float sLen = state.mScaleLen * state.mUnitLen;

                    len = len * sLen - zLen;
                    ber = ber * state.mScaleBer * state.mUnitBer - state.mZeroBer;
                    if ( mApplyDeclination ) ber += state.mDeclination;
	            // if ( ber < 0 ) { ber += 360; } else if ( ber >= 360 ) { ber -= 360; }
                    ber = TDMath.in360( ber );
                    cln = cln * state.mScaleCln * state.mUnitCln - state.mZeroCln;

                    float dist, b;
                    if ( jLeft >= 0 && jLeft < vals_len) {
                      dist = Float.parseFloat( vals[jLeft] ) * sLen - zLen;
                      // b = ber - 90; if ( b < 0 ) b += 360;
                      b = TDMath.in360( ber - 90 );
                      shots.add( new ParserShot( from, TDString.EMPTY,
                                 dist, b, 0, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                    }
                    if ( jRight >= 0 && jRight < vals_len) {
                      dist = Float.parseFloat( vals[jRight] ) * sLen - zLen;
                      // b = ber + 90; if ( b >= 360 ) b -= 360;
                      b = TDMath.add90( ber );
                      shots.add( new ParserShot( from, TDString.EMPTY,
                                 dist, b, 0, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                    }
                    if ( jUp >= 0 && jUp < vals_len) {
                      dist = Float.parseFloat( vals[jUp] ) * sLen - zLen;
                      shots.add( new ParserShot( from, TDString.EMPTY,
                                 dist, 0, 90, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                    }
                    if ( jDown >= 0 && jDown < vals_len) {
                      dist = Float.parseFloat( vals[jDown] ) * sLen - zLen;
                      shots.add( new ParserShot( from, TDString.EMPTY,
                                 dist, 0, -90, 0.0f, ExtendType.EXTEND_UNSET, LegType.XSPLAY, state.mDuplicate, state.mSurface, false, "" ) );
                    }

                    // TODO add shot
                    if ( to.equals("-") || to.equals(".") ) { // splay shot
                      // FIXME splays
                      shots.add( new ParserShot( from, TDString.EMPTY,
                                            len, ber, cln, 0.0f,
                                            ExtendType.EXTEND_UNSET, LegType.NORMAL, state.mDuplicate, state.mSurface, false, "" ) );
                    } else {
                      // TDLog.v( "Parser Survex add shot " + from + " -- " + to);
                      shots.add( new ParserShot( from, to,
                                           len, ber, cln, 0.0f,
                                           ExtendType.EXTEND_RIGHT, LegType.NORMAL, state.mDuplicate, state.mSurface, false, "" ) );
                    }
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "survex parser error: data " + line );
                  }
                }
                // FIXME other data types
              }
            }            
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      throw new ParserException();
    }

    if ( mDate == null ) mDate = TDUtil.currentDate();
    if ( mTitle == null ) mTitle = TDString.EMPTY;
    TDLog.Log( TDLog.LOG_THERION, "Parser Survex shots "+ shots.size() +" splays "+ splays.size() +" fixes "+  fixes.size() );
    // TDLog.v( "Parser Survex shots "+ shots.size() + " splays "+ splays.size() +" fixes "+  fixes.size() );
  }

  private boolean setJIndices(String[] vals, int vals_len)
  {
    jFrom = jTo = jLength = jCompass = jClino = -1;
    jLeft = jUp = jRight  = jDown = -1;
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
    return false;
  }

  private void resetJIndices()
  {
     jFrom    = 2;
     jTo      = 3;
     jLength  = 4;
     jCompass = 5;
     jClino   = 6;
     jLeft = jUp = jRight  = jDown = -1;
  }

  private String checkAlias( String name )
  {
    if ( name.equals(".") || name.equals("..") || name.equals("-") ) return "";
    if ( aliases.containsKey( name ) ) return aliases.get( name );
    return name;
  }
}
