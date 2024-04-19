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

import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
import com.topodroid.dev.DataType;
import com.topodroid.dev.TopoDroidProtocol;

// import android.os.Handler;
import android.content.Context;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothAdapter;
// import android.bluetooth.BluetoothGatt;
// import android.bluetooth.BluetoothGattService;
// import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattCharacteristic;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

// -----------------------------------------------------------------------------
class SapProtocol extends TopoDroidProtocol
{
  // private SapComm mComm; // UNUSED
  ArrayList< byte[] > mWriteBuffer;  // write buffer
  
  /** cstr
   * @param comm    communication class
   * @param device  BT device
   * @param context context
   */
  SapProtocol( SapComm comm, Device device, Context context )
  {
    super( device, context );
    // mComm   = comm;
    mWriteBuffer = new ArrayList< byte[] >();
    // TDLog.v( "SAP proto: cstr");
  }

  /** add an array of bytes to the write buffer
   * @param bytes   array of bytes to add
   */
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

  /** remove a byte array from the write queue
   */
  public byte[] handleWrite( )
  {
    // TDLog.v( "SAP proto: write - pending " + mWriteBuffer.size() );
    byte[] bytes = null;
    synchronized ( mWriteBuffer ) { // FIXME SYNCH_ON_NON_FINAL
      if ( ! mWriteBuffer.isEmpty() ) { // FIXME this was a while-loop
        bytes = mWriteBuffer.remove(0);
      }
    }
    return bytes;
  }

  /** handle the reading of a received array of bytes
   * @param bytes  byte array that has been received
   * @return result code (packet type or whatever - DataType.PACKET_NONE on error)
   */
  public int handleRead( byte[] bytes )
  {
    if ( Device.isSap5( mDeviceType ) ) {
      // TDLog.v( "SAP proto: read bytes " + bytes.length );
      if ( bytes.length != 8 ) return DataType.PACKET_NONE; 
      byte[] buffer = new byte[8];
      System.arraycopy( bytes, 0, buffer, 0, 8 );

      // FIX SAP5 bug: 2023-01-05 Phil Underwood on SAP list:
      // Looks like calculation of high byte for distoX protocol is incorrect - marks bit 16 when distance > 32.676m, should be when > 65.535m
      if ( TDSetting.mSap5Bit16Bug ) {
        if ( (buffer[2] & 0x80) == 0x80 ) buffer[0] &= 0xbf; // clear 0x40
      }
      return handlePacket( buffer );
    } else if ( Device.isSap6( mDeviceType ) ) { // FIXME_SAP6
      {
        StringBuilder sb = new StringBuilder();
        for ( int k=0; k<bytes.length; ++k ) sb.append( String.format(" %02x", bytes[k] ) );
        TDLog.v( "SAP6 proto: read " + bytes.length + " bytes:" + sb.toString() );
      }
      if ( bytes.length != 17 ) return DataType.PACKET_NONE;
      byte[] buffer = new byte[16];
      System.arraycopy( bytes, 1, buffer, 0, 16 );

      // ACKNOWLEDGMENT
      byte[] ack = new byte[1];
      ack[0] = (byte)( bytes[0] + 0x55 ); // SapConst.SAP_ACK
      addToWriteBuffer( ack );

      // DATA
      ByteBuffer byte_buffer = ByteBuffer.wrap( buffer ).order(ByteOrder.LITTLE_ENDIAN);
      FloatBuffer float_buffer = byte_buffer.asFloatBuffer();
      mBearing  = float_buffer.get(0); // decimal degrees
      mClino    = float_buffer.get(1);
      mRoll     = float_buffer.get(2);
      mDistance = float_buffer.get(3); // meters
      TDLog.v( "SAP6 proto data: " + String.format(Locale.US, "%2f %2f %2f", mDistance, mBearing, mClino ) );
      return DataType.PACKET_DATA;
    }
    return DataType.PACKET_NONE;
  }

  /** handle a notification on the GATT READ characteristics
   * @param chrt   Sap Gatt characteristic
   * @return result code
   */
  public int handleReadNotify( BluetoothGattCharacteristic chrt )
  {
    return handleRead( chrt.getValue() );
  }

  /** handle a notification on the GATT WRITE characteristics
   * @param chrt   Sap Gatt characteristic
   * @return true if the write queue has more data
   */
  public byte[] handleWriteNotify( BluetoothGattCharacteristic chrt )
  {
    return handleWrite();
  }

}
