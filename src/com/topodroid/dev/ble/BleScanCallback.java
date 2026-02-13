/* @file BleScanCallback.java
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
import com.topodroid.TDX.DeviceActivity;

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

public class BleScanCallback extends ScanCallback
{
  DeviceActivity mParent;
  BleScanner mScanner;

  public BleScanCallback( DeviceActivity parent, BleScanner scanner )
  {
    mParent  = parent;
    mScanner = scanner;
  }

  public void onBatchScanResults(List<ScanResult> results) 
  { 
    for ( ScanResult result : results ) onScanResult( ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result );
  }

  public void onScanFailed( int errorCode )
  {
    TDLog.v("BLE Scan failed with error " + errorCode );
    switch ( errorCode ) {
      case 1: TDLog.v("SCAN_FAILED_ALREADY_STARTED"); break;
      case 2: TDLog.v("SCAN_FAILED_APPLICATION_REGISTRATION_FAILED"); break;
      case 4: TDLog.v("SCAN_FAILED_FEATURE_UNSUPPORTED"); break;
      case 3: TDLog.v("SCAN_FAILED_INTERNAL_ERROR"); break;
    }
    mScanner.stopBleScan();
  }

  // callbackType's CALLBACK_TYPE_ALL_MATCHES, CALLBACK_TYPE_FIRST_MATCH, CALLBACK_TYPE_MATCH_LOST
  public void onScanResult( int callbackType, ScanResult result ) 
  {
    BluetoothDevice dev = result.getDevice();
    ScanRecord rec = result.getScanRecord();
    List< ParcelUuid > uuids = rec.getServiceUuids();
    TDLog.v("Scan result " + dev.getName() + " addr " + dev.getAddress() );
    for ( ParcelUuid uuid : uuids ) TDLog.v("uuid " + uuid.toString() );
    if ( mParent != null ) {
      mParent.notifyScanResult( result );
    }
  }
}

