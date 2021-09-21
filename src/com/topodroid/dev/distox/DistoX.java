/* @file DistoX.java
 *
 * @author marco corvi
 * @date feb 2021 (extracted from Device.java)
 *
 * @brief TopoDroid DistoX commands 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox;

// import com.topodroid.utils.TDLog;

public class DistoX
{
  // commands
  public static final int CALIB_OFF        = 0x30;
  public static final int CALIB_ON         = 0x31;
  public static final int SILENT_ON        = 0x32;
  public static final int SILENT_OFF       = 0x33;
  public static final int DISTOX_OFF       = 0x34;
  public static final int DISTOX_35        = 0x35;
  public static final int LASER_ON         = 0x36;
  public static final int LASER_OFF        = 0x37;
  public static final int MEASURE          = 0x38;

  // error codes
  public static final int DISTOX_ERR_OK           =  0; // OK: no error
  public static final int DISTOX_ERR_HEADTAIL     = -1;
  public static final int DISTOX_ERR_HEADTAIL_IO  = -2;
  public static final int DISTOX_ERR_HEADTAIL_EOF = -3;
  public static final int DISTOX_ERR_CONNECTED    = -4;
  public static final int DISTOX_ERR_OFF          = -5; // distox has turned off
  public static final int DISTOX_ERR_PROTOCOL     = -6; // protocol is null

}
