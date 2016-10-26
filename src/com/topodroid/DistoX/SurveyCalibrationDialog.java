/** @file SurveyCalibrationDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device info dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

class SurveyCalibrationDialog extends MyDialog
                       implements View.OnClickListener
{
  private EditText mETlength;
  private EditText mETazimuth;
  private EditText mETclino;
  private Button mBTok;
  private Button mBTback;

  SurveyActivity mParent;
  TopoDroidApp mApp;

  SurveyCalibrationDialog( Context context, SurveyActivity parent )
  {
    super( context, R.string.SurveyCalibrationDialog );
    mParent = parent;
    mApp    = mParent.mApp;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.survey_calibration_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mETlength  = (EditText) findViewById( R.id.et_length );
    mETazimuth = (EditText) findViewById( R.id.et_azimuth );
    mETclino   = (EditText) findViewById( R.id.et_clino );
    mETlength.setText(  Float.toString( mApp.mManualCalibrationLength ) );
    mETazimuth.setText( Float.toString( mApp.mManualCalibrationAzimuth ) );
    mETclino.setText(   Float.toString( mApp.mManualCalibrationClino ) );

    setTitle( mParent.getResources().getString( R.string.calibration_title ) );

    mBTok = (Button) findViewById( R.id.button_ok );
    mBTok.setOnClickListener( this );
    mBTback = (Button) findViewById( R.id.button_back );
    mBTback.setOnClickListener( this );

  }

  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTok ) {
      if ( mETlength.getText() != null ) {
        try {
          mApp.mManualCalibrationLength = Float.parseFloat( mETlength.getText().toString() );
        } catch ( NumberFormatException e ) { }
      }
      if ( mETazimuth.getText() != null ) {
        try {
          mApp.mManualCalibrationAzimuth = Float.parseFloat( mETazimuth.getText().toString() );
        } catch ( NumberFormatException e ) { }
      }
      if ( mETclino.getText() != null ) {
        try {
          mApp.mManualCalibrationClino = Float.parseFloat( mETclino.getText().toString() );
        } catch ( NumberFormatException e ) { }
      }
    } else if ( b == mBTback ) {
      /* nothing */
    }
    dismiss();
  }

}
