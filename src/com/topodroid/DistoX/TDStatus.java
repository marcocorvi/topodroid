/* @file TDStatus.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid status (for data nd sketches)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class TDStatus 
{
  static final int NORMAL    = 0;   // item (shot, plot, fixed) status
  static final int DELETED   = 1;  
  static final int OVERSHOOT = 2;  
  static final int CHECK     = 3;  

  // static final int CALIB     = 11;  
  // static final int GM        = 12;  

  static final String NORMAL_STR = "0"; // NORMAL as string
}
