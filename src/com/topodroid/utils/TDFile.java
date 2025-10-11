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

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDUtil;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDandroid;

import android.os.ParcelFileDescriptor;
import android.os.Environment;
// import android.os.Build;

// import android.app.Application;
// import android.app.Activity;

// import android.content.Context;
// import android.content.ContentValues;
// import android.content.ContentResolver;
// import android.content.Intent;
// import android.content.IntentFilter;
// import android.content.ContentUris;
// import android.content.res.Resources;
// import android.content.res.Configuration;
// import android.content.SharedPreferences;
// import android.preference.PreferenceManager;

// import android.database.Cursor;

// import android.provider.DocumentsContract;
import android.net.Uri;

// import android.provider.MediaStore;

// import androidx.documentfile.provider.DocumentFile;

// import java.util.ArrayList;

import java.io.File;
// import java.io.FileDescriptor;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
// import java.io.FileFilter;
// import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.nio.charset.Charset;

public class TDFile
{
  // public static final String HOME_DIR = TDPath.getPathBase(); // "Documents/TopoDroid/"

  public final static Object mFilesLock = new Object();

  // OLD FILE FUNCTIONS -----------------------------------------------------------------------------

  /** @return the man-dir file
   */
  private static File getManDir( )                 { return TDInstance.context.getExternalFilesDir( "man" ); }

  /** @return man-file
   * @param name   man-file name
   */
  private static File getManFile( String name )    { return new File( getManDir(), name ); }

  /** @return man-file path
   * @param name   man-file name
   */
  public static String getManFilePath( String name )
  { 
    File file = new File( getManDir(), name );
    return (file == null)? "" : file.getPath();
  }

  /** ensure that man-dir exists and is writable
   * @return true if success, false if man-dir does not exist
   */
  public static boolean checkManDir( )
  { 
    File dir = TDInstance.context.getExternalFilesDir( "man" );
    return ( dir.exists() || dir.mkdirs() ) && dir.canWrite();
  }

  /** @return man-file output stream
   * @param name   man-file name
   */
  public static FileOutputStream getManFileOutputStream( String name ) throws IOException
  {
    return new FileOutputStream( getManFile( name ) );
  }

  /** @return true if a man-file exists
   * @param name   man-file name
   */
  public static boolean hasManFile( String name ) { return getManFile( name ).exists(); }

  /** @return man-file reader
   * @param name   man-file name
   */
  public static FileReader getManFileReader( String name ) throws IOException
  {
    return new FileReader( getManFile(name) );
  }
 
  // private static File getC3exportDir( )                { return TDInstance.context.getExternalFilesDir( "c3export" ); }
  // private static File getC3exportFile( String name )   { return new File( TDInstance.context.getExternalFilesDir( "c3export" ),  name ); }
  // private static String getC3exportPath( )             { return getC3exportDir().getPath(); }
  // public  static String getC3exportPath( String name ) { return getC3exportDir( ).getPath(); }
  // private static String getC3exportPath( String name, String ext ) { return getC3exportFile( name + "." + ext ).getPath(); }

  /** @return true if the given file exists 
   * @param name   file full pathname
   */
  public static boolean hasTopoDroidFile( String name )
  { 
    // TDLog.v("FILE has TD file " + name );
    return name != null && (new File( name )).exists();
  }

  /** @return true if the given file exists 
   * @param dirpath   filename folder
   * @param name      filename
   */
  public static boolean hasTopoDroidFile( String dirpath, String name )
  { 
    // TDLog.v("FILE has TD file " + dirpath + " " + name );
    return name != null && (new File( dirpath + "/" + name )).exists();
  }

  /** @return length of the given file (negative if name is null, 0 if the file does not exist)
   * @param name   filename
   */
  public static long getTopoDroidFileLength( String name )
  { 
    if ( name == null ) return -2L;
    // TDLog.v("FILE get TD file length " + name );
    File file = new File(name);
    if ( ! file.exists() ) return 0L;
    return file.length();
  }

  /** @return file age and length
   * @param name   filename
   * @param millis reference time [msec]
   * @note for tdr plot recovery
   */
  public static String getTopoDroidFileAgeLength( String name, long millis )
  { 
    if ( name == null ) return null;
    File file = new File(name);
    if ( ! file.exists() ) return null;
    return TDUtil.getAge( millis - file.lastModified() ) + " [" + file.length() + "]";
  }

  /** @return a File for the pathname
   * @param name   full pathname
   */
  public static File getTopoDroidFile( String name )
  {
    // TDLog.v("FILE get TD file " + name );
    return new File( name );
  }

  /** @return a File for the full file pathname
   * @param dirname full directory pathname
   * @param name    file name
   */
  public static File getTopoDroidFile( String dirname, String name )
  { 
    // TDLog.v("FILE get TD file " + dirname + " " + name );
    return new File( dirname, name );
  }

  /** @return the input-stream for the pathname
   * @param name   full pathname
   */
  public static DataInputStream getTopoDroidFileInputStream( String name ) throws IOException
  { 
    // TDLog.v("FILE get TD file I stream " + name );
    File file = new File( name ); 
    if ( ! file.exists() ) {
      TDLog.e("no file " + name );
      return null;
    }
    if ( ! file.canRead() ) {
      TDLog.e("cannot read file " + name );
      return null;
    }
    try {
      FileInputStream fis = new FileInputStream( file );
      BufferedInputStream bfis = new BufferedInputStream( fis );
      return new DataInputStream( bfis );
    } catch ( FileNotFoundException e ) {
      TDLog.e("file not found " + name );
    }
    return null;
  }

  /** @return the output-stream for the pathname
   * @param name   full pathname
   */
  public static DataOutputStream getTopoDroidFileOutputStream( String name ) throws IOException
  { 
    // TDLog.v("FILE get TD file O stream " + name );
    File file = new File( name ); 
    if ( file.exists() ) {
      if ( ! file.canWrite() ) {
        TDLog.e("file exists and cannot write file " + name );
        return null;
      }
    }
    try {
      FileOutputStream fos = new FileOutputStream( file );
      BufferedOutputStream bfos = new BufferedOutputStream( fos );
      return new DataOutputStream( bfos );
    } catch ( FileNotFoundException e ) {
      TDLog.e("file not found " + name );
    }
    return null;
  }

  /** @return TopoDroid file buffered reader (null on failure)
   * @param name   file full pathname
   */
  public static BufferedReader getTopoDroidFileReader( String name ) throws IOException
  {
    // TDLog.v("FILE get TD file reader " + name );
    File file = new File( name ); 
    if ( ! file.exists() || ! file.canRead() ) {
      TDLog.e("file does not exist or cannot read file " + name );
      return null;
    }
    try {
      FileReader fr = new FileReader( file );
      return new BufferedReader( fr );
    } catch ( FileNotFoundException e ) {
      TDLog.e("file not found " + name );
    }
    return null;
  }

  /** @return TopoDroid file buffered writer (null on failure)
   * @param name   file full pathname
   */
  public static BufferedWriter getTopoDroidFileWriter( String name ) throws IOException
  {
    // TDLog.v("FILE get TD file writer " + name );
    File file = new File( name ); 
    if ( file.exists() ) {
      if ( ! file.canWrite() ) {
        TDLog.e("file exists and cannot write file " + name );
        return null;
      }
    }
    try {
      FileWriter fr = new FileWriter( file );
      return new BufferedWriter( fr );
    } catch ( FileNotFoundException e ) {
      TDLog.e("file not found " + name );
    }
    return null;
  }
    

  // INTERNAL FILES --------------------------------------------------------------
  // context.getFilesDir --> /data/user/0/com.topodroid.TDX/files
  // context.getExternalFilesDir --> /storage/emulated/0/Android/data/com.topodroid.TDX/files

  /** @return the file of the private base folder
   */
  public static File getPrivateBaseDir( ) 
  {
    return TDInstance.context.getExternalFilesDir( null );
  }

  /** @return the file of the specified folder
   * @param type   folder name (in the private app files)
   */
  public static File getPrivateDir( String type ) 
  { 
    return TDInstance.context.getExternalFilesDir( type );
  }

  /** @return a private file
   * @param type   folder name (in the private app files)
   * @param name   filename
   */
  public static File getPrivateFile( String type, String name ) 
  { 
    return new File( TDInstance.context.getExternalFilesDir( type ), name );
  }

  /** @return true if the private file exists
   * @param type   folder name (in the private app files)
   * @param name   filename
   */
  public static boolean existPrivateFile( String type, String name )
  {
    File file = new File( TDInstance.context.getExternalFilesDir( type ), name );
    return file.exists();
  }

  /** @return private file output stream
   * @param type   folder name (in the private app files)
   * @param name   filename
   */
  public static FileOutputStream getPrivateFileOutputStream( String type, String name ) throws IOException
  {
    File file = new File( TDInstance.context.getExternalFilesDir( type ), name );
    return getFileOutputStream( file );
  }

  /** @return private file output writer
   * @param type   folder name (in the private app files)
   * @param name   filename
   */
  public static FileWriter getPrivateFileWriter( String type, String name ) throws IOException
  {
    File file = new File( TDInstance.context.getExternalFilesDir( type ), name );
    return getFileWriter( file );
  }

  /** @return private file input stream
   * @param type   folder name (in the private app files)
   * @param name   filename
   */
  public static FileInputStream getPrivateFileInputStream( String type, String name ) throws IOException
  {
    File file = new File( TDInstance.context.getExternalFilesDir( type ), name );
    return file.exists() ? getFileInputStream( file ) : null;
  }

  /** delete a private file
   * @param type   folder name (in the private app files)
   * @param name   filename
   */
  public static void deletePrivateFile( String type, String name ) 
  {
    File file = new File( TDInstance.context.getExternalFilesDir( type ), name );
    if ( file.exists() ) file.delete();
  }

  /** @return the device database file, in the private folder
   */
  public static File getDeviceDatabase() { return getPrivateFile( null, "device10.sqlite" ); }

  /** @return the packet database file, in the private folder
   */
  public static File getPacketDatabase() { return getPrivateFile( null, "packet10.sqlite" ); }

  /** @return the settings file, in the private folder
   */
  public static File getSettingsFile()   { return getPrivateFile( null, "settings.txt" ); }

  /** @return the log file, in the private folder
   */
  public static File getLogFile()        { return getPrivateFile( null, "log.txt" ); }

  // APP-SPECIFIC EXTERNAL FILES --------------------------------------------------------------

  /** @return the current base directory
   * @param type ...
   * @param create whether to create the directory if it does not exist
   */
  private static File getCBD( String type, boolean create )
  {
    File ret = null;
    if ( TDandroid.PRIVATE_STORAGE ) { // FIXME PRIVATE_STORAGE
      // TDLog.v("getCBD " + type + " use private dir ");
      ret = getPrivateDir( type ); // FIXME do i need to create ?
    } else {
      String documents = ( TDandroid.BELOW_API_19 )? "Documents" : Environment.DIRECTORY_DOCUMENTS;
      if ( type == null ) {
        ret = new File( Environment.getExternalStoragePublicDirectory( documents ), "TDX" );
      } else {
        ret = new File( Environment.getExternalStoragePublicDirectory( documents ), "TDX/" + type );
      } 
    }
    if ( create && ret != null && ! ret.exists() ) {
      // TDLog.v( "mkdirs " + ret.getAbsolutePath() + " type: " + ((type == null)? "null" : type) + " create: " + create );
      ret.mkdirs();
    }
    return ret;
    //
  }

  /** @return true if the directory exists and is read-writable
   * @param type folder name
   */
  public static boolean hasExternalDir( String type ) 
  {
    File ret = getCBD( type, false );
    return ret != null && ret.exists() && ret.canRead() && ret.canWrite();
  }

  /** @return an external folder, in the current work directory
   * @param type   folder name
   */
  public static File getExternalDir( String type ) { return getCBD( type, true ); }

  /** @return an external file, under the current work directory
   * @param type   subfolder name (null for base folder TDX)
   * @param name   file name
   */
  public static File getExternalFile( String type, String name ) 
  { 
    if ( name == null ) return null;
    return new File( getCBD( type, true ), name );
  }

  // PRIVATE_STORAGE : these two has getPath() instead of getAbsolutePath()
  // public static String getExternalPath( String type ) { return getCBD( type, false ).getAbsolutePath(); }
  // public static String getExternalPath( String type, String name ) { return new File( getCBD( type, false ), name ).getAbsolutePath(); }

  // public static boolean hasExternalDir( String type ) { return getCBD( type, false ).exists(); }
  // public static boolean hasExternalFile( String type, String name ) { return new File( getCBD( type, false ), name ).exists(); }

  /** delete an external file, under the current work directory
   * @param type   folder name
   * @param name   file name
   */
  public static void deleteExternalFile( String type, String name ) 
  {
    if ( name == null ) return;
    File file = getExternalFile( type, name );
    if ( file.exists() ) file.delete();
  }

  /** @return writer to an external file writer
   * @param type   folder name
   * @param name   file name
   */
  public static FileWriter getExternalFileWriter( String type, String name ) throws IOException
  {
    if ( name == null ) return null;
    return new FileWriter( getExternalFile( type, name ) );
  }

  /** @return an external file output stream
   * @param type   folder name
   * @param name   file name
   */
  public static DataOutputStream getExternalFileOutputStream( String type, String name ) throws IOException
  {
    if ( name == null ) return null;
    try {
      File file =  getExternalFile( type, name );
      FileOutputStream fos = new FileOutputStream( file );
      BufferedOutputStream bos = new BufferedOutputStream( fos );
      return new DataOutputStream( bos );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    }
    return null;
  }

  // TEMPORARY FILES - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  /** get an external temporary file, under the current work directory
   * @param name   file name
   * @return temporary file
   */
  public static File getExternalTempFile( String name ) { return getExternalFile( "tmp", name ); }

  /** clear stale files in the external temporary folder, in the current work directory
   * @param before  timestamp
   */
  public static void clearExternalTempDir( long before )
  {
    long now  = System.currentTimeMillis();
    long time = now - before; // clean the cache "before" minutes before now
    File dir = getExternalDir( "tmp" );
    File[] files = dir.listFiles();
    if ( files != null ) for ( File f : files ) {
      if ( f.lastModified() < time ) {
        if ( ! f.delete() ) TDLog.e("File delete error: " + f.getAbsolutePath() ); // was getPath()
      }
    }
  }

  /** @return an external temporary file output stream
   * @param name   file name
   */
  public static DataOutputStream getExternalTempFileOutputStream( String name ) throws IOException
  {
    try {
      File file =  getExternalTempFile( name );
      FileOutputStream fos = new FileOutputStream( file );
      BufferedOutputStream bos = new BufferedOutputStream( fos );
      return new DataOutputStream( bos );
    } catch ( FileNotFoundException e ) {
      e.printStackTrace();
    }
    return null;
  }

  // ----------------------------------------------------------------------------

/*
  // @param name     absolute filename
  // used by FixedImportDialog
  public static File getGpsPointFile( String pathname ) { return new File( pathname ); }

  public static FileReader getGpsPointFileReader( String dirname, String filename ) throws IOException
  {
    File file = new File( dirname, filename );
    return new FileReader( file );
  }
*/

  // public static File getFile( File dir, String name )
  // {
  //   return new File( dir, name );
  // }

  public static File getFile( String name ) { return new File( name ); }

  /** @return file input stream
   * @param name  file fullpath
   */
  public static FileInputStream getFileInputStream( String name ) throws IOException { return new FileInputStream( name ); }

  /** @return file input stream
   * @param file  file
   */
  public static FileInputStream getFileInputStream( File file ) throws IOException { return new FileInputStream( file ); }

  /** @return file output stream
   * @param name  file fullpath
   */
  public static FileOutputStream getFileOutputStream( String name ) throws IOException { return new FileOutputStream( name ); }

  /** @return file output stream
   * @param file  file
   */
  public static FileOutputStream getFileOutputStream( File file ) throws IOException { return new FileOutputStream( file ); }

  /** @return a file writer
   * @param name    file fullpath
   * @param append  whether to open the file in append mode
   */
  public static FileWriter getFileWriter( String name, boolean append ) throws IOException { return new FileWriter( name, append ); }

  /** @return file writer
   * @param name    file fullpath
   */
  public static FileWriter getFileWriter( String name ) throws IOException 
  { 
    // TDLog.v("get file writer: " + name );
    return new FileWriter( name );
  }

  /** @return file writer
   * @param file    file 
   */
  public static FileWriter getFileWriter( File file ) throws IOException { return new FileWriter( file ); }

  /** @return file reader
   * @param name    file fullpath
   */
  public static FileReader getFileReader( String name ) throws IOException { return new FileReader( name ); }

  /** @return file reader
   * @param file    file 
   */
  public static FileReader getFileReader( File file ) throws IOException { return new FileReader( file ); }

  // -----------------------------------------------------------------------------
  /** delete a file
   * @param f   file to delete
   * @return true if success
   */
  public static boolean deleteFile( File f ) 
  {
    boolean ret = false;
    if ( f != null && f.exists() ) {
      ret = f.delete();
      if ( ! ret ) TDLog.e("file delete failed " + f.getName() );
    }
    return ret;
  }

  /** delete a folder and its files
   * @param dir   folder to delete
   * @return true if the folder has been deleted
   */
  public static boolean deleteDir( File dir ) 
  {
    if ( dir == null || ! dir.exists() ) return false;
    boolean ret = false;
    boolean ok = true; // if could delete files in the folder
    File[] files = dir.listFiles();
    if ( files != null ) {
      for ( File file : files ) {
        if (file.isFile()) {
          if ( ! file.delete() ) {
            ok = false;
            TDLog.e("file delete failed " + file.getName() ); 
          }
        } else {
          ok = false;
          TDLog.e("file not regular " + file.getName() ); 
        }
      }
    }
    if ( ok ) {
      ret = dir.delete();
      if ( ! ret )  TDLog.e("dir delete failed " + dir.getName() );
    }
    return ret;
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

  /** recursively delete a folder
   * @param dir   folder to delete
   */
  public static boolean recursiveDeleteDir( File dir )
  {
    if ( dir == null ) return false;
    if ( dir.isFile() ) return dir.delete();
    if ( dir.isDirectory() ) {
      String[] children = dir.list();
      for ( String child : children ) { // 202301018 (2 lines)
        if (!recursiveDeleteDir(new File(dir, child))) return false;
      }
      return dir.delete();
    }
    return false;
  }

  /** delete a TopoDroid file
   * @param pathname   full pathname of the file to delete
   * @return true if success
   */
  public static boolean deleteFile( String pathname ) 
  { 
    return deleteFile( getTopoDroidFile( pathname ) ); // DistoXFile;
  }

  /** delete a TopoDroid folder
   * @param dirname   full pathname of the folder to delete
   * @return true if the folder has been deleted
   */
  public static boolean deleteDir( String dirname ) 
  { 
    return deleteDir( getTopoDroidFile( dirname ) ); // DistoX-SAF
  }

  /** rename a file
   * @param oldname   old pathname (it's ok if it does not exist)
   * @param newname   new pathname
   * @pre oldname exists AND ! newname exists
   */
  public static void renameFile( String oldname, String newname )
  {
    File f1 = getTopoDroidFile( oldname ); // DistoX-SAF
    if ( ! f1.exists() ) return;
    File f2 = getTopoDroidFile( newname );
    if ( ! f2.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.e("file rename: failed " + oldname + " to " + newname );
    } else {
      TDLog.e("file rename: " + oldname + " to existing " + newname );
    }
  }

  /** rename a file
   * @param oldname   old pathname of the existing file
   * @param newname   new pathname
   * @pre oldname exists
   */
  public static boolean moveFile( String oldname, String newname )
  {
    File f1 = getTopoDroidFile( oldname ); // DistoX-SAF
    File f2 = getTopoDroidFile( newname );
    if ( f1.exists() ) {
      if ( f1.renameTo( f2 ) ) return true;
      TDLog.e("file move: failed " + oldname + " to " + newname );
    } else {
      TDLog.e("file move: no-exist " + oldname ); // this may be OK
    }
    return false;
  }

  /** creates a "topodroid" folder
   * @param pathname    folder pathname
   */
  public static File makeTopoDroidDir( String pathname )
  {
    // TDLog.v("FILE make TD dir " + pathname );
    File f = new File( pathname );
    if ( ! f.exists() ) {
      if ( ! f.mkdirs() ) {
        TDLog.e("mkdir topodroid failed " + pathname );
        return null;
      }
    }
    return f;
  }

  /** creates an "external" folder
   * @param type    folder pathname
   */
  public static File makeExternalDir( String type )
  {
    File f = getExternalDir( type ); 
    if ( ! f.exists() ) {
      if ( ! f.mkdirs() ) {
        TDLog.e("mkdir external failed " + type );
        return null;
      }
    }
    return f;
  }

  /** rename a temporary file
   * @param temp   temporary file
   * @param file   target file
   */
  public static boolean renameTempFile( File temp, File file )
  {
    boolean ret = false;
    // TDLog.v( "rename " + temp.getAbsolutePath() + " to " + file.getAbsolutePath() ); // was getPath()
    synchronized( mFilesLock ) {
      if ( file.exists() ) file.delete();
      ret = temp.renameTo( file );
    }
    return ret;
  }

  /** rename a temporary file
   * @param temp       temporary file
   * @param pathname   pathname of the target file 
   */
  public static boolean renameTempFile( File temp, String pathname )
  { 
    return renameTempFile( temp, getTopoDroidFile( pathname ) );
  }

  /** rename a TopoDroid file to another TopoDroid file
   * @param oldname  pathname of TopoDroid file
   * @param newname   pathname of the target file
   */
  public static boolean renameTopoDroidFile( String oldname, String newname )
  { 
    File oldfile = getTopoDroidFile( oldname );
    if ( ! oldfile.exists() ) return true;
    return renameTempFile( oldfile, getTopoDroidFile( newname ) );
  }

  /** rename a temporary file to a TopoDroid filE
   * @param oldname  pathname of temporary file
   * @param newname   pathname of the target file
   */
  public static boolean renameExternalTempFile( String oldname, String newname )
  { 
    File oldfile = getExternalTempFile( oldname );
    if ( ! oldfile.exists() ) return true;
    return renameTempFile( oldfile, getTopoDroidFile( newname ) );
  }
  

  // =========================================================================
  // GENERIC INTERFACE relative to TDPath.getPathBase() (CWD)

  public static File getMSfile( String name ) 
  { 
    // TDLog.v("MSfile: " + TDPath.getPathBase() + "/" + name );
    return new File( TDPath.getPathBase() + "/" + name );
  }

  public static File getMSfile( String subdir, String name ) 
  { 
    // TDLog.v("MSfile: " + TDPath.getPathBase() + "/" + subdir + "/" + name );
    return new File( TDPath.getPathBase() + "/" + subdir + "/" + name ); 
  }

  public static long getMSFileLength( String subdir, String name ) 
  { 
    if ( name == null ) return 0;
    File file = getMSfile( subdir, name );
    return (file == null)? 0 : file.length();
  }

  // @param subdir   folder, relative to CWD
  public static boolean hasMSdir( String subdir )
  {
    return getMSfile( subdir ).exists();
  }

  // @param subdir   folder, relative to CWD
  // @param name     filename, relative to the folder
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

  /** create a MS folder
   * @param subdir   folder name
   * @return true if the folder has been created
   */
  public static boolean makeMSdir( String subdir )
  {
    File dir = getMSfile( subdir );
    if ( dir.exists() ) return true;
    // TDLog.v("make MS dir " + subdir );
    return dir.mkdirs();
  }

  /** delete a MS folder
   * @param subdir   folder name
   * @return true if the folder has been deleted
   */
  public static boolean deleteMSdir( String subdir )
  {
    return deleteDir( getMSfile( subdir ) );
  }

  static public FileOutputStream getMSoutput( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! makeMSdir( subdir ) ) {
      TDLog.e("failed to create subdir " + subdir );
      throw new IOException("failed to create subdir");
    }
    return new FileOutputStream( getMSfile( subdir, filename ) );
  }

  // @note the returned OutputStreamWriter must be closed after it has been written
  static public BufferedWriter getMSwriter( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! makeMSdir( subdir ) ) {
      TDLog.e("failed to create subdir " + subdir );
      throw new IOException("failed to create subdir");
    }
    FileOutputStream os = new FileOutputStream( getMSfile( subdir, filename ) );
    if ( os == null ) {
      TDLog.e("failed to create output stream " + filename );
      throw new IOException( "failed to create file output stream ");
    }
    return new BufferedWriter( new OutputStreamWriter( os ) );
  }

  /** @return a FileInputStream
   * @param subdir    subdir relative to app base
   * @param filename  file name
   * @param mimetype  not used
   */
  static public FileInputStream getMSinput( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! hasMSdir( subdir ) ) {
      TDLog.e("Get MS input - failed: no subdir " + subdir );
      throw new IOException("failed: no subdir");
    }
    return new FileInputStream( getMSfile( subdir, filename ) );
  }

  /**
   * @return get a reader for the FileInputStream, then we can read  
   * @param subdir    subdir relative to app base
   * @param filename  file name
   * @param mimetype  not used
   */
  static public BufferedReader getMSReader( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! hasMSdir( subdir ) ) {
      TDLog.e("Get MS reader - failed: no subdir " + subdir );
      throw new IOException("failed: no subdir");
    }
    FileInputStream is = new FileInputStream( getMSfile( subdir, filename ) );
    if ( is == null ) {
      TDLog.e("failed to create input stream " + filename );
      throw new IOException( "failed to create file input stream ");
    }
    return new BufferedReader( new InputStreamReader( is, Charset.forName( "UTF-8" ) ) );
  }
 
  /* =============================================================================
  // TDMediaStore functions
  //
  // static public boolean isMSexists( String subdir, String filename )
  //
  // @note the returned FileOutputStream must be closed after it has been written
  // static public FileOutputStream getMSoutput( String subdir, String filename, String mimetype )
  //
  // static public FileInputStream getMSinput( String subdir, String filename, String mimetype )
  //
  // NOTE listing returns only items inserted with MediaStore
  // @param subdir   topodroid subdirectory
  // @param filter   filename filter
  // static public ArrayList<String> getMSfilelist( String subdir )
  // 
  // static public void osWriteString( FileOutputStream os, String str ) throws IOException
  //
  */

  // -------------------------------------------------------------------------------

  /** get the file descriptor.
   * @param uri    file uri
   * @param mode   open mode, eg, "r" or "wt"
   * @return file descriptor
   * @note After use must call close() on the file descriptor
   */
  static ParcelFileDescriptor getFileDescriptor( Uri uri, String mode )
  {
    try {
      return TDInstance.getContentResolver().openFileDescriptor( uri, mode );
    } catch ( FileNotFoundException e ) {
      return null;
    }
  }

  /** close a file descriptor
   * @param pfd    parcel file descriptor
   */
  static void closeFileDescriptor( ParcelFileDescriptor pfd ) 
  {
    if ( pfd != null ) {
      try {
        pfd.close();
      } catch ( IOException e ) {
        TDLog.e( e.getMessage() );
      }
    }
  }

  /** @return input stream of a file descriptor
   * @param pfd    parcel file descriptor
   */
  static FileInputStream getFileInputStream( ParcelFileDescriptor pfd )
  {
    if ( pfd != null ) {
      // return TDInstance.getContentResolver().openInputStream( uri );
      return new FileInputStream( pfd.getFileDescriptor() );
    }
    return null;
  }

  /** @return output stream of a file descriptor
   * @param pfd    parcel file descriptor
   */
  static FileOutputStream getFileOutputStream( ParcelFileDescriptor pfd )
  {
    if ( pfd != null ) {
      // return TDInstance.getContentResolver().openOutputStream( uri );
      return new FileOutputStream( pfd.getFileDescriptor() );
    }
    return null;
  }

} 
