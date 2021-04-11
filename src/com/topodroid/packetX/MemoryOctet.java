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

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;

public class MemoryOctet
{
  private int index; // memory index
  // A3:   index = address/8
  // X310: index = 56*(address/1024) + (address%1024)/18

  public byte[] data;
  public static final byte BIT_BACKSIGHT2 = 0x20;
  public static final byte BIT_BACKSIGHT  = 0x40; // backsight bit of vector packet

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
    if ( ret < 0 ) ret += 256;
    return ret;
  }

  public static int toInt( byte bh, byte bl )
  {
    int h = (int)(bh & 0xff);   // high
    if ( h < 0 ) h += 256;
    int l = (int)(bl & 0xff);   // low
    if ( l < 0 ) l += 256;
    return (h * 256 + l);
  }
  // ------------------------------------------------------------

  public MemoryOctet( int idx )
  {
    index = idx;
    data  = new byte[8];
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
          if ( (data[0] & BIT_BACKSIGHT2) == BIT_BACKSIGHT2 ) {
            pw.format(Locale.US, "%4d %c %.2f %.1f %.1f", index, hot? 'B' : 'b', dd, bb, cc );
          } else {
            pw.format(Locale.US, "%4d %c %.2f %.1f %.1f", index, hot? 'D' : 'd', dd, bb, cc );
          }
          break;
        case 0x02:
        case 0x03:
          // long X = toInt( data[2], data[1] );
          // long Y = toInt( data[4], data[3] );
          // long Z = toInt( data[6], data[5] );
          // if ( X > TDUtil.ZERO ) X = X - TDUtil.NEG;
          // if ( Y > TDUtil.ZERO ) Y = Y - TDUtil.NEG;
          // if ( Z > TDUtil.ZERO ) Z = Z - TDUtil.NEG;
          if ( type == 0x02 ) {
            pw.format("%4d %c %02x %02x %02x %02x %02x %02x", index, hot? 'G' : 'g',
               data[1], data[2], data[3], data[4], data[5], data[6] );
          } else {
            // pw.format("%4d %c %x %x %x", index, hot? 'M' : 'm', X, Y, Z );
            pw.format("%4d %c %02x %02x %02x %02x %02x %02x", index, hot? 'M' : 'm',
               data[1], data[2], data[3], data[4], data[5], data[6] );
          }
          break;
        case 0x04:
          boolean backsight = ( (data[0] & BIT_BACKSIGHT) == BIT_BACKSIGHT);
          int acc = toInt( data[2], data[1] );
          int mag = toInt( data[4], data[3] );
          int idip = toInt( data[5], data[6] );
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
