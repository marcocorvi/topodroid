/** @file DEMhgtParser.java
 *
 * @author marco corvi
 * @date apr 2020
 *
 * @brief DEM parser for HGT files
 *
 * Usage:
 *    ParserDEM DEM = new ParserDEM( filename );
 *    if ( DEM.valid() ) DEM.readData( west, east, south, north );
 *    if ( DEM.valid() ) { // use data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dem;

import com.topodroid.TDX.Cave3DFix;

import java.io.File;
// import java.io.FileInputStream;
// import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import java.util.Locale;

/**
 * Parses SRTM HGT files (16-bit signed integers, Big-Endian).
 * (3-arc-second) data, resulting in 1201x1201 grid. filesize  2884802 B, resolution about 90 m at equator
 * (1-arc-second) data, resulting in 3601x3601 grid. filesize 25934402 B, resolution about 30 m at equator
 * nodata = -32768
 * 1x1 degrees
 * http://edcftp.cr.usgs.gov/pub/data/srtm/Readme.html
 */
public class DEMhgtParser extends ParserDEM 
{
  private int mGridSize = 1201; 

  private Cave3DFix mOrigin; // model origin
  private double mSNradius;
  private double mWEradius;

  private double mSWeast; // east of SW corner [degree]
  private double mSWnorth; // norte of SW corner

  private int mStartRow;
  private int mStartCol;

  // from DEMsurface
  // double mEast1, mNorth1; // (west, south) center of LL-cornel cell [m]
  // double mEast2, mNorth2; // (east, north) center of UR-corner cell
  // float[] mZ;         // Z[ j * mNr1 + i ]
  // int mNr1;           // number of centers in East 
  // int mNr2;           // number of centers in North
  // double mDim1, mDim2; // spacing between grid centers [m]
  //
  // from Cave3DFix
  // Cave3DFix origin = mParser.getOrigin();
  // latToNorth( lat ) = O.y + (lat - O.latitude) * O.getSNradius()
  // lngToEast( lng )  = O.x + (lng - O.longitude) * O.getWEradius();

  public DEMhgtParser( String filename, int max_size, Cave3DFix origin )
  {
    super( null, filename, max_size ); // null InputStreamReader
    mOrigin = origin;
    mSNradius = origin.getSNradius(); // radius * PI / 180
    mWEradius = origin.getWEradius();
  }
  // ... other constructor parameters


  /** check the name of the file
   * @param filename    pathname of the file
   * @return true if the file has the name and the size of a HGT file
    @note called by the DEMParser cstr - return value assigned to mValid
   */
  @Override
  public boolean readHeader( String filename )
  {
    return parseFilename( filename );
  }

  /** @return longitude [degrees]
   * @param e  east [m]
   */
  private double east2lng( double e ) { return mOrigin.longitude + (e - mOrigin.x) / mWEradius; }

  /** @return hgt column index 
   * @param e  east [m]
   */ 
  private double east2hgtD( double e ) { return ( east2lng(e) - mSWeast ) * (mGridSize-1); }
  private int east2hgt( double e ) { return (int) Math.floor( east2hgtD(e) ); }

  /** @return east [m]
   * @param lng  longitude [degree]
   */
  private double lng2east( double lng ) { return  mOrigin.x + ( lng - mOrigin.longitude ) *  mWEradius; }

  private double hgt2east( int i ) { return lng2east( mSWeast + i / (double)(mGridSize-1) ); }

  /** @return latitude [degrees]
   * @param n  north [m]
   */
  private double north2lat( double n ) { return mOrigin.latitude + (n - mOrigin.y) / mSNradius; }

  /** @return hgt row index 
   * @param n  north [m]
   */
  private double north2hgtD( double n ) { return ( north2lat(n) - mSWnorth )  * (mGridSize-1); }
  private int north2hgt( double n ) { return (int) Math.floor( north2hgtD( n ) ); }

  /** @return north [m]
   * @param lat  tatitude [degree]
   */
  private double lat2north( double lat ) { return mOrigin.y + ( lat - mOrigin.latitude ) * mSNradius; }

  private double hgt2north( int j ) { return  lat2north( mSWnorth + j / (double)(mGridSize-1) ); } 

  /**
   * @param west    west bound [m]
   * @param east    east bound [m]
   * @param south
   * @param north
   */
  @Override
  public boolean readData( double west, double east, double south, double north ) 
  {
    if ( ! mValid ) return false;

    // one arc-second (dense HGT file) is about 20x30 m at lat=45 degrees
    // (mOrigin.longitude + (west - mOrigin.x) * mWEradius / (mGridSize-1) - mSWeast) * (mGridSize-1);
    mStartCol = east2hgt( west );
    int endCol   = east2hgt( east );
        
    // HGT rows are 0 at the North (top)
    mStartRow = north2hgt( south );
    int endRow   = north2hgt( north );

    // Clamp to grid boundaries
    if ( mStartCol < 0 ) {
      mStartCol = 0;
      mEast1   = hgt2east( mStartCol );
    }
    if ( endCol > mGridSize - 1 ) {
      endCol = mGridSize - 1;
      mEast2 = hgt2east( endCol );
    }
    if ( mStartRow < 0 ) {
      mStartRow = 0;
      mNorth1 = hgt2north( mStartRow );
    }
    if ( endRow > mGridSize - 1 ) {
      endRow = mGridSize - 1;
      mNorth2 = hgt2north( endRow );
    }

    int n_rows = endRow - mStartRow + 1; // nr of cells in north direction  - number of rows
    int n_cols = endCol - mStartCol + 1; // nr of cells in east direction - size of the row
    if ( n_cols < 2 || n_rows < 2 ) return false;
    // short dataBuffer = new short[n_cols * n_rows];
    mZ = new float[n_cols * n_rows];

    try ( RandomAccessFile raf = new RandomAccessFile( mFilename, "r" ) ) {
      byte[] rowBuffer = new byte[n_cols * 2];
      for (int r = 0; r < n_rows; r++) {
        int offset = ((mStartRow + r) * mGridSize + mStartCol) * 2;
        raf.seek(offset);
        raf.readFully(rowBuffer);
            
        ByteBuffer bb = ByteBuffer.wrap(rowBuffer).order(ByteOrder.BIG_ENDIAN);
        for (int c = 0; c < n_cols; c++) {
          // dataBuffer[r * n_cols + c] = bb.getShort();
          mZ[r * n_cols + c] = (float)( bb.getShort() );
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    mNr1 = n_cols; // horizontal number of calls
    mNr2 = n_rows; // vertical number of cells

    mValid = true;
    return true;
  }

  @Override
  public float computeZ( double e, double n )
  {
    if ( e < mEast1 || n < mNorth1 || e > mEast2 || n > mNorth2 ) return -9999.0f;
    double i = east2hgtD( e )  - mStartCol; // position in the data array Z[]
    double j = north2hgtD( n ) - mStartRow;

    int i1 = (int)(i);
    int j1 = (int)(j);
    int i2 = i1 + 1;
    int j2 = j1 + 1;
    if ( i1 < 0 || j1 < 0 || i2 > mNr1 || j2 > mNr2 ) return -9999.0f;

    double dx2 = i - i1; // (e - ( mEast1 + i1 * mDim1 ))/mDim1;
    double dx1 = 1.0 - dx2;
    double dy2 = j - j1; // (n - ( mNorth1 + j1 * mDim2 ))/mDim2;
    double dy1 = 1.0 - dy2;
    return (float)( ( j2 < mNr2 ) ?
        ( (i2 < mNr1 )? mZ[j1*mNr1+i1] * dx1 + mZ[j1*mNr1+i2] * dx2 : mZ[j1*mNr1+i1] ) * dy1 + ( (i2 < mNr1 )? mZ[j2*mNr1+i1] * dx1 + mZ[j2*mNr1+i2] * dx2 : mZ[j2*mNr1+i1] ) * dy2
      : ( (i2 < mNr1 )? mZ[j1*mNr1+i1] * dx1 + mZ[j1*mNr1+i2] * dx2 : mZ[j1*mNr1+i1] ) );
    // TDLog.v("Surface i " + i1 + " j " + j2 + " z " + ret );
  }



  /** HGT file covers 1 x 1 degrees tile
   * Extracts the SW corner latitude and longitude from the HGT filename.
   * HGT filename have a fixed syntax: {N,S} 2-digits {W,E} 3-digits ".hgt"
   * N45W122.hgt means thath teh SW corner is hgtLat = 45.0, hgtLon = -122.0
   * Nevertheless the name parser expects {N,S} digits {W,E} digits ".hgt"
   * 
   * The file contains 1201x1201 values or 3601x3601 values
   * The height values are shorts (2 bytes)
   */
  private boolean parseFilename( String filename )
  {
    long filesize =  new java.io.File( filename ).length();

    if ( filesize == 2884802 ) {
      mGridSize = 1201;
    } else if ( filesize == 25934402 ) {
      mGridSize = 3601;
    } else {
      return false;
    }
    xunit = 1.0f / mGridSize; // fraction of degree
    yunit = 1.0f / mGridSize;

    filename = filename.toUpperCase( Locale.US );

    int len = filename.length();
    int pos = filename.lastIndexOf('/') + 1;
    if ( pos >= len ) return false;
    char ch = filename.charAt(pos);
    int latSign = ( ch == 'S' )? -1 : ( ch == 'N' )? +1 : 0;
    if ( latSign == 0 ) return false;
    pos ++;
    int qos = pos;
    while ( qos < len && Character.isDigit( filename.charAt(qos) ) ) ++qos;
    ch = filename.charAt(qos);
    int lngSign = ( ch == 'W' )? -1 : ( ch == 'E' )? +1 : 0;
    if ( lngSign == 0 ) return false;
    try {
      mSWnorth = latSign * Integer.parseInt( filename.substring( pos, qos ) );
      pos = qos + 1;
      qos = filename.lastIndexOf('.');
      if ( qos < pos ) {
        mSWeast = lngSign * Integer.parseInt( filename.substring( pos ) );
      } else {
        mSWeast = lngSign * Integer.parseInt( filename.substring( pos, qos ) );
      }
    } catch ( Exception e ) { // Handle parsing error
      return false;
    }
    mEast1  = mSWeast;
    mNorth1 = mSWnorth;
    mNorth2 = mNorth1 + 1;
    mEast2  = mEast1  + 1;

    // convert bounds to meters
    mEast1  = mOrigin.x + ( mEast1 - mOrigin.longitude) * (mGridSize-1) / mWEradius;
    mEast2  = mOrigin.x + ( mEast2 - mOrigin.longitude) * (mGridSize-1) / mWEradius;
    mNorth1 = mOrigin.y + ( mNorth1 - mOrigin.latitude) * (mGridSize-1) / mSNradius;
    mNorth2 = mOrigin.y + ( mNorth2 - mOrigin.latitude) * (mGridSize-1) / mSNradius;
    
    // it might be that the area is wide and the difference od width (horizontal side) between cells at high
    // and low latitudes must be taken into account
    mDim1 = xunit * mWEradius; // horizontal size of a cell (at the latitude of the origin)
    mDim2 = yunit * mSNradius; // vertical size of a cell

    return true;
  }

}
