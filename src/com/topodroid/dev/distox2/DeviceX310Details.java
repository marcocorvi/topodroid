/* @file DeviceX310Details.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief DistoX2 (X310) details
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox2;

public class DeviceX310Details
{
  static final int MAX_INDEX_X310 = 1064;

  // address of status word in DistoX2 memory
  final static int[] STATUS_ADDRESS = {
    0xc006, 0xc006, 0xc006, 0xc034, 0xc044, 0xc044, 0xc044,
  };

  public final static int FIRMWARE_ADDRESS = 0xe000;
  public final static int HARDWARE_ADDRESS = 0xe004;

  // head-tail command
  // final static int mHeadTailAddress = 0xe008;
  public static final byte[] HeadTail = { 0x38, 0x08, (byte)0xe0 }; // address 0xe008

  public static boolean boundHeadTail( int[] ht )
  {
    if ( ht[0] < 0 ) ht[0] = 0;
    if ( ht[1] > MAX_INDEX_X310 ) ht[1] = MAX_INDEX_X310;
    return ( ht[0] < ht[1] );
  }

  // returns two 16-bit values
  // first value  = head segment index
  // second value = tail packet index
  // data is added in segment but removed in packets
  // segment to address: block  = segment / 56
  //                     offset = segment % 56
  //                     address = block * 1024 + offset * 18
  // packet to address:  segment = packet / 2
  //                     packet_nr = packet % 2

  /** @return the memory address of a given segment
   * @param segment memory segment
   */
  static private int segmentToAddress( int segment )
  { return ( segment / 56 ) * 1024 + ( segment % 56 ) * 18; }

  /** @return the address (???) of a given packet
   * @param packet packet
   */
  static int packetToAddress( int packet ) 
  { return segmentToAddress( packet/2 ); }

  /** convert number of packets to number of data
   * @param packet  number of packets
   * @return number of data (half the number of packets)
   */
  static int packetToNumber( int packet ) 
  { return packet % 2; }

  /** @return memory address for a given memory position (index)
   * @param index    memory position
   */
  static int index2addr( int index )
  {
    int addr = 0;
    while ( index >= 56 ) {
      index -= 56;
      addr += 0x400;
    }
    addr += 18 * index;
    return addr;
  }

  // status word (high bits in the next byte)
  final static int MASK_DIST_UNIT    = 0x0007; // distance unit mask
  final static int BIT_ANGLE_UNIT    = 0x0008; // angle unit
  final static int BIT_ENDPIECE_REF  = 0x0010; // endpiece reference
  final static int BIT_CALIB_MODE    = 0x0020; // calib-mode on
  final static int BIT_DISPLAY_LIGHT = 0x0040; // display illumination on
  final static int BIT_BEEP          = 0x0080; // beep on
  final static int BIT_TRIPLE_SHOT   = 0x0100; // triple shot check on (2.4+)
  final static int BIT_BLUETOOTH     = 0x0200; // bluetooth on
  final static int BIT_LOCKED_POWER  = 0x0400; // locked power off
  final static int BIT_CALIB_SESSION = 0x0800; // new calibration session
  final static int BIT_ALKALINE      = 0x1000; // battery = alkaline
  final static int BIT_SILENT_MODE   = 0x2000; // silent mode
  final static int BIT_REVERSE_SHOT  = 0x4000; // reverse measurement (2.4+)

  private static final byte CALIB_BIT = (byte)0x20; // X2 calib bit

  static boolean isCalibMode( byte b ) { return ( ( b & CALIB_BIT ) == CALIB_BIT ); }
  static boolean isNotCalibMode( byte b ) { return ( ( b & CALIB_BIT ) == 0 ); }

}
