/* @file DrawingDxf.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid drawing: dxf export
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.io.dxf;

import com.topodroid.utils.TDString;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDUtil;
import com.topodroid.math.Point2D;
import com.topodroid.math.BezierCurve;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.common.PlotType;
import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.num.TDNum;
import com.topodroid.TDX.DBlock;
import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.ICanvasCommand;
import com.topodroid.TDX.BrushManager;
import com.topodroid.TDX.LinePoint;
import com.topodroid.TDX.DrawingUtil;
import com.topodroid.TDX.DrawingIO;
import com.topodroid.TDX.DrawingPath;
import com.topodroid.TDX.DrawingPointPath;
import com.topodroid.TDX.DrawingLinePath;
import com.topodroid.TDX.DrawingAreaPath;
import com.topodroid.TDX.DrawingPointLinePath;
import com.topodroid.TDX.DrawingSpecialPath;
import com.topodroid.TDX.DrawingAudioPath;
import com.topodroid.TDX.DrawingPhotoPath;
import com.topodroid.TDX.DrawingStationUser;
import com.topodroid.TDX.DrawingStationName;
import com.topodroid.TDX.DrawingLabelPath;
import com.topodroid.TDX.DrawingCommandManager;
import com.topodroid.TDX.IDrawingLink;
import com.topodroid.TDX.Symbol;
import com.topodroid.TDX.SymbolPoint;
import com.topodroid.TDX.SymbolPointLibrary;
import com.topodroid.TDX.SymbolLineLibrary;
import com.topodroid.TDX.SymbolAreaLibrary;
// import com.topodroid.TDX.DrawingPath;

// import java.util.Locale;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.RectF;

public class DrawingDxf
{
  static final private float POINT_SCALE   = 10.0f; // scale of point icons: only ACAD_6
  // the next three are for text
  static final private float STATION_SCALE =  6.0f / DrawingUtil.SCALE_FIX; // scale of station names
  static final private float LABEL_SCALE   =  8.0f / DrawingUtil.SCALE_FIX; // scale of label text
  static final private float AXIS_SCALE    = 10.0f / DrawingUtil.SCALE_FIX; // scale of text on the axes

  // static final private String two_n_half = "2.5";
  // static final String ten = "10";


  // static void printEndText( PrintWriter pw, String style )
  // {
  //   DXF.printString( pw, 7, style );
  //   DXF.printString( pw, 100, DXF.AcDbText );
  // }

  /** @return the number of interpolating points for a line
   * @param line    line path
   * @param closed  whether the path is closed
   */
  static private int countInterpolatedPolylinePoints(  DrawingPointLinePath line, boolean closed )
  {
    float bezier_step = TDSetting.getBezierStep();
    int npt = 0;
    LinePoint p = line.first();
    float x0 = p.x;
    float y0 = p.y;
    ++ npt;
    for ( p = p.mNext; p != null; p = p.mNext ) { 
      float x3 = p.x;
      float y3 = p.y;
      if ( p.has_cp ) { // FIXME this converts the cubic with a thickly interpolated polyline
        float x1 = p.x1;
        float y1 = p.y1;
        float x2 = p.x2;
        float y2 = p.y2;
	float len = (x1-x0)*(x1-x0) + (x2-x1)*(x2-x1) + (x3-x2)*(x3-x2) + (x3-x0)*(x3-x0)
	          + (y1-y0)*(y1-y0) + (y2-y1)*(y2-y1) + (y3-y2)*(y3-y2) + (y3-y0)*(y3-y0);
	int np = (int)( TDMath.sqrt( len ) * bezier_step + 0.5f );
	if ( np > 1 ) npt += np-1;
      } 
      ++npt;
      x0 = x3;
      y0 = y3;
    }
    if ( closed ) ++npt;
    return npt;
  }

  /** print an interpolated line path
   * @param pw     output writer
   * @param line   line path
   * @param scale  scale factor
   * @param handle ACAD handle
   * @param ref    ...
   * @param layer  DXF layer
   * @param closed  whether the path is closed
   * @param xoff   X offset
   * @param yoff   Y offset
   * @param z      Z "level" (used only if layer is not null)
   * @param p3D    3d polyline
   */
  static private int printInterpolatedPolyline(  PrintWriter pw, DrawingPointLinePath line, float scale, int handle, int ref,
                 String layer, boolean closed, float xoff, float yoff, float z, String linetype, int color, boolean p3D )
  {
    float bezier_step = TDSetting.getBezierStep();
    LinePoint p = line.first();
    float x0 = xoff + p.x;
    float y0 = yoff + p.y;
    if ( layer != null ) {
      handle = DXF.printLinePoint( pw, scale, handle, ref, layer, x0, y0, z, linetype, color, p3D );
    } else {
      DXF.printXY( pw, x0*scale, -y0*scale, 0 );
    }
    for ( p = p.mNext; p != null; p = p.mNext ) { 
      float x3 = xoff + p.x;
      float y3 = yoff + p.y;
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
          if ( layer != null ) {
	    for ( int n=1; n < np; ++n ) {
	      Point2D pb = bc.evaluate( (float)n / (float)np );
              handle = DXF.printLinePoint( pw, scale, handle, ref, layer, pb.x, pb.y, z, linetype, color, p3D );
            }
          } else {
	    for ( int n=1; n < np; ++n ) {
	      Point2D pb = bc.evaluate( (float)n / (float)np );
              DXF.printXY( pw, (pb.x+xoff)*scale, -(pb.y+yoff)*scale, 0 );
            }
          }
        }
      } 
      if ( layer != null ) {
        handle = DXF.printLinePoint( pw, scale, handle, ref, layer, x3, y3, z, linetype, color, p3D );
      } else {
        DXF.printXY( pw, x3*scale, -y3*scale, 0 );
      }
      x0 = x3;
      y0 = y3;
    }
    if ( closed ) {
      p = line.first();
      if ( layer != null ) {
        handle = DXF.printLinePoint( pw, scale, handle, ref, layer, xoff+p.x, yoff+p.y, z, linetype, color, p3D );
      } else {
        DXF.printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
      }
    }
    return handle;
  }

  /** print an interpolated line path
   * @param pw     output writer
   * @param line   line path
   * @param scale  scale factor
   * @param handle ACAD handle
   * @param ref    ...
   * @param layer  DXF layer
   * @param closed  whether the path is closed
   * @param xoff   X offset
   * @param yoff   Y offset
   * @param z      Z "level" (used only if layer is not null)
   * @param p3D    3d polyline
   */
  static private int printPolyline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, int ref,
              String layer, boolean closed, float xoff, float yoff, float z, String linetype, int color, boolean p3D )
  {
    int npt = countInterpolatedPolylinePoints( line, closed );
    handle = DXF.printPolylineHeader( pw, handle, ref, layer, closed, npt, linetype, color, z, p3D );
    int polyline_handle = handle;
    handle = printInterpolatedPolyline( pw, line, scale, handle, handle, layer, closed, xoff, yoff, z, linetype, color, false );
    handle = DXF.printPolylineFooter( pw, handle, polyline_handle, layer );
    return handle;
  }

  // static private int printLWPolyline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, String layer, boolean closed,
  //                             float xoff, float yoff )
  // {
  //   int close = (closed ? 1 : 0 );
  //   DXF.printString( pw, 0, "LWPOLYLINE" );
  //   handle = DXF.printAcDb( pw, handle, DXF.AcDbEntity, DXF.AcDbPolyline );
  //   DXF.printString( pw, 8, layer );
  //   DXF.printInt( pw, 38, 0 ); // elevation
  //   DXF.printInt( pw, 39, 1 ); // thickness
  //   DXF.printInt( pw, 43, 1 ); // start width
  //   DXF.printInt( pw, 70, close ); // not closed
  //   DXF.printInt( pw, 90, line.size() ); // nr. of points
  //   for (LinePoint p = line.first(); p != null; p = p.mNext ) { 
  //     DXF.printXY( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0 );
  //   }
  //   return handle;
  // }

  /** @return true of the line contains a spline piece ( the path has some control point )
   * @param line   line path
   */
  static private boolean checkSpline( DrawingPointLinePath line )
  {
    if ( DXF.mVersion13_14 ) {
      for ( LinePoint p = line.first(); p != null; p = p.mNext ) {
        if ( p.has_cp ) {
          return true;
        }
      }
    }
    return false;
  }

  /** print a spline line path
   * @param pw     output writer
   * @param line   line path
   * @param scale  scale factor
   * @param handle ACAD handle
   * @param layer  DXF layer
   * @param closed  whether the path is closed
   * @param xoff   X offset
   * @param yoff   Y offset
   * @param z      Z "level" (used only if layer is not null)
   */
  static private int printSpline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, String layer, boolean closed,
                                  float xoff, float yoff, float z ) {
    // String linetype = DXF.lt_continuous;
    // int color = DXF.BY_LAYER;
    return printSpline( pw, line, scale, handle, layer, closed, xoff, yoff, z, DXF.lt_continuous, DXF.BY_LAYER );
  }

  /** print a spline line path
   * @param pw     output writer
   * @param line   line path
   * @param scale  scale factor
   * @param handle ACAD handle
   * @param layer  DXF layer
   * @param closed  whether the path is closed
   * @param xoff   X offset
   * @param yoff   Y offset
   * @param z      Z "level" (used only if layer is not null)
   */
  static private int printSpline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, String layer, boolean closed,
                          float xoff, float yoff, float z, String linetype, int color )
  {
    // TDLog.v( "print spline");
    DXF.printString( pw, 0, "SPLINE" );
    handle = DXF.printAcDb( pw, handle, DXF.AcDbEntity, "AcDbSpline" );
    DXF.printString( pw, 8, layer );
    DXF.printString( pw, 6, linetype ); // DXF.lt_continuous );
    DXF.printInt( pw, 62, color ); // 256 = bylayer
    DXF.printFloat( pw, 48, 1.0f ); // scale 
    DXF.printInt( pw, 60, 0 ); // visibility (0: visible, 1: invisible)
    DXF.printInt( pw, 66, 1 ); // group 1: "entities follow" flag
    // DXF.printInt( pw, 67, 0 ); // in model space [default]
    DXF.printXYZ( pw, 0, 0, 1, 200 ); // normal vector

    float xt=0, yt=0;
    int np = 2;
    LinePoint p = line.first(); 
    LinePoint pn = p.mNext;
    if ( pn != null ) {
      if ( pn.has_cp ) {
        xt = pn.x1 - p.x;
        yt = pn.y1 - p.y;
      } else {
        xt = pn.x - p.x;
        yt = pn.y - p.y;
      }
      float d = (float)Math.sqrt( xt*xt + yt*yt );
      DXF.printXYZ( pw, xt/d, -yt/d, z, 2 );

      while ( pn.mNext != null ) {
        p = pn;
        pn = pn.mNext;
        ++np;
      }
      if ( pn.has_cp ) {
        xt = pn.x - pn.x2;
        yt = pn.y - pn.y2;
      } else {
        xt = pn.x - p.x;
        yt = pn.y - p.y;
      }
      d = (float)Math.sqrt( xt*xt + yt*yt );
      DXF.printXYZ( pw, xt/d, -yt/d, z, 3 );
    }

    //int ncp = 4 * np - 4; // np + 3 * (np-1) - 1; // 4 * NP - 4
    int ncp = 3 * np - 2; // control points: 1 + 3 * (NP - 1) = 3 NP - 2 //for (p=...
    int nk  = 3 * np + 2; // ncp + 4 - (np - 2);  // 3 * NP + 2
/*
    if ( closed ) {
      nk  += 3;
      ncp += 3;
      np  += 1;
    }
*/
      
    // TDLog.v( "Spline P " + np + " Cp " + ncp + " K " + nk );
    DXF.printInt( pw, 70, 8+(closed?1:0) ); // flags  1: closed, 2: periodic, 4: rational, 8: planar, 16 linear
    DXF.printInt( pw, 71, 3 );    // degree of the spline
    DXF.printInt( pw, 72, nk );   // nr. of knots
    DXF.printInt( pw, 73, ncp );  // nr. of control pts
    DXF.printInt( pw, 74, np );   // nr. of fit points
    // DXF.printXYZ( pw, x, y, z, 2 ); // start tangent
    // DXF.printXYZ( pw, x, y, z, 3 ); // end tangent
    
    DXF.printInt( pw, 40, 0 );                              // knots: 1 + 3 * NP + 1
    for ( int k=0; k<np; ++k ) {                         // 0 0 0 0 1 1 1 2 2 2 ... N N N N
      for ( int j=0; j<3; ++j ) DXF.printInt( pw, 40, k );
    }
    DXF.printInt( pw, 40, np-1 );

    p = line.first(); 
    xt = p.x;
    yt = p.y;
    DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );         // control points: 1 + 3 * (NP - 1) = 3 NP - 2
    for ( p = p.mNext; p != null; p = p.mNext ) { 
      if ( p.has_cp ) {
        DXF.printXYZ( pw, (p.x1+xoff) * scale, -(p.y1+yoff) * scale, z, 0 );
        DXF.printXYZ( pw, (p.x2+xoff) * scale, -(p.y2+yoff) * scale, z, 0 );
      } else {
        DXF.printXYZ( pw, (xt+xoff) * scale, -(yt+yoff) * scale, z, 0 );
        DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, z, 0 );
      }
      DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, z, 0 );
      xt = p.x;
      yt = p.y;
    }
/*
    if ( closed ) {
      p = line.first();
      DXF.printXYZ( pw, (xt+xoff) * scale, -(yt+yoff) * scale, 0.0f, 0 );
      DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );
    }
*/

    for ( p = line.first(); p != null; p = p.mNext ) { 
      DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, z, 1 );  // fit points: NP
    }
/*
    if ( closed ) {
      p = line.first();
      DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 1 );  // fit points: NP
    }
*/
    return handle;
  }

  /** write the plot-sketch in DXF format
   * @param out     output writer
   * @param num     data reduction (unused)
   * @param plot    plot sketch
   * @param type    plot type (unused)
   */
  public static void writeDxf( BufferedWriter out, TDNum num, /* DrawingUtil util, */ DrawingCommandManager plot, long type )
  {
    DXF.mVersion9  = (TDSetting.mAcadVersion == DXF.ACAD_9);
    DXF.mVersion13 = (TDSetting.mAcadVersion == DXF.ACAD_12);
    DXF.mVersion14 = (TDSetting.mAcadVersion == DXF.ACAD_14);
    DXF.mVersion13_14 = DXF.mVersion13 || DXF.mVersion14;
    
    float scale = 1.0f/DrawingUtil.SCALE_FIX; // TDSetting.mDxfScale; 
    float xoff = 0;
    float yoff = 0;
    int handle = 0;
    int model_record_handle = -1;
    int model_block_handle = -1;
    // int ref    = 0;
    RectF bbox = plot.getBoundingBox( );
    float xmin = bbox.left;
    float xmax = bbox.right;
    float ymin = bbox.top;
    float ymax = bbox.bottom;
    float zmin = -2.0f;
    float zmax = plot.scrapMaxIndex();
    xmin *= scale;
    xmax *= scale;
    ymin *= scale;
    ymax *= scale;

    int[] point_record_handle = new int[ BrushManager.getPointLibSize() ];

    // TDLog.v( "DXF X " + xmin + " " + xmax + " Y " + ymin + " " + ymax );

    try {
      SymbolPointLibrary point_lib = BrushManager.getPointLib();
      SymbolLineLibrary line_lib   = BrushManager.getLineLib();
      SymbolAreaLibrary area_lib   = BrushManager.getAreaLib();
      // header
      DXF.writeComment( out, "DXF created by TopoDroid v. " + TDVersion.string() 
        + " " + TDSetting.mAcadVersion + " " + (TDSetting.mAutoStations? "T ":"F ") + (TDSetting.mAutoXSections? "T ":"F ") + TDSetting.getBezierStep() );

      xmin -= 2;  xmax += 2;
      ymin -= 2;  ymax += 2;

      handle = DXF.writeHeaderSection( out, handle, xmin, -ymax, zmin, xmax, -ymin, zmax );
      handle = DXF.writeClassesSection( out, handle );

      DXF.writeSection( out, "TABLES" );

      // handle = DXF.writeVportTable( out, handle, xmin, -ymax, xmax, -ymin );
      handle = DXF.writeVportTable( out, handle, 0, 0, 1, 1 ); // AutoCAD always uses 0,0 1,1
      handle = DXF.writeStylesTable( out, handle );
      int p1_style = handle; // HBX_DXF pointer to linetype character style
      // HBX_DXF handle = DXF.writeLTypesTable( out, handle );
      // HBX_DXF
      {
        int nr_ltypes = 0;
        if ( TDSetting.mAcadLayer ) {
          nr_ltypes += BrushManager.getLineLibSize() + BrushManager.getAreaLibSize();// its value is not important
        } else {
          nr_ltypes = 0; // its value is not important
        }
        handle = DXF.writeLTypesTableheader(out, handle, nr_ltypes, p1_style); // HBX_DXF
        // if ( TDSetting.mAcadLayer ) {
          if ( line_lib != null ) { // always true
            for ( Symbol line : line_lib.getSymbols() ) {
              String l_name = "L_" + replaceColon( line.getThName() );
              // String l_type = DXF.lt_continuous;
              if ( DXF.mVersion14 &&
                      ( l_name.equals("L_pit")//4
                              || l_name.equals("L_chimney")//3
                              || l_name.equals("L_arrow")  //1
                              || l_name.equals("L_slope")  //7
                              || l_name.equals("L_user")   //a
                              || l_name.equals("L_wall")   //b
                              || l_name.equals("L_section")//c
                              || l_name.equals("L_border") //2
                              || l_name.equals("L_wall-presumed")//5
                              || l_name.equals("L_rock-border")//6
                      ) ) {
                // no print Ltype
              } else { // ACAD version 9-12
                if ( l_name.equals("L_user")   //a
                        || l_name.equals("L_wall")   //b
                        || l_name.equals("L_section")//c
                        || l_name.equals("L_border") //2
                        || l_name.equals("L_wall-presumed")//5
                        || l_name.equals("L_rock-border")//6
                ) {
                  // no print Ltype
                } else {
                  handle = DXF.printLtype(out, handle, l_name);
                }
              }
            }
          }

          if ( area_lib != null ) { // always true
            for ( Symbol area : area_lib.getSymbols() ) {
              String a_name = "A_" + replaceColon( area.getThName() );
              handle = DXF.printLtype( out, handle, a_name );
            }
          }
        // }
        //handle = DXF.printLtype(out, handle, "LEG");
        DXF.writeEndTable(out); // HBX_DXF
      }
      // END HBX_DXF

      int nr_layers = 7;
      // nr_layers += 1 + line_lib.size() + area_lib.size();
      // nr_layers += 1 + BrushManager.getLineLibSize() + BrushManager.getAreaLibSize();
      if ( TDSetting.mAcadLayer ) { // HBX_DXF linetype separated
        nr_layers += 1 + plot.scrapMaxIndex();
      } else { // HBX_DXF original layer separated
        nr_layers += 1 + BrushManager.getLineLibSize() + BrushManager.getAreaLibSize(); // ?PointLib
      }
      if ( DXF.mVersion9 ) { handle = 2; }
      handle = DXF.writeBeginTable( out, "LAYER", handle, nr_layers );
      {
        StringWriter sw2 = new StringWriter();
        PrintWriter pw2  = new PrintWriter(sw2);

        // 2 layer name, 70 flag (64), 62 color code, 6 line type
        int flag = 0;
        int color = 1;
        String l_type = DXF.lt_continuous;
        // if ( ! DXF.mVersion13_14 ) { handle = 40; }
        handle = DXF.printLayer( pw2, handle, "0",       flag, 7, l_type ); // LAYER "0" .. FIXME DraftSight color must be AutoCAD white
        handle = DXF.printLayer( pw2, handle, "LEG",     flag, color, l_type ); ++color; // red
        handle = DXF.printLayer( pw2, handle, "SPLAY",   flag, color, l_type ); ++color; // yellow
        handle = DXF.printLayer( pw2, handle, "STATION", flag, color, l_type ); ++color; // green
        handle = DXF.printLayer( pw2, handle, "LINE",    flag, color, l_type ); ++color; // cyan
        handle = DXF.printLayer( pw2, handle, "POINT",   flag, color, l_type ); ++color; // blue
        handle = DXF.printLayer( pw2, handle, "AREA",    flag, color, l_type ); ++color; // magenta
        handle = DXF.printLayer( pw2, handle, "REF",     flag, color, l_type ); ++color; // white
        handle = DXF.printLayer( pw2, handle, "LINK",    flag, DXF.LNK_color, l_type ); // ??? Link brown

        // HBX_DXF if TDSetting.mAcadLayer then you need a layer for each scrap
        if ( TDSetting.mAcadLayer ) { // HBX_DXF linetype separated
          for ( int s = 0; s < plot.scrapMaxIndex(); ++s ) {
            // String l_name = "SCRAP_" + Integer.toString( s );
            // String l_type = DXF.lt_continuous;
            // color = 7; // black
            handle = DXF.printLayer( pw2, handle, ("SCRAP_" + Integer.toString( s )), flag, 7, l_type );
          }
        } else { // HBX_DXF layer separated
          if ( line_lib != null ) { // always true
            for ( Symbol line : line_lib.getSymbols() ) {
              String l_name = "L_" + replaceColon( line.getThName() );
              l_type = DXF.lt_continuous;
              if ( DXF.mVersion14 &&
                      ( l_name.equals("L_pit")//4
                              || l_name.equals("L_chimney")//3
                              || l_name.equals("L_arrow")  //1
                              || l_name.equals("L_slope")  //7
                              || l_name.equals("L_user")   //a
                              || l_name.equals("L_wall")   //b
                              || l_name.equals("L_section")//c
                              || l_name.equals("L_border") //2
                              || l_name.equals("L_wall-presumed")//5
                              || l_name.equals("L_rock-border")//6
                      )) {
                l_type = l_name;
              } else { //9-12
                if (l_name.equals("L_user")   //a
                        || l_name.equals("L_wall")   //b
                        || l_name.equals("L_section")//c
                        || l_name.equals("L_border") //2
                        || l_name.equals("L_wall-presumed")//5
                        || l_name.equals("L_rock-border")//6
                ) {
                  l_type = l_name;
                }
              }
              color = DxfColor.rgbToIndex( line.getColor() );
              handle = DXF.printLayer( pw2, handle, l_name, flag, color, l_type );
            }
          }
          l_type = DXF.lt_continuous;
          if ( area_lib != null ) { // always true
            for ( Symbol area : area_lib.getSymbols() ) {
              // String a_name = "A_" + replaceColon( area.getThName() );
              color = DxfColor.rgbToIndex( area.getColor() );
              handle = DXF.printLayer( pw2, handle, ("A_" + replaceColon( area.getThName() )), flag, color, l_type );
            }
          }
          if ( point_lib != null ) { // always true
            for ( Symbol point : point_lib.getSymbols() ) {
              // String p_name = "P_" + replaceColon( point.getThName() );
              color = DxfColor.rgbToIndex( point.getColor() );
              handle = DXF.printLayer( pw2, handle, ("P_" + replaceColon( point.getThName() )), flag, color, l_type );
            }
          }
        }
        out.write( sw2.getBuffer().toString() );
      }
      DXF.writeEndTable( out );

      handle = DXF.writeExtraTables( out, handle );
      handle = DXF.writeDimstyleTable( out, handle );
      // HBX Block begin
      if ( DXF.mVersion13_14 ) {
        handle = DXF.writeBeginTable( out, "BLOCK_RECORD", handle, BrushManager.getPointLibSize() );
        {
          handle = DXF.writeSpaceBlockRecord( out, "*Model_Space", handle );
          model_record_handle = handle;
          handle = DXF.writeSpaceBlockRecord( out, "*Paper_Space", handle );

          for (int n = 0; n < BrushManager.getPointLibSize(); ++n) {
            String th_name = replaceColon( BrushManager.getPointThName(n) );
            // HBX_DXF
            String layer2 = TDSetting.mAcadLayer?  "0" // by-block
                                                : "P_" + th_name;
            DXF.writeString(out, 0, "BLOCK_RECORD");
            handle = DXF.writeAcDb(out, handle, DXF.AcDbSymbolTR, "AcDbBlockTableRecord");
            point_record_handle[n] = handle;
            DXF.writeString(out, 8, layer2 );
            DXF.writeString(out, 2, "B_" + th_name); // block name
            DXF.writeInt(out, 70, 0);                // block insertion units
          }
        }
        DXF.writeEndTable( out );
      }
      
      DXF.writeEndSection( out );
      out.flush();
      
      DXF.writeSection( out, "BLOCKS" );
      {
        if ( DXF.mVersion13_14 ) {
          handle = DXF.writeSpaceBlock( out, "*Model_Space", handle );
          model_block_handle = handle - 1; // space-block increase handle by 2
          handle = DXF.writeSpaceBlock( out, "*Paper_Space", handle );
          // TDLog.v( "model handle " + String.format("%X", model_block_handle ) );
        }

        // // 8 layer (0), 2 block name,
        for ( int n = 0; n < BrushManager.getPointLibSize(); ++ n ) {
          int block_handle = -1;
          SymbolPoint pt = (SymbolPoint)BrushManager.getPointByIndex(n);
          DXF.writeString( out, 0, "BLOCK" );
          if ( DXF.mVersion13_14 ) {
            // handle = DXF.writeAcDb( out, handle, DXF.AcDbEntity, "AcDbBlockBegin" );
            handle = DXF.writeAcDb( out, handle, point_record_handle[n], DXF.AcDbEntity, "AcDbBlockBegin" );
            block_handle = handle;
          }
          String th_name = replaceColon( pt.getThName() ); // FIXME may null pointer
          // HBX_DXF
          String layer2 = TDSetting.mAcadLayer?  "0" // by-block
                                              : "P_" + th_name;
          // DXF.writeString( out, 8, "P_" + th_name );
          DXF.writeString( out, 2, "B_" + th_name ); // block name, can be repeated with '3'
          DXF.writeInt( out, 70, 0 );       // flag 0=none, 1=anonymous, 2=non-const attr, 4=xref, 8=xref overlay,
                                        // 16=ext. dependent, 32=ext. resolved (ignored), 64=referenced xref (ignored)
          DXF.writeXYZ( out, 0, 0, 0, 0 );
          // out.write( pt.getDxf() );
          SymbolPointDxf point_dxf = pt.getDxf();
          // handle = point_dxf.writeDxf( out, TDSetting.mAcadVersion, handle, point_record_handle[n] );
          handle = point_dxf.writeDxf( out, TDSetting.mAcadVersion, handle, block_handle );

          DXF.writeString( out, 0, "ENDBLK" );
          if ( DXF.mVersion13_14 ) {
            handle = DXF.writeAcDb( out, handle, block_handle, DXF.AcDbEntity, "AcDbBlockEnd");
            // DXF.writeString( out, 8, "POINT" );
            DXF.writeString( out, 8, layer2 );
          }
        }
      }
      DXF.writeEndSection( out );
      out.flush();
      // HBX Block end

      DXF.writeSection( out, "ENTITIES" );
      {
	String scale_len = TDString.TWENTY;
        float sc1 = 20; // DrawingUtil.SCALE_FIX / 2 = 10;

        // reference
        float z = -1.0f; // Z level
        int scrap = -1;  // scrap index or layer
        if ( TDSetting.mDxfReference ) {
          StringWriter sw9 = new StringWriter();
          PrintWriter pw9  = new PrintWriter(sw9);
	  float sc2 = sc1 / 2;
          handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax,     xmin+sc1,  -ymax,      z );
          handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax,     xmin,      -ymax+sc1,  z );
          handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin+sc2, -ymax,     xmin+sc2,  -ymax+0.5f, z ); // 10 m ticks
          handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax+sc2, xmin+0.5f, -ymax+sc2,  z );
          handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin+sc1, -ymax,     xmin+sc1,  -ymax+0.5f, z ); // 20 m ticks
          handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax+sc1, xmin+0.5f, -ymax+sc1,  z );
          // out.write( sw9.getBuffer().toString() );

          // DXF.printString( pw9, 0, "LINE" );
          // handle = DXF.printAcDb( pw9, handle, DXF.AcDbEntity, DXF.AcDbLine );
          // DXF.printString( pw9, 8, "REF" );
          // // DXF.printInt(  pw9, 39, 0 );         // line thickness
          // DXF.printXYZ( pw9, xmin, -ymax, 0.0f, 0 );
          // DXF.printXYZ( pw9, (xmin+sc1), -ymax, 0.0f, 1 );
          // out.write( sw9.getBuffer().toString() );

          // StringWriter sw8 = new StringWriter();
          // PrintWriter pw8  = new PrintWriter(sw8);
          // DXF.printString( pw8, 0, "LINE" );
          // handle = DXF.printAcDb( pw8, handle, DXF.AcDbEntity, DXF.AcDbLine );
          // DXF.printString( pw8, 8, "REF" );
          // // DXF.printInt(  pw8, 39, 0 );         // line thickness
          // DXF.printXYZ( pw8, xmin, -ymax, 0.0f, 0 );
          // DXF.printXYZ( pw8,  xmin, -ymax+sc1, 0.0f, 1 );
          // out.write( sw8.getBuffer().toString() );
          // out.flush();

	  // offset axes legends by 1
          // StringWriter sw7 = new StringWriter();
          // PrintWriter pw7  = new PrintWriter(sw7);
          handle = DXF.printText( pw9, handle, model_block_handle, scale_len, xmin+sc1, -ymax+1, 0, AXIS_SCALE, "REF", DXF.style_dejavu, xoff, yoff, z, scrap, DXF.BY_LAYER );
          handle = DXF.printText( pw9, handle, model_block_handle, scale_len, xmin+1, -ymax+sc1, 0, AXIS_SCALE, "REF", DXF.style_dejavu, xoff, yoff, z, scrap, DXF.BY_LAYER );
          out.write( sw9.getBuffer().toString() );
          out.flush();
        }

        // centerline data
        // if ( PlotType.isSketch2D( type ) )
        {
          for ( DrawingPath sh : plot.getLegs() ) {
            DBlock blk = sh.mBlock;
            if ( blk == null ) continue;

            StringWriter sw4 = new StringWriter();
            PrintWriter pw4  = new PrintWriter(sw4);
            DXF.printString( pw4, 0, "LINE" );
            handle = DXF.printAcDb( pw4, handle, DXF.AcDbEntity, DXF.AcDbLine );
            DXF.printString( pw4, 8, "LEG" );
            // DXF.printInt( pw4, 39, 2 );         // line thickness

            // // if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
            //   NumStation f = num.getStation( blk.mFrom );
            //   NumStation t = num.getStation( blk.mTo );
            //   if ( type == PlotType.PLOT_PLAN ) {
            //     float x0 = scale *( xoff + DrawingUtil.toSceneX( f.e, f.s ) );
            //     float y0 = scale *( yoff + DrawingUtil.toSceneY( f.e, f.s ) );
            //     float x1 = scale *( xoff + DrawingUtil.toSceneX( t.e, t.s ) );
            //     float y1 = scale *( yoff + DrawingUtil.toSceneY( t.e, t.s ) );
            //     DXF.printXYZ( pw4, x0, -y0, 0.0f, 0 );
            //     DXF.printXYZ( pw4, x1, -y1, 0.0f, 1 );
            //   } else if ( PlotType.isProfile( type ) ) {
            //     float x0 = scale *( xoff + DrawingUtil.toSceneX( f.h, f.v ) );
            //     float y0 = scale *( yoff + DrawingUtil.toSceneY( f.h, f.v ) );
            //     float x1 = scale *( xoff + DrawingUtil.toSceneX( t.h, t.v ) );
            //     float y1 = scale *( yoff + DrawingUtil.toSceneY( t.h, t.v ) );
            //     DXF.printXYZ( pw4, x0, -y0, 0.0f, 0 );
            //     DXF.printXYZ( pw4, x1, -y1, 0.0f, 1 );
            //   // } else if ( type == PlotType.PLOT_SECTION ) {
            //   //   /* nothing */
            //   }
            // // }
            DXF.printXYZ( pw4, scale*(xoff + sh.x1), -scale*(yoff + sh.y1), z, 0 );
            DXF.printXYZ( pw4, scale*(xoff + sh.x2), -scale*(yoff + sh.y2), z, 1 );
            out.write( sw4.getBuffer().toString() );
            out.flush();
          }
          for ( DrawingPath sh : plot.getSplays() ) {
            DBlock blk = sh.mBlock;
            if ( blk == null ) continue;

            StringWriter sw41 = new StringWriter();
            PrintWriter pw41  = new PrintWriter(sw41);
            // // if ( sh.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
            //   NumStation f = num.getStation( blk.mFrom );

            //   DXF.printString( pw41, 0, "LINE" );
            //   handle = DXF.printAcDb( pw41, handle, DXF.AcDbEntity, DXF.AcDbLine );
            //   DXF.printString( pw41, 8, "SPLAY" );
            //   // DXF.printInt( pw41, 39, 1 );         // line thickness

            //   float dhs = scale * blk.mLength * (float)Math.cos( blk.mClino * TDMath.DEG2RAD )*sc1/10; // scaled dh
            //   if ( type == PlotType.PLOT_PLAN ) {
            //     float x = scale * DrawingUtil.toSceneX( f.e, f.s );
            //     float y = scale * DrawingUtil.toSceneY( f.e, f.s );
            //     float de =   dhs * (float)Math.sin( blk.mBearing * TDMath.DEG2RAD);
            //     float ds = - dhs * (float)Math.cos( blk.mBearing * TDMath.DEG2RAD);
            //     DXF.printXYZ( pw41, x, -y, 0.0f, 0 );
            //     DXF.printXYZ( pw41, x + de, -(y+ds), 0.0f, 1 );
            //   } else if ( PlotType.isProfile( type ) ) {
            //     float x = scale * DrawingUtil.toSceneX( f.h, f.v );
            //     float y = scale * DrawingUtil.toSceneY( f.h, f.v );
            //     float dv = - blk.mLength * (float)Math.sin( blk.mClino * TDMath.DEG2RAD )*sc1/10;
            //     DXF.printXYZ( pw41, x, -y, 0.0f, 0 );
            //     DXF.printXYZ( pw41, x+dhs*blk.getReducedExtend(), -(y+dv), 0.0f, 1 ); 
            //   } else if ( type == PlotType.PLOT_SECTION ) {
            //     // nothing
            //   }
            // // }

            DXF.printString( pw41, 0, "LINE" );
            handle = DXF.printAcDb( pw41, handle, DXF.AcDbEntity, DXF.AcDbLine );
            DXF.printString( pw41, 8, "SPLAY" );
            // DXF.printInt( pw41, 39, 1 );         // line thickness
            DXF.printXYZ( pw41, scale*(xoff + sh.x1), -scale*(yoff + sh.y1), z, 0 );
            DXF.printXYZ( pw41, scale*(xoff + sh.x2), -scale*(yoff + sh.y2), z, 1 );

            out.write( sw41.getBuffer().toString() );
            out.flush();
          }
        }

        // FIXME station scale is 0.3
        for ( ICanvasCommand cmd : plot.getCommands() ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath path = (DrawingPath)cmd;
          z = TDSetting.mAcadLayer? path.mLevel : path.mScrap;
          int scrap_flag = TDSetting.mAcadLayer? path.mScrap : path.mLevel;

          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);

          if ( path.mType == DrawingPath.DRAWING_PATH_STATION )
          {
            DrawingStationUser sp = (DrawingStationUser)path;
            handle = DXF.printText( pw5, handle, model_block_handle, sp.name(), (sp.cx+xoff) * scale, -(sp.cy+yoff) * scale,
                                0, LABEL_SCALE, "STATION", DXF.style_dejavu, xoff, yoff, z, scrap_flag, DXF.BY_LAYER );
          }
          else if ( path.mType == DrawingPath.DRAWING_PATH_LINE )
          {
            handle = toDxf( pw5, handle, model_record_handle, (DrawingLinePath)path, scale, xoff, yoff, z, scrap_flag, false );
          }
          else if ( path.mType == DrawingPath.DRAWING_PATH_AREA )
          {
            handle = toDxf( pw5, handle, model_record_handle, (DrawingAreaPath)path, scale, xoff, yoff, z, scrap_flag, false );
          }
          else if ( path.mType == DrawingPath.DRAWING_PATH_POINT )
          {
            DrawingPointPath point = (DrawingPointPath) path;
	        String name = point.getThName();
	        String th_name = replaceColon( point.getThName() );
            // int idx = 1 + point.mPointType;
	        if ( name.equals("label") ) {
                // HBX_DXF
                String layer2 = TDSetting.mAcadLayer?  "SCRAP_" + Integer.toString( path.mScrap ) // z
                                                    : "P_" + th_name;
              DrawingLabelPath label = (DrawingLabelPath)point;
              DXF.printString(pw5, 0, "TEXT");
              if ( DXF.mVersion13_14 ) {
                handle = DXF.inc( handle );
                DXF.printHex(pw5, 5, handle );
                DXF.printHex(pw5, 330, model_block_handle );
                DXF.printString(pw5, 100, "AcDbEntity" );
              }
              DXF.printString( pw5, 8, layer2 );

              if ( DXF.mVersion13_14 ) {
                DXF.printString(pw5, 100, "AcDbText" );
              }
              DXF.printXYZ(pw5, (point.cx + xoff) * scale, -(point.cy + yoff) * scale, z, 0);
              DXF.printFloat(pw5, 40, point.getScaleValue() * 1.4f / 5 );
              DXF.printFloat(pw5, 50, 360.0f - (float)(point.mOrientation));
              DXF.printFloat(pw5, 51, 0.0f );
              DXF.printString(pw5, 1, label.mPointText);
              if ( DXF.mVersion13_14 ) {
                DXF.printString(pw5, 100, "AcDbText" );
              }
            } else {
              boolean done_point = false;
              if ( BrushManager.isPointSection( point.mPointType ) && TDSetting.mAutoXSections ) {
                // TDLog.v( "Point is section : " + TDSetting.mAutoXSections );
                String scrapname = TDUtil.replacePrefix( TDInstance.survey, point.getOption( TDString.OPTION_SCRAP ) );
                // TDLog.v( "Point is section : scrapname " + scrapname );
                if ( scrapname != null ) {
                  String scrapfile = scrapname + ".tdr";
                  if ( DXF.mVersion13_14 ) {
                    handle = tdrToDxf( pw5, handle, model_record_handle, model_record_handle, scrapfile,
                                       scale, point.cx, point.cy, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y, z, scrap_flag );
                    done_point = true;
                  } else { // mVersion9 - TRY
                    handle = tdrToDxf( pw5, handle, model_record_handle, model_record_handle, scrapfile,
                                       scale, point.cx, point.cy, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y, z, scrap_flag );
                    done_point = true;
                  }
                  IDrawingLink link = point.mLink;
                  if ( link != null ) {
                    // TODO line connecting point and link
                    // HBX_DXF
                    String layer2 = TDSetting.mAcadLayer?  "SCRAP_" + Integer.toString( path.mScrap ) // z
                                                        : "LINK";
                    StringWriter sw4l = new StringWriter();
                    PrintWriter pw4l  = new PrintWriter(sw4l);
                    DXF.printString( pw4l, 0, "LINE" );
                    handle = DXF.printAcDb( pw4l, handle, DXF.AcDbEntity, DXF.AcDbLine );
                    DXF.printString( pw4l, 8, layer2 );
                    DXF.printString( pw4l, 6, TDSetting.mAcadLayer? "L_LINK":DXF.lt_byLayer ); // lt_byLayer );// HBX_DXF
                    DXF.printInt( pw4l, 62, TDSetting.mAcadLayer? DXF.LNK_color:DXF.BY_LAYER );// HBX_DXF
                    // DXF.printInt( pw4l, 39, 1 );         // line thickness
                    DXF.printXYZ( pw4l, scale*(xoff + point.cx), -scale*(yoff + point.cy), z, 0 );
                    DXF.printXYZ( pw4l, scale*(xoff + link.getLinkX()), -scale*(yoff + link.getLinkY()), z, 1 );
                    out.write( sw4l.getBuffer().toString() );
                    out.flush();
                  }
                }
              }
              if ( ! done_point ) {
                if ( DXF.mVersion13_14 ) {
                  handle = toDxf( pw5, handle, model_block_handle, model_record_handle, point, scale, xoff, yoff, z, scrap_flag );
                } else {
                    // HBX_DXF
                    String layer2 = TDSetting.mAcadLayer?  "SCRAP_" + Integer.toString( path.mScrap ) // z
                                                        : "P_" + th_name;
                  // String th_name = replaceColon( point.getThName() );
                  DXF.printString( pw5, 0, "INSERT" );
                  DXF.printString( pw5, 8, layer2 );
                  DXF.printString( pw5, 2, "B_" + th_name );
                  //DXF.printFloat( pw5, 41, point.getScaleValue()*1.4f ); // FIX Asenov
                  //DXF.printFloat( pw5, 42, point.getScaleValue()*1.4f );
		  DXF.printFloat( pw5, 41, point.getScaleValue()*TDSetting.mUnitIcons); //HBX unit
                  DXF.printFloat( pw5, 42, point.getScaleValue()*TDSetting.mUnitIcons); //HBX unit
                  DXF.printFloat( pw5, 50, 360.0f-(float)(point.mOrientation) );
                  DXF.printXYZ( pw5, (point.cx+xoff)*scale, -(point.cy+yoff)*scale, z, 0 );
                }
              }
            }
	  }
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }
        StringWriter sw6 = new StringWriter();
        PrintWriter pw6  = new PrintWriter(sw6);
        if ( TDSetting.mAutoStations ) { // whether to add stations automatically to scrap therion files
          z = -1.0f;
          for ( DrawingStationName st : plot.getStations() ) { // auto-stations
            handle = toDxf( pw6, handle, model_block_handle, st, scale, xoff+1.0f, yoff-1.0f, z, scrap );
	    float len = 2.0f + st.getName().length() * 5.0f; // FIXME fonts ?
            handle = DXF.printLine( pw6,scale,handle,"STATION", xoff+st.cx, -(yoff+st.cy), xoff+st.cx+len, -(yoff+st.cy), z );
          }
          out.write( sw6.getBuffer().toString() );
          out.flush();
        } else {
          for ( DrawingStationUser st_path : plot.getUserStations() ) { // user-chosen
            z = TDSetting.mAcadLayer? st_path.mLevel : st_path.mScrap;
            handle = toDxf( pw6, handle, model_block_handle, st_path, scale, xoff, yoff, z, st_path.mLevel ); // scrap_flag = st_path.mLevel
          }
          out.write( sw6.getBuffer().toString() );
          out.flush();
        }
      }
      DXF.writeEndSection( out );
      if ( DXF.mVersion13_14 ) {
        handle = DXF.writeSectionObjects( out, handle );
      }
      DXF.writeString( out, 0, "EOF" );
      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TDLog.e( "DXF io-exception " + e.toString() );
    }
  }

  /** write a station name to DXF format
   * @param pw         output writer
   * @param handle     ACAD handle
   * @param ref_handle ACAD handle of the reference object
   * @param sn         station name
   * @param scale      scale factor
   * @param xoff       X offset
   * @param yoff       Y offset
   * @param z          Z "level"
   * @param scrap      scrap index or layer
   */
  static private int toDxf( PrintWriter pw, int handle, int ref_handle, DrawingStationName sn, float scale,
                            float xoff, float yoff, float z, int scrap )
  { // FIXME point scale factor is 0.3
    if ( sn == null ) return handle;
    return DXF.printText( pw, handle, ref_handle, sn.getName(),  (sn.cx+xoff)*scale, -(sn.cy+yoff)*scale, 0,
                        STATION_SCALE, "STATION", DXF.style_dejavu, xoff, yoff, z, scrap, DXF.BY_LAYER );
  }

  /** write a station point to DXF format
   * @param pw         output writer
   * @param handle     ACAD handle
   * @param ref_handle ACAD handle of the reference object
   * @param sp         station point
   * @param scale      scale factor
   * @param xoff       X offset
   * @param yoff       Y offset
   * @param z          Z "level"
   * @param scrap      scrap index or layer
   */
  static private int toDxf( PrintWriter pw, int handle, int ref_handle, DrawingStationUser sp, float scale,
                            float xoff, float yoff, float z, int scrap )
  { // FIXME point scale factor is 0.3
    if ( sp == null ) return handle;
    return DXF.printText( pw, handle, ref_handle, sp.name(),  (sp.cx+xoff)*scale, -(sp.cy+yoff)*scale, 0,
                        STATION_SCALE, "STATION", DXF.style_dejavu, xoff, yoff, z, scrap, DXF.BY_LAYER );
  }

  /** write a point item to DXF format
   * @param pw         output writer
   * @param handle     ACAD handle
   * @param ref_handle ACAD handle of the reference object
   * @param model_record_handle ...
   * @param point      point item
   * @param scale      scale factor
   * @param xoff       X offset
   * @param yoff       Y offset
   * @param z          Z "level"
   * @param scrap      scrap index or layer
   */
  static private int toDxf( PrintWriter pw, int handle, int ref_handle, int model_record_handle, DrawingPointPath point, float scale,
                            float xoff, float yoff, float z, int scrap )
  { // FIXME point scale factor is 0.3
    if ( point == null ) return handle;
    //String th_name = replaceColon( point.getThName() );
    // HBX_DXF
    //String layer2;
    if ( BrushManager.isPointLabel( point.mPointType ) ) {
      DrawingLabelPath label = (DrawingLabelPath)point;
      // TDLog.v( "LABEL PATH label <" + label.mPointText + ">" );
      return DXF.printText( pw, handle, ref_handle,
              label.mPointText,
              (point.cx+xoff)*scale,
              -(point.cy+yoff)*scale, 360.0f-(float)label.mOrientation,
              LABEL_SCALE,
              (TDSetting.mAcadLayer?  "SCRAP_" + Integer.toString( scrap ) : "POINT"),
              DXF.style_dejavu,
              xoff, yoff, z, scrap,
              DXF.BY_LAYER );
      // HBX_DXF
    }
    String th_name = replaceColon( point.getThName() );
    // TDLog.v( "POINT PATH <" + th_name + "> " + String.format("%X %X", ref_handle, model_record_handle) );
    // int idx = 1 + point.mPointType;
    DXF.printString( pw, 0, "INSERT" );
    handle = DXF.printAcDbModelSpace( pw, handle, model_record_handle,
            (TDSetting.mAcadLayer?  "SCRAP_" + Integer.toString( scrap ) : "P_"+th_name),
            "AcDbBlockReference" );
    DXF.printString( pw, 2, "B_" + th_name );
    //DXF.printFloat( pw, 41, point.getScaleValue()*1.4f ); // FIX Asenov
    //DXF.printFloat( pw, 42, point.getScaleValue()*1.4f );
    DXF.printFloat( pw, 41, point.getScaleValue()*TDSetting.mUnitIcons); //HBX unit
    DXF.printFloat( pw, 42, point.getScaleValue()*TDSetting.mUnitIcons); //HBX unit
    DXF.printFloat( pw, 50, 360.0f-(float)(point.mOrientation) );
    DXF.printXYZ( pw, (point.cx+xoff)*scale, -(point.cy+yoff)*scale, z, 0 );
    return handle;
  }

  /** write a line item to DXF format
   * @param pw         output writer
   * @param handle     ACAD handle
   * @param ref_handle ACAD handle of the reference object
   * @param line       line item
   * @param scale      scale factor
   * @param xoff       X offset
   * @param yoff       Y offset
   * @param z          Z "level"
   * @param scrap      scrap index or (unused) layer
   * @param p3D        3d polyline (future use)
   */
  static private int toDxf( PrintWriter pw, int handle, int ref_handle, DrawingLinePath line, float scale,
                            float xoff, float yoff, float z, int scrap, boolean p3D )
  {
    if ( line == null ) return handle;
    String layer = "L_" + replaceColon( line.getThName( ) );
    // int flag = 0;
    // HBX_DXF
    String linetype, layer2;
    int color;
    if ( TDSetting.mAcadLayer ) {
      linetype = layer;
      layer2 = "SCRAP_" + Integer.toString( scrap ); //
      //layer = inttostr(z);
      Symbol line2 = BrushManager.getLineByIndex(BrushManager.getLineIndexByThName(line.getThName()));
      color = 1;
      try {
        color = DxfColor.rgbToIndex( line2.getColor() );
      } catch ( NullPointerException e ) {
        TDLog.e( e.getMessage() );
      }
      // TDLog.v( "HBX_DXF <" + line.getThName() + "> " + String.format("%X %X ", ref_handle, color) + layer + layer2+" "+color);
    } else {
      linetype = DXF.lt_byLayer;
      layer2 = layer;
      color = DXF.BY_LAYER;
    }
    // END HBX_DXF
    if ( DXF.mVersion13_14 && checkSpline( line ) ) {
      if ( TDSetting.mAcadSpline ) {
        int npt = countInterpolatedPolylinePoints( line, line.isClosed() );
        handle = DXF.printPolylineHeader( pw, handle, ref_handle, layer2, line.isClosed(), npt, linetype, color, z, p3D );
        int polyline_handle = handle;
        handle = printInterpolatedPolyline( pw, line, scale, handle, handle, layer2, line.isClosed(), xoff, yoff, z, linetype, color, p3D );
        handle = DXF.printPolylineFooter( pw, handle, polyline_handle, layer2 );
      } else {
        handle = printSpline( pw, line, scale, handle, layer2, line.isClosed(), xoff, yoff, z, linetype, color );
      }
    } else {
      // handle = printLWPolyline( pw5, line, scale, handle, layer, false );
      handle = printPolyline( pw, line, scale, handle, ref_handle, layer2, line.isClosed(), xoff, yoff, z, linetype, color, p3D );
    }
    return handle;
  }

  /** write a area item to DXF format
   * @param pw         output writer
   * @param handle     ACAD handle
   * @param ref_handle ACAD handle of the reference object
   * @param area       area item
   * @param scale      scale factor
   * @param xoff       X offset
   * @param yoff       Y offset
   * @param z          Z "level"
   * @param scrap      scrap index or (unused) layer
   * @param p3D        3d polyline (future use)
   */
  static private int toDxf( PrintWriter pw, int handle, int ref_handle, DrawingAreaPath area, float scale,
                            float xoff, float yoff, float z, int scrap, boolean p3D )
  {
    if ( area == null ) return handle;
    // float bezier_step = TDSetting.getBezierStep();
    // TDLog.v( "area size " + area.size() );
    String layer = "A_" + replaceColon( area.getThName( ) );
    // HBX_DXF
    String linetype, layer2;
    int color;
    if ( TDSetting.mAcadLayer ) {
      linetype = layer;
      layer2 = "SCRAP_" + Integer.toString( scrap );
      // layer = inttostr(z);
      Symbol area2 = BrushManager.getAreaByIndex(BrushManager.getAreaIndexByThName(area.getThName()));
      color = 1;
      try {
        DxfColor.rgbToIndex(area2.getColor());
      } catch ( NullPointerException e ) {
        TDLog.e( e.getMessage() );
      }
    } else {
      linetype = DXF.lt_byLayer;
      layer2 = layer;
      color = DXF.BY_LAYER;
    }
    // END HBX_DXF
    if ( DXF.mVersion13_14 && checkSpline( area ) ) {
      if ( TDSetting.mAcadSpline ) {
        int npt = countInterpolatedPolylinePoints( area, true );
        handle = DXF.printPolylineHeader( pw, handle, ref_handle, layer2, true, npt, linetype, color, z, p3D );
        int polyline_handle = handle;
        handle = printInterpolatedPolyline( pw, area, scale, handle, handle, layer2, true, xoff, yoff, z, linetype, color, false );
        handle = DXF.printPolylineFooter( pw, handle, polyline_handle, layer2 );
      } else {
        handle = printSpline( pw, area, scale, handle, layer2, true, xoff, yoff, z );
      }
    } else {
      // handle = printLWPolyline( pw5, line, scale, handle, layer, true );
      handle = printPolyline( pw, area, scale, handle, ref_handle, layer2, true, xoff, yoff, z, linetype, color, false );
    }
    if ( DXF.mVersion13_14 ) {
      int npt = countInterpolatedPolylinePoints( area, true );
      handle = DXF.printHatchHeader( pw, handle, ref_handle, layer2, npt, linetype, color );
      int hatch_handle = handle;
      printInterpolatedPolyline( pw, area, scale, 0, handle, null, true, xoff, yoff, z, linetype, color, false );
      handle = DXF.printHatchFooter( pw, handle, hatch_handle );
    }
    return handle;
  }

  /** write a scrap to DXF format (used for xsections)
   * @param pw         output writer
   * @param handle     ACAD handle
   * @param ref_handle ACAD handle of the reference object
   * @param model_record_handle ...
   * @param scrapfile  name of the TDR file of the scrap
   * @param scale      scale factor
   * @param dx         X shift (applied when the scrap is read from file)
   * @param dy         Y shift
   * @param xoff       X offset
   * @param yoff       Y offset
   * @param z          Z "level"
   * @param scrap      scrap index - for scrap items
   */
  static private int tdrToDxf( PrintWriter pw, int handle, int ref_handle, int model_record_handle, String scrapfile,
                               float scale, float dx, float dy, float xoff, float yoff, float z, int scrap )
  {
    try {
      // TDLog.Log( TDLog.LOG_IO, "tdr to dxf. scrapfile " + scrapfile );
      // TDLog.v( "tdr to dxf. scrapfile " + scrapfile );
      FileInputStream fis = TDFile.getFileInputStream( TDPath.getTdrFile( scrapfile ) );
      BufferedInputStream b_fis = new BufferedInputStream( fis );
      DataInputStream dis = new DataInputStream( b_fis );
      int version = DrawingIO.skipTdrHeader( dis );
      // TDLog.v( "tdr to svg delta " + dx + " " + dy + " Offset " + xoff + " " + yoff );

      DrawingPath path; // = null;

      boolean done = false;
      while ( ! done ) {
        int what = dis.read();
        switch ( what ) {
          case 'P':
            path = DrawingPointPath.loadDataStream( version, dis, dx, dy /*, null */ );
            handle = toDxf( pw, handle, ref_handle, model_record_handle, (DrawingPointPath)path, scale, xoff, yoff, z, scrap );
            break;
          case 'T':
            path = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
            handle = toDxf( pw, handle, ref_handle, model_record_handle, (DrawingLabelPath)path, scale, xoff, yoff, z, scrap );
            break;
          case 'L':
            path = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, null */ );
            handle = toDxf( pw, handle, ref_handle, (DrawingLinePath)path, scale, xoff, yoff, z, scrap, false );
            break;
          case 'A':
            path = DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, null */ );
            handle = toDxf( pw, handle, ref_handle, (DrawingAreaPath)path, scale, xoff, yoff, z, scrap, false );
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
          case 'J':
            /* path = */ DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
            break;
          case 'N': // scrap index
            int scrap_index = dis.readInt();
            TDLog.v( "DXF (tdr to dxf) scrap index " + scrap_index + " scrap flag " + scrap );
            break;
          // case 'G':
          //   path = DrawingFixedName.loadDataStream( version, dis ); // consume DrawingFixedName data
          //   break;
          case 'F':
            done = true;
            break;
	  default:
	    TDLog.e("TDR2DXF Error. unexpected code=" + what );
	    return handle;
        }
      }
    } catch ( FileNotFoundException e ) { // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return handle;
  }

  /** @return the string with colon replaced with dash
   * @param input   input string
   */
  static private String replaceColon( String input )
  {
    try {
      return input.replace(':','-');
    } catch ( NullPointerException e ) {
      // TDLog.v("Error " + e.getMessage() );
      return input;
    }
  }

}


