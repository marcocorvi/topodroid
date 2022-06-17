/* @file SurveyCalibrationDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device info dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import java.util.Locale;

import android.os.Bundle;
import android.content.Context;

import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;

class SurveyCalibrationDialog extends MyDialog
                       implements View.OnClickListener
{
  private EditText mETlength;
  private EditText mETazimuth;
  private EditText mETclino;
  // private Button mBTsave;
  // private Button mBTcancel;
  private CheckBox mCBlrud;

  /** cstr
   * @param context   context
   */
  SurveyCalibrationDialog( Context context )
  {
    super( context, null, R.string.SurveyCalibrationDialog ); // null app
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.survey_calibration_dialog, R.string.calibration_title );

    mETlength  = (EditText) findViewById( R.id.et_length );
    mETazimuth = (EditText) findViewById( R.id.et_azimuth );
    mETclino   = (EditText) findViewById( R.id.et_clino );
    // ManualCalibration fields are in the current units and are presented in the current units
    mETlength.setText(  String.format(Locale.US, "%.2f", ManualCalibration.mLength  * TDSetting.mUnitLength ) );
    mETazimuth.setText( String.format(Locale.US, "%.1f", ManualCalibration.mAzimuth * TDSetting.mUnitAngle ) );
    mETclino.setText(   String.format(Locale.US, "%.1f", ManualCalibration.mClino   * TDSetting.mUnitAngle ) );

    mCBlrud   = (CheckBox) findViewById( R.id.cb_lrud );
    mCBlrud.setChecked( ManualCalibration.mLRUD );

    ( (Button) findViewById( R.id.btn_save ) ).setOnClickListener( this );
    ( (Button) findViewById( R.id.btn_cancel ) ).setOnClickListener( this );
  }

  /** respond to a user tap: update static fields in the ManualCalibration class
   * @param view tapped view
    * @note ManualCalibration fields are stored in [m, degrees]
   */
  @Override
  public void onClick(View view)
  {
    hide();
    int vid = view.getId();
    if ( vid == R.id.btn_save ) {
      if ( mETlength.getText() != null ) {
        try {
          ManualCalibration.mLength = Float.parseFloat( mETlength.getText().toString() ) / TDSetting.mUnitLength;
        } catch ( NumberFormatException e ) { }
      }
      if ( mETazimuth.getText() != null ) {
        try {
          ManualCalibration.mAzimuth = Float.parseFloat( mETazimuth.getText().toString() ) / TDSetting.mUnitAngle;
        } catch ( NumberFormatException e ) { }
      }
      if ( mETclino.getText() != null ) {
        try {
          ManualCalibration.mClino = Float.parseFloat( mETclino.getText().toString() ) / TDSetting.mUnitAngle;
        } catch ( NumberFormatException e ) { }
      }
      ManualCalibration.mLRUD = mCBlrud.isChecked();
    // } else if ( vid == R.id.btn_cancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}
