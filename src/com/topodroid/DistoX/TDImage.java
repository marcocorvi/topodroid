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
package com.topodroid.DistoX;

// import android.util.Log;

import java.io.IOException;

import android.widget.ImageView;
// import android.view.View;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface; // REQUIRES android.support

class TDImage
{
  private String mFilename;
  private float  mAzimuth = 0;
  private float  mClino   = 0;
  private int mOrientation = 0;
  private String mDate = "";

  private Bitmap mImage    = null;
  private Bitmap mImage2   = null;
  private int mImageWidth  = 0;
  private int mImageHeight = 0;

  TDImage( String filename )
  {
    mFilename = filename;
    readExif();
    decodeImage();
    // Log.v("DistoX", "TD image " + filename );
  }

  float azimuth() { return mAzimuth; }
  float clino()   { return mClino; }
  String date()   { return mDate; }
  int width()     { return mImageWidth; }
  int height()    { return mImageHeight; }

  private void decodeImage()
  {
    BitmapFactory.Options bfo = new BitmapFactory.Options();
    bfo.inJustDecodeBounds = true;
    BitmapFactory.decodeFile( mFilename, bfo );
    int required_size = TDSetting.mThumbSize;
    // Log.v("DistoX", "photo: file " + mFilename + " " + bfo.outWidth + "x" + bfo.outHeight + " req. size " + required_size );
    int scale = 1;
    while ( bfo.outWidth/scale > required_size || bfo.outHeight/scale > required_size ) {
      scale *= 2;
    }
    bfo.inJustDecodeBounds = false;
    bfo.inSampleSize = scale;
    mImage = BitmapFactory.decodeFile( mFilename, bfo );
    if ( mImage != null ) {
      mImageWidth   = mImage.getWidth();
      mImageHeight  = mImage.getHeight();
    }  
    // Log.v("DistoX", "photo: file " + mFilename + " image " + mImageWidth + "x" + mImageHeight + " req. size " + required_size + " scale " + scale );
  }

  private void readExif()
  {
    try {
      ExifInterface exif = new ExifInterface( mFilename );
      // mAzimuth = exif.getAttribute( "GPSImgDirection" );
      mOrientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 0 );
      // Log.v("DistoX", "Photo edit orientation " + mOrientation );
      String b = exif.getAttribute( ExifInterface.TAG_GPS_LONGITUDE );
      String c = exif.getAttribute( ExifInterface.TAG_GPS_LATITUDE );
      mDate = exif.getAttribute( ExifInterface.TAG_DATETIME );
      if ( mDate == null ) mDate = "";
      if ( b == null || c == null ) { // FIXME-GPS_LATITUDE work-around for tag GPSLatitude not working
        String u = exif.getAttribute( "UserComment" );
        // Log.v("DistoXPHOTO", "u " + u );
        if ( u != null ) {
          String[] vals = u.split(" ");
          if ( vals.length > 1 ) {
            if ( b == null ) b = vals[0] + "/100";
            if ( c == null ) c = vals[1] + "/100";
          }
        }
      }
      if ( b != null && c != null ) {
        // Log.v("DistoXPHOTO", "b " + b + " c " + c );
        int k = b.indexOf('/');
	if ( k >= 0 ) {
          try { mAzimuth = Integer.parseInt( b.substring(0,k) ) / 100.0f; } catch ( NumberFormatException e ) { }
	}
        k = c.indexOf('/');
	if ( k >= 0 ) {
          try { mClino = Integer.parseInt( c.substring(0,k) ) / 100.0f; } catch ( NumberFormatException e ) { }
	}
        // Log.v("DistoX", "Long <" + bearing + "> Lat <" + clino + ">" );
      }
    } catch ( IOException e ) {
      TDLog.Error("failed exif interface " + mFilename );
    }
  }

  // 6 = upward
  // 8 = downward
  // 1 = leftward
  // 3 = rightward
  boolean isPortrait() { return mOrientation == 6 || mOrientation == 8; }

  // @param view
  // @param ww     width
  // @param hh     height
  // @param orient whether to apply image orientation
  boolean fillImageView( ImageView view, int ww, int hh, boolean orient )
  {
    if ( mImage == null ) return false;

    hh = (int)( mImageHeight * ww / mImageWidth );

    // if ( isPortrait() ) {
    //   hh = (int)( mImageHeight * ww / mImageWidth );
    // } else {
    //   ww = (int)( mImageWidth * hh / mImageHeight );
    // }
      
    // Log.v("DistoX", "fill image view w " + ww + " h " + hh + " orientation " + mOrientation );
    if ( ww <= 0 || hh <= 0 ) return false;
    if ( mImage2 != null ) mImage2.recycle();
    mImage2 = Bitmap.createScaledBitmap( mImage, ww, hh, true );
    if ( mImage2 == null ) return false;

    // MyBearingAndClino.applyOrientation( view, mImage2, (orient? mOrientation : 6) );
    MyBearingAndClino.applyOrientation( view, mImage2, mOrientation );
    return true;
  }

  // full image view: fixed orientation
  // boolean fillImageView( ImageView view ) 
  // { 
  //   return fillImageView( view, (int)(TopoDroidApp.mDisplayWidth), (int)(TopoDroidApp.mDisplayHeight), false );
  // }

  void recycleImages()
  {
    if ( mImage != null ) mImage.recycle();
    if ( mImage2 != null ) mImage2.recycle();
  }
}
