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

// import com.topodroid.utils.TDLog;

import android.util.Log;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.content.res.Configuration;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class TDVersion 
{
  // symbol version of installed symbols is stored in the database
  // symbol version of the current  symbols is in the app
  public static final String SYMBOL_VERSION = "37";
  
  // database version
  public static final String DB_VERSION = "44"; // FIXME agrees with Cave3DThParser values
  public static final int DATABASE_VERSION = 44;
  public static final int DATABASE_VERSION_MIN = 21; // was 14

  public static final int DEVICE_DATABASE_VERSION = 27;
  // public static final int DEVICE_DATABASE_VERSION_MIN = 21; // was 14

  public static final int PACKET_DATABASE_VERSION = 1;
  public static final int PACKET_DATABASE_VERSION_MIN = 1; // was 14

  // TopoDroid version: this is loaded from the Manifest
  public static String VERSION = "0.0.0"; 
  public static int VERSION_CODE = 0;
  public static int MAJOR = 0;
  public static int MINOR = 0;
  public static int SUB   = 0;
  public static char VCH  = ' ';

  // minimum compatible TopoDroid version
  public static final int MAJOR_MIN = 2;
  public static final int MINOR_MIN = 1;
  public static final int SUB_MIN   = 1;
  public static final int CODE_MIN  = 20101;
  
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
          ApplicationInfo app_info = context.getApplicationInfo();
          Log.e( app_info.processName, "parse error: major/minor " + ver[0] + " " + ver[1] );
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

}
