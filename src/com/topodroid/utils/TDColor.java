/* @file TDColor.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid colors
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.utils;

import com.topodroid.TDX.TDandroid;

public class TDColor
{
  public static final int BLACK        = 0xff000000;
  public static final int DARK_GREEN   = 0xff004949;
  public static final int GREEN        = 0xff009292;
  public static final int PINK         = 0xffff6db6;
  public static final int LIGHT_PINK   = 0xffffb677;
  public static final int DARK_VIOLET  = 0xff490092;
  public static final int DARK_BLUE    = 0xff006ddb;
  public static final int DEEP_BLUE    = 0xff3b40db;
  public static final int VIOLET       = 0xffb66dff;
  public static final int BLUE         = 0xff6db6ff;
  public static final int LIGHT_BLUE   = 0xffb6dbff;
  public static final int DARK_BROWN   = 0xff920000;
  public static final int BROWN        = 0xff924900;
  public static final int ORANGE       = 0xffdbd100;
  public static final int LIGHT_GREEN  = 0xff24ff24;
  public static final int YELLOW       = 0xffffff6d;
  public static final int YELLOW_GREEN = 0xff99cc3d;
  public static final int FIXED_RED    = 0xffff3333;
  public static final int FIXED_YELLOW = 0xffffff33;
  public static final int FIXED_ORANGE = 0xffff9966;
  public static final int DARK_ORANGE  = 0xffff6600;
  public static final int FIXED_BLUE   = 0xff6699ff;
  public static final int VERYDARK_GRAY = 0xff444444;
  public static final int DARK_GRAY    = 0xff666666;
  // public static final int LIGHT_GRAY   = 0xffaaaaaa;
  public static final int MID_GRAY     = 0xff999999;
  public static final int LIGHT_GRAY   = 0xffcccccc;
  public static final int DARK_GRID    = 0x99666666;
  public static final int GRID         = 0x99999999;
  public static final int LIGHT_GRID   = 0x99cccccc;
  public static final int REDDISH      = 0xffff66cc;
  public static final int FULL_RED     = 0xffff0000;
  public static final int FULL_GREEN   = 0xff00ff00;
  public static final int FULL_BLUE    = 0xff0000ff;
  public static final int FULL_VIOLET  = 0xffff00ff;
  public static final int WHITE        = 0xffffffff;
  public static final int MID_RED      = 0xffbb3300;
  public static final int DARK_RED     = 0xff662200;
  public static final int BACK_GREEN   = 0x66009292; // transparent green
  public static final int BACK_VIOLET  = 0x66b66dff; // transparent violet
  public static final int BACK_YELLOW  = 0x33ffff6d; // transparent yellow
  public static final int LIGHT_YELLOW = 0xffffff9f;
  public static final int MID_BLUE     = 0xff5992d7;
  public static final int TOAST_BLUE   = 0xff7faaff; // FIXED_BLUE=0xff6699ff;

  public static final int HIGH_PINK    = 0xffff9999; // pink
  public static final int HIGH_GREEN   = 0x6600cc00; // green
  public static final int HIGH_RED     = FIXED_RED; // 0xffff3333; // reddish

  public static final int[] mTDColors = { // groups of four
    FULL_VIOLET,   VIOLET,       PINK,         LIGHT_PINK,   // DARK_VIOLET,
    FULL_BLUE,     DARK_BLUE,    DEEP_BLUE,    BLUE,
    LIGHT_BLUE,    MID_BLUE,     FIXED_BLUE,   TOAST_BLUE,
    DARK_GREEN,    FULL_GREEN,   GREEN,        LIGHT_GREEN,
    YELLOW_GREEN,  YELLOW,       FIXED_YELLOW, LIGHT_YELLOW,
    ORANGE,        FIXED_ORANGE, DARK_BROWN,   BROWN,        // DARK_ORANGE,
    DARK_RED,      MID_RED,      FULL_RED,     FIXED_RED,    // REDDISH,
    DARK_GRAY,     MID_GRAY,     LIGHT_GRAY,   WHITE         // VERYDARK_GRAY,
  };

  public static final int TITLE_NORMAL     = TDandroid.TITLE_NORMAL; // FIXED_BLUE same as in values/styles.xml
  public static final int TITLE_NORMAL2    = TDandroid.TITLE_NORMAL2; 
  public static final int TITLE_BACKSHOT   = TDandroid.TITLE_BACKSHOT; // DARK BLUE
  public static final int TITLE_BACKSIGHT  = TDandroid.TITLE_BACKSIGHT; // VIOLET
  public static final int TITLE_TRIPOD     = TDandroid.TITLE_TRIPOD; // PINK
  public static final int TITLE_TOPOROBOT  = TDandroid.TITLE_TOPOROBOT; // ORANGE
  public static final int TITLE_ANOMALY    = TDandroid.TITLE_ANOMALY; // BRIGHT RED

  public static final int TOAST_NORMAL  = TOAST_BLUE; // 0xff7faaff; // FIXED_BLUE=0xff6699ff;
  public static final int TOAST_WARNING = ORANGE; // 0xfffdf322; // 0xffebe110;
  public static final int TOAST_ERROR   = VIOLET; // 0xfff687c2; // 0xfff676df;

  public static final int NORMAL       = WHITE;
  public static final int NORMAL2      = LIGHT_GRAY;
  public static final int CONNECTED    = FIXED_RED;
  public static final int COMPUTE      = VIOLET;
  public static final int NAME_COLOR   = 0xff66cc99;
  // public static final int PREF_TEXT    = 0xff66a8dd;
  public static final int SYMBOL_TAB   = 0xff80cbc4;
  public static final int SYMBOL_ON    = LIGHT_BLUE;

  public static final int TRANSPARENT  = 0x00000000;

  public static final int SPLAY_LIGHT   = BLUE;
  public static final int SPLAY_NORMAL  = MID_BLUE;
  public static final int SPLAY_LRUD    = GREEN;
  public static final int SPLAY_COMMENT = VERYDARK_GRAY;

  public static final int SEARCH        = 0xff996600; // search result highlight color
}
  
