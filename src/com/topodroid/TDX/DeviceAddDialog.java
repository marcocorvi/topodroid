/* @file DeviceAddDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid manually add a device
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyStateBox;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.dev.Device;
import com.topodroid.prefs.TDSetting;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.List;

// import android.app.Dialog;
import android.os.Bundle;
// import android.os.Environment;

import android.content.Context;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.widget.LinearLayout;
import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.Window;
// import android.view.WindowManager;

import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import android.bluetooth.BluetoothDevice;

class DeviceAddDialog extends MyDialog
                  implements View.OnClickListener
{
  private Button mBtnConfirm;
  private Button mBtnClose;

  private final DeviceActivity mParent;

  private Spinner  mAddress;
  private Spinner  mModel;
  private EditText mNumber;
  private EditText mNickname;
  private String[] mAddresses;
  private String[] mModels;

  /** cstr
   * @param ctx    context
   * @param parent parent window
   */
  DeviceAddDialog( Context ctx, DeviceActivity parent, List< BluetoothDevice > devices )
  {
    super( ctx, null, R.string.DeviceAddDialog ); // null app

    mParent  = parent;
    mAddresses = new String[ devices.size() ];
    int k = 0;
    for ( BluetoothDevice device : devices ) mAddresses[k++] = device.getAddress();
    mModels = Device.getBleModels();
  }


  @Override
  protected void onCreate( Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.device_add_dialog, R.string.title_device_add );

    mBtnClose = (Button) findViewById( R.id.button_cancel );
    mBtnClose.setOnClickListener( this );

    mBtnConfirm = (Button) findViewById( R.id.button_ok );
    mBtnConfirm.setOnClickListener( this );

    mAddress   = (Spinner) findViewById( R.id.device_address  );
    mModel     = (Spinner) findViewById( R.id.device_model    );
    mNumber    = (EditText) findViewById( R.id.device_number    );
    mNickname  = (EditText) findViewById( R.id.device_nickname );

    mAddress.setAdapter( new ArrayAdapter<>( mContext, R.layout.menu, mAddresses ) );
    mModel.setAdapter( new ArrayAdapter<>( mContext, R.layout.menu, mModels ) );
  }


  /** implements user taps
   * @param v   tapped view
   */
  public void onClick(View v) 
  {
    if ( v.getId() == R.id.button_ok ) {
      if ( mNumber.getText() == null ) {
        mNumber.setError( mContext.getResources().getString( R.string.model_missing ) );
        return;
      }
      String number = mNumber.getText().toString();
      Pattern p = Pattern.compile( "[0-9]{4}" );
      if ( ! p.matcher( number ).matches() ) {
        mNumber.setError( mContext.getResources().getString( R.string.model_invalid ) );
        return;
      }
      String address = mAddresses[ mAddress.getSelectedItemPosition() ];
      String model = mModels[ mModel.getSelectedItemPosition() ];
      String model_number = model + mNumber.getText().toString();
      String nickname = ( mNickname.getText() == null )? null :  mNickname.getText().toString();
      mParent.addDevice( address, model_number, nickname );
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    super.onBackPressed();
  }

}

