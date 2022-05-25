/** @file FractalResult.java
 *
 *e @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D model renderer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.TDX;

// import com.topodroid.TDX.R;

// import java.util.List;

import android.os.AsyncTask;
// import android.content.Context;

import android.graphics.Bitmap;

import java.util.Locale;

class FractalResult
{
  DialogFractal   mDialog;
  FractalComputer mComputer = null;

  /** @return the array of computed dimensions
   */
  double[] mCount = null;

  /** cstr
   * @param dialog   dialog
   */
  FractalResult( DialogFractal dialog )
  {
    mDialog = dialog;
    mCount = new double[ FractalComputer.SIZE ];
  }

  /** display the result on the dialog
   */
  void showResults()
  {
    mDialog.showResult( this );
  }

  /** compute the dimensions
   * @param r        3D model data
   * @param do_splays whether to use splays
   * @param cell     initial cell size
   * @param mode     computation mode
   * @return false on failure
   */
  boolean compute( final TglParser r, boolean do_splays, double cell, int mode )
  {
    mComputer = new FractalComputer( this, r.emin, r.emax, r.nmin, r.nmax, r.zmin, r.zmax, do_splays, cell, mode );
    (new AsyncTask<Void, Void, Void>() {
	public Void doInBackground( Void ... v ) {
          mComputer.computeFractalCounts( r.getShots(), r.getSplays() );
	  return null;
	}

        public void onPostExecute( Void v )
        {
          TDToast.make( R.string.done_fractals );
          showResults();
        }
    }).execute();
    return true;
  }

  // /** release the fractal computer
  //  */
  // void releaseComputer()
  // {
  //   mComputer = null;
  //   // TDToast.make( R.string.done_fractal );
  // }

  /** set a dimension
   * @param k   index in the result array
   * @param val result value
   */
  void setCount(int k, double val) { mCount[k] = val; }

  String countsString()
  {
    StringBuilder counts = new StringBuilder();
    // counts.append( (int)(mCount[0]*100) );
    counts.append( String.format(Locale.US, " %.1f", mCount[0]) );
    for ( int s=1; s<FractalComputer.SIZE; ++s ) {
      // counts.append(" ");
      // counts.append( (int)(mCount[s]*100) );
      counts.append( String.format(Locale.US, " %.1f", mCount[s]) );
    }
    return counts.toString();
  }

  /** create the image of the resulting graph
   * @return the graph bitmap
   */
  Bitmap makeImage()
  {
    int ww = 100 * FractalComputer.DIM_ONE + 100;
    int hh = 500;
    int zz = 6;
    int zero = 100;
    int ux =  50; // unit
    int uy = 100; // unit
    Bitmap bitmap = Bitmap.createBitmap( ww, hh, Bitmap.Config.ARGB_8888 );
    for ( int j=0; j<hh; ++j ) for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, j, 0 );
    // for ( int j=uy; j<hh; j+=uy ) for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, j, 0xff999999 );
    for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, hh-zero, 0xffffffff );
    for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, hh-1*uy-zero, 0xffcccccc );
    for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, hh-2*uy-zero, 0xff999999 );
    for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, hh-3*uy-zero, 0xff666666 );
    if ( mComputer != null ) {
      for ( int k=0; k<FractalComputer.SIZE; ++k ) {
        int j = hh - zero - (int)(mCount[k]*uy);
        int i = ux + ux*k;
        if ( j > zz && j < hh-zz-1  ) {
          for ( int jj=j-zz; jj<=j+zz; ++jj) for ( int ii=i-zz; ii<=i+zz; ++ii ) bitmap.setPixel( ii, jj, 0xffff0000 );
        }
      }
    }
    return bitmap;
  }

}
