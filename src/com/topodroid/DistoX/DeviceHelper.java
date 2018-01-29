/* @file DeviceHelper.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SQLite "device" database manager
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
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

import android.widget.Toast;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;

public class DeviceHelper extends DataSetObservable
{

  static final String DB_VERSION = "26";
  static final int DATABASE_VERSION = 26;
  static final int DATABASE_VERSION_MIN = 21; 

  private static final String CONFIG_TABLE = "configs";
  private static final String CALIB_TABLE  = "calibs";
  private static final String GM_TABLE     = "gms";
  private static final String DEVICE_TABLE = "devices";

  private SQLiteDatabase myDB = null;
  private long           myNextId;   // id of next shot
  private long           myNextCId;  // id of next calib-data

  private SQLiteStatement updateConfig;
  private SQLiteStatement updateGMGroupStmt = null;
  private SQLiteStatement updateGMErrorStmt = null;
  private SQLiteStatement deleteGMStmt = null;

  private SQLiteStatement updateCalibStmt = null;
  private SQLiteStatement updateCalibAlgoStmt = null;
  private SQLiteStatement updateCalibCoeffStmt = null;
  private SQLiteStatement updateCalibErrorStmt = null;
  private SQLiteStatement resetAllGMStmt = null;

//these are real database "delete"
  private SQLiteStatement doDeleteGMStmt = null;
  private SQLiteStatement doDeleteCalibStmt = null;

  private SQLiteStatement updateDeviceHeadTailStmt = null;
  private SQLiteStatement updateDeviceModelStmt = null;
  private SQLiteStatement updateDeviceNicknameStmt = null;

  private ArrayList<DataListener> mListeners;
  // ----------------------------------------------------------------------
  // DATABASE

  private Context mContext;

  public SQLiteDatabase getDb() { return myDB; }

  public DeviceHelper( Context context, ArrayList<DataListener> listeners )
  {
    mContext = context;
    mListeners = listeners;
    openDatabase();
  }

  public void closeDatabase()
  {
    if ( myDB == null ) return;
    myDB.close();
    myDB = null;
  }


  public void openDatabase() 
  {
    String database_name = TDPath.getDeviceDatabase();
    DistoXOpenHelper openHelper = new DistoXOpenHelper( mContext, database_name );

    try {
      myDB = openHelper.getWritableDatabase();
      if ( myDB == null ) {
        TDLog.Error( "failed get writable database " + database_name );
        return;
      }

      while ( myDB.isDbLockedByOtherThreads() ) {
        try {
          Thread.sleep( 200 );
        } catch ( InterruptedException e ) {}
      }

      updateConfig = myDB.compileStatement( "UPDATE configs SET value=? WHERE key=?" );

    } catch ( SQLiteException e ) {
      myDB = null;
      TDLog.Error( "DeviceHelper cstr failed to get DB " + e.getMessage() );
    }
  }
  
  private void logError( String msg, SQLiteException e )
  {
    TDLog.Error("DB " + msg + ": " + e.getMessage() );
  }


  private void handleDiskIOError( SQLiteDiskIOException e )
  {
    TDLog.Error("DB disk error " + e.getMessage() );
    TopoDroidApp.mActivity.runOnUiThread( new Runnable() {
      public void run() {
        Toast toast = Toast.makeText( mContext, "Critical failure: Disk i/o error", Toast.LENGTH_LONG );
        toast.getView().setBackgroundColor( TDColor.BROWN );
        toast.show();
      }
    } );
  }

  // ----------------------------------------------------------------------
  // CALIBRATION DATA

  void deleteGM( long cid, long id, boolean delete )
  {
    // if ( myDB == null ) return;
    if ( deleteGMStmt == null )
        deleteGMStmt = myDB.compileStatement( "UPDATE gms set status=? WHERE calibID=? AND id=?" );
    deleteGMStmt.bindLong( 1, delete? 1 : 0 );
    deleteGMStmt.bindLong( 2, cid );
    deleteGMStmt.bindLong( 3, id );
    try {
      deleteGMStmt.execute();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "delete GM " + cid + "/" + id, e ); }
  }

  public void doDeleteCalib( long cid ) 
  {
    // if ( myDB == null ) return;
    if ( doDeleteGMStmt == null )
        doDeleteGMStmt    = myDB.compileStatement( "DELETE FROM gms where calibId=?" );
    if ( doDeleteCalibStmt == null )
        doDeleteCalibStmt = myDB.compileStatement( "DELETE FROM calibs where id=?" );
    doDeleteGMStmt.bindLong( 1, cid );
    doDeleteCalibStmt.bindLong( 1, cid );
    try {
      doDeleteGMStmt.execute();
      doDeleteCalibStmt.execute(); 
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "delete calib", e ); }
  }

  public long updateGMName( long gid, long cid, String grp )
  {
    // if ( myDB == null ) return -1;
    if ( updateGMGroupStmt == null )
        updateGMGroupStmt  = myDB.compileStatement( "UPDATE gms SET grp=? WHERE calibId=? AND id=?" );
    updateGMGroupStmt.bindString( 1, grp );
    updateGMGroupStmt.bindLong( 2, cid );
    updateGMGroupStmt.bindLong( 3, gid );
    try {
      updateGMGroupStmt.execute();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "update GM " + cid + "/" + gid + " group " + grp, e ); }
    return 0;
  }

  public long updateGMError( long id, long cid, double error )
  {
    // if ( myDB == null ) return -1;
    if ( updateGMErrorStmt == null ) 
        updateGMErrorStmt  = myDB.compileStatement( "UPDATE gms SET error=? WHERE calibId=? AND id=?" );
    updateGMErrorStmt.bindDouble( 1, error );
    updateGMErrorStmt.bindLong( 2, cid );
    updateGMErrorStmt.bindLong( 3, id );
    try { 
      updateGMErrorStmt.execute();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "update GM error", e ); }
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
    try {
      // this method returns the GM-data ID
      /* long ret = */ myDB.insert( GM_TABLE, null, cv ); // insert returns the nr. of records in the table
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "insert GM", e ); }
    return myNextCId;
  }

    
  // ----------------------------------------------------------------------
  // SELECT STATEMENTS


  public void resetAllGMs( long cid, long start_id )
  {
    if ( resetAllGMStmt == null )
       resetAllGMStmt = myDB.compileStatement( "UPDATE gms SET grp=0, error=0 WHERE calibId=? AND id>? AND status=0" );
       // resetAllGMStmt = myDB.compileStatement( "UPDATE gms SET grp=0, error=0 WHERE calibId=? AND id>?" );
    resetAllGMStmt.bindLong( 1, cid );
    resetAllGMStmt.bindLong( 2, start_id );
    try {
      resetAllGMStmt.execute();
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "reset GM " + cid + "/" + start_id, e ); }
  }

  public List<CalibCBlock> selectAllGMs( long cid, int status )
  {
    List< CalibCBlock > list = new ArrayList<>();
    // if ( myDB == null ) return list;
    Cursor cursor = null;
    try {
      cursor = myDB.query(GM_TABLE,
                          new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error", "status" }, // columns
                          "calibId=?",
                          new String[] { Long.toString(cid) },
                          null, null, "id" );
      if ( cursor != null && cursor.moveToFirst()) {
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
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return list;
  }

  public CalibCBlock selectGM( long id, long cid )
  {
    CalibCBlock block = null;
    // if ( myDB == null ) return null;
    Cursor cursor = null;
    try { 
      cursor = myDB.query(GM_TABLE,
                               new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error", "status" }, // columns
                               "calibId=? and id=?", 
                               new String[] { Long.toString(cid), Long.toString(id) },
                               null,  // groupBy
                               null,  // having
                               null ); // order by
      if ( cursor != null && cursor.moveToFirst()) {
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
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return block;
  }


  public int selectCalibAlgo( long cid )
  {
    int algo = CalibInfo.ALGO_AUTO; // default 
    // if ( myDB == null ) return 0;
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE,
                            new String[] { "algo" }, // columns
                            "id=?",
                            new String[] { Long.toString(cid) },
                            null, null, null ); 
      if ( cursor != null && cursor.moveToFirst()) {
        algo = (int)cursor.getLong( 0 );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return algo;
  }

  public long getCalibCID( String name, String device )
  {
    long id = -1L;
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE,
                           new String[] { "id" }, // columns
                           "name=? and device=?",
                           new String[] { name, device },
                           null, null, null );
      if (cursor != null && cursor.moveToFirst()) {
        id = cursor.getLong( 0 );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return id;
  }
 
  public CalibInfo selectCalibInfo( long cid )
  {
    CalibInfo info = null;
    // if ( myDB == null ) return null;
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE,
                           new String[] { "name", "day", "device", "comment", "algo" }, // columns
                           "id=?",
                           new String[] { Long.toString(cid) },
                           null, null, null ); 
      if ( cursor != null && cursor.moveToFirst()) {
        info = new CalibInfo( 
                cid,
                cursor.getString( 0 ),
                cursor.getString( 1 ),
                cursor.getString( 2 ),
                cursor.getString( 3 ),
                (int)cursor.getLong( 4 ) );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return info;
  }

  public void selectCalibError( long cid, CalibResult res )
  {
    // if ( myDB == null ) return;
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE,
                           new String[] { "error", "max_error", "iterations", "stddev" }, // columns
                           "id=?",
                           new String[] { Long.toString(cid) },
                           null, null, null );
      if ( cursor != null && cursor.moveToFirst()) {
        // Log.v( "DistoX", "select calib error " + cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) );
        try {
          String str = cursor.getString(0);
          if ( str != null ) res.error = Float.parseFloat( str );
          str = cursor.getString(1);
          if ( str != null ) res.max_error = Float.parseFloat( str );
          str = cursor.getString(2);
          if ( str != null ) res.iterations = Integer.parseInt( str );
          str = cursor.getString(3);
          if ( str != null ) res.stddev = Float.parseFloat( str );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "selectCalibError parse Float error: calib ID " + cid );
        }
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
  }

  public String selectCalibCoeff( long cid )
  {
    String coeff = null;
    // if ( myDB == null ) return null;
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE,
                           new String[] { "coeff" }, // columns
                           "id=?",
                           new String[] { Long.toString(cid) },
                           null, null, null );
      if ( cursor != null && cursor.moveToFirst()) {
        coeff = cursor.getString( 0 );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return coeff;
  }
   
  // ----------------------------------------------------------------------
  // SELECT: LIST SURVEY / CABIL NAMES

  private List<String> selectAllNames( String table )
  {
    TDLog.Log( TDLog.LOG_DB, "selectAllNames table " + table );

    List< String > list = new ArrayList<>();
    // if ( myDB == null ) return list;
    Cursor cursor = null;
    try {
      cursor = myDB.query( table,
                           new String[] { "name" }, // columns
                           null, null, null, null, "name" );
      if (cursor.moveToFirst()) {
        do {
          list.add( cursor.getString(0) );
        } while (cursor.moveToNext());
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    TDLog.Log( TDLog.LOG_DB, "found " + list.size() + " names " );
    return list;
  }

  public List<String> selectAllCalibs() { return selectAllNames( CALIB_TABLE ); }

  public List<String> selectDeviceCalibs( String device ) 
  {
    List<String> ret = new ArrayList<>();
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE,
                           new String[] { "name", "day" }, // columns
                           "device=?",
                           new String[] { device },
                           null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        do {
          ret.add( cursor.getString(0) + " - " + cursor.getString(1) );
        } while (cursor.moveToNext());
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  public List<CalibInfo> selectDeviceCalibsInfo( String device ) 
  {
    List<CalibInfo> ret = new ArrayList<>();
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE,
                           new String[] { "id", "name", "day", "comment", "algo" }, // columns
                           "device=?",
                           new String[] { device },
                           null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
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
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  // ----------------------------------------------------------------------
  // CONFIG DATA

  public String getValue( String key )
  {
    if ( myDB == null ) {
      TDLog.Error( "DeviceHelper::getValue null DB");
      return null;
    }
    if ( key == null || key.length() == 0 ) {
      TDLog.Error( "DeviceHelper::getValue null key");
      return null;
    }
    String value = null;
    Cursor cursor = null;
    try {
      cursor = myDB.query( CONFIG_TABLE,
                           new String[] { "value" }, // columns
                           "key = ?", new String[] { key },
                           null, null, null );
      if ( cursor != null && cursor.moveToFirst()) {
        value = cursor.getString( 0 );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return value;
  }

  public void setValue( String key, String value )
  {
    if ( myDB == null ) {
      TDLog.Error( "DeviceHelper::setValue null DB");
      return;
    }
    if ( key == null || key.length() == 0 ) {
      TDLog.Error( "DeviceHelper::setValue null key");
      return;
    }
    if ( value == null || value.length() == 0 ) {
      TDLog.Error( "DeviceHelper::setValue null value");
      return;
    }

    Cursor cursor = null;
    try {
      cursor = myDB.query( CONFIG_TABLE,
                           new String[] { "value" }, // columns
                           "key = ?", new String[] { key },
                           null, null, null );
      if ( cursor != null ) {
        if (cursor.moveToFirst()) {
          updateConfig.bindString( 1, value );
          updateConfig.bindString( 2, key );
          try {
            updateConfig.execute();
          } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
          } catch (SQLiteException e ) { logError( "update config " + key + "=" + value, e ); }
        } else {
          ContentValues cv = new ContentValues();
          cv.put( "key",     key );
          cv.put( "value",   value );
          myDB.insert( CONFIG_TABLE, null, cv );
        }
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
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
    if ( myDB != null ) {
      ContentValues cv = new ContentValues();
      cv.put( "key",     name );
      cv.put( "value",   "1" );     // symbols are enabled by default
      try {
        myDB.insert( CONFIG_TABLE, null, cv );
      } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
      } catch (SQLiteException e ) { logError( "symbol enable " + name, e ); }
    }
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
    Cursor cursor = null;
    try {
      cursor = myDB.query( table, new String[] { "name" },
                           "id=?", new String[] { Long.toString(id) },
                           null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        ret = cursor.getString(0);
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  private long getIdFromName( String table, String name ) 
  {
    long id = -1;
    // if ( myDB == null ) { return -2; }
    Cursor cursor = null;
    try {
      cursor = myDB.query( table, new String[] { "id" },
                           "name = ?", new String[] { name },
                           null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        id = cursor.getLong(0);
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return id;
  }

  // this must be called when the calib name is not yet in the db
  long insertCalibInfo( String name, String date, String device, String comment, long algo )
  {
    if ( hasCalibName( name ) ) return -1L;
    long id = 1;
    Cursor cursor = null;
    try {
      cursor = myDB.query( "calibs", new String[] { "max(id)" }, null, null, null, null, null );
      if ( cursor != null && cursor.moveToFirst() ) {
        id = 1 + cursor.getLong(0);
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }

    ContentValues cv = new ContentValues();
    cv.put( "id",      id );
    cv.put( "name",    name );
    cv.put( "day",     date );
    cv.put( "device",  device );
    cv.put( "comment", comment );
    cv.put( "algo",    algo );
    try {
      myDB.insert( "calibs", null, cv );
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "insert calib info", e ); }
    myNextCId = 0;
    return id;
  }

  // used only by setCalib
  private long setCalibName( String name ) 
  {
    long id = -1;
    // if ( myDB == null ) { return 0; }
    // TDLog.Log( TDLog.LOG_DB, "set Calib Name >" + name + "< table " + table );
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE, new String[] { "id" },
                           "name = ?", new String[] { name },
                           null, null, null );
      if ( cursor != null && cursor.moveToFirst() ) {
        id = cursor.getLong(0);
        if (cursor != null && !cursor.isClosed()) { cursor.close(); cursor = null; }
      } else {
        if (cursor != null && !cursor.isClosed()) { cursor.close(); cursor = null; }
        // SELECT max(id) FROM table
        cursor = myDB.query( CALIB_TABLE, new String[] { "max(id)" },
                             null, null, null, null, null );
        if (cursor != null && cursor.moveToFirst() ) {
          id = 1 + cursor.getLong(0);
        } else {
          id = 1;
        }
        if (cursor != null && !cursor.isClosed()) { cursor.close(); cursor = null; }
        // INSERT INTO table VALUES( id, name, "", "" )
        ContentValues cv = new ContentValues();
        cv.put( "id",      id );
        cv.put( "name",    name );
        cv.put( "day",     "" );
        cv.put( "comment", "" );
        myDB.insert( CALIB_TABLE, null, cv );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "set calib name" + name, e ); 
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return id;
  }

  private long maxId( String table, long sid )
  {
    long id = 1;
    // if ( myDB == null ) return 1L;
    Cursor cursor = null;
    try {
      cursor = myDB.query( table, new String[] { "max(id)" },
                         "surveyId=?", 
                         new String[] { Long.toString(sid) },
                         null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        id = 1 + cursor.getLong(0);
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return id;
  }

  public ArrayList< Device > getDevices( ) 
  {
    ArrayList<Device> ret = new ArrayList<>();
    // if ( myDB == null ) return ret;
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "address", "model", "head", "tail", "name", "nickname" }, 
                                null, null, null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        do {
          ret.add( new Device( cursor.getString(0), 
                               cursor.getString(1),
                               (int)cursor.getLong(2),
                               (int)cursor.getLong(3),
                               cursor.getString(4),
                               cursor.getString(5)
                  ) );
        } while (cursor.moveToNext());
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  // get device by address or by nickname
  public Device getDevice( String addr )
  {
    // if ( myDB == null ) return null;
    Device ret = getDeviceByAddress( addr );
    if ( ret == null ) {
      ret = getDeviceByNickname( addr );
    }
    return ret;
  }
       
  private Device getDeviceByNickname( String nickname )
  {
    if ( myDB == null ) return null;
    Device ret = null;
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "address", "model", "head", "tail", "name", "nickname" }, 
                                "nickname=?", new String[] { nickname }, null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        ret = new Device( cursor.getString(0), 
                          cursor.getString(1),
                          (int)cursor.getLong(2),
                          (int)cursor.getLong(3),
                          cursor.getString(4),
                          cursor.getString(5)
                        );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  private Device getDeviceByAddress( String addr )
  {
    if ( myDB == null ) return null;
    Device ret = null;
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "address", "model", "head", "tail", "name", "nickname" }, 
                                "address=?", new String[] { addr }, null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        ret = new Device( cursor.getString(0), 
                          cursor.getString(1),
                          (int)cursor.getLong(2),
                          (int)cursor.getLong(3),
                          cursor.getString(4),
                          cursor.getString(5)
                        );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  public int getDeviceTail( String address )
  { 
    // if ( myDB == null ) return 0;
    int ret = 0;
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "tail" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        ret = (int)( cursor.getLong(0) );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  public boolean getDeviceHeadTail( String address, int[] head_tail )
  {
    // if ( myDB == null ) return false;
    boolean ret = false;
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "head", "tail" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        head_tail[0] = (int)( cursor.getLong(0) );
        head_tail[1] = (int)( cursor.getLong(1) );
        ret = true;
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  boolean insertDevice( String address, String model, String name )
  {
    if ( myDB == null ) return false;
    boolean ret = false;
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "model" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
      if ( cursor != null ) {
        if (cursor.moveToFirst() ) {
          // TODO address already in the database: check model
        } else {
          ContentValues cv = new ContentValues();
          cv.put( "address", address );
          cv.put( "model",   model );
          cv.put( "head",    0 );
          cv.put( "tail",    0 );
          cv.put( "name",    name );
          cv.put( "nickname", "" );  // FIXME empty nickname
          myDB.insert( DEVICE_TABLE, null, cv );
	  ret = true;
        }
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "insert device", e ); 
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
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
    cv.put( "nickname", "" );  // FIXME empty nickname
    try {
      myDB.insert( DEVICE_TABLE, null, cv );
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "insert device H-T", e ); }
  }

  public void updateDeviceModel( String address, String model )
  {
    if ( updateDeviceModelStmt == null )
      updateDeviceModelStmt = myDB.compileStatement( "UPDATE devices set model=? WHERE address=?" );
    updateDeviceModelStmt.bindString( 1, model );
    updateDeviceModelStmt.bindString( 2, address );
    try { updateDeviceModelStmt.execute(); 
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "update device", e ); }
  }

  public void updateDeviceNickname( String address, String nickname )
  {
    if ( updateDeviceNicknameStmt == null )
        updateDeviceNicknameStmt = myDB.compileStatement( "UPDATE devices set nickname=? WHERE address=?" );
    updateDeviceNicknameStmt.bindString( 1, nickname );
    updateDeviceNicknameStmt.bindString( 2, address );
    try { updateDeviceNicknameStmt.execute(); 
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "update device nickname", e ); }
  }

  public boolean updateDeviceHeadTail( String address, int[] head_tail )
  {
    // if ( myDB == null ) return false;
    boolean ret = false;
    if ( updateDeviceHeadTailStmt == null )
        updateDeviceHeadTailStmt = myDB.compileStatement( "UPDATE devices set head=?, tail=? WHERE address=?" );
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "head" },
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
          try { updateDeviceHeadTailStmt.execute();
          } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
          } catch (SQLiteException e ) { logError( "update device H-T", e ); }
          ret = true;
        } else {
          // insertDeviceHeadTail( address, "DistoX", head_tail, name ); // FIXME name ?
        }
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }


  public boolean hasCalibName( String name )  { return hasName( name, CALIB_TABLE ); }

  private boolean hasName( String name, String table )
  {
    boolean ret = false;
    // if ( myDB == null ) return ret;
    Cursor cursor = null;
    try {
      cursor = myDB.query( table, new String[] { "id" },
                          "name=?", 
                          new String[] { name },
                          null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
         ret = true;
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
     return ret;
   }

   public boolean updateCalibInfo( long id, String date, String device, String comment )
   {
     // TDLog.Log( TDLog.LOG_DB, "updateCalibInfo id " + id + " day " + date + " comm. " + comment );
     if ( date == null ) return false;
     if ( updateCalibStmt == null )
        updateCalibStmt = myDB.compileStatement( "UPDATE calibs SET day=?, device=?, comment=? WHERE id=?" );
     String dev = (device != null)? device : "";
     String cmt = (comment != null)? comment : "";
     updateCalibStmt.bindString( 1, date );
     updateCalibStmt.bindString( 2, dev );
     updateCalibStmt.bindString( 3, cmt );
     updateCalibStmt.bindLong( 4, id );
     try {
       updateCalibStmt.execute(); 
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch (SQLiteException e ) { logError( "update calib", e ); }
     return true;
   }

   public boolean updateCalibAlgo( long id, long algo )
   {
     // TDLog.Log( TDLog.LOG_DB, "updateCalibAlgo id " + id + " algo " + algo );
     if ( updateCalibAlgoStmt == null )
        updateCalibAlgoStmt = myDB.compileStatement( "UPDATE calibs SET algo=? WHERE id=?" );
     updateCalibAlgoStmt.bindLong( 1, algo );
     updateCalibAlgoStmt.bindLong( 2, id );
     try {
       updateCalibAlgoStmt.execute();
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch (SQLiteException e ) { logError( "update calib algo", e ); }
     return true;
   }

   public boolean updateCalibCoeff( long id, String coeff )
   {
     // TDLog.Log( TDLog.LOG_DB, "updateCalibCoeff id " + id + " coeff. " + coeff );
     if ( coeff == null ) return false;
     if ( updateCalibCoeffStmt == null )
        updateCalibCoeffStmt = myDB.compileStatement( "UPDATE calibs SET coeff=? WHERE id=?" );
     updateCalibCoeffStmt.bindString( 1, coeff );
     updateCalibCoeffStmt.bindLong( 2, id );
     try {
       updateCalibCoeffStmt.execute();
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch (SQLiteException e ) { logError( "update calib coeff", e ); }
     return true;
   }

   public boolean updateCalibError( long id, double error, double stddev, double max_error, int iterations )
   {
     // TDLog.Log( TDLog.LOG_DB, "updateCalibCoeff id " + id + " coeff. " + coeff );
     if ( updateCalibErrorStmt == null )
        updateCalibErrorStmt = myDB.compileStatement( "UPDATE calibs SET error=?, stddev=?, max_error=?, iterations=? WHERE id=?" );
     updateCalibErrorStmt.bindDouble( 1, error );
     updateCalibErrorStmt.bindDouble( 2, stddev );
     updateCalibErrorStmt.bindDouble( 3, max_error );
     updateCalibErrorStmt.bindLong( 4, iterations );
     updateCalibErrorStmt.bindLong( 5, id );
     try {
       updateCalibErrorStmt.execute(); 
     } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     } catch (SQLiteException e ) { logError( "update calib error", e ); }
     return true;
   }

   public long setCalib( String calib ) 
   {
     myNextCId = 0;
     // if ( myDB == null ) return 0L;
     long cid = setCalibName( calib );
     Cursor cursor = null;
     try {
       cursor = myDB.query( GM_TABLE, new String[] { "max(id)" },
                          "calibId=?", new String[] { Long.toString(cid) },
                          null, null, null );
       if (cursor != null && cursor.moveToFirst() ) {
         myNextCId = cursor.getLong(0);
       }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
     return cid;
   }

   public String getCalibFromId( long cid ) { return getNameFromId( CALIB_TABLE, cid ); }


   // ----------------------------------------------------------------------
   // SERIALIZATION of surveys TO FILE
   // the following tables are serialized (besides the survey recond)

   // public void dumpToFile( String filename, long sid )
   // {
   //   // TDLog.Log( TDLog.LOG_DB, "dumpToFile " + filename );
   //   if ( myDB == null ) return;
   //   try {
   //     TDPath.checkPath( filename );
   //     FileWriter fw = new FileWriter( filename );
   //     PrintWriter pw = new PrintWriter( fw );
   //     // Cursor cursor = myDB.query( TABLE, 
   //     //                      new String[] { "id", "shotId", "status", "title", "date", "comment", "type", "value" },
   //     //                      "surveyId=?", new String[] { Long.toString( sid ) },
   //     //                      null, null, null );
   //     // if (cursor.moveToFirst()) {
   //     //   do {
   //     //     pw.format(Locale.US,
   //     //               "INSERT into %s values( %d, %d, %d, %d, \"%s\", \"%s\", \"%s\", \"%s\", \"%s\" );\n",
   //     //               FIXED_TABLE,
   //     //               sid,
   //     //               cursor.getLong(0),
   //     //               cursor.getLong(1),
   //     //               cursor.getLong(2),
   //     //               cursor.getString(3),
   //     //               cursor.getString(4),
   //     //               cursor.getString(5),
   //     //               cursor.getString(6),
   //     //               cursor.getString(7)
   //     //              );
   //     //   } while (cursor.moveToNext());
   //     // }
   //     // if (cursor != null && !cursor.isClosed()) {
   //     //   cursor.close();
   //     // }
   //     fw.flush();
   //     fw.close();
   //   } catch ( FileNotFoundException e ) {
   //     // FIXME
   //   } catch ( IOException e ) {
   //     // FIXME
   //   }
   // }

   // /** load survey data from a sql file
   //  * @param filename  name of the sql file
   //  */
   // long loadFromFile( String filename, int db_version )
   // {
   //   long sid = -1;
   //   long id, status, shotid;
   //   String station, title, date, name, comment;
   //   String line;
   //   try {
   //     FileReader fr = new FileReader( filename );
   //     BufferedReader br = new BufferedReader( fr );
   //     // first line is survey
   //     line = br.readLine();
   //     // TDLog.Log( TDLog.LOG_DB, "loadFromFile: " + line );
   //     String[] vals = line.split(" ", 4); 
   //     // if ( vals.length != 4 ) { TODO } // FIXME
   //     String table = vals[2];
   //     String v = vals[3];
   //     pos = v.indexOf( '(' ) + 1;
   //     len = v.lastIndexOf( ')' );
   //     skipSpaces( v );
   //     // ... 
   //     fr.close();
   //   } catch ( FileNotFoundException e ) {
   //   } catch ( IOException e ) {
   //   }
   //   return sid;
   // }

   // ----------------------------------------------------------------------
   // DATABASE TABLES

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
         db.setLockingEnabled( false );
         db.beginTransaction();
         try {
           db.execSQL( 
               create_table + CONFIG_TABLE
             + " ( key TEXT NOT NULL,"
             +   " value TEXT )"
           );

           // db.execSQL( "insert into " + CONFIG_TABLE + " values ( \"sketch\", \"on\" )" );

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
             +   " algo INTEGER default 0, "
             +   " stddev REAL default 0 "
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
               create_table + DEVICE_TABLE
             + " ( address TEXT, "
             +   " model TEXT, "
             +   " head INTEGER, "
             +   " tail INTEGER, "
             +   " name TEXT, "
             +   " nickname TEXT "
             +   ")"
           );


           db.setTransactionSuccessful();
         } catch ( SQLException e ) { TDLog.Error( "createTables exception " + e.toString() );
         } finally {
           db.endTransaction();
           db.setLockingEnabled( true );
         }
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {  
         // FIXME this is called at each start when the database file exists
         TDLog.Log( TDLog.LOG_DB, "onUpgrade old " + oldVersion + " new " + newVersion );
         switch ( oldVersion ) {
           case 16:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN coeff BLOB" );
           case 17:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN error REAL default 0" );
             db.execSQL( "ALTER TABLE calibs ADD COLUMN max_error REAL default 0" );
             db.execSQL( "ALTER TABLE calibs ADD COLUMN iterations INTEGER default 0" );
           case 18:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN algo INTEGER default 1" );
           case 23:
             db.execSQL( "ALTER TABLE devices ADD COLUMN nickname TEXT default \"\"" );
           case 24:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN stddev REAL default 0" );
           case 25:
           case 26:
             /* current version */
           default:
             break;
         }
      }
   }
}
