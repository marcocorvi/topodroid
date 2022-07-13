/** @File TdmEquate.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager station-equate object
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import java.util.ArrayList;

class TdmEquate
{
  ArrayList< String > mStations; // full station names

  /** cstr
   */
  TdmEquate()
  {
    mStations = new ArrayList< String >();
  }

  /** return true if the equate contains the given station
   * @param name   station name
   */
  boolean contains( String name )
  {
    for ( String st : mStations ) {
      if ( st.equals( name ) ) return true;
    }
    return false;
  }

  /** get the station name of the station@survey in this equate
   * @param survey   survey name
   * @return null if there is no ...@survey 
   */
  String getSurveyStation( String survey )
  {
    // TDLog.v("Equate: get survey station " + survey );
    for ( String name : mStations ) {
      // TDLog.v("Equate: try name <" + name + ">" );
      String[] names = name.split("@");
      if ( names.length > 1 && survey.equals( names[1] ) ) return names[0];
    }
    return null;
  }

  /** remove the stations of a given survey
   * @param survey   survey name
   */
  int dropStations( String survey )
  {
    ArrayList< String > stations = new ArrayList<>();
    for ( String name : mStations ) {
      String[] names = name.split("@");
      if ( names.length > 1 && survey.equals( names[1] ) ) {
	// TDLog.v("equate drop station >" + name + "<" );
      } else {
	stations.add( name );
      }
    }
    mStations = stations;
    // TDLog.v("equate " + stationsString() + " size " + size() );
    return mStations.size();
  }

  // boolean containsStations( String survey ) 
  // {
  //   for ( String name : mStations ) {
  //     String[] names = name.split("@");
  //     if ( names.length > 1 && survey.equals( names[1] ) ) return true;
  //   }
  //   return false;
  // }

  /** add a station to the equate
   * @param station  station name
   */
  void addStation( String station ) { mStations.add( station ); }

  /** @return the number of stations in the equate
   */
  int size() { return mStations.size(); }
  
  /** @return the string presentation of the stations in the equate
   */
  String stationsString()
  {
    StringBuilder sb = new StringBuilder();
    for ( String name : mStations ) {
      sb.append( name ).append( " " );
    }
    sb.deleteCharAt( sb.length() - 1 );
    return sb.toString();
  }
}
