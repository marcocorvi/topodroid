/* @file TopoDroidApp.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid application (constants and preferences)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.dev.cavway.CavwayComm;
import com.topodroid.dev.cavway.CavwayInfoDialog;
import com.topodroid.dev.distox_ble.DistoXBLEComm; // SIWEI_TIAN
// import com.topodroid.dev.distox_ble.DistoXBLEConst;
import com.topodroid.dev.distox_ble.DistoXBLEInfoDialog;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDVersion;
import com.topodroid.utils.TDLocale;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.MyFileProvider;
// import com.topodroid.utils.TDStatus;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDProgress;
// import com.topodroid.prefs.TDPrefActivity;
import com.topodroid.prefs.TDPrefHelper;
import com.topodroid.prefs.TDSetting;
import com.topodroid.packetX.MemoryOctet;
import com.topodroid.packetX.CavwayData;
import com.topodroid.dev.Device;
// import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.TopoDroidComm;
import com.topodroid.dev.DataType;
import com.topodroid.dev.distox.IMemoryDialog;
import com.topodroid.dev.distox1.DistoXA3Comm;
import com.topodroid.dev.distox1.DeviceA3Info;
import com.topodroid.dev.distox2.DistoX310Comm;
import com.topodroid.dev.distox2.DeviceX310Info;
import com.topodroid.dev.distox2.DeviceX310Details;
import com.topodroid.dev.sap.SapComm;
import com.topodroid.dev.bric.BricComm;
import com.topodroid.dev.bric.BricInfoDialog;
import com.topodroid.dev.PairingRequest;
import com.topodroid.dev.cavway.CavwayMemoryDialog;
import com.topodroid.common.LegType;
import com.topodroid.common.ExtendType;
import com.topodroid.common.PlotType;
import com.topodroid.common.ExportInfo;
// import com.topodroid.calib.CalibCoeffDialog;
// import com.topodroid.calib.CalibReadTask;
import com.topodroid.calib.CalibInfo;
import com.topodroid.calib.CalibExport;


import java.io.File; // PRIVATE FILE firmware-bin
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
// import java.io.FileReader;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
// import java.util.Stack;

// import android.os.Environment;
// import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
// import android.os.Debug;
// import android.os.SystemClock; // FIXME TROBOT

// import android.database.sqlite.SQLiteDatabase;

import android.app.Application;
// import android.app.Activity;
// import android.app.Notification;
// import android.app.NotificationManager;
// import android.app.KeyguardManager;
// import android.app.KeyguardManager.KeyguardLock;

import android.content.Context;
// import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.Configuration;

// import android.content.pm.PackageManager;
// import android.content.pm.PackageManager.NameNotFoundException;
// import android.content.FileProvider;

import android.net.Uri;

// import android.provider.Settings;
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

// SIWEI_TIAN changed on Jun 2022
public class TopoDroidApp extends Application
{
  // static final String EMPTY = "";
  static private TopoDroidApp thisApp = null;

  static private GeoCodes mGeoCodes = null; // singleton

  static synchronized GeoCodes getGeoCodes()
  {
    if ( mGeoCodes == null ) mGeoCodes = new GeoCodes();
    return mGeoCodes;
  }

  /** check if the app has the database helper
   * @return true if the app has the database helper
   */
  static boolean hasTopoDroidDatabase() 
  { 
    // return ( TDFile.getTopoDroidFile( TDPath.getDatabase() ).exists() );
    return mData != null && mData.hasDB();
  }

  boolean mWelcomeScreen;  // whether to show the welcome screen (used by MainWindow)
  boolean mSetupScreen;    // whether to show the welcome screen (used by MainWindow)
  boolean mHasTDX;         // whether there is TDX folder
  static boolean mCheckManualTranslation = false;
  // static String mManual;  // manual url

  // DialogR controller - only for PRIVATE_STORAGE
  // static private boolean say_dialogR = false; // ! TopoDroidApp.hasTopoDroidDatabase(); // updated by showInitDialogs

  /** @return  whether to say the dialogR or not
   */
  static boolean sayDialogR() 
  { 
    if ( mDData == null ) return true;
    String res = mDData.getValue( "say_dialogR" );
    boolean ret = ( res == null || ! res.equals("NO") );
    // TDLog.v("TD say dialog R " + ret );
    return ret;
  }

  /** set the say_dialog_R config
   * @param res   whether to say the dialogR or not
   */
  static void setSayDialogR( boolean res ) 
  { 
    // TDLog.v("TD set dialog R " + res );
    if ( mDData != null ) mDData.setValue( "say_dialogR", ( res? "YES" : "NO" ) );
  }


  // static int mCheckPerms;

  static String mClipboardText = null; // text clipboard

  public static float mScaleFactor   = 1.0f; // DisplayHeight / 320 * density, eg, 2040/320*3.0 = 19.15...
  public static float mDisplayWidth  = 200f; // TDInstance.context.getResources().getDisplayMetrics().widthPixels; // 200f;
  public static float mDisplayHeight = 320f; // TDInstance.context.getResources().getDisplayMetrics().heightPixels; // 320f;
  public static float mBorderRight      = 4096;
  public static float mBorderLeft       = 0;
  public static float mBorderInnerRight = 4096;
  public static float mBorderInnerLeft  = 0;
  public static float mBorderTop        = 0;
  public static float mBorderBottom     = 4096; // in DrawingWindow

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
  public ListerSetHandler mListerSet; // FIXME_LISTER

  /** register a lister in the lister-set
   * @param lister   data lister to register
   */
  void registerLister( ILister lister ) { if ( lister != null ) mListerSet.registerLister( lister ); }

  /** unregister a lister from the lister-set
   * @param lister   data lister to unregister
   */
  void unregisterLister( ILister lister ) { if ( lister != null ) mListerSet.unregisterLister( lister ); }

  /** notify a new status to the data listers
   * @param lister  data lister
   * @param status new status
   */
  public void notifyListerStatus( ListerHandler lister, final int status )
  { 
    // TDLog.v( "TDApp: notify status " + status );
    if ( lister == null ) return;
    if ( mMainActivity == null ) return;
    mMainActivity.runOnUiThread( new Runnable() { 
      public void run () { 
        // TDLog.v("APP notify " + status + " to listers " + mListerSet.size() );
        // mListerSet.setConnectionStatus( status );
        lister.setConnectionStatus( status );
      }
    } );
  }

  /** receive a disconnection notification
   * @param data_type   type of data that are downloaded
   */
  public void notifyDisconnected( ListerHandler lister, int data_type )
  {
    if ( mListerSet.size() > 0 ) {
      try {
        new ReconnectTask( mDataDownloader, lister, data_type, 500 ).execute();
      } catch ( RuntimeException e ) {
        TDLog.e("reconnect error: " + e.getMessage() );
      }
    }
  }

  // -----------------------------------------------------

  // FIXME INSTALL_SYMBOL boolean askSymbolUpdate = false; // by default do not ask

  String[] DistoXConnectionError;
  // BluetoothAdapter mBTAdapter = null;     // BT connection
  private TopoDroidComm mComm = null;     // BT communication
  public DataDownloader mDataDownloader = null;  // data downloader
  public static DataHelper mData = null;         // database 
  public static DeviceHelper mDData = null;      // device/calib database

  // public static TDPrefHelper mPrefHlp      = null;
  static SurveyWindow mSurveyWindow          = null; // FIXME ref Survey Activity
  public static ShotWindow mShotWindow       = null; // FIXME ref Shot Activity - public for prefs/TDSetting
  public static DrawingWindow mDrawingWindow = null;
  public static MainWindow mMainActivity     = null; // FIXME ref Main Activity
  public static TopoGL mTopoGL               = null; // FIXME

  /** @return the ID of the last shot
   */
  static long lastShotId( ) { return mData.getLastShotId( TDInstance.sid ); }

  static StationName mStationName = null;

  public static void resetRecentTools() 
  {
    if ( mDrawingWindow != null ) mDrawingWindow.resetRecentTools();
  }

  // static Device mDevice = null;
  // static int deviceType() { return (mDevice == null)? 0 : mDevice.mType; }
  // static String distoAddress() { return (mDevice == null)? null : mDevice.getAddress(); }

  // FIXME VirtualDistoX
  // VirtualDistoX mVirtualDistoX = new VirtualDistoX();

  /** notify the drawing window to update the midline display
   * @param blk_id    ID of the shot DBlock 
   * @param got_leg   whether a new leg has been detected
   */
  static void notifyDrawingUpdateDisplay( long blk_id, boolean got_leg )
  {
    if ( mDrawingWindow != null ) mDrawingWindow.notifyUpdateDisplay( blk_id, got_leg );
  }

  /** set the toolbar parameters of the drawing window
   */
  public static void setToolsToolbarParams()
  {
    if ( mDrawingWindow != null ) mDrawingWindow.setToolsToolbarParams();
  }

  // -------------------------------------------------------------------------------------
  // static SIZE methods

  /** @return the density of the display, e.g, 3.0
   */
  public static float getDisplayDensity( )
  {
    return Resources.getSystem().getDisplayMetrics().density;
  }

  // /** @return the density of the display
  //  * @param context   context (unused)
  //  */
  // public static float getDisplayDensity( Context context )
  // {
  //   return Resources.getSystem().getDisplayMetrics().density;
  // }

  /** @return the density DPI of the display, e.g., 480 // FIXED_ZOOM
   */
  public static int getDisplayDensityDpi( )
  {
    return Resources.getSystem().getDisplayMetrics().densityDpi;
  }

  /** @return the X dpi
   */
  public static float getDisplayXDpi( )
  {
    return Resources.getSystem().getDisplayMetrics().xdpi;
  }

  /** @return the Y dpi
   */
  public static float getDisplayYDpi( )
  {
    return Resources.getSystem().getDisplayMetrics().ydpi;
  }

  private static float CACHED_DENSITY = -1.0f;

  /** @return the system display density - for graph-paper
   */
  public static float getDensity()
  {
    if ( CACHED_DENSITY > 0 ) return CACHED_DENSITY;

    double log_xdpi = Math.log( Resources.getSystem().getDisplayMetrics().xdpi / 100.0 );
    double log_ydpi = Math.log( Resources.getSystem().getDisplayMetrics().ydpi / 100.0 );

    //              Android below-24 dpi density sys-density X-dpi   Y-dpi    density    adjust dp1cm 1:100A      
    // Nexus 4        5 21  true     320 2.0     320 *       319.79  318.745  640 (need)    ?         251.9685 (needed)   
    // Note 3         7 24  false    480 3.0     480         386.366 387.047  530 (need)  -50         208.66142 (needed) 
    // MiA2          10 29  false    408 2.55    480         397.565 474.688  480 (ok)      0         188.97638          
    //                               480 3.0                                                   
    //                               540 3.375                                                   
    // Samsung A3    11 30  false    420 2.625   420         409,432 411.891  500 (ok)      0         196.8504
    //                               450 2.8125                                                  
    //                               480 3.0                                                     
    //                               510 3.1875                                                  
    //                               540 3.375                                                   
    // Note Pro       5     true     320 2.0     320 *       239.94  236.28   830        -190         327.1653     
    // CAT S41        8     false    420 2.625   420         449.70  443.34   465          35         183.07086    
    // Tab S6        12 31  false    320 2.0     360         286.197 286.449  720 (710)  -200 (-190)  282.2835 (needed) 
    // Note 4         6     true     640 4.0     640 *       508     516.06   400         880         156.29921 
    // Xperia M2      5     true     240 1.5     240 *       232.47  234.46   900        -420         354.72443    
    // Galaxy S4      5     true     480 3.0     480 *       442.45  439.35   470         490         183.858
    // X-cover       11 30  false    320 2.0                 309.96  310.67                 ?                      
    //
    // Android dp are pixels
    // conversion: 1 m [world] = 20 units [scene]
    // not-used:   1 pt = 1.333333 pxl     1 pxl = 0.75 pt
    //
    // the dp per cm are: dp1cm = dpi * in/cm = 480 / 2.54 = 188.97637795 pxl/cm (correct 188.97638)
    // now 1 m becomes 20 pxl, 
    // therefore the scale 1:100 should have a zoom (188.97637795 pxl/cm) / (20 pxl/m) = 9.4488188975
    // 1600 is magic-number = 10 * 160
    //
    // Ys = 100 * exp( 3.00521 + -0.90490 * log(  dds/100 ) )
    // Yx = 100 * exp( 3.01876 + -1.00301 * log( xdpi/100 ) )
    // Yy = 100 * exp( 2.96281 + -0.94614 * log( ydpi/100 ) )
    int yx = 10 * (int)( 10 * Math.exp( 3.01876 + -1.00301 * log_xdpi ) );
    int yy = 10 * (int)( 10 * Math.exp( 2.96281 + -0.94614 * log_ydpi ) );
    if ( yx > yy ) { 
      int dpi = getDisplayDensityDpi();
      int dds = TDandroid.BELOW_API_24 ?  dpi : Resources.getSystem().getDisplayMetrics().DENSITY_DEVICE_STABLE;
      double log_s_dpi = Math.log( dds / 100.0 );
      int ys = 10 * (int)( 10 * Math.exp( 3.00521 + -0.90490 * log_s_dpi ) );
      CACHED_DENSITY = ys;
    } else {
      CACHED_DENSITY = yy;
    }
    // TDLog.v("ZOOM " + TDandroid.BELOW_API_24 + " dpi " + getDisplayDensityDpi()
    //                 + " " + TopoDroidApp.getDisplayDensity()
    //                 + " density " + Resources.getSystem().getDisplayMetrics().density 
    //                 + " DDS " +  ( TDandroid.BELOW_API_24 ?  getDisplayDensityDpi() : Resources.getSystem().getDisplayMetrics().DENSITY_DEVICE_STABLE )
    //                 + " xdpi " + Resources.getSystem().getDisplayMetrics().xdpi 
    //                 + " ydpi " + Resources.getSystem().getDisplayMetrics().ydpi );
    return CACHED_DENSITY;
  }

  /** set the height of the button list-view
   * @param context   context
   * @param listView  list view
   * @return button size
   */
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

  /** @return the scaled size of buttons
   * @param context   context
   * @note default button size - USED by Tdm...
   */
  static int getScaledSize( Context context )
  {
    return (int)( TDSetting.mSizeButtons * context.getResources().getSystem().getDisplayMetrics().density );
  }
  
  /** re-set the button bar of the main window
   */
  public static void resetButtonBar() 
  { 
    if ( mMainActivity == null ) return;
    mMainActivity.runOnUiThread( new Runnable() { 
      public void run () { 
        mMainActivity.resetButtonBar(); 
      }
    } );
  }

  public static void resetUiVisibility()
  {
    if ( mMainActivity == null ) return;
    mMainActivity.runOnUiThread( new Runnable() { 
      public void run () {
        mMainActivity.resetUiVisibility();
      }
    } ); 
  }

   
  /** set the screen orientation
   */
  public static void setScreenOrientation() 
  { 
    if ( mMainActivity == null ) return;
    TDandroid.setScreenOrientation( mMainActivity  );
  }

  // /** re-set the menu adapter of the main window
  //  */
  // public static void setMenuAdapter( ) 
  // { 
  //   if ( mMainActivity == null ) return;
  //   mMainActivity.runOnUiThread( new Runnable() { 
  //     public void run () { 
  //       mMainActivity.setMenuAdapter( TDInstance.getResources() ); 
  //     }
  //   } );
  // }

  // UNUSED was called by HelpEntry
  // static int getDefaultSize( Context context )
  // {
  //   return (int)( 42 * context.getResources().getSystem().getDisplayMetrics().density );
  // }

  // ------------------------------------------------------------
  // CONSTANTS
  // private static final byte char0C = 0x0c;

  // ---------------------------------------------------------------
  // ConnListener
  ArrayList< Handler > mConnListener = null;

  /** register a connection-listener in the conn-listener set
   * @param hdl   connection listener
   */
  void registerConnListener( Handler hdl )
  {
    if ( hdl != null && mConnListener != null ) {
      mConnListener.add( hdl );
      // try { new Messenger( hdl ).send( new Message() ); } catch ( RemoteException e ) { }
    }
  }

  /** unregister a connection-listener from the conn-listener set
   * @param hdl   connection listener
   */
  void unregisterConnListener( Handler hdl )
  {
    if ( hdl != null && mConnListener != null ) {
      // try { new Messenger( hdl ).send( new Message() ); } catch ( RemoteException e ) { }
      mConnListener.remove( hdl );
    }
  }

  /** send a notification to the connection listeners
   * @param w  notification (value)
   */
  private void notifyConnState( int w )
  {
    // TDLog.v( "notify conn state" );
    if ( mConnListener == null ) return;
    for ( Handler hdl : mConnListener ) {
      try {
        Message msg = Message.obtain();
        msg.what = w;
        new Messenger( hdl ).send( msg );
      } catch ( RemoteException e ) {
        TDLog.e( e.getMessage() );
      }
    }
  }
  
  // ---------------------------------------------------------------
  // survey/calib info
  //

  /** @return true if the calibration matches with the current device
   */
  boolean checkCalibrationDeviceMatch() 
  {
    CalibInfo calib_info = mDData.selectCalibInfo( TDInstance.cid  );
    // TDLog.Log( TDLog.LOG_CALIB, "calib_info.device " + ((calib_info == null)? "null" : calib_info.device) );
    // TDLog.Log( TDLog.LOG_CALIB, "device " + ((mDevice == null)? "null" : mDevice.getAddress()) );
    return ( TDInstance.getDeviceA() == null || ( calib_info != null && calib_info.device.equals( TDInstance.deviceAddress() ) ) );
  }

  /** @return the list of the survey names
   */ 
  public static List< String > getSurveyNames()
  {
    if ( mData == null ) return null;
    return mData.selectAllSurveys();
  }

  /** @return the info of the current survey
   */
  public static SurveyInfo getSurveyInfo()
  {
    if ( TDInstance.sid <= 0 ) return null;
    if ( mData == null ) return null;
    return mData.selectSurveyInfo( TDInstance.sid );
    // if ( info == null ) TDLog.e("null survey info. sid " + TDInstance.sid );
  }

  /** @return the xsection mode of the current survey: 0 shared, 1 private, -1 error
   */
  public static int getSurveyXSectionsMode() 
  {
    if ( TDInstance.sid <= 0 ) return -1;
    if ( mData == null ) return -1;
    return mData.getSurveyXSectionsMode( TDInstance.sid );
  }

  /** @return the "extend" value of the current survey (or default to NORMAL)
   */
  private static int getSurveyExtend()
  {
    if ( TDInstance.sid <= 0 ) return SurveyInfo.SURVEY_EXTEND_NORMAL;
    if ( mData == null ) return SurveyInfo.SURVEY_EXTEND_NORMAL;
    return mData.getSurveyExtend( TDInstance.sid );
  }

  /** set the "extend" value of the current survey
   * @param extend   extend value
   */
  public static void setSurveyExtend( int extend )
  {
    // TDLog.v( "set Survey Extend: " + extend );
    if ( TDInstance.sid <= 0 ) return;
    if ( mData == null ) return;
    mData.updateSurveyExtend( TDInstance.sid, extend );
  }

  /** @return the statistics of a survey
   * @param sid   survey ID
   */
  static SurveyStat getSurveyStat( long sid )
  {
    List< Device > devices = mDData.getDevices();
    return mData.getSurveyStat( sid, devices );
  }

  /** @return the info of the current calibration
   */
  public CalibInfo getCalibInfo()
  {
    if ( TDInstance.cid <= 0 ) return null;
    if ( mDData == null ) return null;
    return mDData.selectCalibInfo( TDInstance.cid );
  }

  /** @return the names of the stations of the current survey
   */
  Set<String> getStationNames() { return mData.selectAllStations( TDInstance.sid ); }

  // ----------------------------------------------------------------

  @Override 
  public void onTerminate()
  {
    super.onTerminate();
    thisApp = null;
  }

  /** set the bluetooth device nickname
   * @param device      device
   * @param nickname    device nickname
   */
  static void setDeviceName( Device device, String nickname )
  {
    if ( device != null /* && device == TDInstance.getDeviceA() */ ) {
      mDData.updateDeviceNickname( device.getAddress(), nickname );
      device.mNickname = nickname;
    }
  }

  // called by DeviceActivity::onResume()
  public void resumeComm() { if ( mComm != null ) mComm.resume(); }

  public void suspendComm() { if ( mComm != null ) mComm.suspend(); }

  public void resetComm() 
  { 
    // TDLog.v("APP reset BT");
    mDataDownloader.onStop(); // mDownload = false;
    createComm();
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
    return mComm != null && mComm.isConnected() && ! mComm.isCommThreadNull(); // FIXME BLE5 to check
  }

  public void disconnectRemoteDevice( boolean force )
  {
    // TDLog.v( "TDApp: disconnect remote device. force " + force );
    // TDLog.Log( TDLog.LOG_COMM, "TDApp disconnect RemoteDevice listers " + mListerSet.size() + " force " + force );
    if ( force || mListerSet.size() == 0 ) {
      if ( mComm != null && mComm.isConnected() ) {
        mComm.disconnectRemoteDevice( ); // FIXME BLE5 to check
      }
    }
  }

  /** delete the comm object, if not null
   * @param disconnect   if true force call disconnectRemoteDevice (? always false may be safe)
   */
  private void deleteComm( boolean disconnect ) // FIXME BLE5
  {
    // TDLog.v( "APP: delete comm - disconnect " + disconnect );
    if ( mComm != null ) {
      if ( disconnect || mComm.isConnected() ) {
        mComm.disconnectRemoteDevice(); 
      }
      mComm.terminate();
      mComm = null;
    }
  }

  // void connectRemoteDevice( String address )
  // {
  //   if ( mComm != null ) mComm.connectRemoteDevice( address, mListerSet );
  // }

  // FIXME_COMM
  // @param address   device address
  // @param data_type data type ...
  public boolean connectDevice( ListerHandler lister, String address, int data_type, int timeout ) 
  {
    // TDLog.v( "TDApp: connect address " + address + " comm is " + ((mComm==null)? "null" : "non-null") );
    // return mComm != null && mComm.connectDevice( address, mListerSet, data_type, timeout ); // FIXME_LISTER
    if ( lister == null ) lister = mListerSet;
    return mComm != null && mComm.connectDevice( address, lister, data_type, timeout ); // FIXME_LISTER
  }

  public boolean disconnectComm()
  {
    // TDLog.v( "TDApp: disconnect. comm is " + ((mComm==null)? "null" : "non-null") );
    return ( mComm == null ) || mComm.disconnectDevice();
  }

  public void notifyConnectionState( int state )
  {
    // TODO
    TDLog.e( "TDApp: notify conn state " + state + " >>>>> TODO" );
  }
  // end FIXME_COMM

  // --------------------------------------------------------------------------------------

  public DeviceA3Info readDeviceA3Info( String address )
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

  public DeviceX310Info readDeviceX310Info( String address )
  {
    // TDLog.v( "read info - address " + address );
    DeviceX310Info info = new DeviceX310Info();
    byte[] ret = readMemory( address, 0x8008 );
    if ( ret == null ) {
      TDLog.e("Failed read 8008");
      // info.setError( DeviceX310Info.ERR_8008 );
      return null;
    }
    // TDLog.Log( TDLog.LOG_COMM, "Addr 8008 (code): " + ret[0] + " " + ret[1] );
    // TDLog.v( "read 8008 (code): " + ret[0] + " " + ret[1] );
    info.mCode = String.format( getResources().getString( R.string.device_code ), MemoryOctet.toInt( ret[1], ret[0] ) );

    // ret = readMemory( address, 0xe000 );
    ret = readMemory( address, DeviceX310Details.FIRMWARE_ADDRESS );
    if ( ret == null ) {
      TDLog.e("Failed read e000" );
      // info.setError( DeviceX310Info.ERR_E000 );
      return null;
    }
    // TDLog.Log( TDLog.LOG_COMM, "Addr e000 (fw): " + ret[0] + " " + ret[1] );
    info.mFirmware = String.format( getResources().getString( R.string.device_firmware ), ret[0], ret[1] );
    // int fw0 = ret[0]; // this is always 2
    int fw1 = ret[1]; // firmware 2.X

    // ret = readMemory( address, 0xe004 );
    ret = readMemory( address, DeviceX310Details.HARDWARE_ADDRESS );
    if ( ret == null ) {
      TDLog.e("Failed read e004" );
      // info.setError( DeviceX310Info.ERR_E004 );
      return null;
    }
    // TDLog.v( "TD Addr e004 (hw): " + ret[0] + " " + ret[1] );
    info.mHardware = String.format( getResources().getString( R.string.device_hardware ), (ret[0]/10), (ret[0]%10) ); // 20221102

    // ret = readMemory( address, 0xc044 );
    // if ( ret != null ) {
    //   // TDLog.v( "X310 info C044 " + String.format( getResources().getString( R.string.device_memory ), ret[0], ret[1] ) );
    // }

    resetComm();
    return info;
  }

  /** read head-tail from a DistoX A3
   * @param address    device address
   * @param command    command byte array
   * @param head_tail  return array with positions of head and tail
   * @return head-tail string, null on failure
   */
  public String readA3HeadTail( String address, byte[] command, int[] head_tail )
  {
    if ( mComm instanceof DistoXA3Comm ) {
      DistoXA3Comm comm = (DistoXA3Comm)mComm;
      String ret = comm.readA3HeadTail( address, command, head_tail );
      resetComm();
      return ret;
    } else {
      TDLog.e("read A3 HeadTail: class cast exception");
    }
    return null;
  }

  /** swap hot bit in the range [from, to)
   * @param address   device address
   * @param from      initial memory address
   * @param to        final memory address (non-inclusive)
   * @param on_off    whether to swap hot bit ON or OFF
   * @return the number of bit that have been swapped
   */
  public int swapA3HotBit( String address, int from, int to, boolean on_off ) 
  {
    if ( mComm instanceof DistoXA3Comm ) {
      DistoXA3Comm comm = (DistoXA3Comm)mComm;
      int ret = comm.swapA3HotBit( address, from, to, on_off ); // FIXME_A3
      resetComm();
      return ret;
    } else {
      TDLog.e("swap A3 HeadTail: not A3 comm");
    }
    return -1; // failure
  }

  static boolean mEnableZip = true;  // whether zip saving is enabled or must wait (locked by th2. saving thread)
  static boolean mSketches = false;  // whether to use 3D models

  // ---------------------------------------------------------

  /** second step of application initialization
   * @note called by the MainWindow loader thread
   */
  void startupStep2( )
  {
    TDPrefHelper prefHlp = new TDPrefHelper( this );
    // ***** LOG FRAMEWORK
    TDLog.loadLogPreferences( prefHlp ); 
    // TDLog.v( "log load prefs done");

    // mData.compileStatements(); // this method is now empty (and commented)

    PtCmapActivity.setMap( prefHlp.getString( "DISTOX_PT_CMAP", null ) );
    // TDLog.v( "PCmap set map done");

    TDSetting.loadSecondaryPreferences( prefHlp );
    TDLog.v( "load secondary done");
    checkAutoPairing();

    // if ( TDLog.LOG_DEBUG ) {
    //   isTracing = true;
    //   Debug.startMethodTracing("DISTOX");
    // }

    // TDLog.Debug("ready");
  }

  /** create the bluetooth communication
   */
  private void createComm()
  {
    // TDLog.v("APP create Comm");
    deleteComm( true ); // if mComm is null this is nothing
    // if ( TDInstance.isDeviceAddress( Device.ZERO_ADDRESS ) ) { // FIXME VirtualDistoX
    //   mComm = new VirtualDistoXComm( this, mVirtualDistoX );
    // } else {
      if ( TDInstance.isDeviceX310() ) {
        // TDLog.v( "TDApp: create DistoX2 comm");
        mComm = new DistoX310Comm( this );
      } else if ( TDInstance.isDeviceA3() ) {
        // TDLog.v( "TDApp: create DistoX1 comm");
        mComm = new DistoXA3Comm( this );
      } else if ( TDInstance.isDeviceSap() ) {
        String address = TDInstance.deviceAddress();
        BluetoothDevice bt_device = TDInstance.getBleDevice();
        // TDLog.v( "TDApp: create SAP comm");
        mComm = new SapComm( this, address, bt_device );
      } else if ( TDInstance.isDeviceBric() ) {
        String address = TDInstance.deviceAddress();
        BluetoothDevice bt_device = TDInstance.getBleDevice();
        // TDLog.v( "TDApp: create BRIC comm");
        mComm = new BricComm( this, this, address, bt_device );
      // } else if ( TDInstance.isDeviceBlex() ) {
      //   address = TDInstance.deviceAddress();
      //   BluetoothDevice bt_device = TDInstance.getBleDevice();
      //   // TDLog.v( "TDApp: create ble comm. address " + address + " BT " + ((bt_device==null)? "null" : bt_device.getAddress() ) );
      //   mComm = new BleComm( this, address, bt_device );
      } else if (TDInstance.isDeviceXBLE()){ // SIWEI_TIAN changed on Jun 2022
        // TDLog.v( "TDApp: create DistoX-BLE comm");
        String address = TDInstance.deviceAddress();
        BluetoothDevice bt_device = TDInstance.getBleDevice();
        mComm = new DistoXBLEComm( this,this, address, bt_device );
      } else if (TDInstance.isDeviceCavway()){
        String address = TDInstance.deviceAddress();
        BluetoothDevice bt_device = TDInstance.getBleDevice();
        mComm = new CavwayComm( this,this, address, bt_device );
      }
    // }
  }

  /** react to a user tap on the bluetooth button
   * @param ctx       context
   * @param lister    data lister
   * @param b         bluetooth button
   * @param nr_shots  ...
   */
  void doBluetoothButton( Context ctx, ILister lister, Button b, int nr_shots )
  {
    if ( TDSetting.WITH_IMMUTABLE && ! TDInstance.isSurveyMutable ) {
      doBluetoothReset( lister );
      return;
    }

    if ( TDLevel.overAdvanced ) {
      if ( TDInstance.isDeviceBric() ) {
        // TDLog.v( "bt button over advanced : BRIC");
        if ( mComm != null && mComm.isConnected() ) { // FIXME BRIC_TESTER
          CutNPaste.showPopupBT( ctx, lister, this, b, false, (nr_shots == 0) );
          return;
        }
      } else if ( TDInstance.isDeviceSap5() ) { // SAP5 nothing
        // TDLog.v( "bt button over advanced : SAP5");
      } else if ( TDInstance.isDeviceSap6() ) { // FIXME_SAP6
        TDLog.v( "SAP6 do bt button");
        // if ( TDInstance.hasDeviceRemoteControl() ) { // test not necessary 
          CutNPaste.showPopupBT( ctx, lister, this, b, false, (nr_shots == 0) );
          return;
        // }
      } else { // DistoX
        // TDLog.v( "bt button over advanced : DistoX");
        if ( ! isDownloading() ) {
          if ( TDInstance.hasDeviceRemoteControl() && ! TDSetting.isConnectionModeMulti()) {
            CutNPaste.showPopupBT( ctx, lister, this, b, false, (nr_shots == 0) );
            return;
          }
        } else { // downloading: nothing
          return;
        }
      }
    }
    // TDLog.v( "bt button not over advanced");
    doBluetoothReset( lister );
  }

  // GM download flag - kludge for DistoXBLE calib data
  // private static boolean mGMdownload = false;

  // /** set the GM download flag
  //  * @param value   new flag value
  //  */
  // void setGMdownload( boolean value ) { mGMdownload = value; }

  /** @return true if the data downloader is downloading
   */
  public boolean isDownloading() 
  { 
    return /* mGMdownload || */ mDataDownloader.isDownloading(); 
  }

  /** stop data download
   * @param lister  data lister
   */
  public void stopDownloading( ListerHandler lister )
  {
    mDataDownloader.setDownloading( false );
    mDataDownloader.stopDownloadData( lister );
    if ( lister != null ) {
      lister.setConnectionStatus( mDataDownloader.getStatus() );
      // mDataDownloader.notifyConnectionStatus( lister, ConnectionState.CONN_DISCONNECTED );
    }
  }

  /** reset the bluetooth
   * @param lister  data lister
   */
  private void doBluetoothReset( ILister lister )
  {
    stopDownloading( new ListerHandler(lister) );
    this.resetComm();
    TDToast.make(R.string.bt_reset );
  }
 
  /** initialize the display parameters
   * @param dm     display metrics
   * // @param landscape whether the screen orientation is landscape
   */
  static private void setDisplayParams( DisplayMetrics dm /* , boolean landscape */ )
  {
    float density  = dm.density;
    float dim = dm.widthPixels;
    mBorderRight      = dim * 15 / 16;
    mBorderLeft       = dim / 16;
    mBorderInnerRight = dim * 3 / 4;
    mBorderInnerLeft  = dim / 4;
    mBorderTop        = (int)( dm.heightPixels / 8 ); // 20230118 cast to int (2 lines)
    mBorderBottom     = (int)( (dm.heightPixels * 7) / 8 ) + DrawingWindow.ZOOM_TRANSLATION_1;
    mDisplayWidth  = dm.widthPixels;
    mDisplayHeight = dm.heightPixels;
    TDLog.v("ConfigChange set display params " + mDisplayWidth + " " + mDisplayHeight + " density " + density + " dim " + dim );
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    thisApp = this;
    TDInstance.setContext( getApplicationContext() );
    TDLog.v("TDApp on create");

    // MODE_WORLD_WRITEABLE and MODE_WORLD_READABLE are no longer supported
    // SQLiteDatabase dbase = openOrCreateDatabase("DISTOX14", 0, null );
    // DBase path: /data/user/0/com.topodroid.TDX/databases/DISTOX14

    // ONLY IF BUILT WITH ANDROID-30 and above
    // if ( TDandroid.ABOVE_API_29 ) {
    //   if ( ! android.os.Environment.isExternalStorageManager() ) {
    //     // Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID );
    //     // startActivity( android.content.Intent( android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri ) );
    //     android.content.Intent intent = new android.content.Intent();
    //     intent.setAction( android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION );
    //     startActivity( intent );
    //   }
    // }

    // require large memory pre Honeycomb
    // dalvik.system.VMRuntime.getRuntime().setMinimumHeapSize( 64<<20 );

    // TDLog.Profile("TDApp onCreate");
    
    // MOVE_TO_6
    // boolean hasTopoDroidDir = TDFile.getTopoDroidFile( "/sdcard/Documents/TopoDroid" ).exists();
    // boolean hasTDXDir =  TDFile.hasExternalDir(null);
    // TDLog.v( "APP has external dir: " + hasTDXDir + " " + TDFile.hasExternalDir("TopoDroid") );
    // TDLog.v( "APP has old TopoDroid dir: " + hasTopoDroidDir );
    // if ( hasTopoDroidDir && ! hasTDXDir ) {
    //   // TDLog.v("APP move to 6");
    //   TDPath.moveTo6( this );
    // }

    TDVersion.setVersion( this );
    TDPrefHelper prefHlp = new TDPrefHelper( this );

    mWelcomeScreen = prefHlp.getBoolean( "DISTOX_WELCOME_SCREEN", true ); // default: WelcomeScreen = true
    // if ( mWelcomeScreen ) {
    //   setDefaultSocketType( );
    // }
    mSetupScreen = prefHlp.getBoolean( "DISTOX_SETUP_SCREEN", true ); // default: SetupScreen = true
    if ( mSetupScreen ) { // default button size
      TDSetting.mSizeBtns = 5;
      TDSetting.mSizeButtons =  (int)( TDSetting.BTN_SIZE_UNUSED * TopoDroidApp.getDisplayDensity() * 0.86f );
    }

    DistoXConnectionError = new String[5];
    DistoXConnectionError[0] = getResources().getString( R.string.distox_err_ok );
    DistoXConnectionError[1] = getResources().getString( R.string.distox_err_headtail );
    DistoXConnectionError[2] = getResources().getString( R.string.distox_err_headtail_io );
    DistoXConnectionError[3] = getResources().getString( R.string.distox_err_headtail_eof );
    DistoXConnectionError[4] = getResources().getString( R.string.distox_err_connected );

    DisplayMetrics dm = getResources().getDisplayMetrics();
    setDisplayParams( dm /* , getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE */ );

    mScaleFactor   = (mDisplayHeight / 320.0f) * dm.density;
    // FIXME it would be nice to have this, but it breaks all existing sketches
    //       therefore must stick with initial choice
    // DrawingUtil.CENTER_X = mDisplayWidth  / 2;
    // DrawingUtil.CENTER_Y = mDisplayHeight / 2;

    mListerSet = new ListerSetHandler();
    mEnableZip = true;

    // initEnvironmentFirst( prefHlp ); // must be called after the permissions

    // mCheckPerms = TDandroid.checkPermissions( this );
    // if ( mCheckPerms >= 0 ) {
    //   initEnvironmentSecond( ); // called by MainWindow.onStart
    // }

    // mManual = getResources().getString( R.string.topodroid_man );

    // TDLog.v( "W " + mDisplayWidth + " H " + mDisplayHeight + " D " + density );
  }

  // init env requires the device database, which requires having the permissions
  //
  static boolean done_init_env_first  = false;
  static boolean done_init_env_second = false;
  static boolean done_loaded_palette  = false;

  /** initialize the environment, first step
   *  - device DB helper, 
   *  - primary preferences, 
   *  - version, symbols, firmwares, translated user man-pages
   *  - data connection
   */
  void initEnvironmentFirst(  ) // TDPrefHelper prefHlp 
  {
    TDLog.v("TDApp init env [1] already done " + done_init_env_first );
    if ( done_init_env_first ) return;
    // TDLog.v("TDApp Init Env [1]");
    done_init_env_first = true;

    // TDLog.v( "APP init env. first " );
    TDPrefHelper prefHlp = new TDPrefHelper( this );
    mDData = new DeviceHelper( thisApp );
    // LOADING THE SETTINGS IS RATHER EXPENSIVE !!!
    TDLog.v("TDApp load primary prefs");
    TDSetting.loadPrimaryPreferences( TDInstance.getResources(),  prefHlp );

    thisApp.mDataDownloader = new DataDownloader( thisApp, thisApp );

    if ( TDandroid.PRIVATE_STORAGE ) {
      String version = mDData.getValue( "version" );
      if ( version == null || ( ! version.equals( TDVersion.string() ) ) ) {
        setSayDialogR( true );
      }
    }

    // ***** DRAWING TOOLS SYMBOLS
    // TDLog.Profile("TDApp symbols");

    // if one of the symbol dirs does not exist all of then are restored - MOVED TO SECOND STEP
    // String version = mDData.getValue( "version" );
    // // TDLog.v("PATH " + "version " + version + " " + TDVersion.string() );
    // // TDLog.v( "DData version <" + version + "> TDversion <" + TDVersion.string() + ">" );
    // if ( version == null || ( ! version.equals( TDVersion.string() ) ) ) {
    //   mDData.setValue( "version",  TDVersion.string()  );
    //   // FIXME INSTALL_SYMBOL installSymbols( false ); // this updates symbol_version in the database
    //   String symbol_version = mDData.getValue( "symbol_version" );
    //   if ( symbol_version == null ) {
    //     // TDLog.v("PATH " + "symbol version " + symbol_version );
    //     installSymbols( true );
    //   }
    //   String firmware_version = mDData.getValue( "firmware_version" );
    //   // TDLog.v("APP current firmware version " + firmware_version );
    //   if ( firmware_version == null || ( ! firmware_version.equals( TDVersion.FIRMWARE_VERSION ) ) ) {
    //     installFirmware( false ); // false = do not overwrite
    //   }
    //   // installUserManual( );
    //   mCheckManualTranslation = true;
    // }

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
    TDInstance.setDeviceA( mDData.getDevice( prefHlp.getString( TDSetting.keyDeviceName(), TDString.EMPTY ) ) );

    if ( TDInstance.getDeviceA() != null ) {
      thisApp.createComm();
    }
  }

  /** second step of environment initialization: CWD and paths
   * @return true if successful - can fail if cannot open the database
   */
  static  boolean initEnvironmentSecond( )
  {
    TDLog.v("TDApp init env [2] already done " + done_init_env_second );
    if ( done_init_env_second ) return true;
    // TDLog.v("TDApp Init Env [2]");
    done_init_env_second = true;
    // TDLog.v("APP init env. second " );

    String version = mDData.getValue( "version" );
    // TDLog.v("PATH " + "version " + version + " " + TDVersion.string() );
    // TDLog.v( "DData version <" + version + "> TDversion <" + TDVersion.string() + ">" );
    if ( version == null || ( ! version.equals( TDVersion.string() ) ) ) {
      mDData.setValue( "version",  TDVersion.string()  );
      // FIXME INSTALL_SYMBOL installSymbols( false ); // this updates symbol_version in the database
      String symbol_version = mDData.getValue( "symbol_version" );
      if ( symbol_version == null ) {
        // TDLog.v("PATH " + "symbol version " + symbol_version );
        installSymbols( true );
      }
      String firmware_version = mDData.getValue( "firmware_version" );
      TDLog.v("APP current firmware version " + firmware_version );
      if ( firmware_version == null || ( ! firmware_version.equals( TDVersion.FIRMWARE_VERSION ) ) ) {
        installFirmware( false ); // false = do not overwrite
      }
      // installUserManual( );
      mCheckManualTranslation = true;
    }

    TDPrefHelper prefHlp = new TDPrefHelper( thisApp );

    // TDLog.Profile("TDApp cwd");
    TDInstance.cwd = prefHlp.getString( "DISTOX_CWD", "TopoDroid" );
    TDInstance.cbd = TDPath.getCurrentBaseDir();

    // TDLog.Profile("TDApp paths");
    boolean created = TDPath.setTdPaths( TDInstance.cwd /*, TDInstance.cbd */ );
    TDLog.v("TD app: env init-2 cwd created " + created );
    return true;
  }

  static boolean initEnvironmentThird()
  {
    TDLog.v("TDApp init env [3] loaded palette " + done_loaded_palette );
    // TDLog.Profile("TDApp DB"); 
    // ***** DATABASE MUST COME BEFORE PREFERENCES
    // if ( ! with_dialog_r ) {
      // TDLog.v( "Open TopoDroid Database");
      mData = new DataHelper( thisApp ); 
    // }
    if ( ! done_loaded_palette ) {
      if ( mData.hasDB() ) {
        Thread loader = new Thread() {
          @Override
          public void run() {
            TDLog.v("TDApp loading palette");
            Resources res = TDInstance.getResources();
            // BrushManager.reloadPointLibrary( TDInstance.context, res ); // reload symbols
            // BrushManager.reloadLineLibrary( res );
            // BrushManager.reloadAreaLibrary( res );
            BrushManager.setHasSymbolLibraries( false );
            BrushManager.loadAllSymbolLibraries( TDInstance.context, res );
            BrushManager.doMakePaths( );
            MainWindow.enablePaletteButton();
            done_loaded_palette = true;
            BrushManager.setHasSymbolLibraries( true );
          }
        };
        loader.setPriority( Thread.MIN_PRIORITY );
        loader.start();
      } else {
        TDLog.e("TDApp init env [3] database not opened");
      }
    } else {
      BrushManager.setHasSymbolLibraries( false );
      BrushManager.initAllSymbolIndices();
      BrushManager.setHasSymbolLibraries( true );
    }
    // mStationName = new StationName();
    return mData.hasDB();
  }


  /** attach base context
   * @param ctx   context
   */
  @Override
  protected void attachBaseContext( Context ctx )
  {
    // TDLog.v("APP attach base context");
    TDInstance.context = ctx;
    TDLocale.resetTheLocale();
    super.attachBaseContext( TDInstance.context );
  }

  private MyDialog mCurrentDialog = null;

  public void popDialog() { mCurrentDialog = null; }

  public void pushDialog( MyDialog dialog ) { mCurrentDialog = dialog; }

  /** react to a change of configuration
   * @param cfg  new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration cfg )
  {
    super.onConfigurationChanged( cfg );
    boolean landscape = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE;
    // TDLog.v("APP config change - landscape " + landscape );
    TDLocale.resetTheLocale( );
    setDisplayParams( getResources().getDisplayMetrics() /* , landscape */ );
    if ( mCurrentDialog != null ) mCurrentDialog.doInit( landscape );
  }

  /** set the current work directory
   * @param cwd current work directory
   // * @param cbd current base directory (UNUSED)
   */
  public static void setCWD( String cwd /* , String cbd */ )
  {
    if ( TDString.isNullOrEmpty( cwd ) ) cwd = TDInstance.cwd;
    TDLog.v( "TDApp set CWD " + cwd /* + " CBD " + cbd */ );

    if ( cwd.equals( TDInstance.cwd ) ) return;
    // TDInstance.cbd = cbd;
    TDInstance.cwd = cwd;
    // TDLog.Log( TDLog.LOG_PATH, "TDApp set cwd <" + cwd + /* "> cbd <" + cbd + */ ">");
    mData.closeDatabase();

    boolean created_cwd = TDPath.setTdPaths( TDInstance.cwd /*, TDInstance.cbd */ );
    boolean created_db  = mData.openSurveyDatabase( TDInstance.context );
    TDLog.v("TD app: cwd created " + created_cwd + " db created " + created_db );
    if ( created_db ) done_loaded_palette = false; // 20241018 force reloading palette

    if ( mMainActivity != null ) mMainActivity.setTheTitle( );
  }

// -----------------------------------------------------------------

  /** upload the calibration coefficients to the DistoX
   * @param coeff    calibration coefficients
   * @param check    whether to check that the calibration matches the device 
   * @param b        "write" button - to update its state at the end
   * @note called by GMActivity and by CalibCoeffDialog and DeviceActivity (to reset coeffs)
   */
  public void uploadCalibCoeff( byte[] coeff, boolean check, Button b ) // 20250123 dropped second
  {
    // TODO this writeCoeff should be run in an AsyncTask
    if ( b != null ) b.setEnabled( false );
    if ( mComm == null || TDInstance.getDeviceA() == null ) {
      TDToast.makeBad( R.string.no_device_address );
    } else if ( check && ! checkCalibrationDeviceMatch() ) {
      TDToast.makeBad( R.string.calib_device_mismatch );
    } else if ( ! mComm.writeCoeff( TDInstance.deviceAddress(), coeff ) ) {
      TDToast.makeBad( R.string.write_failed );
    } else {
      // write OK: read coeff back and check they were written correctly
      int len = coeff.length;
      byte[] coeff2 = new byte[ len ];
      if ( ! mComm.readCoeff( TDInstance.deviceAddress(), coeff2 ) ) {
        TDToast.makeBad( R.string.read_failed );
      } else {
        boolean success = true;
        for ( int k=0; k<len; ++k ) {
          if ( coeff2[k] != coeff[k] ) {
            success = false;
            break;
          }
        }
        if ( success ) { 
          TDToast.make( R.string.write_ok );
        } else {
          TDToast.makeBad( R.string.write_failed );
        }
      } 
    }
    if ( b != null ) b.setEnabled( true );
    resetComm();
  }

  /** read the calibration coefficients from the DistoX
   * @param coeff    calibration coefficients [filled in output]
   * @return true if successful
   * @note called by CalibReadTask.onPostExecute
   */
  public boolean readCalibCoeff( byte[] coeff ) // 20250123 droped second
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) {
      TDLog.e("No comm or no device");
      return false;
    }
    boolean ret = mComm.readCoeff( TDInstance.deviceAddress(), coeff );
    resetComm();
    return ret;
  }

  /** toggle the DistoX calibration/normal mode
   * @return true if successful
   * @note called by CalibToggleTask.doInBackground
   */
  public boolean toggleCalibMode( )
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) return false;
    boolean ret = mComm.toggleCalibMode( TDInstance.deviceAddress(), TDInstance.deviceType() );
    resetComm();
    return ret;
  }

  /** read the DistoX memory
   * @param address   device address
   * @param addr      memory address
   * @return array of bytes read from the DistoX memory at the given address
   */
  public byte[] readMemory( String address, int addr )
  {
    if ( mComm == null || isCommConnected() ) return null;
    byte[] ret = mComm.readMemory( address, addr );
    resetComm();
    return ret;
  }

  /** read the DistoX2 (X310) memory
   * @param address   device address
   * @param h0        from index
   * @param h1        to index
   * @param memory    array of octets to be filled by the memory-read
   * @param dialog    feedback receiver
   * @return number of octets that have been read (-1 on error)
   */
  public int readX310Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory, IMemoryDialog dialog )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    if ( mComm instanceof DistoX310Comm ) {
      DistoX310Comm comm = (DistoX310Comm)mComm;
      int ret = comm.readX310Memory( address, h0, h1, memory, dialog );
      resetComm();
      return ret;
    } else {
      TDLog.e("read X310 memory: not X310 comm");
    }
    return -1;
  }

  /** read the DistoX-BLE memory
   * @param address   device address
   * @param h0        from index
   * @param h1        to index
   * @param memory    array of octets to be filled by the memory-read
   * @param dialog    feedback receiver
   * @return number of octets that have been read (-1 on error)
   */
  public int readXBLEMemory( String address, int h0, int h1, ArrayList< MemoryOctet > memory, IMemoryDialog dialog )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    if ( mComm instanceof DistoXBLEComm ) {
      DistoXBLEComm comm = (DistoXBLEComm)mComm;
      int ret = comm.readXBLEMemory( address, h0, h1, memory, dialog );
      resetComm();
      return ret;
    } else {
      TDLog.e("read XBLE memory: not XBLE comm");
    }
    return -1;
  }

  public int readCavwayX1Memory( String address, int number, ArrayList< CavwayData > memory, CavwayMemoryDialog dialog )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    if ( mComm instanceof CavwayComm) {
      CavwayComm comm = (CavwayComm)mComm;
      int ret = comm.readCavwayMemory( address, number, memory, dialog );
      resetComm();
      return ret;
    } else {
      TDLog.e("read Cavway memory: not Cavway comm");
    }
    return -1;
  }

  /** read the DistoX (A3) memory
   * @param address   device address
   * @param h0        ?
   * @param h1        ?
   * @param memory    array of octets to be filled by the memory-read
   * @param dialog    feedback receiver
   * @return number of octets that have been read (-1 on error)
   */
  public int readA3Memory( String address, int h0, int h1, ArrayList< MemoryOctet > memory, IMemoryDialog dialog )
  {
    if ( mComm == null || isCommConnected() ) return -1;
    if ( mComm instanceof DistoXA3Comm ) {
      DistoXA3Comm comm = (DistoXA3Comm)mComm;
      int ret = comm.readA3Memory( address, h0, h1, memory, dialog );
      resetComm();
      return ret;
    } else {
      TDLog.e("read A3 memory: not A3 comm");
    }
    return -1;
  }

  // ------------------------------------------------------------------
  // FILE NAMES

  /** write the manifest file for the current survey
   */
  public void writeManifestFile()
  {
    SurveyInfo survey_info = mData.selectSurveyInfo( TDInstance.sid );
    try {
      String filename = TDPath.getManifestFile();
      // TDPath.checkPath( filename );
      FileWriter fw = TDFile.getFileWriter( filename );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s %d\n",  TDVersion.string(), TDVersion.code() );
      pw.format( "%s\n", TDVersion.DB_VERSION );
      pw.format( "%s\n", survey_info.name );
      pw.format("%s\n", TDUtil.currentDate() );
      fw.flush();
      fw.close();
    } catch ( FileNotFoundException e ) {
      TDLog.e("manifest write failure: no file");
    } catch ( IOException e ) {
      TDLog.e("manifest write failure: " + e.getMessage() );
    }
  }

  // ----------------------------------------------------------
  // SURVEY AND CALIBRATION

  /** rename the current survey
   * @param sid   survey ID
   * @param name  new survey name
   * @return true if success
   */
  boolean renameCurrentSurvey( long sid, String name )
  {
    if ( TDString.isNullOrEmpty( name ) ) return false;
    if ( name.equals( TDInstance.survey ) ) return true;
    if ( mData == null ) return false;
    if ( mData.renameSurvey( sid, name ) ) {  
      { // rename plot/sketch files: th3
        List< PlotInfo > plots = mData.selectAllPlots( sid );
        for ( PlotInfo p : plots ) {
          // Tdr
          String scrap_name = name + "-" + p.name;
          DrawingIO.changeTdrFile( TDPath.getSurveyPlotTdrFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTdrFile( name, p.name ), scrap_name );
          // TDFile.renameFile( TDPath.getSurveyPlotTdrFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTdrFile( name, p.name ) );

          // Tdr backups
          DrawingIO.changeTdrFile( TDPath.getSurveyPlotTdrBackupFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTdrBackupFile( name, p.name ), scrap_name );
          // TDFile.renameFile( TDPath.getSurveyPlotTdrBackupFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTdrBackupFile( name, p.name ) );
          for ( int bck_nr = 0; bck_nr < 5; ++ bck_nr ) {
            DrawingIO.changeTdrFile( TDPath.getSurveyPlotTdrBackupFile( TDInstance.survey, p.name, bck_nr ), TDPath.getSurveyPlotTdrBackupFile( name, p.name, bck_nr ), scrap_name );
            // TDFile.renameFile( TDPath.getSurveyPlotTdrBackupFile( TDInstance.survey, p.name, bck_nr ), TDPath.getSurveyPlotTdrBackupFile( name, p.name, bck_nr ) );
          }

          // rename exported plots: th2 dxf png svg csx
          // TDFile.renameFile( TDPath.getSurveyPlotTh2File( TDInstance.survey, p.name ), TDPath.getSurveyPlotTh2File( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotDxfFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotDxfFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotShzFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotShzFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotSvgFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotSvgFile( name, p.name ) );
          // // TDFile.renameFile( TDPath.getSurveyPlotHtmFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotHtmFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotTnlFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotTnlFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotPngFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotPngFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotCsxFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotCsxFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotXviFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotXviFile( name, p.name ) );
          // TDFile.renameFile( TDPath.getSurveyPlotC3dFile( TDInstance.survey, p.name ), TDPath.getSurveyPlotC3dFile( name, p.name ) );
        }
      }
      /* FIXME_SKETCH_3D *
      { // rename sketch files: th3
        List< Sketch3dInfo > sketches = mData.selectAllSketches( sid );
        for ( Sketch3dInfo s : sketches ) {
          TDFile.renameFile( TDPath.getSurveySketchOutFile( TDInstance.survey, s.name ), TDPath.getSurveySketchOutFile( name, s.name ) );
        }
      }
       * FIXME_SKETCH_3D */
      // rename exported files: csv csx dat dxf kml plt srv svx th top tro 
        // TDFile.renameFile( TDPath.getSurveyThFile( TDInstance.survey ), TDPath.getSurveyThFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyCsvFile( TDInstance.survey ), TDPath.getSurveyCsvFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyCsxFile( TDInstance.survey ), TDPath.getSurveyCsxFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyCavFile( TDInstance.survey ), TDPath.getSurveyCavFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyCaveFile( TDInstance.survey ), TDPath.getSurveyCaveFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyDatFile( TDInstance.survey ), TDPath.getSurveyDatFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyDxfFile( TDInstance.survey ), TDPath.getSurveyDxfFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyGrtFile( TDInstance.survey ), TDPath.getSurveyGrtFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyGtxFile( TDInstance.survey ), TDPath.getSurveyGtxFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyKmlFile( TDInstance.survey ), TDPath.getSurveyKmlFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyJsonFile( TDInstance.survey ), TDPath.getSurveyJsonFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyPltFile( TDInstance.survey ), TDPath.getSurveyPltFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyShzFile( TDInstance.survey ), TDPath.getSurveyShzFile( name ) );
        // TDFile.renameFile( TDPath.getSurveySrvFile( TDInstance.survey ), TDPath.getSurveySrvFile( name ) );
        // TDFile.renameFile( TDPath.getSurveySurFile( TDInstance.survey ), TDPath.getSurveySurFile( name ) );
        // TDFile.renameFile( TDPath.getSurveySvxFile( TDInstance.survey ), TDPath.getSurveySvxFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyTopFile( TDInstance.survey ), TDPath.getSurveyTopFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyTrbFile( TDInstance.survey ), TDPath.getSurveyTrbFile( name ) );
        // TDFile.renameFile( TDPath.getSurveyTroFile( TDInstance.survey ), TDPath.getSurveyTroFile( name ) );

      // rename note file: note
      TDFile.renameFile( TDPath.getSurveyNoteFile( TDInstance.survey ), TDPath.getSurveyNoteFile( name ) );
      // rename photo folder: photo
      // TDFile.renameFile( TDPath.getSurveyPhotoDir( TDInstance.survey ), TDPath.getSurveyPhotoDir( name ) );
      // rename audio folder: audio
      // TDFile.renameFile( TDPath.getSurveyAudioDir( TDInstance.survey ), TDPath.getSurveyAudioDir( name ) );

      // rename survey folder
      TDFile.renameFile( TDPath.getSurveyDir( TDInstance.survey ), TDPath.getSurveyDir( name ) );

      TDInstance.survey = name;
      return true;
    }
    return false;
  }

  /** add a prefix to the station names of a survey
   * @param sid     survey ID
   * @param prefix  prefix
   */
  void prefixSurveyStations( long sid, String prefix )
  {
    if ( mData == null ) return;
    mData.prefixSurveyStations( sid, prefix );
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
   * @param name      survey name - if name is null, survey refs in TDInstance are cleared 
   * @param datamode  survey datamode
   * @param update    whether to call a display update
   * @param all_info  whether to set all data instead of only info in TDInstance
   * @return survey ID
   */
  public long setSurveyFromName( String name, int datamode, boolean update, boolean all_info )
  { 
    TDInstance.sid      = -1;       // no survey by default
    TDInstance.survey   = null;
    TDInstance.datamode = 0;
    // TDInstance.extend   = SurveyInfo.SURVEY_EXTEND_NORMAL;
    StationName.clearCurrentStation();
    ManualCalibration.reset();

    if ( name != null && mData != null ) {
      TDLog.v( "set Survey From Name <" + name + ">");

      TDInstance.sid = mData.setSurvey( name, datamode );
      if ( TDSetting.WITH_IMMUTABLE ) {
        TDInstance.isSurveyMutable = mData.isSurveyMutable( TDInstance.sid );
      } else {
        TDInstance.isSurveyMutable = true;
      }

      // mFixed.clear();
      TDInstance.survey = null;
      if ( TDInstance.sid > 0 ) {
        TDPath.setSurveyPaths( name );
        TDInstance.survey = name;
	TDInstance.datamode = mData.getSurveyDataMode( TDInstance.sid );
	// TDLog.v( "set survey from name: <" + name + "> datamode " + datamode + " " + TDInstance.datamode );
        if ( all_info ) {
          DistoXStationName.setInitialStation( mData.getSurveyInitStation( TDInstance.sid ) );
          TDInstance.secondLastShotId = lastShotId();
          // restoreFixed();
	  if ( update ) updateWindows();
          TDInstance.xsections = ( SurveyInfo.XSECTION_SHARED == mData.getSurveyXSectionsMode( TDInstance.sid ) );

          // TDInstance.extend = 
          int extend = mData.getSurveyExtend( TDInstance.sid );
          // TDLog.v( "set SurveyFromName extend: " + extend );
          if ( SurveyInfo.isSurveyExtendLeft( extend ) ) { 
            TDAzimuth.mFixedExtend = -1L;
          } else if ( SurveyInfo.isSurveyExtendRight( extend ) ) { 
            TDAzimuth.mFixedExtend = 1L;
          } else {
            TDAzimuth.mFixedExtend = 0;
            TDAzimuth.mRefAzimuth  = extend;
          }
        }
      }
      return TDInstance.sid;
    }
    return 0;
  }

  /** move survey data to a new survey
   * @param old_sid    old survey ID
   * @param old_id     old ID of start data to move to new survey
   * @param new_survey new survey name
   * @return true if success
   */
  boolean moveSurveyData( long old_sid, long old_id, String new_survey )
  {
    if ( mData == null ) return false;
    long new_sid = mData.getSurveyId( new_survey );
    TDLog.v( "MOVE data: " + old_sid + "/" + old_id + " to " + new_sid );
    if ( new_sid <= 0 || new_sid == old_sid ) return false;
    return mData.moveShotsBetweenSurveys( old_sid, old_id, new_sid );
  }

  /** check if there exists a survey
   * @param name   survey name
   * @return true if the survey exists
   */
  boolean hasSurveyName( String name )
  {
    return ( mData != null ) && mData.hasSurveyName( name );
  }

  /** check if there exists a survey plot (for the current survey)
   * @param name   plot name
   * @return true if the plot exists
   */
  boolean hasSurveyPlotName( String name )
  {
    return ( mData != null ) && mData.hasSurveyPlotName( TDInstance.sid, name );
  }


  /** check if there exists a calibration
   * @param name   calibration name
   * @return true if the calibration exists
   */
  boolean hasCalibName( String name )
  {
    return ( mDData != null ) && mDData.hasCalibName( name );
  }

  /** set the active calibration
   * @param calib    calibration name, or null when creating a new calibration
   */
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

  // private void setDefaultSocketType( )
  // {
  //   // String defaultSockType = ( android.os.Build.MANUFACTURER.equals("samsung") ) ? "1" : "0";
  //   // TDPrefHelper.update( "DISTOX_SOCKET_TYPE", defaultSockType ); 
  //   TDPrefHelper.update( "DISTOX_SOCKET_TYPE", 1 ); // UNSECURE
  // }

  /** set the CWD preference
   * @param cwd   CWD (Current Work Directory)
   */
  static void setCWDPreference( String cwd /*, String cbd */ )
  { 
    if ( TDInstance.cwd.equals( cwd ) /* && TDInstance.cbd.equals( cbd ) */ ) return;
    // TDLog.v( "set CWD preference " + cwd );
    TDPrefHelper.update( "DISTOX_CWD", cwd /* , "DISTOX_CBD", cbd */ ); 
    setCWD( cwd /*, cbd */ ); 
  }

  /** set the PT map preference
   * @param cmap  PT map - PocketTopo color map
   */
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

  /** set the text size
   * @param ts    text size
   */
  static void setTextSize( int ts )
  {
    TDSetting.setTextSize( ts );
    if ( TDSetting.setLabelSize( ts*3, false ) || TDSetting.setStationSize( ts*2, false ) ) { // false: do not update brush
      BrushManager.setTextSizes( );
    }
    TDPrefHelper.update( "DISTOX_TEXT_SIZE", Integer.toString(ts), "DISTOX_LABEL_SIZE", Float.toString(ts*3), "DISTOX_STATION_SIZE", Float.toString(ts*2) );
  }

  /** set the buttons size
   * @param bs    buttons size
   */
  static void setButtonSize( int bs )
  {
    // TDLog.v("set button size " + bs );
    TDSetting.setSizeButtons( bs );
    TDPrefHelper.update( "DISTOX_SIZE_BUTTONS", Integer.toString(bs) );
  }

  /** set the drawing icons units
   * @param u    drawing icons units
   */
  static void setDrawingUnitIcons( float u )
  {
    TDSetting.setDrawingUnitIcons( u );
    TDPrefHelper.update( "DISTOX_DRAWING_UNIT", Float.toString(u) );
  }

  /** set the drawing lines units
   * @param u    drawing lines units
   */
  static void setDrawingUnitLines( float u )
  {
    TDSetting.setDrawingUnitLines( u );
    TDPrefHelper.update( "DISTOX_LINE_UNITS", Float.toString(u) );
  }

  /** set a boolean preference
   * @param preference  preference name
   * @param val         preference value
   * @note used for "DISTOX_WELCOME_SCREEN" and "DISTOX_TD_SYMBOL"
   */
  static void setBooleanPreference( String preference, boolean val ) { TDPrefHelper.update( preference, val ); }

  // FIXME_DEVICE_STATIC
  /** set the primary device
   * @param address   primary device address
   * @param model     device model
   * @param name      device name
   * @param bt_device bluetooth device of the device
   */
  void setDevicePrimary( String address, String model, String name, BluetoothDevice bt_device )
  { 
    deleteComm( false );
    if ( address == null ) { // null, ..., ...
      TDInstance.setDeviceA( null );
      address = TDString.EMPTY;
    } else {
      if ( model != null ) { // addr, model, ...
        // TDInstance.setDeviceA( new Device( address, model, 0, 0, name, null ) );
        // if ( Device.isBle( TDInstance.deviceType() ) ) TDInstance.initBleDevice();
        Device device = mDData.getDevice( address );
        if ( device == null ) device = new Device( address, model, 0, 0, name, null );
        TDInstance.setDeviceA( device );
        if ( device.isBLE() ) TDInstance.initBleDevice();
      } else if ( bt_device != null ) { // addr, null, dev
        model = Device.typeToString( TDInstance.deviceType() );
        // address, model, head, tail, name, nickname
        TDInstance.setDeviceA( new Device( address, model, 0, 0, name, null ) );
        TDInstance.setBleDevice( bt_device );
        if ( TDInstance.isDeviceSap() ) {
          mComm = new SapComm( this, address, bt_device ); 
        } else if ( TDInstance.isDeviceBric() ) {
          mComm = new BricComm( this, this, address, bt_device );
        }
      } else { // addr, null, null
        // boolean create = Device.isBle( TDInstance.deviceType() );
        TDInstance.setDeviceA( mDData.getDevice( address ) );
        // TDInstance.setBleDevice( null );
        // if ( create ) createComm();
        if ( Device.isBle( TDInstance.deviceType() ) ) TDInstance.initBleDevice();
      }
      createComm();
    }
    TDPrefHelper.update( TDSetting.keyDeviceName(), address ); 
    if ( mMainActivity != null ) mMainActivity.setButtonDevice();
  }

  // TODO BLE for the second DistoX
  /** set the address of the alternate device
   * @param address   alternate device address
   */
  void setDeviceB( String address )
  {
    if ( address == null ) {
      // if ( mVirtualDistoX != null ) mVirtualDistoX.stopServer( this ); // FIXME VirtualDistoX
      TDInstance.setDeviceB( null );
      // address = TDString.EMPTY;
    } else {
      TDInstance.setDeviceB( mDData.getDevice( address ) );
    }
  }

  /** switch to the alternate device
   * @return true if success
   */
  boolean switchSecondDevice()
  {
    return TDInstance.switchDevice();
  }

  // -------------------------------------------------------------
  // DATA BATCH DOWNLOAD

  /** 
   * @param lister    data lister
   * @param data_type packet datatype
   * @return number of packet received
   *
   * NOTE called only by DataDownloadTask
   */
  int downloadDataBatch( ListerHandler lister, int data_type ) // FIXME_LISTER
  {
    // TDLog.v( "APP: batch download, data type " + data_type );
    int ret = 0;
    if ( mComm == null || TDInstance.getDeviceA() == null ) {
      TDLog.e( "Comm or Device null ");
    } else {
      if ( data_type != DataType.DATA_CALIB ) { // 20250118 moved inside the block
        TDInstance.secondLastShotId = lastShotId();
      }
      // TDLog.v( "APP: batch download, device " + TDInstance.deviceAddress() + " " + TDInstance.getDeviceA().getAddress() + " comm " + mComm.toString() );
      int timeout = 10; // 10 seconds
      ret = mComm.downloadData( TDInstance.getDeviceA().getAddress(), lister, data_type, timeout );
      // FIXME BATCH
      // if ( ret > 0 && TDSetting.mSurveyStations > 0 ) {
      //   // FIXME TODO select only shots after the last leg shots
      //   List< DBlock > list = mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
      //   assign Stations( list );
      // }
    }
    return ret;
  }

  // =======================================================
  // StationName mStationName;

  // void resetCurrentStationName( String name ) { StationName.resetCurrentStationName( name ); }

  /** set the name of the "current station" or unset it
   * @param name  "current station" name
   * @return true if the "current station" is set
   * @note if the given name equals the "current station" this is unset
   */
  boolean setCurrentStationName( String name ) { return StationName.setCurrentStationName( name ); }

  /** @return the "current station" name or null (if unset)
   */
  String getCurrentStationName() { return StationName.getCurrentStationName(); }

  /** @return true if the given name is the "current station"
   * @param name   name
   */
  boolean isCurrentStationName( String name ) { return StationName.isCurrentStationName( name ); }

  // void clearCurrentStation() { StationName.clearCurrentStation(); }

  /** @return the "current station" or the "last station" (if unset) 
   */
  String getCurrentOrLastStation( ) { return StationName.getCurrentOrLastStation( mData, TDInstance.sid); }

  /** @return the "first station" name
   */
  String getFirstStation( ) { return StationName.getFirstStation( mData, TDInstance.sid); }

  /** set the "current station" to the "last station" if the current station is unset
   */
  private void resetCurrentOrLastStation( )
  { 
    // TDLog.v("APP DATA reset current station");
    StationName.resetCurrentOrLastStation( mData, TDInstance.sid);
    // if ( mDrawingWindow != null ) mDrawingWindow.clearCurrentStation(); // not necessary
  }

  /** @return the origin station of the first plot
   */
  String getFirstPlotOrigin() { return ( TDInstance.sid < 0 )? null : mData.getFirstPlotOrigin( TDInstance.sid ); }


  // static long trobotmillis = 0L; // TROBOT_MILLIS

  /** called also by ShotWindow::updateBlockList
   * this re-assign stations to shots with station(s) already set
   * the list of stations is ordered by compare
   *
   * @param blk0 block after which to assign stations
   * @param list list of shot to assign
   * @return ???
   */
  boolean assignStationsAfter( DBlock blk0, List< DBlock > list )
  { 
    Set<String> sts = mData.selectAllStationsBefore( blk0.mId, TDInstance.sid /*, TDStatus.NORMAL */ );
    // TDLog.v("DATA " + "assign stations after " + blk0.mId + " " + blk0.Name() + " size " + list.size() + " stations " + sts.size() );
    // if ( TDSetting.mSurveyStations < 0 ) return;
    // TDLog.v("APP DATA clear current station");
    StationName.clearCurrentStation();
    // if ( mDrawingWindow != null ) mDrawingWindow.clearCurrentStation(); // not necessary
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

  /** called also by ShotWindow::updateBlockList
   * @param list blocks whose stations need to be set in the DB
   * @return true if a leg was assigned
   */
  boolean assignStationsAll(  List< DBlock > list )
  { 
    Set<String> sts = mData.selectAllStations( TDInstance.sid );
    int sz = list.size();
    if ( sz == 0 ) return false;
    // TDLog.v("DATA " + "assign stations all: size " + sz + " blk[0] id " + list.get(0).mId );

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

  /** 
   * @param context   context
   * @param uri       export URI or null (to export to private folder)
   * @param origin    sketch origin
   * @param psd1      plot data
   * @param psd2      profile data
   * @param toast     whether to toast to result
   */
  static void exportSurveyAsCsxAsync( Context context, Uri uri, String origin, PlotSaveData psd1, PlotSaveData psd2, boolean toast )
  {
    SurveyInfo survey_info = getSurveyInfo();
    if ( survey_info == null ) {
      TDLog.e("Error: null survey info");
      // TDLog.v( "null survey info");
      return;
    }
    String fullname = ( psd1 == null )? TDInstance.survey
                                      : TDInstance.survey + "-" + psd1.name;
    // String filename = TDPath.getSurveyCsxFile( fullname );
    // TDLog.Log( TDLog.LOG_IO, "exporting as CSX " + fullname + " " + filename );
    // TDLog.Log( TDLog.LOG_IO, "exporting as CSX " + fullname );
    (new SaveFullFileTask( context, uri, TDInstance.sid, mData, survey_info, psd1, psd2, origin, /* filename, */ fullname, 
       /* TDPath.getCsxFile(""), */ toast )).execute();
  }

  // FIXME_SYNC might be a problem with big surveys
  // this is called sync to pass the therion file to the 3D viewer
  // static boolean exportSurveyAsThSync( )
  // {
  //   SurveyInfo survey_info = getSurveyInfo();
  //   if ( survey_info == null ) return false;
  //   // if ( async ) {
  //   //   String saving = context.getResources().getString(R.string.saving_);
  //   //   (new SaveDataFileTask( saving, TDInstance.sid, survey_info, mData, TDInstance.survey, null, TDConst.SURVEY_FORMAT_TH, toast )).execute();
  //   //   return true;
  //   // }
  //   return ( TDExporter.exportSurveyAsTh( TDInstance.sid, mData, survey_info, TDPath.getSurveyThFile( TDInstance.survey ) ) != null );
  // }

  // FIXME_SYNC ok because calib files are small
  String exportCalibAsCsv( )
  {
    if ( TDInstance.cid < 0 ) return null;
    CalibInfo ci = mDData.selectCalibInfo( TDInstance.cid );
    if ( ci == null ) return null;
    // TDPath.checkCCsvDir();
    // String filename = TDPath.getCCsvFile( ci.name );
    // return TDExporter.exportCalibAsCsv( TDInstance.cid, mDData, ci, filename );
    // File file = TDPath.getCcsvFile( ci.name + ".csv" );
    return CalibExport.exportCalibAsCsv( TDInstance.cid, mDData, ci, ci.name );
  }

  // ----------------------------------------------
  // FIRMWARE 

  /** install the firmware files
   * @param overwrite whether to overwrite existing files
   */
  static private void installFirmware( boolean overwrite )
  {
    TDLog.v("APP FW install firmware. overwrite: " + overwrite );
    InputStream is = TDInstance.getResources().openRawResource( R.raw.firmware );
    firmwareUncompress( is, overwrite );
    try { is.close(); } catch ( IOException e ) {
      TDLog.e( e.getMessage() );
    }
    mDData.setValue( "firmware_version", TDVersion.FIRMWARE_VERSION );
  }
 
  // -------------------------------------------------------------
  // SYMBOLS

  /** install default (speleo) symbols
   * @param overwrite whether to overwrite existing files
   */
  static void installSymbols( boolean overwrite )
  {
    deleteObsoleteSymbols();
    // TDLog.v("PATH " + "install symbol version " + TDVersion.SYMBOL_VERSION );
    installSymbols( R.raw.symbols_speleo, overwrite );
    mDData.setValue( "symbol_version", TDVersion.SYMBOL_VERSION );
  }

  /** install a symbol set
   * @param res       symbol set resource 
   * @param overwrite whether to overwrite existing files
   */
  static void installSymbols( int res, boolean overwrite )
  {
    InputStream is = TDInstance.getResources().openRawResource( res );
    symbolsUncompress( is, overwrite );
  }

  /** delete the files of obsolete symbols 
   */
  static private void deleteObsoleteSymbols()
  {
    String[] lines = { 
      SymbolLibrary.BLOCKS,
      SymbolLibrary.DEBRIS,
      SymbolLibrary.CLAY,
      SymbolLibrary.CONTOUR,
      SymbolLibrary.WALL_PRESUMED,
      SymbolLibrary.SAND,
      SymbolLibrary.ICE,
      SymbolLibrary.SECTION,
      "wall:sand"
    };
    for ( String line : lines ) {
      TDPath.deleteLineFile( line );
    }
    String[] points = {
      "breakdown-choke",
      "low-end",
      "paleo-flow",
      SymbolLibrary.SECTION
    };
    for ( String point : points ) {
      TDPath.deletePointFile( point );
    }
  }

  /** reload all symbols
   * @param clear   whether to first clear symbols
   * @param speleo  whether to load symbols "speleo"
   * @param extra   whether to load symbols "extra"
   * @param mine    whether to load symbols "mine"
   * @param geo     whether to load symbols "geo"
   * @param archeo  whether to load symbols "archeo"
   * @param anthro  whether to load symbols "anthro"
   * @param paleo   whether to load symbols "paleo"
   * @param bio     whether to load symbols "bio"
   * @param karst   whether to load symbols "karst"
   */
  void reloadSymbols( boolean clear, 
                      boolean speleo, boolean extra, boolean mine, boolean geo, boolean archeo, boolean anthro, boolean paleo,
                      boolean bio,    boolean karst )
  {
    // TDLog.v( "Reload symbols " + speleo + " " + mine + " " + geo + " " + archeo + " " + paleo + " " + bio + " clear " + clear );
    // if ( extra ) speleo = true; // extra implies speleo

    if ( clear ) {
      if (speleo || extra || mine || geo || archeo || anthro || paleo || bio || karst ) { 
        TDPath.clearSymbols();
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
    BrushManager.setHasSymbolLibraries( false );
    BrushManager.loadAllSymbolLibraries( this, getResources() );
    // BrushManager.doMakePaths( ); // TODO FIXME needed ?
    BrushManager.setHasSymbolLibraries( true );
    DrawingSurface.clearManagersCache();
  }

  /** decompress the symbols files
   * @param fis       input zip-file stream
   * @param overwrite whether to overwrite existing files
   * 
   * @note the output symbols files are in the private "point", "line", and "area" folders, 
   *       their names are taken from the zip entries
   */
  static private void symbolsUncompress( InputStream fis, boolean overwrite )
  {
    // TDLog.v("APP uncompressing symbols - overwrite " + overwrite );
    // TDPath.symbolsCheckDirs();
    try {
      // byte buffer[] = new byte[36768];
      byte[] buffer = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {  // NOTE getNextEntry() throws ZipException if zip file entry name contains '..' or starts with '/'
        String filepath = ze.getName();
        if ( filepath.endsWith("README") ) continue;
        if ( ! ze.isDirectory() ) {
          int pos = 0;
          if ( filepath.startsWith( "symbol" ) ) {
            pos  = filepath.indexOf('/');
            filepath = filepath.substring( pos+1 );
          }
          pos  = filepath.indexOf('/');
          String type = filepath.substring( 0, pos );
          filepath = filepath.substring( pos+1 );
          
          // String pathname = TDPath.getSymbolFile( filepath );
          // File file = TDFile.getPrivateFile( type, filepath );
          // TDLog.v("APP uncompress symbol " + type + " " + filepath );
          if ( overwrite || ! TDFile.existPrivateFile( type, filepath ) ) {
            // APP_SAVE SYMBOLS LOAD_MISSING
            // if ( file.exists() ) {
            //   if ( ! file.renameTo( TDFile.getFile( TDPath.getSymbolSaveFile( filepath ) ) ) ) TDLog.e("File rename error");
            // }

            // TDPath.checkPath( pathname );
            // FileOutputStream fout = TDFile.getFileOutputStream( pathname );
            FileOutputStream fout = TDFile.getPrivateFileOutputStream( type, filepath );
            int c;
            while ( ( c = zin.read( buffer ) ) != -1 ) {
              fout.write(buffer, 0, c); // offset 0 in buffer
            }
            fout.close();
          }
        }
        zin.closeEntry();
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
      TDLog.e( e.getMessage() );
    } catch ( IOException e ) {
      TDLog.e( e.getMessage() );
    }
  }

  // -------------------------------------------------------------------

  /** decompress the firmwares files
   * @param fis       input zip-file stream
   * @param overwrite whether to overwrite existing files
   * 
   * @note the output files are in the private "bin" folder, and their names are taken from the zip entries
   */
  static private void firmwareUncompress( InputStream fis, boolean overwrite )
  {
    // TDLog.v("FW " + "firmware uncompress ...");
    // TDPath.checkBinDir( );
    try {
      // byte buffer[] = new byte[36768];
      byte[] buffer = new byte[4096];
      ZipEntry ze = null;
      ZipInputStream zin = new ZipInputStream( fis );
      while ( ( ze = zin.getNextEntry() ) != null ) {  // NOTE getNextEntry() throws ZipException if zip file entry name contains '..' or starts with '/'
        String filepath = ze.getName();
        // TDLog.v( "firmware uncompress path " + filepath );
        if ( ze.isDirectory() ) continue;
        if ( ! filepath.endsWith( TDPath.DIR_BIN ) ) continue;
        // String pathname =  TDPath.getBinFilename( filepath );
        File file = TDPath.getBinFile( filepath ); // PRIVATE FILE
        if ( overwrite || ! file.exists() ) {
          // TDPath.checkPath( pathname );
          // FileOutputStream fout = TDFile.getFileOutputStream( pathname );
          FileOutputStream fout = new FileOutputStream( file );
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
      TDLog.e( e.getMessage() );
    } catch ( IOException e ) {
      TDLog.e( e.getMessage() );
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
    // TDLog.v("LRUD " + "insert LRUD " + at + " station " + splay_station );
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
    * @note before inserting the duplicate leg it set the CurrentStationName
    */
  long insertDuplicateLeg( String from, String to, float distance, float bearing, float clino, int extend )
  {
    // reset current station so that the next shot does not attach to the intermediate leg
    resetCurrentOrLastStation( );
    long time = TDUtil.getTimeStamp();
    distance = distance / TDSetting.mUnitLength;
    TDLog.v( "[2] duplicate-shot Data " + distance + " " + bearing + " " + clino );
    long id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, distance, bearing, clino, 0.0f, extend, 0.0, DBlock.FLAG_DUPLICATE, LegType.NORMAL, 1 );
    if ( mData.checkSiblings( id, TDInstance.sid, from, to, distance, bearing, clino ) ) {
      // TDLog.v("APP insert duplicate leg detect bad sibling");
      TDToast.makeWarn( R.string.bad_sibling );
    }
    mData.updateShotName( id, TDInstance.sid, from, to );
    mData.updateShotFlag( id, TDInstance.sid, DBlock.FLAG_DUPLICATE ); // @note manual shot flag does not have cavway bits
    return id;
  }

  /**
   * @param at       station where to add (?)
   * @param splay_station station of the splays (?)
   * @param left     left splay 
   * @param right    right splay 
   * @param up       up splay 
   * @param down     down splay 
   * @param bearing       leg azimuth
   * @param horizontal    whether WENS or LRUD
   * @param ret_success   ...
   */
  private long addManualSplays( long at, String splay_station, String left, String right, String up, String down,
                                float bearing, boolean horizontal, boolean ret_success )
  {
    long id;
    long time   = TDUtil.getTimeStamp();
    long extend = 0L;
    float calib = ManualCalibration.mLRUD ? ManualCalibration.mLength : 0;
    float l = -1.0f;
    float r = -1.0f;
    float u = -1.0f;
    float d = -1.0f;
    boolean ok = false;
    if ( left != null && left.length() > 0 ) {
      try {
        l = Float.parseFloat( left ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.e( "manual-shot parse error: left " + ((left==null)?"null":left) );
      }
    }  
    if ( right != null && right.length() > 0 ) {
      try {
        r = Float.parseFloat( right ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.e( "manual-shot parse error: right " + ((right==null)?"null":right) );
      }
    }
    if ( up != null && up.length() > 0 ) {
      try {
        u = Float.parseFloat( up ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.e( "manual-shot parse error: up " + ((up==null)?"null":up) );
      }
    }
    if ( down != null && down.length() > 0 ) {
      try {
        d = Float.parseFloat( down ) / TDSetting.mUnitLength - calib;
      } catch ( NumberFormatException e ) {
        TDLog.e( "manual-shot parse error: down " + ((down==null)?"null":down) );
      }
    }

    extend = ExtendType.EXTEND_IGNORE;
    if ( l >= 0.0f ) { // FIXME_X_SPLAY
      ok = true;
      if ( horizontal ) { // WENS
        // extend = TDAzimuth.computeSplayExtend( 270 );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( 270 ) : ExtendType.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, l, 270.0f, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
          // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [2]" );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, l, 270.0f, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
        }
      } else {
        float b = bearing - 90.0f;
        if ( b < 0.0f ) b += 360.0f;
        // extend = TDAzimuth.computeSplayExtend( b );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( b ) : ExtendType.EXTEND_UNSET;
        // b = in360( b );
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, l, b, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
          // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [3]" );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, l, b, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // TDLog.v("LRUD " + "insert " + id + " left " + l );
    }
    if ( r >= 0.0f ) {
      ok = true;
      if ( horizontal ) { // WENS
        // extend = TDAzimuth.computeSplayExtend( 90 );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( 90 ) : ExtendType.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, r, 90.0f, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
          // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [4]" );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, r, 90.0f, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
        }
      } else {
        // float b = bearing + 90.0f; if ( b >= 360.0f ) b -= 360.0f;
        float b = TDMath.add90( bearing );
        // extend = TDAzimuth.computeSplayExtend( b );
        // extend = ( TDSetting.mLRExtend )? TDAzimuth.computeSplayExtend( b ) : ExtendType.EXTEND_UNSET;
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, r, b, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
          // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [5]" );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, r, b, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // TDLog.v("LRUD " + "insert " + id + " right " + r );
    }
    extend = ExtendType.EXTEND_VERT;
    if ( u >= 0.0f ) {  
      ok = true;
      if ( horizontal ) {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, u, 0.0f, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
          // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [6]" );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, u, 0.0f, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
        }
      } else {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, u, 0.0f, 90.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
          // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [7]" );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, u, 0.0f, 90.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // TDLog.v("LRUD " + "insert " + id + " up " + u );
    }
    if ( d >= 0.0f ) {
      ok = true;
      if ( horizontal ) {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, d, 180.0f, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
          // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [8]" );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, d, 180.0f, 0.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
        }
      } else {
        if ( at >= 0L ) {
          id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, d, 0.0f, -90.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
          // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [9]" );
          ++at;
        } else {
          id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, d, 0.0f, -90.0f, 0.0f, extend, 0.0, DBlock.FLAG_SURVEY, LegType.XSPLAY, 1 );
        }
      }
      mData.updateShotName( id, TDInstance.sid, splay_station, TDString.EMPTY );
      // TDLog.v("LRUD " + "insert " + id + " down " + d );
    }
    // TDLog.v("LRUD " + "add manual returns " + at );
    if ( ret_success ) return (ok ? 1L : -1L);
    return at;
  }

  /** insert manual-data shot
   * @param at   id of the shot before which to insert the new shot (and LRUD)
   * @param from     from station
   * @param to       to station
   * @param distance shot length
   * @param bearing  shot azimuth
   * @param clino    shot clino
   * @param extend0  extend
   * @param flag0    flags
   * @param left     left splay 
   * @param right    right splay 
   * @param up       up splay 
   * @param down     down splay 
   * @param splay_station station of splay shots
   * @return inserted block or null on failure
   * 
   * @note manual shots take into account the instruments calibrations, LRUD can also be affected
   */
  DBlock insertManualShot( long at, String from, String to,
                           float distance, float bearing, float clino, long extend0, long flag0,
                           String left, String right, String up, String down,
                           String splay_station )
  {
    TDInstance.secondLastShotId = lastShotId();
    DBlock ret = null;
    long id;
    long time = TDUtil.getTimeStamp();

    distance = distance / TDSetting.mUnitLength - ManualCalibration.mLength;
    clino    = clino    / TDSetting.mUnitAngle  - ManualCalibration.mClino;
    float b  = bearing  / TDSetting.mUnitAngle;

    TDLog.v("APP insert manual shot " + from + "-" + to + " at " + at + " D " + distance + " B " + bearing + " C " + clino + " flag " + flag0 );

    if ( ( distance < 0.0f ) ||
         ( clino < -90.0f || clino > 90.0f ) ||
         ( b < 0.0f || b >= 360.0f ) ) {
      TDToast.makeBad( R.string.illegal_data_value );
      return null;
    }
    bearing = TDMath.in360( bearing / TDSetting.mUnitAngle - ManualCalibration.mAzimuth );

    if ( from != null && to != null && from.length() > 0 ) {
      // if ( mData.makesCycle( -1L, TDInstance.sid, from, to ) ) {
      //   TDToast.make( R.string.makes_cycle );
      // } else
      {
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot Data " + distance + " " + bearing + " " + clino );
        boolean horizontal = ( Math.abs( clino ) > TDSetting.mVThreshold );
        // TDLog.Log( TDLog.LOG_SHOT, "manual-shot SID " + TDInstance.sid + " LRUD " + left + " " + right + " " + up + " " + down);
        if ( StationPolicy.mShotAfterSplays ) {
          // TDLog.v( "[1] manual-shot Data " + distance + " " + bearing + " " + clino );
          at = addManualSplays( at, splay_station, left, right, up, down, bearing, horizontal, false );

          if ( at >= 0L ) {
            id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, distance, bearing, clino, 0.0f, extend0, 0.0, flag0, LegType.NORMAL, 1 );
            // TDLog.v("APP insert at " + at + " return " + id );
          } else {
            id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, distance, bearing, clino, 0.0f, extend0, 0.0, flag0, LegType.NORMAL, 1 );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, TDInstance.sid, from, to );
          if ( mData.checkSiblings( id, TDInstance.sid, from, to, distance, bearing, clino ) ) {
            // TDLog.v("APP insert manual shot detect bad sibling");
            TDToast.makeWarn( R.string.bad_sibling );
          }
          // mData.updateShotExtend( id, TDInstance.sid, extend0, stretch0 );
          // mData.updateShotExtend( id, TDInstance.sid, ExtendType.EXTEND_IGNORE, 1 ); // FIXME WHY ???
          // FIXME updateDisplay( );
        } else {
          // TDLog.v( "[2] manual-shot Data " + distance + " " + bearing + " " + clino );
          if ( at >= 0L ) {
            id = mData.insertManualShotAt( TDInstance.sid, at, time, 0, distance, bearing, clino, 0.0f, extend0, 0.0, flag0, LegType.NORMAL, 1 );
            // TDLog.v("APP insert at " + at + " return " + id + " incrementing at [1]" );
            ++ at;
          } else {
            id = mData.insertManualShot( TDInstance.sid, -1L, time, 0, distance, bearing, clino, 0.0f, extend0, 0.0, flag0, LegType.NORMAL, 1 );
          }
          // String name = from + "-" + to;
          mData.updateShotName( id, TDInstance.sid, from, to );
          // mData.updateShotExtend( id, TDInstance.sid, extend0, stretch0 );
          // mData.updateShotExtend( id, TDInstance.sid, ExtendType.EXTEND_IGNORE, 1 );  // FIXME WHY ???
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

  /** guess the algo from the info in the database of the current calibration 
   * @return algo code (@see CalibInfo)
   */
  int getCalibAlgoFromDB()
  {
    return mDData.selectCalibAlgo( TDInstance.cid );
  }

  /** update the algo of the current calibration in the database
   * @param algo   algo code (@see CalibInfo)
   */
  void updateCalibAlgo( int algo ) 
  {
    mDData.updateCalibAlgo( TDInstance.cid, algo );
  }
  
  /** guess the calibration algo from the device infos
   * @return algo code (@see CalibInfo)
   */
  int getCalibAlgoFromDevice()
  {
    if ( TDInstance.getDeviceA() == null ) return CalibInfo.ALGO_LINEAR;
    if ( TDInstance.isDeviceA3() ) return CalibInfo.ALGO_LINEAR; // A3
    if ( TDInstance.isDeviceX310() ) {
      if ( mComm == null ) return 1; // should not happen
      byte[] ret = mComm.readMemory( TDInstance.deviceAddress(), 0xe000 );
      if ( ret != null && ( ret[0] >= 2 && ret[1] >= 3 ) ) return CalibInfo.ALGO_NON_LINEAR;
    }
    return CalibInfo.ALGO_LINEAR; // default
  }  

  //--------------DISTOXBLE Functions------------------------
  // SIWEI_TIAN Added on Jun 2022
  /** retrieve the DISTOXBLE info
   * @param info   info display dialog
   * @return true if successful
   */
  public boolean getDistoXBLEInfo( DistoXBLEInfoDialog info )
  {
    if ( mComm != null && mComm instanceof DistoXBLEComm ) {
      DistoXBLEComm comm = (DistoXBLEComm)mComm;
      /* // boolean is_connect = comm.isConnected();
      if ( ! comm.isConnected() ) {
        connectDevice( lister?, TDInstance.deviceAddress(), DataType.DATA_ALL, timeout );
        // TDLog.v("BRIC info: wait 4 secs");
        TDUtil.yieldDown( 1000 ); // FIXME was 4000
      }
      int wait_cnt = 0;
      while( ! comm.isConnected() ) {
        TDUtil.yieldDown( 500 );
        wait_cnt++;
        if ( wait_cnt > 10 ) {
          TDLog.e("DistoXBLE info: failed to connect");
          return false;
        }
      } */
      comm.tryConnectDevice( TDInstance.deviceAddress(), null, DataType.DATA_ALL );
      comm.getXBLEInfo( info );
	  
      if ( comm.isConnected() ) {
        TDUtil.yieldDown( 1000 ); // 500
        disconnectComm();
      }
      return true;
    }
    return false;
  }

  //--------------Cavway Functions------------------------
  // SIWEI_TIAN Added on Jun 2024
  /** retrieve the Cavway info
   * @param info   info display dialog
   * @return true if successful
   */
  public boolean getCavwayInfo( CavwayInfoDialog info )
  {
    if ( mComm != null && mComm instanceof CavwayComm ) {
      CavwayComm comm = (CavwayComm)mComm;
      /* // boolean is_connect = comm.isConnected();
      if ( ! comm.isConnected() ) {
        connectDevice( lister?, TDInstance.deviceAddress(), DataType.DATA_ALL, timeout );
        // TDLog.v("BRIC info: wait 4 secs");
        TDUtil.yieldDown( 1000 ); // FIXME was 4000
      }
      int wait_cnt = 0;
      while( ! comm.isConnected() ) {
        TDUtil.yieldDown( 500 );
        wait_cnt++;
        if ( wait_cnt > 10 ) {
          TDLog.e("DistoXBLE info: failed to connect");
          return false;
        }
      } */
      comm.tryConnectDevice( TDInstance.deviceAddress(), null, DataType.DATA_ALL );
      comm.getCavwayInfo( info );

      if ( comm.isConnected() ) {
        TDUtil.yieldDown( 1000 ); // 500
        disconnectComm();
      }
      return true;
    }
    return false;
  }


  // --------------------------------------------------------

  // FIXME_SAP6
  /** send a command to the SAP6
   * @param cmd   command code (@see SapConst)
   */
  public void sendSap6Command( int cmd )
  { 
    // boolean ret = false;
    if ( mComm != null && mComm instanceof SapComm ) {
      TDLog.v( "SAP6 app send command " + cmd );
      mComm.sendCommand( cmd );
    // } else {
    //   TDLog.e("Comm is null or not SAP6");
    }
    // return ret;
  }

  /** send a command to the BRIC
   * @param cmd   command code (@see BricConst)
   */
  public void sendBricCommand( int cmd )
  { 
    // boolean ret = false;
    if ( mComm != null && mComm instanceof BricComm ) {
      // TDLog.v( "TDApp: send bric command " + cmd );
      mComm.sendCommand( cmd );
      // TDToast( R.string.bric_command_fail ); // should never happen to fail
    // } else {
    //   TDLog.e("Comm is null or not BRIC");
    }
    // return ret;
  }

  /** retrieve the BRIC info
   * @param info   info display dialog
   * @return true if successful
   */
  public boolean getBricInfo( BricInfoDialog info )
  {
    if ( mComm != null && mComm instanceof BricComm ) {
      BricComm comm = (BricComm)mComm;
      boolean disconnect = comm.isConnected();
      if ( ! disconnect ) {
        int timeout = 10; // unused
        connectDevice( null, TDInstance.deviceAddress(), DataType.DATA_ALL, timeout ); // null lister
        // TDLog.v("BRIC info: wait 4 secs");
        TDUtil.yieldDown(4000); // FIXME was 4000
      }
      if ( comm.isConnected() ) {
        comm.registerInfo( info );
        info.getInfo( comm );
        if ( disconnect ) disconnectComm();
        return true;
      } else {
        TDLog.e("BRIC info: failed to connect");
      }
    }
    return false;
  }

  /** set BRIC memory
   * @param bytes ...
   * @return true if successful
   */
  public boolean setBricMemory( byte[] bytes )
  {
    // TDLog.v( "set BRIC memory - ... " + ( (bytes == null)? "clear" : bytes[4] + ":" + bytes[5] + ":" + bytes[6] ) );
    boolean ret = false;
    if ( mComm != null && mComm instanceof BricComm ) {
      BricComm comm = (BricComm)mComm;
      boolean disconnect = comm.isConnected();
      if ( ! disconnect ) {
        int timeout = 10; // unused
        connectDevice( null, TDInstance.deviceAddress(), DataType.DATA_ALL, timeout );
        // TDLog.v("BRIC memory: wait 4 secs");
        TDUtil.yieldDown(4000);
      }
      if ( comm.isConnected() ) {
        ret = comm.setMemory( bytes );
        if ( disconnect ) disconnectComm();
        return true;
      } else {
        TDLog.e( "BRIC memory: failed to connect");
      }
    }
    return ret;
  }


  /** set the X310 laser
   * @param what      what to do:  0: off, 1: on, 2: measure
   * @param nr        number od data to download
   # @param lister    optional lister
   * @param data_type type of expected data
   * @param do_thread whether to run the call to the comm.setX310Laser() on a thread
   * @return true on success
   * @note this is called by popup menu to set laser on/off
   *                  and by DeviceTakeShot to measure splay/leg
   */
  public boolean setX310Laser( int what, int nr, ListerHandler lister, int data_type, boolean do_thread ) // FIXME_LISTER
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) return true;
    if ( mComm instanceof DistoX310Comm ) {
      DistoX310Comm comm = (DistoX310Comm)mComm;
      if ( comm != null ) {
        // TDLog.v("APP set X310 laser " + what + " nr " + nr );
        if ( do_thread ) {
          (new Thread() {
            public void run() {
              comm.setX310Laser( TDInstance.deviceAddress(), what, nr, lister, data_type );
            }
          } ).start();
          return true;
        } else {
          return comm.setX310Laser( TDInstance.deviceAddress(), what, nr, lister, data_type );
        }
      }
    } else {
      TDLog.e("APP set X310 laser: not X310 comm");
    }
    return false;
  }

  /** set the Disto-XBLE laser
   * @param what      what to do:  0: off, 1: on, 2: measure
   * @param nr        number od data to download
   # @param lister    optional lister
   * @param data_type type of expected data
   # @param closeBT   whether to close the connection at the end 
   * @param do_thread whether to run on a thread
   *                  SIWEI TIAN added on Jul
   */
  public boolean setXBLELaser( int what, int nr, ListerHandler lister, int data_type, boolean closeBT, boolean do_thread ) // FIXME_LISTER
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) return false;
    if ( mComm instanceof DistoXBLEComm ) {
      if ( do_thread ) {
        (new Thread() {
          public void run() {
            ((DistoXBLEComm)mComm).setXBLELaser(TDInstance.deviceAddress(), what, nr, lister, data_type, closeBT );
          }
        } ).start();
        return true;
      } else {
        return ((DistoXBLEComm)mComm).setXBLELaser(TDInstance.deviceAddress(), what, nr, lister, data_type, closeBT );
      }
    } else {
      TDLog.e("set XBLE laser: not XBLE comm");
    }
    return false;
  }

  /** set the Cavway laser
   * @param what      what to do:  0: off, 1: on, 2: measure
   * @param nr        number od data to download
   # @param lister    optional lister
   * @param data_type type of expected data
   # @param closeBT   whether to close the connection at the end
   * @param do_thread whether to run on a thread
   *                  SIWEI TIAN added on Jul
   */
  public boolean setCavwayLaser( int what, int nr, ListerHandler lister, int data_type, boolean closeBT, boolean do_thread ) // FIXME_LISTER
  {
    if ( mComm == null || TDInstance.getDeviceA() == null ) return false;
    if ( mComm instanceof CavwayComm ) {
      if ( do_thread ) {
        (new Thread() {
          public void run() {
            ((CavwayComm)mComm).setCavwayLaser(TDInstance.deviceAddress(), what, nr, lister, data_type, closeBT );
          }
        } ).start();
        return true;
      } else {
        return ((CavwayComm)mComm).setCavwayLaser(TDInstance.deviceAddress(), what, nr, lister, data_type, closeBT );
      }
    } else {
      TDLog.e("set XBLE laser: not XBLE comm");
    }
    return false;
  }

  // int readFirmwareHardware()
  // {
  //   return mComm.readFirmwareHardware( TDInstance.getDeviceA().getAddress() );
  // }

  /** read the firmware signature - only X310
   * @param hw        expected device hardware
   * @return firmware signature (or null if failure)
   * @note the firmware signature block is read by the Comm class which can be either X2 or XBLE
   *       and which block is read is specific to the class (ie, to the device)
   */
  public byte[] readFirmwareSignature( int hw )
  {
    TDLog.v("APP FW read signature - HW " + hw );
    // FIXME ASYNC_FIRMWARE_TASK
    // if ( mComm == null || TDInstance.getDeviceA() == null ) return;
    // if ( ! (mComm instanceof DistoX310Comm) ) return;
    // (new FirmwareTask( (DistoX310Comm)mComm, FirmwareTask.FIRMWARE_SIGN, filename )).execute( );

    if ( mComm == null || TDInstance.getDeviceA() == null ) return null;
    if ( mComm instanceof DistoX310Comm ) {
      return ((DistoX310Comm)mComm).readFirmwareSignature( TDInstance.deviceAddress(), hw );
    } else if( mComm instanceof DistoXBLEComm ) {
      return ((DistoXBLEComm)mComm).readFirmwareSignature( TDInstance.deviceAddress(), hw );
    } 
    return null;
  }

  /** read the firmware and save it to a file - only X310
   * @param name   filename including ".bin" extension
   */
  public void dumpFirmware( String name, TDProgress progress )
  {
    TDLog.v("APP FW dump " + name );
    // FIXME ASYNC_FIRMWARE_TASK
    // if ( mComm == null || TDInstance.getDeviceA() == null ) return;
    // if ( ! (mComm instanceof DistoX310Comm) ) return;
    // (new FirmwareTask( (DistoX310Comm)mComm, FirmwareTask.FIRMWARE_READ, filename )).execute( );

    if ( mComm == null || TDInstance.getDeviceA() == null ) return;
    if ( mComm instanceof DistoX310Comm ) {
      ((DistoX310Comm) mComm).dumpFirmware(TDInstance.deviceAddress(), TDPath.getBinFile(name), progress );
    } else if ( mComm instanceof DistoXBLEComm ) {
      ((DistoXBLEComm)mComm).dumpFirmware( TDInstance.deviceAddress(), TDPath.getBinFile(name), progress );
    } else {
      TDLog.e("DistoX device with no firmware upload");
    }
  }

  // /** read a firmware reading it from a file - only X310
  //  * @param name   filename including ".bin" extension
  //  */
  // public void uploadFirmware( String name )
  // {
  //   TDLog.v("APP FW upload " + name );
  //   if ( mComm == null || TDInstance.getDeviceA() == null ) return -1;
  //   if ( ! (mComm instanceof DistoX310Comm) && ! (mComm instanceof DistoXBLEComm)) return -1;     //SIWEI TIAN
  //   File file = TDPath.getBinFile( name ); // PRIVATE FILE
  //   // TDLog.v( "Firmware upload address " + TDInstance.deviceAddress() + " file " + file.getPath() );
  //   // TDLog.v( "Firmware upload file " + file.getPath() );
  //   TDLog.v("APP FW upload file " + file.getPath() );
  //   // return ((DistoX310Comm)mComm).uploadFirmware( TDInstance.deviceAddress(), pathname );
  //   //SIWEI TIAN
  //   if ( mComm instanceof DistoX310Comm ) {
  //     ((DistoX310Comm)mComm).uploadFirmware( TDInstance.deviceAddress(), file );
  //   } else if ( mComm instanceof DistoXBLEComm ) {
  //     ((DistoXBLEComm)mComm).uploadFirmware( TDInstance.deviceAddress(), file );
  //   } else {
  //     TDLog.e("DistoX device with no firmware upload");
  //   }
  // }

  /** read a firmware reading it from a file - only X310
   * @param name     filename including ".bin" extension
   * @param progress progress dialog
   */
  public void uploadFirmware( String name, TDProgress progress )
  {
    TDLog.v("APP FW upload " + name );
    if ( mComm == null || TDInstance.getDeviceA() == null ) {
      TDLog.e("Firmware upload: null device");
      return;
    }
    File file = TDPath.getBinFile( name ); // PRIVATE FILE
    TDLog.v("APP FW upload file " + file.getPath() );
    if ( mComm instanceof DistoX310Comm ) {
      ((DistoX310Comm)mComm).uploadFirmware( TDInstance.deviceAddress(), file, progress );
    } else if ( mComm instanceof DistoXBLEComm ) {
      ((DistoXBLEComm)mComm).uploadFirmware( TDInstance.deviceAddress(), file, progress );
    } else {
      TDLog.e("DistoX device with no firmware upload");
    }
  }

  // ----------------------------------------------------------------------

  /** insert a new 2D plot
   * @param sid      survey ID
   * @param name     plot name
   * @param start    plot base station (origin)
   * @param extended whether the plot is extended profile
   * @param azimuth  projection plane (for profile)
   * @param oblique  projection oblique angle (for profile, 0 normal)
   * @return plot ID
   */
  public long insert2dPlot( long sid , String name, String start, boolean extended, int azimuth, int oblique )
  {
    // PlotInfo.ORIENTATION_PORTRAIT = 0
    // TDLog.Log( TDLog.LOG_PLOT, "new plot " + name + " start " + start );
    long pid_p = mData.insertPlot( sid, -1L, name+"p",
                 PlotType.PLOT_PLAN, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, 0, 0, TDString.EMPTY, TDString.EMPTY, 0 );
    if ( extended ) {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotType.PLOT_EXTENDED, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, 0, 0, TDString.EMPTY, TDString.EMPTY, 0 );
    } else {
      long pid_s = mData.insertPlot( sid, -1L, name+"s",
                   PlotType.PLOT_PROJECTED, 0L, start, TDString.EMPTY, 0, 0, mScaleFactor, azimuth, oblique, TDString.EMPTY, TDString.EMPTY, 0 );
    }
    return pid_p;
  }
  
  /** insert a new XSection
   * @param sid      survey ID
   * @param name     section name
   * @param type     X-section type
   * @param from     viewing station
   * @param to       viewed station
   * @param azimuth  projected profile azimuth / section plane direction 
   * @param clino    projected profile clino / section plane direction 
   * @param parent   name of parent plot
   * @param nickname xsection comment
   * @return XSection plot ID
   * 
   * @note the database field "hide" is overloaded for x_sections with the parent plot name
   */
  public long insert2dSection( long sid, String name, long type, String from, String to, float azimuth, float clino, String parent, String nickname )
  {
    // FIXME COSURVEY 2d sections are not forwarded
    // pid = -1L   assign new PID
    // status = 0  normal
    // X,Y offset  0,0
    // zoom        mScaleFactor
    // orientation 0
    // TDLog.v("APP insert 2D section from <" + from + "> to <" + to + ">" );
    String hide = ( parent == null )? TDString.EMPTY : parent;
    String nick = ( nickname == null )? TDString.EMPTY : nickname;
    // FIXME the reason for this offset is not clear - especially why 3.5 ?
    float xoff = mDisplayWidth / (2*mScaleFactor) - DrawingUtil.CENTER_X;
    float yoff = mDisplayHeight / (2*mScaleFactor) - DrawingUtil.CENTER_Y;
    // TDLog.v("size " + mDisplayWidth + " " + mDisplayHeight + " offset " + xoff + " " + yoff + " density " + getDisplayDensity() + " " + getDisplayDensityDpi() );
    return mData.insertPlot( sid, -1L, name, type, 0L, from, to, xoff, yoff, mScaleFactor, azimuth, clino, hide, nick, 0 );
  }

  // @param ctx       context
  // @param filename  photo filename
  // static void viewPhoto( Context ctx, String filename )
  // {
  //   // TDLog.v( "photo <" + filename + ">" );
  //   File file = TDFile.getFile( filename );
  //   if ( file.exists() ) {
  //     // FIXME create a dialog like QCam that displays the JPEG file
  //     //
  //     // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("file://" + filename ) );
  //     
  //     if ( TDandroid.BELOW_API_24 ) {
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
    // TDLog.v( "connStateChanged()" );
    if ( mSurveyWindow != null ) mSurveyWindow.setTheTitle();
    if ( mShotWindow  != null) mShotWindow.setTheTitle();
    if ( mMainActivity != null ) mMainActivity.setTheTitle();
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
    if ( mMainActivity != null ) mMainActivity.updateDisplay();
  }

  void clearSurveyReferences()
  {
    mSurveyWindow = null;
    mShotWindow   = null;
  }

  // ---------------------------------------------------------------
  // DISTOX PAIRING
  // cannot be static because register/unregister are not static

  // active pairing request
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

  /** terminate a pairing request, if any
   */
  void stopPairingRequest()
  {
    if ( mPairingRequest != null ) {
      // TDLog.v( "stop pairing" );
      unregisterReceiver( mPairingRequest );
      mPairingRequest = null;
    }
  }

  /** start a pairing request, if none is ongoing
   */
  private void startPairingRequest()
  {
    if ( mPairingRequest == null ) {
      // TDLog.v( "start pairing" );
      // IntentFilter filter = new IntentFilter( DeviceUtil.ACTION_PAIRING_REQUEST );
      IntentFilter filter = new IntentFilter( "android.bluetooth.device.action.PAIRING_REQUEST" );
      // filter.addCategory( Intent.CATEGORY_ALTERNATIVE );
      mPairingRequest = new PairingRequest();
      registerReceiver( mPairingRequest, filter );
    }
  }

  // ==================================================================
  
  /** export survey data in a user-chosen URI --- unused URI_EXPORT
   * @param context         context
   * @param uri             output URI
   * @param export_info     export info
   * @param toast           whether to toast a message
   * @note called by (ShotWindow and) SurveyWindow on export
   *
  boolean doExportDataAsync( Context context, Uri uri, ExportInfo export_info, boolean toast )
  {
    // TDLog.v( "APP URI-export - index " + export_info.index );
    if ( export_info.index < 0 ) return false; // extra safety
    if ( export_info.index == TDConst.SURVEY_FORMAT_ZIP ) { // EXPORT ZIP
      // TDLog.v( "APP URIr-export zip");
      // this is SurveyWindow.doArchive
      while ( ! TopoDroidApp.mEnableZip ) Thread.yield();
      (new ExportZipTask( context, this, uri )).execute();
    } else {
      SurveyInfo survey_info = getSurveyInfo( );
      if ( survey_info == null ) return false;
      // TDLog.v( "APP URI-export survey " + TDInstance.survey + " Index " + export_info.index );
      String format = context.getResources().getString(R.string.saved_file_1);
      // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
      (new SaveDataFileTask( uri, format, TDInstance.sid, survey_info, mData, TDInstance.survey, TDInstance.getDeviceA(), export_info, toast )).execute();
    }
    return true;
  }
  */
  
  /** export survey data in the "out" subfolder --- APP_OUT_DATA
   * @param context         context
   * @param export_info     export info
   * @param toast           whether to toast a message
   * @note called by SurveyWindow on export
   */
  boolean doExportDataAsync( Context context, ExportInfo export_info, boolean toast )
  {
    // TDLog.v( "APP async-export - index " + export_info.index + " name " + export_info.name );
    if ( export_info.index < 0 ) return false; // extra safety
    if ( export_info.index == TDConst.SURVEY_FORMAT_ZIP ) { // EXPORT ZIP
      while ( ! TopoDroidApp.mEnableZip ) Thread.yield();
      (new ExportZipTask( context, this, null )).execute();
      // (new ExportZipTask( context, this, null )).doInForeground();
      // return false;
      return true;
    } else {
      String filename = export_info.name;
      SurveyInfo survey_info = getSurveyInfo( );
      if ( survey_info == null ) return false;
      String format = context.getResources().getString(R.string.saved_file_1);
      // TDLog.v( "APP async-export survey " + TDInstance.survey + " Index " + export_info.index + " format " + format + " filename " + filename );
      Uri uri = Uri.fromFile( new File( TDPath.getOutFile( filename ) ) );
      if ( uri != null ) {
        // TDLog.v("EXPORT " + filename + " info " + export_info.index + " " + export_info.name );
        (new SaveDataFileTask( uri, format, TDInstance.sid, survey_info, mData, TDInstance.survey, TDInstance.getDeviceA(), export_info, toast )).execute();
        return true;
      }
    }
    return false;
  }

  void shareZip( Uri uri0 )
  {
    String zipname = TDPath.getSurveyZipFile( TDInstance.survey );
    TDLog.v("Zip share file " + zipname );
    // Uri uri = Uri.fromFile( TDFile.getFile( zipname ) );
    Uri uri = MyFileProvider.fileToUri( this, TDFile.getFile( zipname ) );

    Intent intent = new Intent( );
    intent.setAction( Intent.ACTION_SEND );
    if ( TDLevel.overExpert && TDSetting.mZipShareCategory ) { // DISTOX_ZIP_SHARE_CATEGORY
      intent.addCategory( "com.topodroid.TDX.CATEGORY_SURVEY" );
    }
    intent.putExtra( Intent.EXTRA_STREAM, uri );
    intent.setType( "application/zip" );
    intent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
    try {
      mSurveyWindow.startActivity( intent );
      // mSurveyWindow.startActivity( Intent.createChooser( intent, "chooser title" ) );
    } catch ( ActivityNotFoundException e ) {
      TDToast.makeWarn( R.string.zip_share_failed );
    }
  }

  // called by zip archiver to export survey data before zip archive if TDSetting.mExportShotFormat >= 0
  // static void doExportDataSync( int exportType )
  // {
  //   if ( exportType < 0 ) return;
  //   if ( TDInstance.sid >= 0 ) {
  //     SurveyInfo survey_info = getSurveyInfo( );
  //     if ( survey_info == null ) return;
  //     TDLog.Log( TDLog.LOG_IO, "sync-export survey " + TDInstance.survey + " type " + exportType );
  //     // String saving = null; // because toast is false
  //     (new SaveDataFileTask( null, TDInstance.sid, survey_info, mData, TDInstance.survey, TDInstance.getDeviceA(), exportType, false )).immed_exec();
  //   }
  // }

  // ==================================================================
  // PATH_11

  // static void setPath11NoAgain()
  // {
  //   if ( mDData != null ) mDData.setValue( "Path11", "1" );
  // }

  // static boolean hasPath11NoAgain()
  // {
  //   if ( mDData != null ) {
  //     String no_again = mDData.getValue( "Path11" );
  //     return ( no_again != null && no_again.equals("1") );
  //   }
  //   return false;
  // }

  // ------------------------------------------------------------------
  // SCREEN ORIENTATION

  // /** lock/unlock the screen orientation for an activity
  //  * @param activity activity
  //  * @param lock     whether to lock or unlock
  //  * @note from https://riptutorial.com/android/example/21077/lock-screen-s-rotation-programmatically
  //  */ 
  // static void lockScreenOrientation( Activity activity, boolean lock ) 
  // {
  //   if ( lock ) {
  //     int currentOrientation = activity.getResources().getConfiguration().orientation;
  //     if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE ) {
  //       activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE );
  //     } else {
  //       activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT );
  //     }
  //   } else { // unlock
  //     activity.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE );
  //     // if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { // API-18
  //       activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_FULL_USER );
  //     // } else {
  //     //   activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR );
  //     // }
  //   }
  // }

}
