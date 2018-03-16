/* @file DrawingOutlinePath.java
 *
 * @author marco corvi
 * @date sept 2017
 *
 * @brief TopoDroid drawing: outline-path
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

/**
 */
class DrawingOutlinePath
{
  private String mScrap;  // scrap name
  DrawingLinePath mPath;

  DrawingOutlinePath( String name, DrawingLinePath path )
  {
    mScrap = name;
    mPath  = path;
  }

  boolean isScrap( String name ) { return mScrap.equals( name ); }

}

