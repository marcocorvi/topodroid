/* @file SapConst.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SAP-5, SAP-5 bluetooth LE constants and commands
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING UNTESTED
 * after a file provided by Ph. Underwood
 */
package com.topodroid.dev.sap;

// import com.topodroid.dev.ble.BleUtils;

import java.util.UUID;

public class SapConst 
{
  // static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

  static final String SAP5_SRV_UUID_STR        = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"; // SAP-5 service uuid
  static final String SAP5_CHRT_READ_UUID_STR  = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"; // SAP-5 transmit (from pony to app)
  static final String SAP5_CHRT_WRITE_UUID_STR = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"; // SAP-5 receive (from app to pony)

  static final UUID SAP5_SERVICE_UUID    = UUID.fromString( SAP5_SRV_UUID_STR );
  static final UUID SAP5_CHRT_READ_UUID  = UUID.fromString( SAP5_CHRT_READ_UUID_STR );
  static final UUID SAP5_CHRT_WRITE_UUID = UUID.fromString( SAP5_CHRT_WRITE_UUID_STR );

  static final String SAP6_SRV_UUID_STR        = "137c4435-8a64-4bcb-93f1-3792c6bdc965"; // SAP-6 service uuid
  static final String SAP6_CHRT_READ_UUID_STR  = "137c4435-8a64-4bcb-93f1-3792c6bdc968"; // SAP-6 transmit (from pony to app)
  static final String SAP6_CHRT_WRITE_UUID_STR = "137c4435-8a64-4bcb-93f1-3792c6bdc967"; // SAP-6 receive (from app to pony)
  static final String SAP6_DSCR_NITIFICATION   = "00002902-0000-1000-8000-00805f9b34fb"; // SAP-6 notification descriptor

  static final public UUID SAP6_SERVICE_UUID      = UUID.fromString( SAP6_SRV_UUID_STR );
  static final public UUID SAP6_CHRT_READ_UUID    = UUID.fromString( SAP6_CHRT_READ_UUID_STR );
  static final public UUID SAP6_CHRT_WRITE_UUID   = UUID.fromString( SAP6_CHRT_WRITE_UUID_STR );
  static final public UUID SAP6_DSCR_NOTIFICATION = UUID.fromString( SAP6_DSCR_NITIFICATION );

  static final byte SAP_ACK               = (byte)0x55;

  static final public byte SAP_START_CAL  = (byte)0x31;
  static final public byte SAP_STOP_CAL   = (byte)0x30;
  static final public byte SAP_DEVICE_OFF = (byte)0x34;
  static final public byte SAP_LASER_ON   = (byte)0x36;
  static final public byte SAP_LASER_OFF  = (byte)0x37;
  static final public byte SAP_TAKE_SHOT  = (byte)0x38;

}
