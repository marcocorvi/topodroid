/** @file PTMapping.java
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
package com.topodroid.DistoX;

import java.io.FileInputStream;
import java.io.FileOutputStream;

// import android.util.Log;

class PTMapping
{
  static final private int XTHERION_FACTOR = 25;

    private PTPoint _origin;
    private int _scale;   //!< scale 50 .. 20000

    PTMapping()
    {
      _scale = XTHERION_FACTOR;
      _origin = new PTPoint();
    }

    // -----------------------------------------------------

    PTPoint origin() { return _origin; }

    private void setOrigin( int x, int y ) { _origin.set( x, y ); }

    int scale() { return _scale; }
    void setScale( int s )
    {
      if ( s >= 50 && s <= 20000 ) _scale = s;
    }

    void read( FileInputStream fs )
    {
      _origin.read( fs );
      _scale = PTFile.readInt( fs );
      TDLog.Log( TDLog.LOG_PTOPO, "PT Mapping origin " + _origin._x + " " + _origin._y + " scale " + _scale );
    }

    void write( FileOutputStream fs )
    {
      _origin.write( fs );
      PTFile.writeInt( fs, _scale );
    }

    // void print() { Log.v( TopoDroidApp.TAG, "mapping: scale " + _scale ); _origin.print(); }

    void clear()
    {
      setOrigin( 0, 0 );
      _scale = XTHERION_FACTOR;
    }
}

