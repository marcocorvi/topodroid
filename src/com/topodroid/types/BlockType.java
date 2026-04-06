/* @file BlockType.java
 *
 * @author marco corvi
 * @date mar 2026
 *
 * @brief TopoDroid types of DBlock
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.types;

import com.topodroid.util.TDColor;

public class BlockType
{
  // BLOCK TYPES
  public static final int INVALID   = -1;
  public static final int BLANK     =  0;
  public static final int MAIN_LEG  =  1; // primary leg shot
  public static final int SEC_LEG   =  2; // additional shot of a centerline leg
  public static final int BLANK_LEG =  3; // blank centerline leg-shot
  public static final int BACK_LEG  =  4; // 
  public static final int BLUNDER   =  5; // UNUSED
  // splays must come last
  public static final int SPLAY     =  6;
  public static final int X_SPLAY   =  7; // FIXME_X_SPLAY cross splay
  public static final int H_SPLAY   =  8; // FIXME_H_SPLAY horizontal splay
  public static final int V_SPLAY   =  9; // FIXME_V_SPLAY vertical splay

  public static final int SCAN      = 10; // FIXME_S_SPLAY scan splay
  public static final int XSCAN     = 11; // FIXME_S_SPLAY scan splay
  public static final int HSCAN     = 12; // FIXME_S_SPLAY scan splay
  public static final int VSCAN     = 13; // FIXME_S_SPLAY scan splay


  /** block-type to color-table; used in the shot listing
   */
  public static final int[] mTypeColor = {
    TDColor.LIGHT_PINK,   // 0 blank
    TDColor.WHITE,        // 1 midline
    TDColor.LIGHT_GRAY,   // 3 sec. leg
    TDColor.VIOLET,       // 4 blank leg
    TDColor.LIGHT_YELLOW, // 5 back leg
    TDColor.PINK,         // 6 blunder UNUSED
    TDColor.LIGHT_BLUE,   // 6 splay
    TDColor.GREEN,        // 7 FIXME_X_SPLAY X splay
    TDColor.DARK_BLUE,    // 8 H_SPLAY
    TDColor.DEEP_BLUE,    // 9 V_SPLAY
    TDColor.YELLOW_GREEN, // 10 SCAN    // all scan-set are shown yellow-green in the shot list - distinguished by a character at the end
    TDColor.YELLOW_GREEN, // 11 XSCAN
    TDColor.YELLOW_GREEN, // 12 HSCAN
    TDColor.YELLOW_GREEN, // 13 VSCAN
    TDColor.GREEN
  };

  /** @return true if the given type is BLANK or BLANK_LEG
   * @param t   given type
   */
  public static boolean isTypeBlank( int t ) { return t == BLANK || t == BLANK_LEG; }

  public static final int[] LegToBlock = {
    BLANK,    // 0: blank, leg, or splay
    SEC_LEG,
    X_SPLAY,
    BACK_LEG, // 3
    H_SPLAY,
    V_SPLAY,  // 5
    SCAN,
    XSCAN,    // 7
    HSCAN,
    VSCAN,    // 9
    BLUNDER,  // 10
    INVALID         // invalid
  };


}
