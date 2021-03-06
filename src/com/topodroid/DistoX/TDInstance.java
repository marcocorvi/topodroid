/* @file TDInstance.java
 *
 * @author marco corvi
 * @date aug 2018
 *
 * @brief TopoDroid instance data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
import com.topodroid.common.PlotType;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

import android.util.Log;

// static class (singleton) with instance data
public class TDInstance
{
  public static Context context; // must be the application context FIXME LEAK AND BREAKS INSTANT RUN

  public static String cwd;  // current work directory
  public static String cbd;  // current base directory

  public static long sid   = -1;   // id of the current survey
  public static long cid   = -1;   // id of the current calib
  public static String survey;   // current survey name
  static String calib;    // current calib name
  static long secondLastShotId = 0L;

  static boolean xsections = false; // current value of mSharedSections
  public static int     datamode = 0;      // current value of survey datamode
  // FIXME static int    extend = 90;  // current value of survey extend

  private static Device  deviceA = null;
  private static Device  deviceB = null; // second-DistoX

  public static Device getDeviceA() 
  { 
    // if ( TDLevel.overExpert ) return deviceA;
    // if ( isDeviceBLE( deviceA ) ) return null;
    return deviceA;
  }

  public static Device getDeviceB() 
  { 
    // if ( TDLevel.overExpert ) return deviceB;
    // if ( isDeviceBLE( deviceB ) ) return null;
    return deviceB;
  }

  public static void setDeviceA( Device device ) { deviceA = device; }
  public static void setDeviceB( Device device ) { deviceB = device; }

  // the bluetooth device is necessary for the cstr of SAP/BRIC comm
  private static BluetoothDevice mBleDevice = null; 

  static BluetoothDevice getBleDevice()
  {
    if ( mBleDevice == null ) initBleDevice();
    // Log.v("DistoX-BLE", "TD Instance: get ble device " + ((mBleDevice == null)? "null" : mBleDevice.getName() ) );
    return mBleDevice;
  }

  static void initBleDevice( )
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    mBleDevice = adapter.getRemoteDevice( TDInstance.deviceAddress() );
  }

  static void setBleDevice( BluetoothDevice dev ) 
  { 
    mBleDevice = dev;
    // Log.v("DistoX-BLE", "TD Instance: set ble device " + ( (dev==null)? "null" : dev.getName() ) );
  }

  static boolean hasBleDevice() { return mBleDevice != null; }

  public static int deviceType() { return (deviceA == null)? 0 : deviceA.mType; }
  public static String deviceAddress() { return (deviceA == null)? null : deviceA.getAddress(); }
  public static String deviceNickname() { return (deviceA == null)? "- - -" : deviceA.getNickname(); }
  public static boolean isDeviceAddress( String addr ) { return deviceA != null && deviceA.getAddress().equals( addr ); }

  static boolean isDeviceA3()     { return deviceA != null && deviceA.isA3(); }
  static boolean isDeviceX310()   { return deviceA != null && deviceA.isX310(); }
  static boolean isDeviceDistoX() { return deviceA != null && deviceA.isDistoX(); }
  static boolean isDeviceSap()    { return deviceA != null && deviceA.isSap(); }
  static boolean isDeviceBric()   { return deviceA != null && deviceA.isBric(); }

  static boolean isDeviceBLE()    { return deviceA != null && ( deviceA.isBric() || deviceA.isSap() ); }
  private static boolean isDeviceBLE( Device device )    { return device != null && ( device.isBric() || device.isSap() ); }

  static boolean isContinuousMode() 
  {
    return TDSetting.isConnectionModeContinuous() || isDeviceBLE();
  }

  static boolean hasDeviceRemoteControl() 
  {
    return deviceA != null && ( deviceA.isX310() || deviceA.isBric() ); 
  }

  // FIXME VitualDistoX
  // static boolean isDeviceZeroAddress( ) { return ( deviceA == null || deviceA.getAddress().equals( Device.ZERO_ADDRESS ) ); }
  static boolean isDeviceZeroAddress( ) { return ( deviceA == null ); }

  // FIXME second-DistoX
  static int secondDeviceType() { return (deviceB == null)? 0 : deviceB.mType; }
  static String secondDeviceAddress() { return (deviceB == null)? null : deviceB.getAddress(); }
  static boolean isSecondDeviceAddress( String addr ) { return deviceB != null && deviceB.getAddress().equals( addr ); }

  static String recentPlot = null;
  static long   recentPlotType = PlotType.PLOT_PLAN;

  static boolean switchDevice()
  {
    if ( deviceB == null ) return false;
    Device tmp = deviceA;
    deviceA = deviceB;
    deviceB = tmp;
    return true;
  }

  public static ContentResolver getContentResolver() { return context.getContentResolver(); }
  
  static boolean isDivingMode() { return datamode == SurveyInfo.DATAMODE_DIVING; }

  public static SharedPreferences getPrefs() { return PreferenceManager.getDefaultSharedPreferences( context ); }

  static Bundle toBundle()
  {
    Bundle b = new Bundle();
    b.putString(  "TOPODROID_CWD", cwd );
    b.putString(  "TOPODROID_CBD", cbd );
    b.putLong(    "TOPODROID_SID", sid );
    b.putLong(    "TOPODROID_CID", cid );
    b.putString(  "TOPODROID_SURVEY", survey );
    b.putString(  "TOPODROID_CALIB",  calib  );
    b.putLong(    "TOPODROID_SECOND_LAST_SHOT_ID", secondLastShotId );
    b.putBoolean( "TOPODROID_XSECTIONS", xsections );
    b.putString(  "TOPODROID_DEVICE", ( (deviceA == null)? "" : deviceA.getAddress())  );
    b.putString(  "TOPODROID_SECOND_DEVICE", ( (deviceB == null)? "" : deviceB.getAddress())  );
    return b;
  }

  // @param ctx must be the application context
  static void fromBundle( Context ctx, Bundle b )
  {
    context = ctx;
    cwd = b.getString( "TOPODROID_CWD" );
    cbd = b.getString( "TOPODROID_CBD" );
    sid = b.getLong( "TOPODROID_SID" );
    cid = b.getLong( "TOPODROID_CID" );
    survey = b.getString( "TOPODROID_SURVEY" );
    calib  = b.getString( "TOPODROID_CALIB"  );
    secondLastShotId = b.getLong( "TOPODROID_SECOND_LAST_SHOT_ID" );
    xsections = b.getBoolean( "TOPODROID_XSECTIONS" );
    String addr = b.getString( "TOPODROID_DEVICE" );
    if ( addr == null || addr.length() == 0 ) {
      deviceA = null;
    // } else {
      // deviceA = TopoDroidApp.getDevice( addr ); // FIXME_DEVICE_STATIC
    }
    addr = b.getString( "TOPODROID_SECOND_DEVICE" );
    if ( addr == null || addr.length() == 0 ) {
      deviceB = null;
    // } else {
      // deviceB = TopoDroidApp.getDevice( addr ); // FIXME_DEVICE_STATIC
    }
    recentPlot = null;
    recentPlotType = PlotType.PLOT_PLAN;
  }

  static void setContext( Context ctx ) { context = ctx; }

  public static Resources getResources() { return context.getResources(); }

  static void setRecentPlot( String name, long type )
  {
    recentPlot     = name;
    recentPlotType = type;
  }

}
