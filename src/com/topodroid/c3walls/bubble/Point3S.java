/* @file Point3S.java
 *
 * @author marco corvi
 * @date may 2021
 *
 * @brief bubble algo: a point on the sphere correspoding to a 3D vector
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.bubble;

import com.topodroid.TDX.Vector3D;

// import java.util.ArrayList;

// normalized vector
public class Point3S extends Vector3D
{
  private static int cnt = 0;
  private int id;
  public int getId() { return id; }
  public boolean inside;  // work bool

  public Vector3D v;       // vector of this Point3S
  // ArrayList< Triangle3S > mTri;

  // public Point3S( ) { init( 1, 0, 0 ); }

  // public Point3S( double xx, double yy, double zz ) { init(xx, yy, zz); }

  // @param vv   3D vector
  // @param cc   3D center (to which refer this point)
  public Point3S( Vector3D vv, Vector3D cc ) 
  {
    super( vv.difference(cc) );
    id = ++cnt;
    normalized();
    v = vv;
    // mTri = new ArrayList< Triangle3S >();
  }

  // public void addTriangle( Triangle3S tr ) { mTri.add( tr ); }

  // public int getNrTriangles() { return mTri.size(); }

  // public Point3S( double xx, double yy, double zz, boolean on_sphere, boolean random )
  // {
  //   init( xx, yy, zz );
  //   if ( random )    randomized();
  //   if ( on_sphere ) normalized();
  // }

  // a  azimuth [degrees]
  // c  clino [degrees]
  // public Point3S( double a, double c )
  // {
  //   a = a * Math.PI / 180.0;
  //   c = c * Math.PI / 180.0;
  //   init( Math.sin(c) * Math.cos(a), Math.sin(c) * Math.sin(a), Math.cos(c) );
  // }

  public boolean coincides( Point3S p ) { return x == p.x && y == p.y && z == p.z; }

  public void normalized()
  {
    double d = 1.0 / Math.sqrt( x*x + y*y + z*z );
    x *= d;
    y *= d;
    z *= d;
  }

  // cosine between this point and another point
  // NOTE both this and the other point are supposed of unit length 
  public double cosine( Point3S p ) 
  { 
    double ret = this.dotProduct( p );
    return ( ret < -1 )? -1 : ( ret > 1 )? 1 : ret;
  }

  // arc-distance between two points on the unit sphere
  public double arcDistance( Point3S p ) { return Math.acos( cosine( p ) ) * 180.0 / Math.PI; }

}

