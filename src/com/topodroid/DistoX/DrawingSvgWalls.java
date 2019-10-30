/* @file DrawingSvgWalls.java
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

class DrawingSvgWalls
{
  // FIXME station scale is 0.3
  static final private int POINT_SCALE  = 10;
  static final private int POINT_RADIUS = 10;
  static final private int RADIUS = 3;
  // float SCALE_FIX = util.SCALE_FIX; // 20.0f
  
  private class XSection
  {
    String mFilename;
    float  mX, mY;

    XSection( String filename, float x, float y ) 
    {
      mFilename = filename;
      mX = x; 
      mY = y;
    }
  }

  private static void printSvgGrid( BufferedWriter out, List<DrawingPath> grid, String color, float opacity, float xoff, float yoff )
  {
    if ( grid != null && grid.size() > 0 ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.format("<g id=\"w2d_Grid\"\n" );
      pw.format( group_mode );
      pw.format(Locale.US, " style=\"fill:none;stroke-opacity:%.1f;stroke-width=%.2f;stroke:#666666;display:inine\" >\n", opacity, TDSetting.mSvgGridStroke );
      for ( DrawingPath p : grid ) {
        pw.format(Locale.US, "  <path stroke-width=\"%.2f\" stroke=\"#%s\" d=\"", TDSetting.mSvgGridStroke, color );
        pw.format(Locale.US, "M %.2f %.2f",  xoff+p.x1, yoff+p.y1 );
        pw.format(Locale.US, " L %.2f %.2f", xoff+p.x2, yoff+p.y2 );
        pw.format("\" />\n");
      }
      pw.format( end_grp );
      try {
        out.write( sw.getBuffer().toString() );
        out.flush();
      } catch ( IOException e ) {
        TDLog.Error( "SVG grid io-exception " + e.getMessage() );
      }
    }
  }

  static private String pathToColor( DrawingPath path )
  {
    int color = path.color();
    int red = ( color >> 16 ) & 0xff;
    int grn = ( color >>  8 ) & 0xff;
    int blu = ( color       ) & 0xff;
    if ( red > 0xcc && grn > 0xcc && blu > 0xcc ) return "#cccccc"; // cut-off white
    return String.format( "#%02x%02x%02x", red, grn, blu );
  }

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

    // Log.v( "DistoX-SVGWALLS", "X " + xmin + " " + xmax + " Y " + ymin + " " + ymax + " W " + width + " H " + height ); 

    try {
      // if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
      //   out.write("<!DOCTYPE html>\n<html>\n<body>\n");
      // }
      // String vbox = Integer.toString( (int)xmin ) + " " + Integer.toString( -(int)ymax ) + " " + Integer.toString( (int)xmax ) + " " + Integer.toString( -(int)ymin );
      String vbox = "0 0 " + width + " " + height;
      xoff = - xmin;
      yoff = - ymin;
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
      out.write( "  <defs id=\"defs50\">\n" );
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
      out.write( "  </defs>\n");
      out.write(    sodipodi );
      out.write(    clip );
      out.write( "  <svg width=\"" + width + "\" height=\"" + height + "\"\n" );
      out.write(      svg_options );

      for ( int n = 0; n < BrushManager.mPointLib.size(); ++ n ) {
        SymbolPoint pt = (SymbolPoint)BrushManager.getPointByIndex(n);

        int block = 1+n; // block_name = 1 + therion_code
        out.write( "    <symbol id=\"" + pt.mThName + "\">\n" );
        out.write( "      " + pt.getSvg().replace("path", "path inkscape:connector-curvature=\"0\"" ) + "\n" );;
        out.write( "    </symbol>\n" );
      }

      // out.write( "<g id=\"canvas\"\n" );
      // out.write( "  transform=\"translate(" + (int)(-xmin) + "," + (int)(-ymin) + ")\" >\n" );

      out.write(      bgrnd ); out.write( group_mode ); out.write("/>\n");
      out.write(      ref ); out.write( group_mode ); out.write("/>\n");
      out.write(      tracing ); out.write( group_mode ); out.write(">\n");

      out.write(        passage ); out.write( group_mode ); out.write(">\n");
      // out.write("<g id=\"legs\"\n" );
      // out.write("  style=\"fill:none;stroke-opacity:0.6;stroke:red\" >\n");
      for ( DrawingPath sh : plot.getLegs() ) {
        DBlock blk = sh.mBlock;
        if ( blk == null ) continue;

        StringWriter sw4 = new StringWriter();
        PrintWriter pw4  = new PrintWriter(sw4);
        pw4.format(Locale.US, "        <path stroke-width=\"%.2f\" stroke=\"black\" d=\"", TDSetting.mSvgShotStroke );
        pw4.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );
        out.write( sw4.getBuffer().toString() );
        out.flush();
      }
      // out.write("</g>\n");
      out.write( "      " + end_grp ); // passage

      out.write(        lrud ); out.write( group_mode ); out.write(">\n");
      if ( TDSetting.mSvgSplays ) {
        // out.write("<g id=\"splays\"\n" );
        // out.write("  style=\"fill:none;stroke-opacity:0.4;stroke:orange\" >\n");
        for ( DrawingPath sh : plot.getSplays() ) {
          DBlock blk = sh.mBlock;
          if ( blk == null ) continue;

          StringWriter sw41 = new StringWriter();
          PrintWriter pw41  = new PrintWriter(sw41);
          pw41.format(Locale.US, "        <path stroke-width=\"%.2f\" stroke=\"grey\" d=\"", TDSetting.mSvgShotStroke );
          pw41.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );

          out.write( sw41.getBuffer().toString() );
          out.flush();
        }
        // out.write("</g>\n");
      }

      out.write( "      " + end_grp ); // lrud

      out.write(       sketchmap2 );  out.write( group_mode ); out.write("/>\n");
      out.write(       sketchmap1 );  out.write( group_mode ); out.write("/>\n");
      out.write( "    " + end_grp ); // tracing

      out.write( mask ); out.write( group_mode ); out.write("/>\n");                                     

      ArrayList< XSection > xsections = new ArrayList< XSection >();
      if ( TDSetting.mAutoXSections ) {
        for ( ICanvasCommand cmd : plot.getCommands() ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath path = (DrawingPath)cmd;
          if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            DrawingPointPath point = (DrawingPointPath)path;
            if ( BrushManager.isPointSection( point.mPointType ) ) {
              float xx = xoff+point.cx;
              float yy = yoff+point.cy;
              String scrapname = point.getOption("-scrap");
              if ( scrapname != null ) {
                String scrapfile = scrapname + ".tdr";
	        xsections.add( new XSection( scrapfile, xx, yy ) );
              }
            }
          }
        }
      }

      out.write(     detail ); out.write( group_mode ); out.write(">\n");                                     
      out.write(       detail_shp ); out.write( group_mode ); out.write(">\n");
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

      out.write( detail_sym ); out.write( group_mode ); out.write(">\n");
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

      out.write(     walls ); out.write( group_mode ); out.write(">\n");
      out.write(       walls_shp ); out.write( group_mode ); out.write(">\n");
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

      out.write( walls_sym ); out.write( group_mode ); out.write("/>\n");

      out.write( "    " + end_grp ); // walls

      out.write(     survey ); out.write( group_mode ); out.write(">\n");
      out.write(       vectors ); out.write( group_mode ); out.write(">\n");
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
        // out.write("</g>\n");
      }
      out.write( "      " + end_grp ); // vectors

      out.write(       markers ); out.write( group_mode ); out.write("/>\n");
      out.write(       flags ); out.write( group_mode ); out.write( "/>\n" );

      out.write(       stations ); out.write( group_mode ); out.write( ">\n" );
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
      out.write( "      " + end_grp ); // stations

      out.write( labels ); out.write( group_mode ); out.write( ">\n" );
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

      out.write(      notes ); out.write( group_mode ); out.write( "/>\n" );

      if ( PlotInfo.isSketch2D( type ) ) { 
        if ( TDSetting.mSvgGrid ) {
          printSvgGrid( out, plot.getGrid1(),   "999999", 0.4f, xoff, yoff );
          printSvgGrid( out, plot.getGrid10(),  "666666", 0.6f, xoff, yoff );
          printSvgGrid( out, plot.getGrid100(), "333333", 0.8f, xoff, yoff );
        }
      }

      out.write(     legend );     out.write( group_mode ); out.write( ">\n" );
      out.write(       scalebar ); out.write( group_mode ); out.write( "/>\n" );
      out.write(       north );    out.write( group_mode ); out.write( "/>\n" );
      out.write( "    " + end_grp ); // legend
      out.write(     frame );  out.write( group_mode ); out.write( "/>\n" );
      out.write( "  " + end_svg );
      out.write( end_svg );

      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TDLog.Error( "SVG io-exception " + e.getMessage() );
    }
  }

  static private void toSvg( PrintWriter pw, DrawingStationName st, float xoff, float yoff )
  {
    // pw.format("<text font-size=\"20\" font-family=\"sans-serif\" fill=\"violet\" stroke=\"none\" text-anchor=\"middle\"");
    pw.format("<text font-size=\"20\" fill=\"violet\" stroke=\"none\" text-anchor=\"middle\"");
    pw.format(Locale.US, " x=\"%.2f\" y=\"%.2f\">", xoff + st.cx, yoff + st.cy );
    pw.format("%s</text>\n", st.getName() );
  }

  static private void toSvg( PrintWriter pw, DrawingStationPath sp, float xoff, float yoff )
  {
    // pw.format("<text font-size=\"20\" font-family=\"sans-serif\" fill=\"black\" stroke=\"none\" text-anchor=\"middle\"");
    pw.format("<text font-size=\"20\" fill=\"black\" stroke=\"none\" text-anchor=\"middle\"");
    pw.format(Locale.US, " x=\"%.2f\" y=\"%.2f\">", xoff + sp.cx, yoff + sp.cy );
    pw.format("%s</text>\n", sp.name() );
  }

  static private void toSvgPointLine( PrintWriter pw, DrawingPointLinePath lp, float xoff, float yoff, boolean closed )
  {
    float bezier_step = TDSetting.getBezierStep();
    pw.format(" d=\"");
    LinePoint p = lp.mFirst;
    float x0 = xoff+p.x;
    float y0 = yoff+p.y;
    pw.format(Locale.US, "M %.2f %.2f", xoff+p.x, yoff+p.y );
    for ( p = p.mNext; p != null; p = p.mNext ) { 
      float x3 = xoff+p.x;
      float y3 = yoff+p.y;
      if ( p.has_cp ) { // FIXME this converts the cubic with a thickly interpolated polyline
        float x1 = xoff + p.x1;
        float y1 = yoff + p.y1;
        float x2 = xoff + p.x2;
        float y2 = yoff + p.y2;
	float len = (x1-x0)*(x1-x0) + (x2-x1)*(x2-x1) + (x3-x2)*(x3-x2) + (x3-x0)*(x3-x0)
	          + (y1-y0)*(y1-y0) + (y2-y1)*(y2-y1) + (y3-y2)*(y3-y2) + (y3-y0)*(y3-y0);
	int np = (int)( TDMath.sqrt( len ) * bezier_step + 0.5f );
	if ( np > 1 ) {
	  BezierCurve bc = new BezierCurve( x0, y0, x1, y1, x2, y2, x3, y3 );
	  for ( int n=1; n < np; ++n ) {
	    Point2D pb = bc.evaluate( (float)n / (float)np );
            pw.format(Locale.US, " L %.2f %.2f", pb.x, pb.y );
          }
	}
      } 
      pw.format(Locale.US, " L %.2f %.2f", x3, y3 );
      x0 = x3;
      y0 = y3;
    }
    if ( closed ) { 
      pw.format(" Z \"");
    } else {
      pw.format("\"");
    }
  }

  static private void toSvg( PrintWriter pw, DrawingLinePath line, String color, float xoff, float yoff ) 
  {
    String th_name = BrushManager.getLineThName( line.mLineType ); 
    pw.format(Locale.US, "  <path stroke=\"%s\" stroke-width=\"%.2f\" fill=\"none\" class=\"%s\"", color, TDSetting.mSvgLineStroke, th_name );
    if ( th_name.equals( "arrow" ) ) pw.format(" marker-end=\"url(#Triangle)\"");
    else if ( th_name.equals( "section" ) ) pw.format(" stroke-dasharray=\"5 3 \"");
    else if ( th_name.equals( "fault" ) ) pw.format(" stroke-dasharray=\"8 4 \"");
    else if ( th_name.equals( "floor-meander" ) ) pw.format(" stroke-dasharray=\"6 2 \"");
    else if ( th_name.equals( "ceiling-meander" ) ) pw.format(" stroke-dasharray=\"6 2 \"");
    toSvgPointLine( pw, line, xoff, yoff, line.isClosed() );
    if ( TDSetting.mSvgLineDirection ) {
      if ( BrushManager.mLineLib.hasEffect( line.mLineType ) ) {
        if ( line.isReversed() ) {
          pw.format(" marker-start=\"url(#rev)\"");
        } else {
          pw.format(" marker-start=\"url(#dir)\"");
        }
      }
    } 
    pw.format(" />\n");
  }

  static private void toSvg( PrintWriter pw, DrawingAreaPath area, String color, float xoff, float yoff )
  {
    pw.format(Locale.US, "  <path stroke=\"black\" stroke-width=\"%.2f\" fill=\"%s\" fill-opacity=\"0.5\" ", TDSetting.mSvgLineStroke, color );
    toSvgPointLine( pw, area, xoff, yoff, true ); // area borders are closed
    pw.format(" />\n");
  }

  static private void toSvg( PrintWriter pw, DrawingPointPath point, String color, float xoff, float yoff )
  {
    int idx = point.mPointType;
    String name = BrushManager.getPointThName( idx );
    pw.format("<!-- point %s -->\n", name );
    if ( name.equals("label") ) {
      DrawingLabelPath label = (DrawingLabelPath)point;
      pw.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff+point.cy );
      pw.format(Locale.US, " style=\"fill:black;stroke:black;stroke-width:%.2f\">%s</text>\n", TDSetting.mSvgLabelStroke, label.mPointText );
    // } else if ( name.equals("continuation") ) {
    //   pw.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff+point.cy );
    //   pw.format(Locale.US, " style=\"fill:none;stroke:black;stroke-width:%.2f\">\?</text>\n", TDSetting.mSvgLabelStroke );
    // } else if ( name.equals("danger") ) {
    //   pw.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff+point.cy );
    //   pw.format(Locale.US, " style=\"fill:none;stroke:red;stroke-width:%.2f\">!</text>\n", TDSetting.mSvgLabelStroke );
    } else if ( BrushManager.isPointSection( idx ) ) {
      /* nothing */
    } else {
      SymbolPoint sp = (SymbolPoint)BrushManager.getPointByIndex( idx );
      if ( sp != null ) {
        pw.format(Locale.US, "<g style=\"fill:none;stroke:%s;stroke-width:%.2f\" >\n", color, TDSetting.mSvgPointStroke );
        // pw.format(Locale.US, "<g transform=\"translate(%.2f,%.2f), scale(%d), rotate(%.2f)\">\n", 
        //   xoff+point.cx, yoff+point.cy, POINT_SCALE, point.mOrientation );

        float o = (float)(point.mOrientation);
        float s = POINT_SCALE * TDMath.sind( o );
        float c = POINT_SCALE * TDMath.cosd( o );
        pw.format(Locale.US, "<g transform=\"matrix(%.2f,%.2f,%.2f,%.2f,%.2f,%.2f)\">\n", 
          c, s, -s, c, xoff+point.cx, yoff+point.cy );

        pw.format("%s\n", sp.getSvg() );
        pw.format("</g>\n");
        pw.format("</g>\n");
      } else {
        pw.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%d\" ", xoff+point.cx, yoff+point.cy, POINT_RADIUS );
        pw.format(Locale.US, " style=\"fill:none;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgPointStroke );
      }
    }
  }

  static private void toSvgLabel( PrintWriter pw, DrawingLabelPath point, String color, float xoff, float yoff )
  {
    int idx = point.mPointType;
    String name = BrushManager.getPointThName( idx );
    pw.format("<!-- point %s -->\n", name );
    if ( name.equals("label") ) {
      DrawingLabelPath label = (DrawingLabelPath)point;
      pw.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff+point.cy );
      pw.format(Locale.US, " style=\"fill:black;stroke:black;stroke-width:%.2f\">%s</text>\n", TDSetting.mSvgLabelStroke, label.mPointText );
    }
  }
  // <text
  //    style="font-size:6px;font-family:ArialMT;fill:#0000ff"
  //    x="228.089"
  //    y="562.5"
  //    id="text32">0</text>

  static private void tdrToSvg( PrintWriter pw, String scrapfile, float dx, float dy, float xoff, float yoff )
  {
    try {
      // TDLog.Log( TDLog.LOG_IO, "trd to svg. scrap file " + scrapfile );
      FileInputStream fis = new FileInputStream( TDPath.getTdrFile( scrapfile ) );
      BufferedInputStream bfis = new BufferedInputStream( fis );
      DataInputStream dis = new DataInputStream( bfis );
      int version = DrawingIO.skipTdrHeader( dis );
      // Log.v("DistoXsvg", "tdr to svg " + scrapfile + " delta " + dx + " " + dy + " Offset " + xoff + " " + yoff );

      DrawingPath path = null;
      boolean done = false;
      while ( ! done ) {
        int what = dis.read();
        switch ( what ) {
          case 'P':
            path = DrawingPointPath.loadDataStream( version, dis, dx, dy /*, null */ );
            if ( path != null) toSvg( pw, (DrawingPointPath)path, pathToColor(path), xoff, yoff );
            break;
          case 'T':
            path = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
            if ( path != null) toSvg( pw, (DrawingLabelPath)path, pathToColor(path), xoff, yoff );
            break;
          case 'L':
            path = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, null */ );
            if ( path != null) toSvg( pw, (DrawingLinePath)path, pathToColor(path), xoff, yoff );
            break;
          case 'A':
            path = DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, null */ );
            if ( path != null) toSvg( pw, (DrawingAreaPath)path, pathToColor(path), xoff, yoff );
            break;
          case 'U':
            path = DrawingStationPath.loadDataStream( version, dis ); // consume DrawingStationName data
            break;
          case 'X':
            path = DrawingStationName.loadDataStream( version, dis ); // consume DrawingStationName data
            break;
          case 'Y':
            path = DrawingPhotoPath.loadDataStream( version, dis, dx, dy );
            break;
          case 'Z':
            path = DrawingAudioPath.loadDataStream( version, dis, dx, dy );
            break;
          case 'J':
            path = DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
            break;
          case 'F':
            done = true;
            break;
	  default:
	    TDLog.Error("TDR2SVG Error. unexpected code=" + what );
	    return;
        }
      }
    } catch ( FileNotFoundException e ) { // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  private static final String end_grp = "</g>\n";
  private static final String group_mode = " inkscape:groupmode=\"layer\" i:layer=\"yes\"";

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
  private static final String stations   = "      <g id=\"w2d_Stations\" ";
  private static final String markers    = "      <g id=\"w2d_Markers\" ";
  private static final String flags      = "      <g id=\"w2d_Flags\"  style=\"display:none\" ";
  private static final String labels     = "      <g id=\"w2d_Labels\" style=\"display:inline\" ";

  private static final String notes     = "    <g id=\"w2d_Notes\" style=\"display:none\" ";
  // private static final String grid      = "<g id=\"w2d_Grid\"  style=\"display:inline\" ";
  private static final String legend   = "    <g id=\"w2d_Legend\" style=\"display:none\" ";
  private static final String scalebar = "      <g id=\"Scalebar\"   ";
  private static final String north    = "      <g id=\"NorthArrow\" ";
  private static final String frame    = "    <g id=\"w2d_Frame\" style=\"display:none\" />";
  private static final String end_svg  = "</svg>\n";

}

