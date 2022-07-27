/* @file Cave3DDelaunay.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief 3D: Delaunay-style triangulation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

//  import android.util.FloatMath;

// import java.io.*;
// import java.util.List;
import java.util.ArrayList;

class Cave3DDelaunay
{
  double eps = 0.001;
  int color; // DEBUG

  // normalized 3D vector
  class DelaunayPoint extends Vector3D
  {
    // int index; // debug
    Vector3D orig; // original vector
    // Vector3D v;    // normalized vector
    boolean used;
 
    DelaunayPoint( Vector3D vv, int kk ) 
    {
      super( vv );
      orig  = vv;
      // index = kk;
      // v = new Vector3D( vv.x, vv.y, vv.z );
      // v.normalized();
      this.normalized();
      used = false;
    }

    // double distance( DelaunayPoint p ) { return orig.distance( p.orig ); }
  }

  class DelaunaySide // half-side
  {
    DelaunayPoint    p1, p2;
    DelaunaySide     otherHalf;
    DelaunayTriangle t;
    Vector3D     n; // normal

    DelaunaySide( DelaunayPoint i0, DelaunayPoint j0 )
    {
      p1=i0;
      p2=j0;
      n = p1.crossProduct( p2 );
      n.normalized();
      t = null;
    }

    // boolean coincide( DelaunayPoint i0, DelaunayPoint j0 ) { return ( p1==i0 && p2==j0 ); }

    // boolean opposite( DelaunayPoint i0, DelaunayPoint j0 ) { return ( p2==i0 && p1==j0 ); }

    boolean isPositive( Vector3D v, double eps ) { return n.dotProduct( v ) > eps; }

    // boolean isNegative( Vector3D v, double eps ) { return n.dotProduct( v ) < -eps; }

    // boolean contains( Vector3D v, double eps ) { return Math.abs( n.dotProduct( v ) ) < eps; } 

    // double dot( Vector3D v ) { return n.dotProduct( v ); }
  }

  class DelaunayTriangle
  {
    DelaunaySide s1, s2, s3;
    // int   sign;
    private final double radius; // maximal inside the circumsphere
    private Vector3D center;

    DelaunayTriangle( DelaunaySide i0, DelaunaySide j0, DelaunaySide k0 )
    {
      s1 = i0;
      s2 = j0;
      s3 = k0;
      Vector3D v1 = s1.p1;
      Vector3D v2 = s2.p1.difference( v1 );
      Vector3D v3 = s3.p1.difference( v1 );
      // Vector3D v0 = v1.sum( s2.p1 ).sum( s3.p1 );
      center = v2.crossProduct( v3 ); // (v2-v1)^(v3-v1) = (v2^v3 + v3^v1 + v1^v2) normalized
      center.normalized();
      // sign = ( center.dotProduct(v0) > 0 )? -1 : +1;
      radius = center.dotProduct( v1 );
    }

    // /** copy cstr
    //  * @param t the triangle to copy
    //  */
    // DelaunayTriangle( DelaunayTriangle t )
    // {
    //   s1 = t.s1;
    //   s2 = t.s2;
    //   s3 = t.s3;
    //   // sign   = t.sign;
    //   radius = t.radius;
    //   center = t.center;
    // }

    // v = unit vector
    boolean circumcontains( Vector3D v, double eps ) { return center.dotProduct(v) > radius + eps; }

    // boolean contains( DelaunayPoint p ) { return contains( p.v ); }

    boolean contains( Vector3D v, double eps )
    { 
      // if ( sign < 0 ) {
      //   return s1.isNegative( v, eps ) || s2.isNegative( v, eps ) || s3.isNegative( v, eps );
      // }
      return s1.isPositive( v, eps ) && s2.isPositive( v, eps ) && s3.isPositive( v, eps );
    }

    DelaunayPoint vertexOf( DelaunaySide s )
    { 
      if ( s == s1 ) return s2.p2;
      if ( s == s2 ) return s3.p2;
      if ( s == s3 ) return s1.p2;
      return null;
    }

    DelaunaySide next( DelaunaySide s )
    { 
      if ( s == s1 ) return s2;
      if ( s == s2 ) return s3;
      if ( s == s3 ) return s1;
      return null;
    }

    DelaunaySide prev( DelaunaySide s )
    { 
      if ( s == s1 ) return s3;
      if ( s == s2 ) return s1;
      if ( s == s3 ) return s2;
      return null;
    }

  }

  // -----------------------------------------------------------------
  int N;  // number of points
  DelaunayPoint[] mPts;
  ArrayList< DelaunayTriangle > mTri;
  ArrayList< DelaunaySide > mSide;
  // double[] mDist; // precomputed arc-distances between points

  // For each DelaunayTriangle add a Triangle3D into the array list
  // The Triangle3D has vertex vectors the Cave3DStation + the original points 
  // of the DelaunayTriangle
  // @param triangles   array of triangles
  // @param st          base station (e,n,z)
  //
  // void insertTrianglesIn( ArrayList<Triangle3D> triangles, Cave3DStation st )
  // {
  //   Vector3D p0 = new Vector3D( st.x, st.y, st.z );
  //   for ( DelaunayTriangle t : mTri ) {
  //     if ( t.s1.p1.orig != null && t.s2.p1.orig != null && t.s3.p1.orig != null ) {
  //       Vector3D v1 = p0.sum( t.s1.p1.orig );
  //       Vector3D v2 = p0.sum( t.s2.p1.orig );
  //       Vector3D v3 = p0.sum( t.s3.p1.orig );
  //       Triangle3D t0 = new Triangle3D( v1, v2, v3, color );
  //       triangles.add( t0 );
  //     }
  //   }
  // }

  // cross-product of two Vector3D
  // Vector3D cross_product( Vector3D p1, Vector3D p2 )
  // {
  //   return new Vector3D( p1.y * p2.z - p1.z * p2.y,
  //                            p1.z * p2.x - p1.x * p2.z,
  //   			     p1.x * p2.y - p1.y * p2.x );
  // }

  // dot-product of two Vector3D
  // double dot_product( Vector3D v1, Vector3D v2 ) { return v1.dot( v2 ); }

  // arc-angle = ( range in [0, 2] )
  // double arc_angle( DelaunayPoint p1, DelaunayPoint p2 ) { return 1 - p1.v.dot( p2.v ); }
  
  // arc-distance = arccos of the dot-product ( range in [0, PI] )
  // double arc_distance( DelaunayPoint p1, DelaunayPoint p2 ) { return ( Math.acos( p1.v.dot( p2.v ) ) ); }
  // double arc_distance( Vector3D v1, Vector3D v2 ) { return ( Math.acos( v1.dot( v2 ) ) ); }

  // double distance3D( Vector3D v1, Vector3D v2 ) { return v1.distance3D( v2 ); }

  // triple-product of three Vector3D
  // double triple_product( Vector3D p1, Vector3D p2, Vector3D p3 ) { return p1.crossProduct( p2 ).dot( p3 ); }

  // add a delaunay triangle
  void addTriangle( DelaunaySide s1, DelaunaySide s2, DelaunaySide s3 )
  {
    DelaunayTriangle tri = new DelaunayTriangle( s1, s2, s3 );
    mTri.add( tri );
    s1.t = tri;
    s2.t = tri;
    s3.t = tri;
  }

  DelaunaySide addSide( DelaunayPoint p1, DelaunayPoint p2 ) 
  {
    DelaunaySide side = new DelaunaySide( p1, p2 );
    mSide.add( side );
    return side;
  }

  void setOpposite( DelaunaySide s1, DelaunaySide s2 )
  {
    s1.otherHalf = s2;
    s2.otherHalf = s1;
  }

  Cave3DDelaunay( Vector3D[] pts )
  {
    N = pts.length;
    mPts = new DelaunayPoint[ N ]; // delaunay points on the unit sphere
    for ( int n=0; n<N; ++n ) {
      mPts[n] = new DelaunayPoint( pts[n], n );
    }

    // prepare null-initialized triangles
    mTri  = new ArrayList<DelaunayTriangle>();
    mSide = new ArrayList<DelaunaySide>();

    if ( N >= 4 ) {
      computeLawson( );
    }
    color = 0xffcccccc; // DEBUG grey
  }

  // DelaunaySide findSide( Vector3D v )
  // {
  //   for ( DelaunaySide side : mSide ) {
  //     if ( side.contains( v, eps ) ) return side;
  //   }
  //   return null;
  // }

  DelaunayTriangle findTriangle( Vector3D v )
  {
    double e = eps;
    DelaunayTriangle ret = null;
    while ( ret == null ) {
      for ( DelaunayTriangle tri : mTri ) {
        if ( tri.contains( v, eps ) ) {
          if ( ret != null ) {
            TDLog.Error( "Delaunay point in many triangles ");
          } else {
            ret = tri;
          }
        }
      }
      if ( ret == null ) {
        // TDLog.v( "Delaunay point in no triangle ... retry" );
        v.x += (e * ( Math.random() - 0.5 ));
        v.y += (e * ( Math.random() - 0.5 ));
        v.z += (e * ( Math.random() - 0.5 ));
        v.normalized();
        e *= 2;
      }
    }
    return ret;
  }

  private void handle( DelaunaySide s0, DelaunayPoint p0 )
  {
    DelaunaySide sh = s0.otherHalf;
    DelaunayTriangle th = sh.t;
    if ( th.circumcontains( p0, eps ) ) { 
      DelaunayTriangle t0 = s0.t;
      DelaunayPoint ph = th.vertexOf( sh );
      DelaunaySide sh2 = th.prev( sh );
      DelaunaySide sh1 = th.next( sh ); 
      DelaunaySide s02 = t0.prev( s0 );  
      DelaunaySide s01 = t0.next( s0 );   
      DelaunaySide pph = addSide( p0, ph );    
      DelaunaySide php = addSide( ph, p0 );    
      setOpposite( pph, php );               
      mTri.remove( th );                    
      mTri.remove( t0 );
      mSide.remove( s0 );
      mSide.remove( sh );
      addTriangle( sh2, s01, pph ); 
      addTriangle( sh1, php, s02 );
      handle( sh2, p0 );
      handle( sh1, p0 );
    }
  }

  // boolean checkConsistency()
  // {
  //   for ( DelaunaySide s0 : mSide ) {
  //     DelaunaySide sh = s0.otherHalf;
  //     if ( sh == null ) {
  //       // TDLog.v("MISSING opposite sides of " + s0.p1.index + "-" + s0.p2.index );
  //       return false; 
  //     }
  //     if ( s0.p1 != sh.p2 || s0.p2 != sh.p1 ) {
  //       // TDLog.v("BAD opposite sides S0 " + s0.p1.index + "-" + s0.p2.index + " SH " + sh.p1.index + "-" + sh.p2.index );
  //       return false; 
  //     }
  //     DelaunayTriangle t0 = s0.t;
  //     if ( t0 == null ) {
  //       // TDLog.v("MISSING triangle" );
  //       return false; 
  //     }
  //     if ( t0.s1 != s0 && t0.s2 != s0 && t0.s3 != s0 ) {
  //       // TDLog.v("Bad triangle" );
  //       return false; 
  //     }
  //   }
  //   for ( DelaunayTriangle t : mTri ) {
  //     if ( t.s1.p2 != t.s2.p1 ) {
  //       // TDLog.v("MISMATCH 1-2 " + t.s1.p2.index + " " + t.s2.p1.index );
  //       return false;
  //     }
  //     if ( t.s2.p2 != t.s3.p1 ) {
  //       // TDLog.v("MISMATCH 2-3 " + t.s2.p2.index + " " + t.s3.p1.index );
  //       return false;
  //     }
  //     if ( t.s3.p2 != t.s1.p1 ) {
  //       // TDLog.v("MISMATCH 3-1 " + t.s3.p2.index + " " + t.s1.p1.index );
  //       return false;
  //     }
  //   }
  //   return true;
  // }

  private void handleTriangle( DelaunayTriangle tri, DelaunayPoint p ) 
  {
    DelaunaySide s1 = tri.s1;
    DelaunaySide s2 = tri.s2;
    DelaunaySide s3 = tri.s3;
    mTri.remove( tri );
    DelaunaySide s1p2p = addSide( s1.p2, p ); //   s1.p2 <------------ s1.p1
    DelaunaySide ps1p1 = addSide( p, s1.p1 ); //   s2.p1 ====>   ====> s3.p2
    DelaunaySide s2p2p = addSide( s2.p2, p ); //       \       p       ^ 
    DelaunaySide ps2p1 = addSide( p, s2.p1 ); //         \    ^ |     /
    DelaunaySide s3p2p = addSide( s3.p2, p ); //           v  | v   / 
    DelaunaySide ps3p1 = addSide( p, s3.p1 ); //          s2.p2 s3.p1
    setOpposite( s1p2p, ps2p1 );   
    setOpposite( s2p2p, ps3p1 );  
    setOpposite( s3p2p, ps1p1 ); 
    addTriangle( s1, s1p2p, ps1p1 );
    addTriangle( s2, s2p2p, ps2p1 );
    addTriangle( s3, s3p2p, ps3p1 );
    handle( s1, p );
    handle( s2, p );
    handle( s3, p );
  }

  // private void handleSide( DelaunaySide s0, DelaunayPoint p )
  // {
  //   DelaunaySide sh = s0.otherHalf;
  //   DelaunayTriangle t0 = s0.t;
  //   DelaunayTriangle th = sh.t;
  //   DelaunayPoint p0 = t0.vertexOf( s0 );
  //   DelaunayPoint ph = th.vertexOf( sh );
  //
  //   DelaunaySide pp0   = addSide( p, p0 );
  //   DelaunaySide p0p   = addSide( p0, p );
  //   setOpposite( pp0, p0p );
  //   DelaunaySide pph   = addSide( p, ph );
  //   DelaunaySide php   = addSide( ph, p );
  //   setOpposite( pph, php );
  //   DelaunaySide s0p1p = addSide( s0.p1, p     ); 
  //   DelaunaySide ps0p2 = addSide( p,     s0.p2 ); 
  //   DelaunaySide shp1p = addSide( sh.p1, p     ); 
  //   DelaunaySide pshp2 = addSide( p,     sh.p2 ); 
  //   setOpposite( s0p1p, pshp2 );
  //   setOpposite( ps0p2, shp1p );
  //
  //   DelaunaySide t0next = t0.next( s0 ); 
  //   DelaunaySide t0prev = t0.prev( s0 ); 
  //   DelaunaySide thnext = th.next( sh ); 
  //   DelaunaySide thprev = th.prev( sh ); 
  //   // remove t0, th, 
  //   // remove s0, sh
  //   mTri.remove( t0 );
  //   mTri.remove( th );
  //   mSide.remove( s0 );
  //   mSide.remove( sh );
  //   // insert four triangles
  //   addTriangle( t0prev, s0p1p, pp0 );
  //   addTriangle( t0next, p0p,   ps0p2 );
  //   addTriangle( thprev, shp1p, pph );
  //   addTriangle( thnext, php,   pshp2 );
  // }
    
  // n is the third vertex of the triangle

  private void computeLawson( )
  {
    // TDLog.v( "Delaunay compute Lawson N pts " + N );
    if ( N < 4 ) return;
    // decide whether 0,1,2,3 is right-handed
    DelaunayPoint[] pp = new DelaunayPoint[4];
    for ( int k = 0; k<4;  ) {
      int n = (int)(N*Math.random());
      if ( ! mPts[n].used ) {
        mPts[n].used = true;
        pp[k] = mPts[n];
        ++k;
      }
    }
    DelaunaySide s01 = addSide( pp[0], pp[1] );
    DelaunaySide s10 = addSide( pp[1], pp[0] );
    DelaunaySide s12 = addSide( pp[1], pp[2] );
    DelaunaySide s21 = addSide( pp[2], pp[1] );
    DelaunaySide s20 = addSide( pp[2], pp[0] );
    DelaunaySide s02 = addSide( pp[0], pp[2] );
    DelaunaySide s03 = addSide( pp[0], pp[3] );
    DelaunaySide s30 = addSide( pp[3], pp[0] );
    DelaunaySide s13 = addSide( pp[1], pp[3] );
    DelaunaySide s31 = addSide( pp[3], pp[1] );
    DelaunaySide s23 = addSide( pp[2], pp[3] );
    DelaunaySide s32 = addSide( pp[3], pp[2] );
    setOpposite( s01, s10 );
    setOpposite( s02, s20 );
    setOpposite( s03, s30 );
    setOpposite( s12, s21 );
    setOpposite( s13, s31 );
    setOpposite( s23, s32 );

    Vector3D v0 = pp[0];
    Vector3D v1 = pp[1].difference( v0 );
    Vector3D v2 = pp[2].difference( v0 );
    Vector3D v3 = pp[3].difference( v0 );
    double d = v1.crossProduct(v2).dotProduct( v3 );
    if ( d < 0 ) {
      addTriangle( s01, s12, s20 ); //          0
      addTriangle( s03, s31, s10 ); //          |
      addTriangle( s13, s32, s21 ); //          3
      addTriangle( s02, s23, s30 ); //     2          1
    } else {
      addTriangle( s02, s21, s10 ); //          0
      addTriangle( s03, s32, s20 ); //          |
      addTriangle( s23, s31, s12 ); //          3
      addTriangle( s01, s13, s30 ); //     1          2
    }
    // dumpTriangles( 2 );
    // TDLog.v( "Delaunay start with volume " + d + " consistency " + checkConsistency() );

    int kmax = N/2;
    for ( int k=4; k<kmax; ++k ) {
      int n = (int)(N*Math.random());
      while ( mPts[n].used ) { n = (int)(N*Math.random()); }
      // TDLog.v( "Delaunay handling point " + n );
      mPts[n].used = true;
      DelaunayPoint p = mPts[n];
      // Vector3D v  = p.v;
      // DelaunaySide s0 = findSide( v );
      // if ( s0 != null ) {
      //   handleSide( s0, p );
      // } else 
      {
        DelaunayTriangle tri = findTriangle( p );
        if ( tri == null ) {
          TDLog.Error( "Delaunay V on no triangle. " + p.x + " " + p.y + " " + p.z + " S " + mSide.size() + " T " + mTri.size() );
          return;
        }
        // TDLog.v( "Delaunay K " + k + " Point " + p.index + " in T " + tri.s1.p1.k + " " + tri.s2.p1.k + " " + tri.s3.p1.k );
        handleTriangle( tri, p );
      }
      // TDLog.v( "Delaunay point " + n + "/" + N + " S " + mSide.size() + " T " + mTri.size() 
      //       + " consistency " + checkConsistency() );
    }
    for ( int n=0; n<N; ++n ) {
      if ( mPts[n].used ) continue;
      mPts[n].used = true;
      DelaunayPoint p = mPts[n];
      // Vector3D v  = p.v;
      // DelaunaySide s0 = findSide( v );
      // if ( s0 != null ) {
      //   handleSide( s0, p );
      // } else
      {
        DelaunayTriangle tri = findTriangle( p );
        if ( tri == null ) {
          TDLog.Error( "Delaunay V on no triangle. " + p.x + " " + p.y + " " + p.z + " S " + mSide.size() + " T " + mTri.size() );
          return;
        }
        // TDLog.v( "Delaunay N " + n + " Point " + p.index + " in T " + tri.s1.p1.k + " " + tri.s2.p1.k + " " + tri.s3.p1.k );
        handleTriangle( tri, p );
      }
      // TDLog.v( "Delaunay point " + n + "/" + N + " S " + mSide.size() + " T " + mTri.size() 
      //       + " consistency " + checkConsistency() );
    }

  }

}
      
  
