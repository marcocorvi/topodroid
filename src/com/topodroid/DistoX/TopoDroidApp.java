/* @file TopoDroidApp.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid application (consts and prefs)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
// import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.io.InputStream;
// import java.io.FileInputStream;
// import java.io.BufferedInputStream;
import java.io.FileOutputStream;
// import java.io.BufferedOutputStream;
// import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
// import java.util.zip.ZipFile;

import java.util.Locale;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
// import java.util.Stack;


// import android.widget.ArrayAdapter;

// import android.os.Environment;
// import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
// import android.os.Debug;
// import android.os.SystemClock; // FIXME TROBOT

import android.app.Application;
// import android.app.KeyguardManager;
// import android.app.KeyguardManager.KeyguardLock;
// import android.app.Activity;

import android.content.Context;
// import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Configuration;
// import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
// import android.content.FileProvider;

// import android.provider.Settings.System;
// import android.provider.Settings.SettingNotFoundException;

// import android.view.WindowManager;
// import android.view.Display;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
// import android.graphics.Point;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
// import android.graphics.drawable.BitmapDrawable;

// import android.net.Uri;

import android.util.Log;
import android.util.DisplayMetrics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

// import android.widget.Toast;

public class TopoDroidApp extends Application
{
  static final String EMPTY = "";

  static TDPrefHelper mPrefHlp = null;

  static final String SYMBOL_VERSION = "35";
  static String VERSION = "0.0.0"; 
  static int VERSION_CODE = 0;
  static int MAJOR = 0;
  static int MINOR = 0;
  static int SUB   = 0;
  static final int MAJOR_MIN = 2; // minimum compatible version
  static final int MINOR_MIN = 1;
  static final int SUB_MIN   = 1;
  
  boolean mWelcomeScreen;  // whether to show the welcome screen
  boolean mSetupScreen;    // whether to show the welcome screen
  // static String mManual;  // manual url
  static Locale mLocale;
  static String mLocaleStr;
  static int mCheckPerms;

  static private MyTurnBitmap mDialBitmap = null;

  static MyTurnBitmap getDialBitmap( Resources res )
  {
    if ( mDialBitmap == null ) {
      Bitmap dial = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp ); // FIXME AZIMUTH_DIAL
      mDialBitmap = new MyTurnBitmap( dial, 0x00000000 );
    }
    return mDialBitmap;
  }

  static String mClipboardText = null; // text clipboard

  public static float mScaleFactor   = 1.0f;
  public static float mDisplayWidth  = 200f;
  public static float mDisplayHeight = 320f;

  // static boolean isTracing = false;

  // static float mManualCalibrationLength  = 0; // calibration of manually inputed data: length
  // static float mManualCalibrationAzimuth = 0;
  // static float mManualCalibrationClino   = 0;
  // static boolean mManualCalibrationLRUD  = false;

  // static private void resetManualCalibrations() 
  // {
  //   mManualCalibrationLength  = 0; 
  //   mManualCalibrationAzimuth = 0;
  //   mManualCalibrationClino   = 0;
  //   mManualCalibrationLRUD    = false;
  // }

  private List<DBlock> mHighlighted = null;
  void    setHighlighted( List<DBlock> blks ) { mHighlighted = blks; }
  // int     getHighlightedSize() { return (mHighlighted != null)? -1 : mHighlighted.size(); }
  boolean hasHighlighted() { return mHighlighted != null && mHighlighted.size() > 0; }
  boolean hasHighlightedId( long id )
  {
    if ( mHighlighted == null ) return false;
    for ( DBlock b : mHighlighted ) if ( b.mId == id ) return true;
    return false;
  }

  // cross-section splay display mode
  int mSplayMode = 2; 
  boolean mShowSectionSplays = true;
  
  // ----------------------------------------------------------------------
  // data lister
  // ListerSet mListerSet;
  ListerSetHandler mListerSet; // FIXME_LISTER

  void registerLister( ILister lister ) { mListerSet.registerLister( lister ); }
  void unregisterLister( ILister lister ) { mListerSet.unregisterLister( lister ); }

  void notifyStatus( )
  { 
    mListerSet.setConnectionStatus( mDataDownloader.getStatus() );
  }

  void notifyDisconnected()
  {
    if ( mListerSet.size() > 0 ) {
      try {
        new ReconnectTask( mDataDownloader ).execute();
      } catch ( RuntimeException e ) {
        TDLog.Error("reconnect error: " + e.getMessage() );
      }
    }
  }



  // -----------------------------------------------------

  // FIXME INSTALL_SYMBOL boolean askSymbolUpdate = false; // by default do not ask

  String[] DistoXConnectionError;
  BluetoothAdapter mBTAdapter = null;     // BT connection
  private TopoDroidComm mComm = null;     // BT communication
  DataDownloader mDataDownloader = null;  // data downloader
  static DataHelper mData = null;         // database 
  static DeviceHelper mDData = null;      // device/calib database

  static SurveyWindow mSurveyWindow = null;
  static ShotWindow   mShotWindow   = null;
  // static DrawingWindow mDrawingWindow = null; // FIXME currently not used
  static MainWindow mActivity = null; 

  static boolean mDeviceActivityVisible = false;
  static boolean mGMActivityVisible = false;

  private static long lastShotId( ) { return mData.getLastShotId( TDInstance.sid ); }

  // static Device mDevice = null;
  // static int distoType() { return (mDevice == null)? 0 : mDevice.mType; }
  // static String distoAddress() { return (mDevice == null)? null : mDevice.mAddress; }

  VirtualDistoX mVirtualDistoX = new VirtualDistoX();

  // -------------------------------------------------------------------------------------
  // static SIZE methods

  static float getDisplayDensity( )
  {
    return Resources.getSystem().getDisplayMetrics().density;
  }

  static float getDisplayDensity( Context context )
  {
    return Resources.getSystem().getDisplayMetrics().density;
  }

  // int setListViewHeight( HorizontalListView listView )
  // {
  //   return TopoDroidApp.setListViewHeight( this, listView );
  // }

  static int setListViewHeight( Context context, HorizontalListView listView )
  {
    // int size = getScaledSize( context );
    if ( listView != null ) {
      LayoutParams params = listView.getLayoutParams();
      params.height = TDSetting.mSizeButtons + 10;
      listView.setLayoutParams( params );
    }
    return TDSetting.mSizeButtons;
  }

  // UNUSED default button size
  // static int getScaledSize( Context context )
  // {
  //   return (int)( TDSetting.mSizeButtons * context.getResources().getSystem().getDisplayMetrics().density );
  // }

  // UNUSED was called by HelpEntry
  // static int getDefaultSize( Context context )
  // {
  //   return (int)( 42 * context.getResources().getSystem().getDisplayMetrics().density );
  // }

  // ------------------------------------------------------------
  // CONSTS
  // private static final byte char0C = 0x0c;

  // ---------------------------------------------------------------
  // ConnListener
  ArrayList< Handler > mConnListener = null;

  void registerConnListener( Handler hdl )
  {
    if ( hdl != null && mConnListener != null ) {
      mConnListener.add( hdl );
      // try {
      //   new Messenger( hdl ).send( new Message() );
      // } catch ( RemoteException e ) { }
    }
  }

  void unregisterConnListener( Handler hdl )
  {
    if ( hdl != null && mConnListener != null ) {
      // try {
      //   new Messenger( hdl ).send( new Message() );
      // } catch ( RemoteException e ) { }
      mConnListener.remove( hdl );
    }
  }

  private void notifyConnState( int w )
  {
    // Log.v( TAG, "notify conn state" );
    if ( mConnListener == null ) return;
    for ( Handler hdl : mConnListener ) {
      try {
        Message msg = Message.obtain();
        msg.what = w;
        new Messenger( hdl ).send( msg );
      } catch ( RemoteException e ) { }
    }
  }
  
  // ---------------------------------------------------------------
  // survey/calib info
  //

  boolean checkCalibrationDeviceMatch() 
  {
    CalibInfo info = mDData.selectCalibInfo( TDInstance.cid  );
    // TDLog.Log( TDLog.LOG_CALIB, "info.device " + ((info == null)? "null" : info.device) );
    // TDLog.Log( TDLog.LOG_CALIB, "device " + ((mDevice == null)? "null" : mDevice.mAddress) );
    return ( TDInstance.device == null || ( info != null && info.device.equals( TDInstance.device.mAddress ) ) );
  }

  public static SurveyInfo getSurveyInfo()
  {
    if ( TDInstance.sid <= 0 ) return null;
    if ( mData == null ) return null;
    return mData.selectSurveyInfo( TDInstance.sid );
    // if ( info == null ) TDLog.Error("null survey info. sid " + TDInstance.sid );
  }

  public CalibInfo getCalibInfo()
  {
    if ( TDInstance.cid <= 0 ) return null;
    if ( mDData == null ) return null;
    return mDData.selectCalibInfo( TDInstance.cid );
  }

  Set<String> getStationNames() { return mData.selectAllStations( TDInstance.sid ); }

  // ----------------------------------------------------------------

  @Override
  public void onTerminate()
  {
    super.onTerminate();
    // Log.v(TAG, "onTerminate app");
  }

  static void setDeviceModel( Device device, int model )
  {
    if ( device != null && device == TDInstance.device ) {
      if ( device.mType != model ) {
        if ( model == Device.DISTO_A3 ) {
          mDData.updateDeviceModel( device.mAddress, "DistoX" );
          device.mType = model;
        } else if ( model == Device.DISTO_X310 ) {
          mDData.updateDeviceModel( device.mAddress, "DistoX-0000" );
          device.mType = model;
        } else if ( model == Device.DISTO_X000 ) {
          mDData.updateDeviceModel( device.mAddress, "DistoX0" );
          device.mType = model;
        }
      }
    }
  }

  static void setDeviceName( Device device, String nickname )
  {
    if ( device != null /* && device == TDInstance.device */ ) {
      mDData.updateDeviceNickname( device.mAddress, nickname );
      device.mNickname = nickname;
    }
  }

  // called by DeviceActivity::onResume()
  public void resumeComm() { if ( mComm != null ) mComm.resume(); }

  public void suspendComm() { if ( mComm != null ) mComm.suspend(); }

  public void resetComm() 
  { 
    createComm();
    mDataDownloader.onStop(); // mDownload = false;
  }

  // FIXME BT_RECEIVER 
  // void resetCommBTReceiver()
  // {
  //   mComm.resetBTReceiver();
  // }

  // called by DeviceActivity::setState()
  //           ShotWindow::onResume()
  public boolean isCommConnected()
  {
    // return mComm != null && mComm.mBTConnected;
    return mComm != null && mComm.mBTConnected && mComm.mRfcommThread != null;
  }

  void disconnectRemoteDevice( boolean force )
  {
    // TDLog.Log( TDLog.LOG_COMM, "App disconnect RemoteDevice listers " + mListerSet.size() + " force " + force );
    if ( force || mListerSet.size() == 0 ) {
      if ( mComm != null && mComm.mBTConnected ) mComm.disconnectRemoteDevice( );
    }
  }

  // void connectRemoteDevice( String address )
  // {
  //   if ( mComm != null ) mComm.connectRemoteDevice( address, mListerSet );
  // }

  // FIXME_COMM
  public boolean connectDevice( String address ) 
  {
    return mComm != null && mComm.connectDevice( address, mListerSet ); // FIXME_LISTER
  }

  public void disconnectComm()
  {
    if ( mComm != null ) mComm.disconnect();
  }
  // end FIXME_COMM

  DeviceA3Info readDeviceA3Info( String address )
  {
    DeviceA3Info info = new DeviceA3Info();
    byte[] ret = readMemory( address, 0x8008 );
    if ( ret == null ) return null;
    info.mCode = String.format( getResources().getString( R.string.device_code ), MemoryOctet.toInt( ret[1], ret[0] ) );

    ret = readMemory( TDInstance.device.mAddress, 0x8000 );
    if ( ret == null ) return null;

    info.mAngle   = getResources().getString( 
                    (( ret[0] & 0x01 ) != 0)? R.string.device_status_angle_grad : R.string.device_status_angle_degree );
    info.mCompass = getResources().getString( 
                    (( ret[0] & 0x04 ) != 0)? R.string.device_status_compass_on : R.string.device_status_compass_off );
    info.mCalib   = getResources().getString(
                    (( ret[0] & 0x08 ) != 0)? R.string.device_status_calib : R.string.device_status_normal );
    info.mSilent  = getResources().getString(
                    (( ret[0] & 0x10 ) != 0)? R.string.device_status_silent_on : R.string.device_status_silent_off );
    resetComm();
    return info;
  }

  DeviceX310Info readDeviceX310Info( String address )
  {
    DeviceX310Info info = new DeviceX310Info();
    byte[] ret = readMemory( address, 0x8008 );
    if ( ret == null ) return null;
    info.mCode = String.format( getResources().getString( R.string.device_code ), MemoryOctet.toInt( ret[1], ret[0] ) );

    ret = readMemory( address, 0xe000 );
    if ( ret == null ) return null;
    info.mFirmware = String.format( getResources().getString( R.string.device_firmware ), ret[0], ret[1] );
    // int fw0 = ret[0]; // this is always 2
    int fw1 = ret[1]; // firmware 2.X

    ret = readMemory( address, 0xe004 );
    if ( ret == null ) return null;
    info.mHardware = String.format( getResources().getString( R.string.device_hardware ), ret[0], ret[1] );

    // ret = readMemory( address, 0xc044 );
    // if ( ret != null ) {
    //   Log.v("DistoX", "X310 info C044 " + String.format( getResources().getString( R.string.device_memory ), ret[0], ret[1] ) );
    // }

    resetComm();
    return info;
  }

  String readHeadTail( String address, byte[] command, int[] head_tail )
  {
    String ret = mComm.readHeadTail( address, command, head_tail );
    resetComm();
    return ret;
  }

  int swapHotBit( String address, int from, int to ) 
  {
    int ret = mComm.swapHotBit( address, from, to );
    resetComm();
    return ret;
  }

  static boolean mEnableZip = true;  // whether zip saving is enabled or must wait (locked by th2. saving thread)
  static boolean mSketches = false;  // whether to use 3D models

  // ---------------------------------------------------------

  // TDPrefHelper getPrefHelper() { return mPrefHlp; }

  void startupStep2()
  {
    // ***** LOG FRAMEWORK
    TDLog.loadLogPreferences( mPrefHlp );

    mData.compileStatements();

    PtCmapActivity.setMap( mPrefHlp.getString( "DISTOX_PT_CMAP", null ) );

    TDSetting.loadSecondaryPreferences( this, mPrefHlp );
    checkAutoPairing();

    // if ( TDLog.LOG_DEBUG ) {
    //   isTracing = true;
    //   Debug.startMethodTracing("DISTOX");
    // }

    // TDLog.Debug("ready");
  }

  private void createComm()
  {
    if ( mComm != null ) {
      mComm.disconnectRemoteDevice( );
      mComm = null;
    }
    if ( TDInstance.isDeviceAddress( Device.ZERO_ADDRESS ) ) {
      mComm = new VirtualDistoXComm( this, mVirtualDistoX );
    } else { 
      mComm = new DistoXComm( this );
    }
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    TDInstance.setContext( getApplicationContext() );

    // require large memory pre Honeycomb
    // dalvik.system.VMRuntime.getRuntime().setMinimumHeapSize( 64<<20 );

    // TDLog.Profile("TDApp onCreate");
    try {
      VERSION      = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName;
      VERSION_CODE = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionCode;
      int v = VERSION_CODE;
      MAJOR = v / 100000;    
      v -= MAJOR * 100000;
      MINOR = v /   1000;    
      v -= MINOR *   1000;
      SUB = v / 10;
    } catch ( NameNotFoundException e ) {
      // FIXME
      e.printStackTrace();
    }

    mPrefHlp = new TDPrefHelper( this, this );
    mWelcomeScreen = mPrefHlp.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true
    if ( mWelcomeScreen ) {
      setDefaultSocketType();
    }
    mSetupScreen = mPrefHlp.getBoolean( "DISTOX_SETUP_SCREEN", true ); // default: SetupScreen = true

    mCheckPerms = FeatureChecker.checkPermissions( this );

    if ( mCheckPerms >= 0 ) {
      // TDLog.Profile("TDApp paths");
      TDPath.setDefaultPaths();

      // TDLog.Profile("TDApp cwd");
      TDInstance.cwd = mPrefHlp.getString( "DISTOX_CWD", "TopoDroid" );
      TDInstance.cbd = mPrefHlp.getString( "DISTOX_CBD", TDPath.PATH_BASEDIR );
      TDPath.setPaths( TDInstance.cwd, TDInstance.cbd );

      // TDLog.Profile("TDApp DB");
      mDataListeners = new DataListenerSet( );
      
      // ***** DATABASE MUST COME BEFORE PREFERENCES
      mData  = new DataHelper( this, this, mDataListeners );
      mDData = new DeviceHelper( this, this, null ); 

      // mStationName = new StationName();

      // TDLog.Profile("TDApp prefs");
      // LOADING THE SETTINGS IS RATHER EXPENSIVE !!!
      TDSetting.loadPrimaryPreferences( this, mPrefHlp );

      // TDLog.Profile("TDApp BT");
      mBTAdapter = BluetoothAdapter.getDefaultAdapter();
      // if ( mBTAdapter == null ) {
      //   // TDToast.make( R.string.not_available );
      //   // finish(); // FIXME
      //   // return;
      // }
      // TDLog.Profile("TDApp comm");

      // Log.v("DistoX", "VD TDapp on create");
      // createComm();

      mListerSet = new ListerSetHandler();
      mDataDownloader = new DataDownloader( this, this );

      mEnableZip = true;

      // ***** DRAWING TOOLS SYMBOLS
      // TDLog.Profile("TDApp symbols");

      // if one of the symbol dirs does not exists all of then are restored
      String version = mDData.getValue( "version" );
      if ( version == null || ( ! version.equals(VERSION) ) ) {
        mDData.setValue( "version", VERSION );
        // FIXME MANUAL installManual( );  // must come before installSymbols
        // FIXME INSTALL_SYMBOL installSymbols( false ); // this updates symbol_version in the database
        if ( mDData.getValue( "symbol_version" ) == null ) installSymbols( true );
        installFirmware( false );
        // installUserManual( );
      }

      // ***** CHECK SPECIAL EXPERIMENTAL FEATURES
      if ( TDLevel.overTester ) {
        String value = mDData.getValue("sketches");
        mSketches =  value != null 
                && value.equals("on")
                && getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
      }

      if ( TDLevel.overExpert ) {
        String value = mDData.getValue("cosurvey");
        mCosurvey =  value != null && value.equals("on");
        setCoSurvey( false );
        mPrefHlp.update( "DISTOX_COSURVEY", false );
        if ( mCosurvey ) {
          mSyncConn = new ConnectionHandler( this );
          mConnListener = new ArrayList<>();
        }
      }

      // TDLog.Profile("TDApp device etc.");
      TDInstance.device = mDData.getDevice( mPrefHlp.getString( TDSetting.keyDeviceName(), EMPTY ) );

      if ( TDInstance.device != null ) {
        createComm();
      }
      mHighlighted = null;
    }

    DistoXConnectionError = new String[5];
    DistoXConnectionError[0] = getResources().getString( R.string.distox_err_ok );
    DistoXConnectionError[1] = getResources().getString( R.string.distox_err_headtail );
    DistoXConnectionError[2] = getResources().getString( R.string.distox_err_headtail_io );
    DistoXConnectionError[3] = getResources().getString( R.string.distox_err_headtail_eof );
    DistoXConnectionError[4] = getResources().getString( R.string.distox_err_connected );

    DisplayMetrics dm = getResources().getDisplayMetrics();
    float density  = dm.density;
    mDisplayWidth  = dm.widthPixels;
    mDisplayHeight = dm.heightPixels;
    mScaleFactor   = (mDisplayHeight / 320.0f) * density;
    // FIXME it would be nice to have this, but it breaks all existing sketches
    //       therefore must stick with initial choice
    // DrawingUtil.CENTER_X = mDisplayWidth  / 2;
    // DrawingUtil.CENTER_Y = mDisplayHeight / 2;


    // mManual = getResources().getString( R.string.topodroid_man );
  }

  static void resetLocale()
  {
    // Log.v("DistoX", "reset locale to " + mLocaleStr );
    // mLocale = (mLocaleStr.equals(EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    Resources res = TDInstance.context.getResources();
    DisplayMetrics dm = res.getDisplayMetrics();
    Configuration conf = res.getConfiguration();
    conf.locale = mLocale; // setLocale API-17
    res.updateConfiguration( conf, dm );
  }

  static void setLocale( String locale, boolean load_symbols )
  {
    mLocaleStr = locale;
    mLocale = (mLocaleStr.equals(EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    // Log.v("DistoXPref", "set locale str <" + locale + "> " + mLocale.toString() );
    resetLocale();
    Resources res = TDInstance.context.getResources();
    if ( load_symbols ) {
      BrushManager.reloadPointLibrary( TDInstance.context, res ); // reload symbols
      BrushManager.reloadLineLibrary( res );
      BrushManager.reloadAreaLibrary( res );
    }
    if ( mActivity != null ) mActivity.setMenuAdapter( res );
    if ( TDPrefActivity.mPrefActivityAll != null ) TDPrefActivity.mPrefActivityAll.reloadPreferences();
  }

  static void setCWD( String cwd, String cbd )
  {
    if ( cwd == null || cwd.length() == 0 ) cwd = TDInstance.cwd;
    if ( cbd == null || cbd.length() == 0 ) cbd = TDInstance.cbd;
    if ( cbd.equals( TDInstance.cbd ) && cwd.equals( TDInstance.cwd ) ) return;
    TDLog.Log( TDLog.LOG_PATH, "set cwd " + cwd + " " + cbd );
    mData.closeDatabase();
    TDInstance.cbd = cbd;
    TDInstance.cwd = cwd;
    TDPath.setPaths( TDInstance.cwd, TDInstance.cbd );
    mData.openDatabase();
    if ( mActivity != null ) mActivity.setTheTitle( );
  }

// -----------------------------------------------------------------

  // called by GMActivity and by CalibCoeffDialog 
  void uploadCalibCoeff( Context context, byte[] coeff, boolean check, Button b )
  {
    if ( b != null ) b.setEnabled( false );
    if ( mComm == null || TDInstance.device == null ) {
      TDToast.make( R.string.no_device_address );
    } else if ( check && ! checkCalibrationDeviceMatch() ) {
      TDToast.make( R.string.calib_device_mismatch );
    } else if ( ! mComm.writeCoeff( TDInstance.distoAddress(), coeff ) ) {
      TDToast.make( R.string.write_failed );
    } else {
      TDToast.make( R.string.write_ok );
    }
    if ( b != null ) b.setEnabled( true );
    resetComm();
  }

  // called by CalibReadTask.onPostExecute
  boolean readCalibCoeff( byte[] coeff )
  {
    if ( mComm == null || TDInstance.device == null ) return false;
    boolean ret = mComm.readCoeff( TDInstance.device.mAddress, coeff );
    resetComm();
    return ret;
  }

  // called by CalibToggleTask.doInBackground
  boolean toggleCalibMode( )
  {
    if ( mComm == null || TDInstance.device == null ) return false;
    boolean ret = mComm.toggleCalibMode( TDInstance.device.mAddress, TDInstance.device.mType );
    resetComm();
    return ret;
  }

  byte[] readMemory( String address, int addr )
  {
    if ( mComm == null || isCommConnected() ) return null;
    byte[] ret = mComm.readMemory( address, addr );
    resetComm();
    return ret;
  }

  int readX310Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    int ret = mComm.readX310Memory( address, h0, h1, memory );
    resetComm();
    return ret;
  }

  int readA3Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    int ret = mComm.readA3Memory( address, h0, h1, memory );
    resetComm();
    return ret;
  }

  // ------------------------------------------------------------------
  // FILE NAMES

  // public static String getSqlFile() { return APP_BASE_PATH + "survey.sql"; }

  // public static String getManifestFile() { return APP_BASE_PATH + "manifest"; }

  public void writeManifestFile()
  {
    SurveyInfo info = mData.selectSurveyInfo( TDInstance.sid );
    try {
      String filename = TDPath.getManifestFile();
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s\n", VERSION );
      pw.format( "%s\n", DataHelper.DB_VERSION );
      pw.format( "%s\n", info.name );
      pw.format("%s\n", TopoDroidUtil.currentDate() );
      fw.flush();
      fw.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error("manifest write failure: no file");
    } catch ( IOException e ) {
      TDLog.Error("manifest write failure: " + e.getMessage() );
    }
  }

  int mManifestDbVersion = 0;

  // returns
  //  0 ok
  // -1 survey already present
  // -2 TopoDroid version mismatch
  // -3 database version mismatch
  // -4 survey name does not match filename
  public int checkManifestFile( String filename, String surveyname )
  {
    mManifestDbVersion = 0;
    String line;
    if ( mData.hasSurveyName( surveyname ) ) {
      return -1;
    }
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      // first line is version
      line = br.readLine().trim();
      String[] ver = line.split("\\.");
      int major = 0;
      int minor = 0;
      try {
        major = Integer.parseInt( ver[0] );
        minor = Integer.parseInt( ver[1] );
      } catch ( NumberFormatException e ) {
        TDLog.Error( "parse error: major/minor " + ver[0] + " " + ver[1] );
      }
      int sub   = 0;
      int k = 0;
      while ( k < ver[2].length() ) {
        char ch = ver[2].charAt(k);
        if ( ch < '0' || ch > '9' ) break;
        sub = 10 * sub + (int)(ch - '0');
        ++k;
      }
      // Log.v( "DistoX", "Version " + major + " " + minor + " " + sub );
      if (    ( major < MAJOR_MIN )
           || ( major == MAJOR_MIN && minor < MINOR_MIN )
           || ( major == MAJOR_MIN && minor == MINOR_MIN && sub < SUB_MIN ) ) {
        TDLog.Log( TDLog.LOG_ZIP, "TopDroid version mismatch: found " + line + " expected " + VERSION );
        return -2;
      }
      line = br.readLine().trim();
      try {
        mManifestDbVersion = Integer.parseInt( line );
      } catch ( NumberFormatException e ) {
        TDLog.Error( "parse error: db version " + line );
      }
      
      if ( ! (    mManifestDbVersion >= DataHelper.DATABASE_VERSION_MIN
               && mManifestDbVersion <= DataHelper.DATABASE_VERSION ) ) {
        TDLog.Log( TDLog.LOG_ZIP,
                          "TopDroid DB version mismatch: found " + mManifestDbVersion + " expected " + 
                          + DataHelper.DATABASE_VERSION_MIN + "-" + DataHelper.DATABASE_VERSION );
        return -3;
      }
      line = br.readLine().trim();
      if ( ! line.equals( surveyname ) ) return -4;
      fr.close();
    } catch ( NumberFormatException e ) {
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
    return 0;
  }


  // ----------------------------------------------------------
  // SURVEY AND CALIBRATION

  boolean renameCurrentSurvey( long sid, String name, boolean forward )
  {
    if ( name == null || name.length() == 0 ) return false;
    if ( name.equals( TDInstance.survey ) ) return true;
    if ( mData == null ) return false;
    if ( mData.renameSurvey( sid, name, forward ) ) {  
      File old = null;
      File nev = null;
      { // rename plot/sketch files: th3
        List< PlotInfo > plots = mData.selectAllPlots( sid );
        for ( PlotInfo p : plots ) {
          // Therion
          TopoDroidUtil.renameFile( TDPath.getSurveyPlotTh2File( TDInstance.survey, p.name ), TDPath.getSurveyPlotTh2File( name, p.name ) );
          // Tdr
          TopoDroidUtil.renameFile( TDPath.getSurveyPlotTdrFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTdrFile( name, p.name ) );
          // rename exported plots: dxf png svg csx
          TopoDroidUtil.renameFile( TDPath.getSurveyPlotDxfFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotDxfFile( name, p.name ) );
          TopoDroidUtil.renameFile( TDPath.getSurveyPlotSvgFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotSvgFile( name, p.name ) );
          // TopoDroidUtil.renameFile( TDPath.getSurveyPlotHtmFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotHtmFile( name, p.name ) );
          TopoDroidUtil.renameFile( TDPath.getSurveyPlotPngFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotPngFile( name, p.name ) );
          TopoDroidUtil.renameFile( TDPath.getSurveyPlotCsxFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotCsxFile( name, p.name ) );
        }
      }
      { // rename sketch files: th3
        List< Sketch3dInfo > sketches = mData.selectAllSketches( sid );
        for ( Sketch3dInfo s : sketches ) {
          TopoDroidUtil.renameFile( TDPath.getSurveySketchOutFile( TDInstance.survey, s.name ), TDPath.getSurveySketchOutFile( name, s.name ) );
        }
      }
      // rename exported files: csv csx dat dxf kml plt srv svx th top tro 
        TopoDroidUtil.renameFile( TDPath.getSurveyThFile( TDInstance.survey ), TDPath.getSurveyThFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyCsvFile( TDInstance.survey ), TDPath.getSurveyCsvFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyCsxFile( TDInstance.survey ), TDPath.getSurveyCsxFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyCaveFile( TDInstance.survey ), TDPath.getSurveyCaveFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyDatFile( TDInstance.survey ), TDPath.getSurveyDatFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyDxfFile( TDInstance.survey ), TDPath.getSurveyDxfFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyKmlFile( TDInstance.survey ), TDPath.getSurveyKmlFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyJsonFile( TDInstance.survey ), TDPath.getSurveyJsonFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyPltFile( TDInstance.survey ), TDPath.getSurveyPltFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveySrvFile( TDInstance.survey ), TDPath.getSurveySrvFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveySvxFile( TDInstance.survey ), TDPath.getSurveySvxFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyTopFile( TDInstance.survey ), TDPath.getSurveyTopFile( name ) );
        TopoDroidUtil.renameFile( TDPath.getSurveyTroFile( TDInstance.survey ), TDPath.getSurveyTroFile( name ) );

      { // rename note file: note
        TopoDroidUtil.renameFile( TDPath.getSurveyNoteFile( TDInstance.survey ), TDPath.getSurveyNoteFile( name ) );
      }
      { // rename photo folder: photo
        TopoDroidUtil.renameFile( TDPath.getSurveyPhotoDir( TDInstance.survey ), TDPath.getSurveyPhotoDir( name ) );
      }
      TDInstance.survey = name;
      return true;
    }
    return false;
  }
    
  /**
   * @param name      survey name
   * @param datamode  survey datamode
   */
  long setSurveyFromName( String name, int datamode, boolean update, boolean forward )
  { 
    TDInstance.sid      = -1;       // no survey by default
    TDInstance.survey   = null;
    TDInstance.datamode = 0;
    StationName.clearCurrentStation();
    // resetManualCalibrations();
    ManualCalibration.reset();

    if ( name != null && mData != null ) {
      // Log.v( "DistoX", "set SurveyFromName <" + name + "> forward " + forward );

      TDInstance.sid = mData.setSurvey( name, datamode, forward );
      // mFixed.clear();
      TDInstance.survey = null;
      if ( TDInstance.sid > 0 ) {
        DistoXStationName.setInitialStation( mData.getSurveyInitStation( TDInstance.sid ) );
        TDInstance.survey = name;
	TDInstance.datamode = mData.getSurveyDataMode( TDInstance.sid );
	// Log.v("DistoX", "set survey from name: <" + name + "> datamode " + datamode + " " + TDInstance.datamode );
        TDInstance.secondLastShotId = lastShotId();
        // restoreFixed();
	if ( update ) {
          if ( mShotWindow != null) {
            mShotWindow.setTheTitle();
            mShotWindow.updateDisplay();
          }
          if ( mSurveyWindow != null ) {
            mSurveyWindow.setTheTitle();
            mSurveyWindow.updateDisplay();
          }
	}
        TDInstance.xsections = ( SurveyInfo.XSECTION_SHARED == mData.getSurveyXSections( TDInstance.sid ) );
      }
      return TDInstance.sid;
    }
    return 0;
  }

  boolean hasSurveyName( String name )
  {
    return ( mData == null ) || mData.hasSurveyName( name );
  }

  boolean hasCalibName( String name )
  {
    return ( mDData == null ) || mDData.hasCalibName( name );
  }

  void /*long*/ setCalibFromName( String calib ) // RETURN value not used
  {
    TDInstance.cid = -1;
    TDInstance.calib = null;
    if ( calib != null && mDData != null ) {
      TDInstance.cid = mDData.setCalib( calib );
      TDInstance.calib = (TDInstance.cid > 0)? calib : null;
      // return TDInstance.cid;
    }
    // return 0;
  }

  // -----------------------------------------------------------------
  // PREFERENCES

  private void setDefaultSocketType()
  {
    String defaultSockType = ( android.os.Build.MANUFACTURER.equals("samsung") ) ? "1" : "0";
    mPrefHlp.update( "DISTOX_SOCK_TYPE", defaultSockType ); 
  }

  void setCWDPreference( String cwd, String cbd )
  { 
    if ( TDInstance.cwd.equals( cwd ) && TDInstance.cbd.equals( cbd ) ) return;
    // Log.v("DistoX", "setCWDPreference " + cwd );
    if ( mPrefHlp != null ) {
      mPrefHlp.update( "DISTOX_CWD", cwd, "DISTOX_CBD", cbd ); 
    }
    setCWD( cwd, cbd ); 
  }

  void setPtCmapPreference( String cmap )
  {
    if ( mPrefHlp != null ) {
      mPrefHlp.update( "DISTOX_PT_CMAP", cmap ); 
    }
    PtCmapActivity.setMap( cmap );
  }

  // unused
  // void setAccuracyPreference( float acceleration, float magnetic, float dip )
  // {
  //   mPrefHlp.update( "DISTOX_ACCEL_THR", Float.toString( acceleration ), "DISTOX_MAG_THR", Float.toString( magnetic ), "DISTOX_DIP_THR", Float.toString( dip ) ); 
  // }

  void setTextSize( int ts )
  {
    TDSetting.setTextSize( ts );
    if ( TDSetting.setLabelSize( ts*3, false ) || TDSetting.setStationSize( ts*2, false ) ) { // false: do not update brush
      BrushManager.setTextSizes( );
    }
    mPrefHlp.update( "DISTOX_TEXT_SIZE", Integer.toString(ts), "DISTOX_LABEL_SIZE", Float.toString(ts*3), "DISTOX_STATION_SIZE", Float.toString(ts*2) );
  }

  void setButtonSize( int bs )
  {
    TDSetting.setSizeButtons( bs );
    mPrefHlp.update( "DISTOX_SIZE_BUTTONS", Integer.toString(bs) );
  }

  void setDrawingUnit( float u )
  {
    TDSetting.setDrawingUnits( u );
    mPrefHlp.update( "DISTOX_DRAWING_UNIT", Float.toString(u) );
  }

  // used for "DISTOX_WELCOME_SCREEN" and "DISTOX_TD_SYMBOL"
  void setBooleanPreference( String preference, boolean val ) { mPrefHlp.update( preference, val ); }

  // FIXME_DEVICE_STATIC
  void setDevice( String address ) 
  { 
    // Log.v("DistoX", "VD TDapp set device address " + address );
    if ( address == null ) {
      if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this );
      TDInstance.device = null;
      address = EMPTY;
    } else if ( address.equals( Device.ZERO_ADDRESS )  ) {
      if ( mVirtualDistoX != null ) mVirtualDistoX.startServer( this );
      // boolean create = ( TDInstance.device == null || ! address.equals( TDInstance.device.mAddress ) );
      boolean create = ( ! TDInstance.isDeviceAddress( address ) );
      TDInstance.device = new Device( address, "DistoX0", "X000", null );
      if ( create ) createComm();
    } else {
      if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this );
      // boolean create = ( TDInstance.device == null || TDInstance.device.mAddress.equals( Device.ZERO_ADDRESS ) );
      boolean create = TDInstance.isDeviceZeroAddress();
      TDInstance.device = mDData.getDevice( address );
      if ( create ) createComm();
    }
    if ( mPrefHlp != null ) {
      mPrefHlp.update( TDSetting.keyDeviceName(), address ); 
    }
  }

  // -------------------------------------------------------------
  // DATA BATCH DOWNLOAD

  int downloadDataBatch( Handler /* ILister */ lister ) // FIXME_LISTER
  {
    TDInstance.secondLastShotId = lastShotId();
    int ret = 0;
    if ( mComm == null || TDInstance.device == null ) {
      TDLog.Error( "Comm or Device null ");
    } else {
      TDLog.Log( TDLog.LOG_DATA, "Download Data Batch() device " + TDInstance.device + " comm " + mComm.toString() );
      ret = mComm.downloadData( TDInstance.device.mAddress, lister );
      // FIXME BATCH
      // if ( ret > 0 && TDSetting.mSurveyStations > 0 ) {
      //   // FIXME TODO select only shots after the last leg shots
      //   List<DBlock> list = mData.selectAllShots( TDInstance.sid, TDStataus.NORMAL );
      //   assign Stations( list );
      // }
    }
    return ret;
  }

  // =======================================================
  // StationName mStationName;

  // void resetCurrentStationName( String name ) { StationName.resetCurrentStationName( name ); }
  boolean setCurrentStationName( String name ) { return StationName.setCurrentStationName( name ); }
  String getCurrentStationName() { return StationName.getCurrentStationName(); }
  boolean isCurrentStationName( String name ) { return StationName.isCurrentStationName( name ); }
  // void clearCurrentStation() { StationName.clearCurrentStation(); }
  String getCurrentOrLastStation( ) { return StationName.getCurrentOrLastStation( mData, TDInstance.sid); }
  String getFirstStation( ) { return StationName.getFirstStation( mData, TDInstance.sid); }
  private void resetCurrentOrLastStation( ) { StationName.resetCurrentOrLastStation( mData, TDInstance.sid); }

  // static long trobotmillis = 0L; // TROBOT_MILLIS

  // called also by ShotWindow::updataBlockList
  // this re-assign stations to shots with station(s) already set
  // the list of stations is ordered by compare
  //
  // @param list list of shot to assign
  void assignStationsAfter( DBlock blk0, List<DBlock> list )
  { 
    Set<String> sts = mData.selectAllStationsBefore( blk0.mId, TDInstance.sid, TDStatus.NORMAL  );
    // Log.v("DistoX", "assign stations after " + blk0.Name() + " size " + list.size() + " stations " + sts.size() );
    // if ( TDSetting.mSurveyStations < 0 ) return;
    StationName.clearCurrentStation();
    if ( StationPolicy.doTopoRobot() ) {
      // long millis = SystemClock.uptimeMillis(); // TROBOT_MILLIS
      // if ( millis > trobotmillis + 10000 ) {
      //   TDToast.make( R.string.toporobot_warning );
      //   trobotmillis = millis;
      // }
      StationName.assignStationsAfter_TRobot( mData, TDInstance.sid, blk0, list, sts );
    } else  if ( StationPolicy.doBacksight() ) {
      StationName.assignStationsAfter_Backsight( mData, TDInstance.sid, blk0, list, sts );
    } else if ( StationPolicy.doTripod() ) {
      StationName.assignStationsAfter_Tripod( mData, TDInstance.sid, blk0, list, sts );
    } else {
      StationName.assignStationsAfter_Default( mData, TDInstance.sid, blk0, list, sts );
    }
  }

  // called also by ShotWindow::updataBlockList
  // @param list blocks whose stations need to be set in the DB
  //
  void assignStationsAll(  List<DBlock> list )
  { 
    Set<String> sts = mData.selectAllStations( TDInstance.sid );
    // Log.v("DistoX", "assign stations size " + list.size() );
    // if ( TDSetting.mSurveyStations < 0 ) return;
    if ( StationPolicy.doTopoRobot() ) {
      // long millis = SystemClock.uptimeMillis(); // TROBOT_MILLIS
      // if ( millis > trobotmillis + 10000 ) {
      //   TDToast.make( R.string.toporobot_warning );
      //   trobotmillis = millis;
      // }
      StationName.assignStations_TRobot( mData, TDInstance.sid, list, sts );
      return;
    } 
    if ( StationPolicy.doBacksight() ) {
      StationName.assignStations_Backsight( mData, TDInstance.sid, list, sts );
      return;
    } 
    if ( StationPolicy.doTripod() ) {
      StationName.assignStations_Tripod( mData, TDInstance.sid, list, sts );
      return;
    }
    StationName.assignStations_Default( mData, TDInstance.sid, list, sts );
  }

  // ================================================================
  // EXPORTS

  static void exportSurveyAsCsxAsync( Context context, String origin, PlotSaveData psd1, PlotSaveData psd2, boolean toast )
  {
    SurveyInfo info = getSurveyInfo();
    if ( info == null ) {
      TDLog.Error("Error: null survey info");
      return;
    }
    String filename = ( psd1 == null )? TDPath.getSurveyCsxFile(TDInstance.survey)
                                      : TDPath.getSurveyCsxFile(TDInstance.survey, psd1.name /* = sketch.mName1 */ );
    TDLog.Log( TDLog.LOG_IO, "exporting as CSX " + filename );
    (new SaveFullFileTask( context, TDInstance.sid, mData, info, psd1, psd2, origin, filename, toast )).execute();
  }

  // FIXME_SYNC might be a problem with big surveys
  // this is called sync to pass the therion file to the 3D viewwer
  static boolean exportSurveyAsThSync( )
  {
    SurveyInfo info = getSurveyInfo();
    if ( info == null ) return false;
    // if ( async ) {
    //   String saving = context.getResources().getString(R.string.saving_);
    //   (new SaveDataFileTask( saving, TDInstance.sid, info, mData, TDInstance.survey, null, TDConst.DISTOX_EXPORT_TH, toast )).execute();
    //   return true;
    // }
    return ( TDExporter.exportSurveyAsTh( TDInstance.sid, mData, info, TDPath.getSurveyThFile( TDInstance.survey ) ) != null );
  }

  // FIXME_SYNC ok because calib files are small
  String exportCalibAsCsv( )
  {
    if ( TDInstance.cid < 0 ) return null;
    CalibInfo ci = mDData.selectCalibInfo( TDInstance.cid );
    if ( ci == null ) return null;
    TDPath.checkCCsvDir();
    String filename = TDPath.getCCsvFile( ci.name );
    return TDExporter.exportCalibAsCsv( TDInstance.cid, mDData, ci, filename );
  }

  // ----------------------------------------------
  // FIRMWARE and USER MANUAL

  private void installFirmware( boolean overwrite )
  {
    InputStream is = getResources().openRawResource( R.raw.firmware );
    firmwareUncompress( is, overwrite );
    try { is.close(); } catch ( IOException e ) { }
  }
 
  // static private void installUserManual()
  // {
  //   InputStream is = getResources().openRawResource( R.raw.manual ); // res/raw/manual.zip
  //   userManualUncompress( is );
  //   try { is.close(); } catch ( IOException e ) { }
  // }
 
  // -------------------------------------------------------------
  // SYMBOLS

  // FIXME INSTALL_SYMBOL
  // void installSymbols( boolean overwrite )
  // {
  //   boolean install = overwrite;
  //   askSymbolUpdate = false;
  //   if ( ! overwrite ) { // check whether to install
  //     String version = mDData.getValue( "symbol_version" );
  //     // Log.v("DistoX", "symbol version <" + version + "> SYMBOL_VERSION <" + SYMBOL_VERSION + ">" );
  //     if ( version == null ) {
  //       install = true;
  //     } else if ( ! version.equals(SYMBOL_VERSION) ) {
  //       askSymbolUpdate = true;
  //     } else { // version .equals SYMBOL_VERSION
  //       return;
  //     }
  //   }
  //   if ( install ) {
  //     deleteObsoleteSymbols();
  //     InputStream is = getResources().openRawResource( R.raw.symbols_speleo );
  //     symbolsUncompress( is, overwrite );
  //   }
  //   mDData.setValue( "symbol_version", SYMBOL_VERSION );
  // }

  void installSymbols( boolean overwrite )
  {
    deleteObsoleteSymbols();
    installSymbols( R.raw.symbols_speleo, overwrite );
    mDData.setValue( "symbol_version", SYMBOL_VERSION );
  }

  void installSymbols( int res, boolean overwrite )
  {
    InputStream is = getResources().openRawResource( res );
    symbolsUncompress( is, overwrite );
  }

  static private void deleteObsoleteSymbols()
  {
    String lines[] = { "blocks", "debris", "clay", "presumed", "sand", "ice" };
    for ( String line : lines ) {
      TopoDroidUtil.deleteFile( TDPath.APP_LINE_PATH + line );
    }
  }

  private void clearSymbolsDir( String dirname )
  {
    // Log.v("DistoX", "clear " + dirname );
    File dir = new File( dirname );
    File [] files = dir.listFiles();
    if ( files == null ) return;
    for ( int i=0; i<files.length; ++i ) {
      if ( files[i].isDirectory() ) continue;
      if ( ! files[i].delete() ) TDLog.Error("File delete failed ");
    }
  }
    
  private void clearSymbols( )
  {
    clearSymbolsDir( TDPath.APP_POINT_PATH );
    clearSymbolsDir( TDPath.APP_LINE_PATH );
    clearSymbolsDir( TDPath.APP_AREA_PATH );
  }  

  void reloadSymbols( boolean clear, 
                      boolean speleo, boolean extra, boolean mine, boolean geo, boolean archeo, boolean paleo,
                      boolean bio,    boolean karst )
  {
    // Log.v("DistoX", "Reload symbols " + speleo + " " + mine + " " + geo + " " + archeo + " " + paleo + " " + bio + " clear " + clear );
    if ( extra ) speleo = true; // extra implies speleo

    if ( clear ) {
      if (speleo || mine || geo || archeo || paleo || bio ) { 
        clearSymbols();
      }
    }
    if ( speleo ) installSymbols( R.raw.symbols_speleo, true );
    if ( extra  ) installSymbols( R.raw.symbols_extra,  true );
    if ( mine   ) installSymbols( R.raw.symbols_mine,   true );
    if ( geo    ) installSymbols( R.raw.symbols_geo,    true );
    if ( archeo ) installSymbols( R.raw.symbols_archeo, true );
    if ( paleo  ) installSymbols( R.raw.symbols_paleo,  true );
    if ( bio    ) installSymbols( R.raw.symbols_bio,    true );
    if ( karst  ) installSymbols( R.raw.symbols_karst,  true );

    mDData.setValue( "symbol_version", SYMBOL_VERSION );
    BrushManager.reloadAllLibraries( this, getResources() );
    // BrushManager.makePaths( getResources() );
  }

  static private void symbolsUncompress( InputStream fis, boolean overwrite )
  {
    TDPath.symbolsCheckDirs();
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( filepath.endsWith("README") ) continue;
        if ( ! ze.isDirectory() ) {
          if ( filepath.startsWith( "symbol" ) ) {
            int pos  = 1 + filepath.indexOf('/');
            filepath = filepath.substring( pos );
          }
          String pathname = TDPath.getSymbolFile( filepath );
          File file = new File( pathname );
          if ( overwrite || ! file.exists() ) {
            // APP_SAVE SYMBOLS
            if ( file.exists() ) {
              if ( ! file.renameTo( new File( TDPath.getSymbolSaveFile( filepath ) ) ) ) TDLog.Error("File rename error");
            }

            TDPath.checkPath( pathname );
            FileOutputStream fout = new FileOutputStream( pathname );
            int c;
            while ( ( c = zin.read( buffer ) ) != -1 ) {
              fout.write(buffer, 0, c); // offset 0 in buffer
            }
            fout.close();
          
            // pathname =  APP_SYMBOL_SAVE_PATH + filepath;
            // file = new File( pathname );
            // if ( ! file.exists() ) {
            //   TDPath.checkPath( pathname );
            //   FileOutputStream fout = new FileOutputStream( pathname );
            //   int c;
            //   while ( ( c = zin.read( buffer ) ) != -1 ) {
            //     fout.write(buffer, 0, c); // offset 0 in buffer
            //   }
            //   fout.close();
            // }
          }
        }
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
  }

  static private void firmwareUncompress( InputStream fis, boolean overwrite )
  {
    // Log.v(TAG, "firmware uncompress ...");
    TDPath.checkBinDir( );
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( ze.isDirectory() ) continue;
        if ( ! filepath.endsWith("bin") ) continue;
        String pathname =  TDPath.getBinFile( filepath );
        File file = new File( pathname );
        if ( overwrite || ! file.exists() ) {
          TDPath.checkPath( pathname );
          FileOutputStream fout = new FileOutputStream( pathname );
          int c;
          while ( ( c = zin.read( buffer ) ) != -1 ) {
            fout.write(buffer, 0, c); // offset 0 in buffer
          }
          fout.close();
        }
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
  }

  // ---------------------------------------------------------
  /** insert LRUD splays at a given station, before shot with id "at"
   * @param at       block id before which to insert the LRUD
   * @param splay_station station of the LRUD splay
   * @param bearing  block azimuth
   * @param clino    bock clino
   * @param left     LEFT length
   * @param right    RIGHT length
   * @param up       UP length
   * @param down     DOWN length
   */
  void insertLRUDatStation( long at, String splay_station, float bearing, float clino,
                            String left, String right, String up, String down )
  {
    // could return the long
    addManualSplays( at, splay_station, left, right, up, down, bearing, false ); // horizontal=false
  }

  /**
    * @param from     FROM station
    * @param to       TO station
    * @param distance user-input distance (current units)
    * @param bearing  from block
    * @param clino    from block
    * @param extend   ...
    * @return id of inserted leg
    * note before inserting the duplicate leg it set the CurrentStationName
    */
  long insertDuplicateLeg( String from, String to, float distance, float bearing, float clino, int extend )
  {
    resetCurrentOrLastStation( );
    long millis = java.lang.System.currentTimeMillis()/1000;
    distance = distance / TDSetting.mUnitLength;
    long id = mData.insertShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend, 0.0, LegType.NORMAL, 1, true );
    mData.updateShotName( id, TDInstance.sid, from, to, true ); // forward = true
    mData.updateShotFlag( id, TDInstance.sid, DBlock.FLAG_DUPLICATE, true ); // forward = true
    return id;
  }

  private long addManualSplays( long at, String splay_station, String left, String right, String up, String down,
                                float bearing, boolean horizontal )
  {
    long id;
    long millis = java.lang.System.currentTimeMillis()/1000;
    long extend = 0L;
    float calib = ManualCalibration.mLRUD ? ManualCalibration.mLength / TDSetting.mUnitLength : 0;
    float l = -1.0f;
    float r = -1.0f;
    float u = -1.0f;
    float d = -1.0f;
    if ( left != null && left.length() > 0 ) {
      try {
        l = Float.parseFloat( left ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: left " + left );
      }
      l -= calib;
    }  
    if ( right != null && right.length() > 0 ) {
      try {
        r = Float.parseFloat( right ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: right " + right );
      }
      r -= calib;
    }
    if ( up != null && up.length() > 0 ) {
      try {
        u = Float.parseFloat( up ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: up " + up );
      }
      u -= calib;
    }
    if ( down != null && down.length() > 0 ) {
      try {
        d = Float.parseFloat( down ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: down " + down );
      }
      d -= calib;
    }

    if ( l >= 0.0f ) {
      if ( horizontal ) { // WENS
        extend = TDAzimuth.computeSplayExtend( 270 );
        if ( at >= 0L ) {
          id = mData.insertShotAt( TDInstance.sid, at, millis, 0, l, 270.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1, true );
          ++at;
        } else {
          id = mData.insertShot( TDInstance.sid, -1L, millis, 0, l, 270.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1, true );
        }
      } else {
        float b = bearing - 90.0f;
        if ( b < 0.0f ) b += 360.0f;
        extend = TDAzimuth.computeSplayExtend( b );
        // b = in360( b );
        if ( at >= 0L ) {
          id = mData.insertShotAt( TDInstance.sid, at, millis, 0, l, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1, true );
          ++at;
        } else {
          id = mData.insertShot( TDInstance.sid, -1L, millis, 0, l, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1, true );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, EMPTY, true );
    }
    if ( r >= 0.0f ) {
      if ( horizontal ) { // WENS
        extend = TDAzimuth.computeSplayExtend( 90 );
        if ( at >= 0L ) {
          id = mData.insertShotAt( TDInstance.sid, at, millis, 0, r, 90.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1, true );
          ++at;
        } else {
          id = mData.insertShot( TDInstance.sid, -1L, millis, 0, r, 90.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1, true );
        }
      } else {
        float b = bearing + 90.0f;
        if ( b >= 360.0f ) b -= 360.0f;
        extend = TDAzimuth.computeSplayExtend( b );
        if ( at >= 0L ) {
          id = mData.insertShotAt( TDInstance.sid, at, millis, 0, r, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1, true );
          ++at;
        } else {
          id = mData.insertShot( TDInstance.sid, -1L, millis, 0, r, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1, true );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, EMPTY, true );
    }
    if ( u >= 0.0f ) {  
      if ( horizontal ) {
        if ( at >= 0L ) {
          id = mData.insertShotAt( TDInstance.sid, at, millis, 0, u, 0.0f, 0.0f, 0.0f, 0L, 0.0, LegType.XSPLAY, 1, true );
          ++at;
        } else {
          id = mData.insertShot( TDInstance.sid, -1L, millis, 0, u, 0.0f, 0.0f, 0.0f, 0L, 0.0, LegType.XSPLAY, 1, true );
        }
      } else {
        if ( at >= 0L ) {
          id = mData.insertShotAt( TDInstance.sid, at, millis, 0, u, 0.0f, 90.0f, 0.0f, 0L, 0.0, LegType.XSPLAY, 1, true );
          ++at;
        } else {
          id = mData.insertShot( TDInstance.sid, -1L, millis, 0, u, 0.0f, 90.0f, 0.0f, 0L, 0.0, LegType.XSPLAY, 1, true );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, EMPTY, true );
    }
    if ( d >= 0.0f ) {
      if ( horizontal ) {
        if ( at >= 0L ) {
          id = mData.insertShotAt( TDInstance.sid, at, millis, 0, d, 180.0f, 0.0f, 0.0f, 0L, 0.0, LegType.XSPLAY, 1, true );
          ++at;
        } else {
          id = mData.insertShot( TDInstance.sid, -1L, millis, 0, d, 180.0f, 0.0f, 0.0f, 0L, 0.0, LegType.XSPLAY, 1, true );
        }
      } else {
        if ( at >= 0L ) {
          id = mData.insertShotAt( TDInstance.sid, at, millis, 0, d, 0.0f, -90.0f, 0.0f, 0L, 0.0, LegType.XSPLAY, 1, true );
          ++at;
        } else {
          id = mData.insertShot( TDInstance.sid, -1L, millis, 0, d, 0.0f, -90.0f, 0.0f, 0L, 0.0, LegType.XSPLAY, 1, true );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, EMPTY, true );
    }
    return at;
  }

  /** insert manual-data shot
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   * 
   * NOTE manual shots take into account the instruents calibrations
   *      LRUD are not affected
   */
  DBlock insertManualShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend0, long flag0,
                           String left, String right, String up, String down,
                           String splay_station )
  {
    TDInstance.secondLastShotId = lastShotId();
    DBlock ret = null;
    long id;
    long millis = java.lang.System.currentTimeMillis()/1000;

    distance = (distance - ManualCalibration.mLength)  / TDSetting.mUnitLength;
    clino    = (clino    - ManualCalibration.mClino)   / TDSetting.mUnitAngle;
    float b  = bearing / TDSetting.mUnitAngle;


    if ( ( distance < 0.0f ) ||
         ( clino < -90.0f || clino > 90.0f ) ||
         ( b < 0.0f || b >= 360.0f ) ) {
      TDToast.make( R.string.illegal_data_value );
      return null;
    }
    bearing = (bearing  - ManualCalibration.mAzimuth) / TDSetting.mUnitAngle;
    while ( bearing >= 360 ) bearing -= 360;
    while ( bearing <    0 ) bearing += 360;

    if ( from != null && to != null && from.length() > 0 ) {
      // if ( mData.makesCycle( -1L, TDInstance.sid, from, to ) ) {
      //   TDToast.make( R.string.makes_cycle );
      // } else
      {
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > TDSetting.mVThreshold );
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot SID " + TDInstance.sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( StationPolicy.mShotAfterSplays ) {
          at = addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal );

          if ( at >= 0L ) {
            id = mData.insertShotAt( TDInstance.sid, at, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1, true );
          } else {
            id = mData.insertShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1, true );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, TDInstance.sid, from, to, true );
          // mData.updateShotExtend( id, TDInstance.sid, extend0, stretch0, true );
          // mData.updateShotExtend( id, TDInstance.sid, DBlock.EXTEND_IGNORE, 1, true ); // FIXME WHY ???
          // FIXME updateDisplay( );
        } else {
          if ( at >= 0L ) {
            id = mData.insertShotAt( TDInstance.sid, at, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1, true );
            ++ at;
          } else {
            id = mData.insertShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1, true );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, TDInstance.sid, from, to, true );
          // mData.updateShotExtend( id, TDInstance.sid, extend0, stretch0, true ); 
          // mData.updateShotExtend( id, TDInstance.sid, DBlock.EXTEND_IGNORE, 1, true );  // FIXME WHY ???
          // FIXME updateDisplay( );

          addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal );
        }
        ret = mData.selectShot( id, TDInstance.sid );
      }
    } else {
      TDToast.make( R.string.missing_station );
    }
    return ret;
  }

  int getCalibAlgoFromDB()
  {
    return mDData.selectCalibAlgo( TDInstance.cid );
  }

  void updateCalibAlgo( int algo ) 
  {
    mDData.updateCalibAlgo( TDInstance.cid, algo );
  }
  
  int getCalibAlgoFromDevice()
  {
    if ( TDInstance.device == null ) return CalibInfo.ALGO_LINEAR;
    if ( TDInstance.device.mType == Device.DISTO_A3 ) return CalibInfo.ALGO_LINEAR; // A3
    if ( TDInstance.device.mType == Device.DISTO_X310 ) {
      if ( mComm == null ) return 1; // should not happen
      byte[] ret = mComm.readMemory( TDInstance.device.mAddress, 0xe000 );
      if ( ret != null && ( ret[0] >= 2 && ret[1] >= 3 ) ) return CalibInfo.ALGO_NON_LINEAR;
    }
    return CalibInfo.ALGO_LINEAR; // default
  }  

  // --------------------------------------------------------

  void setX310Laser( int what, Handler /* ILister */ lister ) // 0: off, 1: on, 2: measure // FIXME_LISTER
  {
    if ( mComm == null || TDInstance.device == null ) return;
    mComm.setX310Laser( TDInstance.device.mAddress, what, lister );
  }

  // int readFirmwareHardware()
  // {
  //   return mComm.readFirmwareHardware( TDInstance.device.mAddress );
  // }

  int dumpFirmware( String filename )
  {
    if ( mComm == null || TDInstance.device == null ) return -1;
    return mComm.dumpFirmware( TDInstance.device.mAddress, TDPath.getBinFile(filename) );
  }

  int uploadFirmware( String filename )
  {
    if ( mComm == null || TDInstance.device == null ) {
      TDLog.Error( "Comm or Device null");
      return -1;
    }
    String pathname = TDPath.getBinFile( filename );
    TDLog.LogFile( "Firmware upload address " + TDInstance.device.mAddress );
    TDLog.LogFile( "Firmware upload file " + pathname );
    if ( ! pathname.endsWith( "bin" ) ) {
      TDLog.LogFile( "Firmware upload file does not end with \"bin\"");
      return 0;
    }
    return mComm.uploadFirmware( TDInstance.device.mAddress, pathname );
  }

  // ----------------------------------------------------------------------

  long insert2dPlot( long sid , String name, String start, boolean extended, int project )
  {
    // PlotInfo.ORIENTATION_PORTRAIT = 0
    // TDLog.Log( TDLog.LOG_PLOT, "new plot " + name + " start " + start );
    long pid_p = mData.insertPlot( sid, -1L, name+"p",
                 PlotInfo.PLOT_PLAN, 0L, start, EMPTY, 0, 0, mScaleFactor, 0, 0, EMPTY, EMPTY, 0, true );
    if ( extended ) {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotInfo.PLOT_EXTENDED, 0L, start, EMPTY, 0, 0, mScaleFactor, 0, 0, EMPTY, EMPTY, 0, true );
    } else {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotInfo.PLOT_PROFILE, 0L, start, EMPTY, 0, 0, mScaleFactor, project, 0, EMPTY, EMPTY, 0, true );
    }
    return pid_p;
  }
  
  // @param azimuth clino : projected profile azimuth / section plane direction 
  // @param parent parent plot name
  // NOTE field "hide" is overloaded for x_sections with the parent plot name
  long insert2dSection( long sid, String name, long type, String from, String to, float azimuth, float clino,
                        String parent, String nickname )
  {
    // FIXME COSURVEY 2d sections are not forwarded
    // 0 0 mScaleFactor : offset and zoom
    String hide = ( parent == null )? EMPTY : parent;
    String nick = ( nickname == null )? EMPTY : nickname;
    return mData.insertPlot( sid, -1L, name, type, 0L, from, to, 0, 0, TopoDroidApp.mScaleFactor, azimuth, clino, hide, nick, 0, false );
  }

  // @param ctx       context
  // @prarm filename  photo filename
  // static void viewPhoto( Context ctx, String filename )
  // {
  //   // Log.v("DistoX", "photo <" + filename + ">" );
  //   File file = new File( filename );
  //   if ( file.exists() ) {
  //     // FIXME create a dialog like QCam that displays the JPEG file
  //     //
  //     // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + filename ) );
  //     if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.N ) {
  //       Intent intent = new Intent(Intent.ACTION_VIEW );
  //       intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
  //       intent.setDataAndType( Uri.fromFile( file ), "image/jpeg" ); // pre Nougat
  //       // } else {
  //       //   URI apkURI = FileProvider.getUriForFile( ctx, ctx.getApplicationContext().getPackageName() + ".provider", file );
  //       //   intent.setDataAndType( apkURI, "image/jpeg" );
  //       //   intent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
  //       try {
  //         ctx.startActivity( intent );
  //       } catch ( ActivityNotFoundException e ) {
  //         // gracefully fail without saying anything
  //       }
  //     } else {
  //       TDToast.make( "Photo display not yet implemented" );
  //     }
  //   } else {
  //     TDToast.make( "ERROR file not found: " + filename );
  //   }
  // }

  // ---------------------------------------------------------------------
  // SYNC (COSURVEY)
  
  // DataListener (co-surveying)
  private DataListenerSet mDataListeners;

  // synchronized( mDataListener )
  void registerDataListener( DataListener listener ) { mDataListeners.registerDataListener( listener ); }

  // synchronized( mDataListener )
  void unregisterDataListener( DataListener listener ) { mDataListeners.unregisterDataListener( listener ); }

  static boolean mCosurvey = false;       // whether co-survey is enable by the DB
  static boolean mCoSurveyServer = false; // whether co-survey server is on
  static ConnectionHandler mSyncConn = null;


  void setCoSurvey( boolean co_survey ) // FIXME interplay with TDSetting
  {
    if ( ! mCosurvey ) {
      mCoSurveyServer = false;
      mPrefHlp.update( "DISTOX_COSURVEY", false );
      return;
    } 
    mCoSurveyServer = co_survey;
    if ( mCoSurveyServer ) { // start server
      startRemoteTopoDroid( );
    } else { // stop server
      stopRemoteTopoDroid( );
    }
  }

  static int getConnectionType() 
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getType();
  }

  static int getAcceptState()
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getAcceptState();
  }

  static int getConnectState()
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getConnectState();
  }

  static String getConnectionStateStr()
  {
    return ( mSyncConn == null )? "NONE": mSyncConn.getConnectStateStr();
  }

  static String getConnectedDeviceName()
  {
    return ( mSyncConn == null )? null : mSyncConn.getConnectedDeviceName();
  }

  static String getConnectionStateTitleStr()
  {
    return ( mSyncConn == null )? EMPTY : mSyncConn.getConnectionStateTitleStr();
  }

  static void connStateChanged()
  {
    // Log.v( "DistoX", "connStateChanged()" );
    if ( mSurveyWindow != null ) mSurveyWindow.setTheTitle();
    if ( mShotWindow  != null) mShotWindow.setTheTitle();
    if ( mActivity != null ) mActivity.setTheTitle();
  }

  static void connectRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) mSyncConn.connect( device );
  }

  void disconnectRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) {
      unregisterDataListener( mSyncConn );
      mSyncConn.disconnect( device );
    }
  }

  static void syncRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) mSyncConn.syncDevice( device );
  }

  static void startRemoteTopoDroid( )
  { 
    if ( mSyncConn != null ) mSyncConn.start( );
  }

  void stopRemoteTopoDroid( )
  { 
    if ( mSyncConn != null ) {
      unregisterDataListener( mSyncConn );
      mSyncConn.stop( );
    }
  }

  static void syncConnectionFailed()
  {
    TDToast.make( "Sync connection failed" );
  }

  void syncConnectedDevice( String name )
  {
    TDToast.make( "Sync connected " + name );
    if ( mSyncConn != null ) registerDataListener( mSyncConn );
  }

  // --------------------------------------------------------------

  void refreshUI()
  {
    if ( mSurveyWindow != null ) mSurveyWindow.updateDisplay();
    if ( mShotWindow  != null) mShotWindow.updateDisplay();
    if ( mActivity != null ) mActivity.updateDisplay();
  }

  void clearSurveyReferences()
  {
    mSurveyWindow = null;
    mShotWindow   = null;
  }

  // ---------------------------------------------------------------
  // DISTOX PAIRING

  PairingRequest mPairingRequest = null;

  void checkAutoPairing()
  {
    if ( TDSetting.mAutoPair ) {
      startPairingRequest();
    } else {
      stopPairingRequest();
    }
  }

  void stopPairingRequest()
  {
    if ( mPairingRequest != null ) {
      // Log.v("DistoX", "stop pairing" );
      unregisterReceiver( mPairingRequest );
      mPairingRequest = null;
    }
  }

  void startPairingRequest()
  {
    if ( mPairingRequest == null ) {
      // Log.v("DistoX", "start pairing" );
      // IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_PAIRING_REQUEST );
      IntentFilter filter = new IntentFilter( "android.bluetooth.device.action.PAIRING_REQUEST" );
      // filter.addCategory( Intent.CATEGORY_ALTERNATIVE );
      mPairingRequest = new PairingRequest();
      registerReceiver( mPairingRequest, filter );
    }
  }

  // ==================================================================
  
  // called by ShotWindow and SurveyWindow on export
  static void doExportDataAsync( Context context, int exportType, boolean warn )
  {
    if ( exportType < 0 ) return;
    if ( TDInstance.sid < 0 ) {
      if ( warn ) TDToast.make( R.string.no_survey );
    } else {
      SurveyInfo info = getSurveyInfo( );
      if ( info == null ) return;
      TDLog.Log( TDLog.LOG_IO, "async-export survey " + TDInstance.survey + " type " + exportType );
      String saving = context.getResources().getString(R.string.saving_);
      (new SaveDataFileTask( saving, TDInstance.sid, info, mData, TDInstance.survey, TDInstance.device, exportType, true )).execute();
    }
  }

  // called by zip archiver
  static void doExportDataSync( int exportType )
  {
    if ( exportType < 0 ) return;
    if ( TDInstance.sid >= 0 ) {
      SurveyInfo info = getSurveyInfo( );
      if ( info == null ) return;
      TDLog.Log( TDLog.LOG_IO, "sync-export survey " + TDInstance.survey + " type " + exportType );
      // String saving = null; // because toast is false
      (new SaveDataFileTask( null, TDInstance.sid, info, mData, TDInstance.survey, TDInstance.device, exportType, false )).immed_exec();
    }
  }
}
