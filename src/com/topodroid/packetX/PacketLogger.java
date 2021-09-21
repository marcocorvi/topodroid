/* @file PacketLogger.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid SQLite database manager for packet logging
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 *  Note: variables
 *     String[] vals for String.split
 *     ContentValues cv
 */
package com.topodroid.packetX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDVersion;
import com.topodroid.Cave3X.TDPath;

// import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.IOException;
// import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.DataSetObservable;
// import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
// import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDiskIOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
// import java.util.HashMap;


public class PacketLogger extends DataSetObservable
{
  // static final String PACKET_DB_VERSION = "1";
  // static final int PACKET_DATABASE_VERSION = 1;
  // static final int PACKET_DATABASE_VERSION_MIN = 1;

  // private static final String CONFIG_TABLE = "configs";
  private static final String PACKET_TABLE = "packets";

  // private SQLiteStatement updateConfig = null;

  private SQLiteDatabase myDB = null;

  // ----------------------------------------------------------------------
  // DATABASE

  private final Context mContext;
  // private final TopoDroidApp mApp; // unused

  public SQLiteDatabase getDb() { return myDB; }

  public PacketLogger( Context context /* , TopoDroidApp app */ )
  {
    mContext = context;
    // mApp     = app;
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
    String database_name = TDFile.getPacketDatabase().getAbsolutePath();
    DistoXOpenHelper openHelper = new DistoXOpenHelper( mContext, database_name );

    try {
        myDB = openHelper.getWritableDatabase();
        if ( myDB == null ) {
          TDLog.Error( "failed get writable packet database" );
          // return;
        }

        // while ( myDB.isDbLockedByOtherThreads() ) {
        //   TDUtil.slowDown( 200 );
        // }

     } catch ( SQLiteException e ) {
       myDB = null;
       logError( "Packet Logger cstr failed to get DB", e );
     }
   }

   // ----------------------------------------------------------------------

  private ContentValues makePacketContentValues( long millis, long dir, String addr, int type, String data )
  {
    ContentValues cv = new ContentValues();
    cv.put( "millis",   millis );
    cv.put( "dir",      dir );
    cv.put( "address",  addr );
    cv.put( "type",     type );
    cv.put( "data",     data );
    return cv;
  }

  private void logError( String msg, Exception e )
  {
    TDLog.Error("PACKET DB " + msg + ": " + e.getMessage() );
  }

  private void handleDiskIOError( SQLiteDiskIOException e )
  {
    TDLog.Error("PACKET DB disk error " + e.getMessage() );
  }

  private long doInsert( String table, ContentValues cv )
  {
    long ret = 0L;
    try { 
      myDB.beginTransaction();
      myDB.insert( table, null, cv ); 
      myDB.setTransactionSuccessful();
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e ); ret = -11L;
    } catch ( SQLiteException e1 )       { logError("insert", e1 );     ret = -12L;
    } catch ( IllegalStateException e2 ) { logError("insert", e2 );     ret = -13L;
    } finally { myDB.endTransaction(); }
    return ret;
  }

  private boolean doExecSQL( StringWriter sw )
  {
    boolean ret = false;
    try {
      myDB.beginTransaction();
      myDB.execSQL( sw.toString() );
      myDB.setTransactionSuccessful();
      ret = true;
    } catch ( SQLiteDiskIOException e )  { handleDiskIOError( e );
    } catch ( SQLiteException e1 )       { logError("delete", e1 );
    // } catch ( IllegalStateException e2 ) { logError("delete", e2 );
    } finally { myDB.endTransaction(); }
    return ret;
  }

  PacketData fillPacketData( Cursor cursor ) 
  {
    return new PacketData( cursor.getLong(0), 
                           cursor.getLong(1),
                           cursor.getString(2),
                           (int)(cursor.getLong(3)),
                           cursor.getString(4) );
  }

  // ------------------------------------------------------------------------

  private void clearOldest( long secs )
  {
    long time = System.currentTimeMillis() - (1000L * secs);
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format( Locale.US, "DELETE FROM packets WHERE millis < %d", time );
    doExecSQL( sw );
  }

  void clearAll( ) { clearOldest( 0L ); }
  void clearDayOlder( ) { clearOldest( 24L * 3600L ); }
  void clearWeekOlder( ) { clearOldest( 7L * 24L * 3600L ); }

  // @param millis packet time
  // @param dir    packet direction
  // @param addr   packet address
  // @param data   packet data
  public long insertPacket( long millis, long dir, String addr, int type, String data )
  {
    // TDLog.Log( TDLog.LOG_DB, "...) );
    // TDLog.v( "...");
    if ( myDB == null ) return -1L;
    if ( data == null ) return -2L;
    if (addr == null) addr = "";
    ContentValues cv = makePacketContentValues( millis, dir, addr, type, data );
    return doInsert( PACKET_TABLE, cv );
  }

  // @param secs    max packet age
  List< PacketData > selectPackets( long secs ) 
  {
    ArrayList< PacketData > ret = new ArrayList<>();
    long time = System.currentTimeMillis() - (1000 * secs);
    Cursor cursor = myDB.query( PACKET_TABLE,
       		         new String[] { "millis", "dir", "address", "type", "data" }, // columns
                                "millis > ?",
                                new String[] { Long.toString(time) },
                                null, null, "millis DESC" );
    if ( cursor.moveToFirst() ) {
      do { ret.add( fillPacketData( cursor ) ); } while ( cursor.moveToNext() );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  // @param secs    max packet age
  // @param filter  1 zero type
  //                2 D
  //                4 G
  //                8 M
  //               16 V
  //               32 C
  //               64 X
  List< PacketData > selectPackets( long secs, int filter )
  {
    ArrayList< PacketData > ret = new ArrayList<>();
    long time = System.currentTimeMillis() - (1000 * secs);
    Cursor cursor = myDB.query( PACKET_TABLE,
       		         new String[] { "millis", "dir", "address", "type", "data" }, // columns
                                "millis > ?",
                                new String[] { Long.toString(time) },
                                null, null, "millis DESC" );
    if ( cursor.moveToFirst() ) {
      do { 
        if ( PacketData.checkType( (int)(cursor.getLong(3)), filter ) ) {
          ret.add( fillPacketData( cursor ) ); 
        }
      } while ( cursor.moveToNext() );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }

  // @param secs    max packet age
  // @param addr    packet address
  List< PacketData > selectPackets( long secs, String addr )
  {
    ArrayList< PacketData > ret = new ArrayList<>();
    long time = System.currentTimeMillis() - (1000 * secs);
    Cursor cursor = myDB.query( PACKET_TABLE,
       		         new String[] { "millis", "dir", "address", "type", "data" }, // columns
                                "millis > ? AND address = ?",
                                new String[] { Long.toString(time), addr },
                                null, null, "millis DESC" );
    if ( cursor.moveToFirst() ) {
      do { ret.add( fillPacketData( cursor ) ); } while ( cursor.moveToNext() );
    }
    if ( /* cursor != null && */ !cursor.isClosed()) cursor.close();
    return ret;
  }


   // ----------------------------------------------------------------------
   // DATABASE TABLES


   private static class DistoXOpenHelper extends SQLiteOpenHelper
   {
      private static final String create_table = "CREATE TABLE IF NOT EXISTS ";

      DistoXOpenHelper(Context context, String database_name ) 
      {
         super(context, database_name, null, TDVersion.PACKET_DATABASE_VERSION);
         // TDLog.v( "PACKET DB NAME " + database_name );
         // TDLog.Log( TDLog.LOG_DB, "createTables ... " + database_name + " version " + TDVersion.PACKET_DATABASE_VERSION );
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
           // db.setLockingEnabled( false );
           db.beginTransaction();

           // db.execSQL( 
           //     create_table + CONFIG_TABLE
           //   + " ( key TEXT NOT NULL,"
           //   +   " value TEXT )"
           // );

           // db.execSQL("DROP TABLE IF EXISTS " + SHOT_TABLE);
           // db.execSQL("DROP TABLE IF EXISTS " + SURVEY_TABLE);
           db.execSQL(
               create_table + PACKET_TABLE 
             + " ( millis  INTEGER, "
             +   " dir     INTEGER, "  // 0 in (DistoX -> TopoDroid), 1 out (TopoDroid -> DistoX)
             +   " address TEXT, "
             +   " type    INTEGER, "  // first byte of data
             +   " data    TEXT "
             +   ")"
           );

           db.setTransactionSuccessful();
           db.endTransaction();
         // } catch ( SQLiteDiskIOException e ) { handleDiskIOError( e );
         } catch ( SQLException e ) { TDLog.Error( "PACKET DB createTables exception: " + e.getMessage() );
         // } finally {
           // db.setLockingEnabled( true );
         }
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {  
         // FIXME this is called at each start when the database file exists
         // TDLog.Log( TDLog.LOG_DB, "onUpgrade old " + oldVersion + " new " + newVersion );
         switch ( oldVersion ) {
	   case 1:
             /* current version */
           default:
             break;
         }
      }
   }
}
