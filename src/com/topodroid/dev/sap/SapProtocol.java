/* @file SapProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SAP5 protocol REQUIRES API-18
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.dev.sap;

// import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
import com.topodroid.dev.TopoDroidProtocol;

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
class SapProtocol extends TopoDroidProtocol
{
  // private SapComm mComm; // UNUSED
  ArrayList< byte[] > mWriteBuffer;  // write buffer
  
  SapProtocol( SapComm comm, Device device, Context context )
  {
    super( device, context );
    // mComm   = comm;
    mWriteBuffer = new ArrayList< byte[] >();
    // TDLog.v( "SAP proto: cstr");
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

  // @param chrt   GATT write characteristic
  // @return number of bytes set into the write characteristic
  public byte[] handleWrite( )
  {
    // TDLog.v( "SAP proto: write - pending " + mWriteBuffer.size() );
    byte[] bytes = null;
    synchronized ( mWriteBuffer ) { // FIXME SYNCH_ON_NON_FINAL
      while ( ! mWriteBuffer.isEmpty() ) {
        bytes = mWriteBuffer.remove(0);
      }
    }
    return bytes;
  }

  // @param chrt   GATT read characteristic
  // @param bytes  return byte array
  public int handleRead( byte[] bytes )
  {
    // TDLog.v( "SAP proto: read bytes " + bytes.length );
    byte[] buffer = new byte[8];
    System.arraycopy( bytes, 0, buffer, 0, 8 );
    // ACKNOWLEDGMENT
    // byte[] ack = new byte[1];
    // ack[0] = (byte)( ( buffer[0] & 0x80 ) | 0x55 );
    // addToWriteBuffer( ack );
    return handlePacket( buffer );
  }

  // @param chrt   Sap Gatt characteristic
  public int handleReadNotify( BluetoothGattCharacteristic chrt )
  {
    return handleRead( chrt.getValue() );
  }

  public byte[] handleWriteNotify( BluetoothGattCharacteristic chrt )
  {
    return handleWrite( );
  }

}
