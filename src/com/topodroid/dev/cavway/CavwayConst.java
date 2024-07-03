/* @file DistoXBLEConst.java
 *
 * @author siwei tian
 * @date aug 2022
 *
 * @brief TopoDroid DistoX BLE constants
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.cavway;

import java.util.UUID;

public class CavwayConst
{
  static final String CAVWAY_SRV_UUID_STR        = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"; // Cavway service uuid
  static final String CAVWAY_CHRT_READ_UUID_STR  = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"; // Cavway receive (from XBLE to app)
  static final String CAVWAY_CHRT_WRITE_UUID_STR = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"; // Cavway send (from app to XBLE)

  static final UUID CAVWAY_SERVICE_UUID    = UUID.fromString( CAVWAY_SRV_UUID_STR );
  static final UUID CAVWAY_CHRT_READ_UUID  = UUID.fromString( CAVWAY_CHRT_READ_UUID_STR );
  static final UUID CAVWAY_CHRT_WRITE_UUID = UUID.fromString( CAVWAY_CHRT_WRITE_UUID_STR );
}
