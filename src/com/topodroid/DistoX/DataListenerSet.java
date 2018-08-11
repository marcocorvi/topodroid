/* @file DataListenerSet.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid container of data listener (co-surveying)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

class DataListenerSet
{
  private ArrayList< DataListener > mListeners;

  DataListenerSet()
  {
    mListeners = new ArrayList<>();
  }

  // synchronized( mDataListener )
  void registerDataListener( DataListener listener )
  {
    for ( DataListener l : mListeners ) {
      if ( l == listener ) return;
    }
    mListeners.add( listener );
  }

  // synchronized( mDataListener )
  void unregisterDataListener( DataListener listener )
  {
    mListeners.remove( listener );
  }


  // only the sync-layer need be notified of this
  void onSetSurvey( long id, String name )
  { for ( DataListener l : mListeners ) l.onSetSurvey( id, name ); }

  void onUpdateSurveyName( long id, String name )
  { for ( DataListener l : mListeners ) l.onUpdateSurveyName( id, name ); }

  void onUpdateSurveyInfo( long id, String date, String team, double decl,
                                  String comment, String station, int xsections )
  { for ( DataListener l : mListeners ) l.onUpdateSurveyInfo( id, date, team, decl, comment, station, xsections ); }

  void onUpdateSurveyDayAndComment( long id, String date, String comment )
  { for ( DataListener l : mListeners ) l.onUpdateSurveyDayAndComment( id, date, comment ); }

  void onUpdateSurveyTeam( long id, String team )
  { for ( DataListener l : mListeners ) l.onUpdateSurveyTeam( id, team ); }

  void onUpdateSurveyInitStation( long id, String station )
  { for ( DataListener l : mListeners ) l.onUpdateSurveyTeam( id, station ); }

  void onUpdateSurveyDeclination( long id, double decl )
  { for ( DataListener l : mListeners ) l.onUpdateSurveyDeclination( id, decl ); }

  // -------------------------------------------------------------------------
  // SHOTS

  void onUpdateShot( long id, long sid, String fStation, String tStation,
                            long extend, long flag, long leg, String comment )
  { for ( DataListener l : mListeners ) l.onUpdateShot( id, sid, fStation, tStation, extend, flag, leg, comment ); }

  void onUpdateShotDBC( long id, long sid, float d, float b, float c )
  { for ( DataListener l : mListeners ) l.onUpdateShotDBC( id, sid, d, b, c ); }

  void onUpdateShotName( long id, long sid, String fStation, String tStation )
  { for ( DataListener l : mListeners ) l.onUpdateShotName( id, sid, fStation, tStation ); }

  void onUpdateShotLeg( long id, long sid, long leg )
  { for ( DataListener l : mListeners ) l.onUpdateShotLeg( id, sid, leg ); }

  void onUpdateShotExtend( long id, long sid, long extend, float stretch )
  { for ( DataListener l : mListeners ) l.onUpdateShotExtend( id, sid, extend, stretch ); }

  void onUpdateShotFlag( long id, long sid, long flag )
  { for ( DataListener l : mListeners ) l.onUpdateShotFlag( id, sid, flag ); }

  void onUpdateShotLegFlag( long id, long sid, long leg, long flag )
  { for ( DataListener l : mListeners ) l.onUpdateShotLegFlag( id, sid, leg, flag ); }

  void onUpdateShotComment( long id, long sid, String comment )
  { for ( DataListener l : mListeners ) l.onUpdateShotComment( id, sid, comment ); }

  void onUpdateShotStatus( long id, long sid, long status )
  { for ( DataListener l : mListeners ) l.onUpdateShotStatus( id, sid, status ); }

  void onUpdateShotAMDR( long sid, long id, double acc, double mag, double dip, double r )
  { for ( DataListener l : mListeners ) l.onUpdateShotAMDR( sid, id, acc, mag, dip, r ); }

  void onUpdateShotColor( long sid, long id, long color )
  { for ( DataListener l : mListeners ) l.onUpdateShotColor( sid, id, color ); }

  // public void onUpdateShotNameAndExtend( long sid, ArrayList< DBlock > updatelist );
  // FIXME repeatedly call UpdateShotName() and UpdateShotExtend();

  void onDeleteShot( long id, long sid, int status )
  { for ( DataListener l : mListeners ) l.onDeleteShot( id, sid, status ); }

  void onUndeleteShot( long id, long sid )
  { for ( DataListener l : mListeners ) l.onUndeleteShot( id, sid ); }

  // public void onInsertShots( long sid, long id, ArrayList< ParserShot > shots )
  // FIXME repeatedly call InsertShot()

  void onInsertShot( long sid, long id, long millis, long color, String from, String to,
                            double d, double b, double c, double r, 
                            long extend, double stretch, long flag, long leg, long status, long shot_type, String comment )
  {
    for ( DataListener l : mListeners )
      l.onInsertShot( sid, id, millis, color, from, to, d, b, c, r, extend, stretch, flag, leg, status, shot_type, comment );
  }

  // @param e extend
  // @param t type
  void onInsertShotAt( long sid, long at, long millis, long color, double d, double b, double c, double r,
		  long e, double stretch, long leg, long t )
  { for ( DataListener l : mListeners ) l.onInsertShotAt( sid, at, millis, color, d, b, c, r, e, stretch, leg, t ); }

  // public void transferShots( long sid, long old_sid, long old_id );

  // public void doDeleteSurvey( long sid ) 

  // -------------------------------------------------------
  // PLOTS Aand SKETCHES

  void onInsertPlot( long sid, long id, String name, long type, long status, String start, String view,
                            double xoff, double yoff, double zoom, double azimuth, double clino, String hide,
		            String nick, int orientation )
  { for ( DataListener l : mListeners )
      l.onInsertPlot( sid, id, name, type, status, start, view, xoff, yoff, zoom, azimuth, clino, hide, nick, orientation );
  }

  // public void updatePlot( long plot_id, long survey_id, double xoffset, double yoffset, double zoom );

  // public void onNewSketch3d( long sid, long id, String name, long status, String start, String st1, String st2,
  //                         double xoffsettop, double yoffsettop, double zoomtop,
  //                         double xoffsetside, double yoffsetside, double zoomside,
  //                         double xoffset3d, double yoffset3d, double zoom3d,
  //                         double x, double y, double z, double azimuth, double clino );

  // public void updateSketch( long sketch_id, long survey_id, 
  //                            String st1, String st2,
  //                            double xofftop, double yofftop, double zoomtop,
  //                            double xoffside, double yoffside, double zoomside,
  //                            double xoff3d, double yoff3d, double zoom3d,
  //                            double east, double south, double vert, double azimuth, double clino );

  // public void dropPlot( long plot_id, long survey_id ); // real delete
  // public void deletePlot( long plot_id, long survey_id )
  // public void undeletePlot( long plot_id, long survey_id )
  // public void deleteSketch( long sketch_id, long survey_id )

  // public void onNewPhoto( long sid, long id, long shotid, String title, String date, String comment );
  // public void onUpdatePhoto( long sid, long id, String comment );
  // public void onDeletePhoto( long sid, long id );

  // public void onNewSensor( long sid, long id, long shotid, String title, String date, String comment, 
  //                            String type, String value );
  // public void onDeleteSensor( long sid, long id );
  // public void onUpdateSensor( long sid, long id, String comment );

  // public void onNewFixed( long sid, long id, String station, double lng, double lat, double alt, double asl,
  //                         String comment, long status );
  // public void onUpdateFixedStation( long id, long sid, String station );
  // public void onUpdateFixedStatus( long id, long sid, long status );
  // public void onDeletedFixed( long sid, String station ); 

}

