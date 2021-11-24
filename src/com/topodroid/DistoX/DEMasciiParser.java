/** @file DEMasciiParser.java
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
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;

// import java.io.FileReader;
// import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/* ascii DEM have reference at the LL-corner.
 * For TopoGL grid points (center of cells) must add (mDim1/2, mDim2/2) 
 */
class DEMasciiParser extends ParserDEM
{
  private double  xll,  yll;  // Lower-left corner of lower-left cell
  private int     cols, rows; // columns, rows
  private boolean flip_horz;  // whether to flip lines horizontally

  /** cstr
   * @param isr        input reader
   * @param filename   fullpath of the DEM file
   * @param maxsize    ...
   * @param hflip      flip horizontally
   * @param xu         X unit factor
   * @param yu         Y unit factor
   */
  DEMasciiParser( InputStreamReader isr, String filename, int maxsize, boolean hflip, double xu, double yu ) // FIXME DEM_URI
  {
    super( isr, filename, maxsize, xu, yu );
    flip_horz = hflip;
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
    if ( ! mValid ) return mValid;
    if ( mBr == null ) return false;
    // FileReader fr = null;
    try {
      // fr = new FileReader( mFilename );
      // fr = TDFile.getFileReader( mFilename );
      // BufferedReader mBr = new BufferedReader( mIsr );
      // for ( int k=0; k<6; ++k) mBr.readLine(); // header MUST have been read already

      double y = yll + mDim2/2 + mDim2 * (rows-1); // upper-row midpoint
      int k = 0;
      for ( ; k < rows && y > ynorth; ++k ) {
        mBr.readLine();
        y -= mDim2;
      }
      mNorth2 = y;
      // int yoff = k
      
      double x = xll + mDim1/2; // left-column midpoint
      int i = 0;
      for ( ; i < cols && x < xwest; ++i ) x += mDim1;
      mEast1 = x;
      int xoff = i;
      mNr1 = 0;
      for ( ; i < cols && x <= xeast; ++i ) { x += mDim1; ++mNr1; }
      mEast2 = x - mDim1;

      if ( mNr1 > mMaxSize ) {
        int d = (mNr1 - mMaxSize)/2;
        xoff += d;
        mNr1 -= 2 * d;
        mEast1 += d * mDim1;
        mEast2 -= d * mDim1;
      }
      
      mNr2 = 0;
      int kk = k;
      for ( ; kk < rows && y >= ysouth; ++kk ) { y -= mDim2; ++mNr2; }
      mNorth1 = y + mDim2;

      if ( mNr2 > mMaxSize ) {
        int d = (mNr2 - mMaxSize)/2;
        k += d;
        mNr2 -= 2 * d;
        mNorth1 += d * mDim2;
        mNorth2 -= d * mDim2;
      }

      if ( mNr1 <= 1 || mNr2 <= 1 ) {
        mValid = false;
      } else {
        // TDLog.v("DEM size " + mNr1 + "x" + mNr2 + " E " + mEast1 + " " + mEast2 + " N " + mNorth1 + " " + mNorth2 );
        mZ = new float[ mNr1 * mNr2 ];
        int j = mNr2-1; // rotate by 180 degrees the map stored in mZ
        for ( ; k < rows && j >= 0; ++k, --j ) {
          String line = mBr.readLine();
          String[] vals = line.replaceAll("\\s+", " ").split(" ");
          if ( flip_horz ) {
            for ( int ii=0; ii<mNr1; ++ii ) mZ[j*mNr1 + mNr1-1-ii] = Float.parseFloat( vals[xoff+ii] );
          } else {
            for ( int ii=0; ii<mNr1; ++ii ) mZ[j*mNr1 + ii] = Float.parseFloat( vals[xoff+ii] );
          }
        }
      }
    } catch ( IOException e1 ) {
      mValid = false;
    } catch ( NumberFormatException e2 ) {
      mValid = false;
    } finally {
      tryCloseStream();
    }
    // TDLog.v("DEM W " + mEast1 + " E " + mEast2 + " S " + mNorth1 + " N " + mNorth2 );
    // TDLog.v("DEM size " + mNr1 + " " + mNr2 );
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
    if ( mBr == null ) return false;
    try {
      // FileReader fr = new FileReader( filename );
      // FileReader fr = TDFile.getFileReader( filename );
      // BufferedReader mBr = new BufferedReader( mIsr );
      String line = mBr.readLine();
      String[] vals = line.replaceAll("\\s+", " ").split(" ");
      cols = Integer.parseInt( vals[1] ); // ncols
      line = mBr.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      rows = Integer.parseInt( vals[1] ); // nrows
      line = mBr.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      xll  = Double.parseDouble( vals[1] ); // xllcorner
      line = mBr.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      yll  = Double.parseDouble( vals[1] ); // yllcorner
      line = mBr.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      mDim1 = Double.parseDouble( vals[1] ); // cellsize
      mDim2 = mDim1;
      line = mBr.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      nodata = Double.parseDouble( vals[1] ); // nodata.value
      // fr.close();
    } catch ( IOException e1 ) { 
      return false;
    } catch ( NumberFormatException e2 ) {
      return false;
    }
    xll   *= xunit;
    mDim1 *= xunit;
    yll   *= yunit;
    mDim2 *= yunit;
    // TDLog.v("DEM cell " + mDim1 + " X " + xll + " Y " + yll + " Nx " + cols + " Ny " + rows );
    return true;
  }

}

