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

  /** @return true if all the station names are already in TopoRobot style
   */
  public boolean areStationsAllTopoRobot()
  {
    for ( String st : mMap.keySet() ) {
      int pos = st.indexOf( "." );
      if ( pos < 1 || pos >= st.length() - 1 ) return false;
      try {
        Integer.parseInt( st.substring(0, pos) );
        Integer.parseInt( st.substring(pos + 1) );
      } catch ( NumberFormatException e ) { return false; }
    }
    return true;
  }

  /** copy the names of the TopoRobot stations and compute the series (TODO)
   */
  public void copyStations()
  {
    for ( String st : mMap.keySet() ) {
      mMap.put( st, st );
      int pos = st.indexOf( "." );
      try {
        int sr = Integer.parseInt( st.substring(0, pos) );
        int pt = Integer.parseInt( st.substring(pos + 1) );
        TrbSeries srs = getSeries( sr );
        if ( srs == null ) {
          srs = new TrbSeries( sr, sr, pt );
          mTrbSeries.add( srs );
        }
        srs.increaseNrPoints();
      } catch ( NumberFormatException e ) { /* does not happen */ } 
    }
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

  /** @return a series (or null)
   * @param sr  series number
   */
  public TrbSeries getSeries( int sr ) 
  {
    for ( TrbSeries srs : mTrbSeries ) if ( srs.series == sr ) return srs;
    return null;
  }

}
