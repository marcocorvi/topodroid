/** @file TDConst.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid numerical constants
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
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

  static final int DISTOX_EXPORT_ZIP = 20;

  static final String[] mSurveyExportTypes = { "ZIP", 
                            "Compass", "cSurvey", "PocketTopo", "Survex", "Therion", "VisualTopo", "Walls", 
                            "CSV", "DXF", "KML", "OziExplorer", "Polygon" };
  static final int[] mSurveyExportIndex = { 20, 
                            DISTOX_EXPORT_DAT, DISTOX_EXPORT_CSX, DISTOX_EXPORT_TOP,
                            DISTOX_EXPORT_SVX, DISTOX_EXPORT_TH,
                            DISTOX_EXPORT_TRO, DISTOX_EXPORT_SRV,
                            DISTOX_EXPORT_CSV, DISTOX_EXPORT_DXF,
                            DISTOX_EXPORT_KML, DISTOX_EXPORT_PLT, DISTOX_EXPORT_PLG };

  static final String[] mPlotExportTypes = { "Therion", "cSurvey", "DXF", "SVG", "PNG" };
  static final int[] mPlotExportIndex = {
    DISTOX_EXPORT_TH2, DISTOX_EXPORT_CSX, DISTOX_EXPORT_DXF, DISTOX_EXPORT_SVG, DISTOX_EXPORT_PNG };

  static final String[] mCalibExportTypes = { "CSV" };
  static final int[] mCalibExportIndex = { DISTOX_EXPORT_CSV };

  static final String[] mSketchExportTypes = { "Therion", "DXF" };
  static final int[] mSketchExportIndex = { DISTOX_EXPORT_TH3, DISTOX_EXPORT_DXF };

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

  static final int COLOR_NORMAL    = 0xffffffff; // title colors
  static final int COLOR_NORMAL2   = 0xffcccccc; // title color nr. 2
  static final int COLOR_CONNECTED = 0xffff0000; 
  static final int COLOR_COMPUTE   = 0xffff33cc;

  static final int NUMBER                 = InputType.TYPE_CLASS_NUMBER;
  static final int NUMBER_DECIMAL         = NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
  static final int NUMBER_SIGNED          = NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
  static final int NUMBER_DECIMAL_SIGNED  = NUMBER_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;

}
