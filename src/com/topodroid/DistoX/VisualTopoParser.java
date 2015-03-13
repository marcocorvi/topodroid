/** @file VisualTopoParser.java
 *
 * @author marco corvi
 * @date mar 2015
 *
 * @brief TopoDroid VisualTopo parser
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

public class VisualTopoParser extends ImportParser
{
  public VisualTopoParser( String filename, boolean apply_declination ) throws ParserException
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

  float angle( float value, float unit, boolean dm )
  {
    if ( dm ) {
      int sign = 1;
      if ( value < 0 ) { sign = -1; value = -value; }
      int iv = (int)value;
      return sign * ( iv + (value-iv)*0.6f ); // 0.6 = 60/100
    }
    return value * unit;
  }
      

  /** read input file
   * @param filename name of the file to parse
   */
  @Override
  void readFile( BufferedReader br ) throws ParserException
  {
    float mLength, mBearing, mClino, mLeft, mUp, mDown, mRight;
    String mFlag=null, mFrom=null, mTo=null;

    boolean dmb = false; // whether bearing is DD.MM
    boolean dmc = false;
    float ul = 1;  // units factor [m]
    float ub = 1;  // dec.deg
    float uc = 1;  // dec.deg
    int dirw = 1;  // width direction
    int dirb = 1;  // bearing direction
    int dirc = 1;  // clino direction

    boolean splayAtFrom = true;
    String comment = "";
    int extend = 1;
    int shot_extend = 1;
    boolean duplicate = false;
    boolean surface   = false;

    String line = null;
    try {
      line = nextLine( br );
      while ( line != null ) {
        line = line.trim();
        if ( line.startsWith("[Configuration]") ) break;

        int pos = line.indexOf(";");
        if ( pos >= 0 ) {
          line = line.substring( 0, pos );
          comment = line.substring( pos+1 );
          comment.trim();
        } else {
          comment = "";
        }
        if ( line.length() == 0 ) {    // comment
        } else {
          String[] vals = splitLine(line); // line.split( "\\s+" );
          if ( line.startsWith("Version") ) {
            // IGNORE
          } else if ( line.startsWith("Trou") ) {
            String[] params = line.substring(5).split(",");
            if ( params.length > 0 ) {
              mName = params[0].replaceAll(" ","_");
              // TODO coordinates
            }
          } else if ( vals[0].equals("Param") ) {
            for ( int k = 1; k < vals.length; ++k ) {
              if ( vals[k].equals("Deca") ) {
                if ( ++k < vals.length ) {
                  ub = 1;
                  dmb = false;
                  if ( vals[k].equals("Deg") ) {
                    dmb = true;
                  } else if ( vals[k].equals("Gra" ) ) {
                    ub = 0.9f; // 360/400
                  } else { // if ( vals[k].equals("Degd" ) 
                    /* nothing */
                  }
                }
              } else if ( vals[k].equals("Clino") ) {
                if ( ++k < vals.length ) {
                  uc = 1;
                  dmc = false;
                  if ( vals[k].equals("Deg") ) {
                    dmc = true;
                  } else if ( vals[k].equals("Gra" ) ) {
                    uc = 0.9f; // 360/400
                  } else { // if ( vals[k].equals("Degd" ) 
                    /* nothing */
                  }
                }
              } else if ( vals[k].startsWith("Dir") || vals[k].startsWith("Inv") ) {
                String[] dirs = vals[k].split(",");
                if ( dirs.length == 3 ) {
                  dirb = ( dirs[0].equals("Dir") )? 1 : -1;
                  dirc = ( dirs[1].equals("Dir") )? 1 : -1;
                  dirw = ( dirs[2].equals("Dir") )? 1 : -1;
                }
              } else if ( vals[k].equals("Inc") ) {
                // FIXME splay at next station: Which ???
                splayAtFrom = false;
              } else if ( vals[k].equals("Dep") ) {
                splayAtFrom = true;
              } else if ( vals[k].equals("Arr") ) {
                splayAtFrom = false;
              } else if ( vals[k].equals("Std") ) {
                // standard colors; ignore
              } else if ( k == 5 ) {
                try {
                  mDeclination = angle( Float.parseFloat( vals[k] ), 1, true );
                } catch ( NumberFormatException e ) { }
              } else {
                // ignore colors
              }
            }
          } else if ( vals[0].equals("Entree") ) {
          } else if ( vals[0].equals("Club") ) {
            mTeam = line.substring(5);
          } else if ( vals[0].equals("Couleur") ) {
            // IGNORE
          } else { // survey data
            if ( vals.length >= 5 && ! vals[0].equals( vals[1] ) ) {
              int k = 0;
              mFrom = vals[k]; ++k;
              mTo   = vals[k]; ++k;
              try {
                mLength  = Float.parseFloat(vals[k]) * ul; ++k;
                mBearing = angle( Float.parseFloat(vals[k]), ub, dmb); ++k;
                mClino   = angle( Float.parseFloat(vals[k]), uc, dmc); ++k;
                if ( vals.length >= 9 ) {
                  mLeft  = vals[k].equals("*")? -1 : Float.parseFloat(vals[k]) * ul; ++k;
                  mRight = vals[k].equals("*")? -1 : Float.parseFloat(vals[k]) * ul; ++k;
                  mUp    = vals[k].equals("*")? -1 : Float.parseFloat(vals[k]) * ul; ++k;
                  mDown  = vals[k].equals("*")? -1 : Float.parseFloat(vals[k]) * ul; ++k;
                  shot_extend = vals[k].equals("N")? 1 : -1; ++k; // 'N' or 'I'
                  duplicate = vals[k].equals("E"); ++k;           // 'I' or 'E'

                  String station = ( splayAtFrom ? mFrom : mTo );
                  extend = 0;
                  if ( mLeft > 0 ) {
	            float ber = mBearing + 180 + 90 * dirw;
                    if ( TopoDroidSetting.mSplayExtend ) {
                      extend = ( ber < 90 || ber > 270 )? 1 : -1;
                    }
                    splays.add( new ParserShot( station, null, mLeft, ber, 0.0f, 0.0f, extend, false, false, "" ) );
                  }
                  if ( mRight > 0 ) {
                    float ber = mBearing + 180 - 90 * dirw;
                    if ( ber > 360 ) ber -= 360;
                    if ( TopoDroidSetting.mSplayExtend ) {
                      extend = ( ber < 90 || ber > 270 )? 1 : -1;
                    }
                    splays.add( new ParserShot( station, null, mRight, ber, 0.0f, 0.0f, -extend, false, false, "" ) );
                  } 
                  if ( mUp > 0 ) {
                    splays.add( new ParserShot( station, null, mUp, 0.0f, 90.0f, 0.0f, 0, false, false, "" ) );
                  }
                  if ( mDown > 0 ) {
                    splays.add( new ParserShot( station, null, mDown, 0.0f, -90.0f, 0.0f, 0, false, false, "" ) );
                  }
                }
                extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
                shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f,
                                           shot_extend, duplicate, surface, comment ) );
              } catch ( NumberFormatException e ) {
                TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ERROR " + mLineCnt + ": " + line );
                TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "ERROR " + e );
              }
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
    TopoDroidLog.Log( TopoDroidLog.LOG_THERION, "VisualTopoParser shots "+ shots.size() +" splays "+ splays.size()  );
    // Log.v( TopoDroidApp.TAG, "VisualTopoParser shots "+ shots.size() + " splays "+ splays.size() );
  }

  float parseAngleUnit( String unit ) 
  {
    // not handled "percent"
    if ( unit.startsWith("Min") ) return 1/60.0f;
    if ( unit.startsWith("Grad") ) return (float)TopoDroidUtil.GRAD2DEG;
    if ( unit.startsWith("Mil") ) return (float)TopoDroidUtil.GRAD2DEG;
    // if ( unit.startsWith("Deg") ) return 1.0f;
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

}
