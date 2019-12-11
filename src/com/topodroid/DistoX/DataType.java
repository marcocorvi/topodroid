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
package com.topodroid.DistoX;

class DataType
{
  final static int ALL   = 0;
  final static int SHOT  = 1;
  final static int CALIB = 2;
  final static int MEM   = 3;

  static int of( byte b ) 
  {
    switch ( b & 0x07 ) {
      case 1: return SHOT;
      case 2: 
      case 3: return CALIB;
      case 4: return SHOT;
    } 
    if ( b == 0x38 ) return MEM;
    return ALL;
  }

}
