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
    // TDLog.v( "X310 memory start " + start + " end " + end );
    int cnt = 0;
    int start0 = start;
    Handler handler = new Handler( Looper.getMainLooper() );
    while ( start < end ) {
      MemoryOctet result = new MemoryOctet( start );
      // MemoryOctet result2 = new MemoryOctet( start ); // vector data
      // read only bytes 0-7 and 16-17
      int k = 0;
      int addr = index2addrX310( start );
      int end_addr = addr + BYTE_PER_DATA;
      // TDLog.v( start + " addr " + addr + " end " + end_addr );
      for ( ; addr < end_addr && k < 8; addr += 4, k+=4 ) {
        mBuffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REPLY );  // 0x38
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        try {
          mOut.write( mBuffer, 0, 3 );
          // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

          mIn.readFully( mBuffer, 0, 8 );
          // if ( TDSetting.mPacketLog ) logPacket( 0L );
          // TDLog.v( "X310 read-memory[1]: " + String.format(" %02x", mBuffer[0] ) );
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
      // vector packet - need only the first byte
      // k = 0;
      addr = index2addrX310( start ) + 8;
      // end_addr = addr + BYTE_PER_DATA;
      // for ( ; addr < end_addr && k < 8; addr += 4, k+=4 ) {
        mBuffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REPLY ); // 0x38
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        try {
          mOut.write( mBuffer, 0, 3 );
          // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

          mIn.readFully( mBuffer, 0, 8 );
          // if ( TDSetting.mPacketLog ) logPacket( 0L );
          // TDLog.v( "X310 read-memory[2]: " + String.format(" %02x", mBuffer[0] ) );
        } catch ( IOException e ) {
          TDLog.e( "read memory() IO failed" );
          break;
        }
      //   if ( mBuffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) ) break; // 0x38
      //   int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
      //   if ( reply_addr != addr ) break;
      //   // for (int i=3; i<7; ++i) result.data[k+i-3] = mBuffer[i];
      //   result2.data[k  ] = mBuffer[3];
      //   result2.data[k+1] = mBuffer[4];
      //   result2.data[k+2] = mBuffer[5];
      //   result2.data[k+3] = mBuffer[6];
      // }
      if ( mBuffer[0] == (byte)( MemoryOctet.BYTE_PACKET_REPLY ) && addr == MemoryOctet.toInt( mBuffer[2], mBuffer[1]) ) { // 0x38
        if ( ( mBuffer[3] & MemoryOctet.BIT_BACKSIGHT) == MemoryOctet.BIT_BACKSIGHT ) {
          result.data[0] |= MemoryOctet.BIT_BACKSIGHT2;
        }
      }

      if ( k == 8 ) {
        addr = index2addrX310( start ) + 16;
        mBuffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REPLY ); // 0x38
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        try {
          mOut.write( mBuffer, 0, 3 );
          // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );

          mIn.readFully( mBuffer, 0, 8 );
          // if ( TDSetting.mPacketLog ) logPacket( 0L );
          // TDLog.v( "X310 read-memory[3]: " + String.format(" %02x", mBuffer[0] ) );
        } catch ( IOException e ) {
          TDLog.e( "read memory() IO failed" );
          break;
        }
        if ( mBuffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) ) break; // 0x38
        if ( mBuffer[3] == (byte)( 0xff ) ) result.data[0] |= (byte)( 0x80 ); 
        data.add( result );
        // if ( mBuffer[4] == (byte)( 0xff ) ) result2.data[0] |= (byte)( 0x80 ); 
        // data.add( result2 );
        // TDLog.v( "X310 memory " + result.toString() + " " + mBuffer[3] );
        ++ cnt;
      } else {
        break;
      }
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
  //   while ( start < end ) {
  //     int addr = index2addrX310( start ) + 16;
  //     mBuffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REPLY ); // 0x38
  //     mBuffer[1] = (byte)( addr & 0xff );
  //     mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //     TDLog.e( "resetMemory() address " + mBuffer[1] + " " + mBuffer[2] );
  // 
  //     // TODO write and read
  //     try {
  //       mOut.write( mBuffer, 0, 3 );
  //       // if ( TDSetting.mPacketLog ) logPacket3( 1L, mBuffer );
  //
  //       mIn.readFully( mBuffer, 0, 8 );
  //       // if ( TDSetting.mPacketLog ) logPacket( 0L );
  //
  //     } catch ( IOException e ) {
  //       TDLog.e( "resetMemory() IO nr. 1 failed" );
  //       break;
  //     }
  //     if ( mBuffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) || // 0x38
  //          mBuffer[1] != (byte)( addr & 0xff ) ||
  //          mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) {
  //       TDLog.e( "resetMemory() bad read reply " + mBuffer[0] + " addr " + mBuffer[1] + " " + mBuffer[2] );
  //       break;
  //     }
  //     TDLog.e( "resetMemory() ok read reply " + mBuffer[3] + " " + mBuffer[4] + " " + mBuffer[5] + " " + mBuffer[6] );
  // 
  //     mBuffer[0] = (byte)( MemoryOctet.BYTE_PACKET_REQST ); // 0x39
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
  //       TDLog.e( "resetMemory() IO nr. 2 failed" );
  //       break;
  //     }
  //     if ( mBuffer[0] != (byte)( MemoryOctet.BYTE_PACKET_REPLY ) || // 0x38
  //          mBuffer[1] != (byte)( addr & 0xff ) ||
  //          mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) {
  //       TDLog.e( "resetMemory() bad write reply " + mBuffer[0] + " addr " + mBuffer[1] + " " + mBuffer[2] );
  //       break;
  //     }
  //     TDLog.e( "resetMemory() ok write reply " + mBuffer[3] + " " + mBuffer[4] + " " + mBuffer[5] + " " + mBuffer[6] );
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
    // TDLog.f( "Firmware upload: protocol starts. file " + fp.getPath() );
    TDLog.v( "X310-proto fw upload: starts. file " + fp.getPath() );
    boolean is_log_file = TDLog.isStreamFile();
    if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_FILE ); // set log to file if necessary

    long len        = fp.length();
    String filename = fp.getName();
    Resources res   = TDInstance.getResources();
    Handler handler = new Handler();

    (new Thread() {
      public void run() {
        byte[] buf = new byte[259];
        buf[0] = MemoryOctet.BYTE_PACKET_FW_WRITE; // (byte)0x3b;
        buf[1] = (byte)0;
        buf[2] = (byte)0;

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
              TDLog.f( "X310-proto fw upload: addr " + addr + " count " + cnt );
              // memset(buf+3, 0, 256)
              for (int k=0; k<256; ++k) buf[3+k] = (byte)0xff;
              int nr = dis.read( buf, 3, 256 );
              if ( nr <= 0 ) {
                TDLog.f( "X310-proto fw upload: file read failure. Result " + nr );
                break;
              }
              cnt += nr;
              if ( addr >= 0x08 ) {
                buf[0] = MemoryOctet.BYTE_PACKET_FW_WRITE; // (byte)0x3b;
                buf[1] = (byte)( addr & 0xff );
                buf[2] = 0; // not necessary
                mOut.write( buf, 0, 259 );
                // if ( TDSetting.mPacketLog ) logPacket8( 1L, buf );

                mIn.readFully( mBuffer, 0, 8 );
                // if ( TDSetting.mPacketLog ) logPacket( 0L );
                // TDLog.v( "X310 upload firmware: " + String.format(" %02x", mBuffer[0] ) );

                int reply_addr = ( ((int)(mBuffer[2]))<<8 ) + ((int)(mBuffer[1]));
                if ( mBuffer[0] != MemoryOctet.BYTE_PACKET_FW_WRITE || addr != reply_addr ) {
                  msg = "X310-proto fw upload: fail at " + cnt + " buffer[0]: " + mBuffer[0] + " reply_addr " + reply_addr;
                  TDLog.f( msg );
                  ok = false;
                  break;
                } else {
                  String msg1 = String.format( mContext.getResources().getString( R.string.firmware_uploaded ), "XBLE", cnt );
                  TDLog.f( msg1 );
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
                msg = "X310-proto fw upload: skip " + cnt;
                TDLog.f( msg );
              }
            }
            fis.close();
          } catch ( EOFException e ) { // OK
            TDLog.f( "X310-proto fw update: EOF " + e.getMessage() );
          } catch ( IOException e ) { 
            TDLog.f( "X310-proto fw update: IO error " + e.getMessage() );
            ok = false;
          }
        } catch ( FileNotFoundException e ) {
          TDLog.f( "X310-proto fw update: Not Found error " + e.getMessage() );
          ok = false;
        }
        msg = "X310-proto fw update: result is " + (ok? "OK" : "FAIL") + " count " + cnt;
        TDLog.f( msg );
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
            public void run () { TDToast.makeLong( msg2 ); }
          } );
        }
      }
    } ).start();
    if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // reset log stream if necessary
  }

  // @Override
  // public int dumpFirmware( String filepath )
  public void dumpFirmware( File fp, TDProgress progress )
  {
    // TDLog.f( "Proto Firmware dump: output filepath " + fp.getPath() );
    TDLog.v( "X310-proto fw dump: output file " + fp.getPath() );
    Resources res   = TDInstance.getResources();
    Handler handler = new Handler();

    (new Thread() {
      public void run() {
        String filename = fp.getName();
        byte[] buf = new byte[256];
        boolean ok = true;
        int cnt = 0;
        try {
          // TDPath.checkPath( filepath );
          // File fp = new File( filepath );
          FileOutputStream fos = new FileOutputStream( fp );
          DataOutputStream dos = new DataOutputStream( fos );
          try {
            for ( int addr = 0; ; ++ addr ) {
            // TDLog.f( "Firmware dump: addr " + addr + " count " + cnt );
              buf[0] = MemoryOctet.BYTE_PACKET_FW_READ; // (byte)0x3a;
              buf[1] = (byte)( addr & 0xff );
              buf[2] = 0; // not necessary
              mOut.write( buf, 0, 3 );
              // if ( TDSetting.mPacketLog ) logPacket3( 1L, buf );

              mIn.readFully( mBuffer, 0, 8 );
              // if ( TDSetting.mPacketLog ) logPacket( 0L );
              // TDLog.v( "X310 dump firmware: " + String.format(" %02x", mBuffer[0] ) );

              int reply_addr = ( ((int)(mBuffer[2]))<<8 ) + ((int)(mBuffer[1]));
              if ( mBuffer[0] != MemoryOctet.BYTE_PACKET_FW_READ || addr != reply_addr ) {
                TDLog.f( "X310-proto fw dump: fail at " + cnt + " buffer[0]: " + mBuffer[0] + " reply_addr " + reply_addr + " addr " + addr );
                ok = false;
                break;
              // } else {
              //   TDLog.f( "Firmware dump: reply addr ok");
              }

              mIn.readFully( buf, 0, 256 );
              // if ( TDSetting.mPacketLog ) logPacket8( 0L, buf );
              // TDLog.v( "X310 dump firmware[2]: " + String.format(" %02x", mBuffer[0] ) );

              // boolean last = true;
              // for ( int k=0; last && k<256; ++k ) {
              //   if ( buf[k] != (byte)0xff ) last = false;
              // }
              // if ( last ) break;
              int k = 0; // check there is a byte that is not 0xFF
              for ( ; k<256; ++k ) {
                if ( buf[k] != (byte)0xff ) break;
              }
              if ( k == 256 ) break;
              dos.write( buf, 0, 256 );
              String msg1 = String.format( mContext.getResources().getString( R.string.firmware_downloaded ), "X310", cnt );
              TDLog.f( msg1 );
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
        // TDLog.f( "Proto Firmware dump: result is " + (ok? "OK" : "FAIL") + " count " + cnt );
        TDLog.v( "X310-proto fw dump: result is " + (ok? "OK" : "FAIL") + " count " + cnt );
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
            public void run () { TDToast.makeLong( msg2 ); }
          } );
        }
      }
    } ).start();
    // if ( ! is_log_file ) TDLog.setLogStream( TDLog.LOG_SYSLOG ); // reset log stream if necessary
  }

  // read a block (256 bytes) of firmware from the DistoX
  // @param nr    block index (starting at 0)
  // @Override
  public byte[] readFirmwareBlock( int nr )
  {
    byte[] buf = new byte[256];
    // boolean ok = true;
    int addr = nr;
    try {
      buf[0] = MemoryOctet.BYTE_PACKET_FW_READ; // (byte)0x3a;
      buf[1] = (byte)( addr & 0xff );
      buf[2] = 0; // not necessary
      mOut.write( buf, 0, 3 );
      // if ( TDSetting.mPacketLog ) logPacket3( 1L, buf );

      mIn.readFully( mBuffer, 0, 8 );
      // if ( TDSetting.mPacketLog ) logPacket( 0L );
      // TDLog.v( "X310 dump firmware: " + String.format(" %02x", mBuffer[0] ) );

      int reply_addr = ( ((int)(mBuffer[2]))<<8 ) + ((int)(mBuffer[1]));
      if ( mBuffer[0] != MemoryOctet.BYTE_PACKET_FW_READ || addr != reply_addr ) {
        TDLog.f( "Firmware read block " + nr + ": fail buffer[0]: " + mBuffer[0] + " reply_addr " + reply_addr );
        // ok = false;
      } else {
        TDLog.f( "Firmware read block " + nr + ": ok");
      }

      mIn.readFully( buf, 0, 256 );
    } catch ( IOException e ) {
      TDLog.e("IO " + e.getMessage() );
      // ok = false;
    }
    return buf;
  }

}
