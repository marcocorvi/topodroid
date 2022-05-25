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
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Cave3DFix;
import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Geodetic;
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

public class ExportKML
{
  ArrayList<CWFacet> mFacets;
  double lat, lng, asl;
  double s_radius, e_radius;
  Cave3DStation zero;
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

  private boolean getGeolocalizedData( TglParser data, double decl, double asl_factor )
  {
    // TDLog.v( "KML get geoloc. data. Decl " + decl );
    List< Cave3DFix > fixes = data.getFixes();
    if ( fixes.size() == 0 ) {
      // TDLog.v( "KML no geolocalization");
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
      // TDLog.v( "KML no geolocalized origin");
      return false;
    }

    // origin has coordinates ( e, n, z ) these are assumed lat-long
    // altitude is assumed wgs84
    lat = origin.latitude;
    lng = origin.longitude;
    double alt = origin.altitude;
    asl = origin.z; // KML uses Geoid altitude (unless altitudeMode is set)
    // TDLog.v( "KML origin " + lat + " N " + lng + " E " + asl );

    s_radius = 1.0 / Geodetic.meridianRadiusExact( lat, alt );
    e_radius = 1.0 / Geodetic.parallelRadiusExact( lat, alt );

    return true;
  }

  public boolean exportASCII( BufferedWriter osw, TglParser data, boolean do_splays, boolean do_walls, boolean do_station )
  {
    String name = "TopoGL";
    boolean ret = true;
    if ( data == null ) return false;

    // TDLog.v( "KML export " + filename );
    if ( ! getGeolocalizedData( data, 0.0f, 1.0f ) ) { // FIXME declination 0.0f
      TDLog.v( "KML no geolocalized station");
      return false;
    }

    List< Cave3DStation> stations = data.getStations();
    List< Cave3DShot>    shots    = data.getShots();
    List< Cave3DShot>    splays   = data.getSplays();

    // now write the KML
    try {
      PrintWriter pw = new PrintWriter( osw );

      pw.format(Locale.US, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      pw.format(Locale.US, "<kml xmlnx=\"http://www.opengis.net/kml/2.2\">\n");
      pw.format(Locale.US, "<Document>\n");

      pw.format(Locale.US, "<name>%s</name>\n", name );
      pw.format(Locale.US, "<description>%s</description>\n", name );

      pw.format(Locale.US, "<Style id=\"centerline\">\n");
      pw.format(Locale.US, "  <LineStyle>\n");
      pw.format(Locale.US, "    <color>ff0000ff</color>\n"); // AABBGGRR
      pw.format(Locale.US, "    <width>2</width>\n");
      pw.format(Locale.US, "  </LineStyle>\n");
      pw.format(Locale.US, "  <LabelStyle>\n");
      pw.format(Locale.US, "     <color>ff0000ff</color>\n"); // AABBGGRR
      pw.format(Locale.US, "     <colorMode>normal</colorMode>\n");
      pw.format(Locale.US, "     <scale>1.0</scale>\n");
      pw.format(Locale.US, "  </LabelStyle>\n");
      pw.format(Locale.US, "</Style>\n");

      pw.format(Locale.US, "<Style id=\"splay\">\n");
      pw.format(Locale.US, "  <LineStyle>\n");
      pw.format(Locale.US, "    <color>ff66cccc</color>\n"); // AABBGGRR
      pw.format(Locale.US, "    <width>1</width>\n");
      pw.format(Locale.US, "  </LineStyle>\n");
      pw.format(Locale.US, "  <LabelStyle>\n");
      pw.format(Locale.US, "     <color>ff66cccc</color>\n"); // AABBGGRR
      pw.format(Locale.US, "     <colorMode>normal</colorMode>\n");
      pw.format(Locale.US, "     <scale>0.5</scale>\n");
      pw.format(Locale.US, "  </LabelStyle>\n");
      pw.format(Locale.US, "</Style>\n");

      pw.format(Locale.US, "<Style id=\"station\">\n");
      pw.format(Locale.US, "  <IconStyle><Icon></Icon></IconStyle>\n");
      pw.format(Locale.US, "  <LabelStyle>\n");
      pw.format(Locale.US, "     <color>ffff00ff</color>\n"); // AABBGGRR
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
      pw.format(Locale.US, "  <LineStyle>\n");
      pw.format(Locale.US, "    <color>9900ccff</color>\n"); // AABBGGRR
      pw.format(Locale.US, "    <width>1</width>\n");
      pw.format(Locale.US, "  </LineStyle>\n");
      pw.format(Locale.US, "  <PolyStyle>\n");
      // pw.format(Locale.US, "    <color>9900ccff</color>\n"); // AABBGGRR
      pw.format(Locale.US, "    <color>9900ccff</color>\n"); // AABBGGRR
      pw.format(Locale.US, "    <colorMode>normal</colorMode>\n"); 
      pw.format(Locale.US, "    <fill>1</fill>\n"); 
      pw.format(Locale.US, "    <outline>1</outline>\n"); 
      pw.format(Locale.US, "  </PolyStyle>\n");
      pw.format(Locale.US, "</Style>\n");

      if ( do_station ) {
        for ( Cave3DStation st : stations ) {
          double e = lng + (st.x - zero.x) * e_radius;
          double n = lat + (st.y - zero.y) * s_radius;
          double z = asl + (st.z - zero.z);
          pw.format(Locale.US, "<Placemark>\n");
          pw.format(Locale.US, "  <name>%s</name>\n", st.getFullName() );
          pw.format(Locale.US, "  <styleUrl>#station</styleUrl>\n");
          pw.format(Locale.US, "  <MultiGeometry>\n");
            pw.format(Locale.US, "  <Point id=\"%s\">\n", st.getFullName() );
            pw.format(Locale.US, "    <coordinates>%f,%f,%f</coordinates>\n", e, n, z );
            pw.format(Locale.US, "  </Point>\n");
          pw.format(Locale.US, "  </MultiGeometry>\n");
          pw.format(Locale.US, "</Placemark>\n");
        }
      }

      pw.format(Locale.US, "<Placemark>\n");
      pw.format(Locale.US, "  <name>centerline</name>\n" );
      pw.format(Locale.US, "  <styleUrl>#centerline</styleUrl>\n");
      pw.format(Locale.US, "  <MultiGeometry>\n");
      pw.format(Locale.US, "    <altitudeMode>absolute</altitudeMode>\n");
      for ( Cave3DShot sh : shots ) {
        Cave3DStation sf = sh.from_station;
        Cave3DStation st = sh.to_station;
        if ( sf == null || st == null ) continue;
        double ef = lng + (sf.x - zero.x) * e_radius;
        double nf = lat + (sf.y - zero.y) * s_radius;
        double zf = asl + (sf.z - zero.z);
        double et = lng + (st.x - zero.x) * e_radius;
        double nt = lat + (st.y - zero.y) * s_radius;
        double zt = asl + (st.z - zero.z);
        pw.format(Locale.US, "    <LineString id=\"%s-%s\"> <coordinates>\n", sf.getFullName(), st.getFullName() );
        // pw.format(Locale.US, "      <tessellate>1</tessellate>\n"); //   breaks the line up in small chunks
        // pw.format(Locale.US, "      <extrude>1</extrude>\n"); // extends the line down to the ground
        pw.format(Locale.US, "        %f,%f,%f %f,%f,%f\n", ef, nf, zf, et, nt, zt );
        pw.format(Locale.US, "    </coordinates> </LineString>\n");
      }
      pw.format(Locale.US, "  </MultiGeometry>\n");
      pw.format(Locale.US, "</Placemark>\n");

      if ( do_splays ) {
        pw.format(Locale.US, "<Placemark>\n");
        pw.format(Locale.US, "  <name>splays</name>\n" );
        pw.format(Locale.US, "  <styleUrl>#splay</styleUrl>\n");
        pw.format(Locale.US, "  <MultiGeometry>\n");
        pw.format(Locale.US, "    <altitudeMode>absolute</altitudeMode>\n");
        for ( Cave3DShot sp : splays ) {
          Cave3DStation sf = sp.from_station;
          if ( sf == null ) continue;
          Vector3D v = sp.toVector3D();
          double ef = lng + (sf.x - zero.x) * e_radius;
          double nf = lat + (sf.y - zero.y) * s_radius;
          double zf = asl + (sf.z - zero.z);
          double et = lng + (sf.x + v.x - zero.x) * e_radius;
          double nt = lat + (sf.y + v.y - zero.y) * s_radius;
          double zt = asl + (sf.z + v.z - zero.z);
          pw.format(Locale.US, "    <LineString> <coordinates>\n" );
          // pw.format(Locale.US, "      <tessellate>1</tessellate>\n"); //   breaks the line up in small chunks
          // pw.format(Locale.US, "      <extrude>1</extrude>\n"); // extends the line down to the ground
          pw.format(Locale.US, "        %f,%f,%f %f,%f,%f\n", ef, nf, zf, et, nt, zt );
          pw.format(Locale.US, "    </coordinates> </LineString>\n");
        }
        pw.format(Locale.US, "  </MultiGeometry>\n");
        pw.format(Locale.US, "</Placemark>\n");
      }

      if ( do_walls ) {
        pw.format(Locale.US, "<Placemark>\n");
        pw.format(Locale.US, "  <name>walls</name>\n" );
        pw.format(Locale.US, "  <styleUrl>#wall</styleUrl>\n");
        pw.format(Locale.US, "  <altitudeMode>absolute</altitudeMode>\n");
        pw.format(Locale.US, "  <MultiGeometry>\n");
        for ( CWFacet facet : mFacets ) {
          double e1 = lng + (facet.v1.x - zero.x) * e_radius;
          double n1 = lat + (facet.v1.y - zero.y) * s_radius;
          double z1 = asl + (facet.v1.z - zero.z);
          double e2 = lng + (facet.v2.x - zero.x) * e_radius;
          double n2 = lat + (facet.v2.y - zero.y) * s_radius;
          double z2 = asl + (facet.v2.z - zero.z);
          double e3 = lng + (facet.v3.x - zero.x) * e_radius;
          double n3 = lat + (facet.v3.y - zero.y) * s_radius;
          double z3 = asl + (facet.v3.z - zero.z);
          pw.format(Locale.US, "    <Polygon>\n");
          pw.format(Locale.US, "      <outerBoundaryIs> <LinearRing> <coordinates>\n");
          pw.format(Locale.US, "             %f,%f,%.3f\n", e1,n1,z1);
          pw.format(Locale.US, "             %f,%f,%.3f\n", e2,n2,z2);
          pw.format(Locale.US, "             %f,%f,%.3f\n", e3,n3,z3);
          pw.format(Locale.US, "      </coordinates> </LinearRing> </outerBoundaryIs>\n");
          pw.format(Locale.US, "    </Polygon>\n");
          pw.format(Locale.US, "    <LineString> <coordinates>\n");
          pw.format(Locale.US, "             %f,%f,%.3f %f,%f,%.3f", e1,n1,z1, e2,n2,z2 );
          pw.format(Locale.US, "    </coordinates> </LineString>\n");
          pw.format(Locale.US, "    <LineString> <coordinates>\n");
          pw.format(Locale.US, "             %f,%f,%.3f %f,%f,%.3f", e2,n2,z2, e3,n3,z3 );
          pw.format(Locale.US, "    </coordinates> </LineString>\n");
          pw.format(Locale.US, "    <LineString> <coordinates>\n");
          pw.format(Locale.US, "             %f,%f,%.3f %f,%f,%.3f", e3,n3,z3, e1,n1,z1 );
          pw.format(Locale.US, "    </coordinates> </LineString>\n");
        }
        if ( mTriangles != null ) {
          for ( Triangle3D t : mTriangles ) {
            pw.format(Locale.US, "    <Polygon>\n");
            pw.format(Locale.US, "      <outerBoundaryIs> <LinearRing> <coordinates>\n");
            for ( int k = 0; k < t.size; ++k ) {
              double e1 = lng + (t.vertex[k].x - zero.x) * e_radius;
              double n1 = lat + (t.vertex[k].y - zero.y) * s_radius;
              double z1 = asl + (t.vertex[k].z - zero.z);
              pw.format(Locale.US, "             %f,%f,%.3f\n", e1,n1,z1);
            }
            pw.format(Locale.US, "      </coordinates> </LinearRing> </outerBoundaryIs>\n");
            pw.format(Locale.US, "    </Polygon>\n");
            double e0 = lng + (t.vertex[t.size-1].x - zero.x) * e_radius;
            double n0 = lat + (t.vertex[t.size-1].y - zero.y) * s_radius;
            double z0 = asl + (t.vertex[t.size-1].z - zero.z);
            for ( int k = 0; k < t.size; ++k ) {
              double e1 = lng + (t.vertex[k].x - zero.x) * e_radius;
              double n1 = lat + (t.vertex[k].y - zero.y) * s_radius;
              double z1 = asl + (t.vertex[k].z - zero.z);
              pw.format(Locale.US, "    <LineString> <coordinates>\n");
              pw.format(Locale.US, "             %f,%f,%.3f %f,%f,%.3f", e0,n0,z0, e1,n1,z1 );
              pw.format(Locale.US, "    </coordinates> </LineString>\n");
              e0 = e1;
              n0 = n1;
              z0 = z1;
            }
          }
        }
        pw.format(Locale.US, "  </MultiGeometry>\n");
        pw.format(Locale.US, "</Placemark>\n");
      }

      pw.format(Locale.US, "</Document>\n");
      pw.format(Locale.US, "</kml>\n");
      osw.flush();
      osw.close();
      return true;
    } catch ( IOException e ) {
      TDLog.v( "KML IO error " + e.getMessage() );
      return false;
    }
  }

}

