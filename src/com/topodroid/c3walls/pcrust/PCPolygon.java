/** @file PCPolygon.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief non-convex 2D polygon
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3walls.pcrust;

import com.topodroid.DistoX.Vector3D;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.ArrayList;

public class PCPolygon
{
  ArrayList< PCSite > points;

  PCPolygon( )
  {
    points = new ArrayList< PCSite >();
  }

  public int size() { return points.size(); }

  public Vector3D get( int k ) { return points.get(k); }

  // return true if the site is already in the polygon
  boolean addPoint( PCSite s )
  {
    for ( PCSite pt : points ) if ( pt == s ) return true;
    points.add( s );
    // points.add( new Point2D( s.x, s.y ) );
    return false;
  }

  double getAverageDistance() 
  {
    int ns = points.size();
    if ( ns <= 1 ) return 0.0;
    double d = 0.0;
    for ( int k=1; k<ns; ++k ) d += Vector3D.distance3D( points.get(k-1), points.get(k) );
    return d / (ns - 1);
  }
}
