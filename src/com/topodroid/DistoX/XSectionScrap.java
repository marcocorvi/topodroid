/* @file XSectionScrap.java    
 *
 * @author marco corvi
 * @date nov 2015
 *
 * @brief TopoDroid drawing: therion export x-section scrap
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// used to export x-section scrap in therion file
class XSectionScrap
{
  String name; // scrap name
  float x, y;  // offset

  /** cstr
   * @param nm  name
   * @param xx  X offset
   * @param yy  Y offset
   */
  XSectionScrap( String nn, float xx, float yy )
  {
    name = nn;
    x = xx;
    y = yy;
  }
}

