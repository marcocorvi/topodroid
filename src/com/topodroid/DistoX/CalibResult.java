/** @file CalibResult.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calib info (name, date, comment etc)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20140720 created
 */
package com.topodroid.DistoX;

class CalibResult
{
  public float error;      
  public float max_error;      
  public int iterations;  

  CalibResult()
  {
    error = 0.0f;
    max_error = 0.0f;
    iterations = 0;
  }
}

