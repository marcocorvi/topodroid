/* @file GeoCodes.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid geo codes
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * 
 * GeoCode file syntax.
 * One code per line, each line must have (separated by single space)
 *   type code description
 * type = integer in [0,5]
 * code = string without spaces
 * description = everything else on the line
 */
package com.topodroid.TDX;

import java.util.ArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

class GeoCodes
{
  // TODO use HashMap if the codes are many
  private ArrayList< GeoCode > mGeoCodes;

  /** cstr
   */
  GeoCodes( )
  {
    mGeoCodes = new ArrayList< GeoCode >();
    File file = TDPath.getGeocodesFile();
    if ( file.exists() ) {
      try {
        FileReader fr = new FileReader( file );
        BufferedReader br = new BufferedReader( fr );
        String line;
        while ( (line = br.readLine() ) != null ) {
          line = line.trim();
          if ( line.isEmpty() ) continue;
          String[] vals = line.split(" ", 3 );
          if ( vals.length == 3 ) {
            int type = 0;
            try {
              type = Integer.parseInt( vals[0] );
              if ( type < 0 ) type = 0;
            } catch ( NumberFormatException e ) {
              // TODO
            }
            GeoCode geocode = new GeoCode( type, vals[1], vals[2].trim() );
            mGeoCodes.add( geocode );
          }
        }
        fr.close();
      } catch ( FileNotFoundException e ) {
      } catch ( IOException e ) {
      }
    }
  }

  void resetSelected()
  {
    for ( GeoCode geocode : mGeoCodes ) geocode.setSelected( false );
  }

  /** @return the list of geocodes
   */
  ArrayList< GeoCode > getGeoCodes() { return mGeoCodes; }

  /** @return the geocode for a given code (or null if not found)
   * @param code  code of the geocode
   */
  GeoCode getGeoCode( String code )
  {
    if ( code != null ) { 
      for ( GeoCode geocode : mGeoCodes ) if ( geocode.hasGeoCode( code ) ) return geocode;
    }
    return null;
  }

  /** @return the number of geocodes
   */
  int size() { return mGeoCodes.size(); }

  /** set "selected" to the geocode with a given code
   * @param code   code of the geocode
   */
  void setSelected( String code )
  {
    if ( code == null ) return;
    for ( GeoCode geocode : mGeoCodes ) {
      if ( geocode.selectByGeoCode( code ) ) return;
    }
  }

}
