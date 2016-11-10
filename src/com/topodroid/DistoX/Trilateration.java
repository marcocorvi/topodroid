/* @file Trilateration.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid centerline computation: trilateration
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.util.List;

  
public class Trilateration
{
  ArrayList<TrilaterationLeg> legs;
  ArrayList<TrilaterationPoint> points;
  double err0;
  int iter;

  TrilaterationPoint getPoint( String n )
  {
    for ( TrilaterationPoint p : points ) if ( n.equals(p.name) ) return p;
    return null;
  }

  Trilateration( TriCluster cl )
  {
    legs   = new ArrayList<TrilaterationLeg>();
    points = new ArrayList<TrilaterationPoint>();

    // populate
    for ( String n : cl.stations ) {
      points.add( new TrilaterationPoint( n ) );
    }
    for ( TriShot sh : cl.shots ) {
      TrilaterationPoint p1 = getPoint( sh.from );
      TrilaterationPoint p2 = getPoint( sh.to );
      legs.add( new TrilaterationLeg( sh, p1, p2 ) );
    } 
    // initialize points
    initialize();
    // and minimize
    minimize( 0.01, 0.20, 10000 );
  }

  void initialize()
  {
    double d, a;
    boolean repeat = true;
    legs.get(0).pi.used = true;
    while ( repeat ) {
      repeat = false;
      for ( TrilaterationLeg leg : legs ) {
        if ( ! leg.used ) {
          TrilaterationPoint pi = leg.pi;
          TrilaterationPoint pj = leg.pj;
          d = leg.d;
          a = leg.a * Math.PI / 180.0;
          if ( pi.used && ! pj.used ) {
            pj.used = true;
            pj.x = pi.x + d * Math.sin( a );
            pj.y = pi.y + d * Math.cos( a );
            leg.used = true;
            repeat = true;
          } else if ( pj.used && ! pi.used ) {
            pi.used = true;
            pi.x = pj.x - d * Math.sin( a );
            pi.y = pj.y - d * Math.cos( a );
            leg.used = true;
            repeat = true;
          }
	  }
      }
    }
  }

  private void clearPointsDelta() 
  {
    for ( TrilaterationPoint p : points ) { p.dx = p.dy = 0; }
  }

  private void addPointsDelta( double delta )
  {
    for ( TrilaterationPoint p : points ) {
      p.x += p.dx * delta;
      p.y += p.dy * delta;
    }
  }

  private double distance1( TrilaterationPoint p1, TrilaterationPoint p2 )
  {
    return Math.sqrt( (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
  }

  private double computeError2( )
  {
    double error = 0;
    for ( TrilaterationLeg l : legs ) {
      double d = ( distance1( l.pi, l.pj ) - l.d );
      error += d * d;
    }
    return error;
  }

  // eps: error for one point [m] 0.001
  // delta: initial delta [m] 0.10
  // iter_max: max iterations 100000
  void minimize( double eps, double delta, int iter_max )
  {
    eps *= points.size(); // 1 mm per point
    err0 = computeError2();
    int n_pts = points.size();
    for ( iter =0 ; iter < iter_max; ++ iter ) {
      double err3, e;
      for ( int m=1; m<n_pts; ++m ) { // compute derivatives D Error / DPx
        TrilaterationPoint p = points.get( m );
        if ( m > 1 ) {
          p.x += delta;
  	  e = err0 - computeError2();
          p.x -= delta;
  	  p.dx = e / ( 1 + e * e );
        }
        p.y += delta;
        e = err0 - computeError2();
        p.y -= delta;
        p.dy = e / ( 1 + e * e );
      }
      addPointsDelta( delta );
      err3 = computeError2();
      if ( err3 >= err0 ) {
        addPointsDelta( -delta );
        delta = delta / 2;
      } else {
        err0 = err3;
      }
      clearPointsDelta();
      if ( err0 < eps || delta < 0.00000001 ) break;
    }
  }
}

