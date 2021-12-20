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

import com.topodroid.DistoX.R;

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
  public static final int PREF_CATEGORY_SVX       =  8;
  public static final int PREF_CATEGORY_TH        =  9;
  public static final int PREF_CATEGORY_DAT       = 10;
  public static final int PREF_CATEGORY_CSX       = 11;
  public static final int PREF_CATEGORY_TRO       = 12;
  public static final int PREF_CATEGORY_SVG       = 13;
  public static final int PREF_CATEGORY_SHP       = 14;
  public static final int PREF_CATEGORY_DXF       = 15;
  public static final int PREF_CATEGORY_PNG       = 16;
  public static final int PREF_CATEGORY_KML       = 17;
  public static final int PREF_CATEGORY_CSV       = 18;
  public static final int PREF_SHOT_DATA          = 19; 
  public static final int PREF_SHOT_UNITS         = 20; 
  public static final int PREF_ACCURACY           = 21; 
  public static final int PREF_LOCATION           = 22; 
  public static final int PREF_PLOT_SCREEN        = 23; 
  public static final int PREF_TOOL_LINE          = 24; 
  public static final int PREF_TOOL_POINT         = 25; 
  // public static final int PREF_PLOT_WALLS         = 26;  // AUTOWALLS
  public static final int PREF_PLOT_DRAW          = 26; 
  public static final int PREF_PLOT_ERASE         = 27; 
  public static final int PREF_PLOT_EDIT          = 28;
  public static final int PREF_CATEGORY_CAVE3D    = 29;
  public static final int PREF_DEM3D              = 30;
  public static final int PREF_WALLS3D            = 31;
  public static final int PREF_CATEGORY_GEEK      = 32; 
  public static final int PREF_GEEK_SHOT          = 33; 
  public static final int PREF_GEEK_SPLAY         = 34; 
  public static final int PREF_GEEK_PLOT          = 35; 
  public static final int PREF_GEEK_LINE          = 36; 
  public static final int PREF_GEEK_DEVICE        = 37; 
  public static final int PREF_GEEK_IMPORT        = 38; 
  public static final int PREF_CATEGORY_LOG       = 39; // this must be the last

  static int[] mTitleRes = {
    R.string.title_settings_main,     // 0
    R.string.title_settings_survey,
    R.string.title_settings_plot,
    R.string.title_settings_calib,
    R.string.title_settings_device,
    R.string.title_settings_sketch,   // 5
    R.string.title_settings_export,
    R.string.title_settings_import,
    R.string.title_settings_svx,
    R.string.title_settings_th,
    R.string.title_settings_dat,
    R.string.title_settings_csx,
    R.string.title_settings_tro,
    R.string.title_settings_svg,      // 13
    R.string.title_settings_shp,
    R.string.title_settings_dxf,
    R.string.title_settings_png,
    R.string.title_settings_kml,      // 17
    R.string.title_settings_csv,
    R.string.title_settings_shot,     
    R.string.title_settings_units,    // 20
    R.string.title_settings_accuracy,
    R.string.title_settings_location,
    R.string.title_settings_screen,   // 23
    R.string.title_settings_line,
    R.string.title_settings_point,    // 25
    // R.string.title_settings_walls, // AUTOWALLS
    R.string.title_settings_draw,
    R.string.title_settings_erase,    // 27
    R.string.title_settings_edit,
    R.string.title_settings_3d,
    R.string.title_settings_dem,
    R.string.title_settings_walls3d,
    R.string.title_settings_geek,
    R.string.title_settings_geek_survey,   // 33
    R.string.title_settings_geek_splay,    // 34
    R.string.title_settings_geek_plot,
    R.string.title_settings_geek_line,     // 36
    R.string.title_settings_geek_device,   // 37
    R.string.title_settings_geek_import,   // 38
    R.string.title_settings_log       // 39
  };
}
