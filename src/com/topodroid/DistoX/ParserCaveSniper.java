/** @file ParserCaveSniper.java
 *
 * @author marco corvi
 * @date dec 2017
 *
 * @brief TopoDroid CaveSniper parser
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
// import java.util.Stack;
import java.util.regex.Pattern;

import android.util.Log;

public class ParserCaveSniper extends ImportParser
{
  public ParserCaveSniper( String filename ) throws ParserException
  {
    super( false );  // do not apply_declination
    readFile( filename );
    int pos = filename.lastIndexOf('/');
    if ( pos >= 0 ) { 
      mName = filename.substring(pos+1).replace(".csn", "");
    } else {
      mName = filename.replace(".csn", "");
    }
    mDate = TopoDroidUtil.currentDate();
    mTeam = "";
    mDeclination = 0;
  }

  /** read input file
   * @param filename name of the file to parse
  /** read input file
   * @param filename name of the file to parse
   */
  @Override
  void readFile( BufferedReader br ) throws ParserException
  {
    float mLength, mBearing, mClino, mLeft, mUp, mDown, mRight;
    String mFlag=null, mComment=null, mFrom=null, mTo=null;

    String line = "";
    try {
      line = nextLine( br );
      while ( line != null ) {
        line = line.trim();
        // Log.v("DistoX", line );
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
              if ( k < kmax ) {
                mComment = vals[k];
                while ( k < kmax ) { mComment = mComment + " " + vals[k]; ++k; }
              } else {
                mComment = "";
	      }
	      if ( mTo.startsWith( mFrom + ":" ) ) { // splay are added to the shots array to keep the list order
                shots.add( new ParserShot( mFrom, EMPTY, mLength, mBearing, mClino, 0.0f, 0, 0, false, false, false, mComment ) );
              } else {
                int extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
                shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f, extend, 0, false, false, false, mComment ) );
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
    TDLog.Log( TDLog.LOG_THERION, "ParserCaveSniper shots "+ shots.size() +" splays "+ splays.size()  );
    // Log.v( TopoDroidApp.TAG, "ParserCaveSniper shots "+ shots.size() + " splays "+ splays.size() );
  }

}
