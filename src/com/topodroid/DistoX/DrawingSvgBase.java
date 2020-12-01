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
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.math.Point2D;
import com.topodroid.math.BezierCurve;
import com.topodroid.prefs.TDSetting;

// import android.util.Log;

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

/* Inkscape units
 * - The root element can have width and height with units, which with the proper viewbox
 *   determines the scale for the drawing (real-world value of the SVG user units)
 * - User-unit: unit length in the current user coord system
 * - Unit-id: can be mm, cm, in, pt, px, pc ...
 * - Scale-factor: (from SVG root width/height and viewbox) maps user-units in the doc to real-world units
 *
 * Example <svg width="100cm" height="60cm" viewBox="0 0, 50 30">
 *   the nominal size of the drawing is 100x60 cm
 *   if the unit-id is missing the user-unit is intended
 *   the drawing is 50x30 unser-units ( 1 user-unit = 2 cm )
 *     width="10"  -> 10 user-units = 2 in (at nominal drawing size)
 *     width="1in" -> 96 user-units = 19.2 in
 *   the user-unit can be scaled by a transform="scale(0.75,0.75)" -> user-unit is scaled to 3/4
 *   
 * The scale factor is 0.2 in/user-unit ( therefore 10 user-unit = 2 in, above ) 
 * Initial user-unit is 96 px / in
 * has a CSS value of 96 pxl / inch ( 1 in = 25.4 mm )
 *
 */
class DrawingSvgBase
{
  // FIXME station scale is 0.3
  static final protected int POINT_SCALE  = 10;
  static final protected int POINT_RADIUS = 10;
  static final protected int RADIUS = 3;
  // float SCALE_FIX = util.SCALE_FIX; // 20.0f

  protected static final String end_grp = "</g>\n";
  protected static final String end_svg = "</svg>\n";
  
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

  static protected void printPoint( PrintWriter pw, String prefix, float x, float y )
  {
    pw.format(Locale.US, "%s %.2f %.2f", prefix, x*TDSetting.mToSvg, y*TDSetting.mToSvg );
  }

  static protected void printPointWithXY( PrintWriter pw, String prefix, float x, float y )
  {
    pw.format(Locale.US, "%s x=\"%.2f\" y=\"%.2f\" ", prefix, x*TDSetting.mToSvg, y*TDSetting.mToSvg );
  }

  static protected void printPointWithCXCY( PrintWriter pw, String prefix, float x, float y )
  {
    pw.format(Locale.US, "%s cx=\"%.2f\" cy=\"%.2f\" ", prefix, x*TDSetting.mToSvg, y*TDSetting.mToSvg );
  }

  static protected void printSegmentWithClose( PrintWriter pw, float x1, float y1, float x2, float y2 )
  {
    pw.format(Locale.US, "M %.2f %.2f L %.2f %.2f\" />",
      x1*TDSetting.mToSvg, y1*TDSetting.mToSvg, x2*TDSetting.mToSvg, y2*TDSetting.mToSvg );
  }

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
        TDLog.Error( "SVG grid io-exception " + e.getMessage() );
      }
    }
  }

  static protected void toSvgLabel( PrintWriter pw, DrawingLabelPath point, String color, float xoff, float yoff )
  {
    String name = point.getThName();
    pw.format("<!-- point %s -->\n", name );
    if ( name.equals("label") ) {
      DrawingLabelPath label = (DrawingLabelPath)point;
      printPointWithXY( pw, "<text", xoff+point.cx, yoff+point.cy );
      pw.format(Locale.US, " style=\"fill:black;stroke:black;stroke-width:%.2f\">%s</text>\n", TDSetting.mSvgLabelStroke, label.mPointText );
    }
  }
  // <text
  //    style="font-size:6px;font-family:ArialMT;fill:#0000ff"
  //    x="228.089"
  //    y="562.5"
  //    id="text32">0</text>

  static protected void toSvg( PrintWriter pw, DrawingLinePath line, String color, float xoff, float yoff ) 
  {
    String th_name = line.getThName( ); 
    pw.format(Locale.US, "  <path stroke=\"%s\" stroke-width=\"%.2f\" fill=\"none\" class=\"%s\"", color, TDSetting.mSvgLineStroke, th_name );
    if ( th_name.equals( "arrow" ) ) pw.format(" marker-end=\"url(#Triangle)\"");
    else if ( th_name.equals( "section" ) ) pw.format(" stroke-dasharray=\"5 3 \"");
    else if ( th_name.equals( "fault" ) ) pw.format(" stroke-dasharray=\"8 4 \"");
    else if ( th_name.equals( "floor-meander" ) ) pw.format(" stroke-dasharray=\"6 2 \"");
    else if ( th_name.equals( "ceiling-meander" ) ) pw.format(" stroke-dasharray=\"6 2 \"");
    toSvgPointLine( pw, line, xoff, yoff, line.isClosed() );
    if ( TDSetting.mSvgLineDirection ) {
      if ( BrushManager.hasLineEffect( line.mLineType ) ) {
        if ( line.isReversed() ) {
          pw.format(" marker-start=\"url(#rev)\"");
        } else {
          pw.format(" marker-start=\"url(#dir)\"");
        }
      }
    } 
    pw.format(" />\n");
  }

  static protected void toSvg( PrintWriter pw, DrawingAreaPath area, String color, float xoff, float yoff )
  {
    pw.format(Locale.US, "  <path stroke=\"black\" stroke-width=\"%.2f\" fill=\"%s\" fill-opacity=\"0.5\" ", TDSetting.mSvgLineStroke, color );
    toSvgPointLine( pw, area, xoff, yoff, true ); // area borders are closed
    pw.format(" />\n");
  }

  static protected void toSvg( PrintWriter pw, DrawingStationName st, float xoff, float yoff )
  {
    // pw.format("<text font-size=\"20\" font-family=\"sans-serif\" fill=\"violet\" stroke=\"none\" text-anchor=\"middle\"");
    pw.format("<text font-size=\"%d\" fill=\"violet\" stroke=\"none\" text-anchor=\"middle\"", TDSetting.mSvgStationSize );
    printPointWithXY( pw, "", xoff+st.cx, yoff+st.cy );
    pw.format(">%s</text>\n", st.getName() );
  }

  static protected void toSvg( PrintWriter pw, DrawingStationPath sp, float xoff, float yoff )
  {
    // pw.format("<text font-size=\"20\" font-family=\"sans-serif\" fill=\"black\" stroke=\"none\" text-anchor=\"middle\"");
    pw.format("<text font-size=\"%d\" fill=\"black\" stroke=\"none\" text-anchor=\"middle\"", TDSetting.mSvgStationSize );
    printPointWithXY( pw, "", xoff+sp.cx, yoff+sp.cy );
    pw.format(">%s</text>\n", sp.name() );
  }

  static protected void toSvgPointLine( PrintWriter pw, DrawingPointLinePath lp, float xoff, float yoff, boolean closed )
  {
    float bezier_step = TDSetting.getBezierStep();
    pw.format(" d=\"");
    LinePoint p = lp.mFirst;
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
    if ( closed ) { 
      pw.format(" Z \"");
    } else {
      pw.format("\"");
    }
  }

  static protected void toSvg( PrintWriter pw, DrawingPointPath point, String color, float xoff, float yoff )
  {
    int idx = point.mPointType;
    String name = point.getThName( );
    pw.format("<!-- point %s -->\n", name );
    if ( name.equals("label") ) {
      DrawingLabelPath label = (DrawingLabelPath)point;
      printPointWithXY( pw, "<text", xoff+point.cx, yoff+point.cy );
      pw.format(Locale.US, " style=\"fill:black;stroke:black;stroke-width:%.2f\">%s</text>\n", TDSetting.mSvgLabelStroke, label.mPointText );
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
        float s = POINT_SCALE * TDMath.sind( o );
        float c = POINT_SCALE * TDMath.cosd( o );
        pw.format(Locale.US, "<g transform=\"matrix(%.2f,%.2f,%.2f,%.2f,%.2f,%.2f)\">\n", 
          c, s, -s, c, (xoff+point.cx)*TDSetting.mToSvg, (yoff+point.cy)*TDSetting.mToSvg );

        pw.format("%s\n", sp.getSvg() );
        pw.format( end_grp );
        pw.format( end_grp );
      } else {
        printPointWithCXCY( pw, "<circle", xoff+point.cx, yoff+point.cy );
        pw.format(Locale.US, " r=\"%d\" ", POINT_RADIUS );
        pw.format(Locale.US, " style=\"fill:none;stroke:black;stroke-width:%.2f\" />\n", TDSetting.mSvgPointStroke );
      }
    }
  }

  static protected void tdrToSvg( PrintWriter pw, String scrapfile, float dx, float dy, float xoff, float yoff )
  {
    try {
      // TDLog.Log( TDLog.LOG_IO, "trd to svg. scrap file " + scrapfile );
      FileInputStream fis = new FileInputStream( TDPath.getTdrFile( scrapfile ) );
      BufferedInputStream bfis = new BufferedInputStream( fis );
      DataInputStream dis = new DataInputStream( bfis );
      int version = DrawingIO.skipTdrHeader( dis );
      // Log.v("DistoXsvg", "tdr to svg " + scrapfile + " delta " + dx + " " + dy + " Offset " + xoff + " " + yoff );

      DrawingPath path; // = null;
      boolean done = false;
      while ( ! done ) {
        int what = dis.read();
        switch ( what ) {
          case 'N': // scrap index ( v. >= 401160 )
            int scrap = dis.readInt();
            break;
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
	    TDLog.Error("TDR2SVG Error. unexpected code=" + what );
	    return;
        }
      }
    } catch ( FileNotFoundException e ) { // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }
}
