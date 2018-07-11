/* @file DataHelper.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SQLite database manager
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

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
import android.database.sqlite.SQLiteDiskIOException;

// import android.widget.Toast;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Locale;
import java.util.HashMap;

@SuppressWarnings("SyntaxError")
class DataHelper extends DataSetObservable
{

  static final String DB_VERSION = "35";
  static final int DATABASE_VERSION = 35;
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
  private static final String AUDIO_TABLE  = "audios";

  private final static String WHERE_ID          = "id=?";
  private final static String WHERE_SID         = "surveyId=?";
  private final static String WHERE_SID_ID      = "surveyId=? AND id=?";
  private final static String WHERE_SID_ID_MORE = "surveyId=? AND id>=?";
  private final static String WHERE_SID_NAME    = "surveyId=? AND name=?";
  private final static String WHERE_SID_STATUS  = "surveyId=? AND status=?";
  private final static String WHERE_SID_STATUS_LEG  = "surveyId=? AND status=? AND fStation > \"\" AND tStation > \"\"";
  private final static String WHERE_SID_SHOTID  = "surveyId=? AND shotId=?";
  private final static String WHERE_SID_START   = "surveyId=? AND start=?";

  private SQLiteDatabase myDB = null;
  private long           myNextId;   // id of next shot
  private long           myNextCId;  // id of next calib-data

  // private SQLiteStatement updateConfig = null;

  private SQLiteStatement updateAudioStmt = null;
  private SQLiteStatement updateShotStmt = null;
  private SQLiteStatement updateShotStmtFull = null;
  // private SQLiteStatement updateShotDBCStmt = null;
  // private SQLiteStatement clearStationsStmt = null;
  // private SQLiteStatement updateShotLegStmt = null;
  private SQLiteStatement updateStationCommentStmt = null;
  // private SQLiteStatement deleteStationStmt = null;
  // private SQLiteStatement updateShotNameStmt = null;
  // private SQLiteStatement updateShotExtendStmt = null;
  // private SQLiteStatement updateShotFlagStmt = null;
  // private SQLiteStatement updateShotLegFlagStmt = null;
  // private SQLiteStatement updateShotCommentStmt = null;
  private SQLiteStatement resetShotColorStmt = null;
  private SQLiteStatement updateShotColorStmt = null;
  private SQLiteStatement updateShotAMDRStmt = null;
  // private SQLiteStatement shiftShotsIdStmt = null;

  // private SQLiteStatement updatePlotStmt     = null;
  private SQLiteStatement updatePlotViewStmt = null;
  private SQLiteStatement updatePlotHideStmt = null;
  private SQLiteStatement updatePlotNameStmt = null;
  private SQLiteStatement updatePlotOrientationStmt = null;
  private SQLiteStatement updatePlotAzimuthClinoStmt = null;
  private SQLiteStatement updatePlotNickStmt = null;

// FIXME_SKETCH_3D
  private SQLiteStatement updateSketchStmt = null;
  // private SQLiteStatement deleteSketchStmt = null;
// END_SKETCH_3D
  // private SQLiteStatement updatePhotoStmt = null;
  // private SQLiteStatement updateSensorStmt = null;

  private SQLiteStatement transferShotStmt = null;
  // private SQLiteStatement transferSensorStmt = null;
  // private SQLiteStatement transferPhotoStmt = null;
  // private SQLiteStatement transferFixedStmt = null;
  private SQLiteStatement transferPlotStmt = null;
  private SQLiteStatement transferSketchStmt = null;
  // private SQLiteStatement transferStationStmt = null;

  // private SQLiteStatement updateFixedStationStmt = null;
  // private SQLiteStatement updateFixedStatusStmt = null;
  // private SQLiteStatement updateFixedCommentStmt = null;
  // private SQLiteStatement updateFixedAltStmt = null;
  // private SQLiteStatement updateFixedDataStmt = null;
  // private SQLiteStatement updateFixedCSStmt = null;

//these are real database "delete"
  // private SQLiteStatement deletePhotoStmt = null;
  // private SQLiteStatement deleteSensorStmt = null;
  // private SQLiteStatement dropPlotStmt = null;
  // private SQLiteStatement dropFixedStmt = null;

  private String[] mShotFields; // select shot fields

  static final private String[] mPlotFieldsFull =
    { "id", "name", "type", "status", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino", "hide", "nick" };
  static final private String[] mPlotFields =
    { "id", "name", "type", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino", "hide", "nick", "orientation" };

  private String[] mSketchFields; // select sketch fields

  private DataListenerSet mListeners;

  // N.B. "source" comes after "status" although it is after "cs_altitude" in the table
  private static final String[] mFixedFields = {
    "id", "station", "longitude", "latitude", "altitude", "altimetric", "comment", "status", "source",
    "cs_name", "cs_longitude", "cs_latitude", "cs_altitude", "cs_decimals"
  };

  // ----------------------------------------------------------------------
  // DATABASE

  private final Context mContext;
  private final TopoDroidApp mApp;

  public SQLiteDatabase getDb() { return myDB; }


  public DataHelper( Context context, TopoDroidApp app, DataListenerSet listeners )
  {
    mContext = context;
    mApp     = app;
    mShotFields = new String[] { 
         "id", "fStation", "tStation", "distance", "bearing",
         "clino", "acceleration", "magnetic", "dip", "extend",
         "flag", "leg", "comment", "type", "millis", "color"
    };
    mSketchFields =
    new String[] {
         "id", "name", "start", "st1", "st2",
         "xoffsettop", "yoffsettop", "zoomtop",
         "xoffsetside", "yoffsetside", "zoomside",
         "xoffset3d", "yoffset3d", "zoom3d",
         "east", "south", "vert", "azimuth", "clino" 
    };
    mListeners = listeners;
    openDatabase();
  }

  void closeDatabase()
  {
    if ( myDB == null ) return;
    myDB.close();
    myDB = null;
  }

  void openDatabase()
  {
    String database_name = TDPath.getDatabase();
    DistoXOpenHelper openHelper = new DistoXOpenHelper( mContext, database_name );

    try {
        myDB = openHelper.getWritableDatabase();
        if ( myDB == null ) {
          TDLog.Error( "failed get writable database" );
          return;
        }

        while ( myDB.isDbLockedByOtherThreads() ) {
          TopoDroidUtil.slowDown( 200 );
        }

        // updateConfig = myDB.compileStatement( "UPDATE configs SET value=? WHERE key=?" );

     } catch ( SQLiteException e ) {
       myDB = null;
       logError( "DataHelper cstr failed to get DB", e );
     }
   }

   private void fillBlock( long sid, DBlock block, Cursor cursor )
   {
     long leg = cursor.getLong(11);
     block.setId( cursor.getLong(0), sid );
     block.setBlockName( cursor.getString(1), cursor.getString(2), (leg == LegType.BACK) );  // from - to
     block.mLength       = (float)( cursor.getDouble(3) );  // length [meters]
     // block.setBearing( (float)( cursor.getDouble(4) ) ); 
     block.mBearing      = (float)( cursor.getDouble(4) );  // bearing [degrees]
     block.mClino        = (float)( cursor.getDouble(5) );  // clino [degrees]
     block.mAcceleration = (float)( cursor.getDouble(6) );
     block.mMagnetic     = (float)( cursor.getDouble(7) );
     block.mDip          = (float)( cursor.getDouble(8) );
     
     block.setExtend( (int)(cursor.getLong(9) ) );
     block.resetFlag( cursor.getLong(10) );
     if ( leg == LegType.EXTRA ) {
       block.setBlockType( DBlock.BLOCK_SEC_LEG );
     } else if ( leg == LegType.XSPLAY ) {
       block.setBlockType( DBlock.BLOCK_X_SPLAY );
     } else if ( leg == LegType.BACK ) {
       block.setBlockType( DBlock.BLOCK_BACK_LEG );
     }
     // Log.v("DistoXX", "A7 fill block " + cursor.getLong(0) + " leg " + leg + " flag " + cursor.getLong(10) );
     block.mComment = cursor.getString(12);
     block.mShotType = (int)cursor.getLong(13);
     block.mTime = cursor.getLong(14);
     int color = (int)cursor.getLong(15);
     block.mPaint = ( color == 0 )? null : BrushManager.makePaint( color );
   }
   

   // ----------------------------------------------------------------------
   // SURVEY DATA

  String getSurveyInitailStation( long id )
  {
    String ret = "";
    if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( SURVEY_TABLE,
			        new String[] { "init_station" },
                                "id=? ", 
                                new String[] { Long.toString(id) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
    if ( cursor != null ) {
      if (cursor.moveToFirst()) {
        ret = cursor.getString(0);
      }
      if ( ! cursor.isClosed() ) cursor.close();
    }
    return ret;
  }

  // at-station xsextions type
  //   0 : shared
  //   1 : private
  int getSurveyXSections( long id )
  {
    int ret = 0;
    if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( SURVEY_TABLE,
			        new String[] { "xsections" },
                                "id=? ", 
                                new String[] { Long.toString(id) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
    if ( cursor != null ) {
      if (cursor.moveToFirst()) {
        ret = (int)cursor.getLong(0);
      }
      if ( ! cursor.isClosed() ) cursor.close();
    }
    return ret;
  }

  float getSurveyDeclination( long sid )
  {
    float ret = 0;
    if ( myDB == null ) return 0;
    Cursor cursor = myDB.query( SURVEY_TABLE,
			        new String[] { "declination" },
                                "id=?", 
                                new String[] { Long.toString(sid) },
                                null,  // groupBy
                                null,  // having
                                null ); // order by
    if (cursor.moveToFirst()) {
      ret = (float)(cursor.getDouble( 0 ));
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }


  SurveyStat getSurveyStat( long sid )
  {
    // TDLog.Log( TDLog.LOG_DB, "Get Survey Stat sid " + sid );
    HashMap< String, Integer > map = new HashMap< String, Integer >();
    int n0 = 0;
    int nc = 0;
    int ne = 0;
    int nl = 0;
    int nv = 0;

    SurveyStat stat = new SurveyStat();
    stat.id = sid;
    stat.lengthLeg  = 0.0f;
    stat.extLength  = 0.0f;
    stat.planLength = 0.0f;
    stat.lengthDuplicate = 0.0f;
    stat.lengthSurface   = 0.0f;
    stat.countLeg = 0;
    stat.countDuplicate = 0;
    stat.countSurface   = 0;
    stat.countSplay     = 0;
    stat.countStation   = 0;
    stat.countLoop      = 0;
    stat.countComponent = 0;
    stat.averageM = 0;
    stat.averageG = 0;
    stat.averageD = 0;
    stat.stddevM  = 0;
    stat.stddevG  = 0;
    stat.stddevD  = 0;
    stat.nrMGD = 0;

    if ( myDB == null ) return stat;

    Cursor cursor = myDB.query( SHOT_TABLE,
			        new String[] { "flag", "acceleration", "magnetic", "dip" },
                                "surveyId=? AND status=0 AND acceleration > 1 ",
                                new String[] { Long.toString(sid) },
                                null, null, null );
    int nrMGD = 0;
    if (cursor.moveToFirst()) {
      int nr = cursor.getCount();
      stat.G = new float[ nr ];
      stat.M = new float[ nr ];
      stat.D = new float[ nr ];
      do {
        int k = 0;
        float a = (float)( cursor.getDouble(1) );
        if ( a > 0.1f ) {
          float m = (float)( cursor.getDouble(2) );
          float d = (float)( cursor.getDouble(3) );
          stat.averageM += m;
          stat.averageG += a;
          stat.averageD += d;
          stat.stddevM  += m * m;
          stat.stddevG  += a * a;
          stat.stddevD  += d * d;
          stat.G[nrMGD]  = a;
          stat.M[nrMGD]  = m;
          stat.D[nrMGD]  = d;
          ++nrMGD;
        }
      } while ( cursor.moveToNext() );
      stat.nrMGD = nrMGD;
      if ( nrMGD > 0 ) {
        stat.averageM /= nrMGD;
        stat.averageG /= nrMGD;
        stat.averageD /= nrMGD;
        stat.stddevM   = (float)Math.sqrt( stat.stddevM / nrMGD - stat.averageM * stat.averageM );
        stat.stddevG   = (float)Math.sqrt( stat.stddevG / nrMGD - stat.averageG * stat.averageG );
        stat.stddevD   = (float)Math.sqrt( stat.stddevD / nrMGD - stat.averageD * stat.averageD );
        stat.stddevM  *= 100/stat.averageM;
        stat.stddevG  *= 100/stat.averageG;
      }
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

    // count components
    cursor = myDB.query( SHOT_TABLE,
			 new String[] { "flag", "distance", "fStation", "tStation", "clino", "extend" },
                         "surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ", 
                         new String[] { Long.toString(sid) },
                         null, null, null );
    if (cursor.moveToFirst()) {
      do {
	    float len = (float)( cursor.getDouble(1) );
        switch ( (int)(cursor.getLong(0)) ) {
          case 0: // NORMAL SHOT
	        ++ stat.countLeg;
            stat.lengthLeg += len;
	        if ( cursor.getLong(5) == 0 ) {
              stat.extLength += len * Math.abs( Math.sin( cursor.getDouble(4)*TDMath.DEG2RAD ) );
	        } else {
              stat.extLength += len;
	        }
            stat.planLength += (float)( len * Math.cos( cursor.getDouble(4)*TDMath.DEG2RAD ) );
            break;
          case 1: // SURFACE SHOT
	        ++ stat.countSurface;
            stat.lengthSurface += len;
            break;
          case 2: // DUPLICATE SHOT
	        ++ stat.countDuplicate;
            stat.lengthDuplicate += len;
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
            Integer fi = Integer.valueOf( n0 );
            map.put( t, fi );
            map.put( f, fi );
            nv += 2;
            ++ nc;
          }
        }

      } while ( cursor.moveToNext() );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

    stat.countStation = map.size();
    stat.countLoop = nl;
    stat.countComponent = nc;
    // TDLog.Log( TDLog.LOG_DB, "Get Survey Stats NV " + nv + " NE " + ne + " NL " + nl + " NC " + nc);

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
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return stat;
  }

  // --------------------------------------------------------------------
  // compile statements

  void compileStatements()
  {
    if ( myDB != null ) {
      // clearStationsStmt = myDB.compileStatement( "UPDATE shots SET fStation=\"\", tStation=\"\" where id>? and surveyId=?" );
      // updateShotDBCStmt = myDB.compileStatement( "UPDATE shots SET distance=?, bearing=?, clino=? WHERE surveyId=? AND id=?" );
      updateShotStmtFull   = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, leg=?, comment=? WHERE surveyId=? AND id=?" );
      updateShotStmt       = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, leg=? WHERE surveyId=? AND id=?" );
      updateAudioStmt      = myDB.compileStatement( "UPDATE audios SET date=? WHERE surveyId=? AND shotId=?" );

      // updateShotNameStmt    = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=? WHERE surveyId=? AND id=?" );
      // updateShotExtendStmt  = myDB.compileStatement( "UPDATE shots SET extend=? WHERE surveyId=? AND id=?" );
      // updateShotCommentStmt = myDB.compileStatement( "UPDATE shots SET comment=? WHERE surveyId=? AND id=?" );
      // shiftShotsIdStmt   = myDB.compileStatement( "UPDATE shots SET id=id+1 where surveyId=? and id>=?" );
      // updateShotLegStmt  = myDB.compileStatement( "UPDATE shots SET leg=? WHERE surveyId=? AND id=?" );
      // updateShotFlagStmt = myDB.compileStatement( "UPDATE shots SET flag=? WHERE surveyId=? AND id=?" );
      // updateShotLegFlagStmt = myDB.compileStatement( "UPDATE shots SET leg=?, flag=? WHERE surveyId=? AND id=?" );

      resetShotColorStmt   = myDB.compileStatement( "UPDATE shots SET color=0 WHERE surveyId=?" );
      updateShotColorStmt  = myDB.compileStatement( "UPDATE shots SET color=? WHERE surveyId=? AND id=?" );
      updateShotAMDRStmt   =
        myDB.compileStatement( "UPDATE shots SET acceleration=?, magnetic=?, dip=?, roll=? WHERE surveyId=? AND id=?" );

      // updatePlotStmt     = myDB.compileStatement( "UPDATE plots set xoffset=?, yoffset=?, zoom=? WHERE surveyId=? AND id=?" );
      updatePlotViewStmt = myDB.compileStatement( "UPDATE plots set view=? WHERE surveyId=? AND id=?" );
      updatePlotHideStmt = myDB.compileStatement( "UPDATE plots set hide=? WHERE surveyId=? AND id=?" );
      updatePlotNameStmt = myDB.compileStatement( "UPDATE plots set name=? WHERE surveyId=? AND id=?" );
      updatePlotOrientationStmt = myDB.compileStatement( "UPDATE plots set orientation=? WHERE surveyId=? AND id=?" );
      updatePlotAzimuthClinoStmt = myDB.compileStatement( "UPDATE plots set azimuth=?, clino=? WHERE surveyId=? AND id=?" );
      updatePlotNickStmt = myDB.compileStatement( "UPDATE plots set nick=? WHERE surveyId=? AND id=?" );
      // dropPlotStmt    = myDB.compileStatement( "DELETE FROM plots WHERE surveyId=? AND id=?" );

      updateSketchStmt = myDB.compileStatement( "UPDATE sketches set st1=?, st2=?, xoffsettop=?, yoffsettop=?, zoomtop=?, xoffsetside=?, yoffsetside=?, zoomside=?, xoffset3d=?, yoffset3d=?, zoom3d=?, east=?, south=?, vert=?, azimuth=?, clino=? WHERE surveyId=? AND id=?" );

      // deleteSketchStmt = myDB.compileStatement( "UPDATE sketches set status=1 WHERE surveyId=? AND id=?" );
      // updatePhotoStmt  = myDB.compileStatement( "UPDATE photos set comment=? WHERE surveyId=? AND id=?" );
      // deletePhotoStmt  = myDB.compileStatement( "DELETE FROM photos WHERE surveyId=? AND id=?" );
      // deleteSensorStmt = myDB.compileStatement( "UPDATE sensors set status=1 WHERE surveyId=? AND id=?" );
      // updateSensorStmt = myDB.compileStatement( "UPDATE sensors set comment=? WHERE surveyId=? AND id=?" );

      transferShotStmt   = myDB.compileStatement( "UPDATE shots SET surveyId=?, id=? where surveyId=? and id=?" );
      transferPlotStmt   = myDB.compileStatement( "UPDATE plots set surveyId=? WHERE surveyId=? AND id=?" );
      transferSketchStmt = myDB.compileStatement( "UPDATE sketches set surveyId=? WHERE surveyId=? AND id=?" );
      // transferSensorStmt  = myDB.compileStatement( "UPDATE sensors set surveyId=?, shotId=? WHERE surveyId=? AND id=?" );
      // transferPhotoStmt   = myDB.compileStatement( "UPDATE photos set surveyId=?, shotId=? WHERE surveyId=? AND id=?" );
      // transferFixedStmt   = myDB.compileStatement( "UPDATE fixeds set surveyId=? WHERE surveyId=? AND id=?" );
      // transferStationStmt = myDB.compileStatement( "UPDATE stations set surveyId=? WHERE surveyId=? AND name=?" );

      // dropFixedStmt       = myDB.compileStatement( "DELETE FROM fixeds where surveyId=? and station=? and status=1" );
      // updateFixedStationStmt = myDB.compileStatement( "UPDATE fixeds set station=? WHERE surveyId=? AND id=?" );
      // updateFixedStatusStmt  = myDB.compileStatement( "UPDATE fixeds set status=? WHERE surveyId=? AND id=?" );
      // updateFixedCommentStmt = myDB.compileStatement( "UPDATE fixeds set station=?, comment=? WHERE surveyId=? AND id=?" );
      // updateFixedAltStmt     = myDB.compileStatement( "UPDATE fixeds set altitude=?, altimetric=? WHERE surveyId=? AND id=?" );
      // updateFixedDataStmt    = myDB.compileStatement( "UPDATE fixeds set longitude=?, latitude=?, altitude=? WHERE surveyId=? AND id=?" );

      updateStationCommentStmt = myDB.compileStatement( "UPDATE stations SET comment=?, flag=? WHERE surveyId=? AND name=?" );
      // deleteStationStmt        = myDB.compileStatement( "DELETE FROM stations WHERE surveyId=? AND name=?" );
    }
    // Log.v("DistoX", "compile statements done");
  }

  private void logError( String msg, Exception e )
  {
    TDLog.Error("DB " + msg + ": " + e.getMessage() );
  }

  private void handleDiskIOError( SQLiteDiskIOException e )
  {
    Log.e("DistoX", "DB disk error " + e.getMessage() );
    mApp.mActivity.runOnUiThread( new Runnable() {
      public void run() {
        TDToast.makeBG( mContext, R.string.disk_io_error, TDColor.BROWN );
      }
    } );
  }

  // --------------------------------------------------------------------
  // SURVEY
  // survey attributes renaming are "rare" actions

  boolean renameSurvey( long id, String name, boolean forward )
  {
    boolean ret = true;
    ContentValues vals = new ContentValues();
    vals.put("name", name );
    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, vals, "id=?", new String[]{ Long.toString(id) } );
      myDB.setTransactionSuccessful();
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateSurveyName( id, name );
      }
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e ); ret = false;
    } catch ( SQLiteException e1 )       { logError("survey rename " + name, e1 ); ret =false;
    } catch ( IllegalStateException e2 ) { logError("survey rename", e2 ); ret = false;
    } finally { myDB.endTransaction(); }
    return ret;
  }

  boolean updateSurveyInfo( long id, String date, String team, double decl, String comment,
                                String init_station, int xsections, boolean forward )
  {
    boolean ret = false;
    ContentValues vals = new ContentValues();
    vals.put( "day", date );
    vals.put( "team", ((team != null)? team : "") );
    vals.put( "declination", decl );
    vals.put( "comment", ((comment != null)? comment : "") );
    vals.put( "init_station", ((init_station != null)? init_station : "") );
    vals.put( "xsections", xsections );

    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, vals, "id=?", new String[]{ Long.toString(id) } );
      myDB.setTransactionSuccessful();
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateSurveyInfo( id, date, team, decl, comment, init_station, xsections );
      }
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError("survey info", e1 ); 
    } catch ( IllegalStateException e2 ) { logError("survey info", e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  boolean updateSurveyDayAndComment( String name, String date, String comment, boolean forward )
  {
    boolean ret = false;
    long id = getIdFromName( SURVEY_TABLE, name );
    if ( id >= 0 ) { // survey name exists
      ret = updateSurveyDayAndComment( id, date, comment, forward );
    }
    return ret;
  }

  private boolean doUpdateSurvey( long id, ContentValues vals, String msg )
  {
    boolean ret = false;
    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, vals, "id=?", new String[]{ Long.toString(id) } );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 ); 
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  private boolean doUpdate( String table, ContentValues vals, long sid, long id, String msg )
  {
    boolean ret = false;
    try {
      myDB.beginTransaction();
      myDB.update( table, vals, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 );
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  private boolean doInsert( String table, ContentValues cv, String msg )
  {
    boolean ret = false;
    try { 
      myDB.beginTransaction();
      myDB.insert( table, null, cv ); 
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 ); 
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  private boolean doExecSQL( StringWriter sw, String msg )
  {
    boolean ret = false;
    try {
      myDB.beginTransaction();
      myDB.execSQL( sw.toString() );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 );
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  private boolean doExecShotSQL( long id, StringWriter sw ) { return doExecSQL( sw, "shot " + id ); }

  private boolean updateStatus( String table, long id, long sid, long status )
  {
    boolean ret = false;
    ContentValues vals = new ContentValues();
    vals.put( "status", status );
    try {
      myDB.beginTransaction();
      myDB.update( table, vals, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  {  handleDiskIOError( e );
    } catch (SQLiteException e1 )        { logError(table + " update " + id, e1 ); 
    } catch ( IllegalStateException e2 ) { logError(table + " update " + id, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  private boolean doStatement( SQLiteStatement stmt, String msg )
  {
    boolean ret = false;
    try {
      myDB.beginTransaction();
      stmt.execute();
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e0 ) { handleDiskIOError( e0 );
    } catch ( SQLiteException e1 )       { logError(msg, e1 ); 
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  // -----------------------------------------------------------------

  boolean updateSurveyDayAndComment( long id, String date, String comment, boolean forward )
  {
    if ( date == null ) return false;
    ContentValues vals = new ContentValues();
    vals.put( "day", date );
    vals.put( "comment", (comment != null)? comment : "" );
    if ( doUpdateSurvey( id, vals, "survey day+cmt" ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateSurveyDayAndComment( id, date, comment );
      }
    }
    return true;
  }

  void updateSurveyTeam( long id, String team, boolean forward )
  {
    ContentValues vals = new ContentValues();
    vals.put( "team", team );
    if ( doUpdateSurvey( id, vals, "survey team" ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateSurveyTeam( id, team );
      }
    }
  }

  void updateSurveyInitStation( long id, String station, boolean forward )
  {
    ContentValues vals = new ContentValues();
    vals.put( "init_station", station );
    if ( doUpdateSurvey( id, vals, "survey init_station" ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateSurveyInitStation( id, station );
      }
    }
  }
  void updateSurveyDeclination( long id, double decl, boolean forward )
  {
    ContentValues vals = new ContentValues();
    vals.put( "declination", decl );
    if ( doUpdateSurvey( id, vals, "survey decl" ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateSurveyDeclination( id, decl );
      }
    } 
  }

  // -----------------------------------------------------------------------
  // SURVEY delete

  void doDeleteSurvey( long sid )
  {
    if ( myDB == null ) return;
    String[] clause = new String[]{ Long.toString( sid ) };

    try {
      myDB.setLockingEnabled( false );
      myDB.beginTransaction();
      myDB.delete( PHOTO_TABLE,   WHERE_SID, clause );
      myDB.delete( AUDIO_TABLE,   WHERE_SID, clause );
      myDB.delete( PLOT_TABLE,    WHERE_SID, clause );
      myDB.delete( FIXED_TABLE,   WHERE_SID, clause );
      myDB.delete( SHOT_TABLE,    WHERE_SID, clause );
      myDB.delete( STATION_TABLE, WHERE_SID, clause );
      myDB.delete( SURVEY_TABLE, "id=?", clause );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) {  handleDiskIOError( e );
    } catch ( SQLiteException e ) { logError("survey delete", e);
    } finally {
      myDB.endTransaction();
      myDB.setLockingEnabled( true );
    }
  }
  
  // --------------------------------------------------------------------
  // SHOTS

  // void clearStationsAfter( long id, long sid, boolean forward ) 
  // {
  //   // update shots set fStation="", tStation="" where id>id and surveyId=sid
  //   if ( clearStationsStmt == null )
  //     clearStationsStmt = myDB.compileStatement( "UPDATE shots SET fStation=\"\", tStation=\"\" where id>? and surveyId=?" );
  //   clearStationsStmt.bindLong( 1, id );
  //   clearStationsStmt.bindLong( 2, sid );
  //   try { clearStationsStmt.execute(); } catch (SQLiteException e ) { logError("clear station after", e); }
  //   if ( forward ) {
  //     // no need to forward ?
  //   }
  // }


  // this is an update of a manual-shot data
  void updateShotDistanceBearingClino( long id, long sid, float d, float b, float c, boolean forward )
  {
    // updateShotDBCStmt.bindDouble(  1, d );
    // updateShotDBCStmt.bindDouble(  2, b );
    // updateShotDBCStmt.bindDouble(  3, c );
    // updateShotDBCStmt.bindLong(   4, sid );     // WHERE
    // updateShotDBCStmt.bindLong(   5, id );
    // updateShotDBCStmt.execute();

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US,
               "UPDATE shots SET distance=%.6f, bearing=%.4f, clino=%.4f WHERE surveyId=%d AND id=%d",
               d, b, c, sid, id );
    if ( doExecShotSQL( id, sw ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotDBC( id, sid, d, b, c );
      }
    } 
  }

  int updateShot( long id, long sid, String fStation, String tStation,
                  long extend, long flag, long leg,
		  String comment, boolean forward )
  {
    boolean success = false;
    if ( myDB == null ) return -1;
    // if ( makesCycle( id, sid, fStation, tStation ) ) return -2;

    // Log.v("DistoXX", "A8 update shot. id " + id + " leg " + leg );
    if ( updateShotStmtFull == null ) {
      updateShotStmtFull = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, leg=?, comment=? WHERE surveyId=? AND id=?" );
    }
    if ( updateShotStmt == null ) {
      updateShotStmt = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, leg=? WHERE surveyId=? AND id=?" );
    }

    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter( sw );
    // if ( comment != null ) {
    //   pw.format( Locale.US,
// "UPDATE shots SET fStation=\"%s\", tStation=\"%s\", extend=%d, flag=%d, leg=%d, comment=\"%s\" WHERE surveyId=%d AND id=%d",
    //      fStation, tStation, extend, flag, leg, comment, sid, id );
    // } else {
    //   pw.format( Locale.US,
    //   "UPDATE shots SET fStation=\"%s\", tStation=\"%s\", extend=%d, flag=%d, leg=%d WHERE surveyId=%d AND id=%d",
    //      fStation, tStation, extend, flag, leg, sid, id );
    // }
    // try {
    //   myDB.execSQL( sw.toString() );
    // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e ); }

    if ( tStation == null ) tStation = "";
    try {
      myDB.beginTransaction();
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
        success = true;
      } else {
        updateShotStmt.bindString( 1, fStation );
        updateShotStmt.bindString( 2, tStation );
        updateShotStmt.bindLong(   3, extend );
        updateShotStmt.bindLong(   4, flag );
        updateShotStmt.bindLong(   5, leg );
        updateShotStmt.bindLong(   6, sid );
        updateShotStmt.bindLong(   7, id );
        updateShotStmt.execute();
        success = true;
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch ( SQLiteException e ) { logError("Shot update sqlite error " + fStation + " " + tStation, e );
      // TopoDroidUtil.slowDown( 50 );
    } catch ( IllegalStateException e2 ) { logError("Shot update sqlite error " + fStation + " " + tStation, e2 );
    } finally {
      myDB.endTransaction();
    }

    // Log.v("DistoX", "update shot " + fStation + " " + tStation + " success " + success );
    if ( success && forward && mListeners != null ) { // synchronized( mListeners )
      mListeners.onUpdateShot( id, sid, fStation, tStation, extend, flag, leg, comment );
    }
    return 0;
  }

  private void shiftShotsId( long sid, long id )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET id=id+1 where surveyId=%d and id>=%d", sid, id );
    doExecShotSQL( id, sw );

    // shiftShotsIdStmt.bindLong(1, sid);
    // shiftShotsIdStmt.bindLong(2, id);
    // try { shiftShotsIdStmt.execute(); } catch (SQLiteException e ) { }
  }

  // public boolean makesCycle( long id, long sid, String f, String t )
  // {
  //   if ( t == null || t.length() == 0 ) return false;
  //   if ( f == null || f.length() == 0 ) return false;
  //   int cnt = 0;
  //   if ( hasShotAtStation( id, sid, f ) ) ++cnt;
  //   if ( hasShotAtStation( id, sid, t ) ) ++cnt;
  //   TDLog.Log( TDLog.LOG_DB, "makesCycle cnt " + cnt );
  //   return cnt >= 2;
  // }


  void updateShotName( long id, long sid, String fStation, String tStation, boolean forward )
  {
    if ( myDB == null ) return;
    if ( fStation == null ) fStation = "";
    if ( tStation == null ) tStation = "";
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET fStation=\"%s\", tStation=\"%s\" WHERE surveyId=%d AND id=%d",
               fStation, tStation, sid, id );
    myDB.beginTransaction();
    try {
      myDB.execSQL( sw.toString() );
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotName( id, sid, fStation, tStation );
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch ( SQLiteException e ) { logError("shot " + id + " name " + fStation + " " + tStation, e );
    } finally { myDB.endTransaction(); }
  }

  // "leg" flag: 0 splay, 1 leg, 2 x-splay
  void updateShotLeg( long id, long sid, long leg, boolean forward )
  {
    // Log.v("DistoXX", "A1 update shot leg. id " + id + " leg " + leg ); 
    // if ( myDB == null ) return;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET leg=%d WHERE surveyId=%d AND id=%d", leg, sid, id );
    myDB.beginTransaction();
    try {
      myDB.execSQL( sw.toString() );
      myDB.setTransactionSuccessful();
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotLeg( id, sid, leg );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
      Log.e( "DistoX", e.getMessage() );
    } catch (SQLiteException e ) { logError("shot " + id + " leg", e );
    } finally { myDB.endTransaction(); }
   
  }

  // FIXME_X_SPLAY
  long updateSplayLeg( long id, long sid, boolean forward )
  {
    // if ( myDB == null ) return;
    // get the shot sid/id
    DBlock blk = selectShot( id, sid );
    if ( blk.isPlainSplay() ) {
      updateShotLeg( id, sid, LegType.XSPLAY, forward );
      return LegType.XSPLAY;
    } else if ( blk.isXSplay() ) {
      updateShotLeg( id, sid, LegType.NORMAL, forward );
      return LegType.NORMAL;
    }
    return LegType.INVALID;
  }

  void updateShotExtend( long id, long sid, long extend, boolean forward )
  {
    // if ( myDB == null ) return;

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET extend=%d WHERE surveyId=%d AND id=%d", extend, sid, id );
    if ( doExecShotSQL( id, sw ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotExtend( id, sid, extend );
      }
    } 
  }

  void updateShotFlag( long id, long sid, long flag, boolean forward )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET flag=%d WHERE surveyId=%d AND id=%d", flag, sid, id );
    if ( doExecShotSQL( id, sw ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotFlag( id, sid, flag );
      }
    } 
  }

  void updateShotLegFlag( long id, long sid, long leg, long flag, boolean forward )
  {
    // Log.v("DistoXX", "A2 update shot leg/flag. id " + id + " leg " + leg + " flag " + flag ); 
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET leg=%d, flag=%d WHERE surveyId=%d AND id=%d", leg, flag, sid, id );
    if ( doExecShotSQL( id, sw ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotLegFlag( id, sid, leg, flag );
      }
    } 
  }

  void updateShotComment( long id, long sid, String comment, boolean forward )
  {
    // if ( myDB == null ) return;
    if ( comment == null ) comment = "";
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET comment=\"%s\" WHERE surveyId=%d AND id=%d", comment, sid, id );
    if ( doExecShotSQL( id, sw ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotComment( id, sid, comment );
      }
    } 
  }

  void updateShotStatus( long id, long sid, long status, boolean forward )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET status=%d WHERE surveyId=%d AND id=%d", status, sid, id );
    if ( doExecShotSQL( id, sw ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotStatus( id, sid, status );
      }
    } 
  }

  void deleteShot( long id, long sid, int status, boolean forward )
  {
    // if ( myDB == null ) return;
    if ( updateStatus( SHOT_TABLE, id, sid, status ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onDeleteShot( id, sid, status );
      }
    }
  }

  void undeleteShot( long id, long sid, boolean forward )
  {
    // if ( myDB == null ) return;
    if ( updateStatus( SHOT_TABLE, id, sid, TDStatus.NORMAL ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUndeleteShot( id, sid );
      }
    }
  }
  
  // called by the importXXXTask's
  long insertShots( long sid, long id, ArrayList< ParserShot > shots )
  {
    // if ( myDB == null ) return -1L;
    long millis = 0L;
    long color  = 0L;

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
    final int millisCol   = ih.getColumnIndex( "millis" );
    final int colorCol    = ih.getColumnIndex( "color" );
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
        ih.bind( flagCol, s.duplicate ? DBlock.FLAG_DUPLICATE 
                        : s.surface ? DBlock.FLAG_SURFACE 
                        // : s.commented ? DBlock.FLAG_COMMENTED // ParserShots are not "commented"
                        // : s.backshot ? DBlock.FLAG_BACKSHOT
                        : 0 );
        ih.bind( legCol, s.leg );
        ih.bind( statusCol, 0 );
        ih.bind( commentCol, s.comment );
        ih.bind( typeCol, 0 ); // parser shot are not-modifiable
        ih.bind( millisCol, millis );
        ih.bind( colorCol, color );
        ih.execute();
        ++id;
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError("parser shot insert", e);
    } finally {
      ih.close(); // FIXME this was before endTransaction
      myDB.endTransaction();
      myDB.setLockingEnabled( true );
      // myDB.execSQL("PRAGMA synchronous=NORMAL");
    }
    if ( mListeners != null ) {
      // synchronized( mListeners )
      for ( ParserShot s : shots ) {
        mListeners.onInsertShot( sid, id, millis, color, s.from, s.to, s.len, s.ber, s.cln, s.rol, s.extend, 
                            s.duplicate ? DBlock.FLAG_DUPLICATE    // flag
                            : s.surface ? DBlock.FLAG_SURFACE 
                            // : s.commented ? DBlock.FLAG_COMMENTED 
                            // : s.backshot ? DBlock.FLAG_BACKSHOT
                            : 0,
                            s.leg, // leg
                            0L, // status
                            0L, // shot_type: parser-shots are not modifiable
                            s.comment );
      }
    }
    return id;
  }

  long insertDistoXShot( long sid, long id, double d, double b, double c, double r, long extend,
                          long status, boolean forward )
  { // 0L=leg, status, 0L=type DISTOX
    return doInsertShot( sid, id, System.currentTimeMillis()/1000, 0L, "", "",  d, b, c, r, extend, DBlock.FLAG_SURVEY, 0L, status, 0L, "", forward );
  }

  long insertShot( long sid, long id, long millis, long color, double d, double b, double c, double r, long extend, long leg,
                          long shot_type, boolean forward )
  { // leg, 0L=status, type 
    return doInsertShot( sid, id, millis, color, "", "",  d, b, c, r, extend, DBlock.FLAG_SURVEY, leg, 0L, shot_type, "", forward );
  }

  boolean resetShotColor( long sid )
  {
    if ( resetShotColorStmt == null ) {
      resetShotColorStmt   =
        myDB.compileStatement( "UPDATE shots SET color=0 WHERE surveyId=?" );
    }
    resetShotColorStmt.bindLong( 1, sid );
    return doStatement( resetShotColorStmt, "Color" );
  }

  boolean updateShotColor( long id, long sid, int color, boolean forward )
  {
    // if ( myDB == null ) return;

    // StringWriter sw = new StringWriter();
    // PrintWriter pw  = new PrintWriter( sw );
    // pw.format( Locale.US,
    //            "UPDATE shots SET color=%d WHERE surveyId=%d AND id=%d",
    //            color, sid, id );
    // myDB.execSQL( sw.toString() );
    if ( updateShotColorStmt == null ) {
      updateShotColorStmt   =
        myDB.compileStatement( "UPDATE shots SET color=? WHERE surveyId=? AND id=?" );
    }

    updateShotColorStmt.bindLong( 1, color );
    updateShotColorStmt.bindLong( 2, sid );
    updateShotColorStmt.bindLong( 3, id );
    if ( doStatement( updateShotColorStmt, "Color" ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotColor( sid, id, color );
      }
      return true;
    }
    return false;
  }

  boolean updateShotAMDR( long id, long sid, double acc, double mag, double dip, double r, boolean forward )
  {
    // if ( myDB == null ) return;

    // StringWriter sw = new StringWriter();
    // PrintWriter pw  = new PrintWriter( sw );
    // pw.format( Locale.US,
    //            "UPDATE shots SET acceleration=%.6f, magnetic=%.6f, dip=%.4f, roll=%.6f WHERE surveyId=%d AND id=%d",
    //            acc, mag, dip, r, sid, id );
    // myDB.execSQL( sw.toString() );
    if ( updateShotAMDRStmt == null ) {
      updateShotAMDRStmt   =
        myDB.compileStatement( "UPDATE shots SET acceleration=?, magnetic=?, dip=?, roll=? WHERE surveyId=? AND id=?" );
    }

    updateShotAMDRStmt.bindDouble( 1, acc );
    updateShotAMDRStmt.bindDouble( 2, mag );
    updateShotAMDRStmt.bindDouble( 3, dip );
    updateShotAMDRStmt.bindDouble( 4, r );
    updateShotAMDRStmt.bindLong( 5, sid );
    updateShotAMDRStmt.bindLong( 6, id );
    if ( doStatement( updateShotAMDRStmt, "AMDR" ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onUpdateShotAMDR( sid, id, acc, mag, dip, r );
      }
      return true;
    }
    return false;
  }

  private void renamePlotFile( String oldname, String newname )
  {
    File oldfile = new File( oldname );
    File newfile = new File( newname );
    if ( oldfile.exists() ) {
      if ( ! newfile.exists() ) {
        if ( ! oldfile.renameTo( newfile ) ) TDLog.Error("File rename error");
      } else {
        TDLog.Error("Plot rename: " + newname + " exists" );
      }
    // } else { // THIS IS OK
    //   TDLog.Error( "Failed rename. Old file does not exist: " + oldname );
    }
  }

  private void transferPlots( String old_survey_name, String new_survey_name, long sid, long old_sid, String station )
  {
    if ( myDB == null ) return;
    List< PlotInfo > plots = selectPlotsAtStation( old_sid, station );
    for ( PlotInfo plot : plots ) {
      transferPlot( sid, old_sid, plot.id );
      renamePlotFile( TDPath.getTh2File( old_survey_name + "-" + plot.name + ".th2" ),
                      TDPath.getTh2File( new_survey_name + "-" + plot.name + ".th2" ) );

      renamePlotFile( TDPath.getTdrFile( old_survey_name + "-" + plot.name + ".tdr" ),
                      TDPath.getTdrFile( new_survey_name + "-" + plot.name + ".tdr" ) );
    }
  }

  private void transferSketches( String old_survey_name, String new_survey_name, long sid, long old_sid, String station )
  {
    if ( myDB == null ) return;
    List< Sketch3dInfo > sketches = selectSketchesAtStation( old_sid, station );
    for ( Sketch3dInfo sketch : sketches ) {
      transferSketch( sid, old_sid, sketch.id );
      File oldfile = new File( TDPath.getTdr3File( old_survey_name + "-" + sketch.name + ".tdr3" ) );
      File newfile = new File( TDPath.getTdr3File( new_survey_name + "=" + sketch.name + ".tdr3" ) );
      if ( oldfile.exists() && ! newfile.exists() ) {
        if ( ! oldfile.renameTo( newfile ) ) TDLog.Error("File rename error");
      } else {
        TDLog.Error( "Sketch rename: " + sketch.name + " exists" );
      }
    }
  }

  void transferShots( long sid, long old_sid, long old_id )
  {
    if ( myDB == null ) return;
    SurveyInfo old_survey = selectSurveyInfo( old_sid );
    SurveyInfo new_survey = selectSurveyInfo( sid );
    long max_id = maxId( SHOT_TABLE, old_sid );

    ContentValues vals0 = new ContentValues();
    vals0.put( "surveyId", sid );
    String[] where0 = new String[2];
    where0[0] = Long.toString( old_sid );

    try {
      while ( old_id < max_id ) {
        DBlock blk = selectShot( old_id, old_sid );
        if ( blk == null ) continue;

        if ( transferShotStmt == null ) {
          transferShotStmt = myDB.compileStatement( "UPDATE shots SET surveyId=?, id=? where surveyId=? and id=?" );
        }
        transferShotStmt.bindLong(1, sid);
        transferShotStmt.bindLong(2, myNextId);
        transferShotStmt.bindLong(3, old_sid);
        transferShotStmt.bindLong(4, old_id);
        transferShotStmt.execute();
        
        // transfer fixeds, stations, plots and sketches
        // TODO FIXME cross-sections 
        if ( blk.mFrom.length() > 0 ) {
          List< FixedInfo > fixeds = selectFixedAtStation( old_sid, blk.mFrom ); 
          for ( FixedInfo fixed : fixeds ) {
            where0[1] = Long.toString( fixed.id );
            myDB.update( FIXED_TABLE, vals0, WHERE_SID_ID, where0 );
          }
          where0[1] = blk.mFrom;
          myDB.update( STATION_TABLE, vals0, WHERE_SID_NAME, where0 );

          transferPlots( old_survey.name, new_survey.name, sid, old_sid, blk.mFrom );
          transferSketches( old_survey.name, new_survey.name, sid, old_sid, blk.mFrom );
        }
        if ( blk.mTo.length() > 0 ) {
          List< FixedInfo > fixeds = selectFixedAtStation( old_sid, blk.mTo );
          for ( FixedInfo fixed : fixeds ) {
            where0[1] = Long.toString( fixed.id );
            myDB.update( FIXED_TABLE, vals0, WHERE_SID_ID, where0 );
          }
          where0[1] = blk.mTo;
          myDB.update( STATION_TABLE, vals0, WHERE_SID_NAME, where0 ); 
          
          transferPlots( old_survey.name, new_survey.name, sid, old_sid, blk.mTo );
          transferSketches( old_survey.name, new_survey.name, sid, old_sid, blk.mFrom );
        }

        ContentValues vals = new ContentValues();
        vals.put( "surveyId", sid );
        vals.put( "shotId",   myNextId );
        String where[] = new String[2];
        where[0] = Long.toString( old_sid );
        List< SensorInfo > sensors = selectSensorsAtShot( old_sid, old_id ); // transfer sensors
        for ( SensorInfo sensor : sensors ) {
          where[1] = Long.toString( sensor.id );
          myDB.update( SENSOR_TABLE, vals, WHERE_SID_ID, where );
        }

        AudioInfo audio = getAudio( old_sid, old_id ); // transfer audio
        if ( audio != null ) {
          where[1] = Long.toString( audio.shotid );
          myDB.update( AUDIO_TABLE, vals, WHERE_SID_SHOTID, where );
          File oldfile = new File( TDPath.getSurveyAudioFile( old_survey.name, Long.toString(audio.shotid) ) );
          File newfile = new File( TDPath.getSurveyAudioFile( new_survey.name, Long.toString(audio.shotid) ) );
          if ( oldfile.exists() && ! newfile.exists() ) {
            if ( ! oldfile.renameTo( newfile ) )TDLog.Error("File rename error");
          } else {
            TDLog.Error( "Survey rename " + old_survey.name + "/" + audio.id + ".wav exists" );
          }
        }

        List< PhotoInfo > photos = selectPhotoAtShot( old_sid, old_id ); // transfer photos
        for ( PhotoInfo photo : photos ) {
          where[1] = Long.toString( photo.id );
          myDB.update( PHOTO_TABLE, vals, WHERE_SID_ID, where );
          File oldfile = new File( TDPath.getSurveyJpgFile( old_survey.name, Long.toString(photo.id) ) );
          File newfile = new File( TDPath.getSurveyJpgFile( new_survey.name, Long.toString(photo.id) ) );
          if ( oldfile.exists() && ! newfile.exists() ) {
            if ( ! oldfile.renameTo( newfile ) ) TDLog.Error("File rename error");
          } else {
            TDLog.Error( "Survey rename " + old_survey.name + "/" + photo.id + ".jpg exists" );
          }
        }

        ++ myNextId;
        ++ old_id;
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("transfer shots", e); }
  }

  long insertShotAt( long sid, long at, long millis, long color, double d, double b, double c, double r, long extend, long leg, long type, boolean forward )
  {
    if ( myDB == null ) return -1L;
    // Log.v("DistoXX", "A4 insert shot at " + at + " leg " + leg );
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
    cv.put( "extend",   extend );
    cv.put( "flag",     DBlock.FLAG_SURVEY ); // flag );
    cv.put( "leg",      leg ); // LegType.NORMAL ); // leg );
    cv.put( "status",   TDStatus.NORMAL ); // status );
    cv.put( "comment",  "" ); // comment );
    cv.put( "type",     type ); 
    cv.put( "millis",   millis );
    cv.put( "color",    color );

    if ( doInsert( SHOT_TABLE, cv, "insert at" ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onInsertShotAt( sid, at, millis, color, d, b, c, r, extend, leg, DBlock.FLAG_SURVEY );
      }
    }
    return at;
  }

  // return the new-shot id
  // called by ConnectionHandler too
  long doInsertShot( long sid, long id, long millis, long color, String from, String to, 
                          double d, double b, double c, double r, 
                          long extend, long flag, long leg, long status, long shot_type,
                          String comment, boolean forward )
  {
    // TDLog.Log( TDLog.LOG_DB, "insert shot <" + id + "> " + from + "-" + to + " extend " + extend );
    // Log.v("DistoXX", "A5 do insert shot id " + id + " leg " + leg + " flag " + flag );
    if ( myDB == null ) return -1L;
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
    cv.put( "type",     shot_type );
    cv.put( "millis",   millis );
    cv.put( "color",    color );
    if ( doInsert( SHOT_TABLE, cv, "insert" ) ) {
      if ( forward && mListeners != null ) { // synchronized( mListeners )
        mListeners.onInsertShot( sid,  id, millis, color, from, to, d, b, c, r, extend, flag, leg, status, shot_type, comment );
      }
    } 
    return id;
  }

  // -----------------------------------------------------------------
  // PLOT

  void updatePlot( long pid, long sid, double xoffset, double yoffset, double zoom )
  {
    if ( myDB == null ) return;
    // TDLog.Log( TDLog.LOG_DB, "update Plot: " + pid + "/" + sid + " x " + xoffset + " y " + yoffset + " zoom " + zoom);
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US,
               "UPDATE plots set xoffset=%.2f, yoffset=%.2f, zoom=%.2f WHERE surveyId=%d AND id=%d", 
               xoffset, yoffset, zoom, sid, pid );
    doExecSQL( sw, "update plot" );

    // FIXME with the update statement I get a crash on immediate switching Plan/Profile on load
    // updatePlotStmt.bindDouble( 1, xoffset );
    // updatePlotStmt.bindDouble( 2, yoffset );
    // updatePlotStmt.bindDouble( 3, zoom );
    // updatePlotStmt.bindLong( 4, sid );
    // updatePlotStmt.bindLong( 5, pid );
    // try { updatePlotStmt.execute(); } catch (SQLiteException e) { logError("plot update", e); }
  }
 
  boolean updatePlotNick( long pid, long sid, String nick )
  {
    if ( myDB == null ) return false;
    // TDLog.Log( TDLog.LOG_DB, "update PlotView: " + pid + "/" + sid + " view " + view );
    if ( nick == null ) nick = "";
    // StringWriter sw = new StringWriter();
    // PrintWriter  pw = new PrintWriter( sw );
    // pw.format( Locale.US, "UPDATE plots set view=\"%s\" WHERE surveyId=%d AND id=%d", view, sid, pid );
    // myDB.execSQL( sw.toString() );

    if ( updatePlotNickStmt == null ) {
      updatePlotNickStmt = myDB.compileStatement( "UPDATE plots set nick=? WHERE surveyId=? AND id=?" );
    }
    updatePlotNickStmt.bindString( 1, nick );
    updatePlotNickStmt.bindLong( 2, sid );
    updatePlotNickStmt.bindLong( 3, pid );
    return doStatement( updatePlotNickStmt, "plot nick" );
  }
 
  boolean updatePlotView( long pid, long sid, String view )
  {
    if ( myDB == null ) return false;
    // TDLog.Log( TDLog.LOG_DB, "update PlotView: " + pid + "/" + sid + " view " + view );
    if ( view == null ) view = "";
    // StringWriter sw = new StringWriter();
    // PrintWriter  pw = new PrintWriter( sw );
    // pw.format( Locale.US, "UPDATE plots set view=\"%s\" WHERE surveyId=%d AND id=%d", view, sid, pid );
    // myDB.execSQL( sw.toString() );

    if ( updatePlotViewStmt == null ) {
      updatePlotViewStmt = myDB.compileStatement( "UPDATE plots set view=? WHERE surveyId=? AND id=?" );
    }
    updatePlotViewStmt.bindString( 1, view );
    updatePlotViewStmt.bindLong( 2, sid );
    updatePlotViewStmt.bindLong( 3, pid );
    return doStatement( updatePlotViewStmt, "plot view" );
  }

  boolean updatePlotHide( long pid, long sid, String hide )
  {
    if ( myDB == null ) return false;
    // TDLog.Log( TDLog.LOG_DB, "update PlotHide: " + pid + "/" + sid + " hide " + hide );
    if ( hide == null ) hide = "";
    // StringWriter sw = new StringWriter();
    // PrintWriter  pw = new PrintWriter( sw );
    // pw.format( Locale.US, "UPDATE plots set hide=\"%s\" WHERE surveyId=%d AND id=%d", hide, sid, pid );
    // myDB.execSQL( sw.toString() );

    if ( updatePlotHideStmt == null ) {
      updatePlotHideStmt = myDB.compileStatement( "UPDATE plots set hide=? WHERE surveyId=? AND id=?" );
    }
    updatePlotHideStmt.bindString( 1, hide );
    updatePlotHideStmt.bindLong( 2, sid );
    updatePlotHideStmt.bindLong( 3, pid );
    return doStatement( updatePlotHideStmt, "plot hide" );
  }
   
  /** DROP is a real record delete from the database table
   */
  void dropPlot( long pid, long sid )
  {
    if ( myDB == null ) return;
    myDB.beginTransaction();
    try {
      myDB.delete( PLOT_TABLE, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(pid) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("plot drop", e);
    } finally { myDB.endTransaction(); }
  }

  void deletePlot( long pid, long sid )
  {
    // TDLog.Log( TDLog.LOG_DB, "deletePlot: " + pid + "/" + sid );
    if ( myDB == null ) return;
    updateStatus( PLOT_TABLE, pid, sid, TDStatus.DELETED );
  }

  // THIS REALLY DROPS THE RECORD FROM THE TABLE
  void deletePlotByName( String name, long sid )
  {
    if ( myDB == null ) return;
    myDB.beginTransaction();
    try {
      myDB.delete( PLOT_TABLE, WHERE_SID_NAME, new String[]{ Long.toString(sid), name } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("plot dropN", e);
    } finally { myDB.endTransaction(); }
  }
  
  void undeletePlot( long pid, long sid )
  {
    // TDLog.Log( TDLog.LOG_DB, "undeletePlot: " + pid + "/" + sid );
    if ( myDB == null ) return;
    updateStatus( PLOT_TABLE, pid, sid, TDStatus.NORMAL );
    // get plot type 
    Cursor cursor = myDB.query( PLOT_TABLE,
       		         new String[] { "type" }, // columns
                                WHERE_SID_ID,
                                new String[] { Long.toString(sid), Long.toString(pid) },
                                null, null, null );
    if ( cursor.moveToFirst() && cursor.getLong(0) == PlotInfo.PLOT_PLAN ) {
      updateStatus( PLOT_TABLE, pid+1, sid, TDStatus.NORMAL );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
  }
  

  // ----------------------------------------------------------------------
  // SELECT STATEMENTS

  List< SensorInfo > selectAllSensors( long sid, long status )
  {
    List< SensorInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query( SENSOR_TABLE,
       		         new String[] { "id", "shotId", "title", "date", "comment", "type", "value" }, // columns
                                WHERE_SID_STATUS,
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
                                 cursor.getString(5), // shot_type
                                 cursor.getString(6) ) ); // value
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Sensors list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    for ( SensorInfo si : list ) { // set shot-names to the sensor infos
      cursor = myDB.query( SHOT_TABLE, 
                           new String[] { "fStation", "tStation" },
                           WHERE_SID_ID,
                           new String[] { Long.toString(sid), Long.toString(si.shotid) },
                           null, null, null );
      if (cursor.moveToFirst()) {
        si.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
      }
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    return list;
  }

  private List< SensorInfo > selectSensorsAtShot( long sid, long shotid )
  {
    List< SensorInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query( SENSOR_TABLE,
       		         new String[] { "id", "shotId", "title", "date", "comment", "type", "value" }, // columns
                                "surveyId=? AND shotId=?", 
                                new String[] { Long.toString(sid), Long.toString(shotid) },
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
                                 cursor.getString(5), // shot_type
                                 cursor.getString(6) ) ); // value
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Sensors list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    String[] where = new String[2];
    where[0] = Long.toString(sid);
    for ( SensorInfo si : list ) { // set shot-names to the sensor infos
      where[1] = Long.toString( si.shotid );
      cursor = myDB.query( SHOT_TABLE, 
                           new String[] { "fStation", "tStation" },
                           WHERE_SID_ID, where, null, null, null );
      if (cursor.moveToFirst()) {
        si.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
      }
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    return list;
  }


  AudioInfo getAudio( long sid, long bid )
  {
    if ( myDB == null ) return null;
    AudioInfo ret = null;
    Cursor cursor = myDB.query( AUDIO_TABLE,
       		         new String[] { "id", "date" }, // columns
                                WHERE_SID_SHOTID, new String[] { Long.toString(sid), Long.toString(bid) },
                                null, null,  null ); 
    if (cursor.moveToFirst()) { // update
      ret = new AudioInfo( sid, 
                           cursor.getLong(0), // id
                           bid,
                           cursor.getString(1)  // date
                         );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  // negative id used for sketch audios
  // positive id used for blocks audios
  long nextAudioNegId( long sid )
  {
    return minId( AUDIO_TABLE, sid );
  }

  private long insertAudio( long sid, long id, long bid, String date )
  {
    if ( myDB == null ) return -1L;
    if ( id == -1L ) id = maxId( AUDIO_TABLE, sid );
    ContentValues cv = new ContentValues();
    cv.put( "surveyId", sid );
    cv.put( "id",       id );
    cv.put( "shotId",   bid );
    cv.put( "date",     date );
    if ( ! doInsert( AUDIO_TABLE, cv, "insert audio" ) ) return -1L;
    return id;
  }

  boolean setAudio( long sid, long bid, String date )
  {
    boolean ret = false;
    if ( myDB == null ) return false;
    String[] args = new String[] { Long.toString(sid), Long.toString(bid) };
    Cursor cursor = myDB.query( AUDIO_TABLE, new String[] { "date" }, WHERE_SID_SHOTID, args, null, null,  null ); 
    if (cursor.moveToFirst()) { // update
      String where = "WHERE surveyId=? AND shotId=?";
      ContentValues cv = new ContentValues();
      cv.put( "date", date );
      ret = ( myDB.update( AUDIO_TABLE, cv, WHERE_SID_SHOTID, args ) > 0 );
      // updateAudioStmt.bindString( 1, date );
      // updateAudioStmt.bindString( 2, Long.toString(sid) );
      // updateAudioStmt.bindString( 3, Long.toString(bid) );
      // ret = doStatement( updateAudioStmt, "audio update" );
    } else { // insert
      ret = ( insertAudio( sid, -1L, bid, date ) >= 0 );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  List< AudioInfo > selectAllAudios( long sid )
  {
    List< AudioInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query( AUDIO_TABLE,
       		                new String[] { "id", "shotId", "date" }, // columns
                                WHERE_SID, new String[] { Long.toString(sid) },
                                null, null,  null ); 
    if (cursor.moveToFirst()) {
      do {
        list.add( new AudioInfo( sid, 
                                 cursor.getLong(0), // id
                                 cursor.getLong(1), // shotId
                                 cursor.getString(2)  // date
                               ) );
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Photos list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  void deleteAudio( long sid, long bid )
  {
    if ( myDB == null ) return;
    try {
      myDB.delete( AUDIO_TABLE, WHERE_SID_SHOTID, new String[]{ Long.toString(sid), Long.toString(bid) } );
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("photo delete", e); }
  }

  List< PhotoInfo > selectAllPhotos( long sid, long status )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query( PHOTO_TABLE,
       		         new String[] { "id", "shotId", "title", "date", "comment" }, // columns
                                WHERE_SID_STATUS, new String[] { Long.toString(sid), Long.toString(status) },
                                null, null,  null ); 
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
    // TDLog.Log( TDLog.LOG_DB, "select All Photos list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

    String[] where = new String[2];
    where[0] = Long.toString(sid);
    for ( PhotoInfo pi : list ) { // fill in the shot-name of the photos
      where[1] = Long.toString( pi.shotid );
      cursor = myDB.query( SHOT_TABLE, 
                           new String[] { "fStation", "tStation" },
                           WHERE_SID_ID, where, null, null, null );
      if (cursor.moveToFirst()) {
        pi.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
      }
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    return list;
  }

  List< PhotoInfo > selectPhotoAtShot( long sid, long shotid )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query( PHOTO_TABLE,
                                new String[] { "id", "shotId", "title", "date", "comment" }, // columns
                                WHERE_SID_SHOTID, new String[] { Long.toString(sid), Long.toString(shotid) },
                                null, null, null ); 
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
    // TDLog.Log( TDLog.LOG_DB, "select All Photos list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

    String[] where = new String[2];
    where[0] = Long.toString(sid);
    for ( PhotoInfo pi : list ) { // fill in the shot-name of the photos
      where[1] = Long.toString( pi.shotid );
      cursor = myDB.query( SHOT_TABLE, new String[] { "fStation", "tStation" },
                           WHERE_SID_ID, where, null, null, null );
      if (cursor.moveToFirst()) {
        pi.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
      }
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    return list;
  }

  List< FixedInfo > selectAllFixed( long sid, int status )
  {
    List< FixedInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query( FIXED_TABLE,
                                mFixedFields,
                                WHERE_SID_STATUS, new String[] { Long.toString(sid), Long.toString(status) }, 
                                null, null, null );
    if (cursor.moveToFirst()) {
      do {
        list.add( new FixedInfo( cursor.getLong(0),
                                 cursor.getString(1), // station
                                 cursor.getDouble(2), // longitude
                                 cursor.getDouble(3), // latitude
                                 cursor.getDouble(4), // ellipsoid height
                                 cursor.getDouble(5), // geoid height
                                 cursor.getString(6),
                                 // skip status
                                 cursor.getLong(8),   // source type
                                 cursor.getString(9),
                                 cursor.getDouble(10),
                                 cursor.getDouble(11),
                                 cursor.getDouble(12),
				 cursor.getLong(13)
        ) );
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    // TDLog.Log( TDLog.LOG_DB, "select all fixeds " + sid + " size " + list.size() );
    return list;
  }

   private List< FixedInfo > selectFixedAtStation( long sid, String name )
   {
     List< FixedInfo > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query( FIXED_TABLE,
                                 mFixedFields,
                                 "surveyId=? and station=?",  // selection = WHERE clause (without "WHERE")
                                 new String[] { Long.toString(sid), name },     // selectionArgs
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
                                  cursor.getString(6),
                                  // skip status
                                  cursor.getLong(8),  // source
                                  cursor.getString(9),
                                  cursor.getDouble(10),
                                  cursor.getDouble(11),
                                  cursor.getDouble(12),
				  cursor.getLong(13)  // cs_decimals
         ) );
       } while (cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     // TDLog.Log( TDLog.LOG_DB, "select all fixeds " + sid + " size " + list.size() );
     return list;
   }

   boolean hasSurveyPlot( long sid, String name )
   {
     boolean ret = false;
     if ( myDB != null ) {
       Cursor cursor = myDB.query( PLOT_TABLE, new String[]{ "id", "name" },
           WHERE_SID_NAME, new String[]{ Long.toString( sid ), name },
           null, null, "id" );
       if (cursor.moveToFirst()) ret = true;
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return ret;
   }

   boolean hasSurveyStation( long sid, String start )
   {
     boolean ret = false;
     if ( myDB != null ) {
       Cursor cursor = myDB.query( SHOT_TABLE,
           new String[]{ "id", "fStation", "tStation" },
           "surveyId=? and ( fStation=? or tStation=? )",
           new String[]{ Long.toString( sid ), start, start },
           null, null, "id" );
       if (cursor.moveToFirst()) ret = true;
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return ret;
   }


   int maxPlotIndex( long sid )
   {
     int ret = 0;
     if ( myDB == null ) return ret;
     Cursor cursor = myDB.query(PLOT_TABLE, new String[] { "id", "name", "type" },
                                WHERE_SID, new String[] { Long.toString(sid) }, 
                                null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         int type = cursor.getInt(2);
         if ( type == PlotInfo.PLOT_PLAN ) { // FIXME || type == PlotInfo.PLOT_EXTENDED || type == PlotInfo.PLOT_PROFILE
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
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return ret;
   }

   private PlotInfo makePlotInfo( long sid, Cursor cursor )
   {
     PlotInfo plot = new  PlotInfo ();
     plot.surveyId = sid;
     plot.id      = cursor.getLong(0);
     plot.name    = cursor.getString(1);
     plot.type    = cursor.getInt(2);
     plot.start   = cursor.getString(3);
     plot.view    = cursor.getString(4);
     plot.xoffset = (float)(cursor.getDouble(5));
     plot.yoffset = (float)(cursor.getDouble(6));
     plot.zoom    = (float)(cursor.getDouble(7));
     plot.azimuth = (float)(cursor.getDouble(8));
     plot.clino   = (float)(cursor.getDouble(9));
     plot.hide    = cursor.getString(10);
     plot.nick    = cursor.getString(11);
     plot.orientation = (int)(cursor.getLong(12));
     return plot;
   }


   private List< PlotInfo > doSelectAllPlots( long sid, String where_str, String[] where )
   {
     List< PlotInfo > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query( PLOT_TABLE, mPlotFields, where_str, where, null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         list.add( makePlotInfo( sid, cursor ) );
       } while (cursor.moveToNext());
     }
     // TDLog.Log( TDLog.LOG_DB, "select All Plots list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   List< PlotInfo > selectAllPlots( long sid )
   {
     return doSelectAllPlots( sid, 
                              WHERE_SID,
                              new String[] { Long.toString(sid) }
     );
   }


   List< PlotInfo > selectAllPlots( long sid, long status )
   {
     return doSelectAllPlots( sid, 
                              WHERE_SID_STATUS,
                              new String[] { Long.toString(sid), Long.toString(status) }
     );
   }

   List< PlotInfo > selectAllPlotsWithType( long sid, long status, long type )
   {
     return doSelectAllPlots( sid, 
                              "surveyId=? and status=? and type=? ",
                              new String[] { Long.toString(sid), Long.toString(status), Long.toString(type) }
     );
   }


   List< PlotInfo > selectAllPlotsWithType( long sid, long status, long type, boolean landscape )
   {
     return doSelectAllPlots( sid, 
                              "surveyId=? and status=? and type=? and orientation=" + (landscape? 1 : 0),
                              new String[] { Long.toString(sid), Long.toString(status), Long.toString(type) }
     );
   }

   // NEW X_SECTIONS hide = parent plot
   // @param parent   parent plot name (null if shared xsections)
   //
   List< PlotInfo > selectAllPlotSectionsWithType( long sid, long status, long type, String parent )
   {
     if ( parent == null ) {
       return doSelectAllPlots( sid, 
                              "surveyId=? and status=? and type=?",
                              new String[] { Long.toString(sid), Long.toString(status), Long.toString(type) }
       );
     }
     return doSelectAllPlots( sid, 
                              "surveyId=? and status=? and type=? and hide=?",
                              new String[] { Long.toString(sid), Long.toString(status), Long.toString(type), parent }
     );
   }


   List< PlotInfo > selectAllPlotsSection( long sid, long status )
   {
     return doSelectAllPlots( sid, 
                              "surveyId=? and status=? and ( type=0 or type=3 or type=5 or type=7 )",
                              new String[] { Long.toString(sid), Long.toString(status) }
     );
   }

   private List< PlotInfo > selectPlotsAtStation( long sid, String name )
   {
     return doSelectAllPlots( sid, 
                              WHERE_SID_START,
                              new String[] { Long.toString(sid), name }
     );
   }


   boolean hasShot( long sid, String fStation, String tStation )
   {
     if ( myDB == null ) return false;
     Cursor cursor = myDB.query( SHOT_TABLE,
       new String[] { "fStation", "tStation" }, // columns
       "surveyId=? and ( ( fStation=? and tStation=? ) or ( fStation=? and tStation=? ) )", 
       new String[] { Long.toString(sid), fStation, tStation, tStation, fStation },
       null,   // groupBy
       null,   // having
       null ); // order by
     boolean ret = cursor.moveToFirst();
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return ret;
   }
     
   String nextStation( long sid, String fStation )
   {
     if ( myDB == null ) return null;
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
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return ret;
   }

   // mergeToNextLeg does not change anything if blk has both FROM and TO stations
   long mergeToNextLeg( DBlock blk, long sid, boolean forward )
   {
     long ret = -1;
     if ( myDB == null ) return ret;
     Cursor cursor = myDB.query( SHOT_TABLE, new String[] { "id", "fStation", "tStation" },
                                 WHERE_SID_ID_MORE, new String[] { Long.toString(sid), Long.toString(blk.mId) },
                                 null, null, "id ASC" ); 
     if (cursor.moveToFirst()) {
       for ( int k = 0; k < 3; ++ k ) {
         String from = cursor.getString(1);
         String to   = cursor.getString(2);
         if ( from.length() > 0 && to.length() > 0 ) {
           ret = cursor.getLong( 0 );
           // Log.v("DistoX", blk.mId + " < " + from + " - " + to + " > at k " + k );
           if ( k > 0 ) {
             // Log.v("DistoX", blk.mId + " clear shot name " + ret );
             updateShotName( ret, sid, "", "", forward );
             updateShotLeg( ret, sid, LegType.EXTRA, forward ); 
             if ( k == 2 ) { // N.B. if k == 2 must set ShotLeg also to intermediate shot
               if ( cursor.moveToPrevious() ) { // overcautious
                 updateShotLeg( cursor.getLong(0), sid, LegType.EXTRA, forward ); 
               }
             }
           }
           updateShotName( blk.mId, sid, from, to, forward );
           blk.mFrom = from;
           blk.mTo   = to;
           break;
         }
         if ( ! cursor.moveToNext() ) break;
       }
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return ret;
   }

   // public void getShotName( long sid, DBlock blk )
   // {
   //   Cursor cursor = myDB.query( SHOT_TABLE, new String[] { "fStation", "tStation" },
   //                               WHERE_SID_ID, new String[] { Long.toString(sid), Long.toString(blk.mId) },
   //                               null, null, null );
   //   if (cursor.moveToFirst()) {
   //     blk.mFrom = cursor.getString(0);
   //     blk.mTo   = cursor.getString(1);
   //   }
   //   if (cursor != null && !cursor.isClosed()) cursor.close();
   // }

   DBlock selectShot( long id, long sid )
   {
     // TDLog.Log( TDLog.LOG_DB, "selectShot " + id + "/" + sid );
     if ( myDB == null ) return null;
     Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
                                 WHERE_SID_ID, new String[] { Long.toString(sid), Long.toString(id) },
                                 null, null, null );
     DBlock block = null;
     if (cursor.moveToFirst()) {
       block = new DBlock();
       fillBlock( sid, block, cursor );
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     // Log.v("DistoXX", "A6 select one shot id " + id + " block leg " + block.getLegType() );
     return block;
   }

   DBlock selectLastLegShot( long sid )
   {
     return selectPreviousLegShot( myNextId+1, sid );
   }

   private DBlock selectPreviousLegShot( long shot_id, long sid )
   {
     // TDLog.Log( TDLog.LOG_DB, "select previous leg shot " + shot_id + "/" + sid );
     if ( myDB == null ) return null;
     Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
                                 "surveyId=? and id<?",
                                 new String[] { Long.toString(sid), Long.toString(shot_id) },
                                 null, null, "id DESC" );
     DBlock block = null;
     if (cursor.moveToFirst()) {
       do {
	 String str0 = cursor.getString(0);
	 String str1 = cursor.getString(1);
         if ( str0 != null && str1 != null && str0.length() > 0 && str1.length() > 0 ) {
           block = new DBlock();
           fillBlock( sid, block, cursor );
         }  
       } while (block == null && cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return block;
   }

   String getLastStationName( long sid )
   {
     if ( myDB == null ) return null;
     Cursor cursor = myDB.query(SHOT_TABLE,
       new String[] { "fStation", "tStation" },
       WHERE_SID,
       new String[] { Long.toString(sid) },
       null,  // groupBy
       null,  // having
       "id DESC" // order by
     );
     String ret = DistoXStationName.mInitialStation;
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
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return ret;
   }

   // FIXME this is ok only for numbers
   // String getNextStationName( long sid )
   // {
   //   if ( myDB == null ) return null;
   //   Cursor cursor = myDB.query(SHOT_TABLE,
   //     new String[] { "fStation", "tStation" },
   //      WHERE_SID,
   //     new String[] { Long.toString(sid) },
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
   //         TDLog.Error( "getNextStationName parseInt error: " + cursor.getString(0) + " " + cursor.getString(1) );
   //       }
   //     } while (cursor.moveToNext());
   //   }
   //   if (cursor != null && !cursor.isClosed()) cursor.close();
   //   ++ ret;
   //   return Integer.toString(ret);
   // }

   DBlock selectNextLegShot( long shot_id, long sid )
   {
     // TDLog.Log( TDLog.LOG_DB, "selectNextLegShot " + shot_id + "/" + sid );
     if ( myDB == null ) return null;
     Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
                                 "surveyId=? and id>?",
                                 new String[] { Long.toString(sid), Long.toString(shot_id) },
                                 null, null, "id ASC" );
     DBlock block = null;
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getString(0).length() > 0 && cursor.getString(1).length() > 0 ) {
           block = new DBlock();
           fillBlock( sid, block, cursor );
         }  
       } while (block == null && cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return block;
   }

   // private boolean hasShotAtStation( long id, long sid, String station )
   // {
   //   if ( myDB == null ) return false;
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
   //       TDLog.Log( TDLog.LOG_DB, "hasShotAtStation " + id + " " + idc ); 
   //       if ( id != idc ) {
   //         ret = true;
   //       }
   //     } while (ret == false && cursor.moveToNext());
   //   }
   //   if (cursor != null && !cursor.isClosed()) cursor.close();
   //   TDLog.Log( TDLog.LOG_DB, "hasShotAtStation returns " + ret );
   //   return ret;
   // }

   List<DBlock> selectShotsBetweenStations( long sid, String st1, String st2, long status )
   {
     List< DBlock > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     "surveyId=? and status=? and ( ( fStation=? and tStation=? ) or ( fStation=? and tStation=? ) )",
                     new String[] { Long.toString(sid), TDStatus.NORMAL_STR, st1, st2, st2, st1 },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         DBlock block = new DBlock();
         fillBlock( sid, block, cursor );
         list.add( block );
       } while (cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   List<DBlock> selectShotsAfterId( long sid, long id , long status )
   {
     // Log.v("DistoXX", "B1 select shots after id " + id );
     List< DBlock > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     "surveyId=? and status=? and id>?",
                     new String[] { Long.toString(sid), TDStatus.NORMAL_STR, Long.toString(id) },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         DBlock block = new DBlock();
         fillBlock( sid, block, cursor );
         list.add( block );
       } while (cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   // select shots (either legs or splays) at a station
   // in the case of legs, select only "independent" legs (one for each neighbor station)
   List<DBlock> selectShotsAt( long sid, String station, boolean leg )
   {
     List< DBlock > list = new ArrayList<>();
     if ( station == null || station.length() == 0 ) return list;
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     "surveyId=? and status=? and (fStation=? or tStation=?)",
                     new String[] { Long.toString(sid), TDStatus.NORMAL_STR, station, station },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         String fs = cursor.getString(1);
         int fl = fs.length();
         String ts = cursor.getString(2);
         int tl = ts.length();
         if ( leg ) { // legs only
           if ( fl > 0 && tl > 0 ) { // add block only if "independent"
             boolean independent = true;
             for ( DBlock blk : list ) {
               if (  ( fs.equals( blk.mFrom ) && ts.equals( blk.mTo   ) )
                  || ( fs.equals( blk.mTo   ) && ts.equals( blk.mFrom ) ) ) {
                 independent = false;
                 break;
               }
             }
             if ( independent ) {
               DBlock block = new DBlock();
               fillBlock( sid, block, cursor );
               list.add( block );
             }
           }
         } else { // splays only
           if ( ( fl > 0 && tl ==0 ) || ( fl == 0 && tl > 0 ) ) { 
             DBlock block = new DBlock();
             fillBlock( sid, block, cursor );
             list.add( block );
           }
         }
       } while (cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   List<DBlock> selectSplaysAt( long sid, String station, boolean leg )
   {
     List< DBlock > list = new ArrayList<>();
     if ( station == null || station.length() == 0 ) return list;
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     "surveyId=? and status=? and (fStation=? or tStation=?)",
                     new String[] { Long.toString(sid), TDStatus.NORMAL_STR, station, station },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         int fl = cursor.getString(1).length();
         int tl = cursor.getString(2).length();
         if ( !leg && ( ( fl > 0 && tl ==0 ) || ( fl == 0 && tl > 0 ) ) ) { // splay only
           DBlock block = new DBlock();
           fillBlock( sid, block, cursor );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   // called by DrawingWindow to splay select shots for x-sections
   List<DBlock> selectAllShotsAtStations( long sid, String station1, String station2 )
   {
     if ( station2 == null ) return selectAllShotsAtStation( sid, station1 );

     List< DBlock > list = new ArrayList<>();
     if ( station1 == null ) return list;

     if ( myDB == null ) return list;
     Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
       "surveyId=? and status=? and ( fStation=? or tStation=? or fStation=? or tStation=? )",
       new String[] { Long.toString(sid), TDStatus.NORMAL_STR, station1, station2, station2, station1 },
       null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getLong(11) == 0 ) { // skip leg-blocks (11 = "leg" flag)
           DBlock block = new DBlock();
           fillBlock( sid, block, cursor );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     // TDLog.Log( TDLog.LOG_DB, "select All Shots At Station list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   // @param sid        survey id
   // @param stations   stations names (must be unique)
   // @param with_leg   whether to include legs or not
   List<DBlock> selectAllShotsAtStations( long sid, List<String> stations, boolean with_legs )
   {
     List< DBlock > list = new ArrayList<>();
     if ( stations == null || myDB == null ) return list;
     int sz = stations.size();
     if ( sz == 0 ) return list;
     if ( sz == 1 ) return selectAllShotsAtStation( sid, stations.get(0) );
     if ( sz == 2 ) return selectAllShotsAtStations( sid, stations.get(0), stations.get(1) );

     for ( String station : stations ) {
       Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
         "surveyId=? and status=? and ( fStation=? or tStation=? )",
         new String[] { Long.toString(sid), TDStatus.NORMAL_STR, station, station },
         null, null, "id" );
       if (cursor.moveToFirst()) {
         do {
           if ( cursor.getLong(11) == 0 ) { // non-leg blocks
             DBlock block = new DBlock();
             fillBlock( sid, block, cursor );
             list.add( block );
           } else if ( with_legs ) { // leg blocks
	     long id = cursor.getLong(0);
	     boolean contains = false;
	     for ( DBlock b : list ) if ( b.mId == id ) { contains = true; break; }
	     if ( ! contains ) {
               DBlock block = new DBlock();
               fillBlock( sid, block, cursor );
               list.add( block );
	     }
	   }
         } while (cursor.moveToNext());
       }
       // TDLog.Log( TDLog.LOG_DB, "select All Shots At Station list size " + list.size() );
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return list;
   }

   private List<DBlock> selectAllShotsAtStation( long sid, String station )
   {
     List< DBlock > list = new ArrayList<>();
     if ( station == null ) return list;

     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     "surveyId=? and status=? and fStation=?", 
                     new String[] { Long.toString(sid), TDStatus.NORMAL_STR, station },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getLong(11) == 0 ) { // skip leg-blocks
           DBlock block = new DBlock();
           fillBlock( sid, block, cursor );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     // TDLog.Log( TDLog.LOG_DB, "select All Shots At Station list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   List<DBlock> selectAllShotsToStation( long sid, String station )
   {
     List< DBlock > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     "surveyId=? and status=? and tStation=?", 
                     new String[] { Long.toString(sid), TDStatus.NORMAL_STR, station },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getLong(11) == 0 ) { // skip leg-blocks
           DBlock block = new DBlock();
           fillBlock( sid, block, cursor );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     // TDLog.Log( TDLog.LOG_DB, "select All Shots To Station list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   // select all LEG stations before a shot
   Set<String> selectAllStationsBefore( long id, long sid, long status )
   {
     Set< String > set = new TreeSet<String>();
     if ( myDB == null ) return set;
     Cursor cursor = myDB.query(SHOT_TABLE, new String[] { "fStation", "tStation" },
                     "id<=? and surveyId=? and status=?",
                     new String[] { Long.toString(id), Long.toString(sid), Long.toString(status) },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         String f = cursor.getString( 0 );
         String t = cursor.getString( 1 );
         if ( f.length() > 0 && t.length() > 0 ) {
           set.add( f );
           set.add( t );
	 }
       } while (cursor.moveToNext());
     }
     // TDLog.Log( TDLog.LOG_DB, "select All Shots after " + id + " list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return set;
   }

   List<DBlock> selectAllShotsAfter( long id, long sid, long status )
   {
     // Log.v("DistoXX", "B2 select shots after id " + id );
     List< DBlock > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     "id>=? and surveyId=? and status=?",
                     new String[] { Long.toString(id), Long.toString(sid), Long.toString(status) },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         DBlock block = new DBlock();
         fillBlock( sid, block, cursor );
         list.add( block );
       } while (cursor.moveToNext());
     }
     // TDLog.Log( TDLog.LOG_DB, "select All Shots after " + id + " list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   // select all LEG stations
   Set<String> selectAllStations( long sid )
   {
     Set<String> set = new TreeSet<String>();
     if ( myDB == null ) return set;
     Cursor cursor = myDB.query(SHOT_TABLE, new String[] { "fStation", "tStation" },
                     WHERE_SID, new String[]{ Long.toString(sid) },
                     null, null, null );
     if (cursor.moveToFirst()) {
       do {
         String f = cursor.getString( 0 );
         String t = cursor.getString( 1 );
         if ( f.length() > 0 && t.length() > 0 ) {
           set.add( f );
           set.add( t );
	 }
       } while (cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return set;
   }

   List<DBlock> selectAllShots( long sid, long status )
   {
     // Log.v("DistoXX", "B3 select shots all");
     List< DBlock > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     WHERE_SID_STATUS, new String[]{ Long.toString(sid), Long.toString(status) },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         DBlock block = new DBlock();
         fillBlock( sid, block, cursor );
         list.add( block );
       } while (cursor.moveToNext());
     }
     // TDLog.Log( TDLog.LOG_DB, "select All Shots list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   // @param backshot  whether the DistoX is in backshot mode
   // @return the last block with either the from station (non-backshot) or the to station (backshot)
   DBlock selectLastNonBlankShot( long sid, long status, boolean backshot )
   {
     if ( myDB == null ) return null;
     DBlock ret = null;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     WHERE_SID_STATUS_LEG, new String[]{ Long.toString(sid), Long.toString(status) },
                     null, null, "id desc", "1" );
     if (cursor.moveToFirst()) {
       // Log.v("DistoX", "got the last leg " + cursor.getLong(0) + " " + cursor.getString(1) + " - " + cursor.getString(2) );
       DBlock block = new DBlock();
       do { 
         fillBlock( sid, block, cursor );
	 if ( backshot ) {
           if ( block.mTo != null && block.mTo.length() > 0 ) { ret = block; break; }
	 } else {
           if ( block.mFrom != null && block.mFrom.length() > 0 ) { ret = block; break; }
	 }
       } while (cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return ret;
   }

   List<DBlock> selectAllLegShots( long sid, long status )
   {
     // Log.v("DistoXX", "B4 select shots all leg");
     List< DBlock > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                     WHERE_SID_STATUS, new String[]{ Long.toString(sid), Long.toString(status) },
                     null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         if ( cursor.getString(1).length() > 0 && cursor.getString(2).length() > 0 ) {
           DBlock block = new DBlock();
           fillBlock( sid, block, cursor );
           list.add( block );
         }
       } while (cursor.moveToNext());
     }
     // TDLog.Log( TDLog.LOG_DB, "select All Shots list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   SurveyInfo selectSurveyInfo( long sid )
   {
     SurveyInfo info = null;
     if ( myDB == null ) return null;
     Cursor cursor = myDB.query( SURVEY_TABLE,
                                new String[] { "name", "day", "team", "declination", "comment", "init_station", "xsections" }, // columns
                                WHERE_ID, new String[] { Long.toString(sid) },
                                null, null, "name" );
     if (cursor.moveToFirst()) {
       info = new SurveyInfo();
       info.id      = sid;
       info.name    = cursor.getString( 0 );
       info.date    = cursor.getString( 1 );
       info.team    = cursor.getString( 2 );
       info.declination = (float)(cursor.getDouble( 3 ));
       info.comment = cursor.getString( 4 );
       info.initStation = cursor.getString( 5 );
       info.xsections = (int)cursor.getLong( 6 );
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return info;
   }
   // ----------------------------------------------------------------------
   // SELECT: LIST SURVEY / CABIL NAMES

   private List<String> selectAllNames( String table )
   {
     // TDLog.Log( TDLog.LOG_DB, "selectAllNames table " + table );

     List< String > list = new ArrayList<>();
     if ( myDB == null ) return list;
     try {
       Cursor cursor = myDB.query( table,
                                   new String[] { "name" }, // columns
                                   null, null, null, null, "name" );
       if (cursor.moveToFirst()) {
         do {
           list.add( cursor.getString(0) );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     } catch ( SQLException e ) {
       // ignore
     }
     // TDLog.Log( TDLog.LOG_DB, "found " + list.size() + " names " );
     return list;
   }

   List<String> selectAllSurveys() { return selectAllNames( SURVEY_TABLE ); }

   // ----------------------------------------------------------------------
   // CONFIG DATA

   String getValue( String key )
   {
     if ( myDB == null ) {
       TDLog.Error( "DataHelper::getValue null DB");
       return null;
     }
     if ( key == null || key.length() == 0 ) { // this is not an error
       return null;
     }
     String value = null;
     Cursor cursor = null;
     try {
       cursor = myDB.query( CONFIG_TABLE,
                            new String[] { "value" }, // columns
                            "key = ?", new String[] { key },
                            null, null, null );
       if ( cursor != null && cursor.moveToFirst() ) {
         value = cursor.getString( 0 );
       }
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } finally {
       if ( cursor != null && ! cursor.isClosed()) cursor.close();
     }
     return value;
   }

   void setValue( String key, String value )
   {
     if ( myDB == null ) {
       TDLog.Error( "DataHelper::setValue null DB");
       return;
     }
     if ( key == null || key.length() == 0 ) {
       TDLog.Error( "DataHelper::setValue null key");
       return;
     }
     if ( value == null || value.length() == 0 ) {
       TDLog.Error( "DataHelper::setValue null value");
       return;
     }

     Cursor cursor = myDB.query( CONFIG_TABLE,
                                new String[] { "value" }, // columns
                                "key = ?", new String[] { key },
                                null, null, null );
     if ( cursor != null ) {
       ContentValues cv = new ContentValues();
       cv.put( "value",   value );
       try {
         if (cursor.moveToFirst()) {
           // updateConfig.bindString( 1, value );
           // updateConfig.bindString( 2, key );
           try {
             // updateConfig.execute();
	     myDB.update( CONFIG_TABLE, cv, "key=?", new String[] { key } );
           } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
           } catch (SQLiteException e ) { logError( "config update " + key + " " + value, e ); }
         } else {
           cv.put( "key",     key );
           try {
             myDB.insert( CONFIG_TABLE, null, cv );
           } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
           } catch ( SQLiteException e ) { logError("config insert " + key + " " + value, e ); }
         }
       } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e ); }
       if ( ! cursor.isClosed()) cursor.close();
     }
   }

   // ----------------------------------------------------------------------
   // symbols

   void setSymbolEnabled( String name, boolean enabled ) { setValue( name, enabled? "1" : "0" ); }

   boolean getSymbolEnabled( String name )
   { 
     String enabled = getValue( name );
     if ( enabled != null ) {
       return enabled.equals("1");
     }
     return false;
   }

   void addSymbolEnabled( String name )
   {
     if ( myDB != null ) {
       ContentValues cv = new ContentValues();
       cv.put( "key",     name );
       cv.put( "value",   "0" );     // symbols are enabled by default
       try {
         myDB.insert( CONFIG_TABLE, null, cv );
       } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
       } catch ( SQLiteException e ) { logError("config symbol " + name, e ); }
     }
   }

   boolean hasSymbolName( String name ) { return ( getValue( name ) != null ); }

   // ----------------------------------------------------------------------
   /* Set the current survey/calib name.
    * If the survey/calib name does not exists a new record is inserted in the table
    */

   private String getNameFromId( String table, long id )
   {
     String ret = null;
     if ( myDB == null ) return null;
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
     if ( myDB == null ) { return -2; }
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
     if ( myDB == null ) { return 0; }
     Cursor cursor = myDB.query( table, new String[] { "id" },
                                 "name = ?", new String[] { name },
                                 null, null, null );
     if (cursor.moveToFirst() ) {
       id = cursor.getLong(0);
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     } else {
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
       // SELECT max(id) FROM table
       cursor = myDB.query( table, new String[] { "max(id)" },
                            null, null, null, null, null );
       if (cursor.moveToFirst() ) {
         id = 1 + cursor.getLong(0);
       } else {
         id = 1;
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
       // INSERT INTO table VALUES( id, name, "", "" )
       ContentValues cv = new ContentValues();
       cv.put( "id",      id );
       cv.put( "name",    name );
       cv.put( "day",     "" );
       cv.put( "comment", "" );
       doInsert( table, cv, "set name" );
     }
     return id;
   }

   // FIXME 'xx' is the prefix-name for sections
   private static final String prefix = "xx";
   private static final int prefix_length = 2; // prefix.length();

   String getNextSectionId( long sid )
   {
     int max = 0; 
     if ( myDB == null ) return "xxo"; // FIXME null
     // Log.v( TopoDroidApp.TAG, "getNextSectionId sid " + sid + " prefix " + prefix );
     Cursor cursor = myDB.query( PLOT_TABLE, 
                 new String[] { "id", "type", "name" },
                 WHERE_SID,
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
               TDLog.Error( "getNextSectionId parse Int error: survey ID " + sid );
             }
           }
         } while (cursor.moveToNext());
       }
       if (!cursor.isClosed()) cursor.close();
     }
     // return prefix + Integer.toString(max);
     return String.format(Locale.US, "%s%d", prefix, max );
   }

   boolean updatePlotName( long sid, long pid, String name )
   {
     if ( updatePlotNameStmt == null ) {
       updatePlotNameStmt = myDB.compileStatement( "UPDATE plots set name=? WHERE surveyId=? AND id=?" );
     }
     updatePlotNameStmt.bindString( 1, name );
     updatePlotNameStmt.bindLong( 2, sid );
     updatePlotNameStmt.bindLong( 3, pid );
     return doStatement( updatePlotNameStmt, "plot name" );
   }

   boolean updatePlotOrientation( long sid, long pid, int orient )
   {
     if ( updatePlotOrientationStmt == null ) {
       updatePlotOrientationStmt = myDB.compileStatement( "UPDATE plots set orientation=? WHERE surveyId=? AND id=?" );
     }
     updatePlotOrientationStmt.bindLong( 1, orient );
     updatePlotOrientationStmt.bindLong( 2, sid );
     updatePlotOrientationStmt.bindLong( 3, pid );
     return doStatement( updatePlotOrientationStmt, "plot orientation" );
   }

   boolean updatePlotAzimuthClino( long sid, long pid, float b, float c )
   {
     if ( updatePlotAzimuthClinoStmt == null ) {
       updatePlotAzimuthClinoStmt = myDB.compileStatement( "UPDATE plots set azimuth=?, clino=? WHERE surveyId=? AND id=?" );
     }
     updatePlotAzimuthClinoStmt.bindDouble( 1, b );
     updatePlotAzimuthClinoStmt.bindDouble( 2, c );
     updatePlotAzimuthClinoStmt.bindLong( 3, sid );
     updatePlotAzimuthClinoStmt.bindLong( 4, pid );
     return doStatement( updatePlotAzimuthClinoStmt, "plot azi+clino" );
   }
 
   PlotInfo getPlotInfo( long sid, String name )
   {
     PlotInfo plot = null;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( PLOT_TABLE, mPlotFields,
                 WHERE_SID_NAME,
                 new String[] { Long.toString(sid), name },
                 null, null, null );
       if (cursor != null ) {
         if (cursor.moveToFirst() ) plot = makePlotInfo( sid, cursor );
         if (!cursor.isClosed()) cursor.close();
       }
     }
     return plot;
   }
 
   // NEW X_SECTIONS
   // this is for at-station private x-sections
   // the name of the parent plot is stored in the "hide" field
   // public PlotInfo getPlotSectionInfo( long sid, String name, String parent )
   // {
   //   PlotInfo plot = null;
   //   if ( myDB != null && name != null ) {
   //     Cursor cursor = myDB.query( PLOT_TABLE, mPlotFields,
   //               "surveyId=? AND name=? AND hide=?",
   //               new String[] { Long.toString(sid), name, parent },
   //               null, null, null );
   //     if (cursor != null ) {
   //       if (cursor.moveToFirst() ) plot = makePlotInfo( sid, cursor );
   //       if (!cursor.isClosed()) cursor.close();
   //     }
   //   }
   //   return plot;
   // }

   long getPlotId( long sid, String name )
   {
     long ret = -1;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( PLOT_TABLE, new String[] { "id" },
                            WHERE_SID_NAME,
                            new String[] { Long.toString(sid), name },
                            null, null, null );
       if ( cursor != null ) {
         if (cursor.moveToFirst() ) ret = cursor.getLong(0);
         if ( !cursor.isClosed()) cursor.close();
       }
     }
     return ret;
   }

   // String getPlotFieldAsString( long sid, long pid, String field )
   // {
   //   String ret = null;
   //   if ( field == null ) return ret;
   //   if ( myDB == null ) return ret;
   //   Cursor cursor = myDB.query( PLOT_TABLE, new String[] { field },
   //                        WHERE_SID_ID,
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
    * @param comment   comment
    */
   long insertPhoto( long sid, long id, long shotid, String title, String date, String comment )
   {
     if ( myDB == null ) return -1L;
     if ( id == -1L ) id = maxId( PHOTO_TABLE, sid );
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "id",        id );
     cv.put( "shotId",    shotid );
     cv.put( "status",    TDStatus.NORMAL );
     cv.put( "title",     title );
     cv.put( "date",      date );
     cv.put( "comment",   (comment == null)? "" : comment );
     if ( ! doInsert( PHOTO_TABLE, cv, "photo insert" ) ) return -1L;
     return id;
   }

   long nextPhotoId( long sid )
   {
     return maxId( PHOTO_TABLE, sid );
   }

   boolean updatePhoto( long sid, long id, String comment )
   {
     if ( myDB == null ) return false;
     ContentValues vals = new ContentValues();
     vals.put( "comment", comment );
     myDB.beginTransaction();
     try {
       myDB.update( PHOTO_TABLE, vals, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
       myDB.setTransactionSuccessful();
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch (SQLiteException e) { logError("photo update", e); 
     } finally { myDB.endTransaction(); }
     return true;
   }

   void deletePhoto( long sid, long id )
   {
     if ( myDB == null ) return;
     myDB.beginTransaction();
     try {
       myDB.delete( PHOTO_TABLE, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
       myDB.setTransactionSuccessful();
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch (SQLiteException e) { logError("photo delete", e); 
     } finally { myDB.endTransaction(); }
   }

   /**
    * @param sid       survey id
    * @param id        photo id (or -1)
    * @param shotid    shot id
    * @param title     sensor title
    * @param date      sensor date
    * @param comment   comment
    * @param type      sensor type
    * @param value     sensor value
    */
   long insertSensor( long sid, long id, long shotid, String title, String date, String comment,
   String type, String value )
   {
     if ( id == -1L ) id = maxId( SENSOR_TABLE, sid );
     if ( myDB == null ) return -1L;
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "id",        id );
     cv.put( "shotId",    shotid );
     cv.put( "status",    TDStatus.NORMAL );
     cv.put( "title",     title );
     cv.put( "date",      date );
     cv.put( "comment",   (comment == null)? "" : comment );
     cv.put( "type",      type );
     cv.put( "value",     value );
     if ( ! doInsert( SENSOR_TABLE, cv, "sensor insert" ) ) return -1L;
     return id;
   }

   long nextSensorId( long sid )
   {
     return maxId( SENSOR_TABLE, sid );
   }

   void deleteSensor( long sid, long id )
   {
     if ( myDB == null ) return;
     updateStatus( SENSOR_TABLE, id, sid, TDStatus.DELETED );
   }

   boolean updateSensor( long sid, long id, String comment )
   {
     if ( myDB == null ) return false;
     ContentValues vals = new ContentValues();
     vals.put( "comment", comment );
     myDB.beginTransaction();
     try {
       myDB.update( SENSOR_TABLE, vals, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
       myDB.setTransactionSuccessful();
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch ( SQLiteException e ) { logError("sensor update", e); 
     } finally { myDB.endTransaction(); }
     return true;
   }

   // private void transferSensor( long sid, long shot_id, long old_sid, long old_id )
   // {
   //   if ( transferSensorStmt == null )
   //     transferSensorStmt = myDB.compileStatement( "UPDATE sensors set surveyId=?, shotId=? WHERE surveyId=? AND id=?" );
   //   transferSensorStmt.bindLong( 1, sid );
   //   transferSensorStmt.bindLong( 2, shot_id );
   //   transferSensorStmt.bindLong( 3, old_sid );
   //   transferSensorStmt.bindLong( 4, old_id );
   //   try { transferSensorStmt.execute(); } catch (SQLiteException e ) { logError("...", e);  }
   // }

   // private void transferPhoto( long sid, long shot_id, long old_sid, long old_id )
   // {
   //   if ( transferPhotoStmt == null )
   //     transferPhotoStmt = myDB.compileStatement( "UPDATE photos set surveyId=?, shotId=? WHERE surveyId=? AND id=?" );
   //   transferPhotoStmt.bindLong( 1, sid );
   //   transferPhotoStmt.bindLong( 2, shot_id );
   //   transferPhotoStmt.bindLong( 3, old_sid );
   //   transferPhotoStmt.bindLong( 4, old_id );
   //   try { transferPhotoStmt.execute(); } catch (SQLiteException e ) { logError("...", e);  }
   // }

   // private void transferFixed( long sid, long old_sid, long fixed_id )
   // {
   //   if ( transferFixedStmt == null )
   //     transferFixedStmt = myDB.compileStatement( "UPDATE fixeds set surveyId=? WHERE surveyId=? AND id=?" );
   //   transferFixedStmt.bindLong( 1, sid );
   //   transferFixedStmt.bindLong( 2, old_sid );
   //   transferFixedStmt.bindLong( 3, fixed_id );
   //   try { transferFixedStmt.execute(); } catch (SQLiteException e ) { logError("...", e);  }
   // }

   // private void transferStation( long sid, long old_sid, String name )
   // {
   //   if ( transferStationStmt == null )
   //     transferStationStmt = myDB.compileStatement( "UPDATE stations set surveyId=? WHERE surveyId=? AND name=?" );
   //   transferStationStmt.bindLong( 1, sid );
   //   transferStationStmt.bindLong( 2, old_sid );
   //   transferStationStmt.bindString( 3, name );
   //   try { transferStationStmt.execute(); } catch (SQLiteException e ) { logError("...", e);  }
   // }
       
   private void transferPlot( long sid, long old_sid, long pid )
   {
     if ( transferPlotStmt == null ) {
       transferPlotStmt = myDB.compileStatement( "UPDATE plots set surveyId=? WHERE surveyId=? AND id=?" );
     }
     transferPlotStmt.bindLong( 1, sid );
     transferPlotStmt.bindLong( 2, old_sid );
     transferPlotStmt.bindLong( 3, pid );
     try {
       transferPlotStmt.execute();
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch (SQLiteException e ) { logError("plot transf", e); }
   }

   private void transferSketch( long sid, long old_sid, long pid )
   {
     if ( transferSketchStmt == null ) {
       transferSketchStmt = myDB.compileStatement( "UPDATE sketches set surveyId=? WHERE surveyId=? AND id=?" );
     }
     transferSketchStmt.bindLong( 1, sid );
     transferSketchStmt.bindLong( 2, old_sid );
     transferSketchStmt.bindLong( 3, pid );
     try {
       transferSketchStmt.execute();
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch (SQLiteException e ) { logError("sketch transf", e); }
   }


   boolean hasFixed( long sid, String station )
   {
     return ( getFixedId( sid, station ) != -1 );
   }
   
   /** N.B. only one location per station
    *       Before inserting a location drop existing deleted fixeds for the station
    * N.B. this must be called with id == -1L ( currently called only by SurveyWindow )
    */
   long insertFixed( long sid, long id, String station, double lng, double lat, double alt, double asl,
                            String comment, long status, long source )
   {
     return insertFixed( sid, id, station, lng, lat, alt, asl, comment, status, source, "", 0, 0, 0, 2 );
   }

   private long insertFixed( long sid, long id, String station, double lng, double lat, double alt, double asl,
                            String comment, long status, long source,
                            String cs, double cs_lng, double cs_lat, double cs_alt, long cs_n_dec )
   {
     // Log.v("DistoX", "insert fixed id " + id + " station " + station );
     if ( id != -1L ) return id;
     if ( myDB == null ) return -1L;
     long fid = getFixedId( sid, station );
     if ( fid != -1L ) return fid;     // check non-deleted fixeds
     dropDeletedFixed( sid, station ); // drop deleted fixed if any

     id = maxId( FIXED_TABLE, sid );
     // TDLog.Log( TDLog.LOG_DB, "insert Fixed id " + id );
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
     cv.put( "cs_name",   cs );
     cv.put( "cs_longitude", cs_lng );
     cv.put( "cs_latitude",  cs_lat );
     cv.put( "cs_altitude",  cs_alt );
     cv.put( "source",     source );
     cv.put( "cs_decimals", cs_n_dec );
     if ( ! doInsert( FIXED_TABLE, cv, "insert fixed" ) ) return -1L;
     return id;
   }

   long insertPlot( long sid, long id, String name, long type, long status, String start, String view,
                           double xoffset, double yoffset, double zoom, double azimuth, double clino,
                           String hide, String nick, int orientation, boolean forward )
   {
     // Log.v( TopoDroidApp.TAG, "insert plot " + name + " start " + start + " azimuth " + azimuth );
     // Log.v("DistoXX", "insert plot <" + name + "> hide <" + hide + "> nick <" + nick + ">" );
     if ( myDB == null ) return -1L;
     long ret = getPlotId( sid, name );
     if ( ret >= 0 ) return -1;
     if ( view == null ) view = "";
     if ( id == -1L ) id = maxId( PLOT_TABLE, sid );
     ContentValues cv = new ContentValues();
     cv.put( "surveyId", sid );
     cv.put( "id",       id );
     cv.put( "name",     name );
     cv.put( "type",     type );
     cv.put( "status",   status );
     cv.put( "start",    start );
     cv.put( "view",     view );
     cv.put( "xoffset",  xoffset );
     cv.put( "yoffset",  yoffset );
     cv.put( "zoom",     zoom );
     cv.put( "azimuth",  azimuth );
     cv.put( "clino",    clino );
     cv.put( "hide",     hide );
     cv.put( "nick",     nick );
     cv.put( "orientation", orientation );
     if ( doInsert( PLOT_TABLE, cv, "plot insert" ) ) {
       if ( forward && mListeners != null ) { // synchronized( mListeners )
         mListeners.onInsertPlot( sid, id, name, type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation );
       }
     } else { // failed
       id = -1L;
     }
     return id;
   }

  private long maxId( String table, long sid )
  {
    long id = 1;
    if ( myDB == null ) return 1L;
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

  private long minId( String table, long sid )
  {
    if ( myDB == null ) return -2L;
    long id = -1L;
    Cursor cursor = myDB.query( table, new String[] { "min(id)" },
                         "surveyId=?", 
                         new String[] { Long.toString(sid) },
                         null, null, null );
    if (cursor != null ) {
      if (cursor.moveToFirst() ) {
        if ( cursor.getLong(0) < id ) id = cursor.getLong(0);
      }
      if (!cursor.isClosed()) cursor.close();
    }
    return id - 1L;
  }

  long getLastShotId( long sid )
  {
    return maxId( SHOT_TABLE, sid );
  }

  /** check if there is already a fixed record for the given station
   * @param id       do not consider record with this fixed ID
   * @param sid      survey ID
   * @param station  station name
   * @return true if found a record, false otherwise
   */  
  private boolean hasFixedStation( long id, long sid, String station )
  {
    boolean ret = false;
    if ( myDB != null ) {
      Cursor cursor = myDB.query( FIXED_TABLE,
          new String[]{ "id" },
          "surveyId=? and station=? and status=0",  // 0 == TDStatus.NORMAL
          new String[]{ Long.toString( sid ), station },
          null, null, null );
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          do {
            if (cursor.getLong( 0 ) != id) ret = true;
          } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) cursor.close();
      }
    }
    return ret;
  }
  
  /** get the ID of the fixed for the given station
   * @param sid          survey ID
   * @param station      station
   * @return fixed ID or -1L
   * note only non-deleted fixed are considered
   */
  private long getFixedId( long sid, String station )
  {
    long ret = -1L;
    if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( FIXED_TABLE, 
                            new String[] { "id" },
                            "surveyId=? and station=? and status=0",  // 0 == TDStatus.NORMAL
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
    myDB.beginTransaction();
    try {
      myDB.delete( FIXED_TABLE, "surveyId=? and station=? and status=1", new String[]{ Long.toString(sid), station } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("fixed delete", e ); 
    } finally { myDB.endTransaction(); }
  }
  

  // FIXME DBCHECK
  boolean updateFixedStation( long id, long sid, String station )
  {
    // Log.v("DistoX", "update fixed id " + id + " station " + station );
    boolean ret = false;
    if ( ! hasFixedStation( id, sid, station ) ) {
      dropDeletedFixed( sid, station ); // drop deleted fixed at station, if any

      ContentValues vals = new ContentValues();
      vals.put( "station", station );
      ret = doUpdate( FIXED_TABLE, vals, sid, id, "fixed update" );
    }
    return ret;
  }

  void updateFixedStatus( long id, long sid, long status )
  {
    updateStatus( FIXED_TABLE, id, sid, status );
  }

  void updateFixedStationComment( long id, long sid, String station, String comment )
  {
    ContentValues vals = new ContentValues();
    vals.put( "station", station );
    vals.put( "comment", comment );
    doUpdate( FIXED_TABLE, vals, sid, id, "fixed cmt" );
  }

  void updateFixedAltitude( long id, long sid, double alt, double asl )
  {
    if ( myDB == null ) return;
    ContentValues vals = new ContentValues();
    vals.put( "altitude",   alt );
    vals.put( "altimetric", asl );
    doUpdate( FIXED_TABLE, vals, sid, id, "fixed alt" );
  }

  void updateFixedData( long id, long sid, double lng, double lat, double alt )
  {
    if ( myDB == null ) return;
    ContentValues vals = new ContentValues();
    vals.put( "longitude", lng );
    vals.put( "latitude",  lat );
    vals.put( "altitude",  alt );
    doUpdate( FIXED_TABLE, vals, sid, id, "fixed data" );
  }

  void updateFixedData( long id, long sid, double lng, double lat, double alt, double asl )
  {
    if ( myDB == null ) return;
    ContentValues vals = new ContentValues();
    vals.put( "longitude", lng );
    vals.put( "latitude",  lat );
    vals.put( "altitude",  alt );
    vals.put( "altimetric", asl );
    doUpdate( FIXED_TABLE, vals, sid, id, "fixed data" );
  }

  void updateFixedCS( long id, long sid, String cs, double lng, double lat, double alt, long n_dec )
  {
    if ( myDB == null ) return;
    ContentValues vals = new ContentValues();
    if ( cs != null && cs.length() > 0 ) {
      vals.put( "cs_name", cs );
      vals.put( "cs_longitude", lng );
      vals.put( "cs_latitude",  lat );
      vals.put( "cs_altitude",  alt );
      vals.put( "cs_decimals",  n_dec );
    } else {
      vals.put( "cs_name", "" );
      vals.put( "cs_longitude", 0 );
      vals.put( "cs_latitude",  0 );
      vals.put( "cs_altitude",  0 );
      vals.put( "cs_decimals",  2 );
    }
    doUpdate( FIXED_TABLE, vals, sid, id, "fixed cs" );
  }

  boolean hasSurveyName( String name )  { return hasName( name, SURVEY_TABLE ); }
  // boolean hasCalibName( String name )  { return hasName( name, CALIB_TABLE ); }

  private boolean hasName( String name, String table )
  {
    boolean ret = false;
    if ( myDB != null ) {
      Cursor cursor = myDB.query( table, new String[]{ "id" },
          "name=?",
          new String[]{ name },
          null, null, null );
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          ret = true;
        }
        if (!cursor.isClosed()) cursor.close();
      }
    }
    return ret;
  }

   long setSurvey( String name, boolean forward )
   {
     myNextId = 0;
     if ( myDB == null ) return 0L;
     long sid = setName( SURVEY_TABLE, name );
     Cursor cursor = myDB.query( SHOT_TABLE, new String[] { "max(id)" },
                          "surveyId=?", new String[] { Long.toString(sid) },
                          null, null, null );
     if (cursor.moveToFirst() ) {
       myNextId = cursor.getLong(0);
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

     // TDLog.Log( TDLog.LOG_DB, "setSurvey " + name + " forward " + forward + " listeners " + mListeners.size() );
     if ( forward && mListeners != null ) { // synchronized( mListeners )
       mListeners.onSetSurvey( sid, name );
     }
     return sid;
   }

   String getSurveyFromId( long sid ) { return getNameFromId( SURVEY_TABLE, sid ); }

   String getSurveyDate( long sid ) { return getSurveyFieldAsString( sid, "day" ); }

   String getSurveyComment( long sid ) { return getSurveyFieldAsString( sid, "comment" ); }

   String getSurveyTeam( long sid ) { return getSurveyFieldAsString( sid, "team" ); }

   private String getSurveyFieldAsString( long sid, String attr )
   {
     String ret = null;
     if ( myDB != null ) {
       Cursor cursor = myDB.query( SURVEY_TABLE, new String[]{ attr },
           "id=?", new String[]{ Long.toString( sid ) },
           null, null, null );
       if (cursor.moveToFirst()) {
         ret = cursor.getString( 0 );
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return ret;
   }

// -------------------------------------------------------------------------------
// SKETCH_3D
/* FIXME BEGIN SKETCH_3D */
  boolean updateSketch( long sketch_id, long sid,
                            String st1, String st2,
                            double xofftop, double yofftop, double zoomtop,
                            double xoffside, double yoffside, double zoomside,
                            double xoff3d, double yoff3d, double zoom3d,
                            double east, double south, double vert, double azimuth, double clino )
  {
    if ( myDB == null ) return false;
    if ( updateSketchStmt == null ) {
      updateSketchStmt = myDB.compileStatement( "UPDATE sketches set st1=?, st2=?, xoffsettop=?, yoffsettop=?, zoomtop=?, xoffsetside=?, yoffsetside=?, zoomside=?, xoffset3d=?, yoffset3d=?, zoom3d=?, east=?, south=?, vert=?, azimuth=?, clino=? WHERE surveyId=? AND id=?" );
    }
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
    updateSketchStmt.bindLong( 17, sid );
    updateSketchStmt.bindLong( 18, sketch_id );
    return doStatement( updateSketchStmt, "sketch" );
  }
  
  void deleteSketch( long sketch_id, long sid )
  {
    if ( myDB == null ) return;
    updateStatus( SKETCH_TABLE, sketch_id, sid, TDStatus.DELETED );
    // if ( deleteSketchStmt == null )
    //   deleteSketchStmt = myDB.compileStatement( "UPDATE sketches set status=1 WHERE surveyId=? AND id=?" );
    // deleteSketchStmt.bindLong( 1, sid );
    // deleteSketchStmt.bindLong( 2, sketch_id );
    // try { deleteSketchStmt.execute(); } catch (SQLiteException e ) { logError("sketch", e); }
  }

   private List< Sketch3dInfo > doSelectAllSketches( long sid, String where_str, String[] where )
   {
     List< Sketch3dInfo > list = new ArrayList<>();
     if ( myDB == null ) return list;
     Cursor cursor = myDB.query( SKETCH_TABLE, mSketchFields,
                                 where_str, where,
                                 null, null, "id" );
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
     // TDLog.Log( TDLog.LOG_DB, "select All Sketch list size " + list.size() );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return list;
   }

   List< Sketch3dInfo > selectAllSketches( long sid )
   {
     return doSelectAllSketches( sid, 
                                 WHERE_SID,
                                 new String[] { Long.toString(sid) } 
     );
   }

   List< Sketch3dInfo > selectAllSketches( long sid, long status )
   {
     return doSelectAllSketches( sid, 
                                 WHERE_SID_STATUS,
                                 new String[] { Long.toString(sid), Long.toString(status) }
     );
   }

   private List< Sketch3dInfo > selectSketchesAtStation( long sid, String name )
   {
     return doSelectAllSketches( sid, 
                                 WHERE_SID_START,
                                 new String[] { Long.toString(sid), name }
     );
   }
 
   Sketch3dInfo getSketch3dInfo( long sid, String name )
   {
     Sketch3dInfo sketch = null;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( SKETCH_TABLE, 
                 new String[] { "id", "start", "st1", "st2", "xoffsettop", "yoffsettop", "zoomtop", "xoffsetside", "yoffsetside", "zoomside", "xoffset3d", "yoffset3d", "zoom3d", "east", "south", "vert", "azimuth", "clino" },
                 WHERE_SID_NAME,
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
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return sketch;
   }

   private long getSketch3dId( long sid, String name )
   {
     long ret = -1;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( SKETCH_TABLE, new String[] { "id" },
                            WHERE_SID_NAME,
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

   boolean hasSketch3d( long sid, String name )
   {
     boolean ret = false;
     if ( myDB != null && name != null ) {
       Cursor cursor = myDB.query( SKETCH_TABLE, new String[] { "id" },
                            WHERE_SID_NAME,
                            new String[] { Long.toString(sid), name },
                            null, null, null );
       if ( cursor != null ) {
         ret = (cursor.moveToFirst() );
         if ( !cursor.isClosed()) cursor.close();
       }
     }
     return ret;
   }


   long insertSketch3d( long sid, long id, String name, long status, String start, String st1, String st2,
                           double xoffsettop, double yoffsettop, double zoomtop,
                           double xoffsetside, double yoffsetside, double zoomside,
                           double xoffset3d, double yoffset3d, double zoom3d,
                           double x, double y, double z, double azimuth, double clino )
   {
     if ( myDB == null ) return -1L;
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
     if ( ! doInsert( SKETCH_TABLE, cv, "sketch insert" ) ) return -1L;
     return id;
   }
/* END SKETCH_3D */
      
   // ----------------------------------------------------------------------
   // SERIALIZATION of surveys TO FILE
   // the following tables are serialized (besides the survey recond)
   // 15 SHOTS    { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll", "acceleration", "magnetic", "dip",
   //                                             "extend", "flag", "leg", "status", "comment" },
   // 10 PLOTS    { "id", "name", "type", "status", "start", "view", "xoffset", "yoffset", "zoom", "azimuth" },
   //    SKETCHES
   //  6 PHOTOS   { "id", "shotId", "status", "title", "date", "comment" },
   //  8 SENSORS  { "id", "shotId", "status", "title", "date", "comment", "type", "value" },
   // 12 FIXEDS   { "id", "station", "longitude", "latitude", "altitude", "altimetric", "comment", "status",
   //               "cs_name", "cs_longitude", "cs_latitude", "cs_altitude" }

   void dumpToFile( String filename, long sid )
   {
     // TDLog.Log( TDLog.LOG_DB, "dumpToFile " + filename );
     // String where = "surveyId=" + Long.toString(sid);
     if ( myDB == null ) return;
     try {
       TDPath.checkPath( filename );
       FileWriter fw = new FileWriter( filename );
       PrintWriter pw = new PrintWriter( fw );
       Cursor cursor = myDB.query( SURVEY_TABLE, 
                            new String[] { "name", "day", "team", "declination", "comment", "init_station", "xsections" },
                            "id=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, \"%s\", \"%s\", \"%s\", %.4f, \"%s\", \"%s\", %d );\n",
                     SURVEY_TABLE,
                     sid,
                     cursor.getString(0),
                     cursor.getString(1),
                     cursor.getString(2),
                     cursor.getDouble(3),     // declination
                     cursor.getString(4),     // comment
                     cursor.getString(5),     // init_station
                     (int)cursor.getLong(6)   // xstation
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( AUDIO_TABLE, // SELECT ALL AUDIO RECORD
                            new String[] { "id", "shotId", "date" },
                            "surveyId=?", new String[] { Long.toString(sid) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, %d, \"%s\" );\n",
                     AUDIO_TABLE,
                     sid,
                     cursor.getLong(0),   // id
                     cursor.getLong(1),   // shotid
                     cursor.getString(2) ); // date
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( PHOTO_TABLE, // SELECT ALL PHOTO RECORD
  			    new String[] { "id", "shotId", "status", "title", "date", "comment" },
                            "surveyId=?", new String[] { Long.toString(sid) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, %d, %d, \"%s\", \"%s\", \"%s\" );\n",
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
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( PLOT_TABLE, 
                            mPlotFieldsFull,
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
             "INSERT into %s values( %d, %d, \"%s\", %d, %d, \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %.2f, \"%s\", \"%s\" );\n",
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
             cursor.getDouble(10),
             cursor.getString(11),
             cursor.getString(12)
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
       cursor = myDB.query( SKETCH_TABLE, 
                            new String[] { "id", "name", "status", "start", "st1", "st2", "xoffsettop", "yoffsettop", "zoomtop", "xoffsetside", "yoffsetside", "zoomside", "xoffset3d", "yoffset3d", "zoom3d", "east", "south", "vert", "azimuth", "clino" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
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
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( SHOT_TABLE, 
                            new String[] { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll",
                                           "acceleration", "magnetic", "dip",
                                           "extend", "flag", "leg", "status", "comment", "type", "millis", "color" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %.2f %.2f %.2f, %d, %d, %d, %d, \"%s\", %d, %d, %d );\n",
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
                     cursor.getLong(15),    // type
                     cursor.getLong(16),    // millis
                     0 // cursor.getLong(17) // COLOR is not exported
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( FIXED_TABLE, 
                  mFixedFields,
                  "surveyId=?", new String[] { Long.toString( sid ) },
                  null, null, null );
       if (cursor.moveToFirst()) {
         do { // values in the order of the fields of the table
           pw.format(Locale.US,
             "INSERT into %s values( %d, %d, \"%s\", %.6f, %.6f, %.2f, %.2f \"%s\", %d, \"%s\", %.6f, %.6f, %.1f, %d, %d );\n",
             FIXED_TABLE,
             sid,
             cursor.getLong(0),
             cursor.getString(1),
             cursor.getDouble(2),
             cursor.getDouble(3),
             cursor.getDouble(4),
             cursor.getDouble(5),
             cursor.getString(6),
             cursor.getLong(7),   // status
             cursor.getString(9), // cs_name
             cursor.getDouble(10),
             cursor.getDouble(11),
             cursor.getDouble(12),
             cursor.getLong(8),   // source type
             cursor.getLong(13)   // cs decimals
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( STATION_TABLE, 
                            new String[] { "name", "comment", "flag" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           // STATION_TABLE does not have field "id"
           pw.format(Locale.US,
             "INSERT into %s values( %d, 0, \"%s\", \"%s\", %d );\n",
             STATION_TABLE,
             sid, 
             cursor.getString(0),
             cursor.getString(1),
             cursor.getLong(2) );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( SENSOR_TABLE, 
                            new String[] { "id", "shotId", "status", "title", "date", "comment", "type", "value" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, %d, %d, \"%s\", \"%s\", \"%s\", \"%s\", \"%s\" );\n",
                     SENSOR_TABLE,
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
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       fw.flush();
       fw.close();
     } catch ( FileNotFoundException e ) {
       // FIXME
     } catch ( IOException e ) {
       // FIXME
     }
   }

   /** load survey data from a sql file
    * @param filename  name of the sql file
    */
   long loadFromFile( String filename, int db_version )
   {
     boolean success = true; // whether the load is successful
     long sid = -1;
     long id, status, shotid;
     String station, title, date, name, comment;
     String line;
     try {
       FileReader fr = new FileReader( filename );
       BufferedReader br = new BufferedReader( fr );
       // first line is survey
       line = br.readLine();
       // TDLog.Log( TDLog.LOG_DB, "loadFromFile: " + line );
       String[] vals = line.split(" ", 4); 
       // if ( vals.length != 4 ) { TODO } // FIXME
       String table = vals[2];
       String v = vals[3];
       Scanline scanline0 = new Scanline( v, v.indexOf('(')+1, v.lastIndexOf(')') );
       // pos = v.indexOf( '(' ) + 1;
       // len = v.lastIndexOf( ')' );
       // scanline0.skipSpaces( );
       if ( table.equals(SURVEY_TABLE) ) { 
         long skip_sid = scanline0.longValue( );
         name          = scanline0.stringValue( );
         String day    = scanline0.stringValue( );
         String team   = scanline0.stringValue( );
         double decl   = 0; if ( db_version > 14 ) scanline0.doubleValue( );
         comment       = scanline0.stringValue( );
         String init_station = "0"; if ( db_version > 22) init_station = scanline0.stringValue( );
         int xsections = SurveyInfo.XSECTION_SHARED; // old at-sationx-sections were "shared"
         if ( db_version > 29) xsections = (int)( scanline0.longValue( ) );

         sid = setSurvey( name, false );
         success &= updateSurveyInfo( sid, day, team, decl, comment, init_station, xsections, false );

         while ( (line = br.readLine()) != null ) {
           TDLog.Log( TDLog.LOG_DB, "loadFromFile: " + line );
           vals = line.split(" ", 4);
           table = vals[2];
           v = vals[3];
           Scanline scanline1 = new Scanline( v, v.indexOf('(')+1, v.lastIndexOf(')') );
           // pos = v.indexOf( '(' ) + 1;
           // len = v.lastIndexOf( ')' );
           // scanline1.skipSpaces( );
           // TDLog.Log( TDLog.LOG_DB, "loafFromFile " + table + " " + v );

           skip_sid = scanline1.longValue( );
           id = scanline1.longValue( );
           // Log.v("DistoX", "table " + table + " id " + id + " v " + v );

           if ( table.equals(AUDIO_TABLE) ) { // FIXME AUDIO
             shotid  = scanline1.longValue( );
             date    = scanline1.stringValue( );
             if ( shotid >= 0 ) {
               if ( insertAudio( sid, id, shotid, date ) < 0 ) { success = false; }
               // TDLog.Log( TDLog.LOG_DB, "loadFromFile photo " + sid + " " + id + " " + title + " " + name );
             }

           } else if ( table.equals(SENSOR_TABLE) ) { // FIXME SENSORS
             // "id", "shotId", "status", "title", "date", "comment", "type", "value" 
             id      = scanline1.longValue( );
             shotid  = scanline1.longValue( );
             status  = scanline1.longValue( );
             title   = scanline1.stringValue( );
             date    = scanline1.stringValue( );
             comment = scanline1.stringValue( );
             String type  = scanline1.stringValue( );
             String value = scanline1.stringValue( );
             if ( shotid >= 0 ) {
               if ( insertSensor( sid, id, shotid, title, date, comment, type, value ) >= 0 ) {
                 success &= updateStatus( SENSOR_TABLE, id, sid, status );
	       } else {
		 success = false;
	       }
               // TDLog.Log( TDLog.LOG_DB, "loadFromFile photo " + sid + " " + id + " " + title + " " + name );
             }

           } else if ( table.equals(PHOTO_TABLE) ) { // FIXME PHOTO
             shotid  = scanline1.longValue( );
             title   = scanline1.stringValue( );
             date    = scanline1.stringValue( );
             comment = scanline1.stringValue( );
             if ( shotid >= 0 ) {
               if ( insertPhoto( sid, id, shotid, title, date, comment ) < 0 ) { success = false; }
               // TDLog.Log( TDLog.LOG_DB, "loadFromFile photo " + sid + " " + id + " " + title + " " + name );
             }
           } else if ( table.equals(PLOT_TABLE) ) { // ***** PLOTS
             name         = scanline1.stringValue( );
             long type    = scanline1.longValue( ); if ( db_version <= 20 ) if ( type == 3 ) type = 5;
             status       = scanline1.longValue( );
             String start = scanline1.stringValue( );
             String view  = scanline1.stringValue( );
             double xoffset = scanline1.doubleValue( );
             double yoffset = scanline1.doubleValue( );
             double zoom  = scanline1.doubleValue( );
             double azimuth = scanline1.doubleValue( );
             double clino = ( db_version > 20 )? scanline1.doubleValue( ) : 0;
             String hide  = ( db_version > 24 )? scanline1.stringValue( ) : "";
             String nick  = ( db_version > 30 )? scanline1.stringValue( ) : "";
	     int orientation = (db_version > 32 )? (int)(scanline1.longValue()) : 0; // default PlotInfo.ORIENTATION_PORTRAIT
             if ( insertPlot( sid, id, name, type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation, false ) < 0 ) { success = false; }
             // TDLog.Log( TDLog.LOG_DB, "loadFromFile plot " + sid + " " + id + " " + start + " " + name );
   
/* FIXME BEGIN SKETCH_3D */
           } else if ( table.equals(SKETCH_TABLE) ) { // ***** SKETCHES
             name         = scanline1.stringValue( );
             status       = scanline1.longValue( );
             String start = scanline1.stringValue( );
             String st1   = scanline1.stringValue( );
             String st2   = scanline1.stringValue( );
             double xofft  = scanline1.doubleValue( );
             double yofft  = scanline1.doubleValue( );
             double zoomt  = scanline1.doubleValue( );
             double xoffs  = scanline1.doubleValue( );
             double yoffs  = scanline1.doubleValue( );
             double zooms  = scanline1.doubleValue( );
             double xoff3  = scanline1.doubleValue( );
             double yoff3  = scanline1.doubleValue( );
             double zoom3  = scanline1.doubleValue( );
             double east   = scanline1.doubleValue( );
             double south  = scanline1.doubleValue( );
             double vert   = scanline1.doubleValue( );
             double azimuth= scanline1.doubleValue( );
             double clino  = scanline1.doubleValue( );
             if ( insertSketch3d( sid, id, name, status, start, st1, st2, xofft, yofft, zoomt, xoffs, yoffs, zooms, xoff3, yoff3, zoom3, east, south, vert, azimuth, clino ) < 0 ) { success = false; }
/* END SKETCH_3D */
           } else if ( table.equals(SHOT_TABLE) ) { // ***** SHOTS
             String from = scanline1.stringValue( );
             String to   = scanline1.stringValue( );
             double d    = scanline1.doubleValue( );
             double b    = scanline1.doubleValue( );
             double c    = scanline1.doubleValue( );
             double r    = scanline1.doubleValue( );
             double acc  = scanline1.doubleValue( );
             double mag  = scanline1.doubleValue( );
             double dip  = scanline1.doubleValue( );
             long extend = scanline1.longValue( );
             long flag   = scanline1.longValue( );
             long leg    = scanline1.longValue( );
             status      = scanline1.longValue( );
             comment     = scanline1.stringValue( );
             // FIXME N.B. shot_type is not saved before 22
             long type   = 0; if ( db_version > 21 ) type   = scanline1.longValue( );
	     long millis = 0; if ( db_version > 31 ) millis = scanline1.longValue( );
	     long color  = 0; if ( db_version > 33 ) color  = scanline1.longValue( );

             if ( doInsertShot( sid, id, millis, color, from, to, d, b, c, r, extend, flag, leg, status, type, comment, false ) >= 0 ) {
               success &= updateShotAMDR( id, sid, acc, mag, dip, r, false );
	     } else {
	       success = false;
	     }
             // TDLog.Log( TDLog.LOG_DB, "insert shot " + sid + " " + id + " " + from + " " + to );
           } else if ( table.equals(FIXED_TABLE) ) {
             station    = scanline1.stringValue( );
             double lng = scanline1.doubleValue( );
             double lat = scanline1.doubleValue( );
             double alt = scanline1.doubleValue( );
             double asl = scanline1.doubleValue( );
             comment    = scanline1.stringValue( );
             status     = scanline1.longValue( );
             long source = scanline1.longValue( );
             double cs_lng = 0;
             double cs_lat = 0;
             double cs_alt = 0;
	     long cs_n_dec = 2;
             String cs = scanline1.stringValue( );
             if ( cs.length() > 0 ) {
               cs_lng = scanline1.doubleValue( );
               cs_lat = scanline1.doubleValue( );
               cs_alt = scanline1.doubleValue( );
	       if ( db_version > 34 ) cs_n_dec = scanline1.longValue( );
             }
             // use id == -1L to force DB get a new id
             if ( insertFixed( sid, -1L, station, lng, lat, alt, asl, comment, status, source, cs, cs_lng, cs_lat, cs_alt, cs_n_dec ) < 0 ) {
	       success = false;
	     }
             // TDLog.Log( TDLog.LOG_DB, "loadFromFile fixed " + sid + " " + id + " " + station  );
           } else if ( table.equals(STATION_TABLE) ) {
             // N.B. ONLY IF db_version > 19
             // TDLog.Error( "v <" + v + ">" );
             // TDLog.Log( TDLog.LOG_DB, "loadFromFile station " + sid + " " + name + " " + comment + " " + flag  );
             name    = scanline1.stringValue( );
             comment = scanline1.stringValue( );
             long flag = ( db_version > 25 )? scanline1.longValue() : 0;
             success &= insertStation( sid, name, comment, flag );
           }
         }
       }
       fr.close();
     } catch ( FileNotFoundException e ) {
     } catch ( IOException e ) {
     }

     return (success ? sid : -sid );
   }

   // ----------------------------------------------------------------------
   boolean insertStation( long sid, String name, String comment, long flag )
   {
     if ( myDB == null ) return false;
     boolean ret = false;
     if ( comment == null ) comment = "";
     Cursor cursor = myDB.query( STATION_TABLE, 
                            new String[] { "name", "comment", "flag" },
                            "surveyId=? and name=?", new String[] { Long.toString( sid ), name },
                            null, null, null );
     if (cursor.moveToFirst()) {
       // StringWriter sw = new StringWriter();
       // PrintWriter  pw = new PrintWriter( sw );
       // pw.format( Locale.US, "UPDATE stations SET comment=\"%s\", flag=%d WHERE surveyId=%d AND name=\"%s\"",
       //            comment, flag, sid, name );
       // myDB.execSQL( sw.toString() );

       if ( updateStationCommentStmt == null ) {
          updateStationCommentStmt = myDB.compileStatement( "UPDATE stations SET comment=?, flag=? WHERE surveyId=? AND name=?" );
       }
       updateStationCommentStmt.bindString( 1, comment );
       updateStationCommentStmt.bindLong(   2, flag );
       updateStationCommentStmt.bindLong(   3, sid );
       updateStationCommentStmt.bindString( 4, name );
       ret = doStatement( updateStationCommentStmt, "station update" );
     } else {
       ContentValues cv = new ContentValues();
       cv.put( "surveyId",  sid );
       cv.put( "name",      name );
       cv.put( "comment",   comment );
       cv.put( "flag",      flag );
       ret = doInsert( STATION_TABLE, cv, "station insert" );
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return ret;
   }

   CurrentStation getStation( long sid, String name )
   {
     CurrentStation cs = null;
     if ( myDB != null ) {
       Cursor cursor = myDB.query( STATION_TABLE,
           new String[]{ "name", "comment", "flag" },
           "surveyId=? and name=?", new String[]{ Long.toString( sid ), name },
           null, null, null );
       if (cursor.moveToFirst()) {
         cs = new CurrentStation( cursor.getString( 0 ), cursor.getString( 1 ), cursor.getLong( 2 ) );
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return cs;
   }


   ArrayList< CurrentStation > getStations( long sid )
   {
     ArrayList< CurrentStation > ret = new ArrayList<>();
     if ( myDB == null ) return ret;
     Cursor cursor = myDB.query( STATION_TABLE, 
                            new String[] { "name", "comment", "flag" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
     if (cursor.moveToFirst()) {
       do {
         ret.add( new CurrentStation( cursor.getString(0), cursor.getString(1), cursor.getLong(2) ) );
       } while (cursor.moveToNext());
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     return ret;
   }

   void deleteStation( long sid, String name )
   {
     StringWriter sw = new StringWriter();
     PrintWriter  pw = new PrintWriter( sw );
     pw.format( Locale.US, "DELETE FROM stations WHERE surveyId=%d AND name=\"%s\"", sid, name );
     doExecSQL( sw, "station delete" );

     // if ( deleteStationStmt == null )
     //   deleteStationStmt = myDB.compileStatement( "DELETE FROM stations WHERE surveyId=? AND name=?" );
     // deleteStationStmt.bindLong(   1, sid );
     // deleteStationStmt.bindString( 2, name );
     // deleteStationStmt.execute();
   }


   // ----------------------------------------------------------------------
   // DATABASE TABLES

   @SuppressWarnings("SyntaxError")
   private static class DistoXOpenHelper extends SQLiteOpenHelper
   {
      private static final String create_table = "CREATE TABLE IF NOT EXISTS ";

      DistoXOpenHelper(Context context, String database_name ) 
      {
         super(context, database_name, null, DATABASE_VERSION);
         // Log.v("DistoX", "DB NAME " + database_name );
         // TDLog.Log( TDLog.LOG_DB, "createTables ... " + database_name + " version " + DATABASE_VERSION );
      }

      @Override
      public void onCreate(SQLiteDatabase db) 
      {
        createTables( db );
        // TDLog.Log( TDLog.LOG_DB, "DistoXOpenHelper onCreate done db " + db );
      }

      private void createTables( SQLiteDatabase db )
      {
         try {
           db.setLockingEnabled( false );
           db.beginTransaction();
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
             +   " declination REAL, "
             +   " init_station TEXT, "
             +   " xsections INTEGER "
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
             +   " extend INTEGER, " // LEFT VERT RIGHT IGNORE etc.
             +   " flag INTEGER, "   // NONE DUPLICATE SURFACE COMMENTED
             +   " leg INTEGER, "    // MAIN SEC SPLAY XSPLAY BACK ...
             +   " status INTEGER, " // NORMAL DELETED OVERSHOOT
             +   " comment TEXT, "
             +   " type INTEGER, "     // DISTOX MANUAL
             +   " millis INTEGER, "   // timestamp
	     +   " color INTEGER "     // custom color
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
             +   " status INTEGER, "
             +   " cs_name TEXT, "
             +   " cs_longitude REAL, "
             +   " cs_latitude REAL, "
             +   " cs_altitude REAL, "
             +   " source INTEGER, "    // 0: unknown,  1: topodroid,  2: manual,   3: mobile-topographer
	     +   " cs_decimals INTEGER"
             // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
             // +   " ON DELETE CASCADE "
             +   ")"
           );

           db.execSQL(
               create_table + STATION_TABLE 
             + " ( surveyId INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
             +   " name TEXT, "
             +   " comment TEXT, "
             +   " flag INTEGER default 0 "
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
             +   " clino REAL, "
             +   " hide TEXT, "
             +   " nick TEXT, "
	     +   " orientation INTEGER "
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
               create_table + AUDIO_TABLE
             + " ( surveyId INTEGER, "
             +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
             +   " shotId INTEGER, "
             +   " date TEXT "
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
           db.endTransaction();
         // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
         } catch ( SQLException e ) { TDLog.Error( "createTables exception: " + e.getMessage() );
         } finally {
           db.setLockingEnabled( true );
         }
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {  
         // FIXME this is called at each start when the database file exists
         TDLog.Log( TDLog.LOG_DB, "onUpgrade old " + oldVersion + " new " + newVersion );
         switch ( oldVersion ) {
           case 14: 
             db.execSQL( "ALTER TABLE surveys ADD COLUMN declination REAL default 0" );
             db.execSQL( "ALTER TABLE gms ADD COLUMN status INTEGER default 0" );
           case 15:
             db.execSQL( "ALTER TABLE devices ADD COLUMN name TEXT" );
            case 16:
// FIXME DEVICE_DB
//              db.execSQL( "ALTER TABLE calibs ADD COLUMN coeff BLOB" );
//            case 17:
//              db.execSQL( "ALTER TABLE calibs ADD COLUMN error REAL default 0" );
//              db.execSQL( "ALTER TABLE calibs ADD COLUMN max_error REAL default 0" );
//              db.execSQL( "ALTER TABLE calibs ADD COLUMN iterations INTEGER default 0" );
//            case 18:
//              db.execSQL( "ALTER TABLE calibs ADD COLUMN algo INTEGER default 1" );
           case 19:
             db.execSQL( "CREATE TABLE stations ( surveyId INTEGER, name TEXT, comment TEXT )" );
           case 20:
             db.execSQL( "UPDATE plots SET type=5 WHERE type=3" );
             db.execSQL( "ALTER TABLE plots ADD COLUMN clino REAL default 0" );
           case 21:
             db.execSQL( "ALTER TABLE shots ADD COLUMN type INTEGER default 0" );
           case 22:
             db.execSQL( "ALTER TABLE surveys ADD COLUMN init_station TEXT default \"0\"" );
           case 23:
// FIXME DEVICE_DB
//              db.execSQL( "ALTER TABLE devices ADD COLUMN nickname TEXT default \"\"" );
           case 24:
             db.execSQL( "ALTER TABLE plots ADD COLUMN hide TEXT default \"\"" );
           case 25:
             db.execSQL( "ALTER TABLE stations ADD COLUMN flag INTEGER default 0" );
           case 26:
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN cs_name TEXT default \"\"" );
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN cs_longitude REAL default 0" );
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN cs_latitude REAL default 0" );
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN cs_altitude REAL default 0" );
           case 27:
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN source INTEGER default 0" );
           case 28:
             db.execSQL( 
                 create_table + AUDIO_TABLE
               + " ( surveyId INTEGER, "
               +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
               +   " shotId INTEGER, "
               +   " date TEXT "
               +   ")"
             );
           case 29:
             db.execSQL( "ALTER TABLE surveys ADD COLUMN xsections INTEGER default 0" );
           case 30:
             db.execSQL( "ALTER TABLE plots ADD COLUMN nick TEXT default \"\"" );
           case 31:
             db.execSQL( "ALTER TABLE shots ADD COLUMN millis INTEGER default 0" );
	   case 32:
             db.execSQL( "ALTER TABLE plots ADD COLUMN orientation INTEGER default 0" );
	   case 33:
             db.execSQL( "ALTER TABLE shots ADD COLUMN color INTEGER default 0" );
	   case 34:
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN cs_decimals INTEGER default 2" );
	   case 35:
             /* current version */
           default:
             break;
         }
      }
   }
}
