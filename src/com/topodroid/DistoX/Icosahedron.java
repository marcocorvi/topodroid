/** @file Icosahedron.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief solid of 3D rose direction diagram
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 */
package com.topodroid.DistoX;

// import java.util.ArrayList;

// import java.io.StringWriter;
// import java.io.PrintWriter;

class Icosahedron
{
  int mN;
  int mPointNr;
  IcoPoint[] mPoint;
  IcoPoint[] mVertex;
  IcoFace[]  mFace;
  IcoSide[]  mSide;
  int[] mNghb;

  IcoPoint getPoint( int k ) 
  {
    // if ( k < 0 || n > mPointNr ) return null;
    return mPoint[k];
  }

  void fillNghb()
  {
    for (int k1=0; k1<12; ++k1 ) {
      mNghb[ k1*12 + k1 ] = 0;
      for (int k2=k1+1; k2<12; ++k2 ) {
        double d = mVertex[k1].distance( mVertex[k2] );
        mNghb[ k1*12 + k2 ] = mNghb[ k2*12 + k1 ] = ( d < 2.01 )? 1 : 0;
      }
    }
    // for (int k1=0; k1<12; ++k1 ) {
    //   StringWriter sw = new StringWriter();
    //   PrintWriter  pw = new PrintWriter( sw );
    //   for (int k2=0; k2<12; ++k2 ) {
    //     pw.format(Locale.US, "%1d ", mNghb[ k1*12 + k2 ] );
    //   }
    //   TDLog.v( "ICO " + sw.getBuffer().toString() );
    // }
  }

  Icosahedron( int n )
  {
    mVertex = new IcoPoint[12];
    mFace   = new IcoFace[20];
    mSide   = new IcoSide[30];
    mNghb = new int[12*12];

    for (int k=0; k<12; ++k ) {
      mVertex[k] = new IcoVertex( k );
    }
    fillNghb();

    int ns = 0;
    int nf = 0;
    for ( int k1 = 0; k1 < 12; ++k1 ) {
      for (int k2 = k1+1; k2 < 12; ++k2 ) {
        if ( mNghb[ k1*12 + k2 ] == 1 ) {
          mSide[ns] = new IcoSide( mVertex[k1], mVertex[k2] );
          ++ ns;
          for ( int k3 = k2+1; k3 < 12; ++k3 ) {
            if ( mNghb[ k1*12 + k3 ] == 1 && mNghb[ k2*12 + k3 ] == 1 ) {
              mFace[nf] = new IcoFace( mVertex[k1], mVertex[k2], mVertex[k3] );
              ++ nf;
            }
          }
        }
      }
    }
    mN = n;
    mPointNr = 12 + ns * (n-1) + nf * (n-1)*(n-2) / 2;
    // TDLog.v( "ICO expected nr. points " + mPointNr );
    mPoint = new IcoPoint[ mPointNr ];
    int np = 0;
    for (int k=0; k<12; ++k ) {
      mPoint[np] = mVertex[k];
      // TDLog.v( "ICO V-Point " + np + ": " + mPoint[np].x + " " + mPoint[np].y + " " + mPoint[np].z );
      ++ np;
    }
    for (int k=0; k<ns; ++k ) {
      IcoSide side = mSide[k];
      for ( int i=1; i<n; ++i ) {
        // TDLog.v( "ICO side " + k + " index " + i + " point " + np );
        mPoint[np] = side.interpolate( i, n );
        // TDLog.v( "ICO S-Point " + np + ": " + mPoint[np].x + " " + mPoint[np].y + " " + mPoint[np].z );
        ++ np;
      }
    }
    for (int k=0; k<nf; ++k ) {
      IcoFace face = mFace[k];
      for ( int i=1; i<n; ++i ) for ( int j = 1; j < n-i; ++j ) {
        // TDLog.v( "ICO face " + k + " index " + i + "/" + j + " point " + np );
        mPoint[np] = face.interpolate( i, j, n );
        // TDLog.v( "ICO F-Point " + np + ": " + mPoint[np].x + " " + mPoint[np].y + " " + mPoint[np].z );
        ++ np;
      }
    }
    // TDLog.v( "ICO ico points " + np + " " + mPointNr );
  }
}
       
