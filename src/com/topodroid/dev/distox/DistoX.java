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

  // error codes
  public static final int DISTOX_ERR_OK           =  0; // OK: no error
  public static final int DISTOX_ERR_HEADTAIL     = -1;
  public static final int DISTOX_ERR_HEADTAIL_IO  = -2;
  public static final int DISTOX_ERR_HEADTAIL_EOF = -3;
  public static final int DISTOX_ERR_CONNECTED    = -4;
  public static final int DISTOX_ERR_OFF          = -5; // distox has turned off
  public static final int DISTOX_ERR_PROTOCOL     = -6; // protocol is null

}
