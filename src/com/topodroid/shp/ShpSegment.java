/* @file ShpSegment.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile shot segment
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
import com.topodroid.DistoX.DrawingPath;
import com.topodroid.DistoX.DrawingUtil;
import com.topodroid.DistoX.DBlock;

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


// This class handles shots: les and splays
public class ShpSegment extends ShpObject
{
  public ShpSegment( String path, List<File> files ) // throws IOException
  {
    super( SHP_POLYLINE, path, files );
  }

  // @param x0 x-offset
  // @param y0 y-offset
  public boolean writeSegments( List<DrawingPath> sgms, float x0, float y0, float xscale, float yscale ) throws IOException
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
