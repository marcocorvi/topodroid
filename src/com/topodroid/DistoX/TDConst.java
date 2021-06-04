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
  public static final int DISTOX_EXPORT_NONE = -1;
  public static final int DISTOX_EXPORT_TH  = 0;
  public static final int DISTOX_EXPORT_TLX = 1;
  public static final int DISTOX_EXPORT_DAT = 2; // Compass     DatFile
  public static final int DISTOX_EXPORT_SVX = 3; // Survex      SvxFile
  public static final int DISTOX_EXPORT_TRO = 4; // VisualTopo  TroFile
  public static final int DISTOX_EXPORT_CSV = 5;
  public static final int DISTOX_EXPORT_DXF = 6;
  public static final int DISTOX_EXPORT_CSX = 7; // cSurvey     CsxFile
  public static final int DISTOX_EXPORT_TOP = 8; // PocketTopo  TopFile
  public static final int DISTOX_EXPORT_SRV = 9; // Walls       SrvFile
  public static final int DISTOX_EXPORT_KML = 10; //Keyhole     KmlFile
  public static final int DISTOX_EXPORT_PLT = 11; // track file PltFile

  public static final int DISTOX_EXPORT_PNG = 12;
  public static final int DISTOX_EXPORT_SVG = 13;
  public static final int DISTOX_EXPORT_TH2 = 14;
  public static final int DISTOX_EXPORT_TH3 = 15;

  public static final int DISTOX_EXPORT_PLG = 16; // Polygon   CaveFile
  public static final int DISTOX_EXPORT_CAV = 17; // Topo      CavFile
  public static final int DISTOX_EXPORT_GRT = 18; // Grottolf
  public static final int DISTOX_EXPORT_GTX = 19; // GHTopo    GtxFile
  public static final int DISTOX_EXPORT_SUR = 20; // WinKarst  SurFile
  public static final int DISTOX_EXPORT_TRB = 21; // TopoRobot TrbFile
  public static final int DISTOX_EXPORT_JSON = 22; // GeoJSON  JsonFile
  public static final int DISTOX_EXPORT_SHP = 23; // Shapefile ShpFile
  public static final int DISTOX_EXPORT_XVI = 24; // xtherion  XviFile
  public static final int DISTOX_EXPORT_TNL = 25; // Tunnel    TnlFile
  public static final int DISTOX_EXPORT_C3D = 26; // Cave3D    C3dFile
  public static final int DISTOX_EXPORT_PDF = 27; // PDF

  public static final int DISTOX_EXPORT_ZIP = 30;

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
    DISTOX_EXPORT_ZIP,
    DISTOX_EXPORT_DAT,
    DISTOX_EXPORT_CSX,
    DISTOX_EXPORT_GTX,
    DISTOX_EXPORT_GRT,
    DISTOX_EXPORT_TOP,
    DISTOX_EXPORT_PLG,
    DISTOX_EXPORT_SVX,
    DISTOX_EXPORT_TH,
    DISTOX_EXPORT_CAV,
    DISTOX_EXPORT_TRB,
    DISTOX_EXPORT_TRO,
    DISTOX_EXPORT_SRV,
    DISTOX_EXPORT_SUR,
    DISTOX_EXPORT_CSV,
    DISTOX_EXPORT_DXF,
    DISTOX_EXPORT_KML,
    DISTOX_EXPORT_PLT,
    DISTOX_EXPORT_JSON,
    DISTOX_EXPORT_SHP
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
    DISTOX_EXPORT_TH2,
    DISTOX_EXPORT_CSX,
    DISTOX_EXPORT_DXF,
    DISTOX_EXPORT_SVG,
    DISTOX_EXPORT_SHP,
    DISTOX_EXPORT_PNG,
    DISTOX_EXPORT_PDF,
    DISTOX_EXPORT_XVI,
    DISTOX_EXPORT_TNL,
    DISTOX_EXPORT_C3D
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
    DISTOX_EXPORT_TH2,
    DISTOX_EXPORT_DXF,
    DISTOX_EXPORT_SVG,
    DISTOX_EXPORT_SHP,
    DISTOX_EXPORT_PDF,
    DISTOX_EXPORT_XVI
  };

  public static final String[] mCalibExportTypes = { "CSV" };
  private static final int[] mCalibExportIndex = { DISTOX_EXPORT_CSV };

  public static final String[] mSketchExportTypes = { "Therion", "DXF" };
  private static final int[] mSketchExportIndex = { DISTOX_EXPORT_TH3, DISTOX_EXPORT_DXF };

  private static int exportIndex( String type, String[] types, int[] index )
  {
    for ( int k=0; k<types.length; ++k ) {
      if ( types[k].equals( type ) ) return index[k];
    }
    return DISTOX_EXPORT_NONE;
  }

  public static int surveyExportIndex( String type ) { return exportIndex( type, mSurveyExportTypes, mSurveyExportIndex ); }
  public static int plotExportIndex( String type ) { return exportIndex( type, mPlotExportTypes, mPlotExportIndex ); }
  public static int overviewExportIndex( String type ) { return exportIndex( type, mOverviewExportTypes, mOverviewExportIndex ); }
  public static int calibExportIndex( String type ) { return exportIndex( type, mCalibExportTypes, mCalibExportIndex ); }
  public static int sketchExportIndex( String type ) { return exportIndex( type, mSketchExportTypes, mSketchExportIndex ); }

  public static final int NUMBER                 = InputType.TYPE_CLASS_NUMBER;
  public static final int NUMBER_DECIMAL         = NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
  public static final int NUMBER_SIGNED          = NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
  public static final int NUMBER_DECIMAL_SIGNED  = NUMBER_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;
  public static final int TEXT                   = InputType.TYPE_CLASS_TEXT;

}
