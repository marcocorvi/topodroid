/* @file TDVersion.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid version
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.utils.TDLog;

import android.util.Log;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Configuration;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class TDVersion 
{
  // symbol version of installed symbols is stored in the database
  // symbol version of the current  symbols is in the app
  public static final String SYMBOL_VERSION = "36";
  
  // database version
  public static final String DB_VERSION = "42"; // FIXME agrees with Cave3DThParser values
  public static final int DATABASE_VERSION = 42;
  public static final int DATABASE_VERSION_MIN = 21; // was 14

  // TopoDroid version: this is loaded from the Manifest
  private static String VERSION = "0.0.0"; 
  private static int VERSION_CODE = 0;
  private static int MAJOR = 0;
  private static int MINOR = 0;
  private static int SUB   = 0;
  private static char VCH  = ' ';

  // minimum compatible TopoDroid version
  private static final int MAJOR_MIN = 2;
  private static final int MINOR_MIN = 1;
  private static final int SUB_MIN   = 1;
  private static final int CODE_MIN  = 20101;
  
  public static String string()  { return VERSION; }
  public static int    code()    { return VERSION_CODE; }
  public static String symbols() { return SYMBOL_VERSION; }

  public static boolean setVersion( Context context )
  {
    // TDLog.Profile("TDApp onCreate");
    try {
      VERSION      = context.getPackageManager().getPackageInfo( context.getPackageName(), 0 ).versionName;
      VERSION_CODE = context.getPackageManager().getPackageInfo( context.getPackageName(), 0 ).versionCode;
      int v = VERSION_CODE;
      String[] ver = VERSION.split("\\.");
      if ( ver.length > 2 ) {
        try {
          MAJOR = Integer.parseInt( ver[0] );
          MINOR = Integer.parseInt( ver[1] );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse error: major/minor " + ver[0] + " " + ver[1] );
        }
        int k = 0;
        SUB = 0;
        while ( k < ver[2].length() ) {
          char ch = ver[2].charAt(k);
          if ( ch < '0' || ch > '9' ) { VCH = ch; break; }
          SUB = 10 * SUB + (int)(ch - '0');
          ++k;
        }
      } else {
        MAJOR = v /    100000;    
        v -= MAJOR *   100000;
        MINOR = v /      1000;    
        v -= MINOR *     1000;
        SUB = v /          10;
        v -= SUB *         10;
        VCH = (char)('a' + v); // FIXME
      }
      // Log.v("DistoX", "Major " + MAJOR + " minor " + MINOR + " sub " + SUB + VCH );
      return true;
    } catch ( NameNotFoundException e ) {
      // FIXME
      e.printStackTrace();
    }
    return false;
  }


  public static int check( String version_line )
  {
    int ret = 0;
    int version_code = 0;
    String[] vers = version_line.split(" ");
    for ( int k=1; k<vers.length; ++ k ) {
      if ( vers[k].length() > 0 ) {
        try {
          version_code = Integer.parseInt( vers[k] );
          break;
        } catch ( NumberFormatException e ) { }
      }
    }
    if ( version_code == 0 ) {
      String[] ver = vers[0].split("\\.");
      int major = 0;
      int minor = 0;
      int sub   = 0;
      char vch  = ' '; // char order: ' ' < A < B < ... < a < b < ... < '}' 
      if ( ver.length > 2 ) { // M.m.sv version code
        try {
          major = Integer.parseInt( ver[0] );
          minor = Integer.parseInt( ver[1] );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse error: major/minor " + ver[0] + " " + ver[1] );
          return -2;
        }
        int k = 0;
        while ( k < ver[2].length() ) {
          char ch = ver[2].charAt(k);
          if ( ch < '0' || ch > '9' ) { vch = ch; break; }
          sub = 10 * sub + (int)(ch - '0');
          ++k;
        }
        // Log.v( "DistoX", "Version " + major + " " + minor + " " + sub );
        if (    ( major < MAJOR_MIN )
             || ( major == MAJOR_MIN && minor < MINOR_MIN )
             || ( major == MAJOR_MIN && minor == MINOR_MIN && sub < SUB_MIN ) 
          ) {
          TDLog.Log( TDLog.LOG_ZIP, "TopoDroid version mismatch: " + version_line + " < " + MAJOR_MIN + "." + MINOR_MIN + "." + SUB_MIN );
          return -2;
        }
        if (    ( major > MAJOR ) 
             || ( major == MAJOR && minor > MINOR )
             || ( major == MAJOR && minor == MINOR && sub > SUB ) ) {
          ret = 1; 
        } else if ( major == MAJOR && minor == MINOR && sub == SUB && vch > ' ' ) {
          if ( VCH == ' ' ) { 
            ret = 1;
          } else if ( VCH <= 'Z' && ( vch >= 'a' || vch < VCH ) ) { // a-z or vch(A-Z) < VCH
            ret = 1;
          } else if ( VCH >= 'a' && vch < VCH ) { // A-Z < a-z 
            ret = 1;
          }
        }

      } else { // version code
        try {
          version_code = Integer.parseInt( ver[0] );
          if ( version_code < CODE_MIN ) {
            TDLog.Log( TDLog.LOG_ZIP, "TopoDroid version mismatch: " + version_line + " < " + CODE_MIN );
            return -2;
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse error: version code " + ver[0] );
          return -2;
        }
        if ( version_code > VERSION_CODE ) ret = 1;
      }
    } else {
      if ( version_code > VERSION_CODE ) ret = 1;
    }
    return ret;
  }

}
