/* @file ShpWriter.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief shapefile export writer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.utils.TDLog;
import com.topodroid.DistoX.Vector3D;
import com.topodroid.DistoX.Triangle3D;
import com.topodroid.DistoX.Cave3DStation;
import com.topodroid.DistoX.Cave3DShot;
import com.topodroid.c3walls.cw.CWFacet;
import com.topodroid.c3walls.cw.CWPoint;
   
// import java.io.File;
import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;   

import java.nio.ByteBuffer;   
import java.nio.MappedByteBuffer;   
import java.nio.ByteOrder;   
import java.nio.channels.FileChannel;   

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;

class ShpObject
{
  final static int AREA_SIZE_LENGTH    = 16;
  final static int STATION_NAME_LENGTH = 16;
  final static int SURVEY_NAME_LENGTH  = 64;

  final static int SHP_MAGIC = 9994;
  final static int SHP_VERSION = 1000;
  final static int SHP_POINT     =  1;
  final static int SHP_POLYLINE  =  3;
  final static int SHP_POLYGON   =  5;
  final static int SHP_POINTZ    = 11;
  final static int SHP_POLYLINEZ = 13;
  final static int SHP_POLYGONZ  = 15;

  final static byte BYTE0  = (byte)0;
  final static byte BYTEC  = (byte)'C';
  final static byte BYTEN  = (byte)'N';
  final static short SHORT0 = (short)0;

  int mGeomType; // geom type
  int nr;   // nuber of objects
  String path; // file path 
  String name; // file name 
  int year, month, day;

  double xmin, xmax, ymin, ymax, zmin, zmax; // bounding box
  // double mmin = 0.0, mmax = 0.0;

  FileChannel shpChannel;   
  FileChannel shxChannel;   
  FileChannel dbfChannel;   
  ByteBuffer shpBuffer;   
  ByteBuffer shxBuffer;   
  ByteBuffer dbfBuffer;
  FileOutputStream shpFos;
  FileOutputStream shxFos;
  FileOutputStream dbfFos;

  List<String> mFiles; // list of files to which append my files

  // @param yy year [four digit]
  // @param mm month [1..12]
  // @param dd day [1..31]
  ShpObject( int typ, String pth, String nam, List<String> files ) // throws IOException
  { 
    mGeomType  = typ;
    nr    = 0;
    path  = pth;
    name  = nam;
    mFiles = files;
    setYYMMDD( );
    // TDLog.v("SHP object. path " + path + " name " + name );
  }

  protected void open( ) throws IOException
  {
    try {
      shpFos = new FileOutputStream( path + ".shp" );
      shxFos = new FileOutputStream( path + ".shx" );
      dbfFos = new FileOutputStream( path + ".dbf" );
      shpChannel = shpFos.getChannel();
      shxChannel = shxFos.getChannel();
      dbfChannel = dbfFos.getChannel();
      if ( mFiles != null ) {
        mFiles.add( name + ".shp" );
        mFiles.add( name + ".shx" );
        mFiles.add( name + ".dbf" );
      }
    } catch ( IOException e ) {
      TDLog.Error( "SHP output streams error " + e.getMessage() );
      throw e;
    }
  }

  void setYYMMDD( )
  {
    Calendar calendar = new GregorianCalendar();
    year  = calendar.get( Calendar.YEAR );
    month = calendar.get( Calendar.MONTH );
    day   = calendar.get( Calendar.DAY_OF_MONTH );
  }


  protected void resetChannels( int shplen, int shxlen, int dbflen ) throws IOException
  {
    shpBuffer = ByteBuffer.allocateDirect( shplen + 8 );
    shxBuffer = ByteBuffer.allocateDirect( shxlen + 8 );
    dbfBuffer = ByteBuffer.allocateDirect( dbflen + 8 );
    try { 
      shpChannel.position(0);   
      shxChannel.position(0);   
      dbfChannel.position(0);   
    } catch ( IOException e ) {
      TDLog.Error( "SHP position 0 buffers " + e.getMessage() );
      throw e;
    } 
  }

  // record lengths [16-bit words]
  protected int getShpRecordLength( ) { return 0; }
  protected int getShxRecordLength( ) { return 4; } // shx records are 4 16-bit words

  // make sure buffers size is ok
  private ByteBuffer checkBuffer( ByteBuffer buffer, int size )
  {   
    if (buffer.capacity() < size) {   
      /* if (buffer != null) */ buffer.clear();
      buffer = ByteBuffer.allocateDirect(size);   
    }   
    return buffer;
  }

  // drain buffers into channels
  private void drainBuffers( )  throws IOException
  {
    drain( shpBuffer, shpChannel );   
    drain( shxBuffer, shxChannel );   
    drain( dbfBuffer, dbfChannel );   
  }

  private void drain( ByteBuffer buffer, FileChannel channel )  throws IOException
  {  
    // TDLog.v( "SHP drain buffer pos " + buffer.position() );
    try { 
      buffer.flip();  // set limit to current-pos and pos to 0
      while (buffer.remaining() > 0) channel.write(buffer);   
      buffer.flip().limit(buffer.capacity()); // set limit to capacity and pos to 0
    } catch ( IOException e ) {
      TDLog.Error( "SHP drain buffers " + e.getMessage() );
      throw e;
    }
  }

  protected void close() throws IOException
  {
    // TDLog.v( "TopoGL-SHP", "drain and close" );
    shpBuffer.putInt( 0 );
    shpBuffer.putInt( 0 );
    shxBuffer.putInt( 0 );
    shxBuffer.putInt( 0 );
    writeDBaseEOF();
    drainBuffers();

    try {   
      if (shpChannel != null && shpChannel.isOpen()) shpChannel.close();
      shpFos.close();
    } catch ( IOException e ) {
      TDLog.Error( "SHP close shp buffer " + e.getMessage() );
      throw e;
    }
    try {   
      if (shxChannel != null && shxChannel.isOpen()) shxChannel.close();   
      shxFos.close();
    } catch ( IOException e ) {
      TDLog.Error( "SHP close shx buffer " + e.getMessage() );
      throw e;
    }
    try {   
      if (dbfChannel != null && dbfChannel.isOpen()) dbfChannel.close();   
      dbfFos.close();
    } catch ( IOException e ) {
      TDLog.Error( "SHP close dbf buffer " + e.getMessage() );
      throw e;
    }
    shpChannel = null;   
    shxChannel = null;   
    dbfChannel = null;   
    if (shpBuffer instanceof MappedByteBuffer) shpBuffer.clear();   
    if (shxBuffer instanceof MappedByteBuffer) shxBuffer.clear();   
    if (dbfBuffer instanceof MappedByteBuffer) dbfBuffer.clear();   
    shpBuffer = null;   
    shxBuffer = null;   
    dbfBuffer = null;   
    // TDLog.v( "SHP drain and close DONE" );
  }

  // Write the headers for this shapefile including the bounds, shape type,
  // the number of geometries and the total fileLength (in actual bytes, NOT 
  // 16 bit words)
  // @param buffer   byte buffer [in/ret]
  // @param channel  file channel
  // @param geom_type     geom type
  // @param length   file length [16-bit words]
  //
  // @note bounding box must have been computed
  ByteBuffer writeShapeHeader( ByteBuffer buffer, int geom_type, int length ) 
  { 
    // must allocate enough for shape + header (2 ints)   
    buffer = checkBuffer( buffer, 2*length + 8);   
    buffer.order( ByteOrder.BIG_ENDIAN );
    buffer.putInt( SHP_MAGIC );
    for (int i = 0; i < 5; i++) {
      buffer.putInt(0); // Skip unused part of header
    }
    buffer.putInt(length);

    buffer.order( ByteOrder.LITTLE_ENDIAN );
    buffer.putInt( SHP_VERSION );
    buffer.putInt( geom_type );

    // write the bounding box
    buffer.putDouble(xmin);
    buffer.putDouble(ymin);
    buffer.putDouble(xmax);
    buffer.putDouble(ymax);
    buffer.putDouble(zmin);
    buffer.putDouble(zmax);
    buffer.putDouble(0.0);
    buffer.putDouble(0.0);
    return buffer;
  }

  // @param buffer
  // @param n_rec  number of records
  // @param n_fld  number of fields
  // @param flds   array of fields names
  // @return buffer
  //
  // @note write 33 + 32*n_fld bytes (less than 100 for 2 fields)
  //
  // dBase II file is much simple:
  //   0  version 0x02
  // 1-2  nr of records
  // 3-5  yy mm dd
  // 6-7  length of each record
  // 8-x  field descriptor (max 32, 16 bytes each):
  //      0-10 ascii name
  //        11 type (ascii: C, N, or L)
  //        12 field length = 1 + sum of all fields (max 1000)
  //     13-14 address in memory
  //        15 decimal count
  // x+1 terminator: 0x0d all 32 fields present, 0x00 otherwise
  // ... records
  //     eof: 0x1a
  void writeDBaseHeader( int n_rec, int lenRecord, int n_fld, String[] flds, byte[] types, int[] lens )
  {
    int fldLength = 32; // field descriptor length [bytes]
    int lenHeader = 32 + fldLength * n_fld + 1;
    // int lenRecord = 1 + 16 * n_fld; // 16 bytes per field in each record
    dbfBuffer = checkBuffer( dbfBuffer, lenHeader + 8);   
    dbfBuffer.order(ByteOrder.LITTLE_ENDIAN);
    dbfBuffer.put( (byte)0x03 ); // filetype:
                              // 0x02 FixBase
			      // 0x03 File without DBT (memo file)
			      // 0x04 dBase IV w/o memo
			      // 0x05 dBase V  w/o memo
			      // 0x07 Visual Objects for dBase III w/o memo
			      // 0x30 Visual FoxPro (with DBC [Data Container]
			      // 0x31 Visual FoxPro with autoincrement
			      // 0x43
			      // 0x7b dBase iV with memo
			      // 0x83 File with DBT, dBase III+ with memo
			      // 0x87 Visual Objects for dBase III with memo
			      // 0x8b dBase IV with memo
			      // 0x8e dBasde IV with SQL table
			      // 0xb3 .dvb and .dbt memo
			      // 0xe5
			      // 0xf5 FoxPro with memo
			      // 0xfb FoxPro ???
      // dBase flag bits 7___ 6___5___4 3___ 2__1__0
      //                 DBT  SQL_table memo version
    dbfBuffer.put( (byte)(year-1900) );
    dbfBuffer.put( (byte)month );
    dbfBuffer.put( (byte)day );
    dbfBuffer.putInt( n_rec );              // 4-7 number of record
    dbfBuffer.putShort( (short)lenHeader ); // 8-9 pos first record
    dbfBuffer.putShort( (short)lenRecord ); // 10-11 length of each record
    dbfBuffer.putShort( SHORT0 ); // 12-13 reserved
    dbfBuffer.put( BYTE0 );      // 14 transaction: 0 ended, 1 started
    dbfBuffer.put( BYTE0 );      // 15 dBase IV encryption: 0 no, 1 yes
    for (int k=0; k<12; ++k ) dbfBuffer.put( BYTE0 ); // 16-27 reserved

    dbfBuffer.put( BYTE0 );         // 28 table flag: no MDX file 
    dbfBuffer.put( (byte)0x03 ); // 29 language driver ID - codepage mark 1252 Window ANSI
    dbfBuffer.putShort( SHORT0 );    // 30-31 reserved

    // for (int k=0; k<32; ++k ) dbfBuffer.put( 0 ); // language driver name
    // for (int k=0; k<4; ++k ) dbfBuffer.put( 0 ); // reserved

    // write field descriptors (32 bytes each) max 128 fields
    int off = 1;
    for ( int n=0; n<n_fld; ++n ) {
      String fld = flds[n];
      int k = 0;
      int k0 = fld.length(); if ( k0 > 10 ) k0 = 10;
      for ( ; k<k0; ++k ) dbfBuffer.put( (byte)fld.charAt(k) ); // field name
      for ( ; k<11; ++k ) dbfBuffer.put( BYTE0 );

      dbfBuffer.put( types[n] ); // field type
      dbfBuffer.putInt( off );  // displacement of field in record
                               // 12-15 dBase address of field in memory
			       // 12-13 FoxPro offset of field from beginning of record
			       // others: irrelevant
      off += lens[n];
      dbfBuffer.put( (byte)(lens[n]) ); // field length in bytes 
      dbfBuffer.put( BYTE0 );           // field decimal places 
      dbfBuffer.putShort( SHORT0 );     // reserved (column: 1 system, 2 can be null, 4 binary)
      dbfBuffer.put( (byte)0x01 );      // work area (0x01 in dBase)
      dbfBuffer.putShort( SHORT0 );     // reserved
      dbfBuffer.put( BYTE0 );           // set fields
      for (k=0; k<7; ++k ) dbfBuffer.put( BYTE0 ); // reserved
      dbfBuffer.put( BYTE0 );           // key: 0 no, 1 yes
    }
    dbfBuffer.put( (byte)0x0d ); // (32+32*n_fld) header ternimator
    // no FoxPro data container [264 bytes]

    // // field property struct
    // dbfBuffer.putShort( 0 ); // nr standard properties
    // dbfBuffer.putShort( 0 );
    // dbfBuffer.putShort( 0 ); // nr custom properties
    // dbfBuffer.putShort( 0 );
    // dbfBuffer.putShort( 0 ); // nr referential integrity properties
    // dbfBuffer.putShort( (short)16 ); // start of descriptor property array
    // dbfBuffer.putShort( (short)16 ); // size of struct
    // // no std property descriptor (15 bytes each)
    // // no custom property descriptor (14 bytes each)
    // // no referential integrity properties (22 bytes each)
  }

  // dbf record length = 1 + 16*n_fld
  // @param n_fld   number of fields
  // @param vals    field values
  // @param lens    field lengths
  void writeDBaseRecord( int n_fld, String[] vals, int[] lens )
  {
    dbfBuffer.put( (byte)0x20 ); // 0x20 ok, 0x2a deleted
    for ( int n=0; n<n_fld; ++n ) {
      int len = lens[n];
      String val = vals[n];
      int k = 0;
      int k0 = val.length(); if ( k0 >= len ) k0 = len-1;
      for ( ; k<k0; ++k ) dbfBuffer.put( (byte)val.charAt(k) );
      for ( ; k<len; ++k ) dbfBuffer.put( BYTE0 );
    }
  }

  private void writeDBaseEOF( ) // throws IOException
  {
    dbfBuffer.put( (byte)0x1a );
    // drain( dbfBuffer, dbfChannel );
  }

  // @param cnt    record number (begins at 1)
  // @param length [16-bit words]
  void writeShpRecordHeader( int cnt, int length )
  {
    shpBuffer.order( ByteOrder.BIG_ENDIAN );   
    shpBuffer.putInt(cnt);   
    shpBuffer.putInt(length);
  }

  void writeShxRecord( int off, int length )
  {
    shxBuffer.order( ByteOrder.BIG_ENDIAN );   
    shxBuffer.putInt(off);   
    shxBuffer.putInt(length);
  }


  // protected void initBBox( double x, double y, double z )
  // {
  //   xmin = xmax = x;
  //   ymin = ymax = y;
  //   zmin = zmax = z;
  // }
  protected void initBBox( Vector3D v )
  {
    xmin = xmax = v.x;
    ymin = ymax = v.y;
    zmin = zmax = v.z;
  }

  // protected void initBBox( double x, double y ) { initBBox( x, y, 0 ); }

  // protected void updateBBox( double x, double y, double z )
  // {
  //   if ( x < xmin ) { xmin = x; } else if ( x > xmax ) { xmax = x; }
  //   if ( y < ymin ) { ymin = y; } else if ( y > ymax ) { ymax = y; }
  //   if ( z < zmin ) { zmin = z; } else if ( z > zmax ) { zmax = z; }
  // }

  protected void updateBBox( Vector3D v )
  {
    if ( v == null ) return;
    if ( v.x < xmin ) { xmin = v.x; } else if ( v.x > xmax ) { xmax = v.x; }
    if ( v.y < ymin ) { ymin = v.y; } else if ( v.y > ymax ) { ymax = v.y; }
    if ( v.z < zmin ) { zmin = v.z; } else if ( v.z > zmax ) { zmax = v.z; }
  }

  // protected void updateBBox( double x, double y )
  // {
  //   if ( x < xmin ) { xmin = x; } else if ( x > xmax ) { xmax = x; }
  //   if ( y < ymin ) { ymin = y; } else if ( y > ymax ) { ymax = y; }
  // }

  // protected void updateBBoxScene( double x, double y )
  // {
  //   updateBBox( xWorld( x ), yWorld(y) );
  // }

}
// ---------------------------------------------------------------------------------------------
// 3D classes

class ShpPointz extends ShpObject
{
  private static int mShpType = SHP_POINTZ;

  ShpPointz( String path, String name, List<String> files ) // throws IOException
  {
    super( mShpType, path, name, files );
  }

  // write headers for POINTZ
  boolean writeStations( List< Cave3DStation > pts ) throws IOException
  {
    int n_pts = (pts != null)? pts.size() : 0;
    if ( n_pts == 0 ) return false;

    int n_fld = 2;
    String[] fields = new String[ n_fld ];
    fields[0] = "name";
    fields[1] = "survey";
    byte[]   ftypes = { BYTEC, BYTEC };
    int[]    flens  = { STATION_NAME_LENGTH, SURVEY_NAME_LENGTH };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + n_pts * shpRecLen; // [16-bit words]
    int shxLength = 50 + n_pts * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + n_pts * dbfRecLen; // [Bytes]

    setBoundsStations( pts );
    // TDLog.v( "SHP Pts X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
    // TDLog.v( "SHP POINTZ " + pts.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // TDLog.v( "SHP bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength+8, 2*shxLength+8, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, mShpType, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, mShpType, shxLength );
    writeDBaseHeader( n_pts, dbfRecLen, n_fld, fields, ftypes, flens );
    // TDLog.v( "SHP POINTZ done headers");

    int cnt = 0;
    for ( Cave3DStation pt : pts ) {
      int offset = 50 + cnt * shpRecLen; 
      writeShpRecordHeader( cnt, shpRecLen );
      shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
      shpBuffer.putInt( mShpType );
      // TDLog.v( "SHP POINTZ " + cnt + ": " + pt.e + " " + pt.s + " " + pt.v + " offset " + offset );
      shpBuffer.putDouble( pt.x );
      shpBuffer.putDouble( pt.y );
      shpBuffer.putDouble( pt.z );
      shpBuffer.putDouble( 0.0 );

      writeShxRecord( offset, shpRecLen );
      fields[0] = pt.getFullName();
      fields[1] = pt.getSurvey();
      writeDBaseRecord( n_fld, fields, flens );
      ++cnt;
    }
    // TDLog.v( "SHP POINTZ done records");
    close();
    return true;
  }

  // record length [word]: 4 + 36/2
  @Override protected int getShpRecordLength( ) { return 22; }
    
  // Utility: set the bounding box of the set of geometries
  private void setBoundsStations( List< Cave3DStation > pts ) 
  {
    if ( pts.size() == 0 ) {
      xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
      return;
    }
    initBBox( pts.get(0) );
    for ( int k=pts.size() - 1; k>0; --k ) {
      updateBBox( pts.get(k) );
    }
  }
}

class ShpPolylinez extends ShpObject
{
  static final int mShpType = SHP_POLYLINEZ;

  ShpPolylinez( String path, String name, List<String> files ) // throws IOException
  {
    super( mShpType, path, name, files );
  }

  boolean writeShots( List< Cave3DShot > lns0, String name ) throws IOException
  {
    if ( lns0 == null ) return false;

    // guarantee shots have FROM and TO stations
    ArrayList< Cave3DShot > lns = new ArrayList<>();
    for ( Cave3DShot ln : lns0 ) { 
      if ( ln.from_station != null && ln.to_station != null ) lns.add( ln );
    }

    int nr = lns.size();
    if ( nr == 0 ) return false;

    int n_fld = 4; // type from to // flag comment
    String[] fields = new String[ n_fld ];
    fields[0] = "type";
    fields[1] = "from";
    fields[2] = "to";
    fields[3] = "survey"; // survey name of FROM
    // fields[3] = "flag";
    // fields[4] = "comment";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC, BYTEC }; // , BYTEC, BYTEC }; // use only strings
    int[]    flens  = { 8, STATION_NAME_LENGTH, STATION_NAME_LENGTH, SURVEY_NAME_LENGTH }; // , 8, 32 };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + nr * shpRecLen; // [16-bit words]
    int shxLength = 50 + nr * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + nr * dbfRecLen; // Bytes, 3 fields

    setBoundsShots( lns );
    // TDLog.v( "SHP Lines X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
    // TDLog.v( "SHP POLYLINEZ shots " + lns.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // TDLog.v( "SHP bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength, 2*shxLength, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, mShpType, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, mShpType, shxLength );
    writeDBaseHeader( nr, dbfRecLen, n_fld, fields, ftypes, flens );
    // TDLog.v( "SHP shots done headers" );

    int cnt = 0;
    for ( Cave3DShot ln : lns ) {
      Cave3DStation p1 = ln.from_station;
      Cave3DStation p2 = ln.to_station;
      int offset = 50 + cnt * shpRecLen; 
      ++cnt;

      writeShpRecord( cnt, shpRecLen, p1, p2 );
      writeShxRecord( offset, shpRecLen );
      fields[0] = name;
      fields[1] = p1.getShortName(); 
      fields[2] = p2.getShortName();
      fields[3] = p1.getSurvey();
      // TDLog.v( "SHP shots fields " + fields[1] + " " + fields[2] + " " + fields[3] );
      // fields[3] = String.format("0x%02x", ln.getReducedFlag() ); // flag
      // fields[4] = ln.getComment();
      writeDBaseRecord( n_fld, fields, flens );
    }
    
    // TDLog.v( "SHP shots done records" );
    close();
    return true;
  }

  private void writeShpRecord( int cnt, int len, Cave3DStation p1, Cave3DStation p2 )
  {
    writeShpRecordHeader( cnt, len );
    shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
    shpBuffer.putInt( mShpType );
    double x1 = p1.x; double x2 = p2.x; if ( x1 > x2 ) { x1=p2.x; x2=p1.x; }
    double y1 = p1.y; double y2 = p2.y; if ( y1 > y2 ) { y1=p2.y; y2=p1.y; }
    double z1 = p1.z; double z2 = p2.z; if ( z1 > z2 ) { z1=p2.z; z2=p1.z; }
    shpBuffer.putDouble( x1 );
    shpBuffer.putDouble( y1 );
    shpBuffer.putDouble( x2 );
    shpBuffer.putDouble( y2 );
    shpBuffer.putInt( 1 ); // one part: number of parts
    shpBuffer.putInt( 2 ); // two points: total number of points
    shpBuffer.putInt( 0 ); // part 0 starts with point 0 (and ends with point 1)
    shpBuffer.putDouble( p1.x );
    shpBuffer.putDouble( p1.y );
    shpBuffer.putDouble( p2.x );
    shpBuffer.putDouble( p2.y );
    shpBuffer.putDouble( z1 );
    shpBuffer.putDouble( z2 );
    shpBuffer.putDouble( p1.z );
    shpBuffer.putDouble( p2.z );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
  }

  @Override protected int getShpRecordLength( ) { return 76; } // POLYLINEZ 

  private void setBoundsShots( List< Cave3DShot > lns )
  {
    int nrs = ( lns != null )? lns.size() : 0;
    // if ( lns.size() == 0 && lms.size() ) { // guaramteed one is non-zero
    //   xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
    //   return;
    // }
    int k = 0;
    if ( nrs > 0 ) {
      // init BBox - break on first line with a endpoint
      for ( k=0; k<nrs; ++k ) {
        Cave3DShot ln = lns.get(k);
        Cave3DStation pt = ln.from_station;
        if ( pt != null ) {
          initBBox( pt );
          updateBBox( ln.to_station );
          break;
        } 
        pt = ln.to_station;
        if ( pt != null ) {
          initBBox( pt );
          break;
        }
      }
    }
    if ( k >= nrs ) return; // not really necessary
    
    // update BBox with other lines
    for ( ++k; k<nrs; ++k ) {
      Cave3DShot ln = lns.get(k);
      updateBBox( ln.from_station );
      updateBBox( ln.to_station );
    }
  }

  // private void setBoundsSplays( List<NumSplay> lns )
  // {
  //   if ( lns.size() == 0 ) {
  //     xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
  //     return;
  //   }
  //   NumSplay ln = lns.get(0);
  //   NumStation pt = ln.from;
  //   initBBox( pt.e, pt.s, pt.v );
  //   updateBBox( ln.e, ln.s, ln.v );
  //   for ( int k=lns.size() - 1; k>0; --k ) {
  //     ln = lns.get(k);
  //     pt = ln.from;
  //     updateBBox( pt.e, pt.s, pt.v );
  //     updateBBox( ln.e, ln.s, ln.v );
  //   }
  // }
}

class ShpPolygonz extends ShpObject
{
  static final int mShpType = SHP_POLYGONZ; // SHP_POLYLINEZ

  ShpPolygonz( String path, String name, List<String> files ) // throws IOException
  {
    super( mShpType, path, name, files );
  }

  @Override protected int getShpRecordLength( ) { return 108; } // POLIGONZ 76 + 4*8/2 + 4*8/2

  private double area( CWPoint p1, CWPoint p2, CWPoint p3 ) 
  {
    double x2 = p2.x - p1.x; // V1
    double y2 = p2.y - p1.y;
    double z2 = p2.z - p1.z;
    double x3 = p3.x - p1.x; // V2
    double y3 = p3.y - p1.y;
    double z3 = p3.z - p1.z;
    double nx = y2 * z3 - z2 * y3; // N = V1 ^ V2
    double ny = z2 * x3 - x2 * z3;
    double nz = x2 * y3 - y2 * x3;
    double nn = Math.sqrt( nx*nx + ny*ny + nz*nz );
    double vx = ny * z2 - nz * y2; // N ^ V1
    double vy = nz * x2 - nx * z2;
    double vz = nx * y2 - ny * x2;
    double area = ( vx * x3 + vy * y3 + vz * z3 ) / nn; // N ^ V1 * V2
    return area / 2;
  }

  boolean writeFacets( List< CWFacet > lns0 ) throws IOException
  {
    if ( lns0 == null ) return false;

    // guarantee facets have three points
    ArrayList< CWFacet > lns = new ArrayList<>();
    for ( CWFacet ln : lns0 ) {
      if ( ln.v1 != null && ln.v2 != null && ln.v3 != null ) lns.add( ln );
    }

    int nr = lns.size();
    if ( nr == 0 ) return false;

    int n_fld = 3; // type from to // flag comment
    String[] fields = new String[ n_fld ];
    fields[0] = "type";
    fields[1] = "area";
    fields[2] = "Z";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC };
    int[]    flens  = { 8, AREA_SIZE_LENGTH, 8 };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + nr * shpRecLen; // [16-bit words]
    int shxLength = 50 + nr * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + nr * dbfRecLen; // Bytes, 3 fields

    setBoundsFacets( lns );
    // TDLog.v( "SHP Facets X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
    // TDLog.v( "SHP POLYGONZ facets " + lns.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // TDLog.v( "SHP bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength, 2*shxLength, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, mShpType, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, mShpType, shxLength );
    writeDBaseHeader( nr, dbfRecLen, n_fld, fields, ftypes, flens );
    // TDLog.v( "SHP facets done headers" );

    int cnt = 0;
    for ( CWFacet ln : lns ) {
      CWPoint p1 = ln.v1;
      CWPoint p2 = ln.v2;
      CWPoint p3 = ln.v3;

      String zz = "+1";
      double a = area( p1, p2, p3 );
      if ( a > 0 ) { CWPoint p = p2; p2 = p3; p3 = p; } // points must be counterclockwise
      else         { a = -a; zz = "-1"; }

      int offset = 50 + cnt * shpRecLen; 
      ++cnt;

      // TDLog.v( "SHP Face " + p1.x + " " + p2.x + " " + p3.x );

      writeShpRecord( cnt, shpRecLen, p1, p2, p3 );
      writeShxRecord( offset, shpRecLen );
      fields[0] = "facet";
      fields[1] = String.format(Locale.US, "%.2f", a );
      fields[0] = zz;
      writeDBaseRecord( n_fld, fields, flens );
    }
    // TDLog.v( "SHP shots done " + cnt + " records" );
    close();
    return true;
  }

  private void writeShpRecord( int cnt, int len, CWPoint p1, CWPoint p2, CWPoint p3 )
  {
    writeShpRecordHeader( cnt, len );
    shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
    shpBuffer.putInt( mShpType );
    double x1 = p1.x; double x2 = p2.x; if ( x1 > x2 ) { x1=p2.x; x2=p1.x; } if ( x1 > p3.x ) { x1 = p3.x; } else if ( x2 < p3.x ) { x2 = p3.x; }
    double y1 = p1.y; double y2 = p2.y; if ( y1 > y2 ) { y1=p2.y; y2=p1.y; } if ( y1 > p3.y ) { y1 = p3.y; } else if ( y2 < p3.y ) { y2 = p3.y; }
    double z1 = p1.z; double z2 = p2.z; if ( z1 > z2 ) { z1=p2.z; z2=p1.z; } if ( z1 > p3.z ) { z1 = p3.z; } else if ( z2 < p3.z ) { z2 = p3.z; }
    shpBuffer.putDouble( x1 );
    shpBuffer.putDouble( y1 );
    shpBuffer.putDouble( x2 );
    shpBuffer.putDouble( y2 );
    shpBuffer.putInt( 1 ); // one part: number of parts
    shpBuffer.putInt( 4 ); // four points: total number of points: last must coincide with first
    shpBuffer.putInt( 0 ); // part 0 starts with point 0 (and ends with point 2)
    shpBuffer.putDouble( p1.x );
    shpBuffer.putDouble( p1.y );
    shpBuffer.putDouble( p2.x );
    shpBuffer.putDouble( p2.y );
    shpBuffer.putDouble( p3.x );
    shpBuffer.putDouble( p3.y );
    shpBuffer.putDouble( p1.x );
    shpBuffer.putDouble( p1.y );
    shpBuffer.putDouble( z1 );
    shpBuffer.putDouble( z2 );
    shpBuffer.putDouble( p1.z );
    shpBuffer.putDouble( p2.z );
    shpBuffer.putDouble( p3.z );
    shpBuffer.putDouble( p1.z );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
  }

  private void setBoundsFacets( List< CWFacet > lns )
  {
    int nrs = ( lns != null )? lns.size() : 0;
    if ( nrs > 0 ) {
      CWFacet ln = lns.get(0);
      initBBox( ln.v1 );
      updateBBox( ln.v2 );
      updateBBox( ln.v3 );
      for ( int k=1; k<nrs; ++k ) {
        ln = lns.get(k);
        updateBBox( ln.v1 );
        updateBBox( ln.v2 );
        updateBBox( ln.v3 );
      }
    }
  }

  private double area( Vector3D p1, Vector3D p2, Vector3D p3 ) 
  {
    double x2 = p2.x - p1.x; // V1
    double y2 = p2.y - p1.y;
    double z2 = p2.z - p1.z;
    double x3 = p3.x - p1.x; // V2
    double y3 = p3.y - p1.y;
    double z3 = p3.z - p1.z;
    double nx = y2 * z3 - z2 * y3; // N = V1 ^ V2
    double ny = z2 * x3 - x2 * z3;
    double nz = x2 * y3 - y2 * x3;
    double nn = Math.sqrt( nx*nx + ny*ny + nz*nz );
    double vx = ny * z2 - nz * y2; // N ^ V1
    double vy = nz * x2 - nx * z2;
    double vz = nx * y2 - ny * x2;
    double area = ( vx * x3 + vy * y3 + vz * z3 ) / nn; // N ^ V1 * V2
    return area / 2;
  }

  boolean writeTriangles( List< Triangle3D > lns0 ) throws IOException
  {
    if ( lns0 == null ) return false;

    // guarantee triangle have three vertices
    ArrayList< Triangle3D > lns = new ArrayList<>();
    for ( Triangle3D ln : lns0 ) {
      if ( ln.vertex[0] != null && ln.vertex[1] != null && ln.vertex[2] != null ) lns.add( ln );
    }

    int nr = lns.size();
    if ( nr == 0 ) return false;

    int n_fld = 3; // type from to // flag comment
    String[] fields = new String[ n_fld ];
    fields[0] = "type";
    fields[1] = "area";
    fields[2] = "Z";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC };
    int[]    flens  = { 8, AREA_SIZE_LENGTH, 8 };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + nr * shpRecLen; // [16-bit words]
    int shxLength = 50 + nr * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + nr * dbfRecLen; // Bytes, 3 fields

    setBoundsTriangles( lns );
    // TDLog.v( "SHP Tris X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );
    // TDLog.v( "SHP POLYLINEZ shots " + lns.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // TDLog.v( "SHP bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength, 2*shxLength, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, mShpType, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, mShpType, shxLength );
    writeDBaseHeader( nr, dbfRecLen, n_fld, fields, ftypes, flens );
    // TDLog.v( "SHP shots done headers" );

    int cnt = 0;
    for ( Triangle3D ln : lns ) {
      Vector3D p1 = ln.vertex[0];
      Vector3D p2 = ln.vertex[1];
      Vector3D p3 = ln.vertex[2];
      int offset = 50 + cnt * shpRecLen; 
      ++cnt;

      String zz = "+1";
      double a = area( p1, p2, p3 );
      if ( a > 0 ) { Vector3D p = p2; p2 = p3; p3 = p; }
      else         { a = -a; zz = "-1"; }

      writeShpRecord( cnt, shpRecLen, p1, p2, p3 );
      writeShxRecord( offset, shpRecLen );
      fields[0] = "tri";
      fields[1] = String.format( Locale.US, "%.2f", a );
      fields[2] = zz;
      writeDBaseRecord( n_fld, fields, flens );
    }
    // TDLog.v( "SHP shots done records" );
    close();
    return true;
  }

  private void writeShpRecord( int cnt, int len, Vector3D p1, Vector3D p2, Vector3D p3 )
  {
    writeShpRecordHeader( cnt, len );
    shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
    shpBuffer.putInt( mShpType );
    double x1 = p1.x; double x2 = p2.x; if ( x1 > x2 ) { x1=p2.x; x2=p1.x; } if ( x1 > p3.x ) { x1 = p3.x; } else if ( x2 < p3.x ) { x2 = p3.x; }
    double y1 = p1.y; double y2 = p2.y; if ( y1 > y2 ) { y1=p2.y; y2=p1.y; } if ( y1 > p3.y ) { y1 = p3.y; } else if ( y2 < p3.y ) { y2 = p3.y; }
    double z1 = p1.z; double z2 = p2.z; if ( z1 > z2 ) { z1=p2.z; z2=p1.z; } if ( z1 > p3.z ) { z1 = p3.z; } else if ( z2 < p3.z ) { z2 = p3.z; }
    shpBuffer.putDouble( x1 );
    shpBuffer.putDouble( y1 );
    shpBuffer.putDouble( x2 );
    shpBuffer.putDouble( y2 );
    shpBuffer.putInt( 1 ); // one part: number of parts
    shpBuffer.putInt( 4 ); // four points: total number of points - last = first
    shpBuffer.putInt( 0 ); // part 0 starts with point 0 (and ends with point 1)
    shpBuffer.putDouble( p1.x );
    shpBuffer.putDouble( p1.y );
    shpBuffer.putDouble( p2.x );
    shpBuffer.putDouble( p2.y );
    shpBuffer.putDouble( p3.x );
    shpBuffer.putDouble( p3.y );
    shpBuffer.putDouble( p1.x );
    shpBuffer.putDouble( p1.y );
    shpBuffer.putDouble( z1 );
    shpBuffer.putDouble( z2 );
    shpBuffer.putDouble( p1.z );
    shpBuffer.putDouble( p2.z );
    shpBuffer.putDouble( p3.z );
    shpBuffer.putDouble( p1.z );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
  }

  private void setBoundsTriangles( List< Triangle3D > lns )
  {
    int nrs = ( lns != null )? lns.size() : 0;
    if ( nrs > 0 ) {
      Triangle3D ln = lns.get(0);
      initBBox( ln.vertex[0] );
      updateBBox( ln.vertex[1] );
      updateBBox( ln.vertex[2] );
      for ( int k=1; k<nrs; ++k ) {
        ln = lns.get(k);
        updateBBox( ln.vertex[0] );
        updateBBox( ln.vertex[1] );
        updateBBox( ln.vertex[2] );
      }
    }
  }

}
