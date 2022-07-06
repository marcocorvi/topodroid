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
package com.topodroid.TDX;

import com.topodroid.prefs.TDSetting;
import com.topodroid.dev.Device;
import com.topodroid.common.PlotType;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.ContentResolver;
import android.content.Context;
// import android.content.Intent;
import android.content.res.Resources;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;

// import android.net.Uri;
// import androidx.documentfile.provider.DocumentFile;

// static class (singleton) with instance data
// SIWEI_TIAN changed on Jun 2022
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

  // the bluetooth device is necessary for the cstr of SAP/BRIC comm
  private static BluetoothDevice mBleDevice = null; 

  /** @return the type of the primary device (or 0 if not set)
   */
  public static int deviceType() { return (deviceA == null)? 0 : deviceA.mType; }

  /** @return the address of the primary device (or null if not set)
   */
  public static String deviceAddress() { return (deviceA == null)? null : deviceA.getAddress(); }

  /** @return the nickname of the primary device (or "-" if not set)
   */
  public static String deviceNickname() { return (deviceA == null)? "- - -" : deviceA.getNickname(); }

  /** @return true if the primary device is set and has the given address
   * @param addr   address
   */
  public static boolean isDeviceAddress( String addr ) { return deviceA != null && deviceA.getAddress().equals( addr ); }

  /** @return true if the primary device is set and is of type A3
   */
  static boolean isDeviceA3()     { return deviceA != null && deviceA.isA3(); }

  /** @return true if the primary device is set and is of type X310
   */
  static boolean isDeviceX310()   { return deviceA != null && deviceA.isX310(); }

  /** @return true if the primary device is set and is of type DistoX
   */
  static boolean isDeviceDistoX() { return deviceA != null && deviceA.isDistoX(); }

  /** @return true if the primary device is set and is of type SAP5
   */
  static boolean isDeviceSap()    { return deviceA != null && deviceA.isSap(); }

  /** @return true if the primary device is set and is of type BRIC4
   */
  static boolean isDeviceBric()   { return deviceA != null && deviceA.isBric(); }

  /** @return true if the primary device is set and is of type DistoXBLE
   * SIWEI_TIAN
   */
  static boolean isDeviceDistoXBLE()   { return deviceA != null && deviceA.isDistoXBLE(); }

  /** @return primary bluetooth device
   */
  public static Device getDeviceA() 
  { 
    // if ( TDLevel.overExpert ) return deviceA;
    // if ( isDeviceBLE( deviceA ) ) return null;
    return deviceA;
  }

  /** @return secondary bluetooth device
   */
  public static Device getDeviceB() 
  { 
    // if ( TDLevel.overExpert ) return deviceB;
    // if ( isDeviceBLE( deviceB ) ) return null;
    return deviceB;
  }

  /** set primary bluetooth device
   * @param device bluetooth device
   */
  public static void setDeviceA( Device device ) { deviceA = device; }

  /** set secondary bluetooth device
   * @param device bluetooth device
   */
  public static void setDeviceB( Device device ) { deviceB = device; }

  /** @return bluetooth LE device
   */
  static BluetoothDevice getBleDevice()
  {
    if ( mBleDevice == null ) initBleDevice();
    // TDLog.v("BLE " + "TD Instance: get ble device " + ((mBleDevice == null)? "null" : mBleDevice.getName() ) );
    return mBleDevice;
  }

  /** initialize the bluetooth LE device
   */
  static void initBleDevice( )
  {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    mBleDevice = adapter.getRemoteDevice( TDInstance.deviceAddress() );
  }

  /** set the bluetooth LE device
   * @param dev   bluetooth LE device
   */
  static void setBleDevice( BluetoothDevice dev ) 
  { 
    mBleDevice = dev;
    // TDLog.v("BLE " + "TD Instance: set ble device " + ( (dev==null)? "null" : dev.getName() ) );
  }

  /** @return true if the bluetooth LE device is set
   */
  static boolean hasBleDevice() { return mBleDevice != null; }

  /** @return true if the primary device is LE
   * SIWEI_TIAN
   */
  static boolean isDeviceBLE()    { return deviceA != null && ( deviceA.isBric() || deviceA.isSap() || deviceA.isDistoXBLE()); }

  /** @return true if the device is LE
   * @param device   bluetooth device
   * SIWEI_TIAN
   */
  private static boolean isDeviceBLE( Device device )    { return device != null && ( device.isBric() || device.isSap() || device.isDistoXBLE()); }

  /** @return true if the connection is set in continuous mode
   */
  static boolean isContinuousMode() 
  {
    return TDSetting.isConnectionModeContinuous() || isDeviceBLE();
  }

  /** @return true if the bluetooth device has remote control
   */
  static boolean hasDeviceRemoteControl() 
  {
    return deviceA != null && ( deviceA.isX310() || deviceA.isBric() ); 
  }

  // FIXME VirtualDistoX
  // static boolean isDeviceZeroAddress( ) { return ( deviceA == null || deviceA.getAddress().equals( Device.ZERO_ADDRESS ) ); }

  /** @return true if the primary device is set
   */
  static boolean isDeviceZeroAddress( ) { return ( deviceA == null ); }

  // FIXME second-DistoX
  /** @return the type of the secondary device (or 0if not set)
   */
  static int secondDeviceType() { return (deviceB == null)? 0 : deviceB.mType; }

  /** @return the address of the secondary device (or null if not set)
   */
  static String secondDeviceAddress() { return (deviceB == null)? null : deviceB.getAddress(); }

  /** @return true if the secondary device is set and has the given address
   * @param addr   device address
   */
  static boolean isSecondDeviceAddress( String addr ) { return deviceB != null && deviceB.getAddress().equals( addr ); }

  static String recentPlot = null;
  static long   recentPlotType = PlotType.PLOT_PLAN;

  /** switch device
   * @return true if successfully switched
   */
  static boolean switchDevice()
  {
    if ( deviceB == null ) return false;
    Device tmp = deviceA;
    deviceA = deviceB;
    deviceB = tmp;
    return true;
  }

  /** @return a bundle containing TopoDroid status
   */
  static Bundle toBundle()
  {
    Bundle b = new Bundle();
    b.putString(  "TOPODROID_CWD", cwd );
    // b.putString(  "TOPODROID_CBD", cbd );
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

  /** restore TopoDroid status from a bundle
   * @param ctx context - must be the application context
   * @param b   bundle
   */
  static void fromBundle( Context ctx, Bundle b )
  {
    context = ctx;
    cwd = b.getString( "TOPODROID_CWD" );
    // cbd = b.getString( "TOPODROID_CBD" );
    sid = b.getLong( "TOPODROID_SID" );
    cid = b.getLong( "TOPODROID_CID" );
    survey = b.getString( "TOPODROID_SURVEY" );
    calib  = b.getString( "TOPODROID_CALIB"  );
    secondLastShotId = b.getLong( "TOPODROID_SECOND_LAST_SHOT_ID" );
    xsections = b.getBoolean( "TOPODROID_XSECTIONS" );
    String addr = b.getString( "TOPODROID_DEVICE" );
    if ( TDString.isNullOrEmpty( addr ) ) {
      deviceA = null;
    // } else {
      // deviceA = TopoDroidApp.getDevice( addr ); // FIXME_DEVICE_STATIC
    }
    addr = b.getString( "TOPODROID_SECOND_DEVICE" );
    if ( TDString.isNullOrEmpty( addr ) ) {
      deviceB = null;
    // } else {
      // deviceB = TopoDroidApp.getDevice( addr ); // FIXME_DEVICE_STATIC
    }
    recentPlot = null;
    recentPlotType = PlotType.PLOT_PLAN;
  }

  /** set the application context
   * @param ctx   context
   */
  static void setContext( Context ctx ) { context = ctx; }

  /** @return the content resolver of the application context
   */
  public static ContentResolver getContentResolver() { return context.getContentResolver(); }
  
  /** @return the application resources
   */
  public static Resources getResources() { return context.getResources(); }

  /** @return resource string
   * @param r   resource index
   */
  public static String getResourceString( int r ) { return context.getResources().getString( r ); }

  /** @return string formatted with a resource
   * @param r   resource index
   * @param arg argument
   */
  public static String formatString( int r, String arg ) 
  {
    return String.format( context.getResources().getString( r ), arg );
  }

  /** @return the application shared preferences
   */
  public static SharedPreferences getPrefs() { return PreferenceManager.getDefaultSharedPreferences( context ); }

  /** set the "recent" plot
   * @param name   plot name
   * @param type   plot type
   */
  static void setRecentPlot( String name, long type )
  {
    recentPlot     = name;
    recentPlotType = type;
  }

  /** @return true if survey data are set to "diving mode"
   */
  static boolean isDivingMode() { return datamode == SurveyInfo.DATAMODE_DIVING; }


  // FOLDER PERMISSION ----------------------------------------------------
  // This ok to get permission to the Document Tree, not the File API unfortunately

  // static final String TDX_TREE_URI = "TDX_TREE_URI";

  // /** take persistent permissions for a given uri
  //  * @param uri   uri
  //  * @param flag  intent flags
  //  * @return true if successful
  //  */
  // static boolean takePersistentPermissions( Uri uri, int flag )
  // {
  //   if ( (flag & Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) == 0 ) return false;
  //   flag = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
  //   getContentResolver().takePersistableUriPermission( uri, flag );
  //   return true;
  // }

  // /** @return true if the app has permission on the (document tree) folder
  //  */
  // static boolean hasFolderPermission( )
  // {
  //   boolean ret = false;
  //   try {
  //     // SharedPreferences preferences = context.getPreferences(MODE_PRIVATE);
  //     SharedPreferences preferences = getPrefs();
  //     String treeUri = preferences.getString( TDX_TREE_URI, "");
  //     Uri uri = Uri.parse(treeUri);
  //     ret = DocumentFile.fromTreeUri( context, uri).canWrite();
  //     TDLog.v("folder " + treeUri + " has permission " + ret );
  //   } catch (Exception e) {
  //     TDLog.Error("has folder permission error: " + e.getMessage() );
  //   }
  //   return ret;
  // }

  // /** handle a return from the activity that request folder permission
  //  * @param data   return data
  //  * @return true if successful
  //  */
  // static boolean handleRequestTreeUri( Intent data )
  // {
  //   if ( data == null ) return false;
  //   Uri treeUri = data.getData();
  //   if ( treeUri == null ) return false;
  //   DocumentFile file = DocumentFile.fromTreeUri( context, treeUri );
  //   if ( file == null ) return false;
  //   boolean ret = file.canWrite();
  //   TDLog.v("result TREE URI return " + ret );
  //   if ( ret ) {
  //     if ( takePersistentPermissions( treeUri, data.getFlags() ) ) {
  //       TDLog.v("result TREE URI taken persistent permissions" );
  //       SharedPreferences preferences = getPrefs();
  //       SharedPreferences.Editor editor = preferences.edit();
  //       editor.putString( TDX_TREE_URI, data.getDataString() );
  //       editor.commit();
  //     }
  //   }
  //   return ret;
  // }

}
