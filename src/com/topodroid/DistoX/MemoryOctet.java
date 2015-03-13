/** @file MemoryOctet.java
 *
 * @author marco corvi
 * @date 201311
 *
 * @brief 8-byte data in the distox memory
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;

class MemoryOctet
{
  int index; // memory index
  // A3:   index = address/8
  // X310: index = 56*(address/1024) + (address%1024)/18

  byte[] data;

  // ------------------------------------------------------------

  static double toDistance( byte b0, byte b1, byte b2 )
  {
    int dhh = (int)( b0 & 0x40 );
    double d =  dhh * 1024.0 + toInt( b2, b1 );
    if ( d < 99999 ) {
      return d / 1000.0;
    }
    return 100 + (d-100000) / 100.0;
  }

  static double toAzimuth( byte b1, byte b2 ) // b1 low, b2 high
  {
    int b = toInt( b2, b1 );
    return b * 180.0 / 32768.0; // 180/0x8000;
  }

  static double toClino( byte b1, byte b2 ) // b1 low, b2 high
  {
    int c = toInt( b2, b1 );
    double cc = c * 90.0  / 16384.0; // 90/0x4000;
    if ( c >= 32768 ) { cc = (65536 - c) * (-90.0) / 16384.0; }
    return cc;
  }

  static int toInt( byte b ) 
  {
    int ret = (int)(b & 0xff);
    if ( ret < 0 ) ret += 256;
    return ret;
  }

  static int toInt( byte bh, byte bl )
  {
    int h = (int)(bh & 0xff);   // high
    if ( h < 0 ) h += 256;
    int l = (int)(bl & 0xff);   // low
    if ( l < 0 ) l += 256;
    return (h * 256 + l);
  }
  // ------------------------------------------------------------

  MemoryOctet( int idx )
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
      int type = (int)( data[0] & 0x3f ); // bits 0-5
      switch ( type ) {
        case 0x01:
          double dd = toDistance( data[0], data[1], data[2] );
          double bb = toAzimuth( data[3], data[4] );
          double cc = toClino( data[5], data[6] );
          pw.format(Locale.ENGLISH, "%4d %c %.2f %.1f %.1f", index, hot? 'D' : 'd', dd, bb, cc );
          break;
        case 0x02:
        case 0x03:
          long X = toInt( data[2], data[1] );
          long Y = toInt( data[4], data[3] );
          long Z = toInt( data[6], data[5] );
          if ( X > TopoDroidUtil.ZERO ) X = X - TopoDroidUtil.NEG;
          if ( Y > TopoDroidUtil.ZERO ) Y = Y - TopoDroidUtil.NEG;
          if ( Z > TopoDroidUtil.ZERO ) Z = Z - TopoDroidUtil.NEG;
          if ( type == 0x02 ) {
            pw.format("%4d %c %x %x %x", index, hot? 'G' : 'g', X, Y, Z );
          } else {
            pw.format("%4d %c %x %x %x", index, hot? 'M' : 'm', X, Y, Z );
          }
          break;
        case 0x04:
          int acc = toInt( data[2], data[1] );
          int mag = toInt( data[4], data[3] );
          double dip = toInt( data[5], data[6] );
          dip = dip * 90.0  / 16384.0; // 90/0x4000;
          if ( dip >= 32768 ) { dip = (65536 - dip) * (-90.0) / 16384.0; }
          pw.format(Locale.ENGLISH, "%4d %c %d %d %.2f %02x", index, hot? 'V' : 'v', acc, mag, dip, data[7] );
          break;
        default:
          printHexString( pw );
          break;
      }
    }
    return sw.getBuffer().toString();
  }

}
