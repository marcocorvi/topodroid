/* @file ShpPolyline.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile 2D polyline
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
import com.topodroid.DistoX.DrawingPointLinePath;
import com.topodroid.DistoX.DrawingLinePath;
import com.topodroid.DistoX.DrawingAreaPath;
import com.topodroid.DistoX.DrawingUtil;
import com.topodroid.DistoX.LinePoint;

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

// This class handles lines and areas
public class ShpPolyline extends ShpObject
{
  // int mPathType; 

  // @param path_type   either DRAWING_PATH_LINE or DRAWING_PATH_AREA
  public ShpPolyline( String path, int path_type, List< File > files ) // throws IOException
  {
    super( ( (path_type == DrawingPath.DRAWING_PATH_LINE)? SHP_POLYLINE : SHP_POLYGON ), path, files );
    // mPathType = path_type;
  }

  public void writeLines( List< DrawingPointLinePath > lns, float x0, float y0, float xscale, float yscale ) throws IOException
  {
    writwPointLines( lns, DrawingPath.DRAWING_PATH_LINE, x0, y0, xscale, yscale );
  }

  public void writeAreas( List< DrawingPointLinePath > lns, float x0, float y0, float xscale, float yscale ) throws IOException
  {
    writwPointLines( lns, DrawingPath.DRAWING_PATH_AREA, x0, y0, xscale, yscale );
  }

  private boolean writwPointLines( List< DrawingPointLinePath > lns, int path_type, float x0, float y0, float xscale, float yscale ) throws IOException
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
	  fields[1] = line.getThName( );
          fields[2] = Integer.toString( line.mLevel );
	} else if ( ln.mType == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath area = (DrawingAreaPath)ln;
          fields[0] = "area";
	  fields[1] = area.getThName( );
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
    LinePoint pt = ln.first();
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
    for ( pt = ln.first(); pt != null; pt = pt.mNext ) {
      shpBuffer.putDouble( x0+xscale*(pt.x-DrawingUtil.CENTER_X) );
      shpBuffer.putDouble( y0-yscale*(pt.y-DrawingUtil.CENTER_Y) );
    }
    if ( close == 1 ) {
      pt = ln.first();
      shpBuffer.putDouble( x0+xscale*(pt.x-DrawingUtil.CENTER_X) );
      shpBuffer.putDouble( y0-yscale*(pt.y-DrawingUtil.CENTER_Y) );
    }
  }

  // polyline record length [word]: 4 + (48 + npt * 16)/2
  // 4 for the record header (2 int)
  // @Override 
  protected int getShpRecordLength( int npt ) { return 28 + npt * 8; }

  private void setBoundsLines( List< DrawingPointLinePath > lns, float x0, float y0, float xscale, float yscale )
  {
    int nrs = ( lns != null )? lns.size() : 0;
    if ( nrs > 0 ) {
      DrawingPointLinePath ln = lns.get(0);
      LinePoint pt = ln.first();
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
        for ( pt = ln.first(); pt != null; pt = pt.mNext ) {
          xx = x0 + xscale * (pt.x - DrawingUtil.CENTER_X);
          yy = y0 - yscale * (pt.y - DrawingUtil.CENTER_Y);
          updateBBox( xx, yy );
        }
      }
    }
  }

}
