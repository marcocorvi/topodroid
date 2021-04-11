/* @file DataType.java
 *
 * @author marco corvi
 * @date sept 2014
 *
 * @brief TopoDroid DistoX data types (for the downloader)
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

/** packet data types
 *  data   0000.0111
 *  memory 0011.1000
 */
public class DataType
{
  public final static int DATA_ALL   = 0;
  public final static int DATA_SHOT  = 1;   // shot data packets
  public final static int DATA_CALIB = 2;   // calib data packets
  public final static int DATA_MEM   = 3;   // memory packet
  public final static int DATA_SCAN  = 4;   // scan data packets

  static int of( byte b ) 
  {
    switch ( b & 0x07 ) { // packet-data bitmask 0000.0111
      case 1: return DATA_SHOT;
      case 2: 
      case 3: return DATA_CALIB;
      case 4: return DATA_SHOT;
    } 
    if ( b == 0x38 ) return DATA_MEM; // packer-memory bitmask 0011.1000
    return DATA_ALL;
  }

  // protocol packet types
  public static final int PACKET_NONE   = 0;
  public static final int PACKET_DATA   = 1;
  public static final int PACKET_G      = 2;
  public static final int PACKET_M      = 3;
  public static final int PACKET_VECTOR = 4;
  public static final int PACKET_REPLY  = 5;

}
