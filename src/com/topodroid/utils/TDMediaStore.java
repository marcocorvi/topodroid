/** @file TDMediaStore.java
 *
 * @author marco corvi
 * @date june 2021
 *
 * @brief TopoDroid MediaStore File layer
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

class TDMediaStore
{ 
  /* =============================================================================
  // MediaStore
  // thanks to https://stackoverflow.com/questions/59511147/create-copy-file-in-android-q-using-mediastore/62879112#62879112
  //
  // MediaStore has an unrecoverable flaw: if the user adds a file without updating the MediaStore database
  // this file is not seen by the MediaStore

  static public boolean isMSexists( String subdir, String filename )
  {
    String dir = "Documents/TopoDroid/" + subdir + "/";
    ContentResolver cr = TDInstance.getContentResolver();
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

    ContentResolver cr = TDInstance.getContentResolver();
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

    ContentResolver cr = TDInstance.getContentResolver();
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
  //   ContentResolver cr = TDInstance.getContentResolver();
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
