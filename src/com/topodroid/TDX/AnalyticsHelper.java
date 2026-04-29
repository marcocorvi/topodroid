/* @file AnalyticsHelper.java
 *
 * @author marco corvi
 * @date apr 2026
 *
 * @brief TopoDroid SQLite "analytics" database manager
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDFile;
// import com.topodroid.util.TDString;
import com.topodroid.util.TDVersion;
import com.topodroid.util.TDAnalytics;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.DataSetObservable;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDiskIOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
// import java.util.Locale;
// import java.util.HashMap;

public class AnalyticsHelper extends DataSetObservable
{
  static final String ERROR_NULL_DB = "null analytics DB ";

  // private static final String CONFIG_TABLE = "configs";
  private static final String ANALYTIC_TABLE = "analytics";

  private static final String WHERE_ID = "id=?";
  private static final String WHERE_NAME = "name=?";

  private SQLiteDatabase myDB = null;

  // private SQLiteStatement updateConfig;
  // private SQLiteStatement updateAnalytic;

  // ----------------------------------------------------------------------
  // DATABASE

  private final Context mContext;

  /** cstr
   * @param context context
   */
  AnalyticsHelper( Context context )
  {
    mContext = context;
    openAnalyticDatabase();
  }

  /** @return the SQLite database
   */
  SQLiteDatabase getDb() { return myDB; }

  /** close the database
   */
  void closeDatabase()
  {
    if ( myDB == null ) return;
    myDB.close();
    myDB = null;
  }

  /** open the "analytics.sqlite" database
   */
  private void openAnalyticDatabase()
  {
    String database_name = TDFile.getAnalyticDatabase().getAbsolutePath();
    // TDLog.v( "Device DB <" + database_name + ">");
    AnalyticOpenHelper openHelper = new AnalyticOpenHelper( mContext, database_name );
    try {
      myDB = openHelper.getWritableDatabase();
      if ( myDB == null ) {
        TDLog.e( "failed get writable database " + database_name );
        return;
      }
      // updateConfig   = myDB.compileStatement( "UPDATE configs SET value=? WHERE key=?" );
    } catch ( SQLiteException e ) {
      myDB = null;
      TDLog.e( "Failed to get analytics DB " + e.getMessage() );
    }
  }
  
  /** log an error message
   * @param msg    message
   * @param e      error
   */
  private void logError( String msg, SQLiteException e )
  {
    TDLog.e("DB " + msg + ": " + e.getMessage() );
  }

  /** handle disk I/O error
   * @param e  disk error
   */
  private void handleDiskIOError( SQLiteDiskIOException e )
  {
    TDLog.e("DB disk error " + e.getMessage() );
  }

  /** update a table
   * @param table   table to update
   * @param cv      values (key-value pairs) to update
   * @param where   where clause
   * @param args    args to the where cause
   * @param msg     log message
   */
  private boolean doUpdate( String table, ContentValues cv, String where, String[] args, String msg )
  {
    if ( myDB == null ) {
      TDLog.e( ERROR_NULL_DB + "do update");
      return false; 
    }
    boolean ret = false;
    try {
      myDB.beginTransaction();
      myDB.update( table, cv, where, args );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 );
    // } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  /** insert into a table
   * @param table   table to update
   * @param cv      values (key-value pairs) to update
   * @param msg     log message
   */
  private boolean doInsert( String table, ContentValues cv, String msg )
  {
    if ( myDB == null ) {
      TDLog.e( ERROR_NULL_DB + "do insert");
      return false; 
    }
    boolean ret = false;
    try {
      myDB.beginTransaction();
      myDB.insert( table, null, cv );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError(msg, e1 );
    // } catch ( IllegalStateException e2 ) { logError(msg, e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }


  // ----------------------------------------------------------------------
  // SELECT STATEMENTS

  /** @return the analytics
   */
  String getAnalytics( )
  {
    TDLog.v("get analytics" );
    if ( myDB == null ) {
      TDLog.e( ERROR_NULL_DB + "select GM");
      return null;
    }
    StringBuilder sb = new StringBuilder();
    Cursor cursor = null;
    try { 
      cursor = myDB.query( ANALYTIC_TABLE, new String[] { "name", "usage" }, "usage > 0", new String[] { }, null, null, "name" ); // groupBy having orderBy
      if ( cursor != null && cursor.moveToFirst()) {
        boolean first = true;
        sb.append("{\"android\": \"").append( TDandroid.getAndroidModel() ).append("\", ")
          .append("\"TD\": \"").append( TDVersion.string() ).append("\", ")
          .append("\"country\": \"").append( (TDAnalytics.mCT == null)? "--" : TDAnalytics.mCT ).append("\", ");
        do {
          if ( ! first ) sb.append(", ");
          sb.append("\"").append( cursor.getString(0) ).append("\": \"").append( cursor.getLong(1) ).append("\"");
          first = false;
        } while ( cursor.moveToNext() );
        sb.append("}");
      }
    
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    TDLog.v("Analytics " + sb.toString() );
    return sb.toString();
  }

  /** update a function usage (increment by 1)
   * @param name function name (can be null)
   *  "UPDATE analytics SET usage=usage + 1 WHERE name=?"
   *  "INSERT INTO analytics VALUES( ?, 1 )"
   */
  void updateAnalytic( String name ) 
  {
    if ( name == null || myDB == null ) return;
    long usage = 0;
    Cursor cursor = null;
    try {
      cursor = myDB.query( ANALYTIC_TABLE, new String[] { "usage" }, WHERE_NAME, new String[] { name }, null, null, null );
      if ( cursor != null && cursor.moveToFirst()) {
        usage = cursor.getLong(0);
        usage ++;
        ContentValues cv = new ContentValues();
        cv.put( "usage", usage );
        doUpdate( ANALYTIC_TABLE, cv, WHERE_NAME, new String[] { name }, "update" );
        TDLog.v("update analytic " + name + " usage " + usage );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }      
    if ( usage == 0 ) { // INSERT
      usage ++;
      ContentValues cv = new ContentValues();
      cv.put( "name", name );
      cv.put( "usage", usage );
      doInsert( ANALYTIC_TABLE, cv, "insert" );
      TDLog.v("insert analytic " + name + " " + usage );
    }
  }

  /** reset analytic usage to 0
   *    "UPDATE analytics SET usage=0"
   */
  void resetAnalytics()
  {
    TDLog.v("reset analytics");
    ContentValues cv = new ContentValues();
    cv.put( "usage", 0 );
    doUpdate( ANALYTIC_TABLE, cv, null, null, "reset" );
  } 


  // ----------------------------------------------------------------------
  // CONFIG DATA

  // /** @return a configuration value
  //  * @param key  value key
  //  */
  // String getValue( String key )
  // {
  //   if ( myDB == null ) {
  //     TDLog.e( ERROR_NULL_DB + "get value" );
  //     return null;
  //   }
  //   if ( TDString.isNullOrEmpty( key ) ) {
  //     TDLog.e( "AnalyticsHelper::getValue null key");
  //     return null;
  //   }
  //   String value = null;
  //   Cursor cursor = null;
  //   try {
  //     cursor = myDB.query( CONFIG_TABLE,
  //                          new String[] { "value" }, // columns
  //                          "key = ?", new String[] { key },
  //                          null, null, null );
  //     if ( cursor != null && cursor.moveToFirst()) {
  //       value = cursor.getString( 0 );
  //     }
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //   } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
  //   return value;
  // }

  // /** set a configuration value
  //  * @param key   key
  //  * @param value value
  //  */
  // void setValue( String key, String value )
  // {
  //   if ( myDB == null ) {
  //     TDLog.e( ERROR_NULL_DB + "set value" );
  //     return;
  //   }
  //   if ( TDString.isNullOrEmpty( key ) ) {
  //     TDLog.e( "AnalyticsHelper::setValue null key");
  //     return;
  //   }
  //   if ( TDString.isNullOrEmpty( value ) ) {
  //     TDLog.e( "AnalyticsHelper::setValue null value");
  //     return;
  //   }

  //   Cursor cursor = null;
  //   try {
  //     cursor = myDB.query( CONFIG_TABLE,
  //                          new String[] { "value" }, // columns
  //                          "key = ?", new String[] { key },
  //                          null, null, null );
  //     if ( cursor != null ) {
  //       if (cursor.moveToFirst()) {
  //         updateConfig.bindString( 1, value );
  //         updateConfig.bindString( 2, key );
  //         try {
  //           updateConfig.execute();
  //         } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //         } catch (SQLiteException e ) { logError( "update config " + key + "=" + value, e ); }
  //       } else {
  //         ContentValues cv = new ContentValues();
  //         cv.put( "key",     key );
  //         cv.put( "value",   value );
  //         myDB.insert( CONFIG_TABLE, null, cv );
  //       }
  //     }
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //   } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
  // }

  // ----------------------------------------------------------------------
  // DATABASE TABLES

  private static class AnalyticOpenHelper extends SQLiteOpenHelper
  {
     private static final String create_table = "CREATE TABLE IF NOT EXISTS ";

     AnalyticOpenHelper(Context context, String database_name ) 
     {
        super(context, database_name, null, TDVersion.ANALYTIC_DATABASE_VERSION);
        // TDLog.v( "DB NAME " + database_name );
        // TDLog.Log( TDLog.LOG_DB, "createTables ... " + database_name + " version " + TDVersion.ANALYTIC_DATABASE_VERSION );
     }

     @Override
     public void onCreate(SQLiteDatabase db) 
     {
       createTables( db );
       // TDLog.Log( TDLog.LOG_DB, "Analytic Open Helper onCreate done db " + db );
     }

     private void createTables(SQLiteDatabase db )
     {
        // db.setLockingEnabled( false );
        db.beginTransaction();
        try {
          // db.execSQL( 
          //     create_table + CONFIG_TABLE
          //   + " ( key TEXT NOT NULL,"
          //   +   " value TEXT )"
          // );

          // db.execSQL( "insert into " + CONFIG_TABLE + " values ( \"sketch\", \"on\" )" );

          db.execSQL(
              create_table + ANALYTIC_TABLE
            + " ( name TEXT NOT NULL,"
            +   " usage INTEGER "
            +   ")"
          );

          db.setTransactionSuccessful();
        } catch ( SQLException e ) { TDLog.e( "createTables exception " + e.toString() );
        } finally {
          db.endTransaction();
          // db.setLockingEnabled( true );
        }
     }

     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
     {  
        // FIXME this is called at each start when the database file exists
        // TDLog.v( "UPGRADE DB old " + oldVersion + " new " + newVersion );
        switch ( oldVersion ) {
            /* current version */
          default:
            break;
        }
     }
  }
}
