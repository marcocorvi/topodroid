
/* @file DeviceA3Details.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief DistoX1 (A3) details
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class DeviceA3Details
{
  static final int MAX_ADDRESS_A3 = 0x8000;

  // address of status word in DistoX2 memory
  final static int mStatusAddress = 0x8000;

  private static final byte CALIB_BIT = (byte)0x08; // X1 calib bit

  static boolean isCalibMode( byte b ) { return ( ( b & CALIB_BIT ) == CALIB_BIT ); }
  static boolean isNotCalibMode( byte b ) { return ( ( b & CALIB_BIT ) == 0 ); }

  // heat-tail command
  static final byte[] HeadTail = { 0x38, 0x20, (byte)0xc0 }; // address 0xc020

}
