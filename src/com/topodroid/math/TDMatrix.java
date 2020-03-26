/* @file TDMatrix.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid 3x3 matrix
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 */
package com.topodroid.math;

public class TDMatrix
{ 
  public TDVector x,y,z;
 
  public static TDMatrix zero = new TDMatrix( TDVector.zero, TDVector.zero, TDVector.zero );

  public static TDMatrix one = new TDMatrix( new TDVector(1.0f, 0.0f, 0.0f),
                                  new TDVector(0.0f, 1.0f, 0.0f),
                                  new TDVector(0.0f, 0.0f, 1.0f) );

  // Default cstr: zero matrix
  public TDMatrix()
  {
    x = new TDVector();
    y = new TDVector();
    z = new TDVector();
  }

  public TDMatrix( TDVector x0, TDVector y0, TDVector z0 )
  {
    x = new TDVector(x0);
    y = new TDVector(y0);
    z = new TDVector(z0);
  }

  // OUTER PRODUCT: a & b
  public TDMatrix( TDVector a, TDVector b )
  {
    x = b.times(a.x);
    y = b.times(a.y);
    z = b.times(a.z);
  }

  public TDMatrix( TDMatrix a )
  {
    x = new TDVector( a.x );
    y = new TDVector( a.y );
    z = new TDVector( a.z );
  }

  public float maxAbsValue()
  {
    double mx = x.maxAbsValue();
    double my = y.maxAbsValue();
    double mz = z.maxAbsValue();
    return (float)( ( mx > my )? ( ( mx > mz )? mx : mz )
                               : ( ( my > mz )? my : mz ) );
  }

  public void plusEqual( TDMatrix b )
  {
    x.plusEqual( b.x );
    y.plusEqual( b.y );
    z.plusEqual( b.z );
  }

  public TDMatrix plus( TDMatrix b )
  {
    return new TDMatrix( x.plus(b.x), y.plus(b.y), z.plus(b.z) );
  }

  public TDMatrix minus( TDMatrix b )
  {
    return new TDMatrix( x.minus(b.x), y.minus(b.y), z.minus(b.z) );
  }

  public void timesEqual( float b )
  {
    x.timesEqual( b );
    y.timesEqual( b );
    z.timesEqual( b );
  }

  public TDMatrix timesF( float b )
  {
    return new TDMatrix( x.times(b), y.times(b), z.times(b) );
  }

  public TDVector timesV( TDVector b )
  {
    return new TDVector( x.dot(b), y.dot(b), z.dot(b) );
  }

  // multiplication with the transposed: this * B^t
  public TDMatrix timesT( TDMatrix b )
  {
    return new TDMatrix( b.timesV(x), b.timesV(y), b.timesV(z) );
  }

  public TDMatrix timesM( TDMatrix b )
  {
    return this.timesT( b.Transposed() );
  }

  // inverse of the transposed: (this^t)^-1
  public TDMatrix InverseT()
  {
    TDMatrix ad = new TDMatrix( y.cross(z), z.cross(x), x.cross(y) );
    float inv_det = 1.0f / ( x.dot( ad.x ) );
    ad.timesEqual( inv_det );
    return ad;
  }

  // inverse 
  public TDMatrix InverseM()
  {
    TDMatrix at = this.Transposed();
    return at.InverseT();
  }

  public TDMatrix Transposed()
  {
    TDMatrix ret = new TDMatrix();
    ret.x.x = x.x;
    ret.x.y = y.x;
    ret.x.z = z.x;
    ret.y.x = x.y;
    ret.y.y = y.y;
    ret.y.z = z.y;
    ret.z.x = x.z;
    ret.z.y = y.z;
    ret.z.z = z.z;
    return ret;
  }

  public float MaxDiff( TDMatrix b )
  {
    float dx = x.MaxDiff( b.x );
    float dy = y.MaxDiff( b.y );
    float dz = z.MaxDiff( b.z );
    if ( dx < dy ) { dx = dy; }
    if ( dx < dz ) { dx = dz; }
    return dx;
  }

}
