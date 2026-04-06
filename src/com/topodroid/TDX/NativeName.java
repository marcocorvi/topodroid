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
  public native static String incrementName( String name, Set<String> stations );

  public native void initLog();

  static {
    System.loadLibrary( "nativename" );
  }

  NativeName()
  {
    initLog();
  }

  static NativeName get()
  {
    try {
      return new NativeName();
      // TDLog.v( "Using native name lib" );
    } catch ( java.lang.UnsatisfiedLinkError e ) {
      TDLog.e("Native link error " + e.getMessage() );
    }
    return null;
  }                            
    
}

