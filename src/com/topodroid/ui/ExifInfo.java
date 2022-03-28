/* @file TDImage.java
 *
 * @author marco corvi
 * @date july 2018
 *
 * @brief TopoDroid image utility
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ui;

import com.topodroid.utils.TDLog;
import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.TDandroid;
import com.topodroid.utils.TDVersion;

import java.io.IOException;
import java.util.Locale;
import android.os.Build;
import android.media.ExifInterface; // REQUIRES android.support

public class ExifInfo
{
  public final static int ORIENTATION_UP    =   0;
  public final static int ORIENTATION_RIGHT =  90;
  public final static int ORIENTATION_DOWN  = 180;
  public final static int ORIENTATION_LEFT  = 270;
  final static int EXIF_UP    = 6;
  final static int EXIF_RIGHT = 3;
  final static int EXIF_DOWN  = 8;
  final static int EXIF_LEFT  = 1;

  private float  mAzimuth = 0;
  private float  mClino   = 0;
  private int mOrientation = 0;
  private String mDate = "";

  /** default cstr
   */
  public ExifInfo( )
  {
  }

  /** cstr
   * @param filename   file path
   */
  public ExifInfo( String filename )
  {
    readExif( filename );
  }

  public float azimuth() { return mAzimuth; }
  public float clino()   { return mClino; }
  public String date()   { return mDate; }
  public int orientation() { return mOrientation; }

  /** set exif values
   * @param azimuth      azimuth [degrees]
   * @param clino        clino [degrees]
   * @param orienatation camera orientation [degrees]
   */
  public void setValues( float azimuth, float clino, int orientation )
  {
    mAzimuth     = azimuth;
    mClino       = clino;
    mOrientation = orientation;
    mDate = TDUtil.currentDateTime();
  }

  // ANDROID-11
  // ExifInterface can be instantiated from a filename (String), a file descriptor, or an input_stream
  // In the last case it does not support attributes write
  //
  /** set the values of azimuth and clino in the file exif tags
   * @param filepath   image filepath (file.getPath())
   */
  public void writeExif( String filepath )
  {
    TDLog.v( "EXIF set " + mAzimuth + " " + mClino + " file " + filepath );
    try {
      ExifInterface exif = new ExifInterface( filepath );
      // String.format(Locale.US, "%.2f %.2f", azimuth, clino );
      int rot = getExifOrientation( mOrientation );
      if ( TDandroid.AT_LEAST_API_24 ) { // at least Android-7 (N)
        exif.setAttribute( ExifInterface.TAG_SOFTWARE, "TopoDroid " + TDVersion.string() );
      }
      exif.setAttribute( ExifInterface.TAG_ORIENTATION, String.format(Locale.US, "%d", rot) );
      exif.setAttribute( ExifInterface.TAG_DATETIME, TDUtil.currentDateTime() );
      int cint = (int)(mClino*100);
      int bint = (int)(mAzimuth*100);
      TDLog.v("EXIF azimuth " + bint + " clino " + cint );
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
      exif.setAttribute( ExifInterface.TAG_GPS_IMG_DIRECTION,  String.format(Locale.US, "%d/100", bint ) );

      // FIXME-GPS_LATITUDE work-around for tag GPS Latitude not supported correctly
      if ( TDandroid.AT_LEAST_API_24 ) { // at least Android-7 (N)
        exif.setAttribute( ExifInterface.TAG_IMAGE_DESCRIPTION, String.format(Locale.US, "%d %d", bint, cint ) );
      }
      exif.saveAttributes();
    } catch ( IOException e ) {
      TDLog.Error( "EXIF: IO exception " + e.getMessage() );
      // TDLog.v( "Bearing/Clino IO exception " + e.getMessage() );
    }
  }

  /** read the image info from the EXIF
   *  - orientation
   *  - azimuth (gps_longitude or image_description)
   *  - clino (gps_latitude or image_description)
   *  - date (datetime)
   */
  private void readExif( String filename )
  {
    try {
      ExifInterface exif = new ExifInterface( filename );
      // mAzimuth = exif.getAttribute( "GPSImgDirection" );
      mOrientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 0 );
      // TDLog.v( "Photo edit orientation " + mOrientation );
      String azimuth = exif.getAttribute( ExifInterface.TAG_GPS_LONGITUDE );
      String clino   = exif.getAttribute( ExifInterface.TAG_GPS_LATITUDE );
      String bref = exif.getAttribute( ExifInterface.TAG_GPS_LONGITUDE_REF );
      String cref = exif.getAttribute( ExifInterface.TAG_GPS_LATITUDE_REF );
      int bsign = 1;
      int csign = 1;
      if ( bref.startsWith("W") || bref.startsWith("-") || bref.startsWith("West") ) bsign = -1; 
      if ( cref.startsWith("S") || cref.startsWith("-") || cref.startsWith("South") ) csign = -1; 

      mDate = exif.getAttribute( ExifInterface.TAG_DATETIME );
      // TDLog.v( "TD image bearing " + azimuth + " clino " + clino + " date " + mDate );
      if ( mDate == null ) mDate = "";
      if ( azimuth == null || clino == null ) { // FIXME-GPS_LATITUDE work-around for tag GPSLatitude not working
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
          String u = exif.getAttribute( ExifInterface.TAG_IMAGE_DESCRIPTION );
          // TDLog.v( "Photo desc " + u );
          if ( u != null ) {
            String[] vals = u.split(" ");
            if (vals.length > 1) {
              if (azimuth == null) azimuth = vals[0] + "/100";
              if (clino == null) clino = vals[1] + "/100";
            }
          }
        }
      }
      if ( azimuth != null && clino != null ) {
        // TDLog.v( "EXIF azimuth " + azimuth + " " + bref + " clino " + clino + " " + cref );
        int k = azimuth.indexOf('/');
	if ( k >= 0 ) {
          try { mAzimuth = bsign * Integer.parseInt( azimuth.substring(0,k) ) / 100.0f; } catch ( NumberFormatException e ) { }
	}
        k = clino.indexOf('/');
	if ( k >= 0 ) {
          try { mClino = csign * Integer.parseInt( clino.substring(0,k) ) / 100.0f; } catch ( NumberFormatException e ) { }
	}
        // TDLog.v( "Long <" + mAzimuth + "> Lat <" + mClino + "> " );
      }
    } catch ( IOException e ) {
      TDLog.Error("EXIF failed exif interface " + filename );
    }
  }

  /** @return true if the image is portrait - exif orientations:
   * 6 = upward
   * 8 = downward
   * 1 = leftward
   * 3 = rightward
   */
  public boolean isPortrait() { return mOrientation == 6 || mOrientation == 8; }


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
  static public int getCameraOrientation( int orientation )
  {
    if ( orientation <  45 ) return ORIENTATION_UP;
    if ( orientation < 135 ) return ORIENTATION_RIGHT;
    if ( orientation < 225 ) return ORIENTATION_DOWN;
    if ( orientation < 315 ) return ORIENTATION_LEFT;
    return ORIENTATION_UP;
  }

}
