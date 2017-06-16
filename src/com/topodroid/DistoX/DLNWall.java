/** @file DLNWall.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid Delaunay-Voronoi class
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

// import java.io.FileWriter;
// import java.io.PrintWriter;
// import java.io.IOException;

import android.util.Log;

class DLNWall
{
  ArrayList< DLNTriangle > mTri;
  ArrayList< HullSide > mHull; // convex hull
  ArrayList< HullSide > mPosHull;
  ArrayList< HullSide > mNegHull;
  BezierPoint P0;
  BezierPoint P1;
  BezierPoint P01;
  float P01len2;

  DLNWall( BezierPoint p0, BezierPoint p1 )
  {
    P0 = p0;
    P1 = p1;
    P01 = P1.sub( P0 );
    P01len2 = P01.dot(P01);
  }

  int size() { return mTri.size(); }

  DLNTriangle getTriangle( int k ) { return mTri.get(k); }

  HullSide getBorderHead() { return mHull.get(0); }

  int hullSize() { return mHull.size(); }

  void compute( List< DLNSite > pts )
  {
    int size = pts.size();
    // Log.v("DistoX", "DLNWall sites " + size );
    // try {
    //   FileWriter fw = new FileWriter( "/sdcard/test.txt" );
    //   PrintWriter pw = new PrintWriter( fw );
    //   for ( DLNSite s : pts ) {
    //     pw.format("%.4f %.2f\n", s.mX, s.mY );
    //   }
    //   pw.flush();
    //   fw.close();
    // } catch ( IOException e ) { }

    mTri = new ArrayList< DLNTriangle >();
    mHull = new ArrayList< HullSide >();

    // bbox of points
    BezierPoint p0 = pts.get(0);
    BezierPoint min = new BezierPoint( p0 );
    BezierPoint max = new BezierPoint( p0 );
    for ( BezierPoint p : pts ) {
      if ( p.mX < min.mX )      { min.mX = p.mX; }
      else if ( p.mX > max.mX ) { max.mX = p.mX; }
      if ( p.mY < min.mY )      { min.mY = p.mY; }
      else if ( p.mY > max.mY ) { max.mY = p.mY; }
    }
    BezierPoint c = new BezierPoint( (min.mX+max.mX)/2, (min.mY+max.mY)/2 ); // center
    float S0 = max.mX - min.mX;
    float s  = max.mY - min.mY; if ( s > S0 ) S0 = s;
    // Log.v("DistoX", "center " + c.mX + " " + c.mY + " side " + S0 );
    s = 10*S0;
    BezierPoint A = new BezierPoint( c.mX-s, c.mY-s );
    BezierPoint B = new BezierPoint( c.mX-s, c.mY+s );
    BezierPoint C = new BezierPoint( c.mX+s, c.mY+s );
    BezierPoint D = new BezierPoint( c.mX+s, c.mY-s );
    s *= 1.4142f;
    BezierPoint AB = new BezierPoint( c.mX-s, c.mY   );
    BezierPoint CD = new BezierPoint( c.mX+s, c.mY   );
    BezierPoint BC = new BezierPoint( c.mX,   c.mY+s );
    BezierPoint DA = new BezierPoint( c.mX,   c.mY-s );
    
    DLNTriangle tAB1 = new DLNTriangle( p0, A,  AB );
    DLNTriangle tAB2 = new DLNTriangle( p0, AB, B  );
    DLNTriangle tBC1 = new DLNTriangle( p0, B,  BC );
    DLNTriangle tBC2 = new DLNTriangle( p0, BC, C  );
    DLNTriangle tCD1 = new DLNTriangle( p0, C,  CD );
    DLNTriangle tCD2 = new DLNTriangle( p0, CD, D );
    DLNTriangle tDA1 = new DLNTriangle( p0, D,  DA );
    DLNTriangle tDA2 = new DLNTriangle( p0, DA, A );
    DLNSide.pairSides( tAB1.side(1), tAB2.side(2) );
    DLNSide.pairSides( tAB2.side(1), tBC1.side(2) );
    DLNSide.pairSides( tBC1.side(1), tBC2.side(2) );
    DLNSide.pairSides( tBC2.side(1), tCD1.side(2) );
    DLNSide.pairSides( tCD1.side(1), tCD2.side(2) );
    DLNSide.pairSides( tCD2.side(1), tDA1.side(2) );
    DLNSide.pairSides( tDA1.side(1), tDA2.side(2) );
    DLNSide.pairSides( tDA2.side(1), tAB1.side(2) );
    mTri.add( tAB1 );
    mTri.add( tBC1 );
    mTri.add( tCD1 );
    mTri.add( tDA1 );
    mTri.add( tAB2 );
    mTri.add( tBC2 );
    mTri.add( tCD2 );
    mTri.add( tDA2 );
  
    // consistency();
  
    for ( int j=1; j<size; ++j ) {
      BezierPoint p = pts.get(j);
      // get the triangle p belongs to
      DLNTriangle tri = null;
      for ( DLNTriangle t : mTri ) {
        if ( t.contains( p ) ) {
          tri = t;
          mTri.remove( t ); // remove from vector
          break;
        }
      }
      // assert( tri != NULL );
      DLNTriangle t0 = new DLNTriangle( p, tri.point(1), tri.point(2) );
      DLNTriangle t1 = new DLNTriangle( p, tri.point(2), tri.point(0) );
      DLNTriangle t2 = new DLNTriangle( p, tri.point(0), tri.point(1) );
      DLNSide.pairSides( t0.side(1), t1.side(2) );
      DLNSide.pairSides( t1.side(1), t2.side(2) );
      DLNSide.pairSides( t2.side(1), t0.side(2) );
      DLNSide.pairSides( tri.side(0).other, t0.side(0) );
      DLNSide.pairSides( tri.side(1).other, t1.side(0) );
      DLNSide.pairSides( tri.side(2).other, t2.side(0) );
      mTri.add( t0 );
      mTri.add( t1 );
      mTri.add( t2 );
  
      // delete tri;
      doTriangle( t0, p );
      doTriangle( t1, p );
      doTriangle( t2, p );
    }
    // Log.v("DistoX", "DLNWall triangle " + mTri.size() );

    // compute the convex hull
    ArrayList< DLNSide > tmp = new ArrayList< DLNSide >();
    for ( DLNTriangle t : mTri ) {
      if ( t.hasPoint( A ) )       { 
        if ( ! t.hasPoint( DA ) && ! t.hasPoint( AB ) ) tmp.add( t.sideOf( A  ) ); 
      } else if ( t.hasPoint( AB ) ) {
        if ( ! t.hasPoint( A  ) && ! t.hasPoint( B  ) ) tmp.add( t.sideOf( AB ) );
      } else if ( t.hasPoint( B  ) ) {
        if ( ! t.hasPoint( AB ) && ! t.hasPoint( BC ) ) tmp.add( t.sideOf( B  ) );
      } else if ( t.hasPoint( BC ) ) {
        if ( ! t.hasPoint( B  ) && ! t.hasPoint( C  ) ) tmp.add( t.sideOf( BC ) );
      } else if ( t.hasPoint( C  ) ) {
        if ( ! t.hasPoint( BC ) && ! t.hasPoint( CD ) ) tmp.add( t.sideOf( C  ) );
      } else if ( t.hasPoint( CD ) ) {
        if ( ! t.hasPoint( C  ) && ! t.hasPoint( D  ) ) tmp.add( t.sideOf( CD ) ); 
      } else if ( t.hasPoint( D  ) ) {
        if ( ! t.hasPoint( CD ) && ! t.hasPoint( DA ) ) tmp.add( t.sideOf( D  ) );
      } else if ( t.hasPoint( DA ) ) {
        if ( ! t.hasPoint( D  ) && ! t.hasPoint( A  ) ) tmp.add( t.sideOf( DA ) );
      }
    }
    // Log.v("DistoX", "DLNWall convex hull start " + tmp.size() );
  
    int sz = tmp.size();
    DLNSide ts = tmp.get(0);
    BezierPoint p1 = ts.mP1;
    BezierPoint p2 = ts.mP2; 
    HullSide hs0 = new HullSide( ts );
    HullSide hs1 = hs0;
    mHull.add( hs0 );
    while ( p2 != p1 ) {
      BezierPoint p3 = p2;
      for ( int j=0; j<sz; ++j ) {
        DLNSide tt = tmp.get(j);
        // if ( coincide( tt.mP1, p2, 0.0001 ) ) 
        if ( tt.mP1 == p2 ) {
          HullSide hs2 = new HullSide( tt );
  	  hs1.next = hs2;
  	  hs2.prev = hs1;
          mHull.add( hs2 );
  	  hs1 = hs2;
          p2 = tt.mP2;
          break;
        }
      }
      if ( p3 == p2 ) {
        TDLog.Error("failed next at " + p2.mX + " " + p2.mY );
        break;
      }
    }
    hs1.next = hs0;
    hs0.prev = hs1;
    // Log.v("DistoX", "DLNWall convex hull done " + mHull.size() );
  
    // compute sites poles
    for ( DLNSite it : pts ) {
      setSitePole( it );
    }
  
    // refine Convex Hull
    boolean repeat = true;
    while ( repeat ) {
      repeat = false;
      for ( DLNSite pt : pts ) {
        if ( pt.polePoint() == null ) continue;
        if ( isInsideHull( pt ) && ! isInsideHull( pt.polePoint() ) ) {
  	    DLNTriangle t1 = pt.poleTriangle();
  	    DLNSide s1 = t1.sideOf( pt );
  	    for ( HullSide hs9 : mHull ) {
  	      DLNSide s0 = hs9.side;
  	      if ( s1.other == s0 ) {
  	        DLNSide s2 = t1.nextSide( s1 );
  	        DLNSide s3 = t1.nextSide( s2 );
  	        mHull.remove( hs9 );
  	        HullSide hs2 = new HullSide( s2 );
  	        HullSide hs3 = new HullSide( s3 );
  	        hs2.next = hs3;       hs3.prev = hs2;
  	        hs9.prev.next = hs2; hs2.prev = hs9.prev;
  	        hs9.next.prev = hs3; hs3.next = hs9.next;
  	        mHull.add( hs2 );
  	        mHull.add( hs3 );
  	        // delete hs9;
  	        repeat = true;
  	        break;
  	      }
          }
        }
      }
    }
    // Log.v("DistoX", "DLNWall convex hull final " + mHull.size() );
    mPosHull = new ArrayList< HullSide >();
    mNegHull = new ArrayList< HullSide >();

    HullSide hsp1 = null;
    HullSide hsn1 = null;
    int nhp = -1;
    int nhn = -1;
    for ( int nh=0; nh < mHull.size(); ++nh ) {
      HullSide hs = mHull.get(nh);
      int sng = sign( hs );
      if ( sng > 0 && hsp1 == null ) {
        hsp1 = hs.prev;
        while ( hsp1 != hs && sign(hsp1) > 0 ) hsp1 = hsp1.prev;
        HullSide hsp2 = hs.next;
        while ( hsp2 != hs && sign(hsp2) > 0 ) hsp2 = hsp2.next;
        for ( hsp1=hsp1.next; hsp1 != hsp2; hsp1=hsp1.next ) {
          mPosHull.add( hsp1 );
        }
        if ( hsn1 != null ) break;
      }
      if ( sng < 0 && hsn1 == null ) {
        hsn1 = hs.prev;
        while ( hsn1 != hs && sign(hsn1) < 0 ) hsn1 = hsn1.prev;
        HullSide hsn2 = hs.next;
        while ( hsn2 != hs && sign(hsn2) < 0 ) hsn2 = hsn2.next;
        for ( hsn1=hsn1.next; hsn1 != hsn2; hsn1=hsn1.next ) {
          mNegHull.add( hsn1 );
        }
        if ( hsp1 != null ) break;
      }
    }
    // Log.v("DistoX", "Hull pos " + mPosHull.size() + " neg " + mNegHull.size() );
  }

  // void setSitePole( DLNSite site )
  // {
  //   for ( DLNTriangle t : mTri ) {
  //     if ( t.hasPoint( site ) ) {
  //       site.setPole( t.mCenter, t ); // possibly replace if better
  //     }
  //   }
  // }

  void setSitePole( DLNSite site )
  {
    for ( DLNTriangle t : mTri ) {
      if ( t.hasPoint( site ) ) {
        if ( ! isIntersection( t.mCenter, site ) ) {
          site.setPole( t.mCenter, t ); // possibly replace if better
        }
      }
    }
  }

  boolean isIntersection( BezierPoint q0, BezierPoint q1 )
  {
    {
      float xp = P1.mX - P0.mX;
      float yp = P1.mY - P0.mY;
      if ( ( (q0.mX-P0.mX)*yp - (q0.mY-P0.mY)*xp) * ( (q1.mX-P0.mX)*yp - (q1.mY-P0.mY)*xp ) > 0 ) return false;
    }
    {
      float xq = q1.mX - q0.mX;
      float yq = q1.mY - q0.mY;
      if ( ( (P0.mX-q0.mX)*yq - (P0.mY-q0.mY)*xq) * ( (P1.mX-q0.mX)*yq - (P1.mY-q0.mY)*xq ) > 0 ) return false;
    }
    return true;
  }

  int sign( HullSide hs )
  {
    DLNSide s = hs.side;
    BezierPoint p1 = s.mP1.sub( P0 );
    BezierPoint p2 = s.mP2.sub( P0 );
    float x1 = p1.dot( P01 ) / P01len2;
    float x2 = p2.dot( P01 ) / P01len2;
    if ( x1 < -0.1f && x2 < -0.1f ) return 0;
    if ( x1 >  1.1f && x2 >  1.1f ) return 0;
    if ( x1 >= -0.1f && x1 <= 1.1f ) {
      return ( p1.cross(P01) > 0)? 1 : -1;
    }
    return ( p2.cross(P01) > 0)? 1 : -1;
  }

  // boolean coincide( BezierPoint p1, BezierPoint p2, double eps )
  // {
  //   if ( Math.abs( p1.mX - p2.mX ) > eps ) return false;
  //   if ( Math.abs( p1.mY - p2.mY ) > eps ) return false;
  //   return true;
  // }

  void doTriangle( DLNTriangle t0, BezierPoint p )
  {
    Stack< DLNTriangle > stack = new Stack< DLNTriangle >();
    stack.add( t0 );
    while ( ! stack.empty() ) {
      handleTriangle( stack, p );
    }
    // consistency();
  }

  boolean isInsideHull( BezierPoint p )
  {
    double a = 0;
    for ( HullSide it : mHull ) {
      DLNSide s = it.side;
      double x1 = p.mX - s.mP1.mX;
      double y1 = p.mY - s.mP1.mY;
      double x2 = p.mX - s.mP2.mX;
      double y2 = p.mY - s.mP2.mY;
      double d1 = Math.sqrt( x1*x1 + y1*y1 ); x1 /= d1; y1 /= d1;
      double d2 = Math.sqrt( x2*x2 + y2*y2 ); x2 /= d2; y2 /= d2;
      double sa = x2*y1 - y2*x1;
      double ca = x1*x2 + y1*y2;
      a += Math.atan2( sa, ca );
    }
    return Math.abs( a ) > 3.18; // slightly more than M_PI
  }
  
  
  void handleTriangle( Stack< DLNTriangle > stack, BezierPoint p1 )
  {
    DLNTriangle t1 = stack.pop();
    DLNSide s1 = t1.sideOf( p1 );
    DLNSide s2 = s1.other;
    if ( s2 == null ) return;
    DLNTriangle t2 = s2.triangle;
    BezierPoint p2 = t2.pointOf( s2 );
    BezierPoint c2 = t2.mCenter;
    if ( c2.distance( p1 ) < t2.mRadius ) {
      BezierPoint pa = t1.nextPoint( p1 );
      BezierPoint pb = t1.prevPoint( p1 );
      // assert( pa == t2.prevPoint( p2 ) );
      // assert( pb == t2.nextPoint( p2 ) );
      DLNTriangle ta = new DLNTriangle( pa, p2, p1 );
      DLNTriangle tb = new DLNTriangle( pb, p1, p2 );
      DLNSide.pairSides( ta.sideOf(pa), tb.sideOf(pb) );
      DLNSide.pairSides( ta.sideOf(p2), t1.sideOf(pb).other );
      DLNSide.pairSides( ta.sideOf(p1), t2.sideOf(pb).other );
      DLNSide.pairSides( tb.sideOf(p2), t1.sideOf(pa).other );
      DLNSide.pairSides( tb.sideOf(p1), t2.sideOf(pa).other );
      removeTriangle( t1 );
      removeTriangle( t2 );
      mTri.add( ta );
      mTri.add( tb );
      stack.add( ta );
      stack.add( tb );
    }
  }


  // void consistency( DLNTriangle t )
  // {
  //   DLNSide s0 = t.side(0);
  //   DLNSide s1 = t.side(1);
  //   DLNSide s2 = t.side(2);
  //   BezierPoint p0 = t.point(0);
  //   BezierPoint p1 = t.point(1);
  //   BezierPoint p2 = t.point(2);
  //   assert( p0 == t.pointOf(s0) );
  //   assert( p1 == t.pointOf(s1) );
  //   assert( p2 == t.pointOf(s2) );
  //   assert( s0 == t.sideOf(p0) );
  //   assert( s1 == t.sideOf(p1) );
  //   assert( s2 == t.sideOf(p2) );
  //   assert( p1 == t.nextPoint(p0) );
  //   assert( p2 == t.nextPoint(p1) );
  //   assert( p0 == t.nextPoint(p2) );
  //   assert( p2 == t.prevPoint(p0) );
  //   assert( p0 == t.prevPoint(p1) );
  //   assert( p1 == t.prevPoint(p2) );
  //   assert( s0.mP1 == p1 );
  //   assert( s0.mP2 == p2 );
  //   assert( s1.mP1 == p2 );
  //   assert( s1.mP2 == p0 );
  //   assert( s2.mP1 == p0 );
  //   assert( s2.mP2 == p1 );
  //   Side * s00 = s0.other; assert( s00 == NULL || s00.other == s0 );
  //   Side * s11 = s1.other; assert( s11 == NULL || s11.other == s1 );
  //   Side * s22 = s2.other; assert( s22 == NULL || s22.other == s2 );
  //   BezierPoint c = t.mCenter;
  //   double r2 = t->mRadius;
  //   assert( Math.fabs(c.distance( p0 ) - r2) < 0.01 );
  //   assert( Math.fabs(c.distance( p1 ) - r2) < 0.01 );
  //   assert( Math.fabs(c.distance( p2 ) - r2) < 0.01 );
  // }

  // void consistency()
  // {
  //   for ( DLNTriangle t : mTri ) consistency( t );
  // }

  void removeTriangle( DLNTriangle t ) { mTri.remove( t ); }

}

