/* @file DeviceUtil.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid device utility
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.TDX.TDToast;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
// import java.util.Map;
// import java.util.HashMap;
// import java.util.List;
// import java.util.ArrayList;

// import android.app.Activity;
// import android.os.Bundle;

// import android.content.Context;
// import android.content.Intent;
// import android.content.IntentFilter;
// import android.content.BroadcastReceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class DeviceUtil
{
  public static final int ERROR = BluetoothDevice.ERROR;
  public static final int BOND_NONE    = BluetoothDevice.BOND_NONE;
  public static final int BOND_BONDED  = BluetoothDevice.BOND_BONDED;
  public static final int BOND_BONDING = BluetoothDevice.BOND_BONDING;

  public static final String ACTION_ACL_CONNECTED      = BluetoothDevice.ACTION_ACL_CONNECTED;
  public static final String ACTION_ACL_DISCONNECTED   = BluetoothDevice.ACTION_ACL_DISCONNECTED;
  public static final String ACTION_ACL_DISCONNECT_REQUESTED = BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED;
  // public static final String ACTION_FOUND              = BluetoothDevice.ACTION_FOUND;
  public static final String ACTION_BOND_STATE_CHANGED = BluetoothDevice.ACTION_BOND_STATE_CHANGED;
  // public static final String ACTION_PAIRING_REQUEST    = BluetoothDevice.ACTION_PAIRING_REQUEST;  // REQUIRES API-19

  // public static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;

  // public static final String EXTRA_DISCOVERABLE_DURATION  = BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION;
  // public static final String ACTION_DISCOVERY_STARTED     = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
  // public static final String ACTION_DISCOVERY_FINISHED    = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
  // public static final String ACTION_REQUEST_DISCOVERABLE  = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
  public static final String ACTION_REQUEST_ENABLE        = BluetoothAdapter.ACTION_REQUEST_ENABLE;

  public static final String EXTRA_DEVICE              = BluetoothDevice.EXTRA_DEVICE;
  public static final String EXTRA_BOND_STATE          = BluetoothDevice.EXTRA_BOND_STATE;
  public static final String EXTRA_PREVIOUS_BOND_STATE = BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE;

  // ---------------------------------------------------------------------------------

  // static byte[] convertPinToBytes( String pin ) { return BluetoothDevice.convertPinToBytes(pin); }

  /** @return true if the BT is enabled
   */
  public static boolean isAdapterEnabled() { return BluetoothAdapter.getDefaultAdapter().isEnabled(); }

  /** @return true if there is a BT adapter
   */
  public static boolean hasAdapter() { return BluetoothAdapter.getDefaultAdapter() != null; }

  /** @return the set of bonded devices
   */
  public static Set< BluetoothDevice > getBondedDevices() 
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter == null ) return null;
    try {
      return adapter.getBondedDevices();
    } catch ( SecurityException e ) { // FIXME ANDROID-12
      TDLog.Error("SECURITY get bonded devices " + e.getMessage() );
      // TDToast.makeBad("Security error: get bonded devices");
    }
    return null;
  }

  /** @return the remote device with a given address
   * @param address   device address
   */
  public static BluetoothDevice getRemoteDevice( String address ) 
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter == null ) return null;
    return adapter.getRemoteDevice( address );
  }

  /** start BT devices discovery
   */
  public static void startDiscovery()
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter != null ) {
      try {
        adapter.startDiscovery();
      } catch ( SecurityException e ) {
        TDLog.Error("SECURITY start discovery " + e.getMessage() );
        // TDToast.makeBad("Security error: start discovery");
      }
    }
  }

  /** cancel BT devices discovery
   */
  public static void cancelDiscovery()
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter != null ) {
      try {
        adapter.cancelDiscovery();
      } catch ( SecurityException e ) {
        TDLog.Error("SECURITY cancel discovery " + e.getMessage() );
        // TDToast.makeBad("Security error: cancel discovery");
      }
    }
  }

  // static void ensureDiscoverable()
  // {
  //   BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
  //   if ( adapter == null ) return;
  //   if ( adapter.getScanMode() != SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
  //     Intent discoverIntent = new Intent( ACTION_REQUEST_DISCOVERABLE );
  //     discoverIntent.putExtra( EXTRA_DISCOVERABLE_DURATION, 300 );
  //     startActivity( discoverIntent ); // FIXME was in DeviceList.java
  //   }
  // }

  // FIXME PAIRING api-19
  // final static String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
  // final static String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
  // final static String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
  // final static int PAIRING_VARIANT_PIN = 0;

  /** @return true if the remote device is paired
   * @param device  remote device
   */
  public static boolean isPaired( BluetoothDevice device )
  {
    if ( device != null ) {
      try {
        return device.getBondState() == BOND_BONDED;
      } catch (SecurityException e) {
        TDLog.Error("SECURITY is paired " + e.getMessage());
        // TDToast.makeBad("Security error: is paired");
      }
    }
    return false;
  }

  /** pair android with a remote device
   * @param device  remote device
   * @return 0: null device
   *         1:
   *         2: already paired
   *        -1: failed
   */
  public static int pairDevice( BluetoothDevice device )
  {
    if ( device == null ) return 0;
    try {
      if ( device.getBondState() == BOND_BONDED ) { // already paired
        return 2;
      }
    } catch (SecurityException e) {
      TDLog.Error("SECURITY pair device " + e.getMessage());
      // TDToast.makeBad("Security error: pair device");
      return -1;
    }
    try {
      Method m = device.getClass().getMethod( "createBond", (Class[]) null );
      m.invoke( device, (Object[]) null );
    } catch ( Exception e ) {
      TDLog.Error("Error create bond " + e.getMessage());
      return -1;
    }
    return 1;
  }

  /** unpair android from a remote device
   * @param device  remote device
   * @return ??
   */
  public static int unpairDevice( BluetoothDevice device )
  {
    if ( device == null ) return 0;
    try {
      if (device.getBondState() != BOND_BONDED) { // already not paired
        return 2;
      }
    } catch ( SecurityException e ) {
      TDLog.Error( "SECURITY unpair device " + e.getMessage() );
      // TDToast.makeBad("Security error: unpair device");
    }

    try {
      Method m = device.getClass().getMethod( "removeBond", (Class[]) null );
      m.invoke( device, (Object[]) null );
    } catch ( Exception e ) {
      TDLog.Error("Error remove bond " + e.getMessage());
      return -1;
    }
    return 1;
  }

  // Intent pairing_intent = new Intent( ACTION_PAIRING_REQUEST );
  // pairing_intent.putExtra( EXTRA_DEVICE, device )
  // pairing_intent.putExtra( EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN );
  // pairing_intent.setFlag( Intent.FLAG_ACTIVITY_NEW_TASK );
  // startActivityForResult( pairing_intent, 0 );


  // public static void bind2Device( Intent intent )
  // {
  //   BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
  //   // TDLog.v( "PAIRING: " + device.getName() + " " + device.getAddress() );
  //   try { 
  //     device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
  //     // TDLog.v( "done setPairingConfirmation");
  //     // device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device, true);
  //     byte[] pin = ByteBuffer.allocate(4).putInt(0000).array();
  //     // byte[] pinBytes = convertPinToBytes("0000");
  //     //Entering pin programmatically:  
  //     Method ms = device.getClass().getMethod("setPin", byte[].class);
  //     // Method ms = device.getClass().getMethod("setPasskey", int.class);
  //     ms.invoke( device, pin );
  //     // TDLog.v( "done setPin");
  //     Class[] classes3 = new Class[ 0 ];
  //     Method m_createBond = device.getClass().getMethod( "createBond", classes3 );
  //     m_createBond.invoke( device );
  //     // TDLog.v( "done createBond");
  //   } catch ( NoSuchMethodException e ) {
  //     TDLog.Error "No Such method: " + e.getMessage() );
  //   } catch ( IllegalAccessException e ) {
  //     TDLog.Error "Illegal access: " + e.getMessage() );
  //   } catch ( InvocationTargetException e ) {
  //     TDLog.Error "Invocation target: " + e.getMessage() );
  //   }
  // }

  /** bind a remote device
   * @param device  remote device
   */
  public static void bindDevice( BluetoothDevice device )
  {
    // TDLog.Log( TDLog.LOG_COMM, " bind device ... " + ((device==null)? "null" : device.getAddress()) );
    if ( device == null ) return;

    String PIN_CODE = "0000";
    byte[] pin = PIN_CODE.getBytes();
    // byte[] pin = new byte[] { 0, 0, 0, 0 };
    try {
      Class[] classes2 = new Class[ 1 ];
      classes2[0] = byte[].class;
      Method m_setPin = device.getClass().getMethod("setPin", classes2 );
      m_setPin.invoke( device, pin );
      Class[] classes3 = new Class[ 0 ];
      Method m_createBond = device.getClass().getMethod( "createBond", classes3 );
      m_createBond.invoke( device );
    } catch ( NoSuchMethodException e ) {
      TDLog.Error( "Failed to set PIN: no method " + e.getMessage() );
    } catch ( InvocationTargetException e ) {
      TDLog.Error( "Failed to set PIN: invoke " + e.getMessage() );
    } catch (IllegalAccessException e ) {
      TDLog.Error( "Failed to set PIN: illegal access " + e.getMessage() );
    }
  }

  /** check that a remote device is paired
   * @param  address  remote device address
   */
  public static void checkPairing( String address )
  {
    if ( TDSetting.mAutoPair ) { // try to get the system ask for the PIN
      // TDLog.Log( TDLog.LOG_COMM, " check pairing " + address );
      BluetoothDevice btDevice = getRemoteDevice( address );
      // TDLog.Log( TDLog.LOG_BT, "auto-pairing remote device " + btDevice.getAddress() + " status " + btDevice.getBondState() );
      if ( ! isPaired( btDevice ) ) {
        pairDevice( btDevice );
        bindDevice( btDevice );
        for (int c=0; c<TDSetting.mConnectSocketDelay; ++c ) {
          if ( isPaired( btDevice ) ) break;
          TDUtil.slowDown( 100 ); // Thread.yield();
        }
      }
    }
  }

}
