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
package com.topodroid.DistoX;

import java.util.UUID;

class BleConst 
{
  // static final String BLE_SERVICE_STRING = "7D2EA28A-F7BD-485A-BD9D-92AD6ECFE900";
  static final String BLE_SERVICE_STRING = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"; // SAP service uuid
  static final UUID BLE_SERVICE_UUID = UUID.fromString( BLE_SERVICE_STRING );

  // static final String BLE_CHAR_WRITE_STRING = "7D2EBAAD-F7BD-485A-BD9D-92AD6ECFE93E";
  // static UUID BLE_CHAR_WRITE_UUID = UUID.fromString( BLE_CHAR_WRITE_STRING );

  // // static final String BLE_DESC_WRITE_STRING = "7D2EBAAD-F7BD-485A-BD9D-92AD6ECFE93F";
  // static final String BLE_CHAR_WRITE_STRING = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"; // SAP receive (from app to pony)
  // static UUID BLE_DESC_WRITE_UUID = UUID.fromString( BLE_DESC_WRITE_STRING );

  // static final String BLE_CHAR_READ_STRING  = "7D2EDEAD-F7BD-485A-BD9D-92AD6ECFE92E";
  static final String BLE_CHAR_READ_STRING  = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"; // SAP transmit (from pony to app)
  static final UUID BLE_CHAR_READ_UUID  = UUID.fromString( BLE_CHAR_READ_STRING );

  // static final String BLE_DESC_READ_STRING  = "00002902-0000-1000-8000-00805f9b34fb";
  // static UUID BLE_DESC_READ_UUID  = UUID.fromString( BLE_DESC_READ_STRING );

  // static final String BLE_CONFIG_DESC_STRING = "00002902-0000-1000-8000-00805f9b34fb";
  // static UUID BLE_CONFIG_DESC_UUID = UUID.fromString(BLE_CONFIG_DESC_STRING);

  // static final String BLE_CONFIG_DESC_SHORT_ID = "2902";
}
