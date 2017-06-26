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

  static boolean checkLocation( Context context )
  {
    PackageManager pm = context.getPackageManager();
    return pm.hasSystemFeature(PackageManager.FEATURE_LOCATION)
        && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
  }

  static boolean checkCamera( Context context )
  {
    PackageManager pm = context.getPackageManager();
    return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
  }

  static boolean checkMultitouch( Context context )
  {
    return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
  }

  static boolean checkMicrophone( Context context )
  {
    return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_MICROPHONE );
  }

  static boolean checkBluetooth( Context context )
  {
    return context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH );
  }
}
