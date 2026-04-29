/** @file TDAnalytics.java
 *
 * @author marco corvi
 * @date apr 2026
 *
 * @brief name of TopoDroid functions under analytics
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.util;

import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.TDLevel;

import java.net.URL;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
// import java.io.OutputStreamWriter;
// import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import android.content.Context;


public class TDAnalytics
{
  // public static final String NAME = "name";
  // the string must contain only alphanumeric char (a-Z, 0-9)
  public static final String NUM           = "Nnum";
  public static final String LOOP_CLOSURE  = "Nloop";
  public static final String TRILATERATION = "Ntrilat";

  public static final String PLOT_FLIP     = "1flip";  // flipProfile
  public static final String PLOT_COPY     = "1copy";
  public static final String PLOT_SPLIT    = "1split"; // doSplitPlot
  public static final String PLOT_MERGE    = "1merge"; // doMergePlot
  public static final String PLOT_SHIFT    = "1shift"; // setShiftDrawing
  public static final String SCRAP_SPLIT   = "2split"; // doSplitScrap
  public static final String SCRAP_PASTE   = "2paste"; // pasteSplitBufferToScrap

  public static final String RENUMBER      = "0renumber";
  public static final String MULTICOPY     = "0copy";
  public static final String MULTIPASTE    = "0paste";
  public static final String BED_FITTING   = "0bed";
  public static final String DIST_OFFSET   = "0offset";

  public static final String WALL_BUBBLE = "3bubble";
  public static final String WALL_CW     = "3cw";
  public static final String WALL_HULL   = "3hull";
  public static final String WALL_PCRUST = "3prust";

  public static final String DEM     = "3dem";
  public static final String TEXTURE = "3texture";
  public static final String TEMP    = "3temp";
  // public static final String DIKSTRA = "3dikstra";

  public static final String PHOTO  = "Xphoto";
  public static final String AUDIO  = "Xaudio";
  public static final String NOTES  = "Xnotes";
  public static final String SENSOR = "Xsensor";

  public static final String GNSS    = "Sgnss";
  public static final String LATLNG  = "Slatlng";
  public static final String GEOCODE = "Sgeo";
  public static final String PROJ4   = "Sproj4";
  public static final String STATION = "Sstation";
  public static final String MOB_TOP = "Smobtop";
  public static final String GPX_REC = "Sgpxrec";
  public static final String GPS_POS = "Sgpspos";
  public static final String GPS_TST = "Sgpstst";
  public static final String GPS_LOG = "Sgpslog";
  public static final String GPS_PT  = "Sgpspt";

  public static final String CAL_NEW  = "Cnew";
  public static final String CAL_COMP = "Ccomp";
  public static final String CAL_VAL  = "Cval";

  public static final String TH2_EDIT  = "Mth2edit";
  public static final String CAVE3D    = "Mcave3d";
  public static final String PALETTE   = "Mpalette";

  public static final String EXPORT_TH  = "1th";  // Therion
  public static final String EXPORT_DAT = "1dat"; // Comapass
  public static final String EXPORT_SVX = "1svx"; // Survex
  public static final String EXPORT_TRO = "1tro"; // VisualTopo
  public static final String EXPORT_TROX = "1trox"; // VisualTopo
  public static final String EXPORT_CSV = "1csv"; // CSV
  public static final String EXPORT_CSVF = "1csvf"; // CSV full
  public static final String EXPORT_DXF = "1dxf"; // DXF
  public static final String EXPORT_TOP = "1top"; // PocketTopo
  public static final String EXPORT_SRV = "1srv"; // Walls
  public static final String EXPORT_KML = "1kml"; // KML
  public static final String EXPORT_GPX = "1gpx"; // track file
  // public static final String EXPORT_SVG = "1svg";
  public static final String EXPORT_PLG = "1plg"; // Polygon
  public static final String EXPORT_CAV = "1cav"; // Topo (Cav)
  public static final String EXPORT_SUR = "1sur"; // Winkarst
  public static final String EXPORT_TRB = "1trb"; // TopoRobot
  public static final String EXPORT_SHP = "1shp"; // shapefile
  public static final String EXPORT_CSX = "1csx"; // cSurvey
  // public static final String EXPORT_JSON = "1json"; // JSON
  // public static final String EXPORT_PLT = "1plt"; // OziExplorer
  // public static final String EXPORT_TLX = "1tlx"; // TopoLinux
  // public static final String EXPORT_GTX = "1gtx"; // GHTopo
  // public static final String EXPORT_GRT = "1grt"; // Grottolf

  public static final String EXPORT_TH2  = "2th";  // Therion
  public static final String EXPORT_DXF2 = "2dxf"; // DXF
  public static final String EXPORT_SVG2 = "2svg"; // SVG
  public static final String EXPORT_SHP2 = "2shp"; // SHP
  public static final String EXPORT_XVI2 = "2xvi";
  public static final String EXPORT_PDF2 = "2pdf";
  public static final String EXPORT_CSX2 = "2csx";
  public static final String EXPORT_RTRIP = "2rtrp"; // walls round trip

  public static final String IMPORT_TH  = "0th";
  public static final String IMPORT_DAT = "0dat";
  public static final String IMPORT_TRO = "0tro";   // VisualTopo
  public static final String IMPORT_SVX = "0svx";
  public static final String IMPORT_TRB = "0trb";   // TopoRobot
  public static final String IMPORT_SRV = "0srv";   // Walls
  public static final String IMPORT_SNP = "0snp";
  public static final String IMPORT_TOP = "0top";
  public static final String IMPORT_BRIC = "0bric"; // BRIC
  public static final String IMPORT_CVWY = "0cvwy"; // Cavway

  public static final String IMPORT_PREFS = "prefsimp";
  public static final String EXPORT_PREFS = "prefsexp";

  public static final String MAN_PAGE   = "Hman"; 
  public static final String AI_DIALOG  = "Haid"; 
  public static final String AI_SETTING = "Hais"; 

  private static final String QUERY = "analytics=";
  public static String  mCT = null;

  /** send the analytics to the server
   * @param msg analytics message - json format
   * @note used only by TopoDroidApp
   */
  public static boolean sendAnalytics( Context ctx, String msg )
  {
    boolean ret = false;
    HttpURLConnection urlConnection = null;
    try {
      // URL url = new URL( ANALYTICS_SERVER + "?" + msg );
      URL url = new URL( TopoDroidApp.getAnalyticsUri( ctx ) );
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("POST");
      urlConnection.setDoOutput(true); // make it POST
      urlConnection.setReadTimeout(10000 /*ms*/);
      urlConnection.setConnectTimeout(15000 /*ms*/);
      // urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded" );
      // urlConnection.setRequestProperty("Content-Type", "text/plain" );
      urlConnection.setRequestProperty("Content-Type", "application/json; utf-8" );
      urlConnection.setRequestProperty("Content-Length", Integer.toString(msg.length()) ); // msg.getBytes().length
      urlConnection.setRequestProperty("User-Agent", "TopoDroid" );
      urlConnection.setRequestProperty("Accept", "application/json" );
      // urlConnection.setChunkedStreamingMode(0);
      // urlConnection.connect();

      // OutputStream out = new BufferedOutputStream( urlConnection.getOutputStream() ); // raises exception with null message
      DataOutputStream out = new DataOutputStream( urlConnection.getOutputStream() ); // raises exception with null message
      // out.write( QUERY.getBytes( "UTF-8" ) );
      byte[] input = msg.getBytes( "utf-8" );
      out.write( input, 0, input.length );
      out.flush();
      out.close();

      int ret_code = urlConnection.getResponseCode();
      TDLog.v("response code " + ret_code );
      // if ( ret_code == HttpURLConnection.HTTP_OK) {
      //   InputStream in = new BufferedInputStream( urlConnection.getInputStream() );
      //   int nr = 0;
      //   byte[] buffer = new byte[1024];
      //   while ( nr >= 0 ) {
      //     nr = in.read( buffer );
      //     if ( nr > 0 ) {
      //       TDLog.v("read " + nr + ": " + new String(buffer, 0, nr ) );
      //     }
      //   }
      //   in.close();
      // }
      ret = true;
    } catch ( MalformedURLException e ) {
      TDLog.v("URL error " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.v("IO error " + e.getMessage() );
    } catch ( Throwable e ) {
      for ( StackTraceElement st : e.getStackTrace() ) {
        TDLog.v( "  " + st.getFileName() + ":" + st.getLineNumber() + " " + st.getMethodName() );
      }
    } finally {
      if ( urlConnection != null ) urlConnection.disconnect();
    }
    return ret;
  }

  /** send a request to retrive the CT for the analytics
   * @param ctx context
   * @note used only by TopoDroidApp
   */
  public static void retrieveCT( final Context ctx )
  {
    Thread thread = new Thread() {
      @Override public void run() {
        HttpURLConnection urlConnection = null;
        try {
          // URL url = new URL( ANALYTICS_SERVER + "?" + msg );
          // URL url = new URL( TopoDroidApp.getInfoUri( ctx ) );
          URL url = new URL( CT.IRI );
          urlConnection = (HttpURLConnection) url.openConnection();
          urlConnection.setDoInput(true); // make it POST
          urlConnection.setReadTimeout(10000 /*ms*/);
          urlConnection.setConnectTimeout(15000 /*ms*/);
          urlConnection.setRequestProperty("User-Agent", "TopoDroid" );
          urlConnection.setRequestProperty("Accept", "application/json" );

          int ret_code = urlConnection.getResponseCode();
          if ( ret_code == HttpURLConnection.HTTP_OK) {
            InputStream in = new BufferedInputStream( urlConnection.getInputStream() );
            StringBuffer sb = new StringBuffer();
            int nr = 0;
            byte[] buffer = new byte[1024];
            while ( nr >= 0 ) {
              nr = in.read( buffer );
              if ( nr > 0 ) {
                sb.append( new String(buffer, 0, nr ) );
                // TDLog.v("read " + nr + ": " + new String(buffer, 0, nr ) );
              }
            }
            in.close();
            String response = sb.toString();
            int pos = response.indexOf( "code\": \"" );
            if ( pos > 0 ) {
              pos += 8;
              String ct = response.substring( pos, pos+2 );
              TDLog.v("Code " + ct );
              TopoDroidApp.storeCT( ctx, ct );
            }
          // } else {
          //   TDLog.v("response code " + ret_code );
          }
        } catch ( MalformedURLException e ) {
          TDLog.v("URL error " + e.getMessage() );
        } catch ( IOException e ) {
          TDLog.v("IO error " + e.getMessage() );
        } catch ( Throwable e ) {
          for ( StackTraceElement st : e.getStackTrace() ) {
            TDLog.v( "  " + st.getFileName() + ":" + st.getLineNumber() + " " + st.getMethodName() );
          }
        } finally {
          if ( urlConnection != null ) urlConnection.disconnect();
        }
      }
    };
    thread.start();
  }

  /** save the CT
   * @param ct   ct to be used by analytics
   */
  public static void setCT( Context ctx, String ct )
  {
    if ( ct == null ) return;
    mCT = ct;
    TDLevel.setMaior( ctx, CT.maior( ct ) );
  }

  // FXIME_HICSUM
  // /** handle the app place
  //  * @param hs   karst area of the app
  //  */
  // public static void setHicsum( String hs )
  // {
  //   if ( hs == null ) return;
  //   // TODO
  // }


} 

