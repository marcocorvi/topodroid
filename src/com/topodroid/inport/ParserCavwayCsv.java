/* @file ParserCavwayCsv.java
 *
 * @author marco corvi
 * @date dec 2024
 *
 * @brief TopoDroid CSV parser - for BRIC
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.inport;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDio;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
import com.topodroid.common.LegType;
import com.topodroid.common.ExtendType;
import com.topodroid.TDX.StationPolicy;

// import java.io.File;
import java.io.IOException;
// import java.io.FileReader;
// import java.io.FileNotFoundException;
// import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
// import java.util.Stack;
// import java.util.regex.Pattern;

class ParserCavwayCsv extends ImportParser
{
  /** CaveSniper parser
   * @param filename name of the file to parse
   */
  ParserCavwayCsv( InputStreamReader isr, String filename ) throws ParserException
  {
    super( false );  // do not apply_declination
    TDLog.v("PARSER Cavway file: " + filename );
    readFile( isr, filename );
    int pos = filename.lastIndexOf('/');
    if ( pos >= 0 ) { 
      mName = filename.substring(pos+1).replace(".csv", "");
    } else {
      mName = filename.replace(".csv", "");
    }
    mDate = TDUtil.currentDate();
    mTeam = "";
    mDeclination = 0;
    checkValid();
  }

  /** read input file
   * @param isr      input stream reader
   * @param filename input filename
   * Time, Posix-Time, index, distance (units), azimuth (units), clino (units), dip (units), roll (units), T (units), measurement_type, error_log
   */
  private void readFile( InputStreamReader isr, String filename ) throws ParserException
  {
    ParserShot old_shot = null;
    float old_length=0, old_bearing=0, old_clino=0;
    float mLength, mBearing, mClino;
    float mAbsG, mAbsM, mDip;
    int   mGx1, mGy1, mGz1, mMx1, mMy1, mMz1;
    int   mGx2, mGy2, mGz2, mMx2, mMy2, mMz2;
    String mFlag=null, mComment=null, mFrom=null, mTo=null;
    boolean in_leg = false;
    int t;
    ArrayList< ParserShot > tmp_shots = new ArrayList< ParserShot >();

    String line = "";
    try {
      BufferedReader br = TDio.getBufferedReader( isr, filename );
      line = nextLine( br );  // header line
      TDLog.v("PARSER header " + line );
      if ( line == null ) throw new ParserException(); 
      // line.replace(" ", "");
      String[] vals; // = line.split( "," );
      // nothing for header line
      // Time,Flag,Distance/m,Azimuth/deg,Inclination/deg,absG/g,absM/uT,dip/deg,GX1,GY1,GZ1,MX1,MY1,MZ1,GX2,GY2,GZ2,MX2,MY2,MZ2,Error Infos,
      line = nextLine( br );
      while ( line != null ) {
        line.trim();
        TDLog.v("PARSER " + line );
        // line.replace(" ", "");
        vals = line.split( "," );
        if ( vals.length >= 6 ) {
          try {
            // String time = vals[0];
            String type = vals[1];
            t = ( type.equals("leg") )? 1 : ( type.equals("splay") )? 2 : 0;
            if ( t == 0 ) {
              line = nextLine( br );
              continue;
            }
            mLength  = Float.parseFloat( vals[2] );
            mBearing = Float.parseFloat( vals[3] );
            mClino   = Float.parseFloat( vals[4] );
            mAbsG    = Float.parseFloat( vals[5] );
            mAbsM    = Float.parseFloat( vals[6] );
            mDip     = Float.parseFloat( vals[7] );
            mGx1     = Integer.parseInt( vals[8] );
            mGy1     = Integer.parseInt( vals[9] );
            mGz1     = Integer.parseInt( vals[10] );
            mMx1     = Integer.parseInt( vals[11] );
            mMy1     = Integer.parseInt( vals[12] );
            mMz1     = Integer.parseInt( vals[13] );
            mGx2     = Integer.parseInt( vals[14] );
            mGy2     = Integer.parseInt( vals[15] );
            mGz2     = Integer.parseInt( vals[16] );
            mMx2     = Integer.parseInt( vals[17] );
            mMy2     = Integer.parseInt( vals[18] );
            mMz2     = Integer.parseInt( vals[19] );
          } catch ( NumberFormatException e ) {
            TDLog.e( "ERROR " + mLineCnt + ": " + line + e.getMessage() );
            line = nextLine( br );
            continue;
          }
          mComment = ( vals[20].startsWith( "No" ) )? "" : vals[20];
          if ( t == 1 ) {
            if ( old_shot != null && closeShots( mLength, mBearing, mClino, old_length, old_bearing, old_clino ) ) {
              old_shot.leg = LegType.EXTRA;
              old_shot.extend = ExtendType.EXTEND_IGNORE;
            }
            old_shot = new ParserShot( TDString.EMPTY, TDString.EMPTY, mLength, mBearing, mClino, 0.0f, 
	                               ExtendType.EXTEND_RIGHT, LegType.NORMAL, false, false, false, "" );
            old_length  = mLength;
            old_bearing = mBearing;
            old_clino   = mClino;
            tmp_shots.add( old_shot );
          } else { // t == 2
            old_shot = null;
            tmp_shots.add( new ParserShot( TDString.EMPTY, TDString.EMPTY, mLength, mBearing, mClino, 0.0f, 
	                               ExtendType.EXTEND_IGNORE, LegType.NORMAL, false, false, false, "" ) );
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      // TODO
      TDLog.e( "ERROR " + mLineCnt + ": " + line );
      throw new ParserException();
    }
    TDLog.v( "Parser CSV shots "+ tmp_shots.size() + " splays "+ splays.size() );

    boolean backward = StationPolicy.isSurveyBackward();
    boolean splay_before_leg = StationPolicy.isShotAfterSplays();
    int from = 0;
    int to   = 1;
    if ( backward ) {
      from = 1;
      to   = 0;
    }
    int st = from;

    int n = tmp_shots.size() - 1;
    for ( ; n >= 0; --n ) {
      ParserShot shot = tmp_shots.get(n);
      if ( shot.leg != LegType.EXTRA ) {
        shot.from = Integer.toString( from );
        if ( shot.extend != ExtendType.EXTEND_IGNORE ) {
          shot.to = Integer.toString( to );
          if ( backward ) {
            //  splay_after_leg: 2-1 ... splay at 2 ... 3-2
            //        before   : 2-1 ... splay at 3 ... 3-2
            st = splay_before_leg ? from + 1 : from;
            to = from;
            from ++;
          } else {
            //  splay_after_leg: 1-2 ... splay at 1 ... 2-3
            //        before   : 1-2 ... splay at 2 ... 2-3
            st = splay_before_leg ? to : from;
            from = to;
            to ++;
          }
        }
      }
      shots.add( shot );
    }
  }

  private boolean closeShots(float l1, float b1, float c1, float l2, float b2, float c2 )
  {
    if ( Math.abs( l1 - l2 ) > 0.3 ) return false;
    if ( Math.abs( c1 - c2 ) > 2 )   return false;
    float db = ( b1 > 270 + b2 )? b1-360-b2 : ( b2 > 270 + b1 )? b2-360-b1 : b1-b2;
    return Math.abs( db ) < 2;
  }

}
