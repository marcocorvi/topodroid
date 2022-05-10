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
package com.topodroid.io.shp;

// import com.topodroid.utils.TDLog;
// import com.topodroid.num.NumStation;
// import com.topodroid.num.NumShot;
// import com.topodroid.num.NumSplay;
import com.topodroid.TDX.DrawingPath;
import com.topodroid.TDX.DrawingPointLinePath;
import com.topodroid.TDX.DrawingLinePath;
import com.topodroid.TDX.DrawingAreaPath;
import com.topodroid.TDX.DrawingUtil;
import com.topodroid.TDX.LinePoint;

// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;   

// import java.nio.ByteBuffer;
// import java.nio.MappedByteBuffer;
import java.nio.ByteOrder;   
// import java.nio.channels.FileChannel;

import java.util.List;

// This class handles lines and areas
public class ShpPolyline extends ShpObject
{
  // int mPathType; 

  // @param path_type   either DRAWING_PATH_LINE or DRAWING_PATH_AREA
  public ShpPolyline( String subdir, String path, int path_type, List< String > files ) // throws IOException
  {
    super( ( (path_type == DrawingPath.DRAWING_PATH_LINE)? SHP_POLYLINE : SHP_POLYGON ), subdir, path, files );
    // mPathType = path_type;
  }

  public void writeLines( List< DrawingPointLinePath > lns, double x0, double y0, double xscale, double yscale, double cd, double sd ) throws IOException
  {
    writwPointLines( lns, DrawingPath.DRAWING_PATH_LINE, x0, y0, xscale, yscale, cd, sd );
  }

  public void writeAreas( List< DrawingPointLinePath > lns, double x0, double y0, double xscale, double yscale, double cd, double sd ) throws IOException
  {
    writwPointLines( lns, DrawingPath.DRAWING_PATH_AREA, x0, y0, xscale, yscale, cd, sd );
  }

  private boolean writwPointLines( List< DrawingPointLinePath > lns, int path_type, double x0, double y0, double xscale, double yscale, double cd, double sd ) throws IOException
  {
    int nrs = ( lns != null )? lns.size() : 0;
    if ( nrs == 0 ) return false;

    int n_fld = 4; // type from to flag comment
    String[] fields = new String[ n_fld ];
    fields[0] = "type";
    fields[1] = "name";
    fields[2] = "levels";
    fields[3] = "scrap";
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC, BYTEC }; // use only strings
    int[]    flens  = { SIZE_TYPE, SIZE_NAME, SIZE_LEVELS, SIZE_SCRAP };

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

    setBoundsLines( lns, x0, y0, xscale, yscale, cd, sd );
    // TDLog.v( "POLYLINEZ shots " + lns.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // TDLog.v( "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength, 2*shxLength, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, geomType, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, geomType, shxLength );
    writeDBaseHeader( nrs, dbfRecLen, n_fld, fields, ftypes, flens );
    // TDLog.v( "shots done headers" );

    int cnt = 1;
    int offset = 50;
    if ( lns != null && nrs > 0 ) {
      for ( DrawingPointLinePath ln : lns ) {
	if ( ln.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath line = (DrawingLinePath)ln;
          fields[0] = "line";
	  fields[1] = line.getThName( );
          fields[2] = Integer.toString( line.mLevel );
          fields[3] = Integer.toString( line.mScrap );
	} else if ( ln.mType == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath area = (DrawingAreaPath)ln;
          fields[0] = "area";
	  fields[1] = area.getThName( );
          fields[2] = Integer.toString( area.mLevel );
          fields[3] = Integer.toString( area.mScrap );
	} else {
	  fields[0] = "undef";
          fields[1] = "undef";
          fields[2] = "0";
          fields[3] = "0";
	}
        int close = ( ln.mType == DrawingPath.DRAWING_PATH_AREA || ln.isClosed() )? 1 : 0;
	int shp_len = getShpRecordLength( ln.size() + close );

        writeShpRecord( cnt, shp_len, ln, close, x0, y0, xscale, yscale, cd, sd );
        writeShxRecord( offset, shp_len );
        writeDBaseRecord( n_fld, fields, flens );

        offset += shp_len;
        ++cnt;
      }
    }
    // TDLog.v( "shots done records" );
    close();
    return true;
  }

  private void writeShpRecord( int cnt, int len, DrawingPointLinePath ln, int close, double x0, double y0, double xscale, double yscale, double cd, double sd )
  {
    double xmin, ymin, xmax, ymax;
    LinePoint pt = ln.first();
    double x = DrawingUtil.declinatedX( pt.x, pt.y, cd, sd );
    double y = DrawingUtil.declinatedY( pt.x, pt.y, cd, sd );
    {
      xmin = xmax =  x;
      ymin = ymax = -y;
      for ( pt = pt.mNext; pt != null; pt = pt.mNext ) {
        x = DrawingUtil.declinatedX( pt.x, pt.y, cd, sd );
        y = DrawingUtil.declinatedY( pt.x, pt.y, cd, sd );
        if (  x < xmin ) { xmin =  x; } else if (  x > xmax ) { xmax =  x; }
        if ( -y < ymin ) { ymin = -y; } else if ( -y > ymax ) { ymax = -y; }
      }
    }
    xmin = x0 + xscale*xmin;
    ymin = y0 + yscale*ymin;
    xmax = x0 + xscale*xmax;
    ymax = y0 + yscale*ymax;

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
      x = DrawingUtil.declinatedX( pt.x, pt.y, cd, sd );
      y = DrawingUtil.declinatedY( pt.x, pt.y, cd, sd );
      shpBuffer.putDouble( x0 + xscale * x );
      shpBuffer.putDouble( y0 - yscale * y );
    }
    if ( close == 1 ) {
      pt = ln.first();
      x = DrawingUtil.declinatedX( pt.x, pt.y, cd, sd );
      y = DrawingUtil.declinatedY( pt.x, pt.y, cd, sd );
      shpBuffer.putDouble( x0 + xscale * x );
      shpBuffer.putDouble( y0 - yscale * y );
    }
  }

  // polyline record length [word]: 4 + (48 + npt * 16)/2
  // 4 for the record header (2 int)
  // @Override 
  protected int getShpRecordLength( int npt ) { return 28 + npt * 8; }

  private void setBoundsLines( List< DrawingPointLinePath > lns, double x0, double y0, double xscale, double yscale, double cd, double sd )
  {
    int nrs = ( lns != null )? lns.size() : 0;
    if ( nrs > 0 ) {
      DrawingPointLinePath ln = lns.get(0);
      LinePoint pt = ln.first();
      double x = DrawingUtil.declinatedX( pt.x, pt.y, cd, sd );
      double y = DrawingUtil.declinatedY( pt.x, pt.y, cd, sd );
      initBBox( x0 + xscale * x, y0 - yscale * y );
      for ( pt = pt.mNext; pt != null; pt = pt.mNext ) {
        x = DrawingUtil.declinatedX( pt.x, pt.y, cd, sd );
        y = DrawingUtil.declinatedY( pt.x, pt.y, cd, sd );
        updateBBox( x0 + xscale * x, y0 - yscale * y );
      }
      for ( int k=1; k<nrs; ++k ) {
        ln = lns.get(k);
        for ( pt = ln.first(); pt != null; pt = pt.mNext ) {
          x = DrawingUtil.declinatedX( pt.x, pt.y, cd, sd );
          y = DrawingUtil.declinatedY( pt.x, pt.y, cd, sd );
          updateBBox( x0 + xscale * x, y0 - yscale * y );
        }
      }
    }
  }

}
