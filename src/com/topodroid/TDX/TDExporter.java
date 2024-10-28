/* @file TDExporter.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief topodroid exports
 * --------------------------------------------------------
 *  copyright this software is distributed under gpl-3.0 or later
 *  see the file copying.
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
 *   GeoJSON
 *   Track file (OziExplorer)
 *   DXF
 *   CSV
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
import com.topodroid.num.TDNum;
import com.topodroid.num.NumStation;
import com.topodroid.num.NumShot;
import com.topodroid.num.NumSplay;
import com.topodroid.num.NumBranch;
import com.topodroid.mag.Geodetic;
import com.topodroid.ptopo.PTFile;
import com.topodroid.io.shp.ShpPointz;
import com.topodroid.io.shp.ShpPolylinez;
import com.topodroid.io.shp.ShpNamez;
import com.topodroid.io.shp.ShpFixedz;
import com.topodroid.io.trb.TrbStruct;
import com.topodroid.io.trb.TrbSeries;
import com.topodroid.io.trb.TrbShot;
import com.topodroid.trb.TRobotPoint;
import com.topodroid.trb.TRobotSeries;
import com.topodroid.trb.TRobot;
import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
import com.topodroid.common.ExtendType;
import com.topodroid.common.PlotType;
// import com.topodroid.common.StationFlag;

import android.os.ParcelFileDescriptor;
import android.net.Uri;

import java.io.StringWriter;
// import java.io.FileOutputStream;
// import java.io.FileInputStream;
import java.io.OutputStream;
// import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashMap;

import android.util.Base64;

@SuppressWarnings("ALL")
public class TDExporter
{
                                                 // -1      0           1        2         3       4        5
  private static final String[] therion_extend = { "left", "vertical", "right", "ignore", "hide", "start", "unset", "left", "vert", "right" };
  private static final int[] csurvey_extend = { 1, 2, 0, 0, 0, 0, 0, 1, 2, 0 };
  private static final String   therion_flags_duplicate     = "   flags duplicate\n";
  private static final String   therion_flags_not_duplicate = "   flags not duplicate\n";
  private static final String   therion_flags_surface       = "   flags surface\n";
  private static final String   therion_flags_not_surface   = "   flags not surface\n";
  private static final String   extend_auto = "    # extend auto\n";

  private static double mERadius = Geodetic.EARTH_A;
  private static double mSRadius = Geodetic.EARTH_A;

  private static void checkShotsClino( List< DBlock > list )
  {
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
      for ( DBlock blk : list ) {
	if ( blk.mTo != null && blk.mTo.length() > 0 && blk.mFrom != null && blk.mFrom.length() > 0 ) {
          // sets the blocks clino
          TDNum num = new TDNum( list, blk.mFrom, null, null, 0.0f, null ); // no declination, null formatClosure
	  break;
	}
      }
    }
  }

  // used by DrawingAudioPath, DrawingPhotoPath DrawingPointPath
  static byte[] readFileBytes( String subdir, String filename )
  {
    // int len = (int)TDFile.getFileLength( filepath );
    int len = (int)TDFile.getMSFileLength( subdir, filename );
    if ( len > 0 ) {
      byte[] buf = new byte[ len ];
      int read = 0;
      try {
        // TDLog.Log( TDLog.LOG_IO, "read file bytes: " + filepath );
        // FileInputStream fis = TDFile.getFileInputStream( filepath );
        // BufferedInputStream bis = new BufferedInputStream( fis );
        BufferedInputStream bis = new BufferedInputStream( TDFile.getMSinput( subdir, filename, "application/octec-stream" ) );
        while ( read < len ) {
          read += bis.read( buf, read, len-read );
        }
        if ( bis != null ) bis.close();
      } catch ( IOException e ) {
        // TODO
      }
      if ( read == len ) return buf;
    }
    return null;
  }

  // =======================================================================
  // CSURVEY EXPORT cSurvey
  //   handles flags: duplicate surface commented

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

  static private void writeCsxLeg( PrintWriter pw, AverageLeg leg, DBlock ref )
  {
    pw.format(Locale.US, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"",
      leg.length(), leg.bearing(), leg.clino()
    );
    pw.format(Locale.US, " g=\"%.1f\" m=\"%.1f\" dip=\"%.1f\"", ref.mAcceleration, ref.mMagnetic, ref.mDip );
    leg.reset();
  }

  // segments have only the attribute "cave", no attribute "branch"
  static private void writeCsxSegment( PrintWriter pw, long id, String cave, String branch, String session, String prefix, String f, String t )
  {
    // TDLog.Log( TDLog.LOG_CSURVEY, "shot segment " + id + " cave " + cave + " " + f + " - " + t ); 
    if ( TDSetting.mExportStationsPrefix ) {
      pw.format("<segment id=\"%d\" cave=\"%s\" branch=\"%s\" session=\"%s\" from=\"%s%s\" to=\"%s%s\"", 
        (int)id, cave, branch, session, prefix, f, prefix, t );
    } else {
      pw.format("<segment id=\"%d\" cave=\"%s\" branch=\"%s\" session=\"%s\" from=\"%s\" to=\"%s\"", (int)id, cave, branch, session, f, t );
    }
  }

  static private void writeCsxTSplaySegment( PrintWriter pw, String cave, String branch, String session, String prefix, String t, int cnt, boolean xsplay )
  {
    // TDLog.Log( TDLog.LOG_CSURVEY, "T-splay segment cave " + cave + " " + t + " " + cnt ); 
    if ( TDSetting.mExportStationsPrefix ) {
      pw.format("<segment id=\"\" cave=\"%s\" branch=\"%s\" session=\"%s\" from=\"%s%s(%d)\" to=\"%s%s\"",
        cave, branch, session, prefix, t, cnt, prefix, t );
    } else {
      pw.format("<segment id=\"\" cave=\"%s\" branch=\"%s\" session=\"%s\" from=\"%s(%d)\" to=\"%s\"", cave, branch, session, t, cnt, t );
    }
    if ( xsplay ) pw.format(" cut=\"1\"");
  }

  static private void writeCsxFSplaySegment( PrintWriter pw, String cave, String branch, String session, String prefix, String f, int cnt, boolean xsplay )
  {
    // TDLog.Log( TDLog.LOG_CSURVEY, "F-splay segment cave " + cave + " " + f + " " + cnt ); 
    if ( TDSetting.mExportStationsPrefix ) {
      pw.format("<segment id=\"\" cave=\"%s\" branch=\"%s\" session=\"%s\" from=\"%s%s\" to=\"%s%s(%d)\"",
        cave, branch, session, prefix, f, prefix, f, cnt );
    } else {
      pw.format("<segment id=\"\" cave=\"%s\" branch=\"%s\" session=\"%s\" from=\"%s\" to=\"%s(%d)\"", cave, branch, session, f, f, cnt );
    }
    if ( xsplay ) pw.format(" cut=\"1\"");
  }

  static private void writeCsxShotAttachments( PrintWriter pw, DataHelper data, String survey, long sid, DBlock blk )
  {
    long bid = blk.mId;
    AudioInfo audio = data.getAudioAtShot( sid, bid );
    List< PhotoInfo > photos = data.selectPhotoAtShot( sid, bid );
    if ( audio == null && photos.size() == 0 ) return;
    pw.format("      <attachments>\n");
    if ( audio != null ) {
      // TDLog.v( "audio " + audio.id + " " + audio.shotid + " blk " + bid );
      String subdir = survey + "/audio"; // "audio/" + survey;
      String name   = bid  + ".wav"; // Long.toString(bid) + ".wav";
      if ( TDFile.hasMSfile( subdir, name ) ) {
        byte[] buf = readFileBytes( subdir, name );
        if ( buf != null ) {
          pw.format("        <attachment dataformat=\"0\" data=\"%s\" name=\"\" note=\"\" type=\"audio/x-wav\" />\n",
            Base64.encodeToString( buf, Base64.NO_WRAP ) );
        }
      }
    }
    String photo_dir = TDPath.getSurveyPhotoDir( survey ); // 20230118 FIXME why this ?
    for ( PhotoInfo photo : photos ) {
      String subdir = survey + "/photo"; // "photo/" + survey;
      String name   = photo.id + ".jpg"; // Long.toString(photo.id) + ".jpg";
      if ( TDFile.hasMSfile( subdir, name ) ) {
        byte[] buf = readFileBytes( subdir, name );
        if ( buf != null ) {
          pw.format("        <attachment dataformat=\"0\" data=\"%s\" name=\"\" note=\"%s\" type=\"image/jpeg\" />\n",
            Base64.encodeToString( buf, Base64.NO_WRAP ), photo.mComment );
        }
      }
    }
    pw.format("      </attachments>\n");
  }

  private static String toXml( String s )
  { 
    return s.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\'", "&apos;"); 
  }

  /** export survey in cSurvey format
   * @param uri     output stream uri
   * @param sid     survey ID
   * @param data    surveys database
   * @param info    survey info
   * @param psd1    first plot-data of the survey (plan)
   * @param psd2    second plot-data of the survey (profile)
   * @param origin  plot origin station
   * @param surveyname file name, either "survey" or "survey-plot"
   * @return 1 on success
   */
  static int exportSurveyAsCsx( Uri uri, long sid, DataHelper data, SurveyInfo info, PlotSaveData psd1, PlotSaveData psd2, String origin, String surveyname )
  {
    int ret = 0; // 0 = failure
    ParcelFileDescriptor pfd = null;
    if ( uri != null ) {
      pfd = TDsafUri.docWriteFileDescriptor( uri );
      if ( pfd == null ) return 0;
    }
    try {
      // BufferedWriter bw = TDFile.getMSwriter( "csx", surveyname + ".csx", "text/csx" );
      BufferedWriter bw = new BufferedWriter( (pfd != null)? TDsafUri.docFileWriter( pfd ) : TDFile.getFileWriter( TDPath.getOutFile( surveyname + ".csx" ) ) );
      // BufferedWriter bw = new BufferedWriter( TDsafUri.docFileWriter( pfd ) );
      ret = exportSurveyAsCsx( bw, sid, data, info, psd1, psd2, origin, surveyname );
      bw.flush();
      bw.close();
    } catch ( FileNotFoundException e ) {
      TDLog.e("file not found");
    } catch ( IOException e ) {
      TDLog.e( "io error " + e.getMessage() );
    } finally {
      if ( pfd != null ) {
        TDsafUri.closeFileDescriptor( pfd );
      }
    }
    return ret;
  }

  /** export survey in cSurvey format
   * @param bw      output writer
   * @param sid     survey ID
   * @param data    surveys database
   * @param info    survey info
   * @param psd1    first plot-data of the survey (plan)
   * @param psd2    second plot-data of the survey (profile)
   * @param origin  plot origin station
   * @param survey_name survey name (unused)
   * @return 1 on success or 0 on error
   */
  static int exportSurveyAsCsx( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, PlotSaveData psd1, PlotSaveData psd2, String origin, String survey_name )
  {
    int ret = 0; // 0 = failure
    // TDLog.v( "export as csurvey: " + file.getName() );
    String cave   = toXml( info.name.toUpperCase(Locale.US) );
    String survey = toXml( info.name );

    String prefix = cave + "-";
    String branch = "";
    if ( psd1 != null && psd1.name != null ) { // if ( sketch != null && sketch.getName() != null ) 
      branch  = psd1.name; // sketch.getName();
      int len = branch.length();
      if ( len > 1 ) branch = branch.substring(0, len-1);
      branch = toXml( branch );
    }

    // STATIONS_PREFIX
    // if ( TDSetting.mExportStationsPrefix ) {
    //   if ( branch.length() > 0 ) {
    //     prefix = cave + "-" + branch + "-";
    //   } else {
    //     prefix = cave + "-";
    //   }
    // }

    List< DBlock > d_list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    List< DBlock > c_list = data.selectAllExportShots( sid, TDStatus.CHECK );
    checkShotsClino( d_list );

    List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    // List< PlotInfo > plots  = data.selectAllPlots( sid, TDStatus.NORMAL );
    // FIXME TODO_CSURVEY
    // List< StationInfo > stations = data.getStations( sid );

    if ( origin == null ) { // use first non-null "from"
      for ( DBlock item : d_list ) {
        String from = item.mFrom;
        if ( from != null && from.length() > 0 ) {
          origin = from;
          break;
        }
      }
    }

    try {
      // BufferedWriter bw = TDFile.getMSwriter( "csx", survey_name + ".csx", "text/csx" );
      PrintWriter pw = new PrintWriter( bw );
      String date = TDUtil.getDateString( "yyyy-MM-dd" );

      pw.format("<csurvey version=\"1.11\" id=\"\">\n");
      pw.format("<!-- %s created by TopoDroid v %s -->\n", date, TDVersion.string() );

// ++++++++++++++++ PROPERTIES
      // FIXME origin = origin of Num
      if ( TDSetting.mExportStationsPrefix ) {
        pw.format("  <properties id=\"\" name=\"\" origin=\"%s%s\" ", prefix, origin ); 
      } else {
        pw.format("  <properties id=\"\" name=\"\" origin=\"%s\" ", origin );
      }
      // FIXME TODO_CSURVEY
      pw.format(      "creatid=\"TopoDroid\" creatversion=\"%s\" creatdate=\"%s\" ", TDVersion.string(), date );
      pw.format(      "calculatemode=\"1\" calculatetype=\"2\" calculateversion=\"-1\" " );
      pw.format(      "ringcorrectionmode=\"2\" nordcorrectionmode=\"0\" inversionmode=\"1\" ");
      pw.format(      "designwarpingmode=\"1\" bindcrosssection=\"1\">\n");
      

   // ============== SESSIONS
      String info_date = info.date.replaceAll("[\\.,-,/]", "");
      pw.format("    <sessions>\n");
      pw.format("      <session date=\"%s\" ", info.date); // yyyy-mm-dd or any other format is ok
      pw.format(         "description=\"%s\" ", cave ); // title
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format(" team=\"%s\" ", toXml(info.team) );
      }
      if ( info.hasDeclination() ) { // DECLINATION in cSurvey
        pw.format(Locale.US, "nordtype=\"0\" manualdeclination=\"1\" declination=\"%.4f\" ", info.declination ); 
      } else {
        pw.format("nordtype=\"0\" manualdeclination=\"0\" ");
      }

      pw.format(">\n");
      pw.format("      </session>\n");
      pw.format("    </sessions>\n");

      String session = info_date + "_" + TDString.spacesToUnderscores( cave );
      session = session.toLowerCase(Locale.US);

   // ============== CAVE INFOS and BRANCHES
      pw.format("    <caveinfos>\n");
      pw.format("      <caveinfo name=\"%s\" color=\"1724697804\"", cave );
      // pw.format( " color=\"\"");
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format( " comment=\"%s\"\n", toXml( info.comment ) );
      }
      pw.format(" >\n");
      pw.format("        <branches>\n");
      if ( branch != null ) {
        pw.format("          <branch name=\"%s\" color=\"%d\">\n          </branch>\n", branch, TDUtil.randomPastel() );
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

      for ( DBlock blk : c_list ) { // calib-check shots
        writeCsxSegment( pw, blk.mId, cave, branch, session, prefix, blk.mFrom, blk.mTo );
        pw.format(" exclude=\"1\"");
        pw.format(" calibration=\"1\""); 
        pw.format(Locale.US, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", blk.mLength, blk.mBearing, blk.mClino);
        pw.format(Locale.US, " g=\"%.1f\" m=\"%.1f\" dip=\"%.1f\"", blk.mAcceleration, blk.mMagnetic, blk.mDip );
        // pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\"");
        if ( blk.mComment != null && blk.mComment.length() > 0 ) {
          pw.format(" note=\"%s\"", toXml( blk.mComment.replaceAll("\"", "") ) );
        }
	pw.format(" distox=\"%s\"", blk.getAddress() ); // MAC-address
        pw.format(" >\n");
        // writeCsxShotAttachments( pw, data, survey, sid, blk ); // calib-check shots have no attachment
        pw.format("    </segment>\n");
      }

      // optional attrs of "segment": id cave branch session

      int cntSplay = 0;     // splay counter (index)
      int extend = 0;      // current extend
      boolean dup = false;  // duplicate
      boolean sur = false;  // surface
      // boolean bck = false;  // backshot
      String com = null;    // comment
      String f="", t="";    // from to stations
      DBlock ref_item = null;
      // float l=0.0f, b=0.0f, c=0.0f, b0=0.0f;
      // int n = 0;
      AverageLeg leg = new AverageLeg(0);

      for ( DBlock item : d_list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsxSegment( pw, ref_item.mId, cave, branch, session, prefix, f, t ); // branch prefix

              if ( extend < 1 ) pw.format(" direction=\"%d\"", csurvey_extend[1+extend] );
              if ( dup || sur /* || bck */ ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
		// if ( bck ) { pw.format(" backshot=\"1\"");   bck = false; }
              }
              // pw.format(" planshowsplayborder=\"1\" profileshowsplayborder=\"1\" ");
              if ( ref_item.isCommented() ) pw.format(" commented=\"1\"");
              writeCsxLeg( pw, leg, ref_item );
              pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\"");
              if ( com != null && com.length() > 0 ) {
                pw.format(" note=\"%s\"", toXml( com ) );
                com = null;
              }
	      pw.format(" distox=\"%s\"", ref_item.getAddress() ); // MAC-address
              pw.format(" >\n");
              writeCsxShotAttachments( pw, data, survey, sid, ref_item );
              pw.format("    </segment>\n");
              ref_item = null; 
            }

            extend = item.getIntExtend();
            writeCsxTSplaySegment( pw, cave, branch, session, prefix, to, cntSplay, item.isXSplay() ); // branch prefix
            ++ cntSplay;
            pw.format(" splay=\"1\" exclude=\"1\"");
            if ( item.isCommented() ) pw.format(" commented=\"1\"");
            if ( extend < 1 ) pw.format(" direction=\"%d\"", csurvey_extend[1+extend] );
            pw.format(Locale.US, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", item.mLength, item.mBearing, item.mClino );
            pw.format(Locale.US, " g=\"%.1f\" m=\"%.1f\" dip=\"%.1f\"", item.mAcceleration, item.mMagnetic, item.mDip );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format(" note=\"%s\"", toXml( item.mComment.replaceAll("\"", "") ) );
            }
	    pw.format(" distox=\"%s\"", item.getAddress() ); // MAC-address
            pw.format(" >\n");
            writeCsxShotAttachments( pw, data, survey, sid, item );
            pw.format("    </segment>\n");
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // ONLY FROM STATION : splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
              writeCsxSegment( pw, ref_item.mId, cave, branch, session, prefix, f, t ); // branch prefix
              if ( extend < 1 ) pw.format(" direction=\"%d\"", csurvey_extend[1+extend] );
              if ( dup || sur /* || bck */ ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
                // if ( bck ) { pw.format(" backshot=\"1\"");   bck = false; }
              }
              // pw.format(" planshowsplayborder=\"1\" profileshowsplayborder=\"1\" ");
              if ( ref_item.isCommented() ) pw.format(" commented=\"1\"");
              writeCsxLeg( pw, leg, ref_item );
              pw.format(" l=\"0.0\" r=\"0.0\" u=\"0.0\" d=\"0.0\"");
              if ( com != null && com.length() > 0 ) {
                pw.format(" note=\"%s\"", toXml( com ) );
                com = null;
              }
	      pw.format(" distox=\"%s\"", ref_item.getAddress() ); // MAC-address
              pw.format(" >\n");
              writeCsxShotAttachments( pw, data, survey, sid, ref_item );
              pw.format("    </segment>\n");
              ref_item = null; 
            }

            extend = item.getIntExtend();
            writeCsxFSplaySegment( pw, cave, branch, session, prefix, from, cntSplay, item.isXSplay() ); // branch prefix
            ++cntSplay;
            pw.format(" splay=\"1\" exclude=\"1\"");
            if ( item.isCommented() ) pw.format(" commented=\"1\"");
            if ( extend < 1 ) pw.format(" direction=\"%d\"", csurvey_extend[1+extend] );
            pw.format(Locale.US, " distance=\"%.2f\" bearing=\"%.1f\" inclination=\"%.1f\"", item.mLength, item.mBearing, item.mClino );
            pw.format(Locale.US, " g=\"%.1f\" m=\"%.1f\" dip=\"%.1f\"", item.mAcceleration, item.mMagnetic, item.mDip );
            pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format(" note=\"%s\"", toXml( item.mComment.replaceAll("\"", "") ) );
            }
	    pw.format(" distox=\"%s\"", item.getAddress() ); // MAC-address
            pw.format(" >\n");
            writeCsxShotAttachments( pw, data, survey, sid, item );
            pw.format("    </segment>\n");
          } else { // BOTH FROM AND TO STATIONS
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsxSegment( pw, ref_item.mId, cave, branch, session, prefix, f, t ); // branch prefix
              if ( extend < 1 ) pw.format(" direction=\"%d\"", csurvey_extend[1+extend] );
              if ( dup || sur /* || bck */ ) {
                pw.format(" exclude=\"1\"");
                if ( dup ) { pw.format(" duplicate=\"1\""); dup = false; }
                if ( sur ) { pw.format(" surface=\"1\"");   sur = false; }
                // if ( bck ) { pw.format(" backshot=\"1\"");   bck = false; }
              }
              if ( ref_item.isCommented() ) pw.format(" commented=\"1\"");
              writeCsxLeg( pw, leg, ref_item );
              pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
              if ( com != null && com.length() > 0 ) {
                pw.format(" note=\"%s\"", toXml( com ) );
                com = null;
              }
	      pw.format(" distox=\"%s\"", ref_item.getAddress() ); // MAC-address
              pw.format(" >\n");
              writeCsxShotAttachments( pw, data, survey, sid, ref_item );
              pw.format("    </segment>\n");
            }
            ref_item = item;
            extend = item.getIntExtend();
            if ( item.isDuplicate() ) {
              dup = true;
            } else if ( item.isSurface() ) {
              sur = true;
            // } else if ( item.isCommented() ) {
            //   ...
            // } else if ( item.isBackshot() ) {
            //   bck = true;
            }
            f = (from!=null)? from : "";
            t = (to!=null)?   to   : "";
            leg.set( item.mLength, item.mBearing, item.mClino );
            com = item.mComment;
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeCsxSegment( pw, ref_item.mId, cave, branch, session, prefix, f, t ); // branch prefix
        if ( extend < 1 ) pw.format(" direction=\"%d\"", csurvey_extend[1+extend] );
        if ( dup || sur /* || bck */ ) {
           pw.format(" exclude=\"1\"");
           if ( dup ) { pw.format(" duplicate=\"1\"");  /* dup = false; */ }
           if ( sur ) { pw.format(" surface=\"1\"");    /* sur = false; */ }
           // if ( bck ) { pw.format(" backshot=\"1\"");  /* bck = false; */ }
        }
        if ( ref_item.isCommented() ) pw.format(" commented=\"1\"");
        writeCsxLeg( pw, leg, ref_item );
        pw.format(" l=\"0\" r=\"0\" u=\"0\" d=\"0\"");
        if ( com != null && com.length() > 0 ) {
          pw.format(" note=\"%s\"", toXml( com ) );
          // com = null;
        }
	pw.format(" distox=\"%s\"", ref_item.getAddress() ); // MAC-address
        pw.format(" >\n");
        writeCsxShotAttachments( pw, data, survey, sid, ref_item );
        pw.format("    </segment>\n");
      }
      pw.format("  </segments>\n");

      // ============= TRIG POINTS
      pw.format("  <trigpoints>\n");
      if ( fixed.size() > 0 ) {
        for ( FixedInfo fix : fixed ) {
          pw.format("     <trigpoint name=\"%s\" labelsymbol=\"0\" >\n", fix.name );
          pw.format(Locale.US, "       <coordinate latv=\"%.7f\" longv=\"%.7f\" altv=\"%.2f\" lat=\"%.7f N\" long=\"%.7f E\" format=\"dd.ddddddd N\" alt=\"%.2f\" />\n",
                    fix.lat, fix.lng, fix.h_geo, fix.lat, fix.lng, fix.h_geo );
          pw.format("     </trigpoint>\n");
        }
      }
      pw.format("  </trigpoints>\n");

      // ============= PLOTS
      if ( psd1 != null ) {
        DrawingWindow.exportAsCsx( sid, pw, survey, cave, branch /*, session */, psd1, psd2 );
      } else {
        pw.format("  <plan>\n");
        exportEmptyCsxSketch( pw );
        pw.format("  </plan>\n");
        pw.format("  <profile>\n");
        exportEmptyCsxSketch( pw );
        pw.format("  </profile>\n");
      }
      pw.format("</csurvey>\n");

      bw.flush();
      bw.close();
      ret = 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed cSurvey export: " + e.getMessage() );
    }
    return ret;
  }

  // ####################################################################################################################################
  // geographic exports - use (ge, gs, gv)

  /**
   * @param sid      survey ID
   * @param data     database helper
   * @param h_geo_factor unit conversion factor for vertical coord
   * @param ellipsoid_h  whether to use ellipsoid altitude
   * @param station      ???
   * @param convergence  whether to apply convergence
   */
  static GeoReference getGeolocalizedStation( long sid, DataHelper data, float h_geo_factor, boolean ellipsoid_h, String station, boolean convergence )
  {
    float decl = data.getSurveyDeclination( sid );
    if ( decl >= SurveyInfo.DECLINATION_MAX ) decl = 0; // if unset use 0

    List< TDNum > nums = getGeolocalizedData( sid, data, decl, h_geo_factor, ellipsoid_h, convergence );
    if ( nums == null ) return null;
    for ( TDNum num : nums ) {
      for ( NumStation st : num.getStations() ) {
        if ( station.equals( st.name ) ) return new GeoReference( st.e, st.s, st.v, mERadius, mSRadius, num.getDeclination() ); // 20230104 was decl
      }
    }
    return null;
  }

  /**
   * @param sid      survey ID
   * @param data     survey database
   * @param decl     magnetic declination [degree]
   * @param h_geo_factor unit conversion factor for vertical coord
   * @param ellipsoid_h  whether altitude is ellipsoidic
   * @param convergence  whether to apply the meridian convergence
   */
  static private List< TDNum > getGeolocalizedData( long sid, DataHelper data, float decl, float h_geo_factor, boolean ellipsoid_h, boolean convergence )
  {
    List< FixedInfo > fixeds = data.selectAllFixed( sid, 0 );
    // TDLog.v( "get geoloc. data. Decl " + decl + " fixeds " + fixeds.size() );
    if ( TDUtil.isEmpty(fixeds) ) return null;

    List< TDNum > nums = new ArrayList< TDNum >();
    List< DBlock > shots_data = data.selectAllExportShots( sid, 0 );
    FixedInfo origin = null;
    for ( FixedInfo fixed : fixeds ) {
      float decl0 = decl;
      if ( convergence && fixed.hasCSCoords() ) decl0 -= fixed.getConvergence();
      TDNum num = new TDNum( shots_data, fixed.name, null, null, decl0, null ); // null formatClosure
      // TDLog.v( "Num shots " + num.getShots().size() );
      if ( num.getShots().size() > 0 ) {
        makeGeolocalizedData( num, fixed, h_geo_factor, ellipsoid_h, convergence );
	nums.add( num );
      } 
    }
    // if ( origin == null || num == null ) return null;
    return nums;
  }

  // FIXME CONVERGENCE
  /** make the reduced data geolocalized with respect to the given origin
   * @param num          data reductuion
   * @param origin       fix point, origin
   * @param h_geo_factor unit conversion factor for vertical coords
   * @param ellipsoid_h  whether to use ellipsoid altitude
   * @param convergence  whether to apple meridian convergence
   */
  static private void  makeGeolocalizedData( TDNum num, FixedInfo origin, float h_geo_factor, boolean ellipsoid_h, boolean convergence )
  {
    double lat, lng, h_geo;
    // TDLog.v( "st cnt " + NumStation.cnt + " size " + num.getStations().size() );
    if ( convergence && origin.hasCSCoords() ) {
      lat   = origin.cs_lat   * origin.mToUnits; // CS units
      lng   = origin.cs_lng   * origin.mToUnits; 
      h_geo = origin.cs_h_geo; // meters
      mERadius = origin.mToUnits;
      mSRadius = origin.mToUnits;
      h_geo_factor = (float)origin.mToVUnits; 
    } else { // WGS84
      lat = origin.lat;
      lng = origin.lng;
      h_geo = ellipsoid_h ? origin.h_ell : origin.h_geo; // KML uses Geoid altitude (unless altitudeMode is set)
      mSRadius = 1 / Geodetic.meridianRadiusExact( lat, origin.h_ell ); // TODO 1 / Geodetic.meridianRadiusEllipsoid( lat, origin.h_ell );
      mERadius = 1 / Geodetic.parallelRadiusExact( lat, origin.h_ell ); //      1 / Geodetic.parallelRadiusEllipsoid( lat, origin.h_ell );
      // TDLog.v( "radius S " + (mSRadius / s_radius_e) + " E " + (mERadius / e_radius_a ) );
    }

    for ( NumStation st : num.getStations() ) {
      st.s = (lat - st.s * mSRadius);
      st.e = (lng + st.e * mERadius); 
      st.v = (h_geo - st.v) * h_geo_factor;
    }
    for ( NumStation cst : num.getClosureStations() ) {
      cst.s = (lat - cst.s * mSRadius);
      cst.e = (lng + cst.e * mERadius); 
      cst.v = (h_geo - cst.v) * h_geo_factor;
    }
    for ( NumSplay sp : num.getSplays() ) {
      sp.s = (lat - sp.s * mSRadius);
      sp.e = (lng + sp.e * mERadius); 
      sp.v = (h_geo - sp.v) * h_geo_factor;
    }
  }

  // ====================================================================================================================================
  // KML export Keyhole Markup Language
  //   NOTE shot flags are ignored

  /** export data in KML (keyhole) format
   * @param bw    buffered output stream
   * @param sid   survey ID
   * @param data  database helper object
   * @param info  survey info
   * @param survey_name survey name (unused)
   * @return 1 success, 0 fail, 2 no geopoint
   */
  static int exportSurveyAsKml( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name )
  {
    final String name          = "<name>%s</name>\n";
    final String name2         = "  <name>%s</name>\n";
    final String placemark     = "<Placemark>\n";
    final String placemark_end = "</Placemark>\n";
    final String linestyle     = "  <LineStyle>\n";
    final String linestyle_end = "  </LineStyle>\n";
    final String labelstyle     = "  <LabelStyle>\n";
    final String labelstyle_end = "  </LabelStyle>\n";
    final String style     = "<Style id=\"%s\">\n";
    final String style_end = "</Style>\n";
    final String point_id  = "<Point id=\"%s\">\n";
    final String point_end = "</Point>\n";
    final String linestring     = "  <LineString> <coordinates>\n";
    final String linestring_id  = "  <LineString id=\"%s-%s\"> <coordinates>\n";
    final String linestring_end = "  </coordinates> </LineString>\n";
    final String width      = "    <width>%d</width>\n";
    final String scale      = "    <width>%.1f</width>\n";
    final String color      = "    <color>%s</color>\n";
    final String color_mode = "    <colorMode>%s</colorMode>\n";
    final String multigeometry     = "  <MultiGeometry>\n";
    final String multigeometry_end = "  </MultiGeometry>\n";
    final String altitudeMode = "    <altitudeMode>absolute</altitudeMode>\n";
    final String style_url = "    <styleUrl>%s</styleUrl>\n";
    final String coordinates3 = "    <coordinates>%.8f,%.8f,%.1f</coordinates>\n";
    final String coordinates6 = "    %.8f,%.8f,%.1f %.8f,%.8f,%.1f\n";
    // TDLog.v( "export as KML " + file.getFilename() );
    List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), 1.0f, false, false ); // false: geoid altitude, false no convergence
    if ( TDUtil.isEmpty(nums) ) {
      TDLog.e( "Failed KML export: no geolocalized station");
      return 2;
    }

    // now write the KML
    try {
      // TDLog.Log( TDLog.LOG_IO, "export KML " + file );
      // BufferedWriter bw = TDFile.getMSwriter( "kml", survey_name + ".kml", "text/kml" );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      pw.format("<kml xmlnx=\"http://www.opengis.net/kml/2.2\">\n");
      pw.format("<Document>\n");

      pw.format(name, info.name );
      pw.format("<description>%s - TopoDroid v %s</description>\n",  TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
      pw.format("<TimeStamp><when>%s</when></TimeStamp>\n", info.date );

      pw.format(style, "centerline");
      pw.format(linestyle);
      pw.format(color, "ff0000ff"); // AABBGGRR
      pw.format(width, 2 );
      pw.format(linestyle_end);
      pw.format(labelstyle);
      pw.format(color, "ff0000ff"); // AABBGGRR
      pw.format(color_mode, "normal");
      pw.format(scale, 1.0f );
      pw.format(labelstyle_end);
      pw.format(style_end);

      pw.format(style, "splay");
      pw.format(linestyle);
      pw.format(color, "ffffff00"); // AABBGGRR
      pw.format(width, 1 );
      pw.format(linestyle_end);
      pw.format(labelstyle);
      pw.format(color, "ffffff00"); // AABBGGRR
      pw.format(color_mode, "normal");
      pw.format(scale, 0.5f );
      pw.format(labelstyle_end);
      pw.format(style_end);

      pw.format(style, "station");
      pw.format("  <IconStyle><Icon></Icon></IconStyle>\n");
      pw.format(labelstyle);
      pw.format(color, "ffff00ff"); // AABBGGRR
      pw.format(color_mode, "normal");
      pw.format(scale, 1.0f );
      pw.format(labelstyle_end);
      pw.format(linestyle);
      pw.format(color, "ffff00ff"); // AABBGGRR
      pw.format(width, 1 );
      pw.format(linestyle_end);
      pw.format(style_end);
      
      for ( TDNum num : nums ) {
        List< NumStation > stations = num.getStations();
        List< NumShot >    shots = num.getShots();
        List< NumSplay >   splays = num.getSplays();
        if ( TDSetting.mKmlStations ) {
          pw.format("  <Folder>\n");
          pw.format("    <name>stations</name>\n");
          for ( NumStation st : stations ) {
            pw.format(placemark);
            pw.format(name2, st.name );
            pw.format(style_url, "#station");
            pw.format(multigeometry);
            pw.format(altitudeMode);
              pw.format(point_id, st.name );
              pw.format(Locale.US, coordinates3, st.e, st.s, st.v );
              pw.format(point_end);
            pw.format(multigeometry_end);
            pw.format(placemark_end);
          }
          pw.format("  </Folder>\n");
        }

        pw.format(placemark);
        pw.format(name2, "centerline" );
        pw.format(style_url, "#centerline");
        pw.format(multigeometry);
        pw.format(altitudeMode);
        for ( NumShot sh : shots ) {
          NumStation from = sh.from;
          NumStation to   = sh.to;
          if ( from.hasExtend() && to.hasExtend() ) {
            pw.format(linestring_id, from.name, to.name );
            // pw.format("      <tessellate>1</tessellate>\n"); //   breaks the line up in small chunks
            // pw.format("      <extrude>1</extrude>\n"); // extends the line down to the ground
            pw.format(Locale.US, coordinates6, from.e, from.s, from.v, to.e, to.s, to.v );
            pw.format(linestring_end);
          // } else {
          //   // TDLog.v( "missing coords " + from.name + " " + from.hasExtend() + " " + to.name + " " + to.hasExtend() );
          }
        }
        pw.format(multigeometry_end);
        pw.format(placemark_end);

        if ( TDSetting.mKmlSplays ) {
          pw.format(placemark);
          pw.format(name2, "splays" );
          pw.format(style_url, "#splay");
          pw.format(multigeometry);
          pw.format(altitudeMode);
          for ( NumSplay sp : splays ) {
            NumStation from = sp.from;
            pw.format(linestring);
            // pw.format("      <tessellate>1</tessellate>\n"); //   breaks the line up in small chunks
            // pw.format("      <extrude>1</extrude>\n"); // extends the line down to the ground
            pw.format(Locale.US, coordinates6, from.e, from.s, from.v, sp.e, sp.s, sp.v );
            pw.format(linestring_end);
          }
          pw.format(multigeometry_end);
          pw.format(placemark_end);
        }
      }

      pw.format("</Document>\n");
      pw.format("</kml>\n");
      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed KML export: " + e.getMessage() );
      return 0;
    }
  }
  // =====================================================================================================================
  // SHP SHAPEFILE 

  /** export zipped shapefiles
   * @param os       output stream
   * @param data     database helper object
   * @param info     survey metadata
   * @param survey   survey name
   * @param dirname  dirname (relative to CWD) for temporary files
   * @return 1 success, 0 fail
   */
  static int exportSurveyAsShp( OutputStream os, long sid, DataHelper data, SurveyInfo info, String survey, String dirname )
  {
    List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), 1.0f, false, true ); // false: geoid altitude, true with convergence
    if ( TDUtil.isEmpty(nums) ) {
      TDLog.e( "Failed SHP export: no geolocalized station");
      return 0;
    }

    // TDLog.v( "SHP data export. base " + dirname ); 
    // assert( dirname != null );
    boolean success = true;
    try {
      // TDLog.Log( TDLog.LOG_IO, "export SHP " + filename );
      // TDPath.checkPath( filename );
      if ( TDFile.makeMSdir( dirname ) ) {
        // TDLog.v( "SHP created MSdir " + dirname );
        ArrayList< String > files = new ArrayList<>();
        int nr = 0;
        if ( TDSetting.mKmlStations ) {
          // TDLog.v( "SHP export stations ");
          for ( TDNum num : nums ) {
            String filepath = "stations-" + nr;
            ++ nr;
            List< NumStation > stations = num.getStations();
            ShpPointz shp = new ShpPointz( dirname, filepath, files );
            shp.setYYMMDD( info.date );
            success &= shp.writeStations( stations );
          }
        }

        // TDLog.v( "SHP export shots ");
        nr = 0;
        for ( TDNum num : nums ) {
          String filepath = "shots-" + nr;
          ++ nr;
          List< NumShot > shots = num.getShots();
          List< NumSplay > splays = ( TDSetting.mKmlSplays ? num.getSplays() : null );
          // TDLog.v( "SHP export " + filepath + " shots " + shots.size() );
          ShpPolylinez shp = new ShpPolylinez( dirname, filepath, files );
          shp.setYYMMDD( info.date );
          success &= shp.writeShots( shots, splays );
        }

        // if ( TDSetting.mKmlSplays ) {
        //   nr = 0;
        //   for ( TDNum num : nums ) {
        //     String filepath = "splays-" + nr;
        //     ++ nr;
        //     List< NumSplay > splays = num.getSplays();
        //     // TDLog.v( "SHP export " + filepath + " splays " + splays.size() );
        //     ShpPolylinez shp = new ShpPolylinez( dirname, filepath, files );
        //     shp.setYYMMDD( info.date );
        //     shp.writeSplays( splays );
        //   }
        // }

        List< StationInfo > cst = data.getStations( sid );
        if ( cst.size() > 0 ) {
          ArrayList< SavedStation > sst = new ArrayList<>();
          for ( StationInfo cs : cst ) {
            for ( TDNum num : nums ) {
              NumStation ns = num.getStation( cs.mName );
              if ( ns != null ) {
                sst.add( new SavedStation( cs, ns ) );
                break;
              }
            }
          }
          if ( sst.size() > 0 ) {
            String filepath = "names";
            ShpNamez shp = new ShpNamez( dirname, filepath, files );
            shp.setYYMMDD( info.date );
            shp.writeNames( sst );
          }
        }

        List< FixedInfo > fis = data.selectAllFixed( sid, TDStatus.NORMAL );
        if ( fis.size() > 0 ) {
          ArrayList< FixedStation > fst = new ArrayList<>();
          for ( FixedInfo fi : fis ) {
            NumStation ns2 = new NumStation( fi.name ); // SHP export uses only name and (e,s,v) of NumStation 
            if ( fi.hasCSCoords() ) { 
              ns2.e = fi.cs_lng * fi.mToUnits;  // cs units
              ns2.s = fi.cs_lat * fi.mToUnits;
              ns2.v = fi.h_geo  * fi.mToVUnits; // cs vert-units
            } else {
              ns2.e = fi.lng; // degrees (WGS84)
              ns2.s = fi.lat;
              ns2.v = fi.h_geo; // meters
            }
            fst.add( new FixedStation( fi, ns2 ) );
          }
          // if ( fst.size() > 0 ) {
            String filepath = "fixeds";
            ShpFixedz shp = new ShpFixedz( dirname, filepath, files );
            shp.setYYMMDD( info.date );
            shp.writeFixeds( fst );
          // }
        }   

        // for ( String file : files ) TDLog.v( "SHP export-file " + file );

        // FIXME
        // (new Archiver()).compressFiles( "shp", survey + ".shz", dirname, files );
        (new Archiver()).compressFiles( os, dirname, files );
      }
    } catch ( IOException e ) {
      TDLog.e( "Failed SHP export: " + e.getMessage() );
      return 0;
    } finally {
      // TDLog.v( "delete dir " + dirname );
      TDFile.deleteMSdir( dirname ); // delete temporary shapedir
    }
    return 1;
  }

  // ===================================================================================================
  // GEO JASON GeoJSON export
  //   NOTE shot flags are ignored

  // /** export data json file
  //  * @param bw    buffered output stream
  //  * @param sid   survey ID
  //  * @param data  database helper object
  //  * @param info  survey info
  //  * @param survey_name survey name
  //  * @return 1 success, 0 fail, 2 no geopoint
  //  */
  // static int exportSurveyAsJson( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name )
  // {
  //   final String name    = "\"name\": ";
  //   final String type    = "\"type\": ";
  //   final String item    = "\"item\": ";
  //   final String geom    = "\"geometry\": ";
  //   final String coords  = "\"coordinates\": ";
  //   final String feature = "\"Feature\"";
  //   // TDLog.v( "export as GeoJSON " + file.getName() );
  //   List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), 1.0f, true, false ); // true: ellipsoid altitude, false no convergence
  //   if ( TDUtil.isEmpty(nums) ) {
  //     TDLog.e( "Failed GeoJSON export: no geolocalized station");
  //     return 2;
  //   }

  //   // now write the GeoJSON
  //   try {
  //     // TDLog.Log( TDLog.LOG_IO, "export GeoJSON " + file.getName() );
  //     // BufferedWriter bw = TDFile.getMSwriter( "json", survey_name + ".json", "text/json" );
  //     PrintWriter pw = new PrintWriter( bw );

  //     pw.format("const geojsonObject = {\n");
  //     pw.format("  \"name\": \"%s\",\n", info.name );
  //     pw.format("  \"created\": \"%s - TopoDroid v %s\",\n",  TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
  //     pw.format("  %s \"FeatureCollection\",\n", type );
  //     pw.format("  \"features\": [\n");
  //     
  //     for ( TDNum num : nums ) {
  //       List< NumShot >    shots = num.getShots();
  //       for ( NumShot sh : shots ) {
  //         NumStation from = sh.from;
  //         NumStation to   = sh.to;
  //         if ( from.has3DCoords() && to.has3DCoords() ) {
  //           pw.format("    {\n");
  //           pw.format("      %s %s,\n", type, feature );
  //           pw.format("      %s \"centerline\",\n", item );
  //           pw.format("      %s \"%s %s\",\n", name, from.name, to.name );
  //           pw.format("      %s \"LineString\",\n", geom );
  //           pw.format(Locale.US, "      %s [ [ %.8f, %.8f, %.1f ], [ %.8f, %.8f, %.1f ] ]\n", coords, from.e, from.s, from.v, to.e, to.s, to.v );
  //           pw.format("    },\n");
  //         }
  //       }
  //     }
  //     if ( TDSetting.mKmlSplays ) {
  //       for ( TDNum num : nums ) {
  //         List< NumSplay >   splays = num.getSplays();
  //         for ( NumSplay sp : splays ) {
  //           NumStation from = sp.from;
  //           pw.format("    {\n");
  //           pw.format("      %s %s,\n", type, feature );
  //           pw.format("      %s \"splay\",\n", item );
  //           pw.format("      %s \"%s\",\n", name, from.name );
  //           pw.format("      %s \"LineString\",\n", geom );
  //           pw.format(Locale.US, "     %s [ [ %.8f, %.8f, %.1f ], [ %.8f, %.8f, %.1f ] ]\n", coords, from.e, from.s, from.v, sp.e, sp.s, sp.v );
  //           pw.format("    },\n");
  //         }
  //       }
  //     }
  //     if ( TDSetting.mKmlStations ) {
  //       for ( TDNum num : nums ) {
  //         List< NumStation > stations = num.getStations();
  //         for ( NumStation st : stations ) {
  //           pw.format("    {\n");
  //           pw.format("      %s %s,\n", type, feature );
  //           pw.format("      %s \"station\",\n", item );
  //           pw.format("      %s \"%s\",\n", name, st.name );
  //           pw.format("      %s \"Point\",\n", geom );
  //           pw.format(Locale.US, "      %s [ %.8f %.8f %.1f ]\n", coords, st.e, st.s, st.v );
  //           pw.format("    },\n");
  //         }
  //       }
  //     }
  //     pw.format("    { }\n"); // add a null feature
  //     pw.format("  ]\n");     // close features array
  //     pw.format("};\n");      // close geojson object
  //     bw.flush();
  //     bw.close();
  //     return 1;
  //   } catch ( IOException e ) {
  //     TDLog.e( "Failed GeoJSON export: " + e.getMessage() );
  //     return 0;
  //   }
  // }

  // -------------------------------------------------------------------
  // TRACK FILE GPX
  //   NOTE shot flags are ignored

  static private void printTrkpt( PrintWriter pw, NumStation st )
  {
    pw.format(Locale.US, "    <trkpt lat=\"%.6f\" lon=\"%.6f\">\n", st.s, st.e );
    pw.format(Locale.US, "      <geoidheight>%.1f</geoidheight>\n", st.v );
    pw.format(Locale.US, "      <name>%s</name>\n", st.name );
    pw.format(Locale.US, "    </trkpt>\n" );
  }

  static private void printWpt( PrintWriter pw, NumStation st )
  {
    // double e = st.e; if ( e > 180 ) e -= 360;
    pw.format(Locale.US, "<wpt lat=\"%.6f\" lon=\"%.6f\">\n", st.s, st.e );
    pw.format(Locale.US, "  <geoidheight>%.1f</geoidheight>\n", st.v );
    pw.format(Locale.US, "  <name>%s</name>\n", st.name );
    pw.format(Locale.US, "</wpt>\n");
  }

  /** export data track-file
   * @param bw    buffered output stream
   * @param data  database helper object
   * @param info  survey info
   * @param surveyname survey name
   * @return 1 success, 0 fail, 2 no geopoint
   */
  static int exportSurveyAsGpx( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String surveyname )
  {
    // TDLog.v( "export as trackfile: " + file.getName() );
    List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), 1.0f, false, false ); // false: geoid altitude, false no convergence
    if ( TDUtil.isEmpty(nums) ) {
      TDLog.e( "Failed KML export: no geolocalized station");
      return 2;
    }

    // now write the GPX file
    try {
      // TDLog.Log( TDLog.LOG_IO, "export trackfile " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "plt", surveyname + ".plt", "text/plt" );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("<?xml version=\"1.0\" ?>\n"); //  encoding=\"UTF-8\" standalone=\"no\" ?>\n" );
      pw.format("<gpx\n" );
      pw.format("  xmlns=\"http://www.topografix.com/GPX/1/1\"\n" );
      pw.format("  xmlns:xsi=\"http://www.w3c.org/2001/XMLSchema-instance\"\n" );
      pw.format("  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1\"\n" );
      pw.format("  version=\"1.1\" creator=\"TopoDroid %s\">\n", TDVersion.string() );
      pw.format("<metadata>\n");
      pw.format("<name>%s</name>\n", surveyname );
      pw.format("<time>%s</time>\n", TDUtil.getDateString("yyyy-MM-dd") );
      if ( ! TDString.isNullOrEmpty( info.comment ) ) pw.format("<desc>%s</desc>\n", info.comment );
      pw.format("</metadata>\n");

      // skip-value: 0 (usually 1)
      // track-type: 0=normal, 10=closed_polygon, 20=alarm_zone
      // fill-style: 0=solid, 1=clear, 2=Bdiag, 3=Fdiag, 4=cross, 5=diag_cross, 6=horiz, 7=vert

      // Calendar cal = Calendar.getInstance();
      // cal.set(1996, Calendar.JANUARY, 1);
      // long diff = System.currentTimeMillis() - cal.getTimeInMillis();
      // long days = 35065 + diff / 86400000L; // 24*60*60*1000 // FIXME +33 ?
      // String date = TDUtil.getDateString( "dd-MMM-yy" );

      double minlat = 1;
      double maxlat = 0;
      double minlon = 0;
      double maxlon = 0;
      for ( TDNum num : nums ) {
        List< NumStation > stations = num.getStations();
        for ( NumStation st : stations ) {
          if ( minlat > maxlat ) { // initialize
            minlat = maxlat = st.s;
            minlon = maxlon = st.e;
          } else {
            if ( minlat > st.s )      { minlat = st.s; }
            else if ( maxlat < st.s ) { maxlat = st.s; }
            if ( minlon > st.e )      { minlon = st.e; }
            else if ( maxlon < st.e ) { maxlon = st.e; }
          }
        }
      }
      pw.format(Locale.US, "<bounds minlat=\"%.6f\" maxlat=\".6f\" minlon=\"%.6f\" maxlon=\"%.6f\" />\n", minlat, maxlat, minlon, maxlon );

      for ( TDNum num : nums ) {
        List< NumStation > stations = num.getStations();
        for ( NumStation st : stations ) {
          printWpt( pw, st );
        }
        List< NumShot >    shots = num.getShots();
        // List< NumSplay >   splays = num.getSplays();
        pw.format("<trk>\n");
        pw.format("  <name>%s</name>\n", surveyname );
        NumStation last = null;
        for ( NumShot sh : shots ) {
          NumStation from = sh.from;
          NumStation to   = sh.to;
          if ( from != last ) {
            if ( last != null ) pw.format("  </trkseg>\n");
            pw.format("  <trkseg>\n");
            printTrkpt( pw, from );
          }
          printTrkpt( pw, to );
          last = to;
        }
        if ( last != null ) pw.format("  </trkseg>\n");
        pw.format("</trk>\n");
      }
      pw.format("</gpx>\n");


      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed GPX export: " + e.getMessage() );
      return 0;
    }
  }

  // -------------------------------------------------------------------
  // TRACK FILE OZIEXPLORER
  //   NOTE shot flags are ignored

  /** export data track-file
   * @param bw    buffered output stream
   * @param sid   survey ID
   * @param data  database helper object
   * @param info  survey info
   * @param survey_name survey name (unused)
   * @return 1 success, 0 fail, 2 no geopoint
   *
  static int exportSurveyAsPlt( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name )
  {
    // TDLog.v( "export as trackfile: " + file.getName() );
    List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), TDUtil.M2FT, false, false ); // false geoid alt. - false no convergence
    if ( TDUtil.isEmpty(nums) ) {
      TDLog.e( "Failed PLT export: no geolocalized station");
      return 2;
    }

    // now write the PLT file
    try {
      // TDLog.Log( TDLog.LOG_IO, "export trackfile " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "plt", survey_name + ".plt", "text/plt" );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("OziExplorer Track Point File Version 2.1\r\n");
      pw.format("WGS 84\r\n");
      pw.format("Altitude is in Feet\r\n");
      pw.format("Reserved 3\r\n");

      // skip-value: 0 (usually 1)
      // track-type: 0=normal, 10=closed_polygon, 20=alarm_zone
      // fill-style: 0=solid, 1=clear, 2=Bdiag, 3=Fdiag, 4=cross, 5=diag_cross, 6=horiz, 7=vert
      //
      pw.format("0,2,1677690,%s - TopoDroid v %s,0,0,0,8421376,-1,0\r\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );

      int tot_stations = 0;
      for ( TDNum num : nums ) {
        List< NumStation > stations = num.getStations();
        // List< NumShot >    shots = num.getShots();
        // List< NumSplay >   splays = num.getSplays();
	tot_stations += stations.size();
      }
      pw.format("%d\r\n", tot_stations );
      
      // date should be "days_since_12/30/1899.time_of_the_day"
      // eg, 0=12/30/1899, 2=1/1/1900, 35065=1/1/1996, 36526=1/1/00, 39447=1/1/08, 40908=1/1/12, ...
      Calendar cal = Calendar.getInstance();
      cal.set(1996, Calendar.JANUARY, 1);
      long diff = System.currentTimeMillis() - cal.getTimeInMillis();
      long days = 35065 + diff / 86400000L; // 24*60*60*1000 // FIXME +33 ?

      // String date = TDUtil.getDateString( "dd-MMM-yy" );

      for ( TDNum num : nums ) {
        List< NumStation > stations = num.getStations();
        List< NumShot >    shots = num.getShots();
        // List< NumSplay >   splays = num.getSplays();
        NumStation last = null;
        for ( NumShot sh : shots ) {
          NumStation from = sh.from;
          NumStation to   = sh.to;
          if ( from != last ) {
            pw.format(Locale.US, "%.8f, %.8f,1, %.1f,%d,,\r\n", from.e, from.s, from.v, days );
          }
          pw.format(Locale.US, "%.8f,%.8f,0,%.1f,%d,,\r\n", to.e, to.s, to.v, days );
          last = to;
        }
      }

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed PLT export: " + e.getMessage() );
      return 0;
    }
  }
  */

  // #############################################################################################################
  // =======================================================================
  // POCKETTOPO EXPORT PocketTopo
  //   NOTE shot flags are ignored

  /** export data in PocketTopo format (.top)
   * @param os    output stream
   * @param sid   survey ID
   * @param data  database helper object
   * @param info  survey info
   * @param sketch     sketching window (not used: sketches are not exported)
   * @param origin     sketch origin
   * @param survey_name survey name (unused)
   * @return 1 success, 0 fail
   */
  static int exportSurveyAsTop( OutputStream os, long sid, DataHelper data, SurveyInfo info, DrawingWindow sketch, String origin, String survey_name )
  {
    // TDLog.v( "export as pockettopo: " + file.getName() );
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
      ptfile.addTrip( Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]), info.getDeclination(), info.comment );
    } catch ( NumberFormatException e ) {
      TDLog.e( "export survey as TOP date parse error " + info.date );
    }

    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    int extend = 0;  // current extend
    DBlock ref_item = null;
    int fromId, toId;

    for ( DBlock item : list ) {
      String from = item.mFrom;
      String to   = item.mTo;
      extend = item.getIntExtend();
      if ( from == null || from.length() == 0 ) {
        from = "";
        if ( to == null || to.length() == 0 ) {
          to = "";
          if ( ref_item != null 
            && ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
            from = ref_item.mFrom;
            to   = ref_item.mTo;
            extend = ref_item.getIntExtend();
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
      ptfile.addShot( (short)0, from, to, item.mLength, item.mBearing, item.mClino, item.mRoll, extend, item.mComment );
    }

    // if ( sketch != null ) {
    //   // TODO add sketch
    // }

    try {
      // FileOutputStream fos = TDFile.getFileOutputStream( file );
      // OutputStream os = TDFile.getMSoutput( "top", survey_name + ".top", "application/octet-stream" );
      ptfile.write( os );
      os.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed PocketTopo export: " + e.getMessage() );
      return 0;
    }
  }
  // =======================================================================
  // THERION EXPORT Therion
  //   NOTE handled flags: duplicate surface

  /** write a leg in therion format
   * @param pw    print stream
   * @param leg   average leg
   * @param ul    length unit factor
   * @param ua    angle unit factor
   */
  static private void writeThLeg( PrintWriter pw, AverageLeg leg, float ul, float ua )
  {
    pw.format(Locale.US, "%.2f %.1f %.1f\n", leg.length() * ul, leg.bearing() * ua, leg.clino() * ua );
    leg.reset();
  }

  /** write the extend of a splay
   * @param pw    print stream
   * @param item  splay data-block
   * @param splay_extend (?)
   * @param extend  current extend value
   * @return if this return 1, the variable extend must be updated
   *         if this return 0 or 1, splay_extend must be updated false or true, respectively
   * note extend values start from -1
   */
  static private int writeSplayExtend( PrintWriter pw, DBlock item, boolean splay_extend, int extend )
  {
    int item_extend = item.getIntExtend();
    if ( item_extend > 1 ) {
      if ( splay_extend ) {
        pw.format( extend_auto );
        return 0; // splay_extend <- false
      }
    } else if ( item_extend != extend || ! splay_extend ) {
      pw.format("    extend %s\n", therion_extend[1+item_extend] );
      // if ( splay_stretch != 0 ) pw.format(Locale.US, "    extend %d\n", (int)(100 + 100 * splay_stretch ) );
      return 1; // extend <- item_extend, splay_extend <- true
    }
    return -1;
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // Therion scraps and maps

  /** write the map commands 
   * @param pw     output writer
   * @param info   survey info
   * @param plots  list of survey sketch files
   */
  static private void doTherionMaps( PrintWriter pw, SurveyInfo info, List< PlotInfo > plots )
  {
    if ( TDUtil.isEmpty(plots) ) return;
    for ( PlotInfo plt : plots ) {
      String subdir = TDInstance.survey + "/tdr"; // plot files
      String plotname =  info.name + "-" + plt.name;
      if (  TDFile.hasMSfile( subdir, plotname + ".tdr" ) ) {
        pw.format("  # input \"%s.th2\"\n", plotname );
      }
    } 
    pw.format("\n");
    for ( PlotInfo plt : plots ) {
      if ( PlotType.isSketch2D( plt.type ) ) {
        int scrap_nr = plt.maxscrap;
        String subdir = TDInstance.survey + "/tdr"; // plot files
        String plotname =  info.name + "-" + plt.name;
        if ( TDFile.hasMSfile( subdir, plotname + ".tdr" ) ) {
          if ( plt.type == PlotType.PLOT_PROJECTED ){
            pw.format("  # map m%s -projection [%s %d]\n", plt.name, PlotType.projName( plt.type ) , (int) plt.azimuth);
          } else {
            pw.format("  # map m%s -projection %s\n", plt.name, PlotType.projName( plt.type ) );
          }
          pw.format("  #   %s\n", plotname );
          for ( int k=1; k<=scrap_nr; ++k) pw.format("  #   %s%d\n", plotname, k );
          pw.format("  # endmap\n");
        } 
      }
    }
    pw.format("\n");
  }

  /** output the survey stations of a data line
   * @param pw     output writer
   * @param from   FROM station
   * @param to     TO station
   * @param cmtd   whether prepend a comment on the data line
   */
  static private void writeThStations( PrintWriter pw, String from, String to, boolean cmtd )
  {
    if ( cmtd ) {
      pw.format("#   %s %s ", from, to );
    } else {
      pw.format("    %s %s ", from, to );
    }
  }

  /** possible output the fractional extend
   * @param pw       output writer
   * @param item     work item 
   * @return current stretch
   */
  static private int checkThStretch( PrintWriter pw, DBlock item )
  {
    int item_stretch = item.hasStretch()? (int)(100 + 100 * item.getStretch() ) : 100;
    if ( item_stretch < 0 ) {
      item_stretch = 0;
    } else if ( item_stretch > 200 ) {
      item_stretch = 200;
    }
    return item_stretch;
  }

  /** count spaces in a string
   * @param str   input string
   * @return number of spaces
   */
  static int countSpaces(String str) {
    int spaceCount = 0;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == ' ') {
        spaceCount++;
      }
    }
    return spaceCount;
  }

  /** replace the last space with a slash
   * @param str   input string
   * @return output string
   */
  static String replaceLastSpaceWithSlash(String str) {
    int lastSpaceIndex = str.lastIndexOf(' ');
    if (lastSpaceIndex != -1) {
      return str.substring(0, lastSpaceIndex) + "/" + str.substring(lastSpaceIndex + 1);
    } else {
      return str; // No space found, return original string
    }
  }

  /** export survey data in Therion format (.th)
   * @param bw      output writer
   * @param sid     survey ID
   * @param data    database object
   * @param info    survey info
   * @param surveyname survey name
   * @return ???
   */
  static int exportSurveyAsTh( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String surveyname )
  {
    boolean with_thconfig = TDSetting.mTherionWithConfig;
    boolean embed_thconfig = false;

    if ( with_thconfig ) {
      if ( TDSetting.mTherionEmbedConfig ) { 
        embed_thconfig = true;
      } else { // write thconfig file
        String thconfig = TDPath.getOutFile( surveyname + ".thconfig" );
        // File dir = TDFile.getFile( thconfig );
        // if ( ! dir.exists() ) dir.mkdirs();
        // TDLog.v("thconfig: " + thconfig );
        try {
          // BufferedWriter bcw = TDFile.getMSwriter( "thconfig", surveyname + ".thconfig", "text/thconfig" );
          BufferedWriter bcw = new BufferedWriter( new FileWriter( thconfig ) );
          PrintWriter pcw = new PrintWriter( bcw );
          pcw.format("encoding utf-8\n");
          pcw.format("# %s created by TopoDroid v %s\n\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
          pcw.format("source \"./%s.th\"\n\n", info.name );
          pcw.format("layout topodroid\n");
          pcw.format("  legend on\n");
          pcw.format("  symbol-hide group centerline\n");
          pcw.format("  symbol-show point station\n");
          pcw.format("  debug station-names\n");
          pcw.format("endlayout\n");
          pcw.format("\n");
          pcw.format("export map -layout topodroid -o %s-p.pdf -proj plan \n\n", info.name );
          pcw.format("export map -layout topodroid -o %s-s.pdf -proj extended \n\n", info.name );
          bcw.flush();
          bcw.close();
        } catch ( IOException e ) {
          TDLog.e( "Failed Therion config export: " + e.getMessage() );
        }
      }
    }

    // TDLog.v( "export as therion: " + file.getName() );
    // TDLog.v( "export " + info.name + " as therion: " + surveyname );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;

    String uls = ( ul < 1.01f )? "meters"  : "feet"; // FIXME
    String uas = ( ua < 1.01f )? "degrees" : "grads";
    // String uls = TDSetting.mUnitLengthStr;
    // String uas = TDSetting.mUnitAngleStr;

    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    List< DBlock > c_list = data.selectAllExportShots( sid, TDStatus.CHECK );
    checkShotsClino( list );

    List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    List< PlotInfo > plots  = data.selectAllPlots( sid, TDStatus.NORMAL );
    List< StationInfo > stations = data.getStations( sid );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export Therion " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "th", surveyname + ".th", "text/th" );
      if ( bw == null ) {
        TDLog.e("cannot get MS therion file");
        return 0;
      }
      PrintWriter pw = new PrintWriter( bw );

      pw.format("encoding utf-8\n");
      pw.format("# %s created by TopoDroid v %s\n\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );

      if ( embed_thconfig /* && TDSetting.mExportUri */ ) { // embed thconfig
        pw.format("layout topodroid\n");
        pw.format("  legend on\n");
        pw.format("  symbol-hide group centerline\n");
        pw.format("  symbol-show point station\n");
        pw.format("  debug station-names\n");
        pw.format("endlayout\n");
        pw.format("\n");
        pw.format("source # \"../th/%s.th\"\n\n", info.name );
        bw.flush();
      }

      String title = TDString.underscoresToSpaces( info.name );
      pw.format("survey %s -title \"%s\"\n", info.name, title );
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format("    # %s \n", info.comment );
      }
      pw.format("\n");

      if ( c_list.size() > 0 ) {
        pw.format("# calibration-check\n");
        for ( DBlock blk : c_list ) { // calib-check shots
          pw.format("# %s %s :", blk.mFrom, blk.mTo );
          pw.format(Locale.US, " %.2f %.1f %.1f", blk.mLength, blk.mBearing, blk.mClino);
          pw.format(Locale.US, " %.1f %.1f %.1f", blk.mAcceleration, blk.mMagnetic, blk.mDip );
          if ( blk.mComment != null && blk.mComment.length() > 0 ) {
            pw.format(" %s", blk.mComment );
          }
          pw.format("\n");
        }
        pw.format("\n");
      }
      
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
        String[] names = info.team.replaceAll("[;|\\/]", ",").replaceAll("\\s+", " ").split(",");
        int len = names.length;
        int k = 0;
        while ( k<len ) {
          String name = names[k].trim();
          if ( name.length() == 0 ) {
            continue;
          }
          int spaceCount = countSpaces(name);
          if (spaceCount > 1) {
            name = replaceLastSpaceWithSlash(name);
          }
          pw.format("    team \"%s\"\n", name);
          ++k;
        }
      }

      if ( info.hasDeclination() ) { // DECLINATION in Therion
        pw.format(Locale.US, "    # declination %.4f degrees\n", info.declination );
      }

      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
      // mark ... 
      // station ...
      
      if ( stations.size() > 0 ) {
        pw.format("\n");
        StringBuilder sb_fixed   = new StringBuilder();
        StringBuilder sb_painted = new StringBuilder();
        for ( StationInfo station : stations ) {
          if ( station.mFlag.isFixed() ) { 
            sb_fixed.append(" ");
            sb_fixed.append( station.mName );
          } else if ( station.mFlag.isPainted() ) {
            sb_painted.append(" ");
            sb_painted.append( station.mName );
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

      boolean splay_extend = true;
      int extend = 0;  // current extend
      int stretch = 100; // therion value for no stretch, which corresponds to topodroid 0
      // int extend_ratio = 100; // 100 percent, ie no shrink nor stretch
      AverageLeg leg = new AverageLeg(0);
      HashMap<String, LRUD> lruds = null;
      if ( TDSetting.mSurvexLRUD ) {
        lruds = new HashMap<String, LRUD>();
      }

      DBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO for Therion
      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null &&
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
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
            int tmp = writeSplayExtend( pw, item, splay_extend, extend );
            if ( tmp == 0 ) {
              splay_extend = false;
            } else if ( tmp == 1 ) {
              splay_extend = true;
              extend = item.getIntExtend();
            }

            writeThStations( pw, ( item.isXSplay() ? "-" : "." ), to, item.isCommented() );
            pw.format(Locale.US, "%.2f %.1f %.1f\n", item.mLength * ul, item.mBearing * ua, item.mClino * ua );
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
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
              // write LRUD for ref_item
              if ( TDSetting.mSurvexLRUD ) {
                // lruds.putIfAbsent( ref_item.mFrom, computeLRUD( ref_item, list, true ) );
                if ( ! lruds.containsKey( ref_item.mFrom) ) lruds.put( ref_item.mFrom, computeLRUD( ref_item, list, true ) );
              }
              ref_item = null; 
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            int tmp = writeSplayExtend( pw, item, splay_extend, extend );
            if ( tmp == 0 ) {
              splay_extend = false;
            } else if ( tmp == 1 ) {
              splay_extend = true;
              extend  = item.getIntExtend();
              // item_stretch = checkThStretch( pw, item );
            }
            
            writeThStations( pw, from, ( item.isXSplay() ? "-" : "." ), item.isCommented() );
            pw.format(Locale.US, "%.2f %.1f %.1f\n", item.mLength * ul, item.mBearing * ua, item.mClino * ua );
          } else { // with both FROM and TO stations
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
            int item_stretch = checkThStretch( pw, item );
            int item_extend  = item.getIntExtend();
            int tmp_extend   = extend;
            if ( item_extend != tmp_extend ) {
              tmp_extend = item_extend;
            }
            if ( tmp_extend == 0 ) {
              if ( item_stretch > 100 ) {
                tmp_extend = 1;
                item_stretch = item_stretch - 100;
              } else if ( item_stretch < 100 ) {
                tmp_extend = -1;
                item_stretch = 100 - item_stretch;
              }
            } 
            if ( item_stretch != stretch ) {
              stretch = item_stretch;
              pw.format(Locale.US, "    extend %d\n", stretch );
            }
            if ( tmp_extend != extend || ! splay_extend )
            {
              extend = tmp_extend;
              pw.format("    extend %s\n", therion_extend[1+extend] );
              // handle extend stretch
              // if ( item.hasStretch() ) {
              //   int ratio = (int)( 100 * (1 + item.getStretch() ) );
              //   if ( ratio < 0 ) { ratio = 0; } else if ( ratio > 200 ) { ratio = 200; }
              //   if ( ratio != extend_ratio ) { 
              //     extend_ratio = ratio;
              //     pw.format("    extend %d\n", extend_ratio );
              //   }
              // } else if ( extend_ratio != 100 ) {
              //   extend_ratio = 100;
              //   pw.format("    extend %d\n", extend_ratio );
              // }
	      splay_extend = true;
            } else {

            }
            if ( item.isDuplicate() ) {
              pw.format(therion_flags_duplicate);
              duplicate = true;
            } else if ( item.isSurface() ) {
              pw.format(therion_flags_surface);
              surface = true;
            // } else if ( item.isCommented() ) { // handled already
            // } else if ( item.isBackshot() ) {
            //   pw.format(therion_flags_duplicate);
            //   duplicate = true;
            }
            if ( item.mComment != null && item.mComment.length() > 0 ) {
              pw.format("  # %s\n", item.mComment );
            }
            writeThStations( pw, from, to, item.isCommented() );
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

      if ( TDSetting.mSurvexLRUD ) {
        pw.format("    units left right up down %s\n", uls );
        pw.format("    data dimensions station left right up down\n");
        for ( String station : lruds.keySet() ) {
          LRUD lrud = lruds.get( station );
          pw.format(Locale.US, "    %s %.2f %.2f %.2f %.2f\n", station, lrud.l * ul, lrud.r * ul, lrud.r * ul, lrud.d * ul );
        }
      }

      pw.format("  endcenterline\n\n");

      if ( ! TDSetting.mTherionMaps ) doTherionMaps( pw, info, plots );

      pw.format("endsurvey\n");
      bw.flush();

      if ( embed_thconfig /* && TDSetting.mExportUri */ ) { // end embed thconfig file 
        pw.format("endsource\n");
        pw.format("\n");
        pw.format("export map -layout topodroid -o %s-p.pdf -proj plan \n\n", info.name );
        pw.format("export map -layout topodroid -o %s-s.pdf -proj extended \n\n", info.name );
        bw.flush();
      }

      bw.close();
      // TDLog.v( "exported therion file");

      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed Therion export: " + e.getMessage() );
      return 0;
    }
  }

  // -----------------------------------------------------------------------
  /** SURVEX EXPORT 
   *    NOTE handled flags: duplicate
   *    N.B. surface treated as duplicate
   *
   * The following format is used to export the centerline data in survex
   *
   *    *begin survey_name
   *      *units tape feet|metres
   *      *units compass clino grads|degrees
   *      *calibrate declination ...
   *      *date yyyy.mm.dd
   *      ; *fix station long lat alt-ell
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
   *
   */
  static private final String survex_flags_duplicate     = "   *flags duplicate";
  static private final String survex_flags_not_duplicate = "   *flags not duplicate";
  // static String   survex_flags_surface       = "   *flags surface";
  // static String   survex_flags_not_surface   = "   *flags not surface";

  private static String splayChar = "a";
  private static void resetSplayChar() { splayChar = "a"; }
  private static void incSplayChar()
  {
    char[] ch = splayChar.toCharArray();
    int k = splayChar.length() - 1;
    while ( k >= 0 ) {
      if ( ch[k] == 'z' ) { ch[k] = 'a'; --k; } else { ++ch[k]; break; }
    }
    if ( k > 0 ) {
      splayChar = new String( ch );
    } else {
      splayChar = new String( ch ) + "a";
    }
  }

  /** write a line in the survex file
   * @param pw     output writer
   * @param str    line
   */
  static private void writeSurvexLine( PrintWriter pw, String str )
  {
    pw.format("%s%s", str, TDSetting.mSurvexEol );
  }

  /** write an end-of-line in the survex file
   * @param pw     output writer
   */
  static private void writeSurvexEOL( PrintWriter pw )
  {
    pw.format("%s", TDSetting.mSurvexEol );
  }

  /** write an data line in the survex file
   * @param pw     output writer
   * @param first  whether first shot of a leg
   * @param dup    whether leg is duplicate
   * @param leg    leg average values
   * @param blk    data block
   * @param ul     unit of length
   * @param ua     unit of angle
   * @return always false
   */
  static private boolean writeSurvexLeg( PrintWriter pw, boolean first, boolean dup, AverageLeg leg, DBlock blk, float ul, float ua )
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

  /** write the LRUD in the survex file
   * @param pw     output writer
   * @param st     string to prepend to the LRUD values
   * @param lrud   LRUD
   * @param ul     unit of length
   */
  static private void writeSurvexLRUD( PrintWriter pw, String st, LRUD lrud, float ul )
  {
    if ( lrud != null ) {
      pw.format(Locale.US, "%s  %.2f %.2f %.2f %.2f", st, lrud.l * ul, lrud.r * ul, lrud.u * ul, lrud.d * ul );
      writeSurvexEOL( pw );
    }
  }

  static private void writeSurvexSplay( PrintWriter pw, String from, String to, DBlock blk, float ul, float ua )
  {
    if ( blk.isCommented() ) {
      pw.format(Locale.US, "; %s %s %.2f %.1f %.1f", from, to, blk.mLength * ul, blk.mBearing * ua, blk.mClino * ua );
    } else {
      pw.format(Locale.US, "  %s %s %.2f %.1f %.1f", from, to, blk.mLength * ul, blk.mBearing * ua, blk.mClino * ua );
    }
    if ( blk.mComment != null && blk.mComment.length() > 0 ) {
      pw.format(" ; %s", blk.mComment );
    }
    writeSurvexEOL( pw );
  }

  /** export survey data in SVX (Survex) format
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsSvx( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, Device device, String survey_name )
  {
    // TDLog.v( "export as survex: " + file.getName() );

    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    String uls = ( ul < 1.01f )? "meters"  : "feet"; // FIXME
    String uas = ( ua < 1.01f )? "degrees" : "grads";

    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    List< DBlock > st_blk = new ArrayList<>(); // blocks with from station (for LRUD)

    // float decl = info.getDeclination(); // DECLINATION not used
    try {
      // TDLog.Log( TDLog.LOG_IO, "export Survex " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "svx", survey_name + ".svx", "text/svx" );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("; %s created by TopoDroid v %s", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
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
      if ( info.team != null && info.team.length() > 0 ) {
        String[] names = info.team.replaceAll("[;|\\/]", ",").replaceAll("\\s+", " ").split(",");
        int len = names.length;
        int k = 0;
        while ( k<len ) {
          String name = names[k].trim();
          if ( name.length() == 0 ) {
            continue;
          }
          pw.format("  *team \"%s\" ", name);  writeSurvexEOL(pw);
          ++k;
        }
      }
      writeSurvexLine(pw, "  *units tape " + uls );
      writeSurvexLine(pw, "  *units compass " + uas );
      writeSurvexLine(pw, "  *units clino " + uas );
      if ( info.hasDeclination() ) { // DECLINATION in Survex
        pw.format(Locale.US, "  *declination %.2f degrees", info.declination ); // units DEGREES
        writeSurvexEOL(pw);
      // } else {
      //   pw.format(Locale.US, "  *calibrate declination auto" );
      //   writeSurvexEOL(pw);
      }
      if ( ! TDSetting.mSurvexSplay ) {
        writeSurvexLine( pw, "  *alias station - .." );
      }

      if ( fixed.size() > 0 ) {
        // do we need "*cs out EPSG:..." ?
        // without "cs out" survex does not process the data, however there is no output-cs in topodroid
        // the output crs could be the CRS of the first fix, if specified
        //
        if ( TDSetting.mSurvexEPSG > 0 ) {
          writeSurvexLine(pw, "  *cs LONG-LAT");
          writeSurvexLine(pw, "  *cs out EPSG:" +  TDSetting.mSurvexEPSG);
          for ( FixedInfo fix : fixed ) {
            writeSurvexLine(pw, "  *fix " + fix.toExportStringEllipsoid() );
          }
        } else {
          // writeSurvexLine(pw, "  ; fix stations as long-lat h_ell");
          writeSurvexLine(pw, "  ; *cs LONG-LAT");
          for ( FixedInfo fix : fixed ) {
            writeSurvexLine(pw, "  ; *fix " + fix.toExportStringEllipsoid() );
          }
          // for ( FixedInfo fix : fixed ) {
          //   writeSurvexLine(pw, "  *fix " + fix.name );
          //   break;
          // }
        }
      }

      writeSurvexLine( pw, "  *flags not splay");
      writeSurvexLine( pw, "  *data normal from to tape compass clino");
      
      boolean first = true; // first-pass
      // first pass legs, second pass splays
      // for ( int k=0; k<2; ++k ) 
      {
        // if ( ! first ) {
        //   writeSurvexEOL(pw);
        //   writeSurvexLine(pw, "  *flags splay");
        // }
        AverageLeg leg = new AverageLeg(0);
        DBlock ref_item = null;
        boolean duplicate = false;
        boolean splays = false;
	resetSplayChar();
        for ( DBlock item : list ) {
          String from = item.mFrom;
          String to   = item.mTo;
          if ( TDString.isNullOrEmpty( from ) ) {
            if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
              if ( ref_item != null &&
                 ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
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
                splays = true;
              } else {
                incSplayChar();
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
            if ( TDString.isNullOrEmpty( to ) ) { // splay shot
              if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
                duplicate = writeSurvexLeg( pw, first, duplicate, leg, ref_item, ul, ua );
                if ( TDSetting.mSurvexLRUD ) st_blk.add( ref_item );
                ref_item = null; 
              }

              if ( ! splays ) {
                if ( TDSetting.mSurvexSplay ) writeSurvexLine(pw, "  *flags splay" );
                splays = true;
              } else {
                incSplayChar();
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
                resetSplayChar();
              }
              ref_item = item;
              if ( item.isDuplicate() || item.isSurface() ) { // FIXME SURFACE
                /* if ( first ) */ writeSurvexLine(pw, survex_flags_duplicate);
                duplicate = true;
              }
              // if ( first ) {
                if ( item.isCommented() ) {
                  pw.format(";   %s %s ", from, to );
                } else {
                  pw.format("    %s %s ", from, to );
                }
              // }
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

      // TDLog.v( "Station blocks " + st_blk.size() );
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
          TDNum num = new TDNum( list, from, null, null, 0.0f, null ); // no declination, null formatClosure
          List< NumBranch > branches = num.makeBranches( true );
          // TDLog.v( "Station " + from + " shots " + num.shotsNr() + " splays " + num.splaysNr()
          //               + " branches " + branches.size() );

          for ( NumBranch branch : branches ) {
            // ArrayList< String > stations = new ArrayList<>();
            ArrayList< NumShot > shots = branch.shots;
            int size = shots.size();
            // TDLog.v( "branch shots " + size );
            if ( size > 0 ) {
              if ( do_header ) {
                pw.format("*units left %s", uls );  writeSurvexEOL( pw );
                pw.format("*units right %s", uls ); writeSurvexEOL( pw );
                pw.format("*units up %s", uls );    writeSurvexEOL( pw );
                pw.format("*units down %s", uls );  writeSurvexEOL( pw );
                do_header = false;
              }
              pw.format("*data passage station left right up down"); writeSurvexEOL( pw );
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
              DBlock blk0 = sh.getFirstBlock();
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
                  DBlock blk = sh.getFirstBlock();
                  if ( k == 1 ) {
                    // TDLog.v( blk0.mFrom + "-" + blk0.mTo + " branch dir " + sh.mBranchDir + " blk dir " + sh.mDirection );
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
                  if ( blk.mTo.equals( st_name ) ) { // 20230118 swapped strings
                    writeSurvexLRUD( pw, blk.mFrom, computeLRUD( blk, list, true ), ul );
                    st_name = blk.mFrom;
                  } else if ( st_name.equals( blk.mFrom ) ) {
                    writeSurvexLRUD( pw, blk.mTo, computeLRUD( blk, list, false ), ul );
                    st_name = blk.mTo;
                  } else {
                    TDLog.e("ERROR unattached branch shot " + sh.from.name + " " + sh.to.name + " station " + st_name );
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
        //   if ( TDString.isNullOrEmpty( from ) ) {
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
        //   for ( DBlock blk : st_blk ) {
        //     writeSurvexLRUD( pw, blk.mFrom, computeLRUD( blk, list, true ), ul );
        //   }
        // }
      }
      pw.format("*end %s", info.name ); writeSurvexEOL(pw);
      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed Survex export: " + e.getMessage() );
      return 0;
    }
  }

  // -----------------------------------------------------------------------
  /** CSV COMMA-SEPARATED VALUES EXPORT 
   *  shot flags are used
   *  NOTE declination exported in comment only in CSV
   *       handled flags: duplicate surface commented 
   */
  static private void writeCsvLeg( PrintWriter pw, AverageLeg leg, float ul, float ua, int leg_extend, char sep )
  {
    pw.format(Locale.US, "%c%.2f%c%.1f%c%.1f%c%d", sep, leg.length() * ul, sep, leg.bearing() * ua, sep, leg.clino() * ua, sep, leg_extend );
    leg.reset();
    // leg_extend is not to be reset
  }

  static private void writeCsvFlag( PrintWriter pw, boolean dup, boolean sur, boolean cmtd, String comment, char sep, String newline )
  {
    if ( dup ) {
      if ( sur ) {
        if ( cmtd ) {
          pw.format("%cLSC", sep );
        } else {
          pw.format("%cLS", sep );
        }
      } else {
        if ( cmtd ) {
          pw.format("%cLC", sep );
        } else {
          pw.format("%cL", sep );
        }
      }
    } else {
      if ( sur ) {
        if ( cmtd ) {
          pw.format("%cSC", sep );
        } else {
          pw.format("%cS", sep );
        }
      } else {
        if ( cmtd ) {
          pw.format("%cC", sep );
        } else {
          pw.format("%c", sep );
        }
      }
    }
    if ( comment == null ) {
      pw.format("%c%s", sep, newline );
    } else {
      pw.format("%c%s%s", sep, TDString.escapeSeparator(sep, comment), newline );
    }
  }

  /** export survey data in CSV (comma separated values) format, including raw data
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsRawCsv( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name )
  {
    char sep = TDSetting.mCsvSeparator;
    List< RawDBlock > list = data.selectAllShotsRawData( sid );
    String newline = TDSetting.mSurvexEol;
    try {
      // BufferedWriter bw = TDFile.getMSwriter( "csv", survey_name + ".csv", "text/csv" );
      PrintWriter pw = new PrintWriter( bw );
      pw.format("# %s [*] created by TopoDroid v %s%s", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string(), newline );
      pw.format("# %s%s", info.name, newline );
      pw.format("# id, from, to, dist, azi, clino, roll, G, M, dip, time, type, addres, extend, flag, leg-type, status, Mx, My, Mz, Gx, Gy, Gz, comment%s", newline );
      for ( RawDBlock b : list ) {
	// String f = ( b.mFrom == null )? "" : b.mFrom;
	// String t = ( b.mTo   == null )? "" : b.mTo;
        pw.format(Locale.US, "%d%c%s%c%s%c", b.mId, sep, b.mFrom, sep, b.mTo, sep );
        pw.format(Locale.US, "%.3f%c%.2f%c%.2f%c%.2f%c%.2f%c%.2f%c%.2f%c",
	  b.mLength, sep, b.mBearing, sep, b.mClino, sep, b.mRoll, sep, b.mAcceleration, sep, b.mMagnetic, sep, b.mDip, sep );
        String address = b.mAddress;
        if ( TDString.isNullOrEmpty( address ) ) address = "-";
        pw.format(Locale.US, "%d%c%d%c%s%c", b.mTime, sep, b.getShotType(), sep, address, sep );
        pw.format(Locale.US, "%d%c%d%c%d%c%d%c", b.mExtend, sep, b.mFlag, sep, b.mLeg, sep, b.mStatus, sep );  // NOTE mLeg is not mBlockType
        pw.format(Locale.US, "%d%c%d%c%d%c%d%c%d%s%d%c", b.mRawMx, sep, b.mRawMy, sep, b.mRawMz, sep, b.mRawGx, sep, b.mRawGy, sep, b.mRawGz, sep );
        pw.format(Locale.US, "%s%s", TDString.escapeSeparator(sep, b.mComment), newline );
      }
      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed CSV export: " + e.getMessage() );
      return 0;
    }
  }

  /** export survey data in CSV (comma separated values) format
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsCsv( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name )
  {
    char sep = TDSetting.mCsvSeparator;
    String newline = TDSetting.mSurvexEol;
    // TDLog.v( "export as CSV: " + file.getName() );
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    // List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    String uls = ( ul < 1.01f )? "meters"  : "feet"; // FIXME
    String uas = ( ua < 1.01f )? "degrees" : "grads";
    try {
      // TDLog.Log( TDLog.LOG_IO, "export CSV " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "csv", survey_name + ".csv", "text/csv" );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("# %s created by TopoDroid v %s%s", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string(), newline );
      pw.format("# %s%s", info.name, newline );
      // if ( fixed.size() > 0 ) {
      //   pw.format("  ; fix stations as long-lat h_geo\n");
      //   for ( FixedInfo fix : fixed ) {
      //     pw.format("  ; *fix %s\n", fix.toExportString() );
      //   }
      // }
      if ( info.hasDeclination() ) { // DECLINATION in CSV
        pw.format(Locale.US, "# from to tape compass clino extend flags (declination %.4f)%s", info.declination, newline ); 
      } else {
        pw.format(Locale.US, "# from to tape compass clino extend flags (declination undefined)%s", newline );
      }
      pw.format(Locale.US, "# units tape %s compass clino %s%s", uls, uas, newline );
      
      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false;
      boolean splays = false;
      int leg_extend = 1; // RIGHT
      int extend;
      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsvLeg( pw, leg, ul, ua, leg_extend, sep );
              writeCsvFlag( pw, duplicate, surface, ref_item.isCommented(), ref_item.mComment, sep, newline );
              duplicate = false; // reset flags
              surface   = false;
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.US, "-%c%s@%s%c%.2f%c%.1f%c%.1f%c",
                      sep, to, info.name, sep, item.mLength * ul, sep, item.mBearing * ua, sep, item.mClino * ua, sep );
            extend = item.getIntExtend();
            if ( extend <= 1 ) pw.format("%d", extend );
            writeCsvFlag( pw, false, false, item.isCommented(), item.mComment, sep, newline );

            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", item.mComment );
            // }
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              writeCsvLeg( pw, leg, ul, ua, leg_extend, sep );
              writeCsvFlag( pw, duplicate, surface, ref_item.isCommented(), item.mComment, sep, newline );
              duplicate = false; // reset flags
              surface   = false;
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.US, "%s@%s%c-%c%.2f%c%.1f%c%.1f%c",
                      from, info.name, sep, sep, item.mLength * ul, sep, item.mBearing * ua, sep, item.mClino * ua, sep );
            extend = item.getIntExtend();
            if ( extend <= 1 ) pw.format("%d", extend );
            writeCsvFlag( pw, false, false, item.isCommented(), item.mComment, sep, newline );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", item.mComment );
            // }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsvLeg( pw, leg, ul, ua, leg_extend, sep );
              writeCsvFlag( pw, duplicate, surface, ref_item.isCommented(), ref_item.mComment, sep, newline );
              duplicate = false; // reset flags
              surface   = false;
              // n = 0;
            }
            if ( splays ) {
              splays = false;
            }
            ref_item = item;
            if ( item.isDuplicate() ) duplicate = true;
            if ( item.isSurface() )   surface = true;
            leg_extend = item.getIntExtend();
            pw.format("%s@%s%c%s@%s", from, info.name, sep, to, info.name );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeCsvLeg( pw, leg, ul, ua, leg_extend, sep );
        writeCsvFlag( pw, duplicate, surface, ref_item.isCommented(), ref_item.mComment, sep, newline );
        // duplicate = false; // reset flags
        // surface   = false;
      }
      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed CSV export: " + e.getMessage() );
      return 0;
    }
  }

  // -----------------------------------------------------------------------
  // TOPOLINUX EXPORT 
  // commented flag not supported 

  // public String exportSurveyAsTlx( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String surveyname ) // FIXME args
  // {
  //   File dir = TDFile.getFile( TopoDroidApp.APP_TLX_PATH );
  //   if (!dir.exists()) {
  //     dir.mkdirs();
  //   }
  //   String filename = TopoDroidApp.APP_TLX_PATH + info.name + ".tlx";
  //   List< DBlock > list = mData.selectAllExportShots( sid, TDStatus.NORMAL );
  //   checkShotsClino( list );
  //   try {
  //     TDPath.checkPath( filename );
  //     // BufferedWriter bw = TDFile.getMSwriter( "tlx", surveyname + ".tlx", "text/tlx" );
  //     PrintWriter pw = new PrintWriter( bw );
  //     pw.format("tlx2\n");
  //     pw.format("# %s created by TopoDroid v %s\n\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
  //     pw.format("# date %s \n", mData.getSurveyDate( sid ) );
  //     pw.format("# %s \n", mData.getSurveyComment( sid ) );
  //     int n = 0;
  //     float l=0.0f, b=0.0f, c=0.0f;
  //     float l0[] = new float[10];
  //     float b0[] = new float[10];
  //     float c0[] = new float[10];
  //     float r0[] = new float[10];
  //     DBlock ref_item = null;
  //     int extend = 0;
  //     int flag   = DBlock.FLAG_SURVEY;

  //     for ( DBlock item : list ) {
  //       String from = item.mFrom;
  //       String to   = item.mTo;
  //       if ( from != null && from.length() > 0 ) {
  //         if ( to != null && to.length() > 0 ) {
  //           if ( n > 0 /* && ref_item != null */ ) {
  //             b = TDUtil.in360( b/n );
  //             pw.format(Locale.US, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.US, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             extend = 0;
  //             flag   = DBlock.FLAG_SURVEY;
  //           }
  //           n = 1;
  //           ref_item = item;
  //           // item.Comment()
  //           pw.format("    \"%s\" \"%s\" ", from, to );
  //           l = item.mLength;
  //           b = item.mBearing;
  //           c = item.mClino;
  //           extend = item.getIntExtend();
  //           flag   = (int) item.getFlag();
  //           l0[0] = item.mLength;
  //           b0[0] = item.mBearing;
  //           c0[0] = item.mClino;
  //           r0[0] = item.mRoll;
  //         } else { // to.isEmpty()
  //           if ( n > 0 /* && ref_item != null */ ) {
  //             b = TDUtil.in360( b/n );
  //             pw.format(Locale.US, "%.2f %.1f %.1f 0.0 %d %d %d\n", l/n, b, c/n, extend, flag, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.US, "@ %.2f %.1f %.1fi %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             n = 0;
  //             ref_item = null;
  //             extend = 0;
  //             flag   = DBlock.FLAG_SURVEY;
  //           }
  //           // item.Comment()
  //           pw.format("    \"%s\" \"\" ", from );
  //           pw.format(Locale.US, "%.2f %.1f %.1f %.1f %d %d 1\n",
  //             item.mLength, item.mBearing, item.mClino, item.mRoll, item.getIntExtend(), item.getFlag() );
  //         }
  //       } else { // from.isEmpty()
  //         if ( to != null && to.length() > 0 ) {
  //           if ( n > 0 /* && ref_item != null */ ) {
  //             b = TDUtil.in360( b/n );
  //             pw.format(Locale.US, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
  //             while ( n > 0 ) {
  //               -- n;
  //               pw.format(Locale.US, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //             }
  //             n = 0;
  //             ref_item = null;
  //             extend = 0;
  //             flag   = DBlock.FLAG_SURVEY;
  //           }
  //           // item.Comment()
  //           pw.format("    \"\" \"%s\" ", to );
  //           pw.format(Locale.US, "%.2f %.1f %.1f %.1f %d %d 1\n",
  //             item.mLength, item.mBearing, item.mClino, item.mRoll, item.getIntExtend(), item.getFlag() );
  //         } else {
  //           // not exported
  //           if ( ref_item != null &&
  //              ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
  //             float bb = TDUtil.around( item.mBearing, b0[0] );
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
  //       b = TDUtil.in360( b/n );
  //       pw.format(Locale.US, "%.2f %.1f %.1f 0.0 %d 0 %d\n", l/n, b, c/n, extend, n );
  //       while ( n > 0 ) {
  //         -- n;
  //         pw.format(Locale.US, "@ %.2f %.1f %.1f %.1f\n", l0[n], b0[n], c0[n], r0[n] );
  //       }
  //       // extend = 0;
  //       // flag   = DBlock.FLAG_SURVEY;
  //     }
  //     // pw.format(Locale.US, "%.2f %.1f %.1f %.1f %d %d %d\n", 
  //     //   item.mLength, item.mBearing, item.mClino, item.mRoll, item.getIntExtend(), 0, 1 );
  //     // item.mComment
  //     bw.flush();
  //     bw.close();
  //     return filename;
  //   } catch ( IOException e ) {
  //     TDLog.e( "Failed QTopo export: " + e.getMessage() );
  //     return null;
  //   }
  // }

  // -----------------------------------------------------------------------
  // COMPASS EXPORT DAT
  //   commented flag not supported
  //   surface flag handled as duplicate

  // unused
  // static private LRUDprofile computeLRUDprofile( DBlock b, List< DBlock > list, boolean at_from )
  // {
  //   LRUDprofile lrud = new LRUDprofile( b.mBearing );
  //   float n0  = TDMath.cosd( b.mBearing );
  //   float e0  = TDMath.sind( b.mBearing );
  //   float cc0 = TDMath.cosd( b.mClino );
  //   float sc0 = TDMath.sind( b.mClino );
  //   float cb0 = n0;
  //   float sb0 = e0;
  //   String station = ( at_from ) ? b.mFrom : b.mTo;
  //   
  //   for ( DBlock item : list ) {
  //     String from = item.mFrom;
  //     String to   = item.mTo;
  //     if ( TDString.isNullOrEmpty( from ) ) { // skip blank
  //       // if ( TDString.isNullOrEmpty( to ) ) continue;
  //       continue;
  //     } else { // skip leg
  //       if ( to != null && to.length() > 0 ) continue;
  //     }
  //     if ( station.equals( from ) ) {
  //       float cb = TDMath.cosd( item.mBearing );
  //       float sb = TDMath.sind( item.mBearing );
  //       float cc = TDMath.cosd( item.mClino );
  //       float sc = TDMath.sind( item.mClino );
  //       float len = item.mLength;
  //
  //       // skip splays too close to shot direction // FIXME setting
  //       // this test aims to use only splays that are "orthogonal" to the shot
  //       if ( TDSetting.mOrthogonalLRUD ) {
  //         float cbb1 = sc*sc0*(sb*sb0 + cb*cb0) + cc*cc0; // cosine of angle between block and item
  //         if ( Math.abs( cbb1 ) > TDSetting.mOrthogonalLRUDCosine ) continue; 
  //       }
  // 
  //       float z1 = len * sc;
  //       float n1 = len * cc * cb;
  //       float e1 = len * cc * sb;
  //       if ( z1 > 0.0 ) { if ( z1 > lrud.u ) lrud.u = z1; }
  //       else            { if ( -z1 > lrud.d ) lrud.d = -z1; }
  // 
  //       float rl = e1 * n0 - n1 * e0;
  //       if ( rl > 0.0 ) { if ( rl > lrud.r ) lrud.r = rl; }
  //       else            { if ( -rl > lrud.l ) lrud.l = -rl; }
  //       lrud.addData( z1, rl, len );
  //     }
  //   }
  //   // TDLog.v( "<" + b.mFrom + "-" + b.mTo + "> at " + station + ": " + lrud.l + " " + lrud.r );
  //   return lrud;
  // }

  /** compute the LRUD for a leg
   * @param b       leg
   * @param list    splay data
   * @param at_from whether to compute LRUD at the FROM station
   * @return computed LRUD
   */
  static private LRUD computeLRUD( DBlock b, List< DBlock > list, boolean at_from )
  {
    LRUD lrud = new LRUD();
    float n0  = TDMath.cosd( b.mBearing );
    float e0  = TDMath.sind( b.mBearing );
    float cc0 = TDMath.cosd( b.mClino );
    float sc0 = TDMath.sind( b.mClino );
    float cb0 = n0;
    float sb0 = e0;
    String station = ( at_from ) ? b.mFrom : b.mTo;

    boolean do_cnt = TDSetting.mLRUDcount; // counter for the splay used to compute LRUD
    // if do_cnt is true LRUD are set only the first time (do_XXX true)
    //                   and only four in-tolerance splays are used
    if ( do_cnt ) {
      boolean do_left  = true;
      boolean do_right = true;
      boolean do_up    = true;
      boolean do_down  = true;
      int cnt = 4;
      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) { // skip blank
          // if ( TDString.isNullOrEmpty( to ) ) continue;
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
          // 
          // FIXME considering only the angle splay-leg may be not enough
          //       should split the case for LR and UD
          //       [1] LR must be almost horizontal: |clino| < threshold
          //       [2] UD must be vertical: |clino| > threshold
          if ( TDSetting.mOrthogonalLRUD ) {
            float cbb1 = sc*sc0*(sb*sb0 + cb*cb0) + cc*cc0; // cosine of angle between block and item
            if ( Math.abs( cbb1 ) > TDSetting.mOrthogonalLRUDCosine ) continue; 
          }

          float z1 = len * sc;
          float n1 = len * cc * cb;
          float e1 = len * cc * sb;
          if ( Math.abs( item.mClino ) >= TDSetting.mLRUDvertical ) {
            if ( z1 > 0.0 ) { if ( do_up   &&  z1 > lrud.u ) { lrud.u =  z1; do_up   = false; } }
            else            { if ( do_down && -z1 > lrud.d ) { lrud.d = -z1; do_down = false; } }
            --cnt;
          } else if ( Math.abs( item.mClino ) <= TDSetting.mLRUDhorizontal ) {
            float rl = e1 * n0 - n1 * e0;
            if ( rl > 0.0 ) { if ( do_right &&  rl > lrud.r ) { lrud.r =  rl; do_right = false; } }
            else            { if ( do_left  && -rl > lrud.l ) { lrud.l = -rl; do_left  = false; } }
            --cnt;
          }
          if ( cnt == 0 ) break;
        }
      }
    } else {
      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) { // skip blank
          // if ( TDString.isNullOrEmpty( to ) ) continue;
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
          // 
          // FIXME considering only the angle splay-leg may be not enough
          //       should split the case for LR and UD
          //       [1] LR must be almost horizontal: |clino| < threshold
          //       [2] UD must be vertical: |clino| > threshold
          if ( TDSetting.mOrthogonalLRUD ) {
            float cbb1 = sc*sc0*(sb*sb0 + cb*cb0) + cc*cc0; // cosine of angle between block and item
            if ( Math.abs( cbb1 ) > TDSetting.mOrthogonalLRUDCosine ) continue; 
          }
          float z1 = len * sc;
          float n1 = len * cc * cb;
          float e1 = len * cc * sb;
          if ( Math.abs( item.mClino ) >= TDSetting.mLRUDvertical ) {
            if ( z1 > 0.0 ) { if (  z1 > lrud.u ) { lrud.u =  z1; } }
            else            { if ( -z1 > lrud.d ) { lrud.d = -z1; } }
          } 
          if ( Math.abs( item.mClino ) <= TDSetting.mLRUDhorizontal ) {
            float rl = e1 * n0 - n1 * e0;
            if ( rl > 0.0 ) { if (  rl > lrud.r ) { lrud.r =  rl; } }
            else            { if ( -rl > lrud.l ) { lrud.l = -rl; } }
          }
        }
      }
    }
    // TDLog.v( "<" + b.mFrom + "-" + b.mTo + "> at " + station + ": " + lrud.l + " " + lrud.r );
    return lrud;
  }

  /** Centerline data are exported in Compass format as follows
   *    SURVEY NAME: survey_name
   *    SURVEY DATE: mm dd yyyy COMMENT: description
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
   * The flags string is composed as "#|...#", Flags characters: L (duplicate) P (no plot) X (surface), S (splay).
   * Splay shots are not exported, they may be used to find transversal dimensions, if LRUD are not provided  
   * Multisurvey file is possible.
   */

  private static void printShotToDat( PrintWriter pw, AverageLeg leg, LRUD lrud, boolean duplicate, String comment )
  {
    if ( TDSetting.mSwapLR ) {
      pw.format(Locale.US, "%.2f %.1f %.1f %.2f %.2f %.2f %.2f", 
        leg.length()*TDUtil.M2FT, leg.bearing(), leg.clino(),
        lrud.r*TDUtil.M2FT, lrud.u*TDUtil.M2FT, lrud.d*TDUtil.M2FT, lrud.l*TDUtil.M2FT );
    } else {
      pw.format(Locale.US, "%.2f %.1f %.1f %.2f %.2f %.2f %.2f", 
        leg.length()*TDUtil.M2FT, leg.bearing(), leg.clino(),
        lrud.l*TDUtil.M2FT, lrud.u*TDUtil.M2FT, lrud.d*TDUtil.M2FT, lrud.r*TDUtil.M2FT );
    }
    leg.reset();
    if ( duplicate ) {
      pw.format(" #|L#");
    }
    if ( comment != null && comment.length() > 0 ) {
      pw.format(" %s", comment );
    }
    pw.format( "\r\n" );
  }

  private static void printSplayToDat( PrintWriter pw, String prefix, String from, String to, DBlock blk, boolean reverse )
  {
    if ( ! TDSetting.mCompassSplays ) return;
    float b = blk.mBearing;
    float c = blk.mClino;
    if ( reverse ) {
      b += 180;
      if ( b >=360 ) b -= 360;
      c = -c;
    }
    writeDatFromTo( pw, prefix, from, to );
    pw.format(Locale.US, "%.2f %.1f %.1f -9.90 -9.90 -9.90 -9.90 #|S#", blk.mLength*TDUtil.M2FT, b, c );

    // if ( duplicate ) {
    //   pw.format(" #|L#");
    // }
    if ( blk.mComment != null && blk.mComment.length() > 0 ) {
      pw.format(" %s", blk.mComment );
    }
    pw.format( "\r\n" );
  }

  static private void writeDatFromTo( PrintWriter pw, String prefix, String from, String to )
  {
    if ( prefix != null && prefix.length() > 0 ) {
      pw.format("%s-%s %s-%s ", prefix, from, prefix, to );
    } else {
      pw.format("%s %s ", from, to );
    }
  }

  static public int nextSplayInt( HashMap<String,Integer> splay_station, String name )
  {
    int ret = 0;
    if ( splay_station.containsKey( name ) ) {
      ret = splay_station.get( name ).intValue(); // FIXME may null pointer
    }
    splay_station.put( name, Integer.valueOf(ret+1) );
    return ret;
  }

  /** export survey data in TROX (VisualTopo) format
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsDat( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name, String prefix )
  {
    // TDLog.v( "export as compass: " + survey_name + " swap LR " + TDSetting.mSwapLR );
    boolean not_diving = ! info.isDivingMode();

    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export Compass " + survey_name + ".dat");
      // BufferedWriter bw = TDFile.getMSwriter( "dat", survey_name + ".dat", "text/dat" );
      PrintWriter pw = new PrintWriter( bw );
  
      // FIXME 
      // pw.format("# %s created by TopoDroid v %s\n\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );

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
          TDLog.e( "export survey as DAT date parse error " + date );
        }
      }
      pw.format("SURVEY DATE: %02d %02d %04d", m, d, y ); // format "MM DD YYYY"
      if ( info.comment != null ) {
        pw.format(" COMMENT: %s", info.comment );
      }
      pw.format("\r\n");

      pw.format("SURVEY TEAM:\r\n");
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("%s\r\n", info.team );
      } else {
        pw.format("...\r\n");
      }
      pw.format(Locale.US, "DECLINATION: %.4f  ", info.getDeclination() ); // DECLINATION Compass does not have undefined declination
      pw.format("FORMAT: DMMDLUDRLADN  CORRECTIONS:  0.00 0.00 0.00\r\n" );
      pw.format("\r\n" );
      if ( info.isDivingMode() ) {
        pw.format("FROM TO DEPTH BEARING LENGTH FLAGS COMMENTS\r\n" );
      } else {
        pw.format("FROM TO LENGTH BEARING INC LEFT UP DOWN RIGHT FLAGS COMMENTS\r\n" );
      }
      pw.format( "\r\n" );

      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay = false;
      boolean duplicate = false;
      LRUD lrud;

      HashMap<String, Integer > splay_stations = TDSetting.mCompassSplays ?  new HashMap<String, Integer >() : null;

      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeDatFromTo( pw, prefix, ref_item.mFrom, ref_item.mTo );
              printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
	    if ( TDSetting.mCompassSplays && not_diving ) {
	      int ii = nextSplayInt( splay_stations, to );
	      printSplayToDat( pw, prefix, to, to + "ss" + ii, item, true ); // reverse
	    }
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write previous leg shot
              lrud = computeLRUD( ref_item, list, true );
              writeDatFromTo( pw, prefix, ref_item.mFrom, ref_item.mTo );
              printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
	    if ( TDSetting.mCompassSplays && not_diving ) {
	      int ii = nextSplayInt( splay_stations, from );
	      printSplayToDat( pw, prefix, from, from + "ss" + ii, item, false ); // not reverse
	    }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeDatFromTo( pw, prefix, ref_item.mFrom, ref_item.mTo );
              printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
            }
            ref_item = item;
            duplicate = item.isDuplicate() || item.isSurface();
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        lrud = computeLRUD( ref_item, list, true );
        writeDatFromTo( pw, prefix, ref_item.mFrom, ref_item.mTo );
        printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
      }
      pw.format( "\f\r\n" );

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed Compass export: " + e.getMessage() );
      return 0;
    }
  }

  // ----------------------------------------------------------------------------------------
  // TOPOROBOT
  // commented flag not supported
  // duplicate and surface flags not handled

  private static final int TRB_LINE_LENGTH = 82; 

  private static int getTrbSeries( String s )
  {
    int pos = s.indexOf('.');
    int ret = 0;
    if ( pos > 0 ) {
      try {
        ret = Integer.parseInt( s.substring(0, pos) );
      } catch ( NumberFormatException e ) { }
    }
    return ret;
  }

  private static int getTrbStation( String s )
  {
    int pos = s.indexOf('.');
    int ret = 0;
    try {
      ret = (pos >= 0)? Integer.parseInt( s.substring(pos+1) )
                      : Integer.parseInt( s );
    } catch ( NumberFormatException e ) { }
    return ret;
  }

  static TrbStruct makeTrbStations( List<DBlock> list )
  {
    TrbStruct trb = new TrbStruct();
    ArrayList< DBlock > shots = new ArrayList< DBlock >(); // local work array

    for ( DBlock item : list ) {
      String from = item.mFrom;
      String to   = item.mTo;
      if ( from != null && from.length() > 0 && to != null && to.length() > 0 ) { 
        trb.put( from, null );
        trb.put( to, null );
        // trb.addShot( item );
        shots.add( item );
      }
    }
    int nr = shots.size();
    if ( trb.areStationsAllTopoRobot() ) {
      trb.copyStations();
      for ( DBlock shot : shots ) {
        String from = shot.mFrom;
        String to   = shot.mTo;
        int sr = -1, pt = -1;
        if ( to != null && to.length() > 0 && from != null && from.length() > 0 ) {
          int pos = to.indexOf('.');
          try {
            sr = Integer.parseInt( to.substring(0, pos) );
            pt = Integer.parseInt( to.substring( pos + 1 ) );
          } catch ( NumberFormatException e ) {
            TDLog.e("TROBOT error station " + to );
          }
        }
        if ( sr > 0 && pt >= 0 ) {
          TrbSeries srs = trb.getSeries( sr );
          if ( srs != null ) {
            srs.appendShot( shot, true );
            srs.increaseNrPoints();
            srs.setEndPoint( sr, pt );
            TDLog.v("TROBOT series " + sr + " append " + shot.mId + " endpoint " + pt );
          } else {
            TDLog.v("TROBOT no-series station " + to );
          }
        }
      }
    } else {
      DBlock item = shots.get( 0 );
      String st = item.mFrom; // current station
      trb.put( st, "1.0" );
      TDLog.v("TRB shots: " + nr + " start " + item.mFrom + "-" + item.mTo + " put " + st + " as 1.0" );
      
      boolean repeat1 = true;
      int series = 1;
      int start_series = 1;
      int start_point  = 0;
      while ( repeat1 ) {
        repeat1 = false;
        TDLog.v("TRB make series " + series + " start " + start_series + "." + start_point );
        TrbSeries trb_series = new TrbSeries( series, start_series, start_point );
        trb.addSeries( trb_series );
        int point  = 0;
        boolean repeat2 = true;
        while ( repeat2 ) {
          repeat2 = false;
          
          for ( int k = 0; k < shots.size(); ) {
            item = shots.get( k );
            String from = item.mFrom;
            String to   = item.mTo;
            TDLog.v("TRB shot " + k + "/" + shots.size() + ": " + from + "-" + to );
            if ( from.equals( st ) && trb.get( to ) == null ) {
              point ++;
              TDLog.v("TRB forward shot " + from + "-" + to + " put " + to + " as " + series + "." + point );
              trb.put( to, String.format("%d.%d", series, point ) );
              trb_series.appendShot( item, true );
              st = to;
              shots.remove( k );
              repeat2 = true;
              break;
            } else if ( to.equals( st ) && trb.get( from ) == null ) {
              point ++;
              TDLog.v("TRB backward shot " + from + "-" + to + " put " + from + " as " + series + "." + point );
              trb.put( from, String.format("%d.%d", series, point ) );
              trb_series.appendShot( item, false );
              st = from;
              shots.remove( k );
              repeat2 = true;
              break;
            } else {
              ++k;
            }
          }
        }
        trb_series.setPoints( point );

        TDLog.v("TRB check new series. shots " + shots.size() );
        boolean found = false;
        for ( int k = 0; k < shots.size(); ++k ) { // check for a new series
          item = shots.get( k );
          String from = item.mFrom;
          String to   = item.mTo;
          String spf  = trb.get( from );
          String spt  = trb.get( to );
          if ( shots.size() < 7 ) TDLog.v("TRB try " + from + " " + to + " ==> " + spf + " " + spt );
          if ( spf != null && spt == null ) { // new series
            ++ series;
            repeat1 = true;
            start_series = getTrbSeries( spf );
            start_point  = getTrbStation( spf );
            st = from;
            found = true;
            TDLog.v("TRB new series F " + series + " from " + st + " = " + spf );
            break;
          } else if ( spt != null && spf == null ) { // new series
            ++ series;
            repeat1 = true;
            start_series = getTrbSeries( spt );
            start_point  = getTrbStation( spt );
            st = to;
            found = true;
            TDLog.v("TRB new series T " + series + " from " + st + " = " + spt );
            break;
          }
        }
        if ( ! found ) { 
          TDLog.e("TRB non-connected shots");
          break;
        }
      }
    }
    // TDLog.v("TRB done make series: " + trb.getNrSeries() + " stations " + trb.getNrStations() );
    return trb;
  }

      
  private static void writeTrbSeries1( PrintWriter pw, List< DBlock > list, TrbStruct trb, String comment ) 
  {
    TDLog.v("TRB write trb: nr. series " + trb.getSeries().size() );
    boolean first = true;
    for ( TrbSeries sr : trb.getSeries() ) {
      // TDLog.v("TRB series " + sr.series + " start " + sr.start_series + "." + sr.start_point + " pts " + sr.points );
      // sr.dumpBlocks(); // DEBUG
      // N.B. all topodroid series are open-end
      if ( first ) {
        if ( TDSetting.TRobotJB ) {
          // pw.format("%6d\t-2\t%s\r", sr.series, comment );
        } else {
          pw.format("%6d\t-2\t1\t1\t1\t%s\r\n", sr.series, comment );
        }
        first = false;
      }
      int fs = sr.start_series;
      int fp = sr.start_point;
      int ts = sr.series; // this is fix
      int end_series = sr.end_series;
      int end_point  = sr.end_point;
      if (sr.end_series < 1) {
        end_series = sr.series;
        end_point  = sr.points;
      }
      if ( TDSetting.TRobotJB ) {
        pw.format("%6d\t-1\t%d\t%d\t%d\t%d\t%d\t0\t0\r", sr.series, sr.start_series, sr.start_point, end_series, end_point, sr.points );
      } else {
        pw.format("%6d\t-1\t1\t1\t1\t%d\t%d\t%d\t%d\t%d\t0\t0\r\n", sr.series, sr.start_series, sr.start_point, end_series, end_point, sr.points );
      }
      boolean atFrom = true;
      
      TrbShot shot = sr.getShots(); // get the first shot of the series
      if ( shot != null ) {
        LRUD lrud = computeLRUD( shot.block, list, shot.forward ); // forwrad: at FROM
        if ( TDSetting.TRobotJB ) {
          pw.format( Locale.US, "%6d\t0\t1\t1\t%.2f\t%.1f\t%.1f\t%.2f\t%.2f\t%.2f\t%.2f\r", ts, 0.0f, 0.0f, 0.0f, lrud.l, lrud.r, lrud.u, lrud.d );
        } else {
          pw.format( Locale.US, "%6d\t0\t1\t1\t1\t%.2f\t%.1f\t%.1f\t%.2f\t%.2f\t%.2f\t%.2f\r\n", ts, 0.0f, 0.0f, 0.0f, lrud.l, lrud.r, lrud.u, lrud.d );
        }
        int tp = 1;
        for ( ; shot != null; shot = shot.next ) {
          DBlock item = shot.block;
          String spf = trb.get( shot.forward ? item.mFrom : item.mTo   );
          String spt = trb.get( shot.forward ? item.mTo   : item.mFrom );
          int sf = getTrbSeries(  spf );
          int pf = getTrbStation( spf );
          int st = getTrbSeries(  spt );
          int pt = getTrbStation( spt );
          // TDLog.v("TRB " + fs + "." + fp + " -- " + st + "." + tp + " item " + item.mFrom + "-" + item.mTo + " < " + spf + " " + sf + "." + pf + " -- " + spt + " " + st + "." + pt + " > " + shot.forward );
          AverageLeg leg = computeAverageLeg( item, list );
          assert( leg != null );
          lrud = computeLRUD( item, list, (! shot.forward) ); // not forward ==> at TO -- forward: at FROM
          // write block ... TODO
          // series point topo code L A C L R U D comment
          float b = leg.bearing();
          float c = leg.clino();
          if ( ! shot.forward ) {
            c = -c;
            if ( b >= 180 ) { b -= 180; } else { b += 180; }
          }
          if ( TDSetting.TRobotJB ) {
            pw.format( Locale.US, "%6d\t%d\t1\t1\t%.2f\t%.1f\t%.1f\t%.2f\t%.2f\t%.2f\t%.2f", ts, pt, leg.length(), b, c, lrud.l, lrud.r, lrud.u, lrud.d );
          } else {
            pw.format( Locale.US, "%6d\t%d\t1\t1\t1\t%.2f\t%.1f\t%.1f\t%.2f\t%.2f\t%.2f\t%.2f", ts, pt, leg.length(), b, c, lrud.l, lrud.r, lrud.u, lrud.d );
          }
          if ( item.mComment != null ) {
            pw.format( "\t%s", item.mComment );
          }
          if ( TDSetting.TRobotJB ) {
            pw.format( "\r" );
          } else {
            pw.format( "\r\n" );
          }
          fs = ts;
          fp = tp;
          ++ tp;
        } // end of series
      }
    }
  }

  private static AverageLeg computeAverageLeg( DBlock blk, List< DBlock > list )
  {
    // TDLog.v("TRB average leg: block " +  blk.mFrom + "-" + blk.mTo + " list size " + list.size() );
    if ( TDString.isNullOrEmpty( blk.mFrom ) || TDString.isNullOrEmpty( blk.mTo ) ) return null;
    for ( int k = 0; k < list.size(); ++k ) {
      DBlock item = list.get( k );
      // if ( blk == list.get( k ) )
      if ( blk.mFrom.equals( item.mFrom ) && blk.mTo.equals( item.mTo ) ) {
        int k0 = k; // DEBUG
        AverageLeg leg = new AverageLeg(0);
        // TDLog.v("TRB " + k + " add block " + item.mId + " to leg " );
        leg.add( blk.mLength, blk.mBearing, blk.mClino );
        ++k;
        while ( k < list.size() ) {
          item = list.get( k );
          if ( /* ( TDString.isNullOrEmpty( item.mFrom ) || TDString.isNullOrEmpty( item.mTo ) ) && item.isSecLeg() && */ item.isRelativeDistance( blk ) ) {
            // TDLog.v("TRB " + k + " add block " + item.mId + " to leg " );
            leg.add( item.mLength, item.mBearing, item.mClino );
            ++ k;
          } else {
            break;
          }
        }
        // TDLog.v("TRB average leg: items " + k0 + " ... " + k );
        return leg;
      } 
    }
    // TDLog.v("TRB average leg: null " );
    return null;
  }

  static int exportSurveyAsTrb( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name, long first )
  {
    int trip = 1;
    int code = 1;
    List< DBlock > list = data.selectExportShots( sid, TDStatus.NORMAL, first );
    checkShotsClino( list );
    // TDLog.v( "TRB export: shots " + list.size() );
    char[] line = new char[ TRB_LINE_LENGTH ];
    try {
      // TDLog.Log( TDLog.LOG_IO, "export TopoRobot " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( TDPath.TRB.substring(1), survey_name + TDPath.TRB, "text/" + TDPath.TRB.substring(1) );
      PrintWriter pw = new PrintWriter( bw );
  
      // FIXME 
      // pw.format("# TopoDroid v %s\r\n", TDVersion.string() );
      // pw.format("# %s\r\n", TDUtil.getDateString("MM dd yyyy") );

      //           5 11 15 19 23
      if ( TDSetting.TRobotJB ) {
        pw.format( "    -6\t1\r" );
      } else {
        pw.format( "    -6\t1\t1\t1\t1\r\n" );
      }

      List< FixedInfo > fixeds = data.selectAllFixed( sid, TDStatus.NORMAL );
      if ( fixeds.size() > 0 ) {
        for ( FixedInfo fixed : fixeds ) {
          // TDLog.v( "TROBOT export - fixed " + fixed.name + " " + fixed.lng + " " + fixed.lat + " " + fixed.h_geo );
          // get TR-station from fixed name
          int pos = fixed.name.indexOf('.');
          int st = (pos < 0)? Integer.parseInt( fixed.name ) : Integer.parseInt( fixed.name.substring( pos+1 ) );
          if ( TDSetting.TRobotJB ) {
            pw.format(Locale.US, "    -5\t1\t%.7f\t%.7f\t%.2f\t1\t0\t%d\r", fixed.lng, fixed.lat, fixed.h_geo, st ); // series=1 point=0 (station)
          } else {
            pw.format(Locale.US, "    -5\t1\t1\t1\t1\t%.7f\t%.7f\t%.2f\t1\t0\t%d\r\n", fixed.lng, fixed.lat, fixed.h_geo, st ); // series=1 point=0 (station)
          }
        }
      } else {
        if ( TDSetting.TRobotJB ) {
          pw.format(Locale.US, "    -5\t1\t0.00\t0.00\t0.00\t1\t0\tnone\r" );
        } else {
          pw.format(Locale.US, "    -5\t1\t1\t1\t1\t0.00\t0.00\t0.00\t1\t0\tnone\r\n" );
        }
      }
      if ( TDSetting.TRobotJB ) {
        // pw.format(Locale.US, "(   -5\t1\t%s\r", survey_name );
      } else {
        pw.format(Locale.US, "(   -5\t1\t1\t1\t1\t%s\r\n", survey_name );
      }

      String date = info.date;
      int y = 0;
      int m = 0;
      int d = 0;
      if ( date != null && date.length() == 10 ) {    // [-2] trip
        try {
          y = Integer.parseInt( date.substring(0,4) ); y = y%100;
          m = Integer.parseInt( date.substring(5,7) );
          d = Integer.parseInt( date.substring(8,10) );
        } catch ( NumberFormatException e ) { }
      }

      if ( TDSetting.TRobotJB ) {
        /* nothing */
      } else {
        pw.format(Locale.US, "    -4\t1\t1\t1\t1\t$s TopoDroid v %s - %s\r\n",   TDUtil.currentDateTimeTRobot(), TDVersion.string() );
        pw.format("    -3\t1\t1\t1\t1\r\n"); // not used - legacy 
      }
      String team = (info.team != null)? info.team : "-";
      if ( team.length() > 26 ) team = team.substring(0,26);
      // DECLINATION TopoRobot: 0 = provided, 1 = to be calculated ???
      // 0 if declination not known, negative of declination (if known)
      int use_decl = 0;
      float decl = 0;
      if (info.hasDeclination() ) {
        decl =  -info.getDeclination();
        use_decl = 1;
      }
      String comment = info.comment;
      if ( comment == null ) comment = "-";

      // TRIP
      //           5 11 15 19 23   31   39   47   55   63   71   79
      if ( TDSetting.TRobotJB ) {
        pw.format(Locale.US, "    -2\t1\t%02d\t%02d\t%02d\t%s\t...\t%d\t%.2f\t0\t1\r", d, m, y, team, use_decl, decl ); // 0: inclination, 1: color
      } else {
        pw.format(Locale.US, "    -2\t1\t1\t1\t1\t%02d/%02d/%02d\t%s\t...\t%d\t%.2f\t0\t1\r\n", d, m, y, team, use_decl, decl ); // 0: inclination, 1: color
      }

      // CODE
      // azimuth degrees (360), clino degrees, precisions (length, azimuth, clino), tape, winkel
      if ( TDSetting.TRobotJB ) {
        pw.format(Locale.US, "    -1\t1\t360\t360\t0.10\t1\t1\t100\t0\r" ); 
      } else {
        pw.format(Locale.US, "    -1\t1\t1\t1\t1\t360\t360\t0.10\t1\t1\t100\t0\r\n" ); 
      }
      
      TrbStruct trb = makeTrbStations( list );
      // at this point mTrbSeries is populated.

      writeTrbSeries1( pw, list, trb, comment );

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed TopoRobot export: " + e.getMessage() );
      return 0;
    }
  }
   

  // private static void writeTrbSeries0( PrintWriter pw, List< DBlock > list, int trip ) 
  // {
  //   int max_series = 0;
  //   for ( DBlock item : list ) {
  //     String from = item.mFrom;
  //     String to   = item.mTo;
  //     if ( from != null && from.length() > 0 && to != null && to.length() > 0 ) { 
  //       int srf = getTrbSeries( from );
  //       int srt = getTrbSeries( to );
  //       // TDLog.v("TRB series F " + srf + " T " + srt );
  //       if ( srt > max_series ) max_series = srt;
  //       if ( srf > max_series ) max_series = srf;
  //     }
  //   }
  //   max_series ++;
  // 
  //   int[] nr_st    = new int[ max_series ]; // number of stations
  //   int[] start_sr = new int[ max_series ]; // start series
  //   int[] start_st = new int[ max_series ]; // start station
  //   int[] end_st   = new int[ max_series ]; // end station
  //   for ( int k=0; k<max_series; ++k ) {
  //     nr_st[k] = 0;
  //     start_sr[k] = 0;
  //     start_st[k] = 0;
  //     end_st[k] = 0;
  //   }
  //  
  //   int first_sr = -1;
  //   int first_st = -1;
  //   int last_sr = -1;
  //   int last_st = -1;
  //   int nr_pts = 0;
  //   for ( DBlock item : list ) {
  //     String from = item.mFrom;
  //     String to   = item.mTo;
  //     if ( from != null && from.length() > 0 && to != null && to.length() > 0 ) { 
  //       int srf = getTrbSeries( from );
  //       int stf = getTrbStation( from );
  //       int srt = getTrbSeries( to );
  //       int stt = getTrbStation( to );
  //       if ( first_sr < 0 || srf != srt ) {
  //         if ( first_sr < 0 ) { first_sr = srf; first_st = stf; }
  //         start_sr[srt] = srf;
  //         start_st[srt] = stf;
  //         nr_st[srt] = 0;
  //       }
  //       nr_st[srt] ++;
  //       end_st[srt] = stt;
  //       last_sr = srt;
  //       last_st = stt;
  //       ++nr_pts;
  //     }
  //   }

  //   pw.format(Locale.US, "%6d%6d%4d%4d%4d%8d%8d%8d%8d%8d%8d%8d\r\n",
  //     1, -1, 1, 1, 1, first_sr, first_st, last_sr, last_st, nr_pts, 1, 0 );

  //   AverageLeg leg = new AverageLeg(0);
  //   DBlock ref_item = null;
  //   int series = first_sr;
  //   // boolean in_splay = false;
  //   // boolean duplicate = false;
  //   LRUD lrud;

  //   for ( DBlock item : list ) {
  //     String from = item.mFrom;
  //     String to   = item.mTo;
  //     if ( TDString.isNullOrEmpty( from ) ) {
  //       if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
  //         if ( ref_item != null && 
  //            ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
  //           leg.add( item.mLength, item.mBearing, item.mClino );
  //         }
  //       } else { // only TO station: should never happen
  //         if ( leg.mCnt > 0 && ref_item != null ) {
  //           // FIXME ???
  //           // duplicate = false;
  //           ref_item = null; 
  //         }
  //       }
  //     } else { // with FROM station
  //       // int s = getTrbSeries( item.mFrom );
  //       // if ( s != series ) {
  //       //   series = s;
  //       //   pw.format("%6d%6d%4d%4d%4d%8d%8d%8d%8d%8d%8d%8d\r\n",
  //       //     s, -1, 1, 1, 1, start_sr[s], start_st[s], s, end_st[s], nr_st[s], 0, 0 );
  //       // }
  //       if ( TDString.isNullOrEmpty( to ) ) { // splay shot
  //         if ( leg.mCnt > 0 && ref_item != null ) { // write previous leg shot
  //           int srt = getTrbSeries( ref_item.mTo );
  //           int stt = getTrbStation( ref_item.mTo );
  //           lrud = computeLRUD( ref_item, list, true );
  //           if ( srt != series ) {
  //             series = srt;
  //             pw.format(Locale.US, "%6d%6d%4d%4d%4d%8d%8d%8d%8d%8d%8d%8d\r\n",
  //               srt, -1, 1, 1, 1, start_sr[srt], start_st[srt], srt, end_st[srt], nr_st[srt], 0, 0 );
  //             // if ( series == first_sr ) 
  //             if ( stt != 0 ) {
  //               pw.format(Locale.US, "%6d%6d%4d%4d%4d%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f\r\n",
  //                 srt, 0, 1, 1, 1, 0.0, 0.0, 0.0, lrud.l, lrud.r, lrud.u, lrud.d );
  //             }
  //           }
  //           //           5 11 15 19 23   31   39   47   55   63   71   79
  //           pw.format(Locale.US, "%6d%6d%4d%4d%4d%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f\r\n",
  //             srt, stt, 1, 1, trip, leg.length(), leg.bearing(), leg.clino(), 
  //             lrud.l, lrud.r, lrud.u, lrud.d );
  //           leg.reset();
  //           // duplicate = false;
  //           ref_item = null; 
  //         }
  //       } else {
  //         // if ( leg.mCnt > 0 && ref_item != null ) {
  //         //   int srt = getTrbSeries( ref_item.mTo );
  //         //   if ( srt != series ) {
  //         //     series = srt;
  //         //     pw.format(Locale.US, "%6d%6d%4d%4d%4d%8d%8d%8d%8d%8d%8d%8d\r\n",
  //         //       srt, -1, 1, 1, 1, start_sr[srt], start_st[srt], srt, end_st[srt], nr_st[srt], 0, 0 );
  //         //   }
  //         // }
  //         ref_item = item;
  //         // duplicate = item.isDuplicate() || item.isSurface();
  //         leg.set( item.mLength, item.mBearing, item.mClino );
  //       }
  //     }
  //   }
  //   if ( leg.mCnt > 0 && ref_item != null ) {
  //     int srt = getTrbSeries( ref_item.mTo );
  //     int stt = getTrbStation( ref_item.mTo );
  //     lrud = computeLRUD( ref_item, list, true );
  //     pw.format(Locale.US, "%6d%6d%4d%4d%4d%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f\r\n",
  //             srt, stt, 1, 1, trip, leg.length(), leg.bearing(), leg.clino(), 
  //             lrud.l, lrud.r, lrud.u, lrud.d );
  //     leg.reset();
  //     // duplicate = false;
  //     ref_item = null; 
  //   }
  //   pw.format( "\r\n" );

  // }

  // ----------------------------------------------------------------------------------------
  // WINKARST
  // commented flag not supported
  //   surface flag treated as duplicate

  private static void printShotToSur( PrintWriter pw, AverageLeg leg, LRUD lrud, String comment )
  {
    if ( TDSetting.mSwapLR ) {
      pw.format(Locale.US, "%.2f %.1f %.1f %.2f %.2f %.2f %.2f", 
        leg.length(), leg.bearing(), leg.clino(), lrud.r, lrud.l, lrud.u, lrud.d );
    } else {
      pw.format(Locale.US, "%.2f %.1f %.1f %.2f %.2f %.2f %.2f", 
        leg.length(), leg.bearing(), leg.clino(), lrud.l, lrud.r, lrud.u, lrud.d );
    }
    leg.reset();
    if ( comment != null && comment.length() > 0 ) {
      pw.format(" %s", comment );
    }
    pw.format( "\r\n" );
  }

  static private void writeSurFromTo( PrintWriter pw, String prefix, String from, String to, boolean duplicate )
  {
    if ( duplicate ) {
      pw.format("X ");
    } else {
      pw.format("N ");
    }
    if ( prefix != null && prefix.length() > 0 ) {
      pw.format("%s-%s %s-%s ", prefix, from, prefix, to );
    } else {
      pw.format("%s %s ", from, to );
    }
  }

  static int exportSurveyAsSur( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name, String prefix )
  {
    // TDLog.v( "export as winkarst: " + file.getName() + " swap LR " + TDSetting.mSwapLR );
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export WinKarst " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "sur", survey_name + ".sur", "text/sur" );
      PrintWriter pw = new PrintWriter( bw );
  
      // FIXME 
      pw.format("#FILE AUTHOR: TopoDroid v %s\r\n", TDVersion.string() );
      pw.format("#FILE DATE: %s\r\n", TDUtil.getDateString("MM dd yyyy") );
      pw.format("\r\n"); 
      pw.format("#SURVEY NAME: %s\r\n", info.name );
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
          TDLog.e( "export survey as DAT date parse error " + date );
        }
      }
      pw.format("#SURVEY DATE: %02d %02d %04d\r\n", m, d, y ); // format "MM DD YYYY"
      if ( info.comment != null ) {
        pw.format("#COMMENT: %s\r\n", info.comment );
      }

      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("#SURVEY TEAM: %s\r\n", info.team );
      }

      List< FixedInfo > fixeds = data.selectAllFixed( sid, TDStatus.NORMAL );
      if ( fixeds.size() > 0 ) {
        pw.format("#DATUM: WGS 84\r\n");
        for ( FixedInfo fixed : fixeds ) {
          pw.format(Locale.US, "#CONTROL POINT: %s %.10f E %.10f N %.1f M\r\n",
            fixed.name, fixed.lng, fixed.lat, fixed.h_geo );
        }
      }
      pw.format("\r\n" );
      if ( info.hasDeclination() ) {
        pw.format(Locale.US, "#DECLINATION: %.4f\r\n", info.getDeclination() ); // DECLINATION WinKarst
      // } else {
      //   pw.format(Locale.US, "#DECLINATION: AUTO\r\n" ); // WinKarst calculates the magnetic declination
      }
      pw.format("\r\n" );
      pw.format("#SHOT STATION STATION LENGTH AZIMUTH VERTICAL LEFT RIGHT UP DOWN COMMENT\r\n" );
      pw.format("#CODE FROM TO M DEG DEG M M M M\r\n" );

      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay = false;
      boolean duplicate = false;
      LRUD lrud;

      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeSurFromTo( pw, prefix, ref_item.mFrom, ref_item.mTo, duplicate );
              printShotToSur( pw, leg, lrud, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write previous leg shot
              lrud = computeLRUD( ref_item, list, true );
              writeSurFromTo( pw, prefix, ref_item.mFrom, ref_item.mTo, duplicate );
              printShotToSur( pw, leg, lrud, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeSurFromTo( pw, prefix, ref_item.mFrom, ref_item.mTo, duplicate );
              printShotToSur( pw, leg, lrud, ref_item.mComment );
            }
            ref_item = item;
            duplicate = item.isDuplicate() || item.isSurface();
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        lrud = computeLRUD( ref_item, list, true );
        writeSurFromTo( pw, prefix, ref_item.mFrom, ref_item.mTo, duplicate );
        printShotToSur( pw, leg, lrud, ref_item.mComment );
      }
      pw.format( "#END\r\n" );

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed WinKarst export: " + e.getMessage() );
      return 0;
    }
  }

  // =======================================================================
  // GHTOPO EXPORT
  // commented flag supported for splays
  //   surface flag handled
  //   duplicate flag not handled

  // one of 6 14 31 83 115 164 211
  static private int randomColor()
  {
    return 5;
  }

  static int exportSurveyAsGtx( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name )
  {
    String date = info.date.replace( '.', '-' ); // MySQL date format YYYY-MM-DD
    try {
      // TDLog.Log( TDLog.LOG_IO, "export GHTopo " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "gtx", survey_name + ".gtx", "text/gtx" );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      pw.format("<!-- %s created by TopoDroid v %s -->\r\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
      pw.format("<GHTopo>\n");

      // mandatory - cave name
      pw.format("<General>\n");
      pw.format("<Cavite"); 
      pw.format(" FolderName=\"%s\"", info.name );
      pw.format(" CoordsSystem=\"\"");          // Coord system (optional)  
      pw.format(" CoordsSystemEPSG=\"4978\"");  // EPSG code
      pw.format(" FolderObservations=\"\"");
      pw.format("/>\n");
      pw.format("</General>\n");

      // optional - TopoDroid generates only one default namespace
      pw.format("<Namespaces>\n");
      pw.format("<Namespace");
      pw.format(" Name=\"\"");
      pw.format(" ColorB=\"0\"");       // Color R (int in [0..255])
      pw.format(" ColorG=\"0\"");
      pw.format(" ColorR=\"255\"");
      pw.format(" NamespaceIdx=\"0\""); // Index of namespace
      pw.format(" Description=\"\"");   // ...
      pw.format("/>\n");
      pw.format("</Namespaces>\n");

      // optional - N.B. do not generate section <Filters>
      // pw.format("<Filters>\n");
      // pw.format("<Filters");
      // pw.format(" Filter=\"\"");
      // pw.format(" Numero=\"\"");
      // pw.format(" Name=\"\"");
      // pw.format(" Expression=\"\"");
      // pw.format(" Description=\"\"");
      // pw.format("/>\n");
      // pw.format("</Filters>\n");

      List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
      checkShotsClino( list );
      TRobot trobot = new TRobot( list );
      // trobot.dump(); // DEBUG

      // mandatory - N.B. only one entrance, typically main entrance
      List< FixedInfo > fixeds = data.selectAllFixed( sid, TDStatus.NORMAL );
      boolean entrance_todo = true;
      pw.format("<Entrances>\n");
      if ( fixeds.size() > 0 ) {
        // int ce = 0;
        for ( FixedInfo fixed : fixeds ) {
          TRobotPoint pt = trobot.getPoint( fixed.name );
          if ( pt != null ) {
            pw.format("<Entrance");
            pw.format(Locale.US, " X=\"%.10f\"", fixed.lng );
            pw.format(Locale.US, " Y=\"%.10f\"", fixed.lat );
            pw.format(Locale.US, " Z=\"%.2f\"",  fixed.h_geo );
            pw.format(" Name=\"%s\"",     fixed.name );
            pw.format(" Numero=\"0\"" );  // ce
            pw.format(" Comments=\"%s\"", fixed.comment );
            pw.format(" RefPoint=\"%d\"", pt.mNumber );
            pw.format(" RefSerie=\"%d\"", pt.mSeries.mNumber );
            // pw.format(" IdTerrain=\"\"" ); // optional
            // pw.format(" Comments=\"\"" );  // optional
            pw.format(" Colour=\"$255000000\"");
            pw.format("/>\n" );
            entrance_todo = false;
            // ++ ce;
            break;
          }
        }
      }
      if ( entrance_todo ) { // use first survey point of first series
        TRobotSeries sr = trobot.mSeries.get(0);
        TRobotPoint  pt = sr.mBegin;
        pw.format("<Entrance");
        pw.format(" X=\"0.00\"" ); // Unknown coords set to 0.00
        pw.format(" Y=\"0.00\"" );
        pw.format(" Z=\"0.00\"" );
        pw.format(" Name=\"First Station\"");       // name of entrance
        pw.format(" Numero=\"0\"");
        pw.format(" Comments=\"\"");
        pw.format(" RefPoint=\"%d\"", pt.mNumber ); // Point number (typically 0)
        pw.format(" RefSerie=\"%d\"", sr.mNumber ); // Series number (typically 1)
        // pw.format(" IdTerrain=\"\"");            // Cave code (optional)
        // pw.format(" Comments=\"\"");             // Comment (optional)
        pw.format(" Colour=\"$255000000\"");        // string $RRRGGGBBB (value is full red)
        pw.format("/>\n");
      }
      pw.format("</Entrances>\n");

      // optional - TopoDroid should not generate it
      // pw.format("<Secteurs>\n");
      // pw.format("<Secteur");
      // pw.format(" Name=\"\"");       // ...
      // pw.format(" Numero=\"\"");     // ...
      // pw.format("/>\n");
      // pw.format("</Secteurs>\n");

      // optional - TopoDroid should not generate it
      // pw.format("<Networks>\n");
      // pw.format("<Network");
      // pw.format(" Name=\"%s\"", info.name ); // name of network
      // pw.format(" Type=\"0\"");              // for future use - set to 0
      // pw.format(" ColorB=\"0\"");
      // pw.format(" ColorG=\"0\"");
      // pw.format(" ColorR=\"255\"");
      // pw.format(" Numero=\"1\"");            // index of network, start from 0
      // pw.format(" Comments=\"\"");           // optional
      // pw.format("/>\n" );
      // pw.format("</Networks>\n");

      // mandatory: instruments code
      pw.format("<Codes>\n");
      pw.format("<Code");
      pw.format(" Numero=\"1\"");             // index of code instruments
      pw.format(" ClinoUnit=\"360\"");        // degrees 360, grad 400
      pw.format(" CompassUnit=\"360\"");
      pw.format(" FactLong=\"1\"");           // length correction factor
      pw.format(" Type=\"0\"");               // for future use - set to 0
      pw.format(" AngleLimite=\"0.0\" ");     // TOPOROBOT angle limite - Set to 0.00 always
      pw.format(" PsiL=\"0.05\"");            // length tolerance [m]
      pw.format(" PsiP=\"1.0\"");             // clino tolerance [clino units]
      pw.format(" PsiAz=\"1.0\"");            // azimuth tolerance
      pw.format(" Comments=\"\"");            // optional
      pw.format(" ErrorTourillon=\"0\"");     // Parameters for Compass and clino correction functions: set to 0
      pw.format(" DiamBoule1=\"0\"");
      pw.format(" DiamBoule2=\"0\"");
      pw.format(" FuncCorrAzCo=\"0\"");
      pw.format(" FuncCorrIncCo=\"0\"");
      pw.format(" FuncCorrAzErrMax=\"0\"");
      pw.format(" FuncCorrIncErrMax=\"0\"");
      pw.format(" FuncCorrAzPosErrMax=\"0\"");
      pw.format(" FuncCorrIncPosErrMax=\"0\"");
      pw.format("/>\n");
      pw.format("</Codes>\n");

      // mandatory
      float declination = ( info.hasDeclination() )? info.getDeclination() : 0;
      pw.format("<Seances>\n");
      pw.format("<Trip");
      pw.format(" Date=\"%s\"", date );             // date YYYY-MM-DD
      pw.format(" ColorIndex=\"%d\"", randomColor() ); // index of TopoRobot palette, in 1..255: Black=1 Red=6 Green=11 Blue=211 Fuchsia=73
      pw.format(" Numero=\"1\"");                   // index of session
      pw.format(" Comments=\"%s\"", info.comment ); // optional
      pw.format(" Surveyor1=\"%s\"", info.team );   // N.B. info.team is not parsed - main operator
      pw.format(" Surveyor2=\"\"");                 // assistant(s)
      pw.format(" ModeDeclination=\"0\"");          // 0 or 1, GHTopo will calculate automatically from date and coordinates. Typically = 0
      pw.format(Locale.US, " Declination=\"%.2f\"", declination);  // magnetic declination
      // pw.format(" Inclination=\"0\"");              // deprecated - set to 0 or omit
      pw.format("/>\n");
      pw.format("</Seances>\n");

      // mandatory
      pw.format("<Series>\n");
      for ( TRobotSeries series : trobot.mSeries ) {
        TRobotPoint dep = series.mBegin;
        TRobotPoint arr = series.mEnd;
        pw.format("<Serie");
        pw.format(" Numero=\"%d\" ", series.mNumber );      // series number
        pw.format(" Name=\"\"");                            // series name
        // pw.format(" Color=\"#0000FF\"");                    // For GHTopo future usage - Must be set to #0000FF - Unused: omit
        pw.format(" SerDep=\"%d\"",  dep.mSeries.mNumber ); // TOPOROBOT notation of the starting station SerDep.PtDep ; eg: 14.18
        pw.format(" PtDep=\"%d\"",   dep.mNumber );         // TOPOROBOT notation of the starting station SerDep.PtDep ; eg: 14.18
        pw.format(" SerArr=\"%d\"",  arr.mSeries.mNumber ); // TOPOROBOT notation of the ending station SerDep.PtDep ; eg: 39.45
        pw.format(" PtArr=\"%d\"",   arr.mNumber );         // TOPOROBOT notation of the endins station SerDep.PtDep ; eg: 39.45
        pw.format(" Network=\"1\"");    // Index of rattachment network - Typically = 0
        pw.format(" Raideur=\"1.00\""); // Stiffness coefficient - Typically = 1.00 for standard shots
        pw.format(" Entrance=\"0\"");   // Index of rattachment entrance - Typically = 0
        pw.format(" Obstacle=\"0\"");   // TOPOROBOT index of obstacle - Typically = 0
        // pw.format(" Comments=\"\"");    // optional
        pw.format(">\n");
        pw.format("<Stations>\n");
        TRobotPoint from = series.mBegin;
        for ( TRobotPoint pt : series.mPoints ) {
          // get leg from-pt and print it
          DBlock blk = pt.mBlk;
          if ( blk != null ) {
            float az   = blk.mBearing;
            float incl = blk.mClino;
            if ( ! pt.mForward ) {
              // az = ( az + 180 ); if ( az >= 360 ) az -= 360;
              az = TDMath.add180( az );
              incl = - incl;
            }
            float len = blk.mLength;
            LRUD lrud = computeLRUD( blk, list, true ); // LRUD at FROM station
            // float up    = 0; // TODO compute from splays as in COMPASS
            // float left  = 0;
            // float down  = 0;
            // float right = 0;
            pw.format("<Shot");
            pw.format(" ID=\"%s\"", pt.mName );
            pw.format(" Code=\"1\"");          // index of instrument - if unknown 1
            pw.format(" Trip=\"1\"");          // index of session - if unknown 1
            pw.format(" Secteur=\"0\"");       // Index of 'Seecteur' (subnetwork) - Typically = 0
            // pw.format(Locale.US, " Label=\"\"");       // Label ID terrain (eg: AB123) - optional
            pw.format(" Horodate=\"%s 00:00:00.00\"", date ); // horodating YYYY-MM-DD HH:MN:SS.MSd. If unknown now() or empty string
            pw.format(" Humidity=\"0.00\"");                  // Humidity ; If unknown or unsupported, set to 0.00
            pw.format(" Temperature=\"0.00\"");               // Temperature. If unknown or unsupported, set to 0.00
            pw.format(" Comments=\"%s\"", blk.mComment );     // optional
            pw.format(" TypeShot=\"%d\"", ( blk.isSurface() ? 7 : 0 ) ); // Type of shot; 0 Default; 1 Natural cave; 7 Surface shot. Typically = 0
            pw.format(Locale.US, " Length=\"%.3f\"", len );
            pw.format(Locale.US, " Az=\"%.2f\"",     az );
            pw.format(Locale.US, " Incl=\"%.2f\"",   incl );
            pw.format(Locale.US, " Left=\"%.2f\"",   lrud.l ); // left );
            pw.format(Locale.US, " Right=\"%.2f\"",  lrud.r ); // right );
            pw.format(Locale.US, " Up=\"%.2f\"",     lrud.u ); // up );
            pw.format(Locale.US, " Down=\"%.2f\"",   lrud.d ); // down );
            pw.format("/>\n");
          }
          from = pt;
        }
        pw.format("</Stations>\n");
        pw.format("</Serie>\n");
      }
      pw.format("</Series>\n");

      // optional (splays)
      pw.format("<AntennaShots>\n");
      // for all splays
      int number = 0;
      for ( DBlock blk : list ) {
        if ( ! blk.isSplay() ) continue;
        TRobotPoint pt = trobot.getPoint( blk.mFrom );
        if ( pt == null ) continue;
        ++ number;
        // Comment: unused (by experience)
        // Trip and Code Label: Unused here (inherits from <PtDep> and <SerDep>)
        if ( blk.isCommented() ) {
          pw.format(Locale.US, "<!-- AntennaShot");
          pw.format(Locale.US, " PtDep=\"%d\"",    pt.mNumber );         // TOPOROBOT notation of the starting station SerDep.PtDep ; eg: 14.18
          pw.format(Locale.US, " SerDep=\"%d\"",   pt.mSeries.mNumber ); // TOPOROBOT notation of the starting station SerDep.PtDep ; eg: 14.18
          pw.format(Locale.US, " Az=\"%.2f\"",     blk.mBearing );
          pw.format(Locale.US, " Incl=\"%.2f\"",   blk.mClino );
          pw.format(Locale.US, " Length=\"%.3f\"", blk.mLength );
          // pw.format(Locale.US, " Network=\"0\"");  // deprecated - set to 0 or omit
          // pw.format(Locale.US, " Secteur=\"0\"");  // deprecated - set to 0 or omit
          pw.format(Locale.US, " Comments=\"%s\"", blk.mComment );
          pw.format("/ -->\n");
        } else {
          // TDLog.v( "TRobot splay " + blk.mFrom + " nr " + number + " Pt " + pt.mSeries.mNumber + "." + pt.mNumber );
          pw.format(Locale.US, "<AntennaShot");
          pw.format(Locale.US, " PtDep=\"%d\"",    pt.mNumber );         // TOPOROBOT notation of the starting station SerDep.PtDep ; eg: 14.18
          pw.format(Locale.US, " SerDep=\"%d\"",   pt.mSeries.mNumber ); // TOPOROBOT notation of the starting station SerDep.PtDep ; eg: 14.18
          pw.format(Locale.US, " Az=\"%.2f\"",     blk.mBearing );
          pw.format(Locale.US, " Incl=\"%.2f\"",   blk.mClino );
          pw.format(Locale.US, " Length=\"%.3f\"", blk.mLength );
          // pw.format(Locale.US, " Network=\"0\"");  // deprecated - set to 0 or omit
          // pw.format(Locale.US, " Secteur=\"0\"");  // deprecated - set to 0 or omit
          pw.format(Locale.US, " Comments=\"%s\"", blk.mComment );
          pw.format("/>\n");
        }
      }
      pw.format("</AntennaShots>\n");
      pw.format("</GHTopo>\n");


      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed Walls export: " + e.getMessage() );
      return 0;
    }
  }

  // =======================================================================
  // GROTTOLF EXPORT
  // commented flag supported for legs

  // // write RLDU and the cross-section points
  // static private void writeGrtProfile( PrintWriter pw, LRUDprofile lrud )
  // {
  //   pw.format(Locale.US, "%.2f %.2f %.2f %.2f\n", lrud.r, lrud.l, lrud.d, lrud.u );
  //   pw.format(Locale.US, "# %d %.1f\n", lrud.size(), lrud.bearing );
  //   int size = lrud.size();
  //   for ( int k = 0; k<size; ++k ) {
  //     pw.format(Locale.US, "# %.1f %.2f\n", lrud.getClino(k), lrud.getDistance(k) );
  //   }
  // }

  // static private void writeGrtLeg( PrintWriter pw, AverageLeg leg, String fr, String to, boolean first,
  //                                  DBlock item, List< DBlock > list )
  // {
  //   LRUDprofile lrud = null;
  //   if ( item.isCommented() ) pw.format("; ");
  //   if ( first ) {
  //     pw.format(Locale.US, "%s %s 0.000 0.000 0.000 ", fr, fr );
  //     lrud = computeLRUDprofile( item, list, true );
  //     writeGrtProfile( pw, lrud );
  //   }
  //   pw.format(Locale.US, "%s %s %.1f %.1f %.2f ", fr, to, leg.bearing(), leg.clino(), leg.length() );
  //   lrud = computeLRUDprofile( item, list, false );
  //   writeGrtProfile( pw, lrud );
  //   leg.reset();
  // }

  // static private void writeGrtFix( PrintWriter pw, String station, List< FixedInfo > fixed )
  // {
  //   if ( fixed.size() > 0 ) {
  //     for ( FixedInfo fix : fixed ) {
  //       if ( station.equals( fix.name ) ) {
  //         pw.format(Locale.US, "%.8f %.8f %.1f\n", fix.lng, fix.lat, fix.h_geo );
  //         return;
  //       }
  //     }
  //   }
  //   pw.format("0.00 0.00 0.00\n");
  // }

  // static int exportSurveyAsGrt( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String name )
  // {
  //   try {
  //     // TDLog.Log( TDLog.LOG_IO, "export Grottolf " + file.getName() );
  //     // BufferedWriter bw = TDFile.getMSwriter( "grt", name + ".grt", "text/grt" );
  //     PrintWriter pw = new PrintWriter( bw );
  // 
  //     pw.format("%s\n", info.name );
  //     pw.format(";\n");
  //     pw.format("; %s created by TopoDroid v %s \n", TDUtil.getDateString("yyyy/MM/dd"), TDVersion.string() );
  //     pw.format(Locale.US, "360.00 360.00 %.2f 1.00\n", info.getDeclination() ); // degrees degrees decl. meters

  //     List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
  //     boolean first = true; // first station
  //     List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
  //     checkShotsClino( list );
  //     // int extend = 1;
  //     AverageLeg leg = new AverageLeg(0);
  //     DBlock ref_item = null;
  //     String ref_from = null;
  //     String ref_to   = null;
  //     for ( DBlock item : list ) {
  //       String from    = item.mFrom;
  //       String to      = item.mTo;
  //       if ( TDString.isNullOrEmpty( from ) ) {
  //         if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
  //           if ( ref_item != null &&
  //              ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
  //             leg.add( item.mLength, item.mBearing, item.mClino );
  //           }
  //         } else { // only TO station
  //           if ( leg.mCnt > 0 && ref_item != null ) {
  //             if ( first ) writeGrtFix( pw, ref_from, fixed );
  //             writeGrtLeg( pw, leg, ref_from, ref_to, first, ref_item, list );
  //             first = false;
  //             ref_item = null; 
  //             ref_from = null;
  //             ref_to   = null;
  //           }
  //         }
  //       } else { // with FROM station
  //         if ( TDString.isNullOrEmpty( to ) ) { // splay shot
  //           if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
  //             if ( first ) writeGrtFix( pw, ref_from, fixed );
  //             writeGrtLeg( pw, leg, ref_from, ref_to, first, ref_item, list );
  //             first = false;
  //             ref_item = null; 
  //             ref_from = null;
  //             ref_to   = null;
  //           }
  //         } else {
  //           if ( leg.mCnt > 0 && ref_item != null ) {
  //             if ( first ) writeGrtFix( pw, ref_from, fixed );
  //             writeGrtLeg( pw, leg, ref_from, ref_to, first, ref_item, list );
  //             first = false;
  //           }
  //           ref_item = item;
  //           ref_from = from;
  //           ref_to   = to;
  //           leg.set( item.mLength, item.mBearing, item.mClino );
  //         }
  //       }
  //     }
  //     if ( leg.mCnt > 0 && ref_item != null ) {
  //       if ( first ) writeGrtFix( pw, ref_from, fixed );
  //       writeGrtLeg( pw, leg, ref_from, ref_to, first, ref_item, list );
  //     }

  //     bw.flush();
  //     bw.close();
  //     return 1;
  //   } catch ( IOException e ) {
  //     TDLog.e( "Failed Walls export: " + e.getMessage() );
  //     return 0;
  //   }
  // }

  // =======================================================================
  // WALLS EXPORT 
  //   duplicate and surface flags are TODO

  static private void writeSrvLeg( PrintWriter pw, AverageLeg leg, float ul, float ua )
  {
    pw.format(Locale.US, "%.2f\t%.1f\t%.1f", leg.length() * ul, leg.bearing() * ua, leg.clino() * ua );
    leg.reset();
  }

  static private void writeSrvStations( PrintWriter pw, String from, String to, boolean cmtd )
  {
    if ( cmtd ) {
      pw.format("; %s\t%s\t", from, to );
    } else {
      pw.format("%s\t%s\t", from, to );
    }
  }

  static private void writeSrvComment( PrintWriter pw, String cmt )
  {
    if ( cmt != null && cmt.length() > 0 ) {
      pw.format("\t; %s\n", cmt );
    } else {
      pw.format("\n");
    }
  }
 
  static int exportSurveyAsSrv( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String name )
  {
    // TDLog.v( "export as walls: " + file.getName() );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    String uls = ( ul < 1.01f )? "Meters"  : "Feet"; // FIXME
    String uas = ( ua < 1.01f )? "Degrees" : "Grads";

    HashMap<String, Integer > splay_stations = TDSetting.mWallsSplays ?  new HashMap<String, Integer >() : null;

    try {
      // TDLog.Log( TDLog.LOG_IO, "export Walls " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "srv", name + ".srv", "text/srv" );
      PrintWriter pw = new PrintWriter( bw );
  
      pw.format("; %s\n", info.name );
      pw.format("; created by TopoDroid v %s - %s \n", TDVersion.string(), TDUtil.getDateString("yyyy.MM.dd") );

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
          TDLog.e( "export survey as SRV date parse error " + date );
        }
      }
   
      pw.format("#Date %04d-%02d-%02d\n", y, m, d ); // format "YYYY-MM-DD"

      if ( info.hasDeclination() ) { // DECLINATION Walls: override declination computed using the date
        pw.format(Locale.US, "#Units Decl=%.1f\n", info.getDeclination() );
      }

      if ( info.comment != null ) {
        pw.format("; %s\n", info.comment );
      }

      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("; TEAM %s\r\n", info.team );
      }

      List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
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
          pw.format(Locale.US, " %.0f", fix.h_geo );
          if ( fix.comment != null && fix.comment.length() > 0 ) pw.format(" /%s", fix.comment );
          pw.format("\n");
        }
      } 

      pw.format("#Units %s A=%s\n", uls, uas );

      List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
      checkShotsClino( list );
      // int extend = 1;
      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false; // TODO
      for ( DBlock item : list ) {
        String from    = item.mFrom;
        String to      = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null &&
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeSrvLeg( pw, leg, ul, ua );
              writeSrvComment( pw, ref_item.mComment );
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
            // if ( item.getIntExtend() != extend ) {
            //   extend = item.getIntExtend();
            //   //  FIXME pw.format("    extend %s\n", therion_extend[1+extend] );
            // }
            if ( TDSetting.mWallsSplays ) {
	      int ii = nextSplayInt( splay_stations, to );
              writeSrvStations( pw, String.format(Locale.US, "%s-%d", to, ii), to, item.isCommented() );
              pw.format(Locale.US, "%.2f\t%.1f\t%.1f", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
            } else {
              writeSrvStations( pw, "-", to, item.isCommented() );
              pw.format(Locale.US, "%.2f\t%.1f\t%.1f", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
            }
            writeSrvComment( pw, item.mComment );
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
              writeSrvLeg( pw, leg, ul, ua );
              writeSrvComment( pw, ref_item.mComment );
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
            // if ( item.getIntExtend() != extend ) {
            //   extend = item.getIntExtend();
            //   // FIXME pw.format("    extend %s\n", therion_extend[1+extend] );
            // }
            if ( TDSetting.mWallsSplays ) {
	      int ii = nextSplayInt( splay_stations, from );
              writeSrvStations( pw, from, String.format(Locale.US, "%s-%d", from, ii), item.isCommented() );
              pw.format(Locale.US, "%.2f\t%.1f\t%.1f", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
            } else {
              writeSrvStations( pw, from, "-", item.isCommented() );
              pw.format(Locale.US, "%.2f\t%.1f\t%.1f", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
            }
            writeSrvComment( pw, item.mComment );
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeSrvLeg( pw, leg, ul, ua );
              writeSrvComment( pw, ref_item.mComment );
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
            // if ( item.getIntExtend() != extend ) {
            //   extend = item.getIntExtend();
            //   // FIXME pw.format("    extend %s\n", therion_extend[1+extend] );
            // }
            if ( item.isDuplicate() ) {
              // FIXME pw.format(therion_flags_duplicate);
              duplicate = true;
            } else if ( item.isSurface() ) {
              // FIXME pw.format(therion_flags_surface);
              surface = true;
            // } else if ( item.isCommented() ) {
            //   ...
            // } else if ( item.isBackshot() ) {
            //   ...
            }
            writeSrvStations( pw, from, to, item.isCommented() );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
        // pw.format(Locale.US, "%.2f\t%.1f\t%.1f\n", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeSrvLeg( pw, leg, ul, ua );
        writeSrvComment( pw, ref_item.mComment );
        // if ( duplicate ) {
          // pw.format(therion_flags_not_duplicate);
          // duplicate = false;
        // } else if ( surface ) {
          // pw.format(therion_flags_not_surface);
          // surface = false;
        // }
      }

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed Walls export: " + e.getMessage() );
      return 0;
    }
  }

  // =======================================================================
  // TOPO EXPORT ( CAV )
  //  handled flags: duplicate surface

  private static long printFlagToCav( PrintWriter pw, long old_flag, long new_flag, String eol )
  {
    if ( old_flag == new_flag ) return old_flag;
    if ( DBlock.isDuplicate(old_flag) ) {
      pw.format("#end_duplicate%s", eol);
    } else if ( DBlock.isSurface(old_flag) ) {
      pw.format("#end_surface%s", eol);
    // } else if ( DBlock.isCommented(old_flag) ) {
    //   pw.format("#end_commented%s", eol);
    }
    if ( DBlock.isDuplicate(new_flag) ) {
      pw.format("#duplicate%s", eol);
    } else if ( DBlock.isSurface(new_flag) ) {
      pw.format("#surface%s", eol);
    // } else if ( DBlock.isCommented(new_flag) ) {
    //   pw.format("#commented%s", eol);
    }
    return new_flag;
  }

  private static void printShotToCav( PrintWriter pw, AverageLeg leg, DBlock item, String eol, ArrayList ents )
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
    if ( item.isCommented() ) pw.format("%% ");
    pw.format(Locale.US, "%s\t%s\t%.2f\t%.1f\t%.1f", item.mFrom, item.mTo, leg.length(), leg.bearing(), leg.clino() );
    if ( item.mComment != null ) {
      pw.format("\t;\t%s%s", item.mComment, eol );
    } else {
      pw.format("%s", eol );
    }
    leg.reset();
  }

  private static void printSplayToCav( PrintWriter pw, DBlock blk, String eol )
  {
    // if ( duplicate ) pw.format("#duplicate%s", eol);
    if ( blk.isCommented() ) pw.format("%% ");
    pw.format(Locale.US, "%s\t-\t%.2f\t%.1f\t%.1f", blk.mFrom, blk.mLength, blk.mBearing, blk.mClino );
    if ( blk.mComment != null ) {
      pw.format("\t;\t%s%s", blk.mComment, eol );
    } else {
      pw.format("%s", eol );
    }
    // if ( duplicate ) pw.format("#end_duplicate%s", eol);
  }

  static private int printCavExtend( PrintWriter pw, int extend, int item_extend, String eol )
  {
    if ( item_extend != extend ) { 
      if ( item_extend == ExtendType.EXTEND_LEFT ) {
        pw.format("#R180%s", eol );
      } else if ( item_extend == ExtendType.EXTEND_RIGHT ) {
        pw.format("#R0%s", eol );
      } else if ( item_extend == ExtendType.EXTEND_VERT ) {
        pw.format("#PR[0]%s", eol );
      }
      return item_extend;
    }
    return extend;
  }

  /** export survey data in CAV format
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsCav( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name )
  {
    // TDLog.v( "export as topo: " + file.getName() );
    String eol = TDSetting.mSurvexEol;
    ArrayList< String > ents = null;

    try {
      // TDLog.Log( TDLog.LOG_IO, "export Topo " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "cav", survey_name + ".cav", "text/cav" );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("#cave %s%s", info.name, eol );
      pw.format("%% Made by: TopoDroid %s - %s%s", TDVersion.string(), TDUtil.currentDate(), eol );

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
          TDLog.e( "export survey as CAV date parse error " + date );
        }
      }
      pw.format("#survey ^%s%s", info.name, eol ); // NOTE "cav" has '^' in front of the cave name (?)
      if ( info.team != null ) pw.format("#survey_team %s%s", info.team, eol );
      pw.format("#survey_date %02d.%02d.%04d%s", d, m, y, eol ); 
      if ( info.comment != null ) pw.format("#survey_title %s%s", info.comment, eol );

      pw.format(Locale.US, "#declination[%.1f]%s", info.getDeclination(), eol ); // DECLINATION Topo seems to use 0.0 in general
      
      List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
      if ( fixed.size() > 0 ) {
        ents = new ArrayList<>();
        for ( FixedInfo fix : fixed ) {
          ents.add( fix.name );
          pw.format(";\t#point\tPoint%s\t", fix.name );
          pw.format(Locale.US, "%.6f\t%.6f\t%.0f%s", fix.lng, fix.lat, fix.h_geo, eol );
          break;
        }
      }

      pw.format("#data_order L Az An%s", eol);
      pw.format("#from_to%s", eol);
      pw.format("#R0%s", eol);

      List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
      checkShotsClino( list );
      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;

      int extend = 1;
      long flag = 0;
      // boolean in_splay = false;
      // LRUD lrud;

      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null && 
              ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              flag = printFlagToCav( pw, flag, ref_item.getFlag(), eol );
              printShotToCav( pw, leg, ref_item, eol, ents );
              ref_item = null; 
            }
            extend = printCavExtend( pw, extend, item.getIntExtend(), eol );
            // TODO export TO splay
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write previous leg shot
              flag = printFlagToCav( pw, flag, ref_item.getFlag(), eol );
              printShotToCav( pw, leg, ref_item, eol, ents );
              ref_item = null; 
            }
            extend = printCavExtend( pw, extend, item.getIntExtend(), eol );
            printSplayToCav( pw, item, eol );
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              flag = printFlagToCav( pw, flag, ref_item.getFlag(), eol );
              printShotToCav( pw, leg, ref_item, eol, ents );
            }
            ref_item = item;
            extend = printCavExtend( pw, extend, item.getIntExtend(), eol );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        flag = printFlagToCav( pw, flag, ref_item.getFlag(), eol );
        printShotToCav( pw, leg, ref_item, eol, ents );
      }      
      flag = printFlagToCav( pw, flag, DBlock.FLAG_SURVEY, eol );
      pw.format( "%s", eol );
      pw.format("#end_declination%s", eol);
      pw.format("#end_survey%s", eol);

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed Topo (cav) export: " + e.getMessage() );
      return 0;
    }
  }

  // =======================================================================
  // POLYGON EXPORT 
  // shot flags are not supported

  private static void printPolygonEOL( PrintWriter pw ) { pw.format( "\r\n" ); }
  private static void printPolygonTabEOL( PrintWriter pw ) { pw.format( "\t\r\n" ); }
  private static void printPolygonTab0EOL( PrintWriter pw ) { pw.format( "\t0\r\n" ); }

  // private static void printShotToPlg( PrintWriter pw, AverageLeg leg, LRUD lrud, String comment )
  // {
  //   pw.format(Locale.US, "%.2f\t%.1f\t%.1f\t\t%.2f\t%.2f\t%.2f\t%.2f\t", 
  //     leg.length(), leg.bearing(), leg.clino(), lrud.l, lrud.r, lrud.u, lrud.d );
  //   leg.reset();
  //   // if ( duplicate ) { pw.format(" #|L#"); }
  //   // if ( surface   ) { pw.format(" #|S#"); }
  //   if ( comment != null && comment.length() > 0 ) {
  //     pw.format("%s", comment );
  //   }
  //   printPolygonEOL( pw );
  // }

  static private void printPolygonData( PrintWriter pw, PolygonData data )
  {
    pw.format( "%s\t%s\t", data.from, data.to );
    pw.format(Locale.US, "%.2f\t%.1f\t%.1f\t\t%.2f\t%.2f\t%.2f\t%.2f\t", 
      data.length, data.bearing, data.clino, data.lrud.l, data.lrud.r, data.lrud.u, data.lrud.d );
    if ( data.comment != null && data.comment.length() > 0 ) {
      pw.format("%s", data.comment );
    }
    printPolygonEOL( pw );
  }
 
  /** export survey data in PLG (Polygon) format
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsPlg( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name )
  {
    // TDLog.v("polygon " + file.getName() );
    float ul = 1; // TDSetting.mUnitLength;
    float ua = 1; // TDSetting.mUnitAngle;
    // String uls = ( ul < 1.01f )? "Meters"  : "Feet"; // FIXME
    // String uas = ( ua < 1.01f )? "Degrees" : "Grads";

    try {
      // TDLog.Log( TDLog.LOG_IO, "export Polygon " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "cave", survey_name + ".cave", "text/cave" );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("POLYGON Cave Surveying Software"); printPolygonEOL( pw );
      pw.format("Polygon Program Version   = 2");   printPolygonEOL( pw );
      pw.format("Polygon Data File Version = 1");   printPolygonEOL( pw );
      pw.format("1998-2001 ===> Prepostffy Zsolt"); printPolygonEOL( pw );
      pw.format("-------------------------------"); printPolygonEOL( pw ); printPolygonEOL( pw );

      pw.format("*** Project ***");                 printPolygonEOL( pw );
      pw.format("Project name: %s", info.name );    printPolygonEOL( pw );
      pw.format("Project place: %s", info.name );   printPolygonEOL( pw );
      pw.format("Project code: 9999");              printPolygonEOL( pw );
      pw.format("Made by: TopoDroid %s", TDVersion.string() );   printPolygonEOL( pw );
      pw.format(Locale.US, "Made date: %f", TDUtil.getDatePlg() ); printPolygonEOL( pw );
      pw.format("Last modi: 0");   printPolygonEOL( pw ); // modi ???
      pw.format("AutoCorrect: 1"); printPolygonEOL( pw );
      pw.format("AutoSize: 20.0"); printPolygonEOL( pw ); printPolygonEOL( pw );

      String date = info.date;
      int y = 0;
      int m = 0;
      int d = 0;
      if ( date != null && date.length() == 10 ) {
        try {
          y = Integer.parseInt( date.substring(0,4) );
          m = TDUtil.parseMonth( date.substring(5,7) );
          d = TDUtil.parseDay( date.substring(8,10) );
        } catch ( NumberFormatException e ) {
          TDLog.e( "export survey as PLG date parse error " + date );
        }
      }
      // TDLog.v("Date Y " + y + " M " + m + " d " + d + " " + date );
      pw.format("*** Surveys ***");             printPolygonEOL( pw );
      pw.format("Survey name: %s", info.name ); printPolygonEOL( pw );
      pw.format("Survey team:");                printPolygonEOL( pw );
      pw.format("\t%s", (info.team != null)? info.team : "" ); printPolygonEOL( pw );
      printPolygonTabEOL( pw );
      printPolygonTabEOL( pw );
      printPolygonTabEOL( pw );
      printPolygonTabEOL( pw );
      pw.format(Locale.US, "Survey date: %f", TDUtil.getDatePlg( y, m, d ) );
      printPolygonEOL( pw );
      pw.format(Locale.US, "Declination: %.1f", info.getDeclination() ); // DECLINATION Polygon seems to have 0.0 in general
      printPolygonEOL( pw );
      pw.format("Instruments:"); printPolygonEOL( pw );
      printPolygonTab0EOL( pw );
      printPolygonTab0EOL( pw );
      printPolygonTab0EOL( pw );

      // if ( info.comment != null ) {
      //   pw.format("; %s\n", info.comment );
      // }

      List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
      if ( fixed.size() > 0 ) {
        for ( FixedInfo fix : fixed ) {
          pw.format("Fix point: %s", fix.name );
          printPolygonEOL( pw );
          pw.format(Locale.US, "%.6f\t%.6f\t%.0f\t0\t0\t0\t0", fix.lng, fix.lat, fix.h_geo );
          printPolygonEOL( pw );
          break;
        }
      } else {
        pw.format("Fix point: 0" );
        printPolygonEOL( pw );
        pw.format(Locale.US, "0\t0\t0\t0\t0\t0\t0" );
        printPolygonEOL( pw );
      }

      pw.format("Survey data");
      printPolygonEOL( pw );
      pw.format("From\tTo\tLength\tAzimuth\tVertical\tLabel\tLeft\tRight\tUp\tDown\tNote");
      printPolygonEOL( pw );

      List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
      checkShotsClino( list );

      int size = 0; // count legs
      for ( DBlock blk : list ) {
        if ( blk.mFrom != null && blk.mFrom.length() > 0 && blk.mTo != null && blk.mTo.length() > 0 ) ++size;
      }
      PolygonData[] polygon_data = new PolygonData[ size ];
      // TDLog.v("size " + size + " list " + list.size() );

      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay = false;
      // boolean duplicate = false;
      // boolean surface   = false;
      LRUD lrud;
      boolean at_from = false;

      int nr_data = 0;
      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, at_from );
              // FIXME_P pw.format("%s\t%s\t", ref_item.mFrom, ref_item.mTo );
              // FIXME_P printShotToPlg( pw, leg, lrud, ref_item.mComment );
              polygon_data[nr_data] = new PolygonData( ref_item.mFrom, ref_item.mTo, leg, lrud, ref_item.mComment );
              nr_data ++;
              leg.reset();
              // duplicate = false;
              // surface = false;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write previous leg shot
              lrud = computeLRUD( ref_item, list, at_from );
              // FIXME_P pw.format("%s\t%s\t", ref_item.mFrom, ref_item.mTo );
              // FIXME_P printShotToPlg( pw, leg, lrud, ref_item.mComment );
              polygon_data[nr_data] = new PolygonData( ref_item.mFrom, ref_item.mTo, leg, lrud, ref_item.mComment );
              nr_data ++;
              leg.reset();
              // duplicate = false;
              // surface = false;
              ref_item = null; 
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, at_from );
              // FIXME_P pw.format("%s\t%s\t", ref_item.mFrom, ref_item.mTo );
              // FIXME_P printShotToPlg( pw, leg, lrud, ref_item.mComment );
              polygon_data[nr_data] = new PolygonData( ref_item.mFrom, ref_item.mTo, leg, lrud, ref_item.mComment );
              nr_data ++;
              leg.reset();
            }
            ref_item = item;
            // duplicate = item.isDuplicate();
            // surface = item.isSurface();
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        lrud = computeLRUD( ref_item, list, at_from );
        // FIXME_P pw.format("%s\t%s\t", ref_item.mFrom, ref_item.mTo );
        // FIXME_P printShotToPlg( pw, leg, lrud, ref_item.mComment );
        polygon_data[nr_data] = new PolygonData( ref_item.mFrom, ref_item.mTo, leg, lrud, ref_item.mComment );
        nr_data ++;
        // leg.reset(); // not necessary
      }

      if ( nr_data > 0 ) {
        PolygonData d0 = polygon_data[0];
        printPolygonData( pw, d0 );
        d0.used = true;
        boolean repeat = true;
        while ( repeat ) {
          repeat = false;
          for ( int n2 = 1; n2 < nr_data; ++n2 ) {
            PolygonData d2 = polygon_data[n2];
            if ( d2.used ) continue;
            String from = d2.from;
            String to   = d2.to;
            for ( int n1 = 0; n1 < nr_data; ++n1 ) {
              PolygonData d1 = polygon_data[n1];
              if ( d1.used ) {
                if ( from.equals( d1.to ) || from.equals( d1.from ) ) {
                  printPolygonData( pw, d2 );
                  d2.used = true;
                  break;
                }
                if ( to.equals( d1.to ) || to.equals( d1.from ) ) { // try reversed
                  d2.reverse();
                  printPolygonData( pw, d2 );
                  d2.used = true;
                  break;
                }
              }
            }
            repeat |= d2.used;
          }
        }
      }

      printPolygonEOL( pw );
      pw.format("End of survey data.");
      printPolygonEOL( pw );
      printPolygonEOL( pw );
      pw.format("*** Surface ***");
      printPolygonEOL( pw );
      pw.format("End of surface data.");
      printPolygonEOL( pw );
      printPolygonEOL( pw );
      pw.format("EOF.");
      printPolygonEOL( pw );

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed Polygon export: " + e.getMessage() );
      return 0;
    }
  }


  // -----------------------------------------------------------------------
  // DXF EXPORT 
  // NOTE declination is taken into account in DXF export (used to compute num)
  // NOTE shot flags are not supported

  /** export survey data in DXF (AutoCAD) format
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsDxf( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, TDNum num, String survey_name )
  {
    // TDLog.v( "export survey as Dxf " + file.getName() );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export DXF " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "dxf", survey_name + ".dxf", "text/dxf" );
      PrintWriter out = new PrintWriter( bw );
      // TODO
      out.printf(Locale.US, "999\nDXF created by TopoDroid v %s - %s ", TDVersion.string(), TDUtil.getDateString("yyyy.MM.dd") );
      if ( info.hasDeclination() ) {
        out.printf(Locale.US, "(declination %.4f)\n", info.getDeclination() ); // DECLINATION DXF
      } else {
        out.printf(Locale.US, "(declination undefined)\n" );
      }
      out.printf("0\nSECTION\n2\nHEADER\n");
      out.printf("9\n$ACADVER\n1\nAC1006\n");
      out.printf("9\n$INSBASE\n");
      out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", 0.0, 0.0, 0.0 ); // FIXME (0,0,0)
      out.printf("9\n$EXTMIN\n");
      float emin =   num.surveyEmin() - 2.0f;
      float nmin = - num.surveySmax() - 2.0f;
      float zmin = - num.surveyVmax() - 2.0f;
      out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", emin, nmin, zmin );
        // num.surveyEmin(), -num.surveySmax(), -num.surveyVmax() );
      out.printf("9\n$EXTMAX\n");
      float emax =   num.surveyEmax();
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
          out.printf("0\nLAYER\n2\nXSPLAY\n70\n%d\n62\n%d\n6\n%s\n",  flag, 6, style );
          out.printf("0\nLAYER\n2\nHSPLAY\n70\n%d\n62\n%d\n6\n%s\n",  flag, 7, style );
          out.printf("0\nLAYER\n2\nVSPLAY\n70\n%d\n62\n%d\n6\n%s\n",  flag, 8, style );
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
	  DBlock blk = sh.getBlock();
          if ( blk.isXSplay() ) {
            out.printf("0\nLINE\n8\nXSPLAY\n");
	  } else if ( blk.isHSplay() ) {         // FIXME_X3_SPLAY
            out.printf("0\nLINE\n8\nHSPLAY\n");
	  } else if ( blk.isVSplay() ) {
            out.printf("0\nLINE\n8\nVSPLAY\n");
          } else {
            out.printf("0\nLINE\n8\nSPLAY\n");
	  }
          out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, -f.s, -f.v );
          out.printf(Locale.US, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", sh.e, -sh.s, -sh.v );
          if ( TDSetting.mDxfBlocks ) {
            out.printf("0\nINSERT\n8\nPOINT\n2\npoint\n");
            // out.printf("41\n1\n42\n1\n") // point scale
            // out.printf("50\n0\n");  // orientation
            // out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", sh.e, -sh.s, -sh.v );
            out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", sh.e, -sh.s, -sh.v );
          }
        }
   
        for ( NumStation st : num.getStations() ) {
          // FIXME station scale is 0.3 (code 40=text height)
          out.printf("0\nTEXT\n8\nSTATION\n");
          out.printf("1\n%s\n", st.name );
          out.printf(Locale.US, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", st.e, -st.s, -st.v );
        }

      }
      out.printf("0\nENDSEC\n");
      out.printf("0\nEOF\n");

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed DXF export: " + e.getMessage() );
      return 0;
    }
  }

  // -----------------------------------------------------------------------
  // VISUALTOPO EXPORT - VTOPO
  // FIXME photos
  // FIXME not sure declination written in the right place
  // shot flags are not supported

  /** write a shot data to the output file, in TRO format
   * @param pw     writer
   * @param item   reference shot
   * @param list   list of data, to compute LRUD
   * @param suffix station names suffix
   * note item is guaranteed not null by the caller
   */
  static private boolean printStartShotToTro( PrintWriter pw, DBlock item, List< DBlock > list, String suffix )
  {
    if ( item == null ) return false;
    LRUD lrud = computeLRUD( item, list, ! TDSetting.mVTopoLrudAtFrom ); // default: mVTopoLrudAtFrom = false
    String station = TDSetting.mVTopoLrudAtFrom ? item.mTo : item.mFrom;
    if ( suffix != null && suffix.length() > 0 ) station = station + suffix;
    pw.format(Locale.US, "%s %s 0.00 0.00 0.00 ", station, station );
    pw.format(Locale.US, "%.2f %.2f %.2f %.2f N I *", lrud.l, lrud.r, lrud.u, lrud.d );
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
    return true;
  }

  /** print shot comment, if any, and close "Visee" tag
   * @param pw    print writer
   * @param item  shot
   */
  static private void printItemComment( PrintWriter pw, DBlock item )
  {
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(">\r\n");
      pw.format("  <Commentaire>%s</Commentaire>\r\n", item.mComment );
      pw.format("</Visee>\r\n");
    } else {
      pw.format("/>\r\n");
    }
  }

  /** write a shot data to the output file, in TROX form
   * @param pw     writer
   * @param item   reference shot
   * @param list   list of data, to compute LRUD
   * @param suffix station names suffix
   * note item is guaranteed not null by the caller
   */
  static private boolean printStartShotToTrox( PrintWriter pw, DBlock item, List< DBlock > list, String suffix ) // , int ref, int suiv )
  {
    if ( item == null ) return false;
    LRUD lrud = computeLRUD( item, list, ! TDSetting.mVTopoLrudAtFrom ); // default: mVTopoLrudAtFrom = false
    String station = TDSetting.mVTopoLrudAtFrom ? item.mTo : item.mFrom;
    if ( suffix != null && suffix.length() > 0 ) station = station + suffix;
    pw.format("<Visee");
    pw.format(" Dep=\"%s\"", station );
    pw.format(" Arr=\"%s\"", station );
    pw.format(" Long=\"0.00\"");
    pw.format(" Az=\"0.0\"");
    pw.format(" Pte=\"0.0\"");
    pw.format(Locale.US, " G=\"%.2f\"", lrud.l );
    pw.format(Locale.US, " D=\"%.2f\"", lrud.r );
    pw.format(Locale.US, " H=\"%.2f\"", lrud.u );
    pw.format(Locale.US, " B=\"%.2f\"", lrud.d );
    // pw.format(" Ref=\"%d\"",  ref );
    // pw.format(" Suiv=\"%d\"", suiv );
    printItemComment( pw, item );
    
    return true;
  }

  static private void printShotToTro( PrintWriter pw, DBlock item, AverageLeg leg, LRUD lrud, String suffix )
  {
    if ( item == null ) return; // false;
    // TDLog.v( "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    if ( TDString.isNullOrEmpty( suffix ) ) {
      pw.format("%s %s ", item.mFrom, item.mTo );
    } else {
      pw.format("%s%s %s%s ", item.mFrom, suffix, item.mTo, suffix );
    }
    pw.format(Locale.US, "%.2f %.1f %.1f ", leg.length(), leg.bearing(), leg.clino() );
    leg.reset();
    pw.format(Locale.US, "%.2f %.2f %.2f %.2f N I *", lrud.l, lrud.r, lrud.u, lrud.d );
    // if ( duplicate ) pw.format(" #|L#");
    // if ( surface ) pw.forma(" #|S#");
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
  }

  static private void printShotToTroxDiving( PrintWriter pw, DBlock item, AverageLeg leg, String suffix ) // , int ref, int suiv )
  {
    if ( item == null ) return; // false;
    // TDLog.v( "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    pw.format("<Visee");
    if ( TDString.isNullOrEmpty( suffix ) ) {
      pw.format(" Dep=\"%s\"", item.mFrom );
      pw.format(" Arr=\"%s\"", item.mTo );
    } else {
      pw.format(" Dep=\"%s%s\"", item.mFrom, suffix );
      pw.format(" Arr=\"%s%s\"", item.mTo, suffix );
    }
    pw.format(Locale.US, " Long=\"%.2f\"", leg.clino() ); 
    pw.format(Locale.US, " Az=\"%.1f\"",   leg.bearing() );
    pw.format(Locale.US, " Pte=\"%.1f\"",  - leg.length() ); // VTopo has prof instead of depth
    leg.reset();
    // pw.format(" Ref=\"%d\"",  ref );
    // pw.format(" Suiv=\"%d\"", suiv );
    if ( item.isDuplicate() ) pw.format(" Exc=\"E\"");
    // if ( surface ) pw.forma(" #|S#");
    printItemComment( pw, item );
  }

  static private void printShotToTrox( PrintWriter pw, DBlock item, AverageLeg leg, LRUD lrud, String suffix ) // , int ref, int suiv )
  {
    if ( item == null ) return; // false;
    // TDLog.v( "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    pw.format("<Visee");
    if ( TDString.isNullOrEmpty( suffix ) ) {
      pw.format(" Dep=\"%s\"", item.mFrom );
      pw.format(" Arr=\"%s\"", item.mTo );
    } else {
      pw.format(" Dep=\"%s%s\"", item.mFrom, suffix );
      pw.format(" Arr=\"%s%s\"", item.mTo, suffix );
    }
    pw.format(Locale.US, " Long=\"%.2f\"", leg.length() );
    pw.format(Locale.US, " Az=\"%.1f\"",   leg.bearing() );
    pw.format(Locale.US, " Pte=\"%.1f\"",  leg.clino() );
    leg.reset();
    pw.format(Locale.US, " G=\"%.2f\"", lrud.l );
    pw.format(Locale.US, " D=\"%.2f\"", lrud.r );
    pw.format(Locale.US, " H=\"%.2f\"", lrud.u );
    pw.format(Locale.US, " B=\"%.2f\"", lrud.d );
    // pw.format(" Ref=\"%d\"",  ref );
    // pw.format(" Suiv=\"%d\"", suiv );
    if ( item.isDuplicate() ) pw.format(" Exc=\"E\"");
    // if ( surface ) pw.forma(" #|S#");
    printItemComment( pw, item );
  }

  static private void printSplayToTro( PrintWriter pw, DBlock item, boolean direct, String suffix )
  {
    if ( ! TDSetting.mVTopoSplays ) return; // false;
    if ( item == null ) return; // false;
    // TDLog.v( "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    if ( direct ) {
      if ( TDString.isNullOrEmpty( suffix ) ) {
        pw.format("%s * ", item.mFrom );
      } else {
        pw.format("%s%s * ", item.mFrom, suffix );
      }
      pw.format(Locale.US, "%.2f %.1f %.1f * * * * N E", item.mLength, item.mBearing, item.mClino );
    } else {
      // float b = item.mBearing + 180; if ( b >= 360 ) b -= 360;
      float b = TDMath.add180( item.mBearing );
      if ( TDString.isNullOrEmpty( suffix ) ) {
        pw.format("%s * ", item.mTo );
      } else {
        pw.format("%s%s * ", item.mTo, suffix );
      }
      pw.format(Locale.US, "%.2f %.1f %.1f * * * * N E", item.mLength, b, - item.mClino );
    }
    pw.format( (item.isCommented() ? " D" : " M" ) );
    // if ( duplicate ) pw.format(" #|L#");
    // if ( surface ) pw.format(" #|S#");
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
  }

  static private void printSplayToTroxDiving( PrintWriter pw, DBlock item, boolean direct, String suffix ) // , int ref )
  {
    // if ( ! TDSetting.mVTopoSplays ) return; // false; - chekcked before call
    if ( item == null ) return; // false;
    // TDLog.v( "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    pw.format("    <Visee ");
    if ( direct ) {
      if ( TDString.isNullOrEmpty( suffix ) ) {
        pw.format("Dep=\"%s\" ", item.mFrom );
      } else {
        pw.format("Dep=\"%s%s\" ", item.mFrom, suffix );
      }
      pw.format(Locale.US, "Long=\"%.2f\" Az=\"%.1f\" Pte=\"%.1f\" ", item.mClino, item.mBearing, - item.mLength ); // VTopo has prof instead of depth
    } else {
      // float b = item.mBearing + 180; if ( b >= 360 ) b -= 360;
      float b = TDMath.add180( item.mBearing );
      if ( TDString.isNullOrEmpty( suffix ) ) {
        pw.format("Dep=\"%s\" ", item.mTo );
      } else {
        pw.format("Dep=\"%s%s\" ", item.mTo, suffix );
      }
      pw.format(Locale.US, "Long=\"%.2f\" Az=\"%.1f\" Pte=\"%.1f\" ", item.mClino, b, - item.mLength ); // VTopo has prof instead of depth
    }
    // pw.format(Locale.US, "Ref=\"%d\" ", ref );
    if (item.isCommented() ) pw.format("Exc=\"E\" ");
    // if ( duplicate ) pw.format(" #|L#");
    // if ( surface ) pw.format(" #|S#");
    printItemComment( pw, item );
  }

  static private void printSplayToTrox( PrintWriter pw, DBlock item, boolean direct, String suffix ) // , int ref )
  {
    // if ( ! TDSetting.mVTopoSplays ) return; // false; - chekcked before call
    if ( item == null ) return; // false;
    // TDLog.v( "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    pw.format("    <Visee ");
    if ( direct ) {
      if ( TDString.isNullOrEmpty( suffix ) ) {
        pw.format("Dep=\"%s\" ", item.mFrom );
      } else {
        pw.format("Dep=\"%s%s\" ", item.mFrom, suffix );
      }
      pw.format(Locale.US, "Long=\"%.2f\" Az=\"%.1f\" Pte=\"%.1f\" ", item.mLength, item.mBearing, item.mClino );
    } else {
      // float b = item.mBearing + 180; if ( b >= 360 ) b -= 360;
      float b = TDMath.add180( item.mBearing );
      if ( TDString.isNullOrEmpty( suffix ) ) {
        pw.format("Dep=\"%s\" ", item.mTo );
      } else {
        pw.format("Dep=\"%s%s\" ", item.mTo, suffix );
      }
      pw.format(Locale.US, "Long=\"%.2f\" Az=\"%.1f\" Pte=\"%.1f\" ", item.mLength, b, - item.mClino );
    }
    // pw.format(Locale.US, "Ref=\"%d\" ", ref );
    if (item.isCommented() ) pw.format("Exc=\"E\" ");
    // if ( duplicate ) pw.format(" #|L#");
    // if ( surface ) pw.format(" #|S#");
    printItemComment( pw, item );
  }

  /** export survey data in TRO (VisualTopo) format
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsTro( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name, String suffix )
  {
    // TDLog.v( "export as visualtopo: " + file.getName() );
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export VisualTopo " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "tro", survey_name + ".tro", "text/tro" );
      PrintWriter pw = new PrintWriter( bw );

      StringWriter sw  = new StringWriter();
      PrintWriter  psw = new PrintWriter( sw );
  
      pw.format("Version 5.02\r\n\r\n");
      pw.format("; %s created by TopoDroid v %s\r\n\r\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
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
      
      // VISUALTOPO: use 0 if declination is undefined
      pw.format(Locale.US, "Param Deca Degd Clino Degd %.4f Dir,Dir,Dir %s Inc 0,0,0\r\n\r\n",
        info.getDeclination(), ( TDSetting.mVTopoLrudAtFrom ? "Dep" : "Arr" ) );

      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay  = false;
      // boolean duplicate = false;
      // boolean surface   = false;
      boolean started   = false;
      LRUD lrud;

      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              // TDLog.v( "data " + item.mLength + " " + item.mBearing + " " + item.mClino );
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list, suffix );
              }
	      pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
              printShotToTro( pw, ref_item, leg, lrud, suffix );
              // duplicate = false;
              // surface = false;
              ref_item = null; 
            }
	    printSplayToTro( psw, item, false, suffix );
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list, suffix );
              }
	      pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
              printShotToTro( pw, ref_item, leg, lrud, suffix );
              // duplicate = false;
              // surface = false;
              ref_item = null; 
            }
	    printSplayToTro( psw, item, true, suffix );
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list, suffix );
              }
	      pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
              printShotToTro( pw, ref_item, leg, lrud, suffix );
            }
            ref_item = item;
            // duplicate = item.isDuplicate();
            // surface = item.isSurface();
            // TDLog.v( "first data " + item.mLength + " " + item.mBearing + " " + item.mClino );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        if ( ! started ) {
          started = printStartShotToTro( pw, ref_item, list, suffix );
        }
	pw.format( sw.toString() );
        lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
        printShotToTro( pw, ref_item, leg, lrud, suffix );
      } else {
	pw.format( sw.toString() );
      }

      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed VisualTopo export: " + e.getMessage() );
      return 0;
    }
  }

  static int countLignesTrox( List<DBlock> list )
  {
    int ret = 1; // for Param line
    AverageLeg leg = new AverageLeg(0);
    DBlock ref_item = null;
   
    boolean started   = false;
    for ( DBlock item : list ) {
      String from = item.mFrom;
      String to   = item.mTo;
      if ( TDString.isNullOrEmpty( from ) ) {
        if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
          if ( ref_item != null && ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
            // TDLog.v( "data " + item.mLength + " " + item.mBearing + " " + item.mClino );
            leg.add( item.mLength, item.mBearing, item.mClino );
          }
        } else { // only TO station
          if ( leg.mCnt > 0 && ref_item != null ) {
            if ( ! started ) {
              started = true;
              ++ret;
            }
            ++ret;
            ref_item = null; 
          }
          ++ret;
        }
      } else { // with FROM station
        if ( TDString.isNullOrEmpty( to ) ) { // splay shot
          if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
            if ( ! started ) {
              started = true;
              ++ret;
            }
            ++ret;
            ref_item = null; 
          }
          ++ret;
        } else {
          if ( leg.mCnt > 0 && ref_item != null ) {
            if ( ! started ) {
              started = true;
              ++ret;
            }
            ++ret;
          }
          ref_item = item;
          leg.set( item.mLength, item.mBearing, item.mClino );
        }
      }
    }
    if ( leg.mCnt > 0 && ref_item != null ) {
      if ( ! started ) {
        started = true;
        ++ret;
      }
      ++ret;
    }
    return ret;
  }


  /** export survey data in TROX (VisualTopo) format
   * @param bw      buffered writer
   * @param sid     survey ID
   * @param data    database helper
   * @param info    survey info
   * @param survey_name survey export name (unused)
   */ 
  static int exportSurveyAsTrox( BufferedWriter bw, long sid, DataHelper data, SurveyInfo info, String survey_name, String suffix )
  {
    boolean diving = info.isDivingMode();

    // TDLog.v( "export as visualtopo-X " );
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    int lignes = countLignesTrox( list );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export VisualTopo " + file.getName() );
      // BufferedWriter bw = TDFile.getMSwriter( "tro", survey_name + ".tro", "text/tro" );
      PrintWriter pw = new PrintWriter( bw );

      StringWriter sw = new StringWriter(); // splays lines
      PrintWriter  psw = new PrintWriter( sw );

      pw.format("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" );
      pw.format("<!-- %s created by TopoDroid v %s -->\r\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
      pw.format("<VisualTopo>\r\n");
      pw.format("<Version>5.15</Version>\r\n");
      pw.format("<Lignes>%d</Lignes>\r\n", lignes );

      pw.format("<Cavite>\r\n");
      pw.format("<Nom>%s</Nom>\r\n", info.name );
      if ( fixed.size() > 0 ) { 
        FixedInfo fix = fixed.get(0);
        if ( fix != null ) {
          pw.format(Locale.US, "<Coordonnees");
          pw.format(Locale.US, " X=\"%.7f\"", fix.lng );
          pw.format(Locale.US, " Y=\"%.7f\"", fix.lat );
          pw.format(Locale.US, " Z=\"%.2f\"", fix.h_geo );
          pw.format(" Projection=\"WGS84\"");
          pw.format("/>\r\n");
          pw.format("<Entree>%s</Entree>\r\n", fix.name );
        } else { // this should never happen
          pw.format("<Entree>%s</Entree>\r\n", list.get(0).mFrom );
        }
      } else {
        pw.format("<Entree>%s</Entree>\r\n", list.get(0).mFrom );
      }
      if ( info.team != null && info.team.length() > 0 ) {
        pw.format("<Club>%s</Club>\r\n", info.team );
      }
      pw.format("<Toporobot>0</Toporobot>\r\n");
      pw.format("<Coleur>0,0,0</Coleur>\r\n");
      pw.format("</Cavite>\r\n");
      pw.format("<Mesures>\r\n");
      
      // VISUALTOPO: use 0 if declination is undefined
      pw.format("<Param");
      pw.format(" InstrDist=\"Deca\"");
      pw.format(" UnitDir=\"Degd\"");
      if ( diving ) {
        pw.format(" InstrPte=\"Prof\"");
      } else {
        pw.format(" InstrPte=\"Clino\"");
        pw.format(" UnitPte=\"Degd\"");
      }
      pw.format(Locale.US, " Declin=\"%.4f\"", info.getDeclination() );
      pw.format(" SensDir=\"Dir\"");
      pw.format(" SensPte=\"Dir\"");
      pw.format(" SensLar=\"Dir\"");
      pw.format(" DimPt=\"%s\"", ( TDSetting.mVTopoLrudAtFrom ? "Dep" : "Arr" ) );
      pw.format(" Coul=\"0,0,0\"");
      pw.format(" Date=\"%s\" ", TDUtil.toVTopoDate( info.date ) );
      if ( fixed.size() > 0 ) {
        pw.format(" DeclinAuto=\"A\"" );
      } else {
        pw.format(" DeclinAuto=\"M\"" );
      }
      pw.format(">\r\n" );

      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format("<Commentaire>%s</Commentaire>\r\n", info.comment );
      }

      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay  = false;
      // boolean duplicate = false;
      // boolean surface   = false;
      boolean started   = false;
      LRUD lrud;

      // int ref  = 0;
      // int suiv = 0;
      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( TDString.isNullOrEmpty( from ) ) {
          if ( TDString.isNullOrEmpty( to ) ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              // TDLog.v( "data " + item.mLength + " " + item.mBearing + " " + item.mClino );
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( ! started ) started = printStartShotToTrox( pw, ref_item, list, suffix ); // , ref, suiv );
              pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              if ( diving ) {
                printShotToTroxDiving( pw, ref_item, leg, suffix ); // , ref, suiv );
              } else {
                lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
                printShotToTrox( pw, ref_item, leg, lrud, suffix ); // , ref, suiv );
              }
              // duplicate = false;
              // surface = false;
              ref_item = null; 
            }
            if ( TDSetting.mVTopoSplays ) {
              if ( diving ) {
                printSplayToTroxDiving( psw, item, false, suffix ); // , ref );
              } else {
                printSplayToTrox( psw, item, false, suffix ); // , ref );
              }
            }
          }
        } else { // with FROM station
          if ( TDString.isNullOrEmpty( to ) ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              if ( ! started ) started = printStartShotToTrox( pw, ref_item, list, suffix ); // , ref, suiv );
              pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              if ( diving ) {
                printShotToTroxDiving( pw, ref_item, leg, suffix ); // , ref, suiv );
              } else {
                lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
                printShotToTrox( pw, ref_item, leg, lrud, suffix ); // , ref, suiv );
              }
              // duplicate = false;
              // surface = false;
              ref_item = null; 
            }
            if ( TDSetting.mVTopoSplays ) {
              if ( diving ) {
                printSplayToTroxDiving( psw, item, true, suffix ); // , ref );
              } else {
                printSplayToTrox( psw, item, true, suffix ); // , ref );
              }
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( ! started ) started = printStartShotToTrox( pw, ref_item, list, suffix ); // , ref, suiv );
              pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              if ( diving ) {
                printShotToTroxDiving( pw, ref_item, leg, suffix ); // , ref, suiv );
              } else {
                lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
                printShotToTrox( pw, ref_item, leg, lrud, suffix ); // , ref, suiv );
              }
            }
            ref_item = item;
            // duplicate = item.isDuplicate();
            // surface = item.isSurface();
            // TDLog.v( "first data " + item.mLength + " " + item.mBearing + " " + item.mClino );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        if ( ! started ) started = printStartShotToTrox( pw, ref_item, list, suffix ); // , ref, suiv );
        pw.format( sw.toString() );
        if ( diving ) {
          printShotToTroxDiving( pw, ref_item, leg, suffix ); // , ref, suiv );
        } else {
          lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
          printShotToTrox( pw, ref_item, leg, lrud, suffix ); // , ref, suiv );
        }
      } else {
        pw.format( sw.toString() );
      }
      pw.format("</Param>\r\n");
      pw.format("</Mesures>\r\n");
      pw.format("</VisualTopo>\r\n");
      bw.flush();
      bw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.e( "Failed VisualTopo export: " + e.getMessage() );
      return 0;
    }
  }

}
