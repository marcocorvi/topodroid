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
 * CHANGES
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
// import android.util.Log;

class DrawingDxf
{
  private static int VERSION = 9;
  private static String ACAD_VERSION = "AC1009";

  private static final float grad2rad = TopoDroidUtil.GRAD2RAD;

  static void writeComment( BufferedWriter out, String comment ) throws IOException
  {
    out.write( "  999\n" + comment + "\n" );
  }

  static void writeHex( BufferedWriter out, int code, int handle ) throws IOException
  {
    if ( VERSION >= 13 ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw  = new PrintWriter(sw);
      pw.printf("  %d\n%d\n", code, handle );
      out.write( sw.getBuffer().toString() );
    }
  }

  static void printHex( PrintWriter pw, int code, int handle )
  {
    if ( VERSION >= 13 ) {
      pw.printf("  %d\n%d\n", code, handle );
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

  static void printAcDb( PrintWriter pw, int hex, String acdb1, String acdb2 )
  {
    if ( VERSION >= 13 ) {
      if ( hex >= 0 ) printHex( pw, 5, hex );
      pw.printf("  100\n" + acdb1 + "\n100\n" + acdb2 + "\n" );
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
    writeInt(out, 70, num );
  }
  
  static void writeEndTable(  BufferedWriter out ) throws IOException
  {
    writeString( out, 0, "ENDTAB");
  }

  static void printLayer( PrintWriter pw2, int handle, String name, int flag, int cnt, int color, String style )
  {
    printString( pw2, 0, "LAYER" );
    printAcDb( pw2, handle, "AcDbSymbolTableRecord", "AcDbLayerTableRecord");
    printString( pw2, 2, name );
    printInt( pw2, 70, flag );
    printInt( pw2, 62, color );
    printString( pw2, 6, style );
    // printInt( pw2, 370, 100 );
    // printString( pw2, 390, "F" );
  }

  static void write( BufferedWriter out, DistoXNum num, DrawingCommandManager plot, long type )
  {
    int handle = 0;
    float xmin=10000f, xmax=-10000f, 
          ymin=10000f, ymax=-10000f;
    for ( DrawingPath p : plot.mCurrentStack ) {
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

    try {
      // header
      writeComment( out, "DXF created by TopoDroid v. " + TopoDroidApp.VERSION );
      writeSection( out, "HEADER" );

      xmin -= 2f;
      ymax += 2f;

      writeString( out, 9, "$ACADVER" );
      writeString( out, 1, ACAD_VERSION );
      writeString( out, 9, "$INSBASE" );
      {
        StringWriter sw1 = new StringWriter();
        PrintWriter pw1  = new PrintWriter(sw1);
        printXYZ( pw1, 0.0f, 0.0f, 0.0f ); // FIXME (0,0,0)
        printString( pw1, 9, "$EXTMIN" );
        printXYZ( pw1, xmin, -ymax, 0.0f );
        printString( pw1, 9, "$EXTMAX" );
        printXYZ( pw1, xmax, -ymin, 0.0f );
        out.write( sw1.getBuffer().toString() );
      }
      writeEndSection( out );
      
      writeSection( out, "TABLES" );
      {
        writeBeginTable( out, "LTYPE", 5, 1 );
        {
          // int flag = 64;
          writeString( out, 0, "LTYPE" );
          writeAcDb( out, 14, "AcDbSymbolTableRecord", "AcDbLinetypeTableRecord" );
          writeString( out, 2, "CONTINUOUS" );
          writeInt( out, 70, 64 );
          writeString( out, 3, "Solid line" );
          writeInt( out, 72, 65 );
          writeInt( out, 73, 0 );
          writeString( out, 40, "0.0" );
        }
        writeEndTable( out );

        writeBeginTable( out, "LAYER", 2, 7 );
        {
          StringWriter sw2 = new StringWriter();
          PrintWriter pw2  = new PrintWriter(sw2);

          // 2 layer name, 70 flag (64), 62 color code, 6 line style
          String style = "CONTINUOUS";
          int flag = 0;

          handle = 40;
          ++handle; printLayer( pw2, handle, "LEG",     flag, 1, 1, style );
          ++handle; printLayer( pw2, handle, "SPLAY",   flag, 2, 2, style );
          ++handle; printLayer( pw2, handle, "STATION", flag, 3, 3, style );
          ++handle; printLayer( pw2, handle, "LINE",    flag, 4, 4, style );
          ++handle; printLayer( pw2, handle, "POINT",   flag, 5, 5, style );
          ++handle; printLayer( pw2, handle, "AREA",    flag, 6, 6, style );
          ++handle; printLayer( pw2, handle, "REF",     flag, 7, 7, style );

          out.write( sw2.getBuffer().toString() );
        }
        writeEndTable( out );

        writeBeginTable( out, "VIEW", 6, 0 );
        writeEndTable( out );

        writeBeginTable( out, "UCS", 6, 0 );
        writeEndTable( out );
        
        writeBeginTable( out, "STYLE", 7, 0 );
        writeEndTable( out );

        writeBeginTable( out, "APPID", 9, 1 );
        {
          writeString( out, 0, "APPID" );
          writeAcDb( out, 12, "AcDbSymbolTableRecord", "AcDbRepAppTableRecord" );
          writeString( out, 2, "ACAD" );
          writeInt( out, 70, 0 );
        }
        writeEndTable( out );
      }
      writeEndSection( out );
      out.flush();
      
      writeSection( out, "BLOCKS" );
      {
        // // 8 layer (0), 2 block name,
        for ( int n = 0; n < DrawingBrushPaths.mPointLib.mAnyPointNr; ++ n ) {
          SymbolPoint pt = DrawingBrushPaths.mPointLib.getAnyPoint(n);

          int block = 1+n; // block_name = 1 + therion_code
          writeString( out, 0, "BLOCK" );
          ++handle; 
          writeAcDb( out, handle, "AcDbEntity");
          writeString( out, 8, "POINT" );
          writeComment( out, pt.mName );
          writeAcDb( out, -1, "AcDbBlockBegin");
          writeInt( out, 2, block );
          writeInt( out, 70, 64 );
          writeString( out, 10, "0.0" );
          writeString( out, 20, "0.0" );
          writeString( out, 30, "0.0" );

          out.write( pt.getDxf() );
          // out.write( DrawingBrushPaths.mPointLib.getPoint(n).getDxf() );

          writeString( out, 0, "ENDBLK" );
          if ( VERSION >= 13 ) {
            ++handle; 
            writeAcDb( out, handle, "AcDbEntity");
            writeString( out, 8, "POINT" );
            writeAcDb( out, -1, "AcDbBlockEnd");
          }
        }
      }
      writeEndSection( out );
      out.flush();
      
      writeSection( out, "ENTITIES" );
      {
        float SCALE_FIX = DrawingActivity.SCALE_FIX;

        // reference
        {
          StringWriter sw9 = new StringWriter();
          PrintWriter pw9  = new PrintWriter(sw9);
          printString( pw9, 0, "LINE" );
          printString( pw9, 8, "REF" );
          printAcDb( pw9, -1, "AcDbEntity", "AcDbLine" );
          printXYZ( pw9, xmin, -ymax, 0.0f );
          printXYZ1( pw9, xmin+10*SCALE_FIX, -ymax, 0.0f );
          out.write( sw9.getBuffer().toString() );
        }
        {
          StringWriter sw8 = new StringWriter();
          PrintWriter pw8  = new PrintWriter(sw8);
          printString( pw8, 0, "LINE" );
          printString( pw8, 8, "REF" );
          printAcDb( pw8, -1, "AcDbEntity", "AcDbLine" );
          printXYZ( pw8, xmin, -ymax, 0.0f );
          printXYZ1( pw8,  xmin, -ymax+10*SCALE_FIX, 0.0f );
          out.write( sw8.getBuffer().toString() );
        }
        {
          StringWriter sw7 = new StringWriter();
          PrintWriter pw7  = new PrintWriter(sw7);
          printString( pw7, 0, "TEXT" );
          printString( pw7, 8, "REF" );
          printAcDb( pw7, -1, "AcDbEntity", "AcDbText" );
          // pw7.printf("%s\n  0\n", "\"10\"" );
          printXYZ( pw7, xmin+10*SCALE_FIX+1, -ymax, 0.0f );
          printFloat( pw7, 40, 0.3f );
          printString( pw7, 1, "\"10\"" );
          out.write( sw7.getBuffer().toString() );
        }
        {
          StringWriter sw6 = new StringWriter();
          PrintWriter pw6  = new PrintWriter(sw6);
          printString( pw6, 0, "TEXT" );
          printString( pw6, 8, "REF" );
          printAcDb( pw6, -1, "AcDbEntity", "AcDbText" );
          // pw6.printf("%s\n  0\n", "\"10\"" );
          printXYZ( pw6, xmin, -ymax+10*SCALE_FIX+1, 0.0f );
          printFloat( pw6, 40, 0.3f );
          printString( pw6, 1, "\"10\"" );
          out.write( sw6.getBuffer().toString() );
        }
        out.flush();

        // centerline data
        for ( DrawingPath sh : plot.mFixedStack ) {
          DistoXDBlock blk = sh.mBlock;
          if ( blk == null ) continue;

          StringWriter sw4 = new StringWriter();
          PrintWriter pw4  = new PrintWriter(sw4);
          if ( sh.mType == DrawingPath.DRAWING_PATH_FIXED ) {
            NumStation f = num.getStation( blk.mFrom );
            NumStation t = num.getStation( blk.mTo );
 
            printString( pw4, 0, "LINE" );
            printString( pw4, 8, "LEG" );
            printAcDb( pw4, -1, "AcDbEntity", "AcDbLine" );

            if ( type == PlotInfo.PLOT_PLAN ) {
              float x =  DrawingActivity.toSceneX( f.e );
              float y =  DrawingActivity.toSceneY( f.s );
              float x1 = DrawingActivity.toSceneX( t.e );
              float y1 = DrawingActivity.toSceneY( t.s );
              printXYZ( pw4, x, -y, 0.0f );
              printXYZ1( pw4, x1, -y1, 0.0f );
            } else if ( type == PlotInfo.PLOT_EXTENDED ) {
              float x =  DrawingActivity.toSceneX( f.h );
              float y =  DrawingActivity.toSceneY( f.v );
              float x1 = DrawingActivity.toSceneX( t.h );
              float y1 = DrawingActivity.toSceneY( t.v );
              printXYZ( pw4, x, -y, 0.0f );
              printXYZ1( pw4, x1, -y1, 0.0f );
            } else if ( type == PlotInfo.PLOT_SECTION ) {
              // nothing
            }
          } else if ( sh.mType == DrawingPath.DRAWING_PATH_SPLAY ) {
            NumStation f = num.getStation( blk.mFrom );

            printString( pw4, 0, "LINE" );
            printString( pw4, 8, "SPLAY" );
            printAcDb( pw4, -1, "AcDbEntity", "AcDbLine" );

            float dh = blk.mLength * FloatMath.cos( blk.mClino * grad2rad )*SCALE_FIX;
            if ( type == PlotInfo.PLOT_PLAN ) {
              float x = DrawingActivity.toSceneX( f.e );
              float y = DrawingActivity.toSceneY( f.s );
              float de =   dh * FloatMath.sin( blk.mBearing * grad2rad);
              float ds = - dh * FloatMath.cos( blk.mBearing * grad2rad);
              printXYZ( pw4, x, -y, 0.0f );
              printXYZ1( pw4, x + de, -(y+ds), 0.0f );
            } else if ( type == PlotInfo.PLOT_EXTENDED ) {
              float x = DrawingActivity.toSceneX( f.h );
              float y = DrawingActivity.toSceneY( f.v );
              float dv = - blk.mLength * FloatMath.sin( blk.mClino * grad2rad )*SCALE_FIX;
              printXYZ( pw4, x, -y, 0.0f );
              printXYZ1( pw4, x+dh*blk.mExtend, -(y+dv), 0.0f );
            } else if ( type == PlotInfo.PLOT_SECTION ) {
              // nothing
            }
          }
          out.write( sw4.getBuffer().toString() );
          out.flush();
        }

        // FIXME station scale is 0.3
        float POINT_SCALE = 10.0f;
        for ( DrawingPath path : plot.mCurrentStack ) {
          StringWriter sw5 = new StringWriter();
          PrintWriter pw5  = new PrintWriter(sw5);
          if ( path.mType == DrawingPath.DRAWING_PATH_STATION ) {
            DrawingStationPath st = (DrawingStationPath)path;

            printString( pw5, 0, "TEXT" );
            printString( pw5, 8, "STATION" );
            if ( VERSION >= 13 ) {
              printAcDb( pw5, -1, "AcDbEntity", "AcDbText" );
              pw5.printf("%s\n  0\n", st.mName );
            }
            printXYZ( pw5, st.mXpos, -st.mYpos, 0.0f );
            printFloat( pw5, 40, POINT_SCALE );
            printString( pw5, 1, st.mName );
          } else if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
            String layer = "LINE";
            int flag = 0;
            DrawingLinePath line = (DrawingLinePath) path;
            printString( pw5, 0, "POLYLINE" );
            printString( pw5, 8, layer );
            printAcDb( pw5, -1, "AcDbEntity", "AcDbPolyline" );
            printInt( pw5, 70, flag );
            // ArrayList< LinePoint > points = line.mPoints;
            // for ( LinePoint p : points ) 
            for (LinePoint p = line.mFirst; p != null; p = p.mNext ) { 
              printString( pw5, 0, "VERTEX" );
              printString( pw5, 8, layer );
              printXYZ( pw5, p.mX, -p.mY, 0.0f );
            }
            printString( pw5, 0, "SEQEND" );
          } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath area = (DrawingAreaPath) path;
            printString( pw5, 0, "HATCH" );
            printString( pw5, 8, "AREA" );
            printInt( pw5, 91, 1 );
            printInt( pw5, 93, area.size() );
            // ArrayList< LinePoint > points = area.mPoints;
            // printInt( pw5, 93, points.size() );
            // for ( LinePoint p : points ) 
            for (LinePoint p = area.mFirst; p != null; p = p.mNext ) { 
              printXYZ( pw5, p.mX, -p.mY, 0.0f );
            }
          } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
            // FIXME point scale factor is 0.3
            DrawingPointPath point = (DrawingPointPath) path;
            int idx = 1 + point.mPointType;
            printString( pw5, 0, "INSERT" );
            printString( pw5, 8, "POINT" );
            printInt( pw5, 2, idx );
            printFloat( pw5, 41, POINT_SCALE );
            printFloat( pw5, 42, POINT_SCALE );
            printXYZ( pw5, point.cx, -point.cy, 0.0f );
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

