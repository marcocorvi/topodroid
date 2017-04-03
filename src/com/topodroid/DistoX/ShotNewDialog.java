/* @file ShotNewDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid dialog for a new (manually entered) shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Locale;
import java.util.ArrayList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioButton;

// import android.widget.Spinner;
// import android.widget.ArrayAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.text.InputType;
import android.inputmethodservice.KeyboardView;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

import android.util.Log;


public class ShotNewDialog extends MyDialog
                           implements View.OnClickListener
                           , View.OnLongClickListener
                           , IBearingAndClino
{
  // private ShotWindow mParent;
  private TopoDroidApp mApp;
  private ILister mLister;
  private DBlock mPrevBlk;
  private boolean  notDone;
  private long mAt; // id of the shot where to add new shot (-1 end)

  private EditText mETfrom;
  private EditText mETto;
  private EditText mETdistance;
  private EditText mETbearing;
  private EditText mETclino;
  private LinearLayout mLbacksight;
  private EditText mETbackdistance;
  private EditText mETbackbearing;
  private EditText mETbackclino;
  // private Spinner  mExtend;
  private RadioButton mRadioLeft;
  private RadioButton mRadioVert;
  private RadioButton mRadioRight;

  private EditText mETleft;
  private EditText mETright;
  private EditText mETup;
  private EditText mETdown;
  private CheckBox mCBsplayAtTo;
  private Button   mBtnOk;
  private Button   mBtnSave;
  private Button   mBtnBack;
  private Button   mBtnSensor;
  private Button   mBtnCamera;
  private byte[] mJpegData; // camera jpeg data

  TimerTask mTimer;
  private MyKeyboard mKeyboard = null;

  public ShotNewDialog( Context context, TopoDroidApp app, ILister lister, DBlock last_blk, long at )
  {
    super( context, R.string.ShotNewDialog );
    mApp     = app;
    mLister  = lister;
    mPrevBlk = last_blk;
    notDone  = true;
    mAt      = at;
    mTimer   = null;
    mJpegData = null;
  }


  private boolean checkCameraHardware()
  {
    return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // TDLog.Log( TDLog.LOG_SHOT, "ShotNewDialog onCreate" );
    initLayout( R.layout.shot_new_dialog, R.string.shot_info );

    mETfrom = (EditText) findViewById(R.id.shot_from );
    mETto   = (EditText) findViewById(R.id.shot_to );

    mETfrom.setOnLongClickListener( this );
    mETto.setOnLongClickListener( this );

    mETdistance = (EditText) findViewById(R.id.shot_distance );
    mETbearing  = (EditText) findViewById(R.id.shot_bearing );
    mETclino    = (EditText) findViewById(R.id.shot_clino );

    mLbacksight = (LinearLayout) findViewById(R.id.shot_backsight );
    mETbackdistance = (EditText) findViewById(R.id.shot_backdistance );
    mETbackbearing  = (EditText) findViewById(R.id.shot_backbearing );
    mETbackclino    = (EditText) findViewById(R.id.shot_backclino );

    if ( ! TDSetting.mBacksight ) {
      TextView forsight = (TextView) findViewById(R.id.shot_forsight );
      forsight.setVisibility( View.GONE );
      mLbacksight.setVisibility( View.GONE );
    }

    mETleft     = (EditText) findViewById(R.id.shot_left );
    mETright    = (EditText) findViewById(R.id.shot_right );
    mETup       = (EditText) findViewById(R.id.shot_up );
    mETdown     = (EditText) findViewById(R.id.shot_down );

    mKeyboard = new MyKeyboard( mContext, (KeyboardView)findViewById( R.id.keyboardview ), 
                                R.xml.my_keyboard_base_sign, R.xml.my_keyboard_qwerty );
    if ( TDSetting.mKeyboard ) {
      int flag = MyKeyboard.FLAG_POINT_LCASE_2ND;
      if ( TDSetting.mStationNames == 1 ) flag = MyKeyboard.FLAG_POINT;
      MyKeyboard.registerEditText( mKeyboard, mETfrom, flag);
      MyKeyboard.registerEditText( mKeyboard, mETto,   flag);
      
      MyKeyboard.registerEditText( mKeyboard, mETdistance,     MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETbearing,      MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETclino,        MyKeyboard.FLAG_POINT_SIGN );
      MyKeyboard.registerEditText( mKeyboard, mETbackdistance, MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETbackbearing,  MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETbackclino,    MyKeyboard.FLAG_POINT_SIGN );
      MyKeyboard.registerEditText( mKeyboard, mETleft,         MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETright,        MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETup,           MyKeyboard.FLAG_POINT );
      MyKeyboard.registerEditText( mKeyboard, mETdown,         MyKeyboard.FLAG_POINT );
    } else {
      mKeyboard.hide();
      if ( TDSetting.mStationNames == 1 ) {
        mETfrom.setInputType( TDConst.NUMBER_DECIMAL );
        mETto.setInputType( TDConst.NUMBER_DECIMAL );
      }
      mETdistance.setInputType( TDConst.NUMBER_DECIMAL );
      mETbearing.setInputType( TDConst.NUMBER_DECIMAL );
      mETclino.setInputType( TDConst.NUMBER_DECIMAL_SIGNED );
      mETbackdistance.setInputType( TDConst.NUMBER_DECIMAL );
      mETbackbearing.setInputType( TDConst.NUMBER_DECIMAL );
      mETbackclino.setInputType( TDConst.NUMBER_DECIMAL_SIGNED );
      mETleft.setInputType( TDConst.NUMBER_DECIMAL );
      mETright.setInputType( TDConst.NUMBER_DECIMAL );
      mETup.setInputType( TDConst.NUMBER_DECIMAL );
      mETdown.setInputType( TDConst.NUMBER_DECIMAL );
    }

    // mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    if ( mAt < 0 ) {
      // prev blk is the last leg block of the survey
      if ( mPrevBlk != null ) {
        if ( TDSetting.mSurveyStations == 1 ) {
          mETfrom.setHint( mPrevBlk.mTo );
          mETto.setHint( DistoXStationName.increment( mPrevBlk.mTo ) );
        } else {
          mETto.setHint( mPrevBlk.mFrom );
          mETfrom.setHint( DistoXStationName.increment( mPrevBlk.mFrom ) );
        }
      } else {
        if ( TDSetting.mSurveyStations == 1 ) {
          mETfrom.setHint( DistoXStationName.mInitialStation );
          mETto.setHint( DistoXStationName.mSecondStation );
        } else {
          mETfrom.setHint( DistoXStationName.mSecondStation );
          mETto.setHint( DistoXStationName.mInitialStation );
        }
      }
      String current_name = mApp.getCurrentStationName();
      if ( current_name != null ) {
        if ( TDSetting.isSurveyForward() ) {
          mETfrom.setHint( current_name );
        } else {
          mETto.setHint( current_name );
        }
      }
    } else {
      // prev blk is the leg after which to add the new shot
      if ( TDSetting.mSurveyStations == 1 ) {
        mETfrom.setHint( mPrevBlk.mTo );
        mETto.setHint( "" );
      } else {
        mETfrom.setHint( "" );
        mETto.setHint( mPrevBlk.mFrom );
      }
    }

    mBtnOk    = (Button) findViewById(R.id.button_ok_shot_name );
    mBtnSave  = (Button) findViewById(R.id.button_save_shot_name );
    mBtnBack  = (Button) findViewById(R.id.button_cancel_name );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    int size = TopoDroidApp.getScaledSize( mContext );
    layout4.setMinimumHeight( size + 10 );

    // LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
    //   LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    // lp.setMargins( 0, -10, 20, 10 );

    mBtnSensor = new MyCheckBox( mContext, size, R.drawable.iz_compass, R.drawable.iz_compass ); 
    layout4.addView( mBtnSensor );
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBtnSensor.getLayoutParams();
    params.setMargins( 0, -10, 40, 10 );
    mBtnSensor.setLayoutParams( params );
    mBtnSensor.setOnClickListener( this );

    if ( TDSetting.mLevelOverAdvanced && checkCameraHardware() ) {
      
      mBtnCamera = new MyCheckBox( mContext, size, R.drawable.iz_camera, R.drawable.iz_camera ); 
      layout4.addView( mBtnCamera );
      params = (LinearLayout.LayoutParams) mBtnCamera.getLayoutParams();
      params.setMargins( 0, -10, 40, 10 );
      mBtnCamera.setLayoutParams( params );
      mBtnCamera.setOnClickListener( this );
    }

    mCBsplayAtTo = new CheckBox( mContext );
    mCBsplayAtTo.setText( R.string.splay_at_to );
    layout4.addView( mCBsplayAtTo );


    // mCBsplayAtTo = (CheckBox) findViewById( R.id.splay_at_to );
    // mBtnSensor = (Button) findViewById(R.id.button_sensor );

    mRadioLeft  = (RadioButton) findViewById(R.id.radio_left );
    mRadioVert  = (RadioButton) findViewById(R.id.radio_vert );
    mRadioRight = (RadioButton) findViewById(R.id.radio_right );
    // mExtend = (Spinner) findViewById( R.id.extend );
    // ArrayAdapter< CharSequence > adapter =
    //   ArrayAdapter.createFromResource( mContext, R.array.extend_name, android.R.layout.simple_spinner_item );   
    // adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
    // mExtend.setAdapter( adapter );

    mBtnOk.setOnClickListener( this );
    mBtnSave.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );

  }

  private void resetData( String from )
  {
    String to = DistoXStationName.increment( from );
    mETfrom.setText( from );
    mETto.setText(to);
    mETdistance.setText("");
    mETbearing.setText("");
    mETclino.setText("");
    mETbackdistance.setText("");
    mETbackbearing.setText("");
    mETbackclino.setText("");
    mETleft.setText("");
    mETright.setText("");
    mETup.setText("");
    mETdown.setText("");
  }

  // implements
  public void setBearingAndClino( float b, float c, int o )
  {
    // Log.v("DistoX", "ShotNewDialog set B " + b + " C " + c + " O " + o );
    mETbearing.setText( String.format(Locale.US, "%.1f", b ) );
    mETclino.setText( String.format(Locale.US, "%.1f", c ) );
  } 

  public void setJpegData( byte[] data ) { mJpegData = data; }

  @Override
  public boolean onLongClick( View v )
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  // FIXME synchronized ?
  @Override
  public void onClick(View v) 
  {
    if ( mTimer != null ) {
      mTimer.cancel( true );
      mTimer = null;
    }
    CutNPaste.dismissPopup();

    Button b = (Button) v;
    String val;
    // TDLog.Log( TDLog.LOG_INPUT, "ShotNewDialog onClick button " + b.getText().toString() );

    if ( b == mBtnOk || b == mBtnSave ) {
      String shot_from =  null;
      String shot_to = null;
   
      if ( notDone && mETfrom.getText() != null ) {
        shot_from = mETfrom.getText().toString();
      }
      if ( notDone && mETto.getText() != null ) {
        shot_to = mETto.getText().toString();
      }

      if ( ( shot_from == null || shot_from.length() == 0 ) &&
             ( shot_to == null || shot_to.length() == 0 ) ) {
        shot_from = mETfrom.getHint().toString();
        shot_to = mETto.getHint().toString();
      }

      shot_to = TopoDroidUtil.noSpaces( shot_to );
      if ( shot_to.equals(".") || shot_to.equals("-") ) shot_to = "";
      shot_from = TopoDroidUtil.noSpaces( shot_from );
      if ( shot_from.length() == 0 ) {
        mETfrom.setError( mContext.getResources().getString( R.string.error_from_required ) );
        return;
      }

      String distance = mETdistance.getText().toString().trim();
      String backdistance = mETbackdistance.getText().toString().trim();
      if ( distance.length() == 0 && backdistance.length() == 0 ) { 
        mETdistance.setError( mContext.getResources().getString( R.string.error_length_required ) );
        return;
      }
      String bearing = mETbearing.getText().toString().trim();
      String backbearing = mETbackbearing.getText().toString().trim();
      if ( bearing.length() == 0 && backbearing.length() == 0 ) {
        mETbearing.setError( mContext.getResources().getString( R.string.error_azimuth_required ) );
        return;
      }
      String clino = mETclino.getText().toString().trim();
      String backclino = mETbackclino.getText().toString().trim();
      if ( clino.length() == 0 && backclino.length() == 0 ) {
        mETclino.setError( mContext.getResources().getString( R.string.error_clino_required ) );
        return;
      }

      notDone = false;
      // Log.v( TopoDroidApp.TAG, "data " + distance + " " + bearing + " " + clino );

      long shot_extend = DBlock.EXTEND_RIGHT; // DBlock.EXTEND_UNSET; // FIXME-EXTEND
      if ( mRadioLeft.isChecked() ) { shot_extend = -1; }
      else if ( mRadioVert.isChecked() ) { shot_extend = 0; }
      else if ( mRadioRight.isChecked() ) { shot_extend = 1; }
      else { // let TopoDroid choose
        try {
          shot_extend = TDAzimuth.computeLegExtend( Float.parseFloat(bearing.replace(',','.') ) );
        } catch ( NumberFormatException e ) { }
      }

      long back_extend = - shot_extend;

      // switch ( mExtend.getSelectedItemPosition() ) {
      //   case 0: shot_extend = -1; break;
      //   case 1: shot_extend = 0;  break;
      //   case 2: shot_extend = 1;  break;
      //   // FIXME case 3: shot_extend = 2;  break;
      // }

      DBlock blk = null;
      try {
        if ( shot_to.length() > 0 ) {
          String splay_station = mCBsplayAtTo.isChecked() ? shot_to : shot_from;
          if ( distance.length() == 0 ) {
            distance = backdistance;
          } else if ( backdistance.length() == 0 ) {
            backdistance = distance;
          }
          if ( bearing.length() > 0 && clino.length() > 0 ) {
            if ( backbearing.length() > 0 && backclino.length() > 0 ) {
              blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat(backdistance.replace(',','.') ),
                             Float.parseFloat(backbearing.replace(',','.') ),
                             Float.parseFloat(backclino.replace(',','.') ),
                             back_extend,
                             null, null, null, null, null );
            }
            blk = mApp.insertManualShot( mAt, shot_from, shot_to,
                             Float.parseFloat( distance.replace(',','.') ),
                             Float.parseFloat( bearing.replace(',','.') ),
                             Float.parseFloat( clino.replace(',','.') ),
                             shot_extend,
                             mETleft.getText().toString().replace(',','.') ,
                             mETright.getText().toString().replace(',','.') ,
                             mETup.getText().toString().replace(',','.') ,
                             mETdown.getText().toString().replace(',','.') ,
                             splay_station );
          } else {
            if ( backbearing.length() > 0 && backclino.length() > 0 ) {
              blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat( backdistance.replace(',','.') ),
                             Float.parseFloat( backbearing.replace(',','.') ),
                             Float.parseFloat( backclino.replace(',','.') ),
                             back_extend,
                             mETleft.getText().toString().replace(',','.') ,
                             mETright.getText().toString().replace(',','.') ,
                             mETup.getText().toString().replace(',','.') ,
                             mETdown.getText().toString().replace(',','.') ,
                             splay_station );
            }
          }
        } else { // SPLAY SHOT
          if ( bearing.length() > 0 && clino.length() > 0 ) {
            blk = mApp.insertManualShot( mAt, shot_from, shot_to,
                             Float.parseFloat(distance.replace(',','.') ),
                             Float.parseFloat(bearing.replace(',','.') ),
                             Float.parseFloat(clino.replace(',','.') ),
                             shot_extend,
                             null, null, null, null, null );
          } else if ( backbearing.length() > 0 && backclino.length() > 0 ) {
            blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat(backdistance.replace(',','.') ),
                             Float.parseFloat(backbearing.replace(',','.') ),
                             Float.parseFloat(backclino.replace(',','.') ),
                             back_extend,
                             null, null, null, null, null );
          }
        }
        mApp.setCurrentStationName( null );
      } catch ( NumberFormatException e ) {
        TDLog.Error( "parse Float error: distance " + distance + " bearing " + bearing + " clino " + clino );
      }
      if ( blk != null ) {
        if ( mJpegData != null ) { 
          // Log.v("DistoX", "save Jpeg image size " + mJpegData.length );
          long photo_id = mApp.mData.nextPhotoId( mApp.mSID );
          File imagefile = new File( TDPath.getSurveyJpgFile( mApp.mySurvey, Long.toString(photo_id ) ) );
          try {
            FileOutputStream fos = new FileOutputStream( imagefile );
            fos.write( mJpegData );
            fos.flush();
            fos.close();
            mApp.mData.insertPhoto( mApp.mSID, photo_id, blk.mId,
                                    "",
                                    TopoDroidUtil.currentDate(),
                                    "snap " + shot_from + " " + shot_to ); // FIXME TITLE has to go
          } catch ( IOException e ) {
            TDLog.Error( "IO exception " + e.getMessage() );
          }
        }
        resetData( shot_to );
        if ( mLister !=  null ) {
          mLister.refreshDisplay( 1, false );
        }
        notDone = true;
      }
      
      if ( b == mBtnOk ) {
        dismiss();
      }
    } else if ( b == mBtnSensor ) {
      mTimer = new TimerTask( mContext, this, TimerTask.Y_AXIS, TDSetting.mTimerWait, 10 );
      mTimer.execute();
    } else if ( b == mBtnCamera && TDSetting.mLevelOverAdvanced ) {
      new QCamCompass( mContext, this, null, true, true).show();  // true = with_box, true=with_delay
    } else if ( b == mBtnBack ) {
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
    if ( CutNPaste.dismissPopup() ) return;
    if ( TDSetting.mKeyboard ) {
      if ( mKeyboard.isVisible() ) {
        mKeyboard.hide();
        return;
      }
    }
    dismiss();
  }

}

