/* @file BricDebug.java
 *
 * @author marco corvi
 * @date jan 2021
 *
 * @brief BRIC4 debug functions
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.bric;

import android.util.Log;

class BricDebug
{

  static void logMeasPrim( byte[] bytes )
  {
    Log.v("DistoX-BLE", "BRIC debug MeasPrim: " + bytes.length + " date " 
      + BleUtils.getShort( bytes, 0 ) + "." + BleUtils.getChar( bytes, 2 ) + "." + BleUtils.getChar( bytes, 3 ) + " Distance " 
      + BleUtils.getFloat( bytes, 8 ) + " Azimuth " + BleUtils.getFloat( bytes, 12 ) + " Clino " + BleUtils.getFloat( bytes, 16 ) 
    );
  }

  static String measPrimToString( byte[] bytes )
  {
    return BleUtils.getShort( bytes, 0 ) + "." + BleUtils.getChar( bytes, 2 ) + "." + BleUtils.getChar( bytes, 3 ) + " "
         + BleUtils.getFloat( bytes, 8 ) + " " + BleUtils.getFloat( bytes, 12 ) + " " + BleUtils.getFloat( bytes, 16 );
  }

  static void logMeasMeta( byte[] bytes )
  {
    Log.v("DistoX-BLE", "BRIC debug MeasMeta: " + bytes.length + " Idx " 
      + BleUtils.getInt( bytes, 0 ) + " dip " + BleUtils.getFloat( bytes, 4 ) + " roll " + BleUtils.getFloat( bytes, 8 ) + " temp " 
      + BleUtils.getFloat( bytes, 12 ) + " samples " + BleUtils.getShort( bytes, 16 ) + " type " + BleUtils.getChar( bytes, 18 ) 
    );
  }

  static void logMeasErr( byte[] bytes )
  {
    Log.v("DistoX-BLE", "BRIC debug MeasErr: " + bytes.length + " Err1 " 
      + BleUtils.getChar( bytes, 0 ) + ": " + BleUtils.getFloat( bytes, 1 ) + " " + BleUtils.getFloat( bytes, 5 ) + " Err2 " 
      + BleUtils.getChar( bytes, 9 ) + ": " + BleUtils.getFloat( bytes, 10 ) + " " + BleUtils.getFloat( bytes, 14 ) 
    );
  }

  static void log( String msg, byte[] bytes )
  {
    if ( bytes != null ) {
      Log.v("DistoX-BLE", "BRIC debug: " + msg + " " + BleUtils.bytesToString( bytes ) );
    }
  }

}
