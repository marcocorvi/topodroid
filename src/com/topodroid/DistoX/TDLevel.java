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
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
// import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
// import android.provider.Settings.Secure;

public class TDLevel
{
  public static final int BASIC    = 0;
  public static final int NORMAL   = 1;
  public static final int ADVANCED = 2;
  public static final int EXPERT   = 3;
  public static final int TESTER   = 4;
  // static final private int COMPLETE = 5;

  public static int mLevel = 1; // activity level

  public static boolean overBasic    = true;
  public static boolean overNormal   = false;
  public static boolean overAdvanced = false;
  public static boolean overExpert   = false;
  public static boolean overTester   = false;
  private static boolean test_debug = true;
  private static boolean debug = false;

  /** set the activity level
   * @param ctx    context
   * @param level  activity level
   */
  public static void setLevel( Context ctx, int level )
  {
    mLevel = level;

    // FIXME_DEVELOPER
    if ( test_debug ) {
      // mAndroidId = Secure.getString( ctx.getContentResolver(), Secure.ANDROID_ID );
      try {
        final PackageInfo info = ctx.getPackageManager().getPackageInfo( ctx.getPackageName(), 0);
        debug = (info.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
      } catch ( NameNotFoundException e ) {
      }
      test_debug = false;
      TDLog.v("DEBUG " + debug );
    }
    overBasic    = mLevel > BASIC;
    overNormal   = mLevel > NORMAL;
    overAdvanced = mLevel > ADVANCED;
    overExpert   = mLevel > EXPERT;
    // overTester  = mLevel > TESTER;
    // FIXME_DEVELOPER
    if ( overExpert && debug ) overTester = true;
  }
}
