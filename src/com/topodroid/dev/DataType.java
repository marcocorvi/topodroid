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
  public final static int ALL   = 0;
  public final static int SHOT  = 1;   // shot data packets
  public final static int CALIB = 2;   // calib data packets
  public final static int MEM   = 3;   // memory packet

  static int of( byte b ) 
  {
    switch ( b & 0x07 ) { // packet-data bitmask 0000.0111
      case 1: return SHOT;
      case 2: 
      case 3: return CALIB;
      case 4: return SHOT;
    } 
    if ( b == 0x38 ) return MEM; // packer-memory bitmask 0011.1000
    return ALL;
  }

}
