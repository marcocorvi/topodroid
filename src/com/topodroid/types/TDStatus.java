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
package com.topodroid.utils;

public class TDStatus 
{
  public static final int NORMAL    = 0;   // item (shot, plot, fixed) status
  public static final int DELETED   = 1;  
  public static final int OVERSHOOT = 2;  
  public static final int CHECK     = 3;  
  public static final int BLUNDER   = 4;   // blunder leg-shot

  // static final int CALIB     = 11;  
  // static final int GM        = 12;  

  public static final String NORMAL_STR = TDString.ZERO; // NORMAL as string
}
