/* @file ManualCalibration.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid instrument calibration for manual shots
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;


class ManualCalibration
{
  static float mLength  = 0; // calibration of manually inputed data: length [m]
  static float mAzimuth = 0; // [degrees]
  static float mClino   = 0; // [degrees]
  static boolean mLRUD  = false; // whether length applies also to LRUD or not

  /** reset the static fields
   */
  static void reset() 
  {
    mLength  = 0; 
    mAzimuth = 0;
    mClino   = 0;
    mLRUD    = false;
  }

}

