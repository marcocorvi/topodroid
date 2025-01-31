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
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDsaf;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDUtil;
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
// import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDiskIOException;

import java.util.ArrayList;
import java.util.HashSet;
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
  // private static final String SKETCH_TABLE = "sketches";
  private static final String PHOTO_TABLE  = "photos";
  private static final String SENSOR_TABLE = "sensors";
  private static final String AUDIO_TABLE  = "audios";
  private static final String TRI_MIRRORED_STATIONS_TABLE  = "tri_mirrored_stations";

  private final static String WHERE_ID          = "id=?";
  private final static String WHERE_SID         = "surveyId=?";
  private final static String WHERE_SID_ID      = "surveyId=? AND id=?";
  // private final static String WHERE_SID_ID_LEGTYPE = "surveyId=? AND id=? AND leg=?";
  private final static String WHERE_SID_ID_MORE = "surveyId=? AND id>=?";
  private final static String WHERE_SID_NAME    = "surveyId=? AND name=?";
  private final static String WHERE_SID_STATUS  = "surveyId=? AND status=?";
  private final static String WHERE_SID_STATUS_FROM  = "surveyId=? AND status=? AND id>=?";
  // private final static String WHERE_SID_STATUS_LEG  = "surveyId=? AND status=? AND fStation > \"\" AND tStation > \"\"";
  private final static String WHERE_SID_LEG     = "surveyId=? AND fStation > \"\" AND tStation > \"\"";
  private final static String WHERE_SID_FROM    = "surveyId=? AND fStation > \"\"";
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

  static final private String[] mShotFullFields =
    { "id", "fStation", "tStation", "distance", "bearing", "clino", "acceleration", "magnetic", "dip",
      "extend", "flag", "leg", "comment", "type", "millis", "color", "stretch", "idx", "time", "address"
    };
  static final private String[] mShotFields =
    { "id", "fStation", "tStation", "distance", "bearing", "clino", "acceleration", "magnetic", "dip",  // 0 .. 8
      "extend", "flag", "leg", "comment", "type", "millis", "color", "stretch", "idx", "time" // 9 .. 18
    };
  static final private String[] mShotRawDataFields = // used only by selectAllShotsRawData
    { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll", "acceleration", "magnetic", "dip",
      "type", "millis", "address", "extend", "flag", "leg", "status", "comment", "rawMx", "rawMy", "rawMz", "rawGx", "rawGy", "rawGz", "idx", "time"
    };

  static final private String[] mPlotFieldsFull =
    { "id", "name", "type", "status", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino", "hide", "nick",
      "orientation", "maxscrap", "intercept", "center_x", "center_y", "center_z" };
  static final private String[] mPlotFields =
    { "id", "name", "type", "start", "view", "xoffset", "yoffset", "zoom", "azimuth", "clino", "hide", "nick",
      "orientation", "maxscrap", "intercept", "center_x", "center_y", "center_z" };

  static final private String[] mPlotName = { "name" };

  static final private String[] mSketchFields =
    { "id", "name", "start", "st1", "st2",
      "xoffsettop", "yoffsettop", "zoomtop", "xoffsetside", "yoffsetside", "zoomside", "xoffset3d", "yoffset3d", "zoom3d",
      "east", "south", "vert", "azimuth", "clino" 
    };

  // N.B. "source" comes after "status" although it is after "cs_altitude" in the table
  static final private String[] mFixedFields = {
    "id", "station", "longitude", "latitude", "altitude", "altimetric", "comment", "status", "source",
    "cs_name", "cs_longitude", "cs_latitude", "cs_altitude", "cs_decimals", "convergence", "accuracy", "accuracy_v", "m_to_units", "m_to_vunits"
  };

  static final private String[] mStationFields = { "name", "comment", "flag", "presentation", "code" };

  // private DataListenerSet mListeners; // IF_COSURVEY

  // ----------------------------------------------------------------------
  // DATABASE

  // private final TopoDroidApp mApp; // unused

  // UNUSED
  // public SQLiteDatabase getDb() { return myDB; }

  public synchronized boolean hasDB() { return myDB != null; }

  /** cstr: open default database (in the current work directory)
   * @param context context
   */
  // public DataHelper( Context context, TopoDroidApp app, DataListenerSet listeners ) // IF_COSURVEY
  public DataHelper( Context context /* , TopoDroidApp app */ )
  {
    // TDLog.v("DB cstr");
    // mApp     = app;
    // mListeners = listeners; // IF_COSURVEY
    this.openSurveyDatabase( context );
  }

  // /** cstr MOVE_TO_6
  //  * @param context context
  //  * @param db_path database pathname
  //  * @note used only for moveTo6
  //  */
  // public DataHelper( Context context, String db_path )
  // {
  //   this.openDatabaseWithPath( context, db_path );
  // }

  /** close the database
   */
  synchronized void closeDatabase()
  {
    if ( myDB == null ) return;
    myDB.close();
    myDB = null;
  }

  /** open or create the default database
   * @param context context
   * @return true if the database has been created
   *
   * open the database, if successful check if it needs to be updated
   * otherwise create the database
   */
  synchronized boolean openSurveyDatabase( Context context )
  {
    String db_name = TDPath.getDatabase(); // DistoX-SAF
    if ( myDB != null ) {
      TDLog.v( "DB open: app already has database " + db_name );
      return false;
    }
    try {
      // TDLog.v("DB ... try to open RW " + db_name);
      myDB = SQLiteDatabase.openDatabase( db_name, null, SQLiteDatabase.OPEN_READWRITE );
      if ( myDB != null ) {
        checkUpgrade();
        return false;
      }
    } catch ( SQLiteException e ) {
      // if it OK to fail
      TDLog.e( "Fail open DB R/W: " + e.getMessage() );
    }
    
    try {
      TDLog.v("DB ... try to open RW+CREATE " + db_name );
      myDB = SQLiteDatabase.openDatabase( db_name, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY );
      if ( myDB != null ) {
        TDLog.v( "DB opened: create tables");
        DistoXOpenHelper.createTables( myDB );
        myDB.setVersion( TDVersion.DATABASE_VERSION );
      } else {
        TDLog.e( "ERROR DB " + db_name + " failed open/create" );
      }
    } catch ( SQLiteException e ) {
      TDLog.e( "ERROR DB " + db_name + " open/create: " + e.getMessage() );
      myDB = null;
    }
    return (myDB != null); // at this point if myDB has been created if it is not null
  }

  /** check if the database need upgrading - in case upgrade it
   */
  private void checkUpgrade()
  {
    int oldVersion = myDB.getVersion();
    int newVersion = TDVersion.DATABASE_VERSION;
    boolean need_upgrade = myDB.needUpgrade( TDVersion.DATABASE_VERSION ); 
    // TDLog.v( "DB: version " + oldVersion + " -> " + newVersion + " upgrade: " + need_upgrade );
    if ( need_upgrade ) {
      // TDLog.v( "DB updating tables ...");
      DistoXOpenHelper.updateTables( myDB, oldVersion, newVersion );
      myDB.setVersion( TDVersion.DATABASE_VERSION );
    }
  }

  /** open a given database file
   * @param context    context
   * @param db_name    database path
   *
   * open the database, if successful check if it needs updating
   * @note the database is opened RW only to update the tables (if needed)
   */
  private void openDatabaseWithPath( Context context, String db_name )
  {
    try {
      // TDLog.v( "BD-path open " + db_name );
      myDB = SQLiteDatabase.openDatabase( db_name, null, SQLiteDatabase.OPEN_READWRITE );
      if ( myDB != null ) {
        int oldVersion = myDB.getVersion();
        int newVersion = TDVersion.DATABASE_VERSION;
        // TDLog.v( "DB-path version: " + oldVersion + " -> " + newVersion );
        if ( oldVersion < newVersion ) {
          // TDLog.v( "DB-path updating tables ...");
          DistoXOpenHelper.updateTables( myDB, oldVersion, newVersion );
          myDB.setVersion( TDVersion.DATABASE_VERSION );
        }
      }
    } catch ( SQLiteException e ) {
      TDLog.e( "ERROR DB-path open: " + e.getMessage() );
      myDB = null;
    }
  }

  /** fill a raw data block with values from the cursor
   * @param sid     survey ID
   * @param blk     raw data block
   * @param cursor  values cursor
   * used only by selectAllShotsRawData
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
    blk.setShotType( (int)(  cursor.getLong(10) ) );
    blk.mTime         = (long)( cursor.getLong(11) );
    blk.mAddress      = cursor.getString(12);
    blk.mExtend       = (int)(  cursor.getLong(13) );
    blk.mFlag         = cursor.getLong(14);
    blk.mLeg          = (int)(  cursor.getLong(15) );  // NOTE mLeg is not mBlockType: see setBlockType()
    blk.mStatus       = (int)(  cursor.getLong(16) );
    blk.mComment      = cursor.getString( 17 );
    blk.mRawMx        = (int)(  cursor.getLong(18) );
    blk.mRawMy        = (int)(  cursor.getLong(19) );
    blk.mRawMz        = (int)(  cursor.getLong(20) );
    blk.mRawGx        = (int)(  cursor.getLong(21) );
    blk.mRawGy        = (int)(  cursor.getLong(22) );
    blk.mRawGz        = (int)(  cursor.getLong(23) );
    blk.mIndex        = (int)(  cursor.getLong(24) );
    blk.mDeviceTime   = cursor.getLong(25);
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
    
    blk.setExtend( (int)cursor.getLong(9), (float)( cursor.getDouble(16) ) );
    blk.resetFlag( cursor.getLong(10) );
    blk.setBlockLegType( (int)leg );
    blk.mComment  = cursor.getString(12);
    blk.setShotType( (int)cursor.getLong(13) );
    blk.mTime     = cursor.getLong(14);
    blk.setPaintColor( (int)cursor.getLong(15) ); // color
    // blk.setStretch( (float)cursor.getDouble(16) ); // already set above
    blk.mIndex  = (int)cursor.getLong(17);
    blk.mDeviceTime = cursor.getLong(18);
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
    blk.setAddress( cursor.getString(19) );
  }

  // ----------------------------------------------------------------------
  // SURVEY DATA

  // private static String qInitStation  = "select init_station from surveys where id=?";
  // private static String qXSections    = "select xsections from surveys where id=?";
  // private static String qSurveysField = "select ? from surveys where id=?";
  private static final String qInitStation  = "select init_station from surveys where id=?";
  private static final String qXSections    = "select xsections from surveys where id=?";
  private static final String qXSectionStations = "select start from plots where surveyId=? and (type=0 or type=7)";
  private static final String qDatamode     = "select datamode from surveys where id=?";
  private static final String qDeclination  = "select declination from surveys where id=?";
  private static final String qExtend       = "select extend from surveys where id=?";
  private static final String qSurveysStat1 = "select flag, acceleration, magnetic, dip, address from shots where surveyId=? AND status=0 AND acceleration > 1 ";
  private static final String qSurveysStat2 =
    "select flag, distance, fStation, tStation, clino, extend from shots where surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ";
  // private static String qSurveysStat3 = "select fStation, clino from shots where surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ";
  // private static String qSurveysStat4 =
  //   "select flag, distance, fStation, tStation, clino, extend from shots where surveyId=? AND status=0 AND fStation!=\"\" AND tStation!=\"\" ";
  private static final String qSurveysStat5 = " select count() from shots where surveyId=? AND status=0 AND flag=0 AND fStation!=\"\" AND tStation=\"\" ";
  private static final String qSurveysStat6 = " select min( millis ), max( millis ) from shots where surveyId=? AND status=0 AND flag=0 ";

  /** @return the name of the survey initial station
   * @param sid   survey ID
   */
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

  /** @return the (at-station) xsections mode 
   * @param sid   survey ID
   * @note at-station xsections mode can be
   *   0 : shared
   *   1 : private
   */
  int getSurveyXSectionsMode( long sid )
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

  /** @return the list of stations that have an x-section
   * @param sid   survey ID
   * @note this function can be used to check when a station name associated to a xsection is changed
   */
  ArrayList<String> getXSectionStations( long sid )
  {
    ArrayList<String> ret = new ArrayList<>();
    Cursor cursor = myDB.rawQuery( qXSectionStations, new String[] { Long.toString(sid) } );
    if ( cursor != null ) {
      if (cursor.moveToFirst()) {
        ret.add( cursor.getString(0) );
      }
      if ( ! cursor.isClosed() ) cursor.close();
    }
    return ret;
  }

  /** @return the survey data-mode ie,   0 : normal,  1 : diving
   * @param sid   survey ID
   */
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

  /** @return the survey declination (or 0 if not known) [degrees]
   * @param sid   survey ID
   */
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

  /** @return the survey "extend"
   * @param sid   survey ID
   */
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

  /** update the survey "extend"
   * @param sid     survey ID
   * @param extend  new "extend" value
   */
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

  /** add a prefix to the station names of a survey
   * @param sid     survey ID
   * @param prefix  prefix
   */
  void prefixSurveyStations( long sid, String prefix )
  {
    if ( myDB == null ) return;
    ContentValues cv;
    String wSid = "where surveyId=" + sid;
    try {
      myDB.beginTransaction();
      myDB.execSQL( "update plots set hide =\"\" " + wSid );
      myDB.execSQL( "update plots set start = \""    + prefix + "\" || start "    + wSid );
      myDB.execSQL( "update plots set view = \""     + prefix + "\" || view "     + wSid + " and view!=\"\" ");
      myDB.execSQL( "update fixeds set station = \"" + prefix + "\" || station "  + wSid + " and station!=\"\" ");
      myDB.execSQL( "update stations set name = \""  + prefix + "\" || name "     + wSid + " and name!=\"\" ");
      myDB.execSQL( "update shots set fStation = \"" + prefix + "\" || fStation " + wSid + " and fStation!=\"\" ");
      myDB.execSQL( "update shots set tStation = \"" + prefix + "\" || tStation " + wSid + " and tStation!=\"\" ");
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError("prefix survey stations ", e1 ); 
    } catch ( IllegalStateException e2 ) { logError("prefix survey stations ", e2 );
    } finally { myDB.endTransaction(); }
  }

  /** @return the survey data statistics
   * @param sid   survey ID
   */
  SurveyStat getSurveyStat( long sid )
  {
    // TDLog.Log( TDLog.LOG_DB, "Get Survey Stat sid " + sid );
    HashMap< String, Integer > map = new HashMap< String, Integer >();
    int n0 = 0;
    int nc = 0;
    int ne = 0;
    int nl = 0;
    int nv = 0;

    SurveyStat stat = new SurveyStat( sid );

    if ( myDB == null ) return stat;

    int datamode = getSurveyDataMode( sid );
    String[] args = new String[1];
    args[0] = Long.toString( sid );

    Cursor cursor; // = null;
    if ( datamode == 0 ) {
      // cursor = myDB.query( SHOT_TABLE,
      //     		   new String[] { "flag", "acceleration", "magnetic", "dip", "address" },
      //                      "surveyId=? AND status=0 AND acceleration > 1 ",
      //                      new String[] { Long.toString(sid) },
      //                      null, null, null );
      cursor = myDB.rawQuery( qSurveysStat1, args );
      int nr_mgd = 0;
      if (cursor.moveToFirst()) {
        int nr = cursor.getCount();
        stat.G = new float[ nr ];
        stat.M = new float[ nr ];
        stat.D = new float[ nr ];
        HashMap< String, Integer > cnts = new HashMap<>();
        do {
          String address = cursor.getString(4);
          if ( address.length() > 0 ) {
            stat.G[nr_mgd] = (float)( cursor.getDouble(1) );
            stat.M[nr_mgd] = (float)( cursor.getDouble(2) );
            stat.D[nr_mgd] = (float)( cursor.getDouble(3) );
            ++nr_mgd;
            Integer cnt = (Integer) cnts.get( address );
            if ( cnt == null ) {
              cnts.put( address, new Integer(1) );
              // cnts.put( address, 1 ); // suggested by lint Integer.valueOf( 1 )
            } else {
              cnts.put( address, new Integer( cnt.intValue() + 1 ) );
              // cnts.put( address, cnt.intValue() + 1 ); // suggested by lint Integer.valueOf( ... )
            }
          }
        } while ( cursor.moveToNext() );
        SurveyAccuracy accu = new SurveyAccuracy();
        stat.nrMGD = accu.setBlocks( stat, nr_mgd );
        if ( stat.nrMGD > 0 ) {
          for ( int k = 0; k < nr_mgd; ++ k ) {
            if ( stat.G[k] > 10.0f ) {
              float m = stat.M[k] - stat.averageM;
              float g = stat.G[k] - stat.averageG;
              float d = stat.D[k] - stat.averageD;
              stat.stddevM += m * m;
              stat.stddevG += g * g;
              stat.stddevD += d * d;
            }
          }
          stat.stddevM   = (float)Math.sqrt( stat.stddevM / stat.nrMGD );
          stat.stddevG   = (float)Math.sqrt( stat.stddevG / stat.nrMGD );
          stat.stddevD   = (float)Math.sqrt( stat.stddevD / stat.nrMGD );
          stat.stddevM  *= 100/stat.averageM;  // percent of average
          stat.stddevG  *= 100/stat.averageG;
          stat.deviceNr  = cnts.size();
          StringBuilder sb = new StringBuilder();
          for ( String addr : cnts.keySet() ) {
            // TDLog.v("address " + addr + " " + (Integer)cnts.get( addr ) );
            try { // 20280118 try - catch
              sb.append(((Integer) cnts.get(addr)).intValue()).append(" ");
            } catch ( NullPointerException e ) {
              TDLog.e( e.getMessage() );
            }
          }
          stat.deviceCnt = sb.toString();
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

    cursor = myDB.rawQuery( qSurveysStat6, args );
    if (cursor.moveToFirst()) {
      stat.minMillis = cursor.getLong(0); // [seconds]
      stat.maxMillis = cursor.getLong(1);
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

  /** DEBUG: log an error
   * @param msg    message
   * @param e      exception
   */
  private void logError( String msg, Exception e )
  {
    TDLog.e("DB " + msg + ": " + e.getMessage() );
  }

  /** handle a disk error
   * @param e      disk error
   */
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

  /** rename a survey
   * @param id   survey ID
   * @param name new survey name
   * @return true if successful
   */
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

  /** make the content-value set for a survey "extend"
   * @param extend   "extend" value
   */
  private ContentValues makeSurveyExtend( int extend )
  {
    ContentValues cv = new ContentValues();
    cv.put( "extend", extend );
    return cv;
  }

  /** make the content-value set for a survey infos
   * @param date         date
   * @param team         survey team
   * @param decl         declination
   * @param comment      survey comment
   * @param init_station initial station
   * @param xsections     xsection mode (private or shared)
   * @param calculated_azimuths  should the azimuths be calculated (overwriting the measured ones)
   */
  private ContentValues makeSurveyInfoCcontentValues( String date, String team, double decl, String comment,
                                String init_station, int xsections, int calculated_azimuths ) // datamode cannot be updated
  {
    ContentValues cv = new ContentValues();
    cv.put( "day", date );
    cv.put( "team", ((team != null)? team : TDString.EMPTY ) );
    cv.put( "declination", decl );
    cv.put( "comment", ((comment != null)? comment : TDString.EMPTY ) );
    cv.put( "init_station", ((init_station != null)? init_station : TDString.ZERO ) );
    cv.put( "xsections", xsections );
    cv.put( "calculated_azimuths", calculated_azimuths );
    return cv;
  }

  /** update a survey infos
   * @param date         date
   * @param team         survey team
   * @param decl         declination
   * @param comment      survey comment
   * @param init_station initial station
   * @param xsections     xsection mode (private or shared)
   * @param calculated_azimuths  should the azimuths be calculated (overwriting the measured ones)
   */
  void updateSurveyInfo( long sid, String date, String team, double decl, String comment,
                         String init_station, int xsections, int calculated_azimuths )
                         // FIXME int extend
  {
    // TDLog.v("DB update survey, init station <" + init_station + ">" );
    ContentValues cv = makeSurveyInfoCcontentValues( date, team, decl, comment, init_station, xsections, calculated_azimuths );
    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(sid) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError("survey info", e1 ); 
    } catch ( IllegalStateException e2 ) { logError("survey info", e2 );
    } finally { myDB.endTransaction(); }
  }

  /** update a survey date and comment
   *@param name      survey name
   * @param date     new date
   * @param comment  survey new comment
   * @return true if successful
   */
  public boolean updateSurveyDayAndComment( String name, String date, String comment )
  {
    boolean ret = false;
    long sid = getIdFromName( SURVEY_TABLE, name );
    if ( sid >= 0 ) { // survey name exists
      ret = updateSurveyDayAndComment( sid, date, comment );
    }
    return ret;
  }

  /** perform a survey update
   * @param sid    survey ID
   * @param cv     content-value set of the update
   * @param msg    message (for error reporting)
   */
  private boolean doUpdateSurvey( long sid, ContentValues cv, String msg )
  {
    if ( myDB == null ) return false;
    boolean ret = false;
    try {
      myDB.beginTransaction();
      myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(sid) } );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 ); 
    } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  /** perform a table update
   * @param table  table name
   * @param cv     content-value set of the update
   * @param sid    survey ID
   * @param msg    message (for error reporting)
   * @return true if successful
   */
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

  /** perform a table update
   * @param table  table name
   * @param cv     content-value set of the update
   * @param where  where string
   * @param args   where args
   * @param msg    message (for error reporting)
   */
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

  /** perform a table insert
   * @param table  table name
   * @param cv     content-value set of the update
   * @param msg    message (for error reporting)
   * @return true if successful
   */
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

  /** perform a SQL statement
   * @param sw     SQL statement
   * @param msg    message (for error reporting)
   * @return true if successful
   */
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

  /** execute a "shot" SQL
   * @param id   shot ID, used for error reporting
   * @param sw   SQL statement
   * @return true if successful
   */
  private boolean doExecShotSQL( long id, StringWriter sw ) { return doExecSQL( sw, "sht " + id ); }

  /** update the "status" field
   * @param table    table
   * @param id       record ID
   * @param sid      survey ID
   * @param status   new status
   */
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

  /** update the survey date and comment
   * @param id      survey ID
   * @param date    survey new date
   * @param comment survey new comment (free string)
   * @return true if successful
   */
  public boolean updateSurveyDayAndComment( long id, String date, String comment )
  {
    if ( date == null ) return false;
    ContentValues cv = new ContentValues();
    cv.put( "day", date );
    cv.put( "comment", (comment != null)? comment : TDString.EMPTY );
    return doUpdateSurvey( id, cv, "survey day+cmt" );
  }

  /** update the survey team
   * @param id      survey ID
   * @param team    survey new team (free string)
   * @return true if successful
   */
  public boolean updateSurveyTeam( long id, String team )
  {
    ContentValues cv = new ContentValues();
    cv.put( "team", team );
    return doUpdateSurvey( id, cv, "survey team" );
  }

  /** update the survey initial station
   * @param id      survey ID
   * @param station survey initial station
   * @return true if successful
   */
  public boolean updateSurveyInitStation( long id, String station )
  {
    ContentValues cv = new ContentValues();
    // TDLog.v("DB update survey init_station <" + station + ">" );
    cv.put( "init_station", station );
    return doUpdateSurvey( id, cv, "survey init_station" );
  }
  
  /** update the survey declination
   * @param id      survey ID
   * @param decl    survey declination
   * @return true if successful
   */
  public boolean updateSurveyDeclination( long id, double decl )
  {
    ContentValues cv = new ContentValues();
    cv.put( "declination", decl );
    return doUpdateSurvey( id, cv, "survey decl" );
  }

  public boolean updateSurveyCalculatedAzimuths( long id, boolean cal_azi )  // 202007
  {
    ContentValues cv = new ContentValues();
    cv.put( "calculated_azimuths", cal_azi ? 1 : 0 );
    return doUpdateSurvey( id, cv, "survey cal_azi" );
  }

  // -----------------------------------------------------------------------
  // SURVEY delete

  /** delete a survey
   * @param sid   survey ID
   */
  void doDeleteSurvey( long sid )
  {
    if ( myDB == null ) return;
    String[] clause = new String[]{ Long.toString( sid ) };

    try {
      // myDB.setLockingEnabled( false );
      myDB.beginTransaction();
      myDB.delete( PHOTO_TABLE,   WHERE_SID, clause );
      myDB.delete( AUDIO_TABLE,   WHERE_SID, clause );
      myDB.delete( TRI_MIRRORED_STATIONS_TABLE, WHERE_SID, clause );
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

  public void updateShotBearing( long id, long sid, float b )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format( Locale.US,
        "UPDATE shots SET bearing=%.2f WHERE surveyId=%d AND id=%d",
        b, sid, id );
    doExecShotSQL( id, sw );
  }

  int updateShotNameAndData( long id, long sid, String fStation, String tStation,
                  long extend, long flag, long leg, String comment )
  {
    if ( myDB == null ) return -1;
    // if ( makesCycle( id, sid, fStation, tStation ) ) return -2;

    if ( tStation == null ) tStation = TDString.EMPTY;

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    if ( comment != null ) {
      // TDLog.v("DB update shot. id " + id + " extend " + extend + " leg " + leg + " comment <" + comment + ">" );
      pw.format( Locale.US,
        "UPDATE shots SET fStation=\"%s\", tStation=\"%s\", extend=%d, flag=%d, leg=%d, comment=\"%s\" WHERE surveyId=%d AND id=%d",
        fStation, tStation, extend, flag, leg, comment, sid, id );
    } else {
      // TDLog.v("DB update shot. id " + id + " extend " + extend + " leg " + leg );
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
  //   if ( TDString.isNullOrEmpty( t ) ) return false;
  //   if ( TDString.isNullOrEmpty( f ) ) return false;
  //   int cnt = 0;
  //   if ( hasShotAtStation( id, sid, f ) ) ++cnt;
  //   if ( hasShotAtStation( id, sid, t ) ) ++cnt;
  //   TDLog.Log( TDLog.LOG_DB, "makesCycle cnt " + cnt );
  //   return cnt >= 2;
  // }

  /** check for possible bad siblings (shots with the given stations and conflicting data)
   * @return false if there are no conflicting siblings
   * @param id0    block id to skip
   * @param sid    survey id
   * @param from   from station
   * @param to     to station
   * @param d0     shot length to check
   * @param b0     shot azimuth to check
   * @param c0     shot clino to check
   */
  boolean checkSiblings( long id0, long sid, String from, String to, float d0, float b0, float c0 )
  {
    if ( from.length() == 0 || to.length() == 0 ) return false;
    boolean ret = false;
    // TDLog.v( "check " + id0 + " siblings " + from + " " + to + " " + d0 + " " + b0 + " " + c0 );
    Cursor cursor = myDB.rawQuery( qShotsByStations, new String[] { Long.toString( sid ), from, to } );
    if (cursor.moveToFirst()) {
      cursor.moveToFirst();
      do { 
        if ( cursor.getLong( 0 ) != id0 ) {
          // TDLog.v( "F-sibling " + cursor.getLong(0) + " " + cursor.getDouble(1) + " " + cursor.getDouble(2) + " " + cursor.getDouble(3) );
          double b1 = Math.abs(cursor.getDouble( 2 ) - b0);
          if ( b1 > 180 ) b1 = Math.abs( b1 - 360 );
          if ( ( Math.abs( cursor.getDouble( 1 ) - d0 ) > d0*TDSetting.mSiblingThrD )
            || ( b1 > TDSetting.mSiblingThrA ) 
            || ( Math.abs( cursor.getDouble( 3 ) - c0 ) > TDSetting.mSiblingThrA ) ) {
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
            // TDLog.v( "B-sibling " + cursor.getLong(0) + " " + cursor.getDouble(1) + " " + cursor.getDouble(2) + " " + cursor.getDouble(3) );
            double b1 = Math.abs(cursor.getDouble( 2 ) + 180 - b0);
            if ( b1 > 180 ) b1 = Math.abs( b1 - 360 );
            if ( ( Math.abs( cursor.getDouble( 1 ) - d0 ) > d0*TDSetting.mSiblingThrD )
              || ( b1 > TDSetting.mSiblingThrA ) 
              || ( Math.abs( cursor.getDouble( 3 ) + c0 ) > TDSetting.mSiblingThrA ) ) {
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
  @SuppressWarnings("SameParameterValue")
  private void updateShotNameAndLeg(long id, long sid, String fStation, String tStation, int leg )
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

  /** set the status of a shot record
   * @param id     shot id
   * @param sid    survey id
   * @param status new status of the shot record
   */
  void deleteShot( long id, long sid, int status )
  {
    // if ( myDB == null ) return;
    updateStatus( SHOT_TABLE, id, sid, status );
  }

  /** undelete a shot, ie set its status to "normal"
   * @param id     shot id
   * @param sid    survey id
   */
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
    final int rawMxCol    = ih.getColumnIndex( "rawMx" );
    final int rawMyCol    = ih.getColumnIndex( "rawMy" );
    final int rawMzCol    = ih.getColumnIndex( "rawMz" );
    final int rawGxCol    = ih.getColumnIndex( "rawGx" );
    final int rawGyCol    = ih.getColumnIndex( "rawGy" );
    final int rawGzCol    = ih.getColumnIndex( "rawGz" );
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
	ih.bind( addressCol, "" );
	ih.bind( rawMxCol, 0 );
	ih.bind( rawMyCol, 0 );
	ih.bind( rawMzCol, 0 );
	ih.bind( rawGxCol, 0 );
	ih.bind( rawGyCol, 0 );
	ih.bind( rawGzCol, 0 );
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
        try { // 20280118 try - catch
          float depth = depths.get(from).floatValue() - shot.len * TDMath.sind(shot.cln);
          depths.put(to, Float.valueOf(depth));
        } catch ( NullPointerException e ) {
          TDLog.e( e.getMessage() );
        }
        // TDLog.v( "processed shot <" + from + "-" + to + "> shots " + stack.size() + "add station <" + to + "> depth " + depth );
      }
      if ( to != null && to.length() > 0 && depths.containsKey( to ) && from != null && from.length() > 0 && ! depths.containsKey( from ) ) { // can add FROM station
        for ( ParserShot sh : shots ) {
	  String s1 = sh.from;
	  String s2 = sh.to;
          if ( s1.length() > 0 && s2.length() > 0 && ( from.equals( s1 ) || from.equals( s2 ) ) ) stack.add( sh );
        }
        try { // 20280118 try - catch
          float depth = depths.get(to).floatValue() + shot.len * TDMath.sind(shot.cln);
          depths.put( from, Float.valueOf( depth ) );
        } catch ( NullPointerException e ) {
          TDLog.e( e.getMessage() );
        }
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

  /** insert a Cavway shot
   * @param sid     survey ID
   * @param id      shot ID or -1
   * @param d       distance
   * @param b       azimuth
   * @param c       clino
   * @param r       roll
   * @param mag     magnetic abs. value
   * @param acc     acceleration abs. value
   * @param dip     dip angle
   * @param extend  extend (int)
   * @param leg     leg-type
   * @param status  status
   * @param comment shot comment
   * @param addr    BRIC4 address
   * @return inserted shot ID
   */
  public long insertCavwayShot( long sid, long id, double d, double b, double c, double r, double mag, double acc, double dip, long extend, int leg, long status, String comment, String addr,
                                int rawMx, int rawMy, int rawMz, int rawGx, int rawGy, int rawGz , long time)
  { // 0L=leg, status, 0L=type DISTOX
    // stretch = 0.0;
    // return doInsertShot( sid, id, TDUtil.getTimeStamp(), 0L, "", "",  d, b, c, r, extend, 0.0, DBlock.FLAG_SURVEY, 0L, status, 0L, "", addr );
    if ( id >= 0 && hasShotId( sid, id ) ) { // if shot ID is already present, use a new ID
      id = -1L;
    }
    return doCavwayInsertShot( sid, id, TDUtil.getTimeStamp(), 0L, d, b, c, r, mag, acc, dip, extend, 0.0, leg, status, comment, 0L, addr, rawMx, rawMy, rawMz, rawGx, rawGy, rawGz, time );
  }

  /** insert a BRIC shot
   * @param sid     survey ID
   * @param d       distance
   * @param b       azimuth
   * @param c       clino
   * @param r       roll
   * @param mag     magnetic abs. value
   * @param acc     acceleration abs. value
   * @param dip     dip angle
   * @param extend  extend (int)
   * @param leg     leg-type
   * @param status  status
   * @param comment shot comment
   * @param addr    BRIC address
   * @param idx     BRIC shot index
   * @param time    BRIC shot timestamp [s]
   * @return inserted shot ID
   */
  public long insertBricShot( long sid, /*long id, */ double d, double b, double c, double r, double mag, double acc, double dip, long extend, int leg, long status, String comment, String addr, long idx, long time )
  { // 0L=leg, status, 0L=type DISTOX
    // stretch = 0.0;
    // return doInsertShot( sid, id, TDUtil.getTimeStamp(), 0L, "", "",  d, b, c, r, extend, 0.0, DBlock.FLAG_SURVEY, 0L, status, 0L, "", addr );
    // if ( TDSetting.mBricIndexIsId ) {
    //   if ( id < maxShotId( sid ) ) { // if shot ID is already present, use a new ID
    //     id = -1L;
    //   }
    // } else {
    //   id = -1L;
    // }
    // return doBricInsertShot( sid, id, TDUtil.getTimeStamp(), 0L, d, b, c, r, mag, acc, dip, extend, 0.0, leg, status, comment, 0L, addr, idx );
    return doBricInsertShot( sid, -1L, TDUtil.getTimeStamp(), 0L, d, b, c, r, mag, acc, dip, extend, 0.0, leg, status, comment, 0L, addr, idx, time );
  }

  /** insert a DistoX shot
   * @param sid    survey ID
   * @param id     shot ID or -1
   * @param d      distance
   * @param b      azimuth
   * @param c      clino
   * @param r      roll
   * @param extend extend (int)
   * @param status status
   * @param addr   DistoX address
   * @param idx    device internal shot-index (0 by default)
   * @return inserted shot ID
   */
  public long insertDistoXShot( long sid, long id, double d, double b, double c, double r, long extend, long status, String addr, long idx )
  { // 0L=leg, status, 0L=type DISTOX
    // stretch = 0.0;
    // return doInsertShot( sid, id, TDUtil.getTimeStamp(), 0L, "", "",  d, b, c, r, extend, 0.0, DBlock.FLAG_SURVEY, 0L, status, 0L, "", addr );
    return doSimpleInsertShot( sid, id, TDUtil.getTimeStamp(), 0L, d, b, c, r, extend, 0.0, 0L, status, 0L, addr, idx, 0L );
  }

  /** insert a shot copying from a dblock (except surveyId and Id)
   * @param sid    survey ID
   * @param blk    dblock
   * @return inserted shot ID (-1 on error)
   */
  public long insertDBlockShot( long sid, DBlock blk )
  {
    if ( myDB == null ) return -1L;
    ++ myNextId;
    // 0L = color, 0 = status
    ContentValues cv = makeShotContentValues( sid, myNextId, blk.mTime, 0L, blk.mFrom, blk.mTo, 
                         blk.mLength, blk.mBearing, blk.mClino, blk.mRoll, blk.mMagnetic, blk.mAcceleration, blk.mDip, 
                         blk.mExtend, blk.getStretch(), blk.mFlag, blk.getLegType(), 0, blk.getShotType(), blk.mComment, blk.getAddress(),
                         blk.mRawMx, blk.mRawMy, blk.mRawMz, blk.mRawGx, blk.mRawGy, blk.mRawGz, blk.mIndex, blk.mDeviceTime );
    if ( ! doInsert( SHOT_TABLE, cv, "dblock insert" ) ) return -1L;
    return myNextId;
  }

  // /**
  //  * @param sid       survey ID
  //  * @param id        shot ID
  //  * @param millis    millis/1000 ie seconds
  //  * @param color     custom color
  //  * @param d         distance [m]
  //  * @param b         azimuth [degree]
  //  * @param c         clino [degree]
  //  * @param r         roll [degree]
  //  * @param extend    extend (-1,0,1,2,...)
  //  * @param stretch   fractional extend (in [-1,1])
  //  * @param leg      
  //  * @param shot_type shot type (?)
  //  * @param addr      device address (?)
  //  */
  // public long insertSimpleShot( long sid, long id, long millis, long color, double d, double b, double c, double r,
  //       	   long extend, double stretch, long leg,
  //                  long shot_type, String addr )
  // { // leg, 0L=status, type 
  //   // return doInsertShot( sid, id, millis, color, "", "",  d, b, c, r, extend, stretch, DBlock.FLAG_SURVEY, leg, 0L, shot_type, "", addr );
  //   return doSimpleInsertShot( sid, id, millis, color, d, b, c, r, extend, stretch, leg, 0L, shot_type, addr, 0, 0L );
  // }

  /**
   * @param sid       survey ID
   * @param id        shot ID
   * @param millis    millis/1000 ie seconds
   * @param color     custom color
   * @param d         distance [m]
   * @param b         azimuth [degree]
   * @param c         clino [degree]
   * @param r         roll [degree]
   * @param extend    extend (-1,0,1,2,...)
   * @param stretch   fractional extend (in [-1,1])
   * @param leg      
   * @param shot_type shot type (?)
   * @return id of the shot record
   */
  long insertManualShot(long sid, @SuppressWarnings("SameParameterValue") long id, long millis, long color, double d, double b, double c, double r,
                        long extend, double stretch, long leg,
                        long shot_type )
  { // leg, 0L=status, type 
    // return doInsertShot( sid, id, millis, color, "", "",  d, b, c, r, extend, stretch, DBlock.FLAG_SURVEY, leg, 0L, shot_type, "", "" );
    return doSimpleInsertShot( sid, id, millis, color, d, b, c, r, extend, stretch, leg, 0L, shot_type, "", 0, 0L );
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

  /**
   * @param id     shot ID
   * @param sid    survey ID
   * @param acc    acceleration
   * @param mag    magnetic
   * @param dip    dip
   * @param r      roll
   * @param backshot  whether the shot is DistoX2 backsight
   */
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

  /** transfer plots, at a given station, from a survey to another
   * @param old_survey_name   name of the source survey
   * @param new_survey_name   name of the target survey
   * @param sid               ID of the target survey
   * @param old_sid           ID of the source survey
   * @param station           plot origin station
   */
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

  /** transfer shots from a survey to another
   * @param sid     target survey ID
   * @param old_sid source survey ID
   * @param old_id  ID of first shot of the source survey that is transferred
   */
  void transferShots( long sid, long old_sid, long old_id )
  {
    if ( myDB == null ) return;
    SurveyInfo old_survey = selectSurveyInfo( old_sid );
    SurveyInfo new_survey = selectSurveyInfo( sid );
    long max_old_id = maxId( SHOT_TABLE, old_sid );
    // TDLog.v("max shot ID " + max_old_id );

    // // transfer shots could be as simple as 
    // long delta_id =  maxId( SHOT_TABLE, sid ) + 1 - old_id;
    // transferShotStmt = myDB.compileStatement( "UPDATE shots SET surveyId=" + sid + ", id=(id+" + delta_id + ") where surveyId=" + old_sid + " and idi>=" + old_id );
    // // however care must be taken about fixeds, photos, audios and plots

    ContentValues vals0 = new ContentValues();
    vals0.put( "surveyId", sid );
    String[] where0 = new String[2];
    where0[0] = Long.toString( old_sid );

    Set<String> stations = new TreeSet<>();

    try {
      myDB.beginTransaction();
      ContentValues cv = new ContentValues();
      cv.put( "surveyId", sid );
      for ( ; old_id < max_old_id; ++old_id ) {
        DBlock blk = selectShot( old_id, old_sid );
        if ( blk == null ) {
          TDLog.e("null block at ID " + old_id );
          continue;
        }
        if ( transferShotStmt == null ) {
          // TDLog.v("compile transfer stmt");
          transferShotStmt = myDB.compileStatement( "UPDATE shots SET surveyId=?, id=? where surveyId=? and id=?" );
        }
        // TDLog.v("transfer shot ID " + old_id + " to " + myNextId );
        transferShotStmt.bindLong(1, sid);
        transferShotStmt.bindLong(2, myNextId);
        transferShotStmt.bindLong(3, old_sid);
        transferShotStmt.bindLong(4, old_id);
        transferShotStmt.execute();
        
        // transfer fixeds, stations, plots and sketches
        // UPDATE fixeds SET surveyId=? where surveyId=? and station=\"?\" 
        //  sid, old_sid, blk.mFrom
        // TODO FIXME cross-sections 
        if ( blk.mFrom.length() > 0 ) stations.add( blk.mFrom );
        if ( blk.mTo.length() > 0 ) stations.add( blk.mTo );

        cv.put( "shotId",   myNextId );
        String[] where = new String[2];
        where[0] = Long.toString( old_sid );
        List< SensorInfo > sensors = selectSensorsAtShot( old_sid, old_id ); // transfer sensors
        // TDLog.v("sensors " + sensors.size() );
        if ( sensors.size() > 0 ) for ( SensorInfo sensor : sensors ) {
          where[1] = Long.toString( sensor.id );
          myDB.update( SENSOR_TABLE, cv, WHERE_SID_ID, where );
        }

        AudioInfo audio = getAudioAtShot( old_sid, old_id ); // transfer audio
        if ( audio != null ) {
          // TDLog.v("audio");
          long item_id = audio.getItemId();
          String audio_file_idx = Long.toString( audio.getItemId() );
          where[1] = audio_file_idx;
          myDB.update( AUDIO_TABLE, cv, WHERE_SID_SHOTID, where );
          String oldname = TDPath.getSurveyWavFile( old_survey.name, audio_file_idx );
          String newname = TDPath.getSurveyWavFile( new_survey.name, audio_file_idx );
          TDFile.renameFile( oldname, newname );
        }

        List< PhotoInfo > photos = selectPhotoAtShot( old_sid, old_id ); // transfer photos
        // TDLog.v("photos " + photos.size( ) );
        if ( photos.size() > 0 ) for ( PhotoInfo photo : photos ) {
          long photo_id = photo.getId();
          where[1] = Long.toString( photo_id );
          myDB.update( PHOTO_TABLE, cv, WHERE_SID_ID, where );
          String oldname = TDPath.getSurveyJpgFile( old_survey.name, Long.toString(photo_id) );
          String newname = TDPath.getSurveyJpgFile( new_survey.name, Long.toString(photo_id) );
          TDFile.renameFile( oldname, newname );
        }

        ++ myNextId;
      }

      // NOTE transfer fixeds and plots was done for each shot considering blk.mFrom (and same for blk.mTo)
      // now the following code is replaced by a for-loop over the stations at the end
      //
      // if ( blk.mFrom.length() > 0 ) {
      //   List< FixedInfo > fixeds = selectFixedAtStation( old_sid, blk.mFrom ); 
      //   for ( FixedInfo fixed : fixeds ) {
      //     where0[1] = Long.toString( fixed.id );
      //     myDB.update( FIXED_TABLE, vals0, WHERE_SID_ID, where0 );
      //   }
      //   where0[1] = blk.mFrom;
      //   myDB.update( STATION_TABLE, vals0, WHERE_SID_NAME, where0 );
      //   transferPlots( old_survey.name, new_survey.name, sid, old_sid, blk.mFrom );
      //   // transferSketches( old_survey.name, new_survey.name, sid, old_sid, blk.mFrom ); // FIXME_SKETCH_3D
      // }

      // TDLog.v("transfer stations " + stations.size() );
      for ( String st : stations ) {
        // if ( transferShotFixedStmt == null ) {
        //   transferShotFixedStmt = myDB.compileStatement( "UPDATE fixeds SET surveyId=? where surveyId=? and station=\"?\"" );
        // }
        // transferShotFixedStmt.bindLong(1, sid);
        // transferShotFixedStmt.bindLong(2, old_sid);
        // transferShotFixedStmt.bindString(2, blk.mFrom);
        // transferShotFixedStmt.execute();

        List< FixedInfo > fixeds = selectFixedAtStation( old_sid, st );
        for ( FixedInfo fixed : fixeds ) {
          where0[1] = Long.toString( fixed.id );
          myDB.update( FIXED_TABLE, vals0, WHERE_SID_ID, where0 );
        }
        where0[1] = st;
        myDB.update( STATION_TABLE, vals0, WHERE_SID_NAME, where0 );

        transferPlots( old_survey.name, new_survey.name, sid, old_sid, st );
        // transferSketches( old_survey.name, new_survey.name, sid, old_sid, st ); // FIXME_SKETCH_3D
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("transfer shots", e);
    } finally { myDB.endTransaction(); }
  }

  long insertManualShotAt( long sid, long at, long millis, long color, double d, double b, double c, double r,
		     long extend, double stretch, long leg, long shot_type )
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
    cv.put( "type",     shot_type ); 
    cv.put( "millis",   millis ); // [s]
    cv.put( "color",    color );
    cv.put( "stretch",  stretch );
    cv.put( "address",  "" );
    cv.put( "rawMx",    0.0 );
    cv.put( "rawMy",    0.0 );
    cv.put( "rawMz",    0.0 );
    cv.put( "rawGx",    0.0 );
    cv.put( "rawGy",    0.0 );
    cv.put( "rawGz",    0.0 );
    cv.put( "idx",      0 );
    cv.put( "time",     0L ); // [s]

    if ( ! doInsert( SHOT_TABLE, cv, "insert at" ) ) return -1L;
    return at;
  }

  private ContentValues makeShotContentValues( long sid, long id, long millis, long color, String from, String to, 
                          double d, double b, double c, double r, double acc, double mag, double dip,
                          long extend, double stretch, long flag, long leg, long status, long shot_type, String comment, String addr,
                          int rawMx, int rawMy, int rawMz, int rawGx, int rawGy, int rawGz, long idx, long time )
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
    cv.put( "millis",   millis ); // [s]
    cv.put( "color",    color );
    cv.put( "stretch",  stretch );
    cv.put( "address",  addr );
    cv.put( "rawMx",    rawMx );
    cv.put( "rawMy",    rawMy );
    cv.put( "rawMz",    rawMz );
    cv.put( "rawGx",    rawGx );
    cv.put( "rawGy",    rawGy );
    cv.put( "rawGz",    rawGz );
    cv.put( "idx",      idx );  // device index
    cv.put( "time",     time ); // [s]
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
		    extend, stretch, flag, leg, status, shot_type, comment, addr, 0, 0, 0, 0, 0, 0,  0, 0L );
    if ( ! doInsert( SHOT_TABLE, cv, "insert" ) ) return -1L;
    return id;
  }
  */

  // return the new-shot id
  // doInsertShot() called with from="", to="", and comment="", flag=DBlock.FLAG_SURVEY
  //
  // @param sid           survey ID
  // @param id            shot id
  // @param millis        timestamp [s]
  // @param color         custom color (splay)
  // @param d, b, c, r    distance, bearing, clino, roll
  // @param extend
  // @param stretch       fractional extend
  // @param leg           "leg" type
  // @param status        shot status
  // @param shot_type     either DISTOX or MANUAL
  // @param addr          device address
  // @param idx           device shot-index
  // @param time          device shot-time [s]
  //
  // FROM and TO are set to ""
  // mag, acc, dip are set to 0
  // comment is set to ""
  // flag is set to SURVEY
  private long doSimpleInsertShot( long sid, long id, long millis, long color, 
                          double d, double b, double c, double r, 
                          long extend, double stretch, long leg, long status, long shot_type, String addr, long idx, long time )
  {
    // TDLog.v("DB simple insert shot id " + id + " d " + d + " b " + b + " c " + c + " extend " + extend + "/" + stretch + " leg " + leg + 
    //   " status " + status + " type " + shot_type + " addr " + addr + " idx " + idx + " millis " + millis + " color " + color );
    if ( myDB == null ) return -1L;
    if ( id == -1L ) {
      ++ myNextId;
      id = myNextId;
    } else {
      myNextId = id;
    }
    if (addr == null) addr = "";                                  // from-to              acc  mag  dip  ext str flag leg status type comment addr raw... index
    ContentValues cv = makeShotContentValues( sid, id, millis, color, "", "", d, b, c, r, 0.0, 0.0, 0.0, extend, stretch, DBlock.FLAG_SURVEY, leg, status, shot_type, "", addr, 
      0, 0, 0, 0, 0, 0, idx, time );
    if ( ! doInsert( SHOT_TABLE, cv, "simple insert" ) ) return -1L;
    return id;
  }

  private long doBricInsertShot( long sid, long id, long millis, long color, 
                          double d, double b, double c, double r, double mag, double acc, double dip,
                          long extend, double stretch, long leg, long status, String comment, long shot_type, String addr, long idx, long time )
  {
    // TDLog.v("DB complete insert shot id " + id + " d " + d + " b " + b + " c " + c );
    if ( myDB == null ) return -1L;
    if ( id == -1L ) {
      ++ myNextId;
      id = myNextId;
    } else {
      myNextId = id;
    }
    if (addr == null) addr = "";
    ContentValues cv = makeShotContentValues( sid, id, millis, color, "", "", d, b, c, r, mag, acc, dip,
		                              extend, stretch, DBlock.FLAG_SURVEY, leg, status, shot_type, comment, addr, 0, 0, 0, 0, 0, 0, idx, time );
    if ( ! doInsert( SHOT_TABLE, cv, "bric insert" ) ) return -1L;
    return id;
  }

  private long doCavwayInsertShot( long sid, long id, long millis, long color, 
                          double d, double b, double c, double r, double mag, double acc, double dip,
                          long extend, double stretch, long leg, long status, String comment, long shot_type, String addr,
                          int rawMx, int rawMy, int rawMz, int rawGx, int rawGy, int rawGz, long time )
  {
    // TDLog.Log( TDLog.LOG_DB, "insert shot <" + id + "> " + from + "-" + to + " extend " + extend );
    // TDLog.v("DB complete insert shot id " + id + " d " + d + " b " + b + " c " + c );
    if ( myDB == null ) return -1L;
    if ( id == -1L ) {
      ++ myNextId;
      id = myNextId;
    } else {
      myNextId = id;
    }
    if (addr == null) addr = "";
    ContentValues cv = makeShotContentValues( sid, id, millis, color, "", "", d, b, c, r, mag, acc, dip,
		    extend, stretch, DBlock.FLAG_SURVEY, leg, status, shot_type, comment, addr, rawMx, rawMy, rawMz, rawGx, rawGy, rawGz, 0, time );
    if ( ! doInsert( SHOT_TABLE, cv, "cavway insert" ) ) return -1L;
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

  // for leg xsections store the value of intercept TT
  void updatePlotIntercept( long pid, long sid, float intercept )
  {
    if ( myDB == null ) return; // false;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE plots set intercept=%f WHERE surveyId=%d AND id=%d", intercept, sid, pid );
    doExecSQL( sw, "plt intercept" );
  }

  // for multileg xsections stores the value of intercept TT=2 and center
  void updatePlotCenter( long pid, long sid, Vector3D center )
  {
    if ( myDB == null ) return; // false;
    if ( center == null ) return;
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "UPDATE plots set intercept=2.0, center_x=%f, center_y=%f, center_z=%f WHERE surveyId=%d AND id=%d", center.x, center.y, center.z, sid, pid );
    doExecSQL( sw, "plt center" );
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

  // FIXME TODO these can be improved with a JOIN select on sensors and shots
  private static final String qSensors1     = "select id, shotId, title, date, comment, type, value, reftype from sensors where surveyId=? AND status=? ";
  private static final String qSensors2     = "select id, shotId, title, date, comment, type, value, reftype from sensors where surveyId=? AND shotId=? ";


  // private static final String qAudio        = "select id, date, type from audios where surveyId=? AND shotId=? ";
  private static final String qAudioShot    = "select id, date from audios where surveyId=? AND shotId=? AND reftype=1 "; // shot-item = 1
  private static final String qAudioPlot    = "select id, date from audios where surveyId=? AND id=? AND shotId=? AND reftype=2 "; // plot-item = 1

  private static final String qAudioIdItem  = "select id from audios where surveyId=? AND shotId=? AND reftype=? "; 

  private static final String qAudiosAll    = "select id, shotId, date, reftype from audios where surveyId=? order by reftype";

  private static final String qAudiosByReftype = "select id, shotId, date from audios where surveyId=? and reftype=?";

  private static final String qPhotosAll    = "select id, shotId, status, title, date, comment, camera, code, reftype from photos where surveyId=? ";

  private static final String qTriMirrorStationAll    = "SELECT name FROM tri_mirrored_stations WHERE surveyId=? ";

  private static final String qjPhoto       = "select id, shotId from photos where surveyId=? and id=? and reftype=?";

  private static final String qjCountPhoto  = "select count() from photos where surveyId=? and status=?";

  // used in selectAllPhotosShot
  private static final String qjPhotosShot  =
    "select p.id, s.id, p.title, s.fStation, s.tStation, p.date, p.comment, p.camera, p.code, p.reftype from photos as p join shots as s on p.shotId=s.id where p.surveyId=? and s.surveyId=? and s.status=? and p.reftype=1";
  // 20241018 was p.status
  //  "select p.id, COALESCE(s.id, -1), p.title, s.fStation, s.tStation, p.date, p.comment, p.camera, p.code from photos as p left join shots as s on p.shotId=s.id where p.surveyId=? and (s.surveyId=? OR p.shotId=-1) and p.status=? ";
  // private static String qShotPhoto    = "select id, shotId, title, date, comment from photos where surveyId=? AND shotId=? ";

  // used in selectAllPhotosPlot
  private static final String qjPhotosPlot  =
    "select p.id, q.id, p.title, q.name, p.date, p.comment, p.camera, p.code, p.reftype from photos as p join plots as q on p.shotId=q.id where p.surveyId=? and q.surveyId=? and q.status=? and p.reftype=2";
  // 20241018 was p.status

  // used in selectAllPhotoAtShot
  private static final String qjShotPhotos  =
    "select p.id, s.id, p.title, s.fStation, s.tStation, p.date, p.comment, p.camera, p.code, p.reftype from photos as p join shots as s on p.shotId=s.id where p.surveyId=? AND s.surveyId=? AND p.shotId=? AND p.reftype=1";

  // used in selectAllPhotoXSection
  private static final String qjPhotosXSection  =
    "select p.id, x.id, p.title, x.name, p.date, p.comment, p.camera, p.code, p.reftype from photos as p join plots as x on p.shotId=x.id where p.surveyId=? AND x.surveyId=? AND x.status=? AND p.reftype=3";
  // 20241018 was p.status

  // // used in countAllShotPhotos
  // private static final String cntShotPhotos      =
  //   "select count(p.id) from photos as p join shots as s on p.shotId=s.id where p.surveyId=? and s.surveyId=? and p.status=? AND p.reftype=1";

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
  private static final String qFixeds = "select A.station, A.latitude, A.longitude, A.altitude, A.altimetric, A.cs_name, A.cs_latitude, A.cs_longitude, A.cs_altitude, A.convergence, A.accuracy, A.accuracy_v, A.m_to_units, A.m_to_vunits, A.status from fixeds as A join surveys as B where A.surveyId=B.id AND B.name=?";
  // private static final String qLength = "select count(), sum(A.distance) from shots as A, surveys as B where A.surveyId=B.id and B.name=? and A.fStation!=\"\" and A.tStation!=\"\"";

  /** @return the list of all sensor_info of a survey with a given status
   * @param sid     survey ID
   * @param status  sensor-info status
   */
  List< SensorInfo > selectAllSensors( long sid, long status )
  {
    List< SensorInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( SENSOR_TABLE,
    //    		         new String[] { "id", "shotId", "title", "date", "comment", "type", "value", "reftype" }, // columns
    //                             WHERE_SID_STATUS,
    //                             new String[] { Long.toString(sid), Long.toString(status) },
    //                             null,  // groupBy
    //                             null,  // having
    //                             null ); // order by
    Cursor cursor = myDB.rawQuery( qSensors1, new String[] { Long.toString(sid), Long.toString(status) } );
    if (cursor.moveToFirst()) {
      do {
        list.add( new SensorInfo( sid, 
                                 cursor.getLong(0),   // id
                                 cursor.getLong(1),   // shot-id
                                 cursor.getString(2), // title
                                 null,                // shot name
                                 cursor.getString(3), // date
                                 cursor.getString(4), // comment
                                 cursor.getString(5), // shot_type
                                 cursor.getString(6), // value
                                 (int)(cursor.getLong(7)) // reftype: item type
        ) ); 
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Sensors list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    // String[] where = new String[2];
    // where[0] = Long.toString(sid);
    for ( SensorInfo si : list ) { // set shot-names to the sensor infos
      // where[1] = Long.toString( si.getItemId() );
      // TDLog.v("shot ID " + si.getItemId() );
      // cursor = myDB.query( SHOT_TABLE, new String[] { "fStation", "tStation" }, WHERE_SID_ID, where, null, null, null );
      cursor = myDB.rawQuery( qShotStations, new String[] {  Long.toString(sid),  Long.toString( si.getItemId() ) } );
      if (cursor.moveToFirst()) {
        si.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
      }
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    return list;
  }

  /** @return the list of all sensor_info of a survey at a given shot
   * @param sid     survey ID
   * @param shotid  ID of the given shot
   */
  private List< SensorInfo > selectSensorsAtShot( long sid, long shotid )
  {
    List< SensorInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( SENSOR_TABLE,
    //                             new String[] { "id", "shotId", "title", "date", "comment", "type", "value", "reftype" }, // columns
    //                             "surveyId=? AND shotId=?", 
    //                             new String[] { Long.toString(sid), Long.toString(shotid) },
    //                             null,  // groupBy
    //                             null,  // having
    //                             null ); // order by
    Cursor cursor = myDB.rawQuery( qSensors2, new String[] { Long.toString(sid), Long.toString(shotid) } );
    if (cursor.moveToFirst()) {
      do {
        list.add( new SensorInfo( sid, 
                                 cursor.getLong(0),   // id
                                 cursor.getLong(1),   // shot-id
                                 cursor.getString(2), // title
                                 null,                // shot name
                                 cursor.getString(3), // date
                                 cursor.getString(4), // comment
                                 cursor.getString(5), // shot_type
                                 cursor.getString(6), // value
                                 (int)(cursor.getLong(7)) // reftype: item type
        ) );
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Sensors list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    String[] where = new String[2];
    where[0] = Long.toString(sid);
    for ( SensorInfo si : list ) { // set shot-names to the sensor infos
      where[1] = Long.toString( si.getItemId() );
      // cursor = myDB.query( SHOT_TABLE, new String[] { "fStation", "tStation" }, WHERE_SID_ID, where, null, null, null );
      cursor = myDB.rawQuery( qShotStations, where );
      if (cursor.moveToFirst()) {
        si.mShotName = cursor.getString(0) + "-" + cursor.getString(1);
      }
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    return list;
  }

  // AUDIO -----------------------------------------------------------

  /** @return audio info for a given shot
   * @param sid   survey ID
   * @param bid   shot ID
   */
  AudioInfo getAudioAtShot( long sid, long bid )
  {
    if ( myDB == null ) return null;
    AudioInfo ret = null;
    // Cursor cursor = myDB.query( AUDIO_TABLE,
    //                             new String[] { "id", "date" }, // columns
    //                             WHERE_SID_SHOTID, new String[] { Long.toString(sid), Long.toString(bid) },
    //                             null, null,  null ); 
    Cursor cursor = myDB.rawQuery( qAudioShot, new String[] { Long.toString(sid), Long.toString(bid) } );
    if (cursor.moveToFirst()) { // update
      ret = new AudioInfo( sid, 
                           cursor.getLong(0),   // id
                           bid,
                           cursor.getString(1), // date
                           1                    // MediaInfo.TYPE_SHOT
                         );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  /** @return audio info of a plot
   * @param sid   survey ID
   * @param aid   audio ID
   * @param bid   plot ID
   */
  AudioInfo getAudioAtPlot( long sid, long aid, long bid )
  {
    if ( myDB == null ) return null;
    AudioInfo ret = null;
    // Cursor cursor = myDB.query( AUDIO_TABLE,
    //                             new String[] { "id", "date" }, // columns
    //                             WHERE_SID_SHOTID, new String[] { Long.toString(sid), Long.toString(bid) },
    //                             null, null,  null ); 
    Cursor cursor = myDB.rawQuery( qAudioPlot, new String[] { Long.toString(sid), Long.toString(aid), Long.toString(bid) } );
    if (cursor.moveToFirst()) { // update
      ret = new AudioInfo( sid, 
                           cursor.getLong(0),   // id
                           bid,
                           cursor.getString(1), // date
                           2                    // MediaInfo.TYPE_PLOT
                         );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  // negative id used for sketch audios
  // positive id used for blocks audios
  /** @return new audio record ID
   * @param sid        survey ID
   * @param item_id    reference item ID: use 0 
   * @param reftype    reference item type
   */
  long nextAudioId( long sid ) // , long item_id, long reftype )
  {
    // long id = minId( AUDIO_TABLE, sid );
    long id = maxId( AUDIO_TABLE, sid );  // FIXME replaced min with max
    // TDLog.v( "Next audio sid " + sid + " id " + id );
    // insertAudio( sid, id, item_id, TDUtil.currentDate(), reftype );
    return id;
  }

  /** @return the audio ID for given item ID and reference type (next audio ID if not found; -1 on error)
   * @param sid     survey ID
   * @param item_id item ID
   * @param reftype reference type
   * @note this does not insert the audio record in the database
   */
  long getAudioIdByItem( long sid, long item_id, long reftype )
  {
    if ( myDB == null ) return -1L;
    long ret = -1;
    // TDLog.v("get audio by item: sid " + sid + " item " + item_id + " type " + reftype );
    Cursor cursor = myDB.rawQuery( qAudioIdItem, new String[] { Long.toString(sid), Long.toString(item_id), Long.toString(reftype) } );
    if (cursor.moveToFirst()) { // update
      ret = cursor.getLong(0);
      // TDLog.v("   found " + ret );
    } else {
      ret = nextAudioId( sid ); // , item_id, reftype );
      // TDLog.v("   not found " + ret );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }
    

  private ContentValues makeAudioContentValues( long sid, long id, long item_id, String date, long reftype )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId", sid );
    cv.put( "id",       id );
    cv.put( "shotId",   item_id );
    cv.put( "date",     date );
    cv.put( "reftype",  reftype );
    return cv;
  }

  /** insert audio record
   * @param sid      survey id
   * @param id       audio ID
   * @param item_id  shot-id or sketch audio index
   * @param date     date (can be null)
   * @param reftype  reference item type (used only for new audio record)
   */
  private long insertAudio( long sid, long id, long item_id, String date, long reftype )
  {
    if ( myDB == null ) return -1L;
    // TDLog.v("Insert audio: SID " + sid + " ID " + id + " item " + item_id + " type " + reftype );
    if ( id == -1L ) id = maxId( AUDIO_TABLE, sid );
    if ( date == null ) date = TDUtil.currentDate();
    ContentValues cv = makeAudioContentValues( sid, id, item_id, date, reftype );
    if ( ! doInsert( AUDIO_TABLE, cv, "insert audio" ) ) return -1L;
    return id;
  }

  /** update or insert audio record
   * @param sid  survey ID
   * @param id   audio ID
   * @param bid  reference ID (block or plot)
   * @param date date
   * @param reftype reference item type (used only for new audio record)
   * @note called by AudioDialog (when recording has been stopped)
   */
  void updateAudio( long sid, long id, long bid, String date, long reftype )
  {
    if ( myDB == null ) return; // false;
    // boolean ret = false;
    // TDLog.v("Update audio: SID " + sid + " ID " + id + " item " + bid + " type " + reftype );
    if ( id >= 0 ) {
      String[] args = new String[] { Long.toString(sid), Long.toString(id) };
      Cursor cursor = myDB.query( AUDIO_TABLE, new String[] { "date" }, WHERE_SID_ID, args, null, null,  null ); 
      if (cursor.moveToFirst()) { // update
        // String where = "WHERE surveyId=? AND id=?";
        ContentValues cv = new ContentValues();
        cv.put( "date", date );
        // ret = ( myDB.update( AUDIO_TABLE, cv, WHERE_SID_SHOTID, args ) > 0 );
        // TDLog.v("Set audio - update ... sid " + sid + " id " + id + " (bid " + bid + ")" );
        myDB.update( AUDIO_TABLE, cv, WHERE_SID_ID, args );
        if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
        // updateAudioStmt.bindString( 1, date );
        // updateAudioStmt.bindString( 2, Long.toString(sid) );
        // updateAudioStmt.bindString( 3, Long.toString(bid) );
        // ret = doStatement( updateAudioStmt, "audio update" );
        return;
      }
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    // insert
    // TDLog.v("Set audio - insert ... sid " + sid + " id " + id + " bid " + bid );
    insertAudio( sid, id, bid, date, reftype ); // id was -1
  }

  /** update a audio record datetime
   * @param sid     survey ID
   * @param id      audio ID
   * @param date    datetime
   */
  void setAudioTime( long sid, long id, String date )
  {
    if ( myDB == null ) return; // false;
    String[] args = new String[] { Long.toString(sid), Long.toString(id) };
    ContentValues cv = new ContentValues();
    cv.put( "date", date );
    myDB.update( AUDIO_TABLE, cv, WHERE_SID_ID, args );
  }

  /** @return the list of all the audio of a survey (ordered by reference type)
   * @param sid   survey ID
   */
  List< AudioInfo > selectAllAudios( long sid )
  {
    List< AudioInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( AUDIO_TABLE,
    //                             new String[] { "id", "shotId", "date" }, // columns
    //                             WHERE_SID, new String[] { Long.toString(sid) },
    //                             null, null,  null ); 
    // TDLog.v("Select all audio: sid " + sid );
    Cursor cursor = myDB.rawQuery( qAudiosAll, new String[] { Long.toString(sid) } );
    if (cursor.moveToFirst()) {
      do {
        // TDLog.v("Select all audio: " + sid + " " + cursor.getLong(0) + " " + cursor.getLong(1) );
        list.add( new AudioInfo( sid, 
                                 cursor.getLong(0),   // id
                                 cursor.getLong(1),   // shotId
                                 cursor.getString(2), // date
                                 (int)(cursor.getLong(3)) // reftype
                               ) );
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Audios list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  /** @return the list of all the audio of a survey of a given reference type
   * @param sid     survey IDA
   * @param reftype reference type
   */
  List< AudioInfo > selectAllAudios( long sid, int reftype )
  {
    List< AudioInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( AUDIO_TABLE,
    //                             new String[] { "id", "shotId", "date" }, // columns
    //                             WHERE_SID, new String[] { Long.toString(sid) },
    //                             null, null,  null ); 
    Cursor cursor = myDB.rawQuery( qAudiosByReftype, new String[] { Long.toString(sid), Long.toString(reftype) } );
    if (cursor.moveToFirst()) {
      do {
        list.add( new AudioInfo( sid, 
                                 cursor.getLong(0),   // id
                                 cursor.getLong(1),   // shotId
                                 cursor.getString(2), // date
                                 reftype
                               ) );
      } while (cursor.moveToNext());
    }
    // TDLog.Log( TDLog.LOG_DB, "select All Audios list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  // /** drop an audio record
  //  * @param sid   survey id
  //  * @param bid   shot-id is positive, sketch audio index if negative
  //  */
  // void deleteAudio( long sid, long bid ) 
  // {
  //   if ( myDB == null ) return;
  //   try {
  //     myDB.delete( AUDIO_TABLE, WHERE_SID_SHOTID, new String[]{ Long.toString(sid), Long.toString(bid) } );
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //   } catch (SQLiteException e) { logError("audio delete", e); }
  // }

  /** drop an audio record
   * @param sid   survey ID
   * @param id    audio ID
   */
  void deleteAudioRecord( long sid, long id ) 
  { 
    deleteFromTable( sid, id, AUDIO_TABLE );
  }

  // // delete a neg audio record 
  // // @param sid   survey id
  // // @param id    audio id (neg)
  // void deleteNegAudio( long sid, long id )
  // {
  //   if ( myDB == null ) return;
  //   try {
  //     myDB.delete( AUDIO_TABLE, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e ); 
  //   } catch (SQLiteException e) { logError("photo delete", e); }
  // }

  // PHOTO ---------------------------------------------------------------------------

  /** @return the number of photos of a survey with a given status
   * @param sid    survey ID
   * @param status photo status
   * @note the number of photos is used to avoid opening the photo-list dialog in case there is no photo
   */
  int countAllPhotos( long sid, long status )
  {
    if ( myDB == null ) return -1;
    int ret = 0;
    Cursor cursor = myDB.rawQuery( qjCountPhoto, new String[] { Long.toString(sid), Long.toString(status) } );
    if ( cursor.moveToFirst() ) { // update
      ret = (int)cursor.getLong(0);
    }
    if ( ! cursor.isClosed() ) cursor.close();
    return ret;
  }

  // /** @return the number of shot photos of a survey
  //  * @param sid    survey ID
  //  * @param status photo status
  //  */
  // int countAllShotPhotos( long sid, long status )
  // {
  //   if ( myDB == null ) return 0;
  //   int ret = 0;
  //   Cursor cursor = myDB.rawQuery( cntShotPhotos, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(status) } );
  //   if (cursor.moveToFirst()) {
  //     ret = (int)( cursor.getLong(0) );
  //   }
  //   if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
  //   return ret;
  // }

  //
  // select p.id, p.shotId, p.title, s.fStation, s.tStation, p.date, p.comment from photos as p join shots as s on p.shotId=s.id where p.surveyId=? and s.surveyId=? and p.status=?
  //

  /** @return the photos at plots of a survey
   * @param sid      survey ID
   * @param status   plot status [20241018 was photo status]
   */
  private List< PhotoInfo > selectAllPhotosPlot( long sid, long status )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.rawQuery( qjPhotosPlot, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(status) } );
    if (cursor.moveToFirst()) {
      do {
        list.add( new PhotoInfo( sid, 
                                 cursor.getLong(0), // id
                                 cursor.getLong(1),
                                 cursor.getString(2),
                                 cursor.getString(3),      // plot name
                                 cursor.getString(4),
                                 cursor.getString(5),
                                 (int)(cursor.getLong(6)), // camera
                                 cursor.getString(7),      // code
                                 (int)(cursor.getLong(8))  // reftype
                 ) );
      } while (cursor.moveToNext());
    }
    // TDLog.v( "select All Photos Plot list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  /** @return the photos at xsections of a survey
   * @param sid      survey ID
   * @param status   xsection status [20241018 was photo status]
   */
  private List< PhotoInfo > selectAllPhotosXSection( long sid, long status )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.rawQuery( qjPhotosXSection, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(status) } );
    if (cursor.moveToFirst()) {
      do {
        list.add( new PhotoInfo( sid, 
                                 cursor.getLong(0), // id
                                 cursor.getLong(1),
                                 cursor.getString(2),
                                 cursor.getString(3),      // xsection plot name
                                 cursor.getString(4),
                                 cursor.getString(5),
                                 (int)(cursor.getLong(6)), // camera
                                 cursor.getString(7),      // code
                                 (int)(cursor.getLong(8))  // reftype
                 ) );
      } while (cursor.moveToNext());
    }
    // TDLog.v( "select All Photos XSection list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  /** @return the photos at shots of a survey
   * @param sid      survey ID
   * @param status   shot status [20241018 was photo status]
   * @note used also by the ShotWindow
   */
  List< PhotoInfo > selectAllPhotosShot( long sid, long status )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.rawQuery( qjPhotosShot, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(status) } );
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
                                 (int)(cursor.getLong(7)), // camera
                                 cursor.getString(8),      // code
                                 (int)(cursor.getLong(9))  // reftype
                 ) );
      } while (cursor.moveToNext());
    }
    // TDLog.v(  "select All Photos Shot list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  /** @return the photos of a survey
   * @param sid      survey ID
   * @param status   ref-item status [20241018 was photo status]
   */
  List< PhotoInfo > selectAllPhotos( long sid, long status )
  {
    TDLog.v("DB select all photos");
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB != null ) {
      list.addAll( selectAllPhotosShot( sid, status ) );
      list.addAll( selectAllPhotosPlot( sid, status ) );
      list.addAll( selectAllPhotosXSection( sid, status ) );
    }
    return list;
  }

  /** insert or update a photo
   * @param sid       survey id
   * @param id        photo id (or -1)
   * @param item_id   reference item ID: shot ID or plot ID
   * @param title     photo title
   * @param date      date
   * @param comment   comment
   * @param camera    camera type
   * @param code      geomophology code
   * @param reftype   reference item type
   * @return id of the record (-1 on error)
   * @note used only for x-sections - could it be used also for shots and plots ?
   */
  long insertOrUpdatePhoto( long sid, long id, long item_id, String title, String date, String comment, int camera, String geocode, int reftype )
  {
    if ( myDB == null ) return -1;
    TDLog.v("insert / update Photo: id " + id + " reftype " + reftype );
    boolean insert = (id < 0);
    Cursor cursor = null;
    if ( ! insert ) {
      cursor = myDB.rawQuery( qjPhoto, new String[] { Long.toString(sid), Long.toString(id), Long.toString(reftype) } );
      if ( cursor.moveToFirst() ) { // update
        updatePhoto( sid, id, title, date, comment, camera, geocode );
        // TODO handle return
      } else {
        insert = true;
      }
      if ( ! cursor.isClosed() ) cursor.close();
    } 
    if ( insert ) id = insertPhotoRecord( sid, -1L, item_id, title, date, comment, camera, geocode, reftype );
    return id;
  }

  /** @return the photos of a survey at a shot
   * @param sid      survey ID
   * @param shotid   shot ID
   */
  List< PhotoInfo > selectPhotoAtShot( long sid, long shotid )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    // Cursor cursor = myDB.query( PHOTO_TABLE,
    //                             new String[] { "id", "shotId", "title", "date", "comment", "camera" }, // columns
    //                             WHERE_SID_SHOTID, new String[] { Long.toString(sid), Long.toString(shotid) },
    //                             null, null, null ); 
    Cursor cursor = myDB.rawQuery( qjShotPhotos, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(shotid) } );
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
                                 (int)(cursor.getLong(7)),
                                 cursor.getString(8),
                                 (int)(cursor.getLong(9))
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


  /** delete a photo record
   * @param sid   survey ID
   * @param id    photo ID
   */
  void deletePhotoRecord( long sid, long id ) { deleteFromTable( sid, id, PHOTO_TABLE ); }



  // FIXED -------------------------------------------------------------------

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
        list.add( new FixedInfo( cursor.getLong(0),   // id
                                 cursor.getString(1), // station
                                 cursor.getDouble(2), // longitude
                                 cursor.getDouble(3), // latitude
                                 cursor.getDouble(4), // ellipsoid height
                                 cursor.getDouble(5), // geoid height
                                 cursor.getString(6), // comment
                                 // skip status
                                 cursor.getLong(8),   // source type
                                 cursor.getString(9),  // cs name
                                 cursor.getDouble(10), // cs longitude
                                 cursor.getDouble(11), // cs latitude
                                 cursor.getDouble(12), // cs altitude
				                 cursor.getLong(13),   // cs decimals
                                 cursor.getDouble(14), // convergence
                                 cursor.getDouble(15), // accuracy
                                 cursor.getDouble(16), // accuracy V
                                 cursor.getDouble(17), // meters to units
                                 cursor.getDouble(18)  // meters to vert units
        ) );
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    // TDLog.Log( TDLog.LOG_DB, "select all fixeds " + sid + " size " + list.size() );
    return list;
  }

  /** @return first fixed data of the survey (station lat-long-h_geo cs and cs lat-long-h_geo)
   * @param name   survey name
   * @param status status of selected fixeds
   * @note h_geo is geoid altitude
   */
  public FixedInfo selectSurveyFixed( String name, int status )
  {
    // ArrayList< FixedInfo > ret = new ArrayList<>();
    FixedInfo info = null;
    Cursor cursor = myDB.rawQuery( qFixeds, new String[]{ name } );
    if (cursor.moveToFirst()) {
      do {
        if ( cursor.getLong(14) == status ) {
          info = new FixedInfo( -1, cursor.getString(0), // station
            cursor.getDouble(1), // latitude
            cursor.getDouble(2),
            cursor.getDouble(3), // ellipsoid h (altitude)
            cursor.getDouble(4), // geoid altitude (altimetric)
            "", -1,              // comment - source
            cursor.getString(5), // cs_name
            cursor.getDouble(6), // cs_latitude
            cursor.getDouble(7),
            cursor.getDouble(8), // cs_altitude (geoid)
            0,                   // nr decimals
            cursor.getDouble(9),  // convergence
            cursor.getDouble(10), // accuracy
            cursor.getDouble(11), // accuracy_v
            cursor.getDouble(12), // meters to units
            cursor.getDouble(13)  // meters to vert units
          );
          break;
          // ret.add( info );
        }
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return info;
  }

  /** @return the list of fixeds at a given station
   * @param sid   survey ID
   * @param name  station name
   */
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
       			  cursor.getLong(13),  // cs_decimals
                                 cursor.getDouble(14),
                                 cursor.getDouble(15),
                                 cursor.getDouble(16),
                                 cursor.getDouble(17), // meters to units
                                 cursor.getDouble(18)  // meters to vert units
        ) );
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    // TDLog.Log( TDLog.LOG_DB, "select all fixeds " + sid + " size " + list.size() );
    return list;
  }

  // STATION ------------------------------------------------------------------

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

  /** @return the name of the TO station of the shot with the given FROM station (or null if the shot was not found)
   * @param sid    survey ID
   * @param fStation FROM station
   * @note the returned TO station can be empty
   */
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

  /** @return the name of the last station
   * @param sid    survey ID
   */
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
  //         TDLog.e( "getNextStationName parseInt error: " + cursor.getString(0) + " " + cursor.getString(1) );
  //       }
  //     } while (cursor.moveToNext());
  //   }
  //   if (cursor != null && !cursor.isClosed()) cursor.close();
  //   ++ ret;
  //   return Integer.toString(ret);
  // }

  // PLOT ------------------------------------------------------------------

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
    plot.center = new Vector3D( (float)(cursor.getDouble(15)), (float)(cursor.getDouble(16)), (float)(cursor.getDouble(17)) );
    return plot;
  }

  /** @return the list of all plots of a survey
   * @param sid        survey ID
   * @param where_str  WHERE string
   * @param where      WHERE args
   */
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

  /** @return the list of all plot names of a survey
   * @param survey     survey name
   */
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

  /** @return the list of plots for a given survey
   * @param sid    survey ID
   */
  List< PlotInfo > selectAllPlots( long sid )
  {
    return doSelectAllPlots( sid, WHERE_SID, new String[] { Long.toString(sid) } );
  }

  /** @return the list of plots for a given survey and with given status
   * @param sid    survey ID
   * @param status plot status
   */
  List< PlotInfo > selectAllPlots( long sid, long status )
  {
    return doSelectAllPlots( sid, WHERE_SID_STATUS, new String[] { Long.toString(sid), Long.toString(status) } );
  }

  /** @return the list of plots for a given survey and with given status, and type
   * @param sid    survey ID
   * @param status plot status
   * @param plot_type   plot type
   */
  List< PlotInfo > selectAllPlotsWithType( long sid, long status, long plot_type )
  {
    return doSelectAllPlots( sid, 
                             "surveyId=? and status=? and type=? ",
                             new String[] { Long.toString(sid), Long.toString(status), Long.toString(plot_type) }
    );
  }

   // NOT USED
   // List< PlotInfo > selectAllPlotsWithTypeOrientation( long sid, long status, long plot_type, boolean landscape )
   // {
   //   return doSelectAllPlots( sid, 
   //                            "surveyId=? and status=? and type=? and orientation=" + (landscape? 1 : 0),
   //                            new String[] { Long.toString(sid), Long.toString(status), Long.toString(plot_type) }
   //   );
   // }

   /** @return list of plot xsections or a given survey and with given status and type
    * @param sid    survey ID
    * @param status plot status
    * @param plot_type   plot (xsection) type
    * @param parent   parent plot name (null if shared xsections)
    * @note NEW X_SECTIONS hide = parent plot
    * @note used by DrawingWindow
    */
   List< PlotInfo > selectAllPlotSectionsWithType( long sid, long status, long plot_type, String parent )
   {
     if ( ! PlotType.isAnySection( plot_type ) ) return null; // safety check: FIXME what about PLOT_PHOTO ?
     if ( parent == null ) {
       return doSelectAllPlots( sid, 
                              "surveyId=? and status=? and type=?",
                              new String[] { Long.toString(sid), Long.toString(status), Long.toString(plot_type) }
       );
     }
     return doSelectAllPlots( sid, 
                              "surveyId=? and status=? and type=? and hide=?",
                              new String[] { Long.toString(sid), Long.toString(status), Long.toString(plot_type), parent }
     );
   }


  /** @return list of plot xsections or a given survey and with given status 
   * @param sid    survey ID
   * @param status plot status
   * @note the values of the type(s) are taken from PlotType: 0 X_SECTION, 3 H_SECTION, 5 SECTION, 7 XH_SECTION
   */
  List< PlotInfo > selectAllPlotsSection( long sid, long status )
  {
    return doSelectAllPlots( sid, 
                             "surveyId=? and status=? and ( type=0 or type=3 or type=5 or type=7 )",
                             new String[] { Long.toString(sid), Long.toString(status) }
    );
  }

  /** @return the list of plots at a station
   * @param sid    survey ID
   * @param name   station name
   */
  private List< PlotInfo > selectPlotsAtStation( long sid, String name )
  {
    return doSelectAllPlots( sid, 
                             WHERE_SID_START,
                             new String[] { Long.toString(sid), name }
    );
  }

  /** select all the plot names of a survey to delete shp exports
   * @param sid    survey ID
   * @return list of plot names
   */
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

  /** @return the origin of the first plot (of type plan, or null if no plots
   * @param sid    survey ID
   */
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
 
  /** @return the plot name 
   * @param sid   survey ID
   * @param pid   plot ID
   */
  String getPlotName( long sid, long pid )
  {
    if ( myDB == null ) return null;
    String ret = null;
    Cursor cursor = myDB.query( PLOT_TABLE, mPlotName,
              WHERE_SID_ID,
              new String[] { Long.toString(sid), Long.toString(pid) },
              null, null, null );
    if (cursor != null ) {
      if (cursor.moveToFirst() ) ret = cursor.getString(0);
      if (!cursor.isClosed()) cursor.close();
    }
    return ret;
  }

  /** @return the info of a plot
   * @param sid    survey ID
   * @param name   plot name
   */
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
 
  /** @return the info of a plot
   * @param sid    survey ID
   * @param pid    plot ID
   */
  PlotInfo getPlotInfo( long sid, long pid )
  {
    PlotInfo plot = null;
    if ( myDB != null && pid >= 0 ) {
      Cursor cursor = myDB.query( PLOT_TABLE, mPlotFields,
                WHERE_SID_ID,
                new String[] { Long.toString(sid), Long.toString(pid) },
                null, null, null );
      if (cursor != null ) {
        if (cursor.moveToFirst() ) plot = makePlotInfo( sid, cursor );
        if (!cursor.isClosed()) cursor.close();
      }
    }
    return plot;
  }

  // XSECTION ------------------------------------------------------------

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
          long plot_type = cursor.getInt(1);    // plot_type is not used
          String name   = cursor.getString(2);
          // TDLog.v( "DB plot name " + name + " prefix " + prefix );
          if ( name.startsWith( prefix ) /* && ( plot_type == PlotType.PLOT_PHOTO || plot_type == PlotType.PLOT_SECTION ) */ ) {
            try {
              int k = Integer.parseInt( name.substring( prefix_length ) );
              if ( k >= max ) max = k+1;
            } catch ( NumberFormatException e ) {
              TDLog.e( "DB getNextSectionId parse Int error: survey ID " + sid );
            }
          }
        } while (cursor.moveToNext());
      }
      if (!cursor.isClosed()) cursor.close();
    }
    // return prefix + Integer.toString(max);
    return String.format(Locale.US, "%s%d", prefix, max );
  }

  // SHOT -------------------------------------------------------------

  /** @return true if the given survey has a shot ID
   * @param sid    survey ID
   * @param id     shot ID
   */
  private boolean hasShotId( long sid, long id )
  {
    if ( myDB == null ) return false;
    Cursor cursor = myDB.rawQuery( qHasShotId, new String[] { Long.toString(sid), Long.toString(id) } );
    boolean ret = cursor.moveToFirst();
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  /** @return true if the survey has a shot between two given stations
   * @param sid    survey ID
   * @param fStation FROM station
   * @param tStation TO station
   */
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

  /** select a shot given the ID
   * @param id   shot ID
   * @param sid  survey ID
   * @return the shot
   */
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

  /** select the last shot with FROM station
   * @param sid   survey ID
   */
  DBlock selectLastShotWithFromStation( long sid )
  {
    // TDLog.Log( TDLog.LOG_DB, "selectShot " + id + "/" + sid );
    if ( myDB == null ) return null;
    Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
                                WHERE_SID_FROM, new String[] { Long.toString(sid) },
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

  /** @return the data block of the last leg
   * @param sid    survey ID
   */
  DBlock selectLastLegShot( long sid )
  {
    return selectPreviousLegShot( myNextId+1, sid );
  }

  /** @return the data block of the leg before a given shot
   * @param shot_id  shot ID
   * @param sid      survey ID
   */
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
    if ( TDString.isNullOrEmpty( station ) ) return list;
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
    if ( TDString.isNullOrEmpty( station ) ) return list;
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

  /** select all the shots between two stations, both 1-2 and 2-1
   * @param sid      survey ID
   * @param station1 first station
   * @param station2 second station
   * @return list of shots
   *
   * @note called by DrawingWindow to splay select shots for x-sections
   */
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

  /** select all the shots at one or two stations
   * @param sid        survey id
   * @param stations   stations names (must be unique)
   * @param with_legs   whether to include legs or not
   * @return list of shots
   */
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

  /** select all the splays at some stations
   * @param sid        survey id
   * @param stations   stations names 
   * @return list of shots
   */
  List< DBlock > selectAllSplaysAtStations( long sid, TreeSet< String > stations )
  {
    List< DBlock > list = new ArrayList<>();
    if ( stations == null || myDB == null ) return list;
    int sz = stations.size();
    if ( sz == 0 ) return list;
    for ( String station : stations ) {
      Cursor cursor = myDB.query( SHOT_TABLE, mShotFields,
        "surveyId=? and status=? and fStation=? and tStation=\"\"",
        new String[] { Long.toString(sid), TDStatus.NORMAL_STR, station },
        null, null, "id" );
      if (cursor.moveToFirst()) {
        do {
          // if ( cursor.getLong(11) == 0 ) { // non-leg blocks
            DBlock block = new DBlock();
            fillBlock( sid, block, cursor );
            list.add( block );
          // }
        } while (cursor.moveToNext());
      }
      // TDLog.Log( TDLog.LOG_DB, "select All Shots At Station list size " + list.size() );
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    return list;
  }

  /** select all the shots at a station
   * @param sid        survey id
   * @return list of shots
   */
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
   * @return list of shots
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

  /** select all shots with a given status
   * @param sid     surveyId
   * @param status  shot status
   * @return list of shots
   */
  List< DBlock > selectAllShots( long sid, long status ) // ANDROID-11 SQLiteDiskIOException
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

  /** get the export shots
   * @param sid     surveyId
   * @param status  shot status
   * @return list of shots
   */
  List< DBlock > selectAllExportShots( long sid, long status )
  {
    // TDLog.v( "B3 select shots all");
    List< DBlock > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query(SHOT_TABLE, mShotFullFields,
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

  /** get the export shots after a given shot-id
   * @param sid     surveyId
   * @param status  shot status
   * @param first   id of first shot to export
   * @return list of shots
   * @note used by TopoRobot export only
   */
  List< DBlock > selectExportShots( long sid, long status, long first )
  {
    // TDLog.v( "B3 select shots all");
    List< DBlock > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query(SHOT_TABLE, mShotFullFields,
                    WHERE_SID_STATUS_FROM, new String[]{ Long.toString(sid), Long.toString(status), Long.toString(first) },
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

  /** get the last non-blank shot
   * @param sid    survey ID
   * // param backshot  whether the DistoX is in backshot mode
   * @return the last block with either the from station (non-backshot) or the to station (backshot)
   * @note used only by StationName
   */
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

  // SURVEY ------------------------------------------------------------------------------

  SurveyInfo selectSurveyInfo( long sid )
  {
    SurveyInfo info = null;
    if ( myDB == null ) return null;
    Cursor cursor = myDB.query( SURVEY_TABLE,
                               new String[] { "name", "day", "team", "declination", "comment", "init_station", "xsections", "datamode", "extend", "calculated_azimuths" }, // columns
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
      info.mCalculatedAzimuths = (int)cursor.getLong( 9 );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return info;
  }

  /** @return the list of surveys
   */
  public List< String > selectAllSurveys() { return selectAllNames( SURVEY_TABLE ); }

  // ----------------------------------------------------------------------
  // SELECT: LIST SURVEY / CALIB NAMES

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


  // ----------------------------------------------------------------------
  // CONFIG DATA

  String getValue( String key )
  {
    if ( myDB == null ) {
      // TDLog.e( "Data Helper::getValue null DB");
      return null;
    }
    if ( TDString.isNullOrEmpty( key ) ) { // this is not an error
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
    } catch ( SQLiteException e ) { logError( "config get " + key, e );
    // } catch ( android.database.CursorWindowAllocationException e ) {
    } finally {
      if ( cursor != null && ! cursor.isClosed()) cursor.close();
    }
    return value;
  }

  void setValue( String key, String value )
  {
    if ( myDB == null ) {
      // TDLog.e( "Data Helper::setValue null DB");
      return;
    }
    if ( TDString.isNullOrEmpty( key ) ) {
      TDLog.e( "DB config: null key");
      return;
    }
    if ( TDString.isNullOrEmpty( value ) ) {
      TDLog.e( "DB config: null value");
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
        TDLog.e( "DB config: cannot get cursor for " + key + " " + value );
      }
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e ); 
    } catch (SQLiteException e ) { logError( "set value " + key + " " + value, e ); 
    } finally {
      if ( ! (cursor == null) && ! cursor.isClosed()) cursor.close();
      myDB.endTransaction();
    }
  }

  // SYMBOLS ------------------------------------------------------------------

  /** store a symbol state (enabled/disabled)
   * @param name    symbol name
   * @param enabled whether the symbol is enabled
   */
  void setSymbolEnabled( String name, boolean enabled ) 
  { 
    // if ( TDString.isNullOrEmpty( name ) ) return; // already handled by setValue
    // if ( name.startsWith("a_" ) ) TDLog.v("DB set symbol " + name + " enabled " + enabled );
    setValue( name, enabled? TDString.ONE : TDString.ZERO );
  }

  /** retrieve a symbol state (enabled/disabled)
   * @param name    symbol name
   * @return true if the symbol is enabled
   */
  boolean getSymbolEnabled( String name )
  { 
    // if ( TDString.isNullOrEmpty( name ) ) return false; // already handled by getValue
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

  /** check if a symbol state is stored in the database
   * @param name    symbol name
   * @return true if the symbol state is in the database
   */
  boolean hasSymbolName( String name ) { return ( getValue( name ) != null ); }

  // UTILITIES ----------------------------------------------------------------------
  /* Set the current survey/calib name.
   * If the survey/calib name does not exist a new record is inserted in the table
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
    if ( TDString.isNullOrEmpty( name ) ) return 0; 
    if ( myDB == null ) return 0;
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
      if ( ! doInsert( table, cv, "set name" ) ) return -1L;
    }
    return id;
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

  /** @return the ID of a plot
   * @param sid    survey ID
   * @param name   plot name
   */
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

  /** make the content-value set for a photo data
   * @param sid       survey id
   * @param id        photo id (or -1)
   * @param shotid    shot id
   * @param title     photo title
   * @param date      date
   * @param comment   comment
   * @param camera    camera type
   * @param code      geomophology code
   * @param reftype   reference item type
   * @return content-value set
   */
  private ContentValues makePhotoContentValues( long sid, long id, long shotid, long status, String title, String date, String comment, long camera, String code, long reftype )
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
    cv.put( "code",      (code == null)? TDString.EMPTY : code );
    cv.put( "reftype",   reftype );
    return cv;
  }

  /** insert a photo
   * @param sid       survey id
   * @param id        photo id (or -1)
   * @param item_id   reference item ID: shot ID or plot ID
   * @param title     photo title
   * @param date      date
   * @param comment   comment
   * @param camera    camera type
   * @param code      geomophology code
   * @param reftype   reference item type
   * @return id of the record (-1 on error)
   */
  long insertPhotoRecord( long sid, long id, long item_id, String title, String date, String comment, int camera, String code, int reftype )
  {
    if ( myDB == null ) return -1L;
    if ( id == -1L ) id = maxId( PHOTO_TABLE, sid );
    TDLog.v("do insert Photo: id " + id + " reftype " + reftype );
    ContentValues cv = makePhotoContentValues( sid, id, item_id, TDStatus.NORMAL, title, date, comment, camera, code, reftype );
    if ( ! doInsert( PHOTO_TABLE, cv, "photo insert" ) ) return -1L;
    return id;
  }

  /** @return the next ID for a photo
   * @param sid   survey ID
   */
  long nextPhotoId( long sid )
  {
    return maxId( PHOTO_TABLE, sid );
  }

  /** update a photo comment
   * @param sid     survey ID
   * @param id      photo ID
   * @param title   new title
   * @param date    new datetime
   * @param comment new photo comment
   * @param camera  new camera type
   * @param geocode new photo geocode
   * @return true if successful
   */
  boolean updatePhoto( long sid, long id, String title, String date, String comment, int camera, String geocode )
  {
    if ( myDB == null ) return false;
    ContentValues cv = new ContentValues();
    cv.put( "title",   title );
    cv.put( "date",    date );
    cv.put( "comment", comment );
    cv.put( "camera",  camera );
    cv.put( "code",    geocode );
    try {
      myDB.beginTransaction();
      myDB.update( PHOTO_TABLE, cv, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("photo update", e); 
    } finally { myDB.endTransaction(); }
    return true;
  }

  /** update a photo comment and geocode
   * @param sid     survey ID
   * @param id      photo ID
   * @param comment new photo comment
   * @param geocode new photo geocode
   * @return true if successful
   */
  boolean updatePhotoCommentAndCode( long sid, long id, String comment, String geocode )
  {
    if ( myDB == null ) return false;
    ContentValues cv = new ContentValues();
    cv.put( "comment", comment );
    cv.put( "code",    geocode );
    try {
      myDB.beginTransaction();
      myDB.update( PHOTO_TABLE, cv, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("photo update", e); 
    } finally { myDB.endTransaction(); }
    return true;
  }

  // /** delete a photo
  //  * @param sid     survey ID
  //  * @param id      photo ID
  //  */
  // void deletePhoto( long sid, long id )
  // {
  //   if ( myDB == null ) return;
  //   try {
  //     myDB.beginTransaction();
  //     myDB.delete( PHOTO_TABLE, WHERE_SID_ID, new String[]{ Long.toString(sid), Long.toString(id) } );
  //     myDB.setTransactionSuccessful();
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //   } catch (SQLiteException e) { logError("photo delete", e); 
  //   } finally { myDB.endTransaction(); }
  // }

  /** make the content-value set for a sensor data
   * @param sid       survey id
   * @param id        photo id (or -1)
   * @param item_id   reference item ID: shot id
   * @param status    ...
   * @param title     sensor title
   * @param date      sensor date
   * @param comment   comment
   * @param sensor_type sensor type
   * @param value     sensor value
   * @param reftype   reference item type
   * @return content-value set
   */
  private ContentValues makeSensorContentValues( long sid, long id, long item_id, long status,
       	   String title, String date, String comment, String sensor_type, String value, int reftype )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId",  sid );
    cv.put( "id",        id );
    cv.put( "shotId",    item_id );
    cv.put( "status",    status );
    cv.put( "title",     title );
    cv.put( "date",      date );
    cv.put( "comment",   (comment == null)? TDString.EMPTY : comment );
    cv.put( "type",      sensor_type );
    cv.put( "value",     value );
    cv.put( "reftype",   reftype );
    return cv;
  }

  /** insert a sensor data
   * @param sid       survey id
   * @param id        photo id (or -1)
   * @param itemid    reference item ID: shot ID
   * @param title     sensor title
   * @param date      sensor date
   * @param comment   comment
   * @param sensor_type      sensor type
   * @param value     sensor value
   * @param reftype   reference item type
   * @return id of the record (-1 on error)
   */
  long insertSensor( long sid, long id, long itemid, String title, String date, String comment, String sensor_type, String value, int reftype )
  {
    if ( myDB == null ) return -1L;
    if ( id == -1L ) id = maxId( SENSOR_TABLE, sid );
    ContentValues cv = makeSensorContentValues( sid, id, itemid, TDStatus.NORMAL, title, date, comment, sensor_type, value, reftype );
    if ( ! doInsert( SENSOR_TABLE, cv, "sensor insert" ) ) return -1L;
    return id;
  }

  /** @return the next ID for a sensor-data
   * @param sid   survey ID
   */
  long nextSensorId( long sid )
  {
    return maxId( SENSOR_TABLE, sid );
  }

  /** delete a sensor-data
   * @param sid   survey ID
   * @param id    sensor-data ID
   */
  void deleteSensor( long sid, long id )
  {
    if ( myDB == null ) return;
    updateStatus( SENSOR_TABLE, id, sid, TDStatus.DELETED );
  }

  /** update a sensor-data comment
   * @param sid   survey ID
   * @param id    sensor-data ID
   * @param comment new sensor-data comment
   */
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
  
  /** transfer a plot from one survey to another
   * @param sid      ID of the target survey
   * @param old_sid  ID of the source survey
   * @param pid      plot ID
   * @note myDB is checked non-null before transfer methods are called
   */
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

  // /** transfer a sketch from one survey to another
  //  * @param sid      ID of the target survey
  //  * @param old_sid  ID of the source survey
  //  * @param pid      sketch ID
  //  * @note myDB is checked non-null before transfer methods are called
  //  */
  // private void transferSketch( long sid, long old_sid, long pid )
  // {
  //   if ( transferSketchStmt == null ) {
  //     transferSketchStmt = myDB.compileStatement( "UPDATE sketches set surveyId=? WHERE surveyId=? AND id=?" );
  //   }
  //   transferSketchStmt.bindLong( 1, sid );
  //   transferSketchStmt.bindLong( 2, old_sid );
  //   transferSketchStmt.bindLong( 3, pid );
  //   try {
  //     transferSketchStmt.execute();
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //   } catch (SQLiteException e ) { logError("sketch transf", e); }
  // }

  boolean hasFixed( long sid, String station )
  {
    return ( getFixedId( sid, station ) != -1 );
  }
  
  /** N.B. only one location per station
   *       Before inserting a location drop existing deleted fixeds for the station
   * N.B. this must be called with id == -1L ( currently called only by SurveyWindow )
   */
  public long insertFixed( long sid, long id, String station, double lng, double lat, double h_ell, double h_geo,
                           String comment, long status, long source, double accur, double accur_v )
  {
    return insertFixed( sid, id, station, lng, lat, h_ell, h_geo, comment, status, source, "", 0, 0, 0, 2, 0, accur, accur_v, 1, 1 );
  }

  private ContentValues makeFixedContentValues( long sid, long id, String station, double lng, double lat, double h_ell, double h_geo,
                           String comment, long status, long source,
                           String cs, double cs_lng, double cs_lat, double cs_h_geo, long cs_n_dec, double conv, double accur, double accur_v,
                           double m_to_units, double m_to_vunits )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId",  sid );
    cv.put( "id",        id );
    cv.put( "station",   station );
    cv.put( "longitude", lng );
    cv.put( "latitude",  lat );
    cv.put( "altitude",  h_ell );
    cv.put( "altimetric", h_geo );
    cv.put( "comment",   (comment == null)? TDString.EMPTY : comment );
    cv.put( "status",    status );
    cv.put( "cs_name",   cs );
    cv.put( "cs_longitude", cs_lng );
    cv.put( "cs_latitude",  cs_lat );
    cv.put( "cs_altitude",  cs_h_geo );
    cv.put( "source",       source );
    cv.put( "cs_decimals",  cs_n_dec );
    cv.put( "convergence",  conv );
    cv.put( "accuracy",     accur );
    cv.put( "accuracy_v",   accur_v );
    cv.put( "m_to_units",   m_to_units );
    cv.put( "m_to_vunits",  m_to_vunits );
    return cv;
  }

  private long insertFixed( long sid, long id, String station, double lng, double lat, double h_ell, double h_geo,
                           String comment, long status, long source,
                           String cs, double cs_lng, double cs_lat, double cs_h_geo, long cs_n_dec, double conv, double accur, double accur_v,
                           double m_to_units, double m_to_vunits )
  {
    // TDLog.v( "insert fixed id " + id + " station " + station );
    if ( id != -1L ) return id;
    if ( myDB == null ) return -1L;
    long fid = getFixedId( sid, station );
    if ( fid != -1L ) return fid;     // check non-deleted fixeds
    dropDeletedFixed( sid, station ); // drop deleted fixed if any

    id = maxId( FIXED_TABLE, sid );
    // TDLog.Log( TDLog.LOG_DB, "insert Fixed id " + id );
    ContentValues cv = makeFixedContentValues( sid, id, station, lng, lat, h_ell, h_geo, comment, status, source,
       	     cs, cs_lng, cs_lat, cs_h_geo, cs_n_dec, conv, accur, accur_v, m_to_units, m_to_vunits );
    if ( ! doInsert( FIXED_TABLE, cv, "insert fixed" ) ) return -1L;
    return id;
  }

  /** @return key-value set to insert a plot
   * @param sid     survey ID
   * @param id      plot id
   * @param name    plot name
   * @param plot_type    plot type code
   * @param status  plot status
   * @param start   plot origin (plan/profile), viewing station (leg-xsection)
   * @param view    viewed station (leg-xstation)
   * @param xoffset display X offset
   * @param yoffset display Y offset
   * @param zoom    display zoom
   * @param azimuth azimuth (xsection / projected profile)
   * @param clino   clino (xsection)
   * @param hide    hiding stations (plan/profile), parent plot (xsection)
   * @param nick    comment (xsection)
   * @param orientation plot orientation, either PORTRAIT (0) or LANDSCAPE (1)
   * @param intercept   leg-xsection intercept abscissa
   * @param center_x    multileg xsection center X
   * @param center_y    multileg xsection center Y
   * @param center_z    multileg xsection center Z
   */
  private ContentValues makePlotContentValues( long sid, long id, String name, long plot_type, long status, String start, String view,
                          double xoffset, double yoffset, double zoom, double azimuth, double clino,
                          String hide, String nick, int orientation, int maxscrap, double intercept,
                          double center_x, double center_y, double center_z )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId", sid );
    cv.put( "id",       id );
    cv.put( "name",     name );
    cv.put( "type",     plot_type );
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
    cv.put( "center_x",    center_x );
    cv.put( "center_y",    center_y );
    cv.put( "center_z",    center_z );
    return cv;
  }

  /** insert a plot
   * @param sid     survey ID
   * @param id      plot id
   * @param name    plot name
   * @param plot_type    plot type code
   * @param status  plot status
   * @param start   ... viewing station (xsection)
   * @param view    viewed station (xstation)
   * @param xoffset display X offset
   * @param yoffset display Y offset
   * @param zoom    display zoom
   * @param azimuth azimuth (xsection / projected profile)
   * @param clino   clino (xsection) / oblique-angle (projected profile)
   * @param hide    hiding stations (plan/profile), parent plot (xsection)
   * @param nick    comment (xsection)
   * @param orientation plot orientation, either PORTRAIT (0) or LANDSCAPE (1)
   * @return ID of the new plot, -1 if failure
   */
  long insertPlot( long sid, long id, String name, long plot_type, long status, String start, String view,
                   double xoffset, double yoffset, double zoom, double azimuth, double clino,
                   String hide, String nick, int orientation )
  {
    return insertPlot( sid, id, name, plot_type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation, -1, 0, 0, 0 );
  }

  /** insert a plot
   * @param sid     survey ID
   * @param id      plot id
   * @param name    plot name
   * @param plot_type    plot type code
   * @param status  plot status
   * @param start   ...
   * @param view    viewed station (xstation)
   * @param xoffset display X offset
   * @param yoffset display Y offset
   * @param zoom    display zoom
   * @param azimuth azimuth (xsection / projected profile)
   * @param clino   clino (xsection) / oblique angle (projected profile)
   * @param hide    hiding stations (plan/profile), parent plot (xsection)
   * @param nick    comment (xsection)
   * @param orientation plot orientation, either PORTRAIT (0) or LANDSCAPE (1)
   * @param intercept   leg-xsection intercept abscissa
   * @param center_x    multileg xsection center X
   * @param center_y    multileg xsection center Y
   * @param center_z    multileg xsection center Z
   * @return ID of the new plot, -1 if failure
   */
  long insertPlot( long sid, long id, String name, long plot_type, long status, String start, String view,
                   double xoffset, double yoffset, double zoom, double azimuth, double clino,
                   String hide, String nick, int orientation,
                   double intercept, double center_x, double center_y, double center_z )
  {
    // TDLog.v( "DB insert plot " + name + " start " + start + " azimuth " + azimuth );
    // TDLog.v( "insert plot <" + name + "> hide <" + hide + "> nick <" + nick + ">" );
    if ( myDB == null ) return -1L;
    long ret = getPlotId( sid, name );
    if ( ret >= 0 ) return -1;
    if ( view == null ) view = TDString.EMPTY;
    if ( id == -1L ) id = maxId( PLOT_TABLE, sid );
    // maxscrap = 0
    ContentValues cv = makePlotContentValues( sid, id, name, plot_type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation, 0, 
                                              intercept, center_x, center_y, center_z );
    if ( ! doInsert( PLOT_TABLE, cv, "plot insert" ) ) { // failed
      id = -1L;
    }
    return id;
  }

  /** @return the max shot id of a survey (max = one past the largest)
   * @param sid   survey id
   */
  long maxShotId( long sid )
  {
    return maxId( SHOT_TABLE, sid );
  }
  
  /** move all shots records, starting with a given ID, from one survey to another
   * @param old_sid   survey ID of source survey
   * @param old_id    ID of starting shot
   * @param new_sid   survey ID of target survey
   * @return true if success
   */
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

  void updateFixedAltitude( long id, long sid, double h_ell, double h_geo )
  {
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    cv.put( "altitude",   h_ell );
    cv.put( "altimetric", h_geo );
    doUpdate( FIXED_TABLE, cv, sid, id, "fix alt" );
  }

  void updateFixedData( long id, long sid, double lng, double lat, double h_ell ) // ,  double accur, double accur_v  )
  {
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    cv.put( "longitude",  lng );
    cv.put( "latitude",   lat );
    cv.put( "altitude",   h_ell );
    // cv.put( "accuracy",   accur );
    // cv.put( "accuracy_v", accur_v );
    doUpdate( FIXED_TABLE, cv, sid, id, "fix dat1" );
  }

  void updateFixedData( long id, long sid, double lng, double lat, double h_ell, double h_geo ) // , double accur, double accur_v )
  {
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    cv.put( "longitude",  lng );
    cv.put( "latitude",   lat );
    cv.put( "altitude",   h_ell );
    cv.put( "altimetric", h_geo );
    // cv.put( "accuracy",   accur );
    // cv.put( "accuracy_v", accur_v );
    doUpdate( FIXED_TABLE, cv, sid, id, "fix dat2" );
  }

  /**
   * @param id    fixed ID 
   * @param sid   survey ID
   * @param cs    CS name
   * @param lng   CS longitude - east [degree / meters]
   * @param lat   CS latitude - north
   * @param h_ell CS altitude [m]
   * @param n_dec number of decimals
   * @param conv  convergence [degree]
   * @param m_to_vunits  meters to vert. units
   */
  void updateFixedCS( long id, long sid, String cs, double lng, double lat, double h_ell, long n_dec, double conv, double m_to_units, double m_to_vunits )
  {
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    if ( cs != null && cs.length() > 0 ) {
      cv.put( "cs_name", cs );
      cv.put( "cs_longitude", lng );
      cv.put( "cs_latitude",  lat );
      cv.put( "cs_altitude",  h_ell );
      cv.put( "cs_decimals",  n_dec );
      cv.put( "convergence",  conv );
      cv.put( "m_to_units",   m_to_units );
      cv.put( "m_to_vunits",  m_to_vunits );
    } else {
      cv.put( "cs_name", TDString.EMPTY );
      cv.put( "cs_longitude", 0 );
      cv.put( "cs_latitude",  0 );
      cv.put( "cs_altitude",  0 );
      cv.put( "cs_decimals",  2 );
      cv.put( "convergence",  0 );
      cv.put( "m_to_units",   1 );
      cv.put( "m_to_vunits",  1 );
    }
    doUpdate( FIXED_TABLE, cv, sid, id, "fix cs" );
  }

  /** @return true if the database contains the survey name
   * @param name   survey name
   */
  public boolean hasSurveyName( String name )  { return hasName( name, SURVEY_TABLE ); }

  /** @return true if the database contains the plot name
   * @param sid    survey ID
   * @param name   plot name
   */
  public boolean hasSurveyPlotName( long sid, String name )  { return hasName( sid, name, PLOT_TABLE ); }

  /** @return true if the database table contains the given name
   * @param name   name
   * @param table  table
   */
  private boolean hasName( String name, String table )
  {
    if ( TDString.isNullOrEmpty( name ) ) return false;
    if ( myDB == null ) {
      TDLog.e( DeviceHelper.ERROR_NULL_DB + "DB data has name");
      return false;
    }
    boolean ret = false;
    Cursor cursor = null;
    try {
      String query = String.format("SELECT name FROM %s WHERE name='%s' COLLATE NOCASE", table, name );
      // TDLog.v("DB query string <" + query + ">" );
      cursor = myDB.rawQuery( query, new String[] { } );
      ret = ( cursor != null && cursor.moveToFirst() ); 
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch ( RuntimeException e ) { TDLog.e( e.getMessage() );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  /** @return true if the database table contains the given name
   * @param sid    survey ID
   * @param name   name
   * @param table  table
   */
  private boolean hasName( long sid, String name, String table )
  {
    if ( TDString.isNullOrEmpty( name ) ) return false;
    if ( myDB == null ) {
      TDLog.e( DeviceHelper.ERROR_NULL_DB + "DB data has name");
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

  /**
   * @return a HashSet with all stations that should be mirrored in triangulation loop closure.
   */
  public HashSet< String > getTriMirroredStations( long sid )
  {
    HashSet< String > ret = new HashSet< String >();
    if ( myDB == null ) return ret;
    Cursor cursor = myDB.query(
        TRI_MIRRORED_STATIONS_TABLE,
        new String[] { "name" },
        WHERE_SID,
        new String[] { Long.toString( sid ) },
        null,
        null,
        null
    );
    if ( cursor != null ) {
      if ( cursor.moveToFirst() ) {
        do {
          ret.add( cursor.getString(0) );
        } while (cursor.moveToNext());
      }
      if ( !cursor.isClosed() ) cursor.close();
    }
    return ret;
  }

  /** toggle the mirrored status of a station. Relevant to triangulation loop closure.
   * @param sid      survey ID
   * @param station  station name
   */
  public void toggleTriMirroredStation( long sid, String station )
  {
    if ( myDB == null ) return;
    Cursor cursor = myDB.query(
      TRI_MIRRORED_STATIONS_TABLE,
      new String[] { "name" },
      WHERE_SID_NAME,
      new String[] { Long.toString(sid), station },
      null,
      null,
      null
    );
    if ( cursor.moveToFirst() ) {
      myDB.delete(
        TRI_MIRRORED_STATIONS_TABLE,
        "surveyId=? and name=?",
        new String[] { Long.toString(sid), station }
      );
    } else {
      ContentValues cv = new ContentValues();
      cv.put( "surveyId", sid );
      cv.put( "name", station );
      doInsert( TRI_MIRRORED_STATIONS_TABLE, cv, "insert mirrored station" );
    }
    if ( !cursor.isClosed() ) cursor.close();
  }

  /** set the current survey
   * @param name     survey name
   * @param datamode survey data-mode
   * @return positive ID on success, 0 or -1 failure
   */
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
   
  /** @return the survey id given the survey name (-1L on error)
   * @param name     survey name
   */
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

  /** @return the survey name given the survey ID
   * @param sid     survey ID
   */
  String getSurveyFromId( long sid ) { return getNameFromId( SURVEY_TABLE, sid ); }

  /** @return the survey date given the survey ID 
   * @param sid     survey ID
   * @note the date is a string formatted yyyy.mm.dd
   */
  String getSurveyDate( long sid ) { return getSurveyFieldAsString( sid, "day" ); }

  /** @return the survey description given the survey ID
   * @param sid     survey ID
   */
  String getSurveyComment( long sid ) { return getSurveyFieldAsString( sid, "comment" ); }

  /** @return the survey team given the survey ID
   * @param sid     survey ID
   */
  String getSurveyTeam( long sid ) { return getSurveyFieldAsString( sid, "team" ); }

  /** @return the survey field given the survey ID
   * @param sid     survey ID
   * @param attr    field name, as in the database "surveys" table
   */
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
   // the following tables are serialized (besides the survey record)
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
     // TDLog.v( "dump DB to file " + filename + " survey ID " + sid );
     // String where = "surveyId=" + Long.toString(sid);
     if ( myDB == null ) {
       TDLog.e("dump DB to file: null DB ");
       return;
     }
     try {
       TDPath.checkPath( filename );
       FileWriter fw = TDFile.getFileWriter( filename ); // DistoX-SAF
       PrintWriter pw = new PrintWriter( fw );
       Cursor cursor = myDB.query( SURVEY_TABLE, 
                            new String[] { "name", "day", "team", "declination", "comment", "init_station", "xsections", "datamode", "extend", "calculated_azimuths" },
                            "id=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         // TDLog.v("dump SURVEY");
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, \"%s\", \"%s\", \"%s\", %.4f, \"%s\", \"%s\", %d, %d, %d, %d );\n",
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
                     (int)cursor.getLong(8),  // extend
                     (int)cursor.getLong(9)   // calculated_azimuths
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
         // TDLog.v("dump AUDIO");
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, %d, \"%s\", %d );\n",
                     AUDIO_TABLE,
                     sid,
                     cursor.getLong(0),   // id
                     cursor.getLong(1),   // itemid
                     TDString.escape( cursor.getString(2) ), // date
                     cursor.getLong(3)    // reftype
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
         // TDLog.v("dump PHOTO");
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, %d, %d, \"%s\", \"%s\", \"%s\", %d, \"%s\", %d );\n",
                     PHOTO_TABLE,
                     sid,
                     cursor.getLong(0),   // id
                     cursor.getLong(1),   // itemid
                     cursor.getLong(2),   // status
                     TDString.escape( cursor.getString(3) ), // title
                     TDString.escape( cursor.getString(4) ), // date
                     TDString.escape( cursor.getString(5) ), // comment
                     cursor.getLong(6),
                     TDString.escape( cursor.getString(7) ), // code
                     cursor.getLong(8)                       // reftype: reference item type
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.rawQuery( qTriMirrorStationAll, new String[] { Long.toString(sid) } );
       if (cursor.moveToFirst()) {
         // TDLog.v("dump PHOTO");
         do {
           pw.format(Locale.US,
               "INSERT INTO %s VALUES( %d, \"%s\" );\n",
               TRI_MIRRORED_STATIONS_TABLE,
               sid,
               TDString.escape( cursor.getString(0) )    // station
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( PLOT_TABLE, 
                            mPlotFieldsFull,
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         // TDLog.v("dump PLOT");
         do {
           pw.format(Locale.US,
             "INSERT into %s values( %d, %d, \"%s\", %d, %d, \"%s\", \"%s\", %.2f, %.2f, %.2f, %.2f, %.2f, \"%s\", \"%s\", %d, %d, %.2f, %.2f, %.2f, %.2f );\n",
             PLOT_TABLE,
             sid,
             cursor.getLong(0),    // plot id
             TDString.escape( cursor.getString(1) ),  // name
             cursor.getLong(2),    // plot_type
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
             cursor.getDouble(15), // intercept
             cursor.getDouble(16), // center_x
             cursor.getDouble(17), // center_y
             cursor.getDouble(18)  // center_z
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
                            new String[] { "id", "fStation", "tStation", "distance", "bearing", "clino", "roll", // 0 - 6
                                           "acceleration", "magnetic", "dip", // 7 - 9
                                           "extend", "flag", "leg", "status", "comment", "type", "millis", "color", "stretch", "address", // 10 - 19
                                           "rawMx", "rawMy", "rawMz", "rawGx", "rawGy", "rawGz", "idx", "time" }, // 20 - 27
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         // TDLog.v("dump SHOT cols " + cursor.getColumnCount() + " rows " + cursor.getCount() );
         do {
           pw.format(Locale.US,
            //           TABLE      SID ID  FROM    TO      dist  bear  clino roll  acc   mag   dip   ext flg leg sts comment typ mse col str.  addr    mx          gx          idx time
            "INSERT into %s values( %d, %d, \"%s\", \"%s\", %.3f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %d, %d, %d, %d, \"%s\", %d, %d, %d, %.2f, \"%s\", %d, %d, %d, %d, %d, %d, %d, %d );\n",
                     SHOT_TABLE,
                     sid,
                     cursor.getLong(0),
                     TDString.escape( cursor.getString(1) ),
                     TDString.escape( cursor.getString(2) ),
                     cursor.getDouble(3),
                     cursor.getDouble(4),
                     cursor.getDouble(5),
                     cursor.getDouble(6),   // roll
                     cursor.getDouble(7),
                     cursor.getDouble(8),
                     cursor.getDouble(9),   // dip
                     cursor.getLong(10),    // extend
                     cursor.getLong(11),    // flag
                     cursor.getLong(12),    // leg
                     cursor.getLong(13),    // status
                     TDString.escape( cursor.getString(14) ), // comment
                     cursor.getLong(15),    // shot_type
                     cursor.getLong(16),    // millis [s]
                     cursor.getLong(17),    // custom color 
		     cursor.getDouble(18),  // stretch
                     TDString.escape( cursor.getString(19) ),    // address
                     cursor.getLong(20),    // rawMx
                     cursor.getLong(21),
                     cursor.getLong(22),
                     cursor.getLong(23),    // rawGx
                     cursor.getLong(24),
                     cursor.getLong(25),
                     cursor.getLong(26),    // idx
                     cursor.getLong(27)     // time [s]
           );
           pw.flush();
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       // "id", "station", "longitude", "latitude", "altitude", "altimetric", "comment", "status", "source",
       // "cs_name", "cs_longitude", "cs_latitude", "cs_altitude", "cs_decimals", "convergence", "accuracy", "accuracy_v", "m_to_units", "m_to_vunits"
       cursor = myDB.query( FIXED_TABLE, 
                  mFixedFields,
                  "surveyId=?", new String[] { Long.toString( sid ) },
                  null, null, null );
       if (cursor.moveToFirst()) {
         // TDLog.v("dump FIXED");
         do { // values in the order of the fields of the table
           pw.format(Locale.US,
             "INSERT into %s values( %d, %d, \"%s\", %.7f, %.7f, %.2f, %.2f, \"%s\", %d, %d, \"%s\", %.7f, %.7f, %.1f, %d, %d, %.4f, %.1f, %.1f, %.6f, %.6f );\n",
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
             cursor.getLong(8),    // source_type: source is written twice
             cursor.getLong(13),   // cs decimals
             cursor.getDouble(14), // cs convergence
             cursor.getDouble(15), // accuracy
             cursor.getDouble(16), // accuracy_v
             cursor.getDouble(17), // meters to units
             cursor.getDouble(18)  // meters to v-units
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( STATION_TABLE, mStationFields,
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         // TDLog.v("dump STATION");
         do {
           // STATION_TABLE does not have field "id"
           pw.format(Locale.US,
             "INSERT into %s values( %d, 0, \"%s\", \"%s\", %d, \"%s\", \"%s\" );\n",
             STATION_TABLE,
             sid, 
             TDString.escape( cursor.getString(0) ),
             TDString.escape( cursor.getString(1) ),
             cursor.getLong(2),
             TDString.escape( cursor.getString(3) ),
             TDString.escape( cursor.getString(4) )
           );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       cursor = myDB.query( SENSOR_TABLE, 
                            new String[] { "id", "shotId", "status", "title", "date", "comment", "type", "value", "reftype" },
                            "surveyId=?", new String[] { Long.toString( sid ) },
                            null, null, null );
       if (cursor.moveToFirst()) {
         // TDLog.v("dump SENSOR");
         do {
           pw.format(Locale.US,
                     "INSERT into %s values( %d, %d, %d, %d, \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", %d );\n",
                     SENSOR_TABLE,
                     sid,
                     cursor.getLong(0),
                     cursor.getLong(1),
                     cursor.getLong(2),
                     TDString.escape( cursor.getString(3) ),
                     TDString.escape( cursor.getString(4) ),
                     TDString.escape( cursor.getString(5) ),
                     TDString.escape( cursor.getString(6) ),  // sensor_type
                     TDString.escape( cursor.getString(7) ),  // sensor_value
                     cursor.getLong(8)                        // refrenece item_type
                    );
         } while (cursor.moveToNext());
       }
       if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();

       // TDLog.v("dump file close");
       fw.flush();
       fw.close();
     } catch ( FileNotFoundException e ) {// DistoX-SAF
       TDLog.e("dump file not found " + e.getMessage() );
     } catch ( IOException e ) {// FIXME
       TDLog.e("dump i/o error " + e.getMessage() );
     }
     // TDLog.v("dump file done");
   }

   static boolean mColorReset = false; // whether custom colors have been reset on survey load

   /** @return true if some custom colors have been reset
    */
   static boolean hasResetColor() { return mColorReset; }

   /** load survey data from a sql file
    * @param filename    name of the sql file
    * @param db_version  current database version
    * @return survey ID on success, negative on failure
    */
   long loadFromFile( String filename, int db_version )
   {
     boolean success = false; // whether the load is successful
     long sid = -1;
     long id, status, itemid, reftype;
     String station, title, date, name, comment;
     String line;

     mColorReset = false;
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
         int calculated_azimuths = ( db_version > 54 )?
           (int)( scanline0.longValue( SurveyInfo.CALCULATED_AZIMUTHS_FALSE ) ) :
           SurveyInfo.CALCULATED_AZIMUTHS_FALSE;

         sid = setSurvey( name, datamode );

         try {
           myDB.beginTransaction();
           // success &= updateSurveyInfo( sid, date, team, decl, comment, init_station, xsections, false );
           ContentValues cv = makeSurveyInfoCcontentValues( date, team, decl, comment, init_station, xsections, calculated_azimuths );
           myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(sid) } );
           // TDLog.v( "DB updateSurveyInfo: " + success );

           cv = makeSurveyExtend( extend_ref );
           myDB.update( SURVEY_TABLE, cv, "id=?", new String[]{ Long.toString(sid) } );

           while ( (line = br.readLine()) != null ) {
             // TDLog.Log( TDLog.LOG_DB, "load from file: " + line );
             vals = line.split(" ", 4);
             table = vals[2];
             v = vals[3];
             Scanline scanline1 = new Scanline( v, v.indexOf('(')+1, v.lastIndexOf(')') );
             skip_sid = scanline1.longValue( -1 ); // skip survey ID
             id = table.equals(TRI_MIRRORED_STATIONS_TABLE )? -1 : scanline1.longValue( -1 );
             // TDLog.v("DB table " + table + " id " + id + " v " + v );

             if ( table.equals(AUDIO_TABLE) ) // ---------------- FIXME_AUDIO
             {
               itemid = scanline1.longValue( -1 );
               if ( itemid >= 0 && id >= 0 ) {
                 date    = TDString.unescape( scanline1.stringValue( ) );
                 reftype = (db_version > 53)? scanline1.longValue( 0 ) : 0 ;
                 cv = makeAudioContentValues( sid, id, itemid, date, reftype );
                 myDB.insert( AUDIO_TABLE, null, cv ); 
                 // TDLog.Log( TDLog.LOG_DB, "load from file photo " + sid + " " + id + " " + title + " " + name );
               }

             }
             else if ( table.equals(SENSOR_TABLE) ) // ------------ FIXME_SENSORS
             {
               itemid  = scanline1.longValue( -1 );
               if ( itemid >= 0 && id >= 0 ) {
                 status  = scanline1.longValue( 0 );
                 title   = TDString.unescape( scanline1.stringValue( ) );
                 date    = TDString.unescape( scanline1.stringValue( ) );
                 comment = TDString.unescape( scanline1.stringValue( ) );
                 String type_str = TDString.unescape( scanline1.stringValue( ) );
                 String value = TDString.unescape( scanline1.stringValue( ) );
                 reftype = (db_version > 53)? scanline1.longValue( 0 ) : 0; // reference item_type
                 cv = makeSensorContentValues( sid, id, itemid, status, title, date, comment, type_str, value, (int)reftype );
                 myDB.insert( SENSOR_TABLE, null, cv ); 
                 // TDLog.Log( TDLog.LOG_DB, "load from file photo " + sid + " " + id + " " + title + " " + name );
               }
             }
             else if ( table.equals(PHOTO_TABLE) ) // --------------- FIXME_PHOTO
             {
               itemid  = scanline1.longValue( -1 );
               if ( itemid >= 0 && id >= 0 ) {
                 title   = TDString.unescape( scanline1.stringValue( ) );
                 date    = TDString.unescape( scanline1.stringValue( ) );
                 comment = TDString.unescape( scanline1.stringValue( ) );
                 long camera = (db_version > 39)? scanline1.longValue( 0 ) : 0 ;
                 String code = (db_version > 52)? scanline1.stringValue( ) : "" ;
                 reftype   = (db_version > 53)? scanline1.longValue( 0 ) : 0 ;
                 cv = makePhotoContentValues( sid, id, itemid, TDStatus.NORMAL, title, date, comment, camera, code, reftype );
                 myDB.insert( PHOTO_TABLE, null, cv ); 
                 // TDLog.Log( TDLog.LOG_DB, "load from file photo " + sid + " " + id + " " + title + " " + name );
               }
             }
             else if ( table.equals(TRI_MIRRORED_STATIONS_TABLE) ) // --------------- TRI_MIRROR_STATIONS
             {
               name    = TDString.unescape( scanline1.stringValue( ) );
               cv = makeTriMirroredStationsContentValues( sid, name );
               myDB.insert( TRI_MIRRORED_STATIONS_TABLE, null, cv );
             }
             else if ( table.equals(PLOT_TABLE) ) // ---------- PLOTS
             {
               name         = TDString.unescape( scanline1.stringValue( ) );
               long plot_type = scanline1.longValue( -1 ); if ( db_version <= 20 ) if ( plot_type == 3 ) plot_type = 5;
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
               double center_x = (db_version > 44)? scanline1.doubleValue( 0.0 ) : 0;
               double center_y = (db_version > 44)? scanline1.doubleValue( 0.0 ) : 0;
               double center_z = (db_version > 44)? scanline1.doubleValue( 0.0 ) : 0;
               // if ( insertPlot( sid, id, name, plot_type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation, false ) < 0 ) { success = false; }
               cv = makePlotContentValues( sid, id, name, plot_type, status, start, view, xoffset, yoffset, zoom, azimuth, clino, hide, nick, orientation, maxscrap, intercept,
                                           center_x, center_y, center_z );
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
               long shot_type = ( db_version > 21 )? scanline1.longValue( 0 ) : 0; // 0: DistoX
	       long millis   = ( db_version > 31 )? scanline1.longValue( 0 ) : 0; // seconds
	       long color  = 0; if ( db_version > 33 ) {
                 color  = scanline1.longValue( 0 );
                 if ( color != 0 ) {
                   if ( ! TDSetting.mSplayColor ) { // FIXME SPLAY_COLOR
                     color = 0;
                     mColorReset = true;
                   }
                 }
               }
	       double stretch = 0; if ( db_version > 35 ) stretch = scanline1.doubleValue( 0.0 );
	       String addr = ""; if ( db_version > 37 ) addr = TDString.unescape( scanline1.stringValue( ) );
               int rawMx = 0;
               int rawMy = 0;
               int rawMz = 0;
               int rawGx = 0;
               int rawGy = 0;
               int rawGz = 0;
               int idx   = 0;
               int time  = 0; // [s]
               if ( db_version > 49 ) {
                 rawMx = (int)( scanline1.longValue( 0 ) ); 
                 rawMy = (int)( scanline1.longValue( 0 ) );
                 rawMz = (int)( scanline1.longValue( 0 ) );
                 rawGx = (int)( scanline1.longValue( 0 ) );
                 rawGy = (int)( scanline1.longValue( 0 ) );
                 rawGz = (int)( scanline1.longValue( 0 ) );
                 if ( db_version > 50 ) {
                   idx = (int)( scanline1.longValue( 0 ) );
                   if ( db_version > 51 ) {
                     time = (int)( scanline1.longValue( time ) );
                   }
                 }
               }
               

               // if ( doInsertShot( sid, id, millis, color, from, to, d, b, c, r, extend, stretch, flag, leg, status, shot_type, comment, addr, false ) >= 0 ) {
               //   success &= updateShotAMDR( id, sid, acc, mag, dip, r, false );
	       // } else {
	       //   success = false;
	       // }
               cv = makeShotContentValues( sid, id, millis, color, from, to, d, b, c, r, acc, mag, dip, extend, stretch, flag, leg, status, shot_type, comment, addr,
                                           rawMx, rawMy, rawMz, rawGx, rawGy, rawGz, idx, time );
               myDB.insert( SHOT_TABLE, null, cv ); 

               // TDLog.v( "DB insert shot " + from + "-" + to + ": " + success );
               // TDLog.Log( TDLog.LOG_DB, "insert shot " + sid + " " + id + " " + from + " " + to );
             }
	     else if ( table.equals(FIXED_TABLE) )
	     {
               station    = TDString.unescape( scanline1.stringValue( ) );
               double lng = scanline1.doubleValue( 0.0 );
               double lat = scanline1.doubleValue( 0.0 );
               double h_ell = scanline1.doubleValue( 0.0 );
               double h_geo = scanline1.doubleValue( 0.0 );
               comment    = TDString.unescape( scanline1.stringValue( ) );
               status     = scanline1.longValue( 0 );
               long source = scanline1.longValue( 0 ); // 0: unknown source
               double cs_lng = 0;
               double cs_lat = 0;
               double cs_h_geo = 0;
	       long cs_n_dec = 2;
               double conv = 0.0;
               double accur   = -1;
               double accur_v = -1;
               double m_to_units = 1;
               double m_to_vunits = 1;
               String cs = TDString.unescape( scanline1.stringValue( ) );
               if ( cs.length() > 0 ) {
                 cs_lng = scanline1.doubleValue( 0.0 );
                 cs_lat = scanline1.doubleValue( 0.0 );
                 cs_h_geo = scanline1.doubleValue( 0.0 );
	         long source_type = ( db_version > 27 )? scanline1.longValue( 0 ) : 0; // source_type
	         if ( db_version > 34 ) cs_n_dec = scanline1.longValue( 8 ); // nr. of decimals

	         if ( db_version > 46 ) conv = scanline1.doubleValue( 0.0 ); // convergence
                 TDLog.v("CS " + cs + " lng " + cs_lng + " lat " + cs_lat + " source_type " + source_type + " conv " + conv );
	         if ( db_version > 47 ) {
                   accur   = scanline1.doubleValue( -1 );
                   accur_v = scanline1.doubleValue( -1 );
                 }
	         if ( db_version > 48 ) {
                   m_to_units = scanline1.doubleValue( 1 );
                   m_to_vunits = scanline1.doubleValue( 1 );
                 }
               }
               // use id == -1L to force DB get a new id
               // if ( insertFixed( sid, -1L, station, lng, lat, h_ell, h_geo, comment, status, source, cs, cs_lng, cs_lat, cs_h_geo, cs_n_dec ) < 0 ) {
	       //   success = false;
	       // }
               cv = makeFixedContentValues( sid, -1L, station, lng, lat, h_ell, h_geo, comment, status, source,
		     cs, cs_lng, cs_lat, cs_h_geo, cs_n_dec, conv, accur, accur_v, m_to_units, m_to_vunits );
               myDB.insert( FIXED_TABLE, null, cv ); 
               // TDLog.Log( TDLog.LOG_DB, "load from file fixed " + sid + " " + id + " " + station  );
             } 
	     else if ( table.equals(STATION_TABLE) )
	     {
         // N.B. ONLY IF db_version > 19
         // TDLog.e( "v <" + v + ">" );
         // TDLog.Log( TDLog.LOG_DB, "load from file station " + sid + " " + name + " " + comment + " " + flag  );
         name    = TDString.unescape( scanline1.stringValue( ) );
         comment = TDString.unescape( scanline1.stringValue( ) );
         long flag = ( db_version > 25 )? scanline1.longValue( 0 ) : 0;
         String presentation = name;
         String code = "";
         if ( db_version > 45 ) presentation = TDString.unescape( scanline1.stringValue( ) );
         if ( db_version > 52 ) code = TDString.unescape( scanline1.stringValue( ) );
         // success &= insertStation( sid, name, comment, flag );
         cv = makeStationContentValues( sid, name, comment, flag, presentation, code );
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
       TDLog.e( e.getMessage() );
     } catch ( IOException e ) {
       TDLog.e( e.getMessage() );
     }
     // TDLog.v( "DB success: " + success + " SID " + sid );

     return (success ? sid : -sid );
   }

   // ----------------------------------------------------------------------
  private ContentValues makeStationContentValues( long sid, String name, String comment, long flag, String presentation, String code )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId",  sid );
    cv.put( "name",      name );
    cv.put( "comment",   comment );
    cv.put( "flag",      flag );
    cv.put( "presentation", presentation );
    cv.put( "code",      (code == null)? "" : code );
    return cv;
  }

  /**
   * Creates a ContentValues object for a tri_mirrored_stations table entry
   * @param sid survey ID
   * @param name station name
   * @return ContentValues object
   */
  private ContentValues makeTriMirroredStationsContentValues( long sid, String name )
  {
    ContentValues cv = new ContentValues();
    cv.put( "surveyId",  sid );
    cv.put( "name",      name );
    return cv;
  }

  /** insert/update a station
   * @param sid          survey ID
   * @param name         station name (ID)
   * @param comment      station comment
   * @param flag         station flag
   * @param presentation station presentation string
   */
  public boolean insertStation( long sid, String name, String comment, long flag, String presentation, String code )
  {
    if ( myDB == null ) return false;
    boolean ret = false;
    if ( comment == null ) comment = TDString.EMPTY;
    Cursor cursor = myDB.query( STATION_TABLE, mStationFields,
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
      cv.put("presentation", ( presentation == null )? name : presentation );
      cv.put("code", (code == null)? "" : code );
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
      ContentValues cv = makeStationContentValues( sid, name, comment, flag, ((presentation == null)? name : presentation), ((code == null)? "" : code) );
      ret = doInsert( STATION_TABLE, cv, "station insert" );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  /** update the station geocode
   * @param sid      survey ID
   * @param name     station name (ID)
   * @param geocode  new station geocode
   */
  void updateStationGeocode( long sid, String name, String geocode )
  {
    if ( TDString.isNullOrEmpty( name ) ) return;
    if ( myDB == null ) return;
    ContentValues cv = new ContentValues();
    // cv.put("comment", comment );
    // cv.put("flag", flag );
    // cv.put("presentation", ( presentation == null )? name : presentation );
    cv.put("code", (geocode == null)? "" : geocode );
    doUpdate( STATION_TABLE, cv, "surveyId=? AND name=?", new String[]{ Long.toString(sid), name }, "station geocode" );
  }
   
  /** @return a station
   * @param sid          survey ID
   * @param name         station name (ID)
   * @param presentation station presentation string: if non-null and the station does not exist it is created
   */
  StationInfo getStation( long sid, String name, String presentation )
  {
    if ( TDString.isNullOrEmpty( name ) ) return null;
    StationInfo cs = null;
    if ( myDB != null ) {
      Cursor cursor = myDB.query( STATION_TABLE, mStationFields,
          "surveyId=? and name=?", new String[]{ Long.toString( sid ), name },
          null, null, null );
      if (cursor.moveToFirst()) {
        cs = new StationInfo( cursor.getString( 0 ), cursor.getString( 1 ), cursor.getLong( 2 ), cursor.getString(3), cursor.getString(4) );
      }
      if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    }
    if ( cs == null && presentation != null ) {
      ContentValues cv = makeStationContentValues( sid, name, TDString.EMPTY, 0, presentation, "" );
      if ( doInsert( STATION_TABLE, cv, "station insert" ) ) {
        cs = new StationInfo( name, TDString.EMPTY, 0, presentation, "" );
      }
    }
    return cs;
  }


  /** @return the set of stations of a survey
   * @param sid   survey ID
   */
  ArrayList< StationInfo > getStations( long sid )
  {
    ArrayList< StationInfo > ret = new ArrayList<>();
    if ( myDB == null ) return ret;
    Cursor cursor = myDB.query( STATION_TABLE, mStationFields, 
                           "surveyId=?", new String[] { Long.toString( sid ) },
                           null, null, null );
    if (cursor.moveToFirst()) {
      do {
        ret.add( new StationInfo( cursor.getString(0), cursor.getString(1), cursor.getLong(2), cursor.getString(3), cursor.getString(4) ) );
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  /** delete a station
   * @param sid   survey ID
   * @param name  station name (ID)
   */
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
  // static private String[] mShotFullFields =
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
      info.mCalculatedAzimuths = (int)cursor.getLong (10 );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return info;
  }

  /**
   * Returns the "calculated_azimuths" status of the survey.
   * @param sid survey ID
   * @return "calculated_azimuths" status of the survey
   */
  public int getSurveyCalculatedAzimuths ( long sid )
  {
    int ret = 0;
    if ( myDB == null ) return 0;
    Cursor cursor = myDB.query( SURVEY_TABLE,
        new String[] { "calculated_azimuths" },
        WHERE_ID,
        new String[] { Long.toString(sid) },
        null,
        null,
        null
    ); // order by
    if (cursor.moveToFirst()) {
      ret = cursor.getInt( 0 );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
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
      // TDLog.v( "DB updated symbols: " + pt + " points, " + ln + " lines, " + ar + " areas");
    } catch ( SQLException e ) { TDLog.e( "updateSymbolKeys exception: " + e.getMessage() );
    } finally {
      db.endTransaction();
    }
  }

  // ----------------------------------------------------------------------
  // DATABASE TABLES

  /** delete a record from a table
   * @param sid   survey ID
   * @param id    record ID
   * @param table table name
   */
  private void deleteFromTable( long sid, long id, String table )
  {
    if ( myDB == null ) return;
    try {
      myDB.beginTransaction();
      myDB.execSQL( "DELETE FROM " + table + " WHERE surveyId=" + sid + " AND id=" + id );
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e) { logError("delete from table", e);
    } finally { myDB.endTransaction(); }
  }

  /** @return the maximum ID in a table for a given survey
   * @param table   table
   * @param sid     survey ID
   */
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

  // /** @return the minimum ID in a table for a given survey
  //  * @param table   table
  //  * @param sid     survey ID
  //  * @note used only for AUDIO table to get negative indices
  //  */
  // private long minId( String table, long sid )
  // {
  //   if ( myDB == null ) return -2L;
  //   long id = -1L;
  //   Cursor cursor = myDB.query( table, new String[] { "min(id)" },
  //                        "surveyId=?", 
  //                        new String[] { Long.toString(sid) },
  //                        null, null, null );
  //   if (cursor != null ) {
  //     if (cursor.moveToFirst() ) {
  //       if ( cursor.getLong(0) < id ) id = cursor.getLong(0);
  //     }
  //     if (!cursor.isClosed()) cursor.close();
  //   }
  //   return id - 1L; // decrement
  // }

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
       // TDLog.v( "BD open helper - create tables");
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
            +   " init_station TEXT, "  // initial station
            +   " xsections INTEGER, "  // whether xsections are shared or private
            +   " datamode INTEGER, "   // datamode: normal or diving
            +   " extend INTEGER "      // ???
            +   ")"
          );

          db.execSQL(
              create_table + SHOT_TABLE 
            + " ( surveyId INTEGER, "
            +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
            +   " fStation TEXT, "   // stations are indexed by the name
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
            +   " millis INTEGER, "   // timestamp [s]
            +   " color INTEGER, "     // custom color
            +   " stretch REAL default 0, " // extend stretch, default ExtendType.STRETCH_NONE
            +   " address TEXT default \"\", " // distox address
            +   " rawMx INTEGER default 0, "
            +   " rawMy INTEGER default 0, "
            +   " rawMz INTEGER default 0, "
            +   " rawGx INTEGER default 0, "
            +   " rawGy INTEGER default 0, "
            +   " rawGz INTEGER default 0, "
            +   " idx INTEGER default 0, "
            +   " time INTEGER default 0 " // device time [s]
            // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
            // +   " ON DELETE CASCADE "
            +   ")"
          );

          db.execSQL(
              create_table + FIXED_TABLE
            + " ( surveyId INTEGER, "
            +   " id INTEGER, "   //  PRIMARY KEY AUTOINCREMENT, "
            +   " station TEXT, "     // fix (GPS) point
            +   " longitude REAL, "
            +   " latitude REAL, "
            +   " altitude REAL, "    // WGS84 ellipsoid H
            +   " altimetric REAL, "  // geoid H (if any)
            +   " comment TEXT, "
            +   " status INTEGER, "   // NORMAL DELETED
            +   " cs_name TEXT, "
            +   " cs_longitude REAL, "
            +   " cs_latitude REAL, "
            +   " cs_altitude REAL, "
            +   " source INTEGER, "    // 0: unknown,  1: topodroid,  2: manual,   3: mobile-topographer
            +   " cs_decimals INTEGER, "
            +   " convergence REAL default 0, "  // meridian convergence [degree]
            +   " accuracy REAL default -1, "    // accuracy: -1 unset
            +   " accuracy_v REAL default -1, "  // vertical accuracy
            +   " m_to_units REAL default 1, "    // meters to units
            +   " m_to_vunits REAL default 1 "    // meters to units
            // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
            // +   " ON DELETE CASCADE "
            +   ")"
          );

          db.execSQL(
              create_table + STATION_TABLE 
            + " ( surveyId INTEGER, " 
            +   " name TEXT, "          // PRIMARY KEY
            +   " comment TEXT, "
            +   " flag INTEGER default 0, "
            +   " presentation TEXT default NIL, "
            +   " code TEXT default NIL "      // geo-morphology code(s)
            +   ")"
          );
           

          db.execSQL(
              create_table + PLOT_TABLE 
            + " ( surveyId INTEGER, "
            +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
            +   " name TEXT, "
            +   " type INTEGER, "
            +   " status INTEGER, " // NORMAL DELETED
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
            +   " intercept REAL default -1, "
            +   " center_x REAL default 0, "
            +   " center_y REAL default 0, "
            +   " center_z REAL default 0 "
            // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
            // +   " ON DELETE CASCADE "
            +   ")"
          );

          // db.execSQL(
          //     create_table + SKETCH_TABLE
          //   + " ( surveyId INTEGER, "
          //   +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
          //   +   " name TEXT, "
          //   +   " status INTEGER, "
          //   +   " start TEXT, "
          //   +   " st1 TEXT, "
          //   +   " st2 TEXT, "
          //   +   " xoffsettop REAL, "
          //   +   " yoffsettop REAL, "
          //   +   " zoomtop REAL, "
          //   +   " xoffsetside REAL, "
          //   +   " yoffsetside REAL, "
          //   +   " zoomside REAL, "
          //   +   " xoffset3d REAL, "
          //   +   " yoffset3d REAL, "
          //   +   " zoom3d REAL, "
          //   +   " east REAL, "
          //   +   " south REAL, "
          //   +   " vert REAL, "
          //   +   " azimuth REAL, "
          //   +   " clino REAL "
          //   // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
          //   // +   " ON DELETE CASCADE "
          //   +   ")"
          // );

          db.execSQL(
              create_table + PHOTO_TABLE
            + " ( surveyId INTEGER, "
            +   " id INTEGER, " //  PRIMARY KEY AUTOINCREMENT, "
            +   " shotId INTEGER, " // reference ID, either shots or plots
            +   " status INTEGER default 0, "
            +   " title TEXT, "
            +   " date TEXT, "
            +   " comment TEXT, "
            +   " camera INTEGER default 0, "  // source_type
            +   " code TEXT default NIL, "     // geo-morphology code(s)
            +   " reftype INTEGER default 0 "     // reference item_type: 0 undefined, 1 shots, 2 plots
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
            +   " type TEXT, " // sensor_type
            +   " value TEXT, "
            +   " reftype INTEGER default 0 "
            // +   " surveyId REFERENCES " + SURVEY_TABLE + "(id)"
            // +   " ON DELETE CASCADE "
            +   ")"
          );

          db.execSQL( 
              create_table + AUDIO_TABLE
            + " ( surveyId INTEGER, "
            +   " id INTEGER, " // PRIMARY KEY AUTOINCREMENT, "
            +   " shotId INTEGER, " // shot ID or plot ID
            +   " date TEXT, "
            +   " reftype INTEGER default 0 " //  reference item_type: 0 undefined, 1 shots, 2 plots
            +   ")"
          );

          // db.execSQL(
          //     " CREATE TRIGGER fk_insert_shot BEFORE "
          //   + " INSERT on " + SHOT_TABLE 
          //   + " FOR EACH ROW BEGIN "
          //   +   " SELECT RAISE "
          //   +   " (ROLLBACK, 'insert on \"" + SHOT_TABLE + "\" violates foreign key constraint')"
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
        } catch ( SQLException e ) { TDLog.e( "createTables exception: " + e.getMessage() );
        // } finally {
          // db.setLockingEnabled( true );
        }
     }

     static boolean columnExists( SQLiteDatabase db, String tableName, String columnName) {
      Cursor cursor = null;
      try {
        // Query the table_info pragma for the specified table
        cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);

        // Iterate through the result set to check for the column name
        if (cursor != null) {
          int nameIndex = cursor.getColumnIndex("name");
          while (cursor.moveToNext()) {
            String currentColumn = cursor.getString(nameIndex);
            if (currentColumn.equalsIgnoreCase(columnName)) {
              return true;
            }
          }
        }
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
      return false;
    }


    static void updateTables( SQLiteDatabase db, int oldVersion, int newVersion)
     {
        // FIXME this is called at each start when the database file exists
        // TDLog.Log( TDLog.LOG_DB, "DB open helper - upgrade old " + oldVersion + " new " + newVersion );
        // TDLog.v( "DB open helper - upgrade old " + oldVersion + " new " + newVersion );
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
             db.execSQL( "ALTER TABLE plots ADD COLUMN center_x REAL default 0" ); // east
             db.execSQL( "ALTER TABLE plots ADD COLUMN center_y REAL default 0" ); // south
             db.execSQL( "ALTER TABLE plots ADD COLUMN center_z REAL default 0" ); // down
	   case 45:
             db.execSQL( "ALTER TABLE stations ADD COLUMN presentation TEXT default NIL" ); 
	   case 46:
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN convergence REAL default 0" ); 
	   case 47:
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN accuracy REAL default -1" ); 
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN accuracy_v REAL default -1" ); 
	   case 48:
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN m_to_units REAL default 1" ); 
             db.execSQL( "ALTER TABLE fixeds ADD COLUMN m_to_vunits REAL default 1" ); 
	   case 49:
             db.execSQL( "ALTER TABLE shots ADD COLUMN rawMx INTEGER default 0 " );
             db.execSQL( "ALTER TABLE shots ADD COLUMN rawMy INTEGER default 0 " );
             db.execSQL( "ALTER TABLE shots ADD COLUMN rawMz INTEGER default 0 " );
             db.execSQL( "ALTER TABLE shots ADD COLUMN rawGx INTEGER default 0 " );
             db.execSQL( "ALTER TABLE shots ADD COLUMN rawGy INTEGER default 0 " );
             db.execSQL( "ALTER TABLE shots ADD COLUMN rawGz INTEGER default 0 " );
           case 50:
             db.execSQL( "ALTER TABLE shots ADD COLUMN idx INTEGER default 0 " );
           case 51:
             db.execSQL( "ALTER TABLE shots ADD COLUMN time INTEGER default 0 " );
           case 52:
             db.execSQL( "ALTER TABLE photos ADD COLUMN code TEXT default NIL" );
             db.execSQL( "ALTER TABLE stations ADD COLUMN code TEXT default NIL" );
           case 53:
             db.execSQL( "ALTER TABLE photos ADD COLUMN reftype INTEGER default 0" );
             db.execSQL( "ALTER TABLE audios ADD COLUMN reftype INTEGER default 0" );
             db.execSQL( "ALTER TABLE sensors ADD COLUMN reftype INTEGER default 0" );
           case 54:
             if ( !columnExists( db, "photos", "reftype" ) ) {
               db.execSQL( "ALTER TABLE photos ADD COLUMN reftype INTEGER default 0" );
             }
             if ( !columnExists( db, "audios", "reftype" ) ) {
               db.execSQL( "ALTER TABLE audios ADD COLUMN reftype INTEGER default 0" );
             }
             if ( !columnExists( db, "sensors", "reftype" ) ) {
               db.execSQL( "ALTER TABLE sensors ADD COLUMN reftype INTEGER default 0" );
             }
           case 55:
             db.execSQL( "ALTER TABLE surveys ADD COLUMN calculated_azimuths INTEGER default 0" );
             db.execSQL(
                 create_table + TRI_MIRRORED_STATIONS_TABLE
                     + " ( surveyId INTEGER, "
                     +   " name TEXT " // 'surveyId' and 'name' together are the PRIMARY KEY
                     + ")"
             );
           case 56:
             // TDLog.v( "current version " + oldVersion );
           default:
             break;
         }
      }
   }
}
