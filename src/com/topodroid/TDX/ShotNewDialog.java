/* @file ShotNewDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid dialog for a new (manually entered) shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyKeyboard;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.ui.ExifInfo;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.ExtendType;


import java.util.Locale;

// import java.io.File; // JPEG FILE
// import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.os.Bundle;
import android.widget.RadioButton;

import android.content.Context;

import android.inputmethodservice.KeyboardView;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;


class ShotNewDialog extends MyDialog
                    implements View.OnClickListener
                    , View.OnLongClickListener
                    , IBearingAndClino
{
  // private ShotWindow mParent;
  private final ILister mLister;
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
  private Button   mBtnCamera = null; // TopoDroid camera button
  private byte[] mJpegData = null;    // camera jpeg data

  private static boolean mLRUDatTo = false;
  private boolean sensorCheck = false;
  private boolean cameraCheck = false;
  private ExifInfo mExif;

  private TimerTask mTimer;
  private MyKeyboard mKeyboard = null;
  private boolean diving;

  /** cstr
   * @param context    context
   * @param app        application
   * @param lister     data lister
   * @param last_blk   last data block
   * @param at         ID where to insert the new data
   */
  ShotNewDialog( Context context, TopoDroidApp app, ILister lister, DBlock last_blk, long at )
  {
    super( context, app, R.string.ShotNewDialog );
    mLister  = lister;
    mPrevBlk = last_blk;
    notDone  = true;
    mAt      = at;
    mTimer   = null;
    // mJpegData = null;
    sensorCheck = TDSetting.mWithAzimuth && TDLevel.overNormal;
    cameraCheck = TDSetting.mWithAzimuth && TDLevel.overAdvanced && TDandroid.checkCamera( mApp );
    diving = (TDInstance.datamode == SurveyInfo.DATAMODE_DIVING);
    mExif = new ExifInfo();
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

    if ( ( ! TDSetting.mBacksightInput ) || diving ) {
      TextView forsight = (TextView) findViewById(R.id.shot_forsight );
      forsight.setVisibility( View.GONE );
      mLbacksight.setVisibility( View.GONE );
    }

    if ( diving ) {
      mETdistance.setHint( R.string.input_depth );
      mETclino.setHint( R.string.input_length );
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
      
      if ( diving ) {
        MyKeyboard.registerEditText( mKeyboard, mETclino,        MyKeyboard.FLAG_POINT ); // diving length
        MyKeyboard.registerEditText( mKeyboard, mETdistance,     MyKeyboard.FLAG_POINT ); // diving depth 
      } else {
        MyKeyboard.registerEditText( mKeyboard, mETdistance,     MyKeyboard.FLAG_POINT );
        MyKeyboard.registerEditText( mKeyboard, mETclino,        MyKeyboard.FLAG_POINT_SIGN );
        MyKeyboard.registerEditText( mKeyboard, mETbackclino,    MyKeyboard.FLAG_POINT_SIGN );
        MyKeyboard.registerEditText( mKeyboard, mETbackdistance, MyKeyboard.FLAG_POINT );
        MyKeyboard.registerEditText( mKeyboard, mETbackbearing,  MyKeyboard.FLAG_POINT );
      }
      MyKeyboard.registerEditText( mKeyboard, mETbearing,      MyKeyboard.FLAG_POINT );
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
      if ( diving ) {
        mETclino.setInputType( TDConst.NUMBER_DECIMAL );    // diving length
        mETdistance.setInputType( TDConst.NUMBER_DECIMAL ); // diving depth
      } else {
        mETdistance.setInputType( TDConst.NUMBER_DECIMAL );
        mETclino.setInputType( TDConst.NUMBER_DECIMAL_SIGNED );
        mETbackdistance.setInputType( TDConst.NUMBER_DECIMAL );
        mETbackbearing.setInputType( TDConst.NUMBER_DECIMAL );
        mETbackclino.setInputType( TDConst.NUMBER_DECIMAL_SIGNED );
      }
      mETbearing.setInputType( TDConst.NUMBER_DECIMAL );
      mETleft.setInputType( TDConst.NUMBER_DECIMAL );
      mETright.setInputType( TDConst.NUMBER_DECIMAL );
      mETup.setInputType( TDConst.NUMBER_DECIMAL );
      mETdown.setInputType( TDConst.NUMBER_DECIMAL );
    }

    // mETfrom.setRawInputType( InputType.TYPE_CLASS_NUMBER );
    // mETto.setRawInputType( InputType.TYPE_CLASS_NUMBER );

    // FIXME setHint -> setText
    if ( mAt < 0 ) {
      // prev blk is the last leg block of the survey
      if ( mPrevBlk != null ) {
        // TDLog.v("SHOT NEW prev block " + mPrevBlk.mId );
        if ( StationPolicy.isSurveyForward() ) { // if ( StationPolicy.mSurveyStations == 1 ) FIXME_POLICY
          if ( mPrevBlk.mTo != null && mPrevBlk.mTo.length() > 0 ) {
            mETfrom.setText( mPrevBlk.mTo );
            mETto.setText( DistoXStationName.incrementName( mPrevBlk.mTo, mApp.getStationNames() ) );
          } else if ( mPrevBlk.mFrom != null && mPrevBlk.mFrom.length() > 0 ) {
            mETfrom.setText( mPrevBlk.mFrom );
          }
        } else {
          if ( mPrevBlk.mFrom != null && mPrevBlk.mFrom.length() > 0 ) {
            mETto.setText( mPrevBlk.mFrom );
            mETfrom.setText( DistoXStationName.incrementName( mPrevBlk.mFrom, mApp.getStationNames() ) );
          } else if ( mPrevBlk.mTo != null && mPrevBlk.mTo.length() > 0 ) {
            mETfrom.setText( mPrevBlk.mTo );
          }
        }
      } else {
        // TDLog.v("SHOT NEW prev block null");
        if ( StationPolicy.doTopoRobot() ) {
          DistoXStationName.mInitialStation = "1.0";
          DistoXStationName.mSecondStation  = "1.1";
          mETfrom.setText( DistoXStationName.mInitialStation );
          mETto.setText( DistoXStationName.mSecondStation );
        } else if ( StationPolicy.isSurveyForward() ) { // if ( StationPolicy.mSurveyStations == 1 ) FIXME_POLICY
          mETfrom.setText( DistoXStationName.mInitialStation );
          mETto.setText( DistoXStationName.mSecondStation );
        } else {
          mETfrom.setText( DistoXStationName.mSecondStation );
          mETto.setText( DistoXStationName.mInitialStation );
        }
      }
      String current_name = mApp.getCurrentStationName();
      if ( current_name != null ) {
        if ( StationPolicy.isSurveyForward() ) {
          mETfrom.setText( current_name );
        } else {
          mETto.setText( current_name );
        }
      }
    } else {
      // prev blk is the leg after which to add the new shot
      if ( StationPolicy.isSurveyForward() ) { // if ( StationPolicy.mSurveyStations == 1 ) FIXME_POLICY
        mETfrom.setText( mPrevBlk.mTo );
        mETto.setText( "" );
      } else {
        mETfrom.setText( "" );
        mETto.setText( mPrevBlk.mFrom );
      }
    }

    mBtnOk    = (Button) findViewById(R.id.button_ok_shot_name );
    mBtnSave  = (Button) findViewById(R.id.button_save_shot_name );
    mBtnBack  = (Button) findViewById(R.id.button_cancel_name );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout4.setMinimumHeight( size + 10 );

    if ( sensorCheck ) {
      mBtnSensor = new MyCheckBox( mContext, size, R.drawable.iz_compass, R.drawable.iz_compass );  // both was iz_compass_transp
      layout4.addView( mBtnSensor );
      TDLayout.setMargins( mBtnSensor, 0, -10, 40, 10 );
      mBtnSensor.setOnClickListener( this );
    }
    if ( cameraCheck && ! diving ) {
      mBtnCamera = new MyCheckBox( mContext, size, R.drawable.iz_camera, R.drawable.iz_camera ); // both was iz_camera_transp
      layout4.addView( mBtnCamera );
      TDLayout.setMargins( mBtnCamera, 0, -10, 40, 10 );
      mBtnCamera.setOnClickListener( this );
    }

    mCBsplayAtTo = new CheckBox( mContext );
    mCBsplayAtTo.setText( R.string.splay_at_to );
    mCBsplayAtTo.setChecked( mLRUDatTo );
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

    if ( TDAzimuth.mFixedExtend == -1L ) {  // FIXME FIXED_EXTEND 20240603 these five lines were commented
      mRadioLeft.setChecked( true );
    } else if ( TDAzimuth.mFixedExtend == 1L ) {
      mRadioRight.setChecked( true );
    }

    mBtnOk.setOnClickListener( this );
    mBtnSave.setOnClickListener( this );
    mBtnBack.setOnClickListener( this );

    mETdistance.requestFocus( );  // try to get focus to distance/depth field
  }

  /** reset UI data
   * @param from    FROM station
   * @note TO station is obtained by incrementing FROM station
   */
  private void resetData( String from )
  {
    String to = DistoXStationName.incrementName( from, mApp.getStationNames() );
    mETfrom.setText( from );
    mETto.setText(to);
    mETdistance.setText("");
    mETbearing.setText("");
    mETclino.setText("");
    if ( ! diving ) {
      mETbackdistance.setText("");
      mETbackbearing.setText("");
      mETbackclino.setText("");
    }
    mETleft.setText("");
    mETright.setText("");
    mETup.setText("");
    mETdown.setText("");
    mETdistance.requestFocus( );  // try to get focus to distance/depth field
  }

  /** @note implements: set azimuth/clino and orientation
   * @param b  azimuth [degrees]
   * @param c  clino [degrees]
   * @param o  camera orientation [degrees]
   * @param a  sensor accuracy
   * @param cam  camera
   */
  public void setBearingAndClino( float b, float c, int o, int a, int cam )
  {
    // TDLog.v( "New shot dialog set orientation " + o + " bearing " + b + " clino " + c );
    mExif.setExifValues( b, c, o, a, cam );
    mETbearing.setText( String.format(Locale.US, "%.1f", b ) );
    mETclino.setText( String.format(Locale.US, "%.1f", c ) );
  } 
 
  /** @note implements: store the image JPEG data
   * @param data   image data
   * @return true if the JPEG data are not null
   */
  public boolean setJpegData( byte[] data )
  {
    mJpegData = data;
    return mJpegData != null;
  }

  /** react to a user long-taps
   * @param v    tapped view
   * @return true if tap has been handled 
   */
  @Override
  public boolean onLongClick( View v )
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }

  /** convert a diving azimuth to normal azimuth
   * @param b    diving azimuth
   * @return normal azimuth
   */
  private float divingBearing( String b )
  {
    float bx = 0;
    if ( b != null && b.length() > 0 ) {
      try {
        bx = 360 - Float.parseFloat( b.replace(',','.') ); // DIVING_BEARING
        if ( bx >= 360 ) bx -= 360;
      } catch ( NumberFormatException e ) {
        TDLog.Error( e.getMessage() );
      }
    }
    return bx;
  }

  /** react to a user taps // FIXME synchronized ?
   * @param v    tapped view
   */
  @Override
  public void onClick(View v) 
  {
    if ( mTimer != null ) {
      mTimer.cancel( true );
      mTimer = null;
    }
    CutNPaste.dismissPopup();
    MyKeyboard.close( mKeyboard );

    Button b = (Button) v;
    String val;
    // TDLog.Log( TDLog.LOG_INPUT, "ShotNewDialog onClick button " + b.getText().toString() );

    if ( b == mBtnOk || b == mBtnSave ) {
      String shot_from =  null;
      String shot_to = "";
   
      if ( notDone && mETfrom.getText() != null ) {
        shot_from = TDUtil.toStationFromName( mETfrom.getText().toString() );
        if ( ! TDUtil.isStationName( shot_from ) ) {
          mETfrom.setError( mContext.getResources().getString( R.string.bad_station_name ) );
          return;
        }
      }
      if ( notDone && mETto.getText() != null ) {
        shot_to = TDUtil.toStationToName( mETto.getText().toString() );
        if ( ! TDUtil.isStationName( shot_to ) ) {
          mETto.setError( mContext.getResources().getString( R.string.bad_station_name ) );
          return;
        }
      }

      // if ( TDString.isNullOrEmpty( shot_from ) && TDString.isNullOrEmpty( shot_to ) ) {
      //   shot_from = mETfrom.getHint().toString();
      //   shot_to = mETto.getHint().toString();
      // }

      if ( TDString.isNullOrEmpty( shot_from ) ) {
        mETfrom.setError( mContext.getResources().getString( R.string.error_from_required ) );
        return;
      }

      String distance     = mETdistance.getText().toString().trim();
      String backdistance = mETbackdistance.getText().toString().trim();
      if ( distance.length() == 0 && backdistance.length() == 0 ) { 
        mETdistance.setError( mContext.getResources().getString( (diving? R.string.error_depth_required : R.string.error_length_required) ) );
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
        mETclino.setError( mContext.getResources().getString( (diving? R.string.error_length_required : R.string.error_clino_required) ) );
        return;
      }

      // TDLog.v( "data " + distance + " " + bearing + " " + clino );

      long shot_extend = ExtendType.EXTEND_RIGHT; // ExtendType.EXTEND_UNSET; // FIXME_EXTEND
      if ( mRadioLeft.isChecked() ) { shot_extend = ExtendType.EXTEND_LEFT; }
      else if ( mRadioVert.isChecked() ) { shot_extend = ExtendType.EXTEND_VERT; }
      else if ( mRadioRight.isChecked() ) { shot_extend = ExtendType.EXTEND_RIGHT; } // already assigned
      else { // let TopoDroid choose
        try {
          float bx = Float.parseFloat( bearing.replace(',','.') );
          if ( diving ) { bx = 360 - bx; if ( bx >= 360 ) bx -= 360; } // DIVING_BEARING
          shot_extend = TDAzimuth.computeLegExtend( bx );
        } catch ( NumberFormatException e ) { 
          // TDLog.Error("Non-number bearing");
          mETbearing.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
          return;
        }
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
          mLRUDatTo = mCBsplayAtTo.isChecked();
          String splay_station = mLRUDatTo ? shot_to : shot_from;
          if ( distance.length() == 0 ) {
            distance = backdistance;
          } else if ( backdistance.length() == 0 ) {
            backdistance = distance;
          }
          if ( bearing.length() > 0 && clino.length() > 0 ) {
            if ( backbearing.length() > 0 && backclino.length() > 0 ) {
              if ( diving ) {
                float bbx = divingBearing( backbearing ); // DIVING_BEARING
                blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat(backclino.replace(',','.') ),
                             bbx, // DIVING_BEARING Float.parseFloat(backbearing.replace(',','.') ),
                             Float.parseFloat(backdistance.replace(',','.') ),
                             back_extend, DBlock.FLAG_SURVEY,
                             null, null, null, null, null );
              } else {
                blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat(backdistance.replace(',','.') ),
                             Float.parseFloat(backbearing.replace(',','.') ),
                             Float.parseFloat(backclino.replace(',','.') ),
                             back_extend, DBlock.FLAG_SURVEY,
                             null, null, null, null, null );
              }
            }
	    if ( diving ) {
              float bx = divingBearing( bearing ); // DIVING_BEARING
              blk = mApp.insertManualShot( mAt, shot_from, shot_to,
                               Float.parseFloat( clino.replace(',','.') ),    // diving length
                               bx, // DIVING_BEARING Float.parseFloat( bearing.replace(',','.') ),
                               Float.parseFloat( distance.replace(',','.') ), // diving depth
                               shot_extend, DBlock.FLAG_SURVEY,
                               mETleft.getText().toString().replace(',','.') ,
                               mETright.getText().toString().replace(',','.') ,
                               mETup.getText().toString().replace(',','.') ,
                               mETdown.getText().toString().replace(',','.') ,
                               splay_station );
	    } else {
              blk = mApp.insertManualShot( mAt, shot_from, shot_to,
                               Float.parseFloat( distance.replace(',','.') ),
                               Float.parseFloat( bearing.replace(',','.') ),
                               Float.parseFloat( clino.replace(',','.') ),
                               shot_extend, DBlock.FLAG_SURVEY,
                               mETleft.getText().toString().replace(',','.') ,
                               mETright.getText().toString().replace(',','.') ,
                               mETup.getText().toString().replace(',','.') ,
                               mETdown.getText().toString().replace(',','.') ,
                               splay_station );
	    }
          } else {
            if ( backbearing.length() > 0 && backclino.length() > 0 ) {
              if ( diving ) {
                float bbx = divingBearing( backbearing ); // DIVING_BEARING
                blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat( backclino.replace(',','.') ),
                             bbx, // Float.parseFloat( backbearing.replace(',','.') ),
                             Float.parseFloat( backdistance.replace(',','.') ),
                             back_extend, DBlock.FLAG_SURVEY,
                             mETleft.getText().toString().replace(',','.') ,
                             mETright.getText().toString().replace(',','.') ,
                             mETup.getText().toString().replace(',','.') ,
                             mETdown.getText().toString().replace(',','.') ,
                             splay_station );
              } else {
                blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat( backdistance.replace(',','.') ),
                             Float.parseFloat( backbearing.replace(',','.') ),
                             Float.parseFloat( backclino.replace(',','.') ),
                             back_extend, DBlock.FLAG_SURVEY,
                             mETleft.getText().toString().replace(',','.') ,
                             mETright.getText().toString().replace(',','.') ,
                             mETup.getText().toString().replace(',','.') ,
                             mETdown.getText().toString().replace(',','.') ,
                             splay_station );
              }
            }
          }
        } else { // SPLAY SHOT
          if ( bearing.length() > 0 && clino.length() > 0 ) {
            if ( diving ) {
              float bx = divingBearing( bearing ); // DIVING_BEARING
              blk = mApp.insertManualShot( mAt, shot_from, shot_to,
                               Float.parseFloat(clino.replace(',','.') ),    // diving length
                               bx, // Float.parseFloat(bearing.replace(',','.') ),
                               Float.parseFloat(distance.replace(',','.') ), // diving depth
                               shot_extend, DBlock.FLAG_SURVEY,
                               null, null, null, null, null );
            } else {
              blk = mApp.insertManualShot( mAt, shot_from, shot_to,
                               Float.parseFloat(distance.replace(',','.') ),
                               Float.parseFloat(bearing.replace(',','.') ),
                               Float.parseFloat(clino.replace(',','.') ),
                               shot_extend, DBlock.FLAG_SURVEY,
                               null, null, null, null, null );
	    }
          } else if ( /* ! diving) && */ backbearing.length() > 0 && backclino.length() > 0 ) {
            if ( diving ) {
              float bbx = divingBearing( backbearing ); // DIVING_BEARING
              blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat(backclino.replace(',','.') ),
                             bbx, // Float.parseFloat(backbearing.replace(',','.') ),
                             Float.parseFloat(backdistance.replace(',','.') ),
                             back_extend, DBlock.FLAG_SURVEY,
                             null, null, null, null, null );
            } else {
              blk = mApp.insertManualShot( mAt, shot_to, shot_from,
                             Float.parseFloat(backdistance.replace(',','.') ),
                             Float.parseFloat(backbearing.replace(',','.') ),
                             Float.parseFloat(backclino.replace(',','.') ),
                             back_extend, DBlock.FLAG_SURVEY,
                             null, null, null, null, null );
            }
          }
        }
        mApp.setCurrentStationName( null );
      } catch ( NumberFormatException e ) {
        TDLog.Error( "parse Float error: distance " + distance + " bearing " + bearing + " clino " + clino );
      }
      if ( blk != null ) {
        notDone = false;
        if ( mJpegData != null ) { 
          // TDLog.v( "save Jpeg image size " + mJpegData.length );
          long photo_id = TopoDroidApp.mData.nextPhotoId( TDInstance.sid );
          String filepath = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(photo_id ) ); // JPEG FILE
          try {
            // File imagefile = TDFile.getTopoDroidFile( filepath );
            // FileOutputStream fos = TDFile.getFileOutputStream( imagefile );
            DataOutputStream fos = TDFile.getTopoDroidFileOutputStream( filepath );
            fos.write( mJpegData ); // FIXME may null pointer
            // fos.flush();
            fos.close();
            mExif.writeExif( filepath );
            TopoDroidApp.mData.insertShotPhoto( TDInstance.sid, photo_id, blk.mId,
                                    "",
                                    TDUtil.currentDateTime(),
                                    "snap " + shot_from + " " + shot_to,
                                    PhotoInfo.CAMERA_TOPODROID,
                                    ""  // TODO geomorphology code
            ); // FIXME TITLE has to go
          } catch ( IOException e ) {
            TDLog.Error( "IO exception " + e.getMessage() );
          }
        }
        resetData( shot_to );
        if ( mLister !=  null ) {
          // TDLog.v("new shot - lister refresh display" );
          mLister.refreshDisplay( 1, false );
        }
        notDone = true;
      } else {
        TDLog.Error("failed insert manual data block");
        return;
      }
      
      if ( b == mBtnOk ) {
        dismiss();
      }
    } else if ( sensorCheck && b == mBtnSensor ) {
      mTimer = new TimerTask( this, TimerTask.Y_AXIS, TDSetting.mTimerWait, 10 );
      mTimer.execute();
    } else if ( cameraCheck && b == mBtnCamera ) {
      new QCamCompass( mContext, TopoDroidApp.mShotWindow, this, null, true, true, PhotoInfo.CAMERA_TOPODROID ).show();
                       // null inserter, with_box, with_delay
    } else if ( b == mBtnBack ) {
      dismiss();
    }
  }

  /** implements user back-press
   */
  @Override
  public void onBackPressed()
  {
    if ( mTimer != null ) {
      mTimer.cancel( true );
      mTimer = null;
    }
    if ( CutNPaste.dismissPopup() ) return;
    if ( MyKeyboard.close( mKeyboard ) ) return;
    dismiss();
  }

}

