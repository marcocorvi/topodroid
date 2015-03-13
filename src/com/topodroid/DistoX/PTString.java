/** @file PTString.java
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// import android.util.Log;

class PTString 
{
  String _str; //!< UTF-8 encoded zero terminated (size=length+1)

  PTString() 
  {
    _str = "";
  }

  /** cstr
   * @param val C string (zero terminated)
   *
   * @note a NULL string is considered as an empty string
   */
  PTString( String val )
  {
    _str = (val == null)? "" : val;
  }

  int size() { return _str.length(); }
  String value() { return _str; }

  void read( FileInputStream fs )
  {
    try {
      int len = 0;
      int shift = 0; 
      int b = 0;
      do {
        b = fs.read( );
        len |= ( ((int)b) << shift );
        shift += 7;
      } while ( (b & 0x80) != 0 );

      byte[] chars = new byte[ len + 1 ];
      if ( len > 0 ) {
        fs.read( chars, 0, len );
      }
      chars[len] = (byte)0;
      _str = new String( chars );
    } catch ( IOException e ) {
    }
  }

  void write( FileOutputStream fs )
  {
    try {
      int len = _str.length();
      do {
        byte b = (byte)( len & 0x7f );
        len = len >> 7;
        if ( len > 0 ) {
          b |= 0x80;
        }
        fs.write( b );
      } while ( len > 0 );
      if ( _str.length() > 0 ) {
        byte[] chars = _str.getBytes();
        fs.write( chars, 0, _str.length() );
      }
    } catch ( IOException e ) {
    }
  }

  // void print() { Log.v( TopoDroidApp.TAG, "PTString " + _str ); }

  void set( String val ) { _str = (val == null)? "" : val; }

}

