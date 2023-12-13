/* @file TDLevel.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid activity level(s)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
// import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.topodroid.utils.TDLog;
// import android.provider.Settings.Secure;

public class TDLevel
{
  public static final int BASIC    = 0;
  public static final int NORMAL   = 1;
  public static final int ADVANCED = 2;
  public static final int EXPERT   = 3;
  public static final int TESTER   = 4;
  public static final int DEBUG    = 5;
  // static final private int COMPLETE = 5;

  public static int mLevel = 1; // activity level

  public static boolean overBasic    = true;
  public static boolean overNormal   = false;
  public static boolean overAdvanced = false;
  public static boolean overExpert   = false;
  public static boolean overTester   = false;
  private static boolean mDebug = false;

  /** @return true if the app is debug-build
   */
  public static boolean isDebugBuild( )
  {
    Context ctx = TDInstance.context;
    try {
      final PackageInfo info = ctx.getPackageManager().getPackageInfo( ctx.getPackageName(), 0);
      return (info.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    } catch ( NameNotFoundException e ) {
      TDLog.Error( e.getMessage() );
    }
    return false;
  }

  /** set the activity level
   * @param ctx    context
   * @param level  activity level
   */
  public static void setLevel( Context ctx, int level )
  {
    mLevel = level;

    // FIXME_DEVELOPER
    mDebug = ( TDSetting.mWithDebug )? isDebugBuild( ) : false;
    overBasic    = mLevel > BASIC;
    overNormal   = mLevel > NORMAL;
    overAdvanced = mLevel > ADVANCED;
    overExpert   = mLevel > EXPERT;
    // overTester  = mLevel > TESTER;
    // FIXME_DEVELOPER
    if ( overExpert && mDebug && TDSetting.mWithDebug ) {
      overTester = true;
      TglParser.setWallMax();
      mLevel = DEBUG; // N.B. this causes all DEBUG settings FIXME_FIXME
      TDLog.v("LEVEL: over tester");
    }
  }

  public static void setLevelWithDebug( boolean with_debug )
  {
    if ( ! with_debug ) { 
      overTester = false;
      if ( overTester ) { // lower level
        mLevel = TESTER;
      }
    } else {
      if ( overExpert ) {
        if ( ! overTester ) { // raise level
          overTester = true;
          TglParser.setWallMax();
          mLevel = DEBUG; // N.B. this causes all DEBUG settings FIXME_FIXME
        }
      } else {
        overTester = false;
      }
    }
  }
    
}
