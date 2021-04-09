/* @file SymbolPointDxf.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief TopoDroid drawing: point symbol dxf encoder
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.math.BezierCurve;
import com.topodroid.math.Point2D;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedWriter;


import java.util.Locale;
import java.util.ArrayList;

class SymbolPointDxf
{
  private class DxfToken
  {
    int version;
    String string; // string or null for handle

    DxfToken( int v, String s ) 
    {
      version = v;
      string  = s;
    }
  }
 
  private ArrayList< DxfToken > mDxfTokens; // PRIVATE

  SymbolPointDxf( )
  {
    mDxfTokens = new ArrayList< DxfToken >();
  }

  public int writeDxf( BufferedWriter out, int version, int handle ) throws IOException
  {
    for ( DxfToken token : mDxfTokens ) {
      if ( version >= token.version ) {
        if ( token.string == null ) { // increment handle and print it
          handle = DrawingDxf.inc( handle );
          DrawingDxf.writeHex( out, 5, handle );
        } else {
          out.write( token.string );
        }
      }
    }
    return handle;
  }

  private void addToken( DxfToken token ) { mDxfTokens.add( token ); }
    
  private void startItem( String name, String pname )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printString( pw, 0, name );
    DrawingDxf.printString( pw, 8, pname );
    addToken( new DxfToken( 9, sw.toString() ) );
  }

  void startLine( String pname ) { startItem( "LINE", pname ); }
  void startPolyline( String pname ) { startItem( "POLYLINE", pname ); }
  void startCircle( String pname ) { startItem( "CIRCLE", pname ); }
  void startArc( String pname ) { startItem( "ARC", pname ); }
  void startEllipse( String pname ) { startItem( "ELLIPSE", pname ); }
    
  private void addAcDb( String acdbitem )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printAcDb( pw, -1, "AcDbEntity", acdbitem );
    addToken( new DxfToken( 13, sw.toString() ) );
  }

  void addAcDbLine( )   { addAcDb( "AcDbLine" ); }
  void addAcDbCircle( ) { addAcDb( "AcDbCircle" ); }
  void addAcDbArc( )    { addAcDb( "AcDbArc" ); }
  void addAcDbEllipse() { addAcDb( "AcDbEllipse" ); }

  void addHandle() { addToken( new DxfToken( 13, null ) ); }

  void addLine( float x0, float y0, float x1, float y1 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printXYZ( pw, x0, y0, 0.0f, 0 ); // prev point
    DrawingDxf.printXYZ( pw, x1, y1, 0.0f, 1 ); // current point
    addToken( new DxfToken( 9, sw.toString() ) );
  }

  void headerPolyline( float x, float y, boolean closed )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    // DrawingDxf.printInt(  pw, 39, 1 ); // line thickness
    // DrawingDxf.printInt(  pw, 40, 1 ); // start width
    // DrawingDxf.printInt(  pw, 41, 1 ); // end width
    DrawingDxf.printInt( pw, 66, 1 ); // group 1
    DrawingDxf.printInt( pw, 70, 8 + (closed? 1 : 0) ); // polyline flag 8 = 3D polyline, 1 = closed 
    DrawingDxf.printInt( pw, 75, 0 ); // 6 cubic spline, 5 quad spline, 0
    DrawingDxf.printXYZ( pw, x, y, 0.0f, 0 ); // position
    addToken( new DxfToken( 9, sw.toString() ) );
  }

  void addVertex( String pname, float x, float y )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printString( pw, 0, "VERTEX" );
    DrawingDxf.printString( pw, 8, pname );
    DrawingDxf.printXYZ( pw, x, y, 0.0f, 0 );
    addToken( new DxfToken( 9, sw.toString() ) );
  }

  void closeSeq()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printString( pw, 0, "SEQEND" );
    addToken( new DxfToken( 9, sw.toString() ) );
  }

  void addCircle( float x, float y, float r )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printXYZ( pw, x, y, 0.0f, 0 );
    DrawingDxf.printFloat( pw, 40, r );
    addToken( new DxfToken( 9, sw.toString() ) );
  }

  void addArcAngles( float a1, float a2 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printFloat( pw, 50, a1 );     // ANGLES
    DrawingDxf.printFloat( pw, 51, a1+a2 );
    addToken( new DxfToken( 9, sw.toString() ) );
  }

  void addEllipse( float a1, float a2 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    // pw.printf(Locale.US,
    //           "  10\n%.2f\n  20\n%.2f\n  30\n%.2f\n  11\n%.2f\n  21\n%.2f\n  31\n%.2f\n  40\n%.2f\n  41\n%.2f\n  42\n%.2f\n",
    //           (x0+x1)/2*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                 // CENTER
    //           x1*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                        // ENDPOINT OF MAJOR AXIS
    //           (y1-y0)/(x1-x0),                                              // RATIO MINOR/MAJOR
    //           x2*TDMath.DEG2RAD, (x2+y2)*TDMath.DEG2RAD );  // START and END PARAMS
    addToken( new DxfToken( 9, sw.toString() ) );
  }

}

