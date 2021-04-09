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
  final static int BY_BLOCK = 0;
  final static int BY_LAYER = 256;

  private class DxfToken
  {
    int version;
    String string; // string or null for handle
    String name;

    DxfToken( int v, String s, String n ) 
    {
      version = v;
      string  = s;
      name    = n;
    }

    // void dump()
    // {
    //   Log.v("DistoX", "Token " + name + " " + version );
    // }
  }
 
  private ArrayList< DxfToken > mDxfTokens; // PRIVATE

  SymbolPointDxf( )
  {
    mDxfTokens = new ArrayList< DxfToken >();
  }

  public int writeDxf( BufferedWriter out, int version, int handle ) throws IOException
  {
    for ( DxfToken token : mDxfTokens ) {
      if ( token.version <= version ) {
        if ( token.string == null ) { // increment handle and print it
          Log.v("DistoX", "print token version " + token.version + "/" + version + " HANDLE" );
          if ( handle >= 0 ) {
            handle = DrawingDxf.inc( handle );
            // DrawingDxf.writeHex( out, 5, handle );
            StringWriter sw = new StringWriter();
            PrintWriter pw  = new PrintWriter(sw);
            pw.printf("  5%s%X%s", DrawingDxf.EOL, handle, DrawingDxf.EOL );
            out.write( sw.getBuffer().toString() );
          }
        } else {
          Log.v("DistoX", "print token version " + token.version + "/" + version + token.name );
          out.write( token.string );
        }
      } else {
        Log.v("DistoX", "skip token version " + token.version + "/" + version + token.name );
      }
    }
    return handle;
  }

  private void addToken( DxfToken token ) 
  { 
    mDxfTokens.add( token );
    // token.dump();
  }
    
  private void startItem( String name, String pname )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printString( pw, 0, name );
    DrawingDxf.printString( pw, 8, pname );
    addToken( new DxfToken( 9, sw.toString(), "Start " + name + " " + pname ) );
  }

  void startLine( String pname ) { startItem( "LINE", pname ); }
  void startPolyline( String pname ) { startItem( "POLYLINE", pname ); }
  void startCircle( String pname ) { startItem( "CIRCLE", pname ); }
  void startArc( String pname ) { startItem( "ARC", pname ); }
  void startEllipse( String pname ) { startItem( "ELLIPSE", pname ); }
  void startVertex( String pname ) { startItem( "VERTEX", pname ); }
    
  private void addAcDb( String acdbitem )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    // DrawingDxf.printAcDb( pw, -1, "AcDbEntity", acdbitem );
    pw.printf( DrawingDxf.EOL100 +  "AcDbEntity" + DrawingDxf.EOL + DrawingDxf.EOL100 + acdbitem + DrawingDxf.EOL );
    addToken( new DxfToken( 13, sw.toString(), acdbitem ) );
  }

  void addAcDbLine( )    { addAcDb( "AcDbLine" ); }
  void addAcDbPolyline() { addAcDb( "AcDbPolyline" ); }
  void addAcDbCircle( )  { addAcDb( "AcDbCircle" ); }
  void addAcDbArc( )     { addAcDb( "AcDbArc" ); }
  void addAcDbEllipse()  { addAcDb( "AcDbEllipse" ); }
  void addAcDbVertex() 
  { 
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    pw.printf( DrawingDxf.EOL100 +  "AcDbVertex" + DrawingDxf.EOL + DrawingDxf.EOL100 + "AcDb3dPolylineVertex" + DrawingDxf.EOL + "  70" + DrawingDxf.EOL + "32" + DrawingDxf.EOL );
    addToken( new DxfToken( 13, sw.toString(), "AcDbVertex" ) );
  }

  void addHandle() { addToken( new DxfToken( 13, null, "Handle" ) ); }

  void addLine( float x0, float y0, float x1, float y1 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printInt( pw, 62, BY_LAYER ); // color 0: by_block, 256: by_layer
    DrawingDxf.printXYZ( pw, x0, y0, 0.0f, 0 ); // prev point
    DrawingDxf.printXYZ( pw, x1, y1, 0.0f, 1 ); // current point
    addToken( new DxfToken( 9, sw.toString(), "Line data" ) );
  }

  void addVertexData( float x0, float y0 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); 
    DrawingDxf.printXY( pw, x0, y0, 0 ); 
    addToken( new DxfToken( 9, sw.toString(), "Vertex data" ) );
  }

  void headerPolyline( float x, float y, boolean closed )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    // DrawingDxf.printInt(  pw, 39, 1 ); // line thickness
    // DrawingDxf.printInt(  pw, 40, 1 ); // start width
    // DrawingDxf.printInt(  pw, 41, 1 ); // end width
    DrawingDxf.printInt( pw, 62, BY_LAYER ); // color 0: by_block, 256: by_layer
    DrawingDxf.printInt( pw, 66, 1 ); // group 1
    DrawingDxf.printInt( pw, 70, 8 + (closed? 1 : 0) ); // polyline flag 8 = 3D polyline, 1 = closed 
    // DrawingDxf.printInt( pw, 75, 0 ); // 6 cubic spline, 5 quad spline, 0 is the default
    DrawingDxf.printXYZ( pw, x, y, 0.0f, 0 ); // position
    addToken( new DxfToken( 9, sw.toString(), "Polyline header" ) );
  }

  void addVertex( String pname, float x, float y )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printString( pw, 0, "VERTEX" );
    DrawingDxf.printString( pw, 8, pname );
    DrawingDxf.printXYZ( pw, x, y, 0.0f, 0 );
    addToken( new DxfToken( 9, sw.toString(), "Vertex data" ) );
  }

  void closeSeq()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printString( pw, 0, "SEQEND" );
    addToken( new DxfToken( 9, sw.toString(), "SeqEnd" ) );
  }

  void addCircle( float x, float y, float r )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printInt( pw, 62, BY_LAYER ); // color 0: by_block, 256: by_layer
    DrawingDxf.printXYZ( pw, x, y, 0.0f, 0 );
    DrawingDxf.printFloat( pw, 40, r );
    addToken( new DxfToken( 9, sw.toString(), "Circle data" ) );
  }

  void addArcAngles( float a1, float a2 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printFloat( pw, 50, a1 );     // ANGLES
    DrawingDxf.printFloat( pw, 51, a1+a2 );
    addToken( new DxfToken( 9, sw.toString(), "Arc data" ) );
  }

  // FIXME TODO
  // @param x0,y0 left endpoint
  // @param x1,y1 right endpoint
  // @param r     aspect ratio
  // @param a1,a2 angles
  void addEllipse( float x0, float y0, float x1, float y1, float r, float a1, float a2 ) 
  {
    float xc = (x0+x1)/2;
    float yc = (y0+y1)/2;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DrawingDxf.printInt( pw, 62, BY_LAYER ); // color 0: by_block, 256: by_layer
    DrawingDxf.printXYZ( pw, xc, yc, 0.0f, 0 ); // CENTER
    DrawingDxf.printXYZ( pw, x0, yc, 0.0f, 1 ); // LEFT VERTEX
    DrawingDxf.printFloat( pw, 40, r  );        // ASPECT_RATIO
    DrawingDxf.printFloat( pw, 41, a1 );        // ANGLES
    DrawingDxf.printFloat( pw, 42, a1+a2 );
    // pw.printf(Locale.US,
    //           "  10\n%.2f\n  20\n%.2f\n  30\n%.2f\n  11\n%.2f\n  21\n%.2f\n  31\n%.2f\n  40\n%.2f\n  41\n%.2f\n  42\n%.2f\n",
    //           (x0+x1)/2*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                 // CENTER
    //           x1*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                        // ENDPOINT OF MAJOR AXIS
    //           (y1-y0)/(x1-x0),                                              // RATIO MINOR/MAJOR
    //           x2*TDMath.DEG2RAD, (x2+y2)*TDMath.DEG2RAD );  // START and END PARAMS
    addToken( new DxfToken( 9, sw.toString(), "Ellipse" ) );
  }

}

