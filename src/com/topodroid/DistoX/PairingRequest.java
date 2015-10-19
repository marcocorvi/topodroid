package com.topodroid.DistoX;

import java.nio.ByteBuffer;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

import android.bluetooth.BluetoothDevice;

import android.util.Log;

public class PairingRequest extends BroadcastReceiver{
  @Override
  public void onReceive(Context context, Intent intent){
    if (intent.getAction().equals("ACTION_PAIRING_REQUEST")) {
      BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
      Log.v("DistoX", "PAIRING: " + device.getName() + " " + device.getAddress() );
      try { 
        device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
        device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device, true);
        byte[] pin = ByteBuffer.allocate(4).putInt(0000).array();
        // byte[] pinBytes = BluetoothDevice.convertPinToBytes("0000");
        //Entering pin programmatically:  
        Method ms = device.getClass().getMethod("setPin", byte[].class);
        // Method ms = device.getClass().getMethod("setPasskey", int.class);
        ms.invoke( device, pin );
      } catch ( NoSuchMethodException e ) {
        Log.v("DistoX", "No Such method: " + e.getMessage() );
      } catch ( IllegalAccessException e ) {
        Log.v("DistoX", "Illegal access: " + e.getMessage() );
      } catch ( InvocationTargetException e ) {
        Log.v("DistoX", "Invocation target: " + e.getMessage() );
      }
    }
  }
}
