/** @file FirmwareUtils.java
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

class FirmwareUtils 
{

  static final byte[] signature = {
    (byte)0x03, (byte)0x48, (byte)0x85, (byte)0x46, (byte)0x03, (byte)0xf0, (byte)0x34, (byte)0xf8,
    (byte)0x00, (byte)0x48, (byte)0x00, (byte)0x47, (byte)0xf5, (byte)0x08, (byte)0x00, (byte)0x08,
    (byte)0x40, (byte)0x0c, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x23, (byte)0x02, (byte)0xe0,
    (byte)0x01, (byte)0x23, (byte)0x00, (byte)0x22, (byte)0xc0, (byte)0x46, (byte)0xf0, (byte)0xb5,
    (byte)0xdb, (byte)0x07, (byte)0x27, (byte)0x4e, (byte)0x00, (byte)0xf0, (byte)0x3b, (byte)0xf8,
    (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b, (byte)0x25, (byte)0x4e, (byte)0x00, (byte)0xf0,
    (byte)0x35, (byte)0xf8, (byte)0x00, (byte)0xf0, (byte)0x34, (byte)0xf8, (byte)0x24, (byte)0x4e,
    (byte)0x00, (byte)0xf0, (byte)0x30, (byte)0xf8, (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b
  };
  //                                    2.1    2.2    2.3
  // signatures differ in bytes 6-7    f834   f83a   f990
  //                           16-17   0c40   0c40   0c50

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
  // return 0 (failure) or 21, 22, 23
  //
  static int readFirmwareFirmware( File fp )
  {
    try {
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
        if ( k==6 || k==7 || k==16 || k==17 ) continue;
        if ( buf[k] != signature[k] ) return 0;
      }
      if ( buf[7] == (byte)0xf8 ) {
        if ( buf[6] == (byte)0x34 ) {
          return 21;
        } else if ( buf[6] == (byte)0x3a ) {
          return 22;
        }
      } else if ( buf[7] == (byte)0xf9 ) {
        if ( buf[6] == (byte)0x90 ) {
          return 23;
        }
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
