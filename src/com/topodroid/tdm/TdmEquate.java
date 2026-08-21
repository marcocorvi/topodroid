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

import com.topodroid.util.TDLog;

import java.util.List;
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

  /** @return the station name of a "station@survey" fullname, null if it is not of the given survey
   * @param name    station fullname
   * @param survey  survey name
   */
  private static String stationOfSurvey( String name, String survey )
  {
    int at = name.lastIndexOf( '@' );
    if ( at <= 0 || at+1 >= name.length() ) return null;
    if ( ! survey.equals( name.substring( at+1 ) ) ) return null;
    return name.substring( 0, at );
  }

  /** @return the first view-station of a command for a station in this equate
   * @param cmd   view-command
   */
  TdmViewStation getCommandStation( TdmViewCommand cmd )
  {
    String survey_name = cmd.mSurvey.mName;
    // for (  TdmViewStation st : cmd.mStations ) {
    //   String station = st.getName() + "@" + survey_name;
    //   for ( String name : mStations ) if ( name.equals( station ) ) return st;
    // }
	for ( String name : mStations ) {
      String station = stationOfSurvey( name, survey_name );
      if ( station == null ) continue;
      TdmViewStation st = cmd.getViewStation( station );
      if ( st != null ) return st;
    }
    return null;
  }

  /** @return the view-stations of a command for the stations in this equate
   * @param cmd   view-command
   */
  List< TdmViewStation > getCommandAllStation( TdmViewCommand cmd )
  {
    ArrayList< TdmViewStation > ret = new ArrayList<>();
    String survey_name = cmd.mSurvey.mName;
    // for (  TdmViewStation st : cmd.mStations ) {
    //   String station = st.getName() + "@" + survey_name; 
    //   for ( String name : mStations ) if ( name.equals( station ) ) ret.add( st );
    // }
	for ( String name : mStations ) {
      String station = stationOfSurvey( name, survey_name );
      if ( station == null ) continue;
      TdmViewStation st = cmd.getViewStation( station );
      if ( st != null ) ret.add( st );
    }
    return ret;
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

  // void dumpEquate()
  // {
  //   TDLog.v("Equate "  + mStations.size() + ": " + stationsString() );
  // }
}
