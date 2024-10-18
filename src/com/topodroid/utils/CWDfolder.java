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
  static final public int NAME_TMP   = 3;

  public static int checkName( String name ) 
  {
    if ( name == null )       return NAME_EMPTY;
    if ( name.length() == 0 ) return NAME_EMPTY;
    if ( name.contains("/") ) return NAME_BAD;
    if ( name.toLowerCase().equals("tmp") ) return NAME_TMP;
    return NAME_OK;
  }

  /** @return true if the folder name is OK
   * @param name   folder name
   */
  public static boolean isNameOk( String name ) { return checkName( name ) == NAME_OK; }

  /** @return the folder name for a given string, ie, the string itself or "TopoDroid" if the string is empty.
   * @param name   input name
   */
  public static String folderName( String name )
  {
    if ( checkName( name ) == NAME_OK ) return name;
    return "TopoDroid";
    // if ( name.equals("TopoDroid") ) return name; // "TopoDroid" is OK
    // if ( ! name.toUpperCase(Locale.US).startsWith( "TOPODROID" ) ) { // other folders have name "TopoDroid-XXX"
    //   return "TopoDroid-" + name;
    // } else if ( name.length() > 9 ) {
    //   if ( name.charAt(9) == '-' ) {
    //     if ( name.length() == 10 ) { // "TopoDroid-" not allowed
    //       return "TopoDroid";
    //     } else {
    //       return "TopoDroid" + name.substring(9); // folder "TopoDroid-XXX" for name "TopoDroid-XXX"
    //     }
    //   } 
    //   return "TopoDroid-" + name.substring(9); // folder "TopoDroid-XXX" for name "TopoDroidXXX"
    // }
    // return "TopoDroid";
  }   

  // /** @return true if the folder name starts with "TopoDroid" (case-independent)
  //  * @param name   folder name
  //  */
  // public static boolean checkTopoDroid( String name )
  // {
  //   if ( checkName( name ) != NAME_OK ) return false;
  //   return name.toLowerCase( Locale.getDefault() ).startsWith( "topodroid" );
  // }

}

