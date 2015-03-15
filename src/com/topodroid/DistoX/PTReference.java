/** @file PTReference.java
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


class PTReference
{
    PTId _station;
    long _east;     //!< east coordinate [mm]
    long _north;    //!< north coordinate [mm]
    int _altitude; //!< altitude [mm above sea level]
    PTString _comment;

    PTReference()
    {
      _station = new PTId();
      _east = 0L;
      _north = 0L;
      _altitude = 0;
      _comment = new PTString();;
    }

    PTReference( String id, long east, long north, int altitude, String comment )
    {
      _station = new PTId();
      _station.set( id );
      _east = east;
      _north = north;
      _altitude = altitude;
      _comment = new PTString( comment );
    }

    // ---------------------------------------------------

    PTId station() { return _station; }

    long east() { return _east; }
    long north() { return _north; }
    int altitude() { return _altitude; }

    void setEast( long east ) { _east = east; }
    void setNorth( long north ) { _north = north; }
    void setAltitude( int altitude ) { _altitude = altitude; }

    void set( int e, int n, int a ) 
    {
      _east = e;
      _north = n;
      _altitude = a;
    }

    String comment() { return _comment.value(); }
    void setComment( String str ) { _comment.set( str ); }

    // ---------------------------------------------------


    void read( FileInputStream fs )
    {
      _station.read( fs );
      _east     = PTFile.readLong( fs );
      _north    = PTFile.readLong( fs );
      _altitude = PTFile.readInt( fs );
      _comment.read( fs );
    }

    void write( FileOutputStream fs )
    {
      _station.write( fs );
      PTFile.writeLong( fs, _east );
      PTFile.writeLong( fs, _north );
      PTFile.writeInt( fs, _altitude );
      _comment.write( fs );
    }

    // void print()
    // { 
    //   Log.v( TopoDroidApp.TAG, "reference: east " + _east + " north " + _north + " alt " + _altitude );
    //   _station.print();
    //   _comment.print();
    // }

/*
void 
PTreference::printTherion( FILE * fp )
{
  std::string station = _station.toString();
  fprintf(fp, "    fix %s %8.2f %8.2f %8.2f\n",
    station.c_str(), _east/1000.0, _north/1000.0, _altitude/1000.0 );
  if ( _comment.size() > 0 ) {
    fprintf(fp, "    # %s\n", _comment.value() );
  }
}
*/

}
