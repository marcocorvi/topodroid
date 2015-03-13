/* @file PhotoSensorsDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo dialog (to enter the name of the photo)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

// import java.Thread;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.TextView;
// import android.widget.EditText;
import android.widget.Button;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;


public class PhotoSensorsDialog extends Dialog
                                implements View.OnClickListener
{
  private ShotActivity mParent;
  private DistoXDBlock mBlk;

  private TextView mTVstations;
  private TextView mTVdata;
  private Button   mButtonPhoto;
  private Button   mButtonSensor;
  // private Button   mButtonExternal;
  private Button   mButtonShot;     // add shot
  private Button   mButtonSurvey;   // split survey

  private Button   mButtonDelete;
  // private Button   mButtonCancel;

  /**
   * @param context   context
   * @param calib     calibration activity
   * @param group     data group
   * @param data      calibration data (as string)
   */
  PhotoSensorsDialog( Context context, ShotActivity parent, DistoXDBlock blk )
  {
    super( context );
    mParent = parent;
    mBlk = blk;
    // TopoDroidLog.Log( TopoDroidLog.LOG_PHOTO, "PhotoSensorDialog");
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TopoDroidLog.Log(  TopoDroidLog.LOG_PHOTO, "PhotoSensorDialog onCreate" );
    setContentView(R.layout.photo_sensor_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mButtonPhoto    = (Button) findViewById(R.id.photo_photo );
    mButtonSensor   = (Button) findViewById(R.id.photo_sensor );
    // mButtonExternal = (Button) findViewById(R.id.photo_external );
    mButtonShot     = (Button) findViewById(R.id.photo_shot );
    mButtonSurvey   = (Button) findViewById(R.id.photo_survey );
    mButtonDelete = (Button) findViewById(R.id.photo_delete );
    // mButtonCancel = (Button) findViewById(R.id.button_cancel );

    setTitle( R.string.title_photo );

    mTVstations = (TextView) findViewById( R.id.photo_shot_stations );
    mTVdata = (TextView) findViewById( R.id.photo_shot_data );
    mTVstations.setText( mBlk.Name() );
    mTVdata.setText( mBlk.dataString() );

    mButtonPhoto.setOnClickListener( this );
    mButtonSensor.setOnClickListener( this );
    // mButtonExternal.setOnClickListener( this );
    mButtonShot.setOnClickListener( this );
    mButtonSurvey.setOnClickListener( this );
    mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TopoDroidLog.Log(  TopoDroidLog.LOG_INPUT, "PhotoiSensorDialog onClick() " + b.getText().toString() );

    if ( b == mButtonPhoto ) {
      mParent.askPhotoComment( );
    } else if ( b == mButtonSensor ) {
      mParent.askSensor( );
    // } else if ( b == mButtonExternal ) {
    //   mParent.askExternal( );
    } else if ( b == mButtonShot ) {
      mParent.askShot( );
    } else if ( b == mButtonSurvey ) {
      mParent.askSurvey( );
    } else if ( b == mButtonDelete ) {
      mParent.askDelete( );
    // } else if ( b == mButtonCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}

