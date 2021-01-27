/* @file BleScanner.java
 *
 * @author marco cor
 * @date jan 2021
 *
 * @brief TopoDroid BLE devices scanner REQUIRES API-18
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 */
package com.topodroid.dev.bric;

// import com.topodroid.prefs.TDSetting;

import android.util.Log;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;

import java.lang.Runnable;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

// -----------------------------------------------------------------------------
public class BleScanner
{
  private static final long SCAN_PERIOD = 10000; // 10 secs

  private static boolean mScanning = false;

  final private BleScanDialog mParent;

  private ScanCallback mScanCallback;
  private BluetoothAdapter mBTAdapter;
  private BluetoothAdapter.LeScanCallback mLeScanCallback;

  private Handler mScanHandler = null;
  private Runnable mScanHandlerRunnable = null;

  // -----------------------------------------------

  BleScanner( BleScanDialog parent, BluetoothAdapter adapter )
  {
    mParent    = parent;
    mBTAdapter = adapter;
    // Log.v("BRIC", "ble scanner cstr");
  }

  /** 
   * @param uuid_str UUID string
   * @return true if scanning successfully started (or already ongoing)
   */
  boolean startScan( String uuid_str ) { return startScan( (uuid_str == null)? null : UUID.fromString( uuid_str ) ); }

  boolean startScan( UUID uuid )
  {
    // BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    // if ( adapter == null ) return false;

    if ( mScanning ) {
      Log.v("BRIC", "scanner already scanning");
      return true; // already scanning
    }
    mScanning = true;
    // Log.v("BRIC", "scanner start scan");

    // FIXME mParent.disconnectGatt();
    if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ) {
      BluetoothLeScanner scanner = mBTAdapter.getBluetoothLeScanner();

      mScanCallback = new ScanCallback() {
        @Override public void onScanResult( int type, ScanResult result ) 
        {
          // Log.v("BRIC", "on scan result");
          BluetoothDevice device = getDevice( result );
          if ( device != null ) setRemoteDevice( device );
          stopScan();
        }

        @Override public void onBatchScanResults( List< ScanResult > results )
        {
          // Log.v("BRIC", "on batch scan result");
          for ( ScanResult result : results ) {
            BluetoothDevice device = getDevice( result );
            if ( device != null ) setRemoteDevice( device );
            break;
          }
          stopScan();
        }

        @Override public void onScanFailed( int error )
        {
          Log.v("BRIC", "on scan fail");
          mParent.notifyBleScan( null );
        }
      };

      List< ScanFilter > filters = new ArrayList<>();
      ScanFilter.Builder builder = new ScanFilter.Builder();
      if ( uuid != null ) builder.setServiceUuid( new ParcelUuid( uuid ) );
        // .setDeviceAddress( address )
        // .setDeviceName( name )
      filters.add( builder.build() );

      ScanSettings settings = new ScanSettings.Builder()
        .setScanMode( ScanSettings.SCAN_MODE_BALANCED )
        // .setCallbackType( ScanSettings.CALLBACK_TYPE_ALL_MATCHES )
        // .setMatchMode( ScanSettings.MATCH_MODE_STICKY )
        .build();

      mScanHandler = new Handler();
      mScanHandlerRunnable = new Runnable() { public void run() { stopScan(); } };
      mScanHandler.postDelayed( mScanHandlerRunnable, SCAN_PERIOD );

      scanner.startScan( filters, settings, mScanCallback );
    } else {
      mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        // FIXME allocate device
        @Override
        public void onLeScan( BluetoothDevice device, int rssi, byte[] scanRecord ) 
        {  
          if ( device != null ) setRemoteDevice( device );
        }
      };
      mBTAdapter.startLeScan( mLeScanCallback ); 
    }
    return true;
  }

  private BluetoothDevice getDevice( ScanResult result )
  {
    int rssi = result.getRssi();
    // Log.v("BRIC", "device RSSI " + rssi );
    ScanRecord record = result.getScanRecord();
    if ( record != null ) {
      // Log.v("BRIC", "device name " + record.getDeviceName() );
      List< ParcelUuid > uuids = record.getServiceUuids();
      if ( uuids != null ) {
        for ( ParcelUuid uuid : uuids ) Log.v("BRIC", uuid.toString() );
      }
    }
    return result.getDevice();
  }

  void stopScan()
  {
    // Log.v("BRIC", "stop scan");
    if ( mScanHandler != null && mScanHandlerRunnable != null ) {
      mScanHandler.removeCallbacks( mScanHandlerRunnable );
    }
    if ( mScanning ) {
      // BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
      if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ) {
        BluetoothLeScanner scanner = mBTAdapter.getBluetoothLeScanner();
        if ( scanner != null ) scanner.stopScan( mScanCallback );
        mScanCallback = null;
      } else {
        mBTAdapter.stopLeScan( mLeScanCallback );
        mLeScanCallback = null;
      }
    }
    mScanning = false;
    mScanHandlerRunnable = null;
    mScanHandler = null;
  }  

  private void setRemoteDevice( final BluetoothDevice device )
  {
    // Log.v("BRIC", "set remote device " + device.getName() );
    mParent.notifyBleScan( device );
  }

}
