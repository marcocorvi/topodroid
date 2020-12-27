
/* @file DeviceA3Details.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief DistoX1 (A3) details
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

public class DeviceA3Details
{
  static final int MAX_ADDRESS_A3 = 0x8000;

  // address of status word in DistoX2 memory
  final static int mStatusAddress = 0x8000;

  private static final byte CALIB_BIT = (byte)0x08; // X1 calib bit

  public static boolean isCalibMode( byte b ) { return ( ( b & CALIB_BIT ) == CALIB_BIT ); }
  public static boolean isNotCalibMode( byte b ) { return ( ( b & CALIB_BIT ) == 0 ); }

  // heat-tail command
  public static final byte[] HeadTail = { 0x38, 0x20, (byte)0xc0 }; // address 0xc020

  public static boolean checkHeadTail( int[] ht ) 
  {
    return ( ht[0] < 0 || ht[0] >= DeviceA3Details.MAX_ADDRESS_A3 || ht[1] < 0 || ht[1] >= DeviceA3Details.MAX_ADDRESS_A3 );
  }
}
