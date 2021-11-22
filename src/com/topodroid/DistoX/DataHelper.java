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
 *
 *  Note: variables
 *     String[] vals for String.split
 *     ContentValues cv
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDsaf;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDStatus;
import com.topodroid.prefs.TDSetting;
import com.topodroid.inport.ParserShot;
import com.topodroid.common.LegType;
import com.topodroid.common.ExtendType;
import com.topodroid.common.PlotType;

// import java.io.File;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Locale;
import java.util.HashMap;


public class DataHelper extends DataSetObservable
{
  private static final String CONFIG_TABLE = "configs";
  private static final String SURVEY_TABLE = "surveys";
  private static final String FIXED_TABLE  = "fixeds";
  private static final String SHOT_TABLE   = "shots";
  private static final String STATION_TABLE = "stations";
  private static final String PLOT_TABLE   = "plots";
  private static final String SKETCH_TABLE = "sketches";
  private static final String PHOTO_TABLE  = "photos";
  private static final String SENSOR_TABLE = "sensors";
  private static final String AUDIO_TABLE  = "audios";

  private final static String WHERE_ID          = "id=?";
  private final static String WHERE_SID         = "surveyId=?";
  private final static String WHERE_SID_ID      = "surveyId=? AND id=?";
  // private final static String WHERE_SID_ID_LEGTYPE = "surveyId=? AND id=? AND leg=?";
  private final static String WHERE_SID_ID_MORE = "surveyId=? AND id>=?";
  private final static String WHERE_SID_NAME    = "surveyId=? AND name=?";
  private final static String WHERE_SID_STATUS  = "surveyId=? AND status=?";
  // private final static String WHERE_SID_STATUS_LEG  = "surveyId=? AND status=? AND fStation > \"\" AND tStation > \"\"";
  private final static String WHERE_SID_LEG     = "surveyId=? AND fStation > \"\" AND tStation > \"\"";
  private final static String WHERE_ID_SID_LEG  = "id<=? AND surveyId=? AND fStation > \"\" AND tStation > \"\"";
  private final static String WHERE_SID_SHOTID  = "surveyId=? AND shotId=?";
  private final static String WHERE_SID_START   = "surveyId=? AND start=?";

  private SQLiteDatabase myDB = null;
  private long           myNextId;   // id of next shot
  private long           myNextCId;  // id of next calib-data

  // private SQLiteStatement updateConfig = null;

  // private SQLiteStatement updateAudioStmt = null;
  // private SQLiteStatement updateShotStmt = null;
  // private SQLiteStatement updateShotStmtFull = null;
  // private SQLiteStatement updateShotDBCStmt = null;
  // private SQLiteStatement clearStationsStmt = null;
  // private SQLiteStatement updateShotLegStmt = null;
  // private SQLiteStatement updateStationCommentStmt = null;
  // private SQLiteStatement deleteStationStmt = null;
  // private SQLiteStatement updateShotNameStmt = null;
  // private SQLiteStatement updateShotExtendStmt = null;
  // private SQLiteStatement updateShotFlagStmt = null;
  // private SQLiteStatement updateShotLegFlagStmt = null;
  // private SQLiteStatement updateShotCommentStmt = null;
  // private SQLiteStatement resetShotColorStmt = null;
  // private SQLiteStatement updateShotColorStmt = null;
  // private SQLiteStatement updateShotAMDRStmt = null;
  // private SQLiteStatement shiftShotsIdStmt = null;

  // private SQLiteStatement updatePlotStmt     = null;
  // private SQLiteStatement updatePlotViewStmt = null;
  // private SQLiteStatement updatePlotHideStmt = null;
  // private SQLiteStatement updatePlotNameStmt = null;
  // private SQLiteStatement updatePlotOrientationStmt = null;
  // private SQLiteStatement updatePlotAzimuthClinoStmt = null;
  // private SQLiteStatement updatePlotNickStmt = null;

/* FIXME_SKETCH_3D *
  private SQLiteStatement updateSketchStmt = null;
  // private SQLiteStatement deleteSketchStmt = null;
 * END_SKETCH_3D */
  // private SQLiteStatement updatePhotoStmt = null;
  // private SQLiteStatement updateSensorStmt = null;

  private SQLiteStatement transferShotStmt = null; // on-demand compile
  // private SQLiteStatement transferSensorStmt = null;
  // private SQLiteStatement transferPhotoStmt = null;
  // private SQLiteStatement transferFixedStmt = null;
  private SQLiteStatement transferPlotStmt = null;  // on-demand compile
  private SQLiteStatement transferSketchStmt = null;  // on-demand compile
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

  static final private String[] mFullShotFields =
    { "id", "fStation", "tStation", "distance", "bearing", "clino", "acceleration", "magnetic", "dip",
      "extend", "flag", "leg", "comment", "type", "millis", "color", "stretch", "address"
    };
  static final private String[] mShotFields =
    { "id", "fStation", "tStation", "distance", "bearing", "clino", "acceleration", "magnetic", "dip",  // 0 .. 8
      "extend", "flag", "leg", "comment", "type", "millis", "color", "stretch" // 9 .. 16
    };
  static final private String[] mShotRawDataFields =
    { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll", "acceleration", "magnetic", "dip",
      "type", "millis", "address", "extend", "flag", "leg", "status", "comment"
    };

  static final private String[] mPlotFieldsFull =
    { "id", "name", "type", "status", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino", "hide", "nick", "orientation", "maxscrap", "intercept" };
  static final private String[] mPlotFields =
    { "id", "name", "type", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino", "hide", "nick", "orientation", "maxscrap", "intercept" };

  static final private String[] mSketchFields =
    { "id", "name", "start", "st1", "st2",
      "xoffsettop", "yoffsettop", "zoomtop", "xoffsetside", "yoffsetside", "zoomside", "xoffset3d", "yoffset3d", "zoom3d",
      "east", "south", "vert", "azimuth", "clino" 
    };

  // N.B. "source" comes after "status" although it is after "cs_altitude" in the table
  static final private String[] mFixedFields = {
    "id", "station", "longitude", "latitude", "altitude", "altimetric", "comment", "status", "source",
    "cs_name", "cs_longitude", "cs_latitude", "cs_altitude", "cs_decimals"
  };

  // private DataListenerSet mListeners; // IF_COSURVEY

  // ----------------------------------------------------------------------
  // DATABASE

  // private final TopoDroidApp mApp; // unused

  // UNUSED
  // public SQLiteDatabase getDb() { return myDB; }

  public boolean hasDB() { return myDB != null; }

  /** cstr: open default database (in the current work directory)
   * @param context context
   */
  // public DataHelper( Context context, TopoDroidApp app, DataListenerSet listeners ) // IF_COSURVEY
  public DataHelper( Context context /* , TopoDroidApp app */ )
  {
    // mApp     = app;
    // mListeners = listeners; // IF_COSURVEY
    openDatabase( context );
  }

  /** cstr
   * @param context context
   * @param db_path database pathname
   */
  public DataHelper( Context context, String db_path )
  {
    openDatabase( context, db_path ); // true = readonly
  }

  void closeDatabase()
  {
    if ( myDB == null ) return;
    myDB.close();
    myDB = null;
  }

  /** open default database
   * @param context context
   */
  void openDatabase( Context context )
  {
    String db_name = TDPath.getDatabase(); // DistoX-SAF
    if ( TopoDroidApp.hasTopoDroidDatabase() ) {
      TDLog.v( "App has database helper " + db_name );
      try {
        myDB = SQLiteDatabase.openDatabase( db_name, null, SQLiteDatabase.OPEN_READWRITE );
        if ( myDB != null ) {
          int oldVersion = myDB.getVersion();
          int newVersion = TDVersion.DATABASE_VERSION;
          TDLog.v( "opened existing database: old version " + oldVersion + " current version " + newVersion );
          if ( oldVersion < newVersion ) {
            DistoXOpenHelper.updateTables( myDB, oldVersion, newVersion );
            myDB.setVersion( TDVersion.DATABASE_VERSION );
          }
        }
      } catch ( SQLiteException e ) {
        TDLog.Error( "opened database error: " + e.getMessage() );
        myDB = null;
      }
    } else {
      TDLog.v( "App does not have database helper " + db_name + " - trying to open it");
      try {
        myDB = SQLiteDatabase.openDatabase( db_name, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY );
        if ( myDB != null ) {
          TDLog.v( "open and create new database if necessary");
          DistoXOpenHelper.createTables( myDB );
          myDB.setVersion( TDVersion.DATABASE_VERSION );
        }
      } catch ( SQLiteException e ) {
        TDLog.Error( "opened database error: " + e.getMessage() );
        myDB = null;
      }
    }
  }

  /** open a given database file
   * @param context    context
   * @param db_name    database path
   */
  private void openDatabase( Context context, String db_name )
  {
    try {
      TDLog.v( "opened database, skip check");
      myDB = SQLiteDatabase.openDatabase( db_name, null, SQLiteDatabase.OPEN_READWRITE );
      if ( myDB != null ) {
        int oldVersion = myDB.getVersion();
        int newVersion = TDVersion.DATABASE_VERSION;
        TDLog.v( "opened readonly database: old version " + oldVersion + " current version " + newVersion );
        if ( oldVersion < newVersion ) {
          DistoXOpenHelper.updateTables( myDB, oldVersion, newVersion );
          myDB.setVersion( TDVersion.DATABASE_VERSION );
        }
      }
    } catch ( SQLiteException e ) {
      TDLog.Error( "opened database error: " + e.getMessage() );
      myDB = null;
    }
  }

  /** fill a raw data block with values from the cursor
   * @param sid     survey ID
   * @param blk     raw data block
   * @param cursor  values cursor
   */
  private void fillBlockRawData( long sid, RawDBlock blk, Cursor cursor )
  {
    blk.mId           = cursor.getLong(0);
    blk.mSurveyId     = sid;
    blk.mFrom         = cursor.getString(1);
    blk.mTo           = cursor.getString(2);
    blk.mLength       = (float)( cursor.getDouble(3) );  // length [meters]
    blk.mBearing      = (float)( cursor.getDouble(4) );  // bearing [degrees]
    blk.mClino        = (float)( cursor.getDouble(5) );  // clino [degrees], or depth [meters]
    blk.mRoll         = (float)( cursor.getDouble(6) );  // clino [degrees], or depth [meters]
    blk.mAcceleration = (float)( cursor.getDouble(7) );
    blk.mMagnetic     = (float)( cursor.getDouble(8) );
    blk.mDip          = (float)( cursor.getDouble(9) );
    blk.mShotType     = (int)(  cursor.getLong(10) );
    blk.mTime         = (long)( cursor.getLong(11) );
    blk.mAddress      = cursor.getString(12);
    blk.mExtend       = (int)(  cursor.getLong(13) );
    blk.mFlag         = cursor.getLong(14);
    blk.mLeg          = (int)(  cursor.getLong(15) );
    blk.mStatus       = (int)(  cursor.getLong(16) );
    blk.mComment      = cursor.getString( 17 );
  }

  /** fill a data block with values from the cursor
   * @param sid     survey ID
   * @param blk     data block
   * @param cursor  values cursor
   */
  private void fillBlock( long sid, DBlock blk, Cursor cursor )
  {
    long leg = cursor.getLong(11);
    blk.setId( cursor.getLong(0), sid );
    blk.setBlockName( cursor.getString(1), cursor.getString(2), (leg == LegType.BACK) );  // from - to
    blk.mLength       = (float)( cursor.getDouble(3) );  // length [meters]
    // blk.setBearing( (float)( cursor.getDouble(4) ) ); 
    blk.mBearing      = (float)( cursor.getDouble(4) );  // bearing [degrees]
    float clino       = (float)( cursor.getDouble(5) );  // clino [degrees], or depth [meters]
    blk.mAcceleration = (float)( cursor.getDouble(6) );
    blk.mMagnetic     = (float)( cursor.getDouble(7) );
    blk.mDip          = (float)( cursor.getDouble(8) );

    if ( TDInstance.datamode == SurveyInfo.DATAMODE_NORMAL ) {
      blk.mClino = clino;
      blk.mDepth = 0;
    } else { // DATAMODE_DIVING
      blk.mClino = 0;
      blk.mDepth = clino;
    }
    
    blk.setExtend( (int)(cursor.getLong(9) ), (float)( cursor.getDouble(16) ) );
    blk.resetFlag( cursor.getLong(10) );
    blk.setBlockType( (int)leg );
    blk.mComment  = cursor.getString(12);
    blk.setShotType( (int)cursor.getLong(13) );
    blk.mTime     = cursor.getLong(14);
    blk.setPaintColor( (int)cursor.getLong(15) ); // color
    // blk.setStretch( (float)cursor.getDouble(16) ); // already set above
    // blk.setAddress( null ); 
  }
  
  /** fill a data block with values from the cursor - including the address
   * @param sid     survey ID
   * @param blk     data block
   * @param cursor  values cursor
   */
  private void fullFillBlock( long sid, DBlock blk, Cursor cursor )
  {
    fillBlock( sid, blk, cursor );
    blk.setAddress( cursor.getString(17) );
  }

  // ----------------------------------------------------------------------
  // SURVEY DATA

  // private static String qInitStation  = "select init_station from surveys where id=?";
  // private static String qXSections    = "select xsections from surveys where id=?";
  // private static String qSurveysField = "select ? from surveys where id=?";
  private static final String qInitStation  = "select init_station from surveys where id=?";
  private static final String qXSections    = "select xsections from surveys where id=?";
  private static final String qDatamode     = "select datamode from surveys where id=?";
  private static final String qDeclination  = "select declination from surveys where id=?";
  private static final String qExtend       = "select extend from surveys where id=?";
  private static final String qSurveysStat1 = "select flag, acceleration, magnetic, dip from shots where surveyId=? AND status=0 AND acceleration > 1 ";
  private static final String qSurveysStat2 =
    "select flag, distance, fStation, tStation, clino, extend from shots where surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ";
  // private static String qSurveysStat3 = "select fStation, clino from shots where surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ";
  // private static String qSurveysStat4 =
  //   "select flag, distance, fStation, tStation, clino, extend from shots where surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ";
  private static final String qSurveysStat5 = " select count() from shots where surveyId=? AND status=0 AND flag=0 AND fStation!=\"\" AND tStation=\"\" ";

  String getSurveyInitStation( long sid )
  {
    String ret = TDSetting.mInitStation; 
    if ( myDB == null ) return ret;
    // Cursor cursor = myDB.query( SURVEY_TABLE,
    //     		        new String[] { "init_station" },
    //                             "id=? ", 
    //                             new String[] { Long.toString(sid) },
    //                             null,  // groupBy
    //                             null,  // having
    //                             null ); // order by
    Cursor cursor = myDB.rawQuery( qInitStation, new String[] { Long.toString(sid) } );
    if ( cursor != null ) {
      if (cursor.moveToFirst()) {
        ret = cursor.getString(0);
      }
      if ( ! cursor.isClosed() ) cursor.close();
    }
    // TDLog.v("DB init station <" + ret + ">" );
    return ret;
  }

  // at-station xsextions type
  //   0 : shared
  //   1 : private
  int getSurveyXSections( long sid )
  {
    int ret = 0;
    if ( myDB == null ) return ret;
    // Cursor cursor = myDB.query( SURVEY_TABLE,
    //     		        new String[] { "xsections" },
    //                             "id=? ", 
    //                             new String[] { Long.toString(sid) },
    //                             null,  // groupBy
    //                             null,  // having
    //                             null ); // order by
    Cursor cursor = myDB.rawQuery( qXSections, new String[] { Long.toString(sid) } );
    if ( cursor != null ) {
      if (cursor.moveToFirst()) {
        ret = (int)cursor.getLong(0);
      }
      if ( ! cursor.isClosed() ) cursor.close();
    }
    return ret;
  }

  // survey data-mode
  //   0 : normal
  //   1 : diving
  int getSurveyDataMode( long sid )
  {
    int ret = 0;
    if ( myDB == null ) return ret;
    // Cursor cursor = myDB.query( SURVEY_TABLE,
    //     		        new String[] { "datamode" },
    //                             "id=? ", 
    //                             new String[] { Long.toString(sid) },
    //                             null,  // groupBy
    //                             null,  // having
    //                             null ); // order by
    Cursor cursor = myDB.rawQuery( qDatamode, new String[] { Long.toString(sid) } );
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
    // Cursor cursor = myDB.query( SURVEY_TABLE,
    //     		        new String[] { "declination" },
    //                             "id=?", 
    //                             new String[] { Long.toString(sid) },
    //                             null,  // groupBy
    //                             null,  // having
    //                             null ); // order by
    Cursor cursor = myDB.rawQuery( qDeclination, new String[] { Long.toString(sid) } );
    if (cursor.moveToFirst()) {
      ret = (float)(cursor.getDouble( 0 ));
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  int getSurveyExtend( long sid )
  {
    int ret = SurveyInfo.SURVEY_EXTEND_NORMAL;
    if ( myDB == null ) return SurveyInfo.SURVEY_EXTEND_NORMAL;
    Cursor cursor = myDB.rawQuery( qExtend, new String[] { Long.toString(sid) } );
    if (cursor.moveToFirst()) {
      ret = (int)(cursor.getLong( 0 ));
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  void updateSurveyExtend( long sid, int extend )
  {
    if ( myDB == null ) return;
    ContentValues cv = makeSurveyExtend( extend );
    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(sid) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError("survey set extend ", e1 ); 
    } catch ( IllegalStateException e2 ) { logError("survey set extend ", e2 );
    } finally { myDB.endTransaction(); }
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

    int datamode = getSurveyDataMode( sid );
    String[] args = new String[1];
    args[0] = Long.toString( sid );

    Cursor cursor; // = null;
    if ( datamode == 0 ) {
      // cursor = myDB.query( SHOT_TABLE,
      //     		   new String[] { "flag", "acceleration", "magnetic", "dip" },
      //                      "surveyId=? AND status=0 AND acceleration > 1 ",
      //                      new String[] { Long.toString(sid) },
      //                      null, null, null );
      cursor = myDB.rawQuery( qSurveysStat1, args );
      int nrMGD = 0;
      if (cursor.moveToFirst()) {
        int nr = cursor.getCount();
        stat.G = new float[ nr ];
        stat.M = new float[ nr ];
        stat.D = new float[ nr ];
        do {
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
    }

    // count components
    if ( datamode == 0 ) {
      // cursor = myDB.query( SHOT_TABLE,
      //                      new String[] { "flag", "distance", "fStation", "tStation", "clino", "extend" },
      //                      "surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ", 
      //                      new String[] { Long.toString(sid) },
      //                      null, null, null );
      cursor = myDB.rawQuery( qSurveysStat2, args );
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
    } else {
      // // select s1.flag, s1.distance, s1.fStation, s1.tStation, s1.clino-s2.clino, "extent" 
      // //        from shots as s1 inner join shots as s2 on s1.tStation = s2.fStation
      // //        where s1.surveyId=? and s2.surveyId=? and s1.tStation != ""
      // //
      // HashMap< String, Float > depths = new HashMap< String, Float >();
      // // cursor = myDB.query( SHOT_TABLE,
      // //                      new String[] { "fStation", "clino" },
      // //                      "surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ", 
      // //                      new String[] { Long.toString(sid) },
      // //                      null, null, null );
      // cursor = myDB.rawQuery( qSurveysStat3, args );
      // if (cursor.moveToFirst()) {
      //   do {
      //     String station = cursor.getString(0);
      //     float  depth   = (float)(cursor.getDouble(1));
      //     // depths.putIfAbsent( station, new Float(depth) );
      //     if ( ! depths.containsKey(station) ) depths.put( station, new Float(depth) );
      //   } while ( cursor.moveToNext() );
      // }
      // if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

      // // select s.flag, s.distance, s.fStation, s.tStation, s.clino, z.clino, s.extend 
      // //        from shots as s join shots as z on z.fStation=s.tStation
      // //        where s.surveyId=? AND z.surveyId=? AND s.fStation!="" AND s.tStation!="" AND s.status=0
      // // cursor = myDB.query( SHOT_TABLE,
      // //                      new String[] { "flag", "distance", "fStation", "tStation", "clino", "extend" },
      // //                      "surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ", 
      // //                      new String[] { Long.toString(sid) },
      // //                      null, null, null );
      // cursor = myDB.rawQuery( qSurveysStat4, args );

      cursor = myDB.rawQuery( qjShots, new String[] { Long.toString(sid), Long.toString(sid) } );
      if (cursor.moveToFirst()) {
        do {
          String f = cursor.getString(2);
          String t = cursor.getString(3);
          float len = (float)( cursor.getDouble(1) );
          switch ( (int)(cursor.getLong(0)) ) {
            case 0: // NORMAL SHOT
              ++ stat.countLeg;
              stat.lengthLeg += len;
	      // if ( depths.containsKey( t ) ) {
	        // float dep = (float)( cursor.getDouble(4) ) - depths.get( t ).floatValue();
	        float dep = (float)( cursor.getDouble(4) - cursor.getDouble(5) );
                if ( cursor.getLong(6) == 0 ) {
                  stat.extLength += Math.abs( dep );
                } else {
                  stat.extLength += len;
                }
		if ( len > dep ) stat.planLength += (float)( Math.sqrt( len*len - dep*dep ) );
	      // }
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
    }

    stat.countStation = map.size();
    stat.countLoop = nl;
    stat.countComponent = nc;
    // TDLog.Log( TDLog.LOG_DB, "Get Survey Stats NV " + nv + " NE " + ne + " NL " + nl + " NC " + nc);

    // cursor = myDB.query( SHOT_TABLE,
    //                      new String[] { "count()" },
    //                      "surveyId=? AND status=0 AND flag=0 AND fStation!=\"\" AND tStation=\"\" ",
    //                      new String[] { Long.toString(sid) },
    //                      null,  // groupBy
    //                      null,  // having
    //                      null ); // order by
    cursor = myDB.rawQuery( qSurveysStat5, args );
    if (cursor.moveToFirst()) {
      stat.countSplay = (int)( cursor.getLong(0) );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return stat;
  }

  // --------------------------------------------------------------------
  /* compile statements

  void compileStatements()
  {
    if ( myDB != null ) {
      // clearStationsStmt = myDB.compileStatement( "UPDATE shots SET fStation=\"\", tStation=\"\" where id>? and surveyId=?" );
      // updateShotDBCStmt = myDB.compileStatement( "UPDATE shots SET distance=?, bearing=?, clino=? WHERE surveyId=? AND id=?" );
      // updateShotStmtFull   = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, leg=?, comment=? WHERE surveyId=? AND id=?" );
      // updateShotStmt       = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=?, extend=?, flag=?, leg=? WHERE surveyId=? AND id=?" );
      // updateAudioStmt      = myDB.compileStatement( "UPDATE audios SET date=? WHERE surveyId=? AND shotId=?" );

      // updateShotNameStmt    = myDB.compileStatement( "UPDATE shots SET fStation=?, tStation=? WHERE surveyId=? AND id=?" );
      // updateShotExtendStmt  = myDB.compileStatement( "UPDATE shots SET extend=?, stretch=? WHERE surveyId=? AND id=?" );
      // updateShotCommentStmt = myDB.compileStatement( "UPDATE shots SET comment=? WHERE surveyId=? AND id=?" );
      // shiftShotsIdStmt   = myDB.compileStatement( "UPDATE shots SET id=id+1 where surveyId=? and id>=?" );
      // updateShotLegStmt  = myDB.compileStatement( "UPDATE shots SET leg=? WHERE surveyId=? AND id=?" );
      // updateShotFlagStmt = myDB.compileStatement( "UPDATE shots SET flag=? WHERE surveyId=? AND id=?" );
      // updateShotLegFlagStmt = myDB.compileStatement( "UPDATE shots SET leg=?, flag=? WHERE surveyId=? AND id=?" );

      // resetShotColorStmt   = myDB.compileStatement( "UPDATE shots SET color=0 WHERE surveyId=?" );
      // updateShotColorStmt  = myDB.compileStatement( "UPDATE shots SET color=? WHERE surveyId=? AND id=?" );
      // updateShotAMDRStmt   =
      //   myDB.compileStatement( "UPDATE shots SET acceleration=?, magnetic=?, dip=?, roll=? WHERE surveyId=? AND id=?" );

      // updatePlotStmt     = myDB.compileStatement( "UPDATE plots set xoffset=?, yoffset=?, zoom=? WHERE surveyId=? AND id=?" );
      // updatePlotViewStmt = myDB.compileStatement( "UPDATE plots set view=? WHERE surveyId=? AND id=?" );
      // updatePlotHideStmt = myDB.compileStatement( "UPDATE plots set hide=? WHERE surveyId=? AND id=?" );
      // updatePlotNameStmt = myDB.compileStatement( "UPDATE plots set name=? WHERE surveyId=? AND id=?" );
      // updatePlotOrientationStmt = myDB.compileStatement( "UPDATE plots set orientation=? WHERE surveyId=? AND id=?" );
      // updatePlotAzimuthClinoStmt = myDB.compileStatement( "UPDATE plots set azimuth=?, clino=? WHERE surveyId=? AND id=?" );
      // updatePlotNickStmt = myDB.compileStatement( "UPDATE plots set nick=? WHERE surveyId=? AND id=?" );
      // dropPlotStmt    = myDB.compileStatement( "DELETE FROM plots WHERE surveyId=? AND id=?" );

      // FIXME_SKETCH_3D
      // updateSketchStmt = myDB.compileStatement( "UPDATE sketches set st1=?, st2=?, xoffsettop=?, yoffsettop=?, zoomtop=?, xoffsetside=?, yoffsetside=?, zoomside=?, xoffset3d=?, yoffset3d=?, zoom3d=?, east=?, south=?, vert=?, azimuth=?, clino=? WHERE surveyId=? AND id=?" );
      // END_SKETCH_3D 

      // deleteSketchStmt = myDB.compileStatement( "UPDATE sketches set status=1 WHERE surveyId=? AND id=?" );
      // updatePhotoStmt  = myDB.compileStatement( "UPDATE photos set comment=? WHERE surveyId=? AND id=?" );
      // deletePhotoStmt  = myDB.compileStatement( "DELETE FROM photos WHERE surveyId=? AND id=?" );
      // deleteSensorStmt = myDB.compileStatement( "UPDATE sensors set status=1 WHERE surveyId=? AND id=?" );
      // updateSensorStmt = myDB.compileStatement( "UPDATE sensors set comment=? WHERE surveyId=? AND id=?" );

      // commented because of crash 202007 -> on-demand compile
      // transferShotStmt   = myDB.compileStatement( "UPDATE shots SET surveyId=?, id=? where surveyId=? and id=?" );
      // transferPlotStmt   = myDB.compileStatement( "UPDATE plots set surveyId=? WHERE surveyId=? AND id=?" );
      // transferSketchStmt = myDB.compileStatement( "UPDATE sketches set surveyId=? WHERE surveyId=? AND id=?" );
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

      // updateStationCommentStmt = myDB.compileStatement( "UPDATE stations SET comment=?, flag=? WHERE surveyId=? AND name=?" );
      // deleteStationStmt        = myDB.compileStatement( "DELETE FROM stations WHERE surveyId=? AND name=?" );
    }
    // TDLog.v( "compile statements done");
  }
  */

  private void logError( String msg, Exception e )
  {
    TDLog.Error("DB " + msg + ": " + e.getMessage() );
  }

  private void handleDiskIOError( SQLiteDiskIOException e )
  {
    // TDLog.e( "DB disk IO error " + e.getMessage() );
    if ( TopoDroidApp.mMainActivity != null ) {
      TopoDroidApp.mMainActivity.runOnUiThread( new Runnable() { public void run() { TDToast.makeBad( R.string.disk_io_error ); } } );
    }
  }

  // --------------------------------------------------------------------
  // SURVEY
  // survey attributes renaming are "rare" actions

  boolean renameSurvey( long id, String name )
  {
    boolean ret = true;
    ContentValues cv = new ContentValues();
    cv.put("name", name );
    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(id) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e ); ret = false;
    } catch ( SQLiteException e1 )       { logError("survey rename " + name, e1 ); ret =false;
    } catch ( IllegalStateException e2 ) { logError("survey rename", e2 ); ret = false;
    } finally { myDB.endTransaction(); }
    return ret;
  }

  private ContentValues makeSurveyExtend( int extend )
  {
    ContentValues cv = new ContentValues();
    cv.put( "extend", extend );
    return cv;
  }

  private ContentValues makeSurveyInfoCcontentValues( String date, String team, double decl, String comment,
                                String init_station, int xsections ) // datamode cannot be updated
  {
    ContentValues cv = new ContentValues();
    cv.put( "day", date );
    cv.put( "team", ((team != null)? team : TDString.EMPTY ) );
    cv.put( "declination", decl );
    cv.put( "comment", ((comment != null)? comment : TDString.EMPTY ) );
    cv.put( "init_station", ((init_station != null)? init_station : TDString.ZERO ) );
    cv.put( "xsections", xsections );
    return cv;
  }

  void updateSurveyInfo( long sid, String date, String team, double decl, String comment,
                         String init_station, int xsections )
                         // FIXME int extend
  {
    // TDLog.v("DB update survey, init station <" + init_station + ">" );
    ContentValues cv = makeSurveyInfoCcontentValues( date, team, decl, comment, init_station, xsections );
    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(sid) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError("survey info", e1 ); 
    } catch ( IllegalStateException e2 ) { logError("survey info", e2 );
    } finally { myDB.endTransaction(); }
  }

  public boolean updateSurveyDayAndComment( String name, String date, String comment )
  {
    boolean ret = false;
    long id = getIdFromName( SURVEY_TABLE, name );
    if ( id >= 0 ) { // survey name exists
      ret = updateSurveyDayAndComment( id, date, comment );
    }
    return ret;
  }

  private boolean doUpdateSurvey( long id, ContentValues cv, String msg )
  {
    if ( myDB == null ) return false;
    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(id) } );
      myDB.setTransactionSuccessful();
      return true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 ); 
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return false;
  }

  private boolean doUpdate( String table, ContentValues cv, long sid, long id, String msg )
  {
    boolean ret = false;
    try {
      myDB.beginTransaction();
      myDB.update( table, cv, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 );
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  private void doUpdate( String table, ContentValues cv, String where, String[] args, String msg )
  {
    try {
      myDB.beginTransaction();
      myDB.update( table, cv, where, args );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 );
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
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

  private boolean doExecShotSQL( long id, StringWriter sw ) { return doExecSQL( sw, "sht " + id ); }

  private void updateStatus( String table, long id, long sid, long status )
  {
    ContentValues cv = new ContentValues();
    cv.put( "status", status );
    try {
      myDB.beginTransaction();
      myDB.update( table, cv, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e )  {  handleDiskIOError( e );
    } catch (SQLiteException e1 )        { logError(table + " update " + id, e1 ); 
    } catch ( IllegalStateException e2 ) { logError(table + " update " + id, e2 );
    } finally { myDB.endTransaction(); }
  }

  // UNUSED
  // private boolean doStatement( SQLiteStatement stmt, String msg )
  // {
  //   boolean ret = false;
  //   try {
  //     myDB.beginTransaction();
  //     stmt.execute();
  //     myDB.setTransactionSuccessful();
  //     ret = true;
  //   } catch ( SQLiteDiskIOException e0 ) { handleDiskIOError( e0 );
  //   } catch ( SQLiteException e1 )       { logError(msg, e1 ); 
  //   } catch ( IllegalStateException e2 ) { logError(msg, e2 );
  //   } finally { myDB.endTransaction(); }
  //   return ret;
  // }

  // -----------------------------------------------------------------

  public boolean updateSurveyDayAndComment( long id, String date, String comment )
  {
    if ( date == null ) return false;
    ContentValues cv = new ContentValues();
    cv.put( "day", date );
    cv.put( "comment", (comment != null)? comment : TDString.EMPTY );
    return doUpdateSurvey( id, cv, "survey day+cmt" );
  }

  public boolean updateSurveyTeam( long id, String team )
  {
    ContentValues cv = new ContentValues();
    cv.put( "team", team );
    return doUpdateSurvey( id, cv, "survey team" );
  }

  public boolean updateSurveyInitStation( long id, String station )
  {
    ContentValues cv = new ContentValues();
    // TDLog.v("DB update survey init_station <" + station + ">" );
    cv.put( "init_station", station );
    return doUpdateSurvey( id, cv, "survey init_station" );
  }
  
  public boolean updateSurveyDeclination( long id, double decl )
  {
    ContentValues cv = new ContentValues();
    cv.put( "declination", decl );
    return doUpdateSurvey( id, cv, "survey decl" );
  }

  // -----------------------------------------------------------------------
  // SURVEY delete

  void doDeleteSurvey( long sid )
  {
    if ( myDB == null ) return;
    String[] clause = new String[]{ Long.toString( sid ) };

    try {
      // myDB.setLockingEnabled( false );
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
      // myDB.setLockingEnabled( true );
    }
  }
  
  // --------------------------------------------------------------------
  // SHOTS

  // void clearStationsAfter( long id, long sid )
  // {
  //   // update shots set fStation="", tStation="" where id>id and surveyId=sid
  //   if ( clearStationsStmt == null )
  //     clearStationsStmt = myDB.compileStatement( "UPDATE shots SET fStation=\"\", tStation=\"\" where id>? and surveyId=?" );
  //   clearStationsStmt.bindLong( 1, id );
  //   clearStationsStmt.bindLong( 2, sid );
  //   try { clearStationsStmt.execute(); } catch (SQLiteException e ) { logError("clear station after", e); }
  // }


  // this is an update of a manual-shot data
  void updateShotDistanceBearingClino( long id, long sid, float d, float b, float c )
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
    doExecShotSQL( id, sw );
  }

  void updateShotDepthBearingDistance( long id, long sid, float p, float b, float d )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US,
               "UPDATE shots SET distance=%.6f, bearing=%.4f, clino=%.4f WHERE surveyId=%d AND id=%d",
               d, b, p, sid, id );
    doExecShotSQL( id, sw );
  }

  int updateShotNameAndData( long id, long sid, String fStation, String tStation,
                  long extend, long flag, long leg, String comment )
  {
    if ( myDB == null ) return -1;
    // if ( makesCycle( id, sid, fStation, tStation ) ) return -2;

    // TDLog.v("DB update shot. id " + id + " extend " + extend + " leg " + leg );
    if ( tStation == null ) tStation = TDString.EMPTY;

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    if ( comment != null ) {
      pw.format( Locale.US,
        "UPDATE shots SET fStation=\"%s\", tStation=\"%s\", extend=%d, flag=%d, leg=%d, comment=\"%s\" WHERE surveyId=%d AND id=%d",
        fStation, tStation, extend, flag, leg, comment, sid, id );
    } else {
      pw.format( Locale.US,
        "UPDATE shots SET fStation=\"%s\", tStation=\"%s\", extend=%d, flag=%d, leg=%d WHERE surveyId=%d AND id=%d",
        fStation, tStation, extend, flag, leg, sid, id );
    }
    doExecShotSQL( id, sw );

    // TDLog.v("DB update shot " + fStation + " " + tStation + " success " + success );
    return 0;
  }

  int updateShotNameAndDataStatus( long id, long sid, String fStation, String tStation,
                  long extend, long flag, long leg, String comment, int status )
  {
    if ( myDB == null ) return -1;
    // if ( makesCycle( id, sid, fStation, tStation ) ) return -2;
    if ( tStation == null ) tStation = TDString.EMPTY;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    if ( comment != null ) {
      pw.format( Locale.US,
        "UPDATE shots SET fStation=\"%s\", tStation=\"%s\", extend=%d, flag=%d, leg=%d, comment=\"%s\", status=%d WHERE surveyId=%d AND id=%d",
        fStation, tStation, extend, flag, leg, comment, status, sid, id );
    } else {
      pw.format( Locale.US,
        "UPDATE shots SET fStation=\"%s\", tStation=\"%s\", extend=%d, flag=%d, leg=%d, status=%d WHERE surveyId=%d AND id=%d",
        fStation, tStation, extend, flag, leg, status, sid, id );
    }
    doExecShotSQL( id, sw );
    return 0;
  }

  private void shiftShotsId( long sid, long id )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET id=id+1 where surveyId=%d and id>=%d", sid, id );
    doExecShotSQL( id, sw );
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

  // return false if there are no conflicting siblings
  boolean checkSiblings( long id0, long sid, String from, String to, float d0, float b0, float c0 )
  {
    float thr_a = 15;
    float thr_d = 0.25f;
    boolean ret = false;
    // TDLog.v( "check " + id0 + " siblings " + from + " " + to + " " + d0 + " " + b0 + " " + c0 );
    Cursor cursor = myDB.rawQuery( qShotsByStations, new String[] { Long.toString( sid ), from, to } );
    if (cursor.moveToFirst()) {
      cursor.moveToFirst();
      do { 
        if ( cursor.getLong( 0 ) != id0 ) {
          // TDLog.v( "[1]-sibling " + cursor.getLong(0) + " " + cursor.getDouble(1) + " " + cursor.getDouble(2) + " " + cursor.getDouble(3) );
          double b1 = Math.abs(cursor.getDouble( 2 ) - b0);
          if ( b1 > 180 ) b1 = Math.abs( b1 - 360 );
          if ( ( Math.abs( cursor.getDouble( 1 ) - d0 ) > thr_d*d0 )
            || ( b1 > thr_a ) 
            || ( Math.abs( cursor.getDouble( 3 ) - c0 ) > thr_a ) ) {
            ret = true;
            break;
          }
        }
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    if ( ! ret ) {
      cursor = myDB.rawQuery( qShotsByStations, new String[] { Long.toString( sid ), to, from } );
      if ( cursor.moveToFirst() ) {
        do { 
          if ( cursor.getLong( 0 ) != id0 ) {
            // TDLog.v( "[2]-sibling " + cursor.getLong(0) + " " + cursor.getDouble(1) + " " + cursor.getDouble(2) + " " + cursor.getDouble(3) );
            double b1 = Math.abs(cursor.getDouble( 2 ) + 180 - b0);
            if ( b1 > 180 ) b1 = Math.abs( b1 - 360 );
            if ( ( Math.abs( cursor.getDouble( 1 ) - d0 ) > thr_d*d0 )
              || ( b1 > thr_a ) 
              || ( Math.abs( cursor.getDouble( 3 ) + c0 ) > thr_a ) ) {
              ret = true;
              break;
            }
          }
        } while (cursor.moveToNext());
      }
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  void updateShotName( long id, long sid, String fStation, String tStation )
  {
    // TDLog.v( "update shot " + id + " name " + fStation + " " + tStation );
    if ( myDB == null ) return;
    if ( fStation == null ) fStation = TDString.EMPTY;
    if ( tStation == null ) tStation = TDString.EMPTY;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET fStation=\"%s\", tStation=\"%s\" WHERE surveyId=%d AND id=%d",
               fStation, tStation, sid, id );
    doExecShotSQL( id, sw );
  }

  // used internally to merge to next leg
  private void updateShotNameAndLeg( long id, long sid, String fStation, String tStation, int leg )
  {
    if ( myDB == null ) return;
    if ( fStation == null ) fStation = TDString.EMPTY;
    if ( tStation == null ) tStation = TDString.EMPTY;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET fStation=\"%s\", tStation=\"%s\", leg=%d WHERE surveyId=%d AND id=%d",
               fStation, tStation, leg, sid, id );
    doExecShotSQL( id, sw );
  }

  void updateShotsName( List< DBlock > blks, long sid )
  {
    if ( myDB == null ) return;
    try {
      myDB.beginTransaction();
      for ( DBlock blk : blks ) {
        String from = blk.mFrom;
        String to   = blk.mTo;
        if ( from == null ) from = TDString.EMPTY;
        if ( to   == null ) to   = TDString.EMPTY;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        pw.format( Locale.US, "UPDATE shots SET fStation=\"%s\", tStation=\"%s\" WHERE surveyId=%d AND id=%d", from, to, sid, blk.mId );
        myDB.execSQL( sw.toString() );
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch ( SQLiteException e ) { logError("update shots name failed", e );
    } finally { myDB.endTransaction(); }
  }

  // "leg" flag: 0 splay, 1 leg, 2 x-splay
  void updateShotLeg( long id, long sid, long leg )
  {
    // TDLog.v( "A1 update shot leg. id " + id + " leg " + leg ); 
    // if ( myDB == null ) return;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET leg=%d WHERE surveyId=%d AND id=%d", leg, sid, id );
    doExecSQL( sw, "sht leg" );
  }

  // void updateShotLeg( long id, long sid, long leg )
  // {
  //   // TDLog.v( "A2 update shot leg. id " + id + " leg " + leg ); 
  //   StringWriter sw = new StringWriter();
  //   PrintWriter pw = new PrintWriter( sw );
  //   pw.format( Locale.US, "UPDATE shots SET leg=%d WHERE surveyId=%d AND id=%d", leg, sid, id );
  //   doExecShotSQL( id, sw );
  // }

  // FIXME_X2_SPLAY
  // long updateSplayLeg( long id, long sid )
  // {
  //   // if ( myDB == null ) return;
  //   // get the shot sid/id
  //   DBlock blk = selectShot( id, sid );
  //   if ( blk.isPlainSplay() ) {
  //     updateShotLeg( id, sid, LegType.XSPLAY );
  //     return LegType.XSPLAY;
  //   } else if ( blk.isXSplay() ) {
  //     updateShotLeg( id, sid, LegType.NORMAL );
  //     return LegType.NORMAL;
  //   }
  //   return LegType.INVALID;
  // }

  void updateShotExtend( long id, long sid, long extend, float stretch )
  {
    // if ( myDB == null ) return;

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET extend=%d, stretch=%.2f WHERE surveyId=%d AND id=%d", extend, stretch, sid, id );
    doExecShotSQL( id, sw );
  }

  void updateShotFlag( long id, long sid, long flag )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET flag=%d WHERE surveyId=%d AND id=%d", flag, sid, id );
    doExecShotSQL( id, sw );
  }

  void updateShotLegFlag( long id, long sid, long leg, long flag )
  {
    // TDLog.v( "A2 update shot leg/flag. id " + id + " leg " + leg + " flag " + flag ); 
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET leg=%d, flag=%d WHERE surveyId=%d AND id=%d", leg, flag, sid, id );
    doExecShotSQL( id, sw );
  }

  public void updateShotComment( long id, long sid, String comment )
  {
    // if ( myDB == null ) return;
    if ( comment == null ) comment = TDString.EMPTY;
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET comment=\"%s\" WHERE surveyId=%d AND id=%d", comment, sid, id );
    doExecShotSQL( id, sw );
  }

  // void updateShotStatus( long id, long sid, long status )
  // {
  //   StringWriter sw = new StringWriter();
  //   PrintWriter pw = new PrintWriter( sw );
  //   pw.format( Locale.US, "UPDATE shots SET status=%d WHERE surveyId=%d AND id=%d", status, sid, id );
  //   doExecShotSQL( id, sw );
  // }

  void deleteShot( long id, long sid, int status )
  {
    // if ( myDB == null ) return;
    updateStatus( SHOT_TABLE, id, sid, status );
  }

  void undeleteShot( long id, long sid )
  {
    // if ( myDB == null ) return;
    updateStatus( SHOT_TABLE, id, sid, TDStatus.NORMAL );
  }
  
  // called by the importXXXTask's
  public long insertImportShots( long sid, long id, ArrayList< ParserShot > shots )
  {
    // if ( myDB == null ) return -1L;
    long millis = 0L;
    long color  = 0L;

    InsertHelper ih = new InsertHelper( myDB, SHOT_TABLE ); // DEPRECATED API-17
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
    final int stretchCol  = ih.getColumnIndex( "stretch" );
    final int addressCol  = ih.getColumnIndex( "address" );
    try {
      // myDB.execSQL("PRAGMA synchronous=OFF");
      // myDB.setLockingEnabled( false );
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
	ih.bind( stretchCol, 0.0 );
	ih.bind( stretchCol, "" );
        ih.execute();
        ++id;
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError("parser shot insert", e);
    } finally {
      ih.close(); // FIXME this was before endTransaction
      myDB.endTransaction();
      // myDB.setLockingEnabled( true );
      // myDB.execSQL("PRAGMA synchronous=NORMAL");
    }
    /* ---- IF_COSURVEY
    if ( mListeners != null ) {
      // synchronized( mListeners )
      for ( ParserShot s : shots ) {
        mListeners.onInsertShot( sid, id, millis, color, s.from, s.to, s.len, s.ber, s.cln, s.rol,
                        s.extend, 0.0, // stretch = 0.0
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
    */
    return id;
  }
  
  // called by the importXXXTask's
  public long insertImportShotsDiving( long sid, long id, ArrayList< ParserShot > shots )
  {
    // [1] compute stations depth
    ArrayList< ParserShot > stack   = new ArrayList<>();
    HashMap< String, Float > depths = new HashMap< String, Float >();
    String start = shots.get(0).from; // FIXME
    depths.put( start, Float.valueOf(0) );
    for ( ParserShot sh : shots ) {
      String s1 = sh.from;
      String s2 = sh.to;
      if ( s1.length() > 0 && s2.length() > 0 && ( start.equals( s1 ) || start.equals( s2 ) ) ) stack.add( sh );
    }
    // TDLog.v( "start station <" + start + "> shots " + stack.size() );
    for ( int k = 0; k < stack.size(); ++k ) {
      ParserShot shot = stack.get(k);
      String from = shot.from;
      String to   = shot.to;
      if ( from != null && from.length() > 0 && depths.containsKey( from ) && to != null && to.length() > 0 && ! depths.containsKey( to ) ) { // can add TO station
        for ( ParserShot sh : shots ) {
	  String s1 = sh.from;
	  String s2 = sh.to;
          if ( s1.length() > 0 && s2.length() > 0 && ( to.equals( s1 ) || to.equals( s2 ) ) ) stack.add( sh );
        }
        float depth = depths.get( from ).floatValue() - shot.len * TDMath.sind( shot.cln );
        depths.put( to, Float.valueOf( depth ) );
        // TDLog.v( "processed shot <" + from + "-" + to + "> shots " + stack.size() + "add station <" + to + "> depth " + depth );
      }
      if ( to != null && to.length() > 0 && depths.containsKey( to ) && from != null && from.length() > 0 && ! depths.containsKey( from ) ) { // can add FROM station
        for ( ParserShot sh : shots ) {
	  String s1 = sh.from;
	  String s2 = sh.to;
          if ( s1.length() > 0 && s2.length() > 0 && ( from.equals( s1 ) || from.equals( s2 ) ) ) stack.add( sh );
        }
        float depth = depths.get( to ).floatValue() + shot.len * TDMath.sind( shot.cln );
        depths.put( from, Float.valueOf( depth ) );
        // TDLog.v( "processed shot <" + from + "-" + to + "> shots " + stack.size() + "add station <" + from + "> depth " + depth );
      }
    }
    //   [2] override shots clino with from-station depth (depth refers to the FROM station)
    for ( ParserShot sh : shots ) {
      Float f = depths.get( sh.from );
      sh.cln = ( f == null )? 0 : f.floatValue();
      // TDLog.v( "update shot " + sh.from + "-" + sh.to + " depth " + sh.cln + " azimuth " + sh.ber + " length " + sh.len );
    }
    // [3] call the insertImportShots
    return insertImportShots( sid, id, shots );
  }

  public long insertBricShot( long sid, long id, double d, double b, double c, double r, double mag, double acc, double dip, long extend, int leg, long status, String comment, String addr )
  { // 0L=leg, status, 0L=type DISTOX
    // stretch = 0.0;
    // return doInsertShot( sid, id, System.currentTimeMillis()/1000, 0L, "", "",  d, b, c, r, extend, 0.0, DBlock.FLAG_SURVEY, 0L, status, 0L, "", addr );
    return doCompleteInsertShot( sid, id, System.currentTimeMillis()/1000, 0L, d, b, c, r, mag, acc, dip, extend, 0.0, leg, status, comment, 0L, addr );
  }

  public long insertDistoXShot( long sid, long id, double d, double b, double c, double r, long extend, long status, String addr )
  { // 0L=leg, status, 0L=type DISTOX
    // stretch = 0.0;
    // return doInsertShot( sid, id, System.currentTimeMillis()/1000, 0L, "", "",  d, b, c, r, extend, 0.0, DBlock.FLAG_SURVEY, 0L, status, 0L, "", addr );
    return doSimpleInsertShot( sid, id, System.currentTimeMillis()/1000, 0L, d, b, c, r, extend, 0.0, 0L, status, 0L, addr );
  }

  public long insertShot( long sid, long id, long millis, long color, double d, double b, double c, double r,
		   long extend, double stretch, long leg,
                   long shot_type, String addr )
  { // leg, 0L=status, type 
    // return doInsertShot( sid, id, millis, color, "", "",  d, b, c, r, extend, stretch, DBlock.FLAG_SURVEY, leg, 0L, shot_type, "", addr );
    return doSimpleInsertShot( sid, id, millis, color, d, b, c, r, extend, stretch, leg, 0L, shot_type, addr );
  }

  long insertManualShot( long sid, long id, long millis, long color, double d, double b, double c, double r,
		   long extend, double stretch, long leg,
                   long shot_type )
  { // leg, 0L=status, type 
    // return doInsertShot( sid, id, millis, color, "", "",  d, b, c, r, extend, stretch, DBlock.FLAG_SURVEY, leg, 0L, shot_type, "", "" );
    return doSimpleInsertShot( sid, id, millis, color, d, b, c, r, extend, stretch, leg, 0L, shot_type, "" );
  }

  void resetShotColor( long sid )
  {
    // if ( resetShotColorStmt == null ) {
    //   resetShotColorStmt = myDB.compileStatement( "UPDATE shots SET color=0 WHERE surveyId=?" );
    // }
    // resetShotColorStmt.bindLong( 1, sid );
    // doStatement( resetShotColorStmt, "Color" );

    ContentValues cv = new ContentValues();
    cv.put("color", 0 );
    doUpdate( SHOT_TABLE, cv, "surveyId=?", new String[] { Long.toString(sid) }, "sht color reset" );
  }

  void updateShotColor( long id, long sid, int color )
  {
    // if ( myDB == null ) return;

    // StringWriter sw = new StringWriter();
    // PrintWriter pw  = new PrintWriter( sw );
    // pw.format( Locale.US,
    //            "UPDATE shots SET color=%d WHERE surveyId=%d AND id=%d",
    //            color, sid, id );
    // myDB.execSQL( sw.toString() );

    ContentValues cv = new ContentValues();
    cv.put("color", color );
    doUpdate( SHOT_TABLE, cv, sid, id, "sht color" );

    // if ( updateShotColorStmt == null ) {
    //   updateShotColorStmt   =
    //     myDB.compileStatement( "UPDATE shots SET color=? WHERE surveyId=? AND id=?" );
    // }
    // updateShotColorStmt.bindLong( 1, color );
    // updateShotColorStmt.bindLong( 2, sid );
    // updateShotColorStmt.bindLong( 3, id );
    // doStatement( updateShotColorStmt, "Color" );
    // return doStatement( updateShotColorStmt, "Color" );
  }

  void updateShotsColor( List< DBlock > blks, long sid, int color )
  {
    if ( myDB == null ) return;
    try {
      myDB.beginTransaction();
      String stmt = "UPDATE shots SET color=" + color + " WHERE surveyId=" + sid + " AND id=";
      for ( DBlock blk : blks ) {
        myDB.execSQL( stmt + blk.mId );
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("update shots color", e);
    } finally { myDB.endTransaction(); }
  }

  public void updateShotAMDR( long id, long sid, double acc, double mag, double dip, double r, boolean backshot )
  {
    // if ( myDB == null ) return;

    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw );
    if ( backshot ) { // shot type = -1
      pw.format( Locale.US,
               "UPDATE shots SET acceleration=%.6f, magnetic=%.6f, dip=%.4f, roll=%.6f, type=-1 WHERE surveyId=%d AND id=%d",
               acc, mag, dip, r, sid, id );
    } else { // shot type = 0 (default)
      pw.format( Locale.US,
               "UPDATE shots SET acceleration=%.6f, magnetic=%.6f, dip=%.4f, roll=%.6f WHERE surveyId=%d AND id=%d",
               acc, mag, dip, r, sid, id );
    }
    doExecSQL( sw, "sht AMDR" );
/*
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
    doStatement( updateShotAMDRStmt, "AMDR" );
    // return doStatement( updateShotAMDRStmt, "AMDR" );
*/
  }


  private void transferPlots( String old_survey_name, String new_survey_name, long sid, long old_sid, String station )
  {
    if ( myDB == null ) return;
    List< PlotInfo > plots = selectPlotsAtStation( old_sid, station );
    for ( PlotInfo plot : plots ) {
      transferPlot( sid, old_sid, plot.id );
      // TDFile.renameFile( TDPath.getTh2File( old_survey_name + "-" + plot.name + ".th2" ),
      //                    TDPath.getTh2File( new_survey_name + "-" + plot.name + ".th2" ) );

      TDFile.renameFile( TDPath.getTdrFile( old_survey_name + "-" + plot.name + ".tdr" ),
                         TDPath.getTdrFile( new_survey_name + "-" + plot.name + ".tdr" ) );
    }
  }

  /* FIXME_SKETCH_3D *
  private void transferSketches( String old_survey_name, String new_survey_name, long sid, long old_sid, String station )
  {
    if ( myDB == null ) return;
    List< Sketch3dInfo > sketches = selectSketchesAtStation( old_sid, station );
    for ( Sketch3dInfo sketch : sketches ) {
      transferSketch( sid, old_sid, sketch.id );
      String oldfile = TDPath.getTdr3File( old_survey_name + "-" + sketch.name + ".tdr3" );
      String newfile = TDPath.getTdr3File( new_survey_name + "=" + sketch.name + ".tdr3" );
      TDFile.renameFile( oldfile, newfile );
    }
  }
   * END_SKETCH_3D */

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
          // transferSketches( old_survey.name, new_survey.name, sid, old_sid, blk.mFrom ); // FIXME_SKETCH_3D
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
          // transferSketches( old_survey.name, new_survey.name, sid, old_sid, blk.mFrom ); // FIXME_SKETCH_3D
        }

        ContentValues cv = new ContentValues();
        cv.put( "surveyId", sid );
        cv.put( "shotId",   myNextId );
        String[] where = new String[2];
        where[0] = Long.toString( old_sid );
        List< SensorInfo > sensors = selectSensorsAtShot( old_sid, old_id ); // transfer sensors
        for ( SensorInfo sensor : sensors ) {
          where[1] = Long.toString( sensor.id );
          myDB.update( SENSOR_TABLE, cv, WHERE_SID_ID, where );
        }

        AudioInfo audio = getAudio( old_sid, old_id ); // transfer audio
        if ( audio != null ) {
          where[1] = Long.toString( audio.fileIdx );
          myDB.update( AUDIO_TABLE, cv, WHERE_SID_SHOTID, where );
          String oldname = TDPath.getSurveyWavFile( old_survey.name, Long.toString(audio.fileIdx) );
          String newname = TDPath.getSurveyWavFile( new_survey.name, Long.toString(audio.fileIdx) );
          TDFile.renameFile( oldname, newname );
        }

        List< PhotoInfo > photos = selectPhotoAtShot( old_sid, old_id ); // transfer photos
        for ( PhotoInfo photo : photos ) {
          where[1] = Long.toString( photo.id );
          myDB.update( PHOTO_TABLE, cv, WHERE_SID_ID, where );
          String oldname = TDPath.getSurveyJpgFile( old_survey.name, Long.toString(photo.id) );
          String newname = TDPath.getSurveyJpgFile( new_survey.name, Long.toString(photo.id) );
          TDFile.renameFile( oldname, newname );
        }

        ++ myNextId;
        ++ old_id;
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("transfer shots", e); }
  }

  long insertManualShotAt( long sid, long at, long millis, long color, double d, double b, double c, double r,
		     long extend, double stretch, long leg, long type )
  {
    if ( myDB == null ) return -1L;
    // TDLog.v("DB manual insert shot at " + at + " d " + d + " b " + b + " c " + c );
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
    cv.put( "stretch",  stretch );
    cv.put( "address",  "" );

    doInsert( SHOT_TABLE, cv, "insert at" );
    return at;
  }

  private ContentValues makeShotContentValues( long sid, long id, long millis, long color, String from, String to, 
                          double d, double b, double c, double r, double acc, double mag, double dip,
                          long extend, double stretch, long flag, long leg, long status, long shot_type,
                          String comment, String addr )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId", sid );
    cv.put( "id",       id );
    cv.put( "fStation", from );
    cv.put( "tStation", to );
    cv.put( "distance", d );
    cv.put( "bearing",  b );
    cv.put( "clino",    c );
    cv.put( "roll",     r );
    cv.put( "acceleration", acc );
    cv.put( "magnetic", mag );
    cv.put( "dip",      dip );
    cv.put( "extend",   extend );
    cv.put( "flag",     flag );
    cv.put( "leg",      leg );
    cv.put( "status",   status );
    cv.put( "comment",  comment );
    cv.put( "type",     shot_type );
    cv.put( "millis",   millis );
    cv.put( "color",    color );
    cv.put( "stretch",  stretch );
    cv.put( "address",  addr );
    return cv;
  }

  // return the new-shot id
  // It was called by ConnectionHandler too
  // @note always called with from="", to="", and comment=""
  /*
  private long doInsertShot( long sid, long id, long millis, long color, String from, String to,
                          double d, double b, double c, double r, 
                          long extend, double stretch, long flag, long leg, long status, long shot_type,
                          String comment, String addr )
  {
    // TDLog.Log( TDLog.LOG_DB, "insert shot <" + id + "> " + from + "-" + to + " extend " + extend );
    // TDLog.v("SHOT " + "do insert shot id " + id + " d " + d + " b " + b + " c " + c );
    if ( myDB == null ) return -1L;
    if ( id == -1L ) {
      ++ myNextId;
      id = myNextId;
    } else {
      myNextId = id;
    }
    if (addr == null) addr = "";
    ContentValues cv = makeShotContentValues( sid, id, millis, color, from, to, d, b, c, r, 0.0, 0.0, 0.0,
		    extend, stretch, flag, leg, status, shot_type, comment, addr );
    doInsert( SHOT_TABLE, cv, "insert" );
    return id;
  }
  */

  // return the new-shot id
  // doInsertShot() called with from="", to="", and comment="", flag=DBlock.FLAG_SURVEY
  //
  // @param sid           survey ID
  // @param id            shot id
  // @param millis        timestamp
  // param color          splay color
  // @param d, b, c, r    distance, bearing, clino, roll
  // @param extend
  // @param stretch       fractional extend
  // @param leg           "leg" type
  // @param status        shot status
  // @param shot_type     either DISTOX or MANUAL
  // @param addr          device address
  //
  // FROM and TO are set to ""
  // mag, acc, dip are set to 0
  // comment is set to ""
  // flag is set to SURVEY
  private long doSimpleInsertShot( long sid, long id, long millis, long color, 
                          double d, double b, double c, double r, 
                          long extend, double stretch, long leg, long status, long shot_type, String addr )
  {
    // TDLog.Log( TDLog.LOG_DB, "insert shot <" + id + "> " + from + "-" + to + " extend " + extend );
    TDLog.v("DB simple insert shot id " + id + " d " + d + " b " + b + " c " + c );
    if ( myDB == null ) return -1L;
    if ( id == -1L ) {
      ++ myNextId;
      id = myNextId;
    } else {
      myNextId = id;
    }
    if (addr == null) addr = "";
    ContentValues cv = makeShotContentValues( sid, id, millis, color, "", "", d, b, c, r, 0.0, 0.0, 0.0,
		    extend, stretch, DBlock.FLAG_SURVEY, leg, status, shot_type, "", addr );
    doInsert( SHOT_TABLE, cv, "simple insert" );
    return id;
  }

  private long doCompleteInsertShot( long sid, long id, long millis, long color, 
                          double d, double b, double c, double r, double mag, double acc, double dip,
                          long extend, double stretch, long leg, long status, String comment, long shot_type, String addr )
  {
    // TDLog.Log( TDLog.LOG_DB, "insert shot <" + id + "> " + from + "-" + to + " extend " + extend );
    TDLog.v("DB complete insert shot id " + id + " d " + d + " b " + b + " c " + c );
    if ( myDB == null ) return -1L;
    if ( id == -1L ) {
      ++ myNextId;
      id = myNextId;
    } else {
      myNextId = id;
    }
    if (addr == null) addr = "";
    ContentValues cv = makeShotContentValues( sid, id, millis, color, "", "", d, b, c, r, mag, acc, dip,
		    extend, stretch, DBlock.FLAG_SURVEY, leg, status, shot_type, comment, addr );
    doInsert( SHOT_TABLE, cv, "complete insert" );
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
    doExecSQL( sw, "plt updt" );

    // FIXME with the update statement I get a crash on immediate switching Plan/Profile on load
    // updatePlotStmt.bindDouble( 1, xoffset );
    // updatePlotStmt.bindDouble( 2, yoffset );
    // updatePlotStmt.bindDouble( 3, zoom );
    // updatePlotStmt.bindLong( 4, sid );
    // updatePlotStmt.bindLong( 5, pid );
    // try { updatePlotStmt.execute(); } catch (SQLiteException e) { logError("plt updt", e); }
  }

  void updatePlotOrigin( long sid, long pid, String station )
  {
    if ( myDB == null ) return; // false;
    if ( station == null ) station = "";
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE plots set start=\"%s\" WHERE surveyId=%d AND id=%d", station, sid, pid );
    doExecSQL( sw, "plt origin" );
  }
 
  void updatePlotNick( long pid, long sid, String nick )
  {
    if ( myDB == null ) return; // false;
    // TDLog.Log( TDLog.LOG_DB, "update PlotNick: " + pid + "/" + sid + " nick " + nick );
    if ( nick == null ) nick = TDString.EMPTY;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE plots set nick=\"%s\" WHERE surveyId=%d AND id=%d", nick, sid, pid );
    doExecSQL( sw, "plt nick" );

    // if ( updatePlotNickStmt == null ) {
    //   updatePlotNickStmt = myDB.compileStatement( "UPDATE plots set nick=? WHERE surveyId=? AND id=?" );
    // }
    // updatePlotNickStmt.bindString( 1, nick );
    // updatePlotNickStmt.bindLong( 2, sid );
    // updatePlotNickStmt.bindLong( 3, pid );
    // doStatement( updatePlotNickStmt, "plt nick" );
  }
 
  void updatePlotView( long pid, long sid, String view )
  {
    if ( myDB == null ) return; // false;
    // TDLog.Log( TDLog.LOG_DB, "update PlotView: " + pid + "/" + sid + " view " + view );
    if ( view == null ) view = TDString.EMPTY;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE plots set view=\"%s\" WHERE surveyId=%d AND id=%d", view, sid, pid );
    doExecSQL( sw, "plt view" );

    // if ( updatePlotViewStmt == null ) {
    //   updatePlotViewStmt = myDB.compileStatement( "UPDATE plots set view=? WHERE surveyId=? AND id=?" );
    // }
    // updatePlotViewStmt.bindString( 1, view );
    // updatePlotViewStmt.bindLong( 2, sid );
    // updatePlotViewStmt.bindLong( 3, pid );
    // doStatement( updatePlotViewStmt, "plt view" );
  }

  // for X-sections hide stores the value of incercept TT
  void updatePlotIntercept( long pid, long sid, float intercept )
  {
    if ( myDB == null ) return; // false;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE plots set intercept=%f WHERE surveyId=%d AND id=%d", intercept, sid, pid );
    doExecSQL( sw, "plt intercept" );
  }

  float selectPlotIntercept( long pid, long sid )
  {
    if ( myDB == null ) return -1;
    float ret = -1;
    Cursor cursor = myDB.query( PLOT_TABLE,
       		         new String[] { "intercept" }, // columns
                                WHERE_SID_ID,
                                new String[] { Long.toString(sid), Long.toString(pid) },
                                null, null, null );
    if ( cursor.moveToFirst() ) {
      ret = (float)cursor.getDouble(0);
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  void updatePlotHide( long pid, long sid, String hide )
  {
    if ( myDB == null ) return; // false;
    // TDLog.Log( TDLog.LOG_DB, "update PlotHide: " + pid + "/" + sid + " hide " + hide );
    if ( hide == null ) hide = TDString.EMPTY;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE plots set hide=\"%s\" WHERE surveyId=%d AND id=%d", hide, sid, pid );
    doExecSQL( sw, "plt hide" );

    // if ( updatePlotHideStmt == null ) {
    //   updatePlotHideStmt = myDB.compileStatement( "UPDATE plots set hide=? WHERE surveyId=? AND id=?" );
    // }
    // updatePlotHideStmt.bindString( 1, hide );
    // updatePlotHideStmt.bindLong( 2, sid );
    // updatePlotHideStmt.bindLong( 3, pid );
    // doStatement( updatePlotHideStmt, "plt hide" );
  }
   
  /** DROP is a real record delete from the database table
   */
  void dropPlot( long pid, long sid )
  {
    if ( myDB == null ) return;
    try {
      myDB.beginTransaction();
      myDB.delete( PLOT_TABLE, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(pid) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("plt drop", e);
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
    try {
      myDB.beginTransaction();
      myDB.delete( PLOT_TABLE, WHERE_SID_NAME, new String[]{ Long.toString(sid), name } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("plt dropN", e);
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
    if ( cursor.moveToFirst() && cursor.getLong(0) == PlotType.PLOT_PLAN ) {
      updateStatus( PLOT_TABLE, pid+1, sid, TDStatus.NORMAL );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
  }
  

  // ----------------------------------------------------------------------
  // SELECT STATEMENTS

  private static final String qShotStations = "select fStation, tStation from shots where surveyId=? AND id=? ";
  private static final String qShotsByStations = "select id, distance, bearing, clino from shots where surveyId=? AND status=0 AND fStation=? AND tStation=? ";
  private static final String qSensors1     = "select id, shotId, title, date, comment, type, value from sensors where surveyId=? AND status=? ";
  private static final String qSensors2     = "select id, shotId, title, date, comment, type, value from sensors where surveyId=? AND shotId=? ";
  private static final String qShotAudio    = "select id, date from audios where surveyId=? AND shotId=? ";
  private static final String qAudiosAll    = "select id, shotId, date from audios where surveyId=? ";
  private static final String qPhotosAll    = "select id, shotId, status, title, date, comment, camera from photos where surveyId=? ";
  private static final String qjPhotos      =
    "select p.id, s.id, p.title, s.fStation, s.tStation, p.date, p.comment, p.camera from photos as p join shots as s on p.shotId=s.id where p.surveyId=? and s.surveyId=? and p.status=? ";
  // private static String qShotPhoto    = "select id, shotId, title, date, comment from photos where surveyId=? AND shotId=? ";
  private static final String qjShotPhoto   =
    "select p.id, s.id, p.title, s.fStation, s.tStation, p.date, p.comment, p.camera from photos as p join shots as s on p.shotId=s.id where p.surveyId=? AND s.surveyId=? AND p.shotId=? ";

  private static final String qFirstStation = "select fStation from shots where surveyId=? AND fStation!=\"\" AND tStation!=\"\" limit 1 ";
  private static final String qHasStation   = "select id, fStation, tStation from shots where surveyId=? and ( fStation=? or tStation=? ) order by id ";
  private static final String qHasPlot      = "select id, name from plots where surveyId=? AND name=? order by id ";
  private static final String qMaxPlotIndex = "select id, name from plots where surveyId=? AND type=? order by id ";
  private static final String qHasShot      = "select fStation, tStation from shots where surveyId=? AND ( ( fStation=? AND tStation=? ) OR ( fStation=? AND tStation=? ) )";
  private static final String qHasShotId    = "select surveyId, id from shots where surveyId=? AND id=?";
  private static final String qNextStation  = "select tStation from shots where surveyId=? AND fStation=? ";
  private static final String qLastStation  = "select fStation, tStation from shots where surveyId=? order by id DESC ";
  private static final String qHasFixedStation = "select id from fixeds where surveyId=? and station=? and id!=? and status=0 ";
  private static final String qjShots       =
    "select s.flag, s.distance, s.fStation, s.tStation, s.clino, z.clino, s.extend from shots as s join shots as z on z.fStation=s.tStation where s.surveyId=? AND z.surveyId=? AND s.fStation!=\"\" AND s.tStation!=\"\" AND s.status=0 ";

  List< SensorInfo > selectAllSensors( long sid, long status )
  {
    List< SensorInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( SENSOR_TABLE,
    //    		         new String[] { "id", "shotId", "title", "date", "comment", "type", "value" }, // columns
    //                             WHERE_SID_STATUS,
    //                             new String[] { Long.toString(sid), Long.toString(status) },
    //                             null,  // groupBy
    //                             null,  // having
    //                             null ); // order by
    Cursor cursor = myDB.rawQuery( qSensors1, new String[] { Long.toString(sid), Long.toString(status) } );
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
      // cursor = myDB.query( SHOT_TABLE, new String[] { "fStation", "tStation" }, WHERE_SID_ID, where, null, null, null );
      cursor = myDB.rawQuery( qShotStations, where );
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
    // Cursor cursor = myDB.query( SENSOR_TABLE,
    //                             new String[] { "id", "shotId", "title", "date", "comment", "type", "value" }, // columns
    //                             "surveyId=? AND shotId=?", 
    //                             new String[] { Long.toString(sid), Long.toString(shotid) },
    //                             null,  // groupBy
    //                             null,  // having
    //                             null ); // order by
    Cursor cursor = myDB.rawQuery( qSensors1, new String[] { Long.toString(sid), Long.toString(shotid) } );
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
      // cursor = myDB.query( SHOT_TABLE, new String[] { "fStation", "tStation" }, WHERE_SID_ID, where, null, null, null );
      cursor = myDB.rawQuery( qShotStations, where );
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
    // Cursor cursor = myDB.query( AUDIO_TABLE,
    //                             new String[] { "id", "date" }, // columns
    //                             WHERE_SID_SHOTID, new String[] { Long.toString(sid), Long.toString(bid) },
    //                             null, null,  null ); 
    Cursor cursor = myDB.rawQuery( qShotAudio, new String[] { Long.toString(sid), Long.toString(bid) } );
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
    long id = minId( AUDIO_TABLE, sid );
    // TDLog.v( "Min audio id " + id );
    insertAudio( sid, id, 0, TDUtil.currentDate() );
    return id;
  }

  private ContentValues makeAudioContentValues( long sid, long id, long bid, String date )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId", sid );
    cv.put( "id",       id );
    cv.put( "shotId",   bid );
    cv.put( "date",     date );
    return cv;
  }

  private long insertAudio( long sid, long id, long bid, String date )
  {
    if ( myDB == null ) return -1L;
    if ( id == -1L ) id = maxId( AUDIO_TABLE, sid );
    ContentValues cv = makeAudioContentValues( sid, id, bid, date );
    if ( ! doInsert( AUDIO_TABLE, cv, "insert audio" ) ) return -1L;
    return id;
  }

  // update or insert audio record
  // @param sid  survey id
  // @param bid  shot-id or sketch audio index
  // @param date date
  void setAudio( long sid, long bid, String date )
  {
    if ( myDB == null ) return; // false;
    // boolean ret = false;
    String[] args = new String[] { Long.toString(sid), Long.toString(bid) };
    Cursor cursor = myDB.query( AUDIO_TABLE, new String[] { "date" }, WHERE_SID_SHOTID, args, null, null,  null ); 
    if (cursor.moveToFirst()) { // update
      String where = "WHERE surveyId=? AND shotId=?";
      ContentValues cv = new ContentValues();
      cv.put( "date", date );
      // ret = ( myDB.update( AUDIO_TABLE, cv, WHERE_SID_SHOTID, args ) > 0 );
      myDB.update( AUDIO_TABLE, cv, WHERE_SID_SHOTID, args );

      // updateAudioStmt.bindString( 1, date );
      // updateAudioStmt.bindString( 2, Long.toString(sid) );
      // updateAudioStmt.bindString( 3, Long.toString(bid) );
      // ret = doStatement( updateAudioStmt, "audio update" );
    } else { // insert
      // ret = ( insertAudio( sid, -1L, bid, date ) >= 0 );
      insertAudio( sid, -1L, bid, date );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    // return ret;
  }

  List< AudioInfo > selectAllAudios( long sid )
  {
    List< AudioInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( AUDIO_TABLE,
    //                             new String[] { "id", "shotId", "date" }, // columns
    //                             WHERE_SID, new String[] { Long.toString(sid) },
    //                             null, null,  null ); 
    Cursor cursor = myDB.rawQuery( qAudiosAll, new String[] { Long.toString(sid) } );
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

  // @param sid survey id
  // @param bid shot-id is positive, sketch audio index if negative
  void deleteAudio( long sid, long bid )
  {
    if ( myDB == null ) return;
    try {
      myDB.delete( AUDIO_TABLE, WHERE_SID_SHOTID, new String[]{ Long.toString(sid), Long.toString(bid) } );
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("audio delete", e); }
  }

  // delete a neg audio record 
  // @param sid   survey id
  // @param id    audio id (neg)
  void deleteNegAudio( long sid, long id )
  {
    if ( myDB == null ) return;
    try {
      myDB.delete( AUDIO_TABLE, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e ); 
    } catch (SQLiteException e) { logError("photo delete", e); }
  }

  //
  // select p.id, p.shotId, p.title, s.fStation, s.tStation, p.date, p.comment from photos as p join shots as s on p.shotId=s.id where p.surveyId=? and s.surveyId=? and p.status=?
  //
  List< PhotoInfo > selectAllPhotos( long sid, long status )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( PHOTO_TABLE,
    //                             new String[] { "id", "shotId", "title", "date", "comment", "camera" }, // columns
    //                             WHERE_SID_STATUS, new String[] { Long.toString(sid), Long.toString(status) },
    //                             null, null,  null ); 
    Cursor cursor = myDB.rawQuery( qjPhotos, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(status) } );
    if (cursor.moveToFirst()) {
      do {
        String name = cursor.getString(3) + "-" + cursor.getString(4);
        list.add( new PhotoInfo( sid, 
                                 cursor.getLong(0), // id
                                 cursor.getLong(1),
                                 cursor.getString(2),
                                 name,              // shot name
                                 cursor.getString(5),
                                 cursor.getString(6),
                                 (int)(cursor.getLong(7))
                 ) );
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Photos list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

    // String[] where = new String[2];
    // where[0] = Long.toString(sid);
    // for ( PhotoInfo pi : list ) { // fill in the shot-name of the photos
    //   where[1] = Long.toString( pi.shotid );
    //   // cursor = myDB.query( SHOT_TABLE, new String[] { "fStation", "tStation" }, WHERE_SID_ID, where, null, null, null );
    //   cursor = myDB.rawQuery( qShotStations, where );
    //   if (cursor.moveToFirst()) {
    //     pi.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
    //   }
    //   if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    // }

    return list;
  }

  List< PhotoInfo > selectPhotoAtShot( long sid, long shotid )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( PHOTO_TABLE,
    //                             new String[] { "id", "shotId", "title", "date", "comment", "camera" }, // columns
    //                             WHERE_SID_SHOTID, new String[] { Long.toString(sid), Long.toString(shotid) },
    //                             null, null, null ); 
    Cursor cursor = myDB.rawQuery( qjShotPhoto, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(shotid) } );
    if (cursor.moveToFirst()) {
      do {
        String name = cursor.getString(3) + "-" + cursor.getString(4);
        list.add( new PhotoInfo( sid, 
                                 cursor.getLong(0), // id
                                 cursor.getLong(1),
                                 cursor.getString(2),
                                 name,              // shot name
                                 cursor.getString(5),
                                 cursor.getString(6),
                                 (int)(cursor.getLong(7))
                 ) );
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Photos list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

    // String[] where = new String[2];
    // where[0] = Long.toString(sid);
    // for ( PhotoInfo pi : list ) { // fill in the shot-name of the photos
    //   where[1] = Long.toString( pi.shotid );
    //   // cursor = myDB.query( SHOT_TABLE, new String[] { "fStation", "tStation" }, WHERE_SID_ID, where, null, null, null );
    //   cursor = myDB.rawQuery( qShotStations, where );
    //   if (cursor.moveToFirst()) {
    //     pi.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
    //   }
    //   if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    // }

    return list;
  }

  boolean hasFixed( long sid, int status )
  {
    if ( myDB == null ) return false;
    Cursor cursor = myDB.query( FIXED_TABLE,
                                mFixedFields,
                                WHERE_SID_STATUS, new String[] { Long.toString(sid), Long.toString(status) }, 
                                null, null, null );
    boolean ret = cursor.moveToFirst();
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
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
       // Cursor cursor = myDB.query( PLOT_TABLE, new String[]{ "id", "name" },
       //     WHERE_SID_NAME, new String[]{ Long.toString( sid ), name },
       //     null, null, "id" );
       Cursor cursor = myDB.rawQuery( qHasPlot, new String[]{ Long.toString( sid ), name } );
       if (cursor.moveToFirst()) ret = true;
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return ret;
   }

   boolean hasSurveyStation( long sid, String start )
   {
     boolean ret = false;
     if ( myDB != null ) {
       // Cursor cursor = myDB.query( SHOT_TABLE,
       //     new String[]{ "id", "fStation", "tStation" },
       //     "surveyId=? and ( fStation=? or tStation=? )",
       //     new String[]{ Long.toString( sid ), start, start },
       //     null, null, "id" );
       Cursor cursor = myDB.rawQuery( qHasStation, new String[]{ Long.toString( sid ), start, start } );
       if (cursor.moveToFirst()) ret = true;
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return ret;
   }

   String getFirstStation( long sid )
   {
     String ret = null;
     if ( myDB != null ) {
       // Cursor cursor = myDB.query( SHOT_TABLE,
       //     new String[]{ "fStation" },
       //     "surveyId=? and ( fStation!=\"\" and tStation!=\"\" )",
       //     new String[]{ Long.toString( sid ) },
       //     null, null, null ); // limit 1
       Cursor cursor = myDB.rawQuery( qFirstStation, new String[]{ Long.toString( sid ) } );
       if (cursor.moveToFirst()) ret = cursor.getString( 0 );
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     }
     return ret;
   }


   int maxPlotIndex( long sid )
   {
     int ret = 0;
     if ( myDB == null ) return ret;
     // Cursor cursor = myDB.query(PLOT_TABLE, new String[] { "id", "name", "type" },
     //                            WHERE_SID, new String[] { Long.toString(sid) }, 
     //                            null, null, "id" );
     Cursor cursor = myDB.rawQuery( qMaxPlotIndex, new String[] { Long.toString(sid), TDString.ONE } ); // type == 1 (PLOT_PLAN)
     if (cursor.moveToFirst()) {
       do {
         // int type = cursor.getInt(2);     
         // if ( type == PlotType.PLOT_PLAN ) { // FIXME || type == PlotType.PLOT_EXTENDED || type == PlotType.PLOT_PROJECTED
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
         // }
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
     plot.maxscrap = (int)(cursor.getLong(13));
     plot.intercept = (float)(cursor.getDouble(14));
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

   public List< String > selectAllPlotNames( String survey )
   {
     List< String > list = new ArrayList<>();
     if ( myDB == null ) return list;

     Cursor cursor = myDB.query( SURVEY_TABLE, new String[] { "id" }, "name=?", new String[] { survey }, null, null, "id" );
     if ( ! cursor.moveToFirst()) return list;
     String sid = Long.toString( cursor.getLong(0) );
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

     cursor = myDB.query( PLOT_TABLE, new String[] { "name" }, "surveyId=?", new String[] { sid }, null, null, "id" );
     if (cursor.moveToFirst()) {
       do {
         list.add( cursor.getString(0) );
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

  // select all the plot names of a survey to delete shp exports
  List< String > selectPlotNames( long sid )
  {
    if ( myDB == null ) return null;
    ArrayList< String > ret = new ArrayList<>();
    Cursor cursor = myDB.rawQuery( "select name from plots where surveyId=?", new String[] { Long.toString(sid) } );
    if ( cursor.moveToFirst() ) {
      do {
        ret.add( cursor.getString( 0 ) );
      } while ( cursor.moveToNext() );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    TDUtil.sortStringList( ret );
    return ret;
  }

  // get the origin of the first plot (of type plan, or null if no plots
  String getFirstPlotOrigin( long sid )
  {
    if ( myDB == null ) return null;
    String ret = null;
    // "1" = PLOT_PLAN
    Cursor cursor = myDB.rawQuery( "select start from plots where surveyId=? AND type=? limit 1", new String[] { Long.toString(sid), "1"  } );
    if ( cursor.moveToFirst() ) {
      ret = cursor.getString( 0 );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  public boolean hasShotId( long sid, long id )
  {
    if ( myDB == null ) return false;
    Cursor cursor = myDB.rawQuery( qHasShotId, new String[] { Long.toString(sid), Long.toString(id) } );
    boolean ret = cursor.moveToFirst();
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  boolean hasShot( long sid, String fStation, String tStation )
  {
    if ( myDB == null ) return false;
    // Cursor cursor = myDB.query( SHOT_TABLE,
    //   new String[] { "fStation", "tStation" }, // columns
    //   "surveyId=? and ( ( fStation=? and tStation=? ) or ( fStation=? and tStation=? ) )", 
    //   new String[] { Long.toString(sid), fStation, tStation, tStation, fStation },
    //   null,   // groupBy
    //   null,   // having
    //   null ); // order by
    Cursor cursor = myDB.rawQuery( qHasShot, new String[] { Long.toString(sid), fStation, tStation, tStation, fStation } );
    boolean ret = cursor.moveToFirst();
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }
    
  String nextStation( long sid, String fStation )
  {
    if ( myDB == null ) return null;
    // Cursor cursor = myDB.query( SHOT_TABLE,
    //       new String[] { "tStation" }, // columns
    //       "surveyId=? and fStation=? ", 
    //       new String[] { Long.toString(sid), fStation },
    //       null, null, null );  // groupBy, having, order by
    Cursor cursor = myDB.rawQuery( qNextStation, new String[] { Long.toString(sid), fStation } );
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
  long mergeToNextLeg( DBlock blk, long sid )
  {
    // TDLog.v("DB Merge to next leg " + blk.mId + " < " + blk.mFrom + " - " + blk.mTo + " >" );
    long ret = -1;
    long extend = ExtendType.EXTEND_LEFT;
    long flag   = DBlock.FLAG_SURVEY;
    long leg    = DBlock.BLOCK_MAIN_LEG;
    String comment = "";
    if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( SHOT_TABLE, new String[] { "id", "fStation", "tStation", "extend", "flag", "leg", "comment" },
                                WHERE_SID_ID_MORE, new String[] { Long.toString(sid), Long.toString(blk.mId) },
                                null, null, "id ASC" ); 
                                // null, null, "id ASC", "LIMIT 1" );  // cannot limit
    if (cursor.moveToFirst()) {
      for ( int k = 0; k < 3; ++ k ) {
        String from = cursor.getString(1);
        String to   = cursor.getString(2);
        if ( from.length() > 0 && to.length() > 0 ) {
          ret = cursor.getLong( 0 );
          extend = cursor.getLong( 3 );
          flag   = cursor.getLong( 4 );
          leg    = cursor.getLong( 5 );
          comment = cursor.getString( 6 );
          // TDLog.v("DB < " + from + " - " + to + " > at k " + k + " extend " + extend + " leg " + leg );
          if ( k > 0 ) {
            // `Log.v("DistoX-NEXT_LEG", blk.mId + " clear shot name at id " + ret );
            updateShotNameAndLeg( ret, sid, "", "", LegType.EXTRA );
            // updateShotLeg( ret, sid, LegType.EXTRA ); 
            if ( k == 2 ) { // N.B. if k == 2 must set ShotLeg also to intermediate shot
              if ( cursor.moveToPrevious() ) { // overcautious
                updateShotLeg( cursor.getLong(0), sid, LegType.EXTRA ); 
              }
            }
          }
          updateShotNameAndData( blk.mId, sid, from, to, extend, flag, leg, comment );
          blk.mFrom = from;
          blk.mTo   = to;
          blk.setExtend( (int)extend, 0 );
          blk.resetFlag( flag );
          blk.resetBlockType( (int)leg );
          blk.mComment   = comment;
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
    // TDLog.v( "A6 select one shot id " + id + " block leg " + block.getLegType() );
    return block;
  }

  // this is actually a "select the shot"
  DBlock selectLastShot( long id, long sid )
  {
    // TDLog.Log( TDLog.LOG_DB, "selectShot " + id + "/" + sid );
    if ( myDB == null ) return null;
    Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
                                WHERE_SID_ID, new String[] { Long.toString(sid), Long.toString(id) },
                                null, null, null );
    DBlock block = null;
    if (cursor.moveToLast()) {
      block = new DBlock();
      fillBlock( sid, block, cursor );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    // TDLog.v( "A6 select one shot id " + id + " block leg " + block.getLegType() );
    return block;
  }

  DBlock selectLastLegShot( long sid )
  {
    return selectPreviousLegShot( myNextId+1, sid );
  }

  private DBlock selectPreviousLegShot( long shot_id, long sid )
  {
    // TDLog.Log( TDLog.LOG_DB, "select previous leg shot " + shot_id + "/" + sid );
    // TDLog.v("DATA " + "select previous leg shot " + shot_id + "/" + sid );
    if ( myDB == null ) return null;
    Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
                                "surveyId=? and id<?",
                                new String[] { Long.toString(sid), Long.toString(shot_id) },
                                null, null, "id DESC" );
                                // null, null, "id DESC", "LIMIT 1" ); // cannot limit
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
    // Cursor cursor = myDB.query(SHOT_TABLE,
    //   new String[] { "fStation", "tStation" },
    //   WHERE_SID, new String[] { Long.toString(sid) },
    //   null,  // groupBy
    //   null,  // having
    //   "id DESC" // order by
    // );
    Cursor cursor = myDB.rawQuery( qLastStation, new String[] { Long.toString(sid) } );
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
                                // null, null, "id ASC", "LIMIT 1" ); // cannot limit
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

  List< DBlock > selectShotsBetweenStations( long sid, String st1, String st2, long status )
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

  List< DBlock > selectShotsAfterId( long sid, long id , long status )
  {
    // TDLog.v( "B1 select shots after id " + id );
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
  List< DBlock > selectShotsAt( long sid, String station, boolean leg )
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

  List< DBlock > selectSplaysAt( long sid, String station, boolean leg )
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
  List< DBlock > selectAllShotsAtStations( long sid, String station1, String station2 )
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
        int leg = (int)( cursor.getLong(11) );
        if ( leg == 0 || leg == 2 ) { // skip leg-blocks (11 = "leg" flag): 0==splay, 1==leg, 2==x-splay
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
  List< DBlock > selectAllShotsAtStations( long sid, List< String > stations, boolean with_legs )
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

  private List< DBlock > selectAllShotsAtStation( long sid, String station )
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

  List< DBlock > selectAllShotsToStation( long sid, String station )
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
  // @param id     shot id
  // @param sid    survey id
  Set<String> selectAllStationsBefore( long id, long sid /*, long status */ )
  {
    Set< String > set = new TreeSet<String>();
    if ( myDB == null ) return set;
    Cursor cursor = myDB.query(SHOT_TABLE, new String[] { "fStation", "tStation" },
                    WHERE_ID_SID_LEG, // "id<=? and surveyId=? and fStation ", // and status=?",
                    new String[] { Long.toString(id), Long.toString(sid) /*, Long.toString(status) */ },
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

  List< DBlock > selectAllShotsAfter( long id, long sid, long status )
  {
    // TDLog.v( "B2 select shots after id " + id );
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
                    WHERE_SID_LEG, new String[]{ Long.toString(sid) },
                    null, null, null );
    if (cursor.moveToFirst()) {
      do {
        String f = cursor.getString( 0 );
        String t = cursor.getString( 1 );
        // if ( f == null || t == null ) continue;
        if ( f.length() > 0 && t.length() > 0 ) {
          set.add( f );
          set.add( t );
        }
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return set;
  }

  /** select all shots, used by CSV raw export
   * @param sid surveyId
   */
  List< RawDBlock > selectAllShotsRawData( long sid )
  {
    // TDLog.v( "B3 select shots all");
    List< RawDBlock > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query(SHOT_TABLE, mShotRawDataFields, WHERE_SID, new String[]{ Long.toString(sid) }, null, null, "id" );
    if (cursor.moveToFirst()) {
      do {
        RawDBlock block = new RawDBlock();
        fillBlockRawData( sid, block, cursor );
        list.add( block );
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Shots list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  List< DBlock > selectAllShots( long sid, long status )
  {
    // TDLog.v( "B3 select shots all");
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
    // FIXME catch ( SQLiteCantOpenDatabaseException e )
    return list;
  }

  List< DBlock > selectAllExportShots( long sid, long status )
  {
    // TDLog.v( "B3 select shots all");
    List< DBlock > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query(SHOT_TABLE, mFullShotFields,
                    WHERE_SID_STATUS, new String[]{ Long.toString(sid), Long.toString(status) },
                    null, null, "id" );
    if (cursor.moveToFirst()) {
      do {
        DBlock block = new DBlock();
        fullFillBlock( sid, block, cursor );
        list.add( block );
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Shots list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  // @param backshot  whether the DistoX is in backshot mode
  // @return the last block with either the from station (non-backshot) or the to station (backshot)
  // used only by StationName
  DBlock selectLastNonBlankShot( long sid /* , long status, boolean backshot */ )
  {
    if ( myDB == null ) return null;
    DBlock ret = null;
    Cursor cursor = myDB.query(SHOT_TABLE, mShotFields,
                    // WHERE_SID_STATUS_LEG, new String[]{ Long.toString(sid), Long.toString(status) },
                    WHERE_SID_LEG, new String[]{ Long.toString(sid) },
                    null, null, "id desc", TDString.ONE );
    if (cursor.moveToFirst()) {
      // TDLog.v( "got the last leg " + cursor.getLong(0) + " " + cursor.getString(1) + " - " + cursor.getString(2) );
      DBlock block = new DBlock();
      do { 
        fillBlock( sid, block, cursor );
        if ( block.isDistoXBacksight() ) {
          if ( block.mTo != null && block.mTo.length() > 0 ) { ret = block; break; }
        } else {
          if ( block.mFrom != null && block.mFrom.length() > 0 ) { ret = block; break; }
        }
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  List< DBlock > selectAllLegShots( long sid, long status )
  {
    // TDLog.v( "B4 select shots all leg");
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
                               new String[] { "name", "day", "team", "declination", "comment", "init_station", "xsections", "datamode", "extend" }, // columns
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
      info.datamode  = (int)cursor.getLong( 7 );
      info.mExtend   = (int)cursor.getLong( 8 );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return info;
  }
  // ----------------------------------------------------------------------
  // SELECT: LIST SURVEY / CABIL NAMES

  private List< String > selectAllNames( String table )
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
    TDUtil.sortStringList( list );
    return list;
  }

  public List< String > selectAllSurveys() { return selectAllNames( SURVEY_TABLE ); }

  // ----------------------------------------------------------------------
  // CONFIG DATA

  String getValue( String key )
  {
    if ( myDB == null ) {
      // TDLog.Error( "Data Helper::getValue null DB");
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
    } catch ( SQLiteException e ) { logError( "config update " + key + " " + value, e );
    // } catch ( android.database.CursorWindowAllocationException e ) {
    } finally {
      if ( cursor != null && ! cursor.isClosed()) cursor.close();
    }
    return value;
  }

  void setValue( String key, String value )
  {
    if ( myDB == null ) {
      // TDLog.Error( "Data Helper::setValue null DB");
      return;
    }
    if ( key == null || key.length() == 0 ) {
      TDLog.Error( "Data Helper::setValue null key");
      return;
    }
    if ( value == null || value.length() == 0 ) {
      TDLog.Error( "Data Helper::setValue null value");
      return;
    }

    // TDLog.v("DB set " + key + ": <" + value + ">" );
    Cursor cursor = null;
    try {
      myDB.beginTransaction();
      cursor = myDB.query( CONFIG_TABLE,
                           new String[] { "value" }, // columns
                           "key = ?", new String[] { key },
                           null, null, null );
      if ( cursor != null ) {
        ContentValues cv = new ContentValues();
        cv.put( "key",     key );
        cv.put( "value",   value );
        if (cursor.moveToFirst()) {
          // updateConfig.bindString( 1, value );
          // updateConfig.bindString( 2, key );
          // updateConfig.execute();
          myDB.update( CONFIG_TABLE, cv, "key=?", new String[] { key } );
        } else {
          // cv.put( "key",     key );
          myDB.insert( CONFIG_TABLE, null, cv );
        }
      } else {
        TDLog.Error( "set value cannot get cursor" );
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e ); 
    } catch (SQLiteException e ) { logError( "set value " + key + " " + value, e ); 
    } finally {
      if ( ! (cursor == null) && ! cursor.isClosed()) cursor.close();
      myDB.endTransaction();
    }
  }

  // ----------------------------------------------------------------------
  // symbols

  void setSymbolEnabled( String name, boolean enabled ) { setValue( name, enabled? TDString.ONE : TDString.ZERO ); }

  boolean getSymbolEnabled( String name )
  { 
    String enabled = getValue( name );
    if ( enabled != null ) {
      return enabled.equals(TDString.ONE);
    }
    return false;
  }

  // UNUSED
  // void addSymbolEnabled( String name )
  // {
  //   if ( myDB != null ) {
  //     ContentValues cv = new ContentValues();
  //     cv.put( "key",     name );
  //     cv.put( "value",   TDString.ZERO );     // symbols are enabled by default
  //     try {
  //       myDB.insert( CONFIG_TABLE, null, cv );
  //     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //     } catch ( SQLiteException e ) { logError("config symbol " + name, e ); }
  //   }
  // }

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

  // @return positive ID on success
  //         0 if no database
  //         -1 failure
  private long setName( String table, String name, int datamode )
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
      // TDLog.v( "INSERT INTO " + table + " VALUES: " + id + " " + name + " datamode " + datamode );
      ContentValues cv = new ContentValues();
      cv.put( "id",       id );
      cv.put( "name",     name );
      cv.put( "day",      "" );
      cv.put( "comment",  "" );
      cv.put( "datamode", datamode );
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
    if ( myDB == null ) return "xx0"; // FIXME null
    // TDLog.v( "DB getNextSectionId sid " + sid + " prefix " + prefix );
    Cursor cursor = myDB.query( PLOT_TABLE, 
                new String[] { "id", "type", "name" },
                WHERE_SID,
                new String[] { Long.toString(sid) },
                null, null, null );
    if (cursor != null ) {
      if (cursor.moveToFirst() ) {
        do {
          int type    = cursor.getInt(1);    // type is not used
          String name = cursor.getString(2);
          // TDLog.v( "DB plot name " + name + " prefix " + prefix );
          if ( name.startsWith( prefix ) /* && ( type == PlotType.PLOT_PHOTO || type == PlotType.PLOT_SECTION ) */ ) {
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

  void updatePlotName( long sid, long pid, String name )
  {
    // if ( updatePlotNameStmt == null ) {
    //   updatePlotNameStmt = myDB.compileStatement( "UPDATE plots set name=? WHERE surveyId=? AND id=?" );
    // }
    // updatePlotNameStmt.bindString( 1, name );
    // updatePlotNameStmt.bindLong( 2, sid );
    // updatePlotNameStmt.bindLong( 3, pid );
    // doStatement( updatePlotNameStmt, "plot name" );

    ContentValues cv = new ContentValues();
    cv.put( "name", name );
    doUpdate( PLOT_TABLE, cv, "surveyId=? AND id=?", new String[] { Long.toString(sid), Long.toString(pid) }, "plot name" );
  }

  void updatePlotMaxScrap( long sid, long pid, int maxscrap )
  {
    ContentValues cv = new ContentValues();
    cv.put( "maxscrap", maxscrap );
    doUpdate( PLOT_TABLE, cv, "surveyId=? AND id=?", new String[] { Long.toString(sid), Long.toString(pid) }, "plot maxscrap" );
  }

  void updatePlotOrientation( long sid, long pid, int orient )
  {
    // if ( updatePlotOrientationStmt == null ) {
    //   updatePlotOrientationStmt = myDB.compileStatement( "UPDATE plots set orientation=? WHERE surveyId=? AND id=?" );
    // }
    // updatePlotOrientationStmt.bindLong( 1, orient );
    // updatePlotOrientationStmt.bindLong( 2, sid );
    // updatePlotOrientationStmt.bindLong( 3, pid );
    // doStatement( updatePlotOrientationStmt, "plot orientation" );

    ContentValues cv = new ContentValues();
    cv.put( "orientation", orient );
    doUpdate( PLOT_TABLE, cv, "surveyId=? AND id=?", new String[] { Long.toString(sid), Long.toString(pid) }, "plot orient" );
  }

  void updatePlotAzimuthClino( long sid, long pid, float b, float c )
  {
    // if ( updatePlotAzimuthClinoStmt == null ) {
    //   updatePlotAzimuthClinoStmt = myDB.compileStatement( "UPDATE plots set azimuth=?, clino=? WHERE surveyId=? AND id=?" );
    // }
    // updatePlotAzimuthClinoStmt.bindDouble( 1, b );
    // updatePlotAzimuthClinoStmt.bindDouble( 2, c );
    // updatePlotAzimuthClinoStmt.bindLong( 3, sid );
    // updatePlotAzimuthClinoStmt.bindLong( 4, pid );
    // return doStatement( updatePlotAzimuthClinoStmt, "plot azi+clino" );

    ContentValues cv = new ContentValues();
    cv.put( "azimuth", b );
    cv.put( "clino", c );
    doUpdate( PLOT_TABLE, cv, "surveyId=? AND id=?", new String[] { Long.toString(sid), Long.toString(pid) }, "plot dir" );
    // return true; // FIXME
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
  private ContentValues makePhotoContentValues( long sid, long id, long shotid, long status, String title, String date, String comment, long camera )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId",  sid );
    cv.put( "id",        id );
    cv.put( "shotId",    shotid );
    cv.put( "status",    status );
    cv.put( "title",     title );
    cv.put( "date",      date );
    cv.put( "comment",   (comment == null)? TDString.EMPTY : comment );
    cv.put( "camera",    camera );
    return cv;
  }

  void insertPhoto( long sid, long id, long shotid, String title, String date, String comment, int camera )
  {
    if ( myDB == null ) return; // -1L;
    if ( id == -1L ) id = maxId( PHOTO_TABLE, sid );
    ContentValues cv = makePhotoContentValues( sid, id, shotid, TDStatus.NORMAL, title, date, comment, camera );
    doInsert( PHOTO_TABLE, cv, "photo insert" );
    // if ( ! doInsert( PHOTO_TABLE, cv, "photo insert" ) ) return -1L;
    // return id;
  }

  long nextPhotoId( long sid )
  {
    return maxId( PHOTO_TABLE, sid );
  }

  boolean updatePhoto( long sid, long id, String comment )
  {
    if ( myDB == null ) return false;
    ContentValues cv = new ContentValues();
    cv.put( "comment", comment );
    try {
      myDB.beginTransaction();
      myDB.update( PHOTO_TABLE, cv, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("photo update", e); 
    } finally { myDB.endTransaction(); }
    return true;
  }

  void deletePhoto( long sid, long id )
  {
    if ( myDB == null ) return;
    try {
      myDB.beginTransaction();
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
  private ContentValues makeSensorContentValues( long sid, long id, long shotid, long status,
       	   String title, String date, String comment, String type, String value )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId",  sid );
    cv.put( "id",        id );
    cv.put( "shotId",    shotid );
    cv.put( "status",    status );
    cv.put( "title",     title );
    cv.put( "date",      date );
    cv.put( "comment",   (comment == null)? TDString.EMPTY : comment );
    cv.put( "type",      type );
    cv.put( "value",     value );
    return cv;
  }

  void insertSensor( long sid, long id, long shotid, String title, String date, String comment, String type, String value )
  {
    if ( myDB == null ) return; // -1L;
    if ( id == -1L ) id = maxId( SENSOR_TABLE, sid );
    ContentValues cv = makeSensorContentValues( sid, id, shotid, TDStatus.NORMAL, title, date, comment, type, value );
    doInsert( SENSOR_TABLE, cv, "sensor insert" );
    // if ( ! doInsert( SENSOR_TABLE, cv, "sensor insert" ) ) return -1L;
    // return id;
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
    ContentValues cv = new ContentValues();
    cv.put( "comment", comment );
    try {
      myDB.beginTransaction();
      myDB.update( SENSOR_TABLE, cv, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
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
  

  // myDB is checked non-null before transfer methods are called
  //
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
  public long insertFixed( long sid, long id, String station, double lng, double lat, double alt, double asl,
                           String comment, long status, long source )
  {
    return insertFixed( sid, id, station, lng, lat, alt, asl, comment, status, source, "", 0, 0, 0, 2 );
  }

  private ContentValues makeFixedContentValues( long sid, long id, String station, double lng, double lat, double alt, double asl,
                           String comment, long status, long source,
                           String cs, double cs_lng, double cs_lat, double cs_alt, long cs_n_dec )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId",  sid );
    cv.put( "id",        id );
    cv.put( "station",   station );
    cv.put( "longitude", lng );
    cv.put( "latitude",  lat );
    cv.put( "altitude",  alt );
    cv.put( "altimetric", asl );
    cv.put( "comment",   (comment == null)? TDString.EMPTY : comment );
    cv.put( "status",    status );
    cv.put( "cs_name",   cs );
    cv.put( "cs_longitude", cs_lng );
    cv.put( "cs_latitude",  cs_lat );
    cv.put( "cs_altitude",  cs_alt );
    cv.put( "source",     source );
    cv.put( "cs_decimals", cs_n_dec );
    return cv;
  }

  private long insertFixed( long sid, long id, String station, double lng, double lat, double alt, double asl,
                           String comment, long status, long source,
                           String cs, double cs_lng, double cs_lat, double cs_alt, long cs_n_dec )
  {
    // TDLog.v( "insert fixed id " + id + " station " + station );
    if ( id != -1L ) return id;
    if ( myDB == null ) return -1L;
    long fid = getFixedId( sid, station );
    if ( fid != -1L ) return fid;     // check non-deleted fixeds
    dropDeletedFixed( sid, station ); // drop deleted fixed if any

    id = maxId( FIXED_TABLE, sid );
    // TDLog.Log( TDLog.LOG_DB, "insert Fixed id " + id );
    ContentValues cv = makeFixedContentValues( sid, id, station, lng, lat, alt, asl, comment, status, source,
       	     cs, cs_lng, cs_lat, cs_alt, cs_n_dec );
    if ( ! doInsert( FIXED_TABLE, cv, "insert fixed" ) ) return -1L;
    return id;
  }

  private ContentValues makePlotContentValues( long sid, long id, String name, long type, long status, String start, String view,
                          double xoffset, double yoffset, double zoom, double azimuth, double clino,
                          String hide, String nick, int orientation, int maxscrap, double intercept )
  {
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
    cv.put( "maxscrap",    maxscrap );
    cv.put( "intercept",   intercept );
    return cv;
  }

  long insertPlot( long sid, long id, String name, long type, long status, String start, String view,
                          double xoffset, double yoffset, double zoom, double azimuth, double clino,
                          String hide, String nick, int orientation )
  {
    // TDLog.v( "DB insert plot " + name + " start " + start + " azimuth " + azimuth );
    // TDLog.v( "insert plot <" + name + "> hide <" + hide + "> nick <" + nick + ">" );
    if ( myDB == null ) return -1L;
    long ret = getPlotId( sid, name );
    if ( ret >= 0 ) return -1;
    if ( view == null ) view = TDString.EMPTY;
    if ( id == -1L ) id = maxId( PLOT_TABLE, sid );
    // maxscrap = 0
    ContentValues cv = makePlotContentValues( sid, id, name, type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation, 0, -1 );
    if ( ! doInsert( PLOT_TABLE, cv, "plot insert" ) ) { // failed
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

  // used only for AUDIO table to get negative indices
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
    return id - 1L; // decrement
  }
  
  boolean moveShotsBetweenSurveys( long old_sid, long old_id, long new_sid )
  {
    boolean ret = false;
    long offset = getLastShotId( new_sid ) + 1 - old_id;
    // update shots set id=id+offset, surveyId=new_sid where surveyId=old_sid && id >= old_id;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE shots SET id=id+%d, surveyId=%d WHERE surveyId=%d AND id>=%d", offset, new_sid, old_sid, old_id );
    // TDLog.v("DB " + sw.toString() );
    try {
      myDB.beginTransaction();
      myDB.execSQL( sw.toString() );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError("move shots", e1 );
    } catch ( IllegalStateException e2 ) { logError("move shots", e2 );
    } finally { myDB.endTransaction(); }
    return ret;
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
      // Cursor cursor = myDB.query( FIXED_TABLE,
      //     new String[]{ "id" },
      //     "surveyId=? and station=? and id!=? and status=0",  // 0 == TDStatus.NORMAL
      //     new String[]{ Long.toString( sid ), station, Long.toString(id) },
      //     null, null, null );
      Cursor cursor = myDB.rawQuery( qHasFixedStation, new String[]{ Long.toString( sid ), station, Long.toString(id) } );
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          // do {
          //   if (cursor.getLong( 0 ) != id) ret = true;
          // } while (cursor.moveToNext());
	  ret = true;
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
    try {
      myDB.beginTransaction();
      myDB.delete( FIXED_TABLE, "surveyId=? and station=? and status=1", new String[]{ Long.toString(sid), station } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("fixed delete", e ); 
    } finally { myDB.endTransaction(); }
  }
  

  // FIXME DBCHECK
  boolean updateFixedStation( long id, long sid, String station )
  {
    // TDLog.v( "update fixed id " + id + " station " + station );
    boolean ret = false;
    if ( ! hasFixedStation( id, sid, station ) ) {
      dropDeletedFixed( sid, station ); // drop deleted fixed at station, if any

      ContentValues cv = new ContentValues();
      cv.put( "station", station );
      ret = doUpdate( FIXED_TABLE, cv, sid, id, "fix updt" );
    }
    return ret;
  }

  void updateFixedStatus( long id, long sid, long status )
  {
    updateStatus( FIXED_TABLE, id, sid, status );
  }

  void updateFixedStationComment( long id, long sid, String station, String comment )
  {
    ContentValues cv = new ContentValues();
    cv.put( "station", station );
    cv.put( "comment", comment );
    doUpdate( FIXED_TABLE, cv, sid, id, "fix cmt" );
  }

  void updateFixedAltitude( long id, long sid, double alt, double asl )
  {
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    cv.put( "altitude",   alt );
    cv.put( "altimetric", asl );
    doUpdate( FIXED_TABLE, cv, sid, id, "fix alt" );
  }

  void updateFixedData( long id, long sid, double lng, double lat, double alt )
  {
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    cv.put( "longitude", lng );
    cv.put( "latitude",  lat );
    cv.put( "altitude",  alt );
    doUpdate( FIXED_TABLE, cv, sid, id, "fix dat1" );
  }

  void updateFixedData( long id, long sid, double lng, double lat, double alt, double asl )
  {
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    cv.put( "longitude", lng );
    cv.put( "latitude",  lat );
    cv.put( "altitude",  alt );
    cv.put( "altimetric", asl );
    doUpdate( FIXED_TABLE, cv, sid, id, "fix dat2" );
  }

  void updateFixedCS( long id, long sid, String cs, double lng, double lat, double alt, long n_dec )
  {
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    if ( cs != null && cs.length() > 0 ) {
      cv.put( "cs_name", cs );
      cv.put( "cs_longitude", lng );
      cv.put( "cs_latitude",  lat );
      cv.put( "cs_altitude",  alt );
      cv.put( "cs_decimals",  n_dec );
    } else {
      cv.put( "cs_name", TDString.EMPTY );
      cv.put( "cs_longitude", 0 );
      cv.put( "cs_latitude",  0 );
      cv.put( "cs_altitude",  0 );
      cv.put( "cs_decimals",  2 );
    }
    doUpdate( FIXED_TABLE, cv, sid, id, "fix cs" );
  }

  public boolean hasSurveyName( String name )  { return hasName( name, SURVEY_TABLE ); }

  public boolean hasSurveyPlotName( long sid, String name )  { return hasName( sid, name, PLOT_TABLE ); }

  private boolean hasName( String name, String table )
  {
    if ( myDB == null ) {
      TDLog.Error( DeviceHelper.ERROR_NULL_DB + "DB data has name");
      return false;
    }
    boolean ret = false;
    Cursor cursor = null;
    try {
      String query = String.format("SELECT name FROM %s WHERE name='%s' COLLATE NOCASE", table, name );
      cursor = myDB.rawQuery( query, new String[] { } );
      ret = ( cursor != null && cursor.moveToFirst() ); 
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  private boolean hasName( long sid, String name, String table )
  {
    if ( myDB == null ) {
      TDLog.Error( DeviceHelper.ERROR_NULL_DB + "DB data has name");
      return false;
    }
    boolean ret = false;
    Cursor cursor = null;
    try {
      String query = String.format( Locale.US,
        "SELECT name FROM %s WHERE surveyId=%d AND ( name='%sp' COLLATE NOCASE OR name='%ss' COLLATE NOCASE)", 
        table, sid, name, name );
      cursor = myDB.rawQuery( query, new String[] { } );
      ret = (cursor != null && cursor.moveToFirst() );
      // TDLog.v( query + " " + ret );
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  // @return positive ID on success
  //         0 or -1 failure
  long setSurvey( String name, int datamode )
  {
    myNextId = 0;
    if ( myDB == null ) return 0L;
    long sid = setName( SURVEY_TABLE, name, datamode );
    Cursor cursor = myDB.query( SHOT_TABLE, new String[] { "max(id)" },
                         "surveyId=?", new String[] { Long.toString(sid) },
                         null, null, null );
    if (cursor.moveToFirst() ) {
      myNextId = cursor.getLong(0);
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return sid;
  }
   
  // get survey id (-1L on error)
  long getSurveyId( String name )
  {
    if ( myDB == null ) return -1L;
    long id = -1L;
    Cursor cursor = myDB.query( "surveys", new String[] { "id" },
                                "name = ?", new String[] { name },
                                null, null, null );
    if (cursor.moveToFirst() ) {
      id = cursor.getLong(0);
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return id;
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
/* FIXME_SKETCH_3D *
  void updateSketch( long sketch_id, long sid,
                            String st1, String st2,
                            double xofftop, double yofftop, double zoomtop,
                            double xoffside, double yoffside, double zoomside,
                            double xoff3d, double yoff3d, double zoom3d,
                            double east, double south, double vert, double azimuth, double clino )
  {
    if ( myDB == null ) return; // false;
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
    doStatement( updateSketchStmt, "sketch" );
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
     if ( // cursor != null && 
          !cursor.isClosed()) cursor.close();
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
       if ( // cursor != null && 
            !cursor.isClosed()) cursor.close();
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

   private ContentValues makeSketch3dContentValues( long sid, long id, String name, long status, String start, String st1, String st2,
                           double xoffsettop, double yoffsettop, double zoomtop,
                           double xoffsetside, double yoffsetside, double zoomside,
                           double xoffset3d, double yoffset3d, double zoom3d,
                           double x, double y, double z, double azimuth, double clino )
   {
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
     return cv;
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
     // TDLog.v( "DB Survey ID " + sid + " Sketch ID " + id );
     ContentValues cv = makeSketch3dContentValues( sid, id, name, status, start, st1, st2, xoffsettop, yoffsettop, zoomtop,
		     xoffsetside, yoffsetside, zoomside, xoffset3d, yoffset3d, zoom3d, x, y, z, azimuth, clino );
     if ( ! doInsert( SKETCH_TABLE, cv, "sketch insert" ) ) return -1L;
     return id;
   }
 * END SKETCH_3D */
      
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
     // TDLog.Log( TDLog.LOG_IO, "dump DB to file " + filename );
     // String where = "surveyId=" + Long.toString(sid);
     if ( myDB == null ) return;
     try {
       TDPath.checkPath( filename );
       FileWriter fw = TDFile.getFileWriter( filename ); // DistoX-SAF
       PrintWriter pw = new PrintWriter( fw );
       Cursor cursor = myDB.query( SURVEY_TABLE, 
                            new String[] { "name", "day", "team", "declination", "comment", "init_station", "xsections", "datamode", "extend" },
                            "id=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, \"%s\", \"%s\", \"%s\", %.4f, \"%s\", \"%s\", %d, %d, %d );\n",
                     SURVEY_TABLE,
                     sid,
                     TDString.escape( cursor.getString(0) ),
                     TDString.escape( cursor.getString(1) ),
                     TDString.escape( cursor.getString(2) ),
                     cursor.getDouble(3),     // declination
                     TDString.escape( cursor.getString(4) ),     // comment
                     TDString.escape( cursor.getString(5) ),     // init_station
                     (int)cursor.getLong(6),  // xstation
                     (int)cursor.getLong(7),  // datamode
                     (int)cursor.getLong(8)   // extend
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       // cursor = myDB.query( AUDIO_TABLE, // SELECT ALL AUDIO RECORD
       //                      new String[] { "id", "shotId", "date" },
       //                      "surveyId=?", new String[] { Long.toString(sid) },
       //                      null, null, null );
       cursor = myDB.rawQuery( qAudiosAll, new String[] { Long.toString(sid) } );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, %d, \"%s\" );\n",
                     AUDIO_TABLE,
                     sid,
                     cursor.getLong(0),   // id
                     cursor.getLong(1),   // shotid
                     TDString.escape( cursor.getString(2) ) // date
                    );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       // cursor = myDB.query( PHOTO_TABLE, // SELECT ALL PHOTO RECORD
       //  		    new String[] { "id", "shotId", "status", "title", "date", "comment", "camera" },
       //                      "surveyId=?", new String[] { Long.toString(sid) },
       //                      null, null, null );
       cursor = myDB.rawQuery( qPhotosAll, new String[] { Long.toString(sid) } );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, %d, %d, \"%s\", \"%s\", \"%s\", %d );\n",
                     PHOTO_TABLE,
                     sid,
                     cursor.getLong(0),   // id
                     cursor.getLong(1),   // shotid
                     cursor.getLong(2),   // status
                     TDString.escape( cursor.getString(3) ), // title
                     TDString.escape( cursor.getString(4) ), // date
                     TDString.escape( cursor.getString(5) ), // comment
                     cursor.getLong(6)
           );
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
             "INSERT into %s values( %d, %d, \"%s\", %d, %d, \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %.2f, \"%s\", \"%s\", %d, %d, %.2f );\n",
             PLOT_TABLE,
             sid,
             cursor.getLong(0),    // plot id
             TDString.escape( cursor.getString(1) ),  // name
             cursor.getLong(2),    // type
             cursor.getLong(3),    // status
             TDString.escape( cursor.getString(4) ),  // start
             TDString.escape( cursor.getString(5) ),  // view
             cursor.getDouble(6),  // X offset
             cursor.getDouble(7),
             cursor.getDouble(8),  // zoom
             cursor.getDouble(9),  // azimuth
             cursor.getDouble(10), // clino
             TDString.escape( cursor.getString(11) ), // hide
             TDString.escape( cursor.getString(12) ), // nick
             cursor.getLong(13),   // orientation
             cursor.getLong(14),   // maxscrap
             cursor.getDouble(15)  // intercept
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
       /* FIXME_SKETCH_3D *
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
                     TDString.escape( cursor.getString(1) ),
                     cursor.getLong(2),
                     TDString.escape( cursor.getString(3) ),
                     TDString.escape( cursor.getString(4) ),
                     TDString.escape( cursor.getString(5) ),
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
       if ( // cursor != null && 
            !cursor.isClosed()) cursor.close();
       * END_SKETCH_3D */

       cursor = myDB.query( SHOT_TABLE, 
                            new String[] { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll",
                                           "acceleration", "magnetic", "dip",
                                           "extend", "flag", "leg", "status", "comment", "type", "millis", "color", "stretch", "address" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %d, %d, %d, %d, \"%s\", %d, %d, %d, %.2f, \"%s\" );\n",
                     SHOT_TABLE,
                     sid,
                     cursor.getLong(0),
                     TDString.escape( cursor.getString(1) ),
                     TDString.escape( cursor.getString(2) ),
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
                     TDString.escape( cursor.getString(14) ),  // comment
                     cursor.getLong(15),    // type
                     cursor.getLong(16),    // millis
                     0, // cursor.getLong(17) // COLOR is not exported
		     cursor.getDouble(18),  // stretch
                     TDString.escape( cursor.getString(19) )   // address
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
             "INSERT into %s values( %d, %d, \"%s\", %.6f, %.6f, %.2f, %.2f \"%s\", %d, %d, \"%s\", %.6f, %.6f, %.1f, %d, %d );\n",
             FIXED_TABLE,
             sid,
             cursor.getLong(0),
             TDString.escape( cursor.getString(1) ),
             cursor.getDouble(2),
             cursor.getDouble(3),
             cursor.getDouble(4),
             cursor.getDouble(5),
             TDString.escape( cursor.getString(6) ),
             cursor.getLong(7),    // status
             cursor.getLong(8),    // source
             TDString.escape( cursor.getString(9) ), // cs_name
             cursor.getDouble(10), // cs longitude
             cursor.getDouble(11), // cs latitude
             cursor.getDouble(12), // cs altitude
             cursor.getLong(8),    // source type
             cursor.getLong(13)    // cs decimals
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
             TDString.escape( cursor.getString(0) ),
             TDString.escape( cursor.getString(1) ),
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
                     TDString.escape( cursor.getString(3) ),
                     TDString.escape( cursor.getString(4) ),
                     TDString.escape( cursor.getString(5) ),
                     TDString.escape( cursor.getString(6) ),
                     TDString.escape( cursor.getString(7) )
                    );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       fw.flush();
       fw.close();
     } catch ( FileNotFoundException e ) {// DistoX-SAF
     } catch ( IOException e ) {// FIXME
     }
   }

   /** load survey data from a sql file
    * @param filename  name of the sql file
    */
   long loadFromFile( String filename, int db_version )
   {
     boolean success = false; // whether the load is successful
     long sid = -1;
     long id, status, shotid;
     String station, title, date, name, comment;
     String line;
     try {
       // TDLog.Log( TDLog.LOG_IO, "load survey from sql file " + filename );
       FileReader fr = TDFile.getFileReader( filename ); // DistoX-SAF
       BufferedReader br = new BufferedReader( fr );
       // first line is survey
       line = br.readLine();
       // TDLog.Log( TDLog.LOG_DB, "load from file: " + line );
       // TDLog.v( "DB load: " + line );
       String[] vals = line.split(" ", 4); 
       // if ( vals.length != 4 ) { TODO } // FIXME
       String table = vals[2];
       String v = vals[3];
       Scanline scanline0 = new Scanline( v, v.indexOf('(')+1, v.lastIndexOf(')') );
       // pos = v.indexOf( '(' ) + 1;
       // len = v.lastIndexOf( ')' );
       // scanline0.skipSpaces( );
       if ( table.equals(SURVEY_TABLE) ) { 
         long skip_sid = scanline0.longValue( -1 );
         name          = TDString.unescape( scanline0.stringValue( ) );
         date          = TDString.unescape( scanline0.stringValue( ) );
         String team   = TDString.unescape( scanline0.stringValue( ) );
         double decl   = ( db_version > 14 )? scanline0.doubleValue( 0.0 ) : 0.0;
         comment       = TDString.unescape( scanline0.stringValue( ) );
         String init_station = ( db_version > 22)? TDString.unescape( scanline0.stringValue( ) ) : "0";
         int xsections = ( db_version > 29)? (int)( scanline0.longValue( SurveyInfo.XSECTION_SHARED ) )
                                           : SurveyInfo.XSECTION_SHARED; // old at-station x-sections were "shared"
	 int datamode  = ( db_version > 36)? (int)( scanline0.longValue( SurveyInfo.DATAMODE_NORMAL ) )
                                           : SurveyInfo.DATAMODE_NORMAL;
	 int extend_ref = ( db_version > 38)? (int)( scanline0.longValue( SurveyInfo.SURVEY_EXTEND_NORMAL ) )
                                            : SurveyInfo.SURVEY_EXTEND_NORMAL;

         sid = setSurvey( name, datamode );

         try {
           myDB.beginTransaction();
           // success &= updateSurveyInfo( sid, date, team, decl, comment, init_station, xsections, false );
           ContentValues cv = makeSurveyInfoCcontentValues( date, team, decl, comment, init_station, xsections );
           myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(sid) } );
           // TDLog.v( "DB updateSurveyInfo: " + success );

           cv = makeSurveyExtend( extend_ref );
           myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(sid) } );

           while ( (line = br.readLine()) != null ) {
             TDLog.Log( TDLog.LOG_DB, "load from file: " + line );
             vals = line.split(" ", 4);
             table = vals[2];
             v = vals[3];
             Scanline scanline1 = new Scanline( v, v.indexOf('(')+1, v.lastIndexOf(')') );
             skip_sid = scanline1.longValue( -1 ); // skip survey ID
             id = scanline1.longValue( -1 );
             // TDLog.v("DB table " + table + " id " + id + " v " + v );

             if ( table.equals(AUDIO_TABLE) ) // ---------------- FIXME_AUDIO
	     {
               shotid = scanline1.longValue( -1 );
               if ( shotid >= 0 && id >= 0 ) {
                 date   = TDString.unescape( scanline1.stringValue( ) );
                 cv = makeAudioContentValues( sid, id, shotid, date );
                 myDB.insert( AUDIO_TABLE, null, cv ); 
                 // TDLog.Log( TDLog.LOG_DB, "load from file photo " + sid + " " + id + " " + title + " " + name );
               }

             }
	     else if ( table.equals(SENSOR_TABLE) ) // ------------ FIXME_SENSORS
	     {
               shotid  = scanline1.longValue( -1 );
               if ( shotid >= 0 && id >= 0 ) {
                 status  = scanline1.longValue( 0 );
                 title   = TDString.unescape( scanline1.stringValue( ) );
                 date    = TDString.unescape( scanline1.stringValue( ) );
                 comment = TDString.unescape( scanline1.stringValue( ) );
                 String type  = TDString.unescape( scanline1.stringValue( ) );
                 String value = TDString.unescape( scanline1.stringValue( ) );
                 cv = makeSensorContentValues( sid, id, shotid, status, title, date, comment, type, value );
                 myDB.insert( SENSOR_TABLE, null, cv ); 
                 // TDLog.Log( TDLog.LOG_DB, "load from file photo " + sid + " " + id + " " + title + " " + name );
               }
             }
	     else if ( table.equals(PHOTO_TABLE) ) // --------------- FIXME_PHOTO
             {
               shotid  = scanline1.longValue( -1 );
               if ( shotid >= 0 && id >= 0 ) {
                 title   = TDString.unescape( scanline1.stringValue( ) );
                 date    = TDString.unescape( scanline1.stringValue( ) );
                 comment = TDString.unescape( scanline1.stringValue( ) );
                 long camera = (db_version > 39)? scanline1.longValue( 0 ) : 0 ;
                 cv = makePhotoContentValues( sid, id, shotid, TDStatus.NORMAL, title, date, comment, camera );
                 myDB.insert( PHOTO_TABLE, null, cv ); 
                 // TDLog.Log( TDLog.LOG_DB, "load from file photo " + sid + " " + id + " " + title + " " + name );
               }
             }
	     else if ( table.equals(PLOT_TABLE) ) // ---------- PLOTS
	     {
               name         = TDString.unescape( scanline1.stringValue( ) );
               long type    = scanline1.longValue( -1 ); if ( db_version <= 20 ) if ( type == 3 ) type = 5;
               status       = scanline1.longValue( 0 );
               String start = TDString.unescape( scanline1.stringValue( ) );
               String view  = TDString.unescape( scanline1.stringValue( ) );
               double xoffset = scanline1.doubleValue( 0.0 );
               double yoffset = scanline1.doubleValue( 0.0 );
               double zoom    = scanline1.doubleValue( 1.0 );
               double azimuth = scanline1.doubleValue( 0.0 );
               double clino = ( db_version > 20 )? scanline1.doubleValue( 0.0 ) : 0.0;
               String hide  = ( db_version > 24 )? TDString.unescape( scanline1.stringValue( ) ) : TDString.EMPTY;
               String nick  = ( db_version > 30 )? TDString.unescape( scanline1.stringValue( ) ) : TDString.EMPTY;
	       int orientation = (db_version > 32 )? (int)(scanline1.longValue( 0 )) : 0; // default PlotInfo.ORIENTATION_PORTRAIT
	       int maxscrap = (db_version > 41 )? (int)(scanline1.longValue( 0 )) : 0; // default 0
               double intercept = (db_version > 42)? scanline1.doubleValue( 0.5 ) : 0;
               // if ( insertPlot( sid, id, name, type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation, false ) < 0 ) { success = false; }
               cv = makePlotContentValues( sid, id, name, type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation, maxscrap, intercept );
               myDB.insert( PLOT_TABLE, null, cv ); 
               // TDLog.Log( TDLog.LOG_DB, "load from file plot " + sid + " " + id + " " + start + " " + name );
               // TDLog.v( "DB load from file plot " + sid + " " + id + " " + start + " " + name + " success " + success );
   
             }
/* FIXME_SKETCH_3D *
	     else if ( table.equals(SKETCH_TABLE) ) // -------------- SKETCHES
	     {
               name         = TDString.unescape( scanline1.stringValue( ) );
               status       = scanline1.longValue( 0 );
               String start = TDString.unescape( scanline1.stringValue( ) );
               String st1   = TDString.unescape( scanline1.stringValue( ) );
               String st2   = TDString.unescape( scanline1.stringValue( ) );
               double xofft  = scanline1.doubleValue( 0.0 );
               double yofft  = scanline1.doubleValue( 0.0 );
               double zoomt  = scanline1.doubleValue( 0.0 );
               double xoffs  = scanline1.doubleValue( 0.0 );
               double yoffs  = scanline1.doubleValue( 0.0 );
               double zooms  = scanline1.doubleValue( 1.0 );
               double xoff3  = scanline1.doubleValue( 0.0 );
               double yoff3  = scanline1.doubleValue( 0.0 );
               double zoom3  = scanline1.doubleValue( 1.0 );
               double east   = scanline1.doubleValue( 0.0 );
               double south  = scanline1.doubleValue( 0.0 );
               double vert   = scanline1.doubleValue( 0.0 );
               double azimuth= scanline1.doubleValue( 0.0 );
               double clino  = scanline1.doubleValue( 0.0 );
               // if ( insertSketch3d( sid, id, name, status, start, st1, st2, xofft, yofft, zoomt, xoffs, yoffs, zooms, xoff3, yoff3, zoom3, east, south, vert, azimuth, clino ) < 0 ) { success = false; }
               cv = makeSketch3dContentValues( sid, id, name, status, start, st1, st2, xofft, yofft, zoomt,
		     xoffs, yoffs, zooms, xoff3, yoff3, zoom3, east, south, vert, azimuth, clino );
               myDB.insert( SKETCH_TABLE, null, cv ); 
             }
 * END_SKETCH_3D */
	     else if ( table.equals(SHOT_TABLE) ) // ------------ SHOTS
             {
               String from = TDString.unescape( scanline1.stringValue( ) );
               String to   = TDString.unescape( scanline1.stringValue( ) );
               double d    = scanline1.doubleValue( 0.0 );
               double b    = scanline1.doubleValue( 0.0 );
               double c    = scanline1.doubleValue( 0.0 );
               double r    = scanline1.doubleValue( 0.0 );
               double acc  = scanline1.doubleValue( 1.0 );
               double mag  = scanline1.doubleValue( 1.0 );
               double dip  = scanline1.doubleValue( 90.0 );
               long extend = scanline1.longValue( 1 );
               long flag   = scanline1.longValue( 0 );
               long leg    = scanline1.longValue( 0 );
               status      = scanline1.longValue( 0 );
               comment     = TDString.unescape( scanline1.stringValue( ) );
               // FIXME N.B. shot_type is not saved before 22
               long type   = 0; if ( db_version > 21 ) type   = scanline1.longValue( 0 ); // 0: DistoX
	       long millis = 0; if ( db_version > 31 ) millis = scanline1.longValue( 0 );
	       long color  = 0; if ( db_version > 33 ) color  = scanline1.longValue( 0 );
	       double stretch = 0; if ( db_version > 35 ) stretch = scanline1.doubleValue( 0.0 );
	       String addr = ""; if ( db_version > 37 ) addr = TDString.unescape( scanline1.stringValue( ) );

               // if ( doInsertShot( sid, id, millis, color, from, to, d, b, c, r, extend, stretch, flag, leg, status, type, comment, addr, false ) >= 0 ) {
               //   success &= updateShotAMDR( id, sid, acc, mag, dip, r, false );
	       // } else {
	       //   success = false;
	       // }
               cv = makeShotContentValues( sid, id, millis, color, from, to, d, b, c, r, acc, mag, dip, extend, stretch, flag, leg, status,
		      type, comment, addr );
               myDB.insert( SHOT_TABLE, null, cv ); 

               // TDLog.v( "DB insert shot " + from + "-" + to + ": " + success );
               // TDLog.Log( TDLog.LOG_DB, "insert shot " + sid + " " + id + " " + from + " " + to );
             }
	     else if ( table.equals(FIXED_TABLE) )
	     {
               station    = TDString.unescape( scanline1.stringValue( ) );
               double lng = scanline1.doubleValue( 0.0 );
               double lat = scanline1.doubleValue( 0.0 );
               double alt = scanline1.doubleValue( 0.0 );
               double asl = scanline1.doubleValue( 0.0 );
               comment    = TDString.unescape( scanline1.stringValue( ) );
               status     = scanline1.longValue( 0 );
               long source = scanline1.longValue( 0 ); // 0: unknown source
               double cs_lng = 0;
               double cs_lat = 0;
               double cs_alt = 0;
	       long cs_n_dec = 2;
               String cs = TDString.unescape( scanline1.stringValue( ) );
               if ( cs.length() > 0 ) {
                 cs_lng = scanline1.doubleValue( 0.0 );
                 cs_lat = scanline1.doubleValue( 0.0 );
                 cs_alt = scanline1.doubleValue( 0.0 );
	         if ( db_version > 34 ) cs_n_dec = scanline1.longValue( 8 ); // nr. of decimals
               }
               // use id == -1L to force DB get a new id
               // if ( insertFixed( sid, -1L, station, lng, lat, alt, asl, comment, status, source, cs, cs_lng, cs_lat, cs_alt, cs_n_dec ) < 0 ) {
	       //   success = false;
	       // }
               cv = makeFixedContentValues( sid, -1L, station, lng, lat, alt, asl, comment, status, source,
		     cs, cs_lng, cs_lat, cs_alt, cs_n_dec );
               myDB.insert( FIXED_TABLE, null, cv ); 
               // TDLog.Log( TDLog.LOG_DB, "load from file fixed " + sid + " " + id + " " + station  );
             } 
	     else if ( table.equals(STATION_TABLE) )
	     {
               // N.B. ONLY IF db_version > 19
               // TDLog.Error( "v <" + v + ">" );
               // TDLog.Log( TDLog.LOG_DB, "load from file station " + sid + " " + name + " " + comment + " " + flag  );
               name    = TDString.unescape( scanline1.stringValue( ) );
               comment = TDString.unescape( scanline1.stringValue( ) );
               long flag = ( db_version > 25 )? scanline1.longValue( 0 ) : 0;
               // success &= insertStation( sid, name, comment, flag );
               cv = makeStationContentValues( sid, name, comment, flag );
               myDB.insert( STATION_TABLE, null, cv ); 
             }
           }
           myDB.setTransactionSuccessful();
	   success = true;
         } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
         } catch ( SQLiteException e1 )       { logError("survey info", e1 ); 
         } catch ( IllegalStateException e2 ) { logError("survey info", e2 );
         } finally { myDB.endTransaction(); }
       }
       fr.close();
     } catch ( FileNotFoundException e ) { // DistoX-SAF
     } catch ( IOException e ) {
     }
     // TDLog.v( "DB success: " + success + " SID " + sid );

     return (success ? sid : -sid );
   }

   // ----------------------------------------------------------------------
   private ContentValues makeStationContentValues( long sid, String name, String comment, long flag )
   {
     ContentValues cv = new ContentValues();
     cv.put( "surveyId",  sid );
     cv.put( "name",      name );
     cv.put( "comment",   comment );
     cv.put( "flag",      flag );
     return cv;
   }

   public void insertStation( long sid, String name, String comment, long flag )
   {
     if ( myDB == null ) return; // false;
     // boolean ret = false;
     if ( comment == null ) comment = TDString.EMPTY;
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
       ContentValues cv = new ContentValues();
       cv.put("comment", comment );
       cv.put("flag", flag );
       doUpdate( STATION_TABLE, cv, "surveyId=? AND name=?", new String[]{ Long.toString(sid), name }, "station" );
       // try {
       //   myDB.beginTransaction();
       //   myDB.update( STATION_TABLE, cv, "surveyId=? AND name=?", new String[]{ Long.toString(sid), name } );
       //   myDB.setTransactionSuccessful();
       // } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e ); ret = false;
       // } catch ( SQLiteException e1 )       { logError("survey rename " + name, e1 ); ret =false;
       // } catch ( IllegalStateException e2 ) { logError("survey rename", e2 ); ret = false;
       // } finally { myDB.endTransaction(); }

       // if ( updateStationCommentStmt == null ) {
       //    updateStationCommentStmt = myDB.compileStatement( "UPDATE stations SET comment=?, flag=? WHERE surveyId=? AND name=?" );
       // }
       // updateStationCommentStmt.bindString( 1, comment );
       // updateStationCommentStmt.bindLong(   2, flag );
       // updateStationCommentStmt.bindLong(   3, sid );
       // updateStationCommentStmt.bindString( 4, name );
       // // ret =
       // doStatement( updateStationCommentStmt, "station update" );
     } else {
       ContentValues cv = makeStationContentValues( sid, name, comment, flag );
       // ret =
       doInsert( STATION_TABLE, cv, "station insert" );
     }
     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
     // return ret;
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
  // TdManager

  private final static String WHERE_NAME        = "name=?";
  // private final static String WHERE_SID         = "surveyId=?";
  // private final static String WHERE_SID_STATUS  = "surveyId=? AND status=?";
  // private final static String WHERE_SID_ID      = "surveyId=? AND id=?";

  static private String[] mReducedShotFields =
    { "id", "fStation", "tStation", "distance", "bearing", "clino", "extend", "leg" };
  // static private String[] mFullShotFields =
  //   { "id", "fStation", "tStation", "distance", "bearing", "clino", "acceleration", "magnetic", "dip", // 0 ..  8
  //     "extend", "flag", "leg", "comment", "type", "millis", "color", "stretch", "address"              // 9 .. 17
  //   };

  long getSurveyIdFromName( String name ) 
  {
    long id = -1;
    if ( myDB == null ) { return -2; }
    Cursor cursor = myDB.query( SURVEY_TABLE, new String[] { "id" },
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

  // SELECT STATEMENTS - SHOT
  List< DBlock > selectAllLegShotsReduced( long sid, long status )
  {
    // TDLog.v( "B4 select shots all leg");
    List< DBlock > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query(SHOT_TABLE, mReducedShotFields, // { "id", "fStation", "tStation", "distance", "bearing", "clino", "extend", "leg" };
                    WHERE_SID_STATUS, new String[]{ Long.toString(sid), Long.toString(status) },
                    null, null, "id" );
    if (cursor.moveToFirst()) {
      do {
        if ( cursor.getString(1).length() > 0 && cursor.getString(2).length() > 0 ) {
          DBlock block = new DBlock();
          reducedFillBlock( sid, block, cursor );
          list.add( block );
        }
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  private void reducedFillBlock( long sid, DBlock blk, Cursor cursor )
  {
    long leg = cursor.getLong(7);
    blk.setId( cursor.getLong(0), sid );
    blk.setBlockName( cursor.getString(1), cursor.getString(2), (leg == LegType.BACK) );  // from - to
    blk.mLength       = (float)( cursor.getDouble(3) );  // length [meters]
    // blk.setBearing( (float)( cursor.getDouble(4) ) ); 
    blk.mBearing      = (float)( cursor.getDouble(4) );  // bearing [degrees]
    float clino       = (float)( cursor.getDouble(5) );  // clino [degrees], or depth [meters]
    blk.mClino        = clino;
    blk.setExtend( (int)( cursor.getLong(6) ), 0 ); 
  }
  
  // SURVEY
  // List< String > selectAllSurveys( )
  // {
  //   List< String > list = new ArrayList<>();
  //   if ( myDB == null ) return list;
  //   try {
  //     Cursor cursor = myDB.query( SURVEY_TABLE,
  //                                 new String[] { "name" }, // columns
  //                                 null, null, null, null, "name" );
  //     if (cursor.moveToFirst()) {
  //       do {
  //         list.add( cursor.getString(0) );
  //       } while (cursor.moveToNext());
  //     }
  //     if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
  //   } catch ( SQLException e ) {
  //     // ignore
  //   }
  //   return list;
  // }

  public SurveyInfo getSurveyInfo( String name )
  {
    SurveyInfo info = null;
    if ( myDB == null ) return null;
    Cursor cursor = myDB.query( SURVEY_TABLE,
                               new String[] { "id", "name", "day", "team", "declination", "comment", "init_station", "xsections", "datamode", "extend" }, // columns
                               WHERE_NAME, new String[] { name },
                               null, null, "name" );
    if (cursor.moveToFirst()) {
      info = new SurveyInfo();
      info.id      = cursor.getLong( 0 );
      info.name    = cursor.getString( 1 );
      info.date    = cursor.getString( 2 );
      info.team    = cursor.getString( 3 );
      info.declination = (float)(cursor.getDouble( 4 ));
      info.comment = cursor.getString( 5 );
      info.initStation = cursor.getString( 6 );
      info.xsections = (int)cursor.getLong( 7 );
      info.datamode  = (int)cursor.getLong( 8 );
      info.mExtend   = (int)cursor.getLong( 9 );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return info;
  }

  public List< DBlock > getSurveyReducedData( long sid ) { return selectAllLegShotsReduced( sid, 0 ); }

  static private void updateSymbolKeys( SQLiteDatabase db )
  {
    int pt = 0;
    int ln = 0;
    int ar = 0;
    try {
      db.beginTransaction();
      Cursor cursor = db.rawQuery( "select key from configs where key like \"p_u:%%\" ", new String[] {} );
      if ( cursor != null ) {
        if ( cursor.moveToFirst() ) {
          do {
            String old_key = cursor.getString(0);
            String new_key = Symbol.deprefix_u( old_key );
            db.execSQL( String.format("update configs set key=\"%s\" where key=\"%s\" ", new_key, old_key ) );
            ++ pt;
          } while ( cursor.moveToNext() );
        }
        if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
      }
      cursor = db.rawQuery( "select key from configs where key like \"l_u:%%\" ", new String[] {} );
      if ( cursor != null ) {
        if ( cursor.moveToFirst() ) {
          do {
            String old_key = cursor.getString(0);
            String new_key = Symbol.deprefix_u( old_key );
            db.execSQL( String.format("update configs set key=\"%s\" where key=\"%s\" ", new_key, old_key ) );
            ++ ln;
          } while ( cursor.moveToNext() );
        }
        if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
      }
      cursor = db.rawQuery( "select key from configs where key like \"a_u:%%\" ", new String[] {} );
      if ( cursor != null ) {
        if ( cursor.moveToFirst() ) {
          do {
            String old_key = cursor.getString(0);
            String new_key = Symbol.deprefix_u( old_key );
            db.execSQL( String.format("update configs set key=\"%s\" where key=\"%s\" ", new_key, old_key ) );
            ++ ar;
          } while ( cursor.moveToNext() );
        }
        if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
      }
      db.setTransactionSuccessful();
      db.endTransaction();
      TDLog.Error( "Updated symbols: " + pt + " points, " + ln + " lines, " + ar + " areas");
    } catch ( SQLException e ) { TDLog.Error( "updateSymbolKeys exception: " + e.getMessage() );
    }
  }

  // ----------------------------------------------------------------------
  // DATABASE TABLES

  private static class DistoXOpenHelper // extends SQLiteOpenHelper
  {
     private static final String create_table = "CREATE TABLE IF NOT EXISTS ";

     // DistoXOpenHelper(Context context, String db_name ) 
     // {
     //    super(context, db_name, null, TDVersion.DATABASE_VERSION );
     //    TDLog.Log( TDLog.LOG_DB, "DB open helper ... " + db_name + " version " + TDVersion.DATABASE_VERSION );
     //    // TDLog.v( "DB open helper ... " + db_name + " version " + TDVersion.DATABASE_VERSION );
     // }

     // @Override
     // public void onCreate(SQLiteDatabase db) 
     // {
     //   createTables( db );
     // }

     // @Override
     // public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
     // {  
     //   updateTables( db, oldVersion, newVersion );
     // }

     static void createTables( SQLiteDatabase db )
     {
       TDLog.Log( TDLog.LOG_DB, "BD open helper - create tables");
       try {
          // db.setLockingEnabled( false );
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
            +   " xsections INTEGER, "
            +   " datamode INTEGER, "
            +   " extend INTEGER "
            +   ")"
          );

          db.execSQL(
              create_table + SHOT_TABLE 
            + " ( surveyId INTEGER, "
            +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
            +   " fStation TEXT, "
            +   " tStation TEXT, "
            +   " distance REAL, "   // distance 
            +   " bearing REAL, "    // azimuth
            +   " clino REAL, "      // clino | depth
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
            +   " color INTEGER, "     // custom color
            +   " stretch REAL default 0, " // extend strech, default ExtendType.STRETCH_NONE
            +   " address TEXT default \"\" " // distox address
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
            +   " orientation INTEGER default 0, "
            +   " maxscrap INTEGER default 0, "
            +   " intercept REAL default -1"
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
            +   " comment TEXT, "
            +   " camera INTEGER "
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
        // } finally {
          // db.setLockingEnabled( true );
        }
     }


     static void updateTables( SQLiteDatabase db, int oldVersion, int newVersion)
     {
        // FIXME this is called at each start when the database file exists
        TDLog.Log( TDLog.LOG_DB, "DB open helper - upgrade old " + oldVersion + " new " + newVersion );
        TDLog.v( "DB open helper - upgrade old " + oldVersion + " new " + newVersion );
        switch ( oldVersion ) {
          case 14: 
            db.execSQL( "ALTER TABLE surveys ADD COLUMN declination REAL default 0" );
            // db.execSQL( "ALTER TABLE gms ADD COLUMN status INTEGER default 0" );
          case 15:
            // db.execSQL( "ALTER TABLE devices ADD COLUMN name TEXT" );
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
             db.execSQL( "ALTER TABLE shots ADD COLUMN stretch REAL default 0" );
	   case 36:
             db.execSQL( "ALTER TABLE surveys ADD COLUMN datamode INTEGER default 0" );
	   case 37:
             db.execSQL( "ALTER TABLE shots ADD COLUMN address TEXT default \"\"" );
	   case 38:
             db.execSQL( "ALTER TABLE surveys ADD COLUMN extend INTEGER default 90" );
	   case 39:
             db.execSQL( "ALTER TABLE photos ADD COLUMN camera INTEGER default 0" );
	   case 40:
             db.execSQL( "update surveys set declination=1080 where declination>720" );
	   case 41:
             db.execSQL( "ALTER TABLE plots ADD COLUMN maxscrap INTEGER default 0" );
	   case 42:
             db.execSQL( "ALTER TABLE plots ADD COLUMN intercept REAL default -1" );
	   case 43:
             updateSymbolKeys( db );
	   case 44:
             // TDLog.v( "current version " + oldVersion );
           default:
             break;
         }
      }
   }
}
