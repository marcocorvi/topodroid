/* @file JedeyeConst.java
 *
 * @author sebastien kister
 * @date 2026
 *
 * @brief TopoDroid JedEye bluetooth LE constants
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * JedEye uses the SAP6 17-byte shot frame and DistoX2/SAP6 single-byte
 * command set, but advertises its own 128-bit GATT UUIDs so it has a
 * distinct identity in BLE scanners.
 */
package com.topodroid.dev.jedeye;

import java.util.UUID;

public class JedeyeConst
{
  static final String JEDEYE_SRV_UUID_STR        = "4a454445-5945-0001-8000-00805f9b34fb"; // service
  static final String JEDEYE_CHRT_READ_UUID_STR  = "4a454445-5945-0002-8000-00805f9b34fb"; // notify (device -> app)
  static final String JEDEYE_CHRT_WRITE_UUID_STR = "4a454445-5945-0003-8000-00805f9b34fb"; // write  (app -> device)

  static final public UUID JEDEYE_SERVICE_UUID    = UUID.fromString( JEDEYE_SRV_UUID_STR );
  static final public UUID JEDEYE_CHRT_READ_UUID  = UUID.fromString( JEDEYE_CHRT_READ_UUID_STR );
  static final public UUID JEDEYE_CHRT_WRITE_UUID = UUID.fromString( JEDEYE_CHRT_WRITE_UUID_STR );
}
