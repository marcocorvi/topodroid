/* @file DisplayMode.java
 *
 * @author marco corvi
 * @date oct 2015
 *
 * @brief TopoDroid drawing: display mode consts
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class DisplayMode
{
  static final int DISPLAY_NONE    = 0;
  static final int DISPLAY_LEG     = 0x01;
  static final int DISPLAY_SPLAY   = 0x02;
  static final int DISPLAY_STATION = 0x04;
  static final int DISPLAY_GRID    = 0x08;
  static final int DISPLAY_OUTLINE = 0x10; // whether to display only the outline
  static final int DISPLAY_SECTION = 0x0d;
  static final int DISPLAY_ALL     = 0x0f;
  // private static final int DISPLAY_MAX     = 4;
}
