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

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
// import java.io.FileOutputStream;
// import java.io.DataOutputStream;
import java.io.IOException;
// import java.io.FileNotFoundException;

class FirmwareUtils 
{

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

  //                                   2.1   2.2   2.3   2.4   2.5  2.5c
  // signatures differ in bytes 7- 6  f834  f83a  f990  fa0a  fb7e  fc10
  //                             -12  08d5  08d5  08d5  08d5  08f5  08d5
  //                           17-16  0c40  0c40  0c50  0c30  0c40  0c48

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
    try {
      // TDLog.Log( TDLog.LOG_IO, "read firmware file " + file.getPath() );
      FileInputStream fis = new FileInputStream( fp );
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
      if ( buf[7] == (byte)0xf8 ) {
        if ( buf[6] == (byte)0x34 && buf[16] == (byte)0x40 && buf[12] == (byte)0xf5 ) {
          return 21;
        } 
        if ( buf[6] == (byte)0x3a && buf[16] == (byte)0x40 && buf[12] == (byte)0xf5 ) {
          return 22;
        }
        return -200;
      } else if ( buf[7] == (byte)0xf9 ) {
        return ( buf[6] == (byte)0x90 && buf[16] == (byte)0x50 && buf[12] == (byte)0xf5 )? 23 : -230;
      } else if ( buf[7] == (byte)0xfa ) {
        return ( buf[6] == (byte)0x0a && buf[16] == (byte)0x30 && buf[12] == (byte)0xf5 )? 24 : -240;
      } else if ( buf[7] == (byte)0xfb ) {
        return ( buf[6] == (byte)0x7e && buf[16] == (byte)0x40 && buf[12] == (byte)0xd5 )? 25 : -250;
      } else if ( buf[7] == (byte)0xfc ) {     
        return ( buf[6] == (byte)0x10 && buf[16] == (byte)0x48 && buf[12] == (byte)0xd5 )? 250 : -2500;
      } else {
        return -7;
      }
    } catch ( IOException e ) {
    }
    return 0;
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
