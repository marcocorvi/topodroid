/** @file TopoDroidSetting.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid application settings (preferenceces)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


import android.content.SharedPreferences;

class TopoDroidSetting
{

  // ---------------------------------------------------------
  // PREFERENCES KEYS

  static final String[] key = { // prefs keys
    "DISTOX_EXTRA_BUTTONS",       //  0 TODO move to general options
    "DISTOX_SIZE_BUTTONS",        //  1
    "DISTOX_TEXT_SIZE",           //  2
    // ------------------- SURVEY PREFERENCES
    "DISTOX_CLOSE_DISTANCE",      //  3
    "DISTOX_EXTEND_THR2",         //  4
    "DISTOX_VTHRESHOLD",          //  5 // LRUD vertical threshold
    "DISTOX_SURVEY_STATION",      //  6 // DISTOX_SURVEY_STATIONS must not be used
    "DISTOX_UNIT_LENGTH",
    "DISTOX_UNIT_ANGLE",
    "DISTOX_ACCEL_THR",           //  9 // shot quality thresholds
    "DISTOX_MAG_THR",
    "DISTOX_DIP_THR",             // 11
    "DISTOX_LOOP_CLOSURE",        // 12 // whether to close loop
    "DISTOX_CHECK_ATTACHED",      // 13

    "DISTOX_UNIT_LOCATION",       // 14 
    "DISTOX_CRS",                 // 16

    "DISTOX_GROUP_BY",            // 18
    "DISTOX_GROUP_DISTANCE",
    "DISTOX_CALIB_EPS",           // 20
    "DISTOX_CALIB_MAX_IT",
    "DISTOX_RAW_DATA",            // 22
    "DISTOX_CALIB_ALGO",          // 23

    "DISTOX_DEVICE",              // 24 N.B. indexKeyDeviceName - USED by TopoDroidApp to store the device
    "DISTOX_BLUETOOTH",           // 25
    "DISTOX_SOCK_TYPE",
    "DISTOX_COMM_RETRY",          // 27
    // "DISTOX_BOOTLOADER",       // UNUSED
    "DISTOX_CONN_MODE",           // 28

    "DISTOX_AUTO_STATIONS",       //  
    "DISTOX_CLOSENESS",           // 30
    "DISTOX_LINE_SEGMENT",
    "DISTOX_LINE_ACCURACY",
    "DISTOX_LINE_CORNER",         // 33
    "DISTOX_LINE_STYLE",          // 
    "DISTOX_DRAWING_UNIT",        // 
    "DISTOX_PICKER_TYPE",         // 36
    "DISTOX_HTHRESHOLD",          // UNUSED
    "DISTOX_STATION_SIZE",        // 
    "DISTOX_LABEL_SIZE",          // 
    "DISTOX_LINE_THICKNESS",      // 40

    "DISTOX_TEAM",                   // 41
    // "DISTOX_ALTIMETRIC",          // UNUSED
    "DISTOX_SHOT_TIMER",             // 42 // bearing-clino timer
    "DISTOX_BEEP_VOLUME",            // 43
    "DISTOX_LEG_SHOTS",              // nr. of shots to make a leg
    "DISTOX_COSURVEY",

    "DISTOX_SKETCH_LINE_STEP",       // 46
    "DISTOX_DELTA_EXTRUDE",          // 
    "DISTOX_COMPASS_READINGS",       // 

    "DISTOX_SPLAY_EXTEND",           // 49 // whether to set extend to splay shots
    "DISTOX_AUTO_RECONNECT",         // 50
    "DISTOX_BITMAP_SCALE",           // 51
    "DISTOX_THUMBNAIL",              // 
    "DISTOX_DOT_RADIUS",             // 
    "DISTOX_FIXED_THICKNESS",        // 54
    "DISTOX_ARROW_LENGTH",           // 55
    "DISTOX_EXPORT_SHOTS",           // 56

    "DISTOX_SPLAY_VERT_THRS",        // over mSplayVertThrs splays are not displayed in plan view
    "DISTOX_INIT_STATION",           // default initial station for sketches
    "DISTOX_BACKSIGHT",
    "DISTOX_Z6_WORKAROUND",          // 60
    "DISTOX_MAG_ANOMALY",            // whether to compensate magnetic anomaly
    "DISTOX_AZIMUTH_MANUAL",         // 62
    "DISTOX_VERT_SPLAY",             // over this splay are shown with dashed line
    "DISTOX_STATION_PREFIX",         // whether to add cave-name prefix to stations (cSurvey)
    "DISTOX_STATION_NAMES",
    "DISTOX_ZOOM_CTRL",
    "DISTOX_SIDE_DRAG",              // whether to enable side-drag
    "DISTOX_MKEYBOARD",
    "DISTOX_DXF_SCALE", 
    "DISTOX_ACAD_VERSION",
    "DISTOX_BITMAP_BGCOLOR",
    "DISTOX_AUTO_PAIR",
    "DISTOX_SOCKET_DELAY",
    "DISTOX_SURVEX_EOL",         // survex end of line
    "DISTOX_SURVEX_SPLAY",
    "DISTOX_SURVEX_LRUD",  
    "DISTOX_UNSCALED_POINTS",     // unscaled drawing point items
    "DISTOX_UNIT_GRID",
    "DISTOX_XTHERION_AREAS",      // save areas a-la xtherion
    "DISTOX_RECENT_NR",           // number of most recent items (item picker)
    "DISTOX_AREA_BORDER",         // area border visibility
    "DISTOX_CONT_JOIN",           // line continuation is join
    "DISTOX_CSV_LENGTH",          // CSV export length unit

    "DISTOX_WALLS_TYPE",
    "DISTOX_WALLS_PLAN_THR",
    "DISTOX_WALLS_EXTENDED_THR",
    "DISTOX_WALLS_XCLOSE",
    "DISTOX_WALLS_XSTEP",
    "DISTOX_WALLS_CONCAVE",

    "DISTOX_LOCALE",                 // 
    "DISTOX_CWD",                    // must be last 

    // "DISTOX_SKETCH_USES_SPLAYS",  // 
    // "DISTOX_SKETCH_BERDER_STEP",
    // "DISTOX_SKETCH_SECTION_STEP", // 

  };

  // static final int indexKeyDeviceName = 24;
  // static String keyDeviceName() { return key[indexKeyDeviceName]; }
  static String keyDeviceName() { return key[24]; }

  // static final  String EXPORT_TYPE    = "th";    // DISTOX_EXPORT_TH

  // prefs default values

  // static int mScreenTimeout = 60000; // 60 secs
  static int mTimerCount = 10; // Acc/Mag timer countdown (secs)
  static int mBeepVolume = 50; // beep volume
  
  final static int CONN_MODE_BATCH = 0; // DistoX connection mode
  final static int CONN_MODE_CONTINUOUS = 1;
  static int mConnectionMode = CONN_MODE_BATCH; 
  static boolean mZ6Workaround = true;

  static boolean mBinaryTh2 = false;

  static int mCompassReadings = 4; // number of compass readings to average

  // static final String CLOSE_DISTANCE = "0.05"; // 50 cm / 1000 cm
  static float mCloseDistance = 1.0f; // FIXME kludge

  static int    mMinNrLegShots = 2;
  static String mInitStation = "0";
  static boolean mBacksight = false;     // whether to check backsight
  static boolean mBacksightShot = false; // backsight shooting policy
  static boolean mMagAnomaly = false;    // local magnetic anomaly survey
  static float  mSplayVertThrs = 80;
  static boolean mAzimuthManual = false; // whether to manually set extend / or use reference azimuth
  static float mVertSplay = 50;
  static boolean mExportStationsPrefix = false; // whether to prepend cave name to station in cSurvey export
  static int mAcadVersion = 9; // AutoCAD version 9, or 13

  static int mStationNames = 0;          // type of station names (0: alpha, 1: number)
  // static boolean mZoomControls = false;
  static int mZoomCtrl = 0;
  static boolean mSideDrag = false;
  static boolean mKeyboard = true;

  // static void setZoomControls( boolean ctrl )
  // {
  //   mZoomControls = ctrl;
  //   // FIXME forward setting to DrawingActivity
  // }
  static void setZoomControls( String ctrl, boolean is_multitouch )
  {
    try {
      int i = Integer.parseInt( ctrl );
      if ( i >= 0 && i <= 2 ) mZoomCtrl = i;
      if ( mZoomCtrl == 0 && ! is_multitouch ) mZoomCtrl = 1;
    } catch ( NumberFormatException e ) { }
  }

  // selection_radius = cutoff + closeness / zoom
  static final float mCloseCutoff = 0.01f; // minimum selection radius

  static float mCloseness = 24f;             // selection radius
  static int mThumbSize = 200;               // thumbnail size

  static final int LEVEL_BASIC    = 0;
  static final int LEVEL_NORMAL   = 1;
  static final int LEVEL_ADVANCED = 2;
  static final int LEVEL_EXPERIMENTAL = 3;
  static final int LEVEL_COMPLETE     = 4;
  static int mActivityLevel = 1;
  static boolean mLevelOverBasic        = true;
  static boolean mLevelOverNormal       = false;
  static boolean mLevelOverAdvanced     = false;
  static boolean mLevelOverExperimental = false;

  static int mSizeButtons = 1;   // action bar buttons scale (either 1 or 2)

  static int mTextSize = 14;     // list text size 

  static String mSurvexEol = "\n";
  static boolean mSurvexSplay = false;
  static boolean mSurvexLRUD  = false;
  static float mCsvLengthUnit = 1; // meters

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // LOCATION

  // static final int ALT_WGS84 = 0; // WGS84 altitude
  // static final int ALT_ASL = 1;   // altimetric altitude
  // static final String ALTITUDE = "0";  
  // static int mAltitude = ALT_WGS84;     // location altitude type

  // static boolean mAltimetricLookup = false; // whether to lookup altimetric atitude

  static String mCRS = "Long-Lat";    // default coord ref systen 

  // static final  String UNIT_LOCATION  = "ddmmss";
  static int mUnitLocation = 0; // 0 dec-degree, 1 ddmmss

  // FIXME GPS_AVERAGE NOT USED
  // static final  boolean USE_GPSAVERAGING = false;
  // static boolean mUseGPSAveraging = false;

  static String  mDefaultTeam = "";

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // CALIBRATION

  static final String GROUP_DISTANCE = "40.0";
  static float mGroupDistance = 40f;

  static final float DISTOX_MAX_EPS  = 0.01f; // hard limit
  static final String CALIB_EPS      = "0.000001";
  static float mCalibEps = 0.000001f; // calibartion epsilon

  static int   mCalibMaxIt = 200;     // calibration max nr of iterations

  // calibration data grouping policies
  static final int GROUP_BY_DISTANCE = 0;
  static final int GROUP_BY_FOUR     = 1;
  static final int GROUP_BY_ONLY_16  = 2;
  static final String GROUP_BY  = "1";     // GROUP_BY_FOUR
  static int mGroupBy = GROUP_BY_FOUR;  // how to group calib data

  static boolean mRawData = false;   // whether to display calibration raw data as well
  static int   mCalibAlgo = 0;   // calibration algorithm: 0 auto, 1 linear, 2 non-linear

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DEVICE
  // static boolean mBootloader = false;  // whether to show bootloader menu
  static boolean mAutoReconnect = false;
  static boolean mAutoPair = true;
  static int mConnectSocketDelay = 100; // wait time if not paired [0.1 sec]

  // static final boolean CHECK_BT = true;
  static int mCheckBT = 1;        // BT: 0 disabled, 1 check on start, 2 enabled

  static final int TOPODROID_SOCK_DEFAULT      = 0;    // BT socket type
  static final int TOPODROID_SOCK_INSEC        = 1;
  static final int TOPODROID_SOCK_PORT         = 2;
  static final int TOPODROID_SOCK_INSEC_PORT   = 3;
  // static final int TOPODROID_SOCK_INSEC_INVOKE = 4;
  static int mSockType = TOPODROID_SOCK_DEFAULT; // FIXME static
  static int mCommRetry = 1; 
  static int mCommType  = 0; // 0: on-demand, 1: continuous

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SHOTS
  static float mVThreshold = 80f;   // verticality threshold (LRUD)

  static float mHThreshold;  // horizontal plot threshold

  static boolean mCheckAttached = false;    // whether to check is there are shots non-attached

  static int mExportShotsFormat = 5; // TopoDroidExport.EXPORT_THERION

  static final String SURVEY_STATION = "1"; 
  static int     mSurveyStations = 1; // automatic survey stations: 0 no, 1 forward-after-splay, 2 backward-after-splay
  static boolean mShotAfterSplays = true;  //                       3 forward-before-splay, 4 backward-before-splay

  static boolean isSurveyForward() { return (mSurveyStations%2) == 1; }
  static boolean isSurveyBackward() { return mSurveyStations>0 && (mSurveyStations%2) == 0; }

  static boolean mLoopClosure = false;  // whether to do loop closure

  static boolean mLRExtend = true;  // whether to extend LR or not (Compass/VisualTopo input)
  
  static final  String UNIT_LENGTH    = "meters";
  static final  String UNIT_ANGLE     = "degrees";
  // static final  String UNIT_ANGLE_GRADS = "grads";
  // static final  String UNIT_ANGLE_SLOPE = "slope";
  // conversion factor from internal units (m) to user units
  static float mUnitLength = 1;
  static float mUnitAngle  = 1;

  // static final String EXTEND_THR = "30"; // extend vertically splays in [90-30, 90+30] of the leg
  static float mExtendThr = 30;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SKETCH

  // static final String LINE_SHIFT = "20.0";
  static float mUnitGrid = 1;
  static int mRecentNr = 4;
  static boolean mXTherionAreas = false;

  static final int PICKER_RECENT = 0; // Drawing-tools picker type
  static final int PICKER_LIST   = 1; 
  static final int PICKER_GRID   = 2;
  static int mPickerType = PICKER_LIST;

  static final int LINE_STYLE_BEZIER = 0;  // drawing line styles
  static final int LINE_STYLE_ONE    = 1;
  static final int LINE_STYLE_TWO    = 2;
  static final int LINE_STYLE_THREE  = 3;
  static final String LINE_STYLE     = "0";     // LINE_STYLE_BEZIER
  static int mLineStyle = LINE_STYLE_BEZIER;    

  static int mLineType;        // line type:  1       1     2    3

  static float mStationSize = 20;     // size of station names [pt]
  static float mLabelSize = 24;       // size of labels [pt]
  static float mFixedThickness = 1;   // width of fixed lines
  static float mLineThickness = 1;    // witdh of drawing lines
  static float mDotRadius = 5;
  static float mArrowLength = 8;

  static boolean mUnscaledPoints = false;
  static boolean mAreaBorder = true;
  static boolean mContJoin = false;

  static float mUnit = 1.2f; // drawing unit

  static int   mLineSegment = 10;
  static float mLineAccuracy = 1f;
  static float mLineCorner = 20;     // corner threshold

  static boolean mAutoStations = true; // whether to add stations automatically to scrap therion files

  static float mBitmapScale = 1.5f;
  static float mDxfScale = 1.0f;
  static int mBitmapBgcolor = 0x000000;

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
        

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // 3D
  static float mSketchSideSize;
  static float mDeltaExtrude;
  // static boolean mSketchUsesSplays; // whether 3D models surfaces use splays
  // static float mSketchBorderStep;
  // static float mSketchSectionStep;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DATA ACCURACY
  static float mAccelerationThr = 400; // acceleration threshold (shot quality)
  static float mMagneticThr     = 300; // magnetic threshold
  static float mDipThr          = 3;  // dip threshold

  // void clearPreferences()
  // {
  //   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( this );
  //   if ( sp.getBoolean( "update_required", true ) ) {
  //     SharedPreferences.Editor editor = sp.edit();
  //     editor.clear();
  //     // TODO make other updates
  //     editor.putBoolean( "update_required", false );
  //     editor.commit();
  //   }
  // }

  static private void setMagAnomaly( boolean val )
  {
    mMagAnomaly = val;
    if ( mMagAnomaly && mSurveyStations > 0 ) {
      mBacksightShot = true;
      mSurveyStations = 1;
      mShotAfterSplays = true;
    }
  }
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // WALLS

  static final int WALLS_NONE = 0;
  static final int WALLS_CONVEX = 1;
  static final int WALLS_LAST = 1; // placeholder
  static int mWallsType = WALLS_NONE;
  static float mWallsPlanThr = 70;
  static float mWallsExtendedThr = 45;
  static float mWallsXClose = 0.1f;
  static float mWallsXStep  = 1.0f;
  static float mWallsConcave = 0.1f;

  // ------------------------------------------------------------------
  static private float tryFloat( SharedPreferences prefs, String key, String def_value, float cur_value )
  {
    try {
      return Float.parseFloat( prefs.getString( key, def_value ) );
    } catch ( NumberFormatException e ) { }
    return cur_value;
  }

  static private float tryFloat( SharedPreferences prefs, String key, String def_value, float cur_value, float min_value )
  {
    try {
      float f = Float.parseFloat( prefs.getString( key, def_value ) );
      if ( f >= min_value ) return f;
    } catch ( NumberFormatException e ) { }
    return cur_value;
  }

  static private float tryFloat( SharedPreferences prefs, String key, String def_value,
                                 float cur_value, float min_value, float max_value )
  {
    try {
      float f = Float.parseFloat( prefs.getString( key, def_value ) );
      if ( f >= min_value && f <= max_value ) return f;
    } catch ( NumberFormatException e ) { }
    return cur_value;
  }


  static private int tryInt( SharedPreferences prefs, String key, String def_value, int cur_value )
  {
    try {
      return Integer.parseInt( prefs.getString( key, def_value ) );
    } catch ( NumberFormatException e ) { }
    return cur_value;
  }

  static private int tryInt( SharedPreferences prefs, String key, String def_value, int cur_value, int min_value )
  {
    try {
      int i = Integer.parseInt( prefs.getString( key, def_value ) );
      if ( i > min_value ) return i;
    } catch ( NumberFormatException e ) { }
    return cur_value;
  }

  static private int tryInt( SharedPreferences prefs, String key, String def_value,
                             int cur_value, int min_value, int max_value )
  {
    try {
      int i = Integer.parseInt( prefs.getString( key, def_value ) );
      if ( i > min_value && i <= max_value ) return i;
    } catch ( NumberFormatException e ) { }
    return cur_value;
  }

  static void loadPreferences( TopoDroidApp app, SharedPreferences prefs )
  {
    // ------------------- GENERAL PREFERENCES
    int k = 0;
    // float f;
    // int i;

    mActivityLevel = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_EXTRA_BUTTONS
    setActivityBooleans( prefs );

    mSizeButtons = tryInt( prefs, key[k++], "1", mSizeButtons  ); // choice: cannot fail
    mTextSize    = tryInt( prefs, key[k++], "14", mTextSize, 1 );

    // ------------------- SURVEY PREFERENCES
    mCloseDistance = tryFloat( prefs, key[k++], "0.05", mCloseDistance, 0.1f ); // DISTOX_CLOSE_DISTANCE 
    mExtendThr  = tryFloat( prefs, key[k++], "10", mExtendThr, 0, 90 );         // DISTOX_EXTEND_THR2
    mVThreshold = tryFloat( prefs, key[k++], "80", mVThreshold, 0, 90 );        // DISTOX_VTHRESHOLD

    parseSurveyStations( prefs.getString( key[k++], SURVEY_STATION ) ); // DISTOX_SURVEY_STATIONS 6

    mUnitLength = prefs.getString( key[k++], UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : TopoDroidUtil.M2FT;
    mUnitAngle  = prefs.getString( key[k++], UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : TopoDroidUtil.DEG2GRAD;
  
    mAccelerationThr = tryFloat( prefs, key[k++], "400.0", mAccelerationThr, 0 );  // DISTOX_ACCEL_THR 
    mMagneticThr     = tryFloat( prefs, key[k++], "300.0", mMagneticThr, 0 );      // DISTOX_MAG_THR
    mDipThr          = tryFloat( prefs, key[k++], "3.0",   mDipThr, 0 );           // DISTOX_DIP_THR

    mLoopClosure   = prefs.getBoolean( key[k++], false );                     // DISTOX_LOOP_CLOSURE 12
    mCheckAttached = prefs.getBoolean( key[k++], false );                     // DISTOX_CHECK_ATTACHED

    mUnitLocation  = prefs.getString( key[k++], "ddmmss" ).equals("ddmmss") ? TopoDroidConst.DDMMSS 
                                                                            : TopoDroidConst.DEGREE;
    mCRS           = prefs.getString( key[k++], "Long-Lat" );                        // DISTOX_CRS

    // ------------------- CALIBRATION PREFERENCES
    mGroupBy       = tryInt( prefs, key[k++], GROUP_BY, mGroupBy );     // DISTOX_GROUP_BY (choice)
    mGroupDistance = tryFloat( prefs, key[k++], GROUP_DISTANCE, 40 );   // DISTOX_GROUP_DISTANCE
    mCalibEps      = tryFloat( prefs, key[k++], CALIB_EPS, 0.000001f );
    mCalibMaxIt    = tryInt( prefs, key[k++], "200", 200, 50 );         // DISTOX_CALIB_MAX_IT

    mRawData       = prefs.getBoolean( key[k++], false );               // DISTOX_RAW_DATA 22
    mCalibAlgo     = tryInt( prefs, key[k++], "0", 0 );                 // DISTOX_CALIB_MAX_IT

    // ------------------- DEVICE PREFERENCES
    k++; // DISTOX_DEVICE - UNUSED HERE

    mCheckBT  = tryInt( prefs, key[k++], "1", 1 );     // DISTOX_BLUETOOTH 25
    mSockType = tryInt( prefs, key[k++], "0", 0 );     // DISTOX_SOCK_TYPE 26
    mCommRetry = tryInt( prefs, key[k++], "1", 1, 1, 5 ); // DISTOX_COMM_RETRY 27
    // mBootloader = prefs.getBoolean( key[k++], false );              // DISTOX_BOOTLOADER 28
    mConnectionMode = tryInt( prefs, key[k++], "0", mConnectionMode ); // DISTOX_CONN_MODE 29

    // -------------------  DRAWING PREFERENCES
    mAutoStations = prefs.getBoolean( key[k++], true );         // DISTOX_AUTO_STATIONS 30
    mCloseness    = tryFloat( prefs, key[k++], "24", 24 );   // DISTOX_CLOSENESS
    mLineSegment  = tryInt( prefs, key[k++], "10", 10 );          // DISTOX_LINE_SEGMENT
    mLineAccuracy = tryFloat( prefs, key[k++], "1.0", mLineAccuracy, 0.01f ); // DISTOX_LINE_ACCURACY
    mLineCorner   = tryFloat( prefs, key[k++], "20", mLineCorner, 0.01f );    // DISTOX_LINE_CORNER
    setLineStyleAndType( prefs.getString( key[k++], LINE_STYLE ) );           // DISTOX_LINE_STYLE
    mUnit        = tryFloat( prefs, key[k++], "1.2", 1.2f );                  // DISTOX_DRAWING_UNIT
    mPickerType  = tryInt( prefs, key[k++], "0", 0 );                         // DISTOX_PICKER_TYPE
    mHThreshold  = tryFloat( prefs, key[k++], "70.0", mHThreshold, 0, 90 );   // DISTOX_HTHRESHOLD
    mStationSize = tryFloat( prefs, key[k++], "20.0", mStationSize, 1 );      // DISTOX_STATION_SIZE 39
    mLabelSize   = tryFloat( prefs, key[k++], "24.0", mLabelSize, 1 );        // DISTOX_LABEL_SIZE 40
    mLineThickness = tryFloat( prefs, key[k++], "1.0", mLineThickness, 1 );   // DISTOX_LINE_THICKNESS 41

    mDefaultTeam = prefs.getString( key[k++], "" );                      // DISTOX_TEAM
    mTimerCount = tryInt( prefs, key[k++], "10", mTimerCount, 0 );       // DISTOX_SHOT_TIMER
    mBeepVolume = tryInt( prefs, key[k++], "10", mBeepVolume, 10, 100 ); // DISTOX_BEEP_VOLUME
    mMinNrLegShots = tryInt( prefs, key[k++], "2", mMinNrLegShots, 2 );  // DISTOX_LEG_SHOTS (choice)

    boolean co_survey = prefs.getBoolean( key[k++], false );        // DISTOX_COSURVEY

    // ------------------- SKETCH PREFERENCES
    mSketchSideSize = tryFloat( prefs, key[k++], "0.5", mSketchSideSize );
    mDeltaExtrude   = tryFloat( prefs, key[k++], "50", mDeltaExtrude );
    // mSketchUsesSplays  = prefs.getBoolean( key[k++], false );
    // mSketchBorderStep  = Float.parseFloat( prefs.getString( key[k++], "0.2") );
    // mSketchSectionStep = Float.parseFloat( prefs.getString( key[k++], "0.5") );

    mCompassReadings = tryInt( prefs, key[k++], "4", mCompassReadings, 0 );
    mLRExtend      = prefs.getBoolean( key[k++], true ); 
    mAutoReconnect = prefs.getBoolean( key[k++], false ); 
    mBitmapScale   = tryFloat( prefs, key[k++], "1.5", mBitmapScale, 0.5f, 10f ); // DISTOX_BITMAP_SCALE
    mThumbSize     = tryInt( prefs, key[k++], "200", mThumbSize, 79, 400 );
    mDotRadius     = tryFloat( prefs, key[k++], "5", mDotRadius, 1, 100 );        // DISTOX_DOT_RADIUS
    mFixedThickness = tryFloat( prefs, key[k++], "1", mFixedThickness, 1, 10 );   // DISTOX_FIXED_THICKNESS
    mArrowLength    = tryFloat( prefs, key[k++], "8", mArrowLength, 1, 20 );      // DISTOX_ARROW_LENGTH
    mExportShotsFormat = tryInt( prefs, key[k++], "0", mExportShotsFormat );      // DISTOX_EXPORT_SHOTS (choice)
    mSplayVertThrs = tryFloat( prefs, key[k++], "80", mSplayVertThrs, 0, 91 );    // DISTOX_SPLAY_VERT_THRS

    mInitStation = prefs.getString( key[k++], "0" ).replaceAll("\\s+", "");  // DISTOX_INIT_STATION
    if ( mInitStation.length() == 0 ) mInitStation = "0";
    DistoXStationName.setInitialStation( mInitStation );
    
    mBacksight = prefs.getBoolean( key[k++], false );     // DISTOX_BACKSIGHT
    mZ6Workaround = prefs.getBoolean( key[k++], true );   // DISTOX_Z6_WORKAROUND
    setMagAnomaly( prefs.getBoolean( key[k++], false ) ); // DISTOX_MAG_ANOMALY
    mAzimuthManual = prefs.getBoolean( key[k++], false ); // DISTOX_AZIMUTH_MANUAL
    app.resetRefAzimuth( app.mRefAzimuth );

    mVertSplay = tryFloat( prefs, key[k++], "50", mVertSplay, 0, 91 ); // DISTOX_VERT_SPLAY

    mExportStationsPrefix =  prefs.getBoolean( key[k++], false ); // DISTOX_STATION_PREFIX

    mStationNames = (prefs.getString( key[k++], "alpha").equals("number"))? 1 : 0; // DISTOX_STATION_NAMES

    // setZoomControls( prefs.getBoolean( key[k++], false ) ); // DISTOX_ZOOM_CONTROLS
    setZoomControls( prefs.getString( key[k++], "1"), app.isMultitouch() ); // DISTOX_ZOOM_CTRL

    mSideDrag = prefs.getBoolean( key[k++], false ); // DISTOX_SIDE_DRAG
    mKeyboard = prefs.getBoolean( key[k++], true ); // DISTOX_MKEYBOARD

    mDxfScale = tryFloat( prefs, key[k++], "1.0", mDxfScale, 0.1f, 10f ); // DISTOX_DXF_SCALE
    mAcadVersion = tryInt( prefs, key[k++], "9", mAcadVersion ); // DISTOX_ACAD_VERSION

    setBitmapBgcolor( prefs.getString( key[k++], "0 0 0" ) ); // DISTOX_BITMAP_BGCOLOR

    mAutoPair = prefs.getBoolean( key[ k++ ], true ); // DISTOX_AUTO_PAIR
    mConnectSocketDelay = tryInt(prefs, key[ k++ ], "0", mConnectSocketDelay, -1, 1000 );  // DISTOX_SOCKET_DELAY

    mSurvexEol = ( prefs.getString( key[ k++ ], "LF" ).equals("LF") )? "\n" : "\r\n";  // DISTOX_SURVEX_EOL
    mSurvexSplay = prefs.getBoolean( key[k++], false ); // DISTOX_SURVEX_SPLAY
    mSurvexLRUD  = prefs.getBoolean( key[k++], false ); // DISTOX_SURVEX_LRUD 
    mUnscaledPoints = prefs.getBoolean( key[k++], false ); // DISTOX_UNSCALED_POINTS
    mUnitGrid = tryFloat( prefs, key[k++], "1", mUnitGrid );  // DISTOX_UNIT_GRID
    mXTherionAreas = prefs.getBoolean( key[k++], false );   // DISTOX_XTHERION_AREAS

    mRecentNr = tryInt( prefs, key[k++], "4", mRecentNr, 2, 6 ); // DISTOX_RECENT_NR

    mAreaBorder = prefs.getBoolean( key[k++], true ); // DISTOX_AREA_BORDER
    mContJoin = prefs.getBoolean( key[k++], false ); // DISTOX_CONT_JOIN

    mCsvLengthUnit = tryFloat( prefs, key[k++], "1", mCsvLengthUnit, 0 ); // DISTOX_CSV_LENGTH
    mWallsType = tryInt(prefs, key[k++], "0", mWallsType, 0, WALLS_LAST ); // DISTOX_WALLS_TYPE
    mWallsPlanThr = tryFloat( prefs, key[k++], "70", mWallsPlanThr, 0, 90 );  // DISTOX_WALLS_PLAN_THR
    mWallsExtendedThr = tryFloat( prefs, key[k++], "45", mWallsExtendedThr, 0, 90 ); // DISTOX_WALLS_EXTENDED_THR
    mWallsXClose = tryFloat( prefs, key[k++], "0.1", mWallsXClose, 0 ); // DISTOX_WALLS_XCLOSE
    mWallsXStep  = tryFloat( prefs, key[k++], "1.0", mWallsXStep, 0 ); // DISTOX_WALLS_XSTEP
    mWallsConcave = tryFloat( prefs, key[k++], "0.1", mWallsConcave, 0 ); // DISTOX_WALLS_CONCAVE

    app.setLocale( prefs.getString( key[k++], "" ) );

    // String cwd = prefs.getString( key[k++], "TopoDroid" );
    // if ( ! cwd.equals( mCWD ) ) {
    //   mCWD = cwd;
    //   TopoDroidPath.setPaths( mCWD );
    //   mData.openDatabase();
    // }
  }

  static void setActivityBooleans( SharedPreferences prefs )
  {
    mLevelOverBasic        = mActivityLevel > LEVEL_BASIC;
    mLevelOverNormal       = mActivityLevel > LEVEL_NORMAL;
    mLevelOverAdvanced     = mActivityLevel > LEVEL_ADVANCED;
    mLevelOverExperimental = mActivityLevel > LEVEL_EXPERIMENTAL;
    if ( ! mLevelOverAdvanced ) {
      mMagAnomaly = false;
    }
  }

  static void checkPreference( SharedPreferences prefs, String k, TopoDroidActivity activity, TopoDroidApp app )
  {
    int nk = 0; // key index
    float f;
    // int i;
    // Log.v(TopoDroidApp.TAG, "onSharePreferenceChanged " + k );

    if ( k.equals( key[ nk++ ] ) ) {                           // DISTOX_EXTRA_BUTTONS
      int level = tryInt( prefs, k, "1", mActivityLevel );
      if ( level != mActivityLevel ) {
        mActivityLevel = level;
        setActivityBooleans( prefs );
        if ( activity != null ) {
          activity.resetButtonBar();
          activity.setMenuAdapter( app.getResources() );
        }  
      }
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSizeButtons = tryInt( prefs, k, "1", mSizeButtons );
      if ( activity != null ) activity.resetButtonBar();
    } else if ( k.equals( key[ nk++ ] ) ) {   // DISTOX_TEXT_SIZE
      mTextSize = tryInt( prefs, k, "14", mTextSize, 1 );
  
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCloseDistance = tryFloat( prefs, k, "0.05", mCloseDistance, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mExtendThr = tryFloat( prefs, k, "10", mExtendThr, 0, 90 );   // DISTOX_EXTEND_THR2
    } else if ( k.equals( key[ nk++ ] ) ) {
      mVThreshold = tryFloat( prefs, k, "80", mVThreshold, 0, 90 );
    } else if ( k.equals( key[ nk++ ] ) ) {
      parseSurveyStations( prefs.getString( k, SURVEY_STATION ) ); // DISTOX_SURVEY_STATION 6
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitLength = prefs.getString( k, UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : TopoDroidUtil.M2FT;
      // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitLength changed " + mUnitLength );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitAngle  = prefs.getString( k, UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : TopoDroidUtil.DEG2GRAD;
      // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitAngle changed " + mUnitAngle );
    } else if ( k.equals( key[ nk++ ] ) ) {                        // DISTOX_ACCEL_THR 9
      mAccelerationThr = tryFloat( prefs, k, "400.0", mAccelerationThr, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) {                       // DISTOX_MAG_THR
      mMagneticThr = tryFloat( prefs, k, "300.0", mMagneticThr, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) {                       // DISTOX_DIP_THR 11
      mDipThr = tryFloat( prefs, k, "3.0", mDipThr, 0 );
  
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLoopClosure = prefs.getBoolean( k, false );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCheckAttached = prefs.getBoolean( k, false );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitLocation  = prefs.getString( k, "ddmmss" ).equals("ddmmss") ? TopoDroidConst.DDMMSS
                                                                       : TopoDroidConst.DEGREE;
      // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   try {
    //     mAltitude = Integer.parseInt( prefs.getString( k, ALTITUDE ) ); // DISTOX_ALTITUDE 15
    //   } catch ( NumberFormatException e ) { mAltitude = _WGS84; }
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCRS = prefs.getString( k, "Long-Lat" );     // DISTOX_CRS 16

    } else if ( k.equals( key[ nk++ ] ) ) {
      mGroupBy = tryInt( prefs, k, GROUP_BY, GROUP_BY_FOUR );  // DISTOX_GROUP_BY 18 (choice)
    } else if ( k.equals( key[ nk++ ] ) ) {
      mGroupDistance = tryFloat( prefs, k, GROUP_DISTANCE, mGroupDistance, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCalibEps = tryFloat( prefs, k, CALIB_EPS, mCalibEps, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCalibMaxIt = tryInt( prefs, k, "200", 200, 50 ); // DISTOX_CALIB_MAX_IT
    } else if ( k.equals( key[ nk++ ] ) ) {
      mRawData = prefs.getBoolean( k, false );  // DISTOX_RAW_DATA 22
    } else if ( k.equals( key[ nk++ ] ) ) {     // DISTOX_CALIB_ALGO 23
      mCalibAlgo = tryInt( prefs, k, "0", 0 ); 

    } else if ( k.equals( key[ nk++ ] ) ) {                         // DISTOX_DEVICE - UNUSED HERE
      // mDevice = mData.getDevice( prefs.getString( k, "" ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                         // DISTOX_BLUETOOTH (choice)
      mCheckBT = tryInt(prefs, k, "1", mCheckBT ); 
    } else if ( k.equals( key[ nk++ ] ) ) {                        // "DISTOX_SOCK_TYPE 26 (choice)
      mSockType = tryInt( prefs, k, "0", mSockType );
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_COMM_RETRY 27
      mCommRetry = tryInt( prefs, k, "1", 1, 1, 5 );
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_CONN_MODE (choice)
      mConnectionMode = tryInt( prefs, k, "0", mConnectionMode ); 
  
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAutoStations = prefs.getBoolean( k, true );  // DISTOX_AUTO_STATIONS 30
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCloseness = tryFloat( prefs, k, "24", mCloseness, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLineSegment = tryInt( prefs, k, "10", mLineSegment, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLineAccuracy = tryFloat( prefs, k, "1.0", mLineAccuracy, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLineCorner = tryFloat( prefs, k, "20", mLineCorner, 0.0f );
    } else if ( k.equals( key[ nk++ ] ) ) {                           // STYLE 35
      setLineStyleAndType( prefs.getString( k, LINE_STYLE ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                           // DISTOX_DRAWING_UNIT 36
      try {
        f = Float.parseFloat( prefs.getString( k, "1.2" ) );
        if ( f > 0 && f != mUnit ) {
          mUnit = f;
          DrawingBrushPaths.reloadPointLibrary( app.getResources() );
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_PICKER_TYPE 37
      mPickerType = tryInt( prefs, k, "0", mPickerType );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mHThreshold = tryFloat( prefs, k, "70", mHThreshold, 0, 90 );  // DISTOX_HTHRESHOLD 38
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "20" ) ); // DISTOX_STATION_SIZE 39
        if ( f >= 1 && f != mStationSize ) {
          mStationSize = f;
          DrawingBrushPaths.setTextSizes( );
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "24" ) ); // DISTOX_LABEL_SIZE 40
        if ( f >= 1 && f != mLabelSize ) {
          mLabelSize = f;
          DrawingBrushPaths.setTextSizes( );
        }
      } catch ( NumberFormatException e ) { }
      // FIXME changing label size affects only new labels
      //       not existing labels (until they are edited)
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "1" ) );  // DISTOX_LINE_THICKNESS 41
        if ( f >= 0.5f && f != mLineThickness ) {
          mLineThickness = f;
          DrawingBrushPaths.reloadLineLibrary( app.getResources() );
        }
      } catch ( NumberFormatException e ) { }

    } else if ( k.equals( key[ nk++ ] ) ) {
      mDefaultTeam = prefs.getString( k, "" );              // DISTOX_TEAM
    } else if ( k.equals( key[ nk++ ] ) ) {
      mTimerCount = tryInt( prefs, k, "10", mTimerCount, 0 );  // DISTOX_SHOT_TIMER
    } else if ( k.equals( key[ nk++ ] ) ) {
      mBeepVolume = tryInt( prefs, k, "10", mBeepVolume, 10, 100 );  // DISTOX_BEEP_VOLUME
    } else if ( k.equals( key[ nk++ ] ) ) {
      mMinNrLegShots = tryInt( prefs, k, "2", mMinNrLegShots, 2 );  // DISTOX_LEG_SHOTS (choice)
    } else if ( k.equals( key[ nk++ ] ) ) {
      boolean co_survey = prefs.getBoolean( k, false );               // DISTOX_COSURVEY
      if ( co_survey != app.mCoSurveyServer ) {
        app.setCoSurvey( co_survey ); // set flag and start/stop server
      }

    } else if ( k.equals( key[ nk++ ] ) ) {    // DISTOX_SKETCH_LINE_STEP
      mSketchSideSize = tryFloat( prefs, k, "0.5", mSketchSideSize, 0.01f );  // 0.5 meter
    } else if ( k.equals( key[ nk++ ] ) ) {    // DISTOX_DELTA_EXTRUDE
      mDeltaExtrude = tryFloat( prefs, k, "50", mDeltaExtrude, 0.01f );
    } else if ( k.equals( key[ nk++ ] ) ) {    // DISTOX_COMPASS_READINGS
      mCompassReadings = tryInt( prefs, k, "4", mCompassReadings, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) {   // DISTOX_SPLAY_EXTEND
      mLRExtend = prefs.getBoolean( k, true ); 
    } else if ( k.equals( key[ nk++ ] ) ) {   // DISTOX_AUTO_RECONNECT
      mAutoReconnect = prefs.getBoolean( k, false ); 
    } else if ( k.equals( key[ nk++ ] ) ) {  // DISTOX_BITMAP_SCALE
      mBitmapScale = tryFloat( prefs, k, "1.5", mBitmapScale, 0.5f, 10 );
    } else if ( k.equals( key[ nk++ ] ) ) {  // DISTOX_THUMBNAIL
      mThumbSize = tryInt( prefs, k, "200", mThumbSize, 80, 400 );
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mDotRadius = tryFloat( prefs, k, "5",mDotRadius, 1, 100 ); // DISTOX_DOT_RADIUS
    } else if ( k.equals( key[ nk++ ] ) ) { 
      try {
        f = Float.parseFloat( prefs.getString( k, "1" ) ); // DISTOX_FIXED_THICKNESS
        if ( f >= 0.5f && f <= 10 && f != mFixedThickness ) {
          mFixedThickness = f;
          DrawingBrushPaths.setStrokeWidths();
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mArrowLength = tryFloat( prefs, k, "8", mArrowLength, 1, 20 ); // DISTOX_ARROW_LENGTH
    } else if ( k.equals( key[ nk++ ] ) ) { 
      mExportShotsFormat = tryInt( prefs, k, "0", mExportShotsFormat );  // DISTOX_EXPORT_SHOTS (choice)

    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_SPLAY_VERT_THRS
      mSplayVertThrs = tryFloat( prefs, k, "80", mSplayVertThrs, 0, 91 );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_INIT_STATION
      mInitStation = prefs.getString( k, "0" ).replaceAll("\\s+", "");
      if ( mInitStation.length() == 0 ) mInitStation = "0";
      DistoXStationName.setInitialStation( mInitStation );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_BACKSIGHT
      mBacksight = prefs.getBoolean( k, false );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_Z6_WORKAROUND
      mZ6Workaround = prefs.getBoolean( k, true );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_MAG_ANOMALY
      setMagAnomaly( prefs.getBoolean( k, false ) );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_AZIMUTH_MANUAL
      mAzimuthManual = prefs.getBoolean( k, false ); 
      app.resetRefAzimuth( app.mRefAzimuth );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mVertSplay = tryFloat( prefs, k, "50", mVertSplay, 0, 91 );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mExportStationsPrefix =  prefs.getBoolean( k, false ); // DISTOX_STATION_PREFIX
    } else if ( k.equals( key[ nk++ ] ) ) {
      mStationNames = (prefs.getString( k, "alpha").equals("number"))? 1 : 0; // DISTOX_STATION_NAMES
    } else if ( k.equals( key[ nk++ ] ) ) {
      // setZoomControls( prefs.getBoolean( k, false ) ); // DISTOX_ZOOM_CONTROLS
      setZoomControls( prefs.getString( k, "1"), app.isMultitouch() ); // DISTOX_ZOOM_CTRL
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSideDrag = prefs.getBoolean( k, false ); // DISTOX_SIDE_DRAG
    } else if ( k.equals( key[ nk++ ] ) ) {
      mKeyboard = prefs.getBoolean( k, true ); // DISTOX_MKEYBOARD
    } else if ( k.equals( key[ nk++ ] ) ) {
      mDxfScale = tryFloat( prefs, k, "1.0", mDxfScale, 0.1f, 10f ); // DISTOX_DXF_SCALE
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        mAcadVersion = Integer.parseInt( prefs.getString( k, "9") ); // DISTOX_ACAD_VERSION
      } catch ( NumberFormatException e) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      setBitmapBgcolor( prefs.getString( k, "0 0 0" ) ); // DISTOX_BITMAP_BGCOLOR
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_AUTO_PAIR
      mAutoPair = prefs.getBoolean( k, true ); // DISTOX_AUTO_PAIR
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_SOCKET_DELAY
      mConnectSocketDelay = tryInt(prefs, k, "0", mConnectSocketDelay, -1, 1000 );  
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSurvexEol = ( prefs.getString( k, "LF" ).equals("LF") )? "\n" : "\r\n";  // DISTOX_SURVEX_EOL
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSurvexSplay = prefs.getBoolean( k, false ); // DISTOX_SURVEX_SPLAY
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSurvexLRUD = prefs.getBoolean( k, false ); // DISTOX_SURVEX_LRUD
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnscaledPoints = prefs.getBoolean( k, false ); // DISTOX_UNSCALED_POINTS
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_UNIT_GRID
      mUnitGrid = Float.parseFloat( prefs.getString( k, "1" ) ); 
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_XTHERION_AREAS
      mXTherionAreas = prefs.getBoolean( k, false );   
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_RECENT_NR
      mRecentNr = tryInt( prefs, k, "4", mRecentNr, 2, 6 );

    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_AREA_BORDER
      mAreaBorder = prefs.getBoolean( k, true ); 
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_CONT_JOIN
      mContJoin = prefs.getBoolean( k, false ); 

    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_CSV_LENGTH
      mCsvLengthUnit = tryFloat( prefs, k, "1", mCsvLengthUnit, 0 );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_WALLS_TYPE
      mWallsType = tryInt(prefs, k, "0", mWallsType, 0, WALLS_LAST );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_WALLS_PLAN_THR
      mWallsPlanThr = tryFloat( prefs, k, "70", mWallsPlanThr, 0, 90 );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_WALLS_EXTENDED_THR
      mWallsExtendedThr = tryFloat( prefs, k, "45", mWallsExtendedThr, 0, 90 ); 
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_WALLS_XCLOSE
      mWallsXClose = tryFloat( prefs, k, "0.1", mWallsXClose, 0.0001f ); 
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_WALLS_XSTEP
      mWallsXStep = tryFloat( prefs, k, "1.0", mWallsXStep, 0.0001f ); 
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_WALLS_CONCAVE
      mWallsConcave = tryFloat( prefs, k, "0.1", mWallsConcave, 0 ); 

    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_LOCALE
      app.setLocale( prefs.getString( k, "" ) );
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_CWD
      app.setCWD( prefs.getString( k, "TopoDroid" ) );

    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchUsesSplays = prefs.getBoolean( k, false );
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchBorderStep  = Float.parseFloat( prefs.getString( k, "0.2") );
    // } else if ( k.equals( key[ nk++ ] ) ) {
    //   mSketchSectionStep = Float.parseFloat( prefs.getString( k, "0.5") );
    } else {
      TopoDroidLog.checkLogPreferences( prefs, k );
    }
  }

  private static void setLineStyleAndType( String style )
  {
    mLineStyle = LINE_STYLE_BEZIER; // default
    mLineType  = 1;
    if ( style.equals( "0" ) ) {
      mLineStyle = LINE_STYLE_BEZIER;
      mLineType  = 1;
    } else if ( style.equals( "1" ) ) {
      mLineStyle = LINE_STYLE_ONE;
      mLineType  = 1;
    } else if ( style.equals( "2" ) ) {
      mLineStyle = LINE_STYLE_TWO;
      mLineType  = 2;
    } else if ( style.equals( "3" ) ) {
      mLineStyle = LINE_STYLE_THREE;
      mLineType  = 3;
    }
  }

  private static void parseSurveyStations( String str ) 
  {
    try {
      mSurveyStations = Integer.parseInt( str );
    } catch ( NumberFormatException e ) {
      mSurveyStations = Integer.parseInt( SURVEY_STATION );
    }
    if ( mSurveyStations == 5 ) { // local magnetic anomaly
      mBacksightShot = true;
      mSurveyStations = 1;
      mShotAfterSplays = true;
    } else {
      mBacksightShot = false;
      mShotAfterSplays = ( mSurveyStations <= 2 );
      if ( mSurveyStations > 2 ) mSurveyStations -= 2;
    }
    // Log.v("DistoX", "mSurveyStations " + mSurveyStations + " mShotAfterSplays " + mShotAfterSplays );
  }

}
