/** @file CWFacet.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief triangular facet (STL)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.cw;

import com.topodroid.DistoX.Vector3D;

import java.util.List;
// import java.io.PrintWriter;
// import java.io.PrintStream;
// import java.io.IOException;

public class CWFacet
{
  public CWPoint v1, v2, v3;
  protected Vector3D u;  // (v2-v1)x(v3-v1): U points "inside"
  public Vector3D un; // u normalized
  protected double u22, u23, u33; // u2*u2 / det, ... etc
  Vector3D u2; // v2-v1 
  Vector3D u3; // v3-v1
  Vector3D u1; // v3-v2
  
  // points are ordered v1--v2--v3 (looking at the triangle from "inside"
  public CWPoint nextOf( CWPoint p )
  {
    if ( p == v1 ) return v2;
    if ( p == v2 ) return v3;
    if ( p == v3 ) return v1;
    return null;
  }
  
  public CWPoint prevOf( CWPoint p )
  {
    if ( p == v1 ) return v3;
    if ( p == v2 ) return v1;
    if ( p == v3 ) return v2;
    return null;
  }

  double distance( Vector3D p ) { return Math.abs( un.dotProduct(p) ); } 
  
  public CWFacet( CWPoint v1, CWPoint v2, CWPoint v3 )
  {
    buildFacet( v1, v2, v3 );
  }
  
  public CWFacet( int tag, CWPoint v1, CWPoint v2, CWPoint v3 )
  {
    buildFacet( v1, v2, v3 );
  }
  
  protected void buildFacet( CWPoint v1, CWPoint v2, CWPoint v3 )
  {
    this.v1 = v1;
    this.v2 = v2;
    this.v3 = v3;
    computeVectors();
  }

  void computeVectors()
  {
    u2 = v2.difference(v1); // side s3
    u3 = v3.difference(v1); // side s2
    u1 = v3.difference(v2); // side s1
    
    u = u2.crossProduct(u3);
    un = new Vector3D( u );
    un.normalized();
    u22 = u2.dotProduct(u2);
    u23 = u2.dotProduct(u3);
    u33 = u3.dotProduct(u3);
    double udet = u22 * u33 - u23 * u23;
    u22 /= udet;
    u23 /= udet;
    u33 /= udet;
  }

  // void serialize( PrintWriter out )
  // {
  //   out.format(Locale.US, "F %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f\n",
  //               un.x, un.y, un.z, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z, v3.x, v3.y, v3.z );
  // }

  
  /* returns true if P is a vertex of the facet
   */
  boolean contains( CWPoint p ) { return p == v1 || p == v2 || p == v3; } 
  
  boolean hasPointAbove( Vector3D v ) { return isProjectionInside( v.difference(v1) ); }
  
  double maxAngleOfPoint( Vector3D p )
  {
	  Vector3D pp = p.difference(v1);
	  double c1 = un.dotProduct( pp ) / pp.length();
	  pp = p.difference(v2);
	  double c2 = un.dotProduct( pp ) / pp.length();
	  pp = p.difference(v3);
	  double c3 = un.dotProduct( pp ) / pp.length();
	  if ( c2 > c1 ) c1 = c2;
	  if ( c3 > c1 ) c1 = c3;
	  return Math.acos(c1);
  }
  
  /** compute the area of the facet
   * @return area
   */
  double area() { return Math.abs( u2.crossProduct(u3).length() ); }

  /** compute the volume of the tetrahedrom of this triangle and the point P0
   * @param p0    point "external" to the triangle
   * @return volume
   * @note the volume has sign: since the triangle is directed towards the inside of the CW the volume is
   *       positive if the point is on-the-"inside" the CW 
   */
  double volume( Vector3D p0 ) { return u.dotProduct( p0.difference(v1) ); }
  
  /** solid angle of the triangle as seen from a point
   * A. van Oosterom, J. Strackee "A solid angle of a plane traiangle" IEEE Trans. Biomed. Eng. 30:2 1983 125-126
   */
  double solidAngle( Vector3D p )
  {
    Vector3D p1 = v1.difference(p);  p1.normalized();
    Vector3D p2 = v2.difference(p);  p2.normalized();
    Vector3D p3 = v3.difference(p);  p3.normalized();
    double s = p1.crossProduct(p3).dotProduct(p2);
    double c = 1 + p1.dotProduct(p2) + p2.dotProduct(p3) + p3.dotProduct(p1);
    return 2 * Math.atan2( s, c );
  }
  
  /* returns true if
   * - the vector P is on the surface of the triangle (eps = 0.001)
   * - and it projects inside the triangle
   */
  boolean isPointInside( Vector3D p, double eps )
  {
    Vector3D v0 = p.difference(v1);
    if ( Math.abs( u.dotProduct(v0) ) > eps ) return false;
    return isProjectionInside( v0 );
  }
  
  /** 
   * @param list   list of points
   * @return true if the three vertices of the triangle are in the list
   */
  boolean hasVerticesInList( List<CWPoint> list )
  {
    return list.contains( v1 ) && list.contains( v2 ) && list.contains( v3 );
  }
  
  /** count number of vertices in the list
   * 
   * @param list  list of points
   * @param pts   vertices of the triangle in the list
   * @return number of vertices in the list
   */
  int countVertexIn( List<CWPoint> list, CWPoint[] pts )
  {
    int n = 0;
    if ( list.contains( v1 ) ) pts[n++] = v1;
    if ( list.contains( v2 ) ) pts[n++] = v2;
    if ( list.contains( v3 ) ) pts[n++] = v3;
    return n;
  }
	  
  /** computes the projection of V0 in the plane of the triangle
   * and checks if it lies inside the triangle
   * @param v0   vector 
   * @note v0 already reduced to v1.
   * @return true if the projection of v0 falls inside the triangle
   */
  protected boolean isProjectionInside( Vector3D v0 )
  {
    double v02 = v0.dotProduct( u2 );
    double v03 = v0.dotProduct( u3 );
    double a = u33 * v02 - u23 * v03;
    double b = u22 * v03 - u23 * v02;
    return ( a >= 0 && b >= 0 && (a+b) <= 1 );
  }
  
  /** intersection with the segment P1-P2
   * computes the point of the line P1+s*(P2-P1)
   * 
   * @param p1  first segment endpoint (inside)
   * @param p2  second segment endpoint (outside)
   * @return intersection point or null
   */
  Vector3D intersection( Vector3D p1, Vector3D p2, Double res )
  {
	  Vector3D dp = new Vector3D( p2.x-p1.x, p2.y-p1.y, p2.z-p1.z);
	  double dpu = u.x * dp.x + u.y * dp.y + u.z * dp.z;
	  // if ( Math.abs(dpu) < 0.001 ) return null;
	  Vector3D vp = new Vector3D( v1.x-p1.x, v1.y-p1.y, v1.z-p1.z);
	  double s = (u.x * vp.x + u.y * vp.y + u.z * vp.z)/dpu;
	  res = s;
	  if ( s < 0.0 || s > 1.0 ) return null;
	  Vector3D j = new Vector3D( p1.x+s*dp.x, p1.y+s*dp.y, p1.z+s*dp.z); // intersection point
	  Vector3D j0 = j.difference(v1);
	  if ( isProjectionInside(j0) ) return j;
	  return null;
  }
  
  /** get an intersection point with another facet
   *  the intersection line is P + alpha (N1 ^ N2)
   * @param t the other triangle
   * @return an intersection point 
   */
  Vector3D intersectionBasepoint( CWFacet t )
  {
	Vector3D ret = new Vector3D();
	Vector3D n = un.crossProduct( t.un );
	double vn1 = v1.dotProduct(un);
	double vn2 = t.v1.dotProduct(t.un);
	if ( Math.abs(n.x) > Math.abs(n.y) ) {
		if ( Math.abs(n.x) > Math.abs(n.z) ) { // solve Y-Z for X=0
			ret.y = (   t.un.z * vn1 - un.z * vn2 ) / n.x;
			ret.z = ( - t.un.y * vn1 + un.y * vn2 ) / n.x;
		} else { // solve X-Y for Z=0
			ret.x = (   t.un.y * vn1 - un.y * vn2 ) / n.z;
			ret.y = ( - t.un.x * vn1 + un.x * vn2 ) / n.z;
		}
	} else {
		if ( Math.abs(n.y) > Math.abs(n.z) ) { // solve Z-X for Y=0
			ret.z = (   t.un.x * vn1 - un.x * vn2 ) / n.y;
			ret.x = ( - t.un.z * vn1 + un.z * vn2 ) / n.y;
		} else { // solve X-Y for Z=0
			ret.x = (   t.un.y * vn1 - un.y * vn2 ) / n.z;
			ret.y = ( - t.un.x * vn1 + un.x * vn2 ) / n.z;
		}
	}
	// check
	// TDLog.v( "Facet t " + mCnt + " intersection with " + t.mCnt + " " 
	//    + un.dotProduct(ret.difference(v1)) + " " + t.un.dotProduct(ret.difference(t.v1)) );
	return ret;
  }
  
  Vector3D intersectionDirection( CWFacet t ) { return un.crossProduct( t.un ); }
  
  protected double beta1 ( Vector3D v, Vector3D n ) { return beta( n, u1, v2.difference(v) ); }
  protected double beta2 ( Vector3D v, Vector3D n ) { return beta( n, u2, v1.difference(v) ); }
  protected double beta3 ( Vector3D v, Vector3D n ) { return beta( n, u3, v1.difference(v) ); }
  
  protected double alpha1 ( Vector3D v, Vector3D n ) { return alpha( n, u1, v2.difference(v) ); }
  protected double alpha2 ( Vector3D v, Vector3D n ) { return alpha( n, u2, v1.difference(v) ); }
  protected double alpha3 ( Vector3D v, Vector3D n ) { return alpha( n, u3, v1.difference(v) ); }
  
  /** compute line param for the intersection point of
   * V + alpha N and VV + beta U
   * the equations are
   *    alpha N*N - beta U*N =  (VV-V)*N
   *   -alpha U*N + beta U*U = -(VV-V)*U
   * therefore
   *    alpha    U*U  U*N        (VV-V)*N
   *    beta  =  U*N  N*N times -(VV-V)*U divided (N*N * U*U - U*n * U*N)
   * @param n   see above eq.
   * @param u   see above eq.
   * @param vv  see above eq.
   * @return value of beta (if in [0,1] the intersection point is on the triangle side] 
   */
  private double beta( Vector3D n, Vector3D u, Vector3D vv )
  {
    double nu = n.dotProduct(u);
    double nn = n.dotProduct(n);
    double uu = u.dotProduct(u);
    double nv = n.dotProduct( vv );
    double uv = u.dotProduct( vv );
    return ( nu * nv - nn * uv ) / ( nn * uu - nu * nu );
  }
  
  private double alpha( Vector3D n, Vector3D u, Vector3D vv )
  {
    double nu = n.dotProduct(u);
    double nn = n.dotProduct(n);
    double uu = u.dotProduct(u);
    double nv = n.dotProduct( vv );
    double uv = u.dotProduct( vv );
    return ( uu * nv - nu * uv ) / ( nn * uu - nu * nu );
  }
  
}
