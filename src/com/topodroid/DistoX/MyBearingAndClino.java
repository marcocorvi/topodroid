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

import java.util.Locale;

import android.widget.ImageView;

import android.graphics.Matrix;
import android.graphics.Bitmap;

import android.media.ExifInterface;

import android.util.Log;

class MyBearingAndClino implements IBearingAndClino
{
  TopoDroidApp mApp;
  private File  mFile;
  // long  mPid;
  float mBearing;
  float mClino;
  int   mOrientation; // camera orientation in degrees

  MyBearingAndClino( TopoDroidApp app, File imagefile /*, long pid */ )
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
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
    }
    setExifBearingAndClino( mFile, mBearing, mClino, mOrientation );
    // ExifInterface exif = new ExifInterface( mFile.getPath() );
    // String.format(Locale.US, "%.2f %.2f", mBearing, mClino );
    // // Log.v("DistoX", "save. orientation " + mOrientation );
    // int rot = getExifOrientation( mOrientation );
    // exif.setAttribute( ExifInterface.TAG_ORIENTATION, String.format(Locale.US, "%d", rot) );
    // exif.setAttribute( ExifInterface.TAG_DATETIME, TopoDroidUtil.currentDateTime() );
    // exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE, String.format(Locale.US, "%d/100", (int)(mClino*100) ) );
    // exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE_REF, "N" );
    // exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE, String.format(Locale.US, "%d/100", (int)(mBearing*100) ) );
    // exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE_REF, "E" );
    // exif.saveAttributes();
  }

  static void setExifBearingAndClino( File file, float b, float c, int o )
  {
    try {
      ExifInterface exif = new ExifInterface( file.getPath() );
      String.format(Locale.US, "%.2f %.2f", b, c );
      int rot = getExifOrientation( o );
      exif.setAttribute( ExifInterface.TAG_ORIENTATION, String.format(Locale.US, "%d", rot) );
      exif.setAttribute( ExifInterface.TAG_DATETIME, TopoDroidUtil.currentDateTime() );
      exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE, String.format(Locale.US, "%d/100", (int)(c*100) ) );
      exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE_REF, "N" );
      exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE, String.format(Locale.US, "%d/100", (int)(b*100) ) );
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
  static void applyOrientation( ImageView image, Bitmap bitmap, int orientation )
  {
    Bitmap bm = null ;
    Matrix m = new Matrix();
    int w = bitmap.getWidth();
    int h = bitmap.getHeight();
    if ( orientation == 6 ) {
      m.setRotate(  90f, w/2, h/2 );
      // image.setRotation(  90 ); // requires API level 14
    } else if ( orientation == 3 ) {
      m.setRotate( 180f, w/2, h/2 );
      // image.setRotation( 180 );
    } else if ( orientation == 8 ) {
      m.setRotate( 270f, w/2, h/2 );
      // image.setRotation( 270 );
    } else {
      image.setImageBitmap( bitmap );
      return;
    }
    bm = Bitmap.createBitmap( bitmap, 0, 0, w, h, m, true );
    image.setImageBitmap( bm );
  }

  // jpegexiforient man page has
  //   up              down                     left               right
  // 1 xxxx  2 xxxx  3    x  4 x    5 xxxxxx  6 x      7      x  8 xxxxxx
  //   x          x      xx    xx     x  x      x  x       x  x      x  x
  //   xx        xx       x    x      x         xxxxxx   xxxxxx         x
  //   x          x    xxxx    xxxx   
  //
  static private int getExifOrientation( int orientation )
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
