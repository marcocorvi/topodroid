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
package com.topodroid.Cave3X;


class ManualCalibration
{
  static float mLength  = 0; // calibration of manually inputed data: length
  static float mAzimuth = 0;
  static float mClino   = 0;
  static boolean mLRUD  = false; // whether length applies also to LRUD or not

  static void reset() 
  {
    mLength  = 0; 
    mAzimuth = 0;
    mClino   = 0;
    mLRUD    = false;
  }

}

