/** @file IcoDiagram.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief Cave3D 3D rose direction diagram
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 */
package com.topodroid.TDX;

class IcoDiagram
{
  int mNr;         // nr. of points
  int mPointNr;    // nr. of icosahedron points
  double[] mValue;
  double mDelta;
  double mEps;
  Icosahedron mIco;

  IcoDiagram( int n ) 
  {
    mNr = n;
    mIco = new Icosahedron( mNr );
    mPointNr = mIco.mPointNr;
    mDelta = Math.acos( 1/Math.sqrt(5) ) / mNr; // Angle step (radians)
    mEps   = Math.cos( mDelta * 2 );
    mValue = new double[ mPointNr ];
    reset();
  }

  IcoPoint getDirection( int k ) 
  {
    return mIco.getPoint( k );
  }

  void reset()
  {
    for ( int k = 0; k < mPointNr; ++k ) mValue[k] = 0;
  }

  double maxValue()
  {
    double max = 0;
    for ( int k = 0; k < mPointNr; ++k ) if ( mValue[k] > max ) max = mValue[k];
    return max;
  }


  void add( double d, double b, double c, double eps ) // angles in radians
  {
    double x = Math.cos( c ) * Math.sin( b );
    double y = Math.cos( c ) * Math.cos( b );
    double z = Math.sin( c );
    for ( int k = 0; k < mPointNr; ++k ) {
      IcoPoint p = mIco.getPoint(k);
      double cc = (x * p.x + y * p.y + z * p.z)/IcoPoint.R;
      if ( cc > eps || -cc > eps ) {
        mValue[k] += d * Math.abs( cc );
      }
    }
  }

}
