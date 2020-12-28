/* @file BleConst.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid BLE devices constants
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.dev;

import java.util.UUID;

class BricConst 
{
  static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

  static final UUID BRIC4_DEVICE_INFO_SERVICE_UUID   = UUID.fromString( "0000180a-0000-1000-8000-00805f9b34fb" ); // 0x180a
  static final UUID BRIC4_BATTERY_SERVICE_UUID       = UUID.fromString( "0000180f-0000-1000-8000-00805f9b34fb" ); // 0x180f
  static final UUID BRIC4_MEASURE_SERVICE_UUID       = UUID.fromString( "000058d0-0000-1000-8000-00805f9b34fb" ); // 0x58d0
  static final UUID BRIC4_MEASURE_PRIMARY_CHRT_UUID  = UUID.fromString( "000058d1-0000-1000-8000-00805f9b34fb" ); // 0x58d1
  static final UUID BRIC4_MEASURE_METADATA_CHRT_UUID = UUID.fromString( "000058d2-0000-1000-8000-00805f9b34fb" ); // 0x58d2
  static final UUID BRIC4_MEASURE_ERROR_CHRT_UUID    = UUID.fromString( "000058d3-0000-1000-8000-00805f9b34fb" ); // 0x58d3
  static final UUID BRIC4_MEASURE_LASTTIME_CHRT_UUID = UUID.fromString( "000058d4-0000-1000-8000-00805f9b34fb" ); // 0x58d4

  static final UUID BRIC4_DEVICE_CTRL_SERVICE_UUID   = UUID.fromString( "000058e0-0000-1000-8000-00805f9b34fb" ); // 0x58e0
  static final UUID BRIC4_DEVICE_CTRL_CHRT_UUID      = UUID.fromString( "000058e1-0000-1000-8000-00805f9b34fb" ); // 0x58e1

  static final UUID BRIC4_SERVICE_UUID    = UUID.fromString( "6e400001-0000-1000-8000-00805f9b34fb" );
  static final UUID BRIC4_CHAR_READ_UUID  = UUID.fromString( "6e400002-0000-1000-8000-00805f9b34fb" );
  static final UUID BRIC4_CHAR_WRITE_UUID = UUID.fromString( "6e400003-0000-1000-8000-00805f9b34fb" );

}
