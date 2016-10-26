/* @file AzimuthDialDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey azimuth dialog 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.text.method.KeyListener;

import android.content.Context;
import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.KeyEvent;

import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

import android.util.Log;


public class AzimuthDialDialog extends MyDialog
                              implements View.OnClickListener
                              , IBearingAndClino
{
  private ILister mParent;
  private float mAzimuth;
  private Bitmap mBMdial;

  // private Button mBTback;
  // private Button mBTfore;
  private Button mBTazimuth;
  private Button mBTsensor;
  private Button mBTok;
  private Button mBTleft;
  private Button mBTright;

  private Button mBtnCancel;

  private SeekBar mSeekBar;

  public AzimuthDialDialog( Context context, ILister parent, float azimuth, Bitmap dial )
  {
    super(context, R.string.AzimuthDialDialog );
    mParent  = parent;
    mAzimuth = azimuth;
    mBMdial  = dial;
  }

  private void updateView()
  {
    Matrix m = new Matrix();
    m.preRotate( mAzimuth - 90 );
    // float s = TDMath.cosd( ((mAzimuth % 90) - 45) );
    // m.postScale( s, s );
    int w = 96; // mBMdial.getWidth();
    Bitmap bm1 = Bitmap.createScaledBitmap( mBMdial, w, w, true );
    Bitmap bm2 = Bitmap.createBitmap( bm1, 0, 0, w, w, m, true);
    mBTazimuth.setBackgroundDrawable( new BitmapDrawable( mContext.getResources(), bm2 ) );
  }

  private void updateSeekBar() { mSeekBar.setProgress( ((int)mAzimuth + 180)%360 ); }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // requestWindowFeature(Window.FEATURE_NO_TITLE);
    // getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

    // TDLog.Log( TDLog.LOG_SHOT, "Shot Dialog::onCreate" );
    initLayout( R.layout.azimuth_dial_dialog, R.string.title_azimut );

    // mBTback = (Button) findViewById(R.id.btn_back );
    // mBTfore = (Button) findViewById(R.id.btn_fore );
    mBTazimuth = (Button) findViewById(R.id.btn_azimuth );
    // mBTsensor  = (Button) findViewById(R.id.btn_sensor );
    mBTok      = (Button) findViewById(R.id.btn_ok );
    mBTleft    = (Button) findViewById(R.id.btn_left );
    mBTright   = (Button) findViewById(R.id.btn_right );

    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );

    mSeekBar  = (SeekBar) findViewById( R.id.seekbar );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    int size = TopoDroidApp.getScaledSize( mContext );
    layout4.setMinimumHeight( size + 20 );

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 10, 20, 10 );

    mBTsensor = new MyCheckBox( mContext, size, R.drawable.iz_compass, R.drawable.iz_compass ); 
    // LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBTsensor.getLayoutParams();
    // params.setMargins( 10, 0, 0, 10 );
    // mBTsensor.setLayoutParams( params );
    layout4.addView( mBTsensor, lp );

    // mBTback.setOnClickListener( this );
    // mBTfore.setOnClickListener( this );
    mBTazimuth.setOnClickListener( this );
    mBTsensor.setOnClickListener( this );
    mBTok.setOnClickListener( this );
    mBTleft.setOnClickListener( this );
    mBTright.setOnClickListener( this );

    mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
      public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
        if ( fromUser ) {
          setBearingAndClino( (progress+180)%360, 0 );
        }
      }
      public void onStartTrackingTouch(SeekBar seekbar) { }
      public void onStopTrackingTouch(SeekBar seekbar) { }
    } );
    mSeekBar.setMax( 360 );

    updateSeekBar();
    updateView();
  }

  public void setBearingAndClino( float b0, float c0 )
  {
    mAzimuth = b0;
    updateView();
  }

  TimerTask mTimer = null;

  public void onClick(View v) 
  {
    if ( mTimer != null ) {
      mTimer.cancel( true );
      mTimer = null;
    }

    Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "AzimuthDialDialog onClick button " + b.getText().toString() );

    // if ( b == mBTback ) {
    //   mAzimuth -= 5;
    //   if ( mAzimuth < 0 ) mAzimuth += 360;
    //   updateSeekBar();
    //   updateView();
    // } else if ( b == mBTfore ) {
    //   mAzimuth += 5;
    //   if ( mAzimuth >= 360 ) mAzimuth -= 360;
    //   updateSeekBar();
    //   updateView();
    // } else 
    if ( b == mBtnCancel ) {
      dismiss();
    } else if ( b == mBTazimuth ) {
      mAzimuth += 90;
      if ( mAzimuth >= 360 ) mAzimuth -= 360;
      updateSeekBar();
      updateView();
    } else if ( b == mBTsensor ) {
      mTimer = new TimerTask( mContext, this );
      mTimer.execute();
    } else if ( b == mBTok ) {
      mParent.setRefAzimuth( mAzimuth, 0 );
      dismiss();
    } else if ( b == mBTleft ) {
      mParent.setRefAzimuth( mAzimuth, -1L );
      dismiss();
    } else if ( b == mBTright ) {
      mParent.setRefAzimuth( mAzimuth, 1L );
      dismiss();
    } else {
      dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( mTimer != null ) {
      mTimer.cancel( true );
      mTimer = null;
    }
    dismiss();
  }

}

