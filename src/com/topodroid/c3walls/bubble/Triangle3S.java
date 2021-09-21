/* @file Triangle3S.java
 *
 * @author marco corvi
 * @date may 2021
 *
 * @brief bubble algo: a triangle of points on the sphere
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.bubble;

import com.topodroid.Cave3X.Vector3D;

//  -.   T31    .--
//    P1 ---- P3
// T12  \    / T23
//        P2
//        |
//
public class Triangle3S
{
  private static int cnt = 0;
  private int id;

  private Point3S p1;
  private Point3S p2;
  private Point3S p3;
  private Vector3D u12, u23, u31;
  private Vector3D v12;     // V1 - V2
  private Vector3D v13;     // V1 - V3
  private Vector3D normal;  // normal (P2-P1)^(P3-P1) not-normalized
  private Vector3D vnormal; // normal (V2-V1)^(V3-V1) not-normalized

  public Triangle3S t12; // neighbor triangles on 1-2
  public Triangle3S t23; // on 2-3
  public Triangle3S t31; // on 3-1
  public int inside;

  public Triangle3S( Point3S pp1, Point3S pp2, Point3S pp3 )
  {
    id = ++cnt;
    reset( pp1, pp2, pp3, null, null, null );
  }

  public Triangle3S( Point3S pp1, Point3S pp2, Point3S pp3, Triangle3S tt12, Triangle3S tt23, Triangle3S tt31 )
  {
    id = ++cnt;
    reset( pp1, pp2, pp3, tt12, tt23, tt31 );
  }

  public int getId() { return id; }

  public Vector3D getVNormal() { return vnormal; }

  public Vector3D getCenter()
  {
    return new Vector3D( (p1.x + p2.x + p3.x)/3,
                       (p1.y + p2.y + p3.y)/3,
                       (p1.z + p2.z + p3.z)/3 );
  }
 
  public Vector3D getNormal() { return normal; }

  public int countInside() 
  {
    inside = 0;
    inside += p1.inside ? 1 : 0;
    inside += p2.inside ? 1 : 0;
    inside += p3.inside ? 1 : 0;
    return inside;
  }

  public boolean contains( Vector3D v, Vector3D c )
  {
    if ( vnormal.dotProduct(v.difference(p1.v)) >= 0 ) return false;
    Vector3D v0 = v.difference(c);
    Vector3D v1 = p1.v.difference(c);
    Vector3D v2 = p2.v.difference(c);
    Vector3D v3 = p3.v.difference(c);
    if ( v0.dotProduct( v2.crossProduct(v1) ) > 0 ) return false;
    if ( v0.dotProduct( v3.crossProduct(v2) ) > 0 ) return false;
    if ( v0.dotProduct( v1.crossProduct(v3) ) > 0 ) return false;
    return true;
  }


  public void reset( Point3S pp1, Point3S pp2, Point3S pp3, Triangle3S tt12, Triangle3S tt23, Triangle3S tt31 )
  {
    p1 = pp1;
    p2 = pp2;
    p3 = pp3;
    t12 = tt12;
    t23 = tt23;
    t31 = tt31;
    initNormals();
  }

  // check if a point on the sphere is inside this spherical triangle
  // 
  //    p1 --- p3   +
  //      \  /      | u31
  //       p2       v
  //
  // u31 * P > 0 if the (unit) vector P lies on the hemisphere side of p2
  //
  public boolean containsPoint( Point3S p ) 
  { 
    return u12.dotProduct(p) >= 0 && u23.dotProduct(p) >= 0 && u31.dotProduct(p) >= 0;
  }

  public void swap23()
  {
    Point3S pt = p2; p2 = p3; p3 = pt;
    Triangle3S tr = t12; t12 = t31; t31 = tr;
    initNormals();
  }

  public void setNghbs( Triangle3S tt12, Triangle3S tt23, Triangle3S tt31 )
  {
    t12 = tt12;
    t23 = tt23;
    t31 = tt31;
  }

  public void setNghb( Point3S pp1, Point3S pp2, Triangle3S tr )
  {
    switch ( getSide( pp1, pp2 ) ) {
      case 1: t23 = tr; break;
      case 2: t31 = tr; break;
      case 3: t12 = tr; break;
    }
  }

  // compute the abscissa of intersection of the segment  PP1 + a (PP2 - PP1) within the triangle
  // return a if exists, or -1
  //
  //        v3
  //       /
  //     v1 ---- v2
  //
  //  vv1 + a * ( vv2 - vv1 ) = v1 + s * ( v2 - v1 ) + t * ( v3 - v1 )
  //
  //  (vv2-vv1) * a  + (v1-v2) * s  + (v1-v3) * t  =  v1 - vv1;
  //
  //  |  |     |    |  |
  //  | vv21  v12  v13 | (a,s,t) = v11
  //  |  |     |    |  |
  //
  //  det = vv21 * p12 ^ p13 = vv21 * vnormal
  //  inverse matrix
  //       |  v12 ^ v13  |
  //       |  v13 ^ vv21 | / det
  //       |  vv21 ^ v12 |
  //  
  //  (a,s,t) = | v12^v13  v13^vv21  vv21^v12 | * v11 / det
  //
  public Vector3D intersection( Vector3D vv1, Vector3D vv2 ) 
  {
    Vector3D vv21 = vv2.difference( vv1 );
    Vector3D v11  = p1.v.difference( vv1 );
    Vector3D vy   = v13.crossProduct( vv21 );
    Vector3D vz   = vv21.crossProduct( v12 );
    double det  = vv21.dotProduct( vnormal );
    if ( det == 0.0 ) return null;
    double a = v11.dotProduct( vnormal ) / det;
    double s = v11.dotProduct( vy ) / det;
    double t = v11.dotProduct( vz ) / det;
    if ( a < 0 || a > 1 ) return null;
    if ( s < 0 || s > 1 ) return null;
    if ( t < 0 || t > 1-s ) return null;
    return new Vector3D( vv1.x + a * (vv2.x - vv1.x), vv1.y + a * (vv2.y - vv1.y), vv1.z + a * (vv2.z - vv1.z) );
  }

  public Point3S pt1() { return p1; }
  public Point3S pt2() { return p2; }
  public Point3S pt3() { return p3; }

  public Vector3D v1() { return p1.v; }
  public Vector3D v2() { return p2.v; }
  public Vector3D v3() { return p3.v; }

  // a neighbor triangle contains a reversed side 
  public boolean hasNghb( Triangle3S tr ) { return getNgbh( tr ) != 0; }

  // get the index of the side on which tr is neghbor, or 0 if not ngbh
  public int getNgbh( Triangle3S tr ) 
  {
    return tr.hasSide( p2, p1 ) ? 3
         : tr.hasSide( p3, p2 ) ? 1
         : tr.hasSide( p1, p3 ) ? 2
         : 0;
  }

  // get the point opposite to (pp1 -- pp2)
  public Point3S getOppositePoint( Point3S pp1, Point3S pp2 )
  {
    switch ( getSide( pp1, pp2 ) ) {
      case 1: return p1;
      case 2: return p2;
      case 3: return p3;
      default:
        // TDLog.v( "opposite point: no side");
    }
    return null;
  }

  // get the ngth triangle of side (pp1 -- pp2)
  public Triangle3S getTriangle( Point3S pp1, Point3S pp2 )
  {
    switch ( getSide( pp1, pp2 ) ) {
      case 1: return t23;
      case 2: return t31;
      case 3: return t12;
      default:
        // TDLog.v( "nghb triangle: no side");
    }
    return null;
  }

  // contains the side (PP1 --> PP2)
  public boolean hasSide( Point3S pp1, Point3S pp2 ) { return getSide( pp1, pp2 ) != 0; }

  // get the index of the side (PP1 --> PP2)
  //       2
  //   p1 ---- p3
  //  3  \   / 1
  //       p2
  // 
  // idx  side     nghb.tri   opp.pt
  //   1: p2--p3        t23       p1
  //   2: p3--p1        t31       p2
  //   3: p1--p2        t12       p3
  //
  public int getSide( Point3S pp1, Point3S pp2 )
  {
    return ( p1 == pp1 && p2 == pp2 )? 3
         : ( p2 == pp1 && p3 == pp2 )? 1
         : ( p3 == pp1 && p1 == pp2 )? 2
         : 0;
  }
    
  public double dotProduct( Point3S p ) { return p.dotProduct( normal ); }

  private void initNormals()
  {
    u12 = p1.crossProduct(p2);  // u12.normalized();
    u23 = p2.crossProduct(p3);  // u23.normalized();
    u31 = p3.crossProduct(p1);  // u31.normalized();
    v12 = p1.v.difference( p2.v );
    v13 = p1.v.difference( p3.v );
    vnormal = v12.crossProduct( v13 );
    normal = ( p2.difference(p1) ).crossProduct( p3.difference(p1) );
    // normal.normalized();
  }

  public double volume( Point3S pt ) 
  {
    return normal.dotProduct( pt.difference(p1) );
  }

  // volume of the tetrahedrom given the four vertices
  static public double volume( Vector3D v1, Vector3D v2, Vector3D v3, Vector3D v4 )
  {
    return volume( v2.difference(v1), v3.difference(v1), v4.difference(v1) );
  }

  // volume of three vectors centered at the origin
  static public double volume( Vector3D v1, Vector3D v2, Vector3D v3 )
  {
    return v1.crossProduct(v2).dotProduct(v3);
  }

}
