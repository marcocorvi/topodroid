/* @file ShpObject.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile oject
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.shp;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.num.NumStation;
import com.topodroid.num.NumShot;
import com.topodroid.num.NumSplay;
import com.topodroid.DistoX.DrawingUtil;
import com.topodroid.DistoX.TDUtil;

// import java.io.File;
import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;   

import java.nio.ByteBuffer;   
import java.nio.MappedByteBuffer;   
import java.nio.ByteOrder;   
import java.nio.channels.FileChannel;   

import java.util.List;

public class ShpObject
{
  final static int SHP_MAGIC = 9994;
  final static int SHP_VERSION = 1000;
  final static int SHP_POINT     =  1;
  final static int SHP_POLYLINE  =  3;
  final static int SHP_POLYGON   =  5;
  final static int SHP_EXTRA     =  7;
  final static int SHP_POINTZ    = 11;
  final static int SHP_POLYLINEZ = 13;

  final static byte BYTE0  = (byte)0;
  final static byte BYTE1  = (byte)1;
  final static byte BYTEC  = (byte)'C';
  final static byte BYTEN  = (byte)'N';
  final static short SHORT0 = (short)0;

  final static int SIZE_TYPE   =   8;
  final static int SIZE_FLAG   =   8;
  final static int SIZE_NAME   =  16; // name, from, to
  final static int SIZE_ORIENT =   6;
  final static int SIZE_SCALE  =   6;
  final static int SIZE_LEVELS =   6;
  final static int SIZE_SCRAP  =   6;
  final static int SIZE_TEXT   = 128;

  public static final double SCALE = 1.0/DrawingUtil.SCALE_FIX; // TDSetting.mDxfScale; 
  // static double xWorld( double x ) { return (x-DrawingUtil.CENTER_X)*SCALE; }
  // static double yWorld( double y ) { return (y-DrawingUtil.CENTER_Y)*SCALE; }

  int geomType; // geom type
  int nr;   // nuber of objects
  String subdir;
  String name; // file name
  // String path; // file path 
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

  List< String > mFiles; // list of files to which append my files

  // @param yy year [four digit]
  // @param mm month [1..12]
  // @param dd day [1..31]

  /** cstr
   * @param typ
   * @param dir    dirname (relative to the CWD)
   * @param pth    filename
   * @param files  array of file names for the files that must be included in the ZIP
   */
  public ShpObject( int typ, String dir, String pth, List< String > files ) // throws IOException
  { 
    geomType  = typ;
    nr    = 0;
    subdir = dir;
    name   = pth;
    // int pos = path.lastIndexOf("/");
    // name = path.substring( pos+1 );
    mFiles = files;
    setYYMMDD( TDUtil.currentDate() );
  }

  /** left justified blank-padded to length
   * if the numeric is larger than length it is truncated 
   * @param value   numeric value
   * @param len     length of output chars
   * @return char-string of the digits of the numeric value
   */
  protected char[] blankPadded( int value, int len )
  {
    String res = Integer.toString( value );
    char[] ret = new char[len];
    int pad = len - res.length();
    if ( pad >= 0 ) {
      int k = 0;
      for ( ; k < res.length(); ++ k ) ret[k] = res.charAt( k );
      for ( ; k < len; ++ k ) ret[k] = ' ';
    } else {
      // keep only lowest significant digits 
      for ( int k = -pad; k < res.length(); ++ k ) ret[pad+k] = res.charAt( k );
    }
    return ret;
  }

  protected void open( ) throws IOException
  {
    try {
      shpFos = new FileOutputStream( TDFile.getMSfile( subdir, name + ".shp" ) );
      shxFos = new FileOutputStream( TDFile.getMSfile( subdir, name + ".shx" ) );
      dbfFos = new FileOutputStream( TDFile.getMSfile( subdir, name + ".dbf" ) );
      shpChannel = shpFos.getChannel();
      shxChannel = shxFos.getChannel();
      dbfChannel = dbfFos.getChannel();
      if ( mFiles != null ) {
        mFiles.add( name + ".shp" );
        mFiles.add( name + ".shx" );
        mFiles.add( name + ".dbf" );
      }
    } catch ( IOException e ) {
      TDLog.Error("output streams " + e.getMessage() );
      throw e;
    }
  }

  public void setYYMMDD( int y, int m, int d )
  {
    year  = y;
    month = m;
    day   = d;
  }

  public void setYYMMDD( String date )
  {
    year  = TDUtil.dateParseYear( date );
    month = TDUtil.dateParseMonth( date );
    day   = TDUtil.dateParseDay( date );
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
      TDLog.Error("position 0 buffers " + e.getMessage() );
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
    // TDLog.v( "drain buffer pos " + buffer.position() );
    try { 
      buffer.flip();  // set limit to current-pos and pos to 0
      while (buffer.remaining() > 0) channel.write(buffer);   
      buffer.flip().limit(buffer.capacity()); // set limit to capacity and pos to 0
    } catch ( IOException e ) {
      TDLog.Error("drain buffers " + e.getMessage() );
      throw e;
    }
  }

  protected void close() throws IOException
  {
    // TDLog.v( "drain and close" );
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
      TDLog.Error("close shp buffer " + e.getMessage() );
      throw e;
    }
    try {   
      if (shxChannel != null && shxChannel.isOpen()) shxChannel.close();   
      shxFos.close();
    } catch ( IOException e ) {
      TDLog.Error("close shx buffer " + e.getMessage() );
      throw e;
    }
    try {   
      if (dbfChannel != null && dbfChannel.isOpen()) dbfChannel.close();   
      dbfFos.close();
    } catch ( IOException e ) {
      TDLog.Error("close dbf buffer " + e.getMessage() );
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
    // TDLog.v( "drain and close DONE" );
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
  public void writeDBaseHeader( int n_rec, int lenRecord, int n_fld, String[] flds, byte[] types, int[] lens )
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
  public void writeDBaseRecord( int n_fld, String[] vals, int[] lens )
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
  public void writeShpRecordHeader( int cnt, int length )
  {
    shpBuffer.order( ByteOrder.BIG_ENDIAN );   
    shpBuffer.putInt(cnt);   
    shpBuffer.putInt(length);
  }

  public void writeShxRecord( int off, int length )
  {
    shxBuffer.order( ByteOrder.BIG_ENDIAN );   
    shxBuffer.putInt(off);   
    shxBuffer.putInt(length);
  }


  protected void initBBox( double x, double y, double z )
  {
    xmin = xmax = x;
    ymin = ymax = y;
    zmin = zmax = z;
  }

  protected void initBBox( double x, double y ) { initBBox( x, y, 0 ); }

  protected void updateBBox( double x, double y, double z )
  {
    if ( x < xmin ) { xmin = x; } else if ( x > xmax ) { xmax = x; }
    if ( y < ymin ) { ymin = y; } else if ( y > ymax ) { ymax = y; }
    if ( z < zmin ) { zmin = z; } else if ( z > zmax ) { zmax = z; }
  }

  protected void updateBBox( double x, double y )
  {
    if ( x < xmin ) { xmin = x; } else if ( x > xmax ) { xmax = x; }
    if ( y < ymin ) { ymin = y; } else if ( y > ymax ) { ymax = y; }
  }

  // protected void updateBBoxScene( double x, double y )
  // {
  //   updateBBox( xWorld( x ), yWorld(y) );
  // }

}
