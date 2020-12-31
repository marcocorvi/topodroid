/* @file PointScale.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing points scale
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.common;

public class PointScale
{
  public static final int SCALE_NONE = -3; // used to force scaling
  public static final int SCALE_XS = -2;
  public static final int SCALE_S  = -1;
  public static final int SCALE_M  = 0;
  public static final int SCALE_L  = 1;
  public static final int SCALE_XL = 2;

  private static final String[] SCALE_STR = { "xs", "s", "m", "l", "xl" };
  private static final String[] SCALE_STR_UC = { "XS", "S", "M", "L", "XL" };

  public static String scaleToString( int scale ) 
  { return ( scale >= SCALE_XS && scale <= SCALE_XL )? SCALE_STR[ scale+2 ] : "-"; }

  public static String scaleToStringUC( int scale ) 
  { return ( scale >= SCALE_XS && scale <= SCALE_XL )? SCALE_STR_UC[ scale+2 ] : "-"; }

}

