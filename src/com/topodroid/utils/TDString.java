/* @file TDString.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid fixed strings
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

public class TDString
{
  public static final String EMPTY = "";
  public static final String SPACE = " ";

  public static final String ZERO  = "0";
  public static final String ONE   = "1";
  public static final String TWO   = "2";
  public static final String THREE = "3";
  public static final String FOUR  = "4";
  public static final String FIVE  = "5";
  public static final String TEN   = "10";
  public static final String TWENTY    = "20";
  public static final String TWENTYFOUR = "24";
  public static final String FIFTY     = "50";
  public static final String SIXTY     = "60";
  public static final String NINETY    = "90";
  public static final String NINETYONE = "91";

  public static final String OPTION_SCRAP = "-scrap";

  public static String escape( String str )
  {
    if ( str == null ) return "";
    return str.replace('"', '\u001b' );
  }

  public static String unescape( String str )
  {
    if ( str == null ) return "";
    return str.replace('\u001b', '"' );
  }
}
