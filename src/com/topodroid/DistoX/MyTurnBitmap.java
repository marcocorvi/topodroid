/* @file MyTurnBitmap.java
 *
 * @author marco corvi
 * @date mar 2018
 *
 * @brief TopoDroid turnable bitmap
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * credits:
 *   the idea for the rotation routine is adapted from 
 *   David Eberly, Integer-based rotations of images, Geometric Tools, LLC, created Feb. 9, 2006 - last modified Mar. 2, 2008
 *   http://www.geometrictools.col
 */
package com.topodroid.DistoX;

// import android.content.Context;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
// import android.graphics.drawable.BitmapDrawable;

// import android.util.Log;

class MyTurnBitmap 
{
  private int mBGcolor;

  private static final int[] cos128 = {
 128, 127, 127, 127, 127, 127, 126, 126, 125, 124, 124, 123, 122, 121, 120, 119, 118, 117, 115, 114, 112, 111, 109, 108, 106, 104, 102, 100, 98, 96, 94, 92, 90, 88, 85, 83, 81, 78, 76, 73, 71, 68, 65, 63, 60, 57, 54, 51, 48, 46, 43, 40, 37, 34, 31, 28, 24, 21, 18, 15, 12, 9, 6, 3, 0, -3, -6, -9, -12, -15, -18, -21, -24, -28, -31, -34, -37, -40, -43, -46, -48, -51, -54, -57, -60, -63, -65, -68, -71, -73, -76, -78, -81, -83, -85, -88, -90, -92, -94, -96, -98, -100, -102, -104, -106, -108, -109, -111, -112, -114, -115, -117, -118, -119, -120, -121, -122, -123, -124, -124, -125, -126, -126, -127, -127, -127, -127, -127  };
  private static final int[] sin128 = {
 0, 3, 6, 9, 12, 15, 18, 21, 24, 28, 31, 34, 37, 40, 43, 46, 48, 51, 54, 57, 60, 63, 65, 68, 71, 73, 76, 78, 81, 83, 85, 88, 90, 92, 94, 96, 98, 100, 102, 104, 106, 108, 109, 111, 112, 114, 115, 117, 118, 119, 120, 121, 122, 123, 124, 124, 125, 126, 126, 127, 127, 127, 127, 127, 128, 127, 127, 127, 127, 127, 126, 126, 125, 124, 124, 123, 122, 121, 120, 119, 118, 117, 115, 114, 112, 111, 109, 108, 106, 104, 102, 100, 98, 96, 94, 92, 90, 88, 85, 83, 81, 78, 76, 73, 71, 68, 65, 63, 60, 57, 54, 51, 48, 46, 43, 40, 37, 34, 31, 28, 24, 21, 18, 15, 12, 9, 6, 3  };

  static private MyTurnBitmap mTurnBitmap = null;

  static MyTurnBitmap getTurnBitmap( Resources res )
  {
    if ( mTurnBitmap == null ) {
      Bitmap dial = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp ); // FIXME AZIMUTH_DIAL
      mTurnBitmap = new MyTurnBitmap( dial, TDColor.TRANSPARENT );
    }
    return mTurnBitmap;
  }

  private int[] mPxlSave = null;
  private int   mPxlSize = 0;
  private int[] mPxl;

  MyTurnBitmap( Bitmap dial, int color )
  {
    mBGcolor = color;

    if ( mPxlSave == null ) {
      mPxlSize = dial.getWidth();
      mPxlSave = new int[mPxlSize * mPxlSize];
      for ( int j=0; j < mPxlSize; ++j ) for ( int i=0; i < mPxlSize; ++i ) {
        mPxlSave[j*mPxlSize+i] = dial.getPixel(i, j);
      }
      mPxl = new int[mPxlSize * mPxlSize];
    }
    // Log.v("DistoX", "Pxl Size " + mPxlSize + " " + dial.getHeight() );
  }

  private void rotatedBitmap( float azimuth )
  {
  /*
    float n11 = (mPxlSize - 1.0f)/2;
    float n21 = (mPxlSize - 1.0f)/2;
    float c = TDMath.cosd( 90-azimuth );
    float s = TDMath.sind( 90-azimuth );
    float n11sn21 = n11 + s * n21 - c * n21;
    float n11cn21 = n11 - c * n21 - s * n21;
  */
    azimuth = 90 - azimuth;
    if ( azimuth < 0 ) { azimuth += 360; } else if ( azimuth > 360 ) { azimuth -= 360; }
    int azi = ((int)(azimuth * 256.0/360.0 )) % 256;
    int c, s;
    if ( azi >= 128 ) {
      c = - cos128[azi-128];
      s = - sin128[azi-128];
    } else {
      c = cos128[azi];
      s = sin128[azi];
    }
    int n11 = (mPxlSize - 1)*128;
    int n21 = (mPxlSize - 1);
    int n11sn21 = n11 + s * n21 - c * n21;
    int n11cn21 = n11 - c * n21 - s * n21;
    c *= 2;
    s *= 2;
  //
    for ( int j=0; j<mPxlSize; ++j ) {
    /*
      float js = n11sn21 - s * j;          // n11 - s * (j - n21);
      float jc = n11cn21 + c * j;          // n11 + c * (j - n21);
    */
      int js = n11sn21 - s * j;       
      int jc = n11cn21 + c * j;      
    //
      for ( int i=0; i<mPxlSize; ++i ) {
	// int ii = (int)( js + c * i );      // js + c * (i - n21);
	int ii = ( js + c * i ) >> 8;  
	if ( ii >= 0 && ii < mPxlSize ) {
	  // int jj = (int)( jc + s * i );    // jc + s * (i - n21);
	  int jj = ( jc + s * i ) >> 8;
	  if ( jj >= 0 && jj < mPxlSize ) {
            mPxl[j*mPxlSize+i] = mPxlSave[ ii + jj * mPxlSize ];
  	  } else {
            mPxl[j*mPxlSize+i] = mBGcolor;
       	  }
        }
      }
    }
  }

    // Bitmap bm1 = Bitmap.createBitmap( mPxl, mPxlSize, mPxlSize, Bitmap.Config.ALPHA_8 );
    // Bitmap bm2 = Bitmap.createScaledBitmap( bm1, w, w, true );
  // @param azimuth angle [deg]
  // @param w       button size [pxl]
  Bitmap getBitmap( float azimuth, int w )
  {
    // Log.v("DistoX", "get rotated bitmap Angle " + azimuth + " size " + w );
    rotatedBitmap( azimuth );
    Bitmap bm1 = Bitmap.createBitmap( mPxlSize, mPxlSize, Bitmap.Config.ARGB_8888 );
    for (int j=0; j<mPxlSize; ++j ) for ( int i=0; i<mPxlSize; ++i ) {
      bm1.setPixel( i, j, mPxl[j*mPxlSize+i] );
    }
    Bitmap bm2 = Bitmap.createScaledBitmap( bm1, w, w, true );
    bm1.recycle();
    return bm2;
  }

}

