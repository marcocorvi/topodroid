/* @file TDRequest.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid main activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class TDRequest
{
  static final int SENSOR_ACTIVITY_SHOTWINDOW   = 1;
  static final int REQUEST_ENABLE_BT = 2;
  static final int INFO_ACTIVITY_SHOTWINDOW     = 3;
  // private static final int EXTERNAL_ACTIVITY = 4;
  static final int REQUEST_DEVICE    = 5; 
  // static final int QCAM_COMPASS_DRAWWINDOW  = 6;
  static final int PLOT_RELOAD = 7;

  static final int REQUEST_TDCONFIG = 10;
  static final String TDCONFIG_PATH = "TdManagerConfig";

  static final int CAPTURE_IMAGE_SHOTWINDOW = 100;
  static final int CAPTURE_IMAGE_DRAWWINDOW = 101;

  static final int RESULT_TDCONFIG_OK     = 0;
  static final int RESULT_TDCONFIG_NONE   = 1;
  static final int RESULT_TDCONFIG_DELETE = 2;

}
