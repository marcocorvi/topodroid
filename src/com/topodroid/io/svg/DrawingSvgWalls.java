/* @file DrawingSvgWalls.java
 *
 * @author marco corvi
 * @date oct 2019
 *
 * @brief TopoDroid drawing: svg export for round-trip
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.svg;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;

import com.topodroid.TDX.DrawingStationUser;
import com.topodroid.TDX.DrawingStationName;
import com.topodroid.TDX.DrawingPointPath;
import com.topodroid.TDX.DrawingLinePath;
import com.topodroid.TDX.DrawingAreaPath;
import com.topodroid.TDX.DrawingLabelPath;
import com.topodroid.TDX.DrawingPath;
import com.topodroid.TDX.DrawingUtil;
import com.topodroid.TDX.DrawingCommandManager;
import com.topodroid.TDX.BrushManager;
import com.topodroid.TDX.Symbol;
import com.topodroid.TDX.SymbolPoint;
import com.topodroid.TDX.TDExporter;
import com.topodroid.TDX.Scrap;
import com.topodroid.TDX.DBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import android.graphics.RectF;

public class DrawingSvgWalls extends DrawingSvgBase
{

  /** @return true if a drawing item has a given round-trip index
   * @param path drawing item
   * @param rt   round-trip index
   */
  private boolean isRoundTrip( DrawingPath path, int rt )
  {
    switch ( path.mType ) {
      case DrawingPath.DRAWING_PATH_LINE:
        return BrushManager.isLineRoundTrip( (DrawingLinePath)path, rt );
      case DrawingPath.DRAWING_PATH_POINT:
        return BrushManager.isPointRoundTrip( (DrawingPointPath)path, rt );
      case DrawingPath.DRAWING_PATH_AREA:
        return BrushManager.isAreaRoundTrip( (DrawingAreaPath)path, rt );
    }
    return false;
  }

  /** 
   * @param out     output writer
   * @param path    drawing item
   * @param xoff    X offset
   * @param yoff    Y offset
   * @param rt      round-trip index
   */
  private void writePath( BufferedWriter out, DrawingPath path, float xoff, float yoff, int rt ) throws IOException
  {
    if ( ! isRoundTrip( path, rt ) ) return;
    if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
      DrawingLinePath line = (DrawingLinePath)path;
      // if ( ! BrushManager.isLineRoundTrip( line, rt ) ) return;
      StringWriter sw5w = new StringWriter();
      PrintWriter pw5w  = new PrintWriter(sw5w);
      toSvg( pw5w, line, pathToColor(path), xoff, yoff );
      out.write( sw5w.getBuffer().toString() );
    } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
      DrawingPointPath point = (DrawingPointPath)path;
      // if ( ! BrushManager.isPointRoundTrip( point, rt ) ) return;
      if ( BrushManager.isPointLabel( point.mPointType ) ) return;
      if ( BrushManager.isPointSection( point.mPointType ) ) {
        if ( TDSetting.mAutoStations ) return;
        float xx = xoff+point.cx;
        float yy = yoff+point.cy;
        StringWriter sw5 = new StringWriter();
        PrintWriter pw5  = new PrintWriter(sw5);
        printPointWithCXCY( pw5, "<circle", xx, yy );
        pw5.format(Locale.US, " r=\"%d\" ", RADIUS );
        // pw5.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%d\" ", xx, yy, RADIUS );
        pw5.format(Locale.US, " style=\"fill:grey;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgLabelStroke );
        out.write( sw5.getBuffer().toString() );
      } else {
        StringWriter sw5p = new StringWriter();
        PrintWriter pw5p  = new PrintWriter(sw5p);
        toSvg( pw5p, point, pathToColor(path), xoff, yoff );
        out.write( sw5p.getBuffer().toString() );
      }
    } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
      DrawingAreaPath area = (DrawingAreaPath)path;
      // if ( ! BrushManager.isAreaRoundTrip( area, rt ) ) return;
      StringWriter sw5 = new StringWriter();
      PrintWriter pw5  = new PrintWriter(sw5);
      toSvg( pw5, area, pathToColor(path), xoff, yoff );
      out.write( sw5.getBuffer().toString() );
    }
  }

  private void writeGroupedPaths( BufferedWriter out, HashMap< String, ArrayList< DrawingPath > > paths, String superType, float xoff, float yoff, int rt ) throws IOException
  {
    if ( ! paths.isEmpty() ) {
      out.write("<g id=\"" + superType + "\"" + group_mode_open);
      ArrayList<String> types = orderSymbolTypes(paths.keySet());
      for (String typeName : types) {
        ArrayList<DrawingPath> list = paths.get(typeName);
        out.write("<g id=\"" + superType + "_" + typeName + "\"" + group_mode_open);
        for (DrawingPath path : list) {
          writePath(out, path, xoff, yoff, rt);
        }
        out.write( end_grp ); // superType_
      }
      out.write( end_grp ); // superType
      out.flush();
    }
  }

  /**
   * Writes provided paths to out.
   * @param out  output writer
   * @param paths list of paths to be drawn
   * @param xoff X offset
   * @param yoff Y offset
   * @param rt  round-trip index
   * @throws IOException
   */
  private void writePaths(BufferedWriter out, ArrayList<DrawingPath> paths, float xoff, float yoff, int rt ) throws IOException
  {
    if ( TDSetting.mSvgGroups ) {
      SvgGroupedPaths gps = separatePathsInGroups( paths);
      writeGroupedPaths( out, gps.points, "points", xoff, yoff, rt );
      writeGroupedPaths( out, gps.lines, "lines", xoff, yoff, rt );
      writeGroupedPaths( out, gps.areas, "areas", xoff, yoff, rt );
    } else {
      for ( DrawingPath path : paths ) {
        writePath( out, path, xoff, yoff, rt );
      }
    }
  }

  public void writeSvg( String filename, BufferedWriter out, TDNum num, DrawingCommandManager plot, long type )
  {
    // TDLog.v("SvgWalls write " + filename + " type " + type );
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
    int width  = (int)((xmax - xmin));
    int height = (int)((ymax - ymin));
    float xoff = - xmin; // offset
    float yoff = - ymin;
    // xoff + xmin = 0
    // xoff + xmax = xmax - xmin = width
    // xmin xmax are used only for the grids

    // TDLog.v( "SVG Walls X " + xoff + " " + xmin + " " + xmax + " W " + width + " Y " + yoff + " " + ymin + " " + ymax + " H " + height ); 

    ArrayList< XSection > xsections = getXSections( plot, xoff, yoff );

    try {
      // if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
      //   out.write("<!DOCTYPE html>\n<html>\n<body>\n");
      // }

      // header
      out.write( xml_header ); 
      out.write( walls_header );
      out.write( compass_header );
      out.write( svg_header );
      out.write( " width=\"" + width + "pt\" height=\"" + height + "pt\"\n" );
      // out.write( "  viewBox=\"0 0 " + width + " " + height + "\"\n" );
      // out.write( "  i:pageBounds=\"0 0 " + width + " " + (height) + "\"\n" ); // FIXME i: ???
      // out.write( "  style=\"overflow:visible;enable-background:new 0 0 " + width + " " + height + "\"\n" );
      out.write( "  version=\"1.1\"\n" );
      out.write( "  id=\"svg46\"\n" );
      out.write( "  sodipodi:docname=\"" + filename + "\"\n" );
      out.write( "  inkscape:version=\"0.92.4 (5da689c313, 2019-01-14)\">\n" );
      out.write( "<!-- SVG created by TopoDroid v. " + TDVersion.string() + " -->\n" );
      out.write(    metadata );
      out.write(      rdf );
      out.write(        dc_format );
      out.write(        dc_type );
      out.write(      end_rdf );
      out.write(    end_metadata );
      out.write(    sodipodi );

      writeDefs( out, plot.getPointSymbols(), plot.getLineSymbols(), plot.getAreaSymbols() ); // replaces:

      // out.write( "  <defs id=\"defs\">\n" );
      // out.write( "    <marker id=\"Triangle\" viewBox=\"0 0 10 10\" refX=\"0\" refY=\"5\" \n");
      // out.write( "      markerUnits=\"strokeWidth\" markerWidth=\"4\" markerHeight=\"3\" orient=\"auto\" >\n");
      // out.write( "      <path d=\"M 0 0 L 10 5 L 0 10 z\" />\n");
      // out.write( "    </marker>\n"); 
      // // if ( TDSetting.mSvgLineDirection ) {
      //   // TDLog.v( "SVG line direction");
      // StringWriter swD = new StringWriter();
      // PrintWriter pwD  = new PrintWriter(swD);
      // pwD.format("    <marker id=\"dir\" viewBox=\"0 0 10 30\"  orient=\"auto\"");
      // pwD.format("       markerUnits=\"strokeWidth\" markerWidth=\"4\" refX=\"0\" refY=\"30\"");
      // pwD.format(Locale.US, "      markerHeight=\"30\" stroke=\"#cccc3a\" stroke-width=\"%.2f\" fill=\"none\" >\n", TDSetting.mSvgLineDirStroke );
      // pwD.format("      <line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"30\" />\n" );
      // pwD.format("    </marker>\n");
      // pwD.format("    <marker id=\"rev\" viewBox=\"0 0 10 30\"  orient=\"auto\"");
      // pwD.format("      markerUnits=\"strokeWidth\" markerWidth=\"4\" refX=\"0\" refY=\"0\"");
      // pwD.format(Locale.US, "      markerHeight=\"30\" stroke=\"#cccc3a\" stroke-width=\"%.2f\" fill=\"none\" >\n", TDSetting.mSvgLineDirStroke );
      // pwD.format("      <line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"30\" />\n" );
      // pwD.format("    </marker>\n");
      // out.write( swD.getBuffer().toString() );
      // out.flush();
      // }
      // out.write( "    <g id=\"icons\" " ); out.write( group_mode_open );
      // for ( int n = 0; n < BrushManager.getPointLibSize(); ++ n ) {
      //   SymbolPoint pt = (SymbolPoint) BrushManager.getPointByIndex(n);
      //   if (pt != null) {
      //     // int block = 1 + n; // block_name = 1 + therion_code
      //     out.write("    <marker id=\"" + pt.getThName() + "\">\n");
      //     out.write("      " + pt.getSvg().replace("path", "path inkscape:connector-curvature=\"0\"") + "\n");
      //     out.write("    </marker>\n");
      //   }
      // }
      // out.write( "    </g>\n");
      // out.write( "  </defs>\n");

      // out.write(    clip );
      // out.write( "  <svg width=\"" + width + "\" height=\"" + height + "\"\n" );
      out.write( "  <svg width=\"auto\" height=\"auto\"\n" );
      out.write(      svg_options );

      // out.write( "<g id=\"canvas\" transform=\"translate(" + (int)(-xmin) + "," + (int)(-ymin) + ")\" >\n" );

      out.write( group_bg ); out.write( group_mode_close );
      out.write( group_ref );   out.write( group_mode_close ); 

      // COMPASS TRACING
      out.write( group_tracing ); out.write( group_mode_open ); 

      out.write( group_sketchmap2 ); out.write( group_mode_close );
      out.write( group_sketchmap1 ); out.write( group_mode_close );
      out.write( group_passage );    out.write( group_mode_close );
      out.write( group_lrud );       out.write( group_mode_close );
      out.write( "    " + end_grp ); // group_tracing


      out.write( group_mask ); out.write( group_mode_close );


      out.write( group_detail );     out.write( group_mode_open );
      out.write( group_detail_shp ); out.write( group_mode_open );
      for ( Scrap scrap : plot.getScraps() ) {
        ArrayList<DrawingPath> paths = new ArrayList<>();
        scrap.addCommandsToList( paths );
        out.write( "        <g id=\"scrap_" + scrap.mScrapIdx + "\">\n" );
        writePaths( out, paths, xoff, yoff, Symbol.W2D_DETAIL_SHP );
        out.write( "        " + end_grp ); // scrap_
      }
      out.write( "      " + end_grp ); // group_detail_shp
      out.flush();

      out.write( group_detail_sym ); out.write( group_mode_open );
      for ( Scrap scrap : plot.getScraps() ) {
        ArrayList<DrawingPath> paths = new ArrayList<>();
        scrap.addCommandsToList( paths );
        out.write( "        <g id=\"scrap_" + scrap.mScrapIdx + "\">\n" );
        writePaths( out, paths, xoff, yoff, Symbol.W2D_DETAIL_SYM );
        out.write( "        " + end_grp ); // scrap_
      }
      out.flush();
      if ( TDSetting.mAutoXSections ) {
        for ( XSection xsection : xsections ) {
          StringWriter sw7 = new StringWriter();
          PrintWriter pw7  = new PrintWriter(sw7);
          pw7.format("<g id=\"%s\">\n", xsection.mFilename );
          out.write( sw7.getBuffer().toString() );
          out.flush();
          writeXSectionToSvg( out, xsection.mFilename, xsection.mFilename, xsection.mX, xsection.mY, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y );
          pw7.format("</g>\n");
          out.write( sw7.getBuffer().toString() );
          out.flush();
        }
      }
      out.write( "      " + end_grp ); // group_detail_sym
      out.write( "    " + end_grp ); // group_detail
      out.flush();

      out.write( group_walls );     out.write( group_mode_open );
      out.write( group_walls_shp ); out.write( group_mode_open );
      for ( Scrap scrap : plot.getScraps() ) {
        ArrayList<DrawingPath> paths = new ArrayList<>();
        scrap.addCommandsToList( paths );
        out.write( "        <g id=\"scrap_" + scrap.mScrapIdx + "\">\n" );
        writePaths( out, paths, xoff, yoff, Symbol.W2D_WALLS_SHP );
        out.write( "        " + end_grp ); // scrap_
      }
      out.write( "      " + end_grp ); // group_walls_shp
      out.flush();

      out.write( group_walls_sym ); out.write( group_mode_open );
      for ( Scrap scrap : plot.getScraps() ) {
        ArrayList<DrawingPath> paths = new ArrayList<>();
        scrap.addCommandsToList( paths );
        out.write( "        <g id=\"scrap_" + scrap.mScrapIdx + "\">\n" );
        writePaths( out, paths, xoff, yoff, Symbol.W2D_WALLS_SYM );
        out.write( "        " + end_grp ); // scrap_
      }
      out.write( "      " + end_grp ); // group_walls_sym
      out.write( "    " + end_grp ); // group_walls
      out.flush();

      // SURVEY: VECTORS
      out.write(     group_survey ); out.write( group_mode_open );
      out.write(       group_vectors ); out.write( group_mode_open );
      for ( DrawingPath sh : plot.getLegs() ) {
        DBlock blk = sh.mBlock;
        if ( blk == null ) continue;
        String id = blk.mFrom  + "_" + blk.mTo;
        StringWriter sw4 = new StringWriter();
        PrintWriter pw4  = new PrintWriter(sw4);
        pw4.format(Locale.US, "        <path id=\"%s\" stroke-width=\"%.2f\" stroke=\"black\" d=\"", id, TDSetting.mSvgShotStroke );
        printSegmentWithClose( pw4, xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );
        // pw4.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );
        out.write( sw4.getBuffer().toString() );
        out.flush();
      }
      if ( TDSetting.mSvgSplays && TDSetting.mCompassSplays ) {
        HashMap<String, Integer> splay_station = new HashMap<String,Integer>();
        for ( DrawingPath sh : plot.getSplays() ) {
          DBlock blk = sh.mBlock;
          if ( blk == null ) continue;
          String id = blk.mFrom + "_" + blk.mFrom + "ss" + TDExporter.nextSplayInt( splay_station, blk.mFrom );
          StringWriter sw41 = new StringWriter();
          PrintWriter pw41  = new PrintWriter(sw41);
          pw41.format(Locale.US, "        <path id=\"%s\" stroke-width=\"%.2f\" stroke=\"grey\" d=\"", id, TDSetting.mSvgShotStroke );
          printSegmentWithClose( pw41, xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );
          // pw41.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );

          out.write( sw41.getBuffer().toString() );
          out.flush();
        }
      }
      out.write( "      " + end_grp ); // group_vectors

      // survey:  MARKERS
      out.write(       group_markers ); out.write( group_mode_open ); 
        if ( TDSetting.mAutoStations ) {
          StringWriter sw6m = new StringWriter();
          PrintWriter pw6m  = new PrintWriter(sw6m);
          for ( DrawingStationName st : plot.getStations() ) { // auto-stations
            pw6m.format(Locale.US, "<use xlink:href=\"#_m\" width=\"2\" height=\"2\" x=\"-1\" y=\"-1\"" );
            printMatrix( pw6m, 0.798f, 0.0f, (xoff + st.cx), (yoff + st.cy) );
            pw6m.format(Locale.US, " style=\"display:inline\" />\n" );
          }
          out.write( sw6m.getBuffer().toString() );
        } 
      out.write( "      " + end_grp ); // group_markers
      out.write(       group_flags ); out.write( group_mode_close );

      // TDLog.v("survey: LABELS");
      out.write(       group_labels ); out.write( group_mode_open );
      if ( TDSetting.mAutoStations ) {
        StringWriter sw6s = new StringWriter();
        PrintWriter pw6s  = new PrintWriter(sw6s);
        for ( DrawingStationName name : plot.getStations() ) { // auto-stations
          toSvg( pw6s, name, xoff, yoff );
        }
        out.write( sw6s.getBuffer().toString() );
      } else {
        if ( plot.hasUserStations() ) {
          StringWriter sw7s = new StringWriter();
          PrintWriter pw7s  = new PrintWriter(sw7s);
          for ( DrawingStationUser st_path : plot.getUserStations() ) { // user-chosen
            toSvg( pw7s, st_path, xoff, yoff );
          }
          out.write( sw7s.getBuffer().toString() );
        }
        // for ( ICanvasCommand cmd : plot.getCommands() ) {
        //   if ( cmd.commandType() != 0 ) continue;
        //   DrawingPath path = (DrawingPath)cmd;
        //   if ( path.mType != DrawingPath.DRAWING_PATH_STATION ) continue;
        //   // String color_str = pathToColor( path );
        //   StringWriter sw5s = new StringWriter();
        //   PrintWriter pw5s  = new PrintWriter(sw5s);
        //   toSvg( pw5s, (DrawingStationUser)path, xoff, yoff );
        //   out.write( sw5s.getBuffer().toString() );
        // }
      }
      out.flush();
      // TDLog.v("survey: LABELS stations done");

      for ( Scrap scrap : plot.getScraps() ) {
        ArrayList<DrawingPath> paths = new ArrayList<>();
        scrap.addCommandsToList( paths );
        // TDLog.v("survey: SCRAP " + scrap.mScrapIdx + " paths " + paths.size() );
        out.write( "        <g id=\"scrap_" + scrap.mScrapIdx + "\">\n" );
        for ( DrawingPath path : paths ) {
          if ( path instanceof DrawingLabelPath ) {
            DrawingLabelPath label = (DrawingLabelPath)path;
            StringWriter sw5l = new StringWriter();
            PrintWriter pw5l  = new PrintWriter( sw5l );
            toSvgLabel( pw5l, label, pathToColor( path ), xoff, yoff );
            out.write( sw5l.getBuffer().toString() );
          }
        }
        out.write( "      " + end_grp ); // scrap_
      }
      out.write( "      " + end_grp ); // group_labels
      out.write( "    " + end_grp ); // group_survey
      out.flush();
      // TDLog.v("survey: SURVEY done");

      // NOTES, GRID, LEGEND (SCALEBAR, NORTH), FRAME
      out.write(      group_notes ); out.write( group_mode_close );
      out.write(      group_grids ); out.write( group_mode_open );
      if ( TDSetting.mSvgGrid ) {
        writeGrid( out, plot, xoff, yoff, xmin, ymin, xmax, ymax );
      }
      out.write( "    " + end_grp ); // group_grids

      out.write(     group_legend );     out.write( group_mode_open );
      out.write(       group_scalebar ); out.write( group_mode_close );
      out.write(       group_north );    out.write( group_mode_close );
      out.write( "    " + end_grp ); // group_legend
      out.write(     group_frame );  out.write( group_mode_close );

      out.write( "  " + end_svg );
      out.write( end_svg ); // svg_header

      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TDLog.e( "SVG io-exception " + e.getMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  private static final String xml_header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
  private static final String walls_header = "<?walls updated=\"no\" merged-content=\"no\" adjustable=\"no\"?>\n";
  private static final String compass_header = "<?compass inkscape-compatible=\"yes\"?>\n";
  
  private static final String metadata = "  <metadata id=\"metadata52\">\n";
  private static final String rdf = "    <rdf:RDF><cc:Work rdf:about=\"\">\n";
  private static final String dc_format = "      <dc:format>image/svg+xml</dc:format>\n";
  private static final String dc_type   = "      <dc:type rdf:resource=\"http://purl.org/dc/dcmitype/StillImage\" />\n";
  private static final String end_rdf      = "    </cc:Work></rdf:RDF>\n";
  private static final String end_metadata = "  </metadata>\n";

  // private static final String clip = "<!--Used to clip frame - Not compatible with AI10 - remove if necessary-->";

  private static final String svg_options = " overflow=\"hidden\" version=\"1.1\" id=\"svg44\" style=\"display:inline;overflow:hidden\" >\n";

  private static final String group_bg      = "    <g id=\"w2d_Background\" ";
  private static final String group_ref     = "    <g id=\"w2d_Ref\" ";
  private static final String group_tracing = "    <g id=\"cmp_tracing\" ";
  private static final String group_passage = "      <g id=\"cmp_passage\" ";
  private static final String group_lrud    = "      <g id=\"cmp_LRUDs\" ";
  private static final String group_sketchmap2 = "    <g id=\"cmp_Sketchmap2\" ";
  private static final String group_sketchmap1 = "    <g id=\"cmp_Sketchmap1\" ";
  private static final String group_mask       = "    <g id=\"w2d_Mask\" ";
  private static final String group_detail     = "    <g id=\"w2d_Detail\" ";
  private static final String group_detail_shp = "      <g id=\"w2d_Detail_shp\" ";
  private static final String group_detail_sym = "      <g id=\"w2d_Detail_sym\" ";
  
  private static final String group_walls      = "    <g id=\"w2d_Walls\" ";
  private static final String group_walls_shp  = "      <g id=\"w2d_Walls_shp\" ";
  private static final String group_walls_sym  = "      <g id=\"w2d_Walls_sym\" ";
  private static final String group_survey     = "    <g id=\"w2d_Survey\" ";
  private static final String group_vectors    = "      <g id=\"w2d_Vectors\" ";
  private static final String group_markers    = "      <g id=\"w2d_Markers\" ";
  private static final String group_flags      = "      <g id=\"w2d_Flags\"  style=\"display:none\" ";
  private static final String group_labels     = "      <g id=\"w2d_Labels\" style=\"display:inline\" ";

  private static final String group_notes     = "    <g id=\"w2d_Notes\" style=\"display:none\" ";
  private static final String group_grids     = "<g id=\"w2d_Grid\"  style=\"display:inline\" ";
  private static final String group_legend   = "    <g id=\"w2d_Legend\" style=\"display:none\" ";
  private static final String group_scalebar = "      <g id=\"Scalebar\"   ";
  private static final String group_north    = "      <g id=\"NorthArrow\" ";
  private static final String group_frame    = "    <g id=\"w2d_Frame\" style=\"display:none\" />";

}

