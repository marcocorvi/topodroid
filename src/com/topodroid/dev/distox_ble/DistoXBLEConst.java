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
package com.topodroid.dev.distox_ble;

import java.util.UUID;

public class DistoXBLEConst
{
  static final String DISTOXBLE_SRV_UUID_STR        = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"; // DistoXBLE service uuid
  static final String DISTOXBLE_CHRT_READ_UUID_STR  = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"; // DistoXBLE receive (from XBLE to app)
  static final String DISTOXBLE_CHRT_WRITE_UUID_STR = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"; // DistoXBLE send (from app to XBLE)

  static final UUID DISTOXBLE_SERVICE_UUID    = UUID.fromString( DISTOXBLE_SRV_UUID_STR );
  static final UUID DISTOXBLE_CHRT_READ_UUID  = UUID.fromString( DISTOXBLE_CHRT_READ_UUID_STR );
  static final UUID DISTOXBLE_CHRT_WRITE_UUID = UUID.fromString( DISTOXBLE_CHRT_WRITE_UUID_STR );
}
