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

class TDLevel
{
  static final int BASIC    = 0;
  static final int NORMAL   = 1;
  static final int ADVANCED = 2;
  static final int EXPERT   = 3;
  static final int TESTER   = 4;
  // static final private int COMPLETE = 5;

  static int mLevel = 1; // activity level

  static boolean overBasic    = true;
  static boolean overNormal   = false;
  static boolean overAdvanced = false;
  static boolean overExpert   = false;
  static boolean overTester   = false;
  static boolean mDeveloper   = false;
  static String  mAndroidId   = null;

  static void setLevel( Context ctx, int level )
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
