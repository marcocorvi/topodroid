/** @file ExportDXF.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Walls DXF exporter
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.TDX.TopoGL;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Triangle3D;
import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.c3walls.cw.CWFacet;
import com.topodroid.c3walls.cw.CWPoint;
import com.topodroid.utils.TDVersion;

// import com.topodroid.utils.TDLog;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

// import java.io.PrintStream;
// import java.io.FileOutputStream;
// import java.io.BufferedOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;

// import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;


public class ExportDXF
{
  ArrayList<CWFacet> mFacets;

  ArrayList< Triangle3D > mTriangles; // powercrust triangles
  Vector3D[] mVertex; // triangle vertices
  Vector3D mMin;
  Vector3D mMax;
  double xoff, yoff, zoff; // offset to have positive coords values
  double scale;       // scale factor

  public ExportDXF()
  {
    mFacets = new ArrayList< CWFacet >();
    mTriangles = null;
    mVertex    = null; 
    resetMinMax();
  }

  private void resetMinMax()
  {
    xoff = 0;
    yoff = 0;
    zoff = 0;
    scale = 1.0f;
    mMin = new Vector3D();
    mMax = new Vector3D();
  }

  public void add( CWFacet facet ) { mFacets.add( facet ); }

  public void add( CWPoint v1, CWPoint v2, CWPoint v3 )
  {
     mFacets.add( new CWFacet( v1, v2, v3 ) );
  }

  private void makePositiveCoords()
  { 
    resetMinMax();
    for ( CWFacet facet : mFacets ) {
      facet.v1.minMax( mMin, mMax );
      facet.v2.minMax( mMin, mMax );
      facet.v3.minMax( mMin, mMax );
    }
    // Log.v( "Cave3D-DXF", "facets: " + mFacets.size() );
    if ( mVertex != null ) {
      int len = mVertex.length;
      for ( int k=0; k<len; ++k ) {
        mVertex[k].minMax( mMin, mMax );
      }
    }
    // Log.v( "Cave3D-DXF", "min " + mMin.x + " " + mMin.y + " " + mMin.z );
    // Log.v( "Cave3D-DXF", "max " + mMax.x + " " + mMax.y + " " + mMax.z );
    xoff = - mMin.x;
    yoff = - mMin.y;
    zoff = - mMin.z;
    mMax.x -= mMin.x;
    mMax.y -= mMin.y;
    mMax.z -= mMin.z;
    mMin.x = 0;
    mMin.y = 0;
    mMin.z = 0;
  }

  // ---------------------------------------------------------------------------
  // DXF stuff

  static private boolean mVersion13 = false;
  static private boolean doHandle   = true;

  static private int inc( int h ) { ++h; if ( h == 0x0105 ) ++h; return h; }

  static final private double POINT_SCALE   = 10.0; // scale of point icons: only ACAD_6
  // the next three are for text
  static final private double STATION_SCALE =  6.0 / 20; // DrawingUtil.SCALE_FIX; // scale of station names
  static final private double AXIS_SCALE    = 10.0 / 20; // DrawingUtil.SCALE_FIX; // scale of text on the axes
  static final private String zero = "0.0";
  static final private String one  = "1.0";
  static final private String two  = "2.0";
  static final private String half = "0.5";
  static final private String two_n_half = "2.5";

  // static final String ten = "10";
  static final private String empty = "";
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
  static final private String AcDbFace     = "AcDbFace";
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

  static private void writeHex( BufferedWriter out, int code, int handle ) throws IOException // mVersion13
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

  static void printString(  PrintWriter pw, int code, String name )
  {
    pw.printf("  %d%s%s%s", code, EOL, name, EOL );
  }

  static void printFloat(  PrintWriter pw, int code, double val )
  {
    pw.printf(Locale.US, "  %d%s%.2f%s", code, EOL, val, EOL );
  }

  static private void writeInt(  BufferedWriter out, int code, int val ) throws IOException
  {
    out.write( SPACE + code + EOL + val + EOL );
  }

  static private void printInt(  PrintWriter pw, int code, int val )
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

  static private void printXY( PrintWriter pw, double x, double y, int base )
  {
    pw.printf(Locale.US, "  %d%s%.4f%s  %d%s%.4f%s", base+10, EOL, x, EOL, base+20, EOL, y, EOL );
  }

  static void printXYZ( PrintWriter pw, double x, double y, double z, int base )
  {
    pw.printf(Locale.US, "  %d%s%.4f%s  %d%s%.4f%s  %d%s%.4f%s",
       base+10, EOL, x, EOL, base+20, EOL, y, EOL, base+30, EOL, z, EOL );
  }

  static void printIntXYZ( PrintWriter pw, int x, int y, int z, int base )
  {
    pw.printf(Locale.US, "  %d%s%d%s  %d%s%d%s  %d%s%d%s",
       base+10, EOL, x, EOL, base+20, EOL, y, EOL, base+30, EOL, z, EOL );
  }

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
  private int printLinePoint( PrintWriter pw, int handle, String layer, double x, double y, double z )
  {
    printString( pw, 0, "VERTEX" );
    if ( mVersion13 ) {
      handle = inc(handle);
      printAcDb( pw, handle, "AcDbVertex", "AcDb3dPolylineVertex" );
      printInt( pw, 70, 32 ); // flag 32 = 3D polyline vertex
    }
    printString( pw, 8, layer );
    printXYZ( pw, (xoff+x) * scale, (yoff+y) * scale, (zoff+z) * scale, 0 );
    return handle;
  }

  private int printLine(PrintWriter pw, int handle, String layer, double x1, double y1, double z1, double x2, double y2, double z2 )
  {
    printString( pw, 0, "LINE" );
    handle = inc(handle);
    printAcDb( pw, handle, AcDbEntity, AcDbLine );
    printString( pw, 8, layer );
    // printInt(  pw, 39, 0 );         // line thickness
    printXYZ( pw, (xoff+x1)*scale, (yoff+y1)*scale, (zoff+z1)*scale, 0 );
    printXYZ( pw, (xoff+x2)*scale, (yoff+y2)*scale, (zoff+z2)*scale, 1 );
    return handle;
  }

  private int printText( PrintWriter pw, int handle, String label,
                        double x, double y, double z, double angle, double height,
                        String layer, String style )
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
    //   printXYZ( pw, x, y, z, 0 );
    // } else {
      printString( pw, 0, "TEXT" );
      // printString( pw, 2, block );
      handle = inc(handle); printAcDb( pw, handle, AcDbEntity, AcDbText );
      printString( pw, 8, layer );
      // printString( pw, 7, style_dejavu ); // style (optional)
      // pw.printf("%s\%s 0%s", "\"10\"", EOL, EOL );
      printXYZ( pw, (xoff+x)*scale, (yoff+y)*scale, (zoff+z)*scale, 0 );
      // printXYZ( pw, 0, 0, 1, 1 );   // second alignmenmt (otional)
      // printXYZ( pw, 0, 0, 1, 200 ); // extrusion (otional 0 0 1)
      // printFloat( pw, 39, 0 );      // thickness (optional 0) 
      printFloat( pw, 40, height );    // height
      // printFloat( pw, 41, 1 );      // scale X (optional 1)
      printFloat( pw, 50, angle );     // rotation [deg]
      printFloat( pw, 51, 0 );         // oblique angle
      // printInt( pw, 71, 0 );        // text generation flag (optional 0)
      // printFloat( pw, 72, 0 );      // H-align (optional 0)
      // printFloat( pw, 73, 0 );      // V-align
      printString( pw, 1, label );
      // printString( pw, 7, style );  // style, optional (dftl STANDARD)
      printString( pw, 100, AcDbText);
    // }
    return handle;
  }

  private int printSegment( PrintWriter pw, int handle, String layer, double x1, double y1, double z1, double x2, double y2, double z2 )
  {
    //printString( pw, 0, "LINE" );
    //handle = inc(handle); printAcDb( pw, handle, AcDbEntity, AcDbLine );
    //printString( pw, 8, layer ); duplicate (HB)
    // printInt( pw, 39, 1 );         // line thickness
    handle = printLine( pw, handle, layer, x1, y1, z1, x2, y2, z2 );
    return handle;
  }

  private int printFace( PrintWriter pw, int handle, CWFacet facet, String layer )
  {
    printString( pw, 0, "3DFACE" );
    handle = inc(handle); printAcDb( pw, handle, AcDbEntity, AcDbFace );
    // handle = inc(handle); printAcDb( pw, handle, AcDbEntity );
    printString( pw, 8, layer );
    printXYZ( pw, (xoff+facet.v1.x)*scale, (yoff+facet.v1.y)*scale, (zoff+facet.v1.z)*scale, 0 );
    printXYZ( pw, (xoff+facet.v2.x)*scale, (yoff+facet.v2.y)*scale, (zoff+facet.v2.z)*scale, 1 );
    printXYZ( pw, (xoff+facet.v3.x)*scale, (yoff+facet.v3.y)*scale, (zoff+facet.v3.z)*scale, 2 );
    printXYZ( pw, (xoff+facet.v3.x)*scale, (yoff+facet.v3.y)*scale, (zoff+facet.v3.z)*scale, 3 );
    printString( pw, 70, "0" );
    //printString( pw, 100, AcDbFace); AutoCAD no (HB)

    // handle = printSegment( pw, handle, layer, facet.v1.x, facet.v1.y, facet.v1.z, facet.v2.x, facet.v2.y, facet.v2.z );
    // handle = printSegment( pw, handle, layer, facet.v2.x, facet.v2.y, facet.v2.z, facet.v3.x, facet.v3.y, facet.v3.z );
    // handle = printSegment( pw, handle, layer, facet.v3.x, facet.v3.y, facet.v3.z, facet.v1.x, facet.v1.y, facet.v1.z );

    return handle;
  }

  public boolean exportASCII( BufferedWriter osw, TglParser data, boolean b_legs, boolean b_splays, boolean b_walls, boolean version13 )
  {
    if ( data == null ) return false;
    List< Cave3DStation> stations = data.getStations();
    List< Cave3DShot>    legs     = data.getShots();
    List< Cave3DShot>    splays   = data.getSplays();

    makePositiveCoords();

    String name = "Cave3D";
    boolean ret = true;
    mVersion13 = version13; // (TDSetting.mAcadVersion >= 13);
    
    int handle = 0;

    double xmin = mMin.x ;
    double xmax = mMax.x + 2;
    double ymin = mMin.y ;
    double ymax = mMax.y + 2;
    double zmin = mMin.z ;
    double zmax = mMax.z + 2;

    int p_style = 0;

    // Log.v( "Cave3D-DXF", "DXF X " + xmin + " " + xmax + " Y " + ymin + " " + ymax );
    try {
      BufferedWriter out = new BufferedWriter( osw );

      // HEADERS
      writeComment( out, "DXF created by TopoDroid v. " + TDVersion.string() );
      writeSection( out, "HEADER" );

      // ACAD versions: 1006 (R10) 1009 (R11 R12) 1012 (R13) 1014 (R14)
      //                1015 (2000) 1018 (2004) 1021 (2007) 1024 (2010)  
      writeString( out, 9, "$ACADVER" );
      writeString( out, 1, ( version13? "AC1012" : "AC1009" ) );
      // writeString( out, 9, "$ACADMAINTVER" ); writeInt( out, 70, 105 ); // ignored
      if ( version13 ) {
        writeString( out, 9, "$HANDSEED" );    writeHex( out, 5, 0xffff );
        writeString( out, 9, "$DWGCODEPAGE" ); writeString( out, 3, "ANSI_1251" );
      }
      // writeString( out, 9, "$REQUIREDVERSIONS" ); writeInt( out, 160, 0 );

      writeString( out, 9, "$INSBASE" );
      {
        StringWriter sw1 = new StringWriter();
        PrintWriter pw1  = new PrintWriter(sw1);
        printXYZ( pw1, 0.0f, 0.0f, 0.0f, 0 ); // FIXME (0,0,0)
        printString( pw1, 9, "$EXTMIN" ); printXYZ( pw1, xmin, ymin, zmin, 0 );
        printString( pw1, 9, "$EXTMAX" ); printXYZ( pw1, xmax, ymax, zmax, 0 );
        if ( version13 ) {
          printString( pw1, 9, "$LIMMIN" ); printXY( pw1, 0.0f, 0.0f, 0 );
          printString( pw1, 9, "$LIMMAX" ); printXY( pw1, 420.0f, 297.0f, 0 );
        }
        out.write( sw1.getBuffer().toString() );
      }
      if ( version13 ) {
        writeString( out, 9, "$DIMSCALE" );    writeString( out, 40, "1.0" ); // 
        writeString( out, 9, "$DIMTXT" );      writeString( out, 40, "2.5" ); // 
        writeString( out, 9, "$LTSCALE" );     writeInt( out, 40, 1 ); // 
        writeString( out, 9, "$LIMCHECK" );    writeInt( out, 70, 0 ); // 
        writeString( out, 9, "$ORTHOMODE" );   writeInt( out, 70, 0 ); // 
        writeString( out, 9, "$FILLMODE" );    writeInt( out, 70, 1 ); // 
        writeString( out, 9, "$QTEXTMODE" );   writeInt( out, 70, 0 ); // 
        writeString( out, 9, "$REGENMODE" );   writeInt( out, 70, 1 ); // 
        //writeString( out, 9, "$MIRRMODE" );    writeInt( out, 70, 0 ); // not handled by DraftSight not AutoCAD variable (HB)
        writeString( out, 9, "$UNITMODE" );    writeInt( out, 70, 0 ); // 

        writeString( out, 9, "$TEXTSIZE" );    writeInt( out, 40, 5 ); // default text size
        writeString( out, 9, "$TEXTSTYLE" );   writeString( out, 7, standard );
        writeString( out, 9, "$CELTYPE" );     writeString( out, 6, "BYLAYER" ); // 
        writeString( out, 9, "$CELTSCALE" );   writeInt( out, 40, 1 ); // 
        writeString( out, 9, "$CECOLOR" );     writeInt( out, 62, 256 ); // 

        writeString( out, 9, "$MEASUREMENT" ); writeInt( out, 70, 1 ); // drawing units 1=metric
        writeString( out, 9, "$INSUNITS" );    writeInt( out, 70, 4 ); // defaulty draing units 0=unitless 4=mm
        writeString( out, 9, "$DIMASSOC" );    writeInt( out, 280, 0 ); // 0=no association
      }
      writeEndSection( out );
      if ( version13 ) {
        writeSection( out, "CLASSES" );
        writeEndSection( out );
      }
      writeSection( out, "TABLES" );
      {
        if ( version13 ) {
          handle = inc(handle); writeBeginTable( out, "VPORT", handle, 1 ); // 1 VPORT
          {
            writeString( out, 0, "VPORT" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbViewportTableRecord" );
            writeString( out, 2, "*Active" ); // name
            writeInt( out, 70, 0 );  // flags:
            //writeXY( out, (int)xmin, (int)ymin, (int)zmin ); // lower-left cormer //not Z point
            //writeXY( out, (int)xmax, (int)ymax, (int)zmax ); // upper-right corner //not Z point
            //writeXY( out, (int)(xmin+xmax)/2, (int)(ymin+ymax)/2, (int)(zmin+zmax)/2 ); // center point //not Z point
            writeXY( out, 0, 0, 0 );
            writeXY( out, 1, 1, 1 ); // AutoCAD always uses 0,0 1,1 (HB)
            writeXY( out, (int)(xmin+xmax)/2, (int)(ymin+ymax)/2, 2 );
            writeXY( out, 0, 0, 3 );     // snap base-point
            writeXY( out, 1, 1, 4 );   // snap-spacing
            writeXY( out, 1, 1, 5 );   // grid-spacing
            writeXYZ( out, 0, 0, 1, 6 ); // view direction
            writeXYZ( out, 0, 0, 0, 7 ); // view tangent
            
            writeInt( out, 40, 297 ); // Float.toString( (xmin+xmax)/2 ) );
            writeInt( out, 41, 2 );   // Float.toString( (ymin+ymax)/2 ) );
            writeInt( out, 42, 50 );  // Float.toString( (zmin+zmax)/2 ) );
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
            //writeString( out, 141, zero );
            //writeString( out, 142, zero );
            //writeInt( out, 63, 250 );
            //writeString( out, 361, "6D" );
          }
          writeEndTable( out );
        }

        if ( version13 ) {
          int nr_styles = 2;
          handle = inc(handle); writeBeginTable( out, "STYLE", handle, nr_styles );  // 2 styles
          {
            writeString( out, 0, "STYLE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbTextStyleTableRecord" );
            writeString( out, 2, standard );  // name
            writeInt( out, 70, 0 );           // flag (1: shape, 4:vert text, ... )
            writeString( out, 40, zero );     // text-height: not fixed
            writeString( out, 41, one  );
            writeString( out, 50, zero  );
            writeInt( out, 71, 0 );
            writeString( out, 42, two_n_half  );
            writeString( out, 3, "txt" );  // fonts
            writeString( out, 4, empty );

            writeString( out, 0, "STYLE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbTextStyleTableRecord" );
            p_style = handle;
            writeString( out, 2, style_dejavu );  // name
            writeInt( out, 70, 0 );               // flag
            writeString( out, 40, zero );
            writeString( out, 41, one  );
            writeString( out, 50, zero  );
            writeInt( out, 71, 0 );
            writeString( out, 42, two_n_half  );
            writeString( out, 3, "Sans Serif.ttf" );  // fonts
            writeString( out, 4, empty );
            writeString( out, 1001, "ACAD" );
            writeString( out, 1000, "DejaVu Sans" );
            writeInt( out, 1071, 0 );
          }
          writeEndTable( out );
        }

        if ( version13 ) { handle = inc(handle); } else { handle = 5; }
        int ltypeowner = handle;
        int ltypenr    = version13 ? 5 : 1; // linetype number
        writeBeginTable( out, "LTYPE", handle, ltypenr ); 
        writeInt( out, 330, 0 ); // table has no owner
        {
          // int flag = 64;
          if ( version13 ) {
            writeString( out, 0, "LTYPE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
            writeString( out, 2, lt_byBlock );
            writeInt( out, 330, ltypeowner );
            writeInt( out, 70, 0 );
            writeString( out, 3, "Std by block" );
            writeInt( out, 72, 65 );
            writeInt( out, 73, 0 );
            writeString( out, 40, zero );

            writeString( out, 0, "LTYPE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
            writeString( out, 2, lt_byLayer );
            writeInt( out, 330, ltypeowner );
            writeInt( out, 70, 0 );
            writeString( out, 3, "Std by layer" );
            writeInt( out, 72, 65 );
            writeInt( out, 73, 0 );
            writeString( out, 40, zero );

            writeString( out, 0, "LTYPE" );
            handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
            writeString( out, 2, lt_continuous );
            writeInt( out, 330, ltypeowner );
            writeInt( out, 70, 0 );
            writeString( out, 3, "Solid line ------" );
            writeInt( out, 72, 65 );
            writeInt( out, 73, 0 );
            writeString( out, 40, zero );

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
            writeString( out, 40, one ); // pattern length
            writeString( out, 49, half );  writeInt( out, 74, 0 ); // segment
            writeString( out, 49, "-0.2" ); writeInt( out, 74, 2 ); // embedded text
              writeInt( out, 75, 0 );   // SHAPE number must be 0
              writeInt( out, 340, p_style );  // STYLE pointer FIXME
              writeString( out, 46, "0.1" );  // scale
              writeString( out, 50, zero );   // rotation
              writeString( out, 44, "-0.1" ); // X offset
              writeString( out, 45, "-0.1" ); // Y offset
              writeString( out, 9, "|" ); // text
            writeString( out, 49, "-0.25" ); writeInt( out, 74, 0 ); // gap

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
            //   writeString( out, 50, zero );   // rotation
            //   writeString( out, 44, "-0.1" ); // X offset
            //   writeString( out, 45, zero );   // Y offset
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
            writeString( out, 40, zero );
          }
        }
        writeEndTable( out );
        int nr_layers = 7;

        nr_layers = 5;
        if ( version13 ) { handle = inc(handle); } else { handle = 2; }
        writeBeginTable( out, "LAYER", handle, nr_layers );
        {
          StringWriter sw2 = new StringWriter();
          PrintWriter pw2  = new PrintWriter(sw2);

          // 2 layer name, 70 flag (64), 62 color code, 6 line type
          int flag = 0;
          int color = 1;
          // if ( ! version13 ) { handle = 40; }
          // handle = inc(handle); printLayer( pw2, handle, "0",       flag, 0, lt_continuous ); // LAYER "0" .. FIXME DraftSight
          handle = inc(handle); printLayer( pw2, handle, "0",       flag, 7, lt_continuous ); // LAYER "0" must be AutoCAD white (HB)
          handle = inc(handle); printLayer( pw2, handle, "LEG",     flag, color, lt_continuous ); ++color; // red
          handle = inc(handle); printLayer( pw2, handle, "SPLAY",   flag, color, lt_continuous ); ++color; // yellow
          handle = inc(handle); printLayer( pw2, handle, "STATION", flag, color, lt_continuous ); ++color; // green
          handle = inc(handle); printLayer( pw2, handle, "FACE",    flag, color, lt_continuous ); ++color; // cyan
          handle = inc(handle); printLayer( pw2, handle, "REF",     flag, color, lt_continuous ); ++color; // white
          
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
          if ( version13 ) { handle = inc(handle); } else { handle = 12; }
          writeAcDb( out, handle, AcDbSymbolTR, "AcDbRegAppTableRecord" );
          writeString( out, 2, "ACAD" ); // applic. name
          writeInt( out, 70, 0 );        // flag
        }
        writeEndTable( out );

        if ( version13 ) {
          handle = inc(handle); writeBeginTable( out, "DIMSTYLE", handle, 1 );
          // writeString( out, 100, "AcDbDimStyleTable" );
          // writeInt( out, 71, 0 ); // DIMTOL
          {
            writeString( out, 0, "DIMSTYLE" );
            handle = inc(handle);
            writeHex( out, 105, handle ); 
            writeAcDb( out, -1, AcDbSymbolTR, "AcDbDimStyleTableRecord" );
            writeString( out, 2, standard );
            writeString( out, 3, empty );
            writeString( out, 4, empty );
            writeString( out, 5, empty );
            writeString( out, 6, empty );
            writeString( out, 7, empty );
            writeString( out, 40, one );
            writeString( out, 41, two_n_half );
            writeString( out, 42, "0.625" );
            writeString( out, 43, "3.75" );
            writeString( out, 44, "1.25" );
            writeString( out, 45, zero );
            writeString( out, 46, zero );
            writeString( out, 47, zero );
            writeString( out, 48, zero );
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
            writeString( out, 142, zero );
            writeString( out, 143, "0.04" );
            writeString( out, 144, one );
            writeString( out, 145, zero );
            writeString( out, 146, one );
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
          writeBeginTable( out, "BLOCK_RECORD", handle, 0 );
             writeString( out, 0, "BLOCK_RECORD" );  // must be AutoCAD (HB)
             handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbBlockTableRecord" );
             writeString( out, 2, "*Model_Space" );
             writeInt( out, 70, 0 );
             writeInt( out, 280, 1 );
             writeInt( out, 281, 0 );
             writeInt( out, 330, 1 );
             writeString( out, 0, "BLOCK_RECORD" );
             handle = inc(handle); writeAcDb( out, handle, AcDbSymbolTR, "AcDbBlockTableRecord" );
             writeString( out, 2, "*Paper_Space" );
             writeInt( out, 70, 0 );
             writeInt( out, 280, 1 );
             writeInt( out, 281, 0 );
             writeInt( out, 330, 1 );	
          writeEndTable( out );
        }
      }
      writeEndSection( out );
      out.flush();
      
      writeSection( out, "BLOCKS" );
        writeString( out, 0, "BLOCK" );  // must be AutoCAD (HB)
        handle = inc(handle); writeAcDb( out, handle, AcDbEntity, "AcDbBlockBegin" );
        writeString( out, 8, "0" );
        writeString( out, 2, "*Model_Space" );
        writeInt( out, 70, 0 );       // flag 0=none, 1=anonymous, 2=non-conts attr, 4=xref, 8=xref overlay,
        writeInt( out, 10, 0 ); 
        writeInt( out, 20, 0 ); 
        writeInt( out, 30, 0 ); 
        writeString( out, 3, "*Model_Space" );
        writeString( out, 1, "" );
        writeString( out, 0, "ENDBLK" );
        handle = inc(handle); writeAcDb( out, handle, AcDbEntity, "AcDbBlockEnd");
        writeString( out, 8, "0");
        writeString( out, 0, "BLOCK" );
        handle = inc(handle); writeAcDb( out, handle, AcDbEntity, "AcDbBlockBegin" );
        writeString( out, 8, "0" );
        writeString( out, 2, "*Paper_Space" );
        writeInt( out, 70, 0 );       // flag 0=none, 1=anonymous, 2=non-conts attr, 4=xref, 8=xref overlay,
        writeInt( out, 10, 0 ); 
        writeInt( out, 20, 0 ); 
        writeInt( out, 30, 0 ); 
        writeString( out, 3, "*Paper_Space" );
        writeString( out, 1, "" );
        writeString( out, 0, "ENDBLK" );
        handle = inc(handle); writeAcDb( out, handle, AcDbEntity, "AcDbBlockEnd");
        writeString( out, 8, "0");	    
      writeEndSection( out );
      out.flush();

      writeSection( out, "ENTITIES" );
      {
        String scale_len = "20";
        double sc1 = 20; // DrawingUtil.SCALE_FIX / 2 = 10;

        // REFERENCE
        StringWriter sw9 = new StringWriter();
        PrintWriter pw9  = new PrintWriter(sw9);
        double sc2 = sc1 / 2;
        if ( false ) {      //0.0 is not good and should be made optional (HB)
          handle = printLine( pw9, handle, "REF", xmin,     ymin,     zmin, xmin+sc1,  ymin,      zmin );
          handle = printLine( pw9, handle, "REF", xmin,     ymin,     zmin, xmin,      ymin+sc1,  zmin );
          handle = printLine( pw9, handle, "REF", xmin,     ymin,     zmin, xmin,      ymin,      zmin+sc1 );
          handle = printLine( pw9, handle, "REF", xmin+sc2, ymin,     zmin, xmin+sc2,  ymin+0.5f, zmin ); // 10 m ticks
          handle = printLine( pw9, handle, "REF", xmin,     ymin+sc2, zmin, xmin+0.5f, ymin+sc2,  zmin );
          handle = printLine( pw9, handle, "REF", xmin,     ymin,     zmin+sc2, xmin+0.5f, ymin,  zmin+sc2 );
          handle = printLine( pw9, handle, "REF", xmin,     ymin,     zmin+sc2, xmin,  ymin+0.5f, zmin+sc2 );
          handle = printLine( pw9, handle, "REF", xmin+sc1, ymin,     zmin, xmin+sc1,  ymin+0.5f, zmin ); // 20 m ticks
          handle = printLine( pw9, handle, "REF", xmin,     ymin+sc1, zmin, xmin+0.5f, ymin+sc1,  zmin );
          handle = printLine( pw9, handle, "REF", xmin,     ymin,     zmin+sc1, xmin+0.5f, ymin,  zmin+sc1 );
          handle = printLine( pw9, handle, "REF", xmin,     ymin,     zmin+sc1, xmin,      ymin+0.5f,  zmin+sc1 );
        
        // out.write( sw9.getBuffer().toString() );
        
        // printString( pw9, 0, "LINE" );
        // handle = inc(handle);
        // printAcDb( pw9, handle, AcDbEntity, AcDbLine );
        // printString( pw9, 8, "REF" );
        // // printInt(  pw9, 39, 0 );         // line thickness
        // printXYZ( pw9, xmin,       ymin, 0.0f, 0 );
        // printXYZ( pw9, (xmin+sc1), ymin, 0.0f, 1 );
        // out.write( sw9.getBuffer().toString() );

        // StringWriter sw8 = new StringWriter();
        // PrintWriter pw8  = new PrintWriter(sw8);
        // printString( pw8, 0, "LINE" );
        // handle = inc(handle);
        // printAcDb( pw8, handle, AcDbEntity, AcDbLine );
        // printString( pw8, 8, "REF" );
        // // printInt(  pw8, 39, 0 );         // line thickness
        // printXYZ( pw8, xmin, ymin, 0.0f, 0 );
        // printXYZ( pw8, xmin, ymin+sc1, 0.0f, 1 );
        // out.write( sw8.getBuffer().toString() );
        // out.flush();
        
        // offset axes legends by 1
        // StringWriter sw7 = new StringWriter();
        // PrintWriter pw7  = new PrintWriter(sw7);
          handle = printText( pw9, handle, scale_len, xmin+sc1, ymin+1,   zmin+1,   0, AXIS_SCALE, "REF", style_dejavu );
          handle = printText( pw9, handle, scale_len, xmin+1,   ymin+sc1, zmin+1,   0, AXIS_SCALE, "REF", style_dejavu );
          handle = printText( pw9, handle, scale_len, xmin+1,   ymin+1,   zmin+sc1, 0, AXIS_SCALE, "REF", style_dejavu );
          out.write( sw9.getBuffer().toString() );
          out.flush();
        }
        // FACETS
        if ( b_walls ) {
          StringWriter sw10 = new StringWriter();
          PrintWriter pw10  = new PrintWriter(sw10);
          for ( CWFacet facet : mFacets ) {
            handle = printFace( pw10, handle, facet, "FACE" );
          }
          out.write( sw10.getBuffer().toString() );
          out.flush();

          // if ( mTriangles != null ) {
          //   for ( Triangle3D t : mTriangles ) {
          //     int size = t.size;
          //     Vector3D n = t.normal;
          //     pw.format(Locale.US, "  facet normal %.3f %.3f %.3f\n", n.x, n.y, n.z );
          //     pw.format(Locale.US, "    outer loop\n");
          //     for ( int k=0; k<size; ++k ) {
          //       Vector3D v = t.vertex[k];
          //       pw.format(Locale.US, "      vertex %.3f %.3f %.3f\n", (x+v.x)*s, (y+v.y)*s, (z+v.z)*s );
          //     }
          //     pw.format(Locale.US, "    endloop\n");
          //     pw.format(Locale.US, "  endfacet\n");
          //   }
          // }
        }

        // centerline data
        // if ( b_legs ) 
        {
          StringWriter sw4 = new StringWriter();
          PrintWriter pw4  = new PrintWriter(sw4);
          for ( Cave3DShot blk : legs ) {
            Cave3DStation fs = blk.from_station;
            Cave3DStation ts = blk.to_station;
            if ( fs == null || ts == null ) continue;
            handle = printSegment( pw4, handle, "LEG", fs.x, fs.y, fs.z, ts.x, ts.y, ts.z );
          }
          out.write( sw4.getBuffer().toString() );
          out.flush();
        }

        if ( b_splays ) {
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          for ( Cave3DShot blk : splays ) {
            Cave3DStation fs = blk.from_station;
            Vector3D  ts = blk.toPoint3D();
            if ( fs == null || ts == null ) continue;
            handle = printSegment( pw5, handle, "SPLAY", fs.x, fs.y, fs.z, ts.x, ts.y, ts.z );
          }
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }

        // STATIONS

      }
      writeEndSection( out );
      // if ( version13 ) {
      handle = writeSectionObjects( out, handle );  // must be AutoCAD (HB)
      // }
      writeString( out, 0, "EOF" );
      out.flush();
      osw.close();
    } catch ( IOException e ) {
      return false;
    }
    return true;
  }

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
    int saved2 = handle = inc(handle); printHex( pwx, 350, handle );

    printString( pwx, 0, "DICTIONARY" );
    printHex( pwx, 330, saved );
    printAcDb( pwx, saved2, AcDbDictionary );
    // printInt( pwx, 280, 0 );
    printInt( pwx, 281, 1 );
   
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
      printString( pwx, 3, empty );
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
      printString( pwx, 1, empty );
      printString( pwx, 2, "" );
      printString( pwx, 4, empty );
      printString( pwx, 6, empty );
      printZero( pwx, 40, 50 );
      printInt( pwx, 140, 0 );
      printInt( pwx, 141, 0 );
      printInt( pwx, 142, 1 );
      printInt( pwx, 143, 1 );
      printInt( pwx, 70, 688 );
      printInt( pwx, 72, 0 );
      printInt( pwx, 73, 0 );
      printInt( pwx, 74, 5 );
      printString( pwx, 7, empty );
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
      printString( pwx, 1, empty );
      printString( pwx, 2, "" );
      printString( pwx, 4, empty );
      printString( pwx, 6, empty );
      printZero( pwx, 40, 50 );
      printInt( pwx, 140, 0 );
      printInt( pwx, 141, 0 );
      printInt( pwx, 142, 1 );
      printInt( pwx, 143, 1 );
      printInt( pwx, 70, 1712 );
      printInt( pwx, 72, 0 );
      printInt( pwx, 73, 0 );
      printInt( pwx, 74, 0 );
      printString( pwx, 7, empty );
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
      printString( pwx, 1, empty );
      printString( pwx, 2, "" );
      printString( pwx, 4, empty );
      printString( pwx, 6, empty );
      printZero( pwx, 40, 50 );
      printInt( pwx, 140, 0 );
      printInt( pwx, 141, 0 );
      printInt( pwx, 142, 1 );
      printInt( pwx, 143, 1 );
      printInt( pwx, 70, 688 );
      printInt( pwx, 72, 0 );
      printInt( pwx, 73, 0 );
      printInt( pwx, 74, 5 );
      printString( pwx, 7, empty );
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

