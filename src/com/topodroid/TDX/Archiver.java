/* @file Archiver.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey archiver
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDVersion;
import com.topodroid.prefs.TDSetting;
// import com.topodroid.common.PlotType;

import android.os.ParcelFileDescriptor;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.StringReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.Enumeration;
// import java.util.zip.DataFormatException;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
// import java.util.zip.Inflater;
import java.util.zip.ZipFile;

// import java.nio.charsets.StandardCharsets; // API-19

import java.util.List;
// import java.util.Locale;
// import java.util.ArrayList;

// import android.content.Context;
// import android.content.Intent;

public class Archiver
{
  private static final int BUF_SIZE = 4096;
  private byte[] data; // = new byte[ BUF_SIZE ];

  private static int mManifestDbVersion = 0;
  private static String mManifestSurveyname = null;


  private String mZipname;
  // private static String mManifestPath = null;

  /** cstr
   */
  public Archiver( ) // TopoDroidApp app
  {
    data = new byte[ BUF_SIZE ];
  }

  /** add an entry to the ZIP archive
   * @param zos       zip output stream
   * @param name      zip-entry file
   * @param filepath  zip-entry pathname
   * @return true if successful
   */
  private boolean addEntry( ZipOutputStream zos, File name, String filepath )
  {
    if ( name == null || ! name.exists() ) return false;
    boolean ret = false;
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "ZIP add file " + name.getPath() );
      bis = new BufferedInputStream( TDFile.getFileInputStream( filepath ), BUF_SIZE );
      ZipEntry entry = new ZipEntry( name.getName() );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
        zos.write( data, 0, cnt );
      }
      zos.closeEntry( );
      ret = true;
    } catch (FileNotFoundException e ) {
      TDLog.Error( "ZIP 1 file not found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "ZIP 1 IO error " + e.getMessage() );
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { /* ret = false; */ }
    }
    // TDLog.v( "ZIP add file " + name.getPath() + " return " + ret );
    return ret;
  }

  /** add an entry to the ZIP archive
   * @param zos       zip output stream
   * @param subdir    entry-file subdirectory 
   * @param filename  zip-entry filename
   * @return true if successful
   */
  private boolean addEntry( ZipOutputStream zos, String subdir, String filename )
  {
    // TDLog.v( "ZIP add entry. subdir: " + subdir + " filename: " + filename );
    boolean ret = false;
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "ZIP add file " + file.getPath() );
      bis = new BufferedInputStream( TDFile.getMSinput( subdir, filename, "application/octet-stream" ), BUF_SIZE );
      ZipEntry entry = new ZipEntry( filename );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
        zos.write( data, 0, cnt );
      }
      zos.closeEntry( );
      ret = true;
    } catch (FileNotFoundException e ) {
      TDLog.Error( "ZIP 2 file not found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "ZIP 2 IO error " + e.getMessage() );
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { /* ret = false; */ }
    }
    return ret;
  }

  /** add an optional entry to the ZIP archive
   * @param zos       zip output stream
   * @param name      entry name
   * @param filepath  zip-entry pathname
   */
  private void addOptionalEntry( ZipOutputStream zos, File name, String filepath )
  {
    if ( name == null || ! name.exists() ) return;
    // TDLog.v( "ZIP optional file " + name.getPath() );
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "ZIP add file " + name.getPath() );
      bis = new BufferedInputStream( TDFile.getFileInputStream( filepath ), BUF_SIZE );
      ZipEntry entry = new ZipEntry( name.getName() );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
        zos.write( data, 0, cnt );
      }
      zos.closeEntry( );
    } catch (FileNotFoundException e ) {
      TDLog.Error( "ZIP 3 file not found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "ZIP 3 IO error " + e.getMessage() );
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { TDLog.v("ZIP IO " + e.getMessage() ); }
    }
  }

  /** add a directory to the ZIP archive
   * @param zos       zip output stream
   * @param dir       directory
   * @param dirname   directory name
   */
  private void addDirectory( ZipOutputStream zos, File dir, String dirname )
  {
    if ( ! dir.exists() ) return;
    // TDLog.v( "ZIP add dir " + dir.getPath() );
    File[] files = dir.listFiles();
    if ( files != null ) {
      for ( File file : files ) { // listFiles MAY NullPointerException
        if (file.isFile()) addOptionalEntry(zos, file, file.getPath() ); 
      }
    }
  }

  // /** used to compress shapefiles
  //  * @param zipdir    zip folder
  //  * @param zipname   zip-filename
  //  * @param subdir    files subdirectory
  //  * @param filenames file names
  //  * @return true if successful
  //  */
  // public boolean compressFiles( String zipdir, String zipname, String subdir, List< String > filenames )
  // {
  //   // here zipname is the full absolute zipfile path
  //   // TDLog.v( "ZIP-compress files to " + zipdir + " " + zipname );
  //   ZipOutputStream zos = null;
  //   boolean ret = true;
  //   try {
  //     zos = new ZipOutputStream( new BufferedOutputStream( TDFile.getMSoutput( zipdir, zipname, "application/octet-stream" ) ) );
  //     for ( String filename : filenames ) {
  //       // the file.getPath() is the full absolute file path
  //       // TDLog.v( "ZIP-compress add file " + file.getPath() );
  //       ret &= addEntry( zos, subdir, filename );
  //     }
  //     // for ( File file : files ) TDFile.deleteFile( file );
  //   } catch ( FileNotFoundException e ) {
  //     TDLog.Error( "ZIP 4 file not found " + e.getMessage() );
  //   } catch ( IOException e ) {
  //     TDLog.Error( "ZIP 4 IO error " + e.getMessage() );
  //     // FIXME
  //   } finally {
  //     if ( zos != null ) try { zos.close(); } catch ( IOException e ) { 
  //       ret = false;
  //       // TDLog.Error("ZIP compress close error");
  //     }
  //   }
  //   return ret;
  // }

  /** compress a set of files from a subdirectory
   * @param os        output stream
   * @param subdir    subdirectory
   * @param filenames files to compress
   * @return true if successful
   */
  public boolean compressFiles( OutputStream os, String subdir, List< String > filenames )
  {
    // here zipname is the full absolute zipfile path
    // TDLog.v( "ZIP-compress files. subdir " + subdir );
    ZipOutputStream zos; //  = null;
    boolean ret = true;
    // try {
      zos = new ZipOutputStream( new BufferedOutputStream( os ) );
      for ( String filename : filenames ) {
        // the file.getPath() is the full absolute file path
        // TDLog.v( "ZIP-compress files: add file " + filename );
        ret &= addEntry( zos, subdir, filename );
      }
      // for ( File file : files ) TDFile.deleteFile( file );
    // } catch ( FileNotFoundException e ) {
    //   TDLog.Error( "ZIP 5 file not found " + e.getMessage() );
    // } catch ( IOException e ) {
    //   TDLog.Error( "ZIP 5 IO error " + e.getMessage() );
    //   FIXME
    // } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { 
        ret = false;
        // TDLog.Error("ZIP compress close error");
      }
    // }
    return ret;
  }

  /** compress symbol files
   * @param zipfile  compressed zip file
   * @param lib      symbol library
   * @param type     symbols type
   * @return true if successful
   */
  private boolean compressSymbols( File zipfile, SymbolLibrary lib, String type )
  {
    if ( lib == null ) return false;
    if ( ! (TDFile.getPrivateDir( type )).exists() ) return false;
    // TDLog.v( "ZIP symbols zip " + zipfile.getPath() );
    List< Symbol > symbols = lib.getSymbols();
    ZipOutputStream zos = null;
    try { 
      zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( zipfile ) ) );
      for ( Symbol symbol : symbols ) {
        if ( symbol.mEnabled ) {
          String filename = symbol.getThName();
          // THERION-U: filename = Symbol.deprefix_u( filename );
          String filepath = type + "/" + filename;
          // TDLog.v( "ZIP symbols compress " + type + " " + filepath );
          addOptionalEntry( zos, TDFile.getPrivateFile( type, filename ), filepath );
        }
      }
    } catch ( FileNotFoundException e ) {
      return false;
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { TDLog.Error("ZIP-symbol close error"); }
    }
    return true;
  }

  /** uncompress symbol files
   * @param zin      compressed input stream
   * @param type     symbols type
   * @param prefix   symbol prefix in the database config table
   * @return true is a symbol has been uncompressed
   */
  static private boolean uncompressSymbols( InputStream zin, String type, String prefix )
  {
    if ( ! (TDLevel.overExpert && TDSetting.mZipWithSymbols ) ) return false;
    boolean ret = false;
    // TDLog.v( "ZIP-uncompress symbol type " + type + " prefix " + prefix );
    File tempfile = TDFile.getExternalFile( null, "tmp.zip" );
    FileOutputStream fout; // = null;
    int c;
    byte[] sbuffer = new byte[4096];
    try { 
      fout = new FileOutputStream( tempfile );
      while ( ( c = zin.read( sbuffer ) ) != -1 ) fout.write(sbuffer, 0, c);
      fout.close();
      // fout = null;
      // uncompress symbols zip
      ZipEntry sze;
      FileInputStream fis = new FileInputStream( tempfile );
      ZipInputStream szin = new ZipInputStream( fis );
      while ( ( sze = szin.getNextEntry() ) != null ) {
        File symbolfile = TDFile.getPrivateFile( type, sze.getName() );
        // TDLog.v( "ZIP try to uncompress symbol " + type + " " + sze.getName() );
        if ( ! symbolfile.exists() ) { // don't overwrite
          // TDLog.v( "ZIP-uncompress symbol " + symbolfile.getPath() );
          // FileOutputStream sfout = TDFile.getFileOutputStream( symbolfilename ); // uncompress symbols zip
          FileOutputStream sfout = new FileOutputStream( symbolfile ); 
          while ( ( c = szin.read( sbuffer ) ) != -1 ) sfout.write(sbuffer, 0, c);
          sfout.close();
          ret = true;
          // add symbol to library and enable it
        }
        szin.closeEntry();
        // need to get the thname from the file
        // FileInputStream sfis = TDFile.getFileInputStream( symbolfilename );
        FileInputStream sfis = new FileInputStream( symbolfile );
        BufferedReader br = new BufferedReader( new InputStreamReader( sfis, "UTF-8" /* StandardCharsets.UTF_8 */ ) ); // String iso = "UTF-8";
        String line;
        while ( (line = br.readLine()) != null ) {
          line = line.trim();
          if ( line.startsWith("th_name") ) {
            String th_name = line.substring(8).trim();
            // TDLog.v( "ZIP enable " + th_name );
            TopoDroidApp.mData.setSymbolEnabled( prefix + th_name, true );
            break;
          }
        }
        sfis.close();
      }
      fis.close();
    } catch ( FileNotFoundException e1 ) { 
      TDLog.v( "ZIP 9 file not found " + e1.getMessage() );
    } catch ( IOException e2 ) {
      TDLog.v( "ZIP 9 IO error " + e2.getMessage() );
    } finally {
      TDFile.deleteFile( tempfile );
    }
    return ret;
  }

  /** archive the current survey - compress to the default zip file
   * @param app   application
   * @param uri   output URI (if null the default output zipfile is used)
   * @return true if successful
   */
  boolean archive( TopoDroidApp app, Uri uri )
  {
    if ( TDInstance.sid < 0 ) return false;
    DataHelper app_data = TopoDroidApp.mData;
    // if ( app_data == null ) return false;
    
    // File temp = null;
    String survey = TDInstance.survey;
    boolean ret = true;

    // TDLog.Log( TDLog.LOG_IO, "ZIP export file: " + zipname );
    // TDLog.v( "ZIP export file: " + zipname + " pre " + ret );

    ParcelFileDescriptor pfd = null;
    ZipOutputStream zos = null;
    try {
      String pathname;
      if ( uri == null ) {
        mZipname = TDPath.getSurveyZipFile( survey );
        TDPath.checkPath( mZipname );
        zos = new ZipOutputStream( new BufferedOutputStream( TDFile.getFileOutputStream( mZipname ) ) );
      } else {
        pfd = TDsafUri.docWriteFileDescriptor( uri );
        zos = new ZipOutputStream( new BufferedOutputStream( TDsafUri.docFileOutputStream( pfd ) ) );
      }

      pathname = TDPath.getManifestFile( ); // The first entry must be the manifest 
      app.writeManifestFile();
      ret &= addEntry( zos, TDFile.getTopoDroidFile(pathname), pathname );
      // TDLog.v("ZIP archive post-manifest returns " + ret );

      pathname = TDPath.getSqlFile( );
      app_data.dumpToFile( pathname, TDInstance.sid );
      ret &= addEntry( zos, TDFile.getTopoDroidFile(pathname), pathname );
      // TDLog.v("ZIP archive post-sqlite returns " + ret );

      pathname = TDPath.getSurveyNoteFile( survey );
      addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );

      if ( TDLevel.overExpert && TDSetting.mZipWithSymbols ) {
        File file = TDFile.getExternalFile( null, "points.zip" );
        if ( compressSymbols( file, BrushManager.getPointLib(), TDPath.getSymbolPointDirname() ) )  {
          addOptionalEntry( zos, file, "points.zip" );
        }
        file = TDFile.getExternalFile( null, "lines.zip" );
        if ( compressSymbols( file, BrushManager.getLineLib(), TDPath.getSymbolLineDirname() ) )  {
          addOptionalEntry( zos, file, "lines.zip" );
        }
        file = TDFile.getExternalFile( null, "areas.zip" );
        if ( compressSymbols( file, BrushManager.getAreaLib(), TDPath.getSymbolAreaDirname() ) )  {
          addOptionalEntry( zos, file, "areas.zip" );
        }
      }

/* FIXME_SKETCH_3D *
      List< Sketch3dInfo > sketches  = app_data.selectAllSketches( TDInstance.sid, TDStatus.NORMAL );
      for ( Sketch3dInfo skt : sketches ) {
        ret &= addEntry( zos, TDFile.getTopoDroidFile( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
      sketches  = app_data.selectAllSketches( TDInstance.sid, TDStatus.DELETED );
      for ( Sketch3dInfo skt : sketches ) {
        ret &= addEntry( zos, TDFile.getTopoDroidFile( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
/* END_SKETCH_3D */

      List< PlotInfo > plots  = app_data.selectAllPlots( TDInstance.sid, TDStatus.NORMAL );
      for ( PlotInfo plt : plots ) {
        pathname = TDPath.getSurveyPlotTdrFile( survey, plt.name ); // N.B. plot file CAN be missing
        addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      }

      plots  = app_data.selectAllPlots( TDInstance.sid, TDStatus.DELETED );
      for ( PlotInfo plt : plots ) {
        pathname = TDPath.getSurveyPlotTdrFile( survey, plt.name );
        addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      }

      pathname = TDPath.getSurveyPhotoDir( survey );
      addDirectory( zos, TDFile.getTopoDroidFile( pathname ), pathname );

      pathname = TDPath.getSurveyAudioDir( survey );
      addDirectory( zos, TDFile.getTopoDroidFile( pathname ), pathname );

      // ret = true;
    } catch ( FileNotFoundException e ) {
      TDLog.v("ZIP 6 file not found " + e.getMessage() );
      ret = false;
    } catch ( IOException e ) {
      TDLog.v("ZIP 6 IO error " + e.getMessage() );
      // FIXME
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { TDLog.Error("ZIP 6 close error"); }
      if ( pfd != null ) TDsafUri.closeFileDescriptor( pfd );
      TDFile.deleteFile( TDPath.getSqlFile() );
    }
    // TDLog.v("ZIP archive returns " + ret );
    return ret;
  }

  /** decompress a zip entry
   * @param zin    zip input stream
   * @param ze     zip entry
   * @param fout   entry output stream
   * @return ...
   */
  private static int decompressEntry( InputStream zin, ZipEntry ze, FileOutputStream fout )
  {
    // int csize = (int)ze.getCompressedSize(); // cannot rely on sizes attributes
    // int dsize = (int)ze.getSize();
    // TDLog.v( "decompress entry: " + ze.getName() );
    byte[] data = new byte[ 4096 ];
    int size = 0;
    try {
      int c;
      while ( ( c = zin.read( data ) ) != -1 ) {
        fout.write(data, 0, c);
        size += c;
      }
      // Inflater decompresser = new Inflater();
      // decompresser.setInput( data, 0, csize );
      // buffer = new byte[ dsize ];
      // size = decompresser.inflate( buffer );
      // decompresser.end();
    } catch ( IOException e ) {
      TDLog.Error("ZIP decompress entry: " + e.getMessage() );
      return -1;
    }
    // TDLog.v( "decompress entry: size " + size );
    return size;
  }

  private static int decompressEntry( InputStream zin, ZipEntry ze, ByteArrayOutputStream bout )
  {
    byte[] data = new byte[ 4096 ];
    int size = 0;
    try {
      int c;
      while ( ( c = zin.read( data ) ) != -1 ) {
        bout.write(data, 0, c);
        size += c;
      }
    } catch ( IOException e ) {
      TDLog.Error("ZIP decompress entry to byte array: " + e.getMessage() );
      return -1;
    }
    return size;
  }

  /** check a  manifest file
   * @param manifest   content of manifest 
   * @return
   *  >=0 ok
   * -1 survey already present
   * -2 TopoDroid version mismatch
   * -3 database version mismatch: manifest_DB_version < min_DB_version
   * -4 database version mismatch: manifest_DB_version > current DB_version
   * -5 survey name does not match filename
   * -10 number format error
   * -11 file not found
   * -12 IO error
   */
  static private int checkManifestFile( TopoDroidApp app, String manifest )
  {
    mManifestDbVersion = 0;
    String line;
    int version_code = 0;
    int ret = -1;
    try {
      // FileReader fr = TDFile.getFileReader( filename );
      StringReader fr = new StringReader( manifest );
      BufferedReader br = new BufferedReader( fr );
      // first line is version
      line = br.readLine().trim();
      ret = checkVersionLine( line );
      if ( ret < 0 ) return ret;

      line = br.readLine().trim();
      try {
        mManifestDbVersion = Integer.parseInt( line );
      } catch ( NumberFormatException e ) {
        TDLog.Error( "MANIFEST DB version format error: " + line );
        return -10;
      }
      
      if ( ! ( mManifestDbVersion >= TDVersion.DATABASE_VERSION_MIN ) ) {
        TDLog.Error( "MANIFEST DB version mismatch: found " + mManifestDbVersion + " min " + + TDVersion.DATABASE_VERSION_MIN );
        return -3;
      }
      if ( ! ( mManifestDbVersion <= TDVersion.DATABASE_VERSION ) ) {
        TDLog.Error( "MANIFEST DB version mismatch: found " + mManifestDbVersion + " current " + TDVersion.DATABASE_VERSION );
        return -4;
      }

      mManifestSurveyname = br.readLine().trim();
      TDLog.v("MANIFEST read <" + mManifestSurveyname + ">" );
      if ( app.mData.hasSurveyName( mManifestSurveyname ) ) {
        TDLog.Error( "MANIFEST survey exists: <" + mManifestSurveyname + ">" );
        return -1;
      }
      // fr.close();
    } catch ( NumberFormatException e ) {
      TDLog.Error( "MANIFEST error: " + e.getMessage() );
      return -10;
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "MANIFEST file not found: " + e.getMessage() );
      return -11;
    } catch ( IOException e ) {
      TDLog.Error( "MANIFEST I/O error: " + e.getMessage() );
      return -12;
    }
    return ret;
  }

  /** check the version line of a manifest file - called by checkManifestFile
   * @param version_line version line
   * @return
   *   -10 number format error
   *   -2  version is too old
   *   0   version is in acceptable range
   *   1   version is newer than this app
   */
  static private int checkVersionLine( String version_line )
  {
    int ret = 0;
    int version_code = 0;
    String[] vers = version_line.split(" ");
    for ( int k=1; k<vers.length; ++ k ) {
      if ( vers[k].length() > 0 ) {
        try {
          version_code = Integer.parseInt( vers[k] );
          break;
        } catch ( NumberFormatException e ) { 
          // this is OK
        }
      }
    }
    if ( version_code == 0 ) {
      String[] ver = vers[0].split("\\.");
      int major = 0;
      int minor = 0;
      int sub   = 0;
      char vch  = ' '; // char order: ' ' < A < B < ... < a < b < ... < '}' 
      if ( ver.length > 2 ) { // M.m.sv version code
        try {
          major = Integer.parseInt( ver[0] );
          minor = Integer.parseInt( ver[1] );
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse error: major/minor " + ver[0] + " " + ver[1] );
          return -10;
        }
        int k = 0;
        while ( k < ver[2].length() ) {
          char ch = ver[2].charAt(k);
          if ( ch < '0' || ch > '9' ) { vch = ch; break; }
          sub = 10 * sub + (int)(ch - '0');
          ++k;
        }
        // TDLog.v( "Version " + major + " " + minor + " " + sub );
        if (    ( major <  TDVersion.MAJOR_MIN )
             || ( major == TDVersion.MAJOR_MIN && minor < TDVersion.MINOR_MIN )
             || ( major == TDVersion.MAJOR_MIN && minor == TDVersion.MINOR_MIN && sub < TDVersion.SUB_MIN ) 
          ) {
          TDLog.Error( "TopoDroid version mismatch: " + version_line + " < " + TDVersion.MAJOR_MIN + "." + TDVersion.MINOR_MIN + "." + TDVersion.SUB_MIN );
          return -2;
        }
        if (    ( major > TDVersion.MAJOR ) 
             || ( major == TDVersion.MAJOR && minor > TDVersion.MINOR )
             || ( major == TDVersion.MAJOR && minor == TDVersion.MINOR && sub > TDVersion.SUB ) ) {
          ret = 1; 
        } else if ( major == TDVersion.MAJOR && minor == TDVersion.MINOR && sub == TDVersion.SUB && vch > ' ' ) {
          if ( TDVersion.VCH == ' ' ) { 
            ret = 1;
          } else if ( TDVersion.VCH <= 'Z' && ( vch >= 'a' || vch < TDVersion.VCH ) ) { // a-z or vch(A-Z) < VCH
            ret = 1;
          } else if ( TDVersion.VCH >= 'a' && vch < TDVersion.VCH ) { // A-Z < a-z 
            ret = 1;
          }
        }

      } else { // version code
        try {
          version_code = Integer.parseInt( ver[0] );
          if ( version_code < TDVersion.CODE_MIN ) {
            TDLog.Error( "TopoDroid version mismatch: " + version_line + " < " + TDVersion.CODE_MIN );
            return -2;
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( "parse error: version code " + ver[0] + " " + e.getMessage() );
          return -10;
        }
        if ( version_code > TDVersion.VERSION_CODE ) ret = 1;
      }
    } else {
      if ( version_code > TDVersion.VERSION_CODE ) ret = 1;
    }
    return ret;
  }

  public int unArchive( TopoDroidApp app, String filename, boolean force )
  {
    TDLog.v("ZIP 7 un-archive file " + filename );
    boolean sql_success = false;
    int ok_manifest = -2;
    String pathname;
    // mManifestPath = null;
    DataHelper app_data = TopoDroidApp.mData;
    try {
      // byte buffer[] = new byte[36768];
      // byte[] buffer = new byte[4096];
      ZipEntry ze;
      // TDLog.Log( TDLog.LOG_ZIP, "unzip " + filename );
      ZipFile zip = new ZipFile( filename );
      ze = zip.getEntry( "manifest" );
      if ( ze == null ) return -2;
      // pathname = TDPath.getManifestFile( );
      // TDPath.checkPath( pathname );
      // FileOutputStream fout = TDFile.getFileOutputStream( pathname );
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      InputStream zin = zip.getInputStream( ze );
      int msize = decompressEntry( zin, ze, bout );
      // fout.close();
      if ( msize > 0 ) {
        // int c; while ( ( c = is.read( mbuffer ) ) != -1 ) fout.write(mbuffer, 0, c);
        ok_manifest = checkManifestFile( app, bout.toString() );
        TDLog.v( "ZIP 7 manifest: " + ze.getName() + " size " + msize + " ok " + ok_manifest + " survey " + mManifestSurveyname );
      }
      // TDFile.deleteFile( pathname );
      // TDLog.Log( TDLog.LOG_ZIP, "un-archived manifest " + ok_manifest );
      if ( ok_manifest < 0 ) {
        TDLog.v("ZIP 7 bad manifest " + ok_manifest );
        if ( ! force ) {
          zip.close();
          return ok_manifest;
        }
      }
      // byte buffer[] = new byte[36768];
      // byte[] buffer = new byte[4096];
      // int nr_entry = 0;
      Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>)zip.entries(); // FIXME "unchecked cast"
      for ( ; entries.hasMoreElements(); ) {
        ze = entries.nextElement();
        // TDLog.v("ZIP 7 entry " + ze.getName() );
        // ++ nr_entry;
        if ( ze.isDirectory() ) {
          TDFile.makeTopoDroidDir( TDPath.getDirFile( ze.getName() ) );
        } else if ( ze.getName().equals( "manifest" ) ) {
          // skip
        } else {
          zin = zip.getInputStream( ze );
          // TDLog.Log( TDLog.LOG_ZIP, "Zip entry " + nr_entry + " \"" + ze.getName() + "\"" );
          boolean sql = false;
          pathname = null;
          if ( ze.getName().equals( "survey.sql" ) ) {
            pathname = TDPath.getSqlFile();
            sql = true;
          // /* FIXME_SKETCH_3D *
          // } else if ( ze.getName().endsWith( TDPath.TH3 ) ) {
          //   pathname = TDPath.getTh3File( ze.getName() );
          // * END_SKETCH_3D */
          } else if ( ze.getName().endsWith( TDPath.TDR ) ) { // PLOTS
            pathname = TDPath.getTdrFile( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TXT ) ) { // NOTES
            pathname = TDPath.getNoteFile( ze.getName() );
          } else if ( ze.getName().endsWith( ".wav" ) ) { // AUDIOS
            pathname = TDPath.getSurveyAudioDir( mManifestSurveyname );
            TDFile.makeTopoDroidDir( pathname );
            pathname = TDPath.getSurveyAudioFile( mManifestSurveyname, ze.getName() );
          } else if ( ze.getName().endsWith( ".jpg" ) ) { // PHOTOS
            // FIXME need survey dir
            pathname = TDPath.getSurveyPhotoDir( mManifestSurveyname );
            TDFile.makeTopoDroidDir( pathname );
            pathname = TDPath.getSurveyJpgFile( mManifestSurveyname, ze.getName() );
          } else if ( ze.getName().equals( "points.zip" ) ) { // POINTS
            if ( uncompressSymbols( zin, TDPath.getSymbolPointDirname(), "p_" ) ) {
              BrushManager.reloadPointLibrary( app, app.getResources() );
            }
          } else if ( ze.getName().equals( "lines.zip" ) ) { // LINES
            if ( uncompressSymbols( zin, TDPath.getSymbolLineDirname(), "l_" ) ) {
              BrushManager.reloadLineLibrary( app.getResources() );
            }
          } else if ( ze.getName().equals( "areas.zip" ) ) { // AREAS
            if ( uncompressSymbols( zin, TDPath.getSymbolAreaDirname(), "a_" ) ) {
              BrushManager.reloadAreaLibrary( app.getResources() );
            }
          } else {
            TDLog.Error("ZIP 7 unexpected file type " + ze.getName() );
            // pathname = null; // already null
          }
          if ( pathname != null ) {
            TDPath.checkPath( pathname );
            FileOutputStream fout = TDFile.getFileOutputStream( pathname );
            int size = decompressEntry( zin, ze, fout );
            // TDLog.Log( TDLog.LOG_ZIP, "Unzip file \"" + pathname + "\" size " + size );
            TDLog.v( "ZIP 7 file " + pathname + " size " + size );
            fout.close();
            if ( size > 0 ) {
              if ( sql ) {
                // TDLog.Log( TDLog.LOG_ZIP, "Zip sqlfile \"" + pathname + "\" DB version " + mManifestDbVersion );
                TDLog.v( "ZIP 7 sqlfile " + pathname + " DB version " + mManifestDbVersion );
                sql_success = ( app_data.loadFromFile( pathname, mManifestDbVersion ) >= 0 );
                TDFile.deleteFile( pathname );
              }
            } else {
              TDFile.deleteFile( pathname );
              TDLog.Error("ZIP 7 failed " + pathname + " ret " + size );
            }
          }
        }
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "ZIP 7 file not found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "ZIP 7 IO: " + e.getMessage() );
    } catch ( ClassCastException e ) {
      TDLog.Error( "ZIP 7 cast: " + e.getMessage() );
    }
    if ( ok_manifest == 0 && ! sql_success ) {
      TDLog.Error( "ZIP 7 sql error" );
      // tell user that there was a problem
      return -5;
    }
    return ok_manifest; // return 0 or 1
  }

  /** check if manifest file is OK
   * @param app   application
   * @param fis   zip archive input stream
   * @note called by MainWindow
   */
  static public int getOkManifest( TopoDroidApp app, InputStream fis )
  {
    int ok_manifest = -2;
    ZipEntry ze;
    // mManifestPath = null;
    try {
      ZipInputStream zin = new ZipInputStream( fis );
      // int nr_entry = 0;
      while ( ( ze = zin.getNextEntry() ) != null ) {
        // TDLog.v( "ZIP get OK manifest: zentry name " + ze.getName() );
        if ( ze.getName().equals( "manifest" ) ) {
          // String pathname = TDPath.getManifestFile( );
          // TDLog.v( "OK manifest: pathname " + pathname + " entry \"" + ze.getName() + "\"");
          // FileOutputStream fout = TDFile.getFileOutputStream( pathname );
          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          int size = decompressEntry( zin, ze, bout );
          // TDLog.Log( TDLog.LOG_ZIP, "Zip manifest: \"" + ze.getName() + "\" size " + size );
          if ( size > 0 ) {
            ok_manifest = checkManifestFile( app, bout.toString() );
            TDLog.v( "ZIP manifest [1]: \"" + ze.getName() + "\" size " + size + " ok " + ok_manifest + " survey " + mManifestSurveyname );
          } else {
            TDLog.Error( "ZIP manifest: \"" + ze.getName() + "\" size " + size );
          }
          // TDFile.deleteFile( pathname );
          if ( ok_manifest < 0 ) return ok_manifest;
          // TDLog.Log( TDLog.LOG_ZIP, "un-archive manifest " + ok_manifest );
          break;
        }
        zin.closeEntry();
      }
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "ZIP 11 file not found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "ZIP 11 IO error " + e.getMessage() );
    }
    return ok_manifest;
  }

  /** un-archive from an input stream
   * @param app        TopoDroid application
   * @param fis        input stream
   *
   * When this method is called the manifest has been checked and is OK
   * This method sets the survey path by the passed survey-name at start, and resets it to null at the end
   */
  static public int unArchive( TopoDroidApp app, InputStream fis )
  {
    int ok_manifest = 0;
    String pathname;
    ZipEntry ze;
    DataHelper app_data = TopoDroidApp.mData;
    TDPath.setSurveyPaths( mManifestSurveyname );

    try {
      // byte buffer[] = new byte[36768];
      // byte[] buffer = new byte[4096];

      // FileInputStream fis = TDFile.getFileInputStream( filename );
      
      ZipInputStream zin = new ZipInputStream( fis );
      // int nr_entry = 0;
      while ( ( ze = zin.getNextEntry() ) != null ) {
        // TDLog.v( "ZIP 8 entry " + ze.getName() ); // entry names do not have directory but only filename with extension
        // ++ nr_entry;
        if ( ze.isDirectory() ) {
          // TDLog.v( "ZIP 8 dir entry " + nr_entry + " \"" + ze.getName() + "\"");
          TDFile.makeTopoDroidDir( TDPath.getDirFile( ze.getName() ) );
        } else if ( ze.getName().equals( "manifest" ) ) {
          // TDLog.v( "ZIP 8 entry " + nr_entry + " \"manifest\": skipping ...");
          // skip
        } else {
          // TDLog.Log( TDLog.LOG_ZIP, "Zip file entry " + nr_entry + " \"" + ze.getName() + "\"");
          // TDLog.v( "ZIP 8 file entry " + nr_entry + " \"" + ze.getName() + "\"");
          boolean sql = false;
          pathname = null;
          if ( ze.getName().equals( "survey.sql" ) ) {
            pathname = TDPath.getSqlFile();
            sql = true;
          } else if ( ze.getName().endsWith( TDPath.TDR ) ) {
            pathname = TDPath.getTdrFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.TDR3 ) ) {
          //   pathname = TDPath.getTdr3File( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TXT ) ) {
            pathname = TDPath.getNoteFile( ze.getName() );
          } else if ( ze.getName().endsWith( ".wav" ) ) { // AUDIOS
            // TDFile.makeTopoDroidDir( pathname );
            pathname = TDPath.getAudioFile( ze.getName() );
          } else if ( ze.getName().endsWith( ".jpg" ) ) { // PHOTOS
            // TDFile.makeTopoDroidDir( pathname );
            pathname = TDPath.getJpgFile( ze.getName() );
          } else if ( ze.getName().equals( "points.zip" ) ) { // POINTS
            if ( uncompressSymbols( zin, TDPath.getSymbolPointDirname(), "p_" ) ) {
              BrushManager.reloadPointLibrary( app, app.getResources() );
            }
          } else if ( ze.getName().equals( "lines.zip" ) ) { // LINES
            if ( uncompressSymbols( zin, TDPath.getSymbolLineDirname(), "l_" ) ) {
              BrushManager.reloadLineLibrary( app.getResources() );
            }
          } else if ( ze.getName().equals( "areas.zip" ) ) { // AREAS
            if ( uncompressSymbols( zin, TDPath.getSymbolAreaDirname(), "a_" ) ) {
              BrushManager.reloadAreaLibrary( app.getResources() );
            }
          } else {
            // TDLog.Error("unexpected file type " + ze.getName() );
            // pathname = null; // already null
          }
          if ( pathname != null ) {
            TDPath.checkPath( pathname );
            FileOutputStream fout = TDFile.getFileOutputStream( pathname );
            int size = decompressEntry( zin, ze, fout );
            // TDLog.Log( TDLog.LOG_ZIP, "Unzip file \"" + pathname + "\" size " + size );
            // TDLog.v( "Unzip file \"" + pathname + "\" size " + size );
            if ( size <= 0 ) {
              TDFile.deleteFile( pathname );
            } else {
              if ( sql ) {
                // TDLog.Log( TDLog.LOG_ZIP, "Zip sqlfile \"" + pathname + "\" DB version " + mManifestDbVersion );
                if ( app_data.loadFromFile( pathname, mManifestDbVersion ) < 0 ) ok_manifest = -5;
                TDFile.deleteFile( pathname );
              }
            }
          }
          zin.closeEntry();
        }
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "ZIP 8 file not found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "ZIP 8 IO error " + e.getMessage() );
    } finally {
      TDPath.setSurveyPaths( null );
    }
    if ( ok_manifest < 0 ) { // delete survey folder
      TDLog.Error("TODO manifest result " + ok_manifest );
    }
    // TDLog.v( "unarchive stream returns " + ok_manifest );
    return ok_manifest; // return 0 or 1
  }

}

