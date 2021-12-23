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
package com.topodroid.dev.ble;

// import com.topodroid.prefs.TDSetting;
import com.topodroid.utils.TDLog;
import com.topodroid.DistoX.TDandroid;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

// import android.os.Build;
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
  private BluetoothAdapter.LeScanCallback mLeScanCallback; // min API-21

  private Handler mScanHandler = null;
  private Runnable mScanHandlerRunnable = null;

  // -----------------------------------------------

  /** cstr
   * @param parent   BT low-energy scan dialog
   * @param adapter  BT adapter
   */
  BleScanner( BleScanDialog parent, BluetoothAdapter adapter )
  {
    mParent    = parent;
    mBTAdapter = adapter;
    // TDLog.v( "BLE scanner cstr");
  }

  /** start a BT scan (if not already scanning)
   * @param uuid_str UUID string
   * @return true if scanning successfully started (or already ongoing)
   */
  boolean startScan( String uuid_str ) { return startScan( (uuid_str == null)? null : UUID.fromString( uuid_str ) ); }

  /** start a BT scan (if not already scanning)
   * @param uuid  UUID 
   * @return true if scanning successfully started (or already ongoing) - always return true
   */
  boolean startScan( UUID uuid )
  {
    // BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    // if ( adapter == null ) return false;

    if ( mScanning ) {
      TDLog.Error("BLE scanner already scanning");
      return true; // already scanning
    }
    mScanning = true;
    // TDLog.v( "BLE scanner start scan");

    // FIXME mParent.disconnectGatt();
    if ( TDandroid.AT_LEAST_API_21 ) { // > Build.VERSION_CODES.KITKAT )
      BluetoothLeScanner scanner = mBTAdapter.getBluetoothLeScanner();

      mScanCallback = new ScanCallback() {
        @Override public void onScanResult( int type, ScanResult result ) 
        {
          // TDLog.v( "BLE scanner on scan result");
          BluetoothDevice device = getDevice( result );
          if ( device != null ) setRemoteDevice( device );
          stopScan();
        }

        @Override public void onBatchScanResults( List< ScanResult > results )
        {
          // TDLog.v( "BLE scanner on batch scan result");
          for ( ScanResult result : results ) {
            BluetoothDevice device = getDevice( result );
            if ( device != null ) setRemoteDevice( device );
            break;
          }
          stopScan();
        }

        @Override public void onScanFailed( int error )
        {
          TDLog.Error("BLE scanner on scan fail");
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

  /** @return the BT device from a scan-result (null for API < 20 )
   * @param result   scan result
   * @note called only on API-21 or above
   */
  private BluetoothDevice getDevice( ScanResult result )
  {
    if ( TDandroid.AT_LEAST_API_21 ) {
      // TDLog.v( "BLE scanner device RSSI " + result.getRssi() );
      ScanRecord record = result.getScanRecord();
      if ( record != null ) {
        // TDLog.v( "BLE scanner device name " + record.getDeviceName() );
        List< ParcelUuid > uuids = record.getServiceUuids();
        if ( uuids != null ) {
          for ( ParcelUuid uuid : uuids ) TDLog.v( "BLE scanner uuid " + uuid.toString() );
        }
      }
      return result.getDevice();
    }
    return null;
  }

  /** finish BT scan
   */
  void stopScan()
  {
    // TDLog.v( "BLE scanner stop scan");
    if ( mScanHandler != null && mScanHandlerRunnable != null ) {
      mScanHandler.removeCallbacks( mScanHandlerRunnable );
    }
    if ( mScanning ) {
      // BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
      if ( TDandroid.AT_LEAST_API_21 ) { // > Build.VERSION_CODES.KITKAT )
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

  /** set the reote BT device: tell the device to the parent
   * @param device   BT device
   */
  private void setRemoteDevice( final BluetoothDevice device )
  {
    // TDLog.v( "BLE scanner set remote device " + device.getName() );
    mParent.notifyBleScan( device );
  }

}
