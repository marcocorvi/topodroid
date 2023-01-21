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
  public static final int HW_TIAN    = 3;

  private static final int OFFSET_TIAN    = 0;
  private static final int OFFSET_HEEB    = 2048;
  private static final int OFFSET_LANDOLT = 4096;


  // ------------------------------------------------------------------------------
  /** @return compatible hardware type for a given firmware
   * @param fw  firmware number
   */
  static int getHardware( int fw ) 
  {
    if ( fw == 2100 || fw == 2200 || fw == 2300 || fw == 2400 || fw == 2500 || fw == 2412 || fw == 2501 || fw == 2512 ) return HW_HEEB;
    if ( fw == 2610 || fw == 2630 || fw == 2640 ) return HW_LANDOLT;
    if ( fw == 2700 || fw == 2701 ) return HW_TIAN;
    return HW_NONE;
  }

  /** say if the file fw code is compatible with some known hardware
   * the real hardware is not known at this point - therefore can only check the firmware file signature
   * @return true if the hardware type is known
   */
  static boolean isCompatible( int fw )
  {
    return getHardware( fw ) != HW_NONE;
  }

  /** try to guess firmware version reading bytes from the firmware file
   * @param fp   firmware file
   * @return <= 0 (failure) or one of the known firmware codes:
   *    2100 2200 2300 2400 2412 2500 2501 2512
   *    2610 2630 2640
   *    2700 2701
   *
   * od -j 2048 -N 64 -x ... <-- HEEB block
   * od -j 4096 -N 64 -x ... <-- LANDOLT block
   * od -j    0 -N 64 -x ... <-- TIAN block
   */
  public static int readFirmwareFirmware( File fp )
  {
    FileInputStream fis = null;
    try {
      // TDLog.Log( TDLog.LOG_IO, "read firmware file " + fp.getPath() );
      TDLog.v( "X310 read firmware file " + fp.getPath() );
      fis = new FileInputStream( fp );
      DataInputStream dis = new DataInputStream( fis );

      int offset = 0; // offset where signature block is read
      int skip = 0;   // bytes to skip in order to reach the offset

      // SIWEI FIXME
      skip = OFFSET_TIAN - offset; // already assigned
      if ( dis.skipBytes( skip ) != skip ) {
        TDLog.v( "failed tian skip");
        return 0; // skip 8 bootloader blocks
      }
      offset += skip;
      byte[] buf = new byte[SIGNATURE_SIZE];
      if ( dis.read( buf, 0, SIGNATURE_SIZE ) != SIGNATURE_SIZE ) {
        TDLog.v( "failed tian read");
        return 0;
      }
      if ( verifySignatureTian( buf ) == SIGNATURE_SIZE ) {
        // TDLog.v( "TIAN fw " + readFirmwareTian( buf ) );
        return readFirmwareTian( buf );
      }
      offset += SIGNATURE_SIZE;

      skip = OFFSET_HEEB - offset; // 1984 = 2048 - (0 + 64)
      if ( dis.skipBytes( skip ) != skip ) {  // skip to 2048: heeb bootloader blocks
        TDLog.v( "failed heeb skip");
        return 0;
      }
      if ( dis.read( buf, 0, SIGNATURE_SIZE ) != SIGNATURE_SIZE ) {
        TDLog.v( "failed heeb read");
        return 0;
      }
      if ( verifySignatureHeeb( buf ) == SIGNATURE_SIZE ) {
        // TDLog.v( "HEEB fw " + readFirmwareHeeb( buf ) );
        return readFirmwareHeeb( buf );
      }
      offset += SIGNATURE_SIZE;

      skip = OFFSET_LANDOLT - offset; // 1984 = 4096 - (2048 + 64)
      if ( dis.skipBytes( skip ) != skip ) { // skip to 4096: landolt bootloader blocks
        TDLog.v( "failed landolt skip");
        return 0; 
      }
      if ( dis.read( buf, 0, SIGNATURE_SIZE ) != SIGNATURE_SIZE ) {
        TDLog.v( "failed landolt read");
        return 0;
      }
      if ( verifySignatureLandolt( buf ) == SIGNATURE_SIZE ) {
        // TDLog.v( "LANDOLT fw " + readFirmwareLandolt( buf ) );
        return readFirmwareLandolt( buf );
      }
    } catch ( IOException e ) {
      TDLog.e("IO " + e.getMessage() );
    } finally {
      try {
        if ( fis != null ) fis.close();
      } catch ( IOException e ) { TDLog.e("IO " + e.getMessage() ); }
    }
    return 0;
  }

  /** check the firmware checksum
   * @param fw_version   firmware version
   * @param fp           firmware file
   * @return true if the checksum passed
   *
   * ./utils/firmware_checksum ... <-- provides length and checksum
   */
  public static boolean firmwareChecksum( int fw_version, File fp )
  {
    TDLog.v( "FW check version " + fw_version );
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
      case 2700: len = 15732; break; // 20221016
      case 2701: len = 15828; break; // 20221029
    }
    if ( len == 0 ) return false; // bad firmware version
    len /= 4; // number of int to read

    int checksum = 0;
    try {
      TDLog.v( "FW read firmware file " + fp.getPath() );
      FileInputStream fis = new FileInputStream( fp );
      DataInputStream dis = new DataInputStream( fis );
      for ( ; len > 0; --len ) {
        checksum ^= dis.readInt();
      }
      fis.close();
    } catch ( IOException e ) {
      TDLog.v( "check " + fw_version + ": IO exception " + e.getMessage() );
      return false;
    }
    TDLog.v( "FW check " + fw_version + " checksum: " + String.format("%08x", checksum) );
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
      case 2700: return ( checksum == 0x4ec030fa ); // 20221016
      case 2701: return ( checksum == 0x2ec0c11b ); // 20221029
    }
    return false;
  }

  /** check 64 bytes on the device
   * @param signature   256-byte signature block of the firmware on the DistoX 
   * @return hardware type
   * @note the block is read by TopoDroidApp.readFirmwareSignature()
   */
  static int getDeviceHardwareSignature( byte[] signature )
  {
    if ( verifySignatureHeeb( signature ) == SIGNATURE_SIZE ) {
      TDLog.v( "device hw HEEB" );
      return HW_HEEB;
    }
    if ( verifySignatureLandolt( signature ) == SIGNATURE_SIZE ) {
      TDLog.v( "device hw LANDOLT" );
      return HW_LANDOLT;
    }
    if ( verifyHardwareSignatureTian( signature ) == SIGNATURE_SIZE ) {
      TDLog.v( "device hw TIAN" );
      return HW_TIAN;
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

  /** HEEB signature is 64 bytes after the first 2048
   *                                   2.1   2.2   2.3   2.4  2.4c   2.5  2.5c  2.51
   * signatures differ in bytes 7- 6  f834  f83a  f990  fa0a  fe94  fb7e  fc10  f894
   *                             -12    d5    d5    d5    f5    f5    d5    d5    d5
   *                           17-16  0c40  0c40  0c50  0c30  0c38  0c40  0c48  0c40
   */
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

  /** LANDOLT signature is 64 bytes after the first 4096
   *                        2.61  2.63  2.63
   *                 13-12  5ba1  5da9  5dcd
   *                   -16    b8    c0    c0
   */
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

  /** TIAN signature is 64 bytes after the first ????
   */
  static final private byte[] signatureTian = { // 20222007
    (byte)0x03, (byte)0x48, (byte)0x85, (byte)0x46, (byte)0x03, (byte)0xf0, (byte)0xd8, (byte)0xfb,
    (byte)0x00, (byte)0x48, (byte)0x00, (byte)0x47, (byte)0xd5, (byte)0x08, (byte)0x00, (byte)0x08,
    (byte)0x60, (byte)0x0c, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x23, (byte)0x02, (byte)0xe0,
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

  /** verify the 64-byte HEEB signature block
   * @param buf   signature block at 2048-offset, either in the firmware file or in the firmware on the device
   * @return neg of failed byte or signature size
   */
  private static int verifySignatureHeeb( byte[] buf )
  {
    for ( int k=0; k<SIGNATURE_SIZE; ++k ) {
      if ( k==6 || k==7 || k==12 || k==16 || k==17 ) continue;
      if ( buf[k] != signatureHeeb[k] ) return -k;
    }
    return SIGNATURE_SIZE; // success
  }

  /** verify the 64-byte LANDOLT signature block
   * @param buf   signature block at 4096-offset, either in the firmware file or in the firmware on the device
   * @return neg of failed byte or signature size
   */
  private static int verifySignatureLandolt( byte[] buf )
  {
    for ( int k=0; k<SIGNATURE_SIZE; ++k ) {
      if ( k==12 || k==13 || k==16) continue;
      if ( buf[k] != signatureLandolt[k] ) return -k;
    }
    return SIGNATURE_SIZE; // success
  }

  /** verify the 64-byte TIAN signature block
   * @param buf   signature block at ????-offset, either in the firmware file 
   * @return neg of failed byte or signature size
   */
  private static int verifySignatureTian( byte[] buf )
  {
    // FIXME: a check on only 2 bytes should not be sufficient for a signature validation
    // if ( buf[0] == 0x0D && buf[1] == 0x00 )       // signature read from hardware
    //   return SIGNATURE_SIZE;
    for ( int k=0; k<SIGNATURE_SIZE; ++k ) {      // signature read from firmware
      if ( k==6 || k==7 || k==12 || k==16 || k==17 ) continue;
      if ( buf[k] != signatureTian[k] ) {
        TDLog.v("FW signature Tian fail at " + k + " found: " + buf[k] );
        return -k;
      }
    }
    TDLog.v("FW signature Tian OK");
    return SIGNATURE_SIZE; // success
  }

  /** verify the 2-byte TIAN hardware signature
   * @param buf   signature code
   * @return neg of failed byte or signature size on success
   * @note the signature code must be read from the device
   */
  private static int verifyHardwareSignatureTian( byte[] buf )
  {
    //reply from command 0x3c, the hardware version 1.3
    if ( buf[0] == 0x0D && buf[1] == 0x00 )       // signature read from hardware
       return SIGNATURE_SIZE;     //valid
    return -1; // invalid
  }

  /** @return the code of the HEEB firmware
   * @param buf   signature block
   *
   * guess the firmware version from a 64-byte block read from the file
   *                                   2.1   2.2   2.3   2.4  2.4c   2.5  2.5c  2.51
   * signatures differ in bytes 7- 6  f834  f83a  f990  fa0a  fe94  fb7e  fc10  fb94
   *                             -12  08d5  08d5  08d5  08f5  08f5  08d5  08d5  08d5
   *                           17-16  0c40  0c40  0c50  0c30  0c38  0c40  0c48  0c40
   */
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

  /** @return the code of the LANDOLT firmware
   * @param buf   signature block
   *
   *                        261    263    264
   *                 12-13  a1 5b  a9 5d  cd 5d
   *                 16     b8     c0     c0
   */
  private static int readFirmwareLandolt( byte[] buf )
  {
    if ( buf[12] == (byte)0xa1 &&  buf[13] == (byte)0x5b && buf[16] == (byte)0xb8 ) return 2610;
    if ( buf[12] == (byte)0xa9 &&  buf[13] == (byte)0x5d && buf[16] == (byte)0xc0 ) return 2630;
    if ( buf[12] == (byte)0xcd &&  buf[13] == (byte)0x5d && buf[16] == (byte)0xc0 ) return 2640;
    return -99; 
  }

  /** @return the code of the TIAN firmware
   * @param buf   signature block
   */
  private static int readFirmwareTian( byte[] buf )
  {
    //            1 0  3 2  5 4  7 6  9 8  b a  d c  f e
    // 20221016: 4803 4685 f003 fc08 4800 4700 08d5 0800
    // 20221029: 4803 4685 f003 fc38 4800 4700 08d5 0800
    if ( buf[4] == (byte)0x03 && buf[5] == (byte)0xf0 && buf[6] == (byte)0x08 && buf[7] == (byte)0xfc ) return 2700; // 20221016
    if ( buf[4] == (byte)0x03 && buf[5] == (byte)0xf0 && buf[6] == (byte)0x38 && buf[7] == (byte)0xfc ) return 2701; // 20221029
    return -99; // failed on byte[7]
  }
}
