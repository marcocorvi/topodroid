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
 * CHANGES
 */
package com.topodroid.DistoX;


import java.util.Locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

// import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import android.util.FloatMath;
// import android.util.Log;

class DrawingSvg
{
  private static final float grad2rad = TopoDroidUtil.GRAD2RAD;

  static void write( BufferedWriter out, DistoXNum num, DrawingCommandManager plot, long type )
  {
    int handle = 0;
    float xmin=10000f, xmax=-10000f, 
          ymin=10000f, ymax=-10000f;
    for ( DrawingPath p : plot.mCurrentStack ) {
      if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
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
        if ( st.mXpos < xmin ) xmin = st.mXpos;
        if ( st.mXpos > xmax ) xmax = st.mXpos;
        if ( st.mYpos < ymin ) ymin = st.mYpos;
        if ( st.mYpos > ymax ) ymax = st.mYpos;
      }
    }
    int width = (int)(xmax - xmin) + 200;
    int height = (int)(ymax - ymin) + 200;

    try {

      // SVG_IN_HTML
      // out.write("<html>\n<body>\n");

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
      out.write( "<g transform=\"translate(" + (int)( 100 + ((xmin < 0)? -xmin : 0) ) + ","
                 + (int)( 100 + ((ymin < 0)? -ymin : 0) ) + ")\" >\n" );

      // ***** FIXME TODO POINT SYMBOLS
      // {
      //   // // 8 layer (0), 2 block name,
      //   for ( int n = 0; n < DrawingBrushPaths.mPointLib.mAnyPointNr; ++ n ) {
      //     SymbolPoint pt = DrawingBrushPaths.mPointLib.getAnyPoint(n);

      //     int block = 1+n; // block_name = 1 + therion_code
      //     writeString( out, 8, "POINT" );
      //     writeComment( out, pt.mName );
      //     writeInt( out, 2, block );
      //     writeInt( out, 70, 64 );
      //     writeString( out, 10, "0.0" );
      //     writeString( out, 20, "0.0" );
      //     writeString( out, 30, "0.0" );

      //     out.write( pt.getDxf() );
      //     // out.write( DrawingBrushPaths.mPointLib.getPoint(n).getDxf() );
      //   }
      // }
      
      {
        float SCALE_FIX = DrawingActivity.SCALE_FIX;

        // centerline data
        if ( type == PlotInfo.PLOT_PLAN || type == PlotInfo.PLOT_EXTENDED ) {
          out.write("<g style=\"fill:none;stroke-opacity:0.6;stroke:red\" >\n");
          for ( DrawingPath sh : plot.mFixedStack ) {
            DistoXDBlock blk = sh.mBlock;
            if ( blk == null ) continue;

            StringWriter sw4 = new StringWriter();
            PrintWriter pw4  = new PrintWriter(sw4);
            if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
              NumStation f = num.getStation( blk.mFrom );
              NumStation t = num.getStation( blk.mTo );
 
              pw4.format("  <path stroke-width=\"1\" stroke=\"black\" d=\"");
              if ( type == PlotInfo.PLOT_PLAN ) {
                float x  = DrawingActivity.toSceneX( f.e ); 
                float y  = DrawingActivity.toSceneY( f.s );
                float x1 = DrawingActivity.toSceneX( t.e );
                float y1 = DrawingActivity.toSceneY( t.s );
                pw4.format(Locale.ENGLISH, "M %.0f %.0f L %.0f %.0f\" />\n", x, y, x1, y1 );
              } else if ( type == PlotInfo.PLOT_EXTENDED ) {
                float x  = DrawingActivity.toSceneX( f.h );
                float y  = DrawingActivity.toSceneY( f.v );
                float x1 = DrawingActivity.toSceneX( t.h );
                float y1 = DrawingActivity.toSceneY( t.v );
                pw4.format(Locale.ENGLISH, "M %.0f %.0f L %.0f %.0f\" />\n", x, y, x1, y1 );
              }
            } else if ( sh.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
              NumStation f = num.getStation( blk.mFrom );
              pw4.format("  <path stroke-width=\"1\" stroke=\"grey\" d=\"");
              float dh = blk.mLength * FloatMath.cos( blk.mClino * grad2rad )*SCALE_FIX;
              if ( type == PlotInfo.PLOT_PLAN ) {
                float x = DrawingActivity.toSceneX( f.e ); 
                float y = DrawingActivity.toSceneY( f.s );
                float de =   dh * FloatMath.sin( blk.mBearing * grad2rad);
                float ds = - dh * FloatMath.cos( blk.mBearing * grad2rad);
                pw4.format(Locale.ENGLISH, "M %.0f %.0f L %.0f %.0f\" />\n", x, y, x + de, (y+ds) );
              } else if ( type == PlotInfo.PLOT_EXTENDED ) {
                float x = DrawingActivity.toSceneX( f.h );
                float y = DrawingActivity.toSceneY( f.v );
                float dv = - blk.mLength * FloatMath.sin( blk.mClino * grad2rad )*SCALE_FIX;
                pw4.format(Locale.ENGLISH, "M %.0f %.0f L %.0f %.0f\" />\n", x, y, x+dh*blk.mExtend, (y+dv) );
              }
            }
            out.write( sw4.getBuffer().toString() );
            out.flush();
          }
          out.write("</g>\n");
        }

        // FIXME station scale is 0.3
        float POINT_SCALE = 10.0f;
        for ( DrawingPath path : plot.mCurrentStack ) {
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
            pw5.format(Locale.ENGLISH, " x=\"%.0f\" y=\"%.0f\">", st.mXpos, st.mYpos );
            pw5.format("%s</text>\n", st.mName );
          } else if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath line = (DrawingLinePath) path;
            pw5.format("  <path stroke=\"%s\" stroke-width=\"2\" fill=\"none\"", color_str );
            String th_name = DrawingBrushPaths.getLineThName( line.mLineType ); 
            if ( th_name.equals( "arrow" ) ) pw5.format(" marker-end=\"url(#Triangle)\"");
            else if ( th_name.equals( "section" ) ) pw5.format(" stroke-dasharray=\"5 3 \"");
            else if ( th_name.equals( "fault" ) ) pw5.format(" stroke-dasharray=\"8 4 \"");
            else if ( th_name.equals( "floor-meander" ) ) pw5.format(" stroke-dasharray=\"6 2 \"");
            else if ( th_name.equals( "ceiling-meander" ) ) pw5.format(" stroke-dasharray=\"6 2 \"");
            pw5.format(" d=\"");
            LinePoint p = line.mFirst;
            pw5.format(Locale.ENGLISH, "M %.0f %.0f", p.mX, p.mY );
            for ( p = p.mNext; p != null; p = p.mNext ) { 
              pw5.format(Locale.ENGLISH, " L %.0f %.0f", p.mX, p.mY );
            }
            pw5.format("\" />\n");
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath) path;
            pw5.format("  <path stroke=\"black\" stroke-width=\"1\" fill=\"%s\" fill-opacity=\"0.5\" d=\"", color_str );
            LinePoint p = area.mFirst;
            pw5.format(Locale.ENGLISH, "M %.0f %.0f", p.mX, p.mY );
            for ( p = p.mNext; p != null; p = p.mNext ) { 
              pw5.format(Locale.ENGLISH, " L %.0f %.0f", p.mX, p.mY );
            }
            pw5.format(" Z\" />\n");
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            // FIXME point scale factor is 0.3
            DrawingPointPath point = (DrawingPointPath) path;
            int idx = point.mPointType;
            String name = DrawingBrushPaths.getPointThName( idx );
            pw5.format("<!-- point %s -->\n", name );
            if ( name.equals("label") ) {
              DrawingLabelPath label = (DrawingLabelPath)point;
              pw5.format(Locale.ENGLISH, "<text x=\"%.0f\" y=\".0f\" ", point.cx, -point.cy );
              pw5.format(" style=\"fill:black;stroke:black;stroke-width:0.3\">%s</text>\n", label.mText );
            // } else if ( name.equals("continuation") ) {
            //   pw5.format(Locale.ENGLISH, "<text x=\"%.0f\" y=\".0f\" ", point.cx, -point.cy );
            //   pw5.format(" style=\"fill:none;stroke:black;stroke-width:0.3\">\?</text>\n" );
            // } else if ( name.equals("danger") ) {
            //   pw5.format(Locale.ENGLISH, "<text x=\"%.0f\" y=\".0f\" ", point.cx, -point.cy );
            //   pw5.format(" style=\"fill:none;stroke:red;stroke-width:0.3\">!</text>\n" );
            } else {
              SymbolPoint sp = DrawingBrushPaths.mPointLib.getAnyPoint( idx );
              if ( sp != null ) {
                pw5.format(Locale.ENGLISH, "<g transform=\"translate(%.0f,%.0f),scale(10),rotate(%.0f)\" \n", 
                  point.cx, point.cy, point.mOrientation );
                pw5.format(" style=\"fill:none;stroke:%s;stroke-width:0.1\" >\n", color_str );
                pw5.format("%s\n", sp.mSvg );
                pw5.format("</g>\n");
              } else {
                pw5.format(Locale.ENGLISH, "<circle cx=\"%.0f\" cy=\".0f\" r=\"10\" ", point.cx, -point.cy );
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

      // SVG_IN_HTML
      // out.write("</body></html>\n");

      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "SVG io-exception " + e.toString() );
    }
  }

}

