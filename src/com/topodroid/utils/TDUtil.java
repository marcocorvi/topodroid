/* @file TDUtil.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief numerical utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.TDandroid;

// import java.lang.Math;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;

import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.time.DateTimeException; // API-26

// import android.content.Context;

public class TDUtil
{
  public static final long ZERO = 32768;
  public static final long NEG  = 65536;
  public static final float FV = 24000.0f;
  public static final float FM = 16384.0f; // 2^14
  public static final float FN = 2796f;    // 2^26 / FV

  public static final float DEG2GRAD = 400.0f/360.0f;
  public static final float GRAD2DEG = 360.0f/400.0f;

  public static final int DDMMSS = 0;
  public static final int DEGREE = 1;

  public static final float M2FT = 3.28084f; // meters to feet 
  public static final float FT2M = 0.3048f;
  public static final float IN2M = 0.0254f;
  public static final float YD2M = 0.9144f;

  // STRINGS --------------------------------------------------------------
  
  // concatenate strings using a single-space separator
  // empty strings are skipped
  public static String concat( String[] vals, int k )
  {
    if ( k < vals.length ) {
      StringBuffer sb = new StringBuffer();
      for ( ; k<vals.length; ++k ) if ( vals[k].length() > 0 ) {
        sb.append(vals[k]);
        break;
      }
      for (++k; k < vals.length; ++k) {
        if ( vals[k].length() > 0 ) sb.append(" ").append(vals[k]);
      }
      return sb.toString();
    }
    return "";
  }

  public static void concat( StringBuffer sb, String[] vals, int k )
  {
    if ( k < vals.length ) {
      for ( ; k<vals.length; ++k ) if ( vals[k].length() > 0 ) {
        sb.append(vals[k]);
        break;
      }
      for (++k; k < vals.length; ++k) {
        if ( vals[k].length() > 0 ) sb.append(" ").append(vals[k]);
      }
      sb.append(" ");
    }
  }

  public static String noSpaces( String s )
  {
    return ( s == null )? null 
      : s.trim().replaceAll("\\s+", "_").replaceAll("/", "-").replaceAll("\\*", "+").replaceAll("\\\\", "").replaceAll(":","-");
  }

  public static String dropSpaces( String s )
  {
    return ( s == null )? null 
      : s.trim().replaceAll("\\s+", "");
  }

  // sort strings by name (alphabetical order)
  public static void sortStringList( List< String > list )
  {
    if ( list.size() <= 1 ) return;
    Comparator<String> cmp = new Comparator<String>() 
    {
      @Override
      public int compare( String s1, String s2 ) { return s1.compareToIgnoreCase( s2 ); }
    };
    Collections.sort( list, cmp );
  }

  // DATE and TIME -------------------------------------------------------------

  /** @return the current (today) date - format "yyyy.mm.dd"
   * @note month mm ranges from 01 to 12
   *       day dd ranges from 01 to 31
   */
  public static String currentDate()
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    return sdf.format( new Date() );
  }

  /** @return the current date-time - format "yyyy.mm.dd-hh:mm"
   */
  public static String currentDateTime()
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd-HH:mm", Locale.US );
    return sdf.format( new Date() );
  }

  /** @return the current date-time - Bric4 format "yyyy mm dd hh mm ss"
   */
  public static String currentDateTimeBric()
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy MM dd HH mm ss", Locale.US );
    return sdf.format( new Date() );
  }

  /** @return the current minute-seconds - format "mm:ss"
   */
  public static String currentMinuteSecond()
  {
    SimpleDateFormat sdf = new SimpleDateFormat( "mm:ss", Locale.US );
    return sdf.format( new Date() );
  }

  /** @return the current date with the given format
   * @param format   given format
   */
  public static String getDateString( String format )
  {
    SimpleDateFormat sdf = new SimpleDateFormat( format, Locale.US );
    return sdf.format( new Date() );
  }

  /** parse a day 2-char string
   * @param str    day string
   * @return the day as a number (in 1 .. 31)
   */
  public static int parseDay( String str )
  {
    int ret = 10 * ( str.charAt(0) - '0' ) + ( str.charAt(1) - '0' );
    if ( ret < 1 || ret > 31 ) {
      if ( TDandroid.BELOW_API_26 ) {
        ret = 1;
      } else {
        throw new DateTimeException( "Illegal day " + str );
      }
    }
    return ret;
  }

  /** parse a month 2-char string
   * @param str    month string
   * @return the month as a number (in 1 .. 12)
   */
  public static int parseMonth( String str )
  {
    int ret = 10 * ( str.charAt(0) - '0' ) + ( str.charAt(1) - '0' );
    if ( ret < 1 || ret > 12 ) {
      if ( TDandroid.BELOW_API_26 ) {
        ret = ( ret < 1 )? 1 : 12;
      } else {
        throw new DateTimeException( "Illegal month " + str );
      }
    }
    return ret;
  }

  /** @return the current date as a Polygon float
   */
  public static float getDatePlg( ) // Polygon style date
  {
    Calendar c = new GregorianCalendar();
    int y = c.get( Calendar.YEAR );
    int m = 1 + c.get( Calendar.MONTH );
    int d = c.get( Calendar.DAY_OF_MONTH );
    return getDatePlg( y, m, d );
  }

  /** days before a given month (month 0 is January)
   */
  private static final int[] mDaysByMonth = { 0, 31, 59, 90, 120, 151, 181,  212, 243, 273, 304, 324, 365 };

  /** @return a date as Polygon float
   * @param y  year
   * @param m  month, in 1 .. 12
   * @param d  day, in 1 .. 31
   */
  public static float getDatePlg( int y, int m, int d )
  {
    int days = 36524; // 100 * 365 + 24 = from 1900.01.01 to 1999.12.31
    boolean leap = ( (y%4) == 0 ); 
    while ( y > 2000 ) {
      days += 365;
      if ( (y % 4) == 0 ) ++ days;
      -- y;
    }
    days += mDaysByMonth[ m-1 ] + d + 1; //
    if ( leap && m > 2 ) ++ days;
    // TDLog.v("Polygon date " + y + " " + m + " " + d +  " " + days );
    return days;
  }

  /** @return the year from a topodroid date string
   * @param date   topodroid date string (yyyy.mm.dd)
   */
  public static int dateParseYear( String date )
  {
    if ( date != null && date.length() >= 4 ) {
      try {
        return Integer.parseInt( date.substring(0, 4) );
      } catch ( NumberFormatException e ) { }
    }
    return 1970;
  }

  /** @return the month from a topodroid date string
   * @param date   topodroid date string (yyyy.mm.dd)
   */
  public static int dateParseMonth( String date )
  {
    int ret = 0;
    if ( date != null && date.length() >= 7 ) {
      if ( date.charAt(5) == '1' ) ret += 10;
      char ch = date.charAt(6);
      if ( ch >= '0' && ch <= '9' ) ret += (ch - '0');
    }
    return (ret > 0)? ret-1 : 0;
  }

  /** @return the month-day from a topodroid date string
   * @param date   topodroid date string (yyyy.mm.dd)
   */
  public static int dateParseDay( String date )
  {
    int ret = 1;
    if ( date != null && date.length() >= 10 ) {
      char ch = date.charAt(8);
      if ( ch >= '1' && ch <= '3' ) ret += 10*(ch - '0');
      ch = date.charAt(9);
      if ( ch >= '1' && ch <= '9' ) ret += (ch - '0');
    }
    return Math.max(ret, 0);
  }

  /** @return the topodroid date string
   * @param y  year
   * @param m  month (1 .. 12)
   * @param d  day of the month (1 .. 31)
   */
  public static String composeDate( int y, int m, int d )
  {
    return String.format(Locale.US, "%04d.%02d.%02d", y, m+1, d );
  }

  /** @return the VTopo date string (DD/MM/YYYY) from a topodroid date string (YYYY.MM.DD)
   * @param date   topodroid date string (yyyy.mm.dd)
   */
  public static String toVTopoDate( String date )
  {
    return date.substring(8,10) + "/" + date.substring(5,7) + "/" + date.substring(0,4);
  }

  /** @return date in TopoDroid format YYYY.MM.DD from a VTopo date string
   * @param date date in VTopo format DD/MM/YYYY
   */
  public static String fromVTopoDate( String date )
  {
    return date.substring(6,10) + "." + date.substring(3,5) + "." + date.substring(0,2);
  }

  /** @return the current year
   */
  public static int year()  { return (new GregorianCalendar()).get( Calendar.YEAR ); }

  /** @return the current month (in 0 .. 11)
   * @note month 0=Jan.
   */
  public static int month() { return (new GregorianCalendar()).get( Calendar.MONTH ); }

  /** @return the current day of the month (in 1 .. 31)
   */
  public static int day()   { return (new GregorianCalendar()).get( Calendar.DAY_OF_MONTH); }

  /** @return the string presentation of a time interval
   * @param age   time interval [milliseconds]
   */
  public static String getAge( long age )
  {
    age /= 60000;
    if ( age < 120 ) return age + "\'"; // Long.toString(age) + "\'";
    age /= 60;
    if ( age < 24 ) return age + "h"; // Long.toString(age) + "h";
    age /= 24;
    if ( age < 60 ) return age + "d"; // Long.toString(age) + "d";
    age /= 30;
    if ( age < 24 ) return age + "m"; // Long.toString(age) + "m";
    age /= 12;
    return age + "y"; // Long.toString(age) + "y";
  }

  // SLOW ----------------------------------------------------

  /** sleep the thread
   * @param msec   sleep time [milliseconds]
   */
  public static boolean slowDown( int msec ) 
  {
    try {
      Thread.sleep( msec );
    } catch ( InterruptedException e ) { return false; }
    return true;
  }

  /** sleep the thread
   * @param msec   sleep time [milliseconds]
   * @param msg    interrupt message
   */
  public static boolean slowDown( int msec, String msg )
  {
    try {
      Thread.sleep( msec );
    } catch ( InterruptedException e ) {
      TDLog.Error( msg + " " + e.getMessage() );
      return false;
    }
    return true;
  }

  /** yield and sleep the thread
   * @param msec   sleep time [milliseconds]
   */
  public static boolean yieldDown( int msec ) 
  {
    try {
      Thread.yield();
      Thread.sleep( msec );
    } catch ( InterruptedException e ) { return false; }
    return true;
  }

  /** replace a xsection prefix in the scrap name and prepend the survey name
   * @param surveyname   name of the survey
   * @param scrapname    name of the Xsection scrap
   * @return ...
   */
  public static String replacePrefix( String surveyname, String scrapname ) 
  {
    if ( scrapname == null ) return null;
    int pos = scrapname.lastIndexOf( "-xx" );
    if ( pos < 0 ) pos = scrapname.lastIndexOf( "-xs-" );
    if ( pos < 0 ) pos = scrapname.lastIndexOf( "-xh-" );
    if ( pos < 0 ) return null;
    return surveyname + scrapname.substring( pos );
  }

  /** @return true if the list is not empty
   * @param list   list to check
   */
  public static <E> boolean isNonEmpty( List< E > list ) 
  {
    return list != null && list.size() > 0;
  }

  /** @return true if the list is empty
   * @param list   list to check
   */
  public static <E> boolean isEmpty( List< E > list ) 
  {
    return list == null || list.size() == 0;
  }


}
