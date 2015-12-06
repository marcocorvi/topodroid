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
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.Locale;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

// import android.util.Log;

public class CalibCoeffDialog extends Dialog
                              implements View.OnClickListener
{
  private Context mContext;
  private TopoDroidApp mApp;

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
                           Vector bg, Matrix ag, Vector bm, Matrix am, Vector nl,
                           float delta, float delta2, float error, long iter, byte[] coeff )
  {
    super( context );
    mContext = context;
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
    error0 = String.format( mContext.getResources().getString( R.string.calib_max_error ), error );
    iter0 = String.format( mContext.getResources().getString( R.string.calib_iter ), iter );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setTitle( mContext.getResources().getString( R.string.title_coeff ) );

    setContentView(R.layout.calib_coeff_dialog);
    mTextBG  = (TextView) findViewById(R.id.coeff_bg);
    mTextAGx = (TextView) findViewById(R.id.coeff_agx);
    mTextAGy = (TextView) findViewById(R.id.coeff_agy);
    mTextAGz = (TextView) findViewById(R.id.coeff_agz);
    
    mTextBM  = (TextView) findViewById(R.id.coeff_bm);
    mTextAMx = (TextView) findViewById(R.id.coeff_amx);
    mTextAMy = (TextView) findViewById(R.id.coeff_amy);
    mTextAMz = (TextView) findViewById(R.id.coeff_amz);

    mTextNL = (TextView) findViewById(R.id.coeff_nl);

    mTextDelta    = (TextView) findViewById(R.id.coeff_delta);
    mTextDelta2   = (TextView) findViewById(R.id.coeff_delta2);
    mTextMaxError = (TextView) findViewById(R.id.coeff_max_error);
    mTextIter     = (TextView) findViewById(R.id.coeff_iter);

    mTextBG.setText( bg0 );
    mTextAGx.setText( agx );
    mTextAGy.setText( agy );
    mTextAGz.setText( agz );
    mTextBM.setText( bm0 );
    mTextAMx.setText( amx );
    mTextAMy.setText( amy );
    mTextNL.setText( nlx );
    mTextAMz.setText( amz );
    mTextDelta.setText( delta0 );
    mTextDelta2.setText( delta02 );
    mTextMaxError.setText( error0 );
    mTextIter.setText( iter0 );

    mButtonWrite = (Button) findViewById( R.id.button_coeff_write );
    mButtonWrite.setOnClickListener( this );
    mButtonWrite.setEnabled( mCoeff != null );
    // mButtonBack  = (Button) findViewById( R.id.button_coeff_back );
    // mButtonBack.setOnClickListener( this );
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

