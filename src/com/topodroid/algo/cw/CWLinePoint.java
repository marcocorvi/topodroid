/** @file CWLinePoint.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief CW triangles intersection line-point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.algo.cw;

import com.topodroid.math.Vector3D;
// import com.topodroid.util.TDLog;

import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Locale;

public class CWLinePoint extends Vector3D 
{
  double mAlpha;  // line abscissa
  CWSide mSide;    // side to which the point belongs
  CWTriangle mTri; // triangle to which the side belongs
  
  /** default cstr
   */
  public CWLinePoint()
  {
    super(0,0,0);
    mAlpha = 0;
    mSide = null;
    mTri  = null;
  }
  
  /** cstr
   * @param a     abscissa
   * @param s     side of the point
   * @param t     triangle of the point
   * @param x     X coordinate
   * @param y     Y coordinate
   * @param z     Z coordinate
   */
  public CWLinePoint( double a, CWSide s, CWTriangle t, double x, double y, double z )
  {
    super( x,y,z);
    mAlpha = a;
    mSide = s;
    mTri  = t;
  }
  
  /** cstr
   * @param a     abscissa
   * @param s     side of the point
   * @param t     triangle of the point
   * @param v     3D coordinates
   */
  public CWLinePoint( double a, CWSide s, CWTriangle t, Vector3D v )
  {
    super(v.x, v.y, v.z );
    mAlpha = a;
    mSide = s;
    mTri  = t;
  }
  
  /** value assignment into this point
   * @param a     abscissa
   * @param s     side of the point
   * @param t     triangle of the point
   * @param v     3D coordinates
   */
  public void copy( double a, CWSide s, CWTriangle t, Vector3D v )
  {
    mAlpha = a;
    mSide = s;
    mTri  = t;
    copy( v );
  }
  
  // void dump( PrintStream out )
  // {
  //   CWTriangle t = mSide.otherTriangle( mTri );
  //   // TDLog.v("CW-Hull LP " + mTri.mCnt + "/" + mSide.mCnt + "/" + t.mCnt );
  // }

  public void writeLinePoint( PrintWriter out )
  {
     out.format(Locale.US, "L %d %d %.3f %.3f %.3f %.3f\n", mTri.mCnt, mSide.mCnt, mAlpha, x, y, z );
  }

  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.write('L');
    dos.writeInt( mTri.mCnt );
    dos.writeInt( mSide.mCnt );
    dos.writeDouble( mAlpha );
    dos.writeDouble( x );
    dos.writeDouble( y );
    dos.writeDouble( z );
  }

  /* FIXME
  static CWLinePoint deserialize( DataInputStream dis ) throws IOException
  {
    TODO
  }
  */
}
