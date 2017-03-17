/** @file MyBearingAndClino.java
 *
 * @author marco corvi 
 * @date nov 2013
 *
 * @brief TopoDroid bearing and clino interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.widget.ImageView;

import android.media.ExifInterface;

import android.util.Log;

class MyBearingAndClino implements IBearingAndClino
{
  TopoDroidApp mApp;
  File  mFile;
  // long  mPid;
  float mBearing;
  float mClino;
  int   mOrientation; // camera orientation in degrees

  public MyBearingAndClino( TopoDroidApp app, File imagefile /*, long pid */ )
  {
    mApp  = app; 
    mFile = imagefile;
    // mPid  = pid;
    mBearing = 0;
    mClino = 0;
    mOrientation = 0;
  }

  // @param b0 bearing
  // @param c0 clino
  public void setBearingAndClino( float b0, float c0, int o0 )
  {
    // Log.v("DistoX", "Bearing and Clino orientation " + o0 );
    // this is not good for photo because it might alter azimuth/clino of xsection sketch
    // mApp.mData.updatePlotAzimuthClino( mApp.mSID, mPid, b0, c0 );
    mBearing = b0;
    mClino   = c0;
    mOrientation = o0;
  }

  public void setJpegData( byte[] data )
  {
    try {
      FileOutputStream fos = new FileOutputStream( mFile );
      fos.write( data );
      fos.flush();
      fos.close();
      ExifInterface exif = new ExifInterface( mFile.getPath() );
      String.format("%.2f %.2f", mBearing, mClino );
      // Log.v("DistoX", "save. orientation " + mOrientation );
      int rot = getExifOrientation( mOrientation );
      exif.setAttribute( ExifInterface.TAG_ORIENTATION, Integer.toString(rot) );
      exif.setAttribute( ExifInterface.TAG_DATETIME, TopoDroidUtil.currentDateTime() );
      exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE, String.format("%d/100", (int)(mClino*100) ) );
      exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE_REF, "N" );
      exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE, String.format("%d/100", (int)(mBearing*100) ) );
      exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE_REF, "E" );
      exif.saveAttributes();
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
    }
  }

  // 1: no rotation
  // 6: rotate right
  // 3: rotate 180
  // 8: rotate left
  static void applyOrientation( ImageView image, int orientation )
  {
    if ( orientation == 6 ) {
      image.setRotation(  90 );
    } else if ( orientation == 3 ) {
      image.setRotation( 180 );
    } else if ( orientation == 8 ) {
      image.setRotation( 270 );
    }
  }

  // jpegexiforient man page has
  //   up              down                     left               right
  // 1 xxxx  2 xxxx  3    x  4 x    5 xxxxxx  6 x      7      x  8 xxxxxx
  //   x          x      xx    xx     x  x      x  x       x  x      x  x
  //   xx        xx       x    x      x         xxxxxx   xxxxxx         x
  //   x          x    xxxx    xxxx   
  //
  static int getExifOrientation( int orientation )
  {
    if ( orientation <  45 ) return 6; // up
    if ( orientation < 135 ) return 3; // right
    if ( orientation < 225 ) return 8; // down
    if ( orientation < 315 ) return 1; // left
    return 6; // up
  }

  static int getCameraOrientation( int orientation )
  {
    if ( orientation <  45 ) return 0;
    if ( orientation < 135 ) return 90; // right
    if ( orientation < 225 ) return 180; // down
    if ( orientation < 315 ) return 270; // left
    return 0; // up
  }

}
