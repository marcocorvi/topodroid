/** @file CWSide.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief face side
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.cw;

import com.topodroid.Cave3X.Vector3D;

import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Locale;

import com.topodroid.utils.TDLog;

/**
 * oriented side (as seen from outside)
 * 
 *       /     \
 *      v       ^
 *     /    t2   \
 *   p1 ----->----P2
 *     \ <--t1-- /
 *      \       /
 */
public class CWSide
{
  static int cnt = 0;
  static void resetCounter() { cnt = 0; }

  int mCnt;
  CWPoint p1, p2;
  Vector3D u12;
  CWTriangle t1; // oriented opposite to the side: points are p2-p1 in t1
  CWTriangle t2; // oriented as the side: points are p1-p2 in t2

  public CWSide( CWPoint p1, CWPoint p2 )
  {
    mCnt = cnt++;
    this.p1 = p1;
    this.p2 = p2;
    computeU12();
    t1 = null;
    t2 = null;
  }
  
  public CWSide( int tag, CWPoint p1, CWPoint p2 )
  {
    mCnt = tag;
    if ( cnt <= tag ) cnt = tag+1;
    this.p1 = p1;
    this.p2 = p2;
    computeU12();
    t1 = null;
    t2 = null;
  }

  public void computeU12()
  {
    u12 = p2.difference(p1);
    u12.normalized();
  }

  public boolean areTrianglesOutside()
  {
    if ( t1 != null && ! t1.isOutside() ) return false;
    if ( t2 != null && ! t2.isOutside() ) return false;
    return true;
  }
  
  public CWTriangle otherTriangle( CWTriangle t )
  {
    if ( t == t1 ) return t2;
    if ( t == t2 ) return t1;
    return null;
  }

  // return the old triangle
  public CWTriangle setTriangle( CWTriangle t )
  {
    CWTriangle ret = null;
    if ( ( t.v1 == p1 && t.v2 == p2 ) || ( t.v2 == p1 && t.v3 == p2 ) || ( t.v3 == p1 && t.v1 == p2 ) ) {
      // triangle oriented as side
      ret = t2;
      t2 = t;
    } else if ( ( t.v1 == p2 && t.v2 == p1 ) || ( t.v2 == p2 && t.v3 == p1 ) || ( t.v3 == p2 && t.v1 == p1 ) ) {
      // triangle oriented opposite to side
      ret = t1;
      t1 = t;
    }
    return ret;
  }

  public void removeTriangle( CWTriangle t )
  {
    if ( t == t1 ) t1 = null;
    if ( t == t2 ) t2 = null;
  }

  public boolean replacePoint( CWPoint pold, CWPoint pnew )
  {
    if ( p1 == pold ) { 
      p1 = pnew; 
    } else if ( p2 == pold ) {
      p2 = pnew;
    } else {
      return false;
    }
    // FIXME recompute direction ?
    // u12 = p2.minus( p1 );
    return true;
  }
  
  public boolean contains( CWPoint p ) { return p == p1 || p == p2; }
  
  CWPoint otherPoint( CWPoint p ) { return ( p == p1 )? p2 : ( p == p2)? p1 : null; }

  // sine of the angle (p2-p1)^(v-p1) [with sign]
  public double cross( Vector3D v )
  {
    Vector3D vp1 = v.difference( p1 );
    vp1.normalized();
    return u12.crossProduct( vp1 ).length();
  }

  // void dump( )
  // {
  //   TDLog.v( "CW Side " + mCnt + " P " + p1.mCnt + "-" + p2.mCnt 
  //               + " T " + ((t1 == null)? "-" : t1.mCnt) + " " + ((t2 == null)? "-" : t2.mCnt)
  //   );
  // }

  public void writeSide( PrintWriter out )
  {
    Vector3D dp = p2.difference(p1);
    out.format(Locale.US, "S %d %d %d %d %d %.3f %.3f %.3f\n", mCnt, p1.mCnt, p2.mCnt,
                ((t1 == null)? -1 : t1.mCnt), ((t2 == null)? -1 : t2.mCnt), dp.x, dp.y, dp.z );
  }

  public void serialize( DataOutputStream dos ) throws IOException
  {
    Vector3D dp = p2.difference(p1);
    dos.write('S');
    dos.writeInt( mCnt );
    dos.writeInt( p1.mCnt );
    dos.writeInt( p2.mCnt );
    dos.writeInt( ((t1 == null)? -1 : t1.mCnt) );
    dos.writeInt( ((t2 == null)? -1 : t2.mCnt) );
    dos.writeDouble( dp.x );
    dos.writeDouble( dp.y );
    dos.writeDouble( dp.z );
  }

  /* FIXME
  static public CWSide deserialize( DataInputStream dis, List<CWPoint> pts ) throws IOException
  {
    Vector3D dp = p2.difference(p1);
    dis.read('S');
    int cnt  = dis.readInt( );
    int cnt1 = dis.readInt( );
    int cnt2 = dis.readInt( );
    CWPoint p1 = getPoint( cnt1, pts );
    CWPoint p2 = getPoint( cnt2, pts );
    int t1 = dis.readInt( );
    int t2 = dis.readInt( );
    double x = dis.readDouble( );
    double y = dis.readDouble( );
    double z = dis.readDouble( );
    return new CWSide( cnt, p1, p2 );
  }

  static private CWPoint getPoint( int tag, List<CWPoint> pts )
  {
    for ( CWPoint pt : pts ) if ( pt.mCnt == tag ) return pt;
    return null;
  }
  */

}

