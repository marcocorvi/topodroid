/* @file ParserCompass.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid Compass parser
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
          Behaviours to be aware of (not bugs)

A line that has AZM2/INC2 columns while the header format says N has those two numbers go into the comment.
Compass spec’s second sample is exactly this case.
If you want to tolerate it, parse the next two tokens as backsight whenever both are numeric, regardless of the B flag.
The risk is a comment that starts with two numbers.

A flag glued to its comment, such as #|PC#Room, is treated as one flag token and the comment is lost.
The spec’s samples have a space, so this is probably fine.

A comment that starts with # but isn’t a flag is read as a flag.
This is harmless, since only L and X are looked at.

With apply_declination off, mDeclination ends up as the last survey’s value, and it is applied to every shot in the file.
That is only wrong for files whose surveys have different declinations.

A cave-name line with 9 or more words is tried as a shot.
It fails on parseFloat and logs one error.

X→surface and L→duplicate are still a mapping choice on your side,
and isC and isNoPlot stay unused. That’s fine as you said.
 */
package com.topodroid.inport;

import com.topodroid.util.TDMath;
import com.topodroid.util.TDLog;
import com.topodroid.util.TDio;
import com.topodroid.util.TDString;
import com.topodroid.util.TDUtil;
import com.topodroid.util.TDAnalytics;
import com.topodroid.prefs.TDSetting;
import com.topodroid.types.ExtendType;
import com.topodroid.types.LegType;
import com.topodroid.TDX.TDAzimuth;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.SurveyInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
// import java.util.ArrayList;
// import java.util.Stack;
import java.util.Locale;
import java.util.regex.Pattern;
// import java.util.regex.Matcher;

class ParserCompass extends ImportParser
{
  private boolean mLrud;
  private boolean mOnlyShot = false;
  private boolean mLegFirst;
  private static final Pattern mPatternSS = Pattern.compile( ".+ss\\d+$" );

  private float mCorLen  = 0;
  private float mCorBer  = 0;
  private float mCorCln  = 0;
  private float mCorBBer = 0;
  private float mCorBCln = 0;
  private boolean mRedundantBacksight = false;
  private boolean mAtTo = false;

  /** Compass parser
   * @param filename name of the file to parse
   * @param apply_declination whether to apply declination correction
   */
  ParserCompass( InputStreamReader isr, String filename, boolean apply_declination, boolean lrud, boolean leg_first ) throws ParserException
  {
    super( apply_declination );
    TopoDroidApp.updateAnalytic( TDAnalytics.IMPORT_DAT );
    if ( mApplyDeclination ) mDeclination = 0.0f;
    TDLog.v( "Parser Compass <" + filename + "> apply-declination " + apply_declination );
    // mName = survey name is read from the file
    mLrud     = lrud;
    mLegFirst = leg_first;
    readFile( isr, filename );
    checkValid();
    if ( mApplyDeclination && mDeclination < SurveyInfo.DECLINATION_MAX ) {
      mDeclination = 0.0f; // set declination to 0 because it has been already applied
    }
  }

  private boolean isC( String flag ) { return  ( flag != null && flag.indexOf('C') >= 0 ); }

  private boolean isNoPlot( String flag ) { return  ( flag != null && flag.indexOf('P') >= 0 ); }

  private boolean isDuplicate( String flag ) { return  ( flag != null && flag.indexOf('L') >= 0 ); }

  private boolean isSurface( String flag ) { return ( flag != null && flag.indexOf('X') >= 0 ); }

  // compass has no flag for backshot, therefore this is always false
  private boolean isBackshot( String flag ) { return false; }

  private void handleFormat( String format )
  {
    int len = format.length();
    if ( len != 11 && len != 12 && len != 13 && len != 15 ) return;
    int kb = ( len == 15 )? 13 : 11;                       // backsight flag index
    if ( len >= 12 ) mRedundantBacksight = ( format.charAt( kb ) == 'B' );
    if ( len >= 13 ) mAtTo               = ( format.charAt( kb+1 ) == 'T' );
  }

  private void resetState()
  {
    mCorLen  = 0;  mCorBer  = 0;  mCorCln  = 0;
    mCorBBer = 0;  mCorBCln = 0;
    mRedundantBacksight = false;
    mAtTo = false;
    mDeclination = 0.0f;
  }

  /** read input file
   * @param isr input stream reader
   * @param filename input filename
   * @note if the declination is set it is added to the azimuths
   */
  private void readFile( InputStreamReader isr, String filename ) throws ParserException
  {
    // lengths in meters
    // angles  in degrees
    float mLength, mBearing, mClino, mLeft, mUp, mDown, mRight;
    String mFlag=null, mComment=null, mFrom=null, mTo=null;

    mTitle = "";
    BufferedReader br = TDio.getBufferedReader( isr, filename );
    String line = "";
    try {
      line = nextLine( br );
      while ( line != null ) {
        line = line.trim();
        // TDLog.v( line );
        if ( line.startsWith("SURVEY NAME") ) {
          if ( mName == null ) {
            int pos = line.indexOf( ':' );
            if ( pos >= 0 ) {
              mName = line.substring( pos+1 ).trim();
            }
          }
          resetState();
        } else if ( line.startsWith("SURVEY DATE") ) {
          String[] vals = splitLine(line); // line.split( "\\s+" );
          if ( vals.length > 4 ) {
            if ( mDate == null ) {
              try {
                int year = Integer.parseInt( vals[4] ); 
                if ( year < 50 ) { year += 2000; }
                else if ( year < 100 ) { year += 1900; }
                mDate = String.format(Locale.US, "%04d.%02d.%02d", year, Integer.parseInt( vals[2] ), Integer.parseInt( vals[3] ) );
              } catch ( NumberFormatException e ) {
                TDLog.e("Non-integer date value");
              }
            }
            if ( mTitle.isEmpty() ) {
              int com = line.indexOf("COMMENT:");
              if ( com >= 0 ) mTitle = line.substring( com+8 ).trim();
            }
          }
        } else if ( line.startsWith("SURVEY TEAM") ) {
          String team = nextLine( br );
          if ( team != null && mTeam.length() == 0 ) {
            mTeam = team.trim();
          }
        } else if ( line.startsWith("DECLINATION") ) {
          String[] vals = splitLine(line); // line.split( "\\s+" );
          if ( vals.length > 1 ) {
            try { 
              mDeclination = Float.parseFloat( vals[1] );
            } catch ( NumberFormatException e ) { }
            // TDLog.v("Declination " + mDeclination );
            int k = 2;
            while ( vals.length > k+1 ) {
              if ( vals[k].equals("FORMAT:") ) {
                handleFormat( vals[k+1] );
                k += 2;
                // TODO handle format
              } else if ( vals[k].equals("CORRECTIONS:") && k+3 < vals.length ) {
                try {
                  mCorBer = Float.parseFloat( vals[k+1] ); // degrees
                  mCorCln = Float.parseFloat( vals[k+2] ); // degrees
                  mCorLen = Float.parseFloat( vals[k+3] ) * TDUtil.FT2M;
                } catch ( NumberFormatException e ) { }
                k += 4;
              } else if ( vals[k].equals("CORRECTIONS2:") && k+2 < vals.length ) {
                try {
                  mCorBBer = Float.parseFloat( vals[k+1] );
                  mCorBCln = Float.parseFloat( vals[k+2] );
                } catch ( NumberFormatException e ) { }
                k += 3;
              } else {
                ++k;
              }
            }
          }
        } else if ( line.length() > 8 ) {
          String[] vals = splitLine(line); // line.split( "\\s+" );
          int k = 0;
          int kmax = vals.length;
          if ( kmax >= 9 && ! vals[0].equals("FROM") ) {
            mBearing = -999;
            mClino   = -999;
            mLength = -1;
            mLeft = mRight = mUp = mDown = -1;

            mFrom = vals[k]; ++k;
            mTo   = vals[k]; ++k;
            if ( mTo.startsWith(")") || mTo.endsWith(")") || mPatternSS.matcher( mTo ).matches() ) mTo=TDString.EMPTY;
            try {
              float vlen = Float.parseFloat( vals[k++] );
              float vber = Float.parseFloat( vals[k++] );
              float vcln = Float.parseFloat( vals[k++] );
              mLength  = vlen * TDUtil.FT2M + mCorLen;
              mBearing = ( vber < -900 )? -999 : TDMath.in360( vber + mCorBer );
              mClino   = ( vcln < -900 )? -999 : vcln + mCorCln;
              mLeft  = Float.parseFloat( vals[k++] ) * TDUtil.FT2M;
              mUp    = Float.parseFloat( vals[k++] ) * TDUtil.FT2M;
              mDown  = Float.parseFloat( vals[k++] ) * TDUtil.FT2M;
              mRight = Float.parseFloat( vals[k++] ) * TDUtil.FT2M;
              mFlag = null;
              mComment = "";
              if ( mRedundantBacksight && k+1 < kmax && ! vals[k].startsWith("#") ) {
                try { 
                  float b = Float.parseFloat( vals[k++] );
                  float c = Float.parseFloat( vals[k++] );
                  if ( b > -900 ) {
                    float bb = TDMath.add180( b + mCorBBer );
                    if ( mBearing < -900 )                       mBearing = bb;
                    else if ( Math.abs( mBearing - bb ) > 180 )  mBearing = TDMath.in360( ( mBearing + bb + 360 ) / 2 );
                    else                                         mBearing = ( mBearing + bb ) / 2;
                  }
                  if ( c > -900 ) {
                    float bc = - ( c + mCorBCln );
                    mClino = ( mClino < -900 )? bc : ( mClino + bc ) / 2;
                  }
                } catch ( NumberFormatException e ) { }
              }
              if ( k < kmax && vals[k].startsWith("#") ) {
                 mFlag = vals[k++];
              }
              if ( k < kmax ) mComment = TDUtil.concat( vals, k );
              if ( mBearing < -900 ) mBearing = 0;
              if ( mClino   < -900 ) mClino   = 0;
              mBearing = checkApplyDeclination( mBearing, mDeclination );

              if ( ! mOnlyShot ) {
                // got shot+LRUD
                int extend = 0;
                if ( mLegFirst ) {
                  extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
                  shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f,
                             extend, LegType.NORMAL, isDuplicate( mFlag ), isSurface(mFlag), isBackshot(mFlag), mComment ) );
                }
                if ( mLrud ) {
                  String station = ( mAtTo && ! mTo.isEmpty() )? mTo : mFrom;
                  if ( mLeft > 0 ) {
                    float ber = TDMath.in360( mBearing + 270 );
                    extend = ( TDSetting.mLRExtend )? (int)TDAzimuth.computeSplayExtend( ber ) : ExtendType.EXTEND_UNSET;
                    // FIXME splays in the shots array to keep them interleaved with legs, but have leg flag 2
                    shots.add( new ParserShot( station, TDString.EMPTY, mLeft, ber, 0.0f, 0.0f, extend, LegType.XSPLAY, false, false, false, "" ) );
                  }
                  if ( mRight > 0 ) {
                    float ber = TDMath.in360( mBearing + 90 );
                    extend = ( TDSetting.mLRExtend )? (int)TDAzimuth.computeSplayExtend( ber ) : ExtendType.EXTEND_UNSET;
                    shots.add( new ParserShot( station, TDString.EMPTY, mRight, ber, 0.0f, 0.0f, extend, LegType.XSPLAY, false, false, false, "" ) );
                  }
                  if ( mUp > 0 ) {
                    shots.add( new ParserShot( station, TDString.EMPTY, mUp, 0.0f, 90.0f, 0.0f, ExtendType.EXTEND_VERT, LegType.XSPLAY, false, false, false, "" ) );
                  }
                  if ( mDown > 0 ) {
                    shots.add( new ParserShot( station, TDString.EMPTY, mDown, 0.0f, -90.0f, 0.0f, ExtendType.EXTEND_VERT, LegType.XSPLAY, false, false, false, "" ) );
                  }
                }
                if ( ! mLegFirst ) {
                  extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
                  shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f,
                             extend, LegType.NORMAL, isDuplicate( mFlag ), isSurface(mFlag), isBackshot(mFlag), mComment ) );
                }
              } else { // got only shot
                int extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
                shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f,
                                 extend, LegType.NORMAL, isDuplicate( mFlag ), isSurface(mFlag), isBackshot(mFlag), mComment ) );
              }
            } catch ( NumberFormatException e ) {
              TDLog.e( "ERROR " + mLineCnt + ": " + line + e.getMessage() );
            }
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      // TODO
      TDLog.e( "ERROR " + mLineCnt + ": " + line );
      throw new ParserException();
    }
    if ( mDate == null ) {
      mDate = TDUtil.currentDate();
    }
    // TDLog.Log( TDLog.LOG_THERION, "Parser Compass shots "+ shots.size() +" splays "+ splays.size()  );
    // TDLog.v( "Parser Compass shots "+ shots.size() + " splays "+ splays.size() );
  }

  // int parseExtend( String extend, int old_extend )
  // {
  //   // skip: hide, start
  //   if ( extend.equals("hide") || extend.equals("start") ) {
  //     return old_extend;
  //   }
  //   if ( extend.equals("left") || extend.equals("reverse") ) {
  //     return ExtendType.EXTEND_LEFT;
  //   } 
  //   if ( extend.startsWith("vert") ) {
  //     return ExtendType.EXTEND_VERT;
  //   }
  //   if ( extend.startsWith("ignore") ) {
  //     return ExtendType.EXTEND_IGNORE;
  //   }
  //   // if ( extend.equals("right") || extend.equals("normal") ) {
  //   //   return ExtendType.EXTEND_RIGHT;
  //   // } 
  //   return ExtendType.EXTEND_RIGHT;
  // }
}
