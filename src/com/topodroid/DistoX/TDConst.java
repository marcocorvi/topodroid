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

  public static final String[] mMimeType = {
    "application/octet-stream",  // 0 // Therion
    "application/tlx", // unused
    "application/octet-stream",       // Compass
    "application/octet-stream",       // Survex
    "application/octet-stream",       // VisualTopo
    "text/csv", // 5            // *CSV
    "application/dxf",          // *DXF
    "application/xml",          // *cSurvey
    "application/octet-stream",       // PocketTopo
    "application/srv",
    "application/vnd", // 10    // *KML
    "application/plt",

    "image/png",        // 12   // *PNG
    "image/svg+kml",            // *SVG
    "application/th2",
    "application/th3", // 15

    "application/plg", // 16
    "application/cav",
    "application/grt",
    "application/gtx",
    "application/sur", // 20
    "application/trb", // 21
    "application/json",       // *Json
    "application/shp",
    "application/xvi",
    "application/xml",        // *Tunnel
    "application/c3d", // 26
    "application/pdf",        // *PDF
    "application/octet-stream",       // CaveSniper
    "application/octet_stream",       // *trox
    "application/zip"  // 30
  };

  public static final String[] mSurveyImportTypes = { 
    "ZIP", 
    "Compass",
    "CaveSniper",
    "Survex",
    "Therion",
    "VisualTopo",
    // "VisualTopo-X",
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
    SURVEY_FORMAT_TOP
  };
   
  public static final String[] mSurveyExportTypes = { "ZIP", 
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
    "DXF",
    "KML",
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

  private static final int[] mSurveyExportIndex = {
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

  public static final String[] mPlotExportTypes = {
      "Therion",
      "cSurvey",
      "DXF",
      "SVG",
      "SHP",
      "PNG",
      "PDF",
      "XVI",
      "Tunnel",
      "Cave3D"
  };

  private static final int[] mPlotExportIndex = {
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
    "shp",
    "png",
    "pdf",
    "xvi",
    "xml",
    "c3d"
  };

  public static final String[] mOverviewExportTypes = {
      "Therion",
      "DXF",
      "SVG",
      "SHP",
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
