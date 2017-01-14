/** @file DrawingSvg.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid drawing: dxf export
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


import java.util.Locale;

import java.util.List;
// import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

// import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

// import android.util.Log;

class DrawingSvg
{
  private static final float grad2rad = TDMath.GRAD2RAD;

  private static void printSvgGrid( BufferedWriter out, List<DrawingPath> grid, String color, float opacity, float xoff, float yoff )
  {
    if ( grid != null && grid.size() > 0 ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.format(Locale.US, "<g style=\"fill:none;stroke-opacity:%.1f;stroke-width=\"1\";stroke:#666666\" >\n", opacity );
      for ( DrawingPath p : grid ) {
        pw.format(Locale.US, "  <path stroke-width=\"1\" stroke=\"#%s\" d=\"", color );
        pw.format(Locale.US, "M %.2f %.2f",  xoff+p.x1, yoff+p.y1 );
        pw.format(Locale.US, " L %.2f %.2f", xoff+p.x2, yoff+p.y2 );
        pw.format("\" />\n");
      }
      pw.format("</g>\n");
      try {
        out.write( sw.getBuffer().toString() );
        out.flush();
      } catch ( IOException e ) {
        TDLog.Error( "SVG grid io-exception " + e.getMessage() );
      }
    }
  }

  static void write( BufferedWriter out, DistoXNum num, DrawingCommandManager plot, long type )
  {
    String wall_group = BrushManager.getLineGroup( BrushManager.mLineLib.mLineWallIndex );

    int handle = 0;
    float xmin=10000f, xmax=-10000f, 
          ymin=10000f, ymax=-10000f;
    for ( ICanvasCommand cmd : plot.getCommands() ) {
      if ( cmd.commandType() != 0 ) continue;
      DrawingPath p = (DrawingPath)cmd;

      if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        String group = BrushManager.getLineGroup( lp.lineType() );
        if ( group != null && group.equals( wall_group ) ) {
          // ArrayList< LinePoint > pts = lp.mPoints;
          // for ( LinePoint pt : pts ) 
          for ( LinePoint pt = lp.mFirst; pt != null; pt = pt.mNext ) {
            if ( pt.mX < xmin ) xmin = pt.mX;
            if ( pt.mX > xmax ) xmax = pt.mX;
            if ( pt.mY < ymin ) ymin = pt.mY;
            if ( pt.mY > ymax ) ymax = pt.mY;
          }
        }
      } else if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
        DrawingPointPath pp = (DrawingPointPath)p;
        if ( pp.cx < xmin ) xmin = pp.cx;
        if ( pp.cx > xmax ) xmax = pp.cx;
        if ( pp.cy < ymin ) ymin = pp.cy;
        if ( pp.cy > ymax ) ymax = pp.cy;
      } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
        DrawingStationPath st = (DrawingStationPath)p;
        if ( st.cx < xmin ) xmin = st.cx;
        if ( st.cx > xmax ) xmax = st.cx;
        if ( st.cy < ymin ) ymin = st.cy;
        if ( st.cy > ymax ) ymax = st.cy;
      }
    }
    int width = (int)(xmax - xmin) + 200;
    int height = (int)(ymax - ymin) + 200;
    float xoff = 100 + xmin;
    float yoff = 100 + ymin;

    try {

      if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
        out.write("<!DOCTYPE html>\n<html>\n<body>\n");
      }

      // header
      out.write( "<svg width=\"" + width + "\" height=\"" + height + "\"\n" );
      out.write( "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n");
      out.write( "   xmlns=\"http://www.w3.org/2000/svg\" >\n" );
      out.write( "<!-- SVG created by TopoDroid v. " + TopoDroidApp.VERSION + " -->\n" );
      out.write( "  <defs>\n");
      out.write( "    <marker id=\"Triangle\" viewBox=\"0 0 10 10\" refX=\"0\" refY=\"5\" \n");
      out.write( "      markerUnits=\"strokeWidth\" markerWidth=\"4\" markerHeight=\"3\" orient=\"auto\" >\n");
      out.write( "      <path d=\"M 0 0 L 10 5 L 0 10 z\" />\n");
      out.write( "    </marker>\n"); 
      out.write( "  </defs>\n");

      if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
        out.write( "<g transform=\"translate(" + (int)( 100 + ((xmin < 0)? -xmin : 0) ) + ","
                   + (int)( 100 + ((ymin < 0)? -ymin : 0) ) + ")\" >\n" );
      } else {
        out.write( "<g>\n");
      }

      // ***** FIXME TODO POINT SYMBOLS
      // {
      //   // // 8 layer (0), 2 block name,
      //   for ( int n = 0; n < BrushManager.mPointLib.mSymbolNr; ++ n ) {
      //     SymbolPoint pt = (SymbolPoint)BrushManager.mPointLib.getSymbolByIndex(n);

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
      
      {
        float SCALE_FIX = DrawingUtil.SCALE_FIX;

        // centerline data
        if ( PlotInfo.isSketch2D( type ) ) { 
          if ( TDSetting.mSvgGrid ) {
            printSvgGrid( out, plot.GetGrid1(),   "999999", 0.4f, xoff, yoff );
            printSvgGrid( out, plot.GetGrid10(),  "666666", 0.6f, xoff, yoff );
            printSvgGrid( out, plot.GetGrid100(), "333333", 0.8f, xoff, yoff );
          }
          // FIXME OK PROFILE
          out.write("<g style=\"fill:none;stroke-opacity:0.6;stroke:red\" >\n");
          for ( DrawingPath sh : plot.getLegs() ) {
            DistoXDBlock blk = sh.mBlock;
            if ( blk == null ) continue;

            StringWriter sw4 = new StringWriter();
            PrintWriter pw4  = new PrintWriter(sw4);
            // if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
              NumStation f = num.getStation( blk.mFrom );
              NumStation t = num.getStation( blk.mTo );
 
              pw4.format("  <path stroke-width=\"1\" stroke=\"black\" d=\"");
              if ( type == PlotInfo.PLOT_PLAN ) {
                float x  = xoff + DrawingUtil.toSceneX( f.e ); 
                float y  = yoff + DrawingUtil.toSceneY( f.s );
                float x1 = xoff + DrawingUtil.toSceneX( t.e );
                float y1 = yoff + DrawingUtil.toSceneY( t.s );
                pw4.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", x, y, x1, y1 );
              } else if ( PlotInfo.isProfile( type ) ) { // FIXME OK PROFILE
                float x  = xoff + DrawingUtil.toSceneX( f.h );
                float y  = yoff + DrawingUtil.toSceneY( f.v );
                float x1 = xoff + DrawingUtil.toSceneX( t.h );
                float y1 = yoff + DrawingUtil.toSceneY( t.v );
                pw4.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", x, y, x1, y1 );
              }
            // }
            out.write( sw4.getBuffer().toString() );
            out.flush();
          }
          for ( DrawingPath sh : plot.getSplays() ) {
            DistoXDBlock blk = sh.mBlock;
            if ( blk == null ) continue;

            StringWriter sw41 = new StringWriter();
            PrintWriter pw41  = new PrintWriter(sw41);
            // if ( sh.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
              NumStation f = num.getStation( blk.mFrom );
              pw41.format("  <path stroke-width=\"1\" stroke=\"grey\" d=\"");
              float dh = blk.mLength * (float)Math.cos( blk.mClino * grad2rad )*SCALE_FIX;
              if ( type == PlotInfo.PLOT_PLAN ) {
                float x = xoff + DrawingUtil.toSceneX( f.e ); 
                float y = yoff + DrawingUtil.toSceneY( f.s );
                float de =   dh * (float)Math.sin( blk.mBearing * grad2rad);
                float ds = - dh * (float)Math.cos( blk.mBearing * grad2rad);
                pw41.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", x, y, x + de, (y+ds) );
              } else if ( PlotInfo.isProfile( type ) ) { // FIXME OK PROFILE
                float x = xoff + DrawingUtil.toSceneX( f.h );
                float y = yoff + DrawingUtil.toSceneY( f.v );
                float dv = - blk.mLength * (float)Math.sin( blk.mClino * grad2rad )*SCALE_FIX;
                pw41.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />\n", x, y, x+dh*blk.mExtend, (y+dv) );
              }
            // }
            out.write( sw41.getBuffer().toString() );
            out.flush();
          }
          out.write("</g>\n");
        }

        // FIXME station scale is 0.3
        float POINT_SCALE = 10.0f;
        for ( ICanvasCommand cmd : plot.getCommands() ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath path = (DrawingPath)cmd;
          int color = path.color();
          int red = ( color >> 16 ) & 0xff;
          int grn = ( color >>  8 ) & 0xff;
          int blu = ( color       ) & 0xff;
          String color_str = String.format( "#%02x%02x%02x", red, grn, blu );
          if ( red > 0xcc && grn > 0xcc && blu > 0xcc ) color_str = "#cccccc"; // cut-off white
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          if ( path.mType == DrawingPath.DRAWING_PATH_STATION ) {
            DrawingStationPath st = (DrawingStationPath)path;
            pw5.format("<text font-size=\"20\" font=\"sans-serif\" fill=\"black\" stroke=\"none\" text-amchor=\"middle\"");
            pw5.format(Locale.US, " x=\"%.2f\" y=\"%.2f\">", xoff + st.cx, yoff + st.cy );
            pw5.format("%s</text>\n", st.mName );
          } else if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath line = (DrawingLinePath) path;
            String th_name = BrushManager.mLineLib.getSymbolThName( line.mLineType ); 
            pw5.format("  <path stroke=\"%s\" stroke-width=\"2\" fill=\"none\" class=\"%s\"", color_str, th_name );
            if ( th_name.equals( "arrow" ) ) pw5.format(" marker-end=\"url(#Triangle)\"");
            else if ( th_name.equals( "section" ) ) pw5.format(" stroke-dasharray=\"5 3 \"");
            else if ( th_name.equals( "fault" ) ) pw5.format(" stroke-dasharray=\"8 4 \"");
            else if ( th_name.equals( "floor-meander" ) ) pw5.format(" stroke-dasharray=\"6 2 \"");
            else if ( th_name.equals( "ceiling-meander" ) ) pw5.format(" stroke-dasharray=\"6 2 \"");
            pw5.format(" d=\"");
            LinePoint p = line.mFirst;
            pw5.format(Locale.US, "M %.2f %.2f", xoff+p.mX, yoff+p.mY );
            for ( p = p.mNext; p != null; p = p.mNext ) { 
              pw5.format(Locale.US, " L %.2f %.2f", xoff+p.mX, yoff+p.mY );
            }
            pw5.format("\" />\n");
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath) path;
            pw5.format("  <path stroke=\"black\" stroke-width=\"1\" fill=\"%s\" fill-opacity=\"0.5\" d=\"", color_str );
            LinePoint p = area.mFirst;
            pw5.format(Locale.US, "M %.2f %.2f", xoff+p.mX, yoff+p.mY );
            for ( p = p.mNext; p != null; p = p.mNext ) { 
              pw5.format(Locale.US, " L %.2f %.2f", xoff+p.mX, yoff+p.mY );
            }
            pw5.format(" Z\" />\n");
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            // FIXME point scale factor is 0.3
            DrawingPointPath point = (DrawingPointPath) path;
            int idx = point.mPointType;
            String name = BrushManager.mPointLib.getSymbolThName( idx );
            pw5.format("<!-- point %s -->\n", name );
            if ( name.equals("label") ) {
              DrawingLabelPath label = (DrawingLabelPath)point;
              pw5.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff+point.cy );
              pw5.format(" style=\"fill:black;stroke:black;stroke-width:0.3\">%s</text>\n", label.mText );
            // } else if ( name.equals("continuation") ) {
            //   pw5.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff+point.cy );
            //   pw5.format(" style=\"fill:none;stroke:black;stroke-width:0.3\">\?</text>\n" );
            // } else if ( name.equals("danger") ) {
            //   pw5.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff+point.cy );
            //   pw5.format(" style=\"fill:none;stroke:red;stroke-width:0.3\">!</text>\n" );
            } else {
              SymbolPoint sp = (SymbolPoint)BrushManager.mPointLib.getSymbolByIndex( idx );
              if ( sp != null ) {
                pw5.format(Locale.US, "<g transform=\"translate(%.2f,%.2f),scale(10),rotate(%.2f)\" \n", 
                  xoff+point.cx, yoff+point.cy, point.mOrientation );
                pw5.format(" style=\"fill:none;stroke:%s;stroke-width:0.1\" >\n", color_str );
                pw5.format("%s\n", sp.mSvg );
                pw5.format("</g>\n");
              } else {
                pw5.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"10\" ", xoff+point.cx, yoff+point.cy );
                pw5.format(" style=\"fill:none;stroke:black;stroke-width:0.1\" />\n");
              }
            }
          }
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }
      }
      out.write("</g>\n");
      out.write("</svg>\n");

      if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
        out.write("</body>\n</html>\n");
      }

      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TDLog.Error( "SVG io-exception " + e.getMessage() );
    }
  }

}

