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
package com.topodroid.DistoX;

import android.util.Log;

import java.util.Locale;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

// import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.RectF;

class DrawingSvgWalls extends DrawingSvgBase
{
  void write( String filename, BufferedWriter out, DistoXNum num, DrawingCommandManager plot, long type )
  {
    String wall_group = BrushManager.getLineGroup( BrushManager.mLineLib.mLineWallIndex );

    int handle = 0;
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
    String vbox = "0 0 " + width + " " + height;
    xoff = - xmin;
    yoff = - ymin;

    // Log.v( "DistoX-SVGWALLS", "X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " W " + width + " H " + height ); 

    ArrayList< XSection > xsections = getXSections( plot, xoff, yoff );

    try {
      // if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
      //   out.write("<!DOCTYPE html>\n<html>\n<body>\n");
      // }
      // String vbox = Integer.toString( (int)xmin ) + " " + Integer.toString( -(int)ymax ) + " " + Integer.toString( (int)xmax ) + " " + Integer.toString( -(int)ymin );
      // Log.v( "DistoX-SVGWALLS", "Xoff " + xoff + " Yoff " + yoff + " vbox " + vbox );

      // header
      out.write( xml_header ); 
      out.write( walls_header );
      out.write( compass_header );
      out.write( "<!-- SVG created by TopoDroid v. " + TopoDroidApp.VERSION + " -->\n" );
      out.write( svg_header );
      out.write( "  i:pageBounds=\"0 0 " + width + " " + (-height) + "\"\n" );
      out.write( "  width=\"" + width + "pt\"\n" );
      out.write( "  height=\"" + height + "pt\"\n" );
      out.write( "  viewBox=\"" + vbox + "\"\n" );
      out.write( "  style=\"overflow:visible;enable-background:new " + vbox + "\"\n" );
      out.write( "  version=\"1.1\"\n" );
      out.write( "  id=\"svg46\"\n" );
      out.write( "  sodipodi:docname=\"" + filename + "\"\n" );
      out.write( "  inkscape:version=\"0.92.4 (5da689c313, 2019-01-14)\">\n" );
      out.write(    metadata );
      out.write(      rdf );
      out.write(        dc_format );
      out.write(        dc_type );
      out.write(      end_rdf );
      out.write(    end_metadata );
      out.write( "  <defs id=\"defs\">\n" );
      out.write( "    <marker id=\"Triangle\" viewBox=\"0 0 10 10\" refX=\"0\" refY=\"5\" \n");
      out.write( "      markerUnits=\"strokeWidth\" markerWidth=\"4\" markerHeight=\"3\" orient=\"auto\" >\n");
      out.write( "      <path d=\"M 0 0 L 10 5 L 0 10 z\" />\n");
      out.write( "    </marker>\n"); 
      // if ( TDSetting.mSvgLineDirection ) {
        // Log.v("DistoXsvg", "SVG line direction");
      StringWriter swD = new StringWriter();
      PrintWriter pwD  = new PrintWriter(swD);
      pwD.format("    <marker id=\"dir\" viewBox=\"0 0 10 30\"  orient=\"auto\"");
      pwD.format("       markerUnits=\"strokeWidth\" markerWidth=\"4\" refX=\"0\" refY=\"30\"");
      pwD.format(Locale.US, "      markerHeight=\"30\" stroke=\"#cccc3a\" stroke-width=\"%.2f\" fill=\"none\" >\n", TDSetting.mSvgLineDirStroke );
      pwD.format("      <line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"30\" />\n" );
      pwD.format("    </marker>\n");
      pwD.format("    <marker id=\"rev\" viewBox=\"0 0 10 30\"  orient=\"auto\"");
      pwD.format("      markerUnits=\"strokeWidth\" markerWidth=\"4\" refX=\"0\" refY=\"0\"");
      pwD.format(Locale.US, "      markerHeight=\"30\" stroke=\"#cccc3a\" stroke-width=\"%.2f\" fill=\"none\" >\n", TDSetting.mSvgLineDirStroke );
      pwD.format("      <line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"30\" />\n" );
      pwD.format("    </marker>\n");
      out.write( swD.getBuffer().toString() );
      out.flush();
      // }
      out.write( "    <icons id=\"icons\" " ); out.write( group_mode_open );
      for ( int n = 0; n < BrushManager.mPointLib.size(); ++ n ) {
        SymbolPoint pt = (SymbolPoint)BrushManager.getPointByIndex(n);
        int block = 1+n; // block_name = 1 + therion_code
        out.write( "    <symbol id=\"" + pt.mThName + "\">\n" );
        out.write( "      " + pt.getSvg().replace("path", "path inkscape:connector-curvature=\"0\"" ) + "\n" );;
        out.write( "    </symbol>\n" );
      }
      out.write( "    </icons>\n");
      out.write( "  </defs>\n");
      out.write(    sodipodi );
      out.write(    clip );
      out.write( "  <svg width=\"" + width + "\" height=\"" + height + "\"\n" );
      out.write(      svg_options );


      // out.write( "<g id=\"canvas\"\n" );
      // out.write( "  transform=\"translate(" + (int)(-xmin) + "," + (int)(-ymin) + ")\" >\n" );

      out.write(      bgrnd ); out.write( group_mode_close ); 
      out.write(      ref ); out.write( group_mode_close ); 

      // COMPASS TRACING
      out.write(      tracing ); out.write( group_mode_open ); 

      out.write(        sketchmap2 ); out.write( group_mode_close );
      out.write(        sketchmap1 ); out.write( group_mode_close );
      out.write(        passage );    out.write( group_mode_close );
      out.write(        lrud );       out.write( group_mode_close );
      out.write( "    " + end_grp ); // tracing


      out.write( mask ); out.write( group_mode_close );

      out.write(     detail );       out.write( group_mode_open );
      out.write(       detail_shp ); out.write( group_mode_open );
      for ( ICanvasCommand cmd : plot.getCommands() ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath path = (DrawingPath)cmd;
        if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath line = (DrawingLinePath)path;
          if ( BrushManager.isLineWall( line.mLineType ) ) continue;
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          toSvg( pw5, line, pathToColor(path), xoff, yoff );
          out.write( sw5.getBuffer().toString() );
        }
        out.flush();
      }
      for ( ICanvasCommand cmd : plot.getCommands() ) { // areas
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath path = (DrawingPath)cmd;
        if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          toSvg( pw5, (DrawingAreaPath) path, pathToColor(path), xoff, yoff );
          out.write( sw5.getBuffer().toString() );
        } 
        out.flush();
      }
      if ( TDSetting.mAutoXSections ) {
        for ( XSection xsection : xsections ) {
          StringWriter sw7 = new StringWriter();
          PrintWriter pw7  = new PrintWriter(sw7);
          pw7.format("<g id=\"%s\">\n", xsection.mFilename );
          tdrToSvg( pw7, xsection.mFilename, xsection.mX, xsection.mY, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y );
          pw7.format("</g>\n");
          out.write( sw7.getBuffer().toString() );
          out.flush();
        }
      }
      out.write( "      " + end_grp ); // detail_shp

      out.write( detail_sym ); out.write( group_mode_open );
      for ( ICanvasCommand cmd : plot.getCommands() ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath path = (DrawingPath)cmd;
        String color_str = pathToColor( path );
        if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
          DrawingPointPath point = (DrawingPointPath)path;
          if ( BrushManager.isPointLabel( point.mPointType ) ) continue;
          if ( BrushManager.isPointSection( point.mPointType ) ) {
            if ( TDSetting.mAutoStations ) continue;
            float xx = xoff+point.cx;
            float yy = yoff+point.cy;
            StringWriter sw5 = new StringWriter();
            PrintWriter pw5  = new PrintWriter(sw5);
            pw5.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%d\" ", xx, yy, RADIUS );
            pw5.format(Locale.US, " style=\"fill:grey;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgLabelStroke );
            out.write( sw5.getBuffer().toString() );
          } else {
            StringWriter sw5p = new StringWriter();
            PrintWriter pw5p  = new PrintWriter(sw5p);
            toSvg( pw5p, point, color_str, xoff, yoff );
            out.write( sw5p.getBuffer().toString() );
          }
        }
        out.flush();
      }
      out.write( "      " + end_grp ); // detail_sym
      out.write( "    " + end_grp ); // detail

      out.write(     walls ); out.write( group_mode_open );
      out.write(       walls_shp ); out.write( group_mode_open );
      for ( ICanvasCommand cmd : plot.getCommands() ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath path = (DrawingPath)cmd;
        if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
          DrawingLinePath line = (DrawingLinePath)path;
          if ( BrushManager.isLineWall( line.mLineType ) ) {
            StringWriter sw5w = new StringWriter();
            PrintWriter pw5w  = new PrintWriter(sw5w);
            toSvg( pw5w, line, pathToColor(path), xoff, yoff );
            out.write( sw5w.getBuffer().toString() );
          }
        }
        out.flush();
      }
      out.write( "      " + end_grp ); // walls_shp
      out.write( walls_sym ); out.write( group_mode_close );
      out.write( "    " + end_grp ); // walls

      // SURVEY: VECTORS
      out.write(     survey ); out.write( group_mode_open );
      out.write(       vectors ); out.write( group_mode_open );
      for ( DrawingPath sh : plot.getLegs() ) {
        DBlock blk = sh.mBlock;
        if ( blk == null ) continue;
        String id = blk.mFrom  + "_" + blk.mTo;
        StringWriter sw4 = new StringWriter();
        PrintWriter pw4  = new PrintWriter(sw4);
        pw4.format(Locale.US, "        <path id=\"%s\" stroke-width=\"%.2f\" stroke=\"black\" d=\"", id, TDSetting.mSvgShotStroke );
        pw4.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );
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
          pw41.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );

          out.write( sw41.getBuffer().toString() );
          out.flush();
        }
      }
      out.write( "      " + end_grp ); // vectors

      // SURVEY:  MARKERS
      out.write(       markers ); out.write( group_mode_open ); 
        if ( TDSetting.mAutoStations ) {
          StringWriter sw6m = new StringWriter();
          PrintWriter pw6m  = new PrintWriter(sw6m);
          for ( DrawingStationName st : plot.getStations() ) { // auto-stations
            pw6m.format(Locale.US, 
                        "<use xlink:href=\"#_m\" width=\"2\" height=\"2\" x=\"-1\" y=\"-1\" transform=\"matrix(0.798 0 0 -0.798 %.2f %.2f)\" style=\"display:inline\"/>\n", 
                        xoff + st.cx, yoff + st.cy );
          }
          out.write( sw6m.getBuffer().toString() );
        } 
      out.write( "      " + end_grp ); // vectors
      out.write(       flags ); out.write( group_mode_close );
      // SURVEY: LABELS
      out.write(       labels ); out.write( group_mode_open );
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
          for ( DrawingStationPath st_path : plot.getUserStations() ) { // user-chosen
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
        //   toSvg( pw5s, (DrawingStationPath)path, xoff, yoff );
        //   out.write( sw5s.getBuffer().toString() );
        // }
      }
      out.flush();

      for ( ICanvasCommand cmd : plot.getCommands() ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath path = (DrawingPath)cmd;
        if ( path.mType != DrawingPath.DRAWING_PATH_POINT ) continue;
        DrawingPointPath point = (DrawingPointPath)path;
        if ( ! BrushManager.isPointLabel( point.mPointType ) ) continue;
        StringWriter sw5l = new StringWriter();
        PrintWriter pw5l  = new PrintWriter( sw5l );
        toSvgLabel( pw5l, (DrawingLabelPath)point, pathToColor( path ), xoff, yoff );
        out.write( sw5l.getBuffer().toString() );
      }
      out.flush();
      out.write( "      " + end_grp ); // labels
      out.write( "    " + end_grp ); // survey

      // NOTES, GRID, LEGEND (SCALEBAR, NORTH), FRAME
      out.write(      notes ); out.write( group_mode_close );
      out.write(      grid); out.write( group_mode_open );
      if ( TDSetting.mSvgGrid ) {
        printSvgGrid( out, plot.getGrid1(),   "grid1",   "999999", 0.4f, xoff, yoff );
        printSvgGrid( out, plot.getGrid10(),  "grid10",  "666666", 0.6f, xoff, yoff );
        printSvgGrid( out, plot.getGrid100(), "grid100", "333333", 0.8f, xoff, yoff );
      }
      out.write( "    " + end_grp ); // grid

      out.write(     legend );     out.write( group_mode_open );
      out.write(       scalebar ); out.write( group_mode_close );
      out.write(       north );    out.write( group_mode_close );
      out.write( "    " + end_grp ); // legend
      out.write(     frame );  out.write( group_mode_close );
      out.write( "  " + end_svg );
      out.write( end_svg );

      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TDLog.Error( "SVG io-exception " + e.getMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  private static final String group_mode_open  = " inkscape:groupmode=\"layer\" i:layer=\"yes\" >\n";
  private static final String group_mode_close = " inkscape:groupmode=\"layer\" i:layer=\"yes\" />\n";

  private static final String xml_header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
  private static final String walls_header = "<?walls updated=\"no\" merged-content=\"no\" adjustable=\"no\"?>\n";
  private static final String compass_header = "<?compass inkscape-compatible=\"yes\"?>\n";
  private static final String svg_header = "<svg xmlns:i=\"http://ns.adobe.com/AdobeIllustrator/10.0/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\" xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\" \n";
  
  private static final String metadata = "  <metadata id=\"metadata52\">\n";
  private static final String rdf = "    <rdf:RDF><cc:Work rdf:about=\"\">\n";
  private static final String dc_format = "      <dc:format>image/svg+xml</dc:format>\n";
  private static final String dc_type   = "      <dc:type rdf:resource=\"http://purl.org/dc/dcmitype/StillImage\" />\n";
  private static final String end_rdf      = "    </cc:Work></rdf:RDF>\n";
  private static final String end_metadata = "  </metadata>\n";

  private static final String sodipodi = "  <sodipodi:namedview pagecolor=\"#ffffff\" bordercolor=\"#666666\" borderopacity=\"1\" objecttolerance=\"10\" gridtolerance=\"10\" guidetolerance=\"10\" inkscape:pageopacity=\"0\" inkscape:pageshadow=\"2\" inkscape:window-width=\"960\" inkscape:window-height=\"680\" id=\"namedview48\" showgrid=\"false\" inkscape:zoom=\"1\" inkscape:cx=\"480\" inkscape:cy=\"340\" inkscape:window-x=\"0\" inkscape:window-y=\"0\" inkscape:window-maximized=\"0\" inkscape:current-layer=\"w2d_Walls_shp\" />";
  private static final String clip = "<!--Used to clip frame - Not compatible with AI10 - remove if necessary-->";
  private static final String svg_options = " overflow=\"hidden\" version=\"1.1\" id=\"svg44\" style=\"display:inline;overflow:hidden\" >\n";

  private static final String bgrnd   = "    <g id=\"w2d_Background\" ";
  private static final String ref     = "    <g id=\"w2d_Ref\" ";
  private static final String tracing = "    <g id=\"cmp_tracing\" ";
  private static final String passage = "      <g id=\"cmp_passage\" ";
  private static final String lrud    = "      <g id=\"cmp_LRUDs\" ";
  private static final String sketchmap2 = "    <g id=\"cmp_Sketchmap2\" ";
  private static final String sketchmap1 = "    <g id=\"cmp_Sketchmap1\" ";
  private static final String mask       = "    <g id=\"w2d_Mask\" ";
  private static final String detail     = "    <g id=\"w2d_Detail\" ";
  private static final String detail_shp = "      <g id=\"w2d_Detail_shp\" ";
  private static final String detail_sym = "      <g id=\"w2d_Detail_sym\" ";
  
  private static final String walls      = "    <g id=\"w2d_Walls\" ";
  private static final String walls_shp  = "      <g id=\"w2d_Walls_shp\" ";
  private static final String walls_sym  = "      <g id=\"w2d_Walls_sym\" ";
  private static final String survey     = "    <g id=\"w2d_Survey\" ";
  private static final String vectors    = "      <g id=\"w2d_Vectors\" ";
  private static final String markers    = "      <g id=\"w2d_Markers\" ";
  private static final String flags      = "      <g id=\"w2d_Flags\"  style=\"display:none\" ";
  private static final String labels     = "      <g id=\"w2d_Labels\" style=\"display:inline\" ";

  private static final String notes     = "    <g id=\"w2d_Notes\" style=\"display:none\" ";
  private static final String grid      = "<g id=\"w2d_Grid\"  style=\"display:inline\" ";
  private static final String legend   = "    <g id=\"w2d_Legend\" style=\"display:none\" ";
  private static final String scalebar = "      <g id=\"Scalebar\"   ";
  private static final String north    = "      <g id=\"NorthArrow\" ";
  private static final String frame    = "    <g id=\"w2d_Frame\" style=\"display:none\" />";

}

