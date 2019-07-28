/* @file CalibResult.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calib info (name, date, comment etc)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class CalibResult
{
  float delta_bh; // BH algo original delta
  float error;  // average error [deg]
  float stddev; // error stddev [deg]
  float max_error;      // [deg]
  int iterations;  

  CalibResult()
  {
    delta_bh   = 0.0f;
    error      = 0.0f;
    stddev     = 0.0f;
    max_error  = 0.0f;
    iterations = 0;
  }
}

