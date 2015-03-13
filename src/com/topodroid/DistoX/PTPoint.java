/** @file PTPoint.java
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief PocketTopo file IO
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.DistoX;

import java.io.FileInputStream;
import java.io.FileOutputStream;

// import android.util.Log;


class PTPoint
{
  int _x;  //!< world X coordinate [mm]
  int _y;  //!< world Y coordinate [mm]

  PTPoint() { _x=0; _y=0; }

  PTPoint( int x, int y ) { _x=x; _y=y; }

  // deafult copy cstr and assignment are ok

  int x() { return _x; } 
  int y() { return _y; } 

  void set( int x, int y ) { _x=x; _y=y; }

  // ----------------------------------------------------------

  void read( FileInputStream fs )
  {
    _x = PTFile.readInt( fs );
    _y = PTFile.readInt( fs );
    // TODO return
  }

  void write( FileOutputStream fs )
  {
    PTFile.writeInt( fs, _x );
    PTFile.writeInt( fs, _y );
  }

  // void print() { Log.v( TopoDroidApp.TAG, "Point " + _x + " " + _y ); }

}
