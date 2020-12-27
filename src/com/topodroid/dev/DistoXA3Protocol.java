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
package com.topodroid.dev;

import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.packetX.MemoryOctet;

import android.util.Log;

// import java.lang.ref.WeakReference;

import java.io.IOException;
import java.io.EOFException;
// import java.io.FileNotFoundException;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
// import java.util.UUID;
// import java.util.List;
// import java.util.Locale;
// import java.lang.reflect.Field;
// import java.net.Socket;

// import android.os.CountDownTimer;

import android.content.Context;

// import java.nio.channels.ClosedByInterruptException;
// import java.nio.ByteBuffer;

class DistoXA3Protocol extends DistoXProtocol
{
  //-----------------------------------------------------

  DistoXA3Protocol( DataInputStream in, DataOutputStream out, Device device, Context context )
  {
    super( in, out, device, context );
  }


  /** swap hot bit in a data in DistoX A3 memory
   * @param addr  memory address
   * @return true if successful
   */
  // @Override
  boolean swapA3HotBit( int addr, boolean on_off ) // only A3
  {
    try {
      mBuffer[0] = (byte) 0x38;
      mBuffer[1] = (byte)( addr & 0xff );
      mBuffer[2] = (byte)( (addr>>8) & 0xff );
      mOut.write( mBuffer, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

      mIn.readFully( mBuffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // Log.v( "DistoX-DATA_TYPE", "swap hot bit: " + String.format(" %02x", mBuffer[0] ) );

      if ( mBuffer[0] != (byte)0x38 ) { 
        TDLog.Error( "HotBit-38 wrong reply packet addr " + addr );
        return false;
      }

      int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
      // Log.v( TopoDroidApp.TAG, "proto read ... addr " + addr + " reply addr " + reply_addr );
      if ( reply_addr != addr ) {
        TDLog.Error( "HotBit-38 wrong reply addr " + reply_addr + " addr " + addr );
        return false;
      }
      mBuffer[0] = (byte)0x39;
      // mBuffer[1] = (byte)( addr & 0xff );
      // mBuffer[2] = (byte)( (addr>>8) & 0xff );
      if ( mBuffer[3] == 0x00 ) {
        TDLog.Error( "HotBit refusing to swap addr " + addr );
        return false;
      }  

      if ( on_off ) {
        mBuffer[3] |= (byte)0x80; // RESET HOT BIT
      } else {
        mBuffer[3] &= (byte)0x7f; // CLEAR HOT BIT
      }
      mOut.write( mBuffer, 0, 7 );
      // if ( TDSetting.mPacketLog ) logPacket7( 1L, mBuffer );

      mIn.readFully( mBuffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // Log.v( "DistoX-DATA_TYPE", "swap hot bit[2]: " + String.format(" %02x", mBuffer[0] ) );

      if ( mBuffer[0] != (byte)0x38 ) {
        TDLog.Error( "HotBit-39 wrong reply packet addr " + addr );
        return false;
      }
      reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
      // Log.v( TopoDroidApp.TAG, "proto reset ... addr " + addr + " reply addr " + reply_addr );
      if ( reply_addr != addr ) {
        TDLog.Error( "HotBit-39 wrong reply addr " + reply_addr + " addr " + addr );
        return false;
      }
    } catch ( EOFException e ) {
      TDLog.Error( "HotBit EOF failed addr " + addr );
      return false;
    } catch (IOException e ) {
      TDLog.Error( "HotBit IO failed addr " + addr );
      return false;
    }
    return true;
  }

  // PACKETS I/O ------------------------------------------------------------------------

  @Override
  int getHeadMinusTail( int head, int tail )
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
    try {
      mOut.write( command, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, command );

      mIn.readFully( mBuffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // Log.v( "DistoX-DATA_TYPE", "read A3 head-tail: " + String.format(" %02x", mBuffer[0] ) );

      if ( mBuffer[0] != (byte)( 0x38 ) ) { return null; }
      if ( mBuffer[1] != command[1] ) { return null; }
      if ( mBuffer[2] != command[2] ) { return null; }
      // TODO value of Head-Tail in byte[3-7]
      head_tail[0] = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
      head_tail[1] = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
      return String.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] );
      // TDLog.Log( TDLog.LOG_PROTO, "read Head Tail " + res );
    } catch ( EOFException e ) {
      TDLog.Error( "read Head Tail read() EOF failed" );
      return null;
    } catch (IOException e ) {
      TDLog.Error( "read Head Tail read() IO failed" );
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
  //   try {
  //     mOut.write( mAddr8000, 0, 3 );
  //     // if ( TDSetting.mPacketLog ) logPacket3( 1L, mAddr8000 );

  //     mIn.readFully( mBuffer, 0, 8 );
  //     // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //     if ( mBuffer[0] != (byte)( 0x38 ) ) { return false; }
  //     if ( mBuffer[1] != mAddr8000[1] ) { return false; }
  //     if ( mBuffer[2] != mAddr8000[2] ) { return false; }
  //     result[0] = mBuffer[3];
  //     result[1] = mBuffer[4];
  //     result[2] = mBuffer[5];
  //     result[3] = mBuffer[6];
  //   } catch ( EOFException e ) {
  //     TDLog.Error( "read 8000 read() EOF failed" );
  //     return false;
  //   } catch (IOException e ) {
  //     TDLog.Error( "read 8000 read() IO failed" );
  //     return false;
  //   }
  //   return true;
  // }

}
