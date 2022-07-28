/* @file AzimuthDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey azimuth dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
// import com.topodroid.ui.MyTurnBitmap;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.TDLayout;
import com.topodroid.ui.ExifInfo;
import com.topodroid.prefs.TDSetting;

import java.util.Locale;

import android.os.Bundle;

import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.view.View;

import android.text.TextWatcher;
import android.text.Editable;

import android.widget.LinearLayout;
import android.widget.SeekBar;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

class AzimuthDialog extends MyDialog
                    implements View.OnClickListener
                    , IBearingAndClino
{
  // FIXME_AZIMUTH_DIAL
  // there are two attempts to the azimuth dial 
  // the first (1) uses a bitmap, the second (2) a turn-bitmap

  private final ILister mParent;
  private float mAzimuth;
  private Bitmap mBMdial;
  // private MyTurnBitmap mDialBitmap;

  private EditText mETazimuth;

  // private Button mBTback;
  // private Button mBTfore;
  private Button mBTazimuth;
  private Button mBTsensor;
  // private Button bt_ok;
  // private Button bt_left;
  // private Button bt_right;

  private Button mBtnCancel;

  private SeekBar mSeekBar;

  // AzimuthDialog( Context context, ILister parent, float azimuth, MyTurnBitmap dial ) // FIXME_AZIMUTH_DIAL 2

  /** cstr
   * @param context  context
   * @param parent   shot-lister parent (ShotWindow)
   * @param azimuth  ???
   * @param dial     ???
   */
  AzimuthDialog( Context context, ILister parent, float azimuth, Bitmap dial ) // FIXME_AZIMUTH_DIAL 1
  {
    super(context, null, R.string.AzimuthDialog ); // null app
    mParent  = parent;
    mAzimuth = azimuth;
    // mDialBitmap = dial;
    mBMdial = dial;
  }

  /** @return the source bitmap CW rotated by an angle 
   * @param azimuth   angle [degrees]
   * @param source    bitmap
   */
  static Bitmap getRotatedBitmap( float azimuth, Bitmap source )
  {
    Matrix m = new Matrix();
    m.preRotate( azimuth - 90 );
    // float s = TDMath.cosd( ((azimuth % 90) - 45) );
    // m.preScale( s, s );
    int w = 96; // source.getWidth();
    Bitmap bm1 = Bitmap.createScaledBitmap( source, w, w, true );
    return Bitmap.createBitmap( bm1, 0, 0, w, w, m, true);
    // rotatedBitmap( source, source.getWidth(), mAzimuth, 96 );
    // Bitmap bm2 = Bitmap.createBitmap( mPxl, 96, 96, Bitmap.Config.ALPHA_8 );
  }

  /** refresh the azimuth button
   */
  private void updateView()
  {
    Bitmap bm2 = getRotatedBitmap( mAzimuth, mBMdial );
    mBTazimuth.setBackgroundDrawable( new BitmapDrawable( mContext.getResources(), bm2 ) ); // DEPRECATED API-16

    // Bitmap bm2 = mDialBitmap.getBitmap( mAzimuth, 96 );
    // TDandroid.setButtonBackground( mBTazimuth, new BitmapDrawable( mContext.getResources(), bm2 ) );
  }

  /** refresh the editable text
   */
  private void updateEditText() { mETazimuth.setText( String.format(Locale.US, "%d", (int)mAzimuth ) ); }

  /** refresh the slider bar
   */
  private void updateSeekBar() { mSeekBar.setProgress( ((int)mAzimuth + 180)%360 ); }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // TDLog.Log( TDLog.LOG_SHOT, "Shot Dialog::onCreate" );
    initLayout( R.layout.azimuth_dialog, R.string.title_azimut );

    // mBTback = (Button) findViewById(R.id.btn_back );
    // mBTfore = (Button) findViewById(R.id.btn_fore );
    mBTazimuth = (Button) findViewById(R.id.btn_azimuth );
    // mBTsensor  = (Button) findViewById(R.id.btn_sensor );
    Button bt_ok      = (Button) findViewById(R.id.btn_ok );
    Button bt_left    = (Button) findViewById(R.id.btn_left );
    Button bt_right   = (Button) findViewById(R.id.btn_right );

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
        } catch ( NumberFormatException e ) { TDLog.Error("Error " + e.getMessage() ); }
      }
    } );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout4.setMinimumHeight( size + 40 );

    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    mBTsensor = new MyCheckBox( mContext, size, R.drawable.iz_compass, R.drawable.iz_compass );  // both was iz_compass_transp
    layout4.addView( mBTsensor, lp );

    // mBTback.setOnClickListener( this );
    // mBTfore.setOnClickListener( this );
    mBTazimuth.setOnClickListener( this );
    mBTsensor.setOnClickListener( this );
    bt_ok.setOnClickListener( this );
    bt_left.setOnClickListener( this );
    bt_right.setOnClickListener( this );

    mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
      public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
        if ( fromUser ) {
          setBearingAndClino( (progress+180)%360, 0, ExifInfo.ORIENTATION_UP, 3, 0 ); // clino 0, orientation 0, accuracy 3 (high), camera 0
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

  /**
   * @param b0  azimuth [degrees]
   * @param c0  clino (unused)
   * @param o0  orientation (unused)
   * @param a0  accuracy
   * @param cam camera API (unused)
   */
  public void setBearingAndClino( float b0, float c0, int o0, int a0, int cam )
  {
    // TDLog.v( "Azimuth dialog set orientation " + o0 + " bearing " + b0 + " clino " + c0 );
    mAzimuth = b0;
    updateView();
    updateEditText();
  }

  /** implement set the photo JPEG - use default (comment wen minsdk = 24)
   * @param data   JPEG data
   * @return false: the JPEG data are not saved
   */
  public boolean setJpegData( byte[] data ) { return false; }

  private TimerTask mTimer = null;

  /** react to a user tap
   * @param v    tapped view - it can be
   *   - CANCEL
   *   - SENSOR: start the timer to measure animuth and clino
   *   - OK: set azimuth and clino
   *   - LEFT: set fixed "left"
   *   - RIGHT: set fixed "right"
   */
  public void onClick(View v) 
  {
    if ( mTimer != null ) {
      mTimer.cancel( true );
      mTimer = null;
    }

    int id = v.getId();
    if ( id == R.id.button_cancel ) {
      dismiss();
    } else if ( id == R.id.btn_azimuth ) {
      // mAzimuth += 90; if ( mAzimuth >= 360 ) mAzimuth -= 360;
      mAzimuth = TDMath.add90( mAzimuth );
      updateSeekBar();
      updateView();
      updateEditText();
    // } else if ( id == mBTsensor.getId() ) {
    } else if ( (Button)v == mBTsensor ) {
      mTimer = new TimerTask( this, TimerTask.Y_AXIS, TDSetting.mTimerWait, 10 );
      mTimer.execute();
    } else if ( id == R.id.btn_ok ) {
      mParent.setRefAzimuth( mAzimuth, 0 );
      dismiss();
    } else if ( id == R.id.btn_left ) {
      mParent.setRefAzimuth( mAzimuth, -1L );
      dismiss();
    } else if ( id == R.id.btn_right ) {
      mParent.setRefAzimuth( mAzimuth, 1L );
      dismiss();
    // } else if ( id == R.id.btn_back ) {
    //   mAzimuth = TDMath.in360( mAzimuth-5 );
    //   updateSeekBar();
    //   updateView();
    //   updateEditText();
    // } else if ( id == R.id.btn_fore ) {
    //   mAzimuth = TDMath.in360( mAzimuth+5 );
    //   updateSeekBar();
    //   updateView();
    //   updateEditText();
    } else {
      dismiss();
    }
  }

  /** react to a tap on BACK - cancel the timer and close the dialog
   */
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

