/* @file TDandroid.java
 *
 * @author marco corvi
 * @date jan 2019
 *
 * @brief TopoDroid android versions specifics
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * TODO Live permission check:
 CAMERA and AUDIO have the same issue.
 First check that the camera and audio code paths also request them lazily, and how MainWindow uses the returned count.
 *
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDRequest;
// import com.topodroid.util.TDVersion;
import com.topodroid.prefs.TDSetting;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;

import android.content.SharedPreferences.Editor;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ConfigurationInfo;
// import android.content.pm.PackageManager.NameNotFoundException;

// import java.lang.reflect.Method;

import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Handler;
import android.os.Bundle;
// import android.os.Build.VERSION_CODES;

import android.provider.Settings;

import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;

import android.hardware.Sensor;
import android.widget.Button;
import android.widget.SeekBar;
// import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.PointF;
// import android.view.Display;
// import android.view.Surface;

import java.util.Locale;
import java.util.ArrayList;

public class TDandroid
{
  // final static public boolean BELOW_API_13 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2 );
  // final static public boolean BELOW_API_15 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 );
  // final static public boolean BELOW_API_18 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 );
  // final static public boolean BELOW_API_19 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT );
  // final static public boolean BELOW_API_21 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP );
  // minSdkApi = 21
  final static public boolean BELOW_API_23 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.M );
  final static public boolean BELOW_API_24 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.N );
  final static public boolean BELOW_API_26 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.O );
  final static public boolean BELOW_API_29 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.Q );
  final static public boolean BELOW_API_30 = ( Build.VERSION.SDK_INT < 30 ) ; // Build.VERSION_CODES.R );
  final static public boolean BELOW_API_31 = ( Build.VERSION.SDK_INT < 31 ) ; // Build.VERSION_CODES.S ); Android-12
  final static public boolean BELOW_API_33 = ( Build.VERSION.SDK_INT < 33 ) ; // Android-13
  // final static public boolean BELOW_API_34 = ( Build.VERSION.SDK_INT < 34 ) ; // Android-14
  final static public boolean BELOW_API_35 = ( Build.VERSION.SDK_INT < 35 ) ; // Android-15
  final static public boolean BELOW_API_36 = ( Build.VERSION.SDK_INT < 36 ) ; // Android-16

  // final static public boolean ABOVE_API_16 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLYBEAN );
  final static public boolean ABOVE_API_21 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP );
  final static public boolean ABOVE_API_23 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.M ); 
  final static public boolean ABOVE_API_24 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.N ); // Android-6 Nougat
  final static public boolean ABOVE_API_26 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.O ); // Android-8 Oreo
  final static public boolean ABOVE_API_28 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.P ); // 
  final static public boolean ABOVE_API_29 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.Q ); // Android-10

  // final static public boolean AT_LEAST_API_15 = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWITCH_MR1 );
  final static public boolean AT_LEAST_API_21 = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP );
  final static public boolean AT_LEAST_API_23 = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M );
  final static public boolean AT_LEAST_API_24 = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ); // Android-6 Nougat
  // final static public boolean AT_LEAST_API_31 = ( Build.VERSION.SDK_INT >= 31 ); 
  final static public boolean AT_LEAST_API_33 = ( Build.VERSION.SDK_INT >= 33 );
  // final static public boolean AT_LEAST_API_34 = ( Build.VERSION.SDK_INT >= 34 );
  // final static public boolean AT_LEAST_API_35 = ( Build.VERSION.SDK_INT >= 35 );
  final static public boolean AT_LEAST_API_36 = ( Build.VERSION.SDK_INT >= 36 );

  final static public boolean PRIVATE_STORAGE = false;

  /** permissions string codes
   */ 
  static final String[] perms = {
      android.Manifest.permission.BLUETOOTH,            // Bluetooth permissions are normal - no need to request at runtime
      android.Manifest.permission.BLUETOOTH_ADMIN,
      android.Manifest.permission.BLUETOOTH_CONNECT, // API-31
      android.Manifest.permission.BLUETOOTH_SCAN,    // API-31
      // android.Manifest.permission.INTERNET,
      // android.Manifest.permission.MANAGE_EXTERNAL_STORAGE, // will always be denied from API-30
      android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
      android.Manifest.permission.READ_EXTERNAL_STORAGE,
      android.Manifest.permission.ACCESS_FINE_LOCATION // needed fot BLE scan
      // .. android.Manifest.permission.CAMERA,
      // .. android.Manifest.permission.RECORD_AUDIO
      // android.Manifest.permission.ACCESS_MEDIA_LOCATION
  };

  static final String[] permNames = {
      "BLUETOOTH", 
      "BLUETOOTH_ADMIN",
      "BLUETOOTH_CONNECT", // API-31
      "BLUETOOTH_SCAN",    // CODE-S
      // "MANAGE_EXTERNAL_STORAGE", // API-30
      "WRITE_EXTERNAL_STORAGE",
      "READ_EXTERNAL_STORAGE",
      // "INTERNET",
      // .. "ACCESS_FINE_LOCATION",
      // .. "CAMERA",
      // .. "RECORD_AUDIO"
      // "ACCESS_MEDIA_LOCATION"
  };

  static final String[] permShortNames = {
      "BLUETOOTH", 
      "BT_ADMIN",
      "BT_CONNECT", // API-31
      "BT_SCAN",    // API-31
      // "MANAGE_FILE", // API-30
      "WRITE_FILE",
      "READ_FILE",
      // "INTERNET",
      // .. "LOCATION",
      // .. "CAMERA",
      // .. "AUDIO"
      // "ACCESS_MEDIA_LOCATION"
  };

  // static final String mPermissionManageExternalStorage = android.Manifest.permission.MANAGE_EXTERNAL_STORAGE; // API-30
  static final String mPermissionManageExternalStorage = "android.permission.MANAGE_EXTERNAL_STORAGE";

  static final int NR_PERMS_D = 6; // 5 API-31 "mandatory" permissions
  // static final int NR_PERMS   = NR_PERMS_D + 3;

  // private static final int PERM_BT         = 0;
  // private static final int PERM_BT_ADMIN   = 1;
  private static final int PERM_BT_CONNECT = 2; // 2 API-31, use -1 to fail test and skip \
  private static final int PERM_BT_SCAN    = 3; // 3 API-31
  private static final int PERM_WRITE      = 4;
  private static final int PERM_READ       = 5; 
  private static final int PERM_LOCATION   = NR_PERMS_D;
  // .. private static final int PERM_CAMERA     = NR_PERMS_D + 1; 
  // .. private static final int PERM_AUDIO      = NR_PERMS_D + 2;
  // private static final int PERM_MEDIA      = NR_PERMS_D + 3;

  // private static boolean MustRestart = false; // whether need to restart app
  // static boolean[] GrantedPermission = { false, false, false, false, false, false };
/* FIXME-23 */
  static final int TEMPERATURE      = Sensor.TYPE_AMBIENT_TEMPERATURE; // REQUIRES API-14

  public static final int TITLE_NORMAL     = 0xff6699ff; // FIXED_BLUE same as in values/styles.xml
  public static final int TITLE_NORMAL2    = 0xff99ccff; 
  public static final int TITLE_BACKSHOT   = 0xff0099cc; // DARK BLUE
  public static final int TITLE_BACKSIGHT  = 0xffb66dff; // VIOLET
  public static final int TITLE_TRIPOD     = 0xffff6db6; // PINK
  public static final int TITLE_TOPOROBOT  = 0xffdbd100; // ORANGE
  public static final int TITLE_ANOMALY    = 0xffff3333; // BRIGHT RED

  /** @return the Android make (manufacturer + model)
   * https://stackoverflow.com/questions/1995439/get-android-phone-model-programmatically-how-to-get-device-name-and-model-prog
   */
  public static String getAndroidModel()
  {
    String manufacturer = Build.MANUFACTURER;
    String model = Build.MODEL;
    if (model.toLowerCase( Locale.US ).startsWith(manufacturer.toLowerCase( Locale.US ))) {
      return model;
    } else {
      return manufacturer + "-" + model;
    }
  }

  /** apply the changes stored in the editor
   * @param editor   preferences editor
   * @return true if successful
   */
  public static boolean applyEditor( Editor editor )
  {
    // editor.apply(); 
    return editor.commit(); 
  }

  /** GrantedPermission[] is still only refreshed in createPermissions().
   * Anything that reads it, for example for BLE connection, should switch to live checks (see checkLocation() for example)
   */
  static boolean[] GrantedPermission = { false, false, false, false, false, false /* .. , false, false, false */ }; // size = NR_PERMS_D
  static boolean MustRestart = false; // whether need to restart app

  // number of times permissions are requested

  /** @return true if a permission has been granted
   * @param ctx context
   * @param permission permission
   */
  private static boolean hasPermission( Context ctx, String permission )
  {
    if ( BELOW_API_23 ) return ctx.checkCallingOrSelfPermission( permission ) == PackageManager.PERMISSION_GRANTED;
    return ( ctx.checkSelfPermission( permission ) == PackageManager.PERMISSION_GRANTED );
  }

  private static boolean hasCallingOrPermission( Context ctx, String permission )
  {
    return ( ctx.checkCallingOrSelfPermission( permission ) == PackageManager.PERMISSION_GRANTED );
  }

  /** @return the number of mandatory permissions that are not granted
   * @param ctx  context
   * @param activity activity
   * @param time     time that the permissions are requested
   */
  static int createPermissions( Context ctx, Activity activity, int time )
  {
    TDLog.v( "PERM create permissions" );
    MustRestart = false;
    if ( BELOW_API_23 ) {
      // TDLog.v("PERM create perms: below API-23 - return " );
      return 0;
    }

    // StringBuilder sb = new StringBuilder();
    // sb.append("Not granted" );

    ArrayList< String > ask = new ArrayList<>();
    int not_granted = 0;
    for ( int k=0; k<NR_PERMS_D; ++k ) { // check whether the app has the six permissions
      // TDLog.v("PERM Create permission " + k + ": " + permNames[k] );

      if ( k == PERM_BT_CONNECT && BELOW_API_31 ) { // BT_CONNECT only for API >= 31 - API-31
        // TDLog.v("PERM Create permission " + k + ": " + permNames[k] + " granted below 31" );
        GrantedPermission[k] = true;
        continue;
      } 

      if ( k == PERM_BT_SCAN && BELOW_API_31 ) { // BT_SCAN only for API >= 31 - API-31
        // TDLog.v("PERM Create permission " + k + ": " + permNames[k] + " granted below 31" );
        GrantedPermission[k] = true;
        continue;
      } 

      if ( (k == PERM_WRITE || k == PERM_READ) && (PRIVATE_STORAGE || AT_LEAST_API_33) ) {
        // TDLog.v("PERM Create permission " + k + ": " + permNames[k] + " not granted above 33" );
        // GrantedPermission[k] = false;
        continue;
      }

      GrantedPermission[k] = hasPermission( ctx, perms[k] );
      if ( ! GrantedPermission[k] ) {
        TDLog.v( "PERM " + permNames[k] + " not granted - request time " + time );
        ask.add( perms[k] );
        if ( time > 1 ) {
          activity.requestPermissions( new String[] { perms[k] }, TDRequest.REQUEST_PERMISSIONS );
          GrantedPermission[k] = hasPermission( ctx, perms[k] );
          if ( ! GrantedPermission[k] ) ++not_granted;
        } else {
          ++not_granted;
        }
        // if ( k < NR_PERMS_D ) MustRestart = true;
      } else {
        // TDLog.v( "PERM " + k + ": " + permNames[k] + " granted ");
      }
    }
    TDLog.v("PERM create perms " + time + ": not granted " + not_granted + " / " + NR_PERMS_D );

    if ( not_granted > 0 && time < 3 && ! ask.isEmpty() ) {
      TDLog.v( "request perms time " + time );
      activity.requestPermissions( ask.toArray( new String[0] ), TDRequest.REQUEST_PERMISSIONS );
    }
    
    // TDLog.v("PERM " + "FC must restart " + MustRestart + " " + sb.toString() );
    // if ( MustRestart ) { // if a permission has not been granted request it
    //   // TDToast.make( "TopoDroid cannot do anything useful without" + sb.toString() );
    //   activity.requestPermissions( perms, TDRequest.REQUEST_PERMISSIONS );
    //   // TDLog.v("PERM " + "exit 1");
    //   android.os.Process.killProcess( android.os.Process.myPid() );
    //   System.exit( 1 );
    // }

    return not_granted;
  }

  /** check if the app can access external storage
   * @param ctx app context
   * @return true if the app can access external storage
   */
  static boolean canManageExternalStorage( Context ctx )
  {
    if ( BELOW_API_30 ) return true;
    if ( PRIVATE_STORAGE ) return true; // skip external storage
    // return ( ctx.checkSelfPermission( mPermissionManageExternalStorage ) == PackageManager.PERMISSION_GRANTED );
    boolean ret = Environment.isExternalStorageManager();
    // TDLog.v("MANAGE external storage: " + ret );
    return ret;
  }

  /** request external storage access 
   * @param ctx    context
   * @param activity   activity
   * #see https://stackoverflow.com/questions/64250814/how-to-obtain-manage-external-storage-permission
   *
   * TODO TO TRY
   * if called from onCreate could use startActivityForResult with requestCode >= 0
   * but I am not sure Settings return a result 
   */
  static void requestExternalStorage(  Context ctx, Activity activity )
  {
    // TDLog.v("MANAGE request external storage");
    if ( PRIVATE_STORAGE ) return;

    Intent intent = new Intent();
    intent.setAction( Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION ); // API_30
    Uri uri = Uri.fromParts( "package", ctx.getPackageName(), null );
    intent.setData( uri );
    try {
      activity.startActivity( intent );
    } catch ( ActivityNotFoundException e ) {
      TDLog.e("Error " + e.getMessage() );
    }
  }

  /** open the system settings page of this app (where the user can grant a permanently denied permission)
   * @param ctx    context
   */
  static void openAppSettings( Context ctx )
  {
    Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
    intent.setData( Uri.fromParts( "package", ctx.getPackageName(), null ) );
    intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
    try {
      ctx.startActivity( intent );
    } catch ( ActivityNotFoundException e ) {
      TDLog.e("Error " + e.getMessage() );
    }
  }

  /** #return true if can do a BLE scan
   * from API-23 (Android-6) to API-30 (Android-11) we need LOCATION to do BLE SCAN
   * from API-31 (Android-12) we need BLUETOOTH_SCAN and BLUETOOTH_CONNECT
   *
   * @note it would be better to request parmission at runtime when a BLE scan is performed
   */
  static boolean canScanBLE( Context ctx )
  {
    return missingScanPermissions( ctx ).length == 0;
  }

  /** @return the permissions missing for a BLE scan (empty if none) 
   */
  static String[] missingScanPermissions( Context ctx )
  {
    ArrayList<String> list = new ArrayList<>();
    if ( BELOW_API_31 ) {
      if ( ! hasPermission( ctx, perms[ PERM_LOCATION ] ) ) list.add( perms[ PERM_LOCATION ] );
    } else {
      if ( ! hasPermission( ctx, perms[ PERM_BT_SCAN ] ) )    list.add( perms[ PERM_BT_SCAN ] );
      if ( ! hasPermission( ctx, perms[ PERM_BT_CONNECT ] ) ) list.add( perms[ PERM_BT_CONNECT ] );
    }
    return list.toArray( new String[0] );
  }


  /** check if the app has the minimal permissions
   * @param ctx    context
   * @param activity   activity (unused)
   * @return true is the app can run
   */
  static boolean canRun( Context ctx, Activity activity )
  {
    if ( BELOW_API_23 ) return true;
    for ( int k=0; k<NR_PERMS_D; ++k ) { // check whether the app has the "mandatory" permissions
      if ( k == PERM_BT_CONNECT && BELOW_API_31 ) continue; // BT_CONNECT only for API >= 31 - API-31
      if ( k == PERM_BT_SCAN && BELOW_API_31 ) continue; // BT_SCAN 
      if ( (k == PERM_WRITE || k == PERM_READ ) && ( PRIVATE_STORAGE || AT_LEAST_API_33 ) ) continue;
      // if ( k == PERM_CAMERA && AT_LEAST_API_21 ) continue; // CAMERA only for API >= 21
      if ( ctx.checkSelfPermission( perms[k] ) != PackageManager.PERMISSION_GRANTED ) {
        // TDLog.v("TD cannot perm " + k + ": " + permShortNames[k] + " - canRun() returns false" );
        // TDToast.makeLong( permShortNames[k] + " is needed to run. Bye.");
        return false;
      }
    }
    return PRIVATE_STORAGE || canManageExternalStorage( ctx );
  }

  public static void setButtonBackground( Button btn, BitmapDrawable drawable ) { btn.setBackground( drawable ); }
  public static void setSeekBarBackground( SeekBar btn, BitmapDrawable drawable ) { btn.setBackground( drawable ); }
  
  // /** check whether the running app has the needed permissions
  //  * @return 0 ok
  //  *         -1 missing some necessary permission
  //  *         > 0 if missing non-necessary permission
  //  */
  // static int checkPermissions( Context ctx )
  // {
  //   // TDLog.v( "PERM check permissions" );
  //   int k;
  //   for ( k=0; k<NR_PERMS_D; ++k ) {
  //     if ( k == PERM_BT_CONNECT && BELOW_API_31 ) {
  //       // nothing: res = PackageManager.PERMISSION_GRANTED; // API-31
  //     } else if ( k == PERM_BT_SCAN && BELOW_API_31 ) {
  //       // nothing: res = PackageManager.PERMISSION_GRANTED; // API-31
  //     } else {
  //       if ( ! hasCallingOrPermission( ctx, perms[k] ) ) {
  //         // TDLog.v("PERM Check permission " + permNames[k] + " not granted ");
  //         // TDToast.make( mActivity, "TopoDroid must have " + perms[k] );
  //         return -1;
  //       }
  //     }
  //   }
  //   int flag = 1;
  //   for ( ; k<NR_PERMS_D; ++k ) {
  //     if ( k == PERM_CAMERA /* && AT_LEAST_API_21 */ ) { // CAMERA only for API >= 21 min sdk is 21
  //       if ( ! hasCallingOrPermission( ctx, perms[k] ) ) {
  //         // TDLog.v("PERM Check permission " + permNames[k] + " not granted ");
  //         // TDToast.make( mActivity, "TopoDroid may need " + perms[k] );
  //         ret += flag;
  //       }
  //     }
  //     flag *= 2;
  //   }
  //   // TDLog.v("PERM Check permission returns " + ret );
  //   return ret;
  //   return 0;
  // }

  /** @return true if coarse location access is granted
   * @param ctx  context
   */
  public static boolean checkCoarseLocation( Context ctx )
  {
    return hasCallingOrPermission( ctx, android.Manifest.permission.ACCESS_FINE_LOCATION ) || hasCallingOrPermission( ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION );
  }

  /** @return true if location access is granted
   * @param ctx  context
   */
  public static boolean checkLocation( Context ctx )
  {
    return hasLocationPermission( ctx ) && hasGpsFeature( ctx );
  }

  /* TODO
     1) Split hardware from permission. [DONE]
        checkCamera() and checkAudio() currently combine both checks, so a denied permission looks like missing hardware.
        Keep the feature checks separate and test the permission live at the point of use.
     2) Request lazily, with a result handler. 
        [CAMERA: ShotWindow DONE (askPhotoComment);
        other CAMERA entry points 
        AUDIO TODO]
        Use dedicated codes in TDRequest, REQUEST_CAMERA_PERMISSION ans REQUEST_AUDIO_PERMISSION,
        and an onRequestPermissionsResult that resumes the action on grant and points to app settings after a permanent denial.
     3) Find the entry points first. [DONE]
        Photo capture and audio recording from the shot, plot and sensor windows will each need their own request path.
        REQUEST_CAMERA_PERMISSION: DrawingWindow ShotWindow DrawingLineSectionDialog ShotNewDialog
        REQUEST_AUDIO_PERMISSION:  DrawingWindow ShotWindow
     4) Then drop them from createPermissions(). [TODO]
        Only once every entry point asks for itself, remove them from the startup request loop, as with location.
        Check how MainWindow uses the returned not_granted count before changing it.
   */

  // --- camera
  /** @return true if the device has a (rear, autofocus) camera */
  static boolean hasCameraFeature( Context ctx )
  {
    PackageManager pm = ctx.getPackageManager();
    return pm.hasSystemFeature( PackageManager.FEATURE_CAMERA )
        && pm.hasSystemFeature( PackageManager.FEATURE_CAMERA_AUTOFOCUS );
  }
  
  /** @return true if the app holds the CAMERA permission (checked live) */
  static boolean hasCameraPermission( Context ctx )
  {
    return hasPermission( ctx, android.Manifest.permission.CAMERA );
  }
  
  /** @return true if camera is granted
   * @param ctx  context
   */
  public static boolean checkCamera( Context ctx )
  {
    return hasCameraFeature( ctx ) && hasCameraPermission( ctx );
  }
  
  // --- microphone
  static boolean hasAudioFeature( Context ctx )
  {
    return ctx.getPackageManager().hasSystemFeature( PackageManager.FEATURE_MICROPHONE );
  }
  
  /** @return true if the app holds the AUDIO permission (checked live) */
  static boolean hasAudioPermission( Context ctx )
  {
    return hasPermission( ctx, android.Manifest.permission.RECORD_AUDIO );
  }
  
  /** @return true if device has audio (microphone) and audio is granted
   * @param ctx  context
   */
  public static boolean checkAudio( Context ctx )
  {
    return hasAudioFeature( ctx ) && hasAudioPermission( ctx );
    // return hasCallingOrPermission( ctx, android.Manifest.permission.RECORD_AUDIO )
    //     && ctx.getPackageManager().hasSystemFeature( PackageManager.FEATURE_MICROPHONE );
  }


  /** @return true if the display is multitouch
   * @param ctx  context
   */
  public static boolean checkMultitouch( Context ctx )
  {
    // TDLog.v("PERM Check multitouch ");
    return ctx.getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
  }

  /** @return true if bluetooth is granted
   * @param ctx  context
   */
  public static boolean checkBluetooth( Context ctx )
  {
    // TDLog.v("PERM Check bluetooth ");
    if ( PERM_BT_CONNECT < 0 || BELOW_API_31 ) {
      return hasCallingOrPermission( ctx, android.Manifest.permission.BLUETOOTH ) 
        && ctx.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH );
    } // API-31
    if ( PERM_BT_SCAN < 0 || BELOW_API_31 ) {
      return hasCallingOrPermission( ctx, android.Manifest.permission.BLUETOOTH ) 
        && ctx.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH );
        // TODO FINE_LOCATION
    } // API-31
    return hasCallingOrPermission( ctx, android.Manifest.permission.BLUETOOTH ) 
        && hasCallingOrPermission( ctx, android.Manifest.permission.BLUETOOTH_CONNECT )
        && ctx.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH );
  }

  /** @return true if internet access is granted
   * @param ctx  context
   */
  public static boolean checkInternet( Context ctx )
  {
    // TDLog.v("PERM Check internet ");
    return hasCallingOrPermission( ctx, android.Manifest.permission.INTERNET );
  }

  /** @return true if bluetooth LE is available
   * @param ctx  context
   * @note REQUIRES API-18
   */
  static boolean checkBluetoothLE( Context ctx )
  {
    // if ( BELOW_API_18 ) return false;
    return ctx.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE );
  }

  // orientation lock is also in TopoDroidApp 
  //
  // static void lockOrientation( Activity act )
  // {
  //   Display d = act.getWindowManager().getDefaultDisplay();
  //   int r = d.getRotation();
  //   int h, w;
  //   if ( BELOW_API_13 ) 
  //     h = d.getHeight();
  //     w = d.getWidth();
  //   } else {
  //     Point s = new Point();
  //     d.getSize( s );
  //     h = s.y;
  //     w = s.x;
  //   }
  //   switch ( r ) {
  //     case Surface.ROTATION_90:
  //       if ( w > h ) {
  //         act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
  //       } else {
  //         act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT );
  //       }
  //       break;
  //     case Surface.ROTATION_180:
  //       if ( h > w ) {
  //         act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT );
  //       } else {
  //         act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE );
  //       }
  //       break;
  //     case Surface.ROTATION_270:
  //       if ( w > h ) {
  //         act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE );
  //       } else {
  //         act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
  //       }
  //       break;
  //     default:
  //       if ( h > w ) {
  //         act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
  //       } else {
  //         act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
  //       }
  //       break;
  //   }
  // }

  // static void unlockOrientation( Activity act ) { act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED ); }

  // static void lockOrientationPortrait( Activity act ) { act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); }

  // static void lockOrientationLandscape( Activity act ) { act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ); }


  public static void setScreenOrientation( Activity act )
  {
    if ( act == null ) return;
    switch ( TDSetting.mOrientation ) {
      case 0:
        act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED );
        break;
      case 1:
        act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        break;
      case 2:
        act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
        break;
    }
  }

  public static boolean hasPackage( Context ctx, String package_name )
  {
    PackageManager pm = ctx.getPackageManager();
    try {
      PackageInfo pi = pm.getPackageInfo( package_name, 0 );
      return true;
    } catch ( PackageManager.NameNotFoundException e ) {
      TDLog.e( "TD android " + e.getMessage() );
    }
    return false;
  }
  

  /** @return a flag of the installed GPS apps that are supported
   * @param ctx   app context
   */
  public static int getImportPointFlag( Context ctx )
  {
    int ret = 0;
    PackageManager pm = ctx.getPackageManager();
    try {
      PackageInfo pi = pm.getPackageInfo( "gr.stasta.mobiletopographer", 0 );
      ret |= FixedActivity.FLAG_MOBILE_TOPOGRAPHER;
    } catch ( PackageManager.NameNotFoundException e ) {
      // TDLog.e( "TD android " + e.getMessage() );
    }
    try {
      PackageInfo pi = pm.getPackageInfo( "nz.ac.elec.gpsapp", 0 );
      ret |= FixedActivity.FLAG_GPS_POSITION;
    } catch ( PackageManager.NameNotFoundException e ) {
      // TDLog.e( "TD android " + e.getMessage() );
    }
    try {
      PackageInfo pi = pm.getPackageInfo( "com.doitintuitively.gpxrecorder", 0 );
      ret |= FixedActivity.FLAG_GPX_RECORDER;
    } catch ( PackageManager.NameNotFoundException e ) {
      // TDLog.e( "TD android " + e.getMessage() );
    }
    try {
      PackageInfo pi = pm.getPackageInfo( "com.android.gpstest", 0 );
      ret |= FixedActivity.FLAG_GPS_TEST;
    } catch ( PackageManager.NameNotFoundException e ) {
      // TDLog.e( "TD android " + e.getMessage() );
    }
    try {
      PackageInfo pi = pm.getPackageInfo( "eu.basicairdata.graziano.gpslogger", 0 );
      ret |= FixedActivity.FLAG_GPS_LOGGER;
    } catch ( PackageManager.NameNotFoundException e ) {
      // TDLog.e( "TD android " + e.getMessage() );
    }
    try {
      PackageInfo pi = pm.getPackageInfo( "sk.grecnar.gpspoint", 0 );
      ret |= FixedActivity.FLAG_GPS_POINT;
    } catch ( PackageManager.NameNotFoundException e ) {
      // TDLog.e( "TD android " + e.getMessage() );
    }
    TDLog.v("GPS apps support flag " + ret );
    return ret;
  }

  /** @return an open-document intent - requires API_19 - 20230118 new method
   * @param index  index of mime type - use 0xffffff to open a folder
   * @note folder is used for bulk-import of surveys
   */
  public static Intent getOpenDocumentIntent( int index )
  {
    TDLog.v("TD android open index " + index );
    Intent intent = null;
    if ( index == TDConst.FOLDER ) {
      intent = new Intent( Intent.ACTION_OPEN_DOCUMENT_TREE ); // API_19
      // intent.setType( "vnd.android.document/directory" );
    } else {
      intent = new Intent( Intent.ACTION_OPEN_DOCUMENT ); // API_19
      intent.setType( TDConst.getMimeType( index ) );
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); // API_19
    }
    return intent;
  }

  /** @return a create-document intent - requires API_19 - 20230118 new method
   * @param index  index of mime type
   */
  public static Intent getCreateDocumentIntent( int index )
  {
    Intent intent = new Intent( Intent.ACTION_CREATE_DOCUMENT ); // API_19
    intent.setType(  TDConst.getMimeType( index ) );
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); // API_19
    return intent;
  }

  /** @return true if the Android is online
   * @param ctx  context
   */
  public static boolean isOnline( Context ctx )
  {
    ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService( Context.CONNECTIVITY_SERVICE );
    if ( cm == null ) return false;
    NetworkInfo net_info = cm.getActiveNetworkInfo();
    return ( net_info != null && net_info.isConnectedOrConnecting() );
  }

  /** @return true if the app has LOCATION permission
   * @param ctx context
   *
   * This checks GPS only.
   * On API 28+, LocationManager.isLocationEnabled() is more accurate for the pre-31 case,
   * since location services can be on with only network location.
   */
  public static boolean hasLocation( Context ctx )
  {
    LocationManager lm = (LocationManager)ctx.getSystemService( Context.LOCATION_SERVICE );
    if ( lm == null ) return false;
    boolean gps_enabled = lm.isProviderEnabled( LocationManager.GPS_PROVIDER );
    // boolean network_enabled = lm.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
    return gps_enabled;
  }

  /** @return true if location services are on (needed for BLE scan below API-31) 
   */
  public static boolean isLocationEnabled( Context ctx )
  {
    LocationManager lm = (LocationManager)ctx.getSystemService( Context.LOCATION_SERVICE );
    if ( lm == null ) return false;
    if ( Build.VERSION.SDK_INT >= 28 ) return lm.isLocationEnabled();
    return lm.isProviderEnabled( LocationManager.GPS_PROVIDER )
        || lm.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
  }

  /** @return true if the device has GPS hardware
   * @param ctx context
   */
  static boolean hasGpsFeature( Context ctx )
  {
    PackageManager pm = ctx.getPackageManager();
    return pm.hasSystemFeature( PackageManager.FEATURE_LOCATION ) && pm.hasSystemFeature( PackageManager.FEATURE_LOCATION_GPS );
  }

  /** @return true if the app has the fine-location permission (checked live) */
  static boolean hasLocationPermission( Context ctx )
  {
    // return hasCallingOrPermission( ctx, android.Manifest.permission.ACCESS_FINE_LOCATION );
    return hasPermission( ctx, android.Manifest.permission.ACCESS_FINE_LOCATION );
  }

  /** post a runnable on the queue of the main looper thread
   * @param runnable  runnable to run on the main thread
   */
  public static void runOnMainThread( Runnable runnable )
  {
    if ( Looper.myLooper() == Looper.getMainLooper() ) { // MM inserted this if-part
      runnable.run();
    } else {
      Handler handler = new Handler( Looper.getMainLooper() );
      handler.post( runnable );
    }
  }

  public static void toast( final String msg )
  {
    runOnMainThread( new Runnable() { public void run() { TDToast.make( msg ); } } );
  }

  public static void toast( final int res_id )
  {
    runOnMainThread( new Runnable() { public void run() { TDToast.make( res_id ); } } );
  }

  public static void toastBad( final String msg )
  {
    runOnMainThread( new Runnable() { public void run() { TDToast.makeBad( msg ); } } );
  }

  public static void toastBad( final int res_id )
  {
    runOnMainThread( new Runnable() { public void run() { TDToast.makeBad( res_id ); } } );
  }

  public static void toastWarn( final String msg )
  {
    runOnMainThread( new Runnable() { public void run() { TDToast.makeWarn( msg ); } } );
  }

  public static void toastWarn( final int res_id )
  {
    runOnMainThread( new Runnable() { public void run() { TDToast.makeWarn( res_id ); } } );
  }


  /** check if arch arm64_v8a is supported
   */
  public static boolean isArm64_v8aSupported()
  {
    for ( String abi : Build.SUPPORTED_ABIS ) {
      if ( abi.equals("arm64-v8a") ) return true;
    }
    return false;
  }

  
  /** @return the levl of AI support
   * 2 - E4B on GPU
   * 1 - E4B on CPU
   * 0 - no support
   */
  public static int getAIsupport( Context ctx )
  {
    ActivityManager am = (ActivityManager)ctx.getSystemService( Context.ACTIVITY_SERVICE );
    MemoryInfo mi = new MemoryInfo();
    am.getMemoryInfo( mi );
    double ram = mi.totalMem / (double)(1024 * 1024 * 1024 ); // [GB]
    int openGL_version = am.getDeviceConfigurationInfo().reqGlEsVersion;
    boolean canGPU = openGL_version >= 0x30001;
    TDLog.v("RAM " + ram + " OpenGL version " + String.format("%x", openGL_version ) );

    if ( ram > 7.5 && canGPU ) return 2;
    if ( ram > 5.5 ) return 1;
    return 0;
  }
    
}
  
