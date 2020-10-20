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

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDStatus;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.prefs.TDPrefActivity;
import com.topodroid.prefs.TDPrefHelper;
import com.topodroid.prefs.TDSetting;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

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
// import android.app.Notification;
// import android.app.NotificationManager;
// import android.app.KeyguardManager;
// import android.app.KeyguardManager.KeyguardLock;
// import android.app.Activity;

import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Configuration;

// import android.content.Intent;
// import android.content.ActivityNotFoundException;
// import android.net.Uri;

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
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.graphics.drawable.BitmapDrawable;


import android.util.DisplayMetrics;

import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothAdapter;
// import android.bluetooth.BluetoothDevice; // COSURVEY

public class TopoDroidApp extends Application
{
  // static final String EMPTY = "";
  static private TopoDroidApp thisApp = null;

  boolean mWelcomeScreen;  // whether to show the welcome screen (used by MainWindow)
  boolean mSetupScreen;    // whether to show the welcome screen (used by MainWindow)
  static boolean mCheckManualTranslation = false;
  // static String mManual;  // manual url

  static Locale mLocale;
  static String mLocaleStr;
  static int mCheckPerms;

  static String mClipboardText = null; // text clipboard

  public static float mScaleFactor   = 1.0f;
  public static float mDisplayWidth  = 200f;
  public static float mDisplayHeight = 320f;
  public static float mBorderRight      = 4096;
  public static float mBorderLeft       = 0;
  public static float mBorderInnerRight = 4096;
  public static float mBorderInnerLeft  = 0;
  // static float mBorderBottom     = 4096; // in DrawingWindow

  // static boolean isTracing = false;

  /* FIXME_HIGHLIGHT
  private List< DBlock > mHighlighted = null;
  void    setHighlighted( List< DBlock > blks ) { mHighlighted = blks; }
  // int     getHighlightedSize() { return (mHighlighted != null)? -1 : mHighlighted.size(); }
  boolean hasHighlighted() { return mHighlighted != null && mHighlighted.size() > 0; }
  boolean hasHighlightedId( long id )
  {
    if ( mHighlighted == null ) return false;
    for ( DBlock b : mHighlighted ) if ( b.mId == id ) return true;
    return false;
  }
  */

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
    if ( mActivity == null ) return;
    mActivity.runOnUiThread( new Runnable() { 
      public void run () { 
        mListerSet.setConnectionStatus( mDataDownloader.getStatus() );
      }
    } );
  }

  void notifyDisconnected( int data_type )
  {
    if ( mListerSet.size() > 0 ) {
      try {
        new ReconnectTask( mDataDownloader, data_type ).execute();
      } catch ( RuntimeException e ) {
        TDLog.Error("reconnect error: " + e.getMessage() );
      }
    }
  }

  // -----------------------------------------------------

  // FIXME INSTALL_SYMBOL boolean askSymbolUpdate = false; // by default do not ask

  String[] DistoXConnectionError;
  // BluetoothAdapter mBTAdapter = null;     // BT connection
  private TopoDroidComm mComm = null;     // BT communication
  DataDownloader mDataDownloader = null;  // data downloader
  public static DataHelper mData = null;         // database 
  static DeviceHelper mDData = null;      // device/calib database

  // public static TDPrefHelper mPrefHlp      = null;
  static SurveyWindow mSurveyWindow    = null; // FIXME ref mActivity
  public static ShotWindow mShotWindow = null; // FIXME ref mActivity - public for prefs/TDSetting
  static DrawingWindow mDrawingWindow  = null; // FIXME currently not used
  static MainWindow mActivity          = null; // FIXME ref mActivity

  static boolean mGMActivityVisible = false;

  static long lastShotId( ) { return mData.getLastShotId( TDInstance.sid ); }
  static StationName mStationName = null;

  // static Device mDevice = null;
  // static int deviceType() { return (mDevice == null)? 0 : mDevice.mType; }
  // static String distoAddress() { return (mDevice == null)? null : mDevice.mAddress; }

  // FIXME VirtualDistoX
  // VirtualDistoX mVirtualDistoX = new VirtualDistoX();

  static void notifyUpdateDisplay()
  {
    if ( mDrawingWindow != null ) mDrawingWindow.notifyUpdateDisplay();
  }

  public static void setToolsToolbars()
  {
    if ( mDrawingWindow != null ) mDrawingWindow.setToolsToolbars();
  }

  // -------------------------------------------------------------------------------------
  // static SIZE methods

  public static float getDisplayDensity( )
  {
    return Resources.getSystem().getDisplayMetrics().density;
  }

  public static float getDisplayDensity( Context context )
  {
    return Resources.getSystem().getDisplayMetrics().density;
  }

  // FIXED_ZOOM
  public static int getDisplayDensityDpi( )
  {
    return Resources.getSystem().getDisplayMetrics().densityDpi;
  }

  // public int setListViewHeight( MyHorizontalListView listView )
  // {
  //   return TopoDroidApp.setListViewHeight( this, listView );
  // }

  public static int setListViewHeight( Context context, MyHorizontalListView listView )
  {
    // int size = getScaledSize( context );
    if ( listView != null ) {
      LayoutParams params = listView.getLayoutParams();
      params.height = TDSetting.mSizeButtons + 10;
      listView.setLayoutParams( params );
    }
    return TDSetting.mSizeButtons;
  }

  // default button size - USED by Tdm...
  static int getScaledSize( Context context )
  {
    return (int)( TDSetting.mSizeButtons * context.getResources().getSystem().getDisplayMetrics().density );
  }
  
  public static void resetButtonBar() { if ( mActivity != null ) mActivity.resetButtonBar(); }
  public static void setMenuAdapter( ) { if ( mActivity != null ) mActivity.setMenuAdapter( TDInstance.getResources() ); }
  public static void setScreenOrientation() { if ( mActivity != null ) TDandroid.setScreenOrientation( mActivity  ); }

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
    return ( TDInstance.deviceA == null || ( info != null && info.device.equals( TDInstance.deviceAddress() ) ) );
  }

  public static List< String > getSurveyNames()
  {
    if ( mData == null ) return null;
    return mData.selectAllSurveys();
  }

  public static SurveyInfo getSurveyInfo()
  {
    if ( TDInstance.sid <= 0 ) return null;
    if ( mData == null ) return null;
    return mData.selectSurveyInfo( TDInstance.sid );
    // if ( info == null ) TDLog.Error("null survey info. sid " + TDInstance.sid );
  }

  private static int getSurveyExtend()
  {
    if ( TDInstance.sid <= 0 ) return SurveyInfo.SURVEY_EXTEND_NORMAL;
    if ( mData == null ) return SurveyInfo.SURVEY_EXTEND_NORMAL;
    return mData.getSurveyExtend( TDInstance.sid );
  }

  public static void setSurveyExtend( int extend )
  {
    // Log.v( "DistoXE", "set SurveyExtend: " + extend );
    if ( TDInstance.sid <= 0 ) return;
    if ( mData == null ) return;
    mData.updateSurveyExtend( TDInstance.sid, extend );
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
    thisApp = null;
  }

  static void setDeviceModel( Device device, int model )
  {
    if ( device != null && device == TDInstance.deviceA ) {
      if ( device.mType != model ) {
        if ( model == Device.DISTO_A3 ) {
          mDData.updateDeviceModel( device.mAddress, "DistoX" );
          device.mType = model;
        } else if ( model == Device.DISTO_X310 ) {
          mDData.updateDeviceModel( device.mAddress, "DistoX-0000" );
          device.mType = model;
        // } else if ( model == Device.DISTO_X000 ) { // FIXME VirtualDistoX
        //   mDData.updateDeviceModel( device.mAddress, "DistoX0" );
        //   device.mType = model;
        }
      }
    }
  }

  /**
   * @param device      device
   * @param nickname    device nickmane
   */
  static void setDeviceName( Device device, String nickname )
  {
    if ( device != null /* && device == TDInstance.deviceA */ ) {
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
    // return mComm != null && mComm.mBTConnected && mComm.mCommThread != null;
    return mComm != null && mComm.isConnected() && ! mComm.checkCommThreadNull(); // FIXME BLE to check
  }

  void disconnectRemoteDevice( boolean force )
  {
    // Log.v( "DistoXBLE", "App disconnect remote device. force " + force );
    // TDLog.Log( TDLog.LOG_COMM, "App disconnect RemoteDevice listers " + mListerSet.size() + " force " + force );
    if ( force || mListerSet.size() == 0 ) {
      if ( mComm != null && mComm.isConnected() ) {
        mComm.disconnectRemoteDevice( ); // FIXME BLE to check
      }
    }
  }

  private void deleteComm() // FIXME BLE
  {
    if ( mComm != null ) {
      if ( mComm.isConnected() ) {
        mComm.disconnectRemoteDevice(); 
      }
      mComm = null;
    }
  }

  // void connectRemoteDevice( String address )
  // {
  //   if ( mComm != null ) mComm.connectRemoteDevice( address, mListerSet );
  // }

  // FIXME_COMM
  public boolean connectDevice( String address, int data_type ) 
  {
    // Log.v( "DistoXBLE", "App connect address " + address + " comm is " + ((mComm==null)? "null" : "non-null") );
    return mComm != null && mComm.connectDevice( address, mListerSet, data_type ); // FIXME_LISTER
  }

  public void disconnectComm()
  {
    // Log.v( "DistoXBLE", "App disconnect. comm is " + ((mComm==null)? "null" : "non-null") );
    if ( mComm != null ) mComm.disconnectDevice();
  }
  // end FIXME_COMM

  DeviceA3Info readDeviceA3Info( String address )
  {
    DeviceA3Info info = new DeviceA3Info();
    byte[] ret = readMemory( address, 0x8008 );
    if ( ret == null ) return null;
    info.mCode = String.format( getResources().getString( R.string.device_code ), MemoryOctet.toInt( ret[1], ret[0] ) );

    ret = readMemory( TDInstance.deviceAddress(), 0x8000 );
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
    //   Log.v("DistoX-APP", "X310 info C044 " + String.format( getResources().getString( R.string.device_memory ), ret[0], ret[1] ) );
    // }

    resetComm();
    return info;
  }

  // @param address    device address
  // @param command
  // @param head_tail  return array with positions of head and tail
  String readA3HeadTail( String address, byte[] command, int[] head_tail )
  {
    DistoXA3Comm comm = (DistoXA3Comm)mComm;
    String ret = comm.readA3HeadTail( address, command, head_tail );
    resetComm();
    return ret;
  }

  // swap hot bit in the range [from, to)
  int swapA3HotBit( String address, int from, int to, boolean on_off ) 
  {
    DistoXA3Comm comm = (DistoXA3Comm)mComm;
    int ret = comm.swapA3HotBit( address, from, to, on_off ); // FIXME_A3
    resetComm();
    return ret;
  }

  static boolean mEnableZip = true;  // whether zip saving is enabled or must wait (locked by th2. saving thread)
  static boolean mSketches = false;  // whether to use 3D models

  // ---------------------------------------------------------

  void startupStep2( )
  {
    TDPrefHelper prefHlp = new TDPrefHelper( this );
    // ***** LOG FRAMEWORK
    TDLog.loadLogPreferences( prefHlp );

    // mData.compileStatements(); // this method is now empty (and commented)

    PtCmapActivity.setMap( prefHlp.getString( "DISTOX_PT_CMAP", null ) );

    TDSetting.loadSecondaryPreferences( prefHlp );
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
    // Log.v("DistoXBLE", "create comm. type " + TDInstance.deviceType() );
    // if ( TDInstance.isDeviceAddress( Device.ZERO_ADDRESS ) ) { // FIXME VirtualDistoX
    //   mComm = new VirtualDistoXComm( this, mVirtualDistoX );
    // } else {
      switch ( TDInstance.deviceType() ) {
        case Device.DISTO_X310:
          mComm = new DistoX310Comm( this );
          break;
        case Device.DISTO_A3:
          mComm = new DistoXA3Comm( this );
          break;
        case Device.DISTO_BLE5: // FIXME BLE
          String address = TDInstance.deviceAddress();
          BluetoothDevice bt_device = TDInstance.bleDevice;
          // Log.v("DistoXBLE", "create BLE comm. address " + address + " BT " + ((bt_device==null)? "null" : bt_device.getAddress() ) );
          mComm = new BleComm( this, address, bt_device );
          break;
      }
    // }
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    thisApp = this;
    TDInstance.setContext( getApplicationContext() );

    // require large memory pre Honeycomb
    // dalvik.system.VMRuntime.getRuntime().setMinimumHeapSize( 64<<20 );

    // TDLog.Profile("TDApp onCreate");
    TDVersion.setVersion( this );
    TDPrefHelper prefHlp = new TDPrefHelper( this );

    mWelcomeScreen = prefHlp.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true
    if ( mWelcomeScreen ) {
      setDefaultSocketType( );
    }
    mSetupScreen = prefHlp.getBoolean( "DISTOX_SETUP_SCREEN", true ); // default: SetupScreen = true

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
    mBorderRight      = TopoDroidApp.mDisplayWidth * 15 / 16;
    mBorderLeft       = TopoDroidApp.mDisplayWidth / 16;
    mBorderInnerRight = TopoDroidApp.mDisplayWidth * 3 / 4;
    mBorderInnerLeft  = TopoDroidApp.mDisplayWidth / 4;
    // mBorderBottom     = TopoDroidApp.mDisplayHeight * 7 / 8;

    mScaleFactor   = (mDisplayHeight / 320.0f) * density;
    // FIXME it would be nice to have this, but it breaks all existing sketches
    //       therefore must stick with initial choice
    // DrawingUtil.CENTER_X = mDisplayWidth  / 2;
    // DrawingUtil.CENTER_Y = mDisplayHeight / 2;

    mCheckPerms = TDandroid.checkPermissions( this );

    if ( mCheckPerms >= 0 ) {
      mListerSet = new ListerSetHandler();
      mEnableZip = true;

      initEnvironment( prefHlp ); // called by SplashActivity

    }
    // mManual = getResources().getString( R.string.topodroid_man );

    // Log.v("DistoX-MAIN", "W " + mDisplayWidth + " H " + mDisplayHeight + " D " + density );
  }

  static void initEnvironment( TDPrefHelper prefHlp )
  {
    // TDLog.Profile("TDApp paths");
    TDPath.setDefaultPaths();

    // TDLog.Profile("TDApp cwd");
    TDInstance.cwd = prefHlp.getString( "DISTOX_CWD", "TopoDroid" );
    TDInstance.cbd = prefHlp.getString( "DISTOX_CBD", TDPath.PATH_BASEDIR );
    TDPath.setPaths( TDInstance.cwd, TDInstance.cbd );

    // TDLog.Profile("TDApp DB"); 
    // ***** DATABASE MUST COME BEFORE PREFERENCES

    // ---- IF_COSURVEY
    // mDataListeners = new DataListenerSet( );
    // mData  = new DataHelper( this, this, mDataListeners );
    // mDData = new DeviceHelper( this, this, null ); 
    //
    // ---- ELSE ----
    mData  = new DataHelper( thisApp /*, this */ ); 
    mDData = new DeviceHelper( thisApp /*, this */ );

    // mStationName = new StationName();

    // TDLog.Profile("TDApp prefs");
    // LOADING THE SETTINGS IS RATHER EXPENSIVE !!!
    TDSetting.loadPrimaryPreferences( /* this, */ TDInstance.getResources(),  prefHlp );

    thisApp.mDataDownloader = new DataDownloader( thisApp, thisApp );

    // ***** DRAWING TOOLS SYMBOLS
    // TDLog.Profile("TDApp symbols");

    // if one of the symbol dirs does not exists all of then are restored
    String version = mDData.getValue( "version" );
    if ( version == null || ( ! version.equals( TDVersion.string() ) ) ) {
      // Log.v("DistoX-PATH", "version " + version + " " + TDVersion.string() );
      mDData.setValue( "version",  TDVersion.string()  );
      // FIXME INSTALL_SYMBOL installSymbols( false ); // this updates symbol_version in the database
      if ( mDData.getValue( "symbol_version" ) == null ) {
        // Log.v("DistoX-PATH", "symbol version " + symbol_version );
        thisApp.installSymbols( true );
      }
      thisApp.installFirmware( false );
      // installUserManual( );
      mCheckManualTranslation = true;
    }

    // ***** CHECK SPECIAL EXPERIMENTAL FEATURES : SKETCH
    // if ( TDLevel.overTester ) {
    //   String value = mDData.getValue("sketches");
    //   mSketches =  value != null 
    //           && value.equals("on")
    //           && getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
    // }

    /* ---- IF_COSURVEY
    if ( TDLevel.overExpert ) {
      String value = mDData.getValue("cosurvey");
      mCosurvey =  value != null && value.equals("on");
      setCoSurvey( false );
      prefHlp.update( "DISTOX_COSURVEY", false );
      if ( mCosurvey ) {
        mSyncConn = new ConnectionHandler( this );
        mConnListener = new ArrayList<>();
      }
    }
    */

    // TDLog.Profile("TDApp device etc.");
    TDInstance.deviceA = mDData.getDevice( prefHlp.getString( TDSetting.keyDeviceName(), TDString.EMPTY ) );

    if ( TDInstance.deviceA != null ) {
      thisApp.createComm();
    }
  }

  // Led notifcation are shown only while the display is off
  // static final int NOTIFY_LED_ID = 10101;
  //   NotificationManager manager =
  //     (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE );
  //   Notification notify_led = new Notification( ); // crash
  //   notify_led.ledARGB = Color
  //   notify_led.ledOffMS = 800;
  //   notify_led.ledOnMS  = 200;
  //   notify_led.flags = notify_led.flags | Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_ONGOING_EVENT;
  //   manager.notify( NOTIFY_LED_ID, notify_led );
  //   manager.cancel( NOTIFY_LED_ID );
  //
  // an alternative is a vibration (but too frequent vibrations are
  // considered a bad idea)
  // manifest must have
  //   <uses-permission android:name="android.permission.VIBRATE" />
  // next
  //   Vibrator vibrator = (Vibrator)getSystemService( Context.VIBRATOR_SERVICE );
  //   vibrator.vibrate( 500 );
  // or
  //   long[] pattern = {400, 200};
  //   vibrator.vibrate( pattern, 0 ); // 0: repeat fom index 0, use -1 not to repeat
  //   vibrator.cancel();
  static boolean mLedOn = false;
  void notifyLed( boolean on_off ) 
  {
    if ( mLedOn ) {
      if ( ! on_off ) { // turn off led
        mLedOn = false;
      }
    } else {
      if ( on_off ) { // turn on led
        mLedOn = true;
        if ( TDSetting.mConnectFeedback == TDSetting.FEEDBACK_BELL ) {
          TDUtil.ringTheBell( 200 );
        } else if ( TDSetting.mConnectFeedback == TDSetting.FEEDBACK_VIBRATE ) {
          TDUtil.vibrate( this, 200 );
        }
      }
    }
  }

  @Override
  protected void attachBaseContext( Context ctx )
  {
    TDInstance.context = ctx;
    super.attachBaseContext( resetLocale( ) );
  }

  @Override
  public void onConfigurationChanged( Configuration cfg )
  {
    super.onConfigurationChanged( cfg );
    resetLocale();
  }

  // called by MainWindow
  static Context resetLocale()
  {
    // Log.v("DistoX", "reset locale to " + mLocaleStr );
    // mLocale = (mLocaleStr.equals(TDString.EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    Resources res = TDInstance.getResources();
    DisplayMetrics dm = res.getDisplayMetrics();
    /* FIXME-23 */
    if ( android.os.Build.VERSION.SDK_INT >= 17 ) {
      Configuration conf = new Configuration( res.getConfiguration() );
      conf.setLocale( mLocale );
      // TDInstance.context = TDInstance.context.createConfigurationContext( conf );
      res.updateConfiguration( conf, dm );
    } else {
      Configuration conf = res.getConfiguration();
      conf.locale = mLocale; 
      res.updateConfiguration( conf, dm );
    }
    /* */
    /* FIXME-16 FIXME-8 
      Configuration conf = res.getConfiguration();
      conf.locale = mLocale; 
      res.updateConfiguration( conf, dm );
    /* */
    return TDInstance.context;
  }

  public static void setLocale( String locale, boolean load_symbols )
  {
    mLocaleStr = locale;
    mLocale = (mLocaleStr.equals(TDString.EMPTY))? Locale.getDefault() : new Locale( mLocaleStr );
    // Log.v("DistoXPref", "set locale str <" + locale + "> " + mLocale.toString() );

    resetLocale();
    Resources res = TDInstance.getResources();
    if ( load_symbols ) {
      BrushManager.reloadPointLibrary( TDInstance.context, res ); // reload symbols
      BrushManager.reloadLineLibrary( res );
      BrushManager.reloadAreaLibrary( res );
    }
    if ( mActivity != null ) mActivity.setMenuAdapter( res );
    TDPrefActivity.reloadPreferences();
  }

  public static void setCWD( String cwd, String cbd )
  {
    if ( cwd == null || cwd.length() == 0 ) cwd = TDInstance.cwd;
    if ( cbd == null || cbd.length() == 0 ) cbd = TDInstance.cbd;
    if ( cbd.equals( TDInstance.cbd ) && cwd.equals( TDInstance.cwd ) ) return;
    TDLog.Log( TDLog.LOG_PATH, "set cwd " + cwd + " " + cbd );
    mData.closeDatabase();
    TDInstance.cbd = cbd;
    TDInstance.cwd = cwd;
    TDPath.setPaths( TDInstance.cwd, TDInstance.cbd );
    mData.openDatabase( TDInstance.context );
    if ( mActivity != null ) mActivity.setTheTitle( );
  }

// -----------------------------------------------------------------

  // called by GMActivity and by CalibCoeffDialog 
  // and DeviceActivity (to reset coeffs)
  void uploadCalibCoeff( byte[] coeff, boolean check, Button b )
  {
    // TODO this writeCoeff shoudl be run in an AsyncTask
    if ( b != null ) b.setEnabled( false );
    if ( mComm == null || TDInstance.deviceA == null ) {
      TDToast.makeBad( R.string.no_device_address );
    } else if ( check && ! checkCalibrationDeviceMatch() ) {
      TDToast.makeBad( R.string.calib_device_mismatch );
    } else if ( ! mComm.writeCoeff( TDInstance.deviceAddress(), coeff ) ) {
      TDToast.makeBad( R.string.write_failed );
    } else {
      TDToast.make( R.string.write_ok );
    }
    if ( b != null ) b.setEnabled( true );
    resetComm();
  }

  // called by CalibReadTask.onPostExecute
  boolean readCalibCoeff( byte[] coeff )
  {
    if ( mComm == null || TDInstance.deviceA == null ) return false;
    boolean ret = mComm.readCoeff( TDInstance.deviceAddress(), coeff );
    resetComm();
    return ret;
  }

  // called by CalibToggleTask.doInBackground
  boolean toggleCalibMode( )
  {
    if ( mComm == null || TDInstance.deviceA == null ) return false;
    boolean ret = mComm.toggleCalibMode( TDInstance.deviceAddress(), TDInstance.deviceType() );
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
    DistoX310Comm comm = (DistoX310Comm)mComm;
    int ret = comm.readX310Memory( address, h0, h1, memory );
    resetComm();
    return ret;
  }

  int readA3Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    DistoXA3Comm comm = (DistoXA3Comm)mComm;
    int ret = comm.readA3Memory( address, h0, h1, memory );
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
      pw.format( "%s %d\n",  TDVersion.string(), TDVersion.code() );
      pw.format( "%s\n", TDVersion.DB_VERSION );
      pw.format( "%s\n", info.name );
      pw.format("%s\n", TDUtil.currentDate() );
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
  //
  // @note surveyname is modified
  public int checkManifestFile( String filename, String surveyname )
  {
    mManifestDbVersion = 0;
    String line;
    // if ( mData.hasSurveyName( surveyname ) ) {
    //   return -1;
    // }
    int version_code = 0;
    int ret = 0;
    try {
      FileReader fr = new FileReader( filename );
      BufferedReader br = new BufferedReader( fr );
      // first line is version
      line = br.readLine().trim();
      ret = TDVersion.check( line );
      if ( ret < 0 ) return ret;

      line = br.readLine().trim();
      try {
        mManifestDbVersion = Integer.parseInt( line );
      } catch ( NumberFormatException e ) {
        TDLog.Error( "parse error: db version " + line );
      }
      
      if ( ! (    mManifestDbVersion >= TDVersion.DATABASE_VERSION_MIN
               && mManifestDbVersion <= TDVersion.DATABASE_VERSION ) ) {
        TDLog.Log( TDLog.LOG_ZIP,
                          "TopDroid DB version mismatch: found " + mManifestDbVersion + " expected " + 
                          + TDVersion.DATABASE_VERSION_MIN + "-" + TDVersion.DATABASE_VERSION );
        return -3;
      }
      surveyname = br.readLine().trim();
      // if ( ! line.equals( surveyname ) ) return -4;
      if ( mData.hasSurveyName( surveyname ) ) {
        return -1;
      }
      fr.close();
    } catch ( NumberFormatException e ) {
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
    }
    return ret;
  }


  // ----------------------------------------------------------
  // SURVEY AND CALIBRATION

  boolean renameCurrentSurvey( long sid, String name )
  {
    if ( name == null || name.length() == 0 ) return false;
    if ( name.equals( TDInstance.survey ) ) return true;
    if ( mData == null ) return false;
    if ( mData.renameSurvey( sid, name ) ) {  
      File old = null;
      File nev = null;
      { // rename plot/sketch files: th3
        List< PlotInfo > plots = mData.selectAllPlots( sid );
        for ( PlotInfo p : plots ) {
          // Therion
          TDUtil.renameFile( TDPath.getSurveyPlotTh2File( TDInstance.survey, p.name ), TDPath.getSurveyPlotTh2File( name, p.name ) );
          // Tdr
          TDUtil.renameFile( TDPath.getSurveyPlotTdrFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTdrFile( name, p.name ) );
          // rename exported plots: dxf png svg csx
          TDUtil.renameFile( TDPath.getSurveyPlotDxfFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotDxfFile( name, p.name ) );
          TDUtil.renameFile( TDPath.getSurveyPlotShzFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotShzFile( name, p.name ) );
          TDUtil.renameFile( TDPath.getSurveyPlotSvgFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotSvgFile( name, p.name ) );
          // TDUtil.renameFile( TDPath.getSurveyPlotHtmFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotHtmFile( name, p.name ) );
          TDUtil.renameFile( TDPath.getSurveyPlotTnlFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTnlFile( name, p.name ) );
          TDUtil.renameFile( TDPath.getSurveyPlotPngFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotPngFile( name, p.name ) );
          TDUtil.renameFile( TDPath.getSurveyPlotCsxFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotCsxFile( name, p.name ) );
          TDUtil.renameFile( TDPath.getSurveyPlotXviFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotXviFile( name, p.name ) );
        }
      }
      /* FIXME_SKETCH_3D *
      { // rename sketch files: th3
        List< Sketch3dInfo > sketches = mData.selectAllSketches( sid );
        for ( Sketch3dInfo s : sketches ) {
          TDUtil.renameFile( TDPath.getSurveySketchOutFile( TDInstance.survey, s.name ), TDPath.getSurveySketchOutFile( name, s.name ) );
        }
      }
       * FIXME_SKETCH_3D */
      // rename exported files: csv csx dat dxf kml plt srv svx th top tro 
        TDUtil.renameFile( TDPath.getSurveyThFile( TDInstance.survey ), TDPath.getSurveyThFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyCsvFile( TDInstance.survey ), TDPath.getSurveyCsvFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyCsxFile( TDInstance.survey ), TDPath.getSurveyCsxFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyCavFile( TDInstance.survey ), TDPath.getSurveyCavFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyCaveFile( TDInstance.survey ), TDPath.getSurveyCaveFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyDatFile( TDInstance.survey ), TDPath.getSurveyDatFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyDxfFile( TDInstance.survey ), TDPath.getSurveyDxfFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyGrtFile( TDInstance.survey ), TDPath.getSurveyGrtFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyGtxFile( TDInstance.survey ), TDPath.getSurveyGtxFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyKmlFile( TDInstance.survey ), TDPath.getSurveyKmlFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyJsonFile( TDInstance.survey ), TDPath.getSurveyJsonFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyPltFile( TDInstance.survey ), TDPath.getSurveyPltFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyShzFile( TDInstance.survey ), TDPath.getSurveyShzFile( name ) );
        TDUtil.renameFile( TDPath.getSurveySrvFile( TDInstance.survey ), TDPath.getSurveySrvFile( name ) );
        TDUtil.renameFile( TDPath.getSurveySurFile( TDInstance.survey ), TDPath.getSurveySurFile( name ) );
        TDUtil.renameFile( TDPath.getSurveySvxFile( TDInstance.survey ), TDPath.getSurveySvxFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyTopFile( TDInstance.survey ), TDPath.getSurveyTopFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyTrbFile( TDInstance.survey ), TDPath.getSurveyTrbFile( name ) );
        TDUtil.renameFile( TDPath.getSurveyTroFile( TDInstance.survey ), TDPath.getSurveyTroFile( name ) );

      { // rename note file: note
        TDUtil.renameFile( TDPath.getSurveyNoteFile( TDInstance.survey ), TDPath.getSurveyNoteFile( name ) );
      }
      { // rename photo folder: photo
        TDUtil.renameFile( TDPath.getSurveyPhotoDir( TDInstance.survey ), TDPath.getSurveyPhotoDir( name ) );
      }
      { // rename audio folder: audio
        TDUtil.renameFile( TDPath.getSurveyAudioDir( TDInstance.survey ), TDPath.getSurveyAudioDir( name ) );
      }
      TDInstance.survey = name;
      return true;
    }
    return false;
  }
    
  /** update windows title and display
   */
  private static void updateWindows()
  {
    if ( mShotWindow != null) {
      mShotWindow.setTheTitle();
      mShotWindow.updateDisplay();
    }
    if ( mSurveyWindow != null ) {
      mSurveyWindow.setTheTitle();
      mSurveyWindow.updateDisplay();
    }
  }

  /**
   * @param name      survey name
   * @param datamode  survey datamode
   */
  long setSurveyFromName( String name, int datamode, boolean update )
  { 
    TDInstance.sid      = -1;       // no survey by default
    TDInstance.survey   = null;
    TDInstance.datamode = 0;
    // TDINstance.extend   = SurveyInfo.SURVEY_EXTEND_NORMAL;
    StationName.clearCurrentStation();
    // resetManualCalibrations();
    ManualCalibration.reset();

    if ( name != null && mData != null ) {
      // Log.v( "DistoX", "set SurveyFromName <" + name + ">");

      TDInstance.sid = mData.setSurvey( name, datamode );
      // mFixed.clear();
      TDInstance.survey = null;
      if ( TDInstance.sid > 0 ) {
        DistoXStationName.setInitialStation( mData.getSurveyInitStation( TDInstance.sid ) );
        TDInstance.survey = name;
	TDInstance.datamode = mData.getSurveyDataMode( TDInstance.sid );
	// Log.v("DistoX", "set survey from name: <" + name + "> datamode " + datamode + " " + TDInstance.datamode );
        TDInstance.secondLastShotId = lastShotId();
        // restoreFixed();
	if ( update ) updateWindows();
        TDInstance.xsections = ( SurveyInfo.XSECTION_SHARED == mData.getSurveyXSections( TDInstance.sid ) );

        // TDInstance.extend = 
        int extend = mData.getSurveyExtend( TDInstance.sid );
        // Log.v( "DistoXE", "set SurveyFromName extend: " + extend );
        if ( SurveyInfo.isExtendLeft( extend ) ) { 
          TDAzimuth.mFixedExtend = -1L;
        } else if ( SurveyInfo.isExtendRight( extend ) ) { 
          TDAzimuth.mFixedExtend = 1L;
        } else {
          TDAzimuth.mFixedExtend = 0;
          TDAzimuth.mRefAzimuth  = extend;
        }
      }
      return TDInstance.sid;
    }
    return 0;
  }

  boolean moveSurveyData( long old_sid, long old_id, String new_survey )
  {
    if ( mData == null ) return false;
    long new_sid = mData.getSurveyId( new_survey );
    // Log.v("DistoX-MOVE", "SID " + old_sid + " " + new_sid + " ID " + old_id );
    if ( new_sid <= 0 || new_sid == old_sid ) return false;
    return mData.moveShotsBetweenSurveys( old_sid, old_id, new_sid );
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

  private void setDefaultSocketType( )
  {
    String defaultSockType = ( android.os.Build.MANUFACTURER.equals("samsung") ) ? "1" : "0";
    TDPrefHelper.update( "DISTOX_SOCK_TYPE", defaultSockType ); 
  }

  static void setCWDPreference( String cwd, String cbd )
  { 
    if ( TDInstance.cwd.equals( cwd ) && TDInstance.cbd.equals( cbd ) ) return;
    // Log.v("DistoX", "setCWDPreference " + cwd );
    TDPrefHelper.update( "DISTOX_CWD", cwd, "DISTOX_CBD", cbd ); 
    setCWD( cwd, cbd ); 
  }

  static void setPtCmapPreference( String cmap )
  {
    TDPrefHelper.update( "DISTOX_PT_CMAP", cmap ); 
    PtCmapActivity.setMap( cmap );
  }

  // unused
  // void setAccuracyPreference( float acceleration, float magnetic, float dip )
  // {
  //   TDPrefHelper.update( "DISTOX_ACCEL_THR", Float.toString( acceleration ), "DISTOX_MAG_THR", Float.toString( magnetic ), "DISTOX_DIP_THR", Float.toString( dip ) ); 
  // }

  static void setTextSize( int ts )
  {
    TDSetting.setTextSize( ts );
    if ( TDSetting.setLabelSize( ts*3, false ) || TDSetting.setStationSize( ts*2, false ) ) { // false: do not update brush
      BrushManager.setTextSizes( );
    }
    TDPrefHelper.update( "DISTOX_TEXT_SIZE", Integer.toString(ts), "DISTOX_LABEL_SIZE", Float.toString(ts*3), "DISTOX_STATION_SIZE", Float.toString(ts*2) );
  }

  static void setButtonSize( int bs )
  {
    // Log.v("DistoX-BUTTON", "set button size " + bs );
    TDSetting.setSizeButtons( bs );
    TDPrefHelper.update( "DISTOX_SIZE_BUTTONS", Integer.toString(bs) );
  }

  static void setDrawingUnitIcons( float u )
  {
    TDSetting.setDrawingUnitIcons( u );
    TDPrefHelper.update( "DISTOX_DRAWING_UNIT", Float.toString(u) );
  }

  static void setDrawingUnitLines( float u )
  {
    TDSetting.setDrawingUnitLines( u );
    TDPrefHelper.update( "DISTOX_LINE_UNITS", Float.toString(u) );
  }

  // used for "DISTOX_WELCOME_SCREEN" and "DISTOX_TD_SYMBOL"
  static void setBooleanPreference( String preference, boolean val ) { TDPrefHelper.update( preference, val ); }

  // FIXME_DEVICE_STATIC
  void setDevice( String address, BluetoothDevice bt_device )
  { 
    // Log.v("DistoXBLE", "set device address " + address + " BLE " + ((bt_device!=null)? "yes" : "no") );
    if ( address == null ) {
      // if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this ); // FIXME VirtualDistoX
      TDInstance.deviceA = null;
      address = TDString.EMPTY;
    // } else if ( address.equals( Device.ZERO_ADDRESS )  ) { // FIXME VirtualDistoX
    //   if ( mVirtualDistoX != null ) mVirtualDistoX.startServer( this );
    //   // boolean create = ( TDInstance.deviceA == null || ! address.equals( TDInstance.deviceAddress() ) );
    //   boolean create = ( ! TDInstance.isDeviceAddress( address ) );
    //   TDInstance.deviceA = new Device( address, "DistoX0", "X000", null );
    //   if ( create ) createComm();
    } else {
      // if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this ); // FIXME VirtualDistoX
      // boolean create = ( TDInstance.deviceA == null || TDInstance.deviceAddress().equals( Device.ZERO_ADDRESS ) );
      if ( bt_device != null ) { // BLE device is temporary
        deleteComm();
        // if ( TDInstance.deviceType() == Device.DISTO_BLE5 ) {
        //   Log.v("DistoXBLE", "update address " + address );
        //   TDInstance.deviceA.mAddress = address;
        // } else {
          // address, model, head, tail, name, nickname
          // model could be "BLE5" or start with DistoX-BLE
          TDInstance.deviceA = new Device( address, "DistoX-BLE", 0, 0, null, null );
          TDInstance.bleDevice = bt_device;
        // }
        // Log.v("DistoXBLE", "create BLE address " + address );
        mComm = new BleComm( this, address, bt_device ); // FIXME BLE
      } else {
        boolean create = TDInstance.isDeviceZeroAddress() || (TDInstance.deviceType() == Device.DISTO_BLE5);
        TDInstance.deviceA = mDData.getDevice( address );
        TDInstance.bleDevice = null;
        if ( create ) createComm();
      }
    }
    TDPrefHelper.update( TDSetting.keyDeviceName(), address ); 
  }

  // TODO BLE for the second DistoX
  void setDeviceB( String address )
  {
    if ( address == null ) {
      // if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this ); // FIXME VirtualDistoX
      TDInstance.deviceB = null;
      // address = TDString.EMPTY;
    } else {
      TDInstance.deviceB = mDData.getDevice( address );
    }
  }

  boolean switchSecondDevice()
  {
    return TDInstance.switchDevice();
  }

  // -------------------------------------------------------------
  // DATA BATCH DOWNLOAD

  int downloadDataBatch( Handler /* ILister */ lister, int data_type ) // FIXME_LISTER
  {
    TDInstance.secondLastShotId = lastShotId();
    int ret = 0;
    if ( mComm == null || TDInstance.deviceA == null ) {
      TDLog.Error( "Comm or Device null ");
    } else {
      TDLog.Log( TDLog.LOG_DATA, "Download Data Batch() device " + TDInstance.deviceAddress() + " comm " + mComm.toString() );
      ret = mComm.downloadData( TDInstance.deviceA.mAddress, lister, data_type );
      // FIXME BATCH
      // if ( ret > 0 && TDSetting.mSurveyStations > 0 ) {
      //   // FIXME TODO select only shots after the last leg shots
      //   List< DBlock > list = mData.selectAllShots( TDInstance.sid, TDStataus.NORMAL );
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

  String getFirstPlotOrigin() { return ( TDInstance.sid < 0 )? null : mData.getFirstPlotOrigin( TDInstance.sid ); }


  // static long trobotmillis = 0L; // TROBOT_MILLIS

  // called also by ShotWindow::updataBlockList
  // this re-assign stations to shots with station(s) already set
  // the list of stations is ordered by compare
  //
  // @param list list of shot to assign
  boolean assignStationsAfter( DBlock blk0, List< DBlock > list )
  { 
    Set<String> sts = mData.selectAllStationsBefore( blk0.mId, TDInstance.sid /*, TDStatus.NORMAL */ );
    // Log.v("DistoX-DATA", "assign stations after " + blk0.Name() + " size " + list.size() + " stations " + sts.size() );
    // if ( TDSetting.mSurveyStations < 0 ) return;
    StationName.clearCurrentStation();
    if ( StationPolicy.doTopoRobot() ) {
      // long millis = SystemClock.uptimeMillis(); // TROBOT_MILLIS
      // if ( millis > trobotmillis + 10000 ) {
      //   TDToast.make( R.string.toporobot_warning );
      //   trobotmillis = millis;
      // }
      return new StationNameTRobot(this, mData, TDInstance.sid ).assignStationsAfter( blk0, list, sts );
    } else  if ( StationPolicy.doBacksight() ) {
      return new StationNameBacksight(this, mData, TDInstance.sid ).assignStationsAfter( blk0, list, sts );
    } else if ( StationPolicy.doTripod() ) {
      return new StationNameTripod( this, mData, TDInstance.sid ).assignStationsAfter( blk0, list, sts );
    } 
    return new StationNameDefault( this, mData, TDInstance.sid ).assignStationsAfter( blk0, list, sts );
  }

  // called also by ShotWindow::updataBlockList
  // @param list blocks whose stations need to be set in the DB
  // return true if a leg was assigned
  //
  boolean assignStationsAll(  List< DBlock > list )
  { 
    Set<String> sts = mData.selectAllStations( TDInstance.sid );
    int sz = list.size();
    if ( sz == 0 ) return false;
    TDLog.Log( TDLog.LOG_DATA, "assign stations all: size " + sz + " blk[0] id " + list.get(0).mId );

    // if ( TDSetting.mSurveyStations < 0 ) return;
    if ( StationPolicy.doTopoRobot() ) {
      // long millis = SystemClock.uptimeMillis(); // TROBOT_MILLIS
      // if ( millis > trobotmillis + 10000 ) {
      //   TDToast.make( R.string.toporobot_warning );
      //   trobotmillis = millis;
      // }
      return new StationNameTRobot(this, mData, TDInstance.sid ).assignStations( list, sts );
    } else  if ( StationPolicy.doBacksight() ) {
      return new StationNameBacksight(this, mData, TDInstance.sid ).assignStations( list, sts );
    } else if ( StationPolicy.doTripod() ) {
      return new StationNameTripod( this, mData, TDInstance.sid ).assignStations( list, sts );
    } 
    return new StationNameDefault( this, mData, TDInstance.sid ).assignStations( list, sts );
  }

  // ================================================================
  // EXPORTS

  static void exportSurveyAsCsxAsync( Context context, String origin, PlotSaveData psd1, PlotSaveData psd2, boolean toast )
  {
    SurveyInfo info = getSurveyInfo();
    if ( info == null ) {
      TDLog.Error("Error: null survey info");
      // Log.v("DistoX-SAVE", "null survey info");
      return;
    }
    String fullname = ( psd1 == null )? TDInstance.survey
                                      : TDInstance.survey + "-" + psd1.name;
    String filename = TDPath.getSurveyCsxFile( fullname );
    TDLog.Log( TDLog.LOG_IO, "exporting as CSX " + fullname + " " + filename );
    // Log.v("DistoX-SAVE", "exporting as CSX " + filename );
    (new SaveFullFileTask( context, TDInstance.sid, mData, info, psd1, psd2, origin, filename, fullname, 
       TDPath.getCsxFile(""), toast )).execute();
  }

  // FIXME_SYNC might be a problem with big surveys
  // this is called sync to pass the therion file to the 3D viewwer
  // static boolean exportSurveyAsThSync( )
  // {
  //   SurveyInfo info = getSurveyInfo();
  //   if ( info == null ) return false;
  //   // if ( async ) {
  //   //   String saving = context.getResources().getString(R.string.saving_);
  //   //   (new SaveDataFileTask( saving, TDInstance.sid, info, mData, TDInstance.survey, null, TDConst.DISTOX_EXPORT_TH, toast )).execute();
  //   //   return true;
  //   // }
  //   return ( TDExporter.exportSurveyAsTh( TDInstance.sid, mData, info, TDPath.getSurveyThFile( TDInstance.survey ) ) != null );
  // }

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
  // FIRMWARE 

  static private void installFirmware( boolean overwrite )
  {
    InputStream is = TDInstance.getResources().openRawResource( R.raw.firmware );
    firmwareUncompress( is, overwrite );
    try { is.close(); } catch ( IOException e ) { }
  }
 
  // -------------------------------------------------------------
  // SYMBOLS

  static void installSymbols( boolean overwrite )
  {
    deleteObsoleteSymbols();
    // Log.v("DistoX-PATH", "install symbol version " + TDVersion.SYMBOL_VERSION );
    installSymbols( R.raw.symbols_speleo, overwrite );
    mDData.setValue( "symbol_version", TDVersion.SYMBOL_VERSION );
  }

  static void installSymbols( int res, boolean overwrite )
  {
    InputStream is = TDInstance.getResources().openRawResource( res );
    symbolsUncompress( is, overwrite );
  }

  static private void deleteObsoleteSymbols()
  {
    String[] lines = { "blocks", "debris", "clay", "contour", "presumed", "sand", "ice", "section", "wall:sand" };
    for ( String line : lines ) {
      TDUtil.deleteFile( TDPath.APP_LINE_PATH + line );
    }
    String[] points = { "breakdown-choke", "low-end", "paleo-flow", "section" };
    for ( String point : points ) {
      TDUtil.deleteFile( TDPath.APP_POINT_PATH + point );
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
                      boolean speleo, boolean extra, boolean mine, boolean geo, boolean archeo, boolean anthro, boolean paleo,
                      boolean bio,    boolean karst )
  {
    // Log.v("DistoX", "Reload symbols " + speleo + " " + mine + " " + geo + " " + archeo + " " + paleo + " " + bio + " clear " + clear );
    // if ( extra ) speleo = true; // extra implies speleo

    if ( clear ) {
      if (speleo || extra || mine || geo || archeo || anthro || paleo || bio || karst ) { 
        clearSymbols();
      }
    }
    if ( speleo ) installSymbols( R.raw.symbols_speleo, true );
    if ( extra  ) installSymbols( R.raw.symbols_extra,  true );
    if ( mine   ) installSymbols( R.raw.symbols_mine,   true );
    if ( geo    ) installSymbols( R.raw.symbols_geo,    true );
    if ( archeo ) installSymbols( R.raw.symbols_archeo, true );
    if ( anthro ) installSymbols( R.raw.symbols_anthro, true );
    if ( paleo  ) installSymbols( R.raw.symbols_paleo,  true );
    if ( bio    ) installSymbols( R.raw.symbols_bio,    true );
    if ( karst  ) installSymbols( R.raw.symbols_karst,  true );

    mDData.setValue( "symbol_version", TDVersion.SYMBOL_VERSION );
    BrushManager.loadAllLibraries( this, getResources() );
  }

  static private void symbolsUncompress( InputStream fis, boolean overwrite )
  {
    // Log.v("DistoX-PATH", "uncompress symbols");
    TDPath.symbolsCheckDirs();
    try {
      // byte buffer[] = new byte[36768];
      byte[] buffer = new byte[4096];
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
          // Log.v("DistoX-PATH", "uncompress symbol " + pathname );
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

  // -------------------------------------------------------------------

  static private void firmwareUncompress( InputStream fis, boolean overwrite )
  {
    // Log.v(TAG, "firmware uncompress ...");
    TDPath.checkBinDir( );
    try {
      // byte buffer[] = new byte[36768];
      byte[] buffer = new byte[4096];
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
   * could return the long at
   */
  long insertLRUDatStation( long at, String splay_station, float bearing, float clino,
                            String left, String right, String up, String down )
  {
    // Log.v("DistoX-LRUD", "isert LRUD " + at + " station " + splay_station );
    return addManualSplays( at, splay_station, left, right, up, down, bearing, false, true ); // horizontal=false, true=ret +/-1;
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
    // reset current station so that the next shot does not attach to the intermediate leg
    resetCurrentOrLastStation( );
    long millis = java.lang.System.currentTimeMillis()/1000;
    distance = distance / TDSetting.mUnitLength;
    long id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend, 0.0, LegType.NORMAL, 1 );
    mData.updateShotName( id, TDInstance.sid, from, to );
    mData.updateShotFlag( id, TDInstance.sid, DBlock.FLAG_DUPLICATE );
    return id;
  }

  private long addManualSplays( long at, String splay_station, String left, String right, String up, String down,
                                float bearing, boolean horizontal, boolean ret_success )
  {
    long id;
    long millis = java.lang.System.currentTimeMillis()/1000;
    long extend = 0L;
    float calib = ManualCalibration.mLRUD ? ManualCalibration.mLength / TDSetting.mUnitLength : 0;
    float l = -1.0f;
    float r = -1.0f;
    float u = -1.0f;
    float d = -1.0f;
    boolean ok = false;
    if ( left != null && left.length() > 0 ) {
      try {
        l = Float.parseFloat( left ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: left " + ((left==null)?"null":left) );
      }
    }  
    if ( right != null && right.length() > 0 ) {
      try {
        r = Float.parseFloat( right ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: right " + ((right==null)?"null":right) );
      }
    }
    if ( up != null && up.length() > 0 ) {
      try {
        u = Float.parseFloat( up ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: up " + ((up==null)?"null":up) );
      }
    }
    if ( down != null && down.length() > 0 ) {
      try {
        d = Float.parseFloat( down ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.Error( "manual-shot parse error: down " + ((down==null)?"null":down) );
      }
    }

    extend = DBlock.EXTEND_IGNORE;
    if ( l >= 0.0f ) { // FIXME_X_SPLAY
      ok = true;
      if ( horizontal ) { // WENS
        // extend = TDAzimuth.computeSplayExtend( 270 );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( 270 ) : DBlock.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, l, 270.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, l, 270.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      } else {
        float b = bearing - 90.0f;
        if ( b < 0.0f ) b += 360.0f;
        // extend = TDAzimuth.computeSplayExtend( b );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( b ) : DBlock.EXTEND_UNSET;
        // b = in360( b );
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, l, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, l, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // Log.v("DistoX-LRUD", "insert " + id + " left " + l );
    }
    if ( r >= 0.0f ) {
      ok = true;
      if ( horizontal ) { // WENS
        // extend = TDAzimuth.computeSplayExtend( 90 );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( 90 ) : DBlock.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, r, 90.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, r, 90.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      } else {
        // float b = bearing + 90.0f; if ( b >= 360.0f ) b -= 360.0f;
        float b = TDMath.add90( bearing );
        // extend = TDAzimuth.computeSplayExtend( b );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( b ) : DBlock.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, r, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, r, b, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // Log.v("DistoX-LRUD", "insert " + id + " right " + r );
    }
    extend = DBlock.EXTEND_VERT;
    if ( u >= 0.0f ) {  
      ok = true;
      if ( horizontal ) {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, u, 0.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, u, 0.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      } else {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, u, 0.0f, 90.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, u, 0.0f, 90.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // Log.v("DistoX-LRUD", "insert " + id + " up " + u );
    }
    if ( d >= 0.0f ) {
      ok = true;
      if ( horizontal ) {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, d, 180.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, d, 180.0f, 0.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      } else {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, d, 0.0f, -90.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, d, 0.0f, -90.0f, 0.0f, extend, 0.0, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // Log.v("DistoX-LRUD", "insert " + id + " down " + d );
    }
    // Log.v("DistoX-LRUD", "add manual returns " + at );
    if ( ret_success ) return (ok ? 1L : -1L);
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
      TDToast.makeBad( R.string.illegal_data_value );
      return null;
    }
    bearing = TDMath.in360( (bearing  - ManualCalibration.mAzimuth) / TDSetting.mUnitAngle );
    // while ( bearing >= 360 ) bearing -= 360;
    // while ( bearing <    0 ) bearing += 360;

    if ( from != null && to != null && from.length() > 0 ) {
      // if ( mData.makesCycle( -1L, TDInstance.sid, from, to ) ) {
      //   TDToast.make( R.string.makes_cycle );
      // } else
      {
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > TDSetting.mVThreshold );
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot SID " + TDInstance.sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( StationPolicy.mShotAfterSplays ) {
          at = addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal, false );

          if ( at >= 0L ) {
            id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1 );
          } else {
            id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1 );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, TDInstance.sid, from, to );
          // mData.updateShotExtend( id, TDInstance.sid, extend0, stretch0 );
          // mData.updateShotExtend( id, TDInstance.sid, DBlock.EXTEND_IGNORE, 1 ); // FIXME WHY ???
          // FIXME updateDisplay( );
        } else {
          if ( at >= 0L ) {
            id = mData.insertManualShotAt( TDInstance.sid, at, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1 );
            ++ at;
          } else {
            id = mData.insertManualShot( TDInstance.sid, -1L, millis, 0, distance, bearing, clino, 0.0f, extend0, 0.0, LegType.NORMAL, 1 );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, TDInstance.sid, from, to );
          // mData.updateShotExtend( id, TDInstance.sid, extend0, stretch0 );
          // mData.updateShotExtend( id, TDInstance.sid, DBlock.EXTEND_IGNORE, 1 );  // FIXME WHY ???
          // FIXME updateDisplay( );

          addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal, false );
        }
        ret = mData.selectShot( id, TDInstance.sid );
      }
    } else {
      TDToast.makeBad( R.string.missing_station );
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
    if ( TDInstance.deviceA == null ) return CalibInfo.ALGO_LINEAR;
    if ( TDInstance.deviceType() == Device.DISTO_A3 ) return CalibInfo.ALGO_LINEAR; // A3
    if ( TDInstance.deviceType() == Device.DISTO_X310 ) {
      if ( mComm == null ) return 1; // should not happen
      byte[] ret = mComm.readMemory( TDInstance.deviceAddress(), 0xe000 );
      if ( ret != null && ( ret[0] >= 2 && ret[1] >= 3 ) ) return CalibInfo.ALGO_NON_LINEAR;
    }
    return CalibInfo.ALGO_LINEAR; // default
  }  

  // --------------------------------------------------------

  /** 
   * @param what      what to do
   * @param nr        number od data to download
   # @param lister    optional lister
   */
  void setX310Laser( int what, int nr, Handler /* ILister */ lister, int data_type ) // 0: off, 1: on, 2: measure // FIXME_LISTER
  {
    if ( mComm == null || TDInstance.deviceA == null ) return;
    DistoX310Comm comm = (DistoX310Comm)mComm;
    if ( comm != null ) comm.setX310Laser( TDInstance.deviceAddress(), what, nr, lister, data_type );
  }

  // int readFirmwareHardware()
  // {
  //   return mComm.readFirmwareHardware( TDInstance.deviceA.mAddress );
  // }

  int dumpFirmware( String filename )
  {
    if ( mComm == null || TDInstance.deviceA == null ) return -1;
    DistoX310Comm comm = (DistoX310Comm)mComm;
    return comm.dumpFirmware( TDInstance.deviceAddress(), TDPath.getBinFile(filename) );
  }

  int uploadFirmware( String filename )
  {
    if ( mComm == null || TDInstance.deviceA == null ) {
      TDLog.Error( "Comm or Device null");
      return -1;
    }
    String pathname = TDPath.getBinFile( filename );
    TDLog.LogFile( "Firmware upload address " + TDInstance.deviceAddress() );
    TDLog.LogFile( "Firmware upload file " + pathname );
    if ( ! pathname.endsWith( "bin" ) ) {
      TDLog.LogFile( "Firmware upload file does not end with \"bin\"");
      return 0;
    }
    DistoX310Comm comm = (DistoX310Comm)mComm;
    return comm.uploadFirmware( TDInstance.deviceAddress(), pathname );
  }

  // ----------------------------------------------------------------------

  long insert2dPlot( long sid , String name, String start, boolean extended, int project )
  {
    // PlotInfo.ORIENTATION_PORTRAIT = 0
    // TDLog.Log( TDLog.LOG_PLOT, "new plot " + name + " start " + start );
    long pid_p = mData.insertPlot( sid, -1L, name+"p",
                 PlotInfo.PLOT_PLAN, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, 0, 0, TDString.EMPTY, TDString.EMPTY, 0 );
    if ( extended ) {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotInfo.PLOT_EXTENDED, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, 0, 0, TDString.EMPTY, TDString.EMPTY, 0 );
    } else {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotInfo.PLOT_PROJECTED, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, project, 0, TDString.EMPTY, TDString.EMPTY, 0 );
    }
    return pid_p;
  }
  
  // @param azimuth clino : projected profile azimuth / section plane direction 
  // @param parent parent plot name
  // NOTE field "hide" is overloaded for x_sections with the parent plot name
  long insert2dSection( long sid, String name, long type, String from, String to, float azimuth, float clino, String parent, String nickname )
  {
    // FIXME COSURVEY 2d sections are not forwarded
    // 0 0 mScaleFactor : offset and zoom
    String hide = ( parent == null )? TDString.EMPTY : parent;
    String nick = ( nickname == null )? TDString.EMPTY : nickname;
    return mData.insertPlot( sid, -1L, name, type, 0L, from, to, 0, 0, mScaleFactor, azimuth, clino, hide, nick, 0 );
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
  //     
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
  //       TDToast.makeBad( "Photo display not yet implemented" );
  //     }
  //   } else {
  //     TDToast.makeBad( "ERROR file not found: " + filename );
  //   }
  // }

  // ---------------------------------------------------------------------
  /* ---- IF_COSURVEY

  // DataListener (co-surveying)
  private DataListenerSet mDataListeners;

  // synchronized( mDataListener )
  void registerDataListener( DataListener listener ) { mDataListeners.registerDataListener( listener ); }

  // synchronized( mDataListener )
  void unregisterDataListener( DataListener listener ) { mDataListeners.unregisterDataListener( listener ); }

  static boolean mCosurvey = false;       // whether co-survey is enable by the DB
  static boolean mCoSurveyServer = false; // whether co-survey server is on
  static ConnectionHandler mSyncConn = null;

  // used by TDSetting
  void setCoSurvey( boolean co_survey ) // FIXME interplay with TDSetting
  {
    if ( ! mCosurvey ) {
      mCoSurveyServer = false;
      TDPrefHelper.update( "DISTOX_COSURVEY", false );
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
    return ( mSyncConn == null )? TDString.EMPTY : mSyncConn.getConnectionStateTitleStr();
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
    TDToast.makeBad( "Sync connection failed" );
  }

  void syncConnectedDevice( String name )
  {
    TDToast.make( "Sync connected " + name );
    if ( mSyncConn != null ) registerDataListener( mSyncConn );
  }

  */

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
  // cannot be static because register/unregister are not static

  static PairingRequest mPairingRequest = null;

  public static void checkAutoPairing()
  {
    if ( thisApp == null ) return;
    if ( TDSetting.mAutoPair ) {
      thisApp.startPairingRequest();
    } else {
      thisApp.stopPairingRequest();
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

  private void startPairingRequest()
  {
    if ( mPairingRequest == null ) {
      // Log.v("DistoX", "start pairing" );
      // IntentFilter filter = new IntentFilter( DeviceUtil.ACTION_PAIRING_REQUEST );
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
      if ( warn ) TDToast.makeBad( R.string.no_survey );
    } else {
      SurveyInfo info = getSurveyInfo( );
      if ( info == null ) return;
      TDLog.Log( TDLog.LOG_IO, "async-export survey " + TDInstance.survey + " type " + exportType );
      String saving = context.getResources().getString(R.string.saving_);
      (new SaveDataFileTask( saving, TDInstance.sid, info, mData, TDInstance.survey, TDInstance.deviceA, exportType, true )).execute();
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
      (new SaveDataFileTask( null, TDInstance.sid, info, mData, TDInstance.survey, TDInstance.deviceA, exportType, false )).immed_exec();
    }
  }

  // ==================================================================
  // PATH_11

  static void setPath11NoAgain()
  {
    if ( mData != null ) mData.setValue( "Path11", "1" );
  }

  static boolean hasPath11NoAgain()
  {
    if ( mData != null ) {
      String no_again = mData.getValue( "Path11" );
      return ( no_again != null && no_again.equals("1") );
    }
    return false;
  }

}
