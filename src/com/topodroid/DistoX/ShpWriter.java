/* @file ShpWriter.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile export writer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;   

import java.nio.ByteBuffer;   
import java.nio.MappedByteBuffer;   
import java.nio.ByteOrder;   
import java.nio.channels.FileChannel;   

import java.util.List;

// import android.util.Log;
   
class ShpObject
{
  final static int SHP_MAGIC = 9994;
  final static int SHP_VERSION = 1000;
  final static int SHP_POINT     =  1;
  final static int SHP_POLYLINE  =  3;
  final static int SHP_POLYGON   =  5;
  final static int SHP_POINTZ    = 11;
  final static int SHP_POLYLINEZ = 13;

  final static byte BYTE0  = (byte)0;
  final static byte BYTEC  = (byte)'C';
  final static byte BYTEN  = (byte)'N';
  final static short SHORT0 = (short)0;

  static final float SCALE = 1.0f/DrawingUtil.SCALE_FIX; // TDSetting.mDxfScale; 
  // static double xWorld( double x ) { return (x-DrawingUtil.CENTER_X)*SCALE; }
  // static double yWorld( double y ) { return (y-DrawingUtil.CENTER_Y)*SCALE; }

  int geomType; // geom type
  int nr;   // nuber of objects
  String path; // file path 
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

  List<File> mFiles; // list of files to which append my files

  // @param yy year [four digit]
  // @param mm month [1..12]
  // @param dd day [1..31]
  ShpObject( int typ, String pth, List<File> files ) // throws IOException
  { 
    geomType  = typ;
    nr    = 0;
    path  = pth;
    mFiles = files;
    setYYMMDD( TDUtil.currentDate() );
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
        mFiles.add( new File( path + ".shp" ) );
        mFiles.add( new File( path + ".shx" ) );
        mFiles.add( new File( path + ".dbf" ) );
      }
    } catch ( IOException e ) {
      TDLog.Error("output streams " + e.getMessage() );
      throw e;
    }
  }

  void setYYMMDD( int y, int m, int d )
  {
    year  = y;
    month = m;
    day   = d;
  }

  void setYYMMDD( String date )
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
    // Log.v("DistoX", "drain buffer pos " + buffer.position() );
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
    // Log.v("DistoX", "drain and close" );
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
    // Log.v("DistoX", "drain and close DONE" );
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
// ---------------------------------------------------------------------------------------------
// 3D classes

class ShpPointz extends ShpObject
{
  ShpPointz( String path, List<File> files ) // throws IOException
  {
    super( SHP_POINTZ, path, files );
  }

  // write headers for POINTZ
  boolean writeStations( List<NumStation> pts ) throws IOException
  {
    int n_pts = (pts != null)? pts.size() : 0;
    if ( n_pts == 0 ) return false;

    int n_fld = 1;
    String[] fields = new String[ n_fld ];
    fields[0] = "name";
    byte[]   ftypes = { BYTEC };
    int[]    flens  = { 16 };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + n_pts * shpRecLen; // [16-bit words]
    int shxLength = 50 + n_pts * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + n_pts * dbfRecLen; // [Bytes]

    setBoundsStations( pts );
    // Log.v("DistoX", "POINTZ " + pts.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // Log.v("DistoX", "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength+8, 2*shxLength+8, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, SHP_POINTZ, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, SHP_POINTZ, shxLength );
    writeDBaseHeader( n_pts, dbfRecLen, n_fld, fields, ftypes, flens );
    // Log.v("DistoX", "POINTZ done headers");

    int cnt = 0;
    for ( NumStation pt : pts ) {
      int offset = 50 + cnt * shpRecLen; 
      writeShpRecordHeader( cnt, shpRecLen );
      shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
      shpBuffer.putInt( SHP_POINTZ );
      // Log.v("DistoX", "POINTZ " + cnt + ": " + pt.e + " " + pt.s + " " + pt.v + " offset " + offset );
      shpBuffer.putDouble( pt.e );
      shpBuffer.putDouble( pt.s );
      shpBuffer.putDouble( pt.v );
      shpBuffer.putDouble( 0.0 );

      writeShxRecord( offset, shpRecLen );
      fields[0] = pt.name;
      writeDBaseRecord( n_fld, fields, flens );
      ++cnt;
    }
    // Log.v("DistoX", "POINTZ done records");
    close();
    return true;
  }

  // record length [word]: 4 + 36/2
  @Override protected int getShpRecordLength( ) { return 22; }
    
  // Utility: set the bounding box of the set of geometries
  private void setBoundsStations( List<NumStation> pts ) 
  {
    if ( pts.size() == 0 ) {
      xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
      return;
    }
    NumStation pt = pts.get(0);
    initBBox( pt.e, pt.s, pt.v );
    for ( int k=pts.size() - 1; k>0; --k ) {
      pt = pts.get(k);
      updateBBox( pt.e, pt.s, pt.v );
    }
  }
}

class ShpPolylinez extends ShpObject
{
  ShpPolylinez( String path, List<File> files ) // throws IOException
  {
    super( SHP_POLYLINEZ, path, files );
  }

  boolean writeShots( List<NumShot> lns, List<NumSplay> lms ) throws IOException
  {
    int nrs = ( lns != null )? lns.size() : 0;
    int mrs = ( lms != null )? lms.size() : 0;
    int nr  = nrs + mrs;

    if ( nr == 0 ) return false;

    int n_fld = 5; // type from to flag comment
    String[] fields = new String[ n_fld ];
    fields[0] = "type";
    fields[1] = "from";
    fields[2] = "to";
    fields[3] = "flag";
    fields[4] = "comment";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC, BYTEC, BYTEC }; // use only strings
    int[]    flens  = { 8, 16, 16, 8, 32 };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + nr * shpRecLen; // [16-bit words]
    int shxLength = 50 + nr * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + nr * dbfRecLen; // Bytes, 3 fields

    setBoundsShots( lns, lms );
    // Log.v("DistoX", "POLYLINEZ shots " + lns.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // Log.v("DistoX", "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength, 2*shxLength, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, SHP_POLYLINEZ, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, SHP_POLYLINEZ, shxLength );
    writeDBaseHeader( nr, dbfRecLen, n_fld, fields, ftypes, flens );
    // Log.v("DistoX", "shots done headers" );

    int cnt = 0;
    if ( lns != null && nrs > 0 ) {
      for ( NumShot ln : lns ) {
        NumStation p1 = ln.from;
        NumStation p2 = ln.to;
        int offset = 50 + cnt * shpRecLen; 
        ++cnt;

        writeShpRecord( cnt, shpRecLen, p1, p2 );
        writeShxRecord( offset, shpRecLen );
	fields[0] = "leg";
	fields[1] = p1.name;
	fields[2] = p2.name;
	fields[3] = String.format("0x%02x", ln.getReducedFlag() ); // flag
	fields[4] = ln.getComment();
        writeDBaseRecord( n_fld, fields, flens );
      }
    }
    if ( lms != null && mrs > 0 ) {
      NumStation p2 = new NumStation( "-" );
      for ( NumSplay lm : lms ) {
        NumStation p1 = lm.from;
        p2.e = lm.e;
        p2.s = lm.s;
        p2.v = lm.v;
        int offset = 50 + cnt * shpRecLen; 
        ++cnt;

        writeShpRecord( cnt, shpRecLen, p1, p2 );
        writeShxRecord( offset, shpRecLen );
	fields[0] = "splay";
	fields[1] = p1.name;
	fields[2] = "-";
	fields[3] = String.format("0x%02x", lm.getReducedFlag() ); // flag
	fields[4] = lm.getComment();
        writeDBaseRecord( n_fld, fields, flens );
      }
    }
    // Log.v("DistoX", "shots done records" );
    close();
    return true;
  }

  private void writeShpRecord( int cnt, int len, NumStation p1, NumStation p2 )
  {
    writeShpRecordHeader( cnt, len );
    shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
    shpBuffer.putInt( SHP_POLYLINEZ );
    double xmin = p1.e; double xmax = p2.e; if ( xmin > xmax ) { xmin=p2.e; xmax=p1.e; }
    double ymin = p1.s; double ymax = p2.s; if ( ymin > ymax ) { ymin=p2.s; ymax=p1.s; }
    double zmin = p1.v; double zmax = p2.v; if ( zmin > zmax ) { zmin=p2.v; zmax=p1.v; }
    shpBuffer.putDouble( xmin );
    shpBuffer.putDouble( ymin );
    shpBuffer.putDouble( xmax );
    shpBuffer.putDouble( ymax );
    shpBuffer.putInt( 1 ); // one part: number of parts
    shpBuffer.putInt( 2 ); // two points: total number of points
    shpBuffer.putInt( 0 ); // part 0 starts with point 0 (and ends with point 1)
    shpBuffer.putDouble( p1.e );
    shpBuffer.putDouble( p1.s );
    shpBuffer.putDouble( p2.e );
    shpBuffer.putDouble( p2.s );
    shpBuffer.putDouble( zmin );
    shpBuffer.putDouble( zmax );
    shpBuffer.putDouble( p1.v );
    shpBuffer.putDouble( p2.v );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
    shpBuffer.putDouble( 0.0 );
  }

  @Override protected int getShpRecordLength( ) { return 76; }

  private void setBoundsShots( List<NumShot> lns, List<NumSplay> lms )
  {
    int nrs = ( lns != null )? lns.size() : 0;
    int mrs = ( lms != null )? lms.size() : 0;
    // if ( lns.size() == 0 && lms.size() ) { // guaramteed one is non-zero
    //   xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
    //   return;
    // }
    if ( nrs > 0 ) {
      NumShot ln = lns.get(0);
      NumStation pt = ln.from;
      initBBox( pt.e, pt.s, pt.v );
      pt = ln.to;
      updateBBox( pt.e, pt.s, pt.v );
      for ( int k=1; k<nrs; ++k ) {
        ln = lns.get(k);
        pt = ln.from;
        updateBBox( pt.e, pt.s, pt.v );
        pt = ln.to;
        updateBBox( pt.e, pt.s, pt.v );
      }
      if ( mrs > 0 ) {
        for ( int k=0; k<mrs; ++k ) {
          NumSplay lm = lms.get(k);
          pt = lm.from;
          updateBBox( pt.e, pt.s, pt.v );
          updateBBox( lm.e, lm.s, lm.v );
        }
      }
    } else { // mrs > 0
      NumSplay lm = lms.get(0);
      NumStation pt = lm.from;
      initBBox( pt.e, pt.s, pt.v );
      updateBBox( lm.e, lm.s, lm.v );
      for ( int k=1; k<mrs; ++k ) {
        lm = lms.get(k);
        pt = lm.from;
        updateBBox( pt.e, pt.s, pt.v );
        updateBBox( lm.e, lm.s, lm.v );
      }
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

// =================================================================
// 2D classes

class ShpPoint extends ShpObject
{
  ShpPoint( String path, List<File> files ) // throws IOException
  {
    super( SHP_POINT, path, files );
  }

  // write headers for POINT
  boolean writePoints( List<DrawingPointPath> pts, float x0, float y0, float xscale, float yscale ) throws IOException
  {
    int n_pts = (pts != null)? pts.size() : 0;
    // Log.v("DistoX", "SHP write points " + n_pts );
    if ( n_pts == 0 ) return false;

    int n_fld = 3;
    String[] fields = new String[ n_fld ];
    fields[0] = "name";
    fields[1] = "orient";
    fields[2] = "levels";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC };
    int[]    flens  = { 16, 6, 6 };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + n_pts * shpRecLen; // [16-bit words]
    int shxLength = 50 + n_pts * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + n_pts * dbfRecLen; // [Bytes]

    setBoundsPoints( pts, x0, y0, xscale, yscale );
    // Log.v("DistoX", "POINTZ " + pts.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // Log.v("DistoX", "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength+8, 2*shxLength+8, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, SHP_POINT, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, SHP_POINT, shxLength );
    writeDBaseHeader( n_pts, dbfRecLen, n_fld, fields, ftypes, flens );
    // Log.v("DistoX", "POINTZ done headers");

    int cnt = 0;
    for ( DrawingPointPath pt : pts ) {
      int offset = 50 + cnt * shpRecLen; 
      writeShpRecordHeader( cnt, shpRecLen );
      shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
      shpBuffer.putInt( SHP_POINT );
      // Log.v("DistoX", "POINTZ " + cnt + ": " + pt.e + " " + pt.s + " " + pt.v + " offset " + offset );
      shpBuffer.putDouble( x0 + xscale*(pt.cx - DrawingUtil.CENTER_X) );
      shpBuffer.putDouble( y0 - yscale*(pt.cy - DrawingUtil.CENTER_Y) );

      writeShxRecord( offset, shpRecLen );
      fields[0] = BrushManager.getPointThName( pt.mPointType );
      fields[1] = Integer.toString( (int)pt.mOrientation ); 
      fields[2] = Integer.toString( pt.mLevel );
      writeDBaseRecord( n_fld, fields, flens );
      ++cnt;
    }
    // Log.v("DistoX", "POINTZ done records");
    close();
    return true;
  }

  // record length [words]: 4 + 20/2
  @Override protected int getShpRecordLength( ) { return 14; }
    
  // Utility: set the bounding box of the set of geometries
  private void setBoundsPoints( List<DrawingPointPath> pts, float x0, float y0, float xscale, float yscale ) 
  {
    if ( pts.size() == 0 ) {
      xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
      return;
    }
    DrawingPointPath pt = pts.get(0);
    double xx = x0+xscale*(pt.cx - DrawingUtil.CENTER_X);
    double yy = y0-yscale*(pt.cy - DrawingUtil.CENTER_Y);
    initBBox( xx, yy );
    for ( int k=pts.size() - 1; k>0; --k ) {
      pt = pts.get(k);
      xx = x0+xscale*(pt.cx - DrawingUtil.CENTER_X);
      yy = y0-yscale*(pt.cy - DrawingUtil.CENTER_Y);
      updateBBox( xx, yy );
    }
  }
}

class ShpStation extends ShpObject
{
  ShpStation( String path, List<File> files ) // throws IOException
  {
    super( SHP_POINT, path, files );
  }

  // write headers for POINT
  boolean writeStations( List<DrawingStationName> pts, float x0, float y0, float xscale, float yscale ) throws IOException
  {
    int n_pts = (pts != null)? pts.size() : 0;
    // Log.v("DistoX", "SHP write stations " + n_pts );
    if ( n_pts == 0 ) return false;

    int n_fld = 1;
    String[] fields = new String[ n_fld ];
    fields[0] = "name";
    byte[]   ftypes = { BYTEC };
    int[]    flens  = { 16 };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + n_pts * shpRecLen; // [16-bit words]
    int shxLength = 50 + n_pts * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + n_pts * dbfRecLen; // [Bytes]

    setBoundsPoints( pts, x0, y0, xscale, yscale );
    // Log.v("DistoX", "POINT station " + pts.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // Log.v("DistoX", "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength+8, 2*shxLength+8, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, SHP_POINT, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, SHP_POINT, shxLength );
    writeDBaseHeader( n_pts, dbfRecLen, n_fld, fields, ftypes, flens );
    // Log.v("DistoX", "POINTZ done headers");

    int cnt = 0;
    for ( DrawingStationName st : pts ) {
      int offset = 50 + cnt * shpRecLen; 
      writeShpRecordHeader( cnt, shpRecLen );
      shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
      shpBuffer.putInt( SHP_POINT );
      // Log.v("DistoX", "POINT station " + cnt + ": " + st.e + " " + st.s + " " + st.v + " offset " + offset );
      shpBuffer.putDouble( x0 + xscale*(st.cx - DrawingUtil.CENTER_X) );
      shpBuffer.putDouble( y0 - yscale*(st.cy - DrawingUtil.CENTER_Y) );

      writeShxRecord( offset, shpRecLen );
      fields[0] = st.getName();
      writeDBaseRecord( n_fld, fields, flens );
      ++cnt;
    }
    // Log.v("DistoX", "POINT station done records");
    close();
    return true;
  }

  // record length [words]: 4 + 20/2
  @Override protected int getShpRecordLength( ) { return 14; }
    
  // Utility: set the bounding box of the set of geometries
  private void setBoundsPoints( List<DrawingStationName> pts, float x0, float y0, float xscale, float yscale ) 
  {
    if ( pts.size() == 0 ) {
      xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
      return;
    }
    DrawingStationName st = pts.get(0);
    double xx = x0+xscale*(st.cx - DrawingUtil.CENTER_X);
    double yy = y0-yscale*(st.cy - DrawingUtil.CENTER_Y);
    initBBox( xx, yy );
    for ( int k=pts.size() - 1; k>0; --k ) {
      st = pts.get(k);
      xx = x0+xscale*(st.cx - DrawingUtil.CENTER_X);
      yy = y0-yscale*(st.cy - DrawingUtil.CENTER_Y);
      updateBBox( xx, yy );
    }
  }
}
// This class handles lines and areas
class ShpPolyline extends ShpObject
{
  // int mPathType; 

  // @param path_type   either DRAWING_PATH_LINE or DRAWING_PATH_AREA
  ShpPolyline( String path, int path_type, List<File> files ) // throws IOException
  {
    super( ( (path_type == DrawingPath.DRAWING_PATH_LINE)? SHP_POLYLINE : SHP_POLYGON ), path, files );
    // mPathType = path_type;
  }

  void writeLines( List<DrawingPointLinePath> lns, float x0, float y0, float xscale, float yscale ) throws IOException
  {
    writwPointLines( lns, DrawingPath.DRAWING_PATH_LINE, x0, y0, xscale, yscale );
  }

  void writeAreas( List<DrawingPointLinePath> lns, float x0, float y0, float xscale, float yscale ) throws IOException
  {
    writwPointLines( lns, DrawingPath.DRAWING_PATH_AREA, x0, y0, xscale, yscale );
  }

  private boolean writwPointLines( List<DrawingPointLinePath> lns, int path_type, float x0, float y0, float xscale, float yscale ) throws IOException
  {
    int nrs = ( lns != null )? lns.size() : 0;
    if ( nrs == 0 ) return false;

    int n_fld = 3; // type from to flag comment
    String[] fields = new String[ n_fld ];
    fields[0] = "type";
    fields[1] = "name";
    fields[2] = "levels";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC }; // use only strings
    int[]    flens  = { 8, 16, 6 };

    int shpLength = 50;
    for ( DrawingPointLinePath ln : lns ) {
      int close = ( path_type == DrawingPath.DRAWING_PATH_AREA || ln.isClosed() )? 1 : 0;
      shpLength += getShpRecordLength( ln.size() + close );
    }

    int shxRecLen = getShxRecordLength( );
    int shxLength = 50 + nrs * shxRecLen;

    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 
    int dbfLength = 33 + n_fld * 32 + nrs * dbfRecLen; // Bytes, 2 fields

    setBoundsLines( lns, x0, y0, xscale, yscale );
    // Log.v("DistoX", "POLYLINEZ shots " + lns.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // Log.v("DistoX", "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength, 2*shxLength, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, geomType, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, geomType, shxLength );
    writeDBaseHeader( nrs, dbfRecLen, n_fld, fields, ftypes, flens );
    // Log.v("DistoX", "shots done headers" );

    int cnt = 1;
    int offset = 50;
    if ( lns != null && nrs > 0 ) {
      for ( DrawingPointLinePath ln : lns ) {
	if ( ln.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath line = (DrawingLinePath)ln;
          fields[0] = "line";
	  fields[1] = BrushManager.getLineThName( line.mLineType );
          fields[2] = Integer.toString( line.mLevel );
	} else if ( ln.mType == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath area = (DrawingAreaPath)ln;
          fields[0] = "area";
	  fields[1] = BrushManager.getAreaThName( area.mAreaType );
          fields[2] = Integer.toString( area.mLevel );
	} else {
	  fields[0] = "undef";
          fields[1] = "undef";
          fields[2] = "0";
	}
        int close = ( ln.mType == DrawingPath.DRAWING_PATH_AREA || ln.isClosed() )? 1 : 0;
	int shp_len = getShpRecordLength( ln.size() + close );

        writeShpRecord( cnt, shp_len, ln, close, x0, y0, xscale, yscale );
        writeShxRecord( offset, shp_len );
        writeDBaseRecord( n_fld, fields, flens );

        offset += shp_len;
        ++cnt;
      }
    }
    // Log.v("DistoX", "shots done records" );
    close();
    return true;
  }

  private void writeShpRecord( int cnt, int len, DrawingPointLinePath ln, int close, float x0, float y0, float xscale, float yscale )
  {
    double xmin, ymin, xmax, ymax;
    LinePoint pt = ln.mFirst;
    {
      xmin = xmax =  pt.x;
      ymin = ymax = -pt.y;
      for ( pt = pt.mNext; pt != null; pt = pt.mNext ) {
        if (  pt.x < xmin ) { xmin =  pt.x; } else if (  pt.x > xmax ) { xmax =  pt.x; }
        if ( -pt.y < ymin ) { ymin = -pt.y; } else if ( -pt.y > ymax ) { ymax = -pt.y; }
      }
    }
    xmin = x0 + xscale*(xmin-DrawingUtil.CENTER_X);
    ymin = y0 + yscale*(ymin-DrawingUtil.CENTER_Y);
    xmax = x0 + xscale*(xmax-DrawingUtil.CENTER_X);
    ymax = y0 + yscale*(ymax-DrawingUtil.CENTER_Y);

    writeShpRecordHeader( cnt, len );
    shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
    shpBuffer.putInt( geomType );
    shpBuffer.putDouble( xmin );
    shpBuffer.putDouble( ymin );
    shpBuffer.putDouble( xmax );
    shpBuffer.putDouble( ymax );
    shpBuffer.putInt( 1 ); // one part: number of parts
    shpBuffer.putInt( ln.size() + close ); // total number of points
    shpBuffer.putInt( 0 ); // part 0 starts with point 0 
    for ( pt = ln.mFirst; pt != null; pt = pt.mNext ) {
      shpBuffer.putDouble( x0+xscale*(pt.x-DrawingUtil.CENTER_X) );
      shpBuffer.putDouble( y0-yscale*(pt.y-DrawingUtil.CENTER_Y) );
    }
    if ( close == 1 ) {
      pt = ln.mFirst;
      shpBuffer.putDouble( x0+xscale*(pt.x-DrawingUtil.CENTER_X) );
      shpBuffer.putDouble( y0-yscale*(pt.y-DrawingUtil.CENTER_Y) );
    }
  }

  // polyline record length [word]: 4 + (48 + npt * 16)/2
  // 4 for the record header (2 int)
  // @Override 
  protected int getShpRecordLength( int npt ) { return 28 + npt * 8; }

  private void setBoundsLines( List<DrawingPointLinePath> lns, float x0, float y0, float xscale, float yscale )
  {
    int nrs = ( lns != null )? lns.size() : 0;
    if ( nrs > 0 ) {
      DrawingPointLinePath ln = lns.get(0);
      LinePoint pt = ln.mFirst;
      double xx = x0 + xscale * (pt.x - DrawingUtil.CENTER_X);
      double yy = y0 - yscale * (pt.y - DrawingUtil.CENTER_Y);
      initBBox( xx, yy );
      for ( pt = pt.mNext; pt != null; pt = pt.mNext ) {
        xx = x0 + xscale * (pt.x - DrawingUtil.CENTER_X);
        yy = y0 - yscale * (pt.y - DrawingUtil.CENTER_Y);
        updateBBox( xx, yy );
      }
      for ( int k=1; k<nrs; ++k ) {
        ln = lns.get(k);
        for ( pt = ln.mFirst; pt != null; pt = pt.mNext ) {
          xx = x0 + xscale * (pt.x - DrawingUtil.CENTER_X);
          yy = y0 - yscale * (pt.y - DrawingUtil.CENTER_Y);
          updateBBox( xx, yy );
        }
      }
    }
  }

}


// This class handles shots: les and splays
class ShpSegment extends ShpObject
{
  ShpSegment( String path, List<File> files ) // throws IOException
  {
    super( SHP_POLYLINE, path, files );
  }

  // @param x0 x-offset
  // @param y0 y-offset
  boolean writeSegments( List<DrawingPath> sgms, float x0, float y0, float xscale, float yscale ) throws IOException
  {
    int nrs = ( sgms != null )? sgms.size() : 0;
    if ( nrs == 0 ) return false;

    int n_fld = 3; // type from to flag comment
    String[] fields = new String[ n_fld ];
    fields[0] = "type";
    fields[1] = "from";
    fields[2] = "to";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC }; // use only strings
    int[]    flens  = { 8, 16, 16 };

    int shpLength = 50 + nrs * getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int shxLength = 50 + nrs * shxRecLen;

    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 
    int dbfLength = 33 + n_fld * 32 + nrs * dbfRecLen; // Bytes, 3 fields

    setBoundsLines( sgms, x0, y0, xscale, yscale );
    // Log.v("DistoX", "POLYLINE shots " + lns.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // Log.v("DistoX", "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength, 2*shxLength, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, geomType, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, geomType, shxLength );
    writeDBaseHeader( nrs, dbfRecLen, n_fld, fields, ftypes, flens );
    // Log.v("DistoX", "shots done headers" );

    int cnt = 1;
    int offset = 50;
    if ( sgms != null && nrs > 0 ) {
      for ( DrawingPath sgm : sgms ) {
	DBlock blk = sgm.mBlock;
	if ( sgm.mType == DrawingPath.DRAWING_PATH_FIXED ) {
          fields[0] = "leg";
	  fields[1] = blk.mFrom;
	  fields[2] = blk.mTo;
	} else if ( sgm.mType == DrawingPath.DRAWING_PATH_AREA ) {
          fields[0] = "splay";
	  fields[1] = blk.mFrom;
	  fields[2] = "-";
	}
	int shp_len = getShpRecordLength( );

        writeShpRecord( cnt, shp_len, sgm, x0, y0, xscale, yscale );
        writeShxRecord( offset, shp_len );
        writeDBaseRecord( n_fld, fields, flens );

        offset += shp_len;
        ++cnt;
      }
    }
    // Log.v("DistoX", "shots done records" );
    close();
    return true;
  }

  private void writeShpRecord( int cnt, int len, DrawingPath sgm, float x0, float y0, float xscale, float yscale )
  {
    double xmin, ymin, xmax, ymax;
    {
      xmin = xmax =  sgm.x1;
      ymin = ymax = -sgm.y1;
      if (  sgm.x2 < xmin ) { xmin =  sgm.x2; } else if (  sgm.x2 > xmax ) { xmax =  sgm.x2; }
      if ( -sgm.y2 < ymin ) { ymin = -sgm.y2; } else if ( -sgm.y2 > ymax ) { ymax = -sgm.y2; }
    }
    xmin = x0 + xscale*(xmin-DrawingUtil.CENTER_X);
    xmax = x0 + xscale*(xmax-DrawingUtil.CENTER_X);
    ymin = y0 + yscale*(ymin-DrawingUtil.CENTER_Y);
    ymax = y0 + yscale*(ymax-DrawingUtil.CENTER_Y);

    writeShpRecordHeader( cnt, len );
    shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
    shpBuffer.putInt( SHP_POLYLINE ); // geomType );
    shpBuffer.putDouble( xmin );
    shpBuffer.putDouble( ymin );
    shpBuffer.putDouble( xmax );
    shpBuffer.putDouble( ymax );
    shpBuffer.putInt( 1 ); // one part: number of parts
    shpBuffer.putInt( 2 ); // total number of points
    shpBuffer.putInt( 0 ); // part 0 starts with point 0 
    shpBuffer.putDouble( x0+xscale*(sgm.x1-DrawingUtil.CENTER_X) );
    shpBuffer.putDouble( y0-yscale*(sgm.y1-DrawingUtil.CENTER_Y) );

    shpBuffer.putDouble( x0+xscale*(sgm.x2-DrawingUtil.CENTER_X) );
    shpBuffer.putDouble( y0-yscale*(sgm.y2-DrawingUtil.CENTER_Y) );
  }

  // segment record length [word]: 4 + (48 + npt * 16)/2   [npt = 2]
  // 4 for the record header (2 int)
  // @Override 
  protected int getShpRecordLength( ) { return 28 + 2 * 8; }

  private void setBoundsLines( List<DrawingPath> sgms, float x0, float y0, float xscale, float yscale )
  {
    int nrs = ( sgms != null )? sgms.size() : 0;
    if ( nrs > 0 ) {
      DrawingPath sgm = sgms.get(0);
      double xx = x0 + xscale * ( sgm.x1 - DrawingUtil.CENTER_X );
      double yy = y0 - xscale * ( sgm.y1 - DrawingUtil.CENTER_Y ); 
      initBBox( xx, yy );
      xx = x0 + xscale * ( sgm.x2 - DrawingUtil.CENTER_X );
      yy = y0 - xscale * ( sgm.y2 - DrawingUtil.CENTER_Y ); 
      updateBBox( xx, yy );

      for ( int k=1; k<nrs; ++k ) {
        sgm = sgms.get(k);
        xx = x0 + xscale * ( sgm.x1 - DrawingUtil.CENTER_X );
        yy = y0 - xscale * ( sgm.y1 - DrawingUtil.CENTER_Y ); 
        updateBBox( xx, yy );
        xx = x0 + xscale * ( sgm.x2 - DrawingUtil.CENTER_X );
        yy = y0 - xscale * ( sgm.y2 - DrawingUtil.CENTER_Y ); 
        updateBBox( xx, yy );
      }
    }
  }
}
