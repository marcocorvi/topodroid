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
package com.topodroid.utils;

public class TDRequest
{
  public static final int SENSOR_ACTIVITY_SHOTWINDOW   = 1;
  public static final int REQUEST_ENABLE_BT = 2;
  public static final int INFO_ACTIVITY_SHOTWINDOW     = 3;
  // private static final int EXTERNAL_ACTIVITY = 4;
  public static final int REQUEST_DEVICE    = 5; 
  // public static final int QCAM_COMPASS_DRAWWINDOW  = 6;
  public static final int PLOT_RELOAD = 7;
  public static final int REQUEST_GET_IMPORT = 8;
  public static final int REQUEST_GET_EXPORT = 9;
  public static final int REQUEST_GET_GPS_IMPORT = 10;
  public static final int REQUEST_SETTINGS = 11;

  // public static final int REQUEST_TREE_URI = 20;

  public static final int REQUEST_TDCONFIG = 200;
  public static final String TDCONFIG_PATH = "TdManagerConfig"; // request extra key

  public static final int CAPTURE_IMAGE_SHOTWINDOW = 100;
  public static final int CAPTURE_IMAGE_DRAWWINDOW = 101;

  public static final int RESULT_TDCONFIG_OK     = 0;
  public static final int RESULT_TDCONFIG_NONE   = 1;
  public static final int RESULT_TDCONFIG_DELETE = 2;

}
