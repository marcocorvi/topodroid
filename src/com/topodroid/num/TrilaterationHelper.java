/* @file TrilaterationHelper.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief trilateration loop closure helper
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.util.TDMath;
import com.topodroid.util.TDLog;
import com.topodroid.util.TDAnalytics;
import com.topodroid.math.TDVector;

import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.R;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

class TrilaterationHelper
{
  /** correct temporary shots using trilateration
   * @param shots temporary shot list
   */
  static void makeTrilateration( List< TriShot > shots )
  {
    TopoDroidApp.updateAnalytic( TDAnalytics.TRILATERATION );
    ArrayList< TriCluster > clusters = new ArrayList<>();
    for ( TriShot sh : shots ) sh.mCluster = null;
    boolean repeat = true;
    while ( repeat ) {
      repeat = false;
      for ( TriShot sh : shots ) {
        if ( sh.mCluster != null ) continue;
        repeat = true;
        TriCluster cl = new TriCluster();
        clusters.add( cl );
        cl.addTmpShot( sh );
        cl.addStation( sh.from );
        cl.addStation( sh.to );
        // TDLog.v("TRI cluster start " + sh.from + " " + sh.to + " [" + String.format(Locale.US, "%.2f", sh.length() ) + "]" );
        // recursively populate the cluster
        boolean repeat2 = true;
        while ( repeat2 ) {
          repeat2 = false;
          int ns = shots.size();
          for ( int n1 = 0; n1 < ns; ++n1 ) { // stations
            TriShot sh1 = shots.get(n1);
            if ( sh1.mCluster != null ) continue;
            if ( cl.containsStation( sh1.from ) ) {
              if ( cl.containsStation( sh1.to ) ) {
                cl.addTmpShot( sh1 );
                // TDLog.v("TRI cluster finish " + sh1.from + " " + sh1.to );
              } else {
                // TDLog.v("TRI cluster check have " + sh1.from + " missing " + sh1.to + " [" + String.format(Locale.US, "%.2f", sh1.length() ) + "]" );
                boolean added = false;
                for ( int n2 = n1+1; n2 < ns; ++n2 ) {
                  TriShot sh2 = shots.get(n2);
                  if ( sh2.mCluster != null ) continue;
                  if ( ( sh1.to.equals( sh2.from ) && cl.containsStation( sh2.to ) ) ||
                       ( sh1.to.equals( sh2.to ) && cl.containsStation( sh2.from ) ) ) {
                    // TDLog.v("TRI cluster add (2) " + sh2.from + " " + sh2.to + " [" + String.format(Locale.US, "%.2f", sh2.length() ) + "]" );
                    cl.addTmpShot( sh2 );
                    added = true;
                  }
                }
                if ( added ) {
                  // TDLog.v("TRI cluster add (1) " + sh1.from + " " + sh1.to );
                  cl.addStation( sh1.to );
                  cl.addTmpShot( sh1 );
                  repeat2 = true;
                }
              }
            } else if ( cl.containsStation( sh1.to ) ) {
              // TDLog.v("TRI cluster check missing " + sh1.from + " have " + sh1.to + " [" + String.format(Locale.US, "%.2f", sh1.length() ) + "]" );
              boolean added = false;
              for ( int n2 = n1+1; n2 < ns; ++n2 ) {
                TriShot sh2 = shots.get(n2);
                if ( sh2.mCluster != null ) continue;
                if ( ( sh1.from.equals( sh2.from ) && cl.containsStation( sh2.to ) ) ||
                     ( sh1.from.equals( sh2.to ) && cl.containsStation( sh2.from ) ) ) {
                  // TDLog.v("TRI cluster add (2) " + sh2.from + " " + sh2.to + " [" + String.format(Locale.US, "%.2f", sh2.length() ) + "]" );
                  cl.addTmpShot( sh2 );
                  added = true;
                }
              }
              if ( added ) {
                // TDLog.v("TRI cluster add (1) " + sh1.from + " " + sh1.to + " [" + String.format(Locale.US, "%.2f", sh1.length() ) + "]" );
                cl.addStation( sh1.from );
                cl.addTmpShot( sh1 );
                repeat2 = true;
              }
            }
          }         
          for ( TriShot sh1 : shots ) { // shots (should not be needed)
            if ( sh1.mCluster != null ) continue;
            if ( cl.containsStation( sh1.from ) && cl.containsStation( sh1.to ) ) {
              // TDLog.v("TRI cluster add (final) " + sh1.from + " " + sh1.to + " [" + String.format(Locale.US, "%.2f", sh1.length() ) + "]" );
              cl.addTmpShot( sh1 );
            }
          }
        }
      }
    }
    // apply trilateration with recursive minimization
    int nr_triangle = 0;
    int nr_success  = 0;
    int nr_fail     = 0;
    double err = 0.0;
    // TDLog.v("TRI nr clusters " + clusters.size() );
    for ( TriCluster cl : clusters ) {
      if ( cl.nrStations() > 2 ) {
        ++nr_triangle;
        break;
      }
    }
    if ( nr_triangle == 0 ) {
      TDLog.v("NUM trilateration without triangles");
    } else {
      for ( TriCluster cl : clusters ) {
        if ( cl.nrStations() > 2 ) {
          // cl.dump();
          Trilateration trilateration = new Trilateration( cl );
          // TODO check all conditions a trilateration can fail
          double e = trilateration.getError();
          int iter = trilateration.getIterations();
          int nr_l = trilateration.getNrLegs();
          float max_angle = trilateration.maxAngle();
          // TDLog.v("TRI error " + e + " iter " + iter + " legs " + nr_l + " pts " + trilateration.getNrPoints() + " max_angle " + max_angle );
          if ( iter < 0 || max_angle > 10 ) { // FIXME 10 is a trilateration parameters
            ++ nr_fail;
          } else {
            ++ nr_success;
            err += e;
            // TDLog.v("TRI apply");
            trilateration.apply();
          }
        }
      }
    }
    if ( nr_fail > 0 ) {
      if ( nr_success > 0 ) {
        TDToast.makeWarn( String.format( Locale.US, TDInstance.getResourceString(R.string.trilateration_failure), nr_fail, (nr_fail+nr_success) ) ); 
      } else {
        TDToast.makeWarn( R.string.trilateration_failed );
      }
    } else if ( nr_success > 0 ) {
      TDToast.make( String.format( Locale.US, TDInstance.getResourceString(R.plurals.trilateration_success), nr_success ) );
    }
  }

}
