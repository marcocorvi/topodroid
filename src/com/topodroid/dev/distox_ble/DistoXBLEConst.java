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
  static final String DISTOXBLE_SRV_UUID_STR        = "0000fff0-0000-1000-8000-00805f9b34fb"; // DistoXBLE service uuid
  static final String DISTOXBLE_CHRT_READ_UUID_STR  = "0000fff1-0000-1000-8000-00805f9b34fb"; //  DistoXBLE receive (from pony to app)
  static final String DISTOXBLE_CHRT_WRITE_UUID_STR = "0000fff2-0000-1000-8000-00805f9b34fb"; // DistoXBLE send (from app to pony)

  static final UUID DISTOXBLE_SERVICE_UUID    = UUID.fromString( DISTOXBLE_SRV_UUID_STR );
  static final UUID DISTOXBLE_CHRT_READ_UUID  = UUID.fromString( DISTOXBLE_CHRT_READ_UUID_STR );
  static final UUID DISTOXBLE_CHRT_WRITE_UUID = UUID.fromString( DISTOXBLE_CHRT_WRITE_UUID_STR );
}
