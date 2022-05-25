/** @file DEMgridParser.java
 *
 * @author marco corvi
 * @date apr 2020
 *
 * @brief ASCII grid DEM parser
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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDFile;

// import java.io.FileReader;
// import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class DEMgridParser extends ParserDEM
{
  private double xll, yll; // lower-left corner of lower-left cell
  private int   cols, rows;
  private boolean flip_vert; // flip_vert: rows are south-to-north
  private boolean flip_horz; // flip_horz: columns are east-to-west

  /** cstr
   * @param filename   file fullpath
   * @param maxsize    ...
   */
  DEMgridParser( InputStreamReader isr, String filename, int maxsize ) // FIXME DEM_URI
  {
    super( isr, filename, maxsize );
  }

  /** read the DEM data
   * @param xwest    X west border
   * @param xeast    X east border
   * @param ysouth   Y south border
   * @param ynorth   Y north border
   * @return true if the DEM in memory is valid
   */
  @Override
  boolean readData( double xwest, double xeast, double ysouth, double ynorth ) 
  {
    // TDLog.v("DEM read data dim " + cols + "x" + rows + " LLcorner " + xll + " " + yll + " cell " + mDim1 + " " + mDim2 + " flip " + flip_vert + " " + flip_horz );
    if ( ! mValid ) return mValid;
    if ( mBr == null ) return false;
    int dj = (flip_vert ? 1 : -1);
    int di = (flip_horz ? -1 : 1);

    int xoff = 0;
    int yoff = 0;
    if ( flip_horz ) {  // row-data [xoff, xoff+mNr1) are [xeast .. xwest]
      double x = xll + mDim1/2 + (cols-1) * mDim1;
      int i = 0;
      for ( ; i < cols && x > xeast; ++i ) x -= mDim1;
      mEast2 = x;   // X-coord of first data
      xoff = i; // col-index of first data 
      mNr1 = 0;     // numver of X-data
      for ( ; i < cols && x >= xwest; ++i ) { x -= mDim1; ++mNr1; }
      mEast1 = x + mDim1; // X-coord of last data
    } else {
      double x = xll + mDim1/2;     // row-data [xoff, xoff+mNr1) are [xwest .. xeast]
      int i = 0;
      for ( ; i < cols && x < xwest; ++i ) x += mDim1;
      mEast1 = x;   // X-coord of first data
      xoff = i; // col-index of first data 
      mNr1 = 0;     // numver of X-data
      for ( ; i < cols && x <= xeast; ++i ) { x += mDim1; ++mNr1; }
      mEast2 = x - mDim1; // X-coord of last data
    }
    if ( mNr1 > mMaxSize ) {
      int d = (mNr1 - mMaxSize)/2;
      xoff += d;
      mNr1 -= 2 * d;
      mEast2 -= d * mDim1;
      mEast1 += d * mDim1;
    }

    if ( ! flip_vert ) { // yll is TOP-LEFT
      double y = yll + mDim2/2 + (rows-1) * mDim2;
      int j = 0;
      for ( ; j < rows && y > ynorth; ++j ) y -= mDim2; 
      mNorth2 = y;
      yoff = j;
      mNr2 = 0;
      for ( ; j < rows && y >= ysouth; ++j ) { y -= mDim2; ++mNr2; }
      mNorth1 = y + mDim2;
    } else {
      double y = yll + mDim2/2;
      int j = 0;
      for ( ; j < rows && y < ysouth; ++j ) y += mDim2; 
      mNorth1 = y;
      yoff = j;
      mNr2 = 0;
      for ( ; j < rows && y <= ynorth; ++j ) { y += mDim2; ++mNr2; }
      mNorth2 = y - mDim2;
    }
    if ( mNr2 > mMaxSize ) {
      int d = (mNr2 - mMaxSize)/2;
      yoff += d;
      mNr2 -= 2 * d;
      mNorth2 -= d * mDim2;
      mNorth1 += d * mDim2;
    }

    // TDLog.v("DEM bounds " + mNr1 + "x" + mNr2 + " offset " + xoff + "-" + yoff + " E " + mEast1 + " " + mEast2 + " N " + mNorth1 + " " + mNorth2 ); 
    if ( mNr1 <= 1 || mNr2 <= 1 ) {
      mValid = false;
      return false;
    }
    mZ = new float[ mNr1 * mNr2 ];

    // FileReader fr = null;
    try {
      // fr = new FileReader( mFilename );
      // fr = TDFile.getFileReader( mFilename );
      // BufferedReader mBr = new BufferedReader( fr );
      String line = null;
      boolean ready = false;
      while ( ! ready ) {
        line = mBr.readLine().trim();
        if ( line.length() == 0 ) continue;
        if ( line.startsWith("#") || line.startsWith("grid") ) continue;
        try { 
          String[] vals = line.replaceAll("\\s+", " ").split(" ");
          Float.parseFloat( vals[0] );
          ready = true;
        } catch ( NumberFormatException e ) { continue; }
      } 
      int j = flip_vert ? 0 : mNr2-1;
      int k = 0; // grid-line number
      for ( ; k < yoff; ++k ) {
        line = mBr.readLine();
      }
      for ( ; k < mNr2; ++ k ) {
        line = line.trim();
        String[] vals = line.replaceAll("\\s+", " ").split(" ");
        // TDLog.v("DEM " + j + " " + (mNorth1+j*mDim2) + ": " + line );
        try {
          if ( flip_horz ) {
            for ( int ii=0; ii<mNr1; ++ii ) mZ[j*mNr1 + mNr1-1-ii] = Float.parseFloat( vals[xoff+ii] );
          } else {
            for ( int ii=0; ii<mNr1; ++ii ) mZ[j*mNr1 + ii] = Float.parseFloat( vals[xoff+ii] );
          }
        } catch ( NumberFormatException e ) { mValid = false; break; }
        if ( k < mNr2-1 ) {
          j += dj;
          line = mBr.readLine();
        }
      }
    } catch ( IOException e1 ) {
      mValid = false;
    } finally {
      // if ( fr != null ) try { fr.close(); } catch ( IOException e ) {}
      tryCloseStream();
    }
    // makeNormal();
    return mValid;
  }

  /** read tthe header info
   * @param filename file fullpath
   * @return true if successful
   */
  @Override
  protected boolean readHeader( String filename ) // FIXME DEM_URI
  {
    // TDLog.v("DEM read header " + filename );
    if ( mBr == null ) return false;
    flip_vert = false;
    flip_horz = false;
    try {
      // FileReader fr = new FileReader( filename );
      // FileReader fr = TDFile.getFileReader( filename );
      // BufferedReader mBr = new BufferedReader( fr );
      String line;
      while ( ( line = mBr.readLine() ) != null ) {
        line = line.trim();
        if ( line.length() == 0 ) continue;    // empty line
        if ( line.startsWith("#" ) ) continue; // comment line
        if ( line.startsWith("grid ") ) {
          // TDLog.v("DEM grid: " + line );
          String[] vals = line.replaceAll("\\s+", " ").split(" ");
          cols = Integer.parseInt( vals[5] ); // ncols
          rows = Integer.parseInt( vals[6] ); // nrows
          xll = Double.parseDouble( vals[1] ); // xcorner
          yll = Double.parseDouble( vals[2] ); // ycorner
          mDim1 = Double.parseDouble( vals[3] ); // cellsize
          mDim2 = Double.parseDouble( vals[4] ); // cellsize
          continue;
        }
        if ( line.startsWith("grid-flip ") ) {
          // TDLog.v("DEM grid-flip: " + line );
          String[] vals = line.replaceAll("\\s+", " ").split(" ");
          if ( vals[1].startsWith("vert") ) {  flip_vert = true; }
          else if ( vals[1].startsWith("horiz") ) { flip_horz = true; }
          else { TDLog.Error("DEM unknown flip " + vals[1] ); }
          continue;
        }
        // try a data line:
        try {
          // TDLog.v("DEM try data: " + line );
          String[] vals = line.replaceAll("\\s+", " ").split(" ");
          Float.parseFloat( vals[0] );
          break;
        } catch ( NumberFormatException e ) { // ok
        }
      }
      // fr.close();
      // TDLog.v("DEM dim " + cols + "x" + rows + " LLcorner " + xll + " " + yll + " cell " + mDim1 + " " + mDim2 + " flip " + flip_vert + " " + flip_horz );
    } catch ( IOException e1 ) { 
      return false;
    } catch ( NumberFormatException e2 ) {
      return false;
    }
    // TDLog.v("DEM cell " + mDim1 + " " + mDim2 + " X " + xll + " Y " + yll + " Nx " + cols + " Ny " + rows );
    return true;
  }

}

