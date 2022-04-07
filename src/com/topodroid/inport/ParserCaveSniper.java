/* @file ParserCaveSniper.java
 *
 * @author marco corvi
 * @date dec 2017
 *
 * @brief TopoDroid CaveSniper parser
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

class ParserCaveSniper extends ImportParser
{
  /** CaveSniper parser
   * @param filename name of the file to parse
   */
  ParserCaveSniper( InputStreamReader isr, String filename ) throws ParserException
  {
    super( false );  // do not apply_declination
    readFile( isr, filename );
    int pos = filename.lastIndexOf('/');
    if ( pos >= 0 ) { 
      mName = filename.substring(pos+1).replace(".csn", "");
    } else {
      mName = filename.replace(".csn", "");
    }
    mDate = TDUtil.currentDate();
    mTeam = "";
    mDeclination = 0;
    checkValid();
  }

  /** read input file
   * @param isr      imput stream reader
   * @param filename input filename
   */
  private void readFile( InputStreamReader isr, String filename ) throws ParserException
  {
    float mLength, mBearing, mClino, mLeft, mUp, mDown, mRight;
    String mFlag=null, mComment=null, mFrom=null, mTo=null;

    String line = "";
    try {
      BufferedReader br = getBufferedReader( isr, filename );
      line = nextLine( br );
      while ( line != null ) {
        line = line.trim();
        // TDLog.v( line );
        if ( line.startsWith("Unit=") ) {
	  if ( ! line.substring(5).startsWith("Meter") ) {
            TDLog.Error("unhandled unit line: " + line );
	  }
        } else if ( line.startsWith("GPS:") ) {
          // GPS: lng , lat , alt
        } else if ( line.startsWith("Od") ) {
          // Od Do Odleglosc Azymut Upad Komentarz
        } else if ( line.length() > 8 ) {
          String[] vals = splitLine(line); // line.split( "\\s+" );
          int k = 0;
          int kmax = vals.length;
          if ( kmax >= 5 ) {
            mLength = -1;
            mLeft = mRight = mUp = mDown = -1;
            mFrom = vals[k]; ++k;
            mTo = vals[k];   ++k;
            try {
              mLength  = Float.parseFloat(vals[k]); ++k;
              mBearing = Float.parseFloat(vals[k]); ++k;
              mClino   = Float.parseFloat(vals[k]); ++k;
              mComment = TDUtil.concat( vals, k );
              mBearing = TDMath.in360( mBearing );
              // k = vals.length;
	      if ( mTo.startsWith( mFrom + ":" ) ) { // splay are added to the shots array to keep the list order
		// splays have "extend" = ExtendType.EXTEND_UNSET
                shots.add( new ParserShot( mFrom, TDString.EMPTY, mLength, mBearing, mClino, 0.0f, 
					ExtendType.EXTEND_UNSET, LegType.NORMAL, false, false, false, mComment ) );
              } else {
                int extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
                shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f, extend, LegType.NORMAL, false, false, false, mComment ) );
              }
            } catch ( NumberFormatException e ) {
              TDLog.Error( "ERROR " + mLineCnt + ": " + line + e.getMessage() );
            }
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      // TODO
      TDLog.Error( "ERROR " + mLineCnt + ": " + line );
      throw new ParserException();
    }
    TDLog.Log( TDLog.LOG_THERION, "Parser CaveSniper shots "+ shots.size() +" splays "+ splays.size()  );
    // TDLog.v( "Parser CaveSniper shots "+ shots.size() + " splays "+ splays.size() );
  }

}
