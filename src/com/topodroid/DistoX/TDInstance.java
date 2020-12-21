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


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.res.Resources;

import android.bluetooth.BluetoothDevice;

// static class (singleton) with instance data
public class TDInstance
{
  public static Context context; // must be the application context FIXME LEAK AND BREAKS INSTANT RUN

  public static String cwd;  // current work directory
  public static String cbd;  // current base directory

  static long sid   = -1;   // id of the current survey
  static long cid   = -1;   // id of the current calib
  static String survey;   // current survey name
  static String calib;    // current calib name
  static long secondLastShotId = 0L;

  static boolean xsections = false; // current value of mSharedSections
  public static int     datamode = 0;      // current value of survey datamode
  // FIXME static int    extend = 90;  // current value of survey extend

  static Device  deviceA = null;
  static Device  deviceB = null; // second-DistoX
  static BluetoothDevice bleDevice = null; // FIXME BLE_5

  static int deviceType() { return (deviceA == null)? 0 : deviceA.mType; }
  static String deviceAddress() { return (deviceA == null)? null : deviceA.mAddress; }
  static String deviceNickname() { return (deviceA == null)? "- - -" : deviceA.getNickname(); }
  static boolean isDeviceAddress( String addr ) { return deviceA != null && deviceA.mAddress.equals( addr ); }

  // FIXME VitualDistoX
  // static boolean isDeviceZeroAddress( ) { return ( deviceA == null || deviceA.mAddress.equals( Device.ZERO_ADDRESS ) ); }
  static boolean isDeviceZeroAddress( ) { return ( deviceA == null ); }

  // FIXME second-DistoX
  static int secondDeviceType() { return (deviceB == null)? 0 : deviceB.mType; }
  static String secondDeviceAddress() { return (deviceB == null)? null : deviceB.mAddress; }
  static boolean isSecondDeviceAddress( String addr ) { return deviceB != null && deviceB.mAddress.equals( addr ); }

  static String recentPlot = null;
  static long   recentPlotType = PlotInfo.PLOT_PLAN;

  static boolean switchDevice()
  {
    if ( deviceB == null ) return false;
    Device tmp = deviceA;
    deviceA = deviceB;
    deviceB = tmp;
    return true;
  }

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
    b.putString(  "TOPODROID_DEVICE", ( (deviceA == null)? "" : deviceA.mAddress)  );
    b.putString(  "TOPODROID_SECOND_DEVICE", ( (deviceB == null)? "" : deviceB.mAddress)  );
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
    recentPlotType = PlotInfo.PLOT_PLAN;
  }

  static void setContext( Context ctx ) { context = ctx; }

  public static Resources getResources() { return context.getResources(); }

  static void setRecentPlot( String name, long type )
  {
    recentPlot     = name;
    recentPlotType = type;
  }

}
