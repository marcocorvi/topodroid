/** @file CWLinePoint.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief CW triangles intersection line-point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.cw;

import com.topodroid.Cave3X.Vector3D;
import com.topodroid.utils.TDLog;

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
  
  public CWLinePoint()
  {
    super(0,0,0);
    mAlpha = 0;
    mSide = null;
    mTri  = null;
  }
  
  public CWLinePoint( double a, CWSide s, CWTriangle t, double x, double y, double z )
  {
    super( x,y,z);
    mAlpha = a;
    mSide = s;
    mTri  = t;
  }
  
  public CWLinePoint( double a, CWSide s, CWTriangle t, Vector3D v )
  {
    super(v.x, v.y, v.z );
    mAlpha = a;
    mSide = s;
    mTri  = t;
  }
  
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
  //   TDLog.v("CW LP " + mTri.mCnt + "/" + mSide.mCnt + "/" + t.mCnt );
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
  static CWLinePoint deserialize( DataInputStream dis ) thrpws IOException
  {
    TODO
  }
  */
}
