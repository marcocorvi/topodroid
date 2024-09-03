/* @file DistoXProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid TopoDroid DistoX2 (X310) communication protocol
 *
 * a DistoX310Protocol is created by the DistoXComm to handle data for DistoX2 X310
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox2;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.TDProgress;
import com.topodroid.dev.Device;
import com.topodroid.dev.distox.DistoXProtocol;
import com.topodroid.dev.distox.IMemoryDialog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.packetX.MemoryOctet;
// import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

// import java.lang.ref.WeakReference;

import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
// import java.util.UUID;
import java.util.List;
// import java.util.Locale;
// import java.lang.reflect.Field;
// import java.net.Socket;

// import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import android.content.Context;
import android.content.res.Resources;

// import java.nio.channels.ClosedByInterruptException;
// import java.nio.ByteBuffer;

public class DistoX310Protocol extends DistoXProtocol
{
  //-----------------------------------------------------

  public DistoX310Protocol( DataInputStream in, DataOutputStream out, Device device, Context context )
  {
    super( in, out, device, context );
  }

  // PACKETS I/O ------------------------------------------------------------------------

  @Override
  public int getHeadMinusTail( int head, int tail )
  {
    // head = head segment index ---- X310 specific code
    // tail = tail packet index
    int hp = 2 * head; // head packet index
    return ( hp >= tail )? (hp - tail) : (hp + (DeviceX310Details.MAX_INDEX_X310 - tail) );
    // ret can be odd
  }

  // X310 ====================================================================   
  private static final int DATA_PER_BLOCK = 56;
  private static final int BYTE_PER_DATA  = 18;
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

  private static int index2addrX310( int index )
  {
    if ( index < 0 ) index = 0;
    if ( index > 1792 ) index = 1792;
    int addr = 0;
    while ( index >= DATA_PER_BLOCK ) {
      index -= DATA_PER_BLOCK;
      addr += 0x400;
    }
    addr += BYTE_PER_DATA * index;
    return addr;
  }

  private static int addr2indexX310( int addr )
  {
    int index = 0;
    addr = addr - ( addr % 8 );
    while ( addr >= 0x400 ) {
      addr -= 0x400;
      index += DATA_PER_BLOCK;
    }
    index += (int)(addr/BYTE_PER_DATA);
    return index;
  }
  
  boolean readX310memory_4byte( int addr )
  {
    mBuffer[0] = (byte) (MemoryOctet.BYTE_PACKET_REPLY);  // 0x38
    mBuffer[1] = (byte) (addr & 0xff);
    mBuffer[2] = (byte) ((addr >> 8) & 0xff);
    try {
      mOut.write(mBuffer, 0, 3);
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );
      mIn.readFully(mBuffer, 0, 8);
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // TDLog.v( "X310 read-memory[1]: " + String.format(" %02x", mBuffer[0] ) );
    } catch (IOException e) {
      TDLog.e("read memory() IO failed");
      return false;
    }
    if (mBuffer[0] != (byte) (MemoryOctet.BYTE_PACKET_REPLY)) return false; // 0x38
    int reply_addr = MemoryOctet.toInt(mBuffer[2], mBuffer[1]);
    if (reply_addr != addr) return false;

    return true;
  }

  // memory layout
  // byte 0-7  first packet
  // byte 8-15 second packet
  // byte 16   hot-flag for the first packet
  // byte 17   hot-flag for the second packet
  //
  // X310 data address space: 0x0000 - 0x7fff
  // each data takes 18 bytes

  /** read a portion of X310 memory
   * @param start   memory FROM index
   * @param end     memory TO index
   * @param data    list of octets, to be filled
   * @return number of octets that have been read (-1 on error)
   */
  // @Override
  public int readX310Memory( int start, int end, List< MemoryOctet > data, IMemoryDialog dialog )
  {
    TDLog.v( "X310 memory start " + start + " end " + end );
    byte[] buffer = new byte[8];
    int cnt = 0;
    int start0 = start;
    Handler handler = new Handler( Looper.getMainLooper() );
    while ( start < end ) {
      MemoryOctet result1 = new MemoryOctet( start );
      MemoryOctet result2 = new MemoryOctet( start ); // vector data

      int addr = index2addrX310( start );
      // TDLog.v( start + " addr " + addr + " end " + end_addr );
      if ( ! readX310memory_4byte( addr )) break;
      result1.data[k  ] = buffer[3];
      result.data[0] &= (byte) 0x7f; // ignore sequence bit HB
      result1.data[k+1] = buffer[4];
      result1.data[k+2] = buffer[5];
      result1.data[k+3] = buffer[6];

      addr = index2addrX310( start ) + 4;
      if ( ! readX310memory_4byte( addr )) break;
      result.data[4] = mBuffer[3];
      result.data[5] = mBuffer[4];
      result.data[6] = mBuffer[5];
      result.data[7] = mBuffer[6];
      
      addr = index2addrX310( start ) + 8; // second packet HB
      if ( ! readX310memory_4byte( addr )) break;
      result2.data[0] = mBuffer[3];
      result2.data[0] &= (byte) 0x7f; // ignore sequence bit HB
      result2.data[1] = mBuffer[4];
      result2.data[2] = mBuffer[5];
      result2.data[3] = mBuffer[6];

      if ( mBuffer[0] == (byte)( MemoryOctet.BYTE_PACKET_REPLY ) && addr == MemoryOctet.toInt( mBuffer[2], mBuffer[1]) ) { // 0x38
        if ( ( mBuffer[3] & MemoryOctet.BIT_BACKSIGHT) == MemoryOctet.BIT_BACKSIGHT ) {
          result.data[0] |= MemoryOctet.BIT_BACKSIGHT2; 
        }
      }
      
      addr = index2addrX310( start ) + 12; // second packet HB
      if ( ! readX310memory_4byte( addr )) break;
      result2.data[4] = mBuffer[3];
      result2.data[5] = mBuffer[4];
      result2.data[6] = mBuffer[5];
      result2.data[7] = mBuffer[6];

      addr = index2addrX310( start ) + 16; // Hot flag bytes
      if ( ! readX310memory_4byte( addr )) break;

      if ( buffer[3] == (byte)( 0xff ) ) result1.data[0] |= (byte)( 0x80 ); 
      data.add( result1 );
      if ( buffer[4] == (byte)( 0xff ) ) result2.data[0] |= (byte)( 0x80 ); 
      data.add( result2 );
        // TDLog.v( "X310 memory " + result2.toString() + " " + buffer[3] );
      ++ cnt;

      if ( dialog != null ) {
        int k1 = start;
        handler.post( new Runnable() {
          public void run() {
            dialog.setIndex( k1 );
          }
        } );
      }
      ++start;
    }
    if ( dialog != null ) {
      int k1 = start0;
      handler.post( new Runnable() {
        public void run() {
          dialog.setIndex( k1 );
        }
      } );
    }
    return cnt;
  }

  // X310 data memory is read-only
  // // return number of memory slots that have been reset
  // public int resetX310Memory( int start, int end )
  // {
  //   int cnt = start;
  //   byte[] buffer = new byte[8];
  //   while ( start < end ) {
  //     int addr = index2addrX310( start ) + 16;
  //     buffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REPLY ); // 0x38
  //     buffer[1] = (byte)( addr & 0xff );
  //     buffer[2] = (byte)( (addr>>8) & 0xff );
  //     TDLog.e( "resetMemory() address " + buffer[1] + " " + buffer[2] );
  // 
  //     // TODO write and read
  //     try {
  //       mOut.write( buffer, 0, 3 );
  //       // if ( TDSetting.mPacketLog ) logPacket3( 1L, buffer );
  //
  //       mIn.readFully( buffer, 0, 8 );
  //       // if ( TDSetting.mPacketLog ) logPacket( 0L );
  //
  //     } catch ( IOException e ) {
  //       TDLog.e( "resetMemory() IO nr. 1 failed" );
  //       break;
  //     }
  //     if ( buffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) || // 0x38
  //          buffer[1] != (byte)( addr & 0xff ) ||
  //          buffer[2] != (byte)( (addr>>8) & 0xff ) ) {
  //       TDLog.e( "resetMemory() bad read reply " + buffer[0] + " addr " + buffer[1] + " " + buffer[2] );
  //       break;
  //     }
  //     TDLog.e( "resetMemory() ok read reply " + buffer[3] + " " + buffer[4] + " " + buffer[5] + " " + buffer[6] );
  // 
  //     buffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REQST ); // 0x39
  //     buffer[1] = (byte)( addr & 0xff );
  //     buffer[2] = (byte)( (addr>>8) & 0xff );
  //     buffer[3] = (byte)( 0xff );
  //     buffer[4] = (byte)( 0xff );
  //     try {
  //       mOut.write( buffer, 0, 7 );
  //       // if ( TDSetting.mPacketLog ) logPacket7( 1L, buffer );
  //
  //       mIn.readFully( buffer, 0, 8 );
  //       // if ( TDSetting.mPacketLog ) logPacket( 0L );
  //
  //     } catch ( IOException e ) {
  //       TDLog.e( "resetMemory() IO nr. 2 failed" );
  //       break;
  //     }
  //     if ( buffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) || // 0x38
  //          buffer[1] != (byte)( addr & 0xff ) ||
  //          buffer[2] != (byte)( (addr>>8) & 0xff ) ) {
  //       TDLog.e( "resetMemory() bad write reply " + buffer[0] + " addr " + buffer[1] + " " + buffer[2] );
  //       break;
  //     }
  //     TDLog.e( "resetMemory() ok write reply " + buffer[3] + " " + buffer[4] + " " + buffer[5] + " " + buffer[6] );
  //     ++ start;
  //   }
  //   return start - cnt;
  // }

  // DRY_RUN
  public void uploadFirmwareDryRun( File fp, TDProgress progress )
  {
    int len = (int)( fp.length() );
    TDLog.v( "X310-proto fw upload: file " + fp.getPath() + " dry run - length " + len );
  }

  // @Override
  public void uploadFirmware( File fp, TDProgress progress )
  {
    if ( ! TDLog.isStreamFile() ) TDLog.setLogStream( TDLog.LOG_FILE ); // set log to file
    // TDLog.t( "Firmware upload: protocol starts. file " + fp.getPath() );
    TDLog.t( "X310-proto fw upload: starts. file " + fp.getPath() );

    long len        = fp.length();
    String filename = fp.getName();
    Resources res   = TDInstance.getResources();
    Handler handler = new Handler();

    // FIXME cannot run on thread because the socket is closed by the DistoX310Comm
    // (new Thread() {
    //   public void run() {
        byte[] buf = new byte[259];
        buf[0] = MemoryOctet.BYTE_PACKET_FW_WRITE; // (byte)0x3b;
        buf[1] = (byte)0;
        buf[2] = (byte)0;

        byte[] buffer = new byte[8];
        String msg; // feedback message
        boolean ok = true;
        int cnt = 0;
        try {
          // File fp = new File( filepath );
          FileInputStream fis = new FileInputStream( fp );
          DataInputStream dis = new DataInputStream( fis );
          // int end_addr = (fp.size() + 255)/256;

          try {
            for ( int addr = 0; /* addr < end_addr */; ++ addr ) {
              TDLog.t( "X310-proto fw upload: block " + addr + " offset " + cnt );
              // memset(buf+3, 0, 256)
              for (int k=0; k<256; ++k) buf[3+k] = (byte)0xff;
              int nr = dis.read( buf, 3, 256 );
              if ( nr <= 0 ) {
                TDLog.t( "X310-proto fw upload: file read failure. Result " + nr );
                break;
              }
              cnt += nr;
              if ( addr >= 0x08 ) {
                buf[0] = MemoryOctet.BYTE_PACKET_FW_WRITE; // (byte)0x3b;
                buf[1] = (byte)( addr & 0xff );
                buf[2] = 0; // not necessary
                TDLog.t( "X310-proto fw upload: write block " + String.format("%02x %02x %02x", buf[0], buf[1], buf[2] ) );
                mOut.write( buf, 0, 259 );
                // if ( TDSetting.mPacketLog ) logPacket8( 1L, buf );

                mIn.readFully( buffer, 0, 8 );
                // if ( TDSetting.mPacketLog ) logPacket( 0L );

                int reply_addr = ( ((int)(buffer[2]))<<8 ) + ((int)(buffer[1]));
                TDLog.t( "X310-proto fw upload: ack " + String.format("%02x", buffer[0]) + " reply-block " + reply_addr ); 
                if ( buffer[0] != MemoryOctet.BYTE_PACKET_FW_WRITE || addr != reply_addr ) {
                  msg = "X310-proto fw upload: fail at offset " + cnt + " buffer[0]: " + buffer[0] + " reply_addr " + reply_addr;
                  TDLog.t( msg );
                  ok = false;
                  break;
                } else {
                  String msg1 = String.format( mContext.getResources().getString( R.string.firmware_uploaded ), "X310", cnt );
                  TDLog.t( msg1 );
                  int cnt1 = cnt;
                  if ( progress != null ) {
                    handler.post( new Runnable() {
                      public void run() {
                        progress.setProgress( cnt1 );
                        progress.setText( msg1 );
                      }
                    } );
                  }
                }
              } else {
                msg = "X310-proto fw upload: skip offset " + cnt;
                TDLog.t( msg );
              }
            }
            fis.close();
          } catch ( EOFException e ) { // OK
            TDLog.t( "X310-proto fw update: EOF " + e.getMessage() );
          } catch ( IOException e ) { 
            TDLog.t( "X310-proto fw update: IO error " + e.getMessage() );
            ok = false;
          }
        } catch ( FileNotFoundException e ) {
          TDLog.t( "X310-proto fw update: Not Found error " + e.getMessage() );
          ok = false;
        }
        msg = "X310-proto fw update: result is " + (ok? "OK" : "FAIL") + " count " + cnt;
        TDLog.t( msg );
        int ret = ( ok ? cnt : -cnt );
        TDLog.v( "Dialog Firmware upload result: written " + ret + " bytes of " + len );

        boolean ok2 = ok;
        String msg2 = ( ret > 0 )? String.format( res.getString(R.string.firmware_file_uploaded), filename, ret, len )
                                 : res.getString(R.string.firmware_file_upload_fail);
        if ( progress != null ) {
          handler.post( new Runnable() {
            public void run() {
              progress.setDone( ok2, msg2  );
            }
          } );
        } else { // run on UI thread
          handler.post( new Runnable() { 
            public void run () { 
              TDToast.makeLong( msg2 );
              if ( TDLog.isStreamFile() ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // set log to system
            }
          } );
        }
    //   }
    // } ).start();
  }

  // @Override
  // public int dumpFirmware( String filepath )
  public void dumpFirmware( File fp, TDProgress progress )
  {
    if ( ! TDLog.isStreamFile() ) TDLog.setLogStream( TDLog.LOG_FILE ); // set log to file
    TDLog.t( "X310-proto fw dump: output file " + fp.getPath() );
    Resources res   = TDInstance.getResources();
    Handler handler = new Handler();

    // FIXME cannot run on thread because the socket is closed by the DistoX310Comm
    // (new Thread() {
    //   public void run() {
        String filename = fp.getName();
        byte[] buf = new byte[256];
        byte[] buffer = new byte[8];
        boolean ok = true;
        int cnt = 0;
        try {
          // TDPath.checkPath( filepath );
          // File fp = new File( filepath );
          FileOutputStream fos = new FileOutputStream( fp );
          DataOutputStream dos = new DataOutputStream( fos );
          try {
            for ( int addr = 0; ; ++ addr ) {
              TDLog.t( "X310-proto fw dump: block " + addr + " offset " + cnt );
              buf[0] = MemoryOctet.BYTE_PACKET_FW_READ; // (byte)0x3a;
              buf[1] = (byte)( addr & 0xff );
              buf[2] = 0; // not necessary
              mOut.write( buf, 0, 3 );
              // if ( TDSetting.mPacketLog ) logPacket3( 1L, buf );

              mIn.readFully( buffer, 0, 8 );
              // if ( TDSetting.mPacketLog ) logPacket( 0L );

              int reply_addr = ( ((int)(buffer[2]))<<8 ) + ((int)(buffer[1]));
              TDLog.t( "X310-proto fw dump: read " + String.format("%02x", buffer[0]) + " reply-block " + reply_addr ); 
              if ( buffer[0] != MemoryOctet.BYTE_PACKET_FW_READ || addr != reply_addr ) {
                TDLog.t( "X310-proto fw dump: fail at offset " + cnt + " buffer[0]: " + buffer[0] + " reply_addr " + reply_addr + " addr " + addr );
                ok = false;
                break;
              // } else {
              //   TDLog.t( "Firmware dump: reply addr ok");
              }

              mIn.readFully( buf, 0, 256 );
              // if ( TDSetting.mPacketLog ) logPacket8( 0L, buf );
              // TDLog.v( "X310 dump firmware[2]: " + String.format(" %02x", buffer[0] ) );

              // boolean last = true;
              // for ( int k=0; last && k<256; ++k ) {
              //   if ( buf[k] != (byte)0xff ) last = false;
              // }
              // if ( last ) break;
              int k = 0; // check there is a byte that is not 0xFF
              for ( ; k<256; ++k ) {
                if ( buf[k] != (byte)0xff ) break;
              }
              if ( k == 256 ) break; // done if all bytes are 0xFF
              dos.write( buf, 0, 256 );
              String msg1 = String.format( mContext.getResources().getString( R.string.firmware_downloaded ), "X310", cnt );
              TDLog.t( msg1 );
              int cnt1 = cnt;
              if ( progress != null ) {
                handler.post( new Runnable() {
                  public void run() {
                    progress.setProgress( cnt1 );
                    progress.setText( msg1 );
                  }
                } );
              }
              cnt += 256;
            }
            fos.close();
          } catch ( EOFException e ) {
            // OK
          } catch ( IOException e ) { 
            ok = false;
          }
        } catch ( FileNotFoundException e ) {
          ok = false;
        }
        TDLog.t( "X310-proto fw dump: result is " + (ok? "OK" : "FAIL") + " count " + cnt );
        int ret = ( ok ? cnt : -cnt );
        boolean ok2 = ok;
        String msg2 = ( ret > 0 )? String.format( res.getString(R.string.firmware_file_downloaded), filename, ret )
                                 : res.getString(R.string.firmware_file_download_fail);
        if ( progress != null ) {
          handler.post( new Runnable() {
            public void run() {
              progress.setDone( ok2, msg2  );
            }
          } );
        } else { // run on UI thread
          handler.post( new Runnable() { 
            public void run () { 
              TDToast.makeLong( msg2 ); 
              if ( TDLog.isStreamFile() ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // reset log stream if necessary
            }
          } );
        }
    //   }
    // } ).start();
  }

  // read a block (256 bytes) of firmware from the DistoX
  // @param nr    block index (starting at 0)
  // @Override
  public byte[] readFirmwareBlock( int nr )
  {
    if ( ! TDLog.isStreamFile() ) TDLog.setLogStream( TDLog.LOG_FILE ); // reset log stream if necessary
    byte[] buf = new byte[256];
    byte[] buffer = new byte[8];
    // boolean ok = true;
    int addr = nr;
    try {
      buf[0] = MemoryOctet.BYTE_PACKET_FW_READ; // (byte)0x3a;
      buf[1] = (byte)( addr & 0xff );
      buf[2] = 0; // not necessary
      TDLog.t("X310-proto fw readbloak: block " + addr );
      mOut.write( buf, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, buf );

      mIn.readFully( buffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // TDLog.v( "X310 dump firmware: " + String.format(" %02x", buffer[0] ) );

      int reply_addr = ( ((int)(buffer[2]))<<8 ) + ((int)(buffer[1]));
      TDLog.t( "X310-proto fw readblock: buffer[0] " + buffer[0] + " reply_addr " + reply_addr );
      if ( buffer[0] != MemoryOctet.BYTE_PACKET_FW_READ || addr != reply_addr ) {
        TDLog.t( "X310-proto fw read block " + nr + ": fail buffer[0]: " + buffer[0] + " reply_addr " + reply_addr );
        // ok = false;
      } else {
        TDLog.t( "X310-proto fw read block " + nr + ": ok");
      }

      mIn.readFully( buf, 0, 256 );
    } catch ( IOException e ) {
      TDLog.t( "X310-proto fw read block IO error " + e.getMessage() );
      // TDLog.e( "X310-proto fw read block IO error " + e.getMessage() );
      // ok = false;
    }
    if ( TDLog.isStreamFile() ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // reset log stream if necessary
    return buf;
  }

}
