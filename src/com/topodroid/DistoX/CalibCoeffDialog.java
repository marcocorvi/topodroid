/* @file CalibCoeffDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration coefficients display dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Locale;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnClickListener;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import android.util.Log;

public class CalibCoeffDialog extends MyDialog
                              implements View.OnClickListener
{
  private TopoDroidApp mApp;

  private static final int WIDTH  = 200;
  private static final int HEIGHT = 100;
  private int mHist[] = null; // histogram: 20 bin of size 0.1 from 0.0 to 2.0
  private ImageView mImage; // error histogram
  private Bitmap mBitmap = null;

  private TextView mTextBG;
  private TextView mTextAGx;
  private TextView mTextAGy;
  private TextView mTextAGz;
  private TextView mTextBM;
  private TextView mTextAMx;
  private TextView mTextAMy;
  private TextView mTextAMz;
  private TextView mTextNL;
  private TextView mTextDelta;
  private TextView mTextDelta2;
  private TextView mTextMaxError;
  private TextView mTextIter;

  private Button   mButtonWrite;
  // private Button   mButtonBack;

  private String bg0;
  private String agx;
  private String agy;
  private String agz;
  private String bm0;
  private String amx;
  private String amy;
  private String amz;
  private String nlx;
  private String delta0;
  private String delta02;
  private String error0;
  private String iter0;
  private byte[] mCoeff;

  public CalibCoeffDialog( Context context, TopoDroidApp app,
                           Vector bg, Matrix ag, Vector bm, Matrix am, Vector nl, float[] errors,
                           float delta, float delta2, float error, long iter, byte[] coeff )
  {
    super( context, R.string.CalibCoeffDialog );
    mApp     = app;
    mCoeff   = coeff;

    bg0 = String.format(Locale.ENGLISH, "bG   %8.4f %8.4f %8.4f", bg.x, bg.y, bg.z );
    agx = String.format(Locale.ENGLISH, "aGx  %8.4f %8.4f %8.4f", ag.x.x, ag.x.y, ag.x.z );
    agy = String.format(Locale.ENGLISH, "aGy  %8.4f %8.4f %8.4f", ag.y.x, ag.y.y, ag.y.z );
    agz = String.format(Locale.ENGLISH, "aGz  %8.4f %8.4f %8.4f", ag.z.x, ag.z.y, ag.z.z );

    bm0 = String.format(Locale.ENGLISH, "bM   %8.4f %8.4f %8.4f", bm.x, bm.y, bm.z );
    amx = String.format(Locale.ENGLISH, "aMx  %8.4f %8.4f %8.4f", am.x.x, am.x.y, am.x.z );
    amy = String.format(Locale.ENGLISH, "aMy  %8.4f %8.4f %8.4f", am.y.x, am.y.y, am.y.z );
    amz = String.format(Locale.ENGLISH, "aMz  %8.4f %8.4f %8.4f", am.z.x, am.z.y, am.z.z );

    if ( nl != null ) {
      nlx = String.format(Locale.ENGLISH, "nL   %8.4f %8.4f %8.4f", nl.x, nl.y, nl.z );
    } else {
      nlx = new String("");
    }

    delta0  = String.format( mContext.getResources().getString( R.string.calib_error ), delta );
    delta02 = String.format( mContext.getResources().getString( R.string.calib_stddev ), delta2 );
    error0  = String.format( mContext.getResources().getString( R.string.calib_max_error ), error );
    iter0   = String.format( mContext.getResources().getString( R.string.calib_iter ), iter );

    if ( errors != null ) {
      fillImage( errors );
    }
  }

  void fillImage( float[] error )
  {
    mBitmap = Bitmap.createBitmap( WIDTH+20, HEIGHT+20, Bitmap.Config.ARGB_8888 );
    int ww = mBitmap.getWidth();
    int hh = mBitmap.getHeight();
    for ( int j=0; j<hh; ++j ) {
      for ( int i=0; i<ww; ++i ) mBitmap.setPixel( i, j, 0 );
    }
    mHist = new int[20];
    for ( int k=0; k<20; ++k ) mHist[k] = 0;
    if ( error != null ) {
      for ( int k=0; k < error.length; ++ k ) {
        int i = (int)( error[k]*10*TDMath.RAD2GRAD );
        if ( i < 20 && i >= 0 ) ++ mHist[i];
      }
    }

    // each unit height is 5 pixel
    int red = 0xffffffff;
    int top = red;
    int col = 0xff6699ff;
    int joff = hh-10;
    int ioff = 10;
    int dx   = (int)( ww / 20.0 ); 
    if ( dx*20 >= ww ) dx --;
    int x, y;
    for ( int k=0; k<20; ++ k ) {
      int h = 5 * mHist[k];
      if ( h > joff ) {
        h = joff;
        top = col;
      } else {
        top = red;
      }
      x  = ioff + dx * k;
      for ( y=joff-h; y <= joff; ++y ) mBitmap.setPixel( x, y, red );
      int x2 = x  + dx-1;
      for ( ++x; x < x2; ++ x ) {
        y = joff-h;
        mBitmap.setPixel( x, y, red );
        for ( ++y; y < joff; ++y ) mBitmap.setPixel( x, y, col );
        mBitmap.setPixel( x, y, top );
      }
      for ( y=joff-h; y <= joff; ++y ) mBitmap.setPixel( x, y, red );
    }
    for ( y = 0; y < hh; ++y ) mBitmap.setPixel( ioff, y, red );
    for ( x = 0; x < ww; ++x ) mBitmap.setPixel( x, joff, red );
    x  = ioff + dx * 5;
    for ( y = joff; y < hh - 5; ++y ) mBitmap.setPixel( x, y, red );
    x  = ioff + dx * 10;
    for ( y = joff; y < hh - 0; ++y ) mBitmap.setPixel( x, y, red );
    x  = ioff + dx * 15;
    for ( y = joff; y < hh - 5; ++y ) mBitmap.setPixel( x, y, red );
    y = joff - 50;
    for ( x = 5; x < ioff; ++x ) mBitmap.setPixel( x, y, red );
    y = joff - 100;
    for ( x = 5; x < ioff; ++x ) mBitmap.setPixel( x, y, red );
      
    // Log.v("DistoX", "fill image done");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.calib_coeff_dialog, R.string.title_coeff );

    mTextBG  = (TextView) findViewById(R.id.coeff_bg);
    mTextAGx = (TextView) findViewById(R.id.coeff_agx);
    mTextAGy = (TextView) findViewById(R.id.coeff_agy);
    mTextAGz = (TextView) findViewById(R.id.coeff_agz);
    
    mTextBM  = (TextView) findViewById(R.id.coeff_bm);
    mTextAMx = (TextView) findViewById(R.id.coeff_amx);
    mTextAMy = (TextView) findViewById(R.id.coeff_amy);
    mTextAMz = (TextView) findViewById(R.id.coeff_amz);

    mTextNL = (TextView) findViewById(R.id.coeff_nl);

    mImage        = (ImageView) findViewById( R.id.histogram );
    mTextDelta    = (TextView) findViewById(R.id.coeff_delta);
    mTextDelta2   = (TextView) findViewById(R.id.coeff_delta2);
    mTextMaxError = (TextView) findViewById(R.id.coeff_max_error);
    mTextIter     = (TextView) findViewById(R.id.coeff_iter);
    mButtonWrite  = (Button) findViewById( R.id.button_coeff_write );

    mTextBG.setText( bg0 );
    mTextAGx.setText( agx );
    mTextAGy.setText( agy );
    mTextAGz.setText( agz );
    mTextBM.setText( bm0 );
    mTextAMx.setText( amx );
    mTextAMy.setText( amy );
    mTextNL.setText( nlx );
    mTextAMz.setText( amz );
    if ( mBitmap != null ) {
      mImage.setImageBitmap( mBitmap );
      mTextDelta.setText( delta0 );
      mTextDelta2.setText( delta02 );
      mTextMaxError.setText( error0 );
      mTextIter.setText( iter0 );
      mButtonWrite.setOnClickListener( this );
      mButtonWrite.setEnabled( mCoeff != null );
      // mButtonBack  = (Button) findViewById( R.id.button_coeff_back );
      // mButtonBack.setOnClickListener( this );
    } else {
      mImage.setVisibility( View.GONE );
      mTextDelta.setVisibility( View.GONE );
      mTextDelta2.setVisibility( View.GONE );
      mTextMaxError.setVisibility( View.GONE );
      mTextIter.setVisibility( View.GONE );
      mButtonWrite.setVisibility( View.GONE );
    }

  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button)v;
    if ( b == mButtonWrite ) {
      mApp.uploadCalibCoeff( mContext, mCoeff );
    } else {
      dismiss();
    }
  }

}

