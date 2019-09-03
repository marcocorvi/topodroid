/* @file PTFile.java
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

import java.util.ArrayList;
import java.util.HashMap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// import android.util.Log;

class PTFile
{

  static private byte bytes2byte( byte[] b )
  {
    final ByteBuffer bb = ByteBuffer.wrap( b );
    bb.order( ByteOrder.LITTLE_ENDIAN );
    return bb.get(0);
  }
 
  static private int bytes2int( byte[] b )
  {
    final ByteBuffer bb = ByteBuffer.wrap( b );
    bb.order( ByteOrder.LITTLE_ENDIAN );
    return bb.getInt();
  }
 
  static private byte[] int2bytes( int i )
  { 
    final ByteBuffer bb = ByteBuffer.allocate( Integer.SIZE / Byte.SIZE );
    bb.order( ByteOrder.LITTLE_ENDIAN );
    bb.putInt( i );
    return bb.array();
  }

  static private int bytes2short( byte[] b )
  {
    final ByteBuffer bb = ByteBuffer.wrap( b );
    bb.order( ByteOrder.LITTLE_ENDIAN );
    return bb.getShort();
  }
 
  static private byte[] short2bytes( short i )
  { 
    final ByteBuffer bb = ByteBuffer.allocate( Short.SIZE / Byte.SIZE );
    bb.order( ByteOrder.LITTLE_ENDIAN );
    bb.putShort( i );
    return bb.array();
  }

  static private long bytes2long( byte[] b )
  {
    final ByteBuffer bb = ByteBuffer.wrap( b );
    bb.order( ByteOrder.LITTLE_ENDIAN );
    return bb.getLong();
  }
 
  static private byte[] long2bytes( long i )
  { 
    final ByteBuffer bb = ByteBuffer.allocate( Long.SIZE / Byte.SIZE );
    bb.order( ByteOrder.LITTLE_ENDIAN );
    bb.putLong( i );
    return bb.array();
  }

  private static void write( FileOutputStream fs, byte[] b, int n )
  {
    try {
      fs.write( b, 0, n );
    } catch ( IOException e ) {
      TDLog.Error( "IO error on write " + n + " bytes: " + e.toString() );
    }
  }

  // used also by PTString
  //  @param fs    input stream
  //  @param b     byte array to store read bytes (size n+1)
  //  @param n     total number of bytes to read
  static void read( FileInputStream fs, byte[] b, int n )
  {
    try {
      int nread  = 0; // number of bytes that have been read
      int toread = n; // number of bytes still to read
      do {
        int nn = fs.read( b, nread, toread ); // 401126 reports an ArrayOutOfBound here but it does not make sense
	    nread  += nn;
	    toread -= nn;
      } while ( nread < n );
    } catch ( IOException e ) {
      TDLog.Error( "IO error on read " + n + " bytes: " + e.getMessage() );
    }
  }

  static byte readByte( FileInputStream fs )
  {
    byte[] b = new byte[1];
    read( fs, b, 1 );
    return bytes2byte( b );
  }

  static void writeByte( FileOutputStream fs, byte b )
  {
    try { 
      fs.write( b );
    } catch ( IOException e ) {
    }
  }

  static int readInt( FileInputStream fs )
  {
    byte[] b = new byte[4];
    read( fs, b, 4 );
    return bytes2int( b );
  }

  static void writeInt( FileOutputStream fs, int i ) 
  {
    byte[] b = int2bytes( i );
    write( fs, b, 4 );
  }

  static short readShort( FileInputStream fs )
  {
    byte[] b = new byte[2];
    read( fs, b, 2 );
    return (short)bytes2short( b );
  }

  static void writeShort( FileOutputStream fs, short i ) 
  {
    byte[] b = short2bytes( i );
    write( fs, b, 2 );
  }

  static long readLong( FileInputStream fs )
  {
    byte[] b = new byte[8];
    read( fs, b, 8 );
    return bytes2long( b );
  }

  static void writeLong( FileOutputStream fs, long i ) 
  {
    byte[] b = long2bytes( i );
    write( fs, b, 8 );
  }

  // ---------------------------------------------------------------
  // ---------------------------------------------------------------

  static final float INT16_2_DEG = 0.005493164f;
  static final float DEG_2_INT16 = 182.0444444f; /* 65536 / 360  */
  // #define INT16_MAX     65536       /* 1<<16 */

  static final float INT8_2_DEG = 0.711111111f;
  static final float DEG_2_INT8 = 1.40625f;

  static short DEG_2_ANGLE( float d ) // u_int16_t
  {
    if ( d < 0.0f ) d += 360.0f; 
    return (short)( d * DEG_2_INT16 );
  }

  static float ANGLE_2_DEG( float a ) { return a * INT16_2_DEG; }

  static short DEG_2_CLINO( float d ) // u_int16_t
  {
    if ( d < 0.0f ) d += 360.0f;
    return (short)( d * DEG_2_INT16 );
  }

  static float CLINO_2_DEG( float c ) 
  { 
    float d = c * INT16_2_DEG;
    if ( d > 180.0f ) d -= 360.0f;
    return d;
  }

  static byte DEG_2_ROLL( float d )
  {
    if ( d < 0.0f ) d += 360.0f; 
    return (byte)(int)( d * DEG_2_INT8 );
  }

  static float ROLL_2_DEG( float r )
  {
    return r * INT8_2_DEG;
  }

    // int _trip_count;
    // int _shot_count;
    // int _ref_count;
    private ArrayList< PTTrip > _trips;
    private ArrayList< PTShot > _shots;
    private ArrayList< PTReference > _references;
    private HashMap<String,Integer> stationsId;

    private PTMapping _overview;
    private PTDrawing _outline;
    private PTDrawing _sideview;

    // String[] _color_line;  // color --> th_line
    // String[] _color_point; // color --> th_point
    // static String _color_default_line[] =
    //   { "user", 
    //     "wall",         // 1 black
    //     "contour",      // 2 gray
    //     "rock-border",  // 3 brown
    //     "border",       // 4 blue
    //     "pit",          // 5 red
    //     "arrow"         // 6 green
    //   };
    // static String _color_default_point[] =
    //   { "clay", 
    //     "station",     // 1 black
    //     "block",       // 2 gray
    //     "stalactite",  // 3 brown
    //     "water-flow",  // 4 blue
    //     "anchor",      // 5 red
    //     "debris"       // 6 green
    //   };

    PTFile()
    {
      stationsId = new HashMap<String,Integer>();  // indices of stations (for TopoDroid export)

      _trips = new ArrayList<>();
      _shots = new ArrayList<>();
      _references = new ArrayList<>();
      _overview = new PTMapping();
      _outline  = new PTDrawing();
      _sideview = new PTDrawing();
      // _color_line = _color_default_line;
      // _color_point = _color_default_point;
    }

    // -----------------------------------------------------

    // -----------------------------------------------------

    int tripCount() { return _trips.size(); }
    PTTrip getTrip( int k ) { return _trips.get(k); }
 
    int shotCount() { return _shots.size(); }
    PTShot getShot( int k ) { return _shots.get(k); }
 
    int refCount() { return _references.size(); }
    PTReference getRef( int k ) { return _references.get(k); }
 
    PTMapping getOverview() { return _overview; }
    PTDrawing getOutline()  { return _outline;  }
    PTDrawing getSideview() { return _sideview; }

    // -----------------------------------------------------

    // void setColorLine( String[] line )   { _color_line = line; }
    // void setColorPoint( String[] point ) { _color_point = point; }


  private void clear()
    {
      _trips.clear();
      _shots.clear();
      _references.clear();
      _overview.clear();
      _outline.clear();
      _sideview.clear();
    }



    void read( FileInputStream fs )
    {
      clear();
      // read ID and version
      byte[] bytes = new byte[4];
      read( fs, bytes, 4 );
      // assert bytes == Top3
      TDLog.Log( TDLog.LOG_PTOPO, "PT ID " + bytes[0] + bytes[1] + bytes[2] );

      int tc = PTFile.readInt( fs ); 
      // Log.v("PTDistoX", "trip count " + tc );

      for (int k=0; k<tc; ++k ) {
        PTTrip trip = new PTTrip();
        trip.read( fs );
        _trips.add( trip );
      }
    
      int sc = PTFile.readInt( fs );
      // Log.v("PTDistoX", "shot count " + sc );

      for ( int k=0; k<sc; ++k ) {
        PTShot shot = new PTShot();
        shot.read( fs );
        _shots.add( shot );
      }
    
      int rc = PTFile.readInt( fs );
      TDLog.Log( TDLog.LOG_PTOPO, "PT trips " + tc + " shots " + sc + " refs " + rc );
      // Log.v( "PTDistoX", "PT trips " + tc + " shots " + sc + " refs " + rc );
      for ( int k=0; k<rc; ++k ) {
        PTReference ref = new PTReference();
        ref.read( fs );
        _references.add( ref );
      }
      
      _overview.read( fs );
      _outline.read( fs );
      _sideview.read( fs );
    }

    void write( FileOutputStream fs ) 
    {
      try {
        byte[] header = { 'T', 'o', 'p', (byte)3 };
        fs.write( header, 0, 4 );
      } catch( IOException e ) {
      }
      PTFile.writeInt( fs, _trips.size() );
      for ( PTTrip t : _trips ) t.write( fs );
      PTFile.writeInt( fs, _shots.size() );
      for ( PTShot s : _shots ) s.write( fs );
      PTFile.writeInt( fs, _references.size() );
      for ( PTReference r : _references ) r.write( fs );
      _overview.write( fs );
      _outline.write( fs );
      _sideview.write( fs );
    }

    // void print()
    // { 
    //   Log.v( TopoDroidApp.TAG, "FILE trips " + _trips.size() + " shots " + _shots.size() +
    //                            " refs " + _references.size() );
    //   Log.v( TopoDroidApp.TAG, "TRIPS");
    //   for ( PTTrip t : _trips ) t.print();
    //   Log.v( TopoDroidApp.TAG, "SHOTS");
    //   for ( PTShot s : _shots ) s.print();
    //   Log.v( TopoDroidApp.TAG, "REFERENCES");
    //   for ( PTReference r : _references ) r.print();
    //   Log.v( TopoDroidApp.TAG, "OVERVIEW");
    //   _overview.print();
    //   Log.v( TopoDroidApp.TAG, "OUTLINE");
    //   _outline.print();
    //   Log.v( TopoDroidApp.TAG, "SIDEVIEW");
    //   _sideview.print();
    // }

/*
// void 
// PTfile::printTherion( const char * prefix )
// {
//   if ( prefix == NULL || strlen(prefix) == 0 )
//     prefix = "cave";
//   std::ostringstream oss;
//   std::ostringstream oss_plan;
//   std::ostringstream oss_ext;
//   oss << prefix << ".th";
//   oss_plan << prefix << "-p.th2";
//   oss_ext  << prefix << "-s.th2";
//   const char * outlinefile  = oss_plan.str().c_str();
//   const char * sideviewfile = oss_ext.str().c_str();
// 
//   TDPath.checkPath( oss.str().c_str() );
//   FileOutputStream fp = fopen( oss.str().c_str(), "w" );
//   if ( fp == NULL ) return; // FIXME
//   fprintf(fp, "encoding UTF-8\n");
//   fprintf(fp, "survey survey_name\n\n");
// 
//   fprintf(fp, "  centerline\n");
//   for ( int r=0; r < _ref_count; ++r ) {
//     _references[r].printTherion( fp );
//   }
//   fprintf(fp, "  endcenterline\n\n");
// 
//   bool no_trip_shot = false;
//   for ( int t=0; t < _trip_count; ++t ) {
//     int extend = 1;
//     _trips[t].printTherion( fp );
//     for ( int s=0; s < _shot_count; ++s ) {
//       if ( _shots[s].tripIndex() == -1 ) no_trip_shot = true;
//       if ( _shots[s].tripIndex() == t ) {
//         _shots[s].printTherion( fp, extend );
//       }
//     }
//     fprintf(fp, "  endcenterline\n\n");
//   }
//   if ( no_trip_shot ) {
//     int extend = 1;
//     fprintf(fp, "  centerline\n");
//     fprintf(fp, "    declination - degrees\n");
//     fprintf(fp, "    data normal from to length compass clino\n");
//     fprintf(fp, "    extend right\n");
//     for ( int s=0; s < _shot_count; ++s ) {
//       if ( _shots[s].tripIndex() == -1 ) {
//         _shots[s].printTherion( fp, extend );
//       }
//     }
//     fprintf(fp, "  endcenterline\n\n");
//   }
//   fprintf(fp, "  input %s\n\n", outlinefile );
//   fprintf(fp, "  input %s\n\n", sideviewfile );
//   // _overview.printTherion( fp );
//   fprintf(fp, "endsurvey\n");
//   fclose( fp );
// 
//   fp = fopen( outlinefile, "w" );
//   if ( fp ) {
//     _outline.printTherion( fp, "outline", "plan", _color_point, _color_line );
//     fclose( fp );
//   }
// 
//   fp = fopen( sideviewfile, "w" );
//   if ( fp ) {
//     _sideview.printTherion( fp, "sideview", "extended", _color_point, _color_line );
//     fclose( fp );
//   }
// 
// }
*/


    /** add a trip
     * @param y   year
     * @param m   month
     * @param d   day
     * @param declination magnetic declination
     * @param comment     comment
     * @return trip index
     */
    int addTrip( int y, int m, int d, float declination, String comment )
    {
      PTTrip trip = new PTTrip();
      trip.setTime( y, m, d );
      trip.setDeclination( declination );
      trip.setComment( comment );
      _trips.add( trip );
      return _trips.size();
    }

    private int getId( String name )
    {
      Integer intId = stationsId.get( name );
      if ( intId == null ) {
        int id = DistoXStationName.toInt( name );
        stationsId.put( name, Integer.valueOf( id ) );
        return id;
      }
      return intId.intValue();
    }

    /**
     * @return number of shots
     */
    int addShot( short trip, String from, String to, 
                 float distance,    // [m]
                 float azimuth,     // [degrees]
                 float inclination, // [degrees]
                 float roll,        // [degrees]
                 int extend,         // -1 left, +1 right
                 String comment )
    {
      TDLog.Log( TDLog.LOG_DEBUG,
                 "PT file add shot " + from + " " + to + " " + distance + " " + azimuth + " " + inclination );
      PTShot shot = new PTShot( distance, azimuth, inclination, roll, (extend == -1), trip );
      int id;
      if ( from.length() == 0 || from.equals("-") ) {
        shot.setFromUndefined();
      } else {
        if ( ! StationPolicy.doTopoRobot() || ! shot.setFrom( from ) ) {
          shot.setFrom( getId( from ) );
        }
      }
      if ( to.length() == 0 || to.equals("-") ) {
        shot.setToUndefined();
      } else {
        if ( ! StationPolicy.doTopoRobot() || ! shot.setTo( to ) ) {
          shot.setTo( getId( to ) );
        }
      }
      shot.setComment( comment );
    
      _shots.add( shot );
      return _shots.size();
    }


    /** add a reference
     * @return number of references
     */
    int addReference( String station, float e, float n, float a, String comment )
    {
      int e0 = (int)(e * 1000);
      int n0 = (int)(n * 1000);
      int a0 = (int)(a * 1000);
      PTReference ref = new PTReference( station, e0, n0, a0, comment );
      _references.add( ref );
      return _references.size();
    }

      // void initColors( String color_point[], String color_line[] )
      // {
      //   for (int k=0; k<7; ++k ) {
      //     color_point[k] = _color_default_point[k];
      //     color_line[k] = _color_default_line[k];
      //   }
      // }
}

