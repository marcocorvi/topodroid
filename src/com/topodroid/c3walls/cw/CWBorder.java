/** @file CWBorder.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief CW intersection border
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.cw;

import com.topodroid.DistoX.Vector3D;

import java.util.ArrayList;
import java.util.List;

import java.util.Locale;

import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CWBorder 
{
  static int cnt = 0;
  static void resetCounter() { cnt = 0; }

  int mCnt;
  CWConvexHull mCV1;  // first CW
  CWConvexHull mCV2;  // second CW
  ArrayList< CWIntersection > mInts;
  List<CWPoint> pts2in1;  // points of the second CW inside the first CW
  List<CWPoint> pts1in2;  // points of the first CW inside the second CW
  double mVolume;
  boolean hasVolume;
  
  public CWBorder( CWConvexHull cv1, CWConvexHull cv2, double eps )
  {
    mCnt = cnt ++;
    mCV1 = cv1;
    mCV2 = cv2;
    mInts = new ArrayList< CWIntersection >();
    pts2in1 = cv1.computeInsidePoints( cv2, eps ); // points of cv2 inside cv1
    pts1in2 = cv2.computeInsidePoints( cv1, eps );
    // TDLog.v( "Border " + mCnt + ": " + pts2in1.size() + " points of " + cv2.mCnt + " in " + cv1.mCnt 
    //      + " " + pts1in2.size() + " points of " + cv1.mCnt + " in " + cv2.mCnt );
    hasVolume = false;
  }

  double getVolume()
  {
    if ( ! hasVolume ) {
      mVolume = computeVolume( 0.00001f );
      hasVolume = true;
    }
    return mVolume;
  }


  int size() { return mInts.size(); }
  
  boolean makeBorder()
  {
    // mCV1.dump();
    // mCV2.dump();
    ArrayList<CWIntersection> ints = mCV1.computeIntersection( mCV2 );
    // int sz0 = ints.size();
    for ( CWIntersection ii : ints ) {
      ii.makeSignature();
      // ii.dump();
    }
    // TDLog.v("make border. nr ints " + ints.size() );

    boolean ret = orderIntersections( ints );
    // int sz = ints.size();
    // TDLog.v("make border. order ints " + ret + " nr " +  mInts.size() );

    int ns = mInts.size();
    for ( int k = 0; k < ns; ++ k ) {
      CWIntersection i1 = mInts.get( k );
      CWIntersection i2 = mInts.get( (k+1)%ns );
      i1.setNext( i2 );
    }
    // TDLog.v("Border " + mCnt + " size " + mInts.size() );
    // for ( CWIntersection ii : mInts ) {
    //   ii.dump();
    // }
      
    return ret;
  }

  void splitCWTriangles( )
  {
    // TDLog.v("split CW ints: " + mInts.size() + " pts2in1 " + pts2in1.size() + " pts1in2 " + pts1in2.size() );
    mCV1.splitTriangles( 1, mInts, pts1in2 );
    mCV2.splitTriangles( 2, mInts, pts2in1 );
    // TDLog.v("split CW done");
  }

  // ---------------------------------------------------------------------------
  
  /** sort a list of intersection into this border
   */
  private boolean orderIntersections( ArrayList<CWIntersection> ints )
  {
    if ( ints.size() == 0 ) return false;
    mInts.clear();
    CWIntersection i1 = ints.get(0);
    ints.remove( i1 );
    mInts.add( i1 );

    while ( ints.size() > 0 ) {
      int f = 0;
      for ( CWIntersection i2 : ints ) {
        if ( i2.followSignatureDirect( i1 ) ) {
          f = 1;
          i1 = i2;
          break;
        }
      }
      if ( f != 1 ) {
        for ( CWIntersection i2 : ints ) {
          if ( i2.followSignatureInverse( i1 ) ) {
            f = -1;
            i1 = i2;
            break;
          }
        }
      }
      if ( f == 0 ) break;
      ints.remove( i1 );
      if ( f == -1 ) i1.reverse();
      mInts.add( i1 );
    }
    return ( ints.size() == 0 );
  }

  private Vector3D getCenter()
  {
    if ( mInts.size() == 0 ) return null;
    Vector3D ret = new Vector3D();
    for ( CWIntersection ii : mInts ) {
      ret.x += ii.mV1.x + ii.mV2.x;
      ret.y += ii.mV1.y + ii.mV2.y;
      ret.z += ii.mV1.z + ii.mV2.z;
    }
    double div = 1.0/(2.0*mInts.size());
    ret.x *= div;
    ret.y *= div;
    ret.z *= div;
    return ret;
  }
  
  static private double volume( Vector3D v0, Vector3D v1, Vector3D v2, Vector3D v3 )
  {
    Vector3D u1 = v1.difference(v0);
    Vector3D u2 = v2.difference(v0);
    Vector3D u3 = v3.difference(v0);
    return Math.abs( u1.crossProduct(u2).dotProduct(u3) );
  }

  /** Compute the volume (*6) of the triangles than enter the first CW
   *
   * @param t21  set of triangles of the second CW that enter the first
   * @param p21  points of the second CW inside the first
   * @return volume (*6)
   */
  private double computeVolumeOf( List<CWTriangle> t21, List<CWPoint> p21, Vector3D cc )
  {
    double vol =0;
    CWPoint[] pts = new CWPoint[3];
    for ( CWTriangle t2 : t21 ) {
      switch ( t2.countVertexIn( p21, pts ) ) {
        case 0: break;
        case 1:
          for ( CWIntersection ii : mInts ) {
            if ( ii.mV1.mTri == t2 || ii.mV2.mTri == t2 ) {
              vol += Math.abs( volume( cc, ii.mV1, ii.mV2, pts[0] ) );
            }
          }
          break;
        case 2:
          vol += t2.volume(cc);
          CWPoint v = t2.v1;
          if ( v == pts[0] || v == pts[1] ) v = t2.v2;
          if ( v == pts[0] || v == pts[1] ) v = t2.v3;
          for ( CWIntersection ii : mInts ) {
            if ( ii.mV1.mTri == t2 || ii.mV2.mTri == t2 ) {
              vol -= Math.abs( volume( cc, ii.mV1, ii.mV2, pts[0] ) );
            }
          }
          break;
        case 3:
          vol += Math.abs( t2.volume( cc ) );
          break;
      }
    }
    return vol;
  }
  
  /**
   * compute the volume (*6) of the intersection
   * @param eps
   * @return volume (*6)
   */
  private double computeVolume( double eps )
  {
    Vector3D cc = getCenter();
    if ( cc == null ) return 0;
    
    List<CWPoint> pts2in1 = mCV1.computeInsidePoints( mCV2, eps ); // points of cv2 inside cv1
    List<CWPoint> pts1in2 = mCV2.computeInsidePoints( mCV1, eps );
    // TDLog.v("Pts1in2 " + pts1in2.size() + " Pts2in1 " + pts2in1.size() );
    
    if ( pts1in2.size() == 0 && pts2in1.size() == 0 ) return 0;
    double vol = 0;
    
    List< CWTriangle > t1in2 = new ArrayList<CWTriangle>();
    List< CWTriangle > t2in1 = new ArrayList<CWTriangle>();
    
    for ( CWPoint p2 : pts2in1 ) {
      for ( CWTriangle t2 : p2.mTriangle ) {
        if ( ! t2in1.contains(t2) ) t2in1.add( t2 );
      }
    }
    for ( CWPoint p1 : pts1in2 ) {
      for ( CWTriangle t1 : p1.mTriangle ) {
        if ( ! t1in2.contains(t1) ) t1in2.add( t1 );
      }
    }
   
    vol += computeVolumeOf( t1in2, pts1in2, cc );
    vol += computeVolumeOf( t2in1, pts2in1, cc );
	  
    return vol;
  }

  // ---------------------------------------------------------------------------
  
  // void dump( PrintStream out )
  // {
  //   for ( CWIntersection ii : mInts ) {
  //     ii.dump(out, 1);
  //   }
  // }
  
  public void writeBorder( PrintWriter out )
  {
    out.format(Locale.US,  "B %d %d %d %d %d %d\n", mCnt, mCV1.mCnt, mCV2.mCnt, mInts.size(), pts2in1.size(), pts1in2.size() );
    for ( CWIntersection ii : mInts ) ii.writeIntersection( out );
    for ( CWPoint p2 : pts2in1 )      p2.writePoint( out );
    for ( CWPoint p1 : pts1in2 )      p1.writePoint( out );
    out.flush();
  }

  public void serialize( DataOutputStream dos ) throws IOException
  {
    dos.write('B');
    dos.writeInt( mCnt );
    dos.writeInt( mCV1.mCnt );
    dos.writeInt( mCV2.mCnt );
    dos.writeInt( mInts.size() );
    dos.writeInt( pts2in1.size() );
    dos.writeInt( pts1in2.size() );
    for ( CWIntersection ii : mInts ) ii.serialize( dos );
    for ( CWPoint p2 : pts2in1 )      p2.serialize( dos );
    for ( CWPoint p1 : pts1in2 )      p1.serialize( dos );
    dos.write('E');
  }

}



