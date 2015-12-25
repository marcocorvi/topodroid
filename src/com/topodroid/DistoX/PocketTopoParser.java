/** @file PocketTopoParser.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid PocketTopo parser
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import android.util.Log;

public class PocketTopoParser extends ImportParser
{
  String mTitle = "";
  String mComment;
  String mStartFrom;

  static final float PT_SCALE = 0.1f; // 100/1000

  public PocketTopoParser( String filename, String surveyname, boolean apply_declination )
                           throws ParserException
  {
    super( apply_declination );
    String mStartFrom = null;
    // TDLog.Log( TDLog.LOG_PTOPO, "PocketTopo parser " + surveyname );
    mName     = surveyname.replace(".top", "");
    readPocketTopoFile( filename );
  }

  private void readPocketTopoFile( String filename ) throws ParserException
  {
    PTFile ptfile = new PTFile();
    TDLog.Log( TDLog.LOG_PTOPO, "PT survey " + mName + " read file " + filename );
    // Log.v( "PTDistoX", "PT survey " + mName + " read file " + filename );
    try {
      FileInputStream fs = new FileInputStream( filename );
      ptfile.read( fs );
      fs.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "File not found: " + filename );
      // FIXME
      return;
    } catch ( IOException e ) { // on close
      TDLog.Error( "IO exception: " + e );
      return;
    }
    int nr_trip = ptfile.tripCount();
    TDLog.Log( TDLog.LOG_PTOPO, "PT trip count " + nr_trip );
    mComment = "";
    // mTeam = "";
    if ( nr_trip > 0 ) { // use only the first trip
      PTTrip trip = ptfile.getTrip(0);
      mDate = String.format( "%04d-%02d-%02d", trip._year, trip._month, trip._day );
      if ( trip.hasComment() ) mComment = trip.comment();
      // trip.declination(); NOT USED
      // TODO create a survey
    } else {
      mDate = TopoDroidUtil.currentDate();
    }

    int shot_count = ptfile.shotCount();
    // Log.v("PTDistoX", "PT nr shots " + shot_count );
    int extend = DistoXDBlock.EXTEND_NONE;
    int ext_flag = extend;
    // DistoXDBlock b     = null;  // temporary block pointer
    // DistoXDBlock start = null;  // first block inserted
    // DistoXDBlock last  = null;  // last block on the list

    String from_prev = "";
    String to_prev   = "";
    // Pattern pattern = Pattern.compile( "0+" );
    // ArrayList< DistoXDBlock > data = new ArrayList< DistoXDBlock >();

    for ( int s=0; s < shot_count; ++s ) {
      PTShot shot = ptfile.getShot(s);
      String from = shot.from().toString();
      String to   = shot.to().toString();
      float da = shot.distance();
      float ba = shot.azimuth();
      float ca = shot.inclination();
      float ra = shot.roll();
      // Log.v("PTDistoX", "shot " + from + "-" + to + ": " + da + " " + ba + " " + ca );
      from = from.replaceAll( "^0+", "" );
      to   = to.replaceAll( "^0+", "" );
      if ( from.equals("-") ) from = "";
      if ( to.equals("-") )   to = "";
      if ( from.equals( from_prev ) && to.equals( to_prev ) && ! to_prev.equals("") ) {
        from = "";
        to   = "";
      } else {
        from_prev = from;
        to_prev   = to;
      }
      if ( shot.isFlipped() ) {
        if ( extend != DistoXDBlock.EXTEND_LEFT ) {
          extend = DistoXDBlock.EXTEND_LEFT;
          ext_flag = extend;
        } else {
          ext_flag = DistoXDBlock.EXTEND_NONE;
        }
      } else {
        if ( extend != DistoXDBlock.EXTEND_RIGHT ) {
          extend = DistoXDBlock.EXTEND_RIGHT;
          ext_flag = extend;
        } else {
          ext_flag = DistoXDBlock.EXTEND_NONE;
        }
      }
      shots.add( new ParserShot( from, to,  da, ba, ca, ra, extend, false, false, false,
                                 shot.hasComment()? shot.comment() : "" ) );
      if ( mStartFrom == null && from.length() > 0 && to.length() > 0 ) {
        mStartFrom = from;
      }
      // if ( from.length() > 0 && to.length() > 0 ) {
      //   data.add( new DistoXDBlock( from, to,  da, ba, ca, ra, extend, DistoXDBlock.BLOCK_MAIN_LEG ) );
      // }
    }
    TDLog.Log( TDLog.LOG_PTOPO, "PT parser shot count " + shot_count + " size " + shots.size() );

    // Log.v("PTDistoX", "start from " + mStartFrom );
    // DistoXNum num = new DistoXNum( data, mStartFrom, null, null );
    // Log.v("DistoX", "Num E " + (20*num.surveyEmin()) + " " + (20*num.surveyEmax()) +
    //                 " S " + (20*num.surveySmin()) + " " + (20*num.surveySmax()) +
    //                 " H " + (20*num.surveyHmin()) + " " + (20*num.surveyHmax()) +
    //                 " V " + (20*num.surveyVmin()) + " " + (20*num.surveyVmax()) );
    
 
    // FIXME PT parser uses therion scrap syntax
    if ( mStartFrom != null ) {
      // NumStation st = num.getStation( mStartFrom );
      // Log.v("PTDistoX", " start " + st.e + " " + st.s );

      PTDrawing outline = ptfile.getOutline();
      String filename1 = TDPath.getTh2File( mName + "-1p.th2" );
      writeDrawing( filename1, outline, PlotInfo.PLOT_PLAN, 5*DrawingUtil.CENTER_X, 5*DrawingUtil.CENTER_Y );

      PTDrawing sideview = ptfile.getSideview();
      String filename2 = TDPath.getTh2File( mName + "-1s.th2" );
      writeDrawing( filename2, sideview, PlotInfo.PLOT_EXTENDED, 5*DrawingUtil.CENTER_X, 5*DrawingUtil.CENTER_Y );
      // Log.v("DistoX", "display " + TopoDroidApp.mDisplayWidth + " " + TopoDroidApp.mDisplayHeight ); 
    } else {
      TDLog.Error( "PT null StartFrom");
    }
    
  }

  final static float FCT = 0.0f;
  /** return true if successful
   */
  private boolean writeDrawing( String filename, PTDrawing drawing, long type, float xoff, float yoff )
  {
    if ( drawing == null ) return false;
    int elem_count = drawing.elementNumber();
    // Log.v( "PTDistoX", "off " + xoff + " " + yoff );
    TDLog.Log( TDLog.LOG_PTOPO, "Therion file " + filename + " elems " + elem_count );

    TDPath.checkPath( filename );
    File file = new File( filename );
    boolean ret = false;
    synchronized( TDPath.mTherionLock ) {
      try {
        FileWriter fw = new FileWriter( file );
        PrintWriter pw = new PrintWriter( fw );

        if ( type == PlotInfo.PLOT_PLAN ) {
          pw.format("scrap 1p -proj plan ");
        } else {
          pw.format("scrap 1s -proj extended ");
        }
        pw.format("[0 0 1 0 0.0 0.0 1.0 0.0 m]\n");

        PTMapping mapping = drawing.mapping();
        int scale = mapping.scale();
        int x0 = (mapping.origin().x());
        int y0 = (mapping.origin().y());
        // Log.v("PTDistoX", "map origin " + x0 + " " + y0 + " elements " + elem_count );
        x0 *= FCT;
        y0 *= FCT;

        if ( elem_count > 0 ) {
          for (int h=0; h<elem_count; ++h ) {
            try {
              PTPolygonElement elem = (PTPolygonElement)drawing.getElement(h);
              int point_count = elem.pointCount();
              int col = elem.getColor();
              if ( point_count > 1 ) {
                PTPoint point = elem.point(0);
                // add a line to the plotCanvas
                pw.format("line %s\n", PtCmapActivity.getLineThName(col) );
                int k=0;
                int x1 =   (int)( xoff + PT_SCALE*(point.x() - x0));
                int y1 = - (int)( yoff + PT_SCALE*(point.y() - y0));
                // FIXME drawer->insertLinePoint( x1, y1, type, canvas );
                pw.format("  %d %d \n", x1, y1 );
                // Log.v("PTDistoX", "elem " + h + ":0 " + x1 + " " + y1 + " point " + point.x() + " " + point.y() );

                for (++k; k<point_count; ++k ) {
                  point = elem.point(k);
                  int x =   (int)( xoff + PT_SCALE*(point.x() - x0) );
                  int y = - (int)( yoff + PT_SCALE*(point.y() - y0) );
                  if ( Math.abs(x - x1) >= 4 || Math.abs(y - y1) >= 4 ) {
                    x1 = x;
                    y1 = y;
                    // FIXME drawer->insertLinePoint( x, y, type, canvas );
                    pw.format("  %d %d \n", x1, y1 );
                    // Log.v("PTDistoX", "elem " + h + ":" + k + " " + x1 + " " + y1 + " point " + point.x() + " " + point.y() );
                  }
                }
                // FIXME drawer->insertLinePoint( x1, y1, type, canvas ); // close the line
                // FIXME pw.format("  %d %d \n", x1, y1 );
                pw.format("endline\n");
              } else if ( point_count == 1 ) {
                PTPoint point = elem.point(0);
                int x =   (int)( xoff + PT_SCALE*(point.x() - x0) );
                int y = - (int)( yoff + PT_SCALE*(point.y() - y0) );
                // FIXME drawer->insertPoint(x, y, type, canvas );
                pw.format("point %d %d %s \n", x, y, PtCmapActivity.getPointThName(col) );
                // Log.v("PTDistoX", "elem " + h + " single " + x + " " + y );
              }
            } catch( ClassCastException e ) {
            }
          }
        }
        pw.format("endscrap\n");
        fw.flush();
        fw.close();
        ret = true;
      } catch ( IOException e ) {
        TDLog.Error( mName + " scraps IO error " + e );
        file.delete();
      }
    }
    return ret;
  }


}
