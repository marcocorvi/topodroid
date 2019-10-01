/* @file PTString.java
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief PocketTopo file IO - String
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// import android.util.Log;

class PTString 
{
  private String _str; //!< UTF-8 encoded zero terminated (size=length+1)

  PTString() 
  {
    _str = "";
  }

  /** cstr
   * @param val C string (zero terminated)
   *
   * note a NULL string is considered as an empty string
   */
  PTString( String val )
  {
    _str = (val == null)? "" : val;
  }

  int size() { return _str.length(); }
  String value() { return _str; }

  void read( FileInputStream fs )
  {
    int len = 0;
    try {
      int shift = 0; 
      int b = 0;
      do {
        b = fs.read( );
        len |= ( b << shift );
        shift += 7;
      } while ( (b & 0x80) != 0 );
    } catch ( IOException e ) {
    }

    if ( len > 0 ) {
      byte[] chars = new byte[ len + 1 ];
      PTFile.read( fs, chars, len );
      chars[len] = (byte)0;
      _str = new String( chars );
    } else {
      _str = "";
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

