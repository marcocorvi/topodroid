/* @file TopoRobot.java
 *
 * @author marco corvi
 * @date feb 2024
 *
 * @brief TopoDroid TopoRobot parser
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
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.ExtendType;
import com.topodroid.common.LegType;
import com.topodroid.TDX.TDAzimuth;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import android.util.ArraySet;

class ParserTopoRobot extends ImportParser
{
  private class TRobotTags
  {  
    int series;
    int point;
    int code;
    int session; // always 1
    int trip;

    TRobotTags( String[] token, boolean is_comment )
    {
      int k = is_comment? 1 : 0;
      try {
        series = Integer.parseInt( token[k++] );
        point  = Integer.parseInt( token[k++] );
        code   = Integer.parseInt( token[k++] );
        session= Integer.parseInt( token[k++] );
        trip   = Integer.parseInt( token[k++] );
        // TDLog.v("TR tags " + series + " " + point + " " + code + " " + session + " " + trip );
      } catch ( NumberFormatException e ) { // TODO
        TDLog.e("TR parser: bad tags");
      }
    }
  }

  private class TRobotCode
  { 
    int   code;
    float uAzimuth;
    float uClino;
    // float pLength;   // precision
    // float pAzimuth;
    // float pClino;
    // float tape;      // tape correction ? 100 = tape is correct
    // float winkel;    // for sections

    TRobotCode( String[] token )
    {
      try {
        code = Integer.parseInt( token[1] );
        uAzimuth = 360.0f / Float.parseFloat( token[5] );
        uClino   = 360.0f / Float.parseFloat( token[6] );
        // everything else is ignored
        // TDLog.v("TR code " + code + " A " + uAzimuth + " C " + uClino );
      } catch ( NumberFormatException e ) { // TODO
        TDLog.e("TR parser: bad code " + token[1] );
      }
    }
  }

  private class TRobotTrip
  {
    int index = 0;
    String date;
    int use_declination = 0;

    TRobotTrip( String[] token, ArraySet< String > names )
    {
      try {
        index = Integer.parseInt( token[1] );
      } catch ( NumberFormatException e ) {
        TDLog.v("TR parser: bad trip index " + token[1] );
      } 
      if ( index > 0 ) {
        date  = token[5];
        int k = 5;
        while ( ++k < token.length )  {
          try {
            use_declination = Integer.parseInt( token[k] );
            break;
          } catch ( NumberFormatException e ) {
            names.add( token[k] );
          } 
        }
        if ( ++k < token.length ) {
          if ( use_declination != 0 ) {
            try {
              mDeclination = Float.parseFloat( token[k] );
              TDLog.v("TR parser: set declination " + mDeclination );
            } catch ( NumberFormatException e ) {
              TDLog.e("TR parser: bad declination " + token[k] );
            } 
          } else {
            TDLog.v("TR parser: dont use declination");
          }
        }
      }
      // TDLog.v("TR trip " + index + " decl " + mDeclination + " team " + team );
    }
  }

    

  private class TRobotSequence
  {
    int series;
    int from_series;
    int from_point;
    int to_series;
    int to_point;
    int nr_point;

    TRobotSequence( String[] token )
    {
      try {
        series      = Integer.parseInt( token[0] );
        from_series = Integer.parseInt( token[5] );
        from_point  = Integer.parseInt( token[6] );
        to_series   = Integer.parseInt( token[7] );
        to_point    = Integer.parseInt( token[8] );
        nr_point    = Integer.parseInt( token[9] );
      } catch ( NumberFormatException e ) {
        // TODO
      }
    }

    String getStation() { return String.format("%d.%d", from_series, from_point ); }
  }

  class TRobotFix
  {
    String station;
    double  lat;    // WGS84 dec-degree
    double  lng; 
    double  alt;    // geoid altitude [m]

    TRobotFix( String[] token )
    {
      station = String.format("%s.%s", token[8], token[9] );
      try { 
        lat = Double.parseDouble( token[5] );
        lng = Double.parseDouble( token[6] );
        alt = Double.parseDouble( token[7] );
      } catch ( NumberFormatException e ) {
        TDLog.e("TR parser: bad fix " + station );
      }
    }
   
    void updateLatLng( String str )
    {
      String[] token = str.split("/");
      // TDLog.v("TR parser: fix tokens " + token[0] + " " + token[1] + " " + token[2] );
      try {
        int qos;
        int pos = token[2].indexOf("m");
        if ( pos > 0 ) {
          alt = Integer.parseInt( token[2].substring(0, pos ) );
        }
        lat = 0;
        pos = 1;
        for ( ; ; ) {
          char ch = token[0].charAt( pos );
          ++ pos;
          if ( ch < 0x30 || ch > 0x39 ) break;
          lat = 10 * lat + (ch - 0x30);
        }
        qos = token[0].indexOf("'", pos);
        lat += Integer.parseInt( token[0].substring(pos, qos ) ) / 60.0; qos ++;
        pos = token[0].indexOf("'", qos);
        lat += Float.parseFloat( token[0].substring(qos, pos ) ) / 3600.0;
        if ( token[0].charAt(0) == 'S') lat = -lat;
        lng = 0;
        pos = 1;
        for ( ; ; ) {
          char ch = token[1].charAt( pos );
          ++ pos;
          if ( ch < 0x30 || ch > 0x39 ) break;
          lng = 10 * lng + (ch - 0x30);
        }
        qos = token[1].indexOf("'", pos);
        lng += Integer.parseInt( token[1].substring(pos, qos ) ) / 60.0; qos ++;
        pos = token[1].indexOf("'", qos);
        lng += Float.parseFloat( token[1].substring(qos, pos ) ) / 3600.0;
        if ( token[1].charAt(0) == 'W') lng = -lng;
      } catch ( NumberFormatException e ) { 
        TDLog.v("TR parser: bad coords");
      }
    }
  }

      
  TRobotFix fix = null;
  ArrayList< TRobotTrip > trips;
  ArrayList< TRobotCode > codes;
          
  /** TopoRobot parser
   * @param filename name of the file to parse
   * @param apply_declination  whether to apply declination correction
   */
  ParserTopoRobot( InputStreamReader isr, String filename, boolean apply_declination ) throws ParserException
  {
    super( apply_declination );
    mName = TDio.extractName( filename );
    mDate = TDUtil.currentDate();
    readFile( isr, filename );
    checkValid();
  }

  TRobotCode getCode( int code )
  {
    for ( TRobotCode c : codes ) if ( c.code == code ) return c;
    return null;
  }

  ParserShot getShot( String name )
  {
    for ( int k = shots.size()-1; k >= 0; --k ) {
      ParserShot shot = shots.get(k);
      if ( name.equals( shot.to ) ) return shot;
    }
    return null;
  }

  // @return true if the sting is a valid date (and sets the survey date)
  // private boolean isValidDate( String date ) 
  // {
  //   if ( date.charAt(2) == '/' && date.charAt(5) == '/' ) {
  //     mDate = String.format( "20%s", date.replaceAll("/", ".") );
  //     return true;
  //   }
  //   return false;
  // }

      

  /** read input file
   * @param isr input reader on the input file
   * @param filename   filename, in case isr is null
   */
  private void readFile( InputStreamReader isr, String filename ) throws ParserException
  {
    int series;
    int point;
    float length, azimuth, clino, left, up, down, right;

    boolean is_comment = false;
    String comment = "";
    String sequence_comment = null;
    int dir_w = 1;
    String from = null;
    int extend = 1;
    ArraySet< String > names = new ArraySet<>();

    BufferedReader br = TDio.getBufferedReader( isr, filename );
    String line = null;
    String[] token;
    TRobotTags tag = null;
    TRobotSequence sequence = null;
    trips = new ArrayList< TRobotTrip >();
    codes = new ArrayList< TRobotCode >();

    try {
      line = nextLine( br );
      while ( line != null ) {
        line = line.trim();
        // TDLog.v( "LINE: " + line );
        token = splitLine( line );
        if ( token.length >= 5 ) {
          if ( line.startsWith("(") ) { // comment
            is_comment = true;
            StringBuilder sb = new StringBuilder();
            for ( int k = 6; k < token.length; ++k ) sb.append( token[k] ).append(" ");
            comment = sb.toString();
          } else {
            is_comment = false;
          }
          tag = new TRobotTags( token, is_comment );
          series = tag.series;
          if ( series == -6 ) { // start TopoRobot file
          } else if ( series == -5 ) { // coords of start point
            // TODO add a fixed
            if ( is_comment ) {
              if ( fix != null ) fix.updateLatLng( token[6] );
            } else {
              fix = new TRobotFix( token );
            }
          } else if ( series == -4 ) { // sesion - not used
          } else if ( series == -3 ) { // leftover - not used
          } else if ( series == -2 ) { // trip
            trips.add( new TRobotTrip( token, names ) );
          } else if ( series == -1 ) { // code
            if ( names != null ) {
              StringBuilder sb = new StringBuilder();
              for ( String name : names ) sb.append( name ).append(" ");
              mTeam = sb.toString().trim();;
              TDLog.v("TR parser: team " + mTeam );
              names = null;
              // for ( int k = trips.size()-1; k>=0; --k ) { // set the date
              //   TRobotTrip trip = trips.get(k);
              //   if ( isValidDate( trip.date ) ) {
              //     break;
              //   }
              // }
            }
            codes.add( new TRobotCode( token ) );
          } else { // survey data
            point = tag.point;
            if ( point == -2 ) {
              StringBuilder sb = new StringBuilder();
              for ( int k = 5; k < token.length; ++k ) sb.append( token[k] ).append(" ");
              sequence_comment = sb.toString().trim();
            } else if ( point == -1 ) {
              sequence = new TRobotSequence( token );
              from = sequence.getStation();
            } else {
              if ( sequence != null ) {
                if ( is_comment ) {
                  ParserShot shot = getShot( String.format("%d.%d", series, point ) );
                  if ( shot != null ) {
                    shot.comment = comment;
                  } else {
                    TDLog.e("TR parser: shot for comment not found");
                  }
                } else {
                  try {
                    TRobotCode code = getCode( tag.code ); 
                    length  = Float.parseFloat( token[5] );
                    azimuth = Float.parseFloat( token[6] ) * code.uAzimuth; 
                    clino   = Float.parseFloat( token[7] ) * code.uClino;
                    left    = Float.parseFloat( token[8] );
                    right   = Float.parseFloat( token[9] );
                    up      = Float.parseFloat( token[10] );
                    down    = Float.parseFloat( token[11] );
                    // add shot and splays
                    if ( length > 0.0f ) {
                      String station = String.format("%d.%d", series, point );
                      extend = ( azimuth < 90 || azimuth > 270 )? 1 : -1;
                      if  ( sequence_comment != null ) {
                        shots.add( new ParserShot( from, station, length, azimuth, clino, 0.0f, extend, LegType.NORMAL, false, false, false, sequence_comment ) );
                        sequence_comment = null;
                      } else {
                        shots.add( new ParserShot( from, station, length, azimuth, clino, 0.0f, extend, LegType.NORMAL, false, false, false, "" ) );
                      }
                      from = station;
                    }
                    if ( left > 0.0f ) {
	              float ber = TDMath.in360( azimuth + 180 + 90 * dir_w );
                      extend = ( TDSetting.mLRExtend )? (int)TDAzimuth.computeSplayExtend( ber ) : ExtendType.EXTEND_UNSET;
                      shots.add( new ParserShot( from, TDString.EMPTY, left, ber, 0.0f, 0.0f, extend, LegType.XSPLAY, false, false, false, "" ) );
                    }
                    if ( right > 0.0f ) {
	              float ber = TDMath.in360( azimuth + 180 - 90 * dir_w );
                      extend = ( TDSetting.mLRExtend )? (int)TDAzimuth.computeSplayExtend( ber ) : ExtendType.EXTEND_UNSET;
                      shots.add( new ParserShot( from, TDString.EMPTY, right, ber, 0.0f, 0.0f, extend, LegType.XSPLAY, false, false, false, "" ) );
                    }
                    if ( up > 0 ) {
                      shots.add( new ParserShot( from, TDString.EMPTY, up, 0.0f, 90.0f, 0.0f, ExtendType.EXTEND_VERT, LegType.XSPLAY, false, false, false, "" ) );
                    }
                    if ( down > 0 ) {
                      shots.add( new ParserShot( from, TDString.EMPTY, down, 0.0f, -90.0f, 0.0f, ExtendType.EXTEND_VERT, LegType.XSPLAY, false, false, false, "" ) );
                    }
                  } catch ( NumberFormatException e ) { // TODO
                    TDLog.e("TR parser data-line " + line );
                  }
                }
              } else {
                // ERROR null sequence
              }
            }
          }
        }
        line = nextLine( br );
      }
    } catch ( IOException e ) {
      TDLog.Error( "TR parser: i/o error " + mLineCnt + ": " + line + " " + e.getMessage() );
      throw new ParserException();
    }
    if ( fix != null ) {
      TDLog.v("TR parser: fix " + fix.station + " " + fix.lat + " " + fix.lng + " " + fix.alt );
    }
  }

}
