/* @file TDConst.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid numerical constants
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;

import android.text.InputType;

public class TDConst
{
  public static final int SURVEY_FORMAT_NONE = -1;
  public static final int SURVEY_FORMAT_TH  = 0;
  public static final int SURVEY_FORMAT_TLX = 1;
  public static final int SURVEY_FORMAT_DAT = 2; // Compass     DatFile
  public static final int SURVEY_FORMAT_SVX = 3; // Survex      SvxFile
  public static final int SURVEY_FORMAT_TRO = 4; // VisualTopo  TroFile
  public static final int SURVEY_FORMAT_CSV = 5;
  public static final int SURVEY_FORMAT_DXF = 6;
  public static final int SURVEY_FORMAT_CSX = 7; // cSurvey     CsxFile
  public static final int SURVEY_FORMAT_TOP = 8; // PocketTopo  TopFile
  public static final int SURVEY_FORMAT_SRV = 9; // Walls       SrvFile
  public static final int SURVEY_FORMAT_KML = 10; //Keyhole     KmlFile
  // public static final int SURVEY_FORMAT_PLT = 11; // track file PltFile OziExplorer
  public static final int SURVEY_FORMAT_GPX = 11; // track file GpxFile

  // public static final int SURVEY_FORMAT_PNG = 12; // NO_PNG
  public static final int SURVEY_FORMAT_SVG = 13;
  public static final int SURVEY_FORMAT_TH2 = 14;
  public static final int SURVEY_FORMAT_TH3 = 15;

  public static final int SURVEY_FORMAT_PLG = 16; // Polygon   CaveFile
  public static final int SURVEY_FORMAT_CAV = 17; // Topo      CavFile
  public static final int SURVEY_FORMAT_GRT = 18; // Grottolf
  public static final int SURVEY_FORMAT_GTX = 19; // GHTopo    GtxFile
  public static final int SURVEY_FORMAT_SUR = 20; // WinKarst  SurFile
  public static final int SURVEY_FORMAT_TRB = 21; // TopoRobot TrbFile
  // public static final int SURVEY_FORMAT_JSON = 22; // GeoJSON  JsonFile
  public static final int SURVEY_FORMAT_SHP = 23; // Shapefile ShpFile
  public static final int SURVEY_FORMAT_XVI = 24; // xtherion  XviFile
  // public static final int SURVEY_FORMAT_TNL = 25; // Tunnel    TnlFile
  // public static final int SURVEY_FORMAT_C3D = 26; // Cave3D    C3dFile
  public static final int SURVEY_FORMAT_PDF = 27; // PDF
  public static final int SURVEY_FORMAT_SNP = 28; // CaveSniper
  // public static final int SURVEY_FORMAT_TROX = 29; // VisualTopo X
  
  public static final int SURVEY_FORMAT_ZIP     = 30;

  public static final int SURVEY_FORMAT_GLTF    = 31;
  public static final int SURVEY_FORMAT_CGAL    = 32;
  public static final int SURVEY_FORMAT_STL     = 33;
  public static final int SURVEY_FORMAT_STL_BIN = 34;
  public static final int SURVEY_FORMAT_LAS_BIN = 35;
  public static final int SURVEY_FORMAT_SERIAL  = 36;
  // public static final int SURVEY_FORMAT_PNM     = 37; // NO_PNM

  public static final String[] mMimeType = {
    "application/octet-stream", //  0 Therion
    "application/tlx",          //    unused
    "application/octet-stream", //    Compass
    "application/octet-stream", //    Survex
    "application/octet-stream", //    VisualTopo
    "text/csv",                 //  5 *CSV
    "application/dxf",          //  6 *DXF
    "application/octet-stream", //  7 *cSurvey
    null, // "application/octet-stream", //  8 PocketTopo
    "application/octet-stream", //  9 Walls
    "application/vnd",          // 10 *KML
    // "application/octet-stream", //    trackfile (.plt)
    "application/octet-stream", // 11 trackfile (.gpx)
    null, // "image/png",                // 12 *PNG
    "image/svg+kml",            // 13 *SVG
    "application/octet-stream", // 14 Therion-2
    "application/octet-stream", // 15 Therion-3

    "application/octet-stream", // 16 Polygon (.cave)
    "application/octet-stream", // 17 Topo (.cav)
    null, // "application/octet-stream", // 18 Grottolf
    "application/octet-stream", // 19 GhTopo
    null, // "application/octet-stream", // 20 WinKarst
    "application/octet-stream", // 21 TopoRobot
    null, // "application/json",         // 22 *Json
    "application/shp",          // 23
    "application/xvi",          // 24
    null, // "application/xml",          // 25 *Tunnel
    null, // "application/octet-stream", // 26 *Cave3D
    "application/pdf",          // 27 *PDF
    "application/octet-stream", // 28 CaveSniper
    "application/octet_stream", // 29 *trox
    "application/zip",          // 30 ZIP
    "application/octet-stream", // 31 glTF
    "application/octet-stream", // 32 CGAL
    "application/octet-stream", // 33 STL
    "application/octet-stream", // 34 STL binary
    "application/octet-stream", // 35 LAS binary
    "test/plain",               // 36 serialized
    // "image/x-portable-pixmap",  // 37 NO_PNM
  };

  // ======= IMPORT ======
  public static final String[] mSurveyImportTypes = { 
    "ZIP", 
    "Compass",
    "CaveSniper",
    "Survex",
    "Therion",
    "VisualTopo",
    // "VisualTopo-X",
    "Walls",
    "PocketTopo"
  };

  private static final int[] mSurveyImportIndex = {
    SURVEY_FORMAT_ZIP,
    SURVEY_FORMAT_DAT,
    SURVEY_FORMAT_SNP,
    SURVEY_FORMAT_SVX,
    SURVEY_FORMAT_TH,
    SURVEY_FORMAT_TRO,
    // SURVEY_FORMAT_TROX,
    SURVEY_FORMAT_SRV,
    SURVEY_FORMAT_TOP
  };

  // ======= MODEL EXPORT  ====== see TopoGL::getFilename()

  // These indices coincides with ModelType
  public final static int MODEL_POS_GLTF    = 0;
  public final static int MODEL_POS_CGAL    = 1;
  public final static int MODEL_POS_STL     = 2;
  public final static int MODEL_POS_STL_BIN = 3;
  public final static int MODEL_POS_LAS_BIN = 4;
  public final static int MODEL_POS_DXF     = 5;
  public final static int MODEL_POS_SHAPEFILE = 6;
  public final static int MODEL_POS_GPX     = 7;

  public static final String[] mModelExportTypes = {
    "gLTF",
    "CGAL",
    "STL",
    "STL-bin",
    "LAS-bin",
    "DXF",
    "KML",
    "Shapefile",
    "GPX"
  };

  public static final String[] mModelExportTypesNoGeo = {
    "gLTF",
    "CGAL",
    "STL",
    "STL-bin",
    "LAS-bin",
    "DXF"
    // "KML",
    // "Shapefile"
    // "GPX"
  };

  private static final int[] mModelExportIndex = {
    SURVEY_FORMAT_GLTF,
    SURVEY_FORMAT_CGAL,
    SURVEY_FORMAT_STL,
    SURVEY_FORMAT_STL_BIN,
    SURVEY_FORMAT_LAS_BIN,
    SURVEY_FORMAT_DXF,
    SURVEY_FORMAT_KML,
    SURVEY_FORMAT_SHP,
    SURVEY_FORMAT_GPX
  };

  /** return the model export name (null if illegal type)
   * @param type    export type
   * @param name    model name
   */
  public static String getModelFilename( int type, String name )
  {
    switch( type ) {
      case ModelType.GLTF:       return name + ".gltf";
      case ModelType.CGAL_ASCII: return name + ".cgal";
      case ModelType.STL_ASCII:  return name + ".stl";
      case ModelType.STL_BINARY: return name + ".stl";
      case ModelType.LAS_BINARY: return name + ".las";
      case ModelType.DXF_ASCII:  return name + ".dxf";
      case ModelType.KML_ASCII:  return name + ".kml";
      case ModelType.SHP_ASCII:  return name + ".shz";
      case ModelType.GPX_ASCII:  return name + ".gpx";
      // case ModelType.SERIAL: return name + ".txt";
    }
    return null;
  }

  // ======= DATA EXPORT  ======

  public final static int SURVEY_POS_ZIP       = 0;
  public final static int SURVEY_POS_COMPASS   =  1;
  public final static int SURVEY_POS_CSURVEY   =  2;
  public final static int SURVEY_POS_GHTOPO    =  3;
  public final static int SURVEY_POS_POLYGON   =  4;
  public final static int SURVEY_POS_SURVEX    =  5;
  public final static int SURVEY_POS_THERION   =  6;
  public final static int SURVEY_POS_TOPO      =  7;
  public final static int SURVEY_POS_TOPOROBOT =  8;
  public final static int SURVEY_POS_VTOPO     =  9;
  public final static int SURVEY_POS_VTOPOX    = -9;
  public final static int SURVEY_POS_WALLS     = 10;
  public final static int SURVEY_POS_WINKARST  = 11;
  public final static int SURVEY_POS_CSV       = 12;
  public final static int SURVEY_POS_DXF       = 13;
  public final static int SURVEY_POS_KML       = 14;
  public final static int SURVEY_POS_GPX       = 15;
  // public final static int SURVEY_POS_GEOJSON   = 16;
  public final static int SURVEY_POS_SHAPEFILE = 16;


  public static boolean[] mSurveyExportEnable = { true, // "ZIP", 
    true,  // "Compass",   // 1
    false, // "cSurvey",
    false, // "GHTopo",
    // false, // "Grottolf",
    // false, // "PocketTopo", // 5
    false, // "Polygon",    //  4
    false, // "Survex",     //  5
    true,  // "Therion",    //  6
    false, // "Topo",
    false, // "TopoRobot",  //  8
    true,  // "VisualTopo",
    false, // "Walls", 
    false, // "WinKarst",   // 11
    true,  // "CSV",
    false, // "DXF",
    false, // "KML",        // 14
    // false, // "OziExplorer",
    false, // "GPX",
    // false, // "GeoJSON",
    false, // "Shapefile",
  };

  /** @return the array of enabled export types
   * @param with_geo  whether to include geo-types
   */
  public static String[] surveyExportTypes( boolean with_geo )
  {
    int nr = 0;
    int kk = with_geo? 17 : 14;
    for ( int k = 0; k < kk; ++k ) if ( mSurveyExportEnable[k] ) ++ nr;
    String[] ret = new String[nr];
    int n = 0;
    for ( int k = 0; k < kk; ++k ) {
      if ( mSurveyExportEnable[k] ) {
        ret[n ++] = mSurveyExportTypes[k];
      }
    }
    assert( n == nr );
    return ret;
  }

  /** @return the array export index from the position of the enabled export list
   * @param pos   position
   */
  public static int surveyIndex( int pos )
  {
    int ppos = 0;
    for ( int k = 0; k < mSurveyExportTypes.length; ++k ) {
      TDLog.v("Pos " + pos + " k " + k + " " +  mSurveyExportEnable[k] );
      if ( mSurveyExportEnable[k] ) {
        if ( pos == 0 ) return ppos;
        pos --;
      } 
      ppos ++;
    }
    return -1;
  }

  public static final String[] mSurveyExportTypes = { "ZIP", 
    "Compass",   // 1
    "cSurvey",
    "GHTopo",
    // "Grottolf",
    // "PocketTopo", // 5
    "Polygon",    //  4
    "Survex",     //  5
    "Therion",    //  6
    "Topo",
    "TopoRobot",  //  8
    "VisualTopo",
    "Walls", 
    "WinKarst",   // 11
    "CSV",
    "DXF",
    "KML",        // 14
    // "OziExplorer",
    "GPX",        // 15
    // "GeoJSON",
    "Shapefile",  // 16
  };

  public static final String[] mSurveyExportTypesNoGeo = { "ZIP", 
    "Compass",
    "cSurvey",
    "GHTopo",
    // "Grottolf",
    // "PocketTopo",
    "Polygon",
    "Survex",
    "Therion",
    "Topo",
    "TopoRobot",
    "VisualTopo",
    "Walls", 
    "WinKarst",
    "CSV",
    "DXF"
    // "KML",
    // // "OziExplorer",
    // "GPX",
    // // "GeoJSON",
    // "Shapefile"
  };

  static final int[] mSurveyExportIndex = {
    SURVEY_FORMAT_ZIP,
    SURVEY_FORMAT_DAT,
    SURVEY_FORMAT_CSX,
    SURVEY_FORMAT_GTX,
    // SURVEY_FORMAT_GRT,
    // SURVEY_FORMAT_TOP,
    SURVEY_FORMAT_PLG,
    SURVEY_FORMAT_SVX,
    SURVEY_FORMAT_TH,
    SURVEY_FORMAT_CAV,
    SURVEY_FORMAT_TRB,
    SURVEY_FORMAT_TRO,
    SURVEY_FORMAT_SRV,
    SURVEY_FORMAT_SUR,
    SURVEY_FORMAT_CSV,
    SURVEY_FORMAT_DXF,
    SURVEY_FORMAT_KML,
    // SURVEY_FORMAT_PLT,
    SURVEY_FORMAT_GPX,
    // SURVEY_FORMAT_JSON,
    SURVEY_FORMAT_SHP
  };

  /** return the survey export name (null if illegal type)
   * @param type    export type (position in the export-type list)
   * @param name    survey name
   */
  public static String getSurveyFilename( int type, String survey )
  {
    TDLog.v("CONST export type " + type );
    switch ( type ) {
      case SURVEY_POS_ZIP:       return survey + ".zip";
      case SURVEY_POS_COMPASS:   return survey + ".dat";
      case SURVEY_POS_CSURVEY:   return survey + ".csx";
      case SURVEY_POS_GHTOPO:    return survey + ".gtx";
      // case SURVEY_POS_GROTTOLF: return survey + ".grt";
      // case SURVEY_POS_PTOPO:    return survey + ".top";
      case SURVEY_POS_POLYGON:   return survey + ".cave";
      case SURVEY_POS_SURVEX:    return survey + ".svx"; // 5
      case SURVEY_POS_THERION:   return survey + ".th";
      case SURVEY_POS_TOPO:      return survey + ".cav";
      case SURVEY_POS_TOPOROBOT: return survey + ".trb";
      case SURVEY_POS_VTOPO:     return survey + ".tro";
      case SURVEY_POS_WALLS:     return survey + ".srv"; // 10
      case SURVEY_POS_WINKARST:  return survey + ".sur";
      case SURVEY_POS_CSV:       return survey + ".csv";
      case SURVEY_POS_DXF:       return survey + ".dxf";
      case SURVEY_POS_KML:       return survey + ".kml";
      // case 17: return survey + ".plt";
      case SURVEY_POS_GPX:       return survey + ".gpx";
      // case SURVEY_POS_GEOJSON:   return survey + ".json";
      case SURVEY_POS_SHAPEFILE: return survey + ".shz"; // 15
      case SURVEY_POS_VTOPOX:    return survey + ".trox"; // -9
    }
    return null;
  }

  // ======= PLOT EXPORT  ======
  public final static int PLOT_POS_THERION   = 0;
  public final static int PLOT_POS_CSURVEY   = 1;
  public final static int PLOT_POS_DXF       = 2;
  public final static int PLOT_POS_SVG       = 3;
  public final static int PLOT_POS_SHAPEFILE = 4;
  public final static int PLOT_POS_PDF       = 5;
  public final static int PLOT_POS_XVI       = 6;
  // public final static int PLOT_POS_TUNNEL    = 7;

  public static final String[] mPlotExportTypes = {
      "Therion",
      "cSurvey",
      "DXF",
      "SVG",
      "Shapefile",
      // "PNG", // NO_PNG
      "PDF",
      "XVI",
      // "Tunnel",
      // "Cave3D",
      // "PNM", NO_PNM
  };

  static final int[] mPlotExportIndex = {
    SURVEY_FORMAT_TH2,
    SURVEY_FORMAT_CSX,
    SURVEY_FORMAT_DXF,
    SURVEY_FORMAT_SVG,
    SURVEY_FORMAT_SHP,
    // SURVEY_FORMAT_PNG, // NO_PNG
    SURVEY_FORMAT_PDF,
    SURVEY_FORMAT_XVI,
    // SURVEY_FORMAT_TNL,
    // SURVEY_FORMAT_C3D, // NO_C3D
    // SURVEY_FORMAT_PNM, // NO_PNM
  };

  private static final String[] mPlotExportExt = {
    "th2",
    "csx",
    "dxf",
    "svg",
    "shz",
    // "png", // NO_PNG
    "pdf",
    "xvi",
    // "xml", // NO_TUNNEL
    // "c3d", // NO_C3D
    // "pnm", // NO_PNM
  };

  /** return the plot export name (null if illegal type)
   * @param type    export type (position in the export-type list)
   * @param name    plot name
   */
  public static String getPlotFilename( int type, String name )
  {
    switch ( type ) { 
      case PLOT_POS_THERION: return name + ".th2";
      case PLOT_POS_CSURVEY: return name + ".csx";
      case PLOT_POS_DXF:     return name + ".dxf";
      case PLOT_POS_SVG:     return name + ".svg";
      case PLOT_POS_SHAPEFILE: return name + ".shz";
      // case PLOT_POS_PNG: return name + ".png"; // NO_PNG
      case PLOT_POS_PDF:    return name + ".pdf";
      case PLOT_POS_XVI:    return name + ".xvi";
      // case PLOT_POS_TUNNEL: return name + ".xml"; // Tunnel
      // case PLOT_POS_C3D: return name + ".c3d";
      // case PLOT_POS_PNM: return name + ".pnm"; NO_PNM
    }
    return null;
  }

  // ======= OVERVIEW EXPORT  ======
  public final static int OVERVIEW_POS_THERION   = 0;
  public final static int OVERVIEW_POS_DXF       = 1;
  public final static int OVERVIEW_POS_SVG       = 2;
  public final static int OVERVIEW_POS_SHAPEFILE = 3;
  public final static int OVERVIEW_POS_PDF       = 4;
  public final static int OVERVIEW_POS_XVI       = 5;

  public static final String[] mOverviewExportTypes = {
      "Therion",
      "DXF",
      "SVG",
      "Shapefile",
      "PDF",
      "XVI"
  };
  private static final int[] mOverviewExportIndex = {
    SURVEY_FORMAT_TH2,
    SURVEY_FORMAT_DXF,
    SURVEY_FORMAT_SVG,
    SURVEY_FORMAT_SHP,
    SURVEY_FORMAT_PDF,
    SURVEY_FORMAT_XVI
  };

  private static final String[] mOverviewExportExt = {
    "th2",
    "dxf",
    "svg",
    "shz",
    "pdf",
    "xvi"
  };

  /** return the overview export name (null if illegal type)
   * @param type    export type (position in the export-type list)
   * @param name    survey name (?)
   */
  public static String getOverviewFilename( int type, String name )
  { 
    switch( type ) {
      case OVERVIEW_POS_THERION:   return name + ".th2";
      case OVERVIEW_POS_DXF:       return name + ".dxf";
      case OVERVIEW_POS_SVG:       return name + ".svg";
      case OVERVIEW_POS_SHAPEFILE: return name + ".shz";
      case OVERVIEW_POS_PDF:       return name + ".pdf";
      case OVERVIEW_POS_XVI:       return name + ".xvi";
    }
    return null;
  }

  public static final String[] mCalibExportTypes = { "CSV" };
  private static final int[] mCalibExportIndex = { SURVEY_FORMAT_CSV };

  // public static final String[] mSketchExportTypes = { "Therion", "DXF" };
  // private static final int[] mSketchExportIndex = { SURVEY_FORMAT_TH3, SURVEY_FORMAT_DXF };

  /** @return the format index corresponding to a given type
   * @param type   format type
   * @param types  acceptable format types
   * @param index  corresponding format indices
   */
  private static int formatIndex( String type, String[] types, int[] index )
  {
    for ( int k=0; k<types.length; ++k ) {
      if ( types[k].equals( type ) ) return index[k];
    }
    TDLog.Error("Type not found: " + type );
    return SURVEY_FORMAT_NONE;
  }

  public static int surveyImportFormatIndex( String type ) { return formatIndex( type, mSurveyImportTypes,   mSurveyImportIndex ); }
  public static int surveyFormatIndex( String type )   { return formatIndex( type, mSurveyExportTypes,   mSurveyExportIndex ); }
  public static int plotExportIndex( String type )     { return formatIndex( type, mPlotExportTypes,     mPlotExportIndex ); }
  public static int overviewExportIndex( String type ) { return formatIndex( type, mOverviewExportTypes, mOverviewExportIndex ); }
  public static int calibExportIndex( String type )    { return formatIndex( type, mCalibExportTypes,    mCalibExportIndex ); }
  // public static int sketchExportIndex( String type ) { return formatIndex( type, mSketchExportTypes, mSketchExportIndex ); }

  private static String exportExt( String type, String[] types, String[] ext )
  {
    for ( int k=0; k<types.length; ++k ) {
      if ( types[k].equals( type ) ) return ext[k];
    }
    return "txt";
  }

  public static String plotExportExt( String type ) { return exportExt( type, mPlotExportTypes, mPlotExportExt ); }
  public static String overviewExportExt( String type ) { return exportExt( type, mOverviewExportTypes, mOverviewExportExt ); }

  public static final int NUMBER                 = InputType.TYPE_CLASS_NUMBER;
  public static final int NUMBER_DECIMAL         = NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
  public static final int NUMBER_SIGNED          = NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
  public static final int NUMBER_DECIMAL_SIGNED  = NUMBER_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;
  public static final int TEXT                   = InputType.TYPE_CLASS_TEXT;

}
