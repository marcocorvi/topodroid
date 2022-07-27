/* @file LoxFile.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch file parser
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

// import java.io.File;
import java.io.DataInputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;

// #include "LoxSurvey.h"
// #include "LoxStation.h"
// #include "LoxShot.h"
// #include "LoxScrap.h"
// #include "LoxSurface.h"
// #include "LoxBitmap.h"

import com.topodroid.utils.TDLog;

class LoxFile
{
  private class Chunk_t
  {
    int type;
    int rec_size;
    int rec_cnt;
    int data_size;
    byte[] records;
    byte[] data;

    int size() { return rec_cnt; }

    Chunk_t( int t )
    {
      type      = t;
      rec_size  = 0;
      rec_cnt   = 0;
      data_size = 0;
      records   = null;
      data      = null;
    }
  }

  private Chunk_t mSurveyChunk;
  private Chunk_t mStationChunk;
  private Chunk_t mShotChunk;
  private Chunk_t mScrapChunk;
  private Chunk_t mSurfaceChunk;
  private Chunk_t mBitmapChunk;

  private ArrayList< LoxSurvey >  mSurveys;
  private ArrayList< LoxStation > mStations;
  private ArrayList< LoxShot >    mShots;
  private ArrayList< LoxScrap >   mScraps;
  private LoxSurface              mSurface;
  private LoxBitmap               mBitmap;

  private int linenr = 0; // chunk number

  // @param dis        input stream
  // @param filename   name (for error report)
  LoxFile( DataInputStream dis, String filename ) throws ParserException
  {
    mSurface = null;
    mBitmap  = null;

    mSurveys  = new ArrayList< LoxSurvey >();
    mStations = new ArrayList< LoxStation >();
    mShots    = new ArrayList< LoxShot >();
    mScraps   = new ArrayList< LoxScrap >();
    
    linenr = 0;
    readChunks( dis, filename );
  }

  // int NrSurveys()  { return mSurveyChunk.size(); }
  // int NrStations() { return mStationChunk.size(); }
  // int NrShots()    { return mShotChunk.size(); }
  // int NrScraps()   { return mScrapChunk.size(); }
  // int NrSurfaces() { return mSurfaceChunk.size(); }
  // int NrBitmaps()  { return mBitmapChunk.size(); }

  ArrayList< LoxSurvey >  GetSurveys()  { return mSurveys; }
  ArrayList< LoxStation > GetStations() { return mStations; }
  ArrayList< LoxShot >    GetShots()    { return mShots; }
  // ArrayList< LoxScrap >   GetScraps()   { return mScraps; }
  LoxSurface              GetSurface()  { return mSurface; }
  LoxBitmap               GetBitmap()   { return mBitmap; }

  // static private final int SIZE_SURVEY  = ( ( 6 * Endian.SIZE32 + 0 * Endian.SIZEDBL ) );
  static private final int SIZE_STATION = ( ( 7 * Endian.SIZE32 + 3 * Endian.SIZEDBL ) ); // 52 bytes
  static private final int SIZE_SHOT    = ( ( 5 * Endian.SIZE32 + 9 * Endian.SIZEDBL ) );
  static private final int SIZE_SCRAP   = ( ( 8 * Endian.SIZE32 + 0 * Endian.SIZEDBL ) );
  // static private final int SIZE_SURFACE = ( ( 5 * Endian.SIZE32 + 6 * Endian.SIZEDBL ) );

  private void readChunks( DataInputStream dis, String filename ) throws ParserException
  {
    // TDLog.v(  "LOX read chunks " + filename );
    int type;
    byte[] int32 = new byte[ Endian.SIZE32 ];
    try {
      boolean done = false;
      while ( ! done ) {
	++linenr;
        int len = Endian.readInt32( dis, int32 );
        if ( len == -1 ) { // end of file
          done = true;
          continue;
        }
        type = Endian.toIntLEndian( int32 );
        if ( type < 1 || type > 6 ) {
          TDLog.Error(  "LOX Unexpected chunk type " + type );
          done = true;
          continue;
        }
        Chunk_t c = new Chunk_t( type );
        c.rec_size  = Endian.readInt( dis, int32 );
        c.rec_cnt   = Endian.readInt( dis, int32 );
        c.data_size = Endian.readInt( dis, int32 );
        // TDLog.v(  "LOX Type " + c.type + " RecSize " + c.rec_size + " RecCnt " + c.rec_cnt + " DataSize " + c.data_size );
        if ( c.rec_size > 0 ) {
          c.records = new byte[ c.rec_size ];
          if ( dis.read( c.records, 0, c.rec_size ) != c.rec_size ) throw new ParserException(filename, linenr);
          // TDLog.v(  "LOX " + c.records[0] + " " + c.records[1] + " " + c.records[2] + " " + c.records[3] + " " + 
          //            c.records[4] + " " + c.records[5] + " " + c.records[6] + " " + c.records[7] );
        }
        if ( c.data_size > 0 ) {
          c.data = new byte[ c.data_size ];
          if ( dis.read( c.data, 0, c.data_size ) != c.data_size ) throw new ParserException(filename, linenr);
        }
        // TDLog.v( "LOX Read: bytes " + (4 * Endian.SIZE32 + c.rec_size + c.data_size) );
        switch ( type ) {
          case 1: // SURVEY
            HandleSurvey( c );
            break;
          case 2: // STATIONS
            HandleStations( c );
            break;
          case 3: // SHOTS
            HandleShots( c );
            break;
          case 4: // SCRAPS
            HandleScraps( c );
            break;
          case 5: // SURFACE
            HandleSurface( c );
            break;
          case 6: // SURFACE_BITMAP
            HandleBitmap( c );
            break;
          default:
        }
      }
    } catch( IOException e ) {
      TDLog.Error( "LOX IO error " + e.getMessage() );
      throw new ParserException( filename, linenr );
    } finally {
      try {
        if ( dis != null ) dis.close();
      } catch( IOException e ) { TDLog.v("Error " + e.getMessage() ); }
    }
  }


  private void HandleSurvey( Chunk_t chunk )
  {
    mSurveyChunk = chunk;
    int n0 = chunk.rec_cnt;
    // TDLog.v(  "LOX Handle Survey: Nr. " + n0 );
    byte[] recs = chunk.records; // as int32
    byte[] data = chunk.data;    // as char
    String name  = null;
    String title = null;
    for ( int i=0; i<n0; ++i ) {
      int id = Endian.toIntLEndian( recs, 4*(6*i + 0) );
      int np = Endian.toIntLEndian( recs, 4*(6*i + 1) );
      int ns = Endian.toIntLEndian( recs, 4*(6*i + 2) );
      int pnt= Endian.toIntLEndian( recs, 4*(6*i + 3) );
      int tp = Endian.toIntLEndian( recs, 4*(6*i + 4) );
      int ts = Endian.toIntLEndian( recs, 4*(6*i + 5) );
      name  = getString( data, np, ns );
      title = getString( data, tp, ts );
      // TDLog.v( "LOX " + i + "/" + n0 + ": Survey " + id + " (parent "+ pnt + ") Name " + name + " Title " + title );
      mSurveys.add( new LoxSurvey( id, pnt, name, title ) );
    }
    // TDLog.v( "LOX Handle Survey done");
  }

  private String getString( byte[] data, int np, int ns )
  {
    if ( ns == 0 ) return "";
    String ret = new String( data, np, ns );
    return ret.substring(0, ret.length()-1);
  }
  
  
  private void HandleStations( Chunk_t chunk )
  {
    mStationChunk = chunk;
    int n0 = chunk.rec_cnt;
    byte[] recs = chunk.records; // as int32
    byte[] data = chunk.data;    // as char
    String name    = null;
    String comment = null;
    for ( int i=0; i<n0; ++i ) {
      int off = ( i * SIZE_STATION );
      int id = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int sid= Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int np = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int ns = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int tp = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int ts = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int fl = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      double c0 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double c1 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double c2 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      name    = getString( data, np, ns );
      comment = getString( data, tp, ts );
      mStations.add( new LoxStation( id, sid, name, comment, fl, c0, c1, c2 ) );
      // if ( i < 3 ) {
      //   // TDLog.v( "LOX station " + id + " / " + sid + " <" + name + "> " + np + "-" + ns + " flag " + fl + " " + c0 + " " + c1 + " " + c2 );
      // }
    }
  }
  
  
  private void HandleShots( Chunk_t chunk )
  {
    mShotChunk = chunk;
    int n0 = chunk.rec_cnt;
    byte[] recs = chunk.records; // as int32
    // byte[] data = chunk.data;    // as char
    for ( int i=0; i<n0; ++i ) {
      int off = i * SIZE_SHOT;
      int fr = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int to = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      double f0 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double f1 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double f2 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double f3 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double t0 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double t1 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double t2 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
      double t3 = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
  
      // flag: SURFACE DUPLICATE NOT_VISIBLE NOT_LRUD SPLAY
      int fl = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      // type: NONE OVAL SQUARE DIAMOND TUNNEL
      int ty = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
  
      int sid= Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      double tr = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL; // vthreshold
  
      // LOGI("Shot %d %d (%d) Flag %d Type %d thr %.2f", fr, to, sid, fl, ty, tr );
      // LOGI("  From-LRUD %.2f %.2f %.2f %.2f", f0, f1, f2, f3 );
      // LOGI("  To-LRUD %.2f %.2f %.2f %.2f", t0, t1, t2, t3 );
  
      mShots.add( new LoxShot( fr, to, sid, fl, ty, tr, f0, f1, f2, f3, t0, t1, t2, t3 ) );
      // if ( i < 3 ) {
      //   // TDLog.v( "LOX Shot " + fr + " " + to + " (" + sid + ") flag " + fl + " type " + ty );
      // }
    }
  }
  
  
  private void HandleScraps( Chunk_t chunk )
  {
    mScrapChunk = chunk;
    int n0 = chunk.rec_cnt;
    byte[] recs = chunk.records; // as int32
    byte[] data = chunk.data;    // as char
    for ( int i=0; i<n0; ++i ) {
      int off = i * SIZE_SCRAP;
      int id = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int sid= Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int np = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int pp = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int ps = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int na = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int ap = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      int as = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
      // LOGI("Scrap %d (Survey %d) N.pts %d %d %d N.ang %d %d %d Size %d",
      //   id, sid, np, pp, ps, na, ap, as, mScrapChunk.data_size );
      // assert( pp + np * 3 * sizeof(double) == ap );
      // assert( np * 3 * sizeof(double) == ps );
      // assert( na * 3 * Endian.SIZE32 == as );

      // double * ptr = (double *)( data + pp );
      double[] ptr = new double[ 3*np ];
      for ( int j=0; j<3*np; ++j) {
        ptr[j] = Endian.toDoubleLEndian( data, pp + j*Endian.SIZEDBL );
      }
      // uint32_t * itr = (uint32_t *)( data + ap );
      int[] itr = new int[ 3*na ];
      for ( int k=0; k<3*na; ++k ) {
        itr[k] = Endian.toIntLEndian( data, ap + k*Endian.SIZE32 );
      }
  
      mScraps.add( new LoxScrap( id, sid, np, na, ptr, itr ) );
    }
  }
  
  
  private void HandleSurface( Chunk_t chunk )
  {
    mSurfaceChunk = chunk;
    // int n0 = chunk.rec_cnt;
    byte[] recs = chunk.records; // as int32
    byte[] data = chunk.data;    // as char
    int off = 0;
    int id = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
    int ww = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
    int hh = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
    int dp = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
    int ds = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;  // size in bytes = ww * hh * 8 (8 bytes/double)
    double[] c = new double[6];
    c[0]  = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL; // e0
    c[1]  = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL; // n0
    c[2]  = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL; // e = e0 + C2 * i + C3 * j // not sure about c3/c4
    c[3]  = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
    c[4]  = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL; // n = n0 + C4 * i + C5 * j
    c[5]  = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
    // TDLog.v( "LOX surface id " + id + " " + ww + "x" + hh + " dp " + dp + " ds " + ds + " cal " + c[0] + " " + c[1] + " " + c[2] + " " + c[3] + " " + c[4] + " " + c[5] );

    int npts = ww * hh;
    // assert( ds == npts * sizeof(double) );
    // double * ptr = (double *)( data + dp );
    double[] ptr = new double[ npts ];
    for ( int i=0; i< npts; ++i ) {
      ptr[i] = Endian.toDoubleLEndian( data, dp + i*Endian.SIZEDBL );
    }
    mSurface = new LoxSurface( id, ww, hh, c, ptr );
  }
  
  private void HandleBitmap( Chunk_t chunk )
  {
    mBitmapChunk = chunk;
    // int n0 = chunk.rec_cnt;
    byte[] recs = chunk.records; // as int32
    byte[] data = chunk.data;    // as char
    int off = 0;
    int id = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32; // surface id
    int tp = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32; // type: JPEG PNG
    int dp = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
    int ds = Endian.toIntLEndian( recs, off ); off += Endian.SIZE32;
    double[] c = new double[6];
    c[0] = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
    c[1] = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
    c[2] = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
    c[3] = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
    c[4] = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
    c[5] = Endian.toDoubleLEndian( recs, off ); off += Endian.SIZEDBL;
    // TDLog.v("LOX " + String.format("Bitmap %d Type %d Calib %.2f %.2f %.6f %.6f %.6f %.6f File off %d size %d", id, tp, c[0], c[1], c[2], c[3], c[4], c[5], dp, ds ) );
    // image file binary data
    // unsigned char * img = data + dp;
    mBitmap = new LoxBitmap( id, tp, ds, c, data, dp );
  }

}

