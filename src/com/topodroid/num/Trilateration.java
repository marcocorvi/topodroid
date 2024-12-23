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
package com.topodroid.num;

import com.topodroid.math.Point2D;
import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;

import java.util.ArrayList;
// import java.util.List;

class Trilateration
{
  private ArrayList< TriLeg > legs;
  private ArrayList< TriPoint > points;
  private double error; // result error of the minimization
  private int iter;

  private final static int ITER_MAX = 100;

  /** @return a point
   * @param n   point name
   */
  private TriPoint getPoint( String n )
  {
    for ( TriPoint p : points ) if ( n.equals(p.name) ) return p;
    return null;
  }

  /** @return the error: sum of the abs differences between leg length and endpoints distance
   */
  double getError() { return error; }

  /** @return the number of iterations
   */
  int getIterations() { return ( iter >= ITER_MAX )? -1 : iter; }

  /** @return the number of legs
   */
  int getNrLegs() { return legs.size(); }

  /** @return the number of points
   */
  int getNrPoints() { return points.size(); }

  /** @return list of legs
   */
  ArrayList< TriLeg > getLegs() { return legs; }


  /** cstr
   * @param cl  cluster
   */
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
    error = minimize1( 0.01, ITER_MAX ); // FIXME 0.01 and ITER_MAX are trilateration parameters
  }

  private void initialize()
  {
    double d; // distance
    double a; // angle [rads]
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
            // TDLog.v("TRI init " + pi.name + " --> " + pj.name + " x " + pj.x + " y " + pj.y + " angle [deg] " + leg.a );
            leg.used = true;
            repeat = true;
          } else if ( pj.used && ! pi.used ) {
            pi.used = true;
            pi.x = pj.x - d * Math.sin( a );
            pi.y = pj.y - d * Math.cos( a );
            // TDLog.v("TRI init " + pj.name + " --> " + pi.name + " x " + pi.x + " y " + pi.y + " angle [deg] " + leg.a );
            leg.used = true;
            repeat = true;
          }
	  }
      }
    }
  }

  /** reset points delta to (0,0)
   */
  private void clearPointsDelta() 
  {
    for ( TriPoint p : points ) { p.dx = p.dy = 0; }
  }

  /** add a step to the point position
   * @param delta   step factor
   */
  private void addPointsDelta( double delta )
  {
    for ( TriPoint p : points ) {
      p.x += p.dx * delta;
      p.y += p.dy * delta;
    }
  }

  /** @return the distance (in 2D) between two points
   * @param p1   first point
   * @param p2   secodn point
   */
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

  /** @return the error, sum of abs differences between leg-length and point distance, for all legs 
   * @param n_pts  number of points to consider
   */
  // TODO why not an outer for on legs? because the error depends on the number of points
  private double computeError1( int n_pts )
  {
    // TDLog.v("Error nr pts " + n_pts );
    double err = 0;
    // for ( int i=0; i<n_pts; ++i ) {
    //   TriPoint pi = points.get(i);
    //   for ( int j=i+1; j<n_pts; ++j ) {
    //     TriPoint pj = points.get(j);
    //     double d1 = distance1( pi, pj );
    //     for ( TriLeg l : legs ) {
    //       if ( ( l.pi == pi && l.pj == pj ) || ( l.pi == pj && l.pj == pi ) ) { 
    //         err += Math.abs( d1 - l.d ); 
    //       }
    //     }
    //   }
    // }
    for ( TriLeg l : legs ) {
      double d1 = distance1( l.pi, l.pj );
      err += Math.abs( d1 - l.d ); 
    }
    return err;
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

  // 20241214 the param delta is not used, a new delta is computed from the length of the legs
  // 
  // @param eps      epsilon error for one point
  // @param iter_max maximum number of iterations
  //
  // Error Fct = Sum | d(pi,pj) - leg.d |
  private double minimize1( double eps, int iter_max )
  {
    int n_pts = points.size();
    eps *= n_pts; // 1 mm per point
    double d = 0.0;
    for ( TriLeg l : legs ) d += l.d;
    double delta = d/n_pts * 0.02;  // FIXME 0.02 parameter
    // TDLog.v("TRI delta " + delta + " n pts " + n_pts );
    double err0 = computeError1( n_pts );
    Point2D[] dp = new Point2D[ n_pts ]; // gradient of points (x,y)
    // TDLog.v( "initial error " + err0 );
    // for ( TriPoint p : points ) TDLog.v("TRI p " + p.name + " x " + p.x + " y " + p.y );

    for ( iter =0 ; iter < iter_max; ++ iter ) {
      // TDLog.v("TRI iter " + iter );
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
        // TDLog.v( "error " + err3 );
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
      // for ( TriPoint p : points ) TDLog.v("TRI " + iter + " p " + p.name + " x " + p.x + " y " + p.y );
      if ( err0 < eps || delta < 0.000001 ) break;
    }
    // TDLog.v( "minimize error " + err0 + " iter " + iter + " final delta " + delta );
    // for ( TriPoint p : points ) TDLog.v("TRI p " + p.name + " x " + p.x + " y " + p.y );
    return err0;
  }

  // FIXME this and the next method could be replaced by a method the computes the leg decl and return te max
  //       and a method that clears the leg decl 
  /** @return the maximum of the corrections [degrees]
   */
  float maxAngle()
  {
    float max = 0;
    for ( TriLeg leg : legs ) {
      TriPoint p1 = leg.pi;
      TriPoint p2 = leg.pj;
      // compute azimuth (p2-p1)
      double dx = p2.x - p1.x; // east
      double dy = p2.y - p1.y; // north
      double a = Math.atan2( dx, dy ) * 180 / Math.PI;
      if ( a < 0 ) a += 360;
      // TDLog.v("TRI leg " + p1.name + " " + p2.name + " angle " + a );
      // leg.a is the shot bearing
      // a is the trilateraion bearing
      // setting decl(ave_leg) = trilat_bearing - shot_bearing means that the trilat_bearing is used
      // because num computes the sin/cos of "bearing + decl"
      float da = (float)Math.abs( a - leg.a ); // per shot declination
      if ( max < da ) max = da;
    }  
    return max; 
  }

  /** apply the trilateraion: store the corrections in ave-leg decl
   * @return the maximum of the corrections [degrees]
   */
  void apply()
  {
    for ( TriLeg leg : legs ) {
      TriPoint p1 = leg.pi;
      TriPoint p2 = leg.pj;
      // compute azimuth (p2-p1)
      double dx = p2.x - p1.x; // east
      double dy = p2.y - p1.y; // north
      double a = Math.atan2( dx, dy ) * 180 / Math.PI;
      if ( a < 0 ) a += 360;
      // TDLog.v("TRI leg " + p1.name + " " + p2.name + " angle " + a );
      // leg.a is the shot bearing
      // a is the trilateraion bearing
      // setting decl(ave_leg) = trilat_bearing - shot_bearing means that the trilat_bearing is used
      // because num computes the sin/cos of "bearing + decl"
      leg.shot.mAvgLeg.mDecl = (float)( a - leg.a ); // per shot declination
    }  
  }
}

