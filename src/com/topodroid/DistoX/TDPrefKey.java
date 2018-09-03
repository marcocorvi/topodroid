/* @file TDPrefKey.java
 *
 * @author marco corvi
 * @date aug 2018
 *
 * @brief TopoDroid options keys
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class TDPrefKey
{
  static final String FALSE = "false";
  static final String TRUE  = "true";

  static String[] MAIN = { // [14]
    "DISTOX_CWD",           // 0 // CWD must be the last of primary preferences
    "DISTOX_TEXT_SIZE",     // 1 * // size of tetx [pt]
    "DISTOX_SIZE_BUTTONS",  // 2 * // size of buttons (S, N, M, L, XL)
    "DISTOX_EXTRA_BUTTONS", // 3 * // Activity Level
    "DISTOX_MKEYBOARD",     // 4 * // whether to use TopoDroid keypads
    "DISTOX_NO_CURSOR",     // 5 * // no cursor for custom keyboard
    "DISTOX_LOCAL_MAN",     // 6 * // enable local man pages
    "DISTOX_COSURVEY",      // 7 * // whether to enable co-surveying
    "DISTOX_LOCALE",        // 8 * // language
    "DISTOX_EXPORT_PREF",   // 9  FORWARD
    "DISTOX_SURVEY_PREF",   //    FORWARD
    "DISTOX_PLOT_PREF",     //    FORWARD
    "DISTOX_SKETCH_PREF",   //    FORWARD
    "DISTOX_DEVICE_PREF",   // 13 FORWARD
    null
  };

  static int[] MAINtitle = {
    R.string.pref_cwd_title,
    R.string.pref_text_size_title,
    R.string.pref_size_buttons_title,
    R.string.pref_extra_buttons_title,
    R.string.pref_mkeyboard_title,
    R.string.pref_no_cursor_title,
    R.string.pref_local_help_title,
    R.string.pref_cosurvey_title,
    R.string.pref_locale_title,
    R.string.pref_cat_import_export,
    R.string.pref_cat_survey,
    R.string.pref_cat_drawing,
    R.string.pref_cat_sketch,
    R.string.pref_cat_device
  };

  static int[] MAINdesc = {
    -1,
    R.string.pref_text_size_summary,
    R.string.pref_size_buttons_summary,
    R.string.pref_extra_buttons_summary,
    R.string.pref_mkeyboard_summary,
    R.string.pref_no_cursor_summary,
    R.string.pref_local_help_summary,
    R.string.pref_cosurvey_summary,
    R.string.pref_locale_summary,
    -1,
    -1,
    -1,
    -1,
    -1
  };

  static String[] MAINdef = {
     "TopoDroid",
     "0",
     "1",
     "1",
     TRUE,
     FALSE,
     "0",
     FALSE,
     "",
     "",
     "",
     "",
     ""
  };

  // ----------------------------------------------------------------------------------------------------

  static String[] SURVEY = { // [11] 
    "DISTOX_TEAM",              // 0 * // default team
    "DISTOX_SURVEY_STATION",    // 1   // DISTOX_SURVEY_STATIONS must not be used
    "DISTOX_STATION_NAMES", // station names: 0 alphanumeric, 1 numbers
    "DISTOX_INIT_STATION",      // 3 * // default initial station name
    "DISTOX_THUMBNAIL",                // size of photo thumbnails
    "DISTOX_DATA_BACKUP", //
    "DISTOX_SHARED_XSECTIONS",  // 6 // whether at-station X-sections are shared among plots
    "DISTOX_SHOT_UNITS_SCREEN", // 7  FORWARD
    "DISTOX_SHOT_DATA_SCREEN",  //    FORWARD
    "DISTOX_LOCATION_SCREEN",   //    FORWARD
    "DISTOX_ACCURACY_SCREEN",   // 10 FORWARD
    null
  };

  static int[] SURVEYtitle = {
    R.string.pref_team_title,           
    R.string.pref_survey_stations_title,
    R.string.pref_station_names_title,
    R.string.pref_init_station_title,
    R.string.pref_thumbnail_title,
    R.string.pref_data_backup_title,
    R.string.pref_shared_xsections_title,
    R.string.pref_shot_units_title,
    R.string.pref_shot_data_title,
    R.string.pref_location_title,
    R.string.pref_accuracy_title
  };

  static int[] SURVEYdesc = {
    R.string.pref_team_summary,  
    R.string.pref_survey_stations_summary,
    R.string.pref_station_names_summary,
    R.string.pref_init_station_summary,
    R.string.pref_thumbnail_summary,  
    R.string.pref_data_backup_summary,
    R.string.pref_shared_xsections_summary,
    -1,
    -1,
    -1,
    -1 
  };

  static String[] SURVEYdef = {
    "",
    "1",
    "alpha",
    "0",
    "200",
    FALSE,
    FALSE,
    "",
    "",
    "",
    ""
  };
  // ------------------------------------------------------------------------------

  static String[] PLOT = { // [12]
    "DISTOX_PICKER_TYPE",    // 0 // tool picker: most-recent, list, grid, triple-grid
    "DISTOX_RECENT_NR",           // number of most recent items (item picker)
    "DISTOX_SIDE_DRAG",           // whether to enable side-drag
    "DISTOX_ZOOM_CTRL",           // whether to have zoom-ctrl (no, temp., always)
    // "DISTOX_SECTION_STATIONS",
    "DISTOX_CHECK_ATTACHED", // 4 // whether to check all shots are connected
    "DISTOX_CHECK_EXTEND",        // whether leg extends are all set
    "DISTOX_BACKUP_NUMBER",       // number of plot backups
    "DISTOX_BACKUP_INTERVAL",// 7 // minimum interval between plot backups [60 s]
    "DISTOX_TOOL_POINT",     // 8  FORWARD
    "DISTOX_TOOL_LINE",      //    FORWARD
    "DISTOX_PLOT_SCREEN",    //    FORWARD
    "DISTOX_PLOT_WALLS",     // 11 FORWARD
    null
  };

  static int[] PLOTtitle = {
    R.string.pref_picker_type_title,
    R.string.pref_recent_nr_title,
    R.string.pref_side_drag_title,
    R.string.pref_zoom_controls_title,
    // R.string.pref_section_stations_title,
    R.string.pref_checkAttached_title,
    R.string.pref_checkExtend_title,
    R.string.pref_backup_number_title,
    R.string.pref_backup_interval_title,
    R.string.pref_tool_point_title,
    R.string.pref_tool_line_title,
    R.string.pref_plot_screen_title,
    R.string.pref_plot_walls_title
  };

  static int[] PLOTdesc = {
    R.string.pref_picker_type_summary,
    R.string.pref_recent_nr_summary,
    R.string.pref_side_drag_summary,
    R.string.pref_zoom_controls_summary,
    // R.string.pref_section_stations_summary,
    R.string.pref_checkAttached_summary,
    R.string.pref_checkExtend_summary,
    R.string.pref_backup_number_summary,
    R.string.pref_backup_interval_summary,
    -1,
    -1,
    -1,
    -1
  };

  static String[] PLOTdef = {
    "0", 
    "4", 
    FALSE, 
    "1",
    // "3",
    FALSE,
    TRUE,
    "5",
    "60",
    "",
    "",
    "",
    ""
  };

  // ------------------------------------------------------------------------------
  static String[] CALIB = { // [11]
    "DISTOX_GROUP_BY",       // 0 // calib data grouping policy
    "DISTOX_GROUP_DISTANCE",      // calib data grouping by the distance threshold
    "DISTOX_CALIB_EPS",           // calib computation param EPS
    "DISTOX_CALIB_MAX_IT",   // 3 // calib computation maximun number of iterations
    "DISTOX_CALIB_SHOT_DOWNLOAD", // remote calib data immediately downloaded
    // "DISTOX_RAW_DATA",
    "DISTOX_RAW_CDATA",      // 5 // whether to display also raw calib data
    "DISTOX_CALIB_ALGO",          // calib algo [auto, linear, non-linear]
    "DISTOX_ALGO_MIN_ALPHA",      // min-algo params: alpha
    "DISTOX_ALGO_MIN_BETA",       //                  beta
    "DISTOX_ALGO_MIN_GAMMA",      //                  gamma
    "DISTOX_ALGO_MIN_DELTA", // 10 //                 delta
    null
  };

  static int[] CALIBtitle = {
    R.string.pref_group_by_title,
    R.string.pref_group_title,
    R.string.pref_error_title,
    R.string.pref_iter_title,
    R.string.pref_calib_shot_download_title,
    // R.string.pref_raw_data_title,
    R.string.pref_raw_data_title,
    R.string.pref_calib_algo_title,
    R.string.pref_algo_min_alpha_title,
    R.string.pref_algo_min_beta_title, 
    R.string.pref_algo_min_gamma_title,
    R.string.pref_algo_min_delta_title
  };

  static int[] CALIBdesc = {
    R.string.pref_group_by_summary,
    R.string.pref_group_summary,
    R.string.pref_error_summary,
    R.string.pref_iter_summary,
    R.string.pref_calib_shot_download_summary,
    // R.string.pref_raw_data_summary,
    R.string.pref_raw_data_summary,
    R.string.pref_calib_algo_summary,
    R.string.pref_algo_min_alpha_summary,
    R.string.pref_algo_min_beta_summary,
    R.string.pref_algo_min_gamma_summary,
    R.string.pref_algo_min_delta_summary
  };

  static String[] CALIBdef = {
    "1",
    "40",
    "0.000001",
    "200",
    TRUE,
    // FALSE,
    "0",
    "0",
    "0.1",
    "4.0",
    "1.0",
    "1.0"
  };

  // ------------------------------------------------------------------------------
  static String[] DEVICE = { // [13]
    // "DISTOX_DEVICE", // N.B. indexKeyDeviceName - USED by TopoDroidApp to store the device
    // "DISTOX_DEVICE_TYPE",
    "DISTOX_BLUETOOTH",       // 0 * // whether to check bluetooth on start
    "DISTOX_CONN_MODE",              // data download mode [on-demand, continuous]
    "DISTOX_AUTO_RECONNECT",         // whether to try to reconnect if DistoX is lost [continuos mode]
    "DISTOX_HEAD_TAIL",       // 3 // whether to read head-tail to get the number of packets to download
    "DISTOX_SOCK_TYPE",            // socket type
    // "DISTOX_COMM_RETRY",        // number of socket connection attempts
    "DISTOX_Z6_WORKAROUND",   // 5 // whether to enable Z6 workaround
    "DISTOX_SOCKET_DELAY",         // delay before a socket-connection attempt
    "DISTOX_AUTO_PAIR",       // 7 // whether to auto-pair the discovered DistoX
    "DISTOX_WAIT_DATA",       // 8 // msec wait after a data/vector packet
    "DISTOX_WAIT_CONN",            // msec wait after getting "NO PACKET"
    "DISTOX_WAIT_LASER",           // msec wait after command "laser ON"
    "DISTOX_WAIT_SHOT",       // 11 // msec wait after command "take shot"
    "DISTOX_CALIB_PREF",      // 12 FORWARD
    null
  };

  static int[] DEVICEtitle = {
    // R.string.pref_device_title,
    // R.string.pref_device_type_title,
    R.string.pref_checkBT_title,
    R.string.pref_conn_mode_title,
    R.string.pref_auto_reconnect_title,
    R.string.pref_head_tail_title,
    R.string.pref_sock_type_title,
    // R.string.pref_comm_retry_title,
    R.string.pref_z6_workaround_title,
    R.string.pref_socket_delay_title,
    R.string.pref_auto_pair_title,
    R.string.pref_wait_data_title,
    R.string.pref_wait_conn_title,
    R.string.pref_wait_laser_title,
    R.string.pref_wait_shot_title,
    R.string.pref_cat_calib
  };

  static int[] DEVICEdesc = {
    // R.string.pref_device_summary,
    // R.string.pref_device_type_summary,
    R.string.pref_checkBT_summary,
    R.string.pref_conn_mode_summary,
    R.string.pref_auto_reconnect_summary,
    R.string.pref_head_tail_summary,
    R.string.pref_sock_type_summary,
    // R.string.pref_comm_retry_summary,
    R.string.pref_z6_workaround_summary,
    R.string.pref_socket_delay_summary,
    R.string.pref_auto_pair_summary,
    R.string.pref_wait_data_summary,
    R.string.pref_wait_conn_summary,
    R.string.pref_wait_laser_summary,
    R.string.pref_wait_shot_summary,
    -1
  };

  static String[] DEVICEdef = {
    // "",
    // "1",
    "1",
    "0",
    FALSE,
    FALSE,
    "0",
    // "1",
    TRUE,
    "0",
    TRUE,
    "250",
    "500",
    "1000",
    "4000",
    ""
  };


  // ------------------------------------------------------------------------------
  static String[] EXPORT = { // [31]
    "DISTOX_PT_CMAP",         // 0
    "DISTOX_SPLAY_EXTEND",         // whether to set L/R extend to LRUD splay shots (Compass, VTopo import)
    "DISTOX_EXPORT_SHOTS",    // 2 // default data export
    "DISTOX_EXPORT_PLOT",          // default plot export
    "DISTOX_THERION_MAPS",         // whether to put map commands before centerline in therion
    "DISTOX_AUTO_STATIONS",   // 5 // whether to add stations to thetion th2 exports
    // "DISTOX_XTHERION_AREAS",    // save areas a-la xtherion
    "DISTOX_THERION_SPLAYS",  // 6 // whether to add u:splay lines to Therion th2 export
    "DISTOX_STATION_PREFIX",       // whether to add cave-name prefix to stations (cSurvey/Compass export)
    "DISTOX_COMPASS_SPLAYS",       // whether to add splays to Compass dat export
    "DISTOX_ORTHO_LRUD",      // 9 // orthogonal LRUD ( >=1 disable, min 0 )
    "DISTOX_LRUD_VERTICAL", 
    "DISTOX_LRUD_HORIZONTAL", // 11 
    "DISTOX_SWAP_LR",               // whether to swap Left-Right in Compass export
    "DISTOX_SURVEX_EOL",            // survex end of line [either Linux or Windows]
    "DISTOX_SURVEX_SPLAY",    // 14 // whether to name endpoint of splays in Survex export
    "DISTOX_SURVEX_LRUD",           // whether to add LRUD to Survex export
    "DISTOX_BEZIER_STEP",           // max step between interpolating points for bezier in export (cSurvey)
    "DISTOX_SVG_GRID",        // 17 // whether to export grid in SVG 
    "DISTOX_SVG_LINE_DIR",          // whether to add line orientation ticks in SVG export
    // "DISTOX_SVG_IN_HTML",        // whether to export SVG embedded in HTML
    "DISTOX_SVG_POINT_STROKE", // 19 
    "DISTOX_SVG_LABEL_STROKE", 
    "DISTOX_SVG_LINE_STROKE", 
    "DISTOX_SVG_GRID_STROKE",  // 22 
    "DISTOX_SVG_SHOT_STROKE", 
    "DISTOX_SVG_LINEDIR_STROKE", 
    "DISTOX_KML_STATIONS",     // 25 // whether to add station points to KML export
    "DISTOX_KML_SPLAYS",             // whether to add splay lines to KML export
    "DISTOX_BITMAP_SCALE",           // default bitmap scale PNG
    "DISTOX_BITMAP_BGCOLOR",   // 28 // bitmap background color [RGB]
    // "DISTOX_DXF_SCALE",           // default DXF scale (export)
    "DISTOX_DXF_BLOCKS",             // whether to export point items as Blocks in DXF export
    "DISTOX_ACAD_VERSION",     // 30 
    null
  };

  static int[] EXPORTtitle = {
    R.string.pref_pt_color_map_title,
    R.string.pref_LRExtend_title,
    R.string.pref_export_shots_title,
    R.string.pref_export_plot_title,
    R.string.pref_therion_maps_title,
    R.string.pref_autoStations_title,
    // R.string.pref_xtherion_areas_title,
    R.string.pref_therion_splays_title,
    R.string.pref_station_prefix_title,
    R.string.pref_compass_splays_title,
    R.string.pref_ortho_lrud_title,
    R.string.pref_lrud_vertical_title,
    R.string.pref_lrud_horizontal_title,
    R.string.pref_swapLR_title,
    R.string.pref_survex_eol_title,
    R.string.pref_survex_splay_title,
    R.string.pref_survex_lrud_title,
    R.string.pref_bezier_step_title,
    R.string.pref_svg_grid_title,
    R.string.pref_svg_line_dir_title,
    // R.string.pref_svg_in_html_title,
    R.string.pref_svg_pointstroke_title,
    R.string.pref_svg_labelstroke_title,
    R.string.pref_svg_linestroke_title,
    R.string.pref_svg_gridstroke_title,
    R.string.pref_svg_shotstroke_title,
    R.string.pref_svg_linedirstroke_title,
    R.string.pref_kml_stations_title,
    R.string.pref_kml_splays_title,
    R.string.pref_bitmap_scale_title,
    R.string.pref_bitmap_bgcolor_title,
    // R.string.pref_dxf_scale_title,
    R.string.pref_dxf_blocks_title,
    R.string.pref_acad_version_title,
  };

  static int[] EXPORTdesc = {
    R.string.pref_pt_color_map_summary,
    R.string.pref_LRExtend_summary,
    R.string.pref_export_shots_summary,
    R.string.pref_export_plot_summary,
    R.string.pref_therion_maps_summary,
    R.string.pref_autoStations_summary,
    // R.string.pref_xtherion_areas_summary,
    R.string.pref_therion_splays_summary,
    R.string.pref_station_prefix_summary,
    R.string.pref_compass_splays_summary,
    R.string.pref_ortho_lrud_summary,
    R.string.pref_lrud_vertical_summary,
    R.string.pref_lrud_horizontal_summary,
    R.string.pref_swapLR_summary,
    R.string.pref_survex_eol_summary,
    R.string.pref_survex_splay_summary,
    R.string.pref_survex_lrud_summary,
    R.string.pref_bezier_step_summary,
    R.string.pref_svg_grid_summary,
    R.string.pref_svg_line_dir_summary,
    // R.string.pref_svg_in_html_summary,
    R.string.pref_svg_pointstroke_summary,
    R.string.pref_svg_labelstroke_summary,
    R.string.pref_svg_linestroke_summary,
    R.string.pref_svg_gridstroke_summary,
    R.string.pref_svg_shotstroke_summary,
    R.string.pref_svg_linedirstroke_summary,
    R.string.pref_kml_stations_summary,
    R.string.pref_kml_splays_summary,
    R.string.pref_bitmap_scale_summary,
    R.string.pref_bitmap_bgcolor_summary,
    // R.string.pref_dxf_scale_summary,
    R.string.pref_dxf_blocks_summary,
    R.string.pref_acad_version_summary
  };

  static String[] EXPORTdef = {
    "",
    TRUE,
    "none",
    "none",
    FALSE,
    TRUE,
    // FALSE,
    FALSE,
    FALSE,
    TRUE,
    "0",
    "0",
    "90",
    FALSE,
    "lf",
    FALSE,
    FALSE,
    "0.2",
    FALSE,
    FALSE,
    // FALSE,
    "0.1",
    "0.3",
    "0.5", 
    "0.5",
    "0.5",
    "2.0",
    TRUE,
    FALSE,
    "1.5",
    "0 0 0",
    // "1.0",
    TRUE,
    "9"
  };

  // ------------------------------------------------------------------------------
  static String[] DATA = { // [14]
    "DISTOX_CLOSE_DISTANCE",  // 0 // tolerance among leg shots [%]
    "DISTOX_MAX_SHOT_LENGTH",      // maximum length of a shot data
    "DISTOX_MIN_LEG_LENGTH",       // minimum length of a shot data
    "DISTOX_LEG_SHOTS",       // 3 // nr. of shots to make a leg [2, 3, 4]
    "DISTOX_RECENT_TIMEOUT",       // recent block timeout
    "DISTOX_BACKSHOT",             // using DistoX in backshot mode
    "DISTOX_EXTEND_THR2",     // 6 // half angle around 90 where splays have "vert" extend
    "DISTOX_VTHRESHOLD",           // if shot clino is above, LRUD are horizontal
    "DISTOX_AZIMUTH_MANUAL",  // 8 * // whether the "extend" is fixed L or R, selected by hand 
    "DISTOX_LOOP_CLOSURE_VALUE",     // whether to close loop
    "DISTOX_PREV_NEXT",       // 10  // whether to put "prev-next" arrows in shot edit dialog
    "DISTOX_BACKSIGHT", // whether to add backsight fields in manual shot input dialog
    // "DISTOX_MAG_ANOMALY",        // whether to compensate magnetic anomaly
    "DISTOX_SHOT_TIMER",      // 12 // bearing-clino timer [1/10 s]
    "DISTOX_BEEP_VOLUME",     // 13 // bearing-clino beep volume [%]
    null
  };

  static int[] DATAtitle = {
    R.string.pref_leg_title,
    R.string.pref_max_shot_title,
    R.string.pref_min_leg_title,
    R.string.pref_leg_shots_title,
    R.string.pref_recent_timeout_title,
    R.string.pref_backshot_title,
    R.string.pref_ethr_title,
    R.string.pref_vthr_title,
    R.string.pref_azimuth_manual_title,
    R.string.pref_loopClosure_title,
    R.string.pref_prev_next_title,
    R.string.pref_backsight_title,
    // R.string.pref_mag_anomaly_title,
    R.string.pref_shot_timer_title,
    R.string.pref_beep_volume_title
  };

  static int[] DATAdesc = {
    R.string.pref_leg_summary,
    R.string.pref_max_shot_summary,
    R.string.pref_min_leg_summary,
    R.string.pref_leg_shots_summary,
    R.string.pref_recent_timeout_summary,
    R.string.pref_backshot_summary,
    R.string.pref_ethr_summary,
    R.string.pref_vthr_summary,
    R.string.pref_azimuth_manual_summary,
    R.string.pref_loopClosure_summary,
    R.string.pref_prev_next_summary,
    R.string.pref_backsight_summary,
    // R.string.pref_mag_anomaly_summary,
    R.string.pref_shot_timer_summary,
    R.string.pref_beep_volume_summary
  };

  static String[] DATAdef = {
    "0.05",
    "50",
    "0.5",
    "3",
    "30",
    FALSE,
    "10",
    "80",
    FALSE, 
    "0",
    TRUE,
    FALSE,
    // FALSE,
    "10",
    "50"
  };

  // ------------------------------------------------------------------------------
  static String[] UNITS = { // [3]
    "DISTOX_UNIT_LENGTH",   // units of lengths [m, y, ft]
    "DISTOX_UNIT_ANGLE",    // units of angles [deg, grad]
    "DISTOX_UNIT_GRID", // plot grid unit [m, y, 2ft]
    null
  };

  static int[] UNITStitle = {
    R.string.pref_unit_length_title,
    R.string.pref_unit_angle_title,
    R.string.pref_unit_grid_title
  };

  static int[] UNITSdesc = {
    R.string.pref_unit_length_summary,
    R.string.pref_unit_angle_summary,
    R.string.pref_unit_grid_summary
  };

  static String[] UNITSdef = {
    "meters",
    "degrees",
    "1.0"
  };

  static int[] UNITSarr = {
    R.array.unitLength,
    R.array.unitAngle,
    R.array.unitGrid 
  };

  static int[] UNITSval = {
    R.array.unitLengthValue,
    R.array.unitAngleValue,
    R.array.unitGridValue
  };

  // ------------------------------------------------------------------------------
  static String[] ACCURACY = { // [3]
    "DISTOX_ACCEL_PERCENT", // shot quality G threshold [%]
    "DISTOX_MAG_PERCENT",   // shot quality M threhsold [%]
    "DISTOX_DIP_THR",       // shot qualoity dip threshold [deg]
    null
  };

  static int[] ACCURACYtitle = {
    R.string.pref_accel_thr_title,
    R.string.pref_mag_thr_title,
    R.string.pref_dip_thr_title
  };

  static int[] ACCURACYdesc = {
    R.string.pref_accel_thr_summary,
    R.string.pref_mag_thr_summary,
    R.string.pref_dip_thr_summary
  };

  static String[] ACCURACYdef = {
    "1.0",
    "1.0",
    "2.0"
  };


  // ------------------------------------------------------------------------------
  static String[] LOCATION = { // [2]
    "DISTOX_UNIT_LOCATION", // units of location [ddmmss dec.deg]
    "DISTOX_CRS",           // default C.R.S.
    null
  };

  static int[] LOCATIONtitle = {
    R.string.pref_unit_location_title,
    R.string.pref_crs_title
  };

  static int[] LOCATIONdesc = {
    R.string.pref_unit_location_summary,
    R.string.pref_crs_summary
  };

  static String[] LOCATIONdef = {
    "ddmmss",
    "Long-Lat"
  };

  // ------------------------------------------------------------------------------
  static String[] SCREEN = { // [13]
    "DISTOX_FIXED_THICKNESS", // 0 // thickness of fixed lines
    "DISTOX_STATION_SIZE",         // size of station names [pt]
    "DISTOX_DOT_RADIUS",           // radius of green dots
    "DISTOX_CLOSENESS",       // 3 // "select" radius // "select" radius // "select" radius
    "DISTOX_ERASENESS",            // "erase" radius // "erase" radius
    "DISTOX_MIN_SHIFT",            // maximum amount for a shift (to avoid jumps)
    "DISTOX_POINTING",        // 6 // "size" of a "point touch" (max distance between down and up)
    "DISTOX_SPLAY_VERT_THRS",      // splays with clino over mSplayVertThrs are not displayed in plan view
    "DISTOX_DASH_SPLAY",           // whether dash-splay are coherent between plan and profile
    "DISTOX_VERT_SPLAY",      // 9 // splays with clino over this are shown with dashed/dotted line
    "DISTOX_HORIZ_SPLAY",          // splays off-azimuth over this are shown with dashed/dotted line
    "DISTOX_SECTION_SPLAY",        // splays with angle over this are shown with dashed/dotted line
    "DISTOX_HTHRESHOLD",      // 12 // if clino is over thr, H_SECTION is horizontal (has north arrow)
    null
  };

  static int[] SCREENtitle = {
    R.string.pref_fixed_thickness_title,
    R.string.pref_station_size_title,
    R.string.pref_dot_radius_title,
    R.string.pref_closeness_title,
    R.string.pref_eraseness_title,
    R.string.pref_min_shift_title,
    R.string.pref_pointing_title,
    R.string.pref_vthr_title,
    R.string.pref_dash_splay_title,
    R.string.pref_vert_splay_title,
    R.string.pref_horiz_splay_title,
    R.string.pref_section_splay_title,
    R.string.pref_hthr_title
  };

  static int[] SCREENdesc = {
    R.string.pref_fixed_thickness_summary,
    R.string.pref_station_size_summary,
    R.string.pref_dot_radius_message,
    R.string.pref_closeness_message,
    R.string.pref_eraseness_message,
    R.string.pref_min_shift_message,
    R.string.pref_pointing_message, 
    R.string.pref_vthr_summary,      
    R.string.pref_dash_splay_message, 
    R.string.pref_vert_splay_message,  
    R.string.pref_horiz_splay_message,  
    R.string.pref_section_splay_message, 
    R.string.pref_hthr_summary
  };

  static String[] SCREENdef = {
    "1",
    "20",
    "5", 
    "24",
    "36",
    "60",
    "24",
    "80",
    TRUE,
    "50",
    "60",
    "60",
    "70"
  };


  // ------------------------------------------------------------------------------
  static String[] LINE = { // [10]
    "DISTOX_LINE_THICKNESS",  // 0 // thickness of normal lines (walls are twice)
    "DISTOX_LINE_STYLE",           // line style: 0 bezier, 1 fine, 2 normal, 3 coarse
    "DISTOX_LINE_SEGMENT",         // minimum distance between consecutive points on a line
    "DISTOX_ARROW_LENGTH",    // 3 // length of the tick at the first line-point (when applicable)
    "DISTOX_REDUCE_ANGLE",         // "rock" reducing lines: maximal angle
    "DISTOX_AUTO_SECTION_PT",      // whether to add section point when tracing a section line
    "DISTOX_LINE_CONTINUE",   // 6 // default line continuation set
    "DISTOX_AREA_BORDER",          // area border visibility
    "DISTOX_LINE_ACCURACY",        // Bezier interpolator param:
    "DISTOX_LINE_CORNER",     // 9 // Bezier interpolator param:
    null
  };

  static int[] LINEtitle = {
    R.string.pref_line_thickness_title,
    R.string.pref_linestyle_title,
    R.string.pref_segment_title,
    R.string.pref_arrow_length_title,
    R.string.pref_reduce_angle_title, 
    R.string.pref_auto_section_pt_title,
    R.string.pref_linecontinue_title,
    R.string.pref_area_border_title,
    R.string.pref_lineacc_title, 
    R.string.pref_linecorner_title
  };

  static int[] LINEdesc = {
    R.string.pref_line_thickness_summary,
    R.string.pref_linestyle_summary,
    R.string.pref_segment_message,
    R.string.pref_arrow_length_message,
    R.string.pref_reduce_angle_summary,
    R.string.pref_auto_section_pt_summary,
    R.string.pref_linecontinue_summary,
    R.string.pref_area_border_summary,
    R.string.pref_lineacc_summary,
    R.string.pref_linecorner_summary
  };

  static String[] LINEdef = {
    "1",
    "2",
    "10",
    "8",
    "45",
    FALSE,
    "0",
    TRUE,
    "1.0",
    "20.0"
  };

  // ------------------------------------------------------------------------------
  static String[] POINT = { // [3]
    "DISTOX_UNSCALED_POINTS", // 0 // whether drawing point items should stay unscaled when zooming
    "DISTOX_DRAWING_UNIT",    // 1 // plot unit
    "DISTOX_LABEL_SIZE",      // 2 // size of labels [pt]
    null
  };

  static int[] POINTtitle = {
    R.string.pref_unscaled_points_title,
    R.string.pref_drawing_unit_title,
    R.string.pref_label_size_title
  };

  static int[] POINTdesc = {
    R.string.pref_unscaled_points_summary,
    R.string.pref_drawing_unit_summary,
    R.string.pref_label_size_summary
  };

  static String[] POINTdef = {
    FALSE,
    "1.2",
    "24"
  };


  // ------------------------------------------------------------------------------
  static String[] WALLS = { // [6]
    "DISTOX_WALLS_TYPE",         // 
    "DISTOX_WALLS_PLAN_THR",     // clino threshold for splays to contrinute to walls in plan view
    "DISTOX_WALLS_EXTENDED_THR", // clino threshold for splays to contribute to walls in profile view
    "DISTOX_WALLS_XCLOSE",       // 
    "DISTOX_WALLS_CONCAVE",      // allowed "concavity"
    "DISTOX_WALLS_XSTEP",        // 
    null
  };

  static int[] WALLStitle = {
    R.string.pref_walls_type_title,
    R.string.pref_walls_plan_thr_title,
    R.string.pref_walls_extended_thr_title,
    R.string.pref_walls_xclose_title,
    R.string.pref_walls_concave_title,
    R.string.pref_walls_xstep_title
  };

  static int[] WALLSdesc = {
    R.string.pref_walls_type_summary,
    R.string.pref_walls_plan_thr_summary,
    R.string.pref_walls_extended_thr_summary,
    R.string.pref_walls_xclose_summary,
    R.string.pref_walls_concave_summary,
    R.string.pref_walls_xstep_summary
  };

  static String[] WALLSdef = {
    "0",
    "70",
    "45",
    "0.1",
    "0.1",
    "1.0"
  };

  // ------------------------------------------------------------------------------
  static String[] DRAW = { // [13]
    "DISTOX_UNSCALED_POINTS", // 0  // whether drawing point items should stay unscaled when zooming
    "DISTOX_DRAWING_UNIT",    // 1  // plot unit
    "DISTOX_LABEL_SIZE",      // 2  // size of labels [pt]
    "DISTOX_LINE_THICKNESS",  // 3  // thickness of normal lines (walls are twice)
    "DISTOX_LINE_STYLE",            // line style: 0 bezier, 1 fine, 2 normal, 3 coarse
    "DISTOX_LINE_SEGMENT",    // 5
    "DISTOX_ARROW_LENGTH",          // length of the tick at the first line-point (when applicable)
    "DISTOX_REDUCE_ANGLE",          // "rock" reducing lines: maximal angle
    "DISTOX_AUTO_SECTION_PT", // 8  // whether to add section point when tracing a section line
    "DISTOX_LINE_CONTINUE",         // default line continuation set
    "DISTOX_AREA_BORDER",           // area border visibility
    "DISTOX_LINE_ACCURACY",   // 11 // Bezier interpolator param:
    "DISTOX_LINE_CORNER",     // 12 // Bezier interpolator param:
    null
  };

  static int[] DRAWtitle = {
    R.string.pref_unscaled_points_title,
    R.string.pref_drawing_unit_title,
    R.string.pref_label_size_title, 
    R.string.pref_line_thickness_title,
    R.string.pref_linestyle_title,
    R.string.pref_segment_title,
    R.string.pref_arrow_length_title,
    R.string.pref_reduce_angle_title,
    R.string.pref_auto_section_pt_title,
    R.string.pref_linecontinue_title,
    R.string.pref_area_border_title,
    R.string.pref_lineacc_title,
    R.string.pref_linecorner_title
  };

  static int[] DRAWdesc = {
    R.string.pref_unscaled_points_summary,
    R.string.pref_drawing_unit_summary,
    R.string.pref_label_size_summary,
    R.string.pref_line_thickness_summary,
    R.string.pref_linestyle_summary,
    R.string.pref_segment_message,
    R.string.pref_arrow_length_message,
    R.string.pref_reduce_angle_summary,
    R.string.pref_auto_section_pt_summary,
    R.string.pref_linecontinue_summary,
    R.string.pref_area_border_summary,
    R.string.pref_lineacc_summary,  
    R.string.pref_linecorner_summary
  };

  static String[] DRAWdef = {
    FALSE,
    "1.2",
    "24",
    "1",
    "2",
    "10",
    "8",
    "45",
    FALSE,
    "0",
    TRUE,
    "1.0",
    "20.0"
  };

  // ------------------------------------------------------------------------------
  static String[] ERASE = { // [3]
    "DISTOX_CLOSENESS", // 0 // "select" radius // "select" radius // "select" radius
    "DISTOX_ERASENESS",      // "erase" radius // "erase" radius
    "DISTOX_POINTING",  // 2 // "size" of a "point touch" (max distance between down and up)
    null
  };

  static int[] ERASEtitle = {
    R.string.pref_closeness_title,
    R.string.pref_eraseness_title,
    R.string.pref_pointing_title
  };

  static int[] ERASEdesc = {
    R.string.pref_closeness_message,
    R.string.pref_eraseness_message,
    R.string.pref_pointing_message
  };

  static String[] ERASEdef = {
    "24",
    "36",
    "24"
  };

  // ------------------------------------------------------------------------------
  static String[] EDIT = { // [4]
    "DISTOX_DOT_RADIUS", // 0 // radius of green dots
    "DISTOX_CLOSENESS",  // 1 // "select" radius // "select" radius // "select" radius
    "DISTOX_MIN_SHIFT",       // maximum amount for a shift (to avoid jumps)
    "DISTOX_POINTING",   // 3 // "size" of a "point touch" (max distance between down and up)
    null
  };

  static int[] EDITtitle = {
    R.string.pref_dot_radius_title,
    R.string.pref_closeness_title,
    R.string.pref_min_shift_title,
    R.string.pref_pointing_title
  };

  static int[] EDITdesc = {
    R.string.pref_dot_radius_message,
    R.string.pref_closeness_message,
    R.string.pref_min_shift_message,
    R.string.pref_pointing_message
  };

  static String[] EDITdef = {
    "5",
    "24",
    "60",
    "24"
  };

  // ------------------------------------------------------------------------------
  static String[] SKETCH = { // [3] 
    // "DISTOX_SKETCH_USES_SPLAYS",
    "DISTOX_SKETCH_MODEL_TYPE",
    "DISTOX_SKETCH_LINE_STEP",
    // "DISTOX_SKETCH_BORDER_STEP",
    // "DISTOX_SKETCH_SECTION_STEP",
    "DISTOX_DELTA_EXTRUDE",
    // "DISTOX_COMPASS_READINGS",
    null
  };

  static int[] SKETCHtitle = {
    // R.string.pref_sketchUsesSplays_title,
    R.string.pref_sketchModelType_title,
    R.string.pref_sketchLineStep_title,
    // R.string.pref_sketchBorderStep_title,
    // R.string.pref_sketchSectionStep_title,
    R.string.pref_sketchDeltaExtrude_title
    // R.string.pref_sketchCompassReadings
  };

  static int[] SKETCHdesc = {
    // R.string.pref_sketchUsesSplays_summary,
    R.string.pref_sketchModelType_summary,
    R.string.pref_sketchLineStep_summary,
    // R.string.pref_sketchBorderStep_summary,
    // R.string.pref_sketchSectionStep_summary,
    R.string.pref_sketchDeltaExtrude_summary
    // R.string.pref_sketchCompassReadings
  };

  static String[] SKETCHdef = {
    // FALSE, res, hlp );
    "0",
    "0.5",
    // "0.2",
    // "0.5",
    "50"
    // "4"
  };

  // ------------------------------------------------------------------------------
  static String[] LOG = {
    "DISTOX_LOG_STREAM",
    "DISTOX_LOG_APPEND",
    "DISTOX_LOG_DEBUG",
    "DISTOX_LOG_ERR", 
    "DISTOX_LOG_PERM",
    "DISTOX_LOG_INPUT",
    "DISTOX_LOG_PATH",
    "DISTOX_LOG_IO", 
    "DISTOX_LOG_BT",
    "DISTOX_LOG_COMM",
    "DISTOX_LOG_DISTOX",
    "DISTOX_LOG_PROTO",
    "DISTOX_LOG_DEVICE",
    "DISTOX_LOG_CALIB",
    "DISTOX_LOG_DB",  
    "DISTOX_LOG_UNITS",
    "DISTOX_LOG_DATA",
    "DISTOX_LOG_SHOT",
    "DISTOX_LOG_SURVEY",
    "DISTOX_LOG_NUM",  
    "DISTOX_LOG_FIXED",
    "DISTOX_LOG_LOC", 
    "DISTOX_LOG_PHOTO",
    "DISTOX_LOG_SENSOR",
    "DISTOX_LOG_PLOT", 
    "DISTOX_LOG_BEZIER",
    "DISTOX_LOG_THERION",
    "DISTOX_LOG_CSURVEY",
    "DISTOX_LOG_PTOPO",
    "DISTOX_LOG_ZIP",
    "DISTOX_LOG_SYNC", 
    null
  };

  static int[] LOGtitle = {
     R.string.pref_log_append,  
     R.string.pref_log_debug,   
     R.string.pref_log_err,     
     R.string.pref_log_perm,    
     R.string.pref_log_input,   
     R.string.pref_log_path,    
     R.string.pref_log_io,      
     R.string.pref_log_bt,      
     R.string.pref_log_comm,    
     R.string.pref_log_distox,  
     R.string.pref_log_proto,   
     R.string.pref_log_device,  
     R.string.pref_log_calib,   
     R.string.pref_log_db,      
     R.string.pref_log_units,   
     R.string.pref_log_data,    
     R.string.pref_log_shot,    
     R.string.pref_log_survey,  
     R.string.pref_log_num,     
     R.string.pref_log_fixed,   
     R.string.pref_log_loc,     
     R.string.pref_log_photo,   
     R.string.pref_log_sensor,  
     R.string.pref_log_plot,    
     R.string.pref_log_bezier,  
     R.string.pref_log_therion, 
     R.string.pref_log_csurvey, 
     R.string.pref_log_ptopo,   
     R.string.pref_log_zip,     
     R.string.pref_log_sync
  };
}
