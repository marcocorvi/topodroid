/* @file GeodeticHeight.java
 *
 * @author marco corvi
 * @date nov 2012
 *
 * @brief TopoDroid geodetic height ( from internet )
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130520 altimetric altitude
 * 20140220 failure notification with negative return values (multiple of -1000)
 */
package com.topodroid.DistoX;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.MalformedURLException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;

import java.util.Date;

// import android.util.Log;

class GeodeticHeight
{
  private static String NGA_URL = "http://earth-info.nga.mil/nga-bin/gandg-bin/intpt.cgi";

  static double geodeticHeight( double lat, double lng )
  {
    int latdd = (int)lat;
    lat = (lat-latdd)*60;
    int latmm = (int)lat;
    lat = (lat-latdd)*60;
    int lngdd = (int)lng;
    lng = (lng-lngdd)*60;
    int lngmm = (int)lng;
    lng = (lng-lngdd)*60;
    return geodeticHeight( Integer.toString(latdd), Integer.toString(latmm), Double.toString(lat),
                           Integer.toString(lngdd), Integer.toString(lngmm), Double.toString(lng) );
  }
   
  static double geodeticHeight( String lat, String lng ) 
  {
    String[] lattoken = lat.split( ":" );
    String[] lngtoken = lng.split( ":" );
    String dd0 = "0";
    String mm0 = "0";
    String ss0 = "0";
    String dd1 = "0";
    String mm1 = "0";
    String ss1 = "0";
    if ( lattoken.length > 0 ) {
      dd0 = lattoken[0];
      if ( lattoken.length > 1 ) {
        mm0 = lattoken[1];
        if ( lattoken.length > 2 ) {
          ss0 = lattoken[2];
        }
      }
    }
    if ( lngtoken.length > 0 ) {
      dd1 = lngtoken[0];
      if ( lngtoken.length > 1 ) {
        mm1 = lngtoken[1];
        if ( lngtoken.length > 2 ) {
          ss1 = lngtoken[2];
        }
      }
    }
    return geodeticHeight( dd0, mm0, ss0, dd1, mm1, ss1 );
  }

  static double geodeticHeight( String latdd, String latmm, String latss,
                                String lngdd, String lngmm, String lngss )
  {
    if ( latdd == null || latmm == null || latss == null
      || lngdd == null || lngmm == null || lngss == null ) return -4000;

    double N = 0.0;
    String content = 
         "LatitudeDeg="  + URLEncoder.encode(latdd)
      + "&LatitudeMin="  + URLEncoder.encode(latmm)
      + "&LatitudeSec="  + URLEncoder.encode(latss)
      + "&LongitudeDeg=" + URLEncoder.encode(lngdd)
      + "&LongitudeMin=" + URLEncoder.encode(lngmm)
      + "&LongitudeSec=" + URLEncoder.encode(lngss)
      + "&Units=meters";

    OutputStream os = null;
    BufferedReader br = null;
    try {
      URLConnection url_conn = new URL( NGA_URL ).openConnection();
      url_conn.setDoOutput( true );
      HttpURLConnection conn = (HttpURLConnection)url_conn;
 
      url_conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
      url_conn.setRequestProperty( "Content-length", Integer.toString(content.getBytes().length) );

      os = conn.getOutputStream();
      os.write( content.getBytes() );
      os.close();
    
      InputStream in = new BufferedInputStream( conn.getInputStream() );
      br = new BufferedReader( new InputStreamReader( in ) );
      String line = "";
      while ( (line = br.readLine()) != null ) {
        if ( line.contains("Geoid Height") ) break;
      }
      if ( line == null ) {
        N = -1000;
      } else {
        StringBuilder sb = new StringBuilder( line );
        while ( (line = br.readLine()) != null ) {
          sb.append( line );
          if ( line.contains("Meters") ) {
            int to = sb.indexOf("Meters") - 1;
            int fr = to - 1;
            while ( ! Character.isWhitespace( sb.charAt( fr ) ) ) --fr;
            fr += 5;
            try {
              N = Double.parseDouble( sb.substring( fr, to ) );
            } catch ( NumberFormatException e ) {
              N = -1000;
            }
          }
        }
      }
    } catch ( MalformedURLException e ) {
      // TODO
      N = -2000;
    } catch ( IOException e ) {
      // TODO
      N = -3000;
    } finally {
      if ( os != null ) {
        try {
          os.close();
        } catch ( IOException e ) {
        }
      }
      if ( br != null ) {
        try {
          br.close();
        } catch ( IOException e ) {
        }
      }
    }
    return N;
  }

  static String GEOMAG_URL = "http://www.ngdc.noaa.gov/geomag-web/calculators/calculateDeclination";
  // ?lat1=%.8f&lon1=%.8f&lat1Hemisphere=%s&lon1Hemisphere=%s&model=WMM&startYear=%d&startMonth=%d&startDay=%d&resultFormat=csv";

  static float getGeomag( FixedInfo fxd )
  {
    // float lat = fxd.lat;
    // float lon = fxd.lon;
    String latHemisphere = "N"; // FIXME
    String lonHemisphere = "E";
    Date date = new Date();
    int year  = date.getYear() + 1900;
    int month = date.getMonth() + 1;
    int day   = date.getDay();

    float decl = 0.0f; // returnm value;

    String content = 
         "lat1="  + fxd.lat
      + "&lon1="  + fxd.lng
      + "&lat1Hemisphere=" + latHemisphere
      + "&lon1Hemisphere=" + lonHemisphere
      + "&model=WMM" 
      + "&startYear=" + year
      + "&startMonth=" + month
      + "&startDay" + day
      + "&resultFormat=csv";

    OutputStream os = null;
    BufferedReader br = null;
    try {
      URLConnection url_conn = new URL( GEOMAG_URL ).openConnection();
      url_conn.setDoOutput( true );
      HttpURLConnection conn = (HttpURLConnection)url_conn;
 
      url_conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
      url_conn.setRequestProperty( "Content-length", Integer.toString(content.getBytes().length) );

      os = conn.getOutputStream();
      os.write( content.getBytes() );
      os.close();    

      InputStream in = new BufferedInputStream( conn.getInputStream() );
      br = new BufferedReader( new InputStreamReader( in ) );
      String line = "";
      while ( (line = br.readLine()) != null ) {
        if ( line.startsWith("#") ) continue;
        String[] vals = line.split(",");
        try {
          decl = Float.parseFloat( vals[3] );
        } catch ( NumberFormatException e ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "declination parse error " + vals[3] );
        }
        break;
      }
    } catch ( NumberFormatException e ) {
      // TODO
      return -182;
    } catch ( MalformedURLException e ) {
      // TODO
      return -180;
    } catch ( IOException e ) {
      // TODO
      return -181;
    } finally {
      if ( os != null ) {
        try {
          os.close();
        } catch ( IOException e ) {
        }
      }
      if ( br != null ) {
        try {
          br.close();
        } catch ( IOException e ) {
        }
      }
    }
    return decl;
  }
}
