/* @file PTShot.java
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


public class PTShot
{
    private PTId _from;
    private PTId _to;
    private int _dist;    //!< distance [mm]
    private short _azimuth; //!< full circle 2^16
    private short _inclination;
    private byte _flags;   //!< bit0: flipped shot // byte
    private byte _roll;    //!< roll angle [full circle 256] // byte
    private short _trip_index;    //!< -1: no trip; >=0 trip reference
    private PTString _comment;      //!< only if _flags & 0x2

    PTShot()
    {
      _from = new PTId();
      _to   = new PTId();

      _dist = 0;
      _azimuth     = (short)0;
      _inclination = (short)0;
      _flags = (byte)0;
      _roll  = (byte)0;
      _trip_index = (short)-1;

      _comment = new PTString();
    }

    PTShot( float d, float a, float i, float r, boolean flipped, short trip )
    {
      _from = new PTId();
      _to   = new PTId();

      _dist        = (int)( d * 1000 );
      _azimuth     = PTFile.DEG_2_ANGLE( a );
      _inclination = PTFile.DEG_2_CLINO( i );
      _roll        = PTFile.DEG_2_ROLL( r );
      _flags = 0;
      if ( flipped ) _flags |= (byte)0x01;
      _trip_index = trip;

      _comment = new PTString();
    }


    // ------------------------------------------------------------
    public PTId from() { return _from; }
    public PTId to()   { return _to; }

    // void setFrom( String from ) { _from.set( from ); }
    // void setTo( String to )     { _to.set( to ); }
    void setFromUndefined() { _from.setUndef(); }
    void setToUndefined()   { _to.setUndef(); }
    void setFrom( int from_id ) { _from.setId( from_id ); }
    void setTo( int to_id )     { _to.setId( to_id ); }
    boolean setFrom( String from ) { return _from.set( from ); }
    boolean setTo( String to )     { return _to.set( to ); }

    /** get the distance in m */
    public float distance() { return (float)(_dist) / 1000.0f; }

    /** get the azimuth in degrees */
    public float azimuth() { return (float)(_azimuth) * PTFile.INT16_2_DEG; }

    /** get the inclination in degrees */
    public float inclination() { return PTFile.CLINO_2_DEG( _inclination ); }

    /** get the roll in degrees */
    public float roll() { return (float)(_roll) * PTFile.INT8_2_DEG; }

    /** set the distance
     * @param d   distance [m]
     */
    void setDistance( float d )
    {
      // assert( d >= 0.0 );
      _dist = (int)( d * 1000.0f );
    }

    /** set the azimuth
     * @param a   azimuth [degrees]
     */
    void setAzimuth( float a ) 
    { 
      // assert( a >= 0.0 && a < 360.0f );
      _azimuth = PTFile.DEG_2_ANGLE( a );
    }

    /** set the inclination
     * @param a   inclination [degrees]
     */
    void setInclination( float a )
    { 
      // assert( a >= -90.0f && a <= 90.0f );
      _inclination = PTFile.DEG_2_CLINO( a );
    }

    /** set the roll
     * @param a   roll [degrees]
     */
    void setRoll( float a ) 
    { 
      // assert( a >= 0.0f && a < 360.0f );
      _roll = PTFile.DEG_2_ROLL( a );
    }


    /** get the trip index */
    public short tripIndex() { return _trip_index; }

    void setTripIndex( short ti ) 
    { 
      if ( ti < 0 ) {
        _trip_index = (short)-1;
      } else {
        _trip_index = ti;
      }
    }
    
    void setFlipped() { _flags |= (byte)0x01; }
    void clearFlipped() { _flags &= (byte)0xfe; }
    public boolean isFlipped() { return (_flags & (byte)0x01) != (byte)0; }

    public boolean hasComment() { return (_flags & (byte)0x02) != (byte)0; }

    void setComment( String str ) 
    {
      int len = (str != null) ? str.length() : 0;
      if ( len > 0 ) {
        _flags |= (byte)0x02;
      } else {
        _flags &= (byte)0xfd;
      }
      _comment.set( str );
    }

    public String comment() 
    {
      if ( hasComment() ) return _comment.value();
      return null;
    }

    // ------------------------------------------------------------

    void read( InputStream fs )
    {
      _from.read( fs );
      _to.read( fs );
      _dist = PTFile.readInt( fs );
      _azimuth     = PTFile.readShort( fs );
      _inclination = PTFile.readShort( fs );
      _flags = PTFile.readByte( fs );
      _roll  = PTFile.readByte( fs );
      _trip_index = PTFile.readShort( fs );
      if ( (_flags & (byte)0x02) != (byte)0 ) {
        _comment.read( fs );
      }
    }

    void write( OutputStream fs )
    {
      _from.write( fs );
      _to.write( fs );
      PTFile.writeInt( fs, _dist );
      PTFile.writeShort( fs, _azimuth );
      PTFile.writeShort( fs, _inclination );
      PTFile.writeByte( fs, _flags );
      PTFile.writeByte( fs, _roll );
      PTFile.writeShort( fs, _trip_index );
      if ( (_flags & (byte)0x02) != 0 ) {
        _comment.write( fs );
      }
    }

    // void print()
    // {
    //   float azimuth = (_azimuth * 360.0f) / (1<<16);
    //   float inclination = (_inclination * 360.0f) / (1<<16);
    //   float roll = (((int)_roll) * 360.0f) / (1<<8);

    //   if ( inclination > 180.0f ) inclination -= 360.0f;
    //   TDLog.v( "shot: D " + _dist + " A " + azimuth + " I " + inclination + " R " + roll + " trip " + _trip_index + " flags " + _flags );
    //   _from.print();
    //   _to.print();
    //   if ( (_flags & (byte)0x02) != 0 ) _comment.print();
    // }

/*
void 
PTshot::printTherion( FILE * fp, int & extend )
{
  std::string from = _from.toString();
  std::string to   = _to.toString();
  double distance  = (double)_dist / 1000.0; // [m]
  double azimuth = ((double)_azimuth) / (1<<16) * 360.0;
  double inclination = ((double)_inclination) / (1<<16) * 360.0;
  if ( inclination > 180.0 ) inclination -= 360.0;
  if ( isFlipped() && extend == 1 ) {
    extend = -1;
    fprintf(fp, "    extend left\n");
  } else if ( ! isFlipped() && extend == -1 ) {
    extend = 1;
    fprintf(fp, "    extend right\n");
  }
  fprintf(fp, "    %8s %8s %8.2f %8.2f %8.2f \n",
    from.c_str(), to.c_str(), distance, azimuth, inclination);
  if ( hasComment() ) {
    fprintf(fp, "    # %s \n", _comment.value() );
  }
}
*/

}


