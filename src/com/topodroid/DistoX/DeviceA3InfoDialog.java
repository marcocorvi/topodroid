/** @file DeviceA3InfoDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX A3 device info dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

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
import android.widget.RadioButton;
import android.widget.Button;

class DeviceA3InfoDialog extends Dialog
                         implements View.OnClickListener
{
  private TextView mTVserial;
  private RadioButton mRBa3;
  private RadioButton mRBx310;
  private Button   mBTok;
  // private Button   mBTcancel;

  DeviceActivity mParent;
  Device mDevice;

  DeviceA3InfoDialog( Context context, DeviceActivity parent, Device device )
  {
    super( context );
    mParent = parent;
    mDevice = device;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_a3_info_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mRBa3   = (RadioButton) findViewById( R.id.rb_a3 );
    mRBx310 = (RadioButton) findViewById( R.id.rb_x310 );
    mRBa3.setChecked( true );
    // mRBx310.setChecked( false );

    mTVserial = (TextView) findViewById( R.id.tv_serial );
    TextView tv_statusAngle   = (TextView) findViewById( R.id.tv_status_angle );
    TextView tv_statusCompass = (TextView) findViewById( R.id.tv_status_compass );
    TextView tv_statusCalib   = (TextView) findViewById( R.id.tv_status_calib );
    TextView tv_statusSilent  = (TextView) findViewById( R.id.tv_status_silent );

    setTitle( mParent.getResources().getString( R.string.device_info ) );

    mTVserial.setText( mParent.readDistoXCode() );
    byte res = mParent.readA3status();
    String angle_units = (( res & 0x01 ) != 0)? "grad" : "degree";
    String compass     = (( res & 0x04 ) != 0)? "on" : "off";
    String calib       = (( res & 0x08 ) != 0)? "calib" : "normal";
    String silent      = (( res & 0x10 ) != 0)? "on" : "off";

    tv_statusAngle.setText(   String.format( mParent.getResources().getString( R.string.device_status_angle ), angle_units ) );
    tv_statusCompass.setText( String.format( mParent.getResources().getString( R.string.device_status_compass ), compass ) );
    tv_statusCalib.setText(   String.format( mParent.getResources().getString( R.string.device_status_calib ), calib ) );
    tv_statusSilent.setText(  String.format( mParent.getResources().getString( R.string.device_status_silent ), silent ) );

    mBTok = (Button) findViewById( R.id.btn_ok );
    mBTok.setOnClickListener( this );
    // mBTcancel = (Button) findViewById( R.id.button_cancel );
    // mBTcancel.setOnClickListener( this );
  }

  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTok ) {
      // TODO ask confirm
      new TopoDroidAlertDialog( mParent, mParent.getResources(),
                                mParent.getResources().getString( R.string.device_model_set ) + " ?",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doSetModel( );
          }
        }
      );
    // } else if ( b == mBTcancel ) {
    //   dismiss();
    }
  }

  void doSetModel()
  {
    if ( mRBa3.isChecked() ) {
      mParent.setDeviceModel( mDevice, Device.DISTO_A3 );
    } else if ( mRBx310.isChecked() ) {
      mParent.setDeviceModel( mDevice, Device.DISTO_X310 );
    }
  }

}
