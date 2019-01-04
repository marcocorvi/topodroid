/* @file ParserTherion.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Therion parser
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
// import java.util.Stack;
import java.util.regex.Pattern;

// import android.util.Log;

// TODO this class can be made extend ImportParser
//
// units, calibrate, sd supported quantities:
//   length tape bearing compass gradient clino counter depth x y z position easting dx northing dy altitude dz

class ParserTherion
{
  // final static private String EMPTY = "";

  final static private int DATA_NONE       = 0;
  final static private int DATA_NORMAL     = 1;
  final static private int DATA_TOPOFIL    = 2;
  final static private int DATA_CARTESIAN  = 3;
  final static private int DATA_CYLPOLAR   = 4;
  final static private int DATA_DIVING     = 5;
  final static private int DATA_DIMENSION  = 6;
  final static private int DATA_NOSURVEY   = 7;

  String mName = null;  // survey name
  String mDate = null;  // survey date
  private String mTeam = TDString.EMPTY;
  String mTitle = TDString.EMPTY;
  private float   mDeclination = 0.0f; // one-survey declination
  private boolean mApplyDeclination = false;
 
  // private Stack< ParserTherionState > mStates; // states stack (LIFO)
  // private void pushState( ParserTherionState state )
  // {
  //   mStates.push( state );
  // }
  // private ParserTherionState popState()
  // {
  //   return mStates.pop();
  // }


  /** fix station:
   * fix stations are supposed to be referred to the same coord system
   */
  class Fix 
  {
    // private CS cs;
    String name;
    float e, n, z; // north east, vertical (upwards)

    Fix( String nm, float e0, float n0, float z0 )
    {
      name = nm;
      e = e0;
      n = n0;
      z = z0;
    }
  }

  class Station
  {
    String name;
    String comment;
    long flag;
 
    Station( String n, String c, long f )
    {
      name = n;
      comment = c;
      flag = f;
    }
  }

  private ArrayList< Fix > fixes;
  private ArrayList< Station > stations;
  private ArrayList< ParserShot > shots;   // centerline shots
  private ArrayList< ParserShot > splays;  // splay shots

  // public int getShotNumber()    { return shots.size(); }
  // public int getSplayNumber()   { return splays.size(); }

  ArrayList< ParserShot > getShots()    { return shots; }
  ArrayList< ParserShot > getSplays()   { return splays; }
  ArrayList< Station >    getStations() { return stations; }
  ArrayList< Fix >        getFixes()    { return fixes; }

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

  ParserTherion( String filename, boolean apply_declination ) throws ParserException
  {
    fixes    = new ArrayList<>();
    stations = new ArrayList<>();
    shots    = new ArrayList<>();
    splays   = new ArrayList<>();
    // mStates  = new Stack< ParserTherionState >();
    mApplyDeclination = apply_declination;
    ParserTherionState state = new ParserTherionState(); // root of the linked list of states
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

  private String extractStationName( String fullname )
  {
    int idx = fullname.indexOf('@');
    if ( idx > 0 ) {
       return fullname.substring(0,idx); // + "@" + path + "." + vals[1].substring(idx+1);
    }
    return fullname;
  }

  /** read input file
   * @param filename name of the file to parse
   * @param basepath survey pathname base
   * @param state    state of the parser
   */
  private void readFile( String filename, String basepath, ParserTherionState state )
                       throws ParserException
  {
    String path = basepath;   // survey pathname(s)
    int ks = 0;               // survey index
    int ksmax = 20;
    int[] survey_pos = new int[ksmax]; // current survey pos in the pathname

    int jFrom    = 0;
    int jTo      = 1;
    int jLength  = 2;
    int jCompass = 3;
    int jClino   = 4;
    int jLeft  = -1;
    int jRight = -1;
    int jUp    = -1;
    int jDown  = -1;

    Pattern pattern = Pattern.compile( "\\s+" );

    try {
      String dirname = "./";
      int i = filename.lastIndexOf('/');
      if ( i > 0 ) dirname = filename.substring(0, i+1);
      // System.out.println("readFile dir " + dirname + " filename " + filename );
      // TDLog.Log( TDLog.LOG_THERION, "reading file " + filename + " dir " + dirname );
      // TDLog.Log( TDLog.LOG_IO, "import read Therion file <" + filename + ">" );

      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = nextLine( br );
      while ( line != null ) {
        // TDLog.Log( TDLog.LOG_THERION, "TH " + line );
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
	      if ( ks >= ksmax ) {
		ksmax += 10;
                int[] tmp = new int[ksmax];
		for ( int k=0; k<ks; ++k ) tmp[k] = survey_pos[k];
		survey_pos = tmp;
	      }
              // pushState( state );
              state = new ParserTherionState( state );
              state.mSurveyLevel ++;
              state.in_survey= true;

              // parse survey id
              if ( mName == null ) {
                mName = vals[1];
              }

              // parse survey options
              for ( int j=2; j<vals_len; ++j ) {
                if ( vals[j].equals("-declination") && j+1 < vals_len ) {
		  if ( vals[j+1].equals("-") ) { // declination reset
                    state.mDeclination = ( state.mParent == null )? 0 : state.mParent.mDeclination;
		  } else {
                    try {
                      state.mDeclination = Float.parseFloat( vals[j+1] );
                      ++j;
                      if ( j+1 < vals_len ) { // FIXME check for units
                        state.mDeclination *= parseAngleUnit( vals[j+1] );
                        ++j;
                      }
                      if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                    } catch ( NumberFormatException e ) {
                      TDLog.Error( "therion parser error: -declination " + line );
                    }
		  }
                } else if ( vals[j].equals("-title") && j+1 < vals_len ) {
                  for ( ++j; j<vals_len; ++j ) {
                    if ( vals[j].length() == 0 ) continue;
                    if ( vals[j].startsWith("\"") ) {
                      StringBuilder sb = new StringBuilder();
                      sb.append( vals[j].substring(1) );
                      // mTitle = vals[j].substring(1);
                      for ( ++j; j<vals_len; ++j ) {
                        if ( vals[j].length() == 0 ) continue;
                        if ( vals[j].endsWith( "\"" ) ) {
                          sb.append(" ").append(vals[j].substring(0, vals[j].length()-1));
                          // mTitle += " " + vals[j].substring(0, vals[j].length()-1);
                          break;
                        } else {
                          sb.append(" ").append(vals[j] );
                          // mTitle += " " + vals[j];
                        }
                      }
                      mTitle = sb.toString();
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
                // state = popState();
                if ( state.mParent != null ) state = state.mParent;

              } else if ( cmd.equals("date") ) {
                String date = vals[1];
                if ( mDate == null ) mDate = date; // save centerline date
              } else if ( cmd.equals("team") ) {
                // StringBuilder sb = new StringBuilder();
                // for ( int j = 1; j < vals_len; ++j ) {
                //   sb.append(" ").append( vals[j] );
                //   // mTeam +=  " " + vals[j];
                // }
                // mTeam += sb.toString();
                mTeam += TopoDroidUtil.concat( vals, 1 );
              // } else if ( cmd.equals("explo-date") ) {
              // } else if ( cmd.equals("explo-team") ) {
              // } else if ( cmd.equals("instrument") ) {
              } else if ( cmd.equals("calibrate") ) {
                boolean clen = false;
                boolean cber = false;
                boolean ccln = false;
		int k = 1;
                for ( ; k<vals_len - 1; ++k ) {
                  if ( vals[k].equals("length") || vals[k].equals("tape") )     clen = true;
                  if ( vals[k].equals("compass") || vals[k].equals("bearing") ) cber = true;
                  if ( vals[k].equals("clino") || vals[k].equals("gradient") )  ccln = true;
                }
                float zero = 0.0f;
                float scale = 1.0f;
		int kk = 1;
                while ( kk<vals_len-1 ) {
		  try { // try to read the "scale" float (next val)
		    ++kk;
                    zero = Float.parseFloat( vals[kk] );
		    break;
                  } catch ( NumberFormatException e ) { }
		}
                while ( kk<vals_len-1 ) {
		  try { // try to read the "zero" float (next val)
		    ++kk;
                    scale  = Float.parseFloat( vals[kk] );
		    break;
                  } catch ( NumberFormatException e ) { }
                }

                if ( clen ) {
                  state.mZeroLen  = zero;
                  state.mScaleLen = scale;
                }
                if ( cber ) {
                  state.mZeroBer  = zero;
                  state.mScaleBer = scale;
                }
                if ( ccln ) {
                  state.mZeroCln  = zero;
                  state.mScaleCln = scale;
                }
              } else if ( cmd.equals("units") ) { // units quantity_list [factor] unit
                boolean ulen = false;
                boolean uber = false;
                boolean ucln = false;
                boolean uleft  = false;
                boolean uright = false;
                boolean uup    = false;
                boolean udown  = false;
                for ( int k=1; k<vals_len - 1; ++k ) {
                  if ( vals[k].equals("length")  || vals[k].equals("tape") )     ulen = true;
                  if ( vals[k].equals("compass") || vals[k].equals("bearing") )  uber = true;
                  if ( vals[k].equals("clino")   || vals[k].equals("gradient") ) ucln = true;
                }
                float factor = 1.0f;
                try {
                  factor = Float.parseFloat( vals[vals_len-2] );
                } catch ( NumberFormatException e ) {
                  TDLog.Debug( "therion parser: units without factor " + line ); // this is OK
                }
                if ( ulen || uleft || uright || uup || udown ) {
                  float len = factor * parseLengthUnit( vals[vals_len-1] );
                  if ( ulen )   state.mUnitLen   = len;
                } 
                if ( uber || ucln ) {
                  float angle = factor * parseAngleUnit( vals[vals_len-1] );
                  if ( uber ) state.mUnitBer = angle;
                  if ( ucln ) state.mUnitCln = angle;
                }
              } else if ( cmd.equals("sd") ) {
                // ignore
              } else if ( cmd.equals("grade") ) {
                // ignore
              } else if ( cmd.equals("declination") ) { 
                if ( 1 < vals_len ) {
		  if ( vals[1].equals("-") ) { // declination reset
                    state.mDeclination = ( state.mParent == null )? 0 : state.mParent.mDeclination;
		  } else {
                    try {
                      float declination = Float.parseFloat( vals[1] );
                      if ( 2 < vals_len ) {
                      declination *= parseAngleUnit( vals[2] );
                      }
                      state.mDeclination = declination;
                      if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                    } catch ( NumberFormatException e ) {
                      TDLog.Error( "therion parser error: declination " + line );
                    }
		  }
                }      
              } else if ( cmd.equals("instrument") ) {
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
              } else if ( cmd.equals("cs") ) { 
                // TODO cs
              } else if ( cmd.equals("mark") ) { // ***** fix station east north Z (ignored std-dev's)
                String flag_str = vals[ vals_len - 1 ];
                int flag = 0;
                if ( "painted".equals( vals[ vals_len-1 ] ) ) {
                  flag = CurrentStation.STATION_PAINTED;
                } else if ( "fixed".equals( vals[ vals_len-1 ] ) ) {
                  flag = CurrentStation.STATION_FIXED;
                }
                // Log.v("DistoX", "Therion parser: mark flag " + flag + " " + flag_str );
                if ( flag != 0 ) {
                  for ( int k=1; k<vals_len-1; ++k ) {
                    String name = extractStationName( vals[k] );
                    // Log.v("DistoX", "mark station " + name );
                    boolean must_add = true;
                    for ( Station st : stations ) if ( st.name.equals( name ) ) {
                      must_add = false;
                      st.flag = flag;
                      break;
                    }
                    if ( must_add ) stations.add( new Station( name, "", flag ) );
                  }
                }   
                
              } else if ( cmd.equals("station") ) { // ***** station name "comment"
                if ( vals_len > 2 ) {
                  String name = extractStationName( vals[1] );
                  String comment = vals[2];
                  if ( comment.startsWith( "\"" ) ) {
                    int len = comment.length();
                    StringBuilder sb = new StringBuilder();
                    sb.append( comment.substring( 1 ) );
                    for ( int kk=3; kk<vals_len; ++kk ) {
                      if ( vals[kk].endsWith("\"") ) {
                        sb.append(" ");
                        sb.append( vals[kk].substring(0, vals[kk].length()-1) );
                        break;
                      } else {
                        sb.append(" ");
                        sb.append( vals[kk] );
                      }
                    }
                    comment = sb.toString();
                  }
                  // Log.v("DistoX", "Therion parser station " + name + " comment <" + comment + ">" );
                  if ( comment.length() > 0 ) {
                    boolean must_add = true;
                    for ( Station st : stations ) if ( st.name.equals( name ) ) { 
                      must_add = false;
                      st.comment = comment;
                      break;
                    }
                    if ( must_add ) stations.add( new Station( name, comment, 0 ) );
                  }
                }
              } else if ( cmd.equals("fix") ) { // ***** fix station east north Z (ignored std-dev's)
                if ( vals_len > 4 ) {
                  String name = extractStationName( vals[1] );
                  try {
	            fixes.add( new Fix( name,
                                        Float.parseFloat( vals[2] ),
                                        Float.parseFloat( vals[3] ),
                                        Float.parseFloat( vals[4] ) ) );
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "therion parser error: fix " + line );
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
                                         0.0f, 0.0f, 0.0f, 0.0f, 0, 0, true, false, false, "" ) );
                  }
                }
              } else if ( cmd.startsWith("explo") ) { // explo-date explo-team
                // ignore
              } else if ( cmd.equals("break") ) {
                // ignore
              } else if ( cmd.equals("infer") ) {
                // ignore

              } else if ( cmd.equals("group") ) {
                // pushState( state );
                state = new ParserTherionState( state );
              } else if ( cmd.equals("endgroup") ) {
                // state = popState();
                if ( state.mParent != null ) state = state.mParent;

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
                  state.data_type = DATA_NORMAL;
                  jFrom = jTo = jLength = jCompass = jClino = -1;
                  jLeft = jUp = jRight  = jDown = -1;
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
                  state.in_data = (jFrom >= 0) && (jTo >= 0) && (jLength >= 0) && (jCompass >= 0) && (jClino >= 0);
                // TODO other style syntax
                } else if ( vals[1].equals("topofil") ) {
                  state.data_type = DATA_TOPOFIL;
                } else if ( vals[1].equals("diving") ) {
                  state.data_type = DATA_DIVING;
                } else if ( vals[1].equals("cartesian") ) {
                  state.data_type = DATA_CARTESIAN;
                } else if ( vals[1].equals("cylpolar") ) {
                  state.data_type = DATA_CYLPOLAR;
                } else if ( vals[1].equals("dimensions") ) {
                  state.data_type = DATA_DIMENSION;
                } else if ( vals[1].equals("nosurvey") ) {
                  state.data_type = DATA_NOSURVEY;
                } else {
                  state.data_type = DATA_NONE;
                }
              } else if ( state.in_data && vals_len >= 5 ) {
                if ( state.data_type == DATA_NORMAL ) {
                  try {
                    int sz = vals.length;
                    String from = vals[jFrom];
                    String to   = vals[jTo];
                    float len  = Float.parseFloat( vals[jLength] );
                    float ber  = Float.parseFloat( vals[jCompass] );
                    float cln  = Float.parseFloat( vals[jClino] );

		    // measure = (read - zero)*scale
		    float zLen = state.mZeroLen;
		    float sLen = state.mScaleLen * state.mUnitLen;

                    len = (len - zLen) * sLen;
                    ber = (ber - state.mZeroBer) * state.mScaleBer * state.mUnitBer;
                    if ( mApplyDeclination ) ber += state.mDeclination;
		    if ( ber < 0 ) { ber += 360; } else if ( ber >= 360 ) { ber -= 360; }
                    cln = (cln - state.mZeroCln) * state.mScaleCln * state.mUnitCln;

                    float dist, b;
                    if ( jLeft >= 0 && jLeft < sz ) {
                      dist = (Float.parseFloat( vals[jLeft] ) - zLen) * sLen;
                      b = ber - 90; if ( b < 0 ) b += 360;
                      shots.add( new ParserShot( state.mPrefix + from + state.mSuffix, TDString.EMPTY,
                                 dist, b, 0, 0.0f, state.mExtend, 2, state.mDuplicate, state.mSurface, false, "" ) );
                    }
                    if ( jRight >= 0 && jRight < sz ) {
                      dist = (Float.parseFloat( vals[jRight] ) - zLen) * sLen;
                      b = ber + 90; if ( b >= 360 ) b -= 360;
                      shots.add( new ParserShot( state.mPrefix + from + state.mSuffix, TDString.EMPTY,
                                 dist, b, 0, 0.0f, state.mExtend, 2, state.mDuplicate, state.mSurface, false, "" ) );
                    }
                    if ( jUp >= 0 && jUp < sz ) {
                      dist = (Float.parseFloat( vals[jUp] ) - zLen) * sLen;
                      shots.add( new ParserShot( state.mPrefix + from + state.mSuffix, TDString.EMPTY,
                                 dist, 0, 90, 0.0f, state.mExtend, 2, state.mDuplicate, state.mSurface, false, "" ) );
                    }
                    if ( jDown >= 0 && jDown < sz ) {
                      dist = (Float.parseFloat( vals[jDown] ) - zLen) * sLen;
                      shots.add( new ParserShot( state.mPrefix + from + state.mSuffix, TDString.EMPTY,
                                 dist, 0, -90, 0.0f, state.mExtend, 2, state.mDuplicate, state.mSurface, false, "" ) );
                    }

                    // TODO add shot
                    if ( to.equals("-") || to.equals(".") ) { // splay shot
                      // from = from + "@" + path;
                      // FIXME splays
                      shots.add( new ParserShot( state.mPrefix + from + state.mSuffix, TDString.EMPTY,
                                            len, ber, cln, 0.0f,
                                            state.mExtend, 0, state.mDuplicate, state.mSurface, false, "" ) );
                    } else {
                      // from = from + "@" + path;
                      // to   = to + "@" + path;
                      // Log.v( TopoDroidApp.TAG, "add shot " + from + " -- " + to);
                      shots.add( new ParserShot( state.mPrefix + from + state.mSuffix, state.mPrefix + to + state.mSuffix,
                                           len, ber, cln, 0.0f,
                                           state.mExtend, 0, state.mDuplicate, state.mSurface, false, "" ) );
                    }
                  } catch ( NumberFormatException e ) {
                    TDLog.Error( "therion parser error: data " + line );
                  }
                }
                // FIXME other data types
              }            
            } else if ( cmd.equals("centerline") || cmd.equals("centreline") ) {
              // pushState( state );
              state = new ParserTherionState( state );
              state.in_centerline = true;
              state.in_data = false;
            } else if ( cmd.equals("endsurvey") ) {
              // state = popState();
              if ( state.mParent != null ) state = state.mParent;
	      if ( ks > 0 ) {
                --ks;
              } else {
                TDLog.Error("ParserTherion: endsurvey out of survey");
	      }
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
      mDate = TopoDroidUtil.currentDate();
    }
    TDLog.Log( TDLog.LOG_THERION, "ParserTherion shots "+ shots.size() +" splays "+ splays.size() +" fixes "+  fixes.size() );
    // Log.v( TopoDroidApp.TAG, "ParserTherion shots "+ shots.size() + " splays "+ splays.size() +" fixes "+  fixes.size() );
  }

  private float parseAngleUnit( String unit )
  {
    // not handled "percent"
    if ( unit.startsWith("min") ) return 1/60.0f;
    if ( unit.startsWith("grad") ) return (float)TopoDroidUtil.GRAD2DEG;
    if ( unit.startsWith("mil") ) return (float)TopoDroidUtil.GRAD2DEG;
    // if ( unit.startsWith("deg") ) return 1.0f;
    return 1.0f;
  }

  private float parseLengthUnit( String unit )
  {
    if ( unit.startsWith("c") ) return 0.01f; // cm centimeter
    if ( unit.startsWith("f") ) return (float)TopoDroidUtil.FT2M; // ft feet
    if ( unit.startsWith("i") ) return (float)TopoDroidUtil.IN2M; // in inch
    if ( unit.startsWith("milli") || unit.equals("mm") ) return 0.001f; // mm millimeter
    if ( unit.startsWith("y") ) return (float)TopoDroidUtil.YD2M; // yd yard
    // if ( unit.startsWith("m") ) return 1.0f;
    return 1.0f;
  }

  private int parseExtend( String extend, int old_extend )
  {
    // skip: hide, start
    if ( extend.equals("hide") || extend.equals("start") ) {
      return old_extend;
    }
    if ( extend.equals("left") || extend.equals("reverse") ) {
      return DBlock.EXTEND_LEFT;
    } 
    if ( extend.startsWith("vert") ) {
      return DBlock.EXTEND_VERT;
    }
    if ( extend.startsWith("ignore") ) {
      return DBlock.EXTEND_IGNORE;
    }
    // if ( extend.equals("right") || extend.equals("normal") ) {
    //   return DBlock.EXTEND_RIGHT;
    // } 
    return DBlock.EXTEND_RIGHT;
  }
}
