/* @file CalibCoeffDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX calibration coefficients display dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Locale;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
// import android.view.View.OnClickListener;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.Config;

// import android.util.Log;

class CalibCoeffDialog extends MyDialog
                       implements View.OnClickListener
{
  private GMActivity mParent;

  private static final int WIDTH  = 200;
  private static final int HEIGHT = 100;
  // private ImageView mImage; // error histogram
  private Bitmap mBitmap = null;

  // private TextView mTextBG;
  // private TextView mTextAGx;
  // private TextView mTextAGy;
  // private TextView mTextAGz;
  // private TextView mTextBM;
  // private TextView mTextAMx;
  // private TextView mTextAMy;
  // private TextView mTextAMz;
  // private TextView mTextNL;
  // private TextView mTextDelta;
  // private TextView mTextDelta2;
  // private TextView mTextMaxError;
  // private TextView mTextIter;

  private Button mButtonWrite;
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
  private float mDelta;
  // private boolean mSaturated;

  CalibCoeffDialog( Context context, GMActivity parent,
                    Vector bg, Matrix ag, Vector bm, Matrix am, Vector nl, float[] errors,
                    float delta, float delta2, float error, long iter, byte[] coeff /*, boolean saturated */ )
  {
    super( context, R.string.CalibCoeffDialog );
    mParent = parent;
    mCoeff = coeff;

    bg0 = String.format(Locale.US, "bG   %8.4f %8.4f %8.4f", bg.x, bg.y, bg.z );
    agx = String.format(Locale.US, "aGx  %8.4f %8.4f %8.4f", ag.x.x, ag.x.y, ag.x.z );
    agy = String.format(Locale.US, "aGy  %8.4f %8.4f %8.4f", ag.y.x, ag.y.y, ag.y.z );
    agz = String.format(Locale.US, "aGz  %8.4f %8.4f %8.4f", ag.z.x, ag.z.y, ag.z.z );

    bm0 = String.format(Locale.US, "bM   %8.4f %8.4f %8.4f", bm.x, bm.y, bm.z );
    amx = String.format(Locale.US, "aMx  %8.4f %8.4f %8.4f", am.x.x, am.x.y, am.x.z );
    amy = String.format(Locale.US, "aMy  %8.4f %8.4f %8.4f", am.y.x, am.y.y, am.y.z );
    amz = String.format(Locale.US, "aMz  %8.4f %8.4f %8.4f", am.z.x, am.z.y, am.z.z );

    if ( nl != null ) {
      nlx = String.format(Locale.US, "nL   %8.4f %8.4f %8.4f", nl.x, nl.y, nl.z );
    } else {
      nlx = TDString.EMPTY; // new String(TDString.EMPTY);
    }

    mDelta = delta;
    delta0  = String.format( mContext.getResources().getString( R.string.calib_error ), delta );
    delta02 = String.format( mContext.getResources().getString( R.string.calib_stddev ), delta2 );
    error0  = String.format( mContext.getResources().getString( R.string.calib_max_error ), error );
    iter0   = String.format( mContext.getResources().getString( R.string.calib_iter ), iter );

    if ( errors != null ) {
      mBitmap = makeHistogramBitmap( errors, WIDTH, HEIGHT, 20, 5, TDColor.BLUE );
    }
    // mSaturated = saturated;
  }

  static Bitmap makeHistogramBitmap( float[] error, int width, int height, int bin, int step, int col )
  {
    Bitmap bitmap = Bitmap.createBitmap( width+20, height+20, Bitmap.Config.ARGB_8888 );
    int ww = bitmap.getWidth();
    int hh = bitmap.getHeight();
    for ( int j=0; j<hh; ++j ) {
      for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, j, 0 );
    }
    int[] hist = new int[bin];
    for ( int k=0; k<bin; ++k ) hist[k] = 0;
    if ( error != null ) {
      for ( int k=0; k < error.length; ++ k ) {
        int i = (int)( error[k]*10*TDMath.RAD2DEG );
        if ( i < bin && i >= 0 ) ++ hist[i];
      }
    }

    int red = 0xffffffff;
    int top = red;
    int joff = hh-10;
    int ioff = 10;
    int dx   = (int)( ww / bin ); 
    if ( dx*20 >= ww ) dx --;
    int x, y;
    for ( int k=0; k<bin; ++ k ) {
      int h = step * hist[k];
      if ( h > joff ) {
        h = joff;
        top = col;
      } else {
        top = red;
      }
      x  = ioff + dx * k;
      for ( y=joff-h; y <= joff; ++y ) bitmap.setPixel( x, y, red );
      int x2 = x  + dx-1;
      for ( ++x; x < x2; ++ x ) {
        y = joff-h;
        bitmap.setPixel( x, y, red );
        for ( ++y; y < joff; ++y ) bitmap.setPixel( x, y, col );
        bitmap.setPixel( x, y, top );
      }
      for ( y=joff-h; y <= joff; ++y ) bitmap.setPixel( x, y, red );
    }
    for ( y = 0; y < hh; ++y ) bitmap.setPixel( ioff, y, red );
    for ( x = 0; x < ww; ++x ) bitmap.setPixel( x, joff, red );
    for ( int k = 5; k <= bin; k+=5 ) {
      x  = ioff + dx * k;
      int yy = hh - ( ((k%10) == 0 )? 0 : 5 );
      for ( y = joff; y < yy; ++y ) bitmap.setPixel( x, y, red );
    }
    if ( 5  <= bin ) {
      x  = ioff + dx * 5;
      for ( y = 0; y < joff; ++y ) bitmap.setPixel( x, y, TDColor.FIXED_YELLOW );
    }
    if ( 10  <= bin ) {
      x  = ioff + dx * 10;
      for ( y = 0; y < joff; ++y ) bitmap.setPixel( x, y, TDColor.FULL_RED );
    }
    for ( int k = 10; ; k += 10 ) {
      y = joff - step * k;
      if ( y < 0 ) break;
      for ( x = 5; x < ioff; ++x ) bitmap.setPixel( x, y, red );
    }
      
    // Log.v("DistoX", "fill image done");
    return bitmap;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.calib_coeff_dialog, R.string.title_coeff );

    TextView textBG  = (TextView) findViewById(R.id.coeff_bg);
    TextView textAGx = (TextView) findViewById(R.id.coeff_agx);
    TextView textAGy = (TextView) findViewById(R.id.coeff_agy);
    TextView textAGz = (TextView) findViewById(R.id.coeff_agz);

    TextView textBM  = (TextView) findViewById(R.id.coeff_bm);
    TextView textAMx = (TextView) findViewById(R.id.coeff_amx);
    TextView textAMy = (TextView) findViewById(R.id.coeff_amy);
    TextView textAMz = (TextView) findViewById(R.id.coeff_amz);

    TextView textNL = (TextView) findViewById(R.id.coeff_nl);

    ImageView image        = (ImageView) findViewById( R.id.histogram );
    TextView textDelta    = (TextView) findViewById(R.id.coeff_delta);
    TextView textDelta2   = (TextView) findViewById(R.id.coeff_delta2);
    TextView textMaxError = (TextView) findViewById(R.id.coeff_max_error);
    TextView textIter     = (TextView) findViewById(R.id.coeff_iter);
    mButtonWrite  = (Button) findViewById( R.id.button_coeff_write );

    textBG.setText( bg0 );
    textAGx.setText( agx );
    textAGy.setText( agy );
    textAGz.setText( agz );
    textBM.setText( bm0 );
    textAMx.setText( amx );
    textAMy.setText( amy );
    textNL.setText( nlx );
    textAMz.setText( amz );
    if ( mBitmap != null ) {
      image.setImageBitmap( mBitmap );
      textDelta.setText( delta0 );
      textDelta2.setText( delta02 );
      textMaxError.setText( error0 );
      textIter.setText( iter0 );
      mButtonWrite.setOnClickListener( this );
      // if ( mSaturated ) {
      //   mButtonWrite.setEnabled( false );
      // } else {
        mButtonWrite.setEnabled( mCoeff != null );
      // }
      // mButtonBack  = (Button) findViewById( R.id.button_coeff_back );
      // mButtonBack.setOnClickListener( this );
    } else {
      image.setVisibility( View.GONE );
      textDelta.setVisibility( View.GONE );
      textDelta2.setVisibility( View.GONE );
      textMaxError.setVisibility( View.GONE );
      textIter.setVisibility( View.GONE );
      mButtonWrite.setVisibility( View.GONE );
    }
  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button)v;
    if ( b == mButtonWrite ) {
      if ( mParent != null ) mParent.uploadCoefficients( mDelta, mCoeff, true, b );
    } else {
      dismiss();
    }
  }

}

