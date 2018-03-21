/* @file MyTurnBitmap.java
 *
 * @author marco corvi
 * @date mar 2018
 *
 * @brief TopoDroid turnable bitmap
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.content.Context;

import android.graphics.Bitmap;
// import android.graphics.drawable.BitmapDrawable;

// import android.util.Log;

class MyTurnBitmap 
{
  private int mBGcolor;

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
    float n11 = (mPxlSize - 1.0f)/2;
    float n21 = (mPxlSize - 1.0f)/2;
    float c = TDMath.cosd( 90-azimuth );
    float s = TDMath.sind( 90-azimuth );
    for ( int j=0; j<mPxlSize; ++j ) {
      float js = n11 - s * (j-n21);
      float jc = n11 + c * (j-n21);
      for ( int i=0; i<mPxlSize; ++i ) {
	int ii = (int)( js + c * (i - n21) );
	int jj = (int)( jc + s * (i - n21) );
	if ( ii >= 0 && ii < mPxlSize && jj >= 0 && jj < mPxlSize ) {
          mPxl[j*mPxlSize+i] = mPxlSave[ ii + jj * mPxlSize ];
	} else {
          mPxl[j*mPxlSize+i] = mBGcolor;
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

