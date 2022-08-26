/* @file DistoXBLEInfoDialog.java
 *
 * @author siwei tian
 * @date aug 2022
 *
 * @brief TopoDroid DistoX BLE info dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox_ble;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.topodroid.TDX.DeviceActivity;
import com.topodroid.TDX.R;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
import com.topodroid.dev.ble.BleUtils;
import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;

import java.lang.ref.WeakReference;

public class DistoXBLEInfoDialog extends MyDialog
        implements View.OnClickListener
{
  // private RadioButton mRBa3;
  // private RadioButton mRBx310;
  // private Button mBTok;
  private Button mBTback;

  private final DeviceActivity mParent;
  private final Device mDevice;

  private TextView tv_code;
  private TextView tv_firmware;
  private TextView tv_hardware;

  private final WeakReference<TopoDroidApp> mApp; // FIXME LEAK

  /** cstr
   * @param context   context
   * @param parent    parent activity
   * @param device    current device (DistoX-BLE)
   */
  public DistoXBLEInfoDialog(Context context, DeviceActivity parent, Device device, TopoDroidApp app )
  {
    super( context, null, R.string.DeviceXBLEInfoDialog ); // null app FIXME DeviceXBLEInfoDialog is the help page of this dialog (need writing)
    mParent = parent;
    mDevice = device;
    mApp    = new WeakReference<TopoDroidApp>( app );
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.device_x310_info_dialog, R.string.device_info );

    Resources res = mParent.getResources();

    // mRBa3   = (RadioButton) findViewById( R.id.rb_a3 );
    // mRBx310 = (RadioButton) findViewById( R.id.rb_x310 );
    // // mRBa3.setChecked( false );
    // mRBx310.setChecked( true );

    TextView tv_address  = (TextView) findViewById( R.id.tv_address );
    tv_code     = (TextView) findViewById( R.id.tv_code );
    tv_firmware = (TextView) findViewById( R.id.tv_firmware );
    tv_hardware = (TextView) findViewById( R.id.tv_hardware );

    tv_address.setText( String.format( res.getString( R.string.device_address ), mDevice.getAddress() ) );
    tv_code.setText( res.getString( R.string.gettingble_info ) );
    // tv_firmware.setText( TDString.EMPTY );
    // tv_hardware.setText( TDString.EMPTY );
    // mParent.readXBLEInfo( this )
    // mApp.get().getDistoXBLEInfo(this);  only for test

    // mBTok = (Button) findViewById( R.id.btn_ok );
    // mBTok.setOnClickListener( this );
    mBTback = (Button) findViewById( R.id.button_cancel );
    mBTback.setOnClickListener( this );
  }

  /** set an info value
   * @param type   type of info value
   * @param value  value 
   */
  public void SetVal( final int type, String txtval )
  {
    if ( type == DistoXBLEProtocol.PACKET_INFO_FIRMWARE ) {
      mParent.runOnUiThread( new Runnable() {
        public void run() {
          tv_firmware.setText( "Firmware: " + txtval );
      } } );
    } else if ( type == DistoXBLEProtocol.PACKET_INFO_HARDWARE ) {
      mParent.runOnUiThread( new Runnable() {
        public void run() {
          tv_hardware.setText( "Hardware: " + txtval);
      } } );
    }
  }

  /** update the display of the DistoX2 info
   * @param info   DistoX2 info
   */
  void updateInfo( DistoXBLEInfo info )
  {
    if ( info == null ) return;
    mParent.runOnUiThread( new Runnable() {
      public void run() {
        tv_code.setText(     info.mCode );
        tv_firmware.setText( info.mFirmware );
        tv_hardware.setText( info.mHardware );
    } } );
  }

  /** react to a user tap: dismiss the dialog if the tapped button is "BACK"
   * @param view tapped view
   */
  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    // if ( b == mBTok ) {
    //   // TODO ask confirm
    //   TopoDroidAlertDialog.makeAlert( mContext, mParent.getResources(),
    //                             mParent.getResources().getString( R.string.device_model_set ) + " ?",
    //     new DialogInterface.OnClickListener() {
    //       @Override
    //       public void onClick( DialogInterface dialog, int btn ) {
    //         doSetModel( );
    //       }
    //     }
    //   );
    // } else
    if ( b == mBTback ) {
        dismiss();
    }
  }
}
