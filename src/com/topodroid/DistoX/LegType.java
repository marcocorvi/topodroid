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
package com.topodroid.DistoX;

class LegType
{
  static final int INVALID = -1;
  static final int NORMAL  = 0;
  static final int EXTRA   = 1; // additional leg shots
  static final int XSPLAY  = 2; // cross splay
  static final int BACK    = 3; 
  static final int HSPLAY  = 4; // horizontal splay
  static final int VSPLAY  = 5; // vertical splay

  static int nextSplayType( int type ) 
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
