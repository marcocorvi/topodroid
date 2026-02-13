/* @file DiscoXComm.java
 *
 * @author marco corvi
 * @date nov 2011
 * revised for DiscoX 
 *
 * @brief TopoDroid DiscoX communication REQUIRES API-18
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * TopoDroid implementation of BLE callback follows the guidelines of 
 *   Chee Yi Ong,
 *   "The ultimate guide to Android bluetooth low energy"
 *   May 15, 2020
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.dev.discox;

// import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDUtil;
// // import com.topodroid.prefs.TDSetting;
// import com.topodroid.TDX.TDInstance;
// import com.topodroid.TDX.ListerHandler;
// // import com.topodroid.TDX.DataDownloader;
import com.topodroid.TDX.TopoDroidApp;
// // import com.topodroid.TDX.TDToast;
// import com.topodroid.dev.ConnectionState;
// import com.topodroid.dev.DataType;
import com.topodroid.dev.Device;
// import com.topodroid.dev.TopoDroidComm;
// import com.topodroid.dev.ble.BleUtils;
// import com.topodroid.dev.ble.BleComm;
// // import com.topodroid.dev.ble.BleChrtChanged;
// import com.topodroid.dev.ble.BleCallback;
// import com.topodroid.dev.ble.BleOperation;
// import com.topodroid.dev.ble.BleOpConnect;
// import com.topodroid.dev.ble.BleOpDisconnect;
// import com.topodroid.dev.ble.BleOpChrtRead;
// import com.topodroid.dev.ble.BleOpChrtWrite;

import com.topodroid.dev.sap.SapComm;

// // import android.os.Handler;
// // import android.os.Looper;
// // import android.os.Build;
import android.content.Context;
// 
import android.bluetooth.BluetoothDevice;
// // import android.bluetooth.BluetoothAdapter;
// // import android.bluetooth.BluetoothProfile;
// import android.bluetooth.BluetoothGatt;
// import android.bluetooth.BluetoothGattService;
// import android.bluetooth.BluetoothGattDescriptor;
// // import android.bluetooth.BluetoothGattCallback;
// import android.bluetooth.BluetoothGattCharacteristic;
// 
// import java.util.UUID;
// import java.util.concurrent.ConcurrentLinkedQueue;

// -----------------------------------------------------------------------------
public class DiscoXComm extends SapComm
{
  // -----------------------------------------------
  // BluetoothAdapter   mAdapter;
  // private BluetoothGatt mGatt = null;
  // BluetoothGattCharacteristic mWriteChrt;

  // private UUID mServiceUUID;
  // private UUID mReadUUID;
  // private UUID mWriteUUID;
  // private UUID mNotifyUUID;

  // private ConcurrentLinkedQueue< BleOperation > mOps;
  // private Context         mContext;
  // private String          mRemoteAddress;
  // private BluetoothDevice mRemoteBtDevice;
  // private BleCallback mCallback;
  // private SapProtocol mSapProto;
  // // private int mDataType = DataType.DATA_SHOT;
  // private int mDataType;

  // private ListerHandler mLister;
  // private int mConnectionMode = -1;
  // private boolean mDisconnecting = false;
  // private BleOperation mPendingOp = null;

  // BluetoothGattCharacteristic mReadChrt  = null;
  // BluetoothGattCharacteristic mWriteChrt = null;
  // private boolean mReadInitialized  = false;
  // private boolean mWriteInitialized = false;

  // BluetoothGattCharacteristic getReadChrt() { return mReadChrt; }
  // BluetoothGattCharacteristic getWriteChrt() { return mWriteChrt; }

  /** cstr
   * @param app        application
   * @param address    SAP address
   * @param bt_device  BT (SAP) device
   */
  public DiscoXComm( TopoDroidApp app, String address, BluetoothDevice bt_device ) 
  {
    super( app, address, bt_device, 6 ); // model SAP6
  }

  // void setRemoteDevice( BluetoothDevice device ) 
  // { 
  //   // TDLog.v( "SAP comm: set remote " + device.getAddress() );
  //   mRemoteBtDevice = device;
  // }

  /** create the protocol
   * @param device   BT device
   */
  protected void createProtocol( Device device )
  {
    mSapProto = new DiscoXProtocol( this, device, mContext );
  }

  // -------------------------------------------------------------
  // everything with super class
}
