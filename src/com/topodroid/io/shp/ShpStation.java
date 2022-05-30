/* @file ShpStation.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile 2D station
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
// import com.topodroid.TDX.DrawingPath;
// import com.topodroid.TDX.DrawingPointPath;
// import com.topodroid.TDX.DrawingPointLinePath;
// import com.topodroid.TDX.DrawingLinePath;
// import com.topodroid.TDX.DrawingAreaPath;
import com.topodroid.TDX.DrawingStationName;
import com.topodroid.TDX.DrawingUtil;

// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;   

// import java.nio.ByteBuffer;
// import java.nio.MappedByteBuffer;
import java.nio.ByteOrder;   
// import java.nio.channels.FileChannel;

import java.util.List;

public class ShpStation extends ShpObject
{
  public ShpStation( String subdir, String path, List< String > files ) // throws IOException
  {
    super( SHP_POINT, subdir, path, files );
  }

  // write headers for POINT
  public boolean writeStations( List< DrawingStationName > pts, double x0, double y0, double xscale, double yscale, double cd, double sd ) throws IOException
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

    setBoundsPoints( pts, x0, y0, xscale, yscale, cd, sd );
    // TDLog.v( "POINT station " + pts.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // TDLog.v( "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength+8, 2*shxLength+8, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, SHP_POINT, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, SHP_POINT, shxLength );
    writeDBaseHeader( n_pts, dbfRecLen, n_fld, fields, ftypes, flens );
    // TDLog.v( "POINTZ done headers");

    int cnt = 0;
    for ( DrawingStationName st : pts ) {
      int offset = 50 + cnt * shpRecLen; 
      writeShpRecordHeader( cnt, shpRecLen );
      shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
      shpBuffer.putInt( SHP_POINT );
      // NumStation nst = st.getNumStation();
      double x = DrawingUtil.declinatedX( st.cx, st.cy, cd, sd );
      double y = DrawingUtil.declinatedY( st.cx, st.cy, cd, sd );
      shpBuffer.putDouble( x0 + xscale * x );
      shpBuffer.putDouble( y0 - yscale * y );

      writeShxRecord( offset, shpRecLen );
      fields[0] = st.getName();
      writeDBaseRecord( n_fld, fields, flens );
      ++cnt;
    }
    // TDLog.v( "POINT station done records");
    close();
    return true;
  }

  // record length [words]: 4 + 20/2
  @Override protected int getShpRecordLength( ) { return 14; }
    
  // Utility: set the bounding box of the set of geometries
  private void setBoundsPoints( List< DrawingStationName > pts, double x0, double y0, double xscale, double yscale, double cd, double sd ) 
  {
    if ( pts.size() == 0 ) {
      xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
      return;
    }
    DrawingStationName st = pts.get(0);
    double x = DrawingUtil.declinatedX( st.cx, st.cy, cd, sd );
    double y = DrawingUtil.declinatedY( st.cx, st.cy, cd, sd );
    initBBox( x0 + xscale * x, y0 - yscale * y );
    for ( int k=pts.size() - 1; k>0; --k ) {
      st = pts.get(k);
      x = DrawingUtil.declinatedX( st.cx, st.cy, cd, sd );
      y = DrawingUtil.declinatedY( st.cx, st.cy, cd, sd );
      updateBBox( x0 + xscale * x, y0 - yscale * y );
    }
  }
}
