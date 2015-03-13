/* @file DataListener.java
 *
 * @author marco corvi
 * @date dec 2014
 *
 * @brief TopoDroid lister interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

public interface DataListener 
{
  /**
   * @param nr    number of new shots
   * @param toast whether to toast feedbacks
   *
  public void refreshDisplay( int nr, boolean toast );

  public void updateBlockList( DistoXDBlock blk );
   */

  static final byte SHUTDOWN = (byte)0;
  static final byte SYNC     = (byte)0xfd;
  static final byte ACK = (byte)0xfe;
  static final byte EOL = (byte)0xff;

  // -------------------------------------------------------------------------
  // SURVEY

  static final byte SURVEY_SET  = (byte)1;
  static final byte SURVEY_INFO = (byte)2;
  static final byte SURVEY_DATE = (byte)3;
  static final byte SURVEY_TEAM = (byte)4;
  static final byte SURVEY_DECL = (byte)5;

  // only the sync-layer need be notified of this
  public void onSetSurvey( long id, String name );

  public void onUpdateSurveyInfo( long id, String date, String team, double decl, String comment );

  public void onUpdateSurveyDayAndComment( long id, String date, String comment );

  public void onUpdateSurveyTeam( long id, String team );

  public void onUpdateSurveyDeclination( long id, double decl );

  // -------------------------------------------------------------------------
  // SHOTS

  static final byte SHOT_UPDATE   = (byte)11;
  static final byte SHOT_NAME     = (byte)12;
  static final byte SHOT_LEG      = (byte)13;
  static final byte SHOT_EXTEND   = (byte)14;
  static final byte SHOT_FLAG     = (byte)15;
  static final byte SHOT_COMMENT  = (byte)16;
  static final byte SHOT_DELETE   = (byte)17;
  static final byte SHOT_UNDELETE = (byte)18;
  static final byte SHOT_AMDR     = (byte)19;
  static final byte SHOT_DBC_UPDATE = (byte)20;

  static final byte SHOT_INSERT   = (byte)21;
  static final byte SHOT_INSERTAT = (byte)23;

  public void onUpdateShot( long id, long sid, String fStation, String tStation,
                            long extend, long flag, long leg, String comment );

  public void onUpdateShotDBC( long id, long sid, float d, float b, float c );

  public void onUpdateShotName( long id, long sid, String fStation, String tStation );

  public void onUpdateShotLeg( long id, long sid, long leg );

  public void onUpdateShotExtend( long id, long sid, long extend );

  public void onUpdateShotFlag( long id, long sid, long flag );

  public void onUpdateShotComment( long id, long sid, String comment );

  public void onUpdateShotAMDR( long sid, long id, double acc, double mag, double dip, double r );

  // public void onUpdateShotNameAndExtend( long sid, ArrayList< DistoXDBlock > updatelist );
  // FIXME repeatedly call UpdateShotName() and UpdateShotExtend();

  public void onDeleteShot( long id, long sid );

  public void onUndeleteShot( long hot_id, long sid );

  // public void onInsertShots( long sid, long id, ArrayList< ParserShot > shots );
  // FIXME repeatedly call InsertShot()

  public void onInsertShot( long sid, long id, String from, String to, 
                          double d, double b, double c, double r, 
                          long extend, long flag, long leg, long status, String comment );

  public void onInsertShotAt( long sid, long at, double d, double b, double c, double r );

  // public void transferShots( long sid, long old_sid, long old_id );

  // public void doDeleteSurvey( long sid ) 

  // -------------------------------------------------------
  // PLOTS Aand SKETCHES

  static final byte PLOT_INSERT   = (byte)31;
  static final byte PLOT_UPDATE   = (byte)32;
  static final byte PLOT_DROP     = (byte)33;
  static final byte PLOT_DELETE   = (byte)34;
  static final byte PLOT_UNDLEETE = (byte)35;

  public void onInsertPlot( long sid, long id, String name, long type, long status, String start, String view,
                            double xoffset, double yoffset, double zoom, double azimuth, double clino );

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

