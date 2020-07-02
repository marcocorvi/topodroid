/* @file DrawingShp.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid drawing: shapefile export
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.shp.ShpObject;
import com.topodroid.shp.ShpPoint;
import com.topodroid.shp.ShpPolyline;
import com.topodroid.shp.ShpStation;
import com.topodroid.shp.ShpSegment;
// import com.topodroid.prefs.TDSetting;

// import android.util.Log;


import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

class DrawingShp
{
  // @param basepath   TopoDroid/shp/survey-plot
  // @return true if successful
  static boolean writeShp( String basepath, DrawingCommandManager plot, long type, GeoReference station )
  {

    File dir   = null;
    double xoff = 0;
    double yoff = 0;
    double xscale = ShpObject.SCALE;
    double yscale = ShpObject.SCALE;
    if ( station != null ) {
      xoff = station.e;
      yoff = station.s;
      xscale = ShpObject.SCALE * station.eradius;
      yscale = ShpObject.SCALE * station.sradius;
    }

    try {
      dir = new File( basepath );
      if ( ! dir.exists() && ! dir.mkdir() ) {
        TDLog.Error("mkdir error");
        return false;
      }
      ArrayList< File > files = new ArrayList<>();

      // centerline data: shepafile of segments (fields: type, fron, to)
      // xoff+sh.x1, yoff+sh.y1  --  xoff+sh.x2, yoff+sh.y2
      ArrayList< DrawingPath > shots = new ArrayList<>();
      if ( PlotInfo.isSketch2D( type ) ) { 
        for ( DrawingPath sh : plot.getLegs() ) {
          if ( sh.mBlock != null ) shots.add( sh );
        }
        for ( DrawingPath sh : plot.getSplays() ) {
          if ( sh.mBlock != null ) shots.add( sh );
        }
      }
      ShpSegment shp_shot = new ShpSegment( basepath + "/shot", files );

      shp_shot.writeSegments( shots, xoff, yoff, xscale, yscale );

      // points shapefile
      ArrayList< DrawingPointPath > points     = new ArrayList<>();
      ArrayList< DrawingPointLinePath > lines  = new ArrayList<>();
      ArrayList< DrawingPointLinePath > areas  = new ArrayList<>();
      for ( ICanvasCommand cmd : plot.getCommands() ) {
        if ( cmd.commandType() != 0 ) continue;
        DrawingPath path = (DrawingPath)cmd;
        if ( path.mType == DrawingPath.DRAWING_PATH_POINT ) {
          points.add( (DrawingPointPath)path ); // xoff+cx, yoff+cy
        } else if ( path.mType == DrawingPath.DRAWING_PATH_LINE ) {
          lines.add( (DrawingLinePath)path );  // xoff+pt.x, yoff+pt.y
        } else if ( path.mType == DrawingPath.DRAWING_PATH_AREA ) {
          areas.add( (DrawingAreaPath)path );
        }
      }
      ShpPoint shp_point = new ShpPoint( basepath + "/point", files );
      shp_point.writePoints( points, xoff, yoff, xscale, yscale );
      ShpPolyline shp_line = new ShpPolyline( basepath + "/line", DrawingPath.DRAWING_PATH_LINE, files );
      shp_line.writeLines( lines, xoff, yoff, xscale, yscale );
      ShpPolyline shp_area = new ShpPolyline( basepath + "/area", DrawingPath.DRAWING_PATH_AREA, files );
      shp_area.writeAreas( areas, xoff, yoff, xscale, yscale );

      // stations: xoff+name.cx, yoff+name.cy
      List< DrawingStationName > stations = plot.getStations();
      ShpStation shp_station = new ShpStation( basepath + "/station", files );
      shp_station.writeStations( stations, xoff, yoff, xscale, yscale );

      // Log.v("DistoX", "SHP export stations " + stations.size() + " points " + points.size() );
      
      Archiver zipper = new Archiver( );
      zipper.compressFiles( basepath + ".shz", files );
      TDUtil.deleteDir( basepath ); // delete temporary shapedir

    } catch ( IOException e ) {
      TDLog.Error( "SHP io-exception " + e.getMessage() );
      return false;
    } finally {
      TDUtil.deleteDir( dir );
    }
    return true;
  }

}


