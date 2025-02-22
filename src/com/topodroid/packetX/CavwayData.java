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
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.TDLog;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;
import com.topodroid.utils.TDUtil;

public class CavwayData extends MemoryData
{
  public static final int G_SCALE =  667;
  public static final int M_SCALE = 4876;
  private static final double ABS_SCALE = 10000.0;
  private static final double ANGLE_SCALE = 360.0 / 65536.0;
  // private static final double GLOBAL_FV = 24000.0;
  private static final double GLOBAL_FM = 16384.0;

  // public static final byte BIT_BACKSIGHT2 = 0x20;
  // public static final byte BIT_BACKSIGHT  = 0x40; // backsight bit of vector packet

  // public static final byte BYTE_PACKET_DATA   = 0x01;
  // public static final byte BYTE_PACKET_G      = 0x02;
  // public static final byte BYTE_PACKET_M      = 0x03;
  // public static final byte BYTE_PACKET_VECTOR = 0x04;
  public static final byte BYTE_PACKET_REPLY  = 0x38;
  public static final byte BYTE_PACKET_REQST  = 0x39;
  // // public static final byte BYTE_PACKET_38     = 0x38;
  // // public static final byte BYTE_PACKET_39     = 0x39;
  public static final byte BYTE_PACKET_FW_READ  = 0x3a;
  public static final byte BYTE_PACKET_FW_WRITE = 0x3b;
  // public static final byte BYTE_PACKET_HW_CODE  = 0x3c;
  // public static final byte BYTE_PACKET_3D     = 0x3d;
  // public static final byte BYTE_PACKET_3E     = 0x3e;

  public static final int INVALID = -1;
  public static final int SHOT = 0;
  public static final int LEG  = 1;
  public static final int CALI = 2;
  int mType = INVALID; // data type

  public static final int SIZE = 64; // data size

  private static final int IDX_ERR = 45; // index of error bytes

  // ------------------------------------------------------------

  public CavwayData( int idx )
  {
    super( idx, SIZE );
  }

  public void setData( byte[] b )
  {
    System.arraycopy( b, 0, data, 0, SIZE );
    setType( b );
    // TDLog.v("CVWY data set data - type " + mType + String.format(" %02x %02x", b[0], b[1]) );
  }

  public byte[] getData() { return data; }

  public byte getData( int k ) { return data[k]; }

  public int getType() { return mType; }

  private void setType( byte[] b )
  {
    if ( b[0] == (byte)0xff ) {
      mType = INVALID;
    } else {
      // if ( (b[1] & (byte)0x80) == 0 ) { // 0x7f
      if ( b[1] == (byte)0xef ) {
        mType = LEG; 
      // } else if ( (b[1] & (byte)0x01) == 0 ) { // 0xfe
      } else if ( b[1] == (byte)0xfe ) { // 0xfe
        mType = CALI; 
      } else { // 0xff
        mType = SHOT;
      }
    }
  }

  public static double toLaser( byte[] b )
  {
    int dhh = (int)( b[2] ); 
    if ( dhh < 0 ) dhh += 256;
    return ( dhh * 65536.0 + toInt( b[4], b[3] ) )/1000.0;
  }

  public double getAzimuth() { return toAzimuth( data ); }
  public double getClino()   { return toClino( data ); }
  public double getRoll()    { return toRoll( data ); }

  public static double toAzimuth( byte[] b ) { return toInt( b[ 6], b[ 5] ) * ANGLE_SCALE; }
  // public static double toAzimuth( byte[] b ) { return toAzimuth( b[5], b[6] ); }

  public static double toClino( byte[] b ) // 20250105 changed as for CavwayProtocol
  { 
    return  toSignedInt( b[ 8], b[ 7] ) * ANGLE_SCALE;
  }
  // public double toClino( byte[] b ) { return toClino( b[7], b[8] ); }

  public static double toRoll( byte[] b )  { return - toInt( b[10], b[ 9] ) * ANGLE_SCALE; }
  // public double toRoll( byte[] b ) { return toAzimuth( b[9], b[10] ); }

  public static double toAbsG1( byte[] b ) { return toInt( b[12], b[11] ) * G_SCALE / 1000.0 / GLOBAL_FM; }
  public static double toAbsM1( byte[] b ) { return toInt( b[14], b[13] ) * M_SCALE /  100.0 / GLOBAL_FM; }
  public static double toDip1( byte[] b )  { return toSignedInt( b[16], b[15] ) * ANGLE_SCALE; }
  // public static double toDip1( byte[] b ) { return toClino( b[15], b[16] ) * ANGLE_SCALE; }

  public static double toAbsG2( byte[] b ) { return toInt( b[59], b[58] ) * G_SCALE / 1000.0 / GLOBAL_FM; }
  public static double toAbsM2( byte[] b ) { return toInt( b[61], b[60] ) * M_SCALE /  100.0 / GLOBAL_FM; }
  public static double toDip2( byte[] b )  { return toSignedInt( b[63], b[62] ) * ANGLE_SCALE; }
  // public static double toDip2( byte[] b ) { return toClino( b[62], b[63] ) * ANGLE_SCALE; }

  public static int toSQ( byte[] b ) { return toInt( b[57], b[56] ); }

  public static String toTime( byte[] b ) 
  { 
    long seconds = toLong( b[20], b[19], b[18], b[17] );
    return TDUtil.timestampToDateTime( seconds );
  }

  public static String toDate( byte[] b ) 
  { 
    long seconds = toLong( b[20], b[19], b[18], b[17] );
    return TDUtil.timestampToDate( seconds );
  }


  public static int toRawG1x( byte[] b ) { return toSignedInt( b[22], b[21] ); }
  public static int toRawG1y( byte[] b ) { return toSignedInt( b[24], b[23] ); }
  public static int toRawG1z( byte[] b ) { return toSignedInt( b[26], b[25] ); }
  public static int toRawM1x( byte[] b ) { return toSignedInt( b[28], b[27] ); }
  public static int toRawM1y( byte[] b ) { return toSignedInt( b[30], b[29] ); }
  public static int toRawM1z( byte[] b ) { return toSignedInt( b[32], b[31] ); }

  public static int toRawG2x( byte[] b ) { return toSignedInt( b[34], b[33] ); }
  public static int toRawG2y( byte[] b ) { return toSignedInt( b[36], b[35] ); }
  public static int toRawG2z( byte[] b ) { return toSignedInt( b[38], b[37] ); }
  public static int toRawM2x( byte[] b ) { return toSignedInt( b[40], b[39] ); }
  public static int toRawM2y( byte[] b ) { return toSignedInt( b[42], b[41] ); }
  public static int toRawM2z( byte[] b ) { return toSignedInt( b[44], b[43] ); }

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

    // boolean hot  = (int)( data[0] & 0x80 ) == 0x80; // test hot bit
    switch ( mType ) {
      case CALI:
        pw.format("C %s %d %d %d %d %d %d %d %d %d %d %d %d", 
          toDate( data ),
          toRawG1x( data ), toRawG1y( data ), toRawG1z( data ),
          toRawM1x( data ), toRawM1y( data ), toRawM1z( data ),
          toRawG2x( data ), toRawG2y( data ), toRawG2z( data ),
          toRawM2x( data ), toRawM2y( data ), toRawM2z( data ) );
        break;
      case SHOT:
      case LEG:
        double dd = toLaser( data );
        double bb = toAzimuth( data );
        double cc = toClino( data );
        double rr = toRoll( data );
        // String tt = toTime( data );
        String tt = toDate( data );
        double dip1 = toDip1( data );
        double dip2 = toDip2( data );
        pw.format("%c%c %s %.2f %.2f %.2f %.2f %.2f %.2f", ((mType == LEG)? 'L' : ' '), (hasError()? 'E' : ' '), tt, dd, bb, cc, rr, dip1, dip2 );
        break;
      // case INVALID:
      default:
        pw.format("%4d ! invalid data", index );
        break;
    }
    return sw.getBuffer().toString();
  }

  public boolean hasError() { return data[IDX_ERR] !=  (byte)(0xFF); }

  public String parseErrInfo( )
  {
    byte[] errbytes = new byte[9];
    System.arraycopy( data, IDX_ERR, errbytes, 0, 9 );
    byte errbyte = errbytes[0];
    if ( errbyte == (byte)(0xFF) ) return "No error";
    int err_cnt = 0;
    StringBuilder sb = new StringBuilder();
    if ( ( errbyte & 0x80 ) == 0 ) { // absG error
      sb.append("absG error: G1 ");
      sb.append( String.format( Locale.US, "%.2f ", (toInt(errbytes[4 * err_cnt + 2], errbytes[4 * err_cnt + 1]) / ABS_SCALE ) ) );
      sb.append("G2 ");
      sb.append( String.format( Locale.US, "%.2f ", (toInt(errbytes[4 * err_cnt + 4], errbytes[4 * err_cnt + 3]) / ABS_SCALE ) ) );
      if ( ++err_cnt == 2 ) return sb.toString();
    }
    if ( ( errbyte & 0x40 ) == 0 ) { // absM error
      sb.append("absM error: M1 ");
      sb.append( String.format( Locale.US, "%.2f ", (toInt(errbytes[4 * err_cnt + 2], errbytes[4 * err_cnt + 1]) / ABS_SCALE ) ) );
      sb.append("M2 ");
      sb.append( String.format( Locale.US, "%.2f ", (toInt(errbytes[4 * err_cnt + 4], errbytes[4 * err_cnt + 3]) / ABS_SCALE ) ) );
      if ( ++err_cnt == 2 ) return sb.toString();
    }
    if ( ( errbyte & 0x20 ) == 0 ) { // dip error
      sb.append("dip error: D1 ");
      sb.append( String.format( Locale.US, "%.2f ", (toInt(errbytes[4 * err_cnt + 2], errbytes[4 * err_cnt + 1]) / ABS_SCALE ) ) );
      sb.append("D2 ");
      sb.append( String.format( Locale.US, "%.2f ", (toInt(errbytes[4 * err_cnt + 4], errbytes[4 * err_cnt + 3]) / ABS_SCALE ) ) );
      if ( ++err_cnt == 2 ) return sb.toString();
    }
    if ( ( errbyte & 0x10 ) == 0 ) { // angle error
      sb.append("angle error: A1 ");
      sb.append( String.format( Locale.US, "%.2f ", (toInt(errbytes[4 * err_cnt + 2], errbytes[4 * err_cnt + 1]) / ABS_SCALE ) ) );
      sb.append("A2 ");
      sb.append( String.format( Locale.US, "%.2f ", (toInt(errbytes[4 * err_cnt + 4], errbytes[4 * err_cnt + 3]) / ABS_SCALE ) ) );
      if ( ++err_cnt == 2 ) return sb.toString();
    }
    return sb.toString();
  }      


  // DEBUG
  // public void dumpHexString()
  // {
  //   for ( int k = 0; k+16 <= SIZE; k += 16 ) {
  //     StringBuilder sb = new StringBuilder();
  //     sb.append( "   " );
  //     for ( int h=0; h<16; ++h ) sb.append( String.format("%02x", data[k+h] ) );
  //     TDLog.v( sb.toString() );
  //   }
  // }
   
    

}
