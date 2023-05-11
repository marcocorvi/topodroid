/** @file ExportGPX.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief centerline GPX exporter
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Triangle3D;
import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.Cave3DSurvey;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Cave3DFix;
import com.topodroid.TDX.Cave3DShot;
import com.topodroid.prefs.TDSetting;
import com.topodroid.mag.Geodetic;
import com.topodroid.c3walls.cw.CWFacet;
import com.topodroid.c3walls.cw.CWPoint;


import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.PrintWriter;
// import java.io.PrintStream;
// import java.io.FileOutputStream;
// import java.io.BufferedOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;

public class ExportGPX
{
  ArrayList<CWFacet> mFacets;
  double lat, lng, h_geo;
  double s_radius, e_radius;
  Cave3DStation zero;
  public ArrayList< Triangle3D > mTriangles;

  public ExportGPX()
  {
    mFacets = new ArrayList< CWFacet >();
    mTriangles = null;
  }

  public void add( CWFacet facet ) { mFacets.add( facet ); }

  public void add( CWPoint v1, CWPoint v2, CWPoint v3 )
  {
     mFacets.add( new CWFacet( v1, v2, v3 ) );
  }

  /** compute the geo-referenced origin station and the long-lat conversion parameters
   * @param data        data parser
   * @param decl        magnetic declination
   * @param h_geo_factor ??? (unused)
   * @return true if data can be geolocalized
   */
  private boolean getGeolocalizedData( TglParser data, double decl, double h_geo_factor )
  {
    // TDLog.v( "GPX get geo-localized data. Declination " + decl );
    List< Cave3DFix > fixes = data.getFixes();
    if ( fixes.size() == 0 ) {
      // TDLog.v( "GPX no geo-localization");
      return false;
    }

    Cave3DFix origin = null;
    for ( Cave3DFix fix : fixes ) {
      if ( ! fix.hasWGS84 ) continue;
      // if ( fix.cs == null ) continue;
      // if ( ! fix.cs.name.equals("long-lat") ) continue;
      for ( Cave3DStation st : data.getStations() ) {
        if ( st.getFullName().equals( fix.getFullName() ) ) {
          origin = fix;
          zero   = st;
          break;
        }
      }
      if ( origin != null ) break;
    }
    if ( origin == null ) {
      // TDLog.v( "GPX no geolocalized origin");
      return false;
    }

    // origin has coordinates ( e, n, z ) these are assumed lat-long
    // altitude is assumed wgs84
    lat = origin.latitude;
    lng = origin.longitude;
    double h_ell = origin.a_ellip;
    h_geo = origin.z; // GPX uses Geoid altitude (unless altitudeMode is set)
    // TDLog.v( "GPX origin " + lat + " N " + lng + " E " + h_geo );

    s_radius = 1.0 / Geodetic.meridianRadiusExact( lat, h_ell );
    e_radius = 1.0 / Geodetic.parallelRadiusExact( lat, h_ell );

    return true;
  }

  public boolean exportASCII( BufferedWriter osw, TglParser data, boolean do_splays, boolean do_walls, boolean do_station )
  {
    String name = data.getName();
    boolean ret = true;
    if ( data == null ) return false; // always false

    if ( ! getGeolocalizedData( data, 0.0f, 1.0f ) ) { // FIXME declination 0.0f
      TDLog.Error( "GPX no geolocalized station");
      return false;
    }
    boolean single_track = TDSetting.mGPXSingleTrack;
    TDLog.v( "GPX export splays " + do_splays + " stations " + do_station + " single track " + single_track );

    // TODO use survey colors
    List< Cave3DSurvey > surveys  = data.getSurveys();

    List< Cave3DStation> stations = data.getStations();
    // List< Cave3DShot>    shots    = data.getShots();
    // List< Cave3DShot>    splays   = data.getSplays();
    double minlat = Double.MAX_VALUE;
    double maxlat = Double.MIN_VALUE;
    double minlon = Double.MAX_VALUE;
    double maxlon = Double.MIN_VALUE;
    for ( Cave3DStation st : stations ) {
      double e = lng + (st.x - zero.x) * e_radius;
      double n = lat + (st.y - zero.y) * s_radius;
      if ( e < minlon ) minlon = e;
      if ( e > maxlon ) maxlon = e;
      if ( n < minlat ) minlat = n;
      if ( n > maxlat ) maxlat = n;
    }

    // now write the GPX
    try {
      PrintWriter pw = new PrintWriter( osw );

      pw.format(Locale.US, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      pw.format(Locale.US, "<gpx version=\"1.1\" creator=\"TopoDroid\" xmlns=\"http://www.topografix.com/GPX/1/1\"\n"); 
      pw.format(Locale.US, "    xmlns:osmand=\"https://osmand.net\"\n"); 
      pw.format(Locale.US, "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); 
      pw.format(Locale.US, "    xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");
      pw.format(Locale.US, "  <time>%s</time>\n", TDUtil.currentDateTime() );
      pw.format(Locale.US, "  <bounds minlat=\"%.7f\" minlon=\"%.7f\" maxlat=\"%.7f\" maxlon=\"%.7f\"/>\n", minlat, minlon, maxlat, maxlon );
      pw.format(Locale.US, "  <extensions>\n");
      pw.format(Locale.US, "    <color>#ff0000</color>\n"); // red
      pw.format(Locale.US, "    <width>thin</width>\n");
      pw.format(Locale.US, "  </extensions>\n");

      if ( single_track ) pw.format(Locale.US, "<trk>\n");
    
      for ( Cave3DSurvey survey : surveys ) {
        String survey_name = survey.getName();
        // int    sid  = survey.getId();
        if ( ! single_track ) {
          pw.format(Locale.US, "<trk>\n");
          pw.format(Locale.US, "  <name>%s</name>\n", survey_name );
          // pw.format(Locale.US, "  <extensions>\n"); // ineffective in OsmAnd
          // pw.format(Locale.US, "    <color>#%06x</color>\n", (0x00ffffff & survey.getColor() ) );
          // pw.format(Locale.US, "  </extensions>\n");
        }
        if ( do_station ) {
          stations = survey.getStations();
          // pw.format(Locale.US, "<Folder>\n");
          // pw.format(Locale.US, "  <name>stations</name>\n" );
          for ( Cave3DStation st : stations ) {
            double e = lng + (st.x - zero.x) * e_radius;
            double n = lat + (st.y - zero.y) * s_radius;
            double z = h_geo + (st.z - zero.z);
            pw.format(Locale.US, "  <wpt lat=\"%.7f\" lon=\"%.7f\">\n", e, n );
            pw.format(Locale.US, "    <ele>%.0f</ele>\n", z );
            pw.format(Locale.US, "    <name>%s</name>\n", st.getFullName() );
            pw.format(Locale.US, "    <desc></desc>\n");
            pw.format(Locale.US, "  </wpt>\n");
          }
          // pw.format(Locale.US, "</Folder>\n");
        // } else {
        //   TDLog.v("3D GPX no stations ");
        }

        List< Cave3DShot > survey_shots = survey.getShots();
        Cave3DStation last = null;
        for ( Cave3DShot sh : survey_shots ) {
          // if ( sh.mSurveyId != sid ) continue;
          Cave3DStation sf = sh.from_station;
          Cave3DStation st = sh.to_station;
          if ( sf == null || st == null ) continue;
          if ( last == null ) {
            pw.format(Locale.US, "    <trkseg>\n");
          } else if ( last != sf ) {
            pw.format(Locale.US, "    </trkseg>\n");
            pw.format(Locale.US, "    <trkseg>\n");
            double ef = lng + (sf.x - zero.x) * e_radius;
            double nf = lat + (sf.y - zero.y) * s_radius;
            double zf = h_geo + (sf.z - zero.z);
            pw.format(Locale.US, "      <trkpt lon=\"%.7f\" lat=\"%.7f\"><ele>%.1f</ele></trkpt>\n", ef, nf, zf ); 
          }
          double et = lng + (st.x - zero.x) * e_radius;
          double nt = lat + (st.y - zero.y) * s_radius;
          double zt = h_geo + (st.z - zero.z);
          // pw.format(Locale.US, "    <name>%s-%s</name>\n", sf.getFullName(), st.getFullName() );
          pw.format(Locale.US, "      <trkpt lon=\"%.7f\" lat=\"%.7f\"><ele>%.1f</ele></trkpt>\n", et, nt, zt ); 
          last = st;
        }
        if ( last != null ) {
          pw.format(Locale.US, "    </trkseg>\n");
        }

        if ( do_splays ) {
          List< Cave3DShot > splays = survey.getSplays();
          for ( Cave3DShot sp : splays ) {
            Cave3DStation sf = sp.from_station;
            if ( sf == null ) continue;
            Vector3D v = sp.toVector3D();
            double ef = lng + (sf.x - zero.x) * e_radius;
            double nf = lat + (sf.y - zero.y) * s_radius;
            double zf = h_geo + (sf.z - zero.z);
            double et = lng + (sf.x + v.x - zero.x) * e_radius;
            double nt = lat + (sf.y + v.y - zero.y) * s_radius;
            double zt = h_geo + (sf.z + v.z - zero.z);
            pw.format(Locale.US, "    <trkseg>\n");
            pw.format(Locale.US, "      <trkpt lon=\"%.7f\" lat=\"%.7f\"><ele>%.1f</ele></trkpt>\n", ef, nf, zf ); 
            pw.format(Locale.US, "      <trkpt lon=\"%.7f\" lat=\"%.7f\"><ele>%.1f</ele></trkpt>\n", et, nt, zt ); 
            pw.format(Locale.US, "    </trkseg>\n");
          }
        }
        if ( ! single_track ) pw.format(Locale.US, "</trk>\n");
      }
      if ( single_track ) pw.format(Locale.US, "</trk>\n");
      pw.format(Locale.US, "</gpx>\n");
      osw.flush();
      osw.close();
      return true;
    } catch ( IOException e ) {
      TDLog.Error( "GPX IO error " + e.getMessage() );
      return false;
    }
  }

}

