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
package com.topodroid.dev;

import java.util.UUID;

class SapConst 
{
  static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

  static final UUID SAP5_SERVICE_UUID    = UUID.fromString( "6e400001-b5a3-f393-e0a9-e50e24dcca9e" ); // SAP service uuid
  static final UUID SAP5_CHAR_READ_UUID  = UUID.fromString( "6e400002-b5a3-f393-e0a9-e50e24dcca9e" ); // SAP transmit (from pony to app)
  static final UUID SAP5_CHAR_WRITE_UUID = UUID.fromString( "6e400003-b5a3-f393-e0a9-e50e24dcca9e" ); // SAP receive (from app to pony)

}
