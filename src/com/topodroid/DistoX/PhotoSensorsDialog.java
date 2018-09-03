/* @file PhotoSensorsDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo dialog (to enter the name of the photo)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;


import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CheckBox;

// import android.util.Log;

class PhotoSensorsDialog extends MyDialog
                                implements View.OnClickListener
{
  private final ShotWindow mParent;
  private final DBlock mBlk;

  private boolean audioCheck = false;

  private TextView mTVstations;
  private TextView mTVdata;

  private RadioButton mRBfrom;
  private RadioButton mRBto;
  private RadioButton mRBat;
  private EditText mETat;
  private EditText mETleft;
  private EditText mETright;
  private EditText mETup;
  private EditText mETdown;
  private Button mBTlrud;
  private CheckBox mCBleg = null;

  // private MyCheckBox mButtonPlot;
  private MyCheckBox mButtonPhoto = null;
  private MyCheckBox mButtonAudio = null;
  private MyCheckBox mButtonSensor;
  private MyCheckBox mButtonShot;
  private MyCheckBox mButtonSurvey = null;

  private MyCheckBox mButtonDelete;
  private MyCheckBox mButtonCheck = null;

  private HorizontalListView mListView;
  private HorizontalButtonView mButtonView;
  private Button[] mButton;

  private Button mBtnCancel;

  /**
   * @param context   context
   * @param parent    parent shot list activity
   */
  PhotoSensorsDialog( Context context, ShotWindow parent, DBlock blk )
  {
    super( context, R.string.PhotoSensorsDialog );
    mParent  = parent;
    mBlk = blk;
    // TDLog.Log( TDLog.LOG_PHOTO, "PhotoSensorDialog");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log(  TDLog.LOG_PHOTO, "PhotoSensorDialog onCreate" );
    initLayout(R.layout.photo_sensor_dialog, R.string.title_photo );

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );

    audioCheck = FeatureChecker.checkMicrophone( mContext );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    layout4.setMinimumHeight( size + 20 );
    
    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    mRBfrom  = (RadioButton)findViewById( R.id.station_from );
    mRBto    = (RadioButton)findViewById( R.id.station_to );
    mRBat    = (RadioButton)findViewById( R.id.station_at );
    mETat    = (EditText)findViewById( R.id.station_distance );
    mETleft  = (EditText)findViewById( R.id.shot_left );
    mETright = (EditText)findViewById( R.id.shot_right );
    mETup    = (EditText)findViewById( R.id.shot_up );
    mETdown  = (EditText)findViewById( R.id.shot_down );
    mBTlrud  = (Button)findViewById( R.id.btn_ok );

    int nr_buttons = 5; // ( mBlk.type() == DBlock.BLOCK_MAIN_LEG )? 7 : 6;
    // mButtonPlot   = new MyCheckBox( mContext, size, R.drawable.iz_plot, R.drawable.iz_plot ); 
    mButtonPhoto  = new MyCheckBox( mContext, size, R.drawable.iz_camera, R.drawable.iz_camera ); 
    if ( audioCheck ) {
      mButtonAudio = new MyCheckBox( mContext, size, R.drawable.iz_audio, R.drawable.iz_audio ); 
      mButtonAudio.setOnClickListener( this );
    } else {
      -- nr_buttons;
    }
    mButtonSensor = new MyCheckBox( mContext, size, R.drawable.iz_sensor, R.drawable.iz_sensor ); 
    mButtonShot   = new MyCheckBox( mContext, size, R.drawable.iz_add_leg, R.drawable.iz_add_leg );
    if ( TDLevel.overAdvanced ) {
      mButtonSurvey = new MyCheckBox( mContext, size, R.drawable.iz_split, R.drawable.iz_split );
      mButtonSurvey.setOnClickListener( this );
    } else {
      -- nr_buttons;
    }

    mButton = new Button[nr_buttons];
    int pos = 0;
    mButton[pos++] = mButtonPhoto;
    if ( audioCheck ) mButton[pos++] = mButtonAudio;
    mButton[pos++] = mButtonSensor;
    mButton[pos++] = mButtonShot;
    if ( mButtonSurvey != null ) mButton[pos++] = mButtonSurvey;

    mListView = (HorizontalListView) findViewById(R.id.listview);
    // mListView.setEmptyPlacholder( true );
    /* size = */ TopoDroidApp.setListViewHeight( mContext, mListView );
    mButtonView = new HorizontalButtonView( mButton );
    mListView.setAdapter( mButtonView.mAdapter );
    layout4.invalidate();

    LinearLayout layout4b = (LinearLayout) findViewById( R.id.layout4b );
    layout4b.setMinimumHeight( size + 20 );

    mButtonDelete = new MyCheckBox( mContext, size, R.drawable.iz_delete_transp, R.drawable.iz_delete_transp );
    mButtonDelete.setOnClickListener( this );
    // mCBleg = (CheckBox) findViewById( R.id.leg ); // delete whole leg
    layout4b.addView( mButtonDelete );
    mButtonDelete.setLayoutParams( lp );

    LinearLayout layout4c = (LinearLayout) findViewById( R.id.layout4c );
    if ( mBlk.isMainLeg() ) {
      mCBleg = new CheckBox( mContext );
      mCBleg.setText( R.string.delete_whole_leg );
      mCBleg.setChecked( false );
      layout4b.addView( mCBleg );
      mCBleg.setLayoutParams( lp );
      if ( TDLevel.overAdvanced && mBlk.mShotType == 0 ) {
        layout4c.setMinimumHeight( size + 20 );
        mButtonCheck  = new MyCheckBox( mContext, size, R.drawable.iz_compute_transp, R.drawable.iz_compute_transp );
        mButtonCheck.setOnClickListener( this );
        layout4c.addView( mButtonCheck );
        mButtonCheck.setLayoutParams( lp );
      } else {
        layout4c.setVisibility( View.GONE );
      }
    } else {
      layout4c.setVisibility( View.GONE );
    }
    
    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );

    mTVstations = (TextView) findViewById( R.id.photo_shot_stations );
    mTVdata = (TextView) findViewById( R.id.photo_shot_data );
    mTVstations.setText( mBlk.Name() );
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_NORMAL ) {
      mTVdata.setText( mBlk.dataStringNormal( mContext.getResources().getString(R.string.shot_data) ) );
    } else { // SurveyInfo.DATAMODE_DIVING
      mTVdata.setText( mBlk.dataStringDiving( mContext.getResources().getString(R.string.shot_data) ) );
    }

    if ( mBlk.mFrom.length() > 0 ) {
      mRBfrom.setText( mBlk.mFrom );
      mRBfrom.setChecked( true );
      if ( mBlk.mTo.length() > 0 ) {
        mRBto.setText( mBlk.mTo );
        mETat.setText( "0" );
      } else {
        mRBto.setVisibility( View.GONE );
        mRBat.setVisibility( View.GONE );
        mETat.setVisibility( View.GONE );
      }
      mBTlrud.setOnClickListener( this );
    } else {
      mRBfrom.setVisibility( View.GONE );
      mRBto.setVisibility( View.GONE );
      mRBat.setVisibility( View.GONE );
      mETat.setVisibility( View.GONE );
      mETleft.setVisibility( View.GONE );
      mETright.setVisibility( View.GONE );
      mETup.setVisibility( View.GONE );
      mETdown.setVisibility( View.GONE );
      mBTlrud.setVisibility( View.GONE );
    }

    // mButtonPlot.setOnClickListener( this );
    mButtonPhoto.setOnClickListener( this );
    mButtonSensor.setOnClickListener( this );
    // mButtonExternal.setOnClickListener( this );
    mButtonShot.setOnClickListener( this );

  }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "PhotoiSensorDialog onClick() " + b.getText().toString() );

    if ( b == mBTlrud ) { // AT-STATION LRUD
      long at = mBlk.mId;
      String station = null;
      if ( mRBto.isChecked() ) { // TO
        station = mBlk.mTo;
      } else if ( mRBfrom.isChecked() ) { // FROM
        station = mBlk.mFrom;
      } else { 
	float d = -1;
	String dstr = mETat.getText().toString().replace(',','.');
	try { d = Float.parseFloat( dstr ); } catch ( NumberFormatException e ) { }
        // add a duplicate leg d, mBlk.mBearing, mBlk.mClino
	String from = mBlk.mFrom;
	station = from + "-" + dstr;
	at = mParent.insertDuplicateLeg( from, station, d, mBlk.mBearing, mBlk.mClino, mBlk.getExtend() );
      }
      if ( station != null ) {
        // check the data
        mParent.insertLRUDatStation( at, station, mBlk.mBearing, mBlk.mClino, 
          mETleft.getText().toString().replace(',','.') ,
          mETright.getText().toString().replace(',','.') ,
          mETup.getText().toString().replace(',','.') ,
          mETdown.getText().toString().replace(',','.') 
        );
      }
      dismiss();
    // } else if ( b == mButtonPlot ) {       // PHOTO
    //   mParent.highlightBlock( mBlk );
    //   dismiss();
    } else if ( b == mButtonPhoto ) {       // PHOTO
      mParent.askPhotoComment( );
      dismiss();
    } else if ( audioCheck && b == mButtonAudio ) {       // AUDIO
      mParent.startAudio( mBlk );
      dismiss();
    } else if ( b == mButtonSensor ) { // SENSOIR
      mParent.askSensor( );
      dismiss();
    // } else if ( b == mButtonExternal ) {
    //   mParent.askExternal( );
    } else if ( b == mButtonShot ) {  // INSERT SHOT
      mParent.dialogInsertShotAt( mBlk );
      dismiss();
    } else if ( mButtonSurvey != null && b == mButtonSurvey ) { // SPLIT
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.survey_split,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doSplitSurvey();
            dismiss();
          }
        } );
      // mParent.askSurvey( );
    } else if ( mButtonCheck != null && b == mButtonCheck ) { // CHECK
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.shot_check,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doDeleteShot( mBlk.mId, mBlk, TDStatus.CHECK, true );
            dismiss();
          }
        } );
    } else if ( b == mButtonDelete ) { // DELETE
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.shot_delete,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doDeleteShot( mBlk.mId, mBlk, TDStatus.DELETED, (mCBleg != null && mCBleg.isChecked()) );
            dismiss();
          }
        } );
      // mParent.doDeleteShot( mBlk.mId );

    } else if ( b == mBtnCancel ) {
      /* nothing */
      dismiss();
    }
  }

}

