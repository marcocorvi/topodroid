/* @file ParserVisualTopo.java
 *
 * @author marco corvi
 * @date mar 2015
 *
 * @brief TopoDroid VisualTopo parser
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.io.File;
import java.io.IOException;
// import java.io.FileReader;
import java.io.BufferedReader;
// import java.util.ArrayList;
// import java.util.Stack;
// import java.util.regex.Pattern;

// import android.util.Log;

class ParserVisualTopo extends ImportParser
{
  /** VisualTopo parser
   * @param filename name of the file to parse
   * @param apply_declination  whether to apply declination correction
   */
  ParserVisualTopo( String filename, boolean apply_declination ) throws ParserException
  {
    super( apply_declination );
    mName = extractName( filename );
    readFile( filename );
    checkValid();
  }

  private boolean isDuplicate( String flag )
  {
    if ( flag == null ) return false;
    return ( flag.indexOf('L') >= 0 );
  }

  private boolean isSurface( String flag )
  {
    if ( flag == null ) return false;
    return ( flag.indexOf('X') >= 0 );
  }

  private float angle( float value, float unit, boolean dm )
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
   * @param br buffered reader on the input file
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
    int extend = DBlock.EXTEND_RIGHT;
    int shot_extend = DBlock.EXTEND_RIGHT;
    boolean duplicate = false;
    boolean surface   = false;
    boolean backshot  = false;

    String line = null;
    try {
      line = nextLine( br );
      while ( line != null ) {
        line = line.trim();
        // Log.v("DistoX", "LINE: " + line );
        if ( line.startsWith("[Configuration]") ) break;

        int pos = line.indexOf(";");
        if ( pos >= 0 ) {
          comment = (pos+1<line.length())? line.substring( pos+1 ) : "";
          line    = line.substring( 0, pos );
          comment = comment.trim();
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
          } else if ( vals[0].equals("Surface") ) {
            // IGNORE
          } else { // survey data
            if ( vals.length >= 5 && ! vals[0].equals( vals[1] ) ) {
              int k = 0;
              mFrom = vals[k]; ++k; // 0
              mTo   = vals[k]; ++k; // 1
              try {
                mLength  = Float.parseFloat(vals[k]) * ul; ++k; // 2
                mBearing = angle( Float.parseFloat(vals[k]), ub, dmb); ++k; // 3
                mClino   = angle( Float.parseFloat(vals[k]), uc, dmc); ++k; // 5
                mLeft = mRight = mUp = mDown = 0;
                if ( k < vals.length ) {
                  mLeft  = vals[k].equals("*")? -1 : Float.parseFloat(vals[k]) * ul; ++k; // 5
                }
                if ( k < vals.length ) {
                  mRight = vals[k].equals("*")? -1 : Float.parseFloat(vals[k]) * ul; ++k; // 6
                }
                if ( k < vals.length ) {
                  mUp    = vals[k].equals("*")? -1 : Float.parseFloat(vals[k]) * ul; ++k; // 7
                }
		if ( k < vals.length ) {
                  mDown  = vals[k].equals("*")? -1 : Float.parseFloat(vals[k]) * ul; ++k; // 8
                }
                shot_extend = DBlock.EXTEND_RIGHT;
                if ( k < vals.length ) {
                  shot_extend = vals[k].equals("N")? DBlock.EXTEND_RIGHT : DBlock.EXTEND_LEFT; ++k; // 'N' or 'I'
                } 
                duplicate = false;
                if ( k < vals.length ) {
                  duplicate = vals[k].equals("E"); ++k;           // 'I' or 'E'
                }

                String station = ( splayAtFrom ? mFrom : mTo );
                if ( mLeft > 0 ) {
	          float ber = mBearing + 180 + 90 * dirw;
                  extend = ( TDSetting.mLRExtend )? (int)TDAzimuth.computeSplayExtend( ber ) : DBlock.EXTEND_UNSET;
                  shots.add( new ParserShot( station, TDString.EMPTY, mLeft, ber, 0.0f, 0.0f, extend, 2, false, false, false, "" ) );
                }
                if ( mRight > 0 ) {
                  float ber = mBearing + 180 - 90 * dirw;
                  if ( ber > 360 ) ber -= 360;
                  extend = ( TDSetting.mLRExtend )? (int)TDAzimuth.computeSplayExtend( ber ) : DBlock.EXTEND_UNSET;
                  shots.add( new ParserShot( station, TDString.EMPTY, mRight, ber, 0.0f, 0.0f, -extend, 2, false, false, false, "" ) );
                } 
                if ( mUp > 0 ) {
                  // FIXME splays
                  shots.add( new ParserShot( station, TDString.EMPTY, mUp, 0.0f, 90.0f, 0.0f, DBlock.EXTEND_VERT, 2, false, false, false, "" ) );
                }
                if ( mDown > 0 ) {
                  // FIXME splays
                  shots.add( new ParserShot( station, TDString.EMPTY, mDown, 0.0f, -90.0f, 0.0f, DBlock.EXTEND_VERT, 2, false, false, false, "" ) );
                }
                
                extend = ( mBearing < 90 || mBearing > 270 )? 1 : -1;
                shots.add( new ParserShot( mFrom, mTo, mLength, mBearing, mClino, 0.0f,
                                           shot_extend, 0, duplicate, surface, backshot, comment ) );
              } catch ( NumberFormatException e ) {
                TDLog.Error( "ERROR " + mLineCnt + ": " + line + " " + e.getMessage() );
              }
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
    TDLog.Log( TDLog.LOG_THERION, "Parser VisualTopo shots "+ shots.size() +" splays "+ splays.size()  );
    // Log.v( "DistoX", "Parser VisualTopo shots "+ shots.size() + " splays "+ splays.size() );
  }

  // float parseAngleUnit( String unit )
  // {
  //   // not handled "percent"
  //   if ( unit.startsWith("Min") ) return 1/60.0f;
  //   if ( unit.startsWith("Grad") ) return (float)TDUtil.GRAD2DEG;
  //   if ( unit.startsWith("Mil") ) return (float)TDUtil.GRAD2DEG;
  //   // if ( unit.startsWith("Deg") ) return 1.0f;
  //   return 1.0f;
  // }

  // float parseLengthUnit( String unit )
  // {
  //   if ( unit.startsWith("c") ) return 0.01f; // cm centimeter
  //   if ( unit.startsWith("f") ) return (float)TDUtil.FT2M; // ft feet
  //   if ( unit.startsWith("i") ) return (float)TDUtil.IN2M; // in inch
  //   if ( unit.startsWith("milli") || unit.equals("mm") ) return 0.001f; // mm millimeter
  //   if ( unit.startsWith("y") ) return (float)TDUtil.YD2M; // yd yard
  //   // if ( unit.startsWith("m") ) return 1.0f;
  //   return 1.0f;
  // }

}
