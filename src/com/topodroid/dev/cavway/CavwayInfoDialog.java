/* @file DistoXBLEInfoDialog.java
 *
 * @author siwei tian
 * @date july 2024
 *
 * @brief TopoDroid Cavway info dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

import com.topodroid.TDX.DeviceActivity;
import com.topodroid.TDX.R;
// import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TopoDroidAlertDialog;
// import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
// import com.topodroid.dev.ble.BleUtils;
import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;

import android.content.Context;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CavwayInfoDialog extends MyDialog
        implements View.OnClickListener
{
  // private RadioButton mRBa3;
  // private RadioButton mRBx310;
  // private Button mBTok;
  private Button mBTback;
  private Button mBTsync;

  private final DeviceActivity mParent;
  private final Device mDevice;
  private boolean mDone;

  private TextView tv_code;
  private TextView tv_firmware;
  private TextView tv_hardware;
  private TextView tv_sync;
  private String mHardware = null;
  private String mFirmware = null;
  private long   mSyncOffset = 0; // TopoDroid time - Cavway time [s]

  private final WeakReference<TopoDroidApp> mApp; // FIXME LEAK

  /** cstr
   * @param context   context
   * @param parent    parent activity
   * @param device    current device (DistoX-BLE)
   */
  public CavwayInfoDialog(Context context, DeviceActivity parent, Device device, TopoDroidApp app )
  {
    super( context, null, R.string.CavwayInfoDialog ); // null app FIXME DeviceXBLEInfoDialog is the help page of this dialog (need writing)
    mParent = parent;
    mDevice = device;
    mApp    = new WeakReference<TopoDroidApp>( app );
    mDone   = false;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.device_cavway_info_dialog, R.string.device_info );

    Resources res = mParent.getResources();

    // mRBa3   = (RadioButton) findViewById( R.id.rb_a3 );
    // mRBx310 = (RadioButton) findViewById( R.id.rb_x310 );
    // // mRBa3.setChecked( false );
    // mRBx310.setChecked( true );

    TextView tv_address  = (TextView) findViewById( R.id.tv_address );
    tv_code     = (TextView) findViewById( R.id.tv_code );
    tv_firmware = (TextView) findViewById( R.id.tv_firmware );
    tv_hardware = (TextView) findViewById( R.id.tv_hardware );
    tv_sync     = (TextView) findViewById( R.id.tv_sync     );

    tv_address.setText( String.format( res.getString( R.string.device_address ), mDevice.getAddress() ) );
    tv_code.setText( res.getString( R.string.gettingCavway_info ) );
    // tv_firmware.setText( TDString.EMPTY );
    // tv_hardware.setText( TDString.EMPTY );
    // mParent.readXBLEInfo( this )
    // mApp.get().getDistoXBLEInfo(this);  only for test

    // mBTok = (Button) findViewById( R.id.btn_ok );
    // mBTok.setOnClickListener( this );
    mBTback = (Button) findViewById( R.id.button_cancel );
    mBTback.setOnClickListener( this );
    mBTsync = (Button) findViewById( R.id.button_sync );
    mBTsync.setOnClickListener( this );
  }

  /** set an info value
   * @param type   type of info value
   * @param txtval  value
   */
  public void setVal( final int type, String txtval )
  {
    if ( mDone ) return;
    TDLog.v("Set type " + type + " string " + txtval );
    if ( type == CavwayProtocol.PACKET_INFO_FIRMWARE ) {
      mFirmware = txtval;
    } else if ( type == CavwayProtocol.PACKET_INFO_HARDWARE ) {
      mHardware = txtval;
    } else {
      TDLog.v("Unexpected type " + type + " string " + txtval );
    }
  }

  /** update the sync offset
   * @param type type of info value - must be CavwayProtocol.PACKET_INFO_TIMESTAMP
   * @param val  cavway time [secs]
   */
  public void setVal( final int type, final long val )
  {
    if ( mDone ) return;
    TDLog.v("Set type " + type + " long " + val );
    if ( type == CavwayProtocol.PACKET_INFO_TIMESTAMP ) {
      Calendar cal = new GregorianCalendar();
      int timezoneseconds = ( cal.get( Calendar.ZONE_OFFSET ) + cal.get( Calendar.DST_OFFSET ) )/1000;
      mSyncOffset = val - TDUtil.getSeconds() - timezoneseconds;
      TDLog.v("Sync offset " + mSyncOffset + " : " + TDUtil.getSeconds() + " - " + val + " timezoe " + timezoneseconds );
    } else {
      TDLog.v("Unexpected type " + type + " value " + val );
    }
  }

  /** update hw/fw textfields
   * @note run on postexecute
   */
  void updateHwFwSync()
  {
    if ( mDone ) return;
    if ( mFirmware != null ) {
      tv_firmware.setText( String.format( "Firmware: %s", mFirmware ) );
    }
    if ( mHardware != null ) {
      tv_hardware.setText( String.format( "Hardware: %s", mHardware) );
    }
    int offset = ((int)mSyncOffset) / 60;
    TDLog.v( "sync offset " + mSyncOffset + " offset " + offset );
    if ( offset == 0 ) {
      tv_sync.setText( mParent.getResources().getString( R.string.cavway_sync_ok ) );
    } else {
      tv_sync.setText( String.format( mParent.getResources().getString( R.string.cavway_sync_offset ), offset ) );
    }
  }

  // /** update the display of the DistoX2 info
  //  * @param info   DistoX2 info
  //  */
  // void updateInfo( CavwayInfo info )
  // {
  //   if ( info == null ) return;
  //   if ( mDone ) return;
  //   mParent.runOnUiThread( new Runnable() {
  //     public void run() {
  //       tv_code.setText(     info.mCode );
  //       tv_firmware.setText( info.mFirmware );
  //       tv_hardware.setText( info.mHardware );
  //   } } );
  // }

  /** react to a user tap: dismiss the dialog if the tapped button is "BACK"
   * @param view tapped view
   */
  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTsync ) {
      mParent.syncDateTime();
    } else if ( b == mBTback ) {
      mDone = true;
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    mDone = true;
    super.onBackPressed();
  }
}
