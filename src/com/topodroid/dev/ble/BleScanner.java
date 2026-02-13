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
  private BluetoothLeScanner mBleScanner;
  private ScanSettings       mScanSettings;
  private ScanCallback       mCallback;
  private boolean mScanning;


  // public ScanCallback defaultCallback() 
  // { 
  //   ScanCallback cb = new BleScanCallback( null, this );
  //   return cb;
  // }

  public BleScanner( Context ctx )
  {
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

  public void startBleScan( final ScanCallback cb )
  {
    if ( mScanning ) return;
    mScanning = true;
    mCallback = cb;
    Thread counter = new Thread() {
      @Override public void run()
      {
        for ( int k=0; k<10; ++k ) {
          TDLog.v("waiting ... " + k );
          TDUtil.yieldDown( 1000 );
        }
        stopBleScan( );
      }
    };
    counter.start();
    mBleScanner.startScan( null, mScanSettings, mCallback );
  }
  // ScanCallback must implement
  //  

  public void stopBleScan( )
  {
    if ( ! mScanning ) return;
    mScanning = false;
    mBleScanner.stopScan( mCallback );
  }

} 



