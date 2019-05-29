/* @file TopoDroidComm.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid-DistoX BlueTooth communication 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

import java.util.List;
// import java.util.concurrent.atomic.AtomicInteger; // FIXME_ATOMIC_INT

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

// import android.content.Context;
// import android.content.Intent;
// import android.content.IntentFilter;
// import android.content.BroadcastReceiver;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;

class SAPComm extends TopoDroidComm
{
  private BluetoothDevice mBTDevice;


  SAPComm( TopoDroidApp app )
  {
    super( app );
    mBTDevice = null;
  }

  // void resume() { }

  // void suspend() { }

  // protected void cancelCommThread()

  // -------------------------------------------------------- 
  // PROTOCOL

  // protected void closeProtocol()

  // protected boolean startCommThread( int to_read, Handler /* ILister */ lister ) 

  // void disconnectRemoteDevice( )

  // protected boolean checkCommThreadNull( String msg ) { return ( mCommThread == null ); }

  // protected boolean sendCommand( int cmd )

  // byte[] readMemory( String address, int addr ) { return null; }

  // ------------------------------------------------------------------------------------
  // CONTINUOUS DATA DOWNLOAD

  // boolean connectDevice( String address, Handler /* ILister */ lister )

  // void disconnect() { }

  // -------------------------------------------------------------------------------------
  // ON-DEMAND DATA DOWNLOAD

  // int downloadData( String address, Handler /* ILister */ lister )

}
