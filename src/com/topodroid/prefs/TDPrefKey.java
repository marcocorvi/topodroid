/* @file TDPrefKey.java
 *
 * @author marco corvi
 * @date aug 2018
 *
 * @brief TopoDroid arrays of preference keys and accessory data 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.prefs;

import com.topodroid.utils.TDString;
import com.topodroid.utils.TDLog;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TDLevel;
import com.topodroid.TDX.R;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class TDPrefKey
{
  private static final int POLICY_INDEX = 2; // index of station-policy in mSurvey settings

  static final String FALSE = "false";
  static final String TRUE  = "true";

  static final int B = 0; // activity levels
  static final int N = 1;
  static final int A = 2;
  static final int E = 3;
  static final int T = 4;
  static final int D = 5;

  // setting groups
  static final int NON = 0; // used for forward
  static final int GEN = 1; // general
  static final int UI  = 2; // user interface
  static final int DT  = 3; // data
  static final int DR  = 4; // draw
  static final int D3  = 5; // 3D
  static final int XP  = 6; // export
  static final int BT  = 7; // bluetooth
  static final int CAL = 8; // calib
  static final int XT  = 9; // special functions

  // static final int[] mGroup = {
  //   -1,
  //   R.string.pref_gen,
  //   R.string.pref_ui,
  //   R.string.pref_data,
  //   R.string.pref_plot,
  //   R.string.pref_3d,
  //   R.string.pref_io,
  //   R.string.pref_bt,
  //   R.string.pref_cal,
  //   R.string.pref_xt
  // };

// TODO new formulation of leys

  static final int LONG = 1; // long
  static final int BOOL = 2;
  static final int FLT  = 3;
  static final int STR  = 4;
  static final int ARR  = 5; // array
  static final int BTN  = 6; // button
  static final int XTR  = 7; // special
  static final int FWRD = 8; // forward
  static final int COL  = 9; // color  

  int cat;  // category
  int level; 
  int type; // FIXME type ARR requires two int for array and array-values
  int group;
  String key;
  int title;
  int summary;
  String dflt;
  int label;
  int value;

  // FORWARD KEY
  TDPrefKey( int lvl, int grp, String key, int title )
  {
    this.cat     = -1;
    this.level   = lvl;
    this.type    = FWRD;
    this.group   = grp;
    this.key     = key;
    this.title   = title;
    this.summary = -1;
    this.dflt    = "";
    this.label   = -1;
    this.value   = -1;
  }

  // PLAIN KEY
  TDPrefKey( int lvl, int typ, int grp, String key, int title, int summary, String deflt )
  {
    this.cat     = -1;
    this.level   = lvl;
    this.type    = typ;
    this.group   = grp;
    this.key     = key;
    this.title   = title;
    this.summary = summary;
    this.dflt    = deflt;
    this.label   = -1;
    this.value   = -1;
  }

  // ARRAY KEY
  TDPrefKey( int lvl, int grp, String key, int title, int summary, String deflt, int lbl, int val )
  {
    this.cat     = -1;
    this.level   = lvl;
    this.type    = ARR;
    this.group   = grp;
    this.key     = key;
    this.title   = title;
    this.summary = summary;
    this.dflt    = deflt;
    this.label   = lbl;
    this.value   = val;
  }
  
  /** main settings:
   * size of text [pt]
   * size of buttons (S, N, M, L, XL)
   * symbol size
   * activity Level
   * enable local man pages
   * language
   * orientation:  0 unspecified, 1 portrait, 2 landscape
   */
  static TDPrefKey[] mMain = {
    new TDPrefKey( B, LONG, UI,  "DISTOX_TEXT_SIZE",       R.string.pref_text_size_title,    R.string.pref_text_size_summary,     TDString.TWENTY ),
    new TDPrefKey( B,       UI,  "DISTOX_SIZE_BUTTONS",    R.string.pref_size_buttons_title, R.string.pref_size_buttons_summary,  TDString.ONE, R.array.sizeButtons, R.array.sizeButtonsValue ),
    new TDPrefKey( B, FLT,  UI,  "DISTOX_SYMBOL_SIZE",     R.string.pref_symbol_size_title,  R.string.pref_symbol_size_summary,   "1.8"           ),  
    new TDPrefKey( B,       GEN, "DISTOX_EXTRA_BUTTONS",   R.string.pref_extra_buttons_title,R.string.pref_extra_buttons_summary, TDString.ONE, R.array.extraButtons, R.array.extraButtonsValue ),
    new TDPrefKey( A,       GEN, "DISTOX_LOCAL_MAN",       R.string.pref_local_help_title,   R.string.pref_local_help_summary,    TDString.ZERO, R.array.localUserMan, R.array.localUserManValue ),
    new TDPrefKey( N,       GEN, "DISTOX_LOCALE",          R.string.pref_locale_title,       R.string.pref_locale_summary,        TDString.EMPTY, R.array.locale, R.array.localeValue ), 
    new TDPrefKey( T,       UI,  "DISTOX_ORIENTATION",     R.string.pref_orientation_title,  R.string.pref_orientation_summary,   TDString.ZERO,  R.array.orientation, R.array.orientationValue ),
    new TDPrefKey( B,       NON, "DISTOX_EXPORT_PREF",     R.string.pref_cat_import_export ),
    new TDPrefKey( B,       NON, "DISTOX_SURVEY_PREF",     R.string.pref_cat_survey      ),
    new TDPrefKey( B,       NON, "DISTOX_PLOT_PREF",       R.string.pref_cat_drawing     ),
    new TDPrefKey( B,       NON, "DISTOX_DEVICE_PREF",     R.string.pref_cat_device      ),
    new TDPrefKey( N,       NON, "DISTOX_CAVE3D_PREF",     R.string.pref_cat_cave3d      ),
    new TDPrefKey( A,       NON, "DISTOX_GEEK_PREF",       R.string.pref_cat_geek        ),
    new TDPrefKey( E,       NON, "DISTOX_EXPORT_SETTINGS", R.string.pref_export_settings )
  };                                                            

  /** survey settings
   * default team
   * team dialog
   * station policy (DISTOX_SURVEY_STATIONS must not be used)
   * station names: 0 alphanumeric, 1 numbers
   * default initial station name
   * size of photo thumbnails
   * fixed origin for plots
   * whether at-station X-sections are shared among plots
   */
  static TDPrefKey[] mSurvey = {
    new TDPrefKey( B, STR,  DT,  "DISTOX_TEAM",             R.string.pref_team_title,             R.string.pref_team_summary,            TDString.EMPTY ),
    new TDPrefKey( E,       UI,  "DISTOX_TEAM_DIALOG",      R.string.pref_team_names_title,       R.string.pref_team_names_summary,      TDString.ZERO, R.array.teamNames, R.array.teamNamesValue ),
    new TDPrefKey( B,       DT,  "DISTOX_SURVEY_STATION",   R.string.pref_survey_stations_title,  R.string.pref_survey_stations_summary, TDString.ONE, R.array.surveyStations, R.array.surveyStationsValue  ),
    new TDPrefKey( B,       DT,  "DISTOX_STATION_NAMES",    R.string.pref_station_names_title,    R.string.pref_station_names_summary,   "alpha", R.array.stationNames, R.array.stationNamesValue ),
    new TDPrefKey( B, STR,  DT,  "DISTOX_INIT_STATION",     R.string.pref_init_station_title,     R.string.pref_init_station_summary,    TDString.ZERO ),
    new TDPrefKey( A, LONG, UI,  "DISTOX_THUMBNAIL",        R.string.pref_thumbnail_title,        R.string.pref_thumbnail_summary,       "200"         ),
    new TDPrefKey( B, BOOL, DR,  "DISTOX_FIXED_ORIGIN",     R.string.pref_fixed_origin_title,     R.string.pref_fixed_origin_summary,    FALSE         ),
    new TDPrefKey( B, BOOL, DR,  "DISTOX_SHARED_XSECTIONS", R.string.pref_shared_xsections_title, R.string.pref_shared_xsections_summary, FALSE       ),
    new TDPrefKey( B,       NON, "DISTOX_SHOT_UNITS_SCREEN",R.string.pref_shot_units_title ), 
    new TDPrefKey( B,       NON, "DISTOX_SHOT_DATA_SCREEN", R.string.pref_shot_data_title  ),
    new TDPrefKey( N,       NON, "DISTOX_LOCATION_SCREEN",  R.string.pref_location_title   ), 
    new TDPrefKey( A,       NON, "DISTOX_ACCURACY_SCREEN",  R.string.pref_accuracy_title   )
  };
 
  static String policyKey() { return mSurvey[POLICY_INDEX].key; }

  /** plot settings
   * whether to enable side-drag
   * whether to have zoom-ctrl (no, temp., always)
   * if clino is over thr, H_SECTION is horizontal (has north arrow)
   * whether to check all shots are connected
   * whether leg extends are all set
   * number of symbils in toolbar
   * whether to use plot cache
   */
  static TDPrefKey[] mPlot = {
    new TDPrefKey( B, BOOL, UI,  "DISTOX_SIDE_DRAG",      R.string.pref_side_drag_title,     R.string.pref_side_drag_summary,     TRUE ),
    new TDPrefKey( B,       UI,  "DISTOX_ZOOM_CTRL",      R.string.pref_zoom_controls_title, R.string.pref_zoom_controls_summary, TDString.ZERO, R.array.zoomCtrl, R.array.zoomCtrlValue ),
    new TDPrefKey( T, FLT,  UI,  "DISTOX_HTHRESHOLD",     R.string.pref_hthr_title,          R.string.pref_hthr_summary,          "70"          ),
    new TDPrefKey( A, BOOL, DT,  "DISTOX_CHECK_ATTACHED", R.string.pref_checkAttached_title, R.string.pref_checkAttached_summary, FALSE         ),
    new TDPrefKey( A, BOOL, DT,  "DISTOX_CHECK_EXTEND",   R.string.pref_checkExtend_title,   R.string.pref_checkExtend_summary,   TRUE          ),
    new TDPrefKey( T, FLT,  UI,  "DISTOX_TOOLBAR_SIZE",   R.string.pref_toolbarSize_title,   R.string.pref_toolbarSize_summary,   TDString.FIVE ),
    new TDPrefKey( B, BOOL, GEN, "DISTOX_PLOT_CACHE",     R.string.pref_plot_cache_title,    R.string.pref_plot_cache_summary,    TRUE ),
    new TDPrefKey( B,       NON, "DISTOX_TOOL_POINT",     R.string.pref_tool_point_title  ),
    new TDPrefKey( N,       NON, "DISTOX_TOOL_LINE",      R.string.pref_tool_line_title   ),
    new TDPrefKey( B,       NON, "DISTOX_PLOT_SCREEN",    R.string.pref_plot_screen_title )
  };

  /** calibration settings
   * calib data grouping policy
   * calib data group distance threshold
   * calib computation param EPS
   * calib computation maximum number of iterations
   * whether remote calib data immediately downloaded
   * whether to display also raw calib data
   * min-algo params: alpha, beta, gmmma, delta
   * autocalib params: beta, eta, gamma, delta
   */
  static TDPrefKey[] mCalib = {
    new TDPrefKey( A,       CAL, "DISTOX_GROUP_BY",            R.string.pref_group_by_title,            R.string.pref_group_by_summary,            TDString.ONE, R.array.groupBy, R.array.groupByValue  ),
    new TDPrefKey( A, FLT,  CAL, "DISTOX_GROUP_DISTANCE",      R.string.pref_group_title,               R.string.pref_group_summary,               "40"          ),
    new TDPrefKey( B, FLT,  CAL, "DISTOX_CALIB_EPS",           R.string.pref_error_title,               R.string.pref_error_summary,               "0.000001"    ),
    new TDPrefKey( B, LONG, CAL, "DISTOX_CALIB_MAX_IT",        R.string.pref_iter_title,                R.string.pref_iter_summary,                "200"         ),
    new TDPrefKey( A, BOOL, BT,  "DISTOX_CALIB_SHOT_DOWNLOAD", R.string.pref_calib_shot_download_title, R.string.pref_calib_shot_download_summary, TRUE          ),
    new TDPrefKey( A,       UI,  "DISTOX_RAW_CDATA",           R.string.pref_raw_data_title,            R.string.pref_raw_data_summary,            TDString.ZERO, R.array.rawCData, R.array.rawCDataValue ), 
    new TDPrefKey( D, FLT,  CAL, "DISTOX_ALGO_MIN_ALPHA",      R.string.pref_algo_min_alpha_title,      R.string.pref_algo_min_alpha_summary,      "0.05"        ),
    new TDPrefKey( D, FLT,  CAL, "DISTOX_ALGO_MIN_BETA",       R.string.pref_algo_min_beta_title,       R.string.pref_algo_min_beta_summary,       "3.0"         ),
    new TDPrefKey( D, FLT,  CAL, "DISTOX_ALGO_MIN_GAMMA",      R.string.pref_algo_min_gamma_title,      R.string.pref_algo_min_gamma_summary,      "0.05"        ),
    new TDPrefKey( D, FLT,  CAL, "DISTOX_ALGO_MIN_DELTA",      R.string.pref_algo_min_delta_title,      R.string.pref_algo_min_delta_summary,      "0.05"        ),
    new TDPrefKey( D, FLT,  CAL, "DISTOX_AUTO_CAL_BETA",       R.string.pref_auto_cal_beta_title,       R.string.pref_auto_cal_beta_summary,       "0.004"       ),
    new TDPrefKey( D, FLT,  CAL, "DISTOX_AUTO_CAL_ETA",        R.string.pref_auto_cal_eta_title,        R.string.pref_auto_cal_eta_summary,        "0.04"        ),
    new TDPrefKey( D, FLT,  CAL, "DISTOX_AUTO_CAL_GAMMA",      R.string.pref_auto_cal_gamma_title,      R.string.pref_auto_cal_gamma_summary,      "0.04"        ),
    new TDPrefKey( D, FLT,  CAL, "DISTOX_AUTO_CAL_DELTA",      R.string.pref_auto_cal_delta_title,      R.string.pref_auto_cal_delta_summary,      "0.04"        )
  };

  /** device settings
   * whether to check bluetooth on start
   * data download mode [on-demand, continuous]
   * whether to read head-tail to get the number of packets to download
   * socket type
   * whether to auto-pair the discovered DistoX
   * connection fedback
   */
  static TDPrefKey[] mDevice = {
    new TDPrefKey( N,       BT,  "DISTOX_BLUETOOTH",        R.string.pref_checkBT_title,          R.string.pref_checkBT_summary,         TDString.ONE, R.array.deviceBT, R.array.deviceBTValue ),
    new TDPrefKey( B,       BT,  "DISTOX_CONN_MODE",        R.string.pref_conn_mode_title,        R.string.pref_conn_mode_summary,       TDString.ZERO,R.array.connMode, R.array.connModeValue ),
    new TDPrefKey( B, BOOL, BT,  "DISTOX_HEAD_TAIL",        R.string.pref_head_tail_title,        R.string.pref_head_tail_summary,       FALSE ),
    new TDPrefKey( B,       BT,  "DISTOX_SOCKET_TYPE",      R.string.pref_sock_type_title,        R.string.pref_sock_type_summary,       TDString.ONE, R.array.sockType, R.array.sockTypeValue ),
    // FIXME DROP_PAIRING
    // new TDPrefKey( A, BOOL, BT,  "DISTOX_AUTO_PAIR",        R.string.pref_auto_pair_title,        R.string.pref_auto_pair_summary,       FALSE ),
    new TDPrefKey( E,       UI,  "DISTOX_CONNECT_FEEDBACK", R.string.pref_connect_feedback_title, R.string.pref_connect_feedback_summary,TDString.ZERO, R.array.feedbackMode, R.array.feedbackModeValue ),
    new TDPrefKey( B,       NON, "DISTOX_CALIB_PREF",       R.string.pref_cat_calib )
  };

  /** enabled data export formats
   * IMPORTANT the order must be the same as TDConst.mSurveyExportEnable
   */
  static TDPrefKey[] mExportEnable = {
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_COMPASS",   R.string.compass,   -1, TRUE  ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_CSURVEY",   R.string.csurvey,   -1, FALSE ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_POLYGON",   R.string.polygon,   -1, FALSE ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_SURVEX",    R.string.survex,    -1, TRUE  ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_THERION",   R.string.therion,   -1, TRUE  ), 
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_TOPO",      R.string.topo,      -1, FALSE ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_TOPOROBOT", R.string.toporobot, -1, FALSE ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_VISUALTOPO",R.string.visualtopo,-1, TRUE  ), 
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_WALLS",     R.string.walls,     -1, TRUE  ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_WINKARST",  R.string.winkarst,  -1, FALSE ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_CVS",       R.string.csv,       -1, TRUE  ), 
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_DXF",       R.string.dxf,       -1, FALSE ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_KML",       R.string.kml,       -1, FALSE ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_GPX",       R.string.gpx,       -1, FALSE ),
    new TDPrefKey( B, BOOL, UI, "EXPORT_DATA_SHAPEFILE", R.string.shapefile, -1, FALSE )
  };

  /** export settings
   * default data export format
   * default plot export format
   * format to export plots to "out" folder automatically
   * orthogonal LRUD ( >=1 disable, min 0 )
   * 
   *
   * max step between interpolating points for bezier in export (cSurvey)
   */
  static TDPrefKey[] mExport = {
    new TDPrefKey( N,      XP,  "DISTOX_EXPORT_SHOTS",      R.string.pref_export_shots_title,     R.string.pref_export_shots_summary,    "-1", R.array.exportShots, R.array.exportShotsValue ),
    new TDPrefKey( N,      XP,  "DISTOX_EXPORT_PLOT",       R.string.pref_export_plot_title,      R.string.pref_export_plot_summary,     "-1", R.array.exportPlot, R.array.exportPlotValue ),
    new TDPrefKey( N,      XP,  "DISTOX_AUTO_PLOT_EXPORT",  R.string.pref_auto_plot_export_title, R.string.pref_auto_plot_export_summary,"-1", R.array.exportPlotAuto, R.array.exportPlotAutoValue ),
    new TDPrefKey( A, FLT, XP,  "DISTOX_ORTHO_LRUD",        R.string.pref_ortho_lrud_title,       R.string.pref_ortho_lrud_summary,      TDString.ZERO ),
    new TDPrefKey( A, FLT, XP,  "DISTOX_LRUD_VERTICAL",     R.string.pref_lrud_vertical_title,    R.string.pref_lrud_vertical_summary,   TDString.ZERO ),
    new TDPrefKey( A, FLT, XP,  "DISTOX_LRUD_HORIZONTAL",   R.string.pref_lrud_horizontal_title,  R.string.pref_lrud_horizontal_summary, TDString.NINETY ),
    new TDPrefKey( E, FLT, XP,  "DISTOX_BEZIER_STEP",       R.string.pref_bezier_step_title,      R.string.pref_bezier_step_summary,     "0.2" ),
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_ENABLE_PREF",R.string.pref_cat_exportenable ),
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_IMPORT_PREF",R.string.pref_cat_exportimport ), 
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_SVX_PREF",   R.string.pref_cat_exportsvx ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_TH_PREF",    R.string.pref_cat_exportth  ),    
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_CSX_PREF",   R.string.pref_cat_exportcsx ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_DAT_PREF",   R.string.pref_cat_exportdat ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_TRO_PREF",   R.string.pref_cat_exporttro ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_SRV_PREF",   R.string.pref_cat_exportsrv ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_PLY_PREF",   R.string.pref_cat_exportply ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_SVG_PREF",   R.string.pref_cat_exportsvg ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_SHP_PREF",   R.string.pref_cat_exportshp ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_DXF_PREF",   R.string.pref_cat_exportdxf ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_KML_PREF",   R.string.pref_cat_exportkml ),
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_GPX_PREF",   R.string.pref_cat_exportgpx ),   
    new TDPrefKey( N,      NON, "DISTOX_EXPORT_CSV_PREF",   R.string.pref_cat_exportcsv )
  };                                                    

  /** import settings
   * PocketTopo color-symbol map
   * whether to set L/R extend to LRUD splay shots (Compass, VTopo import)
   */
  static TDPrefKey[] mExportImport = {
    new TDPrefKey( N, BTN,  XP, "DISTOX_PT_CMAP",      R.string.pref_pt_color_map_title, R.string.pref_pt_color_map_summary, TDString.EMPTY ),
    new TDPrefKey( A, BOOL, XP, "DISTOX_SPLAY_EXTEND", R.string.pref_LRExtend_title,     R.string.pref_LRExtend_summary,     TRUE )
  };

  /** cSurvey
   * whether to add cave-name prefix to stations (cSurvey/Compass export)
   * whether export also media
   * @note 1 key repeated
   */
  static TDPrefKey[] mExportCsx = {
    new TDPrefKey( N, BOOL, XP, "DISTOX_STATION_PREFIX", R.string.pref_station_prefix_title, R.string.pref_station_prefix_summary, FALSE ),
    new TDPrefKey( N, BOOL, XP, "DISTOX_WITH_MEDIA",     R.string.pref_with_media_title,     R.string.pref_with_media_summary,     FALSE )
  };

  /** GPX settings
   * whether to export project surveys as a single track
   */
  static TDPrefKey[] mExportGpx = {
    new TDPrefKey( N, BOOL, XP, "DISTOX_GPX_SINGLE_TRACK", R.string.pref_gpx_single_track_title, R.string.pref_gpx_single_track_summary, TRUE )
  };

  /** CSV settings
   * whether to export also raw values
   * separator: 0 comma, 1 pipe, 2 tab
   * end-of-line:
   */
  static TDPrefKey[] mExportCsv = {
    new TDPrefKey( N, BOOL, XP, "DISTOX_CSV_RAW",    R.string.pref_csv_raw_title, R.string.pref_csv_raw_summary, FALSE ),
    new TDPrefKey( A,       XP, "DISTOX_CSV_SEP",    R.string.pref_csv_sep_title, R.string.pref_csv_sep_summary, TDString.ZERO, R.array.csvSeparator, R.array.csvSeparatorValue ),
    new TDPrefKey( N,       XP, "DISTOX_SURVEX_EOL", R.string.pref_csv_eol_title, R.string.pref_csv_eol_summary, "lf",          R.array.survexEol, R.array.survexEolValue )
  };

  /** Survex settings
   * survex end of line [either Linux or Windows]
   * whether to name endpoint of splays in Survex export
   * whether to add LRUD to Survex export
   * EPSG number for cs out
   * @note 2 key repeated
   */
  static TDPrefKey[] mExportSvx = {
    new TDPrefKey( N,       XP, "DISTOX_SURVEX_EOL",   R.string.pref_survex_eol_title,  R.string.pref_survex_eol_summary,   "lf", R.array.survexEol, R.array.survexEolValue ),
    new TDPrefKey( A, BOOL, XP, "DISTOX_SURVEX_LRUD",  R.string.pref_survex_lrud_title, R.string.pref_survex_lrud_summary,  FALSE ),
    new TDPrefKey( A, BOOL, XP, "DISTOX_SURVEX_SPLAY", R.string.pref_survex_splay_title,R.string.pref_survex_splay_summary, FALSE ),
    new TDPrefKey( A, LONG, XP, "DISTOX_SURVEX_EPSG",  R.string.pref_survex_epsg_title, R.string.pref_survex_epsg_summary,  TDString.ZERO )
  };

  /** Therion settings
   * whether to write survey.thconfig file
   * whether to put map commands before centerline in therion
   * whether to add u:splay lines to Therion th2 export
   * whether to add LRUD to Survex export
   * th2/xvi scale
   * th2 with xvi image comment
   */
  static TDPrefKey[] mExportTh = {
    new TDPrefKey( A, BOOL, XP, "DISTOX_THERION_CONFIG", R.string.pref_therion_config_title,R.string.pref_therion_config_summary, FALSE ),
    new TDPrefKey( A, BOOL, XP, "DISTOX_THERION_MAPS",   R.string.pref_therion_maps_title,  R.string.pref_therion_maps_summary,   FALSE ),
    new TDPrefKey( A, BOOL, XP, "DISTOX_THERION_SPLAYS", R.string.pref_therion_splays_title,R.string.pref_therion_splays_summary, FALSE ),
    new TDPrefKey( A, BOOL, XP, "DISTOX_SURVEX_LRUD",    R.string.pref_survex_lrud_title,   R.string.pref_survex_lrud_summary,    FALSE ),
    new TDPrefKey( E, LONG, XP, "DISTOX_TH2_SCALE",      R.string.pref_th2_scale_title,     R.string.pref_th2_scale_summary,      "100" ),
    new TDPrefKey( E, BOOL, XP, "DISTOX_TH2_XVI",        R.string.pref_th2_xvi_title,       R.string.pref_th2_xvi_summary,        FALSE )
  };

  /** SVG settings
   * whether to export SVG in round-trip format
   * whether to export grid in SVG 
   * whether to add line orientation ticks in SVG export
   *
   * whether to group items by the type in SVG export
   * ...
   * SVG program:  inkscape or illustrator
   */
  static TDPrefKey[] mExportSvg = {
    new TDPrefKey( T, BOOL, XP, "DISTOX_SVG_ROUNDTRIP",      R.string.pref_svg_roundtrip_title,    R.string.pref_svg_roundtrip_summary,     FALSE ),
    new TDPrefKey( E, BOOL, XP, "DISTOX_SVG_GRID",           R.string.pref_svg_grid_title,         R.string.pref_svg_grid_summary,          FALSE ),
    new TDPrefKey( E, BOOL, XP, "DISTOX_SVG_LINE_DIR",       R.string.pref_svg_line_dir_title,     R.string.pref_svg_line_dir_summary,      FALSE ),
    new TDPrefKey( N, BOOL, XP, "DISTOX_SVG_SPLAYS",         R.string.pref_svg_splays_title,       R.string.pref_svg_splays_summary,        TRUE  ),
    new TDPrefKey( N, BOOL, XP, "DISTOX_SVG_GROUPS",         R.string.pref_svg_groups_title,       R.string.pref_svg_groups_summary,        FALSE ),
    new TDPrefKey( A, FLT,  XP, "DISTOX_SVG_POINT_STROKE",   R.string.pref_svg_pointstroke_title,  R.string.pref_svg_pointstroke_summary,   "0.5" ),
    new TDPrefKey( A, FLT,  XP, "DISTOX_SVG_LABEL_STROKE",   R.string.pref_svg_labelstroke_title,  R.string.pref_svg_labelstroke_summary,   "1.5" ),
    new TDPrefKey( A, FLT,  XP, "DISTOX_SVG_LINE_STROKE",    R.string.pref_svg_linestroke_title,   R.string.pref_svg_linestroke_summary,    "2.5" ), 
    new TDPrefKey( A, FLT,  XP, "DISTOX_SVG_GRID_STROKE",    R.string.pref_svg_gridstroke_title,   R.string.pref_svg_gridstroke_summary,    "2.5" ),
    new TDPrefKey( A, FLT,  XP, "DISTOX_SVG_SHOT_STROKE",    R.string.pref_svg_shotstroke_title,   R.string.pref_svg_shotstroke_summary,    "2.5" ),
    new TDPrefKey( A, FLT,  XP, "DISTOX_SVG_LINEDIR_STROKE", R.string.pref_svg_linedirstroke_title,R.string.pref_svg_linedirstroke_summary, "12.0" ),
    new TDPrefKey( A, LONG, XP, "DISTOX_SVG_STATION_SIZE",   R.string.pref_svg_stationsize_title,  R.string.pref_svg_stationsize_summary,   "32"   ),
    new TDPrefKey( A, LONG, XP, "DISTOX_SVG_LABEL_SIZE",     R.string.pref_svg_labelsize_title,    R.string.pref_svg_labelsize_summary,     "48"   ),
    new TDPrefKey( N,       XP, "DISTOX_SVG_PROGRAM",        R.string.pref_svg_program_title,      R.string.pref_svg_program_summary,       TDString.ONE, R.array.svgProgram, R.array.svgProgramValue )
  };                                                                                                                                        

  /** Compass settings
   * whether to add cave-name prefix to stations (cSurvey/Compass export)
   * whether to add splays to Compass dat export
   * whether to swap Left-Right in Compass export
   */
  static TDPrefKey[] mExportDat = {
    new TDPrefKey( N, BOOL, XP, "DISTOX_STATION_PREFIX", R.string.pref_station_prefix_title,R.string.pref_station_prefix_summary, FALSE ),
    new TDPrefKey( A, BOOL, XP, "DISTOX_COMPASS_SPLAYS", R.string.pref_compass_splays_title,R.string.pref_compass_splays_summary, TRUE  ),    
    new TDPrefKey( N, BOOL, XP, "DISTOX_SWAP_LR",        R.string.pref_swapLR_title,        R.string.pref_swapLR_summary,         FALSE )  
  };

  /** Walls settings
   * whether to use splays for Walls srv export
   */
  static TDPrefKey[] mExportSrv = {
    new TDPrefKey( N, BOOL, XP, "DISTOX_WALLS_SPLAYS",  R.string.pref_walls_splays_title,  R.string.pref_walls_splays_summary, TRUE )
  };

  /** Polygon settings
   *
   *
   */
  static TDPrefKey[] mExportPly = {
    new TDPrefKey( N, BOOL, XP,"DISTOX_PLY_LRUD", R.string.pref_ply_lrud_title,  R.string.pref_ply_lrud_summary,  TRUE ),
    new TDPrefKey( N, BOOL, XP,"DISTOX_PLY_MINUS",R.string.pref_ply_minus_title, R.string.pref_ply_minus_summary, FALSE )
  };

  /** VisualTopo settings
   * whether to add splays to VisualTopo tro export
   * whether VisualTopo LRUD are at-from
   * whether to write trox format
   */
  static TDPrefKey[] mExportTro = {
    new TDPrefKey( A, BOOL, XP, "DISTOX_VTOPO_SPLAYS", R.string.pref_vtopo_splays_title, R.string.pref_vtopo_splays_summary,TRUE  ),
    new TDPrefKey( N, BOOL, XP, "DISTOX_VTOPO_LRUD",   R.string.pref_vtopo_lrud_title,   R.string.pref_vtopo_lrud_summary,  FALSE ),
    new TDPrefKey( N, BOOL, XP, "DISTOX_VTOPO_TROX",   R.string.pref_vtopo_trox_title,   R.string.pref_vtopo_trox_summary,  FALSE )
  };

  /** KML settings
   * whether to add station points to KML export
   * whether to add splay lines to KML export
   */
  static TDPrefKey[] mExportKml = {
    new TDPrefKey( N, BOOL, XP, "DISTOX_KML_STATIONS",  R.string.pref_kml_stations_title, R.string.pref_kml_stations_summary, TRUE  ),
    new TDPrefKey( E, BOOL, XP, "DISTOX_KML_SPLAYS",    R.string.pref_kml_splays_title,   R.string.pref_kml_splays_summary,   FALSE )
  };

  // static TDPrefKey[] mExportPng = {
  // };

  /** DXF settings
   * default DXF scale (export)
   * whether to export point items as Blocks in DXF export
   * 23 31
   *
   *
   * whether to use layers (?)
   */
  static TDPrefKey[] mExportDxf = {
    new TDPrefKey( N, BOOL, XP, "DISTOX_DXF_BLOCKS",    R.string.pref_dxf_blocks_title,    R.string.pref_dxf_blocks_summary,    TRUE ),
    new TDPrefKey( E,       XP, "DISTOX_ACAD_VERSION",  R.string.pref_acad_version_title,  R.string.pref_acad_version_summary,  "9", R.array.acadVersion, R.array.acadVersionValue ),
    new TDPrefKey( T, BOOL, XP, "DISTOX_ACAD_SPLINE",   R.string.pref_acad_spline_title,   R.string.pref_acad_spline_summary,   TRUE ),
    new TDPrefKey( A, BOOL, XP, "DISTOX_DXF_REFERENCE", R.string.pref_dxf_reference_title, R.string.pref_dxf_reference_summary, FALSE),
    new TDPrefKey( T, BOOL, XP, "DISTOX_ACAD_LAYER",    R.string.pref_acad_layer_title,    R.string.pref_acad_layer_summary,    TRUE )
  };

  /** Shapefile settings
   * whether to export plan-sketch georeferenced
   */
  static TDPrefKey[] mExportShp = {
    new TDPrefKey( T, BOOL, XP, "DISTOX_SHP_GEOREF", R.string.pref_shp_georef_title, R.string.pref_shp_georef_summary, FALSE )
  };

  /** survey data settings
   * tolerance among leg shots [%]
   * maximum length of a shot data
   *  minimum length of a shot data
   * nr. of shots to make a leg [2, 3, 4]
   * half angle around 90 where splays have "vert" extend
   * if shot clino is above, LRUD are horizontal
   * whether the "extend" is fixed L or R, selected by hand 
   * whether to put "prev-next" arrows in shot edit dialog
   * whether to add backsight fields in manual shot input dialog
   *
   */
  static TDPrefKey[] mData = {
    new TDPrefKey( B, FLT, DT, "DISTOX_CLOSE_DISTANCE",  R.string.pref_leg_title,            R.string.pref_leg_summary,            "0.05" ),        
    new TDPrefKey( B, FLT, DT, "DISTOX_MAX_SHOT_LENGTH", R.string.pref_max_shot_title,       R.string.pref_max_shot_summary,       TDString.FIFTY ),
    new TDPrefKey( B, FLT, DT, "DISTOX_MIN_LEG_LENGTH",  R.string.pref_min_leg_title,        R.string.pref_min_leg_summary,        TDString.ZERO  ),
    new TDPrefKey( E,      DT, "DISTOX_LEG_SHOTS",       R.string.pref_leg_shots_title,      R.string.pref_leg_shots_summary,      TDString.THREE, R.array.legShots, R.array.legShotsValue ),
    new TDPrefKey( N, FLT, DR, "DISTOX_EXTEND_THR2",     R.string.pref_ethr_title,           R.string.pref_ethr_summary,           TDString.TEN  ),
    new TDPrefKey( N, FLT, DT, "DISTOX_VTHRESHOLD",      R.string.pref_vthr_title,           R.string.pref_vthr_summary,           "80"          ),
    new TDPrefKey( A, BOOL,DR, "DISTOX_AZIMUTH_MANUAL",  R.string.pref_azimuth_manual_title, R.string.pref_azimuth_manual_summary, FALSE ), 
    new TDPrefKey( A, BOOL,UI, "DISTOX_PREV_NEXT",       R.string.pref_prev_next_title,      R.string.pref_prev_next_summary,      TRUE  ),
    new TDPrefKey( A, BOOL,UI, "DISTOX_BACKSIGHT",       R.string.pref_backsight_title,      R.string.pref_backsight_summary,      FALSE ),
    new TDPrefKey( N,      UI, "DISTOX_LEG_FEEDBACK",    R.string.pref_triple_shot_title,    R.string.pref_triple_shot_summary,    TDString.ZERO, R.array.feedbackMode, R.array.feedbackModeValue )
  };

  /** units settings
   * units of lengths [m, y, ft]
   * units of angles [deg, grad]
   * plot grid unit [m, y, 2ft]
   * ruler units [cell, m, ft]
   */
  static TDPrefKey[] mUnits = {
    new TDPrefKey(  B,   UI, "DISTOX_UNIT_LENGTH",  R.string.pref_unit_length_title,  R.string.pref_unit_length_summary,  "meters", R.array.unitLength,   R.array.unitLengthValue ),
    new TDPrefKey(  B,   UI, "DISTOX_UNIT_ANGLE",   R.string.pref_unit_angle_title,   R.string.pref_unit_angle_summary,   "degrees",R.array.unitAngle,    R.array.unitAngleValue  ),
    new TDPrefKey(  B,   UI, "DISTOX_UNIT_GRID",    R.string.pref_unit_grid_title,    R.string.pref_unit_grid_summary,    "1.0",    R.array.unitGrid,     R.array.unitGridValue   ),
    new TDPrefKey(  B,   UI, "DISTOX_UNIT_MEASURE", R.string.pref_unit_measure_title, R.string.pref_unit_measure_summary, "-1.0",   R.array.unitMeasure,  R.array.unitMeasureValue )
  };

  /** accuracy settings
   * shot quality G threshold [%]
   * shot quality M threshold [%]
   * shot quality dip threshold [deg]
   * shot sibling threshold [%]
   */
  static TDPrefKey[] mAccuracy = {
    new TDPrefKey( A, FLT, DT, "DISTOX_ACCEL_PERCENT",   R.string.pref_accel_thr_title,   R.string.pref_accel_thr_summary,   "1.0" ),
    new TDPrefKey( A, FLT, DT, "DISTOX_MAG_PERCENT",     R.string.pref_mag_thr_title,     R.string.pref_mag_thr_summary,     "1.0" ),
    new TDPrefKey( A, FLT, DT, "DISTOX_DIP_THR",         R.string.pref_dip_thr_title,     R.string.pref_dip_thr_summary,     "2.0" ),
    new TDPrefKey( A, FLT, DT, "DISTOX_SIBLING_PERCENT", R.string.pref_sibling_thr_title, R.string.pref_sibling_thr_summary, "5.0" )
  };

  /** location settings
   * units of location [ddmmss dec.deg]
   * default C.R.S.
   * allow negative altitudes
   * always allow editable altitudes
   * fine location time
   * geopoint import app
   */
  static TDPrefKey[] mLocation = {
    new TDPrefKey( N,       UI, "DISTOX_UNIT_LOCATION", R.string.pref_unit_location_title,R.string.pref_unit_location_summary, "ddmmss", R.array.unitLocation, R.array.unitLocationValue ),
    new TDPrefKey( A, STR,  DT, "DISTOX_CRS",           R.string.pref_crs_title,          R.string.pref_crs_summary,           "Long-Lat" ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_NEG_ALTITUDE",  R.string.pref_neg_altitude_title, R.string.pref_neg_altitude_summary,  FALSE ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_EDIT_ALTITUDE", R.string.pref_edit_altitude_title,R.string.pref_edit_altitude_summary, FALSE ),
    new TDPrefKey( E, LONG, DT, "DISTOX_FINE_LOCATION", R.string.pref_fine_location_title,R.string.pref_fine_location_summary, "60" ),
    new TDPrefKey( N,       DT, "DISTOX_GEOPOINT_APP",  R.string.pref_geoimport_app_title,R.string.pref_geoimport_app_summary, "0", R.array.geoImportApp, R.array.geoImportAppValue )
  };
 
  /** canvas settings
   * thickness of fixed lines
   * size of station names [pt]
   * radius of green dots
   * "select" radius // "select" radius // "select" radius
   * "erase" radius // "erase" radius
   * maximum amount for a shift (to avoid jumps)
   * "size" of a "point touch" (max distance between down and up)
   * alpha (transparency) for splays
   */
  static TDPrefKey[] mScreen = { 
    new TDPrefKey( B, FLT,  UI, "DISTOX_FIXED_THICKNESS", R.string.pref_fixed_thickness_title,R.string.pref_fixed_thickness_summary,TDString.ONE ),
    new TDPrefKey( B, FLT,  UI, "DISTOX_STATION_SIZE",    R.string.pref_station_size_title,   R.string.pref_station_size_summary,   TDString.TWENTY ),
    new TDPrefKey( N, FLT,  UI, "DISTOX_DOT_RADIUS",      R.string.pref_dot_radius_title,     R.string.pref_dot_radius_message,     TDString.FIVE ), 
    new TDPrefKey( B, LONG, UI, "DISTOX_CLOSENESS",       R.string.pref_closeness_title,      R.string.pref_closeness_message,      TDString.TWENTYFOUR ),
    new TDPrefKey( B, LONG, UI, "DISTOX_ERASENESS",       R.string.pref_eraseness_title,      R.string.pref_eraseness_message,      "36" ),
    new TDPrefKey( E, LONG, UI, "DISTOX_MIN_SHIFT",       R.string.pref_min_shift_title,      R.string.pref_min_shift_message,      TDString.SIXTY ),
    new TDPrefKey( E, LONG, UI, "DISTOX_POINTING",        R.string.pref_pointing_title,       R.string.pref_pointing_message,       TDString.TWENTYFOUR ),
    new TDPrefKey( T, LONG, UI, "DISTOX_SPLAY_ALPHA",     R.string.pref_splay_alpha_title,    R.string.pref_splay_alpha_summary,    "80" )
  };

  /** lines settings
   * thickness of normal lines (walls are twice)
   * line units
   * line style: 0 bezier, 1 fine, 2 normal, 3 coarse
   * minimum distance between consecutive points on a line
   * close lines of closed type
   * slope line l-side
   * length of the tick at the first line-point (when applicable)
   * whether to add section point when tracing a section line
   * area border visibility
   * @note 7 keys repeated
   */
  static TDPrefKey[] mLine = {
    new TDPrefKey( N, FLT,  UI, "DISTOX_LINE_THICKNESS",  R.string.pref_line_thickness_title, R.string.pref_line_thickness_summary,  TDString.ONE ),
    new TDPrefKey( N,       DR, "DISTOX_LINE_STYLE",      R.string.pref_linestyle_title,      R.string.pref_linestyle_summary,       TDString.TWO, R.array.lineStyle, R.array.lineStyleValue ),
    new TDPrefKey( N, LONG, DR, "DISTOX_LINE_SEGMENT",    R.string.pref_segment_title,        R.string.pref_segment_message,         TDString.TEN ),
    new TDPrefKey( N, BOOL, DR, "DISTOX_LINE_CLOSE",      R.string.pref_line_close_title,     R.string.pref_line_close_summary,      TRUE ),
    new TDPrefKey( A, FLT,  DR, "DISTOX_ARROW_LENGTH",    R.string.pref_arrow_length_title,   R.string.pref_arrow_length_message,    "8"  ),
    new TDPrefKey( A, BOOL, UI, "DISTOX_AUTO_SECTION_PT", R.string.pref_auto_section_pt_title,R.string.pref_auto_section_pt_summary, TRUE ),
    new TDPrefKey( N, BOOL, UI, "DISTOX_AREA_BORDER",     R.string.pref_area_border_title,    R.string.pref_area_border_summary,     TRUE ),
    new TDPrefKey( N, FLT,  DR, "DISTOX_LINE_UNITS",      R.string.pref_line_units_title,     R.string.pref_line_units_summary,      "1.4" ),
    new TDPrefKey( A, LONG, UI, "DISTOX_SLOPE_LSIDE",     R.string.pref_slope_lside_title,    R.string.pref_slope_lside_summary,     "20" )
  };

  /** point settings
   * whether drawing point items should stay unscaled when zooming
   * plot unit
   * size of labels [pt]
   * 
   * section point offset
   * @note 3 keys repeated
   */
  static TDPrefKey[] mPoint = {
    new TDPrefKey( N, BOOL, UI, "DISTOX_UNSCALED_POINTS",  R.string.pref_unscaled_points_title,  R.string.pref_unscaled_points_summary,FALSE ),
    new TDPrefKey( B, FLT,  DR, "DISTOX_DRAWING_UNIT",     R.string.pref_drawing_unit_title,     R.string.pref_drawing_unit_summary,   "1.2" ),
    new TDPrefKey( B, FLT,  UI, "DISTOX_LABEL_SIZE",       R.string.pref_label_size_title,       R.string.pref_label_size_summary,     TDString.TWENTYFOUR ),
    new TDPrefKey( N, BOOL, UI, "DISTOX_SCALABLE_LABEL",   R.string.pref_scalable_label_title,   R.string.pref_scalable_label_summary, FALSE ),
    new TDPrefKey( N, LONG, DR, "DISTOX_XSECTION_OFFSET",  R.string.pref_xsection_offset_title,  R.string.pref_xsection_offset_summary,"20" )
  };
  
  // static TDPrefKey() mWals = {
  // };

  /** drawing settings
   * whether drawing point items should stay unscaled when zooming
   * plot unit
   * size of labels [pt]
   * thickness of normal lines (walls are twice)
   * line style: 0 bezier, 1 fine, 2 normal, 3 coarse
   * close lines of closed type
   *                                                               
   * length of the tick at the first line-point (when applicable)
   * whether to add section point when tracing a section line
   * area border visibility
   */
  static TDPrefKey[] mDraw = {
    new TDPrefKey( N, BOOL, UI, "DISTOX_UNSCALED_POINTS", R.string.pref_unscaled_points_title, R.string.pref_unscaled_points_summary, FALSE ),
    new TDPrefKey( B, FLT,  DR, "DISTOX_DRAWING_UNIT",    R.string.pref_drawing_unit_title,    R.string.pref_drawing_unit_summary,    "1.2" ),
    new TDPrefKey( B, FLT,  UI, "DISTOX_LABEL_SIZE",      R.string.pref_label_size_title,      R.string.pref_label_size_summary,      TDString.TWENTYFOUR ),
    new TDPrefKey( N, FLT,  UI, "DISTOX_LINE_THICKNESS",  R.string.pref_line_thickness_title,  R.string.pref_line_thickness_summary,  TDString.ONE ),
    new TDPrefKey( N,       DR, "DISTOX_LINE_STYLE",      R.string.pref_linestyle_title,       R.string.pref_linestyle_summary,       TDString.TWO, R.array.lineStyle, R.array.lineStyleValue ),
    new TDPrefKey( N, LONG, DR, "DISTOX_LINE_CLOSE",      R.string.pref_line_close_title,      R.string.pref_line_close_summary,      TRUE ),
    new TDPrefKey( N, BOOL, DR, "DISTOX_LINE_SEGMENT",    R.string.pref_segment_title,         R.string.pref_segment_message,         TDString.TEN ),
    new TDPrefKey( A, FLT,  UI, "DISTOX_ARROW_LENGTH",    R.string.pref_arrow_length_title,    R.string.pref_arrow_length_message,    "8" ),
    new TDPrefKey( A, BOOL, DR, "DISTOX_AUTO_SECTION_PT", R.string.pref_auto_section_pt_title, R.string.pref_auto_section_pt_summary, FALSE ),
    new TDPrefKey( N, BOOL, UI, "DISTOX_AREA_BORDER",     R.string.pref_area_border_title,     R.string.pref_area_border_summary,     TRUE )
  };

  /** eraser settings
   * "select" radius // "select" radius // "select" radius
   * "erase" radius // "erase" radius
   * "size" of a "point touch" (max distance between down and up)
   * @note all keys repeated
   */
  static TDPrefKey[] mErase = {
    new TDPrefKey( B, LONG,  UI, "DISTOX_CLOSENESS",  R.string.pref_closeness_title, R.string.pref_closeness_message,TDString.TWENTYFOUR ),
    new TDPrefKey( B, LONG,  UI, "DISTOX_ERASENESS",  R.string.pref_eraseness_title, R.string.pref_eraseness_message,"36" ),
    new TDPrefKey( E, LONG,  UI, "DISTOX_POINTING",   R.string.pref_pointing_title,  R.string.pref_pointing_message, TDString.TWENTYFOUR )
  };

  /** edit settings
   * radius of green dots
   * "select" radius // "select" radius // "select" radius
   * maximum amount for a shift (to avoid jumps)
   * "size" of a "point touch" (max distance between down and up)
   * @note all keys repeated
   */ 
  static TDPrefKey[] mEdit = {
    new TDPrefKey( N, FLT,  UI, "DISTOX_DOT_RADIUS",  R.string.pref_dot_radius_title, R.string.pref_dot_radius_message, TDString.FIVE ),
    new TDPrefKey( B, LONG, UI, "DISTOX_CLOSENESS",   R.string.pref_closeness_title,  R.string.pref_closeness_message,  TDString.TWENTYFOUR ),
    new TDPrefKey( E, LONG, UI, "DISTOX_MIN_SHIFT",   R.string.pref_min_shift_title,  R.string.pref_min_shift_message,  TDString.SIXTY ),
    new TDPrefKey( E, LONG, UI, "DISTOX_POINTING",    R.string.pref_pointing_title,   R.string.pref_pointing_message,   TDString.TWENTYFOUR )
  };

  /** additional line settings
   * "rock" reducing lines: maximal angle
   * Bezier interpolator param:
   * Bezier interpolator param:
   *                                                       
   *                                                       
   *                                                       
   * whether to show line-snap action
   * whether to show line- smooth/straighten action
   * whether to show line-straighten (more "rocky") button
   * enable path multiselection
   */ 
  static TDPrefKey[] mGeekLine = {
    new TDPrefKey( T, FLT,  DR, "DISTOX_REDUCE_ANGLE",     R.string.pref_reduce_angle_title,    R.string.pref_reduce_angle_summary,     "45" ),
    new TDPrefKey( T, FLT,  DR, "DISTOX_LINE_ACCURACY",    R.string.pref_lineacc_title,         R.string.pref_lineacc_summary,          "1.0" ),
    new TDPrefKey( T, FLT,  DR, "DISTOX_LINE_CORNER",      R.string.pref_linecorner_title,      R.string.pref_linecorner_summary,       "20.0" ),
    new TDPrefKey( E, FLT,  DR, "DISTOX_WEED_DISTANCE",    R.string.pref_weeddistance_title,    R.string.pref_weeddistance_summary,     "0.5" ),
    new TDPrefKey( E, FLT,  DR, "DISTOX_WEED_LENGTH",      R.string.pref_weedlength_title,      R.string.pref_weedlength_summary,       "2.0" ),
    new TDPrefKey( E, FLT,  DR, "DISTOX_WEED_BUFFER",      R.string.pref_weedbuffer_title,      R.string.pref_weedbuffer_summary,       "10"  ),
    new TDPrefKey( A, BOOL, UI, "DISTOX_LINE_SNAP",        R.string.pref_linesnap_title,        R.string.pref_linesnap_summary,         FALSE ),
    new TDPrefKey( A, BOOL, UI, "DISTOX_LINE_CURVE",       R.string.pref_linecurve_title,       R.string.pref_linecurve_summary,        FALSE ),
    new TDPrefKey( A, BOOL, UI, "DISTOX_LINE_STRAIGHT",    R.string.pref_linestraight_title,    R.string.pref_linestraight_summary,     FALSE ),
    new TDPrefKey( T, BOOL, UI, "DISTOX_PATH_MULTISELECT", R.string.pref_path_multiselect_title,R.string.pref_path_multiselect_summary, FALSE )
  };

  /** additional data settings
   * enable diving mode
   * enable DistoX shot editing
   * whether first splay is backsight check
   * highlight recent shots
   * recent block timeout
   * fractional extend
   * using DistoX in backshot mode
   * splays bed plane interpolation
   * using sensors
   * loop compensation: 0 no, 1 yes, 2 weighted, 3 selective, 4 triangle
   * selective loop compensation threshold [%]
   * android azimuth+clino
   * bearing-clino timer [1/10 s]
   * bearing-clino beep volume [%]
   * skipping in-leg blunder shot
   * keep splay stations if already assigned
   * splay stations group only-forward rename
   */ 
  static TDPrefKey[] mGeekShot = {
    new TDPrefKey( T, BOOL, DT, "DISTOX_DIVING_MODE",        R.string.pref_diving_mode_title,         R.string.pref_diving_mode_summary,         FALSE ),
    new TDPrefKey( B, BOOL, GEN,"DISTOX_TAMPERING",          R.string.pref_shot_tampering_title,      R.string.pref_shot_tampering_summary,      FALSE ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_BACKSIGHT_SPLAY",    R.string.pref_backsight_splay_title,     R.string.pref_backsight_splay_summary,     FALSE ),
    new TDPrefKey( T, BOOL, UI, "DISTOX_RECENT_SHOT",        R.string.pref_recent_shot_title,         R.string.pref_recent_shot_summary,         FALSE ),
    new TDPrefKey( T, LONG, UI, "DISTOX_RECENT_TIMEOUT",     R.string.pref_recent_timeout_title,      R.string.pref_recent_timeout_summary,      "30"  ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_EXTEND_FRAC",        R.string.pref_extend_frac_title,         R.string.pref_extend_frac_summary,         FALSE ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_BACKSHOT",           R.string.pref_backshot_title,            R.string.pref_backshot_summary,            FALSE ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_BEDDING",            R.string.pref_plane_interpolation_title, R.string.pref_plane_interpolation_summary, FALSE ),
    new TDPrefKey( A, BOOL, XT, "DISTOX_WITH_SENSORS",       R.string.pref_with_sensors_title,        R.string.pref_with_sensors_summary,        FALSE ),
    new TDPrefKey( E,       DT, "DISTOX_LOOP_CLOSURE_VALUE", R.string.pref_loopClosure_title,         R.string.pref_loopClosure_summary,         TDString.ZERO, R.array.loopClosure, R.array.loopClosureValue ),
    new TDPrefKey( E, FLT,  DT, "DISTOX_LOOP_THRESHOLD",     R.string.pref_loop_thr_title,            R.string.pref_loop_thr_summary,            TDString.ONE ),
    new TDPrefKey( A, BOOL, XT, "DISTOX_ANDROID_AZIMUTH",    R.string.pref_with_android_azimuth_title,R.string.pref_with_android_azimuth_summary,FALSE        ),
    new TDPrefKey( E, LONG, XT, "DISTOX_SHOT_TIMER",         R.string.pref_shot_timer_title,          R.string.pref_shot_timer_summary,          TDString.TEN ),
    new TDPrefKey( E, LONG, XT, "DISTOX_BEEP_VOLUME",        R.string.pref_beep_volume_title,         R.string.pref_beep_volume_summary,         TDString.FIFTY ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_BLUNDER_SHOT",       R.string.pref_blunder_shot_title,        R.string.pref_blunder_shot_summary,        FALSE ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_SPLAY_STATION",      R.string.pref_splay_station_title,       R.string.pref_splay_station_summary,       TRUE  ),
    new TDPrefKey( T, BOOL, DT, "DISTOX_SPLAY_GROUP",        R.string.pref_splay_group_title,         R.string.pref_splay_group_summary,         FALSE )
  };

  /** additional plot settings
   * plot shift and scale
   * plot split and merge
   * size of stylus (0: no stylus)
   * number of plot backups
   * minimum interval between plot backups [60 s]
   * whether to color saved stations
   * whether to update drawing windows at every shot
   * whether to do full affine transform or shift+scale only
   * 0 no, 1 by class, 2 by instance
   *                                                            
   * whether to allow slanted xsections in clino degrees 5 by 5
   * number of points to cut from the line end
   */ 
  static TDPrefKey[] mGeekPlot = {
    new TDPrefKey( T, BOOL, UI,  "DISTOX_PLOT_SHIFT",       R.string.pref_plot_shift_title,        R.string.pref_plot_shift_summary,        FALSE ),
    new TDPrefKey( T, BOOL, XT,  "DISTOX_PLOT_SPLIT",       R.string.pref_plot_split_title,        R.string.pref_plot_split_summary,        FALSE ),
    new TDPrefKey( T, FLT,  UI,  "DISTOX_STYLUS_SIZE",      R.string.pref_stylus_size_title,       R.string.pref_stylus_size_summary,       TDString.ZERO ),
    new TDPrefKey( A,       GEN, "DISTOX_BACKUP_NUMBER",    R.string.pref_backup_number_title,     R.string.pref_backup_number_summary,     TDString.FIVE, R.array.backupNumber, R.array.backupNumberValue ),
    new TDPrefKey( A, LONG, GEN, "DISTOX_BACKUP_INTERVAL",  R.string.pref_backup_interval_title,   R.string.pref_backup_interval_summary,   TDString.SIXTY ),  
    new TDPrefKey( T, BOOL, UI,  "DISTOX_SAVED_STATIONS",   R.string.pref_saved_stations_title,    R.string.pref_saved_stations_summary,    FALSE ),
    new TDPrefKey( T, BOOL, UI,  "DISTOX_LEGONLY_UPDATE",   R.string.pref_legonly_update_title,    R.string.pref_legonly_update_summary,    FALSE ),
    new TDPrefKey( T, BOOL, XT,  "DISTOX_FULL_AFFINE",      R.string.pref_full_affine_title,       R.string.pref_full_affine_summary,       FALSE ),
    new TDPrefKey( T,       XT,  "DISTOX_WITH_LEVELS",      R.string.pref_with_levels_title,       R.string.pref_with_levels_summary,       TDString.ZERO, R.array.canvasLevels, R.array.canvasLevelsValue ),
    new TDPrefKey( T, BTN,  DR,  "DISTOX_GRAPH_PAPER_SCALE",R.string.pref_graph_paper_scale_title, R.string.pref_graph_paper_scale_summary, TDString.ZERO ),
    new TDPrefKey( T, BOOL, XT,  "DISTOX_SLANT_XSECTION",   R.string.pref_slant_xsection_title,    R.string.pref_slant_xsection_summary,    FALSE ),
    new TDPrefKey( T, LONG, XT,  "DISTOX_OBLIQUE_PROJECTED",R.string.pref_oblique_projection_title,R.string.pref_oblique_projection_summary,TDString.ZERO ),
    new TDPrefKey( T, LONG, DR,  "DISTOX_LINE_ENDS",        R.string.pref_line_ends_title,         R.string.pref_line_ends_summary,         TDString.ZERO )
  };

  /** additional splay settings
   * splay classes
   * splay color: 0 no, 1 yes, 2 discrete - was DISTOX_SPLAY_COLOR
   * splays with clino over mSplayVertThrs are not displayed in plan view
   * where dash-splay are coherent from: 0 no, 1 plan, 2 profile, 3 view
   * splays with clino over this are shown with dashed/dotted line
   * splays off-azimuth over this are shown with dashed/dotted line
   * splays with angle over this are shown with dashed/dotted line
   * color for dash splay 
   * color for dot splay 
   * color for latest splay 
   */ 
  static TDPrefKey[] mGeekSplay = {
    new TDPrefKey( E, BOOL, DT, "DISTOX_SPLAY_CLASSES",       R.string.pref_splay_classes_title,     R.string.pref_splay_classes_summary,     FALSE ),
    new TDPrefKey( T,       UI, "DISTOX_DISCRETE_COLORS",     R.string.pref_splay_color_title,       R.string.pref_splay_color_summary,       TDString.ZERO, R.array.splayColors, R.array.splayColorsValue ),
    new TDPrefKey( A, LONG, UI, "DISTOX_SPLAY_VERT_THRS",     R.string.pref_plan_vthr_title,         R.string.pref_plan_vthr_summary,          "80" ),
    new TDPrefKey( T,       UI, "DISTOX_SPLAY_DASH",          R.string.pref_dash_splay_title,        R.string.pref_dash_splay_message,         TDString.ZERO, R.array.splayDash, R.array.splayDashValue ),
    new TDPrefKey( T, FLT,  UI, "DISTOX_VERT_SPLAY",          R.string.pref_vert_splay_title,        R.string.pref_vert_splay_message,         TDString.FIFTY ),
    new TDPrefKey( T, FLT,  UI, "DISTOX_HORIZ_SPLAY",         R.string.pref_horiz_splay_title,       R.string.pref_horiz_splay_message,        TDString.SIXTY ),
    new TDPrefKey( T, FLT,  UI, "DISTOX_SECTION_SPLAY",       R.string.pref_section_splay_title,     R.string.pref_section_splay_message,      TDString.SIXTY ),
    new TDPrefKey( T, COL,  UI, "DISTOX_SPLAY_DASH_COLOR",    R.string.pref_splay_dash_color_title,  R.string.pref_splay_dash_color_summary,   "7190271"),
    new TDPrefKey( T, COL,  UI, "DISTOX_SPLAY_DOT_COLOR",     R.string.pref_splay_dot_color_title,   R.string.pref_splay_dot_color_summary,    "7190271"), 
    new TDPrefKey( T, COL,  UI, "DISTOX_SPLAY_LATEST_COLOR",  R.string.pref_splay_latest_color_title,R.string.pref_splay_latest_color_summary, "6737151")
  };

  /** 3D viewer settings
   * ...
   */ 
  static TDPrefKey[] mCave3D = {
    new TDPrefKey( N, BOOL, D3, "CAVE3D_NEG_CLINO",          R.string.cpref_neg_clino_title,         R.string.cpref_neg_clino_summary,         FALSE ), 
    new TDPrefKey( N, BOOL, D3, "CAVE3D_STATION_POINTS",     R.string.cpref_station_points_title,    R.string.cpref_station_points_summary,    FALSE ),
    new TDPrefKey( A, LONG, D3, "CAVE3D_STATION_POINT_SIZE", R.string.cpref_station_point_size_title,R.string.cpref_station_point_size_summary,"8"  ),
    new TDPrefKey( A, LONG, D3, "CAVE3D_STATION_TEXT_SIZE",  R.string.cpref_station_text_size_title, R.string.cpref_station_text_size_summary, "20" ),
    new TDPrefKey( A, FLT,  D3, "CAVE3D_SELECTION_RADIUS",   R.string.cpref_selection_radius_title,  R.string.cpref_selection_radius_summary,  "50" ),
    new TDPrefKey( N, BOOL, D3, "CAVE3D_MEASURE_DIALOG",     R.string.cpref_measure_dialog_title,    R.string.cpref_measure_dialog_summary,    FALSE ),
    new TDPrefKey( N, BOOL, D3, "CAVE3D_STATION_TOAST",      R.string.cpref_station_toast_title,     R.string.cpref_station_toast_summary,     FALSE ),
    new TDPrefKey( N, BOOL, D3, "CAVE3D_GRID_ABOVE",         R.string.cpref_grid_above_title,        R.string.cpref_grid_above_summary,        FALSE ),
    new TDPrefKey( A, LONG, D3, "CAVE3D_GRID_EXTENT",        R.string.cpref_grid_extent_title,       R.string.cpref_grid_extent_summary,       "10"  ),
    new TDPrefKey( N, BOOL, D3, "DISTOX_NAMES_VISIBILITY",   R.string.cpref_names_visibility_title,  R.string.cpref_names_visibility_summary,  "2"   ),
    new TDPrefKey( N,       NON, "DISTOX_DEM3D_PREF",         R.string.cpref_dem3d  ),
    new TDPrefKey( A,       NON, "DISTOX_WALLS3D_PREF",       R.string.cpref_walls3d )
  };
 
  /** 3D DEM settings
   * ...
   */
  static TDPrefKey[] mDem3D = {
    new TDPrefKey( N, FLT,  D3, "CAVE3D_DEM_BUFFER",   R.string.cpref_dem_buffer_title,  R.string.cpref_dem_buffer_summary,  "200" ),
    new TDPrefKey( N, LONG, D3, "CAVE3D_DEM_MAXSIZE",  R.string.cpref_dem_maxsize_title, R.string.cpref_dem_maxsize_summary, "400" ),
    new TDPrefKey( N,       D3, "CAVE3D_DEM_REDUCE",   R.string.cpref_dem_reduce_title,  R.string.cpref_dem_reduce_summary,  "1", R.array.demReduce, R.array.demReduceValue ),
    new TDPrefKey( N, STR,  D3, "CAVE3D_TEXTURE_ROOT", R.string.cpref_texture_root_title,R.string.cpref_texture_root_summary,"/sdcard/" )
  };

  /** 3D walls settings
   * ...
   */
  static TDPrefKey[] mWalls3D = {
    new TDPrefKey( N,       D3, "CAVE3D_SPLAY_USE",        R.string.cpref_splay_use_title,       R.string.cpref_splay_use_summary,       "1", R.array.splayUse, R.array.splayUseValue ),
    new TDPrefKey( N, BOOL, D3, "CAVE3D_ALL_SPLAY",        R.string.cpref_all_splay_title,       R.string.cpref_all_splay_summary,       TRUE  ),
    new TDPrefKey( N, BOOL, D3, "CAVE3D_SPLAY_PROJ",       R.string.cpref_splay_proj_title,      R.string.cpref_splay_proj_summary,      FALSE ),
    new TDPrefKey( N, FLT,  D3, "CAVE3D_SPLAY_THR",        R.string.cpref_splay_thr_title,       R.string.cpref_splay_thr_summary,       "0.5" ),
    new TDPrefKey( N, BOOL, D3, "CAVE3D_SPLIT_TRIANGLES",  R.string.cpref_split_triangles_title, R.string.cpref_split_triangles_summary, TRUE  ),
    new TDPrefKey( N, FLT,  D3, "CAVE3D_SPLIT_RANDOM",     R.string.cpref_split_random_title,    R.string.cpref_split_random_summary,    "0.1" ),
    new TDPrefKey( N, FLT,  D3, "CAVE3D_SPLIT_STRETCH",    R.string.cpref_split_stretch_title,   R.string.cpref_split_stretch_summary,   "0.1" ),
    new TDPrefKey( N, FLT,  D3, "CAVE3D_POWERCRUST_DELTA", R.string.cpref_powercrust_delta_title,R.string.cpref_powercrust_delta_summary,"0.1" )
  };

  /** additional settings
   * enable single-back close
   * enable navbar hiding
   * ensble extra palettes
   * whether to use TopoDroid keypads
   * no cursor for custom keyboard
   * enable bulk export
   * enable packet logger
   */ 
  static TDPrefKey[] mGeek = {
    new TDPrefKey( A, BOOL, UI,  "DISTOX_SINGLE_BACK",   R.string.pref_single_back_title,   R.string.pref_single_back_summary,   FALSE ),
    new TDPrefKey( N, BOOL, UI,  "DISTOX_HIDE_NAVBAR",   R.string.pref_hide_navbar_title,   R.string.pref_hide_navbar_summary,   FALSE ),
    new TDPrefKey( T, BOOL, GEN, "DISTOX_PALETTES",      R.string.pref_palettes_title,      R.string.pref_palettes_summary,      FALSE ),
    new TDPrefKey( T, BOOL, UI,  "DISTOX_MKEYBOARD",     R.string.pref_mkeyboard_title,     R.string.pref_mkeyboard_summary,     FALSE ),
    new TDPrefKey( T, BOOL, UI,  "DISTOX_NO_CURSOR",     R.string.pref_no_cursor_title,     R.string.pref_no_cursor_summary,     TRUE  ), 
    new TDPrefKey( T, BOOL, GEN, "DISTOX_BULK_EXPORT",   R.string.pref_bulk_export_title,   R.string.pref_bulk_export_summary,   FALSE ),
    new TDPrefKey( T, BOOL, BT,  "DISTOX_PACKET_LOGGER", R.string.pref_packet_logger_title, R.string.pref_packet_logger_summary, FALSE ),
    new TDPrefKey( T, BOOL, XT,  "DISTOX_TH2_EDIT",      R.string.pref_th2_edit_title,      R.string.pref_th2_edit_summary,      FALSE ),
    new TDPrefKey( E, BTN,  GEN, "DISTOX_GEMINI",          R.string.pref_gemini_title,       R.string.pref_gemini_summary,        TDString.EMPTY ),
    new TDPrefKey( A,       NON, "DISTOX_GEEK_SHOT",     R.string.pref_cat_survey        ),
    new TDPrefKey( T,       NON, "DISTOX_GEEK_SPLAY",    R.string.pref_cat_splay         ),           
    new TDPrefKey( A,       NON, "DISTOX_GEEK_PLOT",     R.string.pref_cat_drawing       ),
    new TDPrefKey( A,       NON, "DISTOX_GEEK_LINE",     R.string.pref_tool_line_title   ),
    new TDPrefKey( A,       NON, "DISTOX_GEEK_DEVICE",   R.string.pref_cat_device        ),        
    new TDPrefKey( T,       NON, "DISTOX_GEEK_IMPORT",   R.string.pref_cat_import_export ),
    new TDPrefKey( D,       NON, "DISTOX_SKETCH_PREF",   R.string.pref_cat_sketch        ),
    new TDPrefKey( D, XTR,  GEN, "DISTOX_WITH_DEBUG",    R.string.pref_with_debug_title,  R.string.pref_with_debug_summary, FALSE ),
  };

  static final int IDX_DEBUG = 16; // index of DISTOX_WITH_DEBUG

  /** additional import settings
   * 
   * 
   * automatically add xsections on export/save
   * whether to add stations to therion th2 exports
   * LRUD down-counter
   * ZIP share app (?)
   */
  static TDPrefKey[] mGeekImport = {
    new TDPrefKey( T, BOOL, XP, "DISTOX_ZIP_WITH_SYMBOLS",  R.string.pref_zipped_symbols_title,    R.string.pref_zipped_symbols_summary,     FALSE ),
    new TDPrefKey( T,       XP, "DISTOX_IMPORT_DATAMODE",   R.string.pref_import_datamode_title,   R.string.pref_import_datamode_summary,    TDString.ZERO, R.array.importDatamode, R.array.importDatamodeValue),
    new TDPrefKey( T, BOOL, XP, "DISTOX_AUTO_XSECTIONS",    R.string.pref_auto_xsections_title,    R.string.pref_auto_xsections_summary,     TRUE  ),
    new TDPrefKey( T, BOOL, XP, "DISTOX_AUTO_STATIONS",     R.string.pref_autoStations_title,      R.string.pref_autoStations_summary,       TRUE  ),
    new TDPrefKey( T, BOOL, XP, "DISTOX_LRUD_COUNT" ,       R.string.pref_lrud_count_title,        R.string.pref_lrud_count_summary,         FALSE ),
    new TDPrefKey( T, BOOL, XT, "DISTOX_ZIP_SHARE_CATEGORY",R.string.pref_zip_share_category_title,R.string.pref_zip_share_category_summary, FALSE )
  };

  /** additional device settings
   * open dialog for BT aliases
   * whether to show the menu to enter te name of a UNNAMED device
   * delay before a socket-connection attempt
   * enable the two-disto survey
   * msec wait after a data/vector packet
   * msec wait after getting "NO PACKET"
   * msec wait after command "laser ON"
   * msec wait after command "take shot"
   * enforce firmware sanity checks
   * 1 prim_only, 3 all, 5 no_index
   *                                                               
   * whether to use the BRIC index as shot ID
   * SAP5 bit-16 bug workaround
   */
  static TDPrefKey[] mGeekDevice = { 
    new TDPrefKey( T, BTN,  BT,  "DISTOX_BT_ALIAS",         R.string.pref_bt_alias_title,         -1,                                     null  ),
    new TDPrefKey( T, BOOL, BT,  "DISTOX_UNNAMED_DEVICE",   R.string.pref_unnamed_device_title,   R.string.pref_unnamed_device_summary,   FALSE ),
    new TDPrefKey( E, LONG, BT,  "DISTOX_SOCKET_DELAY",     R.string.pref_socket_delay_title,     R.string.pref_socket_delay_summary,     TDString.ZERO ),
    new TDPrefKey( T, BOOL, BT,  "DISTOX_SECOND_DISTOX",    R.string.pref_second_distox_title,    R.string.pref_second_distox_summary,    FALSE ),
    new TDPrefKey( A, LONG, BT,  "DISTOX_WAIT_DATA",        R.string.pref_wait_data_title,        R.string.pref_wait_data_summary,        "250" ),
    new TDPrefKey( A, LONG, BT,  "DISTOX_WAIT_CONN",        R.string.pref_wait_conn_title,        R.string.pref_wait_conn_summary,        "500" ),
    new TDPrefKey( A, LONG, BT,  "DISTOX_WAIT_LASER",       R.string.pref_wait_laser_title,       R.string.pref_wait_laser_summary,       "2000" ),
    new TDPrefKey( A, LONG, BT,  "DISTOX_WAIT_SHOT",        R.string.pref_wait_shot_title,        R.string.pref_wait_shot_summary,        "2000" ),
    new TDPrefKey( T, BOOL, GEN, "DISTOX_FIRMWARE_SANITY",  R.string.pref_firmware_sanity_title,  R.string.pref_firmware_sanity_summary,  TRUE ),
    new TDPrefKey( T,       BT,  "DISTOX_BRIC_MODE",        R.string.pref_bric_mode_title,        R.string.pref_bric_mode_summary,        TDString.THREE, R.array.bricMode, R.array.bricModeValue ),
    new TDPrefKey( N, BOOL, BT,  "DISTOX_BRIC_ZERO_LENGTH", R.string.pref_bric_zero_length_title, R.string.pref_bric_zero_length_summary, FALSE ),
    new TDPrefKey( T, BOOL, BT,  "DISTOX_BRIC_INDEX_IS_ID", R.string.pref_bric_index_is_id_title, R.string.pref_bric_index_is_id_summary, FALSE ),
    new TDPrefKey( T, BOOL, BT,  "DISTOX_SAP5_BIT16_BUG",   R.string.pref_sap5_bit16_bug_title,   R.string.pref_sap5_bit16_bug_summary,   TRUE )
  };

  /** 3D sketch settings
   *
   *
   */
  static TDPrefKey[] mSketch = {
    new TDPrefKey( D, BOOL, XT, "DISTOX_3D_SKETCH",          R.string.pref_3d_sketch_title,           R.string.pref_3d_sketch_summary,          FALSE ),
    new TDPrefKey( D, FLT,  XT, "DISTOX_SKETCH_SPLAY_BUFFER",R.string.pref_sketch_splay_buffer_title, R.string.pref_sketch_splay_buffer_summary,TDString.TWO )
  };

  static TDPrefKey[] mGeekSketch = {
  };

  // the order must follow category index in TDPrefCat
  static TDPrefKey[][] mKeySet = {
    mMain,   // 0
    mSurvey,
    mPlot,
    mCalib,
    mDevice, 
    mSketch, // 5
    mData,
    mUnits,
    mAccuracy,
    mLocation, 
    mScreen,  // 10
    mLine,
    mPoint,   // 12
    mDraw,
    mErase,
    mEdit,     // 15
    mCave3D,
    mDem3D,
    mWalls3D,
    mGeek,     // 19
    mGeekShot, // 20
    mGeekSplay,
    mGeekPlot,
    mGeekLine,
    mGeekDevice,
    mGeekImport,  // 25
    mGeekSketch,
    mExport,
    mExportImport,
    mExportEnable,
    mExportSvx, // 30
    mExportTh,  // 31
    mExportDat,
    mExportCsx,
    mExportTro,
    mExportSvg,
    mExportShp, // 36
    mExportDxf,
    mExportGpx,
    mExportKml,
    mExportCsv,
    mExportSrv, // 41
    mExportPly,
    null
  };

  static {
    int cat = 0;
    for ( TDPrefKey[] keyset : mKeySet ) {
      if ( keyset == null ) break;
      for ( TDPrefKey k : keyset ) {
        k.cat = cat;
      }
      ++ cat;
    }
    if ( TDLevel.isDebugBuild() ) {
      for ( TDPrefKey k : mGeek ) {
        if ( k.key.equals( "DISTOX_WITH_DEBUG" ) ) k.level = T;
      }
    }
  }
      

  /** @return the list of TDPrefCat categpries that match a givem setting description
   * @param kay   input key
   */
  static List<Integer> match( String kay )
  {
    ArrayList< Integer > res = new ArrayList<>();
    // key = key.replaceAll( "*", "\\*" );
    Pattern p = Pattern.compile( kay.toUpperCase(Locale.US) );
    // int cat = 0;
    for ( TDPrefKey[] keyset : mKeySet ) {
      if ( keyset == null ) continue;
      for ( TDPrefKey k : keyset ) {
        Matcher m = p.matcher( TDInstance.getResourceString( k.summary ).toUpperCase(Locale.US) );
        if ( m.find() ) {
          // TDLog.v("matched category " + cat + " titles " + section.length );
          res.add( k.cat ); // add matching category
          break;
        } else {
          // TDLog.v("not matched " + str );
        }
      }
      // ++ cat;
    }
    // TDLog.v("found " + res.size() + " matches");
    // for ( Integer i : res ) {
    //   cat = i.intValue();
    //   TDLog.v("match " + cat + " " + TDInstance.getResourceString( TDPrefCat.mTitleRes[ cat ] ) );
    // }
    return res;
  }

  // /** @return the group of a setting (-1 if not found)
  //  * @param kay   seting key
  //  */
  // static int getKeyGroup( String kay )
  // {
  //   for ( TDPrefKey[] keyset : mKeySet ) {
  //     if ( keyset == null ) continue;
  //     for ( TDPrefKey k : keyset ) {
  //       if ( k.key.equals( kay ) ) return k.group;
  //     }
  //   }
  //   return -1;
  // }


  /** @return true the group of the setting is in the groups flag
   * @param kay   seting key
   * @param flag  groups flag
   */
  static boolean checkKeyGroup( String kay, int flag )
  {
    for ( TDPrefKey[] keyset : mKeySet ) {
      if ( keyset == null ) continue;
      for ( TDPrefKey k : keyset ) {
        if ( k.key.equals( kay ) ) {
          return ( flag & (1<<k.group) ) != 0;
        }
      }
    }
    return false;
  }
 
  /** @return the number of key-sets
   */
  static int nrKeySets() { return mKeySet.length; }
  
  /** @return the number of keys in a key-sets
   * @param j   index of the keyset
   */
  static int nrKeys( int j ) { return ( mKeySet[j] == null )? 0 : mKeySet[j].length; }

  /** @return a key
   * @param j   index of keyset
   * @param k   index of the key
   */
  static TDPrefKey getKey( int j, int k ) { return mKeySet[j][k]; }

  /** @return if the key is a repeatition
   * @param j   index of keyset
   * @param k   index of the key
   * @note repeated keys are not exported
   */
  static boolean repeatedKey( int j, int k ) 
  {
    return ( j ==  0 && k == 7 ) // DISTOX_GEMINI is not exported
        || ( j ==  3 && k > 5 )  // mCalib : not repeated but not to export
        || ( j == 11 && k < 7 )  // mLine
        || ( j == 12 && k < 3 )  // mPoint
        || ( j == 14 )           // mErase
        || ( j == 15 )           // mEdit
        || ( j == 29 && k < 2 )  // mExportSvx
        || ( j == 32 && k < 1 ); // mExportCsx
  }
}
