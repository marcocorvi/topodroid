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
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDVersion;
import com.topodroid.prefs.TDSetting;

import android.content.SharedPreferences.Editor;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.Context;
import android.content.Intent;

// import java.lang.reflect.Method;

import android.os.Build;
import android.os.Environment;

import android.provider.Settings;
import android.net.Uri;

// import android.os.Build.VERSION_CODES;
import android.content.pm.PackageManager;
// import android.content.pm.PackageManager.NameNotFoundException;
// import android.content.pm.PackageInfo;

import android.hardware.Sensor;
import android.widget.Button;
import android.widget.SeekBar;
// import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
// import android.graphics.Point;
// import android.view.Display;
// import android.view.Surface;

public class TDandroid
{
  // final static public boolean BELOW_API_13 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2 );
  // final static public boolean BELOW_API_15 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 );
  final static public boolean BELOW_API_18 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 );
  final static public boolean BELOW_API_19 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT );
  final static public boolean BELOW_API_21 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP );
  final static public boolean BELOW_API_23 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.M );
  final static public boolean BELOW_API_24 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.N );
  final static public boolean BELOW_API_26 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.O );
  final static public boolean BELOW_API_29 = ( Build.VERSION.SDK_INT < Build.VERSION_CODES.Q );
  final static public boolean BELOW_API_30 = ( Build.VERSION.SDK_INT < 30 ) ; // Build.VERSION_CODES.R );
  final static public boolean BELOW_API_31 = ( Build.VERSION.SDK_INT < 31 ) ; // Build.VERSION_CODES.S );

  // final static public boolean ABOVE_API_16 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLYBEAN );
  final static public boolean ABOVE_API_21 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP );
  final static public boolean ABOVE_API_24 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.N ); // Android-6 Nougat
  final static public boolean ABOVE_API_26 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.O ); // Android-8 Oreo
  final static public boolean ABOVE_API_29 = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.Q ); // Android-10

  // final static public boolean AT_LEAST_API_15 = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWITCH_MR1 );
  final static public boolean AT_LEAST_API_21 = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP );
  final static public boolean AT_LEAST_API_23 = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M );
  final static public boolean AT_LEAST_API_24 = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ); // Android-6 Nougat
  final static public boolean AT_LEAST_API_31 = ( Build.VERSION.SDK_INT >= 31 ); 

  /** permissions string codes
   */ 
  static final String[] perms = {
      android.Manifest.permission.BLUETOOTH,            // Bluetooth permissions are normal - no need to request at runtime
      android.Manifest.permission.BLUETOOTH_ADMIN,
      android.Manifest.permission.BLUETOOTH_CONNECT, // API-31
      // android.Manifest.permission.INTERNET,
      // android.Manifest.permission.MANAGE_EXTERNAL_STORAGE, // will always be denied from API-30
      android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
      android.Manifest.permission.READ_EXTERNAL_STORAGE,
      android.Manifest.permission.ACCESS_FINE_LOCATION,
      android.Manifest.permission.CAMERA,
      android.Manifest.permission.RECORD_AUDIO
      // android.Manifest.permission.ACCESS_MEDIA_LOCATION
  };

  static final String[] permNames = {
      "BLUETOOTH", 
      "BLUETOOTH_ADMIN",
      "BLUETOOTH_CONNECT", // API-31
      // "MANAGE_EXTERNAL_STORAGE", // API-30
      "WRITE_EXTERNAL_STORAGE",
      "READ_EXTERNAL_STORAGE",
      // "INTERNET",
      "ACCESS_FINE_LOCATION",
      "CAMERA",
      "RECORD_AUDIO"
      // "ACCESS_MEDIA_LOCATION"
  };

  static final String[] permShortNames = {
      "BLUETOOTH", 
      "BT_ADMIN",
      "BT_CONNECT", // API-31
      // "MANAGE_FILE", // API-30
      "WRITE_FILE",
      "READ_FILE",
      // "INTERNET",
      "LOCATION",
      "CAMERA",
      "AUDIO"
      // "ACCESS_MEDIA_LOCATION"
  };

  // static final String mPermissionManageExternalStorage = android.Manifest.permission.MANAGE_EXTERNAL_STORAGE; // API-30
  static final String mPermissionManageExternalStorage = "android.permission.MANAGE_EXTERNAL_STORAGE";

  static final int NR_PERMS_D = 5; // 5 API-31 
  static final int NR_PERMS   = NR_PERMS_D + 3;

  // private static final int PERM_BT         = 0;
  // private static final int PERM_BT_ADMIN   = 1;
  private static final int PERM_BT_CONNECT  = 2; // 2 API-31, use -1 to fail test and skip

  // private static final int PERM_LOCATION   = NR_PERMS_D + 0;
  private static final int PERM_CAMERA     = NR_PERMS_D + 1; 
  // private static final int PERM_AUDIO      = NR_PERMS_D + 2;
  // private static final int PERM_MEDIA      = NR_PERMS_D + 3;

  /** app specific code - for callback in MainWindow
   */
  static final int REQUEST_PERMISSIONS = 1;

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

  /** apply the changes stored in the editor
   * @param editor   preferences editor
   * @return true if successful
   */
  public static boolean applyEditor( Editor editor )
  {
    // editor.apply(); 
    return editor.commit(); 
  }

  static boolean MustRestart = false; // whether need to restart app
  static boolean[] GrantedPermission = { false, false, false, false, false, false, false, false }; // size = NR_PERMS

  // number of times permissions are requested

  /** @return the number of permissions that are not granted
   * @param context  context
   * @param activity activity
   * @param time     time that the permissions are requested
   */
  static int createPermissions( Context context, Activity activity, int time )
  {
    // TDLog.Log( LOG_PERM, "create permissions" );

    MustRestart = false;
    if ( BELOW_API_23 ) {
      TDLog.v("PERM " + "create perms: below API-23 - return " );
      return 0;
    }

    // StringBuilder sb = new StringBuilder();
    // sb.append("Not granted" );

    int not_granted = 0;
    for ( int k=0; k<NR_PERMS; ++k ) { // check whether the app has the six permissions
      // TDLog.v("PERM " + "Create permission " + permNames[k] );

      if ( k == PERM_BT_CONNECT && BELOW_API_31 ) { // BT_CONNECT only for API >= 31 - API-31
        GrantedPermission[k] = true;
        continue;
      } 

      if ( k == PERM_CAMERA && BELOW_API_21 ) { // CAMERA only for API >= 21
        // GrantedPermission[k] = false;
        continue;
      }

      GrantedPermission[k] = ( context.checkSelfPermission( perms[k] ) == PackageManager.PERMISSION_GRANTED );
      if ( ! GrantedPermission[k] ) {
        TDLog.v( "PERM " + permNames[k] + " not granted ");
        if ( time > 1 ) {
          activity.requestPermissions( new String[] { perms[k] }, REQUEST_PERMISSIONS );
          GrantedPermission[k] = ( context.checkSelfPermission( perms[k] ) == PackageManager.PERMISSION_GRANTED );
          if ( ! GrantedPermission[k] ) ++not_granted;
        } else {
          ++not_granted;
        }
        // if ( k < NR_PERMS_D ) MustRestart = true;
      // } else {
      //   // TDLog.v( "Perm " + permNames[k] + " granted ");
      }
    }
    TDLog.v("PERM " + "create perms " + time + ": not granted " + not_granted + " / " + NR_PERMS );

    if ( not_granted > 0 && time < 3 ) {
      // TDLog.v( "request perms time " + time );
      // String[] ask_perms = new String[ not_granted ];
      // int kk = 0;
      // for ( int k = 0; k < NR_PERMS; ++k ) if ( ! GrantedPermission[k] ) ask_perms[kk++] = perms[k];
      activity.requestPermissions( perms, REQUEST_PERMISSIONS );
    }
    
    // TDLog.v("PERM " + "FC must restart " + MustRestart + " " + sb.toString() );
    // if ( MustRestart ) { // if a permission has not been granted request it
    //   // TDToast.make( "TopoDroid cannot do anything useful without" + sb.toString() );
    //   activity.requestPermissions( perms, REQUEST_PERMISSIONS );
    //   // TDLog.v("PERM " + "exit 1");
    //   android.os.Process.killProcess( android.os.Process.myPid() );
    //   System.exit( 1 );
    // }

    return not_granted;
  }

  /** check if the app can access external storage
   * @param context app context
   * @return true if the app can access external storage
   */
  static boolean canManageExternalStorage( Context context )
  {
    if ( BELOW_API_30 ) return true;
    // return ( context.checkSelfPermission( mPermissionManageExternalStorage ) == PackageManager.PERMISSION_GRANTED );
    return Environment.isExternalStorageManager();
  }

  /** request external storage access 
   * @param context    context
   * @param activity   activity
   * #see https://stackoverflow.com/questions/64250814/how-to-obtain-manage-external-storage-permission
   *
   * TODO TO TRY
   * if called from onCreate could use startActivityForResult with requestCode >= 0
   * but I am not sure Settings return a result 
   */
  static void requestExternalStorage(  Context context, Activity activity )
  {
    Intent intent = new Intent();
    intent.setAction( Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION );
    Uri uri = Uri.fromParts( "package", context.getPackageName(), null );
    intent.setData( uri );
    try {
      activity.startActivity( intent );
    } catch ( ActivityNotFoundException e ) {
      TDLog.Error("Error " + e.getMessage() );
    }
  }

  /** check if the app has the minimal permissions
   * @param context    context
   * @param activity   activity
   * @return true is the app can run
   */
  static boolean canRun( Context context, Activity activity )
  {
    if ( BELOW_API_23 ) return true;
    for ( int k=0; k<NR_PERMS_D; ++k ) { // check whether the app has the six permissions
      if ( k == PERM_BT_CONNECT && BELOW_API_31 ) continue; // BT_CONNECT only for API >= 31 - API-31
      // if ( k == PERM_CAMERA && AT_LEAST_API_21 ) continue; // CAMERA only for API >= 21
      if ( context.checkSelfPermission( perms[k] ) != PackageManager.PERMISSION_GRANTED ) {
        // TDLog.v("TD cannot run because of " + k + ": " + perms[k] );
        TDToast.makeLong( permShortNames[k] + " is needed to run. Bye.");
        return false;
      }
    }
    // return true;
    return canManageExternalStorage( context );
  }

  public static void setButtonBackground( Button btn, BitmapDrawable drawable ) { btn.setBackground( drawable ); }
  public static void setSeekBarBackground( SeekBar btn, BitmapDrawable drawable ) { btn.setBackground( drawable ); }
/* */
  
/* FIXME-16
  static final int TEMPERATURE      = Sensor.TYPE_AMBIENT_TEMPERATURE;

  static final int TITLE_NORMAL     = 0xff3366cc; // FIXED_BLUE same as in values/styles.xml
  static final int TITLE_NORMAL2    = 0xff6699cc; 
  static final int TITLE_BACKSHOT   = 0xff0066cc; // DARK BLUE
  static final int TITLE_BACKSIGHT  = 0xffb66dff; // VIOLET
  static final int TITLE_TRIPOD     = 0xffff6db6; // PINK
  static final int TITLE_TOPOROBOT  = 0xffdbd100; // ORANGE
  static final int TITLE_ANOMALY    = 0xffff3333; // BRIGHT RED

  static void applyEditor( Editor editor )
  {
    editor.commit();
  }

  private static boolean MustRestart = false; // whether need to restart app
  static boolean[] GrantedPermission = { false, false, false, false, false, false };

  static void createPermissions( Context context, Activity activity )
  {
    // TDLog.Log( LOG_PERM, "create permissions" );
    MustRestart = false;

    for ( int k=0; k<NR_PERMS; ++k ) { // check whether the app has the six permissions
      GrantedPermission[k] = true;
      // TDLog.v( "FC perm " + k + " granted " + GrantedPermission[k] );
      if ( ! GrantedPermission[k] ) MustRestart = true;
    }
    // TDLog.v( "FC must restart " + MustRestart );
    if ( MustRestart ) { // if a permission has not been granted request it
      // nothing
    }
  }

  static void setButtonBackground( Button btn, BitmapDrawable drawable ) { btn.setBackground( drawable ); }
  static void setSeekBarBackground( SeekBar btn, BitmapDrawable drawable ) { btn.setBackground( drawable ); }
/* */
  
/* FIXME-8 
  static final int TEMPERATURE      = Sensor.TYPE_TEMPERATURE;

  static final int TITLE_NORMAL     = 0xff3366cc; // FIXED_BLUE same as in values/styles.xml
  static final int TITLE_NORMAL2    = 0xff6699cc; 
  static final int TITLE_BACKSHOT   = 0xff0066cc; // DARK BLUE
  static final int TITLE_BACKSIGHT  = 0xffb66dff; // VIOLET
  static final int TITLE_TRIPOD     = 0xffff6db6; // PINK
  static final int TITLE_TOPOROBOT  = 0xffdbd100; // ORANGE
  static final int TITLE_ANOMALY    = 0xffff3333; // BRIGHT RED

  static void applyEditor( Editor editor )
  {
    editor.commit();
  }

  private static boolean MustRestart = false; // whether need to restart app
  static boolean[] GrantedPermission = { false, false, false, false, false, false };

  static void createPermissions( Context context, Activity activity )
  {
    // TDLog.Log( LOG_PERM, "create permissions" );
    MustRestart = false;

    for ( int k=0; k<NR_PERMS; ++k ) { // check whether the app has the six permissions
      GrantedPermission[k] = true;
      // TDLog.v( "FC perm " + k + " granted " + GrantedPermission[k] );
      if ( ! GrantedPermission[k] ) MustRestart = true;
    }
    // TDLog.v( "FC must restart " + MustRestart );
    if ( MustRestart ) { // if a permission has not been granted request it
      // nothing
    }
  }

  static void setButtonBackground( Button btn, BitmapDrawable drawable ) { btn.setBackgroundDrawable( drawable ); }
  static void setSeekBarBackground( SeekBar btn, BitmapDrawable drawable ) { btn.setBackgroundDrawable( drawable ); }
/* */


  /** check whether the running app has the needed permissions
   * @return 0 ok
   *         -1 missing some necessary permission
   *         >0 missing some complementary permission (flag):
   *            1 FINE_LOCATION
   *            2 CAMERA
   *            4 AUDIO
   *            8 MEDIA_LOCATION
   */
  static int checkPermissions( Context context )
  {
    // TDLog.Log( LOG_PERM, "check permissions" );
    // TDLog.v( "check permissions" );
    int k;
    for ( k=0; k<NR_PERMS_D; ++k ) {
      if ( k == PERM_BT_CONNECT && BELOW_API_31 ) {
        // nothing: res = PackageManager.PERMISSION_GRANTED; // API-31
      } else {
        int res = context.checkCallingOrSelfPermission( perms[k] );
        if ( res != PackageManager.PERMISSION_GRANTED ) {
          TDLog.v("PERM " + "Check permission " + permNames[k] + " not granted ");
          // TDToast.make( mActivity, "TopoDroid must have " + perms[k] );
          return -1;
        } else {
          // TDLog.v("PERM " + "Check permission " + permNames[k] + " granted ");
        }
      }
    }
    int ret = 0;
    int flag = 1;
    for ( ; k<NR_PERMS; ++k ) {
      // TDLog.v("PERM " + "Check permission " + permNames[k] );
      if ( k == PERM_CAMERA && AT_LEAST_API_21 ) { // CAMERA only for API >= 21
        int res = context.checkCallingOrSelfPermission( perms[k] );
        if ( res != PackageManager.PERMISSION_GRANTED ) {
          // TDLog.v("PERM " + "Check permission " + permNames[k] + " not granted ");
          // TDToast.make( mActivity, "TopoDroid may need " + perms[k] );
          ret += flag;
        } else {
          // TDLog.v("PERM " + "Check permission " + permNames[k] + " granted ");
        }
      }
      flag *= 2;
    }
    // TDLog.v("PERM " + "Check permission returns " + ret );
    return ret;
  }

  /** @return true if location access is granted
   * @return context  context
   */
  public static boolean checkLocation( Context context )
  {
    // TDLog.Log( LOG_PERM, "check location" );
    TDLog.v("PERM " + "Check location ");
    PackageManager pm = context.getPackageManager();
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
  }

  /** @return true if camera is granted
   * @return context  context
   */
  public static boolean checkCamera( Context context )
  {
    // TDLog.Log( LOG_PERM, "check camera" );
    TDLog.v("PERM " + "Check camera ");
    if ( ! AT_LEAST_API_21 ) return false;
    PackageManager pm = context.getPackageManager();
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.CAMERA ) == PackageManager.PERMISSION_GRANTED )
        && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
  }

  /** @return true if the display is multitouch
   * @return context  context
   */
  public static boolean checkMultitouch( Context context )
  {
    // TDLog.Log( LOG_PERM, "check multitouch" );
    TDLog.v("PERM " + "Check multitouch ");
    return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
  }

  /** @return true if audio is granted
   * @return context  context
   */
  public static boolean checkMicrophone( Context context )
  {
    // TDLog.Log( LOG_PERM, "check microphone" );
    TDLog.v("PERM " + "Check microphone ");
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED )
        && context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_MICROPHONE );
  }

  /** @return true if bluetooth is granted
   * @return context  context
   */
  public static boolean checkBluetooth( Context context )
  {
    // TDLog.Log( LOG_PERM, "check bluetooth" );
    TDLog.v("PERM " + "Check bluetooth ");
    if ( PERM_BT_CONNECT < 0 || BELOW_API_31 ) {
      return ( context.checkCallingOrSelfPermission( android.Manifest.permission.BLUETOOTH ) == PackageManager.PERMISSION_GRANTED )
        && context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH );
    } // API-31
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.BLUETOOTH ) == PackageManager.PERMISSION_GRANTED )
        && ( context.checkCallingOrSelfPermission( android.Manifest.permission.BLUETOOTH_CONNECT ) == PackageManager.PERMISSION_GRANTED )
        && context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH );
  }

  /** @return true if internet access is granted
   * @return context  context
   */
  public static boolean checkInternet( Context context )
  {
    // TDLog.Log( LOG_PERM, "check internet" );
    TDLog.v("PERM " + "Check internet ");
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.INTERNET ) == PackageManager.PERMISSION_GRANTED );
  }

  /** @return true if bluetooth LE is available
   * @return context  context
   * @note REQUIRES API-18
   */
  static boolean checkBluetoothLE( Context context )
  {
    if ( BELOW_API_18 ) return false;
    return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE );
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

}
  
