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
package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/* ascii DEM have reference at the LL-corner.
 * For TopoGL grid points (center of cells) must add (mDim1/2, mDim2/2) 
 */
class DEMasciiParser extends ParserDEM
{
  private double  xll,  yll; // Lower-left corner of lower-left cell
  private int     cols, rows;
  private boolean flip_horz; // whether to flip lines horizontally

  DEMasciiParser( String filename, int maxsize, boolean hflip, double xu, double yu )
  {
    super( filename, maxsize, xu, yu );
    flip_horz = hflip;
  }

  @Override
  boolean readData( double xwest, double xeast, double ysouth, double ynorth )
  {
    if ( ! mValid ) return mValid;
    FileReader fr = null;
    try {
      fr = new FileReader( mFilename );
      BufferedReader br = new BufferedReader( fr );
      for ( int k=0; k<6; ++k) br.readLine();

      double y = yll + mDim2/2 + mDim2 * (rows-1); // upper-row midpoint
      int k = 0;
      for ( ; k < rows && y > ynorth; ++k ) {
        br.readLine();
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
          String line = br.readLine();
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
      if ( fr != null ) try { fr.close(); } catch ( IOException e ) {}
    }
    // TDLog.v("DEM W " + mEast1 + " E " + mEast2 + " S " + mNorth1 + " N " + mNorth2 );
    // TDLog.v("DEM size " + mNr1 + " " + mNr2 );
    // makeNormal();
    return mValid;
  }

  @Override
  protected boolean readHeader( String filename )
  {
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      String line = br.readLine();
      String[] vals = line.replaceAll("\\s+", " ").split(" ");
      cols = Integer.parseInt( vals[1] ); // ncols
      line = br.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      rows = Integer.parseInt( vals[1] ); // nrows
      line = br.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      xll  = Double.parseDouble( vals[1] ); // xllcorner
      line = br.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      yll  = Double.parseDouble( vals[1] ); // yllcorner
      line = br.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      mDim1 = Double.parseDouble( vals[1] ); // cellsize
      mDim2 = mDim1;
      line = br.readLine();
      vals = line.replaceAll("\\s+", " ").split(" ");
      nodata = Double.parseDouble( vals[1] ); // nodata.value
      fr.close();
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

