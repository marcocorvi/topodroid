/* @file ShpPolylinez.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile 3D polyline
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.shp;

import com.topodroid.utils.TDLog;
import com.topodroid.num.NumStation;
import com.topodroid.num.NumShot;
import com.topodroid.num.NumSplay;
import com.topodroid.DistoX.DrawingUtil;

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

public class ShpPolylinez extends ShpObject
{
  public ShpPolylinez( String path, List<File> files ) // throws IOException
  {
    super( SHP_POLYLINEZ, path, files );
  }

  public boolean writeShots( List<NumShot> lns, List<NumSplay> lms ) throws IOException
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
