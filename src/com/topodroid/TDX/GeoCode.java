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

  private int    mType;    // class of geocode
  private String mGeoCode; // code
  private String mDesc;    // description
  private boolean mSelected;

  /** cstr
   */
  GeoCode( int t, String c, String d )
  {
    mType = t % TYPE_MAX;
    mGeoCode = c;
    mDesc = d;
    mSelected = false;
  }

  /** @return the color for this geocode type
   */
  int getColorByType()
  {
    return mGeoColor[ mType % TYPE_MAX ];
  }

  /** @return true if the geocode is equal to a given code
   * @param geocode  the given code
   */
  boolean hasGeoCode( String geocode ) { return mGeoCode.equals( geocode ); }

  /** #return the geocode string
   */
  String getGeoCode() { return mGeoCode; }

  /** @return true is this geocode is selected ( in the geodoces dialog)
   */
  boolean isSelected() { return mSelected; }

  /** set whether this geocode is selected ( in the geocodes dialog )
   * @param selected whether this geocode is selected
   */
  void setSelected( boolean selected ) { mSelected = selected; }

  // int getType() { return mType; }

  /** set this geocode selected if it is equal to a given code
   * @param geocode the goven code
   * @return true if this geocode has been selected
   */
  boolean selectByGeoCode( String geocode ) 
  {
    if ( mGeoCode.equals( geocode ) ) {
      mSelected = true;
      return true;
    }
    return false;
  }

  /** @return the geocode description
   */
  String getDescription() { return mDesc; }

}
