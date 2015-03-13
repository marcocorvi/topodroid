/** @file SketchTriangle.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: 3D surface triangle
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130224 created 
 */
package com.topodroid.DistoX;

import android.graphics.PointF;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import android.util.FloatMath;
import android.util.Log;

class SketchTriangle 
{
  // int type;       // 1: (n1, n1+1, n2), 2: (n1, n2+1, n2), 0: others
  boolean highlight;    // whether this triangle is highlighted
  boolean inside;       // whether this triangle is inside the edit-border
  boolean splitted;     // whether this triangle has been splitted
  int i, j, k;          // vertex indices
  // int sjk, ski, sij; // side indices ( side sij is opposite to vertex k )
  SketchVertex  v1, v2, v3;
  SketchSurface surface;
  Vector center; // 3D center of the triangle
  Vector normal;
  PointF p1, p2, p3;     // canvas-projected vertex points (scene coords)

  float cosine1;  // cosine angle v2-v1-v3
  float cosine2;
  float cosine3;

  Vector w12;  // unit vector (v1 - v2)
  Vector w23;  // unit vector (v2 - v3)
  Vector w31;

  // N.B. side s23 is opposite to vertex 1 (i0)
  SketchTriangle( SketchSurface parent, 
                  int i0, int j0, int k0,
                  SketchVertex v10, SketchVertex v20, SketchVertex v30 ) //,
                  // int s23, int s31, int s12 )
  {
    surface = parent;
    highlight = false;
    inside    = false;
    i = i0;
    j = j0;
    k = k0;
    v1 = v10;
    v2 = v20;
    v3 = v30;
    // sjk = s23;
    // ski = s31;
    // sij = s12;
    center = new Vector( (v1.x+v2.x+v3.x)/3, (v1.y+v2.y+v3.y)/3, (v1.z+v2.z+v3.z)/3 );

    w12 = v1.minus( v2 ); w12.Normalized();
    w31 = v3.minus( v1 ); w31.Normalized();
    w23 = v2.minus( v3 ); w23.Normalized();

    // float x1 = v1.x - v2.x;
    // float y1 = v1.y - v2.y;
    // float z1 = v1.z - v2.z;
    // float x3 = v3.x - v2.x;
    // float y3 = v3.y - v2.y;
    // float z3 = v3.z - v2.z;
    // normal = new Vector( y3*z1-y1*z3, z3*x1-z1*x3, x3*y1-y3*x1 );
    normal = w12.cross( w23 );
    normal.Normalized();

    p1 = new PointF(0,0);
    p2 = new PointF(0,0);
    p3 = new PointF(0,0);
    // check
    // float c1 = normal.x*(v1.x - v2.x) + normal.y*(v1.y - v2.y) + normal.z*(v1.z - v2.z);
    // float c3 = normal.x*(v3.x - v2.x) + normal.y*(v3.y - v2.y) + normal.z*(v3.z - v2.z);
    // if ( Math.abs(c1) > 0.01 || Math.abs(c3) > 0.01 ) {
    //   Log.v("DistoX", "fail tri normal: " + c1 + " " + c3 );
    // }

    cosine1 = - w31.dot( w12 );
    cosine2 = - w12.dot( w23 );
    cosine3 = - w23.dot( w31 );
  }

  // plane: ( X - Xc ) * N = 0
  // line:  X = W1 + L * (W2 - W1) 
  // intersection: (W1-Xc)*N = L * (W1-W2)*N
  // ==> L = (W1-Xc)*N / (W1-W2)*N
  //
  Vector intersection( Vector w1, Vector w2 )
  {
    float d1 = normal.dot( w1.minus( center ) );
    Vector z12 = w1.minus(w2);
    float d2 = normal.dot( z12 );
    if ( d1 * d2 <= 0 || Math.abs(d1) > Math.abs(d2) ) return null;
    z12.times( d1/d2 );
    Vector w0 = w1.minus( z12 );

    Vector w = w0.minus( v1 ); w.Normalized();
    if ( w31.dot( w ) < cosine1 ) return null;

    w = w0.minus( v2 ); w.Normalized();
    if ( w12.dot( w ) < cosine2 ) return null;

    w = w0.minus( v3 ); w.Normalized();
    if ( w23.dot( w ) < cosine3 ) return null;

    // Log.v("DistoX", "intersection with plane at " + -d1/d2 + " vector " + w0.x + " " + w0.y + " " + w0.y );
    return w2.minus( w0 );
  }

  void shiftVertices( Vector v )
  {
    v1.add( v );
    v2.add( v );
    v3.add( v );
    center.add( v );
  }

  // dot product of the normal with a vector
  float dotNormal( float x, float y, float z )
  {
    return x * normal.x + y * normal.y + z * normal.z;
  }

  float dotCenter( float x, float y, float z )
  {
    return x * center.x + y * center.y + z * center.z;
  }

  // distance of a 3D point from the center of the triangle
  float distanceCenter( Vector v )
  {
    return center.distance( v ); 
  }

  // distance of a 3D point from the plane of the triangle
  float distancePlane( Vector v ) 
  {
    return normal.dot( v.minus( center ) );
  }

  // get the 3D point in the plane of the triangle
  // that corresponds to a 2D scene point inside the projection of the triangle
  // (a,b,c) barycentric coords of P=(x,y,z):
  // P = a P1 + b P2 + c P3
  //
  Vector get3dPoint( float x, float y ) // (x,y) scene coords
  {
    float y23 = p2.y - p3.y;
    float y31 = p3.y - p1.y;
    float y12 = p1.y - p2.y;
    float x32 = p3.x - p2.x;
    float x13 = p1.x - p3.x;
    float x21 = p2.x - p1.x;
    float det = p1.x*y23 + p2.x*y31 + p3.x*y12;
    float a = (y23 * x + x32 * y + p2.x*p3.y-p3.x*p2.y)/det;
    float b = (y31 * x + x13 * y + p3.x*p1.y-p1.x*p3.y)/det;
    float c = (y12 * x + x21 * y + p1.x*p2.y-p2.x*p1.y)/det;
    Vector ret = new Vector( a*v1.x+b*v2.x+c*v3.x,
                             a*v1.y+b*v2.y+c*v3.y,
                             a*v1.z+b*v2.z+c*v3.z );
    // check
    // float c3 = normal.x*(ret.x-v2.x) + normal.y*(ret.y-v2.y) + normal.z*(ret.z-v2.z);
    // if ( Math.abs(c3) > 0.01 || Math.abs(a+b+c-1) > 0.01 ) {
    //   Log.v("DistoX", "fail tri vector normal: " + c3 + " coords " + a + " " + b + " " + c );
    // }
    return ret;
  }

  /** check if this triangle contais a scene point X=(x,y)
   * compute the three cross-products:
   *     (X - P1) ^ (P2 - P1)
   *     (X - P2) ^ (P3 - P2)
   *     (X - P3) ^ (P1 - P3)
   * if they have all the same sign the point X is on the same side of every
   * side of the triangle
   *                + P1
   *               / \
   *              /   \    neg. side
   *             /     \
   *  ------ P2 +--->---+ P3 -------
   *                       pos. side
   */
  int contains( float x, float y ) // (x,y) scene coords
  {
    float x1 = x - p1.x;
    float y1 = y - p1.y;
    float x2 = p2.x - p1.x;
    float y2 = p2.y - p1.y;
    float z1 = x1 * y2 - x2 * y1;
    x1 = x - p2.x;
    y1 = y - p2.y;
    x2 = p3.x - p2.x;
    y2 = p3.y - p2.y;
    float z2 = x1 * y2 - x2 * y1;
    x1 = x - p3.x;
    y1 = y - p3.y;
    x2 = p1.x - p3.x;
    y2 = p1.y - p3.y;
    float z3 = x1 * y2 - x2 * y1;
    if ( z1 > 0 && z2 > 0 && z3 > 0 ) return -1;
    if ( z1 < 0 && z2 < 0 && z3 < 0 ) return 1;
    return 0;
  }

  // check if the canvas-projected triangle is inside the border
  boolean isInside( ArrayList<PointF> border ) 
  {
    float a1 = angleAround( border, p1 );
    float a2 = angleAround( border, p2 );
    float a3 = angleAround( border, p3 );

    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // pw.format(Locale.ENGLISH, "T %.2f %.2f  %.2f %.2f  %.2f %.2f A %.2f %.2f %.2f",
    //     p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, a1, a2, a3 );
    // Log.v( "DistoX", sw.getBuffer().toString() );

    return Math.abs(a1) > 0.1f && Math.abs(a2) > 0.1f && Math.abs(a3) > 0.1f;
  }

  private float angleAround( ArrayList<PointF> border, PointF p )
  {
    float a = 0;
    int nb = border.size();
    PointF q = border.get(nb-1);
    float x1 = q.x - p.x;
    float y1 = q.y - p.y;
    if ( Math.abs(x1) < 0.001 && Math.abs(y1) < 0.001 ) return TopoDroidUtil.M_PI;
    for ( int k=0; k<nb; ++k ) {
      q = border.get(k);
      float x2 = q.x - p.x;
      float y2 = q.y - p.y;
      if ( Math.abs(x2) < 0.001 && Math.abs(y2) < 0.001 ) return TopoDroidUtil.M_PI;
      float s = x1*y2 - y1*x2;
      float c = x1*x2 + y1*y2;
      a += (float)Math.atan2( s, c );
      x1 = x2;
      y1 = y2;
    }
    // if ( Math.abs(a) < 0.001 ) a = 0;
    return a;
  }

  // ----------------------------------------------------------------
  // THERION three vertex indices and the three opposite side indices
  //     v1 v2 v3 s23 s31 s12

  void toTherion( PrintWriter pw )
  {
    // pw.format("  %d %d %d %d %d %d \n", i, j, k, sjk, ski, sij );
    pw.format("  %d %d %d\n", i, j, k );
  }
}
