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

import java.io.IOException;

import android.widget.ImageView;
// import android.view.View;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

// import android.util.Log;

class TDImage
{
  private String mFilename;
  private float  mAzimuth = 0;
  private float  mClino   = 0;
  private int mOrientation = 0;
  private String mDate = "";

  private Bitmap mImage;
  private int mImageWidth  = 0;
  private int mImageHeight = 0;

  TDImage( String filename )
  {
    mFilename = filename;
    readExif();
    decodeImage();
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
    int scale = 1;
    while ( bfo.outWidth/scale/2 > required_size || bfo.outHeight/scale/2 > required_size ) {
      scale *= 2;
    }
    bfo.inJustDecodeBounds = false;
    bfo.inSampleSize = scale;
    mImage = BitmapFactory.decodeFile( mFilename, bfo );
    if ( mImage != null ) {
      mImageWidth   = mImage.getWidth();
      mImageHeight  = mImage.getHeight();
    }  
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
      if ( b != null && c != null ) {
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

  boolean fillImageView( ImageView view, int width )
  {
    if ( mImage == null ) return false;
    int ww = width;
    int hh = (int)( mImageHeight * ww / mImageWidth );
    Bitmap image2 = Bitmap.createScaledBitmap( mImage, ww, hh, true );
    if ( image2 == null ) return false;
    MyBearingAndClino.applyOrientation( view, image2, mOrientation );
    image2.recycle();
    return true;
  }

  boolean fillImageView( ImageView view ) { return fillImageView( view, (int)(TopoDroidApp.mDisplayWidth) ); }
}
