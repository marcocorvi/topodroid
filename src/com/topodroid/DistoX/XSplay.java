/** @file XSplay.java
 *
 * @author marco corvi
 * @date mar 2021
 *
 * @brief a splay in a x-section
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class XSplay extends Vector3D
             implements Comparable
{
  double angle;

  XSplay( Vector3D p, double a )
  {
    super( p );
    angle = a;
  }

  // used by deserialize
  XSplay( double x, double y, double z, double a )
  {
    super( x,y,z );
    angle = a;
  }


  @Override public boolean equals( Object other )
  { 
    if ( other == null ) return false;
    if ( other instanceof XSplay ) return this.angle == ((XSplay)other).angle;
    return false;
  }

  // if other is not instanceof XSplay return ClassCastException
  @Override public int compareTo( Object other ) 
  { 
    if ( other == null ) throw new NullPointerException();
    if ( other instanceof XSplay )
      return (this.angle < ((XSplay)other).angle)? -1 : (this.angle == ((XSplay)other).angle)? 0 : 1;
    throw new ClassCastException();
  }

  // void dump()
  // {
  //   TDLog.v("TopoGL " + String.format("   %6.1f  %8.2f %8.2f %8.2f", angle, x, y, z ) );
  // }

  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.writeDouble( x );
    dos.writeDouble( y );
    dos.writeDouble( z );
    dos.writeDouble( angle );
  }

  static XSplay deserialize( DataInputStream dis, int version ) throws IOException
  {
    return new XSplay( dis.readDouble(), dis.readDouble(), dis.readDouble(), dis.readDouble() );
  }


}

