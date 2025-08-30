/* @file DistoXBLEConst.java
 *
 * @author siwei tian
 * @date jul 2024
 *
 * @brief TopoDroid Cavway BLE constants
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

  public static final int CALIB_OFF        = 0x30;
  public static final int CALIB_ON         = 0x31;
  public static final int CALIB_CONVERT    = 0x32;
  public static final int SILENT_OFF       = 0x33;
  public static final int DISTOX_OFF       = 0x34;
  public static final int DISTOX_35        = 0x35;
  public static final int LASER_ON         = 0x36;
  public static final int LASER_OFF        = 0x37;
  public static final int MEASURE          = 0x38;

  public static final int FLAG_FEATURE   = 0x7; // Cavway flags: values are complementary than in the device 
  public static final int FLAG_RIDGE     = 0x6;
  public static final int FLAG_BACKSIGHT = 0x5;
  public static final int FLAG_GENERIC   = 0x4;
  // 0x3 0x2 0x1 unused
  public static final int FLAG_NONE      = 0x0;
}
