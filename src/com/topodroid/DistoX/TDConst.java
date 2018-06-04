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

class TDConst
{
  static final int DISTOX_EXPORT_NONE = -1;
  static final int DISTOX_EXPORT_TH  = 0;
  static final int DISTOX_EXPORT_TLX = 1;
  static final int DISTOX_EXPORT_DAT = 2;
  static final int DISTOX_EXPORT_SVX = 3;
  static final int DISTOX_EXPORT_TRO = 4;
  static final int DISTOX_EXPORT_CSV = 5;
  static final int DISTOX_EXPORT_DXF = 6;
  static final int DISTOX_EXPORT_CSX = 7;
  static final int DISTOX_EXPORT_TOP = 8;
  static final int DISTOX_EXPORT_SRV = 9;
  static final int DISTOX_EXPORT_KML = 10;
  static final int DISTOX_EXPORT_PLT = 11;

  static final int DISTOX_EXPORT_PNG = 12;
  static final int DISTOX_EXPORT_SVG = 13;
  static final int DISTOX_EXPORT_TH2 = 14;
  static final int DISTOX_EXPORT_TH3 = 15;

  static final int DISTOX_EXPORT_PLG = 16; // Polygon
  static final int DISTOX_EXPORT_CAV = 17; // Topo
  static final int DISTOX_EXPORT_GRT = 18; // Grottolf
  static final int DISTOX_EXPORT_GTX = 19; // GHTopo
  static final int DISTOX_EXPORT_SUR = 20; // WinKarst
  static final int DISTOX_EXPORT_TRB = 21; // TopoRobot

  static final int DISTOX_EXPORT_ZIP = 30;

  static final String[] mSurveyExportTypes = { "ZIP", 
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
    "OziExplorer"
  };
  static final private int[] mSurveyExportIndex = {
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
    DISTOX_EXPORT_PLT
  };

  static final String[] mPlotExportTypes = {
      "Therion",
      "cSurvey",
      "DXF",
      "SVG",
      "PNG"
  };
  static final private int[] mPlotExportIndex = {
    DISTOX_EXPORT_TH2,
    DISTOX_EXPORT_CSX,
    DISTOX_EXPORT_DXF,
    DISTOX_EXPORT_SVG,
    DISTOX_EXPORT_PNG
  };

  static final String[] mCalibExportTypes = { "CSV" };
  static final private int[] mCalibExportIndex = { DISTOX_EXPORT_CSV };

  static final String[] mSketchExportTypes = { "Therion", "DXF" };
  static final private int[] mSketchExportIndex = { DISTOX_EXPORT_TH3, DISTOX_EXPORT_DXF };

  private static int exportIndex( String type, String[] types, int[] index )
  {
    for ( int k=0; k<types.length; ++k ) {
      if ( types[k].equals( type ) ) return index[k];
    }
    return DISTOX_EXPORT_NONE;
  }

  static int surveyExportIndex( String type ) { return exportIndex( type, mSurveyExportTypes, mSurveyExportIndex ); }
  static int plotExportIndex( String type ) { return exportIndex( type, mPlotExportTypes, mPlotExportIndex ); }
  static int calibExportIndex( String type ) { return exportIndex( type, mCalibExportTypes, mCalibExportIndex ); }
  static int sketchExportIndex( String type ) { return exportIndex( type, mSketchExportTypes, mSketchExportIndex ); }

  static final int DDMMSS = 0;
  static final int DEGREE = 1;

  static final float TO_THERION = 5.0f;  // therion export scale-factor

  static final int NUMBER                 = InputType.TYPE_CLASS_NUMBER;
  static final int NUMBER_DECIMAL         = NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
  static final int NUMBER_SIGNED          = NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
  static final int NUMBER_DECIMAL_SIGNED  = NUMBER_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;

}
