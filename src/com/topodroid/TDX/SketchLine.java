/* @file SketchLine.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketching: 3D line
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.math.TDVector;


public class SketchLine // world coords
{
  TDVector mC;  // base point
  TDVector mN;  // line vector

  /** cstr
   * @param c   base point
   * @param n   line vector
   */
  public SketchLine( TDVector c, TDVector n )
  {
    mC = new TDVector( c );
    mN = new TDVector( n );
    mN.normalize();
  }

  /** @return distance of a point from the line
   * @param pt   3D point
   */
  float distance( SketchPoint pt )
  {
    TDVector v = pt.minus( mC );
    float a = mN.dot( v );
    a = v.LengthSquared() - a*a;
    return ( a > 0 )? TDMath.sqrt( a ) : 0;
  }

  /** @return squared distance of a point from the line
   * @param pt   3D point
   */
  float distanceSquared( SketchPoint pt )
  {
    TDVector v = pt.minus( mC );
    float a = mN.dot( v );
    return ( v.LengthSquared() - a*a );
  }

}

