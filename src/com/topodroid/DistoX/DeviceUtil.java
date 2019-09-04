/* @file DeviceUtil.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device utility
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

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

// import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

class DeviceUtil
{
  static final int ERROR = BluetoothDevice.ERROR;
  static final int BOND_NONE    = BluetoothDevice.BOND_NONE;
  static final int BOND_BONDED  = BluetoothDevice.BOND_BONDED;
  static final int BOND_BONDING = BluetoothDevice.BOND_BONDING;

  static final String ACTION_ACL_CONNECTED      = BluetoothDevice.ACTION_ACL_CONNECTED;
  static final String ACTION_ACL_DISCONNECTED   = BluetoothDevice.ACTION_ACL_DISCONNECTED;
  static final String ACTION_ACL_DISCONNECT_REQUESTED = BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED;
  static final String ACTION_FOUND              = BluetoothDevice.ACTION_FOUND;
  static final String ACTION_BOND_STATE_CHANGED = BluetoothDevice.ACTION_BOND_STATE_CHANGED;
  static final String ACTION_PAIRING_REQUEST    = BluetoothDevice.ACTION_PAIRING_REQUEST;  // REQUIRES API-19

  static final int SCAN_MODE_CONNECTABLE_DISCOVERABLE = BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;

  static final String EXTRA_DISCOVERABLE_DURATION  = BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION;
  static final String ACTION_DISCOVERY_STARTED     = BluetoothAdapter.ACTION_DISCOVERY_STARTED;
  static final String ACTION_DISCOVERY_FINISHED    = BluetoothAdapter.ACTION_DISCOVERY_FINISHED;
  static final String ACTION_REQUEST_DISCOVERABLE  = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
  static final String ACTION_REQUEST_ENABLE        = BluetoothAdapter.ACTION_REQUEST_ENABLE;

  static final String EXTRA_DEVICE              = BluetoothDevice.EXTRA_DEVICE;
  static final String EXTRA_BOND_STATE          = BluetoothDevice.EXTRA_BOND_STATE;
  static final String EXTRA_PREVIOUS_BOND_STATE = BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE;

  // ---------------------------------------------------------------------------------

  // static byte[] convertPinToBytes( String pin ) { return BluetoothDevice.convertPinToBytes(pin); }

  static boolean isAdapterEnabled() { return BluetoothAdapter.getDefaultAdapter().isEnabled(); }

static boolean hasAdapter() { return BluetoothAdapter.getDefaultAdapter() != null; }

  static Set< BluetoothDevice > getBondedDevices() 
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter == null ) return null;
    return adapter.getBondedDevices();
  }

  static BluetoothDevice getRemoteDevice( String address ) 
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter == null ) return null;
    return adapter.getRemoteDevice( address );
  }

  static void startDiscovery()
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter != null ) adapter.startDiscovery();
  }

  static void cancelDiscovery()
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if ( adapter != null ) adapter.cancelDiscovery();
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

  static boolean isPaired( BluetoothDevice device )
  {
    return ( device != null ) && ( device.getBondState() == BOND_BONDED );
  }

  // @return 0: null device
  //         1:
  //         2: already paired
  //        -1: failed
  static int pairDevice( BluetoothDevice device )
  {
    if ( device == null ) return 0;
    int state = device.getBondState();
    if ( state == BOND_BONDED ) { // already paired
      return 2;
    }
    try {
      Method m = device.getClass().getMethod( "createBond", (Class[]) null );
      m.invoke( device, (Object[]) null );
    } catch ( Exception e ) {
      return -1;
    }
    return 1;
  }

  static int unpairDevice( BluetoothDevice device )
  {
    if ( device == null ) return 0;
    int state = device.getBondState();
    if ( state != BOND_BONDED ) { // already not paired
      return 2;
    }
    try {
      Method m = device.getClass().getMethod( "removeBond", (Class[]) null );
      m.invoke( device, (Object[]) null );
    } catch ( Exception e ) {
      return -1;
    }
    return 1;
  }

  // Intent paiting_intent = new Intent( ACTION_PAIRING_REQUEST );
  // pairing_intent.putExtra( EXTRA_DEVICE, device )
  // pairing_intent.putExtra( EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN );
  // pairing_intent.setFlag( Intent.FLAG_ACTIVITY_NEW_TASK );
  // startActivityForResult( pairing_intent, 0 );


  // static void bind2Device( Intent intent )
  // {
  //   BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
  //   Log.v("DistoX", "PAIRING: " + device.getName() + " " + device.getAddress() );
  //   try { 
  //     device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
  //     Log.v("DistoX", "done setPairingConfirmation");
  //     // device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device, true);
  //     byte[] pin = ByteBuffer.allocate(4).putInt(0000).array();
  //     // byte[] pinBytes = convertPinToBytes("0000");
  //     //Entering pin programmatically:  
  //     Method ms = device.getClass().getMethod("setPin", byte[].class);
  //     // Method ms = device.getClass().getMethod("setPasskey", int.class);
  //     ms.invoke( device, pin );
  //     Log.v("DistoX", "done setPin");
  //     Class[] classes3 = new Class[ 0 ];
  //     Method m_createBond = device.getClass().getMethod( "createBond", classes3 );
  //     m_createBond.invoke( device );
  //     Log.v("DistoX", "done createBond");
  //   } catch ( NoSuchMethodException e ) {
  //     Log.v("DistoX", "No Such method: " + e.getMessage() );
  //   } catch ( IllegalAccessException e ) {
  //     Log.v("DistoX", "Illegal access: " + e.getMessage() );
  //   } catch ( InvocationTargetException e ) {
  //     Log.v("DistoX", "Invocation target: " + e.getMessage() );
  //   }
  // }

  static void bindDevice( BluetoothDevice device )
  {
    TDLog.Log( TDLog.LOG_COMM, " bind device ..." );
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

  static void checkPairing( String address )
  {
    if ( TDSetting.mAutoPair ) { // try to get the system ask for the PIN
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
