/** @file Cave3DHull.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief convex hull of the 2D projs of splays on the plane normal to the shot
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 */
package com.topodroid.c3walls.hull;

// import com.topodroid.utils.TDLog;

import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.Triangle3D;

import java.util.ArrayList;

class Cave3DHull
{
  Cave3DShot    shot;    
  Cave3DStation mStationFrom;  // base station
  Cave3DStation mStationTo;
  Vector3D  normal;   // normal to the plane (unit vector along the shot)
  Vector3D  center;   // hull center (in the plane)
  ArrayList< Cave3DShot > rays1;  
  ArrayList< Cave3DShot > rays2;  
  ArrayList< Triangle3D > triangles;
  ArrayList< HullProjection > projs1;
  ArrayList< HullProjection > projs2;
  HullAngle afrom;
  HullAngle ato;
  int color; // DEBUG

  // /** get the size of the projections
  //  * @param k    proj index: 0 at FROM, 1 at TO
  //  */
  // int projSize( int k ) { return (k==0)? projs1.size() : projs2.size(); }

  int size() { return triangles == null ? 0 : triangles.size(); }

  Cave3DHull( Cave3DShot sh,                   // shot
              ArrayList< Cave3DShot > splays1, // splays at FROM station
              ArrayList< Cave3DShot > splays2, // splays at TO station
              Cave3DStation sf,                // shot FROM station
              Cave3DStation st,                // shot TO station
              HullAngle af,
              HullAngle at )
  {
    // TDLog.v( "Hull at station " + st.name + " shot " + sh.from_station.name + "-" + sh.to_station.name );
    mStationFrom = sf;
    mStationTo   = st;
    shot   = sh;
    rays1  = splays1;
    rays2  = splays2;
    projs1 = new ArrayList< HullProjection >();
    projs2 = new ArrayList< HullProjection >();
    afrom  = af;
    ato    = at;

    // TDLog.v("ANGLE Shot " + sf.short_name + "-" + st.short_name + " ber " + (sh.ber * 180/Math.PI) );
    // afrom.log( sf.short_name );
    // ato.log(   st.short_name );

    normal = shot.toVector3D(); // (E,N,Up)
    normal.z = 0;  // make normal horizontal (Up = 0)
    normal.normalized();

    computeHull();
  }

  // void dumpHull()
  // {
  //   int s1 = projs1.size();
  //   int s2 = projs2.size();
  //   TDLog.v("Hull at station " + mStationFrom.getFullName() + " size " + s1 + " " + mStationTo.getFullName() + " " + s2 );
  //   // for (int k=0; k<s1; ++k ) {
  //   //   HullProjection p = projs1.get(k);
  //   //   TDLog.v( "Hull: " + k + ": " + p.angle + " - " + p.proj.x + " " + p.proj.y + " " + p.proj.z );
  //   // }
  //   // for (int k=0; k<s2; ++k ) {
  //   //   HullProjection p = projs2.get(k);
  //   //   TDLog.v( "Hull: " + k + ": " + p.angle + " - " + p.proj.x + " " + p.proj.y + " " + p.proj.z );
  //   // }
  // }

  private void addTriangle( HullProjection p1, HullProjection p2, HullProjection p3 )
  {
    triangles.add( new Triangle3D( p1.vector, p2.vector, p3.vector, color ) );
  }

  // N.B. the two arrays are sorted by the angle
  private void makeTriangles()
  {
    triangles = new ArrayList< Triangle3D >();
    int s1 = projs1.size();
    int s2 = projs2.size();
    // TDLog.v("Hull at station " + mStationFrom.short_name + " size " + s1 + " " + mStationTo.name + " " + s2 );
    
    if ( s1 == 0 || s2 == 0 ) return;
    if ( s1 == 1 && s2 == 1 ) return;
    // at least one projection in each array and at least one array with two projections
    int k1 = 0;
    int k2 = 0;
    HullProjection p1 = projs1.get(0);
    HullProjection p2 = projs2.get(0);
    projs1.add( new HullProjection( p1, Math.PI*2 ) );
    projs2.add( new HullProjection( p2, Math.PI*2 ) );

    while ( k1 < s1 || k2 < s2 ) {
      if ( k1 == s1 ) {  // next point on projs2
        k2 ++;
        HullProjection q2 = projs2.get( k2 );
        addTriangle( p1, p2, q2 );
        p2 = q2;
      } else if ( k2 == s2 ) { // next point on projs1
        k1 ++;
        HullProjection q1 = projs1.get( k1 );
        addTriangle( p1, p2, q1);
        p1 = q1;
      } else { // must choose
        HullProjection q1 = projs1.get( (k1+1) );
        HullProjection q2 = projs2.get( (k2+1) );
        if ( q1.angle < q2.angle ) {
          k1++;
          addTriangle( p1, p2, q1 );
          p1 = q1;
        } else {
          k2++;
          addTriangle( p1, p2, q2 );
          p2 = q2;
        }
      }
    }
    // TDLog.v("Hull at station " + mStationFrom.short_name + " size " + s1 + " " + mStationTo.name + " " + s2 + " triangles " + triangles.size() );
  }

  /** make triangles from the HULL to a vertex
   * @param vertex   vertex
   */
  void makeTriangles( Vector3D vertex )
  {
    triangles = new ArrayList< Triangle3D >();
    int s1 = projs1.size();
    if ( s1 < 2 ) return;
    // TDLog.v( "Hull: triangles at " + mStationFrom.name + " with vertex. Nr triangles " + s1 );
    for ( int k=0; k<s1; ++k ) {
      HullProjection p1 = projs1.get(k);
      HullProjection p2 = projs1.get((k+1)%s1);
      Vector3D v1 = vertex.sum( p1.proj );
      Vector3D v2 = vertex.sum( p2.proj );
      triangles.add( new Triangle3D( p1.vector, p2.vector, v2, color ) );
      triangles.add( new Triangle3D( p1.vector, v2, v1, color ) );
    }
  }

  private void computeHull()
  {
    // TDLog.v( "compute Hull [0]: splays " + rays1.size() + " " + rays2.size() );

    computeHullProjs( rays1, projs1, mStationFrom, afrom );
    computeHullProjs( rays2, projs2, mStationTo,   ato );
    // TDLog.v( "compute Hull [1]: projs " + projs1.size() + " " + projs2.size() );

    Vector3D p0 = null; // pick a reference projection vector
    if ( projs1.size() > 1 ) {
      p0 = new Vector3D( projs1.get(0).proj );
    } else if ( projs2.size() > 1 ) {
      p0 = new Vector3D( projs2.get(0).proj );
    } else {
      return;
    }
    double pn = normal.dotProduct( p0 );
    p0.x -= pn * normal.x;
    p0.y -= pn * normal.y;
    // p0.z -= pn * normal.z; // normal.z == 0
    p0.normalized();

    // TDLog.v("Hull [1] Ref V0 " + p0.x + " " + p0.y + " " + p0.z );
    // TDLog.v( "compute Hull [1]: " + mStationFrom.short_name + " rays " + rays1.size() + " projs " + projs1.size() );
    computeAnglesAndSort( p0, projs1 );
    // TDLog.v( "compute Hull [2]: " + mStationTo.short_name   + " rays " + rays2.size() + " projs " + projs2.size() );
    computeAnglesAndSort( p0, projs2 );


    removeInsideProjs( projs1 );
    removeInsideProjs( projs2 );
    // TDLog.v( "compute Hull after remove-inside: " + projs1.size() + " " + projs2.size() );

    makeTriangles();

    // TDLog.v( "compute Hull [3]: projs " + projs1.size() + " " + projs2.size() );
  }

  // compute the projections of the rays at a given station
  // @param rays    rays (splays) at the station
  // @param projs   result projections (array)
  // @param st      station
  // @param dir     outside direction (-1 at FROM, 1 at TO)
  private void computeHullProjs( ArrayList< Cave3DShot > rays, ArrayList< HullProjection > projs, Cave3DStation st, HullAngle angles )
  {
    // N.B. the HullProjection has a vector that depends on setting mSplayProj (ie use splay as 3D vector or on the projection plane)
    //      but its projection vector is indepenedent
    // Vector3D right = new Vector3D( Math.sin( angles.a1 ), -Math.cos( angles.a1 ), 0 ); // normal to the right
    // Vector3D left  = new Vector3D( Math.sin( angles.a2 ), -Math.cos( angles.a2 ), 0 ); // normal to the left
    // TDLog.v("Hull compute projs ");
    // angles.dump();

    for ( Cave3DShot splay : rays ) {
      // int inside = angles.isInside( splay );
      // if ( inside == 0 ) continue;
      Vector3D v  = splay.toVector3D();
      Vector3D pv = angles.projectSplay( splay, v );
      if ( pv != null ) {
        // projs.add( new HullProjection( st, splay, v, (inside == 1)? right : left ) ); 
        projs.add( new HullProjection( st, splay, v, pv ) );
      }
    }

    // compute projections 3D center and refer projected vectors to the center
    center = new Vector3D( 0, 0, 0 );
    for ( HullProjection p : projs ) center.add( p.proj );
    center.scaleBy( 1.0f/projs.size() );

    for ( HullProjection p : projs ) p.proj.subtracted( center );
  }
 
  // @param ref     reference unit (projection) vector
  // @param projs   array of projections
  // the projections "angle" are computed and the array is sorted by increasing angles
  private void computeAnglesAndSort( Vector3D ref, ArrayList< HullProjection > projs )
  {
    // normalize projected vectors and compute the angles
    int s = projs.size();
    for (int k=0; k<s; ++k ) 
    {
      Vector3D p1 = new Vector3D( projs.get(k).proj );
      p1.normalized();
      projs.get(k).angle = computeAngle( ref, p1 );
    }
    
    // simple sort of the projections by the angle
    if ( s <= 1 ) return;
    boolean repeat = true;
    while ( repeat ) {
      repeat = false;
      for ( int k=0; k<s-1; ++k ) {
        HullProjection p1 = projs.get(k);
        HullProjection p2 = projs.get(k+1);
        if ( p1.angle > p2.angle ) {
          projs.set(k, p2 );
          projs.set(k+1, p1 );
          repeat = true;
        }
      }
    }
    for ( HullProjection proj : projs ) proj.dump();
  }

  // @pre projections are sorted by the angle
  // TODO it might be useful to allow a bit of concavity 
  private void removeInsideProjs( ArrayList< HullProjection > projs )
  {
    int s = projs.size();
    if ( s <= 3 ) return;
    // int k1 = s - 1;
    int k2 = 0;
    int k3 = 1;
    HullProjection p1 = projs.get( s-1 );
    HullProjection p2 = projs.get( k2 );
    while ( k2 < projs.size() && projs.size() > 3 ) {
      HullProjection p3 = projs.get( k3 % projs.size() );
      Vector3D v21 = p1.proj.difference( p2.proj );  // P1 - P2
      Vector3D v23 = p3.proj.difference( p2.proj );  // P3 - P2
      double d = normal.dotProduct( v21.crossProduct(v23) );
      if ( d > 0 ) {  // if the dot-product is positive P2 is inside the triangle (0,P1,P3) 
        projs.remove( k2 );
        // do not increase indices k2/k3
      } else {
        p1 = p2;
        ++k2;
        ++k3;
      }
      p2 = p3;
    }
  }

  // angle between two unit vectors in the projection plane - radians [0, 2 PI]
  private double computeAngle( Vector3D p0, Vector3D p1 )
  {
    Vector3D p = normal.crossProduct( p1 );
    double cc = p0.dotProduct(p);
    double ss = normal.dotProduct( p0.crossProduct( p ) );
    double a = Math.atan2( ss, cc );
    if ( a >= 2*Math.PI ) a -= 2*Math.PI;
    if ( a < 0 )          a += 2*Math.PI;
    return a;
  }


}
