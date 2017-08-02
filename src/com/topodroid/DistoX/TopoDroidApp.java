/** @file TopoDroidApp.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid application (consts and prefs)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.util.Locale;

import java.util.List;
import java.util.ArrayList;
// import java.util.Stack;


import android.widget.ArrayAdapter;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Debug;
import android.os.SystemClock; // FIXME TROBOT

import android.app.Application;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Activity;

import android.preference.PreferenceManager;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.provider.Settings.System;
import android.provider.Settings.SettingNotFoundException;

import android.view.WindowManager;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.graphics.Point;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;

import android.util.Log;
import android.util.DisplayMetrics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.widget.Toast;

public class TopoDroidApp extends Application
                          implements OnSharedPreferenceChangeListener
{
  String mCWD;  // current work directory

  static final String EMPTY = "";

  static String SYMBOL_VERSION = "35";
  static String VERSION = "0.0.0"; 
  static int VERSION_CODE = 0;
  static int MAJOR = 0;
  static int MINOR = 0;
  static int SUB   = 0;
  static final int MAJOR_MIN = 2; // minimum compatible version
  static final int MINOR_MIN = 1;
  static final int SUB_MIN   = 1;
  
  boolean mWelcomeScreen;  // whether to show the welcome screen
  // static String mManual;  // manual url
  static Locale mLocale;
  static String mLocaleStr;

  static String mClipboardText = null; // text clipboard

  public static float mScaleFactor   = 1.0f;
  public static float mDisplayWidth  = 200f;
  public static float mDisplayHeight = 320f;
  static boolean mXSections = false;       // current value of mSharedXSections

  static boolean isTracing = false;

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

  DBlock mHighlightedSplay = null;
  void setHighlightedSplay( DBlock blk ) { mHighlightedSplay = blk; }
  long getHighlightedSplayId( ) { return (mHighlightedSplay == null)? -1 : mHighlightedSplay.mId; }
  int mSplayMode = 2; // cross-section splay display mode
  
  // ----------------------------------------------------------------------
  // data lister
  // ListerSet mListerSet;
  ListerSetHandler mListerSet; // FIXME LISTER

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


  // ----------------------------------------------------------------------
  // DataListener (co-surveying)
  private DataListenerSet mDataListeners;

  // synchronized( mDataListener )
  void registerDataListener( DataListener listener ) { mDataListeners.registerDataListener( listener ); }

  // synchronized( mDataListener )
  void unregisterDataListener( DataListener listener ) { mDataListeners.unregisterDataListener( listener ); }

  // -----------------------------------------------------

  private SharedPreferences mPrefs;
  // FIXME INSTALL_SYMBOL boolean askSymbolUpdate = false; // by default do not ask

  String[] DistoXConnectionError;
  BluetoothAdapter mBTAdapter = null;     // BT connection
  private TopoDroidComm mComm = null;        // BT communication
  DataDownloader mDataDownloader = null;  // data downloader
  static DataHelper mData = null;         // database 
  static DeviceHelper mDData = null;      // device/calib database

  static SurveyWindow mSurveyWindow = null;
  static ShotWindow mShotWindow     = null;
  // static DrawingWindow mDrawingWindow = null; // FIXME currently not used
  static MainWindow mActivity = null; 
  TopoDroidPreferences mPrefActivity = null;

  static boolean mDeviceActivityVisible = false;
  static boolean mGMActivityVisible = false;

  long mSID   = -1;   // id of the current survey
  long mCID   = -1;   // id of the current calib
  String mySurvey;   // current survey name
  String myCalib;    // current calib name
  static long mSecondLastShotId = 0L;

  public long lastShotId( ) { return mData.getLastShotId( mSID ); }
  public long secondLastShotId( ) { return mSecondLastShotId; }

  Device mDevice = null;
  int    distoType() { return (mDevice == null)? 0 : mDevice.mType; }
  String distoAddress() { return (mDevice == null)? null : mDevice.mAddress; }


  VirtualDistoX mVirtualDistoX = new VirtualDistoX();

  // -------------------------------------------------------------------------------------
  // static SIZE methods

  float getDisplayDensity( )
  {
    return getResources().getSystem().getDisplayMetrics().density;
  }

  static float getDisplayDensity( Context context )
  {
    return context.getResources().getSystem().getDisplayMetrics().density;
  }

  int setListViewHeight( HorizontalListView listView )
  {
    return TopoDroidApp.setListViewHeight( this, listView );
  }

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
    CalibInfo info = mDData.selectCalibInfo( mCID  );
    // TDLog.Log( TDLog.LOG_CALIB, "info.device " + ((info == null)? "null" : info.device) );
    // TDLog.Log( TDLog.LOG_CALIB, "device " + ((mDevice == null)? "null" : mDevice.mAddress) );
    return ( mDevice == null || ( info != null && info.device.equals( mDevice.mAddress ) ) );
  }

  public SurveyInfo getSurveyInfo()
  {
    if ( mSID <= 0 ) return null;
    if ( mData == null ) return null;
    return mData.selectSurveyInfo( mSID );
  }

  public CalibInfo getCalibInfo()
  {
    if ( mCID <= 0 ) return null;
    if ( mDData == null ) return null;
    return mDData.selectCalibInfo( mCID );
  }

  // ----------------------------------------------------------------

  @Override
  public void onTerminate()
  {
    super.onTerminate();
    // Log.v(TAG, "onTerminate app");
  }

  void setDeviceModel( Device device, int model )
  {
    if ( device != null && device == mDevice ) {
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

  void setDeviceName( Device device, String nickname )
  {
    if ( device != null /* && device == mDevice */ ) {
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
    return mComm != null && mComm.connectDevice( address, mListerSet ); // FIXME LISTER
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

    ret = readMemory( mDevice.mAddress, 0x8000 );
    if ( ret == null ) return null;

    info.mAngle   = getResources().getString( 
                    (( ret[0] & 0x01 ) != 0)? R.string.device_status_angle_grad : R.string.device_status_angle_degree );
    info.mCompass = getResources().getString( 
                    (( ret[0] & 0x04 ) != 0)? R.string.device_status_compass_on : R.string.device_status_compass_off );
    info.mCalib   = getResources().getString(
                    (( ret[0] & 0x08 ) != 0)? R.string.device_status_calib : R.string.device_status_normal );
    info.mSilent  = getResources().getString(
                    (( ret[0] & 0x10 ) != 0)? R.string.device_status_silent_on : R.string.device_status_silent_off );
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
    //   Log.v("DistoX", "X310 info C044 " + 
    //     String.format( getResources().getString( R.string.device_memory ), ret[0], ret[1] ) );
    // }

    return info;
  }

  String readHeadTail( String address, byte[] command, int[] head_tail )
  {
    return mComm.readHeadTail( address, command, head_tail );
  }

  int swapHotBit( String address, int from, int to ) 
  {
    return mComm.swapHotBit( address, from, to );
  }

  // FIXME to disappear ...
  // public long getSurveyId() { return mSID; }
  // public long getCalibId()  { return mCID; }
  // public String getSurvey() { return mySurvey; }
  // public String getCalib()  { return myCalib; }

  static boolean mEnableZip = true;  // whether zip saving is enabled or must wait (locked by th2. saving thread)
  static boolean mSketches = false;  // whether to use 3D models

  // ---------------------------------------------------------

  void startupStep2()
  {
    // ***** LOG FRAMEWORK
    TDLog.setLogTarget();
    TDLog.loadLogPreferences( mPrefs );

    mData.compileStatements();

    PtCmapActivity.setMap( mPrefs.getString( "DISTOX_PT_CMAP", null ) );

    TDSetting.loadSecondaryPreferences( this, mPrefs );
    checkAutoPairing();

    if ( TDLog.LOG_DEBUG ) {
      isTracing = true;
      Debug.startMethodTracing("DISTOX");
    }

    mPrefs.registerOnSharedPreferenceChangeListener( this );
    // TDLog.Debug("ready");
  }

  private void createComm()
  {
    if ( mComm != null ) {
      mComm.disconnectRemoteDevice( );
      mComm = null;
    }
    if ( mDevice != null && mDevice.mAddress.equals( Device.ZERO_ADDRESS ) ) {
      mComm = new VirtualDistoXComm( this, mVirtualDistoX );
    } else { 
      mComm = new DistoXComm( this );
    }
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    // require large memory pre Honeycomb
    // dalvik.system.VMRuntime.getRuntime().setMinimumHeapSize( 64<<20 );

    // Log.v("DistoX", "START" );
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

    mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
    mWelcomeScreen = mPrefs.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true
    if ( mWelcomeScreen ) {
      setDefaultSocketType();
    }

    // TDLog.Profile("TDApp paths");
    TDPath.setDefaultPaths();

    // TDLog.Profile("TDApp cwd");
    mCWD = mPrefs.getString( "DISTOX_CWD", "TopoDroid" );
    TDPath.setPaths( mCWD );

    // TDLog.Profile("TDApp DB");
    mDataListeners = new DataListenerSet( );
    // ***** DATABASE MUST COME BEFORE PREFERENCES
    mData  = new DataHelper( this, mDataListeners );
    mDData = new DeviceHelper( this, null ); 

    mStationName = new StationName();

    // TDLog.Profile("TDApp prefs");
    // LOADING THE SETTINGS IS RATHER EXPENSIVE !!!
    TDSetting.loadPrimaryPreferences( this, mPrefs );

    // TDLog.Profile("TDApp BT");
    mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    // if ( mBTAdapter == null ) {
    //   // Toast.makeText( this, R.string.not_available, Toast.LENGTH_SHORT ).show();
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
      updateDefaultPreferences(); // reset a few default preference values
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
      setBooleanPreference( "DISTOX_COSURVEY", false );
      if ( mCosurvey ) {
        mSyncConn = new ConnectionHandler( this );
        mConnListener = new ArrayList< Handler >();
      }
    }

    // TDLog.Profile("TDApp device etc.");
    mDevice = mDData.getDevice( mPrefs.getString( TDSetting.keyDeviceName(), EMPTY ) );

    DistoXConnectionError = new String[5];
    DistoXConnectionError[0] = getResources().getString( R.string.distox_err_ok );
    DistoXConnectionError[1] = getResources().getString( R.string.distox_err_headtail );
    DistoXConnectionError[2] = getResources().getString( R.string.distox_err_headtail_io );
    DistoXConnectionError[3] = getResources().getString( R.string.distox_err_headtail_eof );
    DistoXConnectionError[4] = getResources().getString( R.string.distox_err_connected );

    if ( mDevice != null ) {
      createComm();
    }

    DisplayMetrics dm = getResources().getDisplayMetrics();
    float density  = dm.density;
    mDisplayWidth  = dm.widthPixels;
    mDisplayHeight = dm.heightPixels;
    mScaleFactor   = (mDisplayHeight / 320.0f) * density;
    // FIXME it would be nice to have this, but it breaks all existing sketches
    //       therefore must stick with initial choice
    // DrawingUtil.CENTER_X = mDisplayWidth  / 2;
    // DrawingUtil.CENTER_Y = mDisplayHeight / 2;

    mHighlightedSplay = null;

    // mManual = getResources().getString( R.string.topodroid_man );
  }

  void resetLocale()
  {
    // Log.v("DistoX", "reset locale to " + mLocaleStr );
    // mLocale = (mLocaleStr.equals(EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    Resources res = getResources();
    DisplayMetrics dm = res.getDisplayMetrics();
    Configuration conf = res.getConfiguration();
    conf.locale = mLocale; // setLocale API-17
    res.updateConfiguration( conf, dm );
  }

  void setLocale( String locale, boolean load_symbols )
  {
    mLocaleStr = locale;
    mLocale = (mLocaleStr.equals(EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    resetLocale();
    Resources res = getResources();
    if ( load_symbols ) {
      BrushManager.reloadPointLibrary( this, res ); // reload symbols
      BrushManager.reloadLineLibrary( res );
      BrushManager.reloadAreaLibrary( res );
    }
    if ( mActivity != null ) mActivity.setMenuAdapter( res );
    if ( mPrefActivity != null ) mPrefActivity.reloadPreferences();
  }

  void setCWD( String cwd )
  {
    if ( cwd == null || cwd.length() == 0 || cwd.equals( mCWD ) ) return;
    mData.closeDatabase();
    mCWD = cwd;
    TDPath.setPaths( mCWD );
    mData.openDatabase();
    if ( mActivity != null ) mActivity.setTheTitle( );
  }

// -----------------------------------------------------------------

  // called by GMActivity and by CalibCoeffDialog 
  void uploadCalibCoeff( Context context, byte[] coeff, boolean check, Button b )
  {
    if ( b != null ) b.setEnabled( false );
    if ( mComm == null || mDevice == null ) {
      Toast.makeText( context, R.string.no_device_address, Toast.LENGTH_SHORT ).show();
    } else if ( check && ! checkCalibrationDeviceMatch() ) {
      Toast.makeText( context, R.string.calib_device_mismatch, Toast.LENGTH_SHORT ).show();
    } else if ( ! mComm.writeCoeff( distoAddress(), coeff ) ) {
      Toast.makeText( context, R.string.write_failed, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( context, R.string.write_ok, Toast.LENGTH_SHORT).show();
    }
    if ( b != null ) b.setEnabled( true );
  }

  // called by CalibReadTask.onPostExecute
  boolean readCalibCoeff( byte[] coeff )
  {
    if ( mComm == null || mDevice == null ) return false;
    return mComm.readCoeff( mDevice.mAddress, coeff );
  }

  // called by CalibToggleTask.doInBackground
  boolean toggleCalibMode( )
  {
    if ( mComm == null || mDevice == null ) return false;
    return mComm.toggleCalibMode( mDevice.mAddress, mDevice.mType );
  }

  byte[] readMemory( String address, int addr )
  {
    if ( mComm == null || isCommConnected() ) return null;
    return mComm.readMemory( address, addr );
  }

  int readX310Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    return mComm.readX310Memory( address, h0, h1, memory );
  }

  int readA3Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    return mComm.readA3Memory( address, h0, h1, memory );
  }

  // ------------------------------------------------------------------
  // FILE NAMES

  // public static String getSqlFile() { return APP_BASE_PATH + "survey.sql"; }

  // public static String getManifestFile() { return APP_BASE_PATH + "manifest"; }

  public void writeManifestFile()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
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
    if ( name.equals( mySurvey ) ) return true;
    if ( mData == null ) return false;
    if ( mData.renameSurvey( sid, name, forward ) ) {  
      File old = null;
      File nev = null;
      { // rename plot/sketch files: th3
        List< PlotInfo > plots = mData.selectAllPlots( sid );
        for ( PlotInfo p : plots ) {
          // Therion
            old = new File( TDPath.getSurveyPlotTh2File( mySurvey, p.name ) );
            nev = new File( TDPath.getSurveyPlotTh2File( name, p.name ) );
            if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
          // Tdr
            old = new File( TDPath.getSurveyPlotTdrFile( mySurvey, p.name ) );
            nev = new File( TDPath.getSurveyPlotTdrFile( name, p.name ) );
            if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
          // rename exported plots: dxf png svg csx
            old = new File( TDPath.getSurveyPlotDxfFile( mySurvey, p.name ) );
            nev = new File( TDPath.getSurveyPlotDxfFile( name, p.name ) );
            if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
            old = new File( TDPath.getSurveyPlotSvgFile( mySurvey, p.name ) );
            nev = new File( TDPath.getSurveyPlotSvgFile( name, p.name ) );
            if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
            old = new File( TDPath.getSurveyPlotHtmFile( mySurvey, p.name ) ); // SVG in HTML
            nev = new File( TDPath.getSurveyPlotHtmFile( name, p.name ) );
            if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
            old = new File( TDPath.getSurveyPlotPngFile( mySurvey, p.name ) );
            nev = new File( TDPath.getSurveyPlotPngFile( name, p.name ) );
            if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
            old = new File( TDPath.getSurveyPlotCsxFile( mySurvey, p.name ) );
            nev = new File( TDPath.getSurveyPlotCsxFile( name, p.name ) );
            if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        }
      }
      { // rename sketch files: th3
        List< Sketch3dInfo > sketches = mData.selectAllSketches( sid );
        for ( Sketch3dInfo s : sketches ) {
          old = new File( TDPath.getSurveySketchOutFile( mySurvey, s.name ) );
          nev = new File( TDPath.getSurveySketchOutFile( name, s.name ) );
          if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        }
      }
      // rename exported files: csv csx dat dxf kml plt srv svx th top tro 
        old = new File( TDPath.getSurveyThFile( mySurvey ) );
        nev = new File( TDPath.getSurveyThFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyCsvFile( mySurvey ) );
        nev = new File( TDPath.getSurveyCsvFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyCsxFile( mySurvey ) );
        nev = new File( TDPath.getSurveyCsxFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyCaveFile( mySurvey ) );
        nev = new File( TDPath.getSurveyCaveFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyDatFile( mySurvey ) );
        nev = new File( TDPath.getSurveyDatFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyDxfFile( mySurvey ) );
        nev = new File( TDPath.getSurveyDxfFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyKmlFile( mySurvey ) );
        nev = new File( TDPath.getSurveyKmlFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyPltFile( mySurvey ) );
        nev = new File( TDPath.getSurveyPltFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveySrvFile( mySurvey ) );
        nev = new File( TDPath.getSurveySrvFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveySvxFile( mySurvey ) );
        nev = new File( TDPath.getSurveySvxFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyTopFile( mySurvey ) );
        nev = new File( TDPath.getSurveyTopFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
        old = new File( TDPath.getSurveyTroFile( mySurvey ) );
        nev = new File( TDPath.getSurveyTroFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );

      { // rename note file: note
        old = new File( TDPath.getSurveyNoteFile( mySurvey ) );
        nev = new File( TDPath.getSurveyNoteFile( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
      }
      { // rename photo folder: photo
        old = new File( TDPath.getSurveyPhotoDir( mySurvey ) );
        nev = new File( TDPath.getSurveyPhotoDir( name ) );
        if ( old.exists() && ! nev.exists() ) old.renameTo( nev );
      }
      mySurvey = name;
      return true;
    }
    return false;
  }
    

  public long setSurveyFromName( String survey, boolean forward ) 
  { 
    mSID = -1;       // no survey by default
    mySurvey = null;
    clearCurrentStations();
    // resetManualCalibrations();
    ManualCalibration.reset();

    if ( survey != null && mData != null ) {
      // Log.v( "DistoX", "set SurveyFromName <" + survey + "> forward " + forward );

      mSID = mData.setSurvey( survey, forward );
      // mFixed.clear();
      mySurvey = null;
      if ( mSID > 0 ) {
        DistoXStationName.setInitialStation( mData.getSurveyInitailStation( mSID ) );
        mySurvey = survey;
        mSecondLastShotId = lastShotId();
        // restoreFixed();
        if ( mShotWindow != null) {
          mShotWindow.setTheTitle();
          mShotWindow.updateDisplay();
        }
        if ( mSurveyWindow != null ) {
          mSurveyWindow.setTheTitle();
          mSurveyWindow.updateDisplay();
        }
        mXSections = ( SurveyInfo.XSECTION_SHARED == mData.getSurveyXSections( mSID ) );
      }
      return mSID;
    }
    return 0;
  }

  public boolean hasSurveyName( String name ) 
  {
    return ( mData == null ) || mData.hasSurveyName( name );
  }

  public boolean hasCalibName( String name ) 
  {
    return ( mDData == null ) || mDData.hasCalibName( name );
  }

  public long setCalibFromName( String calib ) 
  {
    mCID = -1;
    myCalib = null;
    if ( calib != null && mDData != null ) {
      mCID = mDData.setCalib( calib );
      myCalib = (mCID > 0)? calib : null;
      return mCID;
    }
    return 0;
  }

  // public void setSurveyFromId( long id )
  // {
  //   if ( mData != null ) {
  //     mySurvey = mData.getSurveyFromId( id );
  //     mSID = 0;
  //     // mFixed.clear();
  //     if ( mySurvey != null ) {
  //       mSID = id;
  //       mSecondLastShotId = lastShotId();
  //       // restoreFixed();
  //     }
  //   }
  // }

  // public void setCalibFromId( long id )
  // {
  //  if ( mDData != null ) {
  //     myCalib = mDData.getCalibFromId( id );
  //     mCID = ( myCalib == null )? 0 : id;
  //   }
  // }

  // -----------------------------------------------------------------
  // PREFERENCES

  private void setDefaultSocketType()
  {
    String defaultSockType = ( android.os.Build.MANUFACTURER.equals("samsung") ) ? "1" : "0";
    Editor editor = mPrefs.edit();
    editor.putString( "DISTOX_SOCK_TYPE", defaultSockType ); 
    editor.commit();
  }

  private void updateDefaultPreferences()
  {
    if ( mPrefs != null ) {
      Editor editor = mPrefs.edit();
      if ( ! "1".equals( mPrefs.getString( "DISTOX_GROUP_BY", "1" ) ) ) {
        editor.putString( "DISTOX_GROUP_BY", "1" ); 
      }
      editor.commit();
    }
  }

  void setCWDPreference( String cwd )
  { 
    if ( mCWD.equals( cwd ) ) return;
    // Log.v("DistoX", "setCWDPreference " + cwd );
    if ( mPrefs != null ) {
      Editor editor = mPrefs.edit();
      editor.putString( "DISTOX_CWD", cwd ); 
      editor.commit();
    }
    setCWD( cwd ); 
  }

  void setPtCmapPreference( String cmap )
  {
    if ( mPrefs != null ) {
      Editor editor = mPrefs.edit();
      editor.putString( "DISTOX_PT_CMAP", cmap ); 
      editor.commit();
    }
    PtCmapActivity.setMap( cmap );
  }

  void setAccuracyPreference( float acceleration, float magnetic, float dip )
  {
    SharedPreferences.Editor editor = mPrefs.edit();
    editor.putString( "DISTOX_ACCEL_THR", Float.toString( acceleration ) ); 
    editor.putString( "DISTOX_MAG_THR", Float.toString( magnetic ) ); 
    editor.putString( "DISTOX_DIP_THR", Float.toString( dip ) ); 
    editor.commit();
  }

  void setShotDataPreference( float leg_tolerance, int leg_shots, float extend_thr,
                              float vthreshold, boolean splay_extend, int timer, int volume )
  {
    SharedPreferences.Editor editor = mPrefs.edit();
    editor.putString( "DISTOX_CLOSE_DISTANCE", Float.toString( leg_tolerance ) ); 
    editor.putString( "DISTOX_LEG_SHOTS",   Integer.toString( leg_shots ) ); 
    editor.putString( "DISTOX_EXTEND_THR2", Float.toString( extend_thr ) ); 
    editor.putString( "DISTOX_VTHRESHOLD",  Float.toString( vthreshold ) ); 
    editor.putBoolean( "DISTOX_SPLAY_EXTEND", splay_extend );
    editor.putString( "DISTOX_SHOT_TIMER",  Integer.toString( timer ) ); 
    editor.putString( "DISTOX_BEEP_VOLUME", Integer.toString( volume ) ); 
    editor.commit();
  }

  void setPlotScreenPreference( float line_width, float survey_width, float station_size, float label_size, 
                                float dot_size, float selection_radius )
  {
    SharedPreferences.Editor editor = mPrefs.edit();
    editor.putString( "DISTOX_LINE_THICKNESS",  Float.toString( line_width ) ); 
    editor.putString( "DISTOX_FIXED_THICKNESS", Float.toString( survey_width ) ); 
    editor.putString( "DISTOX_STATION_SIZE",    Float.toString( station_size ) ); 
    editor.putString( "DISTOX_LABEL_SIZE",      Float.toString( label_size ) ); 
    editor.putString( "DISTOX_DOT_RADIUS",      Float.toString( dot_size ) ); 
    editor.putString( "DISTOX_CLOSENESS",       Float.toString( selection_radius ) ); 
    editor.commit();
  }

  void setToolScreenPreference( float point_scale, float section_line_tick, int line_style,
                                float line_point_spacing, float bezier_accuracy, float bezier_corner )
  {
    SharedPreferences.Editor editor = mPrefs.edit();
    editor.putString( "DISTOX_DRAWING_UNIT",  Float.toString( point_scale ) ); 
    editor.putString( "DISTOX_ARROW_LENGTH",  Float.toString( section_line_tick ) ); 
    editor.putString( "DISTOX_LINE_STYLE",    Integer.toString( line_style) ); 
    editor.putString( "DISTOX_LINE_SEGMENT",  Float.toString( line_point_spacing ) ); 
    editor.putString( "DISTOX_LINE_ACCURACY", Float.toString( bezier_accuracy ) ); 
    editor.putString( "DISTOX_LINE_CORNER",   Float.toString( bezier_corner ) ); 
    editor.commit();
  }
  
  void setSurveyLocationPreference( String crs, boolean gps_averaging, String units, int alt, boolean alt_lookup )
  {
    SharedPreferences.Editor editor = mPrefs.edit();
    // editor.putString( "DISTOX_CRS",  crs );
    editor.putBoolean( "DISTOX_GPS_AVERAGING",  gps_averaging );
    editor.putString( "DISTOX_UNIT_LOCATION", units ); 
    editor.putString( "DISTOX_ALTITUDE", Integer.toString( alt ) ); 
    editor.putBoolean( "DISTOX_ALTIMETRIC", alt_lookup );
    editor.commit();
  }


  public void onSharedPreferenceChanged( SharedPreferences sp, String k ) 
  {
    // TDLog.Error("shared pref changed " + k );
    TDSetting.checkPreference( sp, k, mActivity, this );
  }

  // used for "DISTOX_WELCOME_SCREEN" and "DISTOX_TD_SYMBOL"
  void setBooleanPreference( String preference, boolean val )
  {
    SharedPreferences.Editor editor = mPrefs.edit();
    editor.putBoolean( preference, val );
    editor.commit(); // Very important to save the preference
  }

  void setDevice( String address ) 
  { 
    // Log.v("DistoX", "VD TDapp set device address " + address );
    if ( address == null ) {
      if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this );
      mDevice = null;
      address = EMPTY;
    } else if ( address.equals( Device.ZERO_ADDRESS )  ) {
      if ( mVirtualDistoX != null ) mVirtualDistoX.startServer( this );
      boolean create = ( mDevice == null || ! address.equals( mDevice.mAddress ) );
      mDevice = new Device( address, "DistoX0", "X000", null );
      if ( create ) createComm();
    } else {
      if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this );
      boolean create = ( mDevice == null || mDevice.mAddress.equals( Device.ZERO_ADDRESS ) );
      mDevice = mDData.getDevice( address );
      if ( create ) createComm();
    }
    if ( mPrefs != null ) {
      Editor editor = mPrefs.edit();
      editor.putString( TDSetting.keyDeviceName(), address ); 
      editor.commit();
    }
  }

  // -------------------------------------------------------------
  // DATA BATCH DOWNLOAD

  public int downloadDataBatch( Handler /* ILister */ lister ) // FIXME LISTER
  {
    mSecondLastShotId = lastShotId();
    int ret = 0;
    if ( mComm == null || mDevice == null ) {
      TDLog.Error( "Comm or Device null ");
    } else {
      TDLog.Log( TDLog.LOG_DATA, "Download Data Batch() device " + mDevice + " comm " + mComm.toString() );
      ret = mComm.downloadData( mDevice.mAddress, lister );
      // FIXME BATCH
      // if ( ret > 0 && TDSetting.mSurveyStations > 0 ) {
      //   // FIXME TODO select only shots after the last leg shots
      //   List<DBlock> list = mData.selectAllShots( mSID, TDStataus.NORMAL );
      //   assign Stations( list );
      // }
    }
    return ret;
  }

  // =======================================================
  StationName mStationName;

  boolean setCurrentStationName( String name ) { return mStationName.setCurrentStationName( name ); }
  String getCurrentStationName() { return mStationName.getCurrentStationName(); }
  boolean isCurrentStationName( String name ) { return mStationName.isCurrentStationName( name ); }
  void clearCurrentStations() { mStationName.clearCurrentStations(); }
  String getCurrentOrLastStation( ) { return mStationName.getCurrentOrLastStation( mData, mSID); }

  // FIXME TROBOT
  static long trobotmillis = 0L;

  // called also by ShotWindow::updataBlockList
  // this re-assign stations to shots with station(s) already set
  // the list of stations is ordered by compare
  //
  public void assignStationsAfter( DBlock blk0, List<DBlock> list, ArrayList<String> sts )
  { 
    // Log.v("DistoX", "assign stations after " + blk0.Name() + " size " + list.size() );
    // if ( TDSetting.mSurveyStations < 0 ) return;
    if ( TDSetting.mTRobotShot ) {
      long millis = SystemClock.uptimeMillis(); // FIXME TROBOT
      if ( millis > trobotmillis + 10000 ) {
        Toast.makeText( this, "WARNING TopoRobot policy is very experimental", Toast.LENGTH_SHORT).show();
        trobotmillis = millis;
      }
      mStationName.assignStationsAfter_TRobot( mData, mSID, blk0, list, sts );
      return;
    } 
    if ( TDSetting.mBacksightShot ) {
      mStationName.assignStationsAfter_Backsight( mData, mSID, blk0, list, sts );
      return;
    } 
    if ( TDSetting.mTripodShot ) {
      mStationName.assignStationsAfter_Tripod( mData, mSID, blk0, list, sts );
      return;
    }
    mStationName.assignStationsAfter_Default( mData, mSID, blk0, list, sts );
  }

  // called also by ShotWindow::updataBlockList
  // @param list blocks whose stations need to be set in the DB
  //
  public void assignStationsAll( List<DBlock> list )
  { 
    // Log.v("DistoX", "assign stations size " + list.size() );
    // if ( TDSetting.mSurveyStations < 0 ) return;
    if ( TDSetting.mTRobotShot ) {
      long millis = SystemClock.uptimeMillis(); // FIXME TROBOT
      if ( millis > trobotmillis + 10000 ) {
        Toast.makeText( this, "WARNING TopoRobot policy is very experimental", Toast.LENGTH_SHORT).show();
        trobotmillis = millis;
      }
      mStationName.assignStations_TRobot( mData, mSID, list );
      return;
    } 
    if ( TDSetting.mBacksightShot ) {
      mStationName.assignStations_Backsight( mData, mSID, list );
      return;
    } 
    if ( TDSetting.mTripodShot ) {
      mStationName.assignStations_Tripod( mData, mSID, list );
      return;
    }
    mStationName.assignStations_Default( mData, mSID, list );
  }

  // ================================================================
  // EXPORTS

  public String exportSurveyAsCsx( DrawingWindow sketch, String origin )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) return null;
    String filename = ( sketch == null )? TDPath.getSurveyCsxFile(mySurvey)
                                        : TDPath.getSurveyCsxFile(mySurvey, sketch.mName1);
    return TDExporter.exportSurveyAsCsx( mSID, mData, info, sketch, origin, filename );
  }

  public String exportSurveyAsTop( DrawingWindow sketch, String origin )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export TOP null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyTopFile( mySurvey );
    return TDExporter.exportSurveyAsTop( mSID, mData, info, sketch, origin, filename );
  }

  public String exportSurveyAsTh( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export TH null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyThFile( mySurvey );
    return TDExporter.exportSurveyAsTh( mSID, mData, info, filename );
  }

  public String exportSurveyAsPlg()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export PLG null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyCaveFile( mySurvey );
    return TDExporter.exportSurveyAsPlg( mSID, mData, info, filename );
  }

  public String exportSurveyAsCav()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export CAV null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyCavFile( mySurvey );
    return TDExporter.exportSurveyAsCav( mSID, mData, info, filename );
  }

  public String exportSurveyAsGrt()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export GRT null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyGrtFile( mySurvey );
    return TDExporter.exportSurveyAsGrt( mSID, mData, info, filename );
  }

  public String exportSurveyAsGtx()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export GTX null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyGtxFile( mySurvey );
    return TDExporter.exportSurveyAsGtx( mSID, mData, info, filename );
  }

  public String exportSurveyAsSvx()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export SVX null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveySvxFile( mySurvey );
    return TDExporter.exportSurveyAsSvx( mSID, mData, info, mDevice, filename );
  }

  public String exportSurveyAsSur()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export SUR null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveySurFile( mySurvey );
    return TDExporter.exportSurveyAsSur( mSID, mData, info, filename );
  }

  public String exportSurveyAsTrb()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export TRB null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyTrbFile( mySurvey );
    return TDExporter.exportSurveyAsTrb( mSID, mData, info, filename );
  }

  public String exportSurveyAsTro()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export TRO null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyTroFile( mySurvey );
    return TDExporter.exportSurveyAsTro( mSID, mData, info, filename );
  }

  public String exportSurveyAsCsv( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export CSV null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyCsvFile( mySurvey );
    return TDExporter.exportSurveyAsCsv( mSID, mData, info, filename );
  }

  public String exportSurveyAsSrv()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export SRV null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveySrvFile( mySurvey );
    return TDExporter.exportSurveyAsSrv( mSID, mData, info, filename );
  }

  public String exportSurveyAsDxf( DistoXNum num )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export DXF null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyDxfFile( mySurvey );
    return TDExporter.exportSurveyAsDxf( mSID, mData, info, num, filename );
  }

  public String exportSurveyAsKml( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export KML null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyKmlFile( mySurvey );
    return TDExporter.exportSurveyAsKml( mSID, mData, info, filename );
  }

  public String exportSurveyAsPlt( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export PLT null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyPltFile( mySurvey );
    return TDExporter.exportSurveyAsPlt( mSID, mData, info, filename );
  }

  public String exportSurveyAsDat()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export DAT null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyDatFile( mySurvey );
    return TDExporter.exportSurveyAsDat( mSID, mData, info, filename );
  }


  public String exportCalibAsCsv( )
  {
    if ( mCID < 0 ) return null;
    CalibInfo ci = mDData.selectCalibInfo( mCID );
    if ( ci == null ) return null;
    TDPath.checkCCsvDir();
    String filename = TDPath.getCCsvFile( ci.name );
    return TDExporter.exportCalibAsCsv( mCID, mDData, ci, filename );
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
  //     InputStream is = getResources().openRawResource( R.raw.symbols );
  //     symbolsUncompress( is, overwrite );
  //   }
  //   mDData.setValue( "symbol_version", SYMBOL_VERSION );
  // }

  void installSymbols( boolean overwrite )
  {
    deleteObsoleteSymbols();
    installSymbols( R.raw.symbols, overwrite );
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
      File file = new File( TDPath.APP_LINE_PATH + line );
      if ( file.exists() ) file.delete();
    }
  }

  private void clearSymbolsDir( String dirname )
  {
    // Log.v("DistoX", "clear " + dirname );
    File dir = new File( dirname );
    File [] files = dir.listFiles();
    for ( int i=0; i<files.length; ++i ) {
      if ( files[i].isDirectory() ) continue;
      files[i].delete();
    }
  }
    
  private void clearSymbols( )
  {
    clearSymbolsDir( TDPath.APP_POINT_PATH );
    clearSymbolsDir( TDPath.APP_LINE_PATH );
    clearSymbolsDir( TDPath.APP_AREA_PATH );
  }  

  void reloadSymbols( boolean clear, 
                      boolean speleo, boolean extra, boolean mine, boolean geo, boolean archeo, boolean paleo, boolean bio )
  {
    // Log.v("DistoX", "Reload symbols " + speleo + " " + mine + " " + geo + " " + archeo + " " + paleo + " " + bio + " clear " + clear );
    if ( extra ) speleo = true; // extra implies speleo

    if ( clear ) {
      if (speleo || mine || geo || archeo || paleo || bio ) { 
        clearSymbols();
      }
    }
    if ( speleo ) installSymbols( R.raw.symbols, true );
    if ( extra  ) installSymbols( R.raw.symbols_extra,  true );
    if ( mine   ) installSymbols( R.raw.symbols_mine,   true );
    if ( geo    ) installSymbols( R.raw.symbols_geo,    true );
    if ( archeo ) installSymbols( R.raw.symbols_archeo, true );
    if ( paleo  ) installSymbols( R.raw.symbols_paleo,  true );
    if ( bio    ) installSymbols( R.raw.symbols_bio,    true );

    mDData.setValue( "symbol_version", SYMBOL_VERSION );
    BrushManager.reloadAllLibraries( this, getResources() );
    // BrushManager.makePaths( getResources() );
  }

  static private void symbolsUncompress( InputStream fis, boolean overwrite )
  {
    // Log.v( "DistoX", "symbol uncompress ...");
    TDPath.symbolsCheckDirs();
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( filepath.endsWith("README") ) continue;
        // Log.v(  "DistoX", "ZipEntry " + filepath );
        if ( ! ze.isDirectory() ) {
          if ( filepath.startsWith( "symbol" ) ) {
            int pos  = 1 + filepath.indexOf('/');
            filepath = filepath.substring( pos );
          }
          String pathname = TDPath.getSymbolFile( filepath );
          File file = new File( pathname );
          if ( overwrite || ! file.exists() ) {
            // APP_SAVE SYMBOLS
            if ( file.exists() ) file.renameTo( new File( TDPath.getSymbolSaveFile( filepath ) ) );

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
  void insertLRUDatStation( String station, float bearing, float clino,
                            String left, String right, String up, String down )
  {
    // could return the long
    addManualSplays( -1L, station, left, right, up, down, bearing, false );
  }

  private long addManualSplays( long at, String splay_station, String left, String right, String up, String down,
                                float bearing, boolean horizontal )
  {
    long id;
    long extend = 0L;
    float calib = ManualCalibration.mLRUD ? ManualCalibration.mLength / TDSetting.mUnitLength : 0;
    if ( left != null && left.length() > 0 ) {
      float l = -1.0f;
      try {
        l = Float.parseFloat( left ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: left " + left );
      }
      l -= calib;
      
      if ( l >= 0.0f ) {
        if ( horizontal ) { // WENS
          extend = TDAzimuth.computeSplayExtend( 270 );
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, l, 270.0f, 0.0f, 0.0f, extend, 1, true );
            ++at;
          } else {
            id = mData.insertShot( mSID, -1L, l, 270.0f, 0.0f, 0.0f, extend, 1, true );
          }
        } else {
          float b = bearing - 90.0f;
          if ( b < 0.0f ) b += 360.0f;
          extend = TDAzimuth.computeSplayExtend( b );
          // b = in360( b );
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, l, b, 0.0f, 0.0f, extend, 1, true );
            ++at;
          } else {
            id = mData.insertShot( mSID, -1L, l, b, 0.0f, 0.0f, extend, 1, true );
          }
        }
        mData.updateShotName( id, mSID, splay_station, EMPTY, true );
      }
    } 
    if ( right != null && right.length() > 0 ) {
      float r = -1.0f;
      try {
        r = Float.parseFloat( right ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: right " + right );
      }
      r -= calib;
      if ( r >= 0.0f ) {
        if ( horizontal ) { // WENS
          extend = TDAzimuth.computeSplayExtend( 90 );
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, r, 90.0f, 0.0f, 0.0f, extend, 1, true );
            ++at;
          } else {
            id = mData.insertShot( mSID, -1L, r, 90.0f, 0.0f, 0.0f, extend, 1, true );
          }
        } else {
          float b = bearing + 90.0f;
          if ( b >= 360.0f ) b -= 360.0f;
          extend = TDAzimuth.computeSplayExtend( b );
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, r, b, 0.0f, 0.0f, extend, 1, true );
            ++at;
          } else {
            id = mData.insertShot( mSID, -1L, r, b, 0.0f, 0.0f, extend, 1, true );
          }
        }
        mData.updateShotName( id, mSID, splay_station, EMPTY, true );
      }
    }
    if ( up != null && up.length() > 0 ) {
      float u = -1.0f;
      try {
        u = Float.parseFloat( up ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: up " + up );
      }
      u -= calib;
      if ( u >= 0.0f ) {  
        if ( horizontal ) {
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, u, 0.0f, 0.0f, 0.0f, 0L, 1, true );
            ++at;
          } else {
            id = mData.insertShot( mSID, -1L, u, 0.0f, 0.0f, 0.0f, 0L, 1, true );
          }
        } else {
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, u, 0.0f, 90.0f, 0.0f, 0L, 1, true );
            ++at;
          } else {
            id = mData.insertShot( mSID, -1L, u, 0.0f, 90.0f, 0.0f, 0L, 1, true );
          }
        }
        mData.updateShotName( id, mSID, splay_station, EMPTY, true );
      }
    }
    if ( down != null && down.length() > 0 ) {
      float d = -1.0f;
      try {
        d = Float.parseFloat( down ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: down " + down );
      }
      d -= calib;
      if ( d >= 0.0f ) {
        if ( horizontal ) {
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, d, 180.0f, 0.0f, 0.0f, 0L, 1, true );
            ++at;
          } else {
            id = mData.insertShot( mSID, -1L, d, 180.0f, 0.0f, 0.0f, 0L, 1, true );
          }
        } else {
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, d, 0.0f, -90.0f, 0.0f, 0L, 1, true );
            ++at;
          } else {
            id = mData.insertShot( mSID, -1L, d, 0.0f, -90.0f, 0.0f, 0L, 1, true );
          }
        }
        mData.updateShotName( id, mSID, splay_station, EMPTY, true );
      }
    }
    return at;
  }

  /** insert manual-data shot
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   * 
   * NOTE manual shots take into account the instruents calibrations
   *      LRUD are not affected
   */
  public DBlock insertManualShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend0,
                           String left, String right, String up, String down,
                           String splay_station )
  {
    mSecondLastShotId = lastShotId();
    DBlock ret = null;
    long id;

    distance = (distance - ManualCalibration.mLength)  / TDSetting.mUnitLength;
    clino    = (clino    - ManualCalibration.mClino)   / TDSetting.mUnitAngle;
    float b  = bearing / TDSetting.mUnitAngle;


    if ( ( distance < 0.0f ) ||
         ( clino < -90.0f || clino > 90.0f ) ||
         ( b < 0.0f || b >= 360.0f ) ) {
      Toast.makeText( this, R.string.illegal_data_value, Toast.LENGTH_SHORT ).show();
      return null;
    }
    bearing = (bearing  - ManualCalibration.mAzimuth) / TDSetting.mUnitAngle;
    while ( bearing >= 360 ) bearing -= 360;
    while ( bearing <    0 ) bearing += 360;

    if ( from != null && to != null && from.length() > 0 ) {
      // if ( mData.makesCycle( -1L, mSID, from, to ) ) {
      //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
      // } else
      {
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > TDSetting.mVThreshold );
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot SID " + mSID + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( TDSetting.mShotAfterSplays ) {
          at = addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal );

          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, distance, bearing, clino, 0.0f, extend0, 1, true );
          } else {
            id = mData.insertShot( mSID, -1L, distance, bearing, clino, 0.0f, extend0, 1, true );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, mSID, from, to, true );
          // mData.updateShotExtend( id, mSID, extend0, true );
          // mData.updateShotExtend( id, mSID, DBlock.EXTEND_IGNORE, true ); // FIXME WHY ???
          // FIXME updateDisplay( );
        } else {
          if ( at >= 0L ) {
            id = mData.insertShotAt( mSID, at, distance, bearing, clino, 0.0f, extend0, 1, true );
            ++ at;
          } else {
            id = mData.insertShot( mSID, -1L, distance, bearing, clino, 0.0f, extend0, 1, true );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, mSID, from, to, true );
          // mData.updateShotExtend( id, mSID, extend0, true ); 
          // mData.updateShotExtend( id, mSID, DBlock.EXTEND_IGNORE, true );  // FIXME WHY ???
          // FIXME updateDisplay( );

          addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal );
        }
        ret = mData.selectShot( id, mSID );
      }
    } else {
      Toast.makeText( this, R.string.missing_station, Toast.LENGTH_SHORT ).show();
    }
    return ret;
  }

  int getCalibAlgoFromDB()
  {
    return mDData.selectCalibAlgo( mCID );
  }

  void updateCalibAlgo( int algo ) 
  {
    mDData.updateCalibAlgo( mCID, algo );
  }
  
  int getCalibAlgoFromDevice()
  {
    if ( mDevice == null ) return CalibInfo.ALGO_LINEAR;
    if ( mDevice.mType == Device.DISTO_A3 ) return CalibInfo.ALGO_LINEAR; // A3
    if ( mDevice.mType == Device.DISTO_X310 ) {
      // if ( mComm == null ) return 1; // should not happen
      byte[] ret = mComm.readMemory( mDevice.mAddress, 0xe000 );
      if ( ret != null && ( ret[0] >= 2 && ret[1] >= 3 ) ) return CalibInfo.ALGO_NON_LINEAR;
    }
    return CalibInfo.ALGO_LINEAR; // default
  }  

  // --------------------------------------------------------

  void setX310Laser( int what, Handler /* ILister */ lister ) // 0: off, 1: on, 2: measure // FIXME LISTER
  {
    if ( mComm != null && mDevice != null ) {
      mComm.setX310Laser( mDevice.mAddress, what, lister );
    }
  }

  // int readFirmwareHardware()
  // {
  //   return mComm.readFirmwareHardware( mDevice.mAddress );
  // }

  int dumpFirmware( String filename )
  {
    if ( mComm == null || mDevice == null ) return -1;
    return mComm.dumpFirmware( mDevice.mAddress, TDPath.getBinFile(filename) );
  }

  int uploadFirmware( String filename )
  {
    if ( mComm == null || mDevice == null ) {
      TDLog.Error( "Comm or Device null");
      return -1;
    }
    String pathname = TDPath.getBinFile( filename );
    TDLog.LogFile( "Firmware upload address " + mDevice.mAddress );
    TDLog.LogFile( "Firmware upload file " + pathname );
    if ( ! pathname.endsWith( "bin" ) ) {
      TDLog.LogFile( "Firmware upload file does not end with \"bin\"");
      return 0;
    }
    return mComm.uploadFirmware( mDevice.mAddress, pathname );
  }

  // ----------------------------------------------------------------------

  long insert2dPlot( long sid , String name, String start, boolean extended, int project )
  {
    // TDLog.Log( TDLog.LOG_PLOT, "new plot " + name + " start " + start );
    long pid_p = mData.insertPlot( sid, -1L, name+"p",
                 PlotInfo.PLOT_PLAN, 0L, start, EMPTY, 0, 0, mScaleFactor, 0, 0, EMPTY, EMPTY, true );
    if ( extended ) {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotInfo.PLOT_EXTENDED, 0L, start, EMPTY, 0, 0, mScaleFactor, 0, 0, EMPTY, EMPTY, true );
    } else {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotInfo.PLOT_PROFILE, 0L, start, EMPTY, 0, 0, mScaleFactor, project, 0, EMPTY, EMPTY, true );
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
    return mData.insertPlot( sid, -1L, name, type, 0L, from, to, 0, 0, TopoDroidApp.mScaleFactor, azimuth, clino, hide, nick, false );
  }

  public void viewPhoto( Context ctx, String filename )
  {
    // Log.v("DistoX", "photo <" + filename + ">" );
    File file = new File( filename );
    Uri uri = Uri.fromFile( file );
    // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + filename ) );
    Intent intent = new Intent(Intent.ACTION_VIEW );
    intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
    intent.setDataAndType( uri, "image/jpeg" );
    try {
      ctx.startActivity( intent );
    } catch ( ActivityNotFoundException e ) {
      // gracefully fail without saying anything
    }
  }

  // ---------------------------------------------------------------------
  // SYNC

  static boolean mCosurvey = false;       // whether co-survey is enable by the DB
  static boolean mCoSurveyServer = false; // whether co-survey server is on
  ConnectionHandler mSyncConn = null;

  void setCoSurvey( boolean co_survey ) // FIXME interplay with TDSetting
  {
    if ( ! mCosurvey ) {
      mCoSurveyServer = false;
      setBooleanPreference( "DISTOX_COSURVEY", false );
      return;
    } 
    mCoSurveyServer = co_survey;
    if ( mCoSurveyServer ) { // start server
      startRemoteTopoDroid( );
    } else { // stop server
      stopRemoteTopoDroid( );
    }
  }


  int getConnectionType() 
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getType();
  }

  int getAcceptState()
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getAcceptState();
  }

  int getConnectState()
  {
    return ( mSyncConn == null )? SyncService.STATE_NONE : mSyncConn.getConnectState();
  }

  String getConnectionStateStr()
  {
    return ( mSyncConn == null )? "NONE": mSyncConn.getConnectStateStr();
  }

  String getConnectedDeviceName()
  {
    return ( mSyncConn == null )? null : mSyncConn.getConnectedDeviceName();
  }

  String getConnectionStateTitleStr()
  {
    return ( mSyncConn == null )? EMPTY : mSyncConn.getConnectionStateTitleStr();
  }

  void connStateChanged()
  {
    // Log.v( "DistoX", "connStateChanged()" );
    if ( mSurveyWindow != null ) mSurveyWindow.setTheTitle();
    if ( mShotWindow  != null) mShotWindow.setTheTitle();
    if ( mActivity != null ) mActivity.setTheTitle();
  }

  void refreshUI()
  {
    if ( mSurveyWindow != null ) mSurveyWindow.updateDisplay();
    if ( mShotWindow  != null) mShotWindow.updateDisplay();
    if ( mActivity != null ) mActivity.updateDisplay();
  }

  void connectRemoteTopoDroid( BluetoothDevice device )
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

  void syncRemoteTopoDroid( BluetoothDevice device )
  { 
    if ( mSyncConn != null ) mSyncConn.syncDevice( device );
  }

  void startRemoteTopoDroid( )
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

  void syncConnectionFailed()
  {
    Toast.makeText( this, "Sync connection failed", Toast.LENGTH_SHORT ).show();
  }

  void syncConnectedDevice( String name )
  {
    Toast.makeText( this, "Sync connected " + name, Toast.LENGTH_SHORT ).show();
    if ( mSyncConn != null ) registerDataListener( mSyncConn );
  }

  // ---------------------------------------------------------------
  // DISTOX PAIRING

  static PairingRequest mPairingRequest = null;

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

}
