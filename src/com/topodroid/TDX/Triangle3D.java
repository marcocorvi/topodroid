/** @file Triangle3D.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief face triangle
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Locale;

import com.topodroid.utils.TDLog;

public class Triangle3D
{
  public int size;
  public Vector3D[] vertex;
  public Vector3D   normal;
  public Vector3D   center;
  public int direction;
  public int color; // DEBUG

  public Triangle3D( int sz, int col )
  {
    size = sz;
    vertex = new Vector3D[size];
    normal = null;
    center = null;
    direction = 0;
    color = col;
  }

  public Triangle3D( Vector3D v0, Vector3D v1, Vector3D v2, int col )
  {
    size = 3;
    vertex = new Vector3D[3];
    vertex[0] = v0;
    vertex[1] = v1;
    vertex[2] = v2;
    computeNormal();
    direction = 0;
    color = col;
  }
  
  public Triangle3D toOpenGL( double x, double y, double z )
  {
    Triangle3D ret = new Triangle3D( size, color );
    for ( int k=0; k<size; ++k ) {
      ret.vertex[k] = new Vector3D( vertex[k].x - x, vertex[k].z - y, - vertex[k].y - z );
    }
    if ( normal != null ) {
      ret.normal = new Vector3D( normal.x, normal.z, - normal.y );
    }
    if ( center != null ) {
      ret.center = new Vector3D( center.x - x, center.z - y, -center.y - z );
    }
    ret.direction = direction;
    return ret;
  }

  public void dump()
  {
    TDLog.v("Cave3D " + String.format(Locale.US, "%6.1f %6.1f %6.1f  %6.1f %6.1f %6.1f  %6.1f %6.1f %6.1f",
      vertex[0].x, vertex[0].y, vertex[0].z, 
      vertex[1].x, vertex[1].y, vertex[1].z, 
      vertex[2].x, vertex[2].y, vertex[2].z ) );
  }

  public void setVertex( int k, Vector3D v )
  {
    vertex[k] = v;
  }

  public void computeNormal()
  {
    Vector3D w1 = vertex[1].difference( vertex[0] );
    Vector3D w2 = vertex[2].difference( vertex[0] );
    normal = w1.crossProduct(w2);
    normal.normalized();
    center = new Vector3D( 0, 0, 0 );
    for ( int k=0; k<size; ++k ) {
      center.add( vertex[k] );
    }
    center.scaleBy( 1.0f/size );
  }

  // update min-max according to this vector
  void minMax( Vector3D m1, Vector3D m2 )
  {
    for ( int k=0; k<size; ++k ) {
      vertex[k].minMax( m1, m2 );
    }
  }

  void flip()
  {
    Vector3D v = vertex[1];
    vertex[1] = vertex[2];
    vertex[2] = v;
    normal.reverse();
  }

  public String toString()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format(Locale.US, "%.2f %.2f %.2f - %.2f %.2f %.2f - %.2f %.2f %.2f",
      vertex[0].x, vertex[0].y, vertex[0].z,
      vertex[1].x, vertex[1].y, vertex[1].z,
      vertex[2].x, vertex[2].y, vertex[2].z );
    return sw.getBuffer().toString();
  }

  // 6 times the volume of the three vectors
  public static double volume( Vector3D v1, Vector3D v2, Vector3D v3 )
  {
    return v1.x * ( v2.y * v3.z - v2.z * v3.y )
         + v1.y * ( v2.z * v3.x - v2.x * v3.z )
         + v1.z * ( v2.x * v3.y - v2.y * v3.x );
  }

  public static double volume( Vector3D v0, Vector3D v1, Vector3D v2, Vector3D v3 )
  {
    return volume( v1.difference(v0), v2.difference(v0), v3.difference(v0) );
  }

  public double volume( Vector3D v )
  {
    double ret = 0;
    Vector3D v0 = vertex[0].difference(v);
    Vector3D v1 = vertex[1].difference(v);
    for ( int k=2; k<size; ++k ) {
      Vector3D v2 = vertex[k].difference(v);
      ret += volume( v0, v1, v2 );
      v1 = v2;
    }
    return ret;
  }
    

}
