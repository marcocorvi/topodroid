/* @file TrbStruct.java
 *
 * @author marco corvi
 * @date oct 2023
 *
 * @grief TopoRobot struct
 * --------------------------------------------------------
 *  copyright this software is distributed under gpl-3.0 or later
 *  see the file copying.
 * --------------------------------------------------------
 */
package com.topodroid.io.trb;

import java.util.ArrayList;
import java.util.HashMap;

import com.topodroid.TDX.DBlock;

public class TrbStruct
{
  ArrayList< TrbSeries > mTrbSeries; // array of series
  HashMap< String, String > mMap;    // TopoDroid to TopoRobot station names map

  /** cstr
   */
  public TrbStruct()
  {
    mMap = new HashMap< String, String >();
    mTrbSeries = new ArrayList< TrbSeries >();
  }

  /** @return the TopoRobot name for a station
   * @param st TopoDroid station name
   */
  public String getTrb( String st ) { return mMap.get( st ); }

  /** insert a series
   * @param sr TopoRobot series
   */
  public void addSeries( TrbSeries sr ) { mTrbSeries.add( sr ); }

  /** insert a TopoRobot station name
   * @param k TopoDroid station name
   * @param v TopoRobot station name
   */
  public void put( String k, String v ) { mMap.put( k, v ); }

  /** @return the TopoRobot station name for a TopoDroid station name
   * @param k TopoDroid station name
   */
  public String get( String k ) { return mMap.get( k ); }

  /** @return the array of series
   */
  public ArrayList< TrbSeries > getSeries() { return mTrbSeries; }

  /** @return the number of series
   */
  public int getNrSeries() { return mTrbSeries.size(); }

  /** @return the number of stations
   */
  public int getNrStations() { return mMap.size(); }

}
