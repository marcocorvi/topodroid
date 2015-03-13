/** @file DeviceX310InfoDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device info dialog
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

class DeviceX310InfoDialog extends Dialog
                           implements View.OnClickListener
{
  Context mContext;
  private TextView mTVcode;
  private TextView mTVfirmware;
  private TextView mTVhardware;
  // private TextView mTV;
  private RadioButton mRBa3;
  private RadioButton mRBx310;
  private Button mBTok;
  // private Button mBTback;

  DeviceActivity mParent;
  Device mDevice;

  DeviceX310InfoDialog( Context context, DeviceActivity parent, Device device )
  {
    super( context );
    mContext = context;
    mParent = parent;
    mDevice = device;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_x310_info_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mRBa3   = (RadioButton) findViewById( R.id.rb_a3 );
    mRBx310 = (RadioButton) findViewById( R.id.rb_x310 );
    // mRBa3.setChecked( false );
    mRBx310.setChecked( true );

    mTVcode = (TextView) findViewById( R.id.tv_code );
    mTVfirmware = (TextView) findViewById( R.id.tv_firmware );
    mTVhardware = (TextView) findViewById( R.id.tv_hardware );

    setTitle( mParent.getResources().getString( R.string.device_info ) );

    mTVcode.setText( mParent.readDistoXCode() );
    mTVfirmware.setText( mParent.readX310firmware() );
    mTVhardware.setText( mParent.readX310hardware() );

    mBTok = (Button) findViewById( R.id.btn_ok );
    mBTok.setOnClickListener( this );
    // mBTback = (Button) findViewById( R.id.button_cancel );
    // mBTback.setOnClickListener( this );

  }

  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTok ) {
      // TODO ask confirm
      new TopoDroidAlertDialog( mContext, mParent.getResources(),
                                mParent.getResources().getString( R.string.device_model_set ) + " ?",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doSetModel( );
          }
        }
      );
    // } else if ( b == mBTback ) {
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
