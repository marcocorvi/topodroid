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

import java.util.Locale;

// import android.app.Dialog;
import android.os.Bundle;

// import android.text.method.KeyListener;

import android.content.Context;
// import android.util.AttributeSet;
// import android.content.DialogInterface;

// import android.widget.TextView;
// import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.Window;
//  android.view.WindowManager;
// import android.view.KeyEvent;
// import android.view.inputmethod.EditorInfo;

import android.text.TextWatcher;
import android.text.Editable;

import android.widget.LinearLayout;
import android.widget.SeekBar;
// import android.widget.SeekBar.OnSeekBarChangeListener;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

// import android.util.Log;


class AzimuthDialDialog extends MyDialog
                              implements View.OnClickListener
                              , IBearingAndClino
{

  private ILister mParent;
  float mAzimuth;
  private Bitmap mBMdial;

  private EditText mETazimuth;

  // private Button mBTback;
  // private Button mBTfore;
  private Button mBTazimuth;
  private Button mBTsensor;
  private Button mBTok;
  private Button mBTleft;
  private Button mBTright;

  private Button mBtnCancel;

  private SeekBar mSeekBar;
  // private int[] mPxl;

  AzimuthDialDialog( Context context, ILister parent, float azimuth, Bitmap dial )
  {
    super(context, R.string.AzimuthDialDialog );
    mParent  = parent;
    mAzimuth = azimuth;
    mBMdial  = dial;
    // mPxl = new int[96*96];
  }

  // void rotatedBitmap( Bitmap bmp, int n1, float a, int n2 )
  // {
  //   float n11 = (n1-1.0f)/2;
  //   float n21 = (n2-1.0f)/2;
  //   float c = ( TDMath.cosd( a ) * n1 ) / n2;
  //   float s = ( TDMath.sind( a ) * n1 ) / n2;
  //   int i2 = n2/2;
  //   int i1 = i2-1;
  //   for ( int j=0; j<n2; ++j ) {
  //     float js = n11 - s * (j-n21);
  //     float jc = n11 + c * (j-n21);
  //     int i=i2; 
  //     for ( ; i<n2; ++i ) {
  //       try {
  //         mPxl[j*n2+i] = bmp.getPixel( (int)( js + c * (i-n21)), (int)( jc + s * (i-n21)) );
  //       } catch ( IllegalArgumentException e ) {
  //       	Log.v("DistoX", "break at J " + j + " I " + i );
  //         break; }
  //     }
  //     for ( ; i<n2; ++i ) mPxl[j*n2+i] = 0xffcc6666;
  //     i=i1; 
  //     for ( ; i>=0; --i ) {
  //       try {
  //         mPxl[j*n2+i] = bmp.getPixel( (int)( js + c * (i-n21)), (int)( jc + s * (i-n21)) );
  //       } catch ( IllegalArgumentException e ) { break; }
  //     }
  //     for ( ; i>=0; --i ) mPxl[j*n2+i] = 0xff66cc66;
  //   }
  // }

  private void updateView()
  {
    Matrix m = new Matrix();
    // float s = TDMath.cosd( ((mAzimuth % 90) - 45) );
    // m.preScale( s, s );
    m.preRotate( mAzimuth - 90 );
    int w = 96; // mBMdial.getWidth();
    Bitmap bm1 = Bitmap.createScaledBitmap( mBMdial, w, w, true );
    Bitmap bm2 = Bitmap.createBitmap( bm1, 0, 0, w, w, m, true);
    // rotatedBitmap( mBMdial, mBMdial.getWidth(), mAzimuth, 96 );
    // Bitmap bm2 = Bitmap.createBitmap( mPxl, 96, 96, Bitmap.Config.ALPHA_8 );
    mBTazimuth.setBackgroundDrawable( new BitmapDrawable( mContext.getResources(), bm2 ) );
  }

  private void updateEditText() { mETazimuth.setText( String.format(Locale.US, "%d", (int)mAzimuth ) ); }

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
    mETazimuth = (EditText) findViewById( R.id.et_azimuth );

    mETazimuth.addTextChangedListener( new TextWatcher() {
      @Override
      public void afterTextChanged( Editable e ) { }

      @Override
      public void beforeTextChanged( CharSequence cs, int start, int cnt, int after ) { }

      @Override
      public void onTextChanged( CharSequence cs, int start, int before, int cnt ) 
      {
        try {
          int azimuth = Integer.parseInt( mETazimuth.getText().toString() );
          if ( azimuth < 0 || azimuth > 360 ) azimuth = 0;
          mAzimuth = azimuth;
          updateSeekBar();
          updateView();
        } catch ( NumberFormatException e ) { }
      }
    } );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout4.setMinimumHeight( size + 40 );

    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 10, 20, 10 );

    mBTsensor = new MyCheckBox( mContext, size, R.drawable.iz_compass_transp, R.drawable.iz_compass_transp ); 
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
          setBearingAndClino( (progress+180)%360, 0, 0 ); // clino 0, orientation 0
        }
      }
      public void onStartTrackingTouch(SeekBar seekbar) { }
      public void onStopTrackingTouch(SeekBar seekbar) { }
    } );
    mSeekBar.setMax( 360 );

    updateSeekBar();
    updateView();
    updateEditText();
  }

  public void setBearingAndClino( float b0, float c0, int o0 )
  {
    mAzimuth = b0;
    updateView();
    updateEditText();
  }

  public void setJpegData( byte[] data ) { }

  private TimerTask mTimer = null;

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
    //   updateEditText();
    // } else if ( b == mBTfore ) {
    //   mAzimuth += 5;
    //   if ( mAzimuth >= 360 ) mAzimuth -= 360;
    //   updateSeekBar();
    //   updateView();
    //   updateEditText();
    // } else 
    if ( b == mBtnCancel ) {
      dismiss();
    } else if ( b == mBTazimuth ) {
      mAzimuth += 90;
      if ( mAzimuth >= 360 ) mAzimuth -= 360;
      updateSeekBar();
      updateView();
      updateEditText();
    } else if ( b == mBTsensor ) {
      mTimer = new TimerTask( mContext, this, TimerTask.Y_AXIS, TDSetting.mTimerWait, 10 );
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

