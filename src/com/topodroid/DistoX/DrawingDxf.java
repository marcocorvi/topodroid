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
  private static boolean mVersion13 = false;
  private static boolean mVersion16 = false;
  private static boolean doHandle   = true;

  static int inc( int h ) { ++h; if ( h == 0x0105 ) ++h; return h; }

  static final private float POINT_SCALE   = 10.0f; // scale of point icons: only ACAD_6
  // the next three are for text
  static final private float STATION_SCALE =  6.0f / DrawingUtil.SCALE_FIX; // scale of station names
  static final private float LABEL_SCALE   =  8.0f / DrawingUtil.SCALE_FIX; // scale of label text
  static final private float AXIS_SCALE    = 10.0f / DrawingUtil.SCALE_FIX; // scale of text on the axes
  static final private String two  = "2.0";
  static final private String half = "0.5";
  static final private String two_n_half = "2.5";
  // static final String ten = "10";
  static final private String style_dejavu  = "DejaVu";
  static final private String standard      = "Standard";
  static final private String lt_continuous = "Continuous";
  static final private String lt_byBlock    = "ByBlock";
  static final private String lt_byLayer    = "ByLayer";
  static final private String lt_center     = "Center";
  static final private String lt_ticks      = "Ticks";
  
  static final private String AcDbSymbolTR = "AcDbSymbolTableRecord";
  static final private String AcDbEntity   = "AcDbEntity";
  static final private String AcDbText     = "AcDbText";
  static final private String AcDbLine     = "AcDbLine";
  static final private String AcDbPolyline = "AcDbPolyline";
  static final private String AcDbDictionary = "AcDbDictionary";

  static final private String EOL = "\r\n";
  static final private String EOL100 = "  100\r\n";
  static final private String EOLSPACE = "\r\n  ";
  static final private String SPACE = "  ";
 

  static private void writeComment( BufferedWriter out, String comment ) throws IOException
  {
    out.write( "  999" + EOL + comment + EOL );
  }

  static void writeHex( BufferedWriter out, int code, int handle ) throws IOException // mVersion13
  {
    if ( mVersion13 && doHandle ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.printf("  %d%s%X%s", code, EOL, handle, EOL );
      out.write( sw.getBuffer().toString() );
    }
  }

  static private void printHex( PrintWriter pw, int code, int handle ) // mVersion13
  {
    if ( mVersion13 && doHandle ) {
      pw.printf("  %d%s%X%s", code, EOL, handle, EOL );
    }
  }

  static private void writeAcDb( BufferedWriter out, int hex, String acdb1 ) throws IOException // mVersion13
  {
    if ( mVersion13 ) {
      if ( hex >= 0 ) writeHex( out, 5, hex );
      out.write( EOL100 + acdb1 + EOL );
    }
  }

  static private void writeAcDb( BufferedWriter out, int hex, String acdb1, String acdb2 ) throws IOException // mVersion13
  {
    if ( mVersion13 ) {
      if ( hex >= 0 ) writeHex( out, 5, hex );
      out.write( EOL100 + acdb1 + EOL+ EOL100 + acdb2 + EOL );
    }
  }


  static private void printAcDb( PrintWriter pw, int hex, String acdb1 ) // mVersion13
  {
    if ( mVersion13 ) {
      if ( hex >= 0 ) printHex( pw, 5, hex );
      pw.printf( EOL100 + acdb1 + EOL );
    }
  }

  static void printAcDb( PrintWriter pw, int hex, String acdb1, String acdb2 ) // mVersion13
  {
    if ( mVersion13 ) {
      if ( hex >= 0 ) printHex( pw, 5, hex );
      pw.printf( EOL100 + acdb1 + EOL + EOL100 + acdb2 + EOL );
    }
  }

  static private void writeString(  BufferedWriter out, int code, String name ) throws IOException
  {
    out.write( "  " + code + EOL + name + EOL );
  }
  static private void writeStringEmpty(  BufferedWriter out, int code ) throws IOException
  {
    out.write( "  " + code + EOL + TDString.EMPTY + EOL );
  }
  static private void writeStringOne(  BufferedWriter out, int code ) throws IOException
  {
    out.write( "  " + code + EOL + "1.0" + EOL );
  }
  static private void writeStringZero(  BufferedWriter out, int code ) throws IOException
  {
    out.write( "  " + code + EOL + "0.0" + EOL );
  }

  static void printString(  PrintWriter pw, int code, String name )
  {
    pw.printf("  %d%s%s%s", code, EOL, name, EOL );
  }

  static void printFloat(  PrintWriter pw, int code, float val )
  {
    pw.printf(Locale.US, "  %d%s%.2f%s", code, EOL, val, EOL );
  }

  static private void writeInt(  BufferedWriter out, int code, int val ) throws IOException
  {
    out.write( SPACE + code + EOL + val + EOL );
  }

  // used by SymbolPoint
  static void printInt(  PrintWriter pw, int code, int val )
  {
    pw.printf( "  %d%s%d%s", code, EOL, val, EOL );
  }

  static private void writeXY( BufferedWriter out, int x, int y, int base ) throws IOException
  {
    int b10 = 10 + base;
    int b20 = 20 + base;
    out.write( SPACE + b10 + EOL + x + EOLSPACE + b20 + EOL + y + EOL );
  }

  static private void writeXYZ( BufferedWriter out, int x, int y, int z, int base ) throws IOException
  {
    int b10 = 10 + base;
    int b20 = 20 + base;
    int b30 = 30 + base;
    out.write( SPACE + b10 + EOL + x + EOLSPACE + b20 + EOL + y + EOLSPACE + b30 + EOL + z + EOL );
  }

  static private void printXY( PrintWriter pw, float x, float y, int base )
  {
    pw.printf(Locale.US, "  %d%s%.2f%s  %d%s%.2f%s", base+10, EOL, x, EOL, base+20, EOL, y, EOL );
  }

  static void printXYZ( PrintWriter pw, float x, float y, float z, int base )
  {
    pw.printf(Locale.US, "  %d%s%.2f%s  %d%s%.2f%s  %d%s%.2f%s",
       base+10, EOL, x, EOL, base+20, EOL, y, EOL, base+30, EOL, z, EOL );
  }

  // static void printIntXYZ( PrintWriter pw, int x, int y, int z, int base )
  // {
  //   pw.printf(Locale.US, "  %d%s%d%s  %d%s%d%s  %d%s%d%s",
  //      base+10, EOL, x, EOL, base+20, EOL, y, EOL, base+30, EOL, z, EOL );
  // }

  // -----------------------------------------

  static private void writeSection( BufferedWriter out, String name ) throws IOException
  {
    writeString(out, 0, "SECTION");
    writeString(out, 2, name );
  }

  static private void writeEndSection( BufferedWriter out ) throws IOException
  {
    writeString(out, 0, "ENDSEC" );
  }

  static private void writeBeginTable(  BufferedWriter out, String name, int handle, int num ) throws IOException
  {
    writeString(out, 0, "TABLE" );
    writeString(out, 2, name );
    writeAcDb( out, handle, "AcDbSymbolTable" );
    if ( num >= 0 ) writeInt(out, 70, num );
  }
  
  static private void writeEndTable(  BufferedWriter out ) throws IOException
  {
    writeString( out, 0, "ENDTAB");
  }

  static private void printLayer( PrintWriter pw2, int handle, String name, int flag, int color, String linetype )
  {
    name = name.replace(":", "-");
    printString( pw2, 0, "LAYER" );
    printAcDb( pw2, handle, AcDbSymbolTR, "AcDbLayerTableRecord");
    printString( pw2, 2, name );  // layer name
    printInt( pw2, 70, flag );    // layer flag
    printInt( pw2, 62, color );   // layer color
    printString( pw2, 6, linetype ); // linetype name
    // if ( mVersion13 ) {
    //   printInt( pw2, 330, 2 );       // softpointer id/handle to owner dictionary 
    //   printInt( pw2, 370, -3 );      // lineweight enum value
    //   printString( pw2, 390, "F" );  // hardpointer id/handle or plotstylename object
    //   // printInt( pw2, 347, 46 );
    //   // printInt( pw2, 348, 0 );
    // }
  }

  // static void printEndText( PrintWriter pw, String style )
  // {
  //   printString( pw, 7, style );
  //   printString( pw, 100, AcDbText );
  // }
  static private int printLinePoint( PrintWriter pw, float scale, int handle, String layer, float x, float y )
  {
    printString( pw, 0, "VERTEX" );
    if ( mVersion13 ) {
      handle = inc(handle);
      printAcDb( pw, handle, "AcDbVertex", "AcDb3dPolylineVertex" );
      printInt( pw, 70, 32 ); // flag 32 = 3D polyline vertex
    }
    printString( pw, 8, layer );
    printXYZ( pw, x * scale, -y * scale, 0.0f, 0 );
    return handle;
  }

  static private int printLine(PrintWriter pw, float scale, int handle, String layer, float x1, float y1, float x2, float y2)
  {
    printString( pw, 0, "LINE" );
    handle = inc(handle);
    printAcDb( pw, handle, AcDbEntity, AcDbLine );
    printString( pw, 8, layer );
    // printInt(  pw, 39, 0 );         // line thickness
    printXYZ( pw, x1*scale, y1*scale, 0.0f, 0 );
    printXYZ( pw, x2*scale, y2*scale, 0.0f, 1 );
    return handle;
  }

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

      /*
      p = area.mFirst;
      x0 = p.x;
      y0 = p.y;
      printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
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
          if ( np > 1 ) {
            BezierCurve bc = new BezierCurve( x0, y0, x1, y1, x2, y2, x3, y3 );
            for ( int n=1; n < np; ++n ) {
              Point2D pb = bc.evaluate( (float)n / (float)np );
              printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
            }
          }
        }
        printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
        x0 = x3;
        y0 = y3;
      }
      p = area.mFirst;
      x0 = p.x;
      y0 = p.y;
      printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
      */
  static private int printInterpolatedPolyline(  PrintWriter pw, DrawingPointLinePath line, float scale, int handle,
                                    String layer, boolean closed, float xoff, float yoff )
  {
    float bezier_step = TDSetting.getBezierStep();
    LinePoint p = line.mFirst;
    float x0 = xoff + p.x;
    float y0 = yoff + p.y;
    if ( layer != null ) {
      handle = printLinePoint( pw, scale, handle, layer, x0, y0 );
    } else {
      printXY( pw, x0*scale, -y0*scale, 0 );
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
              handle = printLinePoint( pw, scale, handle, layer, pb.x, pb.y );
            }
          } else {
	    for ( int n=1; n < np; ++n ) {
	      Point2D pb = bc.evaluate( (float)n / (float)np );
              printXY( pw, (pb.x+xoff)*scale, -(pb.y+yoff)*scale, 0 );
            }
          }
        }
      } 
      if ( layer != null ) {
        handle = printLinePoint( pw, scale, handle, layer, x3, y3 );
      } else {
        printXY( pw, x3*scale, -y3*scale, 0 );
      }
      x0 = x3;
      y0 = y3;
    }
    if ( closed ) {
      p = line.mFirst;
      if ( layer != null ) {
        handle = printLinePoint( pw, scale, handle, layer, xoff+p.x, yoff+p.y );
      } else {
        printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
      }
    }
    return handle;
  }

  static private int printPolylineHeader( PrintWriter pw, int handle, String layer, boolean closed )
  {
    printString( pw, 0, "POLYLINE" );
    handle = inc(handle);
    printAcDb( pw, handle, AcDbEntity, AcDbPolyline );
    printString( pw, 8, layer );
    // printInt(  pw, 39, 1 ); // line thickness
    // printInt(  pw, 40, 1 ); // start width
    // printInt(  pw, 41, 1 ); // end width
    printInt( pw, 66, 1 ); // group 1
    printInt( pw, 70, 8 + (closed? 1:0) ); // polyline flag 8 = 3D polyline, 1 = closed  // inlined close in 5.1.20
    // printInt( pw, 75, 0 ); // 6 cubic spline, 5 quad spline, 0 (optional, default 0) // commented in 5.1.20
    return handle;
  }

  static private int printPolylineFooter( PrintWriter pw, int handle )
  {
    pw.printf("  0%sSEQEND%s", EOL, EOL );
    if ( mVersion13 ) {
      handle = inc(handle);
      printHex( pw, 5, handle );
    }
    return handle;
  }

  static private int printPolyline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle,
                                    String layer, boolean closed, float xoff, float yoff )
  {
    handle = printPolylineHeader( pw, handle, layer, closed );
    /* commented 5.1.22
    printString( pw, 0, "POLYLINE" );
    handle = inc(handle);
    printAcDb( pw, handle, AcDbEntity, AcDbPolyline );
    printString( pw, 8, layer );
    // printInt(  pw, 39, 1 ); // line thickness
    // printInt(  pw, 40, 1 ); // start width
    // printInt(  pw, 41, 1 ); // end width
    printInt( pw, 66, 1 ); // group 1
    printInt( pw, 70, 8 + (closed? 1:0) ); // polyline flag 8 = 3D polyline, 1 = closed  // inlined close in 5.1.20
    // printInt( pw, 75, 0 ); // 6 cubic spline, 5 quad spline, 0 (optional, default 0) // commented in 5.1.20
    */

    handle = printInterpolatedPolyline( pw, line, scale, handle, layer, closed, xoff, yoff );
    /* commented in 5.1.20
    // float bezier_step = TDSetting.getBezierStep();
    LinePoint p = line.mFirst;
    float x0 = xoff + p.x;
    float y0 = yoff + p.y;
    handle = printLinePoint( pw, scale, handle, layer, x0, y0 );
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
	  for ( int n=1; n < np; ++n ) {
	    Point2D pb = bc.evaluate( (float)n / (float)np );
            handle = printLinePoint( pw, scale, handle, layer, pb.x, pb.y );
          }
	}
      } 
      handle = printLinePoint( pw, scale, handle, layer, x3, y3 );
      x0 = x3;
      y0 = y3;
    }
    if ( closed ) {
      p = line.mFirst;
      handle = printLinePoint( pw, scale, handle, layer, xoff+p.x, yoff+p.y );
    }
    */

    handle = printPolylineFooter( pw, handle );
    /* commented 5.1.22
    pw.printf("  0%sSEQEND%s", EOL, EOL );
    if ( mVersion13 ) {
      handle = inc(handle);
      printHex( pw, 5, handle );
    }
    */
    return handle;
  }

  // static private int printLWPolyline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, String layer, boolean closed,
  //                             float xoff, float yoff )
  // {
  //   int close = (closed ? 1 : 0 );
  //   printString( pw, 0, "LWPOLYLINE" );
  //   handle = inc(handle);
  //       printAcDb( pw, handle, AcDbEntity, AcDbPolyline );
  //   printString( pw, 8, layer );
  //   printInt( pw, 38, 0 ); // elevation
  //   printInt( pw, 39, 1 ); // thickness
  //   printInt( pw, 43, 1 ); // start width
  //   printInt( pw, 70, close ); // not closed
  //   printInt( pw, 90, line.size() ); // nr. of points
  //   for (LinePoint p = line.mFirst; p != null; p = p.mNext ) { 
  //     printXY( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0 );
  //   }
  //   return handle;
  // }

  static private boolean checkSpline( DrawingPointLinePath line )
  {
    if ( mVersion13 ) {
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
    printString( pw, 0, "SPLINE" );
    handle = inc(handle); printAcDb( pw, handle, AcDbEntity, "AcDbSpline" );
    printString( pw, 8, layer );
    printString( pw, 6, lt_continuous );
    printFloat( pw, 48, 1.0f ); // scale 
    printInt( pw, 60, 0 ); // visibilty (0: visible, 1: invisible)
    printInt( pw, 66, 1 ); // group 1: "entities follow" flag
    // printInt( pw, 67, 0 ); // in model space [default]
    printXYZ( pw, 0, 0, 1, 200 ); // normal vector

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
      printXYZ( pw, xt/d, -yt/d, 0, 2 );

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
      printXYZ( pw, xt/d, -yt/d, 0, 3 );
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
    printInt( pw, 70, 8+(closed?1:0) ); // flags  1: closed, 2: periodic, 4: rational, 8: planar, 16 linear
    printInt( pw, 71, 3 );    // degree of the spline
    printInt( pw, 72, nk );   // nr. of knots
    printInt( pw, 73, ncp );  // nr. of control pts
    printInt( pw, 74, np );   // nr. of fit points
    // printXYZ( pw, x, y, z, 2 ); // start tangent
    // printXYZ( pw, x, y, z, 3 ); // end tangent
    
    printInt( pw, 40, 0 );                              // knots: 1 + 3 * NP + 1
    for ( int k=0; k<np; ++k ) {                         // 0 0 0 0 1 1 1 2 2 2 ... N N N N
      for ( int j=0; j<3; ++j ) printInt( pw, 40, k );
    }
    printInt( pw, 40, np-1 );

    p = line.mFirst; 
    xt = p.x;
    yt = p.y;
    printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );         // control points: 1 + 3 * (NP - 1) = 3 NP - 2
    for ( p = p.mNext; p != null; p = p.mNext ) { 
      if ( p.has_cp ) {
        printXYZ( pw, (p.x1+xoff) * scale, -(p.y1+yoff) * scale, 0.0f, 0 );
        printXYZ( pw, (p.x2+xoff) * scale, -(p.y2+yoff) * scale, 0.0f, 0 );
      } else {
        printXYZ( pw, (xt+xoff) * scale, -(yt+yoff) * scale, 0.0f, 0 );
        printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );
      }
      printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );
      xt = p.x;
      yt = p.y;
    }
/*
    if ( closed ) {
      p = line.mFirst;
      printXYZ( pw, (xt+xoff) * scale, -(yt+yoff) * scale, 0.0f, 0 );
      printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 0 );
    }
*/

    for ( p = line.mFirst; p != null; p = p.mNext ) { 
      printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 1 );  // fit points: NP
    }
/*
    if ( closed ) {
      p = line.mFirst;
      printXYZ( pw, (p.x+xoff) * scale, -(p.y+yoff) * scale, 0.0f, 1 );  // fit points: NP
    }
*/
    return handle;
  }

  static private int printText( PrintWriter pw, int handle, String label, float x, float y, float angle, float scale,
                        String layer, String style, float xoff, float yoff )
  {
    // if ( false && mVersion13 ) { // FIXME TEXT in AC1012
    //   // int idx = 1 + point.mPointType;
    //   printString( pw, 0, "INSERT" );
    //   handle = inc(handle); printAcDb( pw, handle, "AcDbBlockReference" );
    //   printString( pw, 8, "POINT" );
    //   printString( pw, 2, "P_label" ); // block_name );
    //   printFloat( pw, 41, POINT_SCALE );
    //   printFloat( pw, 42, POINT_SCALE );
    //   printFloat( pw, 50, 360-angle );
    //   printXYZ( pw, x, y, 0, 0 );
    // } else {
      printString( pw, 0, "TEXT" );
      // printString( pw, 2, block );
      handle = inc(handle); printAcDb( pw, handle, AcDbEntity, AcDbText );
      printString( pw, 8, layer );
      // printString( pw, 7, style_dejavu ); // style (optional)
      // pw.printf("%s\%s 0%s", "\"10\"", EOL, EOL );
      printXYZ( pw, x, y, 0, 0 );
      // printXYZ( pw, 0, 0, 1, 1 );   // second alignmenmt (otional)
      // printXYZ( pw, 0, 0, 1, 200 ); // extrusion (otional 0 0 1)
      // printFloat( pw, 39, 0 );      // thickness (optional 0) 
      printFloat( pw, 40, scale );     // height
      // printFloat( pw, 41, 1 );      // scale X (optional 1)
      printFloat( pw, 50, angle );     // rotation [deg]
      printFloat( pw, 51, 0 );         // oblique angle
      // printInt( pw, 71, 0 );        // text generation flag (optional 0)
      // printFloat( pw, 72, 0 );      // H-align (optional 0)
      // printFloat( pw, 73, 0 );      // V-align
      printString( pw, 1, label );    
      // printString( pw, 7, style );  // style, optional (dftl STANDARD)
      printString( pw, 100, "AcDbText");
    // }
    return handle;
  }

  static private int writeSpaceBlockRecord( BufferedWriter out, String name, int handle ) throws IOException
  {
     writeString( out, 0, "BLOCK_RECORD" );
     handle = inc(handle);
     writeAcDb( out, handle, AcDbSymbolTR, "AcDbBlockTableRecord" );
     writeString( out, 2, name );
     writeInt( out, 70, 0 );
     writeInt( out, 280, 1 );
     writeInt( out, 281, 0 );
     writeInt( out, 330, 1 );
     return handle;
  }

  static private int writeSpaceBlock( BufferedWriter out, String name, int handle ) throws IOException
  {
    writeString( out, 0, "BLOCK" );
    handle = inc(handle);
    writeAcDb( out, handle, AcDbEntity, "AcDbBlockBegin" );
    // writeInt( out, 330, handle );
    writeString( out, 8, "0" );
    writeString( out, 2, name );
    writeInt( out, 70, 0 );       // flag 0=none, 1=anonymous, 2=non-conts attr, 4=xref, 8=xref overlay,
    writeInt( out, 10, 0 ); 
    writeInt( out, 20, 0 ); 
    writeInt( out, 30, 0 ); 
    writeString( out, 3, name );
    writeString( out, 1, "" );
    writeString( out, 0, "ENDBLK" );
    if ( mVersion13 ) {
      handle = inc(handle);
      writeAcDb( out, handle, AcDbEntity, "AcDbBlockEnd");
      writeString( out, 8, "0");
    }
    return handle;
  }

  static void writeDxf( BufferedWriter out, TDNum num, /* DrawingUtil util, */ DrawingCommandManager plot, long type )
  {
    mVersion13 = (TDSetting.mAcadVersion >= 13);
    mVersion16 = (TDSetting.mAcadVersion >= 16);
    
    float scale = 1.0f/DrawingUtil.SCALE_FIX; // TDSetting.mDxfScale; 
    float xoff = 0;
    float yoff = 0;
    int handle = 0;
    RectF bbox = plot.getBoundingBox( );
    float xmin = bbox.left;
    float xmax = bbox.right;
    float ymin = bbox.top;
    float ymax = bbox.bottom;
    xmin *= scale;
    xmax *= scale;
    ymin *= scale;
    ymax *= scale;

    int p_style = 0;

    // Log.v("DistoX", "DXF X " + xmin + " " + xmax + " Y " + ymin + " " + ymax );

    try {
      // header
      writeComment( out, "DXF created by TopoDroid v. " + TDVersion.string() 
        + " " + TDSetting.mAcadVersion + " " + (TDSetting.mAutoStations? "T ":"F ") + (TDSetting.mAutoXSections? "T ":"F ") + TDSetting.getBezierStep() );
      writeSection( out, "HEADER" );

      xmin -= 2;  xmax += 2;
      ymin -= 2;  ymax += 2;

      // ACAD versions: 1006 (R10) 1009 (R11 R12) 1012 (R13) 1014 (R14)
      //                1015 (2000) 1018 (2004) 1021 (2007) 1024 (2010)  
      writeString( out, 9, "$ACADVER" );
      writeString( out, 1, ( mVersion13? "AC1012" : "AC1009" ) );
      // writeString( out, 9, "$ACADMAINTVER" ); writeInt( out, 70, 105 ); // ignored
      if ( mVersion13 ) {
        writeString( out, 9, "$HANDSEED" );    writeHex( out, 5, 0xffff );
        writeString( out, 9, "$DWGCODEPAGE" ); writeString( out, 3, "ANSI_1251" );
      }
      // writeString( out, 9, "$REQUIREDVERSIONS" ); writeInt( out, 160, 0 );

      writeString( out, 9, "$INSBASE" );
      {
        StringWriter sw1 = new StringWriter();
        PrintWriter pw1  = new PrintWriter(sw1);
        printXYZ( pw1, 0.0f, 0.0f, 0.0f, 0 ); // FIXME (0,0,0)
        printString( pw1, 9, "$EXTMIN" ); printXYZ( pw1, xmin, -ymax, 0.0f, 0 );
        printString( pw1, 9, "$EXTMAX" ); printXYZ( pw1, xmax, -ymin, 0.0f, 0 );
        if ( mVersion13 ) {
          printString( pw1, 9, "$LIMMIN" ); printXY( pw1, 0.0f, 0.0f, 0 );
          printString( pw1, 9, "$LIMMAX" ); printXY( pw1, 420.0f, 297.0f, 0 );
        }
        out.write( sw1.getBuffer().toString() );
      }
      if ( mVersion13 ) {
        writeString( out, 9, "$DIMSCALE" );    writeString( out, 40, "1.0" ); // 
        writeString( out, 9, "$DIMTXT" );      writeString( out, 40, "2.5" ); // 
        writeString( out, 9, "$LTSCALE" );     writeInt( out, 40, 1 ); // 
        writeString( out, 9, "$LIMCHECK" );    writeInt( out, 70, 0 ); // 
        writeString( out, 9, "$ORTHOMODE" );   writeInt( out, 70, 0 ); // 
        writeString( out, 9, "$FILLMODE" );    writeInt( out, 70, 1 ); // 
        writeString( out, 9, "$QTEXTMODE" );   writeInt( out, 70, 0 ); // 
        writeString( out, 9, "$REGENMODE" );   writeInt( out, 70, 1 ); // 
        //writeString( out, 9, "$MIRRMODE" );    writeInt( out, 70, 0 ); // not handled by DraftSight, not handled by AutoCAD
        writeString( out, 9, "$UNITMODE" );    writeInt( out, 70, 0 ); // 

        writeString( out, 9, "$TEXTSIZE" );    writeInt( out, 40, 5 ); // default text size
        writeString( out, 9, "$TEXTSTYLE" );   writeString( out, 7, standard );
        writeString( out, 9, "$CELTYPE" );     writeString( out, 6, "BYLAYER" ); // 
        writeString( out, 9, "$CELTSCALE" );   writeInt( out, 40, 1 ); // 
        writeString( out, 9, "$CECOLOR" );     writeInt( out, 62, 256 ); // 

        writeString( out, 9, "$MEASUREMENT" ); writeInt( out, 70, 1 ); // drawing units 1=metric
        writeString( out, 9, "$INSUNITS" );    writeInt( out, 70, 4 ); // defaulty drawing units 0=unitless 4=mm
        writeString( out, 9, "$DIMASSOC" );    writeInt( out, 280, 0 ); // 0=no association
      }
      writeEndSection( out );
      if ( mVersion13 ) {
        writeSection( out, "CLASSES" );
        writeEndSection( out );
      }
      writeSection( out, "TABLES" );
      {
        if ( mVersion13 ) {
          handle = inc(handle); writeBeginTable( out, "VPORT", handle, 1 ); // 1 VPORT
          {
            writeString( out, 0, "VPORT" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbViewportTableRecord" );
            writeString( out, 2, "*Active" ); // name
            writeInt( out, 70, 0 );  // flags:
            writeXY( out, (int)xmin, -(int)ymax, 0 ); // lower-left cormer
            writeXY( out, (int)xmax, -(int)ymin, 1 ); // upper-right corner
            writeXY( out, (int)(xmin+xmax)/2, -(int)(ymin+ymax)/2, 2 ); // center point
            writeXY( out, 286, 148, 2 );
            writeXY( out, 0, 0, 3 );   // snap base-point
            writeXY( out, 1, 1, 4 );   // snap-spacing
            writeXY( out, 1, 1, 5 );   // grid-spacing
            writeXYZ( out, 0, 0, 1, 6 ); // view direction
            writeXYZ( out, 0, 0, 0, 7 ); // view tangent
            
            writeInt( out, 40, 297 ); // Float.toString( (xmin+xmax)/2 ) );
            writeInt( out, 41, 2 );   // Float.toString( (ymin+ymax)/2 ) );
            writeInt( out, 42, 50 );  // lens length
            writeInt( out, 43, 0 );   // front clipping plane
            writeInt( out, 44, 0 );   // back clipping plane
            writeInt( out, 45, 0 );   // view height
            writeInt( out, 50, 0 );   // snap rotation angle
            writeInt( out, 51, 0 );   // view twist angle
            writeInt( out, 71, 0 );   // view mode:
            writeInt( out, 72, 100 ); // circle sides
            writeInt( out, 73, 1 );   
            writeInt( out, 74, 3 );   // UCSICON setting
            writeInt( out, 75, 0 );
            writeInt( out, 76, 0 );
            writeInt( out, 77, 0 );
            writeInt( out, 78, 0 );

            writeInt( out, 281, 0 );  // render mode: 0=2D optimized
            writeInt( out, 65, 1 );
            writeXYZ( out, 0, 0, 0, 100 );  // UCS origin
            writeXYZ( out, 1, 0, 0, 101 );  // UCS X-axis
            writeXYZ( out, 0, 1, 0, 102 );  // UCS Y-axis
            writeInt( out, 79, 0 );
            writeInt( out, 146, 0 );
            //writeString( out, 348, "2F" );
            //writeInt( out, 60, 3 );
            //writeInt( out, 61, 5 );
            //writeInt( out, 292, 1 );
            //writeInt( out, 282, 1 );
            //writeStringZero( out, 141 );
            //writeStringZero( out, 142 );
            //writeInt( out, 63, 250 );
            //writeString( out, 361, "6D" );
          }
          writeEndTable( out );
        }

        if ( mVersion13 ) {
	  int nr_styles = 2;
          handle = inc(handle); writeBeginTable( out, "STYLE", handle, nr_styles );  // 2 styles
          {
            writeString( out, 0, "STYLE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbTextStyleTableRecord" );
            writeString( out, 2, standard );  // name
            writeInt( out, 70, 0 );           // flag (1: shape, 4:vert text, ... )
            writeStringZero( out, 40 );     // text-height: not fixed
            writeStringOne(  out, 41 );
            writeStringZero( out, 50 );
            writeInt( out, 71, 0 );
            writeString( out, 42, two_n_half  );
            writeString( out, 3, "txt" );  // fonts
            writeStringEmpty( out, 4 );

            writeString( out, 0, "STYLE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbTextStyleTableRecord" );
	    p_style = handle;
            writeString( out, 2, style_dejavu );  // name
            writeInt( out, 70, 0 );               // flag
            writeStringZero( out, 40 );
            writeStringOne(  out, 41 );
            writeStringZero( out, 50 );
            writeInt( out, 71, 0 );
            writeString( out, 42, two_n_half  );
            writeString( out, 3, "Sans Serif.ttf" );  // fonts
            writeStringEmpty( out, 4 );
            writeString( out, 1001, "ACAD" );
            writeString( out, 1000, "DejaVu Sans" );
            writeInt( out, 1071, 0 );
          }
          writeEndTable( out );
        }

        if ( mVersion13 ) { handle = inc(handle); } else { handle = 5; }
	int ltypeowner = handle;
	int ltypenr    = mVersion13 ? 5 : 1; // linetype number
        writeBeginTable( out, "LTYPE", handle, ltypenr ); 
        // FIXME this line might be a problem with AutoCAD
	// writeInt( out, 330, 0 ); // table has no owner
        {
          // int flag = 64;
          if ( mVersion13 ) {
            writeString( out, 0, "LTYPE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
            writeString( out, 2, lt_byBlock );
	    writeInt( out, 330, ltypeowner );
            writeInt( out, 70, 0 );
            writeString( out, 3, "Std by block" );
            writeInt( out, 72, 65 );
            writeInt( out, 73, 0 );
            writeStringZero( out, 40 );

	    writeString( out, 0, "LTYPE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
            writeString( out, 2, lt_byLayer );
	    writeInt( out, 330, ltypeowner );
            writeInt( out, 70, 0 );
            writeString( out, 3, "Std by layer" );
            writeInt( out, 72, 65 );
            writeInt( out, 73, 0 );
            writeStringZero( out, 40 );

            writeString( out, 0, "LTYPE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
            writeString( out, 2, lt_continuous );
	    writeInt( out, 330, ltypeowner );
            writeInt( out, 70, 0 );
            writeString( out, 3, "Solid line ------" );
            writeInt( out, 72, 65 );
            writeInt( out, 73, 0 );
            writeStringZero( out, 40 );

            if ( ! mVersion16 ) {
	      writeString( out, 0, "LTYPE" );
              handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
              writeString( out, 2, lt_center );
	      writeInt( out, 330, ltypeowner );
              writeInt( out, 70, 0 );
              writeString( out, 3, "Center ____ _ ____ _ ____ _ ____" ); // description
              writeInt( out, 72, 65 );
              writeInt( out, 73, 4 );         // number of elements
              writeString( out, 40, two );  // pattern length
              writeString( out, 49, "1.25" );  writeInt( out, 74, 0 ); // segment
              writeString( out, 49, "-0.25" ); writeInt( out, 74, 0 ); // gap
              writeString( out, 49, "0.25" );  writeInt( out, 74, 0 );
              writeString( out, 49, "-0.25" ); writeInt( out, 74, 0 );

	      writeString( out, 0, "LTYPE" );
              handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
              writeString( out, 2, lt_ticks );
	      writeInt( out, 330, ltypeowner );
              writeInt( out, 70, 0 );
              writeString( out, 3, "Ticks ____|____|____|____" ); // description
              writeInt( out, 72, 65 );
              writeInt( out, 73, 3 );        // number of elements
              writeStringOne( out, 40 ); // pattern length
              writeString( out, 49, half );  writeInt( out, 74, 0 ); // segment
              writeString( out, 49, "-0.2" ); writeInt( out, 74, 2 ); // embedded text
	        writeInt( out, 75, 0 );   // SHAPE number must be 0
	        writeInt( out, 340, p_style );  // STYLE pointer FIXME
	        writeString( out, 46, "0.1" );  // scale
	        writeStringZero( out, 50 );   // rotation
	        writeString( out, 44, "-0.1" ); // X offset
	        writeString( out, 45, "-0.1" ); // Y offset
	        writeString( out, 9, "|" ); // text
              writeString( out, 49, "-0.25" ); writeInt( out, 74, 0 ); // gap
            }

	    // writeString( out, 0, "LTYPE" );
            // handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
            // writeString( out, 2, lt_tick );
	    // writeInt( out, 330, ltypeowner );
            // writeInt( out, 70, 0 );
            // writeString( out, 3, "Ticks ____|____|____|____" ); // description
            // writeInt( out, 72, 65 );
            // writeInt( out, 73, 4 );
            // writeString( out, 40, "1.45" ); // pattern length
            // writeString( out, 49, "0.25" ); writeInt( out, 74, 0 ); // segment
            // writeString( out, 49, "-0.1" ); writeInt( out, 74, 4 ); // embedded shape
	    //   writeInt( out, 75, 1 );   // SHAPE number
	    //   writeInt( out, 340, 1 );  // STYLE pointer
	    //   writeString( out, 46, "0.1" );  // scale
	    //   writeStringZero( out, 50 );   // rotation
	    //   writeString( out, 44, "-0.1" ); // X offset
	    //   writeStringZero( out, 45 );   // Y offset
            // writeString( out, 49, "-0.1" ); writeInt( out, 74, 0 );
            // writeString( out, 49, "1.0" );  writeInt( out, 74, 0 );

          } else {
            writeString( out, 0, "LTYPE" );
            writeAcDb( out, 14, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
            writeString( out, 2, lt_continuous );
            writeInt( out, 70, 64 );
            writeString( out, 3, "Solid line" );
            writeInt( out, 72, 65 );
            writeInt( out, 73, 0 );
            writeStringZero( out, 40 );
          }
        }
        writeEndTable( out );
        int nr_layers = 7;

        SymbolPointLibrary pointlib = BrushManager.getPointLib();
        SymbolLineLibrary linelib   = BrushManager.getLineLib();
        SymbolAreaLibrary arealib   = BrushManager.getAreaLib();
        // nr_layers += 1 + linelib.size() + arealib.size();
        nr_layers += 1 + BrushManager.getLineLibSize() + BrushManager.getAreaLibSize();
        if ( mVersion13 ) { handle = inc(handle); } else { handle = 2; }
        writeBeginTable( out, "LAYER", handle, nr_layers );
        {
          StringWriter sw2 = new StringWriter();
          PrintWriter pw2  = new PrintWriter(sw2);

          // 2 layer name, 70 flag (64), 62 color code, 6 line type
          int flag = 0;
          int color = 1;
          // if ( ! mVersion13 ) { handle = 40; }
          handle = inc(handle); printLayer( pw2, handle, "0",       flag, 7, lt_continuous ); // LAYER "0" .. FIXME DraftSight ..must be AutoCAD white
          handle = inc(handle); printLayer( pw2, handle, "LEG",     flag, color, lt_continuous ); ++color; // red
          handle = inc(handle); printLayer( pw2, handle, "SPLAY",   flag, color, lt_continuous ); ++color; // yellow
          handle = inc(handle); printLayer( pw2, handle, "STATION", flag, color, lt_continuous ); ++color; // green
          handle = inc(handle); printLayer( pw2, handle, "LINE",    flag, color, lt_continuous ); ++color; // cyan
          handle = inc(handle); printLayer( pw2, handle, "POINT",   flag, color, lt_continuous ); ++color; // blue
          handle = inc(handle); printLayer( pw2, handle, "AREA",    flag, color, lt_continuous ); ++color; // magenta
          handle = inc(handle); printLayer( pw2, handle, "REF",     flag, color, lt_continuous ); ++color; // white
          
          color = 10;
          if ( linelib != null ) { // always true
            for ( Symbol line : linelib.getSymbols() ) {
              String lname = "L_" + line.getThName().replace(':','-');
	      String ltype = lt_continuous;
              if ( mVersion13 && ! mVersion16 ) {
	        if ( lname.equals("L_pit") ) { 
                  ltype = lt_ticks;
                } else if ( lname.equals("L_border" ) ) {
                  ltype = lt_center;
                }
              }
              handle = inc(handle); printLayer( pw2, handle, lname, flag, color, ltype ); 
	      if ( ++color >= 256 ) color = 1;
            }
          }

          color = 60;
          if ( arealib != null ) { // always true
            for ( Symbol s : arealib.getSymbols() ) {
              String aname = "A_" + s.getThName().replace(':','-');
              handle = inc(handle); printLayer( pw2, handle, aname, flag, color, lt_continuous ); 
	      if ( ++color >= 256 ) color = 1;
            }
          }
          color = 80;
          if ( pointlib != null ) { // always true
            for ( Symbol point : pointlib.getSymbols() ) {
              String pname = "P_" + point.getThName().replace(':','-');
              handle = inc(handle); printLayer( pw2, handle, pname, flag, color, lt_continuous ); 
	      if ( ++color >= 256 ) color = 1;
            }
          }
          out.write( sw2.getBuffer().toString() );
        }
        writeEndTable( out );

        handle = inc(handle);
        writeBeginTable( out, "VIEW", handle, 0 ); // no VIEW
        writeEndTable( out );

        handle = inc(handle);
        writeBeginTable( out, "UCS", handle, 0 ); // no UCS
        writeEndTable( out );
        
        handle = inc(handle);
        writeBeginTable( out, "APPID", handle, 1 );
        {
          writeString( out, 0, "APPID" );
          if ( mVersion13 ) { handle = inc(handle); } else { handle = 12; }
          writeAcDb( out, handle, AcDbSymbolTR, "AcDbRegAppTableRecord" );
          writeString( out, 2, "ACAD" ); // applic. name
          writeInt( out, 70, 0 );        // flag
        }
        writeEndTable( out );

        if ( mVersion13 ) {
          handle = inc(handle); writeBeginTable( out, "DIMSTYLE", handle, 1 );
          // writeString( out, 100, "AcDbDimStyleTable" );
          // writeInt( out, 71, 0 ); // DIMTOL
          {
            writeString( out, 0, "DIMSTYLE" );
            handle = inc(handle);
	    writeHex( out, 105, handle ); 
            writeAcDb( out, -1, AcDbSymbolTR, "AcDbDimStyleTableRecord" );
            writeString( out, 2, standard );
            writeStringEmpty( out, 3 );
            writeStringEmpty( out, 4 );
            writeStringEmpty( out, 5 );
            writeStringEmpty( out, 6 );
            writeStringEmpty( out, 7 );
            writeStringOne( out, 40 );
            writeString( out, 41, two_n_half );
            writeString( out, 42, "0.625" );
            writeString( out, 43, "3.75" );
            writeString( out, 44, "1.25" );
            writeStringZero( out, 45 );
            writeStringZero( out, 46 );
            writeStringZero( out, 47 );
            writeStringZero( out, 48 );
            writeInt( out, 70, 0 );
            writeInt( out, 71, 0 );
            writeInt( out, 72, 0 );

            writeInt( out, 73, 0 );
            writeInt( out, 74, 0 );
            writeInt( out, 75, 0 );
            writeInt( out, 76, 0 );
            writeInt( out, 77, 1 );
            writeInt( out, 78, 8 );
            writeString( out, 140, two_n_half );
            writeString( out, 141, two_n_half );
            writeStringZero( out, 142 );
            writeString( out, 143, "0.04" );
            writeStringOne( out, 144 );
            writeStringZero( out, 145 );
            writeStringOne( out, 146 );
            writeString( out, 147, "0.625" );
            writeInt( out, 170, 0 );
            writeInt( out, 171, 3 );
            writeInt( out, 172, 1 );
            writeInt( out, 173, 0 );
            writeInt( out, 174, 0 );
            writeInt( out, 175, 0 );
            writeInt( out, 176, 0 );
            writeInt( out, 177, 0 );
            writeInt( out, 178, 0 );
            writeInt( out, 271, 2 );
            writeInt( out, 272, 2 );
            writeInt( out, 274, 3 );
            writeInt( out, 278, 44 );
            writeInt( out, 283, 0 );
            writeInt( out, 284, 8 );
            writeInt( out, 340, 0x11 );
          }
          writeEndTable( out );

          handle = inc(handle);
          writeBeginTable( out, "BLOCK_RECORD", handle, BrushManager.getPointLibSize() );
          {
            handle = writeSpaceBlockRecord( out, "*Model_Space", handle );
            handle = writeSpaceBlockRecord( out, "*Paper_Space", handle );

            for ( int n = 0; n < BrushManager.getPointLibSize(); ++ n ) {
              String th_name = BrushManager.getPointThName(n).replace(':','-');
              writeString( out, 0, "BLOCK_RECORD" );
              handle = inc(handle);
              writeAcDb( out, handle, AcDbSymbolTR, "AcDbBlockTableRecord" );
              writeString( out, 8, "P_" + th_name );
              writeString( out, 2, "B_" + th_name ); // block name
              writeInt( out, 70, 0 );                // block insertion units
            }
          }
          writeEndTable( out );
        }
      }
      writeEndSection( out );
      out.flush();
      
      writeSection( out, "BLOCKS" );
      {
        if ( mVersion13 ) {
          handle = writeSpaceBlock( out, "*Model_Space", handle );
          handle = writeSpaceBlock( out, "*Paper_Space", handle );
        }

        // // 8 layer (0), 2 block name,
        for ( int n = 0; n < BrushManager.getPointLibSize(); ++ n ) {
          SymbolPoint pt = (SymbolPoint)BrushManager.getPointByIndex(n);
	  String th_name = pt.getThName().replace(':','-');
          writeString( out, 0, "BLOCK" );
          handle = inc(handle);
          writeAcDb( out, handle, AcDbEntity, "AcDbBlockBegin" );
          // writeString( out, 8, "P_" + th_name );
          writeString( out, 2, "B_" + th_name ); // block name, can be repeated with '3'
          writeInt( out, 70, 0 );       // flag 0=none, 1=anonymous, 2=non-conts attr, 4=xref, 8=xref overlay,
                                        // 16=ext. dependent, 32=ext. resolved (ignored), 64=referenced xref (ignored)
          writeXYZ( out, 0, 0, 0, 0 );
          out.write( pt.getDxf() );
          // out.write( BrushManager.mPointLib.getPoint(n).getDxf() );

          writeString( out, 0, "ENDBLK" );
          if ( mVersion13 ) {
            handle = inc(handle);
            writeAcDb( out, handle, AcDbEntity, "AcDbBlockEnd");
            // writeString( out, 8, "POINT" );
            writeString( out, 8, "P_" + th_name );
          }
        }
      }
      writeEndSection( out );
      out.flush();

      writeSection( out, "ENTITIES" );
      {
	String scale_len = TDString.TWENTY;
        float sc1 = 20; // DrawingUtil.SCALE_FIX / 2 = 10;

        // reference
        StringWriter sw9 = new StringWriter();
        PrintWriter pw9  = new PrintWriter(sw9);
	float sc2 = sc1 / 2;
        handle = printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax,     xmin+sc1,  -ymax );
        handle = printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax,     xmin,      -ymax+sc1 );
        handle = printLine( pw9, 1.0f, handle, "REF", xmin+sc2, -ymax,     xmin+sc2,  -ymax+0.5f ); // 10 m ticks
        handle = printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax+sc2, xmin+0.5f, -ymax+sc2 );
        handle = printLine( pw9, 1.0f, handle, "REF", xmin+sc1, -ymax,     xmin+sc1,  -ymax+0.5f ); // 20 m ticks
        handle = printLine( pw9, 1.0f, handle, "REF", xmin,     -ymax+sc1, xmin+0.5f, -ymax+sc1 );
        // out.write( sw9.getBuffer().toString() );
	
        // printString( pw9, 0, "LINE" );
        // handle = inc(handle);
        // printAcDb( pw9, handle, AcDbEntity, AcDbLine );
        // printString( pw9, 8, "REF" );
        // // printInt(  pw9, 39, 0 );         // line thickness
        // printXYZ( pw9, xmin, -ymax, 0.0f, 0 );
        // printXYZ( pw9, (xmin+sc1), -ymax, 0.0f, 1 );
        // out.write( sw9.getBuffer().toString() );

        // StringWriter sw8 = new StringWriter();
        // PrintWriter pw8  = new PrintWriter(sw8);
        // printString( pw8, 0, "LINE" );
        // handle = inc(handle);
        // printAcDb( pw8, handle, AcDbEntity, AcDbLine );
        // printString( pw8, 8, "REF" );
        // // printInt(  pw8, 39, 0 );         // line thickness
        // printXYZ( pw8, xmin, -ymax, 0.0f, 0 );
        // printXYZ( pw8,  xmin, -ymax+sc1, 0.0f, 1 );
        // out.write( sw8.getBuffer().toString() );
        // out.flush();
        
	// offset axes legends by 1
        // StringWriter sw7 = new StringWriter();
        // PrintWriter pw7  = new PrintWriter(sw7);
        handle = printText( pw9, handle, scale_len, xmin+sc1, -ymax+1, 0, AXIS_SCALE, "REF", style_dejavu, xoff, yoff );
        handle = printText( pw9, handle, scale_len, xmin+1, -ymax+sc1, 0, AXIS_SCALE, "REF", style_dejavu, xoff, yoff );
        out.write( sw9.getBuffer().toString() );
       
        out.flush();

        // centerline data
        if ( PlotType.isSketch2D( type ) ) {
          for ( DrawingPath sh : plot.getLegs() ) {
            DBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            
            StringWriter sw4 = new StringWriter();
            PrintWriter pw4  = new PrintWriter(sw4);
            printString( pw4, 0, "LINE" );
            handle = inc(handle);
                        printAcDb( pw4, handle, AcDbEntity, AcDbLine );
            printString( pw4, 8, "LEG" );
            // printInt( pw4, 39, 2 );         // line thickness

            // // if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
            //   NumStation f = num.getStation( blk.mFrom );
            //   NumStation t = num.getStation( blk.mTo );
            //   if ( type == PlotType.PLOT_PLAN ) {
            //     float x0 = scale *( xoff + DrawingUtil.toSceneX( f.e, f.s ) );
            //     float y0 = scale *( yoff + DrawingUtil.toSceneY( f.e, f.s ) );
            //     float x1 = scale *( xoff + DrawingUtil.toSceneX( t.e, t.s ) );
            //     float y1 = scale *( yoff + DrawingUtil.toSceneY( t.e, t.s ) );
            //     printXYZ( pw4, x0, -y0, 0.0f, 0 );
            //     printXYZ( pw4, x1, -y1, 0.0f, 1 );
            //   } else if ( PlotType.isProfile( type ) ) {
            //     float x0 = scale *( xoff + DrawingUtil.toSceneX( f.h, f.v ) );
            //     float y0 = scale *( yoff + DrawingUtil.toSceneY( f.h, f.v ) );
            //     float x1 = scale *( xoff + DrawingUtil.toSceneX( t.h, t.v ) );
            //     float y1 = scale *( yoff + DrawingUtil.toSceneY( t.h, t.v ) );
            //     printXYZ( pw4, x0, -y0, 0.0f, 0 );
            //     printXYZ( pw4, x1, -y1, 0.0f, 1 );
            //   // } else if ( type == PlotType.PLOT_SECTION ) {
            //   //   /* nothing */
            //   }
            // // }
            printXYZ( pw4, scale*(xoff + sh.x1), -scale*(yoff + sh.y1), 0.0f, 0 );
            printXYZ( pw4, scale*(xoff + sh.x2), -scale*(yoff + sh.y2), 0.0f, 1 );
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

            //   printString( pw41, 0, "LINE" );
            //   handle = inc(handle); printAcDb( pw41, handle, AcDbEntity, AcDbLine );
            //   printString( pw41, 8, "SPLAY" );
            //   // printInt( pw41, 39, 1 );         // line thickness

            //   float dhs = scale * blk.mLength * (float)Math.cos( blk.mClino * TDMath.DEG2RAD )*sc1/10; // scaled dh
            //   if ( type == PlotType.PLOT_PLAN ) {
            //     float x = scale * DrawingUtil.toSceneX( f.e, f.s );
            //     float y = scale * DrawingUtil.toSceneY( f.e, f.s );
            //     float de =   dhs * (float)Math.sin( blk.mBearing * TDMath.DEG2RAD);
            //     float ds = - dhs * (float)Math.cos( blk.mBearing * TDMath.DEG2RAD);
            //     printXYZ( pw41, x, -y, 0.0f, 0 );
            //     printXYZ( pw41, x + de, -(y+ds), 0.0f, 1 );
            //   } else if ( PlotType.isProfile( type ) ) {
            //     float x = scale * DrawingUtil.toSceneX( f.h, f.v );
            //     float y = scale * DrawingUtil.toSceneY( f.h, f.v );
            //     float dv = - blk.mLength * (float)Math.sin( blk.mClino * TDMath.DEG2RAD )*sc1/10;
            //     printXYZ( pw41, x, -y, 0.0f, 0 );
            //     printXYZ( pw41, x+dhs*blk.getReducedExtend(), -(y+dv), 0.0f, 1 ); 
            //   } else if ( type == PlotType.PLOT_SECTION ) {
            //     // nothing
            //   }
            // // }

            printString( pw41, 0, "LINE" );
            handle = inc(handle);
            printAcDb( pw41, handle, AcDbEntity, AcDbLine );
            printString( pw41, 8, "SPLAY" );
            // printInt( pw41, 39, 1 );         // line thickness
            printXYZ( pw41, scale*(xoff + sh.x1), -scale*(yoff + sh.y1), 0.0f, 0 );
            printXYZ( pw41, scale*(xoff + sh.x2), -scale*(yoff + sh.y2), 0.0f, 1 );

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
            handle = printText( pw5, handle, sp.name(), (sp.cx+xoff) * scale, -(sp.cy+yoff) * scale,
                                0, LABEL_SCALE, "STATION", style_dejavu, xoff, yoff );
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
              printString(pw5, 0, "TEXT");
              printString(pw5, 8, "P_" + th_name);
              printFloat(pw5, 40, point.getScaleValue() * 1.4f);
              printString(pw5, 1, label.mPointText);
              printFloat(pw5, 50, 360.0f - (float)(point.mOrientation));
              printXYZ(pw5, (point.cx + xoff) * scale, -(point.cy + yoff) * scale, 0, 0);
            } else {
              if ( mVersion13 ) {
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
                printString( pw5, 0, "INSERT" );
                printString( pw5, 8, "P_" + th_name );
                printString( pw5, 2, "B_" + th_name );
                printFloat( pw5, 41, point.getScaleValue()*1.4f ); // FIX Asenov
                printFloat( pw5, 42, point.getScaleValue()*1.4f );
                printFloat( pw5, 50, 360.0f-(float)(point.mOrientation) );
                printXYZ( pw5, (point.cx+xoff)*scale, -(point.cy+yoff)*scale, 0, 0 );
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
            handle = printLine( pw6,scale,handle,"STATION", xoff+st.cx, -(yoff+st.cy), xoff+st.cx+len, -(yoff+st.cy) );
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
      writeEndSection( out );
      if ( mVersion13 ) {
        handle = writeSectionObjects( out, handle );
      }
      writeString( out, 0, "EOF" );
      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TDLog.Error( "DXF io-exception " + e.toString() );
    }
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingStationName sn, float scale, float xoff, float yoff )
  { // FIXME point scale factor is 0.3
    if ( sn == null ) return handle;
    return printText( pw, handle, sn.getName(),  (sn.cx+xoff)*scale, -(sn.cy+yoff)*scale, 0,
                        STATION_SCALE, "STATION", style_dejavu, xoff, yoff );
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingStationPath sp, float scale, float xoff, float yoff )
  { // FIXME point scale factor is 0.3
    if ( sp == null ) return handle;
    return printText( pw, handle, sp.name(),  (sp.cx+xoff)*scale, -(sp.cy+yoff)*scale, 0,
                        STATION_SCALE, "STATION", style_dejavu, xoff, yoff );
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingPointPath point, float scale, float xoff, float yoff )
  { // FIXME point scale factor is 0.3
    if ( point == null ) return handle;
    if ( BrushManager.isPointLabel( point.mPointType ) ) {
      DrawingLabelPath label = (DrawingLabelPath)point;
      // Log.v("DistoX", "LABEL PATH label <" + label.mPointText + ">" );
      return printText( pw, handle, label.mPointText,  (point.cx+xoff)*scale, -(point.cy+yoff)*scale, 360.0f-(float)label.mOrientation,
                        LABEL_SCALE, "POINT", style_dejavu, xoff, yoff );
    }

    String th_name = point.getThName().replace(':','-');
    // int idx = 1 + point.mPointType;
    printString( pw, 0, "INSERT" );
    handle = inc(handle); printAcDb( pw, handle, "AcDbBlockReference" );
    printString( pw, 8, "P_" + th_name );
    printString( pw, 2, "B_" + th_name );
    printFloat( pw, 41, point.getScaleValue()*1.4f ); // FIX Asenov
    printFloat( pw, 42, point.getScaleValue()*1.4f );
    printFloat( pw, 50, 360.0f-(float)(point.mOrientation) );
    printXYZ( pw, (point.cx+xoff)*scale, -(point.cy+yoff)*scale, 0, 0 );
    return handle;
  }

  static private int toDxf( PrintWriter pw, int handle, DrawingLinePath line, float scale, float xoff, float yoff )
  {
    if ( line == null ) return handle;
    String layer = "L_" + line.getThName( ).replace(':','-');
    int flag = 0;
    if ( mVersion13 && checkSpline( line ) ) {
      if ( TDSetting.mAcadSpline ) {
        handle = printPolylineHeader( pw, handle, layer, line.isClosed() );
        handle = printInterpolatedPolyline( pw, line, scale, handle, layer, line.isClosed(), xoff, yoff );
        handle = printPolylineFooter( pw, handle );
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
    if ( mVersion13 && checkSpline( area ) ) {
      if ( TDSetting.mAcadSpline ) {
        handle = printPolylineHeader( pw, handle, layer, true );
        handle = printInterpolatedPolyline( pw, area, scale, handle, layer, true, xoff, yoff );
        handle = printPolylineFooter( pw, handle );
      } else {
        handle = printSpline( pw, area, scale, handle, layer, true, xoff, yoff );
      }
    } else {
      // handle = printLWPolyline( pw5, line, scale, handle, layer, true );
      handle = printPolyline( pw, area, scale, handle, layer, true, xoff, yoff );
    }
    if ( mVersion13 ) {
      printString( pw, 0, "HATCH" );    // entity type HATCH
      handle = inc(handle);
      printAcDb( pw, handle, AcDbEntity, "AcDbHatch" );
      // printString( pw5, 8, "AREA" );  // layer (color BYLAYER)
      printString( pw, 8, layer );      // layer (color BYLAYER)
      printString( pw, 2, "_USER" );    // hatch pattern name

      printXYZ( pw, 0f, 0f, 0f, 0 );
      printXYZ( pw, 0f, 0f, 1f, 200 );  // extrusion direction, default 0,0,1
      printInt( pw, 70, 1 );            // 1:solid fill, 0:pattern-fill
      printInt( pw, 71, 0 );            // 1:associative 0:non-associative
      printInt( pw, 91, 1 );            // nr. boundary paths (loops): 1
      // boundary data
        printInt( pw, 92, 2 );          // flag. 1:external 2:polyline 4:derived 8:text 16:outer
        printInt( pw, 72, 0 );          // not-polyline edge type (0: default) 1:line 2:arc 3:ellipse-arec 4:spline
                                        // polyline: has-bulge
        printInt( pw, 73, 1 );          // is-closed flag

        int npt = countInterpolatedPolylinePoints( area, true );
        /* 
        int npt = 0;
        LinePoint p = area.mFirst;
        float x0 = p.x;
        float y0 = p.y;
        ++npt;
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
            if ( np > 1 ) npt += (np-1);
          }
          ++npt;
          x0 = x3;
          y0 = y3;
        }
        ++npt;
        */
        printInt( pw, 93, npt ); // nr. of points (not polyline) - nr. vertices (polyline)
        // printInt( pw, 93, area.size() ); // nr. of points (not polyline) vertices (polyline)
      // bezier interpolation
      printInterpolatedPolyline( pw, area, scale, 0, null, true, xoff, yoff );
      /*
      p = area.mFirst;
      x0 = p.x;
      y0 = p.y;
      printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
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
          if ( np > 1 ) {
            BezierCurve bc = new BezierCurve( x0, y0, x1, y1, x2, y2, x3, y3 );
            for ( int n=1; n < np; ++n ) {
              Point2D pb = bc.evaluate( (float)n / (float)np );
              printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
            }
          }
        }
        printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
        x0 = x3;
        y0 = y3;
      }
      p = area.mFirst;
      x0 = p.x;
      y0 = p.y;
      printXY( pw, (p.x+xoff)*scale, -(p.y+yoff)*scale, 0 );
      */
      // bezier interpolation

      // printXY( pw, area.mFirst.x * scale, -area.mFirst.y * scale, 0 );
        printInt( pw, 97, 1 );            // nr. source boundary objects
      printInt( pw, 75, 0 );            // hatch style: 0:normal, 1:outer, 2:ignore
      printInt( pw, 76, 1 );            // hatch pattern type: 0:user, 1:predefined, 2:custom
      printFloat( pw, 52, 0f );         // hatch pattern angle (only pattern fill)
      printFloat( pw, 41, 1f );         // hatch pattern scale (only pattern fill)
      printInt( pw, 77, 0 );            // hatch pattern double flag, 0: not double, 1: double (pattern fill only)
      printInt( pw, 78, 1 );            // nr. pattern definition lines
      /* here goes pattern data
        printFloat( pw, 53, 45f );        // pattern line angle
        printFloat( pw, 43, 0f );         // pattern base point
        printFloat( pw, 44, 0f );
        printFloat( pw, 45, -3.6f );      // pattern line offset
        printFloat( pw, 46, 3.6f );         
        printInt( pw, 79, 0 );            // nr. dash length items
      // // printFloat( pw, 49, 3f );         // dash length (repeated nr. times)
      */

       printFloat( pw, 47, 0.02f );         // pixel size
       printInt( pw, 98, 0 );            // nr. seed points
      // printXYZ( pw, 0f, 0f, 0f, 0 );

      // 450 451 452 453 460 461 462 and 470 all present or none
      // printInt( pw, 450, 0 ); // 0:solid, 1:gradient
      // printInt( pw, 451, 0 ); // reserved
      // printInt( pw, 452, 1 ); // 1:single color  2:two-color
      // printInt( pw, 453, 0 ); // 0:solid, 2:gradient
      // printFloat( pw, 460, 0f ); // rotation angle [rad]
      // printFloat( pw, 461, 0f ); // gradient definition
      // printFloat( pw, 462, 0.5f );  // color tint
      // printFloat( pw, 463, 0f ); // reserved
      // printString( pw, 470, "LINEAR" );  // default
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

// SECTION OBJECTS
  static private int writeSectionObjects( BufferedWriter out, int handle ) throws IOException
  {
    writeSection( out, "OBJECTS" );

    StringWriter swx = new StringWriter();
    PrintWriter pwx  = new PrintWriter(swx);

    printString( pwx, 0, "DICTIONARY" );
    int saved = handle = inc(handle);
    printAcDb( pwx, handle, AcDbDictionary );
    // printInt( pwx, 280, 0 );
    printInt( pwx, 281, 1 );
    printString( pwx, 3, "ACAD_GROUP" );
    handle = inc(handle); printHex( pwx, 350, handle );

    printString( pwx, 0, "DICTIONARY" );
    handle = inc(handle); printAcDb( pwx, handle, AcDbDictionary );
    // printInt( pwx, 280, 0 );
    printInt( pwx, 281, 1 );
    printHex( pwx, 330, saved );

    out.write( swx.getBuffer().toString() );
    out.flush();

    writeEndSection( out );
    return handle;
  }

/*
  static int writeSectionObjects( BufferedWriter out, int handle ) throws IOException
  {
    writeSection( out, "OBJECTS" );
    {
      StringWriter swx = new StringWriter();
      PrintWriter pwx  = new PrintWriter(swx);
      printString( pwx, 0, "DICTIONARY" );
      handle = inc(handle); printAcDb( pwx, handle, AcDbDictionary );
      printInt( pwx, 280, 0 );
      printInt( pwx, 281, 1 );
      printString( pwx, 3, "ACAD_GROUP" );         handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 3, "ACAD_LAYOUT" );        handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 3, "ACAD_MLINESTYLE" );    handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 3, "ACAD_PLOTSETTING" );   handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 3, "ACAD_PLOTSTYLENAME" ); handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 3, "AcDbVariableDictionary" ); handle = inc(handle); printHex( pwx, 350, handle );

      printString( pwx, 0, "DICTIONARY" );
      handle = inc(handle); printAcDb( pwx, handle, AcDbDictionary );
      printInt( pwx, 280, 0 );
      printInt( pwx, 281, 1 );

      printString( pwx, 0, "ACDBDICTIONARYWDFLT" );
      handle = inc(handle); printAcDb( pwx, handle, AcDbDictionary );
      printInt( pwx, 281, 1 );
      printString( pwx, 3, "Normal" );
      handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 100, "AcDbDictionaryWithDefault");
      handle = inc(handle); printHex( pwx, 340, handle );

      printString( pwx, 0, "ACDBPLACEHOLDER");
      handle = inc(handle); printHex( pwx, 5, handle );

      printString( pwx, 0, "DICTIONARY" );
      handle = inc(handle); printAcDb( pwx, handle, AcDbDictionary );
      printInt( pwx, 280, 0 );
      printInt( pwx, 281, 1 );
      printString( pwx, 3, "Standard" );
      handle = inc(handle); printHex( pwx, 350, handle );

      printString( pwx, 0, "MLINESTYLE" );
      handle = inc(handle); printAcDb( pwx, handle, "AcDbMlineStyle" );
      printString( pwx, 2, "STANDARD" );
      printInt( pwx, 70, 0 );
      printStringEmpty( pwx, 3 );
      printInt( pwx, 62, 256 );
      printInt( pwx, 51, 90 );
      printInt( pwx, 52, 90 );
      printInt( pwx, 71, 2 );
      printFloat( pwx, 49, 0.5f );
      printInt( pwx, 62, 256 );
      printString( pwx, 6, "BYLAYER" );
      printFloat( pwx, 49, -0.5f );
      printInt( pwx, 62, 256 );
      printString( pwx, 6, "BYLAYER" );
    
      printString( pwx, 0, "DICTIONARY" );
      handle = inc(handle); printAcDb( pwx, handle, AcDbDictionary );
      printInt( pwx, 280, 0 );
      printInt( pwx, 281, 1 );

      printString( pwx, 0, "DICTIONARY" );
      handle = inc(handle); printAcDb( pwx, handle, AcDbDictionary );
      printInt( pwx, 281, 1 );
      printString( pwx, 3, "Layout1" ); handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 3, "Layout2" ); handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 3, "Model" );   handle = inc(handle); printHex( pwx, 350, handle );

      printString( pwx, 0, "LAYOUT" );
      handle = inc(handle); printAcDb( pwx, handle, "AcDbPlotSetting" );
      printStringEmpty( pwx, 1 );
      printString( pwx, 2, "" );
      printStringEmpty( pwx, 4 );
      printStringEmpty( pwx, 6 );
      printZero( pwx, 40, 50 );
      printInt( pwx, 140, 0 );
      printInt( pwx, 141, 0 );
      printInt( pwx, 142, 1 );
      printInt( pwx, 143, 1 );
      printInt( pwx, 70, 688 );
      printInt( pwx, 72, 0 );
      printInt( pwx, 73, 0 );
      printInt( pwx, 74, 5 );
      printStringEmpty( pwx, 7 );
      printInt( pwx, 75, 16 );
      printInt( pwx, 147, 1 );
      printInt( pwx, 148, 0 );
      printInt( pwx, 149, 0 );
      printString( pwx, 100, "AcDbLayout" );
      printString( pwx, 1, "Layout1" );
      printInt( pwx, 70, 1 );
      printInt( pwx, 71, 1 );
      printXY( pwx, 0, 0, 0 );
      printXY( pwx, 420, 297, 1 );
      printXYZ( pwx, 0, 0, 0, 2 );
      printXYZ( pwx, 1.e+20f, 1.e+20f, 1.e+20f, 4 );
      printXYZ( pwx, -1.e+20f, -1.e+20f, -1.e+20f, 5 );
      printInt( pwx, 146, 0 );
      printXYZ( pwx, 0, 0, 0, 3 );
      printXYZ( pwx, 1, 0, 0, 6 );
      printXYZ( pwx, 0, 1, 0, 7 );
      printInt( pwx, 76, 0 );
      handle = inc(handle); printHex( pwx, 330, handle );
  
      printString( pwx, 0, "LAYOUT" );
      handle = inc(handle); printAcDb( pwx, handle, "AcDbPlotSetting" );
      printStringEmpty( pwx, 1 );
      printString( pwx, 2, "" );
      printStringEmpty( pwx, 4 );
      printStringEmpty( pwx, 6 );
      printZero( pwx, 40, 50 );
      printInt( pwx, 140, 0 );
      printInt( pwx, 141, 0 );
      printInt( pwx, 142, 1 );
      printInt( pwx, 143, 1 );
      printInt( pwx, 70, 1712 );
      printInt( pwx, 72, 0 );
      printInt( pwx, 73, 0 );
      printInt( pwx, 74, 0 );
      printStringEmpty( pwx, 7 );
      printInt( pwx, 75, 0 );
      printInt( pwx, 147, 1 );
      printInt( pwx, 148, 0 );
      printInt( pwx, 149, 0 );
      printString( pwx, 100, "AcDbLayout" );
      printString( pwx, 1, "Model" );
      printInt( pwx, 70, 1 );
      printInt( pwx, 71, 0 );
      printXY( pwx, 0, 0, 0 );
      printXY( pwx, 12, 9, 1 );
      print245_367( pwx );
      handle = inc(handle); printHex( pwx, 330, handle );

      printString( pwx, 0, "LAYOUT" );
      handle = inc(handle); printAcDb( pwx, handle, "AcDbPlotSetting" );
      printStringEmpty( pwx, 1 );
      printString( pwx, 2, "" );
      printStringEmpty( pwx, 4 );
      printStringEmpty( pwx, 6 );
      printZero( pwx, 40, 50 );
      printInt( pwx, 140, 0 );
      printInt( pwx, 141, 0 );
      printInt( pwx, 142, 1 );
      printInt( pwx, 143, 1 );
      printInt( pwx, 70, 688 );
      printInt( pwx, 72, 0 );
      printInt( pwx, 73, 0 );
      printInt( pwx, 74, 5 );
      printStringEmpty( pwx, 7 );
      printInt( pwx, 75, 16 );
      printInt( pwx, 147, 1 );
      printInt( pwx, 148, 0 );
      printInt( pwx, 149, 0 );
      printString( pwx, 100, "AcDbLayout" );
      printString( pwx, 1, "Layout2" );
      printInt( pwx, 70, 1 );
      printInt( pwx, 71, 2 );
      printXY( pwx, 0, 0, 0 );
      printXY( pwx, 12, 9, 1 );
      print245_367( pwx );
      handle = inc(handle); printHex( pwx, 330, handle );

      printString( pwx, 0, "DICTIONARY" );
      handle = inc(handle); printAcDb( pwx, handle, AcDbDictionary );
      printInt( pwx, 281, 1 );
      printString( pwx, 3, "DIMASSOC" );
      handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 3, "HIDETEXT" );
      handle = inc(handle); printHex( pwx, 350, handle );
      printString( pwx, 0, "DICTIONARYVAR" );
      handle = inc(handle); printAcDb( pwx, handle, "DictionaryVariables" );
      printInt( pwx, 280, 0 );
      printInt( pwx, 1, 2 );
      printString( pwx, 0, "DICTIONARYVAR" );
      handle = inc(handle); printAcDb( pwx, handle, "DictionaryVariables" );
      printInt( pwx, 280, 0 );
      printInt( pwx, 1, 1 );

      out.write( swx.getBuffer().toString() );
      out.flush();
    }
    writeEndSection( out );
    return handle;
  }
      
  static void printZero( PrintWriter pw, int from, int to )
  {
    for ( ; from < to; ++ from ) {
      printInt( pw, from, 0 );
    }
  }

  static void print245_367( PrintWriter pw ) 
  {
    printXYZ( pw, 0, 0, 0, 2 );
    printXYZ( pw, 0, 0, 0, 4 );
    printXYZ( pw, 0, 0, 0, 5 );
    printInt( pw, 146, 0 );
    printXYZ( pw, 0, 0, 0, 3 );
    printXYZ( pw, 1, 0, 0, 6 );
    printXYZ( pw, 0, 1, 0, 7 );
    printInt( pw, 76, 0 );
  }
*/


}

