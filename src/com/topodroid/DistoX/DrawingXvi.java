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

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.num.TDNum;
import com.topodroid.num.NumStation;
import com.topodroid.prefs.TDSetting;

import android.util.Log;

import java.util.Locale;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.RectF;

class DrawingXvi
{
  // FIXME station scale is 0.3
  static final private int POINT_SCALE  = 10;
  static final private int POINT_RADIUS = 10;
  static final private int RADIUS = 3;
  static final private float CELL  = 20; // 1 meter
  static final private float POINT = 20;

  static final private int[] CHAR_any = { 2,2,1,4, 1,4,0,2, 0,2,1,0, 1,0,2,2 };
  static final private int[] CHAR_underscore = { 0,0,2,0 };
  static final private int[] CHAR_plus = { 1,1,1,3, 0,2,2,2 };
  static final private int[] CHAR_minus = { 0,2,2,2 };
  static final private int[] CHAR_question = { 1,0,1,2, 0,3,0,4, 0,4,2,4, 1,2,2,3, 2,3,2,4 };
  static final private int[] CHAR_slash = { 0,0,2,4 };
  static final private int[] CHAR_less = { 0,2,2,3, 0,2,2,1 };
  static final private int[] CHAR_more = { 0,3,2,2, 0,1,2,2 };

  static final private int[][] GLYPH_AZ = {
    { 0,0,0,4, 0,4,2,4, 2,4,2,0, 0,2,2,2 }, // A
    { 0,0,0,4, 0,4,2,3, 0,2,2,2, 0,0,2,0, 2,3,2,0 },
    { 0,0,0,4, 0,4,2,4, 0,0,2,0 },
    { 0,0,0,4, 0,4,2,3, 2,3,2,0, 0,0,2,0 },
    { 0,0,0,4, 0,4,2,4, 0,2,2,2, 0,0,2,0 }, // E
    { 0,0,0,4, 0,4,2,4, 0,2,2,2 },
    { 0,0,0,4, 0,4,2,4, 0,0,2,0, 2,0,2,2, 1,2,2,2 }, // G
    { 0,0,0,4, 0,2,2,2, 2,0,2,4},
    { 0,0,0,4 },
    { 0,0,1,0, 1,0,1,4, 0,4,2,4 },  // J
    { 0,0,0,4, 0,2,2,3, 0,2,2,0 },
    { 0,0,0,4, 0,0,2,0 },
    { 0,0,0,4, 0,4,1,2, 1,2,2,4, 2,4,2,0 },
    { 0,0,0,4, 0,4,2,0, 2,4,2,0 },
    { 0,0,0,4, 0,4,2,4, 0,0,2,0, 2,4,2,0 }, // O
    { 0,0,0,4, 0,4,2,4, 0,2,2,2, 2,4,2,2, },
    { 0,0,0,4, 0,4,2,4, 0,0,1,0, 1,0,2,1, 2,1,2,4, 1,1,2,0 },
    { 0,0,0,4, 0,4,2,4, 2,4,2,2, 0,2,2,2, 0,2,2,0 },
    { 0,2,0,4, 0,4,2,4, 0,2,2,2, 0,0,2,0, 2,0,2,2 }, // S
    { 1,0,1,4, 0,4,2,4 },
    { 0,0,0,4, 0,0,2,0, 2,4,2,0 },
    { 0,4,1,0, 1,0,2,4 },
    { 0,0,0,4, 0,0,1,2, 1,2,2,0, 2,0,2,4 }, // W
    { 0,0,2,4, 0,4,2,0 },
    { 0,4,1,2, 1,0,1,2, 1,2,2,4 },
    { 0,4,2,4, 0,0,2,4, 0,0,2,0 }
  };
  static final private int[][] GLYPH_01 = {
    { 0,0,0,4, 0,4,2,4, 0,0,2,0, 2,4,2,0 }, // 0
    { 1,0,1,4, 1,3,1,4 },
    { 2,4,2,3, 0,0,0,1, 0,4,2,4, 0,1,2,3, 0,0,2,0 }, // 2
    { 0,4,2,4, 0,2,2,2, 0,0,2,0, 2,4,2,0 },
    { 1,4,0,1, 1,0,1,4, 0,1,2,1 },
    { 0,2,1,4, 1,4,2,4, 0,2,2,2, 0,0,2,0, 2,0,2,2 }, // 5
    { 0,0,0,4, 0,2,2,2, 0,0,2,0, 2,0,2,2 },
    { 0,4,2,4, 0,0,2,4 },
    { 0,0,0,4, 0,4,2,4, 0,2,2,2, 0,0,2,0, 2,4,2,0},
    { 0,2,2,2, 0,4,2,4, 0,2,0,4, 2,4,2,0 } // 9
  };

  // X_orig Y_orig X_cell 0.0 0.0 Y_cell X_nr Y_nr
  // private static void printXviGrid( BufferedWriter out, float x1, float y1, float x2, float y2, float xoff, float yoff )
  private static void printXviGrid( BufferedWriter out, XviBBox bb, float xoff, float yoff )
  {
    int nx = (int)((bb.xmax - bb.xmin)/CELL);
    int ny = (int)((bb.ymax - bb.ymin)/CELL);
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.US, "set XVIgrid { %.2f %.2f %.2f 0.0 0.0 %.2f %d %d }\n\n",
              TDSetting.mToTherion*(xoff+bb.xmin), TDSetting.mToTherion*(yoff-bb.ymax), TDSetting.mToTherion*CELL, TDSetting.mToTherion*CELL, nx, ny );
    try {
      out.write( sw.getBuffer().toString() );
      out.flush();
    } catch ( IOException e ) {
      TDLog.Error( "XVI grid io-exception " + e.getMessage() );
    }
  }

  static void writeXvi( BufferedWriter out, TDNum num, DrawingCommandManager plot, long type )
  {
    String wall_group = BrushManager.getLineWallGroup( );

    // origin is always at (0,0)
    // NumStation origin = num.getOrigin();
    // Log.v("DistoX-XVI", "origin " + origin.name + " " + origin.e + " " + origin.s + " " + origin.h + " " + origin.v + " type " + type );

    int handle = 0;
    XviBBox bb = new XviBBox( plot );

    float xoff = 0; // xmin; // offset
    float yoff = 0; // ymin;

    // Log.v("DistoX-XVI", "offset " + xoff + " " + yoff );

    try {
      // header
      out.write( "set XVIgrids {1.0 m}\n\n");

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
      
      // centerline data
      // if ( PlotInfo.isSketch2D( type ) ) { 
        // FIXME OK PROFILE
        // FIXME OK X-SECTIONS

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

	// GRID always necessary
        // if ( TDSetting.mSvgGrid ) {
          printXviGrid( out, bb, xoff, yoff );
        // }

	// SHOTS
        out.write("set XVIshots {\n");
        StringWriter sw4 = new StringWriter();
        PrintWriter pw4  = new PrintWriter(sw4);
        for ( DrawingPath sh : plot.getLegs() ) {
          DBlock blk = sh.mBlock;
          if ( blk == null ) continue;
          pw4.format(Locale.US, "  { %.2f %.2f %.2f %.2f }\n",
                     TDSetting.mToTherion*(xoff+sh.x1), TDSetting.mToTherion*(yoff-sh.y1), TDSetting.mToTherion*(xoff+sh.x2), TDSetting.mToTherion*(yoff-sh.y2) );
        }
        out.write( sw4.getBuffer().toString() );
        out.flush();

        if ( TDSetting.mSvgSplays ) {
          StringWriter sw41 = new StringWriter();
          PrintWriter pw41  = new PrintWriter(sw41);
          for ( DrawingPath sh : plot.getSplays() ) {
            DBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            pw41.format(Locale.US, "  { %.2f %.2f %.2f %.2f }\n",
                        TDSetting.mToTherion*(xoff+sh.x1), TDSetting.mToTherion*(yoff-sh.y1), TDSetting.mToTherion*(xoff+sh.x2), TDSetting.mToTherion*(yoff-sh.y2) );
          }
          out.write( sw41.getBuffer().toString() );
        }
	out.write("}\n\n");
        out.flush();
      // } else {
      //   printXviGrid( out, bb, xoff, yoff );
      // }

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
          if ( BrushManager.isPointSection( point.mPointType ) ) {
            float xx = xoff+point.cx;
            float yy = yoff+point.cy;
	    // Log.v("DistoXX", " yoff " + yoff + " cy " + point.cy + " yy " + yy );
            // pw5.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%d\" ", xx, yy, RADIUS );
            // pw5.format(Locale.US, " style=\"fill:grey;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgLabelStroke );

	    if ( TDSetting.mAutoXSections ) {
              // GET_OPTION option: -scrap survey-xx#
              String scrapname = point.getOption("-scrap");
              if ( scrapname != null ) {
                String scrapfile = scrapname + ".tdr";
                // String scrapfile = point.mOptions.substring( 7 ) + ".tdr";

                // open file survey-xx#.tdr and convert it to xvi
                tdrToXvi( pw5, scrapfile, xx, yy, -DrawingUtil.CENTER_X,  DrawingUtil.CENTER_Y );
              }
              // pw5.format("</g>\n");
	    }
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

  static private void toXvi( PrintWriter pw, DrawingStationName st, float xoff, float yoff )
  {
    // Log.v("DistoX-XVI", st.getName() + " " + st.cx + " " + st.cy + " off " + xoff + " " + yoff);
    pw.format(Locale.US, "  { %.2f %.2f %s }\n", TDSetting.mToTherion*(xoff+st.cx), TDSetting.mToTherion*(yoff-st.cy), st.getName() );
  }

  static private void toXvi( PrintWriter pw, DrawingStationPath sp, float xoff, float yoff )
  {
    pw.format(Locale.US, "  { %.2f %.2f %s }\n", TDSetting.mToTherion*(xoff+sp.cx), TDSetting.mToTherion*(yoff-sp.cy), sp.name() );
  }

  static private void toXviPointLine( PrintWriter pw, DrawingPointLinePath lp, String color, float xoff, float yoff, boolean closed )
  {
    float bezier_step = TDSetting.getBezierStep();
    LinePoint p = lp.mFirst;
    float x0 = TDSetting.mToTherion*(xoff+p.x);
    float y0 = TDSetting.mToTherion*(yoff-p.y);
    float x00 = x0;
    float y00 = y0;
    for ( p = p.mNext; p != null; p = p.mNext ) { 
      pw.format(Locale.US, "  { %s %.2f %.2f", color, x0, y0 );
      float x3 = TDSetting.mToTherion*(xoff+p.x);
      float y3 = TDSetting.mToTherion*(yoff-p.y);
      if ( p.has_cp ) { // FIXME this converts the cubic with a thickly interpolated polyline
        float x1 = TDSetting.mToTherion*(xoff + p.x1);
        float y1 = TDSetting.mToTherion*(yoff - p.y1);
        float x2 = TDSetting.mToTherion*(xoff + p.x2);
        float y2 = TDSetting.mToTherion*(yoff - p.y2);
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
    if ( BrushManager.isLineWall( ltype ) )         { color = "red"; }
    else if ( BrushManager.isLineSection( ltype ) ) { color = "gray"; }
    else if ( BrushManager.isLineSlope( ltype ) )   { color = "orange"; }
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
    float xof = xoff + point.cx;
    float yof = yoff - point.cy;
    float x1, y1, x2, y2;

    if ( BrushManager.isPointSection( idx ) ) return;
    if ( BrushManager.isPointLabel( idx ) ) {
      String label = point.getPointText().toUpperCase();
      int len = label.length();
      int pos = 0;
      for ( int k = 0; k < len; ++k ) {
        char ch = label.charAt( k );
	int[] glyph = CHAR_any;
	if ( ch >= 'A' && ch <= 'Z' ) {
	  glyph = GLYPH_AZ[ ch - 'A' ];
	} else if ( ch >= '0' && ch <= '9' ) {
	  glyph = GLYPH_01[ ch - '0' ];
	} else if ( ch == '-' ) {
	  glyph = CHAR_minus;
	} else if ( ch == '+' ) {
	  glyph = CHAR_plus;
	} else if ( ch == '?' ) {
	  glyph = CHAR_question;
	} else if ( ch == '_' ) {
	  glyph = CHAR_underscore;
	} else if ( ch == '/' ) {
	  glyph = CHAR_slash;
	} else if ( ch == '>' ) {
	  glyph = CHAR_more;
	} else if ( ch == '<' ) {
	  glyph = CHAR_less;
	}
	int j = 0;
	while ( j < glyph.length ) {
          x1 = TDSetting.mToTherion*(xof + 3 * (pos + glyph[j])); ++j;
          y1 = TDSetting.mToTherion*(yof + 3 * glyph[j] ); ++j;
          x2 = TDSetting.mToTherion*(xof + 3 * (pos + glyph[j])); ++j;
          y2 = TDSetting.mToTherion*(yof + 3 * glyph[j] ); ++j;
          pw.format(Locale.US, "  { black %.2f %.2f %.2f %.2f }\n", x1, y1, x2, y2 );
	}
	pos += 1 + glyph[j-2];
      }	
      return;
    }

    String color = "blue";
    String name = point.getThName( );
    SymbolPoint sp = (SymbolPoint)BrushManager.getPointByIndex( idx );
    String path = ( sp == null )? null : sp.getXvi();
    if ( path == null ) {
      x1 = TDSetting.mToTherion*(xof - 5);
      y1 = TDSetting.mToTherion*(yof - 5);
      x2 = TDSetting.mToTherion*(xof + 5);
      y2 = TDSetting.mToTherion*(yof + 5);
      pw.format(Locale.US, "  { %s %.2f %.2f %.2f %.2f }\n", color, x1, y1, x2, y2 );
      pw.format(Locale.US, "  { %s %.2f %.2f %.2f %.2f }\n", color, x2, y1, x1, y2 );
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
	  ++k; x0 = Float.parseFloat(vals[k]) * POINT;
	  ++k; y0 = Float.parseFloat(vals[k]) * POINT;
	  x00 = x0 * ca - y0 * sa;
	  y00 = y0 * ca + x0 * sa;
	  ++k; x0 = Float.parseFloat(vals[k]) * POINT;
	  ++k; y0 = Float.parseFloat(vals[k]) * POINT;
	  x01 = x0 * ca - y0 * sa;
	  y01 = y0 * ca + x0 * sa;
	  pw.format(Locale.US, "  { %s %.2f %.2f %.2f %.2f }\n",
                    color, TDSetting.mToTherion*(xof+x00), TDSetting.mToTherion*(yof-y00), TDSetting.mToTherion*(xof+x01), TDSetting.mToTherion*(yof-y01) );
	} else if ( vals[k].equals("C") ) {
	  ++k; x0 = Float.parseFloat(vals[k]) * POINT;
	  ++k; y0 = Float.parseFloat(vals[k]) * POINT;
	  x00 = x0 * ca - y0 * sa;
	  y00 = y0 * ca + x0 * sa;
	  ++k; x0 = Float.parseFloat(vals[k]) * POINT;
	  ++k; y0 = Float.parseFloat(vals[k]) * POINT;
	  x01 = x0 * ca - y0 * sa;
	  y01 = y0 * ca + x0 * sa;
	  ++k; x0 = Float.parseFloat(vals[k]) * POINT;
	  ++k; y0 = Float.parseFloat(vals[k]) * POINT;
	  x02 = x0 * ca - y0 * sa;
	  y02 = y0 * ca + x0 * sa;
	  ++k; x0 = Float.parseFloat(vals[k]) * POINT;
	  ++k; y0 = Float.parseFloat(vals[k]) * POINT;
	  x03 = x0 * ca - y0 * sa;
	  y03 = y0 * ca + x0 * sa;
	  pw.format(Locale.US, "  { %s %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f }\n", color,
                    TDSetting.mToTherion*(xof+x00), TDSetting.mToTherion*(yof-y00), TDSetting.mToTherion*(xof+x01), TDSetting.mToTherion*(yof-y01),
                    TDSetting.mToTherion*(xof+x02), TDSetting.mToTherion*(yof-y02), TDSetting.mToTherion*(xof+x03), TDSetting.mToTherion*(yof-y03) );
	} else {
	  TDLog.Error("error xvi format point " + name );
	}
      }
    }

    // if ( name.equals("label") ) {
    //   DrawingLabelPath label = (DrawingLabelPath)point;
    //   pw.format(Locale.US, "<text x=\"%.2f\" y=\"%.2f\" ", xoff+point.cx, yoff-point.cy );
    //   pw.format(Locale.US, " style=\"fill:black;stroke:black;stroke-width:%.2f\">%s</text>\n", TDSetting.mSvgLabelStroke, label.mPointText );
    // } else {
    //   SymbolPoint sp = (SymbolPoint)BrushManager.getPointByIndex( idx );
    //   if ( sp != null ) {
    //     pw.format(Locale.US, "<g transform=\"translate(%.2f,%.2f),scale(%d),rotate(%.2f)\" \n", 
    //       xoff+point.cx, yoff-point.cy, POINT_SCALE, point.mOrientation );
    //     pw.format(Locale.US, " style=\"fill:none;stroke:%s;stroke-width:%.2f\" >\n", color, TDSetting.mSvgPointStroke );
    //     pw.format("%s\n", sp.mSvg );
    //     pw.format("</g>\n");
    //   } else {
    //     pw.format(Locale.US, "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%d\" ", xoff+point.cx, yoff-point.cy, POINT_RADIUS );
    //     pw.format(Locale.US, " style=\"fill:none;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgPointStroke );
    //   }
    // }
  }

  static private void tdrToXvi( PrintWriter pw, String scrapfile, float dx, float dy, float xoff, float yoff )
  {
    // TDLog.Log( TDLog.LOG_IO, "trd to xvi. scrap file " + scrapfile );
    // Log.v( "DistoXX", "trd to xvi. scrap file " + scrapfile + " shift " + dx + " " + dy + " offset " + xoff + " " + yoff );
    try {
      FileInputStream fis = new FileInputStream( TDPath.getTdrFile( scrapfile ) );
      BufferedInputStream bfis = new BufferedInputStream( fis );
      DataInputStream dis = new DataInputStream( bfis );
      int version = DrawingIO.skipTdrHeader( dis );
      // Log.v("DistoX", "tdr to xvi delta " + dx + " " + dy + " Offset " + xoff + " " + yoff );

      DrawingPath path = null;
      boolean done = false;
      while ( ! done ) {
        int what = dis.read();
        switch ( what ) {
          case 'P':
            path = DrawingPointPath.loadDataStream( version, dis, dx, dy /*, null */ );
            if ( path != null) toXvi( pw, (DrawingPointPath)path, xoff, yoff );
            break;
          case 'T':
            path = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
            if ( path != null) toXvi( pw, (DrawingLabelPath)path, xoff, yoff );
            break;
          case 'L':
            path = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, null */ );
            if ( path != null) toXvi( pw, (DrawingLinePath)path, xoff, yoff );
            break;
          case 'A':
            path = DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, null*/ );
            if ( path != null) toXvi( pw, (DrawingAreaPath)path, xoff, yoff );
            break;
          case 'J':
            /* path = */ DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
            break;
          case 'U':
            /* path = */ DrawingStationPath.loadDataStream( version, dis ); // consume DrawingStationName data
            break;
          case 'X':
            /* path = */ DrawingStationName.loadDataStream( version, dis ); // consume DrawingStationName data
            break;
          case 'Y':
            /* path = */ DrawingPhotoPath.loadDataStream( version, dis, dx, dy );
            break;
          case 'Z':
            /* path = */ DrawingAudioPath.loadDataStream( version, dis, dx, dy );
            break;
          // case 'G':
          //   DrawingFixedName.loadDataStream( version, dis ); // consume DrawingFixedName data
          //   break;
          case 'F':
            done = true;
            break;
	  default:
	    TDLog.Error("TDR2XVI Error. unexpected code=" + what );
	    return;
        }
      }
    } catch ( FileNotFoundException e ) { // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

}

