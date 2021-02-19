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

import android.util.Log;


// This class handles shots: les and splays
public class ShpSegment extends ShpObject
{
  public ShpSegment( String path, List< File > files ) // throws IOException
  {
    super( SHP_POLYLINE, path, files );
  }

  // @param x0 x-offset
  // @param y0 y-offset
  public boolean writeSegments( List< DrawingPath > sgms, double x0, double y0, double xscale, double yscale, double cd, double sd ) throws IOException
  {
    int nrs = ( sgms != null )? sgms.size() : 0;
    if ( nrs == 0 ) return false;
    // Log.v("DistoX", "SHOT cd " + cd + " sd " + sd + " Xscale " + xscale + " Yscale " + yscale );

    int n_fld = 4; // type from to flag comment
    String[] fields = new String[ n_fld ];
    fields[0] = "type";
    fields[1] = "from";
    fields[2] = "to";
    fields[3] = "flag";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC, BYTEC }; // use only strings
    int[]    flens  = { 8, 16, 16, 6 };

    int shpLength = 50 + nrs * getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int shxLength = 50 + nrs * shxRecLen;

    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 
    int dbfLength = 33 + n_fld * 32 + nrs * dbfRecLen; // Bytes, 3 fields

    setBoundsLines( sgms, x0, y0, xscale, yscale, cd, sd );
    // Log.v("DistoX", "POLYLINE shots " + lns.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // Log.v("DistoX", "SHOTS bbox X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " Z " + zmin + " " + zmax );

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
        boolean write = false;
	DBlock blk = sgm.mBlock;
	if ( sgm.mType == DrawingPath.DRAWING_PATH_FIXED ) {
          write = true;
          fields[0] = "leg";
	  fields[1] = blk.mFrom;
	  fields[2] = blk.mTo;
          fields[3] = Long.toString( blk.getFlag() );
	} else if ( sgm.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
          write = true;
          fields[0] = "splay";
	  fields[1] = blk.mFrom;
	  fields[2] = "-";
          fields[3] = Long.toString( blk.getFlag() );
	}
        if ( write ) {
	  int shp_len = getShpRecordLength( );

          writeShpRecord( cnt, shp_len, sgm, x0, y0, xscale, yscale, cd, sd );
          writeShxRecord( offset, shp_len );
          writeDBaseRecord( n_fld, fields, flens );

          offset += shp_len;
          ++cnt;
        }
      }
    }
    // Log.v("DistoX", "shots done records" );
    close();
    return true;
  }

  private void writeShpRecord( int cnt, int len, DrawingPath sgm, double x0, double y0, double xscale, double yscale, double cd, double sd )
  {
    double x1 = DrawingUtil.declinatedX( sgm.x1, sgm.y1, cd, sd );
    double y1 = DrawingUtil.declinatedY( sgm.x1, sgm.y1, cd, sd );
    double x2 = DrawingUtil.declinatedX( sgm.x2, sgm.y2, cd, sd );
    double y2 = DrawingUtil.declinatedY( sgm.x2, sgm.y2, cd, sd );

    double xmin, ymin, xmax, ymax;
    {
      xmin = xmax =  x1; //  sgm.x1;
      ymin = ymax = -y1; // -sgm.y1;
      if (  x2 < xmin ) { xmin =  x2; } else if (  x2 > xmax ) { xmax =  x2; }
      if ( -y2 < ymin ) { ymin = -y2; } else if ( -y2 > ymax ) { ymax = -y2; }
    }
    xmin = x0 + xscale * xmin;
    xmax = x0 + xscale * xmax;
    ymin = y0 + yscale * ymin;
    ymax = y0 + yscale * ymax;
    // Log.v("DistoX", "SHOT record bbox X " + xmin + " " + xmax + " Y " + ymin + " " + ymax );

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
    shpBuffer.putDouble( x0 + xscale*x1 );
    shpBuffer.putDouble( y0 - yscale*y1 );
    // Log.v("DistoX", "SHOT record [1] " + (x0 + xscale*x1) + " " + (y0 - yscale*y1 ) );

    shpBuffer.putDouble( x0 + xscale*x2 );
    shpBuffer.putDouble( y0 - yscale*y2 );
    // Log.v("DistoX", "SHOT record [2] " + (x0 + xscale*x2) + " " + (y0 - yscale*y2 ) );
  }

  // segment record length [word]: 4 + (48 + npt * 16)/2   [npt = 2]
  // 4 for the record header (2 int)
  // @Override 
  protected int getShpRecordLength( ) { return 28 + 2 * 8; }

  private void setBoundsLines( List< DrawingPath > sgms, double x0, double y0, double xscale, double yscale, double cd, double sd )
  {
    int nrs = ( sgms != null )? sgms.size() : 0;
    if ( nrs > 0 ) {
      DrawingPath sgm = sgms.get(0);
      double x1 = DrawingUtil.declinatedX( sgm.x1, sgm.y1, cd, sd );
      double y1 = DrawingUtil.declinatedY( sgm.x1, sgm.y1, cd, sd );
      double x2 = DrawingUtil.declinatedX( sgm.x2, sgm.y2, cd, sd );
      double y2 = DrawingUtil.declinatedY( sgm.x2, sgm.y2, cd, sd );

      initBBox( x0 + xscale * x1, y0 - yscale * y1 );
      updateBBox( x0 + xscale * x2, y0 - yscale * y2 );

      for ( int k=1; k<nrs; ++k ) {
        sgm = sgms.get(k);
        x1 = DrawingUtil.declinatedX( sgm.x1, sgm.y1, cd, sd );
        y1 = DrawingUtil.declinatedY( sgm.x1, sgm.y1, cd, sd );
        x2 = DrawingUtil.declinatedX( sgm.x2, sgm.y2, cd, sd );
        y2 = DrawingUtil.declinatedY( sgm.x2, sgm.y2, cd, sd );
        updateBBox( x0 + xscale * x1, y0 - yscale * y1 );
        updateBBox( x0 + xscale * x2, y0 - yscale * y2 );
      }
    }
  }
}
