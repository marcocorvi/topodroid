/** @file TdmStation.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager survey station
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

class TdmStation
{
  String mName;      // name
  float e, s, h, v;  // 3D+H coordinates
  TdmSurvey mSurvey; // survey this station belongs to

  /** cstr
   * @param name     station name
   * @param e0       east coord
   * @param s0       south coord 
   * @param h0       horizontal
   * @param v0       vertical (downwards)
   * @param survey   parent survey
   */
  TdmStation( String name, float e0, float s0, float h0, float v0, TdmSurvey survey )
  {
    mName = name;
    e = e0;
    s = s0;
    h = h0;
    v = v0;
    mSurvey = survey;
  }

  /** get the station full-name
   * @return the station fullname
   */
  String getFullName() 
  {
    if ( mName.indexOf('@') > 0 ) {
      return mName + '.' + mSurvey.getFullName();
    }
    return mName + '@' + mSurvey.getFullName();
  }

  /** get the station name
   * @return the station name
   */
  String getName()
  {
    return mName; 
  }

}
