/* @file ShpPointz.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile 3D point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.shp;

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

import android.util.Log;

public class ShpPointz extends ShpObject
{
  public ShpPointz( String path, List< File > files ) // throws IOException
  {
    super( SHP_POINTZ, path, files );
  }

  // write headers for POINTZ
  public boolean writeStations( List< NumStation > pts ) throws IOException
  {
    int n_pts = (pts != null)? pts.size() : 0;
    if ( n_pts == 0 ) return false;

    int n_fld = 1;
    String[] fields = new String[ n_fld ];
    fields[0] = "name";
    byte[]   ftypes = { BYTEC };
    int[]    flens  = { SIZE_NAME };

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
  private void setBoundsStations( List< NumStation > pts ) 
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
