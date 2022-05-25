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
import com.topodroid.prefs.TDSetting;

// import java.io.IOException;

// import android.os.Build;
import android.widget.ImageView;

import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
// import android.media.ExifInterface; // REQUIRES android.support

public class TDImage
{
  private String mFilename;
  private ExifInfo mExif; 

  private Bitmap mImage    = null;
  private Bitmap mImage2   = null;
  private int mImageWidth  = 0;
  private int mImageHeight = 0;

  /** cstr
   * @param filename   file path
   */
  public TDImage( String filename )
  {
    mFilename = filename;
    mExif = new ExifInfo( filename );
    decodeImage();
    // TDLog.v( "TD image " + filename );
  }

  public float azimuth() { return mExif.azimuth(); }
  public float clino()   { return mExif.clino(); }
  public String date()   { return mExif.date(); }
  public int width()     { return mImageWidth; }
  public int height()    { return mImageHeight; }

  private void decodeImage()
  {
    BitmapFactory.Options bfo = new BitmapFactory.Options();
    bfo.inJustDecodeBounds = true;
    BitmapFactory.decodeFile( mFilename, bfo );
    int required_size = TDSetting.mThumbSize;
    // TDLog.v( "photo: file " + mFilename + " " + bfo.outWidth + "x" + bfo.outHeight + " req. size " + required_size );
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
    // TDLog.v( "photo: file " + mFilename + " image " + mImageWidth + "x" + mImageHeight + " req. size " + required_size + " scale " + scale );
  }

  /** @return true if the image is portrait - exif orientations:
   * 6 = upward
   * 8 = downward
   * 1 = leftward
   * 3 = rightward
   */
  public boolean isPortrait() { return mExif.isPortrait(); }

  /** fill the view with the image
   * @param view   view to fill
   * @param ww     width
   * @param hh     height
   * @param orient whether to apply image orientation
   * @return true on success
   */
  public boolean fillImageView( ImageView view, int ww, int hh, boolean orient )
  {
    if ( mImage == null ) return false;

    int h = mImageHeight * ww / mImageWidth;
    if ( h > hh ) {
      ww = mImageWidth * hh / mImageHeight;
    } else {
      hh = h;
    }

    // if ( isPortrait() ) {
    //   hh = (int)( mImageHeight * ww / mImageWidth );
    // } else {
    //   ww = (int)( mImageWidth * hh / mImageHeight );
    // }
      
    // TDLog.v( "fill image view w " + ww + " h " + hh + " orientation " + mExif.orientation() );
    if ( ww <= 0 || hh <= 0 ) return false;
    if ( mImage2 != null ) mImage2.recycle();
    mImage2 = Bitmap.createScaledBitmap( mImage, ww, hh, true );
    if ( mImage2 == null ) return false;

    // MyBearingAndClino.applyOrientation( view, mImage2, (orient? mExif.orientation() : 6) );
    applyOrientation( view, mImage2, mExif.orientation() );
    return true;
  }

  // full image view: fixed orientation
  // boolean fillImageView( ImageView view ) 
  // { 
  //   return fillImageView( view, (int)(TopoDroidApp.mDisplayWidth), (int)(TopoDroidApp.mDisplayHeight), false );
  // }

  /** recycle image and thumbnail
   */
  public void recycleImages()
  {
    if ( mImage != null ) mImage.recycle();
    if ( mImage2 != null ) mImage2.recycle();
  }
  
  /** apply the image bitmap to the image view using an orientation
   * @param image       image view
   * @param bitmap      image bitmap
   * @param orientation orientation
   * EXIF orientation is detailed in MyBearingAndClino
   *                           up
   * 1: no rotation            6
   * 6: rotate right    left 1-+-3 right
   * 3: rotate 180             8
   * 8: rotate left            down
   */
  private static void applyOrientation( ImageView image, Bitmap bitmap, int orientation )
  {
    Matrix m = new Matrix();
    int w = bitmap.getWidth();
    int h = bitmap.getHeight();
    if ( orientation == 6 ) {
      m.setRotate(  90f, (float)w/2, (float)h/2 );
      // image.setRotation(  90 ); // REQUIRES API-14
    } else if ( orientation == 3 ) {
      m.setRotate( 180f, (float)w/2, (float)h/2 );
      // image.setRotation( 180 );
    } else if ( orientation == 8 ) {
      m.setRotate( 270f, (float)w/2, (float)h/2 );
      // image.setRotation( 270 );
    } else {
      image.setImageBitmap( bitmap );
      return;
    }
    image.setImageBitmap( Bitmap.createBitmap( bitmap, 0, 0, w, h, m, true ) );
  }
}
