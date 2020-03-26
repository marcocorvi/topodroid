/* @file FirmwareUtils.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid firmware utilities
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
// import java.io.FileOutputStream;
// import java.io.DataOutputStream;
import java.io.IOException;
// import java.io.FileNotFoundException;

class FirmwareUtils 
{
  // sigmature is 64 bytes after the first 2048
  static final private byte[] signature = {
    (byte)0x03, (byte)0x48, (byte)0x85, (byte)0x46, (byte)0x03, (byte)0xf0, (byte)0x34, (byte)0xf8,
    (byte)0x00, (byte)0x48, (byte)0x00, (byte)0x47, (byte)0xf5, (byte)0x08, (byte)0x00, (byte)0x08,
    (byte)0x40, (byte)0x0c, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x23, (byte)0x02, (byte)0xe0,
    (byte)0x01, (byte)0x23, (byte)0x00, (byte)0x22, (byte)0xc0, (byte)0x46, (byte)0xf0, (byte)0xb5,
    (byte)0xdb, (byte)0x07, (byte)0x27, (byte)0x4e, (byte)0x00, (byte)0xf0, (byte)0x3b, (byte)0xf8,
    (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b, (byte)0x25, (byte)0x4e, (byte)0x00, (byte)0xf0,
    (byte)0x35, (byte)0xf8, (byte)0x00, (byte)0xf0, (byte)0x34, (byte)0xf8, (byte)0x24, (byte)0x4e,
    (byte)0x00, (byte)0xf0, (byte)0x30, (byte)0xf8, (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b
  };

  //                                   2.1   2.2   2.3   2.4  2.4c   2.5  2.5c  2.51
  // signatures differ in bytes 7- 6  f834  f83a  f990  fa0a  fe94  fb7e  fc10  f894
  //                             -12  08d5  08d5  08d5  08f5  08f5  08d5  08d5  08d5
  //                           17-16  0c40  0c40  0c50  0c30  0c38  0c40  0c48  0c40

  // static boolean areCompatible( int hw, int fw )
  // {
  //   switch ( hw ) {
  //     case 10:
  //       return fw == 21 || fw == 22 || fw == 23;
  //   }
  //   // default:
  //   return false;
  // }

  // try to guess firmware version reading bytes from the file
  // return <= 0 (failure) or one of 21 22 23 24 25 250
  //
  static int readFirmwareFirmware( File fp )
  {
    FileInputStream fis = null;
    try {
      // TDLog.Log( TDLog.LOG_IO, "read firmware file " + file.getPath() );
      fis = new FileInputStream( fp );
      DataInputStream dis = new DataInputStream( fis );
      if ( dis.skipBytes( 2048 ) != 2048 ) {
        // Log.v("DistoX", "failed skip");
        return 0; // skip 8 bootloader blocks
      }
      byte[] buf = new byte[64];
      if ( dis.read( buf, 0, 64 ) != 64 ) {
        // Log.v("DistoX", "failed read");
        return 0;
      }
      for ( int k=0; k<64; ++k ) {
        // Log.v("DistoX", "byte " + k + " " + buf[k] + " sign " + signature[k] );
        if ( k==6 || k==7 || k==12 || k==16 || k==17 ) continue;
        if ( buf[k] != signature[k] ) return -k;
      }
      //                                   2.1   2.2   2.3   2.4  2.4c   2.5  2.5c  2.51
      // signatures differ in bytes 7- 6  f834  f83a  f990  fa0a  fe94  fb7e  fc10  fb94
      //                             -12  08d5  08d5  08d5  08f5  08f5  08d5  08d5  08d5
      //                           17-16  0c40  0c40  0c50  0c30  0c38  0c40  0c48  0c40

      if ( buf[7] == (byte)0xf8 ) {       // 2.1 2.2
        if ( buf[6] == (byte)0x34 ) {     // 2.1
          return ( buf[16] == (byte)0x40 && buf[12] == (byte)0xf5 )? 2100 : -2100;
        } else if ( buf[6] == (byte)0x3a ) { // 2.2
          return ( buf[16] == (byte)0x40 && buf[12] == (byte)0xf5 )? 2200 : -2200;
        }
        return -200;
      } else if ( buf[7] == (byte)0xf9 ) { // 2.3
        if ( buf[6] == (byte)0x90 ) {
          return (buf[16] == (byte)0x50 && buf[12] == (byte)0xf5 )? 2300 : -2300;
        }
        return -230;
      } else if ( buf[7] == (byte)0xfa ) { // 2.4
        if ( buf[6] == (byte)0x0a ) {
          return ( buf[16] == (byte)0x30 && buf[12] == (byte)0xf5 )? 2400 : -2400;
        }
        return -240;
      } else if ( buf[7] == (byte)0xfb ) { // 2.5  2.51
        if ( buf[6] == (byte)0x7e ) {      // 2.5
          return ( buf[16] == (byte)0x40 && buf[12] == (byte)0xd5 )? 2500 : -2500;
        } else if ( buf[6] == (byte)0x94 ) { // 2.51
          return ( buf[16] == (byte)0x40 && buf[12] == (byte)0xd5 )? 2501 : -2501;
        }
        return -250;
      } else if ( buf[7] == (byte)0xfc ) { // 2.5c
        if ( buf[6] == (byte)0x10 ) {
          return ( buf[16] == (byte)0x48 && buf[12] == (byte)0xd5 )? 2512 : -2512;
        }
        return -256;
      } else if ( buf[7] == (byte)0xfe ) { // 2.4c
        if ( buf[6] == (byte)0x94 ) {
          return ( buf[16] == (byte)0x38 && buf[12] == (byte)0xf5 )? 2412 : -2412;
        }
        return -246;
      } else {
        return -27; // failed on byte[7]
      }
    } catch ( IOException e ) {
    } finally {
      try {
        if ( fis != null ) fis.close();
      } catch ( IOException e ) { }
    }
    return 0;
  }

  static boolean firmwareChecksum( int fw_version, File fp )
  {
    int len = 0;
    switch ( fw_version ) {
      case 2100: len = 15220; break;
      case 2200: len = 15232; break;
      case 2300: len = 15952; break;
      case 2400: len = 16224; break;
      case 2500: len = 17496; break;
      case 2412: len = 16504; break;
      case 2501: len = 17540; break;
      case 2512: len = 17792; break;
    }
    if ( len == 0 ) return false; // bad formware version
    len /= 4; // number of int to read

    int check = 0;
    try {
      // TDLog.Log( TDLog.LOG_IO, "read firmware file " + file.getPath() );
      FileInputStream fis = new FileInputStream( fp );
      DataInputStream dis = new DataInputStream( fis );
      for ( ; len > 0; --len ) {
        check ^= dis.readInt();
      }
      fis.close();
    } catch ( IOException e ) {
      // Log.v("DistoX-FW", "check " + fw_version + ": IO exception " + e.getMessage() );
      return false;
    }
    // Log.v("DistoX-FW", "check " + fw_version + ": " + String.format("%08x", check) );
    switch ( fw_version ) {
      case 2100: return ( check == 0xf58b194b );
      case 2200: return ( check == 0x4d66d466 );
      case 2300: return ( check == 0x6523596a );
      case 2400: return ( check == 0x0f8a2bc1 );
      case 2412: return ( check == 0xfddd95a0 ); // continuous
      case 2500: return ( check == 0x463c0306 );
      case 2501: return ( check == 0x20e9a198 ); // 4-calib data double beep
      case 2512: return ( check == 0x1ecb8dc0 ); // continuous
    }
    return false;
  }

  // static int readFirmwareAddress( DataInputStream  dis, DataOutputStream dos )
  // {
  //   int ret = -1;
  //   byte[] buf = new byte[256];
  //   try {
  //     int addr = 0;
  //     buf[0] = (byte)0x3a;
  //     buf[1] = (byte)( addr & 0xff );
  //     buf[2] = 0; // not necessary
  //     dos.write( buf, 0, 3 );
  //     dis.readFully( mBuffer, 0, 8 );
  //     int reply_addr = ( ((int)(mBuffer[2]))<<8 ) + ((int)(mBuffer[1]));
  //     if ( mBuffer[0] == (byte)0x3a && addr == reply_addr ) {
  //       dis.readFully( buf, 0, 256 );
  //       // Log.v("DistoX", "HARDWARE " + buf[0x40] + " " + buf[0x41] + " " + buf[0x42] + " " + buf[0x43] );
  //       ret = (int)(buf[0x40]) + ((int)(buf[0x41])<<8); // major * 10 + minor
  //       // FIXME  ((int)(buf[0x42]))<<16 + ((int)(buf[0x43]))<<24; 
  //     }
  //   } catch ( IOException e ) { 
  //     // TODO
  //   }
  //   return ret;
  // }

}
