/** @file TopoDroidExporter.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief numerical utilities
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.io.FileReader;
import java.io.BufferedReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;

class TopoDroidExporter
{

  // final static int EXPORT_THERION    = 0;
  // final static int EXPORT_COMPASS    = 1;
  // final static int EXPORT_CSURVEY    = 2;
  // final static int EXPORT_POCKETTOPO = 3;
  // final static int EXPORT_SURVEX     = 4;
  // final static int EXPORT_VISUALTOPO = 5;
  // final static int EXPORT_WALLS      = 6;
  // final static int EXPORT_CSV        = 10;
  // final static int EXPORT_DXF        = 11;
  // final static int EXPORT_PNG        = 12;
  // final static int EXPORT_SVG        = 13;
  // final static int EXPORT_KML        = 14;
  // final static int EXPORT_ZIP        = 20;


  // =======================================================================
  // CSURVEY EXPORT cSurvey

  static String[] therion_extend = { "left", "vertical", "right", "ignore" };
  static String   therion_flags_duplicate     = "   flags duplicate\n";
  static String   therion_flags_not_duplicate = "   flags not duplicate\n";
  static String   therion_flags_surface       = "   flags surface\n";
  static String   therion_flags_not_surface   = "   flags not surface\n";


  static private void exportEmptyCsxSketch( PrintWriter pw )
  {
     pw.format("    <layers>\n");
     pw.format("      <layer name=\"Base\" type=\"0\">\n");
     pw.format("         <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Soil\" type=\"1\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Water and floor morphologies\" type=\"2\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Rocks and concretions\" type=\"3\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Ceiling morphologies\" type=\"4\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Borders\" type=\"5\">\n");
     pw.format("        <items>\n");
     pw.format("        </items>\n");
     pw.format("      </layer>\n");
     pw.format("      <layer name=\"Signs\" type=\"6\">\n");
     pw.format("        <items />\n");
     pw.format("      </layer>\n");
     pw.format("    </layers>\n");
     pw.format("    <plot />\n");
  }

  static private void writeCsxLeg( PrintWriter pw, AverageLeg leg )
  {
    pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
      leg.length(), leg.bearing(), leg.clino()
    );
    leg.reset();
  }

  static String exportSurveyAsCsx( long sid, DataHelper data, SurveyInfo info, DrawingActivity sketch, String origin, String filename )
  {
    String cave = info.name.toUpperCase();

    String prefix = "";
    if ( TDSetting.mExportStationsPrefix ) prefix = cave + "-";

    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    List< PlotInfo > plots  = data.selectAllPlots( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );


      pw.format("<csurvey version=\"1.04\" id=\"\">\n");
      pw.format("<!-- %s created by TopoDroid v %s -->\n", TopoDroidUtil.getDateString("yyyy-MM-dd"), TopoDroidApp.VERSION );

// ++++++++++++++++ PROPERTIES
      // FIXME origin = origin of Num
      pw.format("  <properties id=\"\" name=\"\" origin=\"%s%s\" ", prefix, origin );
      // pw.format(      "name=\"\" description=\"\" club=\"\" team=\"\" ");
      pw.format(      "calculatemode=\"1\" calculatetype=\"2\" " );
      pw.format(      "ringcorrectionmode=\"2\" nordcorrectionmode=\"0\" inversionmode=\"1\" ");
      pw.format(      "designwarpingmode=\"1\" bindcrosssection=\"1\">\n");
      

   // ============== SESSIONS
      pw.format("    <sessions>\n");
      pw.format("      <session date=\"%s\" ", info.date); // FIXME yyyy-mm-dd
      pw.format(         "description=\"%s\" ", cave ); // title
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format(" team=\"%s\" ", info.team );
      }
      pw.format(Locale.ENGLISH, "nordtype=\"0\" manualdeclination=\"0\" declination=\"%.4f\" ", info.declination );
      pw.format(">\n");
      pw.format("      </session>\n");
      pw.format("    </sessions>\n");

   // ============== CAVE INFOS and BRANCHES
      pw.format("    <caveinfos>\n");
      pw.format("      <caveinfo name=\"%s\"", cave );
      // pw.format( " color=\"\"");
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format( " comment=\"%s\"\n", info.comment );
      }
      pw.format(" >\n");
      pw.format("        <branches />\n");
      pw.format("      </caveinfo>\n");
      pw.format("    </caveinfos>\n");

   // ============== ORIGIN
      if ( origin != null )  {
        pw.format("    <gps enabled=\"0\" refpointonorigin=\"%s%s\" geo=\"WGS84\" format=\"\" sendtotherion=\"0\" />\n",
                  prefix,  origin );
      }

      pw.format("  </properties>\n");

// ++++++++++++++++ SHOTS
      pw.format("  <segments>\n");

      // optional attrs of "segment": id cave branch session

      int cntSplay = 0;     // splay counter (index)
      long extend = 0;      // current extend
      boolean dup = false;  // duplicate
      boolean sur = false;  // surface
      // boolean bck = false;  // backshot
      String com = null;    // comment
      String f="", t="";          // from to stations
      DistoXDBlock ref_item = null;
      // float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;
      // int n = 0;
      AverageLeg leg = new AverageLeg();

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s%s\" to=\"%s%s\"", cave, prefix, f, prefix, t );

              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur /* || bck */ ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
		// if ( bck ) { pw.format(" backshot=\"1\"");   bck = false; }
              }
              // pw.format(" planshowsplayborder=\"1\" profileshowsplayborder=\"1\" ");
              writeCsxLeg( pw, leg );
              pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\"");
              if ( com != null && com.length() > 0 ) {
                pw.format(" note=\"%s\"", com.replaceAll("\"", "") );
                com = null;
              }
              pw.format(" />\n");
              ref_item = null; 
            }

            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            pw.format("<segment id=\"\" cave=\"%s\" from=\"%s%s(%d)\" to=\"%s%s\"", cave, prefix, to, cntSplay, prefix, to );
            ++ cntSplay;
            pw.format(" splay=\"1\" exclude=\"1\"");
            if ( extend == -1 ) pw.format(" direction=\"1\"");
            pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
               item.mLength, item.mBearing, item.mClino );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format(" note=\"%s\"", item.mComment.replaceAll("\"", "") );
            }
            pw.format(" />\n");
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // ONLY FROM STATION : splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s%s\" to=\"%s%s\"", cave, prefix, f, prefix, t );
              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur /* || bck */ ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
                // if ( bck ) { pw.format(" backshot=\"1\"");   bck = false; }
              }
              // pw.format(" planshowsplayborder=\"1\" profileshowsplayborder=\"1\" ");
              writeCsxLeg( pw, leg );
              pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\"");
              if ( com != null && com.length() > 0 ) {
                pw.format(" note=\"%s\"", com.replaceAll("\"", "") );
                com = null;
              }
              pw.format(" />\n");
              ref_item = null; 
            }

            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            pw.format("<segment id=\"\" cave=\"%s\" from=\"%s%s\" to=\"%s%s(%d)\"", cave, prefix, from, prefix, from, cntSplay );
            ++cntSplay;
            pw.format(" splay=\"1\" exclude=\"1\"");
            if ( extend == -1 ) pw.format(" direction=\"1\"");
            pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
               item.mLength, item.mBearing, item.mClino );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format(" note=\"%s\"", item.mComment.replaceAll("\"", "") );
            }
            pw.format(" />\n");
          } else { // BOTH FROM AND TO STATIONS
            if ( leg.mCnt > 0 && ref_item != null ) {
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s%s\" to=\"%s%s\" ", cave, prefix, f, prefix, t );
              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur /* || bck */ ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
                // if ( bck ) { pw.format(" backshot=\"1\"");   bck = false; }
              }
              writeCsxLeg( pw, leg );
              pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
              if ( com != null && com.length() > 0 ) {
                pw.format(" note=\"%s\"", com.replaceAll("\"", "") );
                com = null;
              }
              pw.format(" />\n");
            }
            ref_item = item;
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              dup = true;
            } else if ( item.mFlag == DistoXDBlock.BLOCK_SURFACE ) {
              sur = true;
            // } else if ( item.mFlag == DistoXDBlock.BLOCK_BACKSHOT ) {
            //   bck = true;
            }
            f = from;
            t = to;
            leg.set( item.mLength, item.mBearing, item.mClino );
            com = item.mComment;
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        pw.format("<segment id=\"\" cave=\"%s\" from=\"%s%s\" to=\"%s%s\" ", cave, prefix, f, prefix, t );
        if ( extend == -1 ) pw.format(" direction=\"1\"");
        if ( dup || sur /* || bck */ ) {
           pw.format(" exclude=\"1\"");
           if ( dup ) { pw.format(" duplicate=\"1\""); /* dup = false; */ }
           if ( sur ) { pw.format(" surface=\"1\"");   /* sur = false; */ }
           // if ( bck ) { pw.format(" backshot=\"1\"");  /* bck = false; */ }
        }
        writeCsxLeg( pw, leg );
        pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
        if ( com != null && com.length() > 0 ) {
          pw.format(" note=\"%s\"", com.replaceAll("\"", "") );
          // com = null;
        }
        pw.format(" />\n");
      }
      pw.format("  </segments>\n");

      // ============= TRIG POINTS
      pw.format("  <trigpoints>\n");
      if ( fixed.size() > 0 ) {
        for ( FixedInfo fix : fixed ) {
          pw.format("     <trigpoint name=\"%s\" labelsymbol=\"0\" >\n", fix.name );
          pw.format(Locale.ENGLISH, "       <coordinate latv=\"%.7f\" longv=\"%.7f\" altv=\"%.2f\" lat=\"%.7f N\" long=\"%.7f E\" format=\"dd.ddddddd N\" alt=\"%.2f\" />\n",
             fix.lat, fix.lng, fix.alt, fix.lat, fix.lng, fix.alt );
          pw.format("     </trigpoint>\n");
        }
      }
      pw.format("  </trigpoints>\n");

      // ============= SKETCHES
      if ( sketch != null ) {
        sketch.exportAsCsx( pw );
      } else {
        pw.format("  <plan>\n");
        exportEmptyCsxSketch( pw );
        pw.format("  </plan>\n");
        pw.format("  <profile>\n");
        exportEmptyCsxSketch( pw );
        pw.format("  </profile>\n");
      }
      pw.format("</csurvey>\n");

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed cSurvey export: " + e.getMessage() );
      return null;
    }
  }

  // =======================================================================
  // KML export

  static float EARTH_RADIUS1 = (float)(6378137 * Math.PI / 180.0f); // semimajor axis [m]
  static float EARTH_RADIUS2 = (float)(6356752 * Math.PI / 180.0f);

  static private DistoXNum getGeolocalizedData( long sid, DataHelper data, float alt_factor )
  {
    List< FixedInfo > fixeds = data.selectAllFixed( sid, 0 );
    if ( fixeds.size() == 0 ) return null;

    DistoXNum num = null;
    FixedInfo origin = null;
    List<DistoXDBlock> shots_data = data.selectAllShots( sid, 0 );
    for ( FixedInfo fixed : fixeds ) {
      num = new DistoXNum( shots_data, fixed.name, null, null );
      if ( num.getShots().size() > 0 ) {
        origin = fixed;
        break;
      }
    }
    if ( origin == null || num == null ) return null;

    float lat = (float)origin.lat;
    float lng = (float)origin.lng;
    float alt = (float)origin.alt;
    float alat = TDMath.abs( lat );

    float s_radius = ((90 - alat) * EARTH_RADIUS1 + alat * EARTH_RADIUS2)/90;
    float e_radius = s_radius * TDMath.cosd( alat );

    s_radius = 1 / s_radius;
    e_radius = 1 / e_radius;

    for ( NumStation st : num.getStations() ) {
      st.s = lat - st.s * s_radius;
      st.e = lng + st.e * e_radius;
      st.v = (alt - st.v)*alt_factor;
    }
    return num;
  }

  static String exportSurveyAsKml( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    DistoXNum num = getGeolocalizedData( sid, data, 1.0f );
    if ( num == null ) return null;
    List<NumStation> stations = num.getStations();
    List<NumShot>    shots = num.getShots();
    // List<NumSplay>   splays = num.getSplays();

    // now write the KML
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      pw.format("<kml xmlnx=\"http://www.opengis.net/kml/2.2\">\n");
      pw.format("<Document>\n");

      pw.format("<name>%s</name>\n", info.name );
      pw.format("<description>%s - TopoDroid v %s</description>\n",  TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );

      pw.format("<Style id=\"centerline\">\n");
      pw.format("  <LineStyle>\n");
      pw.format("    <color>ff0000ff</color>\n"); // AABBGGRR
      pw.format("    <width>2</width>\n");
      pw.format("  </LineStyle>\n");
      pw.format("  <LabelStyle>\n");
      pw.format("     <color>ff0000ff</color>\n"); // AABBGGRR
      pw.format("     <colorMode>normal</colorMode>\n");
      pw.format("     <scale>1.0</scale>\n");
      pw.format("  </LabelStyle>\n");
      pw.format("</Style>\n");

      pw.format("<Style id=\"splay\">\n");
      pw.format("  <LineStyle>\n");
      pw.format("    <color>ffffff00</color>\n"); // AABBGGRR
      pw.format("    <width>1</width>\n");
      pw.format("  </LineStyle>\n");
      pw.format("  <LabelStyle>\n");
      pw.format("     <color>ffffff00</color>\n"); // AABBGGRR
      pw.format("     <colorMode>normal</colorMode>\n");
      pw.format("     <scale>0.5</scale>\n");
      pw.format("  </LabelStyle>\n");
      pw.format("</Style>\n");

      pw.format("<Style id=\"station\">\n");
      pw.format("  <IconStyle><Icon></Icon></IconStyle>\n");
      pw.format("  <LabelStyle>\n");
      pw.format("     <color>ffff00ff</color>\n"); // AABBGGRR
      pw.format("     <colorMode>normal</colorMode>\n");
      pw.format("     <scale>1.0</scale>\n");
      pw.format("  </LabelStyle>\n");
      pw.format("  <LineStyle>\n");
      pw.format("    <color>ffff00ff</color>\n"); // AABBGGRR
      pw.format("    <width>1</width>\n");
      pw.format("  </LineStyle>\n");
      pw.format("</Style>\n");
      
      for ( NumStation st : stations ) {
        pw.format("<Placemark>\n");
        pw.format("  <name>%s</name>\n", st.name );
        pw.format("  <styleUrl>#station</styleUrl>\n");
        pw.format("  <MultiGeometry>\n");
          pw.format("  <Point id=\"%s\">\n", st.name );
          pw.format(Locale.ENGLISH, "    <coordinates>%f,%f,%f</coordinates>\n", st.e, st.s, st.v );
          pw.format("  </Point>\n");
        pw.format("  </MultiGeometry>\n");
        pw.format("</Placemark>\n");
      }

      pw.format("<Placemark>\n");
      pw.format("  <name>centerline</name>\n" );
      pw.format("  <styleUrl>#centerline</styleUrl>\n");
      pw.format("  <MultiGeometry>\n");
      pw.format("    <altitudeMode>absolute</altitudeMode>\n");
      for ( NumShot sh : shots ) {
        NumStation from = sh.from;
        NumStation to   = sh.to;
        if ( from.mHasCoords && to.mHasCoords ) {
          pw.format("    <LineString id=\"%s-%s\">\n", from.name, to.name );
          // pw.format("      <tessellate>1</tessellate>\n"); //   breaks the line up in small chunks
          // pw.format("      <extrude>1</extrude>\n"); // extends the line down to the ground
          pw.format("      <coordinates>\n");
          pw.format(Locale.ENGLISH, "        %f,%f,%f\n", from.e, from.s, from.v );
          pw.format(Locale.ENGLISH, "        %f,%f,%f\n", to.e, to.s, to.v );
          pw.format("      </coordinates>\n");
          pw.format("    </LineString>\n");
        } else {
          // Log.v("DistoX", "missing coords " + from.name + " " + from.mHasCoords + " " + to.name + " " + to.mHasCoords );
        }
      }
      pw.format("  </MultiGeometry>\n");
      pw.format("</Placemark>\n");

      // pw.format("<Placemark>\n");
      // pw.format("  <name>splays</name>\n" );
      // pw.format("  <styleUrl>#splay</styleUrl>\n");
      // pw.format("  <MultiGeometry>\n");
      // pw.format("    <altitudeMode>absolute</altitudeMode>\n");
      // for ( NumSplay sp : splays ) {
      //   NumStation from = sp.from;
      //   pw.format("    <LineString id=\"%s-\">\n", from.name );
      //   // pw.format("      <tessellate>1</tessellate>\n"); //   breaks the line up in small chunks
      //   // pw.format("      <extrude>1</extrude>\n"); // extends the line down to the ground
      //   pw.format("      <coordinates>\n");
      //   pw.format(Locale.ENGLISH, "        %f,%f,%f\n", from.e, from.s, from.v );
      //   pw.format(Locale.ENGLISH, "        %f,%f,%f\n", to.e, to.s, to.v );
      //   pw.format("      </coordinates>\n");
      //   pw.format("    </LineString>\n");
      // }
      // pw.format("  </MultiGeometry>\n");
      // pw.format("</Placemark>\n");

      pw.format("</Document>\n");
      pw.format("</kml>\n");
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed cSurvey export: " + e.getMessage() );
      return null;
    }
  }

  static String exportSurveyAsPlt( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    DistoXNum num = getGeolocalizedData( sid, data, TopoDroidUtil.M2FT );
    if ( num == null ) return null;
    List<NumStation> stations = num.getStations();
    List<NumShot>    shots = num.getShots();
    // List<NumSplay>   splays = num.getSplays();

    // now write the PLT file
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("OziExplorer Track Point File Version 2.1\r\n");
      pw.format("WGS 84\r\n");
      pw.format("Altitude is in Feet\r\n");
      pw.format("Reserved 3\r\n");

      // skip-value: 0 (usually 1)
      // track-type: 0=normal, 10=closed_polygon, 20=alarm_zone
      // fill-style: 0=solid, 1=clear, 2=Bdiag, 3=Fdiag, 4=cross, 5=diag_cross, 6=horiz, 7=vert
      //
      pw.format("0,2,1677690,%s - TopoDroid v %s,0,0,0,8421376,-1,0\r\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );
      pw.format("%d\r\n", stations.size() );
      

      // date should be "days_since_12/30/1899.time_of_the_day"
      // eg, 0=12/30/1899, 2=1/1/1900, 35065=1/1/1996, 36526=1/1/00, 39447=1/1/08, 40908=1/1/12, ...
      Calendar cal = Calendar.getInstance();
      cal.set(1996,1,1);
      long diff = System.currentTimeMillis() - cal.getTimeInMillis();
      long days = 35065 + diff / 86400000L; // 24*60*60*1000 // FIXME +33 ?

      // String date = TopoDroidUtil.getDateString( "dd-MMM-yy" );

      NumStation last = null;
      for ( NumShot sh : shots ) {
        NumStation from = sh.from;
        NumStation to   = sh.to;
        if ( from != last ) {
          pw.format(Locale.ENGLISH, "%f, %f,1, %f,%d,,\r\n", from.e, from.s, from.v, days );
        }
        pw.format(Locale.ENGLISH, "%f,%f,0,%f,%d,,\r\n", to.e, to.s, to.v, days );
        last = to;
      }
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed cSurvey export: " + e.getMessage() );
      return null;
    }
  }

  // =======================================================================
  // POCKETTOPO EXPORT PocketTopo

  static String exportSurveyAsTop( long sid, DataHelper data, SurveyInfo info, DrawingActivity sketch, String origin, String filename )
  {
    PTFile ptfile = new PTFile();
    // TODO add a trip
    // date --> year, month, day --> _time
    // _declination (0)
    // _comment

    // TODO add shots
    // _from, _to
    // _dist, _azimuth, _inclination, _roll
    // extend left --> shot._flags bit-0
    // _trip_index (0)
    // _comment
    String[] vals = info.date.split( "\\." );
    try {
      ptfile.addTrip( Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]),
                      info.declination, info.comment );
    } catch ( NumberFormatException e ) {
      TDLog.Error( "exportSurveyAsTop date parse error " + info.date );
    }

    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    long extend = 0;  // current extend

    DistoXDBlock ref_item = null;
    int fromId, toId;

    for ( DistoXDBlock item : list ) {
      String from = item.mFrom;
      String to   = item.mTo;
      extend = item.mExtend;
      if ( from == null || from.length() == 0 ) {
        from = "";
        if ( to == null || to.length() == 0 ) {
          to = "";
          if ( ref_item != null 
            && ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
            from = ref_item.mFrom;
            to   = ref_item.mTo;
            extend = ref_item.mExtend;
          } else {
            ref_item = null;
          }
        } else { // only TO station
          ref_item = null;
        }
      } else { // with FROM station
        if ( to == null || to.length() == 0 ) { // splay shot
          to = "";
          ref_item = null;
        } else {
          ref_item = item;
        }
      }
      ptfile.addShot( (short)0, from, to, item.mLength, item.mBearing, item.mClino, item.mRoll, (int)extend, item.mComment );
    }

    if ( sketch != null ) {
      // TODO add sketch
    }

    try {
      TDPath.checkPath( filename );
      FileOutputStream fos = new FileOutputStream( filename );
      ptfile.write( fos );
      fos.close();
    } catch ( IOException e ) {
      TDLog.Error( "Failed PocketTopo export: " + e.getMessage() );
    }
    return filename;
  }
  // =======================================================================
  // THERION EXPORT Therion

  static private void writeThLeg( PrintWriter pw, AverageLeg leg ) 
  {
    pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", leg.length(), leg.bearing(), leg.clino() );
    leg.reset();
  }

  static String exportSurveyAsTh( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    List< PlotInfo > plots  = data.selectAllPlots( sid, TopoDroidApp.STATUS_NORMAL );
    List< CurrentStation > stations = data.getStations( sid );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("# %s created by TopoDroid v %s\n\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );
      pw.format("survey %s -title \"%s\"\n", info.name, info.name );
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format("    # %s \n", info.comment );
      }
      pw.format("\n");
      pw.format("  centerline\n");

      if ( fixed.size() > 0 ) {
        pw.format("    cs long-lat\n");
        for ( FixedInfo fix : fixed ) {
          pw.format("    # fix %s\n", fix.toExportString() );
        }
      }
      pw.format("    date %s \n", info.date );
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("    # team %s \n", info.team );
      }

      pw.format(Locale.ENGLISH, "    # declination %.2f degrees\n", info.declination );

      pw.format("    data normal from to length compass clino\n");

      long extend = 0;  // current extend
      AverageLeg leg = new AverageLeg();

      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeThLeg( pw, leg );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
              ref_item = null; 
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
              pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            }
            pw.format("    - %s ", to );
            pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
              writeThLeg( pw, leg );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
              ref_item = null; 
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
              pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            }
            pw.format("    %s - ", from ); // write splay shot
            pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeThLeg( pw, leg );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
            }
            ref_item = item;
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
              pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            }
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              pw.format(therion_flags_duplicate);
              duplicate = true;
            } else if ( item.mFlag == DistoXDBlock.BLOCK_SURFACE ) {
              pw.format(therion_flags_surface);
              surface = true;
            // } else if ( item.mFlag == DistoXDBlock.BLOCK_BACKSHOT ) {
            //   pw.format(therion_flags_duplicate);
            //   duplicate = true;
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            pw.format("    %s %s ", from, to );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
        // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeThLeg( pw, leg );
        if ( duplicate ) {
          pw.format(therion_flags_not_duplicate);
          // duplicate = false;
        }
      }
      if ( stations.size() > 0 ) {
        pw.format("\n");
        for ( CurrentStation station : stations ) {
          pw.format("    station %s \"%s\"\n", station.mName, station.mComment );
        }
      }
      pw.format("  endcenterline\n\n");

      for ( PlotInfo plt : plots ) {
        String extra = ((new File( TDPath.getSurveyPlotTh2File( info.name, plt.name ) )).exists())? "  #" : "  ##";
        pw.format("%s input \"%s-%s.th2\"\n", extra, info.name, plt.name );
      }
      pw.format("\n");
      for ( PlotInfo plt : plots ) {
        if ( plt.type == PlotInfo.PLOT_PLAN || plt.type == PlotInfo.PLOT_EXTENDED ) {
          String extra = ((new File( TDPath.getSurveyPlotTh2File( info.name, plt.name ) )).exists())? "  #" : "  ##";
          pw.format("%s map m%s -projection %s\n", extra, plt.name, PlotInfo.projName[ plt.type ] );
          pw.format("%s   %s-%s\n", extra, info.name, plt.name );
          pw.format("%s endmap\n", extra );
        }
      }

      pw.format("endsurvey\n");
      fw.flush();
      fw.close();

      // (new File( filename )).setReadable( true, false );

      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Therion export: " + e.getMessage() );
      return null;
    }
  }

  // -----------------------------------------------------------------------
  /** SURVEX EXPORT 
   *
   * The following format is used to export the centerline data in survex
   *
   *    *begin survey_name
   *      *units tape feet|metres
   *      *units compass clino grad|degrees
   *      *calibrate declination ...
   *      *date yyyy.mm.dd
   *      ; *fix station long lat alt
   *      ; *team "teams"
   *      *data normal from to tape compass clino
   *      ...
   *      *flags surface|not surface
   *      *flags duplicate|not duplicate
   *      *flags splay|not splay
   *      ...
   *      ; shot_comment
   *      ...
   *      (optional survey commands)
   *    *end survey_name
   */
  static String   survex_flags_duplicate     = "   *flags duplicate";
  static String   survex_flags_not_duplicate = "   *flags not duplicate";
  // static String   survex_flags_surface       = "   *flags surface";
  // static String   survex_flags_not_surface   = "   *flags not surface";

  static void writeSurvexLine( PrintWriter pw, String str )
  {
    pw.format("%s%s", str, TDSetting.mSurvexEol );
  }

  static void writeSurvexEOL( PrintWriter pw )
  {
    pw.format("%s", TDSetting.mSurvexEol );
  }

  static boolean writeSurvexLeg( PrintWriter pw, boolean first, boolean dup, AverageLeg leg, DistoXDBlock blk )
  {
    if ( first ) {
      pw.format(Locale.ENGLISH, "  %.2f %.1f %.1f", leg.length(), leg.bearing(), leg.clino() );
      leg.reset();
      if ( blk.mComment != null && blk.mComment.length() > 0 ) {
        pw.format("  ; %s", blk.mComment );
      } 
      writeSurvexEOL( pw );
    }
    if ( dup ) {
      if ( first ) writeSurvexLine(pw, survex_flags_not_duplicate);
      // dup = false;
    }
    return false;
  }

  static void writeSurvexLRUD( PrintWriter pw, String st, LRUD lrud )
  {
    if ( lrud != null ) {
      pw.format(Locale.ENGLISH, "%s  %.2f %.2f %.2f %.2f", st, lrud.l, lrud.r, lrud.u, lrud.d );
      writeSurvexEOL( pw );
    }
  }

  static void writeSurvexSplay( PrintWriter pw, String from, String to, DistoXDBlock blk )
  {
    pw.format(Locale.ENGLISH, "  %s %s %.2f %.1f %.1f", from, to, blk.mLength, blk.mBearing, blk.mClino );
    if ( blk.mComment != null && blk.mComment.length() > 0 ) {
      pw.format(" ; %s", blk.mComment );
    }
    writeSurvexEOL( pw );
  }

  static String exportSurveyAsSvx( long sid, DataHelper data, SurveyInfo info, Device device, String filename )
  {
    char splayChar = 'a';

    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    List<DistoXDBlock> st_blk = new ArrayList<DistoXDBlock>(); // blocks with from station (for LRUD)

    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("; %s created by TopoDroid v %s", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );
      writeSurvexEOL( pw );

      pw.format("; %s", info.name );
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format(" - %s", info.comment );
      }
      writeSurvexEOL(pw);

      pw.format("; Instrument: ");
      if ( device != null ) {
        pw.format("%s - ", device.toSimpleString() );
      }
      writeSurvexLine(pw, android.os.Build.MODEL );
      writeSurvexEOL(pw);

      pw.format("*begin %s ", info.name );      writeSurvexEOL(pw);
      pw.format("  *date %s ", info.date );     writeSurvexEOL(pw);
      pw.format("  *team \"%s\" ", info.team ); writeSurvexEOL(pw);
      writeSurvexLine(pw, "  *units tape metres" );
      writeSurvexLine(pw, "  *units compass degrees" );
      writeSurvexLine(pw, "  *units clino degrees" );
      pw.format(Locale.ENGLISH, "  *calibrate declination %.2f", info.declination ); writeSurvexEOL(pw);
      if ( ! TDSetting.mSurvexSplay ) {
        writeSurvexLine( pw, "  *alias station - .." );
      }

      if ( fixed.size() > 0 ) {
        writeSurvexLine(pw, "  ; fix stations as long-lat alt");
        for ( FixedInfo fix : fixed ) {
          writeSurvexLine(pw, "  ; *fix " + fix.toExportString() );
        }
      }

      writeSurvexLine( pw, "  *flags not splay");
      writeSurvexLine( pw, "  *data normal from to tape compass clino");
      
      boolean first = true;
      // for ( int k=0; k<2; ++k ) 
      { // first pass legs, second pass splays
        if ( ! first ) {
          writeSurvexEOL(pw);
          writeSurvexLine(pw, "  *flags splay");
        }
        AverageLeg leg = new AverageLeg();
        DistoXDBlock ref_item = null;
        boolean duplicate = false;
        boolean splays = false;
        for ( DistoXDBlock item : list ) {
          String from = item.mFrom;
          String to   = item.mTo;
          if ( from == null || from.length() == 0 ) {
            if ( to == null || to.length() == 0 ) { // no station: not exported
              if ( ref_item != null &&
                 ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
                leg.add( item.mLength, item.mBearing, item.mClino );
              }
            } else { // only TO station
              if ( leg.mCnt > 0 && ref_item != null ) {
                duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item );
                if ( TDSetting.mSurvexLRUD ) st_blk.add( ref_item );
                ref_item = null; 
              }

              if ( ! splays ) {
                if ( TDSetting.mSurvexSplay ) writeSurvexLine(pw, "  *flags splay" );
                splayChar = 'a';
                splays = true;
              } else {
                splayChar ++;
              }
              // if ( ! first  ) 
              {
                if ( TDSetting.mSurvexSplay ) {
                  writeSurvexSplay( pw, to + splayChar, to, item );
                } else {
                  writeSurvexSplay( pw, "-", to, item );
                }
              }
            }
          } else { // with FROM station
            if ( to == null || to.length() == 0 ) { // splay shot
              if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
                duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item );
                if ( TDSetting.mSurvexLRUD ) st_blk.add( ref_item );
                ref_item = null; 
              }

              if ( ! splays ) {
                if ( TDSetting.mSurvexSplay ) writeSurvexLine(pw, "  *flags splay" );
                splays = true;
                splayChar = 'a';
              } else {
                splayChar ++;
              }
              // if ( ! first  )
              {
                if ( TDSetting.mSurvexSplay ) {
                  writeSurvexSplay( pw, from, from + splayChar, item );
                } else {
                  writeSurvexSplay( pw, from, "-", item );
                }
              }
            } else {
              if ( leg.mCnt > 0 && ref_item != null ) {
                duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item );
                if ( TDSetting.mSurvexLRUD ) st_blk.add( ref_item );
                ref_item = null; 
              }
              if ( splays ) {
                if ( TDSetting.mSurvexSplay ) writeSurvexLine(pw, "  *flags not splay");
                splays = false;
              }
              ref_item = item;
              if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
                if ( first ) writeSurvexLine(pw, survex_flags_duplicate);
                duplicate = true;
              }
              if ( first ) pw.format("    %s %s ", from, to );
              leg.set( item.mLength, item.mBearing, item.mClino );
            }
          }
        }
        if ( leg.mCnt > 0 && ref_item != null ) {
          duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item );
          if ( TDSetting.mSurvexLRUD ) st_blk.add( ref_item );
          ref_item = null;
        }
        first = false;
      }
      writeSurvexLine(pw, "  *flags not splay");

      if ( TDSetting.mSurvexLRUD && st_blk.size() > 0 ) {
        String from = null;
        for ( int k=0; k<st_blk.size(); ++k ) { 
          if ( st_blk.get(k).mFrom != null && st_blk.get(k).mFrom.length() > 0 ) {
            from = st_blk.get(k).mFrom;
            break;
          }
        }
        if ( from != null ) {
          DistoXNum num = new DistoXNum( list, from, null, null );
          List<NumBranch> branches = num.makeBranches( true );
          for ( NumBranch branch : branches ) {
            // ArrayList<String> stations = new ArrayList<String>();
            ArrayList<NumShot> shots = branch.shots;
            int size = shots.size();
            if ( size > 0 ) {
              pw.format("*data passage station left right up down");
              writeSurvexEOL( pw );
              NumShot sh = shots.get(0);
              NumStation s1 = sh.from;
              NumStation s2 = sh.to;
              int index = 0;
              int step = 1;
              if ( sh.mBranchDir < 0 ) {
                step = -1;
                index = size - 1;
                sh = shots.get( index );
              }
              DistoXDBlock blk0 = sh.getFirstBlock();
              if ( size == 1 ) {
                if ( step > 0 ) {
                  writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ) );
                  writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ) );
                } else {
                  writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ) );
                  writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ) );
                }
              } else {
                String st_name = null;
                for ( int k = 1; k<size; ++k ) {
                  index += step;
                  sh = shots.get(index);
                  DistoXDBlock blk = sh.getFirstBlock();
                  if ( k == 1 ) {
                    // Log.v("DistoX", blk0.mFrom + "-" + blk0.mTo + " branch dir " + sh.mBranchDir + " blk dir " + sh.mDirection );
                    if ( blk0.mFrom.equals( blk.mFrom ) || blk0.mFrom.equals( blk.mTo ) ) {
                      st_name = blk0.mFrom;
                      if ( step > 0 ) {
                        writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ) );
                        writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ) );
                      } else {
                        writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ) );
                        writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ) );
                      }
                    } else if ( blk0.mTo.equals( blk.mFrom ) || blk0.mTo.equals( blk.mTo ) ) {
                      st_name = blk0.mTo;
                      if ( step > 0 ) {
                        writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ) );
                        writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ) );
                      } else {
                        writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ) );
                        writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ) );
                      }
                    }
                  }
                  if ( st_name.equals( blk.mTo ) ) {
                    writeSurvexLRUD( pw, blk.mFrom, computeLRUD( blk, list, true ) );
                    st_name = blk.mFrom;
                  } else if ( st_name.equals( blk.mFrom ) ) {
                    writeSurvexLRUD( pw, blk.mTo, computeLRUD( blk, list, false ) );
                    st_name = blk.mTo;
                  } else {
                    Log.e("DistoX", "ERROR unattached branch shot " + sh.from.name + " " + sh.to.name + " station " + st_name );
                    break;
                  }
                }
              }
              writeSurvexEOL( pw );
            }
          }
        }
        // for ( int k=0; k<st_blk.size(); ++k ) { // remove duplicate FROM stations
        //   String from = st_blk.get(k).mFrom;
        //   if ( from == null || from.length() == 0 ) {
        //     st_blk.remove(k);
        //     --k;
        //   } else {
        //     for ( int j=0; j<k; ++j ) {
        //       if ( from.equals( st_blk.get(j).mFrom ) ) {
        //         st_blk.remove(k);
        //         --k;
        //         break;
        //       }
        //     }
        //   }
        // }
        // if ( st_blk.size() > 0 ) {
        //   pw.format("*data passage station left right up down");
        //   writeSurvexEOL( pw );
        //   for ( DistoXDBlock blk : st_blk ) {
        //     writeSurvexLRUD( pw, blk.mFrom, computeLRUD( blk, list, true ) );
        //   }
        // }
      }
      pw.format("*end %s", info.name ); writeSurvexEOL(pw);
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Survex export: " + e.getMessage() );
      return null;
    }
  }

  // -----------------------------------------------------------------------
  /** CSV COMMA-SEPARATED VALUES EXPORT 
   *  NOTE declination exported in comment only in CSV
   *
   */
  static private void writeCsvLeg( PrintWriter pw, AverageLeg leg )
  {
    pw.format(Locale.ENGLISH, ",%.2f,%.1f,%.1f", 
      leg.length() * TDSetting.mCsvLengthUnit, leg.bearing(), leg.clino() );
    leg.reset();
  }

  static String exportSurveyAsCsv( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    // List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("# %s created by TopoDroid v %s\n\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );
      pw.format("# %s\n", info.name );
      // if ( fixed.size() > 0 ) {
      //   pw.format("  ; fix stations as long-lat alt\n");
      //   for ( FixedInfo fix : fixed ) {
      //     pw.format("  ; *fix %s\n", fix.toExportString() );
      //   }
      // }
      pw.format(Locale.ENGLISH, "# from to tape compass clino (declination %.4f)\n", info.declination );
      
      AverageLeg leg = new AverageLeg();
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean splays = false;
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsvLeg( pw, leg );
              if ( duplicate ) {
                pw.format(",L");
                duplicate = false;
              }
              pw.format("\n");
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.ENGLISH, "-,%s@%s,%.2f,%.1f,%.1f\n", to, info.name,
              item.mLength * TDSetting.mCsvLengthUnit, item.mBearing, item.mClino );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", item.mComment );
            // }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              writeCsvLeg( pw, leg );
              if ( duplicate ) {
                pw.format(",L");
                duplicate = false;
              }
              pw.format("\n");
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.ENGLISH, "%s@%s,-,%.2f,%.1f,%.1f\n", from, info.name,
              item.mLength * TDSetting.mCsvLengthUnit, item.mBearing, item.mClino );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", item.mComment );
            // }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsvLeg( pw, leg );
              if ( duplicate ) {
                pw.format(",L");
                duplicate = false;
              }
              pw.format("\n");
              // n = 0;
            }
            if ( splays ) {
              splays = false;
            }
            ref_item = item;
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              duplicate = true;
            }
            pw.format("%s@%s,%s@%s", from, info.name, to, info.name );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeCsvLeg( pw, leg );
        if ( duplicate ) {
          pw.format(",L");
          // duplicate = false;
        }
        pw.format("\n");
      }
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed CSV export: " + e.getMessage() );
      return null;
    }
  }

  // -----------------------------------------------------------------------
  // TOPOLINUX EXPORT 

  // public String exportSurveyAsTlx( long sid, DataHelper data, SurveyInfo info, String filename ) // FIXME args
  // {
  //   File dir = new File( TopoDroidApp.APP_TLX_PATH );
  //   if (!dir.exists()) {
  //     dir.mkdirs();
  //   }
  //   String filename = TopoDroidApp.APP_TLX_PATH + info.name + ".tlx";
  //   List<DistoXDBlock> list = mData.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
  //   try {
  //     TDPath.checkPath( filename );
  //     FileWriter fw = new FileWriter( filename );
  //     PrintWriter pw = new PrintWriter( fw );
  //     pw.format("tlx2\n");
  //     pw.format("# %s created by TopoDroid v %s\n\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );
  //     pw.format("# date %s \n", mData.getSurveyDate( sid ) );
  //     pw.format("# %s \n", mData.getSurveyComment( sid ) );
  //     int n = 0;
  //     float l=0.0f, b=0.0f, c=0.0f;
  //     float l0[] = new float[10];
  //     float b0[] = new float[10];
  //     float c0[] = new float[10];
  //     float r0[] = new float[10];
  //     DistoXDBlock ref_item = null;
  //     int extend = 0;
  //     int flag   = DistoXDBlock.BLOCK_SURVEY;

  //     for ( DistoXDBlock item : list ) {
  //       String from = item.mFrom;
  //       String to   = item.mTo;
  //       if ( from != null && from.length() > 0 ) {
  //         if ( to != null && to.length() > 0 ) {
  //           if ( n > 0 /* && ref_item != null */ ) {
  //             b = TopoDroidUtil.in360( b/n );
  //             pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             extend = 0;
  //             flag   = DistoXDBlock.BLOCK_SURVEY;
  //           }
  //           n = 1;
  //           ref_item = item;
  //           // item.Comment()
  //           pw.format("    \"%s\" \"%s\" ", from, to );
  //           l = item.mLength;
  //           b = item.mBearing;
  //           c = item.mClino;
  //           extend = (int) item.mExtend;
  //           flag   = (int) item.mFlag;
  //           l0[0] = item.mLength;
  //           b0[0] = item.mBearing;
  //           c0[0] = item.mClino;
  //           r0[0] = item.mRoll;
  //         } else { // to.isEmpty()
  //           if ( n > 0 /* && ref_item != null */ ) {
  //             b = TopoDroidUtil.in360( b/n );
  //             pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1fi %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             n = 0;
  //             ref_item = null;
  //             extend = 0;
  //             flag   = DistoXDBlock.BLOCK_SURVEY;
  //           }
  //           // item.Comment()
  //           pw.format("    \"%s\" \"\" ", from );
  //           pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d 1\n",
  //             item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, item.mFlag );
  //         }
  //       } else { // from.isEmpty()
  //         if ( to != null && to.length() > 0 ) {
  //           if ( n > 0 /* && ref_item != null */ ) {
  //             b = TopoDroidUtil.in360( b/n );
  //             pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             n = 0;
  //             ref_item = null;
  //             extend = 0;
  //             flag   = DistoXDBlock.BLOCK_SURVEY;
  //           }
  //           // item.Comment()
  //           pw.format("    \"\" \"%s\" ", to );
  //           pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d 1\n",
  //             item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, item.mFlag );
  //         } else {
  //           // not exported
  //           if ( ref_item != null &&
  //              ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
  //             float bb = TopoDroidUtil.around( item.mBearing, b0[0] );
  //             l += item.mLength;
  //             b += bb;
  //             c += item.mClino;
  //             l0[n] = item.mLength;
  //             b0[n] = item.mBearing;
  //             c0[n] = item.mClino;
  //             r0[n] = item.mRoll;
  //             ++n;
  //           }
  //         }
  //       }
  //     }
  //     if ( n > 0 /* && ref_item != null */ ) {
  //       b = TopoDroidUtil.in360( b/n );
  //       pw.format(Locale.ENGLISH, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
  //       while ( n > 0 ) {
  //         -- n;
  //         pw.format(Locale.ENGLISH, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //       }
  //       // extend = 0;
  //       // flag   = DistoXDBlock.BLOCK_SURVEY;
  //     }
  //     // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.1f %d %d %d\n", 
  //     //   item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, 0, 1 );
  //     // item.mComment
  //     fw.flush();
  //     fw.close();
  //     return filename;
  //   } catch ( IOException e ) {
  //     TDLog.Error( "Failed QTopo export: " + e.getMessage() );
  //     return null;
  //   }
  // }

  // -----------------------------------------------------------------------
  // COMPASS EXPORT 

  static private LRUD computeLRUD( DistoXDBlock b, List<DistoXDBlock> list, boolean at_from )
  {
    LRUD lrud = new LRUD();
    float n0  = TDMath.cosd( b.mBearing );
    float e0  = TDMath.sind( b.mBearing );
    float cc0 = TDMath.cosd( b.mClino );
    float sc0 = TDMath.sind( b.mClino );
    float cb0 = n0;
    float sb0 = e0;
    String station = ( at_from ) ? b.mFrom : b.mTo;
    
    for ( DistoXDBlock item : list ) {
      String from = item.mFrom;
      String to   = item.mTo;
      if ( from == null || from.length() == 0 ) { // skip blank
        // if ( to == null || to.length() == 0 ) continue;
        continue;
      } else { // skip leg
        if ( to != null && to.length() > 0 ) continue;
      }
      if ( station.equals( from ) ) {
        float cb = TDMath.cosd( item.mBearing );
        float sb = TDMath.sind( item.mBearing );
        float cc = TDMath.cosd( item.mClino );
        float sc = TDMath.sind( item.mClino );
        float len = item.mLength;

        // skip splays too close to shot direction // FIXME setting
        // this test aims to use only splays that are "orthogonal" to the shot
        if ( TDSetting.mOrthogonalLRUD ) {
          float cbb1 = sc*sc0*(sb*sb0 + cb*cb0) + cc*cc0; // cosine of angle between block and item
          if ( Math.abs( cbb1 ) > TDSetting.mOrthogonalLRUDCosine ) continue; 
        }

        float z1 = len * sc;
        float n1 = len * cc * cb;
        float e1 = len * cc * sb;
        if ( z1 > 0.0 ) { if ( z1 > lrud.u ) lrud.u = z1; }
        else            { if ( -z1 > lrud.d ) lrud.d = -z1; }

        float rl = e1 * n0 - n1 * e0;
        if ( rl > 0.0 ) { if ( rl > lrud.r ) lrud.r = rl; }
        else            { if ( -rl > lrud.l ) lrud.l = -rl; }
      }
    }
    // Log.v("DistoX", "<" + b.mFrom + "-" + b.mTo + "> at " + station + ": " + lrud.l + " " + lrud.r );
    return lrud;
  }

  /** Centerline data are exported in Compass format as follows
   *    SURVEY NAME: survey_name
   *    SURVEY DATE: mm dd yyyy
   *    SURVEY TEAM:
   *    team_line
   *    DECLINATION: declination  FORMAT: DMMDLUDRLADN  CORRECTIONS:  0.00 0.00 0.00
   *    FROM TO LENGTH BEARING INC FLAGS COMMENTS
   *    ...
   *    0x0c
   *
   * Notes.
   * Names must limited to 14 characters: this include the "prefix" and the station FROM and TO names.
   * Distances are in feet.
   * The flags string is composed as "#|...#", Flags characters: L (duplicate) P (no plot) X (surface).
   * Splay shots are not exported, they may be used to find transversal dimensions, if LRUD are not provided  
   * Multisurvey file is possible.
   */

  private static void printShotToDat( PrintWriter pw, AverageLeg leg, LRUD lrud, boolean duplicate, String comment )
  {
    pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.2f %.2f %.2f %.2f", 
      leg.length()*TopoDroidUtil.M2FT, leg.bearing(), leg.clino(),
      lrud.l*TopoDroidUtil.M2FT, lrud.u*TopoDroidUtil.M2FT, lrud.d*TopoDroidUtil.M2FT, lrud.r*TopoDroidUtil.M2FT );
    leg.reset();
    if ( duplicate ) {
      pw.format(" #|L#");
    }
    if ( comment != null && comment.length() > 0 ) {
      pw.format(" %s", comment );
    }
    pw.format( "\r\n" );
  }

  static String exportSurveyAsDat( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      // FIXME 
      // pw.format("# %s created by TopoDroid v %s\n\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );

      pw.format("%s\r\n", info.name ); // export as single survey
      pw.format("SURVEY NAME: %s\r\n", info.name );
      String date = info.date;
      int y = 0;
      int m = 0;
      int d = 0;
      if ( date != null && date.length() == 10 ) {
        try {
          y = Integer.parseInt( date.substring(0,4) );
          m = Integer.parseInt( date.substring(5,7) );
          d = Integer.parseInt( date.substring(8,10) );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "exportSurveyAsDat date parse error " + date );
        }
      }
      pw.format("SURVEY DATE: %02d %02d %04d", m, d, y ); // format "MM DD YYYY"
      if ( info.comment != null ) {
        pw.format(" COMMENT %s", info.comment );
      }
      pw.format("\r\n");

      pw.format("SURVEY TEAM:\r\n");
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("%s\r\n", info.team );
      } else {
        pw.format("...\r\n");
      }
      pw.format(Locale.ENGLISH, "DECLINATION: %.4f  ", info.declination );
      pw.format("FORMAT: DMMDLUDRLADN  CORRECTIONS:  0.00 0.00 0.00\r\n" );
      pw.format("\r\n" );
      pw.format("FROM TO LENGTH BEARING INC FLAGS COMMENTS\r\n" );
      pw.format( "\r\n" );

      AverageLeg leg = new AverageLeg();
      DistoXDBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay = false;
      boolean duplicate = false;
      LRUD lrud;

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              pw.format("%s-%s %s-%s ", info.name, ref_item.mFrom, info.name, ref_item.mTo );
              printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              lrud = computeLRUD( ref_item, list, true );
              pw.format("%s-%s %s-%s ", info.name, ref_item.mFrom, info.name, ref_item.mTo );
              printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              pw.format("%s-%s %s-%s ", info.name, ref_item.mFrom, info.name, ref_item.mTo );
              printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
            }
            ref_item = item;
            duplicate = ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        lrud = computeLRUD( ref_item, list, true );
        pw.format("%s-%s %s-%s ", info.name, ref_item.mFrom, info.name, ref_item.mTo );
        printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
      }
      pw.format( "\f\r\n" );

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Compass export: " + e.getMessage() );
      return null;
    }
  }

  // =======================================================================
  // WALLS EXPORT 

  static private void writeSrvLeg( PrintWriter pw, AverageLeg leg )
  {
    pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f", leg.length(), leg.bearing(), leg.clino() );
    leg.reset();
  }
 
  static String exportSurveyAsSrv( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      pw.format("; %s\n", info.name );
      pw.format("; created by TopoDroid v %s - %s \n", TopoDroidApp.VERSION, TopoDroidUtil.getDateString("yyyy.MM.dd") );
      pw.format(Locale.ENGLISH, "#Units Decl=%.1f\n", info.declination );

      String date = info.date;
      int y = 0;
      int m = 0;
      int d = 0;
      if ( date != null && date.length() == 10 ) {
        try {
          y = Integer.parseInt( date.substring(0,4) );
          m = Integer.parseInt( date.substring(5,7) );
          d = Integer.parseInt( date.substring(8,10) );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "exportSurveyAsDat date parse error " + date );
        }
      }
      pw.format("#Date %04d-%02d-%02d\n", y, m, d ); // format "YYYY-MM-DD"
      if ( info.comment != null ) {
        pw.format("; %s\n", info.comment );
      }

      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("; TEAM %s\r\n", info.team );
      }

      List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
      if ( fixed.size() > 0 ) {
        for ( FixedInfo fix : fixed ) {
          pw.format("#Fix %s", fix.name );
          if ( fix.lng >= 0 ) {
            pw.format(Locale.ENGLISH, " E%.6f", fix.lng );
          } else {
            pw.format(Locale.ENGLISH, " W%.6f", - fix.lng );
          }
          if ( fix.lat >= 0 ) {
            pw.format(Locale.ENGLISH, " N%.6f", fix.lat );
          } else {
            pw.format(Locale.ENGLISH, " S%.6f", - fix.lat );
          }
          pw.format(Locale.ENGLISH, " %.0f", fix.alt );
          if ( fix.comment != null && fix.comment.length() > 0 ) pw.format(" /%s", fix.comment );
          pw.format("\n");
        }
      } 

      List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
      // int extend = 1;
      AverageLeg leg = new AverageLeg();
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO
      for ( DistoXDBlock item : list ) {
        String from    = item.mFrom;
        String to      = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeSrvLeg( pw, leg );
              if ( ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
                pw.format("\t; %s\n", ref_item.mComment );
              } else {
                pw.format("\n");
              }
              if ( duplicate ) {
                // FIXME pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                // FIXME pw.format(therion_flags_not_surface);
                surface = false;
              }
              ref_item = null; 
            }
            // if ( item.mExtend != extend ) {
            //   extend = item.mExtend;
            //   //  FIXME pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            // }
            pw.format("-\t%s\t", to );
            pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f", item.mLength, item.mBearing, item.mClino );
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("\t; %s\n", item.mComment );
            } else {
              pw.format("\n");
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
              writeSrvLeg( pw, leg );
              if ( ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
                pw.format("\t; %s\n", ref_item.mComment );
              } else {
                pw.format("\n");
              }
              if ( duplicate ) {
                // FIXME pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                // FIXME pw.format(therion_flags_not_surface);
                surface = false;
              }
              ref_item = null; 
            }
            // if ( item.mExtend != extend ) {
            //   extend = item.mExtend;
            //   // FIXME pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            // }
            pw.format("%s\t-\t", from ); // write splay shot
            pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f", item.mLength, item.mBearing, item.mClino );
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("\t; %s\n", item.mComment );
            } else {
              pw.format("\n");
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeSrvLeg( pw, leg );
              if ( ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
                pw.format("\t; %s\n", ref_item.mComment );
              } else {
                pw.format("\n");
              }
              if ( duplicate ) {
                // FIXME pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                // FIXME pw.format(therion_flags_not_surface);
                surface = false;
              }
            }
            ref_item = item;
            // if ( item.mExtend != extend ) {
            //   extend = item.mExtend;
            //   // FIXME pw.format("    extend %s\n", therion_extend[1+(int)(extend)] );
            // }
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              // FIXME pw.format(therion_flags_duplicate);
              duplicate = true;
            } else if ( item.mFlag == DistoXDBlock.BLOCK_SURFACE ) {
              // FIXME pw.format(therion_flags_surface);
              surface = true;
            // } else if ( item.mFlag == DistoXDBlock.BLOCK_BACKSHOT ) {
            //   // FIXME pw.format(therion_flags_duplicte);
            //   duplicate = true;
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("\t; %s\n", item.mComment );
            }
            pw.format("%s\t%s\t", from, to );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
        // pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f\n", item.mLength, item.mBearing, item.mClino );
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeSrvLeg( pw, leg );
        if ( ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
          pw.format("\t; %s\n", ref_item.mComment );
        } else {
          pw.format("\n");
        }
        if ( duplicate ) {
          // pw.format(therion_flags_not_duplicate);
          // duplicate = false;
        }
      }

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Walls export: " + e.getMessage() );
      return null;
    }
  }

  // -----------------------------------------------------------------------
  // DXF EXPORT 
  // NOTE declination not taken into account in DXF export (only saved in comment)

  static String exportSurveyAsDxf( long sid, DataHelper data, SurveyInfo info, DistoXNum num, String filename )
  {
    // Log.v( TAG, "exportSurveyAsDxf " + filename );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter out = new PrintWriter( fw );
      // TODO
      out.printf(Locale.ENGLISH, "999\nDXF created by TopoDroid v %s - %s (declination %.4f)\n",
        TopoDroidApp.VERSION, TopoDroidUtil.getDateString("yyyy.MM.dd"), info.declination );
      out.printf("0\nSECTION\n2\nHEADER\n");
      out.printf("9\n$ACADVER\n1\nAC1006\n");
      out.printf("9\n$INSBASE\n");
      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", 0.0, 0.0, 0.0 ); // FIXME (0,0,0)
      out.printf("9\n$EXTMIN\n");
      float emin = num.surveyEmin() - 2.0f;
      float nmin = - num.surveySmax() - 2.0f;
      float zmin = - num.surveyVmax() - 2.0f;
      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        // num.surveyEmin(), -num.surveySmax(), -num.surveyVmax() );
      out.printf("9\n$EXTMAX\n");
      float emax = num.surveyEmax();
      float nmax = - num.surveySmin();
      float zmax = - num.surveyVmin();
      
      int de = (100f < emax-emin )? 100 : (50f < emax-emin)? 50 : 10;
      int dn = (100f < nmax-nmin )? 100 : (50f < nmax-nmin)? 50 : 10;
      int dz = (100f < zmax-zmin )? 100 : (50f < zmax-zmin)? 50 : 10;

      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emax, nmax, zmax );
        // num.surveyEmax(), -num.surveySmin(), -num.surveyVmin() );
      out.printf("0\nENDSEC\n");

      out.printf("0\nSECTION\n2\nTABLES\n");
      {
        out.printf("0\nTABLE\n2\nLTYPE\n70\n1\n");
        // int flag = 64;
        out.printf("0\nLTYPE\n2\nCONTINUOUS\n70\n64\n3\nSolid line\n72\n65\n73\n0\n40\n0.0\n");
        out.printf("0\nENDTAB\n");

        out.printf("0\nTABLE\n2\nLAYER\n70\n6\n");
          // 2 layer name, 70 flag (64), 62 color code, 6 line style
          String style = "CONTINUOUS";
          int flag = 64;
          out.printf("0\nLAYER\n2\nLEG\n70\n%d\n62\n%d\n6\n%s\n",     flag, 1, style );
          out.printf("0\nLAYER\n2\nSPLAY\n70\n%d\n62\n%d\n6\n%s\n",   flag, 2, style );
          out.printf("0\nLAYER\n2\nSTATION\n70\n%d\n62\n%d\n6\n%s\n", flag, 3, style );
          out.printf("0\nLAYER\n2\nREF\n70\n%d\n62\n%d\n6\n%s\n",     flag, 4, style );
        out.printf("0\nENDTAB\n");

        out.printf("0\nTABLE\n2\nSTYLE\n70\n0\n");
        out.printf("0\nENDTAB\n");
      }
      out.printf("0\nENDSEC\n");

      out.printf("0\nSECTION\n2\nBLOCKS\n");
      out.printf("0\nENDSEC\n");

      out.printf("0\nSECTION\n2\nENTITIES\n");
      {
        emin += 1f;
        nmin += 1f;
        zmin += 1f;
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin+de, nmin, zmin );
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin, nmin+dn, zmin );
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin, nmin, zmin+dz );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (de==100)? "100" : (de==50)? "50" : "10" );
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin+de+1, nmin, zmin );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (dn==100)? "100" : (dn==50)? "50" : "10" );
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin, nmin+de+1, zmin );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (dz==100)? "100" : (dz==50)? "50" : "10" );
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin, nmin, zmin+dz+1 );

        // centerline data
        for ( NumShot sh : num.getShots() ) {
          NumStation f = sh.from;
          NumStation t = sh.to;
          out.printf("0\nLINE\n8\nLEG\n");
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, -f.s, -f.v );
          out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", t.e, -t.s, -t.v );
        }

        for ( NumSplay sh : num.getSplays() ) {
          NumStation f = sh.from;
          out.printf("0\nLINE\n8\nSPLAY\n");
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, -f.s, -f.v );
          out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", sh.e, -sh.s, -sh.v );
        }
   
        for ( NumStation st : num.getStations() ) {
          // FIXME station scale is 0.3
          out.printf("0\nTEXT\n8\nSTATION\n");
          out.printf("1\n%s\n", st.name );
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", st.e, -st.s, -st.v );
        }
      }
      out.printf("0\nENDSEC\n");
      out.printf("0\nEOF\n");

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed DXF export: " + e.getMessage() );
      return null;
    }
  }

  // -----------------------------------------------------------------------
  // VISUALTOPO EXPORT 
  // FIXME photos
  // FIXME not sure declination written in the right place

  /**
   * @param pw     writer
   * @param item   reference shot
   ( @param list   ...
   * @note item is guaranteed not null by the caller
   */
  static private boolean printStartShotToTro( PrintWriter pw, DistoXDBlock item, List< DistoXDBlock > list )
  {
    if ( item == null ) return false;
    LRUD lrud = computeLRUD( item, list, true );
    pw.format(Locale.ENGLISH, "%s %s 0.00 0.00 0.00 ", item.mFrom, item.mFrom );
    pw.format(Locale.ENGLISH, "%.2f %.2f %.2f %.2f N I", lrud.l, lrud.r, lrud.u, lrud.d );
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
    return true;
  }

  static private boolean printShotToTro( PrintWriter pw, DistoXDBlock item, AverageLeg leg, LRUD lrud )
  {
    if ( item == null ) return false;
    // Log.v( TAG, "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    pw.format("%s %s ", item.mFrom, item.mTo );
    pw.format(Locale.ENGLISH, "%.2f %.1f %.1f ", leg.length(), leg.bearing(), leg.clino() );
    leg.reset();
    pw.format(Locale.ENGLISH, "%.2f %.2f %.2f %.2f N I", lrud.l, lrud.r, lrud.u, lrud.d );
    // if ( duplicate ) {
    //   // pw.format(" #|L#");
    // }
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
    return true;
  }

  static String exportSurveyAsTro( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      // FIXME 
      // pw.format("; %s created by TopoDroid v %s\n\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );

      pw.format("Version 5.02\r\n\r\n");
      if ( fixed.size() > 0 ) { // FIXME
        // pw.format(Locale.ENGLISH, "Trou %s,%.2f,%.2f,LT2E\r\n", info.name, fix.lat, fix.lng );
        for ( FixedInfo fix : fixed ) {
          // pw.format("Entree %s\r\n", fix.name );
          break;
        }
      } else {
        // pw.format("Trou %s\r\n", info.name );
      }
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("Club %s\r\n", info.team );
      }
      pw.format("Couleur 0,0,0\r\n\r\n");
      
      pw.format(Locale.ENGLISH, "Param Deca Degd Clino Degd %.4f Dir,Dir,Dir Arr Inc 0,0,0\r\n\r\n", info.declination );

      AverageLeg leg = new AverageLeg();
      DistoXDBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay  = false;
      boolean duplicate = false;
      boolean started   = false;
      LRUD lrud;

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) ) ) {
              // Log.v( TAG, "data " + item.mLength + " " + item.mBearing + " " + item.mClino );
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list );
              }
              lrud = computeLRUD( ref_item, list, false );
              printShotToTro( pw, ref_item, leg, lrud );
              duplicate = false;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list );
              }
              lrud = computeLRUD( ref_item, list, false );
              printShotToTro( pw, ref_item, leg, lrud );
              duplicate = false;
              ref_item = null; 
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list );
              }
              lrud = computeLRUD( ref_item, list, false );
              printShotToTro( pw, ref_item, leg, lrud );
            }
            ref_item = item;
            duplicate = ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE );
            // Log.v( TAG, "first data " + item.mLength + " " + item.mBearing + " " + item.mClino );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        if ( ! started ) {
          started = printStartShotToTro( pw, ref_item, list );
        }
        lrud = computeLRUD( ref_item, list, false );
        printShotToTro( pw, ref_item, leg, lrud );
      }

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed VisualTopo export: " + e.getMessage() );
      return null;
    }
  }

  static String exportCalibAsCsv( long cid, DeviceHelper data, CalibInfo ci, String filename )
  {
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("# %s created by TopoDroid v %s\n\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );

      pw.format("# %s\n", ci.name );
      pw.format("# %s\n", ci.date );
      pw.format("# %s\n", ci.device );
      pw.format("# %s\n", ci.comment );
      pw.format("# %d\n", ci.algo );

      List<CalibCBlock> list = data.selectAllGMs( cid, 1 ); // status 1 --> all shots
      for ( CalibCBlock b : list ) {
        b.computeBearingAndClino();
        pw.format(Locale.ENGLISH, "%d, %d, %d, %d, %d, %d, %d, %d, %.2f, %.2f, %.2f, %.4f, %d\n",
          b.mId, b.gx, b.gy, b.gz, b.mx, b.my, b.mz, b.mGroup, b.mBearing, b.mClino, b.mRoll, b.mError, b.mStatus );
      }
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed CSV export: " + e.getMessage() );
      return null;
    }
  }


  static int importCalibFromCsv( DeviceHelper data, String filename, String device_name )
  {
    int ret = 0;
    try {
      TDPath.checkPath( filename );
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
    
      String line = br.readLine();
      if ( line == null || line.indexOf("TopoDroid") < 0 ) {
        ret = -1; // NOT TOPODROID CSV
      } else {
        br.readLine(); // skip empty line
        String name = br.readLine().substring(2);
        if ( data.hasCalibName( name ) ) {
          ret = -2; // CALIB NAME ALREADY EXISTS
        } else {
          String date   = br.readLine().substring(2);
          String device = br.readLine().substring(2);
          if ( ! device.equals( device_name ) ) {
            ret = -3; // DEVICE MISMATCH
          } else {
            String comment = br.readLine().substring(2);
            long algo = 0L;
            line = br.readLine();
            if ( line != null && line.charAt(0) == '#' ) {
              try {
                algo = Long.parseLong( line.substring(2) );
              } catch ( NumberFormatException e ) { }
              line = br.readLine();
            }
            if ( line == null ) {
              ret = -4;
            } else {
              long cid = data.insertCalib( name, date, device, comment, algo );
              while ( line != null ) {
                String[] vals = line.split(", ");
                if ( vals.length > 7 ) {
                  try {
                    long gx = Long.parseLong( vals[1] );
                    long gy = Long.parseLong( vals[2] );
                    long gz = Long.parseLong( vals[3] );
                    long mx = Long.parseLong( vals[4] );
                    long my = Long.parseLong( vals[5] );
                    long mz = Long.parseLong( vals[6] );
                    long gid = data.insertGM( cid, gx, gy, gz, mx, my, mz );
                    String grp = vals[7];
                    grp.trim();
                    data.updateGMName( gid, cid, grp );
                  } catch ( NumberFormatException e ) { }
                }
                line = br.readLine();
              }
            }
          }
        }
      }
      fr.close();
    } catch ( IOException e ) {
      TDLog.Error( "Failed CSV import: " + e.getMessage() );
      ret = -5; // IO Exception
    }
    return ret;
  }
}
