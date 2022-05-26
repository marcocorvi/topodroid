/** @file Cave3DXSection.java
 *
 * @author marco corvi
 * @date mar 2021
 *
 * @brief 3D: XSection, roughly in a plane
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;

// import java.io.StringWriter;
// import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Cave3DXSection
{
  private XSplay[] splays; // splay shots of the x-section
  Cave3DStation station;   // xsection at a station - otherwise it is null
  Vector3D center;         // center of the X_Section (coincides with the station if the xsection has a station
  Vector3D normal;         // normal to the x-section plane
  Vector3D ref;            // reference from which to measure the angles

  /** @return the number of splay shots
   */
  public int size() { return splays.length; }

  /** @return the xsection name
   */
  public String name() { return (station != null)? station.getFullName() : "none"; }
   
  /** @return  the k-th splay point (3D world frame)
   * @param k       index - must be between 0 and splays.length
   * @param reverse whether to count splays from the end
   */
  Vector3D point( int k, boolean reverse ) { return  (k < 0 || k >= splays.length )? null : reverse ? reversePoint(k) : directPoint(k); }

  /** @return  the forward k-th splay point (3D world frame)
   * @param k       index - must be between 0 and splays.length
   */
  private Vector3D directPoint( int k ) { return center.sum( splays[ k ] ); }

  /** @return  the backward k-th splay point (3D world frame)
   * @param k       index - must be between 0 and splays.length
   */
  private Vector3D reversePoint( int k ) { return center.sum( splays[ splays.length-1-k ] ); }

  /** @return  the k-th splay angle
   * @param k        splay index
   * @param reversed whether to count splays in reversed order (and angles complemented to 2PI)
   */
  double angle( int k, boolean reversed ) { return reversed ? reverseAngle(k) : directAngle(k); }

  /** @return  the forward k-th splay angle
   * @param k        splay index
   */
  private double directAngle( int k ) { return splays[ k ].angle; }

  /** @return return the complement to 2*PI of the angle of the splays in reversed order
   * @param k        splay index
   */
  private double reverseAngle( int k ) { return Math.PI*2 - splays[ splays.length-1-k ].angle; }

  /** cstr
   * @param st   xsection station
   * @param c    center
   * @param n    normal to the xsection plane
   * @param r    angle reference direction
   * @param nxs  number of xsplays (to expect)
   */
  private Cave3DXSection( Cave3DStation st, Vector3D c, Vector3D n, Vector3D r, int nxs )
  {
    station = st;
    center  = c;
    normal  = n;
    ref     = r;
    splays  = new XSplay[ nxs ];
  }

  /** cstr
   * @param st    xsection station
   * @param c     center station
   * @param r     angle reference direction
   * @param shots splay shots
   */
  public Cave3DXSection( Cave3DStation st, Vector3D c, Vector3D r, List< Vector3D > shots )
  {
    station = st;
    center  = c;
    ref     = r;
    ArrayList< Vector3D > points = new ArrayList<>();
    for ( Vector3D shot : shots ) points.add( shot.difference( center ) );
    recomputeNormal( points );
    orderPoints( normal, r, points );
  }


  //void dump()
  //{
  //  TDLog.v("XS " + size() + " C " + center.x + " " + center.y + " " + center.z + " N " + normal.x + " " + normal.y + " " + normal.z );
  //  for ( int k=0; k < splays.length; ++k ) splays[k].dump();
  //}

  // --------------------------------------------------------------------
  /** compute the normal, from a set of 3D points
   * @param points   3D points
   */
  private void recomputeNormal( List< Vector3D > points )
  {
    double[] A = new double[9];
    for ( int k=0; k<9; ++k ) A[k]=0;
    for ( Vector3D v : points ) {
      A[0] += v.x * v.x;   A[1] += v.x * v.y;   A[2] += v.x * v.z;
      A[3] += v.y * v.x;   A[4] += v.y * v.y;   A[5] += v.y * v.z;
      A[6] += v.z * v.x;   A[7] += v.z * v.y;   A[8] += v.z * v.z;
    }
    // normal = leastEigenvector( A );

    // compute the smallest eigenvalue of A (A is pos. semidef. therefore eigenval >= 0)
    // L^3 - Tr(A) L^2 + ( Axx Ayy + Ayy Azz + Azz Axx - Axy^2 - Axz^2 - Azy^2 ) L + det(A)
    // 
    double b2 = - ( A[0] + A[4] + A[8] ); // trace
    double b1 = ( A[0]*A[4] + A[0]*A[8] + A[4]*A[8] - A[5]*A[7] - A[1]*A[3] - A[2]*A[6] );
    double b0 =  - A[0]*A[4]*A[8] - A[2]*A[3]*A[7] - A[1]*A[5]*A[6] + A[0]*A[5]*A[7] + A[4]*A[2]*A[6] + A[8]*A[1]*A[3]; // determinant
    // double b00 = A[0]*( A[4]*A[8] - A[5]*A[7] ) - A[1]*( A[3]*A[8] - A[6]*A[5] ) + A[2]*( A[3]*A[7] - A[6]*A[4] ); // determinant
    // find first positive zero of   f(L) = L^3 + b2 L^2 + b1 L + b0 = 0;
    //   f'(L) = 3 L^2 + 2 b2 L + b1 = 0
    // for L = ( - b2 +/- sqrt( b2*b2 - 3 b1 ) )/3
    // they are both positive if b2 < 0 and b1 > 0, 3 b1 < b2*b2
    // b2^2  = A[0]*A[0] + A[4]*A[4] + A[8]*A[8] + 2*A0*A4 + 2*A0*A8 + 2*A4*A8
    // -3 b1 =                                   - 3*A0*A4 - 3*A0*A8 - 3*A4*A8 + 3 A5^2 + 3 A1^2 + 3 A2^2
    // the sum is positive
    // next b1 > 0 because of the way the matrix is built

    // roots of f'(L) = 0
    double det1 = b2 * b2 - 3 * b1;
    // assert( det1 >= 0 );
    double L2 = ( - b2 + Math.sqrt(det1) )/3;
    double L1 = 0;
    // double F1 = L1 * L1 * L1 + b2 * L1 * L1 + b1 * L1 + b0;
    // double F2 = L2 * L2 * L2 + b2 * L2 * L2 + b1 * L2 + b0;
    // assert( F1 <= 0 );
    // assert( F2 >= 0 );
    double L = L1;
    do {
      L= (L1 + L2)/2;
      double F = L * L * L + b2 * L * L + b1 * L + b0;
      if ( F > 0 ) {
        // F2 = F;
        L2 = L;
      } else if ( F < 0 ) {
        // F1 = F;
        L1 = L;
      } else {
        break;
      }
    } while ( L2 - L1 > 1.0E-3 ); // was 1.0E-7

    // TDLog.v("f(L) = L^3 + " + b2 + " L^2 + " + b1 + " L + " + b0 );

    /*
    double L = 0;
    double f0 = L * L * L + b2 * L * L + b1 * L + b0;
    int cnt = 0;
    double delta = 0.1 * Math.sqrt(A[0] + A[4] + A[8] ) / points.size();
    do {
      double L1 = L + delta;
      double f1 = L1 * L1 * L1 + b2 * L1 * L1 + b1 * L1 + b0;
      if ( f1 >= f0 && f1 < 0 ) {
        L = L1;
        f0 = f1;
      } else {
        delta = delta/2;
      }

      // System.out.println("L " + L + " f0 " + f0 + " f1 " + f1 + " delta " + delta );
      // if ( ++cnt > 20 ) break;
      // f0 = L * L * L + b2 * L * L + b1 * L + b0;
    } while ( Math.abs( f0 ) > 0.0000001 );
    */

    // double a0 = A[0] - L;
    double a4 = A[4] - L;
    double a8 = A[8] - L;
    double nx = 1.0;
    //  a8   -a5  * ny = -a3 nx / det
    // -a7    a4    nz   -a6 nx / det
    double det = a4 * a8 - A[5] * A[7];
    double ny = - ( a8 * A[3] - A[5] * A[6] ) / det;
    double nz = - ( a4 * A[6] - A[7] * A[3] ) / det;
    double nlen = Math.sqrt( nx*nx + ny*ny + nz*nz );
    nx /= nlen;
    ny /= nlen;
    nz /= nlen; 
    // check eigenvector and eigenvalue
    // double x = A[0] * nx + A[1] * ny + A[2] * nz - L * nx;
    // double y = A[3] * nx + A[4] * ny + A[5] * nz - L * ny;
    // double z = A[6] * nx + A[7] * ny + A[8] * nz - L * nz;
    // TDLog.v("check eigenvalue " + L + ": " + x + " " + y + " " + z );
    // System.out.println("check eigenvalue " + L + " N: " + nx + " " + ny + " " + nz );

    // now reset the normal to the computed value
    normal = new Vector3D( nx, ny, nz );
    // normal.normalized();
  }

  /** compute the renormalized 3D vector projection of the 3D point P on the x-section plane
   * @param v0  normal
   * @param p   3D point
   * @return  renormalized 3D vector projection 
   */
  private Vector3D projection( Vector3D v0, Vector3D p )
  {
    // Vector3D ret = p.difference( normal.scaledBy( normal.dotProduct(p) ) );
    double pn = v0.dotProduct(p);
    Vector3D ret = new Vector3D( p.x - pn*v0.x, p.y - pn*v0.y, p.z - pn*v0.z );
    ret.normalized();
    return ret;
  }

  /** @return the angle between two 3D vectors, in [0, 2 PI)
   * @param v0   normal
   * @param v1   zero-refrence
   * @param v2   test vector
   */
  private double computeAngle( Vector3D v0, Vector3D v1, Vector3D v2 ) 
  {
    double c = v1.dotProduct( v2 );
    double s = v1.crossProduct( v2 ).dotProduct( v0 );
    double a = Math.atan2( s, c );
    if ( a < 0 ) a += 2 * Math.PI;
    return a;
  }

  /** reorder the points in a list
   * @param n   normal to the plane where the order is done
   * @param r   reference direction
   * @param points 3D points relative to the center
   */
  private void orderPoints( Vector3D n, Vector3D r, List< Vector3D > points )
  {
    Vector3D w = projection( n, r );
    splays = new XSplay[ points.size() ];
    for ( int k=0; k<points.size(); ++k ) {
      Vector3D pt = points.get(k);
      splays[k] = new XSplay( pt, computeAngle( n, w, projection(n, pt) ) );
    }
    Arrays.sort( splays, 0, splays.length );
    // now move at the first position the closest point to the reference 
    if ( Math.PI*2 - splays[splays.length-1].angle < splays[0].angle ) { 
      XSplay temp = splays[splays.length-1];
      for ( int k=splays.length-1; k>0; --k ) splays[k] = splays[k-1];
      splays[0] = temp;
      splays[0].angle -= 2 * Math.PI;
    }
  }  

  /** serialize the 3D XSection
   * @param dos   output stream
   */
  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.write('x');
    dos.writeInt( station.mId );
    dos.writeDouble( center.x );
    dos.writeDouble( center.y );
    dos.writeDouble( center.z );
    dos.writeDouble( normal.x );
    dos.writeDouble( normal.y );
    dos.writeDouble( normal.z );
    dos.writeDouble( ref.x );
    dos.writeDouble( ref.y );
    dos.writeDouble( ref.z );
    dos.writeInt( splays.length );
    for ( XSplay splay : splays ) splay.serialize(dos );
  }

  /** deserialize a 3D XSection
   * @param dis      input stream
   * @param version  serialization version
   * @param stations list of 3D stations
   * @return the deserialized 3D XSection
   */
  static Cave3DXSection deserialize( DataInputStream dis, int version, List<Cave3DStation> stations ) throws IOException
  {
    int ch = dis.read(); // 'x'
    int id = dis.readInt();
    Cave3DStation st0 = null;
    for ( Cave3DStation st : stations ) if ( st.mId == id ) { st0 = st; break; }
    Vector3D c = new Vector3D( dis.readDouble(), dis.readDouble(), dis.readDouble() );
    Vector3D n = new Vector3D( dis.readDouble(), dis.readDouble(), dis.readDouble() );
    Vector3D r = new Vector3D( dis.readDouble(), dis.readDouble(), dis.readDouble() );
    int nxs = dis.readInt();
    Cave3DXSection ret = new Cave3DXSection( st0, c, n, r, nxs );
    for ( int k=0; k<nxs; ++k ) ret.splays[k] = XSplay.deserialize( dis, version );
    return ret;
  }


  /*
  public static void main( String[] args )
  {
    ArrayList<Vector3D> data = new ArrayList<>();

    for ( int k=0; k<10; ++k ) {
      double a = Math.PI * 2 * Math.random();
      data.add( new Vector3D( Math.cos(a) + 0.1*(Math.random() -0.5),
                              Math.sin(a) + 0.1*(Math.random() -0.5),
                                            0.1*(Math.random() -0.5) ) );
    }
    Cave3DXSection xsection = new Cave3DXSection( 0, 0, 0, data );
  }
  */
}
