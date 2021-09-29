/** @file Parser3d.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief 3d file parser 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.utils.TDLog;
import com.topodroid.DistoX.TopoGL;
import com.topodroid.DistoX.TglParser;
import com.topodroid.DistoX.Cave3DStation;
import com.topodroid.DistoX.Cave3DShot;
import com.topodroid.DistoX.Cave3DSurvey;


import java.io.File;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedInputStream;


import java.util.ArrayList;

public class Parser3d extends TglParser
{
  final static int LINE_SURFACE   = 0x01;
  final static int LINE_DUPLICATE = 0x02;
  final static int LINE_SPLAY     = 0x04;

  private StringBuffer mLabel;
  private byte[] int32;
  private byte[] int16;
  private int mDays1, mDays2;
  private int mLegs;
  private double mLength;
  private double mErrorE, mErrorH, mErrorV;
  private double mLeft, mRight, mUp, mDown;

  double x0, y0, z0; // saved point

  public Parser3d( TopoGL app, DataInputStream dis, String name ) throws ParserException
  {
    super( app, name );
    mLabel = new StringBuffer();
    int32 = new byte[4];
    int16 = new byte[2];
    x0 = y0 = z0 = 0;
    readfile( dis );
    // TDLog.v("3d read " + filename + " shots " + shots.size() + " " + splays.size() );
    setShotsNames();
  }

  private void setShotsNames()
  {
    for (Cave3DShot sp : splays ) {
      sp.from = sp.from_station.name;
    }
    for (Cave3DShot sh : shots ) {
      sh.from = sh.from_station.name;
      sh.to   = sh.to_station.name;
    }
  }

  private Cave3DStation from0 = null;

  private void moveTo( double x, double y, double z )
  {
    // TDLog.v("3d move <" + mLabel.toString() + "> " + x + " " + y + " " + z );
    x0 = x;
    y0 = y;
    z0 = z;

    String survey_name = mLabel.toString();
    Cave3DSurvey survey = getSurvey( survey_name );
    if ( survey == null ) {
      survey = new Cave3DSurvey( survey_name );
      surveys.add( survey );
    }

    from0 = getStationAt( x0, y0, z0 );
    if ( from0 == null ) {
      from0 = new Cave3DStation( "", x0, y0, z0 );
      survey.addStation( from0 );
      stations.add( from0 );
    }
  }

  Cave3DStation getStationAt( double x, double y, double z )
  {
    for ( Cave3DStation st : stations ) {
      if ( Math.abs( x - st.x ) < 1.e-7 && Math.abs( y - st.y ) < 1.e-7 && Math.abs( z - st.z ) < 1.e-7 ) return st;
    }
    return null;
  }

  private void lineTo( double x, double y, double z, int flag )
  {

    double len = Math.sqrt( (x-x0)*(x-x0) + (y-y0)*(y-y0) + (z-z0)*(z-z0) );
    double ber = Math.atan2( x-x0, y-y0 );
    if ( ber < 0 ) ber += Math.PI;
    double h = Math.sqrt( (x-x0)*(x-x0) + (y-y0)*(y-y0) );
    double cln = Math.atan2( z-z0, h );

    String survey_name = mLabel.toString();
    Cave3DSurvey survey = getSurvey( survey_name );
    if ( survey == null ) {
      survey = new Cave3DSurvey( survey_name );
      surveys.add( survey );
    }

    Cave3DStation to = null;

    if ( ( flag & LINE_SPLAY ) == LINE_SPLAY ) {
      if ( mSplayUse > SPLAY_USE_SKIP ) {
        to = getStationAt( x, y, z );
        Cave3DShot splay = new Cave3DShot( from0, null, len, ber, cln, 0, 0 );
        splays.add( splay );
        splay.mSurvey = survey;
        splay.mSurveyNr = survey.number;
        if ( to != null ) {
          from0 = to;
        }
      }
    } else {
      // TDLog.v("3d leg <" + mLabel.toString() + "> " + x + " " + y + " " + z + " flag " + flag );
      long fl = ( flag & 0x03); // LINE_SURFACE | LINE_DUPLICATE
      to = getStationAt( x, y, z );
      if ( to == null ) {
        to = new Cave3DStation( "", x, y, z );
        survey.addStation( to );
        stations.add( to );
      }
      Cave3DShot shot = new Cave3DShot( from0, to, len, ber, cln, fl, 0 );
      shots.add( shot );
      shot.mSurvey = survey;
      shot.mSurveyNr = survey.number;
      from0 = to;
    }
    x0 = x;
    y0 = y;
    z0 = z;
  }

  private void labelAt( double x, double y, double z, int flag )
  {
    if ( mLabel.length() > 0 ) {
      int pos = mLabel.lastIndexOf(".");
      String station_name = (pos >= 0 )? mLabel.substring( pos+1 ) : "0";
      String survey_name  = (pos > 0)? mLabel.substring( 0, pos ) : " "; // default survey-name is " " (empty space)
      Cave3DSurvey survey = getSurvey( survey_name );
      if ( survey == null ) {
        survey = new Cave3DSurvey( survey_name );
        surveys.add( survey );
      }
      String fullname = station_name + "@" + survey_name;
      Cave3DStation station = getStationAt( x, y, z );
      if ( station == null ) {
        // TDLog.v("3d station <" + mLabel.toString() + "> " + x + " " + y + " " + z + " flag " + flag );
        station = new Cave3DStation( fullname, x, y, z, survey );
        stations.add( station );
        survey.addStation( station );
      } else {
        station.setName( fullname );
      }
    }
  }


  // --------------------------------------------------------
  private double cm2m16( int cm ) { return ((cm & 0xffff) == 0xffff)? -1 : cm * 0.01; }
  private double cm2m32( int cm ) { return ((cm & 0xffffffff) == 0xffffffff)? -1 : cm * 0.01; }

  private String readline( DataInputStream dis ) throws IOException
  {
    StringBuffer sb = new StringBuffer();
    for ( ; ; ) {
      int ch = dis.read();
      if ( ch == 0x0a ) break;
      sb.append( (char)ch );
    }
    return sb.toString().trim();
  }

  private void readfile( DataInputStream dis ) throws ParserException
  {
    try {
      // BufferedInputStream bis = new BufferedInputStream( dis );

      String line = readline( dis );
      line.trim();
      // TDLog.v("3d " + line ); // Survey 3D Image File
      line = readline( dis );
      // TDLog.v("3d <ver.> " + line ); // v8
      if ( ! line.equals( "v8" ) ) {
        dis.close(); 
        throw new ParserException( getName(), 2 );
      }
      // line = readline( dis );
      // TDLog.v("3d <title> " + line ); // <title>
      // line = readline( dis );
      // TDLog.v("3d <proj4> " + line ); // <proj4>
      for ( ; ; ) {
        line = readline( dis );
        // TDLog.v("3d @ " + line ); // <proj4>
        if ( line == null ) break;
        if ( line.startsWith("@") ) break;
      } 

      String millis_str = line.substring(1);
      long millis = Long.parseLong( millis_str );
      // TDLog.v("3d millis " + millis );

      int flag = dis.read();  // read one byte file-wide flag
      // TDLog.v("3d flag " + flag );

      for ( ; ; ) {
        int code = dis.read();
        // TDLog.v("3d code " + code );

        // if ( code == 0 ) break; // apparently code-0 appears before the end of data
        if ( code == -1 ) break;
        int ccode = code & 0xf0;
        if ( ccode == 0x00 ) { // survey mode
          handleSurvey( dis, code );
        } else if ( ccode == 0x10 ) { // date
          handleDate( dis, code );
        } else if ( ccode == 0x30 ) { // xsect
          handleXSect( dis, code );
        // } else if ( ccode == 0x20 ) { // label omitted: falls in the next case
        } else if ( ccode <= 0x70 ) { // line
          handleLine( dis, code );
        } else if ( ccode >= 0x80 ) { // label
          handleLabel( dis, code );
        }
      }
      if ( dis != null ) dis.close(); 
    } catch ( IOException e ) { 
      TDLog.Error("3d error " + e.getMessage() );
    }
  }

  private void handleSurvey( DataInputStream dis, int code ) throws IOException
  {
    switch ( code & 0xf ) {
      case 0x01: // diving
        // TDLog.v("3d diving format");
        break;
      case 0x02: // cartesian
        // TDLog.v("3d cartesian format");
        break;
      case 0x03: // cylpolar
        // TDLog.v("3d cylpolar format");
        break;
      case 0x04: // nosurvey
        // TDLog.v("3d nosurvey format");
        break;
      case 0x0f: // moveTo
        double x = cm2m32( Endian.readInt( dis, int32 ) );
        double y = cm2m32( Endian.readInt( dis, int32 ) );
        double z = cm2m32( Endian.readInt( dis, int32 ) );
        moveTo( x, y, z );
        // TDLog.v("3d moveto " + x + " " + y + " " + z );
        break;
    }
  }  

  private void handleDate( DataInputStream dis, int code ) throws IOException
  {
    switch ( code & 0xf ) {
      case 0x00: // nothing
        // TDLog.v("3d Date none" );
        break;
      case 0x01: // days
        mDays1 = Endian.readShort( dis, int16 );
        mDays2 = mDays1;
        // TDLog.v("3d Date days 1" );
        break;
      case 0x02: // days, span
        mDays1 = Endian.readShort( dis, int16 );
        int span = Endian.readShort( dis, int16 );
        mDays2 = mDays1 + span;
        // TDLog.v("3d Date days 2" );
        break;
      case 0x03: // days, days
        mDays1 = Endian.readShort( dis, int16 );
        mDays2 = Endian.readShort( dis, int16 );
        // TDLog.v("3d Date days 3" );
        break;
      case 0x0f: // error
        mLegs = Endian.readInt( dis, int32 );
        mLength = cm2m32( Endian.readInt( dis, int32 ) );
        mErrorE = cm2m32( Endian.readInt( dis, int32 ) );
        mErrorH = cm2m32( Endian.readInt( dis, int32 ) );
        mErrorV = cm2m32( Endian.readInt( dis, int32 ) );
        // TDLog.v("3d Date Errors Nr legs " + mLegs );
        break;
    }
  }  

  private boolean handleXSect( DataInputStream dis, int code ) throws IOException
  {
    boolean is_last = false;
    int label = 0;
    switch ( code & 0xf ) {
      case 0x01: 
        // TDLog.v("3d XSect 1" );
        is_last = true;
      case 0x00:
        // label = Endian.readInt( dis, int32 );
        doHandleLabel( label, dis );
        mLeft  = cm2m16( Endian.readShort( dis, int16 ) );
        mRight = cm2m16( Endian.readShort( dis, int16 ) );
        mUp    = cm2m16( Endian.readShort( dis, int16 ) );
        mDown  = cm2m16( Endian.readShort( dis, int16 ) );
        // TDLog.v("3d XSect " + mLeft + " " + mRight + " " + mUp + " " + mDown );
        break;
      case 0x02: 
        is_last = true;
        // TDLog.v("3d XSect 2" );
      case 0x03:
        // label = Endian.readInt( dis, int32 );
        doHandleLabel( label, dis );
        mLeft  = cm2m32( Endian.readInt( dis, int32 ) );
        mRight = cm2m32( Endian.readInt( dis, int32 ) );
        mUp    = cm2m32( Endian.readInt( dis, int32 ) );
        mDown  = cm2m32( Endian.readInt( dis, int32 ) );
        // TDLog.v("3d XSect " + mLeft + " " + mRight + " " + mUp + " " + mDown );
        break;
    }
    return is_last;
  }  

  private void handleLine( DataInputStream dis, int code ) throws IOException
  {
    int flag = code & 0x0f;
    // 0x01  surface
    // 0x02  duplicate
    // 0x04  splay
    int label = 0;
    if ( ( code & 0x20 ) != 0x20 ) { // label not omitted
      // int label = Endian.readInt( dis, int32 );
      doHandleLabel( label, dis );
    }
    double x = cm2m32( Endian.readInt( dis, int32 ) );
    double y = cm2m32( Endian.readInt( dis, int32 ) );
    double z = cm2m32( Endian.readInt( dis, int32 ) );
    lineTo( x, y, z, flag );
    // TDLog.v("3d Line to " + x + " " + y + " " + z );
  }  

  private void handleLabel( DataInputStream dis, int code ) throws IOException
  {
    int flag = code & 0x7f;
    // 0x01  on leg above ground
    // 0x02  on cave leg
    // 0x04  entrance
    // 0x08  exported (connecting point)
    // 0x10  fixed
    // 0x20  anonymous
    // 0x40  on wall
    int label = 0;
    // label = Endian.readInt( dis, int32 );
    doHandleLabel( label, dis );
    double x = cm2m32( Endian.readInt( dis, int32 ) );
    double y = cm2m32( Endian.readInt( dis, int32 ) );
    double z = cm2m32( Endian.readInt( dis, int32 ) );
    labelAt( x, y, z, flag );
    // TDLog.v("3d Label at " + x + " " + y + " " + z );
  }  

  private void doHandleLabel( int label, DataInputStream dis ) throws IOException
  {
    if ( label == 0xffff ) return; // label omitted
    int B = dis.read();
    int D = 0;
    int A = 0;
    int B1 = -1;
    int B2 = -1;
    if ( B != 0 ) {
      D = B >> 4;
      A = B & 0x0f;
    } else {
      B1 = dis.read();
      if ( B1 != 0xff ) {
        D = B1;
      } else {
        D = Endian.readInt( dis, int32 );
      }
      B2 = dis.read();
      if ( B2 != 0xff ) {
        A = B2;
      } else {
        A = Endian.readInt( dis, int32 );
      }
    }  
    if ( D > 0 ) {
      int len = mLabel.length();
      if ( len > D ) {
        mLabel = mLabel.delete( len - D, len );
      } else {
        mLabel = new StringBuffer();
      }
    }
    if ( A > 0 ) {
      byte[] data = new byte[A];
      int read = dis.read( data );
      String str = new String( data );
      // assert( read == A );
      mLabel.append( str.toCharArray() );
    }
    // TDLog.v("3d Label " + B + ": " + D + " " + A + " .. " + B1 + " " + B2 + " <" + mLabel.toString() + ">" );
  }
      
}

