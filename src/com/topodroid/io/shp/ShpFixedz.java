/* @file ShpFixedz.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing: shapefile 3D fixed point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.shp;

// import com.topodroid.utils.TDLog;
import com.topodroid.num.NumStation;
import com.topodroid.DistoX.FixedInfo;
import com.topodroid.DistoX.FixedStation;

// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;   

// import java.nio.ByteBuffer;
// import java.nio.MappedByteBuffer;
import java.nio.ByteOrder;   
// import java.nio.channels.FileChannel;

import java.util.List;

public class ShpFixedz extends ShpObject
{
  static final private String[] flag_string = { " ", "F", "P" }; 
 
  public ShpFixedz( String subdir, String path, List< String > files ) // throws IOException
  {
    super( SHP_POINTZ, subdir, path, files );
  }

  // write headers for NAMEZ
  public boolean writeFixeds( List< FixedStation > pts ) throws IOException
  {
    int n_pts = (pts != null)? pts.size() : 0;
    if ( n_pts == 0 ) return false;

    int n_fld = 3;
    String[] fields = new String[ n_fld ];
    fields[0] = "name";
    fields[1] = "comment";
    fields[2] = "source"; // 0, 1, 2, 3
    byte[]   ftypes = { BYTEC, BYTEC, BYTEC };
    int[]    flens  = { SIZE_NAME, SIZE_TEXT, SIZE_FLAG };

    int shpRecLen = getShpRecordLength( );
    int shxRecLen = getShxRecordLength( );
    int dbfRecLen = 1; // Bytes
    for (int k=0; k<n_fld; ++k ) dbfRecLen += flens[k]; 

    int shpLength = 50 + n_pts * shpRecLen; // [16-bit words]
    int shxLength = 50 + n_pts * shxRecLen;
    int dbfLength = 33 + n_fld * 32 + n_pts * dbfRecLen; // [Bytes]

    setBoundsStations( pts );
    // TDLog.v( "NAMEZ " + pts.size() + " len " + shpLength + " / " + shxLength + " / " + dbfLength );
    // TDLog.v( "bbox X " + xmin + " " + xmax );

    open();
    resetChannels( 2*shpLength+8, 2*shxLength+8, dbfLength );

    shpBuffer = writeShapeHeader( shpBuffer, SHP_POINTZ, shpLength );
    shxBuffer = writeShapeHeader( shxBuffer, SHP_POINTZ, shxLength );
    writeDBaseHeader( n_pts, dbfRecLen, n_fld, fields, ftypes, flens );
    // TDLog.v( "NAMEZ done headers");

    int cnt = 0;
    for ( FixedStation st : pts ) {
      NumStation pt = st.mNumStation;
      FixedInfo  cs = st.mFixed;
      int offset = 50 + cnt * shpRecLen; 
      writeShpRecordHeader( cnt, shpRecLen );
      shpBuffer.order(ByteOrder.LITTLE_ENDIAN);   
      shpBuffer.putInt( SHP_POINTZ );
      // TDLog.v( "NAMEZ " + cnt + ": " + pt.e + " " + pt.s + " " + pt.v + " offset " + offset );
      shpBuffer.putDouble( pt.e );
      shpBuffer.putDouble( pt.s );
      shpBuffer.putDouble( pt.v );
      shpBuffer.putDouble( 0.0 );

      writeShxRecord( offset, shpRecLen );
      fields[0] = pt.name;
      fields[1] = cs.getComment();
      fields[2] = cs.getSource();
      writeDBaseRecord( n_fld, fields, flens );
      ++cnt;
    }
    // TDLog.v( "NAMEZ done records");
    close();
    return true;
  }

  // record length [word]: 4 + 36/2
  @Override protected int getShpRecordLength( ) { return 22; }
    
  // Utility: set the bounding box of the set of geometries
  private void setBoundsStations( List< FixedStation > pts ) 
  {
    if ( pts.size() == 0 ) {
      xmin = xmax = ymin = ymax = zmin = zmax = 0.0;
      return;
    }
    NumStation pt = pts.get(0).mNumStation;
    initBBox( pt.e, pt.s, pt.v );
    for ( int k=pts.size() - 1; k>0; --k ) {
      pt = pts.get(k).mNumStation;
      updateBBox( pt.e, pt.s, pt.v );
    }
  }
}

