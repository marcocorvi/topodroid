/* @file DeviceNameDialog.java
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
import com.topodroid.dev.Device;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
// import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;

class DeviceNameDialog extends MyDialog
                       implements View.OnClickListener
{
  // private TextView mTVmodel;
  // private TextView mTVaddress;
  // private TextView mTVname;
  private EditText mETnickname;
  private CheckBox mCBsecondDevice;
  private Button mBTok;
  // private Button mBTback;

  private final DeviceActivity mParent;
  private final Device mDevice;

  DeviceNameDialog( Context context, DeviceActivity parent, Device device )
  {
    super( context, null, R.string.DeviceNameDialog ); // null app
    mParent = parent;
    mDevice = device;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.device_name_dialog, R.string.device_info );

    TextView mTVmodel = (TextView) findViewById( R.id.tv_model );
    TextView mTVname = (TextView) findViewById( R.id.tv_name );
    TextView mTVaddress = (TextView) findViewById( R.id.tv_address );
    mETnickname = (EditText) findViewById( R.id.tv_nickname );

    mCBsecondDevice = (CheckBox) findViewById( R.id.second_device );

    // setTitle( mParent.getResources().getString( R.string.device_info ) );

    mTVmodel.setText( mDevice.mModel );
    mTVname.setText( mDevice.mName );
    mTVaddress.setText( mDevice.getAddress() );
    if ( mDevice.mNickname != null && mDevice.mNickname.length() > 0 ) {
      mETnickname.setText( mDevice.mNickname );
    }

    mBTok = (Button) findViewById( R.id.button_ok );
    mBTok.setOnClickListener( this );
    // mBTback = (Button) findViewById( R.id.button_cancel );
    // mBTback.setOnClickListener( this );
    ((Button)findViewById( R.id.button_cancel ) ).setOnClickListener( this );
  }

  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTok ) {
      if ( mETnickname.getText() != null ) {
        String nickname = mETnickname.getText().toString();
        mParent.setDeviceName( mDevice, nickname );
      }
      if ( mCBsecondDevice.isChecked() ) {
        mParent.setSecondDevice( mDevice.getAddress() );
      }
    // } else if ( b == mBTback ) {
    //   /* nothing */
    }
    dismiss();
  }

}
