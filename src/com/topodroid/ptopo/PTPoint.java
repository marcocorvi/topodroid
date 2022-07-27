/* @file PTPoint.java
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief PocketTopo file IO
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ptopo;

import java.io.InputStream;
import java.io.OutputStream;


public class PTPoint
{
  int _x;  //!< world X coordinate [mm]
  int _y;  //!< world Y coordinate [mm]

  PTPoint() { _x=0; _y=0; }

  PTPoint( int x, int y ) { _x=x; _y=y; }

  // default copy cstr and assignment are ok

  public int x() { return _x; } 
  public int y() { return _y; } 

  void set( int x, int y ) { _x=x; _y=y; }

  // ----------------------------------------------------------

  void read( InputStream fs )
  {
    _x = PTFile.readInt( fs );
    _y = PTFile.readInt( fs );
    // TODO return
  }

  void write( OutputStream fs )
  {
    PTFile.writeInt( fs, _x );
    PTFile.writeInt( fs, _y );
  }

  // void print( ) { TDLog.v( "PT point " + _x + " " + _y ); }

}
