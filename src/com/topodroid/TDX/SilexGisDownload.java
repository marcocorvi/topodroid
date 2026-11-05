/* @file SilexGisDownload.java
 *
 * @author apgeo
 * @date nov 2026
 *
 * @brief TopoDroid download from a SilexGIS server: where a downloaded result is kept, and opening it
 *
 * A result downloaded from the server goes to the public Download folder of the device: through
 * the MediaStore from Android 10, as a file where the storage permission allows it before. When
 * that fails the file stays in the survey "out" folder, where it was downloaded. Either way it is
 * then opened with the app the system picks for its type.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.MyFileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SilexGisDownload
{
  /** where a downloaded file is kept
   */
  static class Saved
  {
    final Uri uri;          // content URI to open it (null if none could be made)
    final String shown;     // where it is, for the user: "Download/<name>" or the full path
    final String mime;
    final boolean public_downloads; // whether it is in the public Download folder

    Saved( Uri u, String s, String m, boolean d )
    {
      uri   = u;
      shown = s;
      mime  = m;
      public_downloads = d;
    }
  }

  /** put a downloaded file in the public Download folder; if that fails the file stays where it is
   * @param context   context
   * @param file      downloaded file (in the survey "out" folder)
   * @param name      name to give the file
   * @return where the file is kept
   * @note called off the UI thread: the file is copied
   */
  static Saved publish( Context context, File file, String name )
  {
    String mime = SilexGisFiles.mimeType( name );
    Saved saved = null;
    try {
      if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
        saved = toMediaStore( context, file, name, mime );
      } else {
        saved = toPublicFolder( context, file, name, mime );
      }
    } catch ( RuntimeException e ) { // a refusal of the MediaStore or of the storage: the file stays where it is
      TDLog.e( "SilexGIS download not moved to Download: " + e.getMessage() );
      saved = null;
    }
    if ( saved != null ) {
      if ( ! file.delete() ) TDLog.e( "SilexGIS download copy not deleted" );
      return saved;
    }
    return new Saved( fileUri( context, file ), file.getPath(), mime, false );
  }

  /** copy a file into Download through the MediaStore (Android 10 and later: no permission is needed)
   * @return where it is kept, null on failure
   */
  @RequiresApi( api = Build.VERSION_CODES.Q )
  private static Saved toMediaStore( Context context, File file, String name, String mime )
  {
    ContentResolver cr = context.getContentResolver();
    ContentValues cv = new ContentValues();
    cv.put( MediaStore.MediaColumns.DISPLAY_NAME, name );
    cv.put( MediaStore.MediaColumns.MIME_TYPE, mime );
    cv.put( MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS );
    cv.put( MediaStore.MediaColumns.IS_PENDING, 1 ); // hidden from other apps until it is complete
    Uri uri = cr.insert( MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv );
    if ( uri == null ) return null;
    boolean ok = false;
    try ( OutputStream os = cr.openOutputStream( uri ) ) {
      if ( os != null ) {
        copy( file, os );
        ok = true;
      }
    } catch ( IOException e ) {
      TDLog.e( "SilexGIS download to Download failed: " + e.getMessage() );
    }
    if ( ! ok ) {
      cr.delete( uri, null, null );
      return null;
    }
    cv.clear();
    cv.put( MediaStore.MediaColumns.IS_PENDING, 0 );
    cr.update( uri, cv, null, null );
    return new Saved( uri, Environment.DIRECTORY_DOWNLOADS + "/" + displayName( cr, uri, name ), mime, true );
  }

  /** @return the name the MediaStore gave the file: it renames a file whose name is taken
   */
  private static String displayName( ContentResolver cr, Uri uri, String dflt )
  {
    try ( Cursor c = cr.query( uri, new String[] { MediaStore.MediaColumns.DISPLAY_NAME }, null, null, null ) ) {
      if ( c != null && c.moveToFirst() ) {
        String name = c.getString( 0 );
        if ( name != null && ! name.isEmpty() ) return name;
      }
    } catch ( RuntimeException e ) {
      TDLog.e( "SilexGIS download name not read: " + e.getMessage() );
    }
    return dflt;
  }

  /** copy a file into the public Download folder (before Android 10: needs the storage permission)
   * @return where it is kept, null on failure
   */
  @SuppressWarnings( "deprecation" )
  private static Saved toPublicFolder( Context context, File file, String name, String mime )
  {
    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
      && context.checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) return null;
    if ( ! Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) ) return null;
    File dir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
    if ( dir == null || ( ! dir.isDirectory() && ! dir.mkdirs() ) ) return null;
    File dest = SilexGisClient.newFile( dir, name );
    try ( OutputStream os = new FileOutputStream( dest ) ) {
      copy( file, os );
    } catch ( IOException e ) {
      TDLog.e( "SilexGIS download to Download failed: " + e.getMessage() );
      if ( dest.exists() && ! dest.delete() ) TDLog.e( "SilexGIS partial download not deleted" );
      return null;
    }
    Uri uri = fileUri( context, dest );
    if ( uri == null ) { // it could not be opened from there: it stays in the "out" folder
      if ( ! dest.delete() ) TDLog.e( "SilexGIS download copy not deleted" );
      return null;
    }
    MediaScannerConnection.scanFile( context, new String[] { dest.getPath() }, new String[] { mime }, null ); // listed by the Downloads app
    return new Saved( uri, dest.getPath(), mime, true );
  }

  /** @return the URI to open a file with another app, null if none can be made
   * @param context   context
   * @param file      file (in a folder of the file provider paths)
   */
  private static Uri fileUri( Context context, File file )
  {
    try {
      return MyFileProvider.fileToUri( context, file );
    } catch ( IllegalArgumentException e ) { // not in a folder the provider shares
      TDLog.e( "SilexGIS download not shareable: " + e.getMessage() );
      return TDandroid.BELOW_API_24 ? Uri.fromFile( file ) : null; // a file URI is refused from Android 7
    }
  }

  /** copy a file to a stream
   */
  private static void copy( File file, OutputStream os ) throws IOException
  {
    try ( InputStream is = new FileInputStream( file ) ) {
      byte[] buf = new byte[ 65536 ];
      int n;
      while ( ( n = is.read( buf ) ) > 0 ) os.write( buf, 0, n );
    }
  }

  /** open a downloaded file with the app the system picks for its type, or the user picks
   * @param context   activity context
   * @param saved     where the file is kept
   * @param name      file name, for the message
   * @return true if an app was started
   */
  static boolean open( Context context, Saved saved, String name )
  {
    if ( saved.uri != null ) {
      Intent intent = new Intent( Intent.ACTION_VIEW );
      intent.setDataAndType( saved.uri, saved.mime );
      intent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
      try {
        context.startActivity( intent );
        return true;
      } catch ( ActivityNotFoundException e ) {
        TDLog.e( "SilexGIS no app opens " + saved.mime );
      } catch ( SecurityException e ) {
        TDLog.e( "SilexGIS download not opened: " + e.getMessage() );
      }
    }
    TDToast.makeWarn( String.format( context.getResources().getString( R.string.silexgis_open_no_app ), name ) );
    return false;
  }

}
