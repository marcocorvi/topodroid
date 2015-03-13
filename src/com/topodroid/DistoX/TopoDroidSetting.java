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
 * CHANGES
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
    "DISTOX_VTHRESHOLD",          //  5
    "DISTOX_SURVEY_STATION",      //  6 // DISTOX_SURVEY_STATIONS must not be used
    "DISTOX_UNIT_LENGTH",
    "DISTOX_UNIT_ANGLE",
    "DISTOX_ACCEL_THR",           //  9
    "DISTOX_MAG_THR",
    "DISTOX_DIP_THR",             // 11
    "DISTOX_LOOP_CLOSURE",        // 12
    "DISTOX_CHECK_ATTACHED",      // 13

    "DISTOX_UNIT_LOCATION",       // 14 
    "DISTOX_ALTITUDE",            // 15
    "DISTOX_CRS",                 // 16
    "DISTOX_GPS_AVERAGING",       // 17

    "DISTOX_GROUP_BY",            // 18
    "DISTOX_GROUP_DISTANCE",
    "DISTOX_CALIB_EPS",           // 20
    "DISTOX_CALIB_MAX_IT",
    "DISTOX_RAW_DATA",            // 22
    "DISTOX_CALIB_ALGO",          // 23

    "DISTOX_DEVICE",              // 24 N.B. indexKeyDeviceName
    "DISTOX_BLUETOOTH",           // 25
    "DISTOX_SOCK_TYPE",
    "DISTOX_COMM_RETRY",          // 27
    "DISTOX_BOOTLOADER",          // 28
    "DISTOX_CONN_MODE",           // 29

    "DISTOX_AUTO_STATIONS",       // 30 
    "DISTOX_CLOSENESS",           // 31
    "DISTOX_LINE_SEGMENT",
    "DISTOX_LINE_ACCURACY",
    "DISTOX_LINE_CORNER",         // 34
    "DISTOX_LINE_STYLE",          // 35
    "DISTOX_DRAWING_UNIT",        // 36
    "DISTOX_PICKER_TYPE",         // 37
    "DISTOX_HTHRESHOLD",          // 38  // NOT USED
    "DISTOX_STATION_SIZE",        // 39
    "DISTOX_LABEL_SIZE",          // 40
    "DISTOX_LINE_THICKNESS",      // 41

    "DISTOX_TEAM",                   // 42
    "DISTOX_ALTIMETRIC",             // 43
    "DISTOX_SHOT_TIMER",             // 44
    "DISTOX_BEEP_VOLUME",            // 45
    "DISTOX_LEG_SHOTS",              // 46
    "DISTOX_COSURVEY",

    "DISTOX_SKETCH_LINE_STEP",       // 48
    "DISTOX_DELTA_EXTRUDE",          // 49
    "DISTOX_COMPASS_READINGS",       // 50

    "DISTOX_SPLAY_EXTEND",           // 51
    "DISTOX_AUTO_RECONNECT",         // 52
    "DISTOX_BITMAP_SCALE",           // 53
    "DISTOX_THUMBNAIL",              // 54
    "DISTOX_DOT_RADIUS",             // 55
    "DISTOX_FIXED_THICKNESS",        // 56
    "DISTOX_ARROW_LENGTH",           // 57
    "DISTOX_EXPORT_SHOTS",           // 58

    "DISTOX_LOCALE",                 // 59
    "DISTOX_CWD",                    // must be last 

    // "DISTOX_SKETCH_USES_SPLAYS",  // 
    // "DISTOX_SKETCH_BERDER_STEP",
    // "DISTOX_SKETCH_SECTION_STEP", // 

  };

  static final int indexKeyDeviceName = 24;

  static String keyDeviceName() { return key[indexKeyDeviceName]; }

  // static final  String EXPORT_TYPE    = "th";    // DISTOX_EXPORT_TH

  // prefs default values

  // static int mScreenTimeout = 60000; // 60 secs
  static int mTimerCount = 10; // Acc/Mag timer countdown (secs)
  static int mBeepVolume = 50; // beep volume
  
  final static int CONN_MODE_BATCH = 0; // DistoX connection mode
  final static int CONN_MODE_CONTINUOUS = 1;
  static int mConnectionMode = CONN_MODE_BATCH; 

  static int mCompassReadings = 4; // number of compass readings to average

  // static final String CLOSE_DISTANCE = "0.05"; // 50 cm / 1000 cm
  static float mCloseDistance = 1.0f; // FIXME kludge

  static int   mMinNrLegShots = 2;

  // selection_radius = cutoff + closeness / zoom
  static final float mCloseCutoff = 0.01f; // minimum selection radius

  static final String CLOSENESS   = "16";    // drawing closeness threshold
  static float mCloseness = 16f;             // selection radius
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


  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // LOCATION

  static final int ALT_WGS84 = 0; // WGS84 altitude
  static final int ALT_ASL = 1;   // altimetric altitude

  static final String ALTITUDE = "0";  
  static int mAltitude = ALT_WGS84;     // location altitude type

  static boolean mAltimetricLookup = false; // whether to lookup altimetric atitude

  static String mCRS = "Long-Lat";    // default coord ref systen 

  // static final  String UNIT_LOCATION  = "ddmmss";
  static int mUnitLocation = 0; // 0 dec-degree, 1 ddmmss

  static final  boolean USE_GPSAVERAGING = false;
  static boolean mUseGPSAveraging = USE_GPSAVERAGING;

  static String  mDefaultTeam = "";

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // CALIBRATION

  static final String GROUP_DISTANCE = "40.0";
  static float mGroupDistance = 40f;

  static final float DISTOX_MAX_EPS  = 0.01f; // hard limit
  static final String CALIB_EPS      = "0.000001";
  static float mCalibEps = 0.000001f; // calibartion epsilon

  static final int DISTOX_MIN_ITER   = 50;  // hard limit
  static final String CALIB_MAX_ITER = "200";
  static int   mCalibMaxIt = 200;     // calibration max nr of iterations

  // calibration data grouping policies
  static final int GROUP_BY_DISTANCE = 0;
  static final int GROUP_BY_FOUR     = 1;
  static final int GROUP_BY_ONLY_16  = 2;
  static final String GROUP_BY  = "2";     // GROUP_BY_ONLY_16
  static int mGroupBy = GROUP_BY_ONLY_16;  // how to group calib data

  static boolean mRawData;   // whether to display calibration raw data as well
  static int   mCalibAlgo;   // calibration algorithm: 0 auto, 1 linear, 2 non-linear

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DEVICE
  static boolean mBootloader;  // whether to show bootloader menu
  static boolean mAutoReconnect = false;

  // private boolean mSaveOnDestroy = SAVE_ON_DESTROY;
  // int   mDefaultConnectionMode;

  static final boolean CHECK_BT = true;
  static int mCheckBT;        // BT: 0 disabled, 1 check on start, 2 enabled

  static final int TOPODROID_SOCK_DEFAULT      = 0;    // BT socket type
  static final int TOPODROID_SOCK_INSEC        = 1;
  // static final int TOPODROID_SOCK_INSEC_RECORD = 2;
  // static final int TOPODROID_SOCK_INSEC_INVOKE = 3;
  static int mSockType = TOPODROID_SOCK_DEFAULT; // FIXME static
  static int mCommRetry = 1;
  static int mCommType  = 0; // 0: on-demand, 1: continuous

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SHOTS
  static float mVThreshold = 80f;   // verticality threshold (LRUD)

  static float mHThreshold;  // horizontal plot threshold

  static boolean mCheckAttached = false;    // whether to check is there are shots non-attached

  static int mExportShotsFormat = 0; // TopoDroidExport.EXPORT_THERION

  static final String SURVEY_STATION = "1"; 
  static int     mSurveyStations = 1; // automatic survey stations: 0 no, 1 forward-after-splay, 2 backward-after-splay
  static boolean mShotAfterSplays = true;  //                       3 forward-before-splay, 4 backward-before-splay

  static boolean isSurveyForward() { return (mSurveyStations%2) == 1; }
  static boolean isSurveyBackward() { return mSurveyStations>0 && (mSurveyStations%2) == 0; }

  static boolean mLoopClosure = false;  // whether to do loop closure

  static boolean mSplayExtend = true;  // whether to extend splays or not
  
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

  static final int PICKER_LIST = 0; // Drawing-tools picker type
  static final int PICKER_GRID = 1;
  static int mPickerType = PICKER_LIST;

  static final int LINE_STYLE_BEZIER = 0;  // drawing line styles
  static final int LINE_STYLE_ONE    = 1;
  static final int LINE_STYLE_TWO    = 2;
  static final int LINE_STYLE_THREE  = 3;
  static final String LINE_STYLE     = "0";     // LINE_STYLE_BEZIER
  static int mLineStyle = LINE_STYLE_BEZIER;    

  static int mLineType;        // line type:  1       1     2    3

  static float mStationSize;          // size of station names [pt]
  static float mLabelSize;            // size of labels [pt]
  static float mFixedThickness = 1;   // width of fixed lines
  static float mLineThickness = 1;    // witdh of drawing lines
  static float mDotRadius = 5;
  static float mArrowLength = 5;

  static float mUnit = 1.2f; // drawing unit

  static int   mLineSegment = 10;
  static float mLineAccuracy = 1f;
  static float mLineCorner = 20;     // corner threshold

  static boolean mAutoStations = true; // whether to add stations automatically to scrap therion files

  static float mBitmapScale = 1.5f;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // 3D
  static float mSketchSideSize;
  static float mDeltaExtrude;
  // static boolean mSketchUsesSplays; // whether 3D models surfaces use splays
  // static float mSketchBorderStep;
  // static float mSketchSectionStep;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DATA ACCURACY
  static float mAccelerationThr = 300; // acceleration threshold (shot quality)
  static float mMagneticThr     = 200; // magnetic threshold
  static float mDipThr          = 2;  // dip threshold

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

  static void loadPreferences( TopoDroidApp app, SharedPreferences prefs )
  {
    // ------------------- GENERAL PREFERENCES
    int k = 0;
    float f;
    int i;


    mActivityLevel = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_EXTRA_BUTTONS
    setActivityBooleans();

    try {
      mSizeButtons = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // choice: cannot fail
    } catch ( NumberFormatException e ) { }
    try {
      i = Integer.parseInt( prefs.getString( key[k++], "14" ) );
      if ( i > 0 ) mTextSize = i;
    } catch ( NumberFormatException e ) { }

    // ------------------- SURVEY PREFERENCES
    try {
      f = Float.parseFloat( prefs.getString( key[k++], "0.05" ) ); // DISTOX_CLOSE_DISTANCE 3
      if ( f > 0 ) mCloseDistance = f;
    } catch ( NumberFormatException e ) { }

    try {
      setExtendThr( Float.parseFloat( prefs.getString( key[k++], "30" ) ) );    // DISTOX_EXTEND_THR2
    } catch ( NumberFormatException e ) {
      setExtendThr( 30 );
    }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "80" ) );    // DISTOX_VTHRESHOLD
      if ( f > 0 && f <= 90 ) mVThreshold = f;
    } catch ( NumberFormatException e ) { }

    parseSurveyStations( prefs.getString( key[k++], SURVEY_STATION ) ); // DISTOX_SURVEY_STATIONS 6

    mUnitLength = prefs.getString( key[k++], UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : TopoDroidUtil.M2FT;
    mUnitAngle  = prefs.getString( key[k++], UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : TopoDroidUtil.DEG2GRAD;
  
    try {
      f = Float.parseFloat( prefs.getString( key[k++], "300.0" ) );  // DISTOX_ACCEL_THR 9
      if ( f > 0 ) mAccelerationThr = f;
    } catch ( NumberFormatException e ) { }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "200.0" ) );     // DISTOX_MAG_THR
      if ( f > 0 ) mMagneticThr = f;
    } catch ( NumberFormatException e ) { }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "2.0" ) );            // DISTOX_DIP_THR
      if ( f > 0 ) mDipThr = f;
    } catch ( NumberFormatException e ) { }


    mLoopClosure   = prefs.getBoolean( key[k++], false );                     // DISTOX_LOOP_CLOSURE 12
    mCheckAttached = prefs.getBoolean( key[k++], false );                   // DISTOX_CHECK_ATTACHED

    mUnitLocation  = prefs.getString( key[k++], "ddmmss" ).equals("ddmmss") ? TopoDroidConst.DDMMSS 
                                                                            : TopoDroidConst.DEGREE;
    try {
      mAltitude = Integer.parseInt( prefs.getString( key[k++], ALTITUDE ) );      // DISTOX_ALTITUDE
    } catch ( NumberFormatException e ) {
      mAltitude = Integer.parseInt( ALTITUDE );
    }
    mCRS           = prefs.getString( key[k++], "Long-Lat" );                        // DISTOX_CRS
    mUseGPSAveraging = prefs.getBoolean( key[k++], USE_GPSAVERAGING );               // DISTOX_GPS_AVERAGING 17

    // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitLength " + mUnitLength );
    // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitAngle " + mUnitAngle );
    // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitLocation " + mUnitLocation );

    // ------------------- CALIBRATION PREFERENCES
    try {
      mGroupBy = Integer.parseInt( prefs.getString( key[k++], GROUP_BY ) );       // DISTOX_GROUP_BY (choice)
    } catch ( NumberFormatException e ) { mGroupBy = GROUP_BY_ONLY_16; }

    try {
      mGroupDistance = Float.parseFloat( prefs.getString( key[k++], GROUP_DISTANCE ) ); // DISTOX_GROUP_DISTANCE
    } catch ( NumberFormatException e ) { mGroupDistance = 40f; }

    try {
      mCalibEps      = Float.parseFloat( prefs.getString( key[k++], CALIB_EPS ) );      // DISTOX_CALIB_EPS
    } catch ( NumberFormatException e ) { mCalibEps = 0.000001f; }

    try {
      mCalibMaxIt    = Integer.parseInt( prefs.getString( key[k++], CALIB_MAX_ITER ) ); // DISTOX_CALIB_MAX_IT
    } catch ( NumberFormatException e ) { mCalibMaxIt = 200; }

    mRawData       = prefs.getBoolean( key[k++], false );                             // DISTOX_RAW_DATA 22
      
    try {
      mCalibAlgo    = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_CALIB_MAX_IT
    } catch ( NumberFormatException e ) { mCalibAlgo = 1; }

    // ------------------- DEVICE PREFERENCES
    k++; // DISTOX_DEVICE  24

    try {
      mCheckBT = Integer.parseInt( prefs.getString( key[k++], "1" ) ); // DISTOX_BLUETOOTH 25
    } catch ( NumberFormatException e ) { mCheckBT = 1; }

    try {
      mSockType = Integer.parseInt( prefs.getString( key[k++], "0" ) ); // DISTOX_SOCK_TYPE 26
    } catch ( NumberFormatException e ) { mSockType = 0; }
      
    try {
      setCommRetry( Integer.parseInt( prefs.getString( key[k++], "1" ) ) ); // DISTOX_COMM_RETRY 27
    } catch ( NumberFormatException e ) { setCommRetry( 1 ); }

    mBootloader = prefs.getBoolean( key[k++], false );                      // DISTOX_BOOTLOADER 28

    mConnectionMode = Integer.parseInt( prefs.getString( key[k++], "0" ) ); // DISTOX_CONN_MODE 29

    // -------------------  DRAWING PREFERENCES
    mAutoStations  = prefs.getBoolean( key[k++], true );                // DISTOX_AUTO_STATIONS 30
    
    try {
      mCloseness = Float.parseFloat( prefs.getString( key[k++], CLOSENESS ) );   // DISTOX_CLOSENESS
    } catch ( NumberFormatException e ) { mCloseness = 16; }

    try {
      mLineSegment = Integer.parseInt( prefs.getString( key[k++], "10" ) );  // DISTOX_LINE_SEGMENT
    } catch ( NumberFormatException e ) { mLineSegment = 10; }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "1.0" ) ); // DISTOX_LINE_ACCURACY
      if ( f > 0 ) mLineAccuracy = f;
    } catch ( NumberFormatException e ) { mLineAccuracy = 1f; }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "20" ) );   // DISTOX_LINE_CORNER
      if ( f > 0 ) mLineCorner = f;
    } catch ( NumberFormatException e ) { mLineCorner = 20f; }

    setLineStyleAndType( prefs.getString( key[k++], LINE_STYLE ) );              // DISTOX_LINE_STYLE

    try {
      mUnit = Float.parseFloat( prefs.getString( key[k++], "1.2" ) );  // DISTOX_DRAWING_UNIT
    } catch ( NumberFormatException e ) {
      mUnit = 1.2f;
    }

    try {
      mPickerType = Integer.parseInt( prefs.getString( key[k++], "0" ) );        // DISTOX_PICKER_TYPE
    } catch ( NumberFormatException e ) { mPickerType = 0; }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "70.0" ) );    // DISTOX_HTHRESHOLD
      if ( f >= 0 && f <= 90 ) mHThreshold = f;
    } catch ( NumberFormatException e ) { mHThreshold = 70.0f; }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "24.0" ) );   // DISTOX_STATION_SIZE 39
      if ( f > 0 ) mStationSize = f;
    } catch ( NumberFormatException e ) { mStationSize = 24.0f; }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "24.0" ) );     // DISTOX_LABEL_SIZE 40
      if ( f > 0 ) mLabelSize = f;
    } catch ( NumberFormatException e ) { mLabelSize = 24.0f; }

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "1.0" ) );  // DISTOX_LINE_THICKNESS 41
      if ( f > 0.0f ) {
        mLineThickness = f;
        // DrawingBrushPaths.reloadLineLibrary( app.getResources() ); // not needed on load
      }
    } catch ( NumberFormatException e ) { }

    mDefaultTeam = prefs.getString( key[k++], "" );                      // DISTOX_TEAM
    mAltimetricLookup = prefs.getBoolean( key[k++], false );               // DISTOX_ALTIMETRIC
    try {
      int t = Integer.parseInt( prefs.getString( key[k++], "10") );  // DISTOX_SHOT_TIMER
      if ( t > 0 ) mTimerCount = t;
    } catch ( NumberFormatException e ) { }
    try {
      int t = Integer.parseInt( prefs.getString( key[k++], "10") );  // DISTOX_BEEP_VOLUME
      if ( t > 0 ) mBeepVolume = (t<10)? 10 : (t>100)? 100 : t;
    } catch ( NumberFormatException e ) { }

    try {
      i = Integer.parseInt( prefs.getString( key[k++], "2") );  // DISTOX_LEG_SHOTS (choice)
      if ( i > 1 ) mMinNrLegShots = i;
    } catch ( NumberFormatException e ) { }

    boolean co_survey = prefs.getBoolean( key[k++], false );        // DISTOX_COSURVEY
    /* ignore */

    // ------------------- SKETCH PREFERENCES
    try {
      f = Float.parseFloat( prefs.getString( key[k++], "0.5") );
      if ( f > 0 ) mSketchSideSize = f;
    } catch ( NumberFormatException e ) { }
    
    try {
      f = Float.parseFloat( prefs.getString( key[k++], "50") );
      if ( f > 0 ) mDeltaExtrude = f;
    } catch ( NumberFormatException e ) { }

    // mSketchUsesSplays  = prefs.getBoolean( key[k++], false );
    // mSketchBorderStep  = Float.parseFloat( prefs.getString( key[k++], "0.2") );
    // mSketchSectionStep = Float.parseFloat( prefs.getString( key[k++], "0.5") );

    try {
      i = Integer.parseInt( prefs.getString( key[k++], "4" ) );
      if ( i > 0 ) mCompassReadings = i;
    } catch ( NumberFormatException e ) { }
    
    mSplayExtend   = prefs.getBoolean( key[k++], true ); 
    mAutoReconnect = prefs.getBoolean( key[k++], false ); 

    try {
      f = Float.parseFloat( prefs.getString( key[k++], "1.5" ) ); // DISTOX_BITMAP_SCALE
      if ( f > 0.5f && f <= 10 ) mBitmapScale = f;
    } catch ( NumberFormatException e ) { }

    try {
      i = Integer.parseInt( prefs.getString( key[k++], "200" ) );
      if ( i >= 80 && i <= 400 ) mThumbSize = i;
    } catch ( NumberFormatException e ) { }
    
    try {
      f = Float.parseFloat( prefs.getString( key[k++], "5" ) ); // DISTOX_DOT_RADIUS
      if ( f > 0.5f && f <= 100 ) mDotRadius = f;
    } catch ( NumberFormatException e ) { }
    try {
      f = Float.parseFloat( prefs.getString( key[k++], "1" ) ); // DISTOX_FIXED_THICKNESS
      if ( f > 0.5f && f <= 10 ) {
        mFixedThickness = f;
        // DrawingBrushPaths.setStrokeWidths(); // not needed on load
      }
    } catch ( NumberFormatException e ) { }
    try {
      f = Float.parseFloat( prefs.getString( key[k++], "5" ) ); // DISTOX_ARROW_LENGTH
      if ( f > 1 && f <= 20 ) mArrowLength = f;
    } catch ( NumberFormatException e ) { }
    try {
      mExportShotsFormat = Integer.parseInt( prefs.getString( key[k++], "0") );  // DISTOX_EXPORT_SHOTS (choice)
    } catch ( NumberFormatException e ) { }

    app.setLocale( prefs.getString( key[k++], "" ) );

    // String cwd = prefs.getString( key[k++], "TopoDroid" );
    // if ( ! cwd.equals( mCWD ) ) {
    //   mCWD = cwd;
    //   TopoDroidPath.setPaths( mCWD );
    //   mData.openDatabase();
    // }
  }

  static void setActivityBooleans()
  {
    mLevelOverBasic        = mActivityLevel > LEVEL_BASIC;
    mLevelOverNormal       = mActivityLevel > LEVEL_NORMAL;
    mLevelOverAdvanced     = mActivityLevel > LEVEL_ADVANCED;
    mLevelOverExperimental = mActivityLevel > LEVEL_EXPERIMENTAL;
  }

  static void checkPreference( SharedPreferences prefs, String k, TopoDroidActivity activity, TopoDroidApp app )
  {
    int nk = 0; // key index
    int i;
    float f;
    // Log.v(TopoDroidApp.TAG, "onSharePreferenceChanged " + k );

    if ( k.equals( key[ nk++ ] ) ) {                           // DISTOX_EXTRA_BUTTONS
      int level = Integer.parseInt( prefs.getString( k, "1" ) );
      if ( level != mActivityLevel ) {
        mActivityLevel = level;
        setActivityBooleans();
        if ( activity != null ) {
          activity.resetButtonBar();
          activity.setMenuAdapter( app.getResources() );
        }  
      }
    } else if ( k.equals( key[ nk++ ] ) ) {
      mSizeButtons = Integer.parseInt( prefs.getString( k, "1" ) );
      if ( activity != null ) activity.resetButtonBar();
    } else if ( k.equals( key[ nk++ ] ) ) {   // DISTOX_TEXT_SIZE
      try {
        i = Integer.parseInt( prefs.getString( k, "14" ) );
        if ( i > 0 ) mTextSize = i;
      } catch ( NumberFormatException e ) { }
  
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "0.05" ) );
        if ( f > 0.0f ) mCloseDistance = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { 
      try {
        setExtendThr( Float.parseFloat( prefs.getString( k, "30" ) ) );   // DISTOX_EXTEND_THR2 4
      } catch ( NumberFormatException e ) { setExtendThr( 30 ); }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "80" ) );
        if ( f > 0.0f && f <= 90 ) mVThreshold = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      parseSurveyStations( prefs.getString( k, SURVEY_STATION ) ); // DISTOX_SURVEY_STATION 6
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitLength = prefs.getString( k, UNIT_LENGTH ).equals(UNIT_LENGTH) ?  1.0f : TopoDroidUtil.M2FT;
      // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitLength changed " + mUnitLength );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitAngle  = prefs.getString( k, UNIT_ANGLE ).equals(UNIT_ANGLE) ?  1.0f : TopoDroidUtil.DEG2GRAD;
      // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitAngle changed " + mUnitAngle );
    } else if ( k.equals( key[ nk++ ] ) ) {                        // DISTOX_ACCEL_THR 9
      try {
        f = Float.parseFloat( prefs.getString( k, "300.0" ) );
        if ( f > 0.0f ) mAccelerationThr = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {                       // DISTOX_MAG_THR
      try {
        f = Float.parseFloat( prefs.getString( k, "200.0" ) );
        if ( f > 0.0f ) mMagneticThr = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {                       // DISTOX_DIP_THR 11
      try {
        f = Float.parseFloat( prefs.getString( k, "2.0" ) );
        if ( f > 0.0f ) mDipThr = f;
      } catch ( NumberFormatException e ) { }
  
    } else if ( k.equals( key[ nk++ ] ) ) {
      mLoopClosure = prefs.getBoolean( k, false );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCheckAttached = prefs.getBoolean( k, false );
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUnitLocation  = prefs.getString( k, "ddmmss" ).equals("ddmmss") ? TopoDroidConst.DDMMSS
                                                                       : TopoDroidConst.DEGREE;
      // TopoDroidLog.Log( TopoDroidLog.LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        mAltitude = Integer.parseInt( prefs.getString( k, ALTITUDE ) ); // DISTOX_ALTITUDE 15
      } catch ( NumberFormatException e ) { mAltitude = Integer.parseInt( ALTITUDE ); }
    } else if ( k.equals( key[ nk++ ] ) ) {
      mCRS = prefs.getString( k, "Long-Lat" );     // DISTOX_CRS 16
    } else if ( k.equals( key[ nk++ ] ) ) {
      mUseGPSAveraging = prefs.getBoolean( k, USE_GPSAVERAGING );   // DISTOX_GPS_AVERAGING
  
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        mGroupBy = Integer.parseInt( prefs.getString( k, GROUP_BY ) );  // DISTOX_GROUP_BY 18 (choice)
      } catch ( NumberFormatException e ) { mGroupBy = GROUP_BY_ONLY_16; }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, GROUP_DISTANCE ) );
        if ( f > 0.0f ) mGroupDistance = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, CALIB_EPS ) );
        if ( f > 0.0f ) mCalibEps = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        i = Integer.parseInt( prefs.getString( k, CALIB_MAX_ITER ) );
        if ( i > 0 ) mCalibMaxIt = i;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      mRawData = prefs.getBoolean( k, false );  // DISTOX_RAW_DATA 22
    } else if ( k.equals( key[ nk++ ] ) ) {     // DISTOX_CALIB_ALGO 23
      try {
        mCalibAlgo = Integer.parseInt( prefs.getString( k, "1" ) ); // DISTOX_CALIB_MAX_IT (choice)
      } catch ( NumberFormatException e ) { mCalibAlgo = 1; }

    } else if ( k.equals( key[ nk++ ] ) ) {                         // DISTOX_DEVICE 24
      // mDevice = mData.getDevice( prefs.getString( k, DEVICE_NAME ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                         // DISTOX_CHECK_B (choice)
      try {
        mCheckBT = Integer.parseInt(prefs.getString( k, "1" ) ); 
      } catch ( NumberFormatException e ) { mCheckBT = 1; }
    } else if ( k.equals( key[ nk++ ] ) ) {                        // "DISTOX_SOCK_TYPE 26 (choice)
      try {
        mSockType = Integer.parseInt( prefs.getString( k, "0" ) );
      } catch ( NumberFormatException e ) { mSockType = 0; }
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_COMM_RETRY 27
      try {
        setCommRetry( Integer.parseInt( prefs.getString( k, "1" ) ) );
      } catch ( NumberFormatException e ) { setCommRetry( 1 ); }
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_BOOTLOADER 28
      mBootloader = prefs.getBoolean( k, false );     
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_CONN_MODE (choice)
      mConnectionMode = Integer.parseInt( prefs.getString( k, "0" ) ); 
  
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAutoStations = prefs.getBoolean( k, true );  // DISTOX_AUTO_STATIONS 30
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, CLOSENESS ) );
        if ( f > 0.0f ) mCloseness = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        i = Integer.parseInt( prefs.getString( k, "10" ) );
        if ( i > 0 ) mLineSegment = i;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "1.0" ) );
        if ( f > 0.0f ) mLineAccuracy = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "20" ) );
        if ( f > 0.0f ) mLineCorner = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {                           // STYLE 35
      setLineStyleAndType( prefs.getString( k, LINE_STYLE ) );
    } else if ( k.equals( key[ nk++ ] ) ) {                           // DISTOX_DRAWING_UNIT 36
      try {
        f = Float.parseFloat( prefs.getString( k, "1.2" ) );
        if ( f > 0.0f && f != mUnit ) {
          mUnit = f;
          DrawingBrushPaths.reloadPointLibrary( app.getResources() );
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {                          // DISTOX_PICKER_TYPE 37
      try {
        mPickerType = Integer.parseInt( prefs.getString( k, "0" ) );
      } catch ( NumberFormatException e ) { mPickerType = 0; }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "70" ) );  // DISTOX_HTHRESHOLD 38
        if ( f >= 0.0f && f <= 90.0f ) mHThreshold = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "24" ) ); // DISTOX_STATION_SIZE 39
        if ( f > 0.0f && f != mStationSize ) {
          mStationSize = f;
          DrawingBrushPaths.setTextSizes( );
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "24" ) ); // DISTOX_LABEL_SIZE 40
        if ( f > 0.0f && f != mLabelSize ) {
          mLabelSize = f;
          DrawingBrushPaths.setTextSizes( );
        }
      } catch ( NumberFormatException e ) { }
      // FIXME changing label size affects only new labels
      //       not existing labels (until they are edited)
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        f = Float.parseFloat( prefs.getString( k, "1" ) );  // DISTOX_LINE_THICKNESS 41
        if ( f > 0.f && f != mLineThickness ) {
          mLineThickness = f;
          DrawingBrushPaths.reloadLineLibrary( app.getResources() );
        }
      } catch ( NumberFormatException e ) { }

    } else if ( k.equals( key[ nk++ ] ) ) {
      mDefaultTeam = prefs.getString( k, "" );              // DISTOX_TEAM
    } else if ( k.equals( key[ nk++ ] ) ) {
      mAltimetricLookup = prefs.getBoolean( k, false );     // DISTOX_ALTIMETRIC
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        i = Integer.parseInt( prefs.getString( k, "10") );  // DISTOX_SHOT_TIMER
        if ( i > 0 ) mTimerCount = i;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        int t = Integer.parseInt( prefs.getString( k, "10") );  // DISTOX_BEEP_VOLUME
        if ( t > 0 ) mBeepVolume = (t<10)? 10 : (t>100)? 100 : t;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      try {
        mMinNrLegShots = Integer.parseInt( prefs.getString( k, "2") );  // DISTOX_LEG_SHOTS (choice)
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) {
      boolean co_survey = prefs.getBoolean( k, false );               // DISTOX_COSURVEY
      if ( co_survey != app.mCoSurveyServer ) {
        app.setCoSurvey( co_survey ); // set flag and start/stop server
      }

    } else if ( k.equals( key[ nk++ ] ) ) {    // DISTOX_SKETCH_LINE_STEP
      try {
        f = Float.parseFloat( prefs.getString( k, "0.5") );  // 0.5 meter
        if ( f > 0.0f ) mSketchSideSize = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_DELTA_EXTRUDE
      try {
        f = Float.parseFloat( prefs.getString( k, "50" ) );
        if ( f > 0.0f ) mDeltaExtrude = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_COMPASS_READINGS
      try {
        i = Integer.parseInt( prefs.getString( k, "4" ) );
        if ( i > 0 ) mCompassReadings = i;
      } catch ( NumberFormatException e ) { }

    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_SPLAY_EXTEND
      mSplayExtend = prefs.getBoolean( k, true ); 
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_AUTO_RECONNECT
      mAutoReconnect = prefs.getBoolean( k, false ); 
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_BITMAP_SCALE
      try {
        f = Float.parseFloat( prefs.getString( k, "1.5" ) );
        if ( f > 0.5f && f <= 10 ) mBitmapScale = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { // DISTOX_THUMBNAIL
      try {
        i = Integer.parseInt( prefs.getString( k, "200" ) );
        if ( i >= 80 && i <= 400 ) mThumbSize = i;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { 
      try {
        f = Float.parseFloat( prefs.getString( k, "5" ) ); // DISTOX_DOT_RADIUS
        if ( f > 0.5f && f <= 100 ) mDotRadius = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { 
      try {
        f = Float.parseFloat( prefs.getString( k, "1" ) ); // DISTOX_FIXED_THICKNESS
        if ( f > 0.5f && f <= 10 ) {
          mFixedThickness = f;
          DrawingBrushPaths.setStrokeWidths();
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { 
      try {
        f = Float.parseFloat( prefs.getString( k, "5" ) ); // DISTOX_ARROW_LENGTH
        if ( f > 1 && f <= 20 ) mArrowLength = f;
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ nk++ ] ) ) { 
      try {
        mExportShotsFormat = Integer.parseInt( prefs.getString( k, "0") );  // DISTOX_EXPORT_SHOTS (choice)
      } catch ( NumberFormatException e ) { }

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


  private static void setCommRetry( int c )
  {
    if ( c < 1 ) c = 1; else if ( c > 5 ) c = 5;
    mCommRetry = c;
  }

  private static void setExtendThr( float e )
  {
    mExtendThr = e;
    if ( mExtendThr < 0.0f ) mExtendThr = 0.0f;
    if ( mExtendThr > 90.0f ) mExtendThr = 90.0f;
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
    mShotAfterSplays = ( mSurveyStations <= 2 );
    if ( mSurveyStations > 2 ) mSurveyStations -= 2;
    // Log.v("DistoX", "mSurveyStations " + mSurveyStations + " mShotAfterSplays " + mShotAfterSplays );
  }

}
