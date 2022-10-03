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

import java.util.ArrayList;

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
}

