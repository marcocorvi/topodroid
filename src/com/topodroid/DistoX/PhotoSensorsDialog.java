/* @file PhotoSensorsDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo dialog (to enter the name of the photo)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;

import android.widget.TextView;
// import android.widget.EditText;
import android.widget.Button;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

import android.widget.LinearLayout;

import android.util.Log;

public class PhotoSensorsDialog extends MyDialog
                                implements View.OnClickListener
{
  private ShotActivity mParent;
  private DistoXDBlock mBlk;

  private TextView mTVstations;
  private TextView mTVdata;

  private MyCheckBox mButtonPhoto;
  private MyCheckBox mButtonSensor;
  private MyCheckBox mButtonShot;
  private MyCheckBox mButtonSurvey;
  private MyCheckBox mButtonDelete;

  /**
   * @param context   context
   * @param calib     calibration activity
   * @param group     data group
   * @param data      calibration data (as string)
   */
  PhotoSensorsDialog( Context context, ShotActivity parent, DistoXDBlock blk )
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
    setContentView(R.layout.photo_sensor_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    LinearLayout layout4 = (LinearLayout) findViewById( R.id.layout4 );
    int size = TopoDroidApp.getScaledSize( mContext );
    layout4.setMinimumHeight( size + 20 );
    
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 10, 20, 10 );

    mButtonPhoto  = new MyCheckBox( mContext, size, R.drawable.iz_camera, R.drawable.iz_camera ); 
    mButtonSensor = new MyCheckBox( mContext, size, R.drawable.iz_sensor, R.drawable.iz_sensor ); 
    mButtonShot   = new MyCheckBox( mContext, size, R.drawable.iz_add_leg, R.drawable.iz_add_leg );
    mButtonSurvey = new MyCheckBox( mContext, size, R.drawable.iz_split, R.drawable.iz_split );
    mButtonDelete = new MyCheckBox( mContext, size, R.drawable.iz_delete, R.drawable.iz_delete );

    layout4.addView( mButtonPhoto, lp );
    layout4.addView( mButtonSensor, lp );
    layout4.addView( mButtonShot, lp );
    layout4.addView( mButtonSurvey, lp );
    layout4.addView( mButtonDelete, lp );

    layout4.invalidate();

    setTitle( R.string.title_photo );

    mTVstations = (TextView) findViewById( R.id.photo_shot_stations );
    mTVdata = (TextView) findViewById( R.id.photo_shot_data );
    mTVstations.setText( mBlk.Name() );
    mTVdata.setText( mBlk.dataString( mContext.getResources().getString(R.string.shot_data) ) );

    mButtonPhoto.setOnClickListener( this );
    mButtonSensor.setOnClickListener( this );
    // mButtonExternal.setOnClickListener( this );
    mButtonShot.setOnClickListener( this );
    mButtonSurvey.setOnClickListener( this );
    mButtonDelete.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "PhotoiSensorDialog onClick() " + b.getText().toString() );

    if ( b == mButtonPhoto ) {       // PHOTO
      mParent.askPhotoComment( );
      dismiss();
    } else if ( b == mButtonSensor ) { // SENSOIR
      mParent.askSensor( );
      dismiss();
    // } else if ( b == mButtonExternal ) {
    //   mParent.askExternal( );
    } else if ( b == mButtonShot ) {  // INSERT SHOT
      mParent.insertShotAt( mBlk );
      dismiss();
    } else if ( b == mButtonSurvey ) { // SPLIT
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.survey_split,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doSplitSurvey();
            dismiss();
          }
        } );
      // mParent.askSurvey( );
    } else if ( b == mButtonDelete ) { // DELETE
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.shot_delete,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doDeleteShot( mBlk.mId );
            dismiss();
          }
        } );
      // mParent.doDeleteShot( mBlk.mId );

    // } else if ( b == mButtonCancel ) {
    //   /* nothing */
    }
  }

}

