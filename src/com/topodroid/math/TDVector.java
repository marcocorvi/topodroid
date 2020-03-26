/* @file TDVector.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid 3 vector
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 */
package com.topodroid.math;

import com.topodroid.utils.TDMath;

import java.lang.Math;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;

// import android.util.Log;

public class TDVector
{
  public float x,y,z;

  public static final TDVector zero = new TDVector(0.0f, 0.0f, 0.0f);

  // defaulkt cstr: zero vector
  public TDVector()
  {
    x = 0.0f;
    y = 0.0f;
    z = 0.0f;
  }

  // cstr vector (x0, y0., z0)
  public TDVector( float x0, float y0, float z0 )
  {
    x = x0;
    y = y0;
    z = z0;
  }

  // cstr a unit vector from bearing and clino (as used by the calibration class)
  // b   bearing [radians]
  // c   clino [radians]
  public TDVector( float b, float c )
  {
    float h = (float)Math.cos( c );
    x = h * (float)Math.cos( b );
    y = h * (float)Math.sin( b );
    z = (float)Math.sin( c );
  }

  // copy cstr
  public TDVector( TDVector a )
  {
    x = a.x;
    y = a.y;
    z = a.z;
  }

  // this cross (1,0,0)
  public TDVector crossX() { return new TDVector( 0, z, -y ); }

  public float maxAbsValue()
  {
    float mx = TDMath.abs(x);
    float my = TDMath.abs(y);
    float mz = TDMath.abs(z);
    return (float)( ( mx > my )? ( ( mx > mz )? mx : mz )
                               : ( ( my > mz )? my : mz ) );
  }

  // get unit vector 
  public TDVector getUnitVector( )
  {
    TDVector ret = new TDVector( x, y, z );
    ret.normalize();
    return ret;
  }

  public float Length()
  {
    return (float)Math.sqrt( x*x + y*y + z*z );
  }

  public float LengthSquared()
  {
    return ( x*x + y*y + z*z );
  }

  public float Abs( ) { return Length(); }

  public TDVector TurnX( float s, float c )
  {
    return new TDVector( x, c*y - s*z, c*z + s*y );
  }

  // public TDVector TurnY( float s, float c ) { return new TDVector( c*x + s*z, y, c*z - s*x ); }

  // public TDVector TurnZ( float s, float c ) { return new TDVector( c*x - s*y, c*y + s*x, z ); }

  public void normalize( )
  {
    float len = Length();
    if ( len > 0.0f ) {
      float n = 1.0f / len;
      x *= n;
      y *= n;
      z *= n;
    }
  }

  public void reverse()
  {
    x = -x;
    y = -y;
    z = -z;
  }

  public float MaxDiff( TDVector b )
  {
    float dx = TDMath.abs( x - b.x );
    float dy = TDMath.abs( y - b.y );
    float dz = TDMath.abs( z - b.z );
    if ( dx < dy ) { dx = dy; }
    if ( dx < dz ) { dx = dz; }
    return dx;
  }

  public void copy( TDVector b ) // copy assignment
  {
    x = b.x;
    y = b.y;
    z = b.z;
  }

  // public void set( TDVector a )
  // {
  //   x = a.x;
  //   y = a.y;
  //   z = a.z;
  // }

  public void plusEqual( TDVector b ) 
  {
    x += b.x;
    y += b.y;
    z += b.z;
  }

  public void minusEqual( TDVector b ) 
  {
    x -= b.x;
    y -= b.y;
    z -= b.z;
  }

  public void timesEqual( float f )
  {
    x *= f;
    y *= f;
    z *= f;
  }

  public TDVector plus( TDVector b ) { return new TDVector( x+b.x, y+b.y, z+b.z ); }

  public TDVector minus( TDVector b ) { return new TDVector( x-b.x, y-b.y, z-b.z ); }

  // MULTIPLICATION: this * b
  public TDVector times( float b ) { return new TDVector(x*b, y*b, z*b ); }

  // DOT PRODUCT: this * b
  public float dot( TDVector b ) { return x*b.x + y*b.y + z*b.z; }

  // dot-product of two Vectors
  public static float dot_product( TDVector p1, TDVector p2 )
  {
    return p1.x * p2.x + p1.y * p2.y + p1.z * p2.z;
  }

  // CROSS PRODUCT: this % b
  public TDVector cross( TDVector b )
  {
    return new TDVector( y*b.z - z*b.y, z*b.x - x*b.z, x*b.y - y*b.x );
  }

  // cross-product of two Vectors
  public static TDVector cross_product( TDVector p1, TDVector p2 ) // PRIVATE
  {
    return new TDVector( p1.y * p2.z - p1.z * p2.y,
                       p1.z * p2.x - p1.x * p2.z,
    		       p1.x * p2.y - p1.y * p2.x );
  }

  // triple-product of three Vectors
  public static double triple_product( TDVector p1, TDVector p2, TDVector p3 )
  {
    return dot_product( cross_product( p1, p2 ), p3 );
  }

  // arc-distance = arccos of the dot-product ( range in [0, PI] )
  // p1 and p2 unit vectors
  public static double arc_distance( TDVector p1, TDVector p2 )
  {
    float ca1 = dot_product( p1, p2 );
    return TDMath.acos( ca1 );
  }

  // cosine of the spherical angle
  // static double spherical_angle( TDVector p1, TDVector p2, TDVector p3 )
  // {
  //   TDVector p12 = cross_product( p1, p2 );
  //   TDVector p13 = cross_product( p1, p3 );
  //   p12.normalized();
  //   p13.normalized();
  //   return dot_product( p12, p13 );
  // }

  /** projection of a vector in the plane orthogonal to this vector
   * @param b vector to project
   * @return projected vector
   *
   * B - (B*T)/(T*T) T
   */
  public TDVector orthogonalNormal( TDVector b )
  {
    float f = ( this.dot( b ) )/(x*x + y*y + z*z );
    return new TDVector( b.x - f*x, b.y - f*y, b.z - f*z );
  }

  // if this is normalized can use this method
  public TDVector orthogonal( TDVector b ) // PRIVATE
  {
    float f = this.dot( b );
    return new TDVector( b.x - f*x, b.y - f*y, b.z - f*z );
  }

  // euclidean distance from another point
  public float distance( TDVector p )
  {
    float a = x - p.x;
    float b = y - p.y;
    float c = z - p.z;
    return (float)Math.sqrt( a*a + b*b + c*c );
  }

  // as 3D point (X,Y,Z) are east, south, vert(down) 
  // Y and Z are reversed in Therion
  public void toTherion( PrintWriter pw )
  {
    pw.format(Locale.US, "  %.2f %.2f %.2f\n", x, -y, -z );
  }

  /* The plane of a path is: a0*x + b0*y + c0*z = 1
   * There is an unresolved ambiguity: the normal to the plane could be
   * reversed and the plane still be the same.
   * Need to require that the points are traversed righthand wise,
   * going around the normal.
   */
  // static TDVector computeVectorsNormal( ArrayList<TDVector> pts )
  // {
  //   int s = pts.size();
  //   if ( s < 3 ) return new TDVector();

  //   float x0 = 0.0f;
  //   float y0 = 0.0f;
  //   float z0 = 0.0f;
  //   for ( TDVector p : pts ) {
  //     x0 += p.x;
  //     y0 += p.y;
  //     z0 += p.z;
  //   }
  //   x0 /= s;
  //   y0 /= s;
  //   z0 /= s;
  //   // note (x0, y0, z0) is the center of mass of the points

  //   TDVector n = new TDVector( 0, 0, 0 );
  //   TDVector q = pts.get( s - 1 );
  //   for ( TDVector p : pts ) {
  //     n.x += (q.y-y0)*(p.z-z0) - (q.z-z0)*(p.y-y0);
  //     n.y += (q.z-z0)*(p.x-x0) - (q.x-x0)*(p.z-z0);
  //     n.z += (q.x-x0)*(p.y-y0) - (q.y-y0)*(p.x-x0);
  //   }
  //   n.normalize();
  //   return n;
  // }
  
  /** compute the mean vector (CoM) of the vectors of an array
   */
  public static TDVector computeMeanVector( ArrayList<TDVector> pts ) // PRIVATE
  {
    float x0 = 0.0f;
    float y0 = 0.0f;
    float z0 = 0.0f;
    for ( TDVector p : pts ) {
      x0 += p.x;
      y0 += p.y;
      z0 += p.z;
    }
    int n = pts.size();
    return new TDVector( x0/n, y0/n, z0/n );
  }

  public static TDVector computeNormal( ArrayList<TDVector> pts )
  {
    TDVector normal = new TDVector();
    TDVector m = computeMeanVector( pts );
    int n = pts.size() - 1;
    TDVector p1 = pts.get( n );
    float x0 = p1.x - m.x;
    float y0 = p1.y - m.y;
    float z0 = p1.z - m.z;
    for ( int k=0; k < n; ++k ) {
      p1 = pts.get( k );
      float x1 = p1.x - m.x;
      float y1 = p1.y - m.y;
      float z1 = p1.z - m.z;
      normal.x += y0*z1 - y1*z0;
      normal.y += z0*x1 - z1*x1;
      normal.z += x0*y1 - x1*y0;
      x0 = x1;
      y0 = y1;
      z0 = z1;
    }
    normal.normalize();
    return normal;
  }

  public static float computeLength( ArrayList<TDVector> pts )
  {
    float d = 0f;
    TDVector p0 = pts.get(0);
    int npts = pts.size();
    for ( int n = 1; n<npts; ++n ) {
      TDVector p = pts.get( n );
      d += p0.distance( p );
      p0 = p;
    }
    return d;
  }

  /** compute the angle of the projections on a plane of a list of vectors around this vector
   * @param pts2 list of vectors
   * @param normal  normal to the plane
   */
  public float angleAroundVectors( ArrayList<TDVector> pts2, TDVector normal )
  {
    TDVector last_point = pts2.get( pts2.size() - 1 );
    // TDVector w1 = normal.orthogonal( this );
    TDVector w0 = this.minus( last_point );
    w0 = normal.orthogonal( w0 );
    // w0.normalize();
    float a = 0.0f;
    for ( TDVector p : pts2 ) {
      TDVector w2 = this.minus( p );
      w2 = normal.orthogonal( w2 );
      // w2.normalize();
      float s = normal.dot( w0.cross(w2) );
      float c = w0.dot(w2);
      a += TDMath.atan2( s, c );
      w0 = w2;
    }
    return a;
  }

}
