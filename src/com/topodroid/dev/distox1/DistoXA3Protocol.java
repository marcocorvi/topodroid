/* @file DistoXProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid TopoDroid-DistoX communication protocol
 *
 * a DistoXA3Protocol is created by the DistoXComm to handle data for DistoX A3
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox1;

import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.packetX.MemoryOctet;
import com.topodroid.dev.Device;
import com.topodroid.dev.distox.DistoXProtocol;
// import com.topodroid.dev.distox.IMemoryDialog;

// import java.lang.ref.WeakReference;

import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
// import java.util.UUID;

// import android.os.CountDownTimer;

import android.content.Context;

// import java.nio.channels.ClosedByInterruptException;
// import java.nio.ByteBuffer;

public class DistoXA3Protocol extends DistoXProtocol
{
  //-----------------------------------------------------

  public DistoXA3Protocol( DataInputStream in, DataOutputStream out, Device device, Context context )
  {
    super( in, out, device, context );
  }


  /** swap hot bit in a data in DistoX A3 memory
   * @param addr  memory address
   * @return true if successful
   */
  // @Override
  public boolean swapA3HotBit( int addr, boolean on_off ) // only A3
  {
    byte[] buffer = new byte[8];
    try {
      buffer[0] = (byte) MemoryOctet.BYTE_PACKET_REPLY; // 0x38;
      buffer[1] = (byte)( addr & 0xff );
      buffer[2] = (byte)( (addr>>8) & 0xff );
      mOut.write( buffer, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, buffer );

      mIn.readFully( buffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // TDLog.v( "A3 swap hot bit: " + String.format(" %02x", buffer[0] ) );

      if ( buffer[0] != (byte)MemoryOctet.BYTE_PACKET_REPLY ) { // 0x38 
        TDLog.e( "HotBit-38 wrong reply packet addr " + addr );
        return false;
      }

      int reply_addr = MemoryOctet.toInt( buffer[2], buffer[1] );
      // TDLog.v( "A3 proto read ... addr " + addr + " reply addr " + reply_addr );
      if ( reply_addr != addr ) {
        TDLog.e( "HotBit-38 wrong reply addr " + reply_addr + " addr " + addr );
        return false;
      }
      buffer[0] = (byte)MemoryOctet.BYTE_PACKET_REQST; // 0x39;
      // buffer[1] = (byte)( addr & 0xff );
      // buffer[2] = (byte)( (addr>>8) & 0xff );
      if ( buffer[3] == 0x00 ) {
        TDLog.e( "HotBit refusing to swap addr " + addr );
        return false;
      }  

      if ( on_off ) {
        buffer[3] |= (byte)0x80; // RESET HOT BIT
      } else {
        buffer[3] &= (byte)0x7f; // CLEAR HOT BIT
      }
      mOut.write( buffer, 0, 7 );
      // if ( TDSetting.mPacketLog ) logPacket7( 1L, buffer );

      mIn.readFully( buffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // TDLog.v( "A3 swap hot bit[2]: " + String.format(" %02x", buffer[0] ) );

      if ( buffer[0] != (byte) MemoryOctet.BYTE_PACKET_REPLY ) {  // 0x38
        TDLog.e( "HotBit-39 wrong reply packet addr " + addr );
        return false;
      }
      reply_addr = MemoryOctet.toInt( buffer[2], buffer[1] );
      // TDLog.v( "A3 proto reset ... addr " + addr + " reply addr " + reply_addr );
      if ( reply_addr != addr ) {
        TDLog.e( "HotBit-39 wrong reply addr " + reply_addr + " addr " + addr );
        return false;
      }
    } catch ( EOFException e ) {
      TDLog.e( "HotBit EOF failed addr " + addr );
      return false;
    } catch (IOException e ) {
      TDLog.e( "HotBit IO failed addr " + addr );
      return false;
    }
    return true;
  }

  // PACKETS I/O ------------------------------------------------------------------------

  @Override
  public int getHeadMinusTail( int head, int tail )
  {
    return ( head >= tail )? (head-tail)/8 : ((DeviceA3Details.MAX_ADDRESS_A3 - tail) + head)/8; 
  }

  /** read the memory buffer head and tail
   * @param command    head-tail command with the memory address of head-tail words
   * @param head_tail  array to store head and tail values
   * @return null on failure, string presentation on success
   */
  String readA3HeadTail( byte[] command, int[] head_tail )
  {
    byte[] buffer = new byte[8];
    try {
      mOut.write( command, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, command );

      mIn.readFully( buffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // TDLog.v( "A3 read A3 head-tail: " + String.format(" %02x", buffer[0] ) );

      if ( buffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) ) { return null; } // 0x38
      if ( buffer[1] != command[1] ) { return null; }
      if ( buffer[2] != command[2] ) { return null; }
      // TODO value of Head-Tail in byte[3-7]
      head_tail[0] = MemoryOctet.toInt( buffer[4], buffer[3] );
      head_tail[1] = MemoryOctet.toInt( buffer[6], buffer[5] );
      return String.format("%02x%02x-%02x%02x", buffer[4], buffer[3], buffer[6], buffer[5] );
      // TDLog.Log( TDLog.LOG_PROTO, "read Head Tail " + res );
    } catch ( EOFException e ) {
      TDLog.e( "read Head Tail read() EOF failed" );
      return null;
    } catch (IOException e ) {
      TDLog.e( "read Head Tail read() IO failed" );
      return null;
    }
  }

  /* read memory at address 8000 DistoX A3 - this method is not used
   * @param result 4-byte array to write the read values
   * @return true if successful
   */
  // @Override
  // boolean read8000( byte[] result )
  // {
  //   byte[] buffer = new byte[8];
  //   try {
  //     mOut.write( mAddr8000, 0, 3 );
  //     // if ( TDSetting.mPacketLog ) logPacket3( 1L, mAddr8000 );

  //     mIn.readFully( buffer, 0, 8 );
  //     // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //     if ( buffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) ) { return false; } // 0x38
  //     if ( buffer[1] != mAddr8000[1] ) { return false; }
  //     if ( buffer[2] != mAddr8000[2] ) { return false; }
  //     result[0] = buffer[3];
  //     result[1] = buffer[4];
  //     result[2] = buffer[5];
  //     result[3] = buffer[6];
  //   } catch ( EOFException e ) {
  //     TDLog.e( "read 8000 read() EOF failed" );
  //     return false;
  //   } catch (IOException e ) {
  //     TDLog.e( "read 8000 read() IO failed" );
  //     return false;
  //   }
  //   return true;
  // }

}
