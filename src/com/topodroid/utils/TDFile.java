/** @file TDFile.java
 *
 * @author marco corvi
 * @date apr 2021
 *
 * @brief TopoDroid File layer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TDPath;

import android.os.ParcelFileDescriptor;
// import android.app.Application;
import android.app.Activity;
import android.content.Context;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.database.Cursor;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import java.util.ArrayList;
import android.provider.DocumentsContract;
import android.net.Uri;

import android.provider.MediaStore;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
// import java.io.FileFilter;
// import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.charset.Charset;

import android.util.Log;

public class TDFile
{
  // public static final String HOME_DIR = TDPath.getPathBase(); // "Documents/TopoDroid/"

  public final static Object mFilesLock = new Object();

  // OLD FILE FUNCTIONS -----------------------------------------------------------------------------

  public static File getManDir( )                 { return new File( TDInstance.context.getFilesDir(), "man" ); }
  public static File getManFile( String name )    { return new File( getManDir(), name ); }
  public static boolean hasManFile( String name ) { return getManFile( name ).exists(); }
  public static FileReader getManFileReader( String name ) throws IOException { return new FileReader( getManFile(name) ); }
 
  public static boolean hasFile( String name ) { return name != null && (new File( name )).exists(); }

  public static long getFileLength( String name ) { return (name == null)? 0 : (new File(name)).length(); }

  // @param name     TopoDroid-relative filename
  public static File getTopoDroidFile( String name ) { return new File( name ); }

  public static File getTopoDroidFile( String dirname, String name ) { return new File( dirname, name ); }

  // INTERNAL FILES --------------------------------------------------------------
  // context.getFilesDir --> /data/user/0/com.topodroid.DistoX/files

  // APP-SPECIFIC EXTERNAL FILES --------------------------------------------------------------

  public static File getSettingsFile()   { return new File( TDInstance.context.getExternalFilesDir( null ), "settings.txt" ); }
  public static File getLogFile()        { return new File( TDInstance.context.getExternalFilesDir( null ), "log.txt" ); }
  public static File getDeviceDatabase() { return new File( TDInstance.context.getExternalFilesDir( null ), "device10.sqlite" ); }
  public static File getPacketDatabase() { return new File( TDInstance.context.getExternalFilesDir( null ), "packet10.sqlite" ); }

  public static File getExternalDir( String type ) { return TDInstance.context.getExternalFilesDir( type ); }

  public static File getExternalFile( String type, String name ) { return new File ( TDInstance.context.getExternalFilesDir( type ), name ); }

  public static void deleteExternalFile( String type, String name ) 
  {
    File file = new File ( TDInstance.context.getExternalFilesDir( type ), name );
    if ( file.exists() ) {
      file.delete();
    }
  }

  public static FileWriter getExternalFileWriter( String type, String name ) throws IOException
  {
    File file = getExternalFile( type, name );
    return new FileWriter( file );
  }

  // TEMPORARY FILES - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  public static File getExternalTempFile( String name ) { return getExternalFile( "tmp", name ); }

  public static void clearExternalTempDir( long before )
  {
    long now  = System.currentTimeMillis();
    long time = now - before; // clean the cache "before" minutes before now
    File dir = getExternalDir( "tmp" );
    File[] files = dir.listFiles();
    if ( files != null ) for ( File f : files ) {
      if ( f.lastModified() < time ) {
        if ( ! f.delete() ) TDLog.Error("File delete error: " + f.getPath() );
      }
    }
  }

  // ----------------------------------------------------------------------------

  // @param name     absolute filename
  // used by FixedImportDialog
  public static File getGpsPointFile( String pathname ) { return new File( pathname ); }

  public static FileReader getGpsPointFileReader( String dirname, String filename ) throws IOException
  {
    File file = new File( dirname, filename );
    return new FileReader( file );
  }

  // public static File getFile( File dir, String name )
  // {
  //   return new File( dir, name );
  // }

  public static FileInputStream getFileInputStream( String name ) throws IOException { return new FileInputStream( name ); }

  // public static FileInputStream getFileInputStream( File file ) throws IOException { return new FileInputStream( file ); }

  public static FileOutputStream getFileOutputStream( String name ) throws IOException { return new FileOutputStream( name ); }

  public static FileOutputStream getFileOutputStream( File file ) throws IOException { return new FileOutputStream( file ); }

  public static FileWriter getFileWriter( String name, boolean append ) throws IOException { return new FileWriter( name, append ); }

  public static FileWriter getFileWriter( String name ) throws IOException { return new FileWriter( name ); }

  public static FileWriter getFileWriter( File file ) throws IOException { return new FileWriter( file ); }

  public static FileReader getFileReader( String name ) throws IOException { return new FileReader( name ); }

  public static FileReader getFileReader( File file ) throws IOException { return new FileReader( file ); }

  // -----------------------------------------------------------------------------
  public static void deleteFile( File f ) 
  {
    if ( f != null && f.exists() ) {
      if ( ! f.delete() ) TDLog.Error("file delete failed " + f.getName() );
    }
  }

  public static void deleteDir( File dir ) 
  {
    if ( dir != null && dir.exists() ) {
      File[] files = dir.listFiles();
      if ( files != null ) {
        for ( File file : files ) {
          if (file.isFile()) {
            if ( ! file.delete() ) TDLog.Error("file delete failed " + file.getName() ); 
          }
        }
      }
      if ( ! dir.delete() ) TDLog.Error("dir delete failed " + dir.getName() );
    }
  }

  // public static void clearAppCache()
  // {
  //   try {
  //     File cache = TDInstance.context.getCacheDir();
  //     recursiveDeleteDir( cache );
  //   } catch ( Exception e ) {
  //     // TODO
  //   }
  // }

  public static boolean recursiveDeleteDir( File dir )
  {
    if ( dir == null ) return false;
    if ( dir.isFile() ) return dir.delete();
    if ( dir.isDirectory() ) {
      String[] children = dir.list();
      for ( int i=0; i < children.length; ++i ) {
        if ( ! recursiveDeleteDir( new File( dir, children[i] ) ) ) return false;
      }
      return dir.delete();
    }
    return false;
  }

  public static void deleteFile( String pathname ) 
  { 
    deleteFile( getTopoDroidFile( pathname ) ); // DistoXFile;
  }

  public static void deleteDir( String dirname ) 
  { 
    deleteDir( getTopoDroidFile( dirname ) ); // DistoX-SAF
  }

  // @pre oldname exists && ! newname exists
  public static void renameFile( String oldname, String newname )
  {
    File f1 = getTopoDroidFile( oldname ); // DistoX-SAF
    File f2 = getTopoDroidFile( newname );
    if ( f1.exists() && ! f2.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.Error("file rename: failed " + oldname + " to " + newname );
    } else {
      TDLog.Error("file rename: no-exist " + oldname + " or exist " + newname );
    }
  }

  // @pre oldname exists
  public static void moveFile( String oldname, String newname )
  {
    File f1 = getTopoDroidFile( oldname ); // DistoX-SAF
    File f2 = getTopoDroidFile( newname );
    if ( f1.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.Error("file move: failed " + oldname + " to " + newname );
    } else {
      TDLog.Error("file move: no-exist " + oldname );
    }
  }

  public static File makeDir( String pathname )
  {
    File f = getTopoDroidFile( pathname );
    if ( ! f.exists() ) {
      if ( ! f.mkdirs() ) {
        TDLog.Error("mkdir failed " + pathname );
        return null;
      }
    }
    return f;
  }

  public static File makeExternalDir( String type )
  {
    File f = getExternalDir( type ); 
    if ( ! f.exists() ) {
      if ( ! f.mkdirs() ) {
        TDLog.Error("mkdir external failed " + type );
        return null;
      }
    }
    return f;
  }


  public static boolean renameTempFile( File temp, File file )
  {
    boolean ret = false;
    // Log.v("DistoX", "rename " + temp.getPath() + " to " + file.getPath() );
    synchronized( mFilesLock ) {
      if ( file.exists() ) file.delete();
      ret = temp.renameTo( file );
    }
    return ret;
  }

  public static boolean renameTempFile( File temp, String pathname )
  { 
    return renameTempFile( temp, getTopoDroidFile( pathname ) );
  }

  // =========================================================================
  // GENERIC INTERFACE

  public static File getMSfile( String name ) { return new File( TDPath.getPathBase() + name ); }

  public static File getMSfile( String subdir, String name ) { return new File( TDPath.getPathBase() + subdir + "/" + name ); }

  public static long getMSFileLength( String subdir, String name ) 
  { 
    if ( name == null ) return 0;
    File file = getMSfile( subdir, name );
    return (file == null)? 0 : file.length();
  }

  public static boolean hasMSdir( String subdir )
  {
    return getMSfile( subdir ).exists();
  }

  public static boolean hasMSfile( String subdir, String name )
  {
    File dir = getMSfile( subdir );
    if ( ! dir.exists() ) return false;
    File file = new File( dir, name );
    return file.exists();
  }

  // public static boolean hasMSpath( String pathname )
  // {
  //   return (new File(pathname)).exists();
  // }

  public static boolean makeMSdir( String subdir )
  {
    File dir = getMSfile( subdir );
    if ( dir.exists() ) return true;
    return dir.mkdirs();
  }

  public static void deleteMSdir( String subdir )
  {
    deleteDir( getMSfile( subdir ) );
  }

  static public OutputStream getMSoutput( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! makeMSdir( subdir ) ) {
      TDLog.Error("failed to create subdir " + subdir );
      throw new IOException("failed to create subdir");
    }
    return new FileOutputStream( getMSfile( subdir, filename ) );
  }

  // @note the returnet OutputStreamWriter must be closed after it has been written
  static public BufferedWriter getMSwriter( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! makeMSdir( subdir ) ) {
      TDLog.Error("failed to create subdir " + subdir );
      throw new IOException("failed to create subdir");
    }
    OutputStream os = new FileOutputStream( getMSfile( subdir, filename ) );
    if ( os == null ) {
      TDLog.Error("failed to create output stream " + filename );
      throw new IOException( "failed to create file output stream ");
    }
    return new BufferedWriter( new OutputStreamWriter( os ) );
  }

  static public InputStream getMSinput( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! hasMSdir( subdir ) ) {
      TDLog.Error("failed: no subdir " + subdir );
      throw new IOException("failed: no subdir");
    }
    return new FileInputStream( getMSfile( subdir, filename ) );
  }

  // get a reader for the InputStream
  // then we can read  
  static public BufferedReader getMSReader( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! hasMSdir( subdir ) ) {
      TDLog.Error("failed: no subdir " + subdir );
      throw new IOException("failed: no subdir");
    }
    InputStream is = new FileInputStream( getMSfile( subdir, filename ) );
    if ( is == null ) {
      TDLog.Error("failed to create input stream " + filename );
      throw new IOException( "failed to create file input stream ");
    }
    return new BufferedReader( new InputStreamReader( is, Charset.forName( "UTF-8" ) ) );
  }
 
  /* =============================================================================
  // TDMediaStore functions
  //
  // static public boolean isMSexists( String subdir, String filename )
  //
  // @note the returned OutputStream must be closed after it has been written
  // static public OutputStream getMSoutput( String subdir, String filename, String mimetype )
  //
  // static public InputStream getMSinput( String subdir, String filename, String mimetype )
  //
  // NOTE listing returns only items inserted with MediaStore
  // @param subdir   topodroid subdirectory
  // @param filter   filename filter
  // static public ArrayList<String> getMSfilelist( String subdir )
  // 
  // static public void osWriteString( OutputStream os, String str ) throws IOException
  //
  */

  // -------------------------------------------------------------------------------

  // get the file descriptor.
  // After use must call close() on the file desriptor
  static ParcelFileDescriptor getFileDescriptor( Uri uri, String mode )
  {
    try {
      return TDInstance.getContentResolver().openFileDescriptor( uri, mode );
    } catch ( FileNotFoundException e ) {
      return null;
    }
  }

  static InputStream getInputStream( Uri uri ) 
  {
    try {
      return TDInstance.getContentResolver().openInputStream( uri );
    } catch ( FileNotFoundException e ) {
      return null;
    }
  }

  static OutputStream getOutputStream( Uri uri ) 
  {
    try {
      return TDInstance.getContentResolver().openOutputStream( uri );
    } catch ( FileNotFoundException e ) {
      return null;
    }
  }

} 
