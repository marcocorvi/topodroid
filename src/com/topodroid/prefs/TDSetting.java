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
import com.topodroid.utils.TDsafUri;
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
import com.topodroid.TDX.DrawingWindow;
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
import java.util.Map;

import java.io.File; // PRIVATE FILE
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

import android.os.ParcelFileDescriptor;
import android.net.Uri;

import android.preference.PreferenceManager;
import android.content.res.Resources;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.view.View;

public class TDSetting
{
  public static final int FEEDBACK_NONE = 0;
  public static final int FEEDBACK_BELL = 1;
  public static final int FEEDBACK_VIBRATE = 2;
 
  public static final int DASHING_NONE    = 0;
  public static final int DASHING_CLINO   = 1;
  public static final int DASHING_AZIMUTH = 2;
  public static final int DASHING_VIEW    = 3;

  public static final boolean WITH_IMMUTABLE = false;

  private static String defaultTextSize   = "16";
  private static String defaultButtonSize = TDString.THREE;
  private static String defaultSymbolSize = "1.8";

  private static int FLAG_BUTTON = 1;
  private static int FLAG_MENU   = 2;
  private static int FLAG_TEXT   = 4;
  private static int FLAG_LOCALE = 8;
  private static int FLAG_LEVEL  = FLAG_BUTTON | FLAG_MENU;
  private static int mMainFlag = 0xff; // Main Window flag

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

  public static String setSymbolSize( float fs )
  {
    if ( fs > 0.1f && fs != mSymbolSize ) {
      mSymbolSize = fs;
      TopoDroidApp.resetRecentTools();
      return Float.toString( mSymbolSize );
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
  public static float mSymbolSize = 1.8f;   // symbol size
  public static boolean mKeyboard = false;
  public static boolean mNoCursor = true;
  public static boolean mBulkExport = false;
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

  public static boolean mPlyLRUD            = true; // HBPly
  public static boolean mPlyMinus           = false; // HBPly
  public static String  mSurvexEol          = "\n";
  public static boolean mSurvexSplay        = false; // splays with named TO
  public static boolean mSurvexLRUD         = false;
  public static int     mSurvexEPSG         = 0;     // Survex EPSG cs out

  public static boolean mSwapLR             = false; // swap LR in Compass export
  public static boolean mOrthogonalLRUD     = false; // whether angle > 0 
  public static float mOrthogonalLRUDAngle  = 0;     // angle
  public static float mOrthogonalLRUDCosine = 1;     // cosine of the angle

  // public static final boolean mExportUri = true;

  public static boolean mExportMedia          = false;  // whether to include media in export
  public static boolean mExportStationsPrefix = false;  // whether to prepend cave name to station in cSurvey/compass export
  public static String  mExportPrefix         = null;   // export prefix - only for the current run
  public static boolean mZipWithSymbols       = false;  // whether to add/load symbols to/from archive
  // public static boolean mZipShare             = false;  // whether to share exported zip
  public static boolean mZipShareCategory     = false;  // DISTOX_ZIP_SHARE_CATEGORY
  public static boolean mZipOverwrite         = true;   // whether to overwrite exported zip

  public static boolean mExportDataShare      = false;  // whether to share exported data file
  public static boolean mExportPlotShare      = false;  // whether to share exported plot file
  public static boolean mExportModelShare     = false;  // whether to share exported model file

  // ------------ THERION
  public static final float THERION_SCALE = 196.8503937f; // 200 * 39.3700787402 / 40;
  public static int     mTherionScale = 100;
  public static boolean mTherionMaps   = false;
  public static boolean mTherionUncommentedMaps = false;
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
  public static boolean mSvgStations      = true; // whether to export station names NOT PERSISTENT
  public static boolean mSvgSplays        = true;
  public static boolean mSvgGroups      = false;  // whether to group items by the type in SVG export
  public static boolean mSvgOrigin      = false;  // whether to highligh origin station with a cross NOT PERSISTENT
  // public static boolean mSvgOffset      = false;  // whether to offset the export NOT IMPLEMENTED
  public static float mSvgPointStroke   = 0.1f;
  public static float mSvgLabelStroke   = 0.3f;   // stroke-width
  public static float mSvgLineStroke    = 0.5f;
  public static float mSvgLineDirStroke = 2f;
  public static float mSvgGridStroke    = 0.5f;
  public static float mSvgShotStroke    = 0.5f;
  public static int   mSvgStationSize   = 20;     // font-size
  public static int   mSvgLabelSize     = 30;     // font-size
  // public static boolean mFixmeClass     = true;   // FIXME_CLASS
  // public static boolean mFixmeXSection  = true;   // FIXME_XSECTION

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
  public static boolean mEditableStations = false; // FIXED to false
  public static boolean mEditableShots    = false;
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
  public static final int LOOP_NONE       = 0; // coincide with values in array.xml
  public static final int LOOP_CYCLES     = 1;
  public static final int LOOP_TRIANGLES  = 3;
  public static final int LOOP_WEIGHTED   = 4;
  public static final int LOOP_SELECTIVE  = 5;
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
  // public static boolean mHideNavBar = false; // hide nav_bar
  public static int     mUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
  public static boolean mPalettes = false;   // extra tools palettes
  // public static boolean mCompositeActions = false;
  // public static boolean mWithLineJoin = false;  // with line join
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
  // public static float   mZoomLowerBound = 0.1f; // lower bound on zoom for zoom-fit

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
  public static boolean mPlotCache       = true;  // default value
  public static float mDotRadius      = 5;  // radius of selection dots - splay dots are 1.5 as big
  public static float mArrowLength    = 8;
  public static int   mSlopeLSide     = 20;  // l-side of slope lines
  public static int   mXSectionOffset = 20;  // offset of section point


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
    if ( TDString.isNullOrEmpty( val ) ) { 
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
    if ( TDString.isNullOrEmpty( val ) ) { 
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
    if ( TDString.isNullOrEmpty( val ) ) val = def_value;
    TDPrefHelper.update( key, val );
    return val;
  }

  private static boolean tryBooleanValue( TDPrefHelper hlp, String key, String val, boolean def_value )
  {
    boolean i = def_value;
    if ( ! TDString.isNullOrEmpty( val ) ) {
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
    } else if ( mLoopClosure == LOOP_TRIANGLES || mLoopClosure == LOOP_SELECTIVE ) {
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

  private static void setHideNavBar( boolean hide_navbar )
  {
    // mHideNavBar = hide_navbar;
    mUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                  | View.SYSTEM_UI_FLAG_FULLSCREEN;                // remove the appbar
    if ( hide_navbar ) mUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    // | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    // | View.SYSTEM_UI_FLAG_FULLSCREEN                // remove the appbar
    // | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION        // as soos as it is shown it does not go away
    // | View.SYSTEM_UI_FLAG_IMMERSIVE
    // | View.SYSTEM_UI_FLAG_LAYOUT_STABLE           // remove to have the layout appear under the appbar
    // | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    // | View.SYSTEM_UI_FLAG_LOW_PROFILE
    TopoDroidApp.resetUiVisibility();
  }

  // ---------------------------------------------------------------------------------
  //
  public static void loadPrimaryPreferences( /* TopoDroidApp my_app, */ Resources res, TDPrefHelper pref_hlp )
  {
    SharedPreferences prefs = pref_hlp.getSharedPrefs();
  
    defaultTextSize   = res.getString( R.string.default_textsize );
    defaultButtonSize = res.getString( R.string.default_buttonsize );
    // TDLog.v("SETTING default button size " + defaultButtonSize );

    // ------------------- GENERAL PREFERENCES
    TDPrefKey[] key = TDPrefKey.mMain;
    int level = Integer.parseInt( prefs.getString( key[3].key, key[3].dflt ) ); // DISTOX_EXTRA_BUTTONS choice: 0, 1, 2, 3
    setActivityBooleans( prefs, level );

    key = TDPrefKey.mGeek;
    mSingleBack = prefs.getBoolean(  key[0].key, bool(key[0].dflt) ); // DISTOX_SINGLE_BACK
    setHideNavBar( prefs.getBoolean( key[1].key, bool(key[1].dflt) ) ); // DISTOX_HIDE_NAVBAR
    setPalettes(  prefs.getBoolean(  key[2].key, bool(key[2].dflt) ) ); // DISTOX_PALETTES
    // setBackupsClear( prefs.getBoolean( key[1], bool(defGeek[1]) ) ); // DISTOX_BACKUPS_CLEAR CLEAR_BACKUPS
    mKeyboard = prefs.getBoolean(   key[3].key, bool(key[3].dflt) ); // DISTOX_MKEYBOARD
    mNoCursor = prefs.getBoolean(   key[4].key, bool(key[4].dflt) ); // DISTOX_NO_CURSOR
    mBulkExport = prefs.getBoolean( key[5].key, bool(key[5].dflt) ); // DISTOX_BULK_EXPORT
    mPacketLog = prefs.getBoolean(  key[6].key, bool(key[6].dflt) ); // DISTOX_PACKET_LOGGER
    mTh2Edit   = prefs.getBoolean(  key[7].key, bool(key[7].dflt) ); // DISTOX_TH2_EDIT
    mWithDebug = TDLevel.isDebugBuild() ? prefs.getBoolean( key[15].key, bool(key[15].dflt) ) : false; // DISTOX_WITH_DEBUG

    key = TDPrefKey.mMain;
    setTextSize( tryInt(    prefs,  key[0].key, key[0].dflt ) );      // DISTOX_TEXT_SIZE
    setSizeButtons( tryInt( prefs,  key[1].key, key[1].dflt ) );      // DISTOX_SIZE_BUTTONS
    setSymbolSize( tryFloat( prefs, key[2].key, key[2].dflt ) );      // DISTOX_SYMBOL_SIZE
    // skip 3
    mLocalManPages = handleLocalUserMan( /* my_app, */ prefs.getString( key[4].key, key[4].dflt ), false ); // DISTOX_LOCAL_MAN
    setLocale( prefs.getString( key[5].key, TDString.EMPTY ), false ); // DISTOX_LOCALE
    mOrientation = Integer.parseInt( prefs.getString( key[6].key, key[6].dflt ) ); // DISTOX_ORIENTATION choice: 0, 1, 2
    // setLocale( prefs.getString( keyMain[7], defMain[7] ), false ); // DISTOX_LOCALE
    // TDLog.Profile("locale");
    // boolean co_survey = prefs.getBoolean( keyMain[8], bool(defMain[8]) );        // DISTOX_COSURVEY 

    key = TDPrefKey.mSurvey;
    mDefaultTeam = prefs.getString( key[0].key, key[0].dflt );               // DISTOX_TEAM
    String s = TDString.noSpacesAndSpecials( prefs.getString( key[4].key, key[4].dflt ) );  // DISTOX_INIT_STATION 
    if ( TDString.isNullOrEmpty( s ) ) mInitStation = key[4].dflt;
    DistoXStationName.setInitialStation( mInitStation );

    key = TDPrefKey.mData;
    mAzimuthManual = prefs.getBoolean( key[6].key, bool(key[6].dflt) );   // DISTOX_AZIMUTH_MANUAL 
    // TDAzimuth.setAzimuthManual( mAzimuthManual ); // FIXME FIXED_EXTEND 20240603
    TDAzimuth.resetRefAzimuth( null, SurveyInfo.SURVEY_EXTEND_NORMAL, mAzimuthManual ); // BUG ?? may call setRefAzimuthButton on non-UI thread
    
    // ------------------- DEVICE PREFERENCES -def--fallback--min-max
    key = TDPrefKey.mDevice;
    // DISTOX_DEVICE - UNUSED HERE
    mCheckBT        = tryInt( prefs, key[0].key, key[0].dflt );        // DISTOX_BLUETOOTH choice: 0, 1, 2
  }

  public static void loadSecondaryPreferences( /* TopoDroidApp my_app, */ TDPrefHelper pref_hlp )
  {
    // TDLog.v("SETTING load secondary");
    SharedPreferences prefs = pref_hlp.getSharedPrefs();

    TDPrefKey[] key = TDPrefKey.mSurvey;
    // int old = mTeamNames;
    mTeamNames    = tryInt(   prefs,     key[1].key,      key[1].dflt );       // DISTOX_TEAM_DIALOG
    // TDLog.v("SETTING load secondary TEAM DIALOG " + old + " -> " + mTeamNames + " default " + key[1].dflt );
    parseStationPolicy( pref_hlp, prefs.getString( key[2].key, key[2].dflt ) ); // DISTOX_SURVEY_STATION
    mStationNames = (prefs.getString(    key[3].key,      key[3].dflt ).equals("number"))? 1 : 0; // DISTOX_STATION_NAMES
    mThumbSize    = tryInt(   prefs,     key[5].key,      key[5].dflt );       // DISTOX_THUMBNAIL
    // mEditableStations = prefs.getBoolean(key[6].key, bool(key[6].dflt) ); // DISTOX_EDITABLE_STATIONS
    mFixedOrigin  = prefs.getBoolean(    key[6].key, bool(key[6].dflt) ); // DISTOX_FIXED_ORIGIN
    mSharedXSections = prefs.getBoolean( key[7].key, bool(key[7].dflt) ); // DISTOX_SHARED_XSECTIONS
    // mDataBackup   = prefs.getBoolean(    key[8].key, bool(key[8].dflt) ); // DISTOX_DATA_BACKUP
    // TDLog.v("SETTING load survey done");

    key = TDPrefKey.mPlot;
    // mPickerType = tryInt( prefs,       key[0].key,      key[0].dflt );  // DISTOX_PICKER_TYPE choice: 0, 1, 2
    // if ( mPickerType < PICKER_LIST ) mPickerType = PICKER_LIST;
    // mTripleToolbar   = prefs.getBoolean(    key[1].key, bool(key[1].dflt) ); // DISTOX_TRIPLE_TOOLBAR
    // mRecentNr   = tryInt( prefs,       key[ ].key,      key[ ].dflt );  // DISTOX_RECENT_NR choice: 3, 4, 5, 6
    mSideDrag   = prefs.getBoolean(    key[0].key, bool(key[0].dflt) ); // DISTOX_SIDE_DRAG
    // setZoomControls( prefs.getBoolean( key[ ].key, bool(key[ ].dflt) ) ); // DISTOX_ZOOM_CONTROLS
    setZoomControls( prefs.getString(  key[1].key,      key[1].dflt ), TDandroid.checkMultitouch( TDInstance.context ) ); // DISTOX_ZOOM_CTRL
    // mSectionStations  = tryInt( prefs, key[ ].key, "3");      // DISTOX_SECTION_STATIONS
    mHThreshold    = tryFloat( prefs,  key[2].key,      key[2].dflt );  // DISTOX_HTHRESHOLD
    mCheckAttached = prefs.getBoolean( key[3].key, bool(key[3].dflt) ); // DISTOX_CHECK_ATTACHED
    mCheckExtend   = prefs.getBoolean( key[4].key, bool(key[4].dflt) ); // DISTOX_CHECK_EXTEND
    mItemButtonSize= tryFloat( prefs,  key[5].key,      key[5].dflt );  // DISTOX_TOOLBAR_SIZE
    mPlotCache     = prefs.getBoolean( key[6].key, bool(key[6].dflt) ); // DISTOX_PLOT_CACHE
    // TDLog.v("SETTING load plot done");

    key = TDPrefKey.mCalib;
    mGroupBy       = tryInt(   prefs,      key[ 0].key,     key[ 0].dflt );  // DISTOX_GROUP_BY choice: 0, 1, 2
    mGroupDistance = tryFloat( prefs,      key[ 1].key,     key[ 1].dflt );  // DISTOX_GROUP_DISTANCE
    mCalibEps      = tryFloat( prefs,      key[ 2].key,     key[ 2].dflt );  // DISTOX_CAB_EPS
    mCalibMaxIt    = tryInt(   prefs,      key[ 3].key,     key[ 3].dflt );  // DISTOX_CALIB_MAX_IT
    mCalibShotDownload = prefs.getBoolean( key[ 4].key,bool(key[ 4].dflt) ); // DISTOX_CALIB_SHOT_DOWNLOAD
    // mRawData       = prefs.getBoolean(  key[  ].key,bool(key[  ].dflt) );    // DISTOX_RAW_DATA 20
    mRawCData      = tryInt( prefs,        key[ 5].key,     key[ 5].dflt );  // DISTOX_RAW_CDATA 20
    // mCalibAlgo     = tryInt( prefs,     key[ 6].key,     key[ 6].dflt );  // DISTOX_CALIB_ALGO choice: 0, 1, 2
    mAlgoMinAlpha  = tryFloat( prefs,      key[ 6].key,     key[ 6].dflt );  // DISTOX_ALGO_MIN_ALPHA
    mAlgoMinBeta   = tryFloat( prefs,      key[ 7].key,     key[ 7].dflt );  // DISTOX_ALGO_MIN_BETA
    mAlgoMinGamma  = tryFloat( prefs,      key[ 8].key,     key[ 8].dflt );  // DISTOX_ALGO_MIN_GAMMA
    mAlgoMinDelta  = tryFloat( prefs,      key[ 9].key,     key[ 9].dflt ); // DISTOX_ALGO_MIN_DELTA
    mAutoCalBeta   = tryFloat( prefs,      key[10].key,     key[10].dflt ); // DISTOX_AUTO_CAL_BETA
    mAutoCalEta    = tryFloat( prefs,      key[11].key,     key[11].dflt ); // DISTOX_AUTO_CAL_ETA
    mAutoCalGamma  = tryFloat( prefs,      key[12].key,     key[12].dflt ); // DISTOX_AUTO_CAL_GAMMA
    mAutoCalDelta  = tryFloat( prefs,      key[13].key,     key[13].dflt ); // DISTOX_AUTO_CAL_DELTA
    // TDLog.v("SETTING load calib done");

    key = TDPrefKey.mDevice;
    mConnectionMode  = tryInt( prefs,      key[ 1].key,       key[ 1].dflt );   // DISTOX_CONN_MODE choice: 0, 1, 2
    // mAutoReconnect  = prefs.getBoolean( key[ 2].key,  bool(key[ 2].dflt) );  // DISTOX_AUTO_RECONNECT
    mHeadTail        = prefs.getBoolean(   key[ 2].key,  bool(key[ 2].dflt) );  // DISTOX_HEAD_TAIL
    // TDLog.v("SETTINGS load dev skip>" + key[ 3].key + ":" +key[ 3].dflt + "<" );
    mSockType        = tryInt( prefs,      key[ 3].key,       key[ 3].dflt ); // mDefaultSockStrType );  // DISTOX_SOCKET_TYPE choice: 0, 1, (2, 3)
    // mZ6Workaround   = prefs.getBoolean( key[ 4].key,  bool(key[ 4].dflt)  ); // DISTOX_Z6_WORKAROUND
    mAutoPair        = prefs.getBoolean(   key[ 4].key,  bool(key[ 4].dflt) );  // DISTOX_AUTO_PAIR
    mConnectFeedback = tryInt( prefs,      key[ 6].key,       key[ 5].dflt );   // DISTOX_CONNECT_FEEDBACK
    // TDLog.v("SETTING load device done");

    key = TDPrefKey.mGeekDevice;
    mUnnamedDevice  = prefs.getBoolean( key[ 1].key, bool(key[ 1].dflt)  ); // DISTOX_UNNAMED_DEVICE BT_NONAME
    mConnectSocketDelay = tryInt(prefs, key[ 2].key,      key[ 2].dflt );   // DISTOX_SOCKET_DELAY
    mSecondDistoX   = prefs.getBoolean( key[ 3].key, bool(key[ 3].dflt) );  // DISTOX_SECOND_DISTOX
    mWaitData       = tryInt( prefs,    key[ 4].key,      key[ 4].dflt );   // DISTOX_WAIT_DATA
    mWaitConn       = tryInt( prefs,    key[ 5].key,      key[ 5].dflt );   // DISTOX_WAIT_CONN
    mWaitLaser      = tryInt( prefs,    key[ 6].key,      key[ 6].dflt );   // DISTOX_WAIT_LASER
    mWaitShot       = tryInt( prefs,    key[ 7].key,      key[ 7].dflt );   // DISTOX_WAIT_SHOT
    mFirmwareSanity = prefs.getBoolean( key[ 8].key, bool(key[ 8].dflt) );  // DISTOX_FIRMWARE_SANITY
    mBricMode       = tryInt( prefs,    key[ 9].key,      key[ 9].dflt );   // DISTOX_BRIC_MODE
    mBricZeroLength = prefs.getBoolean( key[10].key, bool(key[10].dflt) );  // DISTOX_BRIC_ZERO_LENGTH
    mBricIndexIsId  = prefs.getBoolean( key[11].key, bool(key[11].dflt) );  // DISTOX_BRIC_INDEX_IS_ID
    mSap5Bit16Bug   = prefs.getBoolean( key[12].key, bool(key[12].dflt) );  // DISTOX_SAP5_BIT16_BUG
    // TDLog.v("SETTING load geek device done");

    key = TDPrefKey.mCave3D;
    boolean b = prefs.getBoolean( key[0].key, bool(key[0].dflt) );
    GlRenderer.mMinClino = b ? 90 : 0;
    GlModel.mStationPoints  = prefs.getBoolean( key[1].key, bool(key[1].dflt) );
    GlNames.setPointSize( tryInt(   prefs,      key[2].key,      key[2].dflt ) );
    GlNames.setTextSize( tryInt(   prefs,       key[3].key,      key[3].dflt ) );
    TopoGL.mSelectionRadius = tryFloat( prefs,  key[4].key,      key[4].dflt );
    TopoGL.mMeasureToast    = prefs.getBoolean( key[5].key, bool(key[5].dflt) );
    TopoGL.mStationDialog   = prefs.getBoolean( key[6].key, bool(key[6].dflt) );
    GlModel.mGridAbove      = prefs.getBoolean( key[7].key, bool(key[7].dflt) );
    GlModel.mGridExtent     = tryInt(   prefs,  key[8].key,      key[8].dflt );
    GlNames.mNamesVisible   = prefs.getBoolean( key[9].key, bool(key[9].dflt) );

    key = TDPrefKey.mDem3D;
    TopoGL.mDEMbuffer   = tryFloat( prefs, key[0].key, key[0].dflt );
    TopoGL.mDEMmaxsize  = tryInt(   prefs, key[1].key, key[1].dflt );
    TopoGL.mDEMreduce   = tryInt(   prefs, key[2].key, key[2].dflt );
    TopoGL.mTextureRoot = prefs.getString( key[3].key, key[3].dflt );

    key = TDPrefKey.mWalls3D;
    TglParser.mSplayUse = tryInt(   prefs,  key[0].key,      key[0].dflt );
    GlModel.mAllSplay   = prefs.getBoolean( key[1].key, bool(key[1].dflt) );
    TopoGL.mSplayProj   = prefs.getBoolean( key[2].key, bool(key[2].dflt) );
    TopoGL.mSplayThr    = tryFloat( prefs,  key[3].key,      key[3].dflt );
    GlModel.mSplitTriangles = prefs.getBoolean( key[4].key, bool(key[4].dflt) );
    float r = tryFloat( prefs,  key[5].key, key[5].dflt );
    if ( r > 0.0001f ) {
      GlModel.mSplitRandomizeDelta = r;
      GlModel.mSplitRandomize = true;
    } else {
      GlModel.mSplitRandomize = false;
    }
    r = tryFloat( prefs,  key[6].key, key[6].dflt );
    if ( r > 0.0001f ) {
      GlModel.mSplitStretchDelta = r;
      GlModel.mSplitStretch = true;
    } else {
      GlModel.mSplitStretch = false;
    }
    GlModel.mPowercrustDelta = tryFloat( prefs,  key[7].key, key[7].dflt );
    // TDLog.v("SETTING load model done");

    key = TDPrefKey.mSketch;
    m3Dsketch    = prefs.getBoolean( key[0].key, bool(key[0].dflt) );
    mSplayBuffer = tryFloat( prefs,  key[1].key,      key[1].dflt );

    key = TDPrefKey.mExportImport;
    // keyImport[ 0 ] // DISTOX_PT_CMAP
    mLRExtend          = prefs.getBoolean(     key[ 1].key, bool(key[ 1].dflt) ); // DISTOX_SPLAY_EXTEND
    // TDLog.v("SETTING load secondary export import done");

    key = TDPrefKey.mGeekImport;
    mZipWithSymbols = prefs.getBoolean( key[ 0].key, bool(key[ 0].dflt) ); // DISTOX_ZIP_WITH_SYMBOLS
    mImportDatamode = tryInt(   prefs,  key[ 1].key,      key[ 1].dflt );  // DISTOX_IMPORT_DATAMODE
    mAutoXSections  = prefs.getBoolean( key[ 2].key, bool(key[ 2].dflt) ); // DISTOX_AUTO_XSECTIONS
    mAutoStations   = prefs.getBoolean( key[ 3].key, bool(key[ 3].dflt) ); // DISTOX_AUTO_STATIONS
    mLRUDcount      = prefs.getBoolean( key[ 4].key, bool(key[ 4].dflt) ); // DISTOX_LRUD_COUNT
    mZipShareCategory = prefs.getBoolean( key[ 5].key, bool(key[ 5].dflt) ); // DISTOX_ZIP_SHARE_CATEGORY
    // mAutoExportPlotFormat = tryInt( prefs,  key[ 4].key,      key[ 4].dflt );  // DISTOX_AUTO_PLOT_EXPORT choice: ...
    // mExportTcsx     = prefs.getBoolean(     key[ 2].key, bool(key[ 2].dflt) ); // DISTOX_TRANSFER_CSURVEY
    // TDLog.v("SETTING load secondary GEEK import done");

    key = TDPrefKey.mExportEnable;
    for ( int k = 0; k < key.length; ++ k ) {
      b = prefs.getBoolean( key[ k].key, bool(key[ k].dflt) );
      TDConst.mSurveyExportEnable[ 1 + k ] = b;
      // TDLog.v("SETTING enable " + (1+k) + " " + b );
    }

    key = TDPrefKey.mExport;
    mExportShotsFormat = tryInt(   prefs,      key[ 0].key,      key[ 0].dflt );  // DISTOX_EXPORT_SHOTS choice: 
    mExportPlotFormat  = tryInt(   prefs,      key[ 1].key,      key[ 1].dflt );  // DISTOX_EXPORT_PLOT choice: 14, 2, 11, 12, 13
    mAutoExportPlotFormat = tryInt(  prefs,    key[ 2].key,      key[ 2].dflt );  // DISTOX_AUTO_PLOT_EXPORT choice: ...
    mOrthogonalLRUDAngle = tryFloat( prefs,    key[ 3].key,      key[ 3].dflt );  // DISTOX_ORTHO_LRUD
    mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
    mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    mLRUDvertical      = tryFloat( prefs,      key[ 4].key,      key[ 4].dflt );  // DISTOX_LRUD_VERTICAL
    mLRUDhorizontal    = tryFloat( prefs,      key[ 5].key,      key[ 5].dflt );  // DISTOX_LRUD_HORIZONTAL
    mBezierStep        = tryFloat( prefs,      key[ 6].key,      key[ 6].dflt );  // DISTOX_BEZIER_STEP
    // TDLog.v("SETTING load secondary export done");

    key = TDPrefKey.mExportSvx;
    mSurvexEol         = ( prefs.getString(  key[0].key,      key[0].dflt ).equals("LF") )? "\n" : "\r\n";  // DISTOX_SURVEX_EOL
    mSurvexLRUD        =   prefs.getBoolean( key[1].key, bool(key[1].dflt) ); // DISTOX_SURVEX_LRUD
    mSurvexSplay       =   prefs.getBoolean( key[2].key, bool(key[2].dflt) ); // DISTOX_SURVEX_SPLAY
    mSurvexEPSG        = tryInt(   prefs,    key[3].key,      key[3].dflt );  // DISTOX_SURVEX_EPSG
    // TDLog.v("SETTING load secondary export SVX done");

    key = TDPrefKey.mExportPly;
    mPlyLRUD  = prefs.getBoolean( key[0].key, bool(key[0].dflt) ); // DISTOX_PLY_LRUD
    mPlyMinus = prefs.getBoolean( key[1].key, bool(key[1].dflt) ); // DISTOX_PLY_MINUS

    key = TDPrefKey.mExportTh;
    mTherionWithConfig = prefs.getBoolean( key[0].key, bool(key[0].dflt) ); // DISTOX_THERION_CONFIG
    mTherionMaps       = prefs.getBoolean( key[1].key, bool(key[1].dflt) ); // DISTOX_THERION_MAPS
    // mAutoStations   = prefs.getBoolean( key[2].key, bool(key[2].dflt) ); // DISTOX_AUTO_STATIONS 
    // mXTherionAreas  = prefs.getBoolean( key[ ].key, bool(key[ ].dflt) ); // DISTOX_XTHERION_AREAS
    mTherionSplays     = prefs.getBoolean( key[2].key, bool(key[2].dflt) ); // DISTOX_THERION_SPLAYS
    // mSurvexLRUD     = prefs.getBoolean( key[3].key, bool(key[3].dflt) ); // DISTOX_SURVEX_LRUD
    mTherionScale      = tryInt( prefs,    key[4].key,      key[4].dflt );  // DISTOX_TH2_SCALE
    mTherionXvi        = prefs.getBoolean( key[5].key, bool(key[5].dflt) ); // DISTOX_TH2_XVI
    // TDLog.v("SETTING load secondary export TH done");

    key = TDPrefKey.mExportDat;
    mExportStationsPrefix =  prefs.getBoolean( key[0].key, bool(key[0].dflt) ); // DISTOX_STATION_PREFIX
    mCompassSplays     = prefs.getBoolean(     key[1].key, bool(key[1].dflt) ); // DISTOX_COMPASS_SPLAYS
    mSwapLR            = prefs.getBoolean(     key[2].key, bool(key[2].dflt) ); // DISTOX_SWAP_LR
    
    key = TDPrefKey.mExportSrv;
    mWallsSplays       = prefs.getBoolean( key[0].key, bool(key[0].dflt) ); // DISTOX_WALLS_SPLAYS
    // mWallsUD           = tryInt( prefs, key[1].key,      key[1].dflt );  // DISTOX_WALLS_UD
    
    key = TDPrefKey.mExportTro;
    mVTopoSplays       = prefs.getBoolean(     key[0].key, bool(key[0].dflt) ); // DISTOX_VTOPO_SPLAYS
    mVTopoLrudAtFrom   = prefs.getBoolean(     key[1].key, bool(key[1].dflt) ); // DISTOX_VTOPO_LRUD
    mVTopoTrox         = prefs.getBoolean(     key[2].key, bool(key[2].dflt) ); // DISTOX_VTOPO_TROX

    key = TDPrefKey.mExportSvg;
    mSvgRoundTrip      = prefs.getBoolean(     key[0].key, bool(key[9].dflt) ); // DISTOX_SVG_ROUNDTRIP
    mSvgGrid           = prefs.getBoolean(     key[1].key, bool(key[1].dflt) ); // DISTOX_SVG_GRID
    mSvgLineDirection  = prefs.getBoolean(     key[2].key, bool(key[2].dflt) ); // DISTOX_SVG_LINE_DIR
    mSvgSplays         = prefs.getBoolean(     key[3].key, bool(key[3].dflt) ); // DISTOX_SVG_SPLAYS
    mSvgGroups         = prefs.getBoolean(     key[4].key, bool(key[4].dflt) ); // DISTOX_SVG_GROUPS
    // mSvgInHtml      = prefs.getBoolean(     key[ ].key, bool(key[ ].dflt) ); // DISTOX_SVG_IN_HTML
    mSvgPointStroke    = tryFloat( prefs,      key[ 5].key,     key[ 5].dflt );  // DISTOX_SVG_POINT_STROKE
    mSvgLabelStroke    = tryFloat( prefs,      key[ 6].key,     key[ 6].dflt );  // DISTOX_SVG_LABEL_STROKE
    mSvgLineStroke     = tryFloat( prefs,      key[ 7].key,     key[ 7].dflt );  // DISTOX_SVG_LINE_STROKE
    mSvgGridStroke     = tryFloat( prefs,      key[ 8].key,     key[ 8].dflt );  // DISTOX_SVG_GRID_STROKE
    mSvgShotStroke     = tryFloat( prefs,      key[ 9].key,     key[ 9].dflt );  // DISTOX_SVG_SHOT_STROKE
    mSvgLineDirStroke  = tryFloat( prefs,      key[10].key,     key[10].dflt ); // DISTOX_SVG_LINEDIR_STROKE
    mSvgStationSize    = tryInt(   prefs,      key[11].key,     key[11].dflt ); // DISTOX_SVG_STATION_SIZE
    mSvgLabelSize      = tryInt  ( prefs,      key[12].key,     key[12].dflt ); // DISTOX_SVG_LABEL_SIZE
    mSvgProgram        = tryInt(   prefs,      key[13].key,     key[13].dflt );  // DISTOX_SVG_PROGRAM
    // TDLog.v("SETTING load secondary export SVG done");

    // having mTherionScale and mSvgProgram we can set export scale
    setExportScale( mTherionScale );

    key = TDPrefKey.mExportKml;
    mKmlStations       = prefs.getBoolean(     key[0].key, bool(key[0].dflt) ); // DISTOX_KML_STATIONS
    mKmlSplays         = prefs.getBoolean(     key[1].key, bool(key[1].dflt) ); // DISTOX_KML_SPLAYS
    // TDLog.v("SETTING load secondary export KML done");

    key = TDPrefKey.mExportCsx;
    // mExportStationsPrefix = prefs.getBoolean( key[0].key, bool(key[0].dflt) ); // DISTOX_STATION_PREFIX
    mExportMedia       =  prefs.getBoolean(      key[1].key, bool(key[1].dflt) ); // DISTOX_WITH_MEDIA

    key = TDPrefKey.mExportGpx;
    mGPXSingleTrack    = prefs.getBoolean(     key[0].key, bool(key[0].dflt) ); // DISTOX_GPX_STATION_TRACK

    key = TDPrefKey.mExportCsv;
    mCsvRaw            = prefs.getBoolean(     key[0].key, bool(key[0].dflt) ); // DISTOX_CSV_RAW
    mCsvSeparator      = CSV_SEPARATOR[ tryInt( prefs, key[1].key, key[1].dflt ) ]; // DISTOX_CSV_SEP

    /* NO_PNG
    keyExp = TDPrefKey.EXPORT_PNG;
    defExp = TDPrefKey.EXPORT_PNGdef;
    mBitmapScale       = tryFloat( prefs,      keyExp[0], defExp[0] );  // DISTOX_BITMAP_SCALE 
    setBitmapBgcolor( prefs, keyExp[1], prefs.getString(keyExp[1], defExp[1]), defExp[1] );  // DISTOX_BITMAP_BGCOLOR
    // TDLog.v("SETTING load secondary export PNG done");
    */

    key = TDPrefKey.mExportDxf;
    // mDxfScale     = tryFloat( prefs,    key[ ].key,      key[ ].dflt );  // DISTOX_DXF_SCALE
    mDxfBlocks    =  prefs.getBoolean(     key[0].key, bool(key[0].dflt) ); // DISTOX_DXF_BLOCKS
    mAcadVersion  = tryInt(   prefs,       key[1].key,      key[1].dflt );  // DISTOX_ACAD_VERSION choice: 9, 13, 16
    mAcadSpline   =  prefs.getBoolean(     key[2].key, bool(key[2].dflt) ); // DISTOX_ACAD_SPLINE
    mDxfReference =  prefs.getBoolean(     key[3].key, bool(key[3].dflt) ); // DISTOX_DXF_REFERENCE
    mAcadLayer    =  prefs.getBoolean(     key[4].key, bool(key[4].dflt) ); // DISTOX_ACAD_LAYER
    // TDLog.v("SETTING load secondary export DXF done");
  
    key = TDPrefKey.mExportShp;
    mShpGeoref   =  prefs.getBoolean(     key[0].key, bool(key[0].dflt) ); // DISTOX_SHP_GEOREF
    // TDLog.v("SETTING load secondary export SHP done");

    key = TDPrefKey.mData;
    mCloseDistance = tryFloat( prefs,          key[ 0].key,      key[ 0].dflt );  // DISTOX_CLOSE_DISTANCE
    mMaxShotLength = tryFloat( prefs,          key[ 1].key,      key[ 1].dflt );  // DISTOX_MAX_SHOT_LENGTH
    mMinLegLength  = tryFloat( prefs,          key[ 2].key,      key[ 2].dflt );  // DISTOX_MIN_LEG_LENGTH
    mMinNrLegShots = tryInt(   prefs,          key[ 3].key,      key[ 3].dflt );  // DISTOX_LEG_SHOTS choice: 2, 3, 4
    mExtendThr     = tryFloat( prefs,          key[ 4].key,      key[ 4].dflt  ); // DISTOX_EXTEND_THR2
    mVThreshold    = tryFloat( prefs,          key[ 5].key,      key[ 5].dflt  ); // DISTOX_VTHRESHOLD
    // DISTOX_AZIMUTH_MANUAL [7] handled in the first pass
    mPrevNext      = prefs.getBoolean(         key[ 7].key, bool(key[ 7].dflt) ); // DISTOX_PREV_NEXT
    mBacksightInput = prefs.getBoolean(        key[ 8].key, bool(key[ 8].dflt) ); // DISTOX_BACKSIGHT
    mTripleShot     = tryInt(  prefs,          key[ 9].key,      key[ 9].dflt  ); // DISTOX_LEG_FEEDBACK
    // mTimerWait     = tryInt(   prefs,       key[10].key,      key[10].dflt );  // DISTOX_SHOT_TIMER
    // mBeepVolume    = tryInt(   prefs,       key[11].key,      key[11].dflt );  // DISTOX_BEEP_VOLUME
    // TDLog.v("SETTING load secondary data done");

    key = TDPrefKey.mGeekShot;
    mDivingMode       = prefs.getBoolean( key[ 0].key, bool(key[ 0].dflt) ); // DISTOX_DIVING_MODE
    mEditableShots    = prefs.getBoolean( key[ 1].key, bool(key[ 1].dflt) ); // DISTOX_TAMPERING
    mBacksightSplay   = prefs.getBoolean( key[ 2].key, bool(key[ 2].dflt) ); // DISTOX_BACKSIGHT_SPLAY
    mShotRecent       = prefs.getBoolean( key[ 3].key, bool(key[ 3].dflt) ); // DISTOX_RECENT_SHOT
    mRecentTimeout    = tryInt(   prefs,  key[ 4].key,      key[ 4].dflt );  // DISTOX_RECENT_TIMEOUT
    mExtendFrac       = prefs.getBoolean( key[ 5].key, bool(key[ 5].dflt) ); // DISTOX_EXTEND_FRAC
    mDistoXBackshot   = prefs.getBoolean( key[ 6].key, bool(key[ 6].dflt) ); // DISTOX_BACKSHOT
    mBedding          = prefs.getBoolean( key[ 7].key, bool(key[ 7].dflt) ); // DISTOX_BEDDING
    mWithSensors      = prefs.getBoolean( key[ 8].key, bool(key[ 8].dflt) ); // DISTOX_WITH_SENSORS
    setLoopClosure( tryInt(   prefs,      key[ 9].key,      key[ 9].dflt ) );// DISTOX_LOOP_CLOSURE_VALUE
    mLoopThr          = tryFloat( prefs,  key[10].key,      key[10].dflt );  // DISTOX_LOOP_THR
    mWithAzimuth      = prefs.getBoolean( key[11].key, bool(key[11].dflt) ); // DISTOX_ANDROID_AZIMUTH
    mTimerWait        = tryInt(   prefs,  key[12].key,      key[12].dflt );  // DISTOX_SHOT_TIMER
    mBeepVolume       = tryInt(   prefs,  key[13].key,      key[13].dflt );  // DISTOX_BEEP_VOLUME
    mBlunderShot      = prefs.getBoolean( key[14].key, bool(key[14].dflt) ); // DISTOX_BLUNDER_SHOT
    mSplayStation     = prefs.getBoolean( key[15].key, bool(key[15].dflt) ); // DISTOX_SPLAY_STATION
    mSplayOnlyForward = prefs.getBoolean( key[16].key, bool(key[16].dflt) ); // DISTOX_SPLAY_GROUP
    // mWithTdManager = prefs.getBoolean( key[13].key, bool(key[13].dflt) ); // DISTOX_TDMANAGER
    // TDLog.v("SETTING load secondary GEEK data done");

    key = TDPrefKey.mGeekPlot;
    mPlotShift     = prefs.getBoolean( key[ 0].key, bool(key[ 0].dflt) ); // DISTOX_PLOT_SHIFT
    mPlotSplit     = prefs.getBoolean( key[ 1].key, bool(key[ 1].dflt) ); // DISTOX_PLOT_SPLIT
    setStylusSize(  tryFloat( prefs,   key[ 2].key,      key[ 2].dflt ) );  // DISTOX_STYLUS_SIZE // STYLUS_MM
    mBackupNumber   = tryInt( prefs,   key[ 3].key,      key[ 3].dflt );  // DISTOX_BACKUP_NUMBER
    mBackupInterval = tryInt( prefs,   key[ 4].key,      key[ 4].dflt );  // DISTOX_BACKUP_INTERVAL
    // mAutoXSections  = prefs.getBoolean( key[ 5].key, bool(key.dflt[ 5].dflt) ); // DISTOX_AUTO_XSECTIONS
    mSavedStations  = prefs.getBoolean( key[ 5].key, bool(key[ 5].dflt) ); // DISTOX_SAVED_STATIONS
    mLegOnlyUpdate  = prefs.getBoolean( key[ 6].key, bool(key[ 6].dflt) ); // DISTOX_LEGONLY_UPDATE
    mFullAffine     = prefs.getBoolean( key[ 7].key, bool(key[ 7].dflt) ); // DISTOX_FULL_UPDATE
    mWithLevels     = tryInt( prefs,    key[ 8].key,      key[ 8].dflt );   // DISTOX_WITH_LEVELS
    mGraphPaperScale = tryInt( prefs,   key[ 9].key,      key[ 9].dflt );  // DISTOX_GRAPH_PAPER_SCALE
    mSlantXSection  = prefs.getBoolean( key[10].key, bool(key[10].dflt) ); // DISTOX_SLANT_XSECTION
    mObliqueMax     = tryInt( prefs,    key[11].key,      key[11].dflt );  // DISTOX_OBLIQUE_PROJECTED
    mLineEnds       = tryInt( prefs,    key[12].key,      key[12].dflt );  // DISTOX_LINE_ENDS
    // mZoomLowerBound = tryFloat( prefs, key[13].key,      key[13].dflt );  // DISTOX_ZOOM_LOWER_BOUND
    // TDLog.v("SETTING load secondary GEEK plot done");

    key = TDPrefKey.mGeekSplay;
    mSplayClasses  = prefs.getBoolean( key[ 0].key, bool(key[ 0].dflt) ); // DISTOX_SPLAY_CLASSES
    // mSplayColor = prefs.getBoolean( key[ 1].key, bool(key[ 1].dflt) ); // DISTOX_SPLAY_COLOR
    mDiscreteColors = tryInt( prefs,   key[ 1].key,      key[ 1].dflt );  // DISTOX_DISCRETE_COLORS
    mSplayColor = (mDiscreteColors > 0);
    // mSplayAsDot = prefs.getBoolean( key[ 2].key, bool(key[ 2].dflt) ); // DISTOX_SPLAY_AS_DOT
    mSplayVertThrs  = tryFloat( prefs, key[ 2].key,      key[ 2].dflt  ); // DISTOX_SPLAY_VERT_THRS
    mDashSplay      = tryInt( prefs,   key[ 3].key,      key[ 3].dflt );  // DISTOX_SPLAY_DASH
    mVertSplay      = tryFloat( prefs, key[ 4].key,      key[ 4].dflt );  // DISTOX_VERT_SPLAY
    mHorizSplay     = tryFloat( prefs, key[ 5].key,      key[ 5].dflt );  // DISTOX_HORIZ_SPLAY
    mCosHorizSplay = TDMath.cosd( mHorizSplay );  
    mSectionSplay   = tryFloat( prefs, key[ 6].key,      key[ 6].dflt );  // DISTOX_SECTION_SPLAY
    mCosSectionSplay  = TDMath.cosd( mSectionSplay );
    mSplayDashColor   = tryColor( prefs, key[ 7].key,    key[ 7].dflt );  // DISTOX_SPLAY_DASH_COLOR
    BrushManager.setSplayDashColor( mSplayDashColor );
    mSplayDotColor    = tryColor( prefs, key[ 8].key,    key[ 8].dflt );  // DISTOX_SPLAY_DOT_COLOR
    BrushManager.setSplayDotColor( mSplayDotColor );
    mSplayLatestColor = tryColor( prefs, key[ 9].key,    key[ 9].dflt );  // DISTOX_SPLAY_LATEST_COLOR
    BrushManager.setSplayLatestColor( mSplayLatestColor );
    // TDLog.v("SETTING load secondary GEEK plot done");

    key = TDPrefKey.mGeekLine;
    setReduceAngle( tryFloat(  prefs,  key[ 0].key,      key[ 0].dflt ) ); // DISTOX_REDUCE_ANGLE
    mLineAccuracy  = tryFloat( prefs,  key[ 1].key,      key[ 1].dflt );   // DISTOX_LINE_ACCURACY
    mLineCorner    = tryFloat( prefs,  key[ 2].key,      key[ 2].dflt );   // DISTOX_LINE_CORNER
    mWeedDistance  = tryFloat( prefs,  key[ 3].key,      key[ 3].dflt );   // DISTOX_WEED_DISTANCE
    mWeedLength    = tryFloat( prefs,  key[ 4].key,      key[ 4].dflt );   // DISTOX_WEED_LENGTH
    mWeedBuffer    = tryFloat( prefs,  key[ 5].key,      key[ 5].dflt );   // DISTOX_WEED_BUFFER
    mLineSnap      = prefs.getBoolean( key[ 6].key, bool(key[ 6].dflt) );  // DISTOX_LINE_SNAP
    mLineCurve     = prefs.getBoolean( key[ 7].key, bool(key[ 7].dflt) );  // DISTOX_LINE_CURVE
    mLineStraight  = prefs.getBoolean( key[ 8].key, bool(key[ 8].dflt) );  // DISTOX_LINE_STRAIGHT
    mPathMultiselect=prefs.getBoolean( key[ 9].key, bool(key[ 9].dflt) );  // DISTOX_PATH_MULTISELECT
    // mCompositeActions = prefs.getBoolean( key[10].key, bool(key[10].dflt) );  // DISTOX_COMPOSITE_ACTIONS
    // TDLog.v("SETTING load secondary GEEK line done");

    key = TDPrefKey.mUnits;
    if ( prefs.getString( key[0].key, key[0].dflt ).equals( key[0].dflt ) ) {
      mUnitLength = 1.0f;
      mUnitLengthStr = "m";
    } else {
      mUnitLength = TDUtil.M2FT;
      mUnitLengthStr = "ft";
    }
    if ( prefs.getString( key[1].key,  key[1].dflt ).equals( key[1].dflt ) ) {
      mUnitAngle = 1.0f;
      mUnitAngleStr = "deg";
    } else {
      mUnitAngle = TDUtil.DEG2GRAD;
      mUnitAngleStr = "grad";
    }

    mUnitGrid       = tryFloat(  prefs, key[2].key, key[2].dflt );      // DISTOX_UNIT_GRID
    mUnitMeasure    = tryFloat(  prefs, key[3].key, key[3].dflt );      // DISTOX_UNIT_MEASURE
    // TDLog.v("SETTING units grid " + mUnitGrid );
  
    key = TDPrefKey.mAccuracy;
    mAccelerationThr = tryFloat( prefs, key[0].key, key[0].dflt ); // DISTOX_ACCEL_PERCENT
    mMagneticThr     = tryFloat( prefs, key[1].key, key[1].dflt ); // DISTOX_MAG_PERCENT
    mDipThr          = tryFloat( prefs, key[2].key, key[2].dflt ); // DISTOX_DIP_THR
    setSiblingThr( tryFloat( prefs,     key[3].key, key[3].dflt ) ); // DISTOX_SIBLING_PERCENT

    key = TDPrefKey.mLocation;
    mUnitLocation  = (prefs.getString( key[0].key,      key[0].dflt ).equals(key[0].dflt)) ? TDUtil.DDMMSS  // DISTOX_UNIT_LOCATION
                                                                           : TDUtil.DEGREE;
    mCRS           = prefs.getString(  key[1].key,      key[1].dflt );       // DISTOX_CRS
    mNegAltitude   = prefs.getBoolean( key[2].key, bool(key[2].dflt) ); // DISTOX_NEG_ALTITUDE
    mEditableHGeo  = prefs.getBoolean( key[3].key, bool(key[3].dflt) ); // DISTOX_EDIT_ALTITUDE
    mFineLocation  = tryInt( prefs,    key[4].key,      key[4].dflt );       // DISTOX_FINE_LOCATION
    // mGeoImportApp  = tryInt( prefs, key[4].key,      key[4].dflt );       // DISTOX_GEOPOINT_APP
    // TDLog.v("PREFS key <" + keyLoc[4] + "> val <" + defLoc[4] + ">" );
    try {
      mGeoImportApp = Integer.parseInt( prefs.getString( key[5].key, key[5].dflt ) );
    } catch ( RuntimeException e ) {
      TDLog.v("ERROR " + e.getMessage() );
    } catch ( Exception ee ) {
      TDLog.v("EXCEPT " + ee.getMessage() );
    }

    key = TDPrefKey.mScreen;
    mFixedThickness = tryFloat( prefs, key[ 0].key, key[ 0].dflt );  // DISTOX_FIXED_THICKNESS
    mStationSize    = tryFloat( prefs, key[ 1].key, key[ 1].dflt );  // DISTOX_STATION_SIZE
    mDotRadius      = tryFloat( prefs, key[ 2].key, key[ 2].dflt );  // DISTOX_DOT_RADIUS
    mSelectness     = tryFloat( prefs, key[ 3].key, key[ 3].dflt );  // DISTOX_CLOSENESS
    mEraseness      = tryFloat( prefs, key[ 4].key, key[ 4].dflt );  // DISTOX_ERASENESS
    mMinShift       = tryInt(   prefs, key[ 5].key, key[ 5].dflt );  // DISTOX_MIN_SHIFT
    mPointingRadius = tryInt(   prefs, key[ 6].key, key[ 6].dflt );  // DISTOX_POINTING
    mSplayAlpha     = tryInt(   prefs, key[ 7].key, key[ 7].dflt );  // DISTOX_SPLAY_ALPHA
    BrushManager.setSplayAlpha( mSplayAlpha );

    key = TDPrefKey.mLine;
    mLineThickness = tryFloat( prefs,  key[0].key,      key[0].dflt );   // DISTOX_LINE_THICKNESS
    setLineStyleAndType( prefs.getString( key[1].key,   key[1].dflt ) ); // DISTOX_LINE_STYLE
    setLineSegment( tryInt(    prefs,  key[2].key,      key[2].dflt ) ); // DISTOX_LINE_SEGMENT
    mLineClose     = prefs.getBoolean( key[3].key, bool(key[3].dflt) );  // DISTOX_LINE_CLOSE
    mArrowLength   = tryFloat( prefs,  key[4].key,      key[4].dflt );   // DISTOX_ARROW_LENGTH
    mAutoSectionPt = prefs.getBoolean( key[5].key, bool(key[5].dflt) );  // DISTOX_AUTO_SECTION_PT
    mAreaBorder    = prefs.getBoolean( key[6].key, bool(key[6].dflt) );  // DISTOX_AREA_BORDER
    mUnitLines     = tryFloat( prefs,  key[7].key,      key[7].dflt );   // DISTOX_LINE_UNITS
    mSlopeLSide    = tryInt(   prefs,  key[8].key,      key[8].dflt );   // DISTOX_SLOPE_LSIDE
    // mContinueLine  = tryInt(   prefs,  key[7].key,      key[7].dflt );   // DISTOX_LINE_CONTINUE
    // mWithLineJoin  = prefs.getBoolean( key[8].key, bool(key[8].dflt) );  // DISTOX_WITH_CONTINUE_LINE

    key = TDPrefKey.mPoint;
    mUnscaledPoints = prefs.getBoolean( key[0].key, bool(key[0].dflt) ); // DISTOX_UNSCALED_POINTS
    mUnitIcons     = tryFloat( prefs,   key[1].key,      key[1].dflt );       // DISTOX_DRAWING_UNIT 
    mLabelSize     = tryFloat( prefs,   key[2].key,      key[2].dflt );       // DISTOX_LABEL_SIZE
    mScalableLabel = prefs.getBoolean(  key[3].key, bool(key[3].dflt) ); // DISTOX_SCALABLE_LABEL
    mXSectionOffset = tryInt( prefs,    key[4].key,      key[4].dflt );       // DISTOX_XSECTION_OFFSET
    // FIXME tis should go in SCREEN

    // AUTOWALLS
    // key = TDPrefKey.mWalls;
    // mWallsType        = tryInt(   prefs, key[0].key, key[0].dflt ); // DISTOX_WALLS_TYPE choice: 0, 1
    // mWallsPlanThr     = tryFloat( prefs, key[1].key, key[1].dflt ); // DISTOX_WALLS_PLAN_THR
    // mWallsExtendedThr = tryFloat( prefs, key[2].key, key[2].dflt ); // DISTOX_WALLS_EXTENDED_THR
    // mWallsXClose      = tryFloat( prefs, key[3].key, key[3].dflt ); // DISTOX_WALLS_XCLOSE
    // mWallsXStep       = tryFloat( prefs, key[4].key, key[4].dflt ); // DISTOX_WALLS_XSTEP
    // mWallsConcave     = tryFloat( prefs, key[5].key, key[5].dflt ); // DISTOX_WALLS_CONCAVE

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
    TDLog.v("SETTINGS update cat " + cat + " pref " + k + " val " + v );
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
      case TDPrefCat.PREF_CATEGORY_PLY:    return updatePrefPly( hlp, k, v );
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
    TDPrefKey[] key = TDPrefKey.mMain;
    // TDLog.v("update pref main: " + k );
    // if ( k.equals( key[0] ) ) {// DISTOX_CWD
    //   // handled independently
    //   // TopoDroidApp.setCWD( tryStringValue( hlp, k, v, "TopoDroid" ), hlp.getString( "DISTOX_CBD", TDPath.getBaseDir() ) );
    //   TopoDroidApp.setCWD( tryStringValue( hlp, k, v, "TopoDroid" ) /* , TDPath.getCurrentBaseDir() */ );
    // } else 
    if ( k.equals( key[ 0 ].key ) ) {              // DISTOX_TEXT_SIZE
      ret = setTextSize( tryIntValue( hlp, k, v, defaultTextSize ) );
    } else if ( k.equals( key[ 1 ].key ) ) {              // DISTOX_SIZE_BUTTONS (choice)
      if ( setSizeButtons( tryIntValue( hlp, k, v, defaultButtonSize ) ) ) {
        TopoDroidApp.resetButtonBar();
      }
    } else if ( k.equals( key[ 2 ].key ) ) {             // DISTOX_SYMBOL_SIZE
      ret = setSymbolSize( tryFloatValue( hlp, k, v, defaultSymbolSize ) );
    } else if ( k.equals( key[ 3 ].key ) ) {             // DISTOX_EXTRA_BUTTONS (choice)
      int level = tryIntValue( hlp, k, v, key[3].dflt );
      setActivityBooleans( hlp.getSharedPrefs(), level );
    } else if ( k.equals( key[ 4 ].key ) ) {           // DISTOX_LOCAL_MAN (choice)
      // TDLog.v("SETTING handle local man pages - key " + k + " default " + def[6] );
      mLocalManPages = handleLocalUserMan( /* hlp.getApp(), */ tryStringValue( hlp, k, v, key[4].dflt ), true );
    } else if ( k.equals( key[ 5 ].key ) ) {           // DISTOX_LOCALE (choice)
      setLocale( tryStringValue( hlp, k, v, key[5].dflt ), true );
    } else if ( k.equals( key[ 6 ].key ) ) {           // DISTOX_ORIENTATION (choice)
      mOrientation = tryIntValue( hlp, k, v, key[6].dflt );
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
    TDPrefKey[] key = TDPrefKey.mSurvey;
    String ret = null;
    if ( k.equals( key[ 0 ].key ) ) {        // DISTOX_TEAM (arbitrary)
      mDefaultTeam = tryStringValue( hlp, k, v, key[0].dflt );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_TEAM_DIALOG (bool)
      mTeamNames = tryIntValue( hlp, k, v, key[1].dflt ); 
      // TDLog.v("SETTINGS TEAM Names " + mTeamNames );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_SURVEY_STATION (choice)
      parseStationPolicy( hlp, tryStringValue( hlp, k, v, key[2].dflt ) );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_STATION_NAMES (choice)
      mStationNames = (tryStringValue( hlp, k, v, key[3].dflt).equals("number"))? 1 : 0;
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_INIT_STATION 
      String s = tryStringValue( hlp, k, v, key[4].dflt );
      if ( ! TDString.hasSpecials( s ) ) {
        s = TDString.noSpacesAndSpecials( s );
        if ( TDUtil.isStationName( s ) ) {
          // TDLog.v("Valid station name <" + s + ">" );
          mInitStation = TDString.isNullOrEmpty( s )? key[4].dflt : s;
          DistoXStationName.setInitialStation( mInitStation );
        // } else {
        //   TDLog.v("Invalid station name <" + s + ">" );
        }
        if ( ! mInitStation.equals( v ) ) { ret = mInitStation; }
      }
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_THUMBNAIL
      mThumbSize = tryIntValue( hlp, k, v, key[5].dflt ); 
      if ( mThumbSize < 80 )       { mThumbSize = 80;  ret = Integer.toString( mThumbSize ); }
      else if ( mThumbSize > 400 ) { mThumbSize = 400; ret = Integer.toString( mThumbSize ); }
    // } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_EDITABLE_STATIONS (bool)
    //   mEditableStations = tryBooleanValue( hlp, k, v, bool(key[6].dflt) );
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_FIXED_ORIGIN (bool)
      mFixedOrigin = tryBooleanValue( hlp, k, v, bool(key[6].dflt) );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_SHARED_XSECTIONS (bool)
      mSharedXSections  = tryBooleanValue( hlp, k, v, bool(key[7].dflt) );
    // } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_DATA_BACKUP (bool)
    //   mDataBackup = tryBooleanValue( hlp, k, v, bool(key[8].dflt) );
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
    TDPrefKey[] key = TDPrefKey.mPlot;
    // if ( k.equals( key[ 0 ].key ) ) {        // DISTOX_PICKER_TYPE (choice)
    //   mPickerType = tryIntValue(   hlp, k, v, key[0].dflt );
    //   if ( mPickerType < PICKER_LIST ) mPickerType = PICKER_LIST;
    // } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_RECENT_NR (choice)
    //   mRecentNr = tryIntValue( hlp, k, v, key[1].dflt );
    // } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_TRIPLE_TOOLBAR (bool)
    //   mTripleToolbar = tryBooleanValue( hlp, k, v, bool(key[1].dflt) );
    //   TopoDroidApp.setToolsToolbars();
    // } else 
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_SIDE_DRAG (bool)
      mSideDrag = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_ZOOM_CTRL (choice)
      // setZoomControls( tryBooleanValue( hlp, k, bool(key[1]) ) );
      setZoomControls( tryStringValue( hlp, k, v, key[1].dflt ), TDandroid.checkMultitouch( TDInstance.context ) );
    // } else if ( k.equals( key[ ? ] ) ) {  // DISTOX_SECTION_STATIONS
    //   mSectionStations = tryIntValue( hlp, k, v, key[ ].dflt );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_HTHRESHOLD
      mHThreshold = tryFloatValue( hlp, k, v, key[2].dflt );
      if ( mHThreshold <  0 ) { mHThreshold =  0; ret = TDString.ZERO; }
      if ( mHThreshold > 90 ) { mHThreshold = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_CHECK_ATTACHED (bool)
      mCheckAttached = tryBooleanValue( hlp, k, v, bool(key[3].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_CHECK_EXTEND (bool)
      mCheckExtend   = tryBooleanValue( hlp, k, v, bool(key[4].dflt) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_TOOLBAR_SIZE
      mItemButtonSize = tryFloatValue( hlp, k, v, key[5].dflt );
      TopoDroidApp.setToolsToolbarParams();
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_PLOT_CACHE
      mPlotCache = tryBooleanValue( hlp, k, v, bool(key[6].dflt) );
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
    TDPrefKey[] key = TDPrefKey.mCalib;
    if ( k.equals( key[ 0 ].key ) ) {
      mGroupBy       = tryIntValue(   hlp, k, v, key[0].dflt );  // DISTOX_GROUP_BY (choice)
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_GROUP_DISTANCE
      mGroupDistance = tryFloatValue( hlp, k, v, key[1].dflt );
      if ( mGroupDistance < 0 ) { mGroupDistance = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_CALIB_EPS
      mCalibEps      = tryFloatValue( hlp, k, v, key[2].dflt );
      if ( mCalibEps < 0.000001f ) { mCalibEps = 0.000001f; ret = "0.000001"; }
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_CALIB_MAX_IT
      mCalibMaxIt    = tryIntValue(   hlp, k, v, key[3].dflt );
      if ( mCalibMaxIt < 10 ) { mCalibMaxIt = 10; ret = TDString.TEN; }
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_CALIB_SHOT_DOWNLOAD (bool)
      mCalibShotDownload = tryBooleanValue( hlp, k, v, bool(key[4].dflt) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_RAW_CDATA
      // mRawData       = tryBooleanValue( hlp, k, v, false );  // DISTOX_RAW_DATA (choice)
      mRawCData      = tryIntValue( hlp, k, v, key[5].dflt ); 
    // } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_CALIB_ALGO (choice)
    //   mCalibAlgo     = tryIntValue( hlp, k, v, key[6].dflt );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_ALGO_MIN_ALPHA
      mAlgoMinAlpha   = tryFloatValue( hlp, k, v, key[7].dflt );
      if ( mAlgoMinAlpha < 0 ) { mAlgoMinAlpha = 0; ret = TDString.ZERO; }
      if ( mAlgoMinAlpha > 1 ) { mAlgoMinAlpha = 1; ret = TDString.ONE; }
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_ALGO_MIN_BETA
      mAlgoMinBeta    = tryFloatValue( hlp, k, v, key[8].dflt );
      if ( mAlgoMinBeta  < 0 ) { mAlgoMinBeta  = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_ALGO_MIN_GAMMA
      mAlgoMinGamma   = tryFloatValue( hlp, k, v, key[9].dflt );
      if ( mAlgoMinGamma < 0 ) { mAlgoMinGamma = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 10 ].key ) ) { // DISTOX_ALGO_MIN_DELTA
      mAlgoMinDelta   = tryFloatValue( hlp, k, v, key[10].dflt ); 
      if ( mAlgoMinDelta < -10 ) { mAlgoMinDelta = -10; ret = "-10"; }
    } else if ( k.equals( key[ 11 ].key ) ) { // DISTOX_AUTO_CAL_BETA
      mAutoCalBeta   = tryFloatValue( hlp, k, v, key[11].dflt ); 
      if ( mAutoCalBeta < 0.001f ) { mAutoCalBeta = 0.001f; ret = "0.001"; }
      if ( mAutoCalBeta > 0.999f ) { mAutoCalBeta = 0.999f; ret = "0.999"; }
    } else if ( k.equals( key[ 12 ].key ) ) { // DISTOX_AUTO_CAL_ETA
      mAutoCalEta   = tryFloatValue( hlp, k, v, key[12].dflt ); 
      if ( mAutoCalEta < 0.01f ) { mAutoCalEta = 0.01f; ret = "0.01"; }
      if ( mAutoCalEta > 0.99f ) { mAutoCalEta = 0.99f; ret = "0.99"; }
    } else if ( k.equals( key[ 13 ].key ) ) { // DISTOX_AUTO_CAL_GAMMA
      mAutoCalGamma   = tryFloatValue( hlp, k, v, key[13].dflt ); 
      if ( mAutoCalGamma < 0.01f ) { mAutoCalGamma = 0.01f; ret = "0.01"; }
      if ( mAutoCalGamma > 0.99f ) { mAutoCalGamma = 0.99f; ret = "0.99"; }
    } else if ( k.equals( key[ 14 ].key ) ) { // DISTOX_AUTO_CAL_DELTA
      mAutoCalDelta   = tryFloatValue( hlp, k, v, key[14].dflt ); 
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
    TDPrefKey[] key = TDPrefKey.mDevice;
    // DISTOX_DEVICE, unused here
    // DISTOX_DEVICE_TYPE, unused here
    if ( k.equals( key[ 0 ].key ) ) {        // DISTOX_BLUETOOTH (choice)
      mCheckBT  = tryIntValue( hlp, k, v, key[0].dflt ); 
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_CONN_MODE (choice)
      mConnectionMode = tryIntValue( hlp, k, v, key[1].dflt ); 
    // } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_AUTO_RECONNECT (bool)
    //   mAutoReconnect = tryBooleanValue( hlp, k, v, bool(key[2]) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_HEAD_TAIL (bool)
      mHeadTail = tryBooleanValue( hlp, k, v, bool(key[2].dflt) ); 
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_SOCKET_TYPE (choice)
      mSockType = tryIntValue( hlp, k, v, key[3].dflt ); // mDefaultSockStrType ); 
    // } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_Z6_WORKAROUND (bool)
    //   mZ6Workaround = tryBooleanValue( hlp, k, v, bool(key[4]) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_AUTO_PAIR (bool)
      mAutoPair = tryBooleanValue( hlp, k, v, bool(key[4].dflt) );
      TopoDroidApp.checkAutoPairing();
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_CONNECT_FEEDBACK
      mConnectFeedback = tryIntValue( hlp, k, v, key[6].dflt );
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
    TDPrefKey[] key = TDPrefKey.mCave3D;
    if ( k.equals( key[0].key ) ) {  // CAVE3D_NEG_CLINO
      boolean b = tryBooleanValue( hlp, k, v, bool(key[0].dflt) ); 
      GlRenderer.mMinClino = b ? 90 : 0;
    } else if ( k.equals( key[1].key ) ) { // CAVE3D_STATION_POINTS
      GlModel.mStationPoints = tryBooleanValue( hlp, k, v, bool(key[1].dflt) );
    } else if ( k.equals( key[2].key ) ) { // CAVE3D_STATION_POINT_SIZE def=8
      GlNames.setPointSize( tryIntValue( hlp, k, v, key[2].dflt ) );
    } else if ( k.equals( key[3].key ) ) { // CAVE3D_STATION_TEXT_SIZE def=20
      GlNames.setTextSize( tryIntValue( hlp, k, v, key[3].dflt ) );
    } else if ( k.equals( key[4].key ) ) { // CAVE3D_SELECTION_RADIUS
      float radius = tryFloatValue( hlp, k, v, key[4].dflt );
      if ( radius > 10.0f ) TopoGL.mSelectionRadius = radius;
    } else if ( k.equals( key[5].key ) ) { // CAVE3D_MEASURE_DIALOG
      TopoGL.mMeasureToast = tryBooleanValue( hlp, k, v, bool(key[5].dflt) ); 
    } else if ( k.equals( key[6].key ) ) { // CAVE3D_STATION_TOAST
      TopoGL.mStationDialog = tryBooleanValue( hlp, k, v, bool(key[6].dflt) ); 
    } else if ( k.equals( key[7].key ) ) { // CAVE3D_GRID_ABOVE
      GlModel.mGridAbove = tryBooleanValue( hlp, k, v, bool(key[7].dflt) ); 
    } else if ( k.equals( key[8].key ) ) { // CAVE3D_GRID_EXTENT
      int extent = tryIntValue( hlp, k, v, key[8].dflt ); 
      if ( extent > 1 && extent < 100 ) GlModel.mGridExtent = extent;
    } else if ( k.equals( key[9].key ) ) { // CAVE3D_NAMES_VISIBILITY
      GlNames.mNamesVisible = tryBooleanValue( hlp, k, v, bool(key[9].dflt) ); 
    } else {
      TDLog.e("missing Cave3D key: " + k );
    }
    return ret;
  }

  private static String updatePrefDem3D( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref dem3d: " + k );
    TDPrefKey[] key = TDPrefKey.mDem3D;
    if ( k.equals( key[0].key ) ) { 
      float buffer = tryFloatValue( hlp, k, v, key[0].dflt );
      if ( buffer >= 0 )  TopoGL.mDEMbuffer = buffer;
    } else if ( k.equals( key[1].key ) ) { 
      int size = tryIntValue( hlp, k, v, key[1].dflt ); 
      if ( size >= 50 )  TopoGL.mDEMmaxsize = size;
    } else if ( k.equals( key[2].key ) ) { 
      int reduce = tryIntValue( hlp, k, v, key[2].dflt ); 
      if ( reduce == 1 ) TopoGL.mDEMreduce = TopoGL.DEM_SHRINK;
      else               TopoGL.mDEMreduce = TopoGL.DEM_CUT;
    } else if ( k.equals( key[3].key ) ) {
      if ( TDString.isNullOrEmpty( v ) ) {
        TopoGL.mTextureRoot = key[3].dflt;
      } else {
        TopoGL.mTextureRoot = v;
      }
      hlp.update( k, TopoGL.mTextureRoot );
    } else {
      TDLog.e("missing DEM-3D key: " + k );
    }
    return ret;
  }

  private static String updatePrefWalls3D( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref walls3d: " + k );
    TDPrefKey[] key = TDPrefKey.mWalls3D;
    if ( k.equals( key[0].key ) ) { // CAVE3D_SPLAY_USE
      TglParser.mSplayUse = tryIntValue( hlp, k, v, key[0].dflt ); 
    } else if ( k.equals( key[1].key ) ) { // CAVE3D_ALL_SPLAY
      GlModel.mAllSplay = tryBooleanValue( hlp, k, v, bool(key[1].dflt) ); 
    } else if ( k.equals( key[2].key ) ) { // CAVE3D_SPLAY_PROJ
      TopoGL.mSplayProj = tryBooleanValue( hlp, k, v, bool(key[2].dflt) ); 
    } else if ( k.equals( key[3].key ) ) { // CAVE3D_SPLAY_THR
      TopoGL.mSplayThr = tryFloatValue( hlp, k, v, key[3].dflt );
    } else if ( k.equals( key[4].key ) ) {  // CAVE3D_SPLIT_TRIANGLES
      GlModel.mSplitTriangles = tryBooleanValue( hlp, k, v, bool(key[4].dflt) ); 
    } else if ( k.equals( key[5].key ) ) {  // CAVE3D_SPLIT_RANDOM
      float r = tryFloatValue( hlp, k, v, key[5].dflt );
      if ( r > 0.0001f ) {
        GlModel.mSplitRandomizeDelta = r;
        GlModel.mSplitRandomize = true;
      } else {
        GlModel.mSplitRandomize = false;
      }
    } else if ( k.equals( key[6].key ) ) { // CAVE3D_SPLIT_STRETCH
      float r = tryFloatValue( hlp, k, v, key[6].dflt );
      if ( r > 0.0001f ) {
        GlModel.mSplitStretchDelta = r;
        GlModel.mSplitStretch = true;
      } else {
        GlModel.mSplitStretch = false;
      }
    } else if ( k.equals( key[7].key ) ) { // CAVE3D_POWERCRUST_DELTA 
      float delta = tryFloatValue( hlp, k, v, key[7].dflt );
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
    TDPrefKey[] key = TDPrefKey.mGeek;
    if ( k.equals( key[0].key ) ) {
      mSingleBack = tryBooleanValue( hlp, k, v, bool(key[0].dflt) ); // DISTOX_SINGLE_BACK
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_HIDE_NAVBA
      setHideNavBar( tryBooleanValue( hlp, k, v, bool(key[1].dflt) ) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_PALETTES
      setPalettes( tryBooleanValue( hlp, k, v, bool(key[1].dflt) ) );
    // } else if ( k.equals( key[1] ) ) { // CLEAR_BACKUPS
    //   setBackupsClear( tryBooleanValue( hlp, k, v, bool(key[1].dflt) ) ); // DISTOX_BACKUPS_CLEAR
    } else if ( k.equals( key[ 3 ].key ) ) {           // DISTOX_MKEYBOARD (bool)
      mKeyboard = tryBooleanValue( hlp, k, v, bool(key[3].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) {           // DISTOX_NO_CURSOR(bool)
      mNoCursor = tryBooleanValue( hlp, k, v, bool(key[4].dflt) );
    } else if ( k.equals( key[ 5 ].key ) ) {
      mBulkExport = tryBooleanValue( hlp, k, v, bool(key[5].dflt) );
    } else if ( k.equals( key[ 5 ].key ) ) {
      mPacketLog = tryBooleanValue( hlp, k, v, bool(key[6].dflt) ); // DISTOX_PACKET_LOGGER
    } else if ( k.equals( key[ 7 ].key ) ) {
      mTh2Edit = tryBooleanValue( hlp, k, v, bool(key[7].dflt) ); // DISTOX_TH2_EDIT
      mMainFlag |= FLAG_BUTTON;
    } else if ( TDLevel.isDebugBuild() && k.equals( key[15].key ) ) {
      mWithDebug =  tryBooleanValue( hlp, k, v, bool(key[15].dflt) ); // DISTOX_WITH_DEBUG
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
    TDPrefKey[] key = TDPrefKey.mGeekShot;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_DIVING_MODE
      mDivingMode   = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_TAMPERING
      mEditableShots = tryBooleanValue( hlp, k, v, bool(key[1].dflt) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_BACKSIGHT_SPLAY
      mBacksightSplay = tryBooleanValue( hlp, k, v, bool(key[2].dflt) );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_RECENT_SHOT
      mShotRecent   = tryBooleanValue( hlp, k, v, bool(key[3].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_RECENT_TIMEOUT
      mRecentTimeout = tryIntValue( hlp, k, v, key[4].dflt );
      if ( mRecentTimeout < 0 ) { mRecentTimeout = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_EXTEND_FRAC
      mExtendFrac   = tryBooleanValue( hlp, k, v, bool(key[ 5].dflt) );
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_BACKSHOT (bool)
      mDistoXBackshot = tryBooleanValue( hlp, k, v, bool(key[6].dflt) );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_BEDDING
      mBedding      = tryBooleanValue( hlp, k, v, bool(key[ 7].dflt) );
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_WITH_SENSORS
      mWithSensors  = tryBooleanValue( hlp, k, v, bool(key[ 8].dflt) );
    } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_LOOP_CLOSURE_VALUE
      setLoopClosure( tryIntValue( hlp, k, v, key[ 9].dflt ) );
    } else if ( k.equals( key[10 ].key ) ) { // DISTOX_LOOP_THR
      mLoopThr = tryFloatValue( hlp, k, v, key[10].dflt );
    } else if ( k.equals( key[11 ].key ) ) { // DISTOX_ANDROID_AZIMUTH
      mWithAzimuth  = tryBooleanValue( hlp, k, v, bool(key[11].dflt) );
    } else if ( k.equals( key[12  ].key ) ) { // DISTOX_SHOT_TIMER [3 ..)
      mTimerWait        = tryIntValue( hlp, k, v, key[12].dflt );
      if ( mTimerWait < 0 ) { mTimerWait = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 13 ].key ) ) { // DISTOX_BEEP_VOLUME [0 .. 100]
      ret = setBeepVolume( tryIntValue( hlp, k, v, key[13].dflt ) );
    } else if ( k.equals( key[ 14 ].key ) ) { // DISTOX_BLUNDER_SHOT
      mBlunderShot = tryBooleanValue( hlp, k, v, bool(key[14].dflt) );
    } else if ( k.equals( key[ 15 ].key ) ) { // DISTOX_SPLAY_STATION 
      mSplayStation = tryBooleanValue( hlp, k, v, bool(key[15].dflt) );
    } else if ( k.equals( key[ 16 ].key ) ) { // DISTOX_SPLAY_GROUP
      mSplayOnlyForward = tryBooleanValue( hlp, k, v, bool(key[16].dflt) );
    // } else if ( k.equals( key[13 ].key ) ) { // DISTOX_TDMANAGER
    //   mWithTdManager = tryBooleanValue( hlp, k, v, bool(key[13].dflt) );

    // } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_DIST_TOLERANCE
    //   mDistTolerance = tryFloatValue( hlp, k, v, key[ 9].dflt  );
    // } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_SPLAY_ACTIVE
    //   mSplayActive  = prefs.getBoolean( key[ 9],  bool(key[ 9].dflt) );
    // } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_WITH_RENAME
    //   mWithRename   = tryBooleanValue( hlp, k, v, bool(key[ 9].dflt) );
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
    TDPrefKey[] key = TDPrefKey.mGeekPlot;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_PLOT_SHIFT
      mPlotShift    = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_PLOT_SPLIT
      mPlotSplit = tryBooleanValue( hlp, k, v, bool(key[ 1 ].dflt) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_STYLUS_SIZE
      setStylusSize( tryFloatValue( hlp, k, v, key[ 2].dflt ) ); // STYLUS_MM - LINE ENDS
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_BACKUP_NUMBER
      mBackupNumber  = tryIntValue( hlp, k, v, key[ 3 ].dflt ); 
      if ( mBackupNumber <  4 ) { mBackupNumber =  4; ret = Integer.toString( mBackupNumber ); }
      else if ( mBackupNumber > 10 ) { mBackupNumber = 10; ret = Integer.toString( mBackupNumber ); }
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_BACKUP_INTERVAL
      mBackupInterval = tryIntValue( hlp, k, v, key[ 4 ].dflt );  
      if ( mBackupInterval <  10 ) { mBackupInterval =  10; ret = Integer.toString( mBackupInterval ); }
      else if ( mBackupInterval > 600 ) { mBackupInterval = 600; ret = Integer.toString( mBackupInterval ); }
    // } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_AUTO_XSECTIONS
    //   mAutoXSections = tryBooleanValue( hlp, k, v, bool(key[ 5 ].dflt) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_SAVED_STATIONS
      mSavedStations = tryBooleanValue( hlp, k, v, bool(key[ 5 ].dflt) );
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_LEGONLY_UPDATE
      mLegOnlyUpdate = tryBooleanValue( hlp, k, v, bool(key[ 6 ].dflt) );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_FULL_AFFINE
      mFullAffine    = tryBooleanValue( hlp, k, v, bool(key[ 7 ].dflt) );
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_WITH_LEVELS
      mWithLevels    = tryIntValue( hlp, k, v, key[ 8 ].dflt );
    // } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_GRAPH_PAPER_SCALE - handled by a dialog
    //   mGraphPaperScale = tryIntValue( hlp, k, v, key[ 9 ].dflt );
    } else if ( k.equals( key[10 ].key ) ) { // DISTOX_SLANT_XSECTION
      mSlantXSection = tryBooleanValue( hlp, k, v, bool(key[10 ].dflt) );
    } else if ( k.equals( key[11 ].key ) ) { // DISTOX_OBLIQUE_PROJECTED
      mObliqueMax = tryIntValue( hlp, k, v, key[ 11 ].dflt );
      if ( mObliqueMax < 10 )  { mObliqueMax = 0; ret = Integer.toString( mObliqueMax ); }
      else if ( mObliqueMax > 80 ) { mObliqueMax = 80; ret = Integer.toString( mObliqueMax ); }
    } else if ( k.equals( key[12 ].key ) ) { // DISTOX_LINE_ENDS
      mLineEnds = tryIntValue( hlp, k, v, key[12 ].dflt );
    // } else if ( k.equals( key[13 ] ) ) {  // DISTOX_ZOOM_LOWER_BOUND
    //   mZoomLowerBound = tryFloatValue( hlp, k, v, key[13] );  // DISTOX_ZOOM_LOWER_BOUND
    //   if ( mZoomLowerBound < 0.0f ) mZoomLowerBound = 0.0f;
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
    TDPrefKey[] key = TDPrefKey.mGeekSplay;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_SPLAY_CLASSES
      mSplayClasses = tryBooleanValue( hlp, k, v, bool(key[ 0].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_DISCRETE_COLORS was DISTOX_SPLAY_COLOR
      // mSplayColor   = tryBooleanValue( hlp, k, v, bool(key[ 1]) );
      mDiscreteColors = tryIntValue( hlp, k, v, key[ 1].dflt );
      mSplayColor = (mDiscreteColors > 0);
    // } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_SPLAY_AS_DOT
    //   mSplayAsDot = tryBooleanValue( hlp, k, v, bool(key[ 2]) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_SPLAY_VERT_THRS
      mSplayVertThrs = tryFloatValue( hlp, k, v, key[ 2].dflt );
      if ( mSplayVertThrs <  0 ) { mSplayVertThrs =  0; ret = TDString.ZERO; }
      if ( mSplayVertThrs > 91 ) { mSplayVertThrs = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_SPLAY_DASH (0,1,2)
      mDashSplay = tryIntValue( hlp, k, v, key[ 3].dflt );      
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_VERT_SPLAY
      mVertSplay   = tryFloatValue( hlp, k, v, key[ 4].dflt );
      if ( mVertSplay <  0 ) { mVertSplay =  0; ret = TDString.ZERO; }
      if ( mVertSplay > 91 ) { mVertSplay = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_HORIZ_SPLAY
      mHorizSplay  = tryFloatValue( hlp, k, v, key[ 5].dflt );
      if ( mHorizSplay <  0 ) { mHorizSplay =  0; ret = TDString.ZERO; }
      if ( mHorizSplay > 91 ) { mHorizSplay = 91; ret = TDString.NINETYONE; }
      mCosHorizSplay = TDMath.cosd( mHorizSplay );
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_SECTION_SPLAY
      mSectionSplay = tryFloatValue( hlp, k, v, key[ 6].dflt );
      if ( mSectionSplay <  0 ) { mSectionSplay =  0; ret = TDString.ZERO; }
      if ( mSectionSplay > 91 ) { mSectionSplay = 91; ret = TDString.NINETYONE; }
      mCosSectionSplay  = TDMath.cosd( mSectionSplay );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_SPLAY_DASH_COLOR
      mSplayDashColor = tryColorValue( hlp, k, v, key[ 7].dflt ); 
      BrushManager.setSplayDashColor( mSplayDashColor );
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_SPLAY_DOT_COLOR
      mSplayDotColor = tryColorValue( hlp, k, v, key[ 8].dflt ); 
      BrushManager.setSplayDotColor( mSplayDotColor );
    } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_SPLAY_LATEST_COLOR
      mSplayLatestColor = tryColorValue( hlp, k, v, key[ 9].dflt ); 
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
    TDPrefKey[] key = TDPrefKey.mGeekDevice;
    int j = 0;
    if ( k.equals( key[ ++j ].key ) ) { // DISTOX_UNNAMED_DEVICE 
      mUnnamedDevice = tryBooleanValue( hlp, k, v, bool(key[j].dflt) ); // BT_NONAME
    } else if ( k.equals( key[ ++j ].key ) ) { // DISTOX_SOCKET_DELAY index 1
      mConnectSocketDelay = tryIntValue( hlp, k, v, key[j].dflt );  
      if ( mConnectSocketDelay < 0  ) { mConnectSocketDelay =  0; ret = TDString.ZERO; }
      if ( mConnectSocketDelay > 60 ) { mConnectSocketDelay = 60; ret = TDString.SIXTY; } // was 100
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_SECOND_DISTOX (bool)
      mSecondDistoX = tryBooleanValue( hlp, k, v, bool(key[j].dflt) );
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_WAIT_DATA
      mWaitData = tryIntValue( hlp, k, v, key[j].dflt ); 
      if ( mWaitData <    0 ) { mWaitData =    0; ret = Integer.toString( mWaitData ); }
      if ( mWaitData > 2000 ) { mWaitData = 2000; ret = Integer.toString( mWaitData ); }
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_WAIT_CONN
      mWaitConn = tryIntValue( hlp, k, v, key[j].dflt );
      if ( mWaitConn <   50 ) { mWaitConn =   50; ret = Integer.toString( mWaitConn ); }
      if ( mWaitConn > 2000 ) { mWaitConn = 2000; ret = Integer.toString( mWaitConn ); }
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_WAIT_LASER
      mWaitLaser = tryIntValue( hlp, k, v, key[j].dflt );
      if ( mWaitLaser <  500 ) { mWaitLaser =  500; ret = Integer.toString( mWaitLaser ); }
      if ( mWaitLaser > 5000 ) { mWaitLaser = 5000; ret = Integer.toString( mWaitLaser ); }
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_WAIT_SHOT
      mWaitShot  = tryIntValue( hlp, k, v, key[j].dflt );
      if ( mWaitShot <   500 ) { mWaitShot =   500; ret = Integer.toString( mWaitShot ); }
      if ( mWaitShot > 10000 ) { mWaitShot = 10000; ret = Integer.toString( mWaitShot ); }
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_FIRMWARE_SANITY
      mFirmwareSanity = tryBooleanValue( hlp, k, v, bool(key[j].dflt) );
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_BRIC_MODE
      mBricMode = tryIntValue( hlp, k, v, key[j].dflt );
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_BRIC_ZERO_LENGTH
      mBricZeroLength = tryBooleanValue( hlp, k, v, bool(key[j].dflt) );
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_BRIC_INDEX_IS_ID
      mBricIndexIsId = tryBooleanValue( hlp, k, v, bool(key[j].dflt) );
    } else if ( k.equals( key[ ++j].key ) ) { // DISTOX_SAP5_BIT16_BUG
      mSap5Bit16Bug = tryBooleanValue( hlp, k, v, bool(key[j].dflt) );
    } else {
      TDLog.e("missing DEVICE key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefGeekLine( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    TDPrefKey[] key = TDPrefKey.mGeekLine;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_REDUCE_ANGLE
      ret = setReduceAngle( tryFloatValue(  hlp, k, v, key[0].dflt ) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_LINE_ACCURACY
      ret = setLineAccuracy( tryFloatValue( hlp, k, v, key[1].dflt ) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_LINE_CORNER
      ret = setLineCorner( tryFloatValue(   hlp, k, v, key[2].dflt ) );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_WEED_DISTANCE
      ret = setWeedDistance( tryFloatValue(   hlp, k, v, key[3].dflt ) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_WEED_LENGTH
      ret = setWeedLength( tryFloatValue(   hlp, k, v, key[4].dflt ) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_WEED_BUFFER
      ret = setWeedBuffer( tryFloatValue(   hlp, k, v, key[5].dflt ) );
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_LINE_SNAP (bool)
      mLineSnap = tryBooleanValue(          hlp, k, v, bool(key[6].dflt) );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_LINE_CURVE (bool)
      mLineCurve = tryBooleanValue(         hlp, k, v, bool(key[7].dflt) );
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_LINE_STRAIGHT
      mLineStraight = tryBooleanValue(      hlp, k, v, bool(key[8].dflt) );
    } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_PATH_MULTISELECT (bool)
      mPathMultiselect = tryBooleanValue(   hlp, k, v, bool(key[9].dflt) );
    // } else if ( k.equals( key[10 ] ) ) { // DISTOX_COMPOSITE_ACTIONS (bool)
    //   mCompositeActions = tryBooleanValue(  hlp, k, v, bool(key[10].dflt) );

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
    TDPrefKey[] key = TDPrefKey.mExportImport;
    if ( k.equals( key[ 0 ].key ) ) {        // DISTOX_PT_CMAP
      // not handled here
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_SPLAY_EXTEND (bool)
      mLRExtend = tryBooleanValue( hlp, k, v, bool(key[ 1].dflt) ); 
    } else {
      TDLog.e("missing IMPORT key: " + k );
    }
    return null;
  }

  private static String updatePrefExportEnable( TDPrefHelper hlp, String k, String v )
  {
    TDPrefKey[] key = TDPrefKey.mExportEnable;
    for ( int n = 0; n < key.length; ++ n ) {
      if ( k.equals( key[n].key ) ) {
        boolean b = tryBooleanValue( hlp, k, v, bool(key[n].dflt) ); 
        TDConst.mSurveyExportEnable[ 1 + n ] = b;
        return b ? TDPrefKey.TRUE : TDPrefKey.FALSE;
      }
    }
    return null;
  }

  private static String updatePrefGeekImport( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref import: " + k );
    TDPrefKey[] key = TDPrefKey.mExport;
    if ( k.equals( key[ 0 ].key ) ) {        // DISTOX_ZIP_WITH_SYMBOLS
      mZipWithSymbols = tryBooleanValue( hlp, k, v, bool(key[ 0].dflt) ); 
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_IMPORT_DATAMODE (choice)
      mImportDatamode = tryIntValue( hlp, k, v, key[ 1].dflt );
    } else if ( k.equals( key[ 2 ].key ) ) {        // DISTOX_AUTO_XSECTIONS
      mAutoXSections = tryBooleanValue( hlp, k, v, bool(key[ 2].dflt) ); 
    } else if ( k.equals( key[ 3 ].key ) ) {        // DISTOX_AUTO_STATIONS
      mAutoStations = tryBooleanValue( hlp, k, v, bool(key[ 3].dflt) ); 
    } else if ( k.equals( key[ 4 ].key ) ) {        // DISTOX_LRUD_COUNT
      mLRUDcount = tryBooleanValue( hlp, k, v, bool(key[ 4].dflt) );
    } else if ( k.equals( key[ 5 ].key ) ) {        // DISTOX_ZIP_SHARE_CATEGORY
      mZipShareCategory = tryBooleanValue( hlp, k, v, bool(key[ 5].dflt) );
    // } else if ( k.equals( key[ 4 ].key ) ) {        // DISTOX_AUTO_PLOT_EXPORT moved to EXPORT
    //   mAutoExportPlotFormat = tryIntValue( hlp, k, v, key[ 4].dflt );
    // } else if ( k.equals( key[ 2 ].key ) ) {        // DISTOX_TRANSFER_CSURVEY
    //   mExportTcsx = tryBooleanValue( hlp, k, v, bool(key[ 2].dflt) ); 
    } else {
      TDLog.e("missing GEEK_IMPORT key: " + k );
    }
    return null;
  }
    
  private static String updatePrefExport( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref export: " + k );
    TDPrefKey[] key = TDPrefKey.mExport;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_EXPORT_SHOTS (choice)
      mExportShotsFormat = tryIntValue( hlp, k, v, key[ 0].dflt );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_EXPORT_PLOT (choice)
      mExportPlotFormat = tryIntValue( hlp, k, v, key[ 1].dflt );
    } else if ( k.equals( key[ 2 ].key ) ) {        // DISTOX_AUTO_PLOT_EXPORT
      mAutoExportPlotFormat = tryIntValue( hlp, k, v, key[ 2].dflt );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_ORTHO_LRUD
      mOrthogonalLRUDAngle  = tryFloatValue( hlp, k, v, key[ 3].dflt );
      if ( mOrthogonalLRUDAngle <  0 ) { mOrthogonalLRUDAngle =  0;  ret = TDString.ZERO; }
      if ( mOrthogonalLRUDAngle > 90 ) { mOrthogonalLRUDAngle = 90;  ret = TDString.NINETY; }
      mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
      mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    } else if ( k.equals( key[  4 ].key ) ) { // DISTOX_LRUD_VERTICAL
      mLRUDvertical = tryFloatValue( hlp, k, v, key[ 4].dflt );
      if ( mLRUDvertical <  0 ) { mLRUDvertical =  0; ret = TDString.ZERO; }
      if ( mLRUDvertical > 91 ) { mLRUDvertical = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[  5 ].key ) ) { // DISTOX_LRUD_HORIZONTAL
      mLRUDhorizontal = tryFloatValue( hlp, k, v, key[ 5].dflt );
      if ( mLRUDhorizontal <  0 ) { mLRUDhorizontal =  0; ret = TDString.ZERO; }
      if ( mLRUDhorizontal > 91 ) { mLRUDhorizontal = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[  6 ].key ) ) { // DISTOX_BEZIER_STEP
      mBezierStep  = tryFloatValue( hlp, k, v, key[ 6].dflt );
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
    TDPrefKey[] key = TDPrefKey.mExportSvx;
    if ( k.equals( key[0].key ) ) { // DISTOX_SURVEX_EOL (choice)
      mSurvexEol = ( tryStringValue( hlp, k, v, key[0].dflt ).equals(key[0]) )? "\n" : "\r\n";
    } else if ( k.equals( key[1].key ) ) { // DISTOX_SURVEX_LRUD (bool)
      mSurvexLRUD = tryBooleanValue( hlp, k, v, bool(key[1].dflt) );
    } else if ( k.equals( key[2].key ) ) { // DISTOX_SURVEX_SPLAY (bool)
      mSurvexSplay = tryBooleanValue( hlp, k, v, bool(key[2].dflt) );
    } else if ( k.equals( key[3].key ) ) { // DISTOX_SURVEX_EPSG (int)
      mSurvexEPSG = tryIntValue( hlp, k, v, key[3].dflt );
    } else {
      TDLog.e("missing EXPORT SVX key: " + k );
    }
    return null;
  }

  private static String updatePrefPly( TDPrefHelper hlp, String k, String v ) 
  { 
    TDPrefKey[] key = TDPrefKey.mExportPly;
    if ( k.equals( key[0].key ) ) {      // DISTOX_PLY_LRUD
      mPlyLRUD  = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );
    } else if ( k.equals( key[1].key ) ) { // DISTOX_PLY_MINUS
      mPlyMinus = tryBooleanValue( hlp, k, v, bool(key[1].dflt) );
    } else {
      TDLog.e("missing EXPORT PLY key: " + k );
    }
    return null;
  }

  private static String updatePrefTh( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref TH: " + k );
    String ret = null;
    TDPrefKey[] key = TDPrefKey.mExportTh;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_THERION_CONFIG (bool)
      mTherionWithConfig    = tryBooleanValue( hlp, k, v, bool(key[ 0 ].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_THERION_MAPS (bool)
      mTherionMaps      = tryBooleanValue( hlp, k, v, bool(key[ 1 ].dflt) );
    // } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_AUTO_STATIONS (bool)
    //   mAutoStations     = tryBooleanValue( hlp, k, v, bool(key[ 2 ].dflt) );
    // } else if ( k.equals( key[ ? ].key ) ) { // DISTOX_XTHERION_AREAS (bool)
    //   mXTherionAreas = tryBooleanValue( hlp, k, v, bool(key[ ].dflt) );   
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_THERION_SPLAYS (bool)
      mTherionSplays    = tryBooleanValue( hlp, k, v, bool(key[ 2 ].dflt) );   
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_SURVEX_LRUD (bool)
      mSurvexLRUD       = tryBooleanValue( hlp, k, v, bool(key[ 3 ].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_TH2_SCALE
      ret = setExportScale( tryIntValue( hlp, k, v, key[4].dflt ) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_TH2_XVI (bool)
      mTherionXvi = tryBooleanValue( hlp, k, v, bool(key[5].dflt) );
    } else {
      TDLog.e("missing EXPORT TH key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefDat( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref DAT: " + k );
    TDPrefKey[] key = TDPrefKey.mExportDat;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_STATION_PREFIX (bool)
      mExportStationsPrefix = tryBooleanValue( hlp, k, v, bool(key[ 0].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_COMPASS_SPLAYS (bool)
      mCompassSplays        = tryBooleanValue( hlp, k, v, bool(key[ 1].dflt) );   
    } else if ( k.equals( key[  2 ].key ) ) { // DISTOX_SWAP_LR (bool)
      mSwapLR               = tryBooleanValue( hlp, k, v, bool(key[ 2].dflt) );
    } else {
      TDLog.e("missing EXPORT DAT key: " + k );
    }
    return null;
  }

  private static String updatePrefSrv( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref DAT: " + k );
    TDPrefKey[] key = TDPrefKey.mExportSrv;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_WALLS_SPLAYS (bool)
      mWallsSplays  = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );   
    // } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_WALLS_UD
    //   mWallsUD = tryIntValue( hlp, k, v, key[1].dflt );
    } else {
      TDLog.e("missing EXPORT SRV key: " + k );
    }
    return null;
  }

  private static String updatePrefTro( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref TRO: " + k );
    TDPrefKey[] key = TDPrefKey.mExportTro;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_VTOPO_SPLAYS (bool)
      mVTopoSplays     = tryBooleanValue( hlp, k, v, bool(key[ 0].dflt) );   
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_VTOPO_LRUD (bool)
      mVTopoLrudAtFrom = tryBooleanValue( hlp, k, v, bool(key[ 1].dflt) );   
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_VTOPO_TROX (bool)
      mVTopoTrox       = tryBooleanValue( hlp, k, v, bool(key[ 2].dflt) );   
    } else {
      TDLog.e("missing EXPORT TRO key: " + k );
    }
    return null;
  }

  private static String updatePrefKml( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref KML: " + k );
    TDPrefKey[] key = TDPrefKey.mExportKml;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_KML_STATIONS (bool)
      mKmlStations = tryBooleanValue( hlp, k, v, bool(key[ 0 ].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_KML_SPLAYS (bool)
      mKmlSplays   = tryBooleanValue( hlp, k, v, bool(key[ 1 ].dflt) );
    } else {
      TDLog.e("missing EXPORT KML key: " + k );
    }
    return null;
  }


  private static String updatePrefGpx( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref GPX: " + k );
    TDPrefKey[] key = TDPrefKey.mExportGpx;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_GPX_SINGLE_TRACK (bool)
      mGPXSingleTrack = tryBooleanValue( hlp, k, v, bool(key[ 0 ].dflt) );
    } else {
      TDLog.e("missing EXPORT GPX key: " + k );
    }
    return null;
  }

  private static String updatePrefCsx( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref CSX: " + k );
    TDPrefKey[] key = TDPrefKey.mExportCsx;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_STATION_PREFIX
      mExportStationsPrefix = tryBooleanValue( hlp, k, v, bool(key[ 0 ].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_WITH_MEDIA
      mExportMedia = tryBooleanValue( hlp, k, v, bool(key[ 1 ].dflt) );
    } else {
      TDLog.e("missing EXPORT CSX key: " + k );
    }
    return null;
  }

  private static String updatePrefCsv( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref CSV: " + k );
    TDPrefKey[] key = TDPrefKey.mExportCsv;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_CSV_RAW (bool)
      mCsvRaw      = tryBooleanValue( hlp, k, v, bool(key[ 0 ].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_CSV_SEP (choice)
      mCsvSeparator = CSV_SEPARATOR[ tryIntValue( hlp, k, v, key[1].dflt ) ]; 
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_SURVEX_EOL
      mSurvexEol = ( tryStringValue( hlp, k, v, key[2].dflt ).equals(key[2].dflt) )? "\n" : "\r\n";
    } else {
      TDLog.e("missing EXPORT CSV key: " + k );
    }
    return null;
  }

  private static String updatePrefShp( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref DXF: " + k );
    TDPrefKey[] key = TDPrefKey.mExportShp;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_SHP_GEOREF (bool)
      mShpGeoref = tryBooleanValue( hlp, k, v, bool(key[ 0 ].dflt) );
    } else {
      TDLog.e("missing EXPORT SHP key: " + k );
    }
    return null;
  }

  private static String updatePrefDxf( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref DXF: " + k );
    TDPrefKey[] key = TDPrefKey.mExportDxf;
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_DXF_SCALE
    //   mDxfScale = tryFloatValue( hlp, k, v, key[ ] );
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_DXF_BLOCKS (bool)
      mDxfBlocks = tryBooleanValue( hlp, k, v, bool(key[ 0 ].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_ACAD_VERSION (choice)
      try { mAcadVersion = tryIntValue( hlp, k, v, key[ 1 ].dflt ); } catch ( NumberFormatException e) {
        TDLog.e( e.getMessage() );
      }
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_ACAD_SPLINE (bool)
      mAcadSpline = tryBooleanValue( hlp, k, v, bool(key[ 2 ].dflt) );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_DXF_REFERENCE (bool)
      mDxfReference = tryBooleanValue( hlp, k, v, bool(key[ 3 ].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_ACAD_LAYER (bool)
      mAcadLayer = tryBooleanValue( hlp, k, v, bool(key[ 4 ].dflt) );
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
  //   TDPrefKey[] key = TDPrefKey.mExportPng;
  //   if ( k.equals( key[ 0 ].key ) ) { // DISTOX_BITMAP_SCALE
  //     mBitmapScale = tryFloatValue( hlp, k, v, key[0].dflt );
  //     if ( mBitmapScale < 0.5f ) { mBitmapScale = 0.5f; ret = "0.5"; }
  //     if ( mBitmapScale >  10f ) { mBitmapScale =  10f; ret = TDString.TEN; }
  //   } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_BITMAP_BGCOLOR
  //     return setBitmapBgcolor( hlp.getSharedPrefs(), k, tryStringValue( hlp, k, v, key[1].dflt ), key[1].dflt );
  //   } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_SVG_GRID
  //     mSvgGrid = tryBooleanValue( hlp, k, v, bool(key[2].dflt) );
  //   } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_THERION_SPLAYS
  //     mTherionSplays = tryBooleanValue( hlp, k, v, bool(key[ 3].dflt) );   
  //   // } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_AUTO_STATIONS
  //   //   mAutoStations  = tryBooleanValue( hlp, k, v, bool(key[ 4].dflt) );
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
    TDPrefKey[] key = TDPrefKey.mExportSvg;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_SVG_ROUNDTRIP (bool)
      mSvgRoundTrip = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_SVG_GRID (bool)
      mSvgGrid = tryBooleanValue( hlp, k, v, bool(key[1].dflt) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_SVG_LINE_DIR (bool)
      mSvgLineDirection = tryBooleanValue( hlp, k, v, bool(key[2].dflt) );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_SVG_SPLAYS (bool)
      mSvgSplays = tryBooleanValue( hlp, k, v, bool(key[3].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_SVG_GROUPS (bool)
      mSvgGroups = tryBooleanValue( hlp, k, v, bool(key[4].dflt) );
    // } else if ( k.equals( key[ ? ].key ) ) { // DISTOX_SVG_IN_HTML (bool)
    // } else if ( k.equals( key[ ? ].key ) ) { // DISTOX_SVG_IN_HTML (bool)
    //   mSvgInHtml = tryBooleanValue( hlp, k, bool(key[ ].dflt) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_SVG_POINT_STROKE
      mSvgPointStroke    = tryFloatValue( hlp, k, v, key[5].dflt );
      if ( mSvgPointStroke < 0.01f ) { mSvgPointStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_SVG_LABEL_STROKE
      mSvgLabelStroke    = tryFloatValue( hlp, k, v, key[6].dflt );
      if ( mSvgLabelStroke < 0.01f ) { mSvgLabelStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_SVG_LINE_STROKE
      mSvgLineStroke     = tryFloatValue( hlp, k, v, key[7].dflt );
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_SVG_GRID_STROKE
      mSvgGridStroke     = tryFloatValue( hlp, k, v, key[8].dflt );
      if ( mSvgGridStroke < 0.01f ) { mSvgGridStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_SVG_SHOT_STROKE
      mSvgShotStroke     = tryFloatValue( hlp, k, v, key[9].dflt );
      if ( mSvgShotStroke < 0.01f ) { mSvgShotStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[10 ].key ) ) { // DISTOX_SVG_LINEDIR_STROKE
      mSvgLineDirStroke  = tryFloatValue( hlp, k, v, key[10].dflt );
      if ( mSvgLineStroke < 0.01f ) { mSvgLineStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[11].key ) ) {  // DISTOX_SVG_STATION_SIZE
      mSvgStationSize    = tryIntValue( hlp, k, v, key[11].dflt );
      if ( mSvgStationSize < 1 ) { mSvgStationSize = 1; ret = "1"; }
    } else if ( k.equals( key[12].key ) ) {  // DISTOX_SVG_LABEL_SIZE
      mSvgLabelSize    = tryIntValue( hlp, k, v, key[12].dflt );
      if ( mSvgLabelSize < 1 ) { mSvgLabelSize = 1; ret = "1"; }
    // } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_BEZIER_STEP
    //   mBezierStep  = tryFloatValue( hlp, k, v, key[8].dflt );
    } else if ( k.equals( key[13].key ) ) {  // DISTOX_SVG_PROGRAM
      mSvgProgram    = tryIntValue( hlp, k, v, key[13].dflt );
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
    TDPrefKey[] key = TDPrefKey.mData;
    if ( k.equals( key[ 0 ].key ) ) {        // DISTOX_CLOSE_DISTANCE
      mCloseDistance  = tryFloatValue( hlp, k, v, key[0].dflt );
      if ( mCloseDistance < 0.0001f ) { mCloseDistance = 0.0001f; ret = "0.0001"; }
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_MAX_SHOT_LENGTH
      mMaxShotLength  = tryFloatValue( hlp, k, v, key[1].dflt );  
      if ( mMaxShotLength < 20 ) { mMaxShotLength = 20; ret = TDString.TWENTY; }
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_MIN_LEG_LENGTH
      mMinLegLength   = tryFloatValue( hlp, k, v, key[2].dflt );  
      if ( mMinLegLength < 0 ) { mMinLegLength = 0; ret = TDString.ZERO; }
      if ( mMinLegLength > 5 ) { mMinLegLength = 5; ret = TDString.FIVE; }
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_LEG_SHOTS (choice)
      mMinNrLegShots  = tryIntValue(   hlp, k, v, key[3].dflt );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_EXTEND_THR2
      mExtendThr      = tryFloatValue( hlp, k, v, key[4].dflt );
      if ( mExtendThr <  0 ) { mExtendThr =  0; ret = TDString.ZERO; }
      if ( mExtendThr > 90 ) { mExtendThr = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_VTHRESHOLD
      mVThreshold     = tryFloatValue( hlp, k, v, key[5].dflt );
      if ( mVThreshold <  0 ) { mVThreshold =  0; ret =  TDString.ZERO; }
      if ( mVThreshold > 90 ) { mVThreshold = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_AZIMUTH_MANUAL (bool)
      mAzimuthManual  = tryBooleanValue( hlp, k, v, bool(key[6].dflt) ); 
      // TDAzimuth.setAzimuthManual( mAzimuthManual ); // FIXME FIXED_EXTEND 20240603
      TDAzimuth.resetRefAzimuth( TopoDroidApp.mShotWindow, TDAzimuth.mRefAzimuth, mAzimuthManual ); // FIXME FIXED_EXTEND 20240603
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_PREV_NEXT (bool)
      mPrevNext = tryBooleanValue( hlp, k, v, bool(key[ 7].dflt) );
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_BACKSIGHT (bool)
      mBacksightInput = tryBooleanValue( hlp, k, v, bool(key[ 8].dflt) );
    } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_LEG_FEEDBACK
      mTripleShot   = tryIntValue( hlp, k, v, key[ 9].dflt );
    } else {
      TDLog.e("missing DATA key: " + k );
    }
    if ( ret != null ) TDPrefHelper.update( k, ret );
    return ret;
  }

  private static String updatePrefUnits( TDPrefHelper hlp, String k, String v )
  {
    // TDLog.v("update pref units: " + k );
    TDPrefKey[] key = TDPrefKey.mUnits;
    if ( k.equals( key[ 0 ].key ) ) {    // DISTOX_UNIT_LENGTH (choice)
      if ( tryStringValue( hlp, k, v, key[0].dflt ).equals(key[0].dflt) ) {
        mUnitLength = 1.0f;
        mUnitLengthStr = "m";
      } else {
        mUnitLength = TDUtil.M2FT;
        mUnitLengthStr = "ft";
      }
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_UNIT_ANGLE (choice)
      if ( tryStringValue( hlp, k, v, key[1].dflt ).equals(key[1].dflt) ) {
        mUnitAngle = 1.0f;
        mUnitAngleStr = "deg";
      } else {
        mUnitAngle = TDUtil.DEG2GRAD;
        mUnitAngleStr = "grad";
      }
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_UNIT_GRID (choice)
      mUnitGrid = tryFloatValue( hlp, k, v, key[2].dflt ); 
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_UNIT_MEASURE (choice)
      mUnitMeasure = tryFloatValue( hlp, k, v, key[3].dflt ); 
    } else {
      TDLog.e("missing UNITS key: " + k );
    }
    return null;
  }

  private static String updatePrefAccuracy( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // TDLog.v("update pref accuracy: " + k );
    TDPrefKey[] key = TDPrefKey.mAccuracy;
    if ( k.equals( key[ 0 ].key ) ) {        // DISTOX_ACCEL_PERCENT 
      mAccelerationThr = tryFloatValue( hlp, k, v, key[0].dflt );
      if ( mAccelerationThr < 0 ) { mAccelerationThr = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_MAG_PERCENT
      mMagneticThr     = tryFloatValue( hlp, k, v, key[1].dflt );
      if ( mMagneticThr < 0 ) { mMagneticThr = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_DIP_THR
      mDipThr          = tryFloatValue( hlp, k, v, key[2].dflt );
      if ( mDipThr < 0 ) { mDipThr = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_SIBLING_PERCENT
      ret = setSiblingThr( tryFloatValue( hlp, k, v, key[3].dflt ) );
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
    TDPrefKey[] key = TDPrefKey.mLocation;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_UNIT_LOCATION (choice)
      mUnitLocation  = tryStringValue( hlp, k, v, key[0].dflt ).equals(key[0].dflt) ? TDUtil.DDMMSS : TDUtil.DEGREE;
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_ALTITUDE
    //   try {
    //     mEllipAlt = Integer.parseInt( tryStringValue( hlp, k, v, ALTITUDE ) );
    //   } catch ( NumberFormatException e ) { mEllipAlt = _WGS84; }
    } else if ( k.equals( key[ 1 ].key ) ) {
      mCRS = tryStringValue( hlp, k, v, key[1].dflt );     // DISTOX_CRS (arbitrary)
    } else if ( k.equals( key[ 2 ].key ) ) {    // DISTOX_NEG_ALTITUDE
      mNegAltitude = tryBooleanValue( hlp, k, v, bool(key[2].dflt) );
    } else if ( k.equals( key[ 3 ].key ) ) {    // DISTOX_EDIT_ALTITUDE
      mEditableHGeo = tryBooleanValue( hlp, k, v, bool(key[3].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) {    // DISTOX_FINE_LOCATION
      mFineLocation = tryIntValue(  hlp, k, v, key[4].dflt );
      if ( mFineLocation < 0 ) { mFineLocation = 0; } else if ( mFineLocation > 600 ) { mFineLocation = 600; }
    } else if ( k.equals( key[ 5 ].key ) ) {    // DISTOX_GEOPOINT_APP
      mGeoImportApp = Integer.parseInt( tryStringValue(  hlp, k, v, key[5].dflt ) );
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
    TDPrefKey[] key = TDPrefKey.mScreen;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_FIXED_THICKNESS
      try {
        float f = tryFloatValue( hlp, k, v, key[0].dflt );
        if ( f >= 0.5f && f <= 10 && f != mFixedThickness ) {
          mFixedThickness = f;
          BrushManager.setStrokeWidths();
        }
       	else if ( f < 0.5f ) { f = 0.5f; ret = "0.5"; }
	else if ( f > 10f )  { f =  10f; ret = TDString.TEN; }
      } catch ( NumberFormatException e ) { ret = String.format(Locale.US, "%.2f", mFixedThickness ); }
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_STATION_SIZE
      try {
        setStationSize( Float.parseFloat( tryStringValue( hlp, k, v, key[1].dflt ) ), true );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
      ret = String.format(Locale.US, "%.2f", mStationSize );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_DOT_RADIUS
      ret = setDotRadius( tryFloatValue( hlp, k, v, key[2].dflt ) );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_CLOSENESS
      ret = setSelectness( tryFloatValue( hlp, k, v, key[3].dflt ) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_ERASENESS
      ret = setEraseness( tryFloatValue( hlp, k, v, key[4].dflt ) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_MIN_SHIFT
      ret = setMinShift( tryIntValue(  hlp, k, v, key[5].dflt ) );
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_POINTING
      ret = setPointingRadius( tryIntValue( hlp, k, v, key[6].dflt ) );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_SPLAY_ALPHA
      mSplayAlpha = tryIntValue( hlp, k, v, key[ 7].dflt ); 
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
    TDPrefKey[] key = TDPrefKey.mLine;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_LINE_THICKNESS
      ret = setLineThickness( tryStringValue( hlp, k, v, key[0].dflt ) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_LINE_STYLE (choice)
      setLineStyleAndType( tryStringValue( hlp, k, v, key[1].dflt ) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_LINE_SEGMENT
      ret = setLineSegment( tryIntValue(   hlp, k, v, key[2].dflt ) );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_LINE_CLOSE
      mLineClose = tryBooleanValue( hlp, k, v, bool(key[3].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_ARROW_LENGTH
      ret = setArrowLength( tryFloatValue( hlp, k, v, key[4].dflt ) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_AUTO_SECTION_PT (bool)
      mAutoSectionPt = tryBooleanValue( hlp, k, v, bool(key[5].dflt) );
    // } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_LINE_CONTINUE (choice)
    //   mContinueLine  = tryIntValue( hlp, k, v, key[7].dflt );
    // } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_WITH_CONTINUE_LINE (bool)
    //   mWithLineJoin = tryBooleanValue(  hlp, k, v, bool(key[8].dflt) );
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_AREA_BORDER (bool)
      mAreaBorder = tryBooleanValue( hlp, k, v, bool(key[6].dflt) );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_LINE_UNITS
      try {
        setDrawingUnitLines( tryFloatValue( hlp, k, v, key[1].dflt ) );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_SLOPE_LSIDE
      ret = setSlopeLSide( tryIntValue( hlp, k, v, key[8].dflt ) );
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
    TDPrefKey[] key = TDPrefKey.mPoint;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_UNSCALED_POINTS (bool)
      mUnscaledPoints = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_DRAWING_UNIT 
      try {
        setDrawingUnitIcons( tryFloatValue( hlp, k, v, key[1].dflt ) );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_LABEL_SIZE
      try {
        setLabelSize( Float.parseFloat( tryStringValue( hlp, k, v, key[2].dflt ) ), true );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
      ret = String.format(Locale.US, "%.2f", mLabelSize );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_SCALABLE_LABEL
      mScalableLabel = tryBooleanValue( hlp, k, v, bool(key[3].dflt) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_XSECTION_OFFSET
      try {
        int value = tryIntValue( hlp, k, v, key[4].dflt );
        if ( value > 0 && value < 500 && value != mXSectionOffset ) {
          ret = String.format(Locale.US, "%d", value );
          mXSectionOffset = value;
        }
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
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
  //   TDPrefKey[] key = TDPrefKey.mWalls;
  //   if ( k.equals( key[ 0 ].key ) ) {        // DISTOX_WALLS_TYPE (choice)
  //     mWallsType = tryIntValue(hlp, k, v, key[0].dflt );
  //   } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_WALLS_PLAN_THR
  //     mWallsPlanThr = tryFloatValue( hlp, k, v, key[1].dflt );
  //     if ( mWallsPlanThr < 0 ) { mWallsPlanThr  =  0; ret = TDString.ZERO; }
  //     if ( mWallsPlanThr > 90 ) { mWallsPlanThr = 90; ret = TDString.NINETY; }
  //   } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_WALLS_EXTENDED_THR
  //     mWallsExtendedThr = tryFloatValue( hlp, k, v, key[2].dflt );
  //     if ( mWallsExtendedThr < 0 ) { mWallsExtendedThr = 0; ret = TDString.ZERO; }
  //     if ( mWallsExtendedThr > 90 ) { mWallsExtendedThr = 90; ret = TDString.NINETY; }
  //   } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_WALLS_XCLOSE
  //     mWallsXClose = tryFloatValue( hlp, k, v, key[3].dflt );
  //     if ( mWallsXClose < 0 ) { mWallsXClose = 0; ret = TDString.ZERO; }
  //   } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_WALLS_CONCAVE
  //     mWallsConcave = tryFloatValue( hlp, k, v, key[4].dflt );
  //     if ( mWallsConcave < 0 ) { mWallsConcave = 0; ret = TDString.ZERO; }
  //   } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_WALLS_XSTEP
  //     mWallsXStep = tryFloatValue( hlp, k, v, key[5].dflt );
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
    TDPrefKey[] key = TDPrefKey.mDraw;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_UNSCALED_POINTS (bool)
      mUnscaledPoints = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_DRAWING_UNIT (choice)
      try {
        setDrawingUnitIcons( tryFloatValue( hlp, k, v, key[1].dflt ) );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_LABEL_SIZE
      try {
        setLabelSize( Float.parseFloat( tryStringValue( hlp, k, v, key[2].dflt ) ), true );
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
      ret = String.format(Locale.US, "%.2f", mLabelSize );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_LINE_THICKNESS
      ret = setLineThickness( tryStringValue( hlp, k, v, key[3].dflt ) );
    } else if ( k.equals( key[ 4 ].key ) ) { // DISTOX_LINE_STYLE (choice)
      setLineStyleAndType( tryStringValue( hlp, k, v, key[4].dflt ) );
    } else if ( k.equals( key[ 5 ].key ) ) { // DISTOX_LINE_CLOSE
      mLineClose = tryBooleanValue( hlp, k, v, bool(key[5].dflt) );
    } else if ( k.equals( key[ 6 ].key ) ) { // DISTOX_LINE_SEGMENT
      ret = setLineSegment( tryIntValue(   hlp, k, v, key[6].dflt ) );
    } else if ( k.equals( key[ 7 ].key ) ) { // DISTOX_ARROW_LENGTH
      ret = setArrowLength( tryFloatValue( hlp, k, v, key[7].dflt ) );
    } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_AUTO_SECTION_PT (bool)
      mAutoSectionPt = tryBooleanValue( hlp, k, v, bool(key[8].dflt) );
    // } else if ( k.equals( key[ 8 ].key ) ) { // DISTOX_LINE_CONTINUE (choice)
    //   mContinueLine  = tryIntValue( hlp, k, v, key[8].dflt );
    } else if ( k.equals( key[ 9 ].key ) ) { // DISTOX_AREA_BORDER (bool)
      mAreaBorder = tryBooleanValue( hlp, k, v, bool(key[9].dflt) );
    // } else if ( k.equals( key[ 10 ].key ) ) { // DISTOX_REDUCE_ANGLE
    //   ret = setReduceAngle( tryFloatValue( hlp, k, v, key[10] ) );
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
    TDPrefKey[] key = TDPrefKey.mErase;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_CLOSENESS
      ret = setSelectness( tryFloatValue( hlp, k, v, key[0].dflt ) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_ERASENESS
      ret = setEraseness( tryFloatValue( hlp, k, v, key[1].dflt ) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_POINTING
      ret = setPointingRadius( tryIntValue(   hlp, k, v, key[2].dflt ) );
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
    TDPrefKey[] key = TDPrefKey.mEdit;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_DOT_RADIUS
      ret = setDotRadius( tryFloatValue( hlp, k, v, key[0].dflt ) );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_CLOSENESS
      ret = setSelectness( tryFloatValue( hlp, k, v, key[1].dflt ) );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_MIN_SHIFT
      ret = setMinShift( tryIntValue(  hlp, k, v, key[2].dflt ) );
    } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_POINTING
      ret = setPointingRadius( tryIntValue(   hlp, k, v, key[3].dflt ) );
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
    TDPrefKey[] key = TDPrefKey.mSketch;
    if ( k.equals( key[ 0 ].key ) ) { // DISTOX_3D_SKETCH
      m3Dsketch = tryBooleanValue( hlp, k, v, bool(key[0].dflt) );
      TDLog.e("3D sketch: " + m3Dsketch );
    } else if ( k.equals( key[ 1 ].key ) ) { // DISTOX_SKETCH_SPLAY_BUFFER
      mSplayBuffer = tryFloatValue( hlp, k, v, key[1].dflt );
      TDLog.e("3D sketch buffer: " + mSplayBuffer );
    } else {
      TDLog.e("missing SKETCH key: " + k );
    }
    /*
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SKETCH_MODEL_TYPE (choice)
      mSketchModelType = tryIntValue(  hlp, k, v, key[0] );
    } else if ( k.equals( key[ 1 ].key ) ) { // 0.5 meter // DISTOX_SKETCH_LINE_STEP
      mSketchSideSize = tryFloatValue( hlp, k, v, key[1] );
      if ( mSketchSideSize < 0.01f ) { mSketchSideSize = 0.01f; ret = "0.01"; }
    // } else if ( k.equals( key[ ? ].key ) ) { // DISTOX_BORDER_STEP
    //   mSketchBorderStep  = tryFloatValue( hlp, k, v, key[ ] );
    // } else if ( k.equals( key[ ? ].key ) ) { // DISTOX_SECTION_STEP
    //   mSketchSectionStep = tryFloatValue( hlp, k, v, key[ ] );
    } else if ( k.equals( key[ 2 ].key ) ) { // DISTOX_DELTA_EXTRUDE
      mDeltaExtrude = tryFloatValue( hlp, k, v, key[2] );
      if ( mDeltaExtrude < 0.01f ) { mDeltaExtrude = 0.01f; ret = "0.01"; }
    // } else if ( k.equals( key[ 3 ].key ) ) { // DISTOX_COMPASS_READINGS
    //   mCompassReadings = tryIntValue( hlp, k, v, key[ ] );
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
    TDLog.v("Level current " + TDLevel.mLevel + " set " + level );
    if ( level == TDLevel.mLevel ) return;

    if ( StationPolicy.policyDowngrade( level ) ) {
      setPreference( prefs, TDPrefKey.policyKey(), TDString.ONE );
    }
    TDLevel.setLevel( TDInstance.context, level );
    int policy = StationPolicy.policyUpgrade( level );
    if ( policy > 0 ) {
      setPreference( prefs, TDPrefKey.policyKey(), Integer.toString( policy ) );
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
      TDPrefHelper.update( TDPrefKey.policyKey(), Integer.toString( StationPolicy.savedPolicy() ) );
      if ( TDPrefActivity.mPrefActivitySurvey != null ) TDPrefActivity.mPrefActivitySurvey.reloadPreferences(); // FIXME_PREF
    } else {
      // TDLog.v("preference is set to the policy " + policy );
      TDPrefHelper.update( TDPrefKey.policyKey(), Integer.toString( policy ) );
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
  private static String tf( boolean b ) { return Boolean.toString( b ); }

  /** export settings to the file
   * @param ctx   context
   * @param prefs settings (unused)
   * @param flag  setting groups flag (unused)
   */
  public static boolean exportSettings( Context ctx, Uri uri, SharedPreferences prefs, int flag )
  {
    TDLog.v("TD Setting exports settings");
    // File file = TDFile.getSettingsFile(); // PRIVATE FILE
    try {
      // FileWriter fw = new FileWriter( file, false ); // true = append
      ParcelFileDescriptor pdf = TDsafUri.docWriteFileDescriptor( uri );
      FileWriter fw = TDsafUri.docFileWriter( pdf );

      PrintWriter pw = new PrintWriter( fw, true ); // true = auto-flush
      // FIXME getAll returns only a few preferences
      // Map<String, ?> allPrefs = prefs.getAll();
      // TDLog.v("settings export " + allPrefs.keySet().size() );
      // for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
      //   String key   = entry.getKey();
      //   Object value = entry.getValue();
      //   if (value instanceof Boolean) {
      //     pw.printf(Locale.US, "B %s %s\n", key, value.toString() );
      //   } else if (value instanceof Integer) {
      //     pw.printf(Locale.US, "I %s %s\n", key, value.toString() );
      //   } else if (value instanceof Long) {
      //     pw.printf(Locale.US, "L %s %s\n", key, value.toString() );
      //   } else if (value instanceof Float) {
      //     pw.printf(Locale.US, "F %s %s\n", key, value.toString() );
      //   } else if (value instanceof String) {
      //     pw.printf(Locale.US, "S %s %s\n", key, value.toString() );
      //   } else {
      //     continue; // Skip unknown types
      //   }
      // }
      pw.printf(Locale.US, "TopoDroid v. %s %d flag %d\n", TDVersion.string(), TDVersion.code(), flag );
      // int cnt = 0;
      int n1 = TDPrefKey.nrKeySets();
      for ( int j = 0; j < n1; ++ j ) {
        int n2 = TDPrefKey.nrKeys( j );
        for ( int k = 0; k < n2; ++k ) {
          if ( TDPrefKey.repeatedKey( j, k ) ) continue;
          TDPrefKey kay = TDPrefKey.getKey( j, k );
          if ( ( flag & (1<<kay.group) ) != 0 ) {
            String val;
            boolean bval;
            switch ( kay.type ) {
              case TDPrefKey.LONG:
              case TDPrefKey.COL:
              case TDPrefKey.ARR:
                val = prefs.getString( kay.key, kay.dflt );
                pw.printf(Locale.US, "I %s %s\n", kay.key, val );
                // ++cnt;
                break;
              case TDPrefKey.BOOL:
              case TDPrefKey.XTR:
                bval = prefs.getBoolean( kay.key, bool(kay.dflt) );
                pw.printf(Locale.US, "B %s %s\n", kay.key, tf(bval) );
                // ++cnt;
                break;
              case TDPrefKey.FLT:
                val = prefs.getString( kay.key, kay.dflt );
                pw.printf(Locale.US, "F %s %s\n", kay.key, val );
                // ++cnt;
                break;
              case TDPrefKey.STR:
                val = prefs.getString( kay.key, kay.dflt );
                pw.printf(Locale.US, "S %s %s\n", kay.key, val );
                // ++cnt;
                break;
              case TDPrefKey.BTN:
                // TDLog.v("Button key " + kay.key );
                break;
              case TDPrefKey.FWRD:
                continue;
            }
          }
        }
      }
      // TDLog.v("Printed " + cnt + " settings");
/*
      // this list is incomplete: the following keys are missing - these are probably missing also from importSettings()
I DISTOX_LOCALE 
I DISTOX_SURVEY_STATION 1
F DISTOX_TOOLBAR_SIZE 5
B DISTOX_PLOT_CACHE true
F DISTOX_ALGO_MIN_ALPHA 0.05
F DISTOX_ALGO_MIN_BETA 3.0
F DISTOX_ALGO_MIN_GAMMA 0.05
F DISTOX_ALGO_MIN_DELTA 0.05
F DISTOX_AUTO_CAL_BETA 0.004
F DISTOX_AUTO_CAL_ETA 0.04
F DISTOX_AUTO_CAL_GAMMA 0.04
F DISTOX_AUTO_CAL_DELTA 0.04
B CAVE3D_NEG_CLINO false
B CAVE3D_STATION_POINTS false
I CAVE3D_STATION_POINT_SIZE 8
I CAVE3D_STATION_TEXT_SIZE 20
F CAVE3D_SELECTION_RADIUS 50
B CAVE3D_MEASURE_DIALOG false
B CAVE3D_STATION_TOAST false
B CAVE3D_GRID_ABOVE false
I CAVE3D_GRID_EXTENT 10
B DISTOX_NAMES_VISIBILITY false
B EXPORT_DATA_COMPASS true
B EXPORT_DATA_CSURVEY true
B EXPORT_DATA_POLYGON false
B EXPORT_DATA_SURVEX false
B EXPORT_DATA_THERION true
B EXPORT_DATA_TOPO false
B EXPORT_DATA_TOPOROBOT false
B EXPORT_DATA_VISUALTOPO true
B EXPORT_DATA_WALLS false
B EXPORT_DATA_WINKARST false
B EXPORT_DATA_CVS true
B EXPORT_DATA_DXF false
B EXPORT_DATA_KML false
B EXPORT_DATA_GPX false
B EXPORT_DATA_SHAPEFILE true
I DISTOX_SURVEX_EOL lf
I DISTOX_SVG_PROGRAM 1
B DISTOX_DXF_REFERENCE false
F CAVE3D_DEM_BUFFER 200
I CAVE3D_DEM_MAXSIZE 400
I CAVE3D_DEM_REDUCE 1
S CAVE3D_TEXTURE_ROOT /sdcard/
I CAVE3D_SPLAY_USE 1
B CAVE3D_ALL_SPLAY true
B CAVE3D_SPLAY_PROJ false
F CAVE3D_SPLAY_THR 0.5
B CAVE3D_SPLIT_TRIANGLES true
F CAVE3D_SPLIT_RANDOM 0.1
F CAVE3D_SPLIT_STRETCH 0.1
F CAVE3D_POWERCRUST_DELTA 0.1
B DISTOX_SINGLE_BACK true
B DISTOX_HIDE_NAVBAR false
B DISTOX_WITH_DEBUG false
B DISTOX_BACKSIGHT_SPLAY false
F DISTOX_LOOP_THRESHOLD 1
I DISTOX_LINE_ENDS 0
B DISTOX_FIRMWARE_SANITY true
I DISTOX_BRIC_MODE 3
B DISTOX_BRIC_ZERO_LENGTH false
B DISTOX_BRIC_INDEX_IS_ID false
B DISTOX_SAP5_BIT16_BUG true

      String k;
      k="DISTOX_SIZE_BUTTONS";     if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSizeBtns );
      k="DISTOX_TEXT_SIZE";        if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mTextSize );
      k="DISTOX_SYMBOL_SIZE";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %.4f\n", k, mSymbolSize );
      k="DISTOX_EXTRA_BUTTONS";    if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, TDLevel.mLevel );
      k="DISTOX_MKEYBOARD";        if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mKeyboard) );
      k="DISTOX_NO_CURSOR";        if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mNoCursor) );
      k="DISTOX_BULK_EXPORT";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mBulkExport) );
      k="DISTOX_LOCAL_MAN";        if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mLocalManPages) );
      k="DISTOX_PALETTES";         if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mPalettes) );
      k="DISTOX_ORIENTATION";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mOrientation );
      k="DISTOX_EXPORT_SHOTS";     if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mExportShotsFormat );
      k="DISTOX_EXPORT_PLOT";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mExportPlotFormat );
      k="DISTOX_AUTO_PLOT_EXPORT"; if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mAutoExportPlotFormat );
      TDLog.v("Printed 13 settings");

      // pw.printf(Locale.US, "B DISTOX_DATA_BACKUP %s\n", mDataBackup );
      // FIXME pw.printf(Locale.US, "S DISTOX_SURVEX_EOL \"%s\"\n", mSurvexEol.equals("\r\n")? eol = "\\r\\n" : "\\n" );
      k="DISTOX_SURVEX_SPLAY";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mSurvexSplay) );
      k="DISTOX_SURVEX_LRUD";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mSurvexLRUD) );
      k="DISTOX_SURVEX_EPSG";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",  k, mSurvexEPSG );
      k="DISTOX_PLY_LRUD";              if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mPlyLRUD) );
      k="DISTOX_PLY_MINUS";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mPlyMinus) );
      k="DISTOX_SWAP_LR";               if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mSwapLR) );
      k="DISTOX_STATION_PREFIX";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mExportStationsPrefix) );
      k="DISTOX_COMPASS_SPLAYS";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mCompassSplays) );
      k="DISTOX_WITH_MEDIA";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mExportMedia) );
      k="DISTOX_WALLS_SPLAYS";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, mWallsSplays );
      // k="DISTOX_WALLS_UD";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",  k, mWallsUD );
      k="DISTOX_VTOPO_SPLAYS";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mVTopoSplays) );
      k="DISTOX_VTOPO_LRUD";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mVTopoLrudAtFrom) );
      k="DISTOX_VTOPO_TROX";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mVTopoTrox) );
      k="DISTOX_ORTHO_LRUD";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n",k, mOrthogonalLRUDAngle );
      k="DISTOX_THERION_CONFIG";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mTherionWithConfig) );
      k="DISTOX_THERION_MAPS";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mTherionMaps) );
      k="DISTOX_AUTO_STATIONS";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mAutoStations) );
      k="DISTOX_THERION_SPLAYS";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mTherionSplays) );
      k="DISTOX_TH2_XVI";               if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",  k, tf(mTherionXvi) );
      k="DISTOX_TH2_SCALE";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",  k, mTherionScale );
      TDLog.v("Printed 20 settings");

      // k="DISTOX_BITMAP_SCALE";       if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mBitmapScale );
      // k="DISTOX_BITMAP_BGCOLOR";     if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mBitmapBgcolor );
      k="DISTOX_ACAD_VERSION";          if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mAcadVersion );
      k="DISTOX_DXF_BLOCKS";            if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mDxfBlocks) );
      k="DISTOX_ACAD_SPLINE";           if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mAcadSpline) );
      k="DISTOX_ACAD_LAYER";            if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mAcadLayer) );
      k="DISTOX_SVG_SHOT_STROKE";       if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSvgShotStroke );
      k="DISTOX_SVG_LABEL_STROKE";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSvgLabelStroke );
      k="DISTOX_SVG_LABEL_SIZE";        if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSvgLabelSize );
      k="DISTOX_SVG_STATION_SIZE";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSvgStationSize ); 
      k="DISTOX_SVG_POINT_STROKE";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSvgPointStroke );
      k="DISTOX_SVG_ROUNDTRIP";         if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSvgRoundTrip) );
      k="DISTOX_SVG_GRID";              if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSvgGrid) );
      k="DISTOX_SVG_GRID_STROKE";       if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSvgGridStroke );
      k="DISTOX_SVG_LINE_STROKE";       if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSvgLineStroke );
      k="DISTOX_SVG_LINE_DIR";          if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSvgLineDirection) );
      k="DISTOX_SVG_LINEDIR_STROKE";    if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSvgLineDirStroke );
      k="DISTOX_SVG_SPLAYS";            if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSvgSplays) );
      k="DISTOX_SVG_GROUPS";            if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSvgGroups) );
      // k="DISTOX_SVG_PROGRAM";        if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSvgProgram );
      k="DISTOX_SHP_GEOREF";            if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mShpGeoref) );
      k="DISTOX_GPX_SINGLE_TRACK";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mGPXSingleTrack) );
      k="DISTOX_KML_STATIONS";          if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mKmlStations) );
      k="DISTOX_KML_SPLAYS";            if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mKmlSplays) );
      k="DISTOX_CSV_RAW";               if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mCsvRaw) );
      k="DISTOX_CSV_SEP";               if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, ( mCsvSeparator == CSV_COMMA )? 0 : ( mCsvSeparator == CSV_PIPE )? 1 : 2 );
      TDLog.v("Printed 23 settings");

      k="DISTOX_BLUETOOTH";             if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mCheckBT );
      k="DISTOX_AUTO_PAIR";             if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mAutoPair) );
      k="DISTOX_SOCKET_TYPE";           if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSockType );
      k="DISTOX_SOCKET_DELAY";          if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mConnectSocketDelay );
      k="DISTOX_CONN_MODE";             if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mConnectionMode );
      // k="DISTOX_Z6_WORKAROUND";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mZ6Workaround) );
      k="DISTOX_CONNECT_FEEDBACK";      if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mConnectFeedback );
      k="DISTOX_UNNAMED_DEVICE";        if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mUnnamedDevice) );
      // k="DISTOX_AUTO_RECONNECT";     if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mAutoReconnect) );
      k="DISTOX_SECOND_DISTOX";         if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSecondDistoX) );
      k="DISTOX_COMM_RETRY";            if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mCommRetry );
      k="DISTOX_HEAD_TAIL";             if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mHeadTail) );
      k="DISTOX_PACKET_LOGGER";         if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mPacketLog) );
      k="DISTOX_TH2_EDIT";              if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mTh2Edit) );
      // k="DISTOX_WITH_DEBUG";         if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mWithDebug) );
      k="DISTOX_WAIT_LASER";            if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mWaitLaser );
      k="DISTOX_WAIT_SHOT";             if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mWaitShot );
      k="DISTOX_WAIT_DATA";             if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mWaitData );
      k="DISTOX_WAIT_CONN";             if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mWaitConn );
      k="DISTOX_WAIT_COMMAND";          if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mWaitCommand );
      k="DISTOX_GROUP_BY";              if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mGroupBy );
      k="DISTOX_GROUP_DISTANCE";        if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mGroupDistance );
      // DISTOX_CALIB_ALGOk="";         if ( TDPrefKey.checkKeyGroup(k,flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mCalibAlgo );
      TDLog.v("Printed 19 settings");

      k="DISTOX_CALIB_EPS";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mCalibEps );
      k="DISTOX_CALIB_MAX_IT";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mCalibMaxIt );
      k="DISTOX_CALIB_SHOT_DOWNLOAD";   if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mCalibShotDownload) );
      k="DISTOX_RAW_CADTA";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mRawCData );
      // k="DISTOX_MIN_ALPHA";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mAlgoMinAlpha );
      // k="DISTOX_MIN_BETA";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mAlgoMinBeta );
      // k="DISTOX_MIN_GAMMA";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mAlgoMinGamma );
      // k="DISTOX_MIN_DELTA";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mAlgoMinDelta );
      k="DISTOX_TEAM";                  if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "S %s %s\n",   k, mDefaultTeam );
      k="DISTOX_TEAM_DIALOG";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mTeamNames );
      k="DISTOX_CHECK_ATTACHED";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mCheckAttached) );
      k="DISTOX_CHECK_EXTEND";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mCheckExtend) );
      k="DISTOX_UNIT_LOCATION";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "D %s %s\n",   k, ( mUnitLocation == TDUtil.DDMMSS ? "ddmmss" : "degrees" ) );
      k="DISTOX_CRS";                   if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "S %s %s\n",   k, mCRS );
      k="DISTOX_NEG_ALTITUDE";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mNegAltitude) );
      k="DISTOX_FINE_LOCATION";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mFineLocation );
      k="DISTOX_GEOPOINT_APP";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mGeoImportApp );
      k="DISTOX_EDIT_ALTITUDE";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mEditableHGeo) );
      TDLog.v("Printed 14 settings");

      k="DISTOX_VTHRESHOLD";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mVThreshold );
      k="DISTOX_HTHRESHOLD";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mHThreshold );
      k="DISTOX_BACKSHOT";              if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mDistoXBackshot) );
      k="DISTOX_DIVING_MODE";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mDivingMode) );
      k="DISTOX_TAMPERING";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mEditableShots) );
      k="DISTOX_BACKSIGHT";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mBacksightInput) );
      k="DISTOX_PREV_NEXT";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mPrevNext) );
      k="DISTOX_SPLAY_EXTEND";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mLRExtend) );
      k="DISTOX_BLUNDER_SHOT";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mBlunderShot) );
      k="DISTOX_SPLAY_STATION";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSplayStation) );
      k="DISTOX_SPLAY_GROUP";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSplayOnlyForward) );
      k="DISTOX_LRUD_VERTICAL";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mLRUDvertical );
      k="DISTOX_LRUD_HORIZONTAL";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mLRUDhorizontal );
      k="DISTOX_LRUD_COUNT";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mLRUDcount) );
      k="DISTOX_IMPORT_DATAMODE";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mImportDatamode );
      k="DISTOX_ZIP_WITH_SYMBOLS";      if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mZipWithSymbols) );
      k="DISTOX_ZIP_SHARE_CATEGORY";    if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mZipShareCategory) );
      k="DISTOX_SHOT_TIMER";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mTimerWait );
      k="DISTOX_BEEP_VOLUME";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mBeepVolume );
      k="DISTOX_RECENT_SHOT";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mShotRecent) );
      k="DISTOX_RECENT_TIMEOUT";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mRecentTimeout );
      k="DISTOX_CLOSE_DISTANCE";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mCloseDistance );
      k="DISTOX_LEG_SHOTS";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mMinNrLegShots );
      k="DISTOX_LEG_FEEDBACK";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mTripleShot );
      k="DISTOX_MAX_SHOT_LENGTH";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mMaxShotLength );
      k="DISTOX_MIN_LEG_LENGTH";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mMinLegLength );
      k="DISTOX_SPLAY_VERT_THRS";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSplayVertThrs );
      k="DISTOX_SPLAY_CLASSES";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSplayClasses) );
      // k="DISTOX_SPLAY_AS_DOT";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSplayAsDot) );
      TDLog.v("Printed 28 settings");

      k="DISTOX_STATION_NAMES";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mStationNames );
      k="DISTOX_INIT_STATION";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "S %s %s\n",   k, mInitStation );
      k="DISTOX_EXTEND_THR2";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mExtendThr );
      k="DISTOX_AZIMUTH_MANUAL";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mAzimuthManual) );
      k="DISTOX_EXTEND_FRAC";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mExtendFrac) );
      k="DISTOX_LOOP_CLOSURE_VALUE";    if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mLoopClosure );
      k="DISTOX_LOOP_THR";              if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mLoopThr );
      k="DISTOX_UNIT_LENGTH";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "S %s %s\n",   k, ( mUnitLength > 0.99f ? "meters" : "feet" ) );
      k="DISTOX_UNIT_ANGLE";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "S %s %s\n",   k, ( mUnitAngle > 0.99f ?  "degrees" : "grads" ) );
      k="DISTOX_THUMBNAIL";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mThumbSize );
      k="DISTOX_SAVED_STATIONS";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSavedStations) );
      k="DISTOX_LEGONLY_UPDATE";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mLegOnlyUpdate) );
      k="DISTOX_ANDROID_AZIMUTH";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mWithAzimuth) );
      k="DISTOX_WITH_SENSORS";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mWithSensors) );
      k="DISTOX_BEDDING";               if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mBedding) );
      k="DISTOX_ZOOM_CTRL";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mZoomCtrl );
      k="DISTOX_SIDE_DRAG";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSideDrag) );
      k="DISTOX_FIXED_ORIGIN";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mFixedOrigin) );
      k="DISTOX_PLOT_SPLIT";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mPlotSplit) );
      k="DISTOX_PLOT_SHIFT";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mPlotShift) );
      k="DISTOX_WITH_LEVELS";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mWithLevels );
      TDLog.v("Printed 23 settings");

      k="DISTOX_FULL_AFFINE";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mFullAffine) );
      k="DISTOX_STYLUS_SIZE";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mStylusSize );
      k="DISTOX_SLANT_XSECTION";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSlantXSection) );
      k="DISTOX_OBLIQUE_PROJECTED";     if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mObliqueMax );
      k="DISTOX_DRAWING_UNIT";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mUnitIcons );
      k="DISTOX_LINE_UNITS";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mUnitLines );
      k="DISTOX_UNIT_GRID";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mUnitGrid );
      k="DISTOX_UNIT_MEASURE";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mUnitMeasure );
      k="DISTOX_STATION_SIZE";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mStationSize );
      k="DISTOX_LABEL_SIZE";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mLabelSize );
      k="DISTOX_FIXED_THICKNESS";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mFixedThickness );
      k="DISTOX_LINE_THICKNESS";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mLineThickness );
      k="DISTOX_SCALABLE_LABEL";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, mScalableLabel );
      k="DISTOX_XSECTION_OFFSET";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mXSectionOffset );
      k="DISTOX_CLOSENESS";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSelectness );
      k="DISTOX_POINTING";              if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mPointingRadius );
      k="DISTOX_MIN_SHIFT";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mMinShift );
      k="DISTOX_DOT_RADIUS";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mDotRadius );
      k="DISTOX_PATH_MULTISELECT";      if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mPathMultiselect) );
      k="DISTOX_ERASENESS";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mEraseness );
      // k="DISTOX_PICKER_TYPE";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mPickerType );
      k="DISTOX_UNSCALED_POINTS";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mUnscaledPoints) ); 
      k="DISTOX_LINE_STYLE";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mLineStyle );
      k="DISTOX_LINE_SEGMENT";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mLineSegment );
      // k="DISTOX_LINE_CONTINUE";      if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mContinueLine );
      TDLog.v("Printed 23 settings again");

      k="DISTOX_ARROW_LENGTH";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mArrowLength );
      k="DISTOX_LINE_CLOSE";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mLineClose) );
      k="DISTOX_SLOPE_LSIDE";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSlopeLSide );
      k="DISTOX_BEZIER_STEP";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mBezierStep );
      k="DISTOX_LINE_ACCURACY";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mLineAccuracy );
      k="DISTOX_LINE_CORNER";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mLineCorner );
      k="DISTOX_WEED_DISTANCE";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mWeedDistance );
      k="DISTOX_WEED_LENGTH";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mWeedLength );
      k="DISTOX_WEED_BUFFER";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mWeedBuffer );
      k="DISTOX_AREA_BORDER";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mAreaBorder) );
      k="DISTOX_BACKUP_NUMBER";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mBackupNumber );
      k="DISTOX_BACKUP_INTERVAL";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mBackupInterval );
      // k="DISTOX_BACKUPS_CLEAR";      if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mBackupsClear) );
      k="DISTOX_SHARED_XSECTIONS";      if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSharedXSections) );
      k="DISTOX_AUTO_XSECTIONS";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mAutoXSections) );
      k="DISTOX_AUTO_SECTION_PT";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mAutoSectionPt) );
      k="DISTOX_LINE_SNAP";             if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mLineSnap) );
      k="DISTOX_LINE_CURVE";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mLineCurve) );
      TDLog.v("Printed 17 settings");

      k="DISTOX_LINE_STRAIGHT";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mLineStraight) );
      k="DISTOX_REDUCE_ANGLE";          if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mReduceAngle );
      k="DISTOX_SPLAY_ALPHA";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSplayAlpha );
      // k="DISTOX_SPLAY_COLOR";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "B %s %s\n",   k, tf(mSplayColor) );
      k="DISTOX_DISCRETE_COLORS";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mDiscreteColors );
      k="DISTOX_SPLAY_DASH";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mDashSplay );
      k="DISTOX_VERT_SPLAY";            if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mVertSplay );
      k="DISTOX_HORIZ_SPLAY";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mHorizSplay );
      k="DISTOX_SECTION_SPLAY";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSectionSplay );
      k="DISTOX_SPLAY_DASH_COLOR";      if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSplayDashColor );
      k="DISTOX_SPLAY_DOT_COLOR";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSplayDotColor );
      k="DISTOX_SPLAY_LATEST_COLOR";    if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mSplayLatestColor );
      k="DISTOX_ACCEL_PERCENT";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mAccelerationThr );
      k="DISTOX_MAG_PERCENT";           if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mMagneticThr );
      k="DISTOX_DIP_THR";               if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mDipThr );
      k="DISTOX_SIBLING_PERCENT";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mSiblingThr );
      // k="DISTOX_WALLS_TYPE";         if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "I %s %d\n",   k, mWallsType );
      // k="DISTOX_WALLS_PLAN_THR";     if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mWallsPlanThr );
      // k="DISTOX_WALLS_EXTENDED_THR"; if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mWallsExtendedThr );
      // k="DISTOX_WALLS_XCLOSE";       if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mWallsXClose );
      // k="DISTOX_WALLS_XSTEP";        if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mWallsXStep );
      // k="DISTOX_WALLS_CONCAVE";      if ( TDPrefKey.checkKeyGroup(k, flag) ) pw.printf(Locale.US, "F %s %.4f\n", k, mWallsConcave );
      TDLog.v("Printed 15 settings");
/*
*/
      // TDLog.exportLogSettings( pw ); // NO_LOGS
      fw.close();
      TDsafUri.closeFileDescriptor( pdf );
      return true;
    } catch ( IOException e ) { 
      TDLog.e("Setting export I/O failure " + e.getMessage() );
    } catch (Exception e) {
      TDLog.e( "Setting export error " + e.getMessage() );
    }
    return false;
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

  // /** @return true if the string at the given index is "T"
  //  * @param vals    string array
  //  * @param idx     index
  //  */
  // private static boolean getBoolean( String[] vals, int idx ) { return idx < vals.length && vals[idx].equals("T"); }

  // /** @return the string at the given index as a float value, or the fail-over value
  //  * @param vals    string array
  //  * @param idx     index
  //  * @param fail    fail-over value
  //  */
  // private static float getFloat( String[] vals, int idx, float fail )
  // {
  //   if ( idx < vals.length ) {
  //     try { 
  //       return Float.parseFloat(vals[idx]);
  //     } catch ( NumberFormatException e ) {
  //       TDLog.e( e.getMessage() );
  //     }
  //   }
  //   return fail;
  // }

  // /** @return the string at the given index as an integer value, or the fail-over value
  //  * @param vals    string array
  //  * @param idx     index
  //  * @param fail    fail-over value
  //  */
  // private static int getInt( String[] vals, int idx, int fail )
  // {
  //   if ( idx < vals.length ) {
  //     try { 
  //       return Integer.parseInt(vals[idx]);
  //     } catch ( NumberFormatException e ) {
  //       TDLog.e( e.getMessage() );
  //     }
  //   }
  //   return fail;
  // }


  /** import the settings from the settings file
   * the file must contain one key for each line, as "type key value"
   * where type is B, I, or F,
   *       key  is the name of the setting
   *       and value its value
   * The classes of keys are defined in TDPrefKey
   *   
   * @param ctx     context
   * @param uri     file uri (TODO)
   * @param prefs   shared preferences
   * @param all     whether to import all settings
   * @return true on success
   */
  public static boolean importSettings( Context ctx, Uri uri, SharedPreferences prefs, int flag )
  {
    TDLog.v("Setting import settings - flag " + flag );
    if ( flag == 0 ) return true;
    // String[] keyUnits = TDPrefKey.UNITS;
    // String[] defUnits = TDPrefKey.UNITSdef;
    Editor editor = prefs.edit();
    File file = TDFile.getSettingsFile(); // PRIVATE FILE
    int size;
    float fsize;
    try {
      FileReader fr = new FileReader( file ); // true = append
      BufferedReader br = new BufferedReader( fr ); // true = auto-flush
      String line;
      while ( ( line = br.readLine() ) != null ) {
        // TDLog.v("Setting: " + line );
        String[] vals = line.trim().replaceAll(",", "").replaceAll("\\s+", " ").split(" ");
        if ( line.startsWith("TopoDroid" ) ) {
          // if ( vals.length < 4 ) return false;
          // if ( Long.parseLong( value ) < 500053L ) return false;
          continue;
        }
        String kay   = vals[1];
        String value = vals[2];
        // int group    = TDPrefKey.getKeyGroup( kay ); // FIXME TODO
        if ( TDPrefKey.checkKeyGroup( kay, flag ) ) {
          switch ( kay ) {
            case "DISTOX_SIZE_BUTTONS":
              size = Integer.parseInt( value );
              if ( size >= 0 ) setSizeButtons( size ); setPreference( editor, kay, mSizeBtns );
              break;
            case "DISTOX_TEXT_SIZE":
              size = Integer.parseInt( value );
              if ( size >= 0 ) setTextSize( size ); setPreference( editor, kay, mTextSize );
              break;
            case "DISTOX_SYMBOL_SIZE":
              fsize = Float.parseFloat( value );
              if ( fsize > 0 ) setSymbolSize( fsize ); setPreference( editor, kay, mSymbolSize );
              break;
            case "DISTOX_MKEYBOARD":
              mKeyboard = Boolean.parseBoolean( value ); setPreference( editor, kay, mKeyboard );
              break;
            case "DISTOX_NO_CURSOR":
              mNoCursor = Boolean.parseBoolean( value ); setPreference( editor, kay, mNoCursor );
              break;
            case "DISTOX_BULK_EXPORT":
              mBulkExport = Boolean.parseBoolean( value ); setPreference( editor, kay, mBulkExport );
              break;
            // case "DISTOX_LOCAL_MAN":
            //   setPreference( editor, kay, mLocalManPages );
            //   break;
            case "DISTOX_ORIENTATION":
              mOrientation = Integer.parseInt( value ); setPreference( editor, kay, mOrientation );
              // must run on UI thread
              // TopoDroidApp.setScreenOrientation( );
              TDandroid.setScreenOrientation( TDPrefActivity.mPrefActivityAll );
              break;
            case "DISTOX_CALIB_SHOT_DOWNLOAD":
              mCalibShotDownload = Boolean.parseBoolean( value ); setPreference( editor, kay, mCalibShotDownload );
              break;
            case "DISTOX_RAW_CDATA":
              mRawCData = Integer.parseInt( value );              setPreference( editor, kay, mRawCData );
              break;
            case "DISTOX_TH2_EDIT":
              mTh2Edit = Boolean.parseBoolean( value ); setPreference( editor, kay, mTh2Edit );
              break;
            // case "DISTOX_WITH_DEBUG":
            //   mWithDebug = Boolean.parseBoolean( value ); setPreference( editor, kay, mWithDebug );
            // break;
            case "DISTOX_TAMPERING":
              mEditableShots = Boolean.parseBoolean( value ); setPreference( editor, kay, mEditableShots );
              break;
            case "DISTOX_THUMBNAIL":
              mThumbSize     = Integer.parseInt( value ); setPreference( editor, kay, mThumbSize );
              break;
            case "DISTOX_ZOOM_CTRL":
              mZoomCtrl = Integer.parseInt( value );
              setZoomControls( value, TDandroid.checkMultitouch( TDInstance.context ) );
              setPreference( editor, kay, mZoomCtrl );
              break;
            case "DISTOX_SIDE_DRAG":
              mSideDrag = Boolean.parseBoolean( value ); setPreference( editor, kay, mSideDrag );
              break;
            case "DISTOX_STYLUS_SIZE":
              setStylusSize( Float.parseFloat( value ) ); setPreference( editor, kay, mStylusSize ); // STYLUS_MM
              break;
            // case "DISTOX_BACKUPS_CLEAR":
            //   mBackupsClear = Boolean.parseBoolean( value ); setPreference( editor, kay, mBackupsClear ); // CLEAR_BACKUPS
            //   break;
            case "DISTOX_DRAWING_UNIT":
              setDrawingUnitIcons( Float.parseFloat( value ) ); setPreference( editor, kay, mUnitIcons );
              break;
            case "DISTOX_LINE_UNITS":
              setDrawingUnitLines( Float.parseFloat( value ) ); setPreference( editor, kay, mUnitLines );
              break;
            case "DISTOX_UNIT_GRID":
              mUnitGrid  = Float.parseFloat( value );    setPreference( editor, kay, mUnitGrid );
              break;
            case "DISTOX_UNIT_MEASURE":
              mUnitMeasure = Float.parseFloat( value ); setPreference( editor, kay, mUnitMeasure );
              break;
            case "DISTOX_STATION_SIZE":
              mStationSize    = Float.parseFloat( value ); setPreference( editor, kay, mStationSize );
              break;
            case "DISTOX_LABEL_SIZE":
              mLabelSize      = Float.parseFloat( value ); setPreference( editor, kay, mLabelSize );
              break;
            case "DISTOX_FIXED_THICKNESS":
              mFixedThickness = Float.parseFloat( value );  setPreference( editor, kay, mFixedThickness );
              break;
            case "DISTOX_LINE_THICKNESS":
              mLineThickness  = Float.parseFloat( value );  setPreference( editor, kay, mLineThickness );
              break;
            case "DISTOX_SCALABLE_LABEL":
              mScalableLabel = Boolean.parseBoolean( value ); setPreference( editor, kay, mScalableLabel );
              break;
            case "DISTOX_XSECTION_OFFSET":
              mXSectionOffset = Integer.parseInt( value ); setPreference( editor, kay, mXSectionOffset );
              break;
            case "DISTOX_CLOSENESS":
              mSelectness = Float.parseFloat( value );  setPreference( editor, kay, mSelectness );
              break;
            case "DISTOX_POINTING":
              mPointingRadius = Integer.parseInt( value );   setPreference( editor, kay, mPointingRadius );
              break;
            case "DISTOX_MIN_SHIFT":
              mMinShift = Integer.parseInt( value );         setPreference( editor, kay, mMinShift );
              break;
            case "DISTOX_DOT_RADIUS":
              mDotRadius = Float.parseFloat( value );    setPreference( editor, kay, mDotRadius );
              break;
            case "DISTOX_PATH_MULTISELECT":
              mPathMultiselect = Boolean.parseBoolean( value ); setPreference( editor, kay, mPathMultiselect );
              break;
            case "DISTOX_ERASENESS":
              mEraseness = Float.parseFloat( value ); setPreference( editor, kay, mEraseness );
              break;
            // case "DISTOX_PICKER_TYPE":
            //   mPickerType = Integer.parseInt( value ); setPreference( editor, kay, mPickerType );
            //   break;
            case "DISTOX_UNSCALED_POINTS":
              mUnscaledPoints = Boolean.parseBoolean( value ); setPreference( editor, kay, mUnscaledPoints );
              break;
            case "DISTOX_LINE_STYLE":
              setLineStyleAndType( value ); setPreference( editor, kay, mLineStyle );
              break;
            case "DISTOX_LINE_SEGMENT":
              setLineSegment( Integer.parseInt( value ) ); setPreference( editor, kay, mLineSegment );
              break;
            // caes "DISTOX_LINE_CONTINUE":
            //   mContinueLine = Integer.parseInt( value );        setPreference( editor, kay, mContinueLine );
            //   break;
            case "DISTOX_ARROW_LENGTH":
              mArrowLength  = Float.parseFloat( value ); setPreference( editor, kay, mArrowLength );
              break;
            case "DISTOX_LINE_CLOSE":
              mLineClose = Boolean.parseBoolean( value ); setPreference( editor, kay, mLineClose );
              break;
            case "DISTOX_SLOPE_LSIDE":
              mSlopeLSide = Integer.parseInt( value ); setPreference( editor, kay, mSlopeLSide );
              break;
            case "DISTOX_BEZIER_STEP":
              mBezierStep   = Float.parseFloat( value );  setPreference( editor, kay, mBezierStep );
              break;
            case "DISTOX_LINE_ACCURACY":
              mLineAccuracy = Float.parseFloat( value );  setPreference( editor, kay, mLineAccuracy );
              break;
            case "DISTOX_LINE_CORNER":
              mLineCorner   = Float.parseFloat( value ); setPreference( editor, kay, mLineCorner );
              break;
            case "DISTOX_WEED_DISTANCE":
              mWeedDistance = Float.parseFloat( value );  setPreference( editor, kay, mWeedDistance );
              break;
            case "DISTOX_WEED_LENGTH":
              mWeedLength   = Float.parseFloat( value );  setPreference( editor, kay, mWeedLength );
              break;
            case  "DISTOX_WEED_BUFFER":
              mWeedBuffer   = Float.parseFloat( value ); setPreference( editor, kay, mWeedBuffer );
              break;
            case "DISTOX_AREA_BORDER":
              mAreaBorder = Boolean.parseBoolean( value ); setPreference( editor, kay, mAreaBorder );
              break;
            case "DISTOX_BACKUP_NUMBER":
              mBackupNumber = Integer.parseInt( value );   setPreference( editor, kay, mBackupNumber );
              break;
            case "DISTOX_BACKUP_INTERVAL":
              mBackupInterval = Integer.parseInt( value ); setPreference( editor, kay, mBackupInterval );
              break;

            case "DISTOX_BLUETOOTH":
              mCheckBT  = Integer.parseInt( value ); setPreference( editor, kay, mCheckBT );
              break;
            case "DISTOX_AUTO_PAIR":
              mAutoPair = Boolean.parseBoolean( value ); setPreference( editor, kay, mAutoPair );
              break;
            case "DISTOX_SOCKET_TYPE":
              mSockType = Integer.parseInt( value ); setPreference( editor, kay, mSockType );
              break;
            case "DISTOX_SOCKET_DELAY":
              mConnectSocketDelay = Integer.parseInt( value ); setPreference( editor, kay, mConnectSocketDelay );
              break;
            case "DISTOX_CONN_MODE":
                mConnectionMode  = Integer.parseInt( value );     setPreference( editor, kay, mConnectionMode );
              break;
            // case "DISTOX_Z6_WORKAROUND":
                // mZ6Workaround    = Boolean.parseBoolean( value ); setPreference( editor, kay, mZ6Workaround );
            //  break;
            case "DISTOX_CONNECT_FEEDBACK":
              mConnectFeedback = Integer.parseInt( value );     setPreference( editor, kay, mConnectFeedback );
              break;
            case "DISTOX_UNNAMED_DEVICE":
              mUnnamedDevice = Boolean.parseBoolean( value ); setPreference( editor, kay, mUnnamedDevice );
              break;
            // case "DISTOX_AUTO_RECONNECT":
            // mAutoReconnect = Boolean.parseBoolean( value );  setPreference( editor, kay, mAutoReconnect );
            // break;
            case "DISTOX_SECOND_DISTOX":
              mSecondDistoX = Boolean.parseBoolean( value );  setPreference( editor, kay, mSecondDistoX );
              break;
            case "DISTOX_COMM_RETRY":
              mCommRetry = Integer.parseInt( value );      setPreference( editor, kay, mCommRetry );
              break;
            case "DISTOX_HEAD_TAIL":
              mHeadTail = Boolean.parseBoolean( value ); setPreference( editor, kay,  mHeadTail );
              break;
            case "DISTOX_PACKET_LOGGER":
              mPacketLog = Boolean.parseBoolean( value ); setPreference( editor, kay, mPacketLog );
              break;
        
            case "DISTOX_PALETTES":
              setPalettes( Boolean.parseBoolean( value ) ); setPreference( editor, kay, mPalettes );
              break;
            case "DISTOX_EXPORT_SHOTS":
              mExportShotsFormat = Integer.parseInt( value ); setPreference( editor, kay, mExportShotsFormat );
              break;
            case "DISTOX_EXPORT_PLOT":
              mExportPlotFormat  = Integer.parseInt( value ); setPreference( editor, kay,  mExportPlotFormat );
              break;
            case "DISTOX_AUTO_PLOT_EXPORT":
              mAutoExportPlotFormat = Integer.parseInt( value ); setPreference( editor, kay,  mAutoExportPlotFormat );
              break;
            // case "DISTOX_DATA_BACKUP":
            //   mDataBackup = Boolean.parseBoolean( value ); setPreference( editor, kay, mDataBackup );
            //   break;
            case  "DISTOX_SURVEX_EOL":
              mSurvexEol = value; setPreference( editor, kay, ( mSurvexEol.equals("\n")? "LF" : "LFCR" ) );
              break;
            case "DISTOX_SURVEX_SPLAY":
              mSurvexSplay = Boolean.parseBoolean( value ); setPreference( editor, kay, mSurvexSplay );
              break;
            case "DISTOX_SURVEX_LRUD":
              mSurvexLRUD  = Boolean.parseBoolean( value ); setPreference( editor, kay,  mSurvexLRUD );
              break;
            case "DISTOX_SURVEX_EPSG":
                mSurvexEPSG = Integer.parseInt( value ); setPreference( editor, kay, mSurvexEPSG );
              break;
            case "DISTOX_PLY_LRUD":
              mPlyLRUD  = Boolean.parseBoolean( value ); setPreference( editor, kay,  mPlyLRUD );
              break;
            case "DISTOX_PLY_MINUS":
              mPlyMinus = Boolean.parseBoolean( value ); setPreference( editor, kay, mPlyMinus );
              break;
            case "DISTOX_SWAP_LR":
              mSwapLR = Boolean.parseBoolean( value ); setPreference( editor, kay, mSwapLR );
              break;
            case "DISTOX_STATION_PREFIX":
              mExportStationsPrefix = Boolean.parseBoolean( value ); setPreference( editor, kay, mExportStationsPrefix );
              break;
            case "DISTOX_COMPASS_SPLAYS":
              mCompassSplays = Boolean.parseBoolean( value ); setPreference( editor, kay, mCompassSplays );
              break;
            case "DISTOX_WITH_MEDIA":
                mExportMedia = Boolean.parseBoolean( value ); setPreference( editor, kay, mExportMedia );
              break;
            case "DISTOX_WALLS_SPLAYS":
              mWallsSplays = Boolean.parseBoolean( value ); setPreference( editor, kay, mWallsSplays );
              break;
            // case "DISTOX_WALLS_UD":
            //   mWallsUD = Integer.parseInt( value ); setPreference( editor, kay, mWallsUD );
            // break;
            case "DISTOX_VTOPO_SPLAYS":
              mVTopoSplays     = Boolean.parseBoolean( value ); setPreference( editor, kay, mVTopoSplays );
              break;
            case "DISTOX_VTOPO_LRUD":
              mVTopoLrudAtFrom = Boolean.parseBoolean( value ); setPreference( editor, kay,   mVTopoLrudAtFrom );
              break;
            case "DISTOX_VTOPO_TROX":
              mVTopoTrox     = Boolean.parseBoolean( value ); setPreference( editor, kay,   mVTopoTrox );
              break;
            case "DISTOX_ORTHO_LRUD":
              mOrthogonalLRUDAngle  = Float.parseFloat( value ); setPreference( editor, kay, mOrthogonalLRUDAngle );
              mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
              mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
              break;
            case "DISTOX_THERION_CONFIG":
              mTherionWithConfig = Boolean.parseBoolean( value ); setPreference( editor, kay, mTherionWithConfig );
              break;
            case "DISTOX_THERION_MAPS":
              mTherionMaps   = Boolean.parseBoolean( value ); setPreference( editor, kay,   mTherionMaps );
              break;
            case "DISTOX_AUTO_STATIONS":
              mAutoStations  = Boolean.parseBoolean( value );  setPreference( editor, kay,  mAutoStations );
              break;
            case "DISTOX_THERION_SPLAYS":
              mTherionSplays = Boolean.parseBoolean( value ); setPreference( editor, kay, mTherionSplays );
              break;
            case  "DISTOX_TH2_XVI":
              mTherionXvi    = Boolean.parseBoolean( value ); setPreference( editor, kay,  mTherionXvi );
              break;
            case "DISTOX_TH2_SCALE":
              // the next line sets mTherionScale
              setExportScale( Integer.parseInt( value ) ); setPreference( editor, kay, mTherionScale );
              break;
            // case "DISTOX_BITMAP_SCALE":
            //   mBitmapScale   = Float.parseFloat( value ); setPreference( editor, kay,  mBitmapScale );
            //   break;
            // case "DISTOX_BITMAP_BGCOLOR":
            //   mBitmapBgcolor = Integer.parseInt( value );   setPreference( editor, kay, mBitmapBgcolor );
            //   mBitmapBgcolor |= 0xff000000;
            //   break;
            case "DISTOX_ACAD_VERSION":
              mAcadVersion = Integer.parseInt( value ); setPreference( editor, kay, mAcadVersion );
              break;
            case "DISTOX_DXF_BLOCKS":
              mDxfBlocks   = Boolean.parseBoolean( value ); setPreference( editor, kay, mDxfBlocks );
              break;
            case "DISTOX_ACAD_SPLINE":
              mAcadSpline  = Boolean.parseBoolean( value ); setPreference( editor, kay, mAcadSpline );
              break;
            case "DISTOX_ACAD_LAYER":
              mAcadLayer  = Boolean.parseBoolean( value ); setPreference( editor, kay, mAcadLayer );
              break;
            case  "DISTOX_SVG_SHOT_STROKE":
              mSvgShotStroke  = Float.parseFloat( value );    setPreference( editor, kay, mSvgShotStroke );
              break;
            case "DISTOX_SVG_LABEL_STROKE":
              mSvgLabelStroke = Float.parseFloat( value );    setPreference( editor, kay, mSvgLabelStroke );
              break;
            case "DISTOX_SVG_LABEL_SIZE":
              mSvgLabelSize   = Integer.parseInt( value );        setPreference( editor, kay, mSvgLabelSize );
              break;
            case "DISTOX_SVG_STATION_SIZE":
              mSvgStationSize = Integer.parseInt( value );        setPreference( editor, kay, mSvgStationSize );
              break;
            case "DISTOX_SVG_POINT_STROKE":
              mSvgPointStroke = Float.parseFloat( value );        setPreference( editor, kay, mSvgPointStroke );
              break;
            case "DISTOX_SVG_ROUNDTRIP":
              mSvgRoundTrip   = Boolean.parseBoolean( value );    setPreference( editor, kay, mSvgRoundTrip );
              break;
            case "DISTOX_SVG_GRID":
              mSvgGrid        = Boolean.parseBoolean( value );    setPreference( editor, kay, mSvgGrid );
              break;
            case "DISTOX_SVG_GRID_STROKE":
              mSvgGridStroke  = Float.parseFloat( value );        setPreference( editor, kay, mSvgGridStroke );
              break;
            case "DISTOX_SVG_LINE_STROKE":
              mSvgLineStroke  = Float.parseFloat( value );        setPreference( editor, kay, mSvgLineStroke );
              break;
            case "DISTOX_SVG_LINE_DIR":
              mSvgLineDirection = Boolean.parseBoolean( value );  setPreference( editor, kay, mSvgLineDirection );
              break;
            case "DISTOX_SVG_LINEDIR_STROKE":
              mSvgLineDirStroke = Float.parseFloat( value );      setPreference( editor, kay, mSvgLineDirStroke );
              break;
            case "DISTOX_SVG_SPLAYS":
              mSvgSplays = Boolean.parseBoolean( value );         setPreference( editor, kay, mSvgSplays );
              break;
            case "DISTOX_SVG_GROUPS":
              mSvgGroups = Boolean.parseBoolean( value );         setPreference( editor, kay, mSvgGroups );
              break;
            // case  "DISTOX_SVG_PROGRAM":
            //   mSvgProgram     = Integer.parseInt( value );
            //   if ( mSvgProgram != 1 ) mSvgProgram = 0;  // either 1 (Illustrator) or 0 (Inkscape) 
            //   setPreference( editor, kay, mSvgProgram );
            //   setExportScale( mTherionScale );
            // break;
            case "DISTOX_SHP_GEOREF":
              mShpGeoref = Boolean.parseBoolean( value ); setPreference( editor, kay, mShpGeoref );
              break;
            case "DISTOX_GPX_SINGLE_TRACK":
              mGPXSingleTrack = Boolean.parseBoolean( value ); setPreference( editor, kay, mGPXSingleTrack );
              break;
            case "DISTOX_KML_STATIONS":
              mKmlStations = Boolean.parseBoolean( value ); setPreference( editor, kay, mKmlStations );
              break;
            case "DISTOX_KML_SPLAYS":
              mKmlSplays   = Boolean.parseBoolean( value ); setPreference( editor, kay,   mKmlSplays );
              break;
            case "DISTOX_CSV_RAW":
              mCsvRaw = Boolean.parseBoolean( value ); setPreference( editor, kay, mCsvRaw );
              break;
            case "DISTOX_CSV_SEP":
              mCsvSeparator = getQuotedChar( line ); 
              int sep = ( mCsvSeparator == CSV_COMMA )? 0 : ( mCsvSeparator == CSV_PIPE )? 1 : 2; // CSV_TAB
              setPreference( editor, kay, sep );
              break;
            case "DISTOX_WAIT_LASER":
                mWaitLaser   = Integer.parseInt( value );  setPreference( editor, kay, mWaitLaser );
              break;
            case "DISTOX_WAIT_SHOT":
                mWaitShot    = Integer.parseInt( value );  setPreference( editor, kay,  mWaitShot );
              break;
            case "DISTOX_WAIT_DATA":
                mWaitData    = Integer.parseInt( value );  setPreference( editor, kay,  mWaitData );
              break;
            case "DISTOX_WAIT_CONN":
                mWaitConn    = Integer.parseInt( value ); setPreference( editor, kay,  mWaitConn );
              break;
            // case "DISTOX_WAIT_COMMAND":
            //   mWaitCommand = Integer.parseInt( value ); setPreference( editor, kay, mWaitCommand );
            //   break;
            case "DISTOX_GROUP_BY":
              mGroupBy       = Integer.parseInt( value ); setPreference( editor, kay, mGroupBy );
              break;
            case "DISTOX_GROUP_DISTANCE":
              mGroupDistance = Float.parseFloat( value ); setPreference( editor, kay, mGroupDistance );
              break;
            case "DISTOX_CALIB_ALGO":
              // mCalibAlgo  = Integer.parseInt( value ); setPreference( editor, kay, mCalibAlgo );
              break;
            case "DISTOX_CALIB_EPS":
              mCalibEps   = Float.parseFloat( value ); setPreference( editor, kay, mCalibEps );
              break;
            case "DISTOX_CALIB_MAX_IT":
              mCalibMaxIt = Integer.parseInt( value ); setPreference( editor, kay, mCalibMaxIt );
              break;
            // case "DISTOX_MIN_ALPHA":
            //   mAlgoMinAlpha = Float.parseFloat( value ); setPreference( editor, kay, mAlgoMinAlpha );
            //   break;
            // case "DISTOX_MIN_BETA":
            //   mAlgoMinBeta  = Float.parseFloat( value ); setPreference( editor, kay,  mAlgoMinBeta );
            //   break;
            // case "DISTOX_MIN_GAMMA":
            //   mAlgoMinGamma = Float.parseFloat( value ); setPreference( editor, kay, mAlgoMinGamma );
            //   break;
            // case "DISTOX_MIN_DELTA":
            //   mAlgoMinDelta = Float.parseFloat( value ); setPreference( editor, kay, mAlgoMinDelta );
            //   break;

            case "DISTOX_TEAM":
              mDefaultTeam = value; setPreference( editor, kay, mDefaultTeam );
              break;
            case "DISTOX_TEAM_DIALOG":
              mTeamNames = Integer.parseInt( value ); setPreference( editor, kay,   mTeamNames );
              break;
            case "DISTOX_CHECK_ATTACHED":
              mCheckAttached = Boolean.parseBoolean( value ); setPreference( editor, kay, mCheckAttached );
              break;
            case "DISTOX_CHECK_EXTEND":
              mCheckExtend   = Boolean.parseBoolean( value ); setPreference( editor, kay,   mCheckExtend );
              break;
            case "DISTOX_UNIT_LOCATION":
              mUnitLocation = Integer.parseInt( value );
              setPreference( editor, kay, ( mUnitLocation == TDUtil.DDMMSS ? "ddmmss" : "degrees" ) );
              break;
            case "DISTOX_CRS":
              mCRS = value; setPreference( editor, kay, mCRS );
              break;
            case "DISTOX_NEG_ALTITUDE":
              mNegAltitude = Boolean.parseBoolean( value ); setPreference( editor, kay,   mNegAltitude );
              break;
            case "DISTOX_FINE_LOCATION":
              mFineLocation = Integer.parseInt( value ); setPreference( editor, kay, mFineLocation );
              break;
            case "DISTOX_GEOPOINT_APP":
              mGeoImportApp = Integer.parseInt( value ); setPreference( editor, kay, mGeoImportApp );
              break;
            case "DISTOX_EDIT_ALTITUDE":
              mEditableHGeo = Boolean.parseBoolean( value ); setPreference( editor, kay, mEditableHGeo );
              break;
            case "DISTOX_VTHRESHOLD":
              mVThreshold = Float.parseFloat( value ); setPreference( editor, kay, mVThreshold );
              break;
            case "DISTOX_HTHRESHOLD":
              mHThreshold = Float.parseFloat( value ); setPreference( editor, kay, mHThreshold );
              break;
            case "DISTOX_BACKSHOT":
              mDistoXBackshot = Boolean.parseBoolean( value ); setPreference( editor, kay, mDistoXBackshot );
              break;
            case "DISTOX_DIVING_MODE":
              mDivingMode     = Boolean.parseBoolean( value ); setPreference( editor, kay, mDivingMode );
              break;
            case "DISTOX_BACKSIGHT":
              mBacksightInput = Boolean.parseBoolean( value ); setPreference( editor, kay, mBacksightInput );
              break;
            case "DISTOX_PREV_NEXT":
              mPrevNext       = Boolean.parseBoolean( value ); setPreference( editor, kay, mPrevNext );
              break;
            case "DISTOX_SPLAY_EXTEND":
              mLRExtend = Boolean.parseBoolean( value ); setPreference( editor, kay, mLRExtend );
              break;
            case "DISTOX_BLUNDER_SHOT":
              mBlunderShot = Boolean.parseBoolean( value ); setPreference( editor, kay, mBlunderShot );
              break;
            case "DISTOX_SPLAY_STATION":
              mSplayStation = Boolean.parseBoolean( value ); setPreference( editor, kay, mSplayStation );
              break;
            case "DISTOX_SPLAY_GROUP":
              mSplayOnlyForward = Boolean.parseBoolean( value ); setPreference( editor, kay, mSplayOnlyForward );
              break;
            case "DISTOX_LRUD_VERTICAL":
              mLRUDvertical   = Float.parseFloat( value ); setPreference( editor, kay,   mLRUDvertical );
              break;
            case "DISTOX_LRUD_HORIZONTAL":
              mLRUDhorizontal = Float.parseFloat( value ); setPreference( editor, kay, mLRUDhorizontal );
              break;
            case  "DISTOX_LRUD_COUNT":
              mLRUDcount = Boolean.parseBoolean( value ); setPreference( editor, kay, mLRUDcount );
              break;
            case "DISTOX_IMPORT_DATAMODE":
              mImportDatamode = Integer.parseInt( value ); setPreference( editor, kay, mImportDatamode );
              break;
            case "DISTOX_ZIP_WITH_SYMBOLS":
              mZipWithSymbols = Boolean.parseBoolean( value ); setPreference( editor, kay, mZipWithSymbols );
              break;
            case "DISTOX_ZIP_SHARE_CATEGORY":
              mZipShareCategory = Boolean.parseBoolean( value ); setPreference( editor, kay, mZipShareCategory ); // DISTOX_ZIP_SHARE_CATEGORY
              break;
            case "DISTOX_SHOT_TIMER":
              mTimerWait  = Integer.parseInt( value ); setPreference( editor, kay,  mTimerWait );
              break;
            case "DISTOX_BEEP_VOLUME":
              setBeepVolume( Integer.parseInt( value ) ); setPreference( editor, kay, mBeepVolume );
              break;
            case "DISTOX_RECENT_SHOT":
              mShotRecent = Boolean.parseBoolean( value ); setPreference( editor, kay,    mShotRecent );
              break;
            case "DISTOX_RECENT_TIMEOUT":
              mRecentTimeout = Integer.parseInt( value );  setPreference( editor, kay, mRecentTimeout );
              break;
            case "DISTOX_CLOSE_DISTANCE":
              mCloseDistance = Float.parseFloat( value ); setPreference( editor, kay, mCloseDistance );
              break;
            case "DISTOX_LEG_SHOTS":
              mMinNrLegShots = Integer.parseInt( value );       setPreference( editor, kay, mMinNrLegShots );
              break;
            case "DISTOX_LEG_FEEDBACK":
              mTripleShot    = Integer.parseInt( value );       setPreference( editor, kay, mTripleShot );
              break;
            case "DISTOX_MAX_SHOT_LENGTH":
              mMaxShotLength = Float.parseFloat( value ); setPreference( editor, kay, mMaxShotLength );
              break;
            case "DISTOX_MIN_LEG_LENGTH":
              mMinLegLength  = Float.parseFloat( value ); setPreference( editor, kay, mMinLegLength );
              break;
            case "DISTOX_SPLAY_VERT_THRS":
              mSplayVertThrs = Float.parseFloat( value ); setPreference( editor, kay, mSplayVertThrs );
              break;
            case "DISTOX_SPLAY_CLASSES":
              mSplayClasses  = Boolean.parseBoolean( value );      setPreference( editor, kay, mSplayClasses );
              break;
            // case "DISTOX_SPLAY_AS_DOT":
            //   mSplayAsDot    = Boolean.parseBoolean( value );      setPreference( editor, kay, mSplayAsDot );
            //   break;
            case "DISTOX_STATION_NAMES":
              mStationNames = Integer.parseInt( value ); setPreference( editor, kay, mStationNames );
              break;
            case "DISTOX_INIT_STATION":
              mInitStation  = value; if ( mInitStation.length() > 0 ) setPreference( editor, kay, mInitStation );
              break;
            case "DISTOX_EXTEND_THR2":
              mExtendThr     = Float.parseFloat( value );   setPreference( editor, kay, mExtendThr );
              break;
            case "DISTOX_AZIMUTH_MANUAL":
              mAzimuthManual = Boolean.parseBoolean( value ); setPreference( editor, kay, mAzimuthManual );
              break;
            case "DISTOX_EXTEND_FRAC":
              mExtendFrac    = Boolean.parseBoolean( value ); setPreference( editor, kay, mExtendFrac );
              break;
            case "DISTOX_LOOP_CLOSURE_VALUE":
              mLoopClosure = Integer.parseInt( value ); setPreference( editor, kay, mLoopClosure );
              break;
            case "DISTOX_LOOP_THR":
              mLoopThr = Float.parseFloat( value ); setPreference( editor, kay, mLoopThr );
              break;
            case "DISTOX_UNIT_LENGTH":
              if ( value.equals( TDPrefKey.mUnits[0].dflt ) ) {
                  mUnitLength = 1.0f;
                  mUnitLengthStr = "m";
                } else {
                  mUnitLength = TDUtil.M2FT;
                  mUnitLengthStr = "ft";
                }
              setPreference( editor, kay, ( mUnitLength > 0.99f ? "meters" : "feet" ) );
              break;
            case "DISTOX_UNIT_ANGLE":
              if ( value.equals( TDPrefKey.mUnits[1].dflt ) ) {
                mUnitAngle = 1.0f;
                mUnitAngleStr = "deg";
              } else {
                mUnitAngle = TDUtil.DEG2GRAD;
                mUnitAngleStr = "grad";
              }
              setPreference( editor, kay,  ( mUnitAngle > 0.99f ?  "degrees" : "grads" ) );
              break;
            case "DISTOX_SAVED_STATIONS":
              mSavedStations = Boolean.parseBoolean( value );  setPreference( editor, kay, mSavedStations );
              break;
            case "DISTOX_LEGONLY_UPDATE":
              mLegOnlyUpdate = Boolean.parseBoolean( value );  setPreference( editor, kay, mLegOnlyUpdate );
              break;
            case "DISTOX_ANDROID_AZIMUTH":
              mWithAzimuth   = Boolean.parseBoolean( value );  setPreference( editor, kay, mWithAzimuth );
              break;
            case "DISTOX_WITH_SENSORS":
              mWithSensors   = Boolean.parseBoolean( value );  setPreference( editor, kay, mWithSensors );
              break;
            case "DISTOX_BEDDING":
              mBedding       = Boolean.parseBoolean( value ); setPreference( editor, kay, mBedding );
              break;

            case "DISTOX_FIXED_ORIGIN":
              mFixedOrigin = Boolean.parseBoolean( value );  setPreference( editor, kay, mFixedOrigin );
              break;
            case "DISTOX_PLOT_SPLIT":
              mPlotSplit   = Boolean.parseBoolean( value );  setPreference( editor, kay,   mPlotSplit );
              break;
            case "DISTOX_PLOT_SHIFT":
              mPlotShift   = Boolean.parseBoolean( value ); setPreference( editor, kay,   mPlotShift );
              break;
            case "DISTOX_WITH_LEVELS":
              mWithLevels  = Integer.parseInt( value );  setPreference( editor, kay,  mWithLevels );
              break;
            case "DISTOX_FULL_AFFINE":
              mFullAffine  = Boolean.parseBoolean( value ); setPreference( editor, kay,  mFullAffine );
              break;
            case "DISTOX_SLANT_XSECTION":
              mSlantXSection = Boolean.parseBoolean( value ); setPreference( editor, kay, mSlantXSection );
              break;
            case "DISTOX_OBLIQUE_PROJECTED":
              mObliqueMax = Integer.parseInt( value ); setPreference( editor, kay, mObliqueMax );
              break;

            case "DISTOX_SHARED_XSECTIONS":
              mSharedXSections = Boolean.parseBoolean( value ); setPreference( editor, kay, mSharedXSections );
              break;
            case "DISTOX_AUTO_XSECTIONS":
              mAutoXSections   = Boolean.parseBoolean( value ); setPreference( editor, kay, mAutoXSections );
              break;
            case "DISTOX_AUTO_SECTION_PT":
              mAutoSectionPt   = Boolean.parseBoolean( value ); setPreference( editor, kay, mAutoSectionPt );
              break;
            case "DISTOX_LINE_SNAP":
              mLineSnap     = Boolean.parseBoolean( value ); setPreference( editor, kay, mLineSnap );
              break;
            case "DISTOX_LINE_CURVE":
              mLineCurve    = Boolean.parseBoolean( value ); setPreference( editor, kay, mLineCurve );
              break;
            case "DISTOX_LINE_STRAIGHT":
              mLineStraight = Boolean.parseBoolean( value ); setPreference( editor, kay, mLineStraight );
              break;
            case "DISTOX_REDUCE_ANGLE":
              setReduceAngle( Float.parseFloat( value ) ); setPreference( editor, kay, mReduceAngle );
              break;
            case  "DISTOX_SPLAY_ALPHA":
              mSplayAlpha = Integer.parseInt( value );  setPreference( editor, kay, mSplayAlpha );
              break;
            case "DISTOX_SPLAY_COLOR":
              // mSplayColor = Boolean.parseBoolean( value );  setPreference( editor, kay, mSplayColor );
              break;
            case "DISTOX_DISCRETE_COLORS":
              mDiscreteColors = Integer.parseInt( value );  setPreference( editor, kay, mDiscreteColors );
              mSplayColor = (mDiscreteColors > 0);
              break;
            case "DISTOX_SPLAY_DASH":
              mDashSplay  = Integer.parseInt( value );   setPreference( editor, kay, mDashSplay );
              break;
            case "DISTOX_VERT_SPLAY":
              mVertSplay  = Float.parseFloat( value );    setPreference( editor, kay,  mVertSplay );
              break;
            case  "DISTOX_HORIZ_SPLAY":
              mHorizSplay = Float.parseFloat( value );   setPreference( editor, kay, mHorizSplay );
              mCosHorizSplay = TDMath.cosd( mHorizSplay );  
              break;
            case "DISTOX_SECTION_SPLAY":
              mSectionSplay = Float.parseFloat( value ); setPreference( editor, kay, mSectionSplay );
              mCosSectionSplay  = TDMath.cosd( mSectionSplay );
              break;
            case "DISTOX_SPLAY_DASH_COLOR":
              mSplayDashColor = Integer.parseInt( value );   setPreference( editor, kay,  mSplayDashColor );
              break;
            case  "DISTOX_SPLAY_DOT_COLOR":
              mSplayDotColor  = Integer.parseInt( value );   setPreference( editor, kay,   mSplayDotColor );
              break;
            case "DISTOX_SPLAY_LATEST_COLOR":
              mSplayLatestColor  = Integer.parseInt( value ); setPreference( editor, kay,   mSplayLatestColor ); 
              break;
            case "DISTOX_ACCEL_PERCENT":
              mAccelerationThr = Float.parseFloat( value ); setPreference( editor, kay, mAccelerationThr );
              break;
            case  "DISTOX_MAG_PERCENT":
              mMagneticThr     = Float.parseFloat( value ); setPreference( editor, kay,   mMagneticThr );
              break;
            case "DISTOX_DIP_THR":
              mDipThr          = Float.parseFloat( value ); setPreference( editor, kay,       mDipThr );
              break;
            case "DISTOX_SIBLING_PERCENT":
              mSiblingThr    = Float.parseFloat( value ); setPreference( editor, kay, mSiblingThr );
              break;
            // case "DISTOX_WALLS_TYPE":
            //   mWallsType        = Integer.parseInt( value );    setPreference( editor, kay,         mWallsType );
            //   break;
            // case  "DISTOX_WALLS_PLAN_THR":
            //   mWallsPlanThr     = Float.parseFloat( value );  setPreference( editor, kay,     mWallsPlanThr );
            //   break;
            // case  "DISTOX_WALLS_EXTENDED_THR":
            //   mWallsExtendedThr = Float.parseFloat( value );  setPreference( editor, kay, mWallsExtendedThr );
            //   break;
            // case "DISTOX_WALLS_XCLOSE":
            //   mWallsXClose      = Float.parseFloat( value );  setPreference( editor, kay,       mWallsXClose );
            //   break;
            // case "DISTOX_WALLS_XSTEP":
            //   mWallsXStep       = Float.parseFloat( value ); setPreference( editor, kay,        mWallsXStep );
            //   break;
            // case "DISTOX_WALLS_CONCAVE":
            //   mWallsConcave     = Float.parseFloat( value ); setPreference( editor, kay,      mWallsConcave );
            //   break;
          } // switch ( kay )
        }
      }
      fr.close();
    } catch ( IOException e ) { 
      TDLog.e("failed to import settings"); 
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
