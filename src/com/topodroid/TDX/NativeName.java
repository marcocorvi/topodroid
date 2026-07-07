/* @file NativeName.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief native name
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;

//  import android.util.FloatMath;
// import java.util.List;
import java.util.Set;

class NativeName
{
  static NativeName mNativeName = null; // singleton
  static boolean hasLib = false;

  public native static String incrementName( String name, Set<String> stations );

  public native void initLog();

  static {
    try {
      System.loadLibrary( "nativename" );
      hasLib = true;
    } catch ( UnsatisfiedLinkError e ) {
      // TODO ?
    }
  }

  private NativeName()
  {
    initLog();
  }

  /** factory method
   * @return the NativeName or null if failed to create
   */
  static NativeName get()
  {
    if ( mNativeName == null ) {
      try {
        mNativeName = new NativeName();
        // TDLog.v( "Using native name lib" );
      } catch ( java.lang.UnsatisfiedLinkError e ) {
        TDLog.e("Native link error " + e.getMessage() );
        mNativeName = null;
      }
    }
    return mNativeName;
  }

  /** utility method to get the next name
   * @param native_name  NativeName object - can be null
   * @param name         name string
   * @param sts          set of names in use
   * @return the next usable name
   */
  static String nextName( NativeName native_name, String name, Set<String> sts )
  {
    return  ( native_name != null )? native_name.incrementName( name, sts ) : DistoXStationName.incrementName( name, sts );
  }


}

