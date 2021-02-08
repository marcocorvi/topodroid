/* @file SapConst.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SAP5 bluetooth LE constants
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * WARNING TO BE FINISHED
 */
package com.topodroid.dev.sap;

// import com.topodroid.dev.bric.BleUtils;

import java.util.UUID;

class SapConst 
{
  // static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

  static final String SAP5_SRV_UUID_STR        = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"; // SAP service uuid
  static final String SAP5_CHRT_READ_UUID_STR  = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"; // SAP transmit (from pony to app)
  static final String SAP5_CHRT_WRITE_UUID_STR = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"; // SAP receive (from app to pony)

  static final UUID SAP5_SERVICE_UUID    = UUID.fromString( SAP5_SRV_UUID_STR );
  static final UUID SAP5_CHRT_READ_UUID  = UUID.fromString( SAP5_CHRT_READ_UUID_STR );
  static final UUID SAP5_CHRT_WRITE_UUID = UUID.fromString( SAP5_CHRT_WRITE_UUID_STR );

}
