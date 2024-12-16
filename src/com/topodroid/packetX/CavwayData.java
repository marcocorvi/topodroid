/* @file CavwayData.java
 *
 * @author marco corvi
 * @date 202412
 *
 * @brief 64-byte data in the cavway memory
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

public class CavwayData extends MemoryData
{
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

  public static final int SIZE = 64;

  // ------------------------------------------------------------

  public CavwayData( int idx )
  {
    super( idx, SIZE );
  }

  public double toLaser( byte[] b )
  {
    int dhh = (int)( b[2] );
    if ( dhh < 0 ) dhh += 256;
    return ( dhh * 1024.0 + toInt( b[4], b[3] ) )/1000.0;
  }

  public double toAzimuth( byte[] b ) { return toAzimuth( b[5], b[6] ); }

  public double toClino( byte[] b ) { return toClino( b[7], b[8] ); }

  public double toRoll( byte[] b ) { return toAzimuth( b[9], b[10] ); }

  public int toAbsG1( byte[] b ) { return toInt( b[12], b[11] ); }
  public int toAbsM1( byte[] b ) { return toInt( b[14], b[13] ); }
  public double toDip1( byte[] b ) { return toClino( b[15], b[16] ); }

  public int toAbsG2( byte[] b ) { return toInt( b[59], b[58] ); }
  public int toAbsM2( byte[] b ) { return toInt( b[61], b[60] ); }
  public double toDip2( byte[] b ) { return toClino( b[62], b[63] ); }

  public int toSQ( byte[] b ) { return toInt( b[57], b[56] ); }
  public long toTime( byte[] b ) { return toLong( b[20], b[19], b[18], b[17] ); }

  public static int toRawG1x( byte[] b ) { return toInt( b[22], b[21] ); }
  public static int toRawG1y( byte[] b ) { return toInt( b[24], b[23] ); }
  public static int toRawG1z( byte[] b ) { return toInt( b[26], b[25] ); }
  public static int toRawM1x( byte[] b ) { return toInt( b[28], b[27] ); }
  public static int toRawM1y( byte[] b ) { return toInt( b[30], b[29] ); }
  public static int toRawM1z( byte[] b ) { return toInt( b[32], b[31] ); }

  public static int toRawG2x( byte[] b ) { return toInt( b[34], b[33] ); }
  public static int toRawG2y( byte[] b ) { return toInt( b[36], b[35] ); }
  public static int toRawG2z( byte[] b ) { return toInt( b[38], b[37] ); }
  public static int toRawM2x( byte[] b ) { return toInt( b[40], b[39] ); }
  public static int toRawM2y( byte[] b ) { return toInt( b[42], b[41] ); }
  public static int toRawM2z( byte[] b ) { return toInt( b[44], b[43] ); }

  public void printHexString( PrintWriter pw )
  {
    boolean hot  = (int)( data[0] & 0x80 ) == 0x80; // test hot bit
    StringBuilder sb = new StringBuilder();
    for ( int k=0; k<SIZE; ++k ) sb.append( String.format("%02x", data[k] ) );
    pw.format( "%4d %c %s", index, (hot? '?' : '>'), sb.toString() );
  }

  public String toString() 
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );

    if ( data[0] == 0xff ) {
      pw.format("%4d ! invalid data", index );
    } else {
      boolean hot  = (int)( data[0] & 0x80 ) == 0x80; // test hot bit
      int type = 0; // splay data
      if ( data[1] == (byte)0xfe ) { type = 0x02; } // cali
      if ( data[1] == (byte)0xef ) { type = 0x01; } // leg
      double dd = toLaser( data );
      double bb = toAzimuth( data );
      double cc = toClino( data );
      double rr = toRoll( data );
      long tt = toTime( data );
      double dip1 = toDip1( data );
      double dip2 = toDip2( data );
      pw.format("D %.2f A %.2f C %.2f R %.2f dip %.2f %.2f", dd, bb, cc, rr, dip1, dip2 );
    }
    return sw.getBuffer().toString();
  }

}
