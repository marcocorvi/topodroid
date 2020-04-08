/** @File TdmEquate.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief TopoDroid Manager station-equate objectc
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import java.util.ArrayList;

import android.util.Log;

class TdmEquate
{
  ArrayList< String > mStations; // full station names

  TdmEquate()
  {
    mStations = new ArrayList< String >();
  }

  boolean contains( String name )
  {
    for ( String st : mStations ) {
      if ( st.equals( name ) ) return true;
    }
    return false;
  }

  // get the station name of the station@survey in this equate
  // return null if there is no ...@survey 
  String getSurveyStation( String survey )
  {
    // Log.v("TdManager", "get survey station " + survey );
    for ( String name : mStations ) {
      // Log.v("TdManager", "try name <" + name + ">" );
      String[] names = name.split("@");
      if ( names.length > 1 && survey.equals( names[1] ) ) return names[0];
    }
    return null;
  }

  int dropStations( String survey )
  {
    ArrayList< String > stations = new ArrayList<>();
    for ( String name : mStations ) {
      String[] names = name.split("@");
      if ( names.length > 1 && survey.equals( names[1] ) ) {
	// Log.v("TdManager", "equate drop station >" + name + "<" );
      } else {
	stations.add( name );
      }
    }
    mStations = stations;
    // Log.v("TdManager", "equate " + stationsString() + " size " + size() );
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


  void addStation( String station ) { mStations.add( station ); }

  int size() { return mStations.size(); }

  String stationsString()
  {
    StringBuilder sb = new StringBuilder();
    for ( String name : mStations ) {
      sb.append( name + " " );
    }
    sb.deleteCharAt( sb.length() - 1 );
    return sb.toString();
  }
}
