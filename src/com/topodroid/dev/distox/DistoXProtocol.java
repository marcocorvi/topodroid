/* @file DistoXProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid TopoDroid DistoX communication protocol
 *
 * a DistoXProtocol is created by the DistoXComm to handle data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.prefs.TDSetting;
import com.topodroid.packetX.MemoryOctet;
// import com.topodroid.TDX.TDPath;
import com.topodroid.dev.Device;
import com.topodroid.dev.DataType;
import com.topodroid.dev.TopoDroidProtocol;
// import com.topodroid.dev.distox.DistoX;

// import java.lang.ref.WeakReference;

import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

// import java.util.UUID;
import java.util.List;

import android.content.Context;

import java.nio.channels.ClosedByInterruptException;
// import java.nio.ByteBuffer;

public class DistoXProtocol extends TopoDroidProtocol
{
  // protected Socket  mSocket = null;
  protected DataInputStream  mIn;
  protected DataOutputStream mOut;

  // protected byte[] mHeadTailA3;  // head/tail for Protocol A3
  protected byte[] mAddr8000;     // could be used by DistoXA3Protocol.read8000 
  private byte[] mAcknowledge;
  private byte   mSeqBit;         // sequence bit: 0x00 or 0x80
  protected byte[] mBuffer;

  // protected static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );

  // final protected byte[] mBuffer = new byte[8]; // packet buffer
  // int mMaxTimeout = 8;
  
  //-----------------------------------------------------

  /** cstr
   * @param in     input stream
   * @param out    output stream
   * @param device remote device
   * @param context context
   */
  public DistoXProtocol( DataInputStream in, DataOutputStream out, Device device, Context context )
  {
    super( device, context );

    // mSocket = socket;
    // mDistoX = distox;
    mSeqBit = (byte)0xff;

    // allocate device-specific buffers
    mAddr8000 = new byte[3];
    mAddr8000[0] = MemoryOctet.BYTE_PACKET_REPLY; // 0x38
    mAddr8000[1] = 0x00; // address 0x8000 - already assigned but repeat for completeness
    mAddr8000[2] = (byte)0x80;
    mAcknowledge = new byte[1];
    mBuffer = new byte[8];
    // mAcknowledge[0] = ( b & 0x80 ) | 0x55;
    // mHeadTailA3 = new byte[3];   // to read head/tail for Protocol A3
    // mHeadTailA3[0] = MemoryOctet.BYTE_PACKET_REPLY; // 0x38
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
    //   TDLog.e( "Proto cstr conn failed " + e.getMessage() );
    // }
    mIn  = in;
    mOut = out;
  }

  /** close input/output streams
   */
  @Override
  public void closeIOstreams()
  {
    if ( mIn != null ) {
      try { mIn.close(); } catch ( IOException e ) { TDLog.e("Stream in close"); }
      mIn = null;
    }
    if ( mOut != null ) {
      try { mOut.close(); } catch ( IOException e ) { TDLog.e("Stream out close"); }
      mOut = null;
    }
    super.closeIOstreams(); // to possibly close packet logger db
  }


  // PACKETS I/O ------------------------------------------------------------------------
  /** check that the byte has the proper data type
   * @param b    byte
   * @param data_type expected data type
   */
  // private void checkDataType( byte b, int data_type )
  // {
  //   // if ( DataType.of( b ) != data_type ) { // CHECK_DATA_TYPE 
  //   //   TDLog.v( "DistoX proto read-available: " + String.format(" %02x", b ) + " data_type " + data_type + "/" + DataType.of( b ) );
  //   // }
  // }

  /** try to read 8 bytes - return the number of read bytes
   * @param timeout    joining timeout
   * @param max_timeout max number of join attempts
   * @param data_type  expected data type (shot or calib)
   * @return number of data to read
   */
  private int getAvailable( long timeout, int max_timeout, int data_type ) throws IOException
  {
    mMaxTimeout = max_timeout;
    final int[] dataRead = { 0 };
    final int[] toRead   = { 8 }; // 8 bytes to read
    final int[] count    = { 0 };
    final IOException[] maybeException = { null };
    final Thread reader = new Thread() {
      public void run() {
        // TDLog.Log( TDLog.LOG_PROTO, "reader thread run " + dataRead[0] + "/" + toRead[0] );
        try {
          // synchronized( dataRead ) 
          {
            count[0] = mIn.read( mBuffer, dataRead[0], toRead[0] );
            // if ( TDSetting.mPacketLog ) DoLogPacket( 0L );
            // checkDataType( mBuffer[0], data_type );

            toRead[0]   -= count[0];
            dataRead[0] += count[0];
          }
        } catch ( ClosedByInterruptException e ) {
          TDLog.e( "reader closed by interrupt");
        } catch ( IOException e ) {
          maybeException[0] = e;
        }
        // TDLog.Log( TDLog.LOG_PROTO, "reader thread done " + dataRead[0] + "/" + toRead[0] );
      }
    };
    reader.start();

    for ( int k=0; k<mMaxTimeout; ++k) {
      // TDLog.v( "interrupt loop " + k + " " + dataRead[0] + "/" + toRead[0] );
      try {
        reader.join( timeout );
      } catch ( InterruptedException e ) { TDLog.v( "reader join-1 interrupted"); }
      if ( ! reader.isAlive() ) break;
      {
        Thread interruptor = new Thread() { public void run() {
          // TDLog.v( "interruptor run " + dataRead[0] );
          for ( ; ; ) {
            // synchronized ( dataRead ) 
            {
              if ( dataRead[0] > 0 && toRead[0] > 0 ) { // FIXME
                try { wait( 100 ); } catch ( InterruptedException e ) { TDLog.e("Interrupted wait"); }
              } else {
                if ( reader.isAlive() ) reader.interrupt(); 
                break;
              }
            }
          }
          // TDLog.v( "interruptor done " + dataRead[0] );
        } };
        interruptor.start(); // TODO catch ( OutOfMemoryError e ) { }

        try {
          interruptor.join( 200 );
        } catch ( InterruptedException e ) { TDLog.v( "interruptor join interrupted"); }
      }
      try {
        reader.join( 200 );
      } catch ( InterruptedException e ) { TDLog.v( "reader join-2 interrupted"); }
      if ( ! reader.isAlive() ) break; 
    }
    if ( maybeException[0] != null ) throw maybeException[0];
    return dataRead[0];
  }

  /**
   * @param no_timeout  whether not to timeout
   * @param data_type   expected packet datatype (either shot or calib)
   * @return packet type (if successful)
   */
  @Override // TopoDroidProtocol
  public int readPacket( boolean no_timeout, int data_type )
  {
    // int min_available = ( mDeviceType == Device.DISTO_X000)? 8 : 1; // FIXME 8 should work in every case // FIXME VirtualDistoX
    int min_available = 1; // FIXME 8 should work in every case

    // TDLog.Log( TDLog.LOG_PROTO, "Protocol read packet no-timeout " + (no_timeout?"true":"false") );
    // TDLog.v( "DistoX proto: read packet no-timeout " + (no_timeout?"true":"false") );
    try {
      final int max_timeout = 8;
      int timeout = 0;
      int available = 0;

      if ( no_timeout ) {
        available = 8;
      } else { // do timeout
        if ( TDSetting.mZ6Workaround ) {
          available = getAvailable( 200, 2*max_timeout, data_type );
        } else {
          // while ( ( available = mIn.available() ) == 0 && timeout < max_timeout )
          while ( ( available = mIn.available() ) < min_available && timeout < max_timeout ) {
            ++ timeout;
            // TDLog.Log( TDLog.LOG_PROTO, "Proto read packet sleep " + timeout + "/" + max_timeout );
            TDUtil.slowDown( 100, "Proto read packet InterruptedException" );
          }
        }
      }
      // TDLog.Log( TDLog.LOG_PROTO, "Protocol read packet available " + available );
      // TDLog.v( "DistoX proto: read packet available " + available );
      // if ( available > 0 ) 
      if ( available >= min_available ) {
        if ( no_timeout || ! TDSetting.mZ6Workaround ) {
          mIn.readFully( mBuffer, 0, 8 );
          if ( TDSetting.mPacketLog ) logPacket( 0L, mBuffer );
          // checkDataType( mBuffer[0], data_type );
        }

        // DistoX packets have a sequence bit that flips between 0 and 1
        byte seq  = (byte)(mBuffer[0] & 0x80); 
        // TDLog.v( "VD read packet seq bit " + String.format("%02x %02x %02x", mBuffer[0], seq, mSeqBit ) );
        boolean ok = ( seq != mSeqBit );
        mSeqBit = seq;
        // if ( (mBuffer[0] & 0x0f) != 0 ) // ack every packet
        { 
          // checkDataType( mBuffer[0], data_type );
          mAcknowledge[0] = (byte)(( mBuffer[0] & 0x80 ) | 0x55);
          mOut.write( mAcknowledge, 0, 1 );
          // if ( TDLog.LOG_PROTO ) TDLog.DoLog( "read packet byte " + String.format(" %02x", mBuffer[0] ) + " ... writing ack" );
          if ( TDSetting.mPacketLog ) logPacket1( 1L, mAcknowledge );
        }
        if ( ok ) return handlePacket( mBuffer );
      } // else timed-out with no packet
    } catch ( EOFException e ) {
      TDLog.e( "Proto read packet EOFException" + e.toString() );
    } catch (ClosedByInterruptException e ) {
      TDLog.e( "Proto read packet ClosedByInterruptException" + e.toString() );
    } catch (IOException e ) {
      // this is OK: the DistoX has been turned off
      TDLog.e( "Proto read packet IOException " + e.toString() + " OK distox turned off" );
      // mError = DistoX.DISTOX_ERR_OFF;
      return DistoX.DISTOX_ERR_OFF;
    }
    return DataType.PACKET_NONE;
  }

  /** write a command to the out channel
   * @param cmd command code
   * @return true if success
   */
  @Override
  public boolean sendCommand( byte cmd )
  {
    // TDLog.Log( TDLog.LOG_PROTO, String.format("send command %02x", cmd ) );
    // TDLog.v( String.format("send command %02x", cmd ) );
    byte[] buffer = new byte[8];  // request buffer

    try {
      buffer[0] = (byte)(cmd);
      mOut.write( buffer, 0, 1 );
      mOut.flush();
      // if ( TDSetting.mPacketLog ) logPacket1( 1L, buffer );
    } catch (IOException e ) {
      // TDLog.e( "send command failed" );
      return false;
    }
    return true;
  }

  // must be overridden
  public int getHeadMinusTail( int head, int tail ) { return 0; }

  /** read the number of data to download
   * @param command command to send to the DistoX
   * @return number of data to download
   */
  @Override
  public int readToRead( byte[] command )
  {
    int ret = 0;
    mError = DistoX.DISTOX_ERR_OK;
    try {
      mOut.write( command, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, command );

      mIn.readFully( mBuffer, 0, 8 );
      if ( TDSetting.mPacketLog ) logPacket( 0L, mBuffer );
      // CHECK_DATA_TYPE 
      // checkDataType( mBuffer[0], data_type );

      if ( ( mBuffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) ) || ( mBuffer[1] != command[1] ) || ( mBuffer[2] != command[2] ) ) { // 0x38
	mError = DistoX.DISTOX_ERR_HEADTAIL;
	return DistoX.DISTOX_ERR_HEADTAIL;
      }
      int head = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
      int tail = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
      ret = getHeadMinusTail( head, tail );
      // TDLog.v( "read to-read: H " + head + " T " + tail + " ret " + ret );

      // DEBUG
      // if ( TDLog.LOG_PROTO ) {
      //   TDLog.DoLog( "Proto read-to-read Head-Tail " + 
      //     String.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] )
      //     + " " + head + " - " + tail + " = " + ret );
      // }
      return ret;
    } catch ( EOFException e ) {
      // TDLog.e( "Proto read-to-read Head-Tail read() failed" );
      mError = DistoX.DISTOX_ERR_HEADTAIL_EOF;
      return DistoX.DISTOX_ERR_HEADTAIL_EOF;
    } catch (IOException e ) {
      // TDLog.e( "Proto read-to-read Head-Tail read() failed" );
      mError = DistoX.DISTOX_ERR_HEADTAIL_IO;
      return DistoX.DISTOX_ERR_HEADTAIL_IO;
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
  //     // if ( TDSetting.mPacketLog ) logPacket( 0L, mBuffer );

  //     if ( mBuffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) ) { return null; } // 0x38
  //     if ( mBuffer[1] != command[1] ) { return null; }
  //     if ( mBuffer[2] != command[2] ) { return null; }
  //     // TODO value of Head-Tail in byte[3-7]
  //     head_tail[0] = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
  //     head_tail[1] = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
  //     return String.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] );
  //     // TDLog.Log( TDLog.LOG_PROTO, "read Head Tail " + res );
  //   } catch ( EOFException e ) {
  //     TDLog.e( "read Head Tail read() EOF failed" );
  //     return null;
  //   } catch (IOException e ) {
  //     TDLog.e( "read Head Tail read() IO failed" );
  //     return null;
  //   }
  // }

  /** read 4 bytes at a memory location
   * @param addr    memory address to read
   * @return 4-byte array with memory data
   */
  @Override
  public byte[] readMemory( int addr )
  {
    mBuffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REPLY ); // 0x38
    mBuffer[1] = (byte)( addr & 0xff );
    mBuffer[2] = (byte)( (addr>>8) & 0xff );
    try {
      mOut.write( mBuffer, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

      mIn.readFully( mBuffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L, mBuffer );
      // checkDataType( mBuffer[0], data_type );

    } catch ( IOException e ) {
      // TDLog.e( "read memory() IO failed" );
      return null;
    }
    if ( mBuffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) ) return null; // 0x38
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
  public int readMemory( int start, int end, List< MemoryOctet > data )
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
        mBuffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REPLY ); // 0x38
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        // TODO write and read
        try {
          mOut.write( mBuffer, 0, 3 );
          // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

          mIn.readFully( mBuffer, 0, 8 );
          // if ( TDSetting.mPacketLog ) logPacket( 0L, mBuffer );
          // checkDataType( mBuffer[0], data_type );

        } catch ( IOException e ) {
          TDLog.e( "read memory() IO failed" );
          break;
        }
        if ( mBuffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) ) break; // 0x38
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
        // TDLog.v( "DistoX proto memory " + result.toString() );
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
  public boolean writeCalibration( byte[] calib )
  { 
    if ( calib == null ) return false;
    int  len  = calib.length;
    // TDLog.v( "writeCalibration length " + len );
    long addr = 0x8010;
    // long end  = addr + len;
    try {
      int k = 0;
      while ( k < len ) {
        // TDLog.Log( TDLog.LOG_PROTO, "write calibration " + k + " of " + len );
        mBuffer[0] = MemoryOctet.BYTE_PACKET_REQST; // 0x39
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        mBuffer[3] = calib[k]; ++k;
        mBuffer[4] = calib[k]; ++k;
        mBuffer[5] = calib[k]; ++k;
        mBuffer[6] = calib[k]; ++k;
        mOut.write( mBuffer, 0, 7 );
        if ( TDSetting.mPacketLog ) logPacket7( 1L, mBuffer );

        mIn.readFully( mBuffer, 0, 8 );
        if ( TDSetting.mPacketLog ) logPacket( 0L, mBuffer );
        // checkDataType( mBuffer[0], data_type );

        // TDLog.Log( TDLog.LOG_PROTO, "write calibration " + 
        //   String.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
        //   mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
        if ( mBuffer[0] != MemoryOctet.BYTE_PACKET_REPLY ) { return false; } // 0x38
        if ( mBuffer[1] != (byte)( addr & 0xff ) ) { return false; }
        if ( mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) { return false; }
        addr += 4;
      }
    } catch ( EOFException e ) {
      // TDLog.e( "write calibration EOF failed" );
      return false;
    } catch (IOException e ) {
      // TDLog.e( "write calibration IO failed" );
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
  public boolean readCalibration( byte[] calib )
  {
    if ( calib == null ) return false;
    int  len  = calib.length;
    if ( len > 52 ) len = 52; // FIXME force max length of calib coeffs
    int addr = 0x8010;
    // int end  = addr + len;
    try {
      int k = 0;
      while ( k < len ) { 
        // TDLog.Log( TDLog.LOG_PROTO, "read calibration " + k + " of 52");
        mBuffer[0] = MemoryOctet.BYTE_PACKET_REPLY; // 0x38
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        mOut.write( mBuffer, 0, 3 );
        // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

        mIn.readFully( mBuffer, 0, 8 );
        // if ( TDSetting.mPacketLog ) logPacket( 0L, mBuffer );
        // checkDataType( mBuffer[0], data_type );

        if ( mBuffer[0] != MemoryOctet.BYTE_PACKET_REPLY ) { return false; } // 0x38
        if ( mBuffer[1] != (byte)( addr & 0xff ) ) { return false; }
        if ( mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) { return false; }
        calib[k] = mBuffer[3]; ++k;
        calib[k] = mBuffer[4]; ++k;
        calib[k] = mBuffer[5]; ++k;
        calib[k] = mBuffer[6]; ++k;
        addr += 4;
      }
    } catch ( EOFException e ) {
      // TDLog.e( "read calibration EOF failed" );
      return false;
    } catch (IOException e ) {
      // TDLog.e( "read calibration IO failed" );
      return false;
    }
    return true;  
  }

}
