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

import android.content.Context;
import android.content.pm.PackageManager;
// import android.content.pm.FeatureInfo;

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

  /** check whether the running app has the needed permissions
   * @return 0 ok
   *         -1 missing some necessary permission
   *         >0 missing some complementary permssion (flag):
   *            1 FILE_LOCATION
   *            2 CAMERA
   *            4 AUDIO
   */
  static int checkPermissions( Context context )
  {
    String perms[] = {
      android.Manifest.permission.BLUETOOTH,
      android.Manifest.permission.BLUETOOTH_ADMIN,
      android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
      // android.Manifest.permission.READ_EXTERNAL_STORAGE,
      android.Manifest.permission.ACCESS_FINE_LOCATION,
      android.Manifest.permission.CAMERA,
      android.Manifest.permission.RECORD_AUDIO
    };
    int k;
    for ( k=0; k<3; ++k ) {
      int res = context.checkCallingOrSelfPermission( perms[k] );
      if ( res != PackageManager.PERMISSION_GRANTED ) {
        // Toast.makeText( mActivity, "TopoDroid must have " + perms[k], Toast.LENGTH_LONG ).show();
	return -1;
      }
    }
    int ret = 0;
    int flag = 1;
    for ( ; k<6; ++k ) {
      int res = context.checkCallingOrSelfPermission( perms[k] );
      if ( res != PackageManager.PERMISSION_GRANTED ) {
        // Toast.makeText( mActivity, "TopoDroid may need " + perms[k], Toast.LENGTH_LONG ).show();
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
