/* @file FeatureChecker.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid feature checker
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Build;
// import android.os.Build.VERSION_CODES;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
// import android.content.pm.FeatureInfo;

// import android.util.Log;

class FeatureChecker
{
  // boolean hasBluetooth       = false;
  // boolean hasCamera          = false;
  // boolean hasCameraAutofocus = false;
  // boolean hasLocation        = false;
  // boolean hasLocationGps     = false;
  // boolean hasMicrophone      = false;
  // boolean hasTouchscreen     = false;
  // boolean hasScreenPortrait  = false;

  // FeatureChecker()
  // {
  //   FeatureInfo[] features = PackageManager.getSystemAvailableFeatures();
  //   for ( FeatureInfo f : features ) {
  //     if ( f.name != null ) {
  //       if ( f.name.equals("android.hardware.bluetooth") ) {
  //         hasBluetooth = true;
  //       } else if ( f.name.equals("android.hardware.camera") ) {
  //         hasCamera = true;
  //       } else if ( f.name.equals("android.hardware.camera.autofocus") ) {
  //         hasCameraAutofocus = true;
  //       } else if ( f.name.equals("android.hardware.location") ) {
  //         hasLocation = true;
  //       } else if ( f.name.equals("android.hardware.location.gps") ) {
  //         hasLocationGps = true;
  //       } else if ( f.name.equals("android.hardware.microphone") ) {
  //         hasMicrophone = true;
  //       } else if ( f.name.equals("android.hardware.touchscreen") ) {
  //         hasTouchscreen = true;
  //       } else if ( f.name.equals("android.hardware.screen.portrait") ) {
  //         hasScreenPortrait = true;
  //       }
  //     }
  //   }
  // }

  /** permissions string codes
   */ 
  static private String perms[] = {
      android.Manifest.permission.BLUETOOTH,            // Bluetooth permissions are normal - no need to request at runtime
      android.Manifest.permission.BLUETOOTH_ADMIN,
      android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
      // android.Manifest.permission.READ_EXTERNAL_STORAGE,
      android.Manifest.permission.ACCESS_FINE_LOCATION,
      android.Manifest.permission.CAMERA,
      android.Manifest.permission.RECORD_AUDIO
  };

  /** app specific code - for callback in MainWindow
   */
  static final int REQUEST_PERMISSIONS = 1;

  static boolean MustRestart = false; // whether need to restart app
  static boolean GrantedPermission[] = { false, false, false, false, false, false };

  static void createPermissions( Context context, Activity activity )
  {
    MustRestart = false;
    if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) return;
    for ( int k=0; k<6; ++k ) {
      GrantedPermission[k] = ( context.checkSelfPermission( perms[k] ) == PackageManager.PERMISSION_GRANTED );
      // Log.v("DistoXX", "FC perm " + k + " granted " + GrantedPermission[k] );
      if ( ! GrantedPermission[k] ) MustRestart = true;
    }
    // Log.v("DistoXX", "FC must restart " + MustRestart );
    if ( MustRestart ) {
      activity.requestPermissions( perms, REQUEST_PERMISSIONS );
    }
  }

  /** check whether the running app has the needed permissions
   * @return 0 ok
   *         -1 missing some necessary permission
   *         >0 missing some complementary permssion (flag):
   */
  static int checkPermissions( Context context )
  {
    int k;
    for ( k=0; k<3; ++k ) {
      int res = context.checkCallingOrSelfPermission( perms[k] );
      if ( res != PackageManager.PERMISSION_GRANTED ) {
        // TDToast.make( mActivity, "TopoDroid must have " + perms[k] );
	return -1;
      }
    }
    int ret = 0;
    int flag = 1;
    for ( ; k<6; ++k ) {
      int res = context.checkCallingOrSelfPermission( perms[k] );
      if ( res != PackageManager.PERMISSION_GRANTED ) {
        // TDToast.make( mActivity, "TopoDroid may need " + perms[k] );
	ret += flag;
      }
      flag *= 2;
    }
    return ret;
  }

  static boolean checkLocation( Context context )
  {
    PackageManager pm = context.getPackageManager();
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
  }

  static boolean checkCamera( Context context )
  {
    PackageManager pm = context.getPackageManager();
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.CAMERA ) == PackageManager.PERMISSION_GRANTED )
        && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
  }

  static boolean checkMultitouch( Context context )
  {
    return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
  }

  static boolean checkMicrophone( Context context )
  {
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED )
        && context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_MICROPHONE );
  }

  static boolean checkBluetooth( Context context )
  {
    return ( context.checkCallingOrSelfPermission( android.Manifest.permission.BLUETOOTH ) == PackageManager.PERMISSION_GRANTED )
        && context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH );
  }
}
