/** @file CWIntersection.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief CW triangles intersection
 *        a segment intersection if two triangles.
 *        its endpoints can belong to sides of one of the two triangles (type 2)
 *        or to one side for each triangle (type 1)
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

public class CWIntersection
{
  static int cnt = 0;
  static void resetCounter() { cnt = 0; }

  int mCnt;
  int mType;         // 1 one endpoint fron each triangle, 2 both endpoints from one triangle
  CWTriangle mTriA;    
  CWTriangle mTriB;
  Vector3D mV;       // base-point of intersection line
  Vector3D mN;       // direction of intersection line
  CWLinePoint mV1;
  CWLinePoint mV2;
  int[] mSign;      // signature
  private CWPoint mPA1;      // points for CW of triangle A (only point1, point2 is point1 of next)
  private CWPoint mPB1;      // points for CW of triangle B
  private CWIntersection mNext;
  
  public CWIntersection( int type, CWTriangle ta, CWTriangle tb, Vector3D v, Vector3D n )
  {
    mCnt = cnt++;
    mType = type;
    mTriA = ta;
    mTriB = tb;
    mV = v; 
    mN = n;
    mV1 = null;
    mV2 = null;
    mSign = null;
    mPA1 = null;
    mPB1 = null;
    mNext = null;
  }

  // @param index  index of the CW either 1 or 2
  CWPoint point1( int index )
  {
    return ( index == 1 )? pointA1( ) : pointB1( );
  }

  CWPoint point2( int index )
  {
    return mNext.point1( index );
  }

  private CWPoint pointA1( ) 
  {
    if ( mPA1 == null ) mPA1 = new CWPoint( mV1.x, mV1.y, mV1.z );
    return mPA1;
  }

  private CWPoint pointB1( )
  {
    if ( mPB1 == null ) mPB1 = new CWPoint( mV1.x, mV1.y, mV1.z );
    return mPB1;
  }

  void reverse()
  {
    CWLinePoint v = mV1; mV1 = mV2; mV2 = v;
    mV1.mAlpha *= -1;
    mV2.mAlpha *= -1;
    mN.reverse();
    CWTriangle t = mTriA; mTriA = mTriB; mTriB = t;
    mType += 2;
    int z = mSign[0]; mSign[0] = mSign[5]; mSign[5] = z;
        z = mSign[1]; mSign[1] = mSign[4]; mSign[4] = z;
        z = mSign[2]; mSign[2] = mSign[3]; mSign[3] = z;
  }

  void setNext( CWIntersection next ) { mNext = next; }

  CWIntersection next() { return mNext; }

  void makeSignature()
  {
    CWTriangle t1 = mV1.mSide.otherTriangle( mV1.mTri );
    CWTriangle t2 = mV2.mSide.otherTriangle( mV2.mTri );
    mSign = new int[6];
    mSign[0] = (t1 == null )? -1 : t1.mCnt;
    mSign[1] = mV1.mSide.mCnt;
    mSign[2] = mV1.mTri.mCnt;
    mSign[3] = mV2.mTri.mCnt;
    mSign[4] = mV2.mSide.mCnt;
    mSign[5] = (t2 == null)? -1 : t2.mCnt;
  }

  /*
   * @return 1 (direct follow) -1 (reverse follow) 0 (not follow)
   */
  int followSignature( CWIntersection ii ) 
  {
    if ( mSign[0] == ii.mSign[3] && mSign[1] == ii.mSign[4] && mSign[2] == ii.mSign[5] ) return 1;
    if ( mSign[5] == ii.mSign[3] && mSign[4] == ii.mSign[4] && mSign[3] == ii.mSign[5] ) return -1;
    return 0;
  }
  
  boolean followSignatureDirect( CWIntersection ii )
  {
    return ( mSign[0] == ii.mSign[3] && mSign[1] == ii.mSign[4] && mSign[2] == ii.mSign[5] );
  }
  
  boolean followSignatureInverse( CWIntersection ii )
  {
    return ( mSign[5] == ii.mSign[3] && mSign[4] == ii.mSign[4] && mSign[3] == ii.mSign[5] );
  }

  // void dump( int j )
  // {
  //   CWTriangle t1 = mV1.mSide.otherTriangle( mV1.mTri );
  //   CWTriangle t2 = mV2.mSide.otherTriangle( mV2.mTri );
  //     
  //   if ( j == 1 ) { // first 1 then 2
  //     TDLog.v( "CW " + mCnt + " " + mType + ": " + mTriA.mCnt + "-" + mTriB.mCnt 
  //       + " " + t1.mCnt + "/" + mV1.mSide.mCnt + "/" + mV1.mTri.mCnt
  //       + " " + mV2.mTri.mCnt + "/" + mV2.mSide.mCnt + "/" + t2.mCnt
  //     );
  //   } else { // first 2 then 1
  //     TDLog.v( "CW " + mCnt + " " + mType + ": " + mTriA.mCnt + "-" + mTriB.mCnt 
  //         + " " + mV2.mTri.mCnt + "/" + mV2.mSide.mCnt + "/" + t2.mCnt
  //         + " " + mV1.mTri.mCnt + "/" + mV1.mSide.mCnt + "/" + t1.mCnt
  //     );
  //   }
  //     // if ( mV1 != null ) mV1.dump( out );
  //     // if ( mV2 != null ) mV2.dump( out );
  // }

  void dump()
  {
    CWTriangle t1 = mV1.mSide.otherTriangle( mV1.mTri );
    CWTriangle t2 = mV2.mSide.otherTriangle( mV2.mTri );
    TDLog.v( "CW: I " + mCnt + " [" + mType + ": tri " + mTriA.mCnt + " " + mTriB.mCnt
                   + " ] " + t1.mCnt + "/" + mV1.mSide.mCnt + "/" + mV1.mTri.mCnt
                   + " -- " + mV2.mTri.mCnt + "/" + mV2.mSide.mCnt + "/" + t2.mCnt 
                   + " next " + ( (mNext != null)? mNext.mCnt : -1 ) );
  }
  
  void writeIntersection( PrintWriter out )
  {
    out.format(Locale.US, "I %d %d %d %d %.3f %.3f %.3f %.3f %.3f %.3f\n",
                mCnt, mType, mTriA.mCnt, mTriB.mCnt, mV.x, mV.y, mV.z, mN.x, mN.y, mN.z );
    mV1.writeLinePoint( out );
    mV2.writeLinePoint( out );
  }

  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.write('I');
    dos.writeInt( mCnt );
    dos.writeInt( mType );
    dos.writeInt( mTriA.mCnt );
    dos.writeInt( mTriB.mCnt );
    dos.writeDouble( mV.x );
    dos.writeDouble( mV.y );
    dos.writeDouble( mV.z );
    dos.writeDouble( mN.x );
    dos.writeDouble( mN.y );
    dos.writeDouble( mN.z );
    mV1.serialize( dos );
    mV2.serialize( dos );
  }

  /* FIXME
  static CWIntersection deserialize( DataInputStream dis ) throws IOException
  {
    TODO
  }
  */

}
