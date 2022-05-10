/* @file DeviceHelper.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid SQLite "device" database manager
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDVersion;
import com.topodroid.dev.Device;
import com.topodroid.calib.CBlock;
import com.topodroid.calib.CalibInfo;
import com.topodroid.calib.CalibResult;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.DataSetObservable;
// import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDiskIOException;

import java.util.ArrayList;
import java.util.List;
// import java.util.Locale;
// import java.util.HashMap;

public class DeviceHelper extends DataSetObservable
{

  // static final private String DEVICE_DB_VERSION = "27";
  // static final private int DEVICE_DATABASE_VERSION = 27;
  // static final private int DEVICE_DATABASE_VERSION_MIN = 21;

  static final String ERROR_NULL_DB = "null device DB ";

  private static final String CONFIG_TABLE = "configs";
  private static final String CALIB_TABLE  = "calibs";
  private static final String GM_TABLE     = "gms";
  private static final String DEVICE_TABLE = "devices";

  private static final String WHERE_CID_ID = "calibId=? AND id=?";
  private static final String WHERE_CID_IDMORE = "calibId=? AND id>? AND status=0";
  private static final String WHERE_ID = "id=?";
  private static final String WHERE_ADDRESS = "address=?";

  private SQLiteDatabase myDB = null;
  private long           myNextId;   // id of next shot
  private long           myNextCId;  // id of next calib-data

  private SQLiteStatement updateConfig;
  // private SQLiteStatement updateGMGroupStmt = null;
  // private SQLiteStatement updateGMErrorStmt = null;
  // private SQLiteStatement updateCalibStmt = null;
  // private SQLiteStatement updateCalibAlgoStmt = null;
  // private SQLiteStatement updateCalibCoeffStmt = null;
  // private SQLiteStatement updateCalibErrorStmt = null;
  // private SQLiteStatement resetAllGMStmt = null;

//these are real database "delete"
  private SQLiteStatement deleteGMStmt = null;
  private SQLiteStatement doDeleteGMStmt = null;
  private SQLiteStatement doDeleteCalibStmt = null;

  // private SQLiteStatement updateDeviceHeadTailStmt = null;
  // private SQLiteStatement updateDeviceModelStmt = null;
  // private SQLiteStatement updateDeviceNicknameStmt = null;

  // private ArrayList< DataListener > mListeners; // IF_COSURVEY
  // ----------------------------------------------------------------------
  // DATABASE

  private final Context mContext;

  /** @return the SQLite database
   */
  SQLiteDatabase getDb() { return myDB; }

  // DeviceHelper( Context context, TopoDroidApp app, ArrayList< DataListener > listeners ) // IF_COSURVEY
  /** cstr
   * @param context context
   */
  DeviceHelper( Context context )
  {
    mContext = context;
    // mListeners = listeners; // IF_COSURVEY
    openDatabase();
  }

  /** close the database
   */
  void closeDatabase()
  {
    if ( myDB == null ) return;
    myDB.close();
    myDB = null;
  }

  /** open the "devuice10.sqlite" database
   */
  private void openDatabase()
  {
    String database_name = TDFile.getDeviceDatabase().getAbsolutePath();
    // TDLog.v( "Device DB <" + database_name + ">");
    DistoXOpenHelper openHelper = new DistoXOpenHelper( mContext, database_name );

    try {
      myDB = openHelper.getWritableDatabase();
      if ( myDB == null ) {
        TDLog.Error( "failed get writable database " + database_name );
        return;
      }

      // while ( myDB.isDbLockedByOtherThreads() ) {
      //   TDUtil.slowDown( 200 );
      // }

      updateConfig = myDB.compileStatement( "UPDATE configs SET value=? WHERE key=?" );

    } catch ( SQLiteException e ) {
      myDB = null;
      TDLog.Error( "Failed to get device DB " + e.getMessage() );
    }
  }
  
  /** log an error message
   * @param msg    message
   * @param e      error
   */
  private void logError( String msg, SQLiteException e )
  {
    TDLog.Error("DB " + msg + ": " + e.getMessage() );
  }

  /** handle disk I/O error
   * @param e  disk error
   */
  private void handleDiskIOError( SQLiteDiskIOException e )
  {
    TDLog.Error("DB disk error " + e.getMessage() );
    if ( TopoDroidApp.mMainActivity != null ) {
      TopoDroidApp.mMainActivity.runOnUiThread( new Runnable() { public void run() { TDToast.makeBad( R.string.disk_io_error ); } } );
    }
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
      TDLog.Error( ERROR_NULL_DB + "do update");
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

  // ----------------------------------------------------------------------
  // CALIBRATION DATA

  /** mark a calibration data "deleted"
   * @param cid    calibration ID
   * @param id     data ID
   * @param delete whether to mark the date "deleted" or "normal"
   */
  void deleteGM( long cid, long id, boolean delete )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "delete GM");
      return;
    }
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

  /** delete a calibration and its data
   * @param cid    calibration ID
   */
  void doDeleteCalib( long cid )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "delete calib");
      return;
    }
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

  /** update the group of a calibration data
   * @param gid    data ID
   * @param cid    calibration ID
   * @param grp   data group, as string
   */
  public void updateGMName( long gid, long cid, String grp )
  {
    ContentValues cv = new ContentValues();
    cv.put( "grp", grp );
    doUpdate( "gms", cv, WHERE_CID_ID, new String[] { Long.toString(cid), Long.toString(gid) }, "GM name" );

    // if ( updateGMGroupStmt == null )
    //     updateGMGroupStmt  = myDB.compileStatement( "UPDATE gms SET grp=? WHERE calibId=? AND id=?" );
    // updateGMGroupStmt.bindString( 1, grp );
    // updateGMGroupStmt.bindLong( 2, cid );
    // updateGMGroupStmt.bindLong( 3, gid );
    // try {
    //   updateGMGroupStmt.execute();
    // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    // } catch (SQLiteException e ) { logError( "update GM " + cid + "/" + gid + " group " + grp, e ); }
    // // return 0;
  }

  /** update the error of a calibration data
   * @param gid    data ID
   * @param cid    calibration ID
   * @param error  data error value
   */
  void updateGMError( long gid, long cid, double error )
  {
    ContentValues cv = new ContentValues();
    cv.put( "error", error );
    doUpdate( "gms", cv, WHERE_CID_ID, new String[] { Long.toString(cid), Long.toString(gid) }, "GM error" );

    // if ( updateGMErrorStmt == null ) 
    //     updateGMErrorStmt  = myDB.compileStatement( "UPDATE gms SET error=? WHERE calibId=? AND id=?" );
    // updateGMErrorStmt.bindDouble( 1, error );
    // updateGMErrorStmt.bindLong( 2, cid );
    // updateGMErrorStmt.bindLong( 3, gid );
    // try { 
    //   updateGMErrorStmt.execute();
    // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    // } catch (SQLiteException e ) { logError( "update GM error", e ); }
    // // return 0;
  }

  /** insert a calibration data
   * @param cid     calibration ID
   * @param gx      G X-component
   * @param gy      G Y-component
   * @param gz      G Z-component
   * @param mx      M X-component
   * @param my      M Y-component
   * @param mz      M Z-component
   * @return the new data ID
   */
  public long insertGM( long cid, long gx, long gy, long gz, long mx, long my, long mz )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "insert GM");
      return -1L;
    }
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

  /** reset all the GM data (group=0, error=0)
   * @param cid      calibration ID
   * @param start_id ID where to start to reset
   */
  void resetAllGMs( long cid, long start_id )
  {
    ContentValues cv = new ContentValues();
    cv.put( "grp", 0 );
    cv.put( "error", 0 );
    doUpdate( "gms", cv, WHERE_CID_IDMORE, new String[] { Long.toString(cid), Long.toString(start_id) }, "GM reset" );    

    // if ( resetAllGMStmt == null )
    //    resetAllGMStmt = myDB.compileStatement( "UPDATE gms SET grp=0, error=0 WHERE calibId=? AND id>? AND status=0" );
    //    // resetAllGMStmt = myDB.compileStatement( "UPDATE gms SET grp=0, error=0 WHERE calibId=? AND id>?" );
    // resetAllGMStmt.bindLong( 1, cid );
    // resetAllGMStmt.bindLong( 2, start_id );
    // try {
    //   resetAllGMStmt.execute();
    // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    // } catch (SQLiteException e ) { logError( "reset GM " + cid + "/" + start_id, e ); }
  }

  /** @return the list of GM data
   * @param cid      calibration ID
   * @param status   if 0 return only good-data, if 1 all data
   * @param negative_too whether to include also negative-group data 
   */
  public List< CBlock > selectAllGMs( long cid, int status, boolean negative_too )
  {
    List< CBlock > list = new ArrayList<>();
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select all GM");
      return list;
    }
    Cursor cursor = null;
    try {
      cursor = myDB.query(GM_TABLE,
                          new String[] { "id", "gx", "gy", "gz", "mx", "my", "mz", "grp", "error", "status" }, // columns
                          "calibId=?",
                          new String[] { Long.toString(cid) },
                          null, null, "id" );
      if ( cursor != null && cursor.moveToFirst()) {
        do {
          long grp = cursor.getLong(7);
          long sts = cursor.getLong(9);
          if ( status >= (int)sts ) { // status == 0 --> only good shots
                                      // status == 1 --> all shots
            if ( negative_too || grp >= 0 ) {
              CBlock block = new CBlock();
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
          }
        } while (cursor.moveToNext());
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return list;
  }

  /** @return a GM data
   * @param id      GM data ID
   * @param cid     calibration ID
   */
  CBlock selectGM( long id, long cid )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select GM");
      return null;
    }
    CBlock block = null;
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
        block = new CBlock();
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

  /** @return the index of the calibration algorithm (default AUTO)
   * @param cid     calibration ID
   */
  public int selectCalibAlgo( long cid )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select algo");
      return CalibInfo.ALGO_AUTO;
    }
    int algo = CalibInfo.ALGO_AUTO;  // default 
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

  /** @return the calibration ID (-1 if the calibration name is not present)
   * @param name   calibration name
   * @param device device
   */
  long getCalibCID( String name, String device )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "get CID");
      return -1L;
    }
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
 
  /** @return the info of a calibration 
   * @param cid     calibration ID
   */
  public CalibInfo selectCalibInfo( long cid )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select calib info");
      return null;
    }
    CalibInfo info = null;
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

  /** get the results of a calibration 
   * @param cid     calibration ID
   * @param res     calibration result (output)
   */
  public void selectCalibError( long cid, CalibResult res )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select calib error");
      return;
    }
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE,
                           new String[] { "error", "max_error", "iterations", "stddev", "delta_bh" }, // columns
                           "id=?",
                           new String[] { Long.toString(cid) },
                           null, null, null );
      if ( cursor != null && cursor.moveToFirst()) {
        // TDLog.v( "select calib error " + cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) );
        try {
          String str = cursor.getString(0);
          if ( str != null ) res.error = Float.parseFloat( str );
          str = cursor.getString(1);
          if ( str != null ) res.max_error = Float.parseFloat( str );
          str = cursor.getString(2);
          if ( str != null ) res.iterations = Integer.parseInt( str );
          str = cursor.getString(3);
          if ( str != null ) res.stddev = Float.parseFloat( str );
          str = cursor.getString(4);
          if ( str != null ) res.delta_bh = Float.parseFloat( str );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "selectCalibError parse Float error: calib ID " + cid );
        }
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
  }

  /** @return the coefficients of a calibration (as a string)
   * @param cid     calibration ID
   */
  public String selectCalibCoeff( long cid )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select calib coeff");
      return null;
    }
    String coeff = null;
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

  /** @return the list of names in a table
   * @param table   table
   */
  private List< String > selectAllNames( String table )
  {
    // TDLog.Log( TDLog.LOG_DB, "selectAllNames table " + table );
    List< String > list = new ArrayList<>();
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select all names");
      return list;
    }
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

  /** @return the list of calibration names
   */
  List< String > selectAllCalibs() { return selectAllNames( CALIB_TABLE ); }

  /** @return the list of calibration names of a device
   * @param device  device
   */
  public List< String > selectDeviceCalibs( String device )
  {
    List< String > ret = new ArrayList<>();
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select calibs");
      return ret;
    }
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

  /** @return the list of calibration infos of a device
   * @param device  device
   */
  public List< CalibInfo > selectDeviceCalibsInfo( String device ) 
  {
    List< CalibInfo > ret = new ArrayList<>();
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "select calibs info");
      return ret;
    }
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

  /** @return a configuration value
   * @param key  value key
   */
  String getValue( String key )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "get value" );
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

  /** set a configuration value
   * @param key   key
   * @param value value
   */
  void setValue( String key, String value )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "set value" );
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

  /** set a symbol enabled flag
   * @param name    symbol name
   * @param enabled new enabled flag
   */
  void setSymbolEnabled( String name, boolean enabled ) { setValue( name, enabled? TDString.ONE : TDString.ZERO ); }

  /** @return true is a symbol is enabled
   * @param name  symbol name
   */
  boolean isSymbolEnabled( String name )
  { 
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "is symbol enabled" );
      return true;
    }
    String enabled = getValue( name );
    if ( enabled != null ) {
      return enabled.equals(TDString.ONE);
    }
    if ( myDB != null ) {
      ContentValues cv = new ContentValues();
      cv.put( "key",     name );
      cv.put( "value",   TDString.ONE );     // symbols are enabled by default
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

  /** @return the name field of a given ID
   * @param table  table
   * @param id     given ID
   */
  private String getNameFromId( String table, long id )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "get name from id" );
      return null;
    }
    String ret = null;
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

  // private long getIdFromName( String table, String name ) 
  // {
  //   if ( myDB == null ) {
  //     TDLog.Error( ERROR_NULL_DB + "get id from name" );
  //     return -1L;
  //   }
  //   long id = -1L;
  //   Cursor cursor = null;
  //   try {
  //     cursor = myDB.query( table, new String[] { "id" },
  //                          "name = ?", new String[] { name },
  //                          null, null, null );
  //     if (cursor != null && cursor.moveToFirst() ) {
  //       id = cursor.getLong(0);
  //     }
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //   } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
  //   return id;
  // }

  /** insert a calibration in the database
   * @param name    `calibration name
   * @param date    calibration date
   * @param device  calibration device
   * @param comment calibration description
   * @param algo    calibration algorithm
   * @note this must be called when the calib name is not yet in the db
   */
  public long insertCalibInfo( String name, String date, String device, String comment, long algo )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "insert calib info");
      return -1L;
    }
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

  /** add a new calibration to the database, if the name is not already there
   * @param name   calibration name
   * @return calibration ID
   *
   * @note used only by setCalib
   * @note DB non-null
   */
  private long setCalibName( String name ) 
  {
    long id = -1;
    // TDLog.Log( TDLog.LOG_DB, "set Calib Name >" + name + "< table " + table );
    Cursor cursor = null;
    try {
      cursor = myDB.query( CALIB_TABLE, new String[] { "id" },
                           "name = ?", new String[] { name },
                           null, null, null );
      if ( cursor != null && cursor.moveToFirst() ) {
        id = cursor.getLong(0);
        if ( /* cursor != null && */ !cursor.isClosed()) { cursor.close(); cursor = null; }
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
        cv.put( "day",     TDString.EMPTY );
        cv.put( "comment", TDString.EMPTY );
        myDB.insert( CALIB_TABLE, null, cv );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "set calib name" + name, e ); 
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return id;
  }

  // private long maxId( String table, long sid )
  // {
  //   if ( myDB == null ) {
  //     TDLog.Error( ERROR_NULL_DB + "max ID");
  //     return 1L;
  //   }
  //   long id = 1L;
  //   Cursor cursor = null;
  //   try {
  //     cursor = myDB.query( table, new String[] { "max(id)" },
  //                        "surveyId=?", 
  //                        new String[] { Long.toString(sid) },
  //                        null, null, null );
  //     if (cursor != null && cursor.moveToFirst() ) {
  //       id = 1 + cursor.getLong(0);
  //     }
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //   } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
  //   return id;
  // }

  /** @return the list of (known) device in the database
   */
  ArrayList< Device > getDevices( ) 
  {
    ArrayList< Device > ret = new ArrayList<>();
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "get devices");
      return ret;
    }
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

  /** @return a device by address or by nickname
   * @param addr  device address or nickname
   */
  Device getDevice( String addr )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "get device");
      return null;
    }
    Device ret = getDeviceByAddress( addr );
    if ( ret == null ) {
      ret = getDeviceByNickname( addr );
    }
    return ret;
  }
       
  /** @return a device by the nickname
   * @param nickname  device nickname
   * @note DB non-null
   */
  private Device getDeviceByNickname( String nickname )
  {
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

  /** @return a device by the address
   * @param addr  device address
   * @note DB non-null
   */
  private Device getDeviceByAddress( String addr )
  {
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

  /** @return a device tail index (useful only for DistoX v. 1 ?)
   * @param addr  device address
   */
  int getDeviceTail( String address )
  { 
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "get device tail");
      return 0;
    }
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

  /** get a device haed-tail indices (useful only for DistoX v. 1)
   * @param addr       device address
   * @param head_tail  (output) head-tail pair
   */
  void getDeviceHeadTail( String address, int[] head_tail )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "get device head-tail");
      return;
    }
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "head", "tail" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
      if (cursor != null && cursor.moveToFirst() ) {
        head_tail[0] = (int)( cursor.getLong(0) );
        head_tail[1] = (int)( cursor.getLong(1) );
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
  }

  /** insert a device in the database, if not already present
   * @param address  device address
   * @param model    device model
   * @param name     device name
   */
  void insertDevice( String address, String model, String name )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "insert device");
      return;
    }
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
          cv.put( "nickname", TDString.EMPTY );  // DB_NOTE empty nickname
          myDB.insert( DEVICE_TABLE, null, cv );
        }
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } catch (SQLiteException e ) { logError( "insert device", e ); 
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
  }

  // private void insertDeviceHeadTail( String address, String model, int[] head_tail, String name )
  // {
  //   ContentValues cv = new ContentValues();
  //   cv.put( "address", address );
  //   cv.put( "model",   model );
  //   cv.put( "head",    head_tail[0] );
  //   cv.put( "tail",    head_tail[1] );
  //   cv.put( "name",    name );
  //   cv.put( "nickname", TDString.EMPTY );  // FIXME empty nickname
  //   try {
  //     myDB.insert( DEVICE_TABLE, null, cv );
  //   } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
  //   } catch (SQLiteException e ) { logError( "insert device H-T", e ); }
  // }

  /** update a device model
   * @param address   device address
   * @param model     device new model
   */
  void updateDeviceModel( String address, String model )
  {
    ContentValues cv = new ContentValues();
    cv.put( "model", model );
    doUpdate( "devices", cv, WHERE_ADDRESS, new String[] { address }, "model" );

    // if ( updateDeviceModelStmt == null )
    //   updateDeviceModelStmt = myDB.compileStatement( "UPDATE devices set model=? WHERE address=?" );
    // updateDeviceModelStmt.bindString( 1, model );
    // updateDeviceModelStmt.bindString( 2, address );
    // try { updateDeviceModelStmt.execute(); 
    // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    // } catch (SQLiteException e ) { logError( "update device", e ); }
  }

  /** update a device nickname
   * @param address   device address
   * @param nickname  device new nickname
   */
  void updateDeviceNickname( String address, String nickname )
  {
    ContentValues cv = new ContentValues();
    cv.put( "nickname", nickname );
    doUpdate( "devices", cv, WHERE_ADDRESS, new String[] { address }, "nick" );

    // if ( updateDeviceNicknameStmt == null )
    //     updateDeviceNicknameStmt = myDB.compileStatement( "UPDATE devices set nickname=? WHERE address=?" );
    // updateDeviceNicknameStmt.bindString( 1, nickname );
    // updateDeviceNicknameStmt.bindString( 2, address );
    // try { updateDeviceNicknameStmt.execute(); 
    // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    // } catch (SQLiteException e ) { logError( "update device nickname", e ); }
  }

  /** update a device head-tail pair
   * @param address   device address
   * @param head_tail device new head-tail pair
   */
  boolean updateDeviceHeadTail( String address, int[] head_tail )
  {
    boolean ret = false;
    if ( myDB == null ) return ret; // DB_NOTE should not happen
    // if ( updateDeviceHeadTailStmt == null )
    //     updateDeviceHeadTailStmt = myDB.compileStatement( "UPDATE devices set head=?, tail=? WHERE address=?" );
    Cursor cursor = null;
    try {
      cursor = myDB.query( DEVICE_TABLE, new String[] { "head" },
                         "address=?", 
                         new String[] { address },
                         null, null, null );
      if (cursor != null ) {
        if (cursor.moveToFirst() ) {
          // TDLog.v("update Head Tail " + address + " " + head_tail[0] + " " + head_tail[1] );
          ContentValues cv = new ContentValues();
          cv.put( "head", head_tail[0] );
          cv.put( "tail", head_tail[1] );
          ret = doUpdate( "devices", cv, WHERE_ADDRESS, new String[] { address }, "HT" );

          // long head = head_tail[0];
          // long tail = head_tail[1];
          // updateDeviceHeadTailStmt.bindLong( 1, head );
          // updateDeviceHeadTailStmt.bindLong( 2, tail );
          // updateDeviceHeadTailStmt.bindString( 3, address );
          // try { updateDeviceHeadTailStmt.execute();
          // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
          // } catch (SQLiteException e ) { logError( "update device H-T", e ); }
          // ret = true;
        // } else {
        //   insertDeviceHeadTail( address, "DistoX", head_tail, name ); // FIXME name ?
        }
      }
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
    return ret;
  }

  /** @return true if a calibration name is in the database
   * @param name   calibration name
   */
  public boolean hasCalibName( String name )  { return hasName( name, CALIB_TABLE ); }

  /** @return true if a name is in a database table
   * @param name   name
   * @param table  table
   */
  private boolean hasName( String name, String table )
  {
    if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "has name");
      return false;
    }
    boolean ret = false;
    Cursor cursor = null;
    try {
      String query = String.format("SELECT name FROM %s WHERE name='%s' COLLATE NOCASE", table, name );
      cursor = myDB.rawQuery( query, new String[] { } );
      ret = (cursor != null && cursor.moveToFirst() );
    } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
    } finally { if (cursor != null && !cursor.isClosed()) cursor.close(); }
     return ret;
   }

   /** update the calibration info
    * @param id      calibration ID
    * @param date    calibration date
    * @param device  calibration device
    * @param comment calibration description
    */
   void updateCalibInfo( long id, String date, String device, String comment )
   {
     // TDLog.Log( TDLog.LOG_DB, "updateCalibInfo id " + id + " day " + date + " comm. " + comment );
     if ( date == null ) return; // false;
     ContentValues cv = new ContentValues();
     cv.put( "day", date );
     cv.put( "device", device );
     cv.put( "comment", comment );
     doUpdate( "calibs", cv, WHERE_ID, new String[] { Long.toString(id) }, "info" );

     // if ( updateCalibStmt == null )
     //    updateCalibStmt = myDB.compileStatement( "UPDATE calibs SET day=?, device=?, comment=? WHERE id=?" );
     // String dev = (device != null)? device : TDString.EMPTY;
     // String cmt = (comment != null)? comment : TDString.EMPTY;
     // updateCalibStmt.bindString( 1, date );
     // updateCalibStmt.bindString( 2, dev );
     // updateCalibStmt.bindString( 3, cmt );
     // updateCalibStmt.bindLong( 4, id );
     // try {
     //   updateCalibStmt.execute(); 
     // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     // } catch (SQLiteException e ) { logError( "update calib", e ); }
     // // return true;
   }

   /** update the calibration algorithm
    * @param id      calibration ID
    * @param algo    calibration algorithm
    */
   void updateCalibAlgo( long id, long algo )
   {
     ContentValues cv = new ContentValues();
     cv.put( "algo", algo );
     doUpdate( "calibs", cv, WHERE_ID, new String[] { Long.toString(id) }, "algo" );

     // // TDLog.Log( TDLog.LOG_DB, "updateCalibAlgo id " + id + " algo " + algo );
     // if ( updateCalibAlgoStmt == null )
     //    updateCalibAlgoStmt = myDB.compileStatement( "UPDATE calibs SET algo=? WHERE id=?" );
     // updateCalibAlgoStmt.bindLong( 1, algo );
     // updateCalibAlgoStmt.bindLong( 2, id );
     // try {
     //   updateCalibAlgoStmt.execute();
     // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     // } catch (SQLiteException e ) { logError( "update calib algo", e ); }
     // // return true;
   }

   /** update the calibration coefficinets
    * @param id      calibration ID
    * @param coeff   calibration coefficients (as a string)
    */
   void updateCalibCoeff( long id, String coeff )
   {
     ContentValues cv = new ContentValues();
     cv.put( "coeff", coeff );
     doUpdate( "calibs", cv, WHERE_ID, new String[] { Long.toString(id) }, "coeff" );

     // // TDLog.Log( TDLog.LOG_DB, "updateCalibCoeff id " + id + " coeff. " + coeff );
     // if ( coeff == null ) return; // false;
     // if ( updateCalibCoeffStmt == null )
     //    updateCalibCoeffStmt = myDB.compileStatement( "UPDATE calibs SET coeff=? WHERE id=?" );
     // updateCalibCoeffStmt.bindString( 1, coeff );
     // updateCalibCoeffStmt.bindLong( 2, id );
     // try {
     //   updateCalibCoeffStmt.execute();
     // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     // } catch (SQLiteException e ) { logError( "update calib coeff", e ); }
     // // return true;
   }

   /** update the calibration results
    * @param id         calibration ID
    * @param delta_bh   calibration Heeb Delta
    * @param error      calibration data mean error
    * @param stddev     calibration data error std. deviation
    * @param max_error  calibration data maximum error
    * @param iterations calibration iterations
    */
   void updateCalibError( long id, double delta_bh, double error, double stddev, double max_error, int iterations )
   {
     ContentValues cv = new ContentValues();
     cv.put( "delta_bh", delta_bh );
     cv.put( "error", error );
     cv.put( "stddev", stddev );
     cv.put( "max_error", max_error );
     cv.put( "iterations", iterations );
     doUpdate( "calibs", cv, WHERE_ID, new String[] { Long.toString(id) }, "error" );

     // // TDLog.Log( TDLog.LOG_DB, "updateCalibCoeff id " + id + " coeff. " + coeff );
     // if ( updateCalibErrorStmt == null )
     //    updateCalibErrorStmt = myDB.compileStatement( "UPDATE calibs SET error=?, stddev=?, max_error=?, iterations=? WHERE id=?" );
     // updateCalibErrorStmt.bindDouble( 1, error );
     // updateCalibErrorStmt.bindDouble( 2, stddev );
     // updateCalibErrorStmt.bindDouble( 3, max_error );
     // updateCalibErrorStmt.bindLong( 4, iterations );
     // updateCalibErrorStmt.bindLong( 5, id );
     // try {
     //   updateCalibErrorStmt.execute(); 
     // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
     // } catch (SQLiteException e ) { logError( "update calib error", e ); }
     // // return true;
   }

   /** @return the ID of a calibration 
    * @param calib  calibration name
    */
   long setCalib( String calib )
   {
     if ( myDB == null ) {
      TDLog.Error( ERROR_NULL_DB + "set calib");
       return 0L; 
     }
     myNextCId = 0;
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

   /** @return the calibration name from the ID
    * @param cid   calibration ID
    */
   String getCalibFromId( long cid ) { return getNameFromId( CALIB_TABLE, cid ); }

   // ----------------------------------------------------------------------
   // DATABASE TABLES

   private static class DistoXOpenHelper extends SQLiteOpenHelper
   {
      private static final String create_table = "CREATE TABLE IF NOT EXISTS ";

      DistoXOpenHelper(Context context, String database_name ) 
      {
         super(context, database_name, null, TDVersion.DEVICE_DATABASE_VERSION);
         // TDLog.v( "DB NAME " + database_name );
         // TDLog.Log( TDLog.LOG_DB, "createTables ... " + database_name + " version " + TDVersion.DEVICE_DATABASE_VERSION );
      }

      @Override
      public void onCreate(SQLiteDatabase db) 
      {
        createTables( db );
        // TDLog.Log( TDLog.LOG_DB, "DistoXOpenHelper onCreate done db " + db );
      }

      private void createTables(SQLiteDatabase db )
      {
         // db.setLockingEnabled( false );
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
             +   " stddev REAL default 0, "
             +   " delta_bh REAL default 0 "
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
           // db.setLockingEnabled( true );
         }
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {  
         // FIXME this is called at each start when the database file exists
         TDLog.Log( TDLog.LOG_DB, "onUpgrade old " + oldVersion + " new " + newVersion );
         switch ( oldVersion ) {
           case 14: 
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
           case 23:
             db.execSQL( "ALTER TABLE devices ADD COLUMN nickname TEXT default \"\"" );
           case 24:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN stddev REAL default 0" );
           case 25:
           case 26:
             db.execSQL( "ALTER TABLE calibs ADD COLUMN delta_bh REAL default 0" );
           case 27:
             /* current version */
           default:
             break;
         }
      }
   }
}
