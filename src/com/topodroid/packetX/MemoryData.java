/* @file MemoryData.java
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

public class MemoryData
{
  protected int index; // memory index
  public byte[] data;

  // ------------------------------------------------------------

  protected static double toDistance( byte b0, byte b1, byte b2 )
  {
    int dhh = (int)( b0 & 0x40 );
    double d =  dhh * 1024.0 + toInt( b2, b1 );
    if ( d < 99999 ) {
      return d / 1000.0;
    }
    return 100 + (d-100000) / 100.0;
  }

  protected static double toAzimuth( byte b1, byte b2 ) // b1 low, b2 high
  {
    int b = toInt( b2, b1 );
    return b * 180.0 / 32768.0; // 180/0x8000;
  }

  protected static double toClino( byte b1, byte b2 ) // b1 low, b2 high
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

  public static int toSignedInt( byte bh, byte bl )
  {
    int h = (int)(bh & 0xff);   // high
    if ( h < 0 ) h += 256; // always false
    int l = (int)(bl & 0xff);   // low
    if ( l < 0 ) l += 256; // always false
    h = (h * 256 + l);
    if ( h >= 32768 ) h = 65536 - h;
    return h;
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

  public MemoryData( int idx, int size )
  {
    index = idx;
    data  = new byte[size];
  }

}
