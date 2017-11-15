/** @filke CHTriangle.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid 3 vector triangle
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


public class CHTriangle extends CHFacet
{

  CHTriangle( Vector a, Vector b, Vector c )
  {
    super( 3 );
    mV[0] = a;
    mV[1] = b;
    mV[2] = c;
    computeCenter();
    computeNormal();
  }

  CHTriangle[] refineAtCenter()
  {
    Vector p = mV[0].plus( mV[1] ).plus( mV[2] ); p.timesEqual( 1.0f/3.0f );

    CHTriangle[] ret = new CHTriangle[3];
    ret[0] = new CHTriangle( mV[0], mV[1], p );
    ret[1] = new CHTriangle( mV[1], mV[2], p );
    ret[2] = new CHTriangle( mV[2], mV[0], p );
    return ret;
  }

  CHTriangle[] refineAtSides()
  {
    Vector a = mV[1].plus( mV[2] ); a.timesEqual( 0.5f );
    Vector b = mV[2].plus( mV[0] ); b.timesEqual( 0.5f );
    Vector c = mV[0].plus( mV[1] ); c.timesEqual( 0.5f );
    
    CHTriangle[] ret = new CHTriangle[4];
    ret[0] = new CHTriangle( a, b, c );
    ret[1] = new CHTriangle( mV[0], c, b );
    ret[2] = new CHTriangle( mV[1], a, c );
    ret[3] = new CHTriangle( mV[2], b, a );
    return ret;
  }

  CHTriangle[] refineAtVertex( Vector p )
  {
    CHTriangle[] ret = new CHTriangle[3];

    Vector va=null, vb=null, vc=null;
    if ( p == mV[0] ) {
      va = mV[0];  vb = mV[1];  vc = mV[2];
    } else if ( p == mV[1] ) {
      va = mV[1];  vb = mV[2];  vc = mV[0];
    } else if ( p == mV[2] ) {
      va = mV[2];  vb = mV[0];  vc = mV[1];
    } else {
      return null;
    }
    // Vector a = vb.plus( vc ); a.timesEqual( 0.5f );
    Vector b = vc.plus( va ); b.timesEqual( 0.5f );
    Vector c = va.plus( vb ); c.timesEqual( 0.5f );

    ret[0] = new CHTriangle( va, c, b );
    if ( va.distance( vb ) > va.distance( vc ) ) {
      ret[1] = new CHTriangle( vb, b, c );
      ret[2] = new CHTriangle( vb, vc, b );
    } else {
      ret[1] = new CHTriangle( vc, b, c );
      ret[2] = new CHTriangle( vb, vc, c );
    }
    return ret;
  }

}

