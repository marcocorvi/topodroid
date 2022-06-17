/* @file DeviceA3InfoDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX A3 device info dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox1;

import com.topodroid.ui.MyDialog;
import com.topodroid.dev.Device;
import com.topodroid.TDX.DeviceActivity;
import com.topodroid.TDX.R;
import com.topodroid.TDX.TopoDroidAlertDialog;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;

import android.view.View;

import android.widget.TextView;
// import android.widget.RadioButton;
import android.widget.Button;

public class DeviceA3InfoDialog extends MyDialog
                         implements View.OnClickListener
{
  // private RadioButton mRBa3;
  // private RadioButton mRBx310;
  // private Button   mBTok;
  private Button   mBTcancel;
  private Button   mBTresetMemory;

  private final DeviceActivity mParent;
  private final Device mDevice;

  private TextView tv_serial;
  private TextView tv_statusAngle;
  private TextView tv_statusCompass;
  private TextView tv_statusCalib;
  private TextView tv_statusSilent;

  /** cstr
   * @param context   context
   * @param parent    parent activity
   * @param device    current device (DistoX A3)
   */
  public DeviceA3InfoDialog( Context context, DeviceActivity parent, Device device )
  {
    super( context, null, R.string.DeviceA3InfoDialog ); // null app
    mParent = parent;
    mDevice = device;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.device_a3_info_dialog, R.string.device_info  );

    Resources res = mParent.getResources();

    // mRBa3   = (RadioButton) findViewById( R.id.rb_a3 );
    // mRBx310 = (RadioButton) findViewById( R.id.rb_x310 );
    // mRBa3.setChecked( true );
    // // mRBx310.setChecked( false );

    TextView tv_address       = (TextView) findViewById( R.id.tv_address );
    tv_serial        = (TextView) findViewById( R.id.tv_serial );
    tv_statusAngle   = (TextView) findViewById( R.id.tv_status_angle );
    tv_statusCompass = (TextView) findViewById( R.id.tv_status_compass );
    tv_statusCalib   = (TextView) findViewById( R.id.tv_status_calib );
    tv_statusSilent  = (TextView) findViewById( R.id.tv_status_silent );

    tv_address.setText( String.format( res.getString( R.string.device_address ), mDevice.getAddress() ) );

    tv_serial.setText( res.getString( R.string.getting_info ) );
    // tv_statusAngle.setText(   TDString.EMPTY );
    // tv_statusCompass.setText( TDString.EMPTY );
    // tv_statusCalib.setText(   TDString.EMPTY );
    // tv_statusSilent.setText(  TDString.EMPTY );
    mParent.readA3Info( this );

    // mBTok = (Button) findViewById( R.id.btn_ok );
    // mBTok.setOnClickListener( this );
    mBTcancel = (Button) findViewById( R.id.button_cancel );
    mBTcancel.setOnClickListener( this );
    mBTresetMemory = (Button) findViewById( R.id.btn_reset_memory );
    mBTresetMemory.setOnClickListener( this );
  }

  /** update the display of the DistoX2 info
   * @param info   DistoX info
   */
  void updateInfo( DeviceA3Info info )
  {
    if ( info == null ) return;
    mParent.runOnUiThread( new Runnable() {
      public void run() {
        tv_serial.setText( info.mCode );
        tv_statusAngle.setText(   info.mAngle   );
        tv_statusCompass.setText( info.mCompass );
        tv_statusCalib.setText(   info.mCalib   );
        tv_statusSilent.setText(  info.mSilent  );
      }
    } );
  }

  /** react to a user tap: 
   *    dismiss the dialog if the tapped button is "CANCEL"
   *    clear the memory if the dialog is "MEMORY"
   * @param view tapped view
   */
  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    // if ( b == mBTok ) {
    //   // TODO ask confirm
    //   TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), mParent.getResources().getString( R.string.device_model_set ) + " ?",
    //     new DialogInterface.OnClickListener() {
    //       @Override
    //       public void onClick( DialogInterface dialog, int btn ) {
    //         doSetModel( );
    //       }
    //     }
    //   );
    // } else
    if ( b == mBTresetMemory ) {
      // TODO ask confirm
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), mParent.getResources().getString( R.string.device_clear ) + " ?",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doClearMemory( );
          }
        }
      );
    } else if ( b == mBTcancel ) {
      dismiss();
    }
  }

  /** clear the memory - forward the request to the parent app 
   */
  private void doClearMemory()
  {
    mParent.doClearA3Memory( );
  }

  // private void doSetModel()
  // {
  //   if ( mRBa3.isChecked() ) {
  //     mParent.setDeviceModel( mDevice, Device.DISTO_A3 );
  //   } else if ( mRBx310.isChecked() ) {
  //     mParent.setDeviceModel( mDevice, Device.DISTO_X310 );
  //   }
  // }

}
