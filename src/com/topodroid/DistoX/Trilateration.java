/* @file Trilateration.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid centerline computation: trilateration
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;
// import java.util.List;

// import android.util.Log;

class Trilateration
{
  ArrayList<TriLeg> legs;
  private ArrayList<TriPoint> points;
  private double error;
  private int iter;

  private TriPoint getPoint( String n )
  {
    for ( TriPoint p : points ) if ( n.equals(p.name) ) return p;
    return null;
  }

  double getError() { return error; }

  int getIterations() { return iter; }


  Trilateration( TriCluster cl )
  {
    legs   = new ArrayList<>();
    points = new ArrayList<>();

    // populate
    for ( String n : cl.stations ) {
      points.add( new TriPoint( n ) );
    }
    for ( TriShot sh : cl.shots ) {
      TriPoint p1 = getPoint( sh.from );
      TriPoint p2 = getPoint( sh.to );
      legs.add( new TriLeg( sh, p1, p2 ) );
    } 
    // initialize points
    initialize();
    // and minimize
    error = minimize1( 0.01, 0.10, 100 );
  }

  private void initialize()
  {
    double d, a;
    boolean repeat = true;
    legs.get(0).pi.used = true;
    while ( repeat ) {
      repeat = false;
      for ( TriLeg leg : legs ) {
        if ( ! leg.used ) {
          TriPoint pi = leg.pi;
          TriPoint pj = leg.pj;
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
    for ( TriPoint p : points ) { p.dx = p.dy = 0; }
  }

  private void addPointsDelta( double delta )
  {
    for ( TriPoint p : points ) {
      p.x += p.dx * delta;
      p.y += p.dy * delta;
    }
  }

  private double distance1( TriPoint p1, TriPoint p2 )
  {
    return Math.sqrt( (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y) );
  }

  // private double computeError2( )
  // {
  //   double error = 0;
  //   for ( TriLeg l : legs ) {
  //     double d = ( distance1( l.pi, l.pj ) - l.d );
  //     error += d * d;
  //   }
  //   return error;
  // }

  private double computeError1( int n_pts )
  {
    double error = 0;
    for ( int i=0; i<n_pts; ++i ) {
      TriPoint pi = points.get(i);
      for ( int j=i+1; j<n_pts; ++j ) {
        TriPoint pj = points.get(j);
        double d1 = distance1( pi, pj );
        for ( TriLeg l : legs ) {
          if ( ( l.pi == pi && l.pj == pj ) || ( l.pi == pj && l.pj == pi ) ) { 
            error += Math.abs( d1 - l.d ); 
          }
        }
      }
    }
    return error;
  }



  // eps: error for one point [m] 0.001
  // delta: initial delta [m] 0.10
  // iter_max: max iterations 100000
  //
  // Error Fct = Sum ( d(pi,pj) - leg.d )^2
  // double minimize2( double eps, double delta, int iter_max )
  // {
  //   eps *= points.size(); // 1 mm per point
  //   double err0 = computeError2();
  //   int n_pts = points.size();
  //   for ( iter =0 ; iter < iter_max; ++ iter ) {
  //     double err3, e;
  //     for ( int m=1; m<n_pts; ++m ) { // compute derivatives D Error / DPx
  //       TriPoint p = points.get( m );
  //       if ( m > 1 ) {
  //         p.x += delta;
  // 	  e = err0 - computeError2();
  //         p.x -= delta;
  // 	  p.dx = e / ( 1 + e * e );
  //       }
  //       p.y += delta;
  //       e = err0 - computeError2();
  //       p.y -= delta;
  //       p.dy = e / ( 1 + e * e );
  //     }
  //     addPointsDelta( delta );
  //     err3 = computeError2();
  //     if ( err3 >= err0 ) {
  //       addPointsDelta( -delta );
  //       delta = delta / 2;
  //     } else {
  //       err0 = err3;
  //     }
  //     clearPointsDelta();
  //     if ( err0 < eps || delta < 0.00000001 ) break;
  //   }
  //   return err0;
  // }

  // Error Fct = Sum | d(pi,pj) - leg.d |
  private double minimize1( double eps, double delta, int iter_max )
  {
    int n_pts = points.size();
    eps *= n_pts; // 1 mm per point
    double err0 = computeError1( n_pts );
    // Log.v("DistoX", "initial error " + err0 );
    Point2D[] dp = new Point2D[ n_pts ]; // gradient of points (x,y)
    for ( iter =0 ; iter < iter_max; ++ iter ) {
      for ( int i=0; i<n_pts; ++i ) {
        TriPoint pi = points.get(i);
        double dx = 0;
        double dy = 0;
        for ( TriLeg l : legs ) {
          TriPoint pj = ( l.pi == pi )? l.pj : ( l.pj == pi )? l.pi : null;
          if ( pj != null ) {
            double d1 = distance1( pi, pj );
            if ( d1 > eps ) {
              if ( d1 < l.d ) { // sgn = -1: must enlarge (pi,pj)
                dx += ( pi.x - pj.x ) / d1; // if ( xi < xj ) dx < 0 therefore xi moves left
                dy += ( pi.y - pj.y ) / d1; // if ( x1 > xj ) dx > 0 therefore xi moves right
              } else if ( d1 > l.d ) { // sgn = +1
                dx -= ( pi.x - pj.x ) / d1;
                dy -= ( pi.y - pj.y ) / d1;
              }
            }
          }
        }
        dp[i] = new Point2D( (float)dx, (float)dy );
      }      
      for ( int i=0; i<n_pts; ++i ) { // gradient descent
        TriPoint p = points.get( i );
        p.x += dp[i].x * delta;
        p.y += dp[i].y * delta;
      }   
      for ( int k=0; k<2; ++k ) {
        double err3 = computeError1( n_pts );
        // Log.v("DistoX", "error " + err3 );
        if ( err3 >= err0 ) {
          delta /= 2;
          for ( int i=0; i<n_pts; ++i ) {
            TriPoint p = points.get( i );
            p.x -= dp[i].x * delta;
            p.y -= dp[i].y * delta;
          }      
        } else {
          err0 = err3;
          break;
        }
      }
      if ( err0 < eps || delta < 0.000001 ) break;
    }
    // Log.v("DistoX", "minimize error " + err0 + " iter " + iter + " final delta " + delta );
    return err0;
  }

}

