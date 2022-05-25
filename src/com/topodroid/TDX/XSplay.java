/** @file XSplay.java
 *
 * @author marco corvi
 * @date mar 2021
 *
 * @brief a splay in a x-section
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

// import java.io.StringWriter;
// import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class XSplay extends Vector3D
             implements Comparable
{
  final static double EPS = 1.0e-7; // angle comparison tolerance
  double angle; // XSplay angle

  /** cstr
   * @param p  3D point
   * @param a  angle
   */
  XSplay( Vector3D p, double a )
  {
    super( p );
    angle = a;
  }

  /** cstr
   * @param x  X coord
   * @param y  Y coord
   * @param z  Z coord
   * @param a  angle
   * @note used by deserialize
   */
  XSplay( double x, double y, double z, double a )
  {
    super( x,y,z );
    angle = a;
  }

  /** @return true if the other object is an XSplay and has the same angle (within 1.e-7)
   * @param other   another XSplay
   */
  @Override public boolean equals( Object other )
  { 
    if ( other == null ) return false;
    if ( other instanceof XSplay ) return Math.abs( this.angle - ((XSplay)other).angle ) < EPS;
    return false;
  }

  /** compare to another XSplay - for ordering
   * @param other   another XSplay
   * @return -1,0,1 or an exception
   * @note if other is not instanceof XSplay return ClassCastException
   */
  @Override public int compareTo( Object other ) 
  { 
    if ( other == null ) throw new NullPointerException();
    if ( other instanceof XSplay )
      return (this.angle < ((XSplay)other).angle - EPS)? -1 : (this.angle > ((XSplay)other).angle + EPS)? 1 : 0;
    throw new ClassCastException();
  }

  // void dump()
  // {
  //   TDLog.v("TopoGL " + String.format("   %6.1f  %8.2f %8.2f %8.2f", angle, x, y, z ) );
  // }

  /** serialize the XSplay
   * @param dos     output data stream
   */
  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.writeDouble( x );
    dos.writeDouble( y );
    dos.writeDouble( z );
    dos.writeDouble( angle );
  }
 
  /** @return deserialized XSplay
   * @param dis     input data stream
   * @param version input stream version
   */
  static XSplay deserialize( DataInputStream dis, int version ) throws IOException
  {
    return new XSplay( dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readDouble() );
  }


}

