/** @file Archiver.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D tiff decoder java-side
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import android.graphics.Bitmap;

// import java.io.InputStreamReader;

public class TiffFactory
{
  static {
    // TDLog.v("load TIFF library" );
    System.loadLibrary( "tiff" );
    System.loadLibrary( "tiffdecoder" );
  }

  public static native Bitmap getBitmap( String path, double x1, double y1, double x2, double y2 );

}
