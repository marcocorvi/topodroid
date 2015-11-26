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

import android.util.FloatMath;
import android.util.Log;

class DrawingDxf
{
  private static final float grad2rad = TopoDroidUtil.GRAD2RAD;
  private static int VERSION = 9;

  static final String zero = "0.0";
  static final String one  = "1.0";
  static final String half = "0.5";

  static void writeComment( BufferedWriter out, String comment ) throws IOException
  {
    out.write( "  999\n" + comment + "\n" );
  }

  static void writeHex( BufferedWriter out, int code, int handle ) throws IOException
  {
    if ( VERSION >= 13 ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.printf("  %d\n%X\n", code, handle );
      out.write( sw.getBuffer().toString() );
    }
  }

  static void printHex( PrintWriter pw, int code, int handle )
  {
    if ( VERSION >= 13 ) {
      pw.printf("  %d\n%X\n", code, handle );
    }
  }

  static void writeAcDb( BufferedWriter out, int hex, String acdb1 ) throws IOException
  {
    if ( VERSION >= 13 ) {
      if ( hex >= 0 ) writeHex( out, 5, hex );
      out.write("  100\n" + acdb1 + "\n" );
    }
  }

  static void writeAcDb( BufferedWriter out, int hex, String acdb1, String acdb2 ) throws IOException
  {
    if ( VERSION >= 13 ) {
      if ( hex >= 0 ) writeHex( out, 5, hex );
      out.write("  100\n" + acdb1 + "\n  100\n" + acdb2 + "\n" );
    }
  }


  static void printAcDb( PrintWriter pw, int hex, String acdb1 )
  {
    if ( VERSION >= 13 ) {
      if ( hex >= 0 ) printHex( pw, 5, hex );
      pw.printf("  100\n" + acdb1 + "\n" );
    }
  }

  static void printAcDb( PrintWriter pw, int hex, String acdb1, String acdb2 )
  {
    if ( VERSION >= 13 ) {
      if ( hex >= 0 ) printHex( pw, 5, hex );
      pw.printf("  100\n" + acdb1 + "\n  100\n" + acdb2 + "\n" );
    }
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
    pw.printf(Locale.ENGLISH, "  %d\n%.2f\n", code, val );
  }

  static void writeInt(  BufferedWriter out, int code, int val ) throws IOException
  {
    out.write( "  " + code + "\n" + val + "\n" );
  }

  static void printInt(  PrintWriter pw, int code, int val )
  {
    pw.printf( "  %d\n%d\n", code, val );
  }

  static void printXY( PrintWriter pw, float x, float y )
  {
    pw.printf(Locale.ENGLISH, "  10\n%.2f\n  20\n%.2f\n", x, y );
  }

  static void printXYZ( PrintWriter pw, float x, float y, float z )
  {
    pw.printf(Locale.ENGLISH, "  10\n%.2f\n  20\n%.2f\n  30\n%.2f\n", x, y, z );
  }

  static void printXYZ1( PrintWriter pw, float x, float y, float z )
  {
    pw.printf(Locale.ENGLISH, "  11\n%.2f\n  21\n%.2f\n  31\n%.2f\n", x, y, z );
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
    printAcDb( pw2, handle, "AcDbSymbolTableRecord", "AcDbLayerTableRecord");
    printString( pw2, 2, name );  // layer name
    printInt( pw2, 70, flag );    // layer flag
    printInt( pw2, 62, color );   // layer color
    printString( pw2, 6, linetype ); // linetype name
    // printInt( pw2, 370, 100 );
    // printString( pw2, 390, "F" );
  }

  static void write( BufferedWriter out, DistoXNum num, DrawingCommandManager plot, long type )
  {
    VERSION = TopoDroidSetting.mAcadVersion;

    float scale = TopoDroidSetting.mDxfScale;
    int handle = 0;
    float xmin=10000f, xmax=-10000f, 
          ymin=10000f, ymax=-10000f;
    // compute BBox
    for ( ICanvasCommand cmd : plot.mCurrentStack ) {
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
        if ( st.mXpos < xmin ) xmin = st.mXpos;
        if ( st.mXpos > xmax ) xmax = st.mXpos;
        if ( st.mYpos < ymin ) ymin = st.mYpos;
        if ( st.mYpos > ymax ) ymax = st.mYpos;
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

      writeString( out, 9, "$ACADVER" );
      String ACAD_VERSION = (VERSION == 13)? "AC1012" : "AC1009";
      writeString( out, 1, ACAD_VERSION );

      if ( VERSION >= 13 ) {
        writeString( out, 9, "$DWGCODEPAGE" );
        writeString( out, 3, "ANSI_1251" );
      }

      writeString( out, 9, "$INSBASE" );
      {
        StringWriter sw1 = new StringWriter();
        PrintWriter pw1  = new PrintWriter(sw1);
        printXYZ( pw1, 0.0f, 0.0f, 0.0f ); // FIXME (0,0,0)
        printString( pw1, 9, "$EXTMIN" );
        printXYZ( pw1, xmin, -ymax, 0.0f );
        printString( pw1, 9, "$EXTMAX" );
        printXYZ( pw1, xmax * scale, -ymin * scale, 0.0f );
        out.write( sw1.getBuffer().toString() );
      }
      writeEndSection( out );
      
      String lt_continuous = "CONTINUOUS";
      writeSection( out, "TABLES" );
      {
        if ( VERSION >= 13 ) {
          ++handle; writeBeginTable( out, "VPORT", handle, 1 );
          {
            writeString( out, 0, "VPORT" );
            ++handle; writeAcDb( out, handle, "AcDbSymbolTableRecord", "AcDbViewportTableRecord" );
            writeString( out, 2, "MyViewport" );
            writeInt( out, 70, 0 );
            writeString( out, 10, zero );
            writeString( out, 20, zero );
            writeString( out, 11, one  );
            writeString( out, 21, one  );
            writeString( out, 12, zero );
            writeString( out, 22, zero );
            writeString( out, 13, zero );
            writeString( out, 23, zero );
            writeString( out, 14, half );
            writeString( out, 24, half );
            writeString( out, 15, half );
            writeString( out, 25, half );
            writeString( out, 16, zero );
            writeString( out, 26, zero );
            writeString( out, 36, one  );
            writeString( out, 17, zero );
            writeString( out, 27, zero );
            writeString( out, 37, zero );
            writeString( out, 40, zero );
            writeString( out, 41, "2.0" );
            writeString( out, 42, "50.0" );
          }
          writeEndTable( out );
        }

        ++handle; writeBeginTable( out, "LTYPE", handle, 1 );
        {
          // int flag = 64;
          writeString( out, 0, "LTYPE" );
          ++handle; writeAcDb( out, handle, "AcDbSymbolTableRecord", "AcDbLinetypeTableRecord" );
          writeString( out, 2, lt_continuous );
          writeInt( out, 70, 64 );
          writeString( out, 3, "Solid line" );
          writeInt( out, 72, 65 );
          writeInt( out, 73, 0 );
          writeString( out, 40, zero );
        }
        writeEndTable( out );

        SymbolLineLibrary linelib = DrawingBrushPaths.mLineLib;
        SymbolAreaLibrary arealib = DrawingBrushPaths.mAreaLib;
        int nr_layers = 6 + linelib.mAnyLineNr + arealib.mAnyAreaNr;
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
          // ++handle; printLayer( pw2, handle, "AREA",    flag, color, lt_continuous ); ++color;
          ++handle; printLayer( pw2, handle, "REF",     flag, color, lt_continuous ); ++color;
          
          if ( linelib != null ) { 
            for ( SymbolLine line : linelib.mAnyLine ) {
              String lname = "L_" + line.getThName().replace(':','-');
              ++handle; printLayer( pw2, handle, lname, flag, color, lt_continuous ); ++color;
            }
          }

          if ( arealib != null ) {
            for ( SymbolArea area : arealib.mAnyArea ) {
              String aname = "A_" + area.getThName().replace(':','-');
              ++handle; printLayer( pw2, handle, aname, flag, color, lt_continuous ); ++color;
            }
          }
          out.write( sw2.getBuffer().toString() );
        }
        writeEndTable( out );

        if ( VERSION >= 13 ) {
          ++handle; writeBeginTable( out, "STYLE", handle, 1 ); 
          {
            writeString( out, 0, "STYLE" );
            ++handle; writeAcDb( out, handle, "AcDbSymbolTableRecord", "AcDbTextStyleTableRecord" );
            writeString( out, 2, "MyStyle" );  // name
            writeInt( out, 70, 0 );              // flag
            writeString( out, 40, zero );
            writeString( out, 41, one  );
            writeString( out, 42, one  );
            writeString( out, 3, "arial.ttf" );  // fonts
          }
          writeEndTable( out );
        }

        ++handle; writeBeginTable( out, "VIEW", handle, 0 );
        writeEndTable( out );

        ++handle; writeBeginTable( out, "UCS", handle, 0 );
        writeEndTable( out );
        
        if ( VERSION >= 13 ) {
          ++handle; writeBeginTable( out, "STYLE", handle, 0 );
          writeEndTable( out );
        }

        ++handle; writeBeginTable( out, "APPID", handle, 1 );
        {
          writeString( out, 0, "APPID" );
          ++handle;
          writeAcDb( out, handle, "AcDbSymbolTableRecord", "AcDbRegAppTableRecord" );
          writeString( out, 2, "ACAD" ); // applic. name
          writeInt( out, 70, 0 );        // flag
        }
        writeEndTable( out );

        if ( VERSION >= 13 ) {
          ++handle; writeBeginTable( out, "DIMSTYLE", handle, -1 );
          writeString( out, 100, "AcDbDimStyleTable" );
          writeInt( out, 70, 1 );
          writeEndTable( out );

          ++handle; writeBeginTable( out, "BLOCK_RECORD", handle, DrawingBrushPaths.mPointLib.mAnyPointNr );
          {
            for ( int n = 0; n < DrawingBrushPaths.mPointLib.mAnyPointNr; ++ n ) {
              SymbolPoint pt = DrawingBrushPaths.mPointLib.getAnyPoint(n);
              String block = "P_" + pt.getThName().replace(':','-');
              writeString( out, 0, "BLOCK_RECORD" );
              ++handle; writeAcDb( out, handle, "AcDbSymbolTableRecord", "AcDbBlockTableRecord" );
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
        for ( int n = 0; n < DrawingBrushPaths.mPointLib.mAnyPointNr; ++ n ) {
          SymbolPoint pt = DrawingBrushPaths.mPointLib.getAnyPoint(n);
          String block = "P_" + pt.getThName().replace(':','-');

          writeString( out, 0, "BLOCK" );
          ++handle; writeAcDb( out, handle, "AcDbEntity", "AcDbBlockBegin" );
          writeString( out, 8, "POINT" );
          writeString( out, 2, block );
          writeInt( out, 70, 64 );       // flag 64 = this definition is referenced
          writeString( out, 10, "0.0" );
          writeString( out, 20, "0.0" );
          writeString( out, 30, "0.0" );

          out.write( pt.getDxf() );
          // out.write( DrawingBrushPaths.mPointLib.getPoint(n).getDxf() );

          writeString( out, 0, "ENDBLK" );
          if ( VERSION >= 13 ) {
            ++handle; writeAcDb( out, handle, "AcDbEntity", "AcDbBlockEnd");
            writeString( out, 8, "POINT" );
          }
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
          ++handle; printAcDb( pw9, handle, "AcDbEntity", "AcDbLine" );
          printString( pw9, 8, "REF" );
          // printInt(  pw9, 39, 0 );         // line thickness
          printXYZ(  pw9, xmin, -ymax, 0.0f );
          printXYZ1( pw9, (xmin+10*SCALE_FIX), -ymax, 0.0f );
          out.write( sw9.getBuffer().toString() );
        }
        {
          StringWriter sw8 = new StringWriter();
          PrintWriter pw8  = new PrintWriter(sw8);
          printString( pw8, 0, "LINE" );
          ++handle; printAcDb( pw8, handle, "AcDbEntity", "AcDbLine" );
          printString( pw8, 8, "REF" );
          // printInt(  pw8, 39, 0 );         // line thickness
          printXYZ(  pw8, xmin, -ymax, 0.0f );
          printXYZ1( pw8,  xmin, -ymax+10*SCALE_FIX, 0.0f );
          out.write( sw8.getBuffer().toString() );
        }
        {
          StringWriter sw7 = new StringWriter();
          PrintWriter pw7  = new PrintWriter(sw7);
          printString( pw7, 0, "TEXT" );
          ++handle; printAcDb( pw7, handle, "AcDbEntity", "AcDbText" );
          printString( pw7, 8, "REF" );
          // pw7.printf("%s\n  0\n", "\"10\"" );
          printXYZ(   pw7, (xmin+10*SCALE_FIX+1), -ymax, 0.0f );
          printFloat( pw7, 40, 0.3f );
          printString( pw7, 1, "\"10\"" );
          out.write( sw7.getBuffer().toString() );
        }
        {
          StringWriter sw6 = new StringWriter();
          PrintWriter pw6  = new PrintWriter(sw6);
          printString( pw6, 0, "TEXT" );
          ++handle; printAcDb( pw6, handle, "AcDbEntity", "AcDbText" );
          printString( pw6, 8, "REF" );
          // pw6.printf("%s\n  0\n", "\"10\"" );
          printXYZ(   pw6, xmin, -ymax+10*SCALE_FIX+1, 0.0f );
          printFloat( pw6, 40, 0.3f );
          // printFloat( pw6, 50, 90.0f ); // rotation
          printString( pw6, 1, "\"10\"" );
          out.write( sw6.getBuffer().toString() );
        }
        out.flush();

        // centerline data
        if ( type == PlotInfo.PLOT_PLAN || type == PlotInfo.PLOT_EXTENDED ) {
          for ( DrawingPath sh : plot.mLegsStack ) {
            DistoXDBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            
            StringWriter sw4 = new StringWriter();
            PrintWriter pw4  = new PrintWriter(sw4);
            // if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
              NumStation f = num.getStation( blk.mFrom );
              NumStation t = num.getStation( blk.mTo );
 
              printString( pw4, 0, "LINE" );
              ++handle; printAcDb( pw4, handle, "AcDbEntity", "AcDbLine" );
              printString( pw4, 8, "LEG" );
              // printInt( pw4, 39, 2 );         // line thickness

              if ( type == PlotInfo.PLOT_PLAN ) {
                float x =  scale * DrawingUtil.toSceneX( f.e );
                float y =  scale * DrawingUtil.toSceneY( f.s );
                float x1 = scale * DrawingUtil.toSceneX( t.e );
                float y1 = scale * DrawingUtil.toSceneY( t.s );
                printXYZ( pw4, x, -y, 0.0f );
                printXYZ1( pw4, x1, -y1, 0.0f );
              } else if ( type == PlotInfo.PLOT_EXTENDED ) {
                float x =  scale * DrawingUtil.toSceneX( f.h );
                float y =  scale * DrawingUtil.toSceneY( f.v );
                float x1 = scale * DrawingUtil.toSceneX( t.h );
                float y1 = scale * DrawingUtil.toSceneY( t.v );
                printXYZ( pw4, x, -y, 0.0f );
                printXYZ1( pw4, x1, -y1, 0.0f );
              } else if ( type == PlotInfo.PLOT_SECTION ) {
                // nothing
              }
            // }
            out.write( sw4.getBuffer().toString() );
            out.flush();
          }
          for ( DrawingPath sh : plot.mSplaysStack ) {
            DistoXDBlock blk = sh.mBlock;
            if ( blk == null ) continue;
            
            StringWriter sw41 = new StringWriter();
            PrintWriter pw41  = new PrintWriter(sw41);
            // if ( sh.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
              NumStation f = num.getStation( blk.mFrom );

              printString( pw41, 0, "LINE" );
              ++handle; printAcDb( pw41, handle, "AcDbEntity", "AcDbLine" );
              printString( pw41, 8, "SPLAY" );
              // printInt( pw41, 39, 1 );         // line thickness

              float dhs = scale * blk.mLength * FloatMath.cos( blk.mClino * grad2rad )*SCALE_FIX; // scaled dh
              if ( type == PlotInfo.PLOT_PLAN ) {
                float x = scale * DrawingUtil.toSceneX( f.e );
                float y = scale * DrawingUtil.toSceneY( f.s );
                float de =   dhs * FloatMath.sin( blk.mBearing * grad2rad);
                float ds = - dhs * FloatMath.cos( blk.mBearing * grad2rad);
                printXYZ( pw41, x, -y, 0.0f );
                printXYZ1( pw41, x + de, -(y+ds), 0.0f );
              } else if ( type == PlotInfo.PLOT_EXTENDED ) {
                float x = scale * DrawingUtil.toSceneX( f.h );
                float y = scale * DrawingUtil.toSceneY( f.v );
                float dv = - blk.mLength * FloatMath.sin( blk.mClino * grad2rad )*SCALE_FIX;
                printXYZ( pw41, x, -y, 0.0f );
                printXYZ1( pw41, x+dhs*blk.mExtend, -(y+dv), 0.0f );
              } else if ( type == PlotInfo.PLOT_SECTION ) {
                // nothing
              }
            // }
            out.write( sw41.getBuffer().toString() );
            out.flush();
          }
        }

        // FIXME station scale is 0.3
        float POINT_SCALE = 10.0f;
        for ( ICanvasCommand cmd : plot.mCurrentStack ) {
          if ( cmd.commandType() != 0 ) continue;
          DrawingPath path = (DrawingPath)cmd;

          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          if ( path.mType == DrawingPath.DRAWING_PATH_STATION ) {
            DrawingStationPath st = (DrawingStationPath)path;

            printString( pw5, 0, "TEXT" );
            printString( pw5, 8, "STATION" );
            if ( VERSION >= 13 ) {
              ++handle; printAcDb( pw5, handle, "AcDbEntity", "AcDbText" );
              pw5.printf("%s\n  0\n", st.mName );
            }
            printXYZ( pw5, st.mXpos * scale, -st.mYpos * scale, 0.0f );
            printFloat( pw5, 40, POINT_SCALE );
            printString( pw5, 1, st.mName );
          } else if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath line = (DrawingLinePath)path;
            String layer = "L_" + DrawingBrushPaths.getLineThName( line.lineType() ).replace(':','-');
            // String layer = "LINE";
            int flag = 0;
            boolean use_spline = false;
            if ( VERSION >= 13 ) {
              for ( LinePoint p = line.mFirst; p != null; p = p.mNext ) {
                if ( p.has_cp ) {
                  use_spline = true;
                  break;
                }
              }
            }
            if ( use_spline ) {
              printString( pw5, 0, "SPLINE" );
              ++handle; printAcDb( pw5, handle, "AcDbEntity", "AcDbSpline" );
              printString( pw5, 8, layer );
              printString( pw5, 6, lt_continuous );
              printFloat( pw5, 48, 1.0f ); // scale 
              printInt( pw5, 60, 0 ); // visibilty (0: visible, 1: invisible)
              printInt( pw5, 66, 1 ); // group 1
              // printInt( pw5, 67, 0 ); // in model space [default]
              printInt( pw5, 210, 0 );
              printInt( pw5, 220, 0 );
              printInt( pw5, 230, 1 );

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
                printFloat( pw5, 12, xt/d );
                printFloat( pw5, 22, -yt/d );
                printFloat( pw5, 32, 0 );

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
                printFloat( pw5, 13, xt/d );
                printFloat( pw5, 23, -yt/d );
                printFloat( pw5, 33, 0 );
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
              printXYZ( pw5, p.mX * scale, -p.mY * scale, 0.0f );
              for ( p = p.mNext; p != null; p = p.mNext ) { 
                if ( p.has_cp ) {
                  printXYZ( pw5, p.mX1 * scale, -p.mY1 * scale, 0.0f );
                  printXYZ( pw5, p.mX2 * scale, -p.mY2 * scale, 0.0f );
                } else {
                  printXYZ( pw5, xt * scale, -yt * scale, 0.0f );
                  printXYZ( pw5, p.mX * scale, -p.mY * scale, 0.0f );
                }
                printXYZ( pw5, p.mX * scale, -p.mY * scale, 0.0f );
                xt = p.mX;
                yt = p.mY;
              }
              for ( p = line.mFirst; p != null; p = p.mNext ) { 
                printXYZ1( pw5, p.mX * scale, -p.mY * scale, 0.0f );
              }
            } else {
              printString( pw5, 0, "POLYLINE" );
              ++handle; printAcDb( pw5, handle, "AcDbEntity", "AcDbPolyline" );
              printString( pw5, 8, layer );
              // printInt(  pw5, 39, 1 );         // line thickness
              printInt( pw5, 66, 1 ); // group 1
              printInt( pw5, 70, 0 ); // flag
              for (LinePoint p = line.mFirst; p != null; p = p.mNext ) { 
                printString( pw5, 0, "VERTEX" );
                if ( VERSION >= 13 ) {
                  ++handle; printAcDb( pw5, handle, "AcDbVertex", "AcDb3dPolylineVertex" );
                  printInt( pw5, 70, 32 );
                }
                printString( pw5, 8, layer );
                printXYZ( pw5, p.mX * scale, -p.mY * scale, 0.0f );
              }
            }
            pw5.printf("  0\nSEQEND\n");
            if ( VERSION >= 13 ) {
              ++handle; printHex( pw5, 5, handle );
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath) path;
            String layer = "A_" + DrawingBrushPaths.getAreaThName( area.areaType() ).replace(':','-');
            printString( pw5, 0, "HATCH" );    // entity type HATCH
            // ++handle; printAcDb( pw5, handle, "AcDbEntity", "AcDbHatch" );
            // printString( pw5, 8, "AREA" );  // layer (color BYLAYER)
            printString( pw5, 8, layer );      // layer (color BYLAYER)

            // printXYZ( pw5, 0f, 0f, 0f );
            printFloat( pw5, 210, 0f ); // extrusion direction
            printFloat( pw5, 220, 0f );
            printFloat( pw5, 230, 1f );
            printInt( pw5, 70, 1 );            // solid fill
            printInt( pw5, 71, 1 );            // associative
            printInt( pw5, 91, 1 );            // nr. boundary paths: 1
            printInt( pw5, 92, 3 );            // flag: external (bit-0) polyline (bit-1)
            printInt( pw5, 93, area.size() );  // nr. of edges /  vertices
            printInt( pw5, 72, 0 );            // edge type (0: default)
            printInt( pw5, 73, 1 );            // is-closed flag
            for (LinePoint p = area.mFirst; p != null; p = p.mNext ) { 
              printXY( pw5, p.mX * scale, -p.mY * scale );
            }

            // printInt( pw5, 97, 0 );            // nr. source boundary objects

            // printInt( pw5, 75, 1 );            // hatch style (normal)
            // printInt( pw5, 76, 1 );
            // printFloat( pw5, 52, 1.5708f );    // hatch pattern angle
	    // printFloat( pw5, 41, 3f );         // hatch pattern scale
            // printInt( pw5, 77, 0 );            // hatch pattern double flag (0: not double)
            // printInt( pw5, 78, 1 );            // nr. pattern lines

            // printFloat( pw5, 53, 1.5708f );    // pattern line angle
            // printFloat( pw5, 43, 0f );         // pattern base point
            // printFloat( pw5, 44, 0f );
            // printFloat( pw5, 45, 1f );         // pattern line offset
            // printFloat( pw5, 46, 1f );         
            // printInt( pw5, 79, 0 );            // nr. dash length items
            // // printFloat( pw5, 49, 3f );         // dash length (repeated nr. times)

            // printFloat( pw5, 47, 1f );         // pixel size
            // printInt( pw5, 98, 2 );            // nr. seed points
            // printXYZ( pw5, 0f, 0f, 0f );
            // printXYZ( pw5, 0f, 0f, 0f );
            // printInt( pw5, 451, 0 );
            // printFloat( pw5, 460, 0f );
            // printFloat( pw5, 461, 0f );
            // printInt( pw5, 452, 1 );
            // printFloat( pw5, 462, 1f );
            // printInt( pw5, 453, 2 );
            // printFloat( pw5, 463, 0f );
            // printFloat( pw5, 463, 1f );
            // printString( pw5, 470, "LINEAR" );    
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            // FIXME point scale factor is 0.3
            DrawingPointPath point = (DrawingPointPath) path;
            String block = "P_" + DrawingBrushPaths.getPointThName( point.mPointType ).replace(':','-');
            // int idx = 1 + point.mPointType;
            printString( pw5, 0, "INSERT" );
            ++handle; printAcDb( pw5, handle, "AcDbBlockReference" );
            printString( pw5, 8, "POINT" );
            printString( pw5, 2, block );
            printFloat( pw5, 41, POINT_SCALE );
            printFloat( pw5, 42, POINT_SCALE );
            printFloat( pw5, 50, 360-(float)(point.mOrientation) );
            printXYZ( pw5, point.cx * scale, -point.cy * scale, 0.0f );
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
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "DXF io-exception " + e.toString() );
    }
  }

}

