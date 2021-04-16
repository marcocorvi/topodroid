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
package com.topodroid.io.shp;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDMath;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;
import com.topodroid.num.NumStation;
import com.topodroid.DistoX.Archiver;
import com.topodroid.DistoX.GeoReference;
import com.topodroid.DistoX.ICanvasCommand;
import com.topodroid.DistoX.DrawingPath;
import com.topodroid.DistoX.DrawingPointPath;
import com.topodroid.DistoX.DrawingPointLinePath;
import com.topodroid.DistoX.DrawingLinePath;
import com.topodroid.DistoX.DrawingAreaPath;
import com.topodroid.DistoX.DrawingCommandManager;
import com.topodroid.DistoX.DrawingStationName;

import android.util.Log;

import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

public class DrawingShp
{
  // @param basepath   TopoDroid/shp/survey-plot
  // @param plot       sketch items
  // @param type       sketch type
  // @param station    WGS84 data of the sketch origin 
  // @return true if successful
  public static boolean writeShp( String basepath, DrawingCommandManager plot, long type, GeoReference station )
  {

    File dir   = null;
    double xoff = 0;
    double yoff = 0;
    double xscale = ShpObject.SCALE;
    double yscale = ShpObject.SCALE;
    float cd = 1;
    float sd = 0;
    if ( station != null && TDSetting.mShpGeoref ) {
      xoff = station.ge;
      yoff = station.gs;
      xscale = ShpObject.SCALE * station.eradius; // use only S-radius FIXME
      yscale = ShpObject.SCALE * station.sradius;
      cd = TDMath.cosd( station.declination );
      sd = TDMath.sind( station.declination );
    }

    try {
      dir = TDFile.getFile( basepath );
      if ( ! dir.exists() && ! dir.mkdir() ) {
        TDLog.Error("mkdir error");
        return false;
      }
      ArrayList< File > files = new ArrayList<>();

      // centerline data: shepafile of segments (fields: type, fron, to)
      // xoff+sh.x1, yoff+sh.y1  --  xoff+sh.x2, yoff+sh.y2
      ArrayList< DrawingPath > shots = new ArrayList<>();
      if ( PlotType.isSketch2D( type ) ) { 
        for ( DrawingPath sh : plot.getLegs() ) {
          if ( sh.mBlock != null ) shots.add( sh );
        }
        for ( DrawingPath sh : plot.getSplays() ) {
          if ( sh.mBlock != null ) shots.add( sh );
        }
      }
      ShpSegment shp_shot = new ShpSegment( basepath + "/shot", files );

      shp_shot.writeSegments( shots, xoff, yoff, xscale, yscale, cd, sd );

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
      shp_point.writePoints( points, xoff, yoff, xscale, yscale, cd, sd );
      ShpPolyline shp_line = new ShpPolyline( basepath + "/line", DrawingPath.DRAWING_PATH_LINE, files );
      shp_line.writeLines( lines, xoff, yoff, xscale, yscale, cd, sd );
      ShpPolyline shp_area = new ShpPolyline( basepath + "/area", DrawingPath.DRAWING_PATH_AREA, files );
      shp_area.writeAreas( areas, xoff, yoff, xscale, yscale, cd, sd );

      // stations: xoff+name.cx, yoff+name.cy
      List< DrawingStationName > stations = plot.getStations();
      ShpStation shp_station = new ShpStation( basepath + "/station", files );
      shp_station.writeStations( stations, xoff, yoff, xscale, yscale, cd, sd );

      Archiver zipper = new Archiver( );
      zipper.compressFiles( basepath + ".shz", files );
      TDFile.deleteDir( basepath ); // delete temporary shapedir

    } catch ( IOException e ) {
      TDLog.Error( "SHP io-exception " + e.getMessage() );
      return false;
    } finally {
      TDFile.deleteDir( dir );
    }
    return true;
  }

}


