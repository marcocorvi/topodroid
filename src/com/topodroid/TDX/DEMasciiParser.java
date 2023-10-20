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
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDFile;

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
   * @param xu         X unit factor (either 1 for m, or R*PI/180 for dec-degrees)
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
    // TDLog.v("DEM ascii X " + xwest + " " + xeast + " Y " + ysouth + " " + ynorth );
    if ( ! mValid ) {
      TDLog.Error("DEM ascii parser read data. Not valid" );
      return mValid;
    }
    if ( mBr == null ) {
      TDLog.Error("DEM ascii parser read data. Null buffered reader" );
      return false;
    }
    // FileReader fr = null;
    try {
      // fr = new FileReader( mFilename );
      // fr = TDFile.getFileReader( mFilename );
      // BufferedReader mBr = new BufferedReader( mIsr );
      // for ( int k=0; k<6; ++k) mBr.readLine(); // header MUST have been read already

      double y = yll + mDim2/2 + mDim2 * (rows-1); // upper-row midpoint - mDim2 = Y-cell-size
      // TDLog.v("DEM upper-row midpoint " + y + " " + ynorth + " rows " + rows );
      int k = 0;
      for ( ; k < rows && y > ynorth; ++k ) {
        mBr.readLine();
        y -= mDim2;
      }
      mNorth2 = y;
      // int yoff = k
      // TDLog.v("DEM north " + mNorth2 + " " + ynorth );
      
      double x = xll + mDim1/2; // left-column midpoint
      int i = 0;
      for ( ; i < cols && x < xwest; ++i ) x += mDim1;
      mEast1 = x;
      int xoff = i;
      mNr1 = 0;
      for ( ; i < cols && x <= xeast; ++i ) { x += mDim1; ++mNr1; }
      mEast2 = x - mDim1;
      // TDLog.v("DEM east " + mEast1 + " " + mEast2 );

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
        TDLog.Error("DEM size " + mNr1 + "x" + mNr2 + " invalid ");
        mValid = false;
      } else {
        // TDLog.v("DEM size " + mNr1 + "x" + mNr2 + " E " + mEast1 + " " + mEast2 + " N " + mNorth1 + " " + mNorth2 );
        mZ = new float[ mNr1 * mNr2 ];
        int j = mNr2-1; // rotate by 180 degrees the map stored in mZ
        for ( ; k < rows && j >= 0; ++k, --j ) {
          String line = mBr.readLine();
          String[] vals = TDString.splitOnSpaces( line );
          if ( vals.length < xoff + mNr1 ) throw new DemException("invalid row");
          if ( flip_horz ) {
            for ( int ii=0; ii<mNr1; ++ii ) mZ[j*mNr1 + mNr1-1-ii] = Float.parseFloat( vals[xoff+ii] );
          } else {
            for ( int ii=0; ii<mNr1; ++ii ) mZ[j*mNr1 + ii] = Float.parseFloat( vals[xoff+ii] );
          }
        }
      }
    } catch ( IOException e1 ) {
      TDLog.Error("DEM ascii IO error " + e1.getMessage() );
      mValid = false;
    } catch ( NumberFormatException e2 ) {
      TDLog.Error("DEM ascii number error " + e2.getMessage() );
      mValid = false;
    } catch ( DemException e3 ) {
      TDLog.Error("DEM ascii error " + e3.getMessage() );
      mValid = false;
    } finally {
      tryCloseStream();
    }
    // TDLog.v("DEM W " + mEast1 + " E " + mEast2 + " S " + mNorth1 + " N " + mNorth2 );
    // TDLog.v("DEM size " + mNr1 + " " + mNr2 );
    // makeNormal();
    return mValid;
  }

  /** read the header info
   * @param filename file fullpath - not used
   * @return true if successful
   */
  @Override
  protected boolean readHeader( String filename ) // FIXME DEM_URI
  {
    // boolean xll_degrees  = false;
    // boolean yll_degrees  = false;
    // boolean xdim_degrees = false;
    // boolean ydim_degrees = false;

    // TDLog.v("DEM ascii parser read header " + filename );
    if ( mBr == null ) return false;
    try {
      // FileReader fr = new FileReader( filename );
      // FileReader fr = TDFile.getFileReader( filename );
      // BufferedReader mBr = new BufferedReader( mIsr );
      String line = mBr.readLine();
      String[] vals = TDString.splitOnSpaces( line );
      if ( vals.length <= 1 ) return false;
      cols = Integer.parseInt( vals[1] ); // number cols

      line = mBr.readLine();
      vals = TDString.splitOnSpaces( line );
      if ( vals.length <= 1 ) return false;
      rows = Integer.parseInt( vals[1] ); // number rows

      line = mBr.readLine();
      vals = TDString.splitOnSpaces( line );
      if ( vals.length <= 1 ) return false;
      // int pos = vals[1].indexOf('.');
      // if ( pos > 0 && pos + 4 > vals[1].length() ) xll_degrees = true;
      xll  = Double.parseDouble( vals[1] ); // xll corner

      line = mBr.readLine();
      vals = TDString.splitOnSpaces( line );
      if ( vals.length <= 1 ) return false;
      // pos = vals[1].indexOf('.');
      // if ( pos > 0 && pos + 4 > vals[1].length() ) yll_degrees = true;
      yll  = Double.parseDouble( vals[1] ); // yll corner

      line = mBr.readLine();
      vals = TDString.splitOnSpaces( line );
      if ( vals.length <= 1 ) return false;
      // pos = vals[1].indexOf('.');
      // if ( pos > 0 && pos + 4 > vals[1].length() ) xdim_degrees = ydim_degrees = true;
      mDim2 = mDim1 = Double.parseDouble( vals[1] ); // cell-size

      line = mBr.readLine();
      vals = TDString.splitOnSpaces( line );
      if ( vals.length <= 1 ) return false;
      nodata = Double.parseDouble( vals[1] ); // nodata.value
      // fr.close();
    } catch ( IOException e1 ) { 
      return false;
    } catch ( NumberFormatException e2 ) {
      return false;
    }
    /* if ( xll_degrees  ) */ xll   *= xunit;
    /* if ( xdim_degrees ) */ mDim1 *= xunit;
    /* if ( yll_degrees  ) */ yll   *= yunit;
    /* if ( ydim_degrees ) */ mDim2 *= yunit;
    // TDLog.v("DEM cell " + mDim1 + " X " + xll + " Y " + yll + " Nx " + cols + " Ny " + rows );
    return true;
  }

}

