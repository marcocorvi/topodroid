/** @file BricInfoDialog.java
 */
package com.topodroid.dev.bric;

import com.topodroid.dev.Device;
import com.topodroid.dev.ble.BleConst;
import com.topodroid.dev.ble.BleUtils;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.R;

import com.topodroid.ui.MyDialog;

import android.os.Bundle;
import android.content.res.Resources;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

public class BricInfoDialog extends MyDialog
                         implements View.OnClickListener
{
  private final Resources mRes;
  private final Device mDevice;

  private TextView tv_device;
  private TextView tv_ble;
  private TextView tv_fw;
  private TextView tv_hw;
  private TextView tv_battery;

  public BricInfoDialog( Context ctx, Resources res, Device device )
  {
    super( ctx, R.string.BricInfoDialog );
    mRes    = res;
    mDevice = device;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.bric_info_dialog, R.string.device_info  );

    TextView tv_address  = (TextView) findViewById( R.id.bric_address );
    tv_device  = (TextView) findViewById( R.id.bric_device );
    tv_ble     = (TextView) findViewById( R.id.bric_ble );
    tv_fw      = (TextView) findViewById( R.id.bric_fw );
    tv_hw      = (TextView) findViewById( R.id.bric_hw );
    tv_battery = (TextView) findViewById( R.id.bric_battery );

    tv_address.setText( String.format( mRes.getString( R.string.device_address ), mDevice.getAddress() ) );
    tv_device.setText( String.format( mRes.getString( R.string.bric_device ), "..." )) ;
    tv_ble.setText( String.format( mRes.getString( R.string.bric_ble ), "..." ) );
    tv_fw.setText( String.format( mRes.getString( R.string.bric_fw ), "..." ) );
    tv_hw.setText( String.format( mRes.getString( R.string.bric_hw ), "..." ) );
    tv_battery.setText(  mRes.getString( R.string.bric_battery_wait ) );

    ((Button)findViewById( R.id.button_cancel )).setOnClickListener( this );
    // Log.v("DistoX", "Bric info dialog created");
  }

  @Override
  public void onClick(View view)
  {
    dismiss();
  }

  // this is done by the DeviceActivity
  // public void setValues( TopoDroidApp app )
  // {
  //   app.getBricInfo( this );
  // }

  public void getInfo( BricComm comm )
  {
    // Log.v("DistoX", "BricInfo read - srv: " + BleConst.INFO_SRV_UUID.toString() );
    // comm.enlistRead( BleConst.INFO_SRV_UUID, BleConst.INFO_23_UUID ); // manufacturer
    // comm.enlistRead( BleConst.INFO_SRV_UUID, BleConst.INFO_24_UUID );
    // comm.enlistRead( BleConst.INFO_SRV_UUID, BleConst.INFO_25_UUID );
    comm.enlistRead( BleConst.INFO_SRV_UUID, BleConst.INFO_26_UUID );
    comm.enlistRead( BleConst.INFO_SRV_UUID, BleConst.INFO_27_UUID );
    comm.enlistRead( BleConst.INFO_SRV_UUID, BleConst.INFO_28_UUID );
    // comm.enlistRead( BleConst.INFO_SRV_UUID, BleConst.INFO_29_UUID );
    comm.enlistRead( BleConst.DEVICE_SRV_UUID, BleConst.DEVICE_00_UUID );
    // comm.enlistRead( BleConst.DEVICE_SRV_UUID, BleConst.DEVICE_01_UUID );
    // comm.enlistRead( BleConst.DEVICE_SRV_UUID, BleConst.DEVICE_04_UUID );
    // comm.enlistRead( BleConst.DEVICE_SRV_UUID, BleConst.DEVICE_06_UUID );
    comm.enlistRead( BleConst.BATTERY_SRV_UUID, BleConst.BATTERY_LVL_UUID );
  }

  public void setValue( int type, byte[] bytes )
  {
    switch (type) {
      case BricComm.DATA_DEVICE_00:
        // Log.v("DistoX", "BricInfo Device " + BleUtils.bytesToAscii( bytes ) );
        tv_device.setText( String.format( mRes.getString( R.string.bric_device ), BleUtils.bytesToAscii( bytes ) ) );
        break;
      case BricComm.DATA_INFO_26:
        // Log.v("DistoX", "BricInfo Fw " + BleUtils.bytesToAscii( bytes ) );
        tv_ble.setText( String.format( mRes.getString( R.string.bric_ble ), BleUtils.bytesToAscii( bytes ) ) );
        break;
      case BricComm.DATA_INFO_27:
        // Log.v("DistoX", "BricInfo Hardware " + BleUtils.bytesToAscii( bytes ) );
        tv_fw.setText( String.format( mRes.getString( R.string.bric_fw ), BleUtils.bytesToAscii( bytes ) ) );
        break;
      case BricComm.DATA_INFO_28:
        // Log.v("DistoX", "BricInfo Fiwrmware " + BleUtils.bytesToAscii( bytes ) );
        tv_hw.setText( String.format( mRes.getString( R.string.bric_hw ), BleUtils.bytesToAscii( bytes ) ) );
        break;
      case BricComm.DATA_BATTERY_LVL:
        // Log.v("DistoX", "BricInfo Battery " + (int)(bytes[0]) );
        tv_battery.setText( String.format( mRes.getString( R.string.bric_battery ), (int)(bytes[0]) ) );
        break;
      default:
        break;
    }
  }

}
