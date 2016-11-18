/** @file DrawingDxf.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid drawing: dxf export
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


import java.util.Locale;

import java.util.ArrayList;
import java.util.HashMap;

// import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import android.util.Log;

class DrawingDxf
{
  private static final float grad2rad = TDMath.GRAD2RAD;
  private static boolean mVersion13 = false;

  static final float POINT_SCALE = 10.0f;
  static final float LABEL_SCALE = 3.0f;
  static final String zero = "0.0";
  static final String one  = "1.0";
  // static final String half = "0.5";
  static final String ten  = "10";
  static final String my_style = "MyStyle";
  static final String standard = "Standard";
  static final String lt_continuous = "Continuous";
  static final String lt_byBlock = "ByBlock";
  
  static final String AcDbSymbolTR = "AcDbSymbolTableRecord";
  static final String AcDbEntity   = "AcDbEntity";
  static final String AcDbText     = "AcDbText";
  static final String AcDbLine     = "AcDbLine";
  static final String AcDbPolyline = "AcDbPolyline";
 

  static void writeComment( BufferedWriter out, String comment ) throws IOException
  {
    out.write( "  999\n" + comment + "\n" );
  }

  static void writeHex( BufferedWriter out, int code, int handle ) throws IOException // mVersion13
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.printf("  %d\n%X\n", code, handle );
    out.write( sw.getBuffer().toString() );
  }

  static void printHex( PrintWriter pw, int code, int handle ) // mVersion13
  {
    pw.printf("  %d\n%X\n", code, handle );
  }

  static void writeAcDb( BufferedWriter out, int hex, String acdb1 ) throws IOException // mVersion13
  {
    if ( hex >= 0 ) writeHex( out, 5, hex );
    out.write("  100\n" + acdb1 + "\n" );
  }

  static void writeAcDb( BufferedWriter out, int hex, String acdb1, String acdb2 ) throws IOException // mVersion13
  {
    if ( hex >= 0 ) writeHex( out, 5, hex );
    out.write("  100\n" + acdb1 + "\n  100\n" + acdb2 + "\n" );
  }


  static void printAcDb( PrintWriter pw, int hex, String acdb1 ) // mVersion13
  {
    if ( hex >= 0 ) printHex( pw, 5, hex );
    pw.printf("  100\n" + acdb1 + "\n" );
  }

  static void printAcDb( PrintWriter pw, int hex, String acdb1, String acdb2 ) // mVersion13
  {
    if ( hex >= 0 ) printHex( pw, 5, hex );
    pw.printf("  100\n" + acdb1 + "\n  100\n" + acdb2 + "\n" );
  }

  static void writeString(  BufferedWriter out, int code, String name ) throws IOException
  {
    out.write( "  " + code + "\n" + name + "\n" );
  }

  static void printString(  PrintWriter pw, int code, String name )
  {
    pw.printf("  %d\n%s\n", code, name );
  }

  static void printFloat(  PrintWriter pw, int code, float val )
  {
    pw.printf(Locale.US, "  %d\n%.2f\n", code, val );
  }

  static void writeInt(  BufferedWriter out, int code, int val ) throws IOException
  {
    out.write( "  " + code + "\n" + val + "\n" );
  }

  static void printInt(  PrintWriter pw, int code, int val )
  {
    pw.printf( "  %d\n%d\n", code, val );
  }

  static void writeXY( BufferedWriter out, int x, int y, int base ) throws IOException
  {
    int b10 = 10 + base;
    int b20 = 20 + base;
    out.write( "  " + b10 + "\n" + x + "\n  " + b20 + "\n" + y + "\n" );
  }

  static void writeXYZ( BufferedWriter out, int x, int y, int z, int base ) throws IOException
  {
    int b10 = 10 + base;
    int b20 = 20 + base;
    int b30 = 30 + base;
    out.write( "  " + b10 + "\n" + x + "\n  " + b20 + "\n" + y + "\n  " + b30 + "\n" + z + "\n" );
  }

  static void printXY( PrintWriter pw, float x, float y, int base )
  {
    pw.printf(Locale.US, "  %d\n%.2f\n  %d\n%.2f\n", base+10, x, base+20, y );
  }

  static void printXYZ( PrintWriter pw, float x, float y, float z, int base )
  {
    pw.printf(Locale.US, "  %d\n%.2f\n  %d\n%.2f\n  %d\n%.2f\n", base+10, x, base+20, y, base+30, z );
  }

  static void printIntXYZ( PrintWriter pw, int x, int y, int z, int base )
  {
    pw.printf(Locale.US, "  %d\n%d\n  %d\n%d\n  %d\n%d\n", base+10, x, base+20, y, base+30, z );
  }

  // -----------------------------------------

  static void writeSection( BufferedWriter out, String name ) throws IOException
  {
    writeString(out, 0, "SECTION");
    writeString(out, 2, name );
  }

  static void writeEndSection( BufferedWriter out ) throws IOException
  {
    writeString(out, 0, "ENDSEC" );
  }

  static void writeBeginTable(  BufferedWriter out, String name, int handle, int num ) throws IOException
  {
    writeString(out, 0, "TABLE" );
    writeString(out, 2, name );
    writeAcDb( out, handle, "AcDbSymbolTable" );
    if ( num >= 0 ) writeInt(out, 70, num );
  }
  
  static void writeEndTable(  BufferedWriter out ) throws IOException
  {
    writeString( out, 0, "ENDTAB");
  }

  static void printLayer( PrintWriter pw2, int handle, String name, int flag, int color, String linetype )
  {
    name = name.replace(":", "-");
    printString( pw2, 0, "LAYER" );
    printAcDb( pw2, handle, AcDbSymbolTR, "AcDbLayerTableRecord");
    printString( pw2, 2, name );  // layer name
    printInt( pw2, 70, flag );    // layer flag
    printInt( pw2, 62, color );   // layer color
    printString( pw2, 6, linetype ); // linetype name
    printInt( pw2, 370, -3 );
    printString( pw2, 390, "F" );
    printInt( pw2, 347, 46 );
    printInt( pw2, 348, 0 );
  }


  static void printEndText( PrintWriter pw, String style )
  {
    printString( pw, 7, style );
    printString( pw, 100, AcDbText );
  }

  static int  printPolyline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, String layer, boolean closed )
  {
     int close = (closed ? 1 : 0 );
     printString( pw, 0, "POLYLINE" );
     ++handle; printAcDb( pw, handle, AcDbEntity, AcDbPolyline );
     printString( pw, 8, layer );
     // printInt(  pw, 39, 1 ); // line thickness
     // printInt(  pw, 40, 1 ); // start width
     // printInt(  pw, 41, 1 ); // end width
     printInt( pw, 66, 1 ); // group 1
     printInt( pw, 70, 8 + close ); // polyline flag 8 = 3D polyline, 1 = closed 
     printInt( pw, 75, 0 ); // 6 cubic spline, 5 quad spline, 0
     for (LinePoint p = line.mFirst; p != null; p = p.mNext ) { 
       printString( pw, 0, "VERTEX" );
       // if ( mVersion13 ) {
         ++handle; printAcDb( pw, handle, "AcDbVertex", "AcDb3dPolylineVertex" );
         printInt( pw, 70, 32 ); // flag 32 = 3D polyline vertex
       // }
       printString( pw, 8, layer );
       printXYZ( pw, p.mX * scale, -p.mY * scale, 0.0f, 0 );
     }
     pw.printf("  0\nSEQEND\n");
     // if ( mVersion13 ) {
       ++handle; printHex( pw, 5, handle );
     // }
     return handle;
  }

  static int printLWPolyline( PrintWriter pw, DrawingPointLinePath line, float scale, int handle, String layer, boolean closed )
  {
     int close = (closed ? 1 : 0 );
     printString( pw, 0, "LWPOLYLINE" );
     ++handle; printAcDb( pw, handle, AcDbEntity, AcDbPolyline );
     printString( pw, 8, layer );
     printInt( pw, 38, 0 ); // elevation
     printInt( pw, 39, 5 ); // thickness
     printInt( pw, 43, 5 ); // start width
     printInt( pw, 70, close ); // not closed
     printInt( pw, 90, line.size() ); // nr. of points
     for (LinePoint p = line.mFirst; p != null; p = p.mNext ) { 
       printXY( pw, p.mX * scale, -p.mY * scale, 0 );
     }
     return handle;
  }

  static void write( BufferedWriter out, DistoXNum num, DrawingCommandManager plot, long type )
  {
    mVersion13 = (TDSetting.mAcadVersion >= 13);
    
    float scale = TDSetting.mDxfScale;
    int handle = 0;
    float xmin=10000f, xmax=-10000f, 
          ymin=10000f, ymax=-10000f;
    // compute BBox
    for ( ICanvasCommand cmd : plot.getCommands() ) {
      if ( cmd.commandType() != 0 ) continue;
      DrawingPath p = (DrawingPath)cmd;

      if ( p.mType == DrawingPath.DRAWING_PATH_LINE ) {
        DrawingLinePath lp = (DrawingLinePath)p;
        if ( lp.lineType() == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
          // ArrayList< LinePoint > pts = lp.mPoints;
          // for ( LinePoint pt : pts ) 
          for ( LinePoint pt = lp.mFirst; pt != null; pt = pt.mNext ) {
            if ( pt.mX < xmin ) xmin = pt.mX;
            if ( pt.mX > xmax ) xmax = pt.mX;
            if ( pt.mY < ymin ) ymin = pt.mY;
            if ( pt.mY > ymax ) ymax = pt.mY;
          }
        }
      } else if ( p.mType == DrawingPath.DRAWING_PATH_POINT ) {
        DrawingPointPath pp = (DrawingPointPath)p;
        if ( pp.cx < xmin ) xmin = pp.cx;
        if ( pp.cx > xmax ) xmax = pp.cx;
        if ( pp.cy < ymin ) ymin = pp.cy;
        if ( pp.cy > ymax ) ymax = pp.cy;
      } else if ( p.mType == DrawingPath.DRAWING_PATH_STATION ) {
        DrawingStationPath st = (DrawingStationPath)p;
        if ( st.cx < xmin ) xmin = st.cx;
        if ( st.cx > xmax ) xmax = st.cx;
        if ( st.cy < ymin ) ymin = st.cy;
        if ( st.cy > ymax ) ymax = st.cy;
      }
    }
    xmin *= scale;
    xmax *= scale;
    ymin *= scale;
    ymax *= scale;

    // Log.v("DistoX", "DXF X " + xmin + " " + xmax + " Y " + ymin + " " + ymax );

    try {
      // header
      writeComment( out, "DXF created by TopoDroid v. " + TopoDroidApp.VERSION );
      writeSection( out, "HEADER" );

      xmin -= 2f;
      ymax += 2f;

      // ACAD versions: 1006 (R10) 1009 (R11 R12) 1012 (R13) 1014 (R14)
      //                1015 (2000) 1018 (2004) 1021 (2007) 1024 (2010)  
      writeString( out, 9, "$ACADVER" );
      writeString( out, 1, ( mVersion13? "AC1009" : "AC1009" ) );
      // writeString( out, 9, "$ACADMAINTVER" ); // ignored
      // writeInt( out, 70, 105 );

      if ( mVersion13 ) { // codepage
        writeString( out, 9, "$DWGCODEPAGE" ); writeString( out, 3, "ANSI_1251" );
      }
      // writeString( out, 9, "$REQUIREDVERSIONS" ); writeInt( out, 160, 0 );

      writeString( out, 9, "$INSBASE" );
      {
        StringWriter sw1 = new StringWriter();
        PrintWriter pw1  = new PrintWriter(sw1);
        printXYZ( pw1, 0.0f, 0.0f, 0.0f, 0 ); // FIXME (0,0,0)
        printString( pw1, 9, "$EXTMIN" );
        printXYZ( pw1, xmin, -ymax, 0.0f, 0 );
        printString( pw1, 9, "$EXTMAX" );
        printXYZ( pw1, xmax, -ymin, 0.0f, 0 );
        out.write( sw1.getBuffer().toString() );
      }
      writeString( out, 9, "$TEXTSIZE" );    writeInt( out, 40, 5 ); // default text size
      writeString( out, 9, "$TEXTSTYLE" );   writeString( out, 7, standard );
      // writeString( out, 9, "$CELTYPE" );     writeString( out, 6, "ByLayer" ); // 
      // writeString( out, 9, "$CECOLOR" );     writeInt( out, 62, 256 ); // 
      // writeString( out, 9, "$CELSCALE" );    writeString( out, 6, "1.0" ); // 

      writeString( out, 9, "$UNITMODE" );    writeInt( out, 70, 0 ); // 
      writeString( out, 9, "$MEASUREMENT" ); writeInt( out, 70, 1 ); // drawing units 1=metric
      writeString( out, 9, "$INSUNITS" );    writeInt( out, 70, 4 ); // defaulty draing units 0=unitless 4=mm
      writeString( out, 9, "$DIMASSOC" );    writeInt( out, 280, 0 ); // 0=no association
      
      writeEndSection( out );
      
      writeSection( out, "TABLES" );
      {
        if ( mVersion13 ) {
          ++handle; writeBeginTable( out, "VPORT", handle, 0 );
          {
            writeString( out, 0, "VPORT" );
            ++handle; writeAcDb( out, handle, AcDbSymbolTR, "AcDbViewportTableRecord" );
            writeString( out, 2, "*Active" ); // name
            writeInt( out, 70, 0 );
            writeXY( out, 0, 0, 0 ); // 10 0 20 0 1
            writeXY( out, 1, 1, 1 ); // 11 1 21 1
            // writeXY( out, 0, 0, 2 ); // 12 0 22 0 
            writeString( out, 12, Float.toString( (xmin + xmax)/2 ) );
            writeString( out, 22, Float.toString( (ymin + ymax)/2 ) );
            writeXY( out, 0, 0, 3 );     // 13 0 23 0 
            writeXY( out, 10, 10, 4 );   // 14 10 24 10 
            writeXY( out, 10, 10, 5 );   // 15 10 25 10 
            writeXYZ( out, 0, 0, 1, 6 ); // 16 0 26 0 36 1
            writeXYZ( out, 0, 0, 0, 7 ); // 17 0 27 0 37 0
            writeString( out, 40, Float.toString( (xmin+xmax)/2 ) );
            writeString( out, 41, Float.toString( (ymin+ymax)/2 ) );
            writeString( out, 42, "50.0" );
            // writeString( out, 43, zero );
            // writeString( out, 44, zero );
            // writeString( out, 50, zero );
            // writeString( out, 51, zero );
            // writeInt( out, 71, 0 );
            // writeInt( out, 72, 100 );
            // writeInt( out, 73, 1 );
            // writeInt( out, 74, 3 );
            // writeInt( out, 75, 0 );
            // writeInt( out, 76, 0 );
            // writeInt( out, 77, 0 );
            // writeInt( out, 78, 0 );
            // writeInt( out, 281, 0 );
            // writeInt( out, 65, 1 );
            // writeXYZ( out, 0, 0, 0, 100 );  // UCS origin
            // writeXYZ( out, 1, 0, 0, 101 );  // UCS X-axis
            // writeXYZ( out, 0, 1, 0, 102 );  // UCS Y-axis
            // writeInt( out, 79, 0 );
            // writeString( out, 146, zero );
            // writeString( out, 348, "2F" );
            // writeInt( out, 60, 3 );
            // writeInt( out, 61, 5 );
            // writeInt( out, 292, 1 );
            // writeInt( out, 282, 1 );
            // writeString( out, 141, zero );
            // writeString( out, 142, zero );
            // writeInt( out, 63, 250 );
            // writeString( out, 361, "6D" );
          }
          writeEndTable( out );
        }

        ++handle; writeBeginTable( out, "LTYPE", handle, 2 ); // 2 linetypes
        {
          // int flag = 64;
          writeString( out, 0, "LTYPE" );
          ++handle; writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
          writeString( out, 2, lt_byBlock );
          writeInt( out, 70, 0 );
          writeString( out, 3, "" );
          writeInt( out, 72, 65 );
          writeInt( out, 73, 0 );
          writeString( out, 40, zero );

          writeString( out, 0, "LTYPE" );
          ++handle; writeAcDb( out, handle, AcDbSymbolTR, "AcDbLinetypeTableRecord" );
          writeString( out, 2, lt_continuous );
          writeInt( out, 70, 0 );
          writeString( out, 3, "Solid line" );
          writeInt( out, 72, 65 );
          writeInt( out, 73, 0 );
          writeString( out, 40, zero );

        }
        writeEndTable( out );

        SymbolLineLibrary linelib = DrawingBrushPaths.mLineLib;
        SymbolAreaLibrary arealib = DrawingBrushPaths.mAreaLib;
        int nr_layers = 7 + linelib.mSymbolNr + arealib.mSymbolNr;
        ++handle; writeBeginTable( out, "LAYER", handle, nr_layers );
        {
          StringWriter sw2 = new StringWriter();
          PrintWriter pw2  = new PrintWriter(sw2);

          // 2 layer name, 70 flag (64), 62 color code, 6 line type
          int flag = 0;
          int color = 1;
          ++handle; printLayer( pw2, handle, "LEG",     flag, color, lt_continuous ); ++color;
          ++handle; printLayer( pw2, handle, "SPLAY",   flag, color, lt_continuous ); ++color;
          ++handle; printLayer( pw2, handle, "STATION", flag, color, lt_continuous ); ++color;
          ++handle; printLayer( pw2, handle, "LINE",    flag, color, lt_continuous ); ++color;
          ++handle; printLayer( pw2, handle, "POINT",   flag, color, lt_continuous ); ++color;
          ++handle; printLayer( pw2, handle, "AREA",    flag, color, lt_continuous ); ++color;
          ++handle; printLayer( pw2, handle, "REF",     flag, color, lt_continuous ); ++color;
          
          if ( linelib != null ) { 
            for ( Symbol line : linelib.getSymbols() ) {
              String lname = "L_" + line.getThName().replace(':','-');
              ++handle; printLayer( pw2, handle, lname, flag, color, lt_continuous ); ++color;
            }
          }

          if ( arealib != null ) {
            for ( Symbol s : arealib.getSymbols() ) {
              String aname = "A_" + s.getThName().replace(':','-');
              ++handle; printLayer( pw2, handle, aname, flag, color, lt_continuous ); ++color;
            }
          }
          out.write( sw2.getBuffer().toString() );
        }
        writeEndTable( out );

        if ( mVersion13 ) {
          ++handle; writeBeginTable( out, "STYLE", handle, 1 );  // 1 style
          {
            writeString( out, 0, "STYLE" );
            ++handle; writeAcDb( out, handle, AcDbSymbolTR, "AcDbTextStyleTableRecord" );
            writeString( out, 2, my_style );  // name
            writeInt( out, 70, 0 );              // flag
            writeString( out, 40, zero );
            writeString( out, 41, one  );
            writeString( out, 42, one  );
            writeString( out, 50, zero  );
            writeInt( out, 71, 0 );
            writeString( out, 3, "arial.ttf" );  // fonts
            writeString( out, 4, standard );
          }
          writeEndTable( out );
        }

        ++handle; writeBeginTable( out, "VIEW", handle, 0 ); // no VIEW
        writeEndTable( out );

        ++handle; writeBeginTable( out, "UCS", handle, 0 ); // no UCS
        writeEndTable( out );
        
        ++handle; writeBeginTable( out, "APPID", handle, 1 );
        {
          writeString( out, 0, "APPID" );
          ++handle;
          writeAcDb( out, handle, AcDbSymbolTR, "AcDbRegAppTableRecord" );
          writeString( out, 2, "ACAD" ); // applic. name
          writeInt( out, 70, 0 );        // flag
        }
        writeEndTable( out );

        if ( mVersion13 ) {
          ++handle; writeBeginTable( out, "DIMSTYLE", handle, 0 );
          writeString( out, 100, "AcDbDimStyleTable" );
          // {
          //   writeString( out, 0, "DIMSTYLE" );
          //   ++handle; writeHex( out, 105, handle ); 
          //   writeAcDb( out, -1, AcDbSymbolTR, "AcDbDimStyleTableRecord" );
          //   writeString( out, 2, "ISO-25" );
          //   writeInt( out, 70, 0 );
          //   writeString( out, 40, "1.0" );
          //   writeString( out, 41, "2.5" );
          //   writeString( out, 42, "0.625" );
          //   writeString( out, 43, "3.75" );
          //   writeString( out, 44, "1.25" );
          //   writeInt( out, 73, 0 );
          //   writeInt( out, 74, 0 );
          //   writeInt( out, 77, 1 );
          //   writeInt( out, 78, 8 );
          //   writeString( out, 140, "2.5" );
          //   writeString( out, 141, "2.5" );
          //   writeString( out, 143, "0.04" );
          //   writeString( out, 147, "0.625" );
          //   writeInt( out, 171, 3 );
          //   writeInt( out, 172, 1 );
          //   writeInt( out, 178, 0 );
          //   writeInt( out, 271, 2 );
          //   writeInt( out, 272, 2 );
          //   writeInt( out, 274, 3 );
          //   writeInt( out, 278, 44 );
          //   writeInt( out, 283, 0 );
          //   writeInt( out, 284, 8 );
          //   writeInt( out, 340, 11 );
          // }
          writeEndTable( out );

          ++handle; writeBeginTable( out, "BLOCK_RECORD", handle, DrawingBrushPaths.mPointLib.mSymbolNr );
          {
            for ( int n = 0; n < DrawingBrushPaths.mPointLib.mSymbolNr; ++ n ) {
              String block = "P_" + DrawingBrushPaths.mPointLib.getSymbolThName(n).replace(':','-');
              writeString( out, 0, "BLOCK_RECORD" );
              ++handle; writeAcDb( out, handle, AcDbSymbolTR, "AcDbBlockTableRecord" );
              writeString( out, 2, block );
              writeInt( out, 70, 0 );              // flag
            }
          }
          writeEndTable( out );
        }
      }
      writeEndSection( out );
      out.flush();
      
      writeSection( out, "BLOCKS" );
      {
        // // 8 layer (0), 2 block name,
        for ( int n = 0; n < DrawingBrushPaths.mPointLib.mSymbolNr; ++ n ) {
          SymbolPoint pt = (SymbolPoint)DrawingBrushPaths.mPointLib.getSymbolByIndex(n);
          String block = "P_" + pt.getThName().replace(':','-');

          writeString( out, 0, "BLOCK" );
          ++handle; writeAcDb( out, handle, AcDbEntity, "AcDbBlockBegin" );
          writeString( out, 8, "POINT" );
          writeString( out, 2, block ); // block name, can be repeated with '3'
          writeInt( out, 70, 0 );       // flag 0=none, 1=anonymous, 2=non-conts attr, 4=xref, 8=xref overlay,
                                        // 16=ext. dependent, 32=ext. resolved (ignored), 64=referenced xref (ignored)
          writeXYZ( out, 0, 0, 0, 0 );

          out.write( pt.getDxf() );
          // out.write( DrawingBrushPaths.mPointLib.getPoint(n).getDxf() );

          writeString( out, 0, "ENDBLK" );
          // if ( mVersion13 ) {
            ++handle; writeAcDb( out, handle, AcDbEntity, "AcDbBlockEnd");
            writeString( out, 8, "POINT" );
          // }
        }
      }
      writeEndSection( out );
      out.flush();
      
      writeSection( out, "ENTITIES" );
      {
        float SCALE_FIX = DrawingUtil.SCALE_FIX;

        // reference
        {
          StringWriter sw9 = new StringWriter();
          PrintWriter pw9  = new PrintWriter(sw9);
          printString( pw9, 0, "LINE" );
          ++handle; printAcDb( pw9, handle, AcDbEntity, AcDbLine );
          printString( pw9, 8, "REF" );
          // printInt(  pw9, 39, 0 );         // line thickness
          printXYZ(  pw9, xmin, -ymax, 0.0f, 0 );
          printXYZ( pw9, (xmin+10*SCALE_FIX), -ymax, 0.0f, 1 );
          out.write( sw9.getBuffer().toString() );
        }
        {
          StringWriter sw8 = new StringWriter();
          PrintWriter pw8  = new PrintWriter(sw8);
          printString( pw8, 0, "LINE" );
          ++handle; printAcDb( pw8, handle, AcDbEntity, AcDbLine );
          printString( pw8, 8, "REF" );
          // printInt(  pw8, 39, 0 );         // line thickness
          printXYZ(  pw8, xmin, -ymax, 0.0f, 0 );
          printXYZ( pw8,  xmin, -ymax+10*SCALE_FIX, 0.0f, 1 );
          out.write( sw8.getBuffer().toString() );
        }
        {
          StringWriter sw7 = new StringWriter();
          PrintWriter pw7  = new PrintWriter(sw7);
          printString( pw7, 0, "TEXT" );
          ++handle; printAcDb( pw7, handle, AcDbEntity, AcDbText );
          printString( pw7, 8, "REF" );
          // pw7.printf("%s\n  0\n", "\"10\"" );
          printXYZ(   pw7, (xmin+10*SCALE_FIX+1), -ymax, 0.0f, 0 );
          printFloat( pw7, 40, 0.3f );
          printString( pw7, 1, "\"10\"" );
          printEndText( pw7, my_style );
          out.write( sw7.getBuffer().toString() );
        }
        {
          StringWriter sw6 = new StringWriter();
          PrintWriter pw6  = new PrintWriter(sw6);
          printString( pw6, 0, "TEXT" );
          ++handle; printAcDb( pw6, handle, AcDbEntity, AcDbText );
          printString( pw6, 8, "REF" );
          // pw6.printf("%s\n  0\n", "\"10\"" );
          printXYZ(   pw6, xmin, -ymax+10*SCALE_FIX+1, 0.0f, 0 );
          printFloat( pw6, 40, 0.3f );
          // printFloat( pw6, 50, 90.0f ); // rotation
          printString( pw6, 1, "\"10\"" );
	  printEndText( pw6, my_style );
          out.write( sw6.getBuffer().toString() );
        }
        out.flush();

        // centerline data
        if ( PlotInfo.isSketch2D( type ) ) {
          for ( DrawingPath sh : plot.getLegs() ) {
            DistoXDBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            
            StringWriter sw4 = new StringWriter();
            PrintWriter pw4  = new PrintWriter(sw4);
            // if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
              NumStation f = num.getStation( blk.mFrom );
              NumStation t = num.getStation( blk.mTo );
 
              printString( pw4, 0, "LINE" );
              ++handle; printAcDb( pw4, handle, AcDbEntity, AcDbLine );
              printString( pw4, 8, "LEG" );
              // printInt( pw4, 39, 2 );         // line thickness

              if ( type == PlotInfo.PLOT_PLAN ) {
                float x =  scale * DrawingUtil.toSceneX( f.e );
                float y =  scale * DrawingUtil.toSceneY( f.s );
                float x1 = scale * DrawingUtil.toSceneX( t.e );
                float y1 = scale * DrawingUtil.toSceneY( t.s );
                printXYZ( pw4, x, -y, 0.0f, 0 );
                printXYZ( pw4, x1, -y1, 0.0f, 1 );
              } else if ( PlotInfo.isProfile( type ) ) {
                float x =  scale * DrawingUtil.toSceneX( f.h );
                float y =  scale * DrawingUtil.toSceneY( f.v );
                float x1 = scale * DrawingUtil.toSceneX( t.h );
                float y1 = scale * DrawingUtil.toSceneY( t.v );
                printXYZ( pw4, x, -y, 0.0f, 0 );
                printXYZ( pw4, x1, -y1, 0.0f, 1 );
              } else if ( type == PlotInfo.PLOT_SECTION ) {
                // nothing
              }
            // }
            out.write( sw4.getBuffer().toString() );
            out.flush();
          }
          for ( DrawingPath sh : plot.getSplays() ) {
            DistoXDBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            
            StringWriter sw41 = new StringWriter();
            PrintWriter pw41  = new PrintWriter(sw41);
            // if ( sh.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
              NumStation f = num.getStation( blk.mFrom );

              printString( pw41, 0, "LINE" );
              ++handle; printAcDb( pw41, handle, AcDbEntity, AcDbLine );
              printString( pw41, 8, "SPLAY" );
              // printInt( pw41, 39, 1 );         // line thickness

              float dhs = scale * blk.mLength * (float)Math.cos( blk.mClino * grad2rad )*SCALE_FIX; // scaled dh
              if ( type == PlotInfo.PLOT_PLAN ) {
                float x = scale * DrawingUtil.toSceneX( f.e );
                float y = scale * DrawingUtil.toSceneY( f.s );
                float de =   dhs * (float)Math.sin( blk.mBearing * grad2rad);
                float ds = - dhs * (float)Math.cos( blk.mBearing * grad2rad);
                printXYZ( pw41, x, -y, 0.0f, 0 );
                printXYZ( pw41, x + de, -(y+ds), 0.0f, 1 );
              } else if ( PlotInfo.isProfile( type ) ) {
                float x = scale * DrawingUtil.toSceneX( f.h );
                float y = scale * DrawingUtil.toSceneY( f.v );
                float dv = - blk.mLength * (float)Math.sin( blk.mClino * grad2rad )*SCALE_FIX;
                printXYZ( pw41, x, -y, 0.0f, 0 );
                printXYZ( pw41, x+dhs*blk.mExtend, -(y+dv), 0.0f, 1 );
              } else if ( type == PlotInfo.PLOT_SECTION ) {
                // nothing
              }
            // }
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
            DrawingStationPath st = (DrawingStationPath)path;

            printString( pw5, 0, "TEXT" );
            printString( pw5, 8, "STATION" );
            // if ( mVersion13 ) {
              ++handle; printAcDb( pw5, handle, AcDbEntity, AcDbText );
              pw5.printf("%s\n  0\n", st.mName );
            // }
            printXYZ( pw5, st.cx * scale, -st.cy * scale, 0.0f, 0 );
            printFloat( pw5, 40, LABEL_SCALE );
            printString( pw5, 1, st.mName );
	    printEndText( pw5, my_style );
          } 
          else if ( path.mType == DrawingPath.DRAWING_PATH_LINE )
          {
            DrawingLinePath line = (DrawingLinePath)path;
            String layer = "L_" + DrawingBrushPaths.mLineLib.getSymbolThName( line.lineType() ).replace(':','-');
            int flag = 0;
            boolean use_spline = false;
            if ( mVersion13 ) {
              for ( LinePoint p = line.mFirst; p != null; p = p.mNext ) {
                if ( p.has_cp ) {
                  use_spline = true;
                  break;
                }
              }
            }
            if ( use_spline ) {
              printString( pw5, 0, "SPLINE" );
              ++handle; printAcDb( pw5, handle, AcDbEntity, "AcDbSpline" );
              printString( pw5, 8, layer );
              printString( pw5, 6, lt_continuous );
              printFloat( pw5, 48, 1.0f ); // scale 
              printInt( pw5, 60, 0 ); // visibilty (0: visible, 1: invisible)
              printInt( pw5, 66, 1 ); // group 1: "entities follow" flag
              // printInt( pw5, 67, 0 ); // in model space [default]
              printXYZ( pw5, 0, 0, 1, 200 );

              float xt=0, yt=0;
              int np = 2;
              LinePoint p = line.mFirst; 
              LinePoint pn = p.mNext;
              if ( pn != null ) {
                if ( pn.has_cp ) {
                  xt = pn.mX1 - p.mX;
                  yt = pn.mY1 - p.mY;
                } else {
                  xt = pn.mX - p.mX;
                  yt = pn.mY - p.mY;
                }
                float d = (float)Math.sqrt( xt*xt + yt*yt );
                printXYZ( pw5, xt/d, -yt/d, 0, 2 );

                while ( pn.mNext != null ) {
                  p = pn;
                  pn = pn.mNext;
                  ++np;
                }
                if ( pn.has_cp ) {
                  xt = pn.mX - pn.mX2;
                  yt = pn.mY - pn.mY2;
                } else {
                  xt = pn.mX - p.mX;
                  yt = pn.mY - p.mY;
                }
                d = (float)Math.sqrt( xt*xt + yt*yt );
                printXYZ( pw5, xt/d, -yt/d, 0, 3 );
              }

              int ncp = np + 3 * (np-1) -1;
              int nk = ncp + 4 - (np - 2);
              printInt( pw5, 70, 1064 );
              printInt( pw5, 71, 3 );   // degree
              printInt( pw5, 72, nk );  // nr. of knots
              printInt( pw5, 73, ncp ); // nr. of control pts
              printInt( pw5, 74, np );  // nr. of fix points
              
	      printInt( pw5, 40, 0 );
              for ( int k=0; k<np; ++k ) {
                for ( int j=0; j<3; ++j ) printInt( pw5, 40, k );
              }
	      printInt( pw5, 40, np-1 );

              p = line.mFirst; 
              xt = p.mX;
              yt = p.mY;
              printXYZ( pw5, p.mX * scale, -p.mY * scale, 0.0f, 0 );
              for ( p = p.mNext; p != null; p = p.mNext ) { 
                if ( p.has_cp ) {
                  printXYZ( pw5, p.mX1 * scale, -p.mY1 * scale, 0.0f, 0 );
                  printXYZ( pw5, p.mX2 * scale, -p.mY2 * scale, 0.0f, 0 );
                } else {
                  printXYZ( pw5, xt * scale, -yt * scale, 0.0f, 0 );
                  printXYZ( pw5, p.mX * scale, -p.mY * scale, 0.0f, 0 );
                }
                printXYZ( pw5, p.mX * scale, -p.mY * scale, 0.0f, 0 );
                xt = p.mX;
                yt = p.mY;
              }
              for ( p = line.mFirst; p != null; p = p.mNext ) { 
                printXYZ( pw5, p.mX * scale, -p.mY * scale, 0.0f, 1 );
              }
            } else {
              // handle = printLWPolyline( pw5, line, scale, handle, layer, false );
              handle = printPolyline( pw5, line, scale, handle, layer, false );
            }
          } 
          else if ( path.mType == DrawingPath.DRAWING_PATH_AREA )
          {
            DrawingAreaPath area = (DrawingAreaPath) path;
            // Log.v("DistoX", "area size " + area.size() );
            String layer = "A_" + DrawingBrushPaths.mAreaLib.getSymbolThName( area.areaType() ).replace(':','-');

            // handle = printLWPolyline( pw5, line, scale, handle, layer, true );
            handle = printPolyline( pw5, area, scale, handle, layer, true );

            if ( mVersion13 ) {
              printString( pw5, 0, "HATCH" );    // entity type HATCH
              ++handle; printAcDb( pw5, handle, AcDbEntity, "AcDbHatch" );
              // printString( pw5, 8, "AREA" );  // layer (color BYLAYER)
              printString( pw5, 8, layer );      // layer (color BYLAYER)
              printString( pw5, 2, "_USER" );    // hatch pattern name

              printXYZ( pw5, 0f, 0f, 0f, 0 );
              printXYZ( pw5, 0f, 0f, 1f, 200 );  // extrusion direction, default 0,0,1
              printInt( pw5, 70, 0 );            // 1:solid fill, 0:pattern-fill
              printInt( pw5, 71, 0 );            // 1:associative 0:non-associative
              printInt( pw5, 91, 1 );            // nr. boundary paths (loops): 1
              // boundary data
                printInt( pw5, 92, 2 );          // flag. 1:external 2:polyline 4:derived 8:text 16:outer
                printInt( pw5, 72, 0 );          // not-polyline edge type (0: default) 1:line 2:arc 3:ellipse-arec 4:spline
                                                 // polyline: has-bulge
                printInt( pw5, 73, 1 );          // is-closed flag
                printInt( pw5, 93, area.size() ); // nr. of points (not polyline) vertices (polyline)
                for (LinePoint p = area.mFirst; p != null; p = p.mNext ) { 
                  printXY( pw5, p.mX * scale, -p.mY * scale, 0 );
                }
                // printXY( pw5, area.mFirst.mX * scale, -area.mFirst.mY * scale, 0 );
                printInt( pw5, 97, 0 );            // nr. source boundary objects
              printInt( pw5, 75, 0 );            // hatch style: 0:normal, 1:outer, 2:ignore
              printInt( pw5, 76, 0 );            // hatch pattern type: 0:user, 1:predefined, 2:custom
              printFloat( pw5, 52, 0f );        // hatch pattern angle (only pattern fill)
	      printFloat( pw5, 41, 1f );         // hatch pattern scale (only pattern fill)
              printInt( pw5, 77, 0 );            // hatch pattern double flag (0: not double)
              printInt( pw5, 78, 1 );            // nr. pattern definition lines
              // here goes pattern data
                printFloat( pw5, 53, 45f );        // pattern line angle
                printFloat( pw5, 43, 0f );         // pattern base point
                printFloat( pw5, 44, 0f );
                printFloat( pw5, 45, -3.6f );      // pattern line offset
                printFloat( pw5, 46, 3.6f );         
                printInt( pw5, 79, 0 );            // nr. dash length items
              // // printFloat( pw5, 49, 3f );         // dash length (repeated nr. times)

               printFloat( pw5, 47, 0f );         // pixel size
               printInt( pw5, 98, 0 );            // nr. seed points
              // printXYZ( pw5, 0f, 0f, 0f, 0 );

              // 450 451 452 453 460 461 462 and 470 all present or none
              // printInt( pw5, 450, 0 ); // 0:solid, 1:gradient
              // printInt( pw5, 451, 0 ); // reserved
              // printInt( pw5, 452, 1 ); // 1:single color  2:two-color
              // printInt( pw5, 453, 0 ); // 0:solid, 2:gradient
              // printFloat( pw5, 460, 0f ); // rotation angle [rad]
              // printFloat( pw5, 461, 0f ); // gradient definition
              // printFloat( pw5, 462, 0.5f );  // color tint
              // printFloat( pw5, 463, 0f ); // reserved
              // printString( pw5, 470, "LINEAR" );  // default
            }
          } 
          else if ( path.mType == DrawingPath.DRAWING_PATH_POINT )
          {
            // FIXME point scale factor is 0.3
            DrawingPointPath point = (DrawingPointPath) path;
            String block = "P_" + DrawingBrushPaths.mPointLib.getSymbolThName( point.mPointType ).replace(':','-');
            if ( point.mPointType == DrawingBrushPaths.getPointLabelIndex() ) {
              DrawingLabelPath label = (DrawingLabelPath)point;
              printString( pw5, 0, "TEXT" );
              printString( pw5, 1, label.mText );
              printString( pw5, 2, block );
              ++handle; printAcDb( pw5, handle, AcDbEntity, AcDbText );
              printString( pw5, 8, "POINT" );
              printString( pw5, 7, my_style ); // style
              // pw5.printf("%s\n  0\n", "\"10\"" );
              printXYZ( pw5, point.cx * scale, -point.cy * scale, 0.0f, 0 );
              printXYZ( pw5, 0, 0, 1, 1 ); // alignmenmt
              printFloat( pw5, 40, LABEL_SCALE );   // height 
              printFloat( pw5, 41, 1 );    // scale X
              printFloat( pw5, 50, (float)label.mOrientation );    // rotation
	      printFloat( pw5, 51, 0 );    // oblique angle
	      // printFloat( pw5, 72, 0 );    // align
	      // printFloat( pw5, 73, 0 );    // valign
            } else {
              // int idx = 1 + point.mPointType;
              printString( pw5, 0, "INSERT" );
              ++handle; printAcDb( pw5, handle, "AcDbBlockReference" );
              printString( pw5, 8, "POINT" );
              printString( pw5, 2, block );
              printFloat( pw5, 41, POINT_SCALE );
              printFloat( pw5, 42, POINT_SCALE );
              printFloat( pw5, 50, 360-(float)(point.mOrientation) );
              printXYZ( pw5, point.cx * scale, -point.cy * scale, 0.0f, 0 );
            }
          }
          out.write( sw5.getBuffer().toString() );
          out.flush();
        }
      }
      writeEndSection( out );
      
      writeString( out, 0, "EOF" );
      out.flush();
    } catch ( IOException e ) {
      // FIXME
      TDLog.Error( "DXF io-exception " + e.toString() );
    }
  }

}

