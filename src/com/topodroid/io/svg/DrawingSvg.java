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
import com.topodroid.num.TDNum;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

import com.topodroid.DistoX.DrawingStationPath;
import com.topodroid.DistoX.DrawingStationName;
import com.topodroid.DistoX.DrawingPointPath;
import com.topodroid.DistoX.DrawingLinePath;
import com.topodroid.DistoX.DrawingAreaPath;
import com.topodroid.DistoX.DrawingPath;
import com.topodroid.DistoX.DrawingUtil;
import com.topodroid.DistoX.DrawingCommandManager;
import com.topodroid.DistoX.IDrawingLink;
import com.topodroid.DistoX.BrushManager;
// import com.topodroid.DistoX.SymbolPoint;
import com.topodroid.DistoX.Scrap;
// import com.topodroid.DistoX.SurveyInfo;
import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.DBlock;

import java.util.Locale;
import java.util.ArrayList;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import android.graphics.RectF;

public class DrawingSvg extends DrawingSvgBase
{

  public void writeSvg( BufferedWriter out, TDNum num, /* DrawingUtil util, */ DrawingCommandManager plot, long type )
  {
    String wall_group = BrushManager.getLineWallGroup( );

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

    try {
      // if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
      //   out.write("<!DOCTYPE html>\n<html>\n<body>\n");
      // }

      // header
      out.write( "<svg width=\"" + width + "\" height=\"" + height + "\"\n" );
      out.write( "   xmlns:svg=\"http://www.w3.org/2000/svg\"\n");
      out.write( "   xmlns=\"http://www.w3.org/2000/svg\" >\n" );
      out.write( "<!-- SVG created by TopoDroid v. " + TDVersion.string() + " -->\n" );
      out.write( "  <defs>\n");
      out.write( "    <marker id=\"Triangle\" viewBox=\"0 0 10 10\" refX=\"0\" refY=\"5\" \n");
      out.write( "      markerUnits=\"strokeWidth\" markerWidth=\"4\" markerHeight=\"3\" orient=\"auto\" >\n");
      out.write( "      <path d=\"M 0 0 L 10 5 L 0 10 z\" />\n");
      out.write( "    </marker>\n"); 
      out.write( "  </defs>\n");

      out.write( "<g id=\"canvas\"\n" );
      out.write( "  transform=\"translate(" + (int)(-xmin) + "," + (int)(-ymin) + ")\" >\n" );


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
      
      {

        // centerline data
        if ( PlotType.isSketch2D( type ) ) { 
          // float xmin, xmax, ymax, ymin;
          // if ( PlotType.isPlan( type ) ) {
          //   xmin = num.surveyEmin();
          //   xmax = num.surveyEmax();
          //   ymin = num.surveySmin();
          //   ymax = num.surveySmax();
          // } else {
          //   xmin = num.surveyHmin();
          //   xmax = num.surveyHmax();
          //   ymin = num.surveyVmin();
          //   ymax = num.surveyVmax();
          // }
            
          if ( TDSetting.mSvgGrid ) {
            // TDLog.v( "SVG grid");
            out.write("<g id=\"grid\"\n" );
            out.write("  style=\"fill:none;stroke-opacity:0.4\" >\n");
            printSvgGrid( out, plot.getGrid1(),   "grid1",   "999999", 0.4f, xoff, yoff, xmin, xmax, ymin, ymax );
            printSvgGrid( out, plot.getGrid10(),  "grid10",  "666666", 0.6f, xoff, yoff, xmin, xmax, ymin, ymax );
            printSvgGrid( out, plot.getGrid100(), "grid100", "333333", 0.8f, xoff, yoff, xmin, xmax, ymin, ymax );
            out.write( end_grp );
          }
          // FIXME OK PROFILE

          // TDLog.v( "SVG legs " + plot.getLegs().size() );
          out.write("<g id=\"legs\"\n" );
          out.write("  style=\"fill:none;stroke-opacity:0.6;stroke:red\" >\n");
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
          out.write( end_grp );

          // TDLog.v( "SVG splays " + plot.getSplays().size() );
          if ( TDSetting.mSvgSplays ) {
            ArrayList< DrawingPath > normal = new ArrayList<>();
            if ( TDSetting.mSplayClasses ) {
              // split splays in classes
              ArrayList< DrawingPath > horiz  = new ArrayList<>();
              ArrayList< DrawingPath > vert   = new ArrayList<>();
              ArrayList< DrawingPath > xsect  = new ArrayList<>();
              for ( DrawingPath sh : plot.getSplays() ) {
                DBlock blk = sh.mBlock;
                if ( blk == null ) continue;
                if ( blk.isHSplay() ) {
                  horiz.add( sh );
                } else if ( blk.isVSplay() ) {
                  vert.add( sh );
                } else if ( blk.isXSplay() ) {
                  xsect.add( sh );
                } else {
                  normal.add( sh );
                }
              }
              writeSplays( out, normal, "splays", "grey", xoff, yoff );
              writeSplays( out, horiz,  "h-splays", "lightseagreen", xoff, yoff );
              writeSplays( out, vert,   "v-splays", "lightsteelblue", xoff, yoff );
              writeSplays( out, xsect,  "x-splays", "lightseablue", xoff, yoff );
            } else {
              for ( DrawingPath sh : plot.getSplays() ) {
                DBlock blk = sh.mBlock;
                if ( blk == null ) continue;
                normal.add( sh );
              }
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

	ArrayList< XSection > xsections = new ArrayList<>();

        // TDLog.v( "SVG scraps " + plot.getScraps().size() );
        for ( Scrap scrap : plot.getScraps() ) {
          ArrayList< DrawingPath > paths = new ArrayList<>();
          scrap.addCommandsToList( paths );
          out.write( "<g id=\"scrap_" + scrap.mScrapIdx + "\">\n" );

          // TDLog.v( "SVG paths " + paths.size() + " points" );

          out.write("<g id=\"points\">\n");
          for ( DrawingPath path : paths ) {
            if ( path.mType == DrawingPath.DRAWING_PATH_STATION ) {
              // TDLog.v( "SVG point station");
              StringWriter sw51 = new StringWriter();
              PrintWriter pw51  = new PrintWriter(sw51);
              toSvg( pw51, (DrawingStationPath)path, xoff, yoff );
              out.write( sw51.getBuffer().toString() );
            } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
              DrawingPointPath point = (DrawingPointPath)path;
              if ( BrushManager.isPointSection( point.mPointType ) ) {
                float xx = point.cx;
                float yy = point.cy;
                // TDLog.v( "SVG point xsection " + xx + " " + yy + " offset " + xoff + " " + yoff );
                
  	        if ( TDSetting.mAutoXSections ) {
                  // String color_str = pathToColor( path );
                  // pw5.format(Locale.US, "<g transform=\"translate(%.2f,%.2f)\" >\n", xx, yy );
                  // pw5.format(Locale.US, " style=\"fill:none;stroke:%s;stroke-width:0.1\" >\n", color_str );
                  // TDLog.v( "Section point <" + point.mOptions + "> " + point.cx + " " + point.cy );
                  // option: -scrap survey-xx#
                  // FIXME GET_OPTION
                  XSection xsection = null;
                  String scrapname = TDUtil.replacePrefix( TDInstance.survey, point.getOption( TDString.OPTION_SCRAP ) );
                  if ( scrapname != null ) {
                    String scrapfile = scrapname + ".tdr";
                    // String scrapfile = point.mOptions.substring( 7 ) + ".tdr";
  
                    // TODO open file survey-xx#.tdr and convert it to svg
                    // tdrToSvg( pw5, scrapfile, xx, yy, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y );
                    // xsection = new XSection( scrapfile, xx, yy );
                    xsection = new XSection( scrapfile, xx-DrawingUtil.CENTER_X, yy-DrawingUtil.CENTER_Y );
  	            xsections.add( xsection );
                  }
                  // pw5.format( end_grp );
                  IDrawingLink link = point.mLink; // FIXME Link could be stored in the XSection and wrtten with it 
                  if ( link != null && xsection != null ) {
                    float x1 = (xoff + xx) * TDSetting.mToSvg;
                    float y1 = (yoff + yy) * TDSetting.mToSvg;
                    float x2 = (xoff + link.getLinkX() ) * TDSetting.mToSvg;
                    float y2 = (yoff + link.getLinkY() ) * TDSetting.mToSvg;
                    StringWriter sw520 = new StringWriter();
                    PrintWriter pw520  = new PrintWriter(sw520);
                    pw520.format(Locale.US, "  <line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\"", x1, y1, x2, y2 );
                    pw520.format(Locale.US, " class=\"link\" style=\"fill:none;stroke:brown;stroke-width:%.2f\" />\n", TDSetting.mSvgShotStroke );
                    out.write( sw520.getBuffer().toString() );
                  }
  	        } else {
                  StringWriter sw52 = new StringWriter();
                  PrintWriter pw52  = new PrintWriter(sw52);
                  printPointWithCXCY( pw52, "<circle", xoff+xx, yoff+yy );
                  pw52.format(Locale.US, " r=\"%d\" ", RADIUS );
                  pw52.format(Locale.US, " style=\"fill:grey;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgLabelStroke );
                  out.write( sw52.getBuffer().toString() );
  	        }
              } else {
                String color_str = pathToColor( path );
                StringWriter sw53 = new StringWriter();
                PrintWriter pw53  = new PrintWriter(sw53);
                toSvg( pw53, point, color_str, xoff, yoff );
                out.write( sw53.getBuffer().toString() );
              }
            }
            out.flush();
          }
          out.write( end_grp ); // point

          // TDLog.v( "SVG paths lines" );

          out.write( "<g id=\"lines\">\n" );
          for ( DrawingPath path : paths ) {
            if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
              StringWriter sw54 = new StringWriter();
              PrintWriter pw54  = new PrintWriter(sw54);
              toSvg( pw54, (DrawingLinePath)path, pathToColor(path), xoff, yoff );
              out.write( sw54.getBuffer().toString() );
            }
            out.flush();
          }
          out.write( end_grp ); // lines

          // TDLog.v( "SVG paths areas" );

          out.write( "<g id=\"areas\">\n" );
          for ( DrawingPath path : paths ) {
            if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
              StringWriter sw55 = new StringWriter();
              PrintWriter pw55  = new PrintWriter(sw55);
              toSvg( pw55, (DrawingAreaPath) path, pathToColor(path), xoff, yoff );
              out.write( sw55.getBuffer().toString() );
            } 
            out.flush();
          }
          out.write( end_grp ); // areas

          out.write( end_grp ); // scrap
        }

        // xsections
        // TDLog.v( "SVG xsections " + xsections.size() );
        out.write("<g id=\"xsections\">\n");
        for ( XSection xsection : xsections ) {
          // TDLog.v( "SVG xsection " + xsection.mFilename + " " + xsection.mX + " " + xsection.mY );
          StringWriter sw7 = new StringWriter();
          PrintWriter pw7  = new PrintWriter(sw7);
          pw7.format("<g id=\"%s\">\n", xsection.mFilename );
          // tdrToSvg( pw7, xsection.mFilename, xsection.mX, xsection.mY, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y );
          tdrToSvg( pw7, xsection.mFilename, xsection.mX, xsection.mY, xoff, yoff );
          pw7.format( end_grp );
          out.write( sw7.getBuffer().toString() );
          out.flush();
        }
        out.write( end_grp );

        // stations
        // TDLog.v( "SVG statioons " + plot.getStations().size() );
        out.write("<g id=\"stations\">\n");
        if ( TDSetting.mAutoStations ) {
          for ( DrawingStationName name : plot.getStations() ) { // auto-stations
            StringWriter sw61 = new StringWriter();
            PrintWriter pw61  = new PrintWriter(sw61);
            toSvg( pw61, name, xoff, yoff );
            out.write( sw61.getBuffer().toString() );
          }
        } else {
          for ( DrawingStationPath st_path : plot.getUserStations() ) { // user-chosen
            StringWriter sw62 = new StringWriter();
            PrintWriter pw62  = new PrintWriter(sw62);
            toSvg( pw62, st_path, xoff, yoff );
            out.write( sw62.getBuffer().toString() );
          }
        }
        out.flush();
        
        out.write( end_grp );
      }
      out.write( end_grp );
      out.write( end_svg );

      // if ( TDSetting.mSvgInHtml ) { // SVG_IN_HTML
      //   out.write("</body>\n</html>\n");
      // }

      out.flush();
    } catch ( IOException e ) {
      TDLog.Error( "SVG io-exception " + e.getMessage() );
    }
  }

  private void writeSplays( BufferedWriter out, ArrayList< DrawingPath > splays, String group, String color, float xoff, float yoff )
  {
    if ( splays.size() == 0 ) return;
    try {
      out.write("<g id=\"" + group + "\"\n" );
      out.write("  style=\"fill:none;stroke-opacity:0.4;stroke:" + color + "\" >\n");
      for ( DrawingPath sh : splays ) {
        StringWriter sw41x = new StringWriter();
        PrintWriter pw41x  = new PrintWriter(sw41x);
        pw41x.format(Locale.US, "  <path stroke-width=\"%.2f\" stroke=\"%s\" d=\"", TDSetting.mSvgShotStroke, color );
        printSegmentWithClose( pw41x, xoff+sh.x1, yoff+sh.y1, xoff+sh.x2, yoff+sh.y2 );
        pw41x.format("\n");
        out.write( sw41x.getBuffer().toString() );
        out.flush();
      }
      out.write( end_grp );
    } catch ( IOException e ) {
      TDLog.Error( "SVG splay-io exception " + e.getMessage() );
    }
  }

}

