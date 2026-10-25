/* @file SilexGisFiles.java
 *
 * @author apgeo
 * @date oct 2026
 *
 * @brief TopoDroid upload to a SilexGIS server: the files to send and their roles
 *
 * The survey archive is the one "archive", the Therion survey file the "therionSource",
 * the sketches the "therionSketch" files, and every other export an "other" file.
 * The class uses only java, so that it can be tested on the JVM.
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class SilexGisFiles
{
  static final String ROLE_ARCHIVE  = "archive";
  static final String ROLE_TH       = "therionSource";
  static final String ROLE_TH2      = "therionSketch";
  static final String ROLE_OTHER    = "other";

  static final long MAX_FILE_BYTES = 262144000L; // 250 MB, when the server does not say
  static final int  MAX_FILES      = 64;

  // problems of the file list
  static final int OK          = 0;
  static final int NO_ARCHIVE  = 1;
  static final int TOO_LARGE   = 2; // a file is larger than the server accepts
  static final int TOO_MANY    = 3; // more files than the server accepts

  /** a file to upload
   */
  static class Item
  {
    final String name; // name declared to the server
    final File   file; // file on the device
    final long   size;
    final String role;

    Item( String n, File f, String r )
    {
      name = n;
      file = f;
      size = f.length();
      role = r;
    }
  }

  /** a problem of the file list
   */
  static class Problem
  {
    final int kind;
    final String name; // file (null if the problem is not of one file)

    Problem( int k, String n ) { kind = k; name = n; }
  }

  private final ArrayList< Item > mItems = new ArrayList<>();

  /** @return the files, in the order they have been added
   */
  List< Item > items() { return mItems; }

  /** @return the number of files
   */
  int size() { return mItems.size(); }

  /** @return true if a file with the name is in the list (names that differ only in case are the same name)
   * @param name   file name
   */
  boolean contains( String name )
  {
    for ( Item item : mItems ) if ( item.name.equalsIgnoreCase( name ) ) return true;
    return false;
  }

  /** @return true if a file of the role is in the list
   * @param role   role
   */
  boolean hasRole( String role )
  {
    for ( Item item : mItems ) if ( item.role.equals( role ) ) return true;
    return false;
  }

  /** add a file
   * @param name   name declared to the server
   * @param file   file on the device
   * @param role   role
   * @return true if the file has been added, false if it is missing, empty, or a file with that name is already listed
   * @note the server refuses an empty file
   */
  boolean add( String name, File file, String role )
  {
    if ( name == null || file == null || ! file.isFile() || file.length() <= 0 ) return false;
    if ( contains( name ) ) return false;
    if ( ROLE_ARCHIVE.equals( role ) && hasRole( ROLE_ARCHIVE ) ) return false;
    mItems.add( new Item( name, file, role ) );
    return true;
  }

  /** add every other file of a folder (not recursive), as "other" files
   * @param dir       folder
   * @param accepted  extensions the server accepts (lowercase, with the dot); null to accept every extension
   * @return the names of the files left out because the server does not accept their extension
   * @note a ZIP is never an "other" file: the survey archive is the only ZIP of an upload
   */
  List< String > addFolder( File dir, List< String > accepted )
  {
    ArrayList< String > skipped = new ArrayList<>();
    if ( dir == null ) return skipped;
    File[] files = dir.listFiles();
    if ( files == null ) return skipped;
    Arrays.sort( files ); // a stable order
    for ( File file : files ) {
      if ( ! file.isFile() ) continue;
      String name = file.getName();
      if ( contains( name ) ) continue;
      String ext = extension( name );
      if ( ext.equals( ".zip" ) || ( accepted != null && ! accepted.contains( ext ) ) ) {
        skipped.add( name );
        continue;
      }
      add( name, file, ROLE_OTHER );
    }
    return skipped;
  }

  /** remove the "other" files whose extension the server does not accept
   * @param accepted  extensions the server accepts (lowercase, with the dot); null to keep every file
   * @return the names of the files removed
   */
  List< String > dropNotAccepted( List< String > accepted )
  {
    ArrayList< String > ret = new ArrayList<>();
    if ( accepted == null ) return ret;
    for ( int k = mItems.size() - 1; k >= 0; --k ) {
      Item item = mItems.get( k );
      if ( ROLE_OTHER.equals( item.role ) && ! accepted.contains( extension( item.name ) ) ) {
        ret.add( 0, item.name );
        mItems.remove( k );
      }
    }
    return ret;
  }

  /** @return the first problem of the list against the limits of the server, null if none
   * @param max_bytes  largest file the server accepts
   * @param max_files  most files the server accepts
   */
  Problem check( long max_bytes, int max_files )
  {
    if ( ! hasRole( ROLE_ARCHIVE ) ) return new Problem( NO_ARCHIVE, null );
    if ( mItems.size() > max_files ) return new Problem( TOO_MANY, null );
    for ( Item item : mItems ) {
      if ( item.size > max_bytes ) return new Problem( TOO_LARGE, item.name );
    }
    return null;
  }

  /** @return the total size of the files [bytes]
   */
  long totalSize()
  {
    long ret = 0;
    for ( Item item : mItems ) ret += item.size;
    return ret;
  }

  /** @return the extension of a file name, lowercase with the dot, empty if none
   * @param name   file name
   */
  static String extension( String name )
  {
    if ( name == null ) return "";
    int dot = name.lastIndexOf( '.' );
    if ( dot <= 0 || dot == name.length() - 1 ) return "";
    return name.substring( dot ).toLowerCase( Locale.US );
  }

  /** @return a name that is safe as the name of a file on the device: the last segment, without characters a file name cannot have
   * @param name   name given by the server
   * @param dflt   name to use if nothing is left
   */
  static String safeName( String name, String dflt )
  {
    if ( name == null ) return dflt;
    int pos = Math.max( name.lastIndexOf( '/' ), name.lastIndexOf( '\\' ) );
    if ( pos >= 0 ) name = name.substring( pos + 1 );
    StringBuilder sb = new StringBuilder();
    for ( int k = 0; k < name.length(); ++k ) {
      char c = name.charAt( k );
      if ( c < 32 || "/\\:*?\"<>|".indexOf( c ) >= 0 ) {
        sb.append( '_' );
      } else {
        sb.append( c );
      }
    }
    String ret = sb.toString().trim();
    while ( ret.startsWith( "." ) ) ret = ret.substring( 1 );
    return ret.isEmpty()? dflt : ret;
  }

}
