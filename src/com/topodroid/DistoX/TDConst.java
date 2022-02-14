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
package com.topodroid.DistoX;

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
  public static final int SURVEY_FORMAT_PLT = 11; // track file PltFile

  public static final int SURVEY_FORMAT_PNG = 12;
  public static final int SURVEY_FORMAT_SVG = 13;
  public static final int SURVEY_FORMAT_TH2 = 14;
  public static final int SURVEY_FORMAT_TH3 = 15;

  public static final int SURVEY_FORMAT_PLG = 16; // Polygon   CaveFile
  public static final int SURVEY_FORMAT_CAV = 17; // Topo      CavFile
  public static final int SURVEY_FORMAT_GRT = 18; // Grottolf
  public static final int SURVEY_FORMAT_GTX = 19; // GHTopo    GtxFile
  public static final int SURVEY_FORMAT_SUR = 20; // WinKarst  SurFile
  public static final int SURVEY_FORMAT_TRB = 21; // TopoRobot TrbFile
  public static final int SURVEY_FORMAT_JSON = 22; // GeoJSON  JsonFile
  public static final int SURVEY_FORMAT_SHP = 23; // Shapefile ShpFile
  public static final int SURVEY_FORMAT_XVI = 24; // xtherion  XviFile
  public static final int SURVEY_FORMAT_TNL = 25; // Tunnel    TnlFile
  public static final int SURVEY_FORMAT_C3D = 26; // Cave3D    C3dFile
  public static final int SURVEY_FORMAT_PDF = 27; // PDF
  public static final int SURVEY_FORMAT_SNP = 28; // CaveSniper
  // public static final int SURVEY_FORMAT_TROX = 29; // VisualTopo X
  

  public static final int SURVEY_FORMAT_ZIP = 30;

  public static final int SURVEY_FORMAT_GLTF    = 31;
  public static final int SURVEY_FORMAT_CGAL    = 32;
  public static final int SURVEY_FORMAT_STL     = 33;
  public static final int SURVEY_FORMAT_STL_BIN = 34;
  public static final int SURVEY_FORMAT_LAS_BIN = 35;
  public static final int SURVEY_FORMAT_SERIAL  = 36;

  public static final String[] mMimeType = {
    "application/octet-stream",  // 0 // Therion
    "application/tlx", // unused
    "application/octet-stream", // Compass
    "application/octet-stream", // Survex
    "application/octet-stream", // VisualTopo
    "text/csv", // 5            // *CSV
    "application/dxf",          // *DXF
    "application/octet-stream", // *cSurvey
    "application/octet-stream", // PocketTopo
    "application/octet-stream", // Walls
    "application/vnd",          // *KML
    "application/octet-stream", // trackfile (.plt)

    "image/png",                // 12 *PNG
    "image/svg+kml",            // *SVG
    "application/octet-stream", // Therion-2
    "application/octet-stream", // Therion-3

    "application/octet-stream", // 16 Polygon (.cave)
    "application/octet-stream", // Topo (.cav)
    "application/octet-stream", // Grottolf
    "application/octet-stream", // GhTopo
    "application/octet-stream", // 20 WinKarst
    "application/octet-stream", // 21 TopoRobot
    "application/json",         // 22 *Json
    "application/shp",
    "application/xvi",
    "application/xml",          // 25 *Tunnel
    "application/octet-stream", // 26
    "application/pdf",          // *PDF
    "application/octet-stream", // CaveSniper
    "application/octet_stream", // *trox
    "application/zip",          // 30
    "application/octet-stream", // 31 glTF
    "application/octet-stream", // 32 CGAL
    "application/octet-stream", // 33 STL
    "application/octet-stream", // STL binary
    "application/octet-stream", // LAS binary
    "test/plain"                // 36 serialized
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
  public static final String[] mModelExportTypes = {
    "gLTF",
    "CGAL",
    "STL",
    "STL-bin",
    "LAS-bin",
    "DXF",
    "KML",
    "Shapefile"
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
  };

  private static final int[] mModelExportIndex = {
    SURVEY_FORMAT_GLTF,
    SURVEY_FORMAT_CGAL,
    SURVEY_FORMAT_STL,
    SURVEY_FORMAT_STL_BIN,
    SURVEY_FORMAT_LAS_BIN,
    SURVEY_FORMAT_DXF,
    SURVEY_FORMAT_KML,
    SURVEY_FORMAT_SHP
  };

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
      // case ModelType.SERIAL: return name + ".txt";
    }
    return name;
  }

  // ======= DATA EXPORT  ======
  public static final String[] mSurveyExportTypes = { "ZIP", 
    "Compass",   // 1
    "cSurvey",
    "GHTopo",
    "Grottolf",
    "PocketTopo", // 5
    "Polygon",
    "Survex",
    "Therion",
    "Topo",
    "TopoRobot",  // 10
    "VisualTopo",
    "Walls", 
    "WinKarst",   // 13
    "CSV",
    "DXF",
    "KML",        // 16
    "OziExplorer",
    "GeoJSON",
    "Shapefile"
  };

  public static final String[] mSurveyExportTypesNoGeo = { "ZIP", 
    "Compass",
    "cSurvey",
    "GHTopo",
    "Grottolf",
    "PocketTopo",
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
    // "OziExplorer",
    // "GeoJSON",
    // "Shapefile"
  };

  static final int[] mSurveyExportIndex = {
    SURVEY_FORMAT_ZIP,
    SURVEY_FORMAT_DAT,
    SURVEY_FORMAT_CSX,
    SURVEY_FORMAT_GTX,
    SURVEY_FORMAT_GRT,
    SURVEY_FORMAT_TOP,
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
    SURVEY_FORMAT_PLT,
    SURVEY_FORMAT_JSON,
    SURVEY_FORMAT_SHP
  };

  public static String getSurveyFilename( int type, String survey )
  {
    switch ( type ) {
      case  0: return survey + ".zip";
      case  1: return survey + ".dat";
      case  2: return survey + ".csx";
      case  3: return survey + ".gtx";
      case  4: return survey + ".grt";
      case  5: return survey + ".top";
      case  6: return survey + ".cave";
      case  7: return survey + ".svx";
      case  8: return survey + ".th";
      case  9: return survey + ".cav";
      case 10: return survey + ".trb";
      case 11: return survey + ".tro";
      case 12: return survey + ".srv";
      case 13: return survey + ".sur";
      case 14: return survey + ".csv";
      case 15: return survey + ".dxf";
      case 16: return survey + ".kml";
      case 17: return survey + ".plt";
      case 18: return survey + ".json";
      case 19: return survey + ".shz";
      case -11: return survey + ".trox";
    }
    return survey;
  }

  // ======= PLOT EXPORT  ======
  public static final String[] mPlotExportTypes = {
      "Therion",
      "cSurvey",
      "DXF",
      "SVG",
      "Shapefile",
      "PNG",
      "PDF",
      "XVI",
      "Tunnel",
      "Cave3D"
  };

  static final int[] mPlotExportIndex = {
    SURVEY_FORMAT_TH2,
    SURVEY_FORMAT_CSX,
    SURVEY_FORMAT_DXF,
    SURVEY_FORMAT_SVG,
    SURVEY_FORMAT_SHP,
    SURVEY_FORMAT_PNG,
    SURVEY_FORMAT_PDF,
    SURVEY_FORMAT_XVI,
    SURVEY_FORMAT_TNL,
    SURVEY_FORMAT_C3D
  };

  private static final String[] mPlotExportExt = {
    "th2",
    "csx",
    "dxf",
    "svg",
    "shz",
    "png",
    "pdf",
    "xvi",
    "xml",
    "c3d"
  };

  public static String getPlotFilename( int type, String name )
  {
    switch ( type ) { 
      case 0: return name + ".th2";
      case 1: return name + ".csx";
      case 2: return name + ".dxf";
      case 3: return name + ".svg";
      case 4: return name + ".shz";
      case 5: return name + ".png";
      case 6: return name + ".pdf";
      case 7: return name + ".xvi";
      case 8: return name + ".xml"; // Tunnel
      case 9: return name + ".c3d";
    }
    return name;
  }

  // ======= OVERVIEW EXPORT  ======
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
    "shp",
    "pdf",
    "xvi"
  };

  public static String getOverviewFilename( int type, String name )
  { 
    switch( type ) {
      case 0: return name + ".th2";
      case 1: return name + ".dxf";
      case 2: return name + ".svg";
      case 3: return name + ".shz";
      case 4: return name + ".pdf";
      case 5: return name + ".xvi";
    }
    return name;
  }

  public static final String[] mCalibExportTypes = { "CSV" };
  private static final int[] mCalibExportIndex = { SURVEY_FORMAT_CSV };

  // public static final String[] mSketchExportTypes = { "Therion", "DXF" };
  // private static final int[] mSketchExportIndex = { SURVEY_FORMAT_TH3, SURVEY_FORMAT_DXF };

  private static int exportIndex( String type, String[] types, int[] index )
  {
    for ( int k=0; k<types.length; ++k ) {
      if ( types[k].equals( type ) ) return index[k];
    }
    return SURVEY_FORMAT_NONE;
  }

  public static int surveyFormatIndex( String type )   { return exportIndex( type, mSurveyExportTypes,   mSurveyExportIndex ); }
  public static int plotExportIndex( String type )     { return exportIndex( type, mPlotExportTypes,     mPlotExportIndex ); }
  public static int overviewExportIndex( String type ) { return exportIndex( type, mOverviewExportTypes, mOverviewExportIndex ); }
  public static int calibExportIndex( String type )    { return exportIndex( type, mCalibExportTypes,    mCalibExportIndex ); }
  // public static int sketchExportIndex( String type ) { return exportIndex( type, mSketchExportTypes, mSketchExportIndex ); }

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
