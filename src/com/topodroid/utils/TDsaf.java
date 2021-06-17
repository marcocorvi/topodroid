/** @file TDsaf.java
 *
 * @author marco corvi
 * @date june 2021
 *
 * @brief TopoDroid SAF File layer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TDPath;

import android.os.ParcelFileDescriptor;
import android.os.Build;
import android.os.Environment;
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

import android.provider.MediaStore;
// import android.provider.MediaStore.Video;
// import android.provider.MediaStore.Audio;
// import android.provider.MediaStore.Images;

public class TDsaf
{
  // public static final String HOME_DIR = TDPath.getPathBase(); // "Documents/TopoDroid/"

  public interface FileFilter 
  {
    public boolean accept( TDsaf doc );
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

  // Storage Access Framework

  private String         mFilename; // file name
  private DocumentFile   mFiledoc;  // file document

  // CSTR ------------------------------------------------------------------
  public TDsaf( String filename, boolean create )
  {
    mFiledoc  = filenameToDocumentFile( filename, create );
    mFilename = filename;
  }

  public TDsaf( String filename ) 
  {
    mFiledoc  = filenameToDocumentFile( filename, false );
    mFilename = filename;
  }

  public TDsaf( DocumentFile doc ) 
  {
    mFiledoc  = doc;
    mFilename = ( mFiledoc != null )? mFiledoc.getName() : null;
  }

  public TDsaf( Uri uri )
  {
    mFiledoc  = DocumentFile.fromSingleUri( TDInstance.context, uri );
    mFilename = ( mFiledoc != null )? mFiledoc.getName() : null;
  }

  // GETTERS -----------------------------------------------------------------
  public String getName() { return mFilename; } // docFile.getName()

  public DocumentFile getDocumentFile() { return mFiledoc; }

  public String getPath( ) 
  { 
    String ret = filenameToPath( mFilename );
    Log.v("DistoX", "SAF::getPath <" + mFilename + "> <" + ret + ">");
    return ret;
  }

  public TDsaf getParentFile() { return (mFiledoc == null)? null : new TDsaf( mFiledoc.getParentFile() ); }


  // FILE OPS -----------------------------------------------------------------------------
  public boolean mkdirs() { return mkdir(); }

  public boolean mkdir() 
  {
    if ( ! exists() ) mFiledoc = filenameToDocumentFile( mFilename, true );
    return mFiledoc != null;
  }

  public void rename( String filename )
  {
    if ( exists() ) if ( mFiledoc.renameTo( filename ) ) mFilename = filename;
  }

  public boolean renameTo ( TDsaf file )
  {
    if ( ! exists() || file.exists() ) return false;
    String filename = file.getName();
    boolean ret = mFiledoc.renameTo( filename );
    if ( ret ) mFilename = filename;
    return ret;
  }

  public boolean isDirectory() { return mFiledoc != null && mFiledoc.isDirectory(); }
  public boolean isFile()      { return mFiledoc != null && mFiledoc.isFile(); }
  public boolean canWrite()    { return mFiledoc != null && mFiledoc.canWrite(); }

  public TDsaf[] listFiles()
  {
    if ( ! exists() ) return null;
    DocumentFile[] docs = mFiledoc.listFiles();
    TDsaf[] ret = new TDsaf[ docs.length ];
    int k = 0;
    for ( DocumentFile doc : docs ) ret[k++] = new TDsaf( doc );
    return ret;
  }

  public TDsaf[] listFiles( FileFilter filter )
  {
    if ( ! exists() ) return null;
    DocumentFile[] docs = mFiledoc.listFiles();
    ArrayList<TDsaf> ret = new ArrayList<TDsaf>();
    for ( DocumentFile doc : docs ) {
      TDsaf file = new TDsaf( doc );
      if ( filter.accept( file ) ) ret.add( file );
    }
    return (TDsaf[])(ret.toArray());
  }

  public TDsaf[] listFiles( NameFilter filter )
  {
    if ( ! exists() ) return null;
    DocumentFile[] docs = mFiledoc.listFiles();
    ArrayList<TDsaf> ret = new ArrayList<TDsaf>();
    for ( DocumentFile doc : docs ) {
      if ( filter.accept( doc.getName() ) ) ret.add( new TDsaf( doc ) );
    }
    return (TDsaf[])(ret.toArray());
  }

  public boolean delete()
  {
    if ( ! exists() ) return true;
    boolean ret = mFiledoc.delete();
    mFiledoc = null;
    return ret;
  }

  public boolean exists() { return mFiledoc != null && mFiledoc.exists(); }

  // URI =============================================================================
  // static final String BASE = "/storage/emulated/0"; // TDPath.PATH_BASEDIR;

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
    Log.v("DistoX-SAF", "open base dir");
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
    Log.v("DistoX-SAF", "setup base dir");
    mBaseDirUri = loadUriPref();
    if ( mBaseDirUri == null ) {
      Log.v("DistoX-SAF", "setup base dir: null uri");
      getBaseDirUri( activity );
    } else {
      openBaseDir( activity );
    }
  }

  static private void putInitialUri( Intent intent, String base ) 
  {
    Uri initialUri = Uri.parse( "file://" + base );
    intent.putExtra( DocumentsContract.EXTRA_INITIAL_URI, initialUri );
  }

  // start activity to get the dir URI
  static private void getBaseDirUri( Activity activity  )
  {
    Log.v("DistoX-SAF", "get base dir uri");
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION
                   | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                   | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                   | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
    // intent.addCategory( Intent.CATEGORY_OPENABLE );
    // intent.setType( "text/plain" );
    intent.putExtra(Intent.EXTRA_TITLE, "TopoDroid");
    putInitialUri( intent, TDPath.PATH_BASEDIR );
    activity.startActivityForResult(intent, REQ_BASE);
  }

  // create a new directory
  static public void newBaseDir( Activity activity, String dirname )
  {
    Log.v("DistoX-SAF", "new base dir " + dirname );
    if ( dirname != null && dirname.length() > 0) {
      Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
      intent.setType( DocumentsContract.Document.MIME_TYPE_DIR );
      intent.putExtra(Intent.EXTRA_TITLE, dirname);
      putInitialUri( intent, TDPath.PATH_BASEDIR );
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
    Log.v("DistoX-SAF", "on activity result " + resCode + " request " + reqCode);
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
  static private void storeUriPref( Uri uri ) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( TDInstance.context );
      SharedPreferences.Editor editor = prefs.edit();
      mBaseDirUriStr = Uri.decode( uri.toString() );
      // Log.v("DistoX-SAF, "store dir uri str " + mBaseDirUriStr );
      editor.putString( "DIR_URI", mBaseDirUriStr );
      editor.apply();
  }

  static private Uri loadUriPref() {
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
  static private String filenameToPath( String filename )
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

  // create a document (file) 
  // @param dir      parent folder
  // @param mime     file mime type
  // @param filename file name
  // @param create   create if does not exists
  static private DocumentFile filenameToDocumentFile( DocumentFile dir, String mime, String filename, boolean create )
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

  // create a subfolder 
  // @param dir      parent folder
  // @param dirname  subfolder name 
  static private DocumentFile newDir( DocumentFile dir, String dirname ) 
  {
    // Log.v("DistoX-SAF", "new dir " + dirname );
    if ( dirname == null || dirname.length() == 0 ) return dir;
    return dir.createDirectory( dirname );
  }

  // IO STREAMS  -----------------------------------------------------------------------------

  public InputStream      docInputStream( )      { return docInputStream( mFilename ); }
  public FileInputStream  docFileInputStream( )  { return docFileInputStream( mFilename ); }
  public FileOutputStream docFileOutputStream( ) { return docFileOutputStream( mFilename, true ); }
  public FileReader       docFileReader( )       { return docFileReader( mFilename ); }
  public FileWriter       docFileWriter( )       { return docFileWriter( mFilename, true ); }

  // IO STREAMS - static methods -------------------------------------------------------------

  static public InputStream docInputStream( String filename )
  {
    // Log.v("DistoX-SAF", "get imput filename " + filename + " mime " + mime );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, false );
    if ( uri == null ) return null;
    return docInputStream( uri );
  }

  static public FileInputStream docFileInputStream( String filename )
  {
    // Log.v("DistoX-SAF", "get file imput filename " + filename + " mime " + mime );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, false );
    if ( uri == null ) return null;
    return docFileInputStream( uri );
  }


  static public FileOutputStream docFileOutputStream( String filename, boolean create )
  {
    // Log.v("DistoX-SAF", "get file output filename " + filename + " mime " + mime + " create " + create );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, create );
    if ( uri == null ) return null;
    return docFileOutputStream( uri );
  }

  static public FileReader docFileReader( String filename )
  {
    // Log.v("DistoX-SAF", "get file imput filename " + filename + " mime " + mime );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, false );
    if ( uri == null ) return null;
    return docFileReader( uri );
  }

  static public FileWriter docFileWriter( String filename, boolean create )
  {
    // Log.v("DistoX-SAF", "get file output filename " + filename + " mime " + mime + " create " + create );
    if ( filename == null || filename.length() == 0) return null;
    Uri uri = filenameToUri( docMime(filename), filename, create );
    if ( uri == null ) return null;
    return docFileWriter( uri );
  }

  // -------------------------------------------------------------------------------------
  // private methods to get I/O streams or reader/writer from URI
  //
  static private InputStream docInputStream( Uri uri ) 
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

  static private FileOutputStream docFileOutputStream( Uri uri ) 
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

  static private FileWriter docFileWriter( Uri uri ) 
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

  static public FileInputStream docFileInputStream( Uri uri ) 
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

  static public FileReader docFileReader( Uri uri ) 
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

  public static String docMime( String filename )
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

  public static String getType( Uri uri ) 
  {
    return TDInstance.context.getContentResolver().getType( uri );
  }

  // https://stackoverflow.com/questions/36128077/android-opening-a-file-with-action-get-content-results-into-different-uris
  public static String getPath( Context context, Uri uri )
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // DocumentProvider
      if (DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
          final String docId = DocumentsContract.getDocumentId(uri);
          final String[] split = docId.split(":");
          final String type = split[0];

          if ("primary".equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + split[1];
          }
          // TODO handle non-primary volumes
        } else if (isDownloadsDocument(uri)) { // DownloadsProvider
          final String id = DocumentsContract.getDocumentId(uri);
          final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
          return getDataColumn(context, contentUri, null, null);
        } else if (isMediaDocument(uri)) { // MediaProvider
          final String docId = DocumentsContract.getDocumentId(uri);
          final String[] split = docId.split(":");
          final String type = split[0];
          Uri contentUri = null;
          if ("image".equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
          } else if ("video".equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
          } else if ("audio".equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
          }
          final String selection = "_id=?";
          final String[] selectionArgs = new String[]{split[1]};
          return getDataColumn(context, contentUri, selection, selectionArgs);
        }
      }
    } else if ("content".equalsIgnoreCase(uri.getScheme())) { // MediaStore (and general)
      // Return the remote address
      if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();
      return getDataColumn(context, uri, null, null);
    } else if ("file".equalsIgnoreCase(uri.getScheme())) { // File
      return uri.getPath();
    }
    return null;
  }

  public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
  {
    Cursor cursor = null;
    final String column = "_data";
    final String[] projection = {column};
    try {
     cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
     if (cursor != null && cursor.moveToFirst()) {
       final int index = cursor.getColumnIndexOrThrow(column);
       return cursor.getString(index);
      }
    } finally {
      if (cursor != null) cursor.close();
    }
    return null;
  }

  public static boolean isExternalStorageDocument(Uri uri) {
      return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }
  
  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is DownloadsProvider.
   */
  public static boolean isDownloadsDocument(Uri uri) {
      return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }
  
  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is MediaProvider.
   */
  public static boolean isMediaDocument(Uri uri) {
      return "com.android.providers.media.documents".equals(uri.getAuthority());
  }
  
  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is Google Photos.
   */
  public static boolean isGooglePhotosUri(Uri uri) {
      return "com.google.android.apps.photos.content".equals(uri.getAuthority());
  }

}
