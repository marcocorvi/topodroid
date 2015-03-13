/** @file TherionParser.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Therion parser
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 * 20120606 created (adapted from Cave3D to handle single file, only shots)
 * 20121212 units (m,cm,ft,y) (deg, grad)
 * 20130103 improved support for therion syntax
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
// import java.io.StringWriter;
// import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;


// import android.util.Log;

public class TherionParser
{
  public String mName = null;  // survey name
  public String mDate = null;  // survey date
  public String mTeam = "";
  public String mTitle = "";
  public float  mDeclination = 0.0f; // one-survey declination
  private boolean mApplyDeclination = false;
 
  Stack< TherionParserState > mStates; // states stack (LIFO)

  void pushState( TherionParserState state )
  {
    mStates.push( state );
  }

  TherionParserState popState() 
  {
    return mStates.pop();
  }


  /** fix station:
   * fix stations are supposed to be referred to the same coord system
   */
  class Fix 
  {
    // private CS cs;
    String name;
    float e, n, z; // north east, vertical (upwards)

    public Fix( String nm, float e0, float n0, float z0 )
    {
      name = nm;
      e = e0;
      n = n0;
      z = z0;
    }
  }

  private ArrayList< Fix > fixes;
  private ArrayList< ParserShot > shots;   // centerline shots
  private ArrayList< ParserShot > splays;  // splay shots

  public int getShotNumber()    { return shots.size(); }
  public int getSplayNumber()   { return splays.size(); }

  public ArrayList< ParserShot > getShots() { return shots; }
  public ArrayList< ParserShot > getSplays() { return splays; }


  public TherionParser( String filename, boolean apply_declination ) throws ParserException
  {
    fixes  = new ArrayList< Fix >();
    shots  = new ArrayList< ParserShot >();
    splays = new ArrayList< ParserShot >();
    mStates = new Stack< TherionParserState >();
    mApplyDeclination = apply_declination;
    TherionParserState state = new TherionParserState();
    readFile( filename, "", state );
  }

  private String nextLine( BufferedReader br ) throws IOException
  {
    StringBuilder ret = new StringBuilder();
    {
      String line = br.readLine();
      if ( line == null ) return null; // EOF
      while ( line != null && line.endsWith( "\\" ) ) {
        ret.append( line.replace( '\\', ' ' ) ); // FIXME
        line = br.readLine();
      }
      if ( line != null ) ret.append( line );
    }
    return ret.toString();
  }

  /** read input file
   * @param filename name of the file to parse
   * @param basepath survey pathname base
   * @param usd   use survey declination
   * @param sd    survey decliunation
   * @param ul units of length (as multiple of 1 meter)
   * @param ub units of bearing (as multiple of 1 degree)
   * @param uc units of clino
   */
  private void readFile( String filename, String basepath, TherionParserState state )
                       throws ParserException
  {
    String path = basepath;   // survey pathname(s)
    int[] survey_pos = new int[50]; // current survey pos in the pathname FIXME max 50 levels
    int ks = 0;                     // survey index

    int jFrom    = 0;
    int jTo      = 1;
    int jLength  = 2;
    int jCompass = 3;
    int jClino   = 4;

    Pattern pattern = Pattern.compile( "\\s+" );

    try {
      String dirname = "./";
      int i = filename.lastIndexOf('/');
      if ( i > 0 ) dirname = filename.substring(0, i+1);
      // System.out.println("readFile dir " + dirname + " filename " + filename );
      // TopoDroidLog.Log( TopoDroidLog.LOG_THERION, "reading file " + filename + " dir " + dirname );

      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = nextLine( br );
      while ( line != null ) {
        // TopoDroidLog.Log( TopoDroidLog.LOG_THERION, "TH " + line );
        // Log.v( TopoDroidApp.TAG, "TH " + state.in_survey + " " + state.in_centerline + " " + state.in_data + " : " + line );
        line = line.trim();
        int pos = line.indexOf( '#' );
        if ( pos >= 0 ) {
          line = line.substring( 0, pos );
        }
        if ( line.length() > 0 ) {
          String[] vals = pattern.split(line); // line.split( "\\s+" );
          // int vals_len = 0;
          // for ( int k=0; k<vals.length; ++k ) {
          //   vals[vals_len] = vals[k];
          //   if ( vals[vals_len].length() > 0 ) {
          //     ++ vals_len;
          //   }
          // }
          // Log.v( TopoDroidApp.TAG, "vals " + vals.length + " " + vals_len );
          int vals_len = vals.length;
          if ( vals_len > 0 ) {
            String cmd = vals[0];
            
            if ( cmd.equals("encoding" ) ) { 
              // ignore
            } else if ( cmd.equals("import") ) {
              // ignore
            } else if ( ! state.in_centerline && cmd.equals("grade") ) {
              // ignore
            } else if ( cmd.equals("revise") ) {
              // ignore
            } else if ( cmd.equals("join") ) {
              // ignore
            } else if ( cmd.equals("input") ) { // ignore
              // int j = 1;
              // while ( vals[j] != null ) {
              //   if ( vals[j].length() > 0 ) {
              //     filename = vals[j];
              //     if ( filename.endsWith( ".th" ) ) {
              //       readFile( dirname + '/' + filename, 
              //           path,
              //           use_survey_declination, survey_declination,
              //           units_len, units_ber, units_cln );
              //     }
              //     break;
              //   }
              // }
            } else if ( cmd.equals("surface") ) {
              // TODO check not already in_surface
              state.in_surface = true;
            } else if ( cmd.equals("map") ) {
              // TODO check not already in_map
              state.in_map = true;
            } else if ( cmd.equals("scrap") ) {
              // TODO check not already in_scrap
              state.in_scrap = true;
            } else if ( state.in_scrap && cmd.equals("line") ) {
              // TODO check not already in_line
              state.in_line = true;
            } else if ( state.in_scrap && cmd.equals("area") ) {
              // TODO check not already in_area
              state.in_area = true;

            } else if ( state.in_line && cmd.equals("endline") ) { 
              state.in_line = false;
            } else if ( state.in_area && cmd.equals("endarea" ) ) {
              state.in_area = false;
            } else if ( state.in_scrap && cmd.equals("endscrap" ) ) {
              state.in_scrap = false;
            } else if ( state.in_map && cmd.equals("endmap" ) ) {
              state.in_map = false;
            } else if ( state.in_surface && cmd.equals("endsurface" ) ) {
              state.in_surface = false;
            } else if ( state.in_map || state.in_surface || state.in_scrap || state.in_line || state.in_area ) {
              // ignore

            } else if ( cmd.equals("survey") ) {
              survey_pos[ks] = path.length(); // set current survey pos in pathname
              path = path + "." + vals[1];    // add survey name to path
              ++ks;
              pushState( state );
              state = new TherionParserState( state );
              state.mSurveyLevel ++;
              state.in_survey= true;

              // parse survey id
              if ( mName == null ) {
                mName = vals[1];
              }

              // parse survey options
              for ( int j=2; j<vals_len; ++j ) {
                if ( vals[j].equals("-declination") && j+1 < vals_len ) {
                  try {
                    state.mDeclination = Float.parseFloat( vals[j+1] );
                    ++j;
                    if ( j+1 < vals_len ) { // check for units
                      state.mDeclination *= parseAngleUnit( vals[j+1] );
                      ++j;
                    }
                    if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "therion parser error: -declination " + line );
                  }
                } else if ( vals[j].equals("-title") && j+1 < vals_len ) {
                  for ( ++j; j<vals_len; ++j ) {
                    if ( vals[j].length() == 0 ) continue;
                    if ( vals[j].startsWith("\"") ) {
                      mTitle = vals[j].substring(1);
                      for ( ++j; j<vals_len; ++j ) {
                        if ( vals[j].length() == 0 ) continue;
                        if ( vals[j].endsWith( "\"" ) ) {
                          mTitle += " " + vals[j].substring(0, vals[j].length()-1);
                          break;
                        } else {
                          mTitle += " " + vals[j];
                        }
                      }
                    } else {
                      mTitle = vals[j];
                    }
                    break;
                  }
                }
              }

            } else if ( state.in_centerline ) {
              if ( cmd.equals("endcenterline") || cmd.equals("endcentreline") ) {
                // state.in_data = false;
                // state.in_centerline = false;
                state = popState();

              } else if ( cmd.equals("date") ) {
                String date = vals[1];
                if ( mDate == null ) mDate = date; // save centerline date
              } else if ( cmd.equals("team") ) { 
                for ( int j = 1; j < vals_len; ++j ) {
                  mTeam +=  " " + vals[j];
                }
              // } else if ( cmd.equals("explo-date") ) {
              // } else if ( cmd.equals("explo-team") ) {
              // } else if ( cmd.equals("instrument") ) {
              } else if ( cmd.equals("calibrate") ) {
                boolean clen = false;
                boolean cber = false;
                boolean ccln = false;
                for ( int k=1; k<vals_len - 1; ++k ) {
                  if ( vals[k].equals("length") || vals[k].equals("tape") ) clen = true;
                  if ( vals[k].equals("compass") || vals[k].equals("bearing") ) cber = true;
                  if ( vals[k].equals("clino") || vals[k].equals("gradient") ) ccln = true;
                }
                float zero = 0.0f;
                float scale = 1.0f;
                try {
                  scale = Float.parseFloat( vals[vals_len-1] );
                  zero  = Float.parseFloat( vals[vals_len-2] );
                } catch ( NumberFormatException e ) {
                  TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "therion parser error: scale/zero " + line );
                  zero  = scale;
                }
                if ( clen ) {
                  state.mZeroLen = zero;
                  state.mScaleLen = scale;
                }
                if ( cber ) {
                  state.mZeroBer = zero;
                  state.mScaleBer = scale;
                }
                if ( ccln ) {
                  state.mZeroCln = zero;
                  state.mScaleCln = scale;
                }
              } else if ( cmd.equals("units") ) { // units quantity_list [factor] unit
                boolean ulen = false;
                boolean uber = false;
                boolean ucln = false;
                for ( int k=1; k<vals_len - 1; ++k ) {
                  if ( vals[k].equals("length") || vals[k].equals("tape") ) ulen = true;
                  if ( vals[k].equals("compass") || vals[k].equals("bearing") ) uber = true;
                  if ( vals[k].equals("clino") || vals[k].equals("gradient") ) ucln = true;
                }
                float factor = 1.0f;
                try {
                  factor = Float.parseFloat( vals[vals_len-2] );
                } catch ( NumberFormatException e ) {
                  TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "therion parser error: units " + line );
                }
                if ( ulen ) {
                  state.mUnitLen = factor * parseLengthUnit( vals[vals_len-1] );
                } 
                if ( uber ) {
                  state.mUnitBer = factor * parseAngleUnit( vals[vals_len-1] );
                }
                if ( ucln ) {
                  state.mUnitCln = factor * parseAngleUnit( vals[vals_len-1] );
                }
              } else if ( cmd.equals("sd") ) {
                // ignore
              } else if ( cmd.equals("grade") ) {
                // ignore
              } else if ( cmd.equals("declination") ) { 
                if ( 1 < vals_len ) {
                  try {
                    float declination = Float.parseFloat( vals[1] );
                    if ( 2 < vals_len ) {
                      declination *= parseAngleUnit( vals[2] );
                    }
                    state.mDeclination = declination;
                    if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "therion parser error: declination " + line );
                  }
                }      
              } else if ( cmd.equals("infer") ) {
                // ignore
              } else if ( cmd.equals("instrument") ) {
                // ignore
              } else if ( cmd.equals("mark") ) {
                // ignore
              } else if ( cmd.equals("flags") ) {
                if ( vals_len >= 2 ) {
                  if ( vals[1].startsWith("dup") || vals[1].startsWith("splay") ) {
                    state.mDuplicate = true;
                  } else if ( vals[1].startsWith("surf") ) {
                    state.mSurface = true;
                  } else if ( vals[1].equals("not") && vals_len >= 3 ) {
                    if ( vals[2].startsWith("dup") || vals[2].startsWith("splay") ) {
                      state.mDuplicate = false;
                    } else if ( vals[2].startsWith("surf") ) {
                      state.mSurface = false;
                    }
                  }
                }
              } else if ( cmd.equals("station") ) {
                // ignore: station <station> <comment>
              } else if ( cmd.equals("cs") ) { 
                // TODO cs
              } else if ( cmd.equals("fix") ) { // ***** fix station east north Z (ignored std-dev's)
                if ( vals_len > 4 ) {
                  String name;
                  int idx = vals[1].indexOf('@');
                  if ( idx > 0 ) {
                    name = vals[1].substring(0,idx); // + "@" + path + "." + vals[1].substring(idx+1);
                  } else {
                    name = vals[1]; // + "@" + path;
                  }
                  try {
	            fixes.add( new Fix( name,
                                        Float.parseFloat( vals[2] ),
                                        Float.parseFloat( vals[3] ),
                                        Float.parseFloat( vals[4] ) ) );
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "therion parser error: fix " + line );
                  }
                }
              } else if ( cmd.equals("equate") ) {
                if ( vals_len > 2 ) {
                  String from, to;
                  int idx = vals[1].indexOf('@');
                  if ( idx > 0 ) {
                    from = vals[1].substring(0,idx); // + "@" + path + "." + vals[1].substring(idx+1);
                  } else {
                    from = vals[1]; // + "@" + path;
                  }
                  for ( int j=2; j<vals_len; ++j ) {
                    idx = vals[j].indexOf('@');
                    if ( idx > 0 ) {
                      to = vals[j].substring(0,idx); // + "@" + path + "." + vals[j].substring(idx+1);
                    } else {
                      to = vals[j]; // + "@" + path;
                    }
                    shots.add( new ParserShot( state.mPrefix + from + state.mSuffix, state.mPrefix + to + state.mSuffix,
                                         0.0f, 0.0f, 0.0f, 0.0f, 0, true, false, "" ) );
                  }
                }
              } else if ( cmd.startsWith("explo") ) { // explo-date explo-team
                // ignore
              } else if ( cmd.equals("break") ) {
                // ignore
              } else if ( cmd.equals("infer") ) {
                // ignore

              } else if ( cmd.equals("group") ) {
                pushState( state );
                state = new TherionParserState( state );
              } else if ( cmd.equals("endgroup") ) {
                state = popState();

              } else if ( cmd.equals("walls") ) {
                // ignore
              } else if ( cmd.equals("vthreshold") ) {
                // ignore
              } else if ( cmd.equals("extend") ) { 
                if ( vals_len == 2 ) {
                  state.mExtend = parseExtend( vals[1], state.mExtend );
                } else { // not implemented "extend value station [station]
                }
              } else if ( cmd.equals("station_names") ) {
                state.mPrefix = "";
                state.mSuffix = "";
                if ( vals_len > 1 ) {
                  int off = vals[1].indexOf( '"' );
                  if ( off >= 0 ) {
                    int end = vals[1].lastIndexOf( '"' );
                    state.mPrefix = vals[1].substring(off+1, end );
                  }
                  if ( vals_len > 2 ) {
                    off = vals[2].indexOf( '"' );
                    if ( off >= 0 ) {
                      int end = vals[2].lastIndexOf( '"' );
                      state.mSuffix = vals[2].substring(off+1, end );
                    }
                  }
                }
              } else if ( cmd.equals("data") ) {
                // data normal from to length compass clino ...
                if ( vals[1].equals("normal") ) {
                  jFrom = jTo = jLength = jCompass = jClino = -1;
                  int j0 = 0;
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
                    } else {
                      ++j0;
                    }
                  }
                  state.in_data = (jFrom >= 0) && (jTo >= 0) && (jLength >= 0) && (jCompass >= 0) && (jClino >= 0);
                // TODO other style syntax
                // } else if ( vals[1].equals("topofil") ) {
                // } else if ( vals[1].equals("diving") ) {
                // } else if ( vals[1].equals("cartesian") ) {
                // } else if ( vals[1].equals("cylpolar") ) {
                // } else if ( vals[1].equals("dimensions") ) {
                // } else if ( vals[1].equals("nosurvey") ) {
                }
              } else if ( state.in_data && vals_len >= 5 ) {
                // FIXME
                try {
                  String from = vals[jFrom];
                  String to   = vals[jTo];
                  float len  = Float.parseFloat( vals[jLength] );
                  float ber  = Float.parseFloat( vals[jCompass] );
                  float cln  = Float.parseFloat( vals[jClino] );

                  len = state.mZeroLen + (len*state.mUnitLen) / state.mScaleLen;
                  if ( mApplyDeclination ) {
                    ber = state.mZeroBer + (ber*state.mUnitBer + state.mDeclination) / state.mScaleBer;
                  } else {
                    ber = state.mZeroBer + (ber*state.mUnitBer) / state.mScaleBer;
                  }
                  cln = state.mZeroCln + (cln*state.mUnitCln) / state.mScaleCln;

                  // TODO add shot
                  if ( to.equals("-") || to.equals(".") ) { // splay shot
                    // from = from + "@" + path;
                    splays.add( new ParserShot( state.mPrefix + from + state.mSuffix, null,
                                          len, ber, cln, 0.0f,
                                          state.mExtend, state.mDuplicate, state.mSurface, "" ) );
                  } else {
                    // from = from + "@" + path;
                    // to   = to + "@" + path;
                    // Log.v( TopoDroidApp.TAG, "add shot " + from + " -- " + to);
                    shots.add( new ParserShot( state.mPrefix + from + state.mSuffix, state.mPrefix + to + state.mSuffix,
                                         len, ber, cln, 0.0f,
                                         state.mExtend, state.mDuplicate, state.mSurface, "" ) );
                  }
                } catch ( NumberFormatException e ) {
                  TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "therion parser error: data " + line );
                }
              }            
            } else if ( cmd.equals("centerline") || cmd.equals("centreline") ) {
              pushState( state );
              state = new TherionParserState( state );
              state.in_centerline = true;
              state.in_data = false;
            } else if ( cmd.equals("endsurvey") ) {
              state = popState();
              --ks;
              path = path.substring(survey_pos[ks]); // return to previous survey_pos in path
              state.in_survey = ( ks > 0 );
            }
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      // TODO
      throw new ParserException();
    }
    if ( mDate == null ) {
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      mDate = sdf.format( new Date() );
    }
    TopoDroidLog.Log( TopoDroidLog.LOG_THERION, "TherionParser shots "+ shots.size() +" splays "+ splays.size() +" fixes "+  fixes.size() );
    // Log.v( TopoDroidApp.TAG, "TherionParser shots "+ shots.size() + " splays "+ splays.size() +" fixes "+  fixes.size() );
  }

  float parseAngleUnit( String unit ) 
  {
    // not handled "percent"
    if ( unit.startsWith("min") ) return 1/60.0f;
    if ( unit.startsWith("grad") ) return (float)TopoDroidUtil.GRAD2DEG;
    if ( unit.startsWith("mil") ) return (float)TopoDroidUtil.GRAD2DEG;
    // if ( unit.startsWith("deg") ) return 1.0f;
    return 1.0f;
  }

  float parseLengthUnit( String unit ) 
  {
    if ( unit.startsWith("c") ) return 0.01f; // cm centimeter
    if ( unit.startsWith("f") ) return (float)TopoDroidUtil.FT2M; // ft feet
    if ( unit.startsWith("i") ) return (float)TopoDroidUtil.IN2M; // in inch
    if ( unit.startsWith("milli") || unit.equals("mm") ) return 0.001f; // mm millimeter
    if ( unit.startsWith("y") ) return (float)TopoDroidUtil.YD2M; // yd yard
    // if ( unit.startsWith("m") ) return 1.0f;
    return 1.0f;
  }

  int parseExtend( String extend, int old_extend )
  {
    // skip: hide, start
    if ( extend.equals("hide") || extend.equals("start") ) {
      return old_extend;
    }
    if ( extend.equals("left") || extend.equals("reverse") ) {
      return DistoXDBlock.EXTEND_LEFT;
    } 
    if ( extend.startsWith("vert") ) {
      return DistoXDBlock.EXTEND_VERT;
    }
    if ( extend.startsWith("ignore") ) {
      return DistoXDBlock.EXTEND_IGNORE;
    }
    // if ( extend.equals("right") || extend.equals("normal") ) {
    //   return DistoXDBlock.EXTEND_RIGHT;
    // } 
    return DistoXDBlock.EXTEND_RIGHT;
  }
}
