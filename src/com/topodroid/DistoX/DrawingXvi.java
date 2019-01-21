/* @file DrawingXvi.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid drawing: xvi export
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


import java.util.Locale;

import java.util.List;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Locale;

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

// import android.util.Log;

class DrawingXvi
{
  // FIXME station scale is 0.3
  static final private int POINT_SCALE  = 10;
  static final private int POINT_RADIUS = 10;
  static final private int RADIUS = 3;
  // float SCALE_FIX = util.SCALE_FIX; // 20.0f

  // X_orig Y_orig X_cell 0.0 0.0 Y_cell X_nr Y_nr
  private static void printXviGrid( BufferedWriter out, float x1, float y1, float x2, float y2, float xoff, float yoff )
  {
    float cell = 20; // this makes 1 m cells
    int nx = (int)((x2-x1)/cell);
    int ny = (int)((y2-y1)/cell);
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.US, "set XVIgrid { %.2f %.2f %.2f 0.0 0.0 %.2f %d %d }\n\n", xoff+x1, yoff-y2, cell, cell, nx, ny );
    try {
      out.write( sw.getBuffer().toString() );
      out.flush();
    } catch ( IOException e ) {
      TDLog.Error( "SVG grid io-exception " + e.getMessage() );
    }
  }

  static void write( BufferedWriter out, DistoXNum num, /* DrawingUtil util, */ DrawingCommandManager plot, long type )
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

    try {
      // header
      out.write( "set XVIgrids {1.0 m}\n");

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
      
      // centerline data
      if ( PlotInfo.isSketch2D( type ) ) { 
        // FIXME OK PROFILE


        // STATIONS
        out.write("set XVIstations {\n");
        StringWriter sw6 = new StringWriter();
        PrintWriter pw6  = new PrintWriter(sw6);
        if ( TDSetting.mAutoStations ) {
          for ( DrawingStationName name : plot.getStations() ) { // auto-stations
            toXvi( pw6, name, xoff, yoff );
          }
          out.write( sw6.getBuffer().toString() );
          out.flush();
        } else {
          for ( DrawingStationPath st_path : plot.getUserStations() ) { // user-chosen
            toXvi( pw6, st_path, xoff, yoff );
          }
          out.write( sw6.getBuffer().toString() );
          out.flush();
        }
        out.write("}\n\n");

	// GRID
        if ( TDSetting.mSvgGrid ) {
          printXviGrid( out, xmin, ymin, xmax, ymax, xoff, yoff );
        }

	// SHOTS
        out.write("set XVIshots {\n");
        StringWriter sw4 = new StringWriter();
        PrintWriter pw4  = new PrintWriter(sw4);
        for ( DrawingPath sh : plot.getLegs() ) {
          DBlock blk = sh.mBlock;
          if ( blk == null ) continue;
          pw4.format(Locale.US, "  { %.2f %.2f %.2f %.2f }\n", xoff+sh.x1, yoff-sh.y1, xoff+sh.x2, yoff-sh.y2 );
        }
        out.write( sw4.getBuffer().toString() );
        out.flush();

        if ( TDSetting.mSvgSplays ) {
          StringWriter sw41 = new StringWriter();
          PrintWriter pw41  = new PrintWriter(sw41);
          for ( DrawingPath sh : plot.getSplays() ) {
            DBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            pw41.format(Locale.US, "  { %.2f %.2f %.2f %.2f }\n", xoff+sh.x1, yoff-sh.y1, xoff+sh.x2, yoff-sh.y2 );
          }
          out.write( sw41.getBuffer().toString() );
        }
	out.write("}\n\n");
        out.flush();
      }

      out.write("set XVIsketchlines {\n");
      for ( ICanvasCommand cmd : plot.getCommands() ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath path = (DrawingPath)cmd;
        StringWriter sw5 = new StringWriter();
        PrintWriter pw5  = new PrintWriter(sw5);
        if ( path.mType == DrawingPath.DRAWING_PATH_STATION ) {
          toXvi( pw5, (DrawingStationPath)path, xoff, yoff );
        } else if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
          toXvi( pw5, (DrawingLinePath)path, xoff, yoff );
        } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
          toXvi( pw5, (DrawingAreaPath) path, xoff, yoff );
        } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
          DrawingPointPath point = (DrawingPointPath)path;
          if ( point.mPointType == BrushManager.mPointLib.mPointSectionIndex ) {
            float xx = xoff+point.cx;
            float yy = yoff-point.cy;
            // pw5.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%d\" ", xx, yy, RADIUS );
            // pw5.format(" style=\"fill:grey;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgLabelStroke );

            // GET_OPTION option: -scrap survey-xx#
            String scrapname = point.getOption("-scrap");
            if ( scrapname != null ) {
              String scrapfile = scrapname + ".tdr";
              // String scrapfile = point.mOptions.substring( 7 ) + ".tdr";

              // open file survey-xx#.tdr and convert it to svg
              tdrToXvi( pw5, scrapfile, xx, yy, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y );
            }
            // pw5.format("</g>\n");
          } else {
            toXvi( pw5, point, xoff, yoff );
          }
        }
        out.write( sw5.getBuffer().toString() );
        out.flush();
      }
      out.write("}\n\n");
      out.flush();

    } catch ( IOException e ) {
      // FIXME
      TDLog.Error( "XVI io-exception " + e.getMessage() );
    }
  }

  static private void toXvi( PrintWriter pw, DrawingStationName name, float xoff, float yoff )
  {
    pw.format(Locale.US, "  { %.2f %.2f %s }\n", xoff+name.cx, yoff-name.cy, name.name() );
  }

  static private void toXvi( PrintWriter pw, DrawingStationPath st, float xoff, float yoff )
  {
    pw.format(Locale.US, "  { %.2f %.2f %s }\n", xoff+st.cx, yoff-st.cy, st.name() );
  }

  static private void toXviPointLine( PrintWriter pw, DrawingPointLinePath lp, String color, float xoff, float yoff, boolean closed )
  {
    float bezier_step = TDSetting.getBezierStep();
    LinePoint p = lp.mFirst;
    float x0 = xoff+p.x;
    float y0 = yoff-p.y;
    float x00 = x0;
    float y00 = y0;
    for ( p = p.mNext; p != null; p = p.mNext ) { 
      pw.format(Locale.US, "  { %s %.2f %.2f", color, x0, y0 );
      float x3 = xoff+p.x;
      float y3 = yoff-p.y;
      if ( p.has_cp ) { // FIXME this converts the cubic with a thickly interpolated polyline
        float x1 = xoff + p.x1;
        float y1 = yoff - p.y1;
        float x2 = xoff + p.x2;
        float y2 = yoff - p.y2;
        pw.format(Locale.US, " %.2f %.2f %.2f %.2f", x1, y1, x2, y2 );
      } 
      pw.format(Locale.US, " %.2f %.2f }\n", x3, y3 );
      x0 = x3;
      y0 = y3;
    }
    if ( closed ) { 
      if ( x0 != x00 || y0 != y00 ) {
        pw.format(Locale.US, "  { %s %.2f %.2f %.2f %.2f }\n", color, x0, y0, x00, y00 );
      }
    }
  }

  // colors: black, gray, green, blue, red, orange, brown, ...
  static private void toXvi( PrintWriter pw, DrawingLinePath line, float xoff, float yoff ) 
  {
    String color = "brown";
    int ltype = line.lineType();
    if ( ltype == BrushManager.mLineLib.mLineWallIndex )         { color = "red"; }
    else if ( ltype == BrushManager.mLineLib.mLineSectionIndex ) { color = "gray"; }
    else if ( ltype == BrushManager.mLineLib.mLineSlopeIndex )   { color = "orange"; }
    toXviPointLine( pw, line, color, xoff, yoff, line.isClosed() );
  }

  static private void toXvi( PrintWriter pw, DrawingAreaPath area, float xoff, float yoff )
  {
    String color = "black";
    toXviPointLine( pw, area, color, xoff, yoff, true ); // area borders are closed
  }

  static private void toXvi( PrintWriter pw, DrawingPointPath point, float xoff, float yoff )
  {
    int idx = point.mPointType;
    if ( idx == BrushManager.mPointLib.mPointSectionIndex ) return;

    float xof = xoff + point.cx;
    float yof = yoff - point.cy;

    String color = "blue";
    String name = BrushManager.mPointLib.getSymbolThName( idx );
    SymbolPoint sp = (SymbolPoint)BrushManager.mPointLib.getSymbolByIndex( idx );
    String path = ( sp == null )? null : sp.mXvi;
    if ( path == null ) {
      float x1 = xof - 5;
      float y1 = yof - 5;
      float x2 = xof + 5;
      float y2 = yof + 5;
      pw.format("  { %s %.2f %.2f %.2f %.2f }\n", color, x1, y1, x2, y2 );
      pw.format("  { %s %.2f %.2f %.2f %.2f }\n", color, x2, y1, x1, y2 );
    } else {
      float a  = (float)point.mOrientation;
      float ca = TDMath.cosd( a );
      float sa = TDMath.sind( a );
      float x0;
      float y0;
      float x00, y00, x01, y01, x02, y02, x03, y03;
      String[] vals = path.split(" ");
      int len = vals.length;
      // L x0 y0 x1 y1
      // C x0 y0 x1 y1 x2 y2 x3 y3
      for ( int k=0; k<len; ++k ) {
        if ( vals[k].equals("L") ) {
	  ++k; x0 = Float.parseFloat(vals[k]) * 20;
	  ++k; y0 = Float.parseFloat(vals[k]) * 20;
	  x00 = x0 * ca - y0 * sa;
	  y00 = y0 * ca + x0 * sa;
	  ++k; x0 = Float.parseFloat(vals[k]) * 20;
	  ++k; y0 = Float.parseFloat(vals[k]) * 20;
	  x01 = x0 * ca - y0 * sa;
	  y01 = y0 * ca + x0 * sa;
	  pw.format(Locale.US, "  { %s %.2f %.2f %.2f %.2f }\n", color, xof+x00, yof-y00, xof+x01, yof-y01 );
	} else if ( vals[k].equals("C") ) {
	  ++k; x0 = Float.parseFloat(vals[k]) * 20;
	  ++k; y0 = Float.parseFloat(vals[k]) * 20;
	  x00 = x0 * ca - y0 * sa;
	  y00 = y0 * ca + x0 * sa;
	  ++k; x0 = Float.parseFloat(vals[k]) * 20;
	  ++k; y0 = Float.parseFloat(vals[k]) * 20;
	  x01 = x0 * ca - y0 * sa;
	  y01 = y0 * ca + x0 * sa;
	  ++k; x0 = Float.parseFloat(vals[k]) * 20;
	  ++k; y0 = Float.parseFloat(vals[k]) * 20;
	  x02 = x0 * ca - y0 * sa;
	  y02 = y0 * ca + x0 * sa;
	  ++k; x0 = Float.parseFloat(vals[k]) * 20;
	  ++k; y0 = Float.parseFloat(vals[k]) * 20;
	  x03 = x0 * ca - y0 * sa;
	  y03 = y0 * ca + x0 * sa;
	  pw.format(Locale.US, "  { %s %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f }\n", color, xof+x00, yof-y00, xof+x01, yof-y01, xof+x02, yof-y02, xof+x03, yof-y03 );
	} else {
	  TDLog.Error("error xvi format point " + name );
	}
      }
    }

    // if ( name.equals("label") ) {
    //   DrawingLabelPath label = (DrawingLabelPath)point;
    //   pw.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff-point.cy );
    //   pw.format(" style=\"fill:black;stroke:black;stroke-width:%.2f\">%s</text>\n", TDSetting.mSvgLabelStroke, label.mPointText );
    // } else {
    //   SymbolPoint sp = (SymbolPoint)BrushManager.mPointLib.getSymbolByIndex( idx );
    //   if ( sp != null ) {
    //     pw.format(Locale.US, "<g transform=\"translate(%.2f,%.2f),scale(%d),rotate(%.2f)\" \n", 
    //       xoff+point.cx, yoff-point.cy, POINT_SCALE, point.mOrientation );
    //     pw.format(" style=\"fill:none;stroke:%s;stroke-width:%.2f\" >\n", color, TDSetting.mSvgPointStroke );
    //     pw.format("%s\n", sp.mSvg );
    //     pw.format("</g>\n");
    //   } else {
    //     pw.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%d\" ", xoff+point.cx, yoff-point.cy, POINT_RADIUS );
    //     pw.format(" style=\"fill:none;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgPointStroke );
    //   }
    // }
  }

  static private void tdrToXvi( PrintWriter pw, String scrapfile, float dx, float dy, float xoff, float yoff )
  {
    try {
      // TDLog.Log( TDLog.LOG_IO, "trd to svg. scrap file " + scrapfile );
      FileInputStream fis = new FileInputStream( TDPath.getTdrFile( scrapfile ) );
      BufferedInputStream bfis = new BufferedInputStream( fis );
      DataInputStream dis = new DataInputStream( bfis );
      int version = DrawingIO.skipTdrHeader( dis );
      // Log.v("DistoX", "tdr to svg delta " + dx + " " + dy + " Offset " + xoff + " " + yoff );

      DrawingPath path = null;
      boolean done = false;
      while ( ! done ) {
        int what = dis.read();
        switch ( what ) {
          case 'P':
            path = DrawingPointPath.loadDataStream( version, dis, dx, dy, null );
            if ( path != null) toXvi( pw, (DrawingPointPath)path, xoff, yoff );
            break;
          case 'T':
            path = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
            if ( path != null) toXvi( pw, (DrawingLabelPath)path, xoff, yoff );
            break;
          case 'L':
            path = DrawingLinePath.loadDataStream( version, dis, dx, dy, null );
            if ( path != null) toXvi( pw, (DrawingLinePath)path, xoff, yoff );
            break;
          case 'A':
            path = DrawingAreaPath.loadDataStream( version, dis, dx, dy, null );
            if ( path != null) toXvi( pw, (DrawingAreaPath)path, xoff, yoff );
            break;
          case 'U':
            path = DrawingStationPath.loadDataStream( version, dis ); // consume DrawingStationName data
            break;
          case 'X':
            path = DrawingStationName.loadDataStream( version, dis ); // consume DrawingStationName data
            break;
          case 'F':
            done = true;
            break;
        }
      }
    } catch ( FileNotFoundException e ) { // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

}

