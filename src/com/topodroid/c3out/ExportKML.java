/** @file ExportKML.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Walls KML exporter
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.utils.TDLog;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Triangle3D;
import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.Cave3DSurvey;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Cave3DFix;
import com.topodroid.TDX.Cave3DShot;
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

public class ExportKML extends ExportGeo
{
  ArrayList<CWFacet> mFacets;
  public ArrayList< Triangle3D > mTriangles;

  public ExportKML()
  {
    mFacets = new ArrayList< CWFacet >();
    mTriangles = null;
  }

  public void add( CWFacet facet ) { mFacets.add( facet ); }

  public void add( CWPoint v1, CWPoint v2, CWPoint v3 )
  {
     mFacets.add( new CWFacet( v1, v2, v3 ) );
  }

  public boolean exportASCII( BufferedWriter osw, TglParser data, boolean do_splays, boolean do_walls, boolean do_station )
  {
    String name = data.getName();
    boolean ret = true;
    if ( data == null ) return false; // always false

    TDLog.v( "KML export splays " + do_splays + " walls " + do_walls + " stations " + do_station );
    if ( ! getGeolocalizedData( data, 0.0f, 1.0f ) ) { // FIXME declination 0.0f
      TDLog.Error( "KML no geolocalized station");
      return false;
    }

    // TODO use survey colors
    List< Cave3DSurvey > surveys  = data.getSurveys();

    // List< Cave3DStation> stations = data.getStations();
    // List< Cave3DShot>    shots    = data.getShots();
    // List< Cave3DShot>    splays   = data.getSplays();

    // now write the KML
    try {
      PrintWriter pw = new PrintWriter( osw );

      pw.format(Locale.US, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      pw.format(Locale.US, "<kml xmlnx=\"http://www.opengis.net/kml/2.2\">\n");
      pw.format(Locale.US, "<Document>\n");

      pw.format(Locale.US, "<name>%s</name>\n", name );
      pw.format(Locale.US, "<description>%s</description>\n", name );

      for ( Cave3DSurvey survey : surveys ) {
        int color = survey.getColor();
        int red   = (color & 0x00ff0000) >> 16;   // make color AABBGGRR
        int blue  = (color & 0x000000ff) << 16;
        color = (color & 0xff00ff00 ) | red | blue;
        pw.format(Locale.US, "<Style id=\"%s\">\n", survey.getName() );
        pw.format(Locale.US, "  <LineStyle>\n");
        pw.format(Locale.US, "    <color>%08x</color>\n", color); // AABBGGRR survey color
        pw.format(Locale.US, "    <width>2</width>\n");
        pw.format(Locale.US, "  </LineStyle>\n");
        // pw.format(Locale.US, "  <LabelStyle>\n");
        // pw.format(Locale.US, "     <color>ff0000ff</color>\n"); // AABBGGRR
        // pw.format(Locale.US, "     <colorMode>normal</colorMode>\n");
        // pw.format(Locale.US, "     <scale>1.0</scale>\n");
        // pw.format(Locale.US, "  </LabelStyle>\n");
        pw.format(Locale.US, "</Style>\n");
      }

      pw.format(Locale.US, "<Style id=\"splay\">\n");
      pw.format(Locale.US, "  <LineStyle>\n");
      pw.format(Locale.US, "    <color>ff66cccc</color>\n"); // AABBGGRR yellow
      pw.format(Locale.US, "    <width>1</width>\n");
      pw.format(Locale.US, "  </LineStyle>\n");
      // pw.format(Locale.US, "  <LabelStyle>\n");
      // pw.format(Locale.US, "     <color>ff66cccc</color>\n"); // AABBGGRR 
      // pw.format(Locale.US, "     <colorMode>normal</colorMode>\n");
      // pw.format(Locale.US, "     <scale>0.5</scale>\n");
      // pw.format(Locale.US, "  </LabelStyle>\n");
      pw.format(Locale.US, "</Style>\n");

      pw.format(Locale.US, "<Style id=\"station\">\n");
      // pw.format(Locale.US, "  <IconStyle><Icon></Icon></IconStyle>\n"); // TODO
      pw.format(Locale.US, "  <LabelStyle>\n");
      pw.format(Locale.US, "     <color>ffff00ff</color>\n"); // AABBGGRR pink
      pw.format(Locale.US, "     <colorMode>normal</colorMode>\n");
      pw.format(Locale.US, "     <scale>1.0</scale>\n");
      pw.format(Locale.US, "  </LabelStyle>\n");
      pw.format(Locale.US, "  <LineStyle>\n");
      pw.format(Locale.US, "    <color>ffff00ff</color>\n"); // AABBGGRR
      pw.format(Locale.US, "    <width>1</width>\n");
      pw.format(Locale.US, "  </LineStyle>\n");
      pw.format(Locale.US, "</Style>\n");
      
      pw.format(Locale.US, "<Style id=\"wall\">\n");
      pw.format(Locale.US, "  <IconStyle><Icon></Icon></IconStyle>\n");
      // pw.format(Locale.US, "  <LineStyle>\n");
      // pw.format(Locale.US, "    <color>9900cc66</color>\n"); // AABBGGRR green
      // pw.format(Locale.US, "    <width>0.5</width>\n");
      // pw.format(Locale.US, "  </LineStyle>\n");
      pw.format(Locale.US, "  <PolyStyle>\n");
      pw.format(Locale.US, "    <color>9900cc66</color>\n"); // AABBGGRR
      pw.format(Locale.US, "    <colorMode>normal</colorMode>\n"); 
      pw.format(Locale.US, "    <fill>1</fill>\n"); 
      pw.format(Locale.US, "    <outline>1</outline>\n"); 
      pw.format(Locale.US, "  </PolyStyle>\n");
      pw.format(Locale.US, "</Style>\n");

      pw.format(Locale.US, "<Style id=\"border\">\n");
      pw.format(Locale.US, "  <IconStyle><Icon></Icon></IconStyle>\n");
      pw.format(Locale.US, "  <LineStyle>\n");
      pw.format(Locale.US, "    <color>9900ff99</color>\n"); // AABBGGRR
      pw.format(Locale.US, "    <width>1</width>\n");
      pw.format(Locale.US, "  </LineStyle>\n");
      // pw.format(Locale.US, "  <PolyStyle>\n");
      // pw.format(Locale.US, "    <color>9900ff99</color>\n"); // AABBGGRR
      // pw.format(Locale.US, "    <colorMode>normal</colorMode>\n"); 
      // pw.format(Locale.US, "    <fill>1</fill>\n"); 
      // pw.format(Locale.US, "    <outline>1</outline>\n"); 
      // pw.format(Locale.US, "  </PolyStyle>\n");
      pw.format(Locale.US, "</Style>\n");

      for ( Cave3DSurvey survey : surveys ) {
        String survey_name = survey.getName();
        // int    sid  = survey.getId();
        pw.format(Locale.US, "<Folder>\n");
        pw.format(Locale.US, "  <name>%s</name>\n", survey_name );
        if ( do_station ) {
          List< Cave3DStation > stations = survey.getStations();
          TDLog.v("3D-KML stations " + stations.size() );
          pw.format(Locale.US, "<Folder>\n");
          pw.format(Locale.US, "  <name>stations</name>\n" );
          // pw.format(Locale.US, "  <MultiGeometry>\n");
          for ( Cave3DStation st : stations ) {
            double e = getE( st );
            double n = getN( st );
            double z = getZ( st );
            pw.format(Locale.US, "<Placemark>\n");
            pw.format(Locale.US, "  <name>%s</name>\n", st.getFullName() );
            pw.format(Locale.US, "  <styleUrl>#station</styleUrl>\n");
            pw.format(Locale.US, "  <Point id=\"%s\">\n", st.getFullName() );
            pw.format(Locale.US, "    <coordinates>%.8f,%.8f,%.8f</coordinates>\n", e, n, z );
            pw.format(Locale.US, "  </Point>\n");
            pw.format(Locale.US, "</Placemark>\n");
          }
          // pw.format(Locale.US, "  </MultiGeometry>\n");
          pw.format(Locale.US, "</Folder>\n");
        // } else {
        //   TDLog.v("3D kml no stations ");
        }

        pw.format(Locale.US, "<Placemark>\n");
        pw.format(Locale.US, "  <name>centerline</name>\n" );
        pw.format(Locale.US, "  <styleUrl>#%s</styleUrl>\n", survey_name);
        pw.format(Locale.US, "  <MultiGeometry>\n");
        pw.format(Locale.US, "    <altitudeMode>absolute</altitudeMode>\n");
        List< Cave3DShot > survey_shots = survey.getShots();
        for ( Cave3DShot sh : survey_shots ) {
          // if ( sh.mSurveyId != sid ) continue;
          Cave3DStation sf = sh.from_station;
          Cave3DStation st = sh.to_station;
          if ( sf == null || st == null ) continue;
          double ef = getE( sf );
          double nf = getN( sf );
          double zf = getZ( sf );
          double et = getE( st );
          double nt = getN( st );
          double zt = getZ( st );
          pw.format(Locale.US, "    <LineString id=\"%s-%s\"> <coordinates>\n", sf.getFullName(), st.getFullName() );
          // pw.format(Locale.US, "      <tessellate>1</tessellate>\n"); //   breaks the line up in small chunks
          // pw.format(Locale.US, "      <extrude>1</extrude>\n"); // extends the line down to the ground
          pw.format(Locale.US, "        %.8f,%.8f,%.8f %.8f,%.8f,%.8f\n", ef, nf, zf, et, nt, zt );
          pw.format(Locale.US, "    </coordinates> </LineString>\n");
        }
        pw.format(Locale.US, "  </MultiGeometry>\n");
        pw.format(Locale.US, "</Placemark>\n");

        if ( do_splays ) {
          List< Cave3DShot > splays = survey.getSplays();
          pw.format(Locale.US, "<Placemark>\n");
          pw.format(Locale.US, "  <name>splays</name>\n" );
          pw.format(Locale.US, "  <styleUrl>#splay</styleUrl>\n");
          pw.format(Locale.US, "  <MultiGeometry>\n");
          pw.format(Locale.US, "    <altitudeMode>absolute</altitudeMode>\n");
          for ( Cave3DShot sp : splays ) {
            Cave3DStation sf = sp.from_station;
            if ( sf == null ) continue;
            Vector3D v = sf.sum( sp.toVector3D() );
            double ef = getE( sf );
            double nf = getN( sf );
            double zf = getZ( sf );
            double et = getE( v );
            double nt = getN( v );
            double zt = getZ( v );
            pw.format(Locale.US, "    <LineString> <coordinates>\n" );
            // pw.format(Locale.US, "      <tessellate>1</tessellate>\n"); //   breaks the line up in small chunks
            // pw.format(Locale.US, "      <extrude>1</extrude>\n"); // extends the line down to the ground
            pw.format(Locale.US, "        %.8f,%.8f,%.8f %.8f,%.8f,%.8f\n", ef, nf, zf, et, nt, zt );
            pw.format(Locale.US, "    </coordinates> </LineString>\n");
          }
          pw.format(Locale.US, "  </MultiGeometry>\n");
          pw.format(Locale.US, "</Placemark>\n");
        // } else {
        //   TDLog.v("3D kml no splay ");
        }
        pw.format(Locale.US, "</Folder>\n");
      }

      if ( do_walls ) {
        pw.format(Locale.US, "<Placemark>\n");
        pw.format(Locale.US, "  <name>walls</name>\n" );
        pw.format(Locale.US, "  <styleUrl>#wall</styleUrl>\n");
        pw.format(Locale.US, "  <altitudeMode>absolute</altitudeMode>\n");
        pw.format(Locale.US, "  <MultiGeometry>\n");
        for ( CWFacet facet : mFacets ) {
          double e1 = getE( facet.v1 );
          double n1 = getN( facet.v1 );
          double z1 = getZ( facet.v1 );
          double e2 = getE( facet.v2 );
          double n2 = getN( facet.v2 );
          double z2 = getZ( facet.v2 );
          double e3 = getE( facet.v3 );
          double n3 = getN( facet.v3 );
          double z3 = getZ( facet.v3 );
          pw.format(Locale.US, "    <Polygon>\n");
          pw.format(Locale.US, "      <outerBoundaryIs> <LinearRing> <coordinates>\n");
          pw.format(Locale.US, "             %.8f,%.8f,%.3f\n", e1,n1,z1);
          pw.format(Locale.US, "             %.8f,%.8f,%.3f\n", e2,n2,z2);
          pw.format(Locale.US, "             %.8f,%.8f,%.3f\n", e3,n3,z3);
          pw.format(Locale.US, "             %.8f,%.8f,%.3f\n", e1,n1,z1); // repeat first point
          pw.format(Locale.US, "      </coordinates> </LinearRing> </outerBoundaryIs>\n");
          pw.format(Locale.US, "    </Polygon>\n");
        }
        if ( mTriangles != null ) {
          for ( Triangle3D t : mTriangles ) {
            double e0 = getE( t.vertex[t.size-1] );
            double n0 = getN( t.vertex[t.size-1] );
            double z0 = getZ( t.vertex[t.size-1] );
            pw.format(Locale.US, "    <Polygon>\n");
            pw.format(Locale.US, "      <outerBoundaryIs> <LinearRing> <coordinates>\n");
            pw.format(Locale.US, "             %.8f,%.8f,%.3f\n", e0,n0,z0); // last point
            for ( int k = 0; k < t.size; ++k ) {
              double e1 = getE( t.vertex[k] );
              double n1 = getN( t.vertex[k] );
              double z1 = getZ( t.vertex[k] );
              pw.format(Locale.US, "             %.8f,%.8f,%.3f\n", e1,n1,z1);
            }
            pw.format(Locale.US, "      </coordinates> </LinearRing> </outerBoundaryIs>\n");
            pw.format(Locale.US, "    </Polygon>\n");
          }
        }
        pw.format(Locale.US, "  </MultiGeometry>\n");
        pw.format(Locale.US, "</Placemark>\n");

        pw.format(Locale.US, "<Placemark>\n");
        pw.format(Locale.US, "  <name>walls lines</name>\n" );
        pw.format(Locale.US, "  <styleUrl>#border</styleUrl>\n");
        pw.format(Locale.US, "  <altitudeMode>absolute</altitudeMode>\n");
        pw.format(Locale.US, "  <MultiGeometry>\n");
        for ( CWFacet facet : mFacets ) {
          double e1 = getE( facet.v1 );
          double n1 = getN( facet.v1 );
          double z1 = getZ( facet.v1 );
          double e2 = getE( facet.v2 );
          double n2 = getN( facet.v2 );
          double z2 = getZ( facet.v2 );
          double e3 = getE( facet.v3 );
          double n3 = getN( facet.v3 );
          double z3 = getZ( facet.v3 );
          pw.format(Locale.US, "    <LineString> <coordinates>\n");
          pw.format(Locale.US, "             %.8f,%.8f,%.3f %.8f,%.8f,%.3f", e1,n1,z1, e2,n2,z2 );
          pw.format(Locale.US, "    </coordinates> </LineString>\n");
          pw.format(Locale.US, "    <LineString> <coordinates>\n");
          pw.format(Locale.US, "             %.8f,%.8f,%.3f %.8f,%.8f,%.3f", e2,n2,z2, e3,n3,z3 );
          pw.format(Locale.US, "    </coordinates> </LineString>\n");
          pw.format(Locale.US, "    <LineString> <coordinates>\n");
          pw.format(Locale.US, "             %.8f,%.8f,%.3f %.8f,%.8f,%.3f", e3,n3,z3, e1,n1,z1 );
          pw.format(Locale.US, "    </coordinates> </LineString>\n");
        }
        if ( mTriangles != null ) {
          for ( Triangle3D t : mTriangles ) {
            double e0 = getE( t.vertex[t.size-1] );
            double n0 = getN( t.vertex[t.size-1] );
            double z0 = getZ( t.vertex[t.size-1] );
            for ( int k = 0; k < t.size; ++k ) { // border (e0,n0,z0) set at last point
              double e1 = getE( t.vertex[k] );
              double n1 = getN( t.vertex[k] );
              double z1 = getZ( t.vertex[k] );
              pw.format(Locale.US, "    <LineString> <coordinates>\n");
              pw.format(Locale.US, "             %.8f,%.8f,%.3f %.8f,%.8f,%.3f", e0,n0,z0, e1,n1,z1 );
              pw.format(Locale.US, "    </coordinates> </LineString>\n");
              e0 = e1;
              n0 = n1;
              z0 = z1;
            }
          }
        }
        pw.format(Locale.US, "  </MultiGeometry>\n");
        pw.format(Locale.US, "</Placemark>\n");
      } else {
        TDLog.v("3D kml no walls");
      }

      pw.format(Locale.US, "</Document>\n");
      pw.format(Locale.US, "</kml>\n");
      osw.flush();
      osw.close();
      return true;
    } catch ( IOException e ) {
      TDLog.Error( "KML IO error " + e.getMessage() );
      return false;
    }
  }

}

