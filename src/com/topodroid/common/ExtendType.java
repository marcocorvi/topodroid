/* @file ExtendType.java
 *
 * @author marco corvi
 * @date dec 2020
 *
 * @brief TopoDroid shot "extend"
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.common;

public class ExtendType
{
  // public static final char[] mExtendTag = { '<', '|', '>', ' ', '-', '.', '?', '«', 'I', '»', ' ' };
  public static final char[] mExtendTag = { '<', '|', '>', ' ', '-', '.', '?', ' ', ' ', ' ', ' ' };
  public static final int EXTEND_LEFT   = -1;
  public static final int EXTEND_VERT   =  0;
  public static final int EXTEND_RIGHT  = 1;
  public static final int EXTEND_IGNORE = 2;
  public static final int EXTEND_HIDE   = 3;
  public static final int EXTEND_START  = 4;

  public static final float STRETCH_NONE = 0.0f;

  public static final int EXTEND_UNSET  = 5;
  // public static final int EXTEND_FLEFT  = 6; // LEFT = FLEFT - FVERT
  // public static final int EXTEND_FVERT  = 7;
  // public static final int EXTEND_FRIGHT = 8;
  // public static final int EXTEND_FIGNORE = 9; // overload of IGNORE for splays

  public static final int EXTEND_NONE   = EXTEND_VERT;

}
