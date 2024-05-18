/* @file GeoCode.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid geo code
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDColor;

class GeoCode
{
  static int mGeoColor[] = { 
    0x99ffffff & TDColor.GREEN,
    0x99ffffff & TDColor.DARK_BLUE,
    0x99ffffff & TDColor.BROWN,
    0x99ffffff & TDColor.VIOLET,
    0x99ffffff & TDColor.DARK_GREEN,
    0x99ffffff & TDColor.ORANGE,
    0x99ffffff & TDColor.BLUE,
    0x99ffffff & TDColor.PINK,
    0x99ffffff & TDColor.YELLOW_GREEN,
    0x99ffffff & TDColor.DARK_BROWN
  };
  static final int TYPE_MAX = 10;

  int    mType; // class of geocode
  String mCode; // code
  String mDesc; // description
  boolean mSelected;

  /** cstr
   */
  GeoCode( int t, String c, String d )
  {
    mType = t % TYPE_MAX;
    mCode = c;
    mDesc = d;
    mSelected = false;
  }

  int getColorByType()
  {
    return mGeoColor[ mType % TYPE_MAX ];
  }

}
