/* @file ShpLink.java
 *
 * @author marco corvi
 * @date feb 2022
 *
 * @brief TopoDroid drawing: shapefile link from ai 2D point to another 2D item
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
import com.topodroid.TDX.DrawingPointPath;
import com.topodroid.TDX.DrawingUtil;
import com.topodroid.TDX.IDrawingLink;

// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;   

// import java.nio.ByteBuffer;
// import java.nio.MappedByteBuffer;
import java.nio.ByteOrder;   
// import java.nio.channels.FileChannel;

import java.util.List;

public class ShpLink extends ShpObject
{
  /** cstr
   * @param subdir ...
   * @param path   filename
   * @param files  list of files to fill for the zip-compresssion
   */
  public ShpLink( String subdir, String path, List< String > files ) // throws IOException
  {
    super( SHP_POLYLINE, subdir, path, files );
  }

  /** write a set of links
   * @param links    list of links
   * @param x0      X offset
   * @param y0      Y offset
   * @param xscale  X scale factor
   * @param yscale  Y scale factor
   * @param cd      cosine declination angle
   * @param sd      sine declination angle
   */
  public boolean writeLinks( List< Link > links, double x0, double y0, double xscale, double yscale, double cd, double sd ) throws IOException
  {
    int n_pts = (links != null)? links.size() : 0;
    // TDLog.v( "SHP write points " + n_pts );
    if ( n_pts == 0 ) return false;

    int n_fld = 3;
    String[] fields = new String[ n_fld ];
    fields[0] = "scale";
    fields[1] = "levels";
    fields[2] = "scrap";
    byte[]   ftypes = { BYTEN, BYTEC, BYTEC };
    int[]    flens  = { SIZE_SCALE, SIZE_LEVELS, SIZE_SCRAP };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + n_pts * shpRecLen; // [16-bit words]
    int shxLength = 50 + n_pts * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + n_pts * dbfRecLen; // [Bytes]

    setBoundsLinks( links, x0, y0, xscale, yscale, cd, sd );
    // TDLog.v( "POINT " + pts.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // TDLog.v( "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength+8, 2*shxLength+8, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, SHP_POLYLINE, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, SHP_POLYLINE, shxLength );
    writeDBaseHeader( n_pts, dbfRecLen, n_fld, fields, ftypes, flens );
    // TDLog.v( "POINT done headers - nr " + pts.size() );

    int cnt = 0;
    int offset = 50;
    for ( Link lnk : links ) {
      fields[0] = new String( blankPadded( lnk.pt.getScale(), SIZE_SCALE ) );
      fields[1] = Integer.toString( lnk.level() );
      fields[2] = Integer.toString( lnk.scrap() ); 
      if ( fields[1] == null ) fields[1] = "";
      if ( fields[2] == null ) fields[2] = "";

      // TDLog.v( "LINK " + cnt + ": " + pt.cx + " " + pt.cy + " cd " + cd + " sd " + sd + " scale " + xscale + " " + yscale );
      writeLink( cnt, shpRecLen, lnk, x0, y0, xscale, yscale, cd, sd );
      writeShxRecord( offset, shpRecLen );
      writeDBaseRecord( n_fld, fields, flens );
      ++cnt;
      offset += shpRecLen; 
    }
    // TDLog.v( "LINK done records");
    close();
    return true;
  }

  /** write a link
   * @param cnt     counter
   * @param len     record length
   * @param link    link to write
   * @param x0      X offset
   * @param y0      Y offset
   * @param xscale  X scale factor
   * @param yscale  Y scale factor
   * @param cd      cosine declination angle
   * @param sd      sine declination angle
   */
  private void writeLink( int cnt, int len, Link link, double x0, double y0, double xscale, double yscale, double cd, double sd )
  {
    DrawingPointPath pt = link.pt;
    double x1 = DrawingUtil.declinatedX( pt.cx, pt.cy, cd, sd );
    double y1 = DrawingUtil.declinatedY( pt.cx, pt.cy, cd, sd );
    double x2 = DrawingUtil.declinatedX( link.x, link.y, cd, sd );
    double y2 = DrawingUtil.declinatedY( link.x, link.y, cd, sd );

    double xmin, ymin, xmax, ymax;
    {
      xmin = xmax =  x1; //  lnk.x1;
      ymin = ymax = -y1; // -lnk.y1;
      if (  x2 < xmin ) { xmin =  x2; } else if (  x2 > xmax ) { xmax =  x2; }
      if ( -y2 < ymin ) { ymin = -y2; } else if ( -y2 > ymax ) { ymax = -y2; }
    }
    xmin = x0 + xscale * xmin;
    xmax = x0 + xscale * xmax;
    ymin = y0 + yscale * ymin;
    ymax = y0 + yscale * ymax;

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
    // TDLog.v( "SHOT record [1] " + (x0 + xscale*x1) + " " + (y0 - yscale*y1 ) );

    shpBuffer.putDouble( x0 + xscale*x2 );
    shpBuffer.putDouble( y0 - yscale*y2 );
    // TDLog.v( "SHOT record [2] " + (x0 + xscale*x2) + " " + (y0 - yscale*y2 ) );
  }

  /** @return segment record length [word]: 4 + (48 + npt * 16)/2   [npt = 2]
   * 4 for the record header (2 int)
   *     = 4 + 24 + 2 * 8
   */
  @Override protected int getShpRecordLength( ) { return 28 + 2 * 8; }
    
  /** compute the bounds - Utility: set the bounding box of the set of geometries
   * @param links   list of links
   * @param x0      X offset
   * @param y0      Y offset
   * @param xscale  X scale factor
   * @param yscale  Y scale factor
   * @param cd      cosine declination angle
   * @param sd      sine declination angle
   */
  private void setBoundsLinks( List< Link > links, double x0, double y0, double xscale, double yscale, double cd, double sd )
  {
    int nrs = ( links != null )? links.size() : 0;
    if ( nrs > 0 ) {
      Link lnk = links.get(0);
      double x1 = DrawingUtil.declinatedX( lnk.pt.cx, lnk.pt.cy, cd, sd );
      double y1 = DrawingUtil.declinatedY( lnk.pt.cx, lnk.pt.cy, cd, sd );
      double x2 = DrawingUtil.declinatedX( lnk.x, lnk.y, cd, sd );
      double y2 = DrawingUtil.declinatedY( lnk.x, lnk.y, cd, sd );

      initBBox( x0 + xscale * x1, y0 - yscale * y1 );
      updateBBox( x0 + xscale * x2, y0 - yscale * y2 );

      for ( int k=1; k<nrs; ++k ) {
        lnk = links.get(k);
        x1 = DrawingUtil.declinatedX( lnk.pt.cx, lnk.pt.cy, cd, sd );
        y1 = DrawingUtil.declinatedY( lnk.pt.cx, lnk.pt.cy, cd, sd );
        x2 = DrawingUtil.declinatedX( lnk.x, lnk.y, cd, sd );
        y2 = DrawingUtil.declinatedY( lnk.x, lnk.y, cd, sd );
        updateBBox( x0 + xscale * x1, y0 - yscale * y1 );
        updateBBox( x0 + xscale * x2, y0 - yscale * y2 );
      }
    }
  }
}
