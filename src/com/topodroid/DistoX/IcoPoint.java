/** @file IcoPoint.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief point of 3D rose direction diagram
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 */
package com.topodroid.DistoX;

public class IcoPoint
{
  static final double G  = ( 1 + Math.sqrt(5) ) / 2;
  static final double R2 = 1 + G*G;
  static final double R  = Math.sqrt( R2 );

  double x, y, z; // cartesian coords

  IcoPoint()
  { 
    x = 0;
    y = 1;
    z = G;
  }

  IcoPoint( double x0, double y0, double z0 )
  {
    double d = R / Math.sqrt( x0*x0 + y0*y0 + z0*z0 );
    x = x0 * d;
    y = y0 * d;
    z = z0 * d;
  }

  double distance( double x0, double y0, double z0 )
  {
    return Math.sqrt( (x-x0)*(x-x0) + (y-y0)*(y-y0) + (z-z0)*(z-z0) );
  }

  double distance( IcoPoint p ) 
  {
    return Math.sqrt( (x-p.x)*(x-p.x) + (y-p.y)*(y-p.y) + (z-p.z)*(z-p.z) );
  }

  IcoPoint cross( IcoPoint p )
  {
    double x0 = y * p.z - z * p.y;
    double y0 = z * p.x - x * p.z;
    double z0 = x * p.y - y * p.x;
    return new IcoPoint( x0, y0, z0 );
  }

  double times( IcoPoint p ) 
  {
    return x * p.x + y * p.y + z * p.z;
  }

  IcoPoint times( double a ) 
  {
    return new IcoPoint( a*x, a*y, a*z );
  }

  static double angle( IcoPoint p1, IcoPoint p2 )
  {
    return Math.acos( p1.times(p2) / R2 );
  }

  boolean equals( IcoPoint p ) 
  {
    return x == p.x && y == p.y && z == p.z;
  }

  /** interpolate between two IcoPoints
   */
  static IcoPoint interpolate( IcoPoint p1, IcoPoint p2, int i, int n )
  {
    if ( i < 0 || i > n ) return null;
    if ( i == 0 ) return p1;
    if ( i == n ) return p2;
    if ( p1.equals(p2) ) return p1;
    double a = (i*angle( p1, p2 ))/n; // rotation angle
    double x0 = p1.y * p2.z - p1.z * p2.y;
    double y0 = p1.z * p2.x - p1.x * p2.z;
    double z0 = p1.x * p2.y - p1.y * p2.x;
    double d = Math.sqrt( x0*x0 + y0*y0 + z0*z0 );
    x0 /= d;  // N = unit( P1 x P2 )
    y0 /= d;
    z0 /= d;
    double x1 = y0 * p1.z - z0 * p1.y; // N x P1
    double y1 = z0 * p1.x - x0 * p1.z;
    double z1 = x0 * p1.y - y0 * p1.x;
    double ca = Math.cos( a );
    double sa = Math.sin( a );
    return new IcoPoint( p1.x * ca + x1 * sa, p1.y * ca + y1 * sa, p1.z * ca + z1 * sa );
  }

}
  
