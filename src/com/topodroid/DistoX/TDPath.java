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
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;

import android.os.Build;
// import android.provider.DocumentsContract;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.widget.TextView;

public class TDPath
{
  
  // whether not having ANDROID 10
  final static public boolean BELOW_ANDROID_4  = ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT ); // API_19
  final static public boolean BELOW_ANDROID_5  = ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP ); // API_21
  final static public boolean BELOW_ANDROID_10 = ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.P );
  final static public boolean BELOW_ANDROID_11 = ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q );

  final static int NR_BACKUP = 5;
  final static String BCK_SUFFIX = ".bck";

  final static String C3D = ".c3d";
  final static String CSN = ".csn";  // CaveSniper
  // final static String CSV = ".csv";
  final static String DAT = ".dat";
  final static String SVX = ".svx"; // Survex
  final static String TDR = ".tdr";
  // final static private String TMP = ".tmp";

  final static String TOP = ".top"; // PockeTopo
  final static String TH  = ".th";
  final static String TRO = ".tro"; // VisualTopo
  final static String TROX = ".trox"; // VisualTopo
  final static String TXT = ".txt";
  final static String ZIP = ".zip";

  final static String DIR_BIN   = "bin";
  final static String DIR_CCSV  = "ccsv";
  final static String DIR_DUMP  = "dump";
  final static String DIR_POINT = "point";
  final static String DIR_LINE  = "line";
  final static String DIR_AREA  = "area";

  final static String THCONFIG = ".thconfig";
  // final static String TDCONFIG = ".tdconfig";

  // used by Archiver, and Symbol*Library (thru TDFile)
  static String getSymbolPointDirname() { return DIR_POINT; } // "symbol/point"
  static String getSymbolLineDirname()  { return DIR_LINE; }  // "symbol/line"
  static String getSymbolAreaDirname()  { return DIR_AREA; }  // "symbol/area"

  // ------------------------------------------------------------
  // PATHS

  // if BUILD
  // If PATH_CB_DIR is left null the path is set in the method setPaths():
  //    this works with Android-10 but the data are erased when TopoDroid is uninstalled
  //    because the path is Android/data/com.topodroid.DistoX/files
  // With "/sdcard" they remain
  /*
  private static final String EXTERNAL_STORAGE_PATH_10 = Environment.getExternalStorageDirectory().getAbsolutePath();
  private static final String EXTERNAL_STORAGE_PATH_11 =
    BELOW_ANDROID_4 ? null : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
  private static final String EXTERNAL_STORAGE_PATH =  // app base path
    BELOW_ANDROID_4 ? EXTERNAL_STORAGE_PATH_10
      : ( TDFile.hasTopoDroidFile( EXTERNAL_STORAGE_PATH_11, "TopoDroid" ) )? EXTERNAL_STORAGE_PATH_11
	: BELOW_ANDROID_10 ? EXTERNAL_STORAGE_PATH_10
	  : ( BELOW_ANDROID_11  && TDFile.hasTopoDroidFile( EXTERNAL_STORAGE_PATH_10, "TopoDroid") )? EXTERNAL_STORAGE_PATH_10 
	    : EXTERNAL_STORAGE_PATH_11;
  */

  // private static String PATH_CB_DIR  = EXTERNAL_STORAGE_PATH;
  private static String PATH_CB_DIR   = TDFile.getExternalDir(null).getPath();
  private static String PATH_CW_DIR   = PATH_CB_DIR + "/TopoDroid";

  private static String PATH_ZIP      = PATH_CW_DIR + "/zip";
  private static String PATH_TMP      = PATH_CW_DIR + "/tmp";
  private static String PATH_TDCONFIG = PATH_CW_DIR + "/thconfig";
  private static String PATH_C3EXPORT = PATH_CW_DIR + "/c3export";

  private static String APP_SURVEY_PATH   = null;
  private static String APP_PHOTO_PATH    = null;
  private static String APP_AUDIO_PATH    = null;
  private static String APP_NOTE_PATH     = null;
  private static String APP_TDR_PATH      = null;
  private static String APP_C3D_PATH      = null;

  private static String APP_CAVE_PATH = null;   // Polygon
  private static String APP_CAV_PATH = null;    // Topo
  private static String APP_CSV_PATH = null;    // CSV text
  private static String APP_CSX_PATH = null;    // cSurvey
  private static String APP_DAT_PATH = null;    // Compass
  private static String APP_GRT_PATH = null;    // Grottolf
  private static String APP_GTX_PATH = null;    // GHTopo
  private static String APP_DXF_PATH = null;    
  private static String APP_KML_PATH = null;    
  private static String APP_JSON_PATH = null;   
  private static String APP_PLT_PATH = null;    // trackfile
  private static String APP_PNG_PATH = null;    
  private static String APP_PDF_PATH = null;    
  private static String APP_SHP_PATH = null;    // shapefile
  private static String RELATIVE_SHP_TEMP = null;
  private static String APP_SRV_PATH = null;    // Walls
  private static String APP_SUR_PATH = null;    // WinKarst
  private static String APP_SVG_PATH = null;    
  private static String APP_SVX_PATH = null;    // Survex
  private static String APP_TH_PATH = null;     // 
  // private static String APP_TDR3_PATH = null;  
  // private static String APP_TH2_PATH = null;    // 
  // private static String APP_TH3_PATH = null; 
  private static String APP_TNL_PATH = null;    // Tunnel
  private static String APP_TOP_PATH = null;    // PocketTopo
  private static String APP_TRB_PATH = null;    // TopoRobot
  private static String APP_TRO_PATH = null;    // VisualTopo
  private static String APP_XVI_PATH = null;    
  // private static String APP_TLX_PATH = null; 

  private static void clearAppPaths()
  {
    APP_SURVEY_PATH   = null;
    APP_PHOTO_PATH     = null;
    APP_AUDIO_PATH    = null;
    APP_NOTE_PATH     = null;
    APP_TDR_PATH      = null;
    APP_C3D_PATH      = null;

    APP_CAVE_PATH = null;   // Polygon
    APP_CAV_PATH = null;    // Topo
    APP_CSV_PATH = null;    // CSV text
    APP_CSX_PATH = null;    // cSurvey
    APP_DAT_PATH = null;    // Compass
    APP_GRT_PATH = null;    // Grottolf
    APP_GTX_PATH = null;    // GHTopo
    APP_DXF_PATH = null;    
    APP_KML_PATH = null;    
    APP_JSON_PATH = null;   
    APP_PLT_PATH = null;    // trackfile
    APP_PNG_PATH = null;    
    APP_PDF_PATH = null;    
    APP_SHP_PATH = null;    // shapefile
    RELATIVE_SHP_TEMP = null;
    APP_SRV_PATH = null;    // Walls
    APP_SUR_PATH = null;    // WinKarst
    APP_SVG_PATH = null;    
    APP_SVX_PATH = null;    // Survex
    APP_TH_PATH = null;     // 
    // APP_TDR3_PATH = null;  
    // APP_TH2_PATH = null;    // 
    // APP_TH3_PATH = null; 
    APP_TNL_PATH = null;    // Tunnel
    APP_TOP_PATH = null;    // PocketTopo
    APP_TRB_PATH = null;    // TopoRobot
    APP_TRO_PATH = null;    // VisualTopo
    APP_XVI_PATH = null;    
    // APP_TLX_PATH = null; 
  }

  // final static Object mTherionLock   = new Object(); // FIXME-THREAD_SAFE
  final static Object mXSectionsLock = new Object();
  final static Object mSelectionLock = new Object();
  final static Object mCommandsLock  = new Object();
  final static Object mStationsLock  = new Object();
  final static Object mShotsLock     = new Object();
  final static Object mGridsLock     = new Object();
  // final static Object mFixedsLock    = new Object();
  // final static Object mSelectedLock  = new Object();

  static String getDatabase() { return PATH_CW_DIR + "/distox14.sqlite"; }

  // private static String PATH_DEFAULT  = PATH_CW_DIR;
  // static String getDeviceDatabase() { return PATH_DEFAULT + "device10.sqlite"; }
  // public static String getPacketDatabase() { return PATH_DEFAULT + "packet10.sqlite"; }
  
  // when this is called basedir exists and is writable
  static boolean checkBasePath( String path )
  {
    File dir = TDFile.getExternalDir( path ); 
    if ( ! dir.exists() ) {
      if ( ! dir.mkdirs() ) TDLog.Error("mkdir error " + path );
    }

    boolean ret = false;
    try {
      ret = dir.exists() && dir.isDirectory() && dir.canWrite();
    } catch ( SecurityException e ) { }
    // TDLog.v( "check base path: <" + path + ">: " + ret );
    return ret;
  }

  // returns the current base directory (which is unchangeable )
  public static String getCurrentBaseDir() { return PATH_CB_DIR; }

  // return the current work directory
  public static String getCurrentWorkDir() { return PATH_CW_DIR; }
 
  // set the Current Work Directory
  // @param path current work directory
  static void setTdPaths( String path /*, String base */ )
  {
    // TDLog.v( "set paths [0]: path " + path + " base " + base );

    if ( path == null || ! path.toLowerCase().startsWith( "topodroid" ) ) return;

    File dir = TDFile.getExternalDir( path ); // DistoX-SAF
    // TDLog.v( "set paths [4]. Dir " + dir.getPath()  );
    try {
      if ( dir.exists() || dir.mkdirs() ) {
	if ( dir.isDirectory() && dir.canWrite() ) {
	  PATH_CW_DIR   = PATH_CB_DIR + "/" + path;
          PATH_ZIP      = PATH_CW_DIR + "/zip";      checkFilesystemDirs( PATH_ZIP );
          PATH_TMP      = PATH_CW_DIR + "/tmp";      checkFilesystemDirs( PATH_TMP );
          PATH_TDCONFIG = PATH_CW_DIR + "/thconfig"; checkFilesystemDirs( PATH_TDCONFIG  );
          PATH_C3EXPORT = PATH_CW_DIR + "/c3export"; checkFilesystemDirs( PATH_C3EXPORT  );
          setSurveyPaths( null );
	}
      }
    } catch ( SecurityException e ) { 
      TDLog.Error("PATH " + "ext storage security error " + e.getMessage() );
    }
  }

  public static void createSurveyPaths( String survey )
  {
    // TDLog.v( "create survey path " + survey + " base " + PATH_CW_DIR );
    String root = PATH_CW_DIR + "/" + survey; 
    checkFilesystemDirs( root );
    checkFilesystemDirs( root + "/tdr" );
    checkFilesystemDirs( root + "/c3d" );
    checkFilesystemDirs( root + "/note" );
    checkFilesystemDirs( root + "/photo" );
    checkFilesystemDirs( root + "/audio" );
  }

  static void setSurveyPaths( String survey )
  {
    if ( survey == null || survey.length() == 0 ) {
      TDLog.v( "set survey path NULL");
      clearAppPaths();
    } else {
      // TDLog.v( "set survey path " + survey + " base " + PATH_CW_DIR );
      APP_SURVEY_PATH = PATH_CW_DIR + "/" + survey;  checkFilesystemDirs( APP_SURVEY_PATH );
      String root = APP_SURVEY_PATH;
      APP_TDR_PATH   = root + "/tdr";     checkFilesystemDirs( APP_TDR_PATH );
      APP_C3D_PATH   = root + "/c3d";     checkFilesystemDirs( APP_C3D_PATH );
      APP_NOTE_PATH  = root + "/note";    checkFilesystemDirs( APP_NOTE_PATH );
      APP_PHOTO_PATH = root + "/photo";   checkFilesystemDirs( APP_PHOTO_PATH );
      APP_AUDIO_PATH = root + "/audio";   checkFilesystemDirs( APP_AUDIO_PATH );

      APP_CAVE_PATH = root + "/cave";   // Polygon
      APP_CAV_PATH  = root + "/cav";    // Topo
      APP_CSV_PATH  = root + "/csv";    // CSV text
      APP_CSX_PATH  = root + "/csx";    // cSurvey
      APP_DAT_PATH  = root + "/dat";    // Compass
      APP_GRT_PATH  = root + "/grt";    // Grottolf
      APP_GTX_PATH  = root + "/gtx";    // GHTopo
      APP_DXF_PATH  = root + "/dxf";    
      APP_KML_PATH  = root + "/kml";    
      APP_JSON_PATH = root + "/json";   
      APP_PLT_PATH  = root + "/plt";    // trackfile
      APP_PNG_PATH  = root + "/png";    
      APP_PDF_PATH  = root + "/pdf";    
      APP_SHP_PATH  = root + "/shp";    // shapefile
      RELATIVE_SHP_TEMP = survey + "/shp/tmp";
      APP_SRV_PATH  = root + "/srv";    // Walls
      APP_SUR_PATH  = root + "/sur";    // WinKarst
      APP_SVG_PATH  = root + "/svg";    
      APP_SVX_PATH  = root + "/svx";    // Survex
      APP_TH_PATH   = root + "/th";     // 
      // APP_TDR3_PATH = null;  
      // APP_TH2_PATH  = root + "/th";     // 
      // APP_TH3_PATH = null; 
      APP_TNL_PATH  = root + "/tnl";    // Tunnel
      APP_TOP_PATH  = root + "/top";    // PocketTopo
      APP_TRB_PATH  = root + "/trb";    // TopoRobot
      APP_TRO_PATH  = root + "/tro";    // VisualTopo
      APP_XVI_PATH  = root + "/xvi";    
      // APP_TLX_PATH = null; 
    }
  }

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

  // ------------------------------------------------------------------

  public static File[] scanTdconfigDir() // DistoX-SAF
  {
    File dir = TDFile.getTopoDroidFile( PATH_TDCONFIG  );
    if ( ! dir.exists() ) {
      if ( ! dir.mkdirs() ) {
	TDLog.Error("mkdir error");
	return null;
      }
    }
    FilenameFilter filter = new FilenameFilter() {
       public boolean accept( File dir, String name ) {
	 return name.endsWith( "tdconfig" );
       }
    };
    return dir.listFiles( filter );
  }

  // ------------------------------------------------------------------
  // FILE NAMES

  public static String getPathBase() { return PATH_CW_DIR; }

  // used by Archiver
  static String getSqlFile() { return APP_SURVEY_PATH + "/survey.sql"; }

  // used by Archiver, TopoDroidApp
  static String getManifestFile() { return PATH_TMP + "/manifest"; }

  // used by Archiver
  static String getDirFile( String name ) { return APP_SURVEY_PATH + "/" + name; }

  // static String getSymbolFile( String name ) { return name; }

  static boolean hasTdrDir() { return TDFile.hasTopoDroidFile( APP_TDR_PATH ); } // DistoX-SAF
  static boolean hasC3dDir() { return TDFile.hasTopoDroidFile( APP_C3D_PATH ); } // DistoX-SAF

  static File getTdrDir() { return TDFile.makeTopoDroidDir( APP_TDR_PATH ); } // DistoX-SAF
  static File getC3dDir() { return TDFile.makeTopoDroidDir( APP_C3D_PATH ); } // DistoX-SAF
  static String getC3dPath() { return APP_C3D_PATH; } // DistoX-SAF

  public static String getZipFile( String name ) { return PATH_ZIP     + "/" + name; }
  public static String getTmpFile( String name ) { return PATH_TMP     + "/" + name; }
  public static String getTdrFile( String name ) { return APP_TDR_PATH + "/" + name; }
  public static String getC3dFile( String name ) { return APP_C3D_PATH + "/" + name; }

  public static String getTdconfigDir( ) { return PATH_TDCONFIG ; }
  public static String getTdconfigFile( String name ) { return PATH_TDCONFIG + "/" + name; }

  // replaced with TDFile functions
  // public static String getC3exportDir( ) { return PATH_C3EXPORT ; }
  public static String getC3exportPath( String name ) { return PATH_C3EXPORT + "/" + name; }
  // public static String getC3exportPath( String name, String ext ) { return PATH_C3EXPORT + "/" + name + "." + ext; }

  public static String getManFileName( String name ) { return "man/" + name; }

  // these are used to get files and folders for the current survey
  static String getJpgDir( )                { return APP_PHOTO_PATH; }
  static String getAudioDir( )              { return APP_AUDIO_PATH; }
  static String getNoteFile( String name )  { return APP_NOTE_PATH  + "/" + name; }
  static String getJpgFile( String name )   { return APP_PHOTO_PATH + "/" + name; }
  static String getAudioFile( String name ) { return APP_AUDIO_PATH + "/" + name; }

  // @parm name   (reduced) plot name
  static String getSurveyPlotTdrFile( String survey, String name ) { return APP_TDR_PATH + "/" + survey + "-" + name + ".tdr" ; }
  static String getSurveyPlotC3dFile( String survey, String name ) { return APP_C3D_PATH + "/" + survey + "-" + name + ".c3d" ; }

  public static String getSurveyZipFile( String survey ) { return getPathname( PATH_ZIP, survey, ZIP ); }
  public static String getSurveyNoteFile( String title ) { return getPathname( APP_NOTE_PATH, title, TXT ); }
  public static String getTdrFileWithExt( String name )  {
    // TDLog.v( "APP_TDR_PATH " + APP_TDR_PATH + " name " + name );
    return getPathname( APP_TDR_PATH, name, TDR );
  }

  // @param dirname   survey dirname
  // @param name      tdr name (without extension
  public static String getTdrFileWithExt( String dirname, String name )  {
    // TDLog.v( "dirname " + dirname + " name " + name );
    return getPathname( PATH_CW_DIR, dirname + "/" + name, TDR );
  }

  public static String getC3dFileWithExt( String name )  { return getPathname( APP_C3D_PATH, name, C3D ); }

  public static String getCaveFileWithExt( String name ) { checkFilesystemDirs( APP_CAVE_PATH ); return getPathname( APP_CAVE_PATH, name, ".cave" ); } // Polygon
  public static String getCavFileWithExt(  String name ) { checkFilesystemDirs( APP_CAV_PATH );  return getPathname( APP_CAV_PATH,  name, ".cav"  ); } // Topo
  public static String getCsvFileWithExt(  String name ) { checkFilesystemDirs( APP_CSV_PATH );  return getPathname( APP_CSV_PATH,  name, ".csv"  ); }
  public static String getCsxFileWithExt(  String name ) { checkFilesystemDirs( APP_CSX_PATH );  return getPathname( APP_CSX_PATH,  name, ".csx"  ); } // cSurvey
  public static String getDatFileWithExt(  String name ) { checkFilesystemDirs( APP_DAT_PATH );  return getPathname( APP_DAT_PATH,  name, ".dat"  ); }
  public static String getGrtFileWithExt(  String name ) { checkFilesystemDirs( APP_GRT_PATH );  return getPathname( APP_GRT_PATH,  name, ".grt"  ); } // Grottolf
  public static String getGtxFileWithExt(  String name ) { checkFilesystemDirs( APP_GTX_PATH );  return getPathname( APP_GTX_PATH,  name, ".gtx"  ); } // GHTopo
  public static String getDxfFileWithExt(  String name ) { checkFilesystemDirs( APP_DXF_PATH );  return getPathname( APP_DXF_PATH,  name, ".dxf"  ); }
  public static String getKmlFileWithExt(  String name ) { checkFilesystemDirs( APP_KML_PATH );  return getPathname( APP_KML_PATH,  name, ".kml"  ); }
  public static String getJsonFileWithExt( String name ) { checkFilesystemDirs( APP_JSON_PATH ); return getPathname( APP_JSON_PATH, name, ".json" ); }
  public static String getPltFileWithExt(  String name ) { checkFilesystemDirs( APP_PLT_PATH );  return getPathname( APP_PLT_PATH,  name, ".plt"  ); } // trackfile
  public static String getPngFileWithExt(  String name ) { checkFilesystemDirs( APP_PNG_PATH );  return getPathname( APP_PNG_PATH,  name, ".png"  ); }    
  public static String getPdfFileWithExt(  String name ) { checkFilesystemDirs( APP_PDF_PATH );  return getPathname( APP_PDF_PATH,  name, ".pdf"  ); }    
  public static String getShpFileWithExt(  String name ) { checkFilesystemDirs( APP_SHP_PATH );  return getPathname( APP_SHP_PATH,  name, ".shz"  ); }    
  public static String getSrvFileWithExt(  String name ) { checkFilesystemDirs( APP_SRV_PATH );  return getPathname( APP_SRV_PATH,  name, ".srv"  ); } // Walls
  public static String getSurFileWithExt(  String name ) { checkFilesystemDirs( APP_SUR_PATH );  return getPathname( APP_SUR_PATH,  name, ".sur"  ); } // Winkarst
  public static String getSvgFileWithExt(  String name ) { checkFilesystemDirs( APP_SVG_PATH );  return getPathname( APP_SVG_PATH,  name, ".svg"  ); }    
  public static String getSvxFileWithExt(  String name ) { checkFilesystemDirs( APP_SVX_PATH );  return getPathname( APP_SVX_PATH,  name, ".svx"  ); }    
  public static String getThFileWithExt(   String name ) { checkFilesystemDirs( APP_TH_PATH );   return getPathname( APP_TH_PATH,   name, ".th"   ); }    
  public static String getTh2FileWithExt(  String name ) { checkFilesystemDirs( APP_TH_PATH );   return getPathname( APP_TH_PATH,   name, ".th2"  ); }    
  public static String getThconfigFileWithExt(  String name ) { checkFilesystemDirs( APP_TH_PATH );   return getPathname( APP_TH_PATH,   name, ".thconfig"  ); }    
  public static String getTnlFileWithExt(  String name ) { checkFilesystemDirs( APP_TNL_PATH );  return getPathname( APP_TNL_PATH,  name, ".xml"  ); } // Tunnel
  public static String getTopFileWithExt(  String name ) { checkFilesystemDirs( APP_TOP_PATH );  return getPathname( APP_TOP_PATH,  name, ".top"  ); } // PocketTopo
  public static String getTrbFileWithExt(  String name ) { checkFilesystemDirs( APP_TRB_PATH );  return getPathname( APP_TRB_PATH,  name, ".trb"  ); } // TopoRobot
  public static String getTroFileWithExt(  String name ) { checkFilesystemDirs( APP_TRO_PATH );  return getPathname( APP_TRO_PATH,  name, ".tro"  ); } // VisulaTopo
  public static String getTroxFileWithExt( String name ) { checkFilesystemDirs( APP_TRO_PATH );  return getPathname( APP_TRO_PATH,  name, ".trox" ); } // 
  public static String getXviFileWithExt(  String name ) { checkFilesystemDirs( APP_XVI_PATH );  return getPathname( APP_XVI_PATH,  name, ".xvi"  ); } // XTherion

  public static String getShpTempRelativeDir( ) 
  {
    checkFilesystemDirs( APP_SHP_PATH );
    checkFilesystemDirs( APP_SHP_PATH + "/tmp/" );
    return RELATIVE_SHP_TEMP;
  }

  public static File[] getBinFiles()   { return getInternalFiles( DIR_BIN ); }
  public static File[] getCalibFiles() { return getInternalFiles( DIR_CCSV  ); } // DistoX-SAF

  public static File getBinFile( String filename )   { return TDFile.getPrivateFile( DIR_BIN, filename ); }
  public static File getCcsvFile( String filename )  { return TDFile.getPrivateFile( DIR_CCSV, filename ); }
  public static File getDumpFile( String filename )  { return TDFile.getPrivateFile( DIR_DUMP, filename ); }
  public static File getPointFile( String filename ) { return TDFile.getPrivateFile( DIR_POINT, filename ); }
  public static File getLineFile( String filename )  { return TDFile.getPrivateFile( DIR_LINE, filename ); }
  public static File getAreaFile( String filename )  { return TDFile.getPrivateFile( DIR_AREA, filename ); }

  public static File getPointDir( ) { return TDFile.getPrivateDir( DIR_POINT ); }

  public static  void deletePointFile( String filename ) { TDFile.deletePrivateFile( DIR_POINT, filename ); }
  public static  void deleteLineFile( String filename )  { TDFile.deletePrivateFile( DIR_LINE,  filename ); }


  // used only by CWDActivity to list "topodroid" dirs
  static File[] getTopoDroidFiles( String basename ) // DistoX-SAF
  {
    File dir = TDFile.getTopoDroidFile( basename );
    return dir.listFiles( new FileFilter() {
      public boolean accept( File pathname ) { 
	if ( ! pathname.isDirectory() ) return false;
	return ( pathname.getName().toLowerCase(Locale.US).startsWith( "topodroid" ) );
      }
    } );
  }

  // NOTA BENE extensions include the dot, eg, ".th"
  static final String[] IMPORT_EXT        = { TH, TOP, DAT, TRO, CSN, SVX };
  static final String[] IMPORT_EXT_STREAM = { TOP, ZIP };
  static final String[] IMPORT_EXT_READER = { TH, DAT, TRO, TROX, CSN, SVX };

  // static File[] getImportFiles() // DistoX-SAF
  // { 
  //   return getFiles( PATH_IMPORT, IMPORT_EXT );
  // }

  static String checkImportTypeStream( String ext ) { return checkType( ext, IMPORT_EXT_STREAM ); }
  static String checkImportTypeReader( String ext ) { return checkType( ext, IMPORT_EXT_READER ); }

  static String[] getImportTypes() { return new String[] { TH, TOP, DAT, TRO, TROX, CSN, SVX }; }

  static File[] getZipFiles() { return getFiles( PATH_ZIP, new String[] { ZIP } ); } // DistoX-SAF


  // these are used to get the folders when the survey does not exist yet (on import)
  static String getSurveyDir( String survey ) { return PATH_CW_DIR + "/" + survey; } // : /.../emulated/0/Documents/TDX/TopoDroid/survey

  static String getSurveyPhotoDir( String survey ) { return PATH_CW_DIR + "/" + survey + "/photo"; }
  static String getSurveyAudioDir( String survey ) { return PATH_CW_DIR + "/" + survey + "/audio"; }

  static String getSurveyPhotoFile( String survey, String name ) { return PATH_CW_DIR + "/" + survey + "/photo/" + name; }
  static String getSurveyAudioFile( String survey, String name ) { return PATH_CW_DIR + "/" + survey + "/audio/" + name; }

  static String getSurveyJpgFile( String survey, String id )
  {
    String dirpath = PATH_CW_DIR + "/" + survey + "/photo";
    TDFile.makeTopoDroidDir( dirpath );
    return dirpath + "/" + id + ".jpg";
  }

  static String getSurveyWavFile( String survey, String id )
  {
    String dirpath = PATH_CW_DIR + "/" + survey + "/audio";
    TDFile.makeTopoDroidDir( dirpath );
    return dirpath + "/" + id + ".wav";
  }
  
  // used by SHP export
  public static String getSurveyJpgFilename( String survey, String id ) { return survey + "/photo/" + id + ".jpg"; }

  // used by SHP export
  public static String getSurveyWavFilename( String survey, String id ) { return survey + "/audio/" + id + ".wav"; }

  static void rotateBackups( String filename, int rotate ) // filename has suffix BCK_SUFFIX
  {
    if ( rotate <= 0 ) return;
    for ( int i=rotate-1; i>0; --i ) {
      TDFile.moveFile( filename + (i-1), filename + i );
    }
    TDFile.moveFile( filename, filename + "0"  );
  }

  static void renamePlotFiles( String old_name, String new_name )
  {
    String old_tdr = TDPath.getTdrFile( old_name + ".tdr" );
    String new_tdr = TDPath.getTdrFile( new_name + ".tdr" );
    TDFile.renameFile( old_tdr, new_tdr );
    old_tdr = old_tdr + TDPath.BCK_SUFFIX;
    new_tdr = new_tdr + TDPath.BCK_SUFFIX;
    File file1 = TDFile.getTopoDroidFile( old_tdr ); // DistoX-SAF
    File file2 = TDFile.getTopoDroidFile( new_tdr );
    if ( ( ! file1.exists() ) || file2.exists() ) return;
    if ( ! file1.renameTo( file2 ) ) {
      TDLog.Error("bck file rename failed");
      return;
    }
    for ( int i=0; ; ++i ) {
      file1 = TDFile.getTopoDroidFile( old_tdr + i ); // DistoX-SAF
      file2 = TDFile.getTopoDroidFile( new_tdr + i );
      if ( ( ! file1.exists() ) || file2.exists() ) break;
      if ( ! file1.renameTo( file2 ) ) {
        TDLog.Error("bck" + i + " file rename failed");
        break;
      }
    }
  }

  static void deleteSurveyFiles( String survey )
  {
    File imagedir = TDFile.getTopoDroidFile( getSurveyPhotoDir( survey ) ); // DistoX-SAF
    if ( imagedir.exists() ) {
      File[] files = imagedir.listFiles();
      if ( files != null ) {
        for (File f : files) if (!f.delete()) TDLog.Error("file delete error");
      }
      if ( ! imagedir.delete() ) TDLog.Error("Dir delete error");
    }
    TDFile.deleteFile( getSurveyNoteFile( survey ) );
    // deleteSurveyExportFiles( survey )
  }

  static void deleteSurveyDir( String survey )
  {
    String dirpath = getSurveyDir( survey );
    // TDLog.v("delete path " + dirpath );
    File dir = TDFile.getTopoDroidFile( dirpath );
    TDFile.recursiveDeleteDir( dir );
  }

  static void deleteBackups( String filename ) // filename has suffix BCK_SUFFIX
  {
    TDFile.deleteFile( filename );
    for ( int i=NR_BACKUP-1; i>=0; --i ) {
      TDFile.deleteFile( filename + i );
    }
  }

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

  // -------------- PRIVATE ---------------------------------------------------------

  // private static String noSpaces( String s )
  // {
  //   return ( s == null )? null 
  //     : s.trim().replaceAll("\\s+", "_").replaceAll("/", "-").replaceAll("\\*", "+").replaceAll("\\\\", "");
  // }

  private static void clearSymbolsDir( String dirname )
  {
    // TDLog.v( "clear " + dirname );
    File dir = TDFile.getPrivateDir( dirname );
    File [] files = dir.listFiles();
    if ( files == null ) return;
    for ( int i=0; i<files.length; ++i ) {
      if ( files[i].isDirectory() ) continue;
      if ( ! files[i].delete() ) TDLog.Error("File " + files[i].getPath() + " delete failed ");
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

  /** make sure the file parent directory exists
   * @param fp   file
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
    if ( fpp.exists() ) {
      // TDLog.v( "check path: parent file exists " + fpp.getPath() );
      return;
    }
    if ( ! fpp.mkdirs() ) {
      TDLog.Error("check path: failed mkdirs " + fpp.getPath() );
    }
  }

  /** compose a file pathname: directory/name extension
   * @param directory    directory pathname
   * @param name         file name
   * @param ext          file extension (including the dot)
   * @return the file pathname
   */
  private static String getPathname( String directory, String name, String ext ) 
  {
    checkFilesystemDirs( directory );
    return directory + "/" + name + ext;
  }

  /** compose a file pathname: directory/name
   * @param directory    directory pathname
   * @param name         file name (includin extension)
   * @return the file pathname
   */
  private static String getPathname( String directory, String name ) 
  {
    checkFilesystemDirs( directory );
    return directory + "/" + name;
  }

  private static File[] getFiles( String dirname, final String[] ext ) // DistoX-SAF
  {
    File dir = TDFile.getTopoDroidFile( dirname );
    if ( dir.exists() ) {
      return dir.listFiles( new FileFilter() {
          public boolean accept( File pathname ) { 
            int ne = ext.length;
            if ( pathname.isDirectory() ) return false;
            if ( ne == 0 ) return true;
            String name = pathname.getName().toLowerCase(Locale.US);
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
    // TDLog.v("check filesystem dir " + path );
    TDFile.makeTopoDroidDir( path );
  }

  private static String checkType( String ext, String[] exts )
  {
    for ( String e : exts ) if ( e.equals( ext ) ) return e;
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

  // final static String CAV = ".cav"; // Topo
  // final static String CAVE = ".cave"; // Polygon
  // final static String XVI = ".xvi"; // xtherion
  // final static String CSX = ".csx";
  // final static String DXF = ".dxf";
  // final static String GRT = ".grt"; // Grottolf
  // final static String GTX = ".gtx"; // GHTopo
  // final static String HTML = ".html";
  // final static String JSON = ".json";
  // final static String KML = ".kml";
  // final static String PLT = ".plt"; // trackfile
  // final static String PNG = ".png";
  // final static String PDF = ".pdf";
  // final static String SHP = ".shp"; // shapefile
  // final static String SHX = ".shx";
  // final static String DBF = ".dbf";
  // final static String SHZ = ".shz"; // shapefile zip
  // final static String SRV = ".srv"; // Walls
  // final static String SUR = ".sur"; // WinKarst
  // final static String SVG = ".svg";
  // final static String TDR3 = ".tdr3";
  // final static String TNL = ".xml"; // Tunnel XML
  // final static String TH2 = ".th2";
  // final static String TH3 = ".th3";
  // final static String TRB = ".trb"; // TopoRobot

  // private static String PATH_C3D;    //  = APP_SURVEY_PATH + "/c3d/";   // Cave3D
  // private static String PATH_CAVE;   //  = APP_SURVEY_PATH + "/cave/";  // Polygon
  // private static String PATH_CAV;    //  = APP_SURVEY_PATH + "/cav/";   // Topo
  // private static String PATH_CSV;    //  = APP_SURVEY_PATH + "/csv/";   // CSV text
  // private static String PATH_CSX;    //  = APP_SURVEY_PATH + "/csx/";   // cSurvey
  // private static String PATH_DAT;    //  = APP_SURVEY_PATH + "/dat/";   // Compass
  // private static String PATH_GRT;    //  = APP_SURVEY_PATH + "/grt/";   // Grottolf
  // private static String PATH_GTX;    //  = APP_SURVEY_PATH + "/gtx/";   // GHTopo
  // // private static String PATH_DUMP;   //  = APP_SURVEY_PATH + "/dump/";  // DistoX memory dumps
  // private static String PATH_DXF;    //  = APP_SURVEY_PATH + "/dxf/";
  // private static String PATH_KML;    //  = APP_SURVEY_PATH + "/kml/";
  // private static String PATH_JSON;   //  = APP_SURVEY_PATH + "/json/";
  // private static String PATH_PLT;    //  = APP_SURVEY_PATH + "/plt/";   // trackfile
  // private static String PATH_IMPORT; //  = APP_SURVEY_PATH + "/import/";
  // private static String PATH_PNG;    //  = APP_SURVEY_PATH + "/png/";
  // private static String PATH_PDF;    //  = APP_SURVEY_PATH + "/pdf/";
  // private static String PATH_SHP;    //  = APP_SURVEY_PATH + "/shp/";   // shapefile
  // private static String PATH_SRV;    //  = APP_SURVEY_PATH + "/srv/";   // Walls
  // private static String PATH_SUR;    //  = APP_SURVEY_PATH + "/sur/";   // WinKarst
  // private static String PATH_SVG;    //  = APP_SURVEY_PATH + "/svg/";   
  // private static String PATH_SVX;    //  = APP_SURVEY_PATH + "/svx/";   // Survex
  // private static String PATH_TH;     //  = APP_SURVEY_PATH + "/th/";
  // private static String PATH_TDR3;   //  = APP_SURVEY_PATH + "/tdr3/";
  // private static String PATH_TH2;    //  = APP_SURVEY_PATH + "/th2/";
  // private static String PATH_TH3;    //  = APP_SURVEY_PATH + "/th3/";
  // // private static String APP_TMP_PATH;    //  = APP_SURVEY_PATH + "/tmp/";
  // private static String PATH_TNL;    //  = APP_SURVEY_PATH + "/tnl/";   // Tunnel
  // private static String PATH_TOP;    //  = APP_SURVEY_PATH + "/top/";   // PocketTopo
  // private static String PATH_TRB;    //  = APP_SURVEY_PATH + "/trb/";   // TopoRobot
  // private static String PATH_TRO;    //  = APP_SURVEY_PATH + "/tro/";   // VisualTopo
  // private static String PATH_XVI;    //  = APP_SURVEY_PATH + "/xvi/";
  // // private static String APP_TLX_PATH ; //  = APP_SURVEY_PATH + "/tlx/";

  // private static String PATH_BIN  = "/bin/";    // Firmwares  
  // private static String PATH_CCSV = "/ccsv/";  // calib CSV text
  // private static String PATH_MAN  = TDInstance.context.getFileDir(); // PATH_DEFAULT + "/man/"; // User Manual
  // private static String APP_SYMBOL_PATH = "/symbol";
  // private static String APP_POINT_PATH  = APP_SYMBOL_PATH + "/point/";
  // private static String APP_LINE_PATH   = APP_SYMBOL_PATH + "/line/";
  // private static String APP_AREA_PATH   = APP_SYMBOL_PATH + "/area/";

  // static String getSymbolPointFilename( String name ) { return "symbol/point/" + name; }
  // static String getSymbolLineFilename( String name )  { return "symbol/line/"  + name; }
  // static String getSymbolAreaFilename( String name )  { return "symbol/area/"  + name; }

  /* LOAD_MISSING
  // private static String APP_SYMBOL_SAVE_PATH = APP_SYMBOL_PATH + "save/";
  // private static String APP_SAVE_POINT_PATH  = APP_SYMBOL_SAVE_PATH + "point/";
  // private static String APP_SAVE_LINE_PATH   = APP_SYMBOL_SAVE_PATH + "line/";
  // private static String APP_SAVE_AREA_PATH   = APP_SYMBOL_SAVE_PATH + "area/";
  // 
  // static String getSymbolSavePointPath( String filename ) { return APP_SAVE_POINT_PATH + filename; }
  // static String getSymbolSaveLinePath( String filename )  { return APP_SAVE_LINE_PATH + filename; }
  // static String getSymbolSaveAreaPath( String filename )  { return APP_SAVE_AREA_PATH + filename; }
  */

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


  // private static void deleteSurveyExportFiles( String survey )
  // {
  //   // TDFile.deleteFile( getSurveyTlxFile( survey ) );
  //   TDFile.deleteFile( getCavFile( survey + CAV ) );
  //   TDFile.deleteFile( getCaveFile( survey + CAVE ) );
  //   TDFile.deleteFile( getCsvFile( survey + CSV ) );
  //   TDFile.deleteFile( getCsxFile( survey + CSX ) );
  //   TDFile.deleteFile( getDatFile( survey + DAT ) );
  //   TDFile.deleteFile( getDxfFile( survey + DXF ) );
  //   TDFile.deleteFile( getGrtFile( survey + GRT ) );
  //   TDFile.deleteFile( getGtxFile( survey + GTX ) );
  //   TDFile.deleteFile( getKmlFile( survey + KML ) );
  //   TDFile.deleteFile( getJsonFile( survey + JSON ) );
  //   TDFile.deleteFile( getPltFile( survey + PLT ) );
  //   TDFile.deleteFile( getShzFile( survey + SHZ ) );
  //   // deleteShpFiles( survey ); // SHP stations/shots/splays shp/shx/dbf
  //   TDFile.deleteFile( getSrvFile( survey + SRV ) );
  //   TDFile.deleteFile( getSurFile( survey + SUR ) );
  //   TDFile.deleteFile( getSvgFile( survey + SVG ) );
  //   TDFile.deleteFile( getSvxFile( survey + SVX ) );
  //   TDFile.deleteFile( getThFile(  survey + TH  ) );
  //   TDFile.deleteFile( getTopFile( survey + TOP ) );
  //   // TDFile.deleteFile( getTnlFile( survey + TNL ) );
  //   TDFile.deleteFile( getTrbFile( survey + TRB ) );
  //   TDFile.deleteFile( getTroFile( survey + TRO ) );
  // }

  // static void deleteSurveyOverviewFiles( String survey )
  // {
  //   TDFile.deleteFile( getDxfFile( survey + "-p" + DXF ) );
  //   TDFile.deleteFile( getShzFile( survey + "-p" + SHZ ) );
  //   TDFile.deleteFile( getSvgFile( survey + "-p" + SVX ) );
  //   TDFile.deleteFile( getTh2File( survey + "-p" + TH2  ) );
  //   TDFile.deleteFile( getXviFile( survey + "-p" + XVI ) );

  //   TDFile.deleteFile( getDxfFile( survey + "-s" + DXF ) );
  //   TDFile.deleteFile( getShzFile( survey + "-s" + SHZ ) );
  //   TDFile.deleteFile( getSvgFile( survey + "-s" + SVX ) );
  //   TDFile.deleteFile( getTh2File( survey + "-s" + TH2  ) );
  //   TDFile.deleteFile( getXviFile( survey + "-s" + XVI ) );
  // }

  // static private void deleteShpFiles( String survey )
  // {
  //   File dir = TDFile.getTopoDroidFile( getShpPath( survey ) );
  //   if ( dir.exists() ) {
  //     for ( String filename : dir.list() ) {
  //       (TDFile.getTopoDroidFile( dir, filename )).delete();
  //     }
  //     dir.delete();
  //   }
  // } 

  // static void deleteShpDirs( String survey, List< String > plots ) 
  // {
  //   deleteShpFiles( survey );
  //   if ( plots != null ) {
  //     for ( String plot : plots ) {
  //       deleteShpFiles( survey + "-" + plot );
  //     }
  //   }
  // }

  // private static void deleteSurveyPlotExportFiles( String survey, String pname )
  // {
  //   TDFile.deleteFile( getSurveyPlotCsxFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotDxfFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotPngFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotSvgFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotShzFile( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotTh2File( survey, pname ) );
  //   TDFile.deleteFile( getSurveyPlotXviFile( survey, pname ) );
  // }

  // static File getTmpDir() { return TDFile.getTopoDroidFile( APP_TMP_PATH ); } // DistoX-SAF
  // static String getTmpFileWithExt( String name ) { return getPathname( APP_TMP_PATH, name, TMP ); }
  // static File getCacheFileWithExt( String name ) { return TDFile.getAppCacheFile( name + TMP ); }

  // static String getTdr3FileWithExt( String name ) { return getPathname( PATH_TDR3, name, TDR3 ); }
  // static String getTh2FileWithExt( String name ) { return getPathname( PATH_TH2, name, TH2 ); }
  // static String getTnlFileWithExt( String name ) { return getPathname( PATH_TNL, name, TNL ); }
  // static String getTh3FileWithExt( String name ) { return getPathname( PATH_TH3, name, TH3 ); }
  // static String getDxfFileWithExt( String name ) { return getPathname( PATH_DXF, name, DXF ); }
  // static String getSvgFileWithExt( String name ) { return getPathname( PATH_SVG, name, SVG ); }
  // static String getXviFileWithExt( String name ) { return getPathname( PATH_XVI, name, XVI ); }
  // static String getPngFileWithExt( String name ) { return getPathname( PATH_PNG, name, PNG ); }
  // static String getPdfFileWithExt( String name ) { return getPathname( PATH_PDF, name, PDF ); }
  // static String getC3dFileWithExt( String name ) { return getPathname( PATH_C3D, name, C3D ); }

  // static String getShzFileWithExt( String name ) { return getPathname( PATH_SHP, name, SHZ ); }
  // static String getShpBasepath( String name )    { return getPathname( PATH_SHP, name ); }
  // static String getShpPath( String name )        { return PATH_SHP + name; }


  // static String getSurveyTlxFile( String survey ) { return getPathname( APP_TLX_PATH, survey, TLX ); }
  // static String getSurveyThFile( String survey ) { return getPathname( PATH_TH, survey, TH ); }
  // static String getSurveyCsvFile( String survey ) { return getPathname( PATH_CSV, survey, CSV ); }
  // static String getSurveyCsxFile( String survey ) { return getPathname( PATH_CSX, survey, CSX ); }
  // static String getSurveyCsxFile( String survey, String name ) { return getPathname( PATH_CSX, survey + "-" + name, CSX ); }
  // static String getSurveyCaveFile( String survey ) { return getPathname( PATH_CAVE, survey, CAVE ); }
  // static String getSurveyCavFile( String survey ) { return getPathname( PATH_CAV, survey, CAV ); }
  // static String getSurveyDatFile( String survey ) { return getPathname( PATH_DAT, survey, DAT ); }
  // static String getSurveyDxfFile( String survey ) { return getPathname( PATH_DXF, survey, DXF ); }
  // static String getSurveyGrtFile( String survey ) { return getPathname( PATH_GRT, survey, GRT ); }
  // static String getSurveyGtxFile( String survey ) { return getPathname( PATH_GTX, survey, GTX ); }
  // static String getSurveyKmlFile( String survey ) { return getPathname( PATH_KML, survey, KML ); }
  // static String getSurveyJsonFile( String survey ) { return getPathname( PATH_JSON, survey, JSON ); }
  // static String getSurveyPltFile( String survey ) { return getPathname( PATH_PLT, survey, PLT ); }
  // static String getSurveyShzFile( String survey ) { return getPathname( PATH_SHP, survey, SHZ ); }
  // static String getSurveySrvFile( String survey ) { return getPathname( PATH_SRV, survey, SRV ); }
  // static String getSurveySurFile( String survey ) { return getPathname( PATH_SUR, survey, SUR ); }
  // static String getSurveySvxFile( String survey ) { return getPathname( PATH_SVX, survey, SVX ); }
  // static String getSurveyTopFile( String survey ) { return getPathname( PATH_TOP, survey, TOP ); }
  // static String getSurveyTrbFile( String survey ) { return getPathname( PATH_TRB, survey, TRB ); }
  // static String getSurveyTroFile( String survey ) { return getPathname( PATH_TRO, survey, TRO ); }

  // static String getCaveFile( String name )   { return PATH_CAVE + name; }
  // static String getCavFile( String name )    { return PATH_CAV + name; }
  // static String getCsvFile( String name )    { return PATH_CSV + name; }
  // static String getCsxFile( String name )    { return PATH_CSX + name; }
  // static String getDatFile( String name )    { return PATH_DAT + name; }
  // static String getGrtFile( String name )    { return PATH_GRT + name; }
  // static String getGtxFile( String name )    { return PATH_GTX + name; }
  // static String getDxfFile( String name )    { return PATH_DXF + name; }
  // static String getKmlFile( String name )    { return PATH_KML + name; }
  // static String getJsonFile( String name )   { return PATH_JSON + name; }
  // static String getPltFile( String name )    { return PATH_PLT + name; }
  // static String getPngFile( String name )    { return PATH_PNG + name; }
  // static String getPdfFile( String name )    { return PATH_PDF + name; }
  // static String getSrvFile( String name )    { return PATH_SRV + name; }
  // static String getSurFile( String name )    { return PATH_SUR + name; }
  // static String getSvgFile( String name )    { return PATH_SVG + name; }
  // static String getSvxFile( String name )    { return PATH_SVX + name; }
  // static String getShzFile( String name )    { return PATH_SHP + name; }
  // static String getThFile( String name )     { return PATH_TH + name; }
  // static String getTh2File( String name )    { return PATH_TH2 + name; }
  // static String getTh3File( String name )    { return PATH_TH3 + name; }
  // static String getTopFile( String name )    { return PATH_TOP + name; }
  // static String getTnlFile( String name )    { return PATH_TNL + name; }
  // static String getTrbFile( String name )    { return PATH_TRB + name; }
  // static String getTroFile( String name )    { return PATH_TRO + name; }
  // static String getXviFile( String name )    { return PATH_XVI + name; }
  // static String getC3dFile( String name )    { return PATH_C3D + name; }

  // public static String getDumpFilename( String name )    { return "dump/" + name; }
  // public static String getBinFilename( String name )     { return "bin/" + name; }
  // public static String getCCsvFilename( String name )    { return "ccsv/" + name; }

  // static String getSurveyPlotDxfFile( String survey, String name ) { return PATH_DXF + survey + "-" + name + DXF ; }
  // static String getSurveyPlotSvgFile( String survey, String name ) { return PATH_SVG + survey + "-" + name + SVG ; }
  // // static String getSurveyPlotHtmFile( String survey, String name ) { return PATH_SVG + survey + "-" + name + HTML ; }
  // static String getSurveyPlotTh2File( String survey, String name ) { return PATH_TH2 + survey + "-" + name + TH2 ; }
  // static String getSurveyPlotTnlFile( String survey, String name ) { return PATH_TNL + survey + "-" + name + TNL ; }
  // static String getSurveyPlotPngFile( String survey, String name ) { return PATH_PNG + survey + "-" + name + PNG ; }
  // static String getSurveyPlotPdfFile( String survey, String name ) { return PATH_PDF + survey + "-" + name + PDF ; }
  // static String getSurveyPlotXviFile( String survey, String name ) { return PATH_XVI + survey + "-" + name + XVI ; }
  // static String getSurveyPlotCsxFile( String survey, String name ) { return PATH_CSX + survey + "-" + name + CSX ; }
  // static String getSurveyPlotC3dFile( String survey, String name ) { return PATH_C3D + survey + "-" + name + C3D ; }
  // // static String getSurveyPlotShpDir( String survey, String name ) { return PATH_SHP + survey + "-" + name ; }
  // static String getSurveyPlotShzFile( String survey, String name ) { return PATH_SHP + survey + "-" + name + SHZ ; }

  // static String getSurveySketchInFile( String survey, String name ) { return PATH_TH3 + survey + "-" + name + TH3 ; }
  // static String getSurveySketchOutFile( String survey, String name ) { return PATH_TDR3 + survey + "-" + name + TDR3 ; }

  // LOAD_MISSING
  // static String getSymbolSaveFile( String name ) { return APP_SYMBOL_SAVE_PATH + name; }

  // static boolean hasTdr3Dir() { return (TDFile.getTopoDroidFile( PATH_TDR3 )).exists(); }
  // static boolean hasTh2Dir() { return (TDFile.getTopoDroidFile( PATH_TH2 )).exists(); }
  // static boolean hasTh3Dir() { return (TDFile.getTopoDroidFile( PATH_TH3 )).exists(); }
  // static boolean hasPngDir() { return (TDFile.getTopoDroidFile( PATH_PNG )).exists(); }
  // static boolean hasPdfDir() { return (TDFile.getTopoDroidFile( PATH_PDF )).exists(); }
  // static boolean hasDxfDir() { return (TDFile.getTopoDroidFile( PATH_DXF )).exists(); }
  // static boolean hasKmlDir() { return (TDFile.getTopoDroidFile( PATH_KML )).exists(); }
  // static boolean hasJsonDir() { return (TDFile.getTopoDroidFile( PATH_JSON )).exists(); }
  // static boolean hasPltDir() { return (TDFile.getTopoDroidFile( PATH_PLT )).exists(); }
  // static boolean hasSvgDir() { return (TDFile.getTopoDroidFile( PATH_SVG )).exists(); }
  // static boolean hasXviDir() { return (TDFile.getTopoDroidFile( PATH_XVI )).exists(); }

  // static File getPngDir() { return TDFile.makeDir( PATH_PNG ); } 
  // static File getPdfDir() { return TDFile.makeDir( PATH_PDF ); } 
  // static String getImportFile( String name ) { return PATH_IMPORT + name; }
  // static String getTdr3File( String name )   { return PATH_TDR3 + name; }

  // private static void setExportPaths()
  // {
  //   APP_TLX_PATH = APP_SURVEY_PATH + "/tlx";
  //   checkDirs( APP_TLX_PATH );
  //   PATH_C3D  = APP_SURVEY_PATH + "/c3d"  + File.separator;    // FIXME checkDirs( PATH_C3D );
  //   PATH_CAV  = APP_SURVEY_PATH + "/cav"  + File.separator;    // FIXME checkDirs( PATH_CAV );
  //   PATH_CAVE = APP_SURVEY_PATH + "/cave" + File.separator;    // FIXME checkDirs( PATH_CAVE );
  //   PATH_CSV  = APP_SURVEY_PATH + "/csv"  + File.separator;    // FIXME checkDirs( PATH_CSV );
  //   PATH_CSX  = APP_SURVEY_PATH + "/csx"  + File.separator;    // FIXME checkDirs( PATH_CSX );
  //   PATH_DAT  = APP_SURVEY_PATH + "/dat"  + File.separator;    // FIXME checkDirs( PATH_DAT );
  //   PATH_DXF  = APP_SURVEY_PATH + "/dxf"  + File.separator;    // FIXME checkDirs( PATH_DXF );
  //   PATH_GRT  = APP_SURVEY_PATH + "/grt"  + File.separator;    // FIXME checkDirs( PATH_GRT );
  //   PATH_GTX  = APP_SURVEY_PATH + "/gtx"  + File.separator;    // FIXME checkDirs( PATH_GTX );
  //   PATH_JSON = APP_SURVEY_PATH + "/json" + File.separator;    // FIXME checkDirs( PATH_JSON );
  //   PATH_KML  = APP_SURVEY_PATH + "/kml"  + File.separator;    // FIXME checkDirs( PATH_KML );
  //   PATH_PLT  = APP_SURVEY_PATH + "/plt"  + File.separator;    // FIXME checkDirs( PATH_PLT );
  //   PATH_PNG  = APP_SURVEY_PATH + "/png"  + File.separator;    // FIXME checkDirs( PATH_PNG );
  //   PATH_PDF  = APP_SURVEY_PATH + "/pdf"  + File.separator;    // FIXME checkDirs( PATH_PDF );
  //   PATH_SHP  = APP_SURVEY_PATH + "/shp"  + File.separator;    // FIXME checkDirs( PATH_SHP );
  //   PATH_SRV  = APP_SURVEY_PATH + "/srv"  + File.separator;    // FIXME checkDirs( PATH_SRV );
  //   PATH_SUR  = APP_SURVEY_PATH + "/sur"  + File.separator;    // FIXME checkDirs( PATH_SUR );
  //   PATH_SVG  = APP_SURVEY_PATH + "/svg"  + File.separator;    // FIXME checkDirs( PATH_SVG );
  //   PATH_SVX  = APP_SURVEY_PATH + "/svx"  + File.separator;    // FIXME checkDirs( PATH_SVX );
  //   PATH_TDR3 = APP_SURVEY_PATH + "/tdr3" + File.separator;    checkDirs( PATH_TDR3 );
  //   PATH_TH   = APP_SURVEY_PATH + "/th"   + File.separator;    // FIXME checkDirs( PATH_TH );
  //   PATH_TH2  = APP_SURVEY_PATH + "/th2"  + File.separator;    checkDirs( PATH_TH2 );
  //   PATH_TH3  = APP_SURVEY_PATH + "/th3"  + File.separator;    checkDirs( PATH_TH3 );
  //   PATH_TNL  = APP_SURVEY_PATH + "/tnl"  + File.separator;    // FIXME checkDirs( PATH_TNL );
  //   PATH_TOP  = APP_SURVEY_PATH + "/top"  + File.separator;    // FIXME checkDirs( PATH_TOP );
  //   PATH_TRB  = APP_SURVEY_PATH + "/trb"  + File.separator;    // FIXME checkDirs( PATH_TRB );
  //   PATH_TRO  = APP_SURVEY_PATH + "/tro"  + File.separator;    // FIXME checkDirs( PATH_TRO );
  //   PATH_XVI  = APP_SURVEY_PATH + "/xvi"  + File.separator;    // FIXME checkDirs( PATH_XVI );
  //   APP_TMP_PATH   = APP_SURVEY_PATH + "/tmp"   + File.separator; checkDirs( APP_TMP_PATH );
  //   PATH_IMPORT = APP_SURVEY_PATH + "/import" + File.separator;   checkDirs( PATH_IMPORT );
  // }

  // static private void checkExternalDirs( String path )
  // {
  //   TDFile.makeExternalDir( path );
  // }

  // public static void checkCCsvDir() { checkExternalDirs( DIR_CCSV ); }
  // public static void checkBinDir()  { checkExternalDirs( DIR_BIN ); }

  // static void checkManDir() { checkDirs( PATH_MAN ); }

  // -----------------------------------------------------------------
  // utils

  public static String getExtension( String filename )
  {
    int pos = filename.lastIndexOf(".");
    if ( pos < 0 ) return null;
    return filename.substring( pos+1 ).toLowerCase();
  }

  public static String getFilename( String filename )
  {
    int pos = filename.lastIndexOf("/");
    if ( pos >= 0 ) {
      return filename.substring( pos + 1 );
    }
    return filename;
  }

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

  // ---------------------------------------------------------------
  // transfer from 5.1.40 to 60.1.xx
  public static void moveTo6( Context context, TextView tv )
  {
    if ( TDFile.hasCBD() ) return;

    File cbd = TDFile.getCBD( null, true );
    if ( ! cbd.exists() ) {
      TDLog.Error("failed create CBD");
      tv.setText("failed to create TDX folder");
      return;
    }
    tv.setText("moving app files to /sdcard/Android/data/com.topodroid.DistoX");
    TDFile.renameFile( "/sdcard/Documents/TopoDroid/device10.sqlite", "/sdcard/Android/data/com.topodroid.DistoX/files/device10.sqlite" );
    TDFile.renameFile( "/sdcard/Documents/TopoDroid/symbol/point", "/sdcard/Android/data/com.topodroid.DistoX/files/point" );
    TDFile.renameFile( "/sdcard/Documents/TopoDroid/symbol/line", "/sdcard/Android/data/com.topodroid.DistoX/files/line" );
    TDFile.renameFile( "/sdcard/Documents/TopoDroid/symbol/area", "/sdcard/Android/data/com.topodroid.DistoX/files/area" );
    TDFile.renameFile( "/sdcard/Documents/TopoDroid/symbol/bin", "/sdcard/Android/data/com.topodroid.DistoX/files/bin" );
    // TDFile.renameFile( "/sdcard/Documents/TopoDroid/symbol/man", "/sdcard/Android/data/com.topodroid.DistoX/files/man" );

    File cwd = TDFile.getCBD( "TopoDroid", true );
    if ( ! cwd.exists() ) {
      TDLog.Error("failed create CWD");
      tv.setText("failed to create TopoDroid subfolder");
      return;
    }
    tv.setText("moving project files to /sdcard/Documents/TDX/TopoDroid");
    TDFile.renameFile( "/sdcard/Documents/TopoDroid/distox14.sqlite", "/sdcard/Documents/TDX/TopoDroid/distox14.sqlite" );
    TDFile.renameFile( "/sdcard/Documents/TopoDroid/zip", "/sdcard/Documents/TDX/TopoDroid/zip" );
    TDFile.renameFile( "/sdcard/Documents/TopoDroid/thconfig", "/sdcard/Documents/TDX/TopoDroid/thconfig" );

    // open database and create survey folders, and move survey files
    DataHelper db = new DataHelper( context );
    List<String> surveys = db.selectAllSurveys( );
    for ( String survey : surveys ) {
      tv.setText("moving survey " + survey );
      File tdrs2 = TDFile.getTopoDroidFile( "/sdcard/Documents/TDX/TopoDroid/" + survey + "/tdr" );
      tdrs2.mkdirs();
      // move all tdr files of the survey
      List<String> plots = db.selectAllPlotNames( survey );
      for ( String plot : plots ) {
        File tdr1 =  TDFile.getTopoDroidFile( "/sdcard/Documents/TopoDroid/tdr/" + survey + "-" + plot + ".tdr" );
        if ( tdr1.exists() ) {
          File tdr2 =  TDFile.getTopoDroidFile( "/sdcard/Documents/TDX/TopoDroid/" + survey + "/tdr/" + survey + "-" + plot + ".tdr" );
          tdr1.renameTo( tdr2 );
        }
      }

      File audio1 = TDFile.getTopoDroidFile( "/sdcard/Documents/TopoDroid/audio/" + survey );
      if ( audio1.exists() ) {
        File audio2 = TDFile.getTopoDroidFile( "/sdcard/Documents/TDX/TopoDroid/" + survey + "/audio" );
        audio1.renameTo( audio2 );
      }

      File photo1 = TDFile.getTopoDroidFile( "/sdcard/Documents/TopoDroid/photo/" + survey );
      if ( photo1.exists() ) {
        File photo2 = TDFile.getTopoDroidFile( "/sdcard/Documents/TDX/TopoDroid/" + survey + "/photo" );
        photo1.renameTo( photo2 );
      }

      File note1 = TDFile.getTopoDroidFile( "/sdcard/Documents/TopoDroid/note/" + survey + ".txt" );
      if ( note1.exists() ) {
        File note2 = TDFile.getTopoDroidFile( "/sdcard/Documents/TDX/TopoDroid/" + survey + ".txt" );
        note1.renameTo( note2 );
      }
    }
    tv.setText("done");
  }

}
