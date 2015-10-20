/** @file TopoDroidUtil.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief numerical utilities
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.util.Log;

public class TopoDroidUtil
{
  static final float M_PI  = 3.1415926536f; // Math.PI;
  static final float M_2PI = 6.283185307f;  // 2*Math.PI;
  static final float M_PI2 = M_PI/2;        // Math.PI/2
  static final float M_PI4 = M_PI/4;        // Math.PI/4
  static final float M_PI8 = M_PI/8;        // Math.PI/8
  static final float RAD2GRAD = (180.0f/M_PI);
  static final float GRAD2RAD = (M_PI/180.0f);

  static final long ZERO = 32768;
  static final long NEG  = 65536;
  static final float FV = 24000.0f;
  static final float FM = 16384.0f;
  static final float FN = 2796f; // 2^26 / FV

  static final float DEG2GRAD = 400.0f/360.0f;
  static final float GRAD2DEG = 360.0f/400.0f;

  static final float M2FT = 3.28084f; // meters to feet 
  static final float FT2M = 0.3048f;
  static final float IN2M = 0.0254f;
  static final float YD2M = 0.9144f;
  static float in360( float f )
  {
    while ( f >= 360 ) f -= 360;
    while ( f < 0 )    f += 360;
    return f;
  }

  static float around( float f, float f0 ) 
  {
    if ( f - f0 > 180 ) return f - 360;
    if ( f0 - f > 180 ) return f + 360;
    return f;
  }

  static float degree2slope( float deg )
  {
    return (float)(100 * Math.tan( deg * GRAD2RAD ) );
  }

  static float slope2degree( float slp )
  {
    return (float)( Math.atan( slp/100 ) * RAD2GRAD );
  }


  static String noSpaces( String s )
  {
    return ( s == null )? null 
      : s.trim().replaceAll("\\s+", "_").replaceAll("/", "-").replaceAll("\\*", "+").replaceAll("\\\\", "");
  }

  static String dropSpaces( String s )
  {
    return ( s == null )? null 
      : s.trim().replaceAll("\\s+", "");
  }

  static String currentDate()
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    return sdf.format( new Date() );
  }

  static String getDateString( String format )
  {
    SimpleDateFormat sdf = new SimpleDateFormat( format, Locale.US );
    return sdf.format( new Date() );
  }

  static int dateParseYear( String date )
  {
    try {
      return Integer.parseInt( date.substring(0, 4) );
    } catch ( NumberFormatException e ) { }
    return 2000;
  }

  static int dateParseMonth( String date )
  {
    int ret = 0;
    if ( date.charAt(5) == '1' ) ret += 10;
    char ch = date.charAt(6);
    if ( ch >= '0' && ch <= '9' ) ret += (ch - '0');
    return (ret > 0)? ret-1 : 0;
  }

  static int dateParseDay( String date )
  {
    int ret = 0;
    char ch = date.charAt(8);
    if ( ch >= '1' && ch <= '3' ) ret += 10*(ch - '0');
    ch = date.charAt(9);
    if ( ch >= '1' && ch <= '9' ) ret += (ch - '0');
    return (ret > 0)? ret : 0;
  }

  static String composeDate( int y, int m, int d )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%04d.%02d.%02d", y, m+1, d );
    return sw.getBuffer().toString();
  }

}
