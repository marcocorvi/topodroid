/* @file ParserBricCsv.java
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

// import java.io.File;
import java.io.IOException;
// import java.io.FileReader;
// import java.io.FileNotFoundException;
// import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
// import java.util.ArrayList;
// import java.util.Stack;
// import java.util.regex.Pattern;

class ParserBricCsv extends ImportParser
{
  /** CaveSniper parser
   * @param filename name of the file to parse
   */
  ParserBricCsv( InputStreamReader isr, String filename ) throws ParserException
  {
    super( false );  // do not apply_declination
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
    float mLength, mBearing, mClino;
    String mFlag=null, mComment=null, mFrom=null, mTo=null;
    float ud = 1, ub = 1, uc = 1;

    String line = "";
    try {
      BufferedReader br = TDio.getBufferedReader( isr, filename );
      line = nextLine( br );
      if ( line == null ) throw new ParserException();
      line.replace(" ", "");
      String[] vals = line.split( "," );
      if ( vals.length < 6 ) throw new ParserException();
      if ( vals[3].startsWith( "Distance" ) ) {
        if ( vals[3].contains( "feet" ) ) ud = TDUtil.FT2M;
      } 
      if ( vals[4].startsWith( "Azimuth" ) ) {
        if ( vals[4].contains( "grad" ) ) ub = TDUtil.GRAD2DEG;
      } 
      if ( vals[4].startsWith( "Inclination" ) ) {
        if ( vals[4].contains( "grad" ) ) uc = TDUtil.GRAD2DEG;
      } 
      line = nextLine( br );
      while ( line != null ) {
        line.trim();
        // line.replace(" ", "");
        vals = line.split( "," );
        if ( vals.length >= 6 ) {
          try {
            mLength  = Float.parseFloat( vals[3] ) * ud;
            mBearing = Float.parseFloat( vals[4] ) * ub;
            mClino   = Float.parseFloat( vals[5] ) * uc;;
            if ( mLength > 0.0001f ) {
              shots.add( new ParserShot( TDString.EMPTY, TDString.EMPTY, mLength, mBearing, mClino, 0.0f, 
					ExtendType.EXTEND_UNSET, LegType.INVALID, false, false, false, "" ) );
            }
          } catch ( NumberFormatException e ) {
            TDLog.Error( "ERROR " + mLineCnt + ": " + line + e.getMessage() );
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      // TODO
      TDLog.Error( "ERROR " + mLineCnt + ": " + line );
      throw new ParserException();
    }
    // TDLog.v( "Parser CSV shots "+ shots.size() + " splays "+ splays.size() );
  }

}
