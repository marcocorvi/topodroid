/* @file PacketData.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid packet data for packet logging
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

class PacketData
{
  Long millis;
  int  dir;
  String address;
  String data;
  int    type;
  private byte mRollHigh = 0;

  static final char[] mTypes = { '_', 'D', 'G', 'M', 'V', 'C', 'X' };
  static final int[] mColors = { TDColor.PINK,  TDColor.WHITE, TDColor.BROWN, TDColor.ORANGE, TDColor.LIGHT_GRAY, 
                                 TDColor.GREEN, TDColor.YELLOW };
  static final int[] mBackground = { TDColor.BLACK, TDColor.VERYDARK_GRAY };

  PacketData( long m, long d, String addr, int t, String dat )
  {
    millis = m;
    dir    = (int)d;
    address = addr;
    type    = t;    // byte[0]
    if ( type < 0 ) { type = 0; } else if ( type > 6 ) { type = 6; }
    data    = dat;
  }

  static boolean checkType( int type, int filter )
  { 
    if ( type < 0 ) { type = 0; } else if ( type > 6 ) { type = 6; }
    return ( filter & ( 1 << type ) ) != 0;
  }

  static private byte toByte( String s )
  {
    char ch = s.charAt(0);
    int high = ( ch >= '0' && ch <= '9' )? ch - '0' : 10 + ch - 'a';
    ch = s.charAt(1);
    int low  = ( ch >= '0' && ch <= '9' )? ch - '0' : 10 + ch - 'a';
    return (byte)( ((high * 16) & 0xf0 ) | (low & 0x0f ) );
  }


  private String dataToString()
  {
    String[] vals;
    byte[] buf;
    int len;
    switch ( type ) {
      case 1:
        vals = data.split(" ");
        buf = new byte[8];
        len = vals.length; if ( len > 8 ) len = 8;
        for ( int k=0; k<len; ++k ) buf[k] = toByte( vals[k] );
        int dhh = (int)( buf[0] & 0x40 );
        double d =  dhh * 1024.0 + MemoryOctet.toInt( buf[2], buf[1] );
        double b = MemoryOctet.toInt( buf[4], buf[3] );
        double c = MemoryOctet.toInt( buf[6], buf[5] );
        // X31--ready
        // mRollHigh = buf[7];
        int r7 = (int)(buf[7] & 0xff); if ( r7 < 0 ) r7 += 256;
        // double r = (buf[7] & 0xff);
        double r = r7;

        // if ( mDeviceType == Device.DISTO_A3 || mDeviceType == Device.DISTO_X000) // FIXME VirtualDistoX
        // if ( mDeviceType == Device.DISTO_A3 ) {
        //   d /= 1000.0;
        // } else if ( mDeviceType == Device.DISTO_X310 ) {
          if ( d < 99999 ) {
            d /= 1000.0;
          } else {
            d = 100 + (d-100000) / 100.0;
          }
        // }

        b  = b * 180.0 / 32768.0; // 180/0x8000;
        if ( c >= 32768 ) { c = (65536 - c) * (-90.0) / 16384.0; }
        else { c = c * 90.0  / 16384.0; } // 90/0x4000; 
        r = r * 180.0 / 128.0;
        return String.format(Locale.US, "D %.2f B %.1f C %.1f R %.1f", d, b, c, r );
      case 2:
      case 3:
        vals = data.split(" ");
        buf = new byte[8];
        len = vals.length; if ( len > 7 ) len = 7;
        for ( int k=1; k<len; ++k ) buf[k] = toByte( vals[k] );
        long x = MemoryOctet.toInt( buf[2], buf[1] );
        long y = MemoryOctet.toInt( buf[4], buf[3] );
        long z = MemoryOctet.toInt( buf[6], buf[5] );
        if ( x > TopoDroidUtil.ZERO ) x = x - TopoDroidUtil.NEG;
        if ( y > TopoDroidUtil.ZERO ) y = y - TopoDroidUtil.NEG;
        if ( z > TopoDroidUtil.ZERO ) z = z - TopoDroidUtil.NEG;
        return String.format("X %x Y %x Z %x", x, y, z );
      case 4:
        vals = data.split(" ");
        buf = new byte[8];
        len = vals.length; if ( len > 8 ) len = 8;
        for ( int k=1; k<len; ++k ) buf[k] = toByte( vals[k] );
        double acc = MemoryOctet.toInt( buf[2], buf[1] );
        double mag = MemoryOctet.toInt( buf[4], buf[3] );
        double dip = MemoryOctet.toInt( buf[6], buf[5] );
        double rl = MemoryOctet.toInt( mRollHigh, buf[7] );
        dip = dip * 90.0  / 16384.0; // 90/0x4000;
        if ( dip >= 32768 ) { dip = (65536 - dip) * (-90.0) / 16384.0; }
        // r = r * 180.0 / 32768.0; // 180/0x8000;
        rl  = rl * 180.0 / 128.0; // 180/0x8000;
        return String.format(Locale.US, "a %.0f m %.0f d %.1f r %.1f", acc, mag, dip, rl );
      default:
    }
    return data;
  }

  public String toString()
  {
    // SimpleDateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss");
    // String date = df.format( millis );
    // // Log.v("DistoXP", date + " type " + type );
    // // return String.format("%s: %s (%d %c) %s", address, date, dir, mTypes[type], dataToString() );
    // return String.format("%s", dataToString() );
    return dataToString();
  }

  // foreground color
  public int color() { return mColors[ type ]; }

  public int background() { return mBackground[ dir ]; }
    
}
