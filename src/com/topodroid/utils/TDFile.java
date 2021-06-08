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
import android.content.ContentUris;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.ParcelFileDescriptor;

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

  public interface FileFilter 
  {
    public boolean accept( TDFile doc );
  }

  public interface NameFilter 
  {
    public boolean accept( String name );
  }

  public class ExtensionFilter implements NameFilter
  {
    ArrayList<String> mExtensions;
    public ExtensionFilter( String[] extensions )
    {
      mExtensions = new ArrayList<String>();
      if ( extensions != null ) {
        for ( int k = 0; k < extensions.length; ++k ) mExtensions.add( extensions[k].toLowerCase() );
      }
    }

    public void addExtension( String ext ) { mExtensions.add( ext ); }

    public boolean accept( String name ) 
    {
      name = name.toLowerCase();
      for ( String ext : mExtensions ) {
        if ( name.endsWith( ext ) ) return true;
      }
      return false;
    }
  }

  /* =========================================================================
  // Storage Access Framework

  private String         mFilename; // file name
  private DocumentFile   mDocFile;  // file document

  // NAMES -----------------------------------------------------------------

  public String getName() { return mFilename; } // docFile.getName()

  public String getPath( ) { return filenameToPath( mFilename ); }

  public TDFile getParentFile() { return (mDocFile == null)? null : new TDFile( mDocFile.getParentFile() ); }

  public DocumentFile getDocumentFile() { return mDocFile; }


  // CSTR ------------------------------------------------------------------

  public TDFile( String filename, boolean create )
  {
    mDocFile  = filenameToDocumentFile( filename, create );
    mFilename = filename;
  }

  public TDFile( String filename ) { this( filename, false ); }

  public TDFile( DocumentFile doc ) 
  {
    mDocFile  = doc;
    mFilename = mDocFile.getName();
  }

  // FILE OPS -----------------------------------------------------------------------------

  public boolean mkdirs() { return mkdir(); }

  public boolean mkdir() 
  {
    if ( ! exists() ) mDocFile = filenameToDocumentFile( mFilename, true );
    return mDocFile != null;
  }

  public void rename( String filename )
  {
    if ( exists() ) if ( mDocFile.renameTo( filename ) ) mFilename = filename;
  }

  public boolean renameTo ( TDFile file )
  {
    if ( ! exists() || file.exists() ) return false;
    String filename = file.getName();
    boolean ret = mDocFile.renameTo( filename );
    if ( ret ) mFilename = filename;
    return ret;
  }

  public boolean isDirectory() { return mDocFile != null && mDocFile.isDirectory(); }
  public boolean isFile()      { return mDocFile != null && mDocFile.isFile(); }
  public boolean canWrite()    { return mDocFile != null && mDocFile.canWrite(); }

  public TDFile[] listFiles()
  {
    if ( ! exists() ) return null;
    DocumentFile[] docs = mDocFile.listFiles();
    TDFile[] ret = new TDFile[ docs.length ];
    int k = 0;
    for ( DocumentFile doc : docs ) ret[k++] = new TDFile( doc );
    return ret;
  }

  public TDFile[] listFiles( FileFilter filter )
  {
    if ( ! exists() ) return null;
    DocumentFile[] docs = mDocFile.listFiles();
    ArrayList<TDFile> ret = new ArrayList<TDFile>();
    for ( DocumentFile doc : docs ) {
      TDFile file = new TDFile( doc );
      if ( filter.accept( file ) ) ret.add( file );
    }
    return (TDFile[])(ret.toArray());
  }

  public TDFile[] listFiles( NameFilter filter )
  {
    if ( ! exists() ) return null;
    DocumentFile[] docs = mDocFile.listFiles();
    ArrayList<TDFile> ret = new ArrayList<TDFile>();
    for ( DocumentFile doc : docs ) {
      if ( filter.accept( doc.getName() ) ) ret.add( new TDFile( doc ) );
    }
    return (TDFile[])(ret.toArray());
  }

  public boolean delete()
  {
    if ( ! exists() ) return true;
    boolean ret = mDocFile.delete();
    mDocFile = null;
    return ret;
  }

  public boolean exists() { return mDocFile != null && mDocFile.exists(); }

  // URI =============================================================================
  static final String BASE = "/storage/emulated/0";

  private static final int PERMISSIONS = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
  private static final int PERSISTABLE_PERMISSIONS = PERMISSIONS | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;

  private static final int REQ_CREATE = 40;
  private static final int REQ_BASE = 50;
  private static final int REQ_MAIN = 60;

  private static Uri          mBaseDirUri;
  private static String       mBaseDirUriStr;
  private static DocumentFile mBaseDirTree = null;
  private static boolean mGrantRequested = false;

  private static DocumentFile getBaseDirTree( ) 
  { 
    return mBaseDirTree;
  }

  // open the directory and list its files
  public void openBaseDir( Activity activity )
  {
    try {
      Context ctx = TDInstance.context;
      ctx.grantUriPermission( ctx.getPackageName(), mBaseDirUri, PERMISSIONS );
      Uri child = DocumentsContract.buildChildDocumentsUriUsingTree( mBaseDirUri, DocumentsContract.getTreeDocumentId( mBaseDirUri ) );
      mBaseDirTree = DocumentFile.fromTreeUri( ctx, child );
      Log.v("DistoX-SAF", "open base dir: Uri " + mBaseDirUri.toString() );
    } catch ( SecurityException e ) {
      if ( ! mGrantRequested ) {
        mGrantRequested = true;
        getBaseDirUri( activity );
      }
    }
  }

  public void setupBaseDir( Activity activity )
  {
    mBaseDirUri = loadUriPref();
    if ( mBaseDirUri == null ) {
      Log.v("DistoX-SAF", "setup base dir: null uri");
      getBaseDirUri( activity );
    } else {
      openBaseDir( activity );
    }
  }

  private void putInitialUri( Intent intent, String base ) 
  {
    Uri initialUri = Uri.parse( "file://" + base );
    intent.putExtra( DocumentsContract.EXTRA_INITIAL_URI, initialUri );
  }

  // start activity to get the dir URI
  private void getBaseDirUri( Activity activity  )
  {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    // intent.addCategory( Intent.CATEGORY_OPENABLE );
    // intent.setType( "text/plain" );
    intent.putExtra(Intent.EXTRA_TITLE, "TopoDroid");
    putInitialUri( intent, BASE );
    activity.startActivityForResult(intent, REQ_BASE);
  }

  // create a new directory
  public void newBaseDir( Activity activity, String dirname )
  {
    Log.v("DistoX-SAF", "new base dir " + dirname );
    if ( dirname != null && dirname.length() > 0) {
      Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
      intent.setType( DocumentsContract.Document.MIME_TYPE_DIR );
      intent.putExtra(Intent.EXTRA_TITLE, dirname);
      putInitialUri( intent, BASE );
      // intent.putExtra("android.com.provider.extra.INITIAL_UTI", uri);
      intent.putExtra("android.content.extra.SHOW_ADVANCED", true );
      activity.startActivityForResult(intent, REQ_CREATE);
    }
  }

  // in reply to results to the activity that required
  //    newBaseDir                    REQ_CREATE
  //    openBaseDir / setupBaseDir    REQ_BASE
  //                                  REQ_MAIN should finish the setup activity
  public void onActivityResult( Activity activity, int reqCode, int resCode, Intent data) 
  {
    // super.onActivityResult(reqCode, resCode, data);
    Uri currentUri;
    // Log.v("DistoX-SAF, "on activity result " + resCode + " request " + reqCode);
    if (resCode == Activity.RESULT_OK) {
      if (reqCode == REQ_CREATE) {
        if (data != null) {
          // mText.setText("created File");
        }
      } else if (reqCode == REQ_BASE) {
        if ( data != null ) {
          Uri uri = data.getData();
          mBaseDirUri = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri));
            // grantUriPermission(getPackageName(), mBaseDirUri, PERMISSIONS );
          Log.v("DistoX-SAF", "dir uri " + mBaseDirUri.toString() );
          final int flags = data.getFlags() & PERMISSIONS;
          try {
            // take perms persistable across reboots
            TDInstance.context.getContentResolver().takePersistableUriPermission(mBaseDirUri, flags);
          } catch ( SecurityException e ) {
            e.printStackTrace();
            Log.v("DistoX-SAF", "failed to take persistable URI permissions: flags " + flags );
          }
          storeUriPref( mBaseDirUri );
          openBaseDir( activity );
        } else {
          Log.v("DistoX-SAF", "REQ_BASE null data");
        }
      }
    }
  }

  // ---------------------------------------------------------------
  // stored uri string is decoded
  private void storeUriPref(Uri uri) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( TDInstance.context );
      SharedPreferences.Editor editor = prefs.edit();
      mBaseDirUriStr = Uri.decode( uri.toString() );
      // Log.v("DistoX-SAF, "store dir uri str " + mBaseDirUriStr );
      editor.putString( "DIR_URI", mBaseDirUriStr );
      editor.apply();
  }

  private Uri loadUriPref() {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( TDInstance.context );
      mBaseDirUriStr = prefs.getString( "DIR_URI", null );
      // Log.v("DistoX-SAF, "load uri pref " + ((mBaseDirUriStr == null)? "null" : mBaseDirUriStr ) );
      if ( mBaseDirUriStr == null ) return null;
      return Uri.parse( mBaseDirUriStr );
  }

  // URI -----------------------------------------------------------------------------

  public Uri createFile() { return createFile( mFilename ); }
  public static Uri createFile( String filename ) { return filenameToUri( getBaseDirTree(), docMime(filename), filename, true ); }
  public static Uri createFile( String mime, String filename ) { return filenameToUri( getBaseDirTree(), mime, filename, true ); }

  public Uri docFileUri() { return docFileUri( mFilename ); }
  public static Uri docFileUri( String filename ) { return filenameToUri( getBaseDirTree(), docMime(filename), filename, false ); }
  public static Uri docFileUri( String mime, String filename ) { return filenameToUri( getBaseDirTree(), mime, filename, false ); }

  public static Uri filenameToUri( String mime, String filename, boolean create ) { return filenameToUri( getBaseDirTree(), mime, filename, create ); }

  // -------------------------------------------------------------------------------
  private String filenameToPath( String filename )
  {
    int pos = filename.lastIndexOf('/');
    if ( pos > 0 ) {
      String dirname = filename.substring(0,pos);
      String name = filename.substring(pos+1);
      String mime = DocumentsContract.Document.MIME_TYPE_DIR;
      Uri uri = filenameToUri( getBaseDirTree(), mime, dirname, false );
      return (uri == null)? null : uri.getPath()+"/"+name;
    } else {
      Uri uri = filenameToUri( getBaseDirTree(), docMime(filename), filename, false );
      return (uri == null)? null : uri.getPath();
    }
  }

  // DOCUMENT FILE ==================================================================

  public DocumentFile filenameToDocumentFile( String filename, boolean create ) 
  { 
    return filenameToDocumentFile( getBaseDirTree(), docMime(filename), filename, create ); 
  }

  private static Uri filenameToUri( DocumentFile dir, String mime, String filename, boolean create )
  {
    // Log.v("DistoX-SAF", "dir " + dir.getName() + " uri " + dir.getUri().toString() );
    // for ( DocumentFile doc : dir.listFiles() ) {
    //   Log.v("DistoX-SAF", "doc " + doc.getName() );
    // }
    // Log.v("DistoX-SAF", "filename " + filename + " mime " + mime + " create " + create );
    if ( filename.length() > 0) {
      // DocumentFile file1 = dir.findFile( filename );
      // if ( file1 != null ) return file1.getUri();

      int pos = filename.indexOf( '/' );
      if ( pos > 0 ) {
        String subdirname = filename.substring( 0, pos );
        String filepath   = filename.substring( pos+1 );
        DocumentFile subdir = dir.findFile( subdirname );
        if ( subdir == null ) {
          if ( create ) {
            subdir = newDir( dir, subdirname );
          } else {
            // Log.v("DistoX-SAF", "no subdir " + subdirname + " no create");
          }
        }
        if ( subdir == null ) return null;
        return filenameToUri( subdir, mime, filepath, create );
      } else {
        Uri uri = null;
        DocumentFile file = dir.findFile( filename );
        if ( file == null ) {
          if ( create ) {
            file = dir.createFile( mime, filename );
            if ( file != null ) {
              uri = file.getUri();
              // Log.v("DistoX-SAF", "file created " + uri.toString() );
            // } else {
            //   Log.v("DistoX-SAF", "file not found " + filename + " ... failed create");
            }
          // } else {
          //   Log.v("DistoX-SAF", "file not found " + filename + " ... do not create");
          }
        } else {
          uri = file.getUri();
          // Log.v("DistoX-SAF", "file found " + uri.toString() );
        }
        return (file == null)? null : file.getUri();
      }
    }
    return null;
  }

  private static DocumentFile filenameToDocumentFile( DocumentFile dir, String mime, String filename, boolean create )
  {
    // Log.v("DistoX-SAF", "dir " + dir.getName() + " uri " + dir.getUri().toString() );
    // for ( DocumentFile doc : dir.listFiles() ) {
    //   Log.v("DistoX-SAF", "doc " + doc.getName() );
    // }
    // Log.v("DistoX-SAF", "filename " + filename + " mime " + mime + " create " + create );
    if ( filename.length() > 0 ) {
      // DocumentFile file1 = dir.findFile( filename );
      // if ( file1 != null ) return file1.docUri();

      int pos = filename.indexOf( '/' );
      if ( pos > 0 ) {
        String subdirname = filename.substring( 0, pos );
        String filepath   = filename.substring( pos+1 );
        DocumentFile subdir = dir.findFile( subdirname );
        if ( subdir == null ) {
          if ( create ) {
            subdir = newDir( dir, subdirname );
          } else {
            // Log.v("DistoX-SAF", "no subdir " + subdirname + " no create");
          }
        }
        if ( subdir == null ) return null;
        return filenameToDocumentFile( subdir, mime, filepath, create );
      } else {
        DocumentFile file = dir.findFile( filename );
        if ( file == null ) {
          if ( create ) {
            file = dir.createFile( mime, filename );
          }
        }
        return file;
      }
    }
    return null;
  }

  private static DocumentFile newDir( DocumentFile dir, String dirname ) 
  {
    // Log.v("DistoX-SAF", "new dir " + dirname );
    if ( dirname == null || dirname.length() == 0 ) return dir;
    return dir.createDirectory( dirname );
  }

  // IO STREAMS  -----------------------------------------------------------------------------

  public InputStream      docInputStream( )      { return docInputStream( mFilename ); }
  public FileInputStream  docFileInputStream( )  { return docFileInputStream( mFilename ); }
  public FileOutputStream docFileOutputStream( ) { return docFileOutputStream( mFilename, true ); }
  public FileReader docFileReader( ) { return docFileReader( mFilename ); }
  public FileWriter docFileWriter( ) { return docFileWriter( mFilename, true ); }

  public InputStream docInputStream( String filename )
  {
    // Log.v("DistoX-SAF", "get imput filename " + filename + " mime " + mime );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, false );
    if ( uri == null ) return null;
    return docInputStream( uri );
  }

  public FileInputStream docFileInputStream( String filename )
  {
    // Log.v("DistoX-SAF", "get file imput filename " + filename + " mime " + mime );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, false );
    if ( uri == null ) return null;
    return docFileInputStream( uri );
  }


  public FileOutputStream docFileOutputStream( String filename, boolean create )
  {
    // Log.v("DistoX-SAF", "get file output filename " + filename + " mime " + mime + " create " + create );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, create );
    if ( uri == null ) return null;
    return docFileOutputStream( uri );
  }

  public FileReader docFileReader( String filename )
  {
    // Log.v("DistoX-SAF", "get file imput filename " + filename + " mime " + mime );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, false );
    if ( uri == null ) return null;
    return docFileReader( uri );
  }

  public FileWriter docFileWriter( String filename, boolean create )
  {
    // Log.v("DistoX-SAF", "get file output filename " + filename + " mime " + mime + " create " + create );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, create );
    if ( uri == null ) return null;
    return docFileWriter( uri );
  }

  // -------------------------------------------------------------------------------------
  private InputStream docInputStream( Uri uri ) 
  {
    // if ( uri == null ) return null;
    try {
      InputStream is = TDInstance.context.getContentResolver().openInputStream(uri);
      return is;
      // FIXME caller must close is
    } catch (IOException e) {
      e.printStackTrace();
      // Log.v("DistoX-SAF", "failed open input stream" );
    }
    return null;
  }

  private FileOutputStream docFileOutputStream( Uri uri ) 
  {
    // if ( uri == null ) return null;
    try {
      // AssetFileDescriptor pfd = TDInstance.context.getContentResolver().openAssetFileDescriptor( uri, "w" );
      ParcelFileDescriptor pfd = TDInstance.context.getContentResolver().openFileDescriptor( uri, "w" );
      if ( pfd == null ) return null;
      FileDescriptor fd = pfd.getFileDescriptor();
      if (fd == null) {
        pfd.close();
        return null;
      }
      FileOutputStream fos = new FileOutputStream(fd);
      fos.getChannel().truncate(0);
      return fos;
      // FIXME fos must be closed(), but also pfd must be closed
    } catch ( IOException e ) {
      e.printStackTrace();
      // Log.v("DistoX-SAF", "failed open output stream" );
    }
    return null;
  }

  private FileWriter docFileWriter( Uri uri ) 
  {
    // if ( uri == null ) return null;
    try {
      // AssetFileDescriptor pfd = TDInstance.context.getContentResolver().openAssetFileDescriptor( uri, "w" );
      ParcelFileDescriptor pfd = TDInstance.context.getContentResolver().openFileDescriptor( uri, "w" );
      if ( pfd == null ) return null;
      FileDescriptor fd = pfd.getFileDescriptor();
      if (fd == null) {
        pfd.close();
        return null;
      }
      FileWriter fw = new FileWriter(fd);
      // fos.getChannel().truncate(0);
      return fw;
      // FIXME fos must be closed(), but also pfd must be closed
    } catch ( IOException e ) {
      e.printStackTrace();
      // Log.v("DistoX-SAF", "failed open output stream" );
    }
    return null;
  }

  private FileInputStream docFileInputStream( Uri uri ) 
  {
    if ( uri == null ) return null;
    try {
      // AssetFileDescriptor pfd = TDInstance.context.getContentResolver().openAssetFileDescriptor( uri, "w" );
      ParcelFileDescriptor pfd = TDInstance.context.getContentResolver().openFileDescriptor( uri, "r" );
      if ( pfd == null ) return null;
      FileDescriptor fd = pfd.getFileDescriptor();
      if (fd == null) {
        pfd.close();
        return null;
      }
      FileInputStream fis = new FileInputStream(fd);
      // fis.getChannel(). ???
      return fis;
      // FIXME fos must be closed(), but also pfd must be closed
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return null;
  }

  private FileReader docFileReader( Uri uri ) 
  {
    if ( uri == null ) return null;
    try {
      // AssetFileDescriptor pfd = TDInstance.context.getContentResolver().openAssetFileDescriptor( uri, "w" );
      ParcelFileDescriptor pfd = TDInstance.context.getContentResolver().openFileDescriptor( uri, "r" );
      if ( pfd == null ) return null;
      FileDescriptor fd = pfd.getFileDescriptor();
      if (fd == null) {
        pfd.close();
        return null;
      }
      FileReader fr = new FileReader(fd);
      // fis.getChannel(). ???
      return fr;
      // FIXME fos must be closed(), but also pfd must be closed
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return null;
  }

  // MIME ---------------------------------------------------------------------

  static String docMime( String filename )
  {
    int pos = filename.lastIndexOf('.');
    if ( pos > 0 ) {
      String ext = filename.substring( pos+1 );
      return "application/" + ext;
    }
    if ( filename.endsWith("/") ) {
      return DocumentsContract.Document.MIME_TYPE_DIR;
    }
    return "application/topodroid";
  }

  =========================================================================== */

  // OLD FILE FUNCTIONS -----------------------------------------------------------------------------

  public static File getManDir( )    { return new File( TDInstance.context.getFilesDir(), "man" ); }
  public static File getManFile( String name )    { return new File( getManDir(), name ); }
  public static boolean hasManFile( String name ) { return getManFile( name ).exists(); }
  public static FileReader getManFileReader( String name ) throws IOException { return new FileReader( getManFile(name) ); }
 
  public static boolean hasFile( String name ) { return name != null && (new File( name )).exists(); }

  public static long getFileLength( String name ) { return (name == null)? 0 : (new File(name)).length(); }

  // @param name     TopoDroid-relative filename
  public static File getFile( String name ) { return new File( name ); }

  public static File getTopoDroidFile( String dirname, String name ) { return new File( dirname, name ); }

  // INTERNAL FILES --------------------------------------------------------------
  // context.getFilesDir --> /data/user/0/com.topodroid.DistoX/files

  // CACHE FILES --------------------------------------------------------------

  public static File getCacheFile( String filename ) { return new File ( TDInstance.context.getCacheDir(), filename ); }

  public static void clearCache( long before )
  {
    long now  = System.currentTimeMillis();
    long time = now - before; // clean the cache "before" minutes before now
    File cacheDir = TDInstance.context.getCacheDir();
    File[] files = cacheDir.listFiles();
    if ( files != null ) for ( File f : files ) {
      if ( f.lastModified() < time ) {
        if ( ! f.delete() ) TDLog.Error("File delete error");
      }
    }
  }

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

  public static void clearCache()
  {
    try {
      File cache = TDInstance.context.getCacheDir();
      recursiveDeleteDir( cache );
    } catch ( Exception e ) {
      // TODO
    }
  }

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
    deleteFile( getFile( pathname ) ); // DistoXFile;
  }

  public static void deleteDir( String dirname ) 
  { 
    deleteDir( getFile( dirname ) ); // DistoX-SAF
  }

  // @pre oldname exists && ! newname exists
  public static void renameFile( String oldname, String newname )
  {
    File f1 = getFile( oldname ); // DistoX-SAF
    File f2 = getFile( newname );
    if ( f1.exists() && ! f2.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.Error("file rename: failed " + oldname + " to " + newname );
    } else {
      TDLog.Error("file rename: no-exist " + oldname + " or exist " + newname );
    }
  }

  // @pre oldname exists
  public static void moveFile( String oldname, String newname )
  {
    File f1 = getFile( oldname ); // DistoX-SAF
    File f2 = getFile( newname );
    if ( f1.exists() ) {
      if ( ! f1.renameTo( f2 ) ) TDLog.Error("file move: failed " + oldname + " to " + newname );
    } else {
      TDLog.Error("file move: no-exist " + oldname );
    }
  }

  public static File makeDir( String pathname )
  {
    File f = getFile( pathname );
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
    return renameTempFile( temp, getFile( pathname ) );
  }

  // =========================================================================
  // GENERIC INTERFACE

  public static boolean hasMSdir( String subdir )
  {
    File dir = new File( TDPath.getPathBase() + subdir );
    return ( dir.exists() );
  }

  public static boolean hasMSfile( String subdir, String name )
  {
    File dir = new File( TDPath.getPathBase() + subdir );
    if ( ! dir.exists() ) return false;
    File file = new File( dir, name );
    return file.exists();
  }

  public static boolean hasMSfile( String pathname )
  {
    return (new File(pathname)).exists();
  }

  public static boolean makeMSdir( String subdir )
  {
    File dir = new File( TDPath.getPathBase() + subdir );
    if ( dir.exists() ) return true;
    return dir.mkdirs();
  }

  static public OutputStream getMSoutput( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! makeMSdir( subdir ) ) {
      TDLog.Error("failed to create subdir " + subdir );
      throw new IOException("failed to create subdir");
    }
    return new FileOutputStream( TDPath.getPathBase() + subdir + "/" + filename );
  }

  // @note the returnet OutputStreamWriter must be closed after it has been written
  static public BufferedWriter getMSwriter( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! makeMSdir( subdir ) ) {
      TDLog.Error("failed to create subdir " + subdir );
      throw new IOException("failed to create subdir");
    }
    OutputStream os = new FileOutputStream( TDPath.getPathBase() + subdir + "/" + filename );
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
    return new FileInputStream( TDPath.getPathBase() + subdir + "/" + filename );
  }

  // get a reader for the InputStream
  // then we can read  
  static public BufferedReader getMSReader( String subdir, String filename, String mimetype ) throws IOException
  {
    if ( ! hasMSdir( subdir ) ) {
      TDLog.Error("failed: no subdir " + subdir );
      throw new IOException("failed: no subdir");
    }
    InputStream is = new FileInputStream( TDPath.getPathBase() + subdir + "/" + filename );
    if ( is == null ) {
      TDLog.Error("failed to create input stream " + filename );
      throw new IOException( "failed to create file input stream ");
    }
    return new BufferedReader( new InputStreamReader( is, Charset.forName( "UTF-8" ) ) );
  }
 
  /* =============================================================================
  // MediaStore
  // thanks to https://stackoverflow.com/questions/59511147/create-copy-file-in-android-q-using-mediastore/62879112#62879112
  //
  // MediaStore has an unrecoverable flaw: if the user adds a file without updating the MediaStore database
  // this file is not seen by the MediaStore

  static public boolean isMSexists( String subdir, String filename )
  {
    String dir = "Documents/TopoDroid/" + subdir + "/";
    ContentResolver cr = TDInstance.context.getContentResolver();
    Uri content_uri = MediaStore.Files.getContentUri("external");
    String where = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " + MediaStore.MediaColumns.DISPLAY_NAME + "=?";
    String[] args = new String[]{ dir, filename };
    Cursor cursor = cr.query( content_uri, null, where, args, null);
    return ( cursor != null && cursor.getCount() > 0 );
  }

  // @note the returnet OutputStream must be closed after it has been written
  static public OutputStream getMSoutput( String subdir, String filename, String mimetype )
  {
    OutputStream ret = null;
    String dir = "Documents/TopoDroid/" + subdir + "/";

    ContentResolver cr = TDInstance.context.getContentResolver();
    Uri content_uri = MediaStore.Files.getContentUri("external");

    Uri uri = null;
    String where = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " + MediaStore.MediaColumns.DISPLAY_NAME + "=?";
    String[] args = new String[]{ dir, filename };
    Cursor cursor = cr.query( content_uri, null, where, args, null);
    if ( cursor != null && cursor.getCount() > 0 ) {
      // Log.v("DistoX", "Media store overwrite");
      cursor.moveToNext();
      long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
      uri = ContentUris.withAppendedId(content_uri, id);
    } else {
      // Log.v("DistoX", "Media store write anew");
      ContentValues cv = new ContentValues();
      cv.put( MediaStore.Files.FileColumns.DISPLAY_NAME,  filename );
      cv.put( MediaStore.Files.FileColumns.MIME_TYPE,     mimetype );
      cv.put( MediaStore.Files.FileColumns.RELATIVE_PATH, dir );
      cv.put( MediaStore.Files.FileColumns.IS_PENDING,    1 );
      uri = cr.insert( content_uri, cv );
    }
    if ( uri == null ) {
      TDLog.Error("Media Store failed resolving");
    } else {
      try {
        ret = cr.openOutputStream( uri, "rwt" );
        ContentValues cv = new ContentValues();
        cv.put( MediaStore.Downloads.IS_PENDING, 0 );
        cr.update( uri, cv, null, null );
      } catch ( FileNotFoundException e ) {
        TDLog.Error("Media Store not found exception " + e.getMessage() );
      } catch ( RuntimeException e ) {
        TDLog.Error("Media Store failed exception " + e.getMessage() );
      }
    }
    return ret;
  }

  static public InputStream getMSinput( String subdir, String filename, String mimetype )
  {
    InputStream ret = null;
    String dir = "Documents/TopoDroid/" + subdir + "/";
    ContentValues cv = new ContentValues();
    cv.put( MediaStore.Files.FileColumns.DISPLAY_NAME,  filename );
    cv.put( MediaStore.Files.FileColumns.MIME_TYPE,     mimetype );
    cv.put( MediaStore.Files.FileColumns.RELATIVE_PATH, dir );
    cv.put( MediaStore.Files.FileColumns.IS_PENDING,    1 );

    ContentResolver cr = TDInstance.context.getContentResolver();
    Uri uri = cr.insert( MediaStore.Files.getContentUri("external"), cv );
    if ( uri == null ) {
      Log.v("DistoX", "Media Store failed resolving");
    } else {
      try {
        ret = cr.openInputStream( uri );
        cv.clear();
        cv.put( MediaStore.Downloads.IS_PENDING, 0 );
        cr.update( uri, cv, null, null );
      } catch ( FileNotFoundException e ) {
        Log.v("DistoX", "Media Store not found exception " + e.getMessage() );
      } catch ( RuntimeException e ) {
        Log.v("DistoX", "Media Store failed exception " + e.getMessage() );
      }
    }
    return ret;
  }

  // NOTE listing returns only items inserted with MediaStore
  //
  // @param subdir   topodroid subdirectory
  // @param filter   filename filter
  // static public ArrayList<String> getMSfilelist( String subdir )
  // {
  //   ArrayList<String> ret = new ArrayList<>();
  //   String dir = "Documents/TopoDroid/" + subdir + "/";
  //   ContentResolver cr = TDInstance.context.getContentResolver();
  //   Uri content_uri = MediaStore.Files.getContentUri("external");
  //   // Uri.Builder builder = content_uri.buildUpon();
  //   // builder.appendPath( "/" + dir );
  //   // Uri dir_uri = builder.build();
  //   // cr.refresh( dir_uri, null, null );
  //   String where = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
  //   String[] args = new String[]{ dir };
  //   Cursor cursor = cr.query( content_uri, null, where, args, null);
  //   Log.v("DistoX", "listing " + dir + " count " + cursor.getCount() );
  //   if ( cursor != null && cursor.getCount() > 0 ) {
  //     while ( cursor.moveToNext() ) {
  //       String filename = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
  //       ret.add( filename );
  //       Log.v("DistoX", "  file " + filename );
  //     }
  //   } 
  //   return ret;
  // }

  // -----------------------------------------------------------------------------
  static public void osWriteString( OutputStream os, String str ) throws IOException
  {
    os.write( str.getBytes( Charset.forName( "UTF-8" ) ) );
  }

  */

} 