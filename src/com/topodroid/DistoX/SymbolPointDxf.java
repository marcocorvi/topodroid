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

  public void line( String pname, float x0, float y0, float x1, float y1 )
  {
    startLine( pname );
    addHandle();
    addAcDbLine();
    addLine( x0, y0, x1, y1 );
  }

  public void polyline( String pname, float[] xx, float[] yy )
  {
    startPolyline( pname );
    addHandle();
    addAcDbPolyline();
    headerPolyline( 0, 0, false );
    for ( int k=0; k<xx.length; ++k ) {
      startVertex( pname );
      addHandlePointer();
      addAcDbVertex();
      addVertexData( xx[k], yy[k] );
    }
    closeSeq();
    addHandle();
  }

  public void circle( String pname, float x, float y, float r )
  {
    startCircle( pname );
    addHandle();
    addAcDbCircle();
    addCircle( x, y, r );
  }


  // DXF.printString( pw, 0, "ARC" );
  // DXF.printString( pw, 8, pname );
  // DXF.printAcDb(pw, -1, "AcDbEntity", "AcDbCircle" );
  // DXF.printXYZ( pw, (x0+x1)/2*dxfScale, -(y0+y1)/2*dxfScale, 0.0f, 0 ); // CENTER
  // DXF.printFloat( pw, 40, x1*dxfScale );                                // RADIUS
  // DXF.printString( pw, 100, "AcDbArc" );
  // DXF.printFloat( pw, 50, x2 );                                         // ANGLES
  // DXF.printFloat( pw, 51, x2+y2 );
  public void arc( String pname, float x, float y, float r, float a1, float a2 )
  {
    startArc( pname );
    addHandle();
    addAcDbCircle(); // FIXME ???
    addCircle( x, y, r );
    addArcAngles( a1, a2 );
  }

  // DXF.printString( pw, 0, "ELLIPSE" );
  // DXF.printString( pw, 8, pname );
  // DXF.printAcDb(pw, -1, "AcDbEntity", AcDbEllipse" );
  // pw.printf(Locale.US,
  //           "  10\n%.2f\n  20\n%.2f\n  30\n%.2f\n  11\n%.2f\n  21\n%.2f\n  31\n%.2f\n  40\n%.2f\n  41\n%.2f\n  42\n%.2f\n",
  //           (x0+x1)/2*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                 // CENTER
  //           x1*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                        // ENDPOINT OF MAJOR AXIS
  //           (y1-y0)/(x1-x0),                                              // RATIO MINOR/MAJOR
  //           x2*TDMath.DEG2RAD, (x2+y2)*TDMath.DEG2RAD );  // START and END PARAMS
  public void ellipse( String pname, float x, float y, float a, float b, float a1, float a2 )
  {
    // TODO
  }

  public int writeDxf( BufferedWriter out, int version, int handle, int ref ) throws IOException
  {
    int saved_value = -1;
    for ( DxfToken token : mDxfTokens ) {
      if ( token.version <= version ) {
        if ( token instanceof HandlePointerToken && saved_value > 0 ) ((HandlePointerToken)token).setValue( saved_value );
        handle = token.write( out, version, handle, ref );
        if ( token instanceof HandleToken && handle > 0 ) saved_value = handle;
      }
    }
    return handle;
  }

  // ---------------------------------------------------------

  private final static int TOKEN_LINE     = 1;
  private final static int TOKEN_POLYLINE = 2;
  private final static int TOKEN_CIRCLE   = 3;
  private final static int TOKEN_ARC      = 4;
  private final static int TOKEN_ELLIPSE  = 5;
  private final static int TOKEN_VERTEX   = 6;
  private final static int TOKEN_SEQEND   = 7;

  private final static int TOKEN_HANDLE   = 11;
  private final static int TOKEN_POINTER  = 12;
  private final static int TOKEN_REF      = 13;
  private final static int TOKEN_ACDB     = 14;

  private abstract class DxfToken
  {
    int type = 0;
    int version;

    DxfToken( int t, int v )
    {
      type    = t;
      version = v;
    }

    abstract int write( BufferedWriter out, int version, int handle, int ref ) throws IOException;
  }

  private class NormalToken extends DxfToken
  {
    String string; // string 

    NormalToken( int t, int v, String s )
    {
      super( t, v );
      string = s;
    }

    int write( BufferedWriter out, int version, int handle, int ref ) throws IOException
    {
      if ( string != null ) out.write( string );
      return handle;
    }
  }

  private class HandleToken extends DxfToken
  {
    HandleToken() { super( TOKEN_HANDLE, 13 ); }

    int write( BufferedWriter out, int version, int handle, int ref ) throws IOException
    {
      if ( version >= this.version && handle >= 0 ) {
        handle = DXF.inc( handle );
        // DXF.writeHex( out, 5, handle );
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter(sw);
        pw.printf("  %d%s%X%s", 5, DXF.EOL, handle, DXF.EOL );
        out.write( sw.getBuffer().toString() );
      }
      return handle;
    }
  }

  private class HandlePointerToken extends DxfToken
  {
    int value = -1;

    HandlePointerToken() { super( TOKEN_POINTER, 13 ); }

    void setValue( int v ) { value = v; }

    int write( BufferedWriter out, int version, int handle, int ref ) throws IOException
    {
      if ( version >= this.version && handle >= 0 ) {
        handle = DXF.inc( handle );
        // DXF.writeHex( out, 5, handle );
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter(sw);
        pw.printf("  %d%s%X%s", 5, DXF.EOL, handle, DXF.EOL );
        if ( value > 0 ) {
          pw.printf("  %d%s%X%s", 330, DXF.EOL, value, DXF.EOL );
        }
        out.write( sw.getBuffer().toString() );
      }
      return handle;
    }
  }

  private class HandleRefToken extends DxfToken
  {
    HandleRefToken() { super( TOKEN_REF, 13 ); }

    int write( BufferedWriter out, int version, int handle, int ref ) throws IOException
    {
      if ( version >= this.version && handle >= 0 ) {
        handle = DXF.inc( handle );
        // DXF.writeHex( out, 5, handle );
        StringWriter sw = new StringWriter();
        PrintWriter pw  = new PrintWriter(sw);
        pw.printf("  %d%s%X%s", 5, DXF.EOL, handle, DXF.EOL );
        if ( ref > 0 ) {
          pw.printf("  %d%s%X%s", 330, DXF.EOL, ref, DXF.EOL );
        }
        out.write( sw.getBuffer().toString() );
      }
      return handle;
    }
  }

 
  // -------------------------------------------
  private ArrayList< DxfToken > mDxfTokens; // PRIVATE

  SymbolPointDxf( )
  {
    mDxfTokens = new ArrayList< DxfToken >();
  }

  private void addToken( DxfToken token ) { mDxfTokens.add( token ); }
    
  private void startItem( int type, String name, String pname )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DXF.printString( pw, 0, name );
    DXF.printString( pw, 8, pname );
    addToken( new NormalToken( type, 9, sw.toString() ) );
  }

  private void startLine( String pname )     { startItem( TOKEN_LINE    , "LINE",     pname ); }
  private void startPolyline( String pname ) { startItem( TOKEN_POLYLINE, "POLYLINE", pname ); }
  private void startCircle( String pname )   { startItem( TOKEN_CIRCLE,   "CIRCLE",   pname ); }
  private void startArc( String pname )      { startItem( TOKEN_ARC,      "ARC",      pname ); }
  private void startEllipse( String pname )  { startItem( TOKEN_ELLIPSE,  "ELLIPSE",  pname ); }
  private void startVertex( String pname )   { startItem( TOKEN_VERTEX,   "VERTEX",   pname ); }
    
  private void addAcDb( String acdbitem )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    // DXF.printAcDb( pw, -1, "AcDbEntity", acdbitem );
    pw.printf( DXF.EOL100 +  "AcDbEntity" + DXF.EOL + DXF.EOL100 + acdbitem + DXF.EOL );
    addToken( new NormalToken( TOKEN_ACDB, 13, sw.toString() ) );
  }

  private void addAcDbLine( )    { addAcDb( "AcDbLine" ); }
  private void addAcDbPolyline() { addAcDb( "AcDbPolyline" ); }
  private void addAcDbCircle( )  { addAcDb( "AcDbCircle" ); }
  private void addAcDbArc( )     { addAcDb( "AcDbArc" ); }
  private void addAcDbEllipse()  { addAcDb( "AcDbEllipse" ); }
  private void addAcDbVertex() 
  { 
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    pw.printf( DXF.EOL100 +  "AcDbVertex" + DXF.EOL );
    pw.printf( DXF.EOL100 + "AcDb3dPolylineVertex" + DXF.EOL );
    pw.printf( "  70" + DXF.EOL + "32" + DXF.EOL );
    addToken( new NormalToken( TOKEN_ACDB, 13, sw.toString() ) );
  }

  private void addHandle()        { addToken( new HandleToken( ) ); }
  private void addHandlePointer() { addToken( new HandlePointerToken( ) ); }
  private void addHandleRef()     { addToken( new HandleRefToken( ) ); }

  private void addLine( float x0, float y0, float x1, float y1 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DXF.printInt( pw, 62, BY_LAYER ); // color 0: by_block, 256: by_layer
    DXF.printXYZ( pw, x0, y0, 0.0f, 0 ); // prev point
    DXF.printXYZ( pw, x1, y1, 0.0f, 1 ); // current point
    addToken( new NormalToken( TOKEN_LINE, 9, sw.toString() ) );
  }

  private void addVertexData( float x0, float y0 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); 
    DXF.printXY( pw, x0, y0, 0 ); 
    addToken( new NormalToken( TOKEN_VERTEX, 9, sw.toString() ) );
  }

  private void headerPolyline( float x, float y, boolean closed )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    // DXF.printInt(  pw, 39, 1 ); // line thickness
    // DXF.printInt(  pw, 40, 1 ); // start width
    // DXF.printInt(  pw, 41, 1 ); // end width
    DXF.printInt( pw, 62, BY_LAYER ); // color 0: by_block, 256: by_layer
    DXF.printInt( pw, 66, 1 ); // group 1
    DXF.printInt( pw, 70, 8 + (closed? 1 : 0) ); // polyline flag 8 = 3D polyline, 1 = closed 
    // DXF.printInt( pw, 75, 0 ); // 6 cubic spline, 5 quad spline, 0 is the default
    DXF.printXYZ( pw, x, y, 0.0f, 0 ); // position
    addToken( new NormalToken( TOKEN_POLYLINE, 9, sw.toString() ) );
  }

  private void addVertex( String pname, float x, float y )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DXF.printString( pw, 0, "VERTEX" );
    DXF.printString( pw, 8, pname );
    DXF.printXYZ( pw, x, y, 0.0f, 0 );
    addToken( new NormalToken( TOKEN_VERTEX, 9, sw.toString() ) );
  }

  private void closeSeq()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DXF.printString( pw, 0, "SEQEND" );
    addToken( new NormalToken( TOKEN_SEQEND, 9, sw.toString() ) );
  }

  private void addCircle( float x, float y, float r )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DXF.printInt( pw, 62, BY_LAYER ); // color 0: by_block, 256: by_layer
    DXF.printXYZ( pw, x, y, 0.0f, 0 );
    DXF.printFloat( pw, 40, r );
    addToken( new NormalToken( TOKEN_CIRCLE, 9, sw.toString() ) );
  }

  private void addArcAngles( float a1, float a2 )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DXF.printFloat( pw, 50, a1 );     // ANGLES
    DXF.printFloat( pw, 51, a1+a2 );
    addToken( new NormalToken( TOKEN_ARC, 9, sw.toString() ) );
  }

  // FIXME TODO
  // @param x0,y0 left endpoint
  // @param x1,y1 right endpoint
  // @param r     aspect ratio
  // @param a1,a2 angles
  private void addEllipse( float x0, float y0, float x1, float y1, float r, float a1, float a2 ) 
  {
    float xc = (x0+x1)/2;
    float yc = (y0+y1)/2;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    DXF.printInt( pw, 62, BY_LAYER ); // color 0: by_block, 256: by_layer
    DXF.printXYZ( pw, xc, yc, 0.0f, 0 ); // CENTER
    DXF.printXYZ( pw, x0, yc, 0.0f, 1 ); // LEFT VERTEX
    DXF.printFloat( pw, 40, r  );        // ASPECT_RATIO
    DXF.printFloat( pw, 41, a1 );        // ANGLES
    DXF.printFloat( pw, 42, a1+a2 );
    // pw.printf(Locale.US,
    //           "  10\n%.2f\n  20\n%.2f\n  30\n%.2f\n  11\n%.2f\n  21\n%.2f\n  31\n%.2f\n  40\n%.2f\n  41\n%.2f\n  42\n%.2f\n",
    //           (x0+x1)/2*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                 // CENTER
    //           x1*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                        // ENDPOINT OF MAJOR AXIS
    //           (y1-y0)/(x1-x0),                                              // RATIO MINOR/MAJOR
    //           x2*TDMath.DEG2RAD, (x2+y2)*TDMath.DEG2RAD );  // START and END PARAMS
    addToken( new NormalToken( TOKEN_ELLIPSE, 9, sw.toString() ) );
  }

}

