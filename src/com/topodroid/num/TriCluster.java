/* @file TriCluster.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid centerline computation: cluster of triangles
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDLog;

import java.util.ArrayList;
import java.util.Locale;

class TriCluster
{
  ArrayList< TriShot > shots;
  ArrayList< String >  stations;

  TriCluster() 
  {
    shots    = new ArrayList<>();
    stations = new ArrayList<>();
  }

  /** @return the number of shots in the cluster
   */
  int nrShots() { return shots.size(); }

  /** @return the number of stations in the cluster
   */
  int nrStations() { return stations.size(); }

  /** add a shot
   * @param ts   shot to add
   */
  void addTmpShot( TriShot ts )
  {
    shots.add( ts );
    ts.cluster = this;
  }

  /** add a station to the cluster
   * @param st   station (name)
   */
  void addStation( String st )
  {
    if ( ! containsStation( st ) ) stations.add( st );
  }

  /** @return true if the cluster contains the given station
   * @param st  given station (name)
   */
  boolean containsStation( String st )
  {
    if ( st == null ) return true;
    for ( String s : stations ) if ( st.equals(s) ) return true;
    return false;
  }

  // /** log debug
  //  */
  // void dump()
  // {
  //   StringBuilder sb1 = new StringBuilder();
  //   for ( String s : stations ) sb1.append(s).append(" ");
  //   TDLog.v("TRI cluster: " + stations.size() + " stations: " + sb1.toString() ); 
  //   StringBuilder sb2 = new StringBuilder();
  //   for ( TriShot t : shots ) sb2.append(t.from).append("-").append(t.to).append(" [").append( String.format(Locale.US,"%.2f",t.length()) ).append("] ");
  //   TDLog.v("             " + shots.size() + " shots: " + sb2.toString() ); 
  // }
}

