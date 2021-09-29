/* @file Bubble.java
 *
 * @author marco corvi
 * @date may 2021
 *
 * @brief bubble wall model
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.bubble;

import com.topodroid.utils.TDLog;

import com.topodroid.DistoX.Vector3D;
import com.topodroid.DistoX.Cave3DStation;

import java.util.ArrayList;

public class Bubble
{
  private ArrayList< Point3S > mPts;
  private ArrayList< Triangle3S > mTri;
  private Cave3DStation mCenter;

  // public Bubble( Cave3DStation center, ArrayList< Vector3D > vts )
  // {
  //   mCenter = center;
  //   mPts = new ArrayList< Point3S >();
  //   for ( Vector3D v : vts ) mPts.add( new Point3S( v, mCenter ) );
  // }

  public Bubble( Cave3DStation center, ArrayList< Point3S > pts )
  {
    mCenter = center;
    mPts    = pts;
  }

  public boolean prepareDelaunay()
  {
    if ( ! reorderPoints( mPts ) ) return false;
    mTri = Delaunay3S.computeConvexHull( mPts );
    // addTrianglesToPoints();
    return true;
  }

  public ArrayList< Triangle3S > getTriangles() { return mTri; }

  public Cave3DStation getCenter() { return mCenter; }

  synchronized public ArrayList< Point3S > getPoints() { return mPts; }

  public boolean contains( Vector3D v )
  {
    for ( Triangle3S tr : mTri ) {
      if ( tr.contains( v, mCenter ) ) return true;
    }
    return false;
  }

  public ArrayList<Triangle3S> computeInsides( Bubble bb )
  {
    for ( Point3S pt : mPts ) pt.inside = bb.contains( pt.v );
    ArrayList<Triangle3S> tri2 = new ArrayList<>();
    for ( Triangle3S tr : mTri ) {
      if ( tr.countInside() > 0 ) tri2.add( tr );
    }
    return tri2;
  }

  public void reduce( ArrayList<Triangle3S> tri2, ArrayList<Triangle3S> tri3 )
  {
    for ( Triangle3S tr : tri2 ) mTri.remove( tr );
    for ( Triangle3S tr : tri3 ) mTri.add( tr );
  }

  public ArrayList<Triangle3S> computeReducedTriangles( ArrayList<Triangle3S> tri2, Bubble bb )
  {
    int n1 = 0;
    int n2 = 0;
    int n3 = 0;
    ArrayList<Triangle3S> tri3 = new ArrayList<>();
    for ( Triangle3S tr : tri2 ) {
      if ( tr.inside == 3 ) { ++n3; continue; }
      Point3S p1 = tr.pt1();
      Point3S p2 = tr.pt2();
      Point3S p3 = tr.pt3();
      if ( tr.inside == 1 ) { // make P1 in --- P2, P3 out
        ++n1;
        if ( p2.inside ) {
          p1 = p2;
          p2 = p3;
          p3 = tr.pt1();
        } else if ( p3.inside ) {
          p1 = p3;
          p3 = p2;
          p2 = tr.pt1();
        }
        Vector3D v2 = bb.intersectionP( p1, p2 );
        Vector3D v3 = bb.intersectionP( p1, p3 );
        // replace triangle
        //     P3 ----- P2
        //      \ `.    /
        //       \  `. /
        //       V3---V2
        //         \ /
        //          P1
        if ( v2 != null && v3 != null ) {
          Point3S p13 = new Point3S( v3, mCenter );
          Point3S p12 = new Point3S( v2, mCenter );
          tri3.add( new Triangle3S( p12, p2, p3 ) );
          tri3.add( new Triangle3S( p3, p13, p12 ) );
        } else {
          TDLog.Error("one point inside but no intersections");
        }
      } else if ( tr.inside == 2 ) { // make P1 outside, P2, P3 inside
        ++n2;
        if ( ! p2.inside ) {
          p1 = p2;
          p2 = p3;
          p3 = tr.pt1();
        } else if ( ! p3.inside ) {
          p1 = p3;
          p3 = p2;
          p2 = tr.pt1();
        }
        Vector3D v2 = bb.intersectionP( p1, p2 );
        Vector3D v3 = bb.intersectionP( p1, p3 );
        if ( v2 != null && v3 != null ) {
          Point3S p13 = new Point3S( v3, mCenter );
          Point3S p12 = new Point3S( v2, mCenter );
          tri3.add( new Triangle3S( p1, p12, p13 ) );
        } else {
          TDLog.Error("two points inside but no intersections");
        }
      }
    }
    return tri3;
  }

  // find an intersection with the segment v1--v2
  // NOTE there is one intersection (v1 inside, c2 outside), but there may be more
  public Vector3D intersection( Vector3D v1, Vector3D v2 )
  {
    for ( Triangle3S tr : mTri ) {
      Vector3D v = tr.intersection( v1, v2 );
      if ( v != null ) return v;
    }
    return null;
  }

  public Vector3D intersectionP( Point3S p1, Point3S p2 ) { return intersection( p1.v, p2.v ); }

  // public void addTrianglesToPoints()
  // {
  //   for ( Triangle3S tr : mTri ) {
  //     tr.pt1().addTriangle( tr );
  //     tr.pt2().addTriangle( tr );
  //     tr.pt3().addTriangle( tr );
  //   }
  // }

  // ----------------------------------------------------------------
  // utility fcts

  static private boolean reorderPoints( ArrayList<Point3S> pts )
  {
    // find the four vertices of the max volume tetrahedron
    int kmax = pts.size();
    if ( kmax < 4 ) return false;
    int k00 = 0;
    int k11 = 1;
    int k22 = 2;
    int k33 = 3;
    Point3S pt0 = pts.get(k00);
    Vector3D v1 = pts.get(k11).difference( pt0 );
    Vector3D v2 = pts.get(k22).difference( pt0 );
    Vector3D v12 = v1.crossProduct( v2 );
    Vector3D v3 = pts.get(k33).difference( pt0 );
    double area = Math.abs( v12.dotProduct( v3 ) );
    for ( int k0 = 0; k0 < kmax; ++k0 ) {
      pt0 = pts.get(k0);
      for ( int k1 = k0+1; k1 < kmax; ++k1 ) {
        v1 = pts.get(k1).difference( pt0 );
        for ( int k2 = k1+1; k2 < kmax; ++ k2 ) {
          v2 = pts.get(k2).difference( pt0 );
          v12 = v1.crossProduct( v2 );
          for ( int k3 = k2+1; k3 < kmax; ++k3 ) { 
            v3 = pts.get(k3).difference( pt0 );
            double a = Math.abs( v12.dotProduct( v3 ) );
            if ( a > area ) {
              area = a;
              k00 = k0;
              k11 = k1;
              k22 = k2;
              k33 = k3;
            }
          }
        }
      }
    }
    pt0 = pts.get(k00);
    v1 = pts.get(k11).difference( pt0 );
    v2 = pts.get(k22).difference( pt0 );
    v12 = v1.crossProduct( v2 );
    v3 = pts.get(k33).difference( pt0 );
    if ( v12.dotProduct( v3 ) > 0 ) {
      int k = k11; k11 = k22; k22 = k;
    }
    if ( k00 != 0 ) swapPoints(pts, 0, k00 );
    if ( k11 != 1 ) swapPoints(pts, 1, k11 );
    if ( k22 != 2 ) swapPoints(pts, 2, k22 );
    if ( k33 != 3 ) swapPoints(pts, 3, k33 );
    return true;
  }

  static private void swapPoints( ArrayList<Point3S> pts, int k1, int k2 )
  {
    Point3S p1 = pts.get( k1 );
    Point3S p2 = pts.get( k2 );
    pts.set( k1, p2 );
    pts.set( k2, p1 );
  }
    
}
