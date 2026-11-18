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
 *
 * mod HB HBTc
 *
 */
package com.topodroid.inport;

import com.topodroid.util.TDMath;
import com.topodroid.util.TDLog;
import com.topodroid.util.TDio;
import com.topodroid.util.TDString;
import com.topodroid.util.TDUtil;
import com.topodroid.util.TDAnalytics;
// import com.topodroid.prefs.TDSetting;

import com.topodroid.types.LegType;
import com.topodroid.types.StationType;
import com.topodroid.TDX.TopoDroidApp;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// TODO this class can be made extend ImportParser
//
// units, calibrate, sd supported quantities:
//   length tape bearing compass gradient clino counter depth x y z position easting dx northing dy altitude dz

class ParserTherion extends ImportParser
{
  /** fix station:
   * fix stations are supposed to be referred to the same coord system
   */
  static class ThFix
  {
    // private CS cs;
    String name;
    float e, n, z; // north east, vertical (upwards)

    ThFix( String nm, float e0, float n0, float z0 )
    {
      name = nm;
      e = e0;
      n = n0;
      z = z0;
    }
  }

  static class Station
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

  // private boolean mTherionPath;   // if true filename hierarchy replaces survay hierarchy

  private ArrayList< ThFix > fixes;
  private ArrayList< Station > stations;

  // private ArrayList< ParserShot > shots;   // centerline shots // FROM ImportParser
  // private ArrayList< ParserShot > splays;  // splay shots
  // public int getShotNumber()    { return shots.size(); } // FROM ImportParser
  // public int getSplayNumber()   { return splays.size(); }
  // ArrayList< ParserShot > getShots()    { return shots; }
  // ArrayList< ParserShot > getSplays()   { return splays; }
  ArrayList< Station >    getStations() { return stations; }
  ArrayList< ThFix >      getFixes()    { return fixes; }

  private boolean mWithLrud;

  
  private final HashSet<String> mOpenPath = new HashSet<>();   // canonical paths of files being read (recursion guard)

  private static String canonical( String path )
  {
    try { return new File( path ).getCanonicalPath(); } catch ( IOException e ) { return path; }
  }

  /** @return the declination value [deg ?]
   */
  float surveyDeclination( ) { return mDeclination; }

  // ---------------------------------------------------------

  /** cstr
   * @param isr    input reader
   * @param name   filename or surveyname ?
   * @param apply_declination whether to apply the declination correction
   * @param extern_survey  extrenal survey name - if null the top survey is not used
   * @param filepath          file path (absolute)
   */
  ParserTherion( InputStreamReader isr, String name, boolean apply_declination, boolean with_lrud, String extern_survey, String filepath ) throws ParserException
  {
    super( apply_declination );
    mDate = null; // override super assignment
    TopoDroidApp.updateAnalytic( TDAnalytics.IMPORT_TH );
    if ( mApplyDeclination ) mDeclination = 0.0f;
    fixes    = new ArrayList<>();
    stations = new ArrayList<>();
    // shots    = new ArrayList<>(); // FROM ImportParser
    // splays   = new ArrayList<>();
    mWithLrud = with_lrud;
    ParserTherionState state = new ParserTherionState( extern_survey ); // root of the linked list of states
    mOpenPath.add( canonical( filepath ) );
    // FIXME openReader is not used for the top file - encoding is not handled 
    mPartial = readFile( isr, name, state, filepath ); // initial surveypath is empty
    if ( state.mTeam.size() > 0 ) {
      StringBuilder team = new StringBuilder();
      for ( String team_name : state.mTeam ) team.append(" \"").append( team_name ).append("\"");
      mTeam = team.toString().substring(1);
    }
    if ( mDate == null ) mDate = TDUtil.currentDate();
    checkValid();
    if ( mApplyDeclination ) mDeclination = 0.0f; // declination has been already applied to azimuths
  }

  // private String nextLine( BufferedReader br ) throws IOException // FROM ImportParser
  // {
  //   StringBuffer ret = new StringBuffer();
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

  /** @return the fullname of the station from a parser name
   * @param fullname   station name as read from the therion file: can be aaa or aaa@b.c
   * @param state      parser state
   * station name are referenced in the file as "N" or "N@A" where N is the name of the station and A is a survey path (eg, "a1.a2.a3")
   * (1) with prefix "p" and suffix  they are first to be converted in "pNs" and "pNs@A"
   * (2) in the context of surveyB path they must be fully specified as "pNs@B" and "pNs@A.B
   * in summary
   *     N     --> pNs     --> pNs @ B
   *     N @ A --> pNs @ A --> pNs @ A.B
   */
  private String extractStationName( String name, ParserTherionState state ) 
  {
    int idx = name.indexOf('@');
    if ( idx == 0 || idx == name.length() - 1 ) return null;       // bad name "@x", "x@"
    String ret = state.mPrefix + ( idx > 0 ? name.substring( 0, idx ) : name ) + state.mSuffix;
    if ( idx > 0 ) ret += name.substring( idx );                  // "@A"
    if ( ! state.mSurveyPath.isEmpty() ) ret += ( idx > 0 ? "." : "@" ) + state.mSurveyPath;
    return ret;
  }

  /** read input file
   * @param isr      input stream reader
   * @param filename name of the file to parse
   * @param state    state of the parser
   * @param filepath ...
   */
  private boolean readFile( InputStreamReader isr, String filename, ParserTherionState state, String filepath ) throws ParserException
  {
    TDLog.v("Parser TH file " + filename );

    boolean partial = false;

    Pattern pattern = Pattern.compile( "\\s+" );

    String dirpath = "./";
    int i = filepath.lastIndexOf('/');
    if ( i >= 0 ) dirpath = filepath.substring(0, i+1);
    TDLog.v( "import read Therion dirpath <" + dirpath + ">"  );

    try {
      // String dirname = "./";
      // i = filename.lastIndexOf('/');
      // if ( i > 0 ) dirname = filename.substring(0, i+1);
      // // System.out.println("read file dir " + dirname + " filename " + filename );
      // TDLog.v( "import read Therion file <" + filename + "> dir " + dirname );

      BufferedReader br = TDio.getBufferedReader( isr, filename );
      String line = nextLine( br );
      while ( line != null ) {
        // TDLog.v( "Parser TH " + mLineCnt + " (" + state.in_survey + " " + state.in_centerline + " " + state.in_data + "): " + line );
        line = line.trim();
        String cmnt = ""; // HBTc
        int pos = line.indexOf( '#' );
        if ( pos >= 0 ) {
          // TDLog.v("TH parser line <" + line + "> pos " + pos );
          cmnt = line.substring( pos+1 ).trim(); // HBTc
          line = line.substring( 0, pos ).trim();
          if ( line.length() > 0 ) { // empty and comment lines are skipped
            int cmnt_len = cmnt.length();
            if ( cmnt_len > 0 && cmnt.charAt( 0 ) == '\"' ) cmnt = cmnt.substring(1); // strip leading and ending double-quotes
            cmnt_len = cmnt.length();
            if ( cmnt_len > 0 && cmnt.charAt( cmnt_len-1 ) == '\"' ) cmnt = cmnt.substring(0, cmnt_len-1 );
          }
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
          // TDLog.v( "Parser TH vals " + vals.length + " line " + line );
          int vals_len = vals.length;
          if ( vals_len > 0 ) {
            String cmd = vals[0];
            
            if ( state.in_comment ) {  // CONTEXT NONE SURVEY CENTERLINE
              if ( cmd.equals("endcomment" ) ) state.in_comment = false;
              // otherwise everythin ignored
            } else if ( cmd.equals("comment") ) {
                state.in_comment = true;
            } else {
              if ( cmd.equals("scan") ) { // skip everything until endscan
                state.in_scan = true;
              } else if ( state.in_scan ) {
                if ( cmd.equals("endscan") ) state.in_scan = false;

              } else if ( cmd.equals("revise") ) { // ignore everything until endrevise
                partial = true;
              //   state.in_revise = true;
              // } else if ( state.in_revise ) {
              //   if ( cmd.equals("endrevise") ) { state.in_revise = false; } else { partial = true; }
              } else if ( cmd.equals("endrevise") ) {
                // nothing

              } else if ( cmd.equals("encoding" ) ) { // encoding is ignored - TDLog.v("Warning: therion encoding ignored");
              
              } else if ( cmd.equals("import") ) { // ignored
              } else if ( cmd.equals("join") ) { // ignored
              } else if ( cmd.equals("grade") ) { 
                if ( ! state.in_centerline) state.in_grade = true;
              } else if ( cmd.equals("endgrade") ) { // ignored
                state.in_grade = false;
              } else if ( cmd.equals("input") ) {  // input requires a ".th" file. other filename are ignored 
                if ( handleInput( vals, vals_len, dirpath, state ) ) partial = true;
              } else if ( cmd.equals("surface") ) { // skip everything until endsurface
                state.in_surface = true;
              } else if ( cmd.equals("map") ) {     // skip everything until endmap
                state.in_map = true;
              } else if ( cmd.equals("scrap") ) {   // skip everything until endscrap
                state.in_scrap = true;

              // } else if ( state.in_scrap && cmd.equals("point") ) { // handled below
              //   // ignore
              // } else if ( state.in_scrap && cmd.equals("line") ) { // not necessary because they are in SCRAP CONTEXT
              //   // TODO check not already in_line
              //   state.in_line = true;
              //   // TDLog.v("Theron in line");
              // } else if ( state.in_scrap && cmd.equals("area") ) {
              //   // TODO check not already in_area
              //   state.in_area = true;
              //   // TDLog.v("Theron in area");

              // } else if ( state.in_line && cmd.equals("endline") ) { 
              //   state.in_line = false;
              // } else if ( state.in_area && cmd.equals("endarea" ) ) {
              //   state.in_area = false;
              } else if ( cmd.equals("endscrap" ) ) {
                // if ( ! state.in_scrap ) TDLog.e("TH parser endscrap while not in scrap");
                state.in_scrap = false;
              } else if ( cmd.equals("endmap" ) ) {
                // if ( ! state.in_map ) TDLog.e("TH parser endmap while not in map");
                state.in_map = false;
              } else if ( cmd.equals("endsurface" ) ) {
                // if ( ! state.in_surface ) TDLog.e("TH parser endsurface while not in surface");
                state.in_surface = false;

              } else if ( state.in_map || state.in_surface || state.in_scrap /* || state.in_line || state.in_area */ || state.in_grade ) {
                // ignore

              } else if ( cmd.equals("survey") ) {  // START SURVEY CONTEXT
                if ( vals.length > 1 ) {
                  state = new ParserTherionState( state, vals[1] ); // pushState( state ); - note: autonatic mSurveyLevel increment and mSurveyPath update
                  state.in_survey= true;

                  if ( mName == null ) { // parse survey id
                    mName = vals[1];
                  }
                  // TDLog.v("Therion in survey " + mName );

                  for ( int j=2; j<vals_len; ++j ) { // parse survey options
                    if ( vals[j].equals("-declination") && j+1 < vals_len ) {
                      j = state.handleDeclination( ++j, vals ); // j will be then incremented in the for loop
                      if ( j > 0 ) {
                        if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                      } else {
                        j = -j; // reset j positive
                        partial = true;
                      }
                    } else if ( vals[j].equals("-title") && j+1 < vals_len ) {
                      for ( ++j; j<vals_len; ++j ) {
                        if ( vals[j].length() == 0 ) continue;
                        if ( vals[j].startsWith("\"") ) {
                          if ( vals[j].endsWith( "\"" ) ) {
                            mTitle = vals[j].substring(1,vals[j].length()-1);
                          } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append( vals[j].substring(1) );
                            for ( ++j; j<vals_len; ++j ) {
                              if ( vals[j].length() == 0 ) continue;
                              if ( vals[j].endsWith( "\"" ) ) {
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
                    } else if ( vals[j].equals("-namespace") ) {
                      if ( ++j < vals_len && vals[j].equals("off") ) { // not handled
                        partial = true;
                      }
                    } else if ( vals[j].equals("-entrance") ) { // ignored
                    } else if ( vals[j].equals("-person-rename") ) { // TODO
                      partial = true;
                    } else {
                      partial = true;
                    }
                  }
                } else {
                  // TODO
                }
              } else if ( state.in_centerline ) {  // CENTERLINE CONTEXT
                if ( cmd.equals("endcenterline") || cmd.equals("endcentreline") ) {
                  if ( state.mParent != null ) state = state.mParent; // state = popState();
                } else if ( cmd.equals("date") ) {
                  if ( vals.length > 1 ) {
                    String date = vals[1];
                    if ( ! date.equals("-") && mDate == null ) mDate = date.replaceAll("-", "."); // save centerline date - overwrite today date
                  } else {
                    // TODO
                  }
                } else if ( cmd.equals("team") ) {
                  state.concatTeam( vals, 1 );
                } else if ( cmd.equals("explo-date") ) { // ignored
                } else if ( cmd.equals("explo-team") ) { // ignored
                } else if ( cmd.equals("calibrate") ) {
                  boolean c_len = false;
                  boolean c_ber = false;
                  boolean c_cln = false;
                  boolean c_blen = false;
                  boolean c_bber = false;
                  boolean c_bcln = false;
                  boolean c_left  = false;
                  boolean c_right = false;
                  boolean c_up    = false;
                  boolean c_down  = false;
                  boolean c_depth = false;
	          int k = 1;
                  for ( ; k<vals_len - 1; ++k ) {
                    if ( vals[k].equals("length") || vals[k].equals("tape") )                  { c_len = true; }
                    else if ( vals[k].equals("compass") || vals[k].equals("bearing") )         { c_ber = c_bber = true; } // N.B. "compass" not in Therion
                    else if ( vals[k].equals("clino") || vals[k].equals("gradient") )          { c_cln = c_bcln = true; } // N.B. "clino" not in Therion
                    else if ( vals[k].equals("backlength") || vals[k].equals("backtape") )     { c_blen = true; }
                    // else if ( vals[k].equals("backcompass") || vals[k].equals("backbearing") ) { c_bber = true; }
                    // else if ( vals[k].equals("backclino") || vals[k].equals("backgradient") )  { c_bcln = true; }
                    else if ( vals[k].equals("depth") || vals[k].equals("depthchange") )       { c_depth = true; } 
                    else if ( vals[k].equals("dimensions") ) { c_left = c_right = c_up = c_down = true; }
                    else if ( vals[k].equals("left") )  { c_left  = true; }
                    else if ( vals[k].equals("right") ) { c_right = true; }
                    else if ( vals[k].equals("up") )    { c_up    = true; }
                    else if ( vals[k].equals("down") )  { c_down  = true; }
                    // not handled: counter depth=deptchange x=easting y=northing z=altitude, and position (=x,y,z)
                    else if ( Float.isNaN( ParserUtil.parseFloat( vals[k], Float.NaN ) ) ) { partial = true; }
                  }
                  float zero = 0.0f;
                  float scale = 1.0f;
	          int kk = 1;
                  while ( kk < vals_len && Float.isNaN( ParserUtil.parseFloat( vals[kk], Float.NaN ) ) ) ++kk;
                  if ( kk < vals_len ) { zero  = ParserUtil.parseFloat( vals[kk], 0.0f ); ++kk; }
                  if ( kk < vals_len ) { scale = ParserUtil.parseFloat( vals[kk], 1.0f ); }

                  if ( c_len   ) { state.mZeroLen     = zero; state.mScaleLen     = scale; }
                  if ( c_ber   ) { state.mZeroBer     = zero; state.mScaleBer     = scale; }
                  if ( c_cln   ) { 
                    if ( state.mClinoPercent ) { state.mZeroCln = TDMath.atand( zero ); state.mScaleCln = 1; }
                    else {  state.mZeroCln     = zero; state.mScaleCln     = scale; }
                  }
                  if ( c_blen  ) { state.mZeroBackLen = zero; state.mScaleBackLen = scale; }
                  if ( c_bber  ) { state.mZeroBackBer = zero; state.mScaleBackBer = scale; }
                  if ( c_bcln  ) { 
                    if ( state.mClinoPercent ) { state.mZeroBackCln = TDMath.atand( zero ); state.mScaleBackCln = 1; }
                    else { state.mZeroBackCln = zero; state.mScaleBackCln = scale; }
                  }
                  if ( c_left  ) { state.mZeroLeft    = zero; state.mScaleLeft    = scale; }
                  if ( c_right ) { state.mZeroRight   = zero; state.mScaleRight   = scale; }
                  if ( c_up    ) { state.mZeroUp      = zero; state.mScaleUp      = scale; }
                  if ( c_down  ) { state.mZeroDown    = zero; state.mScaleDown    = scale; }
                  if ( c_depth ) { state.mZeroDepth   = zero; state.mScaleDepth   = scale; }
                } else if ( cmd.equals("units") ) { // units quantity_list [factor] unit
                  boolean u_len   = false;
                  boolean u_ber   = false;
                  boolean u_cln   = false;
                  boolean u_blen  = false; // these are effectively same as u_len etc.
                  boolean u_bber  = false;
                  boolean u_bcln  = false;
                  boolean u_left  = false;
                  boolean u_right = false;
                  boolean u_up    = false;
                  boolean u_down  = false;
                  boolean u_depth = false;
                  for ( int k=1; k<vals_len - 1; ++k ) {
                    if (      vals[k].equals("length")      || vals[k].equals("tape") )         { u_len  = u_blen = true; }
                    else if ( vals[k].equals("compass")     || vals[k].equals("bearing") )      { u_ber  = u_bber = true; } // N.B. "compass" not in Therion
                    else if ( vals[k].equals("clino")       || vals[k].equals("gradient") )     { u_cln  = u_bcln = true; } // N.B. "clino" not in Therion
                    // else if ( vals[k].equals("backlength")  || vals[k].equals("backtape") )     { u_blen = true; }
                    // else if ( vals[k].equals("backcompass") || vals[k].equals("backbearing") )  { u_bber = true; }
                    // else if ( vals[k].equals("backclino")   || vals[k].equals("backgradient") ) { u_bcln = true; }
                    else if ( vals[k].equals("depth") || vals[k].equals("depthchange") )       { u_depth = true; } 
                    else if ( vals[k].equals("dimensions") ) { u_left = u_right = u_up = u_down = true; }
                    else if ( vals[k].equals("left") )  { u_left  = true; }
                    else if ( vals[k].equals("right") ) { u_right = true; }
                    else if ( vals[k].equals("up") )    { u_up    = true; }
                    else if ( vals[k].equals("down") )  { u_down  = true; }
                    // not handles depth = depthchange, counter, x=easting, y=northing, z=altitude 
                    else if ( Float.isNaN( ParserUtil.parseFloat( vals[k], Float.NaN ) ) ) { partial = true; }
                  }
                  float factor = ParserUtil.parseFloat( vals[vals_len-2], 1.0f );
                  if ( u_len || u_left || u_right || u_up || u_down || u_blen ) {
                    float len = factor * ParserUtil.parseLengthUnit( vals[vals_len-1] );
                    if ( u_len )  state.mUnitLen     = len;
                    if ( u_blen ) state.mUnitBackLen = len;
                    if ( u_left ) state.mUnitLeft    = len;
                    if ( u_right) state.mUnitRight   = len;
                    if ( u_up   ) state.mUnitUp      = len;
                    if ( u_down ) state.mUnitDown    = len;
                  } 
                  if ( u_depth ) {
                    float len = factor * ParserUtil.parseLengthUnit( vals[vals_len-1] );
                    if ( u_depth ) state.mUnitDepth   = len;
                  }
                  if ( ( u_cln || u_bcln ) && vals[vals_len-1].equals("percent") ) {
                    state.mUnitCln = state.mUnitBackCln = 1; // clino percent have units = 1
                    state.mClinoPercent = true;
                  } else if ( u_ber || u_cln || u_bber || u_bcln ) {
                    float angle = factor * ParserUtil.parseAngleUnit( vals[vals_len-1] );
                    if ( u_ber )  state.mUnitBer     = angle;
                    if ( u_cln )  state.mUnitCln     = angle;
                    if ( u_bber ) state.mUnitBackBer = angle;
                    if ( u_bcln ) state.mUnitBackCln = angle;
                    if (  u_cln || u_bcln ) state.mClinoPercent = false;
                  }
                } else if ( cmd.equals("sd") ) {         // std devs are not handled in TopoDroid: ignored
                } else if ( cmd.equals("grade") ) {      // grade not handled in TopoDroid: ignored 
                } else if ( cmd.equals("grid-angle") ) { // magnetic grid angle not handled in TopoDroid: ignored
                } else if ( cmd.equals("declination") ) { 
                  if ( state.handleDeclination( 1, vals ) > 0 ) {
                    if ( ! mApplyDeclination ) mDeclination = state.mDeclination;
                  } else {
                    partial = true;
                  }
                } else if ( cmd.equals("instrument") ) { // not handled
                  // partial = true;
                } else if ( cmd.equals("flags") ) {
                  boolean yes = true;
                  for ( int ii = 1; ii < vals_len; ++ii ) {
                    if ( vals[ii].equals("not") ) {
                      yes = false;
                    } else {
                      if ( vals[ii].startsWith("splay") ) {
                        state.mSplay = yes;
                      } else if ( vals[ii].startsWith("dup") ) {
                        state.mDuplicate = yes;
                      } else if ( vals[ii].startsWith("surf") ) {
                        state.mSurface = yes;
                      } else if ( vals[ii].startsWith("artif") ) {
                        state.mArtificial = yes;
                      } else if ( vals[ii].startsWith("approx") ) {
                        state.mApproximate = yes;
                      } else {
                        TDLog.e("TH unknown flag " + vals[ii] );
                        partial = true;
                      }
                      yes = true;
                    }
                  }
                } else if ( cmd.equals("mark") ) { // ***** fix station east north Z (ignored std-dev's)
                  // N.B. mark before shots affect only station lines, not stations in shot lines
                  //      This may be fine because stations in shots are (string) attributes of the shot
                  //      and onle important stations are saved in the database (indexed by the station-name)
                  if ( vals_len > 0 ) {
                    String last = vals[vals_len-1];
                    int type = -1;
                    if ( "painted".equals(last) )        type = StationType.STATION_PAINTED;
                    else if ( "fixed".equals(last) )     type = StationType.STATION_FIXED;
                    else if ( "temporary".equals(last) ) type = StationType.STATION_NONE;
                    else { TDLog.e("TH unknown mark " + last ); partial = true; }
                    int end = vals_len-1;
                    if ( type >= 0 ) {
                      if ( end <= 1 ) {                        // no station list: following stations
                        state.mMark = type;
                      } else {
                        for ( int k = 1; k < end; ++k ) {
                          String name = extractStationName( vals[k], state );
                          if ( name != null ) {
                            boolean not_found = true;
                            for ( Station s : stations ) if ( s.name.equals( name ) ) { s.flag = type; not_found = false; break; }
                            if ( not_found ) { 
                              stations.add( new Station( name, "", type ) );
                            }
                          } else {
                            partial = true;
                          }
                        }
                      }
                    }
                  }
                } else if ( cmd.equals("station") ) { // ***** station name "comment" 
                  // N.B. station flags (entrance, continuation, explored, attr) are not handled in TopoDroid
                  if ( vals_len > 2 ) {
                    String name = extractStationName( vals[1], state );
                    if ( name != null ) {
                      // TDLog.v("Therion station " + name );
                      String comment = vals[2];
                      if ( comment.startsWith( "\"" ) ) {
                        if ( comment.endsWith( "\"" ) ) {
                          comment = comment.substring(1,comment.length()-1);
                        } else {
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
                      }
                      // TDLog.v( "Therion parser station " + name + " comment <" + comment + ">" );
                      if ( comment.length() > 0 ) {
                        boolean must_add = true;
                        for ( Station st : stations ) if ( st.name.equals( name ) ) { 
                          must_add = false;
                          st.comment = comment;
                          // st.flag is kept
                          break;
                        }
                        if ( must_add ) stations.add( new Station( name, comment, state.mMark ) ); 
                      }
                    } else {
                      partial = true;
                    }
                  }

                } else if ( cmd.equals("break") ) {
                  // TDLog.v("TH break ignored");
                  partial = true;
                } else if ( cmd.equals("infer") ) { // TDLog.v("TH infer ignored");

                } else if ( cmd.equals("group") ) {
                  state = new ParserTherionState( state, null ); // pushState( state );
                } else if ( cmd.equals("endgroup") ) {
                  if ( state.mParent != null ) state = state.mParent; // state = popState();

                } else if ( cmd.equals("walls") ) { // TDLog.v("TH walls ignored");
                } else if ( cmd.equals("vthreshold") ) { // TDLog.v("TH vthreshold ignored");
                } else if ( cmd.equals("extend") ) {     // fractional extend (second arg) not handled in TopoDroid TODO
                  if ( vals_len >= 2 ) {
                    state.mExtend = ParserUtil.parseExtend( vals[1], state.mExtend );
                  } else { 
                    // TDLog.v("TH not implemented extend value station [station]");
                    partial = true;
                  }
                } else if ( cmd.equals("station-names") ) {
                  // TDLog.v("Therion station-names");
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
                  // TODO
                  // cartesian is easy: len = √(dN²+dE²+dZ²), bearing = atan2(dE,dN), clino = asin(dZ/len).
                  // cylpolar is similar
                  // diving needs clino = −asin(Δdepth/len) with depth positive downward.
                  // topofil: counter, fromcount and tocount should substitute for length.

                  if ( vals.length > 1 ) { // data normal from to length compass clino ...
                    state.setDataType( vals[1] );
                    state.resetJindices();
                 
                    int j0 = 0;
                    int j  = 2;
                    for ( ; j < vals_len; ++j ) {
                      if ( vals[j].equals("ignoreall") ) break;
                      if ( ! state.setJindex( vals[j], j0 ) ) partial = true;
                      ++j0;
                    }
                    while ( j > 2 && vals[j-1].equals( "ignore" ) ) { --j0; --j; } // decrement j0 for last "ignore"s
                    state.min_value_len = j0; 

                    state.checkStateInData();
                    // TDLog.v("Therion data format - type " + state.data_type );
                  } else {
                    // TODO
                  }
                } else if ( state.in_data && vals_len >= state.min_value_len ) { // therion says that dataline can contain reserved wrord if they start with '!' - TopoDroid does not support it
                  // TDLog.v("Therion data " + line );
                  if ( state.data_type == ParserUtil.DATA_NORMAL ) {
                    vals = processDataVals( vals ); // process vals condensates double L R U D of the form [f t] to f:t
                    if ( vals == null || vals.length < state.min_value_len ) { 
                      partial = true;
                    } else {
                      int sz = vals.length;
                      String from = vals[state.jFrom];
                      String to   = vals[state.jTo];
                      float len   = ( state.jLength      < 0 )? Float.NaN : ParserUtil.parseFloat( vals[state.jLength],      Float.NaN );
                      float ber   = ( state.jCompass     < 0 )? Float.NaN : ParserUtil.parseFloat( vals[state.jCompass],     Float.NaN );
                      float cln   = ( state.jClino       < 0 )? Float.NaN : ParserUtil.parseClino( vals[state.jClino], state.mClinoPercent );
                      float blen  = ( state.jBackLength  < 0 )? Float.NaN : ParserUtil.parseFloat( vals[state.jBackLength],  Float.NaN );
                      float bber  = ( state.jBackCompass < 0 )? Float.NaN : ParserUtil.parseFloat( vals[state.jBackCompass], Float.NaN );
                      float bcln  = ( state.jBackClino   < 0 )? Float.NaN : ParserUtil.parseClino( vals[state.jBackClino], state.mClinoPercent );
	              // measure = (read - zero)*scale
	              float zLen  = state.mZeroLen;
	              float sLen  = state.mScaleLen * state.mUnitLen;
	              float zBLen = state.mZeroBackLen;
	              float sBLen = state.mScaleBackLen * state.mUnitBackLen;

                      if ( Float.isNaN( len ) ) {
                        len = Float.isNaN( blen )? Float.NaN : (blen - zBLen) * sBLen;
                      } else {
                        len = (len - zLen) * sLen;
                        if ( ! Float.isNaN( blen ) ) { blen = (blen - zBLen) * sBLen; len  = ( len + blen )/2; }
                      }

                      if ( Float.isNaN( cln ) ) { // TODO assume clino percent corrected in zero (converted to degrees) and scale=1. units=1 (degrees)
                        cln = Float.isNaN( bcln )? Float.NaN : - (bcln - state.mZeroBackCln) * state.mScaleBackCln * state.mUnitBackCln;
                      } else {
                        cln = (cln - state.mZeroCln) * state.mScaleCln * state.mUnitCln;
                        if ( ! Float.isNaN( bcln ) ) { 
                          bcln = (bcln - state.mZeroBackCln) * state.mScaleBackCln * state.mUnitBackCln;
                          cln  = ( cln - bcln )/2;
                        }
                      }

                      if ( Float.isNaN( ber ) ) {
                        ber = Float.isNaN( bber )? Float.NaN : TDMath.in360(180 + (bber - state.mZeroBackBer) * state.mScaleBackBer * state.mUnitBackBer );
                      } else {
                        ber = (ber - state.mZeroBer) * state.mScaleBer * state.mUnitBer;
                        if ( ! Float.isNaN( bber ) ) { bber = (bber - state.mZeroBackBer) * state.mScaleBackBer * state.mUnitBackBer; ber  = TDMath.averageDegrees( ber, TDMath.in360(bber+180) ); }
                      }

                      if ( Float.isNaN( len ) || Float.isNaN( ber ) || Float.isNaN( cln ) ) {
                        // TDLog.v("TH data NaN " + line );
                        partial = true;
                      } else {
                        boolean from_splay = isSplayStationName( from );
                        boolean to_splay   = isSplayStationName( to );
                        if ( from_splay && to_splay ) {            // no real station
                          // TDLog.v( "TH no real station");
                          partial = true;
                        } else {
                          ber = checkApplyDeclination( ber, state.mDeclination );
                          boolean is_splay = from_splay || to_splay || state.mSplay;
                          boolean mirrored = false; // TODO this needs checkin against Therion sources
                          // If LRUD belongs to the FROM station, which is the anonymous one in the swapped case, attaching it to the real station is a guess.
                          if ( from_splay ) {
                            from = to;
                            mirrored = true;
                            ber  = TDMath.in360( ber + 180 );
                            cln  = - cln;
                          }
                          from = extractStationName( from, state );
                          to = is_splay ? "-" : extractStationName( to, state ); 
                          if ( from != null && to != null ) { // from != null && to != null are always true
                            if ( mWithLrud ) {
                              if ( state.jLeft >= 0 && state.jLeft < sz ) {
                                float b = mirrored ? TDMath.add90( ber ) : TDMath.sub90( ber );
                                handleSplay( vals[state.jLeft], from, to, b, 0, state.mZeroLeft, state.mScaleLeft * state.mUnitLeft, state );
                              }
                              if ( state.jRight >= 0 && state.jRight < sz ) {
                                float b = mirrored ? TDMath.sub90( ber ) : TDMath.add90( ber );
                                handleSplay( vals[state.jRight], from, to, b, 0, state.mZeroRight, state.mScaleRight * state.mUnitRight, state );
                              }
                              if ( state.jUp >= 0 && state.jUp < sz ) {
                                handleSplay( vals[state.jUp], from, to, 0, 90, state.mZeroUp, state.mScaleUp * state.mUnitUp, state );
                              }
                              if ( state.jDown >= 0 && state.jDown < sz ) {
                                handleSplay( vals[state.jDown], from, to, 0, -90, state.mZeroDown, state.mScaleDown * state.mUnitDown, state );
                              }
                            }
                            if ( is_splay ) { // splay shot - topodroid uses "" for splay endpoint
                              shots.add( new ParserShot( from, TDString.EMPTY, len, ber, cln, 0.0f, state.mExtend, LegType.NORMAL, 
                                         state.mDuplicate, state.mSurface, state.mArtificial, state.mApproximate, false, cmnt ) );
                            } else {
                              // TDLog.v( "Therion add shot " + from + " " + to);
                              shots.add( new ParserShot( from, to, len, ber, cln, 0.0f, state.mExtend, LegType.NORMAL, 
                                         state.mDuplicate, state.mSurface, state.mArtificial, state.mApproximate, false, cmnt ) );
                            }
                          } else {
                            partial = true;
                          }
                        }
                      }
                    }
                  } else { // if ( state.data_type == ParserUtil.DATA_NORMAL ) 
                    // FIXME other data types
                    partial = true;
                  }
                } else if ( cmd.equals("equate") ) { // NOTE "equate", "fix", "cs" are allowed both in "survey" and in "centerline"
                  if ( ! handleEquate( vals, vals_len, state ) ) partial = true;
                } else if ( cmd.equals("fix") ) { 
                  if ( ! handleFix( vals, vals_len, state ) ) partial = true;
                } else if ( cmd.equals("cs") ) { 
                  if ( ! handleCs( vals, vals_len, state ) ) partial = true; // prepared for CS

                } else {
                  TDLog.v("TH unknown cmd " + cmd + " in centerline");
                  partial = true;
                }
                // state ! in scan
              } else if ( state.in_survey ) { // && ! state.in_centerline  // SURVEY CONTEXT
                if ( cmd.equals("centerline") || cmd.equals("centreline") ) { // START CENTERLINE CONTEXT N.B. Therion allow cetnterline outside SURVEY CONTEXT but TopoDroid only handles inside SURVEY
                  // TDLog.v("Therion in centerline");
                  // pushState( state );
                  state = new ParserTherionState( state, null );
                  state.in_centerline = true;
                  state.in_data = false;
                } else if ( cmd.equals("endsurvey") ) {
                  if ( state.in_centerline ) {
                    TDLog.e("TH Parser close survey " + state.mSurveyPath + " in centerlline" );
                  }
                  if ( state.mParent != null ) state = state.mParent;

                // } else if ( cmd.equals("scan") ) { // handled by the top "scan"
                //   state.in_scan = true;
                // } else if ( state.in_scan ) {
                //   if ( cmd.equals("endscan") ) state.in_scan = false;
                //   // oterwise ignore scan

                } else if ( cmd.equals("equate") ) { // N.B. Therion allows equate outside SURVEY CONTEXT
                  if ( ! handleEquate( vals, vals_len, state ) ) partial = true;
                } else if ( cmd.equals("fix") ) { // ***** fix station east north Z (ignored std-dev's) N.B. Therion allows fix outside SURVEY CONTEXT
                  if ( ! handleFix( vals, vals_len, state  ) ) partial = true;
                } else if ( cmd.equals("cs") ) { 
                  if ( ! handleCs( vals, vals_len, state  ) ) partial = true; // prepared for CS
                } else {
                  TDLog.v("TH unknown cmd " + cmd + " in survey");
                  partial = true;
                }
              } else {
                TDLog.v("TH unknown cmd " + cmd );
                partial = true;
              }
            } // state ! in_comment
          } // empty vals 
        } // empty line
        line = nextLine( br );
      }

    } catch ( IOException e ) {
      // TODO
      TDLog.e("TH IO " + e.getMessage() );
      partial = true;
      throw new ParserException();
    }
    // TDLog.v( "Parser TH shots "+ shots.size() + " splays "+ splays.size() +" fixes "+  fixes.size() );
    return partial;
  }

  /** handle a "equate" command
   * @param vals       string array of tokens on the command (vals[0] = "equate")
   * @param vals_len   number of tokens
   * @param state      parser state
   * @return false if there was a bad reading
   */
  private boolean handleEquate( String[] vals, int vals_len, ParserTherionState state )
  {
    // TDLog.v("Therion equate vals " + vals_len);
    if ( vals_len < 2 ) return false;
    boolean ret = true;
    int j = 1;
    String from = extractStationName( vals[j++], state );
    if ( from == null ) {
      ret = false;
      while ( j<vals_len ) {
        from = extractStationName( vals[j++], state );
        if ( from != null ) break;
      }
    }
    if ( from != null ) {
      while ( j<vals_len ) {
        String to   = extractStationName( vals[j++], state );
        if ( to == null ) { ret = false; }
        else { shots.add( new ParserShot( from, to, 0.0f, 0.0f, 0.0f, 0.0f, 0, LegType.NORMAL, true, false, false, false, false, "" ) ); }
      }
    }
    return ret;
  }

  /** handle a "fix" command, "fix station x y z" (ignoring std-dev's)
   * @param vals       string array of tokens on the command (vals[0] = "fix")
   * @param vals_len   number of tokens
   * @param state      parser state
   * @return false if there was a bad reading
   */
  private boolean handleFix( String[] vals, int vals_len, ParserTherionState state )
  {
    if ( vals_len < 5 || state.mCSorder == state.CS_NONE ) return false;
    String name = extractStationName( vals[1], state );
    if ( name == null ) return false;
    try {
      float a = ParserUtil.parseDegrees( vals[2], Float.NaN ); if ( Float.isNaN( a ) ) return false;
      float b = ParserUtil.parseDegrees( vals[3], Float.NaN ); if ( Float.isNaN( b ) ) return false;
      float c = ParserUtil.parseFloat( vals[4], Float.NaN );   if ( Float.isNaN( c ) ) return false;
      boolean latlong = ( state.mCSorder == ParserState.CS_LAT_LONG );
      fixes.add( new ThFix( name, latlong ? b : a, latlong ? a : b, c ) ); // e=long, n=lat
      return true;
    } catch ( NumberFormatException e ) {
      TDLog.e( "therion parser error: fix " + vals[1] + " " + vals[2] + " " + vals[3] + " " + vals[4] );
    }
    return false;
  }

  /** handle a "cs" command, "cs ..." - TODO not implemented
   * @param vals       string array of tokens on the command (vals[0] = "cs")
   * @param vals_len   number of tokens
   * @param state      parser state
   * @return false if there was a bad reading
   */
  private boolean handleCs(String[] vals, int vals_len, ParserTherionState state )
  { 
    if ( vals_len < 2 ) return false;
    if ( vals[1].equals("long-lat") ) { state.mCSorder = ParserState.CS_LONG_LAT; return true; }
    if ( vals[1].equals("lat-long") ) { state.mCSorder = ParserState.CS_LAT_LONG; return true; }
    state.mCSorder = ParserState.CS_NONE;
    return false;
  }

  private boolean isSplayStationName( String name )
  {
    return ( name.equals("-") || name.equals("_") || name.equals(".") || name.equals("..") || name.equals("...") );
  }

  /** process data vals array to merge double LRUD of form [f t] into a single token f:t
   * @param vals   input data string array
   * @return condensed data strin array
   */
  private String[] processDataVals( String[] vals )
  {
    ArrayList<String> result = new ArrayList<>();
    int n = vals.length;
    int k = 0;
    while ( k<n ) {
      if ( ! vals[k].startsWith("[") ) { 
        result.add( vals[k] ); 
      } else {
        ArrayList<String> inner = new ArrayList<>();

        // boolean closed = false;
        // for ( ; k < n && ! closed; ++k ) {
        //   String t = vals[k].replace("[", "").replace("]", "");
        //   if ( t.length() > 0 ) inner.add( t );
        //   closed = vals[k].endsWith("]");
        // }
        // if ( ! closed || inner.size() > 2 ) return null;          // unclosed or bad data
        // result.add( inner.isEmpty() ? "" : ( inner.size() == 1 ? inner.get(0) : inner.get(0) + ":" + inner.get(1) ) );
      
        if ( vals[k].equals("[]") ) { } // [] 
        else if ( vals[k].equals("[") ) {
          if ( ++k < n && vals[k].equals("]") ) { } // [ ]
          else if ( vals[k].endsWith("]") ) { inner.add( " " ); inner.add( vals[k].replace("]", "") ); } // [ 1.2]
          else {
            inner.add( vals[k] ); 
            while ( ++k < n ) { 
              if ( vals[k].equals("]") ) break;    // [ 1.2 2.3 ] or [ 1.2 ]
              inner.add( vals[k].replace("]", "") );
              if ( vals[k].endsWith("]") ) break;  // [ 1.2 2.3]  or [ 1.2] 
            }
          }
        } else if ( vals[ k ].startsWith("[") ) { //  one of [1.2] [1.2 ] [1.2 2.3 ] [1.2 2.3]
          if ( vals[ k ].endsWith("]") ) inner.add( vals[k].replace("[", "").replace("]", "") ); // [1.2]
          else {
            inner.add( vals[k].replace("[", "") );
            while ( ++k < n ) { 
              if ( vals[k].equals("]") ) break; // [1.2 ] or [1.2 2.3 ]
              inner.add( vals[k].replace("]", "") ); 
              if ( vals[k].endsWith("]") ) break; // [1.2 2.3]
            }
          }
        } 
        if ( inner.size() == 0 )      result.add( "" );
        else if ( inner.size() == 1 ) result.add( inner.get(0) );
        else if ( inner.size() == 2 ) result.add( inner.get(0) + ":" + inner.get(1) );
        else return null; // bad data
      }
      ++k;
    }
    return  result.toArray( new String[0] );
  }
        
  /** handle a splay output
   * @param val    distance string, either one float or two floats joined by ':'
   * @param from   primary splay station
   * @param to     secondary splay station
   * @param b      azimuth
   * @param c      clino
   * @param zero   data zero correction
   * @param scale_unit data scale-unit factor
   * @param state  parsing state
   */
  private void handleSplay( String val, String from, String to, float b, float c, float zero, float scale_unit, ParserTherionState state )
  {
    float dist;
    if ( val.indexOf(':') > 0 ) {
      String[] dists = val.split(":");
      if ( dists.length == 2 ) {
        dist = (ParserUtil.parseFloat( dists[0], Float.NaN ) - zero) * scale_unit;
        if ( ! Float.isNaN( dist ) && dist > 0 ) {
          shots.add( new ParserShot( from, TDString.EMPTY, dist, b, c, 0.0f, state.mExtend, LegType.XSPLAY,
                                     state.mDuplicate, state.mSurface, state.mArtificial, state.mApproximate, false, "") );
        }
        if ( ! to.equals("-") ) { // therion uses "-" for splay endpoint
          dist = (ParserUtil.parseFloat( dists[1], Float.NaN ) - zero) * scale_unit;
          if ( ! Float.isNaN( dist ) && dist > 0 ) {
            shots.add( new ParserShot( to, TDString.EMPTY, dist, b, c, 0.0f, state.mExtend, LegType.XSPLAY,
                                       state.mDuplicate, state.mSurface, state.mArtificial, state.mApproximate, false, "") );
          }
        // } else { 
        //   TODO partial = true
        }
      }
    } else {
      dist = (ParserUtil.parseFloat( val, Float.NaN ) - zero) * scale_unit;
      if ( ! Float.isNaN( dist ) && dist > 0 ) {
        shots.add( new ParserShot( from, TDString.EMPTY, dist, b, c, 0.0f, state.mExtend, LegType.XSPLAY,
                                   state.mDuplicate, state.mSurface, state.mArtificial, state.mApproximate, false, "") );
      }
    }
  }
    
  /** open a therion file honouring its "encoding" command (the first non-comment line); UTF-8 if absent or unknown.
   *  Use this also for the root file, so that the caller does not build the reader itself.
   */
  static InputStreamReader openReader( String path ) throws IOException
  {
    String enc = null;
    try ( FileInputStream fis = new FileInputStream( path ) ) {
      byte[] buf = new byte[4096];
      int n = fis.read( buf );
      if ( n >= 3 && (buf[0]&0xff) == 0xEF && (buf[1]&0xff) == 0xBB && (buf[2]&0xff) == 0xBF ) {
        enc = "UTF-8";
      } else if ( n > 0 ) {
        for ( String line : new String( buf, 0, n, StandardCharsets.ISO_8859_1 ).split( "\r?\n" ) ) {
          line = line.trim();
          if ( line.isEmpty() || line.startsWith("#") ) continue;
          String[] t = line.split( "\\s+" );
          if ( t.length > 1 && t[0].equals("encoding") ) enc = t[1];
          break; // only the first significant line counts
        }
      }
    }
    return new InputStreamReader( new FileInputStream( path ), therionCharset( enc ) );
  }

  private static Charset therionCharset( String enc )
  {
    if ( enc == null ) return StandardCharsets.UTF_8;
    String e = enc.trim().toUpperCase( Locale.ROOT ).replace( '_', '-' );
    if ( e.startsWith("ISO8859-") ) e = "ISO-8859-" + e.substring(8);
    else if ( e.startsWith("CP125") || e.startsWith("WIN125") ) e = "windows-" + e.substring( e.indexOf('1') );
    try { return Charset.forName( e ); } catch ( Exception ex ) { return StandardCharsets.UTF_8; }
  }

  /** handle an "input" command: textual inclusion of another .th file (the state is shared with the included file)
   * @return true if the input is partial (not read, or read with problems)
   */
  private boolean handleInput( String[] vals, int vals_len, String dirpath, ParserTherionState state ) throws ParserException
  {
    StringBuilder sb = new StringBuilder();
    for ( int j = 1; j < vals_len; ++j ) {          // the name is the rest of the line (it may contain spaces if quoted)
      if ( vals[j].length() == 0 ) continue;
      if ( sb.length() > 0 ) sb.append(' ');
      sb.append( vals[j] );
    }
    String name = sb.toString();
    if ( name.length() > 1 && name.startsWith("\"") && name.endsWith("\"") ) name = name.substring( 1, name.length()-1 );
    if ( name.length() == 0 ) return true;
    name = name.replace( '\\', '/' );
  
    String lower = name.toLowerCase( Locale.ROOT );
    if ( lower.endsWith(".th2") ) return false;      // scrap/map file: not needed, not partial
    if ( ! lower.endsWith(".th") ) {
      if ( name.indexOf( '.', name.lastIndexOf('/') + 1 ) >= 0 ) {   // some other extension
        TDLog.e("TH parser ignored input " + name );
        return true;
      }
      name = name + ".th";                           // default extension
    }
    String path = name.startsWith("/") ? name : dirpath + name;
    String canon = canonical( path );
    if ( mOpenPath.contains( canon ) ) {
      TDLog.e("TH parser recursive input " + path );
      return true;
    }
    boolean partial = false;
    InputStreamReader isr1 = null;
    mOpenPath.add( canon );
    try {
      isr1 = openReader( path );
      if ( readFile( isr1, name, state, path ) ) partial = true;
    } catch ( IOException e ) {                      // missing or unreadable file: skip it
      TDLog.e("TH parser cannot read input " + path + " " + e.getMessage() );
      partial = true;
    } finally {
      mOpenPath.remove( canon );
      if ( isr1 != null ) try { isr1.close(); } catch ( IOException ee ) { }
    }
    return partial;
  }
      
}
