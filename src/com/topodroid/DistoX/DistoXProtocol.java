/* @file DistoXProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid TopoDroid-DistoX communication protocol
 *
 * a DistoXProtocol is created by the DistoXComm to handle data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

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
import java.util.List;
// import java.util.Locale;
// import java.lang.reflect.Field;
// import java.net.Socket;

// import android.os.CountDownTimer;

import android.content.Context;

import java.nio.channels.ClosedByInterruptException;
// import java.nio.ByteBuffer;

class DistoXProtocol extends TopoDroidProtocol
{
  // protected Socket  mSocket = null;
  protected DataInputStream  mIn;
  protected DataOutputStream mOut;

  // protected byte[] mHeadTailA3;  // head/tail for Protocol A3
  protected byte[] mAddr8000;     // cpuld be used by DistoXA3Protocol.read8000 
  private byte[] mAcknowledge;
  private byte   mSeqBit;         // sequence bit: 0x00 or 0x80

  // protected static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );

  // final protected byte[] mBuffer = new byte[8]; // packet buffer
  // int mMaxTimeout = 8;
  
  //-----------------------------------------------------

  protected DistoXProtocol( DataInputStream in, DataOutputStream out, Device device, Context context )
  {
    super( device, context );

    // mSocket = socket;
    // mDistoX = distox;
    mSeqBit = (byte)0xff;

    // allocate device-specific buffers
    mAddr8000 = new byte[3];
    mAddr8000[0] = 0x38;
    mAddr8000[1] = 0x00; // address 0x8000 - already assigned but repeat for completeness
    mAddr8000[2] = (byte)0x80;
    mAcknowledge = new byte[1];
    // mAcknowledge[0] = ( b & 0x80 ) | 0x55;
    // mHeadTailA3 = new byte[3];   // to read head/tail for Protocol A3
    // mHeadTailA3[0] = 0x38;
    // mHeadTailA3[1] = 0x20;       // address 0xC020
    // mHeadTailA3[2] = (byte)0xC0;

    // try {
    //   if ( mSocket != null ) {
    //     // mIn  = new DataInputStream( extractCoreInputStream( mSocket.getInputStream() ) );
    //     mIn  = new DataInputStream( mSocket.getInputStream() );
    //     mOut = new DataOutputStream( mSocket.getOutputStream() );
    //   }
    // } catch ( IOException e ) {
    //   // NOTE socket is null there is nothing we can do
    //   TDLog.Error( "Proto cstr conn failed " + e.getMessage() );
    // }
    mIn  = in;
    mOut = out;
  }

  @Override
  void closeIOstreams()
  {
    if ( mIn != null ) {
      try { mIn.close(); } catch ( IOException e ) { }
      mIn = null;
    }
    if ( mOut != null ) {
      try { mOut.close(); } catch ( IOException e ) { }
      mOut = null;
    }
  }

  // ACTIONS -----------------------------------------------------------

  /* swap hot bit in a data in DistoX A3 memory
   * @param addr  memory address
   */
  // @Override
  // boolean swapA3HotBit( int addr, boolean on_off ) // only A3
  // {
  //   try {
  //     mBuffer[0] = (byte) 0x38;
  //     mBuffer[1] = (byte)( addr & 0xff );
  //     mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //     mOut.write( mBuffer, 0, 3 );
  //     // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

  //     mIn.readFully( mBuffer, 0, 8 );
  //     // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //     if ( mBuffer[0] != (byte)0x38 ) { 
  //       TDLog.Error( "HotBit-38 wrong reply packet addr " + addr );
  //       return false;
  //     }

  //     int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
  //     // Log.v( TopoDroidApp.TAG, "proto read ... addr " + addr + " reply addr " + reply_addr );
  //     if ( reply_addr != addr ) {
  //       TDLog.Error( "HotBit-38 wrong reply addr " + reply_addr + " addr " + addr );
  //       return false;
  //     }
  //     mBuffer[0] = (byte)0x39;
  //     // mBuffer[1] = (byte)( addr & 0xff );
  //     // mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //     if ( mBuffer[3] == 0x00 ) {
  //       TDLog.Error( "HotBit refusing to swap addr " + addr );
  //       return false;
  //     }  
  //     if ( on_off ) {
  //       mBuffer[3] |= (byte)0x80; // RESET HOT BIT
  //     } else {
  //       mBuffer[3] &= (byte)0x7f; // CLEAR HOT BIT
  //     }
  //     mOut.write( mBuffer, 0, 7 );
  //     // if ( TDSetting.mPacketLog ) logPacket7( 1L, mBuffer );

  //     mIn.readFully( mBuffer, 0, 8 );
  //     // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //     if ( mBuffer[0] != (byte)0x38 ) {
  //       TDLog.Error( "HotBit-39 wrong reply packet addr " + addr );
  //       return false;
  //     }
  //     reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
  //     // Log.v( TopoDroidApp.TAG, "proto reset ... addr " + addr + " reply addr " + reply_addr );
  //     if ( reply_addr != addr ) {
  //       TDLog.Error( "HotBit-39 wrong reply addr " + reply_addr + " addr " + addr );
  //       return false;
  //     }
  //   } catch ( EOFException e ) {
  //     TDLog.Error( "HotBit EOF failed addr " + addr );
  //     return false;
  //   } catch (IOException e ) {
  //     TDLog.Error( "HotBit IO failed addr " + addr );
  //     return false;
  //   }
  //   return true;
  // }

  // PACKETS I/O ------------------------------------------------------------------------

  /** try to read 8 bytes - return the number of read bytes
   * @param timeout    joining timeout
   * @param maxtimeout max number of join attempts
   * @return number of data to read
   */
  private int getAvailable( long timeout, int maxtimeout ) throws IOException
  {
    mMaxTimeout = maxtimeout;
    final int[] dataRead = { 0 };
    final int[] toRead   = { 8 }; // 8 bytes to read
    final int[] count    = { 0 };
    final IOException[] maybeException = { null };
    final Thread reader = new Thread() {
      public void run() {
        TDLog.Log( TDLog.LOG_PROTO, "reader thread run " + dataRead[0] + "/" + toRead[0] );
        try {
          // synchronized( dataRead ) 
          {
            count[0] = mIn.read( mBuffer, dataRead[0], toRead[0] );
            // if ( TDSetting.mPacketLog ) LogPacket( 0L );

            toRead[0]   -= count[0];
            dataRead[0] += count[0];
          }
        } catch ( ClosedByInterruptException e ) {
          TDLog.Error( "reader closed by interrupt");
        } catch ( IOException e ) {
          maybeException[0] = e;
        }
        TDLog.Log( TDLog.LOG_PROTO, "reader thread done " + dataRead[0] + "/" + toRead[0] );
      }
    };
    reader.start();

    for ( int k=0; k<mMaxTimeout; ++k) {
      // Log.v("DistoX", "interrupt loop " + k + " " + dataRead[0] + "/" + toRead[0] );
      try {
        reader.join( timeout );
      } catch ( InterruptedException e ) { TDLog.Log(TDLog.LOG_DEBUG, "reader join-1 interrupted"); }
      if ( ! reader.isAlive() ) break;
      {
        Thread interruptor = new Thread() { public void run() {
          // Log.v("DistoX", "interruptor run " + dataRead[0] );
          for ( ; ; ) {
            // synchronized ( dataRead ) 
            {
              if ( dataRead[0] > 0 && toRead[0] > 0 ) { // FIXME
                try { wait( 100 ); } catch ( InterruptedException e ) { }
              } else {
                if ( reader.isAlive() ) reader.interrupt(); 
                break;
              }
            }
          }
          // Log.v("DistoX", "interruptor done " + dataRead[0] );
        } };
        interruptor.start();

        try {
          interruptor.join( 200 );
        } catch ( InterruptedException e ) { TDLog.Log(TDLog.LOG_DEBUG, "interruptor join interrupted"); }
      }
      try {
        reader.join( 200 );
      } catch ( InterruptedException e ) { TDLog.Log(TDLog.LOG_DEBUG, "reader join-2 interrupted"); }
      if ( ! reader.isAlive() ) break; 
    }
    if ( maybeException[0] != null ) throw maybeException[0];
    return dataRead[0];
  }

  /**
   * @return packet type (if successful)
   */
  @Override
  int readPacket( boolean no_timeout )
  {
    // int min_available = ( mDeviceType == Device.DISTO_X000)? 8 : 1; // FIXME 8 should work in every case // FIXME VirtualDistoX
    int min_available = 1; // FIXME 8 should work in every case

    TDLog.Log( TDLog.LOG_PROTO, "Protocol read packet no-timeout " + (no_timeout?"no":"yes") );
    // Log.v( "DistoX", "VD Proto read packet no-timeout " + (no_timeout?"no":"yes") );
    try {
      final int maxtimeout = 8;
      int timeout = 0;
      int available = 0;

      if ( no_timeout ) {
        available = 8;
      } else { // do timeout
        if ( TDSetting.mZ6Workaround ) {
          available = getAvailable( 200, 2*maxtimeout );
        } else {
          // while ( ( available = mIn.available() ) == 0 && timeout < maxtimeout ) 
          while ( ( available = mIn.available() ) < min_available && timeout < maxtimeout ) {
            ++ timeout;
            // TDLog.Log( TDLog.LOG_PROTO, "Proto read packet sleep " + timeout + "/" + maxtimeout );
            TDUtil.slowDown( 100, "Proto read packet InterruptedException" );
          }
        }
      }
      TDLog.Log( TDLog.LOG_PROTO, "Protocol read packet available " + available );
      // Log.v( "DistoX", "VD Proto read packet available " + available );
      // if ( available > 0 ) 
      if ( available >= min_available ) {
        if ( no_timeout || ! TDSetting.mZ6Workaround ) {
          mIn.readFully( mBuffer, 0, 8 );
          if ( TDSetting.mPacketLog ) logPacket( 0L );
        }

        // DistoX packets have a sequence bit that flips between 0 and 1
        byte seq  = (byte)(mBuffer[0] & 0x80); 
        // Log.v( "DistoX", "VD read packet seq bit " + String.format("%02x %02x %02x", mBuffer[0], seq, mSeqBit ) );
        boolean ok = ( seq != mSeqBit );
        mSeqBit = seq;
        // if ( (mBuffer[0] & 0x0f) != 0 ) // ack every packet
        { 
          mAcknowledge[0] = (byte)(( mBuffer[0] & 0x80 ) | 0x55);
          if ( TDLog.LOG_PROTO ) {
            TDLog.DoLog( "read packet byte " + String.format(" %02x", mBuffer[0] ) + " ... writing ack" );
          }
          mOut.write( mAcknowledge, 0, 1 );
          if ( TDSetting.mPacketLog ) logPacket1( 1L, mAcknowledge );
        }
        if ( ok ) return handlePacket();
      } // else timedout with no packet
    } catch ( EOFException e ) {
      TDLog.Log( TDLog.LOG_PROTO, "Proto read packet EOFException" + e.toString() );
    } catch (ClosedByInterruptException e ) {
      TDLog.Error( "Proto read packet ClosedByInterruptException" + e.toString() );
    } catch (IOException e ) {
      // this is OK: the DistoX has been turned off
      TDLog.Debug( "Proto read packet IOException " + e.toString() + " OK distox turned off" );
      // mError = DISTOX_ERR_OFF;
      return DISTOX_ERR_OFF;
    }
    return DISTOX_PACKET_NONE;
  }

  /** write a command to the out channel
   * @param cmd command code
   * @return true if success
   */
  @Override
  boolean sendCommand( byte cmd )
  {
    TDLog.Log( TDLog.LOG_PROTO, "sendCommand " + String.format("Send command %02x", cmd ) );
    // Log.v( "DistoX", "sendCommand " + String.format("Send command %02x", cmd ) );

    try {
      mRequestBuffer[0] = (byte)(cmd);
      mOut.write( mRequestBuffer, 0, 1 );
      mOut.flush();
      // if ( TDSetting.mPacketLog ) logPacket1( 1L, mRequestBuffer );
    } catch (IOException e ) {
      TDLog.Error( "sendCommand failed" );
      return false;
    }
    return true;
  }

  // must be overridden
  int getHeadMinusTail( int head, int tail ) { return 0; }

  /** read the number of data to download
   * @param command command to send to the DistoX
   * @return number of data to download
   */
  @Override
  int readToRead( byte[] command )
  {
    int ret = 0;
    mError = DISTOX_ERR_OK;
    try {
      mOut.write( command, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, command );

      mIn.readFully( mBuffer, 0, 8 );
      if ( TDSetting.mPacketLog ) logPacket( 0L );

      if ( ( mBuffer[0] != (byte)( 0x38 ) ) || ( mBuffer[1] != command[1] ) || ( mBuffer[2] != command[2] ) ) {
	mError = DISTOX_ERR_HEADTAIL;
	return DISTOX_ERR_HEADTAIL;
      }
      int head = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
      int tail = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
      ret = getHeadMinusTail( head, tail );
      // Log.v("DistoX-PROTO", "read to-read: H " + head + " T " + tail + " ret " + ret );

      // DEBUG
      if ( TDLog.LOG_PROTO ) {
        TDLog.DoLog(
          "Proto read-to-read Head-Tail " + 
          String.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] )
          + " " + head + " - " + tail + " = " + ret );
      }
      return ret;
    } catch ( EOFException e ) {
      TDLog.Error( "Proto read-to-read Head-Tail read() failed" );
      mError = DISTOX_ERR_HEADTAIL_EOF;
      return DISTOX_ERR_HEADTAIL_EOF;
    } catch (IOException e ) {
      TDLog.Error( "Proto read-to-read Head-Tail read() failed" );
      mError = DISTOX_ERR_HEADTAIL_IO;
      return DISTOX_ERR_HEADTAIL_IO;
    }
  }

  /* read the memory buffer head and tail
   * @param command    head-tail command with the memory address of head-tail words
   * @param head_tail  array to store head and tail values
   * @return null on failure, string presentation on success
   */
  // @Override
  // String readA3HeadTail( byte[] command, int[] head_tail )
  // {
  //   try {
  //     mOut.write( command, 0, 3 );
  //     // if ( TDSetting.mPacketLog ) logPacket3( 1L, command );

  //     mIn.readFully( mBuffer, 0, 8 );
  //     // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //     if ( mBuffer[0] != (byte)( 0x38 ) ) { return null; }
  //     if ( mBuffer[1] != command[1] ) { return null; }
  //     if ( mBuffer[2] != command[2] ) { return null; }
  //     // TODO value of Head-Tail in byte[3-7]
  //     head_tail[0] = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
  //     head_tail[1] = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
  //     return String.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] );
  //     // TDLog.Log( TDLog.LOG_PROTO, "read Head Tail " + res );
  //   } catch ( EOFException e ) {
  //     TDLog.Error( "read Head Tail read() EOF failed" );
  //     return null;
  //   } catch (IOException e ) {
  //     TDLog.Error( "read Head Tail read() IO failed" );
  //     return null;
  //   }
  // }

  /** read 4 bytes at a memory location
   * @param addr    memory address to read
   * @return 4-byte array with memory data
   */
  @Override
  byte[] readMemory( int addr )
  {
    mBuffer[0] = (byte)( 0x38 );
    mBuffer[1] = (byte)( addr & 0xff );
    mBuffer[2] = (byte)( (addr>>8) & 0xff );
    try {
      mOut.write( mBuffer, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

      mIn.readFully( mBuffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );

    } catch ( IOException e ) {
      TDLog.Error( "readmemory() IO failed" );
      return null;
    }
    if ( mBuffer[0] != (byte)( 0x38 ) ) return null;
    int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
    if ( reply_addr != addr ) return null;
    byte[] ret = new byte[4];
    // for (int i=3; i<7; ++i) ret[i-3] = mBuffer[i];
    ret[0] = mBuffer[3];
    ret[1] = mBuffer[4];
    ret[2] = mBuffer[5];
    ret[3] = mBuffer[6];
    return ret;
  }

  /** read memory data between two addresses
   * @param start    start address (inclusive)
   * @param end      end address (excluded)
   * @param data     array-list of MemoryOctet to fill with read data
   * @return the number of read octets 
   */
  @Override
  int readMemory( int start, int end, List< MemoryOctet > data )
  {
    if ( start < 0 ) start = 0;
    if ( end > 0x8000 ) end = 0x8000;
    start = start - start % 8;
    end   = end - ( end % 8 );
    if ( start >= end ) return -1;
    int cnt = 0; // number of data read
    for ( ; start < end; start += 8 ) {
      MemoryOctet result = new MemoryOctet( start/8 );
      int k = 0;
      for ( ; k<8; k+=4 ) {
        int addr = start+k;
        mBuffer[0] = (byte)( 0x38 );
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        // TODO write and read
        try {
          mOut.write( mBuffer, 0, 3 );
          // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

          mIn.readFully( mBuffer, 0, 8 );
          // if ( TDSetting.mPacketLog ) logPacket( 0L );

        } catch ( IOException e ) {
          TDLog.Error( "readmemory() IO failed" );
          break;
        }
        if ( mBuffer[0] != (byte)( 0x38 ) ) break;
        int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
        if ( reply_addr != addr ) break;
        // for (int i=3; i<7; ++i) result.data[k+i-3] = mBuffer[i];
        result.data[k  ] = mBuffer[3];
        result.data[k+1] = mBuffer[4];
        result.data[k+2] = mBuffer[5];
        result.data[k+3] = mBuffer[6];
      }
      if ( k == 8 ) {
        data.add( result );
        // Log.v( TopoDroidApp.TAG, "memory " + result.toString() );
        ++ cnt;
      } else {
        break;
      }
    }
    return cnt;
  }

  /** write calibration coeffs to the DistoX
   * @param calib    array of 48 (52) bytes with the coefficients
   * @return true if successful
   */
  @Override
  boolean writeCalibration( byte[] calib )
  { 
    if ( calib == null ) return false;
    int  len  = calib.length;
    // Log.v("DistoX", "writeCalibration length " + len );
    long addr = 0x8010;
    // long end  = addr + len;
    try {
      int k = 0;
      while ( k < len ) {
        mBuffer[0] = 0x39;
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        mBuffer[3] = calib[k]; ++k;
        mBuffer[4] = calib[k]; ++k;
        mBuffer[5] = calib[k]; ++k;
        mBuffer[6] = calib[k]; ++k;
        mOut.write( mBuffer, 0, 7 );
        if ( TDSetting.mPacketLog ) logPacket7( 1L, mBuffer );

        mIn.readFully( mBuffer, 0, 8 );
        if ( TDSetting.mPacketLog ) logPacket( 0L );

        // TDLog.Log( TDLog.LOG_PROTO, "writeCalibration " + 
        //   String.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
        //   mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
        if ( mBuffer[0] != 0x38 ) { return false; }
        if ( mBuffer[1] != (byte)( addr & 0xff ) ) { return false; }
        if ( mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) { return false; }
        addr += 4;
      }
    } catch ( EOFException e ) {
      // TDLog.Error( "writeCalibration EOF failed" );
      return false;
    } catch (IOException e ) {
      // TDLog.Error( "writeCalibration IO failed" );
      return false;
    }
    return true;  
  }

  /** read calibration coeffs from the DistoX
   * @param calib    array of 48 (52) bytes to store the coefficients
   * @return true if successful
   *
   * called only by DistoXComm.readCoeff (TopoDroidComm.readCoeff)
   */
  @Override
  boolean readCalibration( byte[] calib )
  {
    if ( calib == null ) return false;
    int  len  = calib.length;
    if ( len > 52 ) len = 52; // FIXME force max length of calib coeffs
    int addr = 0x8010;
    // int end  = addr + len;
    try {
      int k = 0;
      while ( k < len ) { 
        mBuffer[0] = 0x38;
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        mOut.write( mBuffer, 0, 3 );
        // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

        mIn.readFully( mBuffer, 0, 8 );
        // if ( TDSetting.mPacketLog ) logPacket( 0L );

        if ( mBuffer[0] != 0x38 ) { return false; }
        if ( mBuffer[1] != (byte)( addr & 0xff ) ) { return false; }
        if ( mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) { return false; }
        calib[k] = mBuffer[3]; ++k;
        calib[k] = mBuffer[4]; ++k;
        calib[k] = mBuffer[5]; ++k;
        calib[k] = mBuffer[6]; ++k;
        addr += 4;
      }
    } catch ( EOFException e ) {
      // TDLog.Error( "readCalibration EOF failed" );
      return false;
    } catch (IOException e ) {
      // TDLog.Error( "readCalibration IO failed" );
      return false;
    }
    return true;  
  }

  // X310 ====================================================================   
  // private static final int DATA_PER_BLOCK = 56;
  // private static final int BYTE_PER_DATA  = 18;
  // note 56*18 = 1008
  // next there are 16 byte padding for each 1024-byte block (0x400 byte block)
  //
  // address space = 0x0000 - 0x7fff
  // blocks: 0000 - 03ff =    0 -   55
  //         0400 - 07ff =   56 -  111
  //         0800 - 0bff =  112 -  167
  //         0c00 - 0fff =  168 -  223
  //         1000 - 13ff =  224 -  279
  //         ...
  //         2000 - 23ff =  448 -  503
  //         ...
  //         3000 - 33ff =  672 -  727
  //         4000 - 43ff =  896 -  951
  //         5000 - 53ff = 1120 - 1175
  //         6000 - 63ff = 1344 - 1399
  //         7000 - 73ff = 1568 - 1623
  //         ...
  //         7c00 - 7fff = 1736 - 1791
  // 

  // private static int index2addrX310( int index )
  // {
  //   if ( index < 0 ) index = 0;
  //   if ( index > 1792 ) index = 1792;
  //   int addr = 0;
  //   while ( index >= DATA_PER_BLOCK ) {
  //     index -= DATA_PER_BLOCK;
  //     addr += 0x400;
  //   }
  //   addr += BYTE_PER_DATA * index;
  //   return addr;
  // }

  // private static int addr2indexX310( int addr )
  // {
  //   int index = 0;
  //   addr = addr - ( addr % 8 );
  //   while ( addr >= 0x400 ) {
  //     addr -= 0x400;
  //     index += DATA_PER_BLOCK;
  //   }
  //   index += (int)(addr/BYTE_PER_DATA);
  //   return index;
  // }

  // memory layout
  // byte 0-7  first packet
  // byte 8-15 second packet
  // byte 16   hot-flag for the first packet
  // byte 17   hot-flag for the second packet
  //
  // X310 data address space: 0x0000 - 0x7fff
  // each data takes 18 bytes

  // @Override
  // int readX310Memory( int start, int end, List< MemoryOctet > data )
  // {
  //   // Log.v( "DistoX", "start " + start + " end " + end );
  //   int cnt = 0;
  //   while ( start < end ) {
  //     int addr = index2addrX310( start );
  //     // Log.v( "DistoX", start + " addr " + addr );
  //     int endaddr = addr + BYTE_PER_DATA;
  //     MemoryOctet result = new MemoryOctet( start );
  //     // read only bytes 0-7 and 16-17
  //     int k = 0;
  //     for ( ; addr < endaddr && k < 8; addr += 4, k+=4 ) {
  //       mBuffer[0] = (byte)( 0x38 );
  //       mBuffer[1] = (byte)( addr & 0xff );
  //       mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //       // TODO write and read
  //       try {
  //         mOut.write( mBuffer, 0, 3 );
  //         // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

  //         mIn.readFully( mBuffer, 0, 8 );
  //         // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //       } catch ( IOException e ) {
  //         TDLog.Error( "readmemory() IO failed" );
  //         break;
  //       }
  //       if ( mBuffer[0] != (byte)( 0x38 ) ) break;
  //       int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
  //       if ( reply_addr != addr ) break;
  //       // for (int i=3; i<7; ++i) result.data[k+i-3] = mBuffer[i];
  //       result.data[k  ] = mBuffer[3];
  //       result.data[k+1] = mBuffer[4];
  //       result.data[k+2] = mBuffer[5];
  //       result.data[k+3] = mBuffer[6];
  //     }
  //     if ( k == 8 ) {
  //       addr = index2addrX310( start ) + 16;
  //       mBuffer[0] = (byte)( 0x38 );
  //       mBuffer[1] = (byte)( addr & 0xff );
  //       mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //       try {
  //         mOut.write( mBuffer, 0, 3 );
  //         // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

  //         mIn.readFully( mBuffer, 0, 8 );
  //         // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //       } catch ( IOException e ) {
  //         TDLog.Error( "readmemory() IO failed" );
  //         break;
  //       }
  //       if ( mBuffer[0] != (byte)( 0x38 ) ) break;
  //       if ( mBuffer[3] == (byte)( 0xff ) ) result.data[0] |= (byte)( 0x80 ); 
  //       data.add( result );
  //       // Log.v( TopoDroidApp.TAG, "memory " + result.toString() + " " + mBuffer[3] );
  //       ++ cnt;
  //     } else {
  //       break;
  //     }
  //     ++start;
  //   }
  //   return cnt;
  // }

  // X310 data memory is read-only
  // // return number of memory slots that have been reset
  // public int resetX310Memory( int start, int end )
  // {
  //   int cnt = start;
  //   while ( start < end ) {
  //     int addr = index2addrX310( start ) + 16;
  //     mBuffer[0] = (byte)( 0x38 );
  //     mBuffer[1] = (byte)( addr & 0xff );
  //     mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //     TDLog.Error( "resetMemory() address " + mBuffer[1] + " " + mBuffer[2] );

  //     // TODO write and read
  //     try {
  //       mOut.write( mBuffer, 0, 3 );
  //       // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );
  //
  //       mIn.readFully( mBuffer, 0, 8 );
  //       // if ( TDSetting.mPacketLog ) logPacket( 0L );
  //
  //     } catch ( IOException e ) {
  //       TDLog.Error( "resetMemory() IO nr. 1 failed" );
  //       break;
  //     }
  //     if ( mBuffer[0] != (byte)( 0x38 ) ||
  //          mBuffer[1] != (byte)( addr & 0xff ) ||
  //          mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) {
  //       TDLog.Error( "resetMemory() bad read reply " + mBuffer[0] + " addr " + mBuffer[1] + " " + mBuffer[2] );
  //       break;
  //     }
  //     TDLog.Error( "resetMemory() ok read reply " + mBuffer[3] + " " + mBuffer[4] + " " + mBuffer[5] + " " + mBuffer[6] );

  //     mBuffer[0] = (byte)( 0x39 );
  //     mBuffer[1] = (byte)( addr & 0xff );
  //     mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //     mBuffer[3] = (byte)( 0xff );
  //     mBuffer[4] = (byte)( 0xff );
  //     try {
  //       mOut.write( mBuffer, 0, 7 );
  //       // if ( TDSetting.mPacketLog ) logPacket7( 1L, mBuffer );
  //
  //       mIn.readFully( mBuffer, 0, 8 );
  //       // if ( TDSetting.mPacketLog ) logPacket( 0L );
  //
  //     } catch ( IOException e ) {
  //       TDLog.Error( "resetMemory() IO nr. 2 failed" );
  //       break;
  //     }
  //     if ( mBuffer[0] != (byte)( 0x38 ) ||
  //          mBuffer[1] != (byte)( addr & 0xff ) ||
  //          mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) {
  //       TDLog.Error( "resetMemory() bad write reply " + mBuffer[0] + " addr " + mBuffer[1] + " " + mBuffer[2] );
  //       break;
  //     }
  //     TDLog.Error( "resetMemory() ok write reply " + mBuffer[3] + " " + mBuffer[4] + " " + mBuffer[5] + " " + mBuffer[6] );
  //     ++ start;
  //   }
  //   return start - cnt;
  // }

  // @Override
  // int uploadFirmware( String filepath )
  // {
  //   TDLog.LogFile( "Firmware upload: protocol starts. file " + filepath );
  //   byte[] buf = new byte[259];
  //   buf[0] = (byte)0x3b;
  //   buf[1] = (byte)0;
  //   buf[2] = (byte)0;

  //   boolean ok = true;
  //   int cnt = 0;
  //   try {
  //     File fp = new File( filepath );
  //     FileInputStream fis = new FileInputStream( fp );
  //     DataInputStream dis = new DataInputStream( fis );
  //     // int end_addr = (fp.size() + 255)/256;

  //     for ( int addr = 0; /* addr < end_addr */; ++ addr ) {
  //       TDLog.LogFile( "Firmware upload: addr " + addr + " count " + cnt );
  //       // memset(buf+3, 0, 256)
  //       for (int k=0; k<256; ++k) buf[3+k] = (byte)0xff;
  //       try {
  //         int nr = dis.read( buf, 3, 256 );
  //         if ( nr <= 0 ) {
  //           TDLog.LogFile( "Firmware upload: file read failure. Result " + nr );
  //           break;
  //         }
  //         cnt += nr;
  //         if ( addr >= 0x08 ) {
  //           buf[0] = (byte)0x3b;
  //           buf[1] = (byte)( addr & 0xff );
  //           buf[2] = 0; // not necessary
  //           mOut.write( buf, 0, 259 );
  //           // if ( TDSetting.mPacketLog ) logPacket8( 1L, buf );

  //           mIn.readFully( mBuffer, 0, 8 );
  //           // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //           int reply_addr = ( ((int)(mBuffer[2]))<<8 ) + ((int)(mBuffer[1]));
  //           if ( mBuffer[0] != (byte)0x3b || addr != reply_addr ) {
  //             TDLog.LogFile( "Firmware upload: fail at " + cnt + " buffer[0]: " + mBuffer[0] + " reply_addr " + reply_addr );
  //             ok = false;
  //             break;
  //           } else {
  //             TDLog.LogFile( "Firmware upload: reply address ok");
  //           }
  //         } else {
  //           TDLog.LogFile( "Firmware upload: skip address " + addr );
  //         }
  //       } catch ( EOFException e ) { // OK
  //         TDLog.LogFile( "Firmware update: EOF " + e.getMessage() );
  //         break;
  //       } catch ( IOException e ) { 
  //         TDLog.LogFile( "Firmware update: IO error " + e.getMessage() );
  //         ok = false;
  //         break;
  //       }
  //     }
  //   } catch ( FileNotFoundException e ) {
  //     TDLog.LogFile( "Firmware update: Not Found error " + e.getMessage() );
  //     return 0;
  //   }
  //   TDLog.LogFile( "Firmware update: result is " + (ok? "OK" : "FAIL") + " count " + cnt );
  //   return ( ok ? cnt : -cnt );
  // }

  // @Override
  // int dumpFirmware( String filepath )
  // {
  //   TDLog.LogFile( "Firmware dump: output filepath " + filepath );
  //   byte[] buf = new byte[256];

  //   boolean ok = true;
  //   int cnt = 0;
  //   try {
  //     TDPath.checkPath( filepath );
  //     File fp = new File( filepath );
  //     FileOutputStream fos = new FileOutputStream( fp );
  //     DataOutputStream dos = new DataOutputStream( fos );
  //     for ( int addr = 0; ; ++ addr ) {
  //       TDLog.LogFile( "Firmware dump: addr " + addr + " count " + cnt );
  //       try {
  //         buf[0] = (byte)0x3a;
  //         buf[1] = (byte)( addr & 0xff );
  //         buf[2] = 0; // not necessary
  //         mOut.write( buf, 0, 3 );
  //         // if ( TDSetting.mPacketLog ) logPacket3( 1L, buf );

  //         mIn.readFully( mBuffer, 0, 8 );
  //         // if ( TDSetting.mPacketLog ) logPacket( 0L );

  //         int reply_addr = ( ((int)(mBuffer[2]))<<8 ) + ((int)(mBuffer[1]));
  //         if ( mBuffer[0] != (byte)0x3a || addr != reply_addr ) {
  //           TDLog.LogFile( "Firmware dump: fail at " + cnt + " buffer[0]: " + mBuffer[0] + " reply_addr " + reply_addr );
  //           ok = false;
  //           break;
  //         } else {
  //           TDLog.LogFile( "Firmware dump: reply addr ok");
  //         }

  //         mIn.readFully( buf, 0, 256 );
  //         // if ( TDSetting.mPacketLog ) logPacket8( 0L, buf );

  //         boolean last = true;
  //         for ( int k=0; last && k<256; ++k ) {
  //           if ( buf[k] != (byte)0xff ) last = false;
  //         }
  //         if ( last ) break;
  //         dos.write( buf, 0, 256 );
  //         cnt += 256;
  //       } catch ( EOFException e ) { // OK
  //         break;
  //       } catch ( IOException e ) { 
  //         ok = false;
  //         break;
  //       }
  //     }
  //   } catch ( FileNotFoundException e ) {
  //     return 0;
  //   }
  //   TDLog.LogFile( "Firmware dump: result is " + (ok? "OK" : "FAIL") + " count " + cnt );
  //   return ( ok ? cnt : -cnt );
  // }

}
