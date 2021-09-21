/** @file ParserDEM.java
 *
 * @author marco corvi
 * @date apr 2020
 *
 * @brief DEM parser
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

class ParserDEM extends DEMsurface
{
  protected boolean mValid;    // header is valid
  protected int   mMaxSize;    // max DEM size (in each direction)
  // double mDim1; protected double   xcell;     // cell size
  // double mDim2; protected double   ycell;     // cell size
  // float[] mZ; protected float[] mData;     // DEM data
  // int mNr1; protected int     mX;    // grid dimension
  // int mNr2; protected int     mY;    // grid dimension

  // double mEast1 mEast2 mNorth1, mNorth2
  // protected double   mWest, mEast, mSouth, mNorth; // bounds
  protected double nodata;    // nodata value
  String mFilename; // DEM filename
  protected double xunit, yunit;

  ParserDEM( String filename, int size )
  {
    mFilename = filename;
    mMaxSize  = size;
    xunit = 1.0f;
    yunit = 1.0f;
    mValid = readHeader( mFilename );
  }

  ParserDEM( String filename, int size, double xu, double yu )
  {
    mFilename = filename;
    mMaxSize  = size;
    xunit = xu;
    yunit = yu;
    mValid = readHeader( mFilename );
  }

  boolean valid() { return mValid; }
  double west()  { return mEast1; }
  double east()  { return mEast2; }
  double south() { return mNorth1; }
  double north() { return mNorth2; }

  float Z( int i, int j ) { return mZ[j*mNr1+i]; }
  double cellXSize() { return mDim1; }
  double cellYSize() { return mDim2; }
  int dimX() { return mNr1; }
  int dimY() { return mNr2; }
  
  float[] data() { return mZ; }

  boolean readData( double xwest, double xeast, double ysouth, double ynorth ) { return false; }

  protected  boolean readHeader( String filename ) { mValid = false; return false; }

  // protected void makeNormal() 
  // {
  //   if ( mValid ) {
  //     mNormal = new float[ 3*mNr1*mNr2 ];
  //     initNormal();
  //   }
  // }

}

