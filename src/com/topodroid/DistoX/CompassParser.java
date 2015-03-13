/** @file CompassParser.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid Compass parser
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;


// import android.util.Log;

public class CompassParser extends ImportParser
{
  public CompassParser( String filename, boolean apply_declination ) throws ParserException
  {
    super( apply_declination );
    readFile( filename );
  }

  private boolean isDuplicate( String flag )
  {
    if ( flag == null ) return false;
    if ( flag.indexOf('L') >= 0 ) return true;
    return false;
  }

  private boolean isSurface( String flag )
  {
    if ( flag == null ) return false;
    if ( flag.indexOf('X') >= 0 ) return true;
    return false;
  }

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
        if ( line.startsWith("SURVEY NAME") ) {
          if ( mName == null ) {
            int pos = line.indexOf( ':' );
            if ( pos >= 0 ) {
              mName = line.substring( pos+2 );
            }
          }
        } else if ( line.startsWith("SURVEY DATE") ) {
          if ( mDate == null ) {
            String[] vals = splitLine(line); // line.split( "\\s+" );
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw );
            pw.format( "%04d.%02d.%02d", Integer.parseInt( vals[4] ), Integer.parseInt( vals[2] ), Integer.parseInt( vals[3] ) );
            mDate = sw.getBuffer().toString();
            if ( vals.length >= 6 ) {
              int com = line.indexOf("COMMENT:");
              mComment = line.substring( com+8 );
              mComment.trim();
            }
          }
        } else if ( line.startsWith("SURVEY TEAM") ) {
          String team = nextLine( br );
          if ( mTeam.length() == 0 ) {
            mTeam = team;
            mTeam.trim();
          }
        } else if ( line.startsWith("DECLINATION") ) {
          String[] vals = splitLine(line); // line.split( "\\s+" );
          mDeclination = Float.parseFloat( vals[1] );
        } else if ( line.length() > 8 ) {
          String[] vals = splitLine(line); // line.split( "\\s+" );
          int k = 0;
          int kmax = vals.length;
          if ( kmax >= 5 && ! vals[0].equals("FROM") ) {
            mLength = -1;
            mLeft = mRight = mUp = mDown = -1;

            mFrom = vals[k]; ++k;
            mTo = vals[k]; ++k;
            try {
              mLength  = Float.parseFloat(vals[k]) * TopoDroidUtil.FT2M; ++k;
              mBearing = Float.parseFloat(vals[k]); ++k;
              mClino   = Float.parseFloat(vals[k]); ++k;
              if ( k < kmax ) {
                mLeft = Float.parseFloat(vals[k]) * TopoDroidUtil.FT2M; ++k;
                if ( k < kmax ) {
                  mUp = Float.parseFloat(vals[k]) * TopoDroidUtil.FT2M; ++k;
                  if ( k < kmax ) {
                    mDown = Float.parseFloat(vals[k]) * TopoDroidUtil.FT2M; ++k;
                    if ( k < kmax ) {
                      mRight = Float.parseFloat(vals[k]) * TopoDroidUtil.FT2M; ++k;
                      mFlag = null;
                      mComment = "";
                      if ( k < kmax ) {
                        if ( vals[k].startsWith("#") ) {
                          mFlag = vals[k]; ++k;
                        }
                        if ( k < kmax ) {
                          mComment = vals[k];
                          while ( k < kmax ) { mComment = mComment + " " + vals[k]; ++k; }
                        }
                      }
                      // got shot+LRUD
                      int extend = 0;
                      if ( mLeft > 0 ) {
                        float ber = mBearing + 270;
                        if ( ber > 360 ) ber -= 360;
                        if ( TopoDroidSetting.mSplayExtend ) {
                          extend = ( ber < 90 || ber > 270 )? 1 : -1;
                        }
                        splays.add( new ParserShot( mFrom, null, mLeft, ber, 0.0f, 0.0f, extend, false, false, "" ) );
                      }
                      if ( mRight > 0 ) {
                        float ber = mBearing + 90;
                        if ( ber > 360 ) ber -= 360;
                        if ( TopoDroidSetting.mSplayExtend ) {
                          extend = ( ber < 90 || ber > 270 )? 1 : -1;
                        }
                        splays.add( new ParserShot( mFrom, null, mRight, ber, 0.0f, 0.0f, extend, false, false, "" ) );
                      }
                      if ( mUp > 0 ) {
                        splays.add( new ParserShot( mFrom, null, mUp, 0.0f, 90.0f, 0.0f, 0, false, false, "" ) );
                      }
                      if ( mDown > 0 ) {
                        splays.add( new ParserShot( mFrom, null, mDown, 0.0f, -90.0f, 0.0f, 0, false, false, "" ) );
                      }
                      extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
                      shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f,
                      extend, isDuplicate( mFlag ), isSurface(mFlag), mComment ) );
                    }
                  }
                }
              }
              // got only shot
              int extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
              shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f,
                                 extend, isDuplicate( mFlag ), isSurface(mFlag), mComment ) );
            } catch ( NumberFormatException e ) {
              TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ERROR " + mLineCnt + ": " + line );
              TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ERROR " + e );
            }
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      // TODO
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ERROR " + mLineCnt + ": " + line );
      throw new ParserException();
    }
    if ( mDate == null ) {
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      mDate = sdf.format( new Date() );
    }
    TopoDroidLog.Log( TopoDroidLog.LOG_THERION, "CompassParser shots "+ shots.size() +" splays "+ splays.size()  );
    // Log.v( TopoDroidApp.TAG, "CompassParser shots "+ shots.size() + " splays "+ splays.size() );
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
