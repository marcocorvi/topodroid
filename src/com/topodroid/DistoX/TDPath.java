/** @file TDPath.java
 *
 * @author marco corvi
 * @date jan 2015 
 *
 * @brief TopoDroid application paths
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileFilter;
// import java.io.IOException;
// import java.io.FileNotFoundException;

import java.util.List;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

// import android.util.Log;

public class TDPath
{
  final static int NR_BACKUP = 3;
  final static String BCK_SUFFIX = ".bck";

  final static String CSV = ".csv";
  final static String CSX = ".csx";
  final static String DAT = ".dat";
  final static String DXF = ".dxf";
  final static String KML = ".kml";
  final static String PLT = ".plt";
  final static String PNG = ".png";
  final static String SRV = ".srv";
  final static String SVG = ".svg";
  final static String SVX = ".svx";
  final static String TDR = ".tdr";
  final static String TH  = ".th";
  final static String TH2 = ".th2";
  final static String TH3 = ".th3";
  final static String TMP = ".tmp";
  final static String TOP = ".top";
  final static String TRO = ".tro";
  final static String TXT = ".txt";
  final static String ZIP = ".zip";
    

  // ------------------------------------------------------------
  // PATHS

  static String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath(); // app base path
  static String APP_DEFAULT_PATH = EXTERNAL_STORAGE_PATH + "/TopoDroid/";
  static String APP_BASE_PATH = APP_DEFAULT_PATH;

  static String APP_BIN_PATH = APP_DEFAULT_PATH + "bin/";   // Firmwares  
  // static String APP_MAN_PATH = APP_DEFAULT_PATH + "man/";

  static String APP_SYMBOL_PATH = APP_DEFAULT_PATH + "symbol/";
  static String APP_POINT_PATH  = APP_SYMBOL_PATH + "point/";
  static String APP_LINE_PATH   = APP_SYMBOL_PATH + "line/";
  static String APP_AREA_PATH   = APP_SYMBOL_PATH + "area/";
  static String APP_SYMBOL_SAVE_PATH = APP_SYMBOL_PATH + "save/";
  static String APP_SAVE_POINT_PATH  = APP_SYMBOL_SAVE_PATH + "point/";
  static String APP_SAVE_LINE_PATH   = APP_SYMBOL_SAVE_PATH + "line/";
  static String APP_SAVE_AREA_PATH   = APP_SYMBOL_SAVE_PATH + "area/";

  private static String APP_CSV_PATH ;   //  = APP_BASE_PATH + "csv/";   // CSV text
  private static String APP_CSX_PATH ;   //  = APP_BASE_PATH + "csx/";   // cSurvey
  private static String APP_DAT_PATH ;   //  = APP_BASE_PATH + "dat/";   // Compass
  private static String APP_DUMP_PATH ;  //  = APP_BASE_PATH + "dump/"; // DistoX memory dumps
  private static String APP_DXF_PATH ;   //  = APP_BASE_PATH + "dxf/";
  private static String APP_KML_PATH ;   //  = APP_BASE_PATH + "kml/";
  private static String APP_PLT_PATH ;   //  = APP_BASE_PATH + "plt/";
  private static String APP_FOTO_PATH;   //  = APP_BASE_PATH + "photo/";
  private static String APP_IMPORT_PATH; //  = APP_BASE_PATH + "import/";
  private static String APP_NOTE_PATH;   //  = APP_BASE_PATH + "note/";
  private static String APP_PNG_PATH;    //  = APP_BASE_PATH + "png/";
  private static String APP_SRV_PATH ;   //  = APP_BASE_PATH + "srv/";
  private static String APP_SVG_PATH ;   //  = APP_BASE_PATH + "svg/";
  private static String APP_SVX_PATH ;   //  = APP_BASE_PATH + "svx/";
  private static String APP_TH_PATH  ;   //  = APP_BASE_PATH + "th/";
  private static String APP_TDR_PATH ;   //  = APP_BASE_PATH + "tdr/";
  private static String APP_TH2_PATH ;   //  = APP_BASE_PATH + "th2/";
  private static String APP_TH3_PATH ;   //  = APP_BASE_PATH + "th3/";
  private static String APP_TMP_PATH ;   //  = APP_BASE_PATH + "tmp/";
  private static String APP_TOP_PATH ;   //  = APP_BASE_PATH + "top/";
  private static String APP_TRO_PATH ;   //  = APP_BASE_PATH + "tro/";
  private static String APP_ZIP_PATH;    //  = APP_BASE_PATH + "zip/";
  // private static String APP_TLX_PATH ; //  = APP_BASE_PATH + "tlx/";

  static String mTherionLock = new String("Therion Lock");

  static String getDatabase() { return getDirFile( "distox14.sqlite" ); }
  static String getDeviceDatabase() { return APP_DEFAULT_PATH + "device10.sqlite"; }
  
  static void checkBasePath( String path )
  {
    String cwd = EXTERNAL_STORAGE_PATH + "/" + path;
    File dir = new File( cwd );
    if ( ! dir.exists() ) dir.mkdir();
  }

  // FIXME BASEPATH 
  // remove comments when ready to swicth to new Android app path system
  //
  static void setPaths( String path )
  {
    File dir = null;
    if ( path != null ) {
      String cwd = EXTERNAL_STORAGE_PATH + "/" + path;
      dir = new File( cwd );
      if ( ! dir.exists() ) dir.mkdirs();
      if ( dir.isDirectory() && dir.canWrite() ) APP_BASE_PATH = cwd + "/";
    }
    dir = new File( APP_BASE_PATH );
    if ( ! dir.exists() ) {
      if ( ! dir.mkdir() ) {
        TDLog.Error( "failed mkdir " + APP_BASE_PATH );
        APP_BASE_PATH = APP_DEFAULT_PATH;
      }
    }
    // Log.v(TAG, "Base Path \"" + APP_BASE_PATH + "\"" );

    // APP_TLX_PATH = APP_BASE_PATH + "tlx/";
    // checkDirs( APP_TLX_PATH );

    APP_DAT_PATH = APP_BASE_PATH + "dat/";
    // FIXME checkDirs( APP_DAT_PATH );

    APP_SRV_PATH = APP_BASE_PATH + "srv/";
    // FIXME checkDirs( APP_SRV_PATH );

    APP_SVX_PATH = APP_BASE_PATH + "svx/";
    // FIXME checkDirs( APP_SVX_PATH );

    APP_CSV_PATH = APP_BASE_PATH + "csv/";
    // FIXME checkDirs( APP_CSV_PATH );

    APP_CSX_PATH = APP_BASE_PATH + "csx/";
    // FIXME checkDirs( APP_CSX_PATH );

    APP_DUMP_PATH = APP_BASE_PATH + "dump/";
    // FIXME checkDirs( APP_DUMP_PATH );

    APP_TOP_PATH  = APP_BASE_PATH + "top/";
    // FIXME checkDirs( APP_TOP_PATH );

    APP_TH_PATH  = APP_BASE_PATH + "th/";
    // FIXME checkDirs( APP_TH_PATH );

    APP_TDR_PATH = APP_BASE_PATH + "tdr/";
    checkDirs( APP_TDR_PATH );

    APP_TH2_PATH = APP_BASE_PATH + "th2/";
    checkDirs( APP_TH2_PATH );

    APP_TH3_PATH = APP_BASE_PATH + "th3/";
    checkDirs( APP_TH3_PATH );

    APP_TMP_PATH = APP_BASE_PATH + "tmp/";
    checkDirs( APP_TMP_PATH );

    APP_DXF_PATH = APP_BASE_PATH + "dxf/";
    // FIXME checkDirs( APP_DXF_PATH );

    APP_KML_PATH = APP_BASE_PATH + "kml/";
    // FIXME checkDirs( APP_KML_PATH );

    APP_PLT_PATH = APP_BASE_PATH + "plt/";
    // FIXME checkDirs( APP_PLT_PATH );

    APP_SVG_PATH = APP_BASE_PATH + "svg/";
    // FIXME checkDirs( APP_SVG_PATH );

    APP_TRO_PATH = APP_BASE_PATH + "tro/";
    // FIXME checkDirs( APP_TRO_PATH );

    APP_PNG_PATH = APP_BASE_PATH + "png/";
    // FIXME checkDirs( APP_PNG_PATH );

    APP_NOTE_PATH = APP_BASE_PATH + "note/";
    checkDirs( APP_NOTE_PATH );

    APP_FOTO_PATH = APP_BASE_PATH + "photo/";
    checkDirs( APP_FOTO_PATH );

    APP_IMPORT_PATH = APP_BASE_PATH + "import/";
    checkDirs( APP_IMPORT_PATH );

    APP_ZIP_PATH = APP_BASE_PATH + "zip/";
    checkDirs( APP_ZIP_PATH );
  }

  static void setDefaultPaths()
  {
    APP_BIN_PATH = APP_DEFAULT_PATH + "bin/";
    checkDirs( APP_BIN_PATH );

    // APP_MAN_PATH = APP_DEFAULT_PATH + "man/";
    // checkDirs( APP_MAN_PATH );

    APP_SYMBOL_PATH  = APP_DEFAULT_PATH + "symbol/";
    APP_SYMBOL_SAVE_PATH  = APP_SYMBOL_PATH + "save/";
    APP_POINT_PATH  = APP_SYMBOL_PATH + "point/";
    APP_LINE_PATH   = APP_SYMBOL_PATH + "line/";
    APP_AREA_PATH   = APP_SYMBOL_PATH + "area/";
    APP_SAVE_POINT_PATH  = APP_SYMBOL_SAVE_PATH + "point/";
    APP_SAVE_LINE_PATH   = APP_SYMBOL_SAVE_PATH + "line/";
    APP_SAVE_AREA_PATH   = APP_SYMBOL_SAVE_PATH + "area/";
    symbolsCheckDirs();
  }

  static String noSpaces( String s )
  {
    return ( s == null )? null 
      : s.trim().replaceAll("\\s+", "_").replaceAll("/", "-").replaceAll("\\*", "+").replaceAll("\\\\", "");
  }

  static void checkPath( String filename )
  {
    if ( filename == null ) return;
    File fp = new File( filename );
    checkPath( new File( filename ) );
  }

  static void checkPath( File fp ) 
  {
    if ( fp == null || fp.exists() ) return;
    File fpp = fp.getParentFile();
    if ( fpp.exists() ) return;
    fpp.mkdirs(); // return boolean : must check ?
  }

  static String getLogFilename()
  {
    return APP_DEFAULT_PATH + "log.txt";
  }

  static File getLogFile()
  {
    File logfile = new File( APP_DEFAULT_PATH + "log.txt" );
    checkPath( logfile );
    return logfile;
  }

  // ------------------------------------------------------------------
  // FILE NAMES

  public static String getSqlFile() { return APP_BASE_PATH + "survey.sql"; }

  public static String getManifestFile() { return APP_BASE_PATH + "manifest"; }

  static String getSymbolFile( String name ) { return APP_SYMBOL_PATH + name; }
  static String getSymbolSaveFile( String name ) { return APP_SYMBOL_SAVE_PATH + name; }

  static boolean hasTdrDir() { return (new File( APP_TDR_PATH )).exists(); }
  static boolean hasTh2Dir() { return (new File( APP_TH2_PATH )).exists(); }
  static boolean hasTh3Dir() { return (new File( APP_TH3_PATH )).exists(); }
  static boolean hasPngDir() { return (new File( APP_PNG_PATH )).exists(); }
  static boolean hasDxfDir() { return (new File( APP_DXF_PATH )).exists(); }
  static boolean hasKmlDir() { return (new File( APP_KML_PATH )).exists(); }
  static boolean hasPltDir() { return (new File( APP_PLT_PATH )).exists(); }
  static boolean hasSvgDir() { return (new File( APP_SVG_PATH )).exists(); }

  static String getDirFile( String name )    { return APP_BASE_PATH + name; }
  static String getImportFile( String name ) { return APP_IMPORT_PATH + name; }
  static String getZipFile( String name )    { return APP_ZIP_PATH + name; }
  static String getTdrFile( String name )    { return APP_TDR_PATH + name; }
  static String getTh2File( String name )    { return APP_TH2_PATH + name; }
  static String getTh3File( String name )    { return APP_TH3_PATH + name; }

  static String getThFile( String name )     { return APP_TH_PATH + name; }
  static String getDatFile( String name )    { return APP_DAT_PATH + name; }
  static String getDxfFile( String name )    { return APP_DXF_PATH + name; }
  static String getKmlFile( String name )    { return APP_KML_PATH + name; }
  static String getPltFile( String name )    { return APP_PLT_PATH + name; }
  static String getSrvFile( String name )    { return APP_SRV_PATH + name; }
  static String getSvgFile( String name )    { return APP_SVG_PATH + name; }
  static String getSvxFile( String name )    { return APP_SVX_PATH + name; }
  static String getCsvFile( String name )    { return APP_CSV_PATH + name; }
  static String getCsxFile( String name )    { return APP_CSX_PATH + name; }
  static String getDumpFile( String name )   { return APP_DUMP_PATH + name; }
  static String getTopFile( String name )    { return APP_TOP_PATH + name; }
  static String getTroFile( String name )    { return APP_TRO_PATH + name; }
  static String getPngFile( String name )    { return APP_PNG_PATH + name; }

  static String getBinFile( String name )    { return APP_BIN_PATH + name; }
  // static String getManFile( String name )    { return APP_MAN_PATH + name; }

  static String getNoteFile( String name )   { return APP_NOTE_PATH + name; }

  static String getJpgDir( String dir ) { return APP_FOTO_PATH + dir; }
  static String getJpgFile( String dir, String name ) { return APP_FOTO_PATH + dir + "/" + name; }

  static String getSurveyPlotDxfFile( String survey, String name ) { return APP_DXF_PATH + survey + "-" + name + DXF ; }
  static String getSurveyPlotSvgFile( String survey, String name ) { return APP_SVG_PATH + survey + "-" + name + SVG ; }
  static String getSurveyPlotTdrFile( String survey, String name ) { return APP_TDR_PATH + survey + "-" + name + TDR ; }
  static String getSurveyPlotTh2File( String survey, String name ) { return APP_TH2_PATH + survey + "-" + name + TH2 ; }
  static String getSurveyPlotPngFile( String survey, String name ) { return APP_PNG_PATH + survey + "-" + name + PNG ; }

  static String getSurveySketchFile( String survey, String name ) { return APP_TH3_PATH + survey + "-" + name + TH3 ; }

  private static String getFile( String directory, String name, String ext ) 
  {
    checkDirs( directory );
    return directory + name + ext;
  }

  static File getTmpDir() { return new File( APP_TMP_PATH ); }

  static String getSurveyNoteFile( String title ) { return getFile( APP_NOTE_PATH, title, TXT ); }
  static String getTmpFileWithExt( String name ) { return getFile( APP_TMP_PATH, name, TMP ); }
  static String getTdrFileWithExt( String name ) { return getFile( APP_TDR_PATH, name, TDR ); }
  static String getTh2FileWithExt( String name ) { return getFile( APP_TH2_PATH, name, TH2 ); }
  static String getTh3FileWithExt( String name ) { return getFile( APP_TH3_PATH, name, TH3 ); }
  static String getDxfFileWithExt( String name ) { return getFile( APP_DXF_PATH, name, DXF ); }
  static String getSvgFileWithExt( String name ) { return getFile( APP_SVG_PATH, name, SVG ); }
  static String getPngFileWithExt( String name ) { return getFile( APP_PNG_PATH, name, PNG ); }

  static String getSurveyZipFile( String survey ) { return getFile( APP_ZIP_PATH, survey, ZIP ); }

  // static String getSurveyTlxFile( String survey ) { return getFile( APP_TLX_PATH, survey, TLX ); }
  static String getSurveyThFile( String survey ) { return getFile( APP_TH_PATH, survey, TH ); }
  static String getSurveyCsvFile( String survey ) { return getFile( APP_CSV_PATH, survey, CSV ); }
  static String getSurveyCsxFile( String survey ) { return getFile( APP_CSX_PATH, survey, CSX ); }
  static String getSurveyCsxFile( String survey, String name ) { return getFile( APP_CSX_PATH, survey + "-" + name, CSX ); }
  static String getSurveyDatFile( String survey ) { return getFile( APP_DAT_PATH, survey, DAT ); }
  static String getSurveyDxfFile( String survey ) { return getFile( APP_DXF_PATH, survey, DXF ); }
  static String getSurveyKmlFile( String survey ) { return getFile( APP_KML_PATH, survey, KML ); }
  static String getSurveyPltFile( String survey ) { return getFile( APP_PLT_PATH, survey, PLT ); }
  static String getSurveySrvFile( String survey ) { return getFile( APP_SRV_PATH, survey, SRV ); }
  static String getSurveySvxFile( String survey ) { return getFile( APP_SVX_PATH, survey, SVX ); }
  static String getSurveyTopFile( String survey ) { return getFile( APP_TOP_PATH, survey, TOP ); }
  static String getSurveyTroFile( String survey ) { return getFile( APP_TRO_PATH, survey, TRO ); }

  private static File[] getFiles( String dirname, final String[] ext )
  {
    File dir = new File( dirname );
    if ( dir.exists() ) {
      return dir.listFiles( new FileFilter() {
          public boolean accept( File pathname ) { 
            int ne = ext.length;
            if ( pathname.isDirectory() ) return false;
            if ( ne == 0 ) return true;
            for ( int n = 0; n < ne; ++n ) {
              if ( pathname.getName().endsWith( ext[n] ) ) return true;
            }
            return false;
          }
        } );
    }
    return null;
  }

  static File[] getCalibFiles() { return getFiles( APP_CSV_PATH, new String[] {""} ); }

  static File[] getTopoDroidFiles( )
  {
    File dir = new File( EXTERNAL_STORAGE_PATH );
    return dir.listFiles( new FileFilter() {
      public boolean accept( File pathname ) { 
        if ( ! pathname.isDirectory() ) return false;
        if ( pathname.getName().toLowerCase().startsWith( "topodroid" ) ) return true;
        return false;
      }
    } );
  }

  // private static File[] getFiles( String dirname, final String extension )
  // {
  //   File dir = new File( dirname );
  //   if ( dir.exists() ) {
  //     return dir.listFiles( new FileFilter() {
  //         public boolean accept( File pathname ) { return pathname.getName().endsWith( extension ); }
  //       } );
  //   }
  //   return null;
  // }

  static File[] getImportFiles() 
  { 
    return getFiles( APP_IMPORT_PATH, new String[] { TH, TOP, DAT, TRO } );
  }

  static File[] getZipFiles() { return getFiles( APP_ZIP_PATH, new String[] { ZIP } ); }
  static File[] getBinFiles() { return getFiles( APP_BIN_PATH, new String[] { } ); }

  // static String getSurveyPhotoFile( String survey, String name ) { return APP_FOTO_PATH + survey + "/" + name; }

  static String getSurveyPhotoDir( String survey ) { return APP_FOTO_PATH + survey; }

  static String getSurveyJpgFile( String survey, String id )
  {
    File imagedir = new File( APP_FOTO_PATH + survey + "/" );
    if ( ! ( imagedir.exists() ) ) {
      imagedir.mkdirs();
    }
    return APP_FOTO_PATH + survey + "/" + id + ".jpg";
  }

  static void checkDirs( String path )
  {
    File f1 = new File( path );
    if ( ! f1.exists() ) f1.mkdirs( );
  }

  static void symbolsCheckDirs()
  {
    checkDirs( APP_SYMBOL_PATH );
    checkDirs( APP_SYMBOL_SAVE_PATH );
    checkDirs( APP_POINT_PATH );
    checkDirs( APP_LINE_PATH );
    checkDirs( APP_AREA_PATH );
    checkDirs( APP_SAVE_POINT_PATH );
    checkDirs( APP_SAVE_LINE_PATH );
    checkDirs( APP_SAVE_AREA_PATH );
  }

  static void checkBinDir() { checkDirs( APP_BIN_PATH ); }

  // static void checkManDir() { checkDirs( APP_MAN_PATH ); }

  static void deleteSurveyFiles( String survey )
  {
    File imagedir = new File( getSurveyPhotoDir( survey ) );
    if ( imagedir.exists() ) {
      File[] fs = imagedir.listFiles();
      for ( File f : fs ) f.delete();
      imagedir.delete();
    }

    File t = new File( getSurveyNoteFile( survey ) ); if ( t.exists() ) t.delete();
    
    // t = new File( getSurveyTlxFile( survey ) ); if ( t.exists() ) t.delete();
    
    t = new File( getThFile(  survey + TH  ) ); if ( t.exists() ) t.delete();
    t = new File( getCsvFile( survey + CSV ) ); if ( t.exists() ) t.delete();
    t = new File( getCsxFile( survey + CSX ) ); if ( t.exists() ) t.delete();
    t = new File( getDatFile( survey + DAT ) ); if ( t.exists() ) t.delete();
    t = new File( getDxfFile( survey + DXF ) ); if ( t.exists() ) t.delete();
    t = new File( getKmlFile( survey + KML ) ); if ( t.exists() ) t.delete();
    t = new File( getPltFile( survey + PLT ) ); if ( t.exists() ) t.delete();
    t = new File( getSvxFile( survey + SVX ) ); if ( t.exists() ) t.delete();
    t = new File( getSrvFile( survey + SRV ) ); if ( t.exists() ) t.delete();
    t = new File( getTopFile( survey + TOP ) ); if ( t.exists() ) t.delete();
    t = new File( getTroFile( survey + TRO ) ); if ( t.exists() ) t.delete();
  }

  static void rotateBackups( String filename, int rotate ) // filename has suffix BCK_SUFFIX
  {
    File file2;
    File file1;
    for ( int i=rotate-1; i>0; --i ) { 
      file2 = new File( filename + Integer.toString(i) );
      file1 = new File( filename + Integer.toString(i-1) );
      if ( file1.exists() ) {
        file1.renameTo( file2 );
      }
    }
    file2 = new File( filename + "0" );
    file1 = new File( filename );
    if ( file1.exists() ) {
      file1.renameTo( file2 );
    }
  }

  static void deletePlotFileWithBackups( String filename )
  {
    File file = new File( filename );
    if ( file.exists() ) file.delete(); 

    String filepath = filename + TDPath.BCK_SUFFIX;
    file = new File( filepath );
    if ( file.exists() ) file.delete(); 
    for ( int i = 0; i < NR_BACKUP; ++i ) {
      filepath = filename + TDPath.BCK_SUFFIX + Integer.toString(i);
      file = new File( filepath );
      if ( file.exists() ) file.delete(); 
    }
  }

  static void deleteFile( String filename )
  {
    File file = new File( filename );
    if ( file.exists() ) file.delete(); 
  }

  static void deleteBackups( String filename ) // filename has suffix BCK_SUFFIX
  {
    for ( int i=NR_BACKUP-1; i>=0; --i ) { 
      File file = new File( filename + Integer.toString(i) );
      if ( file.exists() ) file.delete();
    }
  }

  static void deleteSurveyPlotFiles( String survey, List<PlotInfo> plots )
  {
    File t;
    for ( PlotInfo p : plots ) {
      String filename = getSurveyPlotTh2File( survey, p.name );
      t = new File( filename ); if ( t.exists() ) t.delete();
      deleteBackups( filename + BCK_SUFFIX );
      filename = getSurveyPlotTdrFile( survey, p.name );
      t = new File( filename ); if ( t.exists() ) t.delete();
      deleteBackups( filename + BCK_SUFFIX );
      t = new File( getSurveyPlotPngFile( survey, p.name ) ); if ( t.exists() ) t.delete();
      t = new File( getSurveyPlotDxfFile( survey, p.name ) ); if ( t.exists() ) t.delete();
      t = new File( getSurveyPlotSvgFile( survey, p.name ) ); if ( t.exists() ) t.delete();
    }
  }

  static void deleteSurvey3dFiles( String survey, List< Sketch3dInfo > sketches )
  {
    if ( hasTh3Dir() ) {
      for ( Sketch3dInfo s : sketches ) {
        File t = new File( getTh3FileWithExt( survey + "-" + s.name + TH3 ) ); if ( t.exists() ) t.delete();
      }
    }
  }
}
