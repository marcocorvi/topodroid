/* @file TDExporter.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @grief TopoDroid exports
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
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
 *   GeoJSON
 *   Track file (OziExplorer)
 *   DXF
 *   CSV
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDStatus;
import com.topodroid.num.TDNum;
import com.topodroid.num.NumStation;
import com.topodroid.num.NumShot;
import com.topodroid.num.NumSplay;
import com.topodroid.num.NumBranch;
import com.topodroid.mag.Geodetic;
import com.topodroid.ptopo.PTFile;
import com.topodroid.shp.ShpPointz;
import com.topodroid.shp.ShpPolylinez;
import com.topodroid.trb.TRobotPoint;
import com.topodroid.trb.TRobotSeries;
import com.topodroid.trb.TRobot;
import com.topodroid.prefs.TDSetting;

import android.util.Log;

import java.io.File;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.io.FileReader;
import java.io.BufferedReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashMap;

import android.util.Base64;

class TDExporter
{
                                                 // -1      0           1        2         3       4        5
  private static final String[] therion_extend = { "left", "vertical", "right", "ignore", "hide", "start", "unset", "left", "vert", "right" };
  private static final int[] csurvey_extend = { 1, 2, 0, 0, 0, 0, 0, 1, 2, 0 };
  private static final String   therion_flags_duplicate     = "   flags duplicate\n";
  private static final String   therion_flags_not_duplicate = "   flags not duplicate\n";
  private static final String   therion_flags_surface       = "   flags surface\n";
  private static final String   therion_flags_not_surface   = "   flags not surface\n";

  private static double mERadius = Geodetic.EARTH_A;
  private static double mSRadius = Geodetic.EARTH_A;

  private static void checkShotsClino( List< DBlock > list )
  {
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) {
      for ( DBlock blk : list ) {
	if ( blk.mTo != null && blk.mTo.length() > 0 && blk.mFrom != null && blk.mFrom.length() > 0 ) {
          // sets the blocks clinos
          TDNum num = new TDNum( list, blk.mFrom, null, null, 0.0f, null ); // no declination, null formatClosure
	  break;
	}
      }
    }
  }

  static byte[] readFileBytes( File file )
  {
    int len = (int)file.length();
    if ( len > 0 ) {
      byte[] buf = new byte[ len ];
      int read = 0;
      try {
        // TDLog.Log( TDLog.LOG_IO, "read file bytes: " + file.getPath() );
        FileInputStream fis = new FileInputStream( file );
        BufferedInputStream bis = new BufferedInputStream( fis );
        while ( read < len ) {
          read += bis.read( buf, read, len-read );
        }
        if ( bis != null ) bis.close();
        if ( fis != null ) fis.close();
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
    AudioInfo audio = data.getAudio( sid, bid );
    List< PhotoInfo > photos = data.selectPhotoAtShot( sid, bid );
    if ( audio == null && photos.size() == 0 ) return;
    pw.format("      <attachments>\n");
    if ( audio != null ) {
      // Log.v("DistoX", "audio " + audio.id + " " + audio.shotid + " blk " + bid );
      File audiofile = new File( TDPath.getSurveyAudioFile( survey, Long.toString(bid) ) );
      if ( audiofile.exists() ) {
        byte[] buf = readFileBytes( audiofile );
        if ( buf != null ) {
          pw.format("        <attachment dataformat=\"0\" data=\"%s\" name=\"\" note=\"\" type=\"audio/x-wav\" />\n",
            Base64.encodeToString( buf, Base64.NO_WRAP ) );
        }
      }
    }
    String photodir = TDPath.getSurveyPhotoDir( survey );
    for ( PhotoInfo photo : photos ) {
      File photofile = new File( TDPath.getSurveyJpgFile( survey, Long.toString(photo.id) ) );
      if ( photofile.exists() ) {
        byte[] buf = readFileBytes( photofile );
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

  // @return 1 on success or 0 on error
  static int exportSurveyAsCsx( long sid, DataHelper data, SurveyInfo info, PlotSaveData psd1, PlotSaveData psd2,
                                   String origin, File file )
  {
    // Log.v("DistoX", "export as csurvey: " + file.getName() );
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

    List< DBlock > dlist = data.selectAllExportShots( sid, TDStatus.NORMAL );
    List< DBlock > clist = data.selectAllExportShots( sid, TDStatus.CHECK );
    checkShotsClino( dlist );

    List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    // List< PlotInfo > plots  = data.selectAllPlots( sid, TDStatus.NORMAL );
    // FIXME TODO_CSURVEY
    // List< CurrentStation > stations = data.getStations( sid );

    if ( origin == null ) { // use first non-null "from"
      for ( DBlock item : dlist ) {
        String from = item.mFrom;
        if ( from != null && from.length() > 0 ) {
          origin = from;
          break;
        }
      }
    }

    try {
      FileWriter fw  = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );
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
      pw.format("    <sessions>\n");
      pw.format("      <session date=\"%s\" ", info.date); // FIXME yyyy-mm-dd
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

      String session = info.date.replaceAll("\\.", "") + "_" +  cave.replaceAll(" ", "_");
      session = session.toLowerCase(Locale.US);

   // ============== CAVE INFOS and BRANCHES
      pw.format("    <caveinfos>\n");
      pw.format("      <caveinfo name=\"%s\"", cave );
      // pw.format( " color=\"\"");
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format( " comment=\"%s\"\n", toXml( info.comment ) );
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

      for ( DBlock blk : clist ) { // calib-check shots
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

      for ( DBlock item : dlist ) {
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
                    fix.lat, fix.lng, fix.asl, fix.lat, fix.lng, fix.asl );
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

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed cSurvey export: " + e.getMessage() );
      return 0;
    }
  }

  // =======================================================================
  // KML export Keyhole Markup Language
  //   NOTE shot flags are ignored

  static GeoReference getGeolocalizedStation( long sid, DataHelper data, float asl_factor, boolean ellipsoid_altitude, String station )
  {
    float decl = data.getSurveyDeclination( sid );
    if ( decl >= SurveyInfo.DECLINATION_MAX ) decl = 0; // if unset use 0

    List< TDNum > nums = getGeolocalizedData( sid, data, decl, asl_factor, ellipsoid_altitude );
    if ( nums == null ) return null;
    for ( TDNum num : nums ) {
      for ( NumStation st : num.getStations() ) {
        if ( station.equals( st.name ) ) return new GeoReference( st.e, st.s, st.v, mERadius, mSRadius );
      }
    }
    return null;
  }

  static private List< TDNum > getGeolocalizedData( long sid, DataHelper data, float decl, float asl_factor, boolean ellipsoid_altitude )
  {
    List< FixedInfo > fixeds = data.selectAllFixed( sid, 0 );
    // Log.v("DistoX-DECL", "get geoloc. data. Decl " + decl + " fixeds " + fixeds.size() );
    if ( fixeds.size() == 0 ) return null;

    List< TDNum > nums = new ArrayList< TDNum >();
    List< DBlock > shots_data = data.selectAllExportShots( sid, 0 );
    FixedInfo origin = null;
    for ( FixedInfo fixed : fixeds ) {
      TDNum num = new TDNum( shots_data, fixed.name, null, null, decl, null ); // null formatClosure
      // Log.v("DistoX", "Num shots " + num.getShots().size() );
      if ( num.getShots().size() > 0 ) {
        makeGeolocalizedData( num, fixed, asl_factor, ellipsoid_altitude );
	nums.add( num );
      } 
    }
    // if ( origin == null || num == null ) return null;
    return nums;
  }

  static private void  makeGeolocalizedData( TDNum num, FixedInfo origin, float asl_factor, boolean ellipsoid_altitude )
  {

    double lat = origin.lat;
    double lng = origin.lng;
    double asl = ellipsoid_altitude ? origin.alt 
                                    : origin.asl; // KML uses Geoid altitude (unless altitudeMode is set)
    double s_radius = 1 / Geodetic.meridianRadiusApprox( lat );
    double e_radius = 1 / Geodetic.parallelRadiusApprox( lat );

    mERadius = e_radius; // save radii factors for getGeolocalizedStation
    mSRadius = s_radius;

    // Log.v("DistoX", "st cnt " + NumStation.cnt + " size " + num.getStations().size() );

    for ( NumStation st : num.getStations() ) {
      st.s = (float)(lat - st.s * s_radius);
      st.e = (float)(lng + st.e * e_radius);
      st.v = (float)(asl - st.v) * asl_factor;
    }
    for ( NumStation cst : num.getClosureStations() ) {
      cst.s = (float)(lat - cst.s * s_radius);
      cst.e = (float)(lng + cst.e * e_radius);
      cst.v = (float)(asl - cst.v) * asl_factor;
    }
    for ( NumSplay sp : num.getSplays() ) {
      sp.s = (float)(lat - sp.s * s_radius);
      sp.e = (float)(lng + sp.e * e_radius);
      sp.v = (float)(asl - sp.v) * asl_factor;
    }
  }

  static int exportSurveyAsKml( long sid, DataHelper data, SurveyInfo info, File file )
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
    // Log.v("DistoX", "export as KML " + file.getFilename() );
    List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), 1.0f, false ); // false: Geoid altitude
    if ( nums == null || nums.size() == 0 ) {
      TDLog.Error( "Failed KML export: no geolocalized station");
      return 2;
    }

    String date = info.date.replaceAll("\\.", "-");

    // now write the KML
    try {
      // TDLog.Log( TDLog.LOG_IO, "export KML " + file );
      FileWriter fw  = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      pw.format("<kml xmlnx=\"http://www.opengis.net/kml/2.2\">\n");
      pw.format("<Document>\n");

      pw.format(name, info.name );
      pw.format("<description>%s - TopoDroid v %s</description>\n",  TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
      pw.format("<TimeStamp><when>%s</when></TimeStamp>\n", date );

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
          //   // Log.v("DistoX", "missing coords " + from.name + " " + from.hasExtend() + " " + to.name + " " + to.hasExtend() );
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
      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed KML export: " + e.getMessage() );
      return 0;
    }
  }
  // =======================================================================

  // @param sid      survey ID
  // @param data     database helper object
  // @param info     survey metadata
  // @param filename filepath without extension 
  static String exportSurveyAsShp( long sid, DataHelper data, SurveyInfo info, String filename )
  {
    List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), 1.0f, false ); // false: Geoid altitude
    if ( nums == null || nums.size() == 0 ) {
      TDLog.Error( "Failed SHP export: no geolocalized station");
      return "";
    }

    boolean success = true;

    try {
      // TDLog.Log( TDLog.LOG_IO, "export SHP " + filename );
      // TDPath.checkPath( filename );
      File dir = new File( filename );
      if ( (dir != null) && ( dir.exists() || dir.mkdirs() ) ) {
        ArrayList< File > files = new ArrayList<>();
        int nr = 0;
        if ( TDSetting.mKmlStations ) {
          for ( TDNum num : nums ) {
            String filepath = filename + "/stations-" + nr;
            ++ nr;
            List< NumStation > stations = num.getStations();
            // Log.v("DistoX", "SHP export " + filepath + " stations " + stations.size() );
            ShpPointz shp = new ShpPointz( filepath, files );
            shp.setYYMMDD( info.date );
            success &= shp.writeStations( stations );
          }
        }

        nr = 0;
        for ( TDNum num : nums ) {
          String filepath = filename + "/shots-" + nr;
          ++ nr;
          List< NumShot > shots = num.getShots();
          List< NumSplay > splays = ( TDSetting.mKmlSplays ? num.getSplays() : null );
          // Log.v("DistoX", "SHP export " + filepath + " shots " + shots.size() );
          ShpPolylinez shp = new ShpPolylinez( filepath, files );
          shp.setYYMMDD( info.date );
          success &= shp.writeShots( shots, splays );
        }

        // if ( TDSetting.mKmlSplays ) {
        //   nr = 0;
        //   for ( TDNum num : nums ) {
        //     String filepath = filename + "-splays-" + nr;
        //     ++ nr;
        //     List< NumSplay > splays = num.getSplays();
        //     // Log.v("DistoX", "SHP export " + filepath + " splays " + splays.size() );
        //     ShpPolylinez shp = new ShpPolylinez( filepath, files );
        //     shp.setYYMMDD( info.date );
        //     shp.writeSplays( splays );
        //   }
        // }

        Archiver zipper = new Archiver( );
        zipper.compressFiles( filename + ".shz", files );
        TDUtil.deleteDir( filename ); // delete temporary shapedir
      }
    } catch ( IOException e ) {
      TDLog.Error( "Failed SHP export: " + e.getMessage() );
      return null;
    }
    return filename;
  }

  // =======================================================================
  // GEO JASON GeoJSON export
  //   NOTE shot flags are ignored

  static int exportSurveyAsJson( long sid, DataHelper data, SurveyInfo info, File file )
  {
    final String name    = "\"name\": ";
    final String type    = "\"type\": ";
    final String item    = "\"item\": ";
    final String geom    = "\"geometry\": ";
    final String coords  = "\"coordinates\": ";
    final String feature = "\"Feature\"";
    // Log.v("DistoX", "export as GeoJSON " + file.getName() );
    List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), 1.0f, true ); // true: ellipsoid altitude
    if ( nums == null || nums.size() == 0 ) {
      TDLog.Error( "Failed GeoJSON export: no geolocalized station");
      return 2;
    }

    // now write the GeoJSON
    try {
      // TDLog.Log( TDLog.LOG_IO, "export GeoJSON " + file.getName() );
      FileWriter fw  = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("const geojsonObject = {\n");
      pw.format("  \"name\": \"%s\",\n", info.name );
      pw.format("  \"created\": \"%s - TopoDroid v %s\",\n",  TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
      pw.format("  %s \"FeatureCollection\",\n", type );
      pw.format("  \"features\": [\n");
      
      for ( TDNum num : nums ) {
        List< NumShot >    shots = num.getShots();
        for ( NumShot sh : shots ) {
          NumStation from = sh.from;
          NumStation to   = sh.to;
          if ( from.has3DCoords() && to.has3DCoords() ) {
            pw.format("    {\n");
            pw.format("      %s %s,\n", type, feature );
            pw.format("      %s \"centerline\",\n", item );
            pw.format("      %s \"%s %s\",\n", name, from.name, to.name );
            pw.format("      %s \"LineString\",\n", geom );
            pw.format(Locale.US, "      %s [ [ %.8f, %.8f, %.1f ], [ %.8f, %.8f, %.1f ] ]\n", coords, from.e, from.s, from.v, to.e, to.s, to.v );
            pw.format("    },\n");
          }
        }
      }
      if ( TDSetting.mKmlSplays ) {
        for ( TDNum num : nums ) {
          List< NumSplay >   splays = num.getSplays();
          for ( NumSplay sp : splays ) {
            NumStation from = sp.from;
            pw.format("    {\n");
            pw.format("      %s %s,\n", type, feature );
            pw.format("      %s \"splay\",\n", item );
            pw.format("      %s \"%s\",\n", name, from.name );
            pw.format("      %s \"LineString\",\n", geom );
            pw.format(Locale.US, "     %s [ [ %.8f, %.8f, %.1f ], [ %.8f, %.8f, %.1f ] ]\n", coords, from.e, from.s, from.v, sp.e, sp.s, sp.v );
            pw.format("    },\n");
          }
        }
      }
      if ( TDSetting.mKmlStations ) {
        for ( TDNum num : nums ) {
          List< NumStation > stations = num.getStations();
          for ( NumStation st : stations ) {
            pw.format("    {\n");
            pw.format("      %s %s,\n", type, feature );
            pw.format("      %s \"station\",\n", item );
            pw.format("      %s \"%s\",\n", name, st.name );
            pw.format("      %s \"Point\",\n", geom );
            pw.format(Locale.US, "      %s [ %.8f %.8f %.1f ]\n", coords, st.e, st.s, st.v );
            pw.format("    },\n");
          }
        }
      }
      pw.format("    { }\n"); // add a null feature
      pw.format("  ]\n");     // close features array
      pw.format("};\n");      // close geojson object
      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed GeoJSON export: " + e.getMessage() );
      return 0;
    }
  }

  // -------------------------------------------------------------------
  // TRACK FILE OZIEXPLORER
  //   NOTE shot flags are ignored

  static int exportSurveyAsPlt( long sid, DataHelper data, SurveyInfo info, File file )
  {
    // Log.v("DistoX", "export as trackfile: " + file.getName() );
    List< TDNum > nums = getGeolocalizedData( sid, data, info.getDeclination(), TDUtil.M2FT, false );
    if ( nums == null || nums.size() == 0 ) {
      TDLog.Error( "Failed PLT export: no geolocalized station");
      return 2;
    }

    // now write the PLT file
    try {
      // TDLog.Log( TDLog.LOG_IO, "export trackfile " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

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

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed PLT export: " + e.getMessage() );
      return 0;
    }
  }

  // =======================================================================
  // POCKETTOPO EXPORT PocketTopo
  //   NOTE shot flags are ignored

  static int exportSurveyAsTop( long sid, DataHelper data, SurveyInfo info, DrawingWindow sketch, String origin, File file )
  {
    // Log.v("DistoX", "export as pockettopo: " + file.getName() );
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
      TDLog.Error( "export survey as TOP date parse error " + info.date );
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
      FileOutputStream fos = new FileOutputStream( file );
      ptfile.write( fos );
      fos.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed PocketTopo export: " + e.getMessage() );
      return 0;
    }
  }
  // =======================================================================
  // THERION EXPORT Therion
  //   NOTE handled flags: duplicate surface

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
      File plot_file = new File( TDPath.getSurveyPlotTh2File( info.name, plt.name ) );
      if ( plot_file.exists() ) {
        if ( TDSetting.mTherionConfig ) {
          pw.format("  input \"../th2/%s-%s.th2\"\n", info.name, plt.name );
        } else {
          pw.format("  # input \"%s-%s.th2\"\n", info.name, plt.name );
        }
      }
    }
    pw.format("\n");
    for ( PlotInfo plt : plots ) {
      if ( PlotInfo.isSketch2D( plt.type ) ) {
        int scrap_nr = plt.maxscrap;
        // Log.v("DistoX-EXP", plt.name + " is 2D sketch - scraps " + scrap_nr );
        File plot_file = new File( TDPath.getSurveyPlotTh2File( info.name, plt.name ) );
        if ( plot_file.exists() ) {
          pw.format("  # map m%s -projection %s\n", plt.name, PlotInfo.projName[ plt.type ] );
          pw.format("  #   %s-%s\n", info.name, plt.name );
          for ( int k=1; k<=scrap_nr; ++k) {
            pw.format("  #   %s-%s%d\n", info.name, plt.name, scrap_nr );
          }
          pw.format("  # endmap\n");
        } 
      }
    }
    pw.format("\n");
  }

  static private void writeThStations( PrintWriter pw, String from, String to, boolean cmtd )
  {
    if ( cmtd ) {
      pw.format("#   %s %s ", from, to );
    } else {
      pw.format("    %s %s ", from, to );
    }
  }

  static int exportSurveyAsTh( long sid, DataHelper data, SurveyInfo info, File file )
  {
    if ( TDSetting.mTherionConfig ) { // craete thconfig
      synchronized( TDPath.mFilesLock ) {
        File dir = new File( TDPath.getThconfigDir() );
        if ( ! dir.exists() ) dir.mkdirs();
        try {
          FileWriter fcw = new FileWriter( TDPath.getSurveyThConfigFile( info.name ) );
          BufferedWriter bcw = new BufferedWriter( fcw );
          PrintWriter pcw = new PrintWriter( bcw );
          pcw.format("# %s created by TopoDroid v %s\n\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
          pcw.format("source \"../th/%s.th\"\n\n", info.name );
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
          // fcw.flush();
          fcw.close();
        } catch ( IOException e ) {
          TDLog.Error( "Failed Therion config export: " + e.getMessage() );
        }
      }
    }

    // Log.v("DistoX", "export as therion: " + file.getName() );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    final String extend_auto = "    # extend auto\n";

    String uls = ( ul < 1.01f )? "meters"  : "feet"; // FIXME
    String uas = ( ua < 1.01f )? "degrees" : "grads";
    // String uls = TDSetting.mUnitLengthStr;
    // String uas = TDSetting.mUnitAngleStr;

    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    List< DBlock > clist = data.selectAllExportShots( sid, TDStatus.CHECK );
    checkShotsClino( list );

    List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    List< PlotInfo > plots  = data.selectAllPlots( sid, TDStatus.NORMAL );
    List< CurrentStation > stations = data.getStations( sid );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export Therion " + file.getName() );
      FileWriter fw = new FileWriter( file );
      BufferedWriter bw = new BufferedWriter( fw );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("# %s created by TopoDroid v %s\n\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );
      String title = info.name.replaceAll("_", " ");
      pw.format("survey %s -title \"%s\"\n", info.name, title );
      if ( info.comment != null && info.comment.length() > 0 ) {
        pw.format("    # %s \n", info.comment );
      }
      pw.format("\n");

      if ( clist.size() > 0 ) {
        pw.format("# calibration-check\n");
        for ( DBlock blk : clist ) { // calib-check shots
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
        if ( TDSetting.mTherionConfig ) { 
          String[] names = info.team.replaceAll(",", " ").replaceAll(";", " ").replaceAll("\\s+", " ").split(" ");
          int len = names.length;
          int k = 0;
          while ( k<len ) {
            pw.format("    team \"");
            String name = names[k];
            int kk = k;
            while ( k < len-1 && ( name.length() == 1 || name.endsWith(".") ) ) {
              pw.format("%s", name );
              if ( name.length() == 1 ) pw.format(".");
              ++k;
              name = names[k];
            } 
            if ( k > kk ) {
              pw.format(" %s\"\n", name );
            } else {
              pw.format("%s\"\n", name );
            }
            ++k;
          }
        } else {
          pw.format("    # team %s \n", info.team );
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
        for ( CurrentStation station : stations ) {
          if ( station.mFlag == CurrentStation.STATION_FIXED ) { 
            sb_fixed.append(" ");
            sb_fixed.append( station.mName );
          } else if ( station.mFlag == CurrentStation.STATION_PAINTED ) {
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
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
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
            if ( item.getIntExtend() > 1 ) {
	          if ( splay_extend ) {
                pw.format( extend_auto );
	            splay_extend = false;
	          }
	        } else if ( item.getIntExtend() != extend || ! splay_extend ) {
              extend = item.getIntExtend();
              pw.format("    extend %s\n", therion_extend[1+extend] );
	          splay_extend = true;
            }
            writeThStations( pw, ( item.isXSplay() ? "-" : "." ), to, item.isCommented() );
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
            if ( item.getIntExtend() > 1 ) {
	          if ( splay_extend ) {
                pw.format( extend_auto );
	            splay_extend = false;
	          }
	        } else if ( item.getIntExtend() != extend || ! splay_extend ) {
              extend = item.getIntExtend();
              pw.format("    extend %s\n", therion_extend[1+extend] );
	          splay_extend = true;
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
            if ( item.getIntExtend() != extend || ! splay_extend ) {
              extend = item.getIntExtend();
              pw.format("    extend %s\n", therion_extend[1+extend] );
	          splay_extend = true;
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
      // fw.flush();
      fw.close();

      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Therion export: " + e.getMessage() );
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

  static private void writeSurvexLine( PrintWriter pw, String str )
  {
    pw.format("%s%s", str, TDSetting.mSurvexEol );
  }

  static private void writeSurvexEOL( PrintWriter pw )
  {
    pw.format("%s", TDSetting.mSurvexEol );
  }

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

  static int exportSurveyAsSvx( long sid, DataHelper data, SurveyInfo info, Device device, File file )
  {
    // Log.v("DistoX", "export as survex: " + file.getName() );

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
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

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
      pw.format("  *team \"%s\" ", info.team ); writeSurvexEOL(pw);
      writeSurvexLine(pw, "  *units tape " + uls );
      writeSurvexLine(pw, "  *units compass " + uas );
      writeSurvexLine(pw, "  *units clino " + uas );
      if ( info.hasDeclination() ) { // DECLINATION in Survex
        pw.format(Locale.US, "  *declination %.2f", info.declination ); // units DEGREES
        writeSurvexEOL(pw);
      // } else {
      //   pw.format(Locale.US, "  *calibrate declination auto" );
      //   writeSurvexEOL(pw);
      }
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
          if ( from == null || from.length() == 0 ) {
            if ( to == null || to.length() == 0 ) { // no station: not exported
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
            if ( to == null || to.length() == 0 ) { // splay shot
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
          TDNum num = new TDNum( list, from, null, null, 0.0f, null ); // no declination, null formatClosure
          List< NumBranch > branches = num.makeBranches( true );
          // Log.v("DistoX", "Station " + from + " shots " + num.shotsNr() + " splays " + num.splaysNr()
          //               + " branches " + branches.size() );

          for ( NumBranch branch : branches ) {
            // ArrayList< String > stations = new ArrayList<>();
            ArrayList< NumShot > shots = branch.shots;
            int size = shots.size();
            // Log.v("DistoX", "branch shots " + size );
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
        //   for ( DBlock blk : st_blk ) {
        //     writeSurvexLRUD( pw, blk.mFrom, computeLRUD( blk, list, true ), ul );
        //   }
        // }
      }
      pw.format("*end %s", info.name ); writeSurvexEOL(pw);
      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Survex export: " + e.getMessage() );
      return 0;
    }
  }

  // -----------------------------------------------------------------------
  /** CSV COMMA-SEPARATED VALUES EXPORT 
   *  shot flags are used
   *  NOTE declination exported in comment only in CSV
   *       handled flags: duplicate surface commented 
   */
  static private void writeCsvLeg( PrintWriter pw, AverageLeg leg, float ul, float ua, char sep )
  {
    pw.format(Locale.US, "%c%.2f%c%.1f%c%.1f", sep, leg.length() * ul, sep, leg.bearing() * ua, sep, leg.clino() * ua );
    leg.reset();
  }

  static private void writeCsvFlag( PrintWriter pw, boolean dup, boolean sur, boolean cmtd, char sep, String newline )
  {
    if ( dup ) {
      if ( sur ) {
        if ( cmtd ) {
          pw.format("%cLSC%s", sep, newline );
        } else {
          pw.format("%cLS%s", sep, newline );
        }
      } else {
        if ( cmtd ) {
          pw.format("%cLC%s", sep, newline );
        } else {
          pw.format("%cL%s", sep, newline );
        }
      }
    } else {
      if ( sur ) {
        if ( cmtd ) {
          pw.format("%cSC%s", sep, newline );
        } else {
          pw.format("%cS%s", sep, newline );
        }
      } else {
        if ( cmtd ) {
          pw.format("%cC%s", sep, newline );
        } else {
          pw.format("%c%s", sep, newline );
        }
      }
    }
  }

  static int exportSurveyAsRawCsv( long sid, DataHelper data, SurveyInfo info, File file )
  {
    List< RawDBlock > list = data.selectAllShotsRawData( sid );
    char sep = TDSetting.mCsvSeparator;
    String newline = TDSetting.mSurvexEol;
    try {
      FileWriter  fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );
      pw.format("# %s [*] created by TopoDroid v %s%s", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string(), newline );
      pw.format("# %s%s", info.name, newline );
      for ( RawDBlock b : list ) {
	// String f = ( b.mFrom == null )? "" : b.mFrom;
	// String t = ( b.mTo   == null )? "" : b.mTo;
        pw.format(Locale.US, "%d%c%s%c%s%c", b.mId, sep, b.mFrom, sep, b.mTo, sep );
        pw.format(Locale.US, "%.3f%c%.2f%c%.2f%c%.2f%c%.2f%c%.2f%c%.2f%c",
	  b.mLength, sep, b.mBearing, sep, b.mClino, sep, b.mRoll, sep, b.mAcceleration, sep, b.mMagnetic, sep, b.mDip, sep );
        String address = b.mAddress;
        if ( address == null || address.length() == 0 ) address = "-";
        pw.format(Locale.US, "%d%c%d%c%s%c", b.mTime, sep, b.mShotType, sep, address, sep );
        pw.format(Locale.US, "%d%c%d%c%d%c%d%c%s%s", b.mExtend, sep, b.mFlag, sep, b.mLeg, sep, b.mStatus, sep, b.mComment, newline );
      }
      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed CSV export: " + e.getMessage() );
      return 0;
    }
  }

  static int exportSurveyAsCsv( long sid, DataHelper data, SurveyInfo info, File file )
  {
    char sep = TDSetting.mCsvSeparator;
    String newline = TDSetting.mSurvexEol;
    // Log.v("DistoX", "export as CSV: " + file.getName() );
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    // List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    String uls = ( ul < 1.01f )? "meters"  : "feet"; // FIXME
    String uas = ( ua < 1.01f )? "degrees" : "grads";
    try {
      // TDLog.Log( TDLog.LOG_IO, "export CSV " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("# %s created by TopoDroid v %s%s", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string(), newline );
      pw.format("# %s%s", info.name, newline );
      // if ( fixed.size() > 0 ) {
      //   pw.format("  ; fix stations as long-lat alt\n");
      //   for ( FixedInfo fix : fixed ) {
      //     pw.format("  ; *fix %s\n", fix.toExportString() );
      //   }
      // }
      if ( info.hasDeclination() ) { // DECLINATION in CSV
        pw.format(Locale.US, "# from to tape compass clino (declination %.4f)%s", info.declination, newline ); 
      } else {
        pw.format(Locale.US, "# from to tape compass clino (declination undefined)%s", newline );
      }
      pw.format(Locale.US, "# units tape %s compass clino %s%s", uls, uas, newline );
      
      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;
      boolean duplicate = false;
      boolean surface   = false;
      boolean splays = false;
      for ( DBlock item : list ) {
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
              writeCsvLeg( pw, leg, ul, ua, sep );
              writeCsvFlag( pw, duplicate, surface, ref_item.isCommented(), sep, newline );
              duplicate = false;
              surface   = false;
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format("  ; %s\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.US, "-%c%s@%s%c%.2f%c%.1f%c%.1f",
                      sep, to, info.name, sep, item.mLength * ul, sep, item.mBearing * ua, sep, item.mClino * ua );
            writeCsvFlag( pw, false, false, item.isCommented(), sep, newline );

            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", item.mComment );
            // }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              writeCsvLeg( pw, leg, ul, ua, sep );
              writeCsvFlag( pw, duplicate, surface, ref_item.isCommented(), sep, newline );
              duplicate = false;
              surface   = false;
              ref_item = null; 
            }
            // if ( ref_item != null && ref_item.mComment != null && ref_item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", ref_item.mComment );
            // }

            if ( ! splays ) {
              splays = true;
            }
            pw.format(Locale.US, "%s@%s%c-%c%.2f%c%.1f%c%.1f",
                      from, info.name, sep, sep, item.mLength * ul, sep, item.mBearing * ua, sep, item.mClino * ua );
            writeCsvFlag( pw, false, false, item.isCommented(), sep, newline );
            // if ( item.mComment != null && item.mComment.length() > 0 ) {
            //   pw.format(",\"%s\"\n", item.mComment );
            // }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              writeCsvLeg( pw, leg, ul, ua, sep );
              writeCsvFlag( pw, duplicate, surface, ref_item.isCommented(), sep, newline );
              duplicate = false;
              surface   = false;
              // n = 0;
            }
            if ( splays ) {
              splays = false;
            }
            ref_item = item;
            if ( item.isDuplicate() ) duplicate = true;
            if ( item.isSurface() ) surface = true;
            pw.format("%s@%s%c%s@%s", from, info.name, sep, to, info.name );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        writeCsvLeg( pw, leg, ul, ua, sep );
        writeCsvFlag( pw, duplicate, surface, ref_item.isCommented(), sep, newline );
        // duplicate = false;
        // surface   = false;
      }
      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed CSV export: " + e.getMessage() );
      return 0;
    }
  }

  // -----------------------------------------------------------------------
  // TOPOLINUX EXPORT 
  // commented flag not supported 

  // public String exportSurveyAsTlx( long sid, DataHelper data, SurveyInfo info, String filename ) // FIXME args
  // {
  //   File dir = new File( TopoDroidApp.APP_TLX_PATH );
  //   if (!dir.exists()) {
  //     dir.mkdirs();
  //   }
  //   String filename = TopoDroidApp.APP_TLX_PATH + info.name + ".tlx";
  //   List< DBlock > list = mData.selectAllExportShots( sid, TDStatus.NORMAL );
  //   checkShotsClino( list );
  //   try {
  //     TDPath.checkPath( filename );
  //     FileWriter fw = new FileWriter( filename );
  //     PrintWriter pw = new PrintWriter( fw );
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
  //     fw.flush();
  //     fw.close();
  //     return filename;
  //   } catch ( IOException e ) {
  //     TDLog.Error( "Failed QTopo export: " + e.getMessage() );
  //     return null;
  //   }
  // }

  // -----------------------------------------------------------------------
  // COMPASS EXPORT DAT
  //   commented flag not supported
  //   surface flag handled as duplicate

  static private LRUDprofile computeLRUDprofile( DBlock b, List< DBlock > list, boolean at_from )
  {
    LRUDprofile lrud = new LRUDprofile( b.mBearing );
    float n0  = TDMath.cosd( b.mBearing );
    float e0  = TDMath.sind( b.mBearing );
    float cc0 = TDMath.cosd( b.mClino );
    float sc0 = TDMath.sind( b.mClino );
    float cb0 = n0;
    float sb0 = e0;
    String station = ( at_from ) ? b.mFrom : b.mTo;
    
    for ( DBlock item : list ) {
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
        lrud.addData( z1, rl, len );
      }
    }
    // Log.v("DistoX", "<" + b.mFrom + "-" + b.mTo + "> at " + station + ": " + lrud.l + " " + lrud.r );
    return lrud;
  }

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
    
    for ( DBlock item : list ) {
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
          if ( z1 > 0.0 ) { if ( z1 > lrud.u ) lrud.u = z1; }
          else            { if ( -z1 > lrud.d ) lrud.d = -z1; }
        } 
	if ( Math.abs( item.mClino ) <= TDSetting.mLRUDhorizontal ) {
          float rl = e1 * n0 - n1 * e0;
          if ( rl > 0.0 ) { if ( rl > lrud.r ) lrud.r = rl; }
          else            { if ( -rl > lrud.l ) lrud.l = -rl; }
	}
      }
    }
    // Log.v("DistoX", "<" + b.mFrom + "-" + b.mTo + "> at " + station + ": " + lrud.l + " " + lrud.r );
    return lrud;
  }

  /** Centerline data are exported in Compass format as follows
   *    SURVEY NAME: survey_name
   *    SURVEY DATE: mm dd yyyy COMMENT: desription
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
    pw.format(Locale.US, "%.2f %.1f %.1f -9.90 -9.90 -9.90 -9.90 #|L#", blk.mLength*TDUtil.M2FT, b, c );

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
    if ( TDSetting.mExportStationsPrefix ) {
      pw.format("%s-%s %s-%s ", prefix, from, prefix, to );
    } else {
      pw.format("%s %s ", from, to );
    }
  }

  static int nextSplayInt( HashMap<String,Integer> splay_station, String name )
  {
    int ret = 0;
    if ( splay_station.containsKey( name ) ) {
      ret = splay_station.get( name ).intValue();
    }
    splay_station.put( name, Integer.valueOf(ret+1) );
    return ret;
  }

  static int exportSurveyAsDat( long sid, DataHelper data, SurveyInfo info, File file )
  {
    // Log.v("DistoX", "export as compass: " + file.getName() + " swap LR " + TDSetting.mSwapLR );
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export Compass " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );
  
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
          TDLog.Error( "export survey as DAT date parse error " + date );
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
      pw.format("FROM TO LENGTH BEARING INC LEFT UP DOWN RIGHT FLAGS COMMENTS\r\n" );
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
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
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
	    if ( TDSetting.mCompassSplays ) {
              // Integer i = splay_station.get( to );
	      int ii = nextSplayInt( splay_stations, to );
	      printSplayToDat( pw, info.name, to, to + "ss" + ii, item, true ); // reverse
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
	    if ( TDSetting.mCompassSplays ) {
              // Integer i = splay_station.get( from );
	      int ii = nextSplayInt( splay_stations, from );
	      printSplayToDat( pw, info.name, from, from + "ss" + ii, item, false ); // not reverse
	    }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeDatFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo );
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
        writeDatFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo );
        printShotToDat( pw, leg, lrud, duplicate, ref_item.mComment );
      }
      pw.format( "\f\r\n" );

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Compass export: " + e.getMessage() );
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

  static int exportSurveyAsTrb( long sid, DataHelper data, SurveyInfo info, File file )
  {
    int trip = 1;
    int code = 1;
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    // Log.v("DistoX", "export as TopoRobot: " + file.getName() + " data " + list.size() );
    char[] line = new char[ TRB_LINE_LENGTH ];
    try {
      // TDLog.Log( TDLog.LOG_IO, "export TopoRobot " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );
  
      // FIXME 
      pw.format("# TopoDroid v %s\r\n", TDVersion.string() );
      pw.format("# %s\r\n", TDUtil.getDateString("MM dd yyyy") );

      //           5 11 15 19 23
      pw.format(Locale.US, "%6d%6d%4d%4d%4d %s\r\n", -6, 1, 1, 1, 1, info.name ); // [-6] cave name

      List< FixedInfo > fixeds = data.selectAllFixed( sid, TDStatus.NORMAL );
      if ( fixeds.size() > 0 ) {
        for ( FixedInfo fixed : fixeds ) {
          // get TR-station from fixed name
          int pos = fixed.name.indexOf('.');
          int st = (pos < 0)? Integer.parseInt( fixed.name ) : Integer.parseInt( fixed.name.substring( pos+1 ) );
          pw.format(Locale.US, "%6d%6d%4d%4d%4d%12.2f%12.2f%12.2f%8d%8d\r\n", -5, 1, 1, 1, 1, fixed.lng, fixed.lat, fixed.alt, 1, st );
          pw.format(Locale.US, "(%5d%6d%4d%4d%4d %s \r\n", -5, 1, 1, 1, 1, fixed.name );
        }
      } else {
        pw.format(Locale.US, "%6d%6d%4d%4d%4d%12.2f%12.2f%12.2f%8d%8d\r\n", -5, 1, 1, 1, 1, 0.0, 0.0, 0.0, 1, 0 );
      }
      pw.format("\r\n" );

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
      pw.format(Locale.US, "%6d%6d%4d%4d%4d %02d/%02d/%02d\r\n", -4, 1, 1, 1, 1, d, m, y );

      if ( info.comment != null ) {                   // [-4, -3]A bla-bla
        pw.format(Locale.US, "%6d%6d%4d%4d%4d %s\r\n", -3, 1, 1, 1, 1, info.comment );
      }

      String team = (info.team != null)? info.team : "";
      if ( team.length() > 26 ) team = team.substring(0,26);
      int auto_declination = (info.hasDeclination()? 0 : 1); // DECLINATION TopoRobot: 0 = provided, 1 = to be calculated
      pw.format(Locale.US, "%6d%6d%4d%4d%4d %02d/%02d/%02d %26s%4d%8.2f%4d%4d\r\n",
        -2, 1, 1, 1, 1, d, m, y, team, auto_declination, info.getDeclination(), 0, 1 ); 

      //           5 11 15 19 23   31   39   47   55   63   71   79
      pw.format(Locale.US, "%6d%6d%4d%4d%4d%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f\r\n",
        -1, 1, 1, 1, 1, 360.0, 360.0, 0.05, 0.5, 0.5, 100.0, 0.0 );

      int max_series = 0;
      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from != null && from.length() > 0 && to != null && to.length() > 0 ) { 
          int srf = getTrbSeries( from );
          int srt = getTrbSeries( to );
          if ( srt > max_series ) max_series = srt;
          if ( srf > max_series ) max_series = srf;
        }
      }
      max_series ++;
    
      int[] nr_st    = new int[ max_series ];
      int[] start_sr = new int[ max_series ];
      int[] start_st = new int[ max_series ];
      int[] end_st   = new int[ max_series ];
      for ( int k=0; k<max_series; ++k ) {
        nr_st[k] = 0;
        start_sr[k] = 0;
        start_st[k] = 0;
        end_st[k] = 0;
      }
     
      int first_sr = -1;
      int first_st = -1;
      int last_sr = -1;
      int last_st = -1;
      int nr_pts = 0;
      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from != null && from.length() > 0 && to != null && to.length() > 0 ) { 
          int srf = getTrbSeries( from );
          int stf = getTrbStation( from );
          int srt = getTrbSeries( to );
          int stt = getTrbStation( to );
          if ( first_sr < 0 || srf != srt ) {
            if ( first_sr < 0 ) { first_sr = srf; first_st = stf; }
            start_sr[srt] = srf;
            start_st[srt] = stf;
            nr_st[srt] = 0;
          }
          nr_st[srt] ++;
          end_st[srt] = stt;
          last_sr = srt;
          last_st = stt;
          ++nr_pts;
        }
      }

      pw.format(Locale.US, "%6d%6d%4d%4d%4d%8d%8d%8d%8d%8d%8d%8d\r\n",
        1, -1, 1, 1, 1, first_sr, first_st, last_sr, last_st, nr_pts, 1, 0 );

      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;
      int series = first_sr;
      // boolean in_splay = false;
      // boolean duplicate = false;
      LRUD lrud;

      for ( DBlock item : list ) {
        String from = item.mFrom;
        String to   = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station: should never happen
            if ( leg.mCnt > 0 && ref_item != null ) {
              // FIXME ???
              // duplicate = false;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          // int s = getTrbSeries( item.mFrom );
          // if ( s != series ) {
          //   series = s;
          //   pw.format("%6d%6d%4d%4d%4d%8d%8d%8d%8d%8d%8d%8d\r\n",
          //     s, -1, 1, 1, 1, start_sr[s], start_st[s], s, end_st[s], nr_st[s], 0, 0 );
          // }
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              int srt = getTrbSeries( ref_item.mTo );
              int stt = getTrbStation( ref_item.mTo );
              lrud = computeLRUD( ref_item, list, true );
              if ( srt != series ) {
                series = srt;
                pw.format(Locale.US, "%6d%6d%4d%4d%4d%8d%8d%8d%8d%8d%8d%8d\r\n",
                  srt, -1, 1, 1, 1, start_sr[srt], start_st[srt], srt, end_st[srt], nr_st[srt], 0, 0 );
                // if ( series == first_sr ) 
                if ( stt != 0 ) {
                  pw.format(Locale.US, "%6d%6d%4d%4d%4d%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f\r\n",
                    srt, 0, 1, 1, 1, 0.0, 0.0, 0.0, lrud.l, lrud.r, lrud.u, lrud.d );
                }
              }
              //           5 11 15 19 23   31   39   47   55   63   71   79
              pw.format(Locale.US, "%6d%6d%4d%4d%4d%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f\r\n",
                srt, stt, 1, 1, trip, leg.length(), leg.bearing(), leg.clino(), 
                lrud.l, lrud.r, lrud.u, lrud.d );
              leg.reset();
              // duplicate = false;
              ref_item = null; 
            }
          } else {
            // if ( leg.mCnt > 0 && ref_item != null ) {
            //   int srt = getTrbSeries( ref_item.mTo );
            //   if ( srt != series ) {
            //     series = srt;
            //     pw.format(Locale.US, "%6d%6d%4d%4d%4d%8d%8d%8d%8d%8d%8d%8d\r\n",
            //       srt, -1, 1, 1, 1, start_sr[srt], start_st[srt], srt, end_st[srt], nr_st[srt], 0, 0 );
            //   }
            // }
            ref_item = item;
            // duplicate = item.isDuplicate() || item.isSurface();
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        int srt = getTrbSeries( ref_item.mTo );
        int stt = getTrbStation( ref_item.mTo );
        lrud = computeLRUD( ref_item, list, true );
        pw.format(Locale.US, "%6d%6d%4d%4d%4d%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f%8.2f\r\n",
                srt, stt, 1, 1, trip, leg.length(), leg.bearing(), leg.clino(), 
                lrud.l, lrud.r, lrud.u, lrud.d );
        leg.reset();
        // duplicate = false;
        ref_item = null; 
      }
      pw.format( "\r\n" );

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed TopoRobot export: " + e.getMessage() );
      return 0;
    }
  }

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
    if ( TDSetting.mExportStationsPrefix ) {
      pw.format("%s-%s %s-%s ", prefix, from, prefix, to );
    } else {
      pw.format("%s %s ", from, to );
    }
  }

  static int exportSurveyAsSur( long sid, DataHelper data, SurveyInfo info, File file )
  {
    // Log.v("DistoX", "export as winkarst: " + file.getName() + " swap LR " + TDSetting.mSwapLR );
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export WinKarst " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );
  
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
          TDLog.Error( "export survey as DAT date parse error " + date );
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
            fixed.name, fixed.lng, fixed.lat, fixed.alt );
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
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeSurFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo, duplicate );
              printShotToSur( pw, leg, lrud, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              lrud = computeLRUD( ref_item, list, true );
              writeSurFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo, duplicate );
              printShotToSur( pw, leg, lrud, ref_item.mComment );
              duplicate = false;
              ref_item = null; 
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              lrud = computeLRUD( ref_item, list, true );
              writeSurFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo, duplicate );
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
        writeSurFromTo( pw, info.name, ref_item.mFrom, ref_item.mTo, duplicate );
        printShotToSur( pw, leg, lrud, ref_item.mComment );
      }
      pw.format( "#END\r\n" );

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed WinKarst export: " + e.getMessage() );
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

  static int exportSurveyAsGtx( long sid, DataHelper data, SurveyInfo info, File file )
  {
    String date = info.date.replace( '.', '-' );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export GHTopo " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<GHTopo>\n");
      pw.format("  <General>\n");
      pw.format("    <Cavite FolderName=\"%s created by TopoDroid v %s\" CoordsSystem=\"\" CoordsSystemEPSG=\"4978\" FolderObservations=\"\"/>\n",
                TDUtil.getDateString("yyyy/MM/dd"), TDVersion.string() );
      pw.format("  </General>\n");

      List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
      checkShotsClino( list );
      TRobot trobot = new TRobot( list );
      // trobot.dump(); // DEBUG

      List< FixedInfo > fixeds = data.selectAllFixed( sid, TDStatus.NORMAL );
      if ( fixeds.size() > 0 ) {
        pw.format("  <Entrances>\n");
        int ce = 0;
        for ( FixedInfo fixed : fixeds ) {
          ++ ce;
          TRobotPoint pt = trobot.getPoint( fixed.name );
          if ( pt != null ) {
            pw.format(Locale.US, "    <Entrance X=\"%.10f\" Y=\"%.10f\" Z=\"%.2f\" ", fixed.lng, fixed.lat, fixed.alt );
            pw.format("Name=\"%s\" Numero=\"%d\" Comments=\"%s\" RefPoint=\"%d\" RefSerie=\"%d\" IdTerrain=\"\" />\n",
                   fixed.name, ce, fixed.comment, pt.mNumber, pt.mSeries.mNumber );
          }
        }
        pw.format("  </Entrances>\n");
      }
      pw.format("  <Networks>\n");
      pw.format("    <Network Name=\"%s\" Type=\"0\" ColorB=\"0\" ColorG=\"0\" ColorR=\"255\" Numero=\"1\" Comments=\"\"/>\n", info.name );
      pw.format("  </Networks>\n");

      pw.format("  <Codes>\n");
      pw.format("    <Code PsiL=\"0.05\" PsiP=\"1.0\" PsiAz=\"1.0\" Numero=\"1\" Comments=\"\" ");
      pw.format("FactLong=\"1\" ClinoUnit=\"360\" AngleLimite=\"100\" CompassUnit=\"360\" ");
      pw.format("FuncCorrAzCo=\"0\" FuncCorrIncCo=\"0\" FuncCorrAzErrMax=\"0\" FuncCorrIncErrMax=\"0\" ");
      pw.format("FuncCorrAzPosErrMax=\"0\" FuncCorrIncPosErrMax=\"0\" Type=\"0\"/>\n");
      pw.format("  </Codes>\n");

      pw.format("  <Seances>\n");
      pw.format("    <Trip Date=\"%s\" Color=\"%d\" Numero=\"1\" Comments=\"%s\" ",
        date, randomColor(), info.comment );
      pw.format("Surveyor1=\"%s\" Surveyor2=\"\" Declination=\"0\" Inclination=\"0\" ModeDeclination=\"0\" />\n",
        info.team );
      pw.format("  </Seances>\n");

      pw.format("  <Series>\n");
      for ( TRobotSeries series : trobot.mSeries ) {
        TRobotPoint dep = series.mBegin;
        TRobotPoint arr = series.mEnd;
        pw.format("    <Serie Name=\"\" Color=\"#000000\" PtArr=\"%d\" PtDep=\"%d\" Chance=\"0\" Numero=\"%d\" ",
                 arr.mNumber, dep.mNumber, series.mNumber );
        pw.format("SerArr=\"%d\" SerDep=\"%d\" Network=\"1\" Raideur=\"1\" Entrance=\"0\" Obstacle=\"0\" ",
                 arr.mSeries.mNumber, dep.mSeries.mNumber );
        pw.format("Comments=\"\">\n");
        pw.format("      <Stations>\n");
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
            float up    = 0;
            float left  = 0;
            float down  = 0;
            float right = 0;
            pw.format(Locale.US, "        <Shot Az=\"%.2f\" ID=\"%s\" Up=\"%.2f\" Code=\"1\" Down=\"%.2f\" Incl=\"%.2f\" ",
              az, pt.mName, up, down, incl );
            pw.format(Locale.US, "Left=\"%.2f\" Trip=\"1\" Label=\"%s\" Right=\"%.2f\" Length=\"%.3f\" ",
              left, blk.Name(), right, len );
            pw.format(Locale.US, "Secteur=\"0\" Comments=\"%s\" TypeShot=\"%d\" />\n",
              blk.mComment, ( blk.isSurface() ? 7 : 0 ) );
          }
          from = pt;
        }
        pw.format("     </Stations>\n");
        pw.format("   </Serie>\n");
      }
      pw.format("  </Series>\n");

      pw.format("  <AntennaShots>\n");
      // for all splays
      int number = 0;
      for ( DBlock blk : list ) {
        if ( ! blk.isSplay() ) continue;
        TRobotPoint pt = trobot.getPoint( blk.mFrom );
        if ( pt == null ) continue;
        ++ number;
        if ( blk.isCommented() ) {
          pw.format(Locale.US, "    <!-- AntennaShot Az=\"%.2f\" Code=\"1\" Incl=\"%.2f\" Trip=\"1\" Label=\"\" PtDep=\"%d\" ",
            blk.mBearing, blk.mClino, pt.mNumber );
          pw.format(Locale.US, "Length=\"%.3f\" Numero=\"%d\" SerDep=\"%d\" Network=\"1\" Secteur=\"1\" Comments=\"%s\" / -->\n",
            blk.mLength, number, pt.mSeries.mNumber, blk.mComment );
        } else {
          // Log.v("DistoX", "TRobot splay " + blk.mFrom + " nr " + number + " Pt " + pt.mSeries.mNumber + "." + pt.mNumber );
          pw.format(Locale.US, "    <AntennaShot Az=\"%.2f\" Code=\"1\" Incl=\"%.2f\" Trip=\"1\" Label=\"\" PtDep=\"%d\" ",
            blk.mBearing, blk.mClino, pt.mNumber );
          pw.format(Locale.US, "Length=\"%.3f\" Numero=\"%d\" SerDep=\"%d\" Network=\"1\" Secteur=\"1\" Comments=\"%s\" />\n",
            blk.mLength, number, pt.mSeries.mNumber, blk.mComment );
        }
      }
      pw.format("  </AntennaShots>\n");
      pw.format("</GHTopo>\n");


      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Walls export: " + e.getMessage() );
      return 0;
    }
  }

  // =======================================================================
  // GROTTOLF EXPORT
  // commented flag supported for legs

  // write RLDU and the cross-section points
  static private void writeGrtProfile( PrintWriter pw, LRUDprofile lrud )
  {
    pw.format(Locale.US, "%.2f %.2f %.2f %.2f\n", lrud.r, lrud.l, lrud.d, lrud.u );
    pw.format(Locale.US, "# %d %.1f\n", lrud.size(), lrud.bearing );
    int size = lrud.size();
    for ( int k = 0; k<size; ++k ) {
      pw.format(Locale.US, "# %.1f %.2f\n", lrud.getClino(k), lrud.getDistance(k) );
    }
  }

  static private void writeGrtLeg( PrintWriter pw, AverageLeg leg, String fr, String to, boolean first,
                                   DBlock item, List< DBlock > list )
  {
    LRUDprofile lrud = null;
    if ( item.isCommented() ) pw.format("; ");
    if ( first ) {
      pw.format(Locale.US, "%s %s 0.000 0.000 0.000 ", fr, fr );
      lrud = computeLRUDprofile( item, list, true );
      writeGrtProfile( pw, lrud );
    }
    pw.format(Locale.US, "%s %s %.1f %.1f %.2f ", fr, to, leg.bearing(), leg.clino(), leg.length() );
    lrud = computeLRUDprofile( item, list, false );
    writeGrtProfile( pw, lrud );
    leg.reset();
  }

  static private void writeGrtFix( PrintWriter pw, String station, List< FixedInfo > fixed )
  {
    if ( fixed.size() > 0 ) {
      for ( FixedInfo fix : fixed ) {
        if ( station.equals( fix.name ) ) {
          pw.format(Locale.US, "%.8f %.8f %.1f\n", fix.lng, fix.lat, fix.asl );
          return;
        }
      }
    }
    pw.format("0.00 0.00 0.00\n");
  }

  static int exportSurveyAsGrt( long sid, DataHelper data, SurveyInfo info, File file )
  {
    try {
      // TDLog.Log( TDLog.LOG_IO, "export Grottolf " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );
  
      pw.format("%s\n", info.name );
      pw.format(";\n");
      pw.format("; %s created by TopoDroid v %s \n", TDUtil.getDateString("yyyy/MM/dd"), TDVersion.string() );
      pw.format(Locale.US, "360.00 360.00 %.2f 1.00\n", info.getDeclination() ); // degrees degrees decl. meters

      List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
      boolean first = true; // first station
      List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
      checkShotsClino( list );
      // int extend = 1;
      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;
      String ref_from = null;
      String ref_to   = null;
      for ( DBlock item : list ) {
        String from    = item.mFrom;
        String to      = item.mTo;
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null &&
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( first ) writeGrtFix( pw, ref_from, fixed );
              writeGrtLeg( pw, leg, ref_from, ref_to, first, ref_item, list );
              first = false;
              ref_item = null; 
              ref_from = null;
              ref_to   = null;
            }
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // finish writing previous leg shot
              if ( first ) writeGrtFix( pw, ref_from, fixed );
              writeGrtLeg( pw, leg, ref_from, ref_to, first, ref_item, list );
              first = false;
              ref_item = null; 
              ref_from = null;
              ref_to   = null;
            }
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( first ) writeGrtFix( pw, ref_from, fixed );
              writeGrtLeg( pw, leg, ref_from, ref_to, first, ref_item, list );
              first = false;
            }
            ref_item = item;
            ref_from = from;
            ref_to   = to;
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        if ( first ) writeGrtFix( pw, ref_from, fixed );
        writeGrtLeg( pw, leg, ref_from, ref_to, first, ref_item, list );
      }

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Walls export: " + e.getMessage() );
      return 0;
    }
  }

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
 
  static int exportSurveyAsSrv( long sid, DataHelper data, SurveyInfo info, File file )
  {
    // Log.v("DistoX", "export as walls: " + file.getName() );
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    String uls = ( ul < 1.01f )? "Meters"  : "Feet"; // FIXME
    String uas = ( ua < 1.01f )? "Degrees" : "Grads";

    try {
      // TDLog.Log( TDLog.LOG_IO, "export Walls " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );
  
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
          TDLog.Error( "export survey as SRV date parse error " + date );
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
          pw.format(Locale.US, " %.0f", fix.asl );
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
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
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
            writeSrvStations( pw, "-", to, item.isCommented() );
            pw.format(Locale.US, "%.2f\t%.1f\t%.1f", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
            writeSrvComment( pw, item.mComment );
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
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
            writeSrvStations( pw, from, "-", item.isCommented() );
            pw.format(Locale.US, "%.2f\t%.1f\t%.1f", item.mLength*ul, item.mBearing*ua, item.mClino*ua );
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

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Walls export: " + e.getMessage() );
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
      if ( item_extend == DBlock.EXTEND_LEFT ) {
        pw.format("#R180%s", eol );
      } else if ( item_extend == DBlock.EXTEND_RIGHT ) {
        pw.format("#R0%s", eol );
      } else if ( item_extend == DBlock.EXTEND_VERT ) {
        pw.format("#PR[0]%s", eol );
      }
      return item_extend;
    }
    return extend;
  }

  static int exportSurveyAsCav( long sid, DataHelper data, SurveyInfo info, File file )
  {
    // Log.v("DistoX", "export as topo: " + file.getName() );
    String eol = TDSetting.mSurvexEol;
    ArrayList< String > ents = null;

    try {
      // TDLog.Log( TDLog.LOG_IO, "export Topo " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

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
          TDLog.Error( "export survey as SRV date parse error " + date );
        }
      }
      pw.format("#survey ^%s%s", info.name, eol );
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
          pw.format(Locale.US, "%.6f\t%.6f\t%.0f%s", fix.lng, fix.lat, fix.asl, eol );
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
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
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
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
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

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Polygon export: " + e.getMessage() );
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
 
  static int exportSurveyAsPlg( long sid, DataHelper data, SurveyInfo info, File file )
  {
    // Log.v("DistoX-POLYGON", "polygon " + file.getName() );
    float ul = 1; // TDSetting.mUnitLength;
    float ua = 1; // TDSetting.mUnitAngle;
    // String uls = ( ul < 1.01f )? "Meters"  : "Feet"; // FIXME
    // String uas = ( ua < 1.01f )? "Degrees" : "Grads";

    try {
      // TDLog.Log( TDLog.LOG_IO, "export Polygon " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

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
      pw.format("Last modi: 0");   printPolygonEOL( pw );
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
          TDLog.Error( "export survey as PLG date parse error " + date );
        }
      }
      // Log.v("DistoX-DATE", "Y " + y + " M " + m + " d " + d + " " + date );
      pw.format("*** Surveys ***");             printPolygonEOL( pw );
      pw.format("Survey name: %s", info.name ); printPolygonEOL( pw );
      pw.format("Survey team:");                printPolygonEOL( pw );
      pw.format("%s", (info.team != null)? info.team : "" ); printPolygonEOL( pw );
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
          pw.format(Locale.US, "%.6f\t%.6f\t%.0f\t0\t0\t0\t0", fix.lng, fix.lat, fix.asl );
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
      // Log.v("DistoX-POLYGON", "size " + size + " list " + list.size() );

      AverageLeg leg = new AverageLeg(0);
      DBlock ref_item = null;

      int extra_cnt = 0;
      boolean in_splay = false;
      // boolean duplicate = false;
      // boolean surface   = false;
      LRUD lrud;

      int nr_data = 0;
      for ( DBlock item : list ) {
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
              lrud = computeLRUD( ref_item, list, true );
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
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              lrud = computeLRUD( ref_item, list, true );
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
              lrud = computeLRUD( ref_item, list, true );
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
        lrud = computeLRUD( ref_item, list, true );
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
                if ( from.equals( d1.to ) ) {
                  printPolygonData( pw, d2 );
                  d2.used = true;
                  break;
                }
                if ( to.equals( d1.to ) ) { // try reversed
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

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed Polygon export: " + e.getMessage() );
      Log.v("DistoX-POLYGON", "Failed export: " + e.getMessage() );
      return 0;
    }
  }


  // -----------------------------------------------------------------------
  // DXF EXPORT 
  // NOTE declination is taken into account in DXF export (used to compute num)
  // NOTE shot flags are not supported

  static int exportSurveyAsDxf( long sid, DataHelper data, SurveyInfo info, TDNum num, File file )
  {
    // Log.v("DistoX", "export as DXF: " + file.getName() );
    // Log.v( TAG, "export SurveyAsDxf " + file.getName() );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export DXF " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter out = new PrintWriter( fw );
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

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed DXF export: " + e.getMessage() );
      return 0;
    }
  }

  // -----------------------------------------------------------------------
  // VISUALTOPO EXPORT 
  // FIXME photos
  // FIXME not sure declination written in the right place
  // shot flags are not supported

  /**
   * @param pw     writer
   * @param item   reference shot
   ( @param list   ...
   * note item is guaranteed not null by the caller
   */
  static private boolean printStartShotToTro( PrintWriter pw, DBlock item, List< DBlock > list )
  {
    if ( item == null ) return false;
    LRUD lrud = computeLRUD( item, list, ! TDSetting.mVTopoLrudAtFrom ); // default: mVTopoLrudAtFrom = false
    String station = TDSetting.mVTopoLrudAtFrom ? item.mTo : item.mFrom;
    pw.format(Locale.US, "%s %s 0.00 0.00 0.00 ", station, station );
    pw.format(Locale.US, "%.2f %.2f %.2f %.2f N I *", lrud.l, lrud.r, lrud.u, lrud.d );
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
    return true;
  }

  static private void printShotToTro( PrintWriter pw, DBlock item, AverageLeg leg, LRUD lrud )
  {
    if ( item == null ) return; // false;
    // Log.v( TAG, "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    pw.format("%s %s ", item.mFrom, item.mTo );
    pw.format(Locale.US, "%.2f %.1f %.1f ", leg.length(), leg.bearing(), leg.clino() );
    leg.reset();
    pw.format(Locale.US, "%.2f %.2f %.2f %.2f N I *", lrud.l, lrud.r, lrud.u, lrud.d );
    // if ( duplicate ) pw.format(" #|L#");
    // if ( surface ) pw.forma(" #|S#");
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
    // return true;
  }

  static private void printSplayToTro( PrintWriter pw, DBlock item, boolean direct )
  {
    if ( ! TDSetting.mVTopoSplays ) return; // false;
    if ( item == null ) return; // false;
    // Log.v( TAG, "shot " + item.mFrom + "-" + item.mTo + " " + l/n + " " + b + " " + c/n );
    if ( direct ) {
      pw.format("%s * ", item.mFrom );
      pw.format(Locale.US, "%.2f %.1f %.1f * * * * N E", item.mLength, item.mBearing, item.mClino );
    } else {
      // float b = item.mBearing + 180; if ( b >= 360 ) b -= 360;
      float b = TDMath.add180( item.mBearing );
      pw.format("%s * ", item.mTo );
      pw.format(Locale.US, "%.2f %.1f %.1f * * * * N E", item.mLength, b, - item.mClino );
    }
    pw.format( (item.isCommented() ? " D" : " M" ) );
    // if ( duplicate ) pw.format(" #|L#");
    // if ( surface ) pw.format(" #|S#");
    if ( item.mComment != null && item.mComment.length() > 0 ) {
      pw.format(" ;%s", item.mComment );
    }
    pw.format("\r\n");
    // return true;
  }

  static int exportSurveyAsTro( long sid, DataHelper data, SurveyInfo info, File file )
  {
    // Log.v("DistoX", "export as visualtopo: " + file.getName() );
    List< DBlock > list = data.selectAllExportShots( sid, TDStatus.NORMAL );
    checkShotsClino( list );
    List< FixedInfo > fixed = data.selectAllFixed( sid, TDStatus.NORMAL );
    try {
      // TDLog.Log( TDLog.LOG_IO, "export VisualTopo " + file.getName() );
      FileWriter fw = new FileWriter( file );
      PrintWriter pw = new PrintWriter( fw );

      StringWriter sw = new StringWriter();
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
        if ( from == null || from.length() == 0 ) {
          if ( to == null || to.length() == 0 ) { // no station: not exported
            if ( ref_item != null && 
               ( item.isSecLeg() || item.isRelativeDistance( ref_item ) ) ) {
              // Log.v( TAG, "data " + item.mLength + " " + item.mBearing + " " + item.mClino );
              leg.add( item.mLength, item.mBearing, item.mClino );
            }
          } else { // only TO station
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list );
              }
	      pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
              printShotToTro( pw, ref_item, leg, lrud );
              // duplicate = false;
              // surface = false;
              ref_item = null; 
            }
	    printSplayToTro( psw, item, false );
          }
        } else { // with FROM station
          if ( to == null || to.length() == 0 ) { // splay shot
            if ( leg.mCnt > 0 && ref_item != null ) { // write pervious leg shot
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list );
              }
	      pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
              printShotToTro( pw, ref_item, leg, lrud );
              // duplicate = false;
              // surface = false;
              ref_item = null; 
            }
	    printSplayToTro( psw, item, true );
          } else {
            if ( leg.mCnt > 0 && ref_item != null ) {
              if ( ! started ) {
                started = printStartShotToTro( pw, ref_item, list );
              }
	      pw.format( sw.toString() );
              sw = new StringWriter();
              psw = new PrintWriter( sw );
              lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
              printShotToTro( pw, ref_item, leg, lrud );
            }
            ref_item = item;
            // duplicate = item.isDuplicate();
            // surface = item.isSurface();
            // Log.v( TAG, "first data " + item.mLength + " " + item.mBearing + " " + item.mClino );
            leg.set( item.mLength, item.mBearing, item.mClino );
          }
        }
      }
      if ( leg.mCnt > 0 && ref_item != null ) {
        if ( ! started ) {
          started = printStartShotToTro( pw, ref_item, list );
        }
	pw.format( sw.toString() );
        lrud = computeLRUD( ref_item, list, TDSetting.mVTopoLrudAtFrom );
        printShotToTro( pw, ref_item, leg, lrud );
      } else {
	pw.format( sw.toString() );
      }

      fw.flush();
      fw.close();
      return 1;
    } catch ( IOException e ) {
      TDLog.Error( "Failed VisualTopo export: " + e.getMessage() );
      return 0;
    }
  }

  // --------------------------------------------------------------------
  // CALIBRATION import/export
  // CCSV

  static String exportCalibAsCsv( long cid, DeviceHelper data, CalibInfo ci, String filename )
  {
    try {
      // TDLog.Log( TDLog.LOG_IO, "export calibration " + filename );
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );

      pw.format("# %s created by TopoDroid v %s\n\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );

      pw.format("# %s\n", ci.name );
      pw.format("# %s\n", ci.date );
      pw.format("# %s\n", ci.device );
      pw.format("# %s\n", ci.comment );
      pw.format("# %d\n", ci.algo );

      List< CalibCBlock > list = data.selectAllGMs( cid, 1, true ); // status 1: all shots, true: negative_grp too
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

  static private String nextLineAtPos( BufferedReader br, int pos ) throws IOException
  {
    String line = br.readLine();
    if ( line == null ) return "";
    if ( line.length() <= pos ) return "";
    return line.substring( pos );
  }

  // calib file
  // line-1 must contain string "TopoDroid"
  // line-2 skipped
  // line-3 contains name at pos 2: must not match any calib name already in the db
  // line-4 contains date at pos 2 format yyyy.mm.dd
  // line-5 containd device MAC at pos 2: must match current device
  // line-6 contains comment starting at pos 2
  // line-7 contains algo at pos 2: 0 unset, 1 linear, 2 non-linear
  // next data lines follow, each with at least 8 entries:
  //   id, gx, gy, gz, mx, my, mz, group
  // data reading ends at end-of-file or at a line with fewer entries
  //
  static int importCalibFromCsv( DeviceHelper data, String filename, String device_name )
  {
    int ret = 0;
    try {
      // TDLog.Log( TDLog.LOG_IO, "import calibration file " + filename );
      TDPath.checkPath( filename );
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
    
      String line = br.readLine();
      if ( line == null || ! line.contains("TopoDroid") ) {
        ret = -1; // NOT TOPODROID CSV
      } else {
        br.readLine(); // skip empty line
        String name = nextLineAtPos( br, 2 );
        if ( data.hasCalibName( name ) ) {
          ret = -2; // CALIB NAME ALREADY EXISTS
        } else {
          String date   = nextLineAtPos( br, 2 );
          if ( date == null || date.length() < 10 ) {
            date = TDUtil.currentDate();
          }
          String device = nextLineAtPos( br, 2 );
          if ( ! device.equals( device_name ) ) {
            ret = -3; // DEVICE MISMATCH
          } else {
            String comment = nextLineAtPos( br, 2 );
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
                // FIXME
                //   (1) replace ' '* with nothing
                //   (2) split on ','
                line = line.replaceAll( " ", "" );
                String[] vals = line.split(",");
                if ( vals.length > 7 ) {
                  // Log.v("DistoX-Calib", vals.length + " <" + vals[1] + "><" + vals[2] + "><" + vals[3] + ">" );
                  try {
                    long gx = Long.parseLong( vals[1] );
                    long gy = Long.parseLong( vals[2] );
                    long gz = Long.parseLong( vals[3] );
                    long mx = Long.parseLong( vals[4] );
                    long my = Long.parseLong( vals[5] );
                    long mz = Long.parseLong( vals[6] );
                    long gid = data.insertGM( cid, gx, gy, gz, mx, my, mz );
                    String grp = vals[7].trim();
                    data.updateGMName( gid, cid, grp );
                  } catch ( NumberFormatException e ) { 
                    TDLog.Error( e.getMessage() );
                  }
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
