/* @file DrawingSvg.java
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
package com.topodroid.io.svg;

// import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDUtil;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

import com.topodroid.TDX.DrawingStationUser;
import com.topodroid.TDX.DrawingStationName;
import com.topodroid.TDX.DrawingPointPath;
import com.topodroid.TDX.DrawingLinePath;
import com.topodroid.TDX.DrawingAreaPath;
import com.topodroid.TDX.DrawingPath;
import com.topodroid.TDX.DrawingUtil;
import com.topodroid.TDX.DrawingCommandManager;
import com.topodroid.TDX.IDrawingLink;
import com.topodroid.TDX.BrushManager;
// import com.topodroid.TDX.SymbolPoint;
import com.topodroid.TDX.Scrap;
// import com.topodroid.TDX.SurveyInfo;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.DBlock;

import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import android.graphics.RectF;
import java.util.HashMap;

public class DrawingSvg extends DrawingSvgBase
{

  public void writeSvg( String filename, BufferedWriter out, TDNum num, /* DrawingUtil util, */ DrawingCommandManager plot, long type )
  {
    // String wall_group = BrushManager.getLineWallGroup( );

    // int handle = 0;
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
    int width  = (int)(xmax - xmin);
    int height = (int)(ymax - ymin);
    float xoff = - xmin; // offset
    float yoff = - ymin;

    final ArrayList< DrawingStationUser > stations = new ArrayList<>();
    final ArrayList< DrawingPointPath > xsectionsPoints = new ArrayList<>();
    final HashMap< String, ArrayList< DrawingPointPath > > points = new HashMap<>();
    final HashMap< String, ArrayList< DrawingLinePath > > lines  = new HashMap<>();
    final HashMap< String, ArrayList< DrawingAreaPath > > areas  = new HashMap<>();
    final ArrayList< XSection > xsections = new ArrayList<>();

    try {
      // if ( TDSetting.mSvgInHtml ) out.write("<!DOCTYPE html>\n<html>\n<body>\n");

      // header
      out.write( svg_header );
      out.write( " width=\"" + (width*TDSetting.mToSvg) + "px\" height=\"" + (height*TDSetting.mToSvg) + "px\"\n" );
      // out.write( " viewBox=\"0 0 " + width + " " + height + "\"\n" );
      // out.write( "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n"); // already in the svg_header
      // out.write( "   xmlns=\"http://www.w3.org/2000/svg\");
      out.write( "  version=\"1.1\"\n" );
      out.write( "  id=\"svg46\"\n" );
      out.write( "  sodipodi:docname=\"" + filename + "\"\n" );
      out.write( "  inkscape:version=\"0.92.4 (5da689c313, 2019-01-14)\">\n" );
      out.write( "<!-- SVG created by TopoDroid v. " + TDVersion.string() + " -->\n" );
      out.write( sodipodi );

      out.write( "  <defs>\n");
      out.write( "    <marker id=\"Triangle\" viewBox=\"0 0 10 10\" refX=\"0\" refY=\"5\" \n");
      out.write( "      markerUnits=\"strokeWidth\" markerWidth=\"4\" markerHeight=\"3\" orient=\"auto\" >\n");
      out.write( "      <path d=\"M 0 0 L 10 5 L 0 10 z\" />\n");
      out.write( "    </marker>\n"); 
      out.write( "  </defs>\n");

      // out.write( "<g id=\"canvas\" transform=\"translate(" + (int)(-xmin) + "," + (int)(-ymin) + ")\" >\n" );

      // ***** FIXME TODO POINT SYMBOLS
      // {
      //   // // 8 layer (0), 2 block name,
      //   for ( int n = 0; n < BrushManager.mPointLib.size(); ++ n ) {
      //     SymbolPoint pt = (SymbolPoint)BrushManager.getPointByIndex(n);

      //     int block = 1+n; // block_name = 1 + therion_code
      //     writeString( out, 8, "POINT" );
      //     writeComment( out, pt.mName );
      //     writeInt( out, 2, block );
      //     writeInt( out, 70, 64 );
      //     writeString( out, 10, "0.0" );
      //     writeString( out, 20, "0.0" );
      //     writeString( out, 30, "0.0" );

      //     out.write( pt.getDxf() );
      //   }
      // }

      if ( TDSetting.mSvgGrid ) {
        writeGrid( out, plot, xoff, yoff, xmin, ymin, xmax, ymax );
      }

      // centerline data
      if ( PlotType.isSketch2D( type ) ) {

        // FIXME OK PROFILE

        // TDLog.v( "SVG legs " + plot.getLegs().size() );
        out.write("<g id=\"legs\" style=\"fill:none;stroke-opacity:0.6;stroke:red\" >\n");
        for ( DrawingPath sh : plot.getLegs() ) {
          DBlock blk = sh.mBlock;
          if ( blk == null ) continue;

          StringWriter sw4 = new StringWriter();
          PrintWriter pw4  = new PrintWriter(sw4);
          pw4.format(Locale.US, "  <path stroke-width=\"%.2f\" stroke=\"black\" d=\"", TDSetting.mSvgShotStroke );
          // // if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
          //   NumStation f = num.getStation( blk.mFrom );
          //   NumStation t = num.getStation( blk.mTo );

          //   if ( type == PlotType.PLOT_PLAN ) {
          //     float x  = xoff + DrawingUtil.toSceneX( f.e, f.s );
          //     float y  = yoff + DrawingUtil.toSceneY( f.e, f.s );
          //     float x1 = xoff + DrawingUtil.toSceneX( t.e, t.s );
          //     float y1 = yoff + DrawingUtil.toSceneY( t.e, t.s );
          //     pw4.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", x, y, x1, y1 );
          //   } else if ( PlotType.isProfile( type ) ) { // FIXME OK PROFILE
          //     float x  = xoff + DrawingUtil.toSceneX( f.h, f.v );
          //     float y  = yoff + DrawingUtil.toSceneY( f.h, f.v );
          //     float x1 = xoff + DrawingUtil.toSceneX( t.h, t.v );
          //     float y1 = yoff + DrawingUtil.toSceneY( t.h, t.v );
          //     pw4.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", x, y, x1, y1 );
          //   }
          // // }
          printSegmentWithClose( pw4, xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );
          pw4.format("\n");
          out.write( sw4.getBuffer().toString() );
          out.flush();
        }
        out.write( end_grp ); // legs

        // TDLog.v( "SVG splays " + plot.getSplays().size() );
        if ( TDSetting.mSvgSplays ) {
          ArrayList< DrawingPath > normal = new ArrayList<>();
          if ( TDSetting.mSplayClasses ) {
            // split splays in classes
            ArrayList< DrawingPath > horiz  = new ArrayList<>();
            ArrayList< DrawingPath > vert   = new ArrayList<>();
            ArrayList< DrawingPath > x_sect = new ArrayList<>();
            for ( DrawingPath sh : plot.getSplays() ) {
              DBlock blk = sh.mBlock;
              if ( blk == null ) continue;
              if ( blk.isHSplay() ) {
                horiz.add( sh );
              } else if ( blk.isVSplay() ) {
                vert.add( sh );
              } else if ( blk.isXSplay() ) {
                x_sect.add( sh );
              } else {
                normal.add( sh );
              }
            }
            writeSplays( out, normal, "splays",   "grey",           xoff, yoff );
            writeSplays( out, horiz,  "h-splays", "lightseagreen",  xoff, yoff );
            writeSplays( out, vert,   "v-splays", "lightsteelblue", xoff, yoff );
            writeSplays( out, x_sect, "x-splays", "lightseablue",   xoff, yoff );
          } else {
            for ( DrawingPath sh : plot.getSplays() ) {
              DBlock blk = sh.mBlock;
              if ( blk == null ) continue;
              normal.add( sh );
            }
            TDLog.v("SVG splays " + normal.size() );
            writeSplays( out, normal, "splays", "grey", xoff, yoff );
          }
        }
      }

      if ( TDSetting.mSvgLineDirection ) {
        // TDLog.v( "SVG line direction");
        StringWriter swD = new StringWriter();
        PrintWriter pwD  = new PrintWriter(swD);
        pwD.format("<marker id=\"dir\" viewBox=\"0 0 10 30\"  orient=\"auto\"");
        pwD.format(" markerUnits=\"strokeWidth\" markerWidth=\"4\" refX=\"0\" refY=\"30\"");
        pwD.format(Locale.US, " markerHeight=\"30\" stroke=\"#cccc3a\" stroke-width=\"%.2f\" fill=\"none\" >\n", TDSetting.mSvgLineDirStroke );
        pwD.format("  <line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"30\" />\n" );
        pwD.format("</marker>\n");
        pwD.format("<marker id=\"rev\" viewBox=\"0 0 10 30\"  orient=\"auto\"");
        pwD.format(" markerUnits=\"strokeWidth\" markerWidth=\"4\" refX=\"0\" refY=\"0\"");
        pwD.format(Locale.US, " markerHeight=\"30\" stroke=\"#cccc3a\" stroke-width=\"%.2f\" fill=\"none\" >\n", TDSetting.mSvgLineDirStroke );
        pwD.format("  <line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"30\" />\n" );
        pwD.format("</marker>\n");
        out.write( swD.getBuffer().toString() );
        out.flush();
      }

      // TDLog.v( "SVG scraps " + plot.getScraps().size() );
      for ( Scrap scrap : plot.getScraps() ) {
        final ArrayList< DrawingPath > paths = new ArrayList<>();
        scrap.addCommandsToList( paths );

        xsectionsPoints.clear();
        points.clear();
        lines.clear();
        areas.clear();

        for ( DrawingPath path : paths ) {
          switch ( path.mType ) {
            case DrawingPath.DRAWING_PATH_POINT:
              DrawingPointPath point = (DrawingPointPath)path;
              if ( BrushManager.isPointSection( point.mPointType ) ) {
                  xsectionsPoints.add( point );
              }
              else {
                final String pointTypeName = point.getFullThName();
                if ( ! points.containsKey( pointTypeName ) ) {
                    points.put( pointTypeName, new ArrayList< DrawingPointPath >() );
                }
                points.get( pointTypeName ).add( point );
              }
              break;
            case DrawingPath.DRAWING_PATH_LINE:
              final DrawingLinePath line = (DrawingLinePath)path;
              final String lineTypeName = line.getFullThName();
              if ( ! lines.containsKey( lineTypeName ) ) {
                  lines.put( lineTypeName, new ArrayList< DrawingLinePath >() );
              }
              lines.get( lineTypeName ).add( line );
              break;
            case DrawingPath.DRAWING_PATH_AREA:
              final DrawingAreaPath area = (DrawingAreaPath)path;
              final String areaTypeName = area.getFullThName();
              if ( ! areas.containsKey( areaTypeName ) ) {
                areas.put( areaTypeName, new ArrayList< DrawingAreaPath >() );
              }
              areas.get( areaTypeName ).add( area );
              break;
          }
        }

        final int scrapId = scrap.mScrapIdx;
        out.write( "<g id=\"scrap_" + scrapId + "\">\n" );

        // TDLog.v( "SVG paths " + paths.size() + " points" );
        if ( ! points.isEmpty() ) {
          out.write("<g id=\"points\">\n");
          ArrayList < String > pointTypes = new ArrayList<>( points.keySet() );
          Collections.sort( pointTypes, String.CASE_INSENSITIVE_ORDER );
          Collections.reverse( pointTypes );
          for ( String pointTypeName : pointTypes ) {
            final ArrayList< DrawingPointPath > pointList = points.get(pointTypeName);
            out.write("<g id=\"point_" + pointTypeName + "\">\n");
            final String color_str = pathToColor( pointList.get(0) );
            for (DrawingPointPath point : pointList) {
              final StringWriter sw53 = new StringWriter();
              final PrintWriter pw53  = new PrintWriter(sw53);
              toSvg( pw53, point, color_str, xoff, yoff );
              out.write( sw53.getBuffer().toString() );
            }
            out.write( end_grp ); // point_
          }
          out.write( end_grp ); // points
          out.flush();
        }

        if ( ! lines.isEmpty() ) {
          out.write("<g id=\"lines\">\n");
          ArrayList < String > lineTypes = new ArrayList<>( lines.keySet() );
          Collections.sort( lineTypes, String.CASE_INSENSITIVE_ORDER );
          Collections.reverse( lineTypes );
          for ( String lineTypeName : lineTypes ) {
            out.write("<g id=\"line_" + lineTypeName + "\">\n");
            final ArrayList< DrawingLinePath > lineList = lines.get(lineTypeName);
            final String color_str = pathToColor( lineList.get(0) );
            for (DrawingLinePath line : lineList) {
              final StringWriter sw54 = new StringWriter();
              final PrintWriter pw54  = new PrintWriter(sw54);
              toSvg( pw54, line, color_str, xoff, yoff );
              out.write( sw54.getBuffer().toString() );
            }
            out.write( end_grp ); // line_
          }
          out.write( end_grp ); // lines
          out.flush();
        }

        if ( ! areas.isEmpty() ) {
          out.write("<g id=\"areas\">\n");
          ArrayList < String > areaTypes = new ArrayList<>( areas.keySet() );
          Collections.sort( areaTypes, String.CASE_INSENSITIVE_ORDER );
          Collections.reverse( areaTypes );
          for ( String areaTypeName : areaTypes ) {
            out.write("<g id=\"area_" + areaTypeName + "\">\n");
            final ArrayList< DrawingAreaPath > areaList = areas.get(areaTypeName);
            final String color_str = pathToColor( areaList.get(0) );
            for (DrawingAreaPath area : areaList) {
              final StringWriter sw55 = new StringWriter();
              final PrintWriter pw55  = new PrintWriter(sw55);
              toSvg( pw55, area, color_str, xoff, yoff );
              out.write( sw55.getBuffer().toString() );
            }
            out.write( end_grp ); // area_
          }
          out.write( end_grp ); // areas
          out.flush();
        }

        if ( ! xsectionsPoints.isEmpty() ) {
          out.write("<g id=\"xsections_" + scrapId + "\">\n");
          out.write("<g id=\"xsection_links\">\n");
          for (DrawingPointPath point : xsectionsPoints) {
            final float xx = point.cx;
            final float yy = point.cy;
            if ( TDSetting.mAutoXSections ) {
              // FIXME GET_OPTION
              final String scrapname = TDUtil.replacePrefix( TDInstance.survey, point.getOption( TDString.OPTION_SCRAP ) );
              XSection xsection = null;
              if ( scrapname != null ) {
                final String scrapfile = scrapname + ".tdr";

                // TODO open file survey-xx#.tdr and convert it to svg
                // tdrToSvg( pw5, scrapfile, xx, yy, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y );
                // xsection = new XSection( scrapfile, xx, yy );
                xsection = new XSection( scrapfile, xx-DrawingUtil.CENTER_X, yy-DrawingUtil.CENTER_Y );
                xsections.add( xsection );
              }
              IDrawingLink link = point.mLink; // FIXME Link could be stored in the XSection and written with it
              if ( link != null && xsection != null ) {
                final float x1 = (xoff + xx) * TDSetting.mToSvg;
                final float y1 = (yoff + yy) * TDSetting.mToSvg;
                final float x2 = (xoff + link.getLinkX() ) * TDSetting.mToSvg;
                final float y2 = (yoff + link.getLinkY() ) * TDSetting.mToSvg;
                final StringWriter sw52 = new StringWriter();
                final PrintWriter pw52  = new PrintWriter(sw52);
                pw52.format(Locale.US, "  <line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\"", x1, y1, x2, y2 );
                pw52.format(Locale.US, " class=\"link\" style=\"fill:none;stroke:brown;stroke-width:%.2f\" />\n", TDSetting.mSvgShotStroke );
                out.write( sw52.getBuffer().toString() );
              }
            } else {
              final StringWriter sw52 = new StringWriter();
              final PrintWriter pw52  = new PrintWriter(sw52);
              printPointWithCXCY( pw52, "<circle", xoff+xx, yoff+yy );
              pw52.format(Locale.US, " r=\"%d\" ", RADIUS );
              pw52.format(Locale.US, " style=\"fill:grey;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgLabelStroke );
              out.write( sw52.getBuffer().toString() );
            }
          }
          out.write( end_grp ); // xsection_links
          out.flush();
        }

        if ( ! xsections.isEmpty() ) {
          // TDLog.v( "SVG xsections " + xsections.size() );
          out.write("<g id=\"xsection_scraps_" + scrapId + "\">\n");
          for (XSection xsection : xsections) {
            // TDLog.v( "SVG xsection " + xsection.mFilename + " " + xsection.mX + " " + xsection.mY );
            StringWriter sw7 = new StringWriter();
            PrintWriter pw7 = new PrintWriter(sw7);
            pw7.format("<g id=\"xsection_%s\">\n", xsection.mFilename);
            // tdrToSvg( pw7, xsection.mFilename, xsection.mX, xsection.mY, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y );
            tdrToSvg(pw7, xsection.mFilename, xsection.mX, xsection.mY, xoff, yoff);
            pw7.format(end_grp);
            out.write(sw7.getBuffer().toString());
            out.flush();
          }
          out.write(end_grp); // xsection_scraps_
          out.flush();
        }

        if ( ! xsectionsPoints.isEmpty() ) {
          out.write(end_grp); // xsections_
          out.flush();
        }

        // TDLog.v( "SVG stations " + plot.getStations().size() );
        out.write("<g id=\"stations\">\n");
        if ( TDSetting.mAutoStations ) {
          for ( DrawingStationName name : plot.getStations() ) { // auto-stations
            StringWriter sw61 = new StringWriter();
            PrintWriter pw61  = new PrintWriter(sw61);
            toSvg( pw61, name, xoff, yoff );
            out.write( sw61.getBuffer().toString() );
          }
        } else {
          for (DrawingStationUser st_path : plot.getUserStations()) { // user-chosen
            StringWriter sw62 = new StringWriter();
            PrintWriter pw62 = new PrintWriter(sw62);
            toSvg(pw62, st_path, xoff, yoff);
            out.write(sw62.getBuffer().toString());
            out.flush();
            out.write(end_grp); // stations
          }
        }

        out.write( end_grp ); // scrap_
        out.flush();
      }

      // out.write( end_grp ); // canvas
      out.write( end_svg );

      // if ( TDSetting.mSvgInHtml ) out.write("</body>\n</html>\n");

      out.flush();
    } catch ( IOException e ) {
      TDLog.e( "SVG io-exception " + e.getMessage() );
    }
  }

  private void writeSplays( BufferedWriter out, ArrayList< DrawingPath > splays, String group, String color, float xoff, float yoff )
  {
    if ( splays.size() == 0 ) return;
    try {
      out.write("<g id=\"" + group + "\"\n" );
      out.write("  style=\"fill:none;stroke-opacity:0.4;stroke:" + color + "\" >\n");
      int count = 1;
      for ( DrawingPath sh : splays ) {
        StringWriter sw41x = new StringWriter();
        PrintWriter pw41x  = new PrintWriter(sw41x);
        pw41x.format(Locale.US, "  <path stroke-width=\"%.2f\" stroke=\"%s\" id=\"splay_%s_%d\" d=\"", TDSetting.mSvgShotStroke, color, group, count++ );
        printSegmentWithClose( pw41x, xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );
        pw41x.format("\n");
        out.write( sw41x.getBuffer().toString() );
        out.flush();
      }
      out.write( end_grp );
    } catch ( IOException e ) {
      TDLog.e( "SVG splay-io exception " + e.getMessage() );
    }
  }

}

