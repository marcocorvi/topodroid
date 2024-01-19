/* @file TDPrefCat.java
 *
 * @author marco corvi
 * @date aug 2018
 *
 * @brief TopoDroid preferences categories
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.prefs;

import com.topodroid.TDX.R;

public class TDPrefCat
{
  public static final String PREF_CATEGORY = "PrefCategory";
  public static final int PREF_CATEGORY_ALL       =  0;
  public static final int PREF_CATEGORY_SURVEY    =  1;
  public static final int PREF_CATEGORY_PLOT      =  2;
  public static final int PREF_CATEGORY_CALIB     =  3;
  public static final int PREF_CATEGORY_DEVICE    =  4;
  public static final int PREF_CATEGORY_SKETCH    =  5;
  public static final int PREF_CATEGORY_EXPORT    =  6;
  public static final int PREF_CATEGORY_IMPORT    =  7;
  public static final int PREF_CATEGORY_CAVE3D    =  8;
  public static final int PREF_CATEGORY_EXPORT_ENABLE = 9;
  public static final int PREF_CATEGORY_SVX       = 10;
  public static final int PREF_CATEGORY_TH        = 11;
  public static final int PREF_CATEGORY_DAT       = 12;
  public static final int PREF_CATEGORY_CSX       = 13;
  public static final int PREF_CATEGORY_TRO       = 14;
  public static final int PREF_CATEGORY_SVG       = 15;
  public static final int PREF_CATEGORY_SHP       = 16;
  public static final int PREF_CATEGORY_DXF       = 17;
  // public static final int PREF_CATEGORY_PNG       = 17; // NO_PNG
  public static final int PREF_CATEGORY_GPX       = 18;
  public static final int PREF_CATEGORY_KML       = 19;
  public static final int PREF_CATEGORY_CSV       = 20;
  public static final int PREF_CATEGORY_SRV       = 21;
  public static final int PREF_SHOT_DATA          = 22; 
  public static final int PREF_SHOT_UNITS         = 23; 
  public static final int PREF_ACCURACY           = 24; 
  public static final int PREF_LOCATION           = 25; 
  public static final int PREF_PLOT_SCREEN        = 26; 
  public static final int PREF_TOOL_LINE          = 27; 
  public static final int PREF_TOOL_POINT         = 28; 
  // public static final int PREF_PLOT_WALLS         = 28;  // AUTOWALLS UNUSED
  public static final int PREF_PLOT_DRAW          = 29; 
  public static final int PREF_PLOT_ERASE         = 30; 
  public static final int PREF_PLOT_EDIT          = 31;
  public static final int PREF_DEM3D              = 32;
  public static final int PREF_WALLS3D            = 33;
  public static final int PREF_CATEGORY_GEEK      = 34; 
  public static final int PREF_GEEK_SHOT          = 35; 
  public static final int PREF_GEEK_SPLAY         = 36; 
  public static final int PREF_GEEK_PLOT          = 37; 
  public static final int PREF_GEEK_LINE          = 38; 
  public static final int PREF_GEEK_DEVICE        = 39; 
  public static final int PREF_GEEK_IMPORT        = 40; 
  public static final int PREF_GEEK_SKETCH        = 41; 
  // public static final int PREF_CATEGORY_LOG       = 41; // this must be the last NO_LOGS
  public static final int PREF_CATEGORY_MAX = 41; // last category

  static int[] mTitleRes = {
    R.string.title_settings_main,     // 0
    R.string.title_settings_survey,
    R.string.title_settings_plot,
    R.string.title_settings_calib,
    R.string.title_settings_device,
    R.string.title_settings_sketch,   // 5
    R.string.title_settings_export,
    R.string.title_settings_import,
    R.string.title_settings_3d,
    R.string.title_settings_export_enable,
    R.string.title_settings_svx,
    R.string.title_settings_th,
    R.string.title_settings_dat,
    R.string.title_settings_csx,
    R.string.title_settings_tro,
    R.string.title_settings_svg,      // 14
    R.string.title_settings_shp,
    R.string.title_settings_dxf,
    // R.string.title_settings_png, // 17
    R.string.title_settings_gpx,      // 18
    R.string.title_settings_kml,      // 18
    R.string.title_settings_csv,
    R.string.title_settings_srv,
    R.string.title_settings_shot,     
    R.string.title_settings_units,    // 21
    R.string.title_settings_accuracy,
    R.string.title_settings_location,
    R.string.title_settings_screen,   // 24
    R.string.title_settings_line,
    R.string.title_settings_point,    // 26
    -1, // R.string.title_settings_walls, // 27 AUTOWALLS
    R.string.title_settings_draw,
    R.string.title_settings_erase,    // 29
    R.string.title_settings_edit,
    R.string.title_settings_dem,
    R.string.title_settings_walls3d,
    R.string.title_settings_geek,
    R.string.title_settings_geek_survey,   // 34
    R.string.title_settings_geek_splay,    // 35
    R.string.title_settings_geek_plot,
    R.string.title_settings_geek_line,     // 37
    R.string.title_settings_geek_device,   // 38
    R.string.title_settings_geek_import,   // 39
    R.string.title_settings_log       // 40
  };
}
