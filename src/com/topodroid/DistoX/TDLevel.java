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

import android.content.Context;
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
  boolean mDeveloper   = false;
  static String  mAndroidId   = null;

  /** set the activity level
   * @param ctx    context
   * @param level  activity level
   */
  public static void setLevel( Context ctx, int level )
  {
    mLevel = level;

    // FIXME_DEVELOPER
    // if ( mAndroidId == null ) {
    //   mAndroidId = Secure.getString( ctx.getContentResolver(), Secure.ANDROID_ID );
    //   mDeveloper = "8c894b79b6dce351".equals( mAndroidId );
    //   // "e5582eda21cafac3" // Nexus-4
    // }
    overBasic    = mLevel > BASIC;
    overNormal   = mLevel > NORMAL;
    overAdvanced = mLevel > ADVANCED;
    overExpert   = mLevel > EXPERT;
    // overTester  = mLevel > TESTER;
    // FIXME_DEVELOPER
    // if ( overExpert && mDeveloper ) overTester = true;
  }
}
