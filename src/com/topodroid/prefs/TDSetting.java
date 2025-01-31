/* @file TDSetting.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid application settings (preferences)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.prefs;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDLocale;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
// import com.topodroid.utils.TDColor;
import com.topodroid.help.UserManDownload;
import com.topodroid.TDX.TDLevel;
import com.topodroid.TDX.TDConst;
import com.topodroid.TDX.TDandroid;
// import com.topodroid.TDX.TDPath;
import com.topodroid.TDX.TDAzimuth;
import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.TDX.BrushManager;
import com.topodroid.TDX.DistoXStationName;
import com.topodroid.TDX.StationPolicy;
import com.topodroid.TDX.SurveyInfo;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.TopoGL;
import com.topodroid.TDX.GlRenderer;
import com.topodroid.TDX.GlModel;
import com.topodroid.TDX.GlNames;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.R;
import com.topodroid.dev.bric.BricMode; // MODE

import java.util.Locale;

import java.io.File; // PRIVATE FILE
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

import android.preference.PreferenceManager;
import android.content.res.Resources;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TDSetting
{
  public static final int FEEDBACK_NONE = 0;
  public static final int FEEDBACK_BELL = 1;
  public static final int FEEDBACK_VIBRATE = 2;
 
  public static final int DASHING_NONE    = 0;
  public static final int DASHING_CLINO   = 1;
  public static final int DASHING_AZIMUTH = 2;
  public static final int DASHING_VIEW    = 3;

  private static String defaultTextSize   = "16";
  private static String defaultButtonSize = TDString.THREE;

  private static int FLAG_BUTTON = 1;
  private static int FLAG_MENU   = 2;
  private static int FLAG_TEXT   = 4;
  private static int FLAG_LOCALE = 8;
  private static int FLAG_LEVEL  = FLAG_BUTTON | FLAG_MENU;
  private static int mMainFlag = 0xff; // Main Window flag

  private static final int POLICY_INDEX = 2; // index of station-policy in SURVEY settings

  public static boolean mWithDebug = false;

  /** reset MainWindow flag
   */
  public static void resetFlag() 
  { 
    // TDLog.v("SETTINGS clear main flag");
    mMainFlag = 0; 
  }

  /** @return true if Main flag button bit is set
   */
  public static boolean isFlagButton() { return ( mMainFlag & FLAG_BUTTON ) != 0; }

  /** @return true if Main flag menu bit is set
   */
  public static boolean isFlagMenu() { return ( mMainFlag & FLAG_MENU ) != 0; }

  /** @return true if Main flag text bit is set
   */
  public static boolean isFlagText() { return ( mMainFlag & FLAG_TEXT) != 0; }

  /** @return true if Main flag language bit is set
   */
  public static boolean isFlagLocale() { return ( mMainFlag & FLAG_LOCALE ) != 0; }

  /** clear language bit in the Main flag
   */
  public static void clearFlagLocale() { mMainFlag &= ~FLAG_LOCALE; }

  /** set the size of the texts
   * @param ts   text size [dip]
   */
  public static String setTextSize( int ts )
  {
    float ds = TopoDroidApp.getDisplayDensity() / 3.0f;
    mTextSize = (int)( ( ds * ts ) );
    if ( mTextSize < MIN_SIZE_TEXT ) {
      mTextSize = MIN_SIZE_TEXT;
      mMainFlag |= FLAG_TEXT;
      return Integer.toString( mTextSize );
    }
    return null;
  }

  /** @return tentative text size
   * @param ts   text size [dip]
   */
  public static int getTextSize( int ts )
  {
    float ds = TopoDroidApp.getDisplayDensity() / 3.0f;
    return (int)( ( ds * ts ) );
  }

  /** set the size of the label text
   * @param f      label text size [pt]
   * @param brush  if true set the text sizes also in the BrushManager
   */
  public static boolean setLabelSize( float f, boolean brush )
  {
    if ( f >= 1 && f != mLabelSize ) {
      mLabelSize = f;
      if ( brush ) BrushManager.setTextSizes( );
      return true;
    }
    return false;
  }

  /** set the size of the station names
   * @param f      station name size [pt]
   * @param brush  if true set the text sizes also in the BrushManager
   */
  public static boolean setStationSize( float f, boolean brush )
  {
    if ( f >= 1 && f != mStationSize ) {
      mStationSize = f;
      if ( brush ) BrushManager.setTextSizes( );
      return true;
    }
    return false;
  }

  /** set the size of the sketching icons
   * @param f      unit size for icons [pt]
   */
  public static void setDrawingUnitIcons( float f )
  {
    if ( f > 0.1f && f != mUnitIcons ) {
      mUnitIcons = f;
      BrushManager.reloadPointLibrary( TDInstance.context, TDInstance.getResources() );
    }
  }

  /** set the size of the sketching lines
   * @param f      unit size for lines [pt]
   */
  public static void setDrawingUnitLines( float f )
  {
    if ( f > 0.1f && f != mUnitLines ) {
      mUnitLines = f;
      BrushManager.reloadLineLibrary( TDInstance.getResources() );
    }
  }

  public static String setSlopeLSide( int f )
  {
    mSlopeLSide = ( f < 1 )? 20 : f;
    return Integer.toString( mSlopeLSide );
  }
     

  public static String keyDeviceName() { return "DISTOX_DEVICE"; }

  // static final  String EXPORT_TYPE    = "th";    // DISTOX_EXPORT_TH

  // ------------ MAIN
  public static String  mDefaultTeam = "";
  public static int mTeamNames = 0; // whether to use therion team naming (separate with ';'): 0 text plain, 1 comma separated, 2: dialog

  public static final int MIN_SIZE_BUTTONS = 32; // minimum size of buttons
  public static final int MIN_SIZE_TEXT    = 12;
  public static final int BTN_SIZE_UNUSED  = 52; // a value that differs from the next five
  public static final int BTN_SIZE_SMALL   = 36;
  public static final int BTN_SIZE_NORMAL  = 42;
  public static final int BTN_SIZE_MEDIUM  = 48;
  public static final int BTN_SIZE_LARGE   = 64;
  public static final int BTN_SIZE_HUGE    = 84;
  public static int mSizeBtns     = 0;      // action bar buttons scale (3: medium)
  public static int mSizeButtons  = BTN_SIZE_UNUSED;     // default 52 
  public static int mTextSize     = 16;     // list text size 
  public static boolean mKeyboard = false;
  public static boolean mNoCursor = true;
  public static boolean mLocalManPages = true;
  public static float mItemButtonSize  = 5.0f;    // used in ItemButton
  // public static float mItemPathScale   = 2.0f; // referred from DrawingWindow

  public static boolean mPacketLog     = false;
  public static boolean mTh2Edit       = false;

  public static int mOrientation = 0; // 0 unspecified, 1 portrait, 2 landscape

  public static float mPictureMin =   5.0f; 
  public static float mPictureMax = 100.0f;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // IMPORT EXPORT
  public static int mImportDatamode    = 0;  // SurveyInfo.DATAMODE_NORMAL
  // public static boolean mExportTcsx    = true;
  public static int mExportShotsFormat = -1; // DISTOX_EXPORT_NONE this is the preferred format on the shot export dialog
  public static int mExportPlotFormat  = -1; // DISTOX_EXPORT_NONE this is the preferred format on the plot export dialog
  public static int mAutoExportPlotFormat  = -1; // DISTOX_EXPORT_NONE

  public static float mBezierStep  = 0.2f;
  public static float getBezierStep() { return ( mBezierStep < 0.1f )? 0.05f : (mBezierStep/2); }

  public static boolean mLRExtend           = true;   // whether to extend LR or not (Compass/VisualTopo input)
  public static float   mLRUDvertical       = 45;     // vertical splay for UD 
  public static float   mLRUDhorizontal     = 45;     // horizontal splay for LR 
  public static boolean mLRUDcount          = false;  // LRUD counter (false: use all)

  public static String  mSurvexEol          = "\n";
  public static boolean mSurvexSplay        = false; // splays with named TO
  public static boolean mSurvexLRUD         = false;
  public static int     mSurvexEPSG         = 0;     // Survex EPSG cs out

  public static boolean mSwapLR             = false; // swap LR in Compass export
  public static boolean mOrthogonalLRUD     = false; // whether angle > 0 
  public static float mOrthogonalLRUDAngle  = 0;     // angle
  public static float mOrthogonalLRUDCosine = 1;     // cosine of the angle

  // public static final boolean mExportUri = true;

  public static boolean mExportStationsPrefix = false;  // whether to prepend cave name to station in cSurvey/compass export
  public static String  mExportPrefix         = null;   // export prefix - only for the current run
  public static boolean mZipWithSymbols       = false;  // whether to add/load symbols to/from archive
  public static boolean mZipShare             = false;  // whether to share exported zip
  public static boolean mZipShareCategory     = false;  // DISTOX_ZIP_SHARE_CATEGORY
  public static boolean mZipOverwrite         = true;   // whether to overwrite exported zip

  // ------------ THERION
  public static final float THERION_SCALE = 196.8503937f; // 200 * 39.3700787402 / 40;
  public static int     mTherionScale = 100;
  public static boolean mTherionMaps   = false;
  public static float   mToTherion = THERION_SCALE / 100;
  // public static boolean mXTherionAreas = false;
  public static boolean mAutoStations  = true;  // whether to add stations automatically to scrap therion files
  public static boolean mTherionSplays = false; // whether to add splay segments to auto stations
  public static boolean mTherionSplaysAll = true; // whether to add splay segments to auto stations for all scraps, not just the first
  public static boolean mTherionWithConfig = false; // whether to embed thconfig commands in th file 
  public static boolean mTherionEmbedConfig = false; // whether to write survey.thconfig file (or embedded commands)
  public static boolean mTherionXvi    = false; // whether to add xvi image to th2

  // ------------ COMPASS WALLS
  public static boolean mCompassSplays = true;  // whether to add splays to Compass export
  public static boolean mWallsSplays   = true;  // whether to add splays to Walls export instead of wall shots
  // public static int     mWallsUD     = 80;      // walls UD threshold: Up/Down (angle - degrees)

  // ------------ TOPOROBOT
  public static boolean TRobotJB = false;  // Jean Botazzi TopoRobot

  // ------------ VTOPO
  public static boolean mVTopoSplays     = true;    // whether to add splays to VisualTopo export
  public static boolean mVTopoLrudAtFrom = false; 
  public static boolean mVTopoTrox       = false; 
  public static boolean mVTopoFaverjon   = false;
  // public static boolean mTherionPath = false; // whether to add surveypath to stations on import // NOT YET A SETTING

  // ------------- PDF
  public static final float PDF_SCALE     = 141.73f; // =AI =1/72 in // was 145.56 approx.
  public static float   mToPdf     = PDF_SCALE / 100;

  // ------------- SVG
  public static final float SVG_SCALE_INK = 188.97637795f; // 94.488189f; // 10 * 96 px/in / ( 25.4 mm/in * 40 px/m ) -> scale 1 : 100  Inkscape
  public static final float SVG_SCALE_AI  = 141.73228346f; // 70.866141732f; // A.I. 10 * 72 / ( 25.4 * 40 ) scale 1:100 Adobe Illustrator
  public static float   SVG_SCALE = SVG_SCALE_AI;
  public static float   mToSvg     = SVG_SCALE / 100;

  public static final int SVG_INKSCAPE    = 0;
  public static final int SVG_ILLUSTRATOR = 1;
  public static int mSvgProgram = SVG_ILLUSTRATOR;

  public static boolean mSvgRoundTrip  = false;
  public static boolean mSvgGrid       = false;
  // public static boolean mSvgInHtml     = false;
  public static boolean mSvgLineDirection = false;
  public static boolean mSvgSplays        = true;
  public static boolean mSvgGroups      = false;  // whether to group items by the type in SVG export
  public static float mSvgPointStroke   = 0.1f;
  public static float mSvgLabelStroke   = 0.3f;   // stroke-width
  public static float mSvgLineStroke    = 0.5f;
  public static float mSvgLineDirStroke = 2f;
  public static float mSvgGridStroke    = 0.5f;
  public static float mSvgShotStroke    = 0.5f;
  public static int   mSvgStationSize   = 20;     // font-size
  public static int   mSvgLabelSize     = 30;     // font-size

  // ----------- KML
  public static boolean mKmlStations   = true;
  public static boolean mKmlSplays     = false;

  // ----------- GPX
  public static boolean mGPXSingleTrack = false;

  // NO_PNG NO_PNM
  // raster image export has the issue of no control on the resolution (scale)
  // because the app cannot guarantee the siz of the bitmap
  //
  // public static float mBitmapScale = 1.5f;
  // public static int mBitmapBgcolor = 0x000000;

  // ------------- DXF
  public static int mAcadVersion = 9;       // AutoCAD version 9, or 13, or 16
  public static boolean mAcadSpline = true; // interpolated cubic
  public static boolean mAcadLayer = true; // HBX_DXF layer or linetype
  public static boolean mDxfBlocks = true; // DXF_BLOCKS data-export
  public static boolean mDxfReference    = false;  // whether to include XY reference in the export 
  // public static float mDxfScale    = 1.0f;

  // ------------- SHP
  public static boolean mShpGeoref = false;

  // ------------- CSV
  public static final char CSV_COMMA = ',';
  public static final char CSV_PIPE  = '|';
  public static final char CSV_TAB   = '\t';
  public static final char[] CSV_SEPARATOR = { CSV_COMMA, CSV_PIPE, CSV_TAB };
  public static boolean mCsvRaw        = false;
  public static char mCsvSeparator     = CSV_COMMA;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // LOCATION
  public static String mCRS = "Long-Lat";     // default coord ref system
  // public static final  String UNIT_LOCATION  = "ddmmss";
  public static int mUnitLocation = 0;        // 0 dec-degree, 1 ddmmss
  public static boolean mNegAltitude = false; // whether to allow negative altitudes
  public static int mFineLocation = 60;       // fine location time
  public static int mGeoImportApp = 0;
  public static boolean mEditableHGeo;
  // 1 Mobile Topographer
  // 2 GPX recorder
  // 4 GPS position

  public static void setGeoImportApp( Context context, int app )
  {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( context );
    mGeoImportApp = app;
    setPreference( sp, "DISTOX_GEOPOINT_APP", Integer.toString(mGeoImportApp) );
  }

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // CALIBRATION

  // public static final String GROUP_DISTANCE = "40";
  public static float mGroupDistance = 40;

  public static final float DISTOX_MAX_EPS  = 0.01f; // hard limit
  // public static final String CALIB_EPS      = "0.000001";
  public static float mCalibEps = 0.000001f; // calibration epsilon

  public static int   mCalibMaxIt = 200;     // calibration max nr of iterations
  public static boolean mCalibShotDownload = true;

  // calibration data grouping policies
  public static final int GROUP_BY_DISTANCE = 0; // DEPRECATED
  public static final int GROUP_BY_FOUR     = 1; // TopoDroid convention
  public static final int GROUP_BY_ONLY_16  = 2; // PocketTopo convention
  public static int mGroupBy = GROUP_BY_FOUR;  // how to group calib data

  // public static boolean mRawData = false;   // whether to display calibration raw data as well
  public static int   mRawCData  = 0;
  // public static int   mCalibAlgo = 0;   // calibration algorithm: 0 auto, 1 linear, 2 non-linear

  // ---------- CALIB ALGO MIN
  public static float mAlgoMinAlpha = 0.1f;
  public static float mAlgoMinBeta  = 4.0f;
  public static float mAlgoMinGamma = 1.0f;
  public static float mAlgoMinDelta = 1.0f;

  // ----------- AUTO CALIB
  public static float mAutoCalBeta  = 0.004f;
  public static float mAutoCalEta   = 0.04f;
  public static float mAutoCalGamma = 0.04f;
  public static float mAutoCalDelta = 0.04f;

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DEVICE
  private final static int CONN_MODE_BATCH      = 0; // on-demand connection mode
  private final static int CONN_MODE_CONTINUOUS = 1; // continuous connection mode
  private final static int CONN_MODE_MULTI      = 2;
  // private final static int CONN_MODE_DOUBLE     = 3;
  private static int mConnectionMode = CONN_MODE_BATCH; // DistoX connection mode

  public static boolean isConnectionModeBatch()      { return mConnectionMode != CONN_MODE_CONTINUOUS; }
  public static boolean isConnectionModeContinuous() { return mConnectionMode == CONN_MODE_CONTINUOUS; }
  public static boolean isConnectionModeMulti()      { return mConnectionMode == CONN_MODE_MULTI; }
  // public static boolean isConnectionModeDouble() { return mConnectionMode == CONN_MODE_DOUBLE; }

  public static String getConnectionMode() { return (mConnectionMode == CONN_MODE_BATCH)? "batch"
                                                  : (mConnectionMode == CONN_MODE_CONTINUOUS)? "continuous" 
                                                  : (mConnectionMode == CONN_MODE_MULTI)? "multi" : "unknown"; 
                                           }

  public static boolean mZ6Workaround  = false; // was true - hardcoded false
  public static boolean mUnnamedDevice = false; // BT_NONAME

  public static boolean mAutoReconnect = true;
  public static boolean mSecondDistoX  = false;
  public static boolean mHeadTail      = false; // whether to use readA3HeadTail to download the data (A3 only)
  public static boolean mAutoPair      = false;
  public static int mConnectSocketDelay = 0; // wait time if not paired [0.1 sec]

  public static boolean mFirmwareSanity = true; // enforce firmware sanity checks
  public static int mBricMode = BricMode.MODE_ALL;
  public static boolean mBricZeroLength = false; // whether to handle 0-length data
  public static boolean mBricIndexIsId  = false; // whether to display BRIC index instead of id
  public static boolean mSap5Bit16Bug   = true;  // whether to apply SAP5 bit-16 bug workaround

  // public static final boolean CHECK_BT = true;
  public static int mCheckBT = 1;        // BT: 0 disabled, 1 check on start, 2 enabled

  public static final int TD_SOCK_DEFAULT      = 0;    // BT socket type
  public static final int TD_SOCK_INSEC        = 1;
  public static final int TD_SOCK_PORT         = 2;
  public static final int TD_SOCK_INSEC_PORT   = 3;
  // public static final int TD_SOCK_INSEC_INVOKE = 4;
  // public static int mDefaultSockType = (android.os.Build.MANUFACTURER.equals("samsung") ) ? TD_SOCK_INSEC : TD_SOCK_DEFAULT;
  // public static String mDefaultSockStrType = "1"; // TDString.ONE; // (android.os.Build.MANUFACTURER.equals("samsung") ) ? TDString.ONE : TDString.ZERO;
  public static int mSockType = TD_SOCK_INSEC; // TD_SOCK_DEFAULT;

  public static int mCommRetry = 1; 
  // public static int mCommType  = 0; // 0: on-demand, 1: continuous, 2: multi REPLACED BY mConnectionMode

  public static int mWaitLaser = 2000;
  public static int mWaitShot  = 2000;
  public static int mWaitData  =  100;  // delay between data
  public static int mWaitConn  =  500;  // delay waiting a connection
  public static int mWaitCommand = 100;
  public static int mConnectFeedback  = FEEDBACK_NONE;

  public static boolean mCheckAttached = false; // whether to check is there are shots non-attached
  public static boolean mCheckExtend   = true;
  public static boolean mPrevNext      = true;  // whether to display prev-next buttons in shot dialog

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SHOTS
  public static float mVThreshold = 80f;   // vertical threshold (LRUD) - manual shots
  public static float mHThreshold;         // horizontal xsection threshold (if |clino| < mHThreshold)
  // public static boolean mDataBackup = false; // whether to export data when shot-window is closed
  public static boolean mDistoXBackshot   = false;
  public static boolean mEditableStations = false;
  // public static int mTitleColor = TDColor.TITLE_NORMAL;

  public static int mRecentTimeout     = 30; // 30 seconds

  // public static int mScreenTimeout = 60000; // 60 secs
  public static boolean mWithAzimuth  = false;
  public static int mTimerWait        = 10;    // Acc/Mag timer countdown (secs)
  public static int mBeepVolume       = 50;    // beep volume
  public static boolean mExtendFrac   = false;    // whether to use fractional extends
  public static boolean mShotRecent   = false;    // whether to highlight recent shots
  // public static int mCompassReadings  = 4;     // number of compass readings to average

  // public static final String CLOSE_DISTANCE = "0.05"; // 50 cm / 1000 cm
  // public static float   mCloseAngle    = 0.05f; 
  public static float   mCloseDistance = 0.05f; 
  // public static float   mDistTolerance = 1.0f;   // ratio of distance-tolerance to angle-tolerance

  public static int     mMinNrLegShots = 3;
  public static String  mInitStation   = TDString.ZERO; // guaranteed non-null non-empty
  public static boolean mBacksightInput = false;   // whether to add backsight fields in shot manual-input dialog
  public static float   mSplayVertThrs = 80;       // (sketch) include only splay with clino below threshold
  public static boolean mAzimuthManual = false;    // whether to manually set extend / or use reference azimuth
  public static int     mDashSplay     = DASHING_NONE; // whether dash-splay are plan-type (1), profile-type (2), or independent (0)
  public static float   mVertSplay     = 50;
  public static float   mHorizSplay    = 60;
  public static float   mCosHorizSplay = TDMath.cosd( mHorizSplay );
  public static float   mSectionSplay  = 60;
  public static float   mCosSectionSplay  = TDMath.cosd( mSectionSplay );

  public static int     mSplayDashColor = TDColor.SPLAY_LIGHT;
  public static int     mSplayDotColor  = TDColor.SPLAY_LIGHT;
  public static int     mSplayLatestColor = TDColor.SPLAY_LATEST;
  public static int     mStationNames  = 0;        // type of station names (0: alpha, 1: number)
  public static int     mSplayAlpha    = 80;       // splay alpha [default 80 out of 100]
  // public static boolean mSplayAsDot    = false;    // draw splays as dots

  // ----------- LOOP CLOSURE
  public static final int LOOP_NONE          = 0; // coincide with values in array.xml
  public static final int LOOP_CYCLES        = 1;
  public static final int LOOP_TRILATERATION = 3;
  public static final int LOOP_WEIGHTED      = 4;
  public static final int LOOP_SELECTIVE     = 5;
  public static int mLoopClosure = LOOP_NONE;      // loop closure: 0 none, 1 normal, 3 triangles
  public static float mLoopThr = 1.0f; // selective compensation threshold [%]
  
  // ----------- UNITS
  public static final  String UNIT_LENGTH         = "meters";
  public static final  String UNIT_ANGLE          = "degrees";
  // public static final  String UNIT_ANGLE_GRADS = "grads";
  // public static final  String UNIT_ANGLE_SLOPE = "slope";
  // conversion factor from internal units (m) to user units
  public static float mUnitLength = 1;
  public static float mUnitAngle  = 1;
  public static String mUnitLengthStr = "m";    // N.B. Therion syntax: "m", "ft"
  public static String mUnitAngleStr  = "deg";  // N.B. Therion syntax: "deg", "grad"

  // public static final String EXTEND_THR = TDString.TEN; 
  public static float mExtendThr = 10;          // extend legs in the interval (-10, +10) ortogonal to the reference azimuth
  public static boolean mBlunderShot  = false;  // skip intermediate leg blunder-shot
  public static boolean mSplayStation = true;   // re-assign station to splays, even if already have it, 
  public static boolean mSplayOnlyForward = false;  // assign station to splay group only forward
  public static boolean mBacksightSplay   = false;  // whether first splay is backsight check

  public static int mThumbSize = 200;           // thumbnail size
  public static boolean mWithSensors = false;   // whether sensors are enabled
  // public static boolean mWithTdManager  = false;       // whether TdManager is enabled
  // public static boolean mSplayActive = false;       // whether splays are attached to active station (if defined)
  // public static boolean mWithRename  = false;       // whether survey rename is enabled

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SKETCH DRAWING

  public static float mReduceAngle  = 45;    // minimal angle between segments of "straightened" lines
  public static float mReduceCosine = 0.7f;  // cosine of mReduceAngle

  // public static boolean mZoomControls = false;
  public static int mZoomCtrl = 0; // 0: gone, 1: temporary, 2: permanent
  public static boolean mSideDrag = true;
  public static boolean mTripleToolbar = false;

  // ------------- UNIT SIZES
  public static float mUnitIcons = 1.4f; // drawing unit icons
  public static float mUnitLines = 1.4f; // drawing unit lines
  public static float mUnitGrid    = 1;         // 1: meter, 0.9... yard
  public static float mUnitMeasure = -1;        // -1: grid-cell

  // selection_radius = cutoff + closeness / zoom
  public static final float mCloseCutoff = 0.01f; // minimum selection radius

  public static float mSelectness = 24f;            // selection radius
  public static float mEraseness = 36f;             // eraser radius
  public static int mMinShift = 60;                 // shift sensitivity
  public static int mPointingRadius = 24;
  public static boolean mStylusOnly = false;        // stylus only sketching - false by default
  public static float mStylusSize = 0;              // stylus size

  // public static final String LINE_SHIFT = "20.0";

  // public static final int PICKER_RECENT = 0; // Drawing-tools picker type
  // public static final int PICKER_LIST   = 1; 
  // public static final int PICKER_GRID   = 2;
  // public static final int PICKER_GRID_3 = 3;
  // public static int mPickerType = PICKER_LIST;
  // public static int mRecentNr     = 4;        // nr. most recent symbols
  public static boolean mSingleBack = false; // with single back
  public static boolean mPalettes = false;   // extra tools palettes
  // public static boolean mCompositeActions = false;
  public static boolean mWithLineJoin = false;  // with line join
  public static boolean mLegOnlyUpdate = false; // whether to update display of drawing window at every shot (not just at legs)
  public static boolean mFullAffine = false; // whether to do full affine transform or shift+scale only
  // public static boolean mLegProjection = true; // leg inclined-projection

  // ------------ LINES
  public static final int LINE_STYLE_BEZIER = 0;  // drawing line styles
  private static final int LINE_STYLE_ONE    = 1;
  private static final int LINE_STYLE_TWO    = 2;
  private static final int LINE_STYLE_THREE  = 3;
  private static final int LINE_STYLE_SIMPLIFIED = 4;
  // private static final String LINE_STYLE     = TDString.TWO;     // LINE_STYLE_TWO NORMAL
  public static int   mLineStyle = LINE_STYLE_BEZIER;    
  public static int   mLineType;        // line type:  1       1     2    3
  public static int   mLineSegment   = 10;
  public static int   mLineSegment2  = 100;   // square of mLineSegment
  public static float mLineAccuracy  = 1f;
  public static float mLineCorner    = 20;    // corner threshold
  // public static int   mContinueLine  = DrawingWindow.CONT_NONE; // 0
  public static boolean mLineClose = true;
  public static int     mLineEnds  = 3;       // number of points to drop from line ends

  // ---------- WEEDING
  public static float mWeedDistance  = 0.5f;  // max weeding distance
  public static float mWeedLength    = 2.0f;  // max weeding length
  public static float mWeedBuffer    = 10;    // weed segment buffer

  // public static boolean mWithLayers  = true; // false;
  public static int mWithLevels = 0;  // 0: no, 1: by class, 2: by item
  public static int mGraphPaperScale = 0;  // correction subtracted to the system display density
  public static boolean mSlantXSection = false; // whether to allow profile slanted xsections
  public static int mObliqueMax = 0; // in [10,80] if enabled, or 0 if disabled

  public static float mStationSize     = 20;   // size of station names [pt]
  public static float mLabelSize       = 24;   // size of labels [pt]
  public static boolean mScalableLabel = false; // whether labels scale with the drawing
  public static float mFixedThickness  = 1;    // width of fixed lines
  public static float mLineThickness   = 1;    // width of drawing lines
  public static boolean mAutoSectionPt = false;
  public static int   mBackupNumber    = 5;
  public static int   mBackupInterval  = 60;
  // public static boolean mBackupsClear = false; // CLEAR_BACKUPS
  public static boolean mFixedOrigin     = false; 
  public static boolean mSharedXSections = false; // default value
  public static boolean mAutoXSections   = true;  // auto save/export xsections with section points
  public static boolean mSavedStations   = false;
  // public static boolean mPlotCache       = true;  // default value
  public static float mDotRadius      = 5;  // radius of selection dots - splay dots are 1.5 as big
  public static float mArrowLength    = 8;
  public static int   mSlopeLSide     = 20;  // l-side of slope lines


  // NOTE not used, but could set a default for section splays
  // public static int mSectionStations = 3; // 1: From, 2: To, 3: both

  public static boolean mUnscaledPoints = false;
  public static boolean mAreaBorder     = true;
  public static boolean mLineSnap       = false;
  public static boolean mLineCurve      = false;
  public static boolean mLineStraight   = false;
  public static boolean mPathMultiselect = false;

  public static boolean mBedding        = false;
  public static int     mTripleShot     = FEEDBACK_NONE;  // leg feedback
  public static boolean mSplayClasses   = false;
  public static int     mDiscreteColors = 0;
  public static boolean mSplayColor     = false; // = (mDiscreteColors > 0)
  public static boolean mDivingMode     = false;

  public static boolean mPlotSplit      = false;
  public static boolean mPlotShift      = false;

  // FIXME_SKETCH_3D - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  public static boolean m3Dsketch    = false;  // whether 3D sketch is enabled
  public static float   mSplayBuffer = 2.0f; // sketch splay buffer size [m]
  /* 
  public static int   mSketchModelType = 1;
  public static float mSketchSideSize;
  public static float mDeltaExtrude;
  // public static boolean mSketchUsesSplays; // whether 3D models surfaces use splays
  // public static float mSketchBorderStep;
  // public static float mSketchSectionStep;
   * END_SKETCH_3D */

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DATA ACCURACY
  public static float mAccelerationThr =  1; // acceleration threshold (shot quality) [%]
  public static float mMagneticThr     =  1; // magnetic threshold [%]
  public static float mDipThr          =  2; // dip threshold [deg]
  private static float mSiblingThr     =  5; // sibling threshold [%]
  public static float mSiblingThrA     = 0.56f * 5; // sibling angle threshold [deg]
  public static float mSiblingThrD     = 0.05f;     // sibling distance threshold [m] 
  public static float mMaxShotLength   = 50; // max length of a shot (if larger it is overshoot) [m]
  public static float mMinLegLength    =  0; // min length of a leg (if shorter it is undershoot) [m]
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // AUTOWALLS
  // public static final int WALLS_NONE    = 0;
  // public static final int WALLS_CONVEX  = 1;
  // public static final int WALLS_DLN     = 2;
  // public static final int WALLS_LAST    = 2; // placeholder
  // public static int   mWallsType        = WALLS_NONE;
  // public static float mWallsPlanThr     = 70;
  // public static float mWallsExtendedThr = 45;
  // public static float mWallsXClose      = 0.1f;
  // public static float mWallsXStep       = 1.0f;
  // public static float mWallsConcave     = 0.1f;


  // ------------------------------------------------------------------
  // public static void setZoomControls( boolean ctrl )
  // {
  //   mZoomControls = ctrl;
  //   // FIXME forward setting to DrawingWindow
  // }
  public static void setZoomControls( String ctrl, boolean is_multitouch ) // PRIVATE
  {
    try {
      int i = Integer.parseInt( ctrl );
      if ( i >= 0 && i <= 2 ) mZoomCtrl = i;
      if ( mZoomCtrl == 0 && ( mStylusOnly || ! is_multitouch ) ) mZoomCtrl = 1;
    } catch ( NumberFormatException e ) {
      TDLog.e( e.getMessage() );
    }
  }

  /** set the sibling thresholds 
   * @param thr   main sibling threshold
   */
  private static String setSiblingThr( float thr ) 
  {
    String ret = null;
    if ( thr <= 1 ) {
      thr = 1.0f;
      ret = TDString.ONE;
    }
    mSiblingThr  = thr;
    mSiblingThrA = thr * 0.56f;
    mSiblingThrD = thr * 0.01f;
    return ret;
  }

  // NO_PNG background color RGB_565
  // private static String setBitmapBgcolor( SharedPreferences prefs, String key, String color, String def_value )
  // {
  //   int r=0, g=0, b=0;
  //   String[] vals = color.split("\\s+"); 
  //   color = def_value;
  //   if ( vals.length == 3 ) {
  //     try { 
  //       r = Integer.parseInt( vals[0] );
  //       g = Integer.parseInt( vals[1] );
  //       b = Integer.parseInt( vals[2] );
  //       if ( r > 255 ) r = 255; if ( r < 0 ) r = 0;
  //       if ( g > 255 ) g = 255; if ( g < 0 ) g = 0;
  //       if ( b > 255 ) b = 255; if ( b < 0 ) b = 0;
  //       color = r + " " + g + " " + b; // Integer.toString(r) + " " + Integer.toString(g) + " " + Integer.toString(b);
  //     } catch ( NumberFormatException e ) {
  //       r = g = b = 0;
  //     }
  //     // TDLog.v("Setting bitmap bg color <" + r + " " + g + " " + b +">" );
  //   }
  //   setPreference( prefs, key, color );
  //   mBitmapBgcolor = 0xff000000 | ( r << 16 ) | ( g << 8 ) | b;
  //   return color;
  // }

  // ------------------------------------------------------------------
  private static float tryFloat( SharedPreferences prefs, String key, String def_value )
  {
    float f = 0;
    try { f = Float.parseFloat( prefs.getString( key, def_value ) ); } 
    catch ( NumberFormatException e ) {
      TDLog.e("Float Format Error. Key " + key + " " + e.getMessage() );
      f = Float.parseFloat(def_value);
      setPreference( prefs, key, def_value );
    }
    return f;
  }

  private static int tryInt( SharedPreferences prefs, String key, String def_value )
  {
    int i = 0;
    // if ( key.equals("DISTOX_TEAM_DIALOG" ) ) {
    //   TDLog.v( "try int TEAM DIALOG " + key + ": default " + def_value + " value " + prefs.getString( key, def_value ) );
    // }
    try { i = Integer.parseInt( prefs.getString( key, def_value ) ); }
    catch( NumberFormatException e ) { 
      TDLog.e("Integer Format Error. Key " + key + " " + e.getMessage() );
      i = Integer.parseInt(def_value);
      setPreference( prefs, key, def_value );
    }
    // if ( key.equals("DISTOX_TEAM_DIALOG" ) ) {
    //   TDLog.v( "try int TEAM DIALOG " + key + ": return " + i );
    // }
    return i;
  }

  // color is stored as integer (string)
  private static int tryColor( SharedPreferences prefs, String key, String def_value )
  {
    return tryInt( prefs, key, def_value );
  }

  private static float tryFloatValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    float f = 0;
    if ( val == null ) { 
      f = Float.parseFloat(def_value);
      TDPrefHelper.update( key, def_value );
    } else {
      try {
        f = Float.parseFloat( val );
        TDPrefHelper.update( key, val );
      } catch ( NumberFormatException e ) {
        TDLog.e("Float Format Error. Key " + key + " " + e.getMessage() );
        f = Float.parseFloat(def_value);
        TDPrefHelper.update( key, def_value );
      }
    }
    return f;
  }

  private static int tryIntValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    int i = 0;
    if ( val == null ) { 
      i = Integer.parseInt(def_value);
      TDPrefHelper.update( key, def_value );
      // if ( key.equals("DISTOX_TEAM_DIALOG" ) ) {
      //   TDLog.v("TEAM DIALOG: null value - update " + key + ": " + def_value );
      // }
    } else {
      try {
        i = Integer.parseInt( val );
        TDPrefHelper.update( key, val );
        // if ( key.equals("DISTOX_TEAM_DIALOG" ) ) {
        //   TDLog.v("TEAM DIALOG: update " + key + ": " + val );
        // }
      } catch( NumberFormatException e ) { 
        TDLog.e("Integer Format Error. Key " + key + " " + e.getMessage() );
        i = Integer.parseInt(def_value);
        TDPrefHelper.update( key, def_value );
      }
    }
    return i;
  }

  // rrggbb as "rr gg bb" with values in [0,100]
  private static int tryColorValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    int color = 0xffffffff;
    try {
      color = Integer.parseInt( val );
    } catch( NumberFormatException e ) { 
      TDLog.e("Integer Format Error. Key " + key + " " + e.getMessage() );
      color = Integer.parseInt(def_value);
    }
    TDPrefHelper.update( key, Integer.toString(color) );
    return color;
  }

  private static String tryStringValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    if ( val == null ) val = def_value;
    TDPrefHelper.update( key, val );
    return val;
  }

  private static boolean tryBooleanValue( TDPrefHelper hlp, String key, String val, boolean def_value )
  {
    boolean i = def_value;
    if ( val != null ) {
      try {
        i = Boolean.parseBoolean( val );
      } catch( NumberFormatException e ) { 
        TDLog.e("Boolean Format Error. Key " + key + " " + e.getMessage() );
      }
    }
    TDPrefHelper.update( key, i );
    return i;
  }

  private static void setLoopClosure( int loop_closure )
  {
    mLoopClosure = loop_closure;
    if ( mLoopClosure == LOOP_CYCLES || mLoopClosure == LOOP_WEIGHTED ) {
      if ( ! TDLevel.overAdvanced ) mLoopClosure = LOOP_NONE;
    } else if ( mLoopClosure == LOOP_TRILATERATION || mLoopClosure == LOOP_SELECTIVE ) {
      if ( ! TDLevel.overExpert ) mLoopClosure = LOOP_NONE;
    }
  }

  private static String setSelectness( float s ) 
  {
    String ret = null;
    if ( s < 1 ) { s = 1; ret = TDString.ONE; }
    mSelectness = s;
    return ret;
  }

  private static String setEraseness( float s ) 
  {
    String ret = null;
    if ( s < 1 ) { s = 1; ret = TDString.ONE; }
    mEraseness = s;
    return ret;
  }

  private static String setDotRadius( float s ) 
  {
    String ret = null;
    if ( s < 1 ) { s = 1; ret = TDString.ONE; }
    if ( s > 100 ) { s = 100; ret = "100"; }
    mDotRadius = s;
    return ret;
  }

  private static String setMinShift( int s ) 
  {
    String ret = null;
    if ( s < 10 ) { s = 10; ret = TDString.TEN; }
    mMinShift = s;
    return ret;
  }

  private static String setPointingRadius( int s ) 
  {
    String ret = null;
    if ( s < 1 ) { s = 1; ret = TDString.ONE; }
    mPointingRadius = s;
    return ret;
  }

  private static String setStylusSize( float s ) // STYLUS_MM
  {
    if ( s <= 0 ) { 
      mStylusOnly = false;
      mStylusSize = 0;
      return TDString.ZERO;
    }
    mStylusOnly = true;
    mStylusSize = s;
    if ( mZoomCtrl == 0 ) mZoomCtrl = 1;
    return Float.toString( s );
  }

  // called only by TopoDroidApp.setButtonSize and internally
  public static boolean setSizeButtons( int size )
  {
    int sz = mSizeButtons;
    switch ( size ) {
      case 0: sz = BTN_SIZE_SMALL;  break;
      case 1: sz = BTN_SIZE_NORMAL; break;
      case 3: sz = BTN_SIZE_MEDIUM; break;
      case 4: sz = BTN_SIZE_LARGE;  break;
      case 2: sz = BTN_SIZE_HUGE;   break;
    }
    TDLog.v("SETTING set button size-index " + size + " size: current " + mSizeButtons + " new " + sz );
    mSizeBtns = size;
    if ( sz != mSizeButtons ) {
      mSizeButtons = (int)( sz * TopoDroidApp.getDisplayDensity() * 0.86f );
      // TDLog.v("SETTING Size " + size + " Btns " + mSizeBtns + " " + mSizeButtons );
      if ( mSizeButtons < MIN_SIZE_BUTTONS ) mSizeButtons = MIN_SIZE_BUTTONS;
      mMainFlag |= FLAG_BUTTON;
      return true;
    }
    return false;
  }

  // get tentative size of buttons
  public static int getSizeButtons( int size )
  {
    int sz = mSizeButtons;
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
  public static void loadPrimaryPreferences( /* TopoDroidApp my_app, */ Resources res, TDPrefHelper pref_hlp )
  {
    SharedPreferences prefs = pref_hlp.getSharedPrefs();
  
    defaultTextSize   = res.getString( R.string.default_textsize );
    defaultButtonSize = res.getString( R.string.default_buttonsize );
    // TDLog.v("SETTING default button size " + defaultButtonSize );

    // ------------------- GENERAL PREFERENCES
    String[] keyMain = TDPrefKey.MAIN;
    String[] defMain = TDPrefKey.MAINdef;
    int level = Integer.parseInt( prefs.getString( keyMain[2], defMain[2] ) ); // DISTOX_EXTRA_BUTTONS choice: 0, 1, 2, 3
    setActivityBooleans( prefs, level );

    String[] keyGeek = TDPrefKey.GEEK;
    String[] defGeek = TDPrefKey.GEEKdef;
    mSingleBack = prefs.getBoolean( keyGeek[0], bool(defGeek[0]) ); // DISTOX_SINGLE_BACK
    setPalettes(  prefs.getBoolean( keyGeek[1], bool(defGeek[1]) ) ); // DISTOX_PALETTES
    // setBackupsClear( prefs.getBoolean( keyGeek[1], bool(defGeek[1]) ) ); // DISTOX_BACKUPS_CLEAR CLEAR_BACKUPS
    mKeyboard = prefs.getBoolean( keyGeek[2], bool(defGeek[2]) ); // DISTOX_MKEYBOARD
    mNoCursor = prefs.getBoolean( keyGeek[3], bool(defGeek[3]) ); // DISTOX_NO_CURSOR
    mPacketLog = prefs.getBoolean( keyGeek[4], bool(defGeek[4]) ); // DISTOX_PACKET_LOGGER
    mTh2Edit   = prefs.getBoolean( keyGeek[5], bool(defGeek[5]) ); // DISTOX_TH2_EDIT
    mWithDebug = TDLevel.isDebugBuild() ? prefs.getBoolean( keyGeek[13], bool(defGeek[13]) ) : false; // DISTOX_WITH_DEBUG

    // String[] keyGPlot = TDPrefKey.GEEKPLOT;
    // String[] defGPlot = TDPrefKey.GEEKPLOTdef;

    setTextSize( tryInt(    prefs,     keyMain[0], defMain[0] ) );      // DISTOX_TEXT_SIZE
    setSizeButtons( tryInt( prefs,     keyMain[1], defMain[1] ) );      // DISTOX_SIZE_BUTTONS
    mLocalManPages = handleLocalUserMan( /* my_app, */ prefs.getString( keyMain[3], defMain[3] ), false ); // DISTOX_LOCAL_MAN
    setLocale( prefs.getString( keyMain[4], TDString.EMPTY ), false ); // DISTOX_LOCALE
    mOrientation = Integer.parseInt( prefs.getString( keyMain[5], defMain[5] ) ); // DISTOX_ORIENTATION choice: 0, 1, 2
    // setLocale( prefs.getString( keyMain[7], defMain[7] ), false ); // DISTOX_LOCALE
    // TDLog.Profile("locale");
    // boolean co_survey = prefs.getBoolean( keyMain[8], bool(defMain[8]) );        // DISTOX_COSURVEY 

    String[] keySurvey = TDPrefKey.SURVEY;
    String[] defSurvey = TDPrefKey.SURVEYdef;
    mDefaultTeam = prefs.getString( keySurvey[0], defSurvey[0] );               // DISTOX_TEAM
    mInitStation = prefs.getString( keySurvey[4], defSurvey[4] ).replaceAll("\\s+", "");  // DISTOX_INIT_STATION 
    if ( mInitStation.length() == 0 ) mInitStation = defSurvey[4];
    DistoXStationName.setInitialStation( mInitStation );

    String[] keyData = TDPrefKey.DATA;
    String[] defData = TDPrefKey.DATAdef;
    mAzimuthManual = prefs.getBoolean( keyData[6], bool(defData[6]) );   // DISTOX_AZIMUTH_MANUAL 
    // TDAzimuth.setAzimuthManual( mAzimuthManual ); // FIXME FIXED_EXTEND 20240603
    TDAzimuth.resetRefAzimuth( null, SurveyInfo.SURVEY_EXTEND_NORMAL, mAzimuthManual ); // BUG ?? may call setRefAzimuthButton on non-UI thread
    
    // ------------------- DEVICE PREFERENCES -def--fallback--min-max
    String[] keyDevice = TDPrefKey.DEVICE;
    String[] defDevice = TDPrefKey.DEVICEdef;
    // DISTOX_DEVICE - UNUSED HERE
    mCheckBT        = tryInt( prefs, keyDevice[0], defDevice[0] );        // DISTOX_BLUETOOTH choice: 0, 1, 2
  }

  public static void loadSecondaryPreferences( /* TopoDroidApp my_app, */ TDPrefHelper pref_hlp )
  {
    // TDLog.v("SETTING load secondary");
    SharedPreferences prefs = pref_hlp.getSharedPrefs();

    String[] keySurvey = TDPrefKey.SURVEY;
    String[] defSurvey = TDPrefKey.SURVEYdef;
    // int old = mTeamNames;
    mTeamNames    = tryInt(   prefs,     keySurvey[1],      defSurvey[1] );       // DISTOX_TEAM_DIALOG
    // TDLog.v("SETTING load secondary TEAM DIALOG " + old + " -> " + mTeamNames + " default " + defSurvey[1] );
    parseStationPolicy( pref_hlp, prefs.getString( keySurvey[2], defSurvey[2] ) ); // DISTOX_SURVEY_STATION
    mStationNames = (prefs.getString(    keySurvey[3],      defSurvey[3] ).equals("number"))? 1 : 0; // DISTOX_STATION_NAMES
    mThumbSize    = tryInt(   prefs,     keySurvey[5],      defSurvey[5] );       // DISTOX_THUMBNAIL
    mEditableStations = prefs.getBoolean(keySurvey[6], bool(defSurvey[6]) ); // DISTOX_EDITABLE_STATIONS
    mFixedOrigin  = prefs.getBoolean(    keySurvey[7], bool(defSurvey[7]) ); // DISTOX_FIXED_ORIGIN
    mSharedXSections = prefs.getBoolean( keySurvey[8], bool(defSurvey[8]) ); // DISTOX_SHARED_XSECTIONS
    // mDataBackup   = prefs.getBoolean(    keySurvey[8], bool(defSurvey[8]) ); // DISTOX_DATA_BACKUP
    // TDLog.v("SETTING load survey done");

    String[] keyPlot = TDPrefKey.PLOT;
    String[] defPlot = TDPrefKey.PLOTdef;
    // mPickerType = tryInt( prefs,       keyPlot[0],      defPlot[0] );  // DISTOX_PICKER_TYPE choice: 0, 1, 2
    // if ( mPickerType < PICKER_LIST ) mPickerType = PICKER_LIST;
    // mTripleToolbar   = prefs.getBoolean(    keyPlot[1], bool(defPlot[1]) ); // DISTOX_TRIPLE_TOOLBAR
    // mRecentNr   = tryInt( prefs,       keyPlot[ ],      defPlot[ ] );  // DISTOX_RECENT_NR choice: 3, 4, 5, 6
    mSideDrag   = prefs.getBoolean(    keyPlot[0], bool(defPlot[0]) ); // DISTOX_SIDE_DRAG
    // setZoomControls( prefs.getBoolean( keyPlot[ ], bool(defPlot[ ]) ) ); // DISTOX_ZOOM_CONTROLS
    setZoomControls( prefs.getString(  keyPlot[1],      defPlot[1] ), TDandroid.checkMultitouch( TDInstance.context ) ); // DISTOX_ZOOM_CTRL
    // mSectionStations  = tryInt( prefs, keyPlot[ ], "3");      // DISTOX_SECTION_STATIONS
    mHThreshold    = tryFloat( prefs,  keyPlot[2],      defPlot[2] );  // DISTOX_HTHRESHOLD
    mCheckAttached = prefs.getBoolean( keyPlot[3], bool(defPlot[3]) ); // DISTOX_CHECK_ATTACHED
    mCheckExtend   = prefs.getBoolean( keyPlot[4], bool(defPlot[4]) ); // DISTOX_CHECK_EXTEND
    mItemButtonSize= tryFloat( prefs,  keyPlot[5],      defPlot[5] );  // DISTOX_TOOLBAR_SIZE
    // TDLog.v("SETTING load plot done");

    String[] keyCalib = TDPrefKey.CALIB;
    String[] defCalib = TDPrefKey.CALIBdef;
    mGroupBy       = tryInt(   prefs,      keyCalib[ 0],     defCalib[ 0] );  // DISTOX_GROUP_BY choice: 0, 1, 2
    mGroupDistance = tryFloat( prefs,      keyCalib[ 1],     defCalib[ 1] );  // DISTOX_GROUP_DISTANCE
    mCalibEps      = tryFloat( prefs,      keyCalib[ 2],     defCalib[ 2] );  // DISTOX_CAB_EPS
    mCalibMaxIt    = tryInt(   prefs,      keyCalib[ 3],     defCalib[ 3] );  // DISTOX_CALIB_MAX_IT
    mCalibShotDownload = prefs.getBoolean( keyCalib[ 4], bool(defCalib[ 4]) ); // DISTOX_CALIB_SHOT_DOWNLOAD
    // mRawData       = prefs.getBoolean( keyCalib[], bool(defCalib[]) );    // DISTOX_RAW_DATA 20
    mRawCData      = tryInt( prefs,        keyCalib[ 5],     defCalib[ 5] );  // DISTOX_RAW_CDATA 20
    // mCalibAlgo     = tryInt( prefs,        keyCalib[ 6],      defCalib[ 6] );  // DISTOX_CALIB_ALGO choice: 0, 1, 2
    mAlgoMinAlpha  = tryFloat( prefs,      keyCalib[ 6],     defCalib[ 6] );  // DISTOX_ALGO_MIN_ALPHA
    mAlgoMinBeta   = tryFloat( prefs,      keyCalib[ 7],     defCalib[ 7] );  // DISTOX_ALGO_MIN_BETA
    mAlgoMinGamma  = tryFloat( prefs,      keyCalib[ 8],     defCalib[ 8] );  // DISTOX_ALGO_MIN_GAMMA
    mAlgoMinDelta  = tryFloat( prefs,      keyCalib[ 9],     defCalib[ 9] ); // DISTOX_ALGO_MIN_DELTA
    mAutoCalBeta   = tryFloat( prefs,      keyCalib[10],     defCalib[10] ); // DISTOX_AUTO_CAL_BETA
    mAutoCalEta    = tryFloat( prefs,      keyCalib[11],     defCalib[11] ); // DISTOX_AUTO_CAL_ETA
    mAutoCalGamma  = tryFloat( prefs,      keyCalib[12],     defCalib[12] ); // DISTOX_AUTO_CAL_GAMMA
    mAutoCalDelta  = tryFloat( prefs,      keyCalib[13],     defCalib[13] ); // DISTOX_AUTO_CAL_DELTA
    // TDLog.v("SETTING load calib done");

    String[] keyDevice = TDPrefKey.DEVICE;
    String[] defDevice = TDPrefKey.DEVICEdef;
    mConnectionMode  = tryInt( prefs,    keyDevice[ 1],      defDevice[ 1] );   // DISTOX_CONN_MODE choice: 0, 1, 2
    // mAutoReconnect  = prefs.getBoolean( keyDevice[ 2], bool(defDevice[ 2]) );  // DISTOX_AUTO_RECONNECT
    mHeadTail        = prefs.getBoolean( keyDevice[ 2], bool(defDevice[ 2]) );  // DISTOX_HEAD_TAIL
    // TDLog.v("SETTINGS load device skip >" + keyDevice[3] + "< >" + defDevice[3] + "<" );
    mSockType        = tryInt( prefs,    keyDevice[ 3],      defDevice[ 3] ); // mDefaultSockStrType );  // DISTOX_SOCKET_TYPE choice: 0, 1, (2, 3)
    // TDLog.v("SETTING load device next " + keyDevice[4] + " " + defDevice[4] );
    // mZ6Workaround    = prefs.getBoolean( keyDevice[ 4], bool(defDevice[ 4])  ); // DISTOX_Z6_WORKAROUND
    mAutoPair        = prefs.getBoolean( keyDevice[ 4], bool(defDevice[ 4]) );  // DISTOX_AUTO_PAIR
    // TDLog.v("SETTING load device next " + keyDevice[6] + " " + defDevice[6] );
    mConnectFeedback = tryInt( prefs,   keyDevice[ 6],      defDevice[ 5] );   // DISTOX_CONNECT_FEEDBACK
    // TDLog.v("SETTING load device done");

    String[] keyGDev = TDPrefKey.GEEKDEVICE;
    String[] defGDev = TDPrefKey.GEEKDEVICEdef;
    mUnnamedDevice  = prefs.getBoolean( keyGDev[ 1], bool(defGDev[ 1])  ); // DISTOX_UNNAMED_DEVICE BT_NONAME
    mConnectSocketDelay = tryInt(prefs, keyGDev[ 2],      defGDev[ 2] );   // DISTOX_SOCKET_DELAY
    mSecondDistoX   = prefs.getBoolean( keyGDev[ 3], bool(defGDev[ 3]) );  // DISTOX_SECOND_DISTOX
    mWaitData       = tryInt( prefs,    keyGDev[ 4],      defGDev[ 4] );   // DISTOX_WAIT_DATA
    mWaitConn       = tryInt( prefs,    keyGDev[ 5],      defGDev[ 5] );   // DISTOX_WAIT_CONN
    mWaitLaser      = tryInt( prefs,    keyGDev[ 6],      defGDev[ 6] );   // DISTOX_WAIT_LASER
    mWaitShot       = tryInt( prefs,    keyGDev[ 7],      defGDev[ 7] );   // DISTOX_WAIT_SHOT
    mFirmwareSanity = prefs.getBoolean( keyGDev[ 8], bool(defGDev[ 8]) );  // DISTOX_FIRMWARE_SANITY
    mBricMode       = tryInt( prefs,    keyGDev[ 9],      defGDev[ 9] );   // DISTOX_BRIC_MODE
    mBricZeroLength = prefs.getBoolean( keyGDev[10], bool(defGDev[10]) );  // DISTOX_BRIC_ZERO_LENGTH
    mBricIndexIsId  = prefs.getBoolean( keyGDev[11], bool(defGDev[11]) );  // DISTOX_BRIC_INDEX_IS_ID
    mSap5Bit16Bug   = prefs.getBoolean( keyGDev[12], bool(defGDev[12]) );  // DISTOX_SAP5_BIT16_BUG
    // TDLog.v("SETTING load geek device done");

    String[] keyCave3D = TDPrefKey.CAVE3D;
    String[] defCave3D = TDPrefKey.CAVE3Ddef;
    boolean b = prefs.getBoolean( keyCave3D[0], bool(defCave3D[0]) );
    GlRenderer.mMinClino = b ? 90 : 0;
    GlModel.mStationPoints  = prefs.getBoolean( keyCave3D[1], bool(defCave3D[1]) );
    GlNames.setPointSize( tryInt(   prefs,  keyCave3D[2], defCave3D[2] ) );
    GlNames.setTextSize( tryInt(   prefs,  keyCave3D[3], defCave3D[3] ) );
    TopoGL.mSelectionRadius = tryFloat( prefs,  keyCave3D[4], defCave3D[4] );
    TopoGL.mMeasureToast    = prefs.getBoolean( keyCave3D[5], bool(defCave3D[5]) );
    TopoGL.mStationDialog   = prefs.getBoolean( keyCave3D[6], bool(defCave3D[6]) );
    GlModel.mGridAbove      = prefs.getBoolean( keyCave3D[7], bool(defCave3D[7]) );
    GlModel.mGridExtent     = tryInt(   prefs,  keyCave3D[8], defCave3D[8] );

    String[] keyDem3D = TDPrefKey.DEM3D;
    String[] defDem3D = TDPrefKey.DEM3Ddef;
    TopoGL.mDEMbuffer   = tryFloat( prefs, keyDem3D[0], defDem3D[0] );
    TopoGL.mDEMmaxsize  = tryInt(   prefs, keyDem3D[1], defDem3D[1] );
    TopoGL.mDEMreduce   = tryInt(   prefs, keyDem3D[2], defDem3D[2] );

    String[] keyWalls3D = TDPrefKey.WALLS3D;
    String[] defWalls3D = TDPrefKey.WALLS3Ddef;
    TglParser.mSplayUse = tryInt(   prefs,  keyWalls3D[0], defWalls3D[0] );
    GlModel.mAllSplay   = prefs.getBoolean( keyWalls3D[1], bool(defWalls3D[1]) );
    TopoGL.mSplayProj   = prefs.getBoolean( keyWalls3D[2], bool(defWalls3D[2]) );
    TopoGL.mSplayThr    = tryFloat( prefs,  keyWalls3D[3], defWalls3D[3] );
    GlModel.mSplitTriangles = prefs.getBoolean( keyWalls3D[4], bool(defWalls3D[4]) );
    float r = tryFloat( prefs,  keyWalls3D[5], defWalls3D[5] );
    if ( r > 0.0001f ) {
      GlModel.mSplitRandomizeDelta = r;
      GlModel.mSplitRandomize = true;
    } else {
      GlModel.mSplitRandomize = false;
    }
    r = tryFloat( prefs,  keyWalls3D[6], defWalls3D[6] );
    if ( r > 0.0001f ) {
      GlModel.mSplitStretchDelta = r;
      GlModel.mSplitStretch = true;
    } else {
      GlModel.mSplitStretch = false;
    }
    GlModel.mPowercrustDelta = tryFloat( prefs,  keyWalls3D[7], defWalls3D[7] );
    // TDLog.v("SETTING load model done");

    String[] keySketch = TDPrefKey.SKETCH;
    String[] defSketch = TDPrefKey.SKETCHdef;
    m3Dsketch    = prefs.getBoolean( keySketch[0], bool(defSketch[0]) );
    mSplayBuffer = tryFloat( prefs, keySketch[1], defSketch[1] );

    String[] keyImport = TDPrefKey.EXPORT_import;
    String[] defImport = TDPrefKey.EXPORT_importdef;
    // keyImport[ 0 ] // DISTOX_PT_CMAP
    mLRExtend          = prefs.getBoolean(     keyImport[ 1], bool(defImport[ 1]) ); // DISTOX_SPLAY_EXTEND
    // TDLog.v("SETTING load secondary export import done");

    String[] keyGeekImport = TDPrefKey.GEEKIMPORT;
    String[] defGeekImport = TDPrefKey.GEEKIMPORTdef;
    mZipWithSymbols = prefs.getBoolean( keyGeekImport[ 0], bool(defGeekImport[ 0]) ); // DISTOX_ZIP_WITH_SYMBOLS
    mImportDatamode = tryInt(   prefs,  keyGeekImport[ 1],      defGeekImport[ 1] );  // DISTOX_IMPORT_DATAMODE
    mAutoXSections  = prefs.getBoolean( keyGeekImport[ 2], bool(defGeekImport[ 2]) ); // DISTOX_AUTO_XSECTIONS
    mAutoStations   = prefs.getBoolean( keyGeekImport[ 3], bool(defGeekImport[ 3]) ); // DISTOX_AUTO_STATIONS
    mLRUDcount      = prefs.getBoolean( keyGeekImport[ 4], bool(defGeekImport[ 4]) ); // DISTOX_LRUD_COUNT
    mZipShareCategory = prefs.getBoolean( keyGeekImport[ 5], bool(defGeekImport[ 5]) ); // DISTOX_ZIP_SHARE_CATEGORY
    // mAutoExportPlotFormat = tryInt( prefs,  keyGeekImport[ 4],      defGeekImport[ 4] );  // DISTOX_AUTO_PLOT_EXPORT choice: ...
    // mExportTcsx     = prefs.getBoolean(     keyGeekImport[ 2], bool(defGeekImport[ 2]) ); // DISTOX_TRANSFER_CSURVEY
    // TDLog.v("SETTING load secondary GEEK import done");

    String[] keyEnable = TDPrefKey.EXPORT_ENABLE;
    String[] defEnable = TDPrefKey.EXPORT_ENABLEdef;
    for ( int k = 0; k < keyEnable.length; ++ k ) {
      b = prefs.getBoolean( keyEnable[ k], bool(defEnable[ k]) );
      TDConst.mSurveyExportEnable[ 1 + k ] = b;
      // TDLog.v("SETTING enable " + (1+k) + " " + b );
    }

    String[] keyExport = TDPrefKey.EXPORT;
    String[] defExport = TDPrefKey.EXPORTdef;
    mExportShotsFormat = tryInt(   prefs,      keyExport[ 0],      defExport[ 0] );  // DISTOX_EXPORT_SHOTS choice: 
    mExportPlotFormat  = tryInt(   prefs,      keyExport[ 1],      defExport[ 1] );  // DISTOX_EXPORT_PLOT choice: 14, 2, 11, 12, 13
    mAutoExportPlotFormat = tryInt(  prefs,    keyExport[ 2],      defExport[ 2] );  // DISTOX_AUTO_PLOT_EXPORT choice: ...
    mOrthogonalLRUDAngle = tryFloat( prefs,    keyExport[ 3],      defExport[ 3] );  // DISTOX_ORTHO_LRUD
    mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
    mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    mLRUDvertical      = tryFloat( prefs,      keyExport[ 4],      defExport[ 4] );  // DISTOX_LRUD_VERTICAL
    mLRUDhorizontal    = tryFloat( prefs,      keyExport[ 5],      defExport[ 5] );  // DISTOX_LRUD_HORIZONTAL
    mBezierStep        = tryFloat( prefs,      keyExport[ 6],      defExport[ 6] );  // DISTOX_BEZIER_STEP
    // TDLog.v("SETTING load secondary export done");

    String[] keyExpSvx = TDPrefKey.EXPORT_SVX;
    String[] defExpSvx = TDPrefKey.EXPORT_SVXdef;
    mSurvexEol         = ( prefs.getString(  keyExpSvx[0],      defExpSvx[0] ).equals("LF") )? "\n" : "\r\n";  // DISTOX_SURVEX_EOL
    mSurvexSplay       =   prefs.getBoolean( keyExpSvx[1], bool(defExpSvx[1]) ); // DISTOX_SURVEX_SPLAY
    mSurvexLRUD        =   prefs.getBoolean( keyExpSvx[2], bool(defExpSvx[2]) ); // DISTOX_SURVEX_LRUD
    mSurvexEPSG        = tryInt(   prefs,    keyExpSvx[3],      defExpSvx[3] );  // DISTOX_SURVEX_EPSG
    // TDLog.v("SETTING load secondary export SVX done");

    String[] keyExpTh  = TDPrefKey.EXPORT_TH;
    String[] defExpTh  = TDPrefKey.EXPORT_THdef;
    mTherionWithConfig = prefs.getBoolean( keyExpTh[0], bool(defExpTh[0]) ); // DISTOX_THERION_CONFIG
    mTherionMaps       = prefs.getBoolean( keyExpTh[1], bool(defExpTh[1]) ); // DISTOX_THERION_MAPS
    // mAutoStations      = prefs.getBoolean( keyExpTh[2], bool(defExpTh[2]) ); // DISTOX_AUTO_STATIONS 
    // mXTherionAreas  = prefs.getBoolean( keyExpTh[ ], bool(defExpTh[ ]) ); // DISTOX_XTHERION_AREAS
    mTherionSplays     = prefs.getBoolean( keyExpTh[2], bool(defExpTh[2]) ); // DISTOX_THERION_SPLAYS
    // mSurvexLRUD     =   prefs.getBoolean(   keyExpTh[3], bool(defExpTh[3]) ); // DISTOX_SURVEX_LRUD
    mTherionScale      = tryInt( prefs, keyExpTh[4], defExpTh[4] );  // DISTOX_TH2_SCALE
    mTherionXvi        = prefs.getBoolean( keyExpTh[5], bool(defExpTh[5]) ); // DISTOX_TH2_XVI
    // TDLog.v("SETTING load secondary export TH done");

    String[] keyExpDat = TDPrefKey.EXPORT_DAT;
    String[] defExpDat = TDPrefKey.EXPORT_DATdef;
    mExportStationsPrefix =  prefs.getBoolean( keyExpDat[0], bool(defExpDat[0]) ); // DISTOX_STATION_PREFIX
    mCompassSplays     = prefs.getBoolean(     keyExpDat[1], bool(defExpDat[1]) ); // DISTOX_COMPASS_SPLAYS
    mSwapLR            = prefs.getBoolean(     keyExpDat[2], bool(defExpDat[2]) ); // DISTOX_SWAP_LR

    String[] keyExpSrv = TDPrefKey.EXPORT_SRV;
    String[] defExpSrv = TDPrefKey.EXPORT_SRVdef;
    mWallsSplays       = prefs.getBoolean(     keyExpSrv[0], bool(defExpSrv[0]) ); // DISTOX_WALLS_SPLAYS
    // mWallsUD           = tryInt( prefs, keyExpSrv[1], defExpSrv[1] );  // DISTOX_WALLS_UD

    String[] keyExpTro = TDPrefKey.EXPORT_TRO;
    String[] defExpTro = TDPrefKey.EXPORT_TROdef;
    mVTopoSplays       = prefs.getBoolean(     keyExpTro[0], bool(defExpTro[0]) ); // DISTOX_VTOPO_SPLAYS
    mVTopoLrudAtFrom   = prefs.getBoolean(     keyExpTro[1], bool(defExpTro[1]) ); // DISTOX_VTOPO_LRUD
    mVTopoTrox         = prefs.getBoolean(     keyExpTro[2], bool(defExpTro[2]) ); // DISTOX_VTOPO_TROX

    String[] keyExpSvg = TDPrefKey.EXPORT_SVG;
    String[] defExpSvg = TDPrefKey.EXPORT_SVGdef;
    mSvgRoundTrip      = prefs.getBoolean(     keyExpSvg[0], bool(defExpSvg[9]) ); // DISTOX_SVG_ROUNDTRIP
    mSvgGrid           = prefs.getBoolean(     keyExpSvg[1], bool(defExpSvg[1]) ); // DISTOX_SVG_GRID
    mSvgLineDirection  = prefs.getBoolean(     keyExpSvg[2], bool(defExpSvg[2]) ); // DISTOX_SVG_LINE_DIR
    mSvgSplays         = prefs.getBoolean(     keyExpSvg[3], bool(defExpSvg[3]) ); // DISTOX_SVG_SPLAYS
    mSvgGroups         = prefs.getBoolean(     keyExpSvg[4], bool(defExpSvg[4]) ); // DISTOX_SVG_GROUPS
    // mSvgInHtml      = prefs.getBoolean(     keyExpSvg[ ], bool(defExpSvg[ ]) ); // DISTOX_SVG_IN_HTML
    mSvgPointStroke    = tryFloat( prefs,      keyExpSvg[ 5],      defExpSvg[ 5] );  // DISTOX_SVG_POINT_STROKE
    mSvgLabelStroke    = tryFloat( prefs,      keyExpSvg[ 6],      defExpSvg[ 6] );  // DISTOX_SVG_LABEL_STROKE
    mSvgLineStroke     = tryFloat( prefs,      keyExpSvg[ 7],      defExpSvg[ 7] );  // DISTOX_SVG_LINE_STROKE
    mSvgGridStroke     = tryFloat( prefs,      keyExpSvg[ 8],      defExpSvg[ 8] );  // DISTOX_SVG_GRID_STROKE
    mSvgShotStroke     = tryFloat( prefs,      keyExpSvg[ 9],      defExpSvg[ 9] );  // DISTOX_SVG_SHOT_STROKE
    mSvgLineDirStroke  = tryFloat( prefs,      keyExpSvg[10],      defExpSvg[10] ); // DISTOX_SVG_LINEDIR_STROKE
    mSvgStationSize    = tryInt(   prefs,      keyExpSvg[11],      defExpSvg[11] ); // DISTOX_SVG_STATION_SIZE
    mSvgLabelSize      = tryInt  ( prefs,      keyExpSvg[12],      defExpSvg[12] ); // DISTOX_SVG_LABEL_SIZE
    mSvgProgram        = tryInt(   prefs,      keyExpSvg[13],      defExpSvg[13] );  // DISTOX_SVG_PROGRAM
    // TDLog.v("SETTING load secondary export SVG done");

    // having mTherionScale and mSvgProgram we can set export scale
    setExportScale( mTherionScale );

    String[] keyExpKml = TDPrefKey.EXPORT_KML;
    String[] defExpKml = TDPrefKey.EXPORT_KMLdef;
    mKmlStations       = prefs.getBoolean(     keyExpKml[0], bool(defExpKml[0]) ); // DISTOX_KML_STATIONS
    mKmlSplays         = prefs.getBoolean(     keyExpKml[1], bool(defExpKml[1]) ); // DISTOX_KML_SPLAYS
    // TDLog.v("SETTING load secondary export KML done");

    // String[] keyExpCsx = TDPrefKey.EXPORT_CSX;
    // String[] defExpCsx = TDPrefKey.EXPORT_CSXdef;
    // mExportStationsPrefix = prefs.getBoolean(     keyExpCsx[0], bool(defExpCsx[0]) ); // DISTOX_STATION_PREFIX

    String[] keyExpGpx = TDPrefKey.EXPORT_GPX;
    String[] defExpGpx = TDPrefKey.EXPORT_GPXdef;
    mGPXSingleTrack    = prefs.getBoolean(     keyExpGpx[0], bool(defExpGpx[0]) ); // DISTOX_GPX_STATION_TRACK

    String[] keyExpCsv = TDPrefKey.EXPORT_CSV;
    String[] defExpCsv = TDPrefKey.EXPORT_CSVdef;
    mCsvRaw            = prefs.getBoolean(     keyExpCsv[0], bool(defExpCsv[0]) ); // DISTOX_CSV_RAW
    mCsvSeparator      = CSV_SEPARATOR[ tryInt( prefs, keyExpCsv[1], defExpCsv[1] ) ]; // DISTOX_CSV_SEP

    /* NO_PNG
    String[] keyExpPng = TDPrefKey.EXPORT_PNG;
    String[] defExpPng = TDPrefKey.EXPORT_PNGdef;
    mBitmapScale       = tryFloat( prefs,      keyExpPng[0], defExpPng[0] );  // DISTOX_BITMAP_SCALE 
    setBitmapBgcolor( prefs, keyExpPng[1], prefs.getString(keyExpPng[1], defExpPng[1]), defExpPng[1] );  // DISTOX_BITMAP_BGCOLOR
    // TDLog.v("SETTING load secondary export PNG done");
    */

    String[] keyExpDxf = TDPrefKey.EXPORT_DXF;
    String[] defExpDxf = TDPrefKey.EXPORT_DXFdef;
    // mDxfScale     = tryFloat( prefs,    keyExpDxf[ ],      defExpDxf[ ] );  // DISTOX_DXF_SCALE
    mDxfBlocks    =  prefs.getBoolean(     keyExpDxf[0], bool(defExpDxf[0]) ); // DISTOX_DXF_BLOCKS
    mAcadVersion  = tryInt(   prefs,       keyExpDxf[1],      defExpDxf[1] );  // DISTOX_ACAD_VERSION choice: 9, 13, 16
    mAcadSpline   =  prefs.getBoolean(     keyExpDxf[2], bool(defExpDxf[2]) ); // DISTOX_ACAD_SPLINE
    mDxfReference =  prefs.getBoolean(     keyExpDxf[3], bool(defExpDxf[3]) ); // DISTOX_DXF_REFERENCE
    mAcadLayer    =  prefs.getBoolean(     keyExpDxf[4], bool(defExpDxf[4]) ); // DISTOX_ACAD_LAYER
    // TDLog.v("SETTING load secondary export DXF done");
  
    String[] keyExpShp = TDPrefKey.EXPORT_SHP;
    String[] defExpShp = TDPrefKey.EXPORT_SHPdef;
    mShpGeoref   =  prefs.getBoolean(     keyExpShp[0], bool(defExpShp[0]) ); // DISTOX_SHP_GEOREF
    // TDLog.v("SETTING load secondary export SHP done");

    String[] keyData = TDPrefKey.DATA;
    String[] defData = TDPrefKey.DATAdef;
    mCloseDistance = tryFloat( prefs,          keyData[ 0],      defData[ 0] );  // DISTOX_CLOSE_DISTANCE
    mMaxShotLength = tryFloat( prefs,          keyData[ 1],      defData[ 1] );  // DISTOX_MAX_SHOT_LENGTH
    mMinLegLength  = tryFloat( prefs,          keyData[ 2],      defData[ 2] );  // DISTOX_MIN_LEG_LENGTH
    mMinNrLegShots = tryInt(   prefs,          keyData[ 3],      defData[ 3] );  // DISTOX_LEG_SHOTS choice: 2, 3, 4
    mExtendThr     = tryFloat( prefs,          keyData[ 4],      defData[ 4]  ); // DISTOX_EXTEND_THR2
    mVThreshold    = tryFloat( prefs,          keyData[ 5],      defData[ 5]  ); // DISTOX_VTHRESHOLD
    // DISTOX_AZIMUTH_MANUAL [7] handled in the first pass
    mPrevNext      = prefs.getBoolean(         keyData[ 7], bool(defData[ 7]) ); // DISTOX_PREV_NEXT
    mBacksightInput = prefs.getBoolean(        keyData[ 8], bool(defData[ 8]) ); // DISTOX_BACKSIGHT
    mTripleShot     = tryInt(  prefs,          keyData[ 9],      defData[ 9]  ); // DISTOX_LEG_FEEDBACK
    // mTimerWait     = tryInt(   prefs,          keyData[10],      defData[10] );  // DISTOX_SHOT_TIMER
    // mBeepVolume    = tryInt(   prefs,          keyData[11],      defData[11] );  // DISTOX_BEEP_VOLUME
    // TDLog.v("SETTING load secondary data done");

    String[] keyGShot = TDPrefKey.GEEKSHOT;
    String[] defGShot = TDPrefKey.GEEKSHOTdef;
    mDivingMode       = prefs.getBoolean( keyGShot[ 0], bool(defGShot[ 0]) ); // DISTOX_DIVING_MODE
    mBacksightSplay   = prefs.getBoolean( keyGShot[ 1], bool(defGShot[ 1]) ); // DISTOX_BACKSIGHT_SPLAY
    mShotRecent       = prefs.getBoolean( keyGShot[ 2], bool(defGShot[ 2]) ); // DISTOX_RECENT_SHOT
    mRecentTimeout    = tryInt(   prefs,  keyGShot[ 3],      defGShot[ 3] );  // DISTOX_RECENT_TIMEOUT
    mExtendFrac       = prefs.getBoolean( keyGShot[ 4], bool(defGShot[ 4]) ); // DISTOX_EXTEND_FRAC
    mDistoXBackshot   = prefs.getBoolean( keyGShot[ 5], bool(defGShot[ 5]) ); // DISTOX_BACKSHOT
    mBedding          = prefs.getBoolean( keyGShot[ 6], bool(defGShot[ 6]) ); // DISTOX_BEDDING
    mWithSensors      = prefs.getBoolean( keyGShot[ 7], bool(defGShot[ 7]) ); // DISTOX_WITH_SENSORS
    setLoopClosure( tryInt(   prefs,      keyGShot[ 8],      defGShot[ 8] ) );// DISTOX_LOOP_CLOSURE_VALUE
    mLoopThr          = tryFloat( prefs,  keyGShot[ 9],      defGShot[ 9] );  // DISTOX_LOOP_THR
    mWithAzimuth      = prefs.getBoolean( keyGShot[10], bool(defGShot[10]) ); // DISTOX_ANDROID_AZIMUTH
    mTimerWait        = tryInt(   prefs,  keyGShot[11],      defGShot[11] );  // DISTOX_SHOT_TIMER
    mBeepVolume       = tryInt(   prefs,  keyGShot[12],      defGShot[12] );  // DISTOX_BEEP_VOLUME
    mBlunderShot      = prefs.getBoolean( keyGShot[13], bool(defGShot[13]) ); // DISTOX_BLUNDER_SHOT
    mSplayStation     = prefs.getBoolean( keyGShot[14], bool(defGShot[14]) ); // DISTOX_SPLAY_STATION
    mSplayOnlyForward = prefs.getBoolean( keyGShot[15], bool(defGShot[15]) ); // DISTOX_SPLAY_GROUP
    // mWithTdManager = prefs.getBoolean( keyGShot[13], bool(defGShot[13]) ); // DISTOX_TDMANAGER
    // TDLog.v("SETTING load secondary GEEK data done");

    String[] keyGPlot = TDPrefKey.GEEKPLOT;
    String[] defGPlot = TDPrefKey.GEEKPLOTdef;
    mPlotShift     = prefs.getBoolean( keyGPlot[ 0], bool(defGPlot[ 0]) ); // DISTOX_PLOT_SHIFT
    mPlotSplit     = prefs.getBoolean( keyGPlot[ 1], bool(defGPlot[ 1]) ); // DISTOX_PLOT_SPLIT
    setStylusSize(  tryFloat( prefs,   keyGPlot[ 2],      defGPlot[ 2] ) );  // DISTOX_STYLUS_SIZE // STYLUS_MM
    mBackupNumber   = tryInt( prefs,   keyGPlot[ 3],      defGPlot[ 3] );  // DISTOX_BACKUP_NUMBER
    mBackupInterval = tryInt( prefs,   keyGPlot[ 4],      defGPlot[ 4] );  // DISTOX_BACKUP_INTERVAL
    // mAutoXSections  = prefs.getBoolean( keyGPlot[ 5], bool(defGPlot[ 5]) ); // DISTOX_AUTO_XSECTIONS
    mSavedStations  = prefs.getBoolean( keyGPlot[ 5], bool(defGPlot[ 5]) ); // DISTOX_SAVED_STATIONS
    mLegOnlyUpdate  = prefs.getBoolean( keyGPlot[ 6], bool(defGPlot[ 6]) ); // DISTOX_LEGONLY_UPDATE
    mFullAffine     = prefs.getBoolean( keyGPlot[ 7], bool(defGPlot[ 7]) ); // DISTOX_FULL_UPDATE
    mWithLevels     = tryInt( prefs,   keyGPlot[ 8],      defGPlot[ 8] );   // DISTOX_WITH_LEVELS
    mGraphPaperScale = tryInt( prefs,  keyGPlot[ 9],      defGPlot[ 9] );  // DISTOX_GRAPH_PAPER_SCALE
    mSlantXSection  = prefs.getBoolean( keyGPlot[10], bool(defGPlot[10]) ); // DISTOX_SLANT_XSECTION
    mObliqueMax     = tryInt( prefs,   keyGPlot[11],      defGPlot[11] );  // DISTOX_OBLIQUE_PROJECTED
    mLineEnds       = tryInt( prefs,   keyGPlot[12],      defGPlot[12] );  // DISTOX_LINE_ENDS
    // TDLog.v("SETTING load secondary GEEK plot done");

    String[] keyGPlotSplay = TDPrefKey.GEEKsplay;
    String[] defGPlotSplay = TDPrefKey.GEEKsplaydef;
    mSplayClasses  = prefs.getBoolean( keyGPlotSplay[ 0], bool(defGPlotSplay[ 0]) ); // DISTOX_SPLAY_CLASSES
    // mSplayColor    = prefs.getBoolean( keyGPlotSplay[ 1], bool(defGPlotSplay[ 1]) ); // DISTOX_SPLAY_COLOR
    mDiscreteColors = tryInt( prefs,   keyGPlotSplay[ 1],      defGPlotSplay[ 1] );  // DISTOX_DISCRETE_COLORS
    mSplayColor = (mDiscreteColors > 0);
    // mSplayAsDot    = prefs.getBoolean( keyGPlotSplay[ 2], bool(defGPlotSplay[ 2]) ); // DISTOX_SPLAY_AS_DOT
    mSplayVertThrs  = tryFloat( prefs, keyGPlotSplay[ 2],      defGPlotSplay[ 2]  ); // DISTOX_SPLAY_VERT_THRS
    mDashSplay      = tryInt( prefs,   keyGPlotSplay[ 3],      defGPlotSplay[ 3] );  // DISTOX_SPLAY_DASH
    mVertSplay      = tryFloat( prefs, keyGPlotSplay[ 4],      defGPlotSplay[ 4] );  // DISTOX_VERT_SPLAY
    mHorizSplay     = tryFloat( prefs, keyGPlotSplay[ 5],      defGPlotSplay[ 5] );  // DISTOX_HORIZ_SPLAY
    mCosHorizSplay = TDMath.cosd( mHorizSplay );  
    mSectionSplay   = tryFloat( prefs, keyGPlotSplay[ 6],      defGPlotSplay[ 6] );  // DISTOX_SECTION_SPLAY
    mCosSectionSplay  = TDMath.cosd( mSectionSplay );
    mSplayDashColor = tryColor( prefs, keyGPlotSplay[ 7],      defGPlotSplay[ 7] );  // DISTOX_SPLAY_DASH_COLOR
    BrushManager.setSplayDashColor( mSplayDashColor );
    mSplayDotColor  = tryColor( prefs, keyGPlotSplay[ 8],      defGPlotSplay[ 8] );  // DISTOX_SPLAY_DOT_COLOR
    BrushManager.setSplayDotColor( mSplayDotColor );
    mSplayLatestColor  = tryColor( prefs, keyGPlotSplay[ 9],   defGPlotSplay[ 9] );  // DISTOX_SPLAY_LATEST_COLOR
    BrushManager.setSplayLatestColor( mSplayLatestColor );
    // TDLog.v("SETTING load secondary GEEK plot done");

    String[] keyGLine = TDPrefKey.GEEKLINE;
    String[] defGLine = TDPrefKey.GEEKLINEdef;
    setReduceAngle( tryFloat(  prefs,  keyGLine[ 0],      defGLine[ 0] ) ); // DISTOX_REDUCE_ANGLE
    mLineAccuracy  = tryFloat( prefs,  keyGLine[ 1],      defGLine[ 1] );   // DISTOX_LINE_ACCURACY
    mLineCorner    = tryFloat( prefs,  keyGLine[ 2],      defGLine[ 2] );   // DISTOX_LINE_CORNER
    mWeedDistance  = tryFloat( prefs,  keyGLine[ 3],      defGLine[ 3] );   // DISTOX_WEED_DISTANCE
    mWeedLength    = tryFloat( prefs,  keyGLine[ 4],      defGLine[ 4] );   // DISTOX_WEED_LENGTH
    mWeedBuffer    = tryFloat( prefs,  keyGLine[ 5],      defGLine[ 5] );   // DISTOX_WEED_BUFFER
    mLineSnap      = prefs.getBoolean( keyGLine[ 6], bool(defGLine[ 6]) );  // DISTOX_LINE_SNAP
    mLineCurve     = prefs.getBoolean( keyGLine[ 7], bool(defGLine[ 7]) );  // DISTOX_LINE_CURVE
    mLineStraight  = prefs.getBoolean( keyGLine[ 8], bool(defGLine[ 8]) );  // DISTOX_LINE_STRAIGHT
    mPathMultiselect = prefs.getBoolean( keyGLine[ 9], bool(defGLine[ 9]) );  // DISTOX_PATH_MULTISELECT
    // mCompositeActions = prefs.getBoolean( keyGLine[10], bool(defGLine[10]) );  // DISTOX_COMPOSITE_ACTIONS
    // TDLog.v("SETTING load secondary GEEK line done");

    String[] keyUnits = TDPrefKey.UNITS;
    String[] defUnits = TDPrefKey.UNITSdef;
    if ( prefs.getString( keyUnits[0], defUnits[0] ).equals( defUnits[0] ) ) {
      mUnitLength = 1.0f;
      mUnitLengthStr = "m";
    } else {
      mUnitLength = TDUtil.M2FT;
      mUnitLengthStr = "ft";
    }
    if ( prefs.getString( keyUnits[1],  defUnits[1] ).equals( defUnits[1] ) ) {
      mUnitAngle = 1.0f;
      mUnitAngleStr = "deg";
    } else {
      mUnitAngle = TDUtil.DEG2GRAD;
      mUnitAngleStr = "grad";
    }
    mUnitGrid       = tryFloat(  prefs, keyUnits[2], defUnits[2] );      // DISTOX_UNIT_GRID
    mUnitMeasure    = tryFloat(  prefs, keyUnits[3], defUnits[3] );      // DISTOX_UNIT_MEASURE
    // TDLog.v("SETTING units grid " + mUnitGrid );
  
    String[] keyAcc = TDPrefKey.ACCURACY;
    String[] defAcc = TDPrefKey.ACCURACYdef;
    mAccelerationThr = tryFloat( prefs, keyAcc[0], defAcc[0] ); // DISTOX_ACCEL_PERCENT
    mMagneticThr     = tryFloat( prefs, keyAcc[1], defAcc[1] ); // DISTOX_MAG_PERCENT
    mDipThr          = tryFloat( prefs, keyAcc[2], defAcc[2] ); // DISTOX_DIP_THR
    setSiblingThr( tryFloat( prefs, keyAcc[3], defAcc[3] ) ); // DISTOX_SIBLING_PERCENT

    String[] keyLoc = TDPrefKey.LOCATION;
    String[] defLoc = TDPrefKey.LOCATIONdef;
    mUnitLocation  = (prefs.getString( keyLoc[0], defLoc[0] ).equals(defLoc[0])) ? TDUtil.DDMMSS  // DISTOX_UNIT_LOCATION
                                                                                 : TDUtil.DEGREE;
    mCRS           = prefs.getString(  keyLoc[1], defLoc[1] );       // DISTOX_CRS
    mNegAltitude   = prefs.getBoolean( keyLoc[2], bool(defLoc[2]) ); // DISTOX_NEG_ALTITUDE
    mEditableHGeo  = prefs.getBoolean( keyLoc[3], bool(defLoc[3]) ); // DISTOX_EDIT_ALTITUDE
    mFineLocation  = tryInt( prefs,    keyLoc[4], defLoc[4] );       // DISTOX_FINE_LOCATION
    // mGeoImportApp  = tryInt( prefs,    keyLoc[4], defLoc[4] );       // DISTOX_GEOPOINT_APP
    // TDLog.v("PREFS key <" + keyLoc[4] + "> val <" + defLoc[4] + ">" );
    try {
      mGeoImportApp = Integer.parseInt( prefs.getString( keyLoc[5], defLoc[5] ) );
    } catch ( RuntimeException e ) {
      TDLog.v("ERROR " + e.getMessage() );
    } catch ( Exception ee ) {
      TDLog.v("EXCEPT " + ee.getMessage() );
    }

    String[] keyScreen = TDPrefKey.SCREEN;
    String[] defScreen = TDPrefKey.SCREENdef;
    mFixedThickness = tryFloat( prefs, keyScreen[ 0], defScreen[ 0] );  // DISTOX_FIXED_THICKNESS
    mStationSize    = tryFloat( prefs, keyScreen[ 1], defScreen[ 1] );  // DISTOX_STATION_SIZE
    mDotRadius      = tryFloat( prefs, keyScreen[ 2], defScreen[ 2] );  // DISTOX_DOT_RADIUS
    mSelectness     = tryFloat( prefs, keyScreen[ 3], defScreen[ 3] );  // DISTOX_CLOSENESS
    mEraseness      = tryFloat( prefs, keyScreen[ 4], defScreen[ 4] );  // DISTOX_ERASENESS
    mMinShift       = tryInt(   prefs, keyScreen[ 5], defScreen[ 5] );  // DISTOX_MIN_SHIFT
    mPointingRadius = tryInt(   prefs, keyScreen[ 6], defScreen[ 6] );  // DISTOX_POINTING
    mSplayAlpha     = tryInt(   prefs, keyScreen[ 7], defScreen[ 7] );  // DISTOX_SPLAY_ALPHA
    BrushManager.setSplayAlpha( mSplayAlpha );

    String[] keyLine = TDPrefKey.LINE;
    String[] defLine = TDPrefKey.LINEdef;
    mLineThickness = tryFloat( prefs,  keyLine[0],      defLine[0] );   // DISTOX_LINE_THICKNESS
    mUnitLines     = tryFloat( prefs,  keyLine[1],      defLine[1] );   // DISTOX_LINE_UNITS
    setLineStyleAndType( prefs.getString( keyLine[2],   defLine[2] ) ); // DISTOX_LINE_STYLE
    setLineSegment( tryInt(    prefs,  keyLine[3],      defLine[3] ) ); // DISTOX_LINE_SEGMENT
    mLineClose     = prefs.getBoolean( keyLine[4], bool(defLine[4]) );  // DISTOX_LINE_CLOSE
    mSlopeLSide    = tryInt(   prefs,  keyLine[5],      defLine[5] );   // DISTOX_SLOPE_LSIDE
    mArrowLength   = tryFloat( prefs,  keyLine[6],      defLine[6] );   // DISTOX_ARROW_LENGTH
    mAutoSectionPt = prefs.getBoolean( keyLine[7], bool(defLine[7]) );  // DISTOX_AUTO_SECTION_PT
    // mContinueLine  = tryInt(   prefs,  keyLine[7],      defLine[7] );   // DISTOX_LINE_CONTINUE
    mWithLineJoin  = prefs.getBoolean( keyLine[8], bool(defLine[8]) );  // DISTOX_WITH_CONTINUE_LINE
    mAreaBorder    = prefs.getBoolean( keyLine[9], bool(defLine[9]) );  // DISTOX_AREA_BORDER

    String[] keyPoint = TDPrefKey.POINT;
    String[] defPoint = TDPrefKey.POINTdef;
    mUnscaledPoints = prefs.getBoolean( keyPoint[0], bool(defPoint[0]) ); // DISTOX_UNSCALED_POINTS
    mUnitIcons     = tryFloat( prefs,   keyPoint[1], defPoint[1] );       // DISTOX_DRAWING_UNIT 
    mLabelSize     = tryFloat( prefs,   keyPoint[2], defPoint[2] );       // DISTOX_LABEL_SIZE
    mScalableLabel = prefs.getBoolean(  keyPoint[3], bool(defPoint[3]) ); // DISTOX_SCALABLE_LABEL
    // mPlotCache  = prefs.getBoolean( keyPoint[], bool(defPoint[]) );    // DISTOX_PLOT_CACHE

    // AUTOWALLS
    // String[] keyWalls = TDPrefKey.WALLS;
    // String[] defWalls = TDPrefKey.WALLSdef;
    // mWallsType        = tryInt(   prefs, keyWalls[0], defWalls[0] ); // DISTOX_WALLS_TYPE choice: 0, 1
    // mWallsPlanThr     = tryFloat( prefs, keyWalls[1], defWalls[1] ); // DISTOX_WALLS_PLAN_THR
    // mWallsExtendedThr = tryFloat( prefs, keyWalls[2], defWalls[2] ); // DISTOX_WALLS_EXTENDED_THR
    // mWallsXClose      = tryFloat( prefs, keyWalls[3], defWalls[3] ); // DISTOX_WALLS_XCLOSE
    // mWallsXStep       = tryFloat( prefs, keyWalls[4], defWalls[4] ); // DISTOX_WALLS_XSTEP
    // mWallsConcave     = tryFloat( prefs, keyWalls[5], defWalls[5] ); // DISTOX_WALLS_CONCAVE

  }

  // ----------------------------------------------------------------------------------
  // return true if the interface must update the value

  /** update a preference
   * @param hlp   preference helper
   * @param cat   preference category
   * @param k     preference key
   * @param v     preference value
   */
  public static String updatePreference( TDPrefHelper hlp, int cat, String k, String v )
  {
    TDLog.v("SETTINGS update pref " + k + " val " + v );
    switch ( cat ) {
      case TDPrefCat.PREF_CATEGORY_ALL:    return updatePrefMain( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_SURVEY: return updatePrefSurvey( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_PLOT:   return updatePrefPlot( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_CALIB:  return updatePrefCalib( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_DEVICE: return updatePrefDevice( hlp, k, v );
      // case TDPrefCat.PREF_CATEGORY_SKETCH: return updatePrefSketch( hlp, k, v ); // SKETCH_3D --> moved to GEEK SKETCH
      case TDPrefCat.PREF_CATEGORY_EXPORT: return updatePrefExport( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_IMPORT: return updatePrefImport( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_EXPORT_ENABLE: return updatePrefExportEnable( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_SVX:    return updatePrefSvx( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_TH:     return updatePrefTh( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_DAT:    return updatePrefDat( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_CSX:    return updatePrefCsx( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_SVG:    return updatePrefSvg( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_DXF:    return updatePrefDxf( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_SHP:    return updatePrefShp( hlp, k, v );
      // case TDPrefCat.PREF_CATEGORY_PNG:    return updatePrefPng( hlp, k, v ); // NO_PNG
      case TDPrefCat.PREF_CATEGORY_GPX:    return updatePrefGpx( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_KML:    return updatePrefKml( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_CSV:    return updatePrefCsv( hlp, k, v );
      case TDPrefCat.PREF_SHOT_DATA:       return updatePrefData( hlp, k, v );
      case TDPrefCat.PREF_SHOT_UNITS:      return updatePrefUnits( hlp, k, v );
      case TDPrefCat.PREF_ACCURACY:        return updatePrefAccuracy( hlp, k, v );
      case TDPrefCat.PREF_LOCATION:        return updatePrefLocation( hlp, k, v );
      case TDPrefCat.PREF_PLOT_SCREEN:     return updatePrefScreen( hlp, k, v );
      case TDPrefCat.PREF_TOOL_LINE:       return updatePrefLine( hlp, k, v );
      case TDPrefCat.PREF_TOOL_POINT:      return updatePrefPoint( hlp, k, v );
      // case TDPrefCat.PREF_PLOT_WALLS:      return updatePrefWalls( hlp, k, v ); // AUTOWALLS
      case TDPrefCat.PREF_PLOT_DRAW:       return updatePrefDraw( hlp, k, v );
      case TDPrefCat.PREF_PLOT_ERASE:      return updatePrefErase( hlp, k, v );
      case TDPrefCat.PREF_PLOT_EDIT:       return updatePrefEdit( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_CAVE3D: return updatePrefCave3D( hlp, k, v );
      case TDPrefCat.PREF_DEM3D:           return updatePrefDem3D( hlp, k, v );
      case TDPrefCat.PREF_WALLS3D:         return updatePrefWalls3D( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_GEEK:   return updatePrefGeek( hlp, k, v);
      case TDPrefCat.PREF_GEEK_SHOT:       return updatePrefGeekShot( hlp, k, v );
      case TDPrefCat.PREF_GEEK_SPLAY:      return updatePrefGeekSplay( hlp, k, v );
      case TDPrefCat.PREF_GEEK_PLOT:       return updatePrefGeekPlot( hlp, k, v );
      case TDPrefCat.PREF_GEEK_LINE:       return updatePrefGeekLine( hlp, k, v );
      case TDPrefCat.PREF_GEEK_IMPORT:     return updatePrefGeekImport( hlp, k, v );
      case TDPrefCat.PREF_GEEK_DEVICE:     return updatePrefGeekDevice( hlp, k, v );
      case TDPrefCat.PREF_GEEK_SKETCH:     return updatePrefSketch( hlp, k, v );
      // case TDPrefCat.PREF_CATEGORY_LOG:    return updatePrefLog( hlp, k, v ); // NO_LOGS
      default:
        TDLog.e("SETTINGS unhandled: cat " + cat + " key " + k + " val <" + v + ">" );
    }
    return null;
  }


  private static String updatePrefMain( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    String[] key = TDPrefKey.MAIN;
    String[] def = TDPrefKey.MAINdef;
    // TDLog.v("update pref main: " + k );
    // if ( k.equals( key[0] ) ) {// DISTOX_CWD
    //   // handled independently
    //   // TopoDroidApp.setCWD( tryStringValue( hlp, k, v, "TopoDroid" ), hlp.getString( "DISTOX_CBD", TDPath.getBaseDir() ) );
    //   TopoDroidApp.setCWD( tryStringValue( hlp, k, v, "TopoDroid" ) /* , TDPath.getCurrentBaseDir() */ );
    // } else 
    if ( k.equals( key[ 0 ] ) ) {              // DISTOX_TEXT_SIZE
      ret = setTextSize( tryIntValue( hlp, k, v, defaultTextSize ) );
    } else if ( k.equals( key[ 1 ] ) ) {              // DISTOX_SIZE_BUTTONS (choice)
      if ( setSizeButtons( tryIntValue( hlp, k, v, defaultButtonSize ) ) ) {
        TopoDroidApp.resetButtonBar();
      }
    } else if ( k.equals( key[ 2 ] ) ) {             // DISTOX_EXTRA_BUTTONS (choice)
      int level = tryIntValue( hlp, k, v, def[2] );
      setActivityBooleans( hlp.getSharedPrefs(), level );
    } else if ( k.equals( key[ 3 ] ) ) {           // DISTOX_LOCAL_MAN (choice)
      // TDLog.v("SETTING handle local man pages - key " + k + " default " + def[6] );
      mLocalManPages = handleLocalUserMan( /* hlp.getApp(), */ tryStringValue( hlp, k, v, def[3] ), true );
    } else if ( k.equals( key[ 4 ] ) ) {           // DISTOX_LOCALE (choice)
      setLocale( tryStringValue( hlp, k, v, def[4] ), true );
    } else if ( k.equals( key[ 5 ] ) ) {           // DISTOX_ORIENTATION (choice)
      mOrientation = tryIntValue( hlp, k, v, def[5] );
      TopoDroidApp.setScreenOrientation( );
      TDandroid.setScreenOrientation( TDPrefActivity.mPrefActivityAll );
    /* ---- IF_COSURVEY
    } else if ( k.equals( key[ 8 ] ) ) {           // DISTOX_COSURVEY (bool)
      boolean co_survey = tryBooleanValue( hlp, k, v, false );
      if ( co_survey != TopoDroidApp.mCoSurveyServer ) {
        hlp.getApp().setCoSurvey( co_survey ); // set flag and start/stop server
      }
    */
    } else {
      TDLog.e("missing MAIN key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  // return the new string value if the value has been corrected
  //        otherwise returns null
  private static String updatePrefSurvey( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("SETTINGS update pref survey: " + k );
    String[] key = TDPrefKey.SURVEY;
    String[] def = TDPrefKey.SURVEYdef;
    String ret = null;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_TEAM (arbitrary)
      mDefaultTeam = tryStringValue( hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_TEAM_DIALOG (bool)
      mTeamNames = tryIntValue( hlp, k, v, def[1] ); 
      // TDLog.v("SETTINGS TEAM Names " + mTeamNames );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SURVEY_STATION (choice)
      parseStationPolicy( hlp, tryStringValue( hlp, k, v, def[2] ) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_STATION_NAMES (choice)
      mStationNames = (tryStringValue( hlp, k, v, def[3]).equals("number"))? 1 : 0;
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_INIT_STATION 
      mInitStation = tryStringValue( hlp, k, v, def[4] ).replaceAll("\\s+", "");
      if ( mInitStation.length() == 0 ) mInitStation = def[4];
      DistoXStationName.setInitialStation( mInitStation );
      if ( ! mInitStation.equals( v ) ) { ret = mInitStation; }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_THUMBNAIL
      mThumbSize = tryIntValue( hlp, k, v, def[5] ); 
      if ( mThumbSize < 80 )       { mThumbSize = 80;  ret = Integer.toString( mThumbSize ); }
      else if ( mThumbSize > 400 ) { mThumbSize = 400; ret = Integer.toString( mThumbSize ); }
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_EDITABLE_STATIONS (bool)
      mEditableStations = tryBooleanValue( hlp, k, v, bool(def[6]) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_FIXED_ORIGIN (bool)
      mFixedOrigin = tryBooleanValue( hlp, k, v, bool(def[7]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_SHARED_XSECTIONS (bool)
      mSharedXSections  = tryBooleanValue( hlp, k, v, bool(def[8]) );
    // } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_DATA_BACKUP (bool)
    //   mDataBackup = tryBooleanValue( hlp, k, v, bool(def[8]) );
    } else {
      TDLog.e("missing SURVEY key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefPlot( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref plot: " + k );
    String[] key = TDPrefKey.PLOT;
    String[] def = TDPrefKey.PLOTdef;
    // if ( k.equals( key[ 0 ] ) ) {        // DISTOX_PICKER_TYPE (choice)
    //   mPickerType = tryIntValue(   hlp, k, v, def[0] );
    //   if ( mPickerType < PICKER_LIST ) mPickerType = PICKER_LIST;
    // } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_RECENT_NR (choice)
    //   mRecentNr = tryIntValue( hlp, k, v, def[1] );
    // } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_TRIPLE_TOOLBAR (bool)
    //   mTripleToolbar = tryBooleanValue( hlp, k, v, bool(def[1]) );
    //   TopoDroidApp.setToolsToolbars();
    // } else 
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SIDE_DRAG (bool)
      mSideDrag = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_ZOOM_CTRL (choice)
      // setZoomControls( tryBooleanValue( hlp, k, bool(def[1]) ) );
      setZoomControls( tryStringValue( hlp, k, v, def[1] ), TDandroid.checkMultitouch( TDInstance.context ) );
    // } else if ( k.equals( key[ ? ] ) ) {  // DISTOX_SECTION_STATIONS
    //   mSectionStations = tryIntValue( hlp, k, v, def[ ] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_HTHRESHOLD
      mHThreshold = tryFloatValue( hlp, k, v, def[2] );
      if ( mHThreshold <  0 ) { mHThreshold =  0; ret = TDString.ZERO; }
      if ( mHThreshold > 90 ) { mHThreshold = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_CHECK_ATTACHED (bool)
      mCheckAttached = tryBooleanValue( hlp, k, v, bool(def[3]) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_CHECK_EXTEND (bool)
      mCheckExtend   = tryBooleanValue( hlp, k, v, bool(def[4]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_TOOLBAR_SIZE
      mItemButtonSize = tryFloatValue( hlp, k, v, def[5] );
      TopoDroidApp.setToolsToolbarParams();
    } else {
      TDLog.e("missing PLOT key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefCalib( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref calib: " + k );
    String[] key = TDPrefKey.CALIB;
    String[] def = TDPrefKey.CALIBdef;
    if ( k.equals( key[ 0 ] ) ) {
      mGroupBy       = tryIntValue(   hlp, k, v, def[0] );  // DISTOX_GROUP_BY (choice)
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_GROUP_DISTANCE
      mGroupDistance = tryFloatValue( hlp, k, v, def[1] );
      if ( mGroupDistance < 0 ) { mGroupDistance = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_CALIB_EPS
      mCalibEps      = tryFloatValue( hlp, k, v, def[2] );
      if ( mCalibEps < 0.000001f ) { mCalibEps = 0.000001f; ret = "0.000001"; }
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_CALIB_MAX_IT
      mCalibMaxIt    = tryIntValue(   hlp, k, v, def[3] );
      if ( mCalibMaxIt < 10 ) { mCalibMaxIt = 10; ret = TDString.TEN; }
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_CALIB_SHOT_DOWNLOAD (bool)
      mCalibShotDownload = tryBooleanValue( hlp, k, v, bool(def[4]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_RAW_CDATA
      // mRawData       = tryBooleanValue( hlp, k, v, false );  // DISTOX_RAW_DATA (choice)
      mRawCData      = tryIntValue( hlp, k, v, def[5] ); 
    // } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_CALIB_ALGO (choice)
    //   mCalibAlgo     = tryIntValue( hlp, k, v, def[6] );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_ALGO_MIN_ALPHA
      mAlgoMinAlpha   = tryFloatValue( hlp, k, v, def[7] );
      if ( mAlgoMinAlpha < 0 ) { mAlgoMinAlpha = 0; ret = TDString.ZERO; }
      if ( mAlgoMinAlpha > 1 ) { mAlgoMinAlpha = 1; ret = TDString.ONE; }
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_ALGO_MIN_BETA
      mAlgoMinBeta    = tryFloatValue( hlp, k, v, def[8] );
      if ( mAlgoMinBeta  < 0 ) { mAlgoMinBeta  = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_ALGO_MIN_GAMMA
      mAlgoMinGamma   = tryFloatValue( hlp, k, v, def[9] );
      if ( mAlgoMinGamma < 0 ) { mAlgoMinGamma = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 10 ] ) ) { // DISTOX_ALGO_MIN_DELTA
      mAlgoMinDelta   = tryFloatValue( hlp, k, v, def[10] ); 
      if ( mAlgoMinDelta < -10 ) { mAlgoMinDelta = -10; ret = "-10"; }
    } else if ( k.equals( key[ 11 ] ) ) { // DISTOX_AUTO_CAL_BETA
      mAutoCalBeta   = tryFloatValue( hlp, k, v, def[11] ); 
      if ( mAutoCalBeta < 0.001f ) { mAutoCalBeta = 0.001f; ret = "0.001"; }
      if ( mAutoCalBeta > 0.999f ) { mAutoCalBeta = 0.999f; ret = "0.999"; }
    } else if ( k.equals( key[ 12 ] ) ) { // DISTOX_AUTO_CAL_ETA
      mAutoCalEta   = tryFloatValue( hlp, k, v, def[12] ); 
      if ( mAutoCalEta < 0.01f ) { mAutoCalEta = 0.01f; ret = "0.01"; }
      if ( mAutoCalEta > 0.99f ) { mAutoCalEta = 0.99f; ret = "0.99"; }
    } else if ( k.equals( key[ 13 ] ) ) { // DISTOX_AUTO_CAL_GAMMA
      mAutoCalGamma   = tryFloatValue( hlp, k, v, def[13] ); 
      if ( mAutoCalGamma < 0.01f ) { mAutoCalGamma = 0.01f; ret = "0.01"; }
      if ( mAutoCalGamma > 0.99f ) { mAutoCalGamma = 0.99f; ret = "0.99"; }
    } else if ( k.equals( key[ 14 ] ) ) { // DISTOX_AUTO_CAL_DELTA
      mAutoCalDelta   = tryFloatValue( hlp, k, v, def[14] ); 
      if ( mAutoCalDelta < 0.01f ) { mAutoCalDelta = 0.01f; ret = "0.01"; }
      if ( mAutoCalDelta > 0.99f ) { mAutoCalDelta = 0.99f; ret = "0.99"; }
    } else {
      TDLog.e("missing CALIB key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefDevice( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    String[] key = TDPrefKey.DEVICE;
    String[] def = TDPrefKey.DEVICEdef;
    // DISTOX_DEVICE, unused here
    // DISTOX_DEVICE_TYPE, unused here
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_BLUETOOTH (choice)
      mCheckBT  = tryIntValue( hlp, k, v, def[0] ); 
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_CONN_MODE (choice)
      mConnectionMode = tryIntValue( hlp, k, v, def[1] ); 
    // } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_AUTO_RECONNECT (bool)
    //   mAutoReconnect = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_HEAD_TAIL (bool)
      mHeadTail = tryBooleanValue( hlp, k, v, bool(def[2]) ); 
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_SOCKET_TYPE (choice)
      mSockType = tryIntValue( hlp, k, v, def[3] ); // mDefaultSockStrType ); 
    // } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_Z6_WORKAROUND (bool)
    //   mZ6Workaround = tryBooleanValue( hlp, k, v, bool(def[4]) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_AUTO_PAIR (bool)
      mAutoPair = tryBooleanValue( hlp, k, v, bool(def[4]) );
      TopoDroidApp.checkAutoPairing();
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_CONNECT_FEEDBACK
      mConnectFeedback = tryIntValue( hlp, k, v, def[6] );
    } else {
      TDLog.e("missing DEVICE key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  // ------------------------------------------------------------------------------------------
  private static String updatePrefCave3D( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref cave3d: " + k );
    String[] key = TDPrefKey.CAVE3D;
    String[] def = TDPrefKey.CAVE3Ddef;
    if ( k.equals( key[0] ) ) {  // CAVE3D_NEG_CLINO
      boolean b = tryBooleanValue( hlp, k, v, bool(def[0]) ); 
      GlRenderer.mMinClino = b ? 90 : 0;
    } else if ( k.equals( key[1] ) ) { // CAVE3D_STATION_POINTS
      GlModel.mStationPoints = tryBooleanValue( hlp, k, v, bool(def[1]) );
    } else if ( k.equals( key[2] ) ) { // CAVE3D_STATION_POINT_SIZE def=8
      GlNames.setPointSize( tryIntValue( hlp, k, v, def[2] ) );
    } else if ( k.equals( key[3] ) ) { // CAVE3D_STATION_TEXT_SIZE def=20
      GlNames.setTextSize( tryIntValue( hlp, k, v, def[3] ) );
    } else if ( k.equals( key[4] ) ) { // CAVE3D_SELECTION_RADIUS
      float radius = tryFloatValue( hlp, k, v, def[4] );
      if ( radius > 10.0f ) TopoGL.mSelectionRadius = radius;
    } else if ( k.equals( key[5] ) ) { // CAVE3D_MEASURE_DIALOG
      TopoGL.mMeasureToast = tryBooleanValue( hlp, k, v, bool(def[5]) ); 
    } else if ( k.equals( key[6] ) ) { // CAVE3D_STATION_TOAST
      TopoGL.mStationDialog = tryBooleanValue( hlp, k, v, bool(def[6]) ); 
    } else if ( k.equals( key[7] ) ) { // CAVE3D_GRID_ABOVE
      GlModel.mGridAbove = tryBooleanValue( hlp, k, v, bool(def[7]) ); 
    } else if ( k.equals( key[8] ) ) { // CAVE3D_GRID_EXTENT
      int extent = tryIntValue( hlp, k, v, def[8] ); 
      if ( extent > 1 && extent < 100 ) GlModel.mGridExtent = extent;
    } else {
      TDLog.e("missing Cave3D key: " + k );
    }
    return ret;
  }

  private static String updatePrefDem3D( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref dem3d: " + k );
    String[] key = TDPrefKey.DEM3D;
    String[] def = TDPrefKey.DEM3Ddef;

    if ( k.equals( key[0] ) ) { 
      float buffer = tryFloatValue( hlp, k, v, def[0] );
      if ( buffer >= 0 )  TopoGL.mDEMbuffer = buffer;
    } else if ( k.equals( key[1] ) ) { 
      int size = tryIntValue( hlp, k, v, def[1] ); 
      if ( size >= 50 )  TopoGL.mDEMmaxsize = size;
    } else if ( k.equals( key[2] ) ) { 
      int reduce = tryIntValue( hlp, k, v, def[2] ); 
      if ( reduce == 1 ) TopoGL.mDEMreduce = TopoGL.DEM_SHRINK;
      else               TopoGL.mDEMreduce = TopoGL.DEM_CUT;
    } else {
      TDLog.e("missing DEM-3D key: " + k );
    }
    return ret;
  }

  private static String updatePrefWalls3D( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref walls3d: " + k );
    String[] key = TDPrefKey.WALLS3D;
    String[] def = TDPrefKey.WALLS3Ddef;
    if ( k.equals( key[0] ) ) { // CAVE3D_SPLAY_USE
      TglParser.mSplayUse = tryIntValue( hlp, k, v, def[0] ); 
    } else if ( k.equals( key[1] ) ) { // CAVE3D_ALL_SPLAY
      GlModel.mAllSplay = tryBooleanValue( hlp, k, v, bool(def[1]) ); 
    } else if ( k.equals( key[2] ) ) { // CAVE3D_SPLAY_PROJ
      TopoGL.mSplayProj = tryBooleanValue( hlp, k, v, bool(def[2]) ); 
    } else if ( k.equals( key[3] ) ) { // CAVE3D_SPLAY_THR
      TopoGL.mSplayThr = tryFloatValue( hlp, k, v, def[3] );
    } else if ( k.equals( key[4] ) ) {  // CAVE3D_SPLIT_TRIANGLES
      GlModel.mSplitTriangles = tryBooleanValue( hlp, k, v, bool(def[4]) ); 
    } else if ( k.equals( key[5] ) ) {  // CAVE3D_SPLIT_RANDOM
      float r = tryFloatValue( hlp, k, v, def[5] );
      if ( r > 0.0001f ) {
        GlModel.mSplitRandomizeDelta = r;
        GlModel.mSplitRandomize = true;
      } else {
        GlModel.mSplitRandomize = false;
      }
    } else if ( k.equals( key[6] ) ) { // CAVE3D_SPLIT_STRETCH
      float r = tryFloatValue( hlp, k, v, def[6] );
      if ( r > 0.0001f ) {
        GlModel.mSplitStretchDelta = r;
        GlModel.mSplitStretch = true;
      } else {
        GlModel.mSplitStretch = false;
      }
    } else if ( k.equals( key[7] ) ) { // CAVE3D_POWERCRUST_DELTA 
      float delta = tryFloatValue( hlp, k, v, def[6] );
      if ( delta > 0 ) GlModel.mPowercrustDelta = delta;
    } else {
      TDLog.e("missing Walls-3D key: " + k );
    }
    return ret;
  }

  // ------------------------------------------------------------------------------------------
  private static String updatePrefGeek( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref data: " + k );
    String[] key = TDPrefKey.GEEK;
    String[] def = TDPrefKey.GEEKdef;
    if ( k.equals( key[0] ) ) {
      mSingleBack = tryBooleanValue( hlp, k, v, bool(def[0]) ); // DISTOX_SINGLE_BACK
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_PALETTES
      setPalettes( tryBooleanValue( hlp, k, v, bool(def[1]) ) );
    // } else if ( k.equals( key[1] ) ) { // CLEAR_BACKUPS
    //   setBackupsClear( tryBooleanValue( hlp, k, v, bool(def[1]) ) ); // DISTOX_BACKUPS_CLEAR
    } else if ( k.equals( key[ 2 ] ) ) {           // DISTOX_MKEYBOARD (bool)
      mKeyboard = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) {           // DISTOX_NO_CURSOR(bool)
      mNoCursor = tryBooleanValue( hlp, k, v, bool(def[3]) );
    } else if ( k.equals( key[4] ) ) {
      mPacketLog = tryBooleanValue( hlp, k, v, bool(def[4]) ); // DISTOX_PACKET_LOGGER
    } else if ( k.equals( key[5] ) ) {
      mTh2Edit = tryBooleanValue( hlp, k, v, bool(def[5]) ); // DISTOX_TH2_EDIT
      mMainFlag |= FLAG_BUTTON;
    } else if ( TDLevel.isDebugBuild() && k.equals( key[13] ) ) {
      mWithDebug =  tryBooleanValue( hlp, k, v, bool(def[13]) ); // DISTOX_WITH_DEBUG
      TDLevel.setLevelWithDebug( mWithDebug );
    } else {
      TDLog.e("missing GEEK key: " + k );
    }
    // if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefGeekShot( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref data: " + k );
    String[] key = TDPrefKey.GEEKSHOT;
    String[] def = TDPrefKey.GEEKSHOTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_DIVING_MODE
      mDivingMode   = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_BACKSIGHT_SPLAY
      mBacksightSplay = tryBooleanValue( hlp, k, v, bool(def[1]) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_RECENT_SHOT
      mShotRecent   = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_RECENT_TIMEOUT
      mRecentTimeout = tryIntValue( hlp, k, v, def[3] );
      if ( mRecentTimeout < 0 ) { mRecentTimeout = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_EXTEND_FRAC
      mExtendFrac   = tryBooleanValue( hlp, k, v, bool(def[ 4]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_BACKSHOT (bool)
      mDistoXBackshot = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_BEDDING
      mBedding      = tryBooleanValue( hlp, k, v, bool(def[ 6]) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_WITH_SENSORS
      mWithSensors  = tryBooleanValue( hlp, k, v, bool(def[ 7]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_LOOP_CLOSURE_VALUE
      setLoopClosure( tryIntValue( hlp, k, v, def[ 8] ) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_LOOP_THR
      mLoopThr = tryFloatValue( hlp, k, v, def[ 9] );
    } else if ( k.equals( key[10 ] ) ) { // DISTOX_ANDROID_AZIMUTH
      mWithAzimuth  = tryBooleanValue( hlp, k, v, bool(def[10]) );
    } else if ( k.equals( key[11  ] ) ) { // DISTOX_SHOT_TIMER [3 ..)
      mTimerWait        = tryIntValue( hlp, k, v, def[11] );
      if ( mTimerWait < 0 ) { mTimerWait = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 12 ] ) ) { // DISTOX_BEEP_VOLUME [0 .. 100]
      ret = setBeepVolume( tryIntValue( hlp, k, v, def[12] ) );
    } else if ( k.equals( key[ 13 ] ) ) { // DISTOX_BLUNDER_SHOT
      mBlunderShot = tryBooleanValue( hlp, k, v, bool(def[13]) );
    } else if ( k.equals( key[ 14 ] ) ) { // DISTOX_SPLAY_STATION 
      mSplayStation = tryBooleanValue( hlp, k, v, bool(def[14]) );
    } else if ( k.equals( key[ 15 ] ) ) { // DISTOX_SPLAY_GROUP
      mSplayOnlyForward = tryBooleanValue( hlp, k, v, bool(def[15]) );
    // } else if ( k.equals( key[13 ] ) ) { // DISTOX_TDMANAGER
    //   mWithTdManager = tryBooleanValue( hlp, k, v, bool(def[13]) );

    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_DIST_TOLERANCE
    //   mDistTolerance = tryFloatValue( hlp, k, v, def[ 9]  );
    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_SPLAY_ACTIVE
    //   mSplayActive  = prefs.getBoolean( key[ 9],  bool(def[ 9]) );
    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_WITH_RENAME
    //   mWithRename   = tryBooleanValue( hlp, k, v, bool(def[ 9]) );
    } else {
      TDLog.e("missing GEEK_SHOT key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String setBeepVolume( int volume )
  {
    if ( volume <   0 ) { mBeepVolume =   0; return TDString.ZERO; }
    if ( volume > 100 ) { mBeepVolume = 100; return "100"; }
    mBeepVolume = volume;
    return null;
  }

  private static String updatePrefGeekPlot( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref data: " + k );
    String[] key = TDPrefKey.GEEKPLOT;
    String[] def = TDPrefKey.GEEKPLOTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_PLOT_SHIFT
      mPlotShift    = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_PLOT_SPLIT
      mPlotSplit = tryBooleanValue( hlp, k, v, bool(def[ 1 ]) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_STYLUS_SIZE
      setStylusSize( tryFloatValue( hlp, k, v, def[ 2] ) ); // STYLUS_MM - LINE ENDS
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_BACKUP_NUMBER
      mBackupNumber  = tryIntValue( hlp, k, v, def[ 3 ] ); 
      if ( mBackupNumber <  4 ) { mBackupNumber =  4; ret = Integer.toString( mBackupNumber ); }
      else if ( mBackupNumber > 10 ) { mBackupNumber = 10; ret = Integer.toString( mBackupNumber ); }
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_BACKUP_INTERVAL
      mBackupInterval = tryIntValue( hlp, k, v, def[ 4 ] );  
      if ( mBackupInterval <  10 ) { mBackupInterval =  10; ret = Integer.toString( mBackupInterval ); }
      else if ( mBackupInterval > 600 ) { mBackupInterval = 600; ret = Integer.toString( mBackupInterval ); }
    // } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_AUTO_XSECTIONS
    //   mAutoXSections = tryBooleanValue( hlp, k, v, bool(def[ 5 ]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_SAVED_STATIONS
      mSavedStations = tryBooleanValue( hlp, k, v, bool(def[ 5 ]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_LEGONLY_UPDATE
      mLegOnlyUpdate = tryBooleanValue( hlp, k, v, bool(def[ 6 ]) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_FULL_AFFINE
      mFullAffine    = tryBooleanValue( hlp, k, v, bool(def[ 7 ]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_WITH_LEVELS
      mWithLevels    = tryIntValue( hlp, k, v, def[ 8 ] );
    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_GRAPH_PAPER_SCALE - handled by a dialog
    //   mGraphPaperScale = tryIntValue( hlp, k, v, def[ 9 ] );
    } else if ( k.equals( key[10 ] ) ) { // DISTOX_SLANT_XSECTION
      mSlantXSection = tryBooleanValue( hlp, k, v, bool(def[10 ]) );
    } else if ( k.equals( key[11 ] ) ) { // DISTOX_OBLIQUE_PROJECTED
      mObliqueMax = tryIntValue( hlp, k, v, def[ 11 ] );
      if ( mObliqueMax < 10 )  { mObliqueMax = 0; ret = Integer.toString( mObliqueMax ); }
      else if ( mObliqueMax > 80 ) { mObliqueMax = 80; ret = Integer.toString( mObliqueMax ); }
    } else if ( k.equals( key[12 ] ) ) { // DISTOX_LINE_ENDS
      mLineEnds = tryIntValue( hlp, k, v, def[12 ] );
    } else {
      TDLog.e("missing GEEK_PLOT key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefGeekSplay( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref data: " + k );
    String[] key = TDPrefKey.GEEKsplay;
    String[] def = TDPrefKey.GEEKsplaydef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SPLAY_CLASSES
      mSplayClasses = tryBooleanValue( hlp, k, v, bool(def[ 0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_DISCRETE_COLORS was DISTOX_SPLAY_COLOR
      // mSplayColor   = tryBooleanValue( hlp, k, v, bool(def[ 1]) );
      mDiscreteColors = tryIntValue( hlp, k, v, def[ 1] );
      mSplayColor = (mDiscreteColors > 0);
    // } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SPLAY_AS_DOT
    //   mSplayAsDot = tryBooleanValue( hlp, k, v, bool(def[ 2]) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SPLAY_VERT_THRS
      mSplayVertThrs = tryFloatValue( hlp, k, v, def[ 2] );
      if ( mSplayVertThrs <  0 ) { mSplayVertThrs =  0; ret = TDString.ZERO; }
      if ( mSplayVertThrs > 91 ) { mSplayVertThrs = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_SPLAY_DASH (0,1,2)
      mDashSplay = tryIntValue( hlp, k, v, def[ 3] );      
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_VERT_SPLAY
      mVertSplay   = tryFloatValue( hlp, k, v, def[ 4] );
      if ( mVertSplay <  0 ) { mVertSplay =  0; ret = TDString.ZERO; }
      if ( mVertSplay > 91 ) { mVertSplay = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_HORIZ_SPLAY
      mHorizSplay  = tryFloatValue( hlp, k, v, def[ 5] );
      if ( mHorizSplay <  0 ) { mHorizSplay =  0; ret = TDString.ZERO; }
      if ( mHorizSplay > 91 ) { mHorizSplay = 91; ret = TDString.NINETYONE; }
      mCosHorizSplay = TDMath.cosd( mHorizSplay );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_SECTION_SPLAY
      mSectionSplay = tryFloatValue( hlp, k, v, def[ 6] );
      if ( mSectionSplay <  0 ) { mSectionSplay =  0; ret = TDString.ZERO; }
      if ( mSectionSplay > 91 ) { mSectionSplay = 91; ret = TDString.NINETYONE; }
      mCosSectionSplay  = TDMath.cosd( mSectionSplay );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_SPLAY_DASH_COLOR
      mSplayDashColor = tryColorValue( hlp, k, v, def[ 7] ); 
      BrushManager.setSplayDashColor( mSplayDashColor );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_SPLAY_DOT_COLOR
      mSplayDotColor = tryColorValue( hlp, k, v, def[ 8] ); 
      BrushManager.setSplayDotColor( mSplayDotColor );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_SPLAY_LATEST_COLOR
      mSplayLatestColor = tryColorValue( hlp, k, v, def[ 9] ); 
      BrushManager.setSplayLatestColor( mSplayLatestColor );
    } else {
      TDLog.e("missing GEEK_SPLAY key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefGeekDevice( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    String[] key = TDPrefKey.GEEKDEVICE;
    String[] def = TDPrefKey.GEEKDEVICEdef;
    int j = 0;
    if ( k.equals( key[ ++j ] ) ) { // DISTOX_UNNAMED_DEVICE 
      mUnnamedDevice = tryBooleanValue( hlp, k, v, bool(def[j]) ); // BT_NONAME
    } else if ( k.equals( key[ ++j ] ) ) { // DISTOX_SOCKET_DELAY index 1
      mConnectSocketDelay = tryIntValue( hlp, k, v, def[j] );  
      if ( mConnectSocketDelay < 0  ) { mConnectSocketDelay =  0; ret = TDString.ZERO; }
      if ( mConnectSocketDelay > 60 ) { mConnectSocketDelay = 60; ret = TDString.SIXTY; } // was 100
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_SECOND_DISTOX (bool)
      mSecondDistoX = tryBooleanValue( hlp, k, v, bool(def[j]) );
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_WAIT_DATA
      mWaitData = tryIntValue( hlp, k, v, def[j] ); 
      if ( mWaitData <    0 ) { mWaitData =    0; ret = Integer.toString( mWaitData ); }
      if ( mWaitData > 2000 ) { mWaitData = 2000; ret = Integer.toString( mWaitData ); }
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_WAIT_CONN
      mWaitConn = tryIntValue( hlp, k, v, def[j] );
      if ( mWaitConn <   50 ) { mWaitConn =   50; ret = Integer.toString( mWaitConn ); }
      if ( mWaitConn > 2000 ) { mWaitConn = 2000; ret = Integer.toString( mWaitConn ); }
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_WAIT_LASER
      mWaitLaser = tryIntValue( hlp, k, v, def[j] );
      if ( mWaitLaser <  500 ) { mWaitLaser =  500; ret = Integer.toString( mWaitLaser ); }
      if ( mWaitLaser > 5000 ) { mWaitLaser = 5000; ret = Integer.toString( mWaitLaser ); }
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_WAIT_SHOT
      mWaitShot  = tryIntValue( hlp, k, v, def[j] );
      if ( mWaitShot <   500 ) { mWaitShot =   500; ret = Integer.toString( mWaitShot ); }
      if ( mWaitShot > 10000 ) { mWaitShot = 10000; ret = Integer.toString( mWaitShot ); }
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_FIRMWARE_SANITY
      mFirmwareSanity = tryBooleanValue( hlp, k, v, bool(def[j]) );
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_BRIC_MODE
      mBricMode = tryIntValue( hlp, k, v, def[j] );
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_BRIC_ZERO_LENGTH
      mBricZeroLength = tryBooleanValue( hlp, k, v, bool(def[j]) );
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_BRIC_INDEX_IS_ID
      mBricIndexIsId = tryBooleanValue( hlp, k, v, bool(def[j]) );
    } else if ( k.equals( key[ ++j] ) ) { // DISTOX_SAP5_BIT16_BUG
      mSap5Bit16Bug = tryBooleanValue( hlp, k, v, bool(def[j]) );
    } else {
      TDLog.e("missing DEVICE key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefGeekLine( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    String[] key = TDPrefKey.GEEKLINE;
    String[] def = TDPrefKey.GEEKLINEdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_REDUCE_ANGLE
      ret = setReduceAngle( tryFloatValue(  hlp, k, v, def[0] ) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_LINE_ACCURACY
      ret = setLineAccuracy( tryFloatValue( hlp, k, v, def[1] ) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LINE_CORNER
      ret = setLineCorner( tryFloatValue(   hlp, k, v, def[2] ) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_WEED_DISTANCE
      ret = setWeedDistance( tryFloatValue(   hlp, k, v, def[3] ) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_WEED_LENGTH
      ret = setWeedLength( tryFloatValue(   hlp, k, v, def[4] ) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_WEED_BUFFER
      ret = setWeedBuffer( tryFloatValue(   hlp, k, v, def[5] ) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_LINE_SNAP (bool)
      mLineSnap = tryBooleanValue(          hlp, k, v, bool(def[6]) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_LINE_CURVE (bool)
      mLineCurve = tryBooleanValue(         hlp, k, v, bool(def[7]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_LINE_STRAIGHT
      mLineStraight = tryBooleanValue(      hlp, k, v, bool(def[8]) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_PATH_MULTISELECT (bool)
      mPathMultiselect = tryBooleanValue(   hlp, k, v, bool(def[9]) );
    // } else if ( k.equals( key[10 ] ) ) { // DISTOX_COMPOSITE_ACTIONS (bool)
    //   mCompositeActions = tryBooleanValue(  hlp, k, v, bool(def[10]) );

    } else {
      TDLog.e("missing DEVICE key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  // ------------------------------------------------------------------------------------------
  private static String updatePrefImport( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref import: " + k );
    String[] key = TDPrefKey.EXPORT_import;
    String[] def = TDPrefKey.EXPORT_importdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_PT_CMAP
      // not handled here
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SPLAY_EXTEND (bool)
      mLRExtend = tryBooleanValue( hlp, k, v, bool(def[ 1]) ); 
    } else {
      TDLog.e("missing IMPORT key: " + k );
    }
    return null;
  }

  private static String updatePrefExportEnable( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.EXPORT_ENABLE;
    String[] def = TDPrefKey.EXPORT_ENABLEdef;
    for ( int n = 0; n < key.length; ++ n ) {
      if ( k.equals( key[n] ) ) {
        boolean b = tryBooleanValue( hlp, k, v, bool(def[n]) ); 
        TDConst.mSurveyExportEnable[ 1 + n ] = b;
        return b ? TDPrefKey.TRUE : TDPrefKey.FALSE;
      }
    }
    return null;
  }

  private static String updatePrefGeekImport( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref import: " + k );
    String[] key = TDPrefKey.GEEKIMPORT;
    String[] def = TDPrefKey.GEEKIMPORTdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_ZIP_WITH_SYMBOLS
      mZipWithSymbols = tryBooleanValue( hlp, k, v, bool(def[ 0]) ); 
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_IMPORT_DATAMODE (choice)
      mImportDatamode = tryIntValue( hlp, k, v, def[ 1] );
    } else if ( k.equals( key[ 2 ] ) ) {        // DISTOX_AUTO_XSECTIONS
      mAutoXSections = tryBooleanValue( hlp, k, v, bool(def[ 2]) ); 
    } else if ( k.equals( key[ 3 ] ) ) {        // DISTOX_AUTO_STATIONS
      mAutoStations = tryBooleanValue( hlp, k, v, bool(def[ 3]) ); 
    } else if ( k.equals( key[ 4 ] ) ) {        // DISTOX_LRUD_COUNT
      mLRUDcount = tryBooleanValue( hlp, k, v, bool(def[ 4]) );
    } else if ( k.equals( key[ 5 ] ) ) {        // DISTOX_ZIP_SHARE_CATEGORY
      mZipShareCategory = tryBooleanValue( hlp, k, v, bool(def[ 5]) );
    // } else if ( k.equals( key[ 4 ] ) ) {        // DISTOX_AUTO_PLOT_EXPORT moved to EXPORT
    //   mAutoExportPlotFormat = tryIntValue( hlp, k, v, def[ 4] );
    // } else if ( k.equals( key[ 2 ] ) ) {        // DISTOX_TRANSFER_CSURVEY
    //   mExportTcsx = tryBooleanValue( hlp, k, v, bool(def[ 2]) ); 
    } else {
      TDLog.e("missing GEEK_IMPORT key: " + k );
    }
    return null;
  }
    
  private static String updatePrefExport( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref export: " + k );
    String[] key = TDPrefKey.EXPORT;
    String[] def = TDPrefKey.EXPORTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_EXPORT_SHOTS (choice)
      mExportShotsFormat = tryIntValue( hlp, k, v, def[ 0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_EXPORT_PLOT (choice)
      mExportPlotFormat = tryIntValue( hlp, k, v, def[ 1] );
    } else if ( k.equals( key[ 2 ] ) ) {        // DISTOX_AUTO_PLOT_EXPORT
      mAutoExportPlotFormat = tryIntValue( hlp, k, v, def[ 2] );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_ORTHO_LRUD
      mOrthogonalLRUDAngle  = tryFloatValue( hlp, k, v, def[ 3] );
      if ( mOrthogonalLRUDAngle <  0 ) { mOrthogonalLRUDAngle =  0;  ret = TDString.ZERO; }
      if ( mOrthogonalLRUDAngle > 90 ) { mOrthogonalLRUDAngle = 90;  ret = TDString.NINETY; }
      mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
      mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    } else if ( k.equals( key[  4 ] ) ) { // DISTOX_LRUD_VERTICAL
      mLRUDvertical = tryFloatValue( hlp, k, v, def[ 4] );
      if ( mLRUDvertical <  0 ) { mLRUDvertical =  0; ret = TDString.ZERO; }
      if ( mLRUDvertical > 91 ) { mLRUDvertical = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[  5 ] ) ) { // DISTOX_LRUD_HORIZONTAL
      mLRUDhorizontal = tryFloatValue( hlp, k, v, def[ 5] );
      if ( mLRUDhorizontal <  0 ) { mLRUDhorizontal =  0; ret = TDString.ZERO; }
      if ( mLRUDhorizontal > 91 ) { mLRUDhorizontal = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[  6 ] ) ) { // DISTOX_BEZIER_STEP
      mBezierStep  = tryFloatValue( hlp, k, v, def[ 6] );
      if ( mBezierStep < 0 ) { mBezierStep = 0; ret = TDString.ZERO; }
      if ( mBezierStep > 3 ) { mBezierStep = 3; ret = TDString.THREE; } // was 2
    } else {
      TDLog.e("missing EXPORT key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefSvx( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref SVX: " + k );
    String[] key = TDPrefKey.EXPORT_SVX;
    String[] def = TDPrefKey.EXPORT_SVXdef;
    if ( k.equals( key[0] ) ) { // DISTOX_SURVEX_EOL (choice)
      mSurvexEol = ( tryStringValue( hlp, k, v, def[0] ).equals(def[0]) )? "\n" : "\r\n";
    } else if ( k.equals( key[1] ) ) { // DISTOX_SURVEX_SPLAY (bool)
      mSurvexSplay = tryBooleanValue( hlp, k, v, bool(def[1]) );
    } else if ( k.equals( key[2] ) ) { // DISTOX_SURVEX_LRUD (bool)
      mSurvexLRUD = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[3] ) ) { // DISTOX_SURVEX_EPSG (int)
      mSurvexEPSG = tryIntValue( hlp, k, v, def[3] );
    } else {
      TDLog.e("missing EXPORT SVX key: " + k );
    }
    return null;
  }

  private static String updatePrefTh( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref TH: " + k );
    String ret = null;
    String[] key = TDPrefKey.EXPORT_TH;
    String[] def = TDPrefKey.EXPORT_THdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_THERION_CONFIG (bool)
      mTherionWithConfig    = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_THERION_MAPS (bool)
      mTherionMaps      = tryBooleanValue( hlp, k, v, bool(def[ 1 ]) );
    // } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_AUTO_STATIONS (bool)
    //   mAutoStations     = tryBooleanValue( hlp, k, v, bool(def[ 2 ]) );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_XTHERION_AREAS (bool)
    //   mXTherionAreas = tryBooleanValue( hlp, k, v, bool(def[ ]) );   
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_THERION_SPLAYS (bool)
      mTherionSplays    = tryBooleanValue( hlp, k, v, bool(def[ 2 ]) );   
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_SURVEX_LRUD (bool)
      mSurvexLRUD       = tryBooleanValue( hlp, k, v, bool(def[ 3 ]) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_TH2_SCALE
      ret = setExportScale( tryIntValue( hlp, k, v, def[4] ) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_TH2_XVI (bool)
      mTherionXvi = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else {
      TDLog.e("missing EXPORT TH key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefDat( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref DAT: " + k );
    String[] key = TDPrefKey.EXPORT_DAT;
    String[] def = TDPrefKey.EXPORT_DATdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_STATION_PREFIX (bool)
      mExportStationsPrefix = tryBooleanValue( hlp, k, v, bool(def[ 0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_COMPASS_SPLAYS (bool)
      mCompassSplays        = tryBooleanValue( hlp, k, v, bool(def[ 1]) );   
    } else if ( k.equals( key[  2 ] ) ) { // DISTOX_SWAP_LR (bool)
      mSwapLR               = tryBooleanValue( hlp, k, v, bool(def[ 2]) );
    } else {
      TDLog.e("missing EXPORT DAT key: " + k );
    }
    return null;
  }

  private static String updatePrefSrv( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref DAT: " + k );
    String[] key = TDPrefKey.EXPORT_SRV;
    String[] def = TDPrefKey.EXPORT_SRVdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_WALLS_SPLAYS (bool)
      mWallsSplays  = tryBooleanValue( hlp, k, v, bool(def[0]) );   
    // } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_WALLS_UD
    //   mWallsUD = tryIntValue( hlp, k, v, def[1] );
    } else {
      TDLog.e("missing EXPORT SRV key: " + k );
    }
    return null;
  }

  private static String updatePrefTro( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref TRO: " + k );
    String[] key = TDPrefKey.EXPORT_TRO;
    String[] def = TDPrefKey.EXPORT_TROdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_VTOPO_SPLAYS (bool)
      mVTopoSplays     = tryBooleanValue( hlp, k, v, bool(def[ 0]) );   
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_VTOPO_LRUD (bool)
      mVTopoLrudAtFrom = tryBooleanValue( hlp, k, v, bool(def[ 1]) );   
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_VTOPO_TROX (bool)
      mVTopoTrox       = tryBooleanValue( hlp, k, v, bool(def[ 2]) );   
    } else {
      TDLog.e("missing EXPORT TRO key: " + k );
    }
    return null;
  }

  private static String updatePrefKml( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref KML: " + k );
    String[] key = TDPrefKey.EXPORT_KML;
    String[] def = TDPrefKey.EXPORT_KMLdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_KML_STATIONS (bool)
      mKmlStations = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_KML_SPLAYS (bool)
      mKmlSplays   = tryBooleanValue( hlp, k, v, bool(def[ 1 ]) );
    } else {
      TDLog.e("missing EXPORT KML key: " + k );
    }
    return null;
  }


  private static String updatePrefGpx( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref GPX: " + k );
    String[] key = TDPrefKey.EXPORT_GPX;
    String[] def = TDPrefKey.EXPORT_GPXdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_GPX_SINGLE_TRACK (bool)
      mGPXSingleTrack = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else {
      TDLog.e("missing EXPORT GPX key: " + k );
    }
    return null;
  }

  private static String updatePrefCsx( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref CSX: " + k );
    String[] key = TDPrefKey.EXPORT_CSX;
    String[] def = TDPrefKey.EXPORT_CSXdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_STATION_PREFIX
      mExportStationsPrefix = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else {
      TDLog.e("missing EXPORT CSX key: " + k );
    }
    return null;
  }

  private static String updatePrefCsv( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref CSV: " + k );
    String[] key = TDPrefKey.EXPORT_CSV;
    String[] def = TDPrefKey.EXPORT_CSVdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_CSV_RAW (bool)
      mCsvRaw      = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_CSV_SEP (choice)
      mCsvSeparator = CSV_SEPARATOR[ tryIntValue( hlp, k, v, def[1] ) ]; 
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SURVEX_EOL
      mSurvexEol = ( tryStringValue( hlp, k, v, def[2] ).equals(def[0]) )? "\n" : "\r\n";
    } else {
      TDLog.e("missing EXPORT CSV key: " + k );
    }
    return null;
  }

  private static String updatePrefShp( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref DXF: " + k );
    String[] key = TDPrefKey.EXPORT_SHP;
    String[] def = TDPrefKey.EXPORT_SHPdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SHP_GEOREF (bool)
      mShpGeoref = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else {
      TDLog.e("missing EXPORT SHP key: " + k );
    }
    return null;
  }

  private static String updatePrefDxf( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref DXF: " + k );
    String[] key = TDPrefKey.EXPORT_DXF;
    String[] def = TDPrefKey.EXPORT_DXFdef;
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_DXF_SCALE
    //   mDxfScale = tryFloatValue( hlp, k, v, def[ ] );
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_DXF_BLOCKS (bool)
      mDxfBlocks = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_ACAD_VERSION (choice)
      try { mAcadVersion = tryIntValue( hlp, k, v, def[ 1 ] ); } catch ( NumberFormatException e) {
        TDLog.e( e.getMessage() );
      }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_ACAD_SPLINE (bool)
      mAcadSpline = tryBooleanValue( hlp, k, v, bool(def[ 2 ]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_DXF_REFERENCE (bool)
      mDxfReference = tryBooleanValue( hlp, k, v, bool(def[ 3 ]) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_ACAD_LAYER (bool)
      mAcadLayer = tryBooleanValue( hlp, k, v, bool(def[ 4 ]) );
    } else {
      TDLog.e("missing EXPORT DXF key: " + k );
    }
    return null;
  }

  // NO_PNG
  // private static String updatePrefPng( TDPrefHelper hlp, String k, String v )
  // {
  //   String ret = null;
  //   // TDLog.v("update pref PNG: " + k );
  //   String[] key = TDPrefKey.EXPORT_PNG;
  //   String[] def = TDPrefKey.EXPORT_PNGdef;
  //   if ( k.equals( key[ 0 ] ) ) { // DISTOX_BITMAP_SCALE
  //     mBitmapScale = tryFloatValue( hlp, k, v, def[0] );
  //     if ( mBitmapScale < 0.5f ) { mBitmapScale = 0.5f; ret = "0.5"; }
  //     if ( mBitmapScale >  10f ) { mBitmapScale =  10f; ret = TDString.TEN; }
  //   } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_BITMAP_BGCOLOR
  //     return setBitmapBgcolor( hlp.getSharedPrefs(), k, tryStringValue( hlp, k, v, def[1] ), def[1] );
  //   } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SVG_GRID
  //     mSvgGrid = tryBooleanValue( hlp, k, v, bool(def[2]) );
  //   } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_THERION_SPLAYS
  //     mTherionSplays = tryBooleanValue( hlp, k, v, bool(def[ 3]) );   
  //   // } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_AUTO_STATIONS
  //   //   mAutoStations  = tryBooleanValue( hlp, k, v, bool(def[ 4]) );
  //   } else {
  //     TDLog.e("missing EXPORT PNG key: " + k );
  //   }
  //   if ( ret != null ) hlp.update( k, ret );
  //   return ret;
  // }

  private static String updatePrefSvg( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref SVY: " + k );
    String[] key = TDPrefKey.EXPORT_SVG;
    String[] def = TDPrefKey.EXPORT_SVGdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SVG_ROUNDTRIP (bool)
      mSvgRoundTrip = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SVG_GRID (bool)
      mSvgGrid = tryBooleanValue( hlp, k, v, bool(def[1]) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SVG_LINE_DIR (bool)
      mSvgLineDirection = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_SVG_SPLAYS (bool)
      mSvgSplays = tryBooleanValue( hlp, k, v, bool(def[3]) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_SVG_GROUPS (bool)
      mSvgGroups = tryBooleanValue( hlp, k, v, bool(def[4]) );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_SVG_IN_HTML (bool)
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_SVG_IN_HTML (bool)
    //   mSvgInHtml = tryBooleanValue( hlp, k, bool(def[ ]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_SVG_POINT_STROKE
      mSvgPointStroke    = tryFloatValue( hlp, k, v, def[5] );
      if ( mSvgPointStroke < 0.01f ) { mSvgPointStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_SVG_LABEL_STROKE
      mSvgLabelStroke    = tryFloatValue( hlp, k, v, def[6] );
      if ( mSvgLabelStroke < 0.01f ) { mSvgLabelStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_SVG_LINE_STROKE
      mSvgLineStroke     = tryFloatValue( hlp, k, v, def[7] );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_SVG_GRID_STROKE
      mSvgGridStroke     = tryFloatValue( hlp, k, v, def[8] );
      if ( mSvgGridStroke < 0.01f ) { mSvgGridStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_SVG_SHOT_STROKE
      mSvgShotStroke     = tryFloatValue( hlp, k, v, def[9] );
      if ( mSvgShotStroke < 0.01f ) { mSvgShotStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[10 ] ) ) { // DISTOX_SVG_LINEDIR_STROKE
      mSvgLineDirStroke  = tryFloatValue( hlp, k, v, def[10] );
      if ( mSvgLineStroke < 0.01f ) { mSvgLineStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[11] ) ) {  // DISTOX_SVG_STATION_SIZE
      mSvgStationSize    = tryIntValue( hlp, k, v, def[11] );
      if ( mSvgStationSize < 1 ) { mSvgStationSize = 1; ret = "1"; }
    } else if ( k.equals( key[12] ) ) {  // DISTOX_SVG_LABEL_SIZE
      mSvgLabelSize    = tryIntValue( hlp, k, v, def[12] );
      if ( mSvgLabelSize < 1 ) { mSvgLabelSize = 1; ret = "1"; }
    // } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_BEZIER_STEP
    //   mBezierStep  = tryFloatValue( hlp, k, v, def[8] );
    } else if ( k.equals( key[13] ) ) {  // DISTOX_SVG_PROGRAM
      mSvgProgram    = tryIntValue( hlp, k, v, def[13] );
      if ( mSvgProgram < 0 || mSvgProgram > 1 ) mSvgProgram = 0;
      setExportScale( mTherionScale );
    } else {
      TDLog.e("missing EXPORT_SVG key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }
  // --------------------------------------------------------------------------

  private static String updatePrefData( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref data: " + k );
    String[] key = TDPrefKey.DATA;
    String[] def = TDPrefKey.DATAdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_CLOSE_DISTANCE
      mCloseDistance  = tryFloatValue( hlp, k, v, def[0] );
      if ( mCloseDistance < 0.0001f ) { mCloseDistance = 0.0001f; ret = "0.0001"; }
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_MAX_SHOT_LENGTH
      mMaxShotLength  = tryFloatValue( hlp, k, v, def[1] );  
      if ( mMaxShotLength < 20 ) { mMaxShotLength = 20; ret = TDString.TWENTY; }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_MIN_LEG_LENGTH
      mMinLegLength   = tryFloatValue( hlp, k, v, def[2] );  
      if ( mMinLegLength < 0 ) { mMinLegLength = 0; ret = TDString.ZERO; }
      if ( mMinLegLength > 5 ) { mMinLegLength = 5; ret = TDString.FIVE; }
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_LEG_SHOTS (choice)
      mMinNrLegShots  = tryIntValue(   hlp, k, v, def[3] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_EXTEND_THR2
      mExtendThr      = tryFloatValue( hlp, k, v, def[4] );
      if ( mExtendThr <  0 ) { mExtendThr =  0; ret = TDString.ZERO; }
      if ( mExtendThr > 90 ) { mExtendThr = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_VTHRESHOLD
      mVThreshold     = tryFloatValue( hlp, k, v, def[5] );
      if ( mVThreshold <  0 ) { mVThreshold =  0; ret =  TDString.ZERO; }
      if ( mVThreshold > 90 ) { mVThreshold = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_AZIMUTH_MANUAL (bool)
      mAzimuthManual  = tryBooleanValue( hlp, k, v, bool(def[6]) ); 
      // TDAzimuth.setAzimuthManual( mAzimuthManual ); // FIXME FIXED_EXTEND 20240603
      TDAzimuth.resetRefAzimuth( TopoDroidApp.mShotWindow, TDAzimuth.mRefAzimuth, mAzimuthManual ); // FIXME FIXED_EXTEND 20240603
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_PREV_NEXT (bool)
      mPrevNext = tryBooleanValue( hlp, k, v, bool(def[ 7]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_BACKSIGHT (bool)
      mBacksightInput = tryBooleanValue( hlp, k, v, bool(def[ 8]) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_LEG_FEEDBACK
      mTripleShot   = tryIntValue( hlp, k, v, def[ 9] );
    } else {
      TDLog.e("missing DATA key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefUnits( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref units: " + k );
    String[] key = TDPrefKey.UNITS;
    String[] def = TDPrefKey.UNITSdef;
    if ( k.equals( key[ 0 ] ) ) {    // DISTOX_UNIT_LENGTH (choice)
      if ( tryStringValue( hlp, k, v, def[0] ).equals(def[0]) ) {
        mUnitLength = 1.0f;
        mUnitLengthStr = "m";
      } else {
        mUnitLength = TDUtil.M2FT;
        mUnitLengthStr = "ft";
      }
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_UNIT_ANGLE (choice)
      if ( tryStringValue( hlp, k, v, def[1] ).equals(def[1]) ) {
        mUnitAngle = 1.0f;
        mUnitAngleStr = "deg";
      } else {
        mUnitAngle = TDUtil.DEG2GRAD;
        mUnitAngleStr = "grad";
      }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_UNIT_GRID (choice)
      mUnitGrid = tryFloatValue( hlp, k, v, def[2] ); 
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_UNIT_MEASURE (choice)
      mUnitMeasure = tryFloatValue( hlp, k, v, def[3] ); 
    } else {
      TDLog.e("missing UNITS key: " + k );
    }
    return null;
  }

  private static String updatePrefAccuracy( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref accuracy: " + k );
    String[] key = TDPrefKey.ACCURACY;
    String[] def = TDPrefKey.ACCURACYdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_ACCEL_PERCENT 
      mAccelerationThr = tryFloatValue( hlp, k, v, def[0] );
      if ( mAccelerationThr < 0 ) { mAccelerationThr = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_MAG_PERCENT
      mMagneticThr     = tryFloatValue( hlp, k, v, def[1] );
      if ( mMagneticThr < 0 ) { mMagneticThr = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_DIP_THR
      mDipThr          = tryFloatValue( hlp, k, v, def[2] );
      if ( mDipThr < 0 ) { mDipThr = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_SIBLING_PERCENT
      ret = setSiblingThr( tryFloatValue( hlp, k, v, def[3] ) );
    } else {
      TDLog.e("missing ACCURACY key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefLocation( TDPrefHelper hlp, String k, String v )
  {
    // String ret = null;
    // TDLog.v("update pref location: " + k );
    String[] key = TDPrefKey.LOCATION;
    String[] def = TDPrefKey.LOCATIONdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNIT_LOCATION (choice)
      mUnitLocation  = tryStringValue( hlp, k, v, def[0] ).equals(def[0]) ? TDUtil.DDMMSS : TDUtil.DEGREE;
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_ALTITUDE
    //   try {
    //     mEllipAlt = Integer.parseInt( tryStringValue( hlp, k, v, ALTITUDE ) );
    //   } catch ( NumberFormatException e ) { mEllipAlt = _WGS84; }
    } else if ( k.equals( key[ 1 ] ) ) {
      mCRS = tryStringValue( hlp, k, v, def[1] );     // DISTOX_CRS (arbitrary)
    } else if ( k.equals( key[ 2 ] ) ) {    // DISTOX_NEG_ALTITUDE
      mNegAltitude = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) {    // DISTOX_EDIT_ALTITUDE
      mEditableHGeo = tryBooleanValue( hlp, k, v, bool(def[3]) );
    } else if ( k.equals( key[ 4 ] ) ) {    // DISTOX_FINE_LOCATION
      mFineLocation = tryIntValue(  hlp, k, v, def[4] );
      if ( mFineLocation < 0 ) { mFineLocation = 0; } else if ( mFineLocation > 600 ) { mFineLocation = 600; }
    } else if ( k.equals( key[ 5 ] ) ) {    // DISTOX_GEOPOINT_APP
      mGeoImportApp = Integer.parseInt( tryStringValue(  hlp, k, v, def[5] ) );
    } else {
      TDLog.e("missing LOCATION key: " + k );
    }
    // if ( ret != null ) hlp.update( k, ret );
    return null;
  }


  private static String updatePrefScreen( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref screen: " + k );
    String[] key = TDPrefKey.SCREEN;
    String[] def = TDPrefKey.SCREENdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_FIXED_THICKNESS
      try {
        float f = tryFloatValue( hlp, k, v, def[0] );
        if ( f >= 0.5f && f <= 10 && f != mFixedThickness ) {
          mFixedThickness = f;
          BrushManager.setStrokeWidths();
        }
       	else if ( f < 0.5f ) { f = 0.5f; ret = "0.5"; }
	else if ( f > 10f )  { f =  10f; ret = TDString.TEN; }
      } catch ( NumberFormatException e ) { ret = String.format(Locale.US, "%.2f", mFixedThickness ); }
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_STATION_SIZE
      try {
        setStationSize( Float.parseFloat( tryStringValue( hlp, k, v, def[1] ) ), true );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
      ret = String.format(Locale.US, "%.2f", mStationSize );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_DOT_RADIUS
      ret = setDotRadius( tryFloatValue( hlp, k, v, def[2] ) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_CLOSENESS
      ret = setSelectness( tryFloatValue( hlp, k, v, def[3] ) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_ERASENESS
      ret = setEraseness( tryFloatValue( hlp, k, v, def[4] ) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_MIN_SHIFT
      ret = setMinShift( tryIntValue(  hlp, k, v, def[5] ) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_POINTING
      ret = setPointingRadius( tryIntValue( hlp, k, v, def[6] ) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_SPLAY_ALPHA
      mSplayAlpha = tryIntValue( hlp, k, v, def[ 7] ); 
      if ( mSplayAlpha < 0 ) { mSplayAlpha = 0; ret = Float.toString( mSplayAlpha ); }
      if ( mSplayAlpha > 100 ) { mSplayAlpha = 100; ret = Float.toString( mSplayAlpha ); }
      BrushManager.setSplayAlpha( mSplayAlpha );
    } else {
      TDLog.e("missing SCREEN key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefLine( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref line: " + k );
    String[] key = TDPrefKey.LINE;
    String[] def = TDPrefKey.LINEdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_LINE_THICKNESS
      ret = setLineThickness( tryStringValue( hlp, k, v, def[0] ) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_LINE_UNITS
      try {
        setDrawingUnitLines( tryFloatValue( hlp, k, v, def[1] ) );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LINE_STYLE (choice)
      setLineStyleAndType( tryStringValue( hlp, k, v, def[2] ) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_LINE_SEGMENT
      ret = setLineSegment( tryIntValue(   hlp, k, v, def[3] ) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_LINE_CLOSE
      mLineClose = tryBooleanValue( hlp, k, v, bool(def[4]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_SLOPE_LSIDE
      ret = setSlopeLSide( tryIntValue( hlp, k, v, def[5] ) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_ARROW_LENGTH
      ret = setArrowLength( tryFloatValue( hlp, k, v, def[6] ) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_AUTO_SECTION_PT (bool)
      mAutoSectionPt = tryBooleanValue( hlp, k, v, bool(def[7]) );
    // } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_LINE_CONTINUE (choice)
    //   mContinueLine  = tryIntValue( hlp, k, v, def[7] );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_WITH_CONTINUE_LINE (bool)
      mWithLineJoin = tryBooleanValue(  hlp, k, v, bool(def[8]) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_AREA_BORDER (bool)
      mAreaBorder = tryBooleanValue( hlp, k, v, bool(def[9]) );
    } else {
      TDLog.e("missing LINE key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefPoint( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref point: " + k );
    String[] key = TDPrefKey.POINT;
    String[] def = TDPrefKey.POINTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNSCALED_POINTS (bool)
      mUnscaledPoints = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_DRAWING_UNIT 
      try {
        setDrawingUnitIcons( tryFloatValue( hlp, k, v, def[1] ) );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LABEL_SIZE
      try {
        setLabelSize( Float.parseFloat( tryStringValue( hlp, k, v, def[2] ) ), true );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
      ret = String.format(Locale.US, "%.2f", mLabelSize );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_SCALABLE_LABEL
      mScalableLabel = tryBooleanValue( hlp, k, v, bool(def[3]) );
    } else {
      TDLog.e("missing POINT key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  // AUTOWALLS
  // private static String updatePrefWalls( TDPrefHelper hlp, String k, String v )
  // {
  //   String ret = null;
  //   // TDLog.v("update pref walls: " + k );
  //   String[] key = TDPrefKey.WALLS;
  //   String[] def = TDPrefKey.WALLSdef;
  //   if ( k.equals( key[ 0 ] ) ) {        // DISTOX_WALLS_TYPE (choice)
  //     mWallsType = tryIntValue(hlp, k, v, def[0] );
  //   } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_WALLS_PLAN_THR
  //     mWallsPlanThr = tryFloatValue( hlp, k, v, def[1] );
  //     if ( mWallsPlanThr < 0 ) { mWallsPlanThr  =  0; ret = TDString.ZERO; }
  //     if ( mWallsPlanThr > 90 ) { mWallsPlanThr = 90; ret = TDString.NINETY; }
  //   } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_WALLS_EXTENDED_THR
  //     mWallsExtendedThr = tryFloatValue( hlp, k, v, def[2] );
  //     if ( mWallsExtendedThr < 0 ) { mWallsExtendedThr = 0; ret = TDString.ZERO; }
  //     if ( mWallsExtendedThr > 90 ) { mWallsExtendedThr = 90; ret = TDString.NINETY; }
  //   } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_WALLS_XCLOSE
  //     mWallsXClose = tryFloatValue( hlp, k, v, def[3] );
  //     if ( mWallsXClose < 0 ) { mWallsXClose = 0; ret = TDString.ZERO; }
  //   } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_WALLS_CONCAVE
  //     mWallsConcave = tryFloatValue( hlp, k, v, def[4] );
  //     if ( mWallsConcave < 0 ) { mWallsConcave = 0; ret = TDString.ZERO; }
  //   } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_WALLS_XSTEP
  //     mWallsXStep = tryFloatValue( hlp, k, v, def[5] );
  //     if ( mWallsXStep < 0 ) { mWallsXStep = 0; ret = TDString.ZERO; }
  //   } else {
  //     TDLog.e("missing WALLS key: " + k );
  //   }
  //   if ( ret != null ) hlp.update( k, ret );
  //   return ret;
  // }

  private static String updatePrefDraw( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref draw: " + k );
    String[] key = TDPrefKey.DRAW;
    String[] def = TDPrefKey.DRAWdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNSCALED_POINTS (bool)
      mUnscaledPoints = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_DRAWING_UNIT (choice)
      try {
        setDrawingUnitIcons( tryFloatValue( hlp, k, v, def[1] ) );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LABEL_SIZE
      try {
        setLabelSize( Float.parseFloat( tryStringValue( hlp, k, v, def[2] ) ), true );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
      ret = String.format(Locale.US, "%.2f", mLabelSize );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_LINE_THICKNESS
      ret = setLineThickness( tryStringValue( hlp, k, v, def[3] ) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_LINE_STYLE (choice)
      setLineStyleAndType( tryStringValue( hlp, k, v, def[4] ) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_LINE_CLOSE
      mLineClose = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_LINE_SEGMENT
      ret = setLineSegment( tryIntValue(   hlp, k, v, def[6] ) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_ARROW_LENGTH
      ret = setArrowLength( tryFloatValue( hlp, k, v, def[7] ) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_AUTO_SECTION_PT (bool)
      mAutoSectionPt = tryBooleanValue( hlp, k, v, bool(def[8]) );
    // } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_LINE_CONTINUE (choice)
    //   mContinueLine  = tryIntValue( hlp, k, v, def[8] );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_AREA_BORDER (bool)
      mAreaBorder = tryBooleanValue( hlp, k, v, bool(def[9]) );
    // } else if ( k.equals( key[ 10 ] ) ) { // DISTOX_REDUCE_ANGLE
    //   ret = setReduceAngle( tryFloatValue( hlp, k, v, def[10] ) );
    } else {
      TDLog.e("missing DRAW key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefErase( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref erase: " + k );
    String[] key = TDPrefKey.ERASE;
    String[] def = TDPrefKey.ERASEdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_CLOSENESS
      ret = setSelectness( tryFloatValue( hlp, k, v, def[0] ) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_ERASENESS
      ret = setEraseness( tryFloatValue( hlp, k, v, def[1] ) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_POINTING
      ret = setPointingRadius( tryIntValue(   hlp, k, v, def[2] ) );
    } else {
      TDLog.e("missing ERASE key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefEdit( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref edit: " + k );
    String[] key = TDPrefKey.EDIT;
    String[] def = TDPrefKey.EDITdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_DOT_RADIUS
      ret = setDotRadius( tryFloatValue( hlp, k, v, def[0] ) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_CLOSENESS
      ret = setSelectness( tryFloatValue( hlp, k, v, def[1] ) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_MIN_SHIFT
      ret = setMinShift( tryIntValue(  hlp, k, v, def[2] ) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_POINTING
      ret = setPointingRadius( tryIntValue(   hlp, k, v, def[3] ) );
    } else {
      TDLog.e("missing EDIT key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  // FIXME_SKETCH_3D 
  private static String updatePrefSketch( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref sketch: " + k );
    String[] key = TDPrefKey.SKETCH;
    String[] def = TDPrefKey.SKETCHdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_3D_SKETCH
      m3Dsketch = tryBooleanValue( hlp, k, v, bool(def[0]) );
      TDLog.e("3D sketch: " + m3Dsketch );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SKETCH_SPLAY_BUFFER
      mSplayBuffer = tryFloatValue( hlp, k, v, def[1] );
      TDLog.e("3D sketch buffer: " + mSplayBuffer );
    } else {
      TDLog.e("missing SKETCH key: " + k );
    }
    /*
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SKETCH_MODEL_TYPE (choice)
      mSketchModelType = tryIntValue(  hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // 0.5 meter // DISTOX_SKETCH_LINE_STEP
      mSketchSideSize = tryFloatValue( hlp, k, v, def[1] );
      if ( mSketchSideSize < 0.01f ) { mSketchSideSize = 0.01f; ret = "0.01"; }
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_BORDER_STEP
    //   mSketchBorderStep  = tryFloatValue( hlp, k, v, def[ ] );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_SECTION_STEP
    //   mSketchSectionStep = tryFloatValue( hlp, k, v, def[ ] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_DELTA_EXTRUDE
      mDeltaExtrude = tryFloatValue( hlp, k, v, def[2] );
      if ( mDeltaExtrude < 0.01f ) { mDeltaExtrude = 0.01f; ret = "0.01"; }
    // } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_COMPASS_READINGS
    //   mCompassReadings = tryIntValue( hlp, k, v, def[ ] );
    //   if ( mCompassReadings < 1 ) { mCompassReadings = 1; ret = TDString.ONE; }
    } else {
      TDLog.e("missing SKETCH key: " + k );
    }
    */
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }
  // END_SKETCH_3D 
 
  // NO_LOGS
  // @param k   key
  // @param v   value (either "true" of "false" for checkboxes)
  // private static String updatePrefLog( TDPrefHelper hlp, String k, String v )
  // {
  //   String[] key = TDPrefKey.LOG;
  //   if ( k.equals( key[0] ) ) { // DISTOX_LOG_STREAM
  //     hlp.update( k, v );
  //     TDLog.checkLogPreferences( hlp.getSharedPrefs(), k, v ); // FIXME_PREF
  //   } else {
  //     boolean def = k.equals( key[3] ); // DISTOX_LOG_ERR
  //     boolean b = tryBooleanValue( hlp, k, v, def );
  //     TDLog.checkLogPreferences( hlp.getSharedPrefs(), k, b ); // FIXME_PREF
  //   }
  //   return null;
  // }

  // -----------------------------------------------------------------------------

  private static void setLineStyleAndType( String style )
  {
    mLineStyle = LINE_STYLE_TWO; // default
    mLineType  = 1;
    if ( style.equals( TDString.ZERO ) ) {
      mLineStyle = LINE_STYLE_BEZIER;
      // mLineType  = 1;                 // already assigned
    } else if ( style.equals( TDString.ONE ) ) {
      mLineStyle = LINE_STYLE_ONE;
      // mLineType  = 1;                 // already assigned
    } else if ( style.equals( TDString.TWO ) ) {
      // mLineStyle = LINE_STYLE_TWO;    // already assigned
      mLineType  = 2;
    } else if ( style.equals( TDString.THREE ) ) {
      mLineStyle = LINE_STYLE_THREE;
      mLineType  = 3;
    } else if ( style.equals( TDString.FOUR ) ) {
      mLineStyle = LINE_STYLE_SIMPLIFIED;
      // mLineType  = 1;
    }
  }

  public static boolean isLineStyleComplex() 
  { return mLineStyle == LINE_STYLE_BEZIER || mLineStyle == LINE_STYLE_SIMPLIFIED; }

  public static boolean isLineStyleBezier() { return mLineStyle == LINE_STYLE_BEZIER; }
  public static boolean isLineStyleSimplified() { return mLineStyle == LINE_STYLE_SIMPLIFIED; }

  private static String setLineThickness( String str )
  {
    String ret = null;
    try {
      float f = Float.parseFloat( str );
      if ( f < 0.5f ) { f = 0.5f; ret = "0.5"; }
      else if ( f > 10 ) { f = 10; ret = "10.0"; }
      if ( f != mLineThickness ) {
        mLineThickness = f;
        BrushManager.reloadLineLibrary( TDInstance.getResources() );
      } 
    } catch ( NumberFormatException e ) { ret = String.format(Locale.US, "%.1f", mLineThickness); }
    return ret;
  }

  private static String setLineSegment( int val )
  {
    String ret = null;
    if ( val < 1 ) { val = 1; ret = TDString.ONE; }
    mLineSegment  = val;
    mLineSegment2 = mLineSegment * mLineSegment;
    return ret;
  }

  private static String setReduceAngle( float a )
  {
    String ret = null;
    if ( a < 0 )  { a =  0; ret = TDString.ZERO; }    
    if ( a > 90 ) { a = 90; ret = TDString.NINETY; }    
    mReduceAngle  = a;
    mReduceCosine = (float)Math.cos( mReduceAngle * TDMath.DEG2RAD );
    return ret;
  }

  private static String setLineAccuracy( float a )
  {
    String ret = null;
    if ( a < 0.1f )  { a = 0.1f; ret = "0.1"; }    
    mLineAccuracy = a;
    return ret;
  }

  private static String setLineCorner( float a )
  {
    String ret = null;
    if ( a < 0.1f )  { a = 0.1f; ret = "0.1"; }    
    mLineCorner = a;
    return ret;
  }

  private static String setWeedDistance( float a )
  {
    String ret = null;
    if ( a < 0.1f )  { a = 0.1f; ret = "0.1"; } 
    mWeedDistance = a;
    return ret;
  }

  private static String setWeedLength( float a )
  {
    String ret = null;
    if ( a < 0.1f )  { a = 0.1f; ret = "0.1"; }    
    mWeedLength = a;
    return ret;
  }

  private static String setWeedBuffer( float a )
  {
    String ret = null;
    if ( a < 0 )  { a = 0; ret = "0"; }    
    mWeedBuffer = a;
    return ret;
  }

  private static String setArrowLength( float len )
  {
    String ret = null;
    if ( len < 1 )  { len = 1;  ret = TDString.ONE; }
    if ( len > 40 ) { len = 40; ret = "40"; }
    mArrowLength = len;
    return ret;
  }

  // private static void setBackupsClear( boolean b ) // CLEAR_BACKUPS
  // {
  //   if ( mBackupsClear != b ) {
  //     mBackupsClear = b;
  //     TopoDroidApp.resetButtonBar();
  //     TopoDroidApp.setMenuAdapter();
  //   }
  // }

  private static void setPalettes( boolean b )
  {
    if ( mPalettes != b ) {
      mPalettes = b;
      // TopoDroidApp.resetButtonBar();
      // TopoDroidApp.setMenuAdapter(); // was in 6.0.33
      mMainFlag |= FLAG_MENU;
    }
  }

  private static void setActivityBooleans( SharedPreferences prefs, int level )
  {
    if ( level == TDLevel.mLevel ) return;

    if ( StationPolicy.policyDowngrade( level ) ) {
      setPreference( prefs, TDPrefKey.SURVEY[POLICY_INDEX], TDString.ONE );
    }
    TDLevel.setLevel( TDInstance.context, level );
    int policy = StationPolicy.policyUpgrade( level );
    if ( policy > 0 ) {
      setPreference( prefs, TDPrefKey.SURVEY[POLICY_INDEX], Integer.toString( policy ) );
    }
    // if ( ! TDLevel.overExpert ) {
    //   mMagAnomaly = false; // magnetic anomaly compensation requires level overExpert
    // }
    // TopoDroidApp.resetButtonBar(); // was in 6.0.33
    // TopoDroidApp.setMenuAdapter(); // was in 6.0.33
    mMainFlag |= FLAG_LEVEL;
    if ( TDPrefActivity.mPrefActivityAll != null ) TDPrefActivity.mPrefActivityAll.reloadPreferences();
  }

  private static void parseStationPolicy( TDPrefHelper hlp, String str ) 
  {
    int policy = StationPolicy.SURVEY_STATION_FOREWARD;
    try {
      policy = Integer.parseInt( str );
    } catch ( NumberFormatException e ) {
      policy = StationPolicy.SURVEY_STATION_FOREWARD;
    }
    if ( ! setStationPolicy( policy ) ) {
      // TDLog.v("preference is reset to the last saved policy " + StationPolicy.savedPolicy() );
      TDPrefHelper.update( TDPrefKey.SURVEY[POLICY_INDEX], Integer.toString( StationPolicy.savedPolicy() ) );
      if ( TDPrefActivity.mPrefActivitySurvey != null ) TDPrefActivity.mPrefActivitySurvey.reloadPreferences(); // FIXME_PREF
    } else {
      // TDLog.v("preference is set to the policy " + policy );
      TDPrefHelper.update( TDPrefKey.SURVEY[POLICY_INDEX], Integer.toString( policy ) );
    }
    // if ( ! mBacksightShot ) clearMagAnomaly( hlp.getSharedPrefs() );
    // TDLog.v("PARSE Policy " + policy + " saved " + StationPolicy.savedPolicy() );
    // return policy;
  }

  private static boolean setStationPolicy( int policy )
  {
    if ( ! StationPolicy.setPolicy( policy ) ) {
      if ( policy == StationPolicy.SURVEY_STATION_TOPOROBOT ) {
        TDToast.make( R.string.toporobot_warning ); // tester level
      } else if ( policy == StationPolicy.SURVEY_STATION_TRIPOD ) {
        TDToast.make( R.string.tripod_warning );    // advanced level
      // } else if ( policy == StationPolicy.SURVEY_STATION_BACKSIGHT ) {
      //   TDToast.make( R.string.backsight_warning );
      } else if ( policy == StationPolicy.SURVEY_STATION_ANOMALY ) {
        TDToast.make( R.string.anomaly_warning );   // expert level
      // } else {
        // nothing
      }
      return false;
    }
    // TDLog.v("set survey stations. policy " + policy );
    return true;
  }

  private static void setLocale( String locale, boolean load_symbols )
  {
    // TDLog.v("SETTING set locale <" + locale + ">" );
    TDLocale.setTheLocale( locale );
    Resources res = TDInstance.getResources();
    if ( load_symbols ) {
      BrushManager.reloadPointLibrary( TDInstance.context, res ); // reload symbols
      BrushManager.reloadLineLibrary( res );
      BrushManager.reloadAreaLibrary( res );
    }
    // TopoDroidApp.setMenuAdapter(); // was in 6.0.33
    TDPrefActivity.reloadPreferences();
    mMainFlag |= (FLAG_MENU | FLAG_LOCALE);
  }

  // void clearPreferences()
  // {
  //   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( this );
  //   SharedPreferences.Editor editor = sp.edit();
  //   editor.clear();
  //   editor.apply();
  // }

  /** change a string preference
   * @param sp    shared preferences
   * @param name  preference name
   * @param value preference value
   * @return true if successful
   */
  public static boolean setPreference( SharedPreferences sp, String name, String value )
  {
    TDLog.v("SETTING set pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putString( name, value );
    return TDandroid.applyEditor( editor );
  }

  /** change a boolean preference
   * @param sp    shared preferences
   * @param name  preference name
   * @param value preference value
   * @return true if successful
   */
  private static boolean setPreference( SharedPreferences sp, String name, boolean value )
  {
    TDLog.v("SETTING set b-pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putBoolean( name, value );
    return TDandroid.applyEditor( editor );
  }

  /** change a long preference
   * @param sp    shared preferences
   * @param name  preference name
   * @param value preference value
   * @return true if successful
   */
  public static boolean setPreference( SharedPreferences sp, String name, long value )
  {
    TDLog.v("SETTING set l-pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putLong( name, value );
    return TDandroid.applyEditor( editor );
  }

  private static void setPreference( Editor editor, String name, String value )  
  {
    TDLog.v("SETTING set s-pref " + name + " " + value );
    editor.putString( name, value );
  }
  private static void setPreference( Editor editor, String name, boolean value ) { editor.putBoolean( name, value ); }
  // private static void setPreference( Editor editor, String name, long value )    { editor.putLong( name, value ); }
  private static void setPreference( Editor editor, String name, int value )
  {
    TDLog.v("SETTING set i-pref " + name + " " + value );
    editor.putString( name, Integer.toString(value) );
  }
  private static void setPreference( Editor editor, String name, float value )   { editor.putString( name, Float.toString(value) ); }

  /** commit the changes that are in the editor
   * @param editor   preferences editor
   * @return true if successful
   */
  private static boolean commitEditor( Editor editor ) { return TDandroid.applyEditor( editor ); }

  /** @return the (long) value of a preference
   * @param sp         shared preferences
   * @param name       preference name
   * @param def_value  default preference value
   */
  public static long getLongPreference( SharedPreferences sp, String name, long def_value )
  {
    return sp.getLong( name, def_value ); 
  }

  // -----------------------------------------------------------------------
  //
  /** handle a request for the local manpages
   * @param man       language code
   * @param download  whether tp download the local man pages
   * @return true if the requested language has been handled
   *
   * IMPORTANT
   *    - the maximum number of user man index must agree with the number of entries in array.xml
   *    - the order on the url resource array must agree with that in array.xml
   */
  public static boolean handleLocalUserMan( /* Context my_app, */ String man, boolean download ) 
  {
    int idx = Integer.parseInt( man ); // no throw
    if ( idx > 0 && idx < 8 ) { 
      if ( download && TDandroid.checkInternet( TDInstance.context ) ) { // download user manual 
       	int[] res = { // url resource array
	         0, // english
	         R.string.user_man_es, // the order must agree with that in array.xml
	         R.string.user_man_it,
	         R.string.user_man_ru,
                 R.string.user_man_hu,
	         R.string.user_man_fr,
	         R.string.user_man_pt,
	         R.string.user_man_cn,
	       };
        String url = TDInstance.getResources().getString( res[idx] );
       	if ( url != null && url.length() > 0 ) {
          // TDLog.Log( TDLog.LOG_PREFS, "idx " + idx + " url " + url );
          // try do download the zip
          (new UserManDownload( /* my_app, */ url )).execute();
	}
      }
      return true;
    }
    return false;
  }

  // ============================================================================
  // export current settings
  private static char tf( boolean b ) { return b? 'T' : 'F'; }

  /** export settings to the file
   */
  public static boolean exportSettings( )
  {
    // TDLog.v("Setting exports settings");
    File file = TDFile.getSettingsFile(); // PRIVATE FILE
    try {
      FileWriter fw = new FileWriter( file, false ); // true = append
      PrintWriter pw = new PrintWriter( fw, true ); // true = auto-flush
      pw.printf(Locale.US, "TopoDroid v. %s %d\n", TDVersion.string(), TDVersion.code() );
      pw.printf(Locale.US, "Buttons Size %d %d\n", mSizeBtns, mSizeButtons);
      pw.printf(Locale.US, "Text Size %d \n", mTextSize);
      pw.printf(Locale.US, "Level %d \n", TDLevel.mLevel );
      pw.printf(Locale.US, "Keyboard %c no-cursor %c \n", tf(mKeyboard), tf(mNoCursor) );
      pw.printf(Locale.US, "Local man %c \n", tf(mLocalManPages) );
      pw.printf(Locale.US, "Palettes %c \n", tf(mPalettes) );
      pw.printf(Locale.US, "Orientation %d \n", mOrientation );

      pw.printf(Locale.US, "Export-format data %d, plot %d, auto-plot %d \n", mExportShotsFormat, mExportPlotFormat, mAutoExportPlotFormat );
      // pw.printf(Locale.US, "Auto-export %c data %d, plot %d \n", tf(mDataBackup), mExportShotsFormat, mExportPlotFormat );
      String eol = mSurvexEol.equals("\r\n")? eol = "\\r\\n" : "\\n";
      pw.printf(Locale.US, "Survex: eol \"%s\", splay %c, LRUD %c, EPSG %d\n", eol, tf(mSurvexSplay), tf(mSurvexLRUD), mSurvexEPSG );
      pw.printf(Locale.US, "Compass: swap_LR %c, prefix %c, splays %c \n", tf(mSwapLR), tf(mExportStationsPrefix), tf(mCompassSplays) );
      // pw.printf(Locale.US, "Walls: splays %c, wallsUD %d \n", tf(mWallsSplays), mWallsUD );
      pw.printf(Locale.US, "Walls: splays %c \n", tf(mWallsSplays) );
      pw.printf(Locale.US, "VisualTopo: splays %c, at-from %c, trox %c \n", tf(mVTopoSplays), tf(mVTopoLrudAtFrom), tf(mVTopoTrox) ); 
      pw.printf(Locale.US, "Ortho LRUD %c, angle %.2f, cos %.2f \n", tf(mOrthogonalLRUD), mOrthogonalLRUDAngle, mOrthogonalLRUDCosine );
      pw.printf(Locale.US, "Therion: config %c, maps %c, stations %c, splays %c, xvi %c, scale %d \n",
        tf(mTherionWithConfig), tf(mTherionMaps), tf(mAutoStations), tf(mTherionSplays), tf(mTherionXvi), mTherionScale );
      // pw.printf(Locale.US, "PNG scale %.2f, bg_color %d \n", mBitmapScale, (mBitmapBgcolor & 0xffffff) );
      pw.printf(Locale.US, "DXF: acad_version %d, blocks %c, spline %c layer %c\n", mAcadVersion, tf(mDxfBlocks), tf(mAcadSpline), tf(mAcadLayer) );
      pw.printf(Locale.US, "SVG: shot %.1f, label %.1f, %d, station %d, point %.1f, round-trip %c, grid %c %.1f, line %.1f, dir %c %.1f, splays %c, groups %c \n",
        mSvgShotStroke, mSvgLabelStroke, mSvgLabelSize, mSvgStationSize, mSvgPointStroke,
        tf(mSvgRoundTrip), tf(mSvgGrid), mSvgGridStroke, mSvgLineStroke, tf(mSvgLineDirection), mSvgLineDirStroke, tf(mSvgSplays), tf(mSvgGroups) ); // , mSvgProgram );
      pw.printf(Locale.US, "SHP: georef-plan %c \n", tf(mShpGeoref) );
      pw.printf(Locale.US, "GPX: single-track %c \n", tf(mGPXSingleTrack) );
      pw.printf(Locale.US, "KML: stations %c, splays %c \n", tf(mKmlStations), tf(mKmlSplays) );
      pw.printf(Locale.US, "CSV: raw %c, separator \'%c\' \n", tf(mCsvRaw), mCsvSeparator );

      pw.printf(Locale.US, "BT: check %d, autopair %c \n", mCheckBT, tf(mAutoPair) );
      pw.printf(Locale.US, "Socket: type %d, delay %d \n", mSockType, mConnectSocketDelay );
      pw.printf(Locale.US, "Connection mode %d Z6 %c, feedback %d unnamed %c\n", mConnectionMode, tf(mZ6Workaround), mConnectFeedback, tf(mUnnamedDevice) ); // BT_NONAME
      // pw.printf(Locale.US, "Communication autoreconnect %c, DistoX-B %c, retry %d, head/tail %c\n", tf(mAutoReconnect), tf(mSecondDistoX), mCommRetry, tf(mHeadTail) );
      pw.printf(Locale.US, "Communication DistoX-B %c, retry %d, head/tail %c \n", tf(mSecondDistoX), mCommRetry, tf(mHeadTail) );
      pw.printf(Locale.US, "Packet log %c Th2Edit %c WithDebug %c \n", tf(mPacketLog), tf(mTh2Edit), tf(mWithDebug) );
      pw.printf(Locale.US, "Wait: laser %d, shot %d, data %d, conn %d, command %d \n", mWaitLaser, mWaitShot, mWaitData, mWaitConn, mWaitCommand );

      pw.printf(Locale.US, "Calib groups %d, distance %.2f\n", mGroupBy, mGroupDistance);
      // pw.printf(Locale.US, "Calib algo %d, eps %f, iter %d\n", mCalibAlgo, mCalibEps, mCalibMaxIt );
      pw.printf(Locale.US, "Calib algo %d, eps %f, iter %d\n", 0, mCalibEps, mCalibMaxIt );
      pw.printf(Locale.US, "Calib shot download %c, raw data %d \n", tf(mCalibShotDownload), mRawCData );
      pw.printf(Locale.US, "Min_Algo alpha %.1f, beta %.1f, gamma %.1f, delta %.1f \n", mAlgoMinAlpha, mAlgoMinBeta, mAlgoMinGamma, mAlgoMinDelta );

      pw.printf(Locale.US, "Default Team \"%s\" names %d\n", mDefaultTeam, mTeamNames );
      pw.printf(Locale.US, "Midline check: attached %c, extend %c\n", tf(mCheckAttached), tf(mCheckExtend) );
      pw.printf(Locale.US, "Location: units %d, CRS \"%s\" NegAlt. %c FineLoc %d GeoApp %d EditAlt. %c\n", mUnitLocation, mCRS, tf(mNegAltitude), mFineLocation, mGeoImportApp, tf(mEditableHGeo) );
      pw.printf(Locale.US, "Shots: vthr %.1f, hthr %.1f \n", mVThreshold, mHThreshold );
      pw.printf(Locale.US, "Data: DistoX-backshot-swap %c, diving-mode %c \n", tf(mDistoXBackshot), tf(mDivingMode) );
      pw.printf(Locale.US, "Data input: backsight %c, prev/next %c\n", tf(mBacksightInput), tf(mPrevNext) );
      // pw.printf(Locale.US, "L/R extend %c BlunderShot %c\n", tf(mLRExtend), tf(mBlunderShot) );
      pw.printf(Locale.US, "L/R extend %c BlunderShot %c SplayStation %c SplayGroup %c\n", tf(mLRExtend), tf(mBlunderShot), tf(mSplayStation), tf(mSplayOnlyForward) ); 
      pw.printf(Locale.US, "U/D vertical %.1f L/R horizontal %.1f count $c\n", mLRUDvertical, mLRUDhorizontal, tf(mLRUDcount) );

      pw.printf(Locale.US, "Geek Import - data mode %d, zipped symbols %c category %c\n", mImportDatamode, tf( mZipWithSymbols), tf( mZipShareCategory ) ); //  tf(mExportTcsx) ); DISTOX_ZIP_SHARE_CATEGORY
      pw.printf(Locale.US, "Timer: wait %d, volume %d\n", mTimerWait, mBeepVolume );
      pw.printf(Locale.US, "Recent data %c, timeout %d\n", tf(mShotRecent), mRecentTimeout );
      pw.printf(Locale.US, "Leg: closeness %.2f, nr %d, triple-shot %d, max %.2f, min %.2f\n",
        mCloseDistance, mMinNrLegShots, mTripleShot, mMaxShotLength, mMinLegLength );
      pw.printf(Locale.US, "Splay: vthr %.1f, classes %c\n", mSplayVertThrs, tf(mSplayClasses) );
      pw.printf(Locale.US, "Stations: names %d, init \"%s\"\n", mStationNames, mInitStation );
      pw.printf(Locale.US, "Extend: thr %.1f, manual %c, frac %c\n", mExtendThr, tf(mAzimuthManual), tf(mExtendFrac) );
      pw.printf(Locale.US, "Loop: %d selective %.1f\n", mLoopClosure, mLoopThr );
      pw.printf(Locale.US, "Units: length %.2f [%s], angle %.2f [%s]\n", mUnitLength, mUnitLengthStr, mUnitAngle, mUnitAngleStr );
      pw.printf(Locale.US, "ThumbSize %d, SavedStations %c, LegonlyUpdate %c, WithAzimuth %c, WithSensors %c, Bedding %c \n", // TdManager %c\n",
        mThumbSize, tf(mSavedStations), tf(mLegOnlyUpdate), tf(mWithAzimuth), tf(mWithSensors), tf(mBedding) ); // , tf(mWithTdManager) );

      pw.printf(Locale.US, "Plot: zoom %d, drag %c, fix-origin %c, split %c, shift %c, levels %d, affine %c, stylus %.1f, slant-xsection %c, oblique %d\n", // STYLUS_MM
        mZoomCtrl, tf(mSideDrag), tf(mFixedOrigin), tf(mPlotSplit), tf(mPlotShift), mWithLevels, tf(mFullAffine), mStylusSize, tf(mSlantXSection), mObliqueMax );
      pw.printf(Locale.US, "Units: icon %.2f, line %.2f, grid %.2f, ruler %.2f\n", mUnitIcons, mUnitLines, mUnitGrid, mUnitMeasure );
      pw.printf(Locale.US, "Size: station %.1f, label %.1f, fixed %.1f line %.1f, scaleable_label %c\n",
        mStationSize, mLabelSize, mFixedThickness, mLineThickness, tf(mScalableLabel) );
      pw.printf(Locale.US, "Select: radius %.2f, pointing %d, shift %d, dot %.1f, multiple %c \n",
        mSelectness, mPointingRadius, mMinShift, mDotRadius, tf(mPathMultiselect) );
      pw.printf(Locale.US, "Erase: radius %.2f\n", mEraseness );
      // pw.printf(Locale.US, "Picker: type %d\n", mPickerType );
      pw.printf(Locale.US, "Point: unscaled %c\n", tf(mUnscaledPoints) );
      // pw.printf(Locale.US, "Line: style %d, type %d, segment %d, continue %d, arrow %.1f\n", mLineStyle, mLineType, mLineSegment, mContinueLine, mArrowLength );
      pw.printf(Locale.US, "Line: style %d, type %d, segment %d, continue 0, arrow %.1f, close %c, l-side %d\n", mLineStyle, mLineType, mLineSegment, mArrowLength, tf(mLineClose), mSlopeLSide );
      pw.printf(Locale.US, "Bezier: step %.2f, accuracy %.2f, corner %.2f\n", mBezierStep, mLineAccuracy, mLineCorner );
      pw.printf(Locale.US, "Weed: distance %.2f, length %.2f, buffer %.2f\n", mWeedDistance, mWeedLength, mWeedBuffer );
      pw.printf(Locale.US, "Area: border %c\n", tf(mAreaBorder) );
      // pw.printf(Locale.US, "Backup: nr %d, interval %d, clear %c\n", mBackupNumber, mBackupInterval, tf(mBackupsClear) ); // CLEAR_BACKUPS
      pw.printf(Locale.US, "Backup: nr %d, interval %d\n", mBackupNumber, mBackupInterval );
      pw.printf(Locale.US, "XSections: shared %c, auto-export %c, point %c\n", tf(mSharedXSections), tf(mAutoXSections), tf(mAutoSectionPt) );
      pw.printf(Locale.US, "Actions: snap %c, curve %c, straight %c %.1f\n", tf(mLineSnap), tf(mLineCurve), tf(mLineStraight), mReduceAngle );
      pw.printf(Locale.US, "Splay: alpha %d, color %d, splay-dash %d, vert %.1f, horiz %.1f, section %.1f color %d %d %d\n",
        mSplayAlpha, mDiscreteColors, mDashSplay, mVertSplay, mHorizSplay, mSectionSplay, mSplayDashColor, mSplayDotColor, mSplayLatestColor );
      pw.printf(Locale.US, "Accuracy: G %.2f, M %.2f, dip %.2f, sibling %.2f\n", mAccelerationThr, mMagneticThr, mDipThr, mSiblingThr );
      // pw.printf(Locale.US, "Sketch: type %d, size %.2f, extrude %.2f\n", mSketchModelType, mSketchSideSize, mDeltaExtrude );
      // AUTOWALLS
      // pw.printf(Locale.US, "Walls: type %d, thr P %.2f E %.2f, close %.2f, step %.2f, concave %.2f\n",
      //   mWallsType, mWallsPlanThr, mWallsExtendedThr, mWallsXClose, mWallsXStep, mWallsConcave );

      // TDLog.exportLogSettings( pw ); // NO_LOGS

      fw.close();
    } catch ( IOException e ) { 
      TDLog.e("failed to export settings");
      return false;
    }
    return true;
  }

  private static String getQuotedString( String line )
  {
    int from = line.indexOf( '"' );
    if ( from < 0 ) return null;
    int to = line.indexOf( '"', from+1 );
    if ( to < 0 ) return line.substring( from+1 );
    return line.substring( from+1, to );
  }

  private static String removeBrackets( String in )
  { 
    return in.substring( 1, in.length() -1 );
  }

  /** @return the string at the given index
   * @param vals    string array
   * @param idx     index
   */
  private static String getString( String[] vals, int idx ) { return ( idx < vals.length )? vals[idx] : null; }

  private static char getQuotedChar( String line )
  { 
    int from = line.indexOf( '\'' );
    if ( from < 0 ) return 0;
    return line.charAt( from+1 );
  }

  /** @return true if the string at the given index is "T"
   * @param vals    string array
   * @param idx     index
   */
  private static boolean getBoolean( String[] vals, int idx ) { return idx < vals.length && vals[idx].equals("T"); }

  /** @return the string at the given index as a float value, or the fail-over value
   * @param vals    string array
   * @param idx     index
   * @param fail    fail-over value
   */
  private static float getFloat( String[] vals, int idx, float fail )
  {
    if ( idx < vals.length ) {
      try { 
        return Float.parseFloat(vals[idx]);
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    }
    return fail;
  }

  /** @return the string at the given index as an integer value, or the fail-over value
   * @param vals    string array
   * @param idx     index
   * @param fail    fail-over value
   */
  private static int getInt( String[] vals, int idx, int fail )
  {
    if ( idx < vals.length ) {
      try { 
        return Integer.parseInt(vals[idx]);
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    }
    return fail;
  }

  /** import the settings from the settings file
   * @param prefs   shared preferences
   * @param all     whether to import all settings
   * @return true on success
   */
  public static boolean importSettings( SharedPreferences prefs, boolean all )
  {
    // TDLog.v("Setting import settings");
    Editor editor = prefs.edit();
    File file = TDFile.getSettingsFile(); // PRIVATE FILE
    try {
      FileReader fr = new FileReader( file ); // true = append
      BufferedReader br = new BufferedReader( fr ); // true = auto-flush
      String line;
      while ( ( line = br.readLine() ) != null ) {
        String[] vals = line.replaceAll(",", "").replaceAll("\\s+", " ").split(" ");
        // TDLog.v("Setting: " + line );
        if ( line.startsWith("TopoDroid" ) ) {
          if ( vals.length < 4 ) return false;
          if ( getInt( vals, 3, 0 ) < 500053 ) return false;
          continue;
        }
        if ( line.startsWith("Buttons Size" ) ) {
          if ( all ) {
            if ( vals.length > 2 ) {
              int size = getInt( vals, 2, -1 );
              if ( size < 0 ) {
                // TDLog.v("Setting button size " + size );
                return false;
              }
              setSizeButtons( size ); setPreference( editor, "DISTOX_SIZE_BUTTONS", mSizeBtns );
              // must run on UI thread
              // TopoDroidApp.resetButtonBar();
            } else {
              // TDLog.v("Setting btns vals len " + vals.length );
              return false;
            }
          }
          continue;
        }
        if ( line.startsWith("Text Size" ) ) {
          if ( all ) {
            if ( vals.length > 2 ) {
              int size = getInt( vals, 2, -1 );
              if ( size < 0 ) {
                // TDLog.v("Setting text size " + size );
                return false;
              }
              setTextSize( size ); setPreference( editor, "DISTOX_TEXT_SIZE", mTextSize );
              // TDLog.v("Setting text size " + mTextSize );
            } else {
              // TDLog.v("Setting text vals len " + vals.length );
              return false;
            }
          }
          continue;
        }
        if ( line.startsWith("Level") ) { 
          // if ( all ) { }
          continue;
        }
        if ( line.startsWith("Keyboard") ) {
          if ( all ) {
            if ( vals.length > 3 ) {
              mKeyboard = getBoolean( vals, 1 ); setPreference( editor, "DISTOX_MKEYBOARD", mKeyboard );
              mNoCursor = getBoolean( vals, 3 ); setPreference( editor, "DISTOX_NO_CURSOR", mNoCursor );
            }
          }
          continue;
        }
        if ( line.startsWith("Local man") ) {
          if ( all ) {
            // setPreference( editor, "DISTOX_LOCAL_MAN", mLocalManPages );
          }
          continue;
        }
        if ( line.startsWith("Palettes") ) {
          if ( vals.length > 1 ) {
            setPalettes( getBoolean( vals, 1 ) ); setPreference( editor, "DISTOX_PALETTES", mPalettes );
          }
          continue;
        }
        if ( line.startsWith("Orientation") ) {
          if ( all ) { 
            if ( vals.length > 1 ) {
              mOrientation = getInt( vals, 1, 0 ); setPreference( editor, "DISTOX_ORIENTATION", mOrientation );
              // must run on UI thread
              // TopoDroidApp.setScreenOrientation( );
              TDandroid.setScreenOrientation( TDPrefActivity.mPrefActivityAll );
            }
          }
          continue;
        }
        if ( line.startsWith("Export-Format") ) {
          if ( vals.length > 4 ) {
            mExportShotsFormat = getInt( vals, 2, 0 ); setPreference( editor, "DISTOX_EXPORT_SHOTS", mExportShotsFormat );
            mExportPlotFormat  = getInt( vals, 4, 0 ); setPreference( editor, "DISTOX_EXPORT_PLOT",  mExportPlotFormat );
            if ( vals.length > 6 ) {
              mAutoExportPlotFormat = getInt( vals, 4, 0 ); setPreference( editor, "DISTOX_AUTO_PLOT_EXPORT",  mAutoExportPlotFormat );
            }
          }
          continue;
        }
        // if ( line.startsWith("Auto-export") )
        //   if ( vals.length > 5 ) { 
        //     mDataBackup = getBoolean( vals, 1 ); setPreference( editor, "DISTOX_DATA_BACKUP", mDataBackup );
        //     mExportShotsFormat = getInt( vals, 3, 0 ); setPreference( editor, "DISTOX_EXPORT_SHOTS", mExportShotsFormat );
        //     mExportPlotFormat  = getInt( vals, 5, 0 ); setPreference( editor, "DISTOX_EXPORT_PLOT",  mExportPlotFormat );
        //   }
        //   continue;
        // }
        if ( line.startsWith("Survex") ) { 
          if ( vals.length > 6 ) {
            mSurvexEol   = getQuotedString( line ); 
            setPreference( editor, "DISTOX_SURVEX_EOL", ( mSurvexEol.equals("\n")? "LF" : "LFCR" ) );
            mSurvexSplay = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_SURVEX_SPLAY", mSurvexSplay );
            mSurvexLRUD  = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_SURVEX_LRUD",  mSurvexLRUD );
            if ( vals.length > 8 ) {
              mSurvexEPSG = getInt( vals, 8, 0 ); setPreference( editor, "DISTOX_SURVEX_EPSG", mSurvexEPSG );
            }
            // TDLog.v("Setting import survex settings " + mSurvexEol + " " + mSurvexSplay + " " + mSurvexLRUD );
          }
          continue;
        }
        if ( line.startsWith("Compass") ) {
          if ( vals.length > 6 ) {
            mSwapLR = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_SWAP_LR", mSwapLR );
            mExportStationsPrefix = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_STATION_PREFIX", mExportStationsPrefix );
            mCompassSplays = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_COMPASS_SPLAYS", mCompassSplays );
          }
          continue;
        }
        if ( line.startsWith("Walls") ) {
          if ( vals.length > 2 ) {
            mWallsSplays = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_WALLS_SPLAYS", mWallsSplays );
            // mWallsUD = getInt( vals, 4, 80 ); setPreference( editor, "DISTOX_WALLS_UD", mWallsUD );
          }
          continue;
        }
        if ( line.startsWith("VisualTopo") ) {
          if ( vals.length > 4 ) {
            mVTopoSplays     = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_VTOPO_SPLAYS", mVTopoSplays );
            mVTopoLrudAtFrom = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_VTOPO_LRUD",   mVTopoLrudAtFrom );
            if ( vals.length > 6 ) {
              mVTopoTrox     = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_VTOPO_TROX",   mVTopoTrox );
            }
          }
          continue;
        }
        if ( line.startsWith("Ortho") ) {
          if ( vals.length > 4 ) {
            mOrthogonalLRUDAngle = getFloat( vals, 4, 0.0f ); setPreference( editor, "DISTOX_ORTHO_LRUD", mOrthogonalLRUDAngle );
            mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
            mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
          }
          continue;
        }
        if ( line.startsWith("Therion") ) {
          if ( vals.length > 12 ) {
            mTherionWithConfig = getBoolean( vals, 2 );    setPreference( editor, "DISTOX_THERION_CONFIG", mTherionWithConfig );
            mTherionMaps   = getBoolean( vals, 4 );    setPreference( editor, "DISTOX_THERION_MAPS",   mTherionMaps );
            mAutoStations  = getBoolean( vals, 6);     setPreference( editor, "DISTOX_AUTO_STATIONS",  mAutoStations );
            mTherionSplays = getBoolean( vals, 8 );    setPreference( editor, "DISTOX_THERION_SPLAYS", mTherionSplays );
            mTherionXvi    = getBoolean( vals, 10 );   setPreference( editor, "DISTOX_TH2_XVI",        mTherionXvi );
            // the next line sets mTherionScale
            setExportScale( getInt( vals, 12, 100 ) ); 
            setPreference( editor, "DISTOX_TH2_SCALE",      mTherionScale );
            // TDLog.v("Setting import therion settings " + mTherionWithConfig + " " + mTherionMaps + " " + mTherionSplays );
          }
          continue;
        }
        if ( line.startsWith("PNG") ) {
          // if ( vals.length > 4 ) { // NO_PNG
          //   mBitmapScale   = getFloat( vals, 2, 1.5f ); setPreference( editor, "DISTOX_BITMAP_SCALE", mBitmapScale );
          //   mBitmapBgcolor = getInt( vals, 4, 0 );   setPreference( editor, "DISTOX_BITMAP_BGCOLOR", mBitmapBgcolor );
          //   mBitmapBgcolor |= 0xff000000;
          // }
          continue;
        }
        if ( line.startsWith("DXF") ) {
          if ( vals.length > 6 ) {
            mAcadVersion = getInt( vals, 2, 9 ); setPreference( editor, "DISTOX_ACAD_VERSION", mAcadVersion );
            mDxfBlocks   = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_DXF_BLOCKS", mDxfBlocks );
            mAcadSpline  = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_ACAD_SPLINE", mAcadSpline );
            if ( vals.length > 8 ) {
              mAcadLayer  = getBoolean( vals, 8 ); setPreference( editor, "DISTOX_ACAD_LAYER", mAcadLayer );
            } 
          }
          continue;
        }
        if ( line.startsWith("SVG") ) {
          if ( vals.length > 21 ) {
            mSvgShotStroke  = getFloat( vals, 2, 0.5f );    setPreference( editor, "DISTOX_SVG_SHOT_STROKE", mSvgShotStroke );
            mSvgLabelStroke = getFloat( vals, 4, 0.3f );    setPreference( editor, "DISTOX_SVG_LABEL_STROKE", mSvgLabelStroke );
            mSvgLabelSize   = getInt( vals, 5, 20 );        setPreference( editor, "DISTOX_SVG_LABEL_SIZE", mSvgLabelSize );
            mSvgStationSize = getInt( vals, 7, 20 );        setPreference( editor, "DISTOX_SVG_STATION_SIZE", mSvgStationSize );
            mSvgPointStroke = getFloat( vals, 9, 0.1f );    setPreference( editor, "DISTOX_SVG_POINT_STROKE", mSvgPointStroke );
            mSvgRoundTrip   = getBoolean( vals, 11 );       setPreference( editor, "DISTOX_SVG_ROUNDTRIP", mSvgRoundTrip );
            mSvgGrid        = getBoolean( vals, 13 );       setPreference( editor, "DISTOX_SVG_GRID", mSvgGrid );
            mSvgGridStroke  = getFloat( vals, 14, 0.5f );   setPreference( editor, "DISTOX_SVG_GRID_STROKE", mSvgGridStroke );
            mSvgLineStroke  = getFloat( vals, 16, 0.5f );   setPreference( editor, "DISTOX_SVG_LINE_STROKE", mSvgLineStroke );
            mSvgLineDirection = getBoolean( vals, 18 );     setPreference( editor, "DISTOX_SVG_LINE_DIR", mSvgLineDirection );
            mSvgLineDirStroke = getFloat( vals, 19, 6.0f ); setPreference( editor, "DISTOX_SVG_LINEDIR_STROKE", mSvgLineDirStroke );
            mSvgSplays = getBoolean( vals, 21 );            setPreference( editor, "DISTOX_SVG_SPLAYS", mSvgSplays );
            if ( vals.length > 23 ) {
              mSvgGroups = getBoolean( vals, 23 );            setPreference( editor, "DISTOX_SVG_GROUPS", mSvgGroups );
            }
            // if ( vals.length > 23 ) {
            //   mSvgProgram     = getInt( vals, 23, 0 );
            //   if ( mSvgProgram != 1 ) mSvgProgram = 0;  // either 1 (Illustrator) or 0 (Inkscape) 
            //   setPreference( editor, "DISTOX_SVG_PROGRAM", mSvgProgram );
            //   setExportScale( mTherionScale );
            // }
          }
          continue;
        }
        if ( line.startsWith("SHP") ) {
          if ( vals.length > 2 ) {
            mShpGeoref = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_SHP_GEOREF", mShpGeoref );
          }
          continue;
        }
        if ( line.startsWith("GPX") ) {
          if ( vals.length > 2 ) {
            mGPXSingleTrack = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_GPX_SINGLE_TRACK", mGPXSingleTrack );
          }
          continue;
        }
        if ( line.startsWith("KML") ) {
          if ( vals.length > 4 ) {
            mKmlStations = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_KML_STATIONS", mKmlStations );
            mKmlSplays   = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_KML_SPLAYS",   mKmlSplays );
          }
          continue;
        }
        if ( line.startsWith("CSV") ) {
          if ( vals.length > 4 ) {
            mCsvRaw = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_CSV_RAW", mCsvRaw );
            mCsvSeparator = getQuotedChar( line ); 
            // TDLog.v("Setting csv separator <" + mCsvSeparator + ">" );
            int sep = ( mCsvSeparator == CSV_COMMA )? 0 : ( mCsvSeparator == CSV_PIPE )? 1 : 2; // CSV_TAB
            setPreference( editor, "DISTOX_CSV_SEP", sep );
          }
          continue;
        }
        if ( line.startsWith("BT") ) {
          if ( all ) {
            if ( vals.length > 4 ) {
              mCheckBT  = getInt( vals, 2, 1 ); setPreference( editor, "DISTOX_BLUETOOTH", mCheckBT );
              mAutoPair = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_AUTO_PAIR", mAutoPair );
            }
          }
          continue;
        }
        if ( line.startsWith("Socket") ) {
          if ( all ) {
            if ( vals.length > 5 ) {
              // mDefaultSockStrType = getQuotedString( line ); setPreference( editor, DISTOX_ );
              mSockType = getInt( vals, 2, 0 ); setPreference( editor, "DISTOX_SOCKET_TYPE", mSockType );
              mConnectSocketDelay = getInt( vals, 4, 0 ); setPreference( editor, "DISTOX_SOCKET_DELAY", mConnectSocketDelay );
            }
          }
          continue;
        }
        if ( line.startsWith("Connection") ) {
          if ( all ) {
            if ( vals.length > 6 ) {
              mConnectionMode  = getInt( vals, 2, 0 );     setPreference( editor, "DISTOX_CONN_MODE", mConnectionMode );
              // mZ6Workaround    = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_Z6_WORKAROUND", mZ6Workaround );
              mConnectFeedback = getInt( vals, 6, 0 );     setPreference( editor, "DISTOX_CONNECT_FEEDBACK", mConnectFeedback );
              if ( vals.length > 8 ) { // BT_NONAME
                mUnnamedDevice = getBoolean( vals, 8 ); setPreference( editor, "DISTOX_UNNAMED_DEVICE", mUnnamedDevice );
              }
            }
          }
          continue;
        }
        if ( line.startsWith("Communication") ) {
          if ( all ) {
            if ( vals.length > 6 ) {
              // mAutoReconnect = getBoolean( vals, 2 );  setPreference( editor, "DISTOX_AUTO_RECONNECT", mAutoReconnect );
              mSecondDistoX  = getBoolean( vals, 2 );  setPreference( editor, "DISTOX_SECOND_DISTOX", mSecondDistoX );
              mCommRetry     = getInt( vals, 4, 1 );      setPreference( editor, "DISTOX_COMM_RETRY", mCommRetry );
              mHeadTail      = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_HEAD_TAIL",  mHeadTail );
            }
          }
          continue;
        }
        if ( line.startsWith("Packet log") ) {
          if ( all ) {
            if ( vals.length > 2 ) {
              mPacketLog = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_PACKET_LOGGER", mPacketLog );
            }
            if ( vals.length > 4 ) {
              mTh2Edit = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_TH2_EDIT", mTh2Edit );
            }
            // if ( vals.length > 6 ) { // WITH_DEBUG is not importable
            //   mWithDebug = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_WITH_DEBUG", mWithDebug );
            // }
          }
          continue;
        }
        if ( line.startsWith("Wait") ) {
          if ( all ) {
            if ( vals.length > 8 ) {
              mWaitLaser   = getInt( vals, 2, 250 );  setPreference( editor, "DISTOX_WAIT_LASER", mWaitLaser );
              mWaitShot    = getInt( vals, 4, 500 );  setPreference( editor, "DISTOX_WAIT_SHOT",  mWaitShot );
              mWaitData    = getInt( vals, 6, 1000 );  setPreference( editor, "DISTOX_WAIT_DATA",  mWaitData );
              mWaitConn    = getInt( vals, 8, 2000);  setPreference( editor, "DISTOX_WAIT_CONN",  mWaitConn );
              // mWaitCommand = getInt( vals, 10 ); setPreference( editor, "DISTOX_WAIT_COMMAND", mWaitCommand );
            }
          }
          continue;
        }
        if ( line.startsWith("Calib groups") ) {
          if ( vals.length > 4 ) {
            mGroupBy       = getInt( vals, 2, 1 ); setPreference( editor, "DISTOX_GROUP_BY", mGroupBy );
            mGroupDistance = getFloat( vals, 4, 40 ); setPreference( editor, "DISTOX_GROUP_DISTANCE", mGroupDistance );
          }
          continue;
        }
        if ( line.startsWith("Calib algo") ) {
          if ( vals.length > 6 ) {
            // mCalibAlgo  = getInt( vals, 2, 0 );   setPreference( editor, "DISTOX_CALIB_ALGO", mCalibAlgo );
            mCalibEps   = getFloat( vals, 4, 0.000001f ); setPreference( editor, "DISTOX_CALIB_EPS", mCalibEps );
            mCalibMaxIt = getInt( vals, 6, 200 );   setPreference( editor, "DISTOX_CALIB_MAX_IT", mCalibMaxIt );
          }
          continue;
        }
        if ( line.startsWith("Calib shot") ) {
          if ( all ) {
            if ( vals.length > 6 ) {
              mCalibShotDownload = getBoolean( vals, 3 ); setPreference( editor, "DISTOX_CALIB_SHOT_DOWNLOAD", mCalibShotDownload );
              mRawCData = getInt( vals, 6, 0 );              setPreference( editor, "DISTOX_RAW_CDATA", mRawCData );
            }
          }
          continue;
        }
        if ( line.startsWith("Min_Algo") ) {
          // if ( vals.length > 8 ) {
          //   mAlgoMinAlpha = getFloat( vals, 2, 0.1f ); setPreference( editor, "DISTOX_MIN_ALPHA", mAlgoMinAlpha );
          //   mAlgoMinBeta  = getFloat( vals, 4, 4.0f ); setPreference( editor, "DISTOX_MIN_BETA",  mAlgoMinBeta );
          //   mAlgoMinGamma = getFloat( vals, 6, 1.0f ); setPreference( editor, "DISTOX_MIN_GAMMA", mAlgoMinGamma );
          //   mAlgoMinDelta = getFloat( vals, 8, 1.0f ); setPreference( editor, "DISTOX_MIN_DELTA", mAlgoMinDelta );
          // }
          continue;
        }
        if ( line.startsWith("Default Team") ) {
          if ( all ) {
            if ( vals.length > 2 ) {
              mDefaultTeam = getQuotedString( line ); setPreference( editor, "DISTOX_TEAM", mDefaultTeam );
              int pos = line.lastIndexOf( "\" names " );
              if ( pos + 8 < line.length() ) try { // "... names %c" 
                String extra = line.substring( pos+8 );
                mTeamNames = Integer.parseInt( extra ); setPreference( editor, "DISTOX_TEAM_DIALOG",   mTeamNames );
              } catch ( NumberFormatException e ) {  }
            }
          }
          continue;
        }
        if ( line.startsWith("Midline check") ) {
          if ( all ) {
            if ( vals.length > 5 ) {
              mCheckAttached = getBoolean( vals, 3 ); setPreference( editor, "DISTOX_CHECK_ATTACHED", mCheckAttached );
              mCheckExtend   = getBoolean( vals, 5 ); setPreference( editor, "DISTOX_CHECK_EXTEND",   mCheckExtend );
            }
          }
          continue;
        }
        if ( line.startsWith("Location") ) {
          if ( vals.length > 4 ) {
            mUnitLocation = getInt( vals, 2, 0 );
            setPreference( editor, "DISTOX_UNIT_LOCATION", ( mUnitLocation == TDUtil.DDMMSS ? "ddmmss" : "degrees" ) );
            mCRS = getQuotedString( line ); 
            // TDLog.v("Setting crs <" + mCRS + ">" );
            setPreference( editor, "DISTOX_CRS", mCRS );
            if ( vals.length > 6 ) {
              mNegAltitude = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_NEG_ALTITUDE",   mNegAltitude );
              if ( vals.length > 8 ) {
                mFineLocation = getInt( vals, 8, 60 ); setPreference( editor, "DISTOX_FINE_LOCATION", mFineLocation );
                if ( vals.length > 10 ) {
                  mGeoImportApp = getInt( vals, 10, 0 ); setPreference( editor, "DISTOX_GEOPOINT_APP", mGeoImportApp );
                  if ( vals.length > 12 ) {
                    mEditableHGeo = getBoolean( vals, 12 ); setPreference( editor, "DISTOX_EDIT_ALTITUDE", mEditableHGeo );
                  }
                }
              }
            }
          }
          continue;
        }
        if ( line.startsWith("Shots") ) {
          if ( vals.length > 4 ) {
            mVThreshold = getFloat( vals, 2, 80.0f ); setPreference( editor, "DISTOX_VTHRESHOLD", mVThreshold );
            mHThreshold = getFloat( vals, 4, 70.0f ); setPreference( editor, "DISTOX_HTHRESHOLD", mHThreshold );
          }
          continue;
        }
        if ( line.startsWith("Data:") ) {
          if ( all ) {
            if ( vals.length > 4 ) {
              mDistoXBackshot = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_BACKSHOT", mDistoXBackshot );
              mDivingMode     = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_DIVING_MODE", mDivingMode );
            }
          }
          continue;
        }
        if ( line.startsWith("Data input") ) {
          if ( vals.length > 5 ) {
            mBacksightInput = getBoolean( vals, 3 ); setPreference( editor, "DISTOX_BACKSIGHT", mBacksightInput );
            mPrevNext       = getBoolean( vals, 5 ); setPreference( editor, "DISTOX_PREV_NEXT", mPrevNext );
          }
          continue;
        }
        if ( line.startsWith("L/R extend") ) {
          if ( vals.length > 2 ) {
            mLRExtend = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_SPLAY_EXTEND", mLRExtend );
          }
          if ( vals.length > 4 ) {
            mBlunderShot = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_BLUNDER_SHOT", mBlunderShot );
          }
          if ( vals.length > 6 ) {
            mSplayStation = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_SPLAY_STATION", mSplayStation );
          }
          if ( vals.length > 8 ) {
            mSplayOnlyForward = getBoolean( vals, 8 ); setPreference( editor, "DISTOX_SPLAY_GROUP", mSplayOnlyForward );
          }
          continue;
        }
        if ( line.startsWith("U/D") ) {
          if ( vals.length > 4 ) {
            mLRUDvertical   = getFloat( vals, 2, 0.0f ); setPreference( editor, "DISTOX_LRUD_VERTICAL",   mLRUDvertical );
            mLRUDhorizontal = getFloat( vals, 4, 90.0f ); setPreference( editor, "DISTOX_LRUD_HORIZONTAL", mLRUDhorizontal );
            if ( vals.length > 6 ) {
             mLRUDcount = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_LRUD_COUNT", mLRUDcount );
            }
          }
          continue;
        }
        if ( line.startsWith("Geek Import") ) {
          if ( vals.length > 8 ) {
            mImportDatamode = getInt( vals, 5, 0 ); setPreference( editor, "DISTOX_IMPORT_DATAMODE", mImportDatamode );
            mZipWithSymbols = getBoolean( vals, 8 ); setPreference( editor, "DISTOX_ZIP_WITH_SYMBOLS", mZipWithSymbols );
            if ( vals.length > 10 ) {
              mZipShareCategory = getBoolean( vals, 10 ); setPreference( editor, "DISTOX_ZIP_SHARE_CATEGORY", mZipShareCategory ); // DISTOX_ZIP_SHARE_CATEGORY
            }
          }
          continue;
        }
        if ( line.startsWith("Timer") ) {
          if ( all ) {
            if ( vals.length > 4 ) {
              mTimerWait  = getInt( vals, 2, 10 ); setPreference( editor, "DISTOX_SHOT_TIMER",  mTimerWait );
              setBeepVolume( getInt( vals, 4, 50 ) ); setPreference( editor, "DISTOX_BEEP_VOLUME", mBeepVolume );
            }
          }
          continue;
        }
        if ( line.startsWith("Recent data") ) {
          if ( all ) {
            if ( vals.length > 4 ) {
              mShotRecent = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_RECENT_SHOT",    mShotRecent );
              mRecentTimeout = getInt( vals, 4, 30 );  setPreference( editor, "DISTOX_RECENT_TIMEOUT", mRecentTimeout );
            }
          }
          continue;
        }
        if ( line.startsWith("Leg:") ) {
          if ( vals.length > 10 ) {
            mCloseDistance = getFloat( vals, 2, 0.05f ); setPreference( editor, "DISTOX_CLOSE_DISTANCE", mCloseDistance );
            mMinNrLegShots = getInt( vals, 4, 3 );       setPreference( editor, "DISTOX_LEG_SHOTS", mMinNrLegShots );
            mTripleShot    = getInt( vals, 6, 0 );       setPreference( editor, "DISTOX_LEG_FEEDBACK", mTripleShot );
            mMaxShotLength = getFloat( vals, 8, 50.0f ); setPreference( editor, "DISTOX_MAX_SHOT_LENGTH", mMaxShotLength );
            mMinLegLength  = getFloat( vals, 10, 0.0f ); setPreference( editor, "DISTOX_MIN_LEG_LENGTH", mMinLegLength );
          }
          continue;
        }
        if ( line.startsWith("Splay: vtr") ) {
          if ( vals.length > 4 ) {
            mSplayVertThrs = getFloat( vals, 2, 80.0f ); setPreference( editor, "DISTOX_SPLAY_VERT_THRS", mSplayVertThrs );
            mSplayClasses  = getBoolean( vals, 4 );      setPreference( editor, "DISTOX_SPLAY_CLASSES", mSplayClasses );
            // mSplayAsDot    = getBoolean( vals, 6 );      setPreference( editor, "DISTOX_SPLAY_AS_DOT", mSplayAsDot );
          }
          continue;
        }
        if ( line.startsWith("Stations:") ) {
          if ( vals.length > 2 ) {
            mStationNames = getInt( vals, 2, 0 ); setPreference( editor, "DISTOX_STATION_NAMES", mStationNames );
            mInitStation  = getQuotedString( line ); if ( mInitStation.length() > 0 ) setPreference( editor, "DISTOX_INIT_STATION", mInitStation );
            // TDLog.v("Setting init station <" + mInitStation + ">" );
          }
          continue;
        }
        if ( line.startsWith("Extend:") ) {
          if ( vals.length > 6 ) {
            mExtendThr     = getFloat( vals, 2, 10.0f );   setPreference( editor, "DISTOX_EXTEND_THR2", mExtendThr );
            mAzimuthManual = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_AZIMUTH_MANUAL", mAzimuthManual );
            mExtendFrac    = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_EXTEND_FRAC", mExtendFrac );
          }
          continue;
        }
        if ( line.startsWith("Loop") ) {
          if ( vals.length > 1 ) {
            mLoopClosure = getInt( vals, 1, 0 ); setPreference( editor, "DISTOX_LOOP_CLOSURE_VALUE", mLoopClosure );
          }
          if ( vals.length > 3 ) {
            mLoopThr = getFloat( vals, 3, 1.0f ); setPreference( editor, "DISTOX_LOOP_THR", mLoopThr );
          }
          continue;
        }
        if ( line.startsWith("Units: length") ) {
          if ( vals.length > 6 ) {
            mUnitLength    = getFloat( vals, 2, 1.0f ); 
            mUnitLengthStr = removeBrackets( getString( vals, 3 ) ); 
            mUnitAngle     = getFloat( vals, 5, 1.0f ); 
            mUnitAngleStr  = removeBrackets( getString( vals, 6 ) ); 
            setPreference( editor, "DISTOX_UNIT_LENGTH", ( mUnitLength > 0.99f ? "meters" : "feet" ) );
            setPreference( editor, "DISTOX_UNIT_ANGLE",  ( mUnitAngle > 0.99f ?  "degrees" : "grads" ) );
          }
          continue;
        }
        if ( line.startsWith("ThumbSize") ) {
          if ( vals.length > 11 ) {
            mThumbSize     = getInt( vals, 1, 200 ); setPreference( editor, "DISTOX_THUMBNAIL", mThumbSize );
            mSavedStations = getBoolean( vals, 3 );  setPreference( editor, "DISTOX_SAVED_STATIONS", mSavedStations );
            mLegOnlyUpdate = getBoolean( vals, 5 );  setPreference( editor, "DISTOX_LEGONLY_UPDATE", mLegOnlyUpdate );
            mWithAzimuth   = getBoolean( vals, 7 );  setPreference( editor, "DISTOX_ANDROID_AZIMUTH", mWithAzimuth );
            mWithSensors   = getBoolean( vals, 9 );  setPreference( editor, "DISTOX_WITH_SENSORS", mWithSensors );
            mBedding       = getBoolean( vals, 11 ); setPreference( editor, "DISTOX_BEDDING", mBedding );
          }
          continue;
        }
        if ( line.startsWith("Plot: zoom") ) {
          if ( vals.length > 16 ) {
            if ( all ) {
              mZoomCtrl = getInt( vals, 2, 1 );
              setZoomControls( vals[2], TDandroid.checkMultitouch( TDInstance.context ) );
              setPreference( editor, "DISTOX_ZOOM_CTRL", mZoomCtrl );
              mSideDrag = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_SIDE_DRAG", mSideDrag );
            }
            mFixedOrigin = getBoolean( vals, 6 );  setPreference( editor, "DISTOX_FIXED_ORIGIN", mFixedOrigin );
            mPlotSplit   = getBoolean( vals, 8 );  setPreference( editor, "DISTOX_PLOT_SPLIT",   mPlotSplit );
            mPlotShift   = getBoolean( vals, 10 ); setPreference( editor, "DISTOX_PLOT_SHIFT",   mPlotShift );
            mWithLevels  = getInt( vals, 12, 0 );  setPreference( editor, "DISTOX_WITH_LEVELS",  mWithLevels );
            mFullAffine  = getBoolean( vals, 14 ); setPreference( editor, "DISTOX_FULL_AFFINE",  mFullAffine );
            setStylusSize( getFloat( vals, 16, 0 ) ); setPreference( editor, "DISTOX_STYLUS_SIZE", mStylusSize ); // STYLUS_MM
            if ( vals.length > 18 ) {
              mSlantXSection = getBoolean( vals, 18 ); setPreference( editor, "DISTOX_SLANT_XSECTION", mSlantXSection );
              if ( vals.length > 20 ) {
                mObliqueMax = getInt( vals, 20, 0 ); setPreference( editor, "DISTOX_OBLIQUE_PROJECTED", mObliqueMax );
                // TODO mLineEnds
              }
            }
          }
          continue;
        }
        if ( line.startsWith("Units: icon") ) {
          if ( all ) {
            if ( vals.length > 8 ) {
              setDrawingUnitIcons( getFloat( vals, 2, 1.2f ) ); setPreference( editor, "DISTOX_DRAWING_UNIT", mUnitIcons );
              setDrawingUnitLines( getFloat( vals, 4, 1.4f ) ); setPreference( editor, "DISTOX_LINE_UNITS", mUnitLines );
              mUnitGrid  = getFloat( vals, 6, 1.0f );    setPreference( editor, "DISTOX_UNIT_GRID", mUnitGrid );
              mUnitMeasure = getFloat( vals, 8, -1.0f ); setPreference( editor, "DISTOX_UNIT_MEASURE", mUnitMeasure );
            }
          }
          continue;
        }
        if ( line.startsWith("Size: station") ) {
          if ( all ) {
            if ( vals.length > 8 ) {
              mStationSize    = getFloat( vals, 2, 20.0f ); setPreference( editor, "DISTOX_STATION_SIZE", mStationSize );
              mLabelSize      = getFloat( vals, 4, 24.0f ); setPreference( editor, "DISTOX_LABEL_SIZE", mLabelSize );
              mFixedThickness = getFloat( vals, 6, 1.0f );  setPreference( editor, "DISTOX_FIXED_THICKNESS", mFixedThickness );
              mLineThickness  = getFloat( vals, 8, 1.0f );  setPreference( editor, "DISTOX_LINE_THICKNESS", mLineThickness );
              if ( vals.length > 10 ) {
                mScalableLabel = getBoolean( vals, 10 ); setPreference( editor, "DISTOX_SCALABLE_LABEL", mScalableLabel );
              }
            }
          }
          continue;
        }
        if ( line.startsWith("Select:") ) {
          if ( all ) {
            if ( vals.length > 10 ) {
              mSelectness = getFloat( vals, 2, 24.0f );  setPreference( editor, "DISTOX_CLOSENESS", mSelectness );
              mPointingRadius = getInt( vals, 4, 24 );   setPreference( editor, "DISTOX_POINTING", mPointingRadius );
              mMinShift = getInt( vals, 6, 60 );         setPreference( editor, "DISTOX_MIN_SHIFT", mMinShift );
              mDotRadius = getFloat( vals, 8, 5.0f );    setPreference( editor, "DISTOX_DOT_RADIUS", mDotRadius );
              mPathMultiselect = getBoolean( vals, 10 ); setPreference( editor, "DISTOX_PATH_MULTISELECT", mPathMultiselect );
            }
          }
          continue;
        }
        if ( line.startsWith("Erase:") ) {
          if ( all ) {
            if ( vals.length > 2 ) {
              mEraseness = getFloat( vals, 2, 36.0f ); setPreference( editor, "DISTOX_ERASENESS", mEraseness );
            }
          }
          continue;
        }
        // if ( line.startsWith("Picker:") ) {
        //   if ( all ) {
        //     if ( vals.length > 2 ) {
        //       mPickerType = getInt( vals, 2, 1 ); setPreference( editor, "DISTOX_PICKER_TYPE", mPickerType );
        //     }
        //   }
        //   continue;
        // }
        if ( line.startsWith("Point:") ) {
          if ( all ) {
            if ( vals.length > 2 ) {
              mUnscaledPoints = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_UNSCALED_POINTS", mUnscaledPoints );
            }
          }
          continue;
        }
        if ( line.startsWith("Line: style") ) {
          if ( vals.length > 10 ) {
            setLineStyleAndType( vals[2] ); 
            setPreference( editor, "DISTOX_LINE_STYLE", mLineStyle );
            // mLineType     = getInt( vals, 4 ); setPreference( editor, DISTOX_L );
            setLineSegment( getInt( vals, 6, 10 ) ); 
            setPreference( editor, "DISTOX_LINE_SEGMENT", mLineSegment );
            // mContinueLine = getInt( vals, 8, 0 );        setPreference( editor, "DISTOX_LINE_CONTINUE", mContinueLine );
            mArrowLength  = getFloat( vals, 10, 10.0f ); setPreference( editor, "DISTOX_ARROW_LENGTH", mArrowLength );
            if ( vals.length > 12 ) {
              mLineClose = getBoolean( vals, 12 ); setPreference( editor, "DISTOX_LINE_CLOSE", mLineClose );
            }
            if ( vals.length > 14 ) {
              mSlopeLSide = getInt( vals, 12, 20 ); setPreference( editor, "DISTOX_SLOPE_LSIDE", mSlopeLSide );
            }
          }
          continue;
        }
        if ( line.startsWith("Bezier: step") ) {
          if ( vals.length > 6 ) {
            mBezierStep   = getFloat( vals, 2, 0.2f );  setPreference( editor, "DISTOX_BEZIER_STEP", mBezierStep );
            mLineAccuracy = getFloat( vals, 4, 1.0f );  setPreference( editor, "DISTOX_LINE_ACCURACY", mLineAccuracy );
            mLineCorner   = getFloat( vals, 6, 20.0f ); setPreference( editor, "DISTOX_LINE_CORNER", mLineCorner );
          }
          continue;
        }
        if ( line.startsWith("Weed:") ) {
          if ( vals.length > 6 ) {
            mWeedDistance = getFloat( vals, 2, 0.5f );  setPreference( editor, "DISTOX_WEED_DISTANCE", mWeedDistance );
            mWeedLength   = getFloat( vals, 4, 2.0f );  setPreference( editor, "DISTOX_WEED_LENGTH", mWeedLength );
            mWeedBuffer   = getFloat( vals, 6, 10.0f ); setPreference( editor, "DISTOX_WEED_BUFFER", mWeedBuffer );
          }
          continue;
        }
        if ( line.startsWith("Area:") ) {
          if ( vals.length > 2 ) {
            mAreaBorder = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_AREA_BORDER", mAreaBorder );
          }
          continue;
        }
        if ( line.startsWith("Backup:") ) {
          if ( vals.length > 6 ) {
            mBackupNumber = getInt( vals, 2, 5 );   setPreference( editor, "DISTOX_BACKUP_NUMBER", mBackupNumber );
            mBackupInterval = getInt( vals, 4, 60 ); setPreference( editor, "DISTOX_BACKUP_INTERVAL", mBackupInterval );
            // if ( all ) mBackupsClear = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_BACKUPS_CLEAR", mBackupsClear ); // CLEAR_BACKUPS
          }
          continue;
        }
        if ( line.startsWith("XSections:") ) {
          if ( vals.length > 6 ) {
            mSharedXSections = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_SHARED_XSECTIONS", mSharedXSections );
            mAutoXSections   = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_AUTO_XSECTIONS", mAutoXSections );
            mAutoSectionPt   = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_AUTO_SECTION_PT", mAutoSectionPt );
          }
          continue;
        }
        if ( line.startsWith("Actions") ) {
          if ( vals.length > 7 ) {
            mLineSnap     = getBoolean( vals, 2 ); setPreference( editor, "DISTOX_LINE_SNAP", mLineSnap );
            mLineCurve    = getBoolean( vals, 4 ); setPreference( editor, "DISTOX_LINE_CURVE", mLineCurve );
            mLineStraight = getBoolean( vals, 6 ); setPreference( editor, "DISTOX_LINE_STRAIGHT", mLineStraight );
            setReduceAngle( getFloat( vals, 7, 45.0f ) ); setPreference( editor, "DISTOX_REDUCE_ANGLE", mReduceAngle );
          }
          continue;
        }
        if ( line.startsWith("Splay: alpha") ) {
          if ( all ) {
            if ( vals.length > 12 ) {
              mSplayAlpha = getInt( vals, 2, 80 );  setPreference( editor, "DISTOX_SPLAY_ALPHA", mSplayAlpha );
              // mSplayColor = getBoolean( vals, 4 );  setPreference( editor, "DISTOX_SPLAY_COLOR", mSplayColor );
              mDiscreteColors = getInt( vals, 4, 0 );  setPreference( editor, "DISTOX_DISCRETE_COLORS", mDiscreteColors );
              mSplayColor = (mDiscreteColors > 0);
              mDashSplay  = getInt( vals, 6, 0 );   setPreference( editor, "DISTOX_SPLAY_DASH",  mDashSplay );
              mVertSplay  = getFloat( vals, 8, 50.0f );    setPreference( editor, "DISTOX_VERT_SPLAY",  mVertSplay );
              mHorizSplay = getFloat( vals, 10, 60.0f );   setPreference( editor, "DISTOX_HORIZ_SPLAY", mHorizSplay );
              mCosHorizSplay = TDMath.cosd( mHorizSplay );  
              mSectionSplay = getFloat( vals, 12, 60.0f ); setPreference( editor, "DISTOX_SECTION_SPLAY", mSectionSplay );
              mCosSectionSplay  = TDMath.cosd( mSectionSplay );
              if ( vals.length > 15 ) {
                mSplayDashColor = getInt( vals, 14, 0 );   setPreference( editor, "DISTOX_SPLAY_DASH_COLOR",  mSplayDashColor );
                mSplayDotColor  = getInt( vals, 15, 0 );   setPreference( editor, "DISTOX_SPLAY_DOT_COLOR",   mSplayDotColor );
                if ( vals.length > 17 ) {
                  mSplayLatestColor  = getInt( vals, 17, 0 ); setPreference( editor, "DISTOX_SPLAY_LATEST_COLOR",   mSplayLatestColor ); 
                }
              }
            }
          }
          continue;
        }
        if ( line.startsWith("Accuracy:") ) {
          if ( vals.length > 6 ) {
            mAccelerationThr = getFloat( vals, 2, 1.0f ); setPreference( editor, "DISTOX_ACCEL_PERCENT", mAccelerationThr );
            mMagneticThr     = getFloat( vals, 4, 1.0f ); setPreference( editor, "DISTOX_MAG_PERCENT",   mMagneticThr );
            mDipThr          = getFloat( vals, 6, 2.0f ); setPreference( editor, "DISTOX_DIP_THR",       mDipThr );
            if ( vals.length > 8 ) {
              mSiblingThr    = getFloat( vals, 8, 10.0f ); setPreference( editor, "DISTOX_SIBLING_PERCENT", mSiblingThr );
            }
          }
          continue;
        }
        // pw.printf(Locale.US, "Sketch: type %d, length %.2f, extrude %.2f\n", mSketchModelType, mSketchSideSize, mDeltaExtrude );
        // AUTOWALLS
        // if ( line.startsWith("Walls: type") ) {
        //   if ( vals.length > 13 ) {
        //     mWallsType        = getInt( vals, 2, 0 );    setPreference( editor, "DISTOX_WALLS_TYPE",         mWallsType );
        //     mWallsPlanThr     = getFloat( vals, 5, 70.0f );  setPreference( editor, "DISTOX_WALLS_PLAN_THR",     mWallsPlanThr );
        //     mWallsExtendedThr = getFloat( vals, 7, 45.0f );  setPreference( editor, "DISTOX_WALLS_EXTENDED_THR", mWallsExtendedThr );
        //     mWallsXClose      = getFloat( vals, 9, 0.1f );  setPreference( editor, "DISTOX_WALLS_XCLOSE",       mWallsXClose );
        //     mWallsXStep       = getFloat( vals, 11, 0.1f ); setPreference( editor, "DISTOX_WALLS_XSTEP",        mWallsXStep );
        //     mWallsConcave     = getFloat( vals, 13, 1.0f ); setPreference( editor, "DISTOX_WALLS_CONCAVE",      mWallsConcave );
        //   }
        //   continue;
        // }
        if ( line.startsWith("Log stream") ) break; 
      }
      fr.close();
    } catch ( IOException e ) { 
      TDLog.e("failed to export settings"); 
      return false;
    }
    return commitEditor( editor );
  }

  /** set the export scale(s)
   * @param scale    export scale
   * @return null if scale is within bounds, otherwise the lower or upper bound
   */
  public static String setExportScale( int scale )
  {
    String ret = null;
    if ( scale < 40 )   { scale = 40;   ret = "40"; }
    if ( scale > 2000 ) { scale = 2000; ret = "2000"; }
    SVG_SCALE = ( mSvgProgram == 1 )? SVG_SCALE_AI : SVG_SCALE_INK;
    mTherionScale = scale;
    mToTherion = THERION_SCALE / mTherionScale;
    mToSvg     = SVG_SCALE / mTherionScale;
    mToPdf     = PDF_SCALE / mTherionScale;
    // TDLog.v("Set export scale " + scale + " SVG " + mToSvg );
    return ret;
  }

  /** set the linepoint spacing (Bezier step)
   * @param step    point spacing [m]
   */
  public static String setBezierStep( float step )
  {
    String ret = null;
    if ( step < 0 ) { step = 0; ret="0"; }
    mBezierStep = step;
    return ret;
  }

}
