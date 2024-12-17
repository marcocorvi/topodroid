/* @file DrawingSvgBase.java
 *
 * @author marco corvi
 * @date oct 2019
 *
 * @brief TopoDroid drawing: svg export base class
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.svg;

import com.topodroid.TDX.DrawingUtil;
import com.topodroid.TDX.IDrawingLink;
import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
import com.topodroid.math.Point2D;
import com.topodroid.math.BezierCurve;
import com.topodroid.prefs.TDSetting;

import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.DrawingStationUser;
import com.topodroid.TDX.DrawingStationName;
import com.topodroid.TDX.DrawingPointPath;
import com.topodroid.TDX.DrawingPointLinePath;
import com.topodroid.TDX.DrawingLinePath;
import com.topodroid.TDX.DrawingAreaPath;
import com.topodroid.TDX.DrawingLabelPath;
import com.topodroid.TDX.DrawingPath;
import com.topodroid.TDX.DrawingAudioPath;
import com.topodroid.TDX.DrawingPhotoPath;
import com.topodroid.TDX.DrawingSpecialPath;
import com.topodroid.TDX.DrawingIO;
import com.topodroid.TDX.DrawingCommandManager;
import com.topodroid.TDX.LinePoint;
import com.topodroid.TDX.BrushManager;
import com.topodroid.TDX.SymbolPoint;
import com.topodroid.TDX.SymbolLibrary;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.ICanvasCommand;

import java.util.Collections;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
// import java.util.HashMap;

// import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

/* Inkscape units
 * - The root element can have width and height with units, which with the proper view-box
 *   determines the scale for the drawing (real-world value of the SVG user units)
 * - User-unit: unit length in the current user coord system
 * - Unit-id: can be mm, cm, in, pt, px, pc ...
 * - Scale-factor: (from SVG root width/height and view-box) maps user-units in the doc to real-world units
 *
 * Example <svg width="100cm" height="60cm" viewBox="0 0, 50 30">
 *   the nominal size of the drawing is 100x60 cm
 *   if the unit-id is missing the user-unit is intended
 *   the drawing is 50x30 user-units ( 1 user-unit = 2 cm )
 *     width="10"  -> 10 user-units = 2 in (at nominal drawing size)
 *     width="1in" -> 96 user-units = 19.2 in
 *   the user-unit can be scaled by a transform="scale(0.75,0.75)" -> user-unit is scaled to 3/4
 *   
 * The scale factor is 0.2 in/user-unit ( therefore 10 user-unit = 2 in, above ) 
 * Initial user-unit is 96 px / in
 * has a CSS value of 96 pxl / inch ( 1 in = 25.4 mm )
 *
 */
public class DrawingSvgBase
{
  // FIXME station scale is 0.3
  static final protected int POINT_SCALE  = 10;
  static final protected int POINT_RADIUS = 10;
  static final protected int RADIUS = 3;
  // float SCALE_FIX = util.SCALE_FIX; // 20.0f

  protected static final String end_grp = "</g>\n";
  protected static final String end_svg = "</svg>\n";

  protected static final String group_mode_open  = " inkscape:groupmode=\"layer\" i:layer=\"yes\" >\n";
  protected static final String group_mode_close = " inkscape:groupmode=\"layer\" i:layer=\"yes\" />\n";

  private final static String ALL = "all";
  
  protected class XSection
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

  /** print a point X-Y
   * @param pw   writer
   * @param prefix   string prefix
   * @param x        X coord
   * @param y        Y coord
   */
  static protected void printPoint( PrintWriter pw, String prefix, float x, float y )
  {
    pw.format(Locale.US, "%s %.2f %.2f", prefix, x*TDSetting.mToSvg, y*TDSetting.mToSvg );
  }

  /** print a point X-Y with x, y attribute tags
   * @param pw   writer
   * @param prefix   string prefix
   * @param x        X coord
   * @param y        Y coord
   */
  static protected void printPointWithXY( PrintWriter pw, String prefix, float x, float y )
  {
    pw.format(Locale.US, "%s x=\"%.2f\" y=\"%.2f\" ", prefix, x*TDSetting.mToSvg, y*TDSetting.mToSvg );
  }

  /** print a point X-Y with cx, cy attribute tags
   * @param pw   writer
   * @param prefix   string prefix
   * @param x        X coord
   * @param y        Y coord
   */
  static protected void printPointWithCXCY( PrintWriter pw, String prefix, float x, float y )
  {
    pw.format(Locale.US, "%s cx=\"%.2f\" cy=\"%.2f\" ", prefix, x*TDSetting.mToSvg, y*TDSetting.mToSvg );
  }

  /** print a segment with the closing angle-bracket
   * @param pw   writer
   * @param x1    X coord of the first point
   * @param y1    Y coord of the first point
   * @param x2    X coord of the second point
   * @param y2    Y coord of the second point
   */
  static protected void printSegmentWithClose( PrintWriter pw, float x1, float y1, float x2, float y2 )
  {
    pw.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />",
      x1*TDSetting.mToSvg, y1*TDSetting.mToSvg, x2*TDSetting.mToSvg, y2*TDSetting.mToSvg );
  }

  /** print a transform matrix
   * @param pw   writer
   * @param c    cosine
   * @param s    sine
   * @param x    x translate
   * @param y    y translate
   */
  static protected void printMatrix( PrintWriter pw, float c, float s, float x, float y )
  {
    pw.format(Locale.US, " transform=\"matrix(%.2f,%.2f,%.2f,%.2f,%.2f,%.2f)\"", c, s, -s, c, x*TDSetting.mToSvg, y*TDSetting.mToSvg );
  }

  /** @return the string hex RGB-color of a path
   * @param path   path
   */
  static protected String pathToColor( DrawingPath path )
  {
    int color = path.color();
    int red = ( color >> 16 ) & 0xff;
    int grn = ( color >>  8 ) & 0xff;
    int blu = ( color       ) & 0xff;
    if ( red > 0xcc && grn > 0xcc && blu > 0xcc ) return "#cccccc"; // cut-off white
    return String.format( "#%02x%02x%02x", red, grn, blu );
  }

  protected ArrayList< XSection > getXSections( DrawingCommandManager plot, float xoff, float yoff )
  {
    ArrayList< XSection > xsections = new ArrayList<>();
    if ( TDSetting.mAutoXSections ) {
      for ( ICanvasCommand cmd : plot.getCommands() ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath path = (DrawingPath)cmd;
        if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
          DrawingPointPath point = (DrawingPointPath)path;
          if ( BrushManager.isPointSection( point.mPointType ) ) {
            float xx = xoff+point.cx;
            float yy = yoff+point.cy;
            String scrapname = TDUtil.replacePrefix( TDInstance.survey, point.getOption(TDString.OPTION_SCRAP) );
            if ( scrapname != null ) {
              String scrapfile = scrapname + ".tdr";
              xsections.add( new XSection( scrapfile, xx, yy ) );
            }
          }
        }
      }
    }
    return xsections;
  }

  static protected void printSvgGrid( BufferedWriter out, List< DrawingPath > grid, String id, String color, float opacity, 
                                      float xoff, float yoff, float xmin, float xmax, float ymin, float ymax )
  {
    if ( grid != null && grid.size() > 0 ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.format(Locale.US, "<g id=\"%s\"\n", id );
      pw.format(Locale.US, " style=\"fill:none;stroke-opacity:%.1f;stroke-width=%.2f;stroke:#666666\" >\n", opacity, TDSetting.mSvgGridStroke );
      for ( DrawingPath p : grid ) {
        float x1 = xmin;
        float x2 = xmax;
        float y1 = ymin;
        float y2 = ymax;
        if ( p.x1 == p.x2 ) {
          if (p.x1 < xmin || p.x2 > xmax ) continue;
          x1 = x2 = p.x1;
        } else {
          if (p.y1 < ymin || p.y2 > ymax ) continue;
          y1 = y2 = p.y1;
        }
        pw.format(Locale.US, "  <path stroke-width=\"%.2f\" stroke=\"#%s\" d=\"", TDSetting.mSvgGridStroke, color );
        printPoint( pw, "M",  xoff+x1, yoff+y1 );
        printPoint( pw, " L", xoff+x2, yoff+y2 );
        pw.format("\" />\n");
      }
      pw.format( end_grp );
      try {
        out.write( sw.getBuffer().toString() );
        out.flush();
      } catch ( IOException e ) {
        TDLog.e( "SVG grid io-exception " + e.getMessage() );
      }
    }
  }

  static protected void toSvgLabel( PrintWriter pw, DrawingLabelPath label, String color, float xoff, float yoff )
  {
    String name = label.getThName();
    float scale = label.getScaleValue();
    // TDLog.v("label: " + name + " " + scale + " text " + label.mPointText );
    pw.format("<!-- label %s -->\n", name );
    if ( name.equals( SymbolLibrary.LABEL ) ) {
      printPointWithXY( pw, "<text", xoff+label.cx, yoff+label.cy );
      pw.format(Locale.US, " font-size=\"%.1f\"", TDSetting.mSvgLabelSize * scale );
      pw.format(Locale.US, " style=\"fill:black;stroke:black;stroke-width:%.2f\">%s</text>\n", TDSetting.mSvgLabelStroke, label.mPointText );
    }
  }
  // <text
  //    style="font-size:6px;font-family:ArialMT;fill:#0000ff"
  //    x="228.089"
  //    y="562.5"
  //    id="text32">0</text>

  /** export a line item to SVG
   * @param pw    output writer
   * @param line  line item
   * @param color color
   * @param xoff  X offset
   * @param yoff  Y offset
   */
  static protected void toSvg( PrintWriter pw, DrawingLinePath line, String color, float xoff, float yoff ) 
  {
    String th_name = line.getThName( ); 
    pw.format(Locale.US, "  <path stroke=\"%s\" stroke-width=\"%.2f\" fill=\"none\" class=\"%s\"", color, TDSetting.mSvgLineStroke, th_name );
    if ( th_name.equals( SymbolLibrary.ARROW ) )                pw.format(" marker-end=\"url(#Triangle)\"");
    else if ( th_name.equals( SymbolLibrary.SECTION ) )         pw.format(" stroke-dasharray=\"5 3\"");
    else if ( th_name.equals( SymbolLibrary.FAULT ) )           pw.format(" stroke-dasharray=\"8 4\"");
    else if ( th_name.equals( SymbolLibrary.FLOOR_MEANDER ) )   pw.format(" stroke-dasharray=\"6 2\"");
    else if ( th_name.equals( SymbolLibrary.CEILING_MEANDER ) ) pw.format(" stroke-dasharray=\"6 2\"");
    toSvgPointLine( pw, line, xoff, yoff, line.isClosed() );
    if ( TDSetting.mSvgLineDirection ) {
      if ( BrushManager.hasLineEffect( line.lineType() ) ) {
        if ( line.isReversed() ) {
          pw.format(" marker-start=\"url(#rev)\"");
        } else {
          pw.format(" marker-start=\"url(#dir)\"");
        }
      }
    } 
    pw.format(" />\n");
  }

  /** export an area item to SVG
   * @param pw    output writer
   * @param area  area item
   * @param color color
   * @param xoff  X offset
   * @param yoff  Y offset
   */
  static protected void toSvg( PrintWriter pw, DrawingAreaPath area, String color, float xoff, float yoff )
  {
    pw.format(Locale.US, "  <path stroke=\"black\" stroke-width=\"%.2f\" fill=\"%s\" fill-opacity=\"0.5\" ", TDSetting.mSvgLineStroke, color );
    toSvgPointLine( pw, area, xoff, yoff, true ); // area borders are closed
    pw.format(" />\n");
  }

  static protected void toSvg( PrintWriter pw, DrawingStationName st, float xoff, float yoff )
  {
    // // pw.format("<text font-size=\"20\" font-family=\"sans-serif\" fill=\"violet\" stroke=\"none\" text-anchor=\"middle\"");
    // pw.format("<text font-size=\"%d\" fill=\"violet\" stroke=\"none\" text-anchor=\"middle\" name=\"%s\"", TDSetting.mSvgStationSize, st.getName() );
    // printPointWithXY( pw, "", xoff+st.cx, yoff+st.cy );
    // pw.format(">%s</text>\n", st.getName() );
    toSvgStation( pw, st.getName(), "violet", xoff + st.cx, yoff + st.cy);
  }

  static protected void toSvg( PrintWriter pw, DrawingStationUser sp, float xoff, float yoff )
  {
    // // pw.format("<text font-size=\"20\" font-family=\"sans-serif\" fill=\"black\" stroke=\"none\" text-anchor=\"middle\"");
    // pw.format("<text font-size=\"%d\" fill=\"black\" stroke=\"none\" text-anchor=\"middle\" name=\"point-%s\"", TDSetting.mSvgStationSize, sp.name() );
    // printPointWithXY( pw, "", xoff+sp.cx, yoff+sp.cy );
    // pw.format(">%s</text>\n", sp.name() );
    toSvgStation( pw, sp.name(), "black", xoff+sp.cx, yoff+sp.cy);
  }

  static private void toSvgStation( PrintWriter pw, String name, String color, float x, float y )
  {
    pw.format("<g id=\"station_%s\">\n", name);
    pw.format("<text font-size=\"%d\" fill=\"%s\" stroke=\"none\" text-anchor=\"middle\" id=\"%s\"", TDSetting.mSvgStationSize, color, name );
    printPointWithXY( pw, "", x, y + (POINT_RADIUS * 1.5f) );
    pw.format(">%s</text>\n", name );
    printPointWithCXCY( pw, "<circle", x, y );
    pw.format(Locale.US, " r=\"%.2f\" ", (float)(POINT_RADIUS / 5) );
    pw.format(Locale.US, " style=\"fill:none;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgPointStroke );
    pw.write( end_grp ); // station_
  }

  static protected void toSvgPointLine( PrintWriter pw, DrawingPointLinePath lp, float xoff, float yoff, boolean closed )
  {
    float bezier_step = TDSetting.getBezierStep();
    pw.format(" d=\"");
    LinePoint p = lp.first();
    float x0 = xoff+p.x;
    float y0 = yoff+p.y;
    printPoint( pw, "M", xoff+p.x, yoff+p.y );
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
            printPoint( pw, " L", pb.x, pb.y );
          }
	}
      } 
      printPoint( pw, " L", x3, y3 );
      x0 = x3;
      y0 = y3;
    }
    p = lp.last();
    if ( closed ) { 
      pw.format(" Z \"");
    } else {
      pw.format("\"");
    }
  }

  static protected void toSvg( PrintWriter pw, DrawingPointPath point, String color, float xoff, float yoff )
  {
    int idx = point.mPointType;
    float scale = point.getScaleValue();
    String name = point.getThName( );
    // TDLog.v( "SVG point " + name + " at " + point.cx + " " + point.cy );
    pw.format("<!-- point %s -->\n", name );
    if ( name.equals( SymbolLibrary.LABEL ) ) {
      // assert( point instanceof DrawingLabelPath );
      float o = (float)(point.mOrientation);
      float s = POINT_SCALE * TDMath.sind( o ) * scale / 10.0f;
      float c = POINT_SCALE * TDMath.cosd( o ) * scale / 10.0f;
      DrawingLabelPath label = (DrawingLabelPath)point;
      // TDLog.v( "SVG point " + name + " at " + point.cx + " " + point.cy + " text " + label.mPointText );
      // printPointWithXY( pw, "<text", xoff+point.cx, yoff+point.cy );
      printPointWithXY( pw, "<text", 0, 0 );
      pw.format(Locale.US, " font-size=\"%.2f\"", TDSetting.mSvgLabelSize * scale );
      pw.format(Locale.US, " style=\"fill:black;stroke:black;stroke-width:%.2f\"", TDSetting.mSvgLabelStroke * scale );
      printMatrix( pw, c, s, (xoff+point.cx), (yoff+point.cy) );
      pw.format( " >%s</text>\n", label.mPointText );
    // } else if ( name.equals("continuation") ) {
    //   printPointWithXY( pw, "<text", xoff+point.cx, yoff+point.cy );
    //   pw.format(Locale.US, " style=\"fill:none;stroke:black;stroke-width:%.2f\">\?</text>\n", TDSetting.mSvgLabelStroke );
    // } else if ( name.equals("danger") ) {
    //   printPointWithXY( pw, "<text", xoff+point.cx, yoff+point.cy );
    //   pw.format(Locale.US, " style=\"fill:none;stroke:red;stroke-width:%.2f\">!</text>\n", TDSetting.mSvgLabelStroke );
    } else if ( BrushManager.isPointSection( idx ) ) {
      /* nothing */
    } else {
      SymbolPoint sp = (SymbolPoint)BrushManager.getPointByIndex( idx );
      if ( sp != null ) {
        
        pw.format(Locale.US, "<g style=\"fill:none;stroke:%s;stroke-width:%.2f\" >\n", color, TDSetting.mSvgPointStroke );
        // pw.format(Locale.US, "<g transform=\"translate(%.2f,%.2f), scale(%d), rotate(%.2f)\">\n", 
        //   (xoff+point.cx)*TDSetting.mToSvg, (yoff+point.cy)*TDSetting.mToSvg, POINT_SCALE, point.mOrientation );

        float o = (float)(point.mOrientation);
        float s = POINT_SCALE * TDMath.sind( o ) * scale;
        float c = POINT_SCALE * TDMath.cosd( o ) * scale;
        pw.format(Locale.US, "<g" );
        printMatrix( pw, c, s, (xoff+point.cx), (yoff+point.cy) );
        pw.format(Locale.US, " >\n" );

        pw.format( "%s\n", sp.getSvg() );
        pw.format( end_grp );
        pw.format( end_grp );
      } else {
        printPointWithCXCY( pw, "<circle", xoff+point.cx, yoff+point.cy );
        pw.format(Locale.US, " r=\"%.2f\" ", POINT_RADIUS * scale );
        pw.format(Locale.US, " style=\"fill:none;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgPointStroke );
      }
    }
  }

  /** draw a xsection tdr
   * @param out         output writer
   * @param scrapfile   tdr file
   * @param dx          delta X applied when the trd items are read from the file
   * @param dy          delta Y
   * @param xoff        X offset applied when the items are drawn to the SVG output
   * @param yoff        Y offset
   */
  protected void writeXSectionToSvg(BufferedWriter out, String scrapfile, String scrapId, float dx, float dy, float xoff, float yoff )
  {
    ArrayList < DrawingPath > paths = new ArrayList<>();
    try {
      // TDLog.Log( TDLog.LOG_IO, "trd to svg. scrap file " + scrapfile );
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      FileInputStream fis = TDFile.getFileInputStream( TDPath.getTdrFile( scrapfile ) );
      BufferedInputStream b_fis = new BufferedInputStream( fis );
      DataInputStream dis = new DataInputStream( b_fis );
      int version = DrawingIO.skipTdrHeader( dis );
      // TDLog.v( "tdr to svg " + scrapfile + " delta " + dx + " " + dy + " Offset " + xoff + " " + yoff );

      DrawingPath path; // = null;
      boolean done = false;
      while ( ! done ) {
        int what = dis.read();
        // TDLog.v("TDR2SVG read code " + what );
        switch ( what ) {
          case 'N': // scrap index ( v. >= 401160 )
            // int scrap = 
              dis.readInt();
            break;
          case 'P':
            path = DrawingPointPath.loadDataStream( version, dis, dx, dy /*, null */ );
            if ( path != null) {
              if (TDSetting.mSvgGroups) {
                paths.add( path );
              } else {
                toSvg(pw, (DrawingPointPath) path, pathToColor(path), xoff, yoff);
              }
            }
            break;
          case 'T':
            path = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
            if ( path != null) {
              paths.add( path );
            } else {
              toSvg(pw, (DrawingLabelPath) path, pathToColor(path), xoff, yoff);
            }
            break;
          case 'L':
            path = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, null */ );
            if ( path != null) {
              paths.add( path );
            } else {
              toSvg(pw, (DrawingLinePath) path, pathToColor(path), xoff, yoff);
            }
            break;
          case 'A':
            path = DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, null */ );
            if ( path != null) {
              paths.add( path );
            } else {
              toSvg(pw, (DrawingAreaPath) path, pathToColor(path), xoff, yoff);
            }
            break;
          case 'J':
            /* path = */ DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
            break;
          case 'U':
            /* path = */ DrawingStationUser.loadDataStream( version, dis ); // consume DrawingStationName data
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
            TDLog.e("TDR2SVG Error. unexpected code=" + what );
            return;
        }
      }
      if ( ! TDSetting.mSvgGroups ) {
        out.write( sw.getBuffer().toString() );
        out.flush();
      }
    } catch ( FileNotFoundException e ) { // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    if (TDSetting.mSvgGroups) {
      writeScrapContent(out, paths, scrapId, xoff, yoff, false);
    }
  }

  /**
   * Aggregates points, lines and areas in groups by their types.
   * @param paths paths to be classified
   * @param writeXSectionsContents whether xsections should be included in the output
   * @returns SvgGroupedPaths with the paths separated in groups
   */
  protected SvgGroupedPaths separatePathsInGroups(ArrayList< DrawingPath > paths, boolean writeXSectionsContents)
  {
    SvgGroupedPaths groupedPaths = new SvgGroupedPaths();

    for ( DrawingPath path : paths ) {
      switch ( path.mType ) {
        case DrawingPath.DRAWING_PATH_POINT:
          DrawingPointPath point = (DrawingPointPath)path;
          if ( BrushManager.isPointSection( point.mPointType ) ) {
            if ( writeXSectionsContents ) groupedPaths.xsectionsPoints.add( point );
          } else {
            String pointTypeName = TDSetting.mSvgGroups ? point.getFullThName() : ALL;
            if ( ! groupedPaths.points.containsKey( pointTypeName ) ) {
              groupedPaths.points.put( pointTypeName, new ArrayList< DrawingPath >() );
            }
            groupedPaths.points.get( pointTypeName ).add( point );
          }
          break;
        case DrawingPath.DRAWING_PATH_LINE:
          DrawingLinePath line = (DrawingLinePath)path;
          String lineTypeName = TDSetting.mSvgGroups ? line.getFullThName() : ALL;
          if ( ! groupedPaths.lines.containsKey( lineTypeName ) ) {
            groupedPaths.lines.put( lineTypeName, new ArrayList< DrawingPath >() );
          }
          groupedPaths.lines.get( lineTypeName ).add( line );
          break;
        case DrawingPath.DRAWING_PATH_AREA:
          DrawingAreaPath area = (DrawingAreaPath)path;
          String areaTypeName = TDSetting.mSvgGroups ? area.getFullThName() : ALL;
          if ( ! groupedPaths.areas.containsKey( areaTypeName ) ) {
            groupedPaths.areas.put( areaTypeName, new ArrayList< DrawingPath >() );
          }
          groupedPaths.areas.get( areaTypeName ).add( area );
          break;
      }
    }
    return groupedPaths;
  }

  /**
   * Orders the types of symbols in reverse alphabetical order.
   * @param types a set with the types that should be ordered
   * @return ArrayList<String> with the ordered types
   */
  protected ArrayList< String > orderSymbolTypes(Set< String > types)
  {
    ArrayList< String > orderedTypes = new ArrayList<>( types );
    Collections.sort( orderedTypes, String.CASE_INSENSITIVE_ORDER );
    Collections.reverse( orderedTypes );
    return orderedTypes;
  }

  /**
   * Writes the content of a scrap to the BufferedWriter provided.
   * @param out BufferedWriter to write the content to
   * @param paths paths to be written
   * @param scrapId scrap id
   * @param xoff X offset
   * @param yoff Y offset
   * @param writeXSectionsContents whether xsections should be included in the output
   */
  protected void writeScrapContent( BufferedWriter out, ArrayList< DrawingPath > paths, String scrapId, float xoff, float yoff, boolean writeXSectionsContents )
  {
    SvgGroupedPaths gps = separatePathsInGroups(paths, writeXSectionsContents);
    final ArrayList< XSection > xsections = new ArrayList<>();

    try {
      // TDLog.v( "SVG paths " + paths.size() + " points" );
      if ( ! gps.points.isEmpty() ) {
        out.write("<g id=\"points_" + scrapId + "\"" + group_mode_open);
        ArrayList < String > pointTypes = orderSymbolTypes( gps.points.keySet() );
        for ( String pointTypeName : pointTypes ) {
          ArrayList< DrawingPath > pointList = gps.points.get(pointTypeName);
          if ( TDSetting.mSvgGroups ) out.write("<g id=\"points_" + pointTypeName + "_" + scrapId + "\"" + group_mode_open);
          for (DrawingPath item : pointList) {
            DrawingPointPath point = (DrawingPointPath)item;
            StringWriter sw53 = new StringWriter();
            PrintWriter pw53  = new PrintWriter(sw53);
            toSvg( pw53, point, pathToColor(point), xoff, yoff );
            out.write( sw53.getBuffer().toString() );
          }
          if ( TDSetting.mSvgGroups ) out.write( end_grp ); // point_
        }
        out.write( end_grp ); // points
        out.flush();
      }

      if ( ! gps.lines.isEmpty() ) {
        out.write("<g id=\"lines_" + scrapId + "\"" + group_mode_open);
        ArrayList < String > lineTypes = orderSymbolTypes( gps.lines.keySet() );
        for ( String lineTypeName : lineTypes ) {
          if ( TDSetting.mSvgGroups ) out.write("<g id=\"lines_" + lineTypeName + "_" + scrapId + "\"" + group_mode_open );
          ArrayList< DrawingPath > lineList = gps.lines.get(lineTypeName);
          for (DrawingPath item : lineList) {
            DrawingLinePath line = (DrawingLinePath)item;
            StringWriter sw54 = new StringWriter();
            PrintWriter pw54  = new PrintWriter(sw54);
            toSvg( pw54, line, pathToColor(line), xoff, yoff );
            out.write( sw54.getBuffer().toString() );
          }
          if ( TDSetting.mSvgGroups ) out.write( end_grp ); // line_
        }
        out.write( end_grp ); // lines
        out.flush();
      }

      if ( ! gps.areas.isEmpty() ) {
        out.write("<g id=\"areas_" + scrapId + "\"" + group_mode_open);
        ArrayList < String > areaTypes = orderSymbolTypes( gps.areas.keySet() );
        for ( String areaTypeName : areaTypes ) {
          if ( TDSetting.mSvgGroups ) out.write("<g id=\"areas_" + areaTypeName + "_" + scrapId + "\"" + group_mode_open);
          ArrayList< DrawingPath > areaList = gps.areas.get(areaTypeName);
          for (DrawingPath item : areaList) {
            DrawingAreaPath area = (DrawingAreaPath)item;
            StringWriter sw55 = new StringWriter();
            PrintWriter pw55  = new PrintWriter(sw55);
            toSvg( pw55, area, pathToColor(area), xoff, yoff );
            out.write( sw55.getBuffer().toString() );
          }
          if ( TDSetting.mSvgGroups ) out.write( end_grp ); // area_
        }
        out.write( end_grp ); // areas
        out.flush();
      }

      if ( ! gps.xsectionsPoints.isEmpty() ) {
        out.write("<g id=\"xsection_links_" + scrapId + "\"" + group_mode_open);
        for (DrawingPointPath point : gps.xsectionsPoints) {
          float xx = point.cx;
          float yy = point.cy;
          if ( TDSetting.mAutoXSections ) {
            // FIXME GET_OPTION
            String scrapname = TDUtil.replacePrefix( TDInstance.survey, point.getOption( TDString.OPTION_SCRAP ) );
            XSection xsection = null;
            if ( scrapname != null ) {
              String scrapfile = scrapname + ".tdr";
              xsection = new XSection( scrapfile, xx- DrawingUtil.CENTER_X, yy-DrawingUtil.CENTER_Y );
              xsections.add( xsection );
            }
            IDrawingLink link = point.mLink; // FIXME Link could be stored in the XSection and written with it
            if ( link != null && xsection != null ) {
              float x1 = (xoff + xx) * TDSetting.mToSvg;
              float y1 = (yoff + yy) * TDSetting.mToSvg;
              float x2 = (xoff + link.getLinkX() ) * TDSetting.mToSvg;
              float y2 = (yoff + link.getLinkY() ) * TDSetting.mToSvg;
              StringWriter sw52 = new StringWriter();
              PrintWriter pw52  = new PrintWriter(sw52);
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
        out.write("<g id=\"xsection_scraps\"" + group_mode_open);
        for (XSection xsection : xsections) {
          // TDLog.v( "SVG xsection " + xsection.mFilename + " " + xsection.mX + " " + xsection.mY );
          StringWriter sw7 = new StringWriter();
          PrintWriter pw7 = new PrintWriter(sw7);
          pw7.format("<g id=\"xsection_%s\"" + group_mode_open, xsection.mFilename);
          out.write(sw7.getBuffer().toString());
          out.flush();
          writeXSectionToSvg(out, xsection.mFilename, xsection.mFilename, xsection.mX, xsection.mY, xoff, yoff);
          StringWriter sw8 = new StringWriter();
          PrintWriter pw8 = new PrintWriter(sw8);
          pw8.format(end_grp);
          out.write(sw8.getBuffer().toString());
          out.flush();
        }
        out.write(end_grp); // xsection_scraps
        out.flush();
      }
    } catch ( IOException e ) {
      TDLog.e( "SVG io-exception " + e.getMessage() );
    }
  }

  protected void writeGrid( BufferedWriter out, DrawingCommandManager plot, float xoff, float yoff, float xmin, float ymin, float xmax, float ymax ) throws IOException
  {
    // TDLog.v( "SVG grid");
    out.write("<g id=\"grid\"\n" );
    out.write("  style=\"fill:none;stroke-opacity:0.4\" >\n");
    printSvgGrid( out, plot.getGrid1(),   "grid1",   "999999", 0.4f, xoff, yoff, xmin, xmax, ymin, ymax );
    printSvgGrid( out, plot.getGrid10(),  "grid10",  "666666", 0.6f, xoff, yoff, xmin, xmax, ymin, ymax );
    printSvgGrid( out, plot.getGrid100(), "grid100", "333333", 0.8f, xoff, yoff, xmin, xmax, ymin, ymax );
    out.write( end_grp ); // grid
  }
 
  // strings

  protected static final String svg_header = "<svg xmlns:i=\"http://ns.adobe.com/AdobeIllustrator/10.0/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\" xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\" \n";

  protected static final String sodipodi = "  <sodipodi:namedview pagecolor=\"#ffffff\" bordercolor=\"#666666\" borderopacity=\"1\" objecttolerance=\"10\" gridtolerance=\"10\" guidetolerance=\"10\" inkscape:pageopacity=\"0\" inkscape:pageshadow=\"2\" inkscape:window-width=\"auto\" inkscape:window-height=\"auto\" id=\"namedview48\" showgrid=\"false\" inkscape:zoom=\"1\" inkscape:cx=\"auto\" inkscape:cy=\"auto\" inkscape:window-x=\"0\" inkscape:window-y=\"0\" inkscape:window-maximized=\"1\" inkscape:current-layer=\"w2d_Walls_shp\" showborder=\"false\"/>\n";

}
