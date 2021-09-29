/* @file BleScanDialog.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief Bluetoth low-energy scan dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDLog;
import com.topodroid.DistoX.DeviceActivity;
import com.topodroid.DistoX.R;

// import android.app.Dialog;

import android.content.Context;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import java.util.ArrayList;

public class BleScanDialog extends MyDialog
                 implements OnItemClickListener,
                 View.OnClickListener
{
  private DeviceActivity mActivity;
  private BleScanner mBleScanner = null;
  private BluetoothAdapter mBTadapter; // bluetooth adapter
  private String uuid_str;             // filter scan on this UUID, null = no filter

  private ArrayList< BluetoothDevice > mDevices;
  private ArrayAdapter< String > mAdapter;

  private ListView mList;
  private Button mBtnStop;


  public BleScanDialog( Context ctx, DeviceActivity activity, BluetoothAdapter adapter, String uuid )
  {
    super( ctx, R.string.BleScanDialog );
    mActivity  = activity;
    mBTadapter = adapter;
    uuid_str   = uuid;
    mDevices   = new ArrayList< BluetoothDevice >();
    // TDLog.v( "BLE scan cstr");
  }  

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.ble_scan_dialog, R.string.title_scan );

    mList = (ListView) findViewById( R.id.list );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );
    // ArrayList< String > names = new ArrayList<>();
    mAdapter = new ArrayAdapter<>( mContext, R.layout.message );
    mList.setAdapter( mAdapter );

    mBtnStop = (Button) findViewById( R.id.stop_scan );
    mBtnStop.setOnClickListener( this );
    ((Button) findViewById( R.id.cancel )).setOnClickListener( this );
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mBleScanner != null ) { 
      stopScan();
    }
    dismiss();
    BluetoothDevice device = mDevices.get( pos );
    // TDLog.v( "BLE scan on item click: " + BleUtils.deviceToString( device ) );
    mActivity.setBLEDevice( device );
  }

  @Override
  public void onClick( View v )
  {
    // TDLog.v( "BLE scan on click" );
    if ( v.getId() == R.id.stop_scan ) {
      if ( mBleScanner != null ) { 
        stopScan();
      } else {
        startScan( );
      }
    } else if ( v.getId() == R.id.cancel ) {
      if ( mBleScanner != null ) { 
        stopScan();
      }
      dismiss();
    }
  }

  // private void initLayout( int layout_resource, int title_resource )
  // {
  //   setContentView( layout_resource );
  //   getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
  //   setTitle( title_resource );
  // }

  private void startScan( )
  {
    // TDLog.v( "BLE scan ==== start");
    mAdapter.clear();
    mDevices.clear();
    // mList.invalidate();

    mBtnStop.setText( mContext.getResources().getString( R.string.stop_scan ) );
    mBleScanner = new BleScanner( this, mBTadapter );
    mBleScanner.startScan( uuid_str );
  }

  private void stopScan()
  {
    // TDLog.v( "BLE scan ==== stop");
    mBtnStop.setText( mContext.getResources().getString( R.string.start_scan ) );
    if ( mBleScanner != null ) {
      mBleScanner.stopScan();
      mBleScanner = null;
    }
  }

  public void notifyBleScan( BluetoothDevice device )
  {
    if ( device != null ) {
      mDevices.add( device );
      mAdapter.add( BleUtils.deviceToString( device ) );
      mAdapter.notifyDataSetChanged();
      // mList.invalidate();
    }
  }
    
}

