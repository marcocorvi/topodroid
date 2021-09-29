/** @file CWTriangle.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief face triangle
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.cw;

import com.topodroid.DistoX.Vector3D;

import java.util.List;
import java.util.Locale;

import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.topodroid.utils.TDLog;

public class CWTriangle extends CWFacet
{
  private static int cnt = 0;
  static void resetCounter() { cnt = 0; }

  int mCnt;
  CWSide s1, s2, s3;

  final static int TRIANGLE_NORMAL = 0;
  final static int TRIANGLE_HIDDEN = 1;
  final static int TRIANGLE_SPLIT  = 2;
  int mType;
  
  // private Vector3D mVolume;
  // private double mVolumeOffset;

  private boolean mOutside; // work variable
  

  public CWSide nextWithPoint( CWSide s, CWPoint p )
  {
    if ( s == s1 ) {
  	  if ( s2.contains(p) ) return s2;
  	  if ( s3.contains(p) ) return s3;
    } else if ( s == s2 ) {
  	  if ( s1.contains(p) ) return s1;
  	  if ( s3.contains(p) ) return s3;
    } else if ( s == s3 ) {
  	  if ( s1.contains(p) ) return s1;
  	  if ( s2.contains(p) ) return s2;
    }
    return null;
  }
  
  public CWSide leftSideOf( CWPoint p ) // left is prev
  {
    if ( p == v1 ) return s2;
    if ( p == v2 ) return s3;
    if ( p == v3 ) return s1;
    return null;
  }
  
  public CWSide rightSideOf( CWPoint p ) // right is next
  {
    if ( p == v1 ) return s3;
    if ( p == v2 ) return s1;
    if ( p == v3 ) return s2;
    return null;
  }
  
  public CWSide oppositeSideOf( CWPoint p )
  {
    if ( p == v1 ) return s1;
    if ( p == v2 ) return s2;
    if ( p == v3 ) return s3;
    return null;
  }
  
  public CWPoint oppositePointOf( CWSide s )
  {
    if ( s == s1 ) return v1;
    if ( s == s2 ) return v2;
    if ( s == s3 ) return v3;
    return null;
  }

  /** create a triangle on three points
   * Note each side can be null (if so it is created)
   */
  public CWTriangle( CWPoint v1, CWPoint v2, CWPoint v3, CWSide s1, CWSide s2, CWSide s3 )
  {
    super( v1, v2, v3 );
    mCnt  = cnt ++;
    mType = TRIANGLE_NORMAL;
    buildTriangle( s1, s2, s3 );
  }
  
  public CWTriangle( int tag, CWPoint v1, CWPoint v2, CWPoint v3, CWSide s1, CWSide s2, CWSide s3 )
  {
    super( v1, v2, v3 );
    mCnt  = tag;
    if ( cnt <= tag ) cnt = tag+1;
    mType = TRIANGLE_NORMAL; // FIXME
    buildTriangle( s1, s2, s3 );
  }
  
  public void rebuildTriangle()
  {
    v1.removeTriangle( this );
    v2.removeTriangle( this );
    v3.removeTriangle( this );
    // buildFacet( v1, v2, v3 ); // FIXME maybe not necessary
    buildTriangle( s1, s2, s3 );
  }
  
  private void buildTriangle( CWSide s1, CWSide s2, CWSide s3 )
  {
    this.s1 = (s1 == null) ? new CWSide( v2, v3 ) : s1;
    this.s2 = (s2 == null) ? new CWSide( v3, v1 ) : s2;
    this.s3 = (s3 == null) ? new CWSide( v1, v2 ) : s3;
    this.s1.setTriangle( this );
    this.s2.setTriangle( this );
    this.s3.setTriangle( this );
    this.v1.addTriangle( this );
    this.v2.addTriangle( this );
    this.v3.addTriangle( this );
    // mVolume = new Vector3D(
    //   ( v1.y * v2.z + v3.y * v1.z + v2.y * v3.z - v1.y * v3.z - v3.y * v2.z - v2.y * v1.z ),
    //   ( v1.x * v3.z + v3.x * v2.z + v2.x * v1.z - v1.x * v2.z - v3.x * v1.z - v2.x * v3.z ),
    //   ( v1.x * v2.y + v3.x * v1.y + v2.x * v3.y - v1.x * v3.y - v3.x * v2.y - v2.x * v1.y ) );
    // mVolumeOffset = v1.x * (v2.y*v3.z - v2.z*v3.y) + v1.y * (v2.z*v3.x - v2.x*v3.z) + v1.z * (v2.x*v3.y - v2.y*v3.z);

  }

  // void dump( )
  // {
  //   TDLog.v( "CW Tri " + mCnt + " " + mType + " V " + v1.mCnt + " " + v2.mCnt + " " + v3.mCnt 
  //               + " S " + s1.mCnt + " " + s2.mCnt + " " + s3.mCnt 
  //               // + " U " + un.x + " " + un.y + " " + un.z
  //   );
  // }
  
  public void writeTriangle( PrintWriter out )
  {
    out.format(Locale.US, "T %d %d %d %d %d %d %d %d %.3f %.3f %.3f\n",
                mCnt, mType, v1.mCnt, v2.mCnt, v3.mCnt, s1.mCnt, s2.mCnt, s3.mCnt, un.x, un.y, un.z );
  }

  public void serialize( DataOutputStream dos ) throws IOException
  {
    dos.write('T');
    dos.writeInt( mCnt );
    dos.writeInt( mType );
    dos.writeInt( v1.mCnt );
    dos.writeInt( v2.mCnt );
    dos.writeInt( v3.mCnt );
    dos.writeInt( s1.mCnt );
    dos.writeInt( s2.mCnt );
    dos.writeInt( s3.mCnt );
    dos.writeDouble( un.x );
    dos.writeDouble( un.y );
    dos.writeDouble( un.z );
  }

  /* FIXME
  static public CWTriangle deserialize( DataInputStream dos, List<CWPoint> pts, List<CWSide> sides ) throws IOException
  {
    char ch = dis.read();
    int cnt = dis.readInt( );
    int typ = dis.readInt( );
    CWPoint p1 = getPoint( dis.readInt(), pts );
    CWPoint p2 = getPoint( dis.readInt(), pts );
    CWPoint p3 = getPoint( dis.readInt(), pts );
    CWSide s1 = getPoint( dis.readInt(), sides );
    CWSide s2 = getPoint( dis.readInt(), sides );
    CWSide s3 = getPoint( dis.readInt(), sides );
    double x = dos.readDouble( );
    double y = dos.readDouble( );
    double z = dos.readDouble( );
    return new CWTriangle( cnt, p1, p2, p3, s1, s2, s3 );
  }

  static private CWPoint getPoint( int tag, List<CWPoint> pts )
  {
    for ( CWPoint pt : pts ) if ( pt.mCnt == tag ) return pt;
    return null;
  }

  static private CWSide getSide( int tag, List<CWSide> sds )
  {
    for ( CWSide sd : sds ) if ( sd.mCnt == tag ) return sd;
    return null;
  }
  */

  /* if vector P is "outside" the triangle-plane (ie on the other side than the hull)
   * set mOutside to true.
   * P is outside if the volume of the tetrahedron of P and the triangle is negative
   * because the normal U of the triangle points "inside" the convex hull
   */
  public boolean setOutside( Vector3D p )
  {
    mOutside = ( volume(p) < 0.0f );
    return mOutside;
  }
  
  public boolean isOutside() { return mOutside; }
  
  /* returns true is S is a side of the triangle
   */
  public boolean contains( CWSide s ) { return s == s1 || s == s2 || s == s3; } 
  
  /*                 |
               ,.--''+ beta3: v1 + b3 u3   alpha3, s2
      s2   v3 '      |
      u3  .^^.s1     |
         /    \ u1   |
       v1 ----> v2 --+ beta2: v1 + b2 u2   alpha2, s3
           u2     `-.|
           s3        + beta1: v2 + b1 u1   alpha1, s1
   */
  public boolean intersectionPoints( Vector3D v, Vector3D n, CWLinePoint lp1, CWLinePoint lp2 )
  {
    // round beta to three decimal digits
    double b2 = ((int)(beta2( v, n )*1000.1))/1000.0;
    double b3 = ((int)(beta3( v, n )*1000.1))/1000.0;
    double b1 = ((int)(beta1( v, n )*1000.1))/1000.0;
    double a1, a2, a3;
    if ( b1 >= 0 && b1 <= 1 ) {
      a1 = alpha1( v, n );
      lp1.copy( a1, s1, this, v.sum( n.scaledBy(a1) ) );
      if ( b2 >= 0 && b2 <= 1 ) {
        a2 = alpha2( v, n );
        lp2.copy( a2, s3, this, v.sum( n.scaledBy(a2) ) );
        // TDLog.v( "CW Tri " + mCnt + " b1 " + b1 + " b2 " + b2 );
        return true;
      } else if ( b3 >= 0 && b3 <= 1 ) {
        a3 = alpha3( v, n );
        lp2.copy( a3, s2, this, v.sum( n.scaledBy(a3) ) );
        // TDLog.v( "CW Tri " + mCnt + " b1 " + b1 + " b3 " + b3 );
        return true;
      }
    } else if ( b2 >= 0 && b2 <= 1 ) {
      a2 = alpha2( v, n );
      lp1.copy( a2, s3, this, v.sum( n.scaledBy(a2) ) );
      if ( b3 >= 0 && b3 <= 1 ) {
        a3 = alpha3( v, n );
        lp2.copy( a3, s2, this, v.sum( n.scaledBy(a3) ) );
        // TDLog.v( "CW Tri " + mCnt + " b2 " + b2 + " b3 " + b3 );
        return true;
      }
    }
    return false;
  }
  
  // ============================================================================================

}
