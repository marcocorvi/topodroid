/* @file BricConst.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BRIC4 constants
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import com.topodroid.dev.ble.BleUtils;

import android.util.Log;

import java.util.UUID; 
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BricConst
{
  public final static String MEAS_SRV  = "000058d0" + BleUtils.STANDARD_UUID;
  public final static String MEAS_PRIM = "000058d1" + BleUtils.STANDARD_UUID;
  public final static String MEAS_META = "000058d2" + BleUtils.STANDARD_UUID;
  public final static String MEAS_ERR  = "000058d3" + BleUtils.STANDARD_UUID;
  public final static String LAST_TIME = "000058d4" + BleUtils.STANDARD_UUID;
 
  public final static UUID MEAS_SRV_UUID  = UUID.fromString( MEAS_SRV );
  public final static UUID MEAS_PRIM_UUID = UUID.fromString( MEAS_PRIM );
  public final static UUID MEAS_META_UUID = UUID.fromString( MEAS_META );
  public final static UUID MEAS_ERR_UUID  = UUID.fromString( MEAS_ERR );
  public final static UUID LAST_TIME_UUID = UUID.fromString( LAST_TIME );

  public final static String CTRL_SRV  = "000058e0" + BleUtils.STANDARD_UUID;
  public final static String CTRL_CHRT = "000058e1" + BleUtils.STANDARD_UUID;

  public final static UUID CTRL_SRV_UUID  = UUID.fromString( CTRL_SRV );
  public final static UUID CTRL_CHRT_UUID = UUID.fromString( CTRL_CHRT );

  public final static byte[] COMMAND_OFF   = { (byte)0x70, (byte)0x6f, (byte)0x77, (byte)0x65, (byte)0x72, (byte)0x20, (byte)0x6f, (byte)0x66, (byte)0x66 };
  public final static byte[] COMMAND_SCAN  = { (byte)0x73, (byte)0x63, (byte)0x61, (byte)0x6e };
  public final static byte[] COMMAND_SHOT  = { (byte)0x73, (byte)0x68, (byte)0x6f, (byte)0x74 };
  public final static byte[] COMMAND_LASER = { (byte)0x6c, (byte)0x61, (byte)0x73, (byte)0x65, (byte)0x72 };
  public final static byte[] COMMAND_CLEAR = { (byte)0x63, (byte)0x6c, (byte)0x65, (byte)0x61, (byte)0x72, (byte)0x20, (byte)0x6d, (byte)0x65, (byte)0x6d, (byte)0x6f, (byte)0x72, (byte)0x79 };

  public final static int CMD_OFF   = 1;
  public final static int CMD_SCAN  = 2;
  public final static int CMD_SHOT  = 3;
  public final static int CMD_LASER = 4;
  public final static int CMD_CLEAR = 5;

  public final static int CMD_SPLAY = 11;
  public final static int CMD_LEG   = 12;

  static float getDistance( byte[] bytes ) { return BleUtils.getFloat( bytes,  8 ); }
  static float getAzimuth( byte[] bytes )  { return BleUtils.getFloat( bytes, 12 ); }
  static float getClino( byte[] bytes )    { return BleUtils.getFloat( bytes, 16 ); }
  static int   getIndex( byte[] bytes )    { return BleUtils.getInt(   bytes,  0 ); }
  static float getDip( byte[] bytes )      { return BleUtils.getFloat( bytes,  4 ); }
  static float getRoll( byte[] bytes )     { return BleUtils.getFloat( bytes,  8 ); }
  static float getTemp( byte[] bytes )     { return BleUtils.getFloat( bytes, 12 ); }
  static int   getType( byte[] bytes )     { return BleUtils.getChar(  bytes, 18 ); }
  static short getSamples( byte[] bytes )  { return BleUtils.getShort( bytes, 16 ); }

  // get the timestamp in msec
  static long  getTimestamp( byte[] bytes ) 
  {
    int yy = BleUtils.getShort( bytes, 0 );
    int mm = BleUtils.getChar( bytes, 2 );
    int dd = BleUtils.getChar( bytes, 3 );
    int HH = BleUtils.getChar( bytes, 4 );
    int MM = BleUtils.getChar( bytes, 5 );
    int SS = BleUtils.getChar( bytes, 6 );
    int CS = BleUtils.getChar( bytes, 7 ); // centiseconds
    // Log.v("DistoX", "BRIC time " + String.format("%4d-%02d-%02d %2d:%02d:%02d.%02d", yy, mm, dd, HH, MM, SS, CS ) );
    Calendar date = new GregorianCalendar();
    date.set( yy, mm, dd, HH, MM, SS );
    return date.getTimeInMillis() + 10 * CS;
  }

  static String getTimeString( byte[] bytes ) 
  {
    StringBuilder sb = new StringBuilder();
    sb.append( Integer.toString( BleUtils.getShort( bytes, 0 ) ) );
    sb.append( "-" );
    sb.append( Integer.toString( BleUtils.getChar( bytes, 2 ) ) );
    sb.append( "-" );
    sb.append( Integer.toString( BleUtils.getChar( bytes, 3 ) ) );
    sb.append( " " );
    sb.append( Integer.toString( BleUtils.getChar( bytes, 4 ) ) );
    sb.append( ":" );
    sb.append( Integer.toString( BleUtils.getChar( bytes, 5 ) ) );
    sb.append( ":" );
    sb.append( Integer.toString( BleUtils.getChar( bytes, 6 ) ) );
    // int CS = BleUtils.getChar( bytes, 7 ); // centiseconds
    return sb.toString();
  }

  static byte[] makeTimeBytes( short year, char month, char day, char hour, char minute, char second, char centisecond )
  {
    byte[] ret = new byte[8];
    setTimeBytes( ret, year, month, day, hour, minute, second, centisecond );
    return ret;
  }

  static void setTimeBytes( byte[] ret, short year, char month, char day, char hour, char minute, char second, char centisecond )
  {
    BleUtils.putShort( ret, 0, year );
    BleUtils.putChar( ret, 2, month );
    BleUtils.putChar( ret, 3, day );
    BleUtils.putChar( ret, 4, hour );
    BleUtils.putChar( ret, 5, minute );
    BleUtils.putChar( ret, 6, second );
    BleUtils.putChar( ret, 7, centisecond );
  }

  static final int ERR_NONE       = 0; // no error
  static final int ERR_ACC_1      = 1;
  static final int ERR_ACC_2      = 2;
  static final int ERR_MAG_1      = 3;
  static final int ERR_MAG_2      = 4;
  static final int ERR_ACC_D      = 5; // accelerometer disparity
  static final int ERR_MAG_D      = 6; // magnetometers disparity
  static final int ERR_LSR        = 7; // target moved
  static final int ERR_LSR_WEAK   = 8; // weak signal
  static final int ERR_LSR_STRONG = 9; // strong signal
  static final int ERR_LSR_0XAA   =10; // pattern error
  static final int ERR_LSR_TIME   =11; // timeout
  static final int ERR_LSR_ERROR  =12; // unrecognized error
  static final int ERR_LSR_MSGID  =13; // wring message
  static final int ERR_CLINO      =14;
  static final int ERR_AZIMUTH    =15;

  private static int getErrorCode( byte[] bytes, int off )
  {
    int ret = BleUtils.getChar( bytes, off );
    if ( ret == 5 || ret == 6 ) {
      float val2 = BleUtils.getFloat( bytes, off+5 );
      if ( val2 < 0.5f ) return ret;
      if ( val2 < 1.5f ) return 32 + ret; 
      if ( val2 < 2.5f ) return 34 + ret; 
      if ( val2 < 3.5f ) return 36 + ret; 
    }
    return ret;
  }

  private static float getErrorValue( byte[] bytes, int code, int off )
  {
    if ( code == 0 || ( code >= 7 && code <= 10 ) ) return 0;
    return BleUtils.getFloat( bytes, off );
  }

  static int firstErrorCode( byte[] bytes ) { return getErrorCode( bytes, 0 ); }
  static int secondErrorCode( byte[] bytes ) { return getErrorCode( bytes, 9 ); }
  static float firstErrorValue( byte[] bytes, int code ) { return getErrorValue( bytes, code, 1 ); }
  static float secondErrorValue( byte[] bytes, int code ) { return getErrorValue( bytes, code, 10 ); }

  static String errorString( byte[] bytes )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "[1] " );
    int err   = BleUtils.getChar( bytes, 0 );
    float val1=0;
    float val2=0;
    if ( err != ERR_NONE && (err < 7 || err > 10) ) {
      val1 = BleUtils.getFloat( bytes, 1 );
      if ( err == 5 || err == 6 ) val2 = BleUtils.getFloat( bytes, 5 );
    }
    sb.append( errorString( err, val1, val2 ) );
    
    if ( bytes.length <= 9 ) return sb.toString();
    err = BleUtils.getChar( bytes, 9 );
    if ( err != ERR_NONE && (err < 7 || err > 10) ) {
      val1 = BleUtils.getFloat( bytes, 10 );
      if ( err == 5 || err == 6 ) val2 = BleUtils.getFloat( bytes, 14 );
    }
    sb.append( " [2] " );
    sb.append( errorString( err, val1, val2 ) );
    return sb.toString();
  }

  private static String errorString( int code, float v1, float v2 )
  {
    switch ( code ) {
      case ERR_NONE       : return "No error";  // no error
      case ERR_ACC_1      : return "Acc-1 high " + v1;
      case ERR_ACC_2      : return "Acc-2 high " + v1;
      case ERR_MAG_1      : return "Mag-1 high " + v1;
      case ERR_MAG_2      : return "Mag-2 high " + v1;
      case ERR_ACC_D      : return "Acc delta " + v1 + " axis " + v2; // accelerometer disparity
      case ERR_MAG_D      : return "Mag delta " + v1 + " axis " + v2; // magnetometers disparity
      case ERR_LSR        : return "Target moved";  // target moved
      case ERR_LSR_WEAK   : return "Weak signal";  // weak signal
      case ERR_LSR_STRONG : return "Strong signal";  // strong signal
      case ERR_LSR_0XAA   : return "Pattern error";  // pattern error
      case ERR_LSR_TIME   : return "Timeout " + v1;  // timeout
      case ERR_LSR_ERROR  : return "Laser error " + v1;  // unrecognized error
      case ERR_LSR_MSGID  : return "Wrong msg ID " + v1;  // wrong message
      case ERR_CLINO      : return "Clino " + v1; 
      case ERR_AZIMUTH    : return "Azimuth " + v1; 
    }
    return "";
  }

}
