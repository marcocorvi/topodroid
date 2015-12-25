/* @file DeviceUtil.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device utility
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

// import android.app.Activity;
// import android.os.Bundle;
// import android.os.AsyncTask;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

// import android.widget.Toast;

import android.util.Log;

import android.bluetooth.BluetoothDevice;

public class DeviceUtil
{
  // FIXME PAIRING api-19
  // final static String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
  // final static String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
  // final static String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
  // final static int PAIRING_VARIANT_PIN = 0;

  static boolean isPaired( BluetoothDevice device )
  {
    if ( device == null ) return false;
    return device.getBondState() == BluetoothDevice.BOND_BONDED;
  }

  // @return 0: null device
  //         1:
  //         2: already paired
  //        -1: failed
  static int pairDevice( BluetoothDevice device )
  {
    if ( device == null ) return 0;
    int state = device.getBondState();
    if ( state == BluetoothDevice.BOND_BONDED ) { // already paired
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
    if ( state != BluetoothDevice.BOND_BONDED ) { // already not paired
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
  //   BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
  //   Log.v("DistoX", "PAIRING: " + device.getName() + " " + device.getAddress() );
  //   try { 
  //     device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
  //     Log.v("DistoX", "done setPairingConfirmation");
  //     // device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device, true);
  //     byte[] pin = ByteBuffer.allocate(4).putInt(0000).array();
  //     // byte[] pinBytes = BluetoothDevice.convertPinToBytes("0000");
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
}
