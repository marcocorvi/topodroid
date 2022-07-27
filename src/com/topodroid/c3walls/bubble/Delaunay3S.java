/* @file Delaunay3S.java
 *
 * @author marco corvi
 * @date may 2021
 *
 * @brief delaunay on the 3D sphere
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.bubble;

import com.topodroid.utils.TDLog;

import java.util.ArrayList;

public class Delaunay3S
{
  static public ArrayList<Triangle3S> computeConvexHull( ArrayList<Point3S> pts )
  {
    ArrayList<Triangle3S> trs = initTriangles( pts );
    for ( int k = 4; k < pts.size(); ++ k ) {
      Point3S pt = pts.get( k );
      ArrayList<Triangle3S> trs2 = getTrianglesBelowPoint( trs, pt );
      if ( trs2.size() == 0 ) {
        TDLog.Error( "Point inside convex hull ");
        continue;
      }
      ArrayList<HalfSide> hsd2 = getBorder( trs2 );
      for ( Triangle3S tr : trs2 ) trs.remove( tr );
      ArrayList<Triangle3S> trs3 = getPointTriangles( hsd2, pt );
      linkTriangles( trs3, hsd2 );
      for ( Triangle3S tr : trs3 ) trs.add( tr );
    }
    return trs;
  }

  // -----------------------------------------------------------------------------
  static private ArrayList<Triangle3S> initTriangles( ArrayList<Point3S> pts )
  {
    ArrayList< Triangle3S > trs = new ArrayList< Triangle3S >();
    // initialize with a tetrahedron
    Triangle3S t00 = new Triangle3S( pts.get(0), pts.get(1), pts.get(2) );
    Triangle3S t12 = new Triangle3S( pts.get(0), pts.get(3), pts.get(1) );
    Triangle3S t23 = new Triangle3S( pts.get(1), pts.get(3), pts.get(2) );
    Triangle3S t31 = new Triangle3S( pts.get(2), pts.get(3), pts.get(0) );
    
    // Triangle3S t00 = new Triangle3S( t0.pt1(), t0.pt2(), t0.pt3(), t12, t23, t31 );
    t00.setNghbs( t12, t23, t31 );
    t12.setNghbs( t31, t23, t00 );
    t23.setNghbs( t12, t31, t00 );
    t31.setNghbs( t23, t12, t00 );
    trs.add( t00 );
    trs.add( t12 );
    trs.add( t23 );
    trs.add( t31 );
    // assert( t00.dotProduct( pts.get(3) ) < 0 );
    // assert( t12.dotProduct( pts.get(2) ) < 0 );
    // assert( t23.dotProduct( pts.get(0) ) < 0 );
    // assert( t31.dotProduct( pts.get(1) ) < 0 );
    // this guarantees the initial tetrahedron is well-oriented
    return trs;
  }

  // insert the other points
  static private ArrayList<Triangle3S> getTrianglesBelowPoint( ArrayList<Triangle3S> trs, Point3S pt )
  {
    // find the triangle that contains pt
    ArrayList< Triangle3S > trs2 = new ArrayList<>();
    for ( Triangle3S tr : trs ) {
      if ( tr.volume( pt ) > 0 ) trs2.add( tr );
    }
    return trs2;
  }

  // find the border sides
  static private ArrayList<HalfSide> getBorder( ArrayList<Triangle3S> trs2 )
  {
    ArrayList<HalfSide> hsd2 = new ArrayList<>();
    for ( Triangle3S tr : trs2 ) {
      if ( ! trs2.contains( tr.t12 ) ) {
        hsd2.add( new HalfSide( tr.pt1(), tr.pt2(), tr.t12 ) );
      }
      if ( ! trs2.contains( tr.t23 ) ) {
        hsd2.add( new HalfSide( tr.pt2(), tr.pt3(), tr.t23 ) );
      }
      if ( ! trs2.contains( tr.t31 ) ) {
        hsd2.add( new HalfSide( tr.pt3(), tr.pt1(), tr.t31 ) );
      }
    }

    // reorder border
    int sz = hsd2.size();
    HalfSide hs0 = hsd2.get(0);
    for ( int k = 1; k<sz-1; ++k ) {
      HalfSide hs1 = hsd2.get(k-1);
      HalfSide hs2 = hsd2.get(k);
      if ( hs1.p2 != hs2.p1 ) {
        for ( int kk = k+1; kk < sz; ++kk ) {
          if ( hsd2.get(kk).p1 == hs1.p2 ) { // exchange
            hsd2.set( k, hsd2.get(kk) );
            hsd2.set( kk, hs2 );
            break;
          }
        }
      }
    }
    // assert( hsd2.get( sz-1 ).p2 == hs0.p1 ); // check border is closed
    return hsd2;
  }

    // insert triangles
    // 
    //         new_pt
    //         .    .
    //     -- p1 -- p2 --
    //        |  tr  |
  static private ArrayList<Triangle3S> getPointTriangles( ArrayList<HalfSide> hsd2, Point3S pt )
  {
    int sz = hsd2.size();
    ArrayList<Triangle3S> trs3 = new ArrayList<>();
    for ( int k = 0; k<sz; ++k ) {
      HalfSide hs = hsd2.get(k);
      Triangle3S tr = new Triangle3S( hs.p1, hs.p2, pt );
      hs.tr.setNghb( hs.p2, hs.p1, tr );
      trs3.add( tr );
    }
    // assert( trs3.size() == sz );
    return trs3;
  }

  // link triangles and put them on the main list
  static private void linkTriangles( ArrayList<Triangle3S> trs3, ArrayList<HalfSide> hsd2 )
  {
    int sz = trs3.size();
    for ( int k0 = 0; k0 <sz; ++k0 ) {
      int k1 = (k0 > 0)? k0 - 1: sz - 1;
      int k2 = (k0 < sz-1)? k0 + 1: 0;
      Triangle3S tr1 = trs3.get( k1 );
      Triangle3S tr2 = trs3.get( k2 );
      Triangle3S tr0 = hsd2.get( k0 ).tr;
      trs3.get( k0 ).setNghbs( tr0, tr2, tr1 );
    }
  }

}
