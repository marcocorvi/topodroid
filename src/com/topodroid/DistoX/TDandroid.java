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
package com.topodroid.DistoX;

// import android.util.Log;
 
import android.content.SharedPreferences.Editor;

// import android.os.Build.VERSION_CODES;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.Context;
import java.lang.reflect.Method;
import android.os.Build;
import android.content.pm.PackageManager;
/* FIXME-23 */
import android.os.StrictMode;
/* */
/* FIXME-16
import android.os.StrictMode;
/* */
/* FIXME-8 nothing */

import android.hardware.Sensor;
import android.widget.Button;
import android.widget.SeekBar;
// import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
// import android.graphics.Point;
// import android.view.Display;
// import android.view.Surface;

class TDandroid
{
  /** permissions string codes
   */ 
  static final String[] perms = {
      android.Manifest.permission.BLUETOOTH,            // Bluetooth permissions are normal - no need to request at runtime
      android.Manifest.permission.BLUETOOTH_ADMIN,
      // android.Manifest.permission.INTERNET,
      android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
      // android.Manifest.permission.READ_EXTERNAL_STORAGE,
      android.Manifest.permission.ACCESS_FINE_LOCATION,
      android.Manifest.permission.CAMERA,
      android.Manifest.permission.RECORD_AUDIO
  };
  static final String[] permNames = {
      "BLUETOOTH", 
      "BLUETOOTH_ADMIN",
      // "INTERNET",
      "WRITE_EXTERNAL_STORAGE",
      // "READ_EXTERNAL_STORAGE",
      "ACCESS_FINE_LOCATION",
      "CAMERA",
      "RECORD_AUDIO"
  };

  static final int NR_PERMS_D = 3;
  static final int NR_PERMS   = 6;

  /** app specific code - for callback in MainWindow
   */
  static final int REQUEST_PERMISSIONS = 1;

  // private static boolean MustRestart = false; // whether need to restart app
  // static boolean[] GrantedPermission = { false, false, false, false, false, false };
/* FIXME-23 */
  static final int TEMPERATURE      = Sensor.TYPE_AMBIENT_TEMPERATURE; // REQUIRES API-14

  static final int TITLE_NORMAL     = 0xff6699ff; // FIXED_BLUE same as in values/styles.xml
  static final int TITLE_NORMAL2    = 0xff99ccff; 
  static final int TITLE_BACKSHOT   = 0xff0099cc; // DARK BLUE
  static final int TITLE_BACKSIGHT  = 0xffb66dff; // VIOLET
  static final int TITLE_TRIPOD     = 0xffff6db6; // PINK
  static final int TITLE_TOPOROBOT  = 0xffdbd100; // ORANGE
  static final int TITLE_ANOMALY    = 0xffff3333; // BRIGHT RED

  static void applyEditor( Editor editor )
  {
    editor.apply(); 
  }

  static boolean MustRestart = false; // whether need to restart app
  static boolean[] GrantedPermission = { false, false, false, false, false, false };

  static void createPermissions( Context context, Activity activity )
  {
    // TDLog.Log( LOG_PERM, "create permissions" );
    MustRestart = false;
    if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) return;

    for ( int k=0; k<NR_PERMS; ++k ) { // check whether the app has the six permissions
      // Log.v("DistoX-PERM", "Create permission " + permNames[k] );
      GrantedPermission[k] = ( context.checkSelfPermission( perms[k] ) == PackageManager.PERMISSION_GRANTED );
      if ( ! GrantedPermission[k] && k < NR_PERMS_D ) MustRestart = true;
    }
    // Log.v("DistoXX", "FC must restart " + MustRestart );
    if ( MustRestart ) { // if a permission has not been granted request it
      activity.requestPermissions( perms, REQUEST_PERMISSIONS );
      android.os.Process.killProcess( android.os.Process.myPid() );
      System.exit( 1 );
    }
  }

  static boolean checkStrictMode()
  {
    boolean ret = true;
    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) { // build version 24
      try {
        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposed");
        m.invoke( null );
      } catch ( Exception e ) { ret = false; }
    }
    return ret;
  }

  static void setButtonBackground( Button btn, BitmapDrawable drawable ) { btn.setBackground( drawable ); }
  static void setSeekBarBackground( SeekBar btn, BitmapDrawable drawable ) { btn.setBackground( drawable ); }
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
      // Log.v("DistoXX", "FC perm " + k + " granted " + GrantedPermission[k] );
      if ( ! GrantedPermission[k] ) MustRestart = true;
    }
    // Log.v("DistoXX", "FC must restart " + MustRestart );
    if ( MustRestart ) { // if a permission has not been granted request it
      // nothing
    }
  }

  static boolean checkStrictMode() { return true; }

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
      // Log.v("DistoXX", "FC perm " + k + " granted " + GrantedPermission[k] );
      if ( ! GrantedPermission[k] ) MustRestart = true;
    }
    // Log.v("DistoXX", "FC must restart " + MustRestart );
    if ( MustRestart ) { // if a permission has not been granted request it
      // nothing
    }
  }

  static boolean checkStrictMode() { return true; }

  static void setButtonBackground( Button btn, BitmapDrawable drawable ) { btn.setBackgroundDrawable( drawable ); }
  static void setSeekBarBackground( SeekBar btn, BitmapDrawable drawable ) { btn.setBackgroundDrawable( drawable ); }
/* */


  /** check whether the running app has the needed permissions
   * @return 0 ok
   *         -1 missing some necessary permission
   *         >0 missing some complementary permssion (flag):
   *            1 FINE_LOCATION
   *            2 CAMERA
   *            4 AUDIO
   */
  static int checkPermissions( Context context )
  {
    // TDLog.Log( LOG_PERM, "check permissions" );
    int k;
    for ( k=0; k<NR_PERMS_D; ++k ) {
      // Log.v("DistoX-PERM", "Check permission " + permNames[k] );
      int res = context.checkCallingOrSelfPermission( perms[k] );
      if ( res != PackageManager.PERMISSION_GRANTED ) {
        // TDToast.make( mActivity, "TopoDroid must have " + perms[k] );
	return -1;
      }
    }
    int ret = 0;
    int flag = 1;
    for ( ; k<NR_PERMS; ++k ) {
      // Log.v("DistoX-PERM", "Check permission " + permNames[k] );
      int res = context.checkCallingOrSelfPermission( perms[k] );
      if ( res != PackageManager.PERMISSION_GRANTED ) {
        // TDToast.make( mActivity, "TopoDroid may need " + perms[k] );
	ret += flag;
      }
      flag *= 2;
    }
    // Log.v("DistoX-PERM", "Check permission returns " + ret );
    return ret;
  }

  static boolean checkLocation( Context context )
  {
    // TDLog.Log( LOG_PERM, "check location" );
    // Log.v("DistoX-PERM", "Check location ");
    PackageManager pm = context.getPackageManager();
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
  }

  static boolean checkCamera( Context context )
  {
    // TDLog.Log( LOG_PERM, "check camera" );
    // Log.v("DistoX-PERM", "Check camera ");
    PackageManager pm = context.getPackageManager();
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.CAMERA ) == PackageManager.PERMISSION_GRANTED )
        && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
  }

  static boolean checkMultitouch( Context context )
  {
    // TDLog.Log( LOG_PERM, "check multitouch" );
    // Log.v("DistoX-PERM", "Check multitouch ");
    return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
  }

  static boolean checkMicrophone( Context context )
  {
    // TDLog.Log( LOG_PERM, "check microphone" );
    // Log.v("DistoX-PERM", "Check microphone ");
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED )
        && context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_MICROPHONE );
  }

  static boolean checkBluetooth( Context context )
  {
    // TDLog.Log( LOG_PERM, "check bluetooth" );
    // Log.v("DistoX-PERM", "Check bluetooth ");
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.BLUETOOTH ) == PackageManager.PERMISSION_GRANTED )
        && context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH );
  }

  static boolean checkInternet( Context context )
  {
    // TDLog.Log( LOG_PERM, "check internet" );
    // Log.v("DistoX-PERM", "Check internet ");
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.INTERNET ) == PackageManager.PERMISSION_GRANTED );
  }

  // REQUIRES API-18
  static boolean checkBluetoothLE( Context context )
  {
    return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE );
  }

  // static void lockOrientation( Activity act )
  // {
  //   Display d = act.getWindowManager().getDefaultDisplay();
  //   int r = d.getRotation();
  //   int h, w;
  //   if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2 ) {
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

  // static void lockOrientationiPortrait( Activity act ) { act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ); }

  // static void lockOrientationiLandscape( Activity act ) { act.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ); }


  static void setScreenOrientation( Activity act )
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
  
