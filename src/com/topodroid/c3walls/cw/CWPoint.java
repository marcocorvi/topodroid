/** @file CWPoint.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief CW point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.cw;

import com.topodroid.TDX.Vector3D;

import java.util.ArrayList;
import java.util.Locale;

import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CWPoint extends Vector3D
{
  private static int cnt = 0;
  static void resetCounter() { cnt = 0; }

  int mCnt;
  ArrayList<CWTriangle> mTriangle;

  public CWPoint( double x, double y, double z )
  {
    super( x, y, z );
    mCnt = cnt++;
    mTriangle = new ArrayList<CWTriangle>();
  }

  public CWPoint( int tag, double x, double y, double z )
  {
    super( x, y, z );
    mCnt = tag;
    if ( cnt <= tag ) cnt = tag+1;
    mTriangle = new ArrayList<CWTriangle>();
  }

  public void addTriangle( CWTriangle t ) 
  {
    if ( t == null || mTriangle.contains(t) ) return;
    mTriangle.add( t );
  }
  
  public void removeTriangle( CWTriangle t )
  {
    if ( t == null ) return;
    mTriangle.remove( t );
  }
  
  // the triangles are ordered rightward around the outward direction
  public void orderTriangles()
  {
    int k = 0;
    CWTriangle t0 = mTriangle.get(0);
    CWSide s0 = t0.leftSideOf( this );
    CWSide s1 = t0.rightSideOf( this );
    ++k;
    while ( k<mTriangle.size() ) {
      int j = k;
      for (; j < mTriangle.size(); ++j) {
        if (mTriangle.get(j).contains(s1)) break;
      }
      if (j == mTriangle.size()) return; // return false;
      // assert ( j < mTriangle.size() );
      CWTriangle tj = mTriangle.get(j);
      if (j > k) {
        CWTriangle tk = mTriangle.get(k);
        mTriangle.set(j, tk);
        mTriangle.set(k, tj);
      }
      s1 = tj.rightSideOf(this);
      ++k;
    }
    // return true;
  }
  
  public CWTriangle rightTriangleOf( CWSide s )
  {
    if ( ! s.contains(this) ) return null;
    for ( CWTriangle t : mTriangle ) if ( s == t.leftSideOf(this) ) return t;
    return null;
  }
  
  public CWTriangle leftTriangleOf( CWSide s )
  {
    if ( ! s.contains(this) ) return null;
    for ( CWTriangle t : mTriangle ) if ( s == t.rightSideOf(this) ) return t;
    return null;
  }

  // check if this point triangles are all marked "outside"
  public boolean areAllTrianglesOutside()
  {
    for ( CWTriangle t : mTriangle ) {
      if ( ! t.isOutside() ) return false;
    }
    return true;
  }

  //void dump( )
  //{
  //  StringBuilder sb = new StringBuilder();
  //  for ( CWTriangle t : mTriangle ) sb.append( "-" + t.mCnt );
  //  TDLog.v( "CW Point " + mCnt + " T" + sb.toString() + " " + x + " " + y + " " + z );
  //}
  
  public void writePoint( PrintWriter out )
  {
    int size = ( mTriangle != null )? mTriangle.size() : -1;
    out.format(Locale.US, "V %d %d %.3f %.3f %.3f\n", mCnt, size, x, y, z );
  }

  public void serialize( DataOutputStream dos ) throws IOException
  {
    int size = ( mTriangle != null )? mTriangle.size() : -1;
    dos.write('V');
    dos.writeInt( mCnt );
    dos.writeInt( size );
    dos.writeDouble( x );
    dos.writeDouble( y );
    dos.writeDouble( z );
  }

  /* FIXME
  static public CWPoint deserialize( DataInputStream dis ) throws IOException
  {
    char ch = dis.read();
    int cnt  = dis.readInt( );
    int size = dis.readInt( );
    double x = dis.readDouble( );
    double y = dis.readDouble( );
    double z = dis.readDouble( );
    return new CWPoint( cnt, x, y, z ); // the triangles must still be added to the point
  }
  */


}

