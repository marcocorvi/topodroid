/* @file LegType.java
 *
 * @author marco corvi
 * @date oct 2015
 *
 * @brief TopoDroid shot "leg" types
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.common;

public class LegType
{
  public static final int INVALID = -1;
  public static final int NORMAL  = 0;
  public static final int EXTRA   = 1; // additional leg shots
  public static final int XSPLAY  = 2; // cross splay
  public static final int BACK    = 3; 
  public static final int HSPLAY  = 4; // horizontal splay
  public static final int VSPLAY  = 5; // vertical splay

  public static int nextSplayClass( int type ) 
  {
    switch( type ) {
      case NORMAL: return XSPLAY;
      case XSPLAY: return HSPLAY;
      case HSPLAY: return VSPLAY;
      case VSPLAY: return NORMAL;
    }
    return INVALID;
  }
}
