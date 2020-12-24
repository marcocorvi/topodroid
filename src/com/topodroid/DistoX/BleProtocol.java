/* @file BleProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid BLE devices protocol REQUIRES API-18
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;

import android.util.Log;

// import android.os.Handler;
import android.content.Context;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothAdapter;
// import android.bluetooth.BluetoothGatt;
// import android.bluetooth.BluetoothGattService;
// import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.ArrayList;
import java.util.Arrays;

// -----------------------------------------------------------------------------
class BleProtocol extends TopoDroidProtocol
{
  private BleComm mComm;
  ArrayList< byte[] > mWriteBuffer;  // write buffer
  
  BleProtocol( BleComm comm, Device device, Context context )
  {
    super( device, context );
    mComm   = comm;
    mWriteBuffer = new ArrayList< byte[] >();
    // Log.v("DistoX-BLEX", "new BLE proto");
  }

  public void addToWriteBuffer( byte[] bytes )
  {
    int pos =  0;
    int len = ( 20 > bytes.length )? bytes.length : 20;
    while ( pos < bytes.length ) {
      mWriteBuffer.add( Arrays.copyOfRange( bytes, pos, len ) );
      pos += len;
      len = ( len + 20 <= bytes.length )? len+20 : bytes.length;
    }
  }

  // @param crtr   GATT write characteristic
  // @return number of bytes set into the write characteristic
  public int handleWrite( BluetoothGattCharacteristic chrt )
  {
    Log.v("DistoX-BLEX", "proto write - pending " + mWriteBuffer.size() );
    byte[] bytes = null;
    synchronized ( mWriteBuffer ) {
      while ( ! mWriteBuffer.isEmpty() ) {
        bytes = mWriteBuffer.remove(0);
      }
    }
    if ( bytes != null && bytes.length > 0 ) {
      chrt.setValue( bytes );
      return bytes.length;
    }
    return 0;
  }

  // @param crtr   GATT read characteristic
  public int handleRead( BluetoothGattCharacteristic chrt )
  {
    byte[] bytes = chrt.getValue();
    // Log.v("DistoX-BLEX", "proto read bytes " + bytes.length );
    byte[] buffer = new byte[8];
    System.arraycopy( bytes, 0, buffer, 0, 8 );
    return handlePacket( buffer );
  }

  // @param chrt   Ble Gatt characteristic
  // @param read   whether the chrt is read or write
  public int handleNotify( BluetoothGattCharacteristic chrt, boolean read )
  {
    // Log.v("DistoX-BLEX", "proto notify: read " + read );
    if ( read ) {
      return handleRead( chrt );
    } 
    return handleWrite( chrt );
  }

}
