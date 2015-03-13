/** @file TopoDroidExporter.java
 *
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

class TopoDroidExporter
{
  final static int EXPORT_THERION    = 0;
  final static int EXPORT_COMPASS    = 1;
  final static int EXPORT_CSURVEY    = 2;
  final static int EXPORT_POCKETTOPO = 3;
  final static int EXPORT_SURVEX     = 4;
  final static int EXPORT_VISUALTOPO = 5;
  final static int EXPORT_WALLS      = 6;
  final static int EXPORT_CSV        = 10;
  final static int EXPORT_DXF        = 11;
  final static int EXPORT_PNG        = 12;
  final static int EXPORT_SVG        = 13;
  final static int EXPORT_ZIP        = 20;

  // =======================================================================
  // CSUREVY EXPORT cSurvey

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

  static String exportSurveyAsCsx( long sid, DataHelper data, SurveyInfo info, DrawingActivity sketch, String origin, String filename )
  {
    String cave = info.name.toUpperCase();

    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    List< PlotInfo > plots  = data.selectAllPlots( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      // SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd", Locale.US );
      // pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );

      pw.format("<csurvey version=\"1.04\" id=\"\">\n");

// ++++++++++++++++ PROPERTIES
      // FIXME origin = origin of Num
      pw.format("  <properties id=\"\" name=\"\" origin=\"%s-%s\" ", cave, origin );
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
        pw.format("    <gps enabled=\"0\" refpointonorigin=\"%s-%s\" geo=\"WGS84\" format=\"\" sendtotherion=\"0\" />\n",
                  cave,  origin );
      }

      pw.format("  </properties>\n");

// ++++++++++++++++ SHOTS
      pw.format("  <segments>\n");

      // optional attrs of "segment": id cave branch session

      int cntSplay = 0;     // splay counter (index)
      long extend = 0;      // current extend
      boolean dup = false;  // duplicate
      boolean sur = false;  // surface
      String com = null;    // comment
      String f="", t="";          // from to stations
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;
      int n = 0;
      DistoXDBlock ref_item = null;

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidSetting.mCloseDistance ) ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s-%s\" to=\"%s-%s\"", cave, cave, f, cave, t );
              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
              }
              // pw.format(" planshowsplayborder=\"1\" profileshowsplayborder=\"1\" ");
              pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", l/n, b, c/n );
              pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\" />\n");
              if ( com != null && com.length() > 0 ) {
                pw.format("<!-- comment=\"%s\" -->\n", com );
                com = null;
              }
              n = 0;
              ref_item = null; 
            }

            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            pw.format("<segment id=\"\" cave=\"%s\" from=\"%s-%s(%d)\" to=\"%s-%s\"", cave, cave, to, cntSplay, cave, to );
            ++ cntSplay;
            pw.format(" splay=\"1\" exclude=\"1\"");
            if ( extend == -1 ) pw.format(" direction=\"1\"");
            pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
               item.mLength, item.mBearing, item.mClino );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\" />\n");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("<!-- comment=\"%s\" -->\n", item.mComment);
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // ONLY FROM STATION : splay shot
            if ( n > 0 && ref_item != null ) { // finish writing previous leg shot
              b = TopoDroidUtil.in360( b/n );
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s-%s\" to=\"%s-%s\"", cave, cave, f, cave, t );
              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
              }
              // pw.format(" planshowsplayborder=\"1\" profileshowsplayborder=\"1\" ");
              pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", l/n, b, c/n );
              pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\" />\n");
              if ( com != null && com.length() > 0 ) {
                pw.format("<!-- comment=\"%s\" -->\n", com );
                com = null;
              }
              n = 0;
              ref_item = null; 
            }

            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            pw.format("<segment id=\"\" cave=\"%s\" from=\"%s-%s\" to=\"%s-%s(%d)\"", cave, cave, from, cave, from, cntSplay );
            ++cntSplay;
            pw.format(" splay=\"1\" exclude=\"1\"");
            if ( extend == -1 ) pw.format(" direction=\"1\"");
            pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
               item.mLength, item.mBearing, item.mClino );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\" />\n");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("<!-- comment=\"%s\" -->\n", item.mComment);
            }
          } else { // BOTH FROM AND TO STATIONS
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format("<segment id=\"\" cave=\"%s\" from=\"%s-%s\" to=\"%s-%s\" ", cave, cave, f, cave, t );
              if ( extend == -1 ) pw.format(" direction=\"1\"");
              if ( dup || sur ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
              }
              pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", l/n, b, c/n );
              pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\" />\n");
              if ( com != null && com.length() > 0 ) {
                pw.format("<!-- comment=\"%s\" -->\n", com);
                com = null;
              }
              // n = 0;
            }
            n = 1;
            ref_item = item;
            if ( item.mExtend != extend ) {
              extend = item.mExtend;
            }
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              dup = true;
            } else if ( item.mFlag == DistoXDBlock.BLOCK_SURFACE ) {
              sur = true;
            }
            f = from;
            t = to;
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
            com = item.mComment;
          }
        }
      }
      if ( n > 0 && ref_item != null ) {
        b = TopoDroidUtil.in360( b/n );
        pw.format("<segment id=\"\" cave=\"%s\" from=\"%s-%s\" to=\"%s-%s\" ", cave, cave, f, cave, t );
        if ( extend == -1 ) pw.format(" direction=\"1\"");
        if ( dup || sur ) {
           pw.format(" exclude=\"1\"");
           if ( dup ) { pw.format(" duplicate=\"1\""); /* dup = false; */ }
           if ( sur ) { pw.format(" surface=\"1\"");   /* sur = false; */ }
        }
        pw.format(Locale.ENGLISH, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", l/n, b, c/n );
        pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\" />\n");
        if ( com != null && com.length() > 0 ) {
          pw.format("<!-- comment=\"%s\" -->\n", com);
          // com = null;
        }
      }
      pw.format("  </segments>\n");

      // ============= TRIG POINTS
      pw.format("  <trigpoints>\n");
      if ( fixed.size() > 0 ) {
        for ( FixedInfo fix : fixed ) {
          pw.format("     <trigpoint name=\"%s\" labelsymbol\"0\" >\n", fix.name );
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
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed cSurvey export: " + e.getMessage() );
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
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "exportSurveyAsTop date parse error " + info.date );
    }

    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    long extend = 0;  // current extend

    int n = 0;
    DistoXDBlock ref_item = null;
    for ( DistoXDBlock item : list ) {
      String from = item.mFrom;
      String to   = item.mTo;
      extend = item.mExtend;
      if ( from == null || from.length() == 0 ) {
        from = "";
        if ( to == null || to.length() == 0 ) {
          to = "";
          if ( ref_item != null 
            && ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidSetting.mCloseDistance ) ) {
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
      TopoDroidApp.checkPath( filename );
      FileOutputStream fos = new FileOutputStream( filename );
      ptfile.write( fos );
      fos.close();
    } catch ( IOException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed PocketTopo export: " + e.getMessage() );
    }
    return filename;
  }
  // =======================================================================
  // THERION EXPORT Therion

  static String exportSurveyAsTh( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    List< PlotInfo > plots  = data.selectAllPlots( sid, TopoDroidApp.STATUS_NORMAL );
    List< CurrentStation > stations = data.getStations( sid );
    try {
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );
      pw.format("survey %s -title \"%s\"\n", info.name, info.name );
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format("    # %s \n", info.comment );
      }
      pw.format("\n");
      pw.format("  centerline\n");

      if ( fixed.size() > 0 ) {
        pw.format("    cs long-lat\n");
        for ( FixedInfo fix : fixed ) {
          pw.format("    # fix %s m\n", fix.toString() );
        }
      }
      pw.format("    date %s \n", info.date );
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("    # team %s \n", info.team );
      }

      pw.format(Locale.ENGLISH, "    declination %.2f degrees\n", info.declination );

      pw.format("    data normal from to length compass clino\n");

      long extend = 0;  // current extend
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;

      int n = 0;
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidSetting.mCloseDistance ) ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
              n = 0;
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
            if ( n > 0 && ref_item != null ) { // finish writing previous leg shot
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
              n = 0;
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
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(therion_flags_not_duplicate);
                duplicate = false;
              }
              if ( surface ) {
                pw.format(therion_flags_not_surface);
                surface = false;
              }
              // n = 0;
            }
            n = 1;
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
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            pw.format("    %s %s ", from, to );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
          }
        }
        // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
      }
      if ( n > 0 && ref_item != null ) {
        b = TopoDroidUtil.in360( b/n );
        pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
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
        pw.format("  # input \"%s-%s.th2\"\n", info.name, plt.name );
      }
      pw.format("\n");
      for ( PlotInfo plt : plots ) {
        if ( plt.type == PlotInfo.PLOT_PLAN || plt.type == PlotInfo.PLOT_EXTENDED ) {
          pw.format("  # map m%s -projection %s\n", plt.name, PlotInfo.projName[ plt.type ] );
          pw.format("  #   %s-%s\n", info.name, plt.name );
          pw.format("  # endmap\n" );
        }
      }

      pw.format("endsurvey\n");
      fw.flush();
      fw.close();

      // (new File( filename )).setReadable( true, false );

      return filename;
    } catch ( IOException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed Therion export: " + e.getMessage() );
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
  static String   survex_flags_duplicate     = "   *flags duplicate\n";
  static String   survex_flags_not_duplicate = "   *flags not duplicate\n";
  static String   survex_flags_surface       = "   *flags surface\n";
  static String   survex_flags_not_surface   = "   *flags not surface\n";

  static String exportSurveyAsSvx( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("; %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );

      pw.format("*begin %s\n", info.name );
      pw.format("  *units tape meters\n");
      pw.format("  *units compass degrees\n");
      pw.format("  *units clino degrees\n");
      pw.format(Locale.ENGLISH, "  *calibrate declination %.2f\n", info.declination );
      pw.format("; %s \n\n", info.comment );
      pw.format("  *date %s \n", info.date );
      pw.format("  ; *team \"%s\" \n", info.team );
      if ( fixed.size() > 0 ) {
        pw.format("  ; fix stations as lomg-lat alt\n");
        for ( FixedInfo fix : fixed ) {
          pw.format("  ; *fix %s\n", fix.toString() );
        }
      }
      pw.format("  *data normal from to tape compass clino\n");
      
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;
      int n = 0;
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean splays = false;
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidSetting.mCloseDistance ) ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "  %.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(survex_flags_not_duplicate);
                duplicate = false;
              }
              n = 0;
              ref_item = null; 
            }
            if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
              pw.format("  ; %s\n", ref_item.mComment );
            }

            if ( ! splays ) {
              pw.format("  *flags splay\n" );
              splays = true;
            }
            pw.format(Locale.ENGLISH, "  - %s %.2f %.1f %.1f\n", to, item.mLength, item.mBearing, item.mClino );
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  ; %s\n", item.mComment );
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 && ref_item != null ) { // write pervious leg shot
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "  %.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(survex_flags_not_duplicate);
                duplicate = false;
              }
              n = 0;
              ref_item = null; 
            }
            if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
              pw.format("  ; %s\n", ref_item.mComment );
            }

            if ( ! splays ) {
              pw.format("  *flags splay\n" );
              splays = true;
            }
            pw.format(Locale.ENGLISH, "  %s - %.2f %.1f %.1f\n", from, item.mLength, item.mBearing, item.mClino );
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  ; %s\n", item.mComment );
            }
          } else {
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(survex_flags_not_duplicate);
                duplicate = false;
              }
              // n = 0;
            }
            if ( splays ) {
              pw.format("  *flags not splay\n");
              splays = false;
            }
            n = 1;
            ref_item = item;
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              pw.format(survex_flags_duplicate);
              duplicate = true;
            }
            pw.format("    %s %s ", from, to );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
          }
        }
        // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
      }
      if ( n > 0 && ref_item != null ) {
        b = TopoDroidUtil.in360( b/n );
        pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", l/n, b, c/n );
        if ( duplicate ) {
          pw.format(survex_flags_not_duplicate);
          // duplicate = false;
        }
      }
      pw.format("*end %s\n", info.name );
      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed Survex export: " + e.getMessage() );
      return null;
    }
  }

  // -----------------------------------------------------------------------
  /** COMMA-SEPARATED VALUES EXPORT 
   *  NOTE declination exported in comment only in CSV
   *
   */
  static String exportSurveyAsCsv( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    // List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );

      pw.format("# %s\n", info.name );
      // if ( fixed.size() > 0 ) {
      //   pw.format("  ; fix stations as lomg-lat alt\n");
      //   for ( FixedInfo fix : fixed ) {
      //     pw.format("  ; *fix %s\n", fix.toString() );
      //   }
      // }
      pw.format(Locale.ENGLISH, "# from to tape compass clino (declination %.4f)\n", info.declination );
      
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;
      int n = 0;
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean splays = false;
      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidSetting.mCloseDistance ) ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, ",%.2f,%.1f,%.1f", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(",L");
                duplicate = false;
              }
              pw.format("\n");
              n = 0;
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.ENGLISH, "-,%s@%s,%.2f,%.1f,%.1f\n", to, info.name, item.mLength, item.mBearing, item.mClino );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", item.mComment );
            // }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 && ref_item != null ) { // write pervious leg shot
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, ",%.2f,%.1f,%.1f", l/n, b, c/n );
              if ( duplicate ) {
                pw.format(",L");
                duplicate = false;
              }
              pw.format("\n");
              n = 0;
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.ENGLISH, "%s@%s,-,%.2f,%.1f,%.1f\n", from, info.name, item.mLength, item.mBearing, item.mClino );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", item.mComment );
            // }
          } else {
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f,%.1f,%.1f", l/n, b, c/n );
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
            n = 1;
            ref_item = item;
            if ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE ) {
              duplicate = true;
            }
            pw.format("%s@%s,%s@%s", from, info.name, to, info.name );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
          }
        }
        // pw.format(Locale.ENGLISH, "%.2f %.1f %.1f\n", item.mLength, item.mBearing, item.mClino );
      }
      if ( n > 0 && ref_item != null ) {
        b = TopoDroidUtil.in360( b/n );
        pw.format(Locale.ENGLISH, ",%.2f,%.1f,%.1f", l/n, b, c/n );
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
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed CSV export: " + e.getMessage() );
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
  //     TopoDroidApp.checkPath( filename );
  //     FileWriter fw = new FileWriter( filename );
  //     PrintWriter pw = new PrintWriter( fw );
  //     pw.format("tlx2\n");
  //     SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
  //     pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );
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
  //              ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidApp.mCloseDistance ) ) {
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
  //     TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed QTopo export: " + e.getMessage() );
  //     return null;
  //   }
  // }

  // -----------------------------------------------------------------------
  // COMPASS EXPORT 

  static private void computeLRUD( LRUD lrud, DistoXDBlock b, List<DistoXDBlock> list, boolean at_from )
  {
    float grad2rad = TopoDroidUtil.GRAD2RAD;
    float n0 = (float)Math.cos( b.mBearing * grad2rad );
    float e0 = (float)Math.sin( b.mBearing * grad2rad );
    float cc0 = (float)Math.cos( b.mClino * grad2rad );
    float sc0 = (float)Math.sin( b.mClino * grad2rad );
    float cb0 = n0;
    float sb0 = e0;
    float sc02 = sc0 * sc0;
    float cc02 = 1.0f - sc02;
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
        float cb = (float)Math.cos( item.mBearing * grad2rad );
        float sb = (float)Math.sin( item.mBearing * grad2rad );
        float cc = (float)Math.cos( item.mClino * grad2rad );
        float sc = (float)Math.sin( item.mClino * grad2rad );
        float len = item.mLength;
        // float z1 = sc02 * sc;      // first point: horizontal projection [times sc02]
        // float n1 = sc02 * cc * cb;
        // float e1 = sc02 * cc * sb;
        float cbb0 = sb*sb0 + cb*cb0;
        // len * ( second_point - first_point )
        float z1 = len * ( sc * cc02 - cc * cc0 * sc0 * cbb0 + sc02 * sc);
        float n1 = len * cc * ( cc02 * ( cb - cb0 * cbb0 )   + sc02 * cb);
        float e1 = len * cc * ( cc02 * ( sb - sb0 * cbb0 )   + sc02 * sb);
        if ( z1 > 0.0 ) { if ( z1 > lrud.u ) lrud.u = z1; }
        else            { if ( -z1 > lrud.d ) lrud.d = -z1; }
        float rl = e1 * n0 - n1 * e0;
        if ( rl > 0.0 ) { if ( rl > lrud.r ) lrud.r = rl; }
        else            { if ( -rl > lrud.l ) lrud.l = -rl; }
      }
    }
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

  private static void printShotToDat( PrintWriter pw, float l, float b, float c, int n, LRUD lrud,
                               boolean duplicate, String comment )
  {
    b = TopoDroidUtil.in360( b/n );
    pw.format(Locale.ENGLISH, "%.2f %.1f %.1f %.2f %.2f %.2f %.2f", (l/n)*TopoDroidUtil.M2FT, b, c/n, 
      lrud.l*TopoDroidUtil.M2FT, lrud.u*TopoDroidUtil.M2FT, lrud.d*TopoDroidUtil.M2FT, lrud.r*TopoDroidUtil.M2FT );
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
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      // FIXME 
      // SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      // pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );

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
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "exportSurveyAsDat date parse error " + date );
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

      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f; // shot average values
      int n = 0;
      DistoXDBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay = false;
      boolean duplicate = false;
      LRUD lrud = new LRUD();

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidSetting.mCloseDistance ) ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 && ref_item != null ) {
              computeLRUD( lrud, ref_item, list, true );
              pw.format("%s-%s %s-%s ", info.name, ref_item.mFrom, info.name, ref_item.mTo );
              printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 && ref_item != null ) { // write pervious leg shot
              computeLRUD( lrud, ref_item, list, true );
              pw.format("%s-%s %s-%s ", info.name, ref_item.mFrom, info.name, ref_item.mTo );
              printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          } else {
            if ( n > 0 && ref_item != null ) {
              computeLRUD( lrud, ref_item, list, true );
              pw.format("%s-%s %s-%s ", info.name, ref_item.mFrom, info.name, ref_item.mTo );
              printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
            }
            n = 1;
            ref_item = item;
            duplicate = ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
          }
        }
      }
      if ( n > 0 && ref_item != null ) {
        computeLRUD( lrud, ref_item, list, true );
        pw.format("%s-%s %s-%s ", info.name, ref_item.mFrom, info.name, ref_item.mTo );
        printShotToDat( pw, l, b, c, n, lrud, duplicate, ref_item.mComment );
      }
      pw.format( "\f\r\n" );

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed Compass export: " + e.getMessage() );
      return null;
    }
  }

  // =======================================================================
  // WALLS EXPORT 
 
  static String exportSurveyAsSrv( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    try {
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      // TODO
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
  
      pw.format("; %s\n", info.name );
      pw.format("; created by TopoDroid v %s - %s \n", TopoDroidApp.VERSION, sdf.format( new Date() ) );
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
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "exportSurveyAsDat date parse error " + date );
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
      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;

      int n = 0;
      DistoXDBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO
      for ( DistoXDBlock item : list ) {
        String from    = item.mFrom;
        String to      = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidSetting.mCloseDistance ) ) {
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f", l/n, b, c/n );
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
              n = 0;
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
            if ( n > 0 && ref_item != null ) { // finish writing previous leg shot
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f", l/n, b, c/n );
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
              n = 0;
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
            if ( n > 0 && ref_item != null ) {
              b = TopoDroidUtil.in360( b/n );
              pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f\n", l/n, b, c/n );
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
              // n = 0;
            }
            n = 1;
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
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("\t; %s\n", item.mComment );
            }
            pw.format("%s\t%s\t", from, to );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
          }
        }
        // pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f\n", item.mLength, item.mBearing, item.mClino );
      }
      if ( n > 0 && ref_item != null ) {
        b = TopoDroidUtil.in360( b/n );
        pw.format(Locale.ENGLISH, "%.2f\t%.1f\t%.1f", l/n, b, c/n );
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
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed Walls export: " + e.getMessage() );
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
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter out = new PrintWriter( fw );
      // TODO
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      out.printf(Locale.ENGLISH, "999\nDXF created by TopoDroid v %s - %s (declination %.4f)\n",
        TopoDroidApp.VERSION, sdf.format( new Date() ), info.declination );
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
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed DXF export: " + e.getMessage() );
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
  static private void printStartShotToTro( PrintWriter pw, DistoXDBlock item, List< DistoXDBlock > list )
  {
    LRUD lrud = new LRUD();
    computeLRUD( lrud, item, list, true );
    pw.format(Locale.ENGLISH, "%s %s 0.00 0.00 0.00 ", item.mFrom, item.mFrom );
    pw.format(Locale.ENGLISH, "%.2f %.2f %.2f %.2f N I", lrud.l, lrud.r, lrud.u, lrud.d );
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
  }

  static private void printShotToTro( PrintWriter pw, DistoXDBlock item, float l, float b, float c, int n, LRUD lrud )
  {
    b = TopoDroidUtil.in360( b/n );
    // Log.v( TAG, "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    pw.format("%s %s ", item.mFrom, item.mTo );
    pw.format(Locale.ENGLISH, "%.2f %.1f %.1f ", (l/n), b, c/n );
    pw.format(Locale.ENGLISH, "%.2f %.2f %.2f %.2f N I", lrud.l, lrud.r, lrud.u, lrud.d );
    // if ( duplicate ) {
    //   // pw.format(" #|L#");
    // }
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
  }

  static String exportSurveyAsTro( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List<DistoXDBlock> list = data.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TopoDroidApp.STATUS_NORMAL );
    try {
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
  
      // FIXME 
      // SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      // pw.format("; %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );

      pw.format("Version 5.02\r\n\r\n");
      if ( fixed.size() > 0 ) {
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

      float l=0.0f, b=0.0f, c=0.0f, b0=0.0f; // shot average values
      int n = 0;
      DistoXDBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay = false;
      boolean duplicate = false;
      boolean start = true;
      LRUD lrud = new LRUD();

      for ( DistoXDBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.mType == DistoXDBlock.BLOCK_SEC_LEG || item.relativeDistance( ref_item ) < TopoDroidSetting.mCloseDistance ) ) {
              // Log.v( TAG, "data " + item.mLength + " " + item.mBearing + " " + item.mClino );
              float bb = TopoDroidUtil.around( item.mBearing, b0 );
              l += item.mLength;
              b += bb;
              c += item.mClino;
              ++n;
            }
          } else { // only TO station
            if ( n > 0 && ref_item != null ) {
              if ( start ) {
                printStartShotToTro( pw, ref_item, list );
                start = false;
              }
              computeLRUD( lrud, ref_item, list, false );
              printShotToTro( pw, ref_item, l, b, c, n, lrud );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( n > 0 && ref_item != null ) { // write pervious leg shot
              if ( start ) {
                printStartShotToTro( pw, ref_item, list );
                start = false;
              }
              computeLRUD( lrud, ref_item, list, false );
              printShotToTro( pw, ref_item, l, b, c, n, lrud );
              duplicate = false;
              n = 0;
              ref_item = null; 
            }
          } else {
            if ( n > 0 && ref_item != null ) {
              if ( start ) {
                printStartShotToTro( pw, ref_item, list );
                start = false;
              }
              computeLRUD( lrud, ref_item, list, false );
              printShotToTro( pw, ref_item, l, b, c, n, lrud );
            }
            n = 1;
            ref_item = item;
            duplicate = ( item.mFlag == DistoXDBlock.BLOCK_DUPLICATE );
            // Log.v( TAG, "first data " + item.mLength + " " + item.mBearing + " " + item.mClino );
            l = item.mLength;
            b = item.mBearing;
            b0 = b;
            c = item.mClino;
          }
        }
      }
      if ( n > 0 && ref_item != null ) {
        if ( start ) {
          printStartShotToTro( pw, ref_item, list );
          start = false;
        }
        computeLRUD( lrud, ref_item, list, false );
        printShotToTro( pw, ref_item, l, b, c, n, lrud );
      }

      fw.flush();
      fw.close();
      return filename;
    } catch ( IOException e ) {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed VisualTopo export: " + e.getMessage() );
      return null;
    }
  }

  static String exportCalibAsCsv( long cid, DataHelper data, CalibInfo ci, String filename )
  {
    try {
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("# %s created by TopoDroid v %s\n\n", sdf.format( new Date() ), TopoDroidApp.VERSION );

      pw.format("# %s\n", ci.name );
      pw.format("# %s\n", ci.date );
      pw.format("# %s\n", ci.device );
      pw.format("# %s\n", ci.comment );

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
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Failed CSV export: " + e.getMessage() );
      return null;
    }
  }

}
