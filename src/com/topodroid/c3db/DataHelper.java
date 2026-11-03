/** @file DataHelper.java
 *
 * @author marco corvi
 * @date nov 2019
 *
 * @brief Interface to TopoDroid survey database
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3db;

import com.topodroid.util.TDLog;
import com.topodroid.TDX.PhotoInfo;
import com.topodroid.c3in.GlPhotoInfo;

// import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.IOException;
// import java.io.FileReader;
// import java.io.BufferedReader;
// import java.io.FileWriter;
// import java.io.PrintWriter;
// import java.io.StringWriter;

import android.content.Context;
// import android.content.ContentValues;
import android.database.Cursor;
// import android.database.SQLException;
import android.database.DataSetObservable;
// import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
// import android.database.sqlite.SQLiteOpenHelper;
// import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteException;
// import android.database.sqlite.SQLiteDiskIOException;

// import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
// import java.util.Locale;

// @SuppressWarnings("SyntaxError")
public class DataHelper extends DataSetObservable
{
  private static final String SURVEY_TABLE = "surveys";
  private static final String SHOT_TABLE   = "shots";

  private final static String WHERE_NAME        = "name=?";
  private final static String WHERE_SID         = "surveyId=?";
  private final static String WHERE_SID_STATUS  = "surveyId=? AND status=?";

  private final static String WHERE_SID_ID      = com.topodroid.TDX.DataHelper.WHERE_SID_ID;

  private SQLiteDatabase myDB = null;

  static private final String[] mReducedShotFields =
    { "id", "millis", "fStation", "tStation", "distance", "bearing", "clino", "flag"};
  // static private String[] mFullShotFields =
  //   { "id", "fStation", "tStation", "distance", "bearing", "clino", "acceleration", "magnetic", "dip", // 0 ..  8
  //     "extend", "flag", "leg", "comment", "type", "millis", "color", "stretch", "address"              // 9 .. 17
  //   };

  public DataHelper( Context context, String db_name, int db_version )
  {
    // TDLog.v( "data helper cstr - database " + db_name + " version " + db_version );
    openDatabase( context, db_name, db_version );
  }

  void closeDatabase()
  {
    if ( myDB == null ) return;
    myDB.close();
    myDB = null;
  }

  private void openDatabase( Context context, String database_name, int db_version )
  {
    // String database_name = TDPath.getDatabase();
    // TDLog.v( "open database ");
    try {
        // DistoXOpenHelper openHelper = new DistoXOpenHelper( context, database_name, db_version );
        // myDB = openHelper.getWritableDatabase();
        // myDB = openHelper.getReadableDatabase();
        myDB = SQLiteDatabase.openDatabase( database_name, null, SQLiteDatabase.OPEN_READONLY );
        if ( myDB == null ) {
          TDLog.e("DB failed get readable database" );
          // return;
        } 
        // TDLog.v("DB opened database");

        // while ( myDB.isDbLockedByOtherThreads() ) {
        //   TDUtil.slowDown( 200 );
        // }

        // updateConfig = myDB.compileStatement( "UPDATE configs SET value=? WHERE key=?" );

     } catch ( SQLiteException e ) {
       myDB = null;
       TDLog.e( "DB Data Helper cstr failed to get DB " + e.getMessage() );
     }
   }

  // /** print an error log
  //  * @param msg  header
  //  * @param e    exception
  //  */
  // private void logError( String msg, Exception e )
  // {
  //   TDLog.e( "DB " + msg + ": " + e.getMessage() );
  // }

  // /** handle a disk error
  //   * @param e  exception
  //  */
  // private void handleDiskIOError( SQLiteDiskIOException e )
  // {
  //   TDLog.e( "DB disk error " + e.getMessage() );
  // }

  // long getSurveyIdFromName( String name ) 
  // {
  //   long id = -1;
  //   if ( myDB == null ) { return -2; }
  //   Cursor cursor = myDB.query( SURVEY_TABLE, new String[] { "id" },
  //                               "name = ?", new String[] { name },
  //                               null, null, null );
  //   if (cursor != null ) {
  //     if (cursor.moveToFirst() ) {
  //       id = cursor.getLong(0);
  //     }
  //     if ( ! cursor.isClosed()) cursor.close();
  //   }
  //   return id;
  // }

  // ----------------------------------------------------------------
  // SELECT STATEMENTS - SHOT

  public List<DBlock> getSurveyShots( long sid, long status )
  {
    List< DBlock > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.query(SHOT_TABLE, mReducedShotFields, // { "id", "millis", "fStation", "tStation", "distance", "bearing", "clino", "flag"};
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
    blk.mId = cursor.getLong(0);
    blk.mSurveyId = sid;
    blk.mMillis  = cursor.getLong( 1 );
    blk.mFrom = cursor.getString(2);
    blk.mTo   = cursor.getString(3);
    // blk.backshot = (leg == LegType.BACK) );  // from - to
    blk.mLength       = cursor.getDouble(4);  // length [meters]
    blk.mBearing      = cursor.getDouble(5);  // bearing [degrees]
    blk.mClino        = cursor.getDouble(6);  // clino [degrees]
    blk.mFlag         = (int)( cursor.getLong(7) ); 
  }
  
  // ----------------------------------------------------------------------
  // SURVEY

  public SurveyInfo getSurveyInfo( String name )
  {
    // TDLog.v("DB get survey " + name );
    if ( myDB == null ) return null;
    SurveyInfo info = null;
    Cursor cursor = myDB.query( SURVEY_TABLE,
                               new String[] { "id", "name", "day", "team", "declination" }, // columns
                               WHERE_NAME, new String[] { name },
                               null, null, null );
    if (cursor.moveToFirst()) {
      info = new SurveyInfo();
      info.id      = cursor.getLong( 0 );
      info.name    = cursor.getString( 1 );
      info.date    = cursor.getString( 2 );
      info.team    = cursor.getString( 3 );
      info.declination = cursor.getDouble( 4 );
      // TDLog.v("DB got survey " + name );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return info;
  }

  /** @return the fixed points of the survey - only those with status "0" (non deleted)
   * @param sid  survey ID
   */
  public List<SurveyFixed> getSurveyFixeds( long sid )
  {
    // TDLog.v("DB get fixed stations" );
    if ( myDB == null ) return null;
    List<SurveyFixed> ret = new ArrayList<>();
    Cursor cursor = myDB.query( "fixeds", // FIXED_TABLE,
                               new String[] { "station", "longitude", "latitude", "altitude", "altimetric", "cs_name", "cs_longitude", "cs_latitude", "cs_altitude", "convergence", "m_to_units", "m_to_vunits" }, // columns
                               WHERE_SID_STATUS, new String[] { Long.toString(sid), "0" },
                               null, null, null );
    if (cursor.moveToFirst()) {
      do {
        SurveyFixed fixed = new SurveyFixed( cursor.getString(0) );
        fixed.mLongitude  = cursor.getDouble(1); // longitude
        fixed.mLatitude   = cursor.getDouble(2);
        fixed.mEllipAlt   = cursor.getDouble(3);
        fixed.mGeoidAlt   = cursor.getDouble(4);
        fixed.mCsName     = cursor.getString(5);
        fixed.mCsLongitude = cursor.getDouble(6);
        fixed.mCsLatitude  = cursor.getDouble(7);
        fixed.mCsGeoidAlt  = cursor.getDouble(8);
        fixed.mConvergence = cursor.getDouble(9);
        fixed.mToUnits     = cursor.getDouble(10);
        fixed.mToVUnits    = cursor.getDouble(11);
        ret.add( fixed );
      } while (cursor.moveToNext());
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  // ----------------------------------------------------------------------

  // @SuppressWarnings("SyntaxError")
  // private static class DistoXOpenHelper extends SQLiteOpenHelper
  // {
  //   // private static final String create_table = "CREATE TABLE IF NOT EXISTS ";

  //   DistoXOpenHelper(Context context, String database_name, int db_version ) 
  //   {
  //      super(context, database_name, null, db_version ); 
  //      // TDLog.v( "DB name " + database_name );
  //   }

  //   @Override
  //   public void onCreate(SQLiteDatabase db) 
  //   {
  //   }

  //   @Override
  //   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  //   {  
  //   }
  // }


  /** @return the photos at shots of a survey - SAME AS com.topodroid.TDX.DataHelper.selectAllPhotosShot
   * @param sid      survey ID
   * @param status   shot status [20241018 was photo status]
   * @note used also by the ShotWindow
   */
  public List< PhotoInfo > selectAllShotPhotos( long sid, long status )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.rawQuery( com.topodroid.TDX.DataHelper.qjPhotosShot, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(status) } );
    if (cursor.moveToFirst()) {
      do {
        String name = cursor.getString(3) + "-" + cursor.getString(4);
        list.add( new PhotoInfo( sid, 
                                 cursor.getLong(0),        // id
                                 cursor.getLong(1),        // shot id
                                 cursor.getString(2),
                                 name,                     // shot name
                                 cursor.getString(5),      // date
                                 cursor.getString(6),      // comment
                                 (int)(cursor.getLong(7)), // camera
                                 cursor.getString(8),      // code
                                 (int)(cursor.getLong(9)), // reftype
                                 (int)(cursor.getLong(10)) // format
                 ) );
      } while (cursor.moveToNext());
    }
    // TDLog.v(  "select All Photos Shot list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  public static final String qjPhotosPlot  =
    "select p.id, q.id, p.title, q.name, p.date, p.comment, p.camera, p.code, p.reftype, p.format from photos as p join plots as q on p.shotId=q.id where p.surveyId=? and q.surveyId=? and q.status=?";

  /** @return the photos at plots of a survey - SAME AS in com.topodroid.TDX.DataHelper.selectAllPhotosPlot
   * @param sid      survey ID
   * @param status   plot status [20241018 was photo status]
   */
  public List< PhotoInfo > selectAllPlotPhotos( long sid, long status )
  {
    List< PhotoInfo > list = new ArrayList<>();
    if ( myDB == null ) return list;
    Cursor cursor = myDB.rawQuery( /* com.topodroid.TDX.DataHelper. */ qjPhotosPlot, new String[] { Long.toString(sid), Long.toString(sid), Long.toString(status) } );
    if (cursor.moveToFirst()) {
      do {
        list.add( new PhotoInfo( sid, 
                                 cursor.getLong(0),        // id
                                 cursor.getLong(1),        // plot id
                                 cursor.getString(2),      // photo title
                                 cursor.getString(3),      // plot name
                                 cursor.getString(4),      // date
                                 cursor.getString(5),      // comment
                                 (int)(cursor.getLong(6)), // camera
                                 cursor.getString(7),      // code
                                 (int)(cursor.getLong(8)), // reftype
                                 (int)(cursor.getLong(9))  // format
                 ) );
      } while (cursor.moveToNext());
    }
    // TDLog.v( "select All Photos Plot list size " + list.size() );
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return list;
  }

  // SAME AS com.topodroid.TDX.DataHelper.selectPlotIntercept()
  public float selectPlotIntercept( long pid, long sid )
  {
    if ( myDB == null ) return -1;
    float ret = -1;
    Cursor cursor = myDB.query( com.topodroid.TDX.DataHelper.PLOT_TABLE,
       		         new String[] { "intercept" }, // columns
                                WHERE_SID_ID, new String[] { Long.toString(sid), Long.toString(pid) }, null, null, null );
    if ( cursor.moveToFirst() ) {
      ret = (float)cursor.getDouble(0);
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  /** select shot stations given the shot ID
   * @param id   plot ID
   * @param sid  survey ID
   * @return the shot
   */
  public void selectPhotoStations( long sid, long id, GlPhotoInfo photo_info )
  {
    TDLog.v( "select photo stations for shot" + id + "/" + sid );
    if ( myDB == null ) return;
    Cursor cursor = myDB.query( "plots", new String[] { "start", "view" },
                                WHERE_SID_ID, new String[] { Long.toString(sid), Long.toString(id) }, null, null, null );
    if (cursor.moveToFirst()) {
      photo_info.from = cursor.getString( 0 );
      photo_info.to   = cursor.getString( 1 );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
  }

}

