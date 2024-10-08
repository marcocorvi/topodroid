/* @file MemoryOctet.java
 *
 * @author marco corvi
 * @date 201311
 *
 * @brief 8-byte data in the distox memory
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.packetX;

import com.topodroid.prefs.TDSetting;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;
import com.topodroid.utils.TDUtil;

public class MemoryOctet
{
  private int index; // memory index
  // A3:   index = address/8
  // X310: index = 56*(address/1024) + (address%1024)/18

  public byte[] data;
  public static final byte BIT_BACKSIGHT2 = 0x20;
  public static final byte BIT_BACKSIGHT  = 0x40; // backsight bit of vector packet

  public static final byte BYTE_PACKET_DATA   = 0x01;
  public static final byte BYTE_PACKET_G      = 0x02;
  public static final byte BYTE_PACKET_M      = 0x03;
  public static final byte BYTE_PACKET_VECTOR = 0x04;
  public static final byte BYTE_PACKET_REPLY  = 0x38;
  public static final byte BYTE_PACKET_REQST  = 0x39;
  // public static final byte BYTE_PACKET_38     = 0x38;
  // public static final byte BYTE_PACKET_39     = 0x39;
  public static final byte BYTE_PACKET_FW_READ  = 0x3a;
  public static final byte BYTE_PACKET_FW_WRITE = 0x3b;
  public static final byte BYTE_PACKET_HW_CODE  = 0x3c;
  public static final byte BYTE_PACKET_3D     = 0x3d;
  public static final byte BYTE_PACKET_3E     = 0x3e;

  public static final int SIZE = 8;

  // ------------------------------------------------------------

  private static double toDistance( byte b0, byte b1, byte b2 )
  {
    int dhh = (int)( b0 & 0x40 );
    double d =  dhh * 1024.0 + toInt( b2, b1 );
    if ( d < 99999 ) {
      return d / 1000.0;
    }
    return 100 + (d-100000) / 100.0;
  }

  private static double toAzimuth( byte b1, byte b2 ) // b1 low, b2 high
  {
    int b = toInt( b2, b1 );
    return b * 180.0 / 32768.0; // 180/0x8000;
  }

  private static double toClino( byte b1, byte b2 ) // b1 low, b2 high
  {
    int c = toInt( b2, b1 );
    double cc = c * 90.0  / 16384.0; // 90/0x4000;
    if ( c >= 32768 ) { cc = (65536 - c) * (-90.0) / 16384.0; }
    return cc;
  }

  public static int toInt( byte b ) 
  {
    int ret = (int)(b & 0xff);
    if ( ret < 0 ) ret += 256; // always false
    return ret;
  }

  public static int toInt( byte bh, byte bl )
  {
    int h = (int)(bh & 0xff);   // high
    if ( h < 0 ) h += 256; // always false
    int l = (int)(bl & 0xff);   // low
    if ( l < 0 ) l += 256; // always false
    return (h * 256 + l);
  }

  public static long toLong( byte b3, byte b2, byte b1, byte b0 )
  {
    long l3 = (long)(b3 & 0xff);   // high
    long l2 = (long)(b2 & 0xff);   // low
    long l1 = (long)(b1 & 0xff);
    long l0 = (long)(b0 & 0xff);
    return (l3 << 24 | l2 << 16 | l1 << 8 | l0);
  }
  // ------------------------------------------------------------

  public MemoryOctet( int idx )
  {
    index = idx;
    data  = new byte[SIZE];
  }

  public void printHexString( PrintWriter pw )
  {
    boolean hot  = (int)( data[0] & 0x80 ) == 0x80; // test hot bit
    pw.format( "%4d %c %02x %02x %02x %02x %02x %02x %02x %02x",
               index, hot? '?' : '>',
               data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7] );
  }

  public String toString() 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );

    if ( data[0] == 0xff ) {
      pw.format("%4d ! invalid data", index );
    } else {
  
      boolean hot  = (int)( data[0] & 0x80 ) == 0x80; // test hot bit
      int type = (int)( data[0] & 0x0f ); // bits 0-5 but here check only 0-3 because bit 5 = backsight
      switch ( type ) {
        case 0x01:
          double dd = toDistance( data[0], data[1], data[2] );
          double bb = toAzimuth( data[3], data[4] );
          double cc = toClino( data[5], data[6] );
          double rr = toAzimuth( (byte) 0, data[7] ); // rotate HB
          if ( (data[0] & BIT_BACKSIGHT2) == BIT_BACKSIGHT2 ) {
            pw.format(Locale.US, "%4d %c %.2f %.1f %.1f %.1f", index, hot? 'B' : 'b', dd, bb, cc, rr );
          } else {
            pw.format(Locale.US, "%4d %c %.2f %.1f %.1f %.1f", index, hot? 'D' : 'd', dd, bb, cc, rr );
          }
          break;
        case 0x02:
        case 0x03:
          long X = toInt( data[2], data[1] );
          long Y = toInt( data[4], data[3] );
          long Z = toInt( data[6], data[5] );
          int n  = toInt( data[7] ); // cal number HB
          if ( X > TDUtil.ZERO ) X = X - TDUtil.NEG;
          if ( Y > TDUtil.ZERO ) Y = Y - TDUtil.NEG;
          if ( Z > TDUtil.ZERO ) Z = Z - TDUtil.NEG;
          if ( type == 0x02 ) {
            if ( TDSetting.mRawCData > 0 ) {
              pw.format("%4d %c %02x %02x %02x %02x %02x %02x %3d", index, hot? 'G' : 'g',
                data[1], data[2], data[3], data[4], data[5], data[6], n );
            } else {
              pw.format("%4d %c %6d %6d %6d %3d", index, hot? 'G' : 'g', X, Y, Z, n ); // HB
            }
          } else {
            if ( TDSetting.mRawCData > 0 ) {
              pw.format("%4d %c %02x %02x %02x %02x %02x %02x %3d", index, hot? 'M' : 'm',
                data[1], data[2], data[3], data[4], data[5], data[6], n );
            } else {
              pw.format("%4d %c %6d %6d %6d %3d", index, hot? 'M' : 'm', X, Y, Z, n ); // HB
            }
          }
          break;
        case 0x04:
          boolean backsight = ( (data[0] & BIT_BACKSIGHT) == BIT_BACKSIGHT);
          int acc = toInt( data[2], data[1] );
          int mag = toInt( data[4], data[3] );
          int idip = toInt( data[6], data[5] );
          double dip = ( idip >= 32768 )? (65536 - idip) * (-90.0) / 16384.0 : idip * 90.0  / 16384.0; // 90/0x4000;
          pw.format(Locale.US, "%4d %c %d %d %.2f %02x", index, backsight? 'V' : 'v', acc, mag, dip, data[0] ); // is data[7] important ?
          break;
        default:
          printHexString( pw );
          break;
      }
    }
    return sw.getBuffer().toString();
  }

}
