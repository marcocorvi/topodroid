/** @filke Facet.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid N vector facet
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.Math;

import android.util.Log;


public class Facet
{
  boolean valid;
  int    nr; // number of vectors
  public Vector[] mV; // vectors
  public Vector mN;   // unit normal
  public Vector mCenter;   // center
  private boolean hasCenter;
  private boolean hasNormal;

  Facet( int n )
  {
    valid = true;
    nr = n;
    mV = new Vector[nr];
    mN = new Vector( 0, 0, 0 );
    mCenter = new Vector( 0, 0, 0 );
    hasCenter = false;
    hasNormal = false;
  }

  void setVertex( int k, Vector v ) 
  { 
    mV[k] = v;
    hasCenter = false;
    hasNormal = false;
  }

  void computeCenter()
  {
    if ( nr <= 0 ) return;
    if ( hasCenter ) return;
    mCenter.x = mCenter.y = mCenter.z = 0;
    for ( int n=0; n<nr; ++n ) {
      mCenter.x += mV[n].x;
      mCenter.y += mV[n].y;
      mCenter.z += mV[n].z;
    }
    mCenter.x /= nr;
    mCenter.y /= nr;
    mCenter.z /= nr;
    hasCenter = true;
  }

  void computeNormal()
  {
    if ( nr <= 1 ) return;
    if ( ! hasCenter ) computeCenter();
    if ( hasNormal ) return;
    mN.x = mN.y = mN.z = 0;
    Vector v1 = mV[nr-1].minus( mCenter );
    for ( int n=0; n<nr; ++n ) {
      Vector v2 = mV[n].minus( mCenter );
      mN.plusEqual( v1.cross( v2 ) );
      v1 = v2;
    }
    mN.normalize();
    hasNormal = true;
  }

  float signedDistance( Vector p ) { return mN.dot( p.minus(mV[0]) ); }

  boolean isPositive( Vector p ) { return signedDistance(p) > 0.0000001f; }

  Triangle[] refineAtCenter()
  {
    Triangle[] ret = new Triangle[nr];
    Vector v1 = mV[nr-1].minus( mCenter );
    for ( int n=0; n<nr; ++n ) {
      Vector v2 = mV[n].minus( mCenter );
      ret[n] = new Triangle( v1, v2, mCenter );
      v1 = v2;
    }
    return ret;
  }

  Triangle[] refineAtSides()
  {
/*
    Vector a = mB.plus( mC ); a.timesEqual( 0.5f );
    Vector b = mC.plus( mA ); b.timesEqual( 0.5f );
    Vector c = mA.plus( mB ); c.timesEqual( 0.5f );
    
    Triangle[] ret = new Triangle[4];
    ret[0] = new Triangle( a, b, c );
    ret[1] = new Triangle( mA, c, b );
    ret[2] = new Triangle( mB, a, c );
    ret[3] = new Triangle( mC, b, a );
    return ret;
*/
    return null;
  }

  Triangle[] refineAtVertex( Vector p )
  {
/*
    Triangle[] ret = new Triangle[3];

    Vector va=null, vb=null, vc=null;
    if ( p == mA ) {
      va = mA;  vb = mB;  vc = mC;
    } else if ( p == mB ) {
      va = mB;  vb = mC;  vc = mA;
    } else if ( p == mC ) {
      va = mC;  vb = mA;  vc = mB;
    } else {
      return null;
    }
    // Vector a = vb.plus( vc ); a.timesEqual( 0.5f );
    Vector b = vc.plus( va ); b.timesEqual( 0.5f );
    Vector c = va.plus( vb ); c.timesEqual( 0.5f );

    ret[0] = new Triangle( va, c, b );
    if ( va.distance( vb ) > va.distance( vc ) ) {
      ret[1] = new Triangle( vb, b, c );
      ret[2] = new Triangle( vb, vc, b );
    } else {
      ret[1] = new Triangle( vc, b, c );
      ret[2] = new Triangle( vb, vc, c );
    }
    return ret;
*/
    return null;
  }

}

