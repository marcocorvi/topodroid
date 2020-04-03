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
package com.topodroid.prefs;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDColor;
import com.topodroid.help.UserManDownload;
import com.topodroid.DistoX.TDLevel;
import com.topodroid.DistoX.TDandroid;
import com.topodroid.DistoX.TDUtil;
import com.topodroid.DistoX.TDPath;
import com.topodroid.DistoX.TDAzimuth;
import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.BrushManager;
import com.topodroid.DistoX.DrawingWindow;
import com.topodroid.DistoX.DistoXStationName;
import com.topodroid.DistoX.StationPolicy;
import com.topodroid.DistoX.SurveyInfo;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;


import android.util.Log;

import java.util.Locale;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import android.content.res.Resources;

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
  private static String defaultButtonSize = TDString.ONE;

  public static String setTextSize( int ts )
  {
    float ds = TopoDroidApp.getDisplayDensity() / 3.0f;
    mTextSize = (int)( ( ds * ts ) );
    if ( mTextSize < MIN_SIZE_TEXT ) {
      mTextSize = MIN_SIZE_TEXT;
      return Integer.toString( mTextSize );
    }
    return null;
  }

  // get tentative text size
  public static int getTextSize( int ts )
  {
    float ds = TopoDroidApp.getDisplayDensity() / 3.0f;
    return (int)( ( ds * ts ) );
  }

  public static boolean setLabelSize( float f, boolean brush )
  {
    if ( f >= 1 && f != mLabelSize ) {
      mLabelSize = f;
      if ( brush ) BrushManager.setTextSizes( );
      return true;
    }
    return false;
  }

  public static boolean setStationSize( float f, boolean brush )
  {
    if ( f >= 1 && f != mStationSize ) {
      mStationSize = f;
      if ( brush ) BrushManager.setTextSizes( );
      return true;
    }
    return false;
  }

  public static void setDrawingUnitIcons( float f )
  {
    if ( f > 0.1f && f != mUnitIcons ) {
      mUnitIcons = f;
      BrushManager.reloadPointLibrary( TDInstance.context, TDInstance.getResources() );
    }
  }

  public static void setDrawingUnitLines( float f )
  {
    if ( f > 0.1f && f != mUnitLines ) {
      mUnitLines = f;
      BrushManager.reloadLineLibrary( TDInstance.getResources() );
    }
  }

  public static boolean mDxfBlocks = true; // DXF_BLOCKS
  public static boolean mShpGeoref = false;

  public static float mAlgoMinAlpha = 0.1f;
  public static float mAlgoMinBeta  = 4.0f;
  public static float mAlgoMinGamma = 1.0f;
  public static float mAlgoMinDelta = 1.0f;

  public static String keyDeviceName() { return "DISTOX_DEVICE"; }

  // static final  String EXPORT_TYPE    = "th";    // DISTOX_EXPORT_TH

  // prefs default values
  public static String  mDefaultTeam = "";

  public static final int MIN_SIZE_BUTTONS = 32;
  public static final int MIN_SIZE_TEXT    = 12;
  public static final int BTN_SIZE_SMALL   = 36;
  public static final int BTN_SIZE_NORMAL  = 42;
  public static final int BTN_SIZE_MEDIUM  = 48;
  public static final int BTN_SIZE_LARGE   = 64;
  public static final int BTN_SIZE_HUGE    = 84;
  public static int mSizeBtns     = 0;      // action bar buttons scale (either 1 or 2)
  public static int mSizeButtons  = 42;     // default 42 minimum MIN_SIZE_BUTTONS
  public static int mTextSize     = 16;     // list text size 
  public static boolean mKeyboard = true;
  public static boolean mNoCursor = false;
  public static boolean mLocalManPages = true;
  public static boolean mPacketLog     = false;

  public static int mOrientation = 0; // 0 unspecified, 1 portrait, 2 landscape

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  // IMPORT EXPORT
  public static boolean mLRExtend           = true;   // whether to extend LR or not (Compass/VisualTopo input)
  public static float   mLRUDvertical       = 45;     // vertical splay for UD 
  public static float   mLRUDhorizontal     = 45;     // horizontal splay for LR 

  public static String mSurvexEol           = "\n";
  public static boolean mSurvexSplay        = false;
  public static boolean mSurvexLRUD         = false;
  public static boolean mSwapLR             = false; // swap LR in Compass export
  public static boolean mOrthogonalLRUD     = false; // whether angle > 0 
  public static float mOrthogonalLRUDAngle  = 0;     // angle
  public static float mOrthogonalLRUDCosine = 1;     // cosine of the angle

  public static boolean mExportStationsPrefix = false;  // whether to prepend cave name to station in cSurvey/compass export
  public static boolean mZipWithSymbols       = false;  // whether to add/load symbols to/from archive

  // public static boolean mXTherionAreas = false;
  public static boolean mAutoStations  = true;  // whether to add stations automatically to scrap therion files
  public static boolean mTherionSplays = false; // whether to add splay segments to auto stations
  public static boolean mTherionXvi    = false; // whether to add xvi image to th2
  public static boolean mCompassSplays = true;  // whether to add splays to Compass export
  public static boolean mVTopoSplays = true;    // whether to add splays to VisualTopo export
  public static boolean mVTopoLrudAtFrom = false; 
  public static final float THERION_SCALE = 196.8503937f; // 200 * 39.3700787402 / 40;
  public static float   mToTherion = THERION_SCALE / 100;

  public static float mBitmapScale = 1.5f;
  public static float mBezierStep  = 0.2f;
  public static float getBezierStep() { return ( mBezierStep < 0.1f )? 0.05f : (mBezierStep/2); }
 
  // public static float mDxfScale    = 1.0f;
  public static int mBitmapBgcolor = 0x000000;

  public static int mAcadVersion = 9;      // AutoCAD version 9, or 13, or 16

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // LOCATION
  public static String mCRS = "Long-Lat";    // default coord ref systen 
  // public static final  String UNIT_LOCATION  = "ddmmss";
  public static int mUnitLocation = 0; // 0 dec-degree, 1 ddmmss

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // CALIBRATION

  // public static final String GROUP_DISTANCE = "40";
  public static float mGroupDistance = 40;

  public static final float DISTOX_MAX_EPS  = 0.01f; // hard limit
  // public static final String CALIB_EPS      = "0.000001";
  public static float mCalibEps = 0.000001f; // calibartion epsilon

  public static int   mCalibMaxIt = 200;     // calibration max nr of iterations
  public static boolean mCalibShotDownload = true;

  // calibration data grouping policies
  public static final int GROUP_BY_DISTANCE = 0; // DEPRECATED
  public static final int GROUP_BY_FOUR     = 1;
  public static final int GROUP_BY_ONLY_16  = 2;
  // public static final String GROUP_BY  = TDString.ONE;     // GROUP_BY_FOUR
  public static int mGroupBy = GROUP_BY_FOUR;  // how to group calib data

  // public static boolean mRawData = false;   // whether to display calibration raw data as well
  public static int   mRawCData  = 0;
  public static int   mCalibAlgo = 0;   // calibration algorithm: 0 auto, 1 linear, 2 non-linear

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DEVICE
  public final static int CONN_MODE_BATCH      = 0;      // DistoX connection mode
  public final static int CONN_MODE_CONTINUOUS = 1;
  public final static int CONN_MODE_MULTI      = 2;
  // public final static int CONN_MODE_DOUBLE     = 3;
  public static int mConnectionMode    = CONN_MODE_BATCH; 

  public static boolean isConnectionModeBatch() { return mConnectionMode != CONN_MODE_CONTINUOUS; }
  public static boolean isConnectionModeContinuous() { return mConnectionMode == CONN_MODE_CONTINUOUS; }
  // public static boolean isConnectionModeDouble() { return mConnectionMode == CONN_MODE_DOUBLE; }
  public static boolean isConnectionModeMulti()  { return mConnectionMode == CONN_MODE_MULTI; }

  public static boolean mZ6Workaround  = true;

  public static boolean mAutoReconnect = false;
  public static boolean mSecondDistoX = false;
  public static boolean mHeadTail      = false; // whether to use readA3HeadTail to download the data (A3 only)
  public static boolean mAutoPair      = true;
  public static int mConnectSocketDelay = 0; // wait time if not paired [0.1 sec]

  // public static final boolean CHECK_BT = true;
  public static int mCheckBT = 1;        // BT: 0 disabled, 1 check on start, 2 enabled

  public static final int TD_SOCK_DEFAULT      = 0;    // BT socket type
  public static final int TD_SOCK_INSEC        = 1;
  public static final int TD_SOCK_PORT         = 2;
  public static final int TD_SOCK_INSEC_PORT   = 3;
  // public static final int TD_SOCK_INSEC_INVOKE = 4;
  // public static int mDefaultSockType = (android.os.Build.MANUFACTURER.equals("samsung") ) ? TD_SOCK_INSEC : TD_SOCK_DEFAULT;
  public static String mDefaultSockStrType = (android.os.Build.MANUFACTURER.equals("samsung") ) ? TDString.ONE : TDString.ZERO;
  public static int mSockType = TD_SOCK_DEFAULT;

  public static int mCommRetry = 1; 
  public static int mCommType  = 0; // 0: on-demand, 1: continuous, 2: multi

  public static int mWaitLaser = 1000;
  public static int mWaitShot  = 4000;
  public static int mWaitData  =  100;  // delay between data
  public static int mWaitConn  =  500;  // delay waiting a connection
  public static int mWaitCommand = 100;
  public static int mConnectFeedback  = FEEDBACK_NONE;

  public static boolean mCheckAttached = false;    // whether to check is there are shots non-attached
  public static boolean mCheckExtend   = true;
  public static boolean mPrevNext = true;    // whether to display prev-next buttons in shot dialog

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SHOTS
  public static float mVThreshold = 80f;   // verticality threshold (LRUD)
  public static float mHThreshold;         // horizontal plot threshold
  public static boolean mDataBackup = false; // whether to export data when shotwindow is closed
  public static boolean mDistoXBackshot = false;
  // public static int mTitleColor = TDColor.TITLE_NORMAL;
  
  public static final char CSV_COMMA = ',';
  public static final char CSV_PIPE  = '|';
  public static final char CSV_TAB   = '\t';
  public static final char[] CSV_SEPARATOR = { CSV_COMMA, CSV_PIPE, CSV_TAB };

  public static int mImportDatamode    = 0;  // SurveyInfo.DATAMODE_NORMAL
  // public static boolean mExportTcsx    = true;
  public static int mExportShotsFormat = -1; // DISTOX_EXPORT_NONE
  public static int mExportPlotFormat  = -1; // DISTOX_EXPORT_NONE
  public static boolean mTherionMaps   = false;
  public static boolean mSvgRoundTrip  = false;
  public static boolean mSvgGrid       = false;
  // public static boolean mSvgInHtml     = false;
  public static boolean mSvgLineDirection = false;
  public static boolean mSvgSplays        = true;
  public static boolean mKmlStations   = true;
  public static boolean mKmlSplays     = false;
  public static boolean mCsvRaw        = false;
  public static char mCsvSeparator     = CSV_COMMA;
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
  public static boolean mBacksightInput = false;   // whether to add backsight fields in shot anual-input dialog
  public static float   mSplayVertThrs = 80;
  public static boolean mAzimuthManual = false;    // whether to manually set extend / or use reference azimuth
  public static int     mDashSplay     = DASHING_NONE; // whether dash-splay are plan-type (1), profile-type (2), or independent (0)
  public static float   mVertSplay     = 50;
  public static float   mHorizSplay    = 60;
  public static float   mCosHorizSplay = TDMath.cosd( mHorizSplay );
  public static float   mSectionSplay  = 60;
  public static int     mStationNames  = 0;        // type of station names (0: alpha, 1: number)
  public static int     mSplayAlpha    = 80;       // splay alpha [default 80 out of 100]

  public static final int LOOP_NONE      = 0;
  public static final int LOOP_CYCLES    = 1;
  public static final int LOOP_TRIANGLES = 3;
  public static int mLoopClosure = LOOP_NONE;      // loop closure: 0 none, 1 normal, 3 triangles
  
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
  public static float mExtendThr = 10;             // extend vertically splays in [90-30, 90+30] of the leg

  public static int mThumbSize = 200;               // thumbnail size
  public static boolean mWithSensors = false;       // whether sensors are enabled
  // public static boolean mWithTdManager  = false;       // whether TdManager is enabled
  // public static boolean mSplayActive = false;       // whether splays are attached to active station (if defined)
  // public static boolean mWithRename  = false;       // whether survey rename is enabled

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // SKETCH DRAWING

  public static float mReduceAngle  = 45;    // minimal angle between segments of "straightened" lines
  public static float mReduceCosine = 0.7f;  // cosine of mReduceAngle

  // public static boolean mZoomControls = false;
  public static int mZoomCtrl = 1;
  public static boolean mSideDrag = false;
  public static boolean mTripleToolbar = false;

  public static float mUnitIcons = 1.4f; // drawing unit icons
  public static float mUnitLines = 1.4f; // drawing unit lines

  // selection_radius = cutoff + closeness / zoom
  public static final float mCloseCutoff = 0.01f; // minimum selection radius

  public static float mSelectness = 24f;            // selection radius
  public static float mEraseness = 36f;             // eraser radius
  public static int mMinShift = 60;                 // shift sensitivity
  public static int mPointingRadius = 24;

  // public static final String LINE_SHIFT = "20.0";
  public static float mUnitGrid    = 1;         // 1: meter, 0.9... yard
  public static float mUnitMeasure = -1;        // -1: grid-cell

  // public static final int PICKER_RECENT = 0; // Drawing-tools picker type
  public static final int PICKER_LIST   = 1; 
  public static final int PICKER_GRID   = 2;
  public static final int PICKER_GRID_3 = 3;
  public static int mPickerType = PICKER_LIST;
  // public static int mRecentNr     = 4;        // nr. most recent symbols
  public static boolean mPalettes = false;   // extra tools palettes

  public static final int LINE_STYLE_BEZIER = 0;  // drawing line styles
  private static final int LINE_STYLE_ONE    = 1;
  private static final int LINE_STYLE_TWO    = 2;
  private static final int LINE_STYLE_THREE  = 3;
  private static final int LINE_STYLE_SIMPLIFIED = 4;
  private static final String LINE_STYLE     = TDString.TWO;     // LINE_STYLE_TWO NORMAL
  public static int   mLineStyle = LINE_STYLE_BEZIER;    
  public static int   mLineType;        // line type:  1       1     2    3
  public static int   mLineSegment   = 10;
  public static int   mLineSegment2  = 100;   // square of mLineSegment
  public static float mLineAccuracy  = 1f;
  public static float mLineCorner    = 20;    // corner threshold
  public static int   mContinueLine  = DrawingWindow.CONT_NONE; // 0
  public static boolean mCompositeActions = false;

  public static float mWeedDistance  = 0.5f;  // max weeding distance
  public static float mWeedLength    = 2.0f;  // max weeding length
  public static float mWeedBuffer    = 10;    // weed segment buffer

  // public static boolean mWithLayers  = true; // false;
  public static int mWithLevels = 0;  // 0: no, 1: by class, 2: by item

  public static float mStationSize    = 20;   // size of station names [pt]
  public static float mLabelSize      = 24;   // size of labels [pt]
  public static float mFixedThickness = 1;    // width of fixed lines
  public static float mLineThickness  = 1;    // witdh of drawing lines
  public static boolean mAutoSectionPt = false;
  public static int   mBackupNumber   = 5;
  public static int   mBackupInterval = 60;
  public static boolean mBackupsClear = false;
  public static boolean mFixedOrigin     = false; 
  public static boolean mSharedXSections = false; // default value
  public static boolean mAutoXSections   = true;  // auto save/export xsections with section points
  public static boolean mSavedStations   = false;
  // public static boolean mPlotCache       = true;  // default value
  public static float mDotRadius      = 5;
  public static float mArrowLength    = 8;

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
  public static boolean mSplayColor     = false;
  public static boolean mDivingMode     = false;

  public static boolean mPlotSplit      = false;
  public static boolean mPlotShift      = false;

  public static float mSvgPointStroke   = 0.1f;
  public static float mSvgLabelStroke   = 0.3f;
  public static float mSvgLineStroke    = 0.5f;
  public static float mSvgLineDirStroke = 2f;
  public static float mSvgGridStroke    = 0.5f;
  public static float mSvgShotStroke    = 0.5f;
  public static int   mSvgStationSize   = 20; 

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  /* FIXME_SKETCH_3D *
  public static int   mSketchModelType = 1;
  public static float mSketchSideSize;
  public static float mDeltaExtrude;
  // public static boolean mSketchUsesSplays; // whether 3D models surfaces use splays
  // public static float mSketchBorderStep;
  // public static float mSketchSectionStep;
   * END_SKETCH_3D */

  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // DATA ACCURACY
  public static float mAccelerationThr = 1; // acceleration threshold (shot quality)
  public static float mMagneticThr     = 1; // magnetic threshold
  public static float mDipThr          = 2; // dip threshold
  public static float mMaxShotLength   = 50; // max length of a shot (if larger it is overshoot)
  public static float mMinLegLength    = 0; // min length of a leg (if shorter it is undershoot)
  // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
  // WALLS

  public static final int WALLS_NONE    = 0;
  public static final int WALLS_CONVEX  = 1;
  public static final int WALLS_DLN     = 2;
  public static final int WALLS_LAST    = 2; // placeholder
  public static int   mWallsType        = WALLS_NONE;
  public static float mWallsPlanThr     = 70;
  public static float mWallsExtendedThr = 45;
  public static float mWallsXClose      = 0.1f;
  public static float mWallsXStep       = 1.0f;
  public static float mWallsConcave     = 0.1f;

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
      if ( mZoomCtrl == 0 && ! is_multitouch ) mZoomCtrl = 1;
    } catch ( NumberFormatException e ) { }
  }

  // backgroind color RGB_565
  private static String setBitmapBgcolor( SharedPreferences prefs, String key, String color, String def_value )
  {
    int r=0, g=0, b=0;
    String[] vals = color.split("\\s+"); 
    color = def_value;
    if ( vals.length == 3 ) {
      try { 
        r = Integer.parseInt( vals[0] );
        g = Integer.parseInt( vals[1] );
        b = Integer.parseInt( vals[2] );
        if ( r > 255 ) r = 255; if ( r < 0 ) r = 0;
        if ( g > 255 ) g = 255; if ( g < 0 ) g = 0;
        if ( b > 255 ) b = 255; if ( b < 0 ) b = 0;
        color = Integer.toString(r) + " " + Integer.toString(g) + " " + Integer.toString(b);
      } catch ( NumberFormatException e ) {
	r = g = b = 0;
      }
      // Log.v("DistoX", "bitmap bg color <" + r + " " + g + " " + b +">" );
    }
    setPreference( prefs, key, color );
    mBitmapBgcolor = 0xff000000 | ( r << 16 ) | ( g << 8 ) | b;
    return color;
  }

  // ------------------------------------------------------------------
  private static float tryFloat( SharedPreferences prefs, String key, String def_value )
  {
    float f = 0;
    try { f = Float.parseFloat( prefs.getString( key, def_value ) ); } 
    catch ( NumberFormatException e ) {
      TDLog.Error("Float Format Error. Key " + key + " " + e.getMessage() );
      f = Float.parseFloat(def_value);
      setPreference( prefs, key, def_value );
    }
    return f;
  }

  private static int tryInt( SharedPreferences prefs, String key, String def_value )
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

  private static float tryFloatValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    float f = 0;
    if ( val == null ) { 
      f = Float.parseFloat(def_value);
      hlp.update( key, def_value );
    } else {
      try {
        f = Float.parseFloat( val );
        hlp.update( key, val );
      } catch ( NumberFormatException e ) {
        TDLog.Error("Float Format Error. Key " + key + " " + e.getMessage() );
        f = Float.parseFloat(def_value);
        hlp.update( key, def_value );
      }
    }
    return f;
  }

  private static int tryIntValue( TDPrefHelper hlp, String key, String val, String def_value )
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

  private static String tryStringValue( TDPrefHelper hlp, String key, String val, String def_value )
  {
    if ( val == null ) val = def_value;
    hlp.update( key, val );
    return val;
  }

  private static boolean tryBooleanValue( TDPrefHelper hlp, String key, String val, boolean def_value )
  {
    boolean i = def_value;
    if ( val != null ) {
      try {
        i = Boolean.parseBoolean( val );
      } catch( NumberFormatException e ) { 
        TDLog.Error("Boolean Format Error. Key " + key + " " + e.getMessage() );
      }
    }
    hlp.update( key, i );
    return i;
  }

  private static void setLoopClosure( int loop_closure )
  {
    mLoopClosure = loop_closure;
    if ( mLoopClosure == LOOP_CYCLES ) {
      if ( ! TDLevel.overAdvanced ) mLoopClosure = LOOP_NONE;
    } else if ( mLoopClosure == LOOP_TRIANGLES ) {
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

  public static boolean setSizeButtons( int size )
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
  public static int getSizeButtons( int size )
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
  public static void loadPrimaryPreferences( /* TopoDroidApp my_app, */ Resources res, TDPrefHelper pref_hlp )
  {
    SharedPreferences prefs = pref_hlp.getSharedPrefs();
  
    // defaultTextSize   = my_app.getResources().getString( R.string.default_textsize );
    // defaultButtonSize = my_app.getResources().getString( R.string.default_buttonsize );
    defaultTextSize   = res.getString( R.string.default_textsize );
    defaultButtonSize = res.getString( R.string.default_buttonsize );

    // ------------------- GENERAL PREFERENCES
    String[] keyMain = TDPrefKey.MAIN;
    String[] defMain = TDPrefKey.MAINdef;
    int level = Integer.parseInt( prefs.getString( keyMain[3], defMain[3] ) ); // DISTOX_EXTRA_BUTTONS choice: 0, 1, 2, 3
    setActivityBooleans( prefs, level );

    String[] keyGeek = TDPrefKey.GEEK;
    String[] defGeek = TDPrefKey.GEEKdef;
    setPalettes(  prefs.getBoolean( keyGeek[0], bool(defGeek[0]) ) ); // DISTOX_PALETTES
    setBackupsClear( prefs.getBoolean( keyGeek[1], bool(defGeek[1]) ) ); // DISTOX_BACKUPS_CLEAR
    mPacketLog = prefs.getBoolean( keyGeek[2], bool(defGeek[2]) ); // DISTOX_PACKET_LOGGER

    // String[] keyGPlot = TDPrefKey.GEEKPLOT;
    // String[] defGPlot = TDPrefKey.GEEKPLOTdef;
    // setBackupsClear( prefs.getBoolean( keyGPlot[ 9], bool(defGPlot[ 9]) ) ); // DISTOX_BACKUPS_CLEAR

    setTextSize( tryInt(    prefs,     keyMain[1], defMain[1] ) );      // DISTOX_TEXT_SIZE
    setSizeButtons( tryInt( prefs,     keyMain[2], defMain[2] ) );      // DISTOX_SIZE_BUTTONS
    mKeyboard      = prefs.getBoolean( keyMain[4], bool(defMain[4]) );  // DISTOX_MKEYBOARD
    mNoCursor      = prefs.getBoolean( keyMain[5], bool(defMain[5]) );  // DISTOX_NO_CURSOR
    mLocalManPages = handleLocalUserMan( /* my_app, */ prefs.getString( keyMain[6], defMain[6] ), false ); // DISTOX_LOCAL_MAN
    TopoDroidApp.setLocale( prefs.getString( keyMain[7], TDString.EMPTY ), false ); // DISTOX_LOCALE
    mOrientation = Integer.parseInt( prefs.getString( keyMain[8], defMain[8] ) ); // DISTOX_ORIENTATION choice: 0, 1, 2
    // TopoDroidApp.setLocale( prefs.getString( keyMain[7], defMain[7] ), false ); // DISTOX_LOCALE
    // TDLog.Profile("locale");
    // boolean co_survey = prefs.getBoolean( keyMain[8], bool(defMain[8]) );        // DISTOX_COSURVEY 

    String[] keySurvey = TDPrefKey.SURVEY;
    String[] defSurvey = TDPrefKey.SURVEYdef;
    mDefaultTeam = prefs.getString( keySurvey[0], defSurvey[0] );               // DISTOX_TEAM
    mInitStation = prefs.getString( keySurvey[3], defSurvey[3] ).replaceAll("\\s+", "");  // DISTOX_INIT_STATION 
    if ( mInitStation.length() == 0 ) mInitStation = defSurvey[3];
    DistoXStationName.setInitialStation( mInitStation );

    String[] keyData = TDPrefKey.DATA;
    String[] defData = TDPrefKey.DATAdef;
    mAzimuthManual = prefs.getBoolean( keyData[6], bool(defData[6]) );   // DISTOX_AZIMUTH_MANUAL 
    TDAzimuth.resetRefAzimuth( null, SurveyInfo.SURVEY_EXTEND_NORMAL ); // BUG ?? may call setRefAzimuthButton on non-UI thread
    
    // ------------------- DEVICE PREFERENCES -def--fallback--min-max
    String[] keyDevice = TDPrefKey.DEVICE;
    String[] defDevice = TDPrefKey.DEVICEdef;
    // DISTOX_DEVICE - UNUSED HERE
    mCheckBT        = tryInt( prefs, keyDevice[0], defDevice[0] );        // DISTOX_BLUETOOTH choice: 0, 1, 2
  }

  public static void loadSecondaryPreferences( /* TopoDroidApp my_app, */ TDPrefHelper pref_hlp )
  {
    SharedPreferences prefs = pref_hlp.getSharedPrefs();

    String[] keySurvey = TDPrefKey.SURVEY;
    String[] defSurvey = TDPrefKey.SURVEYdef;
    parseStationPolicy( pref_hlp, prefs.getString( keySurvey[1], defSurvey[1] ) ); // DISTOX_SURVEY_STATION
    mStationNames = (prefs.getString(    keySurvey[2],      defSurvey[2] ).equals("number"))? 1 : 0; // DISTOX_STATION_NAMES
    mThumbSize    = tryInt(   prefs,     keySurvey[4],      defSurvey[4] );       // DISTOX_THUMBNAIL
    mDataBackup   = prefs.getBoolean(    keySurvey[5], bool(defSurvey[5]) ); // DISTOX_DATA_BACKUP
    mFixedOrigin  = prefs.getBoolean(    keySurvey[6], bool(defSurvey[6]) ); // DISTOX_FIXED_ORIGIN
    mSharedXSections = prefs.getBoolean( keySurvey[7], bool(defSurvey[7]) ); // DISTOX_SHARED_XSECTIONS

    String[] keyPlot = TDPrefKey.PLOT;
    String[] defPlot = TDPrefKey.PLOTdef;
    mPickerType = tryInt( prefs,       keyPlot[0],      defPlot[0] );  // DISTOX_PICKER_TYPE choice: 0, 1, 2
    if ( mPickerType < PICKER_LIST ) mPickerType = PICKER_LIST;
    mTripleToolbar   = prefs.getBoolean(    keyPlot[1], bool(defPlot[1]) ); // DISTOX_TRIPLE_TOOLBAR
    // mRecentNr   = tryInt( prefs,       keyPlot[ ],      defPlot[ ] );  // DISTOX_RECENT_NR choice: 3, 4, 5, 6
    mSideDrag   = prefs.getBoolean(    keyPlot[2], bool(defPlot[2]) ); // DISTOX_SIDE_DRAG
    // setZoomControls( prefs.getBoolean( keyPlot[ ], bool(defPlot[ ]) ) ); // DISTOX_ZOOM_CONTROLS
    setZoomControls( prefs.getString(  keyPlot[3],      defPlot[3] ), TDandroid.checkMultitouch( TDInstance.context ) ); // DISTOX_ZOOM_CTRL
    // mSectionStations  = tryInt( prefs, keyPlot[ ], "3");      // DISTOX_SECTION_STATIONS
    mHThreshold    = tryFloat( prefs,  keyPlot[4],      defPlot[4] );  // DISTOX_HTHRESHOLD
    mCheckAttached = prefs.getBoolean( keyPlot[5], bool(defPlot[5]) ); // DISTOX_CHECK_ATTACHED
    mCheckExtend   = prefs.getBoolean( keyPlot[6], bool(defPlot[6]) ); // DISTOX_CHECK_EXTEND

    String[] keyCalib = TDPrefKey.CALIB;
    String[] defCalib = TDPrefKey.CALIBdef;
    mGroupBy       = tryInt(   prefs,      keyCalib[0],      defCalib[0] );  // DISTOX_GROUP_BY choice: 0, 1, 2
    mGroupDistance = tryFloat( prefs,      keyCalib[1],      defCalib[1] );  // DISTOX_GROUP_DISTANCE
    mCalibEps      = tryFloat( prefs,      keyCalib[2],      defCalib[2] );  // DISTOX_CAB_EPS
    mCalibMaxIt    = tryInt(   prefs,      keyCalib[3],      defCalib[3] );  // DISTOX_CALIB_MAX_IT
    mCalibShotDownload = prefs.getBoolean( keyCalib[4], bool(defCalib[4]) ); // DISTOX_CALIB_SHOT_DOWNLOAD
    // mRawData       = prefs.getBoolean( keyCalib[], bool(defCalib[]) );    // DISTOX_RAW_DATA 20
    mRawCData      = tryInt( prefs,        keyCalib[5],      defCalib[5] );  // DISTOX_RAW_CDATA 20
    mCalibAlgo     = tryInt( prefs,        keyCalib[6],      defCalib[6] );  // DISTOX_CALIB_ALGO choice: 0, 1, 2
    mAlgoMinAlpha  = tryFloat( prefs,      keyCalib[7],      defCalib[7] );  // DISTOX_ALGO_MIN_ALPHA
    mAlgoMinBeta   = tryFloat( prefs,      keyCalib[8],      defCalib[8] );  // DISTOX_ALGO_MIN_BETA
    mAlgoMinGamma  = tryFloat( prefs,      keyCalib[9],      defCalib[9] );  // DISTOX_ALGO_MIN_GAMMA
    mAlgoMinDelta  = tryFloat( prefs,      keyCalib[10],     defCalib[10] ); // DISTOX_ALGO_MIN_DELTA

    String[] keyDevice = TDPrefKey.DEVICE;
    String[] defDevice = TDPrefKey.DEVICEdef;
    mConnectionMode = tryInt( prefs,    keyDevice[ 1],      defDevice[ 1] );   // DISTOX_CONN_MODE choice: 0, 1, 2
    mAutoReconnect  = prefs.getBoolean( keyDevice[ 2], bool(defDevice[ 2]) );  // DISTOX_AUTO_RECONNECT
    mHeadTail       = prefs.getBoolean( keyDevice[ 3], bool(defDevice[ 3]) );  // DISTOX_HEAD_TAIL
    mSockType       = tryInt( prefs,    keyDevice[ 4], mDefaultSockStrType );  // DISTOX_SOCK_TYPE choice: 0, 1, (2, 3)
    // mCommRetry      = tryInt( prefs, keyDevice[  ], bool(defDevice[  ]) );  // DISTOX_COMM_RETRY
    mZ6Workaround   = prefs.getBoolean( keyDevice[ 5], bool(defDevice[ 5])  ); // DISTOX_Z6_WORKAROUND
    mAutoPair       = prefs.getBoolean( keyDevice[ 6], bool(defDevice[ 6]) );  // DISTOX_AUTO_PAIR
    mConnectFeedback = tryInt( prefs,   keyDevice[ 7],      defDevice[ 7] );   // DISTOX_CONNECT_FEEDBACK

    String[] keyGDev = TDPrefKey.GEEKDEVICE;
    String[] defGDev = TDPrefKey.GEEKDEVICEdef;
    mConnectSocketDelay = tryInt(prefs, keyGDev[ 0],      defGDev[ 0] );   // DISTOX_SOCKET_DELAY
    mSecondDistoX   = prefs.getBoolean( keyGDev[ 1], bool(defGDev[ 1]) );  // DISTOX_SECOND_DISTOX
    mWaitData       = tryInt( prefs,    keyGDev[ 2],      defGDev[ 2] );   // DISTOX_WAIT_DATA
    mWaitConn       = tryInt( prefs,    keyGDev[ 3],      defGDev[ 3] );   // DISTOX_WAIT_CONN
    mWaitLaser      = tryInt( prefs,    keyGDev[ 4],      defGDev[ 4] );   // DISTOX_WAIT_LASER
    mWaitShot       = tryInt( prefs,    keyGDev[ 5],      defGDev[ 5] );   // DISTOX_WAIT_SHOT

    String[] keyImport = TDPrefKey.EXPORT_import;
    String[] defImport = TDPrefKey.EXPORT_importdef;
    // keyImport[ 0 ] // DISTOX_PT_CMAP
    mLRExtend          = prefs.getBoolean(     keyImport[ 1], bool(defImport[ 1]) ); // DISTOX_SPLAY_EXTEND


    String[] keyGeekImport = TDPrefKey.GEEKIMPORT;
    String[] defGeekImport = TDPrefKey.GEEKIMPORTdef;
    mZipWithSymbols = prefs.getBoolean(     keyGeekImport[ 0], bool(defGeekImport[ 0]) ); // DISTOX_ZIP_WITH_SYMBOLS
    mImportDatamode = tryInt(   prefs,      keyGeekImport[ 1],      defGeekImport[ 1] );  // DISTOX_IMPORT_DATAMODE
    // mExportTcsx     = prefs.getBoolean(     keyGeekImport[ 2], bool(defGeekImport[ 2]) ); // DISTOX_TRANSFER_CSURVEY

    String[] keyExport = TDPrefKey.EXPORT;
    String[] defExport = TDPrefKey.EXPORTdef;
    mExportShotsFormat = tryInt(   prefs,      keyExport[ 0],      defExport[ 0] );  // DISTOX_EXPORT_SHOTS choice: 
    mExportPlotFormat  = tryInt(   prefs,      keyExport[ 1],      defExport[ 1] );  // DISTOX_EXPORT_PLOT choice: 14, 2, 11, 12, 13

    // mTherionMaps       = prefs.getBoolean(     keyExport[ 5], bool(defExport[ 5]) ); // DISTOX_THERION_MAPS
    // mAutoStations      = prefs.getBoolean(     keyExport[ 6], bool(defExport[ 6]) ); // DISTOX_AUTO_STATIONS 
    // // mXTherionAreas  = prefs.getBoolean(     keyExport[  ], bool(defExport[  ]) ); // DISTOX_XTHERION_AREAS
    // mTherionSplays     = prefs.getBoolean(     keyExport[ 7], bool(defExport[ 7]) ); // DISTOX_THERION_SPLAYS

    // mExportStationsPrefix =  prefs.getBoolean( keyExport[ 8], bool(defExport[ 8]) ); // DISTOX_STATION_PREFIX
    // mCompassSplays     = prefs.getBoolean(     keyExport[ 9], bool(defExport[ 9]) ); // DISTOX_COMPASS_SPLAYS
    // mSwapLR            = prefs.getBoolean(     keyExport[10], bool(defExport[10]) ); // DISTOX_SWAP_LR

    mOrthogonalLRUDAngle = tryFloat( prefs,    keyExport[ 2],      defExport[ 2] );  // DISTOX_ORTHO_LRUD
    mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
    mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    mLRUDvertical      = tryFloat( prefs,      keyExport[ 3],      defExport[ 3] );  // DISTOX_LRUD_VERTICAL
    mLRUDhorizontal    = tryFloat( prefs,      keyExport[ 4],      defExport[ 4] );  // DISTOX_LRUD_HORIZONTAL
    mBezierStep        = tryFloat( prefs,      keyExport[ 5],      defExport[ 5] );  // DISTOX_BEZIER_STEP

    String[] keyExpSvx = TDPrefKey.EXPORT_SVX;
    String[] defExpSvx = TDPrefKey.EXPORT_SVXdef;
    mSurvexEol         = ( prefs.getString(    keyExpSvx[0],      defExpSvx[0] ).equals("LF") )? "\n" : "\r\n";  // DISTOX_SURVEX_EOL
    mSurvexSplay       =   prefs.getBoolean(   keyExpSvx[1], bool(defExpSvx[1]) ); // DISTOX_SURVEX_SPLAY
    mSurvexLRUD        =   prefs.getBoolean(   keyExpSvx[2], bool(defExpSvx[2]) ); // DISTOX_SURVEX_LRUD

    String[] keyExpTh  = TDPrefKey.EXPORT_TH;
    String[] defExpTh  = TDPrefKey.EXPORT_THdef;
    mTherionMaps       = prefs.getBoolean( keyExpTh[0], bool(defExpTh[0]) ); // DISTOX_THERION_MAPS
    mAutoStations      = prefs.getBoolean( keyExpTh[1], bool(defExpTh[1]) ); // DISTOX_AUTO_STATIONS 
    // mXTherionAreas  = prefs.getBoolean( keyExpTh[ ], bool(defExpTh[ ]) ); // DISTOX_XTHERION_AREAS
    mTherionSplays     = prefs.getBoolean( keyExpTh[2], bool(defExpTh[2]) ); // DISTOX_THERION_SPLAYS
    // SVG_GRID not good
    int scale = tryInt( prefs, keyExpTh[4], defExpTh[4] );  // DISTOX_TH2_SCALE
    mToTherion = THERION_SCALE / scale;
    mTherionXvi        = prefs.getBoolean( keyExpTh[5], bool(defExpTh[5]) ); // DISTOX_TH2_XVI
    // mSurvexLRUD        =   prefs.getBoolean(   keyExpTh[3], bool(defExpTh[3]) ); // DISTOX_SURVEX_LRUD

    String[] keyExpDat = TDPrefKey.EXPORT_DAT;
    String[] defExpDat = TDPrefKey.EXPORT_DATdef;
    mExportStationsPrefix =  prefs.getBoolean( keyExpDat[0], bool(defExpDat[0]) ); // DISTOX_STATION_PREFIX
    mCompassSplays     = prefs.getBoolean(     keyExpDat[1], bool(defExpDat[1]) ); // DISTOX_COMPASS_SPLAYS
    mSwapLR            = prefs.getBoolean(     keyExpDat[2], bool(defExpDat[2]) ); // DISTOX_SWAP_LR

    String[] keyExpTro = TDPrefKey.EXPORT_TRO;
    String[] defExpTro = TDPrefKey.EXPORT_TROdef;
    mVTopoSplays       = prefs.getBoolean(     keyExpTro[0], bool(defExpTro[0]) ); // DISTOX_VTOPO_SPLAYS
    mVTopoLrudAtFrom   = prefs.getBoolean(     keyExpTro[1], bool(defExpTro[1]) ); // DISTOX_VTOPO_LRUD

    String[] keyExpSvg = TDPrefKey.EXPORT_SVG;
    String[] defExpSvg = TDPrefKey.EXPORT_SVGdef;
    mSvgRoundTrip      = prefs.getBoolean(     keyExpSvg[0], bool(defExpSvg[0]) ); // DISTOX_SVG_ROUNDTRIP
    mSvgGrid           = prefs.getBoolean(     keyExpSvg[1], bool(defExpSvg[1]) ); // DISTOX_SVG_GRID
    mSvgLineDirection  = prefs.getBoolean(     keyExpSvg[2], bool(defExpSvg[2]) ); // DISTOX_SVG_LINE_DIR
    mSvgSplays         = prefs.getBoolean(     keyExpSvg[3], bool(defExpSvg[3]) ); // DISTOX_SVG_SPLAYS
    // mSvgInHtml      = prefs.getBoolean(     keyExpSvg[ ], bool(defExpSvg[ ]) ); // DISTOX_SVG_IN_HTML
    mSvgPointStroke    = tryFloat( prefs,      keyExpSvg[4],      defExpSvg[4] );  // DISTOX_SVG_POINT_STROKE
    mSvgLabelStroke    = tryFloat( prefs,      keyExpSvg[4],      defExpSvg[5] );  // DISTOX_SVG_LABEL_STROKE
    mSvgLineStroke     = tryFloat( prefs,      keyExpSvg[5],      defExpSvg[6] );  // DISTOX_SVG_LINE_STROKE
    mSvgGridStroke     = tryFloat( prefs,      keyExpSvg[6],      defExpSvg[7] );  // DISTOX_SVG_GRID_STROKE
    mSvgShotStroke     = tryFloat( prefs,      keyExpSvg[8],      defExpSvg[8] );  // DISTOX_SVG_SHOT_STROKE
    mSvgLineDirStroke  = tryFloat( prefs,      keyExpSvg[9],      defExpSvg[9] );  // DISTOX_SVG_LINEDIR_STROKE
    mSvgStationSize    = tryInt(   prefs,      keyExpSvg[10],     defExpSvg[10]);  // DISTOX_SVG_STATION_SIZE


    String[] keyExpKml = TDPrefKey.EXPORT_KML;
    String[] defExpKml = TDPrefKey.EXPORT_KMLdef;
    mKmlStations       = prefs.getBoolean(     keyExpKml[0], bool(defExpKml[0]) ); // DISTOX_KML_STATIONS
    mKmlSplays         = prefs.getBoolean(     keyExpKml[1], bool(defExpKml[1]) ); // DISTOX_KML_SPLAYS

    // String[] keyExpCsx = TDPrefKey.EXPORT_CSX;
    // String[] defExpCsx = TDPrefKey.EXPORT_CSXdef;
    // mExportStationsPrefix = prefs.getBoolean(     keyExpCsx[0], bool(defExpCsx[0]) ); // DISTOX_STATION_PREFIX

    String[] keyExpCsv = TDPrefKey.EXPORT_CSV;
    String[] defExpCsv = TDPrefKey.EXPORT_CSVdef;
    mCsvRaw            = prefs.getBoolean(     keyExpCsv[0], bool(defExpCsv[0]) ); // DISTOX_CSV_RAW
    mCsvSeparator      = CSV_SEPARATOR[ tryInt( prefs, keyExpCsv[1], defExpCsv[1] ) ]; // DISTOX_CSV_SEP

    String[] keyExpPng = TDPrefKey.EXPORT_PNG;
    String[] defExpPng = TDPrefKey.EXPORT_PNGdef;
    mBitmapScale       = tryFloat( prefs,      keyExpPng[0], defExpPng[0] );  // DISTOX_BITMAP_SCALE 
    setBitmapBgcolor( prefs, keyExpPng[1], prefs.getString(keyExpPng[1], defExpPng[1]), defExpPng[1] );  // DISTOX_BITMAP_BGCOLOR

    String[] keyExpDxf = TDPrefKey.EXPORT_DXF;
    String[] defExpDxf = TDPrefKey.EXPORT_DXFdef;
    // mDxfScale    = tryFloat( prefs,    keyExpDxf[ ],      defExpDxf[ ] );  // DISTOX_DXF_SCALE
    mDxfBlocks   =  prefs.getBoolean(     keyExpDxf[0], bool(defExpDxf[0]) ); // DISTOX_DXF_BLOCKS
    mAcadVersion = tryInt(   prefs,       keyExpDxf[1],      defExpDxf[1] );  // DISTOX_ACAD_VERSION choice: 9, 13, 16
  
    String[] keyExpShp = TDPrefKey.EXPORT_SHP;
    String[] defExpShp = TDPrefKey.EXPORT_SHPdef;
    mShpGeoref   =  prefs.getBoolean(     keyExpShp[0], bool(defExpShp[0]) ); // DISTOX_SHP_GEOREF

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

    String[] keyGShot = TDPrefKey.GEEKSHOT;
    String[] defGShot = TDPrefKey.GEEKSHOTdef;
    mDivingMode    = prefs.getBoolean( keyGShot[ 0], bool(defGShot[ 0]) ); // DISTOX_DIVING_MODE
    mShotRecent    = prefs.getBoolean( keyGShot[ 1], bool(defGShot[ 1]) ); // DISTOX_RECENT_SHOT
    mRecentTimeout = tryInt(   prefs,  keyGShot[ 2],      defGShot[ 2] );  // DISTOX_RECENT_TIMEOUT
    mSplayClasses  = prefs.getBoolean( keyGShot[ 3], bool(defGShot[ 3]) ); // DISTOX_SPLAY_CLASSES
    mSplayColor    = prefs.getBoolean( keyGShot[ 4], bool(defGShot[ 4]) ); // DISTOX_SPLAY_COLOR
    mExtendFrac    = prefs.getBoolean( keyGShot[ 5], bool(defGShot[ 5]) ); // DISTOX_EXTEND_FRAC
    mDistoXBackshot= prefs.getBoolean( keyGShot[ 6], bool(defGShot[ 6]) ); // DISTOX_BACKSHOT
    mBedding       = prefs.getBoolean( keyGShot[ 7], bool(defGShot[ 7]) ); // DISTOX_BEDDING
    mWithSensors   = prefs.getBoolean( keyGShot[ 8], bool(defGShot[ 8]) ); // DISTOX_WITH_SENSORS
    setLoopClosure( tryInt(   prefs,   keyGShot[ 9],      defGShot[ 9] ) );// DISTOX_LOOP_CLOSURE_VALUE
    mWithAzimuth   = prefs.getBoolean( keyGShot[10], bool(defGShot[10]) ); // DISTOX_ANDROID_AZIMUTH
    mTimerWait     = tryInt(   prefs,  keyGShot[11],      defGShot[11] );  // DISTOX_SHOT_TIMER
    mBeepVolume    = tryInt(   prefs,  keyGShot[12],      defGShot[12] );  // DISTOX_BEEP_VOLUME
    // mWithTdManager = prefs.getBoolean( keyGShot[13], bool(defGShot[13]) ); // DISTOX_TDMANAGER

    String[] keyGPlot = TDPrefKey.GEEKPLOT;
    String[] defGPlot = TDPrefKey.GEEKPLOTdef;
    mPlotShift     = prefs.getBoolean( keyGPlot[ 0], bool(defGPlot[ 0]) ); // DISTOX_PLOT_SHIFT
    mPlotSplit     = prefs.getBoolean( keyGPlot[ 1], bool(defGPlot[ 1]) ); // DISTOX_PLOT_SPLIT
    mSplayVertThrs  = tryFloat( prefs, keyGPlot[ 2],      defGPlot[ 2]  ); // DISTOX_SPLAY_VERT_THRS
    mDashSplay      = tryInt( prefs,   keyGPlot[ 3],      defGPlot[ 3] );  // DISTOX_SPLAY_DASH
    mVertSplay      = tryFloat( prefs, keyGPlot[ 4],      defGPlot[ 4] );  // DISTOX_VERT_SPLAY
    mHorizSplay     = tryFloat( prefs, keyGPlot[ 5],      defGPlot[ 5] );  // DISTOX_HORIZ_SPLAY
    mCosHorizSplay = TDMath.cosd( mHorizSplay );  
    mSectionSplay   = tryFloat( prefs, keyGPlot[ 6],      defGPlot[ 6] );  // DISTOX_SECTION_SPLAY
    mBackupNumber   = tryInt( prefs,   keyGPlot[ 7],      defGPlot[ 7] );  // DISTOX_BACKUP_NUMBER
    mBackupInterval = tryInt( prefs,   keyGPlot[ 8],      defGPlot[ 8] );  // DISTOX_BACKUP_INTERVAL
    // setBackupsClear( prefs.getBoolean( keyGPlot[ 9], bool(defGPlot[ 9]) ) ); // DISTOX_BACKUPS_CLEAR moved to GEEK
    mAutoXSections  = prefs.getBoolean( keyGPlot[ 9], bool(defGPlot[ 9]) ); // DISTOX_AUTO_XSECTIONS
    mSavedStations  = prefs.getBoolean( keyGPlot[10], bool(defGPlot[10]) ); // DISTOX_SAVED_STATIONS
    mWithLevels     = tryInt( prefs,   keyGPlot[11],      defGPlot[11] );  // DISTOX_WITH_LEVELS

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
    mCompositeActions = prefs.getBoolean( keyGLine[10], bool(defGLine[10]) );  // DISTOX_COMPOSITE_ACTIONS

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
    // Log.v("DistoX", "units grid " + mUnitGrid );
  
    String[] keyAcc = TDPrefKey.ACCURACY;
    String[] defAcc = TDPrefKey.ACCURACYdef;
    mAccelerationThr = tryFloat( prefs, keyAcc[0], defAcc[0] ); // DISTOX_ACCEL_PERCENT
    mMagneticThr     = tryFloat( prefs, keyAcc[1], defAcc[1] ); // DISTOX_MAG_PERCENT
    mDipThr          = tryFloat( prefs, keyAcc[2], defAcc[2] ); // DISTOX_DIP_THR

    String[] keyLoc = TDPrefKey.LOCATION;
    String[] defLoc = TDPrefKey.LOCATIONdef;
    mUnitLocation  = (prefs.getString( keyLoc[0], defLoc[0] ).equals(defLoc[0])) ? TDUtil.DDMMSS  // DISTOX_UNIT_LOCATION
                                                                                 : TDUtil.DEGREE;
    mCRS           = prefs.getString( keyLoc[1], defLoc[1] );                 // DISTOX_CRS

    String[] keyScreen = TDPrefKey.SCREEN;
    String[] defScreen = TDPrefKey.SCREENdef;
    mFixedThickness = tryFloat( prefs, keyScreen[ 0],      defScreen[ 0] );  // DISTOX_FIXED_THICKNESS
    mStationSize    = tryFloat( prefs, keyScreen[ 1],      defScreen[ 1] );  // DISTOX_STATION_SIZE
    mDotRadius      = tryFloat( prefs, keyScreen[ 2],      defScreen[ 2] );  // DISTOX_DOT_RADIUS
    mSelectness     = tryFloat( prefs, keyScreen[ 3],      defScreen[ 3] );  // DISTOX_CLOSENESS
    mEraseness      = tryFloat( prefs, keyScreen[ 4],      defScreen[ 4] );  // DISTOX_ERASENESS
    mMinShift       = tryInt(   prefs, keyScreen[ 5],      defScreen[ 5] );  // DISTOX_MIN_SHIFT
    mPointingRadius = tryInt(   prefs, keyScreen[ 6],      defScreen[ 6] );  // DISTOX_POINTING
    mSplayAlpha     = tryInt(   prefs, keyScreen[ 7],      defScreen[ 7] );  // DISTOX_SPLAY_ALPHA
    BrushManager.setSplayAlpha( mSplayAlpha );

    String[] keyLine = TDPrefKey.LINE;
    String[] defLine = TDPrefKey.LINEdef;
    mLineThickness = tryFloat( prefs,  keyLine[0],      defLine[0] );   // DISTOX_LINE_THICKNESS
    mUnitLines     = tryFloat( prefs,  keyLine[1],      defLine[1] );   // DISTOX_LINE_UNITS
    setLineStyleAndType( prefs.getString( keyLine[2],   defLine[2] ) ); // DISTOX_LINE_STYLE
    setLineSegment( tryInt(    prefs,  keyLine[3],      defLine[3] ) ); // DISTOX_LINE_SEGMENT
    mArrowLength   = tryFloat( prefs,  keyLine[4],      defLine[4] );   // DISTOX_ARROW_LENGTH
    mAutoSectionPt = prefs.getBoolean( keyLine[5], bool(defLine[5]) );  // DISTOX_AUTO_SECTION_PT
    mContinueLine  = tryInt(   prefs,  keyLine[6],      defLine[6] );   // DISTOX_LINE_CONTINUE
    mAreaBorder    = prefs.getBoolean( keyLine[7], bool(defLine[7]) );  // DISTOX_AREA_BORDER

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

    /* FIXME_SKETCH_3D *
    String[] keySketch = TDPrefKey.SKETCH;
    String[] defSketch = TDPrefKey.SKETCHdef;
    // mSketchUsesSplays  = prefs.getBoolean( keySketch[], bool(defSketch[]) );    
    mSketchModelType = tryInt(  prefs, keySketch[0], defSketch[0] );   // DISTOX_SKETCH_MODEL_TYPE
    mSketchSideSize = tryFloat( prefs, keySketch[1], defSketch[1] );   // DISTOX_SKETCH_LINE_STEP
    // mSketchBorderStep  = Float.parseFloat( prefs.getString( keySketch[], defSketch[] );
    // mSketchSectionStep = Float.parseFloat( prefs.getString( keySketch[], defSketch[] );
    mDeltaExtrude   = tryFloat( prefs, keySketch[2], defSketch[2]  );  // DISTOX_DELTA_EXTRUDE
    // mCompassReadings  = tryInt( prefs, keySketch[3], defSketch[] ); // DISTOX_COMPASS_READING 
     * END_SKETCH_3D */

  }

  // ----------------------------------------------------------------------------------
  // return true if the interface must update the value

  public static String updatePreference( TDPrefHelper hlp, int cat, String k, String v )
  {
    // Log.v("DistoXPref", "update pref " + k + " val " + v );
    switch ( cat ) {
      case TDPrefCat.PREF_CATEGORY_ALL:    return updatePrefMain( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_SURVEY: return updatePrefSurvey( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_PLOT:   return updatePrefPlot( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_CALIB:  return updatePrefCalib( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_DEVICE: return updatePrefDevice( hlp, k, v );
      // case TDPrefCat.PREF_CATEGORY_SKETCH: return updatePrefSketch( hlp, k, v ); // FIXME_SKETCH_3D
      case TDPrefCat.PREF_CATEGORY_EXPORT: return updatePrefExport( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_IMPORT: return updatePrefImport( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_SVX:    return updatePrefSvx( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_TH:     return updatePrefTh( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_DAT:    return updatePrefDat( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_CSX:    return updatePrefCsx( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_SVG:    return updatePrefSvg( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_DXF:    return updatePrefDxf( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_SHP:    return updatePrefShp( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_PNG:    return updatePrefPng( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_KML:    return updatePrefKml( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_CSV:    return updatePrefCsv( hlp, k, v );
      case TDPrefCat.PREF_SHOT_DATA:       return updatePrefData( hlp, k, v );
      case TDPrefCat.PREF_SHOT_UNITS:      return updatePrefUnits( hlp, k, v );
      case TDPrefCat.PREF_ACCURACY:        return updatePrefAccuracy( hlp, k, v );
      case TDPrefCat.PREF_LOCATION:        return updatePrefLocation( hlp, k, v );
      case TDPrefCat.PREF_PLOT_SCREEN:     return updatePrefScreen( hlp, k, v );
      case TDPrefCat.PREF_TOOL_LINE:       return updatePrefLine( hlp, k, v );
      case TDPrefCat.PREF_TOOL_POINT:      return updatePrefPoint( hlp, k, v );
      case TDPrefCat.PREF_PLOT_WALLS:      return updatePrefWalls( hlp, k, v );
      case TDPrefCat.PREF_PLOT_DRAW:       return updatePrefDraw( hlp, k, v );
      case TDPrefCat.PREF_PLOT_ERASE:      return updatePrefErase( hlp, k, v );
      case TDPrefCat.PREF_PLOT_EDIT:       return updatePrefEdit( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_GEEK:   return updatePrefGeek( hlp, k, v);
      case TDPrefCat.PREF_GEEK_SHOT:       return updatePrefGeekShot( hlp, k, v );
      case TDPrefCat.PREF_GEEK_PLOT:       return updatePrefGeekPlot( hlp, k, v );
      case TDPrefCat.PREF_GEEK_LINE:       return updatePrefGeekLine( hlp, k, v );
      case TDPrefCat.PREF_GEEK_IMPORT:     return updatePrefGeekImport( hlp, k, v );
      case TDPrefCat.PREF_GEEK_DEVICE:     return updatePrefGeekDevice( hlp, k, v );
      case TDPrefCat.PREF_CATEGORY_LOG:    return updatePrefLog( hlp, k, v );
      default:
        TDLog.Error("DistoXPref. unhandled setting, cat " + cat + " key " + k + " val <" + v + ">" );
    }
    return null;
  }


  private static String updatePrefMain( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    String[] key = TDPrefKey.MAIN;
    String[] def = TDPrefKey.MAINdef;
    // Log.v("DistoX", "update pref main: " + k );
    if ( k.equals( key[0] ) ) {// DISTOX_CWD
      // handled independently
      TopoDroidApp.setCWD( tryStringValue( hlp, k, v, "TopoDroid" ), hlp.getString( "DISTOX_CBD", TDPath.PATH_BASEDIR ) );
    } else if ( k.equals( key[ 1 ] ) ) {              // DISTOX_TEXT_SIZE
      ret = setTextSize( tryIntValue( hlp, k, v, defaultTextSize ) );
    } else if ( k.equals( key[ 2 ] ) ) {              // DISTOX_SIZE_BUTTONS (choice)
      if ( setSizeButtons( tryIntValue( hlp, k, v, defaultButtonSize ) ) ) {
        TopoDroidApp.resetButtonBar();
      }
    } else if ( k.equals( key[ 3 ] ) ) {             // DISTOX_EXTRA_BUTTONS (choice)
      int level = tryIntValue( hlp, k, v, def[3] );
      setActivityBooleans( hlp.getSharedPrefs(), level );
    } else if ( k.equals( key[ 4 ] ) ) {           // DISTOX_MKEYBOARD (bool)
      mKeyboard = tryBooleanValue( hlp, k, v, bool(def[4]) );
    } else if ( k.equals( key[ 5 ] ) ) {           // DISTOX_NO_CURSOR(bool)
      mNoCursor = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) {           // DISTOX_LOCAL_MAN (choice)
      mLocalManPages = handleLocalUserMan( /* hlp.getApp(), */ tryStringValue( hlp, k, v, def[6] ), true );
    } else if ( k.equals( key[ 7 ] ) ) {           // DISTOX_LOCALE (choice)
      TopoDroidApp.setLocale( tryStringValue( hlp, k, v, def[7] ), true );
    } else if ( k.equals( key[ 8 ] ) ) {           // DISTOX_ORIENTATION (choice)
      mOrientation = tryIntValue( hlp, k, v, def[8] );
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
      TDLog.Error("missing MAIN key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  // return the new string value if the value has been corrected
  //        otherwise returns null
  private static String updatePrefSurvey( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref survey: " + k );
    String[] key = TDPrefKey.SURVEY;
    String[] def = TDPrefKey.SURVEYdef;
    String ret = null;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_TEAM (arbitrary)
      mDefaultTeam = tryStringValue( hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SURVEY_STATION (choice)
      parseStationPolicy( hlp, tryStringValue( hlp, k, v, def[1] ) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_STATION_NAMES (choice)
      mStationNames = (tryStringValue( hlp, k, v, def[2]).equals("number"))? 1 : 0;
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_INIT_STATION 
      mInitStation = tryStringValue( hlp, k, v, def[3] ).replaceAll("\\s+", "");
      if ( mInitStation.length() == 0 ) mInitStation = def[3];
      DistoXStationName.setInitialStation( mInitStation );
      if ( ! mInitStation.equals( v ) ) { ret = mInitStation; }
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_THUMBNAIL
      mThumbSize = tryIntValue( hlp, k, v, def[4] ); 
      if ( mThumbSize < 80 )       { mThumbSize = 80;  ret = Integer.toString( mThumbSize ); }
      else if ( mThumbSize > 400 ) { mThumbSize = 400; ret = Integer.toString( mThumbSize ); }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_DATA_BACKUP (bool)
      mDataBackup = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_FIXED_ORIGIN (bool)
      mFixedOrigin = tryBooleanValue( hlp, k, v, bool(def[6]) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_SHARED_XSECTIONS (bool)
      mSharedXSections  = tryBooleanValue( hlp, k, v, bool(def[7]) );
    } else {
      TDLog.Error("missing SURVEY key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefPlot( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref plot: " + k );
    String[] key = TDPrefKey.PLOT;
    String[] def = TDPrefKey.PLOTdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_PICKER_TYPE (choice)
      mPickerType = tryIntValue(   hlp, k, v, def[0] );
      if ( mPickerType < PICKER_LIST ) mPickerType = PICKER_LIST;
    // } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_RECENT_NR (choice)
    //   mRecentNr = tryIntValue( hlp, k, v, def[1] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_TRIPLE_TOOLBAR (bool)
      mTripleToolbar = tryBooleanValue( hlp, k, v, bool(def[1]) );
      TopoDroidApp.setToolsToolbars();
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SIDE_DRAG (bool)
      mSideDrag = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_ZOOM_CTRL (choice)
      // setZoomControls( tryBooleanValue( hlp, k, bool(def[3]) ) );
      setZoomControls( tryStringValue( hlp, k, v, def[3] ), TDandroid.checkMultitouch( TDInstance.context ) );
    // } else if ( k.equals( key[ ? ] ) ) {  // DISTOX_SECTION_STATIONS
    //   mSectionStations = tryIntValue( hlp, k, v, def[ ] );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_HTHRESHOLD
      mHThreshold = tryFloatValue( hlp, k, v, def[4] );
      if ( mHThreshold <  0 ) { mHThreshold =  0; ret = TDString.ZERO; }
      if ( mHThreshold > 90 ) { mHThreshold = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_CHECK_ATTACHED (bool)
      mCheckAttached = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_CHECK_EXTEND (bool)
      mCheckExtend   = tryBooleanValue( hlp, k, v, bool(def[6]) );
    } else {
      TDLog.Error("missing PLOT key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefCalib( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref calib: " + k );
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
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_CALIB_ALGO (choice)
      mCalibAlgo     = tryIntValue( hlp, k, v, def[6] );
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
    } else {
      TDLog.Error("missing CALIB key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
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
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_AUTO_RECONNECT (bool)
      mAutoReconnect = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_HEAD_TAIL (bool)
      mHeadTail = tryBooleanValue( hlp, k, v, bool(def[3]) ); 
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_SOCK_TYPE (choice)
      mSockType = tryIntValue( hlp, k, v, mDefaultSockStrType ); 
    // } else if ( k.equals( key[ ] ) ) { // DISTOX_COMM_RETRY
    //   mCommRetry = tryIntValue( hlp, k, v, def[ ] );
    //   if ( mCommRetry < 1 ) { mCommRetry = 1; ret = Integer.toString( mCommRetry ); }
    //   if ( mCommRetry > 5 ) { mCommRetry = 5; ret = Integer.toString( mCommRetry ); }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_Z6_WORKAROUND (bool)
      mZ6Workaround = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_AUTO_PAIR (bool)
      mAutoPair = tryBooleanValue( hlp, k, v, bool(def[6]) );
      // hlp.getApp().checkAutoPairing();
      TopoDroidApp.checkAutoPairing();
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_CONNECT_FEEDBACK
      mConnectFeedback = tryIntValue( hlp, k, v, def[7] );
    } else {
      TDLog.Error("missing DEVICE key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  // ------------------------------------------------------------------------------------------
  private static String updatePrefGeek( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref data: " + k );
    String[] key = TDPrefKey.GEEK;
    String[] def = TDPrefKey.GEEKdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_PALETTES
      setPalettes( tryBooleanValue( hlp, k, v, bool(def[0]) ) );
    } else if ( k.equals( key[1] ) ) {
      setBackupsClear( tryBooleanValue( hlp, k, v, bool(def[1]) ) ); // DISTOX_BACKUPS_CLEAR
    } else if ( k.equals( key[2] ) ) {
      mPacketLog = tryBooleanValue( hlp, k, v, bool(def[2]) ); // DISTOX_PACKET_LOGGER
    } else {
      TDLog.Error("missing GEEK key: " + k );
    }
    // if ( ret != null ) hlp.update( k, ret );
    return ret;
  }
  private static String updatePrefGeekShot( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref data: " + k );
    String[] key = TDPrefKey.GEEKSHOT;
    String[] def = TDPrefKey.GEEKSHOTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_DIVING_MODE
      mDivingMode   = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_RECENT_SHOT
      mShotRecent   = tryBooleanValue( hlp, k, v, bool(def[1]) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_RECENT_TIMEOUT
      mRecentTimeout = tryIntValue( hlp, k, v, def[2] );
      if ( mRecentTimeout < 0 ) { mRecentTimeout = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_SPLAY_CLASSES
      mSplayClasses = tryBooleanValue( hlp, k, v, bool(def[ 3]) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_SPLAY_COLOR
      mSplayColor   = tryBooleanValue( hlp, k, v, bool(def[ 4]) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_EXTEND_FRAC
      mExtendFrac   = tryBooleanValue( hlp, k, v, bool(def[ 5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_BACKSHOT (bool)
      mDistoXBackshot = tryBooleanValue( hlp, k, v, bool(def[6]) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_BEDDING
      mBedding      = tryBooleanValue( hlp, k, v, bool(def[ 7]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_WITH_SENSORS
      mWithSensors  = tryBooleanValue( hlp, k, v, bool(def[ 8]) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_LOOP_CLOSURE_VALUE
      setLoopClosure( tryIntValue( hlp, k, v, def[ 9] ) );
    } else if ( k.equals( key[ 10] ) ) { // DISTOX_ANDROID_AZIMUTH
      mWithAzimuth  = tryBooleanValue( hlp, k, v, bool(def[10]) );
    } else if ( k.equals( key[ 11 ] ) ) { // DISTOX_SHOT_TIMER [3 ..)
      mTimerWait        = tryIntValue( hlp, k, v, def[11] );
      if ( mTimerWait < 0 ) { mTimerWait = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 12 ] ) ) { // DISTOX_BEEP_VOLUME [0 .. 100]
      mBeepVolume       = tryIntValue( hlp, k, v, def[12] );
      if ( mBeepVolume <   0 ) { mBeepVolume =   0; ret =   TDString.ZERO; }
      if ( mBeepVolume > 100 ) { mBeepVolume = 100; ret = "100"; }
    // } else if ( k.equals( key[13 ] ) ) { // DISTOX_TDMANAGER
    //   mWithTdManager = tryBooleanValue( hlp, k, v, bool(def[13]) );

    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_DIST_TOLERANCE
    //   mDistTolerance = tryFloatValue( hlp, k, v, def[ 9]  );
    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_SPLAY_ACTIVE
    //   mSplayActive  = prefs.getBoolean( key[ 9],  bool(def[ 9]) );
    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_WITH_RENAME
    //   mWithRename   = tryBooleanValue( hlp, k, v, bool(def[ 9]) );
    } else {
      TDLog.Error("missing GEEK_SHOT key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefGeekPlot( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref data: " + k );
    String[] key = TDPrefKey.GEEKPLOT;
    String[] def = TDPrefKey.GEEKPLOTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_PLOT_SHIFT
      mPlotShift    = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_PLOT_SPLIT
      mPlotSplit = tryBooleanValue( hlp, k, v, bool(def[ 1]) );
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
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_BACKUP_NUMBER
      mBackupNumber  = tryIntValue( hlp, k, v, def[ 7] ); 
      if ( mBackupNumber <  4 ) { mBackupNumber =  4; ret = Integer.toString( mBackupNumber ); }
      if ( mBackupNumber > 10 ) { mBackupNumber = 10; ret = Integer.toString( mBackupNumber ); }
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_BACKUP_INTERVAL
      mBackupInterval = tryIntValue( hlp, k, v, def[ 8] );  
      if ( mBackupInterval <  10 ) { mBackupInterval =  10; ret = Integer.toString( mBackupInterval ); }
      if ( mBackupInterval > 600 ) { mBackupInterval = 600; ret = Integer.toString( mBackupInterval ); }
    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_BACKUPS_CLEAR moved to GEEK
    //   setBackupsClear( tryBooleanValue( hlp, k, v, bool(def[ 9]) ) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_AUTO_XSECTIONS
      mAutoXSections = tryBooleanValue( hlp, k, v, bool(def[ 9]) );
    } else if ( k.equals( key[10 ] ) ) { // DISTOX_SAVED_STATIONS
      mSavedStations = tryBooleanValue( hlp, k, v, bool(def[10]) );
    } else if ( k.equals( key[11 ] ) ) { // DISTOX_WITH_LEVELS
      mWithLevels    = tryIntValue( hlp, k, v, def[11] );
    } else {
      TDLog.Error("missing GEEK_PLOT key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefGeekDevice( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    String[] key = TDPrefKey.GEEKDEVICE;
    String[] def = TDPrefKey.GEEKDEVICEdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SOCKET_DELAY
      mConnectSocketDelay = tryIntValue( hlp, k, v, def[0] );  
      if ( mConnectSocketDelay < 0  ) { mConnectSocketDelay =  0; ret = TDString.ZERO; }
      if ( mConnectSocketDelay > 60 ) { mConnectSocketDelay = 60; ret = TDString.SIXTY; } // was 100
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SECOND_DISTOX (bool)
      mSecondDistoX = tryBooleanValue( hlp, k, v, bool(def[1]) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_WAIT_DATA
      mWaitData = tryIntValue( hlp, k, v, def[2] ); 
      if ( mWaitData <    0 ) { mWaitData =    0; ret = Integer.toString( mWaitData ); }
      if ( mWaitData > 2000 ) { mWaitData = 2000; ret = Integer.toString( mWaitData ); }
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_WAIT_CONN
      mWaitConn = tryIntValue( hlp, k, v, def[3] );
      if ( mWaitConn <   50 ) { mWaitConn =   50; ret = Integer.toString( mWaitConn ); }
      if ( mWaitConn > 2000 ) { mWaitConn = 2000; ret = Integer.toString( mWaitConn ); }
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_WAIT_LASER
      mWaitLaser = tryIntValue( hlp, k, v, def[4] );
      if ( mWaitLaser <  500 ) { mWaitLaser =  500; ret = Integer.toString( mWaitLaser ); }
      if ( mWaitLaser > 5000 ) { mWaitLaser = 5000; ret = Integer.toString( mWaitLaser ); }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_WAIT_SHOT
      mWaitShot  = tryIntValue( hlp, k, v, def[5] );
      if ( mWaitShot <   500 ) { mWaitShot =   500; ret = Integer.toString( mWaitShot ); }
      if ( mWaitShot > 10000 ) { mWaitShot = 10000; ret = Integer.toString( mWaitShot ); }
    } else {
      TDLog.Error("missing DEVICE key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
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
    } else if ( k.equals( key[10 ] ) ) { // DISTOX_COMPOSITE_ACTIONS (bool)
      mCompositeActions = tryBooleanValue(  hlp, k, v, bool(def[10]) );

    } else {
      TDLog.Error("missing DEVICE key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  // ------------------------------------------------------------------------------------------
  private static String updatePrefImport( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref import: " + k );
    String[] key = TDPrefKey.EXPORT_import;
    String[] def = TDPrefKey.EXPORT_importdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_PT_CMAP
      // not handled here
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SPLAY_EXTEND (bool)
      mLRExtend = tryBooleanValue( hlp, k, v, bool(def[ 1]) ); 
    } else {
      TDLog.Error("missing EXPORT key: " + k );
    }
    return null;
  }

  private static String updatePrefGeekImport( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref import: " + k );
    String[] key = TDPrefKey.GEEKIMPORT;
    String[] def = TDPrefKey.GEEKIMPORTdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_ZIP_WITH_SYMBOLS
      mZipWithSymbols = tryBooleanValue( hlp, k, v, bool(def[ 0]) ); 
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_IMPORT_DATAMODE (choice)
      mImportDatamode = tryIntValue( hlp, k, v, def[ 1] );
    // } else if ( k.equals( key[ 2 ] ) ) {        // DISTOX_TRANSFER_CSURVEY
    //   mExportTcsx = tryBooleanValue( hlp, k, v, bool(def[ 2]) ); 
    } else {
      TDLog.Error("missing GEEK_IMPORT key: " + k );
    }
    return null;
  }
    
  private static String updatePrefExport( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref export: " + k );
    String[] key = TDPrefKey.EXPORT;
    String[] def = TDPrefKey.EXPORTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_EXPORT_SHOTS (choice)
      mExportShotsFormat = tryIntValue( hlp, k, v, def[ 0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_EXPORT_PLOT (choice)
      mExportPlotFormat = tryIntValue( hlp, k, v, def[ 1] );
    // } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_THERION_MAPS
    //   mTherionMaps = tryBooleanValue( hlp, k, v, bool(def[ 5]) );
    // } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_AUTO_STATIONS
    //   mAutoStations = tryBooleanValue( hlp, k, v, bool(def[ 6]) );
    // // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_XTHERION_AREAS
    // //   mXTherionAreas = tryBooleanValue( hlp, k, v, bool(def[ ]) );   
    // } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_THERION_SPLAYS
    //   mTherionSplays  = tryBooleanValue( hlp, k, v, bool(def[ 7]) );   
    // } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_STATION_PREFIX
    //   mExportStationsPrefix = tryBooleanValue( hlp, k, v, bool(def[ 8]) );
    // } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_COMPASS_SPLAYS
    //   mCompassSplays  = tryBooleanValue( hlp, k, v, bool(def[ 9]) );   
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_ORTHO_LRUD
      mOrthogonalLRUDAngle  = tryFloatValue( hlp, k, v, def[ 2] );
      if ( mOrthogonalLRUDAngle <  0 ) { mOrthogonalLRUDAngle =  0;  ret = TDString.ZERO; }
      if ( mOrthogonalLRUDAngle > 90 ) { mOrthogonalLRUDAngle = 90;  ret = TDString.NINETY; }
      mOrthogonalLRUDCosine = TDMath.cosd( mOrthogonalLRUDAngle );
      mOrthogonalLRUD       = ( mOrthogonalLRUDAngle > 0.000001f ); 
    } else if ( k.equals( key[  3 ] ) ) { // DISTOX_LRUD_VERTICAL
      mLRUDvertical = tryFloatValue( hlp, k, v, def[ 3] );
      if ( mLRUDvertical <  0 ) { mLRUDvertical =  0; ret = TDString.ZERO; }
      if ( mLRUDvertical > 91 ) { mLRUDvertical = 91; ret = TDString.NINETYONE; }
    } else if ( k.equals( key[  4 ] ) ) { // DISTOX_LRUD_HORIZONTAL
      mLRUDhorizontal = tryFloatValue( hlp, k, v, def[ 4] );
      if ( mLRUDhorizontal <  0 ) { mLRUDhorizontal =  0; ret = TDString.ZERO; }
      if ( mLRUDhorizontal > 91 ) { mLRUDhorizontal = 91; ret = TDString.NINETYONE; }
    // } else if ( k.equals( key[  ] ) ) { // DISTOX_SWAP_LR
    //   mSwapLR = tryBooleanValue( hlp, k, v, bool(def[ ]) );
    // } else if ( k.equals( key[  5 ] ) ) { // DISTOX_SURVEX_EOL
    //   mSurvexEol = ( tryStringValue( hlp, k, v, def[ 5] ).equals(def[ 5]) )? "\n" : "\r\n";
    // } else if ( k.equals( key[  6 ] ) ) { // DISTOX_SURVEX_SPLAY
    //   mSurvexSplay = tryBooleanValue( hlp, k, v, bool(def[ 6]) );
    // } else if ( k.equals( key[  7 ] ) ) { // DISTOX_SURVEX_LRUD
    //   mSurvexLRUD = tryBooleanValue( hlp, k, v, bool(def[ 7]) );
    } else if ( k.equals( key[  5 ] ) ) { // DISTOX_BEZIER_STEP
      mBezierStep  = tryFloatValue( hlp, k, v, def[ 5] );
      if ( mBezierStep < 0 ) { mBezierStep = 0; ret = TDString.ZERO; }
      if ( mBezierStep > 3 ) { mBezierStep = 3; ret = TDString.THREE; } // was 2
    } else {
      TDLog.Error("missing EXPORT key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefSvx( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref SVX: " + k );
    String[] key = TDPrefKey.EXPORT_SVX;
    String[] def = TDPrefKey.EXPORT_SVXdef;
    if ( k.equals( key[0] ) ) { // DISTOX_SURVEX_EOL (choice)
      mSurvexEol = ( tryStringValue( hlp, k, v, def[0] ).equals(def[0]) )? "\n" : "\r\n";
    } else if ( k.equals( key[1] ) ) { // DISTOX_SURVEX_SPLAY (bool)
      mSurvexSplay = tryBooleanValue( hlp, k, v, bool(def[1]) );
    } else if ( k.equals( key[2] ) ) { // DISTOX_SURVEX_LRUD (bool)
      mSurvexLRUD = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else {
      TDLog.Error("missing EXPORT SVX key: " + k );
    }
    return null;
  }

  private static String updatePrefTh( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref TH: " + k );
    String ret = null;
    String[] key = TDPrefKey.EXPORT_TH;
    String[] def = TDPrefKey.EXPORT_THdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_THERION_MAPS (bool)
      mTherionMaps      = tryBooleanValue( hlp, k, v, bool(def[ 0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_AUTO_STATIONS (bool)
      mAutoStations     = tryBooleanValue( hlp, k, v, bool(def[ 1]) );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_XTHERION_AREAS (bool)
    //   mXTherionAreas = tryBooleanValue( hlp, k, v, bool(def[ ]) );   
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_THERION_SPLAYS (bool)
      mTherionSplays    = tryBooleanValue( hlp, k, v, bool(def[ 2]) );   
    } else if ( k.equals( key[3] ) ) { // DISTOX_SURVEX_LRUD (bool)
      mSurvexLRUD       = tryBooleanValue( hlp, k, v, bool(def[3]) );
    } else if ( k.equals( key[4] ) ) { // DISTOX_TH2_SCALE
      int scale = tryIntValue( hlp, k, v, def[4] );
      if ( scale < 40 ) { scale = 40; ret = "40"; }
      if ( scale > 2000 ) { scale = 2000; ret = "2000"; }
      mToTherion = THERION_SCALE / scale;
    } else if ( k.equals( key[5] ) ) { // DISTOX_TH2_XVI (bool)
      mTherionXvi = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else {
      TDLog.Error("missing EXPORT TH key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefDat( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref DAT: " + k );
    String[] key = TDPrefKey.EXPORT_DAT;
    String[] def = TDPrefKey.EXPORT_DATdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_STATION_PREFIX (bool)
      mExportStationsPrefix = tryBooleanValue( hlp, k, v, bool(def[ 0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_COMPASS_SPLAYS (bool)
      mCompassSplays        = tryBooleanValue( hlp, k, v, bool(def[ 1]) );   
    } else if ( k.equals( key[  2 ] ) ) { // DISTOX_SWAP_LR (bool)
      mSwapLR               = tryBooleanValue( hlp, k, v, bool(def[ 2]) );
    } else {
      TDLog.Error("missing EXPORT DAT key: " + k );
    }
    return null;
  }

  private static String updatePrefTro( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref TRO: " + k );
    String[] key = TDPrefKey.EXPORT_TRO;
    String[] def = TDPrefKey.EXPORT_TROdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_VTOPO_SPLAYS (bool)
      mVTopoSplays        = tryBooleanValue( hlp, k, v, bool(def[ 0]) );   
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_VTOPO_LRUD (bool)
      mVTopoLrudAtFrom    = tryBooleanValue( hlp, k, v, bool(def[ 1]) );   
    } else {
      TDLog.Error("missing EXPORT TRO key: " + k );
    }
    return null;
  }

  private static String updatePrefKml( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref KML: " + k );
    String[] key = TDPrefKey.EXPORT_KML;
    String[] def = TDPrefKey.EXPORT_KMLdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_KML_STATIONS (bool)
      mKmlStations = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_KML_SPLAYS (bool)
      mKmlSplays   = tryBooleanValue( hlp, k, v, bool(def[ 1 ]) );
    } else {
      TDLog.Error("missing EXPORT KML key: " + k );
    }
    return null;
  }

  private static String updatePrefCsx( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref CSX: " + k );
    String[] key = TDPrefKey.EXPORT_CSX;
    String[] def = TDPrefKey.EXPORT_CSXdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_STATION_PREFIX
      mExportStationsPrefix = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else {
      TDLog.Error("missing EXPORT CSX key: " + k );
    }
    return null;
  }

  private static String updatePrefCsv( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref CSV: " + k );
    String[] key = TDPrefKey.EXPORT_CSV;
    String[] def = TDPrefKey.EXPORT_CSVdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_CSV_RAW (bool)
      mCsvRaw      = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_CSV_SEP (choice)
      mCsvSeparator = CSV_SEPARATOR[ tryIntValue( hlp, k, v, def[1] ) ]; 
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SURVEX_EOL
      mSurvexEol = ( tryStringValue( hlp, k, v, def[2] ).equals(def[0]) )? "\n" : "\r\n";
    } else {
      TDLog.Error("missing EXPORT CSV key: " + k );
    }
    return null;
  }

  private static String updatePrefShp( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref DXF: " + k );
    String[] key = TDPrefKey.EXPORT_SHP;
    String[] def = TDPrefKey.EXPORT_SHPdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SHP_GEOREF (bool)
      mShpGeoref = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else {
      TDLog.Error("missing EXPORT SHP key: " + k );
    }
    return null;
  }

  private static String updatePrefDxf( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref DXF: " + k );
    String[] key = TDPrefKey.EXPORT_DXF;
    String[] def = TDPrefKey.EXPORT_DXFdef;
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_DXF_SCALE
    //   mDxfScale = tryFloatValue( hlp, k, v, def[ ] );
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_DXF_BLOCKS (bool)
      mDxfBlocks = tryBooleanValue( hlp, k, v, bool(def[ 0 ]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_ACAD_VERSION (choice)
      try { mAcadVersion = tryIntValue( hlp, k, v, def[ 1 ] ); } catch ( NumberFormatException e) { }
    } else {
      TDLog.Error("missing EXPORT DXF key: " + k );
    }
    return null;
  }

  private static String updatePrefPng( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref PNG: " + k );
    String[] key = TDPrefKey.EXPORT_PNG;
    String[] def = TDPrefKey.EXPORT_PNGdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_BITMAP_SCALE
      mBitmapScale = tryFloatValue( hlp, k, v, def[0] );
      if ( mBitmapScale < 0.5f ) { mBitmapScale = 0.5f; ret = "0.5"; }
      if ( mBitmapScale >  10f ) { mBitmapScale =  10f; ret = TDString.TEN; }
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_BITMAP_BGCOLOR
      return setBitmapBgcolor( hlp.getSharedPrefs(), k, tryStringValue( hlp, k, v, def[1] ), def[1] );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SVG_GRID
      mSvgGrid = tryBooleanValue( hlp, k, v, bool(def[2]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_THERION_SPLAYS
      mTherionSplays = tryBooleanValue( hlp, k, v, bool(def[ 3]) );   
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_AUTO_STATIONS
      mAutoStations  = tryBooleanValue( hlp, k, v, bool(def[ 4]) );

    } else {
      TDLog.Error("missing EXPORT PNG key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefSvg( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref SVY: " + k );
    String[] key = TDPrefKey.EXPORT_SVG;
    String[] def = TDPrefKey.EXPORT_SVGdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_SVG_ROUNDTRIP (bool)
      mSvgRoundTrip = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_SVG_GRID (bool)
      mSvgGrid = tryBooleanValue( hlp, k, v, bool(def[1]) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_SVG_LINE_DIR (bool)
      mSvgLineDirection = tryBooleanValue( hlp, k, v, bool(def[3]) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_SVG_SPLAYS (bool)
      mSvgSplays = tryBooleanValue( hlp, k, v, bool(def[3]) );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_SVG_IN_HTML (bool)
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_SVG_IN_HTML (bool)
    //   mSvgInHtml = tryBooleanValue( hlp, k, bool(def[ ]) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_SVG_POINT_STROKE
      mSvgPointStroke    = tryFloatValue( hlp, k, v, def[4] );
      if ( mSvgPointStroke < 0.01f ) { mSvgPointStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_SVG_LABEL_STROKE
      mSvgLabelStroke    = tryFloatValue( hlp, k, v, def[5] );
      if ( mSvgLabelStroke < 0.01f ) { mSvgLabelStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_SVG_LINE_STROKE
      mSvgLineStroke     = tryFloatValue( hlp, k, v, def[6] );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_SVG_GRID_STROKE
      mSvgGridStroke     = tryFloatValue( hlp, k, v, def[7] );
      if ( mSvgGridStroke < 0.01f ) { mSvgGridStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_SVG_SHOT_STROKE
      mSvgShotStroke     = tryFloatValue( hlp, k, v, def[8] );
      if ( mSvgShotStroke < 0.01f ) { mSvgShotStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_SVG_LINEDIR_STROKE
      mSvgLineDirStroke  = tryFloatValue( hlp, k, v, def[9] );
      if ( mSvgLineStroke < 0.01f ) { mSvgLineStroke = 0.01f; ret = "0.01"; }
    } else if ( k.equals( key[10] ) ) {  // DISTOX_SVG_STATION_SIZE
      mSvgStationSize    = tryIntValue( hlp, k, v, def[10] );
      if ( mSvgStationSize < 1 ) { mSvgStationSize = 1; ret = "1"; }
    // } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_BEZIER_STEP
    //   mBezierStep  = tryFloatValue( hlp, k, v, def[8] );
    } else {
      TDLog.Error("missing EXPORT_SVG key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }
  // --------------------------------------------------------------------------

  private static String updatePrefData( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref data: " + k );
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
      TDAzimuth.resetRefAzimuth( TopoDroidApp.mShotWindow, TDAzimuth.mRefAzimuth );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_PREV_NEXT (bool)
      mPrevNext = tryBooleanValue( hlp, k, v, bool(def[ 7]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_BACKSIGHT (bool)
      mBacksightInput = tryBooleanValue( hlp, k, v, bool(def[ 8]) );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_LEG_FEEDBACK
      mTripleShot   = tryIntValue( hlp, k, v, def[ 9] );
    } else {
      TDLog.Error("missing DATA key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefUnits( TDPrefHelper hlp, String k, String v )
  {
    // Log.v("DistoX", "update pref units: " + k );
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
      TDLog.Error("missing UNITS key: " + k );
    }
    return null;
  }

  private static String updatePrefAccuracy( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref accuracy: " + k );
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
    } else {
      TDLog.Error("missing ACCURACY key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefLocation( TDPrefHelper hlp, String k, String v )
  {
    // String ret = null;
    // Log.v("DistoX", "update pref location: " + k );
    String[] key = TDPrefKey.LOCATION;
    String[] def = TDPrefKey.LOCATIONdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNIT_LOCATION (choice)
      mUnitLocation  = tryStringValue( hlp, k, v, def[0] ).equals(def[0]) ? TDUtil.DDMMSS : TDUtil.DEGREE;
      // TDLog.Log( TDLog.LOG_UNITS, "mUnitLocation changed " + mUnitLocation );
    // } else if ( k.equals( key[ ? ] ) ) { // DISTOX_ALTITUDE
    //   try {
    //     mAltitude = Integer.parseInt( tryStringValue( hlp, k, v, ALTITUDE ) );
    //   } catch ( NumberFormatException e ) { mAltitude = _WGS84; }
    } else if ( k.equals( key[ 1 ] ) ) {
      mCRS = tryStringValue( hlp, k, v, def[1] );     // DISTOX_CRS (arbitrary)
    } else {
      TDLog.Error("missing LOCATION key: " + k );
    }
    // if ( ret != null ) hlp.update( k, ret );
    return null;
  }


  private static String updatePrefScreen( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref screen: " + k );
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
      } catch ( NumberFormatException e ) { }
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
      if ( mSplayAlpha < 0 ) { mSplayAlpha = 0; ret = Float.toString(mSplayAlpha); }
      if ( mSplayAlpha > 100 ) { mSplayAlpha = 100; ret = Float.toString(mSplayAlpha); }
      BrushManager.setSplayAlpha( mSplayAlpha );
    } else {
      TDLog.Error("missing SCREEN key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefLine( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref line: " + k );
    String[] key = TDPrefKey.LINE;
    String[] def = TDPrefKey.LINEdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_LINE_THICKNESS
      ret = setLineThickness( tryStringValue( hlp, k, v, def[0] ) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_LINE_UNITS
      try { setDrawingUnitLines( tryFloatValue( hlp, k, v, def[1] ) ); } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LINE_STYLE (choice)
      setLineStyleAndType( tryStringValue( hlp, k, v, def[2] ) );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_LINE_SEGMENT
      ret = setLineSegment( tryIntValue(   hlp, k, v, def[3] ) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_ARROW_LENGTH
      ret = setArrowLength( tryFloatValue( hlp, k, v, def[4] ) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_AUTO_SECTION_PT (bool)
      mAutoSectionPt = tryBooleanValue( hlp, k, v, bool(def[5]) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_LINE_CONTINUE (choice)
      mContinueLine  = tryIntValue( hlp, k, v, def[6] );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_AREA_BORDER (bool)
      mAreaBorder = tryBooleanValue( hlp, k, v, bool(def[7]) );
    } else {
      TDLog.Error("missing LINE key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefPoint( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref point: " + k );
    String[] key = TDPrefKey.POINT;
    String[] def = TDPrefKey.POINTdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNSCALED_POINTS (bool)
      mUnscaledPoints = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_DRAWING_UNIT 
      try { setDrawingUnitIcons( tryFloatValue( hlp, k, v, def[1] ) ); } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LABEL_SIZE
      try {
        setLabelSize( Float.parseFloat( tryStringValue( hlp, k, v, def[2] ) ), true );
      } catch ( NumberFormatException e ) { }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
      ret = String.format(Locale.US, "%.2f", mLabelSize );
    } else {
      TDLog.Error("missing POINT key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefWalls( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref walls: " + k );
    String[] key = TDPrefKey.WALLS;
    String[] def = TDPrefKey.WALLSdef;
    if ( k.equals( key[ 0 ] ) ) {        // DISTOX_WALLS_TYPE (choice)
      mWallsType = tryIntValue(hlp, k, v, def[0] );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_WALLS_PLAN_THR
      mWallsPlanThr = tryFloatValue( hlp, k, v, def[1] );
      if ( mWallsPlanThr < 0 ) { mWallsPlanThr  =  0; ret = TDString.ZERO; }
      if ( mWallsPlanThr > 90 ) { mWallsPlanThr = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_WALLS_EXTENDED_THR
      mWallsExtendedThr = tryFloatValue( hlp, k, v, def[2] );
      if ( mWallsExtendedThr < 0 ) { mWallsExtendedThr = 0; ret = TDString.ZERO; }
      if ( mWallsExtendedThr > 90 ) { mWallsExtendedThr = 90; ret = TDString.NINETY; }
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_WALLS_XCLOSE
      mWallsXClose = tryFloatValue( hlp, k, v, def[3] );
      if ( mWallsXClose < 0 ) { mWallsXClose = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_WALLS_CONCAVE
      mWallsConcave = tryFloatValue( hlp, k, v, def[4] );
      if ( mWallsConcave < 0 ) { mWallsConcave = 0; ret = TDString.ZERO; }
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_WALLS_XSTEP
      mWallsXStep = tryFloatValue( hlp, k, v, def[5] );
      if ( mWallsXStep < 0 ) { mWallsXStep = 0; ret = TDString.ZERO; }
    } else {
      TDLog.Error("missing WALLS key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefDraw( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref draw: " + k );
    String[] key = TDPrefKey.DRAW;
    String[] def = TDPrefKey.DRAWdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_UNSCALED_POINTS (bool)
      mUnscaledPoints = tryBooleanValue( hlp, k, v, bool(def[0]) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_DRAWING_UNIT (choice)
      try { setDrawingUnitIcons( tryFloatValue( hlp, k, v, def[1] ) ); } catch ( NumberFormatException e ) { }
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_LABEL_SIZE
      try {
        setLabelSize( Float.parseFloat( tryStringValue( hlp, k, v, def[2] ) ), true );
      } catch ( NumberFormatException e ) { }
      // FIXME changing label size affects only new labels; not existing labels (until they are edited)
      ret = String.format(Locale.US, "%.2f", mLabelSize );
    } else if ( k.equals( key[ 3 ] ) ) { // DISTOX_LINE_THICKNESS
      ret = setLineThickness( tryStringValue( hlp, k, v, def[3] ) );
    } else if ( k.equals( key[ 4 ] ) ) { // DISTOX_LINE_STYLE (choice)
      setLineStyleAndType( tryStringValue( hlp, k, v, def[4] ) );
    } else if ( k.equals( key[ 5 ] ) ) { // DISTOX_LINE_SEGMENT
      ret = setLineSegment( tryIntValue(   hlp, k, v, def[5] ) );
    } else if ( k.equals( key[ 6 ] ) ) { // DISTOX_ARROW_LENGTH
      ret = setArrowLength( tryFloatValue( hlp, k, v, def[6] ) );
    } else if ( k.equals( key[ 7 ] ) ) { // DISTOX_AUTO_SECTION_PT (bool)
      mAutoSectionPt = tryBooleanValue( hlp, k, v, bool(def[7]) );
    } else if ( k.equals( key[ 8 ] ) ) { // DISTOX_LINE_CONTINUE (choice)
      mContinueLine  = tryIntValue( hlp, k, v, def[8] );
    } else if ( k.equals( key[ 9 ] ) ) { // DISTOX_AREA_BORDER (bool)
      mAreaBorder = tryBooleanValue( hlp, k, v, bool(def[9]) );
    // } else if ( k.equals( key[ 10 ] ) ) { // DISTOX_REDUCE_ANGLE
    //   ret = setReduceAngle( tryFloatValue( hlp, k, v, def[10] ) );
    } else {
      TDLog.Error("missing DRAW key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefErase( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref erase: " + k );
    String[] key = TDPrefKey.ERASE;
    String[] def = TDPrefKey.ERASEdef;
    if ( k.equals( key[ 0 ] ) ) { // DISTOX_CLOSENESS
      ret = setSelectness( tryFloatValue( hlp, k, v, def[0] ) );
    } else if ( k.equals( key[ 1 ] ) ) { // DISTOX_ERASENESS
      ret = setEraseness( tryFloatValue( hlp, k, v, def[1] ) );
    } else if ( k.equals( key[ 2 ] ) ) { // DISTOX_POINTING
      ret = setPointingRadius( tryIntValue(   hlp, k, v, def[2] ) );
    } else {
      TDLog.Error("missing ERASE key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  private static String updatePrefEdit( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref edit: " + k );
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
      TDLog.Error("missing EDIT key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }

  /* FIXME_SKETCH_3D *
  private static String updatePrefSketch( TDPrefHelper hlp, String k, String v )
  {
    String ret = null;
    // Log.v("DistoX", "update pref sketch: " + k );
    String[] key = TDPrefKey.SKETCH;
    String[] def = TDPrefKey.SKETCHdef;
    // if ( k.equals( key[ ? ] ) ) {
    //   mSketchUsesSplays = tryBooleanValue( hlp, k, v, bool(def[ ]) );
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
      TDLog.Error("missing SKETCH key: " + k );
    }
    if ( ret != null ) hlp.update( k, ret );
    return ret;
  }
  * END_SKETCH_3D */
 
  // @param k   key
  // @param v   value (either "true" of "false" for checkboxes)
  private static String updatePrefLog( TDPrefHelper hlp, String k, String v )
  {
    String[] key = TDPrefKey.LOG;
    if ( k.equals( key[0] ) ) { // DISTOX_LOG_STREAM
      hlp.update( k, v );
      TDLog.checkLogPreferences( hlp.getSharedPrefs(), k, v ); // FIXME_PREF
    } else {
      boolean def = k.equals( key[3] ); // DSITOX_LOG_ERR
      boolean b = tryBooleanValue( hlp, k, v, def );
      TDLog.checkLogPreferences( hlp.getSharedPrefs(), k, b ); // FIXME_PREF
    }
    return null;
  }

  // -----------------------------------------------------------------------------

  private static void setLineStyleAndType( String style )
  {
    mLineStyle = LINE_STYLE_TWO; // default
    mLineType  = 1;
    if ( style.equals( TDString.ZERO ) ) {
      mLineStyle = LINE_STYLE_BEZIER;
      // mLineType  = 1;                 // alreday assigned
    } else if ( style.equals( TDString.ONE ) ) {
      mLineStyle = LINE_STYLE_ONE;
      // mLineType  = 1;                 // already assignd
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

  private static void setBackupsClear( boolean b )
  {
    if ( mBackupsClear != b ) {
      mBackupsClear = b;
      TopoDroidApp.resetButtonBar();
      TopoDroidApp.setMenuAdapter();
    }
  }

  private static void setPalettes( boolean b )
  {
    if ( mPalettes != b ) {
      mPalettes = b;
      // TopoDroidApp.resetButtonBar();
      TopoDroidApp.setMenuAdapter();
    }
  }

  private static void setActivityBooleans( SharedPreferences prefs, int level )
  {
    if ( level == TDLevel.mLevel ) return;

    if ( StationPolicy.policyDowngrade( level ) ) {
      setPreference( prefs, TDPrefKey.SURVEY[1], TDString.ONE );
    }
    TDLevel.setLevel( TDInstance.context, level );
    int policy = StationPolicy.policyUpgrade( level );
    if ( policy > 0 ) {
      setPreference( prefs, TDPrefKey.SURVEY[1], Integer.toString( policy ) );
    }
    // if ( ! TDLevel.overExpert ) {
    //   mMagAnomaly = false; // magnetic anomaly compensation requires level overExpert
    // }
    TopoDroidApp.resetButtonBar();
    TopoDroidApp.setMenuAdapter();
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
      // } else if ( policy == StationPolicy.SURVEY_STATION_BACKSIGHT ) {
      //   TDToast.make( R.string.backsight_warning );
      } else if ( policy == StationPolicy.SURVEY_STATION_ANOMALY ) {
        TDToast.make( R.string.anomaly_warning );
      // } else {
        // nothing
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

  private static void setPreference( SharedPreferences sp, String name, String value )
  {
    // Log.v("DistoXPref", "TDSetting set pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putString( name, value );
    TDandroid.applyEditor( editor );
  }

  private static void setPreference( SharedPreferences sp, String name, boolean value )
  {
    // Log.v("DistoXPref", "TDSetting set b-pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putBoolean( name, value );
    TDandroid.applyEditor( editor );
  }

  public static void setPreference( SharedPreferences sp, String name, long value )
  {
    // Log.v("DistoXPref", "TDSetting set l-pref " + name + " " + value );
    Editor editor = sp.edit();
    editor.putLong( name, value );
    TDandroid.applyEditor( editor );
  }

  public static long getLongPreference( SharedPreferences sp, String name, long def_value )
  {
    return sp.getLong( name, def_value ); 
  }

  // -----------------------------------------------------------------------
  //
  public static boolean handleLocalUserMan( /* Context my_app, */ String man, boolean download ) 
  {
    int idx = Integer.parseInt( man ); // no throw
    if ( idx > 0 && idx < 5 ) { 
      if ( download && TDandroid.checkInternet( TDInstance.context ) ) { // download user manual 
       	int[] res = {
	         0,
	         R.string.user_man_fr,
	         R.string.user_man_es,
	         R.string.user_man_it,
	         R.string.user_man_ru
	       };
        String url = TDInstance.getResources().getString( res[idx] );
       	if ( url != null && url.length() > 0 ) {
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

  public static void exportSettings( )
  {
    // Log.v("DistoX", "TDSetting exports settings");
    File file = TDPath.getSettingsFile();
    try {
      FileWriter fw = new FileWriter( file, false ); // true = append
      PrintWriter pw = new PrintWriter( fw, true ); // true = autoflush
      pw.printf(Locale.US, "TopoDroid v. %s\n", TDVersion.string() );
      pw.printf(Locale.US, "Buttons Size %2d %3d\n", mSizeBtns, mSizeButtons);
      pw.printf(Locale.US, "Text Size %3d\n", mTextSize);
      pw.printf(Locale.US, "Keyboard %c, no-cursor %c\n", tf(mKeyboard), tf(mNoCursor) );
      pw.printf(Locale.US, "Local man %c\n", tf(mLocalManPages) );
      pw.printf(Locale.US, "Palettes %c\n", tf(mPalettes) );
      pw.printf(Locale.US, "Orientation %d\n", mOrientation );

      pw.printf(Locale.US, "Auto-export: data %c / %d, plot %d \n", tf(mDataBackup), mExportShotsFormat, mExportPlotFormat );
      String eol = "\\n"; if ( mSurvexEol.equals("\r\n") ) eol = "\\r\\n";
      pw.printf(Locale.US, "Survex: eol \"%s\", splay %c LRUD %c \n", eol, tf(mSurvexSplay), tf(mSurvexLRUD) );
      pw.printf(Locale.US, "Compass: swap LR %c, station prefix %c, splays %c\n", tf(mSwapLR), tf(mExportStationsPrefix), tf(mCompassSplays) );
      pw.printf(Locale.US, "VisualTopo: splays %c at-from %c\n", tf(mVTopoSplays), tf(mVTopoLrudAtFrom) ); 
      pw.printf(Locale.US, "Ortho LRUD %c, angle %.2f cos %.2f \n", tf(mOrthogonalLRUD), mOrthogonalLRUDAngle, mOrthogonalLRUDCosine );
      pw.printf(Locale.US, "Therion: maps %c, stations %c, splays %c, xvi %c, scale %.2f\n",
        tf(mTherionMaps), tf(mAutoStations), tf(mTherionSplays), tf(mTherionXvi), mToTherion );
      pw.printf(Locale.US, "PNG scale %.2f, bg color %d\n", mBitmapScale, mBitmapBgcolor );
      pw.printf(Locale.US, "DXF: acad version %d, blocks %c \n", mAcadVersion, tf(mDxfBlocks) );
      pw.printf(Locale.US, "SVG: shot %.1f, label %.1f, station %d, point %.1f, round-trip %c grid %c %.1f, line %.1f, dir %c %.1f, splays %c\n",
        mSvgShotStroke, mSvgLabelStroke, mSvgStationSize, mSvgPointStroke,
        tf(mSvgRoundTrip), tf(mSvgGrid), mSvgGridStroke, mSvgLineStroke, tf(mSvgLineDirection), mSvgLineDirStroke, tf(mSvgSplays) );
      pw.printf(Locale.US, "SHP: georef-plan %c\n", tf(mShpGeoref) );
      pw.printf(Locale.US, "KML: stations %c, splays %c\n", tf(mKmlStations), tf(mKmlSplays) );
      pw.printf(Locale.US, "CSV: raw %c, separator \'%c\'\n", tf(mCsvRaw), mCsvSeparator );

      pw.printf(Locale.US, "BT: check %d, autopair %c \n", mCheckBT, tf(mAutoPair) );
      pw.printf(Locale.US, "Socket: type \"%s\" / %d, delay %d\n", mDefaultSockStrType, mSockType, mConnectSocketDelay );
      pw.printf(Locale.US, "Connection mode %d, Z6 %c, feedback %d\n", mConnectionMode, tf(mZ6Workaround), mConnectFeedback );
      pw.printf(Locale.US, "Communication type %d, autoreconnect %c, DistoX-B %c, retry %d, head/tail %c\n", mCommType, tf(mAutoReconnect), tf(mSecondDistoX), mCommRetry, tf(mHeadTail) );
      pw.printf(Locale.US, "Packet log %c\n", tf(mPacketLog) );
      pw.printf(Locale.US, "Wait: laser %d, shot %d, data %d, conn %d, command %d\n", mWaitLaser, mWaitShot, mWaitData, mWaitConn, mWaitCommand );

      pw.printf(Locale.US, "Calib grous %d, distance %.2f\n", mGroupBy, mGroupDistance);
      pw.printf(Locale.US, "Calib algo %d, eps %f, iter %d\n", mCalibAlgo, mCalibEps, mCalibMaxIt );
      pw.printf(Locale.US, "Calib shot download %c, raw data %d \n", tf(mCalibShotDownload), mRawCData );
      pw.printf(Locale.US, "Min_Algo alpha %.1f, beta %.1f, gamma %.1f, delta %.1f \n", mAlgoMinAlpha, mAlgoMinBeta, mAlgoMinGamma, mAlgoMinDelta );

      pw.printf(Locale.US, "Default Team \"%s\"\n", mDefaultTeam);
      pw.printf(Locale.US, "Midline check: attached %c, extend %c\n", tf(mCheckAttached), tf(mCheckExtend) );
      pw.printf(Locale.US, "Location: units %d, CRS \"%s\"\n", mUnitLocation, mCRS );
      pw.printf(Locale.US, "Shots: vthr %.1f, hthr %.1f \n", mVThreshold, mHThreshold );
      pw.printf(Locale.US, "Data: DistoX-backshot-swap %c, diving-mode %c \n", tf(mDistoXBackshot), tf(mDivingMode) );
      // pw.printf(Locale.US, "Data: diving-mode %c \n", tf(mDivingMode) );
      pw.printf(Locale.US, "Data input: backsight %c, prev/next %c\n", tf(mBacksightInput), tf(mPrevNext) );
      pw.printf(Locale.US, "L/R extend %c\n", tf(mLRExtend) );
      pw.printf(Locale.US, "U/D vertical %.1f, L/R horicontal %.1f\n", mLRUDvertical, mLRUDhorizontal );

      pw.printf(Locale.US, "Geek Import - data mode %d, zipped symbols %c\n", mImportDatamode, tf( mZipWithSymbols) ); //  tf(mExportTcsx) );
      pw.printf(Locale.US, "Timer: wait %d, volume %d\n", mTimerWait, mBeepVolume );
      pw.printf(Locale.US, "Recent data %c, timeout %d\n", tf(mShotRecent), mRecentTimeout );
      pw.printf(Locale.US, "Leg: closeness %.2f, nr %d, triple-shot %d, max %.2f, min %.2f\n",
        mCloseDistance, mMinNrLegShots, mTripleShot, mMaxShotLength, mMinLegLength );
      pw.printf(Locale.US, "Splay: vthr %.1f, classes %c\n", mSplayVertThrs, tf(mSplayClasses) );
      pw.printf(Locale.US, "Stations: names %d, init \"%s\"\n", mStationNames, mInitStation );
      pw.printf(Locale.US, "Extend: thr %.1f, manual %c, frac %c\n", mExtendThr, tf(mAzimuthManual), tf(mExtendFrac) );
      pw.printf(Locale.US, "Loop: %d \n", mLoopClosure );
      pw.printf(Locale.US, "Units: length %.2f [%s], angle %.2f [%s]\n", mUnitLength, mUnitLengthStr, mUnitAngle, mUnitAngleStr );
      pw.printf(Locale.US, "ThumbSize %d, SavedStations %c, WithAzimuth %c, WithSensors %c, Bedding %c \n", // TdManager %c\n",
        mThumbSize, tf(mSavedStations), tf(mWithAzimuth), tf(mWithSensors), tf(mBedding) ); // , tf(mWithTdManager) );

      pw.printf(Locale.US, "Plot: zoom %d, drag %c, fix-origin %c, split %c, shift %c, levels %d\n",
        mZoomCtrl, tf(mSideDrag), tf(mFixedOrigin), tf(mPlotSplit), tf(mPlotShift), mWithLevels );
      pw.printf(Locale.US, "Units: icon %.2f, line %.2f, grid %.2f, ruler %.2f\n", mUnitIcons, mUnitLines, mUnitGrid, mUnitMeasure );
      pw.printf(Locale.US, "Size: station %.1f, label %.1f, fixed %.1f line %.1f\n", mStationSize, mLabelSize, mFixedThickness, mLineThickness );
      pw.printf(Locale.US, "Select: radius %.2f [min %.2f], pointing %d, shift %d, dot %.1f, multiple %c \n",
        mSelectness, mCloseCutoff, mPointingRadius, mMinShift, mDotRadius, tf(mPathMultiselect) );
      pw.printf(Locale.US, "Erase: radius %.2f\n", mEraseness );
      pw.printf(Locale.US, "Picker: type %d\n", mPickerType );
      pw.printf(Locale.US, "Point: unscaled %c\n", tf(mUnscaledPoints) );
      pw.printf(Locale.US, "Line: style %d, type %d, segment %d, continue %d, arrow %.1f\n",
        mLineStyle, mLineType, mLineSegment, mContinueLine, mArrowLength );
      pw.printf(Locale.US, "Bezier: step %.2f, accuracy %.2f, corner %.2f\n", mBezierStep, mLineAccuracy, mLineCorner );
      pw.printf(Locale.US, "Weed: distance %.2f, length %.2f, buffer %.2f\n", mWeedDistance, mWeedLength, mWeedBuffer );
      pw.printf(Locale.US, "Area: border %c\n", tf(mAreaBorder) );
      pw.printf(Locale.US, "Backup: nr %d, interval %d, clear %c\n", mBackupNumber, mBackupInterval, tf(mBackupsClear) );
      pw.printf(Locale.US, "XSections: shared %c, auto-export %c, point %c\n", tf(mSharedXSections), tf(mAutoXSections), tf(mAutoSectionPt) );
      pw.printf(Locale.US, "Actions: snap %c, curve %c, straight %c %.1f\n", tf(mLineSnap), tf(mLineCurve), tf(mLineStraight), mReduceAngle );
      pw.printf(Locale.US, "Splay: alpha %d, color %c, splay-dash %d, vert %.1f, horiz %.1f, section %.1f\n",
        mSplayAlpha, tf(mSplayColor), mDashSplay, mVertSplay, mHorizSplay, mSectionSplay );
      pw.printf(Locale.US, "Accuracy: G %.2f, M %.2f, dip %.2f\n", mAccelerationThr, mMagneticThr, mDipThr );
      // pw.printf(Locale.US, "Sketch: type %d, size %.2f, extrude %.2f\n", mSketchModelType, mSketchSideSize, mDeltaExtrude );
      pw.printf(Locale.US, "Walls: type %d, thr P %.2f E %.2f, close %.2f, step %.2f, concave %.2f\n",
        mWallsType, mWallsPlanThr, mWallsExtendedThr, mWallsXClose, mWallsXStep, mWallsConcave );

      TDLog.exportLogSettings( pw );

      fw.close();
    } catch ( IOException e ) { TDLog.Error("failed to export settings"); }
  }
}
