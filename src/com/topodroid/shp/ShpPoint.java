/* @file ShpPoint.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile 2D point
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
import com.topodroid.DistoX.DrawingPointPath;
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

public class ShpPoint extends ShpObject
{
  public ShpPoint( String path, List<File> files ) // throws IOException
  {
    super( SHP_POINT, path, files );
  }

  // write headers for POINT
  public boolean writePoints( List<DrawingPointPath> pts, float x0, float y0, float xscale, float yscale ) throws IOException
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
      fields[0] = pt.getThName( );
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
