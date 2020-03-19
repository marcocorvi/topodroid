/* @file DrawingTunnel.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid drawing: svg export
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.util.Log;

import java.util.Locale;
// import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import android.graphics.RectF;

class DrawingTunnel extends DrawingSvgBase
{

  static final private float FACTOR = DrawingUtil.SCALE_FIX;

  static final private String formatPTxyz  = "    <pt X=\"%.2f\" Y=\"%.2f\" Z=\"%.2f\"/>\n";
  static final private String formatPTxy0  = "    <pt X=\"%.2f\" Y=\"%.2f\" Z=\"0.0\"/>\n";
  static final private String formatPTxy   = "    <pt X=\"%.2f\" Y=\"%.2f\"/>\n";
  static final private String skpathCentreline = "  <skpath from=\"%d\" to=\"%d\" linestyle=\"centreline\">\n";
  static final private String skpathConnective = "  <skpath from=\"%d\" to=\"%d\" linestyle=\"connective\" splined=\"0\">\n";
  static final private String skpathFilled     = "  <skpath from=\"%d\" to=\"%d\" linestyle=\"filled\" splined=\"%d\">\n";
  static final private String skpathLine       = "  <skpath from=\"%d\" to=\"%d\" linestyle=\"%s\" splined=\"%d\">\n";
  static final private String endSkpath    = "  </skpath>\n";
  static final private String pctext       = "      <pctext style=\"\" nodeposxrel=\"%.2f\" nodeposyrel=\"%.2f\">\n";
  static final private String endPctext    = "      </pctext>\n";
  static final private String pathcodes    = "    <pathcodes>\n";
  static final private String endPathcodes = "    </pathcodes>\n";

  float toWorldX( float x ) { return (x-DrawingUtil.CENTER_X); }
  float toWorldY( float y ) { return (y-DrawingUtil.CENTER_Y); }

  void write( BufferedWriter out, SurveyInfo info, TDNum num, /* DrawingUtil util, */ DrawingCommandManager plot, long type )
  {
    RectF bbox = plot.getBoundingBox( );
    float xmin = bbox.left;
    float xmax = bbox.right;
    float ymin = bbox.top;
    float ymax = bbox.bottom;
    int dx = (int)(xmax - xmin);
    int dy = (int)(ymax - ymin);
    if ( dx > 200 ) dx = 200;
    if ( dy > 200 ) dy = 200;
    xmin -= dx;  xmax += dx;
    ymin -= dy;  ymax += dy;
    float xoff = 0; // xmin; // offset
    float yoff = 0; // ymin;
    int width = (int)((xmax - xmin));
    int height = (int)((ymax - ymin));


    int nr = 0;
    HashMap<String, Integer> map = new HashMap< String, Integer >();

    try {
      String user   = "Unknown";
      String survey = info.name;
      String date   = info.date.replaceAll("\\.", "-") + " 12:00:00";

      // header
      StringWriter sw0 = new StringWriter();
      PrintWriter pw0  = new PrintWriter(sw0);
      pw0.format("<?xml version=\"1.0\" encoding=\"us-ascii\"?>\n");
      // pw0.format( "<!-- XML created by TopoDroid v. " + TopoDroidApp.VERSION + " -->\n" );
      pw0.format("<tunnelxml tunnelversion=\"version2019-07-01 %s\" tunnelproject=\"%s\" tunneluser=\"%s\" tunneldate=\"%s\">\n",
        user, survey, user, date );
      pw0.format("<sketch splined=\"0\" locoffsetx=\"%d\" locoffsety=\"%d\" locoffsetz=\"0.0\" realpaperscale=\"1.0\">\n", (int)(-xmin), (int)(-ymin) );
      // pw0.format( "<svg width=\"" + width + "\" height=\"" + height + "\"\n" );
      out.write( sw0.toString() );
      out.flush();
      
      {
        if ( PlotInfo.isSketch2D( type ) ) { // centerline data
          for ( DrawingPath sh : plot.getLegs() ) { // LEGS
            DBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            // if ( blk.mFrom.length() == 0 || blk.mTo.lnegth() == 0 ) continue;
            String from = blk.mFrom; // .replaceAll(".", "_");
            String to   = blk.mTo; // .replaceAll(".", "_");
            NumStation f = num.getStation( blk.mFrom );
            NumStation t = num.getStation( blk.mTo );
            int nf = -1;
            int nt = -1;
            if ( map.containsKey( from ) ) {
              nf = map.get( from ).intValue();
            } else {
              nf = nr++;
              map.put( from, nf );
            }
            if ( map.containsKey( to ) ) {
              nt = map.get( from ).intValue();
            } else {
              nt = nr++;
              map.put( to, nt );
            }
            StringWriter sw4 = new StringWriter();
            PrintWriter pw4  = new PrintWriter(sw4);
	    pw4.format(Locale.US, skpathCentreline, nf, nt );
            pw4.format(           pathcodes );
            pw4.format(Locale.US, "      <cl_stations tail=\"%s.%s\" head=\"%s.%s\"/>\n", survey, from, survey, to );
            pw4.format(Locale.US, pctext, -1.0f, -1.0f );
            pw4.format(Locale.US, endPctext );
            pw4.format(           endPathcodes );
            if ( PlotInfo.isPlan( type ) ) { 
              pw4.format(Locale.US, formatPTxyz, FACTOR*f.e, FACTOR*f.s, FACTOR*f.v );
              pw4.format(Locale.US, formatPTxyz, FACTOR*t.e, FACTOR*t.s, FACTOR*t.v );
            } else { // if ( PlotInfo.isProfile( type )
              pw4.format(Locale.US, formatPTxy0, FACTOR*f.h, FACTOR*f.v );
              pw4.format(Locale.US, formatPTxy0, FACTOR*t.h, FACTOR*t.v );
            }
            pw4.format( endSkpath );
            out.write( sw4.getBuffer().toString() );
            out.flush();
          }

          // if ( TDSetting.mSvgSplays ) { // SPLAYS
          //   for ( DrawingPath sh : plot.getSplays() ) {
          //     DBlock blk = sh.mBlock;
          //     if ( blk == null ) continue;
          //     StringWriter sw41 = new StringWriter();
          //     PrintWriter pw41  = new PrintWriter(sw41);
          //     // write splay
          //     out.write( sw41.getBuffer().toString() );
          //     out.flush();
          //   }
	  // }
        }

        // FIXME xsections is populated but not used
	ArrayList< XSection > xsections = new ArrayList< XSection >();

        // Log.v("DistoXsvg", "XML commands " + plot.getCommands().size() );
        for ( ICanvasCommand cmd : plot.getCommands() ) { // POINTS
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath path = (DrawingPath)cmd;
          if ( path.mType != DrawingPath.DRAWING_PATH_POINT ) continue;
          // if ( path.mType == DrawingPath.DRAWING_PATH_STATION ) ...
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          DrawingPointPath point = (DrawingPointPath)path;
          float xx = toWorldX( xoff+point.cx );
          float yy = toWorldY( yoff+point.cy );
          if ( BrushManager.isPointSection( point.mPointType ) ) {
	    if ( TDSetting.mAutoXSections ) {
              String scrapname = point.getOption("-scrap");
              if ( scrapname != null ) {
                String scrapfile = scrapname + ".tdr";
	        xsections.add( new XSection( scrapfile, xx, yy ) );
              }
	    // } else {
            //   WHAT ??? nothing
            }
          } else {
            String name = toTunnelPointName( BrushManager.getPointThName( point.mPointType ) );
            NumStation st = num.getClosestStation( type, xx/FACTOR, yy/FACTOR ); // st.name st.e, s, v
            if ( st != null ) {
              if ( map.containsKey( st.name ) ) {
                int nf = map.get( st.name ).intValue();
                int nt = nr++;
                pw5.format(Locale.US, skpathConnective, nf, nt );
                pw5.format( pathcodes );
                pw5.format(Locale.US, pctext, -1.0f, -1.0f );
                pw5.format(Locale.US, endPctext );
                pw5.format(Locale.US, "    	<pcsymbol rname=\"%s\"/>\n", name);
                pw5.format( endPathcodes );
                if ( PlotInfo.isPlan( type ) ) { 
                  pw5.format(Locale.US, formatPTxyz, FACTOR*st.e, FACTOR*st.s, FACTOR*st.v );
                } else { // if ( PlotInfo.isProfile( type )
                  pw5.format(Locale.US, formatPTxy0, FACTOR*st.h, FACTOR*st.v );
                }
                pw5.format(Locale.US, formatPTxy, xx, yy );
                pw5.format( endSkpath );
              }
            }
          }
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }

        for ( ICanvasCommand cmd : plot.getCommands() ) { // LINE
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath path = (DrawingPath)cmd;
          if ( path.mType != DrawingPath.DRAWING_PATH_LINE ) continue;
          DrawingLinePath line = (DrawingLinePath)path;
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          int n1 = nr++;
          int n2 = nr++;
          String name = toTunnelLineName( BrushManager.getLineThName( line.mLineType ) );
          pw5.format(Locale.US, skpathLine, n1, n2, name, 0 );
          pw5.format( pathcodes );
          pw5.format(Locale.US, pctext, -1.0f, -1.0f );
          pw5.format(Locale.US, endPctext );
          pw5.format( endPathcodes );
          for ( LinePoint lp = line.mFirst; lp != null; lp = lp.mNext ) {
            pw5.format(Locale.US, formatPTxy, toWorldX(xoff+lp.x), toWorldY(yoff+lp.y) );
          }
          pw5.format( endSkpath );
          printConnective( pw5, n1, num, type, toWorldX(xoff+line.mFirst.x), toWorldY(yoff+line.mFirst.y), map );
          printConnective( pw5, n2, num, type, toWorldX(xoff+line.mLast.x),  toWorldY(yoff+line.mLast.y),  map );
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }

        for ( ICanvasCommand cmd : plot.getCommands() ) { // AREAS
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath path = (DrawingPath)cmd;
          if ( path.mType != DrawingPath.DRAWING_PATH_AREA ) continue;
          DrawingAreaPath area = (DrawingAreaPath)path;
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          int n1 = nr++;
          pw5.format(Locale.US, skpathFilled, n1, n1, 0 );
          for ( LinePoint lp = area.mFirst; lp != null; lp = lp.mNext ) {
            pw5.format(Locale.US, formatPTxy, toWorldX(xoff+lp.x), toWorldY(yoff+lp.y) );
          }
          pw5.format( endSkpath );
          printConnective( pw5, n1, num, type, toWorldX(xoff+area.mFirst.x), toWorldY(yoff+area.mFirst.y), map );
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }

        // xsections
	// for ( XSection xsection : xsections ) { // XSECTIONS
        //   StringWriter sw7 = new StringWriter();
        //   PrintWriter pw7  = new PrintWriter(sw7);
        //   
        //   out.write( sw7.getBuffer().toString() );
        //   out.flush();
	// }

        // stations
        // Log.v("DistoXsvg", "SVG statioons " + plot.getStations().size() );
        // StringWriter sw6 = new StringWriter();
        // PrintWriter pw6  = new PrintWriter(sw6);
        // if ( TDSetting.mAutoStations ) {
        //   for ( DrawingStationName name : plot.getStations() ) { // auto-stations
        //     toSvg( pw6, name, xoff, yoff );
        //   }
        // } else {
        //   for ( DrawingStationPath st_path : plot.getUserStations() ) { // user-chosen
        //     toSvg( pw6, st_path, xoff, yoff );
        //   }
        // }
        // out.write( sw6.getBuffer().toString() );
        // out.flush();
      }

      // if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
      //   out.write("</body>\n</html>\n");
      // }
      out.write("</sketch>\n");
      out.write("</tunnelxml>\n");
      out.flush();
    } catch ( IOException e ) {
      TDLog.Error( "XML io-exception " + e.getMessage() );
    }
  }

  private static void printConnective( PrintWriter pw, int nn, TDNum num, long type, float x, float y, HashMap<String,Integer> map )
  {
    NumStation st = num.getClosestStation( type, x/FACTOR, y/FACTOR ); // st.name st.e, s, v
    if ( st != null ) {
      if ( map.containsKey( st.name ) ) {
        int nt = map.get( st.name ).intValue();
        pw.format(Locale.US, skpathConnective, nn, nt );
        pw.format(Locale.US, formatPTxy, x, y );
        if ( PlotInfo.isPlan( type ) ) { 
          pw.format(Locale.US, formatPTxyz, FACTOR*st.e, FACTOR*st.s, FACTOR*st.v );
        } else { // if ( PlotInfo.isProfile( type )
          pw.format(Locale.US, formatPTxy0, FACTOR*st.h, FACTOR*st.v );
        }
        pw.format( endSkpath );
      }
    }
  }

  private static String toTunnelPointName( String th_name ) 
  {
    if ( th_name.equals("air-draught") ) return "breeze a";
    if ( th_name.equals("archeo") ) return "humact";
    if ( th_name.equals("blocks") ) return "boulders";
    if ( th_name.equals("column") ) return "column";
    if ( th_name.equals("curtain") ) return "curtain";
    if ( th_name.equals("flowstone") ) return "flowstone";
    if ( th_name.equals("gradient") ) return "slope";
    if ( th_name.equals("guano") ) return "guano";
    if ( th_name.equals("helictite") ) return "helictite";
    if ( th_name.equals("mud") ) return "mud";
    if ( th_name.equals("clay") ) return "mud";
    if ( th_name.equals("pebbles") ) return "pebbles";
    if ( th_name.equals("popcorn") ) return "popcorn";
    if ( th_name.equals("sand") ) return "sand";
    if ( th_name.equals("soda-straw") ) return "straws";
    if ( th_name.equals("stalactite") ) return "stalactite";
    if ( th_name.equals("stalagmite") ) return "stalagmite";
    if ( th_name.equals("water-flow") ) return "stream";
    if ( th_name.equals("water") ) return "puddle";
    // if ( th_name.equals("") ) return "bigboulders";
    // if ( th_name.equals("") ) return "flowpara";
    // if ( th_name.equals("") ) return "flowperp";
    // if ( th_name.equals("") ) return "hexmud";
    // if ( th_name.equals("") ) return "neckbould";
    // if ( th_name.equals("") ) return "neckslope";
    // if ( th_name.equals("") ) return "neckstream";
    // if ( th_name.equals("") ) return "slope2";
    // if ( th_name.equals("") ) return "stream2";
    // if ( th_name.equals("") ) return "sump";
 
    return "bedrock";
  }

  private static String toTunnelLineName( String th_name ) 
  {
    if ( th_name.equals("wall") )    return "wall";
    if ( th_name.equals("presumed")) return "estwall";
    if ( th_name.equals("pit") )     return "pitchbound";
    if ( th_name.equals("chimney") ) return "ceilingbound";
    if ( th_name.equals("slope") )   return "slope";
    // return "invisible";
    return "detail";
  }

}

