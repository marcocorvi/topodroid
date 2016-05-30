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
import android.os.AsyncTask;

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

import android.util.Log;
import android.util.DisplayMetrics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.widget.Toast;

public class TopoDroidApp extends Application
                          implements OnSharedPreferenceChangeListener
{
  String mCWD;  // current work directory

  static String SYMBOL_VERSION = "24";
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

  static boolean isTracing = false;

  static float mManualCalibrationLength  = 0; // calibration of manually inputed data: length
  static float mManualCalibrationAzimuth = 0;
  static float mManualCalibrationClino   = 0;

  static private void resetManualCalibrations() 
  {
    mManualCalibrationLength  = 0; 
    mManualCalibrationAzimuth = 0;
    mManualCalibrationClino   = 0;
  }

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
      new ReconnectTask( mDataDownloader ).execute();
    }
  }


  // ----------------------------------------------------------------------
  // DataListener (co-surveying)
  private ArrayList< DataListener > mDataListeners;

  // synchronized( mDataListener )
  void registerDataListener( DataListener listener )
  {
    for ( DataListener l : mDataListeners ) {
      if ( l == listener ) return;
    }
    mDataListeners.add( listener );
  }

  // synchronized( mDataListener )
  void unregisterDataListener( DataListener listener )
  {
    mDataListeners.remove( listener );
  }

  // -----------------------------------------------------

  private SharedPreferences mPrefs;
  boolean askSymbolUpdate = false; // by default do not ask

  static final int STATUS_NORMAL  = 0;   // item (shot, plot, fixed) status
  static final int STATUS_DELETED = 1;  

  String[] DistoXConnectionError;
  BluetoothAdapter mBTAdapter = null;     // BT connection
  private TopoDroidComm mComm = null;        // BT communication
  DataDownloader mDataDownloader = null;  // data downloader
  static DataHelper mData = null;         // database 
  static DeviceHelper mDData = null;      // device/calib database

  static SurveyActivity mSurveyActivity = null;
  static ShotActivity mShotActivity     = null;
  // static DrawingActivity mDrawingActivity = null; // FIXME currently not used
  TopoDroidActivity mActivity = null; 
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

  // -------------------------------------------------------------------------------------
  // static SIZE methods

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
    int size = getScaledSize( context );
    LayoutParams params = listView.getLayoutParams();
    params.height = size + 10;
    listView.setLayoutParams( params );
    return size;
  }

  // default button size
  static int getScaledSize( Context context )
  {
    return (int)( TDSetting.mSizeButtons * context.getResources().getSystem().getDisplayMetrics().density );
  }

  static int getDefaultSize( Context context )
  {
    return (int)( 42 * context.getResources().getSystem().getDisplayMetrics().density );
  }

  boolean isMultitouch()
  {
    return getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
  }

  // ------------------------------------------------------------

  static float mAccelerationMean = 0.0f;
  static float mMagneticMean     = 0.0f;
  static float mDipMean          = 0.0f;

  static float deltaAcc( float acc )
  {
    if ( mAccelerationMean > 0 ) return TDMath.abs( 100*(acc - mAccelerationMean)/mAccelerationMean ); 
    return 0;
  }

  static float deltaMag( float mag )
  {
    if ( mMagneticMean > 0 ) return TDMath.abs( 100*(mag - mMagneticMean)/mMagneticMean );
    return 0;
  }

  static float deltaDip( float dip ) { return TDMath.abs( dip - mDipMean ); }

  static boolean isBlockAcceptable( float acc, float mag, float dip )
  {
    return true
        && deltaAcc( acc ) < TDSetting.mAccelerationThr
        && deltaMag( mag ) < TDSetting.mMagneticThr
        && deltaDip( dip ) < TDSetting.mDipThr
    ;
  }

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
    mComm.disconnectRemoteDevice( );
    mComm = null;
    mComm = new DistoXComm( this );
    mDataDownloader.onStop(); // mDownload = false;
  }

  // FIXME BT_RECEIVER 
  // void resetCommBTReceiver()
  // {
  //   mComm.resetBTReceiver();
  // }

  // called by DeviceActivity::setState()
  //           ShotActivity::onResume()
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

    ret = readMemory( address, 0xe004 );
    if ( ret == null ) return null;
    info.mHardware = String.format( getResources().getString( R.string.device_hardware ), ret[0], ret[1] );

    return info;
  }

  String readHeadTail( String address, int[] head_tail )
  {
    return mComm.readHeadTail( address, head_tail );
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

  @Override
  public void onCreate()
  {
    super.onCreate();

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

    // TDLog.Profile("TDApp paths");
    TDPath.setDefaultPaths();

    // TDLog.Profile("TDApp cwd");
    mCWD = mPrefs.getString( "DISTOX_CWD", "TopoDroid" );
    TDPath.setPaths( mCWD );

    // TDLog.Profile("TDApp DB");
    mDataListeners = new ArrayList< DataListener >( );
    // ***** DATABASE MUST COME BEFORE PREFERENCES
    mData  = new DataHelper( this, mDataListeners );
    mDData = new DeviceHelper( this, null ); 

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
    mComm = new DistoXComm( this );
    mListerSet = new ListerSetHandler();
    mDataDownloader = new DataDownloader( this, this );

    mEnableZip = true;

    // ***** DRAWING TOOLS SYMBOLS
    // TDLog.Profile("TDApp symbols");
    String version = mDData.getValue( "version" );
    if ( version == null || ( ! version.equals(VERSION) ) ) {
      mDData.setValue( "version", VERSION );
      // FIXME MANUAL installManual( );  // must come before installSymbols
      installSymbols( false ); // this updates symbol_version in the database
      installFirmware( false );
      // installUserManual( );
      updateDefaultPreferences(); // reset a few default preference values
    }

    // ***** CHECK SPECIAL EXPERIMENTAL FEATURES
    if ( TDSetting.mLevelOverExperimental ) {
      String value = mDData.getValue("sketches");
      mSketches =  value != null 
                && value.equals("on")
                && getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
    }

    if ( TDSetting.mLevelOverAdvanced ) {
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
    mDevice = mDData.getDevice( mPrefs.getString( TDSetting.keyDeviceName(), "" ) );

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

    // mManual = getResources().getString( R.string.topodroid_man );
  }

  void resetLocale()
  {
    // Log.v("DistoX", "reset locale to " + mLocaleStr );
    // mLocale = (mLocaleStr.equals(""))? Locale.getDefault() : new Locale( mLocaleStr );
    Resources res = getResources();
    DisplayMetrics dm = res.getDisplayMetrics();
    Configuration conf = res.getConfiguration();
    conf.locale = mLocale; // setLocale API-17
    res.updateConfiguration( conf, dm );
  }

  void setLocale( String locale, boolean load_symbols )
  {
    mLocaleStr = locale;
    mLocale = (mLocaleStr.equals(""))? Locale.getDefault() : new Locale( mLocaleStr );
    resetLocale();
    Resources res = getResources();
    if ( load_symbols ) {
      DrawingBrushPaths.reloadPointLibrary( res ); // reload symbols
      DrawingBrushPaths.reloadLineLibrary( res );
      DrawingBrushPaths.reloadAreaLibrary( res );
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
  void uploadCalibCoeff( Context context, byte[] coeff, boolean check )
  {
    if ( mComm == null || mDevice == null ) {
      Toast.makeText( context, R.string.no_device_address, Toast.LENGTH_SHORT ).show();
    } else if ( check && ! checkCalibrationDeviceMatch() ) {
      Toast.makeText( context, R.string.calib_device_mismatch, Toast.LENGTH_SHORT ).show();
    } else if ( ! mComm.writeCoeff( distoAddress(), coeff ) ) {
      Toast.makeText( context, R.string.write_failed, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( context, R.string.write_ok, Toast.LENGTH_SHORT).show();
    }
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
      // FIXME
    } catch ( IOException e ) {
      // FIXME
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
          old = new File( TDPath.getSurveySketchFile( mySurvey, s.name ) );
          nev = new File( TDPath.getSurveySketchFile( name, s.name ) );
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
    resetManualCalibrations();

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
        if ( mShotActivity != null) {
          mShotActivity.setTheTitle();
          mShotActivity.updateDisplay();
        }
        if ( mSurveyActivity != null ) {
          mSurveyActivity.setTheTitle();
          mSurveyActivity.updateDisplay();
        }
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
    if ( address == null ) {
      mDevice = null;
      address = "";
    } else {
      mDevice = mDData.getDevice( address );
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
    TDLog.Log( TDLog.LOG_DATA, "Download Data Batch() device " + mDevice + " comm " + mComm.toString() );
    int ret = 0;
    if ( mComm != null && mDevice != null ) {
      ret = mComm.downloadData( mDevice.mAddress, lister );
      // FIXME BATCH
      // if ( ret > 0 && TDSetting.mSurveyStations > 0 ) {
      //   // FIXME TODO select only shots after the last leg shots
      //   List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
      //   assign Stations( list );
      // }
    } else {
      TDLog.Error( "Comm or Device is null ");
    }
    return ret;
  }

  // =======================================================
  // current station(s)

  private String mCurrentStationName = null;

  void setCurrentStationName( String name ) 
  { 
    if ( name == null || name.equals(mCurrentStationName) ) {
      mCurrentStationName = null; // clear
    } else {
      mCurrentStationName = name;
    }
  }

  String getCurrentStationName() { return mCurrentStationName; }

  boolean isCurrentStationName( String name ) { return name.equals(mCurrentStationName); }

  private String getLastStationName()
  {
    DistoXDBlock last = null;
    List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
    for ( DistoXDBlock blk : list ) {
      if ( blk.mFrom != null && blk.mFrom.length() > 0 ) { last = blk; }
    }
    if ( last == null ) return "0";
    if ( last.mTo == null || last.mTo.length() == 0 ) return last.mFrom;
    if ( TDSetting.mSurveyStations == 1 ) return last.mTo;  // forward-shot
    return last.mFrom;
  }


  String getCurrentOrLastStation()
  {
    if ( mCurrentStationName != null ) return mCurrentStationName;
    return getLastStationName();
  }
  
  void clearCurrentStations()
  {
    mCurrentStationName = null;
  }

  // ----------------------------------------------------------------

  private void setLegExtend( DistoXDBlock prev )
  {
    // FIXME what has "splay extend" to do with "leg extend" ???
    // if ( ! TDSetting.mSplayExtend ) 
    {
      long extend = TDAzimuth.computeLegExtend( prev.mBearing );
      mData.updateShotExtend( prev.mId, mSID, extend, true );
    }
  }

  // called also by ShotActivity::updataBlockList
  // this re-assign stations to shots with station(s) already set
  //
  public void assignStationsAfter( DistoXDBlock blk0, List<DistoXDBlock> list )
  { 
    // if ( TDSetting.mSurveyStations < 0 ) return;
    if ( TDSetting.mBacksightShot ) {
      assignStationsAfter_Backsight( blk0, list );
      return;
    } else if ( TDSetting.mTripodShot ) {
      assignStationsAfter_Tripod( blk0, list );
      return;
    }
    assignStationsAfter_Default( blk0, list );
  }

  // called also by ShotActivity::updataBlockList
  // @param list blocks whose stations need to be set in the DB
  //
  public void assignStations( List<DistoXDBlock> list )
  { 
    // if ( TDSetting.mSurveyStations < 0 ) return;
    if ( TDSetting.mBacksightShot ) {
      assignStations_Backsight( list );
      return;
    } else if ( TDSetting.mTripodShot ) {
      assignStations_Tripod( list );
      return;
    }
    assignStations_Default( list );
  }

  private void assignStationsAfter_Tripod( DistoXDBlock blk0, List<DistoXDBlock> list )
  { 
    // Log.v("DistoX", "assign stations after.  size " + list.size() );
    boolean increment = true;
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DistoXDBlock prev = null;
    String from = blk0.mFrom; 
    String back = blk0.mTo;
    if ( DistoXStationName.isLessOrEqual( blk0.mFrom, blk0.mTo ) ) { // forward
      flip = true;
      // move next
      // back = blk0.mTo;
      from = DistoXStationName.increment( blk0.mTo );
    } else { // backward
      // increment = false;
      flip = false;
    }
    String next = DistoXStationName.increment( from );
    String station = from;
    // Log.v("DistoX", "*    " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );

    for ( DistoXDBlock blk : list ) {
      if ( blk.mType == DistoXDBlock.BLOCK_SPLAY ) {
        if ( flip ) { 
          flip = false;
        }
        // blk.mFrom = station;
        blk.setName( station, "" );
        mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true );  // SPLAY
        // Log.v("DistoX", "S:"+ station + "   " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
      } else if ( blk.mType == DistoXDBlock.BLOCK_MAIN_LEG ) {
        if ( blk.mId != blk0.mId ) {
          String p_from = from;
          String p_to   = next;
          if ( flip ) { // backward
            flip = false;
            p_to = back; 
          } else {  // forward
            flip = true;
            if ( increment ) {
              // move for
              back = next;
              from = DistoXStationName.increment( next ); 
              next = DistoXStationName.increment( from ); 
              station = from;
            } else {
              increment = true;
            }
          }
          blk.setName( p_from, p_to );
          mData.updateShotName( blk.mId, mSID, p_from, p_to, true ); // LEG
          // Log.v("DistoX", "L:"+from+"-"+ p_to + " " + oldFrom + " " + from + "-" + back + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
        }
      }
    }
  }

  private void assignStations_Tripod( List<DistoXDBlock> list )
  { 
    DistoXDBlock prev = null;
    String from = DistoXStationName.mSecondStation;     // 1
    String back = DistoXStationName.mInitialStation;    // 0
    String next = DistoXStationName.increment( from );  // 2
    boolean flip = true; // whether to swap leg-stations (backsight backward shot)

    String station = ( mCurrentStationName != null )? mCurrentStationName : from;
    int nrLegShots = 1;

    for ( DistoXDBlock blk : list ) {
      // Log.v("DistoX", blk.mId + " <" + blk.mFrom + ">-<" + blk.mTo + "> F " + from + " T " + back + " N " + next );
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );
        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          blk.setName( station, "" );
          mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true );  // SPLAY
          // Log.v( "DistoX", blk.mId + " FROM " + blk.mFrom + " PREV null" );
        } else {
          if ( prev.isRelativeDistance( blk ) ) {
            if ( nrLegShots == 0 ) {
              // checkCurrentStationName
              if ( mCurrentStationName != null ) {
                // if ( forward_shots ) { 
                  from = mCurrentStationName;
                // } else if ( survey_stations == 2 ) {
                //   back = mCurrentStationName;
                // }
              }
              nrLegShots = 2; // prev and this shot
            } else {
              nrLegShots ++;  // one more centerline shot
            }
            if ( nrLegShots == TDSetting.mMinNrLegShots ) {
              mCurrentStationName = null;
              // Log.v("DistoX", "P " + prev.mId + " " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
              String prev_from = from;
              String prev_to   = back;
              if ( flip ) { 
                flip = false;
              } else {         
                flip = true;
                prev_to = next;
                // move forward 
                back   = next;
                from = DistoXStationName.increment( next, list );
                next = DistoXStationName.increment( from ); 
              }
              station = from;
              // Log.v("DistoX", "P: (" + prev_from + "-" + prev_to + ") " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
              prev.setName( prev_from, prev_to );
              mData.updateShotName( prev.mId, mSID, prev_from, prev_to, true ); // LEG
              setLegExtend( prev );
            }
          } else { // distance from prev > "closeness" setting
            if ( nrLegShots == 0 ) {
              if ( flip ) {
                flip = false;
                if ( prev != null && prev.mTo.length() == 0 ) {
                  if ( ! prev.mFrom.equals( station ) ) {
                    prev.setName( station, "" );
                    mData.updateShotName( prev.mId, mSID, station, "", true ); // SPLAY
                  }
                }
              }
            } else { // only when coming from a LEG
              // if ( mCurrentStationName == null ) {
              //   station = from;
              // }
            }
            nrLegShots = 0;
            blk.setName( station, "" );
            mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true ); // SPLAY
            // Log.v( "DistoX", "non-close: b " + blk.mId + " <" + blk.mFrom + "> " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip?"y":"n") );
            prev = blk;
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mTo.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          // Log.v("DistoX", blk.mId + " [" + blk.mFrom + "-" + blk.mTo + "] " + from + "-" + back + "-" + next + " " + station );
          if ( DistoXStationName.isLessOrEqual( blk.mFrom, blk.mTo ) ) { // forward shot
            flip = true;
            back = blk.mTo;
            from = DistoXStationName.increment( back, list );
            next = DistoXStationName.increment( from );
          } else { // backward shot
            flip = false;
            from = blk.mFrom;
            back = blk.mTo;
            next = DistoXStationName.increment( from, list );
          }
          if ( mCurrentStationName == null ) station = from;
          // Log.v("DistoX", "   " + from + "-" + back + "-" + next + " " + station + " flip=" + (flip? "y":"n") );
          nrLegShots = TDSetting.mMinNrLegShots;
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          if ( nrLegShots == 0 ) {
            flip = false;
          }
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }

  private void assignStationsAfter_Backsight( DistoXDBlock blk0, List<DistoXDBlock> list )
  { 
    // Log.v("DistoX", "assign stations after.  size " + list.size() );
    boolean shot_after_splays = TDSetting.mShotAfterSplays;

    boolean increment = true;
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DistoXDBlock prev = null;
    String from = blk0.mFrom;
    String to   = blk0.mTo;
    String next;
    String station;
    if ( DistoXStationName.isLessOrEqual( blk0.mFrom, blk0.mTo ) ) { // forward
      flip    = true;
      station = to;
      next = DistoXStationName.increment( station );
    } else { // backward
      increment = false;
      flip    = false;
      station = from;
      to   = DistoXStationName.increment( from );
      next = DistoXStationName.increment( to );
    }

    String oldFrom = blk0.mFrom;
    // int nrLegShots = 0;
    // Log.v("DistoX", "*    " + oldFrom + " " + from + "-" + to + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );

    for ( DistoXDBlock blk : list ) {
      if ( blk.mType == DistoXDBlock.BLOCK_SPLAY ) {
        if ( flip ) { 
          flip = false;
        }
        // blk.mFrom = station;
        blk.setName( station, "" );
        mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true );  // SPLAY
        // Log.v("DistoX", "S:"+ station + "   " + oldFrom + " " + from + "-" + to + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
      } else if ( blk.mType == DistoXDBlock.BLOCK_MAIN_LEG ) {
        if ( blk.mId != blk0.mId ) {
          String p_to;
          if ( flip ) { // backward
            flip = false;
            p_to = oldFrom; 
            from = to;
            station = from;
          } else {  // forward
            flip = true;
            if ( increment ) {
              from = to;
              to   = next;
              next = DistoXStationName.increment( to ); 
            } else {
              increment = true;
            }
            p_to = to;
            oldFrom = from;
            station = to;
          }
          blk.setName( from, p_to );
          mData.updateShotName( blk.mId, mSID, from, p_to, true ); // LEG
          // Log.v("DistoX", "L:"+from+"-"+ p_to + " " + oldFrom + " " + from + "-" + to + "-" + next + ":" + station + " flip=" + (flip?"y":"n") );
        }
      }
    }
  }

  private void assignStations_Backsight( List<DistoXDBlock> list )
  { 
    // mSecondLastShotId = lastShotId(); // FIXME this probably not needed
    // Log.v("DistoX", "assign stations. size " + list.size() );

    DistoXDBlock prev = null;
    String from = DistoXStationName.mInitialStation;
    String to   = DistoXStationName.mSecondStation;
    String oldFrom = "empty"; // FIXME
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)

    String station = ( mCurrentStationName != null )? mCurrentStationName : from;
    // Log.v("DistoX", "assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // Log.v("DistoX", "Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );

    int nrLegShots = 0;

    for ( DistoXDBlock blk : list ) {
      // Log.v("DistoX", blk.mId + " <" + blk.mFrom + ">-<" + blk.mTo + "> F " + from + " T " + to + " OF " + oldFrom );
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );

        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          blk.setName( station, "" );
          mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true );  // SPLAY
          // Log.v( "DistoX", blk.mId + " FROM " + blk.mFrom + " PREV null" );
        } else {
          if ( prev.isRelativeDistance( blk ) ) {
            if ( nrLegShots == 0 ) {
              // checkCurrentStationName
              if ( mCurrentStationName != null ) {
                // if ( forward_shots ) { 
                  from = mCurrentStationName;
                // } else if ( survey_stations == 2 ) {
                //   to = mCurrentStationName;
                // }
              }
              nrLegShots = 2; // prev and this shot
            } else {
              nrLegShots ++;  // one more centerline shot
            }
            if ( nrLegShots == TDSetting.mMinNrLegShots ) {
              mCurrentStationName = null;
              // Log.v("DistoX", "P " + prev.mId + " " + oldFrom + "-" + from + "-" + to + "-" + station + " flip=" + (flip?"y":"n") );
              String prev_from = from;
              String prev_to   = to;
              if ( flip ) {          // 2 backsight backward shot from--old_from
                prev_to = oldFrom;   // 1
                station = from;
                flip = false;
              } else {               // 2 backsight forward shot from--to
                // prev_to = to;     // 3
                oldFrom = from;      // 2
                from    = to;        // 3
                station = to;
                to   = DistoXStationName.increment( to,list );  // next-shot-to   = increment next-shot-from          
                flip = true;
              }
              // Log.v("DistoX", "P: (" + prev_from + "-" + prev_to + ") " + oldFrom + "-" + from + "-" + to + "-" + station + " flip=" + (flip?"y":"n") );
              prev.setName( prev_from, prev_to );
              mData.updateShotName( prev.mId, mSID, prev_from, prev_to, true ); // LEG
              setLegExtend( prev );
            }
          } else { // distance from prev > "closeness" setting
            if ( nrLegShots == 0 ) {
              flip = false;
            } else { // only when coming from a LEG
              if ( mCurrentStationName == null ) {
                station = from;
              // } else {
              //   station = mCurrentStationName;
              }
            }
            nrLegShots = 0;
            blk.setName( station, "" );
            mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true ); // SPLAY
            // Log.v( "DistoX", "non-close: b " + blk.mId + " <" + blk.mFrom + "> " + oldFrom + "-" + from + "-" + to + "-" + station + " flip=" + (flip?"y":"n") );
            prev = blk;
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mTo.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          // Log.v("DistoX", blk.mId + " [" + blk.mFrom + "-" + blk.mTo + "] " + oldFrom + "-" + from + "-" + to + "-" + station );
          if ( blk.mTo.equals( oldFrom ) ) {
            flip = false;
          } else {
            flip = true;
            oldFrom = blk.mFrom;
            from    = blk.mTo;
            to      = DistoXStationName.increment( from, list );
            if ( mCurrentStationName == null ) {
              station = blk.mTo;
            } // otherwise station = mCurrentStationName
          }
          // Log.v("DistoX", "   " + oldFrom + "-" + from + "-" + to + "-" + station + " flip=" + (flip? "y":"n") );
          nrLegShots = TDSetting.mMinNrLegShots;
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          if ( nrLegShots == 0 ) flip = false;
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }
  
  private void assignStationsAfter_Default( DistoXDBlock blk0, List<DistoXDBlock> list )
  {
    // Log.v("DistoX", "assign stations after.  size " + list.size() );
    int survey_stations = TDSetting.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splays = TDSetting.mShotAfterSplays;

    boolean increment = true;
    boolean flip = false; // whether to swap leg-stations (backsight backward shot)
    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DistoXDBlock prev = null;
    String from = blk0.mFrom;
    String to   = blk0.mTo;
    String next;
    String station;
    if ( forward_shots ) {
      next = DistoXStationName.increment( to );
      station = shot_after_splays ? to : from;
    } else {
      next = DistoXStationName.increment( from );
      station = shot_after_splays ? next : from;
    }

    // int nrLegShots = 0;
    for ( DistoXDBlock blk : list ) {
      if ( blk.mType == DistoXDBlock.BLOCK_SPLAY ) {
        // blk.mFrom = station;
        blk.setName( station, "" );
        mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true );  // SPLAY
      } else if ( blk.mType == DistoXDBlock.BLOCK_MAIN_LEG ) {
        if ( blk.mId != blk0.mId ) {
          if ( forward_shots ) {
            from = to;
            to   = next;
            next = DistoXStationName.increment( to );
            station = shot_after_splays ? to : from;
          } else {
            to   = from;
            from = next;
            next = DistoXStationName.increment( from );
            station = shot_after_splays ? next : from;
          }
          // blk.mFrom = from;
          // blk.mTo   = to;
          blk.setName( from, to );
          mData.updateShotName( blk.mId, mSID, from, to, true );  // SPLAY
        }
      }
    }
  }

  private void assignStations_Default( List<DistoXDBlock> list )
  { 
    // mSecondLastShotId = lastShotId(); // FIXME this probably not needed
    // Log.v("DistoX", "assign stations. size " + list.size() );
    int survey_stations = TDSetting.mSurveyStations;
    if ( survey_stations <= 0 ) return;
    boolean forward_shots = ( survey_stations == 1 );
    boolean shot_after_splay = TDSetting.mShotAfterSplays;

    // TDLog.Log( TDLog.LOG_DATA, "assign Stations() policy " + survey_stations + "/" + shot_after_splay  + " nr. shots " + list.size() );

    DistoXDBlock prev = null;
    String from = ( forward_shots )? DistoXStationName.mInitialStation  // next FROM station
                                   : DistoXStationName.mSecondStation;
    String to   = ( forward_shots )? DistoXStationName.mSecondStation   // nect TO station
                                   : DistoXStationName.mInitialStation;
    String station = ( mCurrentStationName != null )? mCurrentStationName
                   : (shot_after_splay ? from : "");  // splays station
    // Log.v("DistoX", "assign stations: F <" + from + "> T <" + to + "> st. <" + station + "> Blk size " + list.size() );
    // Log.v("DistoX", "Current St. " + ( (mCurrentStationName==null)? "null" : mCurrentStationName ) );

    int nrLegShots = 0;

    for ( DistoXDBlock blk : list ) {
      if ( blk.mFrom.length() == 0 ) // this implies blk.mTo.length() == 0
      {
        // Log.v( "DistoX", blk.mId + " EMPTY FROM. prev " + ( (prev==null)? "null" : prev.mId ) );

        if ( prev == null ) {
          prev = blk;
          // blk.mFrom = station;
          blk.setName( station, "" );
          mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true );  // SPLAY
          // Log.v( "DistoX", blk.mId + " FROM " + blk.mFrom + " PREV null" );
        } else {
          if ( prev.isRelativeDistance( blk ) ) {
            if ( nrLegShots == 0 ) {
              // checkCurrentStationName
              if ( mCurrentStationName != null ) {
                if ( forward_shots ) { 
                  from = mCurrentStationName;
                } else if ( survey_stations == 2 ) {
                  to = mCurrentStationName;
                }
              }
              nrLegShots = 2; // prev and this shot
            } else {
              nrLegShots ++;  // one more centerline shot
            }
            if ( nrLegShots == TDSetting.mMinNrLegShots ) {
              mCurrentStationName = null;
              // Log.v( "DistoX", "PREV " + prev.mId + " nrLegShots " + nrLegShots + " set PREV " + from + "-" + to );
              prev.setName( from, to );
              mData.updateShotName( prev.mId, mSID, from, to, true ); // LEG
              setLegExtend( prev );
              if ( forward_shots ) {
                station = shot_after_splay  ? to : from;     // splay-station = this-shot-to if splays before shot
                                                             //                 this-shot-from if splays after shot
                from = to;                                   // next-shot-from = this-shot-to
                to   = DistoXStationName.increment( to, list );  // next-shot-to   = increment next-shot-from
                // Log.v("DistoX", "station [1] " + station + " FROM " + from + " TO " + to );
              } else { // backward_shots
                to   = from;                                     // next-shot-to   = this-shot-from
                from = DistoXStationName.increment( from,list ); // next-shot-from = increment this-shot-from
                station = shot_after_splay ? from : to;          // splay-station  = next-shot-from if splay before shot
                                                                 //                = this-shot-from if splay after shot
                // Log.v("DistoX", "station [2] " + station + " FROM " + from + " TO " + to );
              }
            }
          } else { // distance from prev > "closeness" setting
            nrLegShots = 0;
            blk.setName( station, "" );
            mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true ); // SPLAY
            prev = blk;
          }
        }
      }
      else // blk.mFrom.length > 0
      {
        if ( blk.mTo.length() > 0 ) // FROM non-empty, TO non-empty --> LEG
        {
          if ( forward_shots ) {  // : ..., 0-1, 1-2 ==> from=(2) to=Next(2)=3 ie 2-3
            from = blk.mTo;
            to   = DistoXStationName.increment( from, list );
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? blk.mTo    // 1,   1, 1-2, [ 2, 2, ..., 2-3 ] ...
                                         : blk.mFrom; // 1-2, 1, 1,   [ 2-3, 2, 2, ... ] ...
            } // otherwise station = mCurrentStationName
          } else { // backward shots: ..., 1-0, 2-1 ==> from=Next(2)=3 to=2 ie 3-2
            to      = blk.mFrom;
            from    = DistoXStationName.increment( to, list ); // FIXME it was from
            if ( mCurrentStationName == null ) {
              station = shot_after_splay ? from       // 2,   2, 2, 2-1, [ 3, 3, ..., 3-2 ]  ...
                                         : blk.mFrom; // 2-1, 2, 2, 2,   [ 3-2, 3, 3, ... 3 ] ...
            } // otherwise station = mCurrentStationName
          }
          nrLegShots = TDSetting.mMinNrLegShots;
        } 
        else // FROM non-empty, TO empty --> SPLAY
        {
          nrLegShots = 0;
        }
        prev = blk;
      }
    }
  }

  // ================================================================
  // EXPORTS

  public String exportSurveyAsCsx( DrawingActivity sketch, String origin )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) return null;
    String filename = ( sketch == null )? TDPath.getSurveyCsxFile(mySurvey)
                                        : TDPath.getSurveyCsxFile(mySurvey, sketch.mName1);
    return TopoDroidExporter.exportSurveyAsCsx( mSID, mData, info, sketch, origin, filename );
  }

  public String exportSurveyAsTop( DrawingActivity sketch, String origin )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export TOP null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyTopFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsTop( mSID, mData, info, sketch, origin, filename );
  }

  public String exportSurveyAsTh( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export TH null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyThFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsTh( mSID, mData, info, filename );
  }

  public String exportSurveyAsPlg()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export PLG null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyCaveFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsPlg( mSID, mData, info, filename );
  }

  public String exportSurveyAsCav()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export CAV null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyCavFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsCav( mSID, mData, info, filename );
  }

  public String exportSurveyAsSvx()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export SVX null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveySvxFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsSvx( mSID, mData, info, mDevice, filename );
  }

  public String exportSurveyAsTro()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export TRO null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyTroFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsTro( mSID, mData, info, filename );
  }

  public String exportSurveyAsCsv( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export CSV null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyCsvFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsCsv( mSID, mData, info, filename );
  }

  public String exportSurveyAsSrv()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export SRV null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveySrvFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsSrv( mSID, mData, info, filename );
  }

  public String exportSurveyAsDxf( DistoXNum num )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export DXF null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyDxfFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsDxf( mSID, mData, info, num, filename );
  }

  public String exportSurveyAsKml( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export KML null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyKmlFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsKml( mSID, mData, info, filename );
  }

  public String exportSurveyAsPlt( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export PLT null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyPltFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsPlt( mSID, mData, info, filename );
  }

  public String exportSurveyAsDat()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    if ( info == null ) {
      TDLog.Error("Export DAT null survey info. sid " + mSID );
      return null;
    }
    String filename = TDPath.getSurveyDatFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsDat( mSID, mData, info, filename );
  }



  public String exportCalibAsCsv( )
  {
    if ( mCID < 0 ) return null;
    CalibInfo ci = mDData.selectCalibInfo( mCID );
    if ( ci == null ) return null;
    TDPath.checkCCsvDir();
    String filename = TDPath.getCCsvFile( ci.name );
    return TopoDroidExporter.exportCalibAsCsv( mCID, mDData, ci, filename );
  }

  // ----------------------------------------------
  // FIRMWARE and USER MANUAL

  private void installFirmware( boolean overwrite )
  {
    InputStream is = getResources().openRawResource( R.raw.firmware );
    firmwareUncompress( is, overwrite );
    try { is.close(); } catch ( IOException e ) { }
  }
 
  // private void installUserManual()
  // {
  //   InputStream is = getResources().openRawResource( R.raw.manual ); // res/raw/manual.zip
  //   userManualUncompress( is );
  //   try { is.close(); } catch ( IOException e ) { }
  // }
 
  // -------------------------------------------------------------
  // SYMBOLS

  void installSymbols( boolean overwrite )
  {
    boolean install = overwrite;
    askSymbolUpdate = false;
    if ( ! overwrite ) { // check whether to install
      String version = mDData.getValue( "symbol_version" );
      // Log.v("DistoX", "symbol version <" + version + "> SYMBOL_VERSION <" + SYMBOL_VERSION + ">" );
      if ( version == null ) {
        install = true;
      } else if ( ! version.equals(SYMBOL_VERSION) ) {
        askSymbolUpdate = true;
      } else { // version .equals SYMBOL_VERSION
        return;
      }
      mDData.setValue( "symbol_version", SYMBOL_VERSION );
    }
    if ( install ) {
      deleteObsoleteSymbols();
      InputStream is = getResources().openRawResource( R.raw.symbols );
      symbolsUncompress( is, overwrite );
    }
  }

  private void deleteObsoleteSymbols()
  {
    String lines[] = { "blocks", "debris", "clay", "presumed", "sand", "ice" };
    for ( String line : lines ) {
      File file = new File( TDPath.APP_LINE_PATH + line );
      if ( file.exists() ) file.delete();
    }
  }

  private void symbolsUncompress( InputStream fis, boolean overwrite )
  {
    // Log.v(TAG, "symbol uncompress ...");
    TDPath.symbolsCheckDirs();
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( filepath.endsWith("README") ) continue;
        // Log.v( TAG, "ZipEntry " + filepath );
        if ( ! ze.isDirectory() ) {
          if ( filepath.startsWith( "symbol" ) ) {
            filepath = filepath.substring( 7 );
          }
          String pathname = TDPath.getSymbolFile( filepath );
          File file = new File( pathname );
          if ( overwrite || ! file.exists() ) {
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

  private void firmwareUncompress( InputStream fis, boolean overwrite )
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

  // private void userManualUncompress( InputStream fis )
  // {
  //   // Log.v(TAG, "user-manual uncompress ...");
  //   TDPath.checkManDir( );
  //   try {
  //     // byte buffer[] = new byte[36768];
  //     byte buffer[] = new byte[4096];
  //     ZipEntry ze = null;
  //     ZipInputStream zin = new ZipInputStream( fis );
  //     while ( ( ze = zin.getNextEntry() ) != null ) {
  //       String filepath = ze.getName();
  //       if ( ze.isDirectory() ) continue;
  //       if ( ! filepath.endsWith("bin") ) continue;
  //       String pathname =  TDPath.getManFile( filepath );
  //       File file = new File( pathname );
  //       TDPath.checkPath( pathname );
  //       FileOutputStream fout = new FileOutputStream( pathname );
  //       int c;
  //       while ( ( c = zin.read( buffer ) ) != -1 ) {
  //         fout.write(buffer, 0, c); // offset 0 in buffer
  //       }
  //       fout.close();
  //       zin.closeEntry();
  //     }
  //     zin.close();
  //   } catch ( FileNotFoundException e ) {
  //   } catch ( IOException e ) {
  //   }
  // }

  // ---------------------------------------------------------

  private long addManualSplays( long at, String splay_station, String left, String right, String up, String down,
                                float bearing, boolean horizontal )
  {
    long id;
    long extend = 0L;
    if ( left != null && left.length() > 0 ) {
      float l = -1.0f;
      try {
        l = Float.parseFloat( left ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: left " + left );
      }
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
        mData.updateShotName( id, mSID, splay_station, "", true );
      }
    } 
    if ( right != null && right.length() > 0 ) {
      float r = -1.0f;
      try {
        r = Float.parseFloat( right ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: right " + right );
      }
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
        mData.updateShotName( id, mSID, splay_station, "", true );
      }
    }
    if ( up != null && up.length() > 0 ) {
      float u = -1.0f;
      try {
        u = Float.parseFloat( up ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: up " + up );
      }
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
        mData.updateShotName( id, mSID, splay_station, "", true );
      }
    }
    if ( down != null && down.length() > 0 ) {
      float d = -1.0f;
      try {
        d = Float.parseFloat( down ) / TDSetting.mUnitLength;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: down " + down );
      }
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
        mData.updateShotName( id, mSID, splay_station, "", true );
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
  public DistoXDBlock insertManualShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend0,
                           String left, String right, String up, String down,
                           String splay_station )
  {
    mSecondLastShotId = lastShotId();
    DistoXDBlock ret = null;
    long id;

    distance = (distance - mManualCalibrationLength)  / TDSetting.mUnitLength;
    clino    = (clino    - mManualCalibrationClino)   / TDSetting.mUnitAngle;
    float b  = bearing / TDSetting.mUnitAngle;

    if ( ( distance < 0.0f ) ||
         ( clino < -90.0f || clino > 90.0f ) ||
         ( b < 0.0f || b >= 360.0f ) ) {
      Toast.makeText( this, R.string.illegal_data_value, Toast.LENGTH_SHORT ).show();
      return null;
    }
    bearing = (bearing  - mManualCalibrationAzimuth) / TDSetting.mUnitAngle;
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
          mData.updateShotExtend( id, mSID, extend0, true ); // FIXME-EXTEND maybe not needed
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
          mData.updateShotExtend( id, mSID, extend0, true ); // FIXME-EXTEND maybe not needed
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
    if ( mComm == null || mDevice == null ) return -1;
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
    TDLog.Log( TDLog.LOG_PLOT, "new plot " + name + " start " + start );
    long pid_p = mData.insertPlot( sid, -1L, name+"p",
                 PlotInfo.PLOT_PLAN, 0L, start, "", 0, 0, mScaleFactor, 0, 0, "", true );
    if ( extended ) {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotInfo.PLOT_EXTENDED, 0L, start, "", 0, 0, mScaleFactor, 0, 0, "", true );
    } else {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotInfo.PLOT_PROFILE, 0L, start, "", 0, 0, mScaleFactor, project, 0, "", true );
    }
    return pid_p;
  }
  
  long insert2dSection( long sid, String name, long type, String from, String to, float azimuth, float clino )
  {
    // FIXME COSURVEY 2d sections are not forwarded
    return mData.insertPlot( sid, -1L, name, type, 0L, from, to, 0, 0, TopoDroidApp.mScaleFactor, azimuth, clino, "", false );
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
    return ( mSyncConn == null )? "" : mSyncConn.getConnectionStateTitleStr();
  }

  void connStateChanged()
  {
    // Log.v( "DistoX", "connStateChanged()" );
    if ( mSurveyActivity != null ) mSurveyActivity.setTheTitle();
    if ( mShotActivity  != null) mShotActivity.setTheTitle();
    if ( mActivity != null ) mActivity.setTheTitle();
  }

  void refreshUI()
  {
    if ( mSurveyActivity != null ) mSurveyActivity.updateDisplay();
    if ( mShotActivity  != null) mShotActivity.updateDisplay();
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
