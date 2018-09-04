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

import android.util.Log;

class TDSetting
{
  static private String defaultTextSize = "16";
  static private String defaultButtonSize = "1";

  static void setTextSize( int ts )
  {
    float ds = TopoDroidApp.getDisplayDensity() / 3.0f;
    mTextSize = (int)( ( ds * ts ) );
  }

  // get tentative text size
  static int getTextSize( int ts )
  {
    float ds = TopoDroidApp.getDisplayDensity() / 3.0f;
    return (int)( ( ds * ts ) );
  }

  static boolean setLabelSize( float f, boolean brush )
  {
    if ( f >= 1 && f != mLabelSize ) {
      mLabelSize = f;
      if ( brush ) BrushManager.setTextSizes( );
      return true;
    }
    return false;
  }

  static boolean setStationSize( float f, boolean brush )
  {
    if ( f >= 1 && f != mStationSize ) {
      mStationSize = f;
      if ( brush ) BrushManager.setTextSizes( );
      return true;
    }
    return false;
  }

  static void setDrawingUnits( float f )
  {
    if ( f > 0.1f && f != mUnitIcons ) {
      mUnitIcons = f;
      BrushManager.reloadPointLibrary( TDInstance.context, TDInstance.context.getResources() );
    }
  }

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
  // static final String CALIB_EPS      = "0.000001";
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

  static int mImportDatamode    = 0;  // SurveyInfo.DATAMODE_NORMAL
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
  // static int mCompassReadings  = 4;     // number of compass readings to average

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
  static int mPointingRadius = 24;

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

  static private float tryFloatValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    float f = 0;
    if ( val == null ) { 
      f = Integer.parseInt(def_value);
      hlp.update( key, def_value );
    } else {
      try {
        f = Float.parseFloat( val );
        hlp.update( key, val );
      } catch ( NumberFormatException e ) {
        TDLog.Error("Integer Format Error. Key " + key + " " + e.getMessage() );
        f = Float.parseFloat(def_value);
        hlp.update( key, def_value );
      }
    }
    return f;
  }

  static private int tryIntValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    int i = 0;
    if ( val == null ) { 
      i = Integer.parseInt(def_value);
      hlp.update( key, def_value );
    } else {
      try {
        i = Integer.parseInt( val );
        hlp.update( key, val );
      } catch( NumberFormatException e ) { 
        TDLog.Error("Integer Format Error. Key " + key + " " + e.getMessage() );
        i = Integer.parseInt(def_value);
        hlp.update( key, def_value );
      }
    }
    return i;
  }

  static private String tryStringValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    if ( val == null ) val = def_value;
    hlp.update( key, val );
    return val;
  }

  static private boolean tryBooleanValue( TDPrefHelper hlp, String key, String val, boolean def_value )
  {
    boolean i = def_value;
    if ( val != null ) {
      try {
        i = Boolean.parseBoolean( val );
      } catch( NumberFormatException e ) { 
        TDLog.Error("Integer Format Error. Key " + key + " " + e.getMessage() );
      }
    }
    hlp.update( key, i );
    return i;
  }

  static private void setLoopClosure( int loop_closure )
  {
    mLoopClosure = loop_closure;
    if ( mLoopClosure == LOOP_CYCLES ) {
      if ( ! TDLevel.overAdvanced ) mLoopClosure = LOOP_NONE;
    } else if ( mLoopClosure == LOOP_TRIANGLES ) {
      if ( ! TDLevel.overExpert ) mLoopClosure = LOOP_NONE;
    }
  }

  static boolean setSizeButtons( int size )
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
      mSizeButtons = (int)( mSizeBtns * TopoDroidApp.getDisplayDensity() * 0.86f );
      // Log.v("DistoX", "Size " + size + " Btns " + mSizeBtns + " " + mSizeButtons );
      if ( mSizeButtons < MIN_SIZE_BUTTONS ) mSizeButtons = MIN_SIZE_BUTTONS;
      return true;
    }
    return false;
  }

  // get tentative size of buttons
  static int getSizeButtons( int size )
  {
    int sz = mSizeBtns;
    switch ( size ) {
      case 0: sz = BTN_SIZE_SMALL;  break;
      case 1: sz = BTN_SIZE_NORMAL; break;
      case 3: sz = BTN_SIZE_MEDIUM; break;
      case 4: sz = BTN_SIZE_LARGE;  break;
      case 2: sz = BTN_SIZE_HUGE;   break;
    }
    return (int)( sz * TopoDroidApp.getDisplayDensity() * 0.86f );
  }

  private static boolean bool( String bol) { return bol.equals("true"); }

  // ---------------------------------------------------------------------------------
  //
  static void loadPrimaryPreferences( TopoDroidApp my_app, TDPrefHelper pref_hlp )
  {
    SharedPreferences prefs = pref_hlp.getSharedPrefs();
  
    defaultTextSize   = my_app.getResources().getString( R.string.default_textsize );
    defaultButtonSize = my_app.getResources().getString( R.string.default_buttonsize );

    // ------------------- GENERAL PREFERENCES
    String[] keyMain = TDPrefKey.MAIN;
    String[] defMain = TDPrefKey.MAINdef;
    int level = Integer.parseInt( prefs.getString( keyMain[3], defMain[3] ) ); // DISTOX_EXTRA_BUTTONS choice: 0, 1, 2, 3
    setActivityBooleans( prefs, level );

    setTextSize( tryInt(    prefs,     keyMain[1], defMain[1] ) );          // DISTOX_TEXT_SIZE
    setSizeButtons( tryInt( prefs,     keyMain[2], defMain[2] ) );          // DISTOX_SIZE_BUTTONS
    mKeyboard      = prefs.getBoolean( keyMain[4], bool(defMain[4]) );  // DISTOX_MKEYBOARD
    mNoCursor      = prefs.getBoolean( keyMain[5], bool(defMain[5]) );  // DISTOX_NO_CURSOR
    mLocalManPages = handleLocalUserMan( my_app, prefs.getString( keyMain[6], defMain[6] ), false ); // DISTOX_LOCAL_MAN
    boolean co_survey = prefs.getBoolean( keyMain[7], bool(defMain[7]) );        // DISTOX_COSURVEY 
    // TDLog.Profile("locale");
    TopoDroidApp.setLocale( prefs.getString( keyMain[8], defMain[8] ), false ); // DISTOX_LOCALE

    String[] keySurvey = TDPrefKey.SURVEY;
    String[] defSurvey = TDPrefKey.SURVEYdef;;
    mDefaultTeam = prefs.getString( keySurvey[0], defSurvey[0] );               // DISTOX_TEAM
    mInitStation = prefs.getString( keySurvey[3], defSurvey[3] ).replaceAll("\\s+", "");  // DISTOX_INIT_STATION 
    if ( mInitStation.length() == 0 ) mInitStation = defSurvey[3];
    DistoXStationName.setInitialStation( mInitStation );

    String[] keyData = TDPrefKey.DATA;
    String[] defData = TDPrefKey.DATAdef;
    mAzimuthManual = prefs.getBoolean( keyData[8], bool(defData[8]) );   // DISTOX_AZIMUTH_MANUAL 
    TDAzimuth.mFixedExtend = ( TDSetting.mAzimuthManual )? 1L : 0L;
    // TDAzimuth.resetRefAzimuth( TDAzimuth.mRefAzimuth ); // BUG may call setRefAzimuthButton on non-UI thread
    
    // ------------------- DEVICE PREFERENCES -def--fallback--min-max
    String[] keyDevice = TDPrefKey.DEVICE;
    String[] defDevice = TDPrefKey.DEVICEdef;
    // DISTOX_DEVICE - UNUSED HERE
    mCheckBT        = tryInt( prefs, keyDevice[0], defDevice[0] );        // DISTOX_BLUETOOTH choice: 0, 1, 2
  }

  static void loadSecondaryPreferences( TopoDroidApp my_app, TDPrefHelper pref_hlp )
  {
    SharedPreferences prefs = pref_hlp.getSharedPrefs();

    String[] keySurvey = TDPrefKey.SURVEY;
    String[] defSurvey = TDPrefKey.SURVEYdef;
    parseStationPolicy( pref_hlp, prefs.getString( keySurvey[1], defSurvey[1] ) ); // DISTOX_SURVEY_STATION
    mStationNames = (prefs.getString( keySurvey[2], defSurvey[2] ).equals("number"))? 1 : 0; // DISTOX_STATION_NAMES
    mThumbSize    = tryInt(   prefs,     keySurvey[4], defSurvey[3] );       // DISTOX_THUMBNAIL
    mDataBackup   = prefs.getBoolean(    keySurvey[5], bool(defSurvey[5]) ); // DISTOX_DATA_BACKUP
    mSharedXSections = prefs.getBoolean( keySurvey[6], bool(defSurvey[6]) ); // DISTOX_SHARED_XSECTIONS

    String[] keyPlot = TDPrefKey.PLOT;
    String[] defPlot = TDPrefKey.PLOTdef;
    mPickerType = tryInt( prefs,       keyPlot[0], defPlot[0] );       // DISTOX_PICKER_TYPE choice: 0, 1, 2
    mRecentNr   = tryInt( prefs,       keyPlot[1], defPlot[1] );       // DISTOX_RECENT_NR choice: 3, 4, 5, 6
    mSideDrag   = prefs.getBoolean(    keyPlot[2], bool(defPlot[2]) );     // DISTOX_SIDE_DRAG
    // setZoomControls( prefs.getBoolean( keyPlot[], bool(defPlot[]) ) ); // DISTOX_ZOOM_CONTROLS
    setZoomControls( prefs.getString(  keyPlot[3], defPlot[3] ), FeatureChecker.checkMultitouch( TDInstance.context ) ); // DISTOX_ZOOM_CTRL
    // mSectionStations  = tryInt( prefs, keyPlot[], "3");      // DISTOX_SECTION_STATIONS
    mCheckAttached = prefs.getBoolean( keyPlot[4], bool(defPlot[4]) ); // DISTOX_CHECK_ATTACHED
    mCheckExtend   = prefs.getBoolean( keyPlot[5], bool(defPlot[5]) ); // DISTOX_CHECK_EXTEND
    mBackupNumber  = tryInt(  prefs,   keyPlot[6], defPlot[6] );       // DISTOX_BACKUP_NUMBER
    mBackupInterval = tryInt( prefs,   keyPlot[7], defPlot[7] );       // DISTOX_BACKUP_INTERVAL

    String[] keyCalib = TDPrefKey.CALIB;
    String[] defCalib = TDPrefKey.CALIBdef;
    mGroupBy       = tryInt(   prefs,      keyCalib[0], defCalib[0] ); // DISTOX_GROUP_BY choice: 0, 1, 2
    mGroupDistance = tryFloat( prefs,      keyCalib[1], defCalib[1] ); // DISTOX_GROUP_DISTANCE
    mCalibEps      = tryFloat( prefs,      keyCalib[2], defCalib[2] ); // DISTOX_CAB_EPS
    mCalibMaxIt    = tryInt(   prefs,      keyCalib[3], defCalib[3] ); // DISTOX_CALIB_MAX_IT
    mCalibShotDownload = prefs.getBoolean( keyCalib[4], bool(defCalib[4]) ); // DISTOX_CALIB_SHOT_DOWNLOAD
    // mRawData       = prefs.getBoolean( keyCalib[], bool(defCalib[]) );    // DISTOX_RAW_DATA 20
    mRawCData      = tryInt( prefs,        keyCalib[5], defCalib[5] );   // DISTOX_RAW_CDATA 20
    mCalibAlgo     = tryInt( prefs,        keyCalib[6], defCalib[6] );   // DISTOX_CALIB_ALGO choice: 0, 1, 2
    mAlgoMinAlpha  = tryFloat( prefs,      keyCalib[7], defCalib[7] );   // DISTOX_ALGO_MIN_ALPHA
    mAlgoMinBeta   = tryFloat( prefs,      keyCalib[8], defCalib[8] );   // DISTOX_ALGO_MIN_BETA
    mAlgoMinGamma  = tryFloat( prefs,      keyCalib[9], defCalib[9] );   // DISTOX_ALGO_MIN_GAMMA
    mAlgoMinDelta  = tryFloat( prefs,      keyCalib[10], defCalib[10] ); // DISTOX_ALGO_MIN_DELTA

    String[] keyDevice = TDPrefKey.DEVICE;
    String[] defDevice = TDPrefKey.DEVICEdef;
    mConnectionMode = tryInt( prefs,    keyDevice[1], defDevice[1] );        // DISTOX_CONN_MODE choice: 0, 1, 2
    mAutoReconnect  = prefs.getBoolean( keyDevice[2], bool(defDevice[2]) );  // DISTOX_AUTO_RECONNECT
    mHeadTail       = prefs.getBoolean( keyDevice[3], bool(defDevice[3]) );  // DISTOX_HEAD_TAIL
    mSockType       = tryInt( prefs,    keyDevice[4], mDefaultSockStrType ); // DISTOX_SOCK_TYPE choice: 0, 1, (2, 3)
    // mCommRetry      = tryInt( prefs,    keyDevice[5], bool(defDevice[]) );   // DISTOX_COMM_RETRY
    mZ6Workaround   = prefs.getBoolean( keyDevice[5], bool(defDevice[5])  ); // DISTOX_Z6_WORKAROUND
    mConnectSocketDelay = tryInt(prefs, keyDevice[6], defDevice[6] );        // DISTOX_SOCKET_DELAY
    mAutoPair       = prefs.getBoolean( keyDevice[7], bool(defDevice[7]) );  // DISTOX_AUTO_PAIR
    mWaitLaser      = tryInt( prefs,    keyDevice[ 8], defDevice[ 8] ); // DISTOX_WAIT_LASER
    mWaitShot       = tryInt( prefs,    keyDevice[ 9], defDevice[ 9] ); // DISTOX_WAIT_SHOT
    mWaitData       = tryInt( prefs,    keyDevice[10], defDevice[10] ); // DISTOX_WAIT_DATA
    mWaitConn       = tryInt( prefs,    keyDevice[11], defDevice[11] ); // DISTOX_WAIT_CONN

    String[] keyExport = TDPrefKey.EXPORT;
    String[] defExport = TDPrefKey.EXPORTdef;
    mLRExtend          = prefs.getBoolean(     keyExport[ 1], bool(defExport[ 1]) ); // DISTOX_SPLAY_EXTEND
    mImportDatamode    = tryInt(   prefs,      keyExport[ 2],      defExport[ 2] );  // DISTOX_IMPORT_DATAMODE
    mExportShotsFormat = tryInt(   prefs,      keyExport[ 3],      defExport[ 3] );  // DISTOX_EXPORT_SHOTS choice: 
    mExportPlotFormat  = tryInt(   prefs,      keyExport[ 4],      defExport[ 4] );  // DISTOX_EXPORT_PLOT choice: 14, 2, 11, 12, 13
    mTherionMaps       = prefs.getBoolean(     keyExport[ 5], bool(defExport[ 5]) ); // DISTOX_THERION_MAPS
    mAutoStations      = prefs.getBoolean(     keyExport[ 6], bool(defExport[ 6]) ); // DISTOX_AUTO_STATIONS 
    // mXTherionAreas  = prefs.getBoolean(     keyExport[  ], bool(defExport[  ]) ); // DISTOX_XTHERION_AREAS
    mTherionSplays     = prefs.getBoolean(     keyExport[ 7], bool(defExport[ 7]) ); // DISTOX_THERION_SPLAYS
    mExportStationsPrefix =  prefs.getBoolean( keyExport[ 8], bool(defExport[ 8]) ); // DISTOX_STATION_PREFIX
    mCompassSplays     = prefs.getBoolean(     keyExport[ 9], bool(defExport[ 9]) ); // DISTOX_COMPASS_SPLAYS
    mOrthogonalLRUDAngle = tryFloat( prefs,    keyExport[10],      defExport[10] );  // DISTOX_ORTHO_LRUD
    mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
    mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    mLRUDvertical      = tryFloat( prefs,      keyExport[11],      defExport[11] );  // DISTOX_LRUD_VERTICAL
    mLRUDhorizontal    = tryFloat( prefs,      keyExport[12],      defExport[12] );  // DISTOX_LRUD_HORIZONTAL
    mSwapLR            = prefs.getBoolean(     keyExport[13], bool(defExport[13]) ); // DISTOX_SWAP_LR
    mSurvexEol         = ( prefs.getString(    keyExport[14],      defExport[14] ).equals("LF") )? "\n" : "\r\n";  // DISTOX_SURVEX_EOL
    mSurvexSplay       =   prefs.getBoolean(   keyExport[15], bool(defExport[15]) ); // DISTOX_SURVEX_SPLAY
    mSurvexLRUD        =   prefs.getBoolean(   keyExport[16], bool(defExport[16]) ); // DISTOX_SURVEX_LRUD
    mBezierStep        = tryFloat( prefs,      keyExport[17],      defExport[17] ); // DISTOX_BEZIER_STEP
    mSvgGrid           = prefs.getBoolean(     keyExport[18], bool(defExport[18]) ); // DISTOX_SVG_GRID
    mSvgLineDirection  = prefs.getBoolean(     keyExport[19], bool(defExport[19]) ); // DISTOX_SVG_LINE_DIR
    // mSvgInHtml      = prefs.getBoolean(     keyExport[  ], bool(defExport[  ]) );     // DISTOX_SVG_IN_HTML
    mSvgPointStroke    = tryFloat( prefs,      keyExport[20],      defExport[20] );       // DISTOX_SVG_POINT_STROKE
    mSvgLabelStroke    = tryFloat( prefs,      keyExport[21],      defExport[21] );       // DISTOX_SVG_LABEL_STROKE
    mSvgLineStroke     = tryFloat( prefs,      keyExport[22],      defExport[22] );       // DISTOX_SVG_LINE_STROKE
    mSvgGridStroke     = tryFloat( prefs,      keyExport[23],      defExport[23] );       // DISTOX_SVG_GRID_STROKE
    mSvgShotStroke     = tryFloat( prefs,      keyExport[24],      defExport[24] );       // DISTOX_SVG_SHOT_STROKE
    mSvgLineDirStroke  = tryFloat( prefs,      keyExport[25],      defExport[25] );       // DISTOX_SVG_LINEDIR_STROKE
    mKmlStations       = prefs.getBoolean(     keyExport[26], bool(defExport[26]) ); // DISTOX_KML_STATIONS
    mKmlSplays         = prefs.getBoolean(     keyExport[27], bool(defExport[27]) ); // DISTOX_KML_SPLAYS
    mBitmapScale       = tryFloat( prefs,      keyExport[28],      defExport[28] );       // DISTOX_BITMAP_SCALE 
    setBitmapBgcolor( prefs.getString(         keyExport[29],      defExport[29] ) );     // DISTOX_BITMAP_BGCOLOR
    // mDxfScale    = tryFloat( prefs,         keyExport[  ],      defExport[  ] );         // DISTOX_DXF_SCALE
    mDxfBlocks        =  prefs.getBoolean(     keyExport[30], bool(defExport[30]) ); // DISTOX_DXF_BLOCKS
    mAcadVersion = tryInt(   prefs,            keyExport[31],      defExport[31] );       // DISTOX_ACAD_VERSION choice: 9, 13

    String[] keyData = TDPrefKey.DATA;
    String[] defData = TDPrefKey.DATAdef;
    mCloseDistance = tryFloat( prefs,          keyData[0], defData[0] );         // DISTOX_CLOSE_DISTANCE
    mMaxShotLength = tryFloat( prefs,          keyData[1], defData[1] );         // DISTOX_MAX_SHOT_LENGTH
    mMinLegLength  = tryFloat( prefs,          keyData[2], defData[2] );         // DISTOX_MIN_LEG_LENGTH
    mMinNrLegShots = tryInt(   prefs,          keyData[3], defData[3] );         // DISTOX_LEG_SHOTS choice: 2, 3, 4
    mRecentTimeout = tryInt(   prefs,          keyData[4], defData[4] );         // DISTOX_RECENT_TIMEOUT
    mDistoXBackshot= prefs.getBoolean(         keyData[5], bool(defData[5]) );   // DISTOX_BACKSHOT
    mExtendThr     = tryFloat( prefs,          keyData[6], defData[6]   );       // DISTOX_EXTEND_THR2
    mVThreshold    = tryFloat( prefs,          keyData[7], defData[7]   );       // DISTOX_VTHRESHOLD
    setLoopClosure( tryInt(   prefs,           keyData[9], defData[9] ) );       // DISTOX_LOOP_CLOSURE_VALUE
    mPrevNext      = prefs.getBoolean(         keyData[10], bool(defData[10]) ); // DISTOX_PREV_NEXT
    mBacksightInput = prefs.getBoolean(        keyData[11], bool(defData[11]) ); // DISTOX_BACKSIGHT
    // setMagAnomaly( prefs, prefs.getBoolean( keyData[], bool(defData[]) ) );   // DISTOX_MAG_ANOMALY
    mTimerWait     = tryInt(   prefs,          keyData[12], defData[12] );       // DISTOX_SHOT_TIMER
    mBeepVolume    = tryInt(   prefs,          keyData[13], defData[13] );       // DISTOX_BEEP_VOLUME

    String[] keyUnits = TDPrefKey.UNITS;
    String[] defUnits = TDPrefKey.UNITSdef;
    if ( prefs.getString( keyUnits[0], defUnits[0] ).equals( defUnits[0] ) ) {
      mUnitLength = 1.0f;
      mUnitLengthStr = "m";
    } else {
      mUnitLength = TopoDroidUtil.M2FT;
      mUnitLengthStr = "ft";
    }
    if ( prefs.getString( keyUnits[1],  defUnits[1] ).equals( defUnits[1] ) ) {
      mUnitAngle = 1.0f;
      mUnitAngleStr = "deg";
    } else {
      mUnitAngle = TopoDroidUtil.DEG2GRAD;
      mUnitAngleStr = "grad";
    }
    mUnitGrid       = tryFloat(  prefs, keyUnits[2], defUnits[2] );      // DISTOX_UNIT_GRID
    // Log.v("DistoX", "units grid " + mUnitGrid );
  
    String[] keyAcc = TDPrefKey.ACCURACY;
    String[] defAcc = TDPrefKey.ACCURACYdef;
    mAccelerationThr = tryFloat( prefs, keyAcc[0], defAcc[0] ); // DISTOX_ACCEL_PERCENT
    mMagneticThr     = tryFloat( prefs, keyAcc[1], defAcc[1] ); // DISTOX_MAG_PERCENT
    mDipThr          = tryFloat( prefs, keyAcc[2], defAcc[2] ); // DISTOX_DIP_THR

    String[] keyLoc = TDPrefKey.LOCATION;
    String[] defLoc = TDPrefKey.LOCATIONdef;
    mUnitLocation  = (prefs.getString( keyLoc[0], defLoc[0] ).equals(defLoc[0])) ? TDConst.DDMMSS  // DISTOX_UNIT_LOCATION
                                                                                 : TDConst.DEGREE;
    mCRS           = prefs.getString( keyLoc[1], defLoc[1] );                 // DISTOX_CRS

    String[] keyScreen = TDPrefKey.SCREEN;
    String[] defScreen = TDPrefKey.SCREENdef;
    mFixedThickness = tryFloat( prefs, keyScreen[0], defScreen[0]   ); // DISTOX_FIXED_THICKNESS
    mStationSize    = tryFloat( prefs, keyScreen[1], defScreen[1] );   // DISTOX_STATION_SIZE
    mDotRadius      = tryFloat( prefs, keyScreen[2], defScreen[2]   ); // DISTOX_DOT_RADIUS
    mSelectness     = tryFloat( prefs, keyScreen[3], defScreen[3] );   // DISTOX_CLOSENESS
    mEraseness      = tryFloat( prefs, keyScreen[4], defScreen[4] );   // DISTOX_ERASENESS
    mMinShift       = tryInt(   prefs, keyScreen[5], defScreen[5] );   // DISTOX_MIN_SHIFT
    mPointingRadius = tryInt(   prefs, keyScreen[6], defScreen[6] );   // DISTOX_POINTING
    mSplayVertThrs  = tryFloat( prefs, keyScreen[7], defScreen[7]  );  // DISTOX_SPLAY_VERT_THRS
    mDashSplay     = prefs.getBoolean( keyScreen[8], bool(defScreen[8]) );   // DISTOX_DASH_SPLAY
    mVertSplay      = tryFloat( prefs, keyScreen[9], defScreen[9] );   // DISTOX_VERT_SPLAY
    mHorizSplay     = tryFloat( prefs, keyScreen[10], defScreen[10] );  // DISTOX_HORIZ_SPLAY
    mCosHorizSplay = TDMath.cosd( mHorizSplay );
    mSectionSplay   = tryFloat( prefs, keyScreen[11], defScreen[11] );  // DISTOX_SECTION_SPLAY
    mHThreshold     = tryFloat( prefs, keyScreen[12], defScreen[12] );  // DISTOX_HTHRESHOLD

    String[] keyLine = TDPrefKey.LINE;
    String[] defLine = TDPrefKey.LINEdef;
    mLineThickness = tryFloat( prefs,     keyLine[0], defLine[0] );   // DISTOX_LINE_THICKNESS
    setLineStyleAndType( prefs.getString( keyLine[1], defLine[1] ) ); // DISTOX_LINE_STYLE
    mLineSegment   = tryInt(   prefs,     keyLine[2], defLine[2] );   // DISTOX_LINE_SEGMENT
    mLineSegment2  = mLineSegment * mLineSegment;
    mArrowLength       = tryFloat( prefs, keyLine[3], defLine[3] );   // DISTOX_ARROW_LENGTH
    mReduceAngle       = tryFloat( prefs, keyLine[4], defLine[4] );   // DISTOX_REDUCE_ANGLE
    mReduceCosine = (float)Math.cos( mReduceAngle * TDMath.DEG2RAD );
    mAutoSectionPt = prefs.getBoolean( keyLine[5], bool(defLine[5]) ); // DISTOX_AUTO_SECTION_PT
    mContinueLine  = tryInt(   prefs,  keyLine[6], defLine[6] );       // DISTOX_LINE_CONTINUE
    mAreaBorder = prefs.getBoolean(    keyLine[7], bool(defLine[7]) ); // DISTOX_AREA_BORDER
    mLineAccuracy  = tryFloat( prefs,  keyLine[8], defLine[8] );       // DISTOX_LINE_ACCURACY
    mLineCorner    = tryFloat( prefs,  keyLine[9], defLine[9] );       // DISTOX_LINE_CORNER

    String[] keyPoint = TDPrefKey.POINT;
    String[] defPoint = TDPrefKey.POINTdef;
    mUnscaledPoints = prefs.getBoolean( keyPoint[0], bool(defPoint[0]) ); // DISTOX_UNSCALED_POINTS
    mUnitIcons     = tryFloat( prefs,   keyPoint[1], defPoint[1] );       // DISTOX_DRAWING_UNIT 
    mLabelSize     = tryFloat( prefs,   keyPoint[2], defPoint[2] );       // DISTOX_LABEL_SIZE
    // mPlotCache  = prefs.getBoolean( keyPoint[], bool(defPoint[]) );    // DISTOX_PLOT_CACHE

    String[] keyWalls = TDPrefKey.WALLS;
    String[] defWalls = TDPrefKey.WALLSdef;
    mWallsType        = tryInt(   prefs, keyWalls[0], defWalls[0] ); // DISTOX_WALLS_TYPE choice: 0, 1
    mWallsPlanThr     = tryFloat( prefs, keyWalls[1], defWalls[1] ); // DISTOX_WALLS_PLAN_THR
    mWallsExtendedThr = tryFloat( prefs, keyWalls[2], defWalls[2] ); // DISTOX_WALLS_EXTENDED_THR
    mWallsXClose      = tryFloat( prefs, keyWalls[3], defWalls[3] ); // DISTOX_WALLS_XCLOSE
    mWallsXStep       = tryFloat( prefs, keyWalls[4], defWalls[4] ); // DISTOX_WALLS_XSTEP
    mWallsConcave     = tryFloat( prefs, keyWalls[5], defWalls[5] ); // DISTOX_WALLS_CONCAVE

    String[] keySketch = TDPrefKey.SKETCH;
    String[] defSketch = TDPrefKey.SKETCHdef;
    // mSketchUsesSplays  = prefs.getBoolean( keySketch[], bool(defSketch[]) );    
    mSketchModelType = tryInt(  prefs, keySketch[0], defSketch[0] );   // DISTOX_SKETCH_MODEL_TYPE
    mSketchSideSize = tryFloat( prefs, keySketch[1], defSketch[1] );   // DISTOX_SKETCH_LINE_STEP
    // mSketchBorderStep  = Float.parseFloat( prefs.getString( keySketch[], defSketch[] );
    // mSketchSectionStep = Float.parseFloat( prefs.getString( keySketch[], defSketch[] );
    mDeltaExtrude   = tryFloat( prefs, keySketch[2], defSketch[2]  );  // DISTOX_DELTA_EXTRUDE
    // mCompassReadings  = tryInt( prefs, keySketch[3], defSketch[] ); // DISTOX_COMPASS_READING 
  }

  // ----------------------------------------------------------------------------------

  static void updatePreference( TDPrefHelper hlp, int cat, String k, String v )
  {
    // Log.v("DistoXPref", "update pref " + k + " val " + v );
    switch ( cat ) {
      case TDPrefActivity.PREF_CATEGORY_ALL:    updatePrefMain( hlp, k, v ); break;
      case TDPrefActivity.PREF_CATEGORY_SURVEY: updatePrefSurvey( hlp, k, v ); break;
      case TDPrefActivity.PREF_CATEGORY_PLOT:   updatePrefPlot( hlp, k, v ); break;
      case TDPrefActivity.PREF_CATEGORY_CALIB:  updatePrefCalib( hlp, k, v ); break;
      case TDPrefActivity.PREF_CATEGORY_DEVICE: updatePrefDevice( hlp, k, v ); break;
      case TDPrefActivity.PREF_CATEGORY_EXPORT: updatePrefExport( hlp, k, v ); break;
      case TDPrefActivity.PREF_SHOT_DATA:       updatePrefData( hlp, k, v ); break;
      case TDPrefActivity.PREF_SHOT_UNITS:      updatePrefUnits( hlp, k, v ); break;
      case TDPrefActivity.PREF_ACCURACY:        updatePrefAccuracy( hlp, k, v ); break;
      case TDPrefActivity.PREF_LOCATION:        updatePrefLocation( hlp, k, v ); break;
      case TDPrefActivity.PREF_PLOT_SCREEN:     updatePrefScreen( hlp, k, v ); break;
      case TDPrefActivity.PREF_TOOL_LINE:       updatePrefLine( hlp, k, v ); break;
      case TDPrefActivity.PREF_TOOL_POINT:      updatePrefPoint( hlp, k, v ); break;
      case TDPrefActivity.PREF_PLOT_WALLS:      updatePrefWalls( hlp, k, v ); break;
      case TDPrefActivity.PREF_PLOT_DRAW:       updatePrefDraw( hlp, k, v ); break;
      case TDPrefActivity.PREF_PLOT_ERASE:      updatePrefErase( hlp, k, v ); break;
      case TDPrefActivity.PREF_PLOT_EDIT:       updatePrefEdit( hlp, k, v ); break;
      case TDPrefActivity.PREF_CATEGORY_SKETCH: updatePrefSketch( hlp, k, v ); break;
      case TDPrefActivity.PREF_CATEGORY_LOG:    updatePrefLog( hlp, k, v ); break;
      default:
        TDLog.Error("DistoXPref. unhandled setting, cat " + cat + " key " + k + " val <" + v + ">" );
    }
  }


  private static void updatePrefMain( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.MAIN;
    if ( k.equals( key[0] ) ) {// DISTOX_CWD
      // handled independently
      TopoDroidApp.setCWD( tryStringValue( hlp, k, v, "TopoDroid" ), hlp.getString( "DISTOX_CBD", TDPath.PATH_BASEDIR ) );
    } else if ( k.equals( key[ 1 ] ) ) {              // DISTOX_TEXT_SIZE
      setTextSize( tryIntValue( hlp, k, v, defaultTextSize ) );
    } else if ( k.equals( key[ 2 ] ) ) {              // DISTOX_SIZE_BUTTONS
      if ( setSizeButtons( tryIntValue( hlp, k, v, defaultButtonSize ) ) ) {
	if ( TopoDroidApp.mActivity != null ) {
          TopoDroidApp.mActivity.resetButtonBar();
          // FIXME TOOLBAR TopoDroidApp.mActivity.resetToolbar();
	}
      }
    } else if ( k.equals( key[ 3 ] ) ) {             // DISTOX_EXTRA_BUTTONS
      int level = tryIntValue( hlp, k, v, "1" );
      setActivityBooleans( hlp.getSharedPrefs(), level );
    } else if ( k.equals( key[ 4 ] ) ) {           // DISTOX_MKEYBOARD 
      mKeyboard = tryBooleanValue( hlp, k, v, true );
    } else if ( k.equals( key[ 5 ] ) ) {           // DISTOX_NO_CURSOR
      mNoCursor = tryBooleanValue( hlp, k, v, false );
    } else if ( k.equals( key[ 6 ] ) ) {           // DISTOX_LOCAL_MAN
      mLocalManPages = handleLocalUserMan( hlp.getApp(), tryStringValue( hlp, k, v, "0" ), true );
    } else if ( k.equals( key[ 7 ] ) ) {           // DISTOX_COSURVEY
      boolean co_survey = tryBooleanValue( hlp, k, v, false );
      if ( co_survey != TopoDroidApp.mCoSurveyServer ) {
        hlp.getApp().setCoSurvey( co_survey ); // set flag and start/stop server
      }
    } else if ( k.equals( key[ 8 ] ) ) {           // DISTOX_LOCALE
      TopoDroidApp.setLocale( tryStringValue( hlp, k, v, "" ), true );
    } else {
      TDLog.Error("missing MAIN key: " + k );
    }
  }

  private static void updatePrefSurvey( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.SURVEY;
    String[] def = TDPrefKey.SURVEYdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_TEAM
      mDefaultTeam = tryStringValue( hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SURVEY_STATION
      parseStationPolicy( hlp, tryStringValue( hlp, k, v, def[1] ) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_STATION_NAMES
      mStationNames = (tryStringValue( hlp, k, v, def[2]).equals("number"))? 1 : 0;
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_INIT_STATION
      mInitStation = tryStringValue( hlp, k, v, def[3] ).replaceAll("\\s+", "");
      if ( mInitStation.length() == 0 ) mInitStation = def[3];
      DistoXStationName.setInitialStation( mInitStation );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_THUMBNAIL
      mThumbSize = tryIntValue( hlp, k, v, def[4] ); 
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_DATA_BACKUP
      mDataBackup = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_SHARED_XSECTIONS
      mSharedXSections  = tryBooleanValue( hlp, k, v, bool(def[6]) );
    } else {
      TDLog.Error("missing SURVEY key: " + k );
    }
  }

  private static void updatePrefPlot( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.PLOT;
    String[] def = TDPrefKey.PLOTdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_PICKER_TYPE
      mPickerType = tryIntValue(   hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_RECENT_NR
      mRecentNr = tryIntValue( hlp, k, v, def[1] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SIDE_DRAG
      mSideDrag = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_ZOOM_CTRL
      // setZoomControls( tryBooleanValue( hlp, k, bool(def[3]) ) );
      setZoomControls( tryStringValue( hlp, k, v, def[3] ), FeatureChecker.checkMultitouch( TDInstance.context ) );
    // } else if ( k.equals( key[ ? ] ) ) {  // DISTOX_SECTION_STATIONS
    //   mSectionStations = tryIntValue( hlp, k, v, def[ ] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_CHECK_ATTACHED
      mCheckAttached = tryBooleanValue( hlp, k, v, bool(def[4]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_CHECK_EXTEND
      mCheckExtend   = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_BACKUP_NUMBER
      mBackupNumber  = tryIntValue( hlp, k, v, def[6] ); 
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_BACKUP_INTERVAL
      mBackupInterval = tryIntValue( hlp, k, v, def[7] );  
    } else {
      TDLog.Error("missing PLOT key: " + k );
    }
  }

  private static void updatePrefCalib( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.CALIB;
    String[] def = TDPrefKey.CALIBdef;
    if ( k.equals( key[ 0 ] ) ) {
      mGroupBy       = tryIntValue(   hlp, k, v, def[0] );  // DISTOX_GROUP_BY (choice)
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_GROUP_DISTANCE
      mGroupDistance = tryFloatValue( hlp, k, v, def[1] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_CALIB_EPS
      mCalibEps      = tryFloatValue( hlp, k, v, def[2] );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_CALIB_MAX_IT
      mCalibMaxIt    = tryIntValue(   hlp, k, v, def[3] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_CALIB_SHOT_DOWNLOAD
      mCalibShotDownload = tryBooleanValue( hlp, k, v, bool(def[4]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_RAW_CDATA
      // mRawData       = tryBooleanValue( hlp, k, v, false );  // DISTOX_RAW_DATA
      mRawCData      = tryIntValue( hlp, k, v, def[5] ); 
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_CALIB_ALGO
      mCalibAlgo     = tryIntValue( hlp, k, v, def[6] );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_ALGO_MIN_ALPHA
      mAlgoMinAlpha   = tryFloatValue( hlp, k, v, def[7] );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_ALGO_MIN_BETA
      mAlgoMinBeta    = tryFloatValue( hlp, k, v, def[8] );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_ALGO_MIN_GAMMA
      mAlgoMinGamma   = tryFloatValue( hlp, k, v, def[9] );
    } else if ( k.equals( key[ 10 ] ) ) { // DISTOX_ALGO_MIN_DELTA
      mAlgoMinDelta   = tryFloatValue( hlp, k, v, def[10] ); 
    } else {
      TDLog.Error("missing CALIB key: " + k );
    }
  }

  private static void updatePrefDevice( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.DEVICE;
    String[] def = TDPrefKey.DEVICEdef;
    // DISTOX_DEVICE, unused here
    // DISTOX_DEVICE_TYPE, unused here
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_BLUETOOTH
      mCheckBT  = tryIntValue( hlp, k, v, def[0] ); 
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_CONN_MODE
      mConnectionMode = tryIntValue( hlp, k, v, def[1] ); 
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_AUTO_RECONNECT
      mAutoReconnect = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_HEAD_TAIL
      mHeadTail = tryBooleanValue( hlp, k, v, bool(def[3]) ); 
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_SOCK_TYPE
      mSockType = tryIntValue( hlp, k, v, mDefaultSockStrType ); 
    // } else if ( k.equals( key[ ] ) ) { // DISTOX_COMM_RETRY
    //   mCommRetry = tryIntValue( hlp, k, v, def[ ] );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_Z6_WORKAROUND
      mZ6Workaround = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_SOCKET_DELAY
      mConnectSocketDelay = tryIntValue( hlp, k, v, def[6] );  
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_AUTO_PAIR
      mAutoPair = tryBooleanValue( hlp, k, v, bool(def[7]) );
      hlp.getApp().checkAutoPairing();
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_WAIT_DATA
      mWaitData = tryIntValue( hlp, k, v, def[8] ); 
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_WAIT_CONN
      mWaitConn = tryIntValue( hlp, k, v, def[9] );
    } else if ( k.equals( key[ 10 ] ) ) { // DISTOX_WAIT_LASER
      mWaitLaser = tryIntValue( hlp, k, v, def[10] );
    } else if ( k.equals( key[ 11 ] ) ) { // DISTOX_WAIT_SHOT
      mWaitShot  = tryIntValue( hlp, k, v, def[11] );
    } else {
      TDLog.Error("missing DEVICE key: " + k );
    }
  }

  private static void updatePrefExport( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.EXPORT;
    String[] def = TDPrefKey.EXPORTdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_PT_CMAP
      // not handled here
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SPLAY_EXTEND
      mLRExtend = tryBooleanValue( hlp, k, v, bool(def[ 1]) ); 
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_IMPORT_DATAMODE
      mImportDatamode = tryIntValue( hlp, k, v, def[ 2] );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_EXPORT_SHOTS (choice)
      mExportShotsFormat = tryIntValue( hlp, k, v, def[ 3] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_EXPORT_PLOT (choice)
      mExportPlotFormat = tryIntValue( hlp, k, v, def[ 4] );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_THERION_MAPS
      mTherionMaps = tryBooleanValue( hlp, k, v, bool(def[ 5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_AUTO_STATIONS
      mAutoStations = tryBooleanValue( hlp, k, v, bool(def[ 6]) );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_XTHERION_AREAS
    //   mXTherionAreas = tryBooleanValue( hlp, k, v, bool(def[ ]) );   
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_THERION_SPLAYS
      mTherionSplays  = tryBooleanValue( hlp, k, v, bool(def[ 7]) );   
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_STATION_PREFIX
      mExportStationsPrefix = tryBooleanValue( hlp, k, v, bool(def[ 8]) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_COMPASS_SPLAYS
      mCompassSplays  = tryBooleanValue( hlp, k, v, bool(def[ 9]) );   
    } else if ( k.equals( key[10 ] ) ) { // DISTOX_ORTHO_LRUD
      mOrthogonalLRUDAngle  = tryFloatValue( hlp, k, v, def[10] );
      mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
      mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    } else if ( k.equals( key[ 11 ] ) ) { // DISTOX_LRUD_VERTICAL
      mLRUDvertical = tryFloatValue( hlp, k, v, def[11] );
    } else if ( k.equals( key[ 12 ] ) ) { // DISTOX_LRUD_HORIZONTAL
      mLRUDhorizontal = tryFloatValue( hlp, k, v, def[12] );
    } else if ( k.equals( key[ 13 ] ) ) { // DISTOX_SWAP_LR
      mSwapLR = tryBooleanValue( hlp, k, v, bool(def[13]) );
    } else if ( k.equals( key[ 14 ] ) ) { // DISTOX_SURVEX_EOL
      mSurvexEol = ( tryStringValue( hlp, k, v, def[14] ).equals(def[14]) )? "\n" : "\r\n";
    } else if ( k.equals( key[ 15 ] ) ) { // DISTOX_SURVEX_SPLAY
      mSurvexSplay = tryBooleanValue( hlp, k, v, bool(def[15]) );
    } else if ( k.equals( key[ 16 ] ) ) { // DISTOX_SURVEX_LRUD
      mSurvexLRUD = tryBooleanValue( hlp, k, v, bool(def[16]) );
    } else if ( k.equals( key[ 17 ] ) ) { // DISTOX_BEZIER_STEP
      mBezierStep  = tryFloatValue( hlp, k, v, def[17] );
    } else if ( k.equals( key[ 18 ] ) ) { // DISTOX_SVG_GRID
      mSvgGrid = tryBooleanValue( hlp, k, v, bool(def[18]) );
    } else if ( k.equals( key[ 19 ] ) ) { // DISTOX_SVG_LINE_DIR
      mSvgLineDirection = tryBooleanValue( hlp, k, v, bool(def[19]) );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_SVG_IN_HTML
    //   mSvgInHtml = tryBooleanValue( hlp, k, bool(def[ ]) );
    } else if ( k.equals( key[ 20 ] ) ) { // DISTOX_SVG_POINT_STROKE
      mSvgPointStroke    = tryFloatValue( hlp, k, v, def[20] );
    } else if ( k.equals( key[ 21 ] ) ) { // DISTOX_SVG_LABEL_STROKE
      mSvgLabelStroke    = tryFloatValue( hlp, k, v, def[21] );
    } else if ( k.equals( key[ 22 ] ) ) { // DISTOX_SVG_LINE_STROKE
      mSvgLineStroke     = tryFloatValue( hlp, k, v, def[22] );
    } else if ( k.equals( key[ 23 ] ) ) { // DISTOX_SVG_GRID_STROKE
      mSvgGridStroke     = tryFloatValue( hlp, k, v, def[23] );
    } else if ( k.equals( key[ 24 ] ) ) { // DISTOX_SVG_SHOT_STROKE
      mSvgShotStroke     = tryFloatValue( hlp, k, v, def[24] );
    } else if ( k.equals( key[ 25 ] ) ) { // DISTOX_SVG_LINEDIR_STROKE
      mSvgLineDirStroke  = tryFloatValue( hlp, k, v, def[25] );
    } else if ( k.equals( key[ 26 ] ) ) { // DISTOX_KML_STATIONS
      mKmlStations = tryBooleanValue( hlp, k, v, bool(def[26]) );
    } else if ( k.equals( key[ 27 ] ) ) { // DISTOX_KML_SPLAYS
      mKmlSplays = tryBooleanValue( hlp, k, v, bool(def[27]) );
    } else if ( k.equals( key[ 28 ] ) ) { // DISTOX_BITMAP_SCALE
      mBitmapScale = tryFloatValue( hlp, k, v, def[28] );
    } else if ( k.equals( key[ 29 ] ) ) { // DISTOX_BITMAP_BGCOLOR
      setBitmapBgcolor( tryStringValue( hlp, k, v, def[29] ) );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_DXF_SCALE
    //   mDxfScale = tryFloatValue( hlp, k, v, def[ ] );
    } else if ( k.equals( key[ 30 ] ) ) { // DISTOX_DXF_BLOCKS
      mDxfBlocks = tryBooleanValue( hlp, k, v, bool(def[30]) );
    } else if ( k.equals( key[ 31 ] ) ) { // DISTOX_ACAD_VERSION
      try { mAcadVersion = tryIntValue( hlp, k, v, def[31] ); } catch ( NumberFormatException e) { }
    } else {
      TDLog.Error("missing EXPORT key: " + k );
    }
  }

  private static void updatePrefData( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.DATA;
    String[] def = TDPrefKey.DATAdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_CLOSE_DISTANCE
      mCloseDistance  = tryFloatValue( hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_MAX_SHOT_LENGTH
      mMaxShotLength  = tryFloatValue( hlp, k, v, def[1] );  
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_MIN_LEG_LENGTH
      mMinLegLength   = tryFloatValue( hlp, k, v, def[2] );  
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_LEG_SHOTS
      mMinNrLegShots  = tryIntValue(   hlp, k, v, def[3] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_RECENT_TIMEOUT
      mRecentTimeout  = tryIntValue(   hlp, k, v, def[4] );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_BACKSHOT
      mDistoXBackshot = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_EXTEND_THR2
      mExtendThr      = tryFloatValue( hlp, k, v, def[6] );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_VTHRESHOLD
      mVThreshold     = tryFloatValue( hlp, k, v, def[7] );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_AZIMUTH_MANUAL
      mAzimuthManual  = tryBooleanValue( hlp, k, v, bool(def[8]) ); 
      TDAzimuth.resetRefAzimuth( TopoDroidApp.mShotWindow, TDAzimuth.mRefAzimuth );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_LOOP_CLOSURE_VALUE
      setLoopClosure( tryIntValue( hlp, k, v, def[9] ) );
    } else if ( k.equals( key[ 10 ] ) ) { // DISTOX_PREV_NEXT
      mPrevNext = tryBooleanValue( hlp, k, v, bool(def[10]) );
    } else if ( k.equals( key[ 11 ] ) ) { // DISTOX_BACKSIGHT
      mBacksightInput = tryBooleanValue( hlp, k, v, bool(def[11]) );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_MAG_ANOMALY
    //   setMagAnomaly( hlp.getSharedPrefs(), tryBooleanValue( hlp, k, v, bool(def[ ]) ) );
    } else if ( k.equals( key[ 12 ] ) ) { // DISTOX_SHOT_TIMER
      mTimerWait        = tryIntValue( hlp, k, v, def[12] );
    } else if ( k.equals( key[ 13 ] ) ) { // DISTOX_BEEP_VOLUME
      mBeepVolume       = tryIntValue( hlp, k, v, def[13] );
    } else {
      TDLog.Error("missing DATA key: " + k );
    }
  }

  private static void updatePrefUnits( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.UNITS;
    String[] def = TDPrefKey.UNITSdef;
    if ( k.equals( key[ 0 ] ) ) {    // DISTOX_UNIT_LENGTH
      if ( tryStringValue( hlp, k, v, def[0] ).equals(def[0]) ) {
        mUnitLength = 1.0f;
        mUnitLengthStr = "m";
      } else {
        mUnitLength = TopoDroidUtil.M2FT;
        mUnitLengthStr = "ft";
      }
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitLength changed " + mUnitLength );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_UNIT_ANGLE
      if ( tryStringValue( hlp, k, v, def[1] ).equals(def[1]) ) {
        mUnitAngle = 1.0f;
        mUnitAngleStr = "deg";
      } else {
        mUnitAngle = TopoDroidUtil.DEG2GRAD;
        mUnitAngleStr = "grad";
      }
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitAngle changed " + mUnitAngle );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_UNIT_GRID
      mUnitGrid = tryFloatValue( hlp, k, v, def[2] ); 
    } else {
      TDLog.Error("missing UNITS key: " + k );
    }
  }

  private static void updatePrefAccuracy( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.ACCURACY;
    String[] def = TDPrefKey.ACCURACYdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_ACCEL_PERCENT 
      mAccelerationThr = tryFloatValue( hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_MAG_PERCENT
      mMagneticThr     = tryFloatValue( hlp, k, v, def[1] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_DIP_THR
      mDipThr          = tryFloatValue( hlp, k, v, def[2] );
    } else {
      TDLog.Error("missing ACCURACY key: " + k );
    }
  }

  private static void updatePrefLocation( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.LOCATION;
    String[] def = TDPrefKey.LOCATIONdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNIT_LOCATION
      mUnitLocation  = tryStringValue( hlp, k, v, def[0] ).equals(def[0]) ? TDConst.DDMMSS : TDConst.DEGREE;
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_ALTITUDE
    //   try {
    //     mAltitude = Integer.parseInt( tryStringValue( hlp, k, v, ALTITUDE ) );
    //   } catch ( NumberFormatException e ) { mAltitude = _WGS84; }
    } else if ( k.equals( key[ 1 ] ) ) {
      mCRS = tryStringValue( hlp, k, v, def[1] );     // DISTOX_CRS
    } else {
      TDLog.Error("missing LOCATION key: " + k );
    }
  }


  private static void updatePrefScreen( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.SCREEN;
    String[] def = TDPrefKey.SCREENdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_FIXED_THICKNESS
      try {
        float f = tryFloatValue( hlp, k, v, def[0] );
        if ( f >= 0.5f && f <= 10 && f != mFixedThickness ) {
          mFixedThickness = f;
          BrushManager.setStrokeWidths();
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_STATION_SIZE
      try {
        setStationSize( Float.parseFloat( tryStringValue( hlp, k, v, def[1] ) ), true );
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_DOT_RADIUS
      mDotRadius    = tryFloatValue( hlp, k, v, def[2] );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_CLOSENESS
      mSelectness   = tryFloatValue( hlp, k, v, def[3] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_ERASENESS
      mEraseness    = tryFloatValue( hlp, k, v, def[4] );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_MIN_SHIFT
      mMinShift     = tryIntValue(  hlp, k, v, def[5] );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_POINTING
      mPointingRadius= tryIntValue( hlp, k, v, def[6] );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_SPLAY_VERT_THRS
      mSplayVertThrs = tryFloatValue( hlp, k, v, def[7] );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_DASH_SPLAY
      mDashSplay = tryBooleanValue( hlp, k, v, bool(def[8]) );      
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_VERT_SPLAY
      mVertSplay   = tryFloatValue( hlp, k, v, def[9] );
    } else if ( k.equals( key[ 10 ] ) ) { // DISTOX_HORIZ_SPLAY
      mHorizSplay  = tryFloatValue( hlp, k, v, def[10] );
      mCosHorizSplay = TDMath.cosd( mHorizSplay );
    } else if ( k.equals( key[ 11 ] ) ) { // DISTOX_SECTION_SPLAY
      mSectionSplay = tryFloatValue( hlp, k, v, def[11] );
    } else if ( k.equals( key[ 12 ] ) ) { // DISTOX_HTHRESHOLD
      mHThreshold = tryFloatValue( hlp, k, v, def[12] );
    } else {
      TDLog.Error("missing SCREEN key: " + k );
    }
  }

  private static void updatePrefLine( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.LINE;
    String[] def = TDPrefKey.LINEdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_LINE_THICKNESS
      try {
        float f = Float.parseFloat( tryStringValue( hlp, k, v, def[0] ) );
        if ( f >= 0.5f && f != mLineThickness ) {
          mLineThickness = f;
          BrushManager.reloadLineLibrary( TDInstance.context.getResources() );
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_LINE_STYLE
      setLineStyleAndType( tryStringValue( hlp, k, v, def[1] ) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LINE_SEGMENT
      mLineSegment  = tryIntValue(   hlp, k, v, def[2] );
      mLineSegment2 = mLineSegment * mLineSegment;
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_ARROW_LENGTH
      mArrowLength = tryFloatValue( hlp, k, v, def[3] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_REDUCE_ANGLE
      mReduceAngle  = tryFloatValue( hlp, k, v, def[4] );
      mReduceCosine = (float)Math.cos( mReduceAngle * TDMath.DEG2RAD );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_AUTO_SECTION_PT
      mAutoSectionPt = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_LINE_CONTINUE
      mContinueLine  = tryIntValue( hlp, k, v, def[6] );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_AREA_BORDER
      mAreaBorder = tryBooleanValue( hlp, k, v, bool(def[7]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_LINE_ACCURACY
      mLineAccuracy = tryFloatValue( hlp, k, v, def[8] );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_LINE_CORNER
      mLineCorner   = tryFloatValue( hlp, k, v, def[9] );
    } else {
      TDLog.Error("missing LINE key: " + k );
    }
  }

  private static void updatePrefPoint( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.POINT;
    String[] def = TDPrefKey.POINTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNSCALED_POINTS
      mUnscaledPoints = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_DRAWING_UNIT
      try { setDrawingUnits( tryFloatValue( hlp, k, v, def[1] ) ); } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LABEL_SIZE
      try {
        setLabelSize( Float.parseFloat( tryStringValue( hlp, k, v, def[2] ) ), true );
      } catch ( NumberFormatException e ) { }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
    } else {
      TDLog.Error("missing POINT key: " + k );
    }
  }

  private static void updatePrefWalls( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.WALLS;
    String[] def = TDPrefKey.WALLSdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_WALLS_TYPE
      mWallsType = tryIntValue(hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_WALLS_PLAN_THR
      mWallsPlanThr = tryFloatValue( hlp, k, v, def[1] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_WALLS_EXTENDED_THR
      mWallsExtendedThr = tryFloatValue( hlp, k, v, def[2] );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_WALLS_XCLOSE
      mWallsXClose = tryFloatValue( hlp, k, v, def[3] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_WALLS_CONCAVE
      mWallsConcave = tryFloatValue( hlp, k, v, def[4] );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_WALLS_XSTEP
      mWallsXStep = tryFloatValue( hlp, k, v, def[5] );
    } else {
      TDLog.Error("missing WALLS key: " + k );
    }
  }

  private static void updatePrefDraw( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.DRAW;
    String[] def = TDPrefKey.DRAWdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNSCALED_POINTS
      mUnscaledPoints = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_DRAWING_UNIT
      try { setDrawingUnits( tryFloatValue( hlp, k, v, def[1] ) ); } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LABEL_SIZE
      try {
        setLabelSize( Float.parseFloat( tryStringValue( hlp, k, v, def[2] ) ), true );
      } catch ( NumberFormatException e ) { }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_LINE_THICKNESS
      try {
        float f = Float.parseFloat( tryStringValue( hlp, k, v, def[3] ) );
        if ( f >= 0.5f && f != mLineThickness ) {
          mLineThickness = f;
          BrushManager.reloadLineLibrary( TDInstance.context.getResources() );
        }
      } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_LINE_STYLE
      setLineStyleAndType( tryStringValue( hlp, k, v, def[4] ) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_LINE_SEGMENT
      mLineSegment  = tryIntValue(   hlp, k, v, def[5] );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_ARROW_LENGTH
      mArrowLength = tryFloatValue( hlp, k, v, def[6] );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_REDUCE_ANGLE
      mReduceAngle  = tryFloatValue( hlp, k, v, def[7] );
      mReduceCosine = (float)Math.cos( mReduceAngle * TDMath.DEG2RAD );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_AUTO_SECTION_PT
      mAutoSectionPt = tryBooleanValue( hlp, k, v, bool(def[8]) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_LINE_CONTINUE
      mContinueLine  = tryIntValue( hlp, k, v, def[9] );
    } else if ( k.equals( key[ 10 ] ) ) { // DISTOX_AREA_BORDER
      mAreaBorder = tryBooleanValue( hlp, k, v, bool(def[10]) );
    } else if ( k.equals( key[ 11 ] ) ) { // DISTOX_LINE_ACCURACY
      mLineAccuracy = tryFloatValue( hlp, k, v, def[11] );
    } else if ( k.equals( key[ 12 ] ) ) { // DISTOX_LINE_CORNER
      mLineCorner   = tryFloatValue( hlp, k, v, def[12] );
    } else {
      TDLog.Error("missing DRAW key: " + k );
    }
  }

  private static void updatePrefErase( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.ERASE;
    String[] def = TDPrefKey.ERASEdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_CLOSENESS
      mSelectness   = tryFloatValue( hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_ERASENESS
      mEraseness    = tryFloatValue( hlp, k, v, def[1] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_POINTING
      mPointingRadius= tryIntValue(   hlp, k, v, def[2] );
    } else {
      TDLog.Error("missing ERASE key: " + k );
    }
  }

  private static void updatePrefEdit( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.EDIT;
    String[] def = TDPrefKey.EDITdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_DOT_RADIUS
      mDotRadius = tryFloatValue( hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_CLOSENESS
      mSelectness   = tryFloatValue( hlp, k, v, def[1] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_MIN_SHIFT
      mMinShift     = tryIntValue(  hlp, k, v, def[2] );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_POINTING
      mPointingRadius= tryIntValue(   hlp, k, v, def[3] );
    } else {
      TDLog.Error("missing EDIT key: " + k );
    }
  }

  private static void updatePrefSketch( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.SKETCH;
    String[] def = TDPrefKey.SKETCHdef;
    // if ( k.equals( key[ ? ] ) ) {
    //   mSketchUsesSplays = tryBooleanValue( hlp, k, v, bool(def[ ]) );
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SKETCH_MODEL_TYPE
      mSketchModelType = tryIntValue(  hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // 0.5 meter // DISTOX_SKETCH_LINE_STEP
      mSketchSideSize = tryFloatValue( hlp, k, v, def[1] );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_BORDER_STEP
    //   mSketchBorderStep  = tryFloatValue( hlp, k, v, def[ ] );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_SECTION_STEP
    //   mSketchSectionStep = tryFloatValue( hlp, k, v, def[ ] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_DELTA_EXTRUDE
      mDeltaExtrude = tryFloatValue( hlp, k, v, def[2] );
    // } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_COMPASS_READINGS
    //   mCompassReadings = tryIntValue( hlp, k, v, def[ ] );
    } else {
      TDLog.Error("missing SKETCH key: " + k );
    }
  }

  private static void updatePrefLog( TDPrefHelper hlp, String k, String v )
  {
    TDLog.checkLogPreferences( hlp.getSharedPrefs(), k ); // FIXME_PREF
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

  private static void setActivityBooleans( SharedPreferences prefs, int level )
  {
    if ( level == TDLevel.mLevel ) return;

    if ( StationPolicy.policyDowngrade( level ) ) {
      setPreference( prefs, TDPrefKey.SURVEY[1], "1" );
    }
    TDLevel.setLevel( TDInstance.context, level );
    int policy = StationPolicy.policyUpgrade( level );
    if ( policy > 0 ) {
      setPreference( prefs, TDPrefKey.SURVEY[1], Integer.toString( policy ) );
    }
    // if ( ! TDLevel.overExpert ) {
    //   mMagAnomaly = false; // magnetic anomaly compensation requires level overExpert
    // }
    if ( TopoDroidApp.mActivity != null ) {
      TopoDroidApp.mActivity.resetButtonBar();
      // FIXME TOOLBAR TopoDroidApp.mActivity.resetToolbar();
      TopoDroidApp.mActivity.setMenuAdapter( TDInstance.context.getResources() );
    }
    if ( TDPrefActivity.mPrefActivityAll != null ) TDPrefActivity.mPrefActivityAll.reloadPreferences();
  }

  private static int parseStationPolicy( TDPrefHelper hlp, String str ) 
  {
    int policy = StationPolicy.SURVEY_STATION_FOREWARD;
    try {
      policy = Integer.parseInt( str );
    } catch ( NumberFormatException e ) {
      policy = StationPolicy.SURVEY_STATION_FOREWARD;
    }
    if ( ! setStationPolicy( policy ) ) {
      // preference is reset to the last saved policy
      hlp.update( TDPrefKey.SURVEY[1], Integer.toString( StationPolicy.savedPolicy() ) );
      if ( TDPrefActivity.mPrefActivitySurvey != null ) TDPrefActivity.mPrefActivitySurvey.reloadPreferences(); // FIXME_PREF
    }
    // if ( ! mBacksightShot ) clearMagAnomaly( hlp.getSharedPrefs() );
    // Log.v("DistoX", "PARSE Policy " + policy + " saved " + StationPolicy.savedPolicy() );
    return policy;
  }

  private static boolean setStationPolicy( int policy )
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
    // Log.v("DistoXPref", "TDSetting set pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putString( name, value );
    applyEditor( editor );
  }

  static private void setPreference( SharedPreferences sp, String name, boolean value )
  {
    // Log.v("DistoXPref", "TDSetting set b-pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putBoolean( name, value );
    applyEditor( editor );
  }

  static void setPreference( SharedPreferences sp, String name, long value )
  {
    // Log.v("DistoXPref", "TDSetting set l-pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putLong( name, value );
    applyEditor( editor );
  }

  static long getLongPreference( SharedPreferences sp, String name, long def_value )
  {
    return sp.getLong( name, def_value ); 
  }

  // -----------------------------------------------------------------------
  //
  static boolean handleLocalUserMan( TopoDroidApp my_app, String man, boolean download ) 
  {
    int idx = Integer.parseInt( man ); // no throw
    if ( idx > 0 && idx < 5 ) { 
      if ( download && FeatureChecker.checkInternet( TDInstance.context ) ) { // download user manual 
       	int[] res = {
	         0,
	         R.string.user_man_fr,
	         R.string.user_man_es,
	         R.string.user_man_it,
	         R.string.user_man_ru
	       };
        String url = TDInstance.context.getResources().getString( res[idx] );
       	if ( url != null && url.length() > 0 ) {
          // try do download the zip
          (new UserManDownload( my_app, url )).execute();
	}
      }
      return true;
    }
    return false;
  }
}
