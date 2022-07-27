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

  /** default cstr: zero matrix
   */
  public TDMatrix()
  {
    x = new TDVector();
    y = new TDVector();
    z = new TDVector();
  }

  /** cstr
   * @param x0   X-vector
   * @param y0   Y-vector
   * @param z0   Z-vector
   * @note the matrix is row-wise (?):
   *    -- X --
   *    -- Y --
   *    -- Z --
   */
  public TDMatrix( TDVector x0, TDVector y0, TDVector z0 )
  {
    x = new TDVector(x0);
    y = new TDVector(y0);
    z = new TDVector(z0);
  }

  /** outer product: a &amp; b
   *     a.x * b.x   a.x * b.y   a.x * b.z 
   *     a.y * b.x   a.y * b.y   a.y * b.z 
   *     a.z * b.x   a.z * b.y   a.z * b.z 
   * @param a  left vector
   * @param b  right vector
   */
  public TDMatrix( TDVector a, TDVector b )
  {
    x = b.times(a.x);
    y = b.times(a.y);
    z = b.times(a.z);
  }

  /** copy cstr
   * @param a  matrix
   */
  public TDMatrix( TDMatrix a )
  {
    x = new TDVector( a.x );
    y = new TDVector( a.y );
    z = new TDVector( a.z );
  }

  /** @return te maximum absolute value of the matrix entries
   */
  public float maxAbsValue()
  {
    double mx = x.maxAbsValue();
    double my = y.maxAbsValue();
    double mz = z.maxAbsValue();
    return (float)( ( mx > my )? ( ( mx > mz )? mx : mz )
                               : ( ( my > mz )? my : mz ) );
  }

  /** add another matrix to this one
   * @param b  the other matrix
   */
  public void plusEqual( TDMatrix b )
  {
    x.plusEqual( b.x );
    y.plusEqual( b.y );
    z.plusEqual( b.z );
  }

  /** @return the sum of this matrix and another matrix
   * @param b  the other matrix
   */
  public TDMatrix plus( TDMatrix b )
  {
    return new TDMatrix( x.plus(b.x), y.plus(b.y), z.plus(b.z) );
  }

  /** @return the difference of this matrix and another matrix
   * @param b  the other matrix
   */
  public TDMatrix minus( TDMatrix b )
  {
    return new TDMatrix( x.minus(b.x), y.minus(b.y), z.minus(b.z) );
  }

  /** multiply this matrix by a scalar
   * @param b   scalar value
   */
  public void timesEqual( float b )
  {
    x.timesEqual( b );
    y.timesEqual( b );
    z.timesEqual( b );
  }

  /** @return the matrix obtained multiplying this matrix with a scalar value
   * @param b   scalar value
   */
  public TDMatrix timesF( float b )
  {
    return new TDMatrix( x.times(b), y.times(b), z.times(b) );
  }

  /** return the vector ovtaimen by multiplying this matrix with a vector
   * @param b  vector
   * @note the result is
   *     -- X --     b.x
   *     -- Y --  *  b.y  = ( X * b,  Y * b,  Z * b )
   *     -- Z --     b.z
   */
  public TDVector timesV( TDVector b )
  {
    return new TDVector( x.dot(b), y.dot(b), z.dot(b) );
  }

  /** @return the multiplication with the transposed: this * B^t
   * @param b  the other matrix
   * @note return the matrix
   *    X * b.X     X * b.Y     X * b.Z        -- b * X --
   *    Y * b.X     Y * b.Y     Y * b.Z   =    -- b * Y --
   *    Z * b.X     Z * b.Y     Z * b.Z        -- b * Z --
   */
  public TDMatrix timesT( TDMatrix b )
  {
    return new TDMatrix( b.timesV(x), b.timesV(y), b.timesV(z) );
  }

  /** @return the multiplication with another matrix: this * B
   * @param b  the other matrix
   * @note return the matrix
   *    X * bt.X     X * bt.Y     X * bt.Z        -- bt * X --
   *    Y * bt.X     Y * bt.Y     Y * bt.Z   =    -- bt * Y --
   *    Z * bt.X     Z * bt.Y     Z * bt.Z        -- bt * Z --
   */
  public TDMatrix timesM( TDMatrix b )
  {
    return this.timesT( b.Transposed() );
  }

  /** @return inverse of the transposed: (this^t)^-1
   */
  public TDMatrix InverseT()
  {
    TDMatrix ad = new TDMatrix( y.cross(z), z.cross(x), x.cross(y) );
    float inv_det = 1.0f / ( x.dot( ad.x ) );
    ad.timesEqual( inv_det );
    return ad;
  }

  /** @return the inverse matrix
   */
  public TDMatrix InverseM()
  {
    TDMatrix at = this.Transposed();
    return at.InverseT();
  }

  /** @return the transposed matrix
   * @note the result is 
   *             |   |   |
   *             X   Y   Z
   *             |   |   |
   */
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

  /** @returns the maximum difference of elemnts between this matrix and another one
   * @param b  the other matrix
   */
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
