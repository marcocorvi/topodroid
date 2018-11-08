/* @file DataListener.java
 *
 * @author marco corvi
 * @date dec 2014
 *
 * @brief TopoDroid lister interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.util.ArrayList;

/* ---- IF_COSURVEY

interface DataListener 
{
  byte SHUTDOWN = (byte)0;
  byte SYNC     = (byte)0xfd;
  byte ACK = (byte)0xfe;
  byte EOL = (byte)0xff;

  // -------------------------------------------------------------------------
  // SURVEY

  byte SURVEY_SET  = (byte)1;
  byte SURVEY_INFO = (byte)2;
  byte SURVEY_DATE = (byte)3;
  byte SURVEY_TEAM = (byte)4;
  byte SURVEY_DECL = (byte)5;
  byte SURVEY_NAME = (byte)6;
  byte SURVEY_INIT_STATION = (byte)7;

  // only the sync-layer need be notified of this
  void onSetSurvey( long id, String name, int datamode );

  void onUpdateSurveyName( long id, String name );

  void onUpdateSurveyInfo( long id, String date, String team, double decl, String comment, String station, int xsections );

  void onUpdateSurveyDayAndComment( long id, String date, String comment );

  void onUpdateSurveyTeam( long id, String team );

  void onUpdateSurveyInitStation( long id, String station );

  void onUpdateSurveyDeclination( long id, double decl );

  // -------------------------------------------------------------------------
  // SHOTS

  byte SHOT_UPDATE   = (byte)11;
  byte SHOT_NAME     = (byte)12;
  byte SHOT_LEG      = (byte)13;
  byte SHOT_EXTEND   = (byte)14;
  byte SHOT_FLAG     = (byte)15;
  byte SHOT_COMMENT  = (byte)16;
  byte SHOT_DELETE   = (byte)17;
  byte SHOT_UNDELETE = (byte)18;
  byte SHOT_AMDR     = (byte)19;
  byte SHOT_DBC_UPDATE = (byte)20;
  byte SHOT_PBD_UPDATE = (byte)21;

  byte SHOT_INSERT   = (byte)22;
  byte SHOT_INSERTAT = (byte)23;
  byte SHOT_STATUS   = (byte)24;
  byte SHOT_COLOR    = (byte)25;
  byte SHOT_LEG_FLAG = (byte)26;

  void onUpdateShot( long id, long sid, String fStation, String tStation, long extend, long flag, long leg, String comment );

  void onUpdateShotDBC( long id, long sid, float d, float b, float c );

  void onUpdateShotPBD( long id, long sid, float p, float b, float d );

  void onUpdateShotName( long id, long sid, String fStation, String tStation );

  void onUpdateShotLeg( long id, long sid, long leg );

  void onUpdateShotExtend( long id, long sid, long extend, float stretch );

  void onUpdateShotFlag( long id, long sid, long flag );

  void onUpdateShotLegFlag( long id, long sid, long leg, long flag );

  void onUpdateShotComment( long id, long sid, String comment );

  void onUpdateShotStatus( long id, long sid, long status );

  void onUpdateShotAMDR( long sid, long id, double acc, double mag, double dip, double r );

  void onUpdateShotColor( long sid, long id, long color );

  // void onUpdateShotNameAndExtend( long sid, ArrayList< DBlock > updatelist );
  // FIXME repeatedly call UpdateShotName() and UpdateShotExtend();

  void onDeleteShot( long id, long sid, int status );

  void onUndeleteShot( long hot_id, long sid );

  // void onInsertShots( long sid, long id, ArrayList< ParserShot > shots );
  // FIXME repeatedly call InsertShot()

  void onInsertShot( long sid, long id, long millis, long color, String from, String to, 
                          double d, double b, double c, double r, 
                          long extend, double stretch, long flag, long leg, long status, long shot_type, String comment );

  // @param e extend
  // @param t type
  void onInsertShotAt( long sid, long at, long millis, long color, double d, double b, double c, double r,
		  long e, double stretch, long leg, long t );

  // void transferShots( long sid, long old_sid, long old_id );

  // void doDeleteSurvey( long sid ) 

  // -------------------------------------------------------
  // PLOTS Aand SKETCHES

  byte PLOT_INSERT   = (byte)31;
  byte PLOT_UPDATE   = (byte)32;
  byte PLOT_DROP     = (byte)33;
  byte PLOT_DELETE   = (byte)34;
  byte PLOT_UNDLEETE = (byte)35;

  void onInsertPlot( long sid, long id, String name, long type, long status, String start, String view,
                            double xoffset, double yoffset, double zoom, double azimuth, double clino, String hide,
			    String nick, int orientation );

  // void updatePlot( long plot_id, long survey_id, double xoffset, double yoffset, double zoom );

  // void onNewSketch3d( long sid, long id, String name, long status, String start, String st1, String st2,
  //                         double xoffsettop, double yoffsettop, double zoomtop,
  //                         double xoffsetside, double yoffsetside, double zoomside,
  //                         double xoffset3d, double yoffset3d, double zoom3d,
  //                         double x, double y, double z, double azimuth, double clino );

  // void updateSketch( long sketch_id, long survey_id, 
  //                            String st1, String st2,
  //                            double xofftop, double yofftop, double zoomtop,
  //                            double xoffside, double yoffside, double zoomside,
  //                            double xoff3d, double yoff3d, double zoom3d,
  //                            double east, double south, double vert, double azimuth, double clino );

  // void dropPlot( long plot_id, long survey_id ); // real delete
  // void deletePlot( long plot_id, long survey_id )
  // void undeletePlot( long plot_id, long survey_id )
  // void deleteSketch( long sketch_id, long survey_id )

  // void onNewPhoto( long sid, long id, long shotid, String title, String date, String comment );
  // void onUpdatePhoto( long sid, long id, String comment );
  // void onDeletePhoto( long sid, long id );

  // void onNewSensor( long sid, long id, long shotid, String title, String date, String comment, 
  //                            String type, String value );
  // void onDeleteSensor( long sid, long id );
  // void onUpdateSensor( long sid, long id, String comment );

  // void onNewFixed( long sid, long id, String station, double lng, double lat, double alt, double asl,
  //                         String comment, long status );
  // void onUpdateFixedStation( long id, long sid, String station );
  // void onUpdateFixedStatus( long id, long sid, long status );
  // void onDeletedFixed( long sid, String station ); 

}

*/

