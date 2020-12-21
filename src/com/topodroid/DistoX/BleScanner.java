/* @file BleScanner.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid BLE devices scanner REQUIRES API-18
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.DistoX;

// import com.topodroid.prefs.TDSetting;

import android.util.Log;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;

import java.lang.Runnable;

import java.util.List;
import java.util.ArrayList;

// -----------------------------------------------------------------------------
class BleScanner
{
  private static final long SAP5_SCAN_PERIOD = 10000; // 10 secs

  private static boolean mScanning = false;

  final private DeviceActivity mParent;

  private ScanCallback mScanCallback;
  private BluetoothAdapter.LeScanCallback mLeScanCallback;

  private Handler mScanHandler = null;
  private Runnable mScanHandlerRunnable = null;

  // -----------------------------------------------

  BleScanner( DeviceActivity parent )
  {
    mParent = parent;
  }

  boolean startScan( /* String address */ )
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter == null ) return false;

    if ( mScanning ) return true; // already scanning
    mScanning = true;
    Log.v("DistoX-BLEX", "start scan");

    // FIXME mParent.disconnectGatt();
    if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ) {
      BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();

      mScanCallback = new ScanCallback() {
        @Override public void onScanResult( int type, ScanResult result ) 
        {
          setRemoteDevice( result.getDevice() );
          stopScan();
        }

        @Override public void onBatchScanResults( List< ScanResult > results )
        {
          for ( ScanResult result : results ) {
            setRemoteDevice( result.getDevice() );
            break;
          }
          stopScan();
        }

        @Override public void onScanFailed( int error )
        {
          TDToast.makeBad( R.string.ble_scan_failed );
        }
      };

      ScanFilter filter = new ScanFilter.Builder()
        .setServiceUuid( new ParcelUuid( BleConst.SAP5_SERVICE_UUID ) )
        // .setDeviceAddress( address )
        // .setDeviceName( name )
        .build();
      List< ScanFilter > filters = new ArrayList<>();
      filters.add( filter );

      ScanSettings settings = new ScanSettings.Builder()
        .setScanMode( ScanSettings.SCAN_MODE_LOW_POWER )
        .build();
      scanner.startScan( filters, settings, mScanCallback );
      mScanHandler = new Handler();
      mScanHandlerRunnable = new Runnable() { public void run() { stopScan(); } };
      mScanHandler.postDelayed( mScanHandlerRunnable, SAP5_SCAN_PERIOD );
    } else {
      mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        // FIXME allocate device
        @Override
        public void onLeScan( BluetoothDevice device, int rssi, byte[] scanRecord ) 
        {  
          setRemoteDevice( device );
        }
      };
      adapter.startLeScan( mLeScanCallback ); 
    }
    return true;
  }

  private void stopScan()
  {
    Log.v("DistoX-BLEX", "stop scan");
    if ( mScanHandler != null && mScanHandlerRunnable != null ) {
      mScanHandler.removeCallbacks( mScanHandlerRunnable );
    }
    if ( mScanning ) {
      BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
      if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ) {
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if ( scanner != null ) scanner.stopScan( mScanCallback );
        mScanCallback = null;
      } else {
        adapter.stopLeScan( mLeScanCallback );
        mLeScanCallback = null;
      }
    }
    mScanning = false;
    mScanHandlerRunnable = null;
    mScanHandler = null;
  }  

  private void setRemoteDevice( final BluetoothDevice device )
  {
    Log.v("DistoX-BLEX", "remote device " + device.getName() );
    mParent.runOnUiThread( new Runnable() { public void run() { mParent.addBleDevice( device ); } } );
  }

}
