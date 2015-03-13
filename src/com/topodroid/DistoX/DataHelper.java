/* @file DataHelper.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SQLite database manager
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120516 survey team and survey info unpdate
 * 20120518 db path from TopoDroid app
 * 20120521 methods for CalibInfo
 * 20120522 photo support
 * 20120603 fixed update and delete support
 * 20120610 archive (zip)
 * 20120722 put table create in a transaction
 * 20120725 TopoDroidApp log
 * 20121001 restored updateShotExtend
 * 20121114 getLastShotId
 * 20121114 allowed multiple locations for a station (commented check at insertFixed)
 * 20121215 InsertHelper for bulk insert (survey insertShots)
 * 20121215 bulk splay shot name+extend update:  updateShotNameAndExtend
 * 20121224 added field xoffset yoffset zoom to table plots
 * 20130111 photo date
 * 20130324 zip export of 3D sketches
 * 20130621 selectLastLegShot()
 * 20130629 database version 11
 * 20130910 shiftShotsId and insertShotAt statement
 * 20130910 transferShot to implement split-survey
 * 20131116 added acc. mag. dip to shot
 * 201312   cross-sections 
 * 201401   min database compatible version (for import)
 * 20140328 "azimuth" column in plots table
 * 20140803 enforce one location per station
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.DataSetObservable;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteException;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;

public class DataHelper extends DataSetObservable
{

  static final String DB_VERSION = "22";
  static final int DATABASE_VERSION = 22;
  static final int DATABASE_VERSION_MIN = 21; // was 14

  private static final String CONFIG_TABLE = "configs";
  private static final String SURVEY_TABLE = "surveys";
  private static final String FIXED_TABLE  = "fixeds";
  private static final String CALIB_TABLE  = "calibs";
  private static final String SHOT_TABLE   = "shots";
  private static final String STATION_TABLE = "stations";
  private static final String GM_TABLE     = "gms";
  private static final String PLOT_TABLE   = "plots";
  private static final String SKETCH_TABLE = "sketches";
  private static final String PHOTO_TABLE  = "photos";
  private static final String SENSOR_TABLE = "sensors";
  private static final String DEVICE_TABLE = "devices";

  private SQLiteDatabase myDB = null;
  private long           myNextId;   // id of next shot
  private long           myNextCId;  // id of next calib-data

  private SQLiteStatement updateConfig;
  private SQLiteStatement updateGMGroupStmt;
  private SQLiteStatement updateGMErrorStmt;
  private SQLiteStatement deleteGMStmt;

  private SQLiteStatement updateShotStmt;
  private SQLiteStatement updateShotStmtFull;
  private SQLiteStatement updateShotDBCStmt;
  private SQLiteStatement updateShotLegStmt;
  private SQLiteStatement updateStationCommentStmt;
  private SQLiteStatement deleteStationStmt;
  private SQLiteStatement updateShotNameStmt;
  private SQLiteStatement updateShotNameAndExtendStmt;
  private SQLiteStatement updateShotExtendStmt;
  private SQLiteStatement updateShotFlagStmt;
  private SQLiteStatement updateShotCommentStmt;
  private SQLiteStatement updateShotAMDRStmt;
  private SQLiteStatement shiftShotsIdStmt;
  private SQLiteStatement transferShotStmt;
  private SQLiteStatement updateSurveyStmt;
  private SQLiteStatement updateSurveyInfoStmt;
  private SQLiteStatement updateSurveyTeamStmt;
  private SQLiteStatement updateSurveyDeclinationStmt;
  // private SQLiteStatement updateSurveyNameStmt;

  private SQLiteStatement updateCalibStmt;
  private SQLiteStatement updateCalibAlgoStmt;
  private SQLiteStatement updateCalibCoeffStmt;
  private SQLiteStatement updateCalibErrorStmt;
  private SQLiteStatement resetAllGMStmt;

  private SQLiteStatement deleteShotStmt;
  private SQLiteStatement undeleteShotStmt;
  private SQLiteStatement updatePlotStmt;
  private SQLiteStatement updatePlotViewStmt;
  private SQLiteStatement deletePlotStmt;
  private SQLiteStatement undeletePlotStmt;
  // FIXME_SKETCH_3D
  private SQLiteStatement updateSketchStmt;
  private SQLiteStatement deleteSketchStmt;
  // END_SKETCH_3D
  private SQLiteStatement updatePhotoStmt;
  private SQLiteStatement updateSensorStmt;

  private SQLiteStatement updateFixedStationStmt;
  private SQLiteStatement updateFixedStatusStmt;

//these are real database "delete"
  private SQLiteStatement deletePhotoStmt;
  private SQLiteStatement deleteSensorStmt;
  private SQLiteStatement dropPlotStmt;
  private SQLiteStatement dropFixedStmt;
  private SQLiteStatement doDeleteGMStmt;
  private SQLiteStatement doDeleteCalibStmt;
  private SQLiteStatement doDeletePhotoStmt;
  private SQLiteStatement doDeletePlotStmt;
  private SQLiteStatement doDeleteFixedStmt;
  private SQLiteStatement doDeleteShotStmt;
  private SQLiteStatement doDeleteStationStmt;
  private SQLiteStatement doDeleteSurveyStmt;

  private SQLiteStatement updateDeviceHeadTailStmt;
  private SQLiteStatement updateDeviceModelStmt;

  private String[] mShotFields; // select shot fields

  private ArrayList<DataListener> mListeners;
  // ----------------------------------------------------------------------
  // DATABASE

  private Context mContext;

  public SQLiteDatabase getDb() { return myDB; }

  public DataHelper( Context context, ArrayList<DataListener> listeners )
  {
    mContext = context;
    mShotFields = new String[] { 
         "id", "fStation", "tStation", "distance", "bearing",
         "clino", "acceleration", "magnetic", "dip", "extend",
         "flag", "leg", "comment", "type"
    };
    mListeners = listeners;
    openDatabase();
  }

  public void openDatabase() 
  {
    String database_name = TopoDroidPath.getDatabase();
    DistoXOpenHelper openHelper = new DistoXOpenHelper( mContext, database_name );

    try {
        myDB = openHelper.getWritableDatabase();
        if ( myDB == null ) {
          Log.v("DistoX", "failed get writable database" );
          return;
        }

        while ( myDB.isDbLockedByOtherThreads() ) {
          try {
            Thread.sleep( 200 );
          } catch ( InterruptedException e ) {}
        }

        updateConfig       = myDB.compileStatement( "UPDATE configs SET value=? WHERE key=?" );
        updateGMGroupStmt  = myDB.compileStatement( "UPDATE gms SET grp=? WHERE calibId=? AND id=?" );
        updateGMErrorStmt  = myDB.compileStatement( "UPDATE gms SET error=? WHERE calibId=? AND id=?" );
        updateStationCommentStmt = myDB.compileStatement( "UPDATE stations SET comment=? WHERE surveyId=? AND name=?" );
        deleteStationStmt  = myDB.compileStatement( "DELETE FROM stations WHERE surveyId=? AND name=?" );
        updateShotNameStmt = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=? WHERE surveyId=? AND id=?" );
        updateShotNameAndExtendStmt = myDB.compileStatement(
                             "UPDATE shots SET fStation=?, tStation=?, extend=?, leg=? WHERE surveyId=? AND id=?" );
        updateShotStmt     = myDB.compileStatement( 
                             "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, leg=? WHERE surveyId=? AND id=?" );
        shiftShotsIdStmt   = myDB.compileStatement(
                             "UPDATE shots SET id=id+1 where surveyId=? and id>=?" );
        transferShotStmt   = myDB.compileStatement(
                             "UPDATE shots SET surveyId=?, id=? where surveyId=? and id=?" );
        updateShotStmtFull = myDB.compileStatement(
                             "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, leg=?, comment=? WHERE surveyId=? AND id=?" );
        updateShotDBCStmt = myDB.compileStatement(
                            "UPDATE shots SET distance=?, bearing=?, clino=? WHERE surveyId=? AND id=?" );

        updateShotLegStmt = myDB.compileStatement( "UPDATE shots SET leg=? WHERE surveyId=? AND id=?" );
        updateShotLegStmt = myDB.compileStatement( "UPDATE shots SET leg=? WHERE surveyId=? AND id=?" );

        updateShotExtendStmt  = myDB.compileStatement( "UPDATE shots SET extend=? WHERE surveyId=? AND id=?" );
        updateShotFlagStmt    = myDB.compileStatement( "UPDATE shots SET flag=? WHERE surveyId=? AND id=?" );
        updateShotCommentStmt = myDB.compileStatement( "UPDATE shots SET comment=? WHERE surveyId=? AND id=?" );
        updateShotAMDRStmt  = myDB.compileStatement( "UPDATE shots SET acceleration=?, magnetic=?, dip=?, roll=? WHERE surveyId=? AND id=?" );

        updateSurveyInfoStmt = myDB.compileStatement( "UPDATE surveys SET day=?, team=?, declination=?, comment=? WHERE id=?" );
        updateSurveyStmt = myDB.compileStatement( "UPDATE surveys SET day=?, comment=? WHERE id=?" );
        updateSurveyTeamStmt = myDB.compileStatement( "UPDATE surveys SET team=? WHERE id=?" );
        updateSurveyDeclinationStmt = myDB.compileStatement( "UPDATE surveys SET declination=? WHERE id=?" );
        // updateSurveyNameStmt = myDB.compileStatement( "UPDATE surveys SET name=? WHERE id=?" );
        updateCalibStmt = myDB.compileStatement( "UPDATE calibs SET day=?, device=?, comment=? WHERE id=?" );
        updateCalibAlgoStmt = myDB.compileStatement( "UPDATE calibs SET algo=? WHERE id=?" );
        updateCalibCoeffStmt = myDB.compileStatement( "UPDATE calibs SET coeff=? WHERE id=?" );
        updateCalibErrorStmt = myDB.compileStatement( "UPDATE calibs SET error=?, max_error=?, iterations=? WHERE id=?" );

        deleteShotStmt   = myDB.compileStatement( "UPDATE shots set status=1 WHERE surveyId=? AND id=?" );
        undeleteShotStmt = myDB.compileStatement( "UPDATE shots set status=0 WHERE surveyId=? AND id=?" );
        updatePlotStmt   = myDB.compileStatement( "UPDATE plots set xoffset=?, yoffset=?, zoom=? WHERE surveyId=? AND id=?" );
        updatePlotViewStmt = myDB.compileStatement( "UPDATE plots set view=? WHERE surveyId=? AND id=?" );
        dropPlotStmt     = myDB.compileStatement( "DELETE FROM plots WHERE surveyId=? AND id=?" );
        deletePlotStmt   = myDB.compileStatement( "UPDATE plots set status=1 WHERE surveyId=? AND id=?" );
        undeletePlotStmt = myDB.compileStatement( "UPDATE plots set status=0 WHERE surveyId=? AND id=?" );

        // FIXME_SKETCH_3D
        updateSketchStmt = myDB.compileStatement( "UPDATE sketches set st1=?, st2=?, xoffsettop=?, yoffsettop=?, zoomtop=?, xoffsetside=?, yoffsetside=?, zoomside=?, xoffset3d=?, yoffset3d=?, zoom3d=?, east=?, south=?, vert=?, azimuth=?, clino=? WHERE surveyId=? AND id=?" );
        deleteSketchStmt = myDB.compileStatement( "UPDATE sketches set status=1 WHERE surveyId=? AND id=?" );
        // END_SKETCH_3D

        deletePhotoStmt  = myDB.compileStatement( "DELETE FROM photos WHERE surveyId=? AND id=?" );
        updatePhotoStmt  = myDB.compileStatement( "UPDATE photos set comment=? WHERE surveyId=? AND id=?" );

        deleteSensorStmt = myDB.compileStatement( "DELETE FROM sensors WHERE surveyId=? AND id=?" );
        updateSensorStmt = myDB.compileStatement( "UPDATE sensors set comment=? WHERE surveyId=? AND id=?" );
 
        updateFixedStationStmt = myDB.compileStatement( "UPDATE fixeds set station=? WHERE surveyId=? AND id=?" );
        updateFixedStatusStmt = myDB.compileStatement( "UPDATE fixeds set status=? WHERE surveyId=? AND id=?" );

        resetAllGMStmt = myDB.compileStatement( "UPDATE gms SET grp=0, error=0 WHERE calibId=? AND status=0" );
        deleteGMStmt = myDB.compileStatement( "UPDATE gms set status=? WHERE calibID=? AND id=?" );

        doDeleteGMStmt    = myDB.compileStatement( "DELETE FROM gms where calibId=?" );
        doDeleteCalibStmt = myDB.compileStatement( "DELETE FROM calibs where id=?" );

        doDeletePhotoStmt   = myDB.compileStatement( "DELETE FROM photos where surveyId=?" );
        doDeletePlotStmt    = myDB.compileStatement( "DELETE FROM plots where surveyId=?" );
        doDeleteFixedStmt   = myDB.compileStatement( "DELETE FROM fixeds where surveyId=?" );
        doDeleteShotStmt    = myDB.compileStatement( "DELETE FROM shots where surveyId=?" );
        doDeleteStationStmt = myDB.compileStatement( "DELETE FROM stations where surveyId=?" );
        doDeleteSurveyStmt  = myDB.compileStatement( "DELETE FROM surveys where id=?" );

        dropFixedStmt  = myDB.compileStatement( "DELETE FROM fixeds where surveyId=? and station=? and status=1" );
        updateDeviceHeadTailStmt = myDB.compileStatement( "UPDATE devices set head=?, tail=? WHERE address=?" );
        updateDeviceModelStmt = myDB.compileStatement( "UPDATE devices set model=? WHERE address=?" );

     } catch ( SQLiteException e ) {
       myDB = null;
       TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "DataHelper cstr failed to get DB " + e.getMessage() );
     }
   }


   private void fillBlock( long survey_id, DistoXDBlock block, Cursor cursor )
   {
     block.setId( cursor.getLong(0), survey_id );
     // Log.v( TopoDroidApp.TAG, survey_id + "/" + cursor.getLong(0) + " name <" + cursor.getString(1) + "> <" + cursor.getString(2) );

     block.setName(    cursor.getString(1), cursor.getString(2) );
     block.mLength  = (float)( cursor.getDouble(3) );
     block.setBearing( (float)( cursor.getDouble(4) ) );
     block.mClino   = (float)( cursor.getDouble(5) );
     block.mAcceleration = (float)( cursor.getDouble(6) );
     block.mMagnetic     = (float)( cursor.getDouble(7) );
     block.mDip          = (float)( cursor.getDouble(8) );
     
     block.mExtend  = cursor.getLong(9);
     block.mFlag    = cursor.getLong(10);
     if ( cursor.getLong(11) == 1 ) {
         block.mType = DistoXDBlock.BLOCK_SEC_LEG; 
     }
     block.mComment = cursor.getString(12);
     block.mShotType = (int)cursor.getLong(13);
   }
   

   // ----------------------------------------------------------------------
   // SURVEY DATA

  public SurveyStat getSurveyStat( long sid )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "getSurveyStat sid " + sid );
    HashMap< String, Integer > map = new HashMap< String, Integer >();
    int n0 = 0;
    int nc = 0;
    int ne = 0;
    int nl = 0;
    int nv = 0;

    SurveyStat stat = new SurveyStat();
    stat.id = sid;
    stat.lengthLeg = 0.0f;
    stat.lengthDuplicate = 0.0f;
    stat.lengthSurface   = 0.0f;
    stat.countLeg = 0;
    stat.countDuplicate = 0;
    stat.countSurface   = 0;
    stat.countSplay     = 0;
    stat.countStation   = 0;
    stat.countLoop      = 0;
    stat.countComponent = 0;

    // if ( myDB == null ) return stat;

    Cursor cursor = myDB.query( SHOT_TABLE,
			        new String[] { "flag", "distance", "fStation", "tStation" },
                                "surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ", 
                                new String[] { Long.toString(sid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
    if (cursor.moveToFirst()) {
      do {
        switch ( (int)(cursor.getLong(0)) ) {
          case 0: ++ stat.countLeg;
            stat.lengthLeg += (float)( cursor.getDouble(1) );
            break;
          case 1: ++ stat.countSurface;
            stat.lengthSurface += (float)( cursor.getDouble(1) );
            break;
          case 2: ++ stat.countDuplicate;
            stat.lengthDuplicate += (float)( cursor.getDouble(1) );
            break;
        }
        String f = cursor.getString(2);
        String t = cursor.getString(3);
        ++ ne;
        if ( map.containsKey( f ) ) {
          Integer fi = map.get( f );
          if ( map.containsKey( t ) ) {
            Integer ti = map.get( t );
            if ( fi.equals( ti ) ) {
              ++ nl;
            } else { // merge 
              for ( String k : map.keySet() ) {
                if ( map.get( k ).equals( ti ) ) {
                  map.put(k, fi );
                }
              }
              -- nc;
            }
          } else {
            map.put( t, fi );
            ++ nv;
          }
        } else {
          if ( map.containsKey( t ) ) {
            Integer ti = map.get( t );
            map.put( f, ti );
            ++ nv;
          } else {
            ++ n0;
            Integer fi = new Integer( n0 );
            map.put( t, fi );
            map.put( f, fi );
            nv += 2;
            ++ nc;
          }
        }
      } while ( cursor.moveToNext() );
    }
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }

    stat.countStation = map.size();
    stat.countLoop = nl;
    stat.countComponent = nc;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "getSurveyStats NV " + nv + " NE " + ne + " NL " + nl + " NC " + nc);
   

    cursor = myDB.query( SHOT_TABLE,
                         new String[] { "count()" },
                         "surveyId=? AND status=0 AND flag=0 AND fStation!=\"\" AND tStation=\"\" ",
                         new String[] { Long.toString(sid) },
                         null,  // groupBy
                         null,  // having
                         null ); // order by
    if (cursor.moveToFirst()) {
      stat.countSplay = (int)( cursor.getLong(0) );
    }
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }
    return stat;
  }

  // --------------------------------------------------------------------
  // SURVEY
   
  public void updateSurveyInfo( long id, String date, String team, double decl, String comment, boolean forward )
  {
    updateSurveyInfoStmt.bindString( 1, date );
    updateSurveyInfoStmt.bindString( 2, team );
    updateSurveyInfoStmt.bindDouble( 3, decl );
    updateSurveyInfoStmt.bindString( 4, (comment != null)? comment : "" );
    updateSurveyInfoStmt.bindLong( 5, id );
    updateSurveyInfoStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateSurveyInfo( id, date, team, decl, comment );
      }
    }
  }

  public boolean updateSurveyDayAndComment( String name, String date, String comment, boolean forward )
  {
    boolean ret = false;
    long id = getIdFromName( SURVEY_TABLE, name );
    if ( id >= 0 ) { // survey name exists
      ret = updateSurveyDayAndComment( id, date, comment, forward );
    }
    return ret;
  }


  public boolean updateSurveyDayAndComment( long id, String date, String comment, boolean forward )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB,
    //   "updateSurveyDayAndComment id " + id + " day " + date + " comment \"" + comment + "\"" );
    if ( date == null ) return false;
    updateSurveyStmt.bindString( 1, date );
    updateSurveyStmt.bindString( 2, (comment != null)? comment : "" );
    updateSurveyStmt.bindLong( 3, id );
    updateSurveyStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateSurveyDayAndComment( id, date, comment );
      }
    }
    return true;
  }

  public void updateSurveyTeam( long id, String team, boolean forward )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateSurveyTeam id " + id + " team \"" + team + "\"" );
    updateSurveyTeamStmt.bindString( 1, (team != null)? team : "" );
    updateSurveyTeamStmt.bindLong( 2, id );
    updateSurveyTeamStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateSurveyTeam( id, team );
      }
    }
  }

  public void updateSurveyDeclination( long id, double decl, boolean forward )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateSurveyDeclination id " + id + " decl. " + decl );
    updateSurveyDeclinationStmt.bindDouble( 1, decl );
    updateSurveyDeclinationStmt.bindLong( 2, id );
    updateSurveyDeclinationStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateSurveyDeclination( id, decl );
      }
    }
  }

  public void doDeleteSurvey( long sid ) 
  {
    // if ( myDB == null ) return;
    doDeletePhotoStmt.bindLong( 1, sid );
    doDeletePhotoStmt.execute();
    doDeletePlotStmt.bindLong( 1, sid );
    doDeletePlotStmt.execute();
    doDeleteFixedStmt.bindLong( 1, sid );
    doDeleteFixedStmt.execute();
    doDeleteShotStmt.bindLong( 1, sid );
    doDeleteShotStmt.execute();
    doDeleteStationStmt.bindLong( 1, sid );
    doDeleteStationStmt.execute();
    doDeleteSurveyStmt.bindLong( 1, sid );
    doDeleteSurveyStmt.execute();
  }
  
  // --------------------------------------------------------------------
  // SHOTS

  void updateShotDistanceBearingClino( long id, long sid, float d, float b, float c, boolean forward )
  {
    updateShotDBCStmt.bindDouble(  1, d );
    updateShotDBCStmt.bindDouble(  2, b );
    updateShotDBCStmt.bindDouble(  3, c );
    updateShotDBCStmt.bindLong(   4, sid );     // WHERE
    updateShotDBCStmt.bindLong(   5, id );
    updateShotDBCStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateShotDBC( id, sid, d, b, c );
      }
    }
  }

  int updateShot( long id, long sid, String fStation, String tStation,
                  long extend, long flag, long leg, String comment, boolean forward )
  {
    TopoDroidLog.Log(  TopoDroidLog.LOG_DB, "updateShot " + fStation + "-" + tStation + " " + extend + " " + flag + " <" + comment + ">");
    // if ( myDB == null ) return -1;
    // if ( makesCycle( id, sid, fStation, tStation ) ) return -2;

    if ( comment != null ) {
      updateShotStmtFull.bindString( 1, fStation );
      updateShotStmtFull.bindString( 2, tStation );
      updateShotStmtFull.bindLong(   3, extend );
      updateShotStmtFull.bindLong(   4, flag );
      updateShotStmtFull.bindLong(   5, leg );
      updateShotStmtFull.bindString( 6, comment );
      updateShotStmtFull.bindLong(   7, sid );     // WHERE
      updateShotStmtFull.bindLong(   8, id );
      updateShotStmtFull.execute();
    } else {
      updateShotStmt.bindString( 1, fStation );
      updateShotStmt.bindString( 2, tStation );
      updateShotStmt.bindLong(   3, extend );
      updateShotStmt.bindLong(   4, flag );
      updateShotStmt.bindLong(   5, leg );
      updateShotStmt.bindLong(   6, sid );
      updateShotStmt.bindLong(   7, id );
      updateShotStmt.execute();
    }
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateShot( id, sid, fStation, tStation, extend, flag, leg, comment );
      }
    }
    return 0;
  }

  private void shiftShotsId( long sid, long id )
  {
    shiftShotsIdStmt.bindLong(1, sid);
    shiftShotsIdStmt.bindLong(2, id);
    shiftShotsIdStmt.execute();
  }

  // public boolean makesCycle( long id, long sid, String f, String t )
  // {
  //   if ( t == null || t.length() == 0 ) return false;
  //   if ( f == null || f.length() == 0 ) return false;
  //   int cnt = 0;
  //   if ( hasShotAtStation( id, sid, f ) ) ++cnt;
  //   if ( hasShotAtStation( id, sid, t ) ) ++cnt;
  //   TopoDroidLog.Log( TopoDroidLog.LOG_DB, "makesCycle cnt " + cnt );
  //   return cnt >= 2;
  // }


  public void updateShotName( long id, long sid, String fStation, String tStation, boolean forward )
  {
    // if ( myDB == null ) return;
    updateShotNameStmt.bindString( 1, fStation );
    updateShotNameStmt.bindString( 2, tStation );
    updateShotNameStmt.bindLong(   3, sid );
    updateShotNameStmt.bindLong(   4, id );
    updateShotNameStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateShotName( id, sid, fStation, tStation );
      }
    }
  }

  public void updateShotLeg( long id, long sid, long leg, boolean forward )
  {
    // if ( myDB == null ) return;
    updateShotLegStmt.bindLong(   1, leg );
    updateShotLegStmt.bindLong(   2, sid );
    updateShotLegStmt.bindLong(   3, id );
    updateShotLegStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateShotLeg( id, sid, leg );
      }
    }
  }

  public void updateShotExtend( long id, long sid, long extend, boolean forward )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateShotExtend <" + id + "> " + extend );
    // if ( myDB == null ) return;
    updateShotExtendStmt.bindLong( 1, extend );
    updateShotExtendStmt.bindLong( 2, sid );
    updateShotExtendStmt.bindLong( 3, id );
    updateShotExtendStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateShotExtend( id, sid, extend );
      }
    }
  }

  public void updateShotFlag( long id, long sid, long flag, boolean forward )
  {
    // if ( myDB == null ) return;
    updateShotFlagStmt.bindLong( 1, flag );
    updateShotFlagStmt.bindLong( 2, sid );
    updateShotFlagStmt.bindLong( 3, id );
    updateShotFlagStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateShotFlag( id, sid, flag );
      }
    }
  }

  public void updateShotComment( long id, long sid, String comment, boolean forward )
  {
    // if ( myDB == null ) return;
    updateShotCommentStmt.bindString( 1, comment );
    updateShotCommentStmt.bindLong( 2, sid );
    updateShotCommentStmt.bindLong( 3, id );
    updateShotCommentStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateShotComment( id, sid, comment );
      }
    }
  }


  public void deleteShot( long id, long sid, boolean forward ) 
  {
    // if ( myDB == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "deleteShot: " + id + "/" + sid );
    deleteShotStmt.bindLong( 1, sid ); 
    deleteShotStmt.bindLong( 2, id );
    deleteShotStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onDeleteShot( id, sid );
      }
    }
  }

  public void undeleteShot( long id, long sid, boolean forward ) 
  {
    // if ( myDB == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "undeleteShot: " + id + "/" + sid );
    undeleteShotStmt.bindLong( 1, sid ); 
    undeleteShotStmt.bindLong( 2, id );
    undeleteShotStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUndeleteShot( id, sid );
      }
    }
  }
  
  public void updateShotNameAndExtend( long sid, ArrayList< DistoXDBlock > updatelist )
  {
    try {
      // myDB.execSQL("PRAGMA synchronous=OFF");
      myDB.setLockingEnabled( false );
      myDB.beginTransaction();
      for ( DistoXDBlock b : updatelist ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateShotNameAndExtend <" + b.mFrom + "-" + b.mTo + "> " + b.mExtend );
        updateShotNameAndExtendStmt.bindString( 1, b.mFrom );
        updateShotNameAndExtendStmt.bindString( 2, b.mTo );
        updateShotNameAndExtendStmt.bindLong(   3, b.mExtend );
        updateShotNameAndExtendStmt.bindLong(   4, (b.mType == DistoXDBlock.BLOCK_SEC_LEG)? 1 : 0 );
        updateShotNameAndExtendStmt.bindLong(   5, sid );
        updateShotNameAndExtendStmt.bindLong(   6, b.mId );
        updateShotNameAndExtendStmt.execute();
      }
      myDB.setTransactionSuccessful();
    } finally {
      myDB.endTransaction();
      myDB.setLockingEnabled( true );
      // myDB.execSQL("PRAGMA synchronous=NORMAL");
    }
    // synchronized( mListeners )
    for ( DataListener listener : mListeners ) {
      for ( DistoXDBlock b : updatelist ) {
        listener.onUpdateShotName( b.mId, sid, b.mFrom, b.mTo );
        listener.onUpdateShotExtend( b.mId, sid, b.mExtend );
        listener.onUpdateShotLeg( b.mId, sid, (b.mType == DistoXDBlock.BLOCK_SEC_LEG)? 1 : 0 );
      }
    }
  }

  // called by the importXXXTask's
  public long insertShots( long sid, long id, ArrayList< ParserShot > shots )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "insertShots list size " + shots.size() );
    // if ( myDB == null ) return -1;
    InsertHelper ih = new InsertHelper( myDB, SHOT_TABLE );
    final int surveyIdCol = ih.getColumnIndex( "surveyId" );
    final int idCol       = ih.getColumnIndex( "id" );
    final int fStationCol = ih.getColumnIndex( "fStation" );
    final int tStationCol = ih.getColumnIndex( "tStation" );
    final int distanceCol = ih.getColumnIndex( "distance" );
    final int bearingCol  = ih.getColumnIndex( "bearing" );
    final int clinoCol    = ih.getColumnIndex( "clino" );
    final int rollCol     = ih.getColumnIndex( "roll" );
    final int accelerationCol = ih.getColumnIndex( "acceleration" );
    final int magneticCol = ih.getColumnIndex( "magnetic" );
    final int dipCol      = ih.getColumnIndex( "dip" );
    final int extendCol   = ih.getColumnIndex( "extend" );
    final int flagCol     = ih.getColumnIndex( "flag" );
    final int legCol      = ih.getColumnIndex( "leg" );
    final int statusCol   = ih.getColumnIndex( "status" );
    final int commentCol  = ih.getColumnIndex( "comment" );
    final int typeCol     = ih.getColumnIndex( "type" );
    try {
      // myDB.execSQL("PRAGMA synchronous=OFF");
      myDB.setLockingEnabled( false );
      myDB.beginTransaction();
      for ( ParserShot s : shots ) {
        ih.prepareForInsert();
        ih.bind( surveyIdCol, sid );
        ih.bind( idCol, id );
        ih.bind( fStationCol, s.from );
        ih.bind( tStationCol, s.to);
        ih.bind( distanceCol, s.len );
        ih.bind( bearingCol, s.ber );
        ih.bind( clinoCol, s.cln );
        ih.bind( rollCol, s.rol);
        ih.bind( accelerationCol, 0.0);
        ih.bind( magneticCol, 0.0);
        ih.bind( dipCol, 0.0);
        ih.bind( extendCol, s.extend );
        ih.bind( flagCol, s.duplicate ? DistoXDBlock.BLOCK_DUPLICATE : s.surface ? DistoXDBlock.BLOCK_SURFACE : 0 );
        ih.bind( legCol, 0 );
        ih.bind( statusCol, 0 );
        ih.bind( commentCol, s.comment );
        ih.bind( typeCol, 0 );
        ih.execute();
        // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "shot " + id + ": " + s.from + "-" + s.to );
        ++id;
      }
      myDB.setTransactionSuccessful();
    } finally {
      ih.close();
      myDB.endTransaction();
      myDB.setLockingEnabled( true );
      // myDB.execSQL("PRAGMA synchronous=NORMAL");
    }
    // synchronized( mListeners )
    for ( DataListener listener : mListeners ) {
      for ( ParserShot s : shots ) {
        listener.onInsertShot( sid, id, s.from, s.to, s.len, s.ber, s.cln, s.rol, s.extend, 
                          s.duplicate ? DistoXDBlock.BLOCK_DUPLICATE : s.surface ? DistoXDBlock.BLOCK_SURFACE : 0,
                          0, 0, s.comment );
      }
    }
    return id;
  }
  
  public long insertShot( long sid, long id, double d, double b, double c, double r, int type, boolean forward )
  {
    long extend = 0L;
    if ( TopoDroidSetting.mSplayExtend ) { // FIXME DataHelper should not do this 
      extend = ( b < 180.0 )? 1L : -1L;
    }
    return insertShot( sid, id, "", "",  d, b, c, r, extend, DistoXDBlock.BLOCK_SURVEY, 0L, 0L, type, "", forward );
  }

  public void updateShotAMDR( long id, long sid, double acc, double mag, double dip, double r, boolean forward )
  {
    // if ( myDB == null ) return;
    updateShotAMDRStmt.bindDouble( 1, acc );
    updateShotAMDRStmt.bindDouble( 2, mag );
    updateShotAMDRStmt.bindDouble( 3, dip );
    updateShotAMDRStmt.bindDouble( 4, r );
    updateShotAMDRStmt.bindLong( 5, sid );
    updateShotAMDRStmt.bindLong( 6, id );
    updateShotAMDRStmt.execute();
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onUpdateShotAMDR( sid, id, acc, mag, dip, r );
      }
    }
  }

  public void transferShots( long sid, long old_sid, long old_id )
  {
    long max_id = maxId( SHOT_TABLE, old_sid );
    while ( old_id < max_id ) {
      transferShotStmt.bindLong(1, sid);
      transferShotStmt.bindLong(2, myNextId);
      transferShotStmt.bindLong(3, old_sid);
      transferShotStmt.bindLong(4, old_id);
      transferShotStmt.execute();
      ++ myNextId;
      ++ old_id;
    }
  }

  public long insertShotAt( long sid, long at, double d, double b, double c, double r, int type, boolean forward )
  {
    // if ( myDB == null ) return -1;
    shiftShotsId( sid, at );
    ++ myNextId;
    ContentValues cv = new ContentValues();
    cv.put( "surveyId", sid );
    cv.put( "id",       at );
    cv.put( "fStation", "" ); // from );
    cv.put( "tStation", "" ); // to );
    cv.put( "distance", d );
    cv.put( "bearing",  b );
    cv.put( "clino",    c );
    cv.put( "roll",     r );
    cv.put( "acceleration", 0.0 );
    cv.put( "magnetic", 0.0 );
    cv.put( "dip",      0.0 );
    cv.put( "extend",   (b < 180.0)? 1L : -1L ); // extend );
    cv.put( "flag",     DistoXDBlock.BLOCK_SURVEY ); // flag );
    cv.put( "leg",      0L ); // leg );
    cv.put( "status",   0L ); // status );
    cv.put( "comment",  "" ); // comment );
    cv.put( "type",     type ); 
    myDB.insert( SHOT_TABLE, null, cv );
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onInsertShotAt( sid, at, d, b, c, r );
      }
    }
    return at;
  }

  // return the new-shot id
  public long insertShot( long sid, long id, String from, String to, 
                          double d, double b, double c, double r, 
                          long extend, long flag, long leg, long status, int type,
                          String comment, boolean forward )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_DB, "insertShot <" + id + "> " + from + "-" + to + " extend " + extend );
    // if ( myDB == null ) return -1;
    if ( id == -1L ) {
      ++ myNextId;
      id = myNextId;
    } else {
      myNextId = id;
    }
    ContentValues cv = new ContentValues();
    cv.put( "surveyId", sid );
    cv.put( "id",       id );
    cv.put( "fStation", from );
    cv.put( "tStation", to );
    cv.put( "distance", d );
    cv.put( "bearing",  b );
    cv.put( "clino",    c );
    cv.put( "roll",     r );
    cv.put( "acceleration", 0.0 );
    cv.put( "magnetic", 0.0 );
    cv.put( "dip",      0.0 );
    cv.put( "extend",   extend );
    cv.put( "flag",     flag );
    cv.put( "leg",      leg );
    cv.put( "status",   status );
    cv.put( "comment",  comment );
    cv.put( "type",     type );
    myDB.insert( SHOT_TABLE, null, cv );
    if ( forward ) {
      // synchronized( mListeners )
      for ( DataListener listener : mListeners ) {
        listener.onInsertShot( sid,  id, from, to, d, b, c, r, extend, flag, leg, status, comment );
      }
    }
    return id;
  }

  // -----------------------------------------------------------------
  // PLOT

  public void updatePlot( long plot_id, long survey_id, double xoffset, double yoffset, double zoom )
  {
    // if ( myDB == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB,
    //                   "updatePlot: " + plot_id + "/" + survey_id + " x " + xoffset + " y " + yoffset + " zoom " + zooom);
    updatePlotStmt.bindDouble( 1, xoffset );
    updatePlotStmt.bindDouble( 2, yoffset );
    updatePlotStmt.bindDouble( 3, zoom );
    updatePlotStmt.bindLong( 4, survey_id );
    updatePlotStmt.bindLong( 5, plot_id );
    updatePlotStmt.execute();
  }
 
  public void updatePlotView( long plot_id, long survey_id, String view )
  {
    // if ( myDB == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updatePlot: " + plot_id + "/" + survey_id + " view " + view );
    updatePlotViewStmt.bindString( 1, view );
    updatePlotViewStmt.bindLong( 2, survey_id );
    updatePlotViewStmt.bindLong( 3, plot_id );
    updatePlotViewStmt.execute();
  }
   
  /** DROP is a real record delete from the database table
   */
  public void dropPlot( long plot_id, long survey_id )
  {
    // if ( myDB == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "dropPlot: " + plot_id + "/" + survey_id );
    dropPlotStmt.bindLong( 1, survey_id );
    dropPlotStmt.bindLong( 2, plot_id );
    dropPlotStmt.execute();
  }

  public void deletePlot( long plot_id, long survey_id )
  {
    // if ( myDB == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "deletePlot: " + plot_id + "/" + survey_id );
    deletePlotStmt.bindLong( 1, survey_id );
    deletePlotStmt.bindLong( 2, plot_id );
    deletePlotStmt.execute();
  }
  
  public void undeletePlot( long plot_id, long survey_id )
  {
    // if ( myDB == null ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "undeletePlot: " + plot_id + "/" + survey_id );
    undeletePlotStmt.bindLong( 1, survey_id );
    undeletePlotStmt.bindLong( 2, plot_id );
    undeletePlotStmt.execute();
  }
  

  // FIXME_SKETCH_3D
  public void updateSketch( long sketch_id, long survey_id, 
                            String st1, String st2,
                            double xofftop, double yofftop, double zoomtop,
                            double xoffside, double yoffside, double zoomside,
                            double xoff3d, double yoff3d, double zoom3d,
                            double east, double south, double vert, double azimuth, double clino )
  {
    // if ( myDB == null ) return;
    updateSketchStmt.bindString( 1, st1 );
    updateSketchStmt.bindString( 2, st2 );
    updateSketchStmt.bindDouble( 3, xofftop );
    updateSketchStmt.bindDouble( 4, yofftop );
    updateSketchStmt.bindDouble( 5, zoomtop );
    updateSketchStmt.bindDouble( 6, xoffside );
    updateSketchStmt.bindDouble( 7, yoffside );
    updateSketchStmt.bindDouble( 8, zoomside );
    updateSketchStmt.bindDouble( 9, xoff3d );
    updateSketchStmt.bindDouble(10, yoff3d );
    updateSketchStmt.bindDouble(11, zoom3d );
    updateSketchStmt.bindDouble(12, east );
    updateSketchStmt.bindDouble(13, south );
    updateSketchStmt.bindDouble(14, vert );
    updateSketchStmt.bindDouble(15, azimuth );
    updateSketchStmt.bindDouble(16, clino );
    updateSketchStmt.bindLong( 17, survey_id );
    updateSketchStmt.bindLong( 18, sketch_id );
    updateSketchStmt.execute();
  }
  
  public void deleteSketch( long sketch_id, long survey_id )
  {
    // if ( myDB == null ) return;
    deleteSketchStmt.bindLong( 1, survey_id );
    deleteSketchStmt.bindLong( 2, sketch_id );
    deleteSketchStmt.execute();
  }
  // END_SKETCH_3D

  
  // ----------------------------------------------------------------------
  // CALIBRATION DATA

  void deleteGM( long cid, long id, boolean delete )
  {
    // if ( myDB == null ) return;
    deleteGMStmt.bindLong( 1, delete? 1 : 0 );
    deleteGMStmt.bindLong( 2, cid );
    deleteGMStmt.bindLong( 3, id );
    deleteGMStmt.execute();
  }

  public void doDeleteCalib( long cid ) 
  {
    // if ( myDB == null ) return;
    doDeleteGMStmt.bindLong( 1, cid );
    doDeleteGMStmt.execute();
    doDeleteCalibStmt.bindLong( 1, cid );
    doDeleteCalibStmt.execute();
  }

  public long updateGMName( long id, long cid, String grp )
  {
    // if ( myDB == null ) return -1;
    updateGMGroupStmt.bindString( 1, grp );
    updateGMGroupStmt.bindLong( 2, cid );
    updateGMGroupStmt.bindLong( 3, id );
    updateGMGroupStmt.execute();
    return 0;
  }

  public long updateGMError( long id, long cid, double error )
  {
    // if ( myDB == null ) return -1;
    updateGMErrorStmt.bindDouble( 1, error );
    updateGMErrorStmt.bindLong( 2, cid );
    updateGMErrorStmt.bindLong( 3, id );
    updateGMErrorStmt.execute();
    return 0;
  }

  public long insertGM( long cid, long gx, long gy, long gz, long mx, long my, long mz )
  {
    // if ( myDB == null ) return -1;
    ++ myNextCId;
    ContentValues cv = new ContentValues();
    cv.put( "calibId", cid );
    cv.put( "id",      myNextCId );
    cv.put( "gx", gx );
    cv.put( "gy", gy );
    cv.put( "gz", gz );
    cv.put( "mx", mx );
    cv.put( "my", my );
    cv.put( "mz", mz );
    cv.put( "grp", 0 );
    cv.put( "error", 0.0 );
    cv.put( "status", 0 );
    long ret= myDB.insert( GM_TABLE, null, cv );
    return ret;
  }
  
  // ----------------------------------------------------------------------
  // SELECT STATEMENTS

  public List< SensorInfo > selectAllSensors( long sid, long status )
  {
    List< SensorInfo > list = new ArrayList< SensorInfo >();
    // if ( myDB == null ) return list;
    Cursor cursor = myDB.query( SENSOR_TABLE,
       		         new String[] { "id", "shotId", "title", "date", "comment", "type", "value" }, // columns
                                "surveyId=? AND status=?", 
                                new String[] { Long.toString(sid), Long.toString(status) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
    if (cursor.moveToFirst()) {
      do {
        list.add( new SensorInfo( sid, 
                                 cursor.getLong(0), // id
                                 cursor.getLong(1), // shot-id
                                 cursor.getString(2), // title
                                 null,                // shot name
                                 cursor.getString(3), // date
                                 cursor.getString(4), // comment
                                 cursor.getString(5), // type
                                 cursor.getString(6) ) ); // value
      } while (cursor.moveToNext());
    }
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllSensors list size " + list.size() );
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }
    for ( SensorInfo si : list ) { // set shot-names to the sensor infos
      cursor = myDB.query( SHOT_TABLE, 
                           new String[] { "fStation", "tStation" },
                           "surveyId=? and id=?",
                           new String[] { Long.toString(sid), Long.toString(si.shotid) },
                           null, null, null );
      if (cursor.moveToFirst()) {
        si.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
      }
      if (cursor != null && !cursor.isClosed()) cursor.close();
    }
    return list;
  }

  public List< PhotoInfo > selectAllPhotos( long sid, long status )
  {
    List< PhotoInfo > list = new ArrayList< PhotoInfo >();
    // if ( myDB == null ) return list;
    Cursor cursor = myDB.query( PHOTO_TABLE,
       		         new String[] { "id", "shotId", "title", "date", "comment" }, // columns
                                "surveyId=? AND status=?", 
                                new String[] { Long.toString(sid), Long.toString(status) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
    if (cursor.moveToFirst()) {
      do {
        list.add( new PhotoInfo( sid, 
                                 cursor.getLong(0), // id
                                 cursor.getLong(1),
                                 cursor.getString(2),
                                 null,              // shot name
                                 cursor.getString(3),
                                 cursor.getString(4) ) );
      } while (cursor.moveToNext());
    }
    // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllPhotos list size " + list.size() );
    if (cursor != null && !cursor.isClosed()) {
      cursor.close();
    }
    for ( PhotoInfo pi : list ) { // fill in the shot-name of the photos
      cursor = myDB.query( SHOT_TABLE, 
                           new String[] { "fStation", "tStation" },
                           "surveyId=? and id=?",
                           new String[] { Long.toString(sid), Long.toString(pi.shotid) },
                           null, null, null );
      if (cursor.moveToFirst()) {
        pi.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
      }
      if (cursor != null && !cursor.isClosed()) cursor.close();
    }
    return list;
  }

/* FIXME PHOTO
   public List< PhotoInfo > selectPhotoAtShot( long sid, long shotid )
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectPhotoAtShot " + shotid + "/" + sid );
     List< PhotoInfo > list = new ArrayList< PhotoInfo >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB,query( SHOT_TABLE, 
                                 new String[] { "fStation", "tStation" },
                                 "surveyId=? and shotId=?",
                                 new String[] { Long.toString(sid), Long.toString(shotid) },
                                 null, null, null );
     if (cursor.moveToFirst()) {
       String shot_name = cursor.getString(0) + "-" + cursor.getString(1);
       if (cursor != null && !cursor.isClosed()) cursor.close();
       cursor = myDB.query( PHOTO_TABLE,
			    new String[] { "id", "title", "date", "comment" }, // columns
                            "surveyId=? and shotId=?",  // selection = WHERE clause (without "WHERE")
                            new String[] { Long.toString(sid), Long.toString(shotid) },     // selectionArgs
                            null,  // groupBy
                            null,  // having
                            null ); // order by
       if (cursor.moveToFirst()) {
         do {
           list.add( new PhotoInfo( sid,
                                    cursor.getLong(0),
                                    shotid,
                                    cursor.getString(1),
                                    null,
                                    cursor.getString(2),
                                    cursor.getString(3) ) );
         } while (cursor.moveToNext());
       }
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }
*/

   public List< FixedInfo > selectAllFixed( long sid, int status )
   {
     List<  FixedInfo  > list = new ArrayList<  FixedInfo  >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query( FIXED_TABLE,
			         new String[] { "id", "station", "longitude", "latitude", "altitude", "altimetric", "comment" }, // columns
                                 "surveyId=? and status=?",  // selection = WHERE clause (without "WHERE")
                                new String[] { Long.toString(sid), Long.toString(status) },     // selectionArgs
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       do {
         list.add( new FixedInfo( cursor.getLong(0),
                                  cursor.getString(1),
                                  cursor.getDouble(2),
                                  cursor.getDouble(3),
                                  cursor.getDouble(4),
                                  cursor.getDouble(5),
                                  cursor.getString(6) ) );
       } while (cursor.moveToNext());
     }
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "seletAllFixed list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   // FIXME_SKETCH_3D
   public List< Sketch3dInfo > selectAllSketches( long sid, long status )
   {
     List<  Sketch3dInfo  > list = new ArrayList<  Sketch3dInfo  >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query( SKETCH_TABLE,
                 new String[] { "id", "name", "start", "st1", "st2", "xoffsettop", "yoffsettop", "zoomtop", "xoffsetside", "yoffsetside", "zoomside", "xoffset3d", "yoffset3d", "zoom3d", "east", "south", "vert", "azimuth", "clino" },
                                "surveyId=? and status=?", 
                                new String[] { Long.toString(sid), Long.toString(status) }, 
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         Sketch3dInfo sketch = new  Sketch3dInfo ();
         sketch.surveyId = sid;
         sketch.id    = cursor.getLong(0);
         sketch.name  = cursor.getString(1);
         sketch.start = cursor.getString(2);
         sketch.st1   = cursor.getString(3);
         sketch.st2   = cursor.getString(4);
         sketch.xoffset_top = (float)( cursor.getDouble(5) );
         sketch.yoffset_top = (float)( cursor.getDouble(6) );
         sketch.zoom_top    = (float)( cursor.getDouble(7) );
         sketch.xoffset_side = (float)( cursor.getDouble(8) );
         sketch.yoffset_side = (float)( cursor.getDouble(9) );
         sketch.zoom_side    = (float)( cursor.getDouble(10) );
         sketch.xoffset_3d = (float)( cursor.getDouble(11) );
         sketch.yoffset_3d = (float)( cursor.getDouble(12) );
         sketch.zoom_3d    = (float)( cursor.getDouble(13) );
         sketch.east    = (float)( cursor.getDouble(14) );
         sketch.south   = (float)( cursor.getDouble(15) );
         sketch.vert    = (float)( cursor.getDouble(16) );
         sketch.azimuth = (float)( cursor.getDouble(17) );
         sketch.clino   = (float)( cursor.getDouble(18) );
         list.add( sketch );
       } while (cursor.moveToNext());
     }
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllPlots list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }
   // END_SKETCH_3D

   public boolean hasSurveyPlot( long sid, String name )
   {
     boolean ret = false;
     // if ( myDB == null ) return ret;
     Cursor cursor = myDB.query(PLOT_TABLE,
			        new String[] { "id", "name" },
                                "surveyId=? and name=?",
                                new String[] { Long.toString(sid), name }, 
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) ret = true;
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return ret;
   }

   public boolean hasSurveyStation( long sid, String start )
   {
     boolean ret = false;
     // if ( myDB == null ) return ret;
     Cursor cursor = myDB.query(SHOT_TABLE,
			        new String[] { "id", "fStation", "tStation" },
                                "surveyId=? and ( fStation=? or tStation=? )",
                                new String[] { Long.toString(sid), start, start }, 
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) ret = true;
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return ret;
   }


   public int maxPlotIndex( long sid ) 
   {
     int ret = 0;
     // if ( myDB == null ) return ret;
     Cursor cursor = myDB.query(PLOT_TABLE,
			        new String[] { "id", "name", "type" },
                                "surveyId=?",
                                new String[] { Long.toString(sid) }, 
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         int type = cursor.getInt(2);
         if ( type == PlotInfo.PLOT_PLAN ) { // FIXME || type == PlotInfo.PLOT_EXTENDED
           int r = 0;
           byte[] name = cursor.getString(1).getBytes();
           for ( int k=0; k<name.length; ++k ) {
             if ( name[k] >= 0x30 && name[k] <= 0x39 ) {
               r = 10*r + ( name[k] - 0x30 );
             } else {
               break;
             }
           }
           if ( r > ret ) ret = r;
         }
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return ret;
   }

   public List< PlotInfo > selectAllPlots( long sid, long status )
   {
     List<  PlotInfo  > list = new ArrayList<  PlotInfo  >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query(PLOT_TABLE,
			        new String[] { "id", "name", "type", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino" }, // columns
                                "surveyId=? and status=?", 
                                new String[] { Long.toString(sid), Long.toString(status) }, 
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         PlotInfo plot = new  PlotInfo ();
         // plot.setId( cursor.getLong(0), sid );
         plot.surveyId = sid;
         plot.id    = cursor.getLong(0);
         plot.name = cursor.getString(1);
         plot.type = cursor.getInt(2);
         plot.start = cursor.getString(3);
         plot.view  = cursor.getString(4);
         plot.xoffset = (float)(cursor.getDouble(5));
         plot.yoffset = (float)(cursor.getDouble(6));
         plot.zoom    = (float)(cursor.getDouble(7));
         plot.azimuth = (float)(cursor.getDouble(8));
         plot.clino   = (float)(cursor.getDouble(9));
         list.add( plot );
         // Log.v( TopoDroidApp.TAG, "plot " + plot.name + " azimuth " + plot.azimuth );
       } while (cursor.moveToNext());
     }
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllPlots list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public boolean hasShot( long sid, String fStation, String tStation )
   {
     // if ( myDB == null ) return false;
     Cursor cursor = myDB.query( SHOT_TABLE,
       new String[] { "fStation", "tStation" }, // columns
       "surveyId=? and ( fStation=? and tStation=? ) or ( fStation=? and tStation=? )", 
       new String[] { Long.toString(sid), fStation, tStation, tStation, fStation },
       null,   // groupBy
       null,   // having
       null ); // order by
     boolean ret = cursor.moveToFirst();
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return ret;
   }
     
   public String nextStation( long sid, String fStation )
   {
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query( SHOT_TABLE,
       new String[] { "tStation" }, // columns
       "surveyId=? and fStation=? ", 
       new String[] { Long.toString(sid), fStation },
       null,   // groupBy
       null,   // having
       null ); // order by
     String ret = null;
     if ( cursor.moveToFirst() ) {
       do {
         ret = cursor.getString( 0 );
       } while ( ret.length() == 0 && cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return ret;
   }

   public DistoXDBlock selectShot( long id, long sid )
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectShot " + id + "/" + sid );
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
       "surveyId=? and id=?", 
       new String[] { Long.toString(sid), Long.toString(id) },
       null,  // groupBy
       null,  // having
       null ); // order by
     DistoXDBlock block = null;
     if (cursor.moveToFirst()) {
       block = new DistoXDBlock();
       fillBlock( sid, block, cursor );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return block;
   }

   public DistoXDBlock selectLastLegShot( long survey_id )
   {
     return selectPreviousLegShot( myNextId+1, survey_id );
   }

   public DistoXDBlock selectPreviousLegShot( long shot_id, long survey_id )
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectPreviousLegShot " + shot_id + "/" + survey_id );
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
       "surveyId=? and id<?",
       new String[] { Long.toString(survey_id), Long.toString(shot_id) },
       null,  // groupBy
       null,  // having
       "id DESC" ); // order by
       // "1" ); // no limit
     DistoXDBlock block = null;
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getString(0).length() > 0 && cursor.getString(1).length() > 0 ) {
           block = new DistoXDBlock();
           fillBlock( survey_id, block, cursor );
         }  
       } while (block == null && cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return block;
   }

   String getLastStationName( long survey_id )
   {
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "fStation", "tStation" },
       "surveyId=?",
       new String[] { Long.toString(survey_id) },
       null,  // groupBy
       null,  // having
       "id DESC" // order by
     );
     String ret = "0";
     if (cursor.moveToFirst()) {
       do {
         String from = cursor.getString(0);
         String to = cursor.getString(1);
         if ( from.length() > 0 && to.length() > 0 ) {
           ret = ( from.compareTo(to) > 0 )? from : to;
           break;
         }
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return ret;
   }

   // FIXME this is ok only for numbers
   // String getNextStationName( long survey_id )
   // {
   //   // if ( myDB == null ) return null;
   //   Cursor cursor = myDB.query(SHOT_TABLE,
   //     new String[] { "fStation", "tStation" },
   //     "surveyId=?",
   //     new String[] { Long.toString(survey_id) },
   //     null,  // groupBy
   //     null,  // having
   //     "id DESC" // order by
   //   );
   //   int ret = -1;
   //   if (cursor.moveToFirst()) {
   //     do {
   //       try {
   //         if ( cursor.getString(0).length() > 0 ) {
   //           int f = Integer.parseInt( cursor.getString(0) );
   //           if ( f > ret ) ret = f;
   //         }
   //         if ( cursor.getString(1).length() > 0 ) {
   //           int t = Integer.parseInt( cursor.getString(1) );
   //           if ( t > ret ) ret = t;
   //         }
   //       } catch ( NumberFormatException e ) {
   //         TopoDroidLog.Log( TopoDroidLog.LOG_ERR,
   //           "getNextStationName parseInt error: " + cursor.getString(0) + " " + cursor.getString(1) );
   //       }
   //     } while (cursor.moveToNext());
   //   }
   //   if (cursor != null && !cursor.isClosed()) {
   //     cursor.close();
   //   }
   //   ++ ret;
   //   return Integer.toString(ret);
   // }

   public DistoXDBlock selectNextLegShot( long shot_id, long survey_id ) 
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectNextLegShot " + shot_id + "/" + survey_id );
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
       "surveyId=? and id>?",
       new String[] { Long.toString(survey_id), Long.toString(shot_id) },
       null,  // groupBy
       null,  // having
       "id ASC" ); // order by
       // "1" ); // no limit
     DistoXDBlock block = null;
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getString(0).length() > 0 && cursor.getString(1).length() > 0 ) {
           block = new DistoXDBlock();
           fillBlock( survey_id, block, cursor );
         }  
       } while (block == null && cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return block;
   }

   // private boolean hasShotAtStation( long id, long sid, String station )
   // {
   //   // if ( myDB == null ) return false;
   //   Cursor cursor = myDB.query(SHOT_TABLE,
   //     new String[] { "id", "fStation", "tStation" }, // columns
   //     "surveyId=? and status=0 and ( fStation=? or tStation=? ) and id!=?",
   //     new String[] { Long.toString(sid), station, station, Long.toString(id) },
   //     null,  // groupBy
   //     null,  // having
   //     "id" ); // order by
   //   boolean ret = false;
   //   if (cursor.moveToFirst()) {
   //     do {
   //       long idc = (long)cursor.getLong(0);
   //       TopoDroidLog.Log( TopoDroidLog.LOG_DB, "hasShotAtStation " + id + " " + idc ); 
   //       if ( id != idc ) {
   //         ret = true;
   //       }
   //     } while (ret == false && cursor.moveToNext());
   //   }
   //   if (cursor != null && !cursor.isClosed()) {
   //     cursor.close();
   //   }
   //   TopoDroidLog.Log( TopoDroidLog.LOG_DB, "hasShotAtStation returns " + ret );
   //   return ret;
   // }

   public List<DistoXDBlock> selectShotsAfterId( long sid, long id , long status )
   {
     List< DistoXDBlock > list = new ArrayList< DistoXDBlock >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
       "surveyId=? and status=? and id>?",
       new String[] { Long.toString(sid), Long.toString(TopoDroidApp.STATUS_NORMAL), Long.toString(id) },
       null,  // groupBy
       null,  // having
       "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         DistoXDBlock block = new DistoXDBlock();
         fillBlock( sid, block, cursor );
         list.add( block );
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;

   }

   public List<DistoXDBlock> selectAllShotsAtStations( long sid, String station1, String station2 )
   {
     List< DistoXDBlock > list = new ArrayList< DistoXDBlock >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
       "surveyId=? and status=? and ( fStation=? or fStation=? )", 
       new String[] { Long.toString(sid), Long.toString(TopoDroidApp.STATUS_NORMAL), station1, station2 },
       null,  // groupBy
       null,  // having
       "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getLong(11) == 0 ) { // skip leg-blocks
           DistoXDBlock block = new DistoXDBlock();
           fillBlock( sid, block, cursor );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllShotsAtStation list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public List<DistoXDBlock> selectAllShotsAtStation( long sid, String station )
   {
     List< DistoXDBlock > list = new ArrayList< DistoXDBlock >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
       "surveyId=? and status=? and fStation=?", 
       new String[] { Long.toString(sid), Long.toString(TopoDroidApp.STATUS_NORMAL), station },
       null,  // groupBy
       null,  // having
       "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getLong(11) == 0 ) { // skip leg-blocks
           DistoXDBlock block = new DistoXDBlock();
           fillBlock( sid, block, cursor );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllShotsAtStation list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public List<DistoXDBlock> selectAllShotsToStation( long sid, String station )
   {
     List< DistoXDBlock > list = new ArrayList< DistoXDBlock >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
       "surveyId=? and status=? and tStation=?", 
       new String[] { Long.toString(sid), Long.toString(TopoDroidApp.STATUS_NORMAL), station },
       null,  // groupBy
       null,  // having
       "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getLong(11) == 0 ) { // skip leg-blocks
           DistoXDBlock block = new DistoXDBlock();
           fillBlock( sid, block, cursor );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllShotsToStation list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }


   public List<DistoXDBlock> selectAllShots( long sid, long status )
   {
     List< DistoXDBlock > list = new ArrayList< DistoXDBlock >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
       "surveyId=? and status=?",
       new String[] { Long.toString(sid), Long.toString(status) },
       null,  // groupBy
       null,  // having
       "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         DistoXDBlock block = new DistoXDBlock();
         fillBlock( sid, block, cursor );
         list.add( block );
       } while (cursor.moveToNext());
     }
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllShots list size " + list.size() );
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public void resetAllGMs( long cid )
   {
     resetAllGMStmt.bindLong( 1, cid );
     resetAllGMStmt.execute();
   }

   public List<CalibCBlock> selectAllGMs( long cid, int status )
   {
     List< CalibCBlock > list = new ArrayList< CalibCBlock >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query(GM_TABLE,
                                new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error", "status" }, // columns
                                "calibId=?",
                                new String[] { Long.toString(cid) },
                                null,  // groupBy
                                null,  // having
                                "id" ); // order by
     if (cursor.moveToFirst()) {
       do {
         if ( status >= (int)cursor.getLong(9) ) { // status == 0 --> only good shots
                                                   // status == 1 --> all shots
           CalibCBlock block = new CalibCBlock();
           block.setId( cursor.getLong(0), cid );
           block.setData( 
             cursor.getLong(1),
             cursor.getLong(2),
             cursor.getLong(3),
             cursor.getLong(4),
             cursor.getLong(5),
             cursor.getLong(6) );
           block.setGroup( cursor.getLong(7) );
           block.setError( (float)( cursor.getDouble(8) ) );
           block.setStatus( cursor.getLong(9) );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return list;
   }

   public CalibCBlock selectGM( long id, long cid )
   {
     CalibCBlock block = null;
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query(GM_TABLE,
                                new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error", "status" }, // columns
                                "calibId=? and id=?", 
                                new String[] { Long.toString(cid), Long.toString(id) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       block = new CalibCBlock();
       block.setId( cursor.getLong(0), cid );
       block.setData( 
         cursor.getLong(1),
         cursor.getLong(2),
         cursor.getLong(3),
         cursor.getLong(4),
         cursor.getLong(5),
         cursor.getLong(6) );
       block.setGroup( cursor.getLong(7) );
       block.setError( (float)( cursor.getDouble(8) ) );
       block.setStatus( cursor.getLong(9) );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return block;
   }

   public SurveyInfo selectSurveyInfo( long sid )
   {
     SurveyInfo info = null;
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query( SURVEY_TABLE,
                                new String[] { "name", "day", "team", "declination", "comment" }, // columns
                                "id=?",
                                new String[] { Long.toString(sid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       info = new SurveyInfo();
       info.id      = sid;
       info.name    = cursor.getString( 0 );
       info.date    = cursor.getString( 1 );
       info.team    = cursor.getString( 2 );
       info.declination = (float)(cursor.getDouble( 3 ));
       info.comment = cursor.getString( 4 );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return info;
   }

   public int selectCalibAlgo( long cid )
   {
     int algo = 0; // default CALIB_ALGO_AUTO
     // if ( myDB == null ) return 0;
     Cursor cursor = myDB.query( CALIB_TABLE,
                                new String[] { "algo" }, // columns
                                "id=?",
                                new String[] { Long.toString(cid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       algo = (int)cursor.getLong( 0 );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return algo;
   }
 
   public CalibInfo selectCalibInfo( long cid )
   {
     CalibInfo info = null;
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query( CALIB_TABLE,
                                new String[] { "name", "day", "device", "comment", "algo" }, // columns
                                "id=?",
                                new String[] { Long.toString(cid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       info = new CalibInfo( 
               cid,
               cursor.getString( 0 ),
               cursor.getString( 1 ),
               cursor.getString( 2 ),
               cursor.getString( 3 ),
               (int)cursor.getLong( 4 ) );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return info;
   }

   public void selectCalibError( long cid, CalibResult res )
   {
     // if ( myDB == null ) return;
     Cursor cursor = myDB.query( CALIB_TABLE,
                                new String[] { "error", "max_error", "iterations" }, // columns
                                "id=?",
                                new String[] { Long.toString(cid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       // Log.v( "DistoX", "select calib error " + cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) );
       try {
         String str = cursor.getString(0);
         if ( str != null ) res.error = Float.parseFloat( str );
         str = cursor.getString(1);
         if ( str != null ) res.max_error = Float.parseFloat( str );
         str = cursor.getString(2);
         if ( str != null ) res.iterations = Integer.parseInt( str );
       } catch ( NumberFormatException e ) {
         TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "selectCalibError parse Float error: calib ID " + cid );
       }
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
   }

   public String selectCalibCoeff( long cid )
   {
     String coeff = null;
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query( CALIB_TABLE,
                                new String[] { "coeff" }, // columns
                                "id=?",
                                new String[] { Long.toString(cid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
     if (cursor.moveToFirst()) {
       coeff = cursor.getString( 0 );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return coeff;
   }
    
   // ----------------------------------------------------------------------
   // SELECT: LIST SURVEY / CABIL NAMES

   private List<String> selectAllNames( String table )
   {
     TopoDroidLog.Log( TopoDroidLog.LOG_DB, "selectAllNames table " + table );

     List< String > list = new ArrayList< String >();
     // if ( myDB == null ) return list;
     Cursor cursor = myDB.query( table,
                                new String[] { "name" }, // columns
                                null, null, null, null, "name" );
     if (cursor.moveToFirst()) {
       do {
         list.add( cursor.getString(0) );
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     TopoDroidLog.Log( TopoDroidLog.LOG_DB, "found " + list.size() + " names " );
     return list;
   }

   public List<String> selectAllSurveys() { return selectAllNames( SURVEY_TABLE ); }
       
   public List<String> selectAllCalibs() { return selectAllNames( CALIB_TABLE ); }

   public List<String> selectDeviceCalibs( String device ) 
   {
     List<String> ret = new ArrayList<String>();
     Cursor cursor = myDB.query( CALIB_TABLE,
                                new String[] { "name" }, // columns
                                "device=?",
                                new String[] { device },
                                null,  // groupBy
                                null,  // having
                                null );
     if (cursor != null ) {
       if ( cursor.moveToFirst() ) {
         do {
           ret.add( new String(cursor.getString(0)) );
         } while (cursor.moveToNext());
       }
       if ( !cursor.isClosed()) cursor.close();
     }
     return ret;
   }

   public List<CalibInfo> selectDeviceCalibsInfo( String device ) 
   {
     List<CalibInfo> ret = new ArrayList<CalibInfo>();
     Cursor cursor = myDB.query( CALIB_TABLE,
                                new String[] { "id", "name", "day", "comment", "algo" }, // columns
                                "device=?",
                                new String[] { device },
                                null,  // groupBy
                                null,  // having
                                null );
     if (cursor != null ) {
       if ( cursor.moveToFirst() ) {
         do {
           ret.add( new CalibInfo(
             cursor.getLong(0),
             cursor.getString(1),
             cursor.getString(2),
             device,
             cursor.getString(3),
             (int)cursor.getLong(4) ) );
         } while (cursor.moveToNext());
       }
       if ( !cursor.isClosed()) cursor.close();
     }
     return ret;
   }

   // ----------------------------------------------------------------------
   // CONFIG DATA

   public String getValue( String key )
   {
     if ( myDB == null ) {
       TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "DataHelper::getValue null DB");
       return null;
     }
     if ( key == null || key.length() == 0 ) {
       TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "DataHelper::getValue null key");
       return null;
     }
     String value = null;
     Cursor cursor = myDB.query( CONFIG_TABLE,
                                   new String[] { "value" }, // columns
                                   "key = ?", new String[] { key },
                                   null, null, null );
     if ( cursor != null ) {
       if (cursor.moveToFirst()) {
         value = cursor.getString( 0 );
       }
       if ( ! cursor.isClosed()) cursor.close();
     }
     return value;
   }

   public void setValue( String key, String value )
   {
     if ( myDB == null ) {
       TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "DataHelper::setValue null DB");
       return;
     }
     if ( key == null || key.length() == 0 ) {
       TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "DataHelper::setValue null key");
       return;
     }
     if ( value == null || value.length() == 0 ) {
       TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "DataHelper::setValue null value");
       return;
     }

     Cursor cursor = myDB.query( CONFIG_TABLE,
                                new String[] { "value" }, // columns
                                "key = ?", new String[] { key },
                                null, null, null );
     if ( cursor != null ) {
       if (cursor.moveToFirst()) {
         updateConfig.bindString( 1, value );
         updateConfig.bindString( 2, key );
         updateConfig.execute();
       } else {
         ContentValues cv = new ContentValues();
         cv.put( "key",     key );
         cv.put( "value",   value );
         myDB.insert( CONFIG_TABLE, null, cv );
       }
       if ( ! cursor.isClosed()) cursor.close();
     }
   }

   // ----------------------------------------------------------------------
   // symbols

   void setSymbolEnabled( String name, boolean enabled ) { setValue( name, enabled? "1" : "0" ); }

   boolean isSymbolEnabled( String name )
   { 
     String enabled = getValue( name );
     if ( enabled != null ) {
       return enabled.equals("1");
     }
     // if ( myDB == null ) return true;
     ContentValues cv = new ContentValues();
     cv.put( "key",     name );
     cv.put( "value",   "1" );     // symbols are enabled by default
     myDB.insert( CONFIG_TABLE, null, cv );
     return true;
   }


     
   // ----------------------------------------------------------------------
   /* Set the current survey/calib name.
    * If the survey/calib name does not exists a new record is inserted in the table
    */

   private String getNameFromId( String table, long id )
   {
     String ret = null;
     // if ( myDB == null ) return null;
     Cursor cursor = myDB.query( table, new String[] { "name" },
                          "id=?", new String[] { Long.toString(id) },
                          null, null, null );
     if (cursor != null ) {
       if (cursor.moveToFirst() ) {
         ret = cursor.getString(0);
       }
       if ( ! cursor.isClosed()) cursor.close();
     }
     return ret;
   }

   private long getIdFromName( String table, String name ) 
   {
     long id = -1;
     // if ( myDB == null ) { return -2; }
     Cursor cursor = myDB.query( table, new String[] { "id" },
                                 "name = ?", new String[] { name },
                                 null, null, null );
     if (cursor != null ) {
       if (cursor.moveToFirst() ) {
         id = cursor.getLong(0);
       }
       if ( ! cursor.isClosed()) cursor.close();
     }
     return id;
   }

   private long setName( String table, String name ) 
   {
     long id = -1;
     // if ( myDB == null ) { return 0; }
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "setName >" + name + "< table " + table );
     Cursor cursor = myDB.query( table, new String[] { "id" },
                                 "name = ?", new String[] { name },
                                 null, null, null );
     if (cursor.moveToFirst() ) {
       id = cursor.getLong(0);
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     } else {
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
       // SELECT max(id) FROM table
       cursor = myDB.query( table, new String[] { "max(id)" },
                            null, null, null, null, null );
       if (cursor.moveToFirst() ) {
         id = 1 + cursor.getLong(0);
       } else {
         id = 1;
       }
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
       // INSERT INTO table VALUES( id, name, "", "" )
       ContentValues cv = new ContentValues();
       cv.put( "id",      id );
       cv.put( "name",    name );
       cv.put( "day",     "" );
       cv.put( "comment", "" );
       myDB.insert( table, null, cv );
     }
     return id;
   }

   // FIXME 'xx' is the prefix-name for sections
   final String prefix = "xx";
   final int prefix_length = 2; // prefix.length();

   public String getNextSectionId( long sid )
   {
     int max = 0; 
     // if ( myDB == null ) return "xx0";
     // Log.v( TopoDroidApp.TAG, "getNextSectionId sid " + sid + " prefix " + prefix );
     Cursor cursor = myDB.query( PLOT_TABLE, 
                 new String[] { "id", "type", "name" },
                 "surveyId=?",
                 new String[] { Long.toString(sid) },
                 null, null, null );
     if (cursor != null ) {
       if (cursor.moveToFirst() ) {
         do {
           int type   = cursor.getInt(1);
           String name = cursor.getString(2);
           // Log.v( TopoDroidApp.TAG, "plot name " + name + " prefix " + prefix );
           if ( name.startsWith( prefix ) /* && ( type == PlotInfo.PLOT_PHOTO || type == PlotInfo.PLOT_SECTION ) */ ) {
             try {
               int k = Integer.parseInt( name.substring( prefix_length ) );
               if ( k >= max ) max = k+1;
             } catch ( NumberFormatException e ) {
               TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "getNextSectionId parse Int error: survey ID " + sid );
             }
           }
         } while (cursor.moveToNext());
       }
       if (!cursor.isClosed()) cursor.close();
     }
     return prefix + Integer.toString(max);
   }
 
   public PlotInfo getPlotInfo( long sid, String name )
   {
     PlotInfo plot = null;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( PLOT_TABLE, 
                 new String[] { "id", "type", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino" },
                 "surveyId=? and name=?", 
                 new String[] { Long.toString(sid), name },
                 null, null, null );
       if (cursor != null ) {
         if (cursor.moveToFirst() ) {
           plot = new PlotInfo();
           plot.surveyId = sid;
           plot.id    = cursor.getLong(0);
           plot.type  = cursor.getInt(1);
           plot.name  = name;
           plot.start = cursor.getString(2);
           plot.view  = cursor.getString(3);
           plot.xoffset = (float)( cursor.getDouble(4) );
           plot.yoffset = (float)( cursor.getDouble(5) );
           plot.zoom    = (float)( cursor.getDouble(6) );
           plot.azimuth = (float)( cursor.getDouble(7) );
           plot.clino   = (float)( cursor.getDouble(8) );
           // Log.v( TopoDroidApp.TAG, "plot " + plot.name + " azimuth " + plot.azimuth );
         }
         if (!cursor.isClosed()) cursor.close();
       }
     }
     return plot;
   }
 
   // FIXME_SKETCH_3D
   public Sketch3dInfo getSketch3dInfo( long sid, String name )
   {
     Sketch3dInfo sketch = null;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( SKETCH_TABLE, 
                 new String[] { "id", "start", "st1", "st2", "xoffsettop", "yoffsettop", "zoomtop", "xoffsetside", "yoffsetside", "zoomside", "xoffset3d", "yoffset3d", "zoom3d", "east", "south", "vert", "azimuth", "clino" },
                 "surveyId=? and name=?", 
                 new String[] { Long.toString(sid), name },
                 null, null, null );
       if (cursor.moveToFirst() ) {
         sketch = new Sketch3dInfo();
         sketch.surveyId = sid;
         sketch.id    = cursor.getLong(0);
         sketch.name  = name;
         sketch.start = cursor.getString(1);
         sketch.st1   = cursor.getString(2);
         sketch.st2   = cursor.getString(3);
         sketch.xoffset_top = (float)( cursor.getDouble(4) );
         sketch.yoffset_top = (float)( cursor.getDouble(5) );
         sketch.zoom_top    = (float)( cursor.getDouble(6) );
         sketch.xoffset_side = (float)( cursor.getDouble(7) );
         sketch.yoffset_side = (float)( cursor.getDouble(8) );
         sketch.zoom_side    = (float)( cursor.getDouble(9) );
         sketch.xoffset_3d = (float)( cursor.getDouble(10) );
         sketch.yoffset_3d = (float)( cursor.getDouble(11) );
         sketch.zoom_3d    = (float)( cursor.getDouble(12) );
         sketch.east    = (float)( cursor.getDouble(13) );
         sketch.south   = (float)( cursor.getDouble(14) );
         sketch.vert    = (float)( cursor.getDouble(15) );
         sketch.azimuth = (float)( cursor.getDouble(16) );
         sketch.clino   = (float)( cursor.getDouble(17) );
       }
       if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     }
     return sketch;
   }
   // END_SKETCH_3D

   long getPlotId( long sid, String name )
   {
     long ret = -1;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( PLOT_TABLE, new String[] { "id" },
                            "surveyId=? and name=?", 
                            new String[] { Long.toString(sid), name },
                            null, null, null );
       if ( cursor != null ) {
         if (cursor.moveToFirst() ) {
           ret = cursor.getLong(0);
         }
         if ( !cursor.isClosed()) cursor.close();
       }
     }
     return ret;
   }

   // FIXME_SKETCH_3D
   private long getSketch3dId( long sid, String name )
   {
     long ret = -1;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( SKETCH_TABLE, new String[] { "id" },
                            "surveyId=? and name=?",
                            new String[] { Long.toString(sid), name },
                            null, null, null );
       if ( cursor != null ) {
         if (cursor.moveToFirst() ) {
           ret = cursor.getLong(0);
         }
         if ( !cursor.isClosed()) cursor.close();
       }
     }
     return ret;
   }
   // END_SKETCH_3D

   // public String getPlotFieldAsString( long sid, long pid, String field )
   // {
   //   String ret = null;
   //   // if ( myDB == null ) return ret;
   //   Cursor cursor = myDB.query( PLOT_TABLE, new String[] { field },
   //                        "surveyId=? and id=?", 
   //                        new String[] { Long.toString(sid), Long.toString(pid) },
   //                        null, null, null );
   //   if ( cursor != null ) {
   //     if (cursor.moveToFirst() ) {
   //       if ( field.equals("type") ) {
   //         ret = Long.toString( cursor.getLong(0) );
   //       } else {
   //         ret = cursor.getString(0);
   //       }
   //     }
   //     if ( !cursor.isClosed()) cursor.close();
   //   }
   //   return ret;
   // }

   /**
    * @param sid       survey id
    * @param id        photo id (or -1)
    * @param shotid    shot id
    * @param title     photo title
    * @param comment
    */
   public long insertPhoto( long sid, long id, long shotid, String title, String date, String comment )
   {
     // if ( myDB == null ) return -1L;
     if ( id == -1L ) id = maxId( PHOTO_TABLE, sid );
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "id",        id );
     cv.put( "shotId",    shotid );
     cv.put( "status",    TopoDroidApp.STATUS_NORMAL );
     cv.put( "title",     title );
     cv.put( "date",      date );
     cv.put( "comment",   (comment == null)? "" : comment );
     myDB.insert( PHOTO_TABLE, null, cv );
     return id;
   }

   public long nextPhotoId( long sid )
   {
     return maxId( PHOTO_TABLE, sid );
   }

   public boolean updatePhoto( long sid, long id, String comment )
   {
     updatePhotoStmt.bindString( 1, comment );
     updatePhotoStmt.bindLong( 2, sid );
     updatePhotoStmt.bindLong( 3, id );
     updatePhotoStmt.execute();
     return true;
   }

   public void deletePhoto( long sid, long id )
   {
     // if ( myDB == null ) return;
     deletePhotoStmt.bindLong( 1, sid );
     deletePhotoStmt.bindLong( 2, id );
     deletePhotoStmt.execute();
   }


   /**
    * @param sid       survey id
    * @param id        photo id (or -1)
    * @param shotid    shot id
    * @param title     sensor title
    * @param date      sensor date
    * @param comment
    * @param type      sensor type
    * @param value     sensor value
    */
   public long insertSensor( long sid, long id, long shotid, String title, String date, String comment, 
                             String type, String value )
   {
     if ( id == -1L ) id = maxId( SENSOR_TABLE, sid );
     // if ( myDB == null ) return -1L;
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "id",        id );
     cv.put( "shotId",    shotid );
     cv.put( "status",    TopoDroidApp.STATUS_NORMAL );
     cv.put( "title",     title );
     cv.put( "date",      date );
     cv.put( "comment",   (comment == null)? "" : comment );
     cv.put( "type",      type );
     cv.put( "value",     value );
     myDB.insert( SENSOR_TABLE, null, cv );
     return id;
   }

   public long nextSensorId( long sid )
   {
     return maxId( SENSOR_TABLE, sid );
   }

   public void deleteSensor( long sid, long id )
   {
     // if ( myDB == null ) return;
     deleteSensorStmt.bindLong( 1, sid );
     deleteSensorStmt.bindLong( 2, id );
     deleteSensorStmt.execute();
   }

   public boolean updateSensor( long sid, long id, String comment )
   {
     updateSensorStmt.bindString( 1, comment );
     updateSensorStmt.bindLong( 2, sid );
     updateSensorStmt.bindLong( 3, id );
     updateSensorStmt.execute();
     return true;
   }
   
   /** N.B. only one location per station
    *       Before inserting a location drop existing deleted fixeds for the station
    * N.B. this must be called with id == -1L ( currently called only by SurveyActivity )
    */
   public long insertFixed( long sid, long id, String station, double lng, double lat, double alt, double asl, String comment, long status )
   {
     if ( id != -1L ) return id;
     // if ( myDB == null ) return -1L;
     long fid = getFixedId( sid, station );
     if ( fid != -1L ) return fid;     // check non-deleted fixeds
     dropDeletedFixed( sid, station ); // drop deleted fixed if any

     id = maxId( FIXED_TABLE, sid );
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "insertFixed id " + id );
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "id",        id );
     cv.put( "station",   station );
     cv.put( "longitude", lng );
     cv.put( "latitude",  lat );
     cv.put( "altitude",  alt );
     cv.put( "altimetric", asl );
     cv.put( "comment",   (comment == null)? "" : comment );
     cv.put( "status",    status );
     myDB.insert( FIXED_TABLE, null, cv );
     return id;
   }

   public long insertPlot( long sid, long id, String name, long type, long status, String start, String view,
                           double xoffset, double yoffset, double zoom, double azimuth, double clino, boolean forward )
   {
     // Log.v( TopoDroidApp.TAG, "insertPlot " + name + " start " + start + " azimuth " + azimuth );
     // if ( myDB == null ) return -1L;
     long ret = getPlotId( sid, name );
     if ( ret >= 0 ) return -1;
     if ( id == -1L ) id = maxId( PLOT_TABLE, sid );
     ContentValues cv = new ContentValues();
     cv.put( "surveyId", sid );
     cv.put( "id",       id );
     cv.put( "name",     name );
     cv.put( "type",     type );
     cv.put( "status",   status );
     cv.put( "start",    start );
     cv.put( "view",     (view == null)? "" : view );
     cv.put( "xoffset",  xoffset );
     cv.put( "yoffset",  yoffset );
     cv.put( "zoom",     zoom );
     cv.put( "azimuth",  azimuth );
     cv.put( "clino",    clino );
     myDB.insert( PLOT_TABLE, null, cv );
     if ( forward ) {
       if ( view == null ) view = "";
       // synchronized( mListeners )
       for ( DataListener listener : mListeners ) {
         listener.onInsertPlot( sid, id, name, type, status, start, view, xoffset, yoffset, zoom, azimuth, clino );
       }
     }
     return id;
   }

   // FIXME_SKETCH_3D
   public long insertSketch3d( long sid, long id, String name, long status, String start, String st1, String st2,
                           double xoffsettop, double yoffsettop, double zoomtop,
                           double xoffsetside, double yoffsetside, double zoomside,
                           double xoffset3d, double yoffset3d, double zoom3d,
                           double x, double y, double z, double azimuth, double clino )
   {
     // if ( myDB == null ) return -1L;
     long ret = getSketch3dId( sid, name );
     if ( ret >= 0 ) return -1;
     if ( id == -1L ) id = maxId( SKETCH_TABLE, sid );
     // Log.v( TopoDroidApp.TAG, "Survey ID " + sid + " Sketch ID " + id );

     ContentValues cv = new ContentValues();
     cv.put( "surveyId", sid );
     cv.put( "id",       id );
     cv.put( "name",     name );
     cv.put( "status",   status );
     cv.put( "start",    start );
     cv.put( "st1",      st1 );
     cv.put( "st2",      st2 );
     cv.put( "xoffsettop",  xoffsettop );
     cv.put( "yoffsettop",  yoffsettop );
     cv.put( "zoomtop",     zoomtop );
     cv.put( "xoffsetside",  xoffsetside );
     cv.put( "yoffsetside",  yoffsetside );
     cv.put( "zoomside",     zoomside );
     cv.put( "xoffset3d",  xoffset3d );
     cv.put( "yoffset3d",  yoffset3d );
     cv.put( "zoom3d",     zoom3d );
     cv.put( "east",     x );
     cv.put( "south",    y );
     cv.put( "vert",     z );
     cv.put( "azimuth",  azimuth );
     cv.put( "clino",    clino );
     myDB.insert( SKETCH_TABLE, null, cv );
     return id;
   }
   // END_SKETCH_3D

   private long maxId( String table, long sid )
   {
     long id = 1;
     // if ( myDB == null ) return 1L;
     Cursor cursor = myDB.query( table, new String[] { "max(id)" },
                          "surveyId=?", 
                          new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor != null ) {
       if (cursor.moveToFirst() ) {
         id = 1 + cursor.getLong(0);
       }
       if (!cursor.isClosed()) cursor.close();
     }
     return id;
   }

  public long getLastShotId( long sid )
  {
    return maxId( SHOT_TABLE, sid );
  }

  public ArrayList< Device > getDevices( ) 
  {
    ArrayList<Device> ret = new ArrayList<Device>();
    // if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( DEVICE_TABLE, new String[] { "address", "model", "head", "tail", "name" }, 
                                null, null, null, null, null );
    if (cursor != null ) {
      if ( cursor.moveToFirst() ) {
        do {
          ret.add( new Device( cursor.getString(0), 
                               cursor.getString(1),
                               (int)cursor.getLong(2),
                               (int)cursor.getLong(3),
                               cursor.getString(4)
                  ) );
        } while (cursor.moveToNext());
      }
      if ( !cursor.isClosed()) cursor.close();
    }
    return ret;
  }

  public Device getDevice( String addr )
  {
    Device ret = null;
    // if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( DEVICE_TABLE, new String[] { "address", "model", "head", "tail", "name" }, 
                                "address=?", new String[] { addr }, null, null, null );
    if (cursor != null ) {
      if ( cursor.moveToFirst() ) {
        ret = new Device( cursor.getString(0), 
                          cursor.getString(1),
                          (int)cursor.getLong(2),
                          (int)cursor.getLong(3),
                          cursor.getString(4)
                        );
      }
      if (!cursor.isClosed()) cursor.close();
    }
    return ret;
  }
    
  public int getDeviceTail( String address )
  { 
    int ret = 0;
    // if ( myDB == null ) return 0;
    Cursor cursor = myDB.query( DEVICE_TABLE, new String[] { "tail" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
    if (cursor != null ) {
      if (cursor.moveToFirst() ) {
        ret = (int)( cursor.getLong(0) );
      }
      if (!cursor.isClosed()) cursor.close();
    }
    return ret;
  }

  public boolean getDeviceHeadTail( String address, int[] head_tail )
  {
    boolean ret = false;
    // if ( myDB == null ) return false;
    Cursor cursor = myDB.query( DEVICE_TABLE, new String[] { "head", "tail" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
    if (cursor != null ) {
      if (cursor.moveToFirst() ) {
        head_tail[0] = (int)( cursor.getLong(0) );
        head_tail[1] = (int)( cursor.getLong(1) );
        ret = true;
      }
      if (!cursor.isClosed()) cursor.close();
    }
    return ret;
  }

  boolean insertDevice( String address, String model, String name )
  {
    boolean ret = true;
    // if ( myDB == null ) return false;
    Cursor cursor = myDB.query( DEVICE_TABLE, new String[] { "model" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
    if ( cursor != null ) {
      if (cursor.moveToFirst() ) {
        // TODO address already in the database: check model
        ret = false;
      } else {
        ContentValues cv = new ContentValues();
        cv.put( "address", address );
        cv.put( "model",   model );
        cv.put( "head",    0 );
        cv.put( "tail",    0 );
        cv.put( "name",    name );
        myDB.insert( DEVICE_TABLE, null, cv );
      }
      if (!cursor.isClosed()) cursor.close(); 
    }
    return ret;
  }

  private void insertDeviceHeadTail( String address, String model, int[] head_tail, String name )
  {
    // if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    cv.put( "address", address );
    cv.put( "model",   model );
    cv.put( "head",    head_tail[0] );
    cv.put( "tail",    head_tail[1] );
    cv.put( "name",    name );
    myDB.insert( DEVICE_TABLE, null, cv );
  }

  public void updateDeviceModel( String address, String model )
  {
    updateDeviceModelStmt.bindString( 1, model );
    updateDeviceModelStmt.bindString( 2, address );
    updateDeviceModelStmt.execute();
  }

  public boolean updateDeviceHeadTail( String address, int[] head_tail )
  {
    // if ( myDB == null ) return false;
    boolean ret = false;
    Cursor cursor = myDB.query( DEVICE_TABLE, new String[] { "head" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
    if (cursor != null ) {
      if (cursor.moveToFirst() ) {
        // Log.v(TopoDroidApp.TAG, "update Head Tail " + address + " " + head_tail[0] + " " + head_tail[1] );
        long head = head_tail[0];
        long tail = head_tail[1];
        updateDeviceHeadTailStmt.bindLong( 1, head );
        updateDeviceHeadTailStmt.bindLong( 2, tail );
        updateDeviceHeadTailStmt.bindString( 3, address );
        updateDeviceHeadTailStmt.execute();
        ret = true;
      } else {
        // insertDeviceHeadTail( address, "DistoX", head_tail, name ); // FIXME name ?
      }
      if (!cursor.isClosed()) cursor.close();
    }
    return ret;
  }

  /** check if there is already a fixed record for the given station
   * @param id       do not consider record with this fixed ID
   * @param sid      survey ID
   * @param station  station name
   * @return true if found a record, false otherwise
   */  
  public boolean hasFixedStation( long id, long sid, String station )
  {
    boolean ret = false;
    // if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( FIXED_TABLE, 
                            new String[] { "id" },
                            "surveyId=? and station=? and status=0",  // 0 == TopoDroidApp.STATUS_NORMAL
                            new String[] { Long.toString( sid ), station },
                            null, null, null );
    if (cursor != null ) {
      if (cursor.moveToFirst() ) {
        do {
          if ( cursor.getLong(0) != id ) ret = true;
        } while (cursor.moveToNext());
      }
      if (!cursor.isClosed()) cursor.close();
    }
    return ret;
  }
  
  /** get the ID of the fixed for the given station
   * @param sid          survey ID
   * @param station      station
   * @return fixed ID or -1L
   * @note only non-deleted fixed are considered
   */
  private long getFixedId( long sid, String station )
  {
    long ret = -1L;
    // if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( FIXED_TABLE, 
                            new String[] { "id" },
                            "surveyId=? and station=? and status=0",  // 0 == TopoDroidApp.STATUS_NORMAL
                            new String[] { Long.toString( sid ), station },
                            null, null, null );
    if (cursor != null ) {
      if (cursor.moveToFirst() ) {
        // do {
          ret = cursor.getLong(0);
        // } while (cursor.moveToNext());
      }
      if (!cursor.isClosed()) cursor.close();
    }
    return ret;
  }

  /** drop deleted fixed with given station
   */
  private void dropDeletedFixed( long sid, String station )
  {
    dropFixedStmt.bindLong( 1, sid );
    dropFixedStmt.bindString( 2, station );
    dropFixedStmt.execute();
  }
  

  public boolean updateFixedStation( long id, long sid, String station )
  {
    // Log.v("DistoX", "update fixed id " + id + " station " + station );
    boolean ret = false;
    if ( ! hasFixedStation( id, sid, station ) ) {
      dropDeletedFixed( sid, station );

      updateFixedStationStmt.bindString( 1, station );
      updateFixedStationStmt.bindLong( 2, sid );
      updateFixedStationStmt.bindLong( 3, id );
      updateFixedStationStmt.execute();
      ret = true;
    }
    return ret;
  }

   public void updateFixedStatus( long id, long sid, long status )
   {
     updateFixedStatusStmt.bindLong( 1, status );
     updateFixedStatusStmt.bindLong( 2, sid );
     updateFixedStatusStmt.bindLong( 3, id );
     updateFixedStatusStmt.execute();
   }

   public boolean hasSurveyName( String name )  { return hasName( name, SURVEY_TABLE ); }
   public boolean hasCalibName( String name )  { return hasName( name, CALIB_TABLE ); }

   private boolean hasName( String name, String table )
   {
     boolean ret = false;
     // if ( myDB == null ) return ret;
     Cursor cursor = myDB.query( table, new String[] { "id" },
                          "name=?", 
                          new String[] { name },
                          null, null, null );
     if (cursor != null) {
       if (cursor.moveToFirst() ) {
         ret = true;
       }
       if (!cursor.isClosed()) cursor.close();
     }
     return ret;
   }

   // public boolean updateSurveyName( long id, String name )
   // {
   //   // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateSurveyName id " + id + " name \"" + name + "\"" );
   //   updateSurveyNameStmt.bindString( 1, (name != null)? name : "" );
   //   updateSurveyNameStmt.bindLong( 2, id );
   //   updateSurveyNameStmt.execute();
   //   return true;
   // }

   public boolean updateCalibInfo( long id, String date, String device, String comment )
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateCalibInfo id " + id + " day " + date + " comm. " + comment );
     if ( date == null ) return false;
     updateCalibStmt.bindString( 1, date );
     updateCalibStmt.bindString( 2, (device != null)? device : "" );
     updateCalibStmt.bindString( 3, (comment != null)? comment : "" );
     updateCalibStmt.bindLong( 4, id );
     updateCalibStmt.execute();
     return true;
   }

   public boolean updateCalibAlgo( long id, long algo )
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateCalibAlgo id " + id + " algo " + algo );
     updateCalibAlgoStmt.bindLong( 1, algo );
     updateCalibAlgoStmt.bindLong( 2, id );
     updateCalibAlgoStmt.execute();
     return true;
   }

   public boolean updateCalibCoeff( long id, String coeff )
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateCalibCoeff id " + id + " coeff. " + coeff );
     if ( coeff == null ) return false;
     updateCalibCoeffStmt.bindString( 1, coeff );
     updateCalibCoeffStmt.bindLong( 2, id );
     updateCalibCoeffStmt.execute();
     return true;
   }

   public boolean updateCalibError( long id, double error, double max_error, int iterations )
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "updateCalibCoeff id " + id + " coeff. " + coeff );
     updateCalibErrorStmt.bindDouble( 1, error );
     updateCalibErrorStmt.bindDouble( 2, max_error );
     updateCalibErrorStmt.bindLong( 3, iterations );
     updateCalibErrorStmt.bindLong( 4, id );
     updateCalibErrorStmt.execute();
     return true;
   }

   public long setSurvey( String name, boolean forward )
   {
     myNextId = 0;
     // if ( myDB == null ) return 0L;
     long sid = setName( SURVEY_TABLE, name );
     Cursor cursor = myDB.query( SHOT_TABLE, new String[] { "max(id)" },
                          "surveyId=?", new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       myNextId = cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }

     TopoDroidLog.Log( TopoDroidLog.LOG_DB, "setSurvey " + name + " forward " + forward + " listeners " + mListeners.size() );
     if ( forward ) {
       // synchronized( mListeners )
       for ( DataListener listener : mListeners ) {
         listener.onSetSurvey( sid, name );
       }
     }
     return sid;
   }

   public long setCalib( String calib ) 
   {
     myNextCId = 0;
     // if ( myDB == null ) return 0L;
     long cid = setName( CALIB_TABLE, calib );
     Cursor cursor = myDB.query( GM_TABLE, new String[] { "max(id)" },
                          "calibId=?", new String[] { Long.toString(cid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       myNextCId = cursor.getLong(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return cid;
   }

   public String getCalibFromId( long cid ) { return getNameFromId( CALIB_TABLE, cid ); }

   public String getSurveyFromId( long sid ) { return getNameFromId( SURVEY_TABLE, sid ); }



   public String getSurveyDate( long sid ) { return getSurveyFieldAsString( sid, "day" ); }

   public String getSurveyComment( long sid ) { return getSurveyFieldAsString( sid, "comment" ); }

   public String getSurveyTeam( long sid ) { return getSurveyFieldAsString( sid, "team" ); }

   private String getSurveyFieldAsString( long sid, String attr )
   {
     String ret = null;
     // if ( myDB == null ) return ret;
     Cursor cursor = myDB.query( SURVEY_TABLE, new String[] { attr },
                          "id=?", new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       ret = cursor.getString(0);
     }
     if (cursor != null && !cursor.isClosed()) { cursor.close(); }
     return ret;
   }
      
   // ----------------------------------------------------------------------
   // SERIALIZATION of surveys TO FILE
   // the following tables are serialized (besides the survey recond)
   // 15 SHOTS    { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll", "acceleration", "magnetic", "dip",
   //                                             "extend", "flag", "leg", "status", "comment" },
   // 10 PLOTS    { "id", "name", "type", "status", "start", "view", "xoffset", "yoffset", "zoom", "azimuth" },
   //    SKETCHES
   //  6 PHOTOS   { "id", "shotId", "status", "title", "date", "comment" },
   //  8 SENSORS  { "id", "shotId", "status", "title", "date", "comment", "type", "value" },
   //  8 FIXEDS   { "id", "station", "longitude", "latitude", "altitude", "altimetric", "comment", "status" },

   public void dumpToFile( String filename, long sid )
   {
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "dumpToFile " + filename );
     // String where = "surveyId=" + Long.toString(sid);
     // if ( myDB == null ) return;
     try {
       TopoDroidApp.checkPath( filename );
       FileWriter fw = new FileWriter( filename );
       PrintWriter pw = new PrintWriter( fw );
       Cursor cursor = myDB.query( SURVEY_TABLE, 
                            new String[] { "name", "day", "team", "declination", "comment" },
                            "id=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, \"%s\", \"%s\", \"%s\", %.4f, \"%s\" );\n",
                     SURVEY_TABLE,
                     sid,
                     cursor.getString(0),
                     cursor.getString(1),
                     cursor.getString(2),
                     cursor.getDouble(3),     // declination
                     cursor.getString(4) );   // comment
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }

       cursor = myDB.query( PHOTO_TABLE, // SELECT ALL PHOTO RECORD
  			           new String[] { "id", "shotId", "status", "title", "date", "comment" },
                                   "surveyId=?", new String[] { Long.toString(sid) },
                                   null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, %d, %d, \"%s\", \"%s\" );\n",
                     PHOTO_TABLE,
                     sid,
                     cursor.getLong(0),   // id
                     cursor.getLong(1),   // shotid
                     cursor.getLong(2),   // status
                     cursor.getString(3), // title
                     cursor.getString(4), // date
                     cursor.getString(5) );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }
       cursor = myDB.query( PLOT_TABLE, 
                            new String[] { "id", "name", "type", "status", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, \"%s\", %d, %d, \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %.2f );\n",
                     PLOT_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getString(1),
                     cursor.getLong(2),
                     cursor.getLong(3),
                     cursor.getString(4),
                     cursor.getString(5),
                     cursor.getDouble(6),
                     cursor.getDouble(7),
                     cursor.getDouble(8),
                     cursor.getDouble(9),
                     cursor.getDouble(10)
                    );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }
       cursor = myDB.query( SKETCH_TABLE, 
                            new String[] { "id", "name", "status", "start", "st1", "st2", "xoffsettop", "yoffsettop", "zoomtop", "xoffsetside", "yoffsetside", "zoomside", "xoffset3d", "yoffset3d", "zoom3d", "east", "south", "vert", "azimuth", "clino" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, \"%s\", %d, \"%s\", \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f );\n",
                     SKETCH_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getString(1),
                     cursor.getLong(2),
                     cursor.getString(3),
                     cursor.getString(4),
                     cursor.getString(5),
                     cursor.getDouble(6),
                     cursor.getDouble(7),
                     cursor.getDouble(8),
                     cursor.getDouble(9),
                     cursor.getDouble(10),
                     cursor.getDouble(11),
                     cursor.getDouble(12),
                     cursor.getDouble(13),
                     cursor.getDouble(14),
                     cursor.getDouble(15),
                     cursor.getDouble(16),
                     cursor.getDouble(17),
                     cursor.getDouble(18),
                     cursor.getDouble(19)
                    );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }

       cursor = myDB.query( SHOT_TABLE, 
                            new String[] { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll",
                                           "acceleration", "magnetic", "dip",
                                           "extend", "flag", "leg", "status", "comment", "type" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %.2f %.2f %.2f, %d, %d, %d, %d, \"%s\", %d );\n",
                     SHOT_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getString(1),
                     cursor.getString(2),
                     cursor.getDouble(3),
                     cursor.getDouble(4),
                     cursor.getDouble(5),
                     cursor.getDouble(6),
                     cursor.getDouble(7),
                     cursor.getDouble(8),
                     cursor.getDouble(9),
                     cursor.getLong(10),    // extend
                     cursor.getLong(11),    // flag
                     cursor.getLong(12),    // leg
                     cursor.getLong(13),    // status
                     cursor.getString(14),  // comment
                     cursor.getLong(15)     // type
           );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }

       cursor = myDB.query( FIXED_TABLE, 
                            new String[] { "id", "station", "longitude", "latitude", "altitude", "altimetric", "comment", "status" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, \"%s\", %.6f, %.6f, %.2f, %.2f \"%s\", %d );\n",
                     FIXED_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getString(1),
                     cursor.getDouble(2),
                     cursor.getDouble(3),
                     cursor.getDouble(4),
                     cursor.getDouble(5),
                     cursor.getString(6),
                     cursor.getLong(7) );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }

       cursor = myDB.query( STATION_TABLE, 
                            new String[] { "name", "comment" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           // STATION_TABLE does not have field "id"
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, 0, \"%s\", \"%s\" );\n",
                     STATION_TABLE,
                     sid, 
                     cursor.getString(0),
                     cursor.getString(1) );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }

       cursor = myDB.query( SENSOR_TABLE, 
                            new String[] { "id", "shotId", "status", "title", "date", "comment", "type", "value" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.ENGLISH,
                     "INSERT into %s values( %d, %d, %d, %d, \"%s\", \"%s\", \"%s\", \"%s\", \"%s\" );\n",
                     FIXED_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getLong(1),
                     cursor.getLong(2),
                     cursor.getString(3),
                     cursor.getString(4),
                     cursor.getString(5),
                     cursor.getString(6),
                     cursor.getString(7)
                    );
         } while (cursor.moveToNext());
       }
       if (cursor != null && !cursor.isClosed()) {
         cursor.close();
       }

       fw.flush();
       fw.close();
     } catch ( FileNotFoundException e ) {
       // FIXME
     } catch ( IOException e ) {
       // FIXME
     }
   }

   private int pos, len;

   private void skipSpaces( String val )
   {
     while ( pos < len && val.charAt(pos) == ' ' ) ++ pos;
   }

   private void skipCommaAndSpaces( String val )
   {
     if ( pos < len && val.charAt(pos) == ',' ) ++pos;
     while ( pos < len && val.charAt(pos) == ' ' ) ++ pos;
   }
   
   private int nextQuote( String val )
   {
     int next = pos;
     while ( next < len && val.charAt(next) != '"' ) ++next; 
     return next;
   }

   private int nextCommaOrSpace( String val )
   {
     int next = pos;
     while ( next < len && val.charAt(next) != ',' && val.charAt(next) != ' ' ) ++next; 
     return next;
   }

   private String stringValue( String val ) 
   {
     ++pos; // skip '"'
     int next_pos = nextQuote( val );
     String ret = (pos == next_pos )? "" : val.substring(pos, next_pos );
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "stringValue <" + ret + ">" );
     pos = next_pos + 1;
     skipCommaAndSpaces( val );
     return ret;
   }

   private long longValue( String val )
   {
     long ret = -1;
     int next_pos = nextCommaOrSpace( val );
     // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "longValue " + pos + " " + next_pos + " " + len + " <" + val.substring(pos,next_pos) + ">" );
     String toParse = val.substring( pos, next_pos );
     if ( ! toParse.equals("\"null\"") ) {
       try {
         ret = Long.parseLong( val.substring( pos, next_pos ) );
       } catch ( NumberFormatException e ) {
         TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "longValue error: " + val.substring( pos, next_pos ) );
       }
     }
     pos = next_pos;
     skipCommaAndSpaces( val );
     return ret;
   }

   private double doubleValue( String val )
   {
     int next_pos = nextCommaOrSpace( val );
     double ret = 0.0;
     try {
       ret = Double.parseDouble( val.substring(pos, next_pos ) );
       // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "doubleValue " + pos + " " + next_pos + " " + len + " <" + val.substring(pos,next_pos) + ">" );
     } catch ( NumberFormatException e ) {
       TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "doubleValue error: " + val.substring(pos, next_pos) );
     }
     pos = next_pos;
     skipCommaAndSpaces( val );
     return ret;
   }

   /** load survey data from a sql file
    * @param filename  name of the sql file
    */
   long loadFromFile( String filename )
   {
     long sid = -1;
     long id, status, shotid;
     String station, title, date, name, comment;
     String line;
     try {
       FileReader fr = new FileReader( filename );
       BufferedReader br = new BufferedReader( fr );
       // first line is survey
       line = br.readLine();
       // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "loadFromFile: " + line );
       String[] vals = line.split(" ", 4);
       String table = vals[2];
       String v = vals[3];
       pos = v.indexOf( '(' ) + 1;
       len = v.lastIndexOf( ')' );
       skipSpaces( v );
       if ( table.equals(SURVEY_TABLE) ) {
         long skip_sid = longValue( v );
         name        = stringValue( v );
         String day  = stringValue( v );
         String team = stringValue( v );
         double decl = doubleValue( v );
         comment     = stringValue( v );
         sid = setSurvey( name, false );
         updateSurveyInfo( sid, day, team, decl, comment, false );
         while ( (line = br.readLine()) != null ) {
           // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "loadFromFile: " + line );
           vals = line.split(" ", 4);
           table = vals[2];
           v = vals[3];
           pos = v.indexOf( '(' ) + 1;
           len = v.lastIndexOf( ')' );
           skipSpaces( v );
           // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "loafFromFile " + table + " " + v );
           skip_sid = longValue( v );
           id = longValue( v );
           if ( table.equals(PHOTO_TABLE) ) {
             // FIXME PHOTO
             shotid  = longValue( v );
             title   = stringValue( v );
             date    = stringValue( v );
             comment = stringValue( v );
             if ( shotid >= 0 ) {
               insertPhoto( sid, id, shotid, title, date, comment );
               // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "loadFromFile photo " + sid + " " + id + " " + title + " " + name );
             }
           } else if ( table.equals(PLOT_TABLE) ) {
             name         = stringValue( v );
             long type    = longValue( v );
             status       = longValue( v );
             String start = stringValue( v );
             String view  = stringValue( v );
             double xoffset = doubleValue( v );
             double yoffset = doubleValue( v );
             double zoom  = doubleValue( v );
             double azimuth = doubleValue( v );
             double clino = doubleValue( v );
             insertPlot( sid, id, name, type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, false );
             // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "loadFromFile plot " + sid + " " + id + " " + start + " " + name );
   
           // FIXME_SKETCH_3D
           } else if ( table.equals(SKETCH_TABLE) ) {
             name         = stringValue( v );
             status       = longValue( v );
             String start = stringValue( v );
             String st1   = stringValue( v );
             String st2   = stringValue( v );
             double xofft  = doubleValue( v );
             double yofft  = doubleValue( v );
             double zoomt  = doubleValue( v );
             double xoffs  = doubleValue( v );
             double yoffs  = doubleValue( v );
             double zooms  = doubleValue( v );
             double xoff3  = doubleValue( v );
             double yoff3  = doubleValue( v );
             double zoom3  = doubleValue( v );
             double east   = doubleValue( v );
             double south  = doubleValue( v );
             double vert   = doubleValue( v );
             double azimuth= doubleValue( v );
             double clino  = doubleValue( v );
             insertSketch3d( sid, id, name, status, start, st1, st2, xofft, yofft, zoomt, xoffs, yoffs, zooms, xoff3, yoff3, zoom3, east, south, vert, azimuth, clino );
           // END_SKETCH_3D
           } else if ( table.equals(SHOT_TABLE) ) {
             String from = stringValue( v );
             String to   = stringValue( v );
             double d    = doubleValue( v );
             double b    = doubleValue( v );
             double c    = doubleValue( v );
             double r    = doubleValue( v );
             double acc  = doubleValue( v );
             double mag  = doubleValue( v );
             double dip  = doubleValue( v );
             long extend = longValue( v );
             long flag   = longValue( v );
             long leg    = longValue( v );
             status      = longValue( v );
             comment     = stringValue( v );
             // long type = 0;
             insertShot( sid, id, from, to, d, b, c, r, extend, flag, leg, status, 0, comment, false );
             updateShotAMDR( id, sid, acc, mag, dip, r, false );
             // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "insertShot " + sid + " " + id + " " + from + " " + to );
           } else if ( table.equals(FIXED_TABLE) ) {
             station    = stringValue( v );
             double lng = doubleValue( v );
             double lat = doubleValue( v );
             double alt = doubleValue( v );
             double asl = doubleValue( v );
             comment    = stringValue( v );
             status     = longValue( v );
             insertFixed( sid, id, station, lng, lat, alt, asl, comment, status );
             // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "loadFromFile fixed " + sid + " " + id + " " + station  );
           } else if ( table.equals(STATION_TABLE) ) {
             // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "v <" + v + ">" );
             // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "loadFromFile station " + sid + " " + name + " " + comment  );
             name    = stringValue( v );
             comment = stringValue( v );
             insertStation( sid, name, comment );
           }
         }
       }
       fr.close();
     } catch ( FileNotFoundException e ) {
     } catch ( IOException e ) {
     }

     return sid;
   }

   // ----------------------------------------------------------------------
   void insertStation( long sid, String name, String comment )
   {
     Cursor cursor = myDB.query( STATION_TABLE, 
                            new String[] { "name", "comment" },
                            "surveyId=? and name=?", new String[] { Long.toString( sid ), name },
                            null, null, null );
     if (cursor.moveToFirst()) {
       updateStationCommentStmt.bindString( 1, comment );
       updateStationCommentStmt.bindLong(   2, sid );
       updateStationCommentStmt.bindString( 3, name );
       updateStationCommentStmt.execute();
     } else {
       ContentValues cv = new ContentValues();
       cv.put( "surveyId",  sid );
       cv.put( "name",      name );
       cv.put( "comment",   (comment == null)? "" : comment );
       myDB.insert( STATION_TABLE, null, cv );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
   }

   CurrentStation getStation( long sid, String name )
   {
     CurrentStation cs = null;
     Cursor cursor = myDB.query( STATION_TABLE, 
                            new String[] { "name", "comment" },
                            "surveyId=? and name=?", new String[] { Long.toString( sid ), name },
                            null, null, null );
     if (cursor.moveToFirst()) {
       cs = new CurrentStation( cursor.getString(0), cursor.getString(1) );
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return cs;
   }
       

   ArrayList< CurrentStation > getStations( long sid )
   {
     ArrayList< CurrentStation > ret = new ArrayList< CurrentStation >();

     Cursor cursor = myDB.query( STATION_TABLE, 
                            new String[] { "name", "comment" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
     if (cursor.moveToFirst()) {
       do {
         ret.add( new CurrentStation( cursor.getString(0), cursor.getString(1) ) );
       } while (cursor.moveToNext());
     }
     if (cursor != null && !cursor.isClosed()) {
       cursor.close();
     }
     return ret;
   }

   void deleteStation( long sid, String name )
   {
     deleteStationStmt.bindLong(   1, sid );
     deleteStationStmt.bindString( 2, name );
     deleteStationStmt.execute();
   }

   // ----------------------------------------------------------------------
   // DATABASE TABLES

   private static class DistoXOpenHelper extends SQLiteOpenHelper
   {
      private static final String create_table = "CREATE TABLE IF NOT EXISTS ";

      DistoXOpenHelper(Context context, String database_name ) 
      {
         super(context, database_name, null, DATABASE_VERSION);
         Log.v("DistoX", "DB NAME " + database_name );
         // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "createTables ... " + database_name + " version " + DATABASE_VERSION );
      }

      @Override
      public void onCreate(SQLiteDatabase db) 
      {
        createTables( db );
        // TopoDroidLog.Log( TopoDroidLog.LOG_DB, "DistoXOpenHelper onCreate done db " + db );
      }

      private void createTables( SQLiteDatabase db )
      {
         db.setLockingEnabled( false );
         db.beginTransaction();
         try {
           db.execSQL( 
               create_table + CONFIG_TABLE
             + " ( key TEXT NOT NULL,"
             +   " value TEXT )"
           );

           // db.execSQL( "insert into " + CONFIG_TABLE + " values ( \"sketch\", \"on\" )" );

           // db.execSQL("DROP TABLE IF EXISTS " + SHOT_TABLE);
           // db.execSQL("DROP TABLE IF EXISTS " + SURVEY_TABLE);
           db.execSQL(
               create_table + SURVEY_TABLE 
             + " ( id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
             +   " name TEXT, "
             +   " day TEXT, "
             +   " team TEXT, "
             +   " comment TEXT, "
             +   " declination REAL "
             +   ")"
           );

           db.execSQL(
               create_table + SHOT_TABLE 
             + " ( surveyId INTEGER, "
             +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
             +   " fStation TEXT, "
             +   " tStation TEXT, "
             +   " distance REAL, "
             +   " bearing REAL, "
             +   " clino REAL, "
             +   " roll REAL, "
             +   " acceleration REAL, "
             +   " magnetic REAL, "
             +   " dip REAL, "
             +   " extend INTEGER, "
             +   " flag INTEGER, "
             +   " leg INTEGER, "
             +   " status INTEGER, "
             +   " comment TEXT, "
             +   " type INTEGER "
             // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
             // +   " ON DELETE CASCADE "
             +   ")"
           );

           db.execSQL(
               create_table + FIXED_TABLE
             + " ( surveyId INTEGER, "
             +   " id INTEGER, "   //  PRIMARY KEY AUTOINCREMENT, "
             +   " station TEXT, "
             +   " longitude REAL, "
             +   " latitude REAL, "
             +   " altitude REAL, "    // WGS84 altitude
             +   " altimetric REAL, "  // altimetric altitude (if any)
             +   " comment TEXT, "
             +   " status INTEGER"
             // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
             // +   " ON DELETE CASCADE "
             +   ")"
           );

           db.execSQL(
               create_table + STATION_TABLE 
             + " ( surveyId INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
             +   " name TEXT, "
             +   " comment TEXT "
             +   ")"
           );
            
           db.execSQL(
               create_table + CALIB_TABLE
             + " ( id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
             +   " name TEXT, "
             +   " day TEXT, "
             +   " device TEXT, "
             +   " comment TEXT, "
             +   " error REAL default 0, "
             +   " max_error REAL default 0, "
             +   " iterations INTEGER default 0, "
             +   " coeff BLOB, "
             +   " algo INTEGER default 0 "
             +   ")"
           );

           db.execSQL(
               create_table + GM_TABLE 
             + " ( calibId INTEGER, "
             +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
             +   " gx INTEGER, "
             +   " gy INTEGER, "
             +   " gz INTEGER, "
             +   " mx INTEGER, "
             +   " my INTEGER, "
             +   " mz INTEGER, "
             +   " grp INTEGER, "
             +   " error REAL default 0, "
             +   " status INTEGER default 0"
             // +   " calibId REFERENCES " + CALIB_TABLE + "(id)"
             // +   " ON DELETE CASCADE "
             +   ")"
           );

           db.execSQL(
               create_table + PLOT_TABLE 
             + " ( surveyId INTEGER, "
             +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
             +   " name TEXT, "
             +   " type INTEGER, "
             +   " status INTEGER, "
             +   " start TEXT, "
             +   " view TEXT, "
             +   " xoffset REAL, "
             +   " yoffset REAL, "
             +   " zoom REAL, "
             +   " azimuth REAL, "
             +   " clino REAL "
             // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
             // +   " ON DELETE CASCADE "
             +   ")"
           );

           db.execSQL(
               create_table + SKETCH_TABLE
             + " ( surveyId INTEGER, "
             +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
             +   " name TEXT, "
             +   " status INTEGER, "
             +   " start TEXT, "
             +   " st1 TEXT, "
             +   " st2 TEXT, "
             +   " xoffsettop REAL, "
             +   " yoffsettop REAL, "
             +   " zoomtop REAL, "
             +   " xoffsetside REAL, "
             +   " yoffsetside REAL, "
             +   " zoomside REAL, "
             +   " xoffset3d REAL, "
             +   " yoffset3d REAL, "
             +   " zoom3d REAL, "
             +   " east REAL, "
             +   " south REAL, "
             +   " vert REAL, "
             +   " azimuth REAL, "
             +   " clino REAL "
             // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
             // +   " ON DELETE CASCADE "
             +   ")"
           );

           db.execSQL(
               create_table + PHOTO_TABLE
             + " ( surveyId INTEGER, "
             +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
             +   " shotId INTEGER, "
             +   " status INTEGER default 0, "
             +   " title TEXT, "
             +   " date TEXT, "
             +   " comment TEXT "
             // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
             // +   " ON DELETE CASCADE "
             +   ")"
           );

           db.execSQL(
               create_table + SENSOR_TABLE
             + " ( surveyId INTEGER, "
             +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
             +   " shotId INTEGER, "
             +   " status INTEGER default 0, "
             +   " title TEXT, "
             +   " date TEXT, "
             +   " comment TEXT, "
             +   " type TEXT, "
             +   " value TEXT "
             // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
             // +   " ON DELETE CASCADE "
             +   ")"
           );

           db.execSQL(
               create_table + DEVICE_TABLE
             + " ( address TEXT, "
             +   " model TEXT, "
             +   " head INTEGER, "
             +   " tail INTEGER, "
             +   " name TEXT "
             +   ")"
           );

           // db.execSQL(
           //     " CREATE TRIGGER fk_insert_shot BEFORE "
           //   + " INSERT on " + SHOT_TABLE 
           //   + " FOR EACH ROW BEGIN "
           //   +   " SELECT RAISE "
           //   +   " (ROLLBACK, 'insert on \"" + SHOT_TABLE + "\" violates foreing key constraint')"
           //   +   " WHERE ( SELECT id FROM " + SURVEY_TABLE + " WHERE id = NEW.surveyId ) IS NULL; "
           //   + " END;"
           // );
           // db.execSQL(
           //     "CREATE TRIGGER fk_delete_survey BEFORE DELETE ON " + SURVEY_TABLE
           //   + " FOR EACH ROW BEGIN "
           //   +   " SELECT RAISE "
           //   +   " (ROLLBACK, 'delete from \"" + SURVEY_TABLE + "\" violates constraint')"
           //   +   " WHERE ( id IS IN ( SELECT DISTINCT surveyId FROM " + SHOT_TABLE + " ) );"
           //   + " END;"
           // );

           db.setTransactionSuccessful();
         } catch ( SQLException e ) {
           TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "createTables exception " + e.toString() );
         } finally {
           db.endTransaction();
           db.setLockingEnabled( true );
         }
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {  
         // FIXME this is called at each start when the database file exists
         TopoDroidLog.Log( TopoDroidLog.LOG_DB, "onUpgrade old " + oldVersion + " new " + newVersion );
         switch ( oldVersion ) {
           case 14: 
             db.execSQL( "ALTER TABLE surveys ADD COLUMN declination REAL default 0" );
             db.execSQL( "ALTER TABLE gms ADD COLUMN status INTEGER default 0" );
           case 15:
             db.execSQL( "ALTER TABLE devices ADD COLUMN name TEXT" );
           case 16:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN coeff BLOB" );
           case 17:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN error REAL default 0" );
             db.execSQL( "ALTER TABLE calibs ADD COLUMN max_error REAL default 0" );
             db.execSQL( "ALTER TABLE calibs ADD COLUMN iterations INTEGER default 0" );
           case 18:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN algo INTEGER default 1" );
           case 19:
             db.execSQL( "CREATE TABLE stations ( surveyId INTEGER, name TEXT, comment TEXT )" );
           case 20:
             db.execSQL( "UPDATE plots SET type=5 WHERE type=3" );
             db.execSQL( "ALTER TABLE plots ADD COLUMN clino REAL default 0" );
           case 21:
             db.execSQL( "ALTER TABLE shots ADD COLUMN type INTEGER default 0" );
           case 22:
             /* current version */
           default:
             break;
         }
      }
   }
}
