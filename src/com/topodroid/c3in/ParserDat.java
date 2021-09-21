/** @file ParserDat.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief compass file parser
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.utils.TDLog;

import com.topodroid.Cave3X.TopoGL;
import com.topodroid.Cave3X.TglParser;
import com.topodroid.Cave3X.Cave3DCS;
import com.topodroid.Cave3X.Cave3DSurvey;
import com.topodroid.Cave3X.Cave3DStation;
import com.topodroid.Cave3X.Cave3DShot;
import com.topodroid.Cave3X.Cave3DFix;
import com.topodroid.Cave3X.Cave3DFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ParserDat extends TglParser
{
  static final int FLIP_NONE       = 0;
  static final int FLIP_HORIZONTAL = 1;
  static final int FLIP_VERTICAL   = 2;

  static final int DATA_NONE      = 0;
  static final int DATA_NORMAL    = 1;
  static final int DATA_DIMENSION = 2;

  // static final public int DAT = 1; // type DAT
  // static final public int MAK = 2;

  // MAK file
  // @param app
  // @param isr       input stream reader
  // @param name      survey name
  // @param pathname  MAK-file pathname
  public ParserDat( TopoGL app, InputStreamReader isr, String name, String pathname ) throws ParserException
  {
    super( app, name );
    TDLog.v( "Parser MAK " + pathname );
    readFileMak( isr, pathname );
    // processShots();
    setShotSurveys();
    setSplaySurveys();
    setStationDepths();
  }

  // DAT file
  // @param app
  // @param isr   input stream reader
  // @param name  survey name
  public ParserDat( TopoGL app, InputStreamReader isr, String name ) throws ParserException
  {
    super( app, name );

    TDLog.v( "Parser DAT " + name );
    readFileDat( isr, name, null, 0.0f, 0.0f, 0.0f );
    // processShots();
    setShotSurveys();
    setSplaySurveys();
    setStationDepths();
  }

  /** read input MAK file
   */
  private boolean readFileMak( InputStreamReader isr, String pathname )
                  throws ParserException
  {
    String dirname = "./";
    int i = pathname.lastIndexOf('/');
    if ( i > 0 ) dirname = pathname.substring(0, i+1);
    TDLog.v( "MAK file " + pathname + " dir " + dirname );

    int linenr = 0;
    try {
      BufferedReader br = new BufferedReader( isr );
      ++linenr;
      String line = br.readLine();
      // TDLog.v( "MAK " + linenr + ":" + line );
      while ( line != null ) {
        // line = line.trim();
        if ( line.startsWith( "#" ) ) {
          i = line.lastIndexOf( ',' );
          String file = line.substring(1,i);
          String filename0 = dirname + file;

          ++linenr; line = br.readLine();
          line = line.trim();
          if ( line.length() == 0 ) continue; // no georeference
	  i = line.indexOf( '[' );
          if ( i <= 0 ) continue; // missing station name
	  String station = line.substring(0,i);
	  int j = line.indexOf( ']' );
          if ( j <= i+3 ) continue; // bad syntax
	  String data = line.substring( i+3, j );
          // TDLog.v( "++ " + linenr + ": " + station + " - " + data );
          String[] vals = data.split( "," );
          if ( vals.length >= 3 ) {
            try {
              int idx = nextIndex( vals, -1 );
	      double x = Double.parseDouble( vals[idx] );
              idx = nextIndex( vals, idx );
	      double y = Double.parseDouble( vals[idx] );
              idx = nextIndex( vals, idx );
	      double z = Double.parseDouble( vals[idx] );
              String survey = Cave3DFile.getMainname( file );
              InputStreamReader isr0 = new InputStreamReader( new FileInputStream( filename0 ) );
	      readFileDat( isr0, survey, station, x, y, z ); // FIXME
	    } catch ( FileNotFoundException e ) {
	      TDLog.Error(  "Error DAT file " + filename0 + " not found");
	    } catch ( NumberFormatException e ) {
	      TDLog.Error(  "Error MAK file " + pathname + ":" + linenr );
	    }
	  }
	}
      
        ++linenr; line = br.readLine();
        // TDLog.v( "MAK " + linenr + ":" + line );
      }
    } catch ( IOException e ) {
      TDLog.Error(  "MAK I/O error " + e.getMessage() );
      throw new ParserException( getName(), linenr );
    }
    // TDLog.v( "done read MAK file " + pathname );

    return ( shots.size() > 0 );
  }

  /** read input DAT file
   */
  private boolean readFileDat( InputStreamReader isr, String survey, String station, double x, double y, double z )
                  throws ParserException
  {
    ArrayList< Cave3DShot > temp_shots  = new ArrayList<>();
    ArrayList< Cave3DShot > temp_splays = new ArrayList<>();

    int linenr = 0;
    // TDLog.v( "DAT file <" + filename + "> station " + station );
    Cave3DCS cs = null;
    // int in_data = 0; // 0 none, 1 normal, 2 dimension

    int[] survey_pos = new int[50]; // FIXME max 50 levels
    int ks = 0;
    boolean in_survey = false;

    double declination = 0.0;
    double units_len = 0.3048; // foot to meter
    double units_ber = 1;
    double units_cln = 1;
    int idx;

    double length, bearing, clino, left, up, down, right, back_bearing, back_clino;

    try {
      TDLog.v( "DAT survey " + survey );

      BufferedReader br = new BufferedReader( isr );
      ++linenr;
      String line = br.readLine();
      // TDLog.v( "DAT " + linenr + ":" + line );
      int cnt_shot = 0;
      while ( line != null ) {
        line = line.trim();
        // "SURVEY NAME" not used
        // "SURVEY DATE" not used
        // "SURVEY TEAM" not used
	if ( line.startsWith( "DECLINATION:" ) ) {
          String[] vals = splitLine( line );
          idx = nextIndex( vals, -1 );
          idx = nextIndex( vals, idx );
	  try {
            declination = Double.parseDouble( vals[idx] );
	    // TDLog.v( "DAT declination " + declination );
	  } catch ( NumberFormatException e ) { }
	} else if ( line.contains("FROM") && line.contains("TO" ) ) {
          ++linenr; line = br.readLine();
          // TDLog.v( "DAT " + linenr + ":" + line );
	  for ( ; line != null; ) {
	    if ( line.length() == 0 ) {
              ++linenr; line = br.readLine();
              // TDLog.v( "DAT " + linenr + ":" + line );
              continue;
	    }
	    if ( line.charAt(0) == 0x0c ) {
              // TDLog.v( "DAT formfeed");
              break; // formfeed
	    }
            String[] vals = splitLine( line );
	    if ( vals.length >= 5 ) { // FROM TO LEN BEAR INC L U D R FLAGS COMMENT
              idx = nextIndex( vals, -1 );
              String f0   = vals[idx]; // remember FROM station
	      String from = vals[idx] + survey;
	      if ( station == null ) station = f0;
              idx = nextIndex( vals, idx );
              String to   = vals[idx] + survey;
	      try {
                idx = nextIndex( vals, idx );
                length = Double.parseDouble( vals[idx] ) * units_len;
                idx = nextIndex( vals, idx );
                bearing = Double.parseDouble( vals[idx] );
                idx = nextIndex( vals, idx );
                clino = Double.parseDouble( vals[idx] );
                left  = -999;
                up    = -999;
                down  = -999;
                right = -999;
                if ( vals.length >= 9 ) {
                  idx = nextIndex( vals, idx );
		  left = Double.parseDouble( vals[idx] ) * units_len; // LEFT
                  idx = nextIndex( vals, idx );
		  up = Double.parseDouble( vals[idx] ) * units_len; // UP
                  idx = nextIndex( vals, idx );
		  down = Double.parseDouble( vals[idx] ) * units_len; // DOWN
                  idx = nextIndex( vals, idx );
		  right = Double.parseDouble( vals[idx] ) * units_len; // RIGHT
                  if ( vals.length >= 11 ) {
                    idx = nextIndex( vals, idx );
                    if (vals[idx].startsWith("#")) {
                      // mFlag = vals[idx];
                      // if ( k < kmax ) mComment = TDUtil.concat( vals, k );
                    } else if ( bearing < -900 || clino < -900 ) {
                      back_bearing = Double.parseDouble(vals[idx]) + 180;
                      if ( back_bearing >= 360 ) back_bearing -= 360;
                      if ( bearing < -900 ) {
                        bearing = back_bearing;
                      } else if ( back_bearing >= 0 && back_bearing <= 360 ) {
                        if ( Math.abs( bearing - back_bearing ) > 180 ) {
                          bearing = ( bearing + back_bearing + 360 ) / 2;
                          if ( bearing >= 360 ) bearing -= 360;
                        } else {
                          bearing = ( bearing + back_bearing ) / 2;
                        }
                      }
                      idx = nextIndex( vals, idx );
                      back_clino = Double.parseDouble(vals[idx]);
                      if ( clino < -900 ) {
                        clino = - back_clino;
                      } else if ( back_clino >= -90 && back_clino <= 90 ) {
                        clino = ( clino - back_clino ) / 2;
                      }
                 
                      // if ( vals.length >= 12 ) {
                      //   idx = nextIndex( vals, idx );
                      //   if (vals[idx].startsWith("#")) {
                      //     // mFlag = vals[idx];
                      //     // if ( k < kmax ) mComment = TDUtil.concat( vals, k );
                      //   }
                      // }
                    }
                  }
                }
                if ( bearing >= 360 ) bearing -= 360;
                else if ( bearing < 0 ) bearing += 360;

                bearing += declination;
                Cave3DShot shot = new Cave3DShot( from, to, length, bearing, clino, 0, 0 );
                temp_shots.add( shot );
                shots.add( shot );
                ++ cnt_shot;

                if ( mSplayUse > SPLAY_USE_SKIP ) {
                  Cave3DShot splay;
                  double bleft = bearing - 90; if ( bleft < 0 ) bleft += 360;
                  double bright = bearing + 90; if ( bright >= 360  ) bright -= 360;
		  if ( left > 0 ) {
                    splay = new Cave3DShot( from, f0+"-L"+survey, left,  bleft,     0, 0, 0 );
                    temp_splays.add( splay );
                    splays.add( splay );
                  }
		  if ( up > 0 ) {
                    splay = new Cave3DShot( from, f0+"-U"+survey, up,    bearing,  90, 0, 0 );
                    temp_splays.add( splay );
                    splays.add( splay );
                  }
		  if ( down > 0 ) {
                    splay = new Cave3DShot( from, f0+"-D"+survey, down,  bearing, -90, 0, 0 );
                    temp_splays.add( splay );
                    splays.add( splay );
                  }
		  if ( right > 0 ) {
                    splay = new Cave3DShot( from, f0+"-R"+survey, right, bright,    0, 0, 0 );
                    temp_splays.add( splay );
                    splays.add( splay );
                  }
                }

	      } catch ( NumberFormatException e ) { }
	    }
            ++linenr; line = br.readLine();
            // TDLog.v( "DAT " + linenr + ":" + line );
	  }
	}
        ++linenr; line = br.readLine();
        // TDLog.v( "DAT " + linenr + ":" + line );
      }
      if ( station != null ) {
        // TDLog.v( "DAT add fix station " +  station + survey );
	fixes.add( new Cave3DFix( station+survey, x, y, z, cs ) );
      }
    } catch ( IOException e ) {
      TDLog.Error( "DAT I/O error " + e.getMessage() );
      throw new ParserException( getName() + survey, linenr );
    }
    // TDLog.v( "DAT shots " + temp_shots.size() );
    processShots( temp_shots, temp_splays );
    return ( temp_shots.size() > 0 );
  }

  private void setShotSurveys()
  {
    for ( Cave3DShot sh : shots ) {
      Cave3DStation sf = sh.from_station;
      Cave3DStation st = sh.to_station;
      sh.mSurvey = null;
      if ( sf != null && st != null ) {
        String sv = sh.from;
        sv = sv.substring( 1 + sv.indexOf('@', 0) );
        for ( Cave3DSurvey srv : surveys ) {
          if ( srv.hasName( sv ) ) {
            // sh.mSurvey = srv;
            // sh.mSurveyNr = srv.number;
            // srv.addShotInfo( sh );
            srv.addShot( sh );
            break;
          }
        }
        if ( sh.mSurvey == null ) {
          Cave3DSurvey survey = new Cave3DSurvey(sv);
          // sh.mSurvey = survey;
          // sh.mSurveyNr = survey.number;
          // survey.addShotInfo( sh );
          survey.addShot( sh );
          surveys.add( survey );
        } 
      }
    }
  }

  private void setSplaySurveys()
  {
    if ( mSplayUse == SPLAY_USE_SKIP ) return;
    for ( Cave3DShot sh : splays ) {
      String sv = null;
      Cave3DStation sf = sh.from_station;
      if ( sf == null ) {
        sf = sh.to_station;
        sv = sh.to;
      } else {
        sv = sh.from;
      }
      if ( sf != null ) {
        sv = sv.substring( 1 + sv.indexOf('@', 0) );
        for ( Cave3DSurvey srv : surveys ) {
          if ( srv.hasName( sv ) ) {
            // sh.mSurvey = srv;
            // sh.mSurveyNr = srv.number;
            // srv.addSplayInfo( sh );
            srv.addSplay( sh );
            break;
          }
        }
      }
    }
  }

  private void processShots( ArrayList<Cave3DShot> tshots, ArrayList<Cave3DShot> tsplays )
  {
    if ( tshots.size() == 0 ) return;
    if ( fixes.size() == 0 ) {
      // TDLog.v( "shots " + tshots.size() + " fixes " + fixes.size() );
      Cave3DShot sh = tshots.get( 0 );
      fixes.add( new Cave3DFix( sh.from, 0.0f, 0.0f, 0.0f, null ) );
    }
 
    int mLoopCnt = 0;
    Cave3DFix f0 = fixes.get( 0 );
    // TDLog.v( "Process Shots. Fix " + f0.name + " " + f0.x + " " + f0.y + " " + f0.z );

    mCaveLength = 0.0f;

    for ( Cave3DFix fix : fixes ) {
      boolean found = false;
      // TDLog.v( "checking fix " + fix.name );
      for ( Cave3DStation s1 : stations ) {
        if ( fix.hasName( s1.name ) ) { found = true; break; }
      }
      if ( found ) { // skip fixed stations that are already included in the model
        // TDLog.v( "found fix " + fix.name );
        continue;
      }
      // TDLog.v( "start station " + fix.name + " N " + fix.y + " E " + fix.x + " Z " + fix.z );
      stations.add( new Cave3DStation( fix.getName(), fix.x, fix.y, fix.z ) );
      // sh.from_station = s0;
    
      boolean repeat = true;
      while ( repeat ) {
        // TDLog.v( "scanning the tshots");
        repeat = false;
        for ( Cave3DShot sh : tshots ) {
          if ( sh.isUsed() ) continue;
          // TDLog.v( "check shot " + sh.from + " " + sh.to );
          // Cave3DStation sf = sh.from_station;
          // Cave3DStation st = sh.to_station;
          Cave3DStation sf = null;
          Cave3DStation st = null;
          for ( Cave3DStation s : stations ) {
            if ( sh.from.equals(s.name) ) {
              sf = s;
              if (  sh.from_station == null ) sh.from_station = s;
              else if ( sh.from_station != s ) TDLog.Error( "shot " + sh.from + " " + sh.to + " from-station mismatch ");
            } 
            if ( sh.to.equals(s.name) )   {
              st = s;
              if (  sh.to_station == null ) sh.to_station = s;
              else if ( sh.to_station != s ) TDLog.Error( "shot " + sh.from + " " + sh.to + " to-station mismatch ");
            }
            if ( sf != null && st != null ) break;
          }
          if ( sf != null && st != null ) {
            // TDLog.v( "unused shot " + sh.from + " " + sh.to + " : " + sf.name + " " + st.name );
            sh.setUsed( ); // LOOP
            mCaveLength += sh.length();
            // make a fake station
            Cave3DStation s = sh.getStationFromStation( sf );
            stations.add( s );
            s.name = s.name + "-" + mLoopCnt;
            ++ mLoopCnt;
            sh.to_station = s;
          } else if ( sf != null && st == null ) {
            // TDLog.v( "unused shot " + sh.from + " " + sh.to + " : " + sf.name + " null" );
            Cave3DStation s = sh.getStationFromStation( sf );
            stations.add( s );
            sh.to_station = s;
            // TDLog.v( "add station " + sh.to_station.name + " N " + sh.to_station.n + " E " + sh.to_station.e + " Z " + sh.to_station.z );
            sh.setUsed( );
            mCaveLength += sh.length();
            repeat = true;
          } else if ( sf == null && st != null ) {
            // TDLog.v( "unused shot " + sh.from + " " + sh.to + " : null " + st.name );
            Cave3DStation s = sh.getStationFromStation( st );
            stations.add( s );
            sh.from_station = s;
            // TDLog.v( "add station " + sh.from_station.name + " N " + sh.from_station.n + " E " + sh.from_station.e + " Z " + sh.from_station.z );
            sh.setUsed( );
            mCaveLength += sh.length();
            repeat = true;
          } else {
            // TDLog.v( "unused shot " + sh.from + " " + sh.to + " : null null" );
          }
        }
      }
    } // for ( Cave3DFix fix : fixes )

    // 3D splay shots
    if ( mSplayUse > SPLAY_USE_SKIP ) {
      for ( Cave3DShot sh : tsplays ) {
        if ( sh.isUsed() ) continue;
        if (  sh.from_station != null ) continue;
        // TDLog.v( "check shot " + sh.from + " " + sh.to );
        for ( Cave3DStation s : stations ) {
          if ( sh.from.equals(s.name) ) {
            sh.from_station = s;
            sh.setUsed( );
            sh.to_station = sh.getStationFromStation( s );
            break;
          }
        }
      }
    }

    computeBoundingBox();
    // // bounding box
    // emin = emax = stations.get(0).e;
    // nmin = nmax = stations.get(0).n;
    // zmin = zmax = stations.get(0).z;
    // for ( Cave3DStation s : stations ) {
    //   if ( nmin > s.n )      nmin = s.n;
    //   else if ( nmax < s.n ) nmax = s.n;
    //   if ( emin > s.e )      emin = s.e;
    //   else if ( emax < s.e ) emax = s.e;
    //   if ( zmin > s.z )      zmin = s.z;
    //   else if ( zmax < s.z ) zmax = s.z;
    // }
  }

  static int nextIndex( String[] vals, int idx )
  {
    ++idx;
    while ( idx < vals.length && vals[idx].length() == 0 ) ++idx;
    return idx;
  }

  static int prevIndex( String[] vals, int idx )
  {
    --idx;
    while ( idx >= 0 && vals[idx].length() == 0 ) --idx;
    return idx;
  }

}
