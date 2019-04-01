/* @file DrawingLevel.java
 *
 * @author marco corvi
 * @date mar 2019
 *
 * @brief TopoDroid drawing canvas levels
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *  @note this is actually DrawingUtilPortrait.java as the Landscape is never used
 *        and it is made all static (state-less)
 */
package com.topodroid.DistoX;

class DrawingLevel
{
  static final int LEVEL_BASE   =   1;
  static final int LEVEL_FLOOR  =   2;  // soil features
  static final int LEVEL_FILL   =   4;  // deposits, fillings
  static final int LEVEL_CEIL   =   8;  // ceiling
  static final int LEVEL_ARTI   =  16;  // man made
  // static final int LEVEL_FORM   =  32;  // symbolic 
  // static final int LEVEL_WATER  =  64;
  // static final int LEVEL_TEXT   = 128;  // text

  static final int LEVEL_USER   =  1;
  static final int LEVEL_LABEL  = 17;
  static final int LEVEL_WALL   = 31;
  static final int LEVEL_WATER  =  7;

  static final int LEVEL_ANY    = 31;
  static final int LEVEL_DEFAULT  = 1;
}

