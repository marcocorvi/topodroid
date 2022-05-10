/** @file TDsafUri.java
 *
 * @author marco corvi
 * @date june 2021
 *
 * @brief TopoDroid file-n-Uri static method
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.utils.TDLog;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDPath;

import android.os.ParcelFileDescriptor;
import android.os.Build;
import android.os.Environment;
// import android.app.Application;
import android.app.Activity;
import android.content.Context;

import android.content.ContentUris;
import android.content.res.Resources;

import java.util.ArrayList;
import android.provider.DocumentsContract;
import android.net.Uri;
import android.database.Cursor;

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

import android.provider.MediaStore;
// import android.provider.MediaStore.Video;
// import android.provider.MediaStore.Audio;
// import android.provider.MediaStore.Images;

public class TDsafUri
{
  // public static final String HOME_DIR = TDPath.getPathBase(); // "Documents/TopoDroid/"

  // IO STREAMS - static methods -------------------------------------------------------------

  // -------------------------------------------------------------------------------------
  // private methods to get I/O streams or reader/writer from URI
  //

  // UNUSED
  static public ParcelFileDescriptor docWriteFileDescriptor( Uri uri )
  {
    if ( uri != null ) {
      try {
        return TDInstance.getContentResolver().openFileDescriptor( uri, "w" );
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
    return null;
  }

  static public ParcelFileDescriptor docReadFileDescriptor( Uri uri )
  {
    if ( uri != null ) {
      try {
        return TDInstance.getContentResolver().openFileDescriptor( uri, "r" );
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
    return null;
  }

  static public void closeFileDescriptor( ParcelFileDescriptor pfd )
  {
    if ( pfd != null ) {
      try {
        pfd.close();
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
  }

  static public FileInputStream docFileInputStream( ParcelFileDescriptor pfd )
  {
    return (pfd == null)? null : new FileInputStream( pfd.getFileDescriptor() );
  }

  static public FileOutputStream docFileOutputStream( ParcelFileDescriptor pfd )
  {
    return (pfd == null)? null : new FileOutputStream( pfd.getFileDescriptor() );
  }

  static public FileWriter docFileWriter( ParcelFileDescriptor pfd )
  {
    return (pfd == null)? null : new FileWriter( pfd.getFileDescriptor() );
  }

  static public FileReader docFileReader( ParcelFileDescriptor pfd )
  {
    return (pfd == null)? null : new FileReader( pfd.getFileDescriptor() );
  }

  // MIME ---------------------------------------------------------------------

  // UNUSED
  // public static String getFilenameMime( String filename )
  // {
  //   int pos = filename.lastIndexOf('.');
  //   if ( pos > 0 ) {
  //     String ext = filename.substring( pos+1 );
  //     return "application/" + ext;
  //   }
  //   if ( filename.endsWith("/") ) {
  //     return DocumentsContract.Document.MIME_TYPE_DIR;
  //   }
  //   return "application/topodroid";
  // }

  public static String getDocumentType( Uri uri ) 
  {
    return TDInstance.getContentResolver().getType( uri );
  }

  // https://stackoverflow.com/questions/36128077/android-opening-a-file-with-action-get-content-results-into-different-uris
  public static String getDocumentPath( Context context, Uri uri )
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

  // ------------------------------------------------------------------------
  // utils

  private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
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

  private static boolean isExternalStorageDocument(Uri uri) {
      return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }
  
  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is DownloadsProvider.
   */
  private static boolean isDownloadsDocument(Uri uri) {
      return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }
  
  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is MediaProvider.
   */
  private static boolean isMediaDocument(Uri uri) {
      return "com.android.providers.media.documents".equals(uri.getAuthority());
  }
  
  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is Google Photos.
   */
  private static boolean isGooglePhotosUri(Uri uri) {
      return "com.google.android.apps.photos.content".equals(uri.getAuthority());
  }


}
