/* @file MyBearingAndClino.java
 *
 * @author marco corvi 
 * @date nov 2013
 *
 * @brief TopoDroid bearing and clino interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDVersion;

// import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Locale;

// import android.widget.ImageView;

// import android.graphics.Matrix;
// import android.graphics.Bitmap;

import android.media.ExifInterface; // REQUIRES android.support

public class MyBearingAndClino implements IBearingAndClino
{
  final static int ORIENTATION_UP    =   0;
  final static int ORIENTATION_RIGHT =  90;
  final static int ORIENTATION_DOWN  = 180;
  final static int ORIENTATION_LEFT  = 270;
  final static int EXIF_UP    = 6;
  final static int EXIF_RIGHT = 3;
  final static int EXIF_DOWN  = 8;
  final static int EXIF_LEFT  = 1;

  private final TopoDroidApp mApp;
  // private File  mFile;
  private String mFilepath;  // file full pathname
  // long  mPid;             // plot id
  private float mBearing;
  private float mClino;
  private int   mOrientation; // camera orientation in degrees

  /** cstr
   * @param app         application
   * @param imagefile   image file full path
   */
  MyBearingAndClino( TopoDroidApp app, String imagefile /*, long pid */ )
  {
    mApp  = app; 
    // mFile = imagefile;
    mFilepath = imagefile; // mFile.getPath();
    // mPid  = pid;
    mBearing = 0;
    mClino = 0;
    mOrientation = 0;
  }

  /** set azimuth/clino and orientation index 
   * @param b0 azimuth [degrees]
   * @param c0 clino [degrees]
   * @param o  camera orientation [degrees], 0: up, 90: right, etc.
   */
  public void setBearingAndClino( float b0, float c0, int o0 )
  {
    // TDLog.v( "BearingClino UI set orientation " + o0 + " bearing " + b0 + " clino " + c0 );
    // TDLog.v( "Bearing and Clino orientation " + o0 );
    // this is not good for photo because it might alter azimuth/clino of xsection sketch
    // mApp.mData.updatePlotAzimuthClino( TDInstance.sid, mPid, b0, c0 );
    mBearing = b0;
    mClino   = c0;
    mOrientation = o0;
  }

  /** write the image data to the (output) file - and stores azimuth/clino as well
   * @param data    image data
   * @return true on success
   */
  public boolean setJpegData( byte[] data )
  {
    if ( data == null ) return false; // FIXME crash 2020-08-09
    try {
      FileOutputStream fos = TDFile.getFileOutputStream( mFilepath );
      fos.write( data );
      fos.flush();
      fos.close();
      // TDLog.v( "BearingClino UI saved JPEG file " + mFile.getPath() );
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
      return false;
    }
    setExifBearingAndClino( mFilepath, mBearing, mClino, mOrientation );
    return true;
  }

  // ANDROID-11
  // ExifInterface can be instantiated from a filename (String), a file descriptor, or an input_stream
  // In the last case it does not support attributes write
  //
  /** set the values of azimuth and clino in the file exif tags
   * @param filepath   image filepath (file.getPath())
   * @param b          azimuth [degrees]
   * @param c          clino [degrees]
   * @param o          camera orientation [degrees]
   */
  static void setExifBearingAndClino( String filepath, float b, float c, int o )
  {
    TDLog.v( "BearingClino UI set exif " + b + " " + c + " file " + filepath );
    try {
      ExifInterface exif = new ExifInterface( filepath );
      // String.format(Locale.US, "%.2f %.2f", b, c );
      int rot = getExifOrientation( o );
      if ( TDandroid.AT_LEAST_API_24 ) { // at least Android-7 (N)
        exif.setAttribute( ExifInterface.TAG_SOFTWARE, "TopoDroid " + TDVersion.string() );
      }
      exif.setAttribute( ExifInterface.TAG_ORIENTATION, String.format(Locale.US, "%d", rot) );
      exif.setAttribute( ExifInterface.TAG_DATETIME, TDUtil.currentDateTime() );
      int cint = (int)(c*100);
      int bint = (int)(b*100);
      TDLog.v("EXIF b " + bint + " c " + cint );
      if ( cint >= 0 ) {
        exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE, String.format(Locale.US, "%d/100", cint ) );
        exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE_REF, "N" );
      } else {
        exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE, String.format(Locale.US, "%d/100", -cint ) );
        exif.setAttribute( ExifInterface.TAG_GPS_LATITUDE_REF, "S" );
      }
      if ( bint >= 0 ) {
        exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE, String.format(Locale.US, "%d/100", bint ) );
        exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE_REF, "E" );
      } else {
        exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE, String.format(Locale.US, "%d/100", -bint ) );
        exif.setAttribute( ExifInterface.TAG_GPS_LONGITUDE_REF, "W" );
      }
      // FIXME-GPS_LATITUDE work-around for tag GPS Latitude not supported correctly
      if ( TDandroid.AT_LEAST_API_24 ) { // at least Android-7 (N)
        exif.setAttribute( ExifInterface.TAG_IMAGE_DESCRIPTION, String.format(Locale.US, "%d %d", bint, cint ) );
      }
      exif.saveAttributes();
    } catch ( IOException e ) {
      TDLog.Error( "Set exif: IO exception " + e.getMessage() );
      // TDLog.v( "Bearing/Clino IO exception " + e.getMessage() );
    }
  }

  /** @return EXIF orientation index from the orientation angle
   * @param orientation   orientation angle [degrees]
   *
   * jpegexiforient man page has
   *   up              down                     left               right
   * 1 xxxx  2 xxxx  3    x  4 x    5 xxxxxx  6 x      7      x  8 xxxxxx
   *   x          x      xx    xx     x  x      x  x       x  x      x  x
   *   xx        xx       x    x      x         xxxxxx   xxxxxx         x
   *   x          x    xxxx    xxxx   
   */
  static private int getExifOrientation( int orientation )
  {
    if ( orientation <  45 ) return EXIF_UP;
    if ( orientation < 135 ) return EXIF_RIGHT;
    if ( orientation < 225 ) return EXIF_DOWN;
    if ( orientation < 315 ) return EXIF_LEFT;
    return EXIF_UP;
  }

  /** @return camera orientation [degrees] from the orientation angle
   * @param orientation   orientation angle [degrees]
   */
  static int getCameraOrientation( int orientation )
  {
    if ( orientation <  45 ) return ORIENTATION_UP;
    if ( orientation < 135 ) return ORIENTATION_RIGHT;
    if ( orientation < 225 ) return ORIENTATION_DOWN;
    if ( orientation < 315 ) return ORIENTATION_LEFT;
    return ORIENTATION_UP;
  }

}
