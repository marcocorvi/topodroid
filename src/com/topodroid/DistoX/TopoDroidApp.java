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
import java.io.StringWriter;
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

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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

  static String SYMBOL_VERSION = "07"; 
  static String VERSION = "0.0.0"; 
  static int VERSION_CODE = 0;
  static int MAJOR = 0;
  static int MINOR = 0;
  static int SUB   = 0;
  static final int MAJOR_MIN = 2; // minimum compatible version
  static final int MINOR_MIN = 1;
  static final int SUB_MIN   = 1;
  
  // static boolean mHideHelp = false;
  static boolean VERSION30 = true;

  boolean mWelcomeScreen;  // whether to show the welcome screen
  static String mManual;  // manual url
  static Locale mLocale;

  public static float mScaleFactor   = 1.0f;
  public static float mDisplayWidth  = 200f;
  public static float mDisplayHeight = 320f;

  static boolean isTracing = false;

  // ----------------------------------------------------------------------
  // DataListener
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
  private DistoXComm mComm = null;        // BT communication
  DataDownloader mDataDownloader = null;  // data downloader
  static DataHelper mData = null;         // database 

  SurveyActivity mSurveyActivity = null;
  ShotActivity mShotActivity  = null;
  TopoDroidActivity mActivity = null; 

  // static final int DISTO_NONE = 0;  // supported Disto types
  // static final int DISTO_A3 = 1;
  // static final int DISTO_X310 = 2;
  // static int mDistoType = DISTO_A3;

  long mSID   = -1;   // id of the current survey
  long mCID   = -1;   // id of the current calib
  String mySurvey;   // current survey name
  String myCalib;    // current calib name
  Calibration mCalibration    = null;     // current calibration 
  static long mSecondLastShotId = 0L;

  public long lastShotId( ) { return mData.getLastShotId( mSID ); }
  public long secondLastShotId( ) { return mSecondLastShotId; }

  static final  String DEVICE_NAME    = "";
  Device mDevice = null;
  int    distoType() { return (mDevice == null)? 0 : mDevice.mType; }
  String distoAddress() { return (mDevice == null)? null : mDevice.mAddress; }


  int setListViewHeight( HorizontalListView listView )
  {
    return TopoDroidApp.setListViewHeight( this, listView );
  }

  static int setListViewHeight( Context context, HorizontalListView listView )
  {
    final float scale = context.getResources().getSystem().getDisplayMetrics().density;
    LayoutParams params = listView.getLayoutParams();
    int size = (int)( 42 * TopoDroidSetting.mSizeButtons * scale );
    params.height = size + 10;
    listView.setLayoutParams( params );
    return size;
  }

  static int getDefaultSize( Context context )
  {
    return (int)(42 * context.getResources().getSystem().getDisplayMetrics().density );
  }

  BitmapDrawable setButtonBackground( Button button, int size, int id )
  {
    return TopoDroidApp.setButtonBackground( this, button, size, id );
  }

  static BitmapDrawable setButtonBackground( Context context, Button button, int size, int id )
  {
    Bitmap bm1 = BitmapFactory.decodeResource( context.getResources(), id );
    BitmapDrawable bm2 = new BitmapDrawable( context.getResources(), Bitmap.createScaledBitmap( bm1, size, size, false ) );
    if ( button != null ) button.setBackgroundDrawable( bm2 );
    return bm2;
  }

  // ------------------------------------------------------------

  static float mAccelerationMean = 0.0f;
  static float mMagneticMean     = 0.0f;
  static float mDipMean          = 0.0f;

  static boolean isBlockAcceptable( float acc, float mag, float dip )
  {
    return true
        && Math.abs( acc - TopoDroidApp.mAccelerationMean ) < TopoDroidSetting.mAccelerationThr
        && Math.abs( mag - TopoDroidApp.mMagneticMean ) < TopoDroidSetting.mMagneticThr
        && Math.abs( dip - TopoDroidApp.mDipMean ) < TopoDroidSetting.mDipThr
    ;
  }

  // ------------------------------------------------------------
  // CONSTS
  // private static final byte char0C = 0x0c;


  // intent extra-names
  public static final String TOPODROID_PLOT_ID     = "topodroid.plot_id";
  public static final String TOPODROID_PLOT_ID2    = "topodroid.plot_id2";
  public static final String TOPODROID_PLOT_NAME   = "topodroid.plot_name";
  public static final String TOPODROID_PLOT_NAME2  = "topodroid.plot_name2";
  public static final String TOPODROID_PLOT_TYPE   = "topodroid.plot_type";
  public static final String TOPODROID_PLOT_FROM   = "topodroid.plot_from";
  public static final String TOPODROID_PLOT_TO     = "topodroid.plot_to";
  public static final String TOPODROID_PLOT_AZIMUTH = "topodroid.plot_azimuth";
  public static final String TOPODROID_PLOT_CLINO  = "topodroid.plot_clino";

  // FIXME_SKETCH_3D
  public static final String TOPODROID_SKETCH_ID   = "topodroid.sketch_id";
  public static final String TOPODROID_SKETCH_NAME = "topodroid.sketch_name";
  // END_SKETCH_3D

  public static final String TOPODROID_SURVEY      = "topodroid.survey";
  public static final String TOPODROID_OLDSID      = "topodroid.old_sid";    // SurveyActivity
  public static final String TOPODROID_OLDID       = "topodroid.old_id";
  public static final String TOPODROID_SURVEY_ID   = "topodroid.survey_id";  // DrawingActivity
  
  public static final String TOPODROID_DEVICE_ACTION = "topodroid.device_action";
  // public static final String TOPODROID_DEVICE_ADDR   = "topodroid.device_addr";
  // public static final String TOPODROID_DEVICE_CNCT   = "topodroid.device_cnct";

  public static final String TOPODROID_SENSOR_TYPE  = "topodroid.sensor_type";
  public static final String TOPODROID_SENSOR_VALUE = "topodroid.sensor_value";
  public static final String TOPODROID_SENSOR_COMMENT = "topodroid.sensor_comment";

  public static final String TOPODROID_CWD = "topodroid.cwd";


  // ---------------------------------------------------------------
  // ConnListener
  ArrayList< Handler > mConnListener;

  void registerConnListener( Handler hdl )
  {
    if ( hdl != null ) {
      mConnListener.add( hdl );
      // try {
      //   new Messenger( hdl ).send( new Message() );
      // } catch ( RemoteException e ) { }
    }
  }

  void unregisterConnListener( Handler hdl )
  {
    if ( hdl != null ) {
      // try {
      //   new Messenger( hdl ).send( new Message() );
      // } catch ( RemoteException e ) { }
      mConnListener.remove( hdl );
    }
  }

  private void notifyConnState( int w )
  {
    // Log.v( TAG, "notify conn state" );
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
    CalibInfo info = mData.selectCalibInfo( mCID  );
    TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "info.device " + ((info == null)? "null" : info.device) );
    TopoDroidLog.Log( TopoDroidLog.LOG_CALIB, "device " + ((mDevice == null)? "null" : mDevice.mAddress) );
    return ( mDevice == null || ( info != null && info.device.equals( mDevice.mAddress ) ) );
  }

  static String noSpaces( String s )
  {
    return ( s == null )? null 
      : s.trim().replaceAll("\\s+", "_").replaceAll("/", "-").replaceAll("\\*", "+").replaceAll("\\\\", "");
  }

  static void checkPath( String filename )
  {
    if ( filename == null ) return;
    File fp = new File( filename );
    checkPath( new File( filename ) );
  }

  static void checkPath( File fp ) 
  {
    if ( fp == null || fp.exists() ) return;
    File fpp = fp.getParentFile();
    if ( fpp.exists() ) return;
    fpp.mkdirs(); // return boolean : must check ?
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
    if ( mData == null ) return null;
    return mData.selectCalibInfo( mCID );
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
          mData.updateDeviceModel( device.mAddress, "DistoX" );
          device.mType = model;
        } else if ( model == Device.DISTO_X310 ) {
          mData.updateDeviceModel( device.mAddress, "DistoX-0000" );
          device.mType = model;
        }
      }
    }
  }

  // called by DeviceActivity::onResume()
  public void resumeComm()
  {
    if ( mComm != null ) mComm.resume();
  }

  public void suspendComm()
  {
    if ( mComm != null ) mComm.suspend();
  }

  public void resetComm() 
  { 
    mComm.disconnectRemoteDevice( );
    mComm = null;
    mComm = new DistoXComm( this );
  }

  // called by DeviceActivity::setState()
  //           ShotActivity::onResume()
  public boolean isCommConnected()
  {
    // return mComm != null && mComm.mBTConnected;
    return mComm != null && mComm.mBTConnected && mComm.mRfcommThread != null;
  }

  void disconnectRemoteDevice( boolean force )
  {
    // Log.v("DistoX", "App disconnect RemoteDevice listers " + mDataDownloader.mLister.size() );
    if ( force || mDataDownloader.mLister.size() == 0 ) {
      if ( mComm != null && mComm.mBTConnected ) mComm.disconnectRemoteDevice( );
    }
  }

  // void connectRemoteDevice( String address, ArrayList<ILister> listers )
  // {
  //   if ( mComm != null ) mComm.connectRemoteDevice( address, listers );
  // }

  // FIXME_COMM
  public boolean connect( String address, ArrayList<ILister> listers ) 
  {
    return mComm != null && mComm.connect( address, listers );
  }

  public void disconnect()
  {
    if ( mComm != null ) mComm.disconnect();
  }
  // end FIXME_COMM


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

  static boolean mEnableZip = true;    // whether zip saving is enabled or must wait (locked by th2. saving thread)
  static boolean mSketches = false;        // whether to use 3D models

  // ---------------------------------------------------------

  @Override
  public void onCreate()
  {
    super.onCreate();

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
    // // disable lock
    // KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Activity.KEYGUARD_SERVICE);
    // KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
    // lock.disableKeyguard();

    // try {
    //   mScreenTimeout = System.getInt(getContentResolver(), System.SCREEN_OFF_TIMEOUT );
    // } catch ( SettingNotFoundException e ) {
    // }


    // Log.v(TAG, "onCreate app");
    this.mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
    this.mPrefs.registerOnSharedPreferenceChangeListener( this );

    TopoDroidSetting.loadPreferences( this, mPrefs );

    TopoDroidPath.setDefaultPaths();
    mCWD = mPrefs.getString( "DISTOX_CWD", "TopoDroid" );
    TopoDroidPath.setPaths( mCWD );

    mWelcomeScreen = mPrefs.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true

    mEnableZip = true;
    
    mDataListeners = new ArrayList<DataListener>();
    mData = new DataHelper( this, mDataListeners );

    String version = mData.getValue( "version" );
    if ( version == null || ( ! version.equals(VERSION) ) ) {
      mData.setValue( "version", VERSION );
      // FIXME MANUAL installManual( );  // must come before installSymbols
      installSymbols( false ); // this updates symbol_version in the database
      installFirmware( false );
    }

    {
      String value = mData.getValue("sketch");
      mSketches =  value != null 
                && value.equals("on")
                && getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
      // Log.v("DistoX", "Sketch value <" + value + ">");

      value = mData.getValue("cosurvey");
      mCosurvey =  value != null && value.equals("on");
      // Log.v("DistoX", "Cosurvey value <" + value + ">");
      setCoSurvey( false );
      setBooleanPreference( "DISTOX_COSURVEY", false );
    }

    mSyncConn = new ConnectionHandler( this );

    mDevice = mData.getDevice( mPrefs.getString( TopoDroidSetting.keyDeviceName(), DEVICE_NAME ) );

    // DrawingBrushPaths.makePaths( getResources() );

    mCalibration = new Calibration( 0, this, false );

    mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    // if ( mBTAdapter == null ) {
    //   // Toast.makeText( this, R.string.not_available, Toast.LENGTH_SHORT ).show();
    //   // finish(); // FIXME
    //   // return;
    // }
    mConnListener = new ArrayList< Handler >();

    mComm = new DistoXComm( this );

    mDataDownloader = new DataDownloader( this, this );

    DistoXConnectionError = new String[5];
    DistoXConnectionError[0] = getResources().getString( R.string.distox_err_ok );
    DistoXConnectionError[1] = getResources().getString( R.string.distox_err_headtail );
    DistoXConnectionError[2] = getResources().getString( R.string.distox_err_headtail_io );
    DistoXConnectionError[3] = getResources().getString( R.string.distox_err_headtail_eof );
    DistoXConnectionError[4] = getResources().getString( R.string.distox_err_connected );
    
    // WindowManager wm = (WindowManager)getSystemService( Context.WINDOW_SERVICE );
    // Display d = wm.getDefaultDisplay();
    // Point s = new Point();
    // d.getSize( s );
    // Log.v( TAG, "display " + d.getWidth() + " " + d.getHeight() );
    // mDisplayWidth  = d.getWidth();
    // mDisplayHeight = d.getHeight();
    DisplayMetrics dm = getResources().getDisplayMetrics();
    float density  = dm.density;
    mDisplayWidth  = dm.widthPixels;
    mDisplayHeight = dm.heightPixels;
    mScaleFactor   = (mDisplayHeight / 320.0f) * density;
    // Log.v( "DistoX", "display " + mDisplayWidth + " " + mDisplayHeight + " scale " + mScaleFactor );

    TopoDroidLog.setLogTarget();
    TopoDroidLog.loadLogPreferences( mPrefs );

    mManual = getResources().getString( R.string.topodroid_man );

    if ( TopoDroidLog.LOG_DEBUG ) {
      isTracing = true;
      Debug.startMethodTracing("DISTOX");
    }
  }

  void setLocale( String locale )
  {
    mLocale = (locale.equals(""))? Locale.getDefault() : new Locale( locale );
    Resources res = getResources();
    DisplayMetrics dm = res.getDisplayMetrics();
    Configuration conf = res.getConfiguration();
    conf.locale = mLocale;
    res.updateConfiguration( conf, dm );
    if ( mActivity != null ) mActivity.setMenuAdapter( res );
  }

  void setCWD( String cwd )
  {
    if ( cwd == null || cwd.length() == 0 || cwd.equals( mCWD ) ) return;
    mCWD = cwd;
    TopoDroidPath.setPaths( mCWD );
    mData.openDatabase();
  }

// -----------------------------------------------------------------

  // called by GMActivity and by CalibCoeffDialog 
  void uploadCalibCoeff( Context context, byte[] coeff )
  {
    if ( mComm == null || mDevice == null ) {
      Toast.makeText( context, R.string.no_device_address, Toast.LENGTH_SHORT ).show();
    } else if ( ! checkCalibrationDeviceMatch() ) {
      Toast.makeText( context, R.string.calib_device_mismatch, Toast.LENGTH_SHORT ).show();
    } else if ( ! mComm.writeCoeff( distoAddress(), coeff ) ) {
      Toast.makeText( context, R.string.write_failed, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText( context, R.string.write_ok, Toast.LENGTH_SHORT).show();
    }
  }

  boolean readCalibCoeff( byte[] coeff )
  {
    if ( mComm == null || mDevice == null ) return false;
    mComm.readCoeff( mDevice.mAddress, coeff );
    return true;
  }

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
      String filename = TopoDroidPath.getManifestFile();
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s\n", VERSION );
      pw.format( "%s\n", DataHelper.DB_VERSION );
      pw.format( "%s\n", info.name );
      SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
      pw.format("%s\n", sdf.format( new Date() ) );
      fw.flush();
      fw.close();
    } catch ( FileNotFoundException e ) {
      // FIXME
    } catch ( IOException e ) {
      // FIXME
    }
  }

  public int checkManifestFile( String filename, String surveyname )
  {
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
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse error: major/minor " + ver[0] + " " + ver[1] );
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
        TopoDroidLog.Log( TopoDroidLog.LOG_ZIP, "TopDroid version mismatch: found " + line + " expected " + VERSION );
        return -2;
      }
      line = br.readLine().trim();
      int db_version = 0;
      try {
        db_version = Integer.parseInt( line );
      } catch ( NumberFormatException e ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse error: db_version " + line );
      }
      
      if ( ! ( db_version >= DataHelper.DATABASE_VERSION_MIN && db_version <= DataHelper.DATABASE_VERSION ) ) {
        TopoDroidLog.Log( TopoDroidLog.LOG_ZIP, "TopDroid DB version mismatch: found " + line + " expected " + DataHelper.DB_VERSION );
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

  public long setSurveyFromName( String survey, boolean forward ) 
  { 
    mSID = -1;       // no survey by default
    mySurvey = null;
    clearCurrentStations();

    if ( survey != null && mData != null ) {
      // Log.v( "DistoX", "setSurveyFromName <" + survey + "> forward " + forward );

      mSID = mData.setSurvey( survey, forward );
      // mFixed.clear();
      mySurvey = null;
      if ( mSID > 0 ) {
        mySurvey = survey;
        mSecondLastShotId = lastShotId();
        // restoreFixed();
        if ( mShotActivity  != null) {
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
    return ( mData == null ) || mData.hasCalibName( name );
  }

  public long setCalibFromName( String calib ) 
  {
    mCID = -1;
    myCalib = null;
    if ( calib != null && mData != null ) {
      mCID = mData.setCalib( calib );
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
  //  if ( mData != null ) {
  //     myCalib = mData.getCalibFromId( id );
  //     mCID = ( myCalib == null )? 0 : id;
  //   }
  // }

  // -----------------------------------------------------------------
  // PREFERENCES

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
    TopoDroidSetting.checkPreference( sp, k, mActivity, this );
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
      mDevice = mData.getDevice( address );
    }
    if ( mPrefs != null ) {
      Editor editor = mPrefs.edit();
      editor.putString( TopoDroidSetting.keyDeviceName(), address ); 
      editor.commit();
    }
  }

  // -------------------------------------------------------------
  // DATA DOWNLOAD

  public int downloadData( ArrayList<ILister> listers )
  {
    mSecondLastShotId = lastShotId();
    TopoDroidLog.Log( TopoDroidLog.LOG_DATA, "downloadData() device " + mDevice + " comm " + mComm.toString() );
    int ret = 0;
    if ( mComm != null && mDevice != null ) {
      ret = mComm.downloadData( mDevice.mAddress, listers );
      // Log.v( TAG, "TopoDroidApp.downloadData() result " + ret );
      if ( ret > 0 && TopoDroidSetting.mSurveyStations > 0 ) {
        // FIXME TODO select only shots after the last leg shots
        List<DistoXDBlock> list = mData.selectAllShots( mSID, STATUS_NORMAL );
        assignStations( list );
      }
    } else {
      TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "Comm or Device is null ");
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
    if ( TopoDroidSetting.mSurveyStations == 1 ) return last.mTo;  // forward-shot
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



  // called also by ShotActivity::updataBlockList
  public void assignStations( List<DistoXDBlock> list )
  { 
    if ( TopoDroidSetting.mSurveyStations <= 0 ) return;
    // TopoDroidLog.Log( TopoDroidLog.LOG_DATA, "assignStations() policy " + mSurveyStations + "/" + mShotAfterSplays + " nr. shots " + list.size() );
    // Log.v( "DistoX", "assignStations() policy " + mSurveyStations + "/" + mShotAfterSplays + " nr. shots " + list.size() );
    // assign stations
    DistoXDBlock prev = null;
    String from = (TopoDroidSetting.mSurveyStations == 1 )? "0" : "1";
    String to   = (TopoDroidSetting.mSurveyStations == 1 )? "1" : "0";
    String station = TopoDroidSetting.mShotAfterSplays? from : "";  // splays station
    // Log.v("DistoX", "station [0] " + station );

    int atStation = 0;
    for ( DistoXDBlock blk : list ) {
      if ( blk.mFrom.length() == 0 ) {
        // Log.v( "DistoX", "Id " + blk.mId + " FROM is empty ");

        if ( prev == null ) {
          prev = blk;
          blk.mFrom = station;
          // Log.v( "DistoX", "Id " + blk.mId + " null prev. FROM " + blk.mFrom );
          mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true );  // SPLAY
        } else {
          if ( prev.relativeDistance( blk ) < TopoDroidSetting.mCloseDistance ) {
            if ( atStation == 0 ) {
              // checkCurrentStationName
              if ( mCurrentStationName != null ) {
                if ( TopoDroidSetting.mSurveyStations == 1 ) { // forward-shot
                  from = mCurrentStationName;
                } else if ( TopoDroidSetting.mSurveyStations == 2 ) {
                  to = mCurrentStationName;
                }
                mCurrentStationName = null;
              }
              atStation = 2;
            } else { /* centerline extra shot */
              atStation ++;
            }
            if ( atStation == TopoDroidSetting.mMinNrLegShots ) {
              prev.mFrom = from;                             // forward-shot from--to
              prev.mTo   = to;
              // Log.v( "DistoX", "Id " + prev.mId + " setting prev. FROM " + from + " TO " + to );
              mData.updateShotName( prev.mId, mSID, from, to, true ); // LEG
              if ( ! TopoDroidSetting.mSplayExtend ) {
                long extend = ( prev.mBearing < 180.0 )? 1L : -1L;
                mData.updateShotExtend( prev.mId, mSID, extend, true );
              }
              if ( TopoDroidSetting.mSurveyStations == 1 ) {                  // forward-shot
                station = TopoDroidSetting.mShotAfterSplays ? to : from;      // splay-station = this-shot-to if splays before shot
                // Log.v("DistoX", "station [1] " + station + " from " + from + " to " + to );
                                                             //                 this-shot-from if splays after shot
                from = to;                                   // next-shot-from = this-shot-to
                
                do {
                  to   = DistoXStationName.increment( to );  // next-shot-to   = increment next-shot-from
                } while ( DistoXStationName.listHasName( list, to ) );
              } else {                                       // backward-shot 
                to   = from;                                 // next-shot-to   = this-shot-from
                do {
                  from = DistoXStationName.increment( from );    // next-shot-from = increment this-shot-from
                } while ( DistoXStationName.listHasName( list, from ) );
                station = TopoDroidSetting.mShotAfterSplays ? from : to;      // splay-station  = next-shot-from if splay before shot
                // Log.v("DistoX", "station [2] " + station + " from " + from + " to " + to );
              }                                              //                = thsi-shot-from if splay after shot
            }
          } else {
            atStation = 0;
            blk.mFrom = station;
            // Log.v( "DistoX", "Id " + blk.mId + " " + blk.mFrom );
            mData.updateShotName( blk.mId, mSID, blk.mFrom, "", true ); // SPLAY
            prev = blk;
          }
        }
      } else { // blk.mFrom.length > 0
        // Log.v("DistoX", " FROM is not empty: " + blk.mFrom );

        if ( blk.mTo.length() > 0 ) {
          // Log.v("DistoX", " TO is not empty: " + blk.mTo );
          if ( TopoDroidSetting.mSurveyStations == 1 ) { // forward shot
            from = blk.mTo;
            to   = from;
            do {
              to   = DistoXStationName.increment( to );
            } while ( DistoXStationName.listHasName( list, to ) );
            station = TopoDroidSetting.mShotAfterSplays ? blk.mTo    // blk.mFrom-blk.mTo blk.mTo, ..., blk.mTo-to
                                                    // 1-2, 2, 2, ..., 2-3, 3, 
                                       : blk.mFrom; // blk.mFrom-blk.mTo blk.mFrom ... blk.mTo-to, blk.mTo, ...
                                                    // 1-2, 1, 1, ..., 2-3, 2, 2, ...
            // Log.v("DistoX", "station [3] " + station + " from " + from + " to " + to );
          } else { // backward shot
            to   = blk.mFrom;
            from = to;
            do {
              from = DistoXStationName.increment( from ); // FIXME it was from
            } while ( DistoXStationName.listHasName( list, from ) );
            station = TopoDroidSetting.mShotAfterSplays ? from       // blk.mFrom-blk.mTo from ... from-blk.mFrom
                                                    // 2-1, 3, 3, ..., 3-2, 4, ...
                                       : blk.mFrom; // blk.mFrom-blk.mTo ... blk.mFrom from-blk.mFrom, from ...
                                                    // 2-1, 2, 2, ..., 3-2, 3, 3, ...
            // Log.v("DistoX", "station [4] " + station + " from " + from + " to " + to );
          }
          atStation = TopoDroidSetting.mMinNrLegShots;
        } else {
          // Log.v("DistoX", " TO is empty " );
          atStation = 0;
        }
        prev = blk;
      }
    }
  }

  // ----------------------------------------------
  // EXPORTS

  public String exportSurveyAsCsx( DrawingActivity sketch, String origin )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = ( sketch == null )? TopoDroidPath.getSurveyCsxFile(mySurvey)
                                        : TopoDroidPath.getSurveyCsxFile(mySurvey, sketch.mName1);
    return TopoDroidExporter.exportSurveyAsCsx( mSID, mData, info, sketch, origin, filename );
  }

  public String exportSurveyAsTop( DrawingActivity sketch, String origin )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = TopoDroidPath.getSurveyTopFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsTop( mSID, mData, info, sketch, origin, filename );
  }

  public String exportSurveyAsTh( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = TopoDroidPath.getSurveyThFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsTh( mSID, mData, info, filename );
  }

  public String exportSurveyAsSvx()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = TopoDroidPath.getSurveySvxFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsSvx( mSID, mData, info, filename );
  }

  public String exportSurveyAsTro()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = TopoDroidPath.getSurveyTroFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsTro( mSID, mData, info, filename );
  }

  public String exportSurveyAsCsv( )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = TopoDroidPath.getSurveyCsvFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsCsv( mSID, mData, info, filename );
  }

  public String exportSurveyAsSrv()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = TopoDroidPath.getSurveySrvFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsSrv( mSID, mData, info, filename );
  }

  public String exportSurveyAsDxf( DistoXNum num )
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = TopoDroidPath.getSurveyDxfFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsDxf( mSID, mData, info, num, filename );
  }

  public String exportSurveyAsDat()
  {
    SurveyInfo info = mData.selectSurveyInfo( mSID );
    String filename = TopoDroidPath.getSurveyDatFile( mySurvey );
    return TopoDroidExporter.exportSurveyAsDat( mSID, mData, info, filename );
  }



  public String exportCalibAsCsv( )
  {
    if ( mCID < 0 ) return null;
    CalibInfo ci = mData.selectCalibInfo( mCID );
    if ( ci == null ) return null;
    String filename = TopoDroidPath.getCsvFile( ci.name );
    return TopoDroidExporter.exportCalibAsCsv( mCID, mData, ci, filename );
  }

  // ----------------------------------------------
  // FIRMWARE

  private void installFirmware( boolean overwrite )
  {
    InputStream is = getResources().openRawResource( R.raw.firmware );
    firmwareUncompress( is, overwrite );
  }
 
  // FIXME MANUAL
  // private void installManual( )
  // {
  //   // Log.v( TopoDroidApp.TAG, "Uncompress Manual ");
  //   InputStream is = getResources().openRawResource( R.raw.manual );
  //   manualUncompress( is );
  // }

  // -------------------------------------------------------------
  // SYMBOLS

  void installSymbols( boolean overwrite )
  {
    boolean install = overwrite;
    askSymbolUpdate = false;
    if ( ! overwrite ) { // check whether to install
      String version = mData.getValue( "symbol_version" );
      // Log.v("DistoX", "symbol version <" + version + "> SYMBOL_VERSION <" + SYMBOL_VERSION + ">" );
      if ( version == null ) {
        install = true;
      } else if ( ! version.equals(SYMBOL_VERSION) ) {
        askSymbolUpdate = true;
      } else { // version .equals SYMBOL_VERSION
        return;
      }
      mData.setValue( "symbol_version", SYMBOL_VERSION );
    }
    if ( install ) {
      InputStream is = getResources().openRawResource( R.raw.symbols );
      symbolsUncompress( is, overwrite );
    }
  }

  /** download symbol zip from internet and store files in save/dirs
   */
  // int symbolsSync()
  // {
  //   int cnt = 0;
  //   try {
  //     URL url = new URL( symbol_urlstr );
  //     URLConnection url_conn = url.openConnection( );
  //     HttpURLConnection http_conn = (HttpURLConnection) url_conn;
  //     int resp_code = http_conn.getResponseCode();
  //     // Log.v( TAG, "resp code " + resp_code );
  //     if ( resp_code == HttpURLConnection.HTTP_OK ) {
  //       InputStream in = http_conn.getInputStream();
  //       cnt = symbolsUncompress( in );
  //     } else {
  //       // Toast.makeText( app, "Engine temporarily not available " + resp_code, Toast.LENGTH_SHORT ).show();
  //     }
  //     http_conn.disconnect();
  //   } catch ( MalformedURLException e ) {
  //     // Toast.makeText( app, "Bad URL " + urlstr, Toast.LENGTH_LONG ).show();
  //   } catch ( IOException e ) {
  //     // Toast.makeText( app, "Failed to get " + urlstr, Toast.LENGTH_LONG ).show();
  //   }
  //   return cnt;
  // }

  private int symbolsUncompress( InputStream fis, boolean overwrite )
  {
    int cnt = 0;
    // Log.v(TAG, "symbol uncompress ...");
    TopoDroidPath.symbolsCheckDirs();
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
          String pathname = TopoDroidPath.getSymbolFile( filepath );
          File file = new File( pathname );
          if ( overwrite || ! file.exists() ) {
            ++cnt;
            TopoDroidApp.checkPath( pathname );
            FileOutputStream fout = new FileOutputStream( pathname );
            int c;
            while ( ( c = zin.read( buffer ) ) != -1 ) {
              fout.write(buffer, 0, c); // offset 0 in buffer
            }
            fout.close();
          
            // pathname =  APP_SYMBOL_SAVE_PATH + filepath;
            // file = new File( pathname );
            // if ( ! file.exists() ) {
            //   TopoDroidApp.checkPath( pathname );
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
    return cnt;
  }

  private int firmwareUncompress( InputStream fis, boolean overwrite )
  {
    int cnt = 0;
    // Log.v(TAG, "firmware uncompress ...");
    TopoDroidPath.checkBinDir( );
    try {
      // byte buffer[] = new byte[36768];
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( ze.isDirectory() ) continue;
        if ( ! filepath.endsWith("bin") ) continue;
        String pathname =  TopoDroidPath.getBinFile( filepath );
        File file = new File( pathname );
        if ( overwrite || ! file.exists() ) {
          ++cnt;
          TopoDroidApp.checkPath( pathname );
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
    return cnt;
  }

  private void manualUncompress( InputStream fis )
  {
    // Log.v(TAG, "manual uncompress ...");
    TopoDroidPath.checkManDir( );
    try {
      byte buffer[] = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {
        String filepath = ze.getName();
        if ( ze.isDirectory() ) continue;
        String pathname =  TopoDroidPath.getManFile( filepath );
        
        TopoDroidApp.checkPath( pathname );
        File file = new File( pathname );
        FileOutputStream fout = new FileOutputStream( pathname );
        int c;
        while ( ( c = zin.read( buffer ) ) != -1 ) {
          fout.write(buffer, 0, c); // offset 0 in buffer
        }
        fout.close();
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
  }

  /** insert manual-data shot
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   */
  public DistoXDBlock insertManualShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend,
                           String left, String right, String up, String down,
                           String splay_station )
  {
    mSecondLastShotId = lastShotId();
    DistoXDBlock ret = null;
    long id;
    distance /= TopoDroidSetting.mUnitLength;
    bearing  /= TopoDroidSetting.mUnitAngle;
    clino    /= TopoDroidSetting.mUnitAngle;
    if ( ( distance < 0.0f ) ||
         ( clino < -90.0f || clino > 90.0f ) ||
         ( bearing < 0.0f || bearing >= 360.0f ) ) {
      Toast.makeText( this, R.string.illegal_data_value, Toast.LENGTH_SHORT ).show();
      return null;
    }

    if ( from != null && to != null && from.length() > 0 ) {
      // if ( mData.makesCycle( -1L, mSID, from, to ) ) {
      //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
      // } else
      {
        // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "manual-shot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > TopoDroidSetting.mVThreshold );
        // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "manual-shot SID " + mSID + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( left != null && left.length() > 0 ) {
          float l = -1.0f;
          try {
            l = Float.parseFloat( left ) / TopoDroidSetting.mUnitLength;
          } catch ( NumberFormatException e ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "manual-shot parse error: left " + left );
          }
          if ( l >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, l, 270.0f, 0.0f, 0.0f, 1, true );
              } else {
                id = mData.insertShot( mSID, -1L, l, 270.0f, 0.0f, 0.0f, 1, true );
              }
            } else {
              float b = bearing - 90.0f;
              if ( b < 0.0f ) b += 360.0f;
              // b = in360( b );
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, l, b, 0.0f, 0.0f, 1, true );
              } else {
                id = mData.insertShot( mSID, -1L, l, b, 0.0f, 0.0f, 1, true );
              }
            }
            mData.updateShotName( id, mSID, splay_station, "", true );
            if ( at >= 0L ) ++at;
          }
        } 
        if ( right != null && right.length() > 0 ) {
          float r = -1.0f;
          try {
            r = Float.parseFloat( right ) / TopoDroidSetting.mUnitLength;
          } catch ( NumberFormatException e ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "manual-shot parse error: right " + right );
          }
          if ( r >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, r, 90.0f, 0.0f, 0.0f, 1, true );
              } else {
                id = mData.insertShot( mSID, -1L, r, 90.0f, 0.0f, 0.0f, 1, true );
              }
            } else {
              float b = bearing + 90.0f;
              if ( b >= 360.0f ) b -= 360.0f;
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, r, b, 0.0f, 0.0f, 1, true );
              } else {
                id = mData.insertShot( mSID, -1L, r, b, 0.0f, 0.0f, 1, true );
              }
            }
            mData.updateShotName( id, mSID, splay_station, "", true );
            if ( at >= 0L ) ++at;
          }
        }
        if ( up != null && up.length() > 0 ) {
          float u = -1.0f;
          try {
            u = Float.parseFloat( up ) / TopoDroidSetting.mUnitLength;
          } catch ( NumberFormatException e ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "manual-shot parse error: up " + up );
          }
          if ( u >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, u, 0.0f, 0.0f, 0.0f, 1, true );
              } else {
                id = mData.insertShot( mSID, -1L, u, 0.0f, 0.0f, 0.0f, 1, true );
              }
            } else {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, u, 0.0f, 90.0f, 0.0f, 1, true );
              } else {
                id = mData.insertShot( mSID, -1L, u, 0.0f, 90.0f, 0.0f, 1, true );
              }
            }
            mData.updateShotName( id, mSID, splay_station, "", true );
            if ( at >= 0L ) ++at;
          }
        }
        if ( down != null && down.length() > 0 ) {
          float d = -1.0f;
          try {
            d = Float.parseFloat( down ) / TopoDroidSetting.mUnitLength;
          } catch ( NumberFormatException e ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "manual-shot parse error: down " + down );
          }
          if ( d >= 0.0f ) {
            if ( horizontal ) {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, d, 180.0f, 0.0f, 0.0f, 1, true );
              } else {
                id = mData.insertShot( mSID, -1L, d, 180.0f, 0.0f, 0.0f, 1, true );
              }
            } else {
              if ( at >= 0L ) {
                id = mData.insertShotAt( mSID, at, d, 0.0f, -90.0f, 0.0f, 1, true );
              } else {
                id = mData.insertShot( mSID, -1L, d, 0.0f, -90.0f, 0.0f, 1, true );
              }
            }
            mData.updateShotName( id, mSID, splay_station, "", true );
            if ( at >= 0L ) ++at;
          }
        }
        if ( at >= 0L ) {
          id = mData.insertShotAt( mSID, at, distance, bearing, clino, 0.0f, 1, true );
        } else {
          id = mData.insertShot( mSID, -1L, distance, bearing, clino, 0.0f, 1, true );
        }
        // String name = from + "-" + to;
        mData.updateShotName( id, mSID, from, to, true );
        mData.updateShotExtend( id, mSID, extend, true );
        // FIXME updateDisplay( );

        ret = mData.selectShot( id, mSID );
      }
    } else {
      Toast.makeText( this, R.string.missing_station, Toast.LENGTH_SHORT ).show();
    }
    return ret;
  }

  int getCalibAlgoFromDB()
  {
    return mData.selectCalibAlgo( mCID );
  }

  void updateCalibAlgo( int algo ) 
  {
    mData.updateCalibAlgo( mCID, algo );
  }
  
  int getCalibAlgoFromDevice()
  {
    if ( mDevice == null ) return 1;                  // default: CALIB_ALGO_LINEAR
    if ( mDevice.mType == Device.DISTO_A3 ) return 1; // A3: CALIB_ALGO_LINEAR
    if ( mDevice.mType == Device.DISTO_X310 ) {
      // if ( mComm == null ) return 1; // should not happen
      byte[] ret = mComm.readMemory( mDevice.mAddress, 0xe000 );
      if ( ret == null ) return -1; // should not happen
      if ( ret[0] >= 2 && ret[1] >= 3 ) return 2; // CALIB_ALGO_NON_LINEAR
      return 1; // CALIB_ALGO_LINEAR
    }
    return 1; // CALIB_ALGO_LINEAR
  }  

  void setX310Laser( int what, ArrayList<ILister> listers ) // 0: off, 1: on, 2: measure
  {
    mComm.setX310Laser( mDevice.mAddress, what, listers );
  }

  // int readFirmwareHardware()
  // {
  //   return mComm.readFirmwareHardware( mDevice.mAddress );
  // }

  int dumpFirmware( String filename )
  {
    // Log.v( "DistoX", "dump firmware " + filename );
    return mComm.dumpFirmware( mDevice.mAddress, TopoDroidPath.getBinFile(filename) );
  }

  int uploadFirmware( String filename )
  {
    // Log.v( "DistoX", "upload firmware " + filename );
    String pathname = TopoDroidPath.getBinFile( filename );
    TopoDroidLog.LogFile( "Firmware upload address " + mDevice.mAddress );
    TopoDroidLog.LogFile( "Firmware upload file " + pathname );
    if ( ! pathname.endsWith( "bin" ) ) return 0;
    return mComm.uploadFirmware( mDevice.mAddress, pathname );
  }

  long insert2dPlot( long sid , String name, String start )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "new plot " + name + " start " + start );
    long pid_p = mData.insertPlot( sid, -1L, name+"p",
                 PlotInfo.PLOT_PLAN, 0L, start, "", 0, 0, mScaleFactor, 0, 0, true );
    long pid_s = mData.insertPlot( sid, -1L, name+"s",
                 PlotInfo.PLOT_EXTENDED, 0L, start, "", 0, 0, mScaleFactor, 0, 0, true );
    return pid_p;
  }
  
  long insert2dSection( long sid, String name, long type, String from, String to, float azimuth, float clino )
  {
    // FIXME COSURVEY 2d sections are not forwarded
    return mData.insertPlot( sid, -1L, name, type, 0L, from, to, 0, 0, TopoDroidApp.mScaleFactor, azimuth, clino, false );
  }

  // ---------------------------------------------------------------------
  // SYNC

  static boolean mCosurvey = false;       // whether co-survey is enable by the DB
  static boolean mCoSurveyServer = false; // whether co-survey server is on
  ConnectionHandler mSyncConn = null;

  void setCoSurvey( boolean co_survey ) // FIXME interplay with TopoDroidSetting
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
    if ( mSyncConn == null ) return SyncService.STATE_NONE;
    return mSyncConn.getType();
  }

  int getAcceptState()
  {
    if ( mSyncConn == null ) return SyncService.STATE_NONE;
    return mSyncConn.getAcceptState();
  }

  int getConnectState()
  {
    if ( mSyncConn == null ) return SyncService.STATE_NONE;
    return mSyncConn.getConnectState();
  }

  String getConnectionStateStr()
  {
    if ( mSyncConn == null ) return "NONE";
    return mSyncConn.getConnectStateStr();
  }

  String getConnectedDeviceName()
  {
    if ( mSyncConn == null ) return null;
    return mSyncConn.getConnectedDeviceName();
  }

  String getConnectionStateTitleStr()
  {
    if ( mSyncConn == null ) return "";
    return mSyncConn.getConnectionStateTitleStr();
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
    if ( mSyncConn != null ) {
      mSyncConn.connect( device );
    }
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
    if ( mSyncConn != null ) {
      mSyncConn.syncDevice( device );
    }
  }

  void startRemoteTopoDroid( )
  { 
    if ( mSyncConn != null ) {
      mSyncConn.start( );
    }
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
    if ( mSyncConn != null ) {
      registerDataListener( mSyncConn );
    }
  }

}
