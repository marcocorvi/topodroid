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
package com.topodroid.DistoX;

import com.topodroid.utils.TDString;
import com.topodroid.math.Point2D;
import com.topodroid.math.BezierCurve;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

import android.util.Log;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.num.TDNum;

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

class DrawingDxf
{
  static final private float POINT_SCALE   = 10.0f; // scale of point icons: only ACAD_6
  // the next three are for text
  static final private float STATION_SCALE =  6.0f / DrawingUtil.SCALE_FIX; // scale of station names
  static final private float LABEL_SCALE   =  8.0f / DrawingUtil.SCALE_FIX; // scale of label text
  static final private float AXIS_SCALE    = 10.0f / DrawingUtil.SCALE_FIX; // scale of text on the axes

  static final private String two_n_half = "2.5";
  // static final String ten = "10";


  // static void printEndText( PrintWriter pw, String style )
  // {
  //   DXF.printString( pw, 7, style );
  //   DXF.printString( pw, 100, DXF.AcDbText );
  // }


  static private int countInterpolatedPolylinePoints(  DrawingPointLinePath line, boolean closed )
  {
    float bezier_step = TDSetting.getBezierStep();
    int npt = 0;
    LinePoint p = line.mFirst;
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

  static private int printInterpolatedPolyline(  PrintWriter pw, DrawingPointLinePath line, float scale, int handle, int ref,
                                    String layer, boolean closed, float xoff, float yoff )
  {
    float bezier_step = TDSetting.getBezierStep();
    LinePoint p = line.mFirst;
    float x0 = xoff + p.x;
    float y0 = yoff + p.y;
    if ( layer != null ) {
      handle = DXF.printLinePoint( pw, scale, handle, ref, layer, x0, y0 );
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
              handle = DXF.printLinePoint( pw, scale, handle, ref, layer, pb.x, pb.y );
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
        handle = DXF.printLinePoint( pw, scale, handle, ref, layer, x3, y3 );
      } else {
        DXF.printXY( pw, x3*scale, -y3*scale, 0 );
      }
      x0 = x3;
      y0 = y3;
    }
    if ( closed ) {
      p = line.mFirst;
      if ( layer != null ) {
        handle = DXF.printLinePoint( pw, scale, handle, ref, layer, xoff+p.x, yoff+p.y );
      } else {
        DXF.printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
      }
    }
    return handle;
  }

  static private int printPolyline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle,
                                    String layer, boolean closed, float xoff, float yoff )
  {
    handle = DXF.printPolylineHeader( pw, handle, layer, closed );
    handle = printInterpolatedPolyline( pw, line, scale, handle, handle, layer, closed, xoff, yoff );
    handle = DXF.printPolylineFooter( pw, handle );
    return handle;
  }

  // static private int printLWPolyline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, String layer, boolean closed,
  //                             float xoff, float yoff )
  // {
  //   int close = (closed ? 1 : 0 );
  //   DXF.printString( pw, 0, "LWPOLYLINE" );
  //   handle = DXF.inc(handle);
  //       DXF.printAcDb( pw, handle, DXF.AcDbEntity, DXF.AcDbPolyline );
  //   DXF.printString( pw, 8, layer );
  //   DXF.printInt( pw, 38, 0 ); // elevation
  //   DXF.printInt( pw, 39, 1 ); // thickness
  //   DXF.printInt( pw, 43, 1 ); // start width
  //   DXF.printInt( pw, 70, close ); // not closed
  //   DXF.printInt( pw, 90, line.size() ); // nr. of points
  //   for (LinePoint p = line.mFirst; p != null; p = p.mNext ) { 
  //     DXF.printXY( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0 );
  //   }
  //   return handle;
  // }

  static private boolean checkSpline( DrawingPointLinePath line )
  {
    if ( DXF.mVersion13 ) {
      for ( LinePoint p = line.mFirst; p != null; p = p.mNext ) {
        if ( p.has_cp ) {
          return true;
        }
      }
    }
    return false;
  }

  static private int printSpline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, String layer, boolean closed,
                          float xoff, float yoff )
  {
    // Log.v("DistoXdxf", "print spline");
    DXF.printString( pw, 0, "SPLINE" );
    handle = DXF.inc(handle); DXF.printAcDb( pw, handle, DXF.AcDbEntity, "AcDbSpline" );
    DXF.printString( pw, 8, layer );
    DXF.printString( pw, 6, DXF.lt_continuous );
    DXF.printFloat( pw, 48, 1.0f ); // scale 
    DXF.printInt( pw, 60, 0 ); // visibilty (0: visible, 1: invisible)
    DXF.printInt( pw, 66, 1 ); // group 1: "entities follow" flag
    // DXF.printInt( pw, 67, 0 ); // in model space [default]
    DXF.printXYZ( pw, 0, 0, 1, 200 ); // normal vector

    float xt=0, yt=0;
    int np = 2;
    LinePoint p = line.mFirst; 
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
      DXF.printXYZ( pw, xt/d, -yt/d, 0, 2 );

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
      DXF.printXYZ( pw, xt/d, -yt/d, 0, 3 );
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
      
    Log.v("DistoX", "Spline P " + np + " Cp " + ncp + " K " + nk );
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

    p = line.mFirst; 
    xt = p.x;
    yt = p.y;
    DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );         // control points: 1 + 3 * (NP - 1) = 3 NP - 2
    for ( p = p.mNext; p != null; p = p.mNext ) { 
      if ( p.has_cp ) {
        DXF.printXYZ( pw, (p.x1+xoff) * scale, -(p.y1+yoff) * scale, 0.0f, 0 );
        DXF.printXYZ( pw, (p.x2+xoff) * scale, -(p.y2+yoff) * scale, 0.0f, 0 );
      } else {
        DXF.printXYZ( pw, (xt+xoff) * scale, -(yt+yoff) * scale, 0.0f, 0 );
        DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );
      }
      DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );
      xt = p.x;
      yt = p.y;
    }
/*
    if ( closed ) {
      p = line.mFirst;
      DXF.printXYZ( pw, (xt+xoff) * scale, -(yt+yoff) * scale, 0.0f, 0 );
      DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );
    }
*/

    for ( p = line.mFirst; p != null; p = p.mNext ) { 
      DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 1 );  // fit points: NP
    }
/*
    if ( closed ) {
      p = line.mFirst;
      DXF.printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 1 );  // fit points: NP
    }
*/
    return handle;
  }


  static void writeDxf( BufferedWriter out, TDNum num, /* DrawingUtil util, */ DrawingCommandManager plot, long type )
  {
    DXF.mVersion13 = (TDSetting.mAcadVersion >= 13);
    DXF.mVersion16 = (TDSetting.mAcadVersion >= 16);
    
    float scale = 1.0f/DrawingUtil.SCALE_FIX; // TDSetting.mDxfScale; 
    float xoff = 0;
    float yoff = 0;
    int handle = 0;
    int ref    = 0;
    RectF bbox = plot.getBoundingBox( );
    float xmin = bbox.left;
    float xmax = bbox.right;
    float ymin = bbox.top;
    float ymax = bbox.bottom;
    xmin *= scale;
    xmax *= scale;
    ymin *= scale;
    ymax *= scale;

    int[] point_record_handle = new int[ BrushManager.getPointLibSize() ];

    // Log.v("DistoX", "DXF X " + xmin + " " + xmax + " Y " + ymin + " " + ymax );

    try {
      // header
      DXF.writeComment( out, "DXF created by TopoDroid v. " + TDVersion.string() 
        + " " + TDSetting.mAcadVersion + " " + (TDSetting.mAutoStations? "T ":"F ") + (TDSetting.mAutoXSections? "T ":"F ") + TDSetting.getBezierStep() );

      xmin -= 2;  xmax += 2;
      ymin -= 2;  ymax += 2;

      handle = DXF.writeHeaderSection( out, handle, xmin, -ymax, xmax, -ymin );
      handle = DXF.writeClassesSection( out, handle );

      DXF.writeSection( out, "TABLES" );

      handle = DXF.writeVportTable( out, handle, xmin, -ymax, xmax, -ymin );
      handle = DXF.writeStylesTable( out, handle );
      handle = DXF.writeLTypesTable( out, handle );

      int nr_layers = 7;
      SymbolPointLibrary pointlib = BrushManager.getPointLib();
      SymbolLineLibrary linelib   = BrushManager.getLineLib();
      SymbolAreaLibrary arealib   = BrushManager.getAreaLib();
      // nr_layers += 1 + linelib.size() + arealib.size();
      nr_layers += 1 + BrushManager.getLineLibSize() + BrushManager.getAreaLibSize();
      if ( DXF.mVersion13 ) { handle = DXF.inc(handle); } else { handle = 2; }
      DXF.writeBeginTable( out, "LAYER", handle, nr_layers );
      {
        StringWriter sw2 = new StringWriter();
        PrintWriter pw2  = new PrintWriter(sw2);

        // 2 layer name, 70 flag (64), 62 color code, 6 line type
        int flag = 0;
        int color = 1;
        // if ( ! DXF.mVersion13 ) { handle = 40; }
        handle = DXF.inc(handle); DXF.printLayer( pw2, handle, "0",       flag, 7, DXF.lt_continuous ); // LAYER "0" .. FIXME DraftSight ..must be AutoCAD white
        handle = DXF.inc(handle); DXF.printLayer( pw2, handle, "LEG",     flag, color, DXF.lt_continuous ); ++color; // red
        handle = DXF.inc(handle); DXF.printLayer( pw2, handle, "SPLAY",   flag, color, DXF.lt_continuous ); ++color; // yellow
        handle = DXF.inc(handle); DXF.printLayer( pw2, handle, "STATION", flag, color, DXF.lt_continuous ); ++color; // green
        handle = DXF.inc(handle); DXF.printLayer( pw2, handle, "LINE",    flag, color, DXF.lt_continuous ); ++color; // cyan
        handle = DXF.inc(handle); DXF.printLayer( pw2, handle, "POINT",   flag, color, DXF.lt_continuous ); ++color; // blue
        handle = DXF.inc(handle); DXF.printLayer( pw2, handle, "AREA",    flag, color, DXF.lt_continuous ); ++color; // magenta
        handle = DXF.inc(handle); DXF.printLayer( pw2, handle, "REF",     flag, color, DXF.lt_continuous ); ++color; // white
        
        color = 10;
        if ( linelib != null ) { // always true
          for ( Symbol line : linelib.getSymbols() ) {
            String lname = "L_" + line.getThName().replace(':','-');
            String ltype = DXF.lt_continuous;
            if ( DXF.mVersion13 && ! DXF.mVersion16 ) {
              if ( lname.equals("L_pit") ) { 
                ltype = DXF.lt_ticks;
              } else if ( lname.equals("L_border" ) ) {
                ltype = DXF.lt_center;
              }
            }
            handle = DXF.inc(handle); DXF.printLayer( pw2, handle, lname, flag, color, ltype ); 
            if ( ++color >= 256 ) color = 1;
          }
        }

        color = 60;
        if ( arealib != null ) { // always true
          for ( Symbol s : arealib.getSymbols() ) {
            String aname = "A_" + s.getThName().replace(':','-');
            handle = DXF.inc(handle); DXF.printLayer( pw2, handle, aname, flag, color, DXF.lt_continuous ); 
            if ( ++color >= 256 ) color = 1;
          }
        }
        color = 80;
        if ( pointlib != null ) { // always true
          for ( Symbol point : pointlib.getSymbols() ) {
            String pname = "P_" + point.getThName().replace(':','-');
            handle = DXF.inc(handle); DXF.printLayer( pw2, handle, pname, flag, color, DXF.lt_continuous ); 
            if ( ++color >= 256 ) color = 1;
          }
        }
        out.write( sw2.getBuffer().toString() );
      }
      DXF.writeEndTable( out );

      handle = DXF.writeExtraTables( out, handle );
      handle = DXF.writeDimstyleTable( out, handle );

      if ( DXF.mVersion13 ) {
        handle = DXF.inc(handle);
        DXF.writeBeginTable( out, "BLOCK_RECORD", handle, BrushManager.getPointLibSize() );
        {
          handle = DXF.writeSpaceBlockRecord( out, "*Model_Space", handle );
          handle = DXF.writeSpaceBlockRecord( out, "*Paper_Space", handle );

          for ( int n = 0; n < BrushManager.getPointLibSize(); ++ n ) {
            String th_name = BrushManager.getPointThName(n).replace(':','-');
            DXF.writeString( out, 0, "BLOCK_RECORD" );
            handle = DXF.inc(handle);
            point_record_handle[ n ] = handle;
            DXF.writeAcDb( out, handle, DXF.AcDbSymbolTR, "AcDbBlockTableRecord" );
            DXF.writeString( out, 8, "P_" + th_name );
            DXF.writeString( out, 2, "B_" + th_name ); // block name
            DXF.writeInt( out, 70, 0 );                // block insertion units
          }
        }
        DXF.writeEndTable( out );
      }
      
      DXF.writeEndSection( out );
      out.flush();
      
      DXF.writeSection( out, "BLOCKS" );
      {
        if ( DXF.mVersion13 ) {
          handle = DXF.writeSpaceBlock( out, "*Model_Space", handle );
          handle = DXF.writeSpaceBlock( out, "*Paper_Space", handle );
        }

        // // 8 layer (0), 2 block name,
        for ( int n = 0; n < BrushManager.getPointLibSize(); ++ n ) {
          SymbolPoint pt = (SymbolPoint)BrushManager.getPointByIndex(n);
	  String th_name = pt.getThName().replace(':','-');
          DXF.writeString( out, 0, "BLOCK" );
          handle = DXF.inc(handle);
          DXF.writeAcDb( out, handle, DXF.AcDbEntity, "AcDbBlockBegin" );
          // DXF.writeString( out, 8, "P_" + th_name );
          DXF.writeString( out, 2, "B_" + th_name ); // block name, can be repeated with '3'
          DXF.writeInt( out, 70, 0 );       // flag 0=none, 1=anonymous, 2=non-conts attr, 4=xref, 8=xref overlay,
                                        // 16=ext. dependent, 32=ext. resolved (ignored), 64=referenced xref (ignored)
          DXF.writeXYZ( out, 0, 0, 0, 0 );
          // out.write( pt.getDxf() );
          SymbolPointDxf point_dxf = pt.getDxf();
          handle = point_dxf.writeDxf( out, 13, handle, point_record_handle[n] );

          DXF.writeString( out, 0, "ENDBLK" );
          if ( DXF.mVersion13 ) {
            handle = DXF.inc(handle);
            DXF.writeAcDb( out, handle, DXF.AcDbEntity, "AcDbBlockEnd");
            // DXF.writeString( out, 8, "POINT" );
            DXF.writeString( out, 8, "P_" + th_name );
          }
        }
      }
      DXF.writeEndSection( out );
      out.flush();

      DXF.writeSection( out, "ENTITIES" );
      {
	String scale_len = TDString.TWENTY;
        float sc1 = 20; // DrawingUtil.SCALE_FIX / 2 = 10;

        // reference
        StringWriter sw9 = new StringWriter();
        PrintWriter pw9  = new PrintWriter(sw9);
	float sc2 = sc1 / 2;
        handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax,     xmin+sc1,  -ymax );
        handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax,     xmin,      -ymax+sc1 );
        handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin+sc2, -ymax,     xmin+sc2,  -ymax+0.5f ); // 10 m ticks
        handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax+sc2, xmin+0.5f, -ymax+sc2 );
        handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin+sc1, -ymax,     xmin+sc1,  -ymax+0.5f ); // 20 m ticks
        handle = DXF.printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax+sc1, xmin+0.5f, -ymax+sc1 );
        // out.write( sw9.getBuffer().toString() );
	
        // DXF.printString( pw9, 0, "LINE" );
        // handle = DXF.inc(handle);
        // DXF.printAcDb( pw9, handle, DXF.AcDbEntity, DXF.AcDbLine );
        // DXF.printString( pw9, 8, "REF" );
        // // DXF.printInt(  pw9, 39, 0 );         // line thickness
        // DXF.printXYZ( pw9, xmin, -ymax, 0.0f, 0 );
        // DXF.printXYZ( pw9, (xmin+sc1), -ymax, 0.0f, 1 );
        // out.write( sw9.getBuffer().toString() );

        // StringWriter sw8 = new StringWriter();
        // PrintWriter pw8  = new PrintWriter(sw8);
        // DXF.printString( pw8, 0, "LINE" );
        // handle = DXF.inc(handle);
        // DXF.printAcDb( pw8, handle, DXF.AcDbEntity, DXF.AcDbLine );
        // DXF.printString( pw8, 8, "REF" );
        // // DXF.printInt(  pw8, 39, 0 );         // line thickness
        // DXF.printXYZ( pw8, xmin, -ymax, 0.0f, 0 );
        // DXF.printXYZ( pw8,  xmin, -ymax+sc1, 0.0f, 1 );
        // out.write( sw8.getBuffer().toString() );
        // out.flush();
        
	// offset axes legends by 1
        // StringWriter sw7 = new StringWriter();
        // PrintWriter pw7  = new PrintWriter(sw7);
        handle = DXF.printText( pw9, handle, scale_len, xmin+sc1, -ymax+1, 0, AXIS_SCALE, "REF", DXF.style_dejavu, xoff, yoff );
        handle = DXF.printText( pw9, handle, scale_len, xmin+1, -ymax+sc1, 0, AXIS_SCALE, "REF", DXF.style_dejavu, xoff, yoff );
        out.write( sw9.getBuffer().toString() );
       
        out.flush();

        // centerline data
        if ( PlotType.isSketch2D( type ) ) {
          for ( DrawingPath sh : plot.getLegs() ) {
            DBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            
            StringWriter sw4 = new StringWriter();
            PrintWriter pw4  = new PrintWriter(sw4);
            DXF.printString( pw4, 0, "LINE" );
            handle = DXF.inc(handle);
                        DXF.printAcDb( pw4, handle, DXF.AcDbEntity, DXF.AcDbLine );
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
            DXF.printXYZ( pw4, scale*(xoff + sh.x1), -scale*(yoff + sh.y1), 0.0f, 0 );
            DXF.printXYZ( pw4, scale*(xoff + sh.x2), -scale*(yoff + sh.y2), 0.0f, 1 );
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
            //   handle = DXF.inc(handle); DXF.printAcDb( pw41, handle, DXF.AcDbEntity, DXF.AcDbLine );
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
            handle = DXF.inc(handle);
            DXF.printAcDb( pw41, handle, DXF.AcDbEntity, DXF.AcDbLine );
            DXF.printString( pw41, 8, "SPLAY" );
            // DXF.printInt( pw41, 39, 1 );         // line thickness
            DXF.printXYZ( pw41, scale*(xoff + sh.x1), -scale*(yoff + sh.y1), 0.0f, 0 );
            DXF.printXYZ( pw41, scale*(xoff + sh.x2), -scale*(yoff + sh.y2), 0.0f, 1 );

            out.write( sw41.getBuffer().toString() );
            out.flush();
          }
        }

        // FIXME station scale is 0.3
        for ( ICanvasCommand cmd : plot.getCommands() ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath path = (DrawingPath)cmd;

          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);

          if ( path.mType == DrawingPath.DRAWING_PATH_STATION )
          {
            DrawingStationPath sp = (DrawingStationPath)path;
            handle = DXF.printText( pw5, handle, sp.name(), (sp.cx+xoff) * scale, -(sp.cy+yoff) * scale,
                                0, LABEL_SCALE, "STATION", DXF.style_dejavu, xoff, yoff );
          } 
          else if ( path.mType == DrawingPath.DRAWING_PATH_LINE )
          {
            handle = toDxf( pw5, handle, (DrawingLinePath)path, scale, xoff, yoff );
          } 
          else if ( path.mType == DrawingPath.DRAWING_PATH_AREA )
          {
            handle = toDxf( pw5, handle, (DrawingAreaPath)path, scale, xoff, yoff );
          }
          else if ( path.mType == DrawingPath.DRAWING_PATH_POINT )
          {
            DrawingPointPath point = (DrawingPointPath) path;
	    String name = point.getThName();
	    String th_name = point.getThName().replace(':','-');
            int idx = 1 + point.mPointType;
	    if ( name.equals("label") ) {
              DrawingLabelPath label = (DrawingLabelPath)point;
              DXF.printString(pw5, 0, "TEXT");
              DXF.printString(pw5, 8, "P_" + th_name);
              DXF.printFloat(pw5, 40, point.getScaleValue() * 1.4f);
              DXF.printString(pw5, 1, label.mPointText);
              DXF.printFloat(pw5, 50, 360.0f - (float)(point.mOrientation));
              DXF.printXYZ(pw5, (point.cx + xoff) * scale, -(point.cy + yoff) * scale, 0, 0);
            } else {
              if ( DXF.mVersion13 ) {
                if ( BrushManager.isPointSection( point.mPointType ) ) {
	          if ( TDSetting.mAutoXSections ) {
                    // FIXME GET_OPTION
                    // String scrapfile = point.mOptions.substring( 7 ) + ".tdr";
                    String scrapname = TDUtil.replacePrefix( TDInstance.survey, point.getOption( TDString.OPTION_SCRAP ) );
                    if ( scrapname != null ) {
                      String scrapfile = scrapname + ".tdr";
                      handle = tdrToDxf( pw5, handle, scrapfile, 
                             scale, point.cx, point.cy, -DrawingUtil.CENTER_X, -DrawingUtil.CENTER_Y );
                    }
                  }
                } else {
                  handle = toDxf( pw5, handle, point, scale, xoff, yoff );
                }
              } else {
                // String th_name = point.getThName().replace(':','-');
                DXF.printString( pw5, 0, "INSERT" );
                DXF.printString( pw5, 8, "P_" + th_name );
                DXF.printString( pw5, 2, "B_" + th_name );
                DXF.printFloat( pw5, 41, point.getScaleValue()*1.4f ); // FIX Asenov
                DXF.printFloat( pw5, 42, point.getScaleValue()*1.4f );
                DXF.printFloat( pw5, 50, 360.0f-(float)(point.mOrientation) );
                DXF.printXYZ( pw5, (point.cx+xoff)*scale, -(point.cy+yoff)*scale, 0, 0 );
              }
            }
	  }
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }
        StringWriter sw6 = new StringWriter();
        PrintWriter pw6  = new PrintWriter(sw6);
        if ( TDSetting.mAutoStations ) {
          for ( DrawingStationName st : plot.getStations() ) { // auto-stations
            handle = toDxf( pw6, handle, st, scale, xoff+1.0f, yoff-1.0f );
	    float len = 2.0f + st.getName().length() * 5.0f; // FIXME fonts ?
            handle = DXF.printLine( pw6,scale,handle,"STATION", xoff+st.cx, -(yoff+st.cy), xoff+st.cx+len, -(yoff+st.cy) );
          }
          out.write( sw6.getBuffer().toString() );
          out.flush();
        } else {
          for ( DrawingStationPath st_path : plot.getUserStations() ) { // user-chosen
            handle = toDxf( pw6, handle, st_path, scale, xoff, yoff );
          }
          out.write( sw6.getBuffer().toString() );
          out.flush();
        }
      }
      DXF.writeEndSection( out );
      if ( DXF.mVersion13 ) {
        handle = DXF.writeSectionObjects( out, handle );
      }
      DXF.writeString( out, 0, "EOF" );
      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TDLog.Error( "DXF io-exception " + e.toString() );
    }
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingStationName sn, float scale, float xoff, float yoff )
  { // FIXME point scale factor is 0.3
    if ( sn == null ) return handle;
    return DXF.printText( pw, handle, sn.getName(),  (sn.cx+xoff)*scale, -(sn.cy+yoff)*scale, 0,
                        STATION_SCALE, "STATION", DXF.style_dejavu, xoff, yoff );
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingStationPath sp, float scale, float xoff, float yoff )
  { // FIXME point scale factor is 0.3
    if ( sp == null ) return handle;
    return DXF.printText( pw, handle, sp.name(),  (sp.cx+xoff)*scale, -(sp.cy+yoff)*scale, 0,
                        STATION_SCALE, "STATION", DXF.style_dejavu, xoff, yoff );
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingPointPath point, float scale, float xoff, float yoff )
  { // FIXME point scale factor is 0.3
    if ( point == null ) return handle;
    if ( BrushManager.isPointLabel( point.mPointType ) ) {
      DrawingLabelPath label = (DrawingLabelPath)point;
      // Log.v("DistoX", "LABEL PATH label <" + label.mPointText + ">" );
      return DXF.printText( pw, handle, label.mPointText,
         (point.cx+xoff)*scale, -(point.cy+yoff)*scale, 360.0f-(float)label.mOrientation,
         LABEL_SCALE, "POINT", DXF.style_dejavu, xoff, yoff );
    }

    String th_name = point.getThName().replace(':','-');
    // int idx = 1 + point.mPointType;
    DXF.printString( pw, 0, "INSERT" );
    handle = DXF.inc(handle); DXF.printAcDb( pw, handle, "AcDbBlockReference" );
    DXF.printString( pw, 8, "P_" + th_name );
    DXF.printString( pw, 2, "B_" + th_name );
    DXF.printFloat( pw, 41, point.getScaleValue()*1.4f ); // FIX Asenov
    DXF.printFloat( pw, 42, point.getScaleValue()*1.4f );
    DXF.printFloat( pw, 50, 360.0f-(float)(point.mOrientation) );
    DXF.printXYZ( pw, (point.cx+xoff)*scale, -(point.cy+yoff)*scale, 0, 0 );
    return handle;
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingLinePath line, float scale, float xoff, float yoff )
  {
    if ( line == null ) return handle;
    String layer = "L_" + line.getThName( ).replace(':','-');
    int flag = 0;
    if ( DXF.mVersion13 && checkSpline( line ) ) {
      if ( TDSetting.mAcadSpline ) {
        handle = DXF.printPolylineHeader( pw, handle, layer, line.isClosed() );
        handle = printInterpolatedPolyline( pw, line, scale, handle, handle, layer, line.isClosed(), xoff, yoff );
        handle = DXF.printPolylineFooter( pw, handle );
      } else {
        handle = printSpline( pw, line, scale, handle, layer, line.isClosed(), xoff, yoff );
      }
    } else {
      // handle = printLWPolyline( pw5, line, scale, handle, layer, false );
      handle = printPolyline( pw, line, scale, handle, layer, line.isClosed(), xoff, yoff );
    }
    return handle;
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingAreaPath area, float scale, float xoff, float yoff )
  {
    if ( area == null ) return handle;
    float bezier_step = TDSetting.getBezierStep();
    // Log.v("DistoX", "area size " + area.size() );
    String layer = "A_" + area.getThName( ).replace(':','-');
    if ( DXF.mVersion13 && checkSpline( area ) ) {
      if ( TDSetting.mAcadSpline ) {
        handle = DXF.printPolylineHeader( pw, handle, layer, true );
        handle = printInterpolatedPolyline( pw, area, scale, handle, handle, layer, true, xoff, yoff );
        handle = DXF.printPolylineFooter( pw, handle );
      } else {
        handle = printSpline( pw, area, scale, handle, layer, true, xoff, yoff );
      }
    } else {
      // handle = printLWPolyline( pw5, line, scale, handle, layer, true );
      handle = printPolyline( pw, area, scale, handle, layer, true, xoff, yoff );
    }
    if ( DXF.mVersion13 ) {
      int npt = countInterpolatedPolylinePoints( area, true );
      handle = DXF.printHatchHeader( pw, handle, layer, npt );
      printInterpolatedPolyline( pw, area, scale, 0, handle, null, true, xoff, yoff );
      handle = DXF.printHatchFooter( pw, handle );
    }
    return handle;
  }

  static private int tdrToDxf( PrintWriter pw, int handle, String scrapfile,
                               float scale, float dx, float dy, float xoff, float yoff )
  {
    try {
      // TDLog.Log( TDLog.LOG_IO, "tdr to dxf. scrapfile " + scrapfile );
      FileInputStream fis = new FileInputStream( TDPath.getTdrFile( scrapfile ) );
      BufferedInputStream bfis = new BufferedInputStream( fis );
      DataInputStream dis = new DataInputStream( bfis );
      int version = DrawingIO.skipTdrHeader( dis );
      // Log.v("DistoX", "tdr to svg delta " + dx + " " + dy + " Offset " + xoff + " " + yoff );

      DrawingPath path; // = null;
      boolean done = false;
      while ( ! done ) {
        int what = dis.read();
        switch ( what ) {
          case 'P':
            path = DrawingPointPath.loadDataStream( version, dis, dx, dy /*, null */ );
            handle = toDxf( pw, handle, (DrawingPointPath)path, scale, xoff, yoff );
            break;
          case 'T':
            path = DrawingLabelPath.loadDataStream( version, dis, dx, dy );
            handle = toDxf( pw, handle, (DrawingLabelPath)path, scale, xoff, yoff );
            break;
          case 'L':
            path = DrawingLinePath.loadDataStream( version, dis, dx, dy /*, null */ );
            handle = toDxf( pw, handle, (DrawingLinePath)path, scale, xoff, yoff );
            break;
          case 'A':
            path = DrawingAreaPath.loadDataStream( version, dis, dx, dy /*, null */ );
            handle = toDxf( pw, handle, (DrawingAreaPath)path, scale, xoff, yoff );
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
          case 'J':
            /* path = */ DrawingSpecialPath.loadDataStream( version, dis, dx, dy );
            break;
          // case 'G':
          //   path = DrawingFixedName.loadDataStream( version, dis ); // consume DrawingFixedName data
          //   break;
          case 'F':
            done = true;
            break;
	  default:
	    TDLog.Error("TDR2DXF Error. unexpected code=" + what );
	    return handle;
        }
      }
    } catch ( FileNotFoundException e ) { // this is OK
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return handle;
  }

}

