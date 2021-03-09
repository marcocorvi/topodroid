/* @file BleConst.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BRIC4 constants
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.ble;

import java.util.UUID; 

public class BleConst
{
  public final static String DEVICE_SRV  = "00001800" + BleUtils.STANDARD_UUID;
  public final static String DEVICE_00   = "00002a00" + BleUtils.STANDARD_UUID; // name
  public final static String DEVICE_01   = "00002a01" + BleUtils.STANDARD_UUID; // appearance
  public final static String DEVICE_04   = "00002a04" + BleUtils.STANDARD_UUID; // preferred connection params
  public final static String DEVICE_06   = "00002a06" + BleUtils.STANDARD_UUID; //

  public final static String INFO_SRV  = "0000180a" + BleUtils.STANDARD_UUID;
  public final static String INFO_23   = "00002a23" + BleUtils.STANDARD_UUID;
  public final static String INFO_24   = "00002a24" + BleUtils.STANDARD_UUID; // model number
  public final static String INFO_25   = "00002a25" + BleUtils.STANDARD_UUID;
  public final static String INFO_26   = "00002a26" + BleUtils.STANDARD_UUID; // fw
  public final static String INFO_27   = "00002a27" + BleUtils.STANDARD_UUID; // hw
  public final static String INFO_28   = "00002a28" + BleUtils.STANDARD_UUID;
  public final static String INFO_29   = "00002a29" + BleUtils.STANDARD_UUID; // manufacturer
 
  public final static String BATTERY_SRV  = "0000180f" + BleUtils.STANDARD_UUID;
  public final static String BATTERY_LVL  = "00002a19" + BleUtils.STANDARD_UUID;

  public final static UUID DEVICE_SRV_UUID  = UUID.fromString( DEVICE_SRV );
  public final static UUID DEVICE_00_UUID   = UUID.fromString( DEVICE_00 );
  public final static UUID DEVICE_01_UUID   = UUID.fromString( DEVICE_01 );
  public final static UUID DEVICE_04_UUID   = UUID.fromString( DEVICE_04 );
  public final static UUID DEVICE_06_UUID   = UUID.fromString( DEVICE_06 );

  public final static UUID INFO_SRV_UUID  = UUID.fromString( INFO_SRV );
  public final static UUID INFO_23_UUID   = UUID.fromString( INFO_23 );
  public final static UUID INFO_24_UUID   = UUID.fromString( INFO_24 );
  public final static UUID INFO_25_UUID   = UUID.fromString( INFO_25 );
  public final static UUID INFO_26_UUID   = UUID.fromString( INFO_26 );
  public final static UUID INFO_27_UUID   = UUID.fromString( INFO_27 );
  public final static UUID INFO_28_UUID   = UUID.fromString( INFO_28 );
  public final static UUID INFO_29_UUID   = UUID.fromString( INFO_29 );

  public final static UUID BATTERY_SRV_UUID  = UUID.fromString( BATTERY_SRV );
  public final static UUID BATTERY_LVL_UUID  = UUID.fromString( BATTERY_LVL );

}
