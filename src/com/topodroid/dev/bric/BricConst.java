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

import java.util.UUID; 
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BricConst
{
  public final static String MEAS_SRV  = "58d0";
  public final static String MEAS_PRIM = "58d1";
  public final static String MEAS_META = "58d2";
  public final static String MEAS_ERR  = "58d3";
  public final static String LAST_TIME = "58d4";
 
  public final static UUID MEAS_SRV_UUID  = BleUtils.toUuid( MEAS_SRV );
  public final static UUID MEAS_PRIM_UUID = BleUtils.toUuid( MEAS_PRIM );
  public final static UUID MEAS_META_UUID = BleUtils.toUuid( MEAS_META );
  public final static UUID MEAS_ERR_UUID  = BleUtils.toUuid( MEAS_ERR );
  public final static UUID LAST_TIME_UUID = BleUtils.toUuid( LAST_TIME );

  public final static String CTRL_SRV  = "58e0";
  public final static String CTRL_CHRT = "58e1";

  public final static UUID CTRL_SRV_UUID  = BleUtils.toUuid( CTRL_SRV );
  public final static UUID CTRL_CHRT_UUID = BleUtils.toUuid( CTRL_CHRT );

  public final static byte[] COMMAND_OFF   = { (byte)0x70, (byte)0x6f, (byte)0x77, (byte)0x65, (byte)0x72, (byte)0x20, (byte)0x6f, (byte)0x66, (byte)0x66 };
  public final static byte[] COMMAND_SCAN  = { (byte)0x73, (byte)0x63, (byte)0x61, (byte)0x6e };
  public final static byte[] COMMAND_SHOT  = { (byte)0x73, (byte)0x68, (byte)0x6f, (byte)0x74 };
  public final static byte[] COMMAND_LASER = { (byte)0x6c, (byte)0x61, (byte)0x73, (byte)0x65, (byte)0x72 };

  public final static int CMD_OFF   = 1;
  public final static int CMD_SCAN  = 2;
  public final static int CMD_SHOT  = 3;
  public final static int CMD_LASER = 4;

  public final static int CMD_SPLAY = 11;
  public final static int CMD_LEG   = 12;

  static float getDistance( byte[] bytes ) { return BleUtils.getFloat( bytes,  8 ); }
  static float getAzimuth( byte[] bytes )  { return BleUtils.getFloat( bytes, 12 ); }
  static float getClino( byte[] bytes )    { return BleUtils.getFloat( bytes, 16 ); }
  static int   getIndex( byte[] bytes )    { return BleUtils.getChar(  bytes,  0 ); }
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
    int SS = BleUtils.getShort( bytes, 6 );
    int CS = BleUtils.getShort( bytes, 7 ); // centiseconds
    Calendar date = new GregorianCalendar();
    date.set( yy, mm, dd, HH, MM, SS );
    return date.getTimeInMillis() + 10 * CS;
  }

}
