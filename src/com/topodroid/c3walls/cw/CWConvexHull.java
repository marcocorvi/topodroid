/** @file CWConvexHull.java
 *
 *e @author marco corvi
 * @date nov 2011
 *
 * @brief Cave3D convex-concave hull
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.cw;

import com.topodroid.DistoX.Vector3D;
// import com.topodroid.DistoX.TopoGL;
import com.topodroid.DistoX.GlModel;
import com.topodroid.DistoX.Cave3DShot;
import com.topodroid.DistoX.Cave3DStation;
import com.topodroid.utils.TDLog;

import java.util.ArrayList;
import java.util.List;
// import java.util.Iterator;
import java.util.Locale;
// import java.util.Collection;
// import static java.util.stream.Collectors.toList;

import java.io.PrintWriter;
// import java.io.PrintStream;
// import java.io.FileNotFoundException;
import java.io.DataOutputStream;
import java.io.IOException;

public class CWConvexHull 
{
  private static int cnt = 0;
  static public void resetCounter() { cnt = 0; }
  static public void resetCounters() 
  {
    CWBorder.resetCounter();
    CWIntersection.resetCounter();
    CWPoint.resetCounter();
    CWSide.resetCounter();
    CWTriangle.resetCounter();
    resetCounter();
  }

  int mCnt;
  Cave3DStation mFrom;
  Cave3DStation mTo;
  private ArrayList< CWPoint > mVertex;
  private ArrayList< CWSide > mSide;
  public ArrayList< CWTriangle > mFace;
  public ArrayList< CWTriangle > mSplits;
  double mVolume;
  boolean hasVolume;
  
  public CWConvexHull( )
  {
    mCnt  = cnt++;
    mVertex = new ArrayList< CWPoint >();
    mFace   = new ArrayList< CWTriangle >();
    mSplits = new ArrayList< CWTriangle >();
    mSide   = new ArrayList< CWSide >();
    hasVolume = false;
  }

  // public void create( ArrayList<Vector3D> pts,
  //   double distance_concavity, double angle_concavity, double eps )
  // {
  //   for ( Vector3D p : pts ) insertPoint( p );
  //   // dump();
  //   makeConcave( pts, distance_concavity, angle_concavity, eps );
  // }

  public void create( ArrayList< Cave3DShot > legs1,   // legs at FROM station
                      ArrayList< Cave3DShot > legs2,   // legs at TO station
                      ArrayList< Cave3DShot > splays1, // splays at FROM station
                      ArrayList< Cave3DShot > splays2, // splays at TO station
                      Cave3DStation sf,                // shot FROM station
                      Cave3DStation st,                // shot TO station
                      boolean all_splay )
    throws RuntimeException
  {
    mFrom = sf;
    mTo   = st;
    // TDLog.v( "CW-Hull " + sf.short_name + "-" + st.short_name 
    //               + " FROM " + legs1.size() + "/" + splays1.size() 
    //               + " TO "   + legs2.size() + "/" + splays2.size() );
    ArrayList< Vector3D > pts = new ArrayList<Vector3D>();
    Vector3D vf = new Vector3D( sf );
    Vector3D vt = new Vector3D( st );
    Vector3D vt0 = null; 
    Vector3D vf0 = null; 

    if ( GlModel.mSplitStretch ) {
      Vector3D vm = vt.difference( vf ); 
      double lm = vm.length();
      if ( lm < 0.0001f ) {
        TDLog.Error( "CW-Hull Error leg too short");
        return;
      }
      vm.scaleBy( GlModel.mSplitStretchDelta / lm );
      vt0 = vt.sum( vm );
      vf0 = vf.difference( vm );
    } else {
      vt0 = new Vector3D( vt );
      vf0 = new Vector3D( vf );
    }
    // int at_from = 1;
    // int at_to   = 1;
      
    try {
      // TDLog.v( "CW-Hull inserted point T " + vt.x + " " + vt.y + " " + vt.z );
      if ( splays1.size() > 0 ) {
        if ( legs1.size() <= 1 ) {
          for ( Cave3DShot s1 : splays1 ) {
            Vector3D v1 = vf0.sum( s1.toVector3D() );
            if ( GlModel.mSplitRandomize ) v1.randomize( GlModel.mSplitRandomizeDelta );
            insertPoint( v1 );
            pts.add( v1 );
            // at_from ++;
            // TDLog.v( "CW-Hull inserted point (1) " + v1.x + " " + v1.y + " " + v1.z );
          }
        } else {
          for ( Cave3DShot s1 : splays1 ) {
            boolean inserted = all_splay;
            Vector3D v1 = vf0.sum( s1.toVector3D() );
            if ( GlModel.mSplitRandomize ) v1.randomize( GlModel.mSplitRandomizeDelta );
            if ( ! inserted ) {
              for ( Cave3DShot l1 : legs1 ) if ( s1.dotProduct( l1 ) >= 0 ) { inserted = true; break; }
            }
            if ( inserted ) {
              insertPoint( v1 );
              pts.add( v1 );
              // at_from ++;
              // TDLog.v( "CW-Hull point (1) " + v1.x + " " + v1.y + " " + v1.z + " inserted " );
            } else {
              // TDLog.v( "CW-Hull point (1) " + v1.x + " " + v1.y + " " + v1.z + " NOT inserted " );
            }
          }
        }
      }
      // TDLog.v( "CW-Hull inserted point F " + vf.x + " " + vf.y + " " + vf.z );
      if ( splays2.size() > 0 ) {
        if ( legs2.size() <= 1 ) {
          for ( Cave3DShot s2 : splays2 ) {
            Vector3D v2 = vt0.sum( s2.toVector3D() );
            if ( GlModel.mSplitRandomize ) v2.randomize( GlModel.mSplitRandomizeDelta );
            insertPoint( v2 );
            pts.add( v2 );
            // at_to ++;
            // TDLog.v( "CW-Hull inserted point (2) " + v2.x + " " + v2.y + " " + v2.z );
          }
        } else {
          for ( Cave3DShot s2 : splays2 ) {
            boolean inserted = all_splay;
            Vector3D v2 = vt0.sum( s2.toVector3D() );
            if ( GlModel.mSplitRandomize ) v2.randomize( GlModel.mSplitRandomizeDelta );
            if ( ! inserted ) {
              for ( Cave3DShot l2 : legs2 ) if ( s2.dotProduct( l2 ) >= 0 ) { inserted = true; break; }
            }
            if ( inserted ) {
              insertPoint( v2 );
              pts.add( v2 );
              // at_to ++;
              // TDLog.v( "CW-Hull point (2) " + v2.x + " " + v2.y + " " + v2.z + " inserted " );
            } else {
              // TDLog.v( "CW-Hull point (2) " + v2.x + " " + v2.y + " " + v2.z + " NOT inserted " );
            }
          }
        }
      }
      if ( GlModel.mSplitRandomize ) {
        vt0.randomize( GlModel.mSplitRandomizeDelta );
        vf0.randomize( GlModel.mSplitRandomizeDelta );
      }
      insertPoint( vt0 );
      pts.add( vt0 );
      insertPoint( vf0 );
      pts.add( vf0 );
    } catch ( RuntimeException e ) {
      // vf.dump();
      // vt.dump();
      // for ( Cave3DShot s1 : splays1 ) {
      //   vf.sum( s1.toVector3D() ).dump();
      // }
      // for ( Cave3DShot s2 : splays2 ) {
      //   vt.sum( s2.toVector3D() ).dump();
      // }
      TDLog.Error( "CW-HUll error " + e.getMessage() );
      throw e;
    }
    // TDLog.v( "CW-Hull " + sf.short_name + "-" + st.short_name 
    //               + " FROM " + legs1.size() + "/" + splays1.size() 
    //               + " TO "   + legs2.size() + "/" + splays2.size() 
    //               + " pts " + pts.size() + " at from " + at_from + " at to " + at_to );

    // TDLog.v( "CW-Hull points " + pts.size() );
    makeConcave( pts, 0.5f, 0.1f, 0.000001f );
  }

  
  void addPoint( Vector3D p ) { insertPoint( p ); }

  void addTriangle( CWTriangle t ) { mFace.add( t ); }

  // int getNrVertex() { return mVertex.size(); }
  // int getNrSide()   { return mSide.size(); }
  // int getNrFace()   { return mFace.size(); }

  double computeDiameter() 
  {
    double diam = 0;
    for ( int k1 = 0; k1 < mVertex.size(); ++k1 ) {
      CWPoint p1 = mVertex.get(k1);
      for ( int k2 = k1+1; k2 < mVertex.size(); ++k2 ) {
        double d = p1.distance3D( mVertex.get( k2 ) );
        if ( d > diam ) diam = d;
      }
    }
    return diam;
  }

  double getVolume()
  {
    if ( ! hasVolume ) {
      mVolume = computeVolume();
      hasVolume = true;
    }
    return mVolume;
  }
  
  // -------------------------------------------------------------------

  private Vector3D getCenter()
  {
    Vector3D ret = new Vector3D();
    for( CWPoint p : mVertex ) { ret.x += p.x; ret.y += p.y; ret.z += p.z; }
    ret.scaleBy( 1.0f / mVertex.size() );
    return ret;
  }

  private static double volume( Vector3D p0, Vector3D p1, Vector3D p2, Vector3D p3 )
  {
    // return p0.x * ( p1.y * p2.z + p3.y * p1.z + p2.y * p3.z - p1.y * p3.z - p3.y * p2.z - p2.y * p1.z ) 
    //      - p0.y * ( p1.x * p2.z + p3.x * p1.z + p2.x * p3.z - p1.x * p3.z - p3.x * p2.z - p2.x * p1.z ) 
    //      + p0.z * ( p1.x * p2.y + p3.x * p1.y + p2.x * p3.y - p1.x * p3.y - p3.x * p2.y - p2.x * p1.y ) 
    //      - (p1.x * (p2.y*p3.z - p2.z*p3.y) + p1.y * (p2.z*p3.x - p2.x*p3.z) + p1.z * (p2.x*p3.y - p2.y*p3.z));
    Vector3D u1 = p1.difference(p0);
    Vector3D u2 = p2.difference(p0);
    Vector3D u3 = p3.difference(p0);
    return u1.crossProduct(u2).dotProduct(u3);
  }

  private void addVertex( CWPoint v )
  {
    if ( v == null || mVertex.contains(v) ) return;
    mVertex.add( v );
  }
  
  private CWPoint getVertexByTag( int tag )
  {
    for ( CWPoint v : mVertex ) if ( v.mCnt == tag ) return v;
    return null;
  }

  private void addSide( CWSide s )
  {
    if ( s == null || mSide.contains( s ) ) return;
    mSide.add( s );
  }
  
  private CWSide getSideByTag( int tag )
  {
    for ( CWSide s : mSide ) if ( s.mCnt == tag ) return s;
    return null;
  }

  private CWSide getSide( CWPoint p1, CWPoint p2 )
  {
    for ( CWSide s : mSide ) {
      if ( s.p1 == p1 && s.p2 == p2 ) return s;
      if ( s.p1 == p2 && s.p2 == p1 ) return s;
    }
    CWSide s0 = new CWSide( p1 , p2 );
    mSide.add( s0 );
    return s0;
  }
  
  private CWTriangle addTriangle( CWPoint p1, CWPoint p2, CWPoint p3 )
  {
    CWSide s1 = getSide( p2, p3 );
    CWSide s2 = getSide( p3, p1 );
    CWSide s3 = getSide( p1, p2 );
    CWTriangle t = new CWTriangle( p1, p2, p3, s1, s2, s3 );
    mFace.add( t );
    s1.setTriangle( t );
    s2.setTriangle( t );
    s3.setTriangle( t );
    addVertex( p1 );
    addVertex( p2 );
    addVertex( p3 );
    return t;
  }
  
  private CWTriangle getTriangleByTag( int tag )
  {
    for ( CWTriangle t : mFace ) if ( t.mCnt == tag ) return t;
    return null;
  }

  private void removeVertex( CWPoint v )  { mVertex.remove( v ); }
  private void removeSide( CWSide s )     { mSide.remove( s ); }
  private void removeFace( CWTriangle t ) { mFace.remove( t ); }
  
  // -------------------------------------------------------------------
  private void addTo( List<CWPoint> yes, List<CWPoint> no, CWPoint v ) 
  {
    if ( v.areAllTrianglesOutside() ) {
      if ( ! yes.contains(v) ) yes.add( v );
    } else {
      if ( ! no.contains(v) ) no.add( v );
    }
  }
  private void addTo( List<CWSide> yes, List<CWSide> no, CWSide s ) 
  {
    if ( s.areTrianglesOutside() ) {
      if ( ! yes.contains(s) ) yes.add( s );
    } else {
      if ( ! no.contains(s) ) no.add( s );
    }
  }
      
  private void processTempSides( CWPoint vv, ArrayList< CWSide > temp, boolean reverse )
  {
    CWSide s0 = temp.get(0);
    CWPoint p0 = s0.p1;
    CWPoint p2 = s0.p2;
    if ( reverse ) {
      p0 = s0.p2;
      p2 = s0.p1;
    }
    addTriangle( p0, p2, vv );
    for ( int todo = 1; todo < temp.size(); ++todo ) {
      p0 = p2;
      p2 = temp.get(todo).otherPoint( p0 );
      addTriangle( p0, p2, vv );
    }
  }
  
  private void insertPoint( Vector3D p )
  {
     // TDLog.v( "CW-Hull " + mVertex.size() + " insert point " + p.x + " " + p.y + " " + p.z );
    if ( mVertex.size() < 4 ) {
      mVertex.add( new CWPoint( p.x, p.y, p.z ) );
      if ( mVertex.size() == 4 ) {
        CWPoint p0 = mVertex.get(0);
        CWPoint p1 = mVertex.get(1);
        CWPoint p2 = mVertex.get(2);
        CWPoint p3 = mVertex.get(3);
        // positive volume for points inside the tetrahedron
        // face triangles must point inside
        if ( volume( p0, p1, p2, p3 ) < 0 ) {
          p1 = mVertex.get(2);
          p2 = mVertex.get(1);
        }
        
        /**                 1      3 is above the screen
         *                / | \
         *  face-3       /  |--\------- T0.s3 = T2.s3
         *  T3.s3 ------/   3   \------ T0.s2 = T3.s1
         *             /  ,' `,--\----- T0.s1 = T1.s3
         *  T1.s1 ----/-,'     `, \
           *           0 ----------- 2
         *                T3.s2 = T1.s2
         *
         *                face-2
         */
        addTriangle( p0, p1, p2 ); //  0--1--2-[3]->
        addTriangle( p1, p3, p2 ); // [0] 1--2--3--<
        addTriangle( p2, p3, p0 ); //  0-[1]-2--3-->
        addTriangle( p3, p1, p0 ); //  0--1-[2]-3--<
      // dump();
      }
      return;
    }

    // boolean cs = checkSideConsistency();
    // boolean cv = checkVertexConsistency();
    // if ( ! cs || ! cv )
    //   TDLog.v( "CW-HUll consistency before: V " + mVertex.size() + " S " + mSide.size() + " T " + mFace.size()
    //     + " S-check " + cs + " V-check " + cv );
   
    // int nv = mVertex.nv();
    ArrayList<CWTriangle> faceToRemove = new ArrayList<CWTriangle>();
    for ( CWTriangle t : mFace ) {
      if ( t.setOutside( p ) ) {
        faceToRemove.add( t );
      }
    }
    if ( faceToRemove.size() == 0 ) {
      // TDLog.v( "CW-Hull point is inside " + p.x + " " + p.y + " " + p.z );
      return;
    } else {
      // TDLog.v( "CW-Hull faces to remove " + faceToRemove.size() );
    }

    // dump();
    CWPoint vv = new CWPoint( p.x, p.y, p.z );

    ArrayList<CWPoint> vertexToRemove = new ArrayList<CWPoint>();
    ArrayList<CWPoint> vertexToKeep   = new ArrayList<CWPoint>();
    ArrayList<CWSide> sideToRemove    = new ArrayList<CWSide>();
    ArrayList<CWSide> sideToKeep      = new ArrayList<CWSide>();

    for ( CWTriangle t : faceToRemove ) {
      addTo( vertexToRemove, vertexToKeep, t.v1 );
      addTo( vertexToRemove, vertexToKeep, t.v2 );
      addTo( vertexToRemove, vertexToKeep, t.v3 );
      addTo( sideToRemove, sideToKeep, t.s1 );
      addTo( sideToRemove, sideToKeep, t.s2 );
      addTo( sideToRemove, sideToKeep, t.s3 );
    }
    int rt = faceToRemove.size();
    int rv = vertexToRemove.size();
    int kv = vertexToKeep.size();
    int rs = sideToRemove.size();
    int ks = sideToKeep.size();

    // TDLog.v( "CW-Hull point " + p.x + " " + p.y + " " + p.z + " To remove T: " + rt
    //   + " V: " + rv + " / " + kv + " S: " + rs + " / " + ks );

    if ( rt + rv != rs + 1 ) return;
    if ( sideToKeep.size() <= 2 ) return;
    if ( (2*rs + ks) % 3 != 0 ) return;

    addVertex( vv );
    
    CWSide  s0 = sideToKeep.get(0);
    CWPoint p0 = s0.p1;
    CWPoint p2 = s0.p2;
    boolean reverse = false; // check if need to reverse the border
    CWPoint p1 = sideToKeep.get(1).p1;
    if ( p1 == p0 || p1 == p2 ) p1 = sideToKeep.get(1).p2;
    if ( volume( p0, p2, p1, vv ) > 0 ) { // if VV is on the positive side of the border, reverse the border
      p1 = p2; p2 = p0; p0 = p1;
      reverse = true;
    }
    
    // TDLog.v( "CW-Hull border " + p0.mCnt + " " + p1.mCnt + " p2 " + p2.mCnt );
    // s0.dump();
    ArrayList<CWSide> temp = new ArrayList<CWSide>();
    sideToKeep.remove( s0 );
    temp.add( s0 );
    while ( sideToKeep.size() > 0 ) {
      int k = 0;
      int size = sideToKeep.size();
      for ( ; k<size; ++k ) {
        CWSide s = sideToKeep.get(k);
        if ( s.contains(p2) ) {
          sideToKeep.remove( s );
          temp.add( s );
          // s.dump();
          p2 = s.otherPoint( p2 );
          break;
        }
      }
      if ( p2 == p0 ) {
        processTempSides( vv, temp, reverse );
        temp.clear();
        if ( sideToKeep.size() > 1 ) {
          s0 = sideToKeep.get(0);
          sideToKeep.remove( s0 );
          temp.add( s0 );
          p0 = s0.p1;
          p2 = s0.p2;
          reverse = false; // check if need to reverse the border
          p1 = sideToKeep.get(1).p1;
          if ( p1 == p0 || p1 == p2 ) p1 = sideToKeep.get(1).p2;
          if ( volume( p0, p2, p1, vv ) > 0 ) { // if VV is on the positive side of the border, reverse the border
            p1 = p2; p2 = p0; p0 = p1;
            reverse = true;
          }
        }          
      }
      if ( k == size ) {
        TDLog.Error( "CW-Hull Warning next side not found");
        // for ( int j=0; j<size; ++j  ) {
        //   CWSide side = sideToKeep.get(j);
        //   TDLog.v( "CW-Hull: " + j + ": " + side.p1.mCnt + " " + side.p2.mCnt );
        // }
        // dump();
        // TDLog.v( "CW-Hull sides to keep " + sideToKeep.size() );
        // for ( int j=0; j<sideToKeep.size(); ++j ) sideToKeep.get(j).dump();
        // TDLog.v( "CW-Hull sides to remove " + sideToRemove.size() );
        // for ( int j=0; j<sideToRemove.size(); ++j ) sideToRemove.get(j).dump();
        // TDLog.v( "CW-Hull vertexs to keep " + vertexToKeep.size() );
        // for ( int j=0; j<vertexToKeep.size(); ++j ) vertexToKeep.get(j).dump();
        // TDLog.v( "CW-Hull vertexs to remove " + vertexToRemove.size() );
        // for ( int j=0; j<vertexToRemove.size(); ++j ) vertexToRemove.get(j).dump();
        // TDLog.v( "CW-Hull faces to remove " + faceToRemove.size() );
        // for ( int j=0; j<faceToRemove.size(); ++j ) faceToRemove.get(j).dump();

        throw new RuntimeException("side not found");
      }
    }

    // now remove sides and triangles
    // TDLog.v( "CW-Hull removing V " + vertexToRemove.size() + " S " + sideToRemove.size() + " T " + faceToRemove.size() );
    for ( CWSide s2 : sideToRemove )     { removeSide( s2 ); }
    for ( CWTriangle f1 : faceToRemove ) { 
      for ( CWPoint v1 :vertexToKeep ) v1.removeTriangle( f1 );
      removeFace( f1 );
    }
    for ( CWPoint v1 : vertexToRemove )  { removeVertex( v1 ); }
    
    // cs = checkSideConsistency();
    // cv = checkVertexConsistency();
    // if ( ! cs || ! cv )
    //   TDLog.v( "CW-Hull consistency after: V " + mVertex.size() + " S " + mSide.size() + " T " + mFace.size() 
    //     + " S-check " + cs + " V-check " + cv );

    // remove small area triangles
    // TDLog.v( "CW-Hull small area V " + mVertex.size() + " S " + mSide.size() + " T " + mFace.size() );
    // dump();
    // TDLog.v( "CW-Hull check small area. nr T " + mFace.size() );
    boolean repeat = true;
    while ( repeat ) {
      repeat = false;
      for ( CWTriangle t : mFace ) {
        // TDLog.v( "CW-Hull Triangle " + t.mCnt + " area " + t.area() );
        if ( t.area() < 0.01f ) {
          double d12 = t.v1.distance3D( t.v2 );
          double d13 = t.v1.distance3D( t.v3 );
          double d23 = t.v3.distance3D( t.v2 );
          if ( d12 < d13 ) {
            if ( d12 < d23 ) {
              reduce( t, t.v2, t.v1, t.v3, t.s3, t.s1, t.s2 );
            } else {
              reduce( t, t.v3, t.v2, t.v1, t.s1, t.s2, t.s3 );
            }
          } else {
            if ( d13 < d23 ) {
              reduce( t, t.v1, t.v3, t.v2, t.s2, t.s3, t.s1 );
            } else {
              reduce( t, t.v3, t.v2, t.v1, t.s1, t.s2, t.s3 );
            }
          }
          repeat = true;
          break;
        }
      }
    }
    
    // TDLog.v( "CW-Hull insert point done");
    // dump();
    // checkVertexConsistency();
  }

  /*
           s4
  p4--------------p1
    \             /\
      \     t2  /s3  \
     s5\      /   t s2 \
  t3     \  /   s1       \
          p2--------------p3
        .'       tn         `.

   */
  private void reduce( CWTriangle t, CWPoint p2, CWPoint p1, CWPoint p3, CWSide s3, CWSide s1, CWSide s2  )
  {
    // TDLog.v( "CW-Hull reduce triangle " + t.mCnt + " " + p2.mCnt + " " + p1.mCnt + " " + p3.mCnt );

    CWTriangle t2 = s3.otherTriangle(t);
    CWTriangle tn = s1.otherTriangle(t);
	  
    CWSide s4 = null;
    CWSide s5 = null;
    if ( t2.s1 == s3 )      { s4 = t2.s2; s5 = t2.s3; }
    else if ( t2.s2 == s3 ) { s4 = t2.s3; s5 = t2.s1; }
    else if ( t2.s3 == s3 ) { s4 = t2.s1; s5 = t2.s2; }
	  
    CWPoint p4 = s4.otherPoint(p1);
	  
    CWTriangle t3 = s5.otherTriangle(t2);
    // TDTDLog.v( "CW-Hull  T2/3 " + t2.mCnt + " " + t3.mCnt +  " TN " + tn.mCnt
    //    + " S4/5 " + s4.mCnt + " " + s5.mCnt + " P4 " + p4.mCnt );
    
    p3.removeTriangle(t);
    p1.removeTriangle(t);
    p1.removeTriangle(t2);
    p4.removeTriangle(t2);
	  
    for ( CWTriangle tt : p2.mTriangle ) {
      if ( tt == t2 || tt == t ) continue;
      if ( tt == t3 ) {
        // change side s5 --> s4
        if ( tt.s1 == s5 )      { tt.s1 = s4; }
        else if ( tt.s2 == s5 ) { tt.s2 = s4; }
        else if ( tt.s3 == s5 ) { tt.s3 = s4; }
      }
      if ( tt == tn ) {
        // change side s1 --> s2
        if ( tt.s1 == s1 )      { tt.s1 = s2; }
        else if ( tt.s2 == s1 ) { tt.s2 = s2; }
        else if ( tt.s3 == s1 ) { tt.s3 = s2; }
      }
      // change vertex p2 --> p1
      if ( tt.v1 == p2 )      { tt.v1 = p1; }
      else if ( tt.v2 == p2 ) { tt.v2 = p1; }
      else if ( tt.v3 == p2 ) { tt.v3 = p1; }
      tt.s1.replacePoint( p2, p1 );
      tt.s2.replacePoint( p2, p1 );
      tt.s3.replacePoint( p2, p1 );
      tt.rebuildTriangle();
    }

    // TDLog.v( "CW-Hull removing T " + t.mCnt + " " + t2.mCnt 
    //   + " S " + s1.mCnt + " " + s3.mCnt + " " + s5.mCnt + " P " + p2.mCnt );
    mFace.remove( t );
    mFace.remove( t2 );
    mSide.remove( s1 );
    mSide.remove( s3 );
    mSide.remove( s5 );
    mVertex.remove( p2 );
    
    // TDLog.v( "CW-Hull after reducing");
    // dump( );
    // orderPointTriangles();
  }
  
  private boolean checkVertexConsistency()
  {
    boolean ret = true;
    for ( CWPoint p : mVertex ) {
      for ( CWTriangle t : mFace ) {
        if ( t.contains( p ) ) continue;
        double vol = t.volume( p );
        if ( vol < -0.01 ) {
          ret = false;
          // TDLog.v( "CW-Hull Inconsistent T " + t.mCnt + " with V " + p.mCnt + " vol " + vol );
        }
      }
    }
    return ret;
  }
  
  private boolean checkSideConsistency()
  {
    boolean ret = true;
    for ( CWSide s : mSide ) {
      CWTriangle t1 = s.t1;
      CWTriangle t2 = s.t2;
      if ( t1.contains( s ) && t2.contains(s) ) continue;
      // TDLog.v( "CW-Hull Inconsistent S " + s.mCnt + " with T " + t1.mCnt + "  " + t2.mCnt );
      ret = false;
    }
    return ret;
  }

  // boolean checkPoint( Vector3D v )
  // {
  //   // TDLog.v( "CW-Hull Check vector " + v.x + " " + v.y + " " + v.z );
  //   double totvol = 0;
  //   boolean ret = true;
  //   for ( CWTriangle t : mFace ) {
  //     double vol = t.volume( v );
  //     if ( vol <= -0.001f ) {
  //       // TDLog.v( "CW-Hull neg vol " + vol );
  //       // t.dump(); 
  //       ret = false;
  //     }
  //     totvol += vol;
  //   }
  //   if ( ret ) {
  //     totvol = Math.round( totvol*100 )/100;
  //     double angle = solidAngle( v, 0.0001f );
  //     TDLog.v( "CW-Hull Volume " + totvol + " " + ret + " angle " + angle );
  //   }
  //   return ret;
  // } 

  // void dump( )
  // {
  //   TDLog.v( "CW-Hull v " + mVertex.size() + " s " + mSide.size() + " t " + mFace.size() );
  //   // for ( CWPoint v :  mVertex ) v.dump( );
  //   // for ( CWSide s : mSide )     s.dump( );
  //   for ( CWTriangle f : mFace ) f.dump( );
  // }
  
  public void writeHull( PrintWriter out )
  {
    out.format(Locale.US, "C %d %d %d %d\n", mCnt, mVertex.size(), mSide.size(), mFace.size() );
    for ( CWPoint v :  mVertex ) v.writePoint( out );
    for ( CWSide s : mSide )     s.writeSide( out );
    for ( CWTriangle f : mFace ) f.writeTriangle( out );
    out.flush();
  }

  public void serialize( DataOutputStream dos ) throws IOException
  {
    dos.write('C');
    dos.writeInt( mVertex.size() );
    dos.writeInt( mSide.size() );
    dos.writeInt( mFace.size() );
    for ( CWPoint v :  mVertex ) v.serialize( dos );
    for ( CWSide s : mSide )     s.serialize( dos );
    for ( CWTriangle f : mFace ) f.serialize( dos );
    dos.write('E');
  }

  /* FIXME
  public void deserialize( DataInputStream dis ) throws IOException
  {
    char ch = dis.read();
    int nv = dis.readInt( );
    int ns = dis.readInt( );
    int nt = dis.readInt( );
    for ( int k=0; k<nv; ++k ) mVertex.add( CWPoint.deserialize( dis ) );
    for ( int k=0; k<ns; ++k ) mSide.add( CWSide.deserialize( dis, mVertex ) );
    for ( int k=0; k<nt; ++k ) mFace.add( CWTriangle.deserialize( dis, mVertex, mSide ) );
    ch = dis.read();
  }
  */


  // thr concavity threshold
  // eps point "coincidence" threshold
  private void makeConcave( ArrayList< Vector3D > pts, double distance_thr, double angle_thr, double eps )
  {
    ArrayList<Vector3D> insidePts = new ArrayList<Vector3D>();
    for ( Vector3D p : pts ) {
      if ( ! isVertex( p, eps ) ) insidePts.add( p );
    }
    // TDLog.v( "CW-Hull Make concave: inside points " + insidePts.size() );
    if ( insidePts.size() == 0 ) return;

    double diameter = computeDiameter();
    while ( true ) {
      Vector3D p0 = null;
      CWTriangle   t0 = null;
      double d0 = diameter;
      for ( Vector3D p : insidePts ) {
        for ( CWTriangle t : mFace ) {
          if ( t.hasPointAbove(p) ) {
            double d = t.distance(p);
            if ( d < d0 ) {
              p0 = p;
              t0 = t;
              d0 = d;
            }
          }
        }
      }
      if ( d0 > distance_thr ) break;
      double a0 = t0.maxAngleOfPoint( p0 );
      if ( a0 > angle_thr ) break;
      // TDLog.v( "CW-Hull concavity T " + t0.mCnt + " P " + p0.x + " " + p0.y + " " + p0.z );
      // TODO replace CWTriangle t0 with 3 Triangles in apex CWPoint p0
      // for the three sides s of t0:
      //   check if instead of CWTriangle with s and p0 
      //   should split the triangle on the other side of s 
      CWTriangle t1 = t0.s1.otherTriangle( t0 );
      CWTriangle t2 = t0.s2.otherTriangle( t0 );
      CWTriangle t3 = t0.s3.otherTriangle( t0 );
      if ( t1 == null || t2 == null || t3 == null ) return;

      CWPoint v0 = new CWPoint( p0.x, p0.y, p0.z );
      mVertex.add( v0 );
      dropTriangle( t0 );

      CWTriangle t31 = addTriangle( v0, t0.v1, t0.v2 ); // side t0.s3 
      CWTriangle t11 = addTriangle( v0, t0.v2, t0.v3 ); // side t0.s1 
      CWTriangle t21 = addTriangle( v0, t0.v3, t0.v1 ); // side t0.s2 
      CWPoint p3 = t3.oppositePointOf( t0.s3 );
      CWPoint p1 = t1.oppositePointOf( t0.s1 );
      CWPoint p2 = t2.oppositePointOf( t0.s2 );
      if ( v0.squareDistance3D(p3) < t0.v1.squareDistance3D(t0.v2) ) {
        reformDiamond( v0, p3, t0.v1, t0.v2, t3, t31, t0.s3 );
      }
      if ( v0.squareDistance3D(p1) < t0.v2.squareDistance3D(t0.v3) ) {
        reformDiamond( v0, p1, t0.v2, t0.v3, t1, t11, t0.s1 );
      }
      if ( v0.squareDistance3D(p2) < t0.v3.squareDistance3D(t0.v1) ) {
        reformDiamond( v0, p2, t0.v3, t0.v1, t2, t21, t0.s2 );
      }
      insidePts.remove( p0 );
    }
  }

  private void reformDiamond( CWPoint p0, CWPoint p3, CWPoint v1, CWPoint v2, CWTriangle t3, CWTriangle t31, CWSide s3 ) 
  {
    dropTriangle( t3 );
    dropTriangle( t31 );
    mSide.remove( s3 );
    addTriangle( v2, v1, p3 );
    addTriangle( v1, v2, p0 );
  }

  private void dropTriangle( CWTriangle t )
  {
    t.v1.removeTriangle( t );
    t.v2.removeTriangle( t );
    t.v3.removeTriangle( t );
    t.s1.removeTriangle( t );
    t.s2.removeTriangle( t );
    t.s3.removeTriangle( t );
    mFace.remove( t );
  }
  
  void orderPointTriangles()
  {
    for ( CWPoint p : mVertex ) p.orderTriangles();
  }
  
  private boolean isVertex( Vector3D p, double eps )
  {
    for ( CWPoint v : mVertex ) if ( v.distance3D(p) < eps ) return true;
    return false;
  }

  
  // private boolean isPointOnSurface( Vector3D p, double eps )
  // {
  //   for ( CWTriangle t : mFace ) if ( t.isInside( p ) ) return true;
  //   return false
  // }

  /**
   * @param eps surface uncertainty
   */
  double solidAngle( Vector3D p, double eps )
  {
    if ( isVertex(p, 0.0001f) ) return 0;
    if ( isOnSurface(p, eps) ) return 0;
    double ret = 0;
    for ( CWTriangle f : mFace ) ret += f.solidAngle(p);
    if ( Math.abs( ret ) < 0.001 ) return 0;
    return Math.round(0.25 * ret / Math.PI);
  }

  /** check if a point is inside this CW
   * @param p   point
   * @param eps
   */
  boolean isPointInside( Vector3D p, double eps ) { return solidAngle(p, eps) > 0.4f; }

  /** check if a point is on the surface of this CW (ie, inside a face)
   * @param v   3D point
   * @param eps tolerance
   */
  boolean isOnSurface( Vector3D v, double eps )
  {
    for ( CWTriangle t : mFace ) {
      if ( t.isPointInside(v,eps) ) return true;
    }
    return false;
  }
 
 
  /* points of the other CW that are inside this CW 
   * @param cv  the other CW
   * @param eps tolerance
   */
  List<CWPoint> computeInsidePoints( CWConvexHull cv, double eps )
  { 
    ArrayList<CWPoint> ret = new ArrayList<CWPoint>();
    for ( CWPoint p : cv.mVertex ) if ( isPointInside( p, eps ) ) ret.add( p );
    return ret;
  }

  // **********************************************************************
  
  /**
   * @param cv2    the other CW
   */
  ArrayList<CWIntersection> computeIntersection( CWConvexHull cv2 ) 
  {
    ArrayList<CWIntersection> ret = new ArrayList<CWIntersection>();
    
    for ( CWTriangle tb : cv2.mFace ) {
      for ( CWTriangle ta : mFace ) {
        Vector3D v = ta.intersectionBasepoint( tb );
        Vector3D n = ta.intersectionDirection( tb );
        CWLinePoint lpa1 = new CWLinePoint();
        CWLinePoint lpa2 = new CWLinePoint();
        CWLinePoint lpb1 = new CWLinePoint();
        CWLinePoint lpb2 = new CWLinePoint();
        if ( ta.intersectionPoints(v, n, lpa1, lpa2) && tb.intersectionPoints(v, n, lpb1, lpb2) ) {
          if ( lpa1.mAlpha > lpa2.mAlpha ) {
            CWLinePoint lpa = lpa1; lpa1= lpa2; lpa2 = lpa;
          }
          if ( lpb1.mAlpha > lpb2.mAlpha ) {
              CWLinePoint lpb = lpb1; lpb1= lpb2; lpb2 = lpb;
            }
          // now a1 < a2 and b1 < b2
          if ( lpa1.mAlpha < lpb1.mAlpha ) {
            if ( lpa2.mAlpha < lpb1.mAlpha ) { // a1 < a2 < b1 < b2
              // no intersection
            } else if ( lpa2.mAlpha < lpb2.mAlpha ) { // a1 < b1 < a2 < b2
              CWIntersection ii = new CWIntersection( 1, ta, tb, v, n );
              ii.mV1 = lpb1;
              ii.mV2 = lpa2;
              ret.add( ii );
            } else { // lpa2.mAlpha > lpb2.mAlpha
                CWIntersection ii = new CWIntersection( 2, ta, tb, v, n );
                ii.mV1 = lpb1;
                ii.mV2 = lpb2;
                ret.add( ii );
            }
          } else {
            if ( lpb2.mAlpha < lpa1.mAlpha ) { // b1 < b2 < a1 < a2
                // no intersection
              } else if ( lpb2.mAlpha < lpa2.mAlpha ) { // b1 < a1 < b2 < a2
                CWIntersection ii = new CWIntersection( 1, tb, ta, v, n );
                ii.mV1 = lpa1;
                ii.mV2 = lpb2;
                ret.add( ii );
              } else { // lpa2.mAlpha > lpb2.mAlpha
                  CWIntersection ii = new CWIntersection( 2, tb, ta, v, n );
                ii.mV1 = lpa1;
                ii.mV2 = lpa2;
                ret.add( ii );
              }
          }
        }
      }  
    }
    return ret;
  }

  
  //**********************************************************************

  // /** shrink a bit the convex hull by moving inside points that are on the surface on another cv
  //  *  
  //  * @param cv the other cw
  //  * @param delta   amount to move towards the center
  //  * @return true if at least one point has moved
  //  */
/*
  private boolean regularizePoints( CWConvexHull cv, double delta, double eps )
  {
    boolean ret = false;
    Vector3D c1 = getCenter();
    for ( CWPoint p : mVertex ) {
      if ( cv.isOnSurface( p, eps ) ) {
        Vector3D dp = c1.difference(p);
        dp.scaleBy( delta / dp.length() );
        p.add( dp );
        ret = true;
      }
    }
    return ret;
  }
  
  private boolean regularizeSides( CWConvexHull cv, double delta, double eps )
  {
    boolean ret = false;
    for ( CWSide s1 : mSide ) {
      Vector3D d1 = s1.p2.difference( s1.p1 );
      double d11 = d1.dotProduct( d1 );
      for ( CWSide s2 : cv.mSide ) {
        Vector3D q = s1.p1.difference( s2.p1 );
        Vector3D d2 = s2.p2.difference( s2.p1 );
        double d22 = d2.dotProduct( d2 );
        double d12 = d1.dotProduct( d2 );
        double det = d11 * d22 - d12 * d12;
        double q1 = q.dotProduct( d1 );
        double q2 = q.dotProduct( d2 );
        double a = ( -d22 * q1 + d12 * q2 )/det;
        double b = ( -d12 * q1 + d11 * q2 )/det;
        if ( a < 0 || a > 1 || b < 0 || b > 1 ) continue;
        Vector3D d = new Vector3D( q.x + a*d1.x - b*d2.x, q.y + a*d1.y - b*d2.y, q.z + a*d1.z - b*d2.z );
        
        if ( d.length() < delta ) {
          Vector3D n = d1.crossProduct( d2 );
          n.normalized();
          Vector3D dn = n.crossProduct(d);
          // TDLog.v( "CW-Hull DN " + dn.x + " " + dn.y + " " + dn.z );
          n.scaleBy( delta/2 );
          if ( n.dotProduct(d) >= 0.0f ) {
            s1.p1.add( n ); s1.p2.add( n ); // N.B. d1 is unchanged
            s2.p1.sub( n ); s2.p2.sub( n );
          } else {
            s1.p1.sub( n ); s1.p2.sub( n );
            s2.p1.add( n ); s2.p2.add( n );
          }
          ret = true;
        }
      }
    }
    return ret;
  }
  
  private static boolean regularizePoints( CWConvexHull cv1, CWConvexHull cv2, double delta, double eps )
  {
    return cv1.regularizePoints(cv2, delta, eps ) || cv2.regularizePoints(cv1, delta, eps );
  }
  
  private static boolean regularizeSides( CWConvexHull cv1, CWConvexHull cv2, double delta, double eps )
  {
    return cv1.regularizeSides(cv2, delta, eps ) || cv2.regularizeSides(cv1, delta, eps );
  }
  
  static void regularize( CWConvexHull cv1, CWConvexHull cv2, double delta, double eps )
  {
    while ( regularizePoints( cv1, cv2, delta, eps ) || regularizeSides( cv1, cv2, delta, eps  ) ) ;
  }
  
*/  

  void randomizePoints( double delta )
  {
    for ( CWPoint v : mVertex ) {
      v.randomize( delta );
    } 
    for ( CWTriangle t : mFace ) t.computeVectors();
    for ( CWSide s : mSide ) s.computeU12();
  }

  /** compute the volume of this CW
   * @return volume
   */
  private double computeVolume()
  {
    if ( mVertex.size() < 4 ) return 0.0f;
    Vector3D cc = getCenter();
    double vol = 0;
    for ( CWTriangle t : mFace ) {
      vol += t.volume( cc );
    }
    return vol;
  }


  // there is a problem with this method
  // the two end-point for the piece of border inside a triangle are not shared
  // with adjacent triangles
  // @param index index of the cw
  // @param ints
  // @param pts
  synchronized void splitTriangles( int index, List< CWIntersection > ints, List< CWPoint > pts )
  {
    int ns = ints.size();
    if ( ns == 0 ) return;
    // for ( CWPoint p : pts ) TDLog.v( "CW-Hull pts in " + p.mCnt + " ints " + ns );
    // mSplits.clear();

    CWPoint[] vts = new CWPoint[3];
    CWPoint w1 = null, w2 = null, w3 = null;
    CWSide  z1 = null, z2 = null, z3 = null;
    for ( CWTriangle t : mFace ) {
      int tcnt = t.mCnt;
      int nv = t.countVertexIn( pts, vts );
      if ( nv == 0 ) continue;
      t.mType = CWTriangle.TRIANGLE_HIDDEN;
      // TDLog.v( "CW-Hull Tri " + t.mCnt + " " + t.v1.mCnt + " " + t.v2.mCnt + " " + t.v3.mCnt + " nv " + nv );
      if ( nv == 3 ) continue;

      if ( nv == 1 ) {
        if (      vts[0] == t.v1 ) { w1 = t.v1; w2 = t.v2; w3 = t.v3; z1 = t.s1; z2 = t.s2; z3 = t.s3; }
        else if ( vts[0] == t.v2 ) { w1 = t.v2; w2 = t.v3; w3 = t.v1; z1 = t.s2; z2 = t.s3; z3 = t.s1; }
        else if ( vts[0] == t.v3 ) { w1 = t.v3; w2 = t.v1; w3 = t.v2; z1 = t.s3; z2 = t.s1; z3 = t.s2; }
        else {
          TDLog.Error( "CW-Hull Error cannot find one vertex");
          return;
        }
      } else if ( nv == 2 ) {
        if (        ( vts[0] == t.v2 && vts[1] == t.v3 ) || ( vts[0] == t.v3 && vts[1] == t.v2 ) ) {
          w1 = t.v1; w2 = t.v2; w3 = t.v3; z1 = t.s1; z2 = t.s2; z3 = t.s3; 
        } else if ( ( vts[0] == t.v3 && vts[1] == t.v1 ) || ( vts[0] == t.v1 && vts[1] == t.v3 ) ) {
          w1 = t.v2; w2 = t.v3; w3 = t.v1; z1 = t.s2; z2 = t.s3; z3 = t.s1; 
        } else if ( ( vts[0] == t.v1 && vts[1] == t.v2 ) || ( vts[0] == t.v2 && vts[1] == t.v1 ) ) {
          w1 = t.v3; w2 = t.v1; w3 = t.v2; z1 = t.s3; z2 = t.s1; z3 = t.s2; 
        } else {
          TDLog.Error( "CW-Hull Error cannot find two vertex");
          return;
        }
      }
 
      ArrayList< CWPoint > pts2 = new ArrayList< CWPoint > ();
      for ( int k = 0; k < ns; ++k ) {
        CWIntersection ii = ints.get( k % ns );
        if ( tcnt == ii.mSign[2] ) { 
          int start = k;
          int end = k;
          boolean inside = true;
          do {
            ii = ints.get( k % ns );
            if ( tcnt == ii.mSign[0] ) {
              end = k;
              inside = false;
            } else {
              ++ k;
            }
          } while ( inside && k < 3*ns );
          if ( k >= 3*ns ) {
            TDLog.Error( "CW-Hull Error triangle with " + nv + " pts inside. cannot get end intersection");
            break;
          }
          // TDLog.v( "CW-Hull now process " + start + "--" + end );
          if ( nv == 1 ) {
            split1( index, w1, w2, w3, z1, z2, z3, start, end, ints, ns );
          } else {
            CWPoint p = split2( index, w1, w2, w3, z1, z2, z3, start, end, ints, ns );
            if ( p != null ) pts2.add( p );
          }
        }
      }
      // if ( pts2.size() > 0 ) {
      //   TDLog.v( "CW-Hull  split with 2 vertex inside. apposite-side points " + pts2.size() );
      // }
    }
    for ( CWTriangle t : mSplits ) mFace.add( t );
    mSplits.clear();
  }


  private void addSplitTriangle( CWPoint p1, CWPoint p2, CWPoint p3, CWSide z1, CWSide z2, CWSide z3 )
  {
    // TDLog.v( "CW-Hull split Triangle " + p1.mCnt + " " + p2.mCnt + " " + p3.mCnt );
    CWTriangle t = new CWTriangle( p1, p2, p3, z1, z2, z3 );
    t.mType = CWTriangle.TRIANGLE_SPLIT;
    z1.setTriangle( t );
    z2.setTriangle( t );
    z3.setTriangle( t );
    mSplits.add( t );
  }

  // add triangles at a vertex counterclockwise
  // param t index of the CW
  private CWPoint addTrianglesAtVertexCCW( int t, CWPoint ww, CWPoint p1, int ks, int ke, List<CWIntersection> ints, int ns )
  {
    // TDLog.v( "CW-Hull cw " + t + " add CCW " + p1.mCnt + " K " + ks + " " + ke );
    addVertex( p1 );
    CWSide r0 = getSide( ww, p1 );
    for ( int k = ks; k < ke; ++k ) {
      CWIntersection ii = ints.get( k % ns );
      CWPoint p2 = ii.point2( t );
      // TDLog.v( "CW-Hull vertices " + k + " ii " + ii.mCnt + " pts " + p1.mCnt + " " + p2.mCnt );
      addVertex( p2 );
      CWSide r1 = getSide( p2, p1 );
      CWSide r2 = getSide( ww, p2 );
      addSplitTriangle( ww, p1, p2, r1, r2, r0 );
      p1 = p2;
      r0 = r2;
    }
    return p1;
  }

  // add triangles at a vertex clockwise
  private CWPoint addTrianglesAtVertexCW( int t, CWPoint ww, CWPoint p1, int ks, int ke, List<CWIntersection> ints, int ns )
  {
    addVertex( p1 );
    CWSide r0 = getSide( ww, p1 );
    for ( int k = ks; k < ke; ++k ) {
      CWIntersection ii = ints.get( k % ns );
      CWPoint p2 = ii.point2( t );
      addVertex( p2 );
      CWSide r1 = getSide( p2, p1 );
      CWSide r2 = getSide( ww, p2 );
      addSplitTriangle( ww, p2, p1, r1, r0, r2 );
      p1 = p2;
      r0 = r2;
    }
    return p1;
  }

  // w1 is inside the other CW
  // @param t  index of the CW
  private void split1( int t, CWPoint w1, CWPoint w2, CWPoint w3, CWSide z1, CWSide z2, CWSide z3, 
                          int ks, int ke, List<CWIntersection> ints, int ns )
  {
    CWIntersection is = ints.get( ks );
    CWIntersection ie = ints.get( ke % ns );
    CWLinePoint vs = is.mV1;
    CWLinePoint ve = ie.mV1;
    CWSide ss = vs.mSide;
    CWSide se = ve.mSide;
    // TDLog.v( "CW-Hull " + t + " split-1 " + w1.mCnt + " " + w2.mCnt + " " + w3.mCnt + " sides "
    //                 + z1.mCnt + " " + z2.mCnt + " " + z3.mCnt  + " int sides " + ss.mCnt + " " + se.mCnt );
    CWPoint ps = is.point1( t );
    if ( ss == z2 && se == z3 ) {
      //            w1
      //          /    \ z2
      //     se /---<----\ ss
      //   w2 /____________\ w3
      int k = ks;
      CWIntersection ii = ints.get( k % ns );
      int kmin = k;
      double dmin = w2.distance3D( ii.mV2 ) + w3.distance3D( ii.mV2 );
      for ( ; k < ke; ++k ) {
        ii = ints.get( k % ns );
        double d = w2.distance3D( ii.mV2 ) + w3.distance3D( ii.mV2 );
        if ( d < dmin ) { dmin = d; kmin = k; }
      }
      // TDLog.v( "CW-Hull [1] ks " + ks + " ke " + ke + " kmin " + kmin );
      CWPoint p1 = (ks < kmin ) ? addTrianglesAtVertexCCW( t, w3, ps, ks, kmin, ints, ns ) : ps;
      CWSide r0 = getSide( p1, w2 );
      CWSide r2 = getSide( p1, w3 );
      addSplitTriangle( w2, w3, p1, r2, r0, z1 );
      if ( kmin < ke ) addTrianglesAtVertexCCW( t, w2, p1, kmin, ke, ints, ns );
    } else if ( ss == z3 && se == z2 ) {
      //            w1
      //          /    \ z2
      //     ss /--->----\ se
      //   w2 /____________\ w3
      int k = ks;
      CWIntersection ii = ints.get( k % ns );
      int kmin = k;
      double dmin = w2.distance3D( ii.mV2 ) + w3.distance3D( ii.mV2 );
      for ( ; k < ke; ++k ) {
        ii = ints.get( k % ns );
        double d = w2.distance3D( ii.mV2 ) + w3.distance3D( ii.mV2 );
        if ( d < dmin ) { dmin = d; kmin = k; }
      }
      // TDLog.v( "CW-Hull [2] ks " + ks + " ke " + ke + " kmin " + kmin );
      CWPoint p1 = (ks < kmin )? addTrianglesAtVertexCW( t, w2, ps, ks, kmin, ints, ns ) : ps;
      CWSide r0 = getSide( p1, w2 );
      CWSide r2 = getSide( p1, w3 );
      addSplitTriangle( w2, w3, p1, r2, r0, z1 );
      if ( kmin < ke ) addTrianglesAtVertexCW( t, w3, p1, kmin, ke, ints, ns );
    } else if ( ss == z2 && se == z1 ) {
      //            w1
      //          /    \ ss
      //     z3 /      / \ z2
      //   w2 /______/_____\ w3
      //           se
      addTrianglesAtVertexCCW( t, w3, ps, ks, ke, ints, ns );
    } else if ( ss == z3 && se == z1 ) {
      //            w1
      //       ss /    \   
      //     z3 / \      \ z2
      //   w2 /_____\______\ w3
      //             se
      addTrianglesAtVertexCW( t, w2, ps, ks, ke, ints, ns );
    } else if ( ss == z1 && se == z2 ) {
      //            w1
      //          /    \ se
      //     z3 /      / \ z2
      //   w2 /______/_____\ w3
      //           ss
      addTrianglesAtVertexCW( t, w3, ps, ks, ke, ints, ns );
    } else if ( ss == z1 && se == z3 ) {
      //            w1
      //       se /    \   
      //     z3 / \      \ z2
      //   w2 /_____\______\ w3
      //             ss
      addTrianglesAtVertexCCW( t, w2, ps, ks, ke, ints, ns );
    }
  }

  // w2 and w3 are inside the other CW
  private CWPoint split2( int t, CWPoint w1, CWPoint w2, CWPoint w3, CWSide z1, CWSide z2, CWSide z3, 
                          int ks, int ke, List<CWIntersection> ints, int ns )
  {
    CWIntersection is = ints.get( ks );
    CWIntersection ie = ints.get( ke % ns );
    CWLinePoint vs = is.mV1;
    CWLinePoint ve = ie.mV1;
    CWSide ss = vs.mSide;
    CWSide se = ve.mSide;
    // TDLog.v( "CW-Hull " + t + " split-2 " + w1.mCnt + " " + w2.mCnt + " " + w3.mCnt + " sides "
    //                 + z1.mCnt + " " + z2.mCnt + " " + z3.mCnt  + " int sides " + ss.mCnt + " " + se.mCnt );
    CWPoint ps = is.point1( t );
    if ( ss == z2 && se == z3 ) {
      //            w1
      //          /    \ z2
      //     se /---<----\ ss
      //   w2 /____________\ w3
      addTrianglesAtVertexCW( t, w1, ps, ks, ke, ints, ns );
    } else if ( ss == z3 && se == z2 ) {
      //            w1
      //          /    \ z2
      //     ss /--->----\ se
      //   w2 /____________\ w3
      addTrianglesAtVertexCCW( t, w1, ps, ks, ke, ints, ns );
    } else if ( ss == z2 && se == z1 ) {
      //            w1
      //          /    \ ss
      //     z3 /      / \ z2
      //   w2 /______/_____\ w3
      //           se
      return addTrianglesAtVertexCW( t, w1, ps, ks, ke, ints, ns );
    } else if ( ss == z3 && se == z1 ) {
      //            w1
      //       ss /    \   
      //     z3 / \      \ z2
      //   w2 /_____\______\ w3
      //             se
      return addTrianglesAtVertexCCW( t, w1, ps, ks, ke, ints, ns );
    } else if ( ss == z1 && se == z2 ) {
      //            w1
      //          /    \ se
      //     z3 /      / \ z2
      //   w2 /______/_____\ w3
      //           ss
      addTrianglesAtVertexCCW( t, w1, ps, ks, ke, ints, ns );
      return ps;
    } else if ( ss == z1 && se == z3 ) {
      //            w1
      //       se /    \   
      //     z3 / \      \ z2
      //   w2 /_____\______\ w3
      //             ss
      addTrianglesAtVertexCW( t, w1, ps, ks, ke, ints, ns );
      return ps;
    }
    return null;
  }

}
