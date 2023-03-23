/** @file MyFileProvider.java
 
 *
 * This class is adapted from androidx.core.content.FileProvider
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.topodroid.utils;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

// import androidx.annotation.GuardedBy;
// import androidx.annotation.NonNull;
// import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyFileProvider extends ContentProvider 
{
  private static final String[] COLUMNS = { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };

  private static final String META_DATA_FILE_PROVIDER_PATHS = "android.support.FILE_PROVIDER_PATHS";

  private static final String FILEPROVIDER_AUTHORITY = "com.topodroid.fileprovider";

  private static final File DEVICE_ROOT = new File("/");

  private static final String TAG_ROOT_PATH      = "root-path";           // device root "/"
  private static final String TAG_FILES_PATH     = "files-path";          // context.getFilesDir()
  private static final String TAG_CACHE_PATH     = "cache-path";          // context.getCacheDir()
  private static final String TAG_EXTERNAL       = "external-path";       // Environment.getExternalStorageDirectory()
  private static final String TAG_EXTERNAL_FILES = "external-files-path"; // Environment.getExternalFilesDirs( context )
  // private static final String TAG_EXTERNAL_CACHE = "external-cache-path"; // Environment.getExternalCacheDirs( context )
  // private static final String TAG_EXTERNAL_MEDIA = "external-media-path"; // Environment.getExternalMediaDirs( )

  private static final String ATTR_NAME = "name";
  private static final String ATTR_PATH = "path";

  // @GuardedBy("sCache")
  private static HashMap<String, MyPathStrategy> sCache = new HashMap<String, MyPathStrategy>();

  private MyPathStrategy mStrategy;

  /** MyFileProvider does not need to be initialized.
   */
  @Override
  public boolean onCreate() { return true; }

  /** After the MyFileProvider is instantiated, this method is called to provide the system with information about the provider.
   * @param context   current context
   * @param info      new provider info
   */
  @Override
  public void attachInfo( Context context, ProviderInfo info ) 
  {
    super.attachInfo( context, info );
    // Sanity check our security
    if ( info.exported ) throw new SecurityException("Provider must not be exported");
    if ( ! info.grantUriPermissions ) throw new SecurityException("Provider must grant uri permissions");
    mStrategy = getPathStrategy(context, info.authority);
  }

  /** get uri for file using the app fileprovider authority
   * @param context   context
   * @param file      file
   * @return content uri for the given file
   */
  static public Uri fileToUri( Context context, File file )
  {
    return getUriForFile( context, FILEPROVIDER_AUTHORITY, file );
  } 

  /** @return content URI for a given file. 
   * @param context   context
   * @param authority authority of the MyFileProvider
   * @param file      file
   * Specific temporary permissions for the content URI can be 
   *   - set with Context.grantUriPermission(String, Uri, int),
   *   - or added to an Intent by calling Intent.setData(Uri) setData() and then Intent.setFlags(int) setFlags()
   * in both cases, the applicable flags are Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION
   */
  public static Uri getUriForFile( Context context, String authority, File file ) 
  {
    final MyPathStrategy strategy = getPathStrategy( context, authority );
    return strategy.getUriForFile( file );
  }

  /**
   * Use a content URI returned by
   * {@link #getUriForFile(Context, String, File) getUriForFile()} to get information about a file
   * managed by the FileProvider.
   * FileProvider reports the column names defined in {@link android.provider.OpenableColumns}:
   * <ul>
   * <li>{@link android.provider.OpenableColumns#DISPLAY_NAME}</li>
   * <li>{@link android.provider.OpenableColumns#SIZE}</li>
   * </ul>
   * For more information, see
   * {@link ContentProvider#query(Uri, String[], String, String[], String)
   * ContentProvider.query()}.
   *
   * @param uri A content URI returned by {@link #getUriForFile}.
   * @param projection The list of columns to put into the {@link Cursor}. If null all columns are
   * included.
   * @param selection Selection criteria to apply. If null then all data that matches the content
   * URI is returned.
   * @param selectionArgs An array of {@link java.lang.String}, containing arguments to bind to
   * the <i>selection</i> parameter. The <i>query</i> method scans <i>selection</i> from left to
   * right and iterates through <i>selectionArgs</i>, replacing the current "?" character in
   * <i>selection</i> with the value at the current position in <i>selectionArgs</i>. The
   * values are bound to <i>selection</i> as {@link java.lang.String} values.
   * @param sortOrder A {@link java.lang.String} containing the column name(s) on which to sort
   * the resulting {@link Cursor}.
   * @return A {@link Cursor} containing the results of the query.
   *
   */
  @Override
  public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder )
  {
    // ContentProvider has already checked granted permissions
    final File file = mStrategy.getFileForUri( uri );
    if ( projection == null ) projection = COLUMNS;

    String[] cols   = new String[ projection.length ];
    Object[] values = new Object[ projection.length ];
    int i = 0; // number of projections
    for ( String col : projection ) {
      if ( OpenableColumns.DISPLAY_NAME.equals( col ) ) {
        cols[i]     = OpenableColumns.DISPLAY_NAME;
        values[i++] = file.getName();
      } else if ( OpenableColumns.SIZE.equals( col ) ) {
        cols[i]     = OpenableColumns.SIZE;
        values[i++] = file.length();
      }
    }

    cols   = TDUtil.copyOf( cols,   i);
    values = TDUtil.copyOf( values, i);
    final MatrixCursor cursor = new MatrixCursor( cols, 1 );
    cursor.addRow( values );
    return cursor;
  }

  /** @return if uri-file has extension, the string-mime of that extension; otherwise "application/octet-stream"
   * @param uri   content URI returned by getUriForFile()
   * @note ContentProvider has already checked granted permissions
   */
  @Override
  public String getType( Uri uri )
  {
    final File file = mStrategy.getFileForUri(uri);
    final int lastDot = file.getName().lastIndexOf('.');
    if ( lastDot >= 0 ) {
      final String extension = file.getName().substring(lastDot + 1);
      final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
      if (mime != null) return mime;
    }
    return "application/octet-stream";
  }

  /** throws by default 
   */
  @Override
  public Uri insert( Uri uri, ContentValues values )
  {
    throw new UnsupportedOperationException("No external inserts");
  }

  /** throws by default 
   */
  @Override
  public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs ) 
  {
    throw new UnsupportedOperationException("No external updates");
  }

  /** delete the uri-file
   * @param uri            content URI for a file, as returned by getUriForFile()
   * @param selection      unused
   * @param selectionArgs  unused
   * @return 1 if the delete succeeds; otherwise, 0.
   * @note ContentProvider has already checked granted permissions
   */
  @Override
  public int delete( Uri uri, String selection, String[] selectionArgs )
  {
    final File file = mStrategy.getFileForUri( uri );
    return file.delete() ? 1 : 0;
  }

  /** @return ParcelFileDescriptor with which you can access the file.
   * @param uri   content URI associated with a file, as returned by getUriForFile()
   * @param mode  access mode for the file: "r'. "rw" ot "rwt"
   * @note ContentProvider has already checked granted permissions
   */
  @Override
  public ParcelFileDescriptor openFile( Uri uri, String mode ) throws FileNotFoundException
  {
    final File file = mStrategy.getFileForUri( uri );
    final int fileMode = modeToMode( mode );
    return ParcelFileDescriptor.open( file, fileMode );
  }

  /** @return MyPathStrategy for given authority, either by parsing or returning from cache.
   * @param context    context
   * @param authority  authority
   */
  private static MyPathStrategy getPathStrategy( Context context, String authority )
  {
    TDLog.v("Get path strategy for authority " + authority );
    MyPathStrategy strat;
    synchronized ( sCache ) {
      strat = sCache.get( authority );
      if ( strat == null ) {
        try {
          strat = parsePathStrategy( context, authority );
        } catch ( IOException e ) {
          throw new IllegalArgumentException( "Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
        } catch ( XmlPullParserException e ) {
          throw new IllegalArgumentException( "Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
        }
        sCache.put( authority, strat );
      }
    }
    return strat;
  }

  /** parse and return MyPathStrategy for given authority as defined in META_DATA_FILE_PROVIDER_PATHS {@code <meta-data>}.
   * @param context    context
   * @param authority  authority
   */
  private static MyPathStrategy parsePathStrategy( Context context, String authority ) throws IOException, XmlPullParserException
  {
    final MyPathStrategy strat = new MyPathStrategy( authority );
    final ProviderInfo info = context.getPackageManager().resolveContentProvider( authority, PackageManager.GET_META_DATA );
    final XmlResourceParser in = info.loadXmlMetaData( context.getPackageManager(), META_DATA_FILE_PROVIDER_PATHS );
    if ( in == null ) {
        throw new IllegalArgumentException( "Missing " + META_DATA_FILE_PROVIDER_PATHS + " meta-data");
    }
    int type;
    while ( (type = in.next()) != END_DOCUMENT ) {
      if ( type == START_TAG ) {
        final String tag  = in.getName();
        final String name = in.getAttributeValue( null, ATTR_NAME );
        String path       = in.getAttributeValue( null, ATTR_PATH );
        TDLog.v("XML tag " + tag + " name " + name + " path " + path );
        File target = null;
        if ( TAG_ROOT_PATH.equals( tag ) ) {
          target = DEVICE_ROOT;
        } else if ( TAG_FILES_PATH.equals( tag ) ) {
          target = context.getFilesDir();
        } else if ( TAG_CACHE_PATH.equals( tag ) ) {
          target = context.getCacheDir();
        } else if ( TAG_EXTERNAL.equals( tag ) ) {
          target = Environment.getExternalStorageDirectory();
        } else if ( TAG_EXTERNAL_FILES.equals( tag ) ) {
          // File[] externalFilesDirs = ContextCompat.getExternalFilesDirs( context, null );
          // if ( externalFilesDirs.length > 0 ) target = externalFilesDirs[0];
          target = TDFile.getPrivateBaseDir();
        // } else if ( TAG_EXTERNAL_CACHE.equals( tag ) ) {
        //   File[] externalCacheDirs = ContextCompat.getExternalCacheDirs( context );
        //   if ( externalCacheDirs.length > 0 ) target = externalCacheDirs[0];
        // } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && TAG_EXTERNAL_MEDIA.equals( tag ) ) {
        //   File[] externalMediaDirs = context.getExternalMediaDirs();
        //   if ( externalMediaDirs.length > 0 ) target = externalMediaDirs[0];
        } else {
          TDLog.v("Path Strategy unsupported tag " + tag );
        }
        if ( target != null ) {
          strat.addRoot( name, buildPath( target, path ) );
        }
      }
    }
    return strat;
  }

  // PATH STRATEGY --------------------------------------

  static class MyPathStrategy
  {
    private final String mAuthority;
    private final HashMap<String, File> mRoots = new HashMap<String, File>();

    /** cstr
     * @param authority authority
     */
    MyPathStrategy( String authority )
    {
        TDLog.v("My PathStrategy authority " + authority );
        mAuthority = authority;
    }

    /** add an entry to the <name,file> map of roots
     * @param name   key (cannot be empty)
     * @param root   value
     */
    void addRoot( String name, File root )
    {
      if ( TextUtils.isEmpty( name ) ) {
        throw new IllegalArgumentException("Name must not be empty");
      }
      try { // Resolve to canonical path to keep path checking fast
        root = root.getCanonicalFile();
      } catch ( IOException e ) {
        throw new IllegalArgumentException( "Failed to resolve canonical path for " + root, e);
      }
      mRoots.put( name, root );
    }

    public Uri getUriForFile( File file )
    {
      String path;
      try {
        path = file.getCanonicalPath();
      } catch (IOException e) {
        throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
      }
      // Find the most-specific root path
      Map.Entry< String, File > mostSpecific = null;
      for ( Map.Entry< String, File > root : mRoots.entrySet() ) {
        final String root_path = root.getValue().getPath();
        TDLog.v("Check root " + root_path + " for path " + path );
        if ( path.startsWith( root_path ) && ( mostSpecific == null || root_path.length() > mostSpecific.getValue().getPath().length() ) ) {
          mostSpecific = root;
        }
      }
      if ( mostSpecific == null ) {
        throw new IllegalArgumentException( "Failed to find configured root that contains " + path);
      }
      // Start at first char of path under root
      final String root_path = mostSpecific.getValue().getPath();
      if ( root_path.endsWith("/") ) {
        path = path.substring( root_path.length() );
      } else {
        path = path.substring( root_path.length() + 1);
      }
      TDLog.v("Using root " + root_path + " got path " + path );

      // Encode the tag and path separately
      path = Uri.encode( mostSpecific.getKey() ) + '/' + Uri.encode( path, "/" );
      TDLog.v("encoded path " + path );
      return new Uri.Builder().scheme("content").authority( mAuthority ).encodedPath( path ).build();
    }

    public File getFileForUri(Uri uri) 
    {
      String encoded_path  = uri.getEncodedPath();
      final int splitIndex = encoded_path.indexOf('/', 1);
      final String tag     = Uri.decode( encoded_path.substring(1, splitIndex) );
      String path          = Uri.decode( encoded_path.substring(splitIndex + 1));
      TDLog.v("URI encoded path " + path + " decoded " + path + " tag " + tag );

      final File root = mRoots.get(tag);
      if (root == null) throw new IllegalArgumentException("Unable to find configured root for " + uri);
      File file = new File(root, path);
      try {
        file = file.getCanonicalFile();
      } catch (IOException e) {
        throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
      }
      TDLog.v("FILE " + file.getPath() + " root " + root.getPath() );
      if ( ! file.getPath().startsWith( root.getPath() ) ) {
        throw new SecurityException("Resolved path jumped beyond configured root");
      }
      return file;
    }
  }

  // UTILITIES --------------------------------------

  /** Copied from ContentResolver.java
   * @param mode string-mode
   * @return the int-mode for a string-mode
   */
  private static int modeToMode( String mode )
  {
    int modeBits;
    if ("r".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
    } else if ("w".equals(mode) || "wt".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE;
    } else if ("wa".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_APPEND;
    } else if ("rw".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
    } else if ("rwt".equals(mode)) {
      modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE;
    } else {
      throw new IllegalArgumentException("Invalid mode: " + mode);
    }
    return modeBits;
  }

  private static File buildPath( File base, String... segments )
  {
    File cur = base;
    for ( String segment : segments ) {
      if (segment != null) {
        cur = new File(cur, segment);
      }
    }
    return cur;
  }

}
