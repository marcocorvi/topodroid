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
package com.topodroid.dev.distox2;

import com.topodroid.utils.TDLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
// import java.io.FileOutputStream;
// import java.io.DataOutputStream;
import java.io.IOException;
// import java.io.FileNotFoundException;

public class FirmwareUtils
{
  private static final int SIGNATURE_SIZE = 64;

  public static final int HW_NONE    = 0;
  public static final int HW_HEEB    = 1;
  public static final int HW_LANDOLT = 2;

  // ------------------------------------------------------------------------------
  static int getHardware( int fw ) 
  {
    if ( fw == 2100 || fw == 2200 || fw == 2300 || fw == 2400 || fw == 2500 || fw == 2412 || fw == 2501 || fw == 2512 ) return HW_HEEB;
    if ( fw == 2610 || fw == 2630 || fw == 2640 ) return HW_LANDOLT;
    return HW_NONE;
  }

  // say if the file fw code is compatible with some known hardware
  // the real hardware is not known at this point - therefore can only check the firmware file signature
  static boolean isCompatible( int fw )
  {
    return getHardware( fw ) != HW_NONE;
  }

  // try to guess firmware version reading bytes from the file
  // return <= 0 (failure) or one of
  //    2100 2200 2300 2400 2412 2500 2501 2512
  //    2610 2630 2640
  //
  // od -j 2048 -N 64 -x ... <-- HEEB block
  // od -j 4096 -N 64 -x ... <-- LANDOLT block

  public static int readFirmwareFirmware( File fp )
  {
    FileInputStream fis = null;
    try {
      // TDLog.Log( TDLog.LOG_IO, "read firmware file " + fp.getPath() );
      TDLog.v( "X310 read firmware file " + fp.getPath() );
      fis = new FileInputStream( fp );
      DataInputStream dis = new DataInputStream( fis );
      if ( dis.skipBytes( 2048 ) != 2048 ) {
        TDLog.v( "failed skip 2048");
        return 0; // skip 8 bootloader blocks
      }
      byte[] buf = new byte[SIGNATURE_SIZE];
      if ( dis.read( buf, 0, SIGNATURE_SIZE ) != SIGNATURE_SIZE ) {
        TDLog.v( "failed read first block");
        return 0;
      }
      if ( verifySignatureHeeb( buf ) == SIGNATURE_SIZE ) {
        TDLog.v( "HEEB fw " + readFirmwareHeeb( buf ) );
        return readFirmwareHeeb( buf );
      }

      if ( dis.skipBytes( 1984 ) != 1984 ) {
        TDLog.v( "failed skip 1984");
        return 0; // skip 8 bootloader blocks
      }
      if ( dis.read( buf, 0, SIGNATURE_SIZE ) != SIGNATURE_SIZE ) {
        TDLog.v( "failed read second block");
        return 0;
      }
      if ( verifySignatureLandolt( buf ) == SIGNATURE_SIZE ) {
        TDLog.v( "LANDOLT fw " + readFirmwareLandolt( buf ) );
        return readFirmwareLandolt( buf );
      }
    } catch ( IOException e ) {
      TDLog.Error("IO " + e.getMessage() );
    } finally {
      try {
        if ( fis != null ) fis.close();
      } catch ( IOException e ) { TDLog.Error("IO " + e.getMessage() ); }
    }
    return 0;
  }

  // ./utils/firmware_checksum ... <-- provides length and checksum
  //
  public static boolean firmwareChecksum( int fw_version, File fp )
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
      case 2610: len = 25040; break;
      case 2630: len = 25568; break;
      case 2640: len = 25604; break;
    }
    if ( len == 0 ) return false; // bad firmware version
    len /= 4; // number of int to read

    int checksum = 0;
    try {
      // TDLog.Log( TDLog.LOG_IO, "read firmware file " + file.getPath() );
      FileInputStream fis = new FileInputStream( fp );
      DataInputStream dis = new DataInputStream( fis );
      for ( ; len > 0; --len ) {
        checksum ^= dis.readInt();
      }
      fis.close();
    } catch ( IOException e ) {
      // TDLog.v( "check " + fw_version + ": IO exception " + e.getMessage() );
      return false;
    }
    // TDLog.v( "check " + fw_version + ": " + String.format("%08x", checksum) );
    switch ( fw_version ) {
      case 2100: return ( checksum == 0xf58b194b );
      case 2200: return ( checksum == 0x4d66d466 );
      case 2300: return ( checksum == 0x6523596a );
      case 2400: return ( checksum == 0x0f8a2bc1 );
      case 2412: return ( checksum == 0xfddd95a0 ); // continuous
      case 2500: return ( checksum == 0x463c0306 );
      case 2501: return ( checksum == 0x20e9a198 ); // 4-calib data double beep
      case 2512: return ( checksum == 0x1ecb8dc0 ); // continuous
      case 2610: return ( checksum == 0xcae98256 );
      case 2630: return ( checksum == 0x1b1488c5 );
      case 2640: return ( checksum == 0xee2d70ff ); // fixed error in magnetic calib matrix
    }
    return false;
  }

  // check 64 bytes on the device
  // @param signature   256-byte signature block on the DistoX af offset 2048
  static int getDeviceHardware( byte[] signature )
  {
    if ( verifySignatureHeeb( signature ) == SIGNATURE_SIZE ) {
      TDLog.v( "device hw HEEB" );
      return HW_HEEB;
    }
    if ( verifySignatureLandolt( signature ) == SIGNATURE_SIZE ) {
      TDLog.v( "device hw LANDOLT" );
      return HW_LANDOLT;
    }
    return HW_NONE;
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
  //       // TDLog.v( "HARDWARE " + buf[0x40] + " " + buf[0x41] + " " + buf[0x42] + " " + buf[0x43] );
  //       ret = (int)(buf[0x40]) + ((int)(buf[0x41])<<8); // major * 10 + minor
  //       // FIXME  ((int)(buf[0x42]))<<16 + ((int)(buf[0x43]))<<24;
  //     }
  //   } catch ( IOException e ) {
  //     // TODO
  //   }
  //   return ret;
  // }

  // ---------------------------------------------------------------
  // FIRMWARE SIGNATURES
  //    od -x -j 2048 -N 64 <firmware_file>
  // use -j 4096 for 2.6 firmwares
  //
  // Firmware21.bin
  // byte index     1 0   3 2   5 4 ...
  //      0004000  4803  4685  f003 <f834> 4800  4700 <08f5> 0800
  //      0004020 <0c40> 2000  2300  e002  2301  2200  46c0  b5f0
  //      0004040  07db  4e27  f000  f83b  1b00  1b49  4e25  f000
  //      0004060  f835  f000  f834  4e24  f000  f830  1b00  1b49
  // Firmware263.bin
  // byte index     1 0   3 2   5 4 ...
  //      0010000  4803  4685  f000  f8a2  4800  4700 <5da9> 0800
  //      0010020 <13c0> 2000  2300  e002  2301  2200  46c0  b5f0
  //      0010040  07db  4e27  f000  f83b  1b00  1b49  4e25  f000
  //      0010060  f835  f000  f834  4e24  f000  f830  1b00  1b49
  //

  // signature is 64 bytes after the first 2048
  //                                   2.1   2.2   2.3   2.4  2.4c   2.5  2.5c  2.51
  // signatures differ in bytes 7- 6  f834  f83a  f990  fa0a  fe94  fb7e  fc10  f894
  //                             -12    d5    d5    d5    f5    f5    d5    d5    d5
  //                           17-16  0c40  0c40  0c50  0c30  0c38  0c40  0c48  0c40
  static final private byte[] signatureHeeb = {
    (byte)0x03, (byte)0x48, (byte)0x85, (byte)0x46, (byte)0x03, (byte)0xf0, (byte)0x34, (byte)0xf8,
    (byte)0x00, (byte)0x48, (byte)0x00, (byte)0x47, (byte)0xf5, (byte)0x08, (byte)0x00, (byte)0x08,
    (byte)0x40, (byte)0x0c, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x23, (byte)0x02, (byte)0xe0,
    (byte)0x01, (byte)0x23, (byte)0x00, (byte)0x22, (byte)0xc0, (byte)0x46, (byte)0xf0, (byte)0xb5,
    (byte)0xdb, (byte)0x07, (byte)0x27, (byte)0x4e, (byte)0x00, (byte)0xf0, (byte)0x3b, (byte)0xf8,
    (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b, (byte)0x25, (byte)0x4e, (byte)0x00, (byte)0xf0,
    (byte)0x35, (byte)0xf8, (byte)0x00, (byte)0xf0, (byte)0x34, (byte)0xf8, (byte)0x24, (byte)0x4e,
    (byte)0x00, (byte)0xf0, (byte)0x30, (byte)0xf8, (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b
  };

  // signature is 64 bytes after the first 4096
  //                        2.61  2.63  2.63
  //                 13-12  5ba1  5da9  5dcd
  //                   -16    b8    c0    c0
  //
  static final private byte[] signatureLandolt = {
    (byte)0x03, (byte)0x48, (byte)0x85, (byte)0x46, (byte)0x00, (byte)0xf0, (byte)0xa2, (byte)0xf8,
    (byte)0x00, (byte)0x48, (byte)0x00, (byte)0x47, (byte)0xa1, (byte)0x5b, (byte)0x00, (byte)0x08,
    (byte)0xb8, (byte)0x13, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x23, (byte)0x02, (byte)0xe0,
    (byte)0x01, (byte)0x23, (byte)0x00, (byte)0x22, (byte)0xc0, (byte)0x46, (byte)0xf0, (byte)0xb5,
    (byte)0xdb, (byte)0x07, (byte)0x27, (byte)0x4e, (byte)0x00, (byte)0xf0, (byte)0x3b, (byte)0xf8,
    (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b, (byte)0x25, (byte)0x4e, (byte)0x00, (byte)0xf0,
    (byte)0x35, (byte)0xf8, (byte)0x00, (byte)0xf0, (byte)0x34, (byte)0xf8, (byte)0x24, (byte)0x4e,
    (byte)0x00, (byte)0xf0, (byte)0x30, (byte)0xf8, (byte)0x00, (byte)0x1b, (byte)0x49, (byte)0x1b
  };

  // static boolean areCompatible( int hw, int fw )
  // {
  //   switch ( hw ) {
  //     case 10:
  //       return fw == 21 || fw == 22 || fw == 23;
  //   }
  //   // default:
  //   return false;
  // }

  // verify the 64-byte signature block
  // @param buf   signature block at 2048-offset
  private static int verifySignatureHeeb( byte[] buf )
  {
    for ( int k=0; k<SIGNATURE_SIZE; ++k ) {
      if ( k==6 || k==7 || k==12 || k==16 || k==17 ) continue;
      if ( buf[k] != signatureHeeb[k] ) return -k;
    }
    return SIGNATURE_SIZE; // success
  }

  // @param buf   signature block at 4096-offset
  private static int verifySignatureLandolt( byte[] buf )
  {
    for ( int k=0; k<SIGNATURE_SIZE; ++k ) {
      if ( k==12 || k==13 || k==16) continue;
      if ( buf[k] != signatureLandolt[k] ) return -k;
    }
    return SIGNATURE_SIZE; // success
  }


  // guess the firmware version from a 64-byte block read from the file
  //                                   2.1   2.2   2.3   2.4  2.4c   2.5  2.5c  2.51
  // signatures differ in bytes 7- 6  f834  f83a  f990  fa0a  fe94  fb7e  fc10  fb94
  //                             -12  08d5  08d5  08d5  08f5  08f5  08d5  08d5  08d5
  //                           17-16  0c40  0c40  0c50  0c30  0c38  0c40  0c48  0c40
  private static int readFirmwareHeeb( byte[] buf )
  {
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
    }
    return -99; // failed on byte[7]
  }

  //                        261    263    264
  //                 12-13  a1 5b  a9 5d  cd 5d
  //                 16     b8     c0     c0
  private static int readFirmwareLandolt( byte[] buf )
  {
    if ( buf[12] == (byte)0xa1 &&  buf[13] == (byte)0x5b && buf[16] == (byte)0xb8 ) return 2610;
    if ( buf[12] == (byte)0xa9 &&  buf[13] == (byte)0x5d && buf[16] == (byte)0xc0 ) return 2630;
    if ( buf[12] == (byte)0xcd &&  buf[13] == (byte)0x5d && buf[16] == (byte)0xc0 ) return 2640;
    return -99; 
  }

}
