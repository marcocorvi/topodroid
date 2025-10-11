/* @file TDPath.java
 *
 * @author marco corvi
 * @date jan 2015 
 *
 * @brief TopoDroid application paths
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDString;
import com.topodroid.utils.CWDfolder;

// import android.provider.DocumentsContract;

import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
import java.io.FileFilter;
import java.io.FilenameFilter;
// import java.io.FileNotFoundException;
// import java.io.IOException;

import java.util.List;
import java.util.Locale;

// import android.content.Context;
// import android.os.Environment;
// import android.widget.TextView;

// import android.net.Uri;

public class TDPath
{
  final static int NR_BACKUP = 5;
  final static String BCK_SUFFIX = ".bck";

  final static String C3D = ".c3d";
  final static String CAV = ".cav"; // 
  final static String CAVE = ".cave"; // Polygon
  final static String CSN = ".csn";  // CaveSniper
  final static String CSV = ".csv";
  final static String CSX = ".csx"; // cSurvey
  final static String DAT = ".dat"; // Compass
  final static String DXF = ".dxf"; // 
  final static String GPX = ".gpx"; // 
  final static String GTX = ".gtx"; // 
  final static String KML = ".kml"; // 
  final static String PDF = ".pdf"; // 
  final static String SHZ = ".shz"; // shapefile
  final static String SUR = ".sur"; // WinKarst
  final static String SVG = ".svg"; // SVG
  final static String SVX = ".svx"; // Survex
  final static String SRV = ".srv"; // Walls
  final static String TRB = ".text"; // TopoRobot
  final static String TDR = ".tdr";
  final static String TH2 = ".th2"; // TH2EDIT
  // final static private String TMP = ".tmp";

  final static String TOP = ".top"; // PocketTopo
  final static String TH  = ".th";  // Therion
  final static String TRO = ".tro"; // VisualTopo
  final static String TROX = ".trox"; // VisualTopo
  final static String TXT = ".txt";
  final static String XVI = ".xvi";
  final static String ZIP = ".zip";

  final static String GLTF = ".gltf";
  final static String CGAL = ".cgal";
  final static String STL  = ".stl";
  final static String LAS  = ".las";

  final static String DIR_BIN   = "bin";
  final static String DIR_CCSV  = "ccsv";
  final static String DIR_DUMP  = "dump";
  final static String DIR_POINT = "point";
  final static String DIR_LINE  = "line";
  final static String DIR_AREA  = "area";

  final static String THCONFIG = ".thconfig";
  // final static String TDCONFIG = ".tdconfig";

  // used by Archiver, and Symbol*Library (through TDFile)
  static String getSymbolPointDirname() { return DIR_POINT; } // "symbol/point"
  static String getSymbolLineDirname()  { return DIR_LINE; }  // "symbol/line"
  static String getSymbolAreaDirname()  { return DIR_AREA; }  // "symbol/area"

  // LOCKS --------------------------------------------------------
  final static Object mXSectionsLock = new Object();
  final static Object mSelectionLock = new Object();
  final static Object mCommandsLock  = new Object();
  final static Object mStationsLock  = new Object();
  final static Object mShotsLock     = new Object();
  final static Object mGridsLock     = new Object();
  // final static Object mFixedsLock    = new Object();
  // final static Object mSelectedLock  = new Object();
  final static Object mTdrLock       = new Object(); // FIXME-THREAD_SAFE synchronize tdr file save / open

  // PATHS ------------------------------------------------------------

  // if BUILD
  // If PATH_CB_DIR is left null the path is set in the method setPaths():
  //    this works with Android-10 but the data are erased when TopoDroid is uninstalled
  //    because the path is Android/data/com.topodroid.TDX/files
  // With "/sdcard" they remain
  /*
  private static final String EXTERNAL_STORAGE_PATH_10 = Environment.getExternalStorageDirectory().getAbsolutePath();
  private static final String EXTERNAL_STORAGE_PATH_11 =
    TDandroid.BELOW_API_19 ? null : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
  private static final String EXTERNAL_STORAGE_PATH =  // app base path
    TDandroid.BELOW_API_19 ? EXTERNAL_STORAGE_PATH_10
      : ( TDFile.hasTopoDroidFile( EXTERNAL_STORAGE_PATH_11, "TopoDroid" ) )? EXTERNAL_STORAGE_PATH_11
	: TDandroid.BELOW_API_29 ? EXTERNAL_STORAGE_PATH_10
	  : ( TDandroid.BELOW_API_30  && TDFile.hasTopoDroidFile( EXTERNAL_STORAGE_PATH_10, "TopoDroid") )? EXTERNAL_STORAGE_PATH_10 
	    : EXTERNAL_STORAGE_PATH_11;
  */

  // private static String PATH_CB_DIR  = EXTERNAL_STORAGE_PATH;
  // FIXME PRIVATE_STORAGE
  private static String PATH_CB_DIR   = TDandroid.PRIVATE_STORAGE ? TDFile.getPrivateDir( null ).getAbsolutePath() : TDFile.getExternalDir(null).getPath(); // fullpath (< 33) or null (for 33+)
  private static String PATH_CW_DIR   = PATH_CB_DIR + "/TopoDroid";     // fullpath 
  private static String ROOT_CW_DIR   = TDandroid.PRIVATE_STORAGE ? "TopoDroid" : PATH_CW_DIR;

  private static String ROOT_ZIP      = ROOT_CW_DIR + "/zip";
  private static String ROOT_TMP      = ROOT_CW_DIR + "/tmp";
  private static String ROOT_TDCONFIG = ROOT_CW_DIR + "/thconfig";
  private static String ROOT_C3EXPORT = ROOT_CW_DIR + "/c3export"; // used as temp folder for shp

  private static String APP_SURVEY_PATH   = null;
  private static String APP_PHOTO_PATH    = null;
  private static String APP_AUDIO_PATH    = null;
  private static String APP_NOTE_PATH     = null;
  private static String APP_TDR_PATH      = null; // full dir pathname
  private static String APP_C3D_PATH      = null;
  private static String APP_OUT_PATH      = null;

  private static String APP_SURVEY_ROOT   = null;
  private static String APP_PHOTO_ROOT    = null;
  private static String APP_AUDIO_ROOT    = null;
  private static String APP_NOTE_ROOT     = null;
  private static String APP_TDR_ROOT      = null;
  // private static String APP_C3D_ROOT      = null; // NO_C3D
  private static String APP_OUT_ROOT      = null;


  private static String APP_TMP_PATH = null;
  private static String APP_TMP_ROOT = null;
  private static String RELATIVE_TMP = null;

  /** clear the surveys pathnames
   */
  private static void clearAppPaths()
  {
    APP_SURVEY_PATH   = null;
    APP_PHOTO_PATH    = null;
    APP_AUDIO_PATH    = null;
    APP_NOTE_PATH     = null;
    APP_TDR_PATH      = null;
    APP_C3D_PATH      = null;
    APP_OUT_PATH      = null;

    APP_TMP_PATH = null;
    RELATIVE_TMP = null;
  }

  /** @return the fullpath of the survey database
   */
  static String getDatabase() 
  { 
    File dir = new File( PATH_CW_DIR );
    if ( ! dir.exists() ) dir.mkdirs();
    return PATH_CW_DIR + "/distox14.sqlite";
  }

  // /** delete a database - only in the private storage
  //  * @param name  database filename
  //  */
  // static void deleteDatabase( String name ) 
  // {
  //   if ( TDandroid.PRIVATE_STORAGE ) {
  //     // TDFile.deletePrivateFile( "TopoDroid", "distox14.sqlite" );
  //     String path = PATH_CW_DIR + "/distox14.sqlite";
  //     TDLog.v("PATH delete " + path );
  //     File file = new File( path );
  //     if ( file.exists() ) file.delete();
  //     path = PATH_CW_DIR + "/distox14.sqlite-journal";
  //     file = new File( path );
  //     if ( file.exists() ) file.delete();
  //   }
  // }

  // private static String PATH_DEFAULT  = PATH_CW_DIR;
  // static String getDeviceDatabase() { return PATH_DEFAULT + "device10.sqlite"; }
  // public static String getPacketDatabase() { return PATH_DEFAULT + "packet10.sqlite"; }
  
  /** @return true if the base-directory can be used
   * @param name   base-directory name relative to "TDX"
   * @note when this is called basedir exists and is writable
   */
  static boolean checkBasePath( String name )
  {
    if ( TDandroid.PRIVATE_STORAGE ) {
      // TDLog.v("check Base Path ... skipping get external dir: " + TDFile.getExternalDir(null).getPath() );
      return true;
    }
    File dir = TDFile.getExternalDir( name ); 
    if ( ! dir.exists() ) {
      if ( ! dir.mkdirs() ) {
        TDLog.e("mkdir error " + name );
        return false;
      }
    }
    // TDInstance.takePersistentPermissions( Uri.fromFile( dir ) ); // FIXME_PERSISTENT

    boolean ret = false;
    try {
      ret = dir.exists() && dir.isDirectory() && dir.canWrite();
    } catch ( SecurityException e ) {
      TDLog.e("security error: " + e.getMessage() );
    }
    // TDLog.v( "PATH check base <" + name + ">: " + ret );
    return ret;
  }

  /** @return the current base directory fullpath (which is unchangeable )
   */
  public static String getCurrentBaseDir() 
  {
    return PATH_CB_DIR;
  }

  /** @return the current work directory fullpath
   */
  public static String getCurrentWorkDir() 
  {
    return ROOT_CW_DIR;
  }
 
  /** set the Current Work Directory
   * @param name   current work directory name, eg, "TopoDroid"
   * @return true if the folder as been created
   * FIXME FIXME FIXME allow cwd change in private folder
   * @note used only by TopoDroidApp
   */
  static boolean setTdPaths( String name /*, String base */ )
  {
    // TDLog.v( "set paths [4]: " + name );
    // if ( TDandroid.PRIVATE_STORAGE ) { // FIXME this was enabled 20230118
    //   checkFilesystemDirs( ROOT_ZIP );
    //   checkFilesystemDirs( ROOT_TMP );
    //   checkFilesystemDirs( ROOT_TDCONFIG  );
    //   checkFilesystemDirs( ROOT_C3EXPORT  );
    //   setSurveyPaths( null );
    //   return;
    // }
    if ( name == null /* || ! CWDfolder.checkTopoDroid( name ) */ ) return false;
    boolean ret = false;
    File dir = null; // 20230118 (8 lines)
    if ( TDandroid.PRIVATE_STORAGE ) { 
      dir = TDFile.getPrivateDir( name ); // PATH_CB_DIR + "/" + name 
      // PATH_CW_DIR = dir.getAbsolutePath();
    } else {
      dir = TDFile.getExternalDir( name ); // DistoX-SAF
      // PATH_CW_DIR =  PATH_CB_DIR + "/" + name;
    }
    // TDLog.v( "set paths [4]: " + name + ". Dir " + dir.getPath()  );
    try {
      if ( dir.exists() || (ret = dir.mkdirs()) ) {
        // TDInstance.takePersistentPermissions( Uri.fromFile( dir ) ); // FIXME_PERSISTENT
	if ( dir.isDirectory() && dir.canWrite() ) {
	  ROOT_CW_DIR   = TDandroid.PRIVATE_STORAGE ? name : PATH_CB_DIR + "/" + name;
          ROOT_ZIP      = ROOT_CW_DIR + "/zip";      checkFilesystemDirs( ROOT_ZIP );
          ROOT_TMP      = ROOT_CW_DIR + "/tmp";      checkFilesystemDirs( ROOT_TMP );
          ROOT_TDCONFIG = ROOT_CW_DIR + "/thconfig"; checkFilesystemDirs( ROOT_TDCONFIG  );
          ROOT_C3EXPORT = ROOT_CW_DIR + "/c3export"; checkFilesystemDirs( ROOT_C3EXPORT  );
          PATH_CW_DIR   = TDandroid.PRIVATE_STORAGE ? dir.getAbsolutePath() : ROOT_CW_DIR;
          setSurveyPaths( null );
	} else {
          TDLog.e("PATH ext storage: no dir or no write " + name );
        }
      } else {
        TDLog.e("PATH ext storage: no exist no make " + name );
      }
    } catch ( SecurityException e ) { 
      TDLog.e("PATH ext storage security error " + e.getMessage() );
    }
    return ret;
  }

  /** create survey folder, if it does not exist, and subfolders
   * @param survey    survey name
   */
  public static void createSurveyPaths( String survey )
  {
    if ( TDandroid.PRIVATE_STORAGE ) {
      PATH_CB_DIR = TDFile.getPrivateBaseDir().getAbsolutePath();
      File cwd    = TDFile.getPrivateDir( ROOT_CW_DIR );
      if ( ! cwd.exists() ) cwd.mkdirs();
      PATH_CW_DIR = cwd.getAbsolutePath();
    }
    String root = ROOT_CW_DIR + "/" + survey;
    // TDLog.v( "create paths survey " + survey + " base " + ROOT_CW_DIR + " root " + root );
    checkFilesystemDirs( root );
    checkFilesystemDirs( root + "/tdr" );
    checkFilesystemDirs( root + "/c3d" );
    checkFilesystemDirs( root + "/note" );
    checkFilesystemDirs( root + "/photo" );
    checkFilesystemDirs( root + "/audio" );
  }

  /** set the survey paths
   * @param survey   survey name
   */
  static void setSurveyPaths( String survey )
  {
    if ( TDString.isNullOrEmpty( survey ) ) {
      // TDLog.v( "PATH set survey path NULL");
      clearAppPaths();
    } else {
      // TDLog.v( "set survey path " + survey + " base " + ROOT_CW_DIR );
      APP_SURVEY_ROOT = ROOT_CW_DIR + "/" + survey;
      checkFilesystemDirs( APP_SURVEY_ROOT ); // fullpath
      APP_TDR_ROOT   = APP_SURVEY_ROOT + "/tdr";     checkFilesystemDirs( APP_TDR_ROOT );
      // APP_C3D_ROOT   = APP_SURVEY_ROOT + "/c3d";     checkFilesystemDirs( APP_C3D_ROOT ); // NO_C3D
      APP_NOTE_ROOT  = APP_SURVEY_ROOT + "/note";    checkFilesystemDirs( APP_NOTE_ROOT );
      APP_PHOTO_ROOT = APP_SURVEY_ROOT + "/photo";   checkFilesystemDirs( APP_PHOTO_ROOT );
      APP_AUDIO_ROOT = APP_SURVEY_ROOT + "/audio";   checkFilesystemDirs( APP_AUDIO_ROOT );
      APP_OUT_ROOT   = APP_SURVEY_ROOT + "/out";     checkFilesystemDirs( APP_OUT_ROOT );
      APP_TMP_ROOT   = APP_SURVEY_ROOT + "/tmp";     checkFilesystemDirs( APP_TMP_ROOT );

      APP_SURVEY_PATH = PATH_CW_DIR + "/" + survey;
      APP_TDR_PATH   = APP_SURVEY_PATH + "/tdr";
      APP_C3D_PATH   = APP_SURVEY_PATH + "/c3d"; 
      APP_NOTE_PATH  = APP_SURVEY_PATH + "/note";
      APP_PHOTO_PATH = APP_SURVEY_PATH + "/photo";
      APP_AUDIO_PATH = APP_SURVEY_PATH + "/audio";
      APP_OUT_PATH   = APP_SURVEY_PATH + "/out";
      APP_TMP_PATH   = APP_SURVEY_PATH + "/tmp";    // CWD/survey/tmp

      RELATIVE_TMP = (survey == null)? null : survey + "/tmp";
    }
  }

  /** clear symbols folders
   */
  static void clearSymbols( )
  {
    clearSymbolsDir( DIR_POINT );
    clearSymbolsDir( DIR_LINE  );
    clearSymbolsDir( DIR_AREA  );
  }  

  /** 
   * @param pathname   full pathname
   */
  public static void checkPath( String pathname )
  {
    if ( pathname == null ) {
      // TDLog.v( "check path: null string" );
      return;
    }
    checkPath( TDFile.getTopoDroidFile( pathname ) ); // DistoX-SAF
  }

  /** @return true if a file exists
   * @param pathname   file pathname
   */ 
  public static boolean existPath( String pathname )
  {
    if ( pathname == null ) return false;
    return existPath( TDFile.getTopoDroidFile( pathname ) ); // DistoX-SAF
  }

  // ------------------------------------------------------------------

  /** @return array of tdconfig files full pathnames
   * @note used only be TdManagerActivity
   */
  public static String[] scanTdconfigDir() // DistoX-SAF
  {
    File dir = TDFile.getTopoDroidFile( ROOT_TDCONFIG  );
    if ( ! dir.exists() && ! dir.mkdirs() ) {
      TDLog.e("tdconfig error: no exist no make");
      return null;
    }
    // TDInstance.takePersistentPermissions( Uri.fromFile( dir ) ); // FIXME_PERSISTENT
    FilenameFilter filter = new FilenameFilter() {
       public boolean accept( File dir, String name ) {
	 return name.endsWith( "tdconfig" );
       }
    };
    File[] files = dir.listFiles( filter );
    if ( files == null || files.length == 0 ) {
      TDLog.e("PATH tdconfig: no files");
      return null;
    }
    String[] filenames = new String[ files.length ];
    for ( int k=0; k<files.length; ++k ) filenames[k] = files[k].getAbsolutePath();
    return filenames;
  }

  // ------------------------------------------------------------------
  // FILE NAMES

  /** @return the ful pathname of the current work directory
   */
  public static String getPathBase() { return PATH_CW_DIR; }

  /** @return the sqlite script pathname, ie, <survey>/survey.sql
   * @note used by Archiver
   */
  static String getSqlFile() 
  {
    TDLog.v("SQL file: " + APP_SURVEY_PATH + "/survey.sql" );
    return APP_SURVEY_PATH + "/survey.sql";
  }

  /** @return the manifest file pathname, ie, <tmp>/manifest
   * @note used by Archiver, TopoDroidApp
   */
  static String getManifestFile() 
  {
    if ( TDandroid.PRIVATE_STORAGE ) return PATH_CW_DIR + "/tmp/manifest";
    return ROOT_TMP + "/manifest";
  }

  /** @return survey subfolder full pathname, is, <survey>/<subfolder>
   * @param name   subfolder name
   * @note used by Archiver
   */
  static String getDirFile( String name ) 
  { 
    return APP_SURVEY_PATH + "/" + name;
  }

  // static String getSymbolFile( String name ) { return name; }

  /** @return true if there is the survey sketch folder
   */
  static boolean hasTdrDir() { return TDFile.hasTopoDroidFile( APP_TDR_PATH ); } // DistoX-SAF

  /** @return true if there is the survey 3D sketch folder
   */
  static boolean hasC3dDir() { return TDFile.hasTopoDroidFile( APP_C3D_PATH ); } // DistoX-SAF

  // static File getTdrDir() { return TDFile.makeTopoDroidDir( APP_TDR_PATH ); } // DistoX-SAF // CLEAR_BACKUPS

  static File getC3dDir() { return TDFile.makeTopoDroidDir( APP_C3D_PATH ); } // DistoX-SAF

  /** @return the survey 3D sketch folder
   */
  static String getC3dPath() { return APP_C3D_PATH; } // DistoX-SAF

  /** @return full pathname of a zip file, in the zip folder
   * @param name   zip-file name
   * @note used by ImportZipTask.unArchive
   */
  public static String getZipFile( String name ) { return ROOT_ZIP     + "/" + name; }

  // /** @return full pathname of a temporary file, in the tmp folder
  //  * @param name   temp-file name
  //  */
  // public static String getTmpFile( String name ) { return ROOT_TMP     + "/" + name; }

  /** @return full pathname of a tdr file, in the tdr folder
   * @param name   tdr-file name
   */
  public static String getTdrFile( String name ) { return APP_TDR_PATH + "/" + name; }

  /** @return full pathname of a c3d file, in the c3d folder
   * @param name   c3d-file name
   */
  public static String getC3dFile( String name ) { return APP_C3D_PATH + "/" + name; }

  /** @return full pathname of a Tdconfig folder, in the "TopoDroid" folder
   */
  public static String getTdconfigDir( ) { return ROOT_TDCONFIG ; }

  /** @return full pathname of a tdconfig file, in the tdconfig folder
   * @param name   tdconfig-file name
   */
  public static String getTdconfigFile( String name ) { return ROOT_TDCONFIG + "/" + name; }

  // replaced with TDFile functions

  /** c3xport folder is used as temporray forder for SHP files before zip-compressing them in SHZ
   */
  // public static String getC3exportDir( ) { return ROOT_C3EXPORT ; }
  public static String getC3exportPath( String name ) { return ROOT_C3EXPORT + "/" + name; }
  // public static String getC3exportPath( String name, String ext ) { return ROOT_C3EXPORT + "/" + name + "." + ext; }

  public static String getManFileName( String name ) { return "man/" + name; }

  // /** @return the current survey photo folder full pathname
  //  */
  // static String getJpgDir( )                { return APP_PHOTO_PATH; }

  // /** @return the current survey audio folder full pathname
  //  */
  // static String getAudioDir( )              { return APP_AUDIO_PATH; }

  /** @return the current survey note file full pathname
   * @param name note filename, ie, <survey>.txt
   * @note used only by Archiver
   */
  static String getNoteFile( String name )  { return APP_NOTE_PATH  + "/" + name; }

  /** @return the survey note file full pathname
   * @param survey  survey name
   * @note used by ImportTRobotTask
   */
  static public String getNoteTRobotFile( String survey )  { return ROOT_CW_DIR + "/" + survey + "/note/trobot.txt"; }

  /** @return a current survey out-file full pathname
   * @param name out filename, eg, <survey>.th
   */
  static String getOutFile( String name )  { return APP_OUT_PATH  + "/" + name; }

  /** @return the current survey photo file full pathname
   * @param name photo filename, ie, <index>.jpg
   * @note used by Archiver, DrawingWindow, and ShpPoint
   */
  static public String getJpgFile( String name ) { return APP_PHOTO_PATH + "/" + name; }

  /** @return the current survey audio file full pathname
   * @param name audio filename, ie, <index>.wav
   * @note used only by Archiver
   */
  static String getAudioFile( String name ) { return APP_AUDIO_PATH + "/" + name; }

  // @param name   (reduced) plot name
  static String getSurveyPlotTdrFile( String survey, String name ) { return APP_TDR_PATH + "/" + survey + "-" + name + ".tdr" ; }

  /** @return survey plot backup filename
   * @param survey   survey name
   * @param name     plot name
   */
  static String getSurveyPlotTdrBackupFile( String survey, String name ) { return APP_TDR_PATH + "/" + survey + "-" + name + ".tdr.bck"; }

  /** @return survey plot backup filename
   * @param survey   survey name
   * @param name     plot name
   * @param backup   backup extension number (-1 to fall back to unnumbered backup)
   */
  static String getSurveyPlotTdrBackupFile( String survey, String name, int backup ) 
  { 
    if ( backup < 0 ) return APP_TDR_PATH + "/" + survey + "-" + name + ".tdr.bck";
    return APP_TDR_PATH + "/" + survey + "-" + name + ".tdr.bck" + backup ; 
  }

  /** return a 3D sketch file in the survey folder
   * @param survey   survey name
   * @param name     sketch name
   */
  static String getSurveyPlotC3dFile( String survey, String name ) { return APP_C3D_PATH + "/" + survey + "-" + name + ".c3d" ; }

  /** @return survey zip-archive full pathname
   * @param survey   survey name
   */
  public static String getSurveyZipFile( String survey ) { return getPathname( ROOT_ZIP, survey, ZIP ); }

  /** @return survey note-file full pathname
   * @param title   survey name
   */
  public static String getSurveyNoteFile( String title ) 
  { 
    // TDLog.v("PATH get survey note file " + title );
    return getPathname( APP_NOTE_ROOT, title, TXT );
  }

  /** @return survey tdr file full pathname
   * @param name   tdr-file name ,ie, <survey>-<plot>.tdr
   */
  public static String getTdrFileWithExt( String name )
  {
    return getPathname( APP_TDR_ROOT, name, TDR );
  }

  /** @return a tdr file full pathname
   * @param dirname   survey dirname
   * @param name      tdr name (without extension
   */
  public static String getTdrFileWithExt( String dirname, String name )
  {
    return getPathname( ROOT_CW_DIR, dirname + "/" + name, TDR );
  }

  // /** return a 3D sketch file in the root folder // NO_C3D
  //  * @param survey   survey name
  //  * @param name     sketch name
  //  */
  // public static String getC3dFileWithExt( String name )  { return getPathname( APP_C3D_ROOT, name, C3D ); }

  public static String getShpTempRelativeDir( ) 
  {
    // checkFilesystemDirs( APP_TMP_PATH );
    return RELATIVE_TMP;
  }

  // ---------------------------------------------------------------------------------------

  /** @return array of firmware files
   */
  public static File[] getBinFiles()   { return getInternalFiles( DIR_BIN ); }

  /** @return array of calibration export files
   */
  public static File[] getCalibFiles() { return getInternalFiles( DIR_CCSV  ); } // DistoX-SAF

  /** @return firmware file
   * @param filename  file name
   */
  public static File getBinFile( String filename )   { return TDFile.getPrivateFile( DIR_BIN, filename ); }


  /** delete a bin file
   * @param filename  file name
   */
  public static void deleteBinFile( String filename ) { TDFile.deletePrivateFile( DIR_BIN, filename ); }

  /** @return calibration export file
   * @param filename  file name
   */
  public static File getCcsvFile( String filename )  { return TDFile.getPrivateFile( DIR_CCSV, filename ); }

  /** @return memory dump file
   * @param filename  file name
   */
  public static File getDumpFile( String filename )  { return TDFile.getPrivateFile( DIR_DUMP, filename ); }

  /** @return point symbol file
   * @param filename  file name
   */
  public static File getPointFile( String filename ) { return TDFile.getPrivateFile( DIR_POINT, filename ); }

  /** @return line symbol file
   * @param filename  file name
   */
  public static File getLineFile( String filename )  { return TDFile.getPrivateFile( DIR_LINE, filename ); }

  /** @return area symbol file
   * @param filename  file name
   */
  public static File getAreaFile( String filename )  { return TDFile.getPrivateFile( DIR_AREA, filename ); }

  /** @return the point symbol folder
   */
  public static File getPointDir( ) { return TDFile.getPrivateDir( DIR_POINT ); }

  /** delete a point symbol file
   * @param name  point name (ie, filename)
   */
  public static  void deletePointFile( String name ) { TDFile.deletePrivateFile( DIR_POINT, name ); }

  /** delete a line symbol file
   * @param name  line name (ie, filename)
   */
  public static  void deleteLineFile( String name )  { TDFile.deletePrivateFile( DIR_LINE,  name ); }

  /** delete a area symbol file
   * @param name  area name (ie, filename)
   */
  public static  void deleteAreaFile( String name )  { TDFile.deletePrivateFile( DIR_AREA,  name ); }

  /** @return geocodes file
   */
  public static File getGeocodesFile( )  { return TDFile.getPrivateFile( null, "geocodes" ); }


  // ---------------------------------------------------------------------------------------

  /** get the list of TopoDroid folders (null if no such folder exists)
   * @param basename   base folder
   * @return array of dirnames that are not empty
   * @note used only by CWDActivity to list TopoDroid dirs
   */
  static String[] getTopoDroidFiles( String basename ) // DistoX-SAF
  {
    File dir = TDFile.getTopoDroidFile( basename );
    File[] files = dir.listFiles( new FileFilter() {
      public boolean accept( File pathname ) { 
	if ( ! pathname.isDirectory() ) return false;
	return ( CWDfolder.isNameOk( pathname.getName() ) );
      }
    } );
    if ( files == null || files.length == 0 ) {
      TDLog.e("PATH no TopoDroid folders");
      return null;
    }
    int len = files.length;
    String[] dirnames = new String[ len ];
    for ( int k=0; k<len; ++ k ) dirnames[k] = files[k].getName();
    return dirnames;
  }

  // NOTA BENE extensions include the dot, eg, ".th"
  // static final String[] IMPORT_EXT        = { TH, TOP, DAT, TRO, CSN, SVX, SRV, TRB };
  static final String[] DRAW_EDIT = { TH2, TDR };
  static final String[] IMPORT_EXT_STREAM = { TOP, ZIP };
  static final String[] IMPORT_EXT_READER = { TH, DAT, TRO, TROX, CSN, SVX, SRV, TRB, TH2, CSV }; // TH2EDIT added TH2

  // static File[] getImportFiles() // DistoX-SAF
  // { 
  //   return getFiles( PATH_IMPORT, IMPORT_EXT );
  // }

  static String checkDrawEditType( String ext ) { return checkType( ext, DRAW_EDIT ); }

  static String checkImportTypeStream( String ext ) { return checkType( ext, IMPORT_EXT_STREAM ); }

  /** @return the file type from the extension
   * @param ext     import type extension (with '.')
   * @note called only by MainWindow
   */
  static String checkImportTypeReader( String ext ) { return checkType( ext, IMPORT_EXT_READER ); }

  /** @return the array of supported import types
   */
  static String[] getImportTypes() { return new String[] { TH, TOP, DAT, TRO, TROX, CSN, SVX }; }

  /** @return the array of zip archives in the zip folder
   */
  static File[] getZipFiles() { return getFiles( ROOT_ZIP, new String[] { ZIP } ); } // DistoX-SAF


  // these are used to get the folders when the survey does not exist yet (on import)
  static String getSurveyDir( String survey ) { return PATH_CW_DIR + "/" + survey; } // : /.../emulated/0/Documents/TDX/TopoDroid/survey

  /** @return the full pathname of the survey photo folder
   * @param survey     survey name
   */
  static String getSurveyPhotoDir( String survey ) { return PATH_CW_DIR + "/" + survey + "/photo"; }

  /** @return the full pathname of the survey audio folder
   * @param survey     survey name
   */
  static String getSurveyAudioDir( String survey ) { return PATH_CW_DIR + "/" + survey + "/audio"; }

  /** @return the full pathname of the survey photo file
   * @param survey     survey name
   * @param name       photo index
   */
  static String getSurveyPhotoFile( String survey, String name ) { return PATH_CW_DIR + "/" + survey + "/photo/" + name; }

  /** @return the full pathname of the survey audio file
   * @param survey     survey name
   * @param name       audio index
   */
  static String getSurveyAudioFile( String survey, String name ) { return PATH_CW_DIR + "/" + survey + "/audio/" + name; }

  /** @return full pathname of a survey photo (JPG) file 
   * @param survey   survey name
   * @param idx      photo index
   */
  static String getSurveyJpgFile( String survey, String idx ) { return getSurveyImageFile( survey, idx, ".jpg" ); }

  /** @return full pathname of a survey photo (PNG) file 
   * @param survey   survey name
   * @param idx      photo index
   */
  static String getSurveyPngFile( String survey, String idx ) { return getSurveyImageFile( survey, idx, ".png" ); }

  /** @return full pathname of a survey photo image file 
   * @param survey   survey name
   * @param idx      photo index
   * @param ext      image file extension
   */
  private static String getSurveyImageFile( String survey, String idx, String ext ) 
  {
    String dirpath = PATH_CW_DIR + "/" + survey + "/photo";
    TDFile.makeTopoDroidDir( dirpath );
    return dirpath + "/" + idx + ext;
  }

  static String getSurveyNextImageFilepath( long photo_id, int format ) 
  {
    if ( format == PhotoInfo.FORMAT_JPEG ) {
      return getSurveyJpgFile( TDInstance.survey, Long.toString(photo_id) ); // photo file is "survey/id.jpg"
    } else if ( format == PhotoInfo.FORMAT_PNG ) {
      return getSurveyPngFile( TDInstance.survey, Long.toString(photo_id) ); // photo file is "survey/id.png"
    }
    return null;
  }


  /** @return full pathname of a survey audio (WAV) file 
   * @param survey   survey name
   * @param idx      audio index
   */
  static String getSurveyWavFile( String survey, String idx )
  {
    String dirpath = PATH_CW_DIR + "/" + survey + "/audio";
    TDFile.makeTopoDroidDir( dirpath );
    return dirpath + "/" + idx + ".wav";
  }
  
  /** @return reduced filename of a survey photo (JPG) file 
   * @param survey   survey name
   * @param idx      photo index
   * @note used by SHP export
   */
  public static String getSurveyJpgFilename( String survey, String idx ) { return survey + "/photo/" + idx + ".jpg"; }

  /** @return reduced filename of a survey audio (WAV) file 
   * @param survey   survey name
   * @param id      audio index
   * @note used by SHP export
   */
  public static String getSurveyWavFilename( String survey, String id ) { return survey + "/audio/" + id + ".wav"; }

  /** rotate the backups of a plot (tdr)
   * @param filename   first backup file pathname
   * @param rotate     last index of backup 
   */
  static void rotateBackups( String filename, int rotate ) // filename has suffix BCK_SUFFIX
  {
    if ( rotate <= 0 ) return;
    for ( int i=rotate-1; i>0; --i ) {
      TDFile.moveFile( filename + (i-1), filename + i );
    }
    TDFile.moveFile( filename, filename + "0"  );
  }

  /** rename a plot file
   * @param old_name old (short) filename
   * @param new_name new (short) filename
   * @return false on error, true on ok
   */
  static boolean renamePlotFiles( String old_name, String new_name )
  {
    String old_tdr = TDPath.getTdrFile( old_name + ".tdr" );
    String new_tdr = TDPath.getTdrFile( new_name + ".tdr" );
    TDFile.renameFile( old_tdr, new_tdr );
    old_tdr = old_tdr + TDPath.BCK_SUFFIX;
    new_tdr = new_tdr + TDPath.BCK_SUFFIX;
    File file1 = TDFile.getTopoDroidFile( old_tdr ); // DistoX-SAF
    File file2 = TDFile.getTopoDroidFile( new_tdr );
    if ( ! file1.exists() ) return true;
    if ( file2.exists() ) return false;
    if ( ! file1.renameTo( file2 ) ) {
      TDLog.e("bck file rename failed");
      return false;
    }
    for ( int i=0; ; ++i ) {
      file1 = TDFile.getTopoDroidFile( old_tdr + i ); // DistoX-SAF
      file2 = TDFile.getTopoDroidFile( new_tdr + i );
      if ( ( ! file1.exists() ) /* || file2.exists() */ ) return true;
      if ( ! file1.renameTo( file2 ) ) {
        TDLog.e("bck" + i + " file rename failed");
        return false;
      }
    }
    // return true;
  }

  /**
   * @param survey   survey name
   * @param from     old station name
   * @param to       new station name
   * @return false on error, true on ok
   */
  static boolean renameStationXSectionFiles( String survey, String from, String to )
  {
    boolean ret = true;
    String prefix  = APP_TDR_PATH + "/" + survey;
    String old_name = prefix + "-xs-" + from + ".tdr";
    if ( TDFile.hasTopoDroidFile( old_name ) ) {
      String new_name = prefix + "-xs-" + to + ".tdr";
      // TDLog.v("Rename " + old_name + " -> " + new_name );
      ret &= TDFile.moveFile( old_name, new_name );
    // } else { TDLog.v("Rename " + old_name + " does not exist" );
    }
    old_name = prefix + "-xh-" + from + ".tdr";
    if ( TDFile.hasTopoDroidFile( APP_TDR_PATH, old_name ) ) {
      String new_name = prefix + "-xh-" + to + ".tdr";
      // TDLog.v("Rename " + old_name + " -> " + new_name );
      ret &= TDFile.moveFile( old_name, new_name );
    // } else { TDLog.v("Rename " + old_name + " does not exist" );
    }
    return ret;
  }

  /** delete the files of a survey
   * @param survey survey name
   */
  static void deleteSurveyFiles( String survey )
  {
    File image_dir = TDFile.getTopoDroidFile( getSurveyPhotoDir( survey ) ); // DistoX-SAF
    if ( image_dir.exists() ) {
      File[] files = image_dir.listFiles();
      if ( files != null ) {
        for (File f : files) if (!f.delete()) TDLog.e("image file delete error " + f.getPath() );
      }
      if ( ! image_dir.delete() ) TDLog.e("Dir photo delete error");
    }
    File audio_dir = TDFile.getTopoDroidFile( getSurveyAudioDir( survey ) ); // DistoX-SAF
    if ( audio_dir.exists() ) {
      File[] files = audio_dir.listFiles();
      if ( files != null ) {
        for (File f : files) if (!f.delete()) TDLog.e("audio file delete error " + f.getPath() );
      }
      if ( ! audio_dir.delete() ) TDLog.e("Dir audio delete error");
    }
    TDFile.deleteFile( getSurveyNoteFile( survey ) );
    // deleteSurveyExportFiles( survey )
  }

  /** delete a survey folder
   * @param survey survey name
   */
  static void deleteSurveyDir( String survey )
  {
    String dirpath = getSurveyDir( survey );
    // TDLog.v("delete path " + dirpath );
    File dir = TDFile.getTopoDroidFile( dirpath );
    TDFile.recursiveDeleteDir( dir );
  }

  /** delete the backups of a plot files 
   * @param filename  plot full pathname
   */
  static void deleteBackups( String filename ) // filename has suffix BCK_SUFFIX
  {
    TDFile.deleteFile( filename );
    for ( int i=NR_BACKUP-1; i>=0; --i ) {
      TDFile.deleteFile( filename + i );
    }
  }

  /** delete a plot files and its backups
   * @param filename  plot full pathname
   */
  static void deletePlotFileWithBackups( String filename )
  {
    TDFile.deleteFile( filename );
    String filepath = filename + TDPath.BCK_SUFFIX;
    TDFile.deleteFile( filepath );
    for ( int i = 0; i < NR_BACKUP; ++i ) {
      filepath = filename + TDPath.BCK_SUFFIX + i;
      TDFile.deleteFile( filepath );
    }
  }

  /** delete the plot files of a survey
   * @param survey survey name
   * @param plots  list of survey plots
   */
  static void deleteSurveyPlotFiles( String survey, List< PlotInfo > plots )
  {
    for ( PlotInfo p : plots ) {
      // String filename = getSurveyPlotTh2File( survey, p.name );
      // TDFile.deleteFile( filename );
      // deleteBackups( filename + BCK_SUFFIX );
      String filename = getSurveyPlotTdrFile( survey, p.name );
      TDFile.deleteFile( filename );
      deleteBackups( filename + BCK_SUFFIX );
      // deleteSurveyPlotExportFiles( survey, p.name )
    }
  }

  // APP_OUT_DIR
  /** @return private export file
   * @param filename file name (with extension if any)
   */
  static File getOutExportFile( String filename )
  {
    return new File( getOutFile( filename ) );
  }


  // -------------- PRIVATE ---------------------------------------------------------

  // /** @return private export file
  //  * @param filename file name (with extension if any)
  //  */
  // static File getPrivateExportFile( String filename )
  // {
  //   return TDFile.getPrivateFile( "export", filename );
  // }

  /** delete symbol files from a folder
   * @param dirname   symbol folder name
   */
  private static void clearSymbolsDir( String dirname )
  {
    // TDLog.v( "clear " + dirname );
    File dir = TDFile.getPrivateDir( dirname );
    File [] files = dir.listFiles();
    if ( files == null || files.length == 0 ) {
      TDLog.e("PATH no symbol files " + dirname + " to clear");
      return;
    }
    for ( int i=0; i<files.length; ++i ) {
      if ( files[i].isDirectory() ) continue;
      if ( ! files[i].delete() ) TDLog.e("File " + files[i].getPath() + " delete failed ");
    }
  }

  // private static File[] getFiles( String dirname, final String extension )
  // {
  //   File dir = TDFile.getTopoDroidFile( dirname );
  //   if ( dir.exists() ) {
  //     return dir.listFiles( new FileFilter() {
  //         public boolean accept( File pathname ) { return pathname.getName().endsWith( extension ); }
  //       } );
  //   }
  //   return null;
  // }

  static void checkOutdir()
  {
    TDLog.v("PATH out path " + APP_OUT_PATH );
    if ( APP_OUT_PATH == null ) return;
    File dir = new File( APP_OUT_PATH );
    if ( ! dir.exists() ) dir.mkdirs();
  }

  /** make sure the file parent directory exists
   * @param fp   file
   *
   * TODO replace with 20230118
   * if ( fp == null || fp.exists() ) return;
   * File fpp fp.getParentFile();
   * if ( fpp != null && ! fpp.exists() ) fpp.mkdirs();
   */
  private static void checkPath( File fp ) // DistoX-SAF
  {
    if ( fp == null ) {
      // TDLog.v( "check path: file null " );
      return;
    }
    if ( fp.exists() ) {
      // TDLog.v( "check path: file exists " + fp.getPath() );
      return;
    }
    File fpp = fp.getParentFile();
    if ( fpp == null || fpp.exists() ) {
      // TDLog.v( "check path: parent file null or exists " + ( (fpp==null) ? "null" : fpp.getPath() ) );
      return;
    }
    if ( ! fpp.mkdirs() ) {
      TDLog.e("check path: failed mkdirs " + fpp.getPath() );
    }
  }

  /** @return true if a file exists
   * @param fp   file 
   */ 
  private static boolean existPath( File fp )
  {
    if ( fp == null ) return false;
    return fp.exists();
  }

  /** compose a file pathname: directory/name extension
   * @param directory    directory pathname / root-name
   * @param name         file name
   * @param ext          file extension (including the dot)
   * @return the file pathname
   */
  private static String getPathname( String directory, String name, String ext ) 
  {
    if ( TDandroid.PRIVATE_STORAGE ) {
      File dir = TDFile.getPrivateDir( directory );
      if  ( ! dir.exists() ) dir.mkdirs();
      return dir.getAbsolutePath() + "/" + name + ext;
    } else {
      checkFilesystemDirs( directory );
      return directory + "/" + name + ext;
    }
  }

  /** compose a file pathname: directory/name
   * @param directory    directory pathname / root-name
   * @param name         file name (including extension)
   * @return the file pathname
   */
  private static String getPathname( String directory, String name ) 
  {
    if ( TDandroid.PRIVATE_STORAGE ) {
      File dir = TDFile.getPrivateDir( directory );
      if  ( ! dir.exists() ) dir.mkdirs();
      return dir.getAbsolutePath() + "/" + name;
    } else {
      checkFilesystemDirs( directory );
      return directory + "/" + name;
    }
  }

  /** get the files inside a folder, with a given extension 
   * @param dirname    folder name
   * @param ext        array of possible extension (use empty list to accept all regular files)
   * @return array of regular files with the given extension
   */ 
  private static File[] getFiles( String dirname, final String[] ext ) // DistoX-SAF
  {
    File dir = TDFile.getTopoDroidFile( dirname );
    if ( dir.exists() ) {
      return dir.listFiles( new FileFilter() {
          public boolean accept( File pathname ) { 
            int ne = ext.length;
            if ( pathname.isDirectory() ) return false;
            if ( ne == 0 ) return true;
            String name = pathname.getName().toLowerCase( Locale.getDefault() );
            for ( int n = 0; n < ne; ++n ) {
              if ( name.endsWith( ext[n] ) ) return true;
            }
            return false;
          }
        } );
    }
    return null;
  }

  // private static File[] getExternalFiles( String dirname )
  // {
  //   File dir = TDFile.getExternalDir( dirname );
  //   if ( ! dir.isDirectory() ) return null;
  //   return dir.listFiles( new FileFilter() {
  //     public boolean accept( File pathname ) { return ( ! pathname.isDirectory() ); }
  //   } );
  // }

  private static void checkFilesystemDirs( String path )
  {
    // TDLog.v("check filesystem dir " + path + " cwd " + ROOT_CW_DIR );
    if ( TDandroid.PRIVATE_STORAGE ) {
      File dir = TDFile.getPrivateDir( path );
      if ( ! dir.exists() ) dir.mkdirs( );
    } else {
      TDFile.makeTopoDroidDir( path );
    }
  }

  private static String checkType( String extension, String[] extensions )
  {
    for ( String e : extensions ) if ( e.equalsIgnoreCase( extension ) ) return e;
    return null;
  }

  private static File[] getInternalFiles( String dirname )
  {
    File dir = TDFile.getPrivateDir( dirname );
    if ( ! dir.isDirectory() ) return null;
    return dir.listFiles( new FileFilter() {
      public boolean accept( File pathname ) { return ( ! pathname.isDirectory() ); }
    } );
  }

  // ================================================================================
  /* FIXME_SKETCH_3D *
  static void deleteSurvey3dFiles( String survey, List< Sketch3dInfo > sketches )
  {
    if ( hasTh3Dir() ) {
      for ( Sketch3dInfo s : sketches ) {
        TDFile.deleteFile( getTh3FileWithExt( survey + "-" + s.name + TH3 ) );
      }
    }
    if ( hasTdr3Dir() ) {
      for ( Sketch3dInfo s : sketches ) {
        TDFile.deleteFile( getTdr3FileWithExt( survey + "-" + s.name + TDR3 ) );
      }
    }
  }
   * END_SKETCH_3D */

  // -----------------------------------------------------------------
  // utils

  /** @return extension string from a filename
   * @param filename   filename or pathname
   */
  public static String getExtension( String filename )
  {
    int pos = filename.lastIndexOf(".");
    if ( pos < 0 ) return null;
    return filename.substring( pos+1 ).toLowerCase( Locale.getDefault() );
  }

  /** @return filename string from a pathname
   * @param filename   (full) pathname
   */
  public static String getFilename( String filename )
  {
    int pos = filename.lastIndexOf("/");
    if ( pos >= 0 ) {
      return filename.substring( pos + 1 );
    }
    return filename;
  }

  /** @return file main name string from a filename or pathname
   * @param filename   filename or (full) pathname
   */
  public static String getMainname( String filename )
  {
    int pos = filename.lastIndexOf("/");
    int qos = filename.lastIndexOf(".");
    if ( pos >= 0 ) {
      if ( qos > pos ) {
        return filename.substring( pos + 1, qos );
      } else {
        return filename.substring( pos + 1 );
      }
    } else {
      if ( qos >= 0 ) {
        return filename.substring( 0, qos );
      } else {
        return filename;
      }
    }
  }

  // MOVE_TO_6 ---------------------------------------------------------------
  // /** transfer from 5.1.40 to 6.1.xx
  //  * @param context    context
  //  * @note when this function is called the external TDX dir does not exist
  //  */
  // public static void moveTo6( Context context /* , TextView tv */ )
  // {
  //   // if ( TDFile.hasExternalDir(null) ) {
  //   //   // TDLog.v("PATH MOVE TO 6 has already external dir CBD");
  //   //   return;
  //   // }
  //   final boolean dry_run = false;
  //   // TDLog.v("PATH move to 6 create CBD");
  //   File cbd = TDFile.getExternalDir( null );
  //   if ( ! cbd.exists() ) {
  //     TDLog.e("PATH move to 6 failed create CBD");
  //     // if ( tv != null) tv.setText("failed to create TDX folder");
  //     return;
  //   }
  //   String sdcard = Environment.getExternalStorageDirectory().getPath();
  //   // if ( tv != null ) tv.setText("moving app files to sdcard + "/Android/data/com.topodroid.TDX");
  //   File priv_dir = TDFile.getPrivateDir( null );
  //   File f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/device10.sqlite" );
  //   copyFile( f, new File( priv_dir, "device10.sqlite" ), dry_run );
  //   f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/symbol/point" );
  //   copyDir( f, new File( priv_dir, "point" ), dry_run );
  //   f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/symbol/line" );
  //   copyDir( f, new File( priv_dir,  "line" ), dry_run );
  //   f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/symbol/area" );
  //   copyDir( f, new File( priv_dir,  "area" ), dry_run );
  //   f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/bin" );
  //   copyDir( f, new File( priv_dir,  "bin" ), dry_run );
  //   // f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/symbol/man" );
  //   // copyDir( f, new File( priv_dir,  "man" ), dry_run );
  //   File cwd = TDFile.getExternalDir( "TopoDroid" );
  //   if ( ! cwd.exists() ) {
  //     TDLog.e("MOVE TO 6: failed create TopoDroid subfolder CWD");
  //     // if ( tv != null) tv.setText("failed to create TopoDroid subfolder");
  //     return;
  //   }
  //   // if ( tv != null ) tv.setText("moving project files to sdcard + "/Documents/TDX/TopoDroid");
  //   f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/distox14.sqlite" );
  //   File f2 = new File( cwd, "distox14.sqlite" );
  //   copyFile( f, f2, dry_run );
  //   f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/zip" );
  //   copyDir( f, new File( cwd, "zip" ), dry_run );
  //   f = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/thconfig" );
  //   copyDir( f, new File( cwd, "thconfig" ), dry_run );
  //   // open database and create survey folders, and move survey files
  //   // TDLog.v("PATH move to 6: database " + f2.getAbsolutePath() );
  //   DataHelper db = new DataHelper( context, f2.getAbsolutePath() );
  //   List<String> surveys = db.selectAllSurveys( );
  //   for ( String survey : surveys ) {
  //     // if ( tv != null ) tv.setText("moving survey " + survey );
  //     File tdr = TDFile.getTopoDroidFile( sdcard + "/Documents/TDX/TopoDroid/" + survey + "/tdr" );
  //     tdr.mkdirs();
  //     // TDInstance.takePersistentPermissions( Uri.fromFile( tdr ) ); // FIXME_PERSISTENT
  //     // move all tdr files of the survey
  //     List<String> plots = db.selectAllPlotNames( survey );
  //     for ( String plot : plots ) {
  //       String plot_name = survey + "-" + plot + ".tdr";
  //       f =  TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/tdr/" + plot_name );
  //       copyFile( f, new File( tdr, plot_name ), dry_run );
  //     }
  //     File audio1 = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/audio/" + survey );
  //     if ( audio1.exists() ) {
  //       File audio2 = TDFile.getTopoDroidFile( sdcard + "/Documents/TDX/TopoDroid/" + survey + "/audio" );
  //       copyDir( audio1, audio2, dry_run );
  //     }
  //     File photo1 = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/photo/" + survey );
  //     if ( photo1.exists() ) {
  //       File photo2 = TDFile.getTopoDroidFile( sdcard + "/Documents/TDX/TopoDroid/" + survey + "/photo" );
  //       copyDir( photo1, photo2, dry_run );
  //     }
  //     File note1 = TDFile.getTopoDroidFile( sdcard + "/Documents/TopoDroid/note/" + survey + ".txt" );
  //     if ( note1.exists() ) {
  //       File note2 = TDFile.getTopoDroidFile( sdcard + "/Documents/TDX/TopoDroid/" + survey + ".txt" );
  //       copyFile( note1, note2, dry_run );
  //     }
  //   }
  //   // if ( tv != null ) tv.setText("done");
  // }

  // /** copy a directory or create it (if not dry_run)
  //  * @param in    input directory
  //  * @param out   output directory
  //  */
  // private static boolean copyDir( File in, File out, boolean dry_run )
  // {
  //   if ( in == null ) {
  //     TDLog.e("PATH copy dir null input"); 
  //     return true;
  //   }
  //   if ( ! in.exists() ) {
  //     TDLog.e("PATH copy dir " + in.getPath() + " not exist");
  //     return true;
  //   }
  //   if ( ! in.isDirectory() ) {
  //     TDLog.e("PATH copy dir " + in.getPath() + " not directory");
  //     return true;
  //   }
  //   if ( ! dry_run ) {
  //     if ( ! out.exists() ) {
  //       if ( ! out.mkdirs() ) return false;
  //       // TDInstance.takePersistentPermissions( Uri.fromFile( out ) ); // FIXME_PERSISTENT
  //     }
  //     if ( ! out.isDirectory() ) return false;
  //   // } else {
  //   //   // TDLog.v("PATH copy dir " + in.getName() + " -> " + out.getName() + " dry run");
  //   }
  //   boolean ret = true;
  //   File[] files = in.listFiles();
  //   if ( files != null ) {
  //     for ( File file1 : files ) {
  //       File file2 = new File( out, file1.getName() );
  //       ret &= copyFile( file1, file2, dry_run );
  //     }
  //   }
  //   return ret;
  // }

  // /** copy a file
  //  * @param in      input file
  //  * @param out     output file
  //  * @param dry_run whether to run dry
  //  * @return true if success
  //  */
  // private static boolean copyFile( File in, File out, boolean dry_run )
  // {
  //   if ( in == null ) {
  //     TDLog.e("PATH copy file null input"); 
  //     return true;
  //   }
  //   if ( !in.exists() ) {
  //     TDLog.e("PATH copy file " + in.getPath() + " not exist");
  //     return true;
  //   }
  //   if ( !in.isFile() ) {
  //     TDLog.e("PATH copy file " + in.getPath() + " not regular");
  //     return true;
  //   }
  //   if ( dry_run ) {
  //     // TDLog.v("PATH copy file " + in.getName() + " -> " + out.getName() + " dry run");
  //     return true;
  //   }
  //   byte[] buf = new byte[4096];
  //   if ( buf == null ) {
  //     TDLog.e("failed alloc buffer");
  //     return false;
  //   }
  //   int len;
  //   int tot_len = 0;
  //   try {
  //     FileInputStream fis = new FileInputStream( in );
  //     FileOutputStream fos = new FileOutputStream( out );
  //     while ( (len = fis.read( buf, 0, 4096 )) > 0 ) {
  //       tot_len += len;
  //       fos.write( buf, 0, len );
  //     }
  //     fos.flush();
  //     fos.close();
  //     fis.close();
  //   } catch ( FileNotFoundException e ) {
  //     TDLog.e("PATH file error " + e.getMessage() );
  //     return false;
  //   } catch ( IOException e ) {
  //     TDLog.e("PATH IO error " + e.getMessage() );
  //     return false;
  //   }
  //   // TDLog.v("PATH copy file " + in.getName() + " -> " + out.getName() + " length " + tot_len );
  //   return true;
  // }
 
}
