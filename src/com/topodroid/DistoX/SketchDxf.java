/** @file SketchDxf.java
 *
 * @author marco corvi
 * @date apr 2013
 *
 * @brief TopoDroid 3d sketch DXF export
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130416 created
 * 20130831 added join(s) to the export
 */
package com.topodroid.DistoX;

// import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.Locale;

import java.util.ArrayList;
import java.util.HashMap;

class SketchDxf
{



  static void write( PrintWriter out, String fullname, SketchModel model )
  {
    DistoXNum num = model.mNum;
    // header
    out.printf("999\nDXF created from TopoDroid\n");
    out.printf("0\nSECTION\n2\nHEADER\n");
      out.printf("9\n$ACADVER\n1\nAC1006\n");
      out.printf("9\n$INSBASE\n");
      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", 0.0, 0.0, 0.0 ); // FIXME (0,0,0)
      out.printf("9\n$EXTMIN\n");
      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n",
        num.surveyEmin(), num.surveySmin(), num.surveyVmin() );
      out.printf("9\n$EXTMAX\n");
      out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n",
        num.surveyEmax(), num.surveySmax(), num.surveyVmax() );
    out.printf("0\nENDSEC\n");
    
    out.printf("0\nSECTION\n2\nTABLES\n");
    {
      out.printf("0\nTABLE\n2\nLTYPE\n70\n1\n");
      // int flag = 64;
      out.printf("0\nLTYPE\n2\nCONTINUOUS\n70\n64\n3\nSolid line\n72\n65\n73\n0\n40\n0.0\n");
      out.printf("0\nENDTAB\n");
      
      out.printf("0\nTABLE\n2\nLAYER\n70\n6\n");
        // 2 layer name, 70 flag (64), 62 color code, 6 line style
        String style = "CONTINUOUS";
        int flag = 64;
        out.printf("0\nLAYER\n2\nLEG\n70\n%d\n62\n%d\n6\n%s\n",     flag, 1, style );
        out.printf("0\nLAYER\n2\nSPLAY\n70\n%d\n62\n%d\n6\n%s\n",   flag, 2, style );
        out.printf("0\nLAYER\n2\nSTATION\n70\n%d\n62\n%d\n6\n%s\n", flag, 3, style );
        out.printf("0\nLAYER\n2\nLINE\n70\n%d\n62\n%d\n6\n%s\n",    flag, 4, style );
        out.printf("0\nLAYER\n2\nPOINT\n70\n%d\n62\n%d\n6\n%s\n",   flag, 5, style );
        out.printf("0\nLAYER\n2\nAREA\n70\n%d\n62\n%d\n6\n%s\n",    flag, 6, style );
        out.printf("0\nLAYER\n2\nSURFACE\n70\n%d\n62\n%d\n6\n%s\n", flag, 7, style );
        out.printf("0\nLAYER\n2\nJOIN\n70\n%d\n62\n%d\n6\n%s\n",    flag, 8, style );
      out.printf("0\nENDTAB\n");
      
      out.printf("0\nTABLE\n2\nSTYLE\n70\n0\n");
      out.printf("0\nENDTAB\n");
    }
    out.printf("0\nENDSEC\n");
    
    out.printf("0\nSECTION\n2\nBLOCKS\n");
    {
      // // 8 layer (0), 2 block name,
      for ( int n = 0; n < DrawingBrushPaths.mPointLib.mAnyPointNr; ++ n ) {
        int block = 1+n; // block_name = 1 + therion_code
        out.printf("0\nBLOCK\n8\nPOINT\n2\n%d\n70\n64\n10\n0.0\n20\n0.0\n30\n0.0\n", block );
        out.printf( DrawingBrushPaths.mPointLib.getAnyPoint(n).getDxf() );
        out.printf("0\nENDBLK\n");
      }
    }
    out.printf("0\nENDSEC\n");
    
    out.printf("0\nSECTION\n2\nENTITIES\n");
    {
      // centerline data
      for ( NumShot sh : num.getShots() ) {
        NumStation f = sh.from;
        NumStation t = sh.to;
        out.printf("0\nLINE\n8\nLEG\n");
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, f.s, f.v );
        out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", t.e, t.s, t.v );
      }

      for ( NumSplay sh : num.getSplays() ) {
        NumStation f = sh.from;
        out.printf("0\nLINE\n8\nSPLAY\n");
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", f.e, f.s, f.v );
        out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", sh.e, sh.s, sh.v );
      }

      for ( NumStation st : num.getStations() ) {
        // FIXME station scale is 0.3
        out.printf("0\nTEXT\n8\nSTATION\n");
        out.printf("1\n%s\n", st.name );
        out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n40\n0.3\n", st.e, st.s, st.v );
      }

      for ( SketchPath path : model.mPaths ) {
        if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
          String layer = "LINE";
          int flag = 0;

          SketchLinePath line = (SketchLinePath) path;
          ArrayList< Vector > points = line.mLine.points;
          // ArrayList< Vector > points = line.mPts3D;
          out.printf("0\nPOLYLINE\n8\n%s\n70\n%d\n", layer, flag );
          for ( Vector p : points ) {
            out.printf("0\nVERTEX\n8\n%s\n", layer );
            out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", p.x, p.y, p.z );
          }
          out.printf("0\nSEQEND\n");
        } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
          SketchLinePath line = (SketchLinePath) path;
          // ArrayList< Vector > points = line.mLine.points;
          ArrayList< Vector > points = line.mPts3D;
          out.printf("0\nHATCH\n8\nAREA\n91\n1\n" );
          out.printf("93\n%d\n", points.size() );
          for ( Vector p : points ) {
            out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", p.x, p.y, p.z );
          }
        } else if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
          // FIXME point scale factor is 0.3
          SketchPointPath point = (SketchPointPath) path;
          int idx = 1 + point.mThType;
          out.printf("0\nINSERT\n8\nPOINT\n2\n%d\n41\n0.3\n42\n0.3\n", idx);
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", point.mXpos, point.mYpos, point.mZpos );
        }
      }
    
      for ( SketchSurface sf : model.mSurfaces ) {
        HashMap< Integer, SketchVertex > vts = sf.mVertices;
        // for ( SketchBorder brd : sf.borders ) {
        //   for ( SketchSide s : brd.sides ) {
        //     SketchVertex v1 = vts.get( s.v1 );
        //     SketchVertex v2 = vts.get( s.v2 );
        //     out.printf("0\nLINE\n8\nBORDER\n" );
        //     out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", v1.x, v1.y, v1.z );
        //     out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", v2.x, v2.y, v2.z );
        //   }
        // }
        ArrayList< SketchTriangle > tris = sf.mTriangles;
        for ( SketchTriangle tri : tris ) {
          SketchVertex v1 = vts.get( tri.i );
          SketchVertex v2 = vts.get( tri.j );
          SketchVertex v3 = vts.get( tri.k );
          out.printf("0\n3DFACE\n8\nSURFACE\n");
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", v1.x, v1.y, v1.z );
          out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", v2.x, v2.y, v2.z );
          out.printf(Locale.ENGLISH, "12\n%.2f\n22\n%.2f\n32\n%.2f\n", v3.x, v3.y, v3.z );
          out.printf(Locale.ENGLISH, "13\n%.2f\n23\n%.2f\n33\n%.2f\n", v3.x, v3.y, v3.z );
        }
      }

      for ( SketchSurface sf : model.mJoins ) {
        HashMap< Integer, SketchVertex > vts = sf.mVertices;
        // for ( SketchBorder brd : sf.borders ) {
        //   for ( SketchSide s : brd.sides ) {
        //     SketchVertex v1 = vts.get( s.v1 );
        //     SketchVertex v2 = vts.get( s.v2 );
        //     out.printf("0\nLINE\n8\nBORDER\n" );
        //     out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", v1.x, v1.y, v1.z );
        //     out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", v2.x, v2.y, v2.z );
        //   }
        // }
        ArrayList< SketchTriangle > tris = sf.mTriangles;
        for ( SketchTriangle tri : tris ) {
          SketchVertex v1 = vts.get( tri.i );
          SketchVertex v2 = vts.get( tri.j );
          SketchVertex v3 = vts.get( tri.k );
          out.printf("0\n3DFACE\n8\nJOIN\n");
          out.printf(Locale.ENGLISH, "10\n%.2f\n20\n%.2f\n30\n%.2f\n", v1.x, v1.y, v1.z );
          out.printf(Locale.ENGLISH, "11\n%.2f\n21\n%.2f\n31\n%.2f\n", v2.x, v2.y, v2.z );
          out.printf(Locale.ENGLISH, "12\n%.2f\n22\n%.2f\n32\n%.2f\n", v3.x, v3.y, v3.z );
          out.printf(Locale.ENGLISH, "13\n%.2f\n23\n%.2f\n33\n%.2f\n", v3.x, v3.y, v3.z );
        }
      }

    }
    out.printf("0\nENDSEC\n");

    out.printf("0\nEOF\n");
  }

}

