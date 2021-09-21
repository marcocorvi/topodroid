/** @file FractalResult.java
 *
 *e @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D model renderer
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.Cave3X;

// import com.topodroid.Cave3X.R;

// import java.util.List;

import android.os.AsyncTask;
import android.content.Context;

import android.graphics.Bitmap;

class FractalResult
{
  static FractalComputer computer = null;
  static TopoGL  mApp = null;
  static Context mContext;

  static double[] mCount = new double[ FractalComputer.SIZE ];

  static int compute( Context context, TopoGL app, final TglParser r, boolean do_splays, int cell, int mode )
  {
    if ( computer != null ) return -1;
    mContext = context;
    mApp  = app;
    computer = new FractalComputer( r.emin, r.emax, r.nmin, r.nmax, r.zmin, r.zmax, do_splays, cell, mode );
    (new AsyncTask<Void, Void, Void>() {
	public Void doInBackground( Void ... v ) {
          computer.computeFractalCounts( r.getShots(), r.getSplays() );
	  return null;
	}

        public void onPostExecute( Void v )
        {
          if ( mApp != null ) mApp.toast( R.string.done_fractals, false );
        }
    }).execute();
    return 0;
  }

  static void releaseComputer()
  {
    computer = null;
    if ( mApp != null ) mApp.uiToast( "Fractal compute finished", false );
  }

  static void setCount(int k, double val) { mCount[k] = val; }

  static String countsString()
  {
    StringBuilder counts = new StringBuilder();
    counts.append( Integer.toString( (int)(mCount[0]*100) ) );
    for ( int s=1; s<FractalComputer.SIZE; ++s ) {
      counts.append(" ");
      counts.append( Integer.toString( (int)(mCount[s]*100) ) );
    }
    return counts.toString();
  }

  static Bitmap makeImage()
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
    for ( int k=0; k<FractalComputer.SIZE; ++k ) {
      int j = hh - zero - (int)(mCount[k]*uy);
      int i = ux + ux*k;
      if ( j > zz && j < hh-zz-1  ) {
        for ( int jj=j-zz; jj<=j+zz; ++jj) for ( int ii=i-zz; ii<=i+zz; ++ii ) bitmap.setPixel( ii, jj, 0xffff0000 );
      }
    }
    return bitmap;
  }

}
