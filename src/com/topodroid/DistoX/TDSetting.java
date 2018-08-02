/* @file TDSetting.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid application settings (preferenceces)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.os.Build;
import android.content.Context;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

// import android.util.Log;

class TDSetting
{
  static private String defaultTextSize = "16";
  static private String defaultButtonSize = "1";

  static void setTextSize( TopoDroidApp app, int ts )
  {
    float ds = app.getDisplayDensity() / 3.0f;
    mTextSize = (int)( ( ds * ts ) );
  }

  // get tentative text size
  static int getTextSize( TopoDroidApp app, int ts )
  {
    float ds = app.getDisplayDensity() / 3.0f;
    return (int)( ( ds * ts ) );
  }

  static void setLabelSize( float f )
  {
    if ( f >= 1 && f != mLabelSize ) {
      mLabelSize = f;
      BrushManager.setTextSizes( );
    }
  }

  static void setStationSize( float f )
  {
    if ( f >= 1 && f != mStationSize ) {
      mStationSize = f;
      BrushManager.setTextSizes( );
    }
  }

  static void setDrawingUnit( TopoDroidApp app, float f )
  {
    if ( f > 0.1f && f != mUnitIcons ) {
      mUnitIcons = f;
      BrushManager.reloadPointLibrary( app, app.getResources() );
    }
  }

  // ---------------------------------------------------------
  // PREFERENCES KEYS

  final static private int NR_PRIMARY_PREFS = 13;

  static final private String[] key = { // prefs keys
    // ------------------------- PRIMARY PREFS
    "DISTOX_EXTRA_BUTTONS",       // Activity Level
    "DISTOX_SIZE_BUTTONS",        // size of buttons (S, N, M, L, XL)
    "DISTOX_TEXT_SIZE",           // size of tetx [pt]
    "DISTOX_MKEYBOARD",           // whether to use TopoDroid keypads
    "DISTOX_TEAM",                // default team
    "DISTOX_LOCAL_MAN",          // enable local man pages
    "DISTOX_COSURVEY",            // whether to enable co-surveying
    "DISTOX_INIT_STATION",        // default initial station name
    "DISTOX_AZIMUTH_MANUAL",      // whether the "extend" is fixed L or R, selected by hand 

    "DISTOX_DEVICE",              // N.B. indexKeyDeviceName - USED by TopoDroidApp to store the device
    "DISTOX_BLUETOOTH",           // whether to check bluetooth on start

    "DISTOX_LOCALE",              // language
    "DISTOX_CWD",                 // CWD must be the last of primary preferences

    // ----------------------- DEVICE PREFERNCES 
    "DISTOX_SOCK_TYPE",           // socket type
    "DISTOX_COMM_RETRY",          // number of socket connection attempts
    "DISTOX_WAIT_LASER",          // msec wait after command "laser ON"
    "DISTOX_WAIT_SHOT",           // msec wait after command "take shot"
    "DISTOX_WAIT_DATA",           // msec wait after a data/vector packet
    "DISTOX_WAIT_CONN",           // msec wait after getting "NO PACKET"
    "DISTOX_Z6_WORKAROUND",       // whether to enable Z6 workaround
    "DISTOX_CONN_MODE",           // data download mode [on-demand, continuous]
    "DISTOX_AUTO_PAIR",           // whether to auto-pair the discovered DistoX
    "DISTOX_SOCKET_DELAY",        // delay before a socket-connection attempt
    "DISTOX_AUTO_RECONNECT",      // whether to try to reconnect if DistoX is lost [continuos mode]
    "DISTOX_HEAD_TAIL",           // whether to read head-tail to get the number of packets to download

    // ------------------- SURVEY PREFERENCES
    "DISTOX_CLOSE_DISTANCE",      // tolerance among leg shots [%]
    "DISTOX_EXTEND_THR2",         // half angle around 90 where splays have "vert" extend
    "DISTOX_VTHRESHOLD",          // if shot clino is above, LRUD are horizontal
    "DISTOX_SURVEY_STATION",      // DISTOX_SURVEY_STATIONS must not be used
    "DISTOX_DATA_BACKUP",         //
    "DISTOX_UNIT_LENGTH",         // units of lengths [m, y, ft]
    "DISTOX_UNIT_ANGLE",          // units of angles [deg, grad]
    "DISTOX_ACCEL_PERCENT",       // shot quality G threshold [%]
    "DISTOX_MAG_PERCENT",         // shot quality M threhsold [%]
    "DISTOX_DIP_THR",             // shot qualoity dip threshold [deg]
    "DISTOX_LOOP_CLOSURE_VALUE",  // whether to close loop
    "DISTOX_CHECK_ATTACHED",      // whether to check all shots are connected
    "DISTOX_CHECK_EXTEND",        // whether leg extends are all set
    "DISTOX_PREV_NEXT",           // whether to put "prev-next" arrows in shot edit dialog
    "DISTOX_MAX_SHOT_LENGTH",     // maximum length of a shot data
    "DISTOX_MIN_LEG_LENGTH",      // minimum length of a shot data
    "DISTOX_BACKSHOT",            // using DistoX in backshot mode
    "DISTOX_NO_CURSOR",           // no cursor for custom keyboard
    "DISTOX_RECENT_TIMEOUT",      // recent block timeout

    "DISTOX_UNIT_LOCATION",       // units of location [ddmmss dec.deg]
    "DISTOX_CRS",                 // default C.R.S.
     
    // -------------------- CALIB PREFERENCES
    "DISTOX_GROUP_BY",            // calib data grouping policy
    "DISTOX_GROUP_DISTANCE",      // calib data grouping by the distance threshold
    "DISTOX_CALIB_EPS",           // calib computation param EPS
    "DISTOX_CALIB_MAX_IT",        // calib computation maximun number of iterations
    "DISTOX_CALIB_SHOT_DOWNLOAD", // remote calib data immediately downloaded
    "DISTOX_RAW_CDATA",           // whether to display also raw calib data
    "DISTOX_CALIB_ALGO",          // calib algo [auto, linear, non-linear]

    // -------------------- SKETCH PREFERENCES
    "DISTOX_AUTO_STATIONS",      // whether to add stations to thetion th2 exports
    "DISTOX_CLOSENESS",          // "select" radius
    "DISTOX_ERASENESS",          // "erase" radius
    "DISTOX_MIN_SHIFT",          // maximum amount for a shift (to avoid jumps)
    "DISTOX_POINTING",           // "size" of a "point touch" (max distance between down and up)
    "DISTOX_LINE_SEGMENT",       // minimum distance between consecutive points on a line
    "DISTOX_LINE_ACCURACY",      // Bezier interpolator param:
    "DISTOX_LINE_CORNER",        // Bezier interpolator param:
    "DISTOX_LINE_STYLE",         // line style: 0 bezier, 1 fine, 2 normal, 3 coarse
    "DISTOX_LINE_CONTINUE",      // default line continuation set
    "DISTOX_DRAWING_UNIT",       // plot unit
    "DISTOX_PICKER_TYPE",        // tool picker: most-recent, list, grid, triple-grid
    "DISTOX_HTHRESHOLD",         // if clino is over thr, H_SECTION is horizontal (has north arrow)
    "DISTOX_STATION_SIZE",       // size of station names [pt]
    "DISTOX_LABEL_SIZE",         // size of labels [pt]
    "DISTOX_LINE_THICKNESS",     // thickness of normal lines (walls are twice)
    "DISTOX_AUTO_SECTION_PT",    // whether to add section point when tracing a section line
    "DISTOX_BACKUP_NUMBER",      // number of plot backups
    "DISTOX_BACKUP_INTERVAL",    // minimum interval between plot backups [60 s]
    "DISTOX_SHARED_XSECTIONS",   // whether at-station X-sections are shared among plots
    // "DISTOX_PLOT_CACHE",

    // -------------------- LASER PREFERENCES
    "DISTOX_SHOT_TIMER",         // bearing-clino timer [1/10 s]
    "DISTOX_BEEP_VOLUME",        // bearing-clino beep volume [%]
    "DISTOX_LEG_SHOTS",          // nr. of shots to make a leg [2, 3, 4]

    // -------------------- 3D-MODEL PREFERENCES
    "DISTOX_SKETCH_MODEL_TYPE",   
    "DISTOX_SKETCH_LINE_STEP",    // 53
    "DISTOX_DELTA_EXTRUDE",       // 54
    "DISTOX_COMPASS_READINGS",    // 55

    // -------------------- IMPORT-EXPORT PREFERENCES
    "DISTOX_SPLAY_EXTEND",       // whether to set L/R extend to LRUD splay shots (Compass, VTopo import)
    "DISTOX_LRUD_VERTICAL",
    "DISTOX_LRUD_HORIZONTAL",
    "DISTOX_BITMAP_SCALE",       // default bitmap scale PNG
    "DISTOX_BEZIER_STEP",        // max step between interpolating points for bezier in export (cSurvey)
    "DISTOX_THUMBNAIL",          // size of photo thumbnails
    "DISTOX_DOT_RADIUS",         // radius of green dots
    "DISTOX_FIXED_THICKNESS",    // thickness of fixed lines
    "DISTOX_ARROW_LENGTH",       // length of the tick at the first line-point (when applicable)
    "DISTOX_EXPORT_SHOTS",       // default data export
    "DISTOX_EXPORT_PLOT",        // default plot export
    "DISTOX_THERION_MAPS",       // whether to put map commands before centerline in therion
    "DISTOX_SVG_GRID",           // whether to export grid in SVG 
    "DISTOX_SVG_LINE_DIR",       // whether to add line orientation ticks in SVG export
    // "DISTOX_SVG_IN_HTML",        // whether to export SVG embedded in HTML
    "DISTOX_SVG_POINT_STROKE",
    "DISTOX_SVG_LABEL_STROKE",
    "DISTOX_SVG_LINE_STROKE",
    "DISTOX_SVG_GRID_STROKE",
    "DISTOX_SVG_SHOT_STROKE",
    "DISTOX_SVG_LINEDIR_STROKE",

    "DISTOX_KML_STATIONS",       // whether to add station points to KML export
    "DISTOX_KML_SPLAYS",         // whether to add splay lines to KML export

    "DISTOX_SPLAY_VERT_THRS",    // splays with clino over mSplayVertThrs are not displayed in plan view
    "DISTOX_BACKSIGHT",          // whether to add backsight fields in manual shot input dialog
    // "DISTOX_MAG_ANOMALY",        // whether to compensate magnetic anomaly
    "DISTOX_DASH_SPLAY",         // whether dash-splay are coherent between plan and profile
    "DISTOX_VERT_SPLAY",         // splays with clino over this are shown with dashed/dotted line
    "DISTOX_HORIZ_SPLAY",        // splays off-azimuth over this are shown with dashed/dotted line
    "DISTOX_SECTION_SPLAY",      // splays with angle over this are shown with dashed/dotted line
    "DISTOX_STATION_PREFIX",     // whether to add cave-name prefix to stations (cSurvey/Compass export)
    "DISTOX_STATION_NAMES",      // station names: 0 alphanumeric, 1 numbers
    "DISTOX_ZOOM_CTRL",          // whether to have zoom-ctrl (no, temp., always)
    "DISTOX_SIDE_DRAG",          // whether to enable side-drag
    // "DISTOX_DXF_SCALE",          // default DXF scale (export)
    "DISTOX_ACAD_VERSION",
    "DISTOX_BITMAP_BGCOLOR",     // bitmap background color [RGB]
    "DISTOX_SURVEX_EOL",         // survex end of line [either Linux or Windows]
    "DISTOX_SURVEX_SPLAY",       // whether to name endpoint of splays in Survex export
    "DISTOX_SURVEX_LRUD",        // whether to add LRUD to Survex export
    "DISTOX_SWAP_LR",            // whether to swap Left-Right in Compass export
    "DISTOX_UNSCALED_POINTS",    // whether drawing point items should stay unscaled when zooming
    "DISTOX_UNIT_GRID",          // plot grid unit [m, y, 2ft]
    // "DISTOX_XTHERION_AREAS",  // save areas a-la xtherion
    "DISTOX_THERION_SPLAYS",     // whether to add u:splay lines to Therion th2 export
    "DISTOX_COMPASS_SPLAYS",     // whether to add splays to Compass dat export
    "DISTOX_RECENT_NR",          // number of most recent items (item picker)
    "DISTOX_AREA_BORDER",        // area border visibility
    "DISTOX_ORTHO_LRUD",         // orthogonal LRUD ( >=1 disable, min 0 )
    "DISTOX_REDUCE_ANGLE",       // "rock" reducing lines: maximal angle
    // "DISTOX_SECTION_STATIONS",    //

    "DISTOX_WALLS_TYPE",          // 87
    "DISTOX_WALLS_PLAN_THR",      // clino threshold for splays to contrinute to walls in plan view
    "DISTOX_WALLS_EXTENDED_THR",  // clino threshold for splays to contribute to walls in profile view
    "DISTOX_WALLS_XCLOSE",        // 
    "DISTOX_WALLS_XSTEP",         // 
    "DISTOX_WALLS_CONCAVE",       // allowed "concavity"

    "DISTOX_DXF_BLOCKS",          // whether to export point items as Blocks in DXF export

    // "DISTOX_SKETCH_USES_SPLAYS",  // 
    // "DISTOX_SKETCH_BERDER_STEP",
    // "DISTOX_SKETCH_SECTION_STEP", // 

    "DISTOX_ALGO_MIN_ALPHA",      // min-algo params: alpha
    "DISTOX_ALGO_MIN_BETA",       //                  beta
    "DISTOX_ALGO_MIN_GAMMA",      //                  gamma
    "DISTOX_ALGO_MIN_DELTA",      //                  delta

  };

  static boolean mDxfBlocks = true; // DXF_BLOCKS

  static float mAlgoMinAlpha = 0.1f;
  static float mAlgoMinBeta  = 4.0f;
  static float mAlgoMinGamma = 1.0f;
  static float mAlgoMinDelta = 1.0f;

  static String keyDeviceName() { return "DISTOX_DEVICE"; }

  // static final  String EXPORT_TYPE    = "th";    // DISTOX_EXPORT_TH

  // prefs default values
  static String  mDefaultTeam = "";

  static final int MIN_SIZE_BUTTONS = 32;
  static final int MIN_SIZE_TEXT    = 12;
  static final int BTN_SIZE_SMALL   = 36;
  static final int BTN_SIZE_NORMAL  = 42;
  static final int BTN_SIZE_MEDIUM  = 48;
  static final int BTN_SIZE_LARGE   = 64;
  static final int BTN_SIZE_HUGE    = 84;
  static int mSizeBtns     = 0;      // action bar buttons scale (either 1 or 2)
  static int mSizeButtons  = 42;     // default 42 minimum MIN_SIZE_BUTTONS
  static int mTextSize     = 16;     // list text size 
  static boolean mKeyboard = true;
  static boolean mNoCursor = false;
  static boolean mLocalManPages = true;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // IMPORT EXPORT
  static boolean mLRExtend           = true;   // whether to extend LR or not (Compass/VisualTopo input)
  static float   mLRUDvertical       = 45;     // vertical splay for UD 
  static float   mLRUDhorizontal     = 45;     // horizontal splay for LR 

  static String mSurvexEol           = "\n";
  static boolean mSurvexSplay        = false;
  static boolean mSurvexLRUD         = false;
  static boolean mSwapLR             = false; // swap LR in Compass export
  static boolean mOrthogonalLRUD     = false; // whether angle > 0 
  static float mOrthogonalLRUDAngle  = 0;     // angle
  static float mOrthogonalLRUDCosine = 1;     // cosine of the angle

  static boolean mExportStationsPrefix = false;  // whether to prepend cave name to station in cSurvey/compass export

  // static boolean mXTherionAreas = false;
  static boolean mAutoStations  = true;  // whether to add stations automatically to scrap therion files
  static boolean mTherionSplays = false; // whether to add splay segments to auto stations
  static boolean mCompassSplays = true;  // whether to add splays to Compass export

  static float mBitmapScale = 1.5f;
  static float mBezierStep  = 0.2f;
  static float getBezierStep() { return ( mBezierStep < 0.1f )? 0.05f : (mBezierStep/2); }
 
  // static float mDxfScale    = 1.0f;
  static int mBitmapBgcolor = 0x000000;

  static int mAcadVersion = 9;      // AutoCAD version 9, or 13

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // LOCATION
  static String mCRS = "Long-Lat";    // default coord ref systen 
  // static final  String UNIT_LOCATION  = "ddmmss";
  static int mUnitLocation = 0; // 0 dec-degree, 1 ddmmss

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // CALIBRATION

  // static final String GROUP_DISTANCE = "40";
  static float mGroupDistance = 40;

  static final float DISTOX_MAX_EPS  = 0.01f; // hard limit
  static final String CALIB_EPS      = "0.000001";
  static float mCalibEps = 0.000001f; // calibartion epsilon

  static int   mCalibMaxIt = 200;     // calibration max nr of iterations
  static boolean mCalibShotDownload = true;

  // calibration data grouping policies
  static final int GROUP_BY_DISTANCE = 0;
  static final int GROUP_BY_FOUR     = 1;
  static final int GROUP_BY_ONLY_16  = 2;
  // static final String GROUP_BY  = "1";     // GROUP_BY_FOUR
  static int mGroupBy = GROUP_BY_FOUR;  // how to group calib data

  // static boolean mRawData = false;   // whether to display calibration raw data as well
  static int   mRawCData  = 0;
  static int   mCalibAlgo = 0;   // calibration algorithm: 0 auto, 1 linear, 2 non-linear

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DEVICE
  final static int CONN_MODE_BATCH      = 0;      // DistoX connection mode
  final static int CONN_MODE_CONTINUOUS = 1;
  final static int CONN_MODE_MULTI      = 2;
  static int mConnectionMode    = CONN_MODE_BATCH; 

  static boolean isConnectionModeBatch() { return mConnectionMode != CONN_MODE_CONTINUOUS; }
  static boolean isConnectionModeContinuous() { return mConnectionMode == CONN_MODE_CONTINUOUS; }

  static boolean mZ6Workaround  = true;

  static boolean mAutoReconnect = false;
  static boolean mHeadTail      = false; // whether to use readHeadTail to download the data
  static boolean mAutoPair      = true;
  static int mConnectSocketDelay = 0; // wait time if not paired [0.1 sec]

  // static final boolean CHECK_BT = true;
  static int mCheckBT = 1;        // BT: 0 disabled, 1 check on start, 2 enabled

  static final int TD_SOCK_DEFAULT      = 0;    // BT socket type
  static final int TD_SOCK_INSEC        = 1;
  static final int TD_SOCK_PORT         = 2;
  static final int TD_SOCK_INSEC_PORT   = 3;
  // static final int TD_SOCK_INSEC_INVOKE = 4;
  // static int mDefaultSockType = (android.os.Build.MANUFACTURER.equals("samsung") ) ? TD_SOCK_INSEC : TD_SOCK_DEFAULT;
  static String mDefaultSockStrType = (android.os.Build.MANUFACTURER.equals("samsung") ) ? "1" : "0";
  static int mSockType = TD_SOCK_DEFAULT;

  static int mCommRetry = 1; 
  static int mCommType  = 0; // 0: on-demand, 1: continuous

  static int mWaitLaser = 1000;
  static int mWaitShot  = 4000;
  static int mWaitData  =  100;  // delay between data
  static int mWaitConn  =  500;  // delay waiting a connection
  static int mWaitCommand = 100;

  static boolean mCheckAttached = false;    // whether to check is there are shots non-attached
  static boolean mCheckExtend   = true;
  static boolean mPrevNext = true;    // whether to display prev-next buttons in shot dialog

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SHOTS
  static float mVThreshold = 80f;   // verticality threshold (LRUD)
  static float mHThreshold;         // horizontal plot threshold
  static boolean mDataBackup = false; // whether to export data when shotwindow is closed
  static boolean mDistoXBackshot = false;
  static int mTitleColor = TDColor.TITLE_NORMAL;

  static int mExportShotsFormat = -1; // DISTOX_EXPORT_NONE
  static int mExportPlotFormat  = -1; // DISTOX_EXPORT_NONE
  static boolean mTherionMaps   = false;
  static boolean mSvgGrid       = false;
  // static boolean mSvgInHtml     = false;
  static boolean mSvgLineDirection = false;
  static boolean mKmlStations   = true;
  static boolean mKmlSplays     = false;
  static int mRecentTimeout     = 30; // 30 seconds

  // static int mScreenTimeout = 60000; // 60 secs
  static int mTimerWait        = 10;    // Acc/Mag timer countdown (secs)
  static int mBeepVolume       = 50;    // beep volume
  static int mCompassReadings  = 4;     // number of compass readings to average

  // static final String CLOSE_DISTANCE = "0.05"; // 50 cm / 1000 cm
  static float   mCloseDistance = 0.05f; 
  static int     mMinNrLegShots = 3;
  static String  mInitStation   = "0";
  static boolean mBacksightInput = false;   // whether to add backsight fields in shot anual-input dialog
  static float   mSplayVertThrs = 80;
  static boolean mAzimuthManual = false;    // whether to manually set extend / or use reference azimuth
  static boolean mDashSplay     = true;     // whether dash-splay are coherent between plan and profile
  static float   mVertSplay     = 50;
  static float   mHorizSplay    = 60;
  static float   mCosHorizSplay = TDMath.cosd( mHorizSplay );
  static float   mSectionSplay  = 60;
  static int     mStationNames  = 0;        // type of station names (0: alpha, 1: number)


  static final int LOOP_NONE      = 0;
  static final int LOOP_CYCLES    = 1;
  static final int LOOP_TRIANGLES = 3;
  static int mLoopClosure = LOOP_NONE;      // loop closure: 0 none, 1 normal, 3 triangles
  
  static final  String UNIT_LENGTH         = "meters";
  static final  String UNIT_ANGLE          = "degrees";
  // static final  String UNIT_ANGLE_GRADS = "grads";
  // static final  String UNIT_ANGLE_SLOPE = "slope";
  // conversion factor from internal units (m) to user units
  static float mUnitLength = 1;
  static float mUnitAngle  = 1;
  static String mUnitLengthStr = "m";    // N.B. Therion syntax: "m", "ft"
  static String mUnitAngleStr  = "deg";  // N.B. Therion syntax: "deg", "grad"

  // static final String EXTEND_THR = "10"; 
  static float mExtendThr = 10;             // extend vertically splays in [90-30, 90+30] of the leg

  static int mThumbSize = 200;               // thumbnail size

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SKETCH DRAWING

  static float mReduceAngle  = 45;    // minimal angle between segments of "straightened" lines
  static float mReduceCosine = 0.7f;  // cosine of mReduceAngle

  // static boolean mZoomControls = false;
  static int mZoomCtrl = 1;
  static boolean mSideDrag = false;

  static float mUnitIcons = 1.4f; // drawing unit

  // selection_radius = cutoff + closeness / zoom
  static final float mCloseCutoff = 0.01f; // minimum selection radius

  static float mSelectness = 24f;            // selection radius
  static float mEraseness = 36f;             // eraser radius
  static int mMinShift = 60;                 // shift sensitivity
  static int mPointingRadius = 16;

  // static final String LINE_SHIFT = "20.0";
  static float mUnitGrid  = 1;         // 1: meter, 0.9... yard

  static final int PICKER_RECENT = 0; // Drawing-tools picker type
  static final int PICKER_LIST   = 1; 
  static final int PICKER_GRID   = 2;
  static final int PICKER_GRID_3 = 3;
  static int mPickerType = PICKER_RECENT;
  static int mRecentNr    = 4;        // nr. most recent symbols

  static final int LINE_STYLE_BEZIER = 0;  // drawing line styles
  static final private int LINE_STYLE_ONE    = 1;
  static final private int LINE_STYLE_TWO    = 2;
  static final private int LINE_STYLE_THREE  = 3;
  static final private String LINE_STYLE     = "2";     // LINE_STYLE_TWO NORMAL
  static int   mLineStyle = LINE_STYLE_BEZIER;    
  static int   mLineType;        // line type:  1       1     2    3
  static int   mLineSegment   = 10;
  static int   mLineSegment2  = 100;   // square of mLineSegment
  static float mLineAccuracy  = 1f;
  static float mLineCorner    = 20;    // corner threshold
  static int   mContinueLine  = DrawingWindow.CONT_NONE; // 0

  static float mStationSize    = 20;   // size of station names [pt]
  static float mLabelSize      = 24;   // size of labels [pt]
  static float mFixedThickness = 1;    // width of fixed lines
  static float mLineThickness  = 1;    // witdh of drawing lines
  static boolean mAutoSectionPt = false;
  static int   mBackupNumber   = 5;
  static int   mBackupInterval = 60;
  static boolean mSharedXSections = false; // default value
  // static boolean mPlotCache       = true;  // default value
  static float mDotRadius      = 5;
  static float mArrowLength    = 8;

  // NOTE not used, but could set a default for section splays
  // static int mSectionStations = 3; // 1: From, 2: To, 3: both

  static boolean mUnscaledPoints = false;
  static boolean mAreaBorder     = true;

  static float mSvgPointStroke   = 0.1f;
  static float mSvgLabelStroke   = 0.3f;
  static float mSvgLineStroke    = 0.5f;
  static float mSvgLineDirStroke = 2f;
  static float mSvgGridStroke    = 0.5f;
  static float mSvgShotStroke    = 0.5f;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // 3D
  static int   mSketchModelType = 1;
  static float mSketchSideSize;
  static float mDeltaExtrude;
  // static boolean mSketchUsesSplays; // whether 3D models surfaces use splays
  // static float mSketchBorderStep;
  // static float mSketchSectionStep;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DATA ACCURACY
  static float mAccelerationThr = 1; // acceleration threshold (shot quality)
  static float mMagneticThr     = 1; // magnetic threshold
  static float mDipThr          = 2; // dip threshold
  static float mMaxShotLength   = 50; // max length of a shot (if larger it is overshoot)
  static float mMinLegLength    = 0.5f; // min length of a leg (if shorter it is undershoot)
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // WALLS

  static final int WALLS_NONE    = 0;
  static final int WALLS_CONVEX  = 1;
  static final int WALLS_DLN     = 2;
  static final int WALLS_LAST    = 2; // placeholder
  static int   mWallsType        = WALLS_NONE;
  static float mWallsPlanThr     = 70;
  static float mWallsExtendedThr = 45;
  static float mWallsXClose      = 0.1f;
  static float mWallsXStep       = 1.0f;
  static float mWallsConcave     = 0.1f;

  // ------------------------------------------------------------------
  // static void setZoomControls( boolean ctrl )
  // {
  //   mZoomControls = ctrl;
  //   // FIXME forward setting to DrawingWindow
  // }
  static void setZoomControls( String ctrl, boolean is_multitouch ) // PRIVATE
  {
    try {
      int i = Integer.parseInt( ctrl );
      if ( i >= 0 && i <= 2 ) mZoomCtrl = i;
      if ( mZoomCtrl == 0 && ! is_multitouch ) mZoomCtrl = 1;
    } catch ( NumberFormatException e ) { }
  }

  // backgroind color RGB_565
  private static void setBitmapBgcolor( String color )
  {
    String[] vals = color.split("\\s+"); 
    if ( vals.length == 3 ) {
      try { 
        int r = Integer.parseInt( vals[0] );
        int g = Integer.parseInt( vals[1] );
        int b = Integer.parseInt( vals[2] );
        if ( r > 255 ) r = 255; if ( r < 0 ) r = 0;
        if ( g > 255 ) g = 255; if ( g < 0 ) g = 0;
        if ( b > 255 ) b = 255; if ( b < 0 ) b = 0;
        mBitmapBgcolor = 0xff000000 | ( r << 16 ) | ( g << 8 ) | b;
      } catch ( NumberFormatException e ) { }
    }
  }

  // ------------------------------------------------------------------
  static private float tryFloat( SharedPreferences prefs, String key, String def_value )
  {
    float f = 0;
    try { f = Float.parseFloat( prefs.getString( key, def_value ) ); } 
    catch ( NumberFormatException e ) {
      TDLog.Error("Integer Format Error. Key " + key + " " + e.getMessage() );
      f = Float.parseFloat(def_value);
      setPreference( prefs, key, def_value );
    }
    return f;
  }

  static private int tryInt( SharedPreferences prefs, String key, String def_value )
  {
    int i = 0;
    try { i = Integer.parseInt( prefs.getString( key, def_value ) ); }
    catch( NumberFormatException e ) { 
      TDLog.Error("Integer Format Error. Key " + key + " " + e.getMessage() );
      i = Integer.parseInt(def_value);
      setPreference( prefs, key, def_value );
    }
    return i;
  }

  // static private float tryFloat( SharedPreferences prefs, String key, String def_value, float cur_value )
  // {
  //   float f = cur_value;
  //   try {
  //     f = Float.parseFloat( prefs.getString( key, def_value ) );
  //   } catch ( NumberFormatException e ) { }
  //   setPreference( prefs, key, f );
  //   return f;
  // }

  // static private float tryFloat( SharedPreferences prefs, String key, String def_value, float cur_value, float min_value )
  // {
  //   float f = cur_value;
  //   try {
  //     f = Float.parseFloat( prefs.getString( key, def_value ) );
  //     if ( f < min_value ) f = min_value;
  //   } catch ( NumberFormatException e ) { }
  //   setPreference( prefs, key, f );
  //   return f;
  // }

  // static private float tryFloat( SharedPreferences prefs, String key, String def_value,
  //                                float cur_value, float min_value, float max_value )
  // {
  //   TDLog.Error("try float. def " + def_value + " cur " + cur_value + " min " + min_value + " max " + max_value );
  //   float f = cur_value;
  //   try {
  //     f = Float.parseFloat( prefs.getString( key, def_value ) );
  //     if ( f < min_value ) f = min_value;
  //     if ( f > max_value ) f = max_value;
  //   } catch ( NumberFormatException e ) { }
  //   setPreference( prefs, key, f );
  //   return f;
  // }

  // static private int tryInt( SharedPreferences prefs, String key, String def_value, int cur_value )
  // {
  //   int i = cur_value;
  //   try {
  //     i = Integer.parseInt( prefs.getString( key, def_value ) );
  //   } catch ( NumberFormatException e ) { }
  //   setPreference( prefs, key, i );
  //   return i;
  // }

  // static private int tryInt( SharedPreferences prefs, String key, String def_value, int cur_value, int min_value )
  // {
  //   int i = cur_value;
  //   try {
  //     i = Integer.parseInt( prefs.getString( key, def_value ) );
  //     if ( i < min_value ) i = min_value;
  //   } catch ( NumberFormatException e ) { }
  //   setPreference( prefs, key, i );
  //   return i;
  // }

  // static private int tryInt( SharedPreferences prefs, String key, String def_value,
  //                            int cur_value, int min_value, int max_value )
  // {
  //   int i = cur_value;
  //   try {
  //     i = Integer.parseInt( prefs.getString( key, def_value ) );
  //     if ( i < min_value ) i = min_value;
  //     if ( i > max_value ) i = max_value;
  //   } catch ( NumberFormatException e ) { }
  //   setPreference( prefs, key, i );
  //   return i;
  // }

  static private void setLoopClosure( int loop_closure )
  {
    mLoopClosure = loop_closure;
    if ( mLoopClosure == LOOP_CYCLES ) {
      if ( ! TDLevel.overAdvanced ) mLoopClosure = LOOP_NONE;
    } else if ( mLoopClosure == LOOP_TRIANGLES ) {
      if ( ! TDLevel.overExpert ) mLoopClosure = LOOP_NONE;
    }
  }

  static boolean setSizeButtons( TopoDroidApp app, int size )
  {
    int sz = mSizeBtns;
    switch ( size ) {
      case 0: sz = BTN_SIZE_SMALL;  break;
      case 1: sz = BTN_SIZE_NORMAL; break;
      case 3: sz = BTN_SIZE_MEDIUM; break;
      case 4: sz = BTN_SIZE_LARGE;  break;
      case 2: sz = BTN_SIZE_HUGE;   break;
    }
    if ( sz != mSizeBtns ) {
      mSizeBtns = sz;
      mSizeButtons = (int)( mSizeBtns * app.getDisplayDensity() * 0.86f );
      // Log.v("DistoX", "Size " + size + " Btns " + mSizeBtns + " " + mSizeButtons );
      if ( mSizeButtons < MIN_SIZE_BUTTONS ) mSizeButtons = MIN_SIZE_BUTTONS;
      return true;
    }
    return false;
  }

  // get tentative size of buttons
  static int getSizeButtons( TopoDroidApp app, int size )
  {
    int sz = mSizeBtns;
    switch ( size ) {
      case 0: sz = BTN_SIZE_SMALL;  break;
      case 1: sz = BTN_SIZE_NORMAL; break;
      case 3: sz = BTN_SIZE_MEDIUM; break;
      case 4: sz = BTN_SIZE_LARGE;  break;
      case 2: sz = BTN_SIZE_HUGE;   break;
    }
    return (int)( sz * app.getDisplayDensity() * 0.86f );
  }

  static void loadPrimaryPreferences( TopoDroidApp app, SharedPreferences prefs )
  {
    defaultTextSize   = app.getResources().getString( R.string.default_textsize );
    defaultButtonSize = app.getResources().getString( R.string.default_buttonsize );

    // ------------------- GENERAL PREFERENCES
    int k = 0;

    int level = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_EXTRA_BUTTONS choice: 0, 1, 2, 3
    setActivityBooleans( prefs, app, level );

    setSizeButtons( app, tryInt( prefs, key[k++], defaultButtonSize ) ); // DISTOX_SIZE_BUTTONS
    setTextSize( app, tryInt( prefs, key[k++], defaultTextSize ) );// DISTOX_TEXT_SIZE
    mKeyboard    = prefs.getBoolean( key[k++], true );             // DISTOX_MKEYBOARD
    mDefaultTeam = prefs.getString( key[k++], "" );                // DISTOX_TEAM
    mLocalManPages = handleLocalUserMan( prefs.getString( key[k++], "0" ), false, null ); // DISTOX_LOCAL_MAN
    boolean co_survey = prefs.getBoolean( key[k++], false );       // DISTOX_COSURVEY 

    mInitStation = prefs.getString( key[k++], "0" ).replaceAll("\\s+", "");  // DISTOX_INIT_STATION 
    if ( mInitStation.length() == 0 ) mInitStation = "0";
    DistoXStationName.setInitialStation( mInitStation );

    mAzimuthManual = prefs.getBoolean( key[k++], false );   // DISTOX_AZIMUTH_MANUAL 
    TDAzimuth.mFixedExtend = ( TDSetting.mAzimuthManual )? 1L : 0L;
    // TDAzimuth.resetRefAzimuth( TDAzimuth.mRefAzimuth ); // BUG may call setRefAzimuthButton on non-UI thread
    
    // ------------------- DEVICE PREFERENCES -def--fallback--min-max
    k++;                                                     // DISTOX_DEVICE - UNUSED HERE
    mCheckBT        = tryInt( prefs, key[k++], "1" );        // DISTOX_BLUETOOTH choice: 0, 1, 2

    // TDLog.Profile("locale");
    app.setLocale( prefs.getString( key[k++], "" ), false );              // DISTOX_LOCALE

  }

  static void loadSecondaryPreferences( TopoDroidApp app, SharedPreferences prefs )
  {
    int k = NR_PRIMARY_PREFS;

    // ------------------- DEVICE PREFERENCES
    mSockType       = tryInt( prefs, key[k++], mDefaultSockStrType );        // DISTOX_SOCK_TYPE choice: 0, 1, (2, 3)

    mCommRetry      = tryInt( prefs, key[k++], "1" );        // DISTOX_COMM_RETRY
    mWaitLaser      = tryInt( prefs, key[k++], "1000" );     // DISTOX_WAIT_LASER
    mWaitShot       = tryInt( prefs, key[k++], "4000" );     // DISTOX_WAIT_SHOT
    mWaitData       = tryInt( prefs, key[k++], "100" );      // DISTOX_WAIT_DATA
    mWaitConn       = tryInt( prefs, key[k++], "500" );      // DISTOX_WAIT_CONN
    mZ6Workaround   = prefs.getBoolean( key[k++], true  );   // DISTOX_Z6_WORKAROUND
    mConnectionMode = tryInt( prefs, key[k++], "0" );        // DISTOX_CONN_MODE choice: 0, 1, 2
    mAutoPair       = prefs.getBoolean( key[ k++ ], true );  // DISTOX_AUTO_PAIR
    mConnectSocketDelay = tryInt(prefs, key[ k++ ], "0" );   // DISTOX_SOCKET_DELAY
    mAutoReconnect  = prefs.getBoolean( key[k++], false ); // DISTOX_AUTO_RECONNECT
    mHeadTail       = prefs.getBoolean( key[k++], false ); // DISTOX_HEAD_TAIL

    // ------------------- SURVEY PREFERENCES
    mCloseDistance = tryFloat( prefs, key[k++], "0.05" ); // DISTOX_CLOSE_DISTANCE
    mExtendThr     = tryFloat( prefs, key[k++], "10"   ); // DISTOX_EXTEND_THR2
    mVThreshold    = tryFloat( prefs, key[k++], "80"   ); // DISTOX_VTHRESHOLD

    parseStationPolicy( prefs, app, prefs.getString( key[k++], "1" ) ); // DISTOX_SURVEY_STATION
    mDataBackup    = prefs.getBoolean( key[k++], false ); // DISTOX_DATA_BACKUP

    if ( prefs.getString( key[k++], UNIT_LENGTH ).equals(UNIT_LENGTH) ) {
      mUnitLength = 1.0f;
      mUnitLengthStr = "m";
    } else {
      mUnitLength = TopoDroidUtil.M2FT;
      mUnitLengthStr = "ft";
    }
    if ( prefs.getString( key[k++], UNIT_ANGLE ).equals(UNIT_ANGLE) ) {
      mUnitAngle = 1.0f;
      mUnitAngleStr = "deg";
    } else {
      mUnitAngle = TopoDroidUtil.DEG2GRAD;
      mUnitAngleStr = "grad";
    }
  
    mAccelerationThr = tryFloat( prefs, key[k++], "1" );  // DISTOX_ACCEL_PERCENT
    mMagneticThr     = tryFloat( prefs, key[k++], "1" );  // DISTOX_MAG_PERCENT
    mDipThr          = tryFloat( prefs, key[k++], "2"   );  // DISTOX_DIP_THR

    setLoopClosure( tryInt(   prefs, key[k++], "0" ) );     // DISTOX_LOOP_CLOSURE_VALUE

    mCheckAttached = prefs.getBoolean( key[k++], false );   // DISTOX_CHECK_ATTACHED
    mCheckExtend   = prefs.getBoolean( key[k++], true  );   // DISTOX_CHECK_EXTEND
    mPrevNext      = prefs.getBoolean( key[k++], true );    // DISTOX_PREV_NEXT
    mMaxShotLength = tryFloat( prefs, key[k++], "50"   );   // DISTOX_MAX_SHOT_LENGTH
    mMinLegLength  = tryFloat( prefs, key[k++], "0.5" );    // DISTOX_MIN_LEG_LENGTH
    mDistoXBackshot= prefs.getBoolean( key[k++], false );   // DISTOX_BACKSHOT
    mNoCursor      = prefs.getBoolean( key[k++], false  );  // DISTOX_NO_CURSOR
    mRecentTimeout = tryInt(   prefs, key[k++], "30" );     // DISTOX_RECENT_TIMEOUT

    mUnitLocation  = prefs.getString( key[k++], "ddmmss" ).equals("ddmmss") ? TDConst.DDMMSS  // choice
                                                                            : TDConst.DEGREE;
    mCRS           = prefs.getString( key[k++], "Long-Lat" );                 // DISTOX_CRS

    // ------------------- CALIBRATION PREFERENCES
    mGroupBy       = tryInt(   prefs, key[k++], "1" );       // DISTOX_GROUP_BY choice: 0, 1, 2
    mGroupDistance = tryFloat( prefs, key[k++], "40" );      // DISTOX_GROUP_DISTANCE
    mCalibEps      = tryFloat( prefs, key[k++], CALIB_EPS );
    mCalibMaxIt    = tryInt(   prefs, key[k++], "200"     );  // DISTOX_CALIB_MAX_IT
    mCalibShotDownload = prefs.getBoolean( key[k++], true );  // DISTOX_CALIB_SHOT_DOWNLOAD

    // mRawData       = prefs.getBoolean( key[k++], false );    // DISTOX_RAW_DATA 20
    mRawCData      = tryInt( prefs, key[k++], "0" );         // DISTOX_RAW_CDATA 20
    mCalibAlgo     = tryInt( prefs, key[k++], "0" );         // choice: 0, 1, 2

    // -------------------  DRAWING PREFERENCES -def----fallback------min/max
    mAutoStations  = prefs.getBoolean( key[k++], true );            // DISTOX_AUTO_STATIONS 
    mSelectness    = tryFloat( prefs, key[k++], "24" );             // DISTOX_CLOSENESS
    mEraseness     = tryFloat( prefs, key[k++], "36" );             // DISTOX_ERASENESS
    mMinShift      = tryInt(   prefs, key[k++], "60" );             // DISTOX_MIN_SHIFT
    mPointingRadius= tryInt(   prefs, key[k++], "16" );             // DISTOX_POINTING
    mLineSegment   = tryInt(   prefs, key[k++], "10" );             // DISTOX_LINE_SEGMENT
    mLineSegment2  = mLineSegment * mLineSegment;
    mLineAccuracy  = tryFloat( prefs, key[k++], "1" );              // DISTOX_LINE_ACCURACY
    mLineCorner    = tryFloat( prefs, key[k++], "20"  );            // DISTOX_LINE_CORNER
    setLineStyleAndType( prefs.getString( key[k++], LINE_STYLE ) ); // DISTOX_LINE_STYLE
    mContinueLine  = tryInt(   prefs, key[k++], "0" );              // DISTOX_LINE_CONTINUE
    mUnitIcons     = tryFloat( prefs, key[k++], "1.4" );            // DISTOX_DRAWING_UNIT 
    mPickerType    = tryInt(   prefs, key[k++], "0" );              // DISTOX_PICKER_TYPE choice: 0, 1, 2
    mHThreshold    = tryFloat( prefs, key[k++], "70" );             // DISTOX_HTHRESHOLD
    mStationSize   = tryFloat( prefs, key[k++], "20" );             // DISTOX_STATION_SIZE
    mLabelSize     = tryFloat( prefs, key[k++], "24" );             // DISTOX_LABEL_SIZE
    mLineThickness = tryFloat( prefs, key[k++], "1"  );             // DISTOX_LINE_THICKNESS
    mAutoSectionPt = prefs.getBoolean( key[k++], false );           // DISTOX_AUTO_SECTION_PT
    mBackupNumber  = tryInt(   prefs, key[k++], "5" );              // DISTOX_BACKUP_NUMBER
    mBackupInterval = tryInt(  prefs, key[k++], "60" );             // DISTOX_BACKUP_INTERVAL
    mSharedXSections = prefs.getBoolean( key[k++], false );         // DISTOX_SHARED_XSECTIONS
    // mPlotCache       = prefs.getBoolean( key[k++], true  );         // DISTOX_PLOT_CACHE

    mTimerWait     = tryInt(   prefs, key[k++], "10" );             // DISTOX_SHOT_TIMER
    mBeepVolume    = tryInt(   prefs, key[k++], "50" );             // DISTOX_BEEP_VOLUME
    mMinNrLegShots = tryInt(   prefs, key[k++], "3" );              // DISTOX_LEG_SHOTS choice: 2, 3, 4

    // ------------------- SKETCH PREFERENCES
    mSketchModelType = tryInt(  prefs, key[k++], "1" );             // DISTOX_SKETCH_MODEL_TYPE
    mSketchSideSize = tryFloat( prefs, key[k++], "0.5" );           // DISTOX_SKETCH_LINE_STEP
    mDeltaExtrude   = tryFloat( prefs, key[k++], "50"  );           // DISTOX_DELTA_EXTRUDE
    // mSketchUsesSplays  = prefs.getBoolean( key[k++], false );    
    // mSketchBorderStep  = Float.parseFloat( prefs.getString( key[k++], "0.2") );
    // mSketchSectionStep = Float.parseFloat( prefs.getString( key[k++], "0.5") );

    mCompassReadings   = tryInt(   prefs, key[k++], "4" );    // DISTOX_COMPASS_READING 
    mLRExtend          = prefs.getBoolean( key[k++], true );  // DISTOX_SPLAY_EXTEND
    mLRUDvertical      = tryFloat( prefs, key[k++], "0" );   // DISTOX_LRUD_VERTICAL
    mLRUDhorizontal    = tryFloat( prefs, key[k++], "90" );   // DISTOX_LRUD_HORIZONTAL

    mBitmapScale       = tryFloat( prefs, key[k++], "1.5" );  // DISTOX_BITMAP_SCALE 
    mBezierStep        = tryFloat( prefs, key[k++], "0.2" );  // DISTOX_BEZIER_STEP
    mThumbSize         = tryInt(   prefs, key[k++], "200" );  // DISTOX_THUMBNAIL
    mDotRadius         = tryFloat( prefs, key[k++], "5"   );  // DISTOX_DOT_RADIUS
    mFixedThickness    = tryFloat( prefs, key[k++], "1"   );  // DISTOX_FIXED_THICKNESS
    mArrowLength       = tryFloat( prefs, key[k++], "8"   );  // DISTOX_ARROW_LENGTH
    mExportShotsFormat = tryInt(   prefs, key[k++], "-1" );   // DISTOX_EXPORT_SHOTS choice: 
    mExportPlotFormat  = tryInt(   prefs, key[k++], "-1" );   // DISTOX_EXPORT_PLOT choice: 14, 2, 11, 12, 13
    mTherionMaps       = prefs.getBoolean( key[k++], false ); // DISTOX_THERION_MAPS
    mSvgGrid           = prefs.getBoolean( key[k++], false ); // DISTOX_SVG_GRID
    mSvgLineDirection  = prefs.getBoolean( key[k++], false ); // DISTOX_SVG_LINE_DIR
    // mSvgInHtml         = prefs.getBoolean( key[k++], false ); // DISTOX_SVG_IN_HTML
    mSvgPointStroke    = tryFloat( prefs, key[k++], "0.1" );  // DISTOX_SVG_POINT_STROKE
    mSvgLabelStroke    = tryFloat( prefs, key[k++], "0.3" );  // DISTOX_SVG_LABEL_STROKE
    mSvgLineStroke     = tryFloat( prefs, key[k++], "0.5" );  // DISTOX_SVG_LINE_STROKE
    mSvgGridStroke     = tryFloat( prefs, key[k++], "0.5" );  // DISTOX_SVG_GRID_STROKE
    mSvgShotStroke     = tryFloat( prefs, key[k++], "0.5" );  // DISTOX_SVG_SHOT_STROKE
    mSvgLineDirStroke  = tryFloat( prefs, key[k++], "2.9" );  // DISTOX_SVG_LINEDIR_STROKE

    mKmlStations       = prefs.getBoolean( key[k++], true );  // DISTOX_KML_STATIONS
    mKmlSplays         = prefs.getBoolean( key[k++], false ); // DISTOX_KML_SPLAYS
    mSplayVertThrs     = tryFloat( prefs, key[k++], "80"  );  // DISTOX_SPLAY_VERT_THRS

    mBacksightInput = prefs.getBoolean( key[k++], false );   // DISTOX_BACKSIGHT
    // setMagAnomaly( prefs, prefs.getBoolean( key[k++], false ) ); // DISTOX_MAG_ANOMALY

    mDashSplay = prefs.getBoolean( key[k++], true );              // DISTOX_DASH_SPLAY
    mVertSplay = tryFloat( prefs, key[k++], "50" );               // DISTOX_VERT_SPLAY
    mHorizSplay = tryFloat( prefs, key[k++], "60" );              // DISTOX_HORIZ_SPLAY
    mCosHorizSplay = TDMath.cosd( mHorizSplay );

    mSectionSplay = tryFloat( prefs, key[k++], "60" );            // DISTOX_SECTION_SPLAY
    mExportStationsPrefix =  prefs.getBoolean( key[k++], false ); // DISTOX_STATION_PREFIX
    mStationNames = (prefs.getString( key[k++], "alpha").equals("number"))? 1 : 0; // DISTOX_STATION_NAMES

    // setZoomControls( prefs.getBoolean( key[k++], false ) ); // DISTOX_ZOOM_CONTROLS
    setZoomControls( prefs.getString( key[k++], "1"), FeatureChecker.checkMultitouch(app) ); // DISTOX_ZOOM_CTRL
    mSideDrag = prefs.getBoolean( key[k++], false );          // DISTOX_SIDE_DRAG

    // mDxfScale    = tryFloat( prefs, key[k++], "1.0" );        // DISTOX_DXF_SCALE
    mAcadVersion = tryInt(   prefs, key[k++], "9" );          // DISTOX_ACAD_VERSION choice: 9, 13

    setBitmapBgcolor( prefs.getString( key[k++], "0 0 0" ) ); // DISTOX_BITMAP_BGCOLOR

    mSurvexEol    = ( prefs.getString(  key[k++], "LF" ).equals("LF") )? "\n" : "\r\n";  // DISTOX_SURVEX_EOL
    mSurvexSplay  =   prefs.getBoolean( key[k++], false );    // DISTOX_SURVEX_SPLAY
    mSurvexLRUD   =   prefs.getBoolean( key[k++], false );    // DISTOX_SURVEX_LRUD
    mSwapLR       =   prefs.getBoolean( key[k++], false );    // DISTOX_SWAP_LR
    mUnscaledPoints = prefs.getBoolean( key[k++], false );    // DISTOX_UNSCALED_POINTS
    mUnitGrid       = tryFloat(  prefs, key[k++], "1" );      // DISTOX_UNIT_GRID
    // mXTherionAreas  = prefs.getBoolean( key[k++], false );    // DISTOX_XTHERION_AREAS
    mTherionSplays  = prefs.getBoolean( key[k++], false );    // DISTOX_THERION_SPLAYS
    mCompassSplays  = prefs.getBoolean( key[k++], true );     // DISTOX_COMPASS_SPLAYS

    mRecentNr   = tryInt( prefs, key[k++], "4" );               // DISTOX_RECENT_NR choice: 3, 4, 5, 6

    mAreaBorder = prefs.getBoolean( key[k++], true );         // DISTOX_AREA_BORDER

    mOrthogonalLRUDAngle  = tryFloat( prefs, key[k++], "0");  // DISTOX_ORTHO_LRUD
    mReduceAngle          = tryFloat( prefs, key[k++], "45"); // DISTOX_REDUCE_ANGLE
    mReduceCosine = (float)Math.cos( mReduceAngle * TDMath.DEG2RAD );

    mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
    mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 

    // mSectionStations  = tryInt( prefs, key[k++], "3");         // DISTOX_SECTION_STATIONS

    mWallsType        = tryInt(   prefs, key[k++], "0" );     // DISTOX_WALLS_TYPE choice: 0, 1
    mWallsPlanThr     = tryFloat( prefs, key[k++], "70"  );   // DISTOX_WALLS_PLAN_THR
    mWallsExtendedThr = tryFloat( prefs, key[k++], "45"  );   // DISTOX_WALLS_EXTENDED_THR
    mWallsXClose      = tryFloat( prefs, key[k++], "0.1" );   // DISTOX_WALLS_XCLOSE
    mWallsXStep       = tryFloat( prefs, key[k++], "1.0" );   // DISTOX_WALLS_XSTEP
    mWallsConcave     = tryFloat( prefs, key[k++], "0.1" );   // DISTOX_WALLS_CONCAVE

    mDxfBlocks        =  prefs.getBoolean( key[k++], true );  // DISTOX_DXF_BLOCKS

    mAlgoMinAlpha     = tryFloat( prefs, key[k++], "0.1" );   // DISTOX_ALGO_MIN_ALPHA
    mAlgoMinBeta      = tryFloat( prefs, key[k++], "4.0" );   // DISTOX_ALGO_MIN_BETA
    mAlgoMinGamma     = tryFloat( prefs, key[k++], "1.0" );   // DISTOX_ALGO_MIN_GAMMA
    mAlgoMinDelta     = tryFloat( prefs, key[k++], "1.0" );   // DISTOX_ALGO_MIN_DELTA
  }

  static private void setActivityBooleans( SharedPreferences prefs, Context ctx, int level )
  {
    if ( StationPolicy.policyDowngrade( level ) ) {
      setPreference( prefs, "DISTOX_SURVEY_STATION", "1" );
    }
    TDLevel.setLevel( ctx, level );
    int policy = StationPolicy.policyUpgrade( level );
    if ( policy > 0 ) {
      setPreference( prefs, "DISTOX_SURVEY_STATION", Integer.toString( policy ) );
    }
    // if ( ! TDLevel.overExpert ) {
    //   mMagAnomaly = false; // magnetic anomaly compensation requires level overExpert
    // }
  }

  static void checkPreference( SharedPreferences prefs, String k, MainWindow main_window, TopoDroidApp app )
  {
    int nk = 0; // key index
    float f;
    // int i;
    // Log.v(TopoDroidApp.TAG, "onSharePreferenceChanged " + k );

    // ---------------- PRIMARY PREFERENCES ---------------------------
    if ( k.equals( key[ nk++ ] ) ) {                     // DISTOX_EXTRA_BUTTONS
      int level = tryInt( prefs, k, "1" );
      if ( level != TDLevel.mLevel ) {
        setActivityBooleans( prefs, app, level );
        if ( main_window != null ) {
          main_window.resetButtonBar();
          // FIXME TOOLBAR main_window.resetToolbar();
          main_window.setMenuAdapter( app.getResources() );
        }  
      }
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_SIZE_BUTTONS
      if ( setSizeButtons( app, tryInt( prefs, k, defaultButtonSize ) ) ) {
        if ( main_window != null ) main_window.resetButtonBar();
          // FIXME TOOLBAR main_window.resetToolbar();
      }
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_TEXT_SIZE
      setTextSize( app, tryInt( prefs, k, defaultTextSize ) );
  
    } else if ( k.equals( key[ nk++ ] ) ) {
      mKeyboard = prefs.getBoolean( k, true );           // DISTOX_MKEYBOARD
    } else if ( k.equals( key[ nk++ ] ) ) {
      mDefaultTeam     = prefs.getString( k, "" );       // DISTOX_TEAM
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLocalManPages = handleLocalUserMan( prefs.getString( k, "0" ), true, app );      // DISTOX_LOCAL_MAN
    } else if ( k.equals( key[ nk++ ] ) ) {
      boolean co_survey = prefs.getBoolean( k, false );  // DISTOX_COSURVEY
      if ( co_survey != TopoDroidApp.mCoSurveyServer ) {
        app.setCoSurvey( co_survey ); // set flag and start/stop server
      }
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_INIT_STATION
      mInitStation = prefs.getString( k, "0" ).replaceAll("\\s+", "");
      if ( mInitStation.length() == 0 ) mInitStation = "0";
      DistoXStationName.setInitialStation( mInitStation );
    } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_AZIMUTH_MANUAL
      mAzimuthManual = prefs.getBoolean( k, false ); 
      TDAzimuth.resetRefAzimuth( app.mShotWindow, TDAzimuth.mRefAzimuth );

    } else if ( k.equals( key[ nk++ ] ) ) {
      // mDevice      = mData.getDevice( prefs.getString( k, "" ) );  // DISTOX_DEVICE - UNUSED HERE
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCheckBT        = tryInt( prefs, k, "1" );         // DISTOX_BLUETOOTH (choice)

    } else if ( k.equals( key[ nk++ ] ) ) {     // DISTOX_LOCALE
      app.setLocale( prefs.getString( k, "" ), true );
    } else if ( k.equals( key[ nk++ ] ) ) {     // DISTOX_CWD
      app.setCWD( prefs.getString( k, "TopoDroid" ), prefs.getString( "DISTOX_CBD", TDPath.PATH_BASEDIR ) );

    // ---------------- SECOINDARY PREFERENCES ---------------------------
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSockType       = tryInt( prefs, k, mDefaultSockStrType );     // "DISTOX_SOCK_TYPE (choice)
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCommRetry      = tryInt( prefs, k, "1" );     // DISTOX_COMM_RETRY
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWaitLaser      = tryInt( prefs, k, "1000" );  // DISTOX_WAIT_LASER
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWaitShot       = tryInt( prefs, k, "4000" );  // DISTOX_WAIT_SHOT
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWaitData       = tryInt( prefs, k, "100" );   // DISTOX_WAIT_DATA
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWaitConn       = tryInt( prefs, k, "500" );   // DISTOX_WAIT_CONN
    } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_Z6_WORKAROUND
      mZ6Workaround = prefs.getBoolean( k, true );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mConnectionMode = tryInt( prefs, k, "0" );     // DISTOX_CONN_MODE (choice)
    } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_AUTO_PAIR
      mAutoPair = prefs.getBoolean( k, true );
      app.checkAutoPairing();
    } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_SOCKET_DELAY
      mConnectSocketDelay = tryInt(prefs, k, "0" );  
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAutoReconnect = prefs.getBoolean( k, false ); // DISTOX_AUTO_RECONNECT
    } else if ( k.equals( key[ nk++ ] ) ) {
      mHeadTail = prefs.getBoolean( k, false );      // DISTOX_HEAD_TAIL
  

    } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_CLOSE_DISTANCE
      mCloseDistance = tryFloat( prefs, k, "0.05" );
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mExtendThr     = tryFloat( prefs, k, "10" );   // DISTOX_EXTEND_THR2
    } else if ( k.equals( key[ nk++ ] ) ) {
      mVThreshold    = tryFloat( prefs, k, "80" );   // DISTOX_VTHRESHOLD
    } else if ( k.equals( key[ nk++ ] ) ) {
      parseStationPolicy( prefs, app, prefs.getString( k, "1" ) ); // DISTOX_SURVEY_STATION
    } else if ( k.equals( key[ nk++ ] ) ) {
      mDataBackup = prefs.getBoolean( k, false );      // DISTOX_DATA_BACKUP
    } else if ( k.equals( key[ nk++ ] ) ) {
      if ( prefs.getString( k, UNIT_LENGTH ).equals(UNIT_LENGTH) ) {
        mUnitLength = 1.0f;
        mUnitLengthStr = "m";
      } else {
        mUnitLength = TopoDroidUtil.M2FT;
        mUnitLengthStr = "ft";
      }
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitLength changed " + mUnitLength );
    } else if ( k.equals( key[ nk++ ] ) ) {
      if ( prefs.getString( k, UNIT_ANGLE ).equals(UNIT_ANGLE) ) {
        mUnitAngle = 1.0f;
        mUnitAngleStr = "deg";
      } else {
        mUnitAngle = TopoDroidUtil.DEG2GRAD;
        mUnitAngleStr = "grad";
      }
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitAngle changed " + mUnitAngle );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAccelerationThr = tryFloat( prefs, k, "1" );    // DISTOX_ACCEL_PERCENT 
    } else if ( k.equals( key[ nk++ ] ) ) {
      mMagneticThr     = tryFloat( prefs, k, "1" );    // DISTOX_MAG_PERCENT
    } else if ( k.equals( key[ nk++ ] ) ) {
      mDipThr          = tryFloat( prefs, k, "2" );      // DISTOX_DIP_THR
  
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_LOOP_CLOSURE_VALUE
      setLoopClosure( tryInt( prefs, k, "0" ) );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCheckAttached = prefs.getBoolean( k, false );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCheckExtend   = prefs.getBoolean( k, true );
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_PREV_NEXT
      mPrevNext = prefs.getBoolean( k, true );
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_MAX_SHOT_LENGTH
      mMaxShotLength = tryFloat( prefs, k, "50" );  
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_MIN_LEG_LENGTH
      mMinLegLength = tryFloat( prefs, k, "0.5" );  
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_BACKSHOT
      mDistoXBackshot = prefs.getBoolean( k, false );
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_NO_CURSOR
      mNoCursor = prefs.getBoolean( k, false );
    } else if ( k.equals( key[ nk++ ] ) ) {              // DISTOX_RECENT_TIMEOUT
      mRecentTimeout = tryInt(   prefs, k, "30" );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitLocation  = prefs.getString( k, "ddmmss" ).equals("ddmmss") ? TDConst.DDMMSS
                                                                       : TDConst.DEGREE;
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   try {
    //     mAltitude = Integer.parseInt( prefs.getString( k, ALTITUDE ) ); // DISTOX_ALTITUDE 15
    //   } catch ( NumberFormatException e ) { mAltitude = _WGS84; }
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCRS = prefs.getString( k, "Long-Lat" );     // DISTOX_CRS

    } else if ( k.equals( key[ nk++ ] ) ) {
      mGroupBy       = tryInt(   prefs, k, "1" );  // DISTOX_GROUP_BY (choice)
    } else if ( k.equals( key[ nk++ ] ) ) {
      mGroupDistance = tryFloat( prefs, k, "40" );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCalibEps      = tryFloat( prefs, k, CALIB_EPS );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCalibMaxIt    = tryInt(   prefs, k, "200" );   // DISTOX_CALIB_MAX_IT
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCalibShotDownload = prefs.getBoolean( k, true );  // DISTOX_CALIB_SHOT_DOWNLOAD
    } else if ( k.equals( key[ nk++ ] ) ) {
      // mRawData       = prefs.getBoolean( k, false );  // DISTOX_RAW_DATA
      mRawCData      = tryInt( prefs, k, "0" );       // DISTOX_RAW_iCDATA
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCalibAlgo     = tryInt( prefs, k, "0" );       // DISTOX_CALIB_ALGO

    } else if ( k.equals( key[ nk++ ] ) ) {
      mAutoStations = prefs.getBoolean( k, true );    // DISTOX_AUTO_STATIONS
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSelectness   = tryFloat( prefs, k, "24" );
    } else if ( k.equals( key[ nk++ ] ) ) {           // DISTOX_ERASENESS
      mEraseness    = tryFloat( prefs, k, "36" );
    } else if ( k.equals( key[ nk++ ] ) ) {           // DISTOX_MIN_SHIFT
      mMinShift     = tryInt(  prefs, k, "60" );
    } else if ( k.equals( key[ nk++ ] ) ) {           // DISTOX_POINTING
      mPointingRadius= tryInt(   prefs, k, "16" );
    } else if ( k.equals( key[ nk++ ] ) ) {           // DISTOX_LINE_SEGMENT
      mLineSegment  = tryInt(   prefs, k, "10" );
      mLineSegment2 = mLineSegment * mLineSegment;
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLineAccuracy = tryFloat( prefs, k, "1" );
    } else if ( k.equals( key[ nk++ ] ) ) {           // DISTOX_LINE_CORNER
      mLineCorner   = tryFloat( prefs, k, "20" );
    } else if ( k.equals( key[ nk++ ] ) ) {                      // DISTOX_LINE_STYLE
      setLineStyleAndType( prefs.getString( k, LINE_STYLE ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                      // DISTOX_LINE_CONTINUE
      mContinueLine  = tryInt( prefs, k, "0" );
    } else if ( k.equals( key[ nk++ ] ) ) {                      // DISTOX_DRAWING_UNIT
      try {
        setDrawingUnit( app, Float.parseFloat( prefs.getString( k, "1.4" ) ) );
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_PICKER_TYPE
      mPickerType = tryInt(   prefs, k, "0" );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mHThreshold = tryFloat( prefs, k, "70" );  // DISTOX_HTHRESHOLD
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        setStationSize( Float.parseFloat( prefs.getString( k, "20" ) ) ); // DISTOX_STATION_SIZE
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        setLabelSize( Float.parseFloat( prefs.getString( k, "24" ) ) ); // DISTOX_LABEL_SIZE
      } catch ( NumberFormatException e ) { }
      // FIXME changing label size affects only new labels
      //       not existing labels (until they are edited)
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "1" ) );  // DISTOX_LINE_THICKNESS
        if ( f >= 0.5f && f != mLineThickness ) {
          mLineThickness = f;
          BrushManager.reloadLineLibrary( app.getResources() );
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAutoSectionPt = prefs.getBoolean( k, false ); // DISTOX_AUTO_SECTION_PT
    } else if ( k.equals( key[ nk++ ] ) ) {
      mBackupNumber  = tryInt( prefs, k, "5" );      // DISTOX_BACKUP_NUMBER
    } else if ( k.equals( key[ nk++ ] ) ) {
      mBackupInterval = tryInt( prefs, k, "60" );    // DISTOX_BACKUP_INTERVAL
    
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSharedXSections  = prefs.getBoolean( k, false ); // DISTOX_SHARED_XSECTIONS
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mPlotCache        = prefs.getBoolean( k, true  ); // DISTOX_PLOT_CACHE

    } else if ( k.equals( key[ nk++ ] ) ) {
      mTimerWait        = tryInt( prefs, k, "10" );  // DISTOX_SHOT_TIMER
    } else if ( k.equals( key[ nk++ ] ) ) {
      mBeepVolume       = tryInt( prefs, k, "50" );  // DISTOX_BEEP_VOLUME
    } else if ( k.equals( key[ nk++ ] ) ) {
      mMinNrLegShots    = tryInt( prefs, k, "3" );   // DISTOX_LEG_SHOTS (choice)

    } else if ( k.equals( key[ nk++ ] ) ) {
      mSketchModelType = tryInt(  prefs, k, "1" );    // DISTOX_SKETCH_MODEL_TYPE
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSketchSideSize = tryFloat( prefs, k, "0.5" );  // 0.5 meter // DISTOX_SKETCH_LINE_STEP
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mDeltaExtrude = tryFloat( prefs, k, "50" );     // DISTOX_DELTA_EXTRUDE
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCompassReadings = tryInt( prefs, k, "4" );     // DISTOX_COMPASS_READINGS
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLRExtend = prefs.getBoolean( k, true );        // DISTOX_SPLAY_EXTEND
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLRUDvertical = tryFloat( prefs, k, "0" );     // DISTOX_LRUD_VERTICAL
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLRUDhorizontal = tryFloat( prefs, k, "90" );   // DISTOX_LRUD_HORIZONTAL

    } else if ( k.equals( key[ nk++ ] ) ) {
      mBitmapScale = tryFloat( prefs, k, "1.5" );     // DISTOX_BITMAP_SCALE
    } else if ( k.equals( key[ nk++ ] ) ) {
      mBezierStep  = tryFloat( prefs, k, "0.2" );     // DISTOX_BEZIER_STEP
    } else if ( k.equals( key[ nk++ ] ) ) {
      mThumbSize = tryInt( prefs, k, "200" );         // DISTOX_THUMBNAIL
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mDotRadius = tryFloat( prefs, k, "5" );         // DISTOX_DOT_RADIUS
    } else if ( k.equals( key[ nk++ ] ) ) { 
      try {
        f = Float.parseFloat( prefs.getString( k, "1" ) ); // DISTOX_FIXED_THICKNESS
        if ( f >= 0.5f && f <= 10 && f != mFixedThickness ) {
          mFixedThickness = f;
          BrushManager.setStrokeWidths();
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mArrowLength = tryFloat( prefs, k, "8" );      // DISTOX_ARROW_LENGTH
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mExportShotsFormat = tryInt( prefs, k, "-1" ); // DISTOX_EXPORT_SHOTS (choice)
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mExportPlotFormat = tryInt( prefs, k, "-1" );  // DISTOX_EXPORT_PLOT (choice)
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mTherionMaps = prefs.getBoolean( k, false );   // DISTOX_THERION_MAPS
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mSvgGrid = prefs.getBoolean( k, false );       // DISTOX_SVG_GRID
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mSvgLineDirection = prefs.getBoolean( k, false ); // DISTOX_SVG_LINE_DIR
    // } else if ( k.equals( key[ nk++ ] ) ) { 
    //   mSvgInHtml = prefs.getBoolean( k, false );     // DISTOX_SVG_IN_HTML
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mSvgPointStroke    = tryFloat( prefs, k, "0.1" );  // DISTOX_SVG_POINT_STROKE
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mSvgLabelStroke    = tryFloat( prefs, k, "0.3" );  // DISTOX_SVG_LABEL_STROKE
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mSvgLineStroke     = tryFloat( prefs, k, "0.5" );  // DISTOX_SVG_LINE_STROKE
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mSvgGridStroke     = tryFloat( prefs, k, "0.5" );  // DISTOX_SVG_GRID_STROKE
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mSvgShotStroke     = tryFloat( prefs, k, "0.5" );  // DISTOX_SVG_SHOT_STROKE
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mSvgLineDirStroke  = tryFloat( prefs, k, "2.0" );  // DISTOX_SVG_LINEDIR_STROKE

    } else if ( k.equals( key[ nk++ ] ) ) { 
      mKmlStations = prefs.getBoolean( k, true );    // DISTOX_KML_STATIONS
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mKmlSplays = prefs.getBoolean( k, false );     // DISTOX_KML_SPLAYS

    } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_SPLAY_VERT_THRS
      mSplayVertThrs = tryFloat( prefs, k, "80" );
    } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_BACKSIGHT
      mBacksightInput = prefs.getBoolean( k, false );
    // } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_MAG_ANOMALY
    //   setMagAnomaly( prefs, prefs.getBoolean( k, false ) );
    } else if ( k.equals( key[ nk++ ] ) ) {          // DISTOX_DASH_SPLAY
      mDashSplay = prefs.getBoolean( k, true );      
    } else if ( k.equals( key[ nk++ ] ) ) {
      mVertSplay = tryFloat( prefs, k, "50" );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mHorizSplay = tryFloat( prefs, k, "60" );
      mCosHorizSplay = TDMath.cosd( mHorizSplay );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSectionSplay = tryFloat( prefs, k, "60" );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mExportStationsPrefix = prefs.getBoolean( k, false ); // DISTOX_STATION_PREFIX
    } else if ( k.equals( key[ nk++ ] ) ) {
      mStationNames = (prefs.getString( k, "alpha").equals("number"))? 1 : 0; // DISTOX_STATION_NAMES
    } else if ( k.equals( key[ nk++ ] ) ) {
      // setZoomControls( prefs.getBoolean( k, false ) ); // DISTOX_ZOOM_CONTROLS
      setZoomControls( prefs.getString( k, "1"), FeatureChecker.checkMultitouch(app) ); // DISTOX_ZOOM_CTRL
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSideDrag = prefs.getBoolean( k, false ); // DISTOX_SIDE_DRAG
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mDxfScale = tryFloat( prefs, k, "1" );   // DISTOX_DXF_SCALE
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        mAcadVersion = Integer.parseInt( prefs.getString( k, "9") ); // DISTOX_ACAD_VERSION
      } catch ( NumberFormatException e) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      setBitmapBgcolor( prefs.getString( k, "0 0 0" ) ); // DISTOX_BITMAP_BGCOLOR
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSurvexEol = ( prefs.getString( k, "LF" ).equals("LF") )? "\n" : "\r\n";  // DISTOX_SURVEX_EOL
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSurvexSplay = prefs.getBoolean( k, false ); // DISTOX_SURVEX_SPLAY
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSurvexLRUD = prefs.getBoolean( k, false ); // DISTOX_SURVEX_LRUD
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSwapLR = prefs.getBoolean( k, false ); // DISTOX_SWAP_LR
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnscaledPoints = prefs.getBoolean( k, false ); // DISTOX_UNSCALED_POINTS
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_UNIT_GRID
      mUnitGrid = Float.parseFloat( prefs.getString( k, "1" ) ); 
    // } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_XTHERION_AREAS
    //   mXTherionAreas = prefs.getBoolean( k, false );   
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_THERION_SPLAYS
      mTherionSplays  = prefs.getBoolean( k, false );   
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_COMPASS_SPLAYS
      mCompassSplays  = prefs.getBoolean( k, true );   
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_RECENT_NR
      mRecentNr = tryInt( prefs, k, "4" );

    } else if ( k.equals( key[ nk++ ] ) ) {
      mAreaBorder = prefs.getBoolean( k, true );  // DISTOX_AREA_BORDER

    } else if ( k.equals( key[ nk++ ] ) ) {       // DISTOX_ORTHO_LRUD
      mOrthogonalLRUDAngle  = tryFloat( prefs, k, "0");
      mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
      mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    } else if ( k.equals( key[ nk++ ] ) ) {       // DISTOX_REDUCE_ANGLE
      mReduceAngle  = tryFloat( prefs, k, "45");
      mReduceCosine = (float)Math.cos( mReduceAngle * TDMath.DEG2RAD );

    // } else if ( k.equals( key[ nk++ ] ) ) {       // DISTOX_SECTION_STATIONS
    //   mSectionStations = tryInt( prefs, k, "3");

    } else if ( k.equals( key[ nk++ ] ) ) {       // DISTOX_WALLS_TYPE
      mWallsType = tryInt(prefs, k, "0" );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWallsPlanThr = tryFloat( prefs, k, "70" ); // DISTOX_WALLS_PLAN_THR
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWallsExtendedThr = tryFloat( prefs, k, "45" ); // DISTOX_WALLS_EXTENDED_THR
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWallsXClose = tryFloat( prefs, k, "0.1" ); // DISTOX_WALLS_XCLOSE
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWallsXStep = tryFloat( prefs, k, "1.0" ); // DISTOX_WALLS_XSTEP
    } else if ( k.equals( key[ nk++ ] ) ) {
      mWallsConcave = tryFloat( prefs, k, "0.1" ); // DISTOX_WALLS_CONCAVE
 
    } else if ( k.equals( key[ nk++ ] ) ) {
      mDxfBlocks = prefs.getBoolean( k, true ); // DISTOX_DXF_BLOCKS

    } else if ( k.equals( key[ nk++ ] ) ) {
      mAlgoMinAlpha   = tryFloat( prefs, k, "0.1" );   // DISTOX_ALGO_MIN_ALPHA
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAlgoMinBeta    = tryFloat( prefs, k, "4.0" );   // DISTOX_ALGO_MIN_BETA
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAlgoMinGamma   = tryFloat( prefs, k, "1.0" );   // DISTOX_ALGO_MIN_GAMMA
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAlgoMinDelta   = tryFloat( prefs, k, "1.0" );   // DISTOX_ALGO_MIN_DELTA

    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchUsesSplays = prefs.getBoolean( k, false );
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchBorderStep  = Float.parseFloat( prefs.getString( k, "0.2") );
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchSectionStep = Float.parseFloat( prefs.getString( k, "0.5") );
    } else {
      TDLog.checkLogPreferences( prefs, k );
    }
  }

  private static void setLineStyleAndType( String style )
  {
    mLineStyle = LINE_STYLE_TWO; // default
    mLineType  = 1;
    if ( style.equals( "0" ) ) {
      mLineStyle = LINE_STYLE_BEZIER;
      // mLineType  = 1;                 // alreday assigned
    } else if ( style.equals( "1" ) ) {
      mLineStyle = LINE_STYLE_ONE;
      // mLineType  = 1;                 // already assignd
    } else if ( style.equals( "2" ) ) {
      // mLineStyle = LINE_STYLE_TWO; // already assigned
      mLineType  = 2;
    } else if ( style.equals( "3" ) ) {
      mLineStyle = LINE_STYLE_THREE;
      mLineType  = 3;
    }
  }

  private static int parseStationPolicy( SharedPreferences prefs, TopoDroidApp app, String str ) 
  {
    int policy = StationPolicy.SURVEY_STATION_FOREWARD;
    try {
      policy = Integer.parseInt( str );
    } catch ( NumberFormatException e ) {
      policy = StationPolicy.SURVEY_STATION_FOREWARD;
    }
    if ( setStationPolicy( app, policy ) ) {
      // nothing
    } else {
      // preference is reset to the last saved policy
      setPreference( prefs, "DISTOX_SURVEY_STATION", Integer.toString( StationPolicy.savedPolicy() ) );
      if ( TopoDroidApp.mPrefActivitySurvey != null ) TopoDroidApp.mPrefActivitySurvey.reloadPreferences();
    }
    // if ( ! mBacksightShot ) clearMagAnomaly( prefs );
    // Log.v("DistoX", "PARSE Policy " + policy + " saved " + StationPolicy.savedPolicy() );
    return policy;
  }

  private static boolean setStationPolicy( Context context, int policy )
  {
    if ( ! StationPolicy.setPolicy( policy ) ) {
      if ( policy == StationPolicy.SURVEY_STATION_TOPOROBOT ) {
        TDToast.make( R.string.toporobot_warning );
      } else if ( policy == StationPolicy.SURVEY_STATION_TRIPOD ) {
        TDToast.make( R.string.tripod_warning );
      } else if ( policy == StationPolicy.SURVEY_STATION_BACKSIGHT ) {
        // TDToast.make( R.string.backsight_warning );
      } else if ( policy == StationPolicy.SURVEY_STATION_ANOMALY ) {
        TDToast.make( R.string.anomaly_warning );
      } else {
        //
      }
      return false;
    }
    // Log.v("DistoX", "set survey stations. policy " + policy );
    return true;
  }

  // void clearPreferences()
  // {
  //   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( this );
  //   SharedPreferences.Editor editor = sp.edit();
  //   editor.clear();
  //   editor.apply();
  // }

  // centralize api-23 changes
  static void applyEditor( Editor editor )
  {
    editor.apply(); 
    // FIXME_23 editor.commit();
  }
  
  static private void setPreference( SharedPreferences sp, String name, String value )
  {
    Editor editor = sp.edit();
    editor.putString( name, value );
    applyEditor( editor );
  }

  static private void setPreference( SharedPreferences sp, String name, boolean value )
  {
    Editor editor = sp.edit();
    editor.putBoolean( name, value );
    applyEditor( editor );
  }

  static void setPreference( SharedPreferences sp, String name, long value )
  {
    Editor editor = sp.edit();
    editor.putLong( name, value );
    applyEditor( editor );
  }

  static long getLongPreference( SharedPreferences sp, String name, long def_value )
  {
    return sp.getLong( name, def_value ); 
  }

  // static void setPreference( SharedPreferences sp, String name, int val )
  // {
  //   Editor editor = sp.edit();
  //   editor.putString( name, Integer.toString(val) );
  //   applyEditor( editor );
  // }

  // static void setPreference( SharedPreferences sp, String name, float val )
  // {
  //   Editor editor = sp.edit();
  //   editor.putString( name, Float.toString(val) );
  //   applyEditor( editor );
  // }

  // ===================================================================
  // ENFORCE BOUNDS

  static private String parseIntValue( String value, int def, int min )
  {
    int i = def;
    try {
      i = Integer.parseInt( value ); 
      if ( i < min ) i = min;
    } catch ( NumberFormatException e ) { }
    return Integer.toString( i );
  }

  static private String parseIntValue( String value, int def, int min, int max )
  {
    int i = def;
    try {
      i = Integer.parseInt( value ); 
      if ( i < min ) i = min;
      if ( i > max ) i = max;
    } catch ( NumberFormatException e ) { }
    return Integer.toString( i );
  }

  static private String parseFloatValue( String value, float def, float min )
  {
    float i = def;
    try {
      i = (float)(Double.parseDouble( value )); 
      if ( i < min ) i = min;
    } catch ( NumberFormatException e ) {
      TDLog.Error("Parse Float Number exception. Value " + value );
    }
    return Float.toString( i );
  }

  static private String parseFloatValue( String value, float def, float min, float max )
  {
    // TDLog.Error("parse float " + value + " def " + def + " min " + min + " max " + max );
    float i = def;
    try {
      i = (float)(Double.parseDouble( value )); 
      if ( i < min ) i = min;
      if ( i > max ) i = max;
    } catch ( NumberFormatException e ) { 
      TDLog.Error("Parse Float Number exception. Value " + value );
    }
    // Log.v("DistoX", "parse float " + value + " def " + def + " min " + min + " max " + max + " return " + i );
    return Float.toString( i );
  }

  static String enforcePreferenceBounds( String name, String value )
  {
    // Log.v("DistoX", "enforce name " + name + " value <" + value + ">" );

    // if ( name.equals( "DISTOX_COSURVEY" )
    //S if ( name.equals( "DISTOX_INIT_STATION" )
    //B if ( name.equals( "DISTOX_AZIMUTH_MANUAL" )

    if ( name.equals( "DISTOX_TEXT_SIZE" ) ) return parseIntValue( value, mTextSize, MIN_SIZE_TEXT );
    if ( name.equals( "DISTOX_CLOSE_DISTANCE" ) ) return parseFloatValue( value, mCloseDistance, 0.0001f );
    if ( name.equals( "DISTOX_EXTEND_THR2"    ) ) return parseFloatValue( value, mExtendThr, 0, 90 );
    if ( name.equals( "DISTOX_VTHRESHOLD"     ) ) return parseFloatValue( value, mVThreshold, 0, 90 );
    //C if ( name.equals( "DISTOX_SURVEY_STATION" ) 
    //B if ( name.equals( "DISTOX_DATA_BACKUP" ) 
    //C if ( name.equals( "DISTOX_UNIT_LENGTH" )
    //C if ( name.equals( "DISTOX_UNIT_ANGLE" )
    if ( name.equals( "DISTOX_ACCEL_PERCENT"  ) ) return parseFloatValue( value, mAccelerationThr, 0 );
    if ( name.equals( "DISTOX_MAG_PERCENT"    ) ) return parseFloatValue( value, mMagneticThr, 0 );
    if ( name.equals( "DISTOX_DIP_THR"        ) ) return parseFloatValue( value, mDipThr, 0 );
    //B if ( name.equals( "DISTOX_LOOP_CLOSURE_VALUE" ) 
    //B if ( name.equals( "DISTOX_CHECK_ATTACHED" )
    //B if ( name.equals( "DISTOX_CHECK_EXTEND" )
    //B if ( name.equals( "DISTOX_PREV_NEXT" )
    if ( name.equals( "DISTOX_MAX_SHOT_LENGTH") ) return parseFloatValue( value, mMaxShotLength, 20 );
    if ( name.equals( "DISTOX_MIN_LEG_LENGTH") ) return parseFloatValue( value, mMinLegLength, 0, 5 );
    //B if ( name.equals( "DISTOX_BACKSHOT" )
    //B if ( name.equals( "DISTOX_NO_CURSOR" )
    if ( name.equals( "DISTOX_RECENT_TIMEOUT"  ) ) return parseIntValue( value, mRecentTimeout, 10, 1200 ); // min 10 secs, max 20 minutes

    //C if ( name.equals( "DISTOX_UNIT_LOCATION" )
    //S if ( name.equals( "DISTOX_CRS" )

    //C if ( name.equals( "DISTOX_GROUP_BY" )
    if ( name.equals( "DISTOX_GROUP_DISTANCE" ) ) return parseFloatValue( value, mGroupDistance, 0 );
    if ( name.equals( "DISTOX_CALIB_EPS"      ) ) return parseFloatValue( value, mCalibEps, 0.000001f );
    if ( name.equals( "DISTOX_CALIB_MAX_IT"   ) ) return parseIntValue( value, mCalibMaxIt, 10 );
    //B if ( name.equals( "DISTOX_CALIB_SHOT_DOWNLOAD" )
    // //B if ( name.equals( "DISTOX_RAW_DATA" )
    //C if ( name.equals( "DISTOX_RAW_CDATA" )
    //C if ( name.equals( "DISTOX_CALIB_ALGO" )

    //S if ( name.equals( "DISTOX_DEVICE" )
    //C if ( name.equals( "DISTOX_BLUETOOTH" )
    //C if ( name.equals( "DISTOX_SOCK_TYPE" )
    if ( name.equals( "DISTOX_COMM_RETRY"    ) ) return parseIntValue( value, mCommRetry, 1, 5 );
    if ( name.equals( "DISTOX_WAIT_LASER"    ) ) return parseIntValue( value, mWaitLaser, 100,  5000 );
    if ( name.equals( "DISTOX_WAIT_SHOT"     ) ) return parseIntValue( value, mWaitShot,  100, 10000 );
    if ( name.equals( "DISTOX_WAIT_DATA"     ) ) return parseIntValue( value, mWaitData,  0,   2000 );
    if ( name.equals( "DISTOX_WAIT_CONN"     ) ) return parseIntValue( value, mWaitConn,  50,  2000 );
    //C if ( name.equals( "DISTOX_CONN_MODE" )

    // if ( name.equals( "DISTOX_AUTO_STATIONS" )
    if ( name.equals( "DISTOX_CLOSENESS"      ) ) return parseFloatValue( value, mSelectness,   1 );
    if ( name.equals( "DISTOX_ERASENESS"      ) ) return parseFloatValue( value, mEraseness,    1 );
    if ( name.equals( "DISTOX_MIN_SHIFT"      ) ) return parseIntValue( value, mMinShift,      10 );
    if ( name.equals( "DISTOX_POINTING"       ) ) return parseIntValue( value, mPointingRadius, 1 );
    if ( name.equals( "DISTOX_LINE_SEGMENT"   ) ) return parseIntValue( value, mLineSegment,    1 );
    if ( name.equals( "DISTOX_LINE_ACCURACY"  ) ) return parseFloatValue( value, mLineAccuracy, 0.1f );
    if ( name.equals( "DISTOX_LINE_CORNER"    ) ) return parseFloatValue( value, mLineCorner,   0.1f );
    // if ( name.equals( "DISTOX_LINE_STYLE" ) 
    // if ( name.equals( "DISTOX_LINE_CONTINUE" ) 
    // if ( name.equals( "DISTOX_DRAWING_UNIT" )
    // if ( name.equals( "DISTOX_PICKER_TYPE" )
    if ( name.equals( "DISTOX_HTHRESHOLD"     ) ) return parseFloatValue( value, mHThreshold,    0, 90 );
    if ( name.equals( "DISTOX_STATION_SIZE"   ) ) return parseFloatValue( value, mStationSize,   1 );
    if ( name.equals( "DISTOX_LABEL_SIZE"     ) ) return parseFloatValue( value, mLabelSize,     1 );
    if ( name.equals( "DISTOX_LINE_THICKNESS" ) ) return parseFloatValue( value, mLineThickness, 1, 10 );

    if ( name.equals( "DISTOX_BACKUP_NUMBER"  ) ) return parseIntValue( value, mBackupNumber,    4, 10 );
    if ( name.equals( "DISTOX_BACKUP_INTERVAL") ) return parseIntValue( value, mBackupInterval,  5, 600 );
    // if ( name.equals( "DISTOX_SHARED_XSECTIONS") ) 
    // if ( name.equals( "DISTOX_PLOT_CACHE") ) 

    // if ( name.equals( "DISTOX_TEAM" )
    // if ( name.equals( "DISTOX_LOCAL_MAN" )
    if ( name.equals( "DISTOX_SHOT_TIMER"     ) ) return parseIntValue( value, mTimerWait, 0 );
    if ( name.equals( "DISTOX_BEEP_VOLUME"    ) ) return parseIntValue( value, mBeepVolume, 10, 100 );
    // if ( name.equals( "DISTOX_LEG_SHOTS" )

    //C if ( name.equals( "DISTOX_SKETCH_MODEL_TYPE" )
    if ( name.equals( "DISTOX_SKETCH_LINE_STEP" ) ) return parseFloatValue( value, mSketchSideSize,  0.01f );
    if ( name.equals( "DISTOX_DELTA_EXTRUDE"    ) ) return parseFloatValue( value, mDeltaExtrude,    0.01f );
    if ( name.equals( "DISTOX_COMPASS_READINGS" ) ) return parseIntValue(   value, mCompassReadings, 1 );

    //B if ( name.equals( "DISTOX_SPLAY_EXTEND" )
    if ( name.equals( "DISTOX_LRUD_VERTICAL"    ) ) return parseFloatValue( value, mLRUDvertical,    0f, 91f );
    if ( name.equals( "DISTOX_LRUD_HORIZONTAL"  ) ) return parseFloatValue( value, mLRUDhorizontal,  0f, 91f );

    //B if ( name.equals( "DISTOX_AUTO_RECONNECT" )
    //B if ( name.equals( "DISTOX_HEAD_TAIL" )
    if ( name.equals( "DISTOX_BITMAP_SCALE"     ) ) return parseFloatValue( value, mBitmapScale,    0.5f, 100f );
    if ( name.equals( "DISTOX_BEZIER_STEP"      ) ) return parseFloatValue( value, mBezierStep,     0f,   2f ); // N.B. 0 = disable Bezier Step
    if ( name.equals( "DISTOX_THUMBNAIL"        ) ) return parseIntValue(   value, mThumbSize,      80,   400 );
    if ( name.equals( "DISTOX_DOT_RADIUS"       ) ) return parseFloatValue( value, mDotRadius,      1,    100 );
    if ( name.equals( "DISTOX_FIXED_THICKNESS"  ) ) return parseFloatValue( value, mFixedThickness, 1,    10 );
    if ( name.equals( "DISTOX_ARROW_LENGTH"     ) ) return parseFloatValue( value, mArrowLength,    1,    40 );
    //C if ( name.equals( "DISTOX_EXPORT_SHOTS" )
    //C if ( name.equals( "DISTOX_EXPORT_PLOT" )
    //B if ( name.equals( "DISTOX_THERION_MAPS" )
    //B if ( name.equals( "DISTOX_SVG_GRID" )
    //B if ( name.equals( "DISTOX_SVG_LINE_DIR" )
    //B if ( name.equals( "DISTOX_SVG_IN_HTML" )
    if ( name.equals( "DISTOX_SVG_POINT_STROKE" ) ) return parseFloatValue( value, mSvgPointStroke,   0.01f );
    if ( name.equals( "DISTOX_SVG_LABEL_STROKE" ) ) return parseFloatValue( value, mSvgLabelStroke,   0.01f );
    if ( name.equals( "DISTOX_SVG_LINE_STROKE"  ) ) return parseFloatValue( value, mSvgLineStroke,    0.01f );
    if ( name.equals( "DISTOX_SVG_GRID_STROKE"  ) ) return parseFloatValue( value, mSvgGridStroke,    0.01f );
    if ( name.equals( "DISTOX_SVG_SHOT_STROKE"  ) ) return parseFloatValue( value, mSvgShotStroke,    0.01f );
    if ( name.equals( "DISTOX_SVG_LINEDIR_STROKE" ) ) return parseFloatValue( value, mSvgLineDirStroke, 0.01f );
    //B if ( name.equals( "DISTOX_KML_STATIONS" )
    //B if ( name.equals( "DISTOX_KML_SPLAYS" )

    if ( name.equals( "DISTOX_SPLAY_VERT_THRS"  ) ) return parseFloatValue( value, mSplayVertThrs, 0, 91 );
    //B if ( name.equals( "DISTOX_BACKSIGHT" )
    //B if ( name.equals( "DISTOX_Z6_WORKAROUND" )
    //B if ( name.equals( "DISTOX_MAG_ANOMALY" )
    //B if ( name.equals( "DISTOX_DASH_SPLAY"       ) 
    if ( name.equals( "DISTOX_VERT_SPLAY"       ) ) return parseFloatValue( value, mVertSplay, 0, 91 );
    if ( name.equals( "DISTOX_HORIZ_SPLAY"      ) ) return parseFloatValue( value, mHorizSplay, 0, 91 );
    if ( name.equals( "DISTOX_SECTION_SPLAY"    ) ) return parseFloatValue( value, mSectionSplay, 0, 91 );
    //B if ( name.equals( "DISTOX_STATION_PREFIX" )
    //C if ( name.equals( "DISTOX_STATION_NAMES" )
    //B if ( name.equals( "DISTOX_TROBOT_NAMES" )
    //C if ( name.equals( "DISTOX_ZOOM_CTRL" )
    //B if ( name.equals( "DISTOX_SIDE_DRAG" )
    //B if ( name.equals( "DISTOX_MKEYBOARD" )
    // if ( name.equals( "DISTOX_DXF_SCALE"    ) ) return parseFloatValue( value, mDxfScale, 0.1f, 10f );
    //C if ( name.equals( "DISTOX_ACAD_VERSION" )
    //X if ( name.equals( "DISTOX_BITMAP_BGCOLOR" )
    //B if ( name.equals( "DISTOX_AUTO_PAIR" )
    if ( name.equals( "DISTOX_SOCKET_DELAY"    ) ) return parseIntValue( value, mConnectSocketDelay, 0, 100 );
    //C if ( name.equals( "DISTOX_SURVEX_EOL" )
    //B if ( name.equals( "DISTOX_SURVEX_SPLAY" )
    //B if ( name.equals( "DISTOX_SURVEX_LRUD" )
    //B if ( name.equals( "DISTOX_SWAP_LR" )
    //B if ( name.equals( "DISTOX_UNSCALED_POINTS" )
    //C if ( name.equals( "DISTOX_UNIT_GRID" ) 
    //B if ( name.equals( "DISTOX_XTHERION_AREAS" )
    //B if ( name.equals( "DISTOX_THERION_SPLAYS" )
    //B if ( name.equals( "DISTOX_COMPASS_SPLAYS" )
    //C if ( name.equals( "DISTOX_RECENT_NR" )
    //B if ( name.equals( "DISTOX_AREA_BORDER" )
    if ( name.equals( "DISTOX_ORTHO_LRUD" ) ) return parseFloatValue( value, mOrthogonalLRUDAngle, 0, 90 );
    if ( name.equals( "DISTOX_REDUCE_ANGLE" ) ) return parseFloatValue( value, mReduceAngle, 0, 90 );

    //C if ( name.equals( "DISTOX_WALLS_TYPE" )
    if ( name.equals( "DISTOX_WALLS_PLAN_THR"     ) ) return parseFloatValue( value, mWallsPlanThr, 0, 90 );
    if ( name.equals( "DISTOX_WALLS_EXTENDED_THR" ) ) return parseFloatValue( value, mWallsExtendedThr, 0, 90 );
    if ( name.equals( "DISTOX_WALLS_XCLOSE"       ) ) return parseFloatValue( value, mWallsXClose, 0 );
    if ( name.equals( "DISTOX_WALLS_XSTEP"        ) ) return parseFloatValue( value, mWallsXStep, 0 );
    if ( name.equals( "DISTOX_WALLS_CONCAVE"      ) ) return parseFloatValue( value, mWallsConcave, 0 );

    // if ( name.equals( "DISTOX_DXF_BLOCKS" )  // DISTOX_DXF_BLOCKS

    if ( name.equals( "DISTOX_ALGO_MIN_ALPHA"     ) ) return parseFloatValue( value, mAlgoMinAlpha, 0, 1 );
    if ( name.equals( "DISTOX_ALGO_MIN_BETA"      ) ) return parseFloatValue( value, mAlgoMinBeta,  0 );
    if ( name.equals( "DISTOX_ALGO_MIN_GAMMA"     ) ) return parseFloatValue( value, mAlgoMinGamma, 0 );
    if ( name.equals( "DISTOX_ALGO_MIN_DELTA"     ) ) return parseFloatValue( value, mAlgoMinDelta, -10 );

    //C if ( name.equals( "DISTOX_LOCALE" )
    //A if ( name.equals( "DISTOX_CWD"    )
    return value;
  }

  static boolean handleLocalUserMan( String man, boolean download, TopoDroidApp app ) 
  {
    int idx = Integer.parseInt( man ); // no throw
    if ( idx > 0 && idx < 5 ) { 
      if ( download && FeatureChecker.checkInternet( app ) ) { // download user manual 
       	int[] res = {
	         0,
	         R.string.user_man_fr,
	         R.string.user_man_es,
	         R.string.user_man_it,
	         R.string.user_man_ru
	       };
        String url = app.getResources().getString( res[idx] );
       	if ( url != null && url.length() > 0 ) {
          // try do download the zip
          (new UserManDownload( app, url )).execute();
	       }
      }
      return true;
    }
    return false;
  }

}
