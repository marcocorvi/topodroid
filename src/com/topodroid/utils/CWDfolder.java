/* @file CWDfolder.java
 *
 * @author marco corvi
 * @date oct 2024
 *
 * @brief TopoDroid CWD folder functions
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import java.util.Locale;

public class CWDfolder
{
  static final public int NAME_OK    = 0;
  static final public int NAME_EMPTY = 1;
  static final public int NAME_BAD   = 2;

  public static int checkName( String name ) 
  {
    if ( name == null )       return NAME_EMPTY;
    if ( name.length() == 0 ) return NAME_EMPTY;
    if ( name.contains("/") ) return NAME_BAD;
    return NAME_OK;
  }

  public static String folderName( String name )
  {
    if ( ! name.toUpperCase(Locale.US).startsWith( "TOPODROID" ) ) {
      return "TopoDroid-" + name;
    }
    if ( name.length() > 9 ) {
      if ( name.charAt(9) == '-' ) {
        return "TopoDroid" + name.substring(9);
      } 
      return "TopoDroid-" + name.substring(9);
    }
    return "TopoDroid";
  }   

}

