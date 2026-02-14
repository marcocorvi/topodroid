/* @file BleScanner.java
 *
 * @author marco corvi
 * @date jan 2026
 *
 * @brief BLE device scanner
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.Timer;
import com.topodroid.TDX.DeviceActivity;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

import android.content.Context;
import android.os.ParcelUuid;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;

import java.util.List;

public class BleScanner
{
  private Context mContext;
  private DeviceActivity     mParent;
  private BluetoothLeScanner mBleScanner;
  private ScanSettings       mScanSettings;
  private BleScanCallback    mCallback;
  private boolean mScanning;
  private Timer mTimer = null;

  // public ScanCallback defaultCallback() 
  // { 
  //   ScanCallback cb = new BleScanCallback( null, this );
  //   return cb;
  // }

  public BleScanner( Context ctx, DeviceActivity parent )
  {
    mContext = ctx;
    mParent  = parent;
    BluetoothManager bt_manager = (BluetoothManager)ctx.getSystemService( Context.BLUETOOTH_SERVICE );
    BluetoothAdapter bt_adapter = bt_manager.getAdapter(); 
    mBleScanner = bt_adapter.getBluetoothLeScanner();
    mScanSettings = (new ScanSettings.Builder())
                 .setMatchMode( ScanSettings.MATCH_MODE_STICKY ) //  MATCH_MODE_AGGRESSIVE 
                 .setCallbackType( ScanSettings.CALLBACK_TYPE_ALL_MATCHES ) // CALLBACK_TYPE_FIRST_MATCH or CALLBACK_TYPE_MATCH_LOST
                 .setNumOfMatches( ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT ) //  MATCH_NUM_FEW_ADVERTISEMENT or MATCH_NUM_MAX_ADVERTISEMENT
                 .setReportDelay( 0 )
                 .setScanMode( ScanSettings.SCAN_MODE_BALANCED ) //  SCAN_MODE_LOW_POWER or SCAN_MODE_LOW_LATENCY.
                 .build();
    mScanning = false;
  }

  public void startBleScan( final BleScanCallback cb )
  {
    if ( mScanning ) return;
    TDToast.makeLong( R.string.scanning );
    TDLog.v("BLE scanner start");
    mScanning = true;
    mCallback = cb;
    mCallback.resetCount();
    mTimer = new Timer( 5000, new Runnable() {
      @Override public void run()
      {
        stopBleScan( );
      }
    } );
    mBleScanner.startScan( null, mScanSettings, mCallback );
  }
  // ScanCallback must implement
  //  

  public void stopBleScan( )
  {
    if ( ! mScanning ) return;
    TDLog.v("BLE scanner stop");
    mScanning = false;
    mBleScanner.stopScan( mCallback );
    final String msg = String.format( mContext.getResources().getString( R.string.scan_result ), mCallback.getNovel() );
    mParent.runOnUiThread( new Runnable() { public void run() { 
      mParent.setBtScanning( false );
      TDToast.make( msg ); 
    } } );
  }

} 



