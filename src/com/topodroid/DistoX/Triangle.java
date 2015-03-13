/** @filke Triangle.java
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
 * CHANGES
 */
package com.topodroid.DistoX;

import java.lang.Math;

import android.util.FloatMath;
import android.util.Log;


public class Triangle
{
  boolean valid;
  public Vector mA, mB, mC;
  public Vector mN;   // unit normal

  Triangle( Vector a, Vector b, Vector c )
  {
    valid = true;
    mA = a;
    mB = b;
    mC = c;
    Vector v1 = mB.minus( mA );
    Vector v2 = mC.minus( mA );
    mN = v1.cross( v2 );
    mN.Normalized();
  }

  float signedDistance( Vector p ) { return mN.dot( p.minus(mA) ); }

  boolean isPositive( Vector p ) { return signedDistance(p) > 0.0000001f; }

  Triangle[] refineAtCenter()
  {
    Vector p = mA.plus( mB ).plus( mC ); p.scaleBy( 1.0f/3.0f );

    Triangle[] ret = new Triangle[3];
    ret[0] = new Triangle( mA, mB, p );
    ret[1] = new Triangle( mB, mC, p );
    ret[2] = new Triangle( mC, mA, p );
    return ret;
  }

  Triangle[] refineAtSides()
  {
    Vector a = mB.plus( mC ); a.scaleBy( 0.5f );
    Vector b = mC.plus( mA ); b.scaleBy( 0.5f );
    Vector c = mA.plus( mB ); c.scaleBy( 0.5f );
    
    Triangle[] ret = new Triangle[4];
    ret[0] = new Triangle( a, b, c );
    ret[1] = new Triangle( mA, c, b );
    ret[2] = new Triangle( mB, a, c );
    ret[3] = new Triangle( mC, b, a );
    return ret;
  }

  Triangle[] refineAtVertex( Vector p )
  {
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
    // Vector a = vb.plus( vc ); a.scaleBy( 0.5f );
    Vector b = vc.plus( va ); b.scaleBy( 0.5f );
    Vector c = va.plus( vb ); c.scaleBy( 0.5f );

    ret[0] = new Triangle( va, c, b );
    if ( va.distance( vb ) > va.distance( vc ) ) {
      ret[1] = new Triangle( vb, b, c );
      ret[2] = new Triangle( vb, vc, b );
    } else {
      ret[1] = new Triangle( vc, b, c );
      ret[2] = new Triangle( vb, vc, c );
    }
    return ret;
  }

}

