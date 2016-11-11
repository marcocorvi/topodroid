/** @file TopoDroidExporter.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief TopoDroid exports
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * formats
 *   cSurvey
 *   Compass
 *   Therion
 *   VisualTopo
 *   PocketTopo
 *   Survex
 *   Walls
 *   Polygon
 *   KML
 *   Track file (OziExplorer)
 *   DXF
 *   CSV
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
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
    pw.format(Locale.US, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
      leg.length(), leg.bearing(), leg.clino()
    );
    leg.reset();
  }

  // segments have only the attribute "cave", no attribute "branch"
  static private void writeCsxSegment( PrintWriter pw, String cave, String f, String t )
  {
    pw.format("<segment id=\"\" cave=\"%s\" from=\"%s\" to=\"%s\"", cave, f, t );
  }

  static private void writeCsxTSplaySegment( PrintWriter pw, String cave, String t, int cnt )
  {
    pw.format("<segment id=\"\" cave=\"%s\" from=\"%s(%d)\" to=\"%s\"", cave, t, cnt, t );
  }

  static private void writeCsxFSplaySegment( PrintWriter pw, String cave, String f, int cnt )
  {
    pw.format("<segment id=\"\" cave=\"%s\" from=\"%s\" to=\"%s(%d)\"", cave, f, f, cnt );
  }


  static String exportSurveyAsCsx( long sid, DataHelper data, SurveyInfo info, DrawingActivity sketch,
                                   String origin, String filename )
  {
    // Log.v("DistoX", "export as csurvey: " + filename );
    String cave = info.name.toUpperCase();

    // String prefix = "";
    String branch = "";
    if ( sketch != null && sketch.getName() != null ) {
      branch = sketch.getName();
    }

    // STATIONS_PREFIX
    // if ( TDSetting.mExportStationsPrefix ) {
    //   if ( branch.length() > 0 ) {
    //     prefix = cave + "-" + branch + "-";
    //   } else {
    //     prefix = cave + "-";
    //   }
    // }

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
      pw.format("  <properties id=\"\" name=\"\" origin=\"%s\" ", origin ); // prefix
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
      pw.format(Locale.US, "nordtype=\"0\" manualdeclination=\"0\" declination=\"%.4f\" ", info.declination );
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
      pw.format("        <branches>\n");
      if ( branch != null ) {
        pw.format("          <branch name=\"%s\">\n          </branch>\n", branch );
      }
      pw.format("        </branches>\n");
      pw.format("      </caveinfo>\n");
      pw.format("    </caveinfos>\n");

   // ============== ORIGIN
      if ( origin != null )  {
        pw.format("    <gps enabled=\"0\" refpointonorigin=\"%s\" geo=\"WGS84\" format=\"\" sendtotherion=\"0\" />\n", origin );
        // prefix
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
      AverageLeg leg = new AverageLeg(0);

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsxSegment( pw, cave, f, t ); // branch prefix

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
            writeCsxTSplaySegment( pw, cave, to, cntSplay ); // branch prefix
            ++ cntSplay;
            pw.format(" splay=\"1\" exclude=\"1\"");
            if ( extend == -1 ) pw.format(" direction=\"1\"");
            pw.format(Locale.US, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
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
              writeCsxSegment( pw, cave, f, t ); // branch prefix
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
            writeCsxFSplaySegment( pw, cave, from, cntSplay ); // branch prefix
            ++cntSplay;
            pw.format(" splay=\"1\" exclude=\"1\"");
            if ( extend == -1 ) pw.format(" direction=\"1\"");
            pw.format(Locale.US, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
               item.mLength, item.mBearing, item.mClino );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format(" note=\"%s\"", item.mComment.replaceAll("\"", "") );
            }
            pw.format(" />\n");
          } else { // BOTH FROM AND TO STATIONS
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsxSegment( pw, cave, f, t ); // branch prefix
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
        writeCsxSegment( pw, cave, f, t ); // branch prefix
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
          pw.format(Locale.US, "       <coordinate latv=\"%.7f\" longv=\"%.7f\" altv=\"%.2f\" lat=\"%.7f N\" long=\"%.7f E\" format=\"dd.ddddddd N\" alt=\"%.2f\" />\n",
             fix.lat, fix.lng, fix.asl, fix.lat, fix.lng, fix.asl );
          pw.format("     </trigpoint>\n");
        }
      }
      pw.format("  </trigpoints>\n");

      // ============= SKETCHES
      if ( sketch != null ) {
        sketch.exportAsCsx( pw, cave, branch );
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

  static private DistoXNum getGeolocalizedData( long sid, DataHelper data, float decl, float asl_factor )
  {
    // Log.v("DistoX", "get geoloc. data. Decl " + decl );
    List< FixedInfo > fixeds = data.selectAllFixed( sid, 0 );
    if ( fixeds.size() == 0 ) return null;

    DistoXNum num = null;
    FixedInfo origin = null;
    List<DistoXDBlock> shots_data = data.selectAllShots( sid, 0 );
    for ( FixedInfo fixed : fixeds ) {
      num = new DistoXNum( shots_data, fixed.name, null, null, decl );
      if ( num.getShots().size() > 0 ) {
        origin = fixed;
        break;
      }
    }
    if ( origin == null || num == null ) return null;

    float lat = (float)origin.lat;
    float lng = (float)origin.lng;
    float asl = (float)origin.asl; // KML uses Geoid altitude (unless altitudeMode is set)
    float alat = TDMath.abs( lat );

    float s_radius = ((90 - alat) * EARTH_RADIUS1 + alat * EARTH_RADIUS2)/90;
    float e_radius = s_radius * TDMath.cosd( alat );

    s_radius = 1 / s_radius;
    e_radius = 1 / e_radius;

    for ( NumStation st : num.getStations() ) {
      st.s = lat - st.s * s_radius;
      st.e = lng + st.e * e_radius;
      st.v = (asl - st.v)*asl_factor;
    }
    return num;
  }

  static String exportSurveyAsKml( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    // Log.v("DistoX", "export as KML " + filename );
    DistoXNum num = getGeolocalizedData( sid, data, info.declination, 1.0f );
    if ( num == null ) {
      TDLog.Error( "Failed PLT export: no geolocalized station");
      return "";
    }
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
          pw.format(Locale.US, "    <coordinates>%f,%f,%f</coordinates>\n", st.e, st.s, st.v );
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
          pw.format(Locale.US, "        %f,%f,%f\n", from.e, from.s, from.v );
          pw.format(Locale.US, "        %f,%f,%f\n", to.e, to.s, to.v );
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
      //   pw.format(Locale.US, "        %f,%f,%f\n", from.e, from.s, from.v );
      //   pw.format(Locale.US, "        %f,%f,%f\n", to.e, to.s, to.v );
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
      TDLog.Error( "Failed KML export: " + e.getMessage() );
      return null;
    }
  }

  // -------------------------------------------------------------------
  // TRACK FILE OZIEXPLORER

  static String exportSurveyAsPlt( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    // Log.v("DistoX", "export as trackfile: " + filename );
    DistoXNum num = getGeolocalizedData( sid, data, info.declination, TopoDroidUtil.M2FT );
    if ( num == null ) {
      TDLog.Error( "Failed PLT export: no geolocalized station");
      return "";
    }
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
          pw.format(Locale.US, "%f, %f,1, %f,%d,,\r\n", from.e, from.s, from.v, days );
        }
        pw.format(Locale.US, "%f,%f,0,%f,%d,,\r\n", to.e, to.s, to.v, days );
        last = to;
      }
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed PLT export: " + e.getMessage() );
      return null;
    }
  }

  // =======================================================================
  // POCKETTOPO EXPORT PocketTopo

  static String exportSurveyAsTop( long sid, DataHelper data, SurveyInfo info, DrawingActivity sketch, String origin, String filename )
  {
    // Log.v("DistoX", "export as pockettopo: " + filename );
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
            && ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
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

  static private void writeThLeg( PrintWriter pw, AverageLeg leg, float ul, float ua )
  {
    pw.format(Locale.US, "%.2f %.1f %.1f\n", leg.length() * ul, leg.bearing() * ua, leg.clino() * ua );
    leg.reset();
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // Therion scraps and maps
  static private void doTherionMaps( PrintWriter pw, SurveyInfo info, List< PlotInfo > plots )
  {
    if ( plots.size() == 0 ) return;
    for ( PlotInfo plt : plots ) {
        String extra = ((new File( TDPath.getSurveyPlotTh2File( info.name, plt.name ) )).exists())? "  #" : "  ##";
        pw.format("%s input \"%s-%s.th2\"\n", extra, info.name, plt.name );
    }
    pw.format("\n");
    for ( PlotInfo plt : plots ) {
      if ( PlotInfo.isSketch2D( plt.type ) ) {
        String extra = ((new File( TDPath.getSurveyPlotTh2File( info.name, plt.name ) )).exists())? "  #" : "  ##";
        pw.format("%s map m%s -projection %s\n", extra, plt.name, PlotInfo.projName[ plt.type ] );
        pw.format("%s   %s-%s\n", extra, info.name, plt.name );
        pw.format("%s endmap\n", extra );
      }
    }
    pw.format("\n");
  }

  static String exportSurveyAsTh( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    // Log.v("DistoX", "export as therion: " + filename );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;

    String uls = ( ul < 1.01f )? "meters"  : "feet"; // FIXME
    String uas = ( ua < 1.01f )? "degrees" : "grads";
    // String uls = TDSetting.mUnitLengthStr;
    // String uas = TDSetting.mUnitAngleStr;

    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    List< PlotInfo > plots  = data.selectAllPlots( sid, TopoDroidApp.STATUS_NORMAL );
    List< CurrentStation > stations = data.getStations( sid );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      BufferedWriter bw = new BufferedWriter( fw );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("# %s created by TopoDroid v %s\n\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );
      pw.format("survey %s -title \"%s\"\n", info.name, info.name );
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format("    # %s \n", info.comment );
      }
      pw.format("\n");
      
      if ( TDSetting.mTherionMaps ) doTherionMaps( pw, info, plots );

      pw.format("  centerline\n");

      if ( fixed.size() > 0 ) {
        pw.format("    cs long-lat\n");
        for ( FixedInfo fix : fixed ) {
          pw.format("    # fix %s\n", fix.toExportString() );
          if ( fix.hasCSCoords() ) {
            pw.format("    # cs %s\n", fix.csName() );
            pw.format("    # fix %s\n", fix.toExportCSString() );
          }
        }
      }
      pw.format("    date %s \n", info.date );
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("    # team %s \n", info.team );
      }

      pw.format(Locale.US, "    # declination %.2f degrees\n", info.declination );

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
      // mark ... 
      // station ...
      
      if ( stations.size() > 0 ) {
        pw.format("\n");
        StringBuilder sb_fixed   = new StringBuilder();
        StringBuilder sb_painted = new StringBuilder();
        for ( CurrentStation station : stations ) {
          if ( station.mFlag == CurrentStation.STATION_FIXED ) { 
            sb_fixed.append(" " + station.mName );
          } else if ( station.mFlag == CurrentStation.STATION_PAINTED ) {
            sb_painted.append(" " + station.mName );
          }
          pw.format("    station %s \"%s\"\n", station.mName, station.mComment );
        }
        String str_fixed   = sb_fixed.toString();
        String str_painted = sb_painted.toString();
        if ( str_fixed.length() > 0 ) {
          pw.format("    mark%s fixed\n", str_fixed );
        }
        if ( str_painted.length() > 0 ) {
          pw.format("    mark%s painted\n", str_painted );
        }
        pw.format("\n");
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
      // centerline data

      pw.format("    units length %s\n", uls );
      pw.format("    units compass clino %s\n", uas );

      pw.format("    data normal from to length compass clino\n");

      long extend = 0;  // current extend
      AverageLeg leg = new AverageLeg(0);

      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeThLeg( pw, leg, ul, ua );
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
            pw.format(Locale.US, "%.2f %.1f %.1f\n", item.mLength * ul, item.mBearing * ua, item.mClino * ua );
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
              writeThLeg( pw, leg, ul, ua );
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
            pw.format(Locale.US, "%.2f %.1f %.1f\n", item.mLength * ul, item.mBearing * ua, item.mClino * ua );
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeThLeg( pw, leg, ul, ua );
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
        // pw.format(Locale.US, "%.2f %.1f %.1f\n", item.mLength * ul, item.mBearing * ua, item.mClino * ua );
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeThLeg( pw, leg, ul, ua );
        if ( duplicate ) {
          pw.format(therion_flags_not_duplicate);
          // duplicate = false;
        }
      }

      pw.format("  endcenterline\n\n");

      if ( ! TDSetting.mTherionMaps ) doTherionMaps( pw, info, plots );

      pw.format("endsurvey\n");
      bw.flush();
      // fw.flush();
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
   *      *units compass clino grads|degrees
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

  static boolean writeSurvexLeg( PrintWriter pw, boolean first, boolean dup, AverageLeg leg, DistoXDBlock blk, float ul, float ua )
  {
    if ( first ) {
      pw.format(Locale.US, "  %.2f %.1f %.1f", leg.length() * ul, leg.bearing() * ua, leg.clino() * ua );
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

  static void writeSurvexLRUD( PrintWriter pw, String st, LRUD lrud, float ul )
  {
    if ( lrud != null ) {
      pw.format(Locale.US, "%s  %.2f %.2f %.2f %.2f", st, lrud.l * ul, lrud.r * ul, lrud.u * ul, lrud.d * ul );
      writeSurvexEOL( pw );
    }
  }

  static void writeSurvexSplay( PrintWriter pw, String from, String to, DistoXDBlock blk, float ul, float ua )
  {
    pw.format(Locale.US, "  %s %s %.2f %.1f %.1f", from, to, blk.mLength * ul, blk.mBearing * ua, blk.mClino * ua );
    if ( blk.mComment != null && blk.mComment.length() > 0 ) {
      pw.format(" ; %s", blk.mComment );
    }
    writeSurvexEOL( pw );
  }

  static String exportSurveyAsSvx( long sid, DataHelper data, SurveyInfo info, Device device, String filename )
  {
    // Log.v("DistoX", "export as survex: " + filename );
    char splayChar = 'a';

    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    String uls = ( ul < 1.01f )? "meters"  : "feet"; // FIXME
    String uas = ( ua < 1.01f )? "degrees" : "grads";

    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    List<DistoXDBlock> st_blk = new ArrayList<DistoXDBlock>(); // blocks with from station (for LRUD)

    // float decl = info.declination; // DECLINATION not used
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
      writeSurvexLine(pw, "  *units tape " + uls );
      writeSurvexLine(pw, "  *units compass " + uas );
      writeSurvexLine(pw, "  *units clino " + uas );
      pw.format(Locale.US, "  *calibrate declination %.2f", info.declination ); writeSurvexEOL(pw);
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
        AverageLeg leg = new AverageLeg(0);
        DistoXDBlock ref_item = null;
        boolean duplicate = false;
        boolean splays = false;
        for ( DistoXDBlock item : list ) {
          String from = item.mFrom;
          String to   = item.mTo;
          if ( from == null || from.length() == 0 ) {
            if ( to == null || to.length() == 0 ) { // no station: not exported
              if ( ref_item != null &&
                 ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
                leg.add( item.mLength, item.mBearing, item.mClino );
              }
            } else { // only TO station
              if ( leg.mCnt > 0 && ref_item != null ) {
                duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item, ul, ua );
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
                  writeSurvexSplay( pw, to + splayChar, to, item, ul, ua );
                } else {
                  writeSurvexSplay( pw, "-", to, item, ul, ua );
                }
              }
            }
          } else { // with FROM station
            if ( to == null || to.length() == 0 ) { // splay shot
              if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
                duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item, ul, ua );
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
                  writeSurvexSplay( pw, from, from + splayChar, item, ul, ua );
                } else {
                  writeSurvexSplay( pw, from, "-", item, ul, ua );
                }
              }
            } else {
              if ( leg.mCnt > 0 && ref_item != null ) {
                duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item, ul, ua );
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
          duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item, ul, ua );
          if ( TDSetting.mSurvexLRUD ) st_blk.add( ref_item );
          ref_item = null;
        }
        first = false;
      }
      writeSurvexLine(pw, "  *flags not splay");

      // Log.v("DistoX", "Station blocks " + st_blk.size() );
      if ( TDSetting.mSurvexLRUD && st_blk.size() > 0 ) {
        String from = null;
        for ( int k=0; k<st_blk.size(); ++k ) { 
          if ( st_blk.get(k).mFrom != null && st_blk.get(k).mFrom.length() > 0 ) {
            from = st_blk.get(k).mFrom;
            break;
          }
        }
        if ( from != null ) {
          boolean do_header = true;
          DistoXNum num = new DistoXNum( list, from, null, null, 0.0f ); // no declination
          List<NumBranch> branches = num.makeBranches( true );
          // Log.v("DistoX", "Station " + from + " shots " + num.shotsNr() + " splays " + num.splaysNr()
          //               + " branches " + branches.size() );

          for ( NumBranch branch : branches ) {
            // ArrayList<String> stations = new ArrayList<String>();
            ArrayList<NumShot> shots = branch.shots;
            int size = shots.size();
            // Log.v("DistoX", "branch shots " + size );
            if ( size > 0 ) {
              if ( do_header ) {
                pw.format("*data passage station left right up down"); writeSurvexEOL( pw );
                pw.format("*units left %s", uls );  writeSurvexEOL( pw );
                pw.format("*units right %s", uls ); writeSurvexEOL( pw );
                pw.format("*units up %s", uls );    writeSurvexEOL( pw );
                pw.format("*units down %s", uls );  writeSurvexEOL( pw );
                do_header = false;
              }
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
                  writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ), ul );
                  writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ), ul );
                } else {
                  writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ), ul );
                  writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ), ul );
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
                        writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ), ul );
                        writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ), ul );
                      } else {
                        writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ), ul );
                        writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ), ul );
                      }
                    } else if ( blk0.mTo.equals( blk.mFrom ) || blk0.mTo.equals( blk.mTo ) ) {
                      st_name = blk0.mTo;
                      if ( step > 0 ) {
                        writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ), ul );
                        writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ), ul );
                      } else {
                        writeSurvexLRUD( pw, blk0.mTo, computeLRUD( blk0, list, false ), ul );
                        writeSurvexLRUD( pw, blk0.mFrom, computeLRUD( blk0, list, true ), ul );
                      }
                    }
                  }
                  if ( st_name.equals( blk.mTo ) ) {
                    writeSurvexLRUD( pw, blk.mFrom, computeLRUD( blk, list, true ), ul );
                    st_name = blk.mFrom;
                  } else if ( st_name.equals( blk.mFrom ) ) {
                    writeSurvexLRUD( pw, blk.mTo, computeLRUD( blk, list, false ), ul );
                    st_name = blk.mTo;
                  } else {
                    TDLog.Error("ERROR unattached branch shot " + sh.from.name + " " + sh.to.name + " station " + st_name );
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
        //     writeSurvexLRUD( pw, blk.mFrom, computeLRUD( blk, list, true ), ul );
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
  static private void writeCsvLeg( PrintWriter pw, AverageLeg leg, float ul, float ua )
  {
    pw.format(Locale.US, ",%.2f,%.1f,%.1f", 
      leg.length() * ul, leg.bearing() * ua, leg.clino() * ua );
    leg.reset();
  }

  static String exportSurveyAsCsv( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    // Log.v("DistoX", "export as CSV: " + filename );
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    // List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    String uls = ( ul < 1.01f )? "meters"  : "feet"; // FIXME
    String uas = ( ua < 1.01f )? "degrees" : "grads";
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
      pw.format(Locale.US, "# from to tape compass clino (declination %.4f)\n", info.declination );
      pw.format(Locale.US, "# units tape %s compass clino %s\n", uls, uas );
      
      AverageLeg leg = new AverageLeg(0);
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean splays = false;
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsvLeg( pw, leg, ul, ua );
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
            pw.format(Locale.US, "-,%s@%s,%.2f,%.1f,%.1f\n", to, info.name,
              item.mLength * ul, item.mBearing * ua, item.mClino * ua );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", item.mComment );
            // }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              writeCsvLeg( pw, leg, ul, ua );
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
            pw.format(Locale.US, "%s@%s,-,%.2f,%.1f,%.1f\n", from, info.name,
              item.mLength * ul, item.mBearing * ua, item.mClino * ua );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", item.mComment );
            // }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsvLeg( pw, leg, ul, ua );
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
        writeCsvLeg( pw, leg, ul, ua );
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
  //             pw.format(Locale.US, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.US, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
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
  //             pw.format(Locale.US, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.US, "@ %.2f %.1f %.1fi %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             n = 0;
  //             ref_item = null;
  //             extend = 0;
  //             flag   = DistoXDBlock.BLOCK_SURVEY;
  //           }
  //           // item.Comment()
  //           pw.format("    \"%s\" \"\" ", from );
  //           pw.format(Locale.US, "%.2f %.1f %.1f %.1f %d %d 1\n",
  //             item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, item.mFlag );
  //         }
  //       } else { // from.isEmpty()
  //         if ( to != null && to.length() > 0 ) {
  //           if ( n > 0 /* && ref_item != null */ ) {
  //             b = TopoDroidUtil.in360( b/n );
  //             pw.format(Locale.US, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.US, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             n = 0;
  //             ref_item = null;
  //             extend = 0;
  //             flag   = DistoXDBlock.BLOCK_SURVEY;
  //           }
  //           // item.Comment()
  //           pw.format("    \"\" \"%s\" ", to );
  //           pw.format(Locale.US, "%.2f %.1f %.1f %.1f %d %d 1\n",
  //             item.mLength, item.mBearing, item.mClino, item.mRoll, item.mExtend, item.mFlag );
  //         } else {
  //           // not exported
  //           if ( ref_item != null &&
  //              ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
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
  //       pw.format(Locale.US, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
  //       while ( n > 0 ) {
  //         -- n;
  //         pw.format(Locale.US, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //       }
  //       // extend = 0;
  //       // flag   = DistoXDBlock.BLOCK_SURVEY;
  //     }
  //     // pw.format(Locale.US, "%.2f %.1f %.1f %.1f %d %d %d\n", 
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
   *    FROM TO LENGTH BEARING INC LEFT UP DOWN RIGHT FLAGS COMMENTS
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
    pw.format(Locale.US, "%.2f %.1f %.1f %.2f %.2f %.2f %.2f", 
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

  static private void writeDatFromTo( PrintWriter pw, String prefix, String from, String to )
  {
    if ( TDSetting.mExportStationsPrefix ) {
      pw.format("%s-%s %s-%s ", prefix, from, prefix, to );
    } else {
      pw.format("%s %s ", from, to );
    }
  }

  static String exportSurveyAsDat( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    // Log.v("DistoX", "export as compass: " + filename );
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
      pw.format(Locale.US, "DECLINATION: %.4f  ", info.declination );
      pw.format("FORMAT: DMMDLUDRLADN  CORRECTIONS:  0.00 0.00 0.00\r\n" );
      pw.format("\r\n" );
      pw.format("FROM TO LENGTH BEARING INC LEFT UP DOWN RIGHT FLAGS COMMENTS\r\n" );
      pw.format( "\r\n" );

      AverageLeg leg = new AverageLeg(0);
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
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeDatFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo );
              printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              lrud = computeLRUD( ref_item, list, true );
              writeDatFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo );
              printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeDatFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo );
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
        writeDatFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo );
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

  static private void writeSrvLeg( PrintWriter pw, AverageLeg leg, float ul, float ua )
  {
    pw.format(Locale.US, "%.2f\t%.1f\t%.1f", leg.length() * ul, leg.bearing() * ua, leg.clino() * ua );
    leg.reset();
  }
 
  static String exportSurveyAsSrv( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    // Log.v("DistoX", "export as walls: " + filename );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    String uls = ( ul < 1.01f )? "Meters"  : "Feet"; // FIXME
    String uas = ( ua < 1.01f )? "Degrees" : "Grads";

    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      pw.format("; %s\n", info.name );
      pw.format("; created by TopoDroid v %s - %s \n", TopoDroidApp.VERSION, TopoDroidUtil.getDateString("yyyy.MM.dd") );
      pw.format(Locale.US, "#Units Decl=%.1f\n", info.declination );

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
          TDLog.Error( "exportSurveyAsSrv date parse error " + date );
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
            pw.format(Locale.US, " E%.6f", fix.lng );
          } else {
            pw.format(Locale.US, " W%.6f", - fix.lng );
          }
          if ( fix.lat >= 0 ) {
            pw.format(Locale.US, " N%.6f", fix.lat );
          } else {
            pw.format(Locale.US, " S%.6f", - fix.lat );
          }
          pw.format(Locale.US, " %.0f", fix.asl );
          if ( fix.comment != null && fix.comment.length() > 0 ) pw.format(" /%s", fix.comment );
          pw.format("\n");
        }
      } 

      pw.format("#Units %s A=%s\n", uls, uas );

      List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
      // int extend = 1;
      AverageLeg leg = new AverageLeg(0);
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO
      for ( DistoXDBlock item : list ) {
        String from    = item.mFrom;
        String to      = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeSrvLeg( pw, leg, ul, ua );
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
            pw.format(Locale.US, "%.2f\t%.1f\t%.1f", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("\t; %s\n", item.mComment );
            } else {
              pw.format("\n");
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
              writeSrvLeg( pw, leg, ul, ua );
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
            pw.format(Locale.US, "%.2f\t%.1f\t%.1f", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("\t; %s\n", item.mComment );
            } else {
              pw.format("\n");
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeSrvLeg( pw, leg, ul, ua );
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
        // pw.format(Locale.US, "%.2f\t%.1f\t%.1f\n", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeSrvLeg( pw, leg, ul, ua );
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

  // =======================================================================
  // TOPO EXPORT ( CAV )

  private static long printFlagToCav( PrintWriter pw, long old_flag, long new_flag, String eol )
  {
    if ( old_flag == new_flag ) return old_flag;
    if ( old_flag == DistoXDBlock.BLOCK_DUPLICATE ) {
      pw.format("#end_duplicate%s", eol);
    } else if ( old_flag == DistoXDBlock.BLOCK_SURFACE ) {
      pw.format("#end_surface%s", eol);
    }
    if ( new_flag == DistoXDBlock.BLOCK_DUPLICATE ) {
      pw.format("#duplicate%s", eol);
    } else if ( new_flag == DistoXDBlock.BLOCK_SURFACE ) {
      pw.format("#surface%s", eol);
    }
    return new_flag;
  }

  private static void printShotToCav( PrintWriter pw, AverageLeg leg, DistoXDBlock item, String eol, ArrayList ents )
  {
    if ( ents != null ) {
      int s = ents.size();
      for ( int k = 0; k < s; ++ k ) {
        if ( ents.get(k).equals( item.mFrom ) ) {
          pw.format("#ent%s", eol );
          ents.remove( k );
          break;
        }
      }
    }
    pw.format(Locale.US, "%s\t%s\t%.2f\t%.1f\t%.1f", item.mFrom, item.mTo, leg.length(), leg.bearing(), leg.clino() );
    if ( item.mComment != null ) {
      pw.format("\t;\t%s%s", item.mComment, eol );
    } else {
      pw.format("%s", eol );
    }
    leg.reset();
  }

  private static void printSplayToCav( PrintWriter pw, DistoXDBlock blk, String eol )
  {
    // if ( duplicate ) pw.format("#duplicate%s", eol);
    pw.format(Locale.US, "%s\t-\t%.2f\t%.1f\t%.1f", blk.mFrom, blk.mLength, blk.mBearing, blk.mClino );
    if ( blk.mComment != null ) {
      pw.format("\t;\t%s%s", blk.mComment, eol );
    } else {
      pw.format("%s", eol );
    }
    // if ( duplicate ) pw.format("#end_duplicate%s", eol);
  }

  static long printCavExtend( PrintWriter pw, long extend, long item_extend, String eol )
  {
    if ( item_extend != extend ) { 
      if ( item_extend == DistoXDBlock.EXTEND_LEFT ) {
        pw.format("#R180%s", eol );
      } else if ( item_extend == DistoXDBlock.EXTEND_RIGHT ) {
        pw.format("#R0%s", eol );
      } else if ( item_extend == DistoXDBlock.EXTEND_VERT ) {
        pw.format("#PR[0]%s", eol );
      }
      return item_extend;
    }
    return extend;
  }

  static String exportSurveyAsCav( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    // Log.v("DistoX", "export as topo: " + filename );
    String eol = TDSetting.mSurvexEol;
    ArrayList< String > ents = null;

    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("#cave %s%s", info.name, eol );
      pw.format("%% Made by: TopoDroid %s - %s%s", TopoDroidApp.VERSION, TopoDroidUtil.currentDate(), eol );

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
          TDLog.Error( "exportSurveyAsSrv date parse error " + date );
        }
      }
      pw.format("#survey ^%s%s", info.name, eol );
      if ( info.team != null ) pw.format("#survey_team %s%s", info.team, eol );
      pw.format("#survey_date %02d.%02d.%04d%s", d, m, y, eol ); 
      if ( info.comment != null ) pw.format("#survey_title %s%s", info.comment, eol );

      pw.format(Locale.US, "#declination[%.1f]%s", info.declination, eol );
      
      List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
      if ( fixed.size() > 0 ) {
        ents = new ArrayList< String >();
        for ( FixedInfo fix : fixed ) {
          ents.add( fix.name );
          pw.format(";\t#point\tPoint%s\t", fix.name );
          pw.format(Locale.US, "%.6f\t%.6f\t%.0f%s", fix.lng, fix.lat, fix.asl, eol );
          break;
        }
      }

      pw.format("#data_order L Az An%s", eol);
      pw.format("#from_to%s", eol);
      pw.format("#R0%s", eol);

      List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
      AverageLeg leg = new AverageLeg(0);
      DistoXDBlock ref_item = null;

      long extend = 1;
      long flag = 0;
      // boolean in_splay = false;
      // LRUD lrud;

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
              ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              flag = printFlagToCav( pw, flag, ref_item.mFlag, eol );
              printShotToCav( pw, leg, ref_item, eol, ents );
              ref_item = null; 
            }
            extend = printCavExtend( pw, extend, item.mExtend, eol );
            // TODO export TO splay
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              flag = printFlagToCav( pw, flag, ref_item.mFlag, eol );
              printShotToCav( pw, leg, ref_item, eol, ents );
              ref_item = null; 
            }
            extend = printCavExtend( pw, extend, item.mExtend, eol );
            printSplayToCav( pw, item, eol );
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              flag = printFlagToCav( pw, flag, ref_item.mFlag, eol );
              printShotToCav( pw, leg, ref_item, eol, ents );
            }
            ref_item = item;
            extend = printCavExtend( pw, extend, item.mExtend, eol );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        flag = printFlagToCav( pw, flag, ref_item.mFlag, eol );
        printShotToCav( pw, leg, ref_item, eol, ents );
      }      
      flag = printFlagToCav( pw, flag, 0, eol );
      pw.format( "%s", eol );
      pw.format("#end_declination%s", eol);
      pw.format("#end_survey%s", eol);

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Polygon export: " + e.getMessage() );
      return null;
    }
  }

  // =======================================================================
  // POLYGON EXPORT 

  private static void printShotToPlg( PrintWriter pw, AverageLeg leg, LRUD lrud, boolean duplicate, String comment )
  {
    pw.format(Locale.US, "%.2f\t%.1f\t%.1f\t\t%.2f\t%.2f\t%.2f\t%.2f\t", 
      leg.length(), leg.bearing(), leg.clino(), lrud.l, lrud.r, lrud.u, lrud.d );
    leg.reset();
    // if ( duplicate ) { pw.format(" #|L#"); }
    if ( comment != null && comment.length() > 0 ) {
      pw.format("%s", comment );
    }
    pw.format( "\n" );
  }
 
  static String exportSurveyAsPlg( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    // Log.v("DistoX", "export as polygon: " + filename );
    float ul = 1; // TDSetting.mUnitLength;
    float ua = 1; // TDSetting.mUnitAngle;
    // String uls = ( ul < 1.01f )? "Meters"  : "Feet"; // FIXME
    // String uas = ( ua < 1.01f )? "Degrees" : "Grads";

    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("POLYGON Cave Surveying Software\n");
      pw.format("Polygon Program Version   = 2\n");
      pw.format("Polygon Data File Version = 1\n");
      pw.format("1998-2001 ===> Prepostffy Zsolt\n");
      pw.format("-------------------------------\n\n");

      pw.format("*** Project ***\n");
      pw.format("Project name: %s\n", info.name );
      pw.format("Project place: %s\n", info.name );
      pw.format("Project code: 9999\n");
      pw.format("Made by: TopoDroid %s\n", TopoDroidApp.VERSION );
      pw.format(Locale.US, "Made date: %f\n", TopoDroidUtil.getDatePlg() );
      pw.format("Last modi: 0\n");
      pw.format("AutoCorrect: 1\n");
      pw.format("AutoSize: 20.0\n\n");

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
          TDLog.Error( "exportSurveyAsSrv date parse error " + date );
        }
      }
      pw.format("*** Surveys ***\n");
      pw.format("Survey name: %s\n", info.name );
      pw.format("Survey team:\n");
      pw.format("%s\n\t\n\t\n\t\n\t\n", (info.team != null)? info.team : "" );
      pw.format(Locale.US, "Survey date: %f\n", TopoDroidUtil.getDatePlg( y, m, d ) );
      pw.format(Locale.US, "Declination: %.1f\n", info.declination );
      pw.format("Instruments:\n\t0\n\t0\n\t0\n");

      // if ( info.comment != null ) {
      //   pw.format("; %s\n", info.comment );
      // }

      List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
      if ( fixed.size() > 0 ) {
        for ( FixedInfo fix : fixed ) {
          pw.format("Fix point: %s\n", fix.name );
          pw.format(Locale.US, "%.6f\t%.6f\t%.0f\t0\t0\t0\t0\n", fix.lng, fix.lat, fix.asl );
          break;
        }
      } else {
        pw.format("Fix point: 0\n" );
        pw.format(Locale.US, "0\t0\t0\t0\t0\t0\t0\n" );
      }

      pw.format("Survey data\n");
      pw.format("From\tTo\tLength\tAzimuth\tVertical\tLabel\tLeft\tRight\tUp\tDown\tNote\n");

      List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );

      AverageLeg leg = new AverageLeg(0);
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
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              pw.format("%s\t%s\t", ref_item.mFrom, ref_item.mTo );
              printShotToPlg( pw, leg, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              lrud = computeLRUD( ref_item, list, true );
              pw.format("%s\t%s\t", ref_item.mFrom, ref_item.mTo );
              printShotToPlg( pw, leg, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              pw.format("%s\t%s\t", ref_item.mFrom, ref_item.mTo );
              printShotToPlg( pw, leg, lrud, duplicate, ref_item.mComment );
            }
            ref_item = item;
            duplicate = ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        lrud = computeLRUD( ref_item, list, true );
        pw.format("%s\t%s\t", ref_item.mFrom, ref_item.mTo );
        printShotToPlg( pw, leg, lrud, duplicate, ref_item.mComment );
      }
      pw.format( "\n" );
      pw.format("End of survey data.\n\n");
      pw.format("*** Surface ***\n");
      pw.format("End of surface data.\n\n");
      pw.format("EOF.\n");

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Polygon export: " + e.getMessage() );
      return null;
    }
  }


  // -----------------------------------------------------------------------
  // DXF EXPORT 
  // NOTE declination is taken into account in DXF export (used to compute num)

  static String exportSurveyAsDxf( long sid, DataHelper data, SurveyInfo info, DistoXNum num, String filename )
  {
    // Log.v("DistoX", "export as DXF: " + filename );
    // Log.v( TAG, "export SurveyAsDxf " + filename );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter out = new PrintWriter( fw );
      // TODO
      out.printf(Locale.US, "999\nDXF created by TopoDroid v %s - %s (declination %.4f)\n",
        TopoDroidApp.VERSION, TopoDroidUtil.getDateString("yyyy.MM.dd"), info.declination );
      out.printf("0\nSECTION\n2\nHEADER\n");
      out.printf("9\n$ACADVER\n1\nAC1006\n");
      out.printf("9\n$INSBASE\n");
      out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", 0.0, 0.0, 0.0 ); // FIXME (0,0,0)
      out.printf("9\n$EXTMIN\n");
      float emin = num.surveyEmin() - 2.0f;
      float nmin = - num.surveySmax() - 2.0f;
      float zmin = - num.surveyVmax() - 2.0f;
      out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        // num.surveyEmin(), -num.surveySmax(), -num.surveyVmax() );
      out.printf("9\n$EXTMAX\n");
      float emax = num.surveyEmax();
      float nmax = - num.surveySmin();
      float zmax = - num.surveyVmin();
      
      int de = (100f < emax-emin )? 100 : (50f < emax-emin)? 50 : 10;
      int dn = (100f < nmax-nmin )? 100 : (50f < nmax-nmin)? 50 : 10;
      int dz = (100f < zmax-zmin )? 100 : (50f < zmax-zmin)? 50 : 10;

      out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emax, nmax, zmax );
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
          out.printf("0\nLAYER\n2\nPOINT\n70\n%d\n62\n%d\n6\n%s\n",   flag, 5, style );
        out.printf("0\nENDTAB\n");

        out.printf("0\nTABLE\n2\nSTYLE\n70\n0\n");
        out.printf("0\nENDTAB\n");
      }
      out.printf("0\nENDSEC\n");

      out.printf("0\nSECTION\n2\nBLOCKS\n");
      if ( TDSetting.mDxfBlocks ) { // DXF_BLOCKS
        out.printf("0\nBLOCK\n");
        out.printf("8\nPOINT\n");
        out.printf("2\npoint\n70\n0\n"); // flag 0
        out.printf("10\n0.0\n20\n0.0\n30\n0.0\n");
        out.printf("0\nLINE\n8\nPOINT\n");
        out.printf("  10\n  -0.3\n  20\n 0.0\n  30\n0.0\n");
        out.printf("  11\n  0.3\n  21\n 0.0\n  31\n0.0\n");
        out.printf("0\nLINE\n8\nPOINT\n");
        out.printf("  10\n  0.0\n  20\n -0.3\n  30\n0.0\n");
        out.printf("  11\n  0.0\n  21\n 0.3\n  31\n0.0\n");
        out.printf("0\nENDBLK\n");
      }
      out.printf("0\nENDSEC\n");

      out.printf("0\nSECTION\n2\nENTITIES\n");
      {
        emin += 1f;
        nmin += 1f;
        zmin += 1f;
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.US, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin+de, nmin, zmin );
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.US, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin, nmin+dn, zmin );
        out.printf("0\nLINE\n8\nREF\n");
        out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        out.printf(Locale.US, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", emin, nmin, zmin+dz );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (de==100)? "100" : (de==50)? "50" : "10" );
        out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin+de+1, nmin, zmin );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (dn==100)? "100" : (dn==50)? "50" : "10" );
        out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin, nmin+de+1, zmin );
        out.printf("0\nTEXT\n8\nREF\n");
        out.printf("1\n%s\n", (dz==100)? "100" : (dz==50)? "50" : "10" );
        out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", emin, nmin, zmin+dz+1 );

        // centerline data
        for ( NumShot sh : num.getShots() ) {
          NumStation f = sh.from;
          NumStation t = sh.to;
          out.printf("0\nLINE\n8\nLEG\n");
          out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, -f.s, -f.v );
          out.printf(Locale.US, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", t.e, -t.s, -t.v );
        }

        for ( NumSplay sh : num.getSplays() ) {
          NumStation f = sh.from;
          out.printf("0\nLINE\n8\nSPLAY\n");
          out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, -f.s, -f.v );
          out.printf(Locale.US, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", sh.e, -sh.s, -sh.v );
          if ( TDSetting.mDxfBlocks ) {
            out.printf("0\nINSERT\n8\nPOINT\n2\npoint\n");
            // out.printf("41\n1\n42\n1\n") // point scale
            // out.printf("50\n0\n");  // orientation
            out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", sh.e, -sh.s, -sh.v );
          }
        }
   
        for ( NumStation st : num.getStations() ) {
          // FIXME station scale is 0.3
          out.printf("0\nTEXT\n8\nSTATION\n");
          out.printf("1\n%s\n", st.name );
          out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", st.e, -st.s, -st.v );
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
    pw.format(Locale.US, "%s %s 0.00 0.00 0.00 ", item.mFrom, item.mFrom );
    pw.format(Locale.US, "%.2f %.2f %.2f %.2f N I", lrud.l, lrud.r, lrud.u, lrud.d );
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
    pw.format(Locale.US, "%.2f %.1f %.1f ", leg.length(), leg.bearing(), leg.clino() );
    leg.reset();
    pw.format(Locale.US, "%.2f %.2f %.2f %.2f N I", lrud.l, lrud.r, lrud.u, lrud.d );
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
    // Log.v("DistoX", "export as visualtopo: " + filename );
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      pw.format("Version 5.02\r\n\r\n");
      pw.format("; %s created by TopoDroid v %s\r\n\r\n", TopoDroidUtil.getDateString("yyyy.MM.dd"), TopoDroidApp.VERSION );
      if ( fixed.size() > 0 ) { 
        FixedInfo fix = fixed.get(0);
        pw.format(Locale.US, "; Trou %s,%.7f,%.7f,LT2E\r\n", info.name, fix.lat, fix.lng );
        pw.format("; Entree %s\r\n", fix.name );
      } else {
        pw.format("; Trou %s\r\n", info.name );
      }
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("Club %s\r\n", info.team );
      }
      pw.format("Couleur 0,0,0\r\n\r\n");
      
      pw.format(Locale.US, "Param Deca Degd Clino Degd %.4f Dir,Dir,Dir Arr Inc 0,0,0\r\n\r\n", info.declination );

      AverageLeg leg = new AverageLeg(0);
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
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.isRelativeDistance( ref_item ) ) ) {
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

  // --------------------------------------------------------------------
  // CALIBRATION import/export

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
        pw.format(Locale.US, "%d, %d, %d, %d, %d, %d, %d, %d, %.2f, %.2f, %.2f, %.4f, %d\n",
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
              long cid = data.insertCalibInfo( name, date, device, comment, algo );
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
